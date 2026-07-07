package com.gurps.ficha.domain.combat.hex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/** Lote HEX-4: portabilidade das manobras do motor (Encontrão/Empurrão/Mover) para posição em hex. */
class HexPortabilidadeTest {

    @Test
    fun `Encontrão força distância 1 - NPC caminha na linha reta até ficar adjacente ao herói`() {
        // Setup: herói (0,0), goblin (3,0). Motor forçou distância = 1 (Encontrão). Espera-se goblin em (1,0).
        val e0 = HexCombatState.setupInicial()
        val e1 = HexPortabilidade.aplicarNovaDistancia(e0, "goblin_1", 1)
        val goblin = e1.posicoes.first { it.id == "goblin_1" }
        assertEquals(HexCoord(1, 0), goblin.posicao)
    }

    @Test
    fun `Empurrão knockback +2m - NPC recua 2 hexes na direção oposta ao herói`() {
        // Setup: herói (0,0), goblin (3,0). Distância atual = 3, alvo = 5. Espera-se goblin em (5,0).
        val e0 = HexCombatState.setupInicial()
        val e1 = HexPortabilidade.aplicarNovaDistancia(e0, "goblin_1", 5)
        val goblin = e1.posicoes.first { it.id == "goblin_1" }
        assertEquals(HexCoord(5, 0), goblin.posicao)
    }

    @Test
    fun `Distância inalterada devolve o mesmo objeto (evita recomposição)`() {
        val e0 = HexCombatState.setupInicial()
        val e1 = HexPortabilidade.aplicarNovaDistancia(e0, "goblin_1", 3)
        assertSame(e0, e1)
    }

    @Test
    fun `NPC ou herói ausentes do estado devolvem o mesmo objeto`() {
        val e0 = HexCombatState.setupInicial()
        val e1 = HexPortabilidade.aplicarNovaDistancia(e0, "fantasma", 1)
        assertSame(e0, e1)
    }

    @Test
    fun `Aproximar até 0 metros não atropela o herói - para em 1 hex`() {
        val e0 = HexCombatState.setupInicial()
        val e1 = HexPortabilidade.aplicarNovaDistancia(e0, "goblin_1", 0)
        val goblin = e1.posicoes.first { it.id == "goblin_1" }
        // Nunca ocupa o hex do herói: para em distância 1.
        assertEquals(HexCoord(1, 0), goblin.posicao)
    }

    @Test
    fun `Aproximar respeita hex ocupado (colisão) - para no último hex livre`() {
        // Setup extra: herói (0,0), goblin_1 (3,0), goblin_2 (2,0) bloqueando (2,0).
        val estado = HexCombatState(posicoes = listOf(
            PosicaoCombatente("heroi", HexCoord.ORIGEM),
            PosicaoCombatente("goblin_1", HexCoord(3, 0)),
            PosicaoCombatente("goblin_2", HexCoord(2, 0))
        ))
        // Tentar aproximar goblin_1 até 1m: (3,0)→(2,0) está ocupado por goblin_2 → para em (3,0) mesmo.
        val depois = HexPortabilidade.aplicarNovaDistancia(estado, "goblin_1", 1)
        val g1 = depois.posicoes.first { it.id == "goblin_1" }
        assertEquals("bloqueado por colisão fica onde estava", HexCoord(3, 0), g1.posicao)
    }

    @Test
    fun `NPC afastar não muda o facing (o knockback empurra sem virar o inimigo)`() {
        val e0 = HexCombatState.setupInicial()
        val facingAntes = e0.posicoes.first { it.id == "goblin_1" }.facing
        val e1 = HexPortabilidade.aplicarNovaDistancia(e0, "goblin_1", 5)
        val facingDepois = e1.posicoes.first { it.id == "goblin_1" }.facing
        assertEquals(facingAntes, facingDepois)
    }
}
