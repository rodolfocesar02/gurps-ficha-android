package com.gurps.ficha.domain.magias

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.MagiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.regras_prerequisitos.PreRequisitoParser
import com.gurps.ficha.regras_prerequisitos.PreRequisitoType
import java.text.Normalizer

class MagiaDependencyPlanner(
    private val dataRepository: DataRepository,
    private val parseFn: (String) -> PreRequisitoParser.ParseResult,
    private val budget: Budget
) {
    interface Budget {
        fun step(amount: Int = 1): Boolean
        fun allowDepth(depth: Int): Boolean
    }

    data class PlanResult(
        val prerequisiteIds: List<String>,
        val parcial: Boolean = false
    )

    private val byId: Map<String, MagiaDefinicao> = dataRepository.magias.associateBy { it.id }
    private val schoolsCatalog: Set<String> = dataRepository.magias
        .flatMap { it.escola.orEmpty() }
        .map(::norm)
        .filter { it.isNotBlank() }
        .toSet()

    fun planForTarget(target: MagiaDefinicao, personagemBase: Personagem): PlanResult {
        val knownBase = personagemBase.magias.map { it.definicaoId }.toMutableSet()
        val planned = linkedSetOf<String>()
        val visiting = mutableSetOf<String>()
        val ok = ensureSpell(
            spell = target,
            personagemBase = personagemBase,
            knownBase = knownBase,
            planned = planned,
            visiting = visiting,
            depth = 0
        )
        val prereqsOnly = planned.filterNot { it == target.id }
        return PlanResult(prerequisiteIds = prereqsOnly, parcial = !ok)
    }

    private fun ensureSpell(
        spell: MagiaDefinicao,
        personagemBase: Personagem,
        knownBase: MutableSet<String>,
        planned: LinkedHashSet<String>,
        visiting: MutableSet<String>,
        depth: Int
    ): Boolean {
        if (!budget.step() || !budget.allowDepth(depth)) return false
        if (spell.id in knownBase || spell.id in planned) return true
        if (!visiting.add(spell.id)) return false

        if (!dataRepository.magiaSemPreRequisito(spell)) {
            val raw = dataRepository.preRequisitoNormalizadoParaAnalise(spell)
            val parsed = parseFn(raw)
            val okTerms = ensureTerms(parsed.terms, personagemBase, knownBase, planned, visiting, depth + 1)
            if (!okTerms) {
                visiting.remove(spell.id)
                return false
            }
        }

        planned.add(spell.id)
        visiting.remove(spell.id)
        return true
    }

    private fun ensureTerms(
        terms: List<PreRequisitoParser.PreRequisitoTerm>,
        personagemBase: Personagem,
        knownBase: MutableSet<String>,
        planned: LinkedHashSet<String>,
        visiting: MutableSet<String>,
        depth: Int
    ): Boolean {
        for (term in terms) {
            if (!budget.step()) return false
            val alternatives = term.alternatives
            if (alternatives.isEmpty()) continue
            val ranked = alternatives.sortedBy { estimateAlternativeCost(it, personagemBase, knownBase, planned) }
            var satisfied = false
            for (alt in ranked) {
                if (!budget.step()) return false
                val plannedSnapshot = LinkedHashSet(planned)
                val ok = applyAlternative(alt, personagemBase, knownBase, planned, visiting, depth + 1)
                if (ok) {
                    satisfied = true
                    break
                }
                planned.clear()
                planned.addAll(plannedSnapshot)
            }
            if (!satisfied) return false
        }
        return true
    }

    private fun applyAlternative(
        types: List<PreRequisitoType>,
        personagemBase: Personagem,
        knownBase: MutableSet<String>,
        planned: LinkedHashSet<String>,
        visiting: MutableSet<String>,
        depth: Int
    ): Boolean {
        for (type in types) {
            if (!budget.step()) return false
            val ok = when (type) {
                is PreRequisitoType.MagiaConhecida -> ensureNamedMagic(type.nomeMagia, personagemBase, knownBase, planned, visiting, depth + 1)
                is PreRequisitoType.MagiaInclusaNaContagem -> ensureNamedMagic(type.nomeMagia, personagemBase, knownBase, planned, visiting, depth + 1)
                is PreRequisitoType.MagiasEscola -> ensureSchoolCount(type.escola, type.quantidade, personagemBase, knownBase, planned, visiting, depth + 1)
                is PreRequisitoType.MagiasEmEscolasDiferentes -> ensureDifferentSchools(type, personagemBase, knownBase, planned, visiting, depth + 1)
                is PreRequisitoType.QuantidadeMagiasPorTemas -> ensureThemeCount(type.temas, type.quantidade, personagemBase, knownBase, planned, visiting, depth + 1)
                is PreRequisitoType.QualquerMagiaComNome -> ensureThemeCount(setOf(type.trechoNome), 1, personagemBase, knownBase, planned, visiting, depth + 1)
                is PreRequisitoType.QuantidadeOutrasMagias -> ensureOtherMagics(type, personagemBase, knownBase, planned, visiting, depth + 1)
                else -> true
            }
            if (!ok) return false
        }
        return true
    }

    private fun ensureNamedMagic(
        rawName: String,
        personagemBase: Personagem,
        knownBase: MutableSet<String>,
        planned: LinkedHashSet<String>,
        visiting: MutableSet<String>,
        depth: Int
    ): Boolean {
        val token = norm(rawName)
        if (token.isBlank()) return false
        val exact = dataRepository.magias
            .filter { norm(it.nome) == token }
            .sortedBy { heuristicCost(it, personagemBase, planned) }
        val candidates = if (exact.isNotEmpty()) {
            exact
        } else {
            dataRepository.magias
                .mapNotNull { magia ->
                    val score = namedMatchScore(norm(magia.nome), token) ?: return@mapNotNull null
                    magia to score
                }
                .sortedWith(
                    compareBy<Pair<MagiaDefinicao, Int>> { it.second }
                        .thenBy { heuristicCost(it.first, personagemBase, planned) }
                )
                .map { it.first }
        }
        for (candidate in candidates) {
            if (!budget.step()) return false
            if (ensureSpell(candidate, personagemBase, knownBase, planned, visiting, depth + 1)) return true
        }
        return false
    }

    private fun ensureSchoolCount(
        rawSchool: String,
        quantity: Int,
        personagemBase: Personagem,
        knownBase: MutableSet<String>,
        planned: LinkedHashSet<String>,
        visiting: MutableSet<String>,
        depth: Int
    ): Boolean {
        val school = norm(rawSchool)
        val required = quantity.coerceAtLeast(1)
        while (countSchool(school, personagemBase, planned) < required) {
            if (!budget.step()) return false
            val candidates = nextSchoolCandidates(school, personagemBase, knownBase, planned)
            if (candidates.isEmpty()) return false
            var added = false
            for (candidate in candidates) {
                if (!budget.step()) return false
                val snapshot = LinkedHashSet(planned)
                val ok = ensureSpell(candidate, personagemBase, knownBase, planned, visiting, depth + 1)
                if (ok) {
                    added = true
                    break
                }
                planned.clear()
                planned.addAll(snapshot)
            }
            if (!added) return false
        }
        return true
    }

    private fun ensureDifferentSchools(
        req: PreRequisitoType.MagiasEmEscolasDiferentes,
        personagemBase: Personagem,
        knownBase: MutableSet<String>,
        planned: LinkedHashSet<String>,
        visiting: MutableSet<String>,
        depth: Int
    ): Boolean {
        val requiredSchools = req.escolasDiferentes.coerceAtLeast(1)
        val perSchool = req.magiasPorEscola.coerceAtLeast(1)
        val initialCounts = schoolCounts(personagemBase, emptySet())
        while (schoolsMeetingThreshold(personagemBase, planned, perSchool).size < requiredSchools) {
            if (!budget.step()) return false
            val current = schoolCounts(personagemBase, planned)
            val options = schoolsCatalog
                .filter { school ->
                    if (req.outrasEscolas && (initialCounts[school] ?: 0) > 0) return@filter false
                    (current[school] ?: 0) < perSchool
                }
                .sortedBy { current[it] ?: 0 }
            if (options.isEmpty()) return false
            var schoolSatisfied = false
            for (schoolChoice in options) {
                if (!budget.step()) return false
                val snapshot = LinkedHashSet(planned)
                val okSchool = ensureSchoolCount(
                    rawSchool = schoolChoice,
                    quantity = perSchool,
                    personagemBase = personagemBase,
                    knownBase = knownBase,
                    planned = planned,
                    visiting = visiting,
                    depth = depth + 1
                )
                if (okSchool) {
                    schoolSatisfied = true
                    break
                }
                planned.clear()
                planned.addAll(snapshot)
            }
            if (!schoolSatisfied) return false
        }
        return true
    }

    private fun ensureThemeCount(
        themesRaw: Set<String>,
        quantity: Int,
        personagemBase: Personagem,
        knownBase: MutableSet<String>,
        planned: LinkedHashSet<String>,
        visiting: MutableSet<String>,
        depth: Int
    ): Boolean {
        val themes = themesRaw.map(::norm).filter { it.isNotBlank() }.toSet()
        if (themes.isEmpty()) return false
        val required = quantity.coerceAtLeast(1)
        while (countTheme(themes, personagemBase, planned) < required) {
            if (!budget.step()) return false
            val candidate = dataRepository.magias
                .filter { spell ->
                    spell.id !in planned &&
                        spell.id !in knownBase &&
                        themes.any { spellNameMatches(norm(spell.nome), it) }
                }
                .sortedBy { heuristicCost(it, personagemBase, planned) }
                .firstOrNull() ?: return false
            if (!ensureSpell(candidate, personagemBase, knownBase, planned, visiting, depth + 1)) return false
        }
        return true
    }

    private fun ensureOtherMagics(
        req: PreRequisitoType.QuantidadeOutrasMagias,
        personagemBase: Personagem,
        knownBase: MutableSet<String>,
        planned: LinkedHashSet<String>,
        visiting: MutableSet<String>,
        depth: Int
    ): Boolean {
        val required = req.quantidade.coerceAtLeast(1)
        val ctx = req.contexto?.let(::norm)
        val selector: (MagiaDefinicao) -> Boolean = when {
            ctx.isNullOrBlank() -> { _ -> true }
            ctx in schoolsCatalog -> { spell -> spell.escola.orEmpty().map(::norm).any { it == ctx } }
            else -> { spell -> spellNameMatches(norm(spell.nome), ctx) }
        }
        while (planned.size < required) {
            if (!budget.step()) return false
            val candidate = dataRepository.magias
                .filter { spell -> spell.id !in planned && spell.id !in knownBase && selector(spell) }
                .sortedBy { heuristicCost(it, personagemBase, planned) }
                .firstOrNull() ?: return false
            if (!ensureSpell(candidate, personagemBase, knownBase, planned, visiting, depth + 1)) return false
        }
        return true
    }

    private fun estimateAlternativeCost(
        alt: List<PreRequisitoType>,
        personagemBase: Personagem,
        knownBase: Set<String>,
        planned: Set<String>
    ): Int {
        var score = 0
        alt.forEach { t ->
            when (t) {
                is PreRequisitoType.MagiaConhecida -> {
                    val token = norm(t.nomeMagia)
                    val satisfied = dataRepository.magias.any { spell ->
                        spell.id in knownBase || spell.id in planned &&
                            spellNameMatches(norm(spell.nome), token)
                    }
                    score += if (satisfied) 0 else 2
                }
                is PreRequisitoType.MagiasEscola -> {
                    val missing = (t.quantidade - countSchool(norm(t.escola), personagemBase, planned)).coerceAtLeast(0)
                    score += missing
                }
                is PreRequisitoType.MagiasEmEscolasDiferentes -> score += t.escolasDiferentes
                is PreRequisitoType.QuantidadeMagiasPorTemas -> score += t.quantidade
                is PreRequisitoType.QuantidadeOutrasMagias -> score += t.quantidade
                else -> score += 0
            }
        }
        return score
    }

    private fun nextSchoolCandidates(
        schoolNorm: String,
        personagemBase: Personagem,
        knownBase: Set<String>,
        planned: Set<String>
    ): List<MagiaDefinicao> {
        return dataRepository.magias
            .filter { spell ->
                spell.id !in knownBase &&
                    spell.id !in planned &&
                    spell.escola.orEmpty().map(::norm).any { it == schoolNorm }
            }
            .sortedWith(
                compareBy<MagiaDefinicao> { if (dataRepository.magiaSemPreRequisito(it)) 0 else 1 }
                    .thenBy { heuristicCost(it, personagemBase, planned) }
            )
            .take(8)
    }

    private fun countSchool(schoolNorm: String, personagemBase: Personagem, planned: Set<String>): Int {
        return schoolCounts(personagemBase, planned)[schoolNorm] ?: 0
    }

    private fun schoolCounts(personagemBase: Personagem, planned: Set<String>): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        personagemBase.magias.forEach { spell ->
            spell.escola.orEmpty().map(::norm).forEach { school ->
                counts[school] = (counts[school] ?: 0) + 1
            }
        }
        planned.forEach { id ->
            byId[id]?.escola.orEmpty().map(::norm).forEach { school ->
                counts[school] = (counts[school] ?: 0) + 1
            }
        }
        return counts
    }

    private fun schoolsMeetingThreshold(personagemBase: Personagem, planned: Set<String>, threshold: Int): Set<String> {
        return schoolCounts(personagemBase, planned)
            .filterValues { it >= threshold }
            .keys
    }

    private fun countTheme(themes: Set<String>, personagemBase: Personagem, planned: Set<String>): Int {
        val knownIds = personagemBase.magias.map { it.definicaoId }.toSet() + planned
        return knownIds.count { id ->
            val spell = byId[id] ?: return@count false
            val name = norm(spell.nome)
            themes.any { spellNameMatches(name, it) }
        }
    }

    private fun heuristicCost(spell: MagiaDefinicao, personagemBase: Personagem, planned: Set<String>): Int {
        val pre = spell.preRequisitos.orEmpty()
        val state = simulatedPersonagem(personagemBase, planned)
        val learnableNow = dataRepository.validarPreRequisitosMagia(spell, state) == null
        val complexity = norm(pre).length / 30 + Regex("\\bou\\b|\\be\\b|,").findAll(norm(pre)).count()
        return (if (learnableNow) 0 else 8) + complexity
    }

    private fun simulatedPersonagem(base: Personagem, planned: Set<String>): Personagem {
        if (planned.isEmpty()) return base
        val existing = base.magias.map { it.definicaoId }.toSet()
        val extras = planned
            .filterNot { it in existing }
            .mapNotNull { id -> byId[id] }
            .map { def ->
                MagiaSelecionada(
                    definicaoId = def.id,
                    nome = def.nome,
                    dificuldade = Dificuldade.fromSigla(def.dificuldadeFixa),
                    pontosGastos = 1,
                    pagina = def.pagina,
                    texto = def.texto,
                    classe = def.classe,
                    escola = def.escola,
                    duracao = def.duracao,
                    energia = def.energia,
                    tempoOperacao = def.tempoOperacao,
                    encantamentoAlvo = null
                )
            }
        return base.copy(magias = base.magias + extras)
    }

    private fun spellNameMatches(normalizedSpellName: String, normalizedToken: String): Boolean {
        return namedMatchScore(normalizedSpellName, normalizedToken) != null
    }

    private fun namedMatchScore(normalizedSpellName: String, normalizedToken: String): Int? {
        if (normalizedSpellName.isBlank() || normalizedToken.isBlank()) return null
        if (normalizedSpellName == normalizedToken) return 0
        val tokenRegex = Regex("\\b${Regex.escape(normalizedToken)}\\b")
        if (tokenRegex.containsMatchIn(normalizedSpellName)) return 1
        if (normalizedSpellName.startsWith("$normalizedToken ")) return 2
        if (normalizedSpellName.contains(normalizedToken)) return 3
        if (normalizedToken.contains(normalizedSpellName)) return 5
        return null
    }

    private fun norm(raw: String): String {
        val semAcento = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace("-", " ")
            .replace(Regex("[^a-z0-9\\s/+_-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
