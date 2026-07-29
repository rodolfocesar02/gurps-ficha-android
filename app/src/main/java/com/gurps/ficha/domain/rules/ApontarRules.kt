package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * **Apontar, Precisão e Visão Telescópica** (MB p.373 e p.99).
 *
 * As três coisas que melhoram um tiro, e que se somam no mesmo ataque:
 *
 * 1. **Precisão (Prec)** — o bônus da arma, que só vale se o personagem gastou um
 *    turno na manobra **Apontar**: *"Esse é o bônus que o personagem recebe se
 *    executar uma ou mais manobras Apontar imediatamente antes do ataque."*
 * 2. **Visão Telescópica** — cada nível ignora **-1** de penalidade de distância,
 *    ou **-2** se o personagem Apontou (MB p.99).
 * 3. O teto: *"A soma do bônus de Precisão com os demais bônus de pontaria nunca
 *    podem exceder o dobro do parâmetro Prec da arma."* (p.373)
 *
 * ## Por que a Visão Telescópica só ficou possível agora
 *
 * Ela é a primeira cliente da tabela de distância. Antes do Lote MIRA-2 o app não
 * aplicava penalidade por distância nenhuma — automatizar o **desconto** de uma
 * penalidade que ninguém estava aplicando não faria diferença nenhuma na tela.
 *
 * ## ⚠️ Ela desconta a DISTÂNCIA, não o ataque
 *
 * A diferença importa quando o alvo está perto: a Visão Telescópica 3 num tiro a
 * 3 metros (penalidade -1) cancela **1**, não 3. Sobra não vira bônus — tratá-la
 * como bônus fixo daria +3 num tiro à queima-roupa, o que o livro não concede.
 *
 * Kotlin puro e testável.
 */
object ApontarRules {

    const val ID_VISAO_TELESCOPICA = "visao_telescopica"

    /** Níveis de Visão Telescópica na ficha. */
    fun nivelTelescopica(personagem: Personagem): Int =
        personagem.vantagensTotais
            .filter { it.definicaoId == ID_VISAO_TELESCOPICA }
            .sumOf { it.nivel.coerceAtLeast(1) }

    /** Se a ficha tem Visão Telescópica. */
    fun temTelescopica(personagem: Personagem): Boolean = nivelTelescopica(personagem) > 0

    /**
     * Quanto da penalidade de distância a Visão Telescópica **cancela**.
     *
     * [penalidadeDistancia] entra negativa (é penalidade). O retorno é positivo:
     * é o quanto some. Nunca cancela mais do que existe — sobra não vira bônus.
     */
    fun cancelaDaDistancia(
        personagem: Personagem,
        penalidadeDistancia: Int,
        apontou: Boolean
    ): Int {
        val nivel = nivelTelescopica(personagem)
        if (nivel == 0 || penalidadeDistancia >= 0) return 0
        val porNivel = if (apontou) 2 else 1
        return (nivel * porNivel).coerceAtMost(-penalidadeDistancia)
    }

    /**
     * O bônus de Precisão da arma, se o personagem Apontou.
     *
     * [precisaoDaArma] null = a ficha não sabe o Prec (arma anterior ao Lote 371).
     * Nesse caso devolve 0 em vez de chutar — bônus inventado é pior que nenhum.
     */
    fun bonusDePrecisao(precisaoDaArma: Int?, apontou: Boolean): Int =
        if (apontou) (precisaoDaArma ?: 0).coerceAtLeast(0) else 0

    /**
     * O total que o Apontar traz: Precisão da arma + o que a Telescópica cancela.
     *
     * ⚠️ O teto do livro (dobro do Prec) vale para *"o bônus de Precisão com os
     * demais bônus de pontaria"*, e **não** para o cancelamento da penalidade de
     * distância, que é outra conta. Por isso o teto aqui limita só a parte de
     * pontaria — que hoje é o Prec de um turno, sempre abaixo do dobro.
     */
    fun bonusTotalDoApontar(
        personagem: Personagem,
        precisaoDaArma: Int?,
        penalidadeDistancia: Int,
        apontou: Boolean
    ): Int =
        bonusDePrecisao(precisaoDaArma, apontou) +
            cancelaDaDistancia(personagem, penalidadeDistancia, apontou)

    /** O rótulo da caixinha, já com os números desta arma e deste personagem. */
    fun rotuloApontar(
        personagem: Personagem,
        precisaoDaArma: Int?,
        penalidadeDistancia: Int
    ): String {
        val prec = precisaoDaArma ?: 0
        val telescopica = cancelaDaDistancia(personagem, penalidadeDistancia, apontou = true)
        val partes = buildList {
            if (prec > 0) add("Precisão +$prec")
            if (telescopica > 0) add("Telescópica +$telescopica")
        }
        return if (partes.isEmpty()) {
            "Apontei 1 turno (esta arma não tem Precisão cadastrada)"
        } else {
            "Apontei 1 turno: ${partes.joinToString(" e ")}"
        }
    }

    /** O mesmo, para o TalkBack — sem dizer se está marcado. */
    fun rotuloAcessivelApontar(
        personagem: Personagem,
        precisaoDaArma: Int?,
        penalidadeDistancia: Int
    ): String {
        val total = bonusTotalDoApontar(personagem, precisaoDaArma, penalidadeDistancia, apontou = true)
        return "Apontar por um turno. Soma mais $total ao ataque." +
            if (temTelescopica(personagem)) {
                " Com Visão Telescópica, Apontar dobra o desconto da distância."
            } else ""
    }
}
