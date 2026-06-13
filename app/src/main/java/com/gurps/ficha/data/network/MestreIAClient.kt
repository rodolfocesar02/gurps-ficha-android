package com.gurps.ficha.data.network

import com.google.gson.Gson
import com.gurps.ficha.domain.saga.NarradorTools
import com.gurps.ficha.domain.tools.ForjadorTools
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
 * Cliente de Rede Híbrido (Lote 89.12 - Motor Sincronizado com Python)
 */
object MestreIAClient {
    private const val CONNECT_TIMEOUT_MS = 120000
    private const val READ_TIMEOUT_MS = 120000
    private val gson = Gson()

    // Lote 354: modos válidos que selecionam toolset. "saga" (Narrador) entrou aqui.
    // Os fluxos reais só passam estes; o require guarda contra modo digitado errado.
    private val MODOS_VALIDOS = setOf("conversa", "geracao", "analise", "planejamento", "saga")

    data class ChatMessage(
        val role: String,
        val text: String,
        val modelName: String? = null,
        val isRagUsed: Boolean = false,
        val latencyMs: Long = 0,
        val data: com.gurps.ficha.data.network.MestreIAResponse? = null,
        val rawJson: String? = null,
        // ID estável: sobrevive aos .copy() (status/chunk). Permite achar
        // a mensagem na lista viva sem depender de === (que quebrava: cada
        // .copy() cria nova referência) nem de índice fixo.
        val uid: String = java.util.UUID.randomUUID().toString()
    )

    data class MestreIAToolCall(
        val name: String,
        val args: JSONObject
    )

    data class ChatResponse(
        val text: String,
        val modelName: String? = null,
        val latencyMs: Long = 0,
        val toolCalls: List<MestreIAToolCall> = emptyList(),
        val promptTokens: Int = 0,
        val completionTokens: Int = 0,
        val totalTokens: Int = 0,
        val data: com.gurps.ficha.data.network.MestreIAResponse? = null,
        val rawJson: String? = null
    )

    data class CatalogoNomes(
        val vantagens: List<String> = emptyList(),
        val desvantagens: List<String> = emptyList(),
        val pericias: List<String> = emptyList(),
        val tecnicas: List<String> = emptyList(),
        val magias: List<String> = emptyList(),
        val equipamentos: List<String> = emptyList(),
        val armas: List<String> = emptyList(),
        val itensDetalhes: List<String> = emptyList(), // LOTE 89.25: Detalhes completos dos JSONs
        val chunks: List<MestreIAChunk> = emptyList(),
        val ponteDeFerro: String = ""
    )

