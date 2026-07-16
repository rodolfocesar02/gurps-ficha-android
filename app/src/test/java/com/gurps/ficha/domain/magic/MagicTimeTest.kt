package com.gurps.ficha.domain.magic

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test
import java.io.File

/**
 * Lote MEC-5: os campos de tempo do catálogo são TEXTO LIVRE, e o parser antigo errava 325 das 879.
 * Cada teste usa o valor LITERAL que está no `magias2versao.json`, não um exemplo inventado.
 */
class MagicTimeTest {

    // ── Duração ─────────────────────────────────────────────────────────────────────────────────

    @Test fun `Perm abreviado e PERMANENTE — o catalogo escreve assim em 154 magias`() {
        // O bug: procurava "permanente" inteiro, "Perm." não casava e virava INSTANTANEA.
        assertEquals(TipoDuracao.PERMANENTE, MagicTime.parseDuracao("Perm.").first)
        assertEquals(TipoDuracao.PERMANENTE, MagicTime.parseDuracao("Perm.#").first)
        assertEquals(TipoDuracao.PERMANENTE, MagicTime.parseDuracao("Permanente").first)
    }

    @Test fun `Indef dura ate ser dissipada, nao expira no relogio`() {
        assertEquals(TipoDuracao.PERMANENTE, MagicTime.parseDuracao("Indef.").first)
        assertEquals(TipoDuracao.PERMANENTE, MagicTime.parseDuracao("Indef.#").first)
    }

    @Test fun `HORA e DIA nao podem virar SEGUNDO — 82 magias de 1 hora expiravam em 1 turno`() {
        assertEquals(TipoDuracao.TEMPORARIA to 3600, MagicTime.parseDuracao("1 hora"))
        assertEquals(TipoDuracao.TEMPORARIA to 3600, MagicTime.parseDuracao("1 hora#"))
        assertEquals(TipoDuracao.TEMPORARIA to 36000, MagicTime.parseDuracao("10 horas"))
        assertEquals(TipoDuracao.TEMPORARIA to 86400, MagicTime.parseDuracao("24 horas")) // 24 × 3600
        assertEquals(TipoDuracao.TEMPORARIA to 86400, MagicTime.parseDuracao("1 dia"))
        assertEquals(TipoDuracao.TEMPORARIA to 86400, MagicTime.parseDuracao("1 dia de marcha"))
    }

    @Test fun `minuto e segundo continuam certos (regressao)`() {
        assertEquals(TipoDuracao.TEMPORARIA to 60, MagicTime.parseDuracao("1 min."))
        assertEquals(TipoDuracao.TEMPORARIA to 600, MagicTime.parseDuracao("10 min."))
        assertEquals(TipoDuracao.TEMPORARIA to 10, MagicTime.parseDuracao("10 seg."))
        assertEquals(TipoDuracao.INSTANTANEA to 0, MagicTime.parseDuracao("Instant."))
        assertEquals(TipoDuracao.INSTANTANEA to 0, MagicTime.parseDuracao(null))
    }

    @Test fun `campo sem numero e sem palavra-chave cai em instantanea`() {
        assertEquals(TipoDuracao.INSTANTANEA to 0, MagicTime.parseDuracao("Varia"))
        assertEquals(TipoDuracao.INSTANTANEA to 0, MagicTime.parseDuracao("Especial"))
    }

    @Test fun `unidade SEM numero vale 1 — o catalogo escreve so Hora e Dia`() {
        // Sem isto não há dígito, cai em INSTANTANEA e a mágica não dura nada.
        assertEquals(TipoDuracao.TEMPORARIA to 3600, MagicTime.parseDuracao("Hora"))
        assertEquals(TipoDuracao.TEMPORARIA to 86400, MagicTime.parseDuracao("Dia#"))
        assertEquals(TipoDuracao.TEMPORARIA to 60, MagicTime.parseDuracao("Min."))
    }

    // ── Tempo de operação ───────────────────────────────────────────────────────────────────────

    @Test fun `tempo de operacao em hora nao vira 1 segundo`() {
        assertEquals(3600, MagicTime.parseTempoSeg("1 hora"))
        assertEquals(60, MagicTime.parseTempoSeg("1 min."))
        assertEquals(300, MagicTime.parseTempoSeg("5 min."))
        assertEquals(1, MagicTime.parseTempoSeg("1 seg."))
        assertEquals(10, MagicTime.parseTempoSeg("10 seg."))
        assertEquals(1, MagicTime.parseTempoSeg("—"))   // não informa → padrão 1s (Magia p.9)
        assertEquals(1, MagicTime.parseTempoSeg(null))
    }

    // ── Catálogo real ───────────────────────────────────────────────────────────────────────────

    private fun carregarCatalogo(): JSONArray? {
        val arquivo = listOf(
            "src/main/assets/magias2versao.json",
            "app/src/main/assets/magias2versao.json",
        ).map { File(it) }.firstOrNull { it.exists() } ?: return null
        return JSONArray(arquivo.readText(Charsets.UTF_8))
    }

