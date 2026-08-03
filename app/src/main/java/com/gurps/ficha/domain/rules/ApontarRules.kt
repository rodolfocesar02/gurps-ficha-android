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

    // ==================================================================
    // Lote MIRA-4 — Apontar por MAIS DE UM SEGUNDO
    // ==================================================================
    //
    // 🔴 Achado por você em 31/07: o Apontar era **liga/desliga**, e o livro
    // deixa acumular. Faltavam duas coisas na p.364:
    //
    // > Se Apontar por mais de um segundo o personagem recebe um bônus
    // > adicional: **+1**, se Apontar por dois segundos, ou **+2** se Apontar
    // > por três ou mais segundos.
    //
    // > Se **firmar** uma arma de fogo ou besta o personagem recebe um bônus
    // > adicional de **+1** na Prec.

    /** O máximo que o app oferece: do terceiro segundo em diante nada muda. */
    const val TURNOS_MAXIMO = 3

    /** Arma apoiada em saco de areia, mureta, carro… (MB p.364). */
    const val BONUS_ARMA_FIRMADA = 1

    /**
     * O extra por passar do primeiro segundo: **+1** com dois, **+2** com três
     * ou mais. O primeiro turno não dá extra — ele é o que libera a Precisão.
     */
    fun bonusPorTurnos(turnos: Int): Int = when {
        turnos <= 1 -> 0
        turnos == 2 -> 1
        else -> 2
    }

    /**
     * O bônus de **pontaria** — Precisão, segundos extras e arma firmada —
     * já com o teto do livro.
     *
     * > A soma do bônus de Precisão com os demais bônus de pontaria nunca podem
     * > exceder o **dobro** do parâmetro Prec da arma. — MB p.373
     *
     * ⚠️ **Esse teto nunca tinha valido**, e o KDoc antigo dizia isso com todas
     * as letras: *"hoje é o Prec de um turno, sempre abaixo do dobro"*. Era
     * verdade enquanto só existia um turno. Com os segundos acumulando e a arma
     * firmada, ele passa a morder de verdade: Prec 2, três segundos e firmada
     * daria 2+2+1 = **5**, e o livro trava em **4**.
     *
     * ⚠️ **Sem Prec cadastrado, não há teto a aplicar.** Dobro de um número que
     * não se conhece não existe — então os extras entram sem corte, e o rótulo
     * avisa que a arma está sem Precisão. Inventar um teto seria pior que não
     * ter: tiraria bônus que o jogador tem direito.
     */
    fun bonusDePontaria(precisaoDaArma: Int?, turnos: Int, armaFirmada: Boolean): Int {
        if (turnos <= 0) return 0
        val prec = (precisaoDaArma ?: 0).coerceAtLeast(0)
        val extras = bonusPorTurnos(turnos) + if (armaFirmada) BONUS_ARMA_FIRMADA else 0
        val bruto = prec + extras
        if (precisaoDaArma == null || prec <= 0) return bruto
        return bruto.coerceAtMost(prec * 2)
    }

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
    ): Int = bonusTotalDoApontar(
        personagem, precisaoDaArma, penalidadeDistancia,
        turnos = if (apontou) 1 else 0, armaFirmada = false
    )

    /**
     * O total do Apontar com os segundos e a arma firmada (Lote MIRA-4).
     *
     * ⚠️ As duas parcelas são **eixos diferentes**, e por isso o teto do livro
     * não vale para a segunda:
     *
     * - **pontaria** (Precisão + segundos + firmada) soma no NH, e é o que o
     *   dobro do Prec limita;
     * - **Visão Telescópica** não soma no NH: ela *cancela* penalidade de
     *   distância (MB p.99). Cortá-la pelo teto de pontaria misturaria duas
     *   contas que o livro mantém separadas.
     */
    fun bonusTotalDoApontar(
        personagem: Personagem,
        precisaoDaArma: Int?,
        penalidadeDistancia: Int,
        turnos: Int,
        armaFirmada: Boolean
    ): Int =
        bonusDePontaria(precisaoDaArma, turnos, armaFirmada) +
            cancelaDaDistancia(personagem, penalidadeDistancia, apontou = turnos > 0)

    /** O rótulo da caixinha, já com os números desta arma e deste personagem. */
    fun rotuloApontar(
        personagem: Personagem,
        precisaoDaArma: Int?,
        penalidadeDistancia: Int
    ): String = rotuloApontar(personagem, precisaoDaArma, penalidadeDistancia, 1, false)

    /**
     * O rótulo com os segundos e a arma firmada (Lote MIRA-4).
     *
     * Mostra **de onde vem cada ponto** e, quando o teto do livro corta, diz
     * que cortou. Um bônus que para de subir sem explicação parece defeito.
     */
    fun rotuloApontar(
        personagem: Personagem,
        precisaoDaArma: Int?,
        penalidadeDistancia: Int,
        turnos: Int,
        armaFirmada: Boolean
    ): String {
        if (turnos <= 0) {
            return "Apontar (nenhum turno) — toque para acumular segundos"
        }
        val segundos = if (turnos >= TURNOS_MAXIMO) "3+ turnos" else "$turnos turno" +
            if (turnos > 1) "s" else ""
        val prec = (precisaoDaArma ?: 0).coerceAtLeast(0)
        val extraTurnos = bonusPorTurnos(turnos)
        val extraFirmada = if (armaFirmada) BONUS_ARMA_FIRMADA else 0
        val pontaria = bonusDePontaria(precisaoDaArma, turnos, armaFirmada)
        val telescopica = cancelaDaDistancia(personagem, penalidadeDistancia, apontou = true)

        val partes = buildList {
            if (prec > 0) add("Precisão +$prec")
            if (extraTurnos > 0) add("segundos +$extraTurnos")
            if (extraFirmada > 0) add("firmada +$extraFirmada")
            if (telescopica > 0) add("Telescópica +$telescopica")
        }
        // ⚠️ O aviso de Prec ausente vale mesmo quando há OUTRAS parcelas.
        // A primeira versão só avisava se a linha ficasse vazia — e aí, com dois
        // segundos, o rótulo dizia "segundos +1" e calava que a arma está sem
        // Precisão. É justamente aí que o jogador precisa saber, porque **o teto
        // do dobro não está sendo aplicado**.
        if (precisaoDaArma == null) {
            val extras = if (partes.isEmpty()) "" else ": ${partes.joinToString(", ")}"
            return "Apontei $segundos$extras (esta arma não tem Precisão cadastrada)"
        }
        if (partes.isEmpty()) {
            return "Apontei $segundos (sem bônus: a Precisão desta arma é $prec)"
        }
        // O teto do livro só é mencionado quando ele de fato cortou algo.
        val bruto = prec + extraTurnos + extraFirmada
        val cortou = prec > 0 && bruto > pontaria
        val aviso = if (cortou) " · teto de ${prec * 2} (dobro da Prec, MB p.373)" else ""
        return "Apontei $segundos: ${partes.joinToString(", ")}$aviso"
    }

    /**
     * O mesmo, para o TalkBack.
     *
     * ⚠️ Anuncia **o que o próximo toque faz**, porque o Apontar deixou de ser
     * liga/desliga e virou um contador que cicla. "Marcado" sozinho não diria
     * que ainda há segundos a acumular.
     */
    fun rotuloAcessivelApontar(
        personagem: Personagem,
        precisaoDaArma: Int?,
        penalidadeDistancia: Int,
        turnos: Int = 1
    ): String {
        val total = bonusTotalDoApontar(
            personagem, precisaoDaArma, penalidadeDistancia, turnos, armaFirmada = false
        )
        val atual = if (turnos <= 0) {
            "Sem apontar."
        } else {
            "Apontando por ${if (turnos >= TURNOS_MAXIMO) "três ou mais" else "$turnos"} " +
                "${if (turnos == 1) "turno" else "turnos"}. Soma mais $total ao ataque."
        }
        val proximo = if (turnos >= TURNOS_MAXIMO) {
            "Tocar volta a nenhum turno."
        } else {
            "Tocar acumula mais um segundo de pontaria."
        }
        return "$atual $proximo" +
            if (temTelescopica(personagem)) {
                " Com Visão Telescópica, Apontar dobra o desconto da distância."
            } else ""
    }

    /** O próximo estado do contador: 0 → 1 → 2 → 3 → 0. */
    fun proximoTurno(turnos: Int): Int = if (turnos >= TURNOS_MAXIMO) 0 else turnos + 1

    const val ROTULO_ARMA_FIRMADA =
        "Arma firmada (apoiada, ou pistola com as duas mãos): +1"

    const val ROTULO_ACESSIVEL_FIRMADA =
        "Marcar que a arma está firmada — apoiada em saco de areia, mureta ou " +
            "carro, pistola empunhada com as duas mãos, ou rifle com bipé. " +
            "Vale para arma de fogo e besta, e soma mais um."
}
