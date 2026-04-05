package com.gurps.ficha.domain.rules.traits

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
}
