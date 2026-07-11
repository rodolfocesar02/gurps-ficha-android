package com.gurps.ficha.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote TOK-1: matemática pura do recorte quadrado do token (sem Android/Bitmap). */
class TokenImageStoreRecorteTest {

    // ─── Sem rosto: quadrado central ───────────────────────────

    @Test
    fun `paisagem sem rosto recorta quadrado central com lado igual a altura`() {
        val r = TokenImageStore.calcularRecorteQuadrado(1600, 900)
        assertEquals(900, r.lado)
        assertEquals((1600 - 900) / 2, r.left)
        assertEquals(0, r.top)
    }

    @Test
    fun `retrato sem rosto recorta quadrado central com lado igual a largura`() {
        val r = TokenImageStore.calcularRecorteQuadrado(800, 1200)
        assertEquals(800, r.lado)
        assertEquals(0, r.left)
        assertEquals((1200 - 800) / 2, r.top)
    }

    @Test
    fun `imagem quadrada sem rosto recorta ela inteira`() {
        val r = TokenImageStore.calcularRecorteQuadrado(500, 500)
        assertEquals(500, r.lado)
        assertEquals(0, r.left)
        assertEquals(0, r.top)
    }

    // ─── Com rosto: centrado e com moldura ─────────────────────

    @Test
    fun `rosto no centro gera recorte centrado no rosto com moldura 2x2`() {
        // Rosto 100x100 centrado em (500, 400) numa imagem 1000x800.
        val r = TokenImageStore.calcularRecorteQuadrado(
            1000, 800, rostoLeft = 450, rostoTop = 350, rostoRight = 550, rostoBottom = 450
        )
        assertEquals(220, r.lado) // 100 * 2.2
        // Centro do recorte deve coincidir com o centro do rosto (500, 400).
        assertEquals(500, r.left + r.lado / 2)
        assertEquals(400, r.top + r.lado / 2)
    }

    @Test
    fun `rosto perto da borda esquerda clampa o left em zero`() {
        val r = TokenImageStore.calcularRecorteQuadrado(
            1000, 800, rostoLeft = 0, rostoTop = 300, rostoRight = 100, rostoBottom = 400
        )
        assertEquals(0, r.left) // não pode negativo
        assertTrue(r.left + r.lado <= 1000)
    }

    @Test
    fun `rosto perto da borda inferior clampa o top`() {
        val r = TokenImageStore.calcularRecorteQuadrado(
            1000, 800, rostoLeft = 450, rostoTop = 700, rostoRight = 550, rostoBottom = 795
        )
        assertTrue(r.top >= 0)
        assertTrue(r.top + r.lado <= 800)
    }

    @Test
    fun `rosto gigante limita o lado ao menor lado da imagem`() {
        // Rosto de 600px numa imagem cujo menor lado é 800 → 600*2.2 = 1320 > 800, limita a 800.
        val r = TokenImageStore.calcularRecorteQuadrado(
            1000, 800, rostoLeft = 200, rostoTop = 100, rostoRight = 800, rostoBottom = 700
        )
        assertEquals(800, r.lado)
        assertTrue(r.left >= 0 && r.left + r.lado <= 1000)
        assertTrue(r.top >= 0 && r.top + r.lado <= 800)
    }

    @Test
    fun `recorte nunca ultrapassa os limites da imagem em nenhum cenario de rosto`() {
        // Varre rostos em posições variadas — invariante de segurança do createBitmap.
        val posicoes = listOf(
            intArrayOf(0, 0, 50, 50), intArrayOf(950, 750, 1000, 800),
            intArrayOf(0, 750, 50, 800), intArrayOf(950, 0, 1000, 50),
            intArrayOf(400, 300, 600, 500)
        )
        for (p in posicoes) {
            val r = TokenImageStore.calcularRecorteQuadrado(1000, 800, p[0], p[1], p[2], p[3])
            assertTrue("left>=0 p/ ${p.toList()}", r.left >= 0)
            assertTrue("top>=0 p/ ${p.toList()}", r.top >= 0)
            assertTrue("right<=w p/ ${p.toList()}", r.left + r.lado <= 1000)
            assertTrue("bottom<=h p/ ${p.toList()}", r.top + r.lado <= 800)
            assertTrue("lado>=1 p/ ${p.toList()}", r.lado >= 1)
        }
    }
}
