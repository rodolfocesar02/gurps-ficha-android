package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **O campo compacto do cartão de identificação** — Lote GER-2.
 *
 * O pedido do usuário foi de aparência: *"o espaço entre a caixa e o texto interno,
 * deixar o mínimo possível"*. Aparência não se testa em JUnit — o que dá para
 * travar é o que faria a mudança **desaparecer sem ninguém notar**:
 *
 * 1. alguém reverter os cinco campos para `OutlinedTextField`, que volta aos 56 dp;
 * 2. alguém encolher o campo abaixo do alvo mínimo de toque de 48 dp, o que
 *    quebraria a variante para quem não enxerga a tela.
 */
class CampoCompactoTest {

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    private val campo get() = fonte("com/gurps/ficha/ui/AppCampos.kt")

    @Test
    fun `os cinco campos do cartao usam o campo compacto`() {
        // Nome, Jogador, Pontos, XP e NT.
        val tab = fonte("com/gurps/ficha/ui/TabGeral.kt")
        val usos = Regex("""AppCampoCompacto\(""").findAll(tab).count()
        assertEquals("o cartao de identificacao voltou ao campo alto", 5, usos)
    }

    @Test
    fun `o respiro interno e pequeno de verdade`() {
        // ⚠️ O de fábrica é 16 dp em cima e embaixo. Se alguém devolver esse número
        // aqui, o cartão volta a ocupar meia tela e este teste reprova.
        val m = Regex("""contentPadding\(\s*start\s*=\s*(\d+)\.dp,\s*top\s*=\s*(\d+)\.dp,\s*end\s*=\s*(\d+)\.dp,\s*bottom\s*=\s*(\d+)\.dp""")
            .find(campo)
        assertTrue("nao achei o contentPadding do campo compacto", m != null)
        val (_, topo, _, base) = m!!.destructured
        assertTrue("o respiro de cima voltou a crescer: $topo dp", topo.toInt() <= 4)
        assertTrue("o respiro de baixo voltou a crescer: $base dp", base.toInt() <= 4)
    }

    @Test
    fun `o alvo de toque nao encolheu junto`() {
        // 🔴 O jeito óbvio de deixar o campo ainda menor é tirar este piso. Não dá:
        // 48 dp é o mínimo de toque do projeto, e este app tem variante pra cego.
        assertTrue(
            "o campo perdeu o piso de altura de toque",
            campo.contains("heightIn(min = UiTokens.TouchMinHeight)")
        )
        val tokens = fonte("com/gurps/ficha/ui/UiStandards.kt")
        assertTrue(
            "o proprio token de toque mudou de valor",
            tokens.contains("TouchMinHeight = 48.dp")
        )
    }

    @Test
    fun `a moldura continua sendo a do Material`() {
        // O campo é montado à mão, mas com as peças do Material: se alguém trocar
        // por um Box desenhado na unha, borda, cores e foco param de acompanhar o
        // tema — inclusive o tema de alto contraste.
        assertTrue(campo.contains("OutlinedTextFieldDefaults.DecorationBox"))
        assertTrue(campo.contains("OutlinedTextFieldDefaults.ContainerBox"))
        assertTrue(campo.contains("OutlinedTextFieldDefaults.colors()"))
    }
}
