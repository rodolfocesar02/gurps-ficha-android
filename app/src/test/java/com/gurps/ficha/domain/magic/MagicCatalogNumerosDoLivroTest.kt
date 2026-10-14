package com.gurps.ficha.domain.magic

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.io.File

/**
 * Lote MC-2 — GUARDA: os números CONFERIDOS do catálogo de magias são os do LIVRO.
 *
 * Os cinco números que o app e a Mesa preferem ao texto (`duracaoTipo`/`duracaoSeg`,
 * `tempoOperacaoSeg`, `custoOperar`, `custoManter`) tinham saído, em 17/jul, da `descricao`
 * transcrita por IA — e herdado os erros dela (Lealdade: 1 semana, 4/2; o livro: 1 hora, 2/2).
 * O script `scripts/magias/aplicar-numeros-conferidos.js` os refez do livro e marcou a origem em
 * `conferido`/`conferidoPor`.
 *
 * Este teste NÃO usa o script: relê a linha da tabela-resumo direto do `chunks.jsonl` e passa o texto
 * dela pelos PARSERS DO PRÓPRIO APP (`MagicTime`, `MagicEnergy`) — outra implementação, a que o jogo
 * realmente usa. Onde a origem do número é a tabela, os dois têm de concordar.
 *
 * Se os arquivos não existirem (build headless), os testes são PULADOS (Assume).
 */
class MagicCatalogNumerosDoLivroTest {

    private fun achar(vararg caminhos: String): File? = caminhos.map { File(it) }.firstOrNull { it.exists() }

    private val catalogoArq = achar("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")
    private val chunksArq = achar("src/main/assets/chunks.jsonl", "app/src/main/assets/chunks.jsonl")
    private val indiceArq = achar("../scripts/magias/magias-no-livro.json", "scripts/magias/magias-no-livro.json")

    private fun temOsArquivos() = catalogoArq != null && chunksArq != null && indiceArq != null

    private fun catalogo(): JSONArray = JSONArray(catalogoArq!!.readText(Charsets.UTF_8))

    private fun linhasDoLivro(): Map<Int, List<String>> {
        val m = HashMap<Int, List<String>>()
        chunksArq!!.forEachLine { linha ->
            if (linha.isBlank() || !linha.contains("\"pt_magia\"")) return@forEachLine
            val o = JSONObject(linha)
            if (o.optString("source_id") != "pt_magia") return@forEachLine
            m[o.getInt("page_number")] = o.getString("text").split(Regex("\r?\n"))
        }
        return m
    }

    private fun celulas(linha: String): List<String> =
        linha.trim().removePrefix("|").removeSuffix("|").split("|").map { it.trim() }

    private fun textoOuNulo(o: JSONObject, k: String): String? = if (o.has(k) && !o.isNull(k)) o.getString(k) else null
    private fun inteiroOuNulo(o: JSONObject, k: String): Int? = if (o.has(k) && !o.isNull(k)) o.getInt(k) else null

