package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import com.gurps.ficha.ui.features.rolagem.*
import com.gurps.ficha.viewmodel.*
import kotlin.math.ceil

/**
 * Regra para Dentes (Teeth).
 * Pág. 52 GURPS 4ª Edição.
 */
class DentesRule : TraitRule {
    override val traitId: String = "dentes"

    override fun calculateCost(
        selection: VantagemSelecionada,
        modifiers: List<ModificadorSelecao>
    ): Int {
        val tipoDentes = selection.metadados?.get("tipoDentes") ?: "rombo"
        
        val custoBase = when (tipoDentes.lowercase()) {
            "rombo" -> 0
            "bico_afiado", "dentes_afiados" -> 1
            "presas" -> 2
            else -> 0
        }

        // Modificadores em Dentes são raros mas possíveis
        val somaPercentual = modifiers.sumOf {
            it.bonusBase + if (it.porNivel) it.valor * it.niveis else it.valor
        }
        val percentualFinal = somaPercentual.coerceAtLeast(-80)
        val multiplicadorMod = 1.0 + (percentualFinal / 100.0)
        
        val custoFinal = ceil(custoBase * multiplicadorMod).toInt()
        
        // Se o custo base era positivo e não foi reduzido a zero, mínimo 1. 
        // Mas se o custo base era 0 (Rombo), continua 0.
        return if (custoFinal < 1 && custoBase > 0) 1 else custoFinal
    }

    override fun getAttackOptions(
        personagem: Personagem,
        selection: VantagemSelecionada
    ): List<RollMappedOption> {
        val tipoDentes = selection.metadados?.get("tipoDentes") ?: "rombo"
        val label = when (tipoDentes) {
            "rombo" -> "Mordida (Rombo)"
            "bico_afiado" -> "Mordida (Bico)"
            "dentes_afiados" -> "Mordida (Afiada)"
            "presas" -> "Mordida (Presas)"
            else -> "Mordida"
        }

        // Mordida usa DX ou Briga (pág. 384)
        val periciaBriga = personagem.pericias.find { it.definicaoId == "briga" }
        val nivelBriga = periciaBriga?.calcularNivel(personagem) ?: 0
        val nivelDx = personagem.destreza
        
        val nh = maxOf(nivelDx, nivelBriga)

        return listOf(RollMappedOption(
            id = "vant_${traitId}_mordida",
            label = label,
            contextLabel = "Ataque $label",
            target = nh
        ))
    }

    override fun getDamageOptions(
        personagem: Personagem,
        selection: VantagemSelecionada
    ): List<DamageSourceOption> {
        val tipoDentes = selection.metadados?.get("tipoDentes") ?: "rombo"
        
        val tipoDano = when (tipoDentes.lowercase()) {
            "rombo" -> "cont"
            "bico_afiado" -> "pa+"
            "dentes_afiados" -> "cort"
            "presas" -> "perf"
            else -> "cont"
        }

        val label = "Mordida"
        
        return listOf(DamageSourceOption(
            id = "vant_${traitId}_mordida",
            label = label,
            contextLabel = "Dano $label",
            damageExpression = "GdP-1 $tipoDano"
        ))
    }
}
