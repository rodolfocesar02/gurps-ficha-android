package com.gurps.ficha.domain.magic

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.io.File
import java.text.Normalizer

/**
 * Lote MC-4 — GUARDA: a `mecanica` das magias não se afasta do LIVRO.
 *
 * A `mecanica` (efeito, entrega, dano por energia, 1/2D…) foi curadoria por IA em cima de uma
 * descrição que tinha erros. O script `scripts/magias/aplicar-mecanica.js` a confrontou com o bloco
 * da entrada (a `descricao` do MC-3) e com a classe da tabela. Este teste NÃO usa o script: relê o
 * catálogo e passa a classe pelo parser DO APP, e o dano e os projéteis pelo texto da descrição.
 *
 *  1. entrega × classe do livro: "projetil" só em classe Projétil; "toque" só em classe Toque ou nas
 *     que o texto da entrada exige (lista abaixo);
 *  2. o dado do `danoPorEnergia` aparece no texto da entrada;
 *  3. 1/2D, Máx e Prec do projétil são os que a entrada escreve;
 *  4. o atributo da `condicaoResistencia` é o da classe da tabela.
 *
 * Sem os arquivos (build headless), os testes são PULADOS.
 */
class MagicCatalogMecanicaDoLivroTest {

    private fun achar(vararg caminhos: String): File? = caminhos.map { File(it) }.firstOrNull { it.exists() }
    private val catalogoArq = achar("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")

