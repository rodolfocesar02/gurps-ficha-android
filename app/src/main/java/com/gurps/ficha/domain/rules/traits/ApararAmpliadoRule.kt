package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.Personagem

/**
 * Regra para Aparar Ampliado (Enhanced Parry).
 * GURPS pág. 51.
 * 5 pts: Uma perícia específica ou "Desarmado".
 * 10 pts: Todas as manobras Aparar.
 */
class ApararAmpliadoRule : TraitRule {
    override val traitId: String = "defesas_ampliadas_aparar_ampliado"

    private val UNARMED_SKILLS = setOf(
        "briga", "carate", "boxe", "judo", "luta_livre", "sumo", "kung_fu", "luta_greco_romana"
    )

    override fun getParryModifier(
        personagem: Personagem,
        selection: TracoSelecionado,
        periciaId: String?
    ): Int {
        val type = selection.metadados?.get("tipo") ?: "global"
        
        if (type == "global") {
            return 1
        }

        val targetSkillId = selection.metadados?.get("skillId")?.lowercase()
        
        if (targetSkillId == "desarmado") {
            // Aplica a qualquer perícia da lista de desarmados
            return if (periciaId != null && periciaId.lowercase() in UNARMED_SKILLS) 1 else 0
        }

        // Aplica apenas à perícia específica
        return if (periciaId != null && periciaId.lowercase() == targetSkillId) 1 else 0
    }
}
