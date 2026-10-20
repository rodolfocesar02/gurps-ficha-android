package com.gurps.ficha.domain.rules

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.domain.magic.MagicClassParser
import com.gurps.ficha.domain.magic.TipoClasseMagia
import com.gurps.ficha.model.MagiaDefinicao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **MG-4 — o que muda a conta do custo além do NH** (Magia p.8, p.11 e p.13): a Bloqueio nunca é reduzida pelo NH, a Comum
 * custa 1 + MT vezes mais num alvo de MT positivo (ANTES de tirar o NH), e as mágicas com tabela ou conta própria ficam de fora.
 *
 * Três camadas, e as duas primeiras são arquivos que a Mesa lê também (`mesa-virtual/server/test/amostras/`):
 *
 * 1. `custo_bloqueio_e_tamanho_casos.json` — casos feitos À MÃO, do texto do livro. A Mesa roda os mesmos em JavaScript.
 * 2. `classes_do_custo_do_catalogo.json` — as 58 escritas de classe do catálogo e o que cada uma quer dizer. Saiu da leitura da
 *    Mesa; o [MagicClassParser] do app tem de dar as mesmas seis respostas. Sem isto, os dois lados podem ler uma classe de
 *    jeitos diferentes e dar contas diferentes, cada um com o seu teste verde.
 * 3. O catálogo de verdade: quantas mágicas ganham o fator do tamanho (o mesmo número que a Mesa trava).
 */
class MagiaCustoBloqueioETamanhoTest {

    private fun recurso(nome: String) = JsonParser.parseString(
        javaClass.classLoader!!.getResourceAsStream(nome)!!.bufferedReader(Charsets.UTF_8).readText()
    ).asJsonObject

    @Test
    fun `os casos do livro - fator, custo antes do NH, o que o NH tirou e o que sai dos PF`() {
        val casos = recurso("custo_bloqueio_e_tamanho_casos.json").getAsJsonArray("casos")
        assertTrue("poucos casos: ${casos.size()}", casos.size() >= 30)
        casos.forEach { c ->
            val k = c.asJsonObject
            val onde = k.get("descricao").asString
            val r = MagiaEnergiaRules.contaDoCusto(
                base = k.get("base").asInt, nh = k.get("nh").asInt,
                classe = k.get("classe").asString, definicaoId = k.get("definicaoId").asString,
                temCondicao = k.get("temCondicao").asBoolean, comoBloqueio = k.get("comoBloqueio").asBoolean,
                mt = k.get("mt").asInt
            )
            assertEquals("fator — $onde", k.get("fator").asInt, r.fator)
            assertEquals("bruto — $onde", k.get("bruto").asInt, r.bruto)
            assertEquals("redução — $onde", k.get("reducao").asInt, r.reducao)
            assertEquals("ajustado — $onde", k.get("ajustado").asInt, r.ajustado)
        }
    }

    @Test
    fun `a leitura da classe e a mesma da Mesa em todas as escritas do catalogo`() {
        val classes = recurso("classes_do_custo_do_catalogo.json").getAsJsonArray("classes")
        assertEquals("o catálogo tem 58 escritas de classe", 58, classes.size())
        classes.forEach { c ->
            val k = c.asJsonObject
            val classe = k.get("classe").asString
            val tipos = MagicClassParser.parse(classe).classes
            fun confere(nome: String, tipo: TipoClasseMagia) =
                assertEquals("$classe: $nome", k.get(nome).asBoolean, tipo in tipos)
            confere("comum", TipoClasseMagia.COMUM)
            confere("projetil", TipoClasseMagia.PROJETIL)
            confere("area", TipoClasseMagia.AREA)
            confere("toque", TipoClasseMagia.TOQUE)
            confere("informacao", TipoClasseMagia.INFORMACAO)
            confere("bloqueio", TipoClasseMagia.BLOQUEIO)
        }
    }

    private val arquivo = listOf("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")
        .map { File(it) }.firstOrNull { it.exists() }

    private val catalogo: List<MagiaDefinicao> by lazy {
        val tipo = object : TypeToken<List<MagiaDefinicao>>() {}.type
        Gson().fromJson<List<MagiaDefinicao>>(arquivo!!.readText(Charsets.UTF_8), tipo)
    }

