package com.gurps.ficha.model

data class EscudoCatalogoItem(
    val id: String,
    val nome: String,
    val nt: Int?,
    val db: Int,
    val custo: Float?,
    val pesoKg: Float?,
    val stMinimo: Int?,
    val observacoes: String
)
