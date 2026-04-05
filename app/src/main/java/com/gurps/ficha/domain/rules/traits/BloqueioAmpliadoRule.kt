package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada

/**
 * Regra para Bloqueio Ampliado (Enhanced Block).
 * GURPS pág. 51.
 * 5 pts: +1 no Bloqueio para uma perícia específica (ex: Escudo ou Capa).
 */
class BloqueioAmpliadoRule : TraitRule {
    override val traitId: String = "defesas_ampliadas_bloqueio_ampliado"

    override fun getBlockModifier(
        personagem: Personagem,
        selection: VantagemSelecionada
    ): Int {
        // Por padrão, se o personagem tem a vantagem, ele ganha +1.
        // Se no futuro houver níveis (ex: +2, +3), podemos ler do 'nivel' ou metadados.
        return 1
    }
}
