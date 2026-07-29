package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Visualização (MB p.99) — três regras de arredondamento, duas delas exceções.
 */
class VisualizacaoRulesTest {

    private val identico = VisualizacaoRules.Semelhanca.QUASE_IDENTICO
    private val parecido = VisualizacaoRules.Semelhanca.PARECIDO
    private val diferente = VisualizacaoRules.Semelhanca.MUITO_DIFERENTE

    @Test
    fun `quase identico da a margem inteira`() {
        assertEquals(6, VisualizacaoRules.bonusDe(6, identico))
        assertEquals(1, VisualizacaoRules.bonusDe(1, identico))
    }

    @Test
    fun `parecido divide por 2 e DESCARTA a fracao`() {
        // Divisão de GURPS descarta a fração: 5 ÷ 2 = 2, não 2,5 nem 3.
        assertEquals(3, VisualizacaoRules.bonusDe(6, parecido))
        assertEquals(2, VisualizacaoRules.bonusDe(5, parecido))
    }

    @Test
    fun `⚠️ parecido tem PISO de mais 1`() {
        // "o bônus é reduzido pela metade (no mínimo +1)". Margem 1 daria 0 pela
        // conta, mas o livro garante +1.
        assertEquals(1, VisualizacaoRules.bonusDe(1, parecido))
        val texto = VisualizacaoRules.explicacao(1, parecido)
        assertTrue(texto, texto.contains("mínimo é +1"))
    }

    @Test
    fun `⚠️ muito diferente divide por 3 e NAO tem piso`() {
        // "divida o bônus por 3 (sem um valor mínimo)". Aqui o zero é permitido --
        // e é justamente a diferença em relação ao caso de cima. Aplicar o piso
        // dos dois lados daria bônus onde o livro não dá.
        assertEquals(0, VisualizacaoRules.bonusDe(2, diferente))
        assertEquals(1, VisualizacaoRules.bonusDe(3, diferente))
        assertEquals(2, VisualizacaoRules.bonusDe(6, diferente))
        val texto = VisualizacaoRules.explicacao(2, diferente)
        assertTrue(texto, texto.contains("não há mínimo"))
    }

    @Test
    fun `os tres casos com a mesma margem, lado a lado`() {
        // Margem 7: 7 / 3 / 2. É a comparação que mostra por que vale a pena o
        // app fazer a conta.
        assertEquals(7, VisualizacaoRules.bonusDe(7, identico))
        assertEquals(3, VisualizacaoRules.bonusDe(7, parecido))
        assertEquals(2, VisualizacaoRules.bonusDe(7, diferente))
    }

    @Test
    fun `fracasso no teste de IQ nao da bonus em nenhum dos tres casos`() {
        VisualizacaoRules.Semelhanca.entries.forEach {
            assertEquals(it.name, 0, VisualizacaoRules.bonusDe(0, it))
            assertEquals(it.name, 0, VisualizacaoRules.bonusDe(-4, it))
        }
        assertTrue(VisualizacaoRules.explicacao(-1, identico).contains("Fracassou"))
    }

    @Test
    fun `a vantagem gate o painel`() {
        val sem = Personagem(nome = "T")
        assertFalse(VisualizacaoRules.tem(sem))

        val com = Personagem(
            nome = "T",
            vantagens = listOf(VantagemSelecionada(definicaoId = VisualizacaoRules.ID, nome = "Visualização"))
        )
        assertTrue(VisualizacaoRules.tem(com))
    }

    @Test
    fun `o aviso de combate esta escrito`() {
        // "Isso a torna inútil durante um combate" -- um minuto de concentracao
        // nao cabe num turno de um segundo.
        assertTrue(VisualizacaoRules.AVISO_COMBATE.contains("combate"))
    }

    @Test
    fun `a explicacao mostra a conta em todos os casos com bonus`() {
        assertTrue(VisualizacaoRules.explicacao(6, identico).contains("Margem 6"))
        assertTrue(VisualizacaoRules.explicacao(6, parecido).contains("÷ 2"))
        assertTrue(VisualizacaoRules.explicacao(6, diferente).contains("÷ 3"))
    }
}
