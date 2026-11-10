package com.gurps.ficha.model

import com.gurps.ficha.domain.loaders.ItensCatalogLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **O item do livro entra na ficha com a regra dentro** — IC-2 do `mesa-virtual/planos/itens/PLANO.md`.
 *
 * Roda sobre o catálogo REAL (`itens.v1.json`): o que o diálogo "Adicionar Itens" mostra, o que entra no inventário, e o
 * que sai para a Mesa no arquivo da ficha. A costura: este teste grava `build/itens-do-catalogo-como-o-app-manda.json`, e
 * a Mesa lê a cópia dele em `server/test/amostras/` (IC-3).
 */
class ItemDoLivroTest {

    private val itens: List<ItemDoCatalogo> by lazy {
        val direto = File("src/main/assets/${ItensCatalogLoader.ARQUIVO}")
        val f = if (direto.exists()) direto else File("app/src/main/assets/${ItensCatalogLoader.ARQUIVO}")
        ItensCatalogLoader.ler(f.readText(Charsets.UTF_8))
    }

    private fun item(id: String) = itens.first { it.id == id }

    @Test
    fun `o leitor traz os 207 itens, e um arquivo estragado nao derruba a tela`() {
        assertEquals(207, itens.size)
        assertTrue(ItensCatalogLoader.ler("isto nao e json").isEmpty())
        assertTrue(ItensCatalogLoader.ler("{}").isEmpty())
    }

    @Test
    fun `a busca ignora acento e maiuscula, e a categoria filtra`() {
        assertTrue(ItensCatalogLoader.filtrar(itens, "veneno de cobra", null).any { it.id == "veneno_de_cobra" })
        assertTrue(ItensCatalogLoader.filtrar(itens, "FRAGMENTACAO", null).any { it.id == "granada_de_fragmentacao" })
        assertTrue("a busca tambem acha pelo que o item faz", ItensCatalogLoader.filtrar(itens, "primeiros socorros", null).any { it.id == "ataduras" })
        val venenos = ItensCatalogLoader.filtrar(itens, "", "Venenos")
        assertEquals(7, venenos.size)
        assertEquals(itens.size, ItensCatalogLoader.filtrar(itens, "", ItensCatalogLoader.TODAS).size)
        assertEquals(12, ItensCatalogLoader.categorias(itens).size)
    }

    @Test
    fun `o cartao diz o que o item faz, o preco e a pagina, sem nome cru de campo`() {
        val cobra = ItensCatalogLoader.linhasDoItem(item("veneno_de_cobra")).map { it.texto }
        assertTrue(cobra.toString(), cobra.any { it.contains("2d de toxina") })
        assertTrue(cobra.toString(), cobra.any { it == "Custo: $10 por dose · MB p.439" })
        val cura = ItensCatalogLoader.linhasDoItem(item("elixir_cura")).map { it.texto }
        assertTrue(cura.toString(), cura.any { it.startsWith("Formas: poção") })
        assertTrue(cura.toString(), cura.any { it.contains("Magia p.217") })
        val corda = ItensCatalogLoader.linhasDoItem(item("corda_3_8")).map { it.texto }
        assertTrue(corda.toString(), corda.any { it.contains("cada 10 metros") })
        itens.forEach { i ->
            val linhas = ItensCatalogLoader.linhasDoItem(i).joinToString(" ") { it.texto }
            listOf("narrativo", "acessorio", "municao", "pocao", "null").forEach { cru ->
                assertFalse("${i.id} mostra o nome cru `$cru`: $linhas", Regex("\\b$cru\\b").containsMatchIn(linhas))
            }
        }
    }

    @Test
    fun `o item escolhido entra no inventario com a regra, vai para a Mesa e volta do arquivo`() {
        val veneno = ItensCatalogLoader.equipamentoDoItem(item("veneno_de_cobra"), 3)
        assertEquals("Veneno de Cobra", veneno.nome)
        assertEquals(3, veneno.quantidade)
        assertEquals(TipoEquipamento.GERAL, veneno.tipo)
        assertEquals("veneno_de_cobra", veneno.itemCatalogoId)
        assertTrue(veneno.notas, veneno.notas.endsWith("MB p.439"))

        val p = Personagem(nome = "Jack", equipamentos = listOf(veneno))
        val eq = PersonagemInterop.fichaInteiraParaAMesa(p).getAsJsonArray("equipamentos")[0].asJsonObject
        val regra = eq.getAsJsonObject("regra")
        assertEquals("veneno", regra.get("tipo").asString)
        assertEquals("2d", regra.get("dano").asString)
        assertEquals(-3, regra.get("mod").asInt)
        assertEquals(6, regra.get("ciclos").asInt)
        assertEquals("veneno_de_cobra", eq.get("itemCatalogoId").asString)

        assertEquals(veneno, Personagem.fromJson(p.toJson()).equipamentos[0])
        val antiga = Personagem.fromJson("""{"nome":"V","equipamentos":[{"nome":"Corda","tipo":"GERAL"}]}""").equipamentos[0]
        assertNull(antiga.regra)
        assertNull(antiga.itemCatalogoId)
    }

    @Test
    fun `mexer na regra do item da ficha nao estraga o catalogo`() {
        val a = ItensCatalogLoader.equipamentoDoItem(item("tocha"))
        a.regra!!.addProperty("duracaoHoras", 99)
        assertEquals(1, item("tocha").regra!!.get("duracaoHoras").asInt)
    }

    @Test
    fun `o Cianureto chega a Mesa dizendo que nao tem teste (o nulo nao viaja)`() {
        val p = Personagem(nome = "J", equipamentos = listOf(ItensCatalogLoader.equipamentoDoItem(item("cianureto"))))
        val regra = Personagem.fromJson(p.toJson()).equipamentos[0].regra!!
        assertTrue(regra.get("semTeste").asBoolean)
    }

    @Test
    fun `grava o que o app manda, para o teste da Mesa usar`() {
        val ids = listOf("veneno_de_cobra", "arsenico", "cianureto", "granada_de_fragmentacao", "coquetel_molotov", "tocha",
            "mira_laser", "kit_de_primeiros_socorros", "ataduras", "elixir_cura", "elixir_forca", "flechas_de_ponta_fina", "corda_3_8")
        val p = Personagem(nome = "Itens", equipamentos = ids.map { ItensCatalogLoader.equipamentoDoItem(item(it), 2) })
        val saida = PersonagemInterop.fichaInteiraParaAMesa(p).getAsJsonArray("equipamentos")
        assertEquals(ids.size, saida.size())
        File("build/itens-do-catalogo-como-o-app-manda.json").also { it.parentFile?.mkdirs() }
            .writeText(com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(saida))
    }
}
