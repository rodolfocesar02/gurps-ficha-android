package com.gurps.ficha.domain.rules

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Lote EQP-5** — nenhum escudo chega à ficha com a nota crua.
 *
 * O defeito que este arquivo tranca: adicionar o *Escudo Grande* deixava o campo
 * *Notas* com `[2, 4, 6]`. Não é texto cortado nem truncado — é a referência sem
 * a referência, e na tela de edição parece dado corrompido.
 */
class NotasDoEscudoTest {

    private fun asset(nome: String): File {
        val direto = File("src/main/assets/$nome")
        return if (direto.exists()) direto else File("app/src/main/assets/$nome")
    }

    private fun escudosDoCatalogo(): List<Pair<String, String>> {
        val arq = asset("escudos.v1.json")
        assertTrue("nao encontrei ${arq.absolutePath}", arq.exists())
        val raiz = JsonParser.parseString(arq.readText(Charsets.UTF_8))
        val itens = if (raiz.isJsonObject) raiz.asJsonObject.getAsJsonArray("items") else raiz.asJsonArray
        return itens.map { el ->
            val o = el.asJsonObject
            o.get("nome").asString to (o.get("observacoes")?.asString.orEmpty())
        }
    }

    @Test
    fun `todo escudo do catalogo tem a nota por extenso`() {
        // ⚠️ Varre o asset de verdade. Um exemplo meu só provaria que a legenda
        // casa com a legenda; o que interessa é se ela cobre os SETE escudos
        // que o jogador realmente vê na lista.
        val semTexto = mutableListOf<String>()
        escudosDoCatalogo().forEach { (nome, obs) ->
            if (obs.isBlank()) return@forEach
            val texto = NotasDoEscudo.paraAsNotas(obs)
            if (texto.isBlank()) semTexto.add("$nome: '$obs' nao virou texto nenhum")
        }
        assertTrue(semTexto.joinToString("\n"), semTexto.isEmpty())
    }

    @Test
    fun `a nota que vai para a ficha nunca e so o numero`() {
        // A invariante que descreve o defeito: cada linha tem de ter texto DEPOIS
        // do colchete. `[2, 4, 6]` sozinho reprova.
        escudosDoCatalogo().forEach { (nome, obs) ->
            NotasDoEscudo.explicar(obs).forEach { linha ->
                val depoisDoColchete = linha.substringAfter("]").trim()
                assertTrue(
                    "$nome: a linha '$linha' nao tem texto depois da referencia",
                    depoisDoColchete.length > 10
                )
            }
        }
    }

    @Test
    fun `o Escudo Grande traz as tres notas dele`() {
        // O caso da foto do usuario: "[2, 4, 6]".
        val linhas = NotasDoEscudo.explicar("[2, 4, 6]")
        assertEquals(3, linhas.size)
        assertTrue(linhas[0].contains("golpe com o escudo"))
        assertTrue(linhas[2].contains("penalidade"))
        assertFalse(NotasDoEscudo.paraAsNotas("[2, 4, 6]").trim() == "[2, 4, 6]")
    }

    @Test
    fun `referencia que o livro nao tem nao vira linha solta`() {
        // Melhor faltar uma linha do que a ficha ganhar um "[9]" sem texto.
        assertTrue(NotasDoEscudo.explicar("[9]").isEmpty())
        assertEquals(1, NotasDoEscudo.explicar("[1, 9]").size)
    }

    @Test
    fun `sem observacao nao inventa nota`() {
        assertTrue(NotasDoEscudo.explicar(null).isEmpty())
        assertTrue(NotasDoEscudo.explicar("").isEmpty())
        assertTrue(NotasDoEscudo.explicar("   ").isEmpty())
    }

    @Test
    fun `a legenda tem as seis notas do livro`() {
        // MB p.288 traz [1] a [6]. Se uma sumir na edição, isto acusa.
        assertEquals(6, NotasDoEscudo.TOTAL)
        (1..6).forEach { n ->
            assertTrue("faltou a nota [$n]", NotasDoEscudo.explicar("[$n]").size == 1)
        }
    }
}
