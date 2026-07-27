package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.features.rolagem.*
import com.gurps.ficha.viewmodel.*
import kotlin.math.ceil

/**
 * Regra para Golpeadores (Strikers).
 * Pág. 62 GURPS 4ª Edição.
 */
class GolpeadoresRule : TraitRule {
    override val traitId: String = "golpeadores"

    override fun calculateCost(
        selection: TracoSelecionado,
        modifiers: List<ModificadorSelecao>
    ): Int {
        val metadados = selection.metadados ?: emptyMap()
        val tipoDano = metadados["tipoDano"] ?: "cont"
        val dice = metadados["dice"]?.toFloatOrNull() ?: 0f
        val bonus = metadados["bonus"]?.toIntOrNull() ?: 0

        val custoPorTipo = when(tipoDano.lowercase()) {
            "cont", "contusao", "contusão", "perfurante-", "pi-", "piercing-", "perfurante", "pa", "pi", "piercing" -> 5
            "perfurante+", "pa+", "pi+", "large_piercing", "large piercing" -> 6
            "corte", "cut" -> 7
            "perfuracao", "perfuração", "imp", "pa++", "huge_piercing", "huge piercing" -> 8
            else -> 5
        }

        val multiplicadorDados = if (dice == 0f && bonus == 0) {
            1.0f 
        } else if (dice == 0f && (bonus == 1 || bonus == -1)) {
            0.25f
        } else {
            dice + (bonus * 0.25f)
        }

        val valorBase = (multiplicadorDados * custoPorTipo).toInt().coerceAtLeast(1)

        val somaPercentual = modifiers.sumOf {
            it.bonusBase + if (it.porNivel) it.valor * it.niveis else it.valor
        }
        val percentualFinal = somaPercentual.coerceAtLeast(-80)
        val multiplicadorMod = 1.0 + (percentualFinal / 100.0)
        
        return ceil(valorBase * multiplicadorMod).toInt().coerceAtLeast(1)
    }

    override fun getAttackOptions(
        personagem: Personagem,
        selection: TracoSelecionado
    ): List<RollMappedOption> {
        val nomePers = selection.metadados?.get("nomePersonalizado") ?: selection.nome
        
        // Golpeadores usa DX ou Briga (pág 62)
        val periciaBriga = personagem.pericias.find { it.definicaoId == "briga" }
        val nivelBriga = periciaBriga?.calcularNivel(personagem) ?: 0
        val nivelDx = personagem.destreza
        
        // Também verifica se existe uma perícia específica (ex: Golpeadores (Cauda))
        val periciaEspecífica = personagem.pericias.find { 
            it.definicaoId == "golpeadores" && 
            (it.especializacao.contains(nomePers, ignoreCase = true) || nomePers.contains(it.especializacao, ignoreCase = true))
        }
        val nivelEspecifica = periciaEspecífica?.calcularNivel(personagem) ?: 0
        
        val nh = maxOf(nivelDx, nivelBriga, nivelEspecifica)

        return listOf(RollMappedOption(
            id = "vant_${traitId}_${nomePers}",
            label = nomePers,
            contextLabel = "Ataque $nomePers",
            target = nh
        ))
    }

    override fun getDefenseOptions(
        personagem: Personagem,
        selection: TracoSelecionado
    ): List<ActiveDefense> {
        val nomePers = selection.metadados?.get("nomePersonalizado") ?: selection.nome
        
        // Pág. 62: maior entre DX/2 + 3 ou Aparar da Briga
        val periciaBriga = personagem.pericias.find { it.definicaoId == "briga" }
        val nhBriga = periciaBriga?.calcularNivel(personagem) ?: 0
        val aparaBriga = if (nhBriga > 0) (nhBriga / 2) + 3 else 0
        val aparaDx = (personagem.destreza / 2) + 3
        val aparaBase = maxOf(aparaBriga, aparaDx)
        
        return listOf(ActiveDefense(
            type = DefenseType.APARA,
            name = "Apara ($nomePers)",
            baseValue = aparaBase,
            bonus = 0,
            finalValue = aparaBase
        ))
    }

    override fun getDamageOptions(
        personagem: Personagem,
        selection: TracoSelecionado
    ): List<DamageSourceOption> {
        val dice = selection.metadados?.get("dice") ?: "0"
        val bonus = selection.metadados?.get("bonus")?.toIntOrNull() ?: 0
        val tipo = selection.metadados?.get("tipoDano") ?: "cont"
        val nomePers = selection.metadados?.get("nomePersonalizado") ?: selection.nome
        
        val bonusStr = if (bonus > 0) "+$bonus" else if (bonus < 0) "$bonus" else ""
        
        // Se for Golpeador e não houver dados, o dano é "baseado em ST" (tokens GdP/GeB)
        val expr = if (dice == "0" && bonus == 0) {
            "GdP $tipo" 
        } else {
            "${dice}d${bonusStr} $tipo"
        }
        
        return listOf(DamageSourceOption(
            id = "vant_${traitId}_${nomePers}",
            label = nomePers,
            contextLabel = "Dano $nomePers",
            damageExpression = expr
        ))
    }
}
