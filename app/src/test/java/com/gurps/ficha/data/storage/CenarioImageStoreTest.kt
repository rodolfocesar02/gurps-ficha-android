package com.gurps.ficha.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote TOK-3: funções puras do fundo de cena (chave de cache, prompt, guard do placeholder). */
class CenarioImageStoreTest {

    // ─── chaveCena ──────────────────────────────────────────────

    @Test
    fun `mesma cena com mesmo conteudo gera a mesma chave`() {
        val a = CenarioImageStore.chaveCena(1L, 5L, "O Coliseu de Ferro", "arena")
        val b = CenarioImageStore.chaveCena(1L, 5L, "O Coliseu de Ferro", "arena")
        assertEquals(a, b)
    }

    @Test
    fun `definir_cena mudando o titulo muda a chave (mesma cena id)`() {
        val antes = CenarioImageStore.chaveCena(1L, 5L, "Início", "")
        val depois = CenarioImageStore.chaveCena(1L, 5L, "O Coliseu de Ferro", "arena")
        assertNotEquals(antes, depois)
    }

    @Test
    fun `chave carrega campanha e cena para nao colidir entre campanhas`() {
        val c1 = CenarioImageStore.chaveCena(1L, 5L, "Taverna", "")
        val c2 = CenarioImageStore.chaveCena(2L, 5L, "Taverna", "")
        assertNotEquals(c1, c2)
        assertTrue(c1.startsWith("c1_s5_"))
        assertTrue(c2.startsWith("c2_s5_"))
    }

    @Test
    fun `bioma nulo equivale a vazio na chave`() {
        val a = CenarioImageStore.chaveCena(1L, 5L, "Taverna", null)
        val b = CenarioImageStore.chaveCena(1L, 5L, "Taverna", "")
        assertEquals(a, b)
    }

    @Test
    fun `HUMOR fica fora da chave — retoque de clima do Narrador NAO regenera o fundo pago`() {
        // Achado da revisão adversarial do TOK-3: humor é volátil ("tenso" → "alívio" na mesma
        // locação) e não muda o terreno. A chave só depende de campanha+cena+titulo+bioma —
        // este teste tranca o contrato de custo/idempotência.
        val chave = CenarioImageStore.chaveCena(1L, 5L, "O Coliseu de Ferro", "arena")
        assertTrue("chave não deve conter referência a humor", chave.startsWith("c1_s5_h"))
        // Mesma locação física = mesma chave, independente do clima narrado no momento.
        assertEquals(chave, CenarioImageStore.chaveCena(1L, 5L, "O Coliseu de Ferro", "arena"))
    }

    // ─── cenaValidaParaFundo ────────────────────────────────────

    @Test
    fun `placeholder Inicio nao gera fundo (com e sem acento)`() {
        assertFalse(CenarioImageStore.cenaValidaParaFundo("Início"))
        assertFalse(CenarioImageStore.cenaValidaParaFundo("Inicio"))
        assertFalse(CenarioImageStore.cenaValidaParaFundo("início"))
        assertFalse(CenarioImageStore.cenaValidaParaFundo(""))
        assertFalse(CenarioImageStore.cenaValidaParaFundo("   "))
    }

    @Test
    fun `cena estabelecida gera fundo`() {
        assertTrue(CenarioImageStore.cenaValidaParaFundo("O Coliseu de Ferro"))
    }

    // ─── promptFundoCena ────────────────────────────────────────

    @Test
    fun `prompt inclui titulo bioma e humor`() {
        val p = CenarioImageStore.promptFundoCena("O Coliseu de Ferro", "arena de areia", "tenso")
        assertTrue(p.contains("O Coliseu de Ferro"))
        assertTrue(p.contains("arena de areia"))
        assertTrue(p.contains("tenso"))
    }

    @Test
    fun `prompt sem bioma e humor omite as linhas`() {
        val p = CenarioImageStore.promptFundoCena("Taverna", null, null)
        assertTrue(p.contains("Taverna"))
        assertFalse(p.contains("Biome/terrain"))
        assertFalse(p.contains("Mood:"))
    }

    @Test
    fun `prompt proibe criaturas grid e texto (grade e desenhada por cima)`() {
        val p = CenarioImageStore.promptFundoCena("Taverna")
        assertTrue(p.contains("NO creatures"))
        assertTrue(p.contains("NO grid"))
        assertTrue(p.contains("NO text"))
    }

    @Test
    fun `prompt com titulo vazio usa localizacao generica`() {
        val p = CenarioImageStore.promptFundoCena("")
        assertTrue(p.contains("generic fantasy location"))
    }
}
