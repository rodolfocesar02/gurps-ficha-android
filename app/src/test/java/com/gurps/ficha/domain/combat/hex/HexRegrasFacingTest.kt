package com.gurps.ficha.domain.combat.hex

import com.gurps.ficha.domain.combat.CombatResolver
import com.gurps.ficha.domain.combat.Condicao
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

    // ── Lote HEX-FACING: os arcos completos, olhando para LESTE ─────────────────────────────────
    // Base da dúvida do usuário no aparelho ("nessa posição eu deveria estar flanqueado?"). A regra:
    // FRENTE = a direção encarada + as DUAS vizinhas (3 de 6); FLANCO = 2; COSTAS = 1.

    @Test fun `olhando para LESTE — frente cobre Leste, Nordeste e Sudeste`() {
        val alvo = HexCoord(0, 0)
        listOf(Direcao.LESTE, Direcao.NORDESTE, Direcao.SUDESTE).forEach { d ->
            assertEquals("ataque vindo de $d deveria ser FRENTE",
                Facing.FRENTE, HexRegrasFacing.facingDoAtaque(alvo + d.vetor, alvo, Direcao.LESTE))
        }
    }

    @Test fun `olhando para LESTE — flanco e so Noroeste e Sudoeste`() {
        val alvo = HexCoord(0, 0)
        listOf(Direcao.NOROESTE, Direcao.SUDOESTE).forEach { d ->
            assertEquals("ataque vindo de $d deveria ser FLANCO",
                Facing.FLANCO, HexRegrasFacing.facingDoAtaque(alvo + d.vetor, alvo, Direcao.LESTE))
        }
    }

    @Test fun `virar-se MUDA o arco — o mesmo atacante sai do flanco para a frente`() {
        // É exatamente o que a ação livre de virar (MB p.387) dá ao jogador: encarar a ameaça.
        val alvo = HexCoord(0, 0)
        val atacante = alvo + Direcao.NOROESTE.vetor
        assertEquals("olhando para Leste, o atacante a Noroeste pega o flanco",
            Facing.FLANCO, HexRegrasFacing.facingDoAtaque(atacante, alvo, Direcao.LESTE))
        assertEquals("virando para Noroeste, ele passa a vir de FRENTE",
            Facing.FRENTE, HexRegrasFacing.facingDoAtaque(atacante, alvo, Direcao.NOROESTE))
    }

    // ── Lote HEX-FACING-2 (MB p.388): virada no FIM do movimento ────────────────────────────────
    // "Livre! O personagem pode se virar para QUALQUER direção se não usou mais que a METADE dos seus
    // pontos de movimento; se usou mais, ele pode mudar sua direção em apenas UM LADO DE HEXÁGONO."

    /**
     * Lote TESTE-C: chama o CÓDIGO REAL (`RegrasMovimentoTatico`), não mais uma cópia da regra.
     *
     * Antes havia aqui um `direcoesPermitidas` que reimplementava a lógica. Ele passava verde mesmo
     * que o controller quebrasse — inclusive no caso mais provável: trocar `deslocamentoEfetivo`
     * (com carga/ferimento) pelo deslocamento cru da ficha.
     */
    private fun direcoesPermitidas(facingAtual: Direcao, andou: Int, deslocamento: Int): List<Direcao> =
        RegrasMovimentoTatico.direcoesDaViradaFinal(
            facingAtual,
            RegrasMovimentoTatico.viradaFinalLivre(andou, deslocamento),
        )

    @Test fun `andou ate METADE do deslocamento — vira para QUALQUER direcao`() {
        assertEquals(6, direcoesPermitidas(Direcao.LESTE, andou = 3, deslocamento = 6).size)
        assertEquals("exatamente a metade ainda é livre", 6,
            direcoesPermitidas(Direcao.LESTE, andou = 2, deslocamento = 4).size)
    }

    @Test fun `andou MAIS que a metade — so um lado de hexagono para cada lado`() {
        val p = direcoesPermitidas(Direcao.LESTE, andou = 5, deslocamento = 6)
        assertEquals(3, p.size) // a atual + 1 vizinha de cada lado
        assertTrue(Direcao.LESTE in p)
        assertTrue(Direcao.SUDESTE in p)   // ordinal +1
        assertTrue(Direcao.NORDESTE in p)  // ordinal −1 (dá a volta na roda)
        assertFalse("dar meia-volta exigiria 3 lados", Direcao.OESTE in p)
    }

    @Test fun `virada limitada ainda tira o atacante das COSTAS`() {
        // Mesmo restrito a 1 lado, o herói consegue sair do pior caso (costas → flanco).
        val alvo = HexCoord(0, 0)
        val atacante = alvo + Direcao.OESTE.vetor
        assertEquals(Facing.COSTAS, HexRegrasFacing.facingDoAtaque(atacante, alvo, Direcao.LESTE))
        val permitidas = direcoesPermitidas(Direcao.LESTE, andou = 5, deslocamento = 6)
        val melhor = permitidas.minByOrNull { HexRegrasFacing.facingDoAtaque(atacante, alvo, it).ordinal }
        assertEquals("virando 1 lado, as costas viram flanco",
            Facing.FLANCO, HexRegrasFacing.facingDoAtaque(atacante, alvo, melhor!!))
    }

    // ── Lote TESTE-C: travas de movimento na grade (antes só existiam dentro do controller) ──────

    @Test fun `atordoado, agarrado ou imobilizado NAO se move pela grade`() {
        // MB p.420 (atordoado) e p.371 (agarrado/imobilizado só sai se Desvencilhar). Sem estas
        // travas a grade driblava a luta agarrada: bastava tocar um hex verde e sair andando.
        assertTrue("sem condição nenhuma, move",
            RegrasMovimentoTatico.podeMoverNaGrade(emptySet(), conjurandoMultiTurno = false))
        listOf(Condicao.ATORDOADO, Condicao.AGARRADO, Condicao.IMOBILIZADO).forEach { c ->
            assertFalse("$c deveria travar o movimento na grade",
                RegrasMovimentoTatico.podeMoverNaGrade(setOf(c), conjurandoMultiTurno = false))
        }
    }

    @Test fun `magia multi-turno em andamento tambem prende o operador`() {
        assertFalse(RegrasMovimentoTatico.podeMoverNaGrade(emptySet(), conjurandoMultiTurno = true))
    }

    @Test fun `condicao que NAO impede andar (ex cego) deixa mover`() {
        // Trava demais também é bug: cegueira atrapalha mirar, não andar.
        assertTrue(RegrasMovimentoTatico.podeMoverNaGrade(setOf(Condicao.CEGO), conjurandoMultiTurno = false))
    }

    @Test fun `a regra da metade usa o deslocamento EFETIVO — o caso que a copia nao protegia`() {
        // Deslocamento efetivo 6 (são): andar 3 ainda é livre.
        assertTrue(RegrasMovimentoTatico.viradaFinalLivre(andou = 3, deslocamentoEfetivo = 6))
        // O MESMO herói ferido/carregado, efetivo 4: andar 3 já passou da metade → virada limitada.
        assertFalse(RegrasMovimentoTatico.viradaFinalLivre(andou = 3, deslocamentoEfetivo = 4))
    }

    @Test fun `atacante DISTANTE tambem e classificado — nao so o adjacente`() {
        // O goblin do print estava longe (magia à distância), não colado.
        val alvo = HexCoord(0, 0)
        val longeNordeste = HexCoord(3, -3) // 3 hexes a nordeste
        assertEquals(Facing.FRENTE, HexRegrasFacing.facingDoAtaque(longeNordeste, alvo, Direcao.LESTE))
        assertEquals(Facing.FLANCO, HexRegrasFacing.facingDoAtaque(longeNordeste, alvo, Direcao.SUDESTE))
    }
}
