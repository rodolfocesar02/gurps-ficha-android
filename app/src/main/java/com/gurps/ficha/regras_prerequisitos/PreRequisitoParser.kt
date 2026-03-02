package com.gurps.ficha.regras_prerequisitos

object PreRequisitoParser {

    data class PreRequisitoTerm(
        val alternatives: List<List<PreRequisitoType>>,
        val raw: String
    )

    data class ParseResult(
        val tipos: List<PreRequisitoType>,
        val terms: List<PreRequisitoTerm>,
        val bypassValidation: Boolean = false,
        val warnings: List<String> = emptyList()
    )

    fun parse(raw: String): ParseResult {
        val normalized = raw.trim().removePrefix("—").trim()
        if (normalized.isEmpty()) {
            return ParseResult(tipos = emptyList(), terms = emptyList())
        }

        if (containsBypassMarker(normalized)) {
            return ParseResult(
                tipos = emptyList(),
                terms = emptyList(),
                bypassValidation = true,
                warnings = listOf("Prerequisito especial/#: validacao automatica ignorada")
            )
        }

        val segments = normalized
            .split(",")
            .map { cleanupConnectorPrefix(it.trim()) }
            .filter { it.isNotEmpty() }

        val terms = mutableListOf<PreRequisitoTerm>()
        val allTipos = mutableListOf<PreRequisitoType>()
        var lastSchool: String? = null

        for (segment in segments) {
            val alternatives = splitAlternatives(segment)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { altRaw ->
                    val andParts = splitAndParts(altRaw)
                    val parsedParts = mutableListOf<PreRequisitoType>()
                    andParts.forEach { part ->
                        val tipo = parseSingle(part, lastSchool)
                        if (tipo is PreRequisitoType.MagiasEscola) {
                            lastSchool = tipo.escola
                        }
                        if (tipo != null) {
                            parsedParts.add(tipo)
                        }
                    }
                    parsedParts
                }
                .filter { it.isNotEmpty() }

            if (alternatives.isNotEmpty()) {
                terms.add(PreRequisitoTerm(alternatives = alternatives, raw = segment))
                alternatives.forEach { allTipos.addAll(it) }
            }
        }

        if (allTipos.none { it is PreRequisitoType.AptidaoMagica }) {
            val base = PreRequisitoType.AptidaoMagica(0)
            terms.add(0, PreRequisitoTerm(alternatives = listOf(listOf(base)), raw = "Aptidao Magica 0+"))
            allTipos.add(base)
        }

        return ParseResult(tipos = allTipos, terms = terms)
    }

