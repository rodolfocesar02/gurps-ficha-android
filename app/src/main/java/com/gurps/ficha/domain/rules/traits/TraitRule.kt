package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.Personagem
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
        selection: TracoSelecionado,
        modifiers: List<ModificadorSelecao>
    ): Int? = null

    /**
     * Retorna opções de ataque (NH) que esta vantagem adiciona à Aba de Rolagem.
     */
    fun getAttackOptions(
        personagem: Personagem,
        selection: TracoSelecionado
    ): List<RollMappedOption> = emptyList()

    /**
     * Retorna opções de defesa (Apara/Bloqueio) que esta vantagem adiciona.
     */
    fun getDefenseOptions(
        personagem: Personagem,
        selection: TracoSelecionado
    ): List<ActiveDefense> = emptyList()

    /**
     * Retorna fontes de dano que esta vantagem adiciona.
     */
    fun getDamageOptions(
        personagem: Personagem,
        selection: TracoSelecionado
    ): List<DamageSourceOption> = emptyList()

    /**
     * Retorna o bônus de Esquiva (Dodge) concedido pela vantagem.
     */
    fun getDodgeModifier(
        personagem: Personagem,
        selection: TracoSelecionado
    ): Int = 0

    /**
     * Retorna o bônus de Bloqueio (Block) concedido pela vantagem.
     */
    fun getBlockModifier(
        personagem: Personagem,
        selection: TracoSelecionado
    ): Int = 0

    /**
     * Retorna o bônus de bônus de Aparar (Parry) concedido pela vantagem.
     */
    fun getParryModifier(
        personagem: Personagem,
        selection: TracoSelecionado,
        periciaId: String?
    ): Int = 0

    /**
     * Retorna bônus em perícias que esta vantagem concede.
     * Ex: mapOf("Escalada" to 3)
     */
    fun getSkillModifiers(
        personagem: Personagem,
        selection: TracoSelecionado
    ): Map<String, Int> = emptyMap()

    /**
     * Bônus que só valem em certas situações ("ao tentar parecer honesto").
     *
     * Ficam FORA do NH base de propósito: somá-los sempre inflaria a ficha e
     * mentiria sobre o personagem. Viram opção marcável na hora de rolar.
     */
    fun getBonusCondicionais(
        personagem: Personagem,
        selection: TracoSelecionado
    ): List<BonusCondicional> = emptyList()

    /**
     * Bônus em atributo ou característica secundária (GANCHO-A).
     *
     * ⚠️ NUNCA leia aqui o atributo que este método modifica: `Personagem.pontosVida`
     * chama o agregador, que chama isto — ler `personagem.pontosVida` de dentro
     * de uma regra de PV entra em laço infinito. Use os campos crus
     * (`personagem.forca`, `personagem.st`) quando precisar de contexto.
     */
    fun getAttributeModifiers(
        personagem: Personagem,
        selection: TracoSelecionado
    ): Map<Atributo, Int> = emptyMap()

    /**
     * Retorna o bônus de dano por dado (ex: Mestre de Armas).
     */
    fun getDamageBonusPerDie(
        personagem: Personagem,
        selection: TracoSelecionado,
        periciaId: String?,
        weaponName: String? = null,
        armaGrupo: String? = null
    ): Int = 0
}
