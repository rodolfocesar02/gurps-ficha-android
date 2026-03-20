package com.gurps.ficha.data.network

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Cliente para comunicação com o AnythingLLM (Gemini).
 * Agora recebe os nomes reais do catálogo do App para injetar no prompt.
 */
object MestreIAClient {
    private const val CONNECT_TIMEOUT_MS = 15000
    private const val READ_TIMEOUT_MS = 90000  // Prompt maior = mais tempo de resposta
    private val gson = Gson()

    /**
     * Resposta textual simples ou JSON de ficha.
     */
    data class ChatMessage(
        val role: String, // "user" ou "model" (ou "assistant")
        val text: String
    )

    fun perguntarAoMestre(
        baseUrl: String,
        apiKey: String,
        prompt: String,
        workspaceSlug: String = "meu-workspace",
        history: List<ChatMessage> = emptyList(),
        contextoPersonagem: String? = null,
        catalogo: CatalogoNomes = CatalogoNomes(),
        modo: String = "geracao" // "geracao", "conversa" ou "analise"
    ): String? {
        if (baseUrl.isBlank()) return null

        val endpoint = "${baseUrl.trimEnd('/')}/chat"
        
        // Catálogo de nomes para injeção
        val listaVantagens = if (catalogo.vantagens.isNotEmpty()) catalogo.vantagens.take(100).joinToString(", ") else ""
        val listaDesvantagens = if (catalogo.desvantagens.isNotEmpty()) catalogo.desvantagens.take(100).joinToString(", ") else ""
        val listaPericias = if (catalogo.pericias.isNotEmpty()) catalogo.pericias.take(100).joinToString(", ") else ""
        val listaMagias = if (catalogo.magias.isNotEmpty()) catalogo.magias.take(100).joinToString(", ") else ""

        val systemPrompt = when(modo) {
            "geracao" -> """
                Você é o 'Mestre Digital GURPS', um especialista em GURPS 4ª Edição (Devir). 
                Sua missão é gerar uma FICHA COMPLETA em JSON a partir de um conceito.
                
                REGRAS:
                1. Use APENAS nomes REAIS das listas abaixo. Não invente.
                2. Gere um conjunto REALISTA e ÚTIL de perícias (8-12), vantagens (4-6) e desvantagens (3-5).
                3. Atributos: Seja coerente (ex: ST 12+ para guerreiros, IQ 12+ para magos).
                4. Equipamento: Sugira 3-6 itens iniciais (nome, peso em kg, custo em $).
                5. Formato: JSON puro. Sem explicações.
                
                ESTRUTURA JSON EXIGIDA:
                {
                  "nome": "...", "atributos": {"st":10,"dx":10,"iq":10,"ht":10},
                  "vantagens": ["..."], "desvantagens": ["..."],
                  "pericias": [{"nome":"...", "nivel":12}], "magias": ["..."],
                  "equipamentos": [{"nome":"...", "peso":1.0, "custo":100, "quantidade":1}],
                  "aparencia": "...", "historico": "..."
                }
                
                Nomes reais liberados:
                Vantagens: $listaVantagens
                Desvantagens: $listaDesvantagens
                Perícias: $listaPericias
                Magias: $listaMagias
            """.trimIndent()
            
            "analise" -> """
                Você é um Mestre Consultor de GURPS 4ª Edição. 
                Analise a FICHA ATUAL abaixo e sugira melhorias mecânicas ou narrativas.
                Aponte inconsistências (ex: pericia de arma sem a arma, DX baixa para combatente).
                Seja encorajador e técnico.
                Ficha Atual: $contextoPersonagem
            """.trimIndent()
            
            else -> """
                Você é o 'Mestre Digital GURPS', um assistente prestativo para jogadores e mestre.
                Conhecimento: GURPS 4ª Edição (Módulo Básico: Personagens e Campanhas).
                Se for perguntado sobre regras, explique de forma simples citando páginas se possível.
                Se for perguntado sobre o personagem atual, use este contexto: $contextoPersonagem
            """.trimIndent()
        }

        var promptFinal = if (modo == "geracao") "$systemPrompt\n\nConceito: $prompt" else prompt
        
        if (history.isNotEmpty()) {
            val historyText = history.joinToString("\n") { "${it.role}: ${it.text}" }
            promptFinal = "$systemPrompt\n\nContexto anterior:\n$historyText\n\nUsuário: $prompt"
        }

        // Corpo da requisição no formato FormUrlEncoded (que o Python espera)
        return try {
            val postData = "text=" + java.net.URLEncoder.encode(promptFinal, "UTF-8")
            val body = postData.toByteArray(StandardCharsets.UTF_8)
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }
            
            connection.outputStream.use { it.write(body) }
            
            if (connection.responseCode in 200..299) {
                val rawBody = readStreamSafely(connection.inputStream)
                val responseMap = gson.fromJson(rawBody, Map::class.java)
                responseMap["response"] as? String ?: rawBody
            } else {
                val errorBody = readStreamSafely(connection.errorStream)
                "Erro do Servidor (${connection.responseCode}): $errorBody"
            }
        } catch (error: Exception) {
            "Erro de Conexão: ${error.localizedMessage}"
        }
    }

    // Helper para extração de JSON para a FichaViewModel usar
    fun extrairJsonFicha(texto: String): MestreIAResponse? {
        return try {
            val jsonStart = texto.indexOf("{")
            val jsonEnd = texto.lastIndexOf("}") + 1
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonString = texto.substring(jsonStart, jsonEnd)
                gson.fromJson(jsonString, MestreIAResponse::class.java)
            } else null
        } catch (e: Exception) { null }
    }

    private fun readStreamSafely(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { it.readText() }
    }

    data class CatalogoNomes(
        val vantagens: List<String> = emptyList(),
        val desvantagens: List<String> = emptyList(),
        val pericias: List<String> = emptyList(),
        val magias: List<String> = emptyList()
    )
}
