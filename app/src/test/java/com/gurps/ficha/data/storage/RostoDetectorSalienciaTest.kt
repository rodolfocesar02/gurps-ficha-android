package com.gurps.ficha.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre [RostoDetector.primeiraLinhaDoAssunto] — a heurística que salva o
 * enquadramento quando o ML Kit não acha rosto na arte (pintura digital,
 * criatura, 3/4 de perfil). Sem ela o recorte volta a colar no topo da imagem
 * e corta o rosto na altura dos olhos.
 *
 * Só a matemática é testada aqui: a parte Android (Bitmap/ML Kit) não roda em
 * teste unitário puro, e é por isso que ela foi separada.
 */
class RostoDetectorSalienciaTest {

    /** Fundo liso em cima, assunto detalhado a partir de [inicio]. */
    private fun perfil(tamanho: Int, inicio: Int, fundo: Float = 5f, assunto: Float = 100f) =
        FloatArray(tamanho) { if (it >= inicio) assunto else fundo }

    @Test
    fun `acha o topo do assunto num retrato com fundo liso`() {
        val energia = perfil(tamanho = 100, inicio = 30)
        assertEquals(30, RostoDetector.primeiraLinhaDoAssunto(energia))
    }

    @Test
    fun `busto que ja comeca no topo devolve a primeira linha`() {
        // Close de rosto: detalhe já na linha 0, fundo liso só no rodapé.
        val energia = FloatArray(100) { if (it < 60) 100f else 5f }
        assertEquals(0, RostoDetector.primeiraLinhaDoAssunto(energia))
    }

    @Test
    fun `respingo isolado de textura no fundo nao vira cabeca`() {
        // Uma única linha ruidosa aos 10 não se sustenta; o assunto real é aos 40.
        val energia = perfil(tamanho = 100, inicio = 40)
        energia[10] = 100f
        assertEquals(40, RostoDetector.primeiraLinhaDoAssunto(energia))
    }

    @Test
    fun `ruido sustentado de fundo nao dispara abaixo do limiar`() {
        // Fundo com variação pequena (até 20% do pico) não é assunto.
        val energia = perfil(tamanho = 100, inicio = 50)
        for (y in 5 until 20) energia[y] = 20f
        assertEquals(50, RostoDetector.primeiraLinhaDoAssunto(energia))
    }

    @Test
    fun `imagem sem contraste nao devolve palpite`() {
        assertNull(RostoDetector.primeiraLinhaDoAssunto(FloatArray(100) { 42f }))
    }

    @Test
    fun `amostra curta demais nao devolve palpite`() {
        assertNull(RostoDetector.primeiraLinhaDoAssunto(perfil(tamanho = 5, inicio = 2)))
    }

    @Test
    fun `transicao suave ainda cai acima do meio da imagem`() {
        // Cabelo/silhueta entram em degradê em vez de degrau seco.
        val energia = FloatArray(120) { y ->
            when {
                y < 25 -> 4f
                y < 40 -> 4f + (y - 25) * 6f   // sobe até 94
                else -> 100f
            }
        }
        val topo = RostoDetector.primeiraLinhaDoAssunto(energia)!!
        assertTrue("topo=$topo deveria ficar na subida, entre 25 e 40", topo in 25..40)
    }
}
