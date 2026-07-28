package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mira: onde acertar e quanto custa (Lote MIRA-1, MB p.398-400).
 *
 * O exemplo do usuário virou teste: *"se o NH de Faca é 12, e você quer acertar
 * o olho (−9), mostra 3"*.
 */
class MiraRulesTest {

    private fun opcao(rotulo: String, desarmar: Boolean = false) =
        MiraRules.opcoes(desarmar).first { it.rotulo == rotulo }

    // --- a tabela do livro ---

    @Test
    fun `as penalidades sao as do Modulo Basico`() {
        assertEquals(0, opcao("Torso").penalidade)
        assertEquals(-2, opcao("Braço").penalidade)
        assertEquals(-2, opcao("Perna").penalidade)
        assertEquals(-3, opcao("Vitais").penalidade)
        assertEquals(-3, opcao("Virilha").penalidade)
        assertEquals(-4, opcao("Mão").penalidade)
        assertEquals(-4, opcao("Pé").penalidade)
        assertEquals(-5, opcao("Rosto").penalidade)
        assertEquals(-5, opcao("Pescoço").penalidade)
        assertEquals(-7, opcao("Crânio").penalidade)
        assertEquals(-9, opcao("Olho").penalidade)
    }

    @Test
    fun `o escudo torna braco e mao mais dificeis`() {
        // MB p.399: braço com escudo -4 (em vez de -2), mão com escudo -8.
        assertEquals(-4, opcao("Braço com escudo").penalidade)
        assertEquals(-8, opcao("Mão com escudo").penalidade)
    }

    @Test
    fun `a arma do oponente usa o ALCANCE dela, nao o tamanho da sua`() {
        // MB p.400. O usuario havia anotado -4/-3/-2; o livro diz -5/-4/-3.
        assertEquals(-5, opcao("Arma pequena").penalidade)
        assertEquals(-4, opcao("Arma média").penalidade)
        assertEquals(-3, opcao("Arma grande").penalidade)
    }

    // --- o cálculo que aparece na tela ---

    @Test
    fun `o exemplo do usuario - Faca NH 12 mirando o olho da 3`() {
        assertEquals(3, opcao("Olho").nhCom(12))
    }

    @Test
    fun `o torso nao muda o NH - e o alvo padrao`() {
        assertEquals(12, opcao("Torso").nhCom(12))
    }

    @Test
    fun `NH pode ficar negativo, e isso e informacao`() {
        // Faca NH 5 mirando o olho da -4. Esconder isso seria pior: o jogador
        // precisa ver que aquele alvo esta fora de alcance.
        assertEquals(-4, opcao("Olho").nhCom(5))
    }

    // --- desarmar ---

    @Test
    fun `desarmar tira mais 2, so das armas`() {
        assertEquals(-7, opcao("Arma pequena", desarmar = true).penalidade)
        assertEquals(-6, opcao("Arma média", desarmar = true).penalidade)
        assertEquals(-5, opcao("Arma grande", desarmar = true).penalidade)
    }

    @Test
    fun `desarmar NAO mexe em nenhum alvo do corpo`() {
        // "Desarmar o cranio" nao existe. Se o -2 vazasse para o corpo, todo
        // ataque mirado ficaria errado enquanto a caixinha estivesse marcada.
        MiraRules.opcoes(desarmar = true)
            .filter { it.grupo == MiraRules.Grupo.CORPO }
            .forEach { comDesarmar ->
                val sem = opcao(comDesarmar.rotulo)
                assertEquals(
                    "${comDesarmar.rotulo} mudou com o desarmar",
                    sem.penalidade, comDesarmar.penalidade
                )
            }
    }

    // --- a lista ---

    @Test
    fun `a lista vem do mais facil para o mais dificil`() {
        MiraRules.Grupo.values().forEach { grupo ->
            val penalidades = MiraRules.opcoes().filter { it.grupo == grupo }.map { it.penalidade }
            assertEquals("$grupo fora de ordem", penalidades.sortedDescending(), penalidades)
        }
    }

    @Test
    fun `todo alvo explica o efeito, nao so o numero`() {
        // A escolha real e pelo EFEITO: mirar no cranio vale a pena pelo x4 de
        // ferimento, nao apesar do -7.
        MiraRules.opcoes().forEach {
            assertTrue("${it.rotulo} sem detalhe", !it.detalhe.isNullOrBlank())
        }
    }

    @Test
    fun `a descricao acessivel le o negativo por extenso`() {
        val d = opcao("Olho").descricaoAcessivel(5)
        assertTrue(d, d.contains("menos 4"))
        assertTrue("nao pode vazar o sinal cru", !d.contains("-4"))
    }

    @Test
    fun `o enum de local continua com os valores de sempre`() {
        // Ele mudou de pacote em 28/07; os valores nao podiam mudar junto.
        assertEquals(-7, LocalAtaque.CRANIO.penalidadeAtaque)
        assertEquals(-9, LocalAtaque.OLHO.penalidadeAtaque)
        assertEquals(0, LocalAtaque.TORSO.penalidadeAtaque)
        assertEquals(11, LocalAtaque.values().size)
    }
}
