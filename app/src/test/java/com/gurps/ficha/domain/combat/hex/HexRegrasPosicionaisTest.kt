package com.gurps.ficha.domain.combat.hex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Lote HEX-6: cobertura + ataque através de hex + manter à distância. Kotlin puro. */
class HexRegrasPosicionaisTest {

    // ---------------- HexCobertura ----------------

    @Test
    fun `cobertura LIMPA quando nao ha bloqueadores`() {
        val g = HexCobertura.grauEntre(HexCoord(0, 0), HexCoord(3, 0), emptySet())
        assertEquals(HexCobertura.Grau.LIMPA, g)
        assertEquals(0, g.penalidadeAtaque)
    }

    @Test
    fun `cobertura TOTAL quando um bloqueador esta entre atacante e alvo`() {
        // linhaReta((0,0), (3,0)) = [(0,0), (1,0), (2,0), (3,0)]
        val g = HexCobertura.grauEntre(HexCoord(0, 0), HexCoord(3, 0), setOf(HexCoord(2, 0)))
        assertEquals(HexCobertura.Grau.TOTAL, g)
        assertEquals(false, g.podeAtacar)
    }

    @Test
    fun `bloqueador nos endpoints nao conta como cobertura TOTAL`() {
        val g = HexCobertura.grauEntre(HexCoord(0, 0), HexCoord(3, 0),
            setOf(HexCoord(0, 0), HexCoord(3, 0)))
        // endpoints excluidos, e (0,0)/(3,0) nao sao vizinhos do alvo mais proximos do atacante — LIMPA.
        assertEquals(HexCobertura.Grau.LIMPA, g)
    }

    @Test
    fun `cobertura PARCIAL quando bloqueador adjacente ao alvo esta mais perto do atacante`() {
        // Atacante (0,0), alvo (2,2). linhaReta = [(0,0),(0,1),(1,1),(2,1),(2,2)] — logo (2,1) daria TOTAL.
        // Uso (1,2) como bloqueador: vizinho OESTE de (2,2), dist ao atacante = 3 < 4, e NAO esta na linha.
        val g = HexCobertura.grauEntre(HexCoord(0, 0), HexCoord(2, 2), setOf(HexCoord(1, 2)))
        assertEquals(HexCobertura.Grau.PARCIAL, g)
        assertEquals(-2, g.penalidadeAtaque)
    }

    @Test
    fun `TOTAL tem precedencia sobre PARCIAL`() {
        // Bloqueador no meio E vizinho do alvo — TOTAL vence.
        val g = HexCobertura.grauEntre(HexCoord(0, 0), HexCoord(3, 0),
            setOf(HexCoord(1, 0), HexCoord(2, 0)))
        assertEquals(HexCobertura.Grau.TOTAL, g)
    }

    @Test
    fun `atacante e alvo no mesmo hex sempre LIMPA`() {
        val g = HexCobertura.grauEntre(HexCoord(2, 2), HexCoord(2, 2), setOf(HexCoord(2, 2)))
        assertEquals(HexCobertura.Grau.LIMPA, g)
    }

    @Test
    fun `hex do atacante em bloqueadores NAO gera PARCIAL falsa contra alvo adjacente`() {
        // Atirador em cima de um telhado/pilar/arbusto denso — o hex do atacante e um obstaculo de LoS,
        // mas nao gera cobertura parcial contra o alvo adjacente. Convencao dos endpoints (espelha
        // HexGrid.linhaDeVisao). Antes do fix, isso devolvia PARCIAL (-2) espurio.
        val g = HexCobertura.grauEntre(HexCoord(0, 0), HexCoord(1, 0), setOf(HexCoord(0, 0)))
        assertEquals(HexCobertura.Grau.LIMPA, g)
    }

    // ---------------- HexAtaqueAtravesHex ----------------

    @Test
    fun `arma de alcance 1 nao pode atacar alvo a 2 hexes`() {
        val p = HexAtaqueAtravesHex.penalidade(
            HexCoord(0, 0), HexCoord(2, 0), alcanceArmaMetros = 1,
            ocupantesAliados = emptySet(), ocupantesInimigos = emptySet()
        )
        assertNull(p)
    }

    @Test
    fun `alcance 2 com linha limpa devolve zero`() {
        val p = HexAtaqueAtravesHex.penalidade(
            HexCoord(0, 0), HexCoord(2, 0), alcanceArmaMetros = 2,
            ocupantesAliados = emptySet(), ocupantesInimigos = emptySet()
        )
        assertEquals(0, p)
    }

    @Test
    fun `aliado no meio nao penaliza (treino basico)`() {
        val p = HexAtaqueAtravesHex.penalidade(
            HexCoord(0, 0), HexCoord(2, 0), alcanceArmaMetros = 2,
            ocupantesAliados = setOf(HexCoord(1, 0)), ocupantesInimigos = emptySet()
        )
        assertEquals(0, p)
    }

    @Test
    fun `inimigo no meio impoe menos 4`() {
        val p = HexAtaqueAtravesHex.penalidade(
            HexCoord(0, 0), HexCoord(2, 0), alcanceArmaMetros = 2,
            ocupantesAliados = emptySet(), ocupantesInimigos = setOf(HexCoord(1, 0))
        )
        assertEquals(-4, p)
    }

    @Test
    fun `alvo adjacente sempre zero mesmo com alcance 1`() {
        val p = HexAtaqueAtravesHex.penalidade(
            HexCoord(0, 0), HexCoord(1, 0), alcanceArmaMetros = 1,
            ocupantesAliados = emptySet(), ocupantesInimigos = emptySet()
        )
        assertEquals(0, p)
    }

    // ---------------- HexManterADistancia ----------------

    @Test
    fun `NENHUMA nao adiciona custo`() {
        val r = HexManterADistancia.avaliar(HexManterADistancia.TipoInterrupcao.NENHUMA)
        assertEquals(true, r.podeAvancar)
        assertEquals(0, r.movimentoExtra)
        assertEquals(false, r.disputaSTNecessaria)
        assertNull(r.testeVontadeMod)
    }

    @Test
    fun `APAROU_COM_DANO_NAO_ESTOCADA exige disputa ST e mais 2 MV`() {
        val r = HexManterADistancia.avaliar(HexManterADistancia.TipoInterrupcao.APAROU_COM_DANO_NAO_ESTOCADA)
        assertEquals(true, r.podeAvancar)
        assertEquals(2, r.movimentoExtra)
        assertEquals(true, r.disputaSTNecessaria)
        assertNull(r.testeVontadeMod)
    }

    @Test
    fun `APAROU_COM_ESTOCADA_PERFURANTE oferece Vontade -3 ao inves de disputa ST`() {
        val r = HexManterADistancia.avaliar(HexManterADistancia.TipoInterrupcao.APAROU_COM_ESTOCADA_PERFURANTE)
        assertEquals(true, r.podeAvancar)
        assertEquals(2, r.movimentoExtra)
        assertEquals(false, r.disputaSTNecessaria)
        assertEquals(-3, r.testeVontadeMod)
    }

    @Test
    fun `NOCAUTE_OU_PROJECAO impede avanco este turno`() {
        val r = HexManterADistancia.avaliar(HexManterADistancia.TipoInterrupcao.NOCAUTE_OU_PROJECAO)
        assertEquals(false, r.podeAvancar)
        assertEquals(0, r.movimentoExtra)
    }
}
