package com.gurps.ficha.model

data class ArmaduraComponenteCatalogo(
    val local: String,
    val rd: String,
    val custoBase: Float?,
    val pesoKg: Float?
)

data class ArmaduraCatalogoItem(
    val id: String,
    val nome: String,
    val nt: Int?,
    val local: String,
    val rd: String,
    val custoBase: Float?,
    val pesoBaseKg: Float?,
    val observacoes: String,
    val componentes: List<ArmaduraComponenteCatalogo> = emptyList()
)