    /** O que o app lê da linha da tabela do livro bate com o número conferido, onde a origem é a tabela. */
    @Test
    fun `o numero conferido bate com a tabela do livro lida pelos parsers do app`() {
        Assume.assumeTrue(temOsArquivos())
        val livro = linhasDoLivro()
        val indice = JSONObject(indiceArq!!.readText(Charsets.UTF_8)).getJSONObject("magias")
        val cat = catalogo()
        val erros = mutableListOf<String>()
        var duracoes = 0; var tempos = 0; var custos = 0; var manutencoes = 0

        for (i in 0 until cat.length()) {
            val m = cat.getJSONObject(i)
            if (textoOuNulo(m, "conferido") != "livro") continue
            val id = m.getString("id")
            val nome = m.getString("nome")
            val por = m.optJSONObject("conferidoPor") ?: continue
            val t = indice.optJSONObject(id)?.optJSONObject("tabela") ?: continue
            val c = celulas(livro[t.getInt("paginaDoChunk")]?.getOrNull(t.getInt("linha")) ?: continue)
            if (c.size != 9) continue // as 2 linhas reparadas à mão (células coladas) o índice confere
            val reparada = t.optString("reparada", "")
            val trocadas = reparada.contains("colunas-trocadas")
            val tTexto = if (trocadas) c[5] else c[6]
            val eTexto = if (trocadas) c[6] else c[5]
            val pag = m.optInt("conferidoPagina", -1)

            // duração
            if (por.optString("duracao") in setOf("tabela", "tabela+entrada")) {
                val tipo = textoOuNulo(m, "duracaoTipo")
                if (tipo != null) {
                    val (lidoTipo, lidoSeg) = MagicTime.parseDuracao(c[4])
                    val esperado = when (tipo) {
                        "instantanea" -> TipoDuracao.INSTANTANEA
                        "temporaria" -> TipoDuracao.TEMPORARIA
                        else -> TipoDuracao.PERMANENTE // permanente e indefinida (o app trata "Indef." como permanente)
                    }
                    val seg = inteiroOuNulo(m, "duracaoSeg") ?: 0
                    if (lidoTipo != esperado || (esperado == TipoDuracao.TEMPORARIA && lidoSeg != seg)) {
                        erros += "$nome (p.$pag): duração — conferido $tipo/${seg}s ≠ tabela '${c[4]}' ($lidoTipo/${lidoSeg}s)"
                    }
                    duracoes++
                }
            }
            // tempo de operação
            if (por.optString("tempoOperacao") in setOf("tabela", "tabela+entrada")) {
                val tipo = textoOuNulo(m, "tempoTipo")
                val seg = inteiroOuNulo(m, "tempoOperacaoSeg")
                if (seg != null && (tipo == "fixo" || tipo == "faixa")) {
                    val lido = MagicTime.parseTempoSeg(tTexto)
                    if (lido != seg) erros += "$nome (p.$pag): tempo — conferido ${seg}s ≠ tabela '$tTexto' (${lido}s)"
                    tempos++
                }
            }
            // custo de operar e de manter
            if (por.optString("custo") in setOf("tabela", "tabela+entrada")) {
                val operar = inteiroOuNulo(m, "custoOperar")
                if (operar != null) {
                    val lido = MagicEnergy.parse(eTexto)
                    if (lido.base != operar || lido.variavel) {
                        erros += "$nome (p.$pag): custo — conferido $operar ≠ tabela '$eTexto' (base ${lido.base}, variável ${lido.variavel})"
                    }
                    custos++
                    val manter = inteiroOuNulo(m, "custoManter")
                    if (manter != null && textoOuNulo(m, "custoManterTipo") == "numero" && lido.manutencao != null) {
                        if (lido.manutencao != manter) erros += "$nome (p.$pag): manter — conferido $manter ≠ tabela '$eTexto' (${lido.manutencao})"
                        manutencoes++
                    }
                }
            }
        }
        assertTrue("número conferido fora do livro em ${erros.size} ponto(s): ${erros.take(10)}", erros.isEmpty())
        assertTrue("poucas conferências: dur=$duracoes tempo=$tempos custo=$custos manter=$manutencoes",
            duracoes > 600 && tempos > 500 && custos > 300 && manutencoes > 150)
    }

