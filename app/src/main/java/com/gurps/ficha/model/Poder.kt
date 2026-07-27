package com.gurps.ficha.model

import androidx.compose.runtime.Stable
import com.google.gson.annotations.SerializedName

@Stable
data class Poder(
    val id: String = java.util.UUID.randomUUID().toString(),
    var nome: String = "",
    var fonte: String = "",
    var foco: String = "",
    var modificadorDePoder: Int = 0, // Ex: -10 para -10%
    var nivelTalento: Int = 0,
    var custoTalentoNivel: Int = 5
) { 
    val custoTotalTalento: Int get() = nivelTalento * custoTalentoNivel
}

@Stable
data class PoderDefinicao(
    val id: String,
    val nome: String,
    @SerializedName("fontes_possiveis") val fontesPossiveis: String,
    val foco: String,
    val descricao: String,
    val modificadorDePoder: Int,
    val pagina: Int
) { 
    fun normalizada(): PoderDefinicao = copy(nome = nome.trim()) 
}
