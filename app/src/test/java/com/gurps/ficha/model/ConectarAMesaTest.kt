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

    // == O token, e onde ele não pode estar ==========================

    @Test
    fun `🔴 o token vai no FRAGMENTO, e nunca na query`() {
        // 🔴 O que esta depois do `#` NAO e enviado ao servidor: nao aparece nos
        // registros de acesso, nao vai no `Referer`, nao passa por intermediario
        // nenhum. O que esta depois do `?` vai em tudo isso -- e este token e a
        // senha da sala inteira.
        val semComentario = tab
            .replace(Regex("/\\*[\\s\\S]*?\\*/"), " ")
            .lines().joinToString("\n") { it.replace(Regex("//.*$"), " ") }
        val abrir = semComentario.substringAfter("fun abrirAMesaNoNavegador")
        assertTrue("nao achei a funcao de abrir", abrir.isNotEmpty())
        assertTrue("o convite nao usa o fragmento", abrir.contains("\"/#nome=\""))
        assertFalse(
            "o token foi para a query string, que vai para os registros do servidor",
            abrir.contains("?nome=") || abrir.contains("?t=")
        )
    }

    @Test
    fun `🔴 nome e token sao escapados no endereco`() {
        // 🔴 Um nome com espaco, acento ou `&` quebraria o convite ao meio -- e um
        // nome com `&t=` outro token poderia trocar o token pelo caminho.
        val abrir = tab.substringAfter("fun abrirAMesaNoNavegador")
        assertTrue("o nome nao e escapado", abrir.contains("URLEncoder.encode(nome"))
        assertTrue("o token nao e escapado", abrir.contains("URLEncoder.encode(token"))
    }

    @Test
    fun `🔴 o Intent NAO nomeia navegador nenhum`() {
        // 🔴 O Android mostra a escolha, ou usa o que a pessoa ja escolheu. Fixar
        // o Chrome tiraria dela uma decisao que e dela.
        val abrir = tab.substringAfter("fun abrirAMesaNoNavegador")
        assertTrue(abrir.contains("Intent.ACTION_VIEW"))
        assertFalse("o app fixou um navegador", abrir.contains("setPackage"))
        assertFalse(abrir.contains("com.android.chrome"))
    }

    @Test
    fun `⚠️ aparelho sem navegador nao derruba a tela`() {
        // ⚠️ A tela ja fez o que importa: guardar o endereco e o token. Estourar
        // aqui derrubaria tudo por causa do extra.
        val abrir = tab.substringAfter("fun abrirAMesaNoNavegador")
        assertTrue("abrir o navegador pode estourar", abrir.contains("catch"))
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
