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
    private const val READ_TIMEOUT_MS = 90000 
    private val gson = Gson()

    private const val GOLD_TEMPLATE = """
{
  "nome": "Nome do Personagem",
  "historia": "Breve biografia narrativa (max 1000 chars)",
  "atributos": { "st": 10, "dx": 10, "iq": 10, "ht": 10, "hp": 10, "vontade": 10, "percepcao": 10, "fp": 10 },
  "vantagens": [ { "nome": "Nome Exato do Catálogo", "custo": 10, "descricao": "..." } ],
  "desvantagens": [ { "nome": "Nome Exato do Catálogo", "custo": -10, "descricao": "..." } ],
  "pericias": [ { "nome": "Nome Exato do Catálogo", "nivel": 12, "base": "DX", "pts": 4 } ],
  "magias": [ { "nome": "Nome Exato do Catálogo", "custo": "1 fp", "tempo": "1 s" } ],
  "equipamentos": [ { "nome": "Nome", "peso": 1.0, "custo": 100, "quantidade": 1, "rd": 0, "dano": "1d cut", "st_min": 10, "aparar": "0" } ]
}
"""

    data class ChatMessage(
        val role: String, // "user" ou "model"
        val text: String,
        val modelName: String? = null,
        val isRagUsed: Boolean = false,
        val latencyMs: Long = 0,
        val data: com.gurps.ficha.data.network.MestreIAResponse? = null,
        val rawJson: String? = null
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
        val rawJson: String? = null
    )

    data class CatalogoNomes(
        val vantagens: List<String> = emptyList(),
        val desvantagens: List<String> = emptyList(),
        val pericias: List<String> = emptyList(),
        val tecnicas: List<String> = emptyList(),
        val magias: List<String> = emptyList(),
        val chunks: List<com.gurps.ficha.model.MestreIAChunk> = emptyList(),
        val summaries: List<com.gurps.ficha.data.storage.GraphNodeEntity> = emptyList()
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
        modo: String = "conversa",
        onChunk: ((String) -> Unit)? = null
    ): ChatResponse = withContext(Dispatchers.IO) {
        try {
            val isGoogleNative = baseUrl.contains("generativelanguage.googleapis.com")
            
            val urlStr = if (isGoogleNative) {
                "$baseUrl/models/$workspaceSlug:streamGenerateContent?key=$apiKey&alt=sse"
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

            val resumosGrafo = if (catalogo?.summaries?.isNotEmpty() == true) {
                "\nCONHECIMENTO DO GRAFO (Contexto Macro e Relações):\n" +
                catalogo.summaries.joinToString("\n") { "Tópico: ${it.title} | Resumo: ${it.summary}" }
            } else ""

                val instrucaoModo = when(modo) {
                    "geracao" -> """
                        Você é o MESTRE CONSULTOR de GURPS 4E BR (Sistema Oficial).
                        REGRAS DE OURO DA GERAÇÃO:
                        1. FIDELIDADE ABSOLUTA: Use EXATAMENTE os nomes contidos no "CATÁLOGO LOCAL". Não invente nomes! 
                        2. PRÉ-REQUISITOS: Magias e perícias avançadas EXIGEM que você adicione os requisitos automaticamente.
                        
                        GABARITO DE OURO (Siga esta estrutura JSON estritamente):
                        $GOLD_TEMPLATE
                        
                        MÉTODO DE RESPOSTA:
                        - Escreva a introdução narrativa no chat.
                        - Insira o JSON INTEGRAL dentro de um bloco de código markdown (```json { ... } ```) no FIM da mensagem.
                        
                        CATÁLOGO LOCAL (Priorize estes nomes exatos para as automações funcionarem):
                        - Vantagens/Desvantagens: $listaVantagens, $listaDesvantagens
                        - Perícias: $listaPericias
                        - Técnicas: ${catalogo?.tecnicas?.joinToString(", ")}
                        - Magias: $listaMagias
                    """.trimIndent()
                else -> """
                    Siga estritamente estas diretrizes prioritárias:
                    1. RESPONDA 100% EM PORTUGUÊS (BRASIL). Use termos oficiais da Devir/Steve Jackson Games.
                    2. PRIORIDADE TÉCNICA (CODEX): Use as regras abaixo como FONTE ABSOLUTA.
                    3. CITAÇÃO OBRIGATÓRIA: Sempre cite o nome do manual e a página (ex: "Módulo Básico pág. 430").
                    4. Use [SUGESTAO: Texto] para sugerir perguntas.
                """.trimIndent()
            }

            val systemPulse = """
                Você é o Mestre Digital 2.0, assistente de GURPS 4ª Edição.
                $instrucaoModo
                
                $resumosGrafo
                
                $fragmentosRegras
                
                NARRATIVA E INTERATIVIDADE:
                1. Use nomes em português (Módulo Básico).
                2. LIMITE DE TEXTO: Mantenha as histórias na ficha em até 1000 caracteres.
                3. ANTI-VÁCUO: Em modo de geração de ficha, você DEVE sempre escrever uma pequena introdução narrativa no chat antes de abrir o bloco ```json.
                
                HISTÓRICO DO PERSONAGEM (Use como contexto):
                $contextoPersonagem
            """.trimIndent()

            val jsonOutput = if (isGoogleNative) {
                gerarJsonGoogleNative(prompt, history, systemPulse, modo)
            } else {
                gerarJsonOpenAI(workspaceSlug, prompt, history, systemPulse, modo)
            }

            connection.outputStream.use { it.write(jsonOutput.toByteArray(StandardCharsets.UTF_8)) }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                var fullText = ""
                var returnedModel = workspaceSlug
                val capturedToolCalls = mutableListOf<MestreIAToolCall>()
                
                // Variáveis para acumular Tool Calls do OpenAI (que chegam fragmentadas)
                var openAIToolName = ""
                var openAIToolArgs = ""
                
                BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)).use { reader ->
                    // Ambas as APIs agora usam Server-Sent Events (SSE) (Gemini via &alt=sse)
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line!!.startsWith("data: ")) {
                            val data = line!!.substring(6).trim()
                            if (data == "[DONE]") break
                            try {
                                if (isGoogleNative) {
                                    val parsed = parseGoogleStreamingChunk(data)
                                    val chunkText = parsed.first
                                    if (chunkText.isNotEmpty()) {
                                        fullText += chunkText
                                        onChunk?.invoke(chunkText)
                                    }
                                    if (parsed.second != null) {
                                        capturedToolCalls.add(parsed.second!!)
                                        val nomeAmigavel = if (parsed.second!!.name == MestreIATools.TOOL_FILL_SHEET) "Forjador de Fichas" else "Pesquisador de Regras"
                                        onChunk?.invoke("\n\n⚙️ *Iniciando $nomeAmigavel...*")
                                    }
                                } else {
                                    val json = JSONObject(data)
                                    val model = json.optString("model", "")
                                    if (model.isNotEmpty()) returnedModel = model
                                    
                                    val choices = json.optJSONArray("choices")
                                    if (choices != null && choices.length() > 0) {
                                        val delta = choices.getJSONObject(0).optJSONObject("delta")
                                        val content = delta?.optString("content", "") ?: ""
                                        if (content.isNotEmpty()) {
                                            fullText += content
                                            onChunk?.invoke(content)
                                        }
                                        val toolCallsArr = delta?.optJSONArray("tool_calls")
                                        if (toolCallsArr != null && toolCallsArr.length() > 0) {
                                            val tc = toolCallsArr.getJSONObject(0)
                                            val func = tc.optJSONObject("function")
                                            if (func != null) {
                                                val name = func.optString("name", "")
                                                if (name.isNotEmpty()) {
                                                    openAIToolName = name
                                                    val nomeAmigavel = if (name == MestreIATools.TOOL_FILL_SHEET) "Forjador de Fichas" else "Pesquisador de Regras"
                                                    onChunk?.invoke("\n\n⚙️ *Iniciando $nomeAmigavel...*")
                                                }
                                                openAIToolArgs += func.optString("arguments", "")
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // Pula linhas malformadas do stream
                            }
                        }
                    }
                }
                
                if (openAIToolName.isNotEmpty()) {
                    try {
                        capturedToolCalls.add(MestreIAToolCall(openAIToolName, JSONObject(openAIToolArgs)))
                    } catch (e: Exception) {
                        android.util.Log.e("MestreIA", "Erro ao parsear argumentos do tool do OpenAI: $openAIToolArgs")
                    }
                }
                
                ChatResponse(fullText, returnedModel, 0, capturedToolCalls)
            } else {
                val errorBody = readStreamSafely(connection.errorStream)
                ChatResponse("Erro de API ($responseCode): ${errorBody.take(150)}")
            }
        } catch (e: Exception) {
            ChatResponse("Erro de Conexão: ${e.message}")
        }
    }

    private fun gerarJsonGoogleNative(prompt: String, history: List<Pair<String, String>>, systemInstruction: String, modo: String): String {
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
        root.put("tools", MestreIATools.getGeminiTools(modo))
        root.put("generationConfig", JSONObject().put("temperature", 0.7).put("maxOutputTokens", 8192))
        
        return root.toString()
    }

    private fun gerarJsonOpenAI(model: String, prompt: String, history: List<Pair<String, String>>, systemPulse: String, modo: String): String {
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
        root.put("tools", MestreIATools.getOpenAITools(modo))
        root.put("temperature", 0.7)
        root.put("stream", true)
        
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
            val model = if (json.has("model")) json.getString("model") else null
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
            println("MestreIA - Erro ao parsear JSON: ${e.message}\nTexto: ${texto.take(100)}...")
            null
        }
    }

    private fun limparJsonPuro(texto: String): String {
        var limpo = texto.trim()
        
        // 1. Localização do Início do JSON
        val firstBrace = limpo.indexOf('{')
        if (firstBrace == -1) return ""
        limpo = limpo.substring(firstBrace)

        // 2. Remoção inteligente de blocos de comentários Markdown (se houver lixo após o JSON)
        if (limpo.contains("```")) {
            limpo = limpo.substringBeforeLast("```").trim()
        }

        // 3. Auto-Reparo de Truncamento e Comentários
        return repararJsonTruncado(limpo)
    }

    /**
     * Tenta salvar o JSON caso ele tenha sido cortado ou contenha comentários.
     * Agora fecha strings abertas e remove lixo pós-JSON.
     */
    private fun repararJsonTruncado(json: String): String {
        val stack = mutableListOf<Char>()
        var inString = false
        var escape = false
        var lastValidIndex = -1

        for (i in json.indices) {
            val c = json[i]
            if (c == '"' && !escape) inString = !inString
            if (inString) {
                escape = if (c == '\\') !escape else false
                continue
            }
            if (c == '{' || c == '[') {
                stack.add(c)
            } else if (c == '}') {
                if (stack.isNotEmpty() && stack.last() == '{') stack.removeAt(stack.size - 1)
                // Se a pilha esvaziou, achamos o fim do objeto raiz
                if (stack.isEmpty()) {
                    lastValidIndex = i
                    break
                }
            } else if (c == ']') {
                if (stack.isNotEmpty() && stack.last() == '[') stack.removeAt(stack.size - 1)
            }
        }

        // Se encontrou o fim natural do JSON, ignora tudo que vier depois
        var finalJson = if (lastValidIndex != -1) {
            json.substring(0, lastValidIndex + 1)
        } else {
            json
        }

        finalJson = finalJson.trim()

        // SE O JSON FOI TRUNCADO:
        if (lastValidIndex == -1) {
            // FECHAR STRING ABERTA
            if (inString) {
                finalJson += "\""
            }

            // REMOVER VÍRGULAS PENDENTES
            while (finalJson.endsWith(",") || finalJson.endsWith(" ")) {
                finalJson = finalJson.dropLast(1).trim()
            }

            // FECHAR PILHA NA ORDEM INVERSA
            while (stack.isNotEmpty()) {
                val opener = stack.removeAt(stack.size - 1)
                finalJson += if (opener == '{') "}" else "]"
            }
        }

        return finalJson.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]"), "").trim()
    }

    private fun parseGoogleStreamingChunk(data: String): Pair<String, MestreIAToolCall?> {
        return try {
            val json = JSONObject(data)
            val part = json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                
            val text = part.optString("text", "")
            val functionCall = part.optJSONObject("functionCall")
            
            val toolCall = if (functionCall != null) {
                MestreIAToolCall(
                    name = functionCall.getString("name"),
                    args = functionCall.getJSONObject("args")
                )
            } else null
            
            Pair(text, toolCall)
        } catch (e: Exception) { Pair("", null) }
    }

    private fun readStreamSafely(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { it.readText() }
    }
}
