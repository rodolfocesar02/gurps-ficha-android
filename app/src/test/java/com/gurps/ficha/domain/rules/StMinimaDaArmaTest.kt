package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A ST mínima da arma — MB p.271. Lote EQP-4.
 */
class StMinimaDaArmaTest {

    @Test
    fun `quem tem a forca exata nao e avisado`() {
        // ST 9 contra ST mínima 9: cumpre. Avisar aqui seria ruído.
        assertNull(StMinimaDaArma.avaliar(stDoPersonagem = 9, stMinimaDaArma = 9))
        assertNull(StMinimaDaArma.avaliar(stDoPersonagem = 12, stMinimaDaArma = 9))
    }

    @Test
    fun `arma sem ST cadastrada nao inventa penalidade`() {
        // A Alabarda do catálogo tem "ST —". Sem dado não se conclui nada.
        assertNull(StMinimaDaArma.avaliar(stDoPersonagem = 9, stMinimaDaArma = null))
        assertNull(StMinimaDaArma.avaliar(stDoPersonagem = 9, stMinimaDaArma = 0))
    }

    @Test
    fun `um ponto de ST que falta e um ponto de NH`() {
        // MB p.271: "-1 na perícia com a arma para cada ponto de ST que falta".
        val falta = StMinimaDaArma.avaliar(stDoPersonagem = 9, stMinimaDaArma = 12)!!
        assertEquals(3, falta.faltando)
        assertEquals(-3, falta.penalidadeNh)
        assertEquals(1, falta.pfExtra)
    }

    @Test
    fun `a razao e sempre um para um, em qualquer diferenca`() {
        // Invariante, não caso: varre a tabela inteira em vez de conferir um ponto.
        for (st in 1..20) {
            for (minima in 1..25) {
                val f = StMinimaDaArma.avaliar(st, minima) ?: continue
                assertEquals("ST $st contra mínima $minima", minima - st, f.faltando)
                assertEquals("ST $st contra mínima $minima", -(minima - st), f.penalidadeNh)
                assertTrue("penalidade tem de ser negativa", f.penalidadeNh < 0)
            }
        }
    }

    @Test
    fun `o PF a mais nao depende do tamanho da falta`() {
        // O livro diz "perde UM PF a mais", não um por ponto.
        assertEquals(1, StMinimaDaArma.avaliar(9, 10)!!.pfExtra)
        assertEquals(1, StMinimaDaArma.avaliar(9, 25)!!.pfExtra)
    }

    @Test
    fun `a fala nao tem sinal cru`() {
        // ⚠️ "-3" lido em voz alta vira "três": um redutor viraria um bônus.
        val falta = StMinimaDaArma.avaliar(9, 12)!!
        val falado = StMinimaDaArma.descricaoAcessivel(falta)
        assertFalse("a fala tem sinal cru: $falado", RotuloAcessivel.temSinalCru(falado))
        assertTrue(falado, falado.contains("menos 3"))
    }

    @Test
    fun `o aviso visivel diz o que custa, nao so que falta`() {
        val aviso = StMinimaDaArma.aviso(StMinimaDaArma.avaliar(9, 12)!!)
        assertTrue(aviso, aviso.contains("3"))
        assertTrue("o aviso não diz a consequência: $aviso", aviso.contains("PF"))
    }
}
