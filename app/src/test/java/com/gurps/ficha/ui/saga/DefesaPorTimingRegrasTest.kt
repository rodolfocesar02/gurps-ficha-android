package com.gurps.ficha.ui.saga

import com.gurps.ficha.domain.combat.CombatResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote HEX-9: regras puras da janela de defesa por timing (Clair Obscur-like). */
class DefesaPorTimingRegrasTest {

    private fun opcao(tipo: CombatResolver.TipoDefesa = CombatResolver.TipoDefesa.ESQUIVA,
                     valor: Int = 9, disponivel: Boolean = true) =
        CombatResolver.OpcaoDefesa(tipo, valor, emptyList(), disponivel)

    @Test
    fun `abaixo de 300ms devolve mais 1 perfeito`() {
        val b = DefesaPorTimingRegras.bonusPorTempoMs(150L)
        assertEquals(+1, b.delta)
        assertEquals("perfeito", b.rotulo)
    }

    @Test
    fun `entre 300 e 600ms devolve zero bom`() {
        val b = DefesaPorTimingRegras.bonusPorTempoMs(450L)
        assertEquals(0, b.delta)
        assertEquals("bom", b.rotulo)
    }

    @Test
    fun `entre 600 e 1000ms devolve menos 1 tarde`() {
        val b = DefesaPorTimingRegras.bonusPorTempoMs(800L)
        assertEquals(-1, b.delta)
        assertEquals("tarde", b.rotulo)
    }

    @Test
    fun `a partir de 1000ms devolve BONUS_EXPIRADO por identidade`() {
        val b = DefesaPorTimingRegras.bonusPorTempoMs(1500L)
        assertSame(DefesaPorTimingRegras.BONUS_EXPIRADO, b)
    }

    @Test
    fun `aplicarBonus com delta positivo soma no valorFinal e adiciona ComponenteMod`() {
        val op = opcao(valor = 9)
        val res = DefesaPorTimingRegras.aplicarBonus(op, DefesaPorTimingRegras.Bonus(+1, "perfeito"))
        assertEquals(10, res.valorFinal)
        assertTrue(res.componentes.any { it.nome.startsWith("timing") && it.valor == +1 })
    }

    @Test
    fun `aplicarBonus com delta negativo abate no valorFinal e adiciona ComponenteMod`() {
        val op = opcao(valor = 9)
        val res = DefesaPorTimingRegras.aplicarBonus(op, DefesaPorTimingRegras.Bonus(-1, "tarde"))
        assertEquals(8, res.valorFinal)
        assertTrue(res.componentes.any { it.nome.startsWith("timing") && it.valor == -1 })
    }

    @Test
    fun `aplicarBonus com delta zero mantem opcao original (sem componente extra)`() {
        val op = opcao(valor = 9)
        val res = DefesaPorTimingRegras.aplicarBonus(op, DefesaPorTimingRegras.Bonus(0, "bom"))
        assertEquals(9, res.valorFinal)
        assertEquals(op.componentes, res.componentes)
    }

    @Test
    fun `aplicarBonus com EXPIRADO nao altera opcao`() {
        val op = opcao(valor = 9)
        val res = DefesaPorTimingRegras.aplicarBonus(op, DefesaPorTimingRegras.BONUS_EXPIRADO)
        assertEquals(9, res.valorFinal)
        assertEquals(op.componentes, res.componentes)
    }

    @Test
    fun `opcaoPadrao escolhe a maior valorFinal entre as disponiveis`() {
        val opcoes = listOf(
            opcao(CombatResolver.TipoDefesa.ESQUIVA, 9),
            opcao(CombatResolver.TipoDefesa.APARA, 11),
            opcao(CombatResolver.TipoDefesa.BLOQUEIO, 10)
        )
        val padrao = DefesaPorTimingRegras.opcaoPadrao(opcoes)
        assertEquals(11, padrao?.valorFinal)
        assertEquals(CombatResolver.TipoDefesa.APARA, padrao?.tipo)
    }

    @Test
    fun `opcaoPadrao ignora indisponivel mesmo com maior valorFinal`() {
        val opcoes = listOf(
            opcao(CombatResolver.TipoDefesa.ESQUIVA, 9, disponivel = true),
            opcao(CombatResolver.TipoDefesa.APARA, 13, disponivel = false),
            opcao(CombatResolver.TipoDefesa.BLOQUEIO, 10, disponivel = true)
        )
        val padrao = DefesaPorTimingRegras.opcaoPadrao(opcoes)
        assertEquals(10, padrao?.valorFinal)
        assertEquals(CombatResolver.TipoDefesa.BLOQUEIO, padrao?.tipo)
    }

    @Test
    fun `opcaoPadrao devolve null quando nenhuma disponivel`() {
        val opcoes = listOf(opcao(disponivel = false), opcao(disponivel = false))
        assertNull(DefesaPorTimingRegras.opcaoPadrao(opcoes))
    }

    @Test
    fun `opcaoPadraoOuFallback usa disponivel primeiro`() {
        val opcoes = listOf(
            opcao(CombatResolver.TipoDefesa.APARA, 13, disponivel = false),
            opcao(CombatResolver.TipoDefesa.ESQUIVA, 9, disponivel = true)
        )
        val res = DefesaPorTimingRegras.opcaoPadraoOuFallback(opcoes)
        assertEquals(9, res?.valorFinal)
        assertEquals(CombatResolver.TipoDefesa.ESQUIVA, res?.tipo)
    }

    @Test
    fun `opcaoPadraoOuFallback cai na primeira indisponivel quando nenhuma disponivel`() {
        val opcoes = listOf(
            opcao(CombatResolver.TipoDefesa.APARA, 13, disponivel = false),
            opcao(CombatResolver.TipoDefesa.ESQUIVA, 9, disponivel = false)
        )
        val res = DefesaPorTimingRegras.opcaoPadraoOuFallback(opcoes)
        assertEquals(13, res?.valorFinal)
        assertEquals(CombatResolver.TipoDefesa.APARA, res?.tipo)
    }

    @Test
    fun `opcaoPadraoOuFallback devolve null so quando a lista e vazia`() {
        assertNull(DefesaPorTimingRegras.opcaoPadraoOuFallback(emptyList()))
    }
}
