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
    private val regra by lazy { fonte("com/gurps/ficha/ui/features/mesa/AAbaDaMesa.kt") }

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

    // == Quando a aba existe — MNA-1c ================================

    @Test
    fun `🟥 a aba existe enquanto a SALA estiver de pe, e nao pelo token guardado`() {
        // 🔴 A regra antiga era "destino MESA + token guardado". Estava certa no
        // papel e errada na tela: o token fica guardado PARA SEMPRE depois da
        // primeira conexao, e a aba virava permanente -- o contrario de "so
        // aparece quando voce entra".
        val linha = regra.lines().first { it.contains("fun aAbaDaMesaAparece") }
        val corpo = regra.substring(regra.indexOf(linha), regra.indexOf(linha) + 220)
        assertTrue(
            "a aba deixou de olhar para a sala",
            corpo.contains("SalaDaMesa.estaDePe")
        )
        assertFalse(
            "o token guardado voltou a fazer a aba aparecer sem ninguem ter entrado",
            corpo.contains("mesaToken") || corpo.contains("DestinoDaRolagem")
        )
    }

    @Test
    fun `🔴 o pedido de entrar tambem abre a aba, senao ninguem entra nunca`() {
        // ⚠️ Sem isto a regra e impossivel: a sala so sobe quando a aba abre, e a
        // aba so abre se a sala estiver de pe. Quem desata o no e a porta de
        // entrada -- o CONECTAR A MESA, que levanta o `irParaAMesa`.
        val linha = regra.lines().first { it.contains("fun aAbaDaMesaAparece") }
        val corpo = regra.substring(regra.indexOf(linha), regra.indexOf(linha) + 220)
        assertTrue(corpo.contains("irParaAMesa"))
    }

    @Test
    fun `🟥 o pedido so e largado quando a sala esta MESMO de pe`() {
        // 🔴 Limpa-lo logo depois de saltar e uma corrida perdida: entre por a aba
        // na frente e a janela nascer ha uma volta de desenho, e naquela volta a
        // sala ainda nao esta de pe. Sem o pedido levantado, a aba sumiria da
        // lista no exato momento em que a pessoa foi mandada para ela.
        val i = regra.indexOf("fun SaltarParaAMesaQuandoPedirem")
        assertTrue(i > 0)
        val corpo = regra.substring(i)
        val onde = corpo.indexOf("irParaAMesa = false")
        assertTrue("o pedido nunca e largado", onde > 0)
        val antes = corpo.substring(0, onde)
        assertTrue(
            "o pedido e largado sem conferir se a sala subiu",
            antes.contains("if (SalaDaMesa.estaDePe)")
        )
    }

    @Test
    fun `🔴 a janela e estado do Compose, senao a aba nao aparece nem some`() {
        // ⚠️ Um campo comum muda e ninguem redesenha: a aba so apareceria na
        // proxima vez que a tela se redesenhasse por outro motivo qualquer.
        assertTrue(
            "a janela voltou a ser um campo comum, e a aba deixou de reagir",
            sala.contains("private var janela by mutableStateOf<WebView?>(null)")
        )
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

    @Test
    fun `🟥 o SAIR pela pagina derruba a sala INTEIRA, e nao so o servico`() {
        // 🔴 A primeira forma apagava so o servico e deixava a janela viva, para
        // quem saisse poder entrar de novo sem sair da aba. Deixou de servir no
        // MNA-1c: agora e a SALA DE PE que decide se o icone existe, e uma janela
        // viva depois do SAIR e uma sala de pe -- o icone ficava na barra com a
        // pessoa ja fora da mesa.
        val i = sala.indexOf("aoSairDaMesa =")
        assertTrue("nao achei o que acontece ao sair pela pagina", i > 0)
        val corpo = sala.substring(i, minOf(i + 120, sala.length))
        assertTrue(
            "o SAIR pela pagina deixou de derrubar a sala",
            corpo.contains("sair()")
        )
    }

    @Test
    fun `🔴 derrubar a sala apaga a janela, e por isso a aba some`() {
        // ⚠️ E a corrente inteira: sem `janela = null` o `estaDePe` continua
        // verdadeiro, e a aba fica na barra para sempre.
        val i = sala.indexOf("fun sair()")
        assertTrue(i > 0)
        val corpo = sala.substring(i)
        listOf("w.destroy()", "janela = null", "ServicoDaMesa.apagar",
               "jaConvidou = false", "aoSairDaAba?.invoke()").forEach {
            assertTrue("o sair() deixou de fazer: $it", corpo.take(1400).contains(it))
        }
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
