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
        val preExpandido = expandirCadaUmDosElementos(raw)
        val normalized = preExpandido.trim().removePrefix("—").trim()
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
            .split(Regex("[,;]"))
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

        // Removido o bloco de inclusão automática de AptidaoMagica(0)
        // para evitar poluir perícias que não utilizam o sistema de magia.

        return ParseResult(tipos = allTipos, terms = terms)
    }

    /**
     * Expande "N mágica(s) de cada um dos <quantos> escola(s) (ar,terra,fogo,agua)"
     * em N exigências SEPARADAS ANDadas: "N magicas de ar; N magicas de fogo; ...".
     * Sem o expand, esse texto caía no fallback de Vantagem e a magia liberava com
     * ficha vazia (ex: Detectar Pontos Fracos). Cada item da lista vira MagiasEscola,
     * e termos separados por ';' são todos obrigatórios (AND) no motor.
     */
    internal fun expandirCadaUmDosElementos(raw: String): String {
        val regex = Regex(
            "(\\d+)\\s+m[aá]g(?:ica|ia)s?\\s+de\\s+cada\\s+um\\s+d[oa]s\\s+\\S+\\s+escolas?\\s*\\(([^)]+)\\)",
            RegexOption.IGNORE_CASE
        )
        return regex.replace(raw) { m ->
            val qtd = m.groupValues[1]
            val elementos = m.groupValues[2]
                .split(Regex("[,;/]|\\s+e\\s+"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
            if (elementos.isEmpty()) m.value
            else elementos.joinToString("; ") { "$qtd magicas de $it" }
        }
    }

    private fun parseSingle(token: String, lastSchool: String?): PreRequisitoType? {
        val tok = cleanupConnectorPrefix(token)
        if (tok.isBlank() || tok == "-") return null

        val naoPodeSer = Regex("(?i)^n[aã]o\\s+pode\\s+(?:ser|ter)\\s+(.+)$").find(tok)
        if (naoPodeSer != null) {
            val condicoes = naoPodeSer.groupValues[1]
                .split(Regex("(?i)\\s+ou\\s+"))
                .map { cleanupNomeTema(it) }
                .filter { it.isNotBlank() }
                .toSet()
            return PreRequisitoType.NaoPodeSer(condicoes)
        }

        val somaAtributos = Regex(
            "^\\(?\\s*([A-Za-zÀ-ú]+)\\s*\\+\\s*([A-Za-zÀ-ú]+)\\s*\\)?\\s*:?\\s*(\\d+)\\+?$",
            RegexOption.IGNORE_CASE
        ).find(tok)
        if (somaAtributos != null) {
            val atr1 = somaAtributos.groupValues[1].trim()
            val atr2 = somaAtributos.groupValues[2].trim()
            val minimo = somaAtributos.groupValues[3].toIntOrNull() ?: return null
            return PreRequisitoType.AtributosSomaMin(
                atributos = listOf(atr1, atr2),
                minimo = minimo
            )
        }

        val attrMatch = Regex("^([A-Za-zÀ-ú]+)\\s*(\\d+)\\+").find(tok)
        if (attrMatch != null) {
            val atributo = attrMatch.groupValues[1]
            if (isAttribute(atributo)) {
                val valor = attrMatch.groupValues[2].toIntOrNull() ?: return null
                return PreRequisitoType.AttributeMin(atributo, valor)
            }
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

        val emEscolasDiferentes = Regex(
            "^(\\d+)\\s+m[aá]g(?:ica|ia)s?\\s+em\\s+([a-zà-ú0-9]+)\\s+(outras\\s+)?escolas(?:\\s+diferentes)?$",
            RegexOption.IGNORE_CASE
        ).find(tok)
        if (emEscolasDiferentes != null) {
            val magiasPorEscola = emEscolasDiferentes.groupValues[1].toIntOrNull() ?: return null
            val escolasRaw = emEscolasDiferentes.groupValues[2]
            val escolasDiferentes = numeroFlex(escolasRaw) ?: return null
            val outras = emEscolasDiferentes.groupValues[3].isNotBlank()
            return PreRequisitoType.MagiasEmEscolasDiferentes(
                magiasPorEscola = magiasPorEscola,
                escolasDiferentes = escolasDiferentes,
                outrasEscolas = outras
            )
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

        val cleanTok = tok.trim()
        if (cleanTok.isEmpty()) return PreRequisitoType.MagiaConhecida("")

        // Lógica Brutal: Se termina com número+, é nível de perícia
        if (cleanTok.endsWith("+")) {
            val semPlus = cleanTok.removeSuffix("+").trim()
            val partes = semPlus.split(Regex("\\s+"))
            if (partes.size >= 2) {
                val nivelStr = partes.last()
                val nivelInt = nivelStr.toIntOrNull()
                if (nivelInt != null) {
                    val nomeRemanescente = partes.dropLast(1).joinToString(" ").replace(Regex("(?i)^pericia de "), "").trim()
                    return PreRequisitoType.SkillMinLevel(cleanupNomeTema(nomeRemanescente), nivelInt)
                }
            }
        }

        // Fallback: Tenta como Vantagem ou Magia (nomes simples)
        val nomeSimples = cleanupNomeTema(cleanTok).trim()
        return PreRequisitoType.VantagemConhecida(nomeSimples)
    }

    private fun isAttribute(name: String): Boolean {
        val n = name.uppercase()
        return n == "ST" || n == "DX" || n == "IQ" || n == "HT" || n == "PER" || n == "VON" || n == "VONTADE" || n == "PERCEPCAO"
    }

    private fun splitAlternatives(segment: String): List<String> {
        if (segment.contains(Regex("(?i)n[aã]o\\s+pode\\s+ser"))) return listOf(segment)
        if (segment.contains(Regex("(?i)^quaisquer?\\s+\\d+"))) return listOf(segment)
        return segment.split(Regex("(?i)\\s+ou\\s+"))
    }

    private fun splitAndParts(raw: String): List<String> {
        val inlineIncl = Regex("(?i)^(.+?)\\s+incl\\.?\\s+(.+)$").find(raw)
        if (inlineIncl != null) {
            val before = inlineIncl.groupValues[1].trim()
            val after = "incl. ${inlineIncl.groupValues[2].trim()}"
            return listOf(before, after)
        }

        val escolaComE = Regex(
            "(?i)^\\d+\\s+m[aá]g(?:ica|ia)s\\s+(?:de|da|do|sobre)\\s+.+\\s+e\\s+.+$"
        ).matches(raw)
        if (escolaComE && !raw.contains(Regex("(?i)\\s+mais\\s+")) && !raw.contains(Regex("(?i)\\s+ou\\s+"))) {
            return listOf(raw)
        }

        if (raw.contains(Regex("(?i)^quaisquer?\\s+\\d+\\s+de\\s+"))) {
            return listOf(raw)
        }
        return raw
            .split(Regex("(?i)\\s+e\\s+|\\s+mais\\s+"))
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

    private fun numeroFlex(raw: String): Int? {
        raw.toIntOrNull()?.let { return it }
        return when (raw.lowercase().trim()) {
            "um", "uma" -> 1
            "dois", "duas" -> 2
            "tres", "três" -> 3
            "quatro" -> 4
            "cinco" -> 5
            "seis" -> 6
            "sete" -> 7
            "oito" -> 8
            "nove" -> 9
            "dez" -> 10
            "onze" -> 11
            "doze" -> 12
            "treze" -> 13
            "catorze", "quatorze" -> 14
            "quinze" -> 15
            else -> null
        }
    }
}
