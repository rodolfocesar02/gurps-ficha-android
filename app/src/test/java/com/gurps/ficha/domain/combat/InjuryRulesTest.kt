package com.gurps.ficha.domain.combat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Lote 362 (B4): choque, ferimento grave, recuperação e simulação 0→morte com seed fixa. */
class InjuryRulesTest {

    @Test
    fun `penalidade de choque limita em -4 e usa PVInicial-10 acima de 20 PV`() {
        // PV Inicial < 20: -1 por PV perdido, teto -4.
        assertEquals(0, InjuryRules.penalidadeChoque(0, 10))
        assertEquals(-3, InjuryRules.penalidadeChoque(3, 10))
        assertEquals(-4, InjuryRules.penalidadeChoque(4, 10))
        assertEquals(-4, InjuryRules.penalidadeChoque(9, 10)) // cap
        // PV Inicial >= 20: -1 a cada PVInicial/10 perdidos. Ex.: 30 PV -> unidade 3.
        assertEquals(-2, InjuryRules.penalidadeChoque(6, 30))   // 6/3 = 2
        assertEquals(-1, InjuryRules.penalidadeChoque(5, 30))   // 5/3 = 1 (arred. baixo)
        assertEquals(-4, InjuryRules.penalidadeChoque(100, 30)) // teto -4
    }

    @Test
    fun `ferimento grave e mais da metade do PV`() {
        assertTrue(InjuryRules.ehFerimentoGrave(6, 10))   // 12 > 10
        assertFalse(InjuryRules.ehFerimentoGrave(5, 10))  // 10 não é > 10
        assertTrue(InjuryRules.ehFerimentoGrave(5, 9))    // 10 > 9
    }

    @Test
    fun `recuperacao de atordoamento depende do HT`() {
        assertTrue(InjuryRules.recuperaAtordoamento(18, Random(1)))  // 3d6<=18 sempre
        assertFalse(InjuryRules.recuperaAtordoamento(2, Random(1)))  // 3d6>=3 sempre falha
    }

    @Test
    fun `morte automatica a partir de menos 5x PV`() {
        val r = InjuryRules.aplicarGolpe(pvAntes = 0, pvMax = 10, ht = 12, dano = 100, random = Random(0))
        assertEquals(InjuryRules.EfeitoFerimento.MORTO, r.efeito)
        assertTrue(r.logs.any { it.contains("morte automática") })
    }

    @Test
    fun `simulacao 0 ate a morte com log e seed fixa`() {
        val c = Combatente(
            id = "vitima", nome = "Vítima", dx = 11, velocidadeBasica = 5.0,
            deslocamento = 5, pvMax = 10, pvAtual = 10
        )
        val ht = 10
        val random = Random(7)
        val logAcumulado = mutableListOf<String>()
        var turnos = 0
        // Golpes de 6 PV por turno até morrer (ou teto de segurança).
        while (c.vivo && turnos < 30) {
            turnos++
            val r = InjuryRules.ferir(c, dano = 6, ht = ht, random = random)
            logAcumulado += "Turno $turnos: " + r.logs.joinToString(" | ")
        }
        // Se o azar não matou em 30 turnos, golpe garantidamente letal.
        if (c.vivo) {
            val r = InjuryRules.ferir(c, dano = 100, ht = ht, random = random)
            logAcumulado += "Turno final: " + r.logs.joinToString(" | ")
        }
        assertFalse("o combatente deveria estar morto/fora de combate", c.vivo)
        assertTrue("deve haver log de cada turno", logAcumulado.size >= 3)
        assertTrue("deve registrar cheque(s) de HT", logAcumulado.any { it.contains("HT") })
    }

    // Lote PONTE-2: sangramento (MB p420 / AM p138).
    private fun vitima(pv: Int = 20) = Combatente(id = "v", nome = "V", dx = 11, velocidadeBasica = 5.0,
        deslocamento = 5, pvMax = pv, pvAtual = pv)

    @Test
    fun `classificarSangramento distingue tipo e local`() {
        assertEquals(null, InjuryRules.classificarSangramento(DanoTipo.CONT, LocalAtaque.TORSO)) // contusão não sangra
        assertEquals(0 to 60, InjuryRules.classificarSangramento(DanoTipo.CORT, LocalAtaque.BRACO))
        assertEquals(4 to 30, InjuryRules.classificarSangramento(DanoTipo.PERF, LocalAtaque.VITAIS))
        assertEquals(2 to 30, InjuryRules.classificarSangramento(DanoTipo.CORT, LocalAtaque.PESCOCO))
    }

    @Test
    fun `ferir por corte marca SANGRANDO e contusao ou sem-tipo nao marcam`() {
        val r = Random(1)
        val cCorte = vitima(); InjuryRules.ferir(cCorte, 5, 12, r, tipo = DanoTipo.CORT, local = LocalAtaque.TORSO)
        assertTrue(cCorte.sangramentoAtivo); assertTrue(Condicao.SANGRANDO in cCorte.condicoes)
        val cCont = vitima(); InjuryRules.ferir(cCont, 5, 12, r, tipo = DanoTipo.CONT, local = LocalAtaque.TORSO)
        assertFalse(cCont.sangramentoAtivo)
        val cSemTipo = vitima(); InjuryRules.ferir(cSemTipo, 5, 12, r) // call-site antigo: sem regressão
        assertFalse(cSemTipo.sangramentoAtivo)
    }

    @Test
    fun `tickSangramento perde PV com HT baixo e estancar limpa o estado`() {
        val c = vitima()
        InjuryRules.ferir(c, 5, 8, Random(1), tipo = DanoTipo.CORT, local = LocalAtaque.TORSO)
        assertTrue(c.sangramentoAtivo)
        // HT 3 (htEf ~2): quase todo teste falha e perde PV. Re-ativa o sangramento (sem novo dano) para garantir.
        var perdeu = false
        repeat(8) { i ->
            if (!c.sangramentoAtivo) { c.sangramentoAtivo = true; c.condicoes.add(Condicao.SANGRANDO); c.sangramentoLesaoPV = 5; c.sangramentoTestesLimpos = 0 }
            val pv = c.pvAtual
            InjuryRules.tickSangramento(c, 3, Random(i.toLong() + 1))
            if (c.pvAtual < pv) perdeu = true
        }
        assertTrue("HT baixo deve causar perda de PV por sangramento em algum tick", perdeu)
        assertTrue(InjuryRules.estancarSangramento(c))
        assertFalse(c.sangramentoAtivo); assertFalse(Condicao.SANGRANDO in c.condicoes)
    }

    @Test
    fun `sangramento com HT altissimo ainda falha em 17 ou 18 (3d6 nunca e sucesso automatico)`() {
        // HT 30: nenhum teste "normal" falha, mas 17/18 em 3d6 são SEMPRE falha → o ferido ainda pode sangrar.
        val c = vitima(pv = 50)
        var perdeu = false
        repeat(500) { i ->
            if (!c.sangramentoAtivo) { c.sangramentoAtivo = true; c.condicoes.add(Condicao.SANGRANDO) }
            val pv = c.pvAtual
            InjuryRules.tickSangramento(c, 30, Random(i.toLong() + 1))
            if (c.pvAtual < pv) perdeu = true
        }
        assertTrue("um 17/18 em 3d6 deve fazer até HT 30 sangrar em algum dos 500 ticks", perdeu)
    }
}
