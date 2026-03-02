package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Test

class MagiaEnergiaRulesTest {

    @Test
    fun `aplica reducao por NH conforme tabela`() {
        assertEquals(0, MagiaEnergiaRules.reducaoPorNh(14))
        assertEquals(1, MagiaEnergiaRules.reducaoPorNh(15))
        assertEquals(1, MagiaEnergiaRules.reducaoPorNh(19))
        assertEquals(2, MagiaEnergiaRules.reducaoPorNh(20))
        assertEquals(3, MagiaEnergiaRules.reducaoPorNh(25))
        assertEquals(4, MagiaEnergiaRules.reducaoPorNh(30))
    }

    @Test
    fun `custo final nao fica negativo`() {
        assertEquals(0, MagiaEnergiaRules.custoAjustadoPorNh(1, 25))
        assertEquals(2, MagiaEnergiaRules.custoAjustadoPorNh(4, 20))
        assertEquals(4, MagiaEnergiaRules.custoAjustadoPorNh(4, 14))
    }
}
