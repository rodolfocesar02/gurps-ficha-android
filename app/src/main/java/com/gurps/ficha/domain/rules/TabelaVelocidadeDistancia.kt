package com.gurps.ficha.domain.rules

/**
 * **Tabela de Tamanho e Velocidade/Distância** (MB p.550-551).
 *
 * Quanto mais longe o alvo, mais difícil acertar. A penalidade **não é linear**:
 *
 * > Um alvo até **2 metros** está suficientemente próximo para que **não haja
 * > penalidade**. A **3 metros**, há uma penalidade de **-1**; até 5 metros, -2;
 * > até 7 metros, -3; até 10 metros, -4; e assim por diante, com **cada aumento
 * > aproximado de 50% na distância resultando numa penalidade de -1**. (MB p.373)
 *
 * Analogia: é como o volume de um som. Do silêncio para um sussurro a diferença
 * é enorme; de um show para um avião, quase nada. Dobrar a distância sempre custa
 * o mesmo pouquinho, não importa se você saiu de 10 ou de 1000 metros.
 *
 * ## As duas armadilhas
 *
 * ⚠️ **Arredonda para a distância MAIOR, nunca para a mais próxima.** O próprio
 * livro dá o exemplo: 17 metros vira 20 metros, ou seja **-6** — e não -5.
 * Arredondar "para o mais perto" daria ao jogador um bônus que ele não tem, e
 * ninguém perceberia olhando a tela.
 *
 * ⚠️ **Velocidade e distância são UMA penalidade, não duas.** O livro manda somar
 * antes de consultar: um motoboy a 40 m viajando a 30 m/s conta como **70** e dá
 * **-9**. Tratar como duas penalidades separadas daria -7 e -7 = -14, quase o
 * dobro. Ver [penalidadeCombinada].
 *
 * Kotlin puro e testável. A mesma tabela serve ao Modificador de Tamanho (com o
 * sinal trocado) e às penalidades de longa distância dos sentidos — por isso ela
 * mora em `domain/rules/` e não dentro da tela.
 */
object TabelaVelocidadeDistancia {

    /** Uma linha da tabela: até [metros], a penalidade é [penalidade]. */
    data class Degrau(val metros: Int, val penalidade: Int) {

        /** "20 m" ou "1,5 km" — como o livro escreve. */
        val rotulo: String
            get() = when {
                metros < 1000 -> "$metros m"
                metros % 1000 == 0 -> "${metros / 1000} km"
                else -> "${(metros / 100) / 10.0}".replace('.', ',') + " km"
            }
    }

    /**
     * A coluna "Velocidade/Distância" do livro, degrau a degrau.
     *
     * Escrita à mão em vez de calculada de propósito: assim dá para conferir
     * linha por linha contra a página 551, que é o que importa numa tabela de
     * regra. A progressão é regular — 1, 1,5, 2, 3, 5, 7 se repetindo a cada
     * casa decimal, com **-6 por década**.
     */
    val DEGRAUS: List<Degrau> = listOf(
        Degrau(2, 0),
        Degrau(3, -1),
        Degrau(5, -2),
        Degrau(7, -3),
        Degrau(10, -4),
        Degrau(15, -5),
        Degrau(20, -6),
        Degrau(30, -7),
        Degrau(50, -8),
        Degrau(70, -9),
        Degrau(100, -10),
        Degrau(150, -11),
        Degrau(200, -12),
        Degrau(300, -13),
        Degrau(500, -14),
        Degrau(700, -15),
        Degrau(1_000, -16),
        Degrau(1_500, -17),
        Degrau(2_000, -18),
        Degrau(3_000, -19),
        Degrau(5_000, -20),
        Degrau(7_000, -21),
        Degrau(10_000, -22),
        Degrau(15_000, -23),
        Degrau(20_000, -24),
        Degrau(30_000, -25),
        Degrau(50_000, -26),
        Degrau(70_000, -27),
        Degrau(100_000, -28),
        Degrau(150_000, -29),
        Degrau(200_000, -30)
    )

    /** O degrau padrão de abertura: 2 metros, sem penalidade. */
    val INDICE_PADRAO = 0

    /**
     * A penalidade para um alvo a [metros] de distância.
     *
     * Arredonda **para cima na tabela** (para a distância maior), como o livro
     * manda. Distância zero ou negativa devolve 0 — ninguém atira para trás.
     */
    fun penalidadePara(metros: Int): Int {
        if (metros <= DEGRAUS.first().metros) return 0
        DEGRAUS.forEach { if (metros <= it.metros) return it.penalidade }
        // Além da tabela impressa: cada 10× na medida vale mais -6 (MB p.551).
        var m = DEGRAUS.last().metros
        var p = DEGRAUS.last().penalidade
        while (metros > m) {
            m *= 10
            p -= 6
        }
        return p
    }

    /**
     * A penalidade de um alvo **distante e em movimento** — uma só, não duas.
     *
     * > Acrescente a velocidade em metros/segundo **à distância** antes de
     * > consultar a coluna "Medida Linear". (MB p.551)
     *
     * Exemplo do livro: 40 m + 30 m/s = 70 → **-9**.
     */
    fun penalidadeCombinada(metros: Int, velocidadeMs: Int): Int =
        penalidadePara(metros.coerceAtLeast(0) + velocidadeMs.coerceAtLeast(0))

    /**
     * A mesma conta, para quem é **míope** (Disopia, MB p.135).
     *
     * > Para ataque à distância, **dobre a distância até o alvo** quando estiver
     * > calculando o modificador de distância.
     *
     * ⚠️ Dobra **só a distância**, não a velocidade: o livro fala de "distância
     * até o alvo". Um míope a 20 metros conta como 40, que arredonda para o
     * degrau de 50 → **-8** em vez de -6: o dobro atravessa dois degraus.
     */
    fun penalidadeCombinada(metros: Int, velocidadeMs: Int, miope: Boolean): Int {
        val d = metros.coerceAtLeast(0).let { if (miope) it * 2 else it }
        return penalidadePara(d + velocidadeMs.coerceAtLeast(0))
    }

    /**
     * O índice do degrau onde uma distância cai — para o botão `-/+` da tela.
     *
     * O seletor anda **de degrau em degrau**, não de metro em metro: assim cada
     * toque vale exatamente -1, e chegar a 100 metros são 10 toques em vez de 98.
     * O botão deixa de ser um contador e passa a ser a própria regra.
     */
    fun indiceDoDegrau(metros: Int): Int {
        val i = DEGRAUS.indexOfFirst { metros <= it.metros }
        return if (i < 0) DEGRAUS.lastIndex else i
    }

    /** O degrau de um índice, preso aos limites da tabela. */
    fun degrau(indice: Int): Degrau = DEGRAUS[indice.coerceIn(0, DEGRAUS.lastIndex)]

    /**
     * A linha de explicação da conta, para a tela poder mostrar de onde veio o
     * número.
     *
     * Sem isto o jogador lê "-9" e não tem como desconfiar quando estiver errado
     * — mesma razão das notinhas de origem do Lote NOTA-1.
     */
    fun explicacao(metros: Int, velocidadeMs: Int): String {
        val p = penalidadeCombinada(metros, velocidadeMs)
        val sinal = if (p == 0) "0" else "$p"
        return if (velocidadeMs > 0) {
            "$metros m + $velocidadeMs m/s = ${metros + velocidadeMs} → $sinal"
        } else {
            "${degrau(indiceDoDegrau(metros)).rotulo} → $sinal"
        }
    }
}
