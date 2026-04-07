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
    )    fun perguntarAoMestre(
        baseUrl: String,
        apiKey: String,
        prompt: String,
        workspaceSlug: String = "gemini-1.5-flash", // Nome do modelo por padrão
        history: List<ChatMessage> = emptyList(),
        contextoPersonagem: String? = null,
        catalogo: CatalogoNomes = CatalogoNomes(),
        modo: String = "geracao"
    ): String? {
        if (baseUrl.isBlank()) return null

        // URL Universal ou específica do provedor
        val endpoint = if (baseUrl.endsWith("/chat/completions")) baseUrl 
                       else "${baseUrl.trimEnd('/')}/chat/completions"
        
        // Injeção de RAG Local no Prompt de Sistema
        val listaVantagens = catalogo.vantagens.joinToString(", ")
        val listaDesvantagens = catalogo.desvantagens.joinToString(", ")
        val listaPericias = catalogo.pericias.joinToString(", ")
        val listaMagias = catalogo.magias.joinToString(", ")

        val systemPrompt = when(modo) {
            "geracao" -> """
                Você é o 'Mestre Digital GURPS 4E'. Gere uma FICHA COMPLETA em JSON.
                USE APENAS ESTES NOMES REAIS NAS LISTAS:
                - Vantagens: $listaVantagens
                - Desvantagens: $listaDesvantagens
                - Perícias: $listaPericias
                - Magias: $listaMagias
                
                Se o catálogo estiver vazio, use nomes do Módulo Básico.
                Responda APENAS com o JSON.
            """.trimIndent()
            
            "analise" -> """
                Você é um 'Mestre Consultor' GURPS 4E. Analise a ficha e sugira melhorias.
                Contexto sugerido pelo RAG Local:
                - Vantagens: $listaVantagens
                - Perícias: $listaPericias
                
                Use [SUGESTAO: Texto] e [ACAO: TIPO:NOME VALOR].
                FICHA ATUAL: $contextoPersonagem
            """.trimIndent()
            
            else -> "Você é um assistente prestativo de GURPS 4E. Contexto: $contextoPersonagem"
        }

        // Construção do Payload Padrão (messages array)
        val messages = mutableListOf<Map<String, String>>()
        messages.add(mapOf("role" to "system", "content" to systemPrompt))
        
        history.forEach {
            messages.add(mapOf("role" to it.role, "content" to it.text))
        }
        
        messages.add(mapOf("role" to "user", "content" to prompt))

        val bodyMap = mapOf(
            "model" to workspaceSlug,
            "messages" to messages,
            "temperature" to 0.7,
            "stream" to false
        )

        return try {
            val body = gson.toJson(bodyMap).toByteArray(StandardCharsets.UTF_8)
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
            }
            
            connection.outputStream.use { it.write(body) }

            if (connection.responseCode in 200..299) {
                val rawBody = readStreamSafely(connection.inputStream)
                try {
                    val responseMap = gson.fromJson(rawBody, Map::class.java)
                    val choices = responseMap["choices"] as? List<*>
                    val firstChoice = choices?.firstOrNull() as? Map<*, *>
                    val message = firstChoice?.get("message") as? Map<*, *>
                    message?.get("content") as? String ?: rawBody
                } catch (e: Exception) {
                    rawBody
                }
            } else {
                val errorBody = readStreamSafely(connection.errorStream)
                "Erro (${connection.responseCode}): $errorBody"
            }
        } catch (error: Exception) {
            "Erro de Conexão: ${error.message}"
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