    /** O que o app e a Mesa leem tem sentido: fórmula NUNCA vira custo fixo; "não mantém" é 0 e só ele. */
    @Test
    fun `os numeros conferidos tem forma e sentido`() {
        Assume.assumeTrue(catalogoArq != null)
        val cat = catalogo()
        val erros = mutableListOf<String>()
        val tiposDuracao = setOf("instantanea", "temporaria", "permanente") // "Indefinida" = permanente + duracaoFormula
        for (i in 0 until cat.length()) {
            val m = cat.getJSONObject(i)
            val nome = m.getString("nome")
            val tipo = textoOuNulo(m, "duracaoTipo")
            if (tipo != null && tipo !in tiposDuracao) erros += "$nome: duracaoTipo '$tipo' desconhecido"
            if (tipo == "temporaria" && (inteiroOuNulo(m, "duracaoSeg") ?: 0) <= 0) erros += "$nome: temporária sem segundos"
            if (tipo != null && tipo != "temporaria" && (inteiroOuNulo(m, "duracaoSeg") ?: 0) != 0) erros += "$nome: $tipo com segundos"
            // 🔴 o app trata `custoOperar` como custo FIXO e não pergunta nada: fórmula não pode ter
            val operar = inteiroOuNulo(m, "custoOperar")
            val custoTipo = textoOuNulo(m, "custoTipo")
            if (operar != null && custoTipo != "fixo") erros += "$nome: custoOperar $operar com custoTipo '$custoTipo' (fórmula não é número)"
            if (operar != null && operar < 0) erros += "$nome: custoOperar negativo"
            val manter = inteiroOuNulo(m, "custoManter")
            val manterTipo = textoOuNulo(m, "custoManterTipo")
            if (manter == 0 && manterTipo != "nao") erros += "$nome: custoManter 0 sem 'não pode ser mantida'"
            if (manterTipo == "nao" && manter != 0) erros += "$nome: 'não pode ser mantida' sem custoManter 0"
            if (manter != null && manterTipo !in setOf("numero", "igual", "nao")) erros += "$nome: custoManter $manter com custoManterTipo '$manterTipo'"
            if (manterTipo == "metade" && manter != null) erros += "$nome: 'metade' virou número ($manter): o arredondamento é regra do MG-3"
            if (textoOuNulo(m, "conferido") == "livro" && m.optJSONObject("conferidoPor") == null) erros += "$nome: conferido sem conferidoPor"
        }
        assertTrue("números conferidos sem sentido em ${erros.size} magia(s): ${erros.take(10)}", erros.isEmpty())
    }

    /**
     * As 8 magias em que o número "conferido" de 17/jul estava ERRADO (e o cabeçalho certo): a
     * CONFERENCIA-LIVRO-MG-0 as leu no livro. Duração em segundos · tempo em segundos · operar/manter.
     */
    @Test
    fun `as 8 magias que estavam erradas saem com os valores do livro`() {
        Assume.assumeTrue(catalogoArq != null)
        val cat = catalogo()
        fun por(nome: String): JSONObject {
            for (i in 0 until cat.length()) if (cat.getJSONObject(i).getString("nome") == nome) return cat.getJSONObject(i)
            throw AssertionError("$nome não está no catálogo")
        }
        // nome -> (duração s, tempo s, operar, manter)
        val livro = mapOf(
            "Lealdade" to listOf(3600, 2, 2, 2),
            "Audição Remota" to listOf(60, 3, 4, 2),
            "Eliminar Odor" to listOf(3600, null, 2, 2),
            "Possessão de Máquina/NT" to listOf(60, 30, 6, 2),
            "Torpor" to listOf(60, 2, 3, 2),
            "Cativar" to listOf(3600, 1, 3, 3),
            "Alimento Essencial" to listOf(null, 30, null, null),
            "Arma Congelante" to listOf(60, 3, 3, 1),
        )
        val erros = mutableListOf<String>()
        livro.forEach { (nome, esperado) ->
            val m = por(nome)
            val lido = listOf(
                inteiroOuNulo(m, "duracaoSeg").takeIf { textoOuNulo(m, "duracaoTipo") == "temporaria" },
                inteiroOuNulo(m, "tempoOperacaoSeg"),
                inteiroOuNulo(m, "custoOperar"),
                inteiroOuNulo(m, "custoManter"),
            )
            esperado.forEachIndexed { k, v ->
                if (v != null && lido[k] != v) erros += "$nome: ${listOf("duração", "tempo", "operar", "manter")[k]} — esperado $v, catálogo ${lido[k]}"
            }
        }
        assertEquals("as 8 do CONFERENCIA: $erros", emptyList<String>(), erros)
        // O Alimento Essencial: duração INDEFINIDA (a tabela dizia Perm.; a entrada, Indefinida), e custo
        // por refeição = fórmula. "Indefinida" entra como permanente + a palavra do livro.
        val alimento = por("Alimento Essencial")
        assertEquals("permanente", textoOuNulo(alimento, "duracaoTipo"))
        assertEquals("Indefinida", textoOuNulo(alimento, "duracaoFormula"))
        assertEquals(null, inteiroOuNulo(alimento, "custoOperar"))
    }
}
