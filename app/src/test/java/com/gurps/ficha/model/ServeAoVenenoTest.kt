package com.gurps.ficha.model

import com.gurps.ficha.domain.loaders.ItensCatalogLoader
import com.gurps.ficha.ui.features.equipamento.ServeAoVenenoRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **O Kit Antitoxinas serve a UM veneno** — IC-4B do `mesa-virtual/planos/itens/PLANO.md` (pedido do Rodolfo, 11/nov).
 *
 * "Antídoto para um veneno específico" (MB p.289): o jogador escolhe na compra, e o campo vai para a Mesa no arquivo da ficha. A costura:
 * grava `build/kits-antitoxinas-como-o-app-manda.json`, que a Mesa lê em `server/test/amostras/`.
 */
class ServeAoVenenoTest {

    private val catalogo: List<ItemDoCatalogo> by lazy {
        val direto = File("src/main/assets/${ItensCatalogLoader.ARQUIVO}")
        ItensCatalogLoader.ler((if (direto.exists()) direto else File("app/src/main/assets/${ItensCatalogLoader.ARQUIVO}")).readText(Charsets.UTF_8))
    }
    private fun item(id: String) = catalogo.first { it.id == id }
    private val doLivro by lazy { ServeAoVenenoRules.doLivro(catalogo) }

    @Test
    fun `a lista traz os 7 venenos do livro, com o id do catalogo`() {
        assertEquals(
            setOf("arsenico", "cianureto", "fumaca", "gas_lacrimogeneo", "gas_mostarda", "gas_nervoso", "veneno_de_cobra"),
            doLivro.mapNotNull { it.id }.toSet()
        )
        assertEquals(7, doLivro.size)
        assertEquals("Veneno de Cobra", doLivro.first { it.id == "veneno_de_cobra" }.nome)
    }

    @Test
    fun `so o Kit Antitoxinas e antidoto`() {
        assertTrue(ServeAoVenenoRules.ehAntidoto(item("kit_antitoxinas")))
        assertEquals(1, catalogo.count { ServeAoVenenoRules.ehAntidoto(it) })
        assertFalse(ServeAoVenenoRules.ehAntidoto(item("ataduras")))
        assertFalse(ServeAoVenenoRules.ehAntidoto(item("veneno_de_cobra")))
    }

    @Test
    fun `o inventario entra na lista, o item de catalogo e o digitado a mao, sem repetir o livro`() {
        val cobra = ItensCatalogLoader.equipamentoDoItem(item("veneno_de_cobra"))
        val aranha = Equipamento(nome = "Peçonha de Aranha", tipo = TipoEquipamento.GERAL, quantidade = 2)
        val corda = Equipamento(nome = "Corda", tipo = TipoEquipamento.GERAL)
        val confiscada = Equipamento(nome = "Toxina Roubada", tipo = TipoEquipamento.GERAL, confiscado = true)
        val doInventario = ServeAoVenenoRules.doInventario(listOf(cobra, aranha, corda, confiscada), doLivro)
        assertEquals(listOf("Peçonha de Aranha"), doInventario.map { it.nome })
        assertNull(doInventario[0].id)
        assertTrue(ServeAoVenenoRules.mesmo(ServeAoVeneno("cianureto", "Cianureto"), ServeAoVeneno("cianureto", "outro nome")))
        assertTrue(ServeAoVenenoRules.mesmo(ServeAoVeneno(null, "Peçonha de Aranha"), ServeAoVeneno(null, " peconha de aranha ")))
        assertFalse(ServeAoVenenoRules.mesmo(ServeAoVeneno("cianureto", "Cianureto"), ServeAoVeneno("veneno_de_cobra", "Veneno de Cobra")))
    }

    @Test
    fun `o kit entra com a escolha, vai para a Mesa e volta do arquivo, e sem escolha vale null`() {
        val kit = ItensCatalogLoader.equipamentoDoItem(item("kit_antitoxinas"), 1, ServeAoVeneno("veneno_de_cobra", "Veneno de Cobra"))
        assertEquals("veneno_de_cobra", kit.serveAoVeneno!!.id)
        assertTrue(kit.regra!!.get("antidoto").asBoolean)
        val p = Personagem(nome = "Ana", equipamentos = listOf(kit))
        val eq = PersonagemInterop.fichaInteiraParaAMesa(p).getAsJsonArray("equipamentos")[0].asJsonObject
        assertEquals("veneno_de_cobra", eq.getAsJsonObject("serveAoVeneno").get("id").asString)
        assertEquals("Veneno de Cobra", eq.getAsJsonObject("serveAoVeneno").get("nome").asString)
        assertEquals(p.equipamentos, Personagem.fromJson(p.toJson()).equipamentos)
        // o nome em branco nao e escolha: o kit nao leva o campo
        assertNull(ItensCatalogLoader.equipamentoDoItem(item("kit_antitoxinas"), 1, ServeAoVeneno(null, "  ")).serveAoVeneno)
        // a ficha antiga (kit sem a escolha) abre sem o campo
        val antiga = Personagem.fromJson("""{"nome":"V","equipamentos":[{"nome":"Kit Antitoxinas","tipo":"GERAL","regra":{"tipo":"cura","antidoto":true,"doses":10}}]}""").equipamentos[0]
        assertNull(antiga.serveAoVeneno)
    }

    @Test
    fun `o cartao do kit diz para que veneno serve, ou que falta escolher`() {
        val com = ItensCatalogLoader.equipamentoDoItem(item("kit_antitoxinas"), 1, ServeAoVeneno("cianureto", "Cianureto"))
        assertEquals("Para: Cianureto", ServeAoVenenoRules.linha(com))
        val sem = ItensCatalogLoader.equipamentoDoItem(item("kit_antitoxinas"))
        assertTrue(ServeAoVenenoRules.linha(sem)!!.startsWith("Sem veneno escolhido"))
        assertNull("so o antidoto tem a linha", ServeAoVenenoRules.linha(ItensCatalogLoader.equipamentoDoItem(item("tocha"))))
    }

    @Test
    fun `grava o que o app manda, para o teste da Mesa usar`() {
        val kits = listOf(
            ItensCatalogLoader.equipamentoDoItem(item("kit_antitoxinas"), 1, ServeAoVeneno("veneno_de_cobra", "Veneno de Cobra")),
            ItensCatalogLoader.equipamentoDoItem(item("kit_antitoxinas"), 1, ServeAoVeneno("cianureto", "Cianureto")),
            ItensCatalogLoader.equipamentoDoItem(item("kit_antitoxinas"), 1, ServeAoVeneno("gas_nervoso", "Gás Nervoso")),
            ItensCatalogLoader.equipamentoDoItem(item("kit_antitoxinas"), 1, ServeAoVeneno(null, "Peçonha de Aranha")),
            ItensCatalogLoader.equipamentoDoItem(item("kit_antitoxinas"), 1)   // ficha antiga: sem a escolha
        )
        val saida = PersonagemInterop.fichaInteiraParaAMesa(Personagem(nome = "Ana", equipamentos = kits)).getAsJsonArray("equipamentos")
        assertEquals(5, saida.size())
        assertFalse(saida[4].asJsonObject.has("serveAoVeneno"))
        File("build/kits-antitoxinas-como-o-app-manda.json").also { it.parentFile?.mkdirs() }
            .writeText(com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(saida))
    }
}
