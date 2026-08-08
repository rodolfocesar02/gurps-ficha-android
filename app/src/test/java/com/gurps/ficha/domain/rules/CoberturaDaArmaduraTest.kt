package com.gurps.ficha.domain.rules

import com.google.gson.JsonParser
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lote MB-7** — o tradutor de "onde a armadura cobre".
 *
 * ## 🔴 Por que a varredura no catálogo real é o teste que importa
 *
 * Dá para escrever dez asserções bonitas com `"tronco"` e `"braços"` e passar
 * todas — e ainda assim o app errar no aparelho, porque o catálogo de verdade
 * escreve **`"crnio"`** e **`"pescoo"`** (a extração do livro comeu o `â` e o
 * `ç`), além de `"traje completo"` e `"corpo, membros"`.
 *
 * Então o teste central aqui pega os **72 itens reais** do `armaduras.v2.json` e
 * exige que o tradutor devolva **exatamente** o `locaisNorm` que o próprio
 * catálogo publica. É o catálogo conferindo o código, não eu conferindo os dois.
 */
class CoberturaDaArmaduraTest {

    private fun catalogo(): List<Pair<String, Set<String>>>? {
        val arq = listOf("src/main/assets/armaduras.v2.json", "app/src/main/assets/armaduras.v2.json")
            .map { File(it) }.firstOrNull { it.exists() } ?: return null
        val raiz = JsonParser.parseString(arq.readText()).asJsonObject
        return raiz.getAsJsonArray("items").map { el ->
            val o = el.asJsonObject
            val raw = o.get("localRaw")?.asString.orEmpty()
            val norm = o.getAsJsonArray("locaisNorm")?.map { it.asString }?.toSet() ?: emptySet()
            raw to norm
        }
    }

    /** O vocabulário do JSON traduzido para o enum do app. */
    private fun daPalavra(p: String): LocalAtaque = when (p) {
        "tronco" -> LocalAtaque.TORSO
        "virilha" -> LocalAtaque.INGLE
        "pernas" -> LocalAtaque.PERNA
        "bracos" -> LocalAtaque.BRACO
        "cranio" -> LocalAtaque.CRANIO
        "pes" -> LocalAtaque.PE
        "maos" -> LocalAtaque.MAO
        "pescoco" -> LocalAtaque.PESCOCO
        "rosto" -> LocalAtaque.ROSTO
        "olhos" -> LocalAtaque.OLHO
        else -> error("palavra '$p' fora do vocabulário do catálogo")
    }

    // ==================================================================
    // 1. 🔴 A varredura no catálogo de verdade
    // ==================================================================

    @Test
    fun `🔴 os 72 itens reais traduzem exatamente como o catalogo diz`() {
        val itens = catalogo() ?: return
        assertTrue("o catálogo encolheu — confira o arquivo", itens.size >= 70)
        itens.forEach { (raw, norm) ->
            val esperado = norm.map { daPalavra(it) }.toSet()
            assertEquals("localRaw '$raw'", esperado, CoberturaDaArmadura.locaisDe(raw))
        }
    }

    @Test
    fun `⚠️ as letras que faltam no catalogo sao entendidas mesmo assim`() {
        // "crnio" e "pescoo" estão gravados assim em 9 itens do JSON.
        assertEquals(setOf(LocalAtaque.CRANIO), CoberturaDaArmadura.locaisDe("crnio"))
        assertEquals(setOf(LocalAtaque.PESCOCO), CoberturaDaArmadura.locaisDe("pescoo"))
        // E a forma certa continua funcionando, para o dia em que corrigirem.
        assertEquals(setOf(LocalAtaque.CRANIO), CoberturaDaArmadura.locaisDe("crânio"))
        assertEquals(setOf(LocalAtaque.PESCOCO), CoberturaDaArmadura.locaisDe("pescoço"))
    }

    @Test
    fun `⚠️ traje completo NAO cobre a cabeca`() {
        // O elmo é comprado à parte. Se o traje cobrisse o crânio, o jogador
        // levaria uma RD que não existe no lugar mais perigoso da tabela.
        val locais = CoberturaDaArmadura.locaisDe("traje completo")
        assertTrue(LocalAtaque.TORSO in locais)
        assertTrue(LocalAtaque.MAO in locais)
        assertTrue("o traje não vem com elmo", LocalAtaque.CRANIO !in locais)
        assertTrue(LocalAtaque.ROSTO !in locais)
    }

    @Test
    fun `⚠️ traje completo nao e confundido com tronco`() {
        // "traje completo" contém a palavra... nenhuma outra, mas a ordem do
        // vocabulário é o que garante isso. Se alguém reordenar a lista, este
        // teste cai.
        assertTrue(CoberturaDaArmadura.locaisDe("traje completo").size > 1)
    }

