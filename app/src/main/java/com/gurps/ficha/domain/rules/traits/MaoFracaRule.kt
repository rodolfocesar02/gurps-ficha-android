package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.PERICIAS_COMBATE_CORPO_A_CORPO
import com.gurps.ficha.model.Personagem

/**
 * **Mão Fraca** (MB p.151) — −2 por nível nas tarefas que exigem mão firme.
 *
 * > O personagem sofre uma penalidade nas tarefas que exigem mão firme. Cada
 * > nível (até no máximo 3) implica numa penalidade de −2 nessas tarefas. Essa
 * > penalidade é geral — **não dividida por mão**. As tarefas afetadas incluem:
 * > **utilizar armas de combate corpo a corpo**, escalar, pegar objetos no ar e
 * > qualquer outra coisa que o Mestre acredite precisar de firmeza nas mãos
 * > (ex.: um teste de Acrobacia para segurar um trapézio).
 *
 * ## Por que é Kotlin e não `efeitos` no catálogo
 *
 * Pelo mesmo motivo da [CegueiraRule]: "armas de combate corpo a corpo" são ~50
 * perícias. Declarar linha por linha no JSON dá um paredão que envelhece mal —
 * fica errado no dia em que o catálogo ganhar uma arma nova.
 *
 * Aqui a lista sai do que o personagem **realmente tem na ficha**, cruzada com
 * [PERICIAS_COMBATE_CORPO_A_CORPO]. Um traço, uma regra, sempre em dia.
 *
 * ⚠️ **Só corpo a corpo.** O livro escreve "armas de combate corpo a corpo", e a
 * [CegueiraRule] usa a união (`PERICIAS_COMBATE`) porque lá o livro fala de
 * *todas*. Usar a união aqui penalizaria o arqueiro que o livro não penaliza.
 *
 * ## O escopo que o livro mesmo dispensa
 *
 * A mão fraca é de UMA mão, mas o livro corta a discussão: *"essa penalidade é
 * geral — não dividida por mão"*. Então não há escopo `mao_inabil` a resolver
 * aqui, e é por isso que esta regra não esbarra na limitação de escopo do
 * [EfeitoInterpretador].
 */
class MaoFracaRule : TraitRule {

    override val traitId: String = ID

    override fun getSkillModifiers(
        personagem: Personagem,
        selection: TracoSelecionado
    ): Map<String, Int> {
        val penalidade = PENALIDADE_POR_NIVEL * selection.nivel.coerceIn(1, MAX_NIVEIS)
        val deCombate = personagem.periciasTotais
            .filter { it.definicaoId.lowercase().removePrefix("racial_") in PERICIAS_COMBATE_CORPO_A_CORPO }
            .map { it.nome }
        // As duas que o livro cita fora do combate entram sempre — mesmo que a
        // ficha ainda não tenha a perícia, o NH predefinido também é afetado.
        return (deCombate + FORA_DO_COMBATE).associateWith { penalidade }
    }

    companion object {
        const val ID = "mao_fraca"

        /** MB p.151: "cada nível (até no máximo 3) implica numa penalidade de −2". */
        const val PENALIDADE_POR_NIVEL = -2
        const val MAX_NIVEIS = 3

        /**
         * Escalar e o trapézio, os dois exemplos nominais do livro.
         *
         * "Pegar objetos no ar" não vira entrada porque não é perícia nenhuma —
         * é situação de mesa, e fica com o Mestre.
         */
        val FORA_DO_COMBATE = listOf("Escalada", "Acrobacia")
    }
}
