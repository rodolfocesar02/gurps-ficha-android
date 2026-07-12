package com.gurps.ficha.domain.combat.hex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote TOK-4: montagem do estado tático real + movimento do herói pelo grid. Kotlin puro. */
class HexSetupTest {

    // ─── setupDoEncontro ────────────────────────────────────────

    @Test
    fun `heroi nasce na origem olhando leste`() {
        val e = HexSetup.setupDoEncontro(listOf("goblin_1"), distanciaM = 4)
        val heroi = e.posicoes.first { it.id == "heroi" }
        assertEquals(HexCoord.ORIGEM, heroi.posicao)
        assertEquals(Direcao.LESTE, heroi.facing)
    }

    @Test
    fun `inimigo unico nasce a distanciaM hexes olhando o heroi`() {
        val e = HexSetup.setupDoEncontro(listOf("goblin_1"), distanciaM = 4)
        val goblin = e.posicoes.first { it.id == "goblin_1" }
        assertEquals(4, goblin.posicao.distancia(HexCoord.ORIGEM))
        assertEquals(Direcao.OESTE, goblin.facing) // primeiro spawn é a LESTE → olha OESTE
    }

    @Test
    fun `tres inimigos nascem em posicoes DISTINTAS todos a distanciaM`() {
        val e = HexSetup.setupDoEncontro(listOf("a", "b", "c"), distanciaM = 3)
        val posicoes = e.posicoes.filter { it.id != "heroi" }.map { it.posicao }
        assertEquals(3, posicoes.toSet().size) // sem sobreposição
        posicoes.forEach { assertEquals(3, it.distancia(HexCoord.ORIGEM)) }
    }

    @Test
    fun `setimo inimigo sobe um anel (todas as 6 direcoes ocupadas)`() {
        val ids = (1..7).map { "g$it" }
        val e = HexSetup.setupDoEncontro(ids, distanciaM = 3)
        val posicoes = e.posicoes.filter { it.id != "heroi" }.map { it.posicao }
        assertEquals(7, posicoes.toSet().size) // sem sobreposição mesmo com 7
        val g7 = e.posicoes.first { it.id == "g7" }
        assertEquals(4, g7.posicao.distancia(HexCoord.ORIGEM)) // anel +1
    }

    @Test
    fun `distanciaM e clampada ao raio da grade`() {
        val e = HexSetup.setupDoEncontro(listOf("g"), distanciaM = 50, raioGrade = 7)
        val g = e.posicoes.first { it.id == "g" }
        assertTrue(g.posicao.distancia(HexCoord.ORIGEM) <= 7)
    }

    // ─── hexesAlcancaveis ───────────────────────────────────────

    @Test
    fun `alcancaveis respeita o deslocamento e exclui a propria posicao`() {
        val e = HexSetup.setupDoEncontro(listOf("g"), distanciaM = 5)
        val alc = HexSetup.hexesAlcancaveis(e, deslocamento = 2)
        assertFalse(HexCoord.ORIGEM in alc)
        assertTrue(alc.isNotEmpty())
        alc.forEach { assertTrue(HexCoord.ORIGEM.distancia(it) <= 2) }
    }

    @Test
    fun `alcancaveis exclui hexes ocupados por inimigos`() {
        val e = HexSetup.setupDoEncontro(listOf("g"), distanciaM = 2)
        val posG = e.posicoes.first { it.id == "g" }.posicao
        val alc = HexSetup.hexesAlcancaveis(e, deslocamento = 3)
        assertFalse(posG in alc)
    }

    @Test
    fun `deslocamento zero devolve vazio`() {
        val e = HexSetup.setupDoEncontro(listOf("g"), distanciaM = 3)
        assertTrue(HexSetup.hexesAlcancaveis(e, 0).isEmpty())
    }

