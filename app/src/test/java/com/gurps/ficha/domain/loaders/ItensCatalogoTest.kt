package com.gurps.ficha.domain.loaders

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **IC-1** — o catálogo de itens do livro (`itens.v1.json`), varrido inteiro.
 *
 * O catálogo é gerado dos PDFs por `scripts/itens/gerar_itens.py`; quem confere se ele ainda bate com o livro é o
 * próprio gerador (`--conferir`, que relê as páginas). Aqui fica a guarda que roda em todo `gradlew test`, sem os
 * PDFs: a forma que o app e a Mesa vão ler, e os números que o Rodolfo conhece de cor.
 */
class ItensCatalogoTest {

    private val tipos = setOf("narrativo", "kit", "luz", "concede", "cura", "acessorio", "explosivo", "municao", "veneno", "elixir")

    private fun itens(): List<JsonObject> {
        val direto = File("src/main/assets/itens.v1.json")
        val f = if (direto.exists()) direto else File("app/src/main/assets/itens.v1.json")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return JsonParser.parseString(f.readText(Charsets.UTF_8)).asJsonObject.getAsJsonArray("itens").map { it.asJsonObject }
    }

    private fun item(id: String) = itens().first { it.get("id").asString == id }
    private fun regra(id: String) = item(id).getAsJsonObject("regra")

    @Test
    fun `todo item tem id unico, nome, pagina, livro e um tipo de regra conhecido`() {
        val todos = itens()
        assertEquals("o livro lista 207 itens", 207, todos.size)
        assertEquals("id repetido", todos.size, todos.map { it.get("id").asString }.toSet().size)
        todos.forEach { i ->
            val id = i.get("id").asString
            assertTrue("$id sem nome", i.get("nome").asString.isNotBlank())
            assertTrue("$id sem pagina", i.get("pagina").asInt > 0)
            assertTrue("$id com livro estranho", i.get("livro").asString in setOf("MB", "Magia"))
            assertTrue("$id com tipo estranho", i.getAsJsonObject("regra").get("tipo").asString in tipos)
            assertTrue("$id: a nota nao pode ser texto longo do livro", i.get("nota").asString.length <= 120)
        }
    }

    @Test
    fun `a contagem por tipo e a que o gerador declarou`() {
        val f = File("src/main/assets/itens.v1.json").let { if (it.exists()) it else File("app/src/main/assets/itens.v1.json") }
        val declarado = JsonParser.parseString(f.readText(Charsets.UTF_8)).asJsonObject.getAsJsonObject("tipos")
        val contado = itens().groupingBy { it.getAsJsonObject("regra").get("tipo").asString }.eachCount()
        declarado.keySet().forEach { assertEquals("tipo $it", declarado.get(it).asInt, contado[it]) }
        assertEquals(67, contado["elixir"])
        assertEquals(9, contado["explosivo"])
        assertEquals(7, contado["veneno"])
    }

    @Test
    fun `o Veneno de Cobra e o Arsenico tem os numeros do livro (MB p439)`() {
        val cobra = regra("veneno_de_cobra")
        assertEquals(60, cobra.get("retardo").asInt)
        assertEquals(-3, cobra.get("mod").asInt)
        assertEquals("2d", cobra.get("dano").asString)
        assertEquals(3600, cobra.get("intervalo").asInt)
        assertEquals(6, cobra.get("ciclos").asInt)
        assertEquals(3, cobra.getAsJsonArray("sintomas").size())
        val arsenico = regra("arsenico")
        assertEquals(3600, arsenico.get("retardo").asInt)
        assertEquals(-2, arsenico.get("mod").asInt)
        assertEquals(8, arsenico.get("ciclos").asInt)
        assertTrue("o Cianureto nao tem teste de HT", regra("cianureto").get("mod").isJsonNull)
    }

    @Test
    fun `a Granada de Fragmentacao e o Molotov tem os numeros do livro (MB p277 e p411)`() {
        val frag = regra("granada_de_fragmentacao")
        assertEquals("4d", frag.get("expr").asString)
        assertEquals(2, frag.get("frag").asInt)
        assertEquals(4, frag.get("estopim").asInt)
        assertTrue(frag.get("pino").asBoolean)
        val molotov = regra("coquetel_molotov")
        assertEquals("3d", molotov.get("expr").asString)
        assertEquals("1d", molotov.get("porSegundo").asString)
        assertEquals(12, molotov.get("mauf").asInt)
        assertTrue(molotov.get("acender").asBoolean)
    }

    @Test
    fun `os itens de mesa de fantasia que ele mais usa`() {
        assertEquals(1, regra("tocha").get("duracaoHoras").asInt)
        assertEquals(1, regra("kit_de_primeiros_socorros").get("bonus").asInt)
        assertTrue(regra("ataduras").get("basico").asBoolean)
        assertEquals(1, regra("mira_laser").get("bonusNh").asInt)
        assertEquals(2, regra("flechas_de_ponta_fina").get("divisor").asInt)
        val cura = regra("elixir_cura").getAsJsonArray("efeitos")
        assertEquals("1d", cura[0].asJsonObject.get("cura").asString)
        assertEquals(120, item("elixir_cura").get("custo").asInt)
        val forca = regra("elixir_forca").getAsJsonArray("efeitos")[0].asJsonObject
        assertEquals("ST", forca.get("atributo").asString)
        assertEquals("+1d", forca.get("mod").asString)
    }
}
