package com.gurps.ficha.domain.magic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote AR-1: expansão do dano estruturado por energia + condição embutida. */
class MagicMechanicsTest {

    @Test fun `dano escala pela energia — Relampago 1d-1 por energia`() {
        assertEquals("3d-3", MagicMechanics.expandirDano("1d-1", energia = 3, energiaPorDado = 1))
        assertEquals("1d-1", MagicMechanics.expandirDano("1d-1", energia = 1, energiaPorDado = 1))
    }

    @Test fun `Concussao 1d por 2 pontos de energia`() {
        assertEquals("2d", MagicMechanics.expandirDano("1d", energia = 4, energiaPorDado = 2))
        assertEquals("1d", MagicMechanics.expandirDano("1d", energia = 1, energiaPorDado = 2)) // piso 1 dado
    }

    @Test fun `Toque Chocante 1d+1 por energia`() {
        assertEquals("2d+2", MagicMechanics.expandirDano("1d+1", energia = 2, energiaPorDado = 1))
    }

    @Test fun `penalidade da condicao por PV (Relampago -1 a cada 2 PV)`() {
        assertEquals(-3, MagicMechanics.penalidadeCondicaoPorPv("HT_por_pv", pvSofridos = 6))
        assertEquals(-3, MagicMechanics.penalidadeCondicaoPorPv("HT-3", pvSofridos = 1))
        assertEquals(0, MagicMechanics.penalidadeCondicaoPorPv(null, 10))
    }

    @Test fun `temDanoEstruturado so quando efeito dano com formula`() {
        assertTrue(MagicMechanics.temDanoEstruturado(MagiaMecanica(efeito = "dano", danoPorEnergia = "1d")))
        assertFalse(MagicMechanics.temDanoEstruturado(MagiaMecanica(efeito = "dano"))) // sem fórmula
        assertFalse(MagicMechanics.temDanoEstruturado(MagiaMecanica(efeito = "buff", buffRotulo = "x")))
        assertFalse(MagicMechanics.temDanoEstruturado(null))
    }
}
