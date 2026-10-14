package com.gurps.ficha.domain.magic

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.io.File
import java.text.Normalizer

/**
 * Lote MC-1 — GUARDA: o cabeçalho do catálogo de magias é o do LIVRO.
 *
 * O livro (`chunks.jsonl`, `pt_magia`) traz cada magia numa tabela-resumo (pp.223–237). O script
 * `scripts/magias/aplicar-cabecalho-da-tabela.js` copiou dela `duracao`, `energia`, `tempoOperacao`,
 * `classe` e `pagina` para o `magias2versao.json`, e marcou a origem em `fonteCabecalho`.
 *
 * Este teste NÃO confia no script: relê a linha da tabela direto do `chunks.jsonl` (o índice
 * `magias-no-livro.json` só diz em que página e linha ela está) e exige igualdade campo a campo.
 * O que ficou diferente da tabela, de propósito, está em `cabecalho-excecoes.json` com o motivo e o
 * valor exato — qualquer outra diferença fica vermelha com o nome da magia e a página.
 *
 * Se os arquivos não existirem (build headless), os testes são PULADOS (Assume).
 */
class MagicCatalogCabecalhoDoLivroTest {

    private fun achar(vararg caminhos: String): File? = caminhos.map { File(it) }.firstOrNull { it.exists() }

    private val catalogoArq = achar("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")
    private val chunksArq = achar("src/main/assets/chunks.jsonl", "app/src/main/assets/chunks.jsonl")
    private val indiceArq = achar("../scripts/magias/magias-no-livro.json", "scripts/magias/magias-no-livro.json")
    private val excecoesArq = achar("../scripts/magias/cabecalho-excecoes.json", "scripts/magias/cabecalho-excecoes.json")
    private val codigoDir = achar("src/main/java", "app/src/main/java")

