package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.TipoCusto
import org.junit.Assert.assertEquals
import org.junit.Test

class RulesLayerTest {

    @Test
    fun `calcula carga e deslocamento na camada de regras`() {
        assertEquals(2, CharacterRules.calcularNivelCarga(baseCarga = 10f, pesoTotal = 25f))
        assertEquals(3, CharacterRules.calcularDeslocamentoAtual(deslocamentoBasico = 5, nivelCarga = 2))
        assertEquals(1, CharacterRules.calcularDeslocamentoAtual(deslocamentoBasico = 1, nivelCarga = 4))
    }

    @Test
    fun `calcula custo de vantagem e desvantagem na camada de regras`() {
        assertEquals(
            25,
            CharacterRules.calcularCustoVantagem(
                definicaoId = "aptidao_magica",
                tipoCusto = TipoCusto.POR_NIVEL,
                custoBase = 10,
                custoEscolhido = 0,
                nivel = 3
            )
        )

        assertEquals(
            -20,
            CharacterRules.calcularCustoDesvantagem(
                tipoCusto = TipoCusto.FIXO,
                custoBase = -10,
                custoEscolhido = -10,
                nivel = 1,
                autocontrole = 6
            )
        )
    }

    @Test
    fun `calcula bonus por dificuldade e combate na camada de regras`() {
        assertEquals(1, CharacterRules.calcularBonusPorDificuldade(Dificuldade.DIFICIL, 8))
        assertEquals(7, CombatRules.calcularEsquiva(esquivaBase = 8, nivelCarga = 2, bonusManual = 1))
        assertEquals(11, CombatRules.calcularBloqueio(nh = 11, bonusEscudo = 2, bonusManual = 1))
    }

    @Test
    fun `normaliza custo de velocidade basica por passos de 0_25`() {
        assertEquals(1, CharacterRules.calcularPassosVelocidadeBasica(0.24f))
        assertEquals(2, CharacterRules.calcularPassosVelocidadeBasica(0.49f))
        assertEquals(-1, CharacterRules.calcularPassosVelocidadeBasica(-0.26f))
    }

    @Test
    fun `aplica tabela de dano oficial para faixa de ST da ficha`() {
        assertEquals("1d", CharacterRules.calcularDanoGdP(14))
        assertEquals("2d-1", CharacterRules.calcularDanoGdP(20))
        assertEquals("3d", CharacterRules.calcularDanoGdP(30))

        assertEquals("2d", CharacterRules.calcularDanoGeB(14))
        assertEquals("3d+2", CharacterRules.calcularDanoGeB(20))
        assertEquals("5d+2", CharacterRules.calcularDanoGeB(30))
    }
}
