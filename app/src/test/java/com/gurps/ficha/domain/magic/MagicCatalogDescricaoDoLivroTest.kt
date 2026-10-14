package com.gurps.ficha.domain.magic

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.io.File

/**
 * Lote MC-3 — GUARDA: a `descricao` das magias é TEXTO DO LIVRO.
 *
 * A descrição era a transcrição de uma IA (278 de 812 com algum número diferente do livro; 8 eram de
 * outra magia). O script `scripts/magias/aplicar-descricoes.js` a trocou pelo bloco da entrada,
 * recortado do `chunks.jsonl`, e marcou `descricaoConferida: true` com a página.
 *
 * Este teste NÃO usa o script. Para toda magia com `descricaoConferida: true`, exige que **cada linha**
 * da descrição exista no texto do livro, nas páginas do bloco (a página marcada e as duas seguintes),
 * depois de tirar o markdown. Se alguém reescrever uma frase à mão, ou colar o texto de outra magia, a
 * linha não existe ali e o teste fica vermelho com o nome da magia, a página e a linha.
 *
 * (A igualdade caractere a caractere com o recorte, em 30 magias de semente fixa, está em
 * `scripts/magias/descricao.test.js`.) Sem os arquivos (build headless), os testes são PULADOS.
 */
class MagicCatalogDescricaoDoLivroTest {

    private fun achar(vararg caminhos: String): File? = caminhos.map { File(it) }.firstOrNull { it.exists() }

    private val catalogoArq = achar("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")
    private val chunksArq = achar("src/main/assets/chunks.jsonl", "app/src/main/assets/chunks.jsonl")

    /** Sem markdown, sem borda de tabela, espaços juntos, minúsculas — o suficiente para comparar. */
    private fun norm(s: String): String =
        s.replace(Regex("[*_#>|]"), " ").replace(Regex("\\s+"), " ").trim().lowercase()

    private fun paginasDoLivro(): Map<Int, String> {
        val m = HashMap<Int, String>()
        chunksArq!!.forEachLine { linha ->
            if (linha.isBlank() || !linha.contains("\"pt_magia\"")) return@forEachLine
            val o = JSONObject(linha)
            if (o.optString("source_id") != "pt_magia") return@forEachLine
            m[o.getInt("page_number")] = norm(o.getString("text"))
        }
        return m
    }

    @Test
    fun `cada linha da descricao conferida existe no livro, na pagina do bloco`() {
        Assume.assumeTrue(catalogoArq != null && chunksArq != null)
        val livro = paginasDoLivro()
        val cat = JSONArray(catalogoArq!!.readText(Charsets.UTF_8))
        val erros = mutableListOf<String>()
        var conferidas = 0

        for (i in 0 until cat.length()) {
            val m = cat.getJSONObject(i)
            if (!m.optBoolean("descricaoConferida", false)) continue
            conferidas++
            val nome = m.getString("nome")
            val pagina = m.optInt("descricaoPagina", -1)
            if (pagina < 0) { erros += "$nome: conferida sem descricaoPagina"; continue }
            // o bloco começa na página marcada e pode atravessar até 2 páginas
            val texto = (pagina..pagina + 2).mapNotNull { livro[it] }.joinToString(" ")
            m.getString("descricao").split("\n").map { norm(it) }.filter { it.isNotEmpty() }.forEach { linha ->
                if (!texto.contains(linha)) erros += "$nome (p.$pagina): linha que não está no livro: '${linha.take(60)}…'"
            }
        }
        assertTrue("descrição fora do livro em ${erros.size} ponto(s): ${erros.take(6)}", erros.isEmpty())
        assertTrue("poucas descrições conferidas: $conferidas", conferidas > 780)
    }

    @Test
    fun `o que nao foi recortado fica marcado com o motivo, e toda magia tem a marca`() {
        Assume.assumeTrue(catalogoArq != null)
        val cat = JSONArray(catalogoArq!!.readText(Charsets.UTF_8))
        val erros = mutableListOf<String>()
        var naoConferidas = 0
        for (i in 0 until cat.length()) {
            val m = cat.getJSONObject(i)
            val nome = m.getString("nome")
            if (!m.has("descricaoConferida")) { erros += "$nome: sem descricaoConferida"; continue }
            if (!m.optBoolean("descricaoConferida")) {
                naoConferidas++
                if (m.optString("descricaoMotivo", "").isBlank()) erros += "$nome: descrição antiga sem motivo"
                if (m.optString("descricao", "").isBlank()) erros += "$nome: descrição antiga vazia"
            }
        }
        assertTrue("marcas ruins: ${erros.take(8)}", erros.isEmpty())
        // 67 no dia 14/out (páginas embaralhadas e entradas ausentes do texto): não pode piorar
        assertTrue("mais descrições sem conferir do que antes: $naoConferidas", naoConferidas <= 67)
    }
}
