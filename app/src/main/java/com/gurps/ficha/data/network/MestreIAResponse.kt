package com.gurps.ficha.data.network

/**
 * Modelo de dados recebido do Mestre IA (Gemini).
 * Expandido para suportar fichas mais completas:
 * - Atributos, Vantagens, Desvantagens, Perícias
 * - Magias (para personagens magos)
 * - Qualidades e Peculiaridades (traços de personalidade)
 * - Aparência, Histórico e Notas
 */
data class MestreIAResponse(
    val nome: String = "",
    val atributos: MestreIAAtributos = MestreIAAtributos(),
    val vantagens: List<String> = emptyList(),
    val desvantagens: List<String> = emptyList(),
    val pericias: List<MestreIAPericia> = emptyList(),
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

data class MestreIAPericia(
    val nome: String = "",
    val nivel: Int = 0
)
