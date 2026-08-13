package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `o campo nao tem piso de altura, e isso foi pedido`() {
        // ⚠️ Lote GER-3. A primeira versão prendia a altura em 48 dp (o mínimo de
        // toque do projeto) e sobrava um vão entre o rótulo e o texto. O usuário
        // comparou as duas na tela e escolheu a sem piso — a caixa passa a ter a
        // altura do que está escrito dentro dela.
        //
        // 🔴 Este teste existe para quem vier depois: reapertar a altura aqui
        // DESFAZ um pedido, não corrige um descuido. Se for para voltar atrás, que
        // seja com o usuário sabendo, e não por um "isso está fora do guia".
        assertFalse(
            "o piso de altura voltou — a altura tem de vir do conteudo",
            campo.contains("heightIn(")
        )
        // O que garante o campo grande para quem precisa: a altura sai do
        // `bodyLarge` do TEMA, então a variante pracego cresce sozinha.
        assertTrue(
            "a altura deixou de acompanhar a fonte do tema",
            campo.contains("MaterialTheme.typography.bodyLarge")
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
