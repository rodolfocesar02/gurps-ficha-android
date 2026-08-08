package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lotes MB-1 e MB-4** — os modificadores condicionais de combate.
 *
 * O que este arquivo protege são as **duas travas** que somem numa conta feita de
 * cabeça e que mudam o resultado:
 *
 * 1. O **teto de 9** quando entra um modificador com asterisco. É teto, não
 *    penalidade — um NH 20 vai a 9.
 * 2. O **limite de −10** das penalidades de visibilidade somadas.
 *
 * E uma armadilha de transcrição: **Ataque Total (Determinado) vale +4 no corpo a
 * corpo e +1 à distância**. São números diferentes no livro.
 */
class ModificadoresDeCombateTest {

    private fun corpo(id: String, qtd: Int = 1) = ModificadoresDeCombate.Escolha(
        ModificadoresDeCombate.CORPO_A_CORPO.first { it.id == id }, qtd
    )

    private fun dist(id: String, qtd: Int = 1) = ModificadoresDeCombate.Escolha(
        ModificadoresDeCombate.A_DISTANCIA.first { it.id == id }, qtd
    )

    // ==================================================================
    // 1. 🔴 O teto do asterisco
    // ==================================================================

    @Test
    fun `🔴 o asterisco derruba o NH para 9, por mais alto que ele seja`() {
        // Golpe Desenfreado é −5 COM asterisco. NH 20 − 5 = 15, e o livro trava
        // em 9. É teto, não penalidade — a mesma armadilha do Avançar e Atacar.
        val r = ModificadoresDeCombate.aplicar(20, listOf(corpo("golpe_desenfreado")))
        assertEquals(9, r.nhFinal)
        assertTrue(r.tetoDoAsteriscoAplicado)
    }

    @Test
    fun `⚠️ NH ja baixo NAO sobe por causa do teto`() {
        // O teto é um MÁXIMO. Um NH 10 com Golpe Desenfreado vira 5, não 9.
        val r = ModificadoresDeCombate.aplicar(10, listOf(corpo("golpe_desenfreado")))
        assertEquals(5, r.nhFinal)
        assertTrue(!r.tetoDoAsteriscoAplicado)
    }

    @Test
    fun `sem asterisco, nao ha teto nenhum`() {
        // Ataque Total (Determinado) é +4 e não tem asterisco: NH 20 vira 24.
        val r = ModificadoresDeCombate.aplicar(20, listOf(corpo("ataque_total_determinado")))
        assertEquals(24, r.nhFinal)
        assertTrue(!r.tetoDoAsteriscoAplicado)
        assertNull(ModificadoresDeCombate.avisoDoTeto(r))
    }

    @Test
    fun `o aviso do teto so aparece quando ele CORTOU`() {
        val cortou = ModificadoresDeCombate.aplicar(20, listOf(corpo("golpe_desenfreado")))
        assertTrue(ModificadoresDeCombate.avisoDoTeto(cortou)!!.contains("p.548"))
        val naoCortou = ModificadoresDeCombate.aplicar(10, listOf(corpo("golpe_desenfreado")))
        assertNull(ModificadoresDeCombate.avisoDoTeto(naoCortou))
    }

    // ==================================================================
    // 2. 🔴 O limite de visibilidade
    // ==================================================================

    @Test
    fun `🔴 a visibilidade somada para em menos 10`() {
        // A luz da cena já descontou −8; o jogador marca mais escuridão. O total
        // não pode passar de −10.
        val r = ModificadoresDeCombate.aplicar(
            nhBase = 15, escolhas = emptyList(),
            penalidadeDeVisibilidadeJaAplicada = -14
        )
        assertTrue(r.limiteDeVisibilidadeAplicado)
        // O nhBase já vinha com os −14; a conta devolve +4 para respeitar o −10.
        assertEquals(19, r.nhFinal)
    }

    @Test
    fun `⚠️ quem e acostumado a cegueira para em menos 6`() {
        val r = ModificadoresDeCombate.aplicar(
            nhBase = 15, escolhas = emptyList(),
            penalidadeDeVisibilidadeJaAplicada = -10,
            acostumadoACegueira = true
        )
        assertTrue(r.limiteDeVisibilidadeAplicado)
        assertEquals(19, r.nhFinal)
        val aviso = ModificadoresDeCombate.avisoDaVisibilidade(r, acostumado = true)
        assertTrue(aviso!!, aviso.contains("-6"))
    }

    @Test
    fun `visibilidade dentro do limite nao e mexida`() {
        val r = ModificadoresDeCombate.aplicar(
            nhBase = 12, escolhas = emptyList(),
            penalidadeDeVisibilidadeJaAplicada = -3
        )
        assertEquals(12, r.nhFinal)
        assertTrue(!r.limiteDeVisibilidadeAplicado)
        assertNull(ModificadoresDeCombate.avisoDaVisibilidade(r, acostumado = false))
    }

    // ==================================================================
    // 3. ⚠️ A armadilha de transcrição
    // ==================================================================

