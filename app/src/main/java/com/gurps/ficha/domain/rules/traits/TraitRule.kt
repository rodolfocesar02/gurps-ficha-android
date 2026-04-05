package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import com.gurps.ficha.ui.features.rolagem.RollMappedOption
import com.gurps.ficha.viewmodel.ActiveDefense
import com.gurps.ficha.ui.features.rolagem.DamageSourceOption

/**
 * Interface para regras específicas de vantagens (Striker, Innate Attack, etc).
 * Permite isolar lógica de cálculo de custo e impacto em combate.
 */
interface TraitRule {
    val traitId: String

    /**
     * Calcula o custo da vantagem. 
     * Retorna null se deve usar o cálculo padrão do CharacterRules.
     */
    fun calculateCost(
        selection: VantagemSelecionada,
        modifiers: List<ModificadorSelecao>
    ): Int? = null

    /**
     * Retorna opções de ataque (NH) que esta vantagem adiciona à Aba de Rolagem.
     */
    fun getAttackOptions(
        personagem: Personagem,
        selection: VantagemSelecionada
    ): List<RollMappedOption> = emptyList()

    /**
     * Retorna opções de defesa (Apara/Bloqueio) que esta vantagem adiciona.
     */
    fun getDefenseOptions(
        personagem: Personagem,
        selection: VantagemSelecionada
    ): List<ActiveDefense> = emptyList()

    /**
     * Retorna fontes de dano que esta vantagem adiciona.
     */
    fun getDamageOptions(
        personagem: Personagem,
        selection: VantagemSelecionada
    ): List<DamageSourceOption> = emptyList()
}
