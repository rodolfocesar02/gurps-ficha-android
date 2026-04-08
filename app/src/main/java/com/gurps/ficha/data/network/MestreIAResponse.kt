package com.gurps.ficha.data.network

/**
 * Modelo de dados completo recebido do Mestre IA (Gemini/DeepSeek).
 * Fidelidade Total (Lote 53) - Suporta Técnicas, Magias e Equipamentos reais.
 */
data class MestreIAResponse(
    val nome: String = "",
    val atributos: MestreIAAtributos = MestreIAAtributos(),
    val vantagens: List<String> = emptyList(),
    val desvantagens: List<String> = emptyList(),
    val pericias: List<MestreIAPericiaIA> = emptyList(),
    val tecnicas: List<MestreIAPericiaIA> = emptyList(),
    val magias: List<String> = emptyList(),
    val qualidades: List<String> = emptyList(),
    val peculiaridades: List<String> = emptyList(),
    val equipamentos: List<MestreIAEquipamento> = emptyList(),
    val aparencia: String = "",
    val historico: String = ""
)

data class MestreIAEquipamento(
    val nome: String = "",
    val peso: Float = 0f,
    val custo: Float = 0f,
    val quantidade: Int = 1
)

data class MestreIAAtributos(
    val st: Int = 10,
    val dx: Int = 10,
    val iq: Int = 10,
    val ht: Int = 10
)

data class MestreIAPericiaIA(
    val nome: String = "",
    val nivel: Int = 0
)
