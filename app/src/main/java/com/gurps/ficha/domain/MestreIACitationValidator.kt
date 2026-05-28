package com.gurps.ficha.domain

import com.gurps.ficha.model.MestreIAChunk

/**
 * MestreIACitationValidator (Lote 315) — Detecta páginas alucinadas.
 *
 * Problema motivador: o modelo às vezes cita páginas específicas (ex: "[Módulo Básico, pág. 174]")
 * que NÃO vieram nos chunks retornados pelo RAG. Isso é alucinação confiante:
 * o modelo usa conhecimento de treinamento e apresenta como se fosse fonte oficial.
 *
 * Este validador roda APÓS a resposta do modelo. Extrai todas as citações de página
 * mencionadas no texto e compara contra as páginas dos chunks que o RAG efetivamente
 * retornou. Citações que não bateram são marcadas como "não verificadas" e um aviso
 * visual é injetado na resposta.
 *
 * NÃO impede alucinação (impossível com LLM atual) — apenas AVISA o usuário.
 */
object MestreIACitationValidator {

    /**
     * Citação de página extraída do texto da resposta.
     * @param livro Nome do livro mencionado (ex: "Módulo Básico", "Magia") ou null se citação genérica.
     * @param paginas Lista de números de página citados.
     * @param trecho Trecho original da citação (para log e exibição).
     */
    data class CitacaoExtraida(
        val livro: String?,
        val paginas: List<Int>,
        val trecho: String,
    )

    data class ResultadoValidacao(
        val verificadas: List<CitacaoExtraida>,
        val naoVerificadas: List<CitacaoExtraida>,
    ) {
        val temAlucinacao: Boolean get() = naoVerificadas.isNotEmpty()
    }

    /**
     * Mapeamento de nomes amigáveis de livro → source_id usado no banco de chunks.
     * Espelha o mapeamento de MestreIAGraphEngine.buscarDiretoNoCodex.
     */
    private val livroParaSourceId = mapOf(
        "modulo basico" to "pt_modulo_basico",
        "módulo básico" to "pt_modulo_basico",
        "basico"        to "pt_modulo_basico",
        "básico"        to "pt_modulo_basico",
        "artes marciais" to "pt_artes_marciais",
        "magia"          to "pt_magia",
        "gun fu"         to "pt_gun_fu",
        "pyramid aquatico" to "pt_pyramid_26_underwater",
        "pyramid aquático" to "pt_pyramid_26_underwater",
        "pyramid"        to "pt_pyramid_26_underwater",
    )

    /**
     * Regex para citações do tipo:
     *   [Módulo Básico, pág. 174]
     *   [Magia, pág. 14]; [Magia, pág. 124]; [Magia, pág. 266]
     *   [Módulo Básico, págs. 235 e 238]
     *   [Módulo Básico, págs. 353, 367, 383, 411, 548]
     */
    private val regexCitacaoColchete = Regex(
        """\[([^,\]]+?),\s*p[áa]gs?\.?\s*([0-9eE,\s]+)\]""",
        RegexOption.IGNORE_CASE,
    )

    /**
     * Regex para citações soltas (sem livro entre colchetes), tipo:
     *   "pág. 174"
     *   "pág 239"
     *   "página 408"
     * Usado como fallback — sem o livro, comparamos só contra o conjunto total de páginas.
     */
    private val regexPaginaSolta = Regex(
        """p[áa]gs?\.?\s*(\d{1,3})\b""",
        RegexOption.IGNORE_CASE,
    )

    /**
     * Extrai citações estruturadas do texto da resposta do modelo.
     */
    fun extrair(textoResposta: String): List<CitacaoExtraida> {
        val resultado = mutableListOf<CitacaoExtraida>()

        regexCitacaoColchete.findAll(textoResposta).forEach { match ->
            val livro = match.groupValues[1].trim()
            val paginasStr = match.groupValues[2]
            val paginas = extrairNumerosPagina(paginasStr)
            if (paginas.isNotEmpty()) {
                resultado.add(CitacaoExtraida(livro = livro, paginas = paginas, trecho = match.value))
            }
        }

        // Páginas soltas: só captura se ainda não foram cobertas pelas citações entre colchetes.
        val paginasJaCobertas = resultado.flatMap { it.paginas }.toSet()
        regexPaginaSolta.findAll(textoResposta).forEach { match ->
            val pagina = match.groupValues[1].toIntOrNull() ?: return@forEach
            if (pagina !in paginasJaCobertas && pagina in 1..600) {
                resultado.add(CitacaoExtraida(livro = null, paginas = listOf(pagina), trecho = match.value))
            }
        }

        return resultado
    }

    /**
     * Parseia "235 e 238", "353, 367, 383", "174" → List<Int>.
     */
    private fun extrairNumerosPagina(raw: String): List<Int> {
        return Regex("""\d+""")
            .findAll(raw)
            .mapNotNull { it.value.toIntOrNull() }
            .filter { it in 1..600 }
            .toList()
    }

    /**
     * Valida citações contra os chunks efetivamente retornados pelo RAG.
     *
     * Regra: uma citação é "verificada" se PELO MENOS UMA das suas páginas está presente
     * nos chunks do livro correspondente. Caso contrário, é "não verificada" (alucinação suspeita).
     *
     * Se a citação não especifica livro, basta a página estar em qualquer chunk.
     */
    fun validar(
        citacoes: List<CitacaoExtraida>,
        chunksRetornados: List<MestreIAChunk>,
    ): ResultadoValidacao {
        val verificadas = mutableListOf<CitacaoExtraida>()
        val naoVerificadas = mutableListOf<CitacaoExtraida>()

        // Index: livro normalizado → set de páginas presentes
        val paginasPorLivro: Map<String, Set<Int>> = chunksRetornados
            .filter { it.page_number != null }
            .groupBy { it.source_id ?: "desconhecido" }
            .mapValues { (_, chunks) -> chunks.mapNotNull { it.page_number }.toSet() }

        val todasPaginasNoContexto: Set<Int> = chunksRetornados
            .mapNotNull { it.page_number }
            .toSet()

        for (citacao in citacoes) {
            val sourceId = citacao.livro?.lowercase()?.trim()?.let { livroParaSourceId[it] }
            val paginasDisponiveis = if (sourceId != null) {
                paginasPorLivro[sourceId].orEmpty()
            } else {
                todasPaginasNoContexto
            }
            val temPaginaPresente = citacao.paginas.any { it in paginasDisponiveis }
            if (temPaginaPresente) {
                verificadas.add(citacao)
            } else {
                naoVerificadas.add(citacao)
            }
        }

        return ResultadoValidacao(verificadas = verificadas, naoVerificadas = naoVerificadas)
    }

    /**
     * Gera o texto de aviso a ser anexado à resposta quando há alucinações detectadas.
     * Retorna string vazia se não houver problemas.
     */
    fun formatarAviso(resultado: ResultadoValidacao): String {
        if (!resultado.temAlucinacao) return ""

        val trechos = resultado.naoVerificadas
            .map { it.trecho.trim().take(80) }
            .distinct()
            .take(8)

        return buildString {
            append("\n\n---\n")
            append("⚠️ **Citações não verificadas:** ")
            append(trechos.joinToString(", "))
            append("\n")
            append("_Estas páginas foram mencionadas na resposta mas não vieram nos resultados do Códex consultado. ")
            append("Pode ser conhecimento do modelo de IA (não verificado pelo manual oficial). ")
            append("Confirme antes de aplicar em mesa de jogo._")
        }
    }
}
