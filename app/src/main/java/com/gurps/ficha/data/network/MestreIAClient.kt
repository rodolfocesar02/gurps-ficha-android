package com.gurps.ficha.data.network

import com.google.gson.Gson
import com.gurps.ficha.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cliente de Rede Híbrido (Lote 53 - Estabilizador PRIME)
 * Suporta protocolo Nativo Gemini (REST) e protocolo OpenAI (compatibilidade).
 */
object MestreIAClient {
    private const val CONNECT_TIMEOUT_MS = 30000
    private const val READ_TIMEOUT_MS = 60000 
    private val gson = Gson()

    data class ChatMessage(
        val role: String, // "user" ou "model"
        val text: String,
        val modelName: String? = null,
        val isRagUsed: Boolean = false,
        val latencyMs: Long = 0
    )

    data class ChatResponse(
        val text: String,
        val modelName: String? = null,
        val latencyMs: Long = 0
    )

    data class CatalogoNomes(
        val vantagens: List<String> = emptyList(),
        val desvantagens: List<String> = emptyList(),
        val pericias: List<String> = emptyList(),
        val tecnicas: List<String> = emptyList(),
        val magias: List<String> = emptyList(),
        val chunks: List<com.gurps.ficha.model.MestreIAChunk> = emptyList()
    ) {
        fun toJson(): String = Gson().toJson(this)
    }

