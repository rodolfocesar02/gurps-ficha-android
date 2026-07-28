package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.StBracalRule
import com.gurps.ficha.model.Personagem

/**
 * O que a **ST Braçal** faz na mesa (MB p.89).
 *
 * O custo dela mora em `traits/BracalCustoRules.kt`; aqui fica o efeito.
 *
 * A regra do livro, em uma frase: os braços agem como se tivessem uma ST maior,
 * mas o resto do corpo não. Um ST 10 com ST Braçal +4 **continua** com ST 10
 * para PV, Base de Carga e esforço corporal — porém ergue, arremessa e **ataca**
 * como ST 14.
 *
 * É por isso que ela NÃO entra na soma geral de atributo (`AtributoBonusRules`):
 * somar lá inflaria PV e Carga, exatamente o que o livro proíbe. O bônus vive
 * separado e o jogador liga quando a ação é dos braços.
 *
 * **O dano é o efeito prático.** No GURPS o dano corpo a corpo sai da ST: GdP
 * (impulso, para estocadas) e GeB (balanço, para golpes). Como atacar com uma
 * arma empunhada é ação de braço, o dano usa a ST braçal — e é essa a diferença
 * que aparece na tela.
 *
 * Kotlin puro e testável, mesmo desenho de [AutocontroleRules] e [ReacaoRules].
 */
object StBracalRules {

    /**
     * Soma dos níveis de ST Braçal da ficha.
     *
     * O nível É o bônus: "ST Braçal +4" é nível 4. Somar várias seleções cobre
     * a ficha que comprou um braço separado do outro.
     */
    fun bonusDe(personagem: Personagem): Int =
        personagem.vantagens
            .filter { it.definicaoId == StBracalRule.ID }
            .sumOf { it.nivel.coerceAtLeast(1) }

    /** Se a ficha tem alguma ST Braçal — o painel só aparece então. */
    fun temStBracal(personagem: Personagem): Boolean = bonusDe(personagem) > 0

    /** A ST que os braços usam: a do corpo mais o bônus. */
    fun stDosBracos(personagem: Personagem): Int = personagem.st + bonusDe(personagem)

    /** Dano de impulso (estocada) usando a ST dos braços. */
    fun danoGdPDosBracos(personagem: Personagem): String =
        CharacterRules.calcularDanoGdP(stDosBracos(personagem))

    /** Dano de balanço (golpe) usando a ST dos braços. */
    fun danoGeBDosBracos(personagem: Personagem): String =
        CharacterRules.calcularDanoGeB(stDosBracos(personagem))

    /**
     * Rótulo curto do seletor: `ST Braçal +4 (braços agem como ST 14)`.
     *
     * Traz o número final junto porque "+4" sozinho não diz contra o quê — e
     * quem lê a ficha no meio da mesa não vai fazer a conta.
     */
    fun rotulo(personagem: Personagem): String {
        val bonus = bonusDe(personagem)
        return "ST Braçal +$bonus (braços agem como ST ${stDosBracos(personagem)})"
    }

    /** O mesmo rótulo, escrito para o TalkBack ler. */
    fun rotuloAcessivel(personagem: Personagem, ativo: Boolean): String =
        "ST Braçal, mais ${bonusDe(personagem)}. Braços agem como ST " +
            "${stDosBracos(personagem)}. ${if (ativo) "Ativado." else "Desativado."} " +
            "Vale para erguer, arremessar e atacar com os braços."
}
