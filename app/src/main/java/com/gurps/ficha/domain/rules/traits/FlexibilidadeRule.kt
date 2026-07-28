package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.Personagem

/**
 * Regra para a vantagem Flexibilidade.
 * Bônus: +3 em Escalada e Fuga.
 */
class FlexibilidadeRule : TraitRule {
    override val traitId: String = "flexibilidade"

    override fun getSkillModifiers(
        personagem: Personagem,
        selection: TracoSelecionado
    ): Map<String, Int> {
        // MB p.61 lista TRES: Escalada, Fuga e Arte Erotica. A terceira estava
        // faltando desde que a regra foi escrita -- achado ao conferir o plano
        // contra o livro em 28/07. Como aqui e regra Kotlin, a correcao tem que
        // ser no .kt: declarar no JSON seria ignorado em silencio.
        return mapOf(
            "Escalada" to 3,
            "Fuga" to 3,
            "Arte Erótica" to 3
        )
    }
}
