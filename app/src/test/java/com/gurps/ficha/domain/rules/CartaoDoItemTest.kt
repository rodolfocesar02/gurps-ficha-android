package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.TipoEquipamento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O que o cartão de item mostra — Lote EQP-2.
 *
 * ⚠️ Estes testes só existem porque a conta **saiu do @Composable**. Enquanto ela
 * morava lá dentro, o gate passava com 2009 testes verdes e o cartão de armadura
 * em cinco linhas: nenhum deles conseguia sequer fazer a pergunta.
 */
class CartaoDoItemTest {

    private fun armadura(
        nome: String = "Túnica",
        notas: String = "",
        local: String? = null,
        rd: String? = null,
        confiscado: Boolean = false
    ) = Equipamento(
        nome = nome,
        notas = notas,
        tipo = TipoEquipamento.ARMADURA,
        armaduraLocal = local,
        armaduraRd = rd,
        confiscado = confiscado
    )

    // ── O orçamento ────────────────────────────────────────────────────

    @Test
    fun `o corpo nunca passa do orcamento`() {
        // A invariante, não um caso: qualquer quantidade de linhas cabe.
        for (n in 0..12) {
            val entrada = (1..n).map { CartaoDoItem.Linha("linha $it") }
            val saida = CartaoDoItem.cortar(entrada)
            assertTrue(
                "com $n entradas o corpo devolveu ${saida.size} linhas",
                saida.size <= CartaoDoItem.LINHAS_DO_CORPO
            )
        }
    }

    @Test
    fun `o cartao inteiro cabe em quatro linhas de texto`() {
        // O nome gasta uma; a soma das alturas do corpo tem de fechar em 4.
        for (n in 1..12) {
            val visiveis = CartaoDoItem.cortar((1..n).map { CartaoDoItem.Linha("linha $it") })
            val altura = visiveis.indices.sumOf { CartaoDoItem.alturaDe(it, visiveis.size) }
            assertTrue(
                "com $n entradas o cartão ocuparia ${altura + 1} linhas",
                altura + 1 <= CartaoDoItem.LINHAS
            )
        }
    }

    @Test
    fun `o excedente e juntado na ultima, nunca descartado`() {
        val entrada = listOf("a", "b", "c", "d", "e").map { CartaoDoItem.Linha(it) }
        val saida = CartaoDoItem.cortar(entrada)
        val texto = saida.joinToString(" ") { it.texto }
        // Nada some calado: quem não coube aparece dentro da última linha.
        listOf("a", "b", "c", "d", "e").forEach {
            assertTrue("perdeu o pedaço '$it'", texto.contains(it))
        }
    }

    @Test
    fun `linha em branco nao gasta orcamento`() {
        val entrada = listOf(
            CartaoDoItem.Linha(""),
            CartaoDoItem.Linha("   "),
            CartaoDoItem.Linha("peso")
        )
        assertEquals(1, CartaoDoItem.cortar(entrada).size)
    }

    // ── Peso e quantidade ──────────────────────────────────────────────

    @Test
    fun `com um item so o peso aparece uma vez`() {
        val texto = CartaoDoItem.pesoEQuantidade(1, 0.2f)
        assertEquals("0.2 kg", texto)
        assertFalse("repetiu o total com quantidade 1", texto.contains("total"))
        assertFalse("mostrou o '1x' inútil", texto.contains("1x"))
    }

    @Test
    fun `com mais de um item o total aparece`() {
        val texto = CartaoDoItem.pesoEQuantidade(2, 0.25f)
        assertTrue(texto.contains("2x"))
        assertTrue(texto.contains("total 0.5 kg"))
    }

    @Test
    fun `peso redondo nao mostra casa decimal`() {
        assertEquals("2 kg", CartaoDoItem.pesoEQuantidade(1, 2f))
    }

    // ── 🔴 Os dois RD da armadura ──────────────────────────────────────

    @Test
    fun `o cartao da armadura mostra um RD so, e o do campo`() {
        // O caso exato da foto do usuário: o campo diz 1*, a nota congelada diz 2*.
        val eq = armadura(
            notas = "Local: tronco; RD: 2*\nBoa Qualidade, +1 RD Encantamento",
            local = "tronco",
            rd = "1*"
        )
        val texto = CartaoDoItem.linhasDaArmadura(eq).joinToString(" | ") { it.texto }

        assertTrue("sumiu o RD de verdade", texto.contains("RD: 1*"))
        assertFalse("o RD congelado da nota continua na tela: $texto", texto.contains("2*"))
        // O resto da nota — o que o jogador escreveu — continua lá.
        assertTrue(texto.contains("Encantamento"))
    }

    @Test
    fun `o RD que o cartao mostra e o mesmo que o combate le`() {
        // A invariante que interessa: tela e regra não podem divergir.
        val eq = armadura(notas = "Local: tronco; RD: 9", local = "tronco", rd = "1*")
        val naTela = CartaoDoItem.linhasDaArmadura(eq)
            .first { it.papel == CartaoDoItem.Papel.PROTECAO }.texto
        assertTrue(naTela.contains(eq.rdArmaduraExibicao()!!))
    }

    @Test
    fun `ficha antiga sem os campos ainda mostra local e RD`() {
        // Sem `armaduraRd`, o RD volta a sair da nota — e por isso a nota nunca
        // é apagada do disco, só escondida da tela.
        val eq = armadura(notas = "Local: pescoco; RD: 3", local = null, rd = null)
        val texto = CartaoDoItem.linhasDaArmadura(eq).joinToString(" | ") { it.texto }
        assertTrue(texto, texto.contains("RD: 3"))
        assertTrue(texto, texto.contains("pescoco"))
    }

    @Test
    fun `o cabecalho automatico sai, o texto do jogador fica`() {
        val nota = "Local: virilha; RD: 2*\nComprada em Muskovia\nLocal: sobra"
        val limpa = CartaoDoItem.notaSemCabecalho(nota)
        assertFalse(limpa.contains("RD: 2*"))
        assertTrue(limpa.contains("Comprada em Muskovia"))
        // "Local:" sem o "; RD:" não é o cabeçalho automático — é texto do jogador.
        assertTrue(limpa.contains("Local: sobra"))
    }

    @Test
    fun `armadura confiscada avisa em primeiro lugar`() {
        val eq = armadura(local = "tronco", rd = "2", confiscado = true)
        val linhas = CartaoDoItem.linhasDaArmadura(eq)
        assertEquals(CartaoDoItem.Papel.ALERTA, linhas.first().papel)
    }

    @Test
    fun `a armadura cabe no cartao mesmo com nota comprida`() {
        val eq = armadura(
            notas = "Local: tronco; RD: 2*\n" + "nota muito comprida ".repeat(20),
            local = "tronco",
            rd = "2*",
            confiscado = true
        )
        val visiveis = CartaoDoItem.cortar(CartaoDoItem.linhasDaArmadura(eq))
        assertTrue(visiveis.size <= CartaoDoItem.LINHAS_DO_CORPO)
    }

    // ── O texto torto do catálogo ──────────────────────────────────────

    @Test
    fun `o acento quebrado do catalogo e consertado`() {
        assertEquals("cranio", TextoDoCatalogo.corrigir("cr?nio"))
        assertEquals("pescoco", TextoDoCatalogo.corrigir("pescoço"))
        assertEquals("bracos, tronco", TextoDoCatalogo.corrigir("braços, tronco"))
    }
}
