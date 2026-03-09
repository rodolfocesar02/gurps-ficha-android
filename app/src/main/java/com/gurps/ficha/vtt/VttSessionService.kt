package com.gurps.ficha.vtt

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gurps.ficha.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

data class VttJoinSessionResult(
    val sessionId: String?,
    val tokenId: String?,
    val needsBind: Boolean,
    val message: String
)

object VttSessionService {
    private val gson = Gson()

    private fun JsonObject.stringOrNull(key: String): String? {
        val element = get(key) ?: return null
        return if (element.isJsonNull) null else element.asString
    }

    private fun JsonObject.boolOrDefault(key: String, defaultValue: Boolean = false): Boolean {
        val element = get(key) ?: return defaultValue
        return if (element.isJsonNull) defaultValue else element.asBoolean
    }

    private fun JsonObject.objectOrNull(key: String): JsonObject? {
        val element = get(key) ?: return null
        return if (element.isJsonNull || !element.isJsonObject) null else element.asJsonObject
    }

    suspend fun joinSession(
        roomKey: String,
        playerId: String,
        fichaJsonRaw: String,
        previousSessionId: String? = null,
        previousTokenId: String? = null,
        baseUrl: String = BuildConfig.VTT_API_BASE_URL
    ): Result<VttJoinSessionResult> = withContext(Dispatchers.IO) {
        runCatching {
            val root = baseUrl.trim().trimEnd('/')
            require(root.isNotBlank()) { "URL do VTT não configurada." }
            val endpoint = "$root/api/v1/session/join"

            val envelope = JsonObject().apply {
                addProperty("contractVersion", "v1")
                addProperty("source", "ficha-app")
                add("payload", JsonObject().apply {
                    addProperty("roomKey", roomKey)
                    addProperty("playerId", playerId)
                    addProperty("playerName", playerId)
                    if (!previousSessionId.isNullOrBlank()) addProperty("sessionId", previousSessionId)
                    if (!previousTokenId.isNullOrBlank()) addProperty("tokenId", previousTokenId)
                    add("fichaJson", gson.fromJson(fichaJsonRaw, JsonObject::class.java))
                })
            }

            var connection: HttpURLConnection? = null
            try {
                connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 12_000
                    readTimeout = 15_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }

                connection.outputStream.use { out ->
                    out.writer(Charsets.UTF_8).use { it.write(gson.toJson(envelope)) }
                }

                val code = connection.responseCode
                val body = if (code in 200..299) {
                    connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                } else {
                    connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        ?: "Falha HTTP $code no join de sessão."
                }

                if (code !in 200..299) {
                    val errorJson = runCatching { gson.fromJson(body, JsonObject::class.java) }.getOrNull()
                    val errorPayload = errorJson?.objectOrNull("payload")
                    val errorCode = errorPayload?.stringOrNull("errorCode").orEmpty()
                    val errorMessage = errorPayload?.stringOrNull("message")
                        ?: body.ifBlank { "Falha HTTP $code no join de sessão." }
                    val friendly = when (errorCode) {
                        "SESSION_EXPIRED" -> "Sessão expirada no VTT. Faça reconexão."
                        "UNAUTHORIZED_TOKEN" -> "Token não autorizado para este jogador. Refaça o vínculo."
                        else -> errorMessage
                    }
                    error(friendly)
                }

                val rootJson = gson.fromJson(body, JsonObject::class.java)
                val payload = rootJson.objectOrNull("payload") ?: JsonObject()
                VttJoinSessionResult(
                    sessionId = payload.stringOrNull("sessionId"),
                    tokenId = payload.stringOrNull("tokenId")
                        ?: payload.stringOrNull("yourTokenId"),
                    needsBind = payload.boolOrDefault("needsBind", false),
                    message = payload.stringOrNull("message") ?: "Sessão VTT validada."
                )
            } finally {
                connection?.disconnect()
            }
        }.recoverCatching { err ->
            throw when (err) {
                is SocketTimeoutException -> IllegalStateException("Timeout ao conectar no VTT.")
                is UnknownHostException -> IllegalStateException("Servidor VTT não encontrado.")
                else -> IllegalStateException(err.message ?: "Falha ao iniciar sessão VTT.")
            }
        }
    }
}
