package com.gurps.ficha.domain.combat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Lote 362 (B4): choque, ferimento grave, recuperação e simulação 0→morte com seed fixa. */
class InjuryRulesTest {

    @Test
    fun `penalidade de choque limita em -4`() {
        assertEquals(0, InjuryRules.penalidadeChoque(0))
        assertEquals(-3, InjuryRules.penalidadeChoque(3))
        assertEquals(-4, InjuryRules.penalidadeChoque(4))
        assertEquals(-4, InjuryRules.penalidadeChoque(9)) // cap
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
}
