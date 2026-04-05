package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.Personagem

/**
 * Registro central de regras de vantagens.
 * Permite localizar rapidamente qual regra se aplica a cada ID.
 */
object TraitRuleRegistry {
    private val rules = mutableMapOf<String, TraitRule>()

    init {
        // Registro de REGRAS COMPLEXAS (Alfabeto e migrações atuais)
        register(AtaqueInatoRule())
        register(GolpeadoresRule())
        register(DentesRule())
        register(FlexibilidadeRule())
        register(GarrasRule())
        register(ApararAmpliadoRule())
        register(BloqueioAmpliadoRule())
        register(EsquivaAmpliadaRule())
        register(MestreDeArmasRule())
    }

    private fun register(rule: TraitRule) {
        rules[rule.traitId] = rule
    }

    /**
     * Retorna a regra para uma determinada vantagem ou null se for custo padrão.
     */
    fun getRuleFor(traitId: String): TraitRule? {
        return rules[traitId]
    }

    /**
     * Retorna se a vantagem tem regras complexas/especiais.
     */
    fun hasSpecialRule(traitId: String): Boolean = rules.containsKey(traitId)

    /**
     * Retorna a soma de bônus em perícia vindo de todas as vantagens do personagem.
     */
    fun getSkillBonus(personagem: Personagem, skillName: String): Int {
        var total = 0
        personagem.vantagens.forEach { selection ->
            val rule = rules[selection.definicaoId]
            if (rule != null) {
                val bonuses = rule.getSkillModifiers(personagem, selection)
                val bonus = bonuses[skillName] ?: 0
                total += bonus
            }
        }
        return total
    }

    /**
     * Retorna a soma de bônus em Aparar (Parry) vindo de todas as vantagens do personagem.
     */
    fun getParryBonus(personagem: Personagem, periciaId: String?): Int {
        var total = 0
        personagem.vantagens.forEach { selection ->
            val rule = rules[selection.definicaoId]
            if (rule != null) {
                total += rule.getParryModifier(personagem, selection, periciaId)
            }
        }
        return total
    }

    /**
     * Retorna a soma de bônus em Esquiva (Dodge) vindo de todas as vantagens do personagem.
     */
    fun getDodgeBonus(personagem: Personagem): Int {
        var total = 0
        personagem.vantagens.forEach { selection ->
            val rule = rules[selection.definicaoId]
            if (rule != null) {
                total += rule.getDodgeModifier(personagem, selection)
            }
        }
        return total
    }

    /**
     * Retorna a soma de bônus em Bloqueio (Block) vindo de todas as vantagens do personagem.
     */
    fun getBlockBonus(personagem: Personagem): Int {
        var total = 0
        personagem.vantagens.forEach { selection ->
            val rule = rules[selection.definicaoId]
            if (rule != null) {
                total += rule.getBlockModifier(personagem, selection)
            }
        }
        return total
    }

    /**
     * Retorna a soma de bônus de dano por dado (ex: Mestre de Armas) acumulado.
     */
    fun getDamageBonusPerDie(personagem: Personagem, periciaId: String?): Int {
        var total = 0
        personagem.vantagens.forEach { selection ->
            val rule = rules[selection.definicaoId]
            if (rule != null) {
                total += rule.getDamageBonusPerDie(personagem, selection, periciaId)
            }
        }
        return total
    }
}
