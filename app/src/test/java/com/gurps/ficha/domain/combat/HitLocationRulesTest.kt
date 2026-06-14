package com.gurps.ficha.domain.combat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lote 361 (B3): paridade com a calculadora da Mesa Virtual (applySmartDmg).
 * Cada caso traz o gabarito calculado pela MESMA fórmula do JS no comentário.
 */
class HitLocationRulesTest {

    private fun pv(dano: Int, tipo: DanoTipo, local: LocalAtaque, rd: Int, pvMax: Int = 10) =
        HitLocationRules.aplicarDano(pvMax, dano, tipo, local, rd).pvSubtrair

    @Test
    fun `paridade com a Mesa Virtual`() {
        assertEquals(6, pv(8, DanoTipo.CONT, LocalAtaque.TORSO, 2))     // 1) pen 6 ×1 = 6
        assertEquals(9, pv(8, DanoTipo.CORT, LocalAtaque.TORSO, 2))     // 2) pen 6 ×1.5 = 9
        assertEquals(16, pv(8, DanoTipo.PERF, LocalAtaque.TORSO, 0))    // 3) pen 8 ×2 = 16
        assertEquals(24, pv(8, DanoTipo.CONT, LocalAtaque.CRANIO, 0))   // 4) rd 0+2; pen 6 ×4 = 24
        assertEquals(32, pv(10, DanoTipo.PI, LocalAtaque.CRANIO, 0))    // 5) rd 2; pen 8 ×4 = 32
        assertEquals(24, pv(8, DanoTipo.PERF, LocalAtaque.VITAIS, 0))   // 6) pen 8 ×3 = 24
        assertEquals(12, pv(8, DanoTipo.CORT, LocalAtaque.VITAIS, 0))   // 7) corte NÃO ganha ×3: pen 8 ×1.5 = 12
        assertEquals(18, pv(6, DanoTipo.PI_MAIS, LocalAtaque.VITAIS, 0))// 8) pen 6 ×3 = 18
        assertEquals(5, pv(10, DanoTipo.CORT, LocalAtaque.BRACO, 0))    // 9) 10×1.5=15 -> limite ceil(10*0.5)=5
        assertEquals(5, pv(8, DanoTipo.CONT, LocalAtaque.PERNA, 0))     // 10) 8 -> limite 5
        assertEquals(4, pv(9, DanoTipo.PI, LocalAtaque.MAO, 0))         // 11) 9 -> limite ceil(10*0.33)=4
        assertEquals(4, pv(4, DanoTipo.CORT, LocalAtaque.PE, 0))        // 12) 4×1.5=6 -> limite 4
        assertEquals(3, pv(8, DanoTipo.PI_MENOS, LocalAtaque.TORSO, 2)) // 13) pen 6 ×0.5 = 3
        assertEquals(0, pv(2, DanoTipo.CONT, LocalAtaque.TORSO, 5))     // 14) pen 0 = 0
        assertEquals(3, pv(6, DanoTipo.PI_MENOS, LocalAtaque.BRACO, 0)) // 15) 6×0.5=3 < limite 5 (sem incapacitar)
    }

    @Test
    fun `incapacitacao de membro sinalizada`() {
        val r = HitLocationRules.aplicarDano(10, 10, DanoTipo.CORT, LocalAtaque.BRACO, 0)
        assertEquals(5, r.pvSubtrair)
        assertTrue(r.incapacitouMembro)

        val r2 = HitLocationRules.aplicarDano(10, 6, DanoTipo.PI_MENOS, LocalAtaque.BRACO, 0)
        assertFalse(r2.incapacitouMembro) // 3 PV, abaixo do limite
    }

    @Test
    fun `multiplicador exposto bate com os overrides`() {
        assertEquals(4.0, HitLocationRules.multiplicador(DanoTipo.CONT, LocalAtaque.CRANIO), 0.001)
        assertEquals(3.0, HitLocationRules.multiplicador(DanoTipo.PERF, LocalAtaque.VITAIS), 0.001)
        assertEquals(1.5, HitLocationRules.multiplicador(DanoTipo.CORT, LocalAtaque.VITAIS), 0.001)
        assertEquals(1.5, HitLocationRules.multiplicador(DanoTipo.CORT, LocalAtaque.TORSO), 0.001)
    }
}