    @Test
    fun `⚠️ Ataque Total vale +4 no corpo a corpo e +1 a distancia`() {
        // São números diferentes no livro (p.548 e p.549). Copiar a tabela
        // trocando os dois é o erro mais fácil deste lote.
        assertEquals(4, ModificadoresDeCombate.CORPO_A_CORPO.first { it.id == "ataque_total_determinado" }.valor)
        assertEquals(1, ModificadoresDeCombate.A_DISTANCIA.first { it.id == "ataque_total_determinado_dist" }.valor)
    }

    // ==================================================================
    // 4. Os que se repetem têm teto próprio
    // ==================================================================

    @Test
    fun `Avaliar acumula +1 por turno e para em +3`() {
        assertEquals(1, corpo("avaliar", 1).total)
        assertEquals(3, corpo("avaliar", 3).total)
        assertEquals("o 4o turno não vale mais nada", 3, corpo("avaliar", 9).total)
    }

    @Test
    fun `Choque acumula por ponto de dano e para em menos 4`() {
        assertEquals(-2, corpo("choque", 2).total)
        assertEquals(-4, corpo("choque", 4).total)
        assertEquals("dano de 12 não dá −12", -4, corpo("choque", 12).total)
    }

    @Test
    fun `os que se repetem sem teto acumulam direto`() {
        // −4 por pessoa no caminho, sem limite no livro.
        assertEquals(-12, dist("pessoa_no_caminho", 3).total)
        // −1 por ponto de ST que falta.
        assertEquals(-3, corpo("st_insuficiente", 3).total)
    }

    // ==================================================================
    // 5. Somando de verdade
    // ==================================================================

    @Test
    fun `varios modificadores somam, e o teto vem por ultimo`() {
        // NH 16, agachado (−2), agarrado (−4), avaliou 2 turnos (+2) = 12.
        val r = ModificadoresDeCombate.aplicar(
            16, listOf(corpo("agachado"), corpo("agarrado"), corpo("avaliar", 2))
        )
        assertEquals(-4, r.somaDosModificadores)
        assertEquals(12, r.nhFinal)
    }

    @Test
    fun `⚠️ o teto NAO segura quem ja esta abaixo dele`() {
        // 16 − 2 − 4 + 2 − 5 = 7. O asterisco está lá, mas o teto é um MÁXIMO:
        // quem já está em 7 continua em 7.
        //
        // 🔴 Escrevi este teste esperando 9 e ele reprovou. Quase "consertei" o
        // código para casar com a minha expectativa errada — que é exatamente
        // como um teto vira piso sem ninguém perceber.
        val r = ModificadoresDeCombate.aplicar(
            16,
            listOf(corpo("agachado"), corpo("agarrado"), corpo("avaliar", 2), corpo("golpe_desenfreado"))
        )
        assertEquals(7, r.nhFinal)
        assertTrue("o teto não devia ter cortado nada", !r.tetoDoAsteriscoAplicado)
    }

    @Test
    fun `🔴 com NH alto, o mesmo conjunto E cortado em 9`() {
        // 22 − 2 − 4 + 2 − 5 = 13, e aí sim o teto morde.
        val r = ModificadoresDeCombate.aplicar(
            22,
            listOf(corpo("agachado"), corpo("agarrado"), corpo("avaliar", 2), corpo("golpe_desenfreado"))
        )
        assertEquals(9, r.nhFinal)
        assertTrue(r.tetoDoAsteriscoAplicado)
    }

    @Test
    fun `lista vazia nao muda nada`() {
        val r = ModificadoresDeCombate.aplicar(13, emptyList())
        assertEquals(13, r.nhFinal)
        assertEquals(0, r.somaDosModificadores)
    }

    // ==================================================================
    // 6. ⚠️ O catálogo não pode repetir o que já é automático
    // ==================================================================

    @Test
    fun `⚠️ nada aqui duplica regra que ja existe em outro arquivo`() {
        // Aplicar duas vezes o mesmo redutor é PIOR que não ter a regra: o
        // jogador não tem como perceber. Estes ids vivem em `AvancarEAtacarRules`,
        // `GolpeRapidoEAparaRules`, `MaoInabilRules`, `LocaisDeAtaque`,
        // `IluminacaoRules`, `TabelaVelocidadeDistancia` e `TamanhoDoAlvoRules`.
        val proibidos = listOf(
            "avancar", "golpe_rapido", "mao_inabil", "ponto_impacto",
            "escuridao", "distancia", "tamanho_alvo", "desarmar"
        )
        val todos = ModificadoresDeCombate.CORPO_A_CORPO + ModificadoresDeCombate.A_DISTANCIA
        todos.forEach { m ->
            proibidos.forEach { p ->
                assertTrue(
                    "'${m.id}' colide com regra que já existe em outro arquivo",
                    !m.id.contains(p)
                )
            }
        }
    }

    @Test
    fun `nenhum id repetido, e todo modificador tem rotulo`() {
        val todos = ModificadoresDeCombate.CORPO_A_CORPO + ModificadoresDeCombate.A_DISTANCIA
        assertEquals(todos.map { it.id }.distinct().size, todos.size)
        todos.forEach {
            assertTrue("${it.id} sem rótulo", it.rotulo.isNotBlank())
            assertTrue("${it.id} com valor zero", it.valor != 0)
        }
    }
}
