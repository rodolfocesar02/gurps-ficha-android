package com.gurps.ficha.domain.magic

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.MagiaDefinicao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.io.File

/**
 * **O custo que DEPENDE DA CONDIÇÃO** — a conta do app, no mesmo catálogo e nos mesmos casos que a Mesa roda.
 *
 * Os 73 casos de `src/test/resources/custo_por_condicao_casos.json` saem do TEXTO DO LIVRO (não da conta): o que
 * cada magia custa com o que o jogador informa. O app lê o catálogo como o carrega (Gson em [MagiaDefinicao]) e a
 * Mesa roda os mesmos casos em JavaScript (`test/custo-por-condicao.test.js`).
 */
class CustoPorCondicaoTest {

    private val arquivo = listOf("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")
        .map { File(it) }.firstOrNull { it.exists() }

    private val catalogo: Map<String, MagiaDefinicao> by lazy {
        val tipo = object : TypeToken<List<MagiaDefinicao>>() {}.type
        Gson().fromJson<List<MagiaDefinicao>>(arquivo!!.readText(Charsets.UTF_8), tipo).associateBy { it.id }
    }

    private fun spec(id: String): CustoPorCondicao = catalogo.getValue(id).custoPorCondicao
        ?: throw AssertionError("$id sem custoPorCondicao no catálogo")

    private fun calcular(id: String, vararg condicoes: Pair<String, Any?>) =
        CustoPorCondicaoRules.calcular(spec(id), mapOf(*condicoes))

    @Test
    fun `os 73 casos do livro - operar e manter saem como o livro manda`() {
        Assume.assumeNotNull(arquivo)
        val texto = javaClass.classLoader!!.getResourceAsStream("custo_por_condicao_casos.json")!!
            .bufferedReader(Charsets.UTF_8).readText()
        val casos = JsonParser.parseString(texto).asJsonObject.getAsJsonArray("casos")
        assertTrue(casos.size() >= 70)
        casos.forEach { c ->
            val k = c.asJsonObject
            val id = k.get("id").asString
            val condicoes: Map<String, Any?> = k.getAsJsonObject("condicoes").entrySet().associate { (nome, v) ->
                nome to when {
                    v.asJsonPrimitive.isBoolean -> v.asBoolean
                    v.asJsonPrimitive.isNumber -> v.asDouble
                    else -> v.asString
                }
            }
            val nome = "$id $condicoes"
            val r = CustoPorCondicaoRules.calcular(spec(id), condicoes)
            assertTrue("$nome: $r", r is CustoPorCondicaoRules.Resultado.Ok)
            r as CustoPorCondicaoRules.Resultado.Ok
            assertEquals("$nome operar", k.get("operar").asInt, r.operar)
            assertEquals("$nome manter", if (k.get("manter").isJsonNull) null else k.get("manter").asInt, r.manter)
            assertEquals(
                "$nome tipo de manter",
                if (k.get("manterTipo").isJsonNull) null else k.get("manterTipo").asString,
                r.manterTipo
            )
        }
    }

    @Test
    fun `as 18 magias do catalogo trazem a conta e ela fecha com os valores padrao`() {
        Assume.assumeNotNull(arquivo)
        val ids = listOf(
            "alongar_objeto", "animar_objeto", "animar_planta", "aumentar_objeto", "brilho", "contrair_objeto",
            "criar_animal", "criar_montaria", "despertar_computadornt", "encolher_objeto", "fortalecer", "mao_magica",
            "reconstruirnt", "transformar_objeto", "apressar", "chicote_de_relampago", "copiar", "teia_de_aranha"
        )
        ids.forEach { id ->
            val s = spec(id)
            val r = CustoPorCondicaoRules.calcular(s, CustoPorCondicaoRules.condicoesPadrao(s))
            assertTrue("$id: $r", r is CustoPorCondicaoRules.Resultado.Ok)
        }
    }

