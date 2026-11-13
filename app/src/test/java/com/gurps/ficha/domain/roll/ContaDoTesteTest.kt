package com.gurps.ficha.domain.roll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** PE-2 passo 0. Mesmos números do `test/pe2-conta-do-teste.test.js` da Mesa. */
class ContaDoTesteTest {
    @Test fun `3 e 4 sempre passam e 17 e 18 sempre falham (MB p343)`() {
        assertFalse(ContaDoTeste.passou(17, 18))
        assertFalse(ContaDoTeste.passou(18, 30))
        assertTrue(ContaDoTeste.passou(4, 3))
        assertTrue(ContaDoTeste.passou(3, 1))
        assertTrue(ContaDoTeste.passou(16, 16))
        assertFalse(ContaDoTeste.passou(5, 4))
    }

    @Test fun `a margem e a distancia ate o NH efetivo sem teto, e forcado vale 0 (MB p347)`() {
        assertEquals(12, ContaDoTeste.margemAssinada(10, 22))
        assertEquals(6, ContaDoTeste.margemAssinada(12, 18))
        assertEquals(-3, ContaDoTeste.margemAssinada(12, 9))
        assertEquals(0, ContaDoTeste.margemAssinada(4, 3))
        assertEquals(0, ContaDoTeste.margemAssinada(17, 18))
    }

    @Test fun `NH abaixo de 3 nao pode tentar exceto defesa (MB p345)`() {
        assertFalse(ContaDoTeste.podeTentar(2, false))
        assertTrue(ContaDoTeste.podeTentar(2, true))
        assertTrue(ContaDoTeste.podeTentar(3, false))
        assertFalse(ContaDoTeste.podeTentar(-4, false))
    }

    @Test fun `o critico continua o do livro`() {
        assertEquals(CriticoRules.ResultadoCritico.DECISIVO, ContaDoTeste.critico(4, 3))
        assertEquals(CriticoRules.ResultadoCritico.FALHA_CRITICA, ContaDoTeste.critico(17, 15))
        assertEquals(CriticoRules.ResultadoCritico.NORMAL, ContaDoTeste.critico(17, 18))
    }
}
