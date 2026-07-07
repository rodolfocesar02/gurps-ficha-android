package com.gurps.ficha.domain.combat.hex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/** Lote HEX-3: estado posicional real (com IDs de combatente). Kotlin puro. */
class HexCombatStateTest {

    @Test
    fun `setupInicial coloca heroi na origem e goblin a 3 hexes de distancia`() {
        val s = HexCombatState.setupInicial()
        assertEquals(3, s.distanciaHex("heroi", "goblin_1"))
        assertEquals(HexCoord.ORIGEM, s.posicoes.first { it.id == "heroi" }.posicao)
    }

    @Test
    fun `distanciaHex devolve null quando qualquer id nao existe`() {
        val s = HexCombatState.setupInicial()
        assertNull(s.distanciaHex("heroi", "fantasma"))
        assertNull(s.distanciaHex("x", "y"))
    }

    @Test
    fun `mover heroi adjacente aproxima do goblin em 1 hex`() {
        val s0 = HexCombatState.setupInicial()
        assertEquals(3, s0.distanciaHex("heroi", "goblin_1"))
        val s1 = s0.mover("heroi", HexCoord(1, 0)) // 1 hex ao leste
        assertEquals(2, s1.distanciaHex("heroi", "goblin_1"))
    }

    @Test
    fun `mover para hex nao-adjacente devolve o MESMO objeto`() {
        val s = HexCombatState.setupInicial()
        val ilegal = s.mover("heroi", HexCoord(5, 5))
        assertSame(s, ilegal)
        val legal = s.mover("heroi", HexCoord(1, 0))
        assertNotSame(s, legal)
    }

    @Test
    fun `mover atualiza o facing na direcao do movimento`() {
        val s0 = HexCombatState.setupInicial()
        val s1 = s0.mover("heroi", HexCoord(0, 1)) // 1 hex ao sudeste
        assertEquals(Direcao.SUDESTE, s1.posicoes.first { it.id == "heroi" }.facing)
    }

    @Test
    fun `mover para hex OCUPADO por outro combatente nao faz nada`() {
        val s0 = HexCombatState(posicoes = listOf(
            PosicaoCombatente("heroi", HexCoord.ORIGEM),
            PosicaoCombatente("goblin", HexCoord(1, 0))
        ))
        val ilegal = s0.mover("heroi", HexCoord(1, 0))
        assertSame(s0, ilegal)
    }
}
