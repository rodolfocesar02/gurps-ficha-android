package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.NotaDeJogo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **A nota indo para o Discord** — Lote NOTA-1.
 *
 * ⚠️ O servidor tem UM endpoint, `/api/rolls`, e ele recebe o pacote de uma
 * rolagem. Nao existe rota de "mandar texto". A nota entra naquele envelope --
 * uma adaptacao, nao a forma certa.
 *
 * 🔴 O risco disso e a nota PARECER uma rolagem que aconteceu. Estes testes
 * guardam os tres campos que impedem isso: `dice` vazio, `total` 0 e `target`
 * nulo. Se alguem puser dados ali "para ficar bonito no Discord", a mesa vai ver
 * um resultado que ninguem rolou.
 */
class NotaParaDiscordTest {

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    private val nota = NotaDeJogo(texto = "O barao mentiu sobre o mapa. Conferir na taverna.")

    @Test
    fun `a nota nao finge ser uma rolagem`() {
        val p = NotaParaDiscord.payloadDe("Cesar", nota)!!
        assertEquals("Nota", p.testType)
        // Os tres que mantem o pacote honesto:
        assertTrue("apareceram dados numa nota", p.dice.isEmpty())
        assertEquals("a nota ganhou um total", 0, p.total)
        assertNull("a nota ganhou um alvo", p.target)
        assertNull("a nota ganhou margem", p.margin)
        assertEquals(0, p.modifier)
    }

    @Test
    fun `o texto da nota vai inteiro, e o titulo vai no contexto`() {
        val p = NotaParaDiscord.payloadDe("Cesar", nota)!!
        assertEquals("Cesar", p.character)
        assertEquals(nota.titulo, p.context)
        assertEquals(nota.texto, p.outcome)
    }

    @Test
    fun `nota em branco NAO e enviada`() {
        // ⚠️ Mandar uma linha vazia para o canal da mesa e pior do que nao
        // mandar nada. A regra recusa, e a tela nem oferece o botao.
        assertNull(NotaParaDiscord.payloadDe("Cesar", NotaDeJogo(texto = "")))
        assertNull(NotaParaDiscord.payloadDe("Cesar", NotaDeJogo(texto = "   \n  ")))
    }

    @Test
    fun `ficha sem nome nao manda um remetente vazio`() {
        assertEquals("Sem nome", NotaParaDiscord.payloadDe("", nota)!!.character)
        assertEquals("Sem nome", NotaParaDiscord.payloadDe("   ", nota)!!.character)
    }

    @Test
    fun `o texto e aparado nas pontas`() {
        val p = NotaParaDiscord.payloadDe("Cesar", NotaDeJogo(texto = "  algo  \n"))!!
        assertEquals("algo", p.outcome)
    }

    @Test
    fun `a confirmacao mostra o que vai sair`() {
        // ⚠️ Confirmar "quer enviar?" sem mostrar O QUE vai sair e uma
        // confirmacao de mentira: o jogador clica sim sem saber o conteudo.
        val texto = NotaParaDiscord.perguntaDeConfirmacao(nota)
        assertTrue(texto, texto.contains("O barao mentiu"))
        assertTrue(texto, texto.contains("Discord"))
    }

    @Test
    fun `a previa da confirmacao nao despeja um texto gigante`() {
        val gigante = NotaDeJogo(texto = "a".repeat(500))
        val texto = NotaParaDiscord.perguntaDeConfirmacao(gigante)
        assertTrue("a previa passou de 200 caracteres", texto.length < 200)
        assertTrue("a previa nao avisa que foi cortada", texto.contains("…"))
    }

    // == A fiacao da tela ===============================================

    @Test
    fun `o botao de notas e o ULTIMO do painel, e igual aos outros`() {
        // Pedido do usuario: "ele tem que ser o ultimo botao do painel, e a
        // mesma cor, tamanho e fonte das letras igual dos outros botoes".
        val menu = fonte("com/gurps/ficha/ui/features/rolagem/RolagemComponents.kt")
        val livre = menu.indexOf("\"Rolagem Livre\"")
        val notas = menu.indexOf("\"Bloco de Notas\"")
        assertTrue("o botao de notas sumiu do menu", notas > 0)
        assertTrue("o botao de notas nao e o ultimo", notas > livre)

        // E o OutlinedButton solto nao pode voltar para a aba.
        val aba = fonte("com/gurps/ficha/ui/TabRolagem.kt")
        assertFalse(
            "o botao voltou a ser um OutlinedButton fora do padrao",
            aba.contains("OutlinedButton(\n            onClick = { showBlocoDeNotasDialog = true }")
        )
        assertTrue("a aba nao liga mais o dialogo",
            aba.contains("onShowBlocoDeNotas = { showBlocoDeNotasDialog = true }"))
    }

