package com.gurps.ficha.model

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **A aba da Mesa não pode trancar ninguém lá dentro** — lote MNA-1b.
 *
 * ## 🟥 O defeito, encontrado por ele no aparelho na primeira vez que entrou
 *
 * > *"entrei na mesa, porém não tenho como voltar pra outras abas, não aparece
 * > na parte de baixo! o botão de sair, dentro do menu, também não faz voltar pro
 * > app, ou onde existe as outras abas!"*
 *
 * 🔴 Eu tinha metido a Mesa no `hideAppChrome`, junto do VTT e do Modo Jogo da
 * Saga. Parecia certo — os três são tela cheia — e **não era**: aqueles dois têm
 * saída própria dentro deles, e a Mesa não tem. O botão SAIR dela sai da
 * **sala**, e não da **aba**.
 *
 * ⚠️ Nenhum teste pegou isso, e nenhum teste **poderia** ter pegado: era a tela
 * desenhada, e a bancada não desenha. O que estas sondas fazem é impedir que ele
 * volte — e ele voltaria, porque a linha que o causou parecia razoável.
 */
class AAbaDaMesaTest {

    private fun fonte(caminho: String): String {
        val f = File("src/main/java/$caminho")
        assertTrue("nao achei $caminho", f.exists())
        return f.readText()
    }

    private val tela by lazy { fonte("com/gurps/ficha/ui/FichaScreen.kt") }
    private val barra by lazy {
        fonte("com/gurps/ficha/ui/components/FichaCustomNavigationBar.kt")
    }
    private val aba by lazy { fonte("com/gurps/ficha/ui/features/mesa/TabMesa.kt") }
    private val sala by lazy { fonte("com/gurps/ficha/ui/features/mesa/SalaDaMesa.kt") }

    /**
     * O código sem as linhas de comentário — elas falam do defeito para o
     * explicar, e uma sonda que as lesse ficaria verde por causa da explicação.
     *
     * 🟥 **Linha a linha, e não por expressão regular.** A primeira versão desta
     * função apagava blocos de comentário com uma regex e comeu **339 linhas** do
     * arquivo: o `arrayOf("image/\*")` do seletor de imagem abre um comentário
     * que só fecha 339 linhas abaixo. Os dois testes de baixo ficaram vermelhos
     * por causa disso, e não por causa do que mediam.
     *
     * ⚠️ É o mesmo jeito do `portugues-do-brasil.test.js` da Mesa, e pela mesma
     * razão: uma regra simples que se entende ganha de uma regex que se acha que
     * se entende.
     */
    private val codigo by lazy {
        tela.lines()
            .filterNot {
                val t = it.trim()
                t.startsWith("*") || t.startsWith("//") || t.startsWith("/*")
            }
            .joinToString("\n")
    }

    // == A saída ======================================================

    @Test
    fun `🟥 a Mesa NAO entra no hideAppChrome`() {
        // 🔴 A linha exata que trancou a pessoa lá dentro. Se ela voltar, isto
        // fica vermelho — e é a única coisa entre o defeito e o aparelho dele.
        val linha = codigo.lines().first { it.contains("val hideAppChrome") }
        assertFalse(
            "a Mesa voltou para o hideAppChrome, e isso esconde a barra de abas",
            linha.contains("\"Mesa\"")
        )
        // E o que ele guarda continua valendo para quem tem saída própria.
        assertTrue(linha.contains("vttFullscreen"))
        assertTrue(linha.contains("sagaModoJogo"))
    }

    @Test
    fun `🟥 a barra de abas de baixo NAO some na Mesa`() {
        // ⚠️ É a barra de baixo que devolve a ficha, o equipamento e a rolagem.
        // Sem ela, entrar na Mesa é entrar e ficar.
        val i = codigo.indexOf("bottomBar = {")
        assertTrue("nao achei a barra de baixo", i > 0)
        val trecho = codigo.substring(i, minOf(i + 400, codigo.length))
        assertTrue(
            "a barra de baixo passou a depender de outra coisa que nao o hideAppChrome",
            trecho.contains("if (!hideAppChrome)")
        )
        assertFalse(
            "a barra de baixo some na Mesa, e a pessoa fica trancada la dentro",
            trecho.contains("esconderOCabecalho") || trecho.contains("\"Mesa\"")
        )
    }

    @Test
    fun `🔴 mas o cabecalho de cima CONTINUA escondido na Mesa`() {
        // A Mesa já tem a barra dela (SALAS / PESSOAS / MENU). Duas barras uma em
        // cima da outra num telefone não deixam tabuleiro nenhum à vista.
        val linha = codigo.lines().first { it.contains("val esconderOCabecalho") }
        assertTrue(linha.contains("\"Mesa\""))
        assertTrue(linha.contains("hideAppChrome"))

        val topo = codigo.indexOf("topBar = {")
        assertTrue(topo > 0)
        assertTrue(
            "o cabecalho voltou a aparecer por cima da Mesa",
            codigo.substring(topo, topo + 200).contains("if (!esconderOCabecalho)")
        )
    }

    // == Sair da sala é sair da aba ===================================

    @Test
    fun `🟥 o SAIR da sala tira a pessoa da aba`() {
        // 🔴 Sem isto, apertar SAIR deixa a pessoa olhando para a porta de
        // entrada da sala de que acabou de sair, dentro da mesma aba.
        assertTrue(
            "a TabMesa deixou de ligar o aviso de saida",
            aba.contains("SalaDaMesa.aoSairDaAba = aoSairDaMesa")
        )
        assertTrue(
            "a tela deixou de dizer para onde ir quando se sai da mesa",
            codigo.contains("aoSairDaMesa = { abaEscolhida =")
        )
        // E os dois caminhos de saída avisam: o botão de dentro da página, e o
        // `sair()` de vez — que é o da notificação, com o telefone no bolso.
        assertTrue(
            "a saida pela pagina nao avisa a aba",
            sala.contains("aoSairDaAba?.invoke()")
        )
        assertTrue(
            "o aviso nao e desligado ao sair da aba, e ficaria preso a uma tela morta",
            aba.contains("SalaDaMesa.aoSairDaAba = null")
        )
    }

    // == O ícone ======================================================

    @Test
    fun `🔴 a aba da Mesa tem icone proprio, e nao o do Geral`() {
        // ⚠️ Ela caía no `else`, que é o ícone do Geral — a barra ficava com dois
        // bonecos iguais e o segundo não dizia para onde levava.
        assertTrue(
            "a Mesa voltou a cair no else e usa o icone do Geral",
            barra.contains("\"Mesa\" -> R.drawable.tab_mesa")
        )
        assertTrue(
            "o desenho do icone sumiu",
            File("src/main/res/drawable/tab_mesa.xml").exists()
        )
    }
}
