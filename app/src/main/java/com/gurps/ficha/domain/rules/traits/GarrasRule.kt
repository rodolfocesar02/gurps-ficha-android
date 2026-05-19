package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import com.gurps.ficha.ui.features.rolagem.*
import kotlin.math.ceil

/**
 * Regra para Garras (Claws).
 * Suporta Cascos, Afiadas, Cegas, Pontudas e Longas Pontudas.
 */
class GarrasRule : TraitRule {
    override val traitId: String = "garras"

    override fun calculateCost(
        selection: VantagemSelecionada,
        modifiers: List<ModificadorSelecao>
    ): Int {
        val tipo = selection.metadados?.get("tipoGarras") ?: "afiadas"
        
        val custoBase = when (tipo.lowercase()) {
            "cascos" -> 3
            "cegas" -> 3
            "afiadas" -> 5
            "pontudas" -> 8
            "longas_pontudas" -> 11
            else -> 5
        }

        val somaPercentual = modifiers.sumOf {
            it.bonusBase + if (it.porNivel) it.valor * it.niveis else it.valor
        }
        val percentualFinal = somaPercentual.coerceAtLeast(-80)
        val multiplicadorMod = 1.0 + (percentualFinal / 100.0)
        
        return ceil(custoBase * multiplicadorMod).toInt().coerceAtLeast(1)
    }

    override fun getDamageOptions(
        personagem: Personagem,
        selection: VantagemSelecionada
    ): List<DamageSourceOption> {
        val tipo = selection.metadados?.get("tipoGarras") ?: "afiadas"
        val gdp = personagem.danoGdP
        val parsed = parseDamageExpression(gdp)
        
        // Helper para bônus de +1 por dado (Usado em Cascos, Cegas e Longas)
        fun applyDiceBonus(base: String): String {
            if (parsed == null) return base
            val diceCount = parsed.diceCount
            val newModifier = parsed.modifier + diceCount
            return "${diceCount}d${if (newModifier >= 0) "+$newModifier" else newModifier}"
        }

        return when (tipo.lowercase()) {
            "cascos" -> listOf(
                DamageSourceOption(
                    id = "garras_cascos",
                    label = "Cascos",
                    contextLabel = "Dano Cascos",
                    damageExpression = "${applyDiceBonus(gdp)} cont"
                )
            )
            "cegas" -> listOf(
                DamageSourceOption(
                    id = "garras_cegas",
                    label = "Garras Cegas",
                    contextLabel = "Dano Garras Cegas",
                    damageExpression = "${applyDiceBonus(gdp)} cont"
                )
            )
            "afiadas" -> listOf(
                DamageSourceOption(
                    id = "garras_afiadas",
                    label = "Garras Afiadas",
                    contextLabel = "Dano Garras Afiadas",
                    damageExpression = "$gdp cort"
                )
            )
            "pontudas" -> listOf(
                DamageSourceOption(
                    id = "garras_pontudas_cort",
                    label = "Garras Pontudas (Corte)",
                    contextLabel = "Dano Garras Pontudas (Corte)",
                    damageExpression = "$gdp cort"
                ),
                DamageSourceOption(
                    id = "garras_pontudas_perf",
                    label = "Garras Pontudas (Perfuração)",
                    contextLabel = "Dano Garras Pontudas (Perfuração)",
                    damageExpression = "$gdp perf"
                )
            )
            "longas_pontudas" -> listOf(
                DamageSourceOption(
                    id = "garras_longas_cort",
                    label = "Longas Garras Pontudas (Corte)",
                    contextLabel = "Dano Longas Garras Pontudas (Corte)",
                    damageExpression = "${applyDiceBonus(gdp)} cort"
                ),
                DamageSourceOption(
                    id = "garras_longas_perf",
                    label = "Longas Garras Pontudas (Perfuração)",
                    contextLabel = "Dano Longas Garras Pontudas (Perfuração)",
                    damageExpression = "${applyDiceBonus(gdp)} perf"
                )
            )
            else -> emptyList()
        }
    }
}
