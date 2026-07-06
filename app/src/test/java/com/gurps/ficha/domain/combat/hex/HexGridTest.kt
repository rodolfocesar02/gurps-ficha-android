package com.gurps.ficha.domain.combat.hex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lote HEX-1: testes do motor de grade. Kotlin puro, sem Android.
 * Referência dos casos: RedBlob Games (Hexagonal Grids) e MB p.366 (1 hex = 1 metro).
 */
class HexGridTest {

    // ── HexCoord: soma, subtração, componente s, distância ─────────────────────

    @Test
    fun `s implícito preserva q+r+s = 0`() {
        val c = HexCoord(3, -1)
        assertEquals(-2, c.s)                    // -q-r
        assertEquals(0, c.q + c.r + c.s)
    }

    @Test
    fun `soma e diferença de hexes se comportam como vetores`() {
        val a = HexCoord(2, -1); val b = HexCoord(1, 3)
        assertEquals(HexCoord(3, 2), a + b)
        assertEquals(HexCoord(1, -4), a - b)
    }

    @Test
    fun `distância entre um hex e ele mesmo é zero`() {
        assertEquals(0, HexCoord(0, 0).distancia(HexCoord(0, 0)))
        assertEquals(0, HexCoord(-3, 5).distancia(HexCoord(-3, 5)))
    }

    @Test
    fun `distância é simétrica`() {
        val a = HexCoord(2, -3); val b = HexCoord(-1, 4)
        assertEquals(a.distancia(b), b.distancia(a))
    }

    @Test
    fun `distância de vizinho adjacente é sempre 1 (todas as 6 direções)`() {
        val centro = HexCoord(0, 0)
        for (dir in Direcao.values()) {
            assertEquals("direção ${dir.name}", 1, centro.distancia(centro + dir.vetor))
        }
    }

    @Test
    fun `distância em linha reta soma o passo (2 e 3 hexes)`() {
        // Dois passos p/ leste = distância 2.
        assertEquals(2, HexCoord(0, 0).distancia(HexCoord(2, 0)))
        // Três passos p/ sudeste = distância 3.
        assertEquals(3, HexCoord(0, 0).distancia(HexCoord(0, 3)))
    }

    // ── HexGrid: vizinhos, range, linha reta ───────────────────────────────────

    @Test
    fun `vizinhos devolvem os 6 hexes adjacentes, únicos e todos a distância 1`() {
        val centro = HexCoord(4, -2)
        val viz = HexGrid.vizinhos(centro)
        assertEquals(6, viz.size)
        assertEquals(6, viz.toSet().size)
        assertTrue(viz.all { centro.distancia(it) == 1 })
    }

    @Test
    fun `range 0 é só o centro e range 1 é centro mais 6 e range 2 é 19`() {
        val c = HexCoord(0, 0)
        assertEquals(listOf(c), HexGrid.range(c, 0))
        assertEquals(7, HexGrid.range(c, 1).size)
        // Fórmula: 3n² + 3n + 1 → n=2 dá 19.
        assertEquals(19, HexGrid.range(c, 2).size)
    }

    @Test
    fun `linhaReta entre iguais é lista com um único hex`() {
        val c = HexCoord(1, 1)
        assertEquals(listOf(c), HexGrid.linhaReta(c, c))
    }

    @Test
    fun `linhaReta tem distancia + 1 hexes e começa em a e termina em b`() {
        val a = HexCoord(0, 0); val b = HexCoord(3, -1)
        val linha = HexGrid.linhaReta(a, b)
        assertEquals(a.distancia(b) + 1, linha.size)
        assertEquals(a, linha.first())
        assertEquals(b, linha.last())
        // Cada passo é adjacente ao anterior (é uma linha, sem saltos).
        for (i in 1 until linha.size) {
            assertEquals("passo $i", 1, linha[i - 1].distancia(linha[i]))
        }
    }

    // ── Linha de visão ─────────────────────────────────────────────────────────

    @Test
    fun `LoS livre entre dois hexes sem obstáculos`() {
        assertTrue(HexGrid.linhaDeVisao(HexCoord(0, 0), HexCoord(4, 0)) { false })
    }

    @Test
    fun `LoS bloqueada quando um hex intermediário é obstáculo`() {
        val obstaculo = HexCoord(2, 0)
        assertFalse(HexGrid.linhaDeVisao(HexCoord(0, 0), HexCoord(4, 0)) { it == obstaculo })
    }

    @Test
    fun `hexes das pontas NÃO bloqueiam a própria LoS (mesmo se marcados como obstáculo)`() {
        // Se o herói estiver "dentro" de um hex de mato alto, ele ainda enxerga pra fora.
        val de = HexCoord(0, 0); val ate = HexCoord(3, 0)
        assertTrue(HexGrid.linhaDeVisao(de, ate) { it == de || it == ate })
    }

    // ── Facing: frente / flanco / costas ───────────────────────────────────────

    @Test
    fun `ataque pela frente do alvo não tem penalidade nem anula defesa`() {
        val alvo = HexCoord(0, 0)
        val origem = alvo + Direcao.LESTE.vetor   // atacante a leste
        val facingAlvo = Direcao.LESTE            // alvo olhando p/ leste → recebe ataque na cara
        val f = HexGrid.facingDoAtaque(origem, alvo, facingAlvo)
        assertEquals(Facing.FRENTE, f)
        assertEquals(0, f.penalidadeDefesa); assertFalse(f.defesaAnulada)
    }

    @Test
    fun `ataque pelas costas do alvo ANULA a defesa`() {
        val alvo = HexCoord(0, 0)
        val origem = alvo + Direcao.OESTE.vetor   // atacante a oeste
        val facingAlvo = Direcao.LESTE            // alvo olha p/ leste → oeste é as costas
        val f = HexGrid.facingDoAtaque(origem, alvo, facingAlvo)
        assertEquals(Facing.COSTAS, f)
        assertTrue(f.defesaAnulada)
    }

    @Test
    fun `ataque pelo flanco do alvo aplica -2 nas defesas e NÃO anula`() {
        val alvo = HexCoord(0, 0)
        // Alvo olhando p/ leste; ataque vindo do SUDOESTE → diff angular 2 → flanco.
        val origem = alvo + Direcao.SUDOESTE.vetor
        val f = HexGrid.facingDoAtaque(origem, alvo, Direcao.LESTE)
        assertEquals(Facing.FLANCO, f)
        assertEquals(-2, f.penalidadeDefesa); assertFalse(f.defesaAnulada)
    }

    @Test
    fun `os hexes vizinhos ao facing também contam como FRENTE (arco frontal de 3 hexes)`() {
        // Alvo olhando p/ LESTE; ataque vindo do NORDESTE (vizinho do facing) → deve ser FRENTE.
        val alvo = HexCoord(0, 0)
        val origem = alvo + Direcao.NORDESTE.vetor
        assertEquals(Facing.FRENTE, HexGrid.facingDoAtaque(origem, alvo, Direcao.LESTE))
    }

    @Test
    fun `mesmo hex de origem e alvo devolve FRENTE por convenção`() {
        val h = HexCoord(2, 2)
        assertEquals(Facing.FRENTE, HexGrid.facingDoAtaque(h, h, Direcao.LESTE))
    }

    @Test
    fun `Direcao oposta é rotação de 180 graus (3 posições)`() {
        assertEquals(Direcao.OESTE, Direcao.LESTE.oposta)
        assertEquals(Direcao.NORDESTE, Direcao.SUDOESTE.oposta)
        assertEquals(Direcao.LESTE, Direcao.LESTE.oposta.oposta)
    }
}
