package com.gurps.ficha.domain.magic

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.MagiaDefinicao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.io.File

/**
 * **P-9: a faixa Operar / Dura / Manter da aba Rolagem** — a conta do app contra a da Mesa.
 *
 * O catálogo do app traz a faixa pronta (`faixaDoLivro`, gravada pelo gerador da Mesa com a conta da Mesa); o app só refaz o "Manter" onde o jogador
 * escolhe o custo base e o NH reduz. `src/test/resources/faixa_do_livro_casos_gerados.json` tem o que a MESA mostra para as 879 magias e para cada
 * escolha (`base`, `reducao`, e a `conta` por condição quando há): este teste exige o mesmo texto do Kotlin, caso a caso.
 *
 * Mudou o catálogo (duração, tempo, energia, números conferidos)? Rode `node scripts/gerar-faixa-do-livro-no-catalogo-do-app.js` na Mesa.
 */
class FaixaDoLivroTest {

    private val arquivo = listOf("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")
        .map { File(it) }.firstOrNull { it.exists() }

    private val catalogo: Map<String, MagiaDefinicao> by lazy {
        val tipo = object : TypeToken<List<MagiaDefinicao>>() {}.type
        Gson().fromJson<List<MagiaDefinicao>>(arquivo!!.readText(Charsets.UTF_8), tipo).associateBy { it.id }
    }

    private fun faixa(id: String): FaixaDoLivro = catalogo.getValue(id).faixaDoLivro!!

    @Test
    fun `as 879 magias do catalogo trazem a faixa`() {
        Assume.assumeNotNull(arquivo)
        assertEquals(879, catalogo.size)
        catalogo.values.forEach { m ->
            val f = m.faixaDoLivro
            assertNotNull("${m.id}: sem faixaDoLivro", f)
            assertTrue("${m.id}: Operar vazio", f!!.operar.valor.isNotBlank() && f.operar.nota.isNotBlank())
            assertTrue("${m.id}: Dura vazio", f.dura.valor.isNotBlank() && f.dura.nota.isNotBlank())
            assertTrue("${m.id}: tipo do manter ${f.manter.tipo}", f.manter.tipo in setOf("numero", "metade", "igual", "nao", "naoDiz"))
        }
    }

    @Test
    fun `os casos que a Mesa gera - 879 magias e cada escolha de custo e de NH - saem iguais no app`() {
        Assume.assumeNotNull(arquivo)
        val texto = javaClass.classLoader!!.getResourceAsStream("faixa_do_livro_casos_gerados.json")!!
            .bufferedReader(Charsets.UTF_8).readText()
        val casos = JsonParser.parseString(texto).asJsonArray
        assertTrue("poucos casos (${casos.size()})", casos.size() >= 2900)
        var completos = 0
        var soManter = 0
        casos.forEach { c ->
            val k = c.asJsonObject
            val id = k.get("id").asString
            val base = k.get("base").takeIf { !it.isJsonNull }?.asInt
            val reducao = k.get("reducao").asInt
            val conta = k.getAsJsonObject("conta")?.let { o ->
                ManterDaConta(o.get("manterTipo").asString, o.get("manter").takeIf { !it.isJsonNull }?.asInt)
            }
            val dele = faixa(id).colunas(base, reducao, conta)
            val onde = "$id (base=$base, NH −$reducao, conta=${conta?.tipo}/${conta?.manter})"
            if (k.has("c")) {
                completos++
                val esperado = k.getAsJsonArray("c")
                assertEquals("$onde: colunas", 3, esperado.size())
                esperado.forEachIndexed { i, par ->
                    val p = par.asJsonArray
                    assertEquals("$onde: ${dele[i].rotulo} valor", p[0].asString, dele[i].valor)
                    assertEquals("$onde: ${dele[i].rotulo} nota", p[1].asString, dele[i].nota)
                }
            } else {
                soManter++
                val p = k.getAsJsonArray("m")
                assertEquals("$onde: Manter valor", p[0].asString, dele[2].valor)
                assertEquals("$onde: Manter nota", p[1].asString, dele[2].nota)
            }
        }
        assertEquals("uma linha completa por magia", 879, completos)
        assertTrue("casos do Manter com escolha: $soManter", soManter >= 1000)
    }

