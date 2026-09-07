package com.gurps.ficha.model

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Conectar à mesa** — lote MESA-44.
 *
 * A tela de destino da rolagem virou o que já era na prática: a **porta de
 * entrada da mesa**. Dois campos — nome e token, os mesmos que se digitam no
 * site — e um botão que salva, testa e abre a mesa no navegador.
 *
 * 🔴 O teste que paga o lote é o do **token fora da query string**. Ele vai no
 * fragmento (`#`), que não é enviado ao servidor; na query (`?`) ele iria para
 * os registros de acesso e para o `Referer` de todo intermediário no caminho.
 */
class ConectarAMesaTest {

    private fun fonte(caminho: String): String {
        val f = File("src/main/java/$caminho")
        assertTrue("nao achei $caminho", f.exists())
        return f.readText()
    }

    private val dialogo by lazy {
        fonte("com/gurps/ficha/ui/features/rolagem/RolagemDestinoDialog.kt")
    }
    private val tab by lazy { fonte("com/gurps/ficha/ui/TabRolagem.kt") }
    private val cliente by lazy { fonte("com/gurps/ficha/data/network/MesaApiClient.kt") }
    private val delegate by lazy {
        fonte("com/gurps/ficha/viewmodel/delegates/FichaSocialDelegate.kt")
    }

    // == A tela ======================================================

    @Test
    fun `o endereco saiu da tela, e vive no codigo`() {
        // ⚠️ Ele nunca muda, e era mais um campo para digitar errado.
        assertFalse(
            "o campo do endereco voltou para a tela",
            dialogo.contains("Endereço da sala")
        )
        assertTrue(
            "nao ha endereco fixo no cliente da Mesa",
            cliente.contains("const val ENDERECO_PADRAO")
        )
        assertTrue(cliente.contains("mesagurps.duckdns.org"))
    }

    // == 🔴 O endereco que a MESA-44 prometeu e nao entregou ======

    @Test
    fun `🔴 o endereco USADO e a constante, e nao o que veio do disco`() {
        // 🔴 O teste vizinho conferia que o CAMPO saiu da tela e que a constante
        // existe -- e passou verde durante o lote inteiro enquanto o valor
        // continuava a vir das preferencias, onde nunca houve nada.
        //
        // ⚠️ Num aparelho onde ninguem tinha digitado o endereco antes (um
        // emulador, uma instalacao nova) ele ficava VAZIO, o `postFicha` saia no
        // `if (baseUrl.isBlank())` calado, e a ficha nunca chegava a mesa.
        // Medido no servidor: pasta `dados/fichas/` vazia, treze bonecos sem ficha.
        assertTrue(
            "o endereco voltou a ser lido das preferencias",
            delegate.contains("val mesaEndereco: String get() = MesaApiClient.ENDERECO_PADRAO")
        )
        assertFalse(
            "ainda ha uma preferencia de endereco: o vazio volta por ali",
            delegate.contains("prefMesaEndereco")
        )
    }

    @Test
    fun `🔴 configurar a mesa NAO recebe endereco`() {
        // 🔴 Era por esse parametro que o vazio entrava: a tela passava uma
        // variavel cujo campo tinha sido removido do ecra.
        assertTrue(
            "configurarMesa voltou a receber endereco",
            delegate.contains("fun configurarMesa(token: String?, nome: String? = null)")
        )
        assertFalse(
            "a tela ainda manda um endereco para o ViewModel",
            dialogo.contains("onSalvarMesa(endereco")
        )
        assertFalse(
            "a tela ainda guarda um endereco proprio",
            dialogo.contains("var endereco by remember")
        )
    }