    @Test
    fun `o exemplo dele - Alongar Objeto de 1 e meio kg duplicado duas vezes custa 6`() {
        Assume.assumeNotNull(arquivo)
        val r = calcular("alongar_objeto", "peso" to 1.5, "duplicacoes" to 2.0) as CustoPorCondicaoRules.Resultado.Ok
        assertEquals(6, r.operar)
        assertEquals(6, r.manter)
        assertEquals(
            "Peso original do objeto 1,5 kg; Duplicações da dimensão escolhida 2",
            CustoPorCondicaoRules.resumo(spec("alongar_objeto"), mapOf("peso" to 1.5, "duplicacoes" to 2.0))
        )
    }

    @Test
    fun `o jogador digita com virgula e o arredondamento e sempre para cima`() {
        Assume.assumeNotNull(arquivo)
        val r = calcular("alongar_objeto", "peso" to "0,7", "duplicacoes" to "1") as CustoPorCondicaoRules.Resultado.Ok
        assertEquals(2, r.operar)
        val teia = calcular("teia_de_aranha", "comprimento" to 15.0) as CustoPorCondicaoRules.Resultado.Ok
        assertEquals(2, teia.manter)
    }

    @Test
    fun `a conta recusa em vez de chutar - faixa, opcao, tipo e teto`() {
        Assume.assumeNotNull(arquivo)
        fun erro(r: CustoPorCondicaoRules.Resultado) = (r as CustoPorCondicaoRules.Resultado.Erro).erro
        assertEquals("condicao_invalida", erro(calcular("alongar_objeto", "peso" to -1.0)))
        assertEquals("condicao_invalida", erro(calcular("alongar_objeto", "peso" to "muito")))
        assertEquals("condicao_invalida", erro(calcular("alongar_objeto", "peso" to 1.0, "duplicacoes" to 1.5)))
        assertEquals("condicao_invalida", erro(calcular("alongar_objeto", "peso" to 1.0, "duplicacoes" to 0.0)))
        assertEquals("condicao_invalida", erro(calcular("alongar_objeto", "peso" to 1.0e9)))
        assertEquals("condicao_invalida", erro(calcular("transformar_objeto", "peso" to 1.0, "material" to "ouro")))
        assertEquals("condicao_invalida", erro(calcular("criar_montaria", "bruta" to "sim")))
        assertEquals("custo_fora_da_faixa", erro(calcular("alongar_objeto", "peso" to 100.0, "duplicacoes" to 20.0)))
        val solta = Gson().fromJson("""{"entradas":[],"operar":["eval",1]}""", CustoPorCondicao::class.java)
        assertEquals("conta_invalida", erro(CustoPorCondicaoRules.calcular(solta, emptyMap())))
        val semId = Gson().fromJson("""{"entradas":[],"operar":"${'$'}nao_existe"}""", CustoPorCondicao::class.java)
        assertEquals("conta_invalida", erro(CustoPorCondicaoRules.calcular(semId, emptyMap())))
        assertEquals("conta_invalida", erro(CustoPorCondicaoRules.calcular(null, emptyMap())))
    }

    @Test
    fun `Despertar Computador e fixo e nao pergunta nada`() {
        Assume.assumeNotNull(arquivo)
        assertTrue(spec("despertar_computadornt").entradas.isEmpty())
        val r = CustoPorCondicaoRules.calcular(spec("despertar_computadornt"), emptyMap()) as CustoPorCondicaoRules.Resultado.Ok
        assertEquals(8, r.operar)
        assertEquals(2, r.manter)
    }

    @Test
    fun `onde o custo e uma conta o catalogo nao guarda mais o numero-base errado`() {
        Assume.assumeNotNull(arquivo)
        listOf(
            "alongar_objeto", "aumentar_objeto", "contrair_objeto", "encolher_objeto", "transformar_objeto", "animar_objeto",
            "fortalecer", "apressar", "copiar", "teia_de_aranha", "criar_animal", "reconstruirnt", "chicote_de_relampago", "brilho"
        ).forEach { id ->
            val m = catalogo.getValue(id)
            assertNull("$id: custoOperar", m.custoOperar)
            assertNull("$id: custoManter", m.custoManter)
            assertNotNull(id, m.custoPorCondicao)
        }
        // as de base fixa com condições guardam o número-base do livro
        assertEquals(8, catalogo.getValue("criar_montaria").custoOperar)
        assertEquals(3, catalogo.getValue("criar_montaria").custoManter)
    }
}
