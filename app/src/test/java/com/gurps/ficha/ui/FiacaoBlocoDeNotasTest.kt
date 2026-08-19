package com.gurps.ficha.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Lote NOTA-3** — os dois defeitos do Bloco de Notas, e por que só um teste
 * que LÊ a tela os pega.
 *
 * ## 🔴 1. Excluir não excluía
 *
 * O botão de excluir chamava `onExcluir(id)` e logo `onClose()`. O `onClose`
 * tira o editor da composição, o `DisposableEffect.onDispose` dispara e **grava
 * a nota de volta** — porque para o `FichaNotesDelegate` um `id` que não está na
 * lista quer dizer *"nota nova"*.
 *
 * ⚠️ **Nenhum teste do delegate pegaria isto**, e é o ponto: o delegate está
 * certo. `salvarNota` acrescentar um `id` desconhecido é o comportamento que faz
 * a criação de nota funcionar. O defeito é a **ordem em que a tela chama as
 * duas coisas** — e ordem de chamada só se vê lendo a tela.
 *
 * ## 🔴 2. Ilegível no tema escuro
 *
 * O fundo da nota é sempre claro, e o texto vinha de `onSurface`, que no tema
 * escuro é quase branco. O `CorDaNotaTest` prova que a **conta** está certa;
 * este aqui prova que a tela **usa** a conta. Sem os dois, a regra fica verde e
 * a nota continua ilegível — o defeito mais repetido deste projeto.
 */
class FiacaoBlocoDeNotasTest {

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao achei o arquivo: $caminho", f.exists())
        return f.readText()
    }

    /**
     * O codigo SEM comentarios.
     *
     * 🔴 A primeira versao da varredura reprovou por causa do proprio KDoc que
     * EXPLICA o defeito: o texto "onSurface" aparecia no comentario, e o teste
     * leu como uso. Documentar bem nao pode reprovar o build.
     *
     * ⚠️ Grosseiro de proposito: tira bloco e linha sem entender strings. Para
     * procurar chamada de funcao serve; para contar linha, nao serviria.
     */
    private fun semComentarios(texto: String): String =
        texto.replace(Regex("""/\*[\s\S]*?\*/"""), " ")
             .replace(Regex("""//[^\n]*"""), " ")

    private val editor by lazy {
        semComentarios(fonte("com/gurps/ficha/ui/features/rolagem/EditorDeNota.kt"))
    }
    private val lista by lazy {
        semComentarios(fonte("com/gurps/ficha/ui/features/rolagem/DialogoBlocoDeNotas.kt"))
    }

    // == 1. Excluir tem de excluir =====================================

    @Test
    fun `o editor avisa o onDispose de que a nota foi excluida`() {
        assertTrue(
            "sumiu a bandeira que impede a nota de ressuscitar",
            editor.contains("var foiExcluida by remember")
        )
        assertTrue(
            "o onDispose voltou a gravar sem conferir se a nota foi excluida",
            Regex("""onDispose\s*\{[\s\S]{0,200}?!foiExcluida""").containsMatchIn(editor)
        )
    }

    @Test
    fun `a bandeira e levantada ANTES de fechar a tela`() {
        // ⚠️ A ordem e a correcao inteira. Marcar depois do `onClose` nao adianta:
        // o `onClose` e que dispara o `onDispose`, e ele ja teria lido `false`.
        val trecho = editor.substringAfter("foiExcluida = true")
        assertTrue(
            "a bandeira nao e marcada antes de onExcluir/onClose",
            trecho.take(200).contains("onExcluir(nota.id)") && trecho.take(200).contains("onClose()")
        )
    }

    // == 2. A cor do texto sai do fundo, nao do tema ===================

    @Test
    fun `o editor pergunta a CorDaNota qual texto usar`() {
        assertTrue(
            "o editor voltou a pintar o texto pelo tema",
            editor.contains("CorDaNota.textoSobre(corSelecionada)")
        )
    }

    @Test
    fun `o texto que a pessoa DIGITA tambem segue a cor do fundo`() {
        // 🔴 Era o mais ilegivel dos tres, e o mais facil de esquecer: o
        // `TextFieldDefaults.colors` nao herda nada -- sem estas chaves o texto
        // digitado continua saindo de `onSurface`.
        assertTrue("faltou focusedTextColor no campo da nota",
            editor.contains("focusedTextColor = corDoTexto"))
        assertTrue("faltou unfocusedTextColor no campo da nota",
            editor.contains("unfocusedTextColor = corDoTexto"))
    }

    @Test
    fun `nenhum texto do editor volta a sair do tema`() {
        // Varredura: dentro do editor, `onSurface` nao pode mais aparecer.
        assertTrue(
            "voltou a usar cor do tema sobre o fundo colorido da nota",
            !editor.contains("MaterialTheme.colorScheme.onSurface")
        )
    }

    @Test
    fun `o cartao da lista pergunta a CorDaNota -- mas so quando ha cor`() {
        assertTrue(
            "o cartao voltou a pintar o texto pelo tema",
            lista.contains("CorDaNota.textoSobre(nota.corHex)")
        )
        // ⚠️ Sem cor, o fundo e `surfaceVariant`, que E do tema -- e ali a cor do
        // tema e a certa. Sobrepor tambem nesse caso trocaria um problema de
        // contraste por outro.
        assertTrue(
            "o cartao deixou de respeitar o tema quando a nota NAO tem cor",
            Regex("""corDaNota != null[\s\S]{0,300}?MaterialTheme\.colorScheme\.onSurface""")
                .containsMatchIn(lista)
        )
    }
}