    @Test
    fun `⚠️ nenhum envio a mesa sai por falta de endereco`() {
        // ⚠️ Havia tres `?: return` presos ao endereco -- tres saidas caladas.
        // Com ele fixo, nenhuma delas faz sentido, e deixa-las la seria deixar o
        // defeito de pe a espera de alguem voltar a ligar o endereco ao disco.
        assertFalse(
            "ainda ha uma saida calada por endereco nulo",
            delegate.contains("val endereco = mesaEndereco ?: return")
        )
        // ⚠️ A frase ainda aparece num COMENTARIO que conta a historia do
        // MESA-44 -- e comentario nao e comportamento. O que se cobra e a SAIDA.
        assertFalse(
            "testarMesa ainda pode falhar por falta de endereco",
            delegate.contains("return ResultadoDaConexao(false, \"Falta o endere")
        )
    }

    @Test
    fun `a tela pede NOME e TOKEN, os mesmos do site`() {
        assertTrue("sumiu o campo do nome", dialogo.contains("Seu nome"))
        assertTrue("sumiu o campo do token", dialogo.contains("Token da sala"))
        // ⚠️ O leitor de tela precisa de saber QUAL nome: "Nome" sozinho nao
        // distingue do nome do personagem, que a pessoa digitou tres telas atras.
        assertTrue(
            "o campo do nome nao se explica para o leitor de tela",
            dialogo.contains("Seu nome na mesa virtual, o mesmo com que voce entra na sala")
        )
    }

    @Test
    fun `o botao chama-se CONECTAR A MESA`() {
        assertTrue(dialogo.contains("CONECTAR À MESA"))
        assertTrue("nao avisa enquanto conecta", dialogo.contains("CONECTANDO..."))
        assertFalse("o rotulo antigo ficou", dialogo.contains("SALVAR E TESTAR"))
    }

    @Test
    fun `⚠️ o botao so acende com os dois campos preenchidos`() {
        // ⚠️ Sem eles nao ha o que testar nem o que abrir, e um botao que nao faz
        // nada e pior que um botao apagado.
        val trecho = dialogo.substringAfter("CONECTAR À MESA")
            .let { dialogo.substringBefore(it) }
        assertTrue(
            "o botao acende sem nome nem token",
            dialogo.contains("nomeNaMesa.isNotBlank() && token.isNotBlank()")
        )
        assertTrue(trecho.isNotEmpty())
    }

    @Test
    fun `⚠️ o botao respeita o minimo de toque`() {
        // ⚠️ 48 dp e o minimo, e o app tem variante para quem nao enxerga a tela.
        // O botao estava em 36 dp desde antes deste lote.
        val trecho = dialogo.substringAfter("if (testando) \"CONECTANDO...\"")
        assertTrue(trecho.isNotEmpty())
        assertTrue(
            "o botao de conectar ficou abaixo do minimo de toque",
            dialogo.contains(".height(48.dp)")
        )
    }

    // == O caminho do navegador saiu do aplicativo — MNA-10 ==========

    /**
     * 🟥 **Quatro sondas do MESA-44 foram apagadas aqui, e é de propósito.**
     *
     * Elas guardavam o `abrirAMesaNoNavegador`: o token no fragmento e nunca na
     * query, o nome escapado, o `Intent` sem navegador fixo, o `catch` para
     * aparelho sem navegador. Eram boas sondas, e cada uma nasceu de um defeito
     * de verdade.
     *
     * 🔴 A função **já não existe**. Decisão dele no MNA-10: *"abre só a aba"*.
     * O token deixou de virar endereço, então não há endereço onde ele possa
     * estar no lugar errado — o [com.gurps.ficha.domain.rules.ConviteDaMesa]
     * entrega o convite por dentro da página, depois de ela carregar.
     *
     * ⚠️ E o que elas guardavam **não se perdeu**: o
     * `dentro-do-aplicativo.js` da Mesa mantém o caminho do navegador vivo do
     * lado da **página**, porque o Mestre está no PC. O que saiu foi o botão do
     * aplicativo, e só ele.
     *
     * 🔴 Ficam duas sondas no lugar delas: que o navegador realmente saiu, e que
     * o botão passou a pedir a aba.
     */

