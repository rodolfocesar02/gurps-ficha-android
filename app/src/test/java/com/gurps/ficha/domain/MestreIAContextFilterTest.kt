package com.gurps.ficha.domain

import com.gurps.ficha.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lote 351: reescrito para o contrato ATUAL do MestreIAContextFilter.
 * A versão antiga testava uma implementação que retornava JSON com truncamento e
 * contagem de pontos — substituída há muitos lotes pelo formato TEXTO compacto
 * (Token Economy, Lote 53+). Estes testes documentam o formato vigente.
 */
class MestreIAContextFilterTest {

    @Test
    fun contextoContemCabecalhoAtributosEStatus() {
        val p = Personagem(nome = "Test")
        p.forca = 12

        val contexto = MestreIAContextFilter.gerarContexto(p, "geracao")

        assertTrue(contexto.contains("--- FICHA ATUAL ---"))
        assertTrue(contexto.contains("Nome: Test"))
        assertTrue(contexto.contains("ST 12"))
        assertTrue(contexto.contains("HP: "))
        assertTrue(contexto.contains("FP: "))
    }

    @Test
    fun modoConversaIncluiAparenciaEHistorico() {
        val p = Personagem(nome = "Test", historico = "Nasceu em Aldeia Verde", aparencia = "Alto e magro")

        val contexto = MestreIAContextFilter.gerarContexto(p, "conversa")

        assertTrue(contexto.contains("Histórico: Nasceu em Aldeia Verde"))
        assertTrue(contexto.contains("Aparência: Alto e magro"))
    }

    @Test
    fun modoGeracaoNaoIncluiAparenciaNemHistorico() {
        val p = Personagem(nome = "Test", historico = "Nasceu em Aldeia Verde", aparencia = "Alto e magro")

        val contexto = MestreIAContextFilter.gerarContexto(p, "geracao")

        assertFalse(contexto.contains("Histórico:"))
        assertFalse(contexto.contains("Aparência:"))
    }

    @Test
    fun listasDeTracosAparecemPorNomeESomemQuandoVazias() {
        val comTracos = Personagem(nome = "Legolas")
        comTracos.vantagens = listOf(VantagemSelecionada(nome = "Visão Aguçada", nivel = 2))
        comTracos.desvantagens = listOf(DesvantagemSelecionada(nome = "Excesso de Confiança", nivel = 1))

        val contexto = MestreIAContextFilter.gerarContexto(comTracos, "conversa")
        assertTrue(contexto.contains("Vantagens: Visão Aguçada"))
        assertTrue(contexto.contains("Desvantagens: Excesso de Confiança"))

        val semTracos = Personagem(nome = "Vazio")
        val contextoVazio = MestreIAContextFilter.gerarContexto(semTracos, "conversa")
        assertFalse(contextoVazio.contains("Vantagens:"))
        assertFalse(contextoVazio.contains("Desvantagens:"))
    }

    @Test
    fun periciasLimitadasAQuinzeComNH() {
        val p = Personagem(nome = "Estudioso")
        p.pericias = (1..20).map { i ->
            PericiaSelecionada(
                definicaoId = "pericia_$i",
                nome = "Pericia $i",
                atributoBase = AtributoBase.IQ,
                dificuldade = Dificuldade.MEDIA,
                pontosGastos = 4
            )
        }

        val contexto = MestreIAContextFilter.gerarContexto(p, "geracao")
        val linhaPericias = contexto.lines().first { it.startsWith("Perícias Principais:") }

        // take(15): as 15 primeiras entram, da 16ª em diante ficam de fora.
        assertEquals(15, Regex("Pericia \\d+ \\(NH ").findAll(linhaPericias).count())
        assertTrue(linhaPericias.contains("Pericia 15 (NH"))
        assertFalse(linhaPericias.contains("Pericia 16 (NH"))
    }
}