    @Test
    fun `enviar pede confirmacao antes`() {
        // ⚠️ Enviar e irreversivel: cai no canal e nao volta.
        val d = fonte("com/gurps/ficha/ui/features/rolagem/DialogoBlocoDeNotas.kt")
        assertTrue("sumiu o dialogo de confirmacao", d.contains("ConfirmarEnvioDaNota("))
        assertTrue("o Sim sumiu", d.contains("AppBotaoPrincipal(\"Sim\""))
        assertTrue("o Nao sumiu", d.contains("AppBotaoSecundario(\"Não\""))
        // O envio so pode acontecer DEPOIS do onConfirmar.
        val iConfirmar = d.indexOf("onConfirmar = {")
        val iEnvio = d.indexOf("viewModel.enviarRolagemDiscord(")
        assertTrue("o envio saiu de dentro da confirmacao",
            iConfirmar in 1 until iEnvio)
    }

    @Test
    fun `o envio avisa quando falha`() {
        // Sucesso silencioso faria o jogador mandar de novo achando que nao foi.
        val d = fonte("com/gurps/ficha/ui/features/rolagem/DialogoBlocoDeNotas.kt")
        assertTrue("nao avisa nada depois de enviar", d.contains("Anotação enviada."))
        assertTrue("nao avisa quando falha", d.contains("Não foi enviada"))
    }

    @Test
    fun `o envio continua alcancavel na variante pracego`() {
        // 🔴 `mergeDescendants` funde o card num no so e o icone SOME para o
        // leitor de tela. A acao personalizada e o que devolve o envio a quem
        // nao ve a tela -- e ninguem perceberia isso olhando o aparelho.
        val d = fonte("com/gurps/ficha/ui/features/rolagem/DialogoBlocoDeNotas.kt")
        assertTrue(
            "o envio ficou inalcancavel para o leitor de tela",
            d.contains("CustomAccessibilityAction(")
        )
    }

    // == O canal, achado pelo usuario no aparelho ========================

    /**
     * 🔴 *"o canal esta definido, porem quando mando a msg ele diz canal de
     * envio nao definido"* -- e estava. Eu nao passava o canal, e o servidor
     * devolvia **400**.
     *
     * ⚠️ A causa nao foi distracao: a nota virou a **quinta rota** para o mesmo
     * envio, e nasceu diferente das outras quatro. E o formato de defeito que
     * mais se repete neste projeto -- duas rotas para a mesma coisa, e o defeito
     * mora na diferenca.
     *
     * Por isso este teste NAO olha so a nota: ele varre **todo** pacote de envio
     * do app e exige o canal em cada um. A sexta rota ja nasce coberta.
     */
    @Test
    fun `todo envio ao Discord leva o canal`() {
        val arquivos = listOf(
            "com/gurps/ficha/ui/TabRolagem.kt",
            "com/gurps/ficha/domain/rules/NotaParaDiscord.kt"
        )
        var pacotes = 0
        arquivos.forEach { caminho ->
            val src = fonte(caminho)
            var i = src.indexOf("DiscordRollPayload(")
            while (i >= 0) {
                pacotes++
                // O bloco de argumentos vai ate o fecha-parenteses da chamada.
                val trecho = src.substring(i, minOf(src.length, i + 700))
                assertTrue(
                    "um pacote de $caminho foi montado sem channelId",
                    trecho.contains("channelId")
                )
                i = src.indexOf("DiscordRollPayload(", i + 1)
            }
        }
        // ⚠️ Se este numero cair, alguem apagou uma rota de envio; se subir sem
        // este teste ser lido, alguem criou a sexta e pode ter repetido o erro.
        assertEquals("mudou a quantidade de rotas de envio", 5, pacotes)
    }

    @Test
    fun `a nota usa o MESMO canal que as rolagens`() {
        // Nao basta ter um canal: tem de ser o que o usuario escolheu na aba,
        // que e o mesmo `canalDiscordSelecionadoId` das quatro rolagens.
        val d = fonte("com/gurps/ficha/ui/features/rolagem/DialogoBlocoDeNotas.kt")
        assertTrue(
            "a nota voltou a ser enviada sem canal",
            d.contains("canalId = viewModel.canalDiscordSelecionadoId")
        )
    }

    @Test
    fun `o icone e o logo do Discord, e nao um Icon pintado`() {
        // ⚠️ `Icon` do Material pinta o desenho inteiro com uma cor so, e o logo
        // viraria uma mancha chapada. Tem de ser `Image`.
        val d = fonte("com/gurps/ficha/ui/features/rolagem/DialogoBlocoDeNotas.kt")
        assertTrue("o logo sumiu", d.contains("R.drawable.ic_discord"))
        assertTrue("o logo virou Icon e perdeu as cores", d.contains("Image("))
        val arte = File("src/main/res/drawable/ic_discord.png").let {
            if (it.exists()) it else File("app/src/main/res/drawable/ic_discord.png")
        }
        assertTrue("o arquivo do logo nao esta no drawable", arte.exists())
    }
}
