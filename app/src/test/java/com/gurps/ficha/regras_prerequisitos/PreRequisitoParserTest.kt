package com.gurps.ficha.regras_prerequisitos

import org.junit.Assert.*
import org.junit.Test

class PreRequisitoParserTest {
    @Test
    fun `empty or dash returns no types`() {
        assertTrue(PreRequisitoParser.parse("  ").tipos.isEmpty())
        assertTrue(PreRequisitoParser.parse("—").tipos.isEmpty())
    }

    @Test
    fun `recognizes attribute minimum`() {
        val res = PreRequisitoParser.parse("IQ 12+")
        assertEquals(1, res.tipos.size)
        val item = res.tipos[0]
        assertTrue(item is PreRequisitoType.AttributeMin)
        assertEquals("IQ", (item as PreRequisitoType.AttributeMin).atributo)
        assertEquals(12, item.minimo)
    }

    @Test
    fun `recognizes aptidao magica variants`() {
        // various forms should all parse to same level
        val examples = listOf("AM2", "AM 2", "AM+2", "am 2", "Aptidao Magica 2",
            "Aptidão Mágica nível 3")
        examples.forEach {
            val tipos = PreRequisitoParser.parse(it).tipos
            assertEquals(1, tipos.size)
            assertTrue(tipos[0] is PreRequisitoType.AptidaoMagica)
        }
    }

    @Test
    fun `recognizes magias escola`() {
        val res = PreRequisitoParser.parse("2 mágicas de Fogo")
        assertEquals(1, res.tipos.size)
        val item = res.tipos[0]
        assertTrue(item is PreRequisitoType.MagiasEscola)
        val me = item as PreRequisitoType.MagiasEscola
        assertEquals(2, me.quantidade)
        assertEquals("Fogo", me.escola)
    }

    @Test
    fun `fallback to magic name`() {
        val res = PreRequisitoParser.parse("Magia Exemplo")
        assertEquals(1, res.tipos.size)
        assertTrue(res.tipos[0] is PreRequisitoType.MagiaConhecida)
    }
}
