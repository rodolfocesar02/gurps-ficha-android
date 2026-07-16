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

    // ── Lote MEC-2: buffs com número ────────────────────────────────────────────────────────────

    @Test fun `Forca escala pela energia (2 por nivel), com teto de 5 e piso de 1`() {
        val forca = MagiaMecanica(efeito = "buff", buffAtributo = "ST", buffAtributoValor = 1,
            buffEnergiaPorNivel = 2, buffMaxNiveis = 5)
        assertEquals(3, MagicMechanics.calcularBuff(forca, energia = 6, alvoId = "heroi").st)
        assertEquals(5, MagicMechanics.calcularBuff(forca, energia = 40, alvoId = "heroi").st) // teto
        assertEquals(1, MagicMechanics.calcularBuff(forca, energia = 1, alvoId = "heroi").st)  // piso: pagou, leva 1
    }

    @Test fun `Debilitar aplica atributo NEGATIVO`() {
        val deb = MagiaMecanica(efeito = "buff", buffAtributo = "ST", buffAtributoValor = -1,
            buffEnergiaPorNivel = 1, buffMaxNiveis = 5)
        assertEquals(-3, MagicMechanics.calcularBuff(deb, energia = 3, alvoId = "goblin").st)
    }

    @Test fun `buff sem escala por energia vale 1 nivel — Pele de Crocodilo RD 4 nao vira RD 20`() {
        val pele = MagiaMecanica(efeito = "buff", buffRd = 4, buffEnergiaPorNivel = 0)
        assertEquals(4, MagicMechanics.calcularBuff(pele, energia = 5, alvoId = "heroi").rd)
    }

    @Test fun `Apressar sobe Deslocamento E Esquiva juntos, teto 3`() {
        val ap = MagiaMecanica(efeito = "buff", buffDeslocamento = 1, buffEsquiva = 1,
            buffEnergiaPorNivel = 2, buffMaxNiveis = 3)
        val b = MagicMechanics.calcularBuff(ap, energia = 20, alvoId = "heroi")
        assertEquals(3, b.deslocamento); assertEquals(3, b.esquiva)
    }

    @Test fun `Voo impoe Deslocamento absoluto, nao um delta`() {
        val voo = MagiaMecanica(efeito = "buff", buffDeslocamentoFixo = 10)
        assertEquals(10, MagicMechanics.calcularBuff(voo, energia = 5, alvoId = "heroi").deslocamentoFixo)
        assertEquals(0, MagicMechanics.calcularBuff(voo, energia = 5, alvoId = "heroi").deslocamento)
    }

    @Test fun `bonus de arma so vale no alcance certo — o +2 do gume nao vaza pro arco`() {
        val cac = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffDanoArma = 2, buffArmaTipo = "cac"), 1, "heroi")
        assertTrue(cac.danoArmaVale(aDistancia = false))
        assertFalse(cac.danoArmaVale(aDistancia = true))
        val dist = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffDanoArma = 2, buffArmaTipo = "distancia"), 1, "heroi")
        assertFalse(dist.danoArmaVale(aDistancia = false))
        assertTrue(dist.danoArmaVale(aDistancia = true))
    }

    @Test fun `buff sem numero nenhum e so narrado — regra de ouro`() {
        val corpoDeAgua = MagiaMecanica(efeito = "buff", buffRotulo = "Corpo de Água")
        assertTrue(MagicMechanics.calcularBuff(corpoDeAgua, 3, "heroi").soNarrado)
        assertFalse(MagicMechanics.temBuffEstruturado(corpoDeAgua))
        assertTrue(MagicMechanics.temBuffEstruturado(MagiaMecanica(efeito = "buff", buffRd = 4)))
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
