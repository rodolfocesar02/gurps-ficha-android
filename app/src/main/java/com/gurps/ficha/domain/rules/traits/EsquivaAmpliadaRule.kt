package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada

/**
 * Regra para Esquiva Ampliada (Enhanced Dodge).
 * GURPS pág. 51.
 * 15 pts: +1 no valor da Esquiva básica.
 */
class EsquivaAmpliadaRule : TraitRule {
    override val traitId: String = "defesas_ampliadas_esquiva_ampliada"

    override fun getDodgeModifier(
        personagem: Personagem,
        selection: VantagemSelecionada
    ): Int {
        // Segundo o manual GURPS pág. 51, é bônus fixo de +1.
        // Se houver níveis futuros, podemos multiplicar pelo nível.
        return 1
    }
}
