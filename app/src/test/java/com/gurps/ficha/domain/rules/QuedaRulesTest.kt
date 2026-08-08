package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lote MB-9** — dano por queda e colisão (MB p.430-432).
 *
 * ## 🔴 O achado deste lote
 *
 * O livro dá **duas** formas de achar a velocidade da queda — uma tabela e a
 * fórmula `√(21,4 × g × altura)` — e **elas discordam**. Em 3 de 15 alturas
 * testadas a fórmula devolve 1 a menos que a página impressa.
 *
 * Não é erro: as linhas da tabela são **faixas** ("30–32 metros"), e o valor
 * publicado é o do topo. Mas o app precisava escolher, e escolheu a **tabela** —
 * é o que o jogador confere no livro aberto na mesa.
 */
class QuedaRulesTest {

    // ==================================================================
    // 1. 🔴 A tabela ganha da fórmula
    // ==================================================================

    @Test
    fun `🔴 nos tres pontos onde a formula diverge, vale a TABELA`() {
        // A fórmula daria 25, 29 e 46. A página impressa diz 26, 30 e 47.
        assertEquals("30 m", 26, QuedaRules.velocidadeDaQueda(30))
        assertEquals("40 m", 30, QuedaRules.velocidadeDaQueda(40))
        assertEquals("100 m", 47, QuedaRules.velocidadeDaQueda(100))
    }

    @Test
    fun `os degraus do comeco da tabela batem com o livro`() {
        val esperado = mapOf(1 to 5, 2 to 7, 3 to 8, 4 to 9, 5 to 10, 6 to 11, 7 to 12, 8 to 13, 9 to 14)
        esperado.forEach { (altura, v) ->
            assertEquals("$altura m", v, QuedaRules.velocidadeDaQueda(altura))
        }
    }

    @Test
    fun `⚠️ a faixa vale para a altura inteira`() {
        // "10–11 metros | 15": tanto 10 quanto 11 dão 15.
        assertEquals(15, QuedaRules.velocidadeDaQueda(10))
        assertEquals(15, QuedaRules.velocidadeDaQueda(11))
        // E 12 já é o degrau seguinte.
        assertEquals(16, QuedaRules.velocidadeDaQueda(12))
    }

    @Test
    fun `acima da tabela, a formula assume`() {
        // A tabela impressa para em 112 m. Além disso, a fórmula.
        assertTrue(QuedaRules.velocidadeDaQueda(QuedaRules.ALTURA_MAXIMA_DA_TABELA + 100) > 49)
    }

    @Test
    fun `gravidade diferente da Terra usa a formula`() {
        // A tabela impressa é só para 1G. Na Lua (0,16G) a queda é bem mais lenta.
        val terra = QuedaRules.velocidadeDaQueda(20, 1.0)
        val lua = QuedaRules.velocidadeDaQueda(20, 0.16)
        assertTrue("lua=$lua terra=$terra", lua < terra)
    }

    @Test
    fun `altura zero ou negativa nao cai`() {
        assertEquals(0, QuedaRules.velocidadeDaQueda(0))
        assertEquals(0, QuedaRules.velocidadeDaQueda(-5))
    }

    @Test
    fun `cair de mais alto NUNCA e mais lento`() {
        (1..200).zipWithNext().forEach { (a, b) ->
            assertTrue(
                "$b m ficou mais lento que $a m",
                QuedaRules.velocidadeDaQueda(b) >= QuedaRules.velocidadeDaQueda(a)
            )
        }
    }

    // ==================================================================
    // 2. 🔴 As frações abaixo de 1d NÃO são arredondamento
    // ==================================================================

    @Test
    fun `🔴 queda pequena NUNCA sai de graca`() {
        // PV 10 caindo 1 m: (10 × 5) ÷ 100 = 0,5 → 1d-2.
        // Arredondar "normalmente" daria ZERO, e o livro nunca deixa uma queda
        // sair sem dano.
        val d = QuedaRules.danoDaQueda(pv = 10, alturaMetros = 1)
        assertEquals("1d-2", d.formula)
    }

