package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.VantagemSelecionada
import kotlin.math.ceil

/**
 * Regra para Telecomunicação (GURPS p.93).
 *
 * É UMA vantagem com 3 opções nomeadas (Garras/Resistente seguem o
 * mesmo padrão). O JSON do livro lista o custo de cada variante, mas
 * o catálogo de vantagens só guarda o `specialRule:"telecomunicacao"`
 * sem implementação — essa regra preenche isso.
 *
 * O tipo vem em `metadados["tipoTelecomunicacao"]`:
 *   - "laser"     -> 15 (Comunicação a Laser)
 *   - "diapsiquia"-> 30 (Diapsiquia / Transmissão Telepática)
 *   - "radio"     -> 10 (Rádio)
 *
 * Modificadores percentuais (Transmissão Aberta +50%, Racial −20%,
 * Somente Envio −50%, Vídeo +40%, ...) aplicam sobre o custo da
 * variante escolhida, como em qualquer vantagem com mods.
 *
 * Ex.: Homens-Inseto / Guerreiros Insetos têm "Diapsiquia
 * (Transmissão Aberta, +50%; Racial, -20%) [39]" → 30 × (1 + 0,5 − 0,2)
 * = 30 × 1,3 = 39 ✓ (bate com o livro).
 */
class TelecomunicacaoRule : TraitRule {
    override val traitId: String = "telecomunicacao"

    override fun calculateCost(
        selection: VantagemSelecionada,
        modifiers: List<ModificadorSelecao>
    ): Int {
        val tipo = selection.metadados?.get("tipoTelecomunicacao") ?: "radio"

        val custoBase = when (tipo.lowercase()) {
            "laser", "comunicacao_a_laser" -> 15
            "diapsiquia", "transmissao_telepatica" -> 30
            "radio" -> 10
            else -> 10
        }

        val somaPercentual = modifiers.sumOf {
            it.bonusBase + if (it.porNivel) it.valor * it.niveis else it.valor
        }
        val percentualFinal = somaPercentual.coerceAtLeast(-80)
        val multiplicadorMod = 1.0 + (percentualFinal / 100.0)

        return ceil(custoBase * multiplicadorMod).toInt().coerceAtLeast(1)
    }
}
