package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import com.gurps.ficha.ui.features.rolagem.RollMappedOption
import com.gurps.ficha.ui.features.rolagem.DamageSourceOption
import kotlin.math.ceil

/**
 * Regra para Ataque Inato (Innate Attack).
 * Pág. 60 GURPS 4ª Edição.
 */
class AtaqueInatoRule : TraitRule {
    override val traitId: String = "ataque_inato"

    override fun calculateCost(
        selection: VantagemSelecionada,
        modifiers: List<ModificadorSelecao>
    ): Int {
        val metadados = selection.metadados ?: emptyMap()
        val tipoDano = metadados["tipoDano"] ?: "cont"
        val dice = metadados["dice"]?.toFloatOrNull() ?: 1.0f
        val bonus = metadados["bonus"]?.toIntOrNull() ?: 0

        val custoPorDado = when (tipoDano.lowercase()) {
            "cont", "contusao", "contusão", "queimadura", "queim", "qmd", "cr", "burn" -> 5
            "corrosao", "corrosão", "cor", "fadiga", "fad", "fat" -> 10
            "corte", "cut" -> 7
            "perfuracao", "perfuração", "imp", "pa++", "huge_piercing", "huge piercing" -> 8
            "perfurante-", "pi-", "piercing-", "perfurante_minus" -> 3
            "perfurante", "pa", "pi", "piercing" -> 5
            "perfurante+", "pa+", "pi+", "large_piercing", "large piercing" -> 6
            "toxina", "tox" -> 4
            else -> 5
        }

        val multiplicadorDados = if (dice == 0f && (bonus == 1 || bonus == -1)) {
            0.25f
        } else {
            dice + (bonus * 0.25f)
        }

        val valorBase = (multiplicadorDados * custoPorDado).toInt().coerceAtLeast(1)

        val somaPercentual = modifiers.sumOf {
            // bonusBase fixo + (valor*niveis se porNivel) — cobre Cone (+50%+10%/m)
            it.bonusBase + if (it.porNivel) it.valor * it.niveis else it.valor
        }
        val percentualFinal = somaPercentual.coerceAtLeast(-80)
        val multiplicadorMod = 1.0 + (percentualFinal / 100.0)

        return ceil(valorBase * multiplicadorMod).toInt().coerceAtLeast(1)
    }

    override fun getAttackOptions(
        personagem: Personagem,
        selection: VantagemSelecionada
    ): List<RollMappedOption> {
        val nomePers = selection.metadados?.get("nomePersonalizado") ?: selection.nome
        
        // Ataque Inato usa perícia de Ataque Inato ou DX-4
        val periciaInata = personagem.pericias.find { 
            it.definicaoId == "ataque_inato" && 
            (it.especializacao.contains(nomePers, ignoreCase = true) || nomePers.contains(it.especializacao, ignoreCase = true))
        } ?: personagem.pericias.find { it.definicaoId == "ataque_inato" }
        
        val nh = periciaInata?.calcularNivel(personagem) ?: (personagem.destreza - 4)

        return listOf(RollMappedOption(
            id = "vant_${traitId}_${nomePers}",
            label = nomePers,
            contextLabel = "Ataque $nomePers",
            target = nh
        ))
    }

    override fun getDamageOptions(
        personagem: Personagem,
        selection: VantagemSelecionada
    ): List<DamageSourceOption> {
        val dice = selection.metadados?.get("dice") ?: "1"
        val bonus = selection.metadados?.get("bonus")?.toIntOrNull() ?: 0
        val tipo = selection.metadados?.get("tipoDano") ?: "cont"
        val nomePers = selection.metadados?.get("nomePersonalizado") ?: selection.nome
        
        val bonusStr = if (bonus > 0) "+$bonus" else if (bonus < 0) "$bonus" else ""
        val expr = "${dice}d${bonusStr} $tipo"
        
        return listOf(DamageSourceOption(
            id = "vant_${traitId}_${nomePers}",
            label = nomePers,
            contextLabel = "Dano $nomePers",
            damageExpression = expr
        ))
    }
}
