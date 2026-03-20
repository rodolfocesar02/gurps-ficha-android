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

data class VttTokenBindResult(
    val playerId: String?,
    val tokenId: String?,
    val message: String
)

object VttTokenBindService {
    private val gson = Gson()

    private fun JsonObject.stringOrNull(key: String): String? {
        val element = get(key) ?: return null
        return if (element.isJsonNull) null else element.asString
    }

    private fun JsonObject.objectOrNull(key: String): JsonObject? {
        val element = get(key) ?: return null
        return if (element.isJsonNull || !element.isJsonObject) null else element.asJsonObject
    }

    suspend fun bindToken(
        roomKey: String,
        playerId: String,
        tokenId: String,
        baseUrl: String = BuildConfig.VTT_API_BASE_URL
    ): Result<VttTokenBindResult> = withContext(Dispatchers.IO) {
        runCatching {
            val root = baseUrl.trim().trimEnd('/')
            require(root.isNotBlank()) { "URL do VTT não configurada." }
            val endpoint = "$root/api/v1/token/bind"

            val envelope = JsonObject().apply {
                addProperty("contractVersion", "v1")
                addProperty("source", "ficha-app")
                add("payload", JsonObject().apply {
                    addProperty("roomKey", roomKey)
                    addProperty("playerId", playerId)
                    addProperty("tokenId", tokenId)
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
                        ?: "Falha HTTP $code ao vincular token."
                }

                if (code !in 200..299) {
                    val errorJson = runCatching { gson.fromJson(body, JsonObject::class.java) }.getOrNull()
                    val errorPayload = errorJson?.objectOrNull("payload")
                    val errorCode = errorPayload?.stringOrNull("errorCode").orEmpty()
                    val errorMessage = errorPayload?.stringOrNull("message")
                        ?: body.ifBlank { "Falha HTTP $code ao vincular token." }
                    val friendly = when (errorCode) {
                        "PLAYER_NOT_FOUND" -> "Player não encontrado na sala."
                        "INVALID_PARAMS" -> "Parâmetros inválidos para vincular token."
                        else -> errorMessage
                    }
                    error(friendly)
                }

                val rootJson = gson.fromJson(body, JsonObject::class.java)
                val payload = rootJson.objectOrNull("payload")
                val bound = payload?.objectOrNull("bound")
                val boundPlayerId = bound?.stringOrNull("playerId") ?: payload?.stringOrNull("playerId")
                val boundTokenId = bound?.stringOrNull("tokenId") ?: payload?.stringOrNull("tokenId")
                VttTokenBindResult(
                    playerId = boundPlayerId,
                    tokenId = boundTokenId,
                    message = payload?.stringOrNull("message") ?: "Token vinculado no VTT."
                )
            } finally {
                connection?.disconnect()
            }
        }.recoverCatching { err ->
            throw when (err) {
                is SocketTimeoutException -> IllegalStateException("Timeout ao vincular token.")
                is UnknownHostException -> IllegalStateException("Servidor VTT não encontrado.")
                else -> IllegalStateException(err.message ?: "Falha ao vincular token.")
            }
        }
    }
}
