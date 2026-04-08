package com.gurps.ficha.domain

import com.google.gson.JsonParser
import com.gurps.ficha.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MestreIAContextFilterTest {

    @Test
    fun testTruncamentoHistoriaModoConversa() {
        val longaHistoria = "A".repeat(500)
        val personagem = Personagem(nome = "Test", historico = longaHistoria)
        
        val contexto = MestreIAContextFilter.gerarContexto(personagem, "conversa")
        val json = JsonParser.parseString(contexto).asJsonObject
        
        val hist = json.get("historico_resumo").asString
        // 300 + prefixo do aviso
        assertTrue(hist.length > 300)
        assertTrue(hist.contains("[LIMITE DE CONTEXTO ATINGIDO]"))
    }

    @Test
    fun testTruncamentoHistoriaModoAnalise() {
        val longaHistoria = "A".repeat(1200)
        val personagem = Personagem(nome = "Test", historico = longaHistoria)
        
        val contexto = MestreIAContextFilter.gerarContexto(personagem, "analise")
        val json = JsonParser.parseString(contexto).asJsonObject
        
        val hist = json.get("historico_resumo").asString
        assertTrue(hist.length > 1000)
        assertTrue(hist.contains("[LIMITE DE CONTEXTO ATINGIDO]"))
    }

    @Test
    fun testInclusaoPontos() {
        val personagem = Personagem(nome = "Test", pontosIniciais = 200)
        personagem.forca = 12 // ST 12 custa 20 pontos de 10
        personagem.forcaBase = 10
        
        val contexto = MestreIAContextFilter.gerarContexto(personagem, "conversa")
        val json = JsonParser.parseString(contexto).asJsonObject
        
        assertTrue(json.has("pontosRestantes"))
        assertTrue(json.has("pontosGastos"))
        assertEquals(200, json.get("pontosIniciais").asInt)
        // GURPS ST 12 = 20 pts
        assertEquals(20, json.get("pontosGastos").asInt)
        assertEquals(180, json.get("pontosRestantes").asInt)
    }

    @Test
    fun testResumoListasModoConversa() {
        val p = Personagem(nome = "Legolas")
        p.vantagens = listOf(VantagemSelecionada(nome = "Visão Aguçada", nivel = 2))
        p.desvantagens = listOf(DesvantagemSelecionada(nome = "Excesso de Confiança", nivel = 1))
        
        val contexto = MestreIAContextFilter.gerarContexto(p, "conversa")
        val json = JsonParser.parseString(contexto).asJsonObject
        
        val vantagens = json.getAsJsonArray("vantagens")
        assertEquals(1, vantagens.size())
        assertEquals("Visão Aguçada", vantagens.get(0).asString)
        
        val desvantagens = json.getAsJsonArray("desvantagens")
        assertEquals(1, desvantagens.size())
        assertEquals("Excesso de Confiança", desvantagens.get(0).asString)
    }

    @Test
    fun testDetalhesListasModoGeracao() {
        val p = Personagem(nome = "Conan")
        p.vantagens = listOf(VantagemSelecionada(nome = "Força Extra", nivel = 1, custoBase = 10))
        
        val contexto = MestreIAContextFilter.gerarContexto(p, "geracao")
        val json = JsonParser.parseString(contexto).asJsonObject
        
        val vantagens = json.getAsJsonArray("vantagens")
        assertEquals(1, vantagens.size())
        val vObj = vantagens.get(0).asJsonObject
        assertTrue(vObj.has("nivel"))
        assertTrue(vObj.has("custo"))
        assertEquals("Força Extra", vObj.get("nome").asString)
    }
}