    @Test
    fun `o catalogo - toda classe escrita esta no arquivo da Mesa, e 408 magias ganham o fator do tamanho`() {
        val doArquivo = recurso("classes_do_custo_do_catalogo.json").getAsJsonArray("classes")
            .map { it.asJsonObject.get("classe").asString }.toSet()
        val doCatalogo = catalogo.map { it.classe.orEmpty() }.toSet()
        assertEquals("classes que o catálogo tem e o arquivo não (refazer o arquivo)", emptySet<String>(), doCatalogo - doArquivo)
        assertEquals("classes que o arquivo tem e o catálogo não", emptySet<String>(), doArquivo - doCatalogo)
        assertEquals(879, catalogo.size)

        val multiplicam = catalogo.filter {
            MagiaEnergiaRules.tamanhoMultiplica(it.classe, it.id, temCondicao = it.custoPorCondicao != null)
        }
        // O mesmo número que a Mesa trava em test/magia-bloqueio-e-tamanho.test.js ("sentinela").
        assertEquals(408, multiplicam.size)
        multiplicam.forEach { assertFalse("${it.id} tem tabela própria", it.id in MagiaEnergiaRules.COM_TABELA_PROPRIA_DE_TAMANHO) }
        MagiaEnergiaRules.COM_TABELA_PROPRIA_DE_TAMANHO.forEach { id ->
            val m = catalogo.first { it.id == id }
            assertFalse("$id leva o fator do tamanho", MagiaEnergiaRules.tamanhoMultiplica(m.classe, m.id, m.custoPorCondicao != null))
        }
    }

    @Test
    fun `o catalogo - as 23 Bloqueios puras nunca tem o desconto e as 8 Comum ou Bloqueio so quando operadas como Bloqueio`() {
        val puras = catalogo.filter {
            val c = MagicClassParser.parse(it.classe).classes
            TipoClasseMagia.BLOQUEIO in c && c.none { x ->
                x in setOf(TipoClasseMagia.COMUM, TipoClasseMagia.PROJETIL, TipoClasseMagia.AREA, TipoClasseMagia.TOQUE, TipoClasseMagia.INFORMACAO)
            }
        }
        val hibridas = catalogo.filter {
            val c = MagicClassParser.parse(it.classe).classes
            TipoClasseMagia.BLOQUEIO in c && TipoClasseMagia.COMUM in c
        }
        assertEquals(23, puras.size)
        assertEquals(8, hibridas.size)
        puras.forEach { m ->
            for (nh in 8..35) {
                val r = MagiaEnergiaRules.contaDoCusto(3, nh, m.classe, m.id)
                assertEquals("${m.id} NH $nh", 0, r.reducao)
                assertEquals(3, r.ajustado)
            }
        }
        hibridas.forEach { m ->
            assertEquals("${m.id} como Bloqueio", 0, MagiaEnergiaRules.contaDoCusto(3, 20, m.classe, m.id, comoBloqueio = true).reducao)
            assertEquals("${m.id} como Comum", 2, MagiaEnergiaRules.contaDoCusto(3, 20, m.classe, m.id, comoBloqueio = false).reducao)
        }
    }

    @Test
    fun `a conta em portugues e a mesma da Mesa`() {
        fun t(base: Int, nh: Int, classe: String, mt: Int = 0, id: String = "sono"): String =
            MagiaEnergiaRules.textoDaConta(MagiaEnergiaRules.contaDoCusto(base, nh, classe, id, mt = mt), nh)
        assertEquals("custo 2", t(2, 12, "Comum"))
        assertEquals("custo 2, −1 pelo NH 15", t(2, 15, "Comum"))
        assertEquals("custo 2 ×3 (alvo MT +2 — Magia p.11) = 6, −1 pelo NH 16", t(2, 16, "Comum", mt = 2))
        assertEquals("custo 2 (Bloqueio: o NH não reduz o custo — Magia p.8)", t(2, 18, "Bloqueio"))
        assertEquals("custo 2", t(2, 12, "Bloqueio"))
    }

    @Test
    fun `a ordem do livro - o tamanho multiplica ANTES do NH tirar, e nunca o contrario`() {
        // custo 2, NH 16, alvo MT +2: (2 × 3) − 1 = 5. Ao contrário seria (2 − 1) × 3 = 3.
        assertEquals(5, MagiaEnergiaRules.contaDoCusto(2, 16, "Comum", "sono", mt = 2).ajustado)
        // Mais tamanho nunca custa menos, em nenhum NH.
        for (nh in listOf(10, 14, 15, 17, 19, 20, 24, 25, 30)) {
            var anterior = MagiaEnergiaRules.contaDoCusto(4, nh, "Comum", "sono", mt = 0).ajustado
            for (mt in 1..6) {
                val agora = MagiaEnergiaRules.contaDoCusto(4, nh, "Comum", "sono", mt = mt).ajustado
                assertTrue("NH $nh MT $mt custa menos que MT ${mt - 1}", agora >= anterior)
                assertEquals(maxOf(0, 4 * (1 + mt) - MagiaEnergiaRules.reducaoPorNh(nh)), agora)
                anterior = agora
            }
        }
        // MT negativo não reduz.
        assertEquals(4, MagiaEnergiaRules.contaDoCusto(4, 12, "Comum", "sono", mt = -3).ajustado)
    }
}
