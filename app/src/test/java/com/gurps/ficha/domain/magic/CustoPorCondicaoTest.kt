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

    /** Os casos do livro de um arquivo de `src/test/resources`: o lote 1 (à mão) e o lote 2 (do gerador). */
    private fun rodarOsCasos(recurso: String, minimo: Int) {
        val texto = javaClass.classLoader!!.getResourceAsStream(recurso)!!
            .bufferedReader(Charsets.UTF_8).readText()
        val casos = JsonParser.parseString(texto).asJsonObject.getAsJsonArray("casos")
        assertTrue("$recurso: poucos casos (${casos.size()})", casos.size() >= minimo)
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
            // P-5c: o ajuste no NH que a condição traz (a distância do Teleporte...)
            if (k.has("modificador")) assertEquals("$nome modificador", k.get("modificador").asInt, r.modificador)
        }
    }

    @Test
    fun `os 73 casos do livro - operar e manter saem como o livro manda`() {
        Assume.assumeNotNull(arquivo)
        rodarOsCasos("custo_por_condicao_casos.json", 70)
    }

    @Test
    fun `os casos do lote 2 - as fixas com condicao, as por unidade e as de area`() {
        Assume.assumeNotNull(arquivo)
        rodarOsCasos("custo_por_condicao_casos_gerados.json", 500)
    }

    @Test
    fun `toda magia com conta no catalogo fecha com os valores padrao e o raio padrao e o minimo`() {
        Assume.assumeNotNull(arquivo)
        val comConta = catalogo.values.filter { it.custoPorCondicao != null }
        assertTrue("magias com conta: ${comConta.size}", comConta.size >= 340)
        comConta.forEach { m ->
            val s = m.custoPorCondicao!!
            val r = CustoPorCondicaoRules.calcular(s, CustoPorCondicaoRules.condicoesPadrao(s))
            assertTrue("${m.id}: $r", r is CustoPorCondicaoRules.Resultado.Ok)
            s.entradas.firstOrNull { it.id == "raio" }?.let { raio ->
                assertEquals("${m.id}: o padrão do raio é o mínimo", raio.min, raio.padrao?.asDouble)
            }
        }
    }

    @Test
    fun `a regra da area - custo basico vezes o raio, no minimo 1 PF`() {
        Assume.assumeNotNull(arquivo)
        // Escudo Antiteleporte: 1/3 por metro, raio mínimo 3 → 1; raio 10 → ⌈3,33⌉ = 4
        val tres = calcular("escudo_antiteleporte", "raio" to 3.0) as CustoPorCondicaoRules.Resultado.Ok
        val dez = calcular("escudo_antiteleporte", "raio" to 10.0) as CustoPorCondicaoRules.Resultado.Ok
        assertEquals(1, tres.operar)
        assertEquals(4, dez.operar)
        assertEquals("condicao_invalida", (calcular("escudo_antiteleporte", "raio" to 2.0) as CustoPorCondicaoRules.Resultado.Erro).erro)
        // Terremoto: a intensidade multiplica (Grave ×4)
        val grave = calcular("terremoto", "raio" to 3.0, "intensidade" to "grave") as CustoPorCondicaoRules.Resultado.Ok
        assertEquals(24, grave.operar)
        // P-5: as de área cujo "Custo básico" é uma conta (2 para o dobro, 3 para o triplo...) também perguntam o raio
        listOf("acelerar_tempo", "retardar_tempo", "retardar_fogo").forEach { id ->
            assertEquals(id, listOf("raio", "ritmo"), spec(id).entradas.map { it.id })
        }
        val tempo = calcular("retardar_tempo", "raio" to 3.0, "ritmo" to 4.0) as CustoPorCondicaoRules.Resultado.Ok
        assertEquals(12, tempo.operar)
    }

    @Test
    fun `P-5 - os exemplos que o proprio livro da`() {
        Assume.assumeNotNull(arquivo)
        fun op(id: String, vararg c: Pair<String, Any?>) = (calcular(id, *c) as CustoPorCondicaoRules.Resultado.Ok).operar
        // Criptografar: seis suplementos de 175.000 palavras = 1.050.000 palavras custam 12 (só contam as 100.000 palavras COMPLETAS)
        assertEquals(12, op("criptografar", "palavras" to 1050000.0))
        assertEquals(3, op("criptografar", "palavras" to 199999.0))
        assertEquals(4, op("criptografar", "palavras" to 200000.0))
        // Acionar/NT: (1,8 t × Deslocamento 75) ÷ NH 15 = 9 de energia
        assertEquals(9, op("acionarnt", "pcc" to 1.8, "deslocamento" to 75.0, "nh" to 15.0))
        // Desviar Teleporte: 60 km (9) contra 10 m (3) = 6, nos dois sentidos; o mínimo é 3
        assertEquals(6, op("desviar_teleporte", "pretendido" to 9.0, "novo" to 3.0))
        assertEquals(6, op("desviar_teleporte", "pretendido" to 3.0, "novo" to 9.0))
        assertEquals(3, op("desviar_teleporte", "pretendido" to 5.0, "novo" to 6.0))
        // Mágica Penetrante: o PDF de 2025 (5 → 6, 10 → 8, ignora RD → 10), não o texto antigo (4 → 6, 5 → 8, 10 → 10)
        assertEquals(6, op("magica_penetrante", "divisor" to "cinco"))
        assertEquals(8, op("magica_penetrante", "divisor" to "dez"))
        // quem não gasta nada (Roubar Energia): custo 0, sem perguntar (a aba Rolagem rola direto, sem diálogo)
        assertEquals(0, op("roubar_energia"))
        assertTrue(spec("roubar_energia").semPergunta)
        assertTrue(!spec("luz_continua").semPergunta)
    }

    @Test
    fun `P-5c - a familia do Teleporte da o custo E a penalidade de NH de cada salto (Magia pp 81, 82, 128 e 147)`() {
        Assume.assumeNotNull(arquivo)
        fun cm(id: String, vararg c: Pair<String, Any?>): Pair<Int, Int> = (calcular(id, *c) as CustoPorCondicaoRules.Resultado.Ok).let { it.operar to it.modificador }
        // a tabela do Teleporte (p.147)
        listOf("ate10" to (3 to 0), "ate20" to (4 to -1), "ate100" to (5 to -2), "ate500" to (6 to -3), "ate3km" to (7 to -4),
            "ate15km" to (8 to -5), "ate150km" to (9 to -6), "ate1500km" to (10 to -7)).forEach { (d, esperado) ->
            assertEquals(d, esperado, cm("teleporte", "distancia" to d))
        }
        // o lugar que não se olha: −2 pela mente ou TV, −2 sem ver há um mês, −1 a −3 visto só brevemente; somam, e o custo é o da distância
        assertEquals(5 to -9, cm("teleporte", "distancia" to "ate100", "mente" to true, "mes" to true, "breve" to "b3"))
        // Teleportar Outro: o custo × o peso (×1/2, ×1, ×2, ×3...), para cima
        assertEquals(2 to 0, cm("teleportar_outro", "peso" to "p5"))
        assertEquals(15 to -2, cm("teleportar_outro", "distancia" to "ate100", "peso" to "p100"))
        // Viagem no Tempo (p.81): a tabela e, no tempo e no espaço, a soma
        assertEquals(8 to -2, cm("viagem_no_tempo", "tempo" to "ate1h"))
        assertEquals(14 to -5, cm("viagem_no_tempo", "tempo" to "ate1h", "distancia" to "ate500"))
        // Jornada Rápida: a viagem e um quarto dela por minuto (p.82)
        val jornada = calcular("jornada_rapida", "distancia" to "ate100") as CustoPorCondicaoRules.Resultado.Ok
        assertEquals(listOf(5, 2, -2), listOf(jornada.operar, jornada.manter, jornada.modificador))
        // Desviar Teleporte (o exemplo do livro, p.84): 100 m contra 1 km custa o mínimo de 3, com −4 pelo destino imposto
        assertEquals(3 to -4, cm("desviar_teleporte", "pretendido" to 5.0, "novo" to 7.0))
        // Translocação no Tempo (p.81): −1 no NH por segundo além do primeiro
        assertEquals(5 to -4, cm("translocacao_no_tempo", "segundos" to 5.0))
        // quem não tem ajuste de NH fica com 0
        assertEquals(0, (calcular("luz_continua") as CustoPorCondicaoRules.Resultado.Ok).modificador)
    }

    @Test
    fun `P-5 - o operador baixo conta so as unidades completas, e o tamanho true mantem o fator do alvo`() {
        Assume.assumeNotNull(arquivo)
        val conta = com.google.gson.JsonParser.parseString("[\"baixo\", \"\$x\"]")
        assertEquals(9.0, CustoPorCondicaoRules.avaliar(conta, mapOf("x" to 9.5)), 0.0)
        assertEquals(10.0, CustoPorCondicaoRules.avaliar(conta, mapOf("x" to 10.0)), 0.0)
        assertEquals(0.0, CustoPorCondicaoRules.avaliar(conta, mapOf("x" to 0.99999)), 0.0)
        // Armadura: a conta é a RD dada, não o tamanho; num alvo de MT +2 a Comum ainda custa ×3. Luz Contínua tem conta própria: não multiplica.
        val armadura = catalogo.getValue("armadura")
        val luz = catalogo.getValue("luz_continua")
        assertEquals(true, armadura.custoPorCondicao?.tamanho)
        assertEquals(false, armadura.custoPorCondicao?.contaPropria)
        assertEquals(6, com.gurps.ficha.domain.rules.MagiaEnergiaRules.contaDoCusto(2, 12, armadura.classe, armadura.id, temCondicao = armadura.custoPorCondicao?.contaPropria == true, mt = 2).bruto)
        assertEquals(2, com.gurps.ficha.domain.rules.MagiaEnergiaRules.contaDoCusto(2, 12, luz.classe, luz.id, temCondicao = luz.custoPorCondicao?.contaPropria == true, mt = 2).bruto)
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
