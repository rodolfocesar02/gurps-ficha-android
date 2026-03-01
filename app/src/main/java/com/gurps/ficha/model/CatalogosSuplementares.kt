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

data class PericiaV2RuleMapItem(
    val id: String = "",
    val nome: String = "",
    val tipo: PericiaV2TipoRegra = PericiaV2TipoRegra(),
    val preRequisito: PericiaV2PreRequisitoRegra = PericiaV2PreRequisitoRegra(),
    val preDefinido: PericiaV2PreDefinidoRegra = PericiaV2PreDefinidoRegra(),
    val descricao: String = "",
    val modificadoresRaw: String = ""
)

data class PericiaV2TipoRegra(
    val attributeMode: String = "fixed",
    val attributeOptions: List<String> = emptyList(),
    val difficultyMode: String = "fixed",
    val difficulty: String? = null
)

data class PericiaV2PreRequisitoRegra(
    val raw: String = "",
    val allowWithoutPrerequisite: Boolean = true,
    // AND de grupos; cada grupo tem OR de condicoes.
    val andGroups: List<List<PericiaV2CondicaoPreRequisito>> = emptyList()
)

data class PericiaV2CondicaoPreRequisito(
    val type: String = "",
    val value: String = "",
    val minLevel: Int? = null
)

data class PericiaV2PreDefinidoRegra(
    val raw: String = "",
    val onZeroPoints: String = "",
    val parsed: List<PericiaV2PreDefinidoEntrada> = emptyList()
)

data class PericiaV2PreDefinidoEntrada(
    val type: String = "",
    val base: String = "",
    val modifier: Int = 0
)