    @Test
    fun `casos que o Rodolfo conhece - o Sopro Congelante nao se mantem, o Voo mantem 3 por minuto`() {
        Assume.assumeNotNull(arquivo)
        val sopro = faixa("sopro_congelante").colunas()
        assertEquals(FaixaColuna("Operar", "2 s", "tempo de concentração"), sopro[0])
        assertEquals(FaixaColuna("Manter", "—", "não se mantém"), sopro[2])

        // o Voo: 3 de energia a cada 1 minuto; o NH reduz o que se paga para manter, mas nunca abaixo de zero
        val voo = faixa("voo")
        assertEquals(FaixaColuna("Manter", "3", "a cada 1 minuto"), voo.colunas()[2])
        assertEquals(FaixaColuna("Manter", "3 → 1", "a cada 1 minuto, com o seu NH"), voo.colunas(base = 7, reducao = 2)[2])
        assertEquals(FaixaColuna("Manter", "3 → 0", "a cada 1 minuto, com o seu NH"), voo.colunas(base = 7, reducao = 9)[2])

        // instantânea: o livro não pede manter, e a nota diz "não se aplica" (e não "o livro não diz")
        val percepcao = faixa("percepcao_de_vida").colunas()
        assertEquals("Instantânea", percepcao[1].valor)
        assertEquals(FaixaColuna("Manter", "—", "não se aplica"), percepcao[2])
    }

    @Test
    fun `metade e igual so viram numero quando o jogador escolhe o custo - metade arredonda para cima`() {
        Assume.assumeNotNull(arquivo)
        val metade = catalogo.values.first { it.faixaDoLivro?.manter?.tipo == "metade" }.faixaDoLivro!!
        assertEquals("Metade", metade.colunas()[2].valor)
        assertEquals("do custo de operar", metade.colunas()[2].nota)
        assertEquals("4", metade.colunas(base = 7)[2].valor) // ⌈7/2⌉
        assertEquals("1", metade.colunas(base = 1)[2].valor) // ⌈1/2⌉
        assertEquals("0", metade.colunas(base = 0)[2].valor)
        val igual = catalogo.values.first { it.faixaDoLivro?.manter?.tipo == "igual" }.faixaDoLivro!!
        assertEquals("Igual", igual.colunas()[2].valor)
        assertEquals("ao custo de operar", igual.colunas()[2].nota)
        assertEquals("7 → 4", igual.colunas(base = 7, reducao = 3)[2].valor)
    }

    @Test
    fun `onde o custo depende da condicao a conta manda no Manter - nenhum nao se mantem, senao o numero dela`() {
        Assume.assumeNotNull(arquivo)
        val f = catalogo.values.first { it.custoPorCondicao != null }.faixaDoLivro!!
        assertEquals(FaixaColuna("Manter", "—", "não se mantém"), f.colunas(7, 0, ManterDaConta("nenhum", 0))[2])
        val cinco = f.colunas(7, 0, ManterDaConta("conta", 5))[2]
        assertEquals("5", cinco.valor)
        assertEquals("5 → 2", f.colunas(7, 3, ManterDaConta("conta", 5))[2].valor)
        assertTrue(cinco.nota.isNotBlank())
        // sem tipo (a conta não fecha) a conta não manda: vale o do livro
        assertEquals(f.colunas()[2], f.colunas(null, 0, ManterDaConta(null, null))[2])
    }

    @Test
    fun `a frase para quem ouve diz as tres colunas e troca a seta por passa a`() {
        Assume.assumeNotNull(arquivo)
        val fala = FaixaDoLivro.fala(faixa("voo").colunas(base = 7, reducao = 2))
        assertEquals(
            "Operar 2 s, tempo de concentração. Dura 1 minuto, depois: manter. Manter 3 passa a 1, a cada 1 minuto, com o seu NH.",
            fala
        )
        assertFalse(fala.contains("→"))
    }

    @Test
    fun `as 8 correcoes da Mesa agora estao no catalogo do app`() {
        Assume.assumeNotNull(arquivo)
        assertEquals(0, catalogo.getValue("sopro_congelante").custoManter) // A48: não pode ser mantido
        assertEquals("1d+1", catalogo.getValue("sopro_congelante").mecanica?.danoPorEnergia) // A12 (Magia p.192)
        assertFalse(catalogo.getValue("percepcao_de_vida").descricao.orEmpty().contains("Duração: 1 minuto")) // A6: o livro diz instantânea
        listOf("ar", "agua", "fogo", "terra").forEach { e ->
            assertEquals("segundos = pontos do elemental (p.28)", catalogo.getValue("criar_elemental_$e").tempoOperacaoTela) // A13
        }
    }

    @Test
    fun `o 28 da pagina do Criar Elemental nao vira 28 segundos`() {
        // a nota "(p.28)" é do livro, e não um tempo: sem a guarda de `MagicTime.parseTempoSeg` o Criar Elemental levaria 28 s
        assertEquals(1, MagicTime.parseTempoSeg("segundos = pontos do elemental (p.28)"))
        assertEquals(3, MagicTime.parseTempoSeg("3 seg."))
        assertEquals(60, MagicTime.parseTempoSeg("1 min."))
        assertEquals(1, MagicTime.parseTempoSeg("Especial"))
    }
}