    @Test
    fun `heroi CERCADO pelos 6 lados nao alcanca nada (BFS nao atravessa ocupados)`() {
        // Achado da revisão TOK-4: o range geométrico deixava atravessar a linha inimiga de graça
        // (Combate.md exige Evadir). O BFS bloqueia o caminho — cercado = preso.
        val vizinhos = listOf(
            HexCoord(1, 0), HexCoord(0, 1), HexCoord(-1, 1),
            HexCoord(-1, 0), HexCoord(0, -1), HexCoord(1, -1)
        )
        val posicoes = mutableListOf(PosicaoCombatente("heroi", HexCoord.ORIGEM, Direcao.LESTE))
        vizinhos.forEachIndexed { i, h -> posicoes.add(PosicaoCombatente("g$i", h, Direcao.OESTE)) }
        val estado = HexCombatState(posicoes = posicoes)
        assertTrue(HexSetup.hexesAlcancaveis(estado, deslocamento = 5).isEmpty())
    }

    @Test
    fun `BFS contorna obstaculo - hex atras de inimigo exige caminho ao redor`() {
        // Inimigo em (1,0): o hex (2,0) SÓ é alcançável com deslocamento suficiente pra contornar.
        val estado = HexCombatState(posicoes = listOf(
            PosicaoCombatente("heroi", HexCoord.ORIGEM, Direcao.LESTE),
            PosicaoCombatente("g", HexCoord(1, 0), Direcao.OESTE)
        ))
        // Com deslocamento 2: caminho reto (0,0)→(1,0)→(2,0) bloqueado; contorno (0,0)→(1,-1)→(2,-1)…
        // (2,0) está a 2 passos? (0,0)→(1,-1)→(2,-1) chega em (2,-1); (2,0) exige 3 passos contornando.
        val alc2 = HexSetup.hexesAlcancaveis(estado, 2)
        assertFalse("(2,0) atrás do inimigo NÃO é alcançável em 2 passos", HexCoord(2, 0) in alc2)
        val alc3 = HexSetup.hexesAlcancaveis(estado, 3)
        assertTrue("(2,0) é alcançável em 3 passos contornando", HexCoord(2, 0) in alc3)
    }

    @Test
    fun `setup na BORDA com 7 inimigos nao sobrepoe ninguem`() {
        // Achado da revisão TOK-4: a trava de borda antiga resetava pra hex OCUPADO.
        val ids = (1..7).map { "g$it" }
        val e = HexSetup.setupDoEncontro(ids, distanciaM = 7, raioGrade = 7)
        val posicoes = e.posicoes.map { it.posicao }
        assertEquals("todas as posições distintas", posicoes.size, posicoes.toSet().size)
        posicoes.forEach { assertTrue(HexCoord.ORIGEM.distancia(it) <= 7) }
    }

    // ─── moverHeroi + distanciasAoHeroi ─────────────────────────

    @Test
    fun `mover heroi atualiza posicao facing e distancias`() {
        val e = HexSetup.setupDoEncontro(listOf("g"), distanciaM = 4) // g em (4,0)
        val movido = HexSetup.moverHeroi(e, HexCoord(2, 0))
        val heroi = movido.posicoes.first { it.id == "heroi" }
        assertEquals(HexCoord(2, 0), heroi.posicao)
        assertEquals(Direcao.LESTE, heroi.facing) // moveu pro leste
        assertEquals(mapOf("g" to 2), HexSetup.distanciasAoHeroi(movido))
    }

    @Test
    fun `mover heroi para hex ocupado nao move`() {
        val e = HexSetup.setupDoEncontro(listOf("g"), distanciaM = 3)
        val posG = e.posicoes.first { it.id == "g" }.posicao
        assertEquals(e, HexSetup.moverHeroi(e, posG))
    }

    // ─── manterApenas ───────────────────────────────────────────

    @Test
    fun `morto sai da grade e heroi sempre fica`() {
        val e = HexSetup.setupDoEncontro(listOf("a", "b"), distanciaM = 3)
        val depois = HexSetup.manterApenas(e, vivos = setOf("b"))
        assertEquals(setOf("heroi", "b"), depois.posicoes.map { it.id }.toSet())
    }

    @Test
    fun `todos vivos devolve o mesmo estado (identidade)`() {
        val e = HexSetup.setupDoEncontro(listOf("a", "b"), distanciaM = 3)
        assertTrue(e === HexSetup.manterApenas(e, setOf("a", "b")))
    }
}
