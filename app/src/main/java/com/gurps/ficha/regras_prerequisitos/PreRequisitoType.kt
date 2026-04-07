package com.gurps.ficha.regras_prerequisitos

sealed class PreRequisitoType {
    abstract fun readableName(): String

    data class AttributeMin(val atributo: String, val minimo: Int) : PreRequisitoType() {
        override fun readableName() = "$atributo $minimo+"
    }
    data class AptidaoMagica(val nivel: Int) : PreRequisitoType() {
        override fun readableName() = "Aptidão Mágica $nivel+"
    }
    data class MagiaConhecida(val nomeMagia: String) : PreRequisitoType() {
        override fun readableName() = nomeMagia
    }
    data class VantagemConhecida(val nomeVantagem: String) : PreRequisitoType() {
        override fun readableName() = "Vantagem: $nomeVantagem"
    }
    data class PericiaConhecida(val nomePericia: String) : PreRequisitoType() {
        override fun readableName() = "Perícia: $nomePericia"
    }
    data class MagiasEscola(val quantidade: Int, val escola: String) : PreRequisitoType() {
        override fun readableName() = "$quantidade magias de $escola"
    }
    data class MagiaInclusaNaContagem(
        val nomeMagia: String,
        val escolaContexto: String?
    ) : PreRequisitoType() {
        override fun readableName() = "Incluindo $nomeMagia"
    }
    data class QualquerMagiaComNome(val trechoNome: String) : PreRequisitoType() {
        override fun readableName() = "Qualquer magia com '$trechoNome'"
    }
    data class QuantidadeOutrasMagias(
        val quantidade: Int,
        val contexto: String?
    ) : PreRequisitoType() {
        override fun readableName() = "$quantidade magias${contexto?.let { " de $it" } ?: ""}"
    }
    data class QuantidadeMagiasPorEscolas(
        val quantidade: Int,
        val escolas: Set<String>
    ) : PreRequisitoType() {
        override fun readableName() = "$quantidade magias em ${escolas.joinToString(" ou ")}"
    }
    data class QuantidadeMagiasPorTemas(
        val quantidade: Int,
        val temas: Set<String>
    ) : PreRequisitoType() {
        override fun readableName() = "$quantidade magias de tema ${temas.joinToString("/")}"
    }
    data class MagiasEmEscolasDiferentes(
        val magiasPorEscola: Int,
        val escolasDiferentes: Int,
        val outrasEscolas: Boolean = false
    ) : PreRequisitoType() {
        override fun readableName() = "$magiasPorEscola magias em $escolasDiferentes escolas diferentes"
    }
    data class AtributosSomaMin(
        val atributos: List<String>,
        val minimo: Int
    ) : PreRequisitoType() {
        override fun readableName() = "${atributos.joinToString("+")} $minimo+"
    }
    data class NaoPodeSer(val condicoes: Set<String>) : PreRequisitoType() {
        override fun readableName() = "Não pode ter: ${condicoes.joinToString(" ou ")}"
    }
    data class SkillMinLevel(val nomePericia: String, val nivelMin: Int) : PreRequisitoType() {
        override fun readableName() = "$nomePericia $nivelMin+"
    }
    data class NivelMin(val nivel: Int) : PreRequisitoType() {
        override fun readableName() = "Nível de Personagem $nivel+"
    }
}