    @Test
    fun `nenhuma magia do catalogo real perde a duracao em hora ou dia`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val quebradas = mutableListOf<String>()
        var comHoraOuDia = 0
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val bruto = magia.optString("duracao", "")
            val t = bruto.lowercase()
            val ehHora = "hora" in t; val ehDia = "dia" in t
            if (!ehHora && !ehDia) continue
            comHoraOuDia++
            val (tipo, seg) = MagicTime.parseDuracao(bruto)
            // Se tem número, a duração tem que ser pelo menos uma hora em segundos.
            val temNumero = Regex("""\d+""").containsMatchIn(t)
            if (temNumero && (tipo != TipoDuracao.TEMPORARIA || seg < 3600))
                quebradas += "${magia.optString("nome")}: '$bruto' → $tipo ${seg}s"
        }
        println("MEC-5: $comHoraOuDia mágicas com duração em hora/dia no catálogo real.")
        assertTrue("o catálogo precisa ter mágicas assim para o teste valer", comHoraOuDia > 50)
        assertTrue("Duração lida ERRADA em ${quebradas.size} magia(s): ${quebradas.take(5)}", quebradas.isEmpty())
    }

    // ── Lote MEC-5b: os números canônicos gravados no catálogo ──────────────────────────────────

    @Test
    fun `as magias auditadas tem numero canonico e ele bate com o livro`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        var comCanonico = 0
        val invalidas = mutableListOf<String>()
        for (i in 0 until catalogo!!.length()) {
            val m = catalogo.getJSONObject(i)
            val tipo = m.optString("duracaoTipo", "")
            if (tipo.isEmpty()) continue // sem canônico → cai no parser (240 mágicas, honesto)
            comCanonico++
            val nome = m.optString("nome")
            if (tipo !in listOf("instantanea", "temporaria", "permanente"))
                invalidas += "$nome: duracaoTipo '$tipo' fora do vocabulário"
            // Temporária TEM que ter segundos > 0, senão expiraria na hora.
            if (tipo == "temporaria" && m.optInt("duracaoSeg", 0) <= 0)
                invalidas += "$nome: temporária com duracaoSeg=${m.optInt("duracaoSeg", 0)}"
            // Instantânea/permanente não podem carregar duração.
            if (tipo != "temporaria" && m.optInt("duracaoSeg", 0) != 0)
                invalidas += "$nome: $tipo com duracaoSeg=${m.optInt("duracaoSeg", 0)}"
            // Manutenção não passa do custo de operar (Magia p.15) — EXCETO quando operar é 0, que é
            // legítimo: o catálogo tem "0/1" (lança de graça, custa para manter).
            val op = if (m.has("custoOperar")) m.optInt("custoOperar") else null
            val mt = if (m.has("custoManter")) m.optInt("custoManter") else null
            if (op != null && op > 0 && mt != null && mt > op)
                invalidas += "$nome: manter ($mt) > operar ($op)"
        }
        println("MEC-5b: $comCanonico mágicas com número canônico no catálogo real.")
        assertTrue("o merge do canônico precisa ter rodado", comCanonico > 500)
        assertTrue("Canônico INVÁLIDO em ${invalidas.size} magia(s): ${invalidas.take(5)}", invalidas.isEmpty())
    }

    @Test
    fun `o canonico corrigiu as transcricoes erradas que a auditoria achou`() {
        // Casos verificados À MÃO contra a descrição fiel do livro (o cabeçalho diverge nos três).
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val porNome = (0 until catalogo!!.length()).associate {
            val m = catalogo.getJSONObject(it); m.optString("nome") to m
        }
        // Arma Congelante: cabeçalho "03/01"; livro "Custo: 4 para operar: 1 para manter".
        porNome["Arma Congelante"]?.let {
            assertEquals(4, it.optInt("custoOperar"))
            assertEquals(1, it.optInt("custoManter"))
            assertEquals(2, it.optInt("tempoOperacaoSeg")) // cabeçalho dizia "3 seg."
        }
        // Analisar Mágica: cabeçalho "5"; livro "Custo: 8".
        porNome["Analisar Mágica"]?.let { assertEquals(8, it.optInt("custoOperar")) }
        // Subjugar: cabeçalho "4"; livro "6 para operar. 3 para manter".
        porNome["Subjugar"]?.let {
            assertEquals(6, it.optInt("custoOperar"))
            assertEquals(3, it.optInt("custoManter"))
        }
    }

    @Test
    fun `toda magia Perm do catalogo real e PERMANENTE, nunca instantanea`() {
        val catalogo = carregarCatalogo()
        Assume.assumeNotNull(catalogo)
        val quebradas = mutableListOf<String>()
        var comPerm = 0
        for (i in 0 until catalogo!!.length()) {
            val magia = catalogo.getJSONObject(i)
            val bruto = magia.optString("duracao", "")
            if (!bruto.lowercase().startsWith("perm")) continue
            comPerm++
            if (MagicTime.parseDuracao(bruto).first != TipoDuracao.PERMANENTE)
                quebradas += "${magia.optString("nome")}: '$bruto'"
        }
        println("MEC-5: $comPerm mágicas 'Perm.' no catálogo real.")
        assertTrue("o catálogo precisa ter mágicas 'Perm.' para o teste valer", comPerm > 100)
        assertTrue("'Perm.' lido errado em ${quebradas.size} magia(s): ${quebradas.take(5)}", quebradas.isEmpty())
    }
}
