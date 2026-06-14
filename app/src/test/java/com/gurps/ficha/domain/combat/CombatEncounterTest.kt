package com.gurps.ficha.domain.combat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote 359 (B1): ordem de iniciativa, legalidade de manobras e resumo determinístico. */
class CombatEncounterTest {

    private fun comb(
        id: String, vel: Double, dx: Int, heroi: Boolean = false,
        postura: Postura = Postura.EM_PE, condicoes: Set<Condicao> = emptySet(), pvMax: Int = 10
    ) = Combatente(
        id = id, nome = id, ehHeroi = heroi, dx = dx, velocidadeBasica = vel,
        deslocamento = vel.toInt(), pvMax = pvMax, postura = postura,
        condicoes = condicoes.toMutableSet()
    )

    @Test
    fun `ordem por velocidade com dois empates e desempate deterministico`() {
        // g3: vel 6.0/dx14 (vence o empate de velocidade por DX) | g1,g2: vel 6.0/dx12 (empate TOTAL -> seed)
        // heroi: vel 5.0 (mais lento, age por ultimo)
        val cs = listOf(
            comb("heroi", 5.0, 11, heroi = true),
            comb("g1", 6.0, 12),
            comb("g2", 6.0, 12),
            comb("g3", 6.0, 14)
        )
        val enc = CombatEncounter(cs, seed = 42L)

        assertEquals("g3", enc.ordemTurnos.first())        // maior DX entre os de vel 6.0
        assertEquals("heroi", enc.ordemTurnos.last())      // menor velocidade
        assertEquals(setOf("g1", "g2"), enc.ordemTurnos.subList(1, 3).toSet()) // empate total no meio

        // Determinismo: mesma seed -> mesma ordem exata.
        val enc2 = CombatEncounter(cs, seed = 42L)
        assertEquals(enc.ordemTurnos, enc2.ordemTurnos)
    }

    @Test
    fun `proximoTurno percorre a ordem e incrementa rodada ao dar a volta`() {
        val cs = listOf(comb("a", 6.0, 12), comb("b", 5.0, 10))
        val enc = CombatEncounter(cs, seed = 1L)
        assertEquals(1, enc.rodadaAtual)
        val primeiro = enc.combatenteAtual.id
        enc.proximoTurno()
        assertEquals(1, enc.rodadaAtual)
        enc.proximoTurno() // volta ao primeiro -> rodada 2
        assertEquals(2, enc.rodadaAtual)
        assertEquals(primeiro, enc.combatenteAtual.id)
    }

    @Test
    fun `manobras legais em 6 estados`() {
        // a) normal ENGAJADO (dist 1): pode atacar corpo-a-corpo
        run {
            val g = comb("g", 6.0, 12)
            val enc = CombatEncounter(listOf(g), mapOf("g" to 1), seed = 0L)
            val m = enc.manobrasLegais(g)
            assertTrue(Manobra.ATAQUE in m)
            assertTrue(Manobra.ATAQUE_TOTAL in m)
            assertTrue(Manobra.MOVER_E_ATACAR in m)
            assertTrue(Manobra.MOVER in m)
        }
        // b) normal NÃO-engajado (dist 5): sem ataque corpo-a-corpo
        run {
            val g = comb("g", 6.0, 12)
            val enc = CombatEncounter(listOf(g), mapOf("g" to 5), seed = 0L)
            val m = enc.manobrasLegais(g)
            assertFalse(Manobra.ATAQUE in m)
            assertFalse(Manobra.ATAQUE_TOTAL in m)
            assertTrue(Manobra.MOVER_E_ATACAR in m)
            assertTrue(Manobra.MOVER in m)
        }
        // c) ATORDOADO: só Defesa Total e Não Fazer Nada
        run {
            val g = comb("g", 6.0, 12, condicoes = setOf(Condicao.ATORDOADO))
            val enc = CombatEncounter(listOf(g), mapOf("g" to 1), seed = 0L)
            assertEquals(listOf(Manobra.DEFESA_TOTAL, Manobra.NAO_FAZER_NADA), enc.manobrasLegais(g))
        }
        // d) CAÍDO engajado: ataca, mas sem Ataque Total nem Mover e Atacar; pode Mudar de Postura
        run {
            val g = comb("g", 6.0, 12, condicoes = setOf(Condicao.CAIDO))
            val enc = CombatEncounter(listOf(g), mapOf("g" to 1), seed = 0L)
            val m = enc.manobrasLegais(g)
            assertTrue(Manobra.ATAQUE in m)
            assertFalse(Manobra.ATAQUE_TOTAL in m)
            assertFalse(Manobra.MOVER_E_ATACAR in m)
            assertTrue(Manobra.MUDAR_POSTURA in m)
        }
        // e) INCONSCIENTE: nenhuma manobra
        run {
            val g = comb("g", 6.0, 12, condicoes = setOf(Condicao.INCONSCIENTE))
            val enc = CombatEncounter(listOf(g), mapOf("g" to 1), seed = 0L)
            assertTrue(enc.manobrasLegais(g).isEmpty())
        }
        // f) CAÍDO não-engajado: sem ataques; Mudar de Postura e Mover disponíveis
        run {
            val g = comb("g", 6.0, 12, postura = Postura.DEITADO)
            val enc = CombatEncounter(listOf(g), mapOf("g" to 5), seed = 0L)
            val m = enc.manobrasLegais(g)
            assertFalse(Manobra.ATAQUE in m)
            assertFalse(Manobra.ATAQUE_TOTAL in m)
            assertFalse(Manobra.MOVER_E_ATACAR in m)
            assertTrue(Manobra.MUDAR_POSTURA in m)
            assertTrue(Manobra.MOVER in m)
        }
    }

    @Test
    fun `estadoResumo e deterministico e factual`() {
        val heroi = comb("Jack", 5.0, 11, heroi = true, pvMax = 12).apply { pvAtual = 9 }
        val goblin = comb("Goblin", 6.0, 12, condicoes = setOf(Condicao.ATORDOADO))
        val enc = CombatEncounter(listOf(heroi, goblin), mapOf("Goblin" to 3), seed = 7L)

        val r1 = enc.estadoResumo()
        val r2 = enc.estadoResumo()
        assertEquals(r1, r2)                                  // determinístico
        assertTrue(r1.contains("Rodada 1"))
        assertTrue(r1.contains("Jack"))
        assertTrue(r1.contains("PV 9/12"))
        assertTrue(r1.contains("3m do herói"))
        assertTrue(r1.contains("atordoado"))
    }
}