    private fun parseSingle(token: String, lastSchool: String?): PreRequisitoType? {
        val tok = cleanupConnectorPrefix(token)
        if (tok.isBlank() || tok == "-") return null

        val naoPodeSer = Regex("(?i)^n[aã]o\\s+pode\\s+ser\\s+(.+)$").find(tok)
        if (naoPodeSer != null) {
            val condicoes = naoPodeSer.groupValues[1]
                .split(Regex("(?i)\\s+ou\\s+"))
                .map { cleanupNomeTema(it) }
                .filter { it.isNotBlank() }
                .toSet()
            return PreRequisitoType.NaoPodeSer(condicoes)
        }

        val attrMatch = Regex("^([A-Za-zÀ-ú]+)\\s*(\\d+)\\+").find(tok)
        if (attrMatch != null) {
            val atributo = attrMatch.groupValues[1]
            val valor = attrMatch.groupValues[2].toIntOrNull() ?: return null
            return PreRequisitoType.AttributeMin(atributo, valor)
        }

        val amMatch = Regex("^AM\\s*\\+?\\s*(\\d+)", RegexOption.IGNORE_CASE).find(tok)
        if (amMatch != null) {
            return PreRequisitoType.AptidaoMagica(amMatch.groupValues[1].toInt())
        }

        val amLongMatch = Regex(
            "^(Apt[ií]d[aã]o M[aá]gica)\\s*(?:n[ií]vel)?\\s*(\\d+)",
            RegexOption.IGNORE_CASE
        ).find(tok)
        if (amLongMatch != null) {
            return PreRequisitoType.AptidaoMagica(amLongMatch.groupValues[2].toInt())
        }

        val outrasMagias = Regex(
            "^(\\d+)\\s+outras?\\s+m[aá]g(?:ica|ia)s(?:\\s+de\\s+(.+))?$",
            RegexOption.IGNORE_CASE
        ).find(tok)
        if (outrasMagias != null) {
            val qtd = outrasMagias.groupValues[1].toIntOrNull() ?: return null
            val contexto = outrasMagias.groupValues.getOrNull(2)?.trim()?.takeIf { it.isNotBlank() }
            return PreRequisitoType.QuantidadeOutrasMagias(qtd, contexto)
        }

        val quaisquerMagiasDa = Regex(
            "^quaisquer?\\s+(\\d+)\\s+m[aá]g(?:ica|ia)s\\s+da\\s+(.+)$",
            RegexOption.IGNORE_CASE
        ).find(tok)
        if (quaisquerMagiasDa != null) {
            val qtd = quaisquerMagiasDa.groupValues[1].toIntOrNull() ?: return null
            val escolas = quaisquerMagiasDa.groupValues[2]
                .split(Regex("(?i)\\s+ou\\s+"))
                .map { cleanupNomeTema(it) }
                .filter { it.isNotBlank() }
                .toSet()
            return PreRequisitoType.QuantidadeMagiasPorEscolas(qtd, escolas)
        }

        val quaisquerMagiasTema = Regex(
            "^quaisquer?\\s+(\\d+)\\s+m[aá]g(?:ica|ia)s\\s+(.+)$",
            RegexOption.IGNORE_CASE
        ).find(tok)
        if (quaisquerMagiasTema != null) {
            val qtd = quaisquerMagiasTema.groupValues[1].toIntOrNull() ?: return null
            val tema = cleanupNomeTema(quaisquerMagiasTema.groupValues[2])
            return PreRequisitoType.QuantidadeMagiasPorTemas(qtd, setOf(tema))
        }

        val quaisquerDe = Regex(
            "^quaisquer?\\s+(\\d+)\\s+de\\s+(.+)$",
            RegexOption.IGNORE_CASE
        ).find(tok)
        if (quaisquerDe != null) {
            val qtd = quaisquerDe.groupValues[1].toIntOrNull() ?: return null
            val temas = quaisquerDe.groupValues[2]
                .split(Regex("(?i)\\s+e\\s+|\\s+ou\\s+"))
                .map { cleanupNomeTema(it) }
                .filter { it.isNotBlank() }
                .toSet()
            return PreRequisitoType.QuantidadeMagiasPorTemas(qtd, temas)
        }

        val qualquerMagia = Regex(
            "^qualquer(?:\\s+uma)?\\s+m[aá]g(?:ica|ia)(?:\\s+de)?\\s+(.+)$",
            RegexOption.IGNORE_CASE
        ).find(tok)
        if (qualquerMagia != null) {
            val trecho = cleanupNomeTema(qualquerMagia.groupValues[1])
            return PreRequisitoType.QualquerMagiaComNome(trecho)
        }

        val escolaMatch = Regex(
            "^(\\d+)\\s+m[aá]gi[cq]as\\s+(?:de|da|do|sobre)\\s+(.+)$",
            RegexOption.IGNORE_CASE
        ).find(tok)
        if (escolaMatch != null) {
            val qtd = escolaMatch.groupValues[1].toIntOrNull() ?: return null
            val escola = escolaMatch.groupValues[2].trim()
            return PreRequisitoType.MagiasEscola(qtd, escola)
        }

        val inclMatch = Regex(
            "^(?:incl\\.?\\s*(?:\\(\\s*ou\\s+inclusive\\s*\\))?|inclusive)\\s*[:.-]?\\s*(.+)$",
            RegexOption.IGNORE_CASE
        ).find(tok)
        if (inclMatch != null) {
            val magiaNome = cleanupNomeTema(inclMatch.groupValues[1])
            return PreRequisitoType.MagiaInclusaNaContagem(magiaNome, lastSchool)
        }

        val vantagemMatch = Regex("^(?:a\\s+)?vantagem\\s+(.+)$", RegexOption.IGNORE_CASE).find(tok)
        if (vantagemMatch != null) {
            return PreRequisitoType.VantagemConhecida(cleanupNomeTema(vantagemMatch.groupValues[1]))
        }

        val periciaMatch = Regex("^(?:a\\s+)?per[ií]cia\\s+(.+)$", RegexOption.IGNORE_CASE).find(tok)
        if (periciaMatch != null) {
            return PreRequisitoType.PericiaConhecida(cleanupNomeTema(periciaMatch.groupValues[1]))
        }

        return PreRequisitoType.MagiaConhecida(cleanupNomeTema(tok))
    }

    private fun splitAlternatives(segment: String): List<String> {
        if (segment.contains(Regex("(?i)n[aã]o\\s+pode\\s+ser"))) return listOf(segment)
        if (segment.contains(Regex("(?i)^quaisquer?\\s+\\d+"))) return listOf(segment)
        return segment.split(Regex("(?i)\\s+ou\\s+"))
    }

    private fun splitAndParts(raw: String): List<String> {
        if (raw.contains(Regex("(?i)^quaisquer?\\s+\\d+\\s+de\\s+"))) {
            return listOf(raw)
        }
        return raw
            .split(Regex("(?i)\\s+e\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun cleanupConnectorPrefix(value: String): String {
        return value
            .replace(Regex("(?i)^(e|ou|mais)\\s+"), "")
            .trim()
    }

    private fun cleanupNomeTema(value: String): String {
        return value
            .replace(Regex("(?i)^as?\\s+m[aá]gicas?\\s+de\\s+"), "")
            .replace(Regex("(?i)^m[aá]gicas?\\s+de\\s+"), "")
            .replace(Regex("(?i)^a\\s+"), "")
            .trim()
    }

    private fun containsBypassMarker(value: String): Boolean {
        if (value.contains('#')) return true
        return value.contains("especial", ignoreCase = true)
    }
}
