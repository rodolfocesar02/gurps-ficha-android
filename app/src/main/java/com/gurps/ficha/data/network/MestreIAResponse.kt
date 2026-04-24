package com.gurps.ficha.data.network

/**
 * Modelo de dados completo recebido do Mestre IA (Gemini/DeepSeek).
 * Fidelidade Total (Lote 53) - Suporta Técnicas, Magias e Equipamentos reais.
 */
data class MestreIAResponse(
    val nome: String = "",
    val atributos: MestreIAAtributos = MestreIAAtributos(),
    val vantagens: List<MestreIAItem> = emptyList(),
    val desvantagens: List<MestreIAItem> = emptyList(),
    val pericias: List<MestreIAPericiaIA> = emptyList(),
    val tecnicas: List<MestreIAPericiaIA> = emptyList(),
    val magias: List<MestreIAItem> = emptyList(),
    val qualidades: List<MestreIAItem> = emptyList(),
    val peculiaridades: List<MestreIAItem> = emptyList(),
    val equipamentos: List<MestreIAEquipamento> = emptyList(),
    val aparencia: String = "",
    val historico: String = ""
)

/**
 * Representa um item genérico (Vantagem, Magia, etc) que pode vir com custo e descrição.
 */
data class MestreIAItem(
    val nome: String = "",
    val custo: Int? = null,
    val descricao: String? = null
)

data class MestreIAEquipamento(
    val nome: String = "",
    val peso: Float = 0f,
    val custo: Float = 0f,
    val quantidade: Int = 1,
    val rd: Int? = null,
    val dano: String? = null,
    val st_min: Int? = null,
    val aparar: String? = null
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
