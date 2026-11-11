package com.gurps.ficha.model

import com.gurps.ficha.domain.loaders.ItensCatalogLoader
import com.gurps.ficha.domain.rules.MunicaoDaArmaRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **A munição e os acessórios dentro da arma** — IC-3B do `mesa-virtual/planos/itens/PLANO.md` (pedido do Rodolfo, 11/nov).
 *
 * O "Tiros" é o carregador; `municoes` é o que o personagem carrega para aquela arma. A ficha da versão 216 (munição como
 * item à parte, com "Serve para") migra sozinha. A costura: grava `build/arma-com-municao-como-o-app-manda.json`, que a
 * Mesa lê em `server/test/amostras/`.
 */
class MunicaoDaArmaTest {

    private val rifle = Equipamento(
        nome = "Rifle de Assalto, 5,56 mm", tipo = TipoEquipamento.ARMA, armaTipoCombate = "armas_de_fogo",
        armaTirosRaw = "30+1(3)", peso = 4.5f, armaMunicaoKg = 0.5f
    )
    private val arco = Equipamento(nome = "Arco Longo", tipo = TipoEquipamento.ARMA, armaTipoCombate = "distancia", armaTirosRaw = "1(2)", peso = 1.5f)
    private val espada = Equipamento(nome = "Espada Larga", tipo = TipoEquipamento.ARMA, armaTipoCombate = "corpo_a_corpo", peso = 1.5f)

    private val catalogo: List<ItemDoCatalogo> by lazy {
        val direto = File("src/main/assets/${ItensCatalogLoader.ARQUIVO}")
        ItensCatalogLoader.ler((if (direto.exists()) direto else File("app/src/main/assets/${ItensCatalogLoader.ARQUIVO}")).readText(Charsets.UTF_8))
    }

    @Test
    fun `so a arma que atira leva municao, e o carregador sai do campo Tiros`() {
        assertTrue(MunicaoDaArmaRules.usaMunicao(rifle))
        assertTrue(MunicaoDaArmaRules.usaMunicao(arco))
        assertTrue("arco digitado a mao, sem Tiros", MunicaoDaArmaRules.usaMunicao(Equipamento(nome = "Funda", tipo = TipoEquipamento.ARMA, armaTipoCombate = "distancia")))
        assertFalse(MunicaoDaArmaRules.usaMunicao(espada))
        assertFalse("a faca de arremesso e a propria municao", MunicaoDaArmaRules.usaMunicao(Equipamento(nome = "Faca", tipo = TipoEquipamento.ARMA, armaTipoCombate = "arremesso", armaTirosRaw = "A(1)")))
        assertEquals(30, MunicaoDaArmaRules.capacidade("30+1(3)"))
        assertEquals(1, MunicaoDaArmaRules.capacidade("1(2)"))
        assertNull(MunicaoDaArmaRules.capacidade(null))
        assertNull(MunicaoDaArmaRules.capacidade("A(1)"))
    }

    @Test
    fun `a municao pesa na carga pelo peso da carga cheia do livro (MB p279)`() {
        // 0,5 kg a carga de 30 tiros: 90 tiros = 3 cargas = 1,5 kg, e $40 por quilo = $60
        val comMunicao = rifle.copy(municoes = listOf(MunicaoDaArma(quantidade = 90)))
        assertEquals(1.5f, MunicaoDaArmaRules.pesoDaMunicao(comMunicao), 0.001f)
        assertEquals(60f, MunicaoDaArmaRules.custoExtra(comMunicao), 0.01f)
        val p = Personagem(nome = "Jack", equipamentos = listOf(comMunicao, espada))
        assertEquals(4.5f + 1.5f + 1.5f, p.pesoTotalEquipamentos, 0.001f)
        // o livro nao da o peso da flecha deste arco: nao soma, e a tela diz
        assertEquals(0f, MunicaoDaArmaRules.pesoDaMunicao(arco.copy(municoes = listOf(MunicaoDaArma(quantidade = 20)))), 0f)
        assertNull(MunicaoDaArmaRules.pesoPorTiro(arco))
        // o acessorio pesa junto
        val mira = MunicaoDaArmaRules.acessorioDoItem(catalogo.first { it.id == "mira_4x" })
        assertEquals(0.75f, MunicaoDaArmaRules.pesoExtra(rifle.copy(acessorios = listOf(mira))), 0.001f)
    }

