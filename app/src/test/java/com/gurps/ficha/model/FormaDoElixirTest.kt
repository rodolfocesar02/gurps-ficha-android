package com.gurps.ficha.model

import com.gurps.ficha.domain.loaders.ItensCatalogLoader
import com.gurps.ficha.ui.features.equipamento.FormaDoElixirRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **A forma do elixir** — IC-5b do `mesa-virtual/planos/itens/PLANO.md` (Magia p.213).
 * A costura: grava `build/elixires-como-o-app-manda.json`, que a Mesa lê em `server/test/amostras/`.
 */
class FormaDoElixirTest {

    private val catalogo: List<ItemDoCatalogo> by lazy {
        val direto = File("src/main/assets/${ItensCatalogLoader.ARQUIVO}")
        ItensCatalogLoader.ler((if (direto.exists()) direto else File("app/src/main/assets/${ItensCatalogLoader.ARQUIVO}")).readText(Charsets.UTF_8))
    }
    private fun item(id: String) = catalogo.first { it.id == id }

    @Test
    fun `as formas que o livro aceita saem da regra do elixir`() {
        assertEquals(listOf("pocao", "po", "unguento"), FormaDoElixirRules.formas(item("elixir_forca")))
        assertEquals(listOf("pocao", "po", "pastilha", "unguento"), FormaDoElixirRules.formas(item("elixir_resistencia")))
        assertEquals(listOf("pastilha"), FormaDoElixirRules.formas(item("elixir_mata_dragao")))
        assertEquals("pastilha", FormaDoElixirRules.unica(item("elixir_mata_dragao")))
        assertNull("com mais de uma forma, a escolha é do jogador", FormaDoElixirRules.unica(item("elixir_forca")))
        assertTrue("o que não é elixir não tem forma", FormaDoElixirRules.formas(item("kit_antitoxinas")).isEmpty())
    }

    @Test
    fun `todo elixir do catalogo tem pelo menos uma forma conhecida`() {
        val elixires = catalogo.filter { ItensCatalogLoader.tipoDe(it) == "elixir" }
        assertEquals(67, elixires.size)
        elixires.forEach { assertTrue(it.id, FormaDoElixirRules.formas(it).isNotEmpty()) }
    }

    @Test
    fun `a forma escolhida entra no item, e uma forma que o livro nao aceita fica de fora`() {
        val unguento = ItensCatalogLoader.equipamentoDoItem(item("elixir_forca"), 2, null, "unguento")
        assertEquals("unguento", unguento.formaDoElixir)
        assertNull("pastilha não existe para o Elixir de Força", ItensCatalogLoader.equipamentoDoItem(item("elixir_forca"), 1, null, "pastilha").formaDoElixir)
        assertNull(ItensCatalogLoader.equipamentoDoItem(item("elixir_forca")).formaDoElixir)
        assertEquals("Forma: Unguento", FormaDoElixirRules.linha(unguento))
        assertTrue(FormaDoElixirRules.linha(ItensCatalogLoader.equipamentoDoItem(item("elixir_forca")))!!.startsWith("Forma não escolhida"))
        assertNull("só o elixir tem a linha", FormaDoElixirRules.linha(ItensCatalogLoader.equipamentoDoItem(item("tocha"))))
    }

    @Test
    fun `a forma vai para a Mesa e volta do arquivo, e a ficha antiga abre sem ela`() {
        val p = Personagem(nome = "Ana", equipamentos = listOf(ItensCatalogLoader.equipamentoDoItem(item("elixir_forca"), 1, null, "po")))
        val eq = PersonagemInterop.fichaInteiraParaAMesa(p).getAsJsonArray("equipamentos")[0].asJsonObject
        assertEquals("po", eq.get("formaDoElixir").asString)
        assertEquals(p.equipamentos, Personagem.fromJson(p.toJson()).equipamentos)
        val antiga = Personagem.fromJson("""{"nome":"V","equipamentos":[{"nome":"Elixir de Força","tipo":"GERAL","regra":{"tipo":"elixir","formas":["pocao","po"]}}]}""").equipamentos[0]
        assertNull(antiga.formaDoElixir)
    }

    @Test
    fun `grava o que o app manda, para o teste da Mesa usar`() {
        val itens = listOf(
            ItensCatalogLoader.equipamentoDoItem(item("elixir_forca"), 1, null, "pocao"),
            ItensCatalogLoader.equipamentoDoItem(item("elixir_sabedoria"), 1, null, "po"),
            ItensCatalogLoader.equipamentoDoItem(item("elixir_visao")),                       // so unguento: entra sozinho
            ItensCatalogLoader.equipamentoDoItem(item("elixir_medo"), 1, null, "pastilha"),
            ItensCatalogLoader.equipamentoDoItem(item("elixir_forca"))                         // ficha antiga: sem a escolha
        )
        val saida = PersonagemInterop.fichaInteiraParaAMesa(Personagem(nome = "Ana", equipamentos = itens)).getAsJsonArray("equipamentos")
        assertEquals(5, saida.size())
        File("build/elixires-como-o-app-manda.json").also { it.parentFile?.mkdirs() }
            .writeText(com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(saida))
    }
}