    suspend fun perguntarAoMestre(
        baseUrl: String,
        apiKey: String,
        workspaceSlug: String,
        prompt: String,
        history: List<Pair<String, String>> = emptyList(),
        contextoPersonagem: String = "",
        catalogo: CatalogoNomes? = null,
        modo: String = "conversa"
    ): ChatResponse = withContext(Dispatchers.IO) {
        try {
            val isGoogleNative = baseUrl.contains("generativelanguage.googleapis.com")
            
            val urlStr = if (isGoogleNative) {
                "$baseUrl/models/$workspaceSlug:generateContent?key=$apiKey"
            } else {
                "$baseUrl/chat/completions"
            }.trim()

            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.setRequestProperty("Content-Type", "application/json")
            
            if (!isGoogleNative) {
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.setRequestProperty("HTTP-Referer", "https://github.com/rodolfocesar02/gurps-ficha-android")
                connection.setRequestProperty("X-Title", "Mestre IA GURPS App")
            }

            val listaVantagens = catalogo?.vantagens?.joinToString(", ") ?: ""
            val listaDesvantagens = catalogo?.desvantagens?.joinToString(", ") ?: ""
            val listaPericias = catalogo?.pericias?.joinToString(", ") ?: ""
            val listaMagias = catalogo?.magias?.joinToString(", ") ?: ""
            
            val fragmentosRegras = if (catalogo?.chunks?.isNotEmpty() == true) {
                "\nREGRAS DO CÓDEX (Siga estas regras à risca):\n" +
                catalogo.chunks.joinToString("\n") { "[${it.source_title} Pág. ${it.page_number}]: ${it.text}" }
            } else ""

                val instrucaoModo = when(modo) {
                    "geracao" -> """
                        Gere uma FICHA COMPLETA em JSON seguindo o modelo GURPS 4E BR.
                        Utilize APENAS os nomes contidos no CATÁLOGO LOCAL abaixo para garantir fidelidade técnica.
                        CAMPOS OBRIGATÓRIOS (Não omita nenhum):
                        - nome, historico, aparencia.
                        - atributos: { st, dx, iq, ht }
                        - vantagens: [ string ], desvantagens: [ string ]
                        - pericias: [ { nome, nivel } ]
                        - tecnicas: [ { nome, nivel } ] (Crucial para artes marciais)
                        - magias: [ string ] (se houver)
                        - qualidades: [ string ], peculiaridades: [ string ]
                        - equipamentos: [ { nome, peso, custo, quantidade } ]
                        NÃO escreva nada fora do JSON. Use apenas termos oficiais em Português-BR.
                        
                        CATÁLOGO LOCAL (Siga estes nomes):
                        - Vantagens/Desvantagens: $listaVantagens, $listaDesvantagens
                        - Perícias: $listaPericias
                        - Técnicas: ${catalogo?.tecnicas?.joinToString(", ")}
                        - Magias: $listaMagias
                    """.trimIndent()
                else -> """
                    Siga estritamente estas diretrizes:
                    1. RESPONDA 100% EM PORTUGUÊS (BRASIL).
                    2. PROIBIDO o uso de termos em inglês (ex: NUNCA escreva "Power Blow", use APENAS "Golpe Fulminante").
                    3. FIDELIDADE AO CODEX: Use APENAS as informações dos parágrafos do CODEX abaixo. Se o CODEX não mencionar a regra, admita que não encontrou nos manuais locais.
                    4. SEJA DIRETO E OBJETIVO (máximo 3 parágrafos). 
                    5. NUNCA exiba planos de escrita ou rascunhos.
                    6. NUNCA escreva referências bibliográficas ao final (o sistema inserirá o rodapé oficial automaticamente).
                    7. Use [SUGESTAO: Texto] para sugerir perguntas.
                """.trimIndent()
            }

            val systemPulse = """
                Você é o Mestre Digital 2.0, assistente de GURPS 4ª Edição.
                $instrucaoModo
                
                $fragmentosRegras
                
                REGRAS DE OURO:
                1. Use nomes em português (Módulo Básico: Personagens / Campanhas).
                2. NUNCA invente números de página. Use apenas os que o CODEX fornecer.
                3. Se o CODEX trouxer uma página irrelevante para a pergunta, ignore-a e diga que não encontrou a regra.
                4. LIMITE DE HISTÓRIA: O campo de história do personagem tem um limite técnico de 1000 caracteres. Se o jogador pedir para você escrever uma história longa ou se você atingir esse limite, informe amigavelmente que o espaço na ficha chegou ao fim e você não pode adicionar mais texto por segurança.
                
                HISTÓRICO DO PERSONAGEM:
                $contextoPersonagem
            """.trimIndent()

            val jsonOutput = if (isGoogleNative) {
                gerarJsonGoogleNative(prompt, history, systemPulse)
            } else {
                gerarJsonOpenAI(workspaceSlug, prompt, history, systemPulse)
            }

            connection.outputStream.use { it.write(jsonOutput.toByteArray(StandardCharsets.UTF_8)) }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseBody = readStreamSafely(connection.inputStream)
                var returnedModel = workspaceSlug
                val textoFinal = if (isGoogleNative) {
                    parseGoogleNativeResponse(responseBody)
                } else {
                    val (content, model) = parseOpenAIResponseWithModel(responseBody)
                    if (model != null) returnedModel = model
                    content
                }
                ChatResponse(textoFinal, returnedModel)
            } else {
                val errorBody = readStreamSafely(connection.errorStream)
                ChatResponse("Erro de API ($responseCode): ${errorBody.take(150)}")
            }
        } catch (e: Exception) {
            ChatResponse("Erro de Conexão: ${e.message}")
        }
    }

    private fun gerarJsonGoogleNative(prompt: String, history: List<Pair<String, String>>, systemInstruction: String): String {
        val root = JSONObject()
        
        // ASGURAR ORDEM: system_instruction deve vir antes conforme teste 200 OK do Python
        root.put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))

        val contents = JSONArray()
        // Histórico
        history.forEach { (role, text) ->
            contents.put(JSONObject().apply {
                put("role", if (role == "user") "user" else "model")
                put("parts", JSONArray().put(JSONObject().put("text", text)))
            })
        }
        // Pergunta atual
        contents.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", prompt)))
        })

        root.put("contents", contents)
        root.put("generationConfig", JSONObject().put("temperature", 0.7).put("maxOutputTokens", 2048))
        
        return root.toString()
    }

    private fun gerarJsonOpenAI(model: String, prompt: String, history: List<Pair<String, String>>, systemPulse: String): String {
        val root = JSONObject()
        root.put("model", model)
        val messages = JSONArray()
        
        // Mensagem de SISTEMA (Importante para modelos de elite como DeepSeek)
        messages.put(JSONObject().put("role", "system").put("content", systemPulse))

        history.forEach { (u, b) ->
            // Normalizar papéis (OpenAI aceita system, user, assistant)
            messages.put(JSONObject().put("role", "user").put("content", u))
            messages.put(JSONObject().put("role", "assistant").put("content", b))
        }
        messages.put(JSONObject().put("role", "user").put("content", prompt))
        
        root.put("messages", messages)
        root.put("temperature", 0.7)
        
        return root.toString()
    }

    private fun parseGoogleNativeResponse(body: String): String {
        return try {
            val json = JSONObject(body)
            json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) { "Falha no parse Nativo: $body" }
    }

    private fun parseOpenAIResponseWithModel(body: String): Pair<String, String?> {
        return try {
            val json = JSONObject(body)
            val model = json.optString("model", null)
            val content = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
            Pair(content, model)
        } catch (e: Exception) { Pair("Falha no parse OpenAI: $body", null) }
    }

    private fun parseOpenAIResponse(body: String): String {
        return parseOpenAIResponseWithModel(body).first
    }

    fun extrairJsonFicha(texto: String): com.gurps.ficha.data.network.MestreIAResponse? {
        return try {
            val jsonLimpo = limparJsonPuro(texto)
            if (jsonLimpo.isNotBlank()) {
                gson.fromJson(jsonLimpo, com.gurps.ficha.data.network.MestreIAResponse::class.java)
            } else null
        } catch (e: Exception) {
            android.util.Log.e("MestreIA", "Erro ao parsear JSON: ${e.message}\nTexto: ${texto.take(100)}...")
            null
        }
    }

    private fun limparJsonPuro(texto: String): String {
        var limpo = texto.trim()
        
        // 1. Remover blocos Markdown se existirem
        if (limpo.contains("```json")) {
            limpo = limpo.substringAfter("```json").substringBeforeLast("```")
        } else if (limpo.contains("```")) {
            limpo = limpo.substringAfter("```").substringBeforeLast("```")
        }
        
        // 2. Extrair apenas o conteúdo entre a primeira '{' e a última '}'
        val start = limpo.indexOf('{')
        val end = limpo.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            limpo = limpo.substring(start, end + 1)
        } else {
            return ""
        }

        // 3. Limpeza de caracteres de controle que podem invalidar o JSON
        // Mantém caracteres imprimíveis e quebras de linha comuns
        return limpo.filter { it.code == 10 || it.code == 13 || (it.code >= 32) }
    }

    private fun readStreamSafely(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { it.readText() }
    }
}
