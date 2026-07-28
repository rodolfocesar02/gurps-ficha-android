package com.gurps.ficha.ui.features.traits

import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry.OrigemDeBonus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre o texto que explica de onde vem o bônus de uma perícia (Lote NOTA-1).
 *
 * Sem essa linha, o NH muda sozinho e nada na tela justifica — a automação vira
 * caixa preta e o jogador perde como conferir se a conta está certa.
 *
 * O formato é testado aqui, fora do Compose, porque é onde mora a decisão: uma
 * origem mostra o nome direto; várias mostram o total com a decomposição.
 */
class OrigemDoBonusTest {

    @Test
    fun `uma origem mostra valor e nome`() {
        assertEquals(
            "+2 Pendulear",
            textoDeOrigem(listOf(OrigemDeBonus("Pendulear", 2)))
        )
    }

    @Test
    fun `origem negativa mostra o sinal de menos`() {
        assertEquals(
            "-2 Gordo",
            textoDeOrigem(listOf(OrigemDeBonus("Gordo", -2)))
        )
    }

    @Test
    fun `varias origens mostram o total e a decomposicao`() {
        assertEquals(
            "+3 (Pendulear +2, Reflexos em Combate +1)",
            textoDeOrigem(
                listOf(
                    OrigemDeBonus("Pendulear", 2),
                    OrigemDeBonus("Reflexos em Combate", 1)
                )
            )
        )
    }

    @Test
    fun `vantagem e desvantagem juntas mostram o saldo`() {
        // O jogador precisa ver que o -2 existe, mesmo que o saldo seja positivo.
        assertEquals(
            "+1 (Bênção +3, Gordo -2)",
            textoDeOrigem(
                listOf(
                    OrigemDeBonus("Bênção", 3),
                    OrigemDeBonus("Gordo", -2)
                )
            )
        )
    }

    @Test
    fun `saldo zero ainda mostra as origens`() {
        // Somar zero nao e o mesmo que nao ter bonus: o jogador tem que saber
        // que ha duas forcas se anulando.
        assertEquals(
            "+0 (Bênção +2, Gordo -2)",
            textoDeOrigem(
                listOf(
                    OrigemDeBonus("Bênção", 2),
                    OrigemDeBonus("Gordo", -2)
                )
            )
        )
    }

    @Test
    fun `sem origem nao gera texto`() {
        assertEquals("", textoDeOrigem(emptyList()))
    }

    // --- acessibilidade ---

    @Test
    fun `descricao acessivel escreve por extenso`() {
        // Quem ouve nao ve "+2": precisa de palavra.
        val texto = descricaoAcessivelDeOrigem(listOf(OrigemDeBonus("Pendulear", 2)))
        assertTrue(texto.contains("mais 2 de Pendulear"))
    }

    @Test
    fun `descricao acessivel diz menos para penalidade`() {
        val texto = descricaoAcessivelDeOrigem(listOf(OrigemDeBonus("Gordo", -2)))
        assertTrue(texto.contains("menos 2 de Gordo"))
        assertTrue("nao pode vazar o sinal cru", !texto.contains("-2"))
    }

    @Test
    fun `sem origem nao acrescenta nada a descricao do card`() {
        assertEquals("", descricaoAcessivelDeOrigem(emptyList()))
    }

    // --- unidade: o bônus de dano é POR DADO (Lote NOTA-2) ---

    @Test
    fun `bonus de dano diz por dado`() {
        // "+1 Mestre de Armas" numa arma de 3d seria mentira: o ganho e +3.
        assertEquals(
            "+1/dado Mestre de Armas",
            textoDeOrigem(listOf(OrigemDeBonus("Mestre de Armas", 1)), unidade = "/dado")
        )
    }

    @Test
    fun `com varias origens a unidade aparece em cada parcela e no total`() {
        val texto = textoDeOrigem(
            listOf(OrigemDeBonus("Mestre de Armas", 1), OrigemDeBonus("Dom da Lâmina", 1)),
            unidade = "/dado"
        )
        assertEquals("+2/dado (Mestre de Armas +1/dado, Dom da Lâmina +1/dado)", texto)
    }

    @Test
    fun `sem unidade o texto continua exatamente como era`() {
        // Garante que o parametro novo nao mexeu no formato do NOTA-1.
        assertEquals("+2 Pendulear", textoDeOrigem(listOf(OrigemDeBonus("Pendulear", 2))))
    }
}
