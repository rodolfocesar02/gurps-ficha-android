package com.gurps.ficha.domain.magic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lotes C12 (ritual alternativo, Magia p.9) e C11 (área encolhe mas não expande, Magia p.10).
 */
class RitualEAreaTest {

    // ── C12: ritual alternativo ────────────────────────────────────────────────────────────────

    @Test
    fun `o ritual PADRAO nao modifica nada`() {
        val r = RitualDeConjuracao()
        assertEquals(0, r.modificador)
        assertTrue(r.ehPadrao)
        assertEquals("descrição vazia quando nada foge do padrão", "", r.descricao())
    }

    @Test
    fun `cada omissao tem o valor que o livro manda`() {
        assertEquals(-2, RitualDeConjuracao(gesto = GestoDoRitual.UMA_MAO).modificador)
        assertEquals(-4, RitualDeConjuracao(gesto = GestoDoRitual.SEM_GESTOS).modificador)
        assertEquals(-2, RitualDeConjuracao(voz = VozDoRitual.SUAVE).modificador)
        assertEquals(-4, RitualDeConjuracao(voz = VozDoRitual.EM_SILENCIO).modificador)
        assertEquals(-2, RitualDeConjuracao(passos = false).modificador)
    }

    @Test
    fun `as penalidades SOMAM — amarrado e amordacado da menos 8`() {
        val r = RitualDeConjuracao(gesto = GestoDoRitual.SEM_GESTOS, voz = VozDoRitual.EM_SILENCIO)
        assertEquals(-8, r.modificador)
    }

    @Test
    fun `caprichar da mais 1 e DOBRA o tempo de operacao`() {
        // O +1 não é de graça: "se o mágico tiver tempo para ser particularmente preciso [...]
        // dobrando o Tempo de Operação, ele recebe +1 em seu NH efetivo".
        val r = RitualDeConjuracao(caprichado = true)
        assertEquals(1, r.modificador)
        assertEquals(6, r.tempoAjustado(3))
        assertEquals("tempo mínimo continua 1s", 2, r.tempoAjustado(1))
        assertEquals("sem caprichar o tempo não muda", 3, RitualDeConjuracao().tempoAjustado(3))
    }

    @Test
    fun `caprichar compensa em parte uma omissao`() {
        val r = RitualDeConjuracao(gesto = GestoDoRitual.UMA_MAO, caprichado = true)
        assertEquals(-1, r.modificador)
        assertFalse(r.ehPadrao)
    }

    @Test
    fun `o ritual ENTRA no NH efetivo como parcela nomeada`() {
        // Não basta calcular: tem que chegar ao NH que o motor usa, e com nome, senão o jogador vê
        // um −4 sem explicação. É a lição do MEC-14 aplicada a este lote.
        val ctx = ContextoConjuracao(
            nhBasico = 15, classe = MagicClassParser.parse("Comum"), mana = NivelMana.NORMAL,
            tocando = true,
            ritual = RitualDeConjuracao(gesto = GestoDoRitual.SEM_GESTOS)
        )
        val nh = MagicCasting.nhEfetivo(ctx)
        assertEquals(11, nh.valor)
        assertTrue("a parcela tem que estar nomeada: ${nh.componentes}",
            nh.componentes.any { it.motivo.contains("sem gestos") && it.valor == -4 })
    }

    @Test
    fun `ritual padrao NAO polui a lista de componentes`() {
        val ctx = ContextoConjuracao(
            nhBasico = 15, classe = MagicClassParser.parse("Comum"), mana = NivelMana.NORMAL,
            tocando = true
        )
        val nh = MagicCasting.nhEfetivo(ctx)
        assertEquals(15, nh.valor)
        assertTrue(nh.componentes.none { it.motivo.contains("gestos") || it.motivo.contains("entoado") })
    }
}
