package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A tabela de distância do livro (MB p.550-551), e as duas armadilhas dela.
 *
 * Os dois testes que **não podem faltar** estão marcados com ⚠️: eles cobrem
 * erros que passariam despercebidos na tela, porque o número sai plausível.
 */
class TabelaVelocidadeDistanciaTest {

    @Test
    fun `a coluna do livro, degrau a degrau`() {
        // Conferido contra a página 551. Se um destes mudar, foi engano.
        val esperado = mapOf(
            2 to 0, 3 to -1, 5 to -2, 7 to -3, 10 to -4, 15 to -5, 20 to -6,
            30 to -7, 50 to -8, 70 to -9, 100 to -10, 150 to -11, 200 to -12,
            300 to -13, 500 to -14, 700 to -15, 1_000 to -16
        )
        esperado.forEach { (metros, penalidade) ->
            assertEquals(
                "$metros m",
                penalidade,
                TabelaVelocidadeDistancia.penalidadePara(metros)
            )
        }
    }

    @Test
    fun `⚠️ arredonda para a distancia MAIOR, nunca para a mais proxima`() {
        // O exemplo do livro (p.373): "O alvo está a 17 metros. Esse valor é
        // arredondado para 20 metros na tabela, resultando numa penalidade de
        // -6."
        //
        // Arredondar para o mais PERTO daria 15 m e -5 — um ponto de bônus que o
        // personagem não tem. Erro invisível: o número sai plausível na tela.
        assertEquals(-6, TabelaVelocidadeDistancia.penalidadePara(17))

        // Mais alguns, para o caso de alguém "otimizar" com arredondamento:
        assertEquals("8 m ainda é o degrau de 10", -4, TabelaVelocidadeDistancia.penalidadePara(8))
        assertEquals("21 m já é o degrau de 30", -7, TabelaVelocidadeDistancia.penalidadePara(21))
        assertEquals("exato no degrau fica no degrau", -6, TabelaVelocidadeDistancia.penalidadePara(20))
    }

    @Test
    fun `⚠️ velocidade SOMA a distancia, nao e uma segunda penalidade`() {
        // O exemplo do livro (p.551): "Um motoboy a 40 metros de distância,
        // viajando a 90 km/h (ou 30 m/s) tem uma velocidade/distância de
        // 40 + 30 = 70, que resulta numa base de 70 metros, e implica numa
        // penalidade de -9."
        assertEquals(-9, TabelaVelocidadeDistancia.penalidadeCombinada(40, 30))

        // O erro que este teste existe para impedir: aplicar as duas separadas.
        // 40 m arredonda para o degrau de 50 (-8) e 30 m/s é o degrau de 30
        // (-7) — daria -15, quase o dobro do certo.
        val separadas = TabelaVelocidadeDistancia.penalidadePara(40) +
            TabelaVelocidadeDistancia.penalidadePara(30)
        assertEquals("o jeito errado daria -15", -15, separadas)

        // O outro exemplo do livro: míssil a 5 m, a 1.000 m/s → 1.005 → -17.
        assertEquals(-17, TabelaVelocidadeDistancia.penalidadeCombinada(5, 1_000))
    }

    @Test
    fun `ate 2 metros nao ha penalidade`() {
        // MB p.551: "disparar contra um alvo próximo não é mais fácil (nem mais
        // difícil) que atacar em combate corporal".
        assertEquals(0, TabelaVelocidadeDistancia.penalidadePara(2))
        assertEquals(0, TabelaVelocidadeDistancia.penalidadePara(1))
        assertEquals(0, TabelaVelocidadeDistancia.penalidadePara(0))
        assertEquals("distância negativa não trava", 0, TabelaVelocidadeDistancia.penalidadePara(-5))
    }

    @Test
    fun `alem da tabela impressa, cada 10 vezes vale mais menos 6`() {
        // MB p.551: "cada aumento de 10 vezes na medida linear resultando em
        // (...) -6 ao modificador de velocidade/distância".
        val ultimo = TabelaVelocidadeDistancia.DEGRAUS.last()
        assertEquals(
            ultimo.penalidade - 6,
            TabelaVelocidadeDistancia.penalidadePara(ultimo.metros * 10)
        )
        assertEquals(
            ultimo.penalidade - 12,
            TabelaVelocidadeDistancia.penalidadePara(ultimo.metros * 100)
        )
    }

    @Test
    fun `cada degrau do seletor vale exatamente menos 1`() {
        // É o que justifica o botão andar de degrau em degrau em vez de metro em
        // metro: cada toque é um ponto. Se esta invariante quebrar, o botão
        // deixa de ser a regra e vira um contador qualquer.
        TabelaVelocidadeDistancia.DEGRAUS.zipWithNext { a, b ->
            assertEquals(
                "de ${a.metros}m para ${b.metros}m",
                a.penalidade - 1,
                b.penalidade
            )
        }
    }

    @Test
    fun `o seletor comeca em 2 metros, sem penalidade`() {
        val inicial = TabelaVelocidadeDistancia.degrau(TabelaVelocidadeDistancia.INDICE_PADRAO)
        assertEquals(2, inicial.metros)
        assertEquals(0, inicial.penalidade)
    }

    @Test
    fun `o seletor nao sai da tabela`() {
        assertEquals(
            TabelaVelocidadeDistancia.DEGRAUS.first(),
            TabelaVelocidadeDistancia.degrau(-99)
        )
        assertEquals(
            TabelaVelocidadeDistancia.DEGRAUS.last(),
            TabelaVelocidadeDistancia.degrau(999)
        )
    }

    @Test
    fun `indiceDoDegrau casa com a penalidade`() {
        listOf(2, 8, 17, 40, 300, 5_000).forEach { metros ->
            val degrau = TabelaVelocidadeDistancia.degrau(
                TabelaVelocidadeDistancia.indiceDoDegrau(metros)
            )
            assertEquals(
                "$metros m",
                TabelaVelocidadeDistancia.penalidadePara(metros),
                degrau.penalidade
            )
        }
    }

    @Test
    fun `o rotulo vira km depois de mil metros`() {
        assertEquals("700 m", TabelaVelocidadeDistancia.degrau(
            TabelaVelocidadeDistancia.indiceDoDegrau(700)).rotulo)
        assertEquals("1 km", TabelaVelocidadeDistancia.degrau(
            TabelaVelocidadeDistancia.indiceDoDegrau(1_000)).rotulo)
        assertEquals("1,5 km", TabelaVelocidadeDistancia.degrau(
            TabelaVelocidadeDistancia.indiceDoDegrau(1_500)).rotulo)
    }

    @Test
    fun `a explicacao mostra a conta quando ha velocidade`() {
        // Sem a conta à vista o jogador não tem como desconfiar do número.
        assertTrue(
            TabelaVelocidadeDistancia.explicacao(40, 30).contains("40 m + 30 m/s = 70")
        )
        // Alvo parado não precisa de conta nenhuma.
        assertTrue(TabelaVelocidadeDistancia.explicacao(20, 0).startsWith("20 m"))
    }
}
