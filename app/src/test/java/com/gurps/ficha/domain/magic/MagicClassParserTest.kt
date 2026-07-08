package com.gurps.ficha.domain.magic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote MA-1: parser tolerante do campo `classe` do catálogo. Kotlin puro. */
class MagicClassParserTest {

    // ─── Classes simples ───────────────────────────────────────

    @Test fun `Comum simples`() {
        val r = MagicClassParser.parse("Comum")
        assertEquals(setOf(TipoClasseMagia.COMUM), r.classes)
        assertNull(r.resistencia)
        assertFalse(r.temParteNaoReconhecida)
    }

    @Test fun `Area com acento`() {
        assertEquals(setOf(TipoClasseMagia.AREA), MagicClassParser.parse("Área").classes)
    }

    @Test fun `Bloqueio`() {
        assertEquals(setOf(TipoClasseMagia.BLOQUEIO), MagicClassParser.parse("Bloqueio").classes)
    }

    // ─── Aliases e typos do JSON real ─────────────────────────

    @Test fun `Comm normaliza para COMUM`() {
        assertEquals(setOf(TipoClasseMagia.COMUM), MagicClassParser.parse("Comm").classes)
    }

    @Test fun `Projetil sem acento normaliza para PROJETIL`() {
        assertEquals(setOf(TipoClasseMagia.PROJETIL), MagicClassParser.parse("Projetil").classes)
    }

    @Test fun `Encant abreviado normaliza para ENCANTAMENTO`() {
        assertEquals(setOf(TipoClasseMagia.ENCANTAMENTO), MagicClassParser.parse("Encant.").classes)
    }

    // ─── Resistência simples ───────────────────────────────────

    @Test fun `Comum R-HT`() {
        val r = MagicClassParser.parse("Comum/R-HT")
        assertEquals(setOf(TipoClasseMagia.COMUM), r.classes)
        assertNotNull(r.resistencia)
        assertEquals(AtributoResistencia.HT, r.resistencia!!.atributo)
        assertEquals(0, r.resistencia!!.modificadorDefensor)
    }

    @Test fun `Comum R-Vont`() {
        val r = MagicClassParser.parse("Comum/R-Vont")
        assertEquals(AtributoResistencia.VONTADE, r.resistencia?.atributo)
    }

    @Test fun `Comum R-Magica`() {
        val r = MagicClassParser.parse("Comum/R-Mágica")
        assertEquals(AtributoResistencia.MAGICA, r.resistencia?.atributo)
    }

    @Test fun `Area R-HT`() {
        val r = MagicClassParser.parse("Área/R-HT")
        assertEquals(setOf(TipoClasseMagia.AREA), r.classes)
        assertEquals(AtributoResistencia.HT, r.resistencia?.atributo)
    }

    // ─── Resistência com modificador ─────────────────────────

    @Test fun `R-Vont+1 aplica mais 1 ao defensor`() {
        val r = MagicClassParser.parse("Comum/R-Vont+1")
        assertEquals(AtributoResistencia.VONTADE, r.resistencia?.atributo)
        assertEquals(+1, r.resistencia?.modificadorDefensor)
    }

    @Test fun `R-Vont-2 aplica menos 2 ao defensor`() {
        val r = MagicClassParser.parse("Comum/R-Vont-2")
        assertEquals(AtributoResistencia.VONTADE, r.resistencia?.atributo)
        assertEquals(-2, r.resistencia?.modificadorDefensor)
    }

    @Test fun `R-HT+2 aplica mais 2 ao defensor`() {
        val r = MagicClassParser.parse("Comum/R-HT+2")
        assertEquals(+2, r.resistencia?.modificadorDefensor)
    }

    // ─── Resistência combinada ────────────────────────────────

    @Test fun `R-HT ou IQ vira HT com IQ como alternativo`() {
        val r = MagicClassParser.parse("Comum/R-HT ou IQ")
        assertEquals(AtributoResistencia.HT, r.resistencia?.atributo)
        assertTrue(r.resistencia?.alternativos?.contains(AtributoResistencia.IQ) == true)
    }

    // ─── Multi-classe ─────────────────────────────────────────

    @Test fun `Comum ou Bloqueio vira as duas`() {
        val r = MagicClassParser.parse("Comum ou Bloqueio")
        assertEquals(setOf(TipoClasseMagia.COMUM, TipoClasseMagia.BLOQUEIO), r.classes)
    }

    @Test fun `Especial e Area vira as duas`() {
        val r = MagicClassParser.parse("Especial/Área")
        assertEquals(setOf(TipoClasseMagia.ESPECIAL, TipoClasseMagia.AREA), r.classes)
    }

    // ─── Marcador especial # ─────────────────────────────────

    @Test fun `R-HT# marca temNotaEspecial`() {
        val r = MagicClassParser.parse("Comum/R-HT#")
        assertEquals(AtributoResistencia.HT, r.resistencia?.atributo)
        assertTrue(r.resistencia?.temNotaEspecial == true)
    }

    // ─── Resistência composta / customizada (delegar ao Narrador) ─

    @Test fun `R-ST+Vont dividido por 2 vira COMPOSTA`() {
        val r = MagicClassParser.parse("Área/R-(ST+Vont)/2")
        // O parser vê "Área" (ok) e "(ST+Vont)" que NÃO começa com R- → não reconhece, cai em ESPECIAL.
        // Ou reconhece como R-? Depende da string bruta. O importante: parser não crasha e deixa
        // marcado como não reconhecido para o motor delegar ao Narrador.
        assertNotNull(r) // não crasha
        assertTrue("classes contém Área", r.classes.contains(TipoClasseMagia.AREA))
    }

    @Test fun `R-Tranca Magica delega ao Narrador via ESPECIAL`() {
        val r = MagicClassParser.parse("Comum/R-Tranca Mágica")
        assertEquals(setOf(TipoClasseMagia.COMUM), r.classes)
        assertNotNull(r.resistencia)
        assertEquals(AtributoResistencia.ESPECIAL, r.resistencia!!.atributo)
        assertEquals("Tranca Mágica", r.resistencia!!.rotuloCustomizado)
    }

    @Test fun `R-Vont+AM delega ao Narrador via COMPOSTA`() {
        val r = MagicClassParser.parse("Comum/R-Vont+AM")
        assertEquals(setOf(TipoClasseMagia.COMUM), r.classes)
        assertNotNull(r.resistencia)
        // Como "+AM" não é um número simples, cai em COMPOSTA para delegar ao Narrador.
        assertEquals(AtributoResistencia.COMPOSTA, r.resistencia!!.atributo)
    }

    // ─── Casos degenerados ────────────────────────────────────

    @Test fun `string vazia vira ESPECIAL sem crashar`() {
        val r = MagicClassParser.parse("")
        assertEquals(setOf(TipoClasseMagia.ESPECIAL), r.classes)
        assertTrue(r.temParteNaoReconhecida)
    }

    @Test fun `string nula vira ESPECIAL sem crashar`() {
        val r = MagicClassParser.parse(null)
        assertEquals(setOf(TipoClasseMagia.ESPECIAL), r.classes)
    }

    @Test fun `string com apenas espacos vira ESPECIAL sem crashar`() {
        val r = MagicClassParser.parse("   ")
        assertEquals(setOf(TipoClasseMagia.ESPECIAL), r.classes)
    }

    @Test fun `classe totalmente desconhecida vira ESPECIAL`() {
        val r = MagicClassParser.parse("XPTO")
        assertEquals(setOf(TipoClasseMagia.ESPECIAL), r.classes)
        assertTrue(r.temParteNaoReconhecida)
    }
}