    @Test
    fun `lista separada por virgula, barra ou e`() {
        val esperado = setOf(LocalAtaque.BRACO, LocalAtaque.PERNA)
        assertEquals(esperado, CoberturaDaArmadura.locaisDe("braços, pernas"))
        assertEquals(esperado, CoberturaDaArmadura.locaisDe("braços e pernas"))
        assertEquals(esperado, CoberturaDaArmadura.locaisDe("braços/pernas"))
    }

    @Test
    fun `texto vazio ou desconhecido nao cobre nada`() {
        assertTrue(CoberturaDaArmadura.locaisDe(null).isEmpty())
        assertTrue(CoberturaDaArmadura.locaisDe("").isEmpty())
        assertTrue(CoberturaDaArmadura.locaisDe("capa de chuva").isEmpty())
    }

    // ==================================================================
    // 2. 🔴 Os vitais moram dentro do tronco
    // ==================================================================

    @Test
    fun `🔴 quem protege o tronco protege os vitais`() {
        // O livro não vende peitoral "para os órgãos vitais". Sem esta linha um
        // personagem de cota de malha levaria o TRIPLO de dano perfurante nos
        // vitais como se estivesse nu.
        assertTrue(CoberturaDaArmadura.cobre("tronco", LocalAtaque.VITAIS))
        assertTrue(CoberturaDaArmadura.cobre("traje completo", LocalAtaque.VITAIS))
        // Um elmo, não.
        assertTrue(!CoberturaDaArmadura.cobre("crnio", LocalAtaque.VITAIS))
    }

    @Test
    fun `cobre responde certo para os locais simples`() {
        assertTrue(CoberturaDaArmadura.cobre("tronco, virilha", LocalAtaque.INGLE))
        assertTrue(!CoberturaDaArmadura.cobre("tronco, virilha", LocalAtaque.PERNA))
    }

    // ==================================================================
    // 3. A RD escrita do jeito do catálogo
    // ==================================================================

    @Test
    fun `🔴 a RD lida bate com a que o proprio catalogo calculou`() {
        val arq = listOf("src/main/assets/armaduras.v2.json", "app/src/main/assets/armaduras.v2.json")
            .map { File(it) }.firstOrNull { it.exists() } ?: return
        val raiz = JsonParser.parseString(arq.readText()).asJsonObject
        raiz.getAsJsonArray("items").forEach { el ->
            val o = el.asJsonObject
            val raw = o.get("rdRaw")?.asString.orEmpty()
            val estruturado = o.getAsJsonObject("rd")
            val lida = CoberturaDaArmadura.rdDe(raw)
            assertEquals("rdRaw '$raw' principal", estruturado.get("principal").asInt, lida!!.principal)
            assertEquals("rdRaw '$raw' flexível", estruturado.get("flexivel").asBoolean, lida.flexivel)
            val sec = estruturado.get("secundario")
            val esperadoSec = if (sec.isJsonNull) null else sec.asInt
            assertEquals("rdRaw '$raw' secundária", esperadoSec, lida.secundaria)
        }
    }

    @Test
    fun `os formatos que o catalogo escreve`() {
        assertEquals(4, CoberturaDaArmadura.rdDe("4")!!.principal)
        assertTrue("o asterisco é flexível", CoberturaDaArmadura.rdDe("1*")!!.flexivel)
        assertEquals(2, CoberturaDaArmadura.rdDe("4/2*")!!.secundaria)
        assertTrue("o D é só na frente", CoberturaDaArmadura.rdDe("5D")!!.frontalSomente)
        assertTrue("o + é RD adicional", CoberturaDaArmadura.rdDe("+20")!!.adicional)
        assertEquals(20, CoberturaDaArmadura.rdDe("+20")!!.principal)
    }

    @Test
    fun `RD vazia ou sem numero e ausencia, nao zero`() {
        // Devolver 0 faria uma peça sem dado parecer uma peça que não protege —
        // são coisas diferentes na tela.
        assertNull(CoberturaDaArmadura.rdDe(null))
        assertNull(CoberturaDaArmadura.rdDe(""))
        assertNull(CoberturaDaArmadura.rdDe("—"))
        assertEquals(0, CoberturaDaArmadura.rdDe("0")!!.principal)
    }

    @Test
    fun `a RD total soma as pecas vestidas`() {
        val pecas = listOf(
            CoberturaDaArmadura.Peca("Cota de Malha", CoberturaDaArmadura.rdDe("4")!!),
            CoberturaDaArmadura.Peca("Gibão", CoberturaDaArmadura.rdDe("2*")!!)
        )
        assertEquals(6, CoberturaDaArmadura.rdTotal(pecas))
        assertEquals(0, CoberturaDaArmadura.rdTotal(emptyList()))
    }
}