    @Test
    fun `a ficha da versao 216 migra sozinha, a municao entra na arma que servia`() {
        val flechas = Equipamento(nome = "Flechas", tipo = TipoEquipamento.MUNICAO, quantidade = 20, municaoDe = "arco longo")
        val orfa = Equipamento(nome = "Balas", tipo = TipoEquipamento.MUNICAO, quantidade = 50, municaoDe = "Pistola que vendi")
        val antes = listOf(espada, arco, flechas, orfa)
        val depois = MunicaoDaArmaRules.migrar(antes)
        assertEquals(listOf("Espada Larga", "Arco Longo", "Balas"), depois.map { it.nome })
        assertEquals(listOf(MunicaoDaArma(nome = "Flechas", quantidade = 20)), depois[1].municoes)
        assertEquals("a que nao achou a arma fica onde estava", TipoEquipamento.MUNICAO, depois[2].tipo)
        // rodar de novo nao muda nada, e ficha sem municao antiga volta o MESMO objeto
        assertEquals(depois, MunicaoDaArmaRules.migrar(depois))
        val semNada = listOf(espada, arco)
        assertSame(semNada, MunicaoDaArmaRules.migrar(semNada))
    }

    @Test
    fun `o arquivo antigo abre ja migrado, e o novo vai e volta igual`() {
        val antigo = """{"nome":"V","equipamentos":[
            {"nome":"Arco Longo","tipo":"ARMA","armaTipoCombate":"distancia","armaTirosRaw":"1(2)"},
            {"nome":"Flechas","tipo":"MUNICAO","quantidade":12,"municaoDe":"Arco Longo"}]}"""
        val p = Personagem.fromJson(antigo)
        assertEquals(1, p.equipamentos.size)
        assertEquals(12, p.equipamentos[0].municoes!![0].quantidade)
        assertEquals(p.equipamentos, Personagem.fromJson(p.toJson()).equipamentos)
        assertNull("arma de ficha antiga sem o campo", Personagem.fromJson("""{"nome":"V","equipamentos":[{"nome":"Espada","tipo":"ARMA"}]}""").equipamentos[0].municoes)
    }

    @Test
    fun `o cartao da arma diz o carregador, a municao e os acessorios`() {
        assertEquals(listOf("Tiros 30+1(3) · Munição: 90 · 1,50 kg"), MunicaoDaArmaRules.linhas(rifle.copy(municoes = listOf(MunicaoDaArma(quantidade = 90)))))
        val perfurante = MunicaoDaArmaRules.municaoDoItem(catalogo.first { it.id == "municao_prpa" }, 30)
        val dois = MunicaoDaArmaRules.linhas(rifle.copy(municoes = listOf(MunicaoDaArma(quantidade = 60), perfurante)))
        assertTrue(dois.toString(), dois[0].startsWith("Tiros 30+1(3) · Munição: Munição comum 60 · Munição perfurante"))
        val laser = MunicaoDaArmaRules.acessorioDoItem(catalogo.first { it.id == "mira_laser" })
        assertEquals("Acessórios: Mira Laser", MunicaoDaArmaRules.linhas(rifle.copy(acessorios = listOf(laser))).last())
        assertTrue(MunicaoDaArmaRules.linhas(rifle)[0].contains("não informada"))
        assertTrue(MunicaoDaArmaRules.linhas(espada).isEmpty())
    }

    @Test
    fun `a arma vai para a Mesa com a municao e o acessorio dentro, e grava a amostra da costura`() {
        val laser = MunicaoDaArmaRules.acessorioDoItem(catalogo.first { it.id == "mira_laser" })
        val silenciador = MunicaoDaArmaRules.acessorioDoItem(catalogo.first { it.id == "silenciador_de_pistola_ou_mm" })
        val perfurante = MunicaoDaArmaRules.municaoDoItem(catalogo.first { it.id == "municao_prpa" }, 10)
        val p = Personagem(nome = "Jack", equipamentos = listOf(
            arco.copy(municoes = listOf(MunicaoDaArma(nome = "Flechas", quantidade = 3))),
            rifle.copy(municoes = listOf(MunicaoDaArma(quantidade = 2), perfurante), acessorios = listOf(laser)),
            Equipamento(nome = "Pistola", tipo = TipoEquipamento.ARMA, armaTipoCombate = "armas_de_fogo", armaTirosRaw = "7+1(3)", acessorios = listOf(silenciador)),
            espada
        ))
        val saida = PersonagemInterop.fichaInteiraParaAMesa(p).getAsJsonArray("equipamentos")
        val doRifle = saida[1].asJsonObject
        assertEquals(2, doRifle.getAsJsonArray("municoes").size())
        assertEquals("municao", doRifle.getAsJsonArray("municoes")[1].asJsonObject.getAsJsonObject("regra").get("tipo").asString)
        assertEquals(1, doRifle.getAsJsonArray("acessorios")[0].asJsonObject.getAsJsonObject("regra").get("bonusNh").asInt)
        assertFalse("a espada nao ganha o campo", saida[3].asJsonObject.has("municoes"))
        File("build/arma-com-municao-como-o-app-manda.json").also { it.parentFile?.mkdirs() }
            .writeText(com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(saida))
    }
}
