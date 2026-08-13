package com.gurps.ficha.domain.rules

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Nenhum arquivo-fonte tem caractere de controle** — trava do lote POD-10.
 *
 * ## 🔴 Por que este teste existe
 *
 * Duas vezes na mesma sessão eu escrevi uma `Regex` num arquivo Kotlin passando
 * por escape de shell, e o `\b` (limite de palavra) virou um **BACKSPACE literal
 * (0x08)** dentro do padrão:
 *
 * ```
 * Regex("""Habilidades\s+d[eoa]<BS>""")          // POD-4
 * Regex("""(?:<BS>(?:para|com|como|ou)\s*)$""")  // POD-10
 * ```
 *
 * O efeito é o pior possível: o padrão **não casa com nada** e o teste passa. Ele
 * fica verde **por estar cego**, não por estar limpo. No POD-4 isso escondeu 45
 * descrições contaminadas; no POD-10, habilidades cortadas ao meio.
 *
 * ⚠️ A regra do projeto é escrever texto Kotlin pela ferramenta de edição, nunca
 * por heredoc de shell. Regra escrita não impede o erro — **este teste impede**.
 *
 * Um byte invisível não é coisa que se ache lendo o diff.
 */
class FonteSemCaractereDeControleTest {

    private fun raiz(): File {
        val direto = File("src")
        return if (direto.exists()) direto else File("app/src")
    }

    /** Tab (9), LF (10) e CR (13) são legítimos; o resto não tem o que fazer aqui. */
    private fun ehControleProibido(c: Char): Boolean {
        val n = c.code
        return (n < 32 && n != 9 && n != 10 && n != 13) || n == 127
    }

    @Test
    fun `nenhum kt tem byte de controle escondido`() {
        val culpados = mutableListOf<String>()
        raiz().walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { f ->
                val texto = f.readText(Charsets.UTF_8)
                texto.forEachIndexed { i, c ->
                    if (ehControleProibido(c)) {
                        val trecho = texto.substring(
                            maxOf(0, i - 45), minOf(texto.length, i + 15)
                        ).replace("\n", " ")
                        culpados += "${f.name}: U+%04X em '…%s…'".format(c.code, trecho)
                    }
                }
            }
        assertTrue(
            "caractere de controle em codigo-fonte:\n" + culpados.joinToString("\n"),
            culpados.isEmpty()
        )
    }
}
