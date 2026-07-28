package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.features.rolagem.apenasInteiroComSinal
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * PV e PF podem ficar **negativos** na ficha.
 *
 * Bug achado no aparelho em 28/07: *"não consigo descer o PV pra −10, apenas
 * até 0"*.
 *
 * O piso estava em zero, e isso travava o marco mais comum do GURPS. O
 * personagem vive abaixo de zero: a 0 PV testa HT a cada turno para não
 * desmaiar; a cada múltiplo negativo do PV máximo testa para não morrer; a −5×
 * morre de vez (MB p.423). Com o piso em zero **nada disso podia ser
 * registrado**, e o teste de morte criado no Lote MARCOS-1 era inalcançável.
 *
 * Havia um segundo bloqueio, no caminho do teclado: o filtro do campo era
 * `filter { it.isDigit() }`, que **come o sinal de menos**. Mesmo com o piso
 * corrigido, não dava para digitar −10.
 */
class PvPfNegativoTest {

    private val delegate = FichaAttributeDelegate()
    private val heroi = Personagem(nome = "Teste", forca = 10, vitalidade = 10)

    private fun pvApos(valor: Int) =
        delegate.atualizarPontosVidaRolagemAtual(heroi, valor).pontosVidaRolagemAtual

    private fun pfApos(valor: Int) =
        delegate.atualizarPontosFadigaRolagemAtual(heroi, valor).pontosFadigaRolagemAtual

    // --- o piso ---

    @Test
    fun `PV negativo e aceito`() {
        assertEquals(-10, pvApos(-10))
        assertEquals(-1, pvApos(-1))
    }

    @Test
    fun `o piso do PV e a morte automatica, menos 5 vezes o maximo`() {
        // PV max 10 -> piso -50. O livro nao tem regra abaixo disso.
        assertEquals(-50, pvApos(-50))
        assertEquals(-50, pvApos(-999))
    }

    @Test
    fun `PF negativo e aceito ate menos 1 vez o maximo`() {
        // MB p.426: a -1x o PF maximo o personagem desmaia.
        assertEquals(-10, pfApos(-10))
        assertEquals(-10, pfApos(-999))
    }

    @Test
    fun `o teto continua valendo`() {
        assertEquals(50, pvApos(999))
    }

    @Test
    fun `nulo continua limpando o valor`() {
        assertEquals(null, delegate.atualizarPontosVidaRolagemAtual(heroi, null).pontosVidaRolagemAtual)
    }

    // --- o filtro do teclado ---

    @Test
    fun `o campo aceita o sinal de menos`() {
        assertEquals("-10", apenasInteiroComSinal("-10"))
    }

    @Test
    fun `o campo continua recusando letra`() {
        assertEquals("12", apenasInteiroComSinal("1a2b"))
    }

    @Test
    fun `o menos so vale na primeira posicao`() {
        // "1-2" nao e numero; vira 12, nao -12.
        assertEquals("12", apenasInteiroComSinal("1-2"))
    }

    @Test
    fun `campo vazio continua vazio`() {
        assertEquals("", apenasInteiroComSinal(""))
        assertEquals("-", apenasInteiroComSinal("-"))
    }
}
