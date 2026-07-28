package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * **ST de Golpe** (MB p.88) e **ST de Levantamento** (MB p.65).
 *
 * São duas metades complementares da força, e cada uma só serve à sua metade:
 *
 * | | Dano GdP/GeB | Base de Carga | PV |
 * |---|---|---|---|
 * | **ST de Golpe** (5/nível) | ✅ | ❌ | ❌ |
 * | **ST de Levantamento** (3/nível) | ❌ | ✅ | ❌ |
 *
 * A diferença de preço conta a história: bater vale mais caro que carregar.
 *
 * ## Por que NÃO tem caixinha, ao contrário da ST Braçal
 *
 * A ST Braçal precisa de seletor porque depende da **ação**: erguer com os
 * braços conta, chutar não. Estas duas não têm essa ambiguidade — a ST de Golpe
 * vale para **todo** dano de GdP/GeB, sempre, e a de Levantamento para **toda**
 * conta de carga. Pedir confirmação a cada rolagem seria atrito sem motivo.
 *
 * ## O que elas NÃO tocam
 *
 * Nenhuma das duas mexe em **PV** — o livro é explícito nas duas. É o mesmo
 * princípio da ST Braçal: força especializada não faz o corpo aguentar mais.
 *
 * Kotlin puro e testável.
 */
object StEspecializadaRules {

    const val ID_GOLPE = "st_de_golpe"
    const val ID_LEVANTAMENTO = "st_de_levantamento"

    /** Níveis de ST de Golpe na ficha. O nível É o bônus. */
    fun bonusDeGolpe(personagem: Personagem): Int = somaDe(personagem, ID_GOLPE)

    /** Níveis de ST de Levantamento na ficha. */
    fun bonusDeLevantamento(personagem: Personagem): Int = somaDe(personagem, ID_LEVANTAMENTO)

    /**
     * A ST que vale para consultar a **Tabela de Dano**.
     *
     * ⚠️ Lê `personagem.st`, nunca `danoGdP` — ler o próprio resultado entraria
     * em laço. É a mesma armadilha que o `AtributoBonusRules` protege com a
     * trava de reentrância.
     */
    fun stParaDano(personagem: Personagem): Int =
        personagem.st + bonusDeGolpe(personagem)

    /** A ST que vale para a Base de Carga e para erguer, empurrar e puxar. */
    fun stParaCarga(personagem: Personagem): Int =
        personagem.st + bonusDeLevantamento(personagem)

    /** Se há algo a explicar na tela — sem nenhuma das duas, não há. */
    fun temAlguma(personagem: Personagem): Boolean =
        bonusDeGolpe(personagem) > 0 || bonusDeLevantamento(personagem) > 0

    /** Se a ficha tem ST de Levantamento — o seletor só aparece então. */
    fun temLevantamento(personagem: Personagem): Boolean = bonusDeLevantamento(personagem) > 0

    /**
     * A linha automática da tela: só a **ST de Golpe**.
     *
     * Ela não tem seletor porque vale para todo dano, sempre. A linha existe
     * porque o dano subiria sozinho e nada explicaria por quê — o mesmo motivo
     * da notinha de origem das perícias (Lote NOTA-1).
     *
     * A ST de Levantamento saiu daqui: ela **tem** seletor, ver [rotuloLevantamento].
     */
    fun resumo(personagem: Personagem): String? {
        val golpe = bonusDeGolpe(personagem)
        if (golpe <= 0) return null
        return "ST de Golpe +$golpe (dano usa ST ${stParaDano(personagem)})"
    }

    /**
     * Rótulo do seletor de **ST de Levantamento**.
     *
     * ## Por que ela ganhou seletor e a de Golpe não
     *
     * A Base de Carga sempre usa a ST aumentada — isso é automático e não pede
     * confirmação. Mas o livro lista mais dez usos que são **testes de ST**:
     * erguer, empurrar, puxar, rebocar, forçar portas, dobrar barras, aplicar
     * pressão contínua, agarrar, estrangular.
     *
     * Nesses o app não tem como saber a intenção: rolar ST para arrombar uma
     * porta recebe o bônus; rolar ST para não ser derrubado, não. Quem sabe é o
     * jogador, na hora — mesma decisão da ST Braçal.
     */
    fun rotuloLevantamento(personagem: Personagem): String =
        "ST de Levantamento +${bonusDeLevantamento(personagem)} " +
            "(erguer, empurrar, forçar: ST ${stParaCarga(personagem)})"

    /**
     * O mesmo, para o TalkBack.
     *
     * ⚠️ Não diz se está marcado — quem anuncia o estado é o leitor de tela.
     * Ver `UiA11y.linhaAlternavel`.
     */
    fun rotuloAcessivelLevantamento(personagem: Personagem): String =
        "ST de Levantamento, mais ${bonusDeLevantamento(personagem)}. " +
            "Rolar com ST ${stParaCarga(personagem)}. Vale para erguer, empurrar, " +
            "puxar, forçar portas e agarrar. A Base de Carga já usa esta força sempre."

    private fun somaDe(personagem: Personagem, id: String): Int =
        personagem.vantagens
            .filter { it.definicaoId == id }
            .sumOf { it.nivel.coerceAtLeast(1) }
}
