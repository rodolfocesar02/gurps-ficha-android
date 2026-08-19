package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **A cor do texto sobre a cor da nota** — Lote NOTA-3.
 *
 * 🔴 O defeito que estes testes guardam: a nota tem fundo claro e o texto vinha
 * do tema. No tema **escuro**, `onSurface` é quase branco — branco sobre
 * amarelo-claro. Só aparecia no aparelho de quem usa tema escuro, ou seja, no
 * aparelho de outra pessoa.
 *
 * ⚠️ O teste principal **varre as cores reais**, lidas do código-fonte, em vez
 * de repetir uma lista aqui. Uma lista repetida descola no dia em que alguém
 * acrescentar uma cor — e é justamente esse dia que interessa.
 */
class CorDaNotaTest {

    // == A conta ======================================================

    @Test
    fun `fundo claro pede texto escuro, fundo escuro pede texto claro`() {
        assertEquals(CorDaNota.TEXTO_ESCURO, CorDaNota.textoSobre("#FFFFFF"))
        assertEquals(CorDaNota.TEXTO_ESCURO, CorDaNota.textoSobre("#FFF9C4"))
        assertEquals(CorDaNota.TEXTO_CLARO, CorDaNota.textoSobre("#000000"))
        assertEquals(CorDaNota.TEXTO_CLARO, CorDaNota.textoSobre("#1C1B1F"))
    }

    @Test
    fun `a virada nao e no meio -- o olho nao e linear`() {
        // 🔴 Um limiar de 0,5 na luminancia erra a faixa dos medios, que e
        // exatamente onde a escolha importa. Um cinza medio (#808080) tem
        // luminancia ~0,216: acima da virada da WCAG (0,179), entao pede texto
        // ESCURO -- e e isso que o olho confirma.
        val l = CorDaNota.luminancia("#808080")!!
        assertTrue("luminancia do cinza medio fora do esperado: $l", l in 0.19..0.24)
        assertEquals(CorDaNota.TEXTO_ESCURO, CorDaNota.textoSobre("#808080"))
    }

    @Test
    fun `aceita o formato do Android, com alfa na frente`() {
        // O `parseColor` do Android devolve e aceita #AARRGGBB. O alfa nao entra
        // na conta de luminancia.
        assertEquals(CorDaNota.luminancia("#FFF9C4"), CorDaNota.luminancia("#FFFFF9C4"))
        assertEquals(CorDaNota.TEXTO_ESCURO, CorDaNota.textoSobre("#FFFFF9C4"))
    }

    @Test
    fun `com ou sem o cerquilha da no mesmo`() {
        assertEquals(CorDaNota.luminancia("#FFF9C4"), CorDaNota.luminancia("FFF9C4"))
    }

    @Test
    fun `hex ilegivel nao explode, e cai no texto escuro`() {
        // ⚠️ Escuro e o palpite certo para as cores que existem (todas claras).
        // E um texto escuro que some num fundo escuro e um defeito VISIVEL --
        // ao contrario de um null que ninguem trata.
        listOf(null, "", "#ZZZ", "#12345", "azul", "#").forEach {
            assertNull("luminancia devia ser nula em $it", CorDaNota.luminancia(it))
            assertEquals(CorDaNota.TEXTO_ESCURO, CorDaNota.textoSobre(it))
        }
    }

    // == A varredura sobre as cores DE VERDADE =========================

    /** As sete cores lidas do proprio codigo-fonte, e nao repetidas aqui. */
    private fun coresReais(): List<String> {
        // O diretorio de trabalho do teste muda conforme quem o roda.
        val caminho = "src/main/java/com/gurps/ficha/ui/features/rolagem/EditorDeNota.kt"
        val direto = File(caminho)
        val fonte = (if (direto.exists()) direto else File("app/$caminho")).readText()
        val bloco = fonte.substringAfter("val CoresNotaDeJogo = listOf(")
            .substringBefore(")")
        return Regex("\"(#[0-9A-Fa-f]{6})\"").findAll(bloco).map { it.groupValues[1] }.toList()
    }

    @Test
    fun `todas as cores da nota tem contraste suficiente com o texto escolhido`() {
        val cores = coresReais()
        assertTrue("nao achei as cores no codigo-fonte", cores.size >= 7)

        cores.forEach { fundo ->
            val texto = CorDaNota.textoSobre(fundo)
            val razao = CorDaNota.contraste(fundo, texto)
            assertNotNull("contraste nulo em $fundo", razao)
            // 4,5 e o minimo da WCAG para texto corrido. Abaixo disso a nota
            // fica dificil de ler -- que e o defeito que este lote conserta.
            assertTrue(
                "contraste de so ${"%.1f".format(razao)} entre $fundo e $texto",
                razao!! >= 4.5
            )
        }
    }

    @Test
    fun `nenhuma cor da nota depende do tema para ser legivel`() {
        // 🔴 A invariante do lote: seja qual for o tema do sistema, a nota se le.
        // Isso so vale porque a cor do texto sai do FUNDO. Se alguem trocar a
        // regra por "sempre onSurface", este teste continua verde -- por isso
        // existe TAMBEM o teste de fiacao em ui/FiacaoBlocoDeNotasTest.
        coresReais().forEach { fundo ->
            val escolhido = CorDaNota.textoSobre(fundo)
            val oOutro = if (escolhido == CorDaNota.TEXTO_ESCURO) CorDaNota.TEXTO_CLARO
                         else CorDaNota.TEXTO_ESCURO
            assertTrue(
                "em $fundo o texto escolhido nao e o de maior contraste",
                CorDaNota.contraste(fundo, escolhido)!! > CorDaNota.contraste(fundo, oOutro)!!
            )
        }
    }

    // == A escala da conta ============================================

    @Test
    fun `preto e branco dao o contraste maximo de 21`() {
        val r = CorDaNota.contraste("#000000", "#FFFFFF")!!
        assertTrue("esperava ~21, veio $r", r in 20.9..21.1)
    }

    @Test
    fun `a mesma cor da contraste 1`() {
        assertEquals(1.0, CorDaNota.contraste("#FFF9C4", "#FFF9C4")!!, 0.001)
    }
}
