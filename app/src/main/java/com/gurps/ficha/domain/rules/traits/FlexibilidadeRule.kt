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
        return mapOf(
            "Escalada" to 3,
            "Fuga" to 3
        )
    }
}
