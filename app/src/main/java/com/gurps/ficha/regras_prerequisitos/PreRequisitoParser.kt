package com.gurps.ficha.regras_prerequisitos

/**
 * Responsável por ler a string bruta do campo `preRequisitos` de uma magia e
 * transformá-la em uma estrutura compreensível pelo sistema, ou seja, uma lista
 * de [PreRequisitoType].
 *
 * Esta versão evolui o parser para cobrir padrões reais extraídos de
 * `magias2versao.json` e similares.
 *
 * Exemplos reconhecidos:
 *  - nenhum ou `—` -> vazio
 *  - atributos: `IQ 12+`, `DX 10+`
 *  - AMx (pode haver `AM2` etc.)
 *  - "2 mágicas de Fogo", "6 mágicas de Ácido" (quantidade + escola)
 *  - nome de outras magias simples (sem número ou unidade)
 *  - combinações com `e`, `ou`, vírgulas
 *
 * Futuro uso esperado (sem integração ainda):
 * ```kotlin
 * val raw = "IQ 12+, 2 mágicas de Água ou AM2"
 * val result = PreRequisitoParser.parse(raw)
 * // result.tipos terá lista de objetos já estruturados
 * ```
 *
 * Observação: ainda não há ligação com o motor de magias; por enquanto é
 * apenas uma biblioteca utilitária isolada.
 */
object PreRequisitoParser {

    data class ParseResult(val tipos: List<PreRequisitoType>, val warnings: List<String> = emptyList())

    fun parse(raw: String): ParseResult {
        val normalized = raw.trim().removePrefix("—").trim()
        if (normalized.isEmpty()) return ParseResult(emptyList())

        // dividir tokens por vírgulas ou " e " / " ou "
        val tokens = normalized
            .replace(" e ", ",")
            .replace(" ou ", ",")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val tipos = mutableListOf<PreRequisitoType>()
        val warnings = mutableListOf<String>()

        for (tok in tokens) {
            when {
                // nenhum requisito (marcado por vazio ou traço)
                tok.isBlank() -> { /* nada a fazer */ }
                // atributo mínimo (IQ 12+ etc.)
                Regex("^([A-Za-zÀ-ú]+)\s*(\d+)\\+").matches(tok) -> {
                    val (atributo, valor) = Regex("^([A-Za-zÀ-ú]+)\s*(\d+)\\+")
                        .find(tok)!!.destructured
                    tipos.add(PreRequisitoType.AttributeMin(atributo, valor.toInt()))
                }
                // aptidão mágica escrita como AM, com várias formas: AM2, AM 2, AM+2, aM 2
                Regex("^AM\s*\+?\s*(\\d+)", RegexOption.IGNORE_CASE).find(tok) != null
                        || Regex("^(Apt[ií]d[aã]o M[aá]gica)\s*(?:n[ií]vel)?\s*(\\d+)", RegexOption.IGNORE_CASE).find(tok) != null -> {
                    // tentamos primeiro padrão "AM"
                    val amMatch = Regex("^AM\s*\+?\s*(\\d+)", RegexOption.IGNORE_CASE).find(tok)
                    val nivel = if (amMatch != null) {
                        amMatch.groupValues[1].toInt()
                    } else {
                        // então padrão extenso "Aptidão Mágica X"
                        val ext = Regex("^(Apt[ií]d[aã]o M[aá]gica)\s*(?:n[ií]vel)?\s*(\\d+)", RegexOption.IGNORE_CASE)
                            .find(tok)!!
                        ext.groupValues[2].toInt()
                    }
                    tipos.add(PreRequisitoType.AptidaoMagica(nivel))
                }
                // X mágicas de Escola (2 mágicas de Fogo, etc.)
                Regex("^(\\d+) mági[cq]as de ([A-Za-zÀ-ú ]+)", RegexOption.IGNORE_CASE).find(tok) != null -> {
                    val (qtd, escola) = Regex("^(\\d+) mági[cq]as de ([A-Za-zÀ-ú ]+)", RegexOption.IGNORE_CASE)
                        .find(tok)!!.destructured
                    tipos.add(PreRequisitoType.MagiasEscola(qtd.toInt(), escola.trim()))
                }
                // outro requisito simples: outra magia ou nome livre
                else -> {
                    tipos.add(PreRequisitoType.MagiaConhecida(tok))
                }
            }
        }

        return ParseResult(tipos, warnings)
    }
}
