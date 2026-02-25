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
    fun `aplica arredondamento e limites nas defesas ativas`() {
        // Esquiva nunca menor que 1
        assertEquals(1, CombatRules.calcularEsquiva(esquivaBase = 5, nivelCarga = 5, bonusManual = -3))
        assertEquals(1, CombatRules.calcularEsquivaBase(esquivaBase = 4, nivelCarga = 5))

        // Apara/Bloqueio usam floor(NH/2)+3
        assertEquals(8, CombatRules.calcularAparaBase(nh = 11)) // floor(5.5)+3
        assertEquals(8, CombatRules.calcularAparaBase(nh = 10)) // floor(5.0)+3
        assertEquals(9, CombatRules.calcularBloqueioBase(nh = 12))
    }

    @Test
    fun `valida bordas finais de defesa base e impacto de equipamento`() {
        // Esquiva base sem carga vs carga extrema (nível 5)
        assertEquals(8, CombatRules.calcularEsquivaBase(esquivaBase = 8, nivelCarga = 0))
        assertEquals(3, CombatRules.calcularEsquivaBase(esquivaBase = 8, nivelCarga = 5))

        // Apara/Bloqueio com NH ímpar/par seguem floor(NH/2)+3
        assertEquals(8, CombatRules.calcularAparaBase(nh = 11))
        assertEquals(9, CombatRules.calcularAparaBase(nh = 12))
        assertEquals(8, CombatRules.calcularBloqueioBase(nh = 11))
        assertEquals(9, CombatRules.calcularBloqueioBase(nh = 12))

        // Bloqueio soma DB do escudo de forma linear
        assertEquals(8, CombatRules.calcularBloqueio(nh = 11, bonusEscudo = 0, bonusManual = 0))
        assertEquals(9, CombatRules.calcularBloqueio(nh = 11, bonusEscudo = 1, bonusManual = 0))
        assertEquals(10, CombatRules.calcularBloqueio(nh = 11, bonusEscudo = 2, bonusManual = 0))
    }

    @Test
    fun `respeita tabela completa de bonus de pericia por pontos`() {
        // Facil: Atr, Atr+1, Atr+1, Atr+2, Atr+3, Atr+4
        assertEquals(0, CharacterRules.calcularBonusPorDificuldade(Dificuldade.FACIL, 1))
        assertEquals(1, CharacterRules.calcularBonusPorDificuldade(Dificuldade.FACIL, 2))
        assertEquals(1, CharacterRules.calcularBonusPorDificuldade(Dificuldade.FACIL, 3))
        assertEquals(2, CharacterRules.calcularBonusPorDificuldade(Dificuldade.FACIL, 4))
        assertEquals(3, CharacterRules.calcularBonusPorDificuldade(Dificuldade.FACIL, 8))
        assertEquals(4, CharacterRules.calcularBonusPorDificuldade(Dificuldade.FACIL, 12))

        // Media: Atr-1, Atr, Atr, Atr+1, Atr+2, Atr+3
        assertEquals(-1, CharacterRules.calcularBonusPorDificuldade(Dificuldade.MEDIA, 1))
        assertEquals(0, CharacterRules.calcularBonusPorDificuldade(Dificuldade.MEDIA, 2))
        assertEquals(0, CharacterRules.calcularBonusPorDificuldade(Dificuldade.MEDIA, 3))
        assertEquals(1, CharacterRules.calcularBonusPorDificuldade(Dificuldade.MEDIA, 4))
        assertEquals(2, CharacterRules.calcularBonusPorDificuldade(Dificuldade.MEDIA, 8))
        assertEquals(3, CharacterRules.calcularBonusPorDificuldade(Dificuldade.MEDIA, 12))

        // Dificil: Atr-2, Atr-1, Atr-1, Atr, Atr+1, Atr+2
        assertEquals(-2, CharacterRules.calcularBonusPorDificuldade(Dificuldade.DIFICIL, 1))
        assertEquals(-1, CharacterRules.calcularBonusPorDificuldade(Dificuldade.DIFICIL, 2))
        assertEquals(-1, CharacterRules.calcularBonusPorDificuldade(Dificuldade.DIFICIL, 3))
        assertEquals(0, CharacterRules.calcularBonusPorDificuldade(Dificuldade.DIFICIL, 4))
        assertEquals(1, CharacterRules.calcularBonusPorDificuldade(Dificuldade.DIFICIL, 8))
        assertEquals(2, CharacterRules.calcularBonusPorDificuldade(Dificuldade.DIFICIL, 12))

        // Muito dificil: Atr-3, Atr-2, Atr-2, Atr-1, Atr, Atr+1
        assertEquals(-3, CharacterRules.calcularBonusPorDificuldade(Dificuldade.MUITO_DIFICIL, 1))
        assertEquals(-2, CharacterRules.calcularBonusPorDificuldade(Dificuldade.MUITO_DIFICIL, 2))
        assertEquals(-2, CharacterRules.calcularBonusPorDificuldade(Dificuldade.MUITO_DIFICIL, 3))
        assertEquals(-1, CharacterRules.calcularBonusPorDificuldade(Dificuldade.MUITO_DIFICIL, 4))
        assertEquals(0, CharacterRules.calcularBonusPorDificuldade(Dificuldade.MUITO_DIFICIL, 8))
        assertEquals(1, CharacterRules.calcularBonusPorDificuldade(Dificuldade.MUITO_DIFICIL, 12))
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

    @Test
    fun `resolve dano de arma por ST para tokens GdP e GeB`() {
        assertEquals("1d+1 corte", CharacterRules.resolverDanoPorSt("GeB+1 corte", st = 10))
        assertEquals("1d-3 perf", CharacterRules.resolverDanoPorSt("GdP-1 perf", st = 10))
        assertEquals("1d+3 perf", CharacterRules.resolverDanoPorSt("GdP+3 perf", st = 13))
        assertEquals("HT-3(0,5) at", CharacterRules.resolverDanoPorSt("HT-3(0,5) at", st = 12))
    }
}