    private fun norm(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
            .lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    private fun catalogo(): JSONArray = JSONArray(catalogoArq!!.readText(Charsets.UTF_8))
    private fun mec(m: JSONObject): JSONObject = m.optJSONObject("mecanica") ?: JSONObject()
    private fun textoOuNulo(o: JSONObject, k: String): String? = if (o.has(k) && !o.isNull(k)) o.getString(k) else null

    /** Onde o TEXTO da entrada exige tocar o alvo (CONFERENCIA-LIVRO-MG-0 §5), e o toque OPCIONAL. */
    private val toqueExigido = setOf(
        "purificar", "conceder magica", "suspender aptidao magica", "roubar vitalidade", "roubar energia", "leitura do corpo",
    )
    private val toqueOpcional = setOf("desidratar", "geladura")

    /** O corpo da descrição do livro: até o sub-bloco "Item". */
    private fun corpo(m: JSONObject): String? {
        if (!m.optBoolean("descricaoConferida", false)) return null
        val d = m.optString("descricao", "")
        val i = Regex("\\n(?:Item|Itens)\\n").find(d)?.range?.first ?: -1
        return if (i > 0) d.substring(0, i) else d
    }

    @Test
    fun `projetil so em classe Projetil e toque so em classe Toque ou onde o texto exige`() {
        Assume.assumeTrue(catalogoArq != null)
        val cat = catalogo()
        val erros = mutableListOf<String>()
        for (i in 0 until cat.length()) {
            val m = cat.getJSONObject(i)
            val nome = m.getString("nome")
            val entrega = textoOuNulo(mec(m), "entrega")
            val classes = MagicClassParser.parse(textoOuNulo(m, "classe")).classes
            if (entrega == "projetil" && TipoClasseMagia.PROJETIL !in classes) {
                erros += "$nome (p.${m.optInt("pagina")}): entrega projetil em classe ${m.optString("classe")}"
            }
            if (entrega == "toque" && TipoClasseMagia.TOQUE !in classes && norm(nome) !in toqueExigido && norm(nome) !in toqueOpcional) {
                erros += "$nome (p.${m.optInt("pagina")}): entrega toque em classe ${m.optString("classe")} sem o texto exigir"
            }
            if (norm(nome) in toqueExigido && entrega != "toque") {
                erros += "$nome (p.${m.optInt("pagina")}): o texto do livro exige tocar o alvo e a entrega é '$entrega'"
            }
        }
        assertTrue("entrega fora do livro em ${erros.size} magia(s): ${erros.take(8)}", erros.isEmpty())
    }

    @Test
    fun `o dado do dano por energia aparece no texto da entrada`() {
        Assume.assumeTrue(catalogoArq != null)
        val cat = catalogo()
        val erros = mutableListOf<String>()
        // Sem NENHUM dado no texto, o dano é fórmula ("1 por dado de dano": Geladura) ou a entrada não
        // traz o dado (Sopro Congelante só traz o Item): o JS (`mecanica.test.js`) confere a fórmula.
        var conferidas = 0
        for (i in 0 until cat.length()) {
            val m = cat.getJSONObject(i)
            val k = mec(m)
            if (textoOuNulo(k, "efeito") != "dano") continue
            val dado = textoOuNulo(k, "danoPorEnergia") ?: continue
            if (!Regex("^\\d+d").containsMatchIn(dado)) continue
            val c = corpo(m) ?: continue
            val dados = Regex("\\b(\\d+d(?:[+-]\\d+)?)(?![\\w])").findAll(c).map { it.groupValues[1] }.toSet()
            if (dados.isEmpty()) continue
            conferidas++
            if (dado !in dados) erros += "${m.getString("nome")} (p.${m.optInt("descricaoPagina")}): dano '$dado' não está no texto do livro (traz $dados)"
        }
        assertTrue("dano fora do livro em ${erros.size} magia(s): ${erros.take(6)}", erros.isEmpty())
        assertTrue("poucas conferidas: $conferidas", conferidas >= 28)
    }

    @Test
    fun `1 meio D, Max e Prec do projetil sao os que a entrada escreve`() {
        Assume.assumeTrue(catalogoArq != null)
        val cat = catalogo()
        val erros = mutableListOf<String>()
        var conferidas = 0
        for (i in 0 until cat.length()) {
            val m = cat.getJSONObject(i)
            if (TipoClasseMagia.PROJETIL !in MagicClassParser.parse(textoOuNulo(m, "classe")).classes) continue
            val c = corpo(m) ?: continue
            val k = mec(m)
            fun numero(re: String): Double? = Regex(re, RegexOption.IGNORE_CASE).find(c)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()
            val livro = mapOf(
                "alcanceMeioDano" to numero("1/2\\s*D\\s*[:=]?\\s*(\\d+(?:[.,]\\d+)?)"),
                "alcanceMaximo" to numero("\\bM[aá]x(?:imo)?\\.?\\s*[:=]?\\s*(\\d+(?:[.,]\\d+)?)"),
                "precisao" to numero("\\bPrec(?:is[aã]o)?\\.?\\s*[:=]?\\s*([+-]?\\d+)"),
            )
            livro.forEach { (campo, v) ->
                if (v == null) return@forEach
                conferidas++
                val meu = if (k.has(campo) && !k.isNull(campo)) k.getDouble(campo) else null
                if (meu != v) erros += "${m.getString("nome")} (p.${m.optInt("descricaoPagina")}): $campo catálogo $meu ≠ livro $v"
            }
        }
        assertTrue("projétil fora do livro em ${erros.size} ponto(s): ${erros.take(6)}", erros.isEmpty())
        assertTrue("poucas conferidas: $conferidas", conferidas >= 25)
    }

    @Test
    fun `o atributo da resistencia da mecanica e o da classe da tabela`() {
        Assume.assumeTrue(catalogoArq != null)
        val cat = catalogo()
        // Controle de Pessoa: a mecânica diz HT por PV e a classe da tabela, Vontade. Controle de Elemental
        // Fogo: a mecânica diz HT e a classe, "ST ou Vontade". Ficam para o Rodolfo conferir na entrada
        // (RELATORIO-MC-4 §6); quando decidir, tire daqui.
        val conhecidas = setOf("controle de pessoa", "controle de elemental fogo")
        val erros = mutableListOf<String>()
        for (i in 0 until cat.length()) {
            val m = cat.getJSONObject(i)
            val cond = textoOuNulo(mec(m), "condicaoResistencia") ?: continue
            val res = MagicClassParser.parse(textoOuNulo(m, "classe")).resistencia ?: continue
            val dela = when {
                cond.startsWith("HT", true) -> AtributoResistencia.HT
                cond.startsWith("Vont", true) -> AtributoResistencia.VONTADE
                cond.startsWith("DX", true) -> AtributoResistencia.DX
                cond.startsWith("ST", true) -> AtributoResistencia.ST
                cond.startsWith("IQ", true) -> AtributoResistencia.IQ
                else -> null
            } ?: continue
            if (res.atributo in setOf(AtributoResistencia.ESPECIAL, AtributoResistencia.COMPOSTA, AtributoResistencia.MAGICA,
                    AtributoResistencia.VONTADE_OU_PERICIA)) continue
            if (dela != res.atributo && norm(m.getString("nome")) !in conhecidas) {
                erros += "${m.getString("nome")} (p.${m.optInt("pagina")}): mecânica '$cond' × classe '${m.optString("classe")}'"
            }
        }
        assertTrue("resistência divergente em ${erros.size} magia(s): ${erros.take(6)}", erros.isEmpty())
    }

    @Test
    fun `toda magia tem a marca de conferencia da mecanica`() {
        Assume.assumeTrue(catalogoArq != null)
        val cat = catalogo()
        val semMarca = (0 until cat.length()).map { cat.getJSONObject(it) }.filter { !it.has("mecanicaConferida") }.map { it.getString("nome") }
        assertTrue("sem mecanicaConferida: ${semMarca.take(8)}", semMarca.isEmpty())
        val semMotivo = (0 until cat.length()).map { cat.getJSONObject(it) }
            .filter { !it.getBoolean("mecanicaConferida") && it.optString("mecanicaMotivo", "").isBlank() }.map { it.getString("nome") }
        assertTrue("não conferida sem motivo: ${semMotivo.take(8)}", semMotivo.isEmpty())
    }
}
