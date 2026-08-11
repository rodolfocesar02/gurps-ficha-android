package com.gurps.ficha.domain.rules

import com.google.gson.JsonParser
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Lote EQP-3** — nenhum nome de local chega torto à tela.
 *
 * ## Por que este teste varre o asset de verdade
 *
 * O conserto de acento é uma lista de palavras escrita à mão. Um teste que eu
 * montasse com exemplos meus só provaria que a lista casa com a lista — eu
 * escreveria o exemplo do jeito que a regra já lê.
 *
 * 🔴 E foi exatamente esse o buraco: a regra existia desde sempre, cobria
 * `cr?nio` e `cr<?>nio`, e **não** cobria `crnio`. Ninguém percebeu porque
 * nenhum teste tinha olhado o catálogo. O usuário viu, na foto de 11/08.
 *
 * ## O gabarito não é meu — é do próprio catálogo
 *
 * O `armaduras.v2.json` publica **duas** versões de cada local: o `localRaw`
 * (torto, como saiu do OCR) e o `locaisNorm` (a lista já normalizada, que o
 * gerador do catálogo produziu certo). O gabarito é o segundo.
 *
 * Assim a trava não depende do meu vocabulário: se amanhã entrar uma armadura
 * com o local `virlha`, este teste fica vermelho sozinho.
 */
class CatalogoSemTextoQuebradoTest {

    private fun asset(nome: String): File {
        val direto = File("src/main/assets/$nome")
        return if (direto.exists()) direto else File("app/src/main/assets/$nome")
    }

    /**
     * As palavras coletivas que o livro usa e que **não** são partes do corpo.
     * Não estão no `locaisNorm` porque não são um local — são um conjunto deles.
     */
    private val COLETIVOS = setOf("corpo", "membros", "traje", "completo")

    @Test
    fun `nenhum local de armadura chega torto depois do conserto`() {
        val arq = asset("armaduras.v2.json")
        assertTrue("nao encontrei ${arq.absolutePath}", arq.exists())

        val itens = JsonParser.parseString(arq.readText(Charsets.UTF_8))
            .asJsonObject.getAsJsonArray("items")

        // O vocabulário bom sai do próprio catálogo, não da minha cabeça.
        val vocabulario = mutableSetOf<String>()
        itens.forEach { el ->
            el.asJsonObject.getAsJsonArray("locaisNorm")?.forEach { n ->
                vocabulario.add(n.asString.trim().lowercase())
            }
        }
        assertTrue("o catalogo nao publicou locaisNorm", vocabulario.size >= 8)

        val tortos = mutableListOf<String>()
        itens.forEach { el ->
            val bruto = el.asJsonObject.get("localRaw")?.asString.orEmpty()
            if (bruto.isBlank()) return@forEach
            val consertado = TextoDoCatalogo.corrigir(bruto)
            consertado.split(",", ";", "/", "|", " ")
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() }
                .forEach { palavra ->
                    if (palavra !in vocabulario && palavra !in COLETIVOS) {
                        tortos.add("${el.asJsonObject.get("id").asString}: '$bruto' -> '$consertado' (palavra '$palavra')")
                    }
                }
        }

        assertTrue(
            "locais que continuam tortos depois do conserto:\n" + tortos.joinToString("\n"),
            tortos.isEmpty()
        )
    }

    @Test
    fun `o conserto so troca palavra inteira`() {
        // A âncora `\b` é o que impede a regra de comer o meio de uma palavra.
        // Sem ela, uma nota do jogador viraria outra coisa sem aviso.
        assertTrue(TextoDoCatalogo.corrigir("descrnioso").contains("crnio"))
        assertTrue(TextoDoCatalogo.corrigir("crnio, rosto").startsWith("cranio"))
        assertTrue(TextoDoCatalogo.corrigir("crnio, pescoo").contains("pescoco"))
    }
}