    @Test
    fun `as tres faixas de fracao do livro`() {
        // até 0,25 → 1d-3 · até 0,5 → 1d-2 · acima → 1d-1
        assertEquals("1d-3", QuedaRules.danoDaColisao(pv = 5, velocidade = 5).formula)   // 0,25
        assertEquals("1d-2", QuedaRules.danoDaColisao(pv = 10, velocidade = 5).formula)  // 0,50
        assertEquals("1d-1", QuedaRules.danoDaColisao(pv = 10, velocidade = 8).formula)  // 0,80
    }

    @Test
    fun `de 1d para cima e arredondamento comum`() {
        // PV 10, velocidade 21 (queda de 20 m): (10 × 21) ÷ 100 = 2,1 → 2d.
        assertEquals("2d", QuedaRules.danoDaQueda(pv = 10, alturaMetros = 20).formula)
        // PV 12, velocidade 26 (30 m): 3,12 → 3d.
        assertEquals("3d", QuedaRules.danoDaQueda(pv = 12, alturaMetros = 30).formula)
        // 2,5 sobe para 3d.
        assertEquals("3d", QuedaRules.danoDaColisao(pv = 10, velocidade = 25).formula)
    }

    @Test
    fun `⚠️ o PV que conta e o de QUEM CAI`() {
        // "doeria mais chocar-se contra uma locomotiva do que contra um
        // travesseiro de mesma massa". Numa queda, quem cai bate no chão: o PV
        // da conta é o do personagem, e mais PV significa MAIS dano.
        val franzino = QuedaRules.danoDaQueda(pv = 8, alturaMetros = 20)
        val robusto = QuedaRules.danoDaQueda(pv = 20, alturaMetros = 20)
        assertTrue("${franzino.formula} vs ${robusto.formula}", robusto.dados > franzino.dados)
    }

    @Test
    fun `PV invalido nao gera dano negativo`() {
        assertEquals(0, QuedaRules.danoDaColisao(pv = 0, velocidade = 10).dados)
        assertEquals(0, QuedaRules.danoDaColisao(pv = -5, velocidade = 10).dados)
        assertEquals("nenhum", QuedaRules.danoDaColisao(pv = 10, velocidade = 0).formula)
    }

    @Test
    fun `cair de mais alto NUNCA doi menos`() {
        (1..120).zipWithNext().forEach { (a, b) ->
            val da = QuedaRules.danoDaQueda(12, a)
            val db = QuedaRules.danoDaQueda(12, b)
            val valorA = da.dados * 3.5 + da.ajuste
            val valorB = db.dados * 3.5 + db.ajuste
            assertTrue("caiu de $b m e doeu menos que de $a m", valorB >= valorA)
        }
    }

    // ==================================================================
    // 3. Objeto pontudo
    // ==================================================================

    @Test
    fun `⚠️ pontudo causa METADE dos dados — mas troca o tipo de dano`() {
        val cheio = QuedaRules.danoDaColisao(pv = 20, velocidade = 30) // 6d
        assertEquals("6d", cheio.formula)
        assertEquals("3d", QuedaRules.metadeDoDano(cheio).formula)
        // E a metade nunca some: 1d vira 1d-2, não zero.
        assertEquals("1d-2", QuedaRules.metadeDoDano(QuedaRules.Dano(1, 0)).formula)
    }

    // ==================================================================
    // 4. A conta escrita
    // ==================================================================

    @Test
    fun `a explicacao mostra a altura, a velocidade e a conta`() {
        val t = QuedaRules.explicacao(pv = 12, alturaMetros = 30)
        assertTrue(t, t.contains("30 m"))
        assertTrue(t, t.contains("26 m/s"))
        assertTrue(t, t.contains("(PV 12 × 26) ÷ 100"))
        assertTrue(t, t.contains("3d"))
    }
}
