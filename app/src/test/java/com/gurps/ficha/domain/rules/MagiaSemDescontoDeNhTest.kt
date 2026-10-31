package com.gurps.ficha.domain.rules

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.MagiaDefinicao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **P-5b — o livro diz que o NH elevado NÃO reduz o custo de 16 magias** (Força, Vigor, Fragilidade e Aumentar (Atributo), p.37;
 * Conceder Energia e Vitalidade e Compartilhar Energia, p.89; Guarda-Mágica, p.127; Telemágica, p.128; Acionar/NT, Conceder, Conduzir e
 * Extrair Corrente Elétrica, pp.180-181). A regra geral (MB p.238) tirava 1 a NH 15, 2 a NH 20, 3 a NH 25 também delas.
 *
 * Os casos são os MESMOS que a Mesa roda (`custo_sem_desconto_de_nh_casos.json`; `test/sem-desconto-de-nh.test.js`), à mão, com o
 * contraste de quem o livro NÃO isenta (Graça, Sabedoria, Sono, Contramágica).
 */
class MagiaSemDescontoDeNhTest {

    private val arquivo = listOf("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")
        .map { File(it) }.firstOrNull { it.exists() }

    private val catalogo: Map<String, MagiaDefinicao> by lazy {
        val tipo = object : TypeToken<List<MagiaDefinicao>>() {}.type
        Gson().fromJson<List<MagiaDefinicao>>(arquivo!!.readText(Charsets.UTF_8), tipo).associateBy { it.id }
    }

    private val casos = JsonParser.parseString(
        javaClass.classLoader!!.getResourceAsStream("custo_sem_desconto_de_nh_casos.json")!!.bufferedReader(Charsets.UTF_8).readText()
    ).asJsonObject

    private fun conta(id: String, base: Int, nh: Int, mt: Int = 0): MagiaEnergiaRules.ContaDoCusto {
        val m = catalogo.getValue(id)
        return MagiaEnergiaRules.contaDoCusto(
            base = base, nh = nh, classe = m.classe, definicaoId = m.id,
            temCondicao = m.custoPorCondicao?.contaPropria == true, mt = mt, semNhPagina = m.semDescontoDeNh?.pagina
        )
    }

    @Test
    fun `os casos do livro - o NH nao tira nada de quem o livro isenta e tira de quem nao isenta`() {
        val lista = casos.getAsJsonArray("casos")
        assertTrue("poucos casos: ${lista.size()}", lista.size() >= 16)
        lista.forEach { c ->
            val k = c.asJsonObject
            val id = k.get("id").asString
            val nh = k.get("nh").asInt
            val r = conta(id, k.get("base").asInt, nh)
            assertEquals("$id NH $nh: ajustado", k.get("ajustado").asInt, r.ajustado)
            assertEquals("$id NH $nh: semNh", k.get("semNh").asBoolean, r.semDescontoDeNh)
            assertEquals("$id: o catálogo diz o mesmo dos casos", k.get("semNh").asBoolean, catalogo.getValue(id).semDescontoDeNh != null)
        }
    }

    @Test
    fun `nenhum NH de 8 a 35 reduz o custo de quem o livro isenta, e as outras seguem a tabela do MB p238`() {
        val isentas = casos.getAsJsonArray("isentas").map { it.asString }
        for (nh in 8..35) {
            isentas.forEach { id ->
                val r = conta(id, 5, nh)
                assertEquals("$id NH $nh", 0, r.reducao)
                assertEquals("$id NH $nh", 5, r.ajustado)
            }
            listOf("graca", "sabedoria", "sono").forEach { id ->
                assertEquals("$id NH $nh", MagiaEnergiaRules.reducaoPorNh(nh), conta(id, 5, nh).reducao)
            }
        }
    }

    @Test
    fun `o catalogo - as 16 isentas sao as dos casos, cada uma com a pagina do livro`() {
        val isentasDoCatalogo = catalogo.values.filter { it.semDescontoDeNh != null }.map { it.id }.sorted()
        assertEquals(casos.getAsJsonArray("isentas").map { it.asString }.sorted(), isentasDoCatalogo)
        assertEquals(16, isentasDoCatalogo.size)
        isentasDoCatalogo.forEach { id -> assertTrue("$id: sem a página", catalogo.getValue(id).semDescontoDeNh!!.pagina > 0) }
        assertEquals(37, catalogo.getValue("forca").semDescontoDeNh!!.pagina)
        assertEquals(181, catalogo.getValue("extrair_corrente_eletricant").semDescontoDeNh!!.pagina)
        assertNull(catalogo.getValue("graca").semDescontoDeNh)
    }

    @Test
    fun `o tamanho do alvo multiplica antes do NH e a isencao segue valendo, e a conta em portugues diz o porque`() {
        val c = conta("conceder_energia", 5, 20, mt = 2)
        assertEquals(15, c.bruto)
        assertEquals(0, c.reducao)
        assertEquals(15, c.ajustado)
        assertEquals("custo 5 ×3 (alvo MT +2 — Magia p.11) = 15 (o NH não reduz o custo desta magia — Magia p.89)", MagiaEnergiaRules.textoDaConta(c, 20))
        assertEquals("custo 6 (o NH não reduz o custo desta magia — Magia p.37)", MagiaEnergiaRules.textoDaConta(conta("forca", 6, 20), 20))
        assertEquals("custo 6", MagiaEnergiaRules.textoDaConta(conta("forca", 6, 12), 12))
        assertEquals("custo 6, −2 pelo NH 20", MagiaEnergiaRules.textoDaConta(conta("graca", 6, 20), 20))
        // a Bloqueio segue com a frase dela
        val bloqueio = MagiaEnergiaRules.contaDoCusto(3, 20, "Bloqueio", "desviar_projetil")
        assertEquals("custo 3 (Bloqueio: o NH não reduz o custo — Magia p.8)", MagiaEnergiaRules.textoDaConta(bloqueio, 20))
        assertNotNull(catalogo["aumentar_forca"]?.semDescontoDeNh)
    }
}
