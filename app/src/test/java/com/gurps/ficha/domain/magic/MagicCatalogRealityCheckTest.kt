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
}