    private fun norm(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
            .lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    /** As páginas do livro de Magia: número -> linhas. */
    private fun paginasDoLivro(): Map<Int, List<String>> {
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

    private fun temOsArquivos() = catalogoArq != null && chunksArq != null && indiceArq != null && excecoesArq != null

    private fun textoOuNulo(o: JSONObject, k: String): String? = if (o.has(k) && !o.isNull(k)) o.getString(k) else null

    /** O índice aponta, de fato, para a linha da tabela com os valores que ele diz (a releitura é independente do script). */
    @Test
    fun `o indice da tabela aponta para a linha certa do livro`() {
        Assume.assumeTrue(temOsArquivos())
        val livro = paginasDoLivro()
        val magias = JSONObject(indiceArq!!.readText(Charsets.UTF_8)).getJSONObject("magias")
        val erros = mutableListOf<String>()
        var conferidas = 0
        magias.keys().forEach { id ->
            val t = magias.getJSONObject(id).optJSONObject("tabela") ?: return@forEach
            val pagina = t.getInt("paginaDoChunk")
            val linha = t.getInt("linha")
            val texto = livro[pagina]?.getOrNull(linha)
            if (texto == null) { erros += "$id: sem a linha $linha da página $pagina"; return@forEach }
            var c = celulas(texto)
            val reparada = t.optString("reparada", "")
            if (reparada.contains("celulas-coladas")) return@forEach // 8 células, reparadas à mão pelo script
            if (c.size != 9) { erros += "$id (p.$pagina:$linha): ${c.size} células"; return@forEach }
            val campos = t.getJSONObject("campos")
            // a página com Energia e Tempo trocados: o índice já guarda na ordem certa
            val energia = if (reparada.contains("colunas-trocadas")) c[6] else c[5]
            val tempo = if (reparada.contains("colunas-trocadas")) c[5] else c[6]
            val iguais = norm(c[1].removeSuffix("*")) == norm(t.getString("nome")) &&
                c[2] == campos.getString("classe") && c[4] == campos.getString("duracao") &&
                energia == campos.getString("energia") && tempo == campos.getString("tempo")
            if (!iguais) erros += "$id (p.$pagina:$linha): o índice diz outra coisa que a linha do livro"
            conferidas++
        }
        assertTrue("índice fora do livro em ${erros.size} magia(s): ${erros.take(8)}", erros.isEmpty())
        assertTrue("poucas linhas conferidas: $conferidas", conferidas > 800)
    }

    /** O cabeçalho do catálogo é o que o livro escreve, exceto o que está nas exceções registradas. */
    @Test
    fun `o cabecalho do catalogo e o da tabela do livro`() {
        Assume.assumeTrue(temOsArquivos())
        val catalogo = JSONArray(catalogoArq!!.readText(Charsets.UTF_8))
        val indice = JSONObject(indiceArq!!.readText(Charsets.UTF_8)).getJSONObject("magias")
        val excecoes = JSONObject(excecoesArq!!.readText(Charsets.UTF_8)).getJSONObject("magias")
        val erros = mutableListOf<String>()
        var conferidas = 0

        for (i in 0 until catalogo.length()) {
            val m = catalogo.getJSONObject(i)
            if (textoOuNulo(m, "fonteCabecalho") != "tabela") continue
            val id = m.getString("id")
            val nome = m.getString("nome")
            val t = indice.optJSONObject(id)?.optJSONObject("tabela")
            if (t == null) { erros += "$nome: marcada \"tabela\" mas o índice não tem a linha"; continue }
            val campos = t.getJSONObject("campos")
            val pag = if (t.isNull("pagina")) "?" else t.getInt("pagina").toString()
            val ex = excecoes.optJSONObject(id)

            fun confere(campo: String, doCatalogo: String?, daTabela: String?) {
                val e = ex?.optJSONObject(campo)
                if (e != null) {
                    // diferença registrada: tem de ser EXATAMENTE a registrada
                    if (doCatalogo != e.optString("catalogo", null) || daTabela != e.optString("tabela", null)) {
                        erros += "$nome (p.$pag): $campo — exceção velha: catálogo '$doCatalogo', tabela '$daTabela'"
                    }
                    return
                }
                if (doCatalogo != daTabela) erros += "$nome (p.$pag): $campo — catálogo '$doCatalogo' ≠ livro '$daTabela'"
            }

            confere("duracao", textoOuNulo(m, "duracao"), campos.getString("duracao"))
            confere("energia", textoOuNulo(m, "energia"), campos.getString("energia"))
            confere("tempoOperacao", textoOuNulo(m, "tempoOperacao"), campos.getString("tempo"))

            // classe: mesma coisa para o parser do app (a tabela escreve "R-Espec", o catálogo "R-Espec.")
            val cat = MagicClassParser.parse(textoOuNulo(m, "classe"))
            val tab = MagicClassParser.parse(campos.getString("classe"))
            val classeIgual = cat.classes.toSet() == tab.classes.toSet() &&
                cat.resistencia?.atributo == tab.resistencia?.atributo &&
                cat.resistencia?.modificadorDefensor == tab.resistencia?.modificadorDefensor
            val exClasse = ex?.optJSONObject("classe")
            if (exClasse != null) {
                if (textoOuNulo(m, "classe") != exClasse.optString("catalogo", null)) {
                    erros += "$nome (p.$pag): classe — exceção velha: catálogo '${textoOuNulo(m, "classe")}'"
                }
            } else if (!classeIgual) {
                erros += "$nome (p.$pag): classe — catálogo '${textoOuNulo(m, "classe")}' ≠ livro '${campos.getString("classe")}'"
            }

            // página: a coluna "Pág." da tabela (vazia em 3 linhas do texto extraído)
            if (!t.isNull("pagina")) {
                val doCatalogo = if (m.isNull("pagina")) null else m.getInt("pagina")
                val exPagina = ex?.optJSONObject("pagina")
                if (doCatalogo != t.getInt("pagina") && exPagina == null) {
                    erros += "$nome: página — catálogo $doCatalogo ≠ livro ${t.getInt("pagina")}"
                }
            }
            conferidas++
        }
        assertTrue("cabeçalho fora do livro em ${erros.size} ponto(s): ${erros.take(10)}", erros.isEmpty())
        assertTrue("poucas magias conferidas: $conferidas", conferidas > 780)
    }

    /** Toda exceção registrada ainda se refere a uma magia do catálogo e traz o motivo. */
    @Test
    fun `as excecoes do cabecalho tem motivo e magia`() {
        Assume.assumeTrue(temOsArquivos())
        val catalogo = JSONArray(catalogoArq!!.readText(Charsets.UTF_8))
        val ids = (0 until catalogo.length()).map { catalogo.getJSONObject(it).getString("id") }.toSet()
        val excecoes = JSONObject(excecoesArq!!.readText(Charsets.UTF_8)).getJSONObject("magias")
        val erros = mutableListOf<String>()
        excecoes.keys().forEach { id ->
            if (id !in ids) erros += "$id: exceção de uma magia que não existe mais"
            val campos = excecoes.getJSONObject(id)
            campos.keys().forEach { campo ->
                if (campos.getJSONObject(campo).optString("motivo", "").isBlank()) erros += "$id.$campo: exceção sem motivo"
            }
        }
        assertTrue("exceções ruins: ${erros.take(8)}", erros.isEmpty())
    }

    /**
     * O motor procura magia POR NOME em texto (`hasMagia("Animação")`, `nivelMagia("Furacão")`…) e a
     * ficha guarda uma cópia do nome. Se um nome do catálogo mudar e essa regra não for atualizada,
     * ela deixa de valer em silêncio. Cada nome procurado no código tem de existir no catálogo.
     */
    @Test
    fun `os nomes de magia que o codigo procura existem no catalogo`() {
        Assume.assumeTrue(catalogoArq != null && codigoDir != null)
        val catalogo = JSONArray(catalogoArq!!.readText(Charsets.UTF_8))
        val nomes = (0 until catalogo.length()).map { norm(catalogo.getJSONObject(it).getString("nome")) }.toSet()
        val procurados = sortedSetOf<String>()
        val re = Regex("""(?:hasMagia|nivelMagia)\("([^"]+)"\)|magia\.nome\.equals\("([^"]+)\"""")
        codigoDir!!.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { arq ->
            re.findAll(arq.readText(Charsets.UTF_8)).forEach { r ->
                procurados += (r.groupValues[1].ifEmpty { r.groupValues[2] })
            }
        }
        assertTrue("não achei nenhum nome procurado no código (a regex quebrou?)", procurados.size >= 10)
        // o código escreve a mesma magia com e sem acento ("Furacão"/"Furacao"): basta existir uma das grafias
        val porNormal = procurados.groupBy { norm(it) }
        val faltam = porNormal.keys.filter { it !in nomes }.toSet()
        // FORMA ADAPTADA, CONHECIDA (e INTENCIONAL — explicação do Rodolfo em 13/out): `transformar_outro`
        // pede hasMagia("Metamorfosear Outro"), e o catálogo só tem as formas por tipo "(Criaturas do
        // Ar/Mar/Terra)". O motor de busca de magias foi adaptado para manter a cadeia de pré-requisitos
        // com essas formas; a regra do livro não mudou. NÃO é erro: fica registrada aqui só para o teste
        // avisar se isso mudar. Não "consertar" sem falar com ele.
        val lacunasConhecidas = setOf("metamorfosear outro")
        assertTrue(
            "o código procura magia que o catálogo não tem: ${faltam - lacunasConhecidas}",
            (faltam - lacunasConhecidas).isEmpty(),
        )
        assertTrue(
            "a forma adaptada já não é assim: tire ${lacunasConhecidas - faltam} da lista",
            (lacunasConhecidas - faltam).isEmpty(),
        )
    }
}
