package com.gurps.ficha.domain.combat.hex

import com.gurps.ficha.domain.combat.CombatResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote HEX-4: regras posicionais base (facing/costas/flanco). Kotlin puro. */
class HexRegrasFacingTest {

    private fun opcao(tipo: CombatResolver.TipoDefesa, valor: Int) =
        CombatResolver.OpcaoDefesa(tipo, valor, emptyList(), disponivel = true)

    @Test
    fun `ajustarValorDefesa preserva valor na frente, subtrai 2 no flanco, zera nas costas`() {
        assertEquals(10, HexRegrasFacing.ajustarValorDefesa(10, Facing.FRENTE))
        assertEquals(8,  HexRegrasFacing.ajustarValorDefesa(10, Facing.FLANCO))
        assertEquals(0,  HexRegrasFacing.ajustarValorDefesa(10, Facing.COSTAS))
    }

    @Test
    fun `ajustarValorDefesa não vai abaixo de zero (piso em flanco)`() {
        assertEquals(0, HexRegrasFacing.ajustarValorDefesa(1, Facing.FLANCO))
    }

    @Test
    fun `defesaAnulada é true SÓ em costas`() {
        assertFalse(HexRegrasFacing.defesaAnulada(Facing.FRENTE))
        assertFalse(HexRegrasFacing.defesaAnulada(Facing.FLANCO))
        assertTrue(HexRegrasFacing.defesaAnulada(Facing.COSTAS))
    }

    @Test
    fun `ajustarOpcoesDefesa preserva lista na frente`() {
        val ops = listOf(opcao(CombatResolver.TipoDefesa.ESQUIVA, 9),
                         opcao(CombatResolver.TipoDefesa.APARA, 11))
        assertEquals(ops, HexRegrasFacing.ajustarOpcoesDefesa(ops, Facing.FRENTE))
    }

    @Test
    fun `ajustarOpcoesDefesa devolve LISTA VAZIA em costas (defesa anulada)`() {
        val ops = listOf(opcao(CombatResolver.TipoDefesa.ESQUIVA, 9))
        assertTrue(HexRegrasFacing.ajustarOpcoesDefesa(ops, Facing.COSTAS).isEmpty())
    }

    @Test
    fun `ajustarOpcoesDefesa subtrai 2 de todas em flanco preservando os outros campos`() {
        val ops = listOf(
            opcao(CombatResolver.TipoDefesa.ESQUIVA, 9),
            opcao(CombatResolver.TipoDefesa.APARA, 11).copy(recuo = true), // preserva flag
        )
        val ajust = HexRegrasFacing.ajustarOpcoesDefesa(ops, Facing.FLANCO)
        assertEquals(7, ajust[0].valorFinal)
        assertEquals(9, ajust[1].valorFinal)
        assertTrue("flag recuo deve ser preservada no flanco", ajust[1].recuo)
    }

    @Test
    fun `flanco tambem tira o BD do escudo (MB p375, RegLote 4)`() {
        // Herói com escudo +2 BD embutido em Esquiva=11 e Apara=13.
        val ops = listOf(opcao(CombatResolver.TipoDefesa.ESQUIVA, 11),
                         opcao(CombatResolver.TipoDefesa.APARA, 13))
        val ajust = HexRegrasFacing.ajustarOpcoesDefesa(ops, Facing.FLANCO, bonusEscudoEmbutido = 2)
        // Espera: 11 - 2 (flanco) - 2 (BD) = 7 ; 13 - 2 - 2 = 9.
        assertEquals(7, ajust[0].valorFinal)
        assertEquals(9, ajust[1].valorFinal)
    }

    @Test
    fun `ajustarValorDefesa escalar tambem desconta o BD do escudo em flanco`() {
        assertEquals(6, HexRegrasFacing.ajustarValorDefesa(10, Facing.FLANCO, bonusEscudoEmbutido = 2))
        assertEquals(0, HexRegrasFacing.ajustarValorDefesa(1, Facing.FLANCO, bonusEscudoEmbutido = 2)) // piso
    }

    @Test
    fun `flanco adiciona ComponenteMod nomeado com o delta real, preservando o invariante da soma`() {
        // O CombatResolver mantém: valorFinal ~ soma(componentes) (mais o valor base implícito). O UI mostra
        // 'valorFinal (componentes)'. Se ajustarOpcoesDefesa reduzir valorFinal sem tocar em componentes,
        // o breakdown fica inconsistente. Este teste tranca o fix.
        val op = opcao(CombatResolver.TipoDefesa.ESQUIVA, 9).copy(
            componentes = listOf(CombatResolver.ComponenteMod("base", 9))
        )
        val ajust = HexRegrasFacing.ajustarOpcoesDefesa(listOf(op), Facing.FLANCO, bonusEscudoEmbutido = 1)[0]
        assertEquals(6, ajust.valorFinal) // 9 - 2 - 1
        assertTrue("componentes devem incluir o modificador 'flanco'",
            ajust.componentes.any { it.nome == "flanco" })
        // O delta somado deve ser -3 (o −2 do flanco + o −1 do BD).
        assertEquals(-3, ajust.componentes.first { it.nome == "flanco" }.valor)
    }

    @Test
    fun `facingDoAtaque (wrapper) delega para HexGrid`() {
        // Herói (0,0) olha LESTE; ataque vindo do OESTE → costas.
        val alvo = HexCoord(0, 0)
        val origem = alvo + Direcao.OESTE.vetor
        assertEquals(Facing.COSTAS, HexRegrasFacing.facingDoAtaque(origem, alvo, Direcao.LESTE))
    }
}
