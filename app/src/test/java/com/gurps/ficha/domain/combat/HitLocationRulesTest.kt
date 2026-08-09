package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.rules.DanoTipo
import com.gurps.ficha.domain.rules.ToleranciaFerimentos

import com.gurps.ficha.domain.rules.LocalAtaque

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lote 361 (B3): dano localizado do combate tático.
 *
 * ## 🔴 O gabarito mudou de dono no Lote MB-7b
 *
 * Este arquivo nasceu conferindo **paridade com a calculadora da Mesa Virtual** —
 * cada caso trazia o número que o JS devolvia. O problema é que o JS estava
 * errado no teto do membro, e **o teste gravava o erro**: ele exigia 5 PV onde o
 * livro diz 6, e por isso ninguém percebeu por dois anos.
 *
 * > Se um homem com **10 PV** sofrer 9 pontos de dano no braço direito, ele só
 * > perde **6 PV**. (MB p.421)
 *
 * ⚠️ Agora o gabarito é **o livro**, não o JS. Onde os dois discordam, os casos
 * abaixo estão marcados — e a conta única mora em
 * `FerimentoPorLocalRules.minimoQueIncapacita`, com os dois exemplos trabalhados
 * do Módulo Básico como teste.
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
        // 🔴 9) e 10) MUDARAM no MB-7b: o mínimo que incapacita um membro de
        // quem tem PV 10 é 6, não 5 (MB p.421). O JS dava 5.
        assertEquals(6, pv(10, DanoTipo.CORT, LocalAtaque.BRACO, 0))    // 9) 10×1.5=15 -> teto floor(10/2)+1=6
        assertEquals(6, pv(8, DanoTipo.CONT, LocalAtaque.PERNA, 0))     // 10) 8 -> teto 6
        assertEquals(4, pv(9, DanoTipo.PI, LocalAtaque.MAO, 0))         // 11) 9 -> teto floor(10/3)+1=4
        assertEquals(4, pv(4, DanoTipo.CORT, LocalAtaque.PE, 0))        // 12) 4×1.5=6 -> limite 4
        assertEquals(3, pv(8, DanoTipo.PI_MENOS, LocalAtaque.TORSO, 2)) // 13) pen 6 ×0.5 = 3
        assertEquals(0, pv(2, DanoTipo.CONT, LocalAtaque.TORSO, 5))     // 14) pen 0 = 0
        assertEquals(3, pv(6, DanoTipo.PI_MENOS, LocalAtaque.BRACO, 0)) // 15) 6×0.5=3 < teto 6 (sem incapacitar)
    }

    @Test
    fun `incapacitacao de membro sinalizada`() {
        val r = HitLocationRules.aplicarDano(10, 10, DanoTipo.CORT, LocalAtaque.BRACO, 0)
        assertEquals(6, r.pvSubtrair)
        assertTrue(r.incapacitouMembro)

        val r2 = HitLocationRules.aplicarDano(10, 6, DanoTipo.PI_MENOS, LocalAtaque.BRACO, 0)
        assertFalse(r2.incapacitouMembro) // 3 PV, abaixo do teto
    }

    // ── Lote MB-7b: o off-by-one, com os exemplos do livro ──

    @Test
    fun `🔴 o exemplo do Friedrick — PV 14 perde 8, nao 7`() {
        // MB p.419: "PV/2 é 7. Dano maior que PV/2 é 8 PV, então ele perde apenas
        // 8 PV." O `ceil(14 × 0,5)` da Mesa Virtual devolvia 7.
        val r = HitLocationRules.aplicarDano(14, 11, DanoTipo.CONT, LocalAtaque.BRACO, 0)
        assertEquals(8, r.pvSubtrair)
        assertTrue(r.incapacitouMembro)
    }

    @Test
    fun `🔴 o homem de PV 10 que leva 9 no braco perde 6`() {
        // MB p.421, palavra por palavra.
        assertEquals(6, pv(9, DanoTipo.CONT, LocalAtaque.BRACO, 0))
    }

    @Test
    fun `⚠️ com PV IMPAR as duas contas coincidem — foi assim que o erro sobreviveu`() {
        // PV 11: floor(5,5)+1 = 6 e ceil(5,5) = 6. Iguais. Metade das fichas
        // nunca viu o defeito.
        assertEquals(6, pv(20, DanoTipo.CONT, LocalAtaque.BRACO, 0, pvMax = 11))
        // PV 12: floor(6,0)+1 = 7, e o ceil dava 6.
        assertEquals(7, pv(20, DanoTipo.CONT, LocalAtaque.BRACO, 0, pvMax = 12))
    }

    @Test
    fun `⚠️ extremidade usa um terco de verdade, nao zero virgula 33`() {
        // PV 3, mão: 1/3 de 3 é 1, então o mínimo que incapacita é 2.
        // `ceil(3 × 0,33)` = 1 — o antigo incapacitava com um ponto a menos em
        // todo PV múltiplo de 3.
        assertEquals(2, pv(9, DanoTipo.CONT, LocalAtaque.MAO, 0, pvMax = 3))
        assertEquals(3, pv(9, DanoTipo.CONT, LocalAtaque.PE, 0, pvMax = 6))
    }

    @Test
    fun `⚠️ o olho continua FORA do teto de membro neste motor`() {
        // A regra única sabe cegar (dano acima de PV/10), mas ensinar isso ao
        // combate tático é mudança de comportamento, não correção de conta —
        // ficou fora do MB-7b de propósito. A cegueira funciona no botão PV.
        val r = HitLocationRules.aplicarDano(10, 8, DanoTipo.PERF, LocalAtaque.OLHO, 0)
        assertFalse(r.incapacitouMembro)
        assertEquals("o olho não é limitado por teto de membro aqui", 16, r.pvSubtrair)
    }

    @Test
    fun `⚠️ nenhum golpe em membro passa do teto — varredura`() {
        listOf(LocalAtaque.BRACO, LocalAtaque.PERNA, LocalAtaque.MAO, LocalAtaque.PE).forEach { local ->
            (1..40).forEach { pvMax ->
                val teto = com.gurps.ficha.domain.rules.FerimentoPorLocalRules
                    .minimoQueIncapacita(local, pvMax)!!
                DanoTipo.entries.forEach { tipo ->
                    (1..60).forEach { dano ->
                        val r = HitLocationRules.aplicarDano(pvMax, dano, tipo, local, 0)
                        assertTrue(
                            "$local PV $pvMax $tipo dano $dano -> ${r.pvSubtrair} (teto $teto)",
                            r.pvSubtrair <= teto
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `multiplicador exposto bate com os overrides`() {
        assertEquals(4.0, HitLocationRules.multiplicador(DanoTipo.CONT, LocalAtaque.CRANIO), 0.001)
        assertEquals(3.0, HitLocationRules.multiplicador(DanoTipo.PERF, LocalAtaque.VITAIS), 0.001)
        assertEquals(1.5, HitLocationRules.multiplicador(DanoTipo.CORT, LocalAtaque.VITAIS), 0.001)
        assertEquals(1.5, HitLocationRules.multiplicador(DanoTipo.CORT, LocalAtaque.TORSO), 0.001)
    }

    // ── Lote 385: Tolerância a Ferimentos (MB p.381) ──

    @Test
    fun `Nao-Vivo reduz dano perfurante (zumbi-esqueleto)`() {
        // pi contra Não-Vivo = ×1/3: 9 penetrante → 3 PV (o vivo levaria 9).
        assertEquals(3, HitLocationRules.aplicarDano(20, 9, DanoTipo.PI, LocalAtaque.TORSO, 0, ToleranciaFerimentos.NAO_VIVO).pvSubtrair)
        assertEquals(9, HitLocationRules.aplicarDano(20, 9, DanoTipo.PI, LocalAtaque.TORSO, 0).pvSubtrair)
        // perfuração (perf) = ×1 mesmo no Não-Vivo (lança/flecha ainda doem).
        assertEquals(8, HitLocationRules.aplicarDano(20, 8, DanoTipo.PERF, LocalAtaque.TORSO, 0, ToleranciaFerimentos.NAO_VIVO).pvSubtrair)
        // Não-Vivo não tem vitais: pi nos vitais NÃO ganha ×3 (vivo ganharia).
        assertEquals(3, HitLocationRules.aplicarDano(20, 9, DanoTipo.PI, LocalAtaque.VITAIS, 0, ToleranciaFerimentos.NAO_VIVO).pvSubtrair)
    }

    @Test
    fun `Homogeneo e Difuso limitam o dano`() {
        // Homogêneo: pi ×0.2 → floor(10*0.2)=2.
        assertEquals(2, HitLocationRules.aplicarDano(50, 10, DanoTipo.PI, LocalAtaque.TORSO, 0, ToleranciaFerimentos.HOMOGENEO).pvSubtrair)
        // Difuso: pi/perf nunca passam de 1 PV; os demais, de 2 PV.
        assertEquals(1, HitLocationRules.aplicarDano(50, 30, DanoTipo.PI, LocalAtaque.TORSO, 0, ToleranciaFerimentos.DIFUSO).pvSubtrair)
        assertEquals(2, HitLocationRules.aplicarDano(50, 30, DanoTipo.CONT, LocalAtaque.TORSO, 0, ToleranciaFerimentos.DIFUSO).pvSubtrair)
    }
}
