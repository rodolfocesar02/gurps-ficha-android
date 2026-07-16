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

    @Test fun `dano FIXO nao escala com a energia — Geiser 3d custe o que custar`() {
        // Sem a trava, o Géiser (custo básico 5) sairia como 15d.
        assertEquals("3d", MagicMechanics.expandirDano("3d", energia = 5, energiaPorDado = 1, danoFixo = true))
        assertEquals("1d", MagicMechanics.expandirDano("1d", energia = 4, energiaPorDado = 1, danoFixo = true))
    }

    @Test fun `dano em PONTOS vira 0d+N — o rolador exige n-d e devolveria 0 para um 1 pelado`() {
        // Nuvem de Faíscas: 1 ponto por segundo POR ponto de energia (escala, mas não é dado).
        assertEquals("0d+3", MagicMechanics.expandirDano("1", energia = 3, energiaPorDado = 1))
        assertEquals("0d+1", MagicMechanics.expandirDano("1", energia = 1, energiaPorDado = 1))
        // Pontos + dano fixo: trava em 1 ponto.
        assertEquals("0d+1", MagicMechanics.expandirDano("1", energia = 5, energiaPorDado = 1, danoFixo = true))
    }

    @Test fun `expandirDano sem danoFixo mantem o comportamento antigo (regressao)`() {
        assertEquals("3d-3", MagicMechanics.expandirDano("1d-1", energia = 3, energiaPorDado = 1, danoFixo = false))
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