    suspend fun perguntarAoMestre(
        baseUrl: String,
        apiKey: String,
        workspaceSlug: String,
        prompt: String,
        history: List<Pair<String, String>> = emptyList(),
        contextoPersonagem: String = "",
        catalogo: CatalogoNomes? = null,
        modo: String = "conversa",
        promptSistema: String? = null,
        onChunk: ((String) -> Unit)? = null,
        desativarTools: Boolean = false,
        maxTokens: Int = 2048,
        silencioso: Boolean = false,
        ativarThinking: Boolean = false
    ): ChatResponse = withContext(Dispatchers.IO) {
        require(modo in MODOS_VALIDOS) { "Modo de IA inválido: '$modo' (válidos: $MODOS_VALIDOS)" }
        val startTime = System.currentTimeMillis()
        try {
            val isGoogleNative = baseUrl.contains("generativelanguage.googleapis.com")
            
            val cleanBaseUrl = if (isGoogleNative && !baseUrl.contains("/v1beta")) {
                baseUrl.replace("/v1", "/v1beta").trimEnd('/')
            } else {
                baseUrl.trimEnd('/')
            }

            val urlStr = if (isGoogleNative) {
                // LOTE 89.50: Removido 'stream' e 'alt=sse' para receber JSON puro e estável
                "$cleanBaseUrl/models/$workspaceSlug:generateContent?key=$apiKey"
            } else {
                "$cleanBaseUrl/chat/completions"
            }

            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.setRequestProperty("Content-Type", "application/json")
            
            if (!isGoogleNative) {
                connection.setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                connection.setRequestProperty("X-Title", "GURPS Ficha Android")
            }

            // LOTE 90: Consolidação de Contexto. 
            // Agora usamos apenas a 'ponteDeFerro' que já vem formatada do GraphEngine,
            // evitando duplicar os mesmos dados (Summaries e Chunks) no prompt.

            val detalhesItens = if (catalogo?.itensDetalhes?.isNotEmpty() == true) {
                "\n=== DETALHES TÉCNICOS (CATÁLOGOS) ===\n" +
                catalogo.itensDetalhes.joinToString("\n")
            } else ""

            val ponteDeFerro = catalogo?.ponteDeFerro ?: ""
            val useStream = false // ALINHAMENTO PYTHON: Modo estático por padrão

            val fullPrompt = buildString {
                append("HISTÓRICO:\n")
                history.forEach { append("${it.first}: ${it.second}\n") }
                append("USUÁRIO: $prompt\n")
                append("\n=== CONTEXTO HIERÁRQUICO (GraphRAG) ===\n")
                append("- Ficha do Personagem: $contextoPersonagem\n")
                append(detalhesItens)
                append("\n")
                append("- Contexto Técnico: $ponteDeFerro")
            }

            if (!silencioso) android.util.Log.i("MestreIA_Prompt", """
                [CONTEÚDO DO PROMPT ENVIADO]
                - Pergunta: ${prompt.take(100)}...
                - Modelo Alvo: $workspaceSlug
                - Tamanho Total: ${fullPrompt.length} chars
                - Personagem: ${contextoPersonagem.length} chars
                - Ponte de Ferro: ${ponteDeFerro.length} chars
            """.trimIndent())

            val systemPulse = (promptSistema ?: when (modo) {
                "geracao", "analise" -> MestreIAPromptsForjador.PROMPT
                "planejamento" -> "Você é um assistente técnico de GURPS focado em extração de termos."
                else -> MestreIAPromptsAuditor.PROMPT
            }) + if (modo != "planejamento") {
                """
                
                === CONTEXTO HIERÁRQUICO (GraphRAG) ===
                - Ficha do Personagem: $contextoPersonagem
                $detalhesItens
                - Contexto Técnico: $ponteDeFerro
                """.trimIndent()
            } else ""

            val jsonOutput = if (isGoogleNative) {
                gerarJsonGoogleNative(prompt, history, systemPulse, modo, desativarTools)
            } else {
                gerarJsonOpenRouter(workspaceSlug, prompt, history, systemPulse, modo, useStream, desativarTools, maxTokens, ativarThinking)
            }

            if (!silencioso) android.util.Log.i("MestreIA_RAG", "║  REQUEST: ${jsonOutput.length}chars → $workspaceSlug")

            connection.outputStream.use { it.write(jsonOutput.toByteArray(StandardCharsets.UTF_8)) }

            val responseCode = connection.responseCode
            
            var fullText = ""
            val capturedToolCalls = mutableListOf<MestreIAToolCall>()

            if (responseCode !in 200..299) {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "Erro desconhecido"
                android.util.Log.e("MestreIA", "Erro $responseCode: $error")
                return@withContext ChatResponse("Erro $responseCode: $error")
            }

            if (!useStream) {
                val responseText = connection.inputStream.bufferedReader().readText()
                
                if (!silencioso) android.util.Log.i("MestreIA_RAG", "║  RESPONSE: ${responseText.length}chars (HTTP $responseCode)")
                
                val json = JSONObject(responseText)
                if (isGoogleNative) {
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null) {
                        for (j in 0 until candidates.length()) {
                            val candidate = candidates.getJSONObject(j)
                            val finishReason = candidate.optString("finishReason", "")
                            if (finishReason.isNotBlank() && finishReason != "STOP" && !silencioso) {
                                android.util.Log.w("MestreIA_Gemini", "finishReason=$finishReason (candidato $j)")
                            }
                            // MALFORMED_FUNCTION_CALL = Gemini tentou chamar tool mas corrompeu o JSON.
                            // Tratado como erro de API para acionar o fallback (DeepSeek).
                            if (finishReason == "MALFORMED_FUNCTION_CALL") {
                                return@withContext ChatResponse(
                                    text = "Erro de API: MALFORMED_FUNCTION_CALL — Gemini corrompeu a chamada de ferramenta. Tentando fallback.",
                                    toolCalls = emptyList()
                                )
                            }
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null) {
                                for (i in 0 until parts.length()) {
                                    val part = parts.getJSONObject(i)

                                    // Captura Texto
                                    val textPart = part.optString("text", "")
                                    if (textPart.isNotBlank()) {
                                        fullText += textPart
                                    }

                                    // Captura Tool Calls (Function Calls)
                                    val functionCall = part.optJSONObject("functionCall")
                                    if (functionCall != null) {
                                        capturedToolCalls.add(MestreIAToolCall(
                                            name = functionCall.getString("name"),
                                            args = functionCall.getJSONObject("args")
                                        ))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val choices = json.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        val choice = choices.getJSONObject(0)
                        val message = choice.optJSONObject("message")
                        fullText = message?.optString("content", "") ?: ""

                        // Thinking Mode (DeepSeek): raciocínio interno — logado para debug
                        val reasoning = message?.optString("reasoning_content", "") ?: ""
                        if (reasoning.isNotBlank() && !silencioso) {
                            android.util.Log.d("MestreIA_Thinking", "Raciocínio (${reasoning.length} chars): ${reasoning.take(500)}")
                        }

                        val toolCalls = message?.optJSONArray("tool_calls")
                        if (toolCalls != null) {
                            for (i in 0 until toolCalls.length()) {
                                val tc = toolCalls.getJSONObject(i)
                                val func = tc.getJSONObject("function")
                                capturedToolCalls.add(MestreIAToolCall(
                                    name = func.getString("name"),
                                    args = JSONObject(func.getString("arguments"))
                                ))
                            }
                        }
                    }
                }

                // --- CAPTURA DE TOKENS (LOTE 89.26) ---
                var pTokens = 0
                var cTokens = 0
                var tTokens = 0
                
                if (isGoogleNative) {
                    val usage = json.optJSONObject("usageMetadata")
                    pTokens = usage?.optInt("promptTokenCount") ?: 0
                    cTokens = usage?.optInt("candidatesTokenCount") ?: 0
                    tTokens = usage?.optInt("totalTokenCount") ?: 0
                } else {
                    val usage = json.optJSONObject("usage")
                    pTokens = usage?.optInt("prompt_tokens") ?: 0
                    cTokens = usage?.optInt("completion_tokens") ?: 0
                    tTokens = usage?.optInt("total_tokens") ?: 0

                    // Context Caching (DeepSeek): monitora economia de tokens
                    val cacheHit  = usage?.optInt("prompt_cache_hit_tokens", 0) ?: 0
                    val cacheMiss = usage?.optInt("prompt_cache_miss_tokens", 0) ?: 0
                    if ((cacheHit > 0 || cacheMiss > 0) && !silencioso) {
                        val economia = if (pTokens > 0) (cacheHit * 100 / pTokens) else 0
                        android.util.Log.i("MestreIA_Cache", "Cache hit=$cacheHit miss=$cacheMiss ($economia% do prompt em cache)")
                    }
                }

                val finalLatency = System.currentTimeMillis() - startTime
                if (!silencioso) android.util.Log.d("MestreIA_Tokens", """
                    [PAINEL DE CONSUMO]
                    Modelo: $workspaceSlug
                    Tokens Prompt: $pTokens
                    Tokens Resposta: $cTokens
                    Total: $tTokens
                    Latência: ${finalLatency}ms
                """.trimIndent())

                if ((fullText.trim().lowercase() == "null" || fullText.isBlank()) && capturedToolCalls.isEmpty()) {
                    ChatResponse("Erro: Resposta vazia ou inválida da API")
                } else {
                    ChatResponse(
                        text = fullText,
                        modelName = workspaceSlug,
                        latencyMs = finalLatency,
                        toolCalls = capturedToolCalls,
                        promptTokens = pTokens,
                        completionTokens = cTokens,
                        totalTokens = tTokens
                    )
                }
            } else {
                ChatResponse("Erro: Modo Stream não implementado nesta versão")
            }
        } catch (e: Exception) {
            ChatResponse("Erro de Conexão: ${e.message}")
        }
    }

    private fun gerarJsonGoogleNative(prompt: String, history: List<Pair<String, String>>, system: String, modo: String, desativarTools: Boolean = false): String {
        val root = JSONObject()
        root.put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
        
        val contents = JSONArray()
        
        // LOTE 90.1: Fusão de Mensagens e Blindagem de Protocolo
        val cleanPrompt = prompt.trim()
        val formattedHistory = mutableListOf<JSONObject>()
        
        history.forEach { (role, text) ->
            val cleanText = text.trim()
            if (cleanText.isBlank() || cleanText == "Pensando..." || cleanText == cleanPrompt) return@forEach
            
            val currentRole = if (role == "user") "user" else "model"
            
            // Se o último papel for igual ao atual, funde os textos (Exigência Gemini 3.1)
            if (formattedHistory.isNotEmpty() && formattedHistory.last().getString("role") == currentRole) {
                val lastObj = formattedHistory.last()
                val lastParts = lastObj.getJSONArray("parts")
                val lastText = lastParts.getJSONObject(0).getString("text")
                lastParts.put(0, JSONObject().put("text", "$lastText\n\n$cleanText"))
            } else {
                val contentObj = JSONObject()
                contentObj.put("role", currentRole)
                contentObj.put("parts", JSONArray().put(JSONObject().put("text", cleanText)))
                formattedHistory.add(contentObj)
            }
        }

        // Garante a alternância final: Se o último do histórico for 'user', o prompt atual vira 'model' (não pode)
        // Então removemos o último 'user' do histórico se ele for muito parecido com o prompt atual
        if (formattedHistory.isNotEmpty() && formattedHistory.last().getString("role") == "user") {
            formattedHistory.removeAt(formattedHistory.size - 1)
        }

        formattedHistory.forEach { contents.put(it) }

        // Adiciona a mensagem atual (Pergunta ou Resultado de Busca)
        contents.put(JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", cleanPrompt))))
        root.put("contents", contents)

        // geracao → tools do Forjador (cria ficha do zero)
        // analise → tools unificadas do Auditor (ForjadorTools + localizar/ler manual)
        // demais  → tools do Bibliotecário (localizar/ler, sem editar ficha)
        if (!desativarTools && modo != "planejamento") {
            when (modo) {
                "geracao" -> root.put("tools", ForjadorTools.getGeminiTools())
                "analise" -> root.put("tools", MestreIATools.getAuditorUnificadoToolsGemini())
                "saga"    -> root.put("tools", NarradorTools.getGeminiTools())
                else      -> root.put("tools", MestreIATools.getAuditorToolsGemini())
            }
        }

        root.put("generationConfig", JSONObject()
            .put("temperature", 0.1)
            .put("maxOutputTokens", 8192))
        return root.toString()
    }

    private fun gerarJsonOpenRouter(modelId: String, prompt: String, history: List<Pair<String, String>>, system: String, modo: String, stream: Boolean, desativarTools: Boolean = false, maxTokens: Int = 2048, ativarThinking: Boolean = false): String {
        val root = JSONObject()
        root.put("model", modelId)
        val messages = JSONArray()
        messages.put(JSONObject().put("role", "system").put("content", system))
        history.forEach { (role, text) ->
            messages.put(JSONObject().put("role", if (role == "user") "user" else "assistant").put("content", text))
        }
        messages.put(JSONObject().put("role", "user").put("content", prompt))
        root.put("messages", messages)

        // geracao → tools do Forjador (cria ficha do zero)
        // analise → tools unificadas do Auditor (ForjadorTools + localizar/ler manual)
        // demais  → tools do Bibliotecário (localizar/ler, sem editar ficha)
        if (!desativarTools && modo != "planejamento") {
            when (modo) {
                "geracao" -> root.put("tools", ForjadorTools.getOpenAITools())
                "analise" -> root.put("tools", MestreIATools.getAuditorUnificadoToolsOpenAI())
                "saga"    -> root.put("tools", NarradorTools.getOpenAITools())
                else      -> root.put("tools", MestreIATools.getAuditorToolsOpenAI())
            }
        }

        root.put("temperature", 0.1)
        root.put("max_tokens", maxTokens)
        root.put("stream", stream)

        // Thinking: ativado só quando solicitado explicitamente (iteração 1 do Forjador
        // para planejar a ficha). Nas demais iterações desativado — tool calling agêntico
        // não precisa de raciocínio interno e o thinking consumia 1000-5000 tokens por
        // iteração sem benefício real.
        if (modo == "geracao" || modo == "analise") {
            if (ativarThinking) {
                root.put("thinking", JSONObject().put("type", "enabled").put("budget_tokens", 2000))
            } else {
                root.put("thinking", JSONObject().put("type", "disabled"))
            }
        }

        return root.toString()
    }

    // Mantido para compatibilidade com a UI de parsing de fichas
    fun extrairJsonFicha(texto: String): com.gurps.ficha.data.network.MestreIAResponse? {
        return try {
            val firstBrace = texto.indexOf('{')
            if (firstBrace == -1) return null
            // Lote 351: recorta no '}' que FECHA o primeiro '{' (balanceamento de chaves,
            // ignorando chaves dentro de strings). Antes o corte ia até o fim do texto e
            // prosa após o JSON ("...} e ele é um elfo") derrubava o parse estrito do Gson.
            val jsonPart = extrairBlocoJsonBalanceado(texto, firstBrace) ?: texto.substring(firstBrace).trim()
            val finalJson = if (jsonPart.contains("```")) jsonPart.substringBeforeLast("```").trim() else jsonPart
            Gson().fromJson(finalJson, com.gurps.ficha.data.network.MestreIAResponse::class.java)
        } catch (e: Exception) { null }
    }

    private fun extrairBlocoJsonBalanceado(texto: String, inicio: Int): String? {
        var profundidade = 0
        var dentroDeString = false
        var i = inicio
        while (i < texto.length) {
            val c = texto[i]
            when {
                dentroDeString -> when (c) {
                    '\\' -> i++ // pula o caractere escapado
                    '"' -> dentroDeString = false
                }
                c == '"' -> dentroDeString = true
                c == '{' -> profundidade++
                c == '}' -> {
                    profundidade--
                    if (profundidade == 0) return texto.substring(inicio, i + 1)
                }
            }
            i++
        }
        return null // chaves desbalanceadas (ex.: JSON truncado) — caller usa o fallback antigo
    }

    // LOTE 89.65: HELPER PARA LOGS GIGANTES (Evita cortes do Android Logcat)
    private fun logLongString(tag: String, content: String) {
        val maxLogSize = 4000
        for (i in 0..content.length / maxLogSize) {
            val start = i * maxLogSize
            var end = (i + 1) * maxLogSize
            end = if (end > content.length) content.length else end
            android.util.Log.d(tag, content.substring(start, end))
        }
    }
}
