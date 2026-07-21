package com.gurps.ficha.domain.magic

import org.json.JSONArray
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.io.File

/**
 * Lote MA-1 — REALITY CHECK: parseia TODAS as 879 magias do catálogo real
 * (`magias2versao.json`) e valida:
 *   1. Nenhuma faz o parser crashar.
 *   2. Toda `classe` bruta reconhece pelo menos UMA classe canônica OU cai em ESPECIAL (fallback ok).
 *   3. Toda resistência do tipo `R-XXX` produz `resistencia != null` (o parser não pode "engolir" resistência).
 *
 * Se o arquivo não existir (build headless), o teste é PULADO (Assume) sem falhar.
 */
class MagicCatalogRealityCheckTest {

    private fun carregarCatalogo(): JSONArray? {
        val paths = listOf(
            "src/main/assets/magias2versao.json",           // rodando de app/
            "app/src/main/assets/magias2versao.json",       // rodando da raiz do projeto
        )
        val arquivo = paths.map { File(it) }.firstOrNull { it.exists() } ?: return null
        val texto = arquivo.readText(Charsets.UTF_8)
        return JSONArray(texto)
    }

    @Test
    fun `parser aceita TODAS as classes do JSON real sem crashar`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        var total = 0
        var comParteNaoReconhecida = 0
        val exemplosNaoReconhecidos = mutableListOf<String>()

        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val classeStr = magia.optString("classe", null)
            val id = magia.optString("id", "?")
            val parsed = MagicClassParser.parse(classeStr) // não pode lançar
            total++
            if (parsed.temParteNaoReconhecida) {
                comParteNaoReconhecida++
                if (exemplosNaoReconhecidos.size < 10) {
                    exemplosNaoReconhecidos.add("$id [$classeStr] -> classes=${parsed.classes} res=${parsed.resistencia}")
                }
            }
        }

        println("[MagicCatalogRealityCheck] total=$total, comParteNaoReconhecida=$comParteNaoReconhecida")
        exemplosNaoReconhecidos.forEach { println("  ↳ $it") }

        // Limite honesto: até 3% de "parte não reconhecida" é aceitável (~25 magias raras). Se subir
        // além disso, o parser precisa de mais aliases.
        val fracao = comParteNaoReconhecida.toDouble() / total
        assertTrue("Fração de magias com parte não reconhecida acima de 3%: $fracao ($comParteNaoReconhecida / $total)",
            fracao <= 0.03)
    }

    @Test
    fun `parser NUNCA perde a resistencia R-XXX declarada no JSON`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val perdidas = mutableListOf<String>()
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val classeStr = magia.optString("classe", "")
            val id = magia.optString("id", "?")
            // Se a classe menciona R-, o parser TEM que devolver alguma resistencia.
            val temResistenciaNaString = "R-" in classeStr || "R/" in classeStr || " R " in classeStr
            val parsed = MagicClassParser.parse(classeStr)
            if (temResistenciaNaString && parsed.resistencia == null) {
                perdidas.add("$id [$classeStr]")
            }
        }
        if (perdidas.isNotEmpty()) {
            println("[MagicCatalogRealityCheck] perdidas: ${perdidas.size}")
            perdidas.take(20).forEach { println("  ↳ $it") }
        }
        assertTrue("Parser PERDEU resistência em ${perdidas.size} magia(s): ${perdidas.take(5)}",
            perdidas.isEmpty())
    }

    // ── Lote MEC-5: o custo do catálogo REAL ────────────────────────────────────────────────────

    @Test
    fun `nenhuma magia do catalogo real perde o custo no formato operar-manter`() {
        // O bug: "04/02" (operar 4, manter 2) casava no regex de FRAÇÃO e virava 2,0 com base=null —
        // 307 das 879. Esta trava roda contra o catálogo real para não voltar a passar batido.
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val quebradas = mutableListOf<String>()
        var comOperarManter = 0
        val formato = Regex("""^\s*\d+\s*/\s*\d+\s*#?\s*$""")
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val bruto = magia.optString("energia", "")
            if (!formato.matches(bruto)) continue
            comOperarManter++
            val c = MagicEnergy.parse(bruto)
            val esperadoOperar = bruto.trim().removeSuffix("#").split("/")[0].trim().toInt()
            val esperadoManter = bruto.trim().removeSuffix("#").split("/")[1].trim().toInt()
            if (c.base != esperadoOperar || c.manutencao != esperadoManter || c.fracao != null)
                quebradas += "${magia.optString("nome")}: '$bruto' → base=${c.base} manut=${c.manutencao} fracao=${c.fracao}"
        }
        println("MEC-5: $comOperarManter mágicas no formato operar/manter no catálogo real.")
        assertTrue("o catálogo real precisa ter mágicas nesse formato para o teste valer", comOperarManter > 100)
        assertTrue("Custo lido ERRADO em ${quebradas.size} magia(s): ${quebradas.take(5)}", quebradas.isEmpty())
    }

    /**
     * Lote P3-1: as 6 mágicas curadas neste lote têm que continuar EXECUTÁVEIS no catálogo real.
     * Uma curadoria só vale enquanto o dado sobrevive — foi assim que 156 buffs viraram rótulo.
     */
    @Test
    fun `as 6 magias curadas no P3-1 continuam executaveis no catalogo real`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        // id → (campo que precisa existir, valor esperado)
        val esperado = mapOf(
            "bloquear" to Triple("buffBd", 1, 1),                    // BD +1 por 1 de energia
            "robustez" to Triple("buffRd", 1, 1),                    // RD +1 por 1 de energia
            "fortalecer_vontade" to Triple("buffAtributoValor", 1, 1),
            "enfraquecer_vontade" to Triple("buffAtributoValor", -1, 2), // -1 a cada 2 de energia
            "sabedoria" to Triple("buffAtributoValor", 1, 4),        // +1 de IQ a cada 4
            "tolice" to Triple("buffAtributoValor", -1, 1),
        )
        val faltando = mutableListOf<String>()
        var achadas = 0
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val alvo = esperado[magia.optString("id")] ?: continue
            achadas++
            val mec = magia.optJSONObject("mecanica")
            if (mec == null) { faltando += "${magia.optString("id")}: sem mecanica"; continue }
            val (campo, valor, porNivel) = alvo
            if (mec.optInt(campo, 0) != valor)
                faltando += "${magia.optString("id")}: $campo=${mec.optInt(campo, 0)}, esperado $valor"
            if (mec.optInt("buffEnergiaPorNivel", 0) != porNivel)
                faltando += "${magia.optString("id")}: energiaPorNivel=${mec.optInt("buffEnergiaPorNivel", 0)}, esperado $porNivel"
            if (mec.optInt("buffMaxNiveis", 0) != 5)
                faltando += "${magia.optString("id")}: maxNiveis deveria ser 5 (teto do livro)"
        }
        assertTrue("as 6 mágicas têm que existir no catálogo (achei $achadas)", achadas == 6)
        assertTrue("curadoria do P3-1 regrediu: $faltando", faltando.isEmpty())
    }

    /** Lote P3-1: Bloquear e Robustez são de Bloqueio — valem UM ataque só, não a duração toda. */
    @Test
    fun `Bloquear e Robustez sao marcadas como buff de um unico uso`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val faltando = mutableListOf<String>()
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            if (magia.optString("id") !in setOf("bloquear", "robustez")) continue
            val mec = magia.optJSONObject("mecanica")
            if (mec?.optBoolean("buffUmUnicoUso", false) != true)
                faltando += magia.optString("id")
        }
        assertTrue("sem buffUmUnicoUso viram RD/BD persistente — regra errada: $faltando",
            faltando.isEmpty())
    }
}