    @Test
    fun `🟥 o aplicativo ja nao abre navegador nenhum`() {
        // Se alguém trouxer o atalho de volta, isto fica vermelho e obriga a
        // decidir outra vez — em vez de o app passar a ter duas portas para a
        // mesma sala, e a pessoa entrar duas vezes e ser expulsa de uma.
        assertFalse(
            "o caminho do navegador voltou ao aplicativo",
            tab.contains("abrirAMesaNoNavegador")
        )
        assertFalse(
            "alguem abriu um Intent para a mesa outra vez",
            tab.contains("ACTION_VIEW") && tab.contains("ENDERECO_PADRAO")
        )
    }

    @Test
    fun `🔴 CONECTAR A MESA guarda ANTES de pedir a aba`() {
        // ⚠️ A aba só existe com destino MESA e token guardado. Pedi-la antes de
        // guardar seria pedir uma aba que ainda não está na lista — e o salto
        // cairia na primeira, com a pessoa olhando para o Geral sem entender.
        val trecho = tab.substringAfter("onAbrirAMesa = {")
        assertTrue("nao achei o que o botao faz", trecho.isNotEmpty())
        val guarda = trecho.indexOf("configurarMesa(")
        val pede = trecho.indexOf("irParaAMesa = true")
        assertTrue("o botao ja nao guarda a mesa", guarda >= 0)
        assertTrue("o botao ja nao pede a aba", pede >= 0)
        assertTrue("pede a aba antes de guardar o token", guarda < pede)
    }

    // == Só abre se o token valer ====================================

    @Test
    fun `🔴 so abre o navegador se a sala ACEITOU o token`() {
        // 🔴 Abrir com token errado poria a pessoa diante da tela de entrada da
        // mesa sem saber por que -- e ela culparia o navegador, nao o token.
        assertTrue(
            "o botao abre o navegador sem conferir",
            dialogo.contains("if (r.ok) onAbrirAMesa(")
        )
    }

    @Test
    fun `🔴 o resultado do teste e ESTRUTURADO, e nao uma frase para adivinhar`() {
        // 🔴 A primeira versao devolvia so a frase, e a tela adivinhava o resto
        // procurando "erro" e "nao" dentro dela. Parecia funcionar e nao
        // funcionava: "Token errado" e "Falta o endereco da sala" passavam as
        // duas pela peneira, e o navegador abria na mesma.
        //
        // ⚠️ Foi medido ANTES de ir para o aparelho, correndo a heuristica contra
        // as quatro frases de verdade.
        assertTrue(
            "o teste voltou a devolver so uma frase",
            delegate.contains("data class ResultadoDaConexao(val ok: Boolean")
        )
        assertTrue(delegate.contains("suspend fun testarMesa(): ResultadoDaConexao"))
        assertFalse(
            "a heuristica de adivinhar pela frase voltou",
            dialogo.contains("fun podeAbrir")
        )
    }

    @Test
    fun `🔴 o teste confere o TOKEN, e nao so se a sala responde`() {
        // 🔴 Antes isto so perguntava se a sala estava de pe, e dizia
        // "respondeu" com o token errado.
        assertTrue(
            "o teste nao manda o token",
            delegate.contains("MesaApiClient.saude(endereco, mesaToken)")
        )
        assertTrue(
            "o cliente nao aceita token no teste de saude",
            cliente.contains("fun saude(baseUrl: String, token: String? = null)")
        )
        // ⚠️ No CABECALHO, e nunca na URL.
        val saude = cliente.substringAfter("fun saude(")
        assertTrue("o token do teste nao vai no cabecalho",
            saude.contains("setRequestProperty(\"X-Token\", token)"))
        assertFalse("o token do teste foi para a URL", saude.contains("?token="))
    }

    @Test
    fun `⚠️ token errado tem frase propria, dizendo o que fazer`() {
        // ⚠️ "401" nao ajuda ninguem no meio de uma sessao.
        assertTrue(cliente.contains("token_da_sala_invalido"))
        assertTrue(
            "o token errado nao diz o que fazer",
            delegate.contains("Peça o token novo ao Mestre")
        )
    }
}
