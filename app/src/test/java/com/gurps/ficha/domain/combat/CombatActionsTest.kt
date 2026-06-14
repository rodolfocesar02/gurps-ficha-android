package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.roll.CriticoRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Lote 360 (B2): matriz de NH efetivo (gabarito à mão) + avaliação de rolagem.
 * Cada caso de calcularNH traz a conta no comentário.
 */
class CombatActionsTest {

    private fun nh(
        base: Int, manobra: Manobra = Manobra.ATAQUE,
        postura: Postura = Postura.EM_PE, local: LocalAtaque = LocalAtaque.TORSO,
        vis: Visibilidade = Visibilidade.NORMAL,
        at: AtaqueTotalModo = AtaqueTotalModo.DETERMINADO, aDist: Boolean = false
    ) = CombatActions.calcularNH(base, manobra, postura, local, vis, at, aDist).nhEfetivo

    @Test
    fun `matriz de NH efetivo`() {
        assertEquals(14, nh(14))                                                   // 1) base 14 = 14
        assertEquals(18, nh(14, Manobra.ATAQUE_TOTAL, at = AtaqueTotalModo.DETERMINADO)) // 2) 14+4=18
        assertEquals(14, nh(14, Manobra.ATAQUE_TOTAL, at = AtaqueTotalModo.FORTE)) // 3) 14+0=14
        assertEquals(10, nh(14, Manobra.MOVER_E_ATACAR))                           // 4) 14-4=10 (CaC)
        assertEquals(11, nh(14, local = LocalAtaque.VITAIS))                       // 5) 14-3=11
        assertEquals(7, nh(14, local = LocalAtaque.CRANIO))                        // 6) 14-7=7
        assertEquals(10, nh(14, postura = Postura.DEITADO))                        // 7) 14-4=10
        assertEquals(4, nh(14, vis = Visibilidade.ESCURIDAO_TOTAL))               // 8) 14-10=4
        assertEquals(5, nh(14, local = LocalAtaque.VITAIS, vis = Visibilidade.ESCURIDAO_PARCIAL)) // 9) 14-3-6=5
        assertEquals(1, nh(12, postura = Postura.AGACHADO, local = LocalAtaque.OLHO)) // 10) 12-2-9=1
        assertEquals(14, nh(14, Manobra.ATAQUE_TOTAL, local = LocalAtaque.VITAIS, vis = Visibilidade.NEVOA_LEVE, at = AtaqueTotalModo.DETERMINADO)) // 11) 14+4-3-1=14
        assertEquals(9, nh(10, Manobra.MOVER_E_ATACAR, aDist = true))              // 12) 10 -> teto 9
        assertEquals(8, nh(8, Manobra.MOVER_E_ATACAR, aDist = true))               // 13) 8 (sem teto)
        assertEquals(9, nh(14, Manobra.MOVER_E_ATACAR, local = LocalAtaque.VITAIS, aDist = true)) // 14) 14-3=11 -> teto 9
    }

    @Test
    fun `teto do mover-e-atacar a distancia marca a flag`() {
        val c = CombatActions.calcularNH(10, Manobra.MOVER_E_ATACAR, aDistancia = true)
        assertTrue(c.limitadoPorTeto)
        val c2 = CombatActions.calcularNH(8, Manobra.MOVER_E_ATACAR, aDistancia = true)
        assertFalse(c2.limitadoPorTeto)
    }

    @Test
    fun `avaliar rolagem - acerto, falha e criticos`() {
        // nh 9, rolou 8 -> acerto margem 1, normal
        CombatActions.avaliarRolagem(9, 8).let {
            assertEquals(CombatActions.ResultadoAcerto.ACERTO, it.first)
            assertEquals(1, it.second)
            assertEquals(CriticoRules.ResultadoCritico.NORMAL, it.third)
        }
        // nh 9, rolou 10 -> falha margem 1
        CombatActions.avaliarRolagem(9, 10).let {
            assertEquals(CombatActions.ResultadoAcerto.FALHA, it.first)
            assertEquals(1, it.second)
        }
        // nh 9, rolou 4 -> decisivo (3-4 sempre), acerto
        assertEquals(CriticoRules.ResultadoCritico.DECISIVO, CombatActions.avaliarRolagem(9, 4).third)
        // nh 16, rolou 6 -> decisivo (6 @ NH>=16)
        assertEquals(CriticoRules.ResultadoCritico.DECISIVO, CombatActions.avaliarRolagem(16, 6).third)
        // nh 9, rolou 17 -> falha crítica (17 @ NH<=15)
        CombatActions.avaliarRolagem(9, 17).let {
            assertEquals(CombatActions.ResultadoAcerto.FALHA, it.first)
            assertEquals(CriticoRules.ResultadoCritico.FALHA_CRITICA, it.third)
        }
        // nh 10, rolou 18 -> falha crítica sempre
        assertEquals(CriticoRules.ResultadoCritico.FALHA_CRITICA, CombatActions.avaliarRolagem(10, 18).third)
    }

    @Test
    fun `relatorio legivel e flags de manobra`() {
        // Ataque Total: sem defesa ativa neste turno.
        val rAtTotal = CombatActions.resolverAtaque(14, Manobra.ATAQUE_TOTAL, random = Random(1))
        assertTrue(rAtTotal.atacanteSemDefesaAtiva)
        assertFalse(rAtTotal.semApararDepois)

        // Mover e Atacar: só esquiva depois (sem aparar).
        val rMover = CombatActions.resolverAtaque(14, Manobra.MOVER_E_ATACAR, random = Random(1))
        assertTrue(rMover.semApararDepois)

        // Texto legível no formato do aceite ("NH ... = X; rolou Y: acerto/falha, margem Z").
        val r = CombatActions.resolverAtaque(
            14, Manobra.ATAQUE, local = LocalAtaque.VITAIS, visibilidade = Visibilidade.ESCURIDAO_PARCIAL, random = Random(7)
        )
        assertTrue(r.texto.startsWith("NH 14"))
        assertTrue(r.texto.contains("-3 vitais"))
        assertTrue(r.texto.contains("= 5"))
        assertEquals(r.soma, r.dados.sum())

        // Determinismo por seed.
        val a = CombatActions.resolverAtaque(12, Manobra.ATAQUE, random = Random(99))
        val b = CombatActions.resolverAtaque(12, Manobra.ATAQUE, random = Random(99))
        assertEquals(a.dados, b.dados)
    }
}
