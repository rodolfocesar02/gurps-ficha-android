package com.gurps.ficha.model

data class ArmaCatalogoItem(
    val id: String,
    val nome: String,
    val tipoCombate: String, // "corpo_a_corpo" | "distancia"
    val categoria: String,
    val grupo: String,
    val stMinimo: Int?,
    val danoRaw: String,
    val custoBase: Float?,
    val pesoBaseKg: Float?,
    val aparar: String? = null,
    val observacoes: String = ""
)
