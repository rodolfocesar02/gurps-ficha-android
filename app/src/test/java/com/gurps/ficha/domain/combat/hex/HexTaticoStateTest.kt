package com.gurps.ficha.domain.combat.hex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lotes HEX-9b/TOK-1: hexes válidos de movimento + aviso transitório do estado tático demo. */
class HexTaticoStateTest {

    @Test
    fun `sem token selecionado hexesValidosParaMover e vazio`() {
        val estado = HexTaticoState.demoInicial()
        assertTrue(estado.hexesValidosParaMover.isEmpty())
    }

    @Test
    fun `token selecionado sem vizinhos ocupados devolve os 6 vizinhos`() {
        val estado = HexTaticoState.demoInicial()
            .copy(tokenSelecionadoId = "heroi")
        // Herói em (0,0); goblin em (3,0) — longe, não ocupa vizinho.
        assertEquals(6, estado.hexesValidosParaMover.size)
        assertTrue(HexCoord(1, 0) in estado.hexesValidosParaMover)
    }

    @Test
    fun `vizinho ocupado por outro token sai dos validos`() {
        val estado = HexTaticoState(
            tokens = listOf(
                TokenDemo("heroi", "Herói", HexCoord.ORIGEM, ehHeroi = true),
                TokenDemo("goblin", "Goblin", HexCoord(1, 0), ehHeroi = false)
            ),
            tokenSelecionadoId = "heroi"
        )
        assertEquals(5, estado.hexesValidosParaMover.size)
        assertTrue(HexCoord(1, 0) !in estado.hexesValidosParaMover)
    }

    @Test
    fun `tocar hex distante com token selecionado seta aviso muito longe`() {
        val estado = HexTaticoState.demoInicial().copy(tokenSelecionadoId = "heroi")
        val depois = estado.aoTocarHex(HexCoord(5, 5))
        assertTrue(depois.ultimoAviso?.contains("Muito longe") == true)
        // Herói NÃO se moveu.
        assertEquals(HexCoord.ORIGEM, depois.tokens.first { it.id == "heroi" }.posicao)
    }

    @Test
    fun `mover valido limpa o aviso`() {
        val comAviso = HexTaticoState.demoInicial()
            .copy(tokenSelecionadoId = "heroi", ultimoAviso = "Muito longe — só hex vizinho (1 passo)")
        val depois = comAviso.aoTocarHex(HexCoord(1, 0))
        assertNull(depois.ultimoAviso)
        assertEquals(HexCoord(1, 0), depois.tokens.first { it.id == "heroi" }.posicao)
    }

    @Test
    fun `tocar num token seleciona e limpa aviso`() {
        val estado = HexTaticoState.demoInicial().copy(ultimoAviso = "qualquer")
        val depois = estado.aoTocarHex(HexCoord(3, 0)) // goblin do demo
        assertEquals("inimigo1", depois.tokenSelecionadoId)
        assertNull(depois.ultimoAviso)
    }

    @Test
    fun `mover atualiza facing na direcao do movimento`() {
        val estado = HexTaticoState.demoInicial().copy(tokenSelecionadoId = "heroi")
        val depois = estado.aoTocarHex(HexCoord(0, 1)) // SUDESTE
        assertEquals(Direcao.SUDESTE, depois.tokens.first { it.id == "heroi" }.facing)
    }
}
