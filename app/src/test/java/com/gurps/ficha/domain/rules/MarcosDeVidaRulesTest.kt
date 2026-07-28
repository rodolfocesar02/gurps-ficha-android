package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Marcos de PV e PF (Lote MARCOS-1, MB p.419-423).
 *
 * O gatilho é a queda do PV **na ficha** — o jeito como o usuário joga, via
 * Discord. Não depende do combate tático.
 *
 * O que mais importa aqui é o que **não** dispara: cura, ajuste do máximo, e
 * queda que não cruza marco nenhum. Um app que pede teste à toa vira ruído e o
 * jogador para de olhar.
 */
class MarcosDeVidaRulesTest {

    private fun heroi(
        pvMax: Int = 10,
        ht: Int = 10,
        vantagens: List<VantagemSelecionada> = emptyList()
    ) = Personagem(
        nome = "Teste",
        forca = pvMax,          // PV = ST por padrão
        vitalidade = ht,
        vantagens = vantagens
    )

    private fun dificilDeSubjugar(nivel: Int) = VantagemSelecionada(
        definicaoId = "dificil_de_subjugar", nome = "Difícil de Subjugar", nivel = nivel
    )

    private fun duroDeMatar(nivel: Int) = VantagemSelecionada(
        definicaoId = "duro_de_matar", nome = "Duro de Matar", nivel = nivel
    )

    private fun boaForma(custo: Int) = VantagemSelecionada(
        definicaoId = "boa_forma", nome = "Boa Forma", custoEscolhido = custo
    )

    // --- o que NÃO dispara ---

    @Test
    fun `curar nunca exige teste`() {
        val p = heroi()
        assertTrue(MarcosDeVidaRules.testesAoPerderPv(p, pvAntes = 2, pvDepois = 8).isEmpty())
        assertTrue(MarcosDeVidaRules.testesAoPerderPv(p, pvAntes = -5, pvDepois = 1).isEmpty())
    }

    @Test
    fun `arranhao nao exige nada`() {
        // PV max 10: perder 1 nao chega a metade e nao cruza marco.
        val p = heroi()
        assertTrue(MarcosDeVidaRules.testesAoPerderPv(p, 10, 9).isEmpty())
    }

    @Test
    fun `continuar caindo dentro do cambaleante nao repete o teste`() {
        // Ja estava em 4; cair para 3 nao cruza marco novo.
        val p = heroi()
        assertTrue(MarcosDeVidaRules.testesAoPerderPv(p, 4, 3).isEmpty())
    }

    // --- ferimento grave ---

    @Test
    fun `perder metade do PV maximo num golpe exige teste`() {
        val p = heroi(pvMax = 10)
        val testes = MarcosDeVidaRules.testesAoPerderPv(p, 10, 5)
        assertEquals(1, testes.size)
        assertTrue(testes.first().rotulo.contains("Ferimento grave"))
        assertEquals(10, testes.first().alvo)   // HT 10, sem vantagem
    }

    @Test
    fun `Dificil de Subjugar soma no ferimento grave`() {
        val p = heroi(vantagens = listOf(dificilDeSubjugar(2)))
        val teste = MarcosDeVidaRules.testesAoPerderPv(p, 10, 5).first()
        assertEquals(12, teste.alvo)
        assertTrue(teste.origens.any { it.contains("Difícil de Subjugar") })
    }

    // --- consciência ---

    @Test
    fun `chegar a zero exige teste para manter a consciencia`() {
        val p = heroi()
        val testes = MarcosDeVidaRules.testesAoPerderPv(p, 3, 0)
        assertTrue(testes.any { it.rotulo.contains("consciência") })
    }

    @Test
    fun `ja estando abaixo de zero nao repete o marco de consciencia`() {
        // O teste de consciencia se repete a cada TURNO, mas isso e do combate.
        // Aqui o marco e o cruzamento -- de -2 para -3 nao cruza nada.
        val p = heroi()
        val testes = MarcosDeVidaRules.testesAoPerderPv(p, -2, -3)
        assertTrue(testes.none { it.rotulo.contains("consciência") })
    }

