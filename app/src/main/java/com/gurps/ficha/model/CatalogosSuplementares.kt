package com.gurps.ficha.model

data class PericiaSuplementarItem(
    val id: String = "",
    val nome: String = "",
    val pagina: Int? = null,
    val paginaRaw: String = "",
    val dificuldadeRaw: String = "",
    val preDefinidoRaw: String = "",
    val preRequisitoRaw: String = "",
    val descricao: String = "",
    val modificadores: String = "",
    val sourceBook: String = "",
    val sourceFile: String = ""
)

data class TecnicaCatalogoItem(
    val id: String = "",
    val nome: String = "",
    val pagina: Int? = null,
    val paginaRaw: String = "",
    val dificuldadeRaw: String = "",
    val preDefinidoRaw: String = "",
    val preRequisitoRaw: String = "",
    val descricao: String = "",
    val modificadores: String = "",
    val sourceBook: String = "",
    val sourceFile: String = ""
)
