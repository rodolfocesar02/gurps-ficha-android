package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * A penalidade por usar a **mão inábil** (MB p.14), e quem a anula.
 *
 * Ideia do usuário: um seletor no botão de Ataque distinguindo mão hábil e
 * inábil. Marcando inábil, −4; com **Ambidestria**, o redutor não aparece, mas
 * o seletor continua funcionando.
 *
 * **Por que isto é UI e não efeito declarado.** A Ambidestria não *concede*
 * bônus — ela **remove** uma penalidade que o app nunca aplicou. Declarar
 * `{"tipo":"atributo","alvo":"DX","valor":-4}` nela daria −4 a quem comprou a
 * vantagem, que é o oposto do livro. A penalidade pertence à situação (qual mão
 * está sendo usada), e a vantagem apenas a zera.
 *
 * O mesmo raciocínio vale para qualquer vantagem que "isenta de penalidade":
 * primeiro a penalidade tem que existir na tela, depois a vantagem a apaga.
 *
 * Kotlin puro e testável.
 */
object MaoInabilRules {

    /** A penalidade do livro, quando nada a anula. */
    const val PENALIDADE = -4

    private const val ID_AMBIDESTRIA = "ambidestria"

    /** Se a ficha tem Ambidestria (MB p.41). */
    fun temAmbidestria(personagem: Personagem): Boolean =
        personagem.vantagens.any { it.definicaoId == ID_AMBIDESTRIA }

    /**
     * Quanto vale usar a mão inábil para ESTE personagem.
     *
     * Zero com Ambidestria — e zero também quando a mão usada é a hábil, claro.
     */
    fun penalidadeDe(personagem: Personagem, usandoMaoInabil: Boolean): Int = when {
        !usandoMaoInabil -> 0
        temAmbidestria(personagem) -> 0
        else -> PENALIDADE
    }

    /**
     * O que mostrar ao lado do seletor.
     *
     * Com Ambidestria a linha vira explicação em vez de número: some o "−4" mas
     * fica dito por que sumiu, senão o jogador acha que o app esqueceu.
     */
    fun rotuloDe(personagem: Personagem, usandoMaoInabil: Boolean): String = when {
        !usandoMaoInabil -> "Mão hábil"
        temAmbidestria(personagem) -> "Mão inábil — sem penalidade (Ambidestria)"
        else -> "Mão inábil ($PENALIDADE)"
    }

    /** O mesmo, escrito para o TalkBack. */
    fun rotuloAcessivel(personagem: Personagem, usandoMaoInabil: Boolean): String = when {
        !usandoMaoInabil -> "Usando a mão hábil. Sem penalidade."
        temAmbidestria(personagem) ->
            "Usando a mão inábil. Sem penalidade, porque o personagem tem Ambidestria."
        else -> "Usando a mão inábil. Penalidade de menos 4."
    }
}
