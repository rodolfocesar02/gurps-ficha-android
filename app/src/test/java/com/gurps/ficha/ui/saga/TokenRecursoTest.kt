package com.gurps.ficha.ui.saga

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lote TOK-7: hexágonos de PV/PF no token.
 *
 * ⚠️ Escopo honesto: o DESENHO só se verifica com olho na tela (posição, opacidade, se o nome
 * ficou bonito). O que dá para travar aqui é a matemática do preenchimento e o limiar em que o
 * número some — que são as duas decisões que podem regredir em silêncio.
 */
class TokenRecursoTest {

    @Test
    fun `hexagono cheio preenche a altura toda e vazio nao preenche nada`() {
        assertEquals(2f * 20f, alturaPreenchidaDoHex(20f, 1f), 0.001f)
        assertEquals(0f, alturaPreenchidaDoHex(20f, 0f), 0.001f)
    }

    @Test
    fun `o preenchimento e proporcional — metade do PV, metade do hexagono`() {
        assertEquals(20f, alturaPreenchidaDoHex(20f, 0.5f), 0.001f)
        assertEquals(10f, alturaPreenchidaDoHex(20f, 0.25f), 0.001f)
    }

    @Test
    fun `PV NEGATIVO nao inverte o retangulo`() {
        // Em GURPS o herói vai a PV negativo antes de morrer, então a fração chega negativa aqui.
        // Sem clamp isso viraria um retângulo de altura negativa desenhado para cima.
        assertEquals(0f, alturaPreenchidaDoHex(20f, -0.4f), 0.001f)
    }

    @Test
    fun `fracao acima de 1 nao estoura o hexagono`() {
        assertEquals(2f * 20f, alturaPreenchidaDoHex(20f, 1.8f), 0.001f)
    }

    // ── Lote TOK-8: geometria do hexágono (o nome vazava para o hex de cima) ───────────────────

    @Test
    fun `a largura do hexagono e PLENA na faixa central`() {
        val tam = 100f
        val plena = tam * (SQRT3 / 2f)
        assertEquals(plena, meiaLarguraDoHex(tam, 0f), 0.01f)
        assertEquals(plena, meiaLarguraDoHex(tam, 50f), 0.01f)
        assertEquals("simétrico acima e abaixo", plena, meiaLarguraDoHex(tam, -50f), 0.01f)
    }

    @Test
    fun `perto do vertice o hexagono e ESTREITO — foi o que fez o nome vazar`() {
        val tam = 100f
        val plena = tam * (SQRT3 / 2f)
        // A 0,92·tam do centro sobra ~16% da largura. O TOK-7 clampava pela largura MÁXIMA e por
        // isso o nome escapava para o hex de cima.
        val em092 = meiaLarguraDoHex(tam, -92f)
        assertTrue("tem que ser bem menor que a plena: $em092 vs $plena", em092 < plena * 0.2f)
        assertEquals("no vértice a largura é zero", 0f, meiaLarguraDoHex(tam, -100f), 0.01f)
        assertEquals("fora do hexágono também", 0f, meiaLarguraDoHex(tam, -140f), 0.01f)
    }

    @Test
    fun `a altura onde o nome fica tem largura utilizavel`() {
        // O nome mora a −0,66·tam. Precisa sobrar largura de verdade ali, senão ele vira "pa...".
        val tam = 100f
        val m = meiaLarguraDoHex(tam, -66f)
        assertTrue("largura insuficiente para o nome: $m", m > tam * 0.4f)
    }

    @Test
    fun `os hexagonos de recurso cabem DENTRO do hexagono do token`() {
        // Regressão do bug do aparelho: eles vazavam pelos cantos de baixo, que são pontudos.
        val tam = 100f
        val tamMini = tam * 0.26f
        val dx = tam * 0.47f
        val dy = tam * 0.38f
        // Ponto mais à direita do mini-hex fica na altura do centro dele (hex pointy-top).
        val direitaMini = dx + tamMini * (SQRT3 / 2f)
        assertTrue("vaza na horizontal: $direitaMini > ${meiaLarguraDoHex(tam, dy)}",
            direitaMini <= meiaLarguraDoHex(tam, dy))
        // Vértice INFERIOR do mini-hex: x = dx, y = dy + tamMini.
        val baseY = dy + tamMini
        assertTrue("vaza embaixo: $dx > ${meiaLarguraDoHex(tam, baseY)}",
            dx <= meiaLarguraDoHex(tam, baseY))
    }

    @Test
    fun `o numero some no token pequeno e aparece no grande`() {
        // Decisão do usuário: abaixo do limiar mantém a cor e tira o número, que viraria borrão.
        assertFalse("token minúsculo não pode tentar escrever número",
            mostraNumeroDeRecurso(TAM_MIN_NUMERO_TOKEN - 1f))
        assertTrue(mostraNumeroDeRecurso(TAM_MIN_NUMERO_TOKEN))
        assertTrue(mostraNumeroDeRecurso(TAM_MIN_NUMERO_TOKEN + 10f))
    }
}