    @Test
    fun `Boa Forma entra em cima de qualquer teste de HT`() {
        val p = heroi(vantagens = listOf(dificilDeSubjugar(1), boaForma(15)))
        val teste = MarcosDeVidaRules.testesAoPerderPv(p, 3, 0)
            .first { it.rotulo.contains("consciência") }
        assertEquals("HT 10 + Dificil 1 + Boa Forma 2", 13, teste.alvo)
        assertEquals(2, teste.origens.size)
    }

    @Test
    fun `Boa Forma de 5 pontos da apenas mais 1`() {
        val p = heroi(vantagens = listOf(boaForma(5)))
        val teste = MarcosDeVidaRules.testesAoPerderPv(p, 3, 0).first()
        assertEquals(11, teste.alvo)
    }

    // --- morte ---

    @Test
    fun `passar de menos 1 vez o PV maximo exige teste de morte`() {
        val p = heroi(pvMax = 10)
        val testes = MarcosDeVidaRules.testesAoPerderPv(p, -9, -10)
        assertEquals(1, testes.size)
        assertTrue(testes.first().rotulo.contains("morte"))
    }

    @Test
    fun `Duro de Matar soma no teste de morte`() {
        val p = heroi(vantagens = listOf(duroDeMatar(3)))
        val teste = MarcosDeVidaRules.testesAoPerderPv(p, -9, -10).first()
        assertEquals(13, teste.alvo)
        assertTrue(teste.origens.any { it.contains("Duro de Matar") })
    }

    @Test
    fun `um golpe enorme cruza VARIOS multiplos e cada um exige seu teste`() {
        // A conta mais facil de errar: de 5 para -25 (PV max 10) passa por
        // -10, -20 -- dois marcos de morte, nao um.
        assertEquals(
            listOf(1, 2),
            MarcosDeVidaRules.marcosDeMorteCruzados(maximo = 10, pvAntes = 5, pvDepois = -25)
        )
    }

    @Test
    fun `Duro de Matar NAO entra no teste de consciencia`() {
        // Sao vantagens diferentes: uma e para nao desmaiar, a outra para nao
        // morrer. Trocar as duas seria dar bonus onde o livro nao da.
        val p = heroi(vantagens = listOf(duroDeMatar(3)))
        val teste = MarcosDeVidaRules.testesAoPerderPv(p, 3, 0)
            .first { it.rotulo.contains("consciência") }
        assertEquals("HT puro, sem Duro de Matar", 10, teste.alvo)
    }

    @Test
    fun `morte automatica a menos 5 vezes o PV maximo`() {
        val p = heroi(pvMax = 10)
        assertTrue(!MarcosDeVidaRules.morteAutomatica(p, -49))
        assertTrue(MarcosDeVidaRules.morteAutomatica(p, -50))
    }

    // --- estados (o lado do PF) ---

    @Test
    fun `ficha inteira nao mostra estado nenhum`() {
        val p = heroi()
        assertTrue(MarcosDeVidaRules.estadosDe(p, pvAtual = 10, pfAtual = 10).isEmpty())
    }

    @Test
    fun `um terco do PV e cambaleante`() {
        val p = heroi(pvMax = 10)
        val estados = MarcosDeVidaRules.estadosDe(p, pvAtual = 3, pfAtual = 10)
        assertEquals(1, estados.size)
        assertEquals("Cambaleante", estados.first().rotulo)
    }

    @Test
    fun `um terco do PF e cansado, e nao e teste - e aviso`() {
        val p = heroi()
        val estados = MarcosDeVidaRules.estadosDe(p, pvAtual = 10, pfAtual = 3)
        assertEquals(listOf("Cansado"), estados.map { it.rotulo })
        assertTrue(estados.first().efeito.contains("metade"))
    }

    @Test
    fun `PV e PF ruins ao mesmo tempo mostram os dois estados`() {
        val p = heroi()
        val estados = MarcosDeVidaRules.estadosDe(p, pvAtual = 0, pfAtual = 0)
        assertEquals(listOf("Caído", "Exausto"), estados.map { it.rotulo })
    }

    @Test
    fun `a descricao acessivel junta numero e motivo`() {
        val p = heroi(vantagens = listOf(duroDeMatar(2)))
        val d = MarcosDeVidaRules.testesAoPerderPv(p, -9, -10).first().descricaoAcessivel
        assertTrue(d, d.contains("Alvo 12"))
        assertTrue(d, d.contains("Duro de Matar"))
    }
}
