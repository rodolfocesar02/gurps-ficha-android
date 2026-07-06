package com.gurps.ficha.domain.combat.hex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote HEX-2: lógica de toque/mover do canvas tático DEMO. Kotlin puro. */
class HexTaticoDemoTest {

    @Test
    fun `estado inicial tem heroi e goblin com raio 7`() {
        val s = HexTaticoState.demoInicial()
        assertEquals(2, s.tokens.size)
        assertTrue(s.tokens.any { it.ehHeroi && it.posicao == HexCoord.ORIGEM })
        assertTrue(s.tokens.any { !it.ehHeroi && it.posicao == HexCoord(3, 0) })
        assertNull(s.hexSelecionado); assertNull(s.tokenSelecionadoId)
        assertEquals(HexGrid.range(HexCoord.ORIGEM, 7).size, s.hexesVisiveis.size)
    }

    @Test
    fun `tocar num token seleciona o token e o hex`() {
        val s = HexTaticoState.demoInicial().aoTocarHex(HexCoord.ORIGEM)
        assertEquals("heroi", s.tokenSelecionadoId)
        assertEquals(HexCoord.ORIGEM, s.hexSelecionado)
    }

    @Test
    fun `com heroi selecionado, tocar num vizinho move o heroi para lá e atualiza o facing`() {
        val s0 = HexTaticoState.demoInicial().aoTocarHex(HexCoord.ORIGEM) // seleciona herói
        val destino = HexCoord(1, 0) // vizinho leste
        val s1 = s0.aoTocarHex(destino)
        val heroi = s1.tokens.first { it.ehHeroi }
        assertEquals(destino, heroi.posicao)
        assertEquals(Direcao.LESTE, heroi.facing)
        assertEquals(destino, s1.hexSelecionado)
    }

    @Test
    fun `mover para hex NÃO adjacente não faz nada (só destaca o hex)`() {
        val s0 = HexTaticoState.demoInicial().aoTocarHex(HexCoord.ORIGEM)
        val longe = HexCoord(3, 0) // 3 hexes de distância, ocupado pelo goblin de qualquer forma
        val s1 = s0.aoTocarHex(longe)
        // Toquei num hex com token → seleciona ELE em vez de mover (o herói não teleporta).
        assertEquals("inimigo1", s1.tokenSelecionadoId)
        assertEquals(HexCoord.ORIGEM, s1.tokens.first { it.ehHeroi }.posicao) // herói ficou onde estava
    }

    @Test
    fun `mover para hex OCUPADO por outro token não faz nada (colisão)`() {
        // Herói selecionado; goblin bloqueando (1,0) manualmente.
        val estado = HexTaticoState(tokens = listOf(
            TokenDemo("heroi", "H", HexCoord.ORIGEM, ehHeroi = true),
            TokenDemo("g", "G", HexCoord(1, 0), ehHeroi = false)
        ), tokenSelecionadoId = "heroi")
        val depois = estado.aoTocarHex(HexCoord(1, 0))
        assertEquals(HexCoord.ORIGEM, depois.tokens.first { it.id == "heroi" }.posicao)
        assertEquals("g", depois.tokenSelecionadoId) // toque num token o seleciona (mesma regra)
    }

    @Test
    fun `tocar em hex vazio SEM token selecionado só destaca o hex`() {
        val hexVazio = HexCoord(2, 2)
        val s = HexTaticoState.demoInicial().aoTocarHex(hexVazio)
        assertEquals(hexVazio, s.hexSelecionado)
        assertNull(s.tokenSelecionadoId)
    }

    @Test
    fun `mover devolve o MESMO objeto quando movimento é ilegal (evita recomposição desnecessária)`() {
        val s = HexTaticoState.demoInicial()
        val ilegal = s.mover("heroi", HexCoord(5, 5)) // muito longe
        assertSame(s, ilegal)
        val legal = s.mover("heroi", HexCoord(1, 0))
        assertNotSame(s, legal)
    }
}
