package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre os Testes de Autocontrole (Lote D-1).
 *
 * 35 desvantagens do catálogo têm Número de Autocontrole. O app já guardava o
 * NA (usava no multiplicador de custo) mas nunca rolava nada — o jogador tinha
 * de fazer na mão.
 */
class AutocontroleRulesTest {

    private fun desvantagem(nome: String, na: Int?, detalhe: String = "") =
        DesvantagemSelecionada(
            definicaoId = nome.lowercase(), nome = nome,
            autocontrole = na, descricao = detalhe
        )

    // --- quais aparecem ---

    @Test
    fun `ficha sem desvantagem nao mostra nada`() {
        val p = Personagem(nome = "Teste")
        assertTrue(AutocontroleRules.testesDisponiveis(p).isEmpty())
        assertFalse(AutocontroleRules.temAlgumTeste(p))
    }

    @Test
    fun `desvantagem com NA aparece com o valor`() {
        val p = Personagem(nome = "Teste", desvantagens = listOf(desvantagem("Fobia", 12)))
        val testes = AutocontroleRules.testesDisponiveis(p)
        assertEquals(1, testes.size)
        assertEquals(12, testes.first().na)
        assertEquals("Fobia", testes.first().nome)
    }

    @Test
    fun `desvantagem mental SEM NA nao aparece`() {
        // Sem numero nao ha o que rolar.
        val p = Personagem(nome = "Teste", desvantagens = listOf(desvantagem("Honestidade", null)))
        assertTrue(AutocontroleRules.testesDisponiveis(p).isEmpty())
    }

    @Test
    fun `NA fora dos valores do GURPS e ignorado`() {
        // Só 6, 9, 12 e 15 existem. Valor estranho e dado corrompido.
        val p = Personagem(nome = "Teste", desvantagens = listOf(desvantagem("Bug", 7)))
        assertTrue(AutocontroleRules.testesDisponiveis(p).isEmpty())
    }

    @Test
    fun `duas instancias da mesma desvantagem aparecem SEPARADAS`() {
        // Caso comum: duas Fobias diferentes, cada uma com seu NA. Agrupar
        // seria erro -- o jogador precisa rolar cada uma.
        val p = Personagem(
            nome = "Teste",
            desvantagens = listOf(
                desvantagem("Fobia", 12, "Altura"),
                desvantagem("Fobia", 6, "Aranhas")
            )
        )
        val testes = AutocontroleRules.testesDisponiveis(p)
        assertEquals(2, testes.size)
        assertEquals(setOf(12, 6), testes.map { it.na }.toSet())
        assertEquals(setOf("Fobia (Altura)", "Fobia (Aranhas)"), testes.map { it.rotulo }.toSet())
    }

    @Test
    fun `o indice aponta para a desvantagem certa na ficha`() {
        val p = Personagem(
            nome = "Teste",
            desvantagens = listOf(
                desvantagem("Sem NA", null),
                desvantagem("Avareza", 12)
            )
        )
        // Avareza e a segunda da lista -> indice 1, mesmo sendo a primeira
        // testavel.
        assertEquals(1, AutocontroleRules.testesDisponiveis(p).first().indice)
    }

    // --- as regras do teste ---

    @Test
    fun `Vontade NAO ajuda no autocontrole`() {
        // Erro classico de quem conhece outros testes do GURPS: o NA e FIXO.
        val fraco = Personagem(nome = "A", inteligencia = 8, modVontade = -2,
            desvantagens = listOf(desvantagem("Avareza", 12)))
        val forte = Personagem(nome = "B", inteligencia = 18, modVontade = 6,
            desvantagens = listOf(desvantagem("Avareza", 12)))

        assertEquals(
            AutocontroleRules.testesDisponiveis(fraco).first().na,
            AutocontroleRules.testesDisponiveis(forte).first().na
        )
    }

    @Test
    fun `modificador situacional do Mestre entra no alvo`() {
        assertEquals(10, AutocontroleRules.alvoEfetivo(na = 12, modificadorSituacional = -2))
        assertEquals(14, AutocontroleRules.alvoEfetivo(na = 12, modificadorSituacional = 2))
        assertEquals(12, AutocontroleRules.alvoEfetivo(na = 12))
    }

    @Test
    fun `alvo fica dentro do que 3d6 pode rolar`() {
        // Fora de 3..18 o teste seria automatico; travar evita numero absurdo.
        assertEquals(3, AutocontroleRules.alvoEfetivo(na = 6, modificadorSituacional = -99))
        assertEquals(18, AutocontroleRules.alvoEfetivo(na = 15, modificadorSituacional = 99))
    }

    // --- a "notinha" ---

    @Test
    fun `a explicacao deixa claro que NA baixo e pior`() {
        // Contraintuitivo: no resto do GURPS numero alto e melhor.
        assertTrue(AutocontroleRules.explicacaoDo(6).contains("raramente"))
        assertTrue(AutocontroleRules.explicacaoDo(15).contains("quase sempre"))
    }

    @Test
    fun `a explicacao sempre diz como rolar`() {
        listOf(6, 9, 12, 15).forEach { na ->
            val t = AutocontroleRules.explicacaoDo(na)
            assertTrue("NA $na sem instrucao: $t", t.contains("3d6") && t.contains("$na"))
        }
    }

    @Test
    fun `desvantagem sem detalhe usa so o nome no rotulo`() {
        val p = Personagem(nome = "Teste", desvantagens = listOf(desvantagem("Avareza", 12)))
        assertEquals("Avareza", AutocontroleRules.testesDisponiveis(p).first().rotulo)
    }
}
