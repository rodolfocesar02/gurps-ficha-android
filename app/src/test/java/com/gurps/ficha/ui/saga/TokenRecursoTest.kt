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

    @Test
    fun `o numero some no token pequeno e aparece no grande`() {
        // Decisão do usuário: abaixo do limiar mantém a cor e tira o número, que viraria borrão.
        assertFalse("token minúsculo não pode tentar escrever número",
            mostraNumeroDeRecurso(TAM_MIN_NUMERO_TOKEN - 1f))
        assertTrue(mostraNumeroDeRecurso(TAM_MIN_NUMERO_TOKEN))
        assertTrue(mostraNumeroDeRecurso(TAM_MIN_NUMERO_TOKEN + 10f))
    }
}
