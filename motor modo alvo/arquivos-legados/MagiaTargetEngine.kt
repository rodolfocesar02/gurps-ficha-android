package com.gurps.ficha.domain.magias

import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.regras_prerequisitos.PreRequisitoParser
import com.gurps.ficha.regras_prerequisitos.PreRequisitoType
import java.util.LinkedHashMap

class MagiaTargetEngine(
    private val dataRepository: MagiaPlannerDataSource
) {
    private val parseCache = object : LinkedHashMap<String, PreRequisitoParser.ParseResult>(512, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, PreRequisitoParser.ParseResult>?): Boolean {
            return size > 512
        }
    }

    private val modoAlvoCache = object : LinkedHashMap<String, ModoAlvoResult>(196, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ModoAlvoResult>?): Boolean {
            return size > 196
        }
    }

    data class ModoAlvoResult(
        val ids: List<String>,
        val parcial: Boolean = false,
        val aviso: String? = null
    )

    private data class GuardrailBudget(
        val startedAtMs: Long = System.currentTimeMillis(),
        val maxMs: Long = 1800,
        val maxNodes: Int = 18000,
        val maxDepth: Int = 6,
        var nodes: Int = 0,
        var limiteMotivo: String? = null
    ) : MagiaDependencyPlanner.Budget {
        override fun step(amount: Int): Boolean {
            nodes += amount
            if (nodes > maxNodes) {
                limiteMotivo = "limite de análise de nós"
                return false
            }
            if (System.currentTimeMillis() - startedAtMs > maxMs) {
                limiteMotivo = "limite de tempo"
                return false
            }
            return true
        }

        override fun allowDepth(depth: Int): Boolean {
            if (depth > maxDepth) {
                limiteMotivo = "limite de profundidade"
                return false
            }
            return true
        }

        fun avisoParcial(): String {
            val motivo = limiteMotivo ?: "limite de segurança"
            return "Trilha parcial (guardrail: $motivo)."
        }
    }

    fun listaRelacionadosMagiaAlvo(
        alvo: MagiaDefinicao,
        personagem: Personagem
    ): List<String> {
        return calcularModoAlvo(alvo, personagem).ids
    }

    fun calcularModoAlvo(
        alvo: MagiaDefinicao,
        personagem: Personagem,
        contextoKey: String? = null
    ): ModoAlvoResult {
        val chaveCache = contextoKey?.takeIf { it.isNotBlank() }
        if (chaveCache != null) {
            synchronized(this) {
                modoAlvoCache[chaveCache]?.let { return it }
            }
        }

        val ids = mutableListOf<String>()
        ids.add(alvo.id)
        if (dataRepository.magiaSemPreRequisito(alvo)) {
            val result = ModoAlvoResult(ids = ids)
            if (chaveCache != null) synchronized(this) { modoAlvoCache[chaveCache] = result }
            return result
        }

        val budget = GuardrailBudget()
        val planner = MagiaDependencyPlanner(
            dataRepository = dataRepository,
            parseFn = ::parseCached,
            budget = budget
        )
        val plan = planner.planForTarget(alvo, personagem)
        plan.prerequisiteIds.forEach { id ->
            if (id !in ids) ids.add(id)
        }
        // Sempre mostrar faltas imediatas úteis mesmo em trilha parcial.
        val imediatas = coletarFallbackImediato(alvo, personagem, limite = 40)
        imediatas.forEach { id ->
            if (id !in ids) ids.add(id)
        }

        val result = if (plan.parcial || budget.limiteMotivo != null) {
            ModoAlvoResult(
                ids = ids,
                parcial = true,
                aviso = budget.avisoParcial()
            )
        } else {
            ModoAlvoResult(ids = ids)
        }

        if (chaveCache != null) {
            synchronized(this) {
                modoAlvoCache[chaveCache] = result
            }
        }
        return result
    }

    private fun parseCached(raw: String): PreRequisitoParser.ParseResult {
        synchronized(this) {
            parseCache[raw]?.let { return it }
        }
        val parsed = PreRequisitoParser.parse(raw)
        synchronized(this) {
            parseCache[raw] = parsed
        }
        return parsed
    }

    private fun coletarFallbackImediato(
        alvo: MagiaDefinicao,
        personagem: Personagem,
        limite: Int
    ): List<String> {
        val out = linkedSetOf<String>()
        val visitados = mutableSetOf<String>()
        val known = personagem.magias.map { it.definicaoId }.toSet()
        fun addBasicsForSchool(schoolRaw: String, quantidade: Int) {
            val school = normalize(schoolRaw)
            if (school.isBlank()) return
            val candidatas = dataRepository.magias
                .filter { magia ->
                    magia.id !in known &&
                        magia.id !in out &&
                        magia.escola.orEmpty().any { schoolMatches(it, school) }
                }
                .sortedWith(
                    compareBy<MagiaDefinicao> {
                        if (dataRepository.validarPreRequisitosMagia(it, personagem) == null) 0 else 1
                    }
                        .thenBy { if (dataRepository.magiaSemPreRequisito(it)) 0 else 1 }
                        .thenBy { it.nome.lowercase() }
                )
                .take(quantidade.coerceAtLeast(1) + 2)
            candidatas.forEach { out.add(it.id) }
        }
        fun recurse(def: MagiaDefinicao, depth: Int) {
            if (depth > 2) return
            if (!visitados.add(def.id)) return
            val raw = dataRepository.preRequisitoNormalizadoParaAnalise(def)
            val parsed = parseCached(raw)
            parsed.tipos.forEach { tipo ->
                when (tipo) {
                    is PreRequisitoType.MagiaConhecida -> {
                        val token = normalize(tipo.nomeMagia)
                        val exact = dataRepository.magias.firstOrNull { normalize(it.nome) == token }
                        val cand = exact ?: dataRepository.magias.firstOrNull { normalize(it.nome).contains(token) }
                        if (cand != null && cand.id !in known) {
                            out.add(cand.id)
                            recurse(cand, depth + 1)
                        }
                    }
                    is PreRequisitoType.MagiaInclusaNaContagem -> {
                        val token = normalize(tipo.nomeMagia)
                        val exact = dataRepository.magias.firstOrNull { normalize(it.nome) == token }
                        val cand = exact ?: dataRepository.magias.firstOrNull { normalize(it.nome).contains(token) }
                        if (cand != null && cand.id !in known) {
                            out.add(cand.id)
                            recurse(cand, depth + 1)
                        }
                    }
                    is PreRequisitoType.MagiasEscola -> {
                        addBasicsForSchool(tipo.escola, tipo.quantidade)
                    }
                    is PreRequisitoType.MagiasEmEscolasDiferentes -> {
                        val schools = dataRepository.magias
                            .flatMap { it.escola.orEmpty() }
                            .map(::normalize)
                            .filter { it.isNotBlank() }
                            .distinct()
                        schools.forEach { school ->
                            if (out.size >= limite) return@forEach
                            addBasicsForSchool(school, tipo.magiasPorEscola)
                        }
                    }
                    else -> Unit
                }
            }
        }
        recurse(alvo, 0)
        if (out.isEmpty()) {
            dataRepository.magias
                .asSequence()
                .filter { magia ->
                    magia.id !in known &&
                        dataRepository.validarPreRequisitosMagia(magia, personagem) == null
                }
                .sortedWith(
                    compareBy<MagiaDefinicao> { if (dataRepository.magiaSemPreRequisito(it)) 0 else 1 }
                        .thenBy { it.nome.lowercase() }
                )
                .take(limite)
                .forEach { out.add(it.id) }
        }
        return out.take(limite)
    }

    private fun schoolMatches(rawSchool: String, targetSchool: String): Boolean {
        val normalized = normalize(rawSchool)
        if (normalized == targetSchool) return true
        val boundary = Regex("\\b${Regex.escape(targetSchool)}\\b")
        if (boundary.containsMatchIn(normalized)) return true
        val tokens = normalized
            .split("/", ";", ",")
            .map(::normalize)
            .filter { it.isNotBlank() }
        return tokens.any { it == targetSchool }
    }

    private fun normalize(raw: String): String {
        return raw
            .lowercase()
            .stripDiacritics()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun String.stripDiacritics(): String {
        return java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
    }
}
