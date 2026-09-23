package com.gurps.ficha.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **O app é 100% em português do BRASIL** — tela, comentários e testes.
 *
 * > *"cuidado novamente, pra não escrever a documentação do app em PT! aqui no
 * > chat tudo bem, mas no app 100% brasileiro!"* (23/set/2026)
 *
 * O Rodolfo é brasileiro e mora em Portugal, e o app é para a mesa dele, no
 * Brasil. Os comentários deste projeto **são** a documentação dele — é o que
 * ele lê. Até 23/set o app não tinha guarda nenhum: a Mesa tinha, e mesmo lá
 * palavras como "partilhar" e "rebentar" passaram durante semanas.
 *
 * ## ⚠️ O que fica de fora, de propósito
 *
 * - **"Apanhar"** no sentido de levar golpe (*"quem apanhou já sabe onde"*) é
 *   brasileiro. Só "apanhar um defeito" é de Portugal, e a diferença é de
 *   sentido, não de palavra — um teste não a vê.
 * - **"Rato"** o bicho: a tabela de Tamanho do Alvo tem *"rato pequeno"*. Só o
 *   rato COM artigo definido (*"o rato"*, *"do rato"*) é o do computador.
 * - **A ênclise** (*"mandá-la"*, *"pede-se"*): é correta no Brasil por escrito.
 *
 * Irmão do `mesa-virtual/server/test/portugues-do-brasil.test.js`: mesma lista,
 * nos dois programas.
 */
class PortuguesDoBrasilTest {

    private data class Palavra(val errada: Regex, val certa: String)

    private val palavras = listOf(
        Palavra(Regex("""(?iU)\bpartilh(a|ar|ada|adas|ado|ados|am|ando|ou)\b"""), "compartilhar"),
        Palavra(Regex("""(?iU)\brebent(a|ar|ava|ou|am|ando|ada|ado)\b"""), "estourar"),
        Palavra(Regex("""(?iU)\bdeit(ar|a|ou|ava|ando|ado)\s+fora\b"""), "jogar fora"),
        Palavra(Regex("""(?iU)\becr[ãa]s?\b"""), "tela"),
        Palavra(Regex("""(?iU)\btelem[óo]ve(l|is)\b"""), "celular"),
        Palavra(Regex("""(?iU)\bficheiros?\b"""), "arquivo"),
        Palavra(Regex("""(?iU)\b(de|o)\s+facto\b"""), "de fato / o fato"),
        Palavra(Regex("""(?iU)\bcontactos?\b"""), "contato"),
        Palavra(Regex("""(?iU)\bregistos?\b"""), "registro"),
        Palavra(Regex("""(?iU)\bs[íi]tios?\b"""), "lugar"),
        Palavra(Regex("""(?iU)\bem\s+baixo\b"""), "embaixo"),
        Palavra(Regex("""(?iU)\b(o|do|no|ao|pelo)\s+rato\b"""), "mouse"),
        Palavra(Regex("""(?iU)\butilizador"""), "usuário"),
        Palavra(Regex("""(?iU)\b(teu|tua|teus|tuas|vosso|vossa)\b"""), "seu / sua"),
        // "está a fazer" e os primos. ⚠️ Menos "a par", que é expressão, e não verbo.
        Palavra(
            Regex("""(?iU)\b(está|estão|estava|estou|estar|continua|continuam|continuava|fica|ficou|anda|andam)\s+a\s+(?!par\b|lugar\b)\p{L}+(ar|er|ir)\b"""),
            "está fazendo / continua fazendo"
        ),
    )

    @Test
    fun `nenhum arquivo Kotlin do app fala portugues de Portugal`() {
        val esteArquivo = "PortuguesDoBrasilTest.kt"
        val arquivos = listOf(File("src/main/java"), File("src/test/java"))
            .filter { it.isDirectory }
            .flatMap { raiz -> raiz.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList() }
            .filter { it.name != esteArquivo }

        // ⚠️ A guarda da guarda: se o caminho estiver errado, a lista vem vazia
        // e o teste passa sem ter olhado para nada.
        assertTrue("o guarda nao achou os arquivos do app (caminho errado?)", arquivos.size > 100)

        val achados = arquivos.flatMap { arq ->
            arq.readLines().mapIndexedNotNull { i, linha ->
                palavras.firstOrNull { it.errada.containsMatchIn(linha) }?.let { p ->
                    "${arq.name}:${i + 1}  ${linha.trim().take(70)}\n      -> ${p.certa}"
                }
            }
        }
        assertTrue(
            "Português de Portugal no app (o app é 100% brasileiro):\n" + achados.joinToString("\n"),
            achados.isEmpty()
        )
    }

    @Test
    fun `o guarda morde - cada palavra da lista e acusada`() {
        // ⚠️ Sem isto, uma regra escrita errada (um \b que nunca casa com "ã", por
        // exemplo) seria um alarme instalado que nunca toca.
        listOf(
            "a tela partilhada", "o erro rebentava", "deitar fora o valor", "no ecrã",
            "o telemóvel", "o ficheiro", "de facto", "o contacto", "o registo",
            "num sítio", "em baixo", "o rato", "o utilizador", "o teu boneco",
            "continua a valer", "está a fazer"
        ).forEach { frase ->
            assertTrue("o guarda deixou passar: $frase", palavras.any { it.errada.containsMatchIn(frase) })
        }
        // E o que é brasileiro passa.
        listOf("quem apanhou já sabe onde", "rato pequeno", "está a par", "mandá-la").forEach { frase ->
            assertTrue("o guarda acusou português do Brasil: $frase", palavras.none { it.errada.containsMatchIn(frase) })
        }
    }
}
