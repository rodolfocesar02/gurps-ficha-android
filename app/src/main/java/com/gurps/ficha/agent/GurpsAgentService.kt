package com.gurps.ficha.agent

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gurps.ficha.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

data class GurpsAgentSource(
    val sourceId: String,
    val sourceTitle: String,
    val pageNumber: Int,
    val score: Double
)

data class GurpsAgentAskResult(
    val answer: String,
    val confidence: String,
    val mode: String,
    val sources: List<GurpsAgentSource>
)

object GurpsAgentService {
    private val gson = Gson()

    suspend fun ask(
        question: String,
        mode: String = "regras",
        topK: Int = 6,
        baseUrl: String = BuildConfig.GURPS_AGENT_API_BASE_URL
    ): Result<GurpsAgentAskResult> = withContext(Dispatchers.IO) {
        runCatching {
            val root = baseUrl.trim().trimEnd('/')
            require(root.isNotBlank()) { "URL do assistente não configurada." }
            val endpoint = "$root/ask"
            var connection: HttpURLConnection? = null

            try {
                connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 20000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }

                val payload = JsonObject().apply {
                    addProperty("question", question)
                    addProperty("mode", mode)
                    addProperty("top_k", topK)
                }

                connection.outputStream.use { out ->
                    out.writer(Charsets.UTF_8).use { it.write(gson.toJson(payload)) }
                }

                val code = connection.responseCode
                val body = if (code in 200..299) {
                    connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        ?: "Falha HTTP $code ao consultar o assistente."
                }

                if (code !in 200..299) {
                    throw IllegalStateException(body.ifBlank { "Falha HTTP $code ao consultar o assistente." })
                }

                val json = gson.fromJson(body, JsonObject::class.java)
                val answer = json.get("answer")?.asString.orEmpty()
                val confidence = json.get("confidence")?.asString ?: "baixa"
                val modeOut = json.get("mode")?.asString ?: mode
                val sourcesJson = json.getAsJsonArray("sources")
                val sources = sourcesJson?.map { elem ->
                    val obj = elem.asJsonObject
                    GurpsAgentSource(
                        sourceId = obj.get("source_id")?.asString.orEmpty(),
                        sourceTitle = obj.get("source_title")?.asString.orEmpty(),
                        pageNumber = obj.get("page_number")?.asInt ?: 0,
                        score = obj.get("score")?.asDouble ?: 0.0
                    )
                } ?: emptyList()

                GurpsAgentAskResult(
                    answer = answer,
                    confidence = confidence,
                    mode = modeOut,
                    sources = sources
                )
            } finally {
                connection?.disconnect()
            }
        }.recoverCatching { err ->
            throw when (err) {
                is SocketTimeoutException -> IllegalStateException("Assistente demorou para responder. Tente novamente.")
                is UnknownHostException -> IllegalStateException("Não foi possível encontrar o servidor do assistente.")
                else -> IllegalStateException(err.message ?: "Falha ao consultar o assistente.")
            }
        }
    }
}
