package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.PERICIAS_COMBATE
import com.gurps.ficha.model.Personagem

/**
 * **Cegueira** (MB p.127) — os −6 em todas as perícias de combate.
 *
 * > Todas as **perícias de combate** de um personagem cego sofrem uma penalidade
 * > de **−6**.
 *
 * ## Por que é regra Kotlin e não `efeitos` no catálogo
 *
 * O efeito declarado casa por **nome exato** de perícia, um alvo por linha. As
 * perícias de combate são ~70 — declarar uma linha para cada seria um paredão de
 * JSON que fica errado no dia em que o catálogo ganhar a perícia número 71.
 *
 * Aqui a lista é consultada **na hora**, a partir do que o personagem realmente
 * tem na ficha: `PERICIAS_COMBATE` diz quais ids são de combate, e a ficha diz o
 * nome de cada uma. Um traço, uma regra, sempre em dia.
 *
 * ⚠️ Vale para as de **corpo a corpo e as de distância**: o livro penaliza as
 * duas. Para o ataque à distância ele acrescenta que o cego só acerta "em direção
 * a um alvo próximo o bastante para ser ouvido", o que é narrativa e fica com o
 * Mestre.
 *
 * ## O que NÃO está aqui
 *
 * - O bloqueio dos testes de Visão já é do `SentidoRules`.
 * - A escuridão não penalizá-lo é do `IluminacaoRules` — o livro diz que ele
 *   *"não sofre nenhuma outra penalidade por atuar no escuro"*, e somar a luz por
 *   cima seria cobrar duas vezes pela mesma cegueira.
 */
class CegueiraRule : TraitRule {

    override val traitId: String = ID

    override fun getSkillModifiers(
        personagem: Personagem,
        selection: TracoSelecionado
    ): Map<String, Int> =
        personagem.periciasTotais
            .filter { it.definicaoId.lowercase().removePrefix("racial_") in PERICIAS_COMBATE }
            .associate { it.nome to PENALIDADE }

    companion object {
        const val ID = "cegueira"

        /** MB p.127. */
        const val PENALIDADE = -6
    }
}
