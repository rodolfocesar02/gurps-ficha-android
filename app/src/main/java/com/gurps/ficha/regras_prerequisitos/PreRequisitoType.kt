package com.gurps.ficha.regras_prerequisitos

sealed class PreRequisitoType {
    data class AttributeMin(val atributo: String, val minimo: Int) : PreRequisitoType()
    data class AptidaoMagica(val nivel: Int) : PreRequisitoType()
    data class MagiaConhecida(val nomeMagia: String) : PreRequisitoType()
    data class VantagemConhecida(val nomeVantagem: String) : PreRequisitoType()
    data class PericiaConhecida(val nomePericia: String) : PreRequisitoType()
    data class MagiasEscola(val quantidade: Int, val escola: String) : PreRequisitoType()
    data class MagiaInclusaNaContagem(
        val nomeMagia: String,
        val escolaContexto: String?
    ) : PreRequisitoType()
    data class QualquerMagiaComNome(val trechoNome: String) : PreRequisitoType()
    data class QuantidadeOutrasMagias(
        val quantidade: Int,
        val contexto: String?
    ) : PreRequisitoType()
    data class QuantidadeMagiasPorEscolas(
        val quantidade: Int,
        val escolas: Set<String>
    ) : PreRequisitoType()
    data class QuantidadeMagiasPorTemas(
        val quantidade: Int,
        val temas: Set<String>
    ) : PreRequisitoType()
    data class MagiasEmEscolasDiferentes(
        val magiasPorEscola: Int,
        val escolasDiferentes: Int,
        val outrasEscolas: Boolean = false
    ) : PreRequisitoType()
    data class AtributosSomaMin(
        val atributos: List<String>,
        val minimo: Int
    ) : PreRequisitoType()
    data class NaoPodeSer(val condicoes: Set<String>) : PreRequisitoType()
    data class NivelMin(val nivel: Int) : PreRequisitoType()
}
