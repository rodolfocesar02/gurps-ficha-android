package com.gurps.ficha.domain

data class ItemValidacao(
    val entrada: String,
    val idEncontrado: String?,
    val nomeEncontrado: String?,
    val status: StatusValidacao,
    val mensagem: String
)

enum class StatusValidacao {
    OK,       // ID encontrado direto no catálogo
    FUZZY,    // Nome encontrado por fuzzy match
    FALLBACK, // Não encontrado — virará Qualidade/Peculiaridade
    ERRO      // Formato inválido
}

data class RelatorioValidacao(
    val vantagens: List<ItemValidacao>,
    val desvantagens: List<ItemValidacao>,
    val pericias: List<ItemValidacao>,
    val magias: List<ItemValidacao>,
    val totalOk: Int,
    val totalFallback: Int,
    val alertaBudget: String?,
    val tecnicas: List<ItemValidacao> = emptyList()
) {
    val totalItens: Int get() = vantagens.size + desvantagens.size + pericias.size + magias.size + tecnicas.size
    val temProblemas: Boolean get() = totalFallback > 0 || alertaBudget != null
}
