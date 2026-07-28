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

    /**
     * A linha que a tela mostra, ou null quando não há o que dizer.
     *
     * Existe porque o dano sobe sozinho e nada explicaria por quê — o mesmo
     * motivo da notinha de origem das perícias (Lote NOTA-1).
     */
    fun resumo(personagem: Personagem): String? {
        val partes = buildList {
            val golpe = bonusDeGolpe(personagem)
            if (golpe > 0) add("ST de Golpe +$golpe (dano usa ST ${stParaDano(personagem)})")
            val carga = bonusDeLevantamento(personagem)
            if (carga > 0) add("ST de Levantamento +$carga (carga usa ST ${stParaCarga(personagem)})")
        }
        return partes.joinToString(" · ").ifBlank { null }
    }

    private fun somaDe(personagem: Personagem, id: String): Int =
        personagem.vantagens
            .filter { it.definicaoId == id }
            .sumOf { it.nivel.coerceAtLeast(1) }
}
