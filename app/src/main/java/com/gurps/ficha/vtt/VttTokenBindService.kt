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
                    val errorPayload = errorJson?.getAsJsonObject("payload")
                    val errorCode = errorPayload?.get("errorCode")?.asString.orEmpty()
                    val errorMessage = errorPayload?.get("message")?.asString
                        ?: body.ifBlank { "Falha HTTP $code ao vincular token." }
                    val friendly = when (errorCode) {
                        "PLAYER_NOT_FOUND" -> "Player não encontrado na sala."
                        "INVALID_PARAMS" -> "Parâmetros inválidos para vincular token."
                        else -> errorMessage
                    }
                    error(friendly)
                }

                val rootJson = gson.fromJson(body, JsonObject::class.java)
                val payload = rootJson.getAsJsonObject("payload")
                val bound = payload?.getAsJsonObject("bound")
                val boundPlayerId = bound?.get("playerId")?.asString ?: payload?.get("playerId")?.asString
                val boundTokenId = bound?.get("tokenId")?.asString ?: payload?.get("tokenId")?.asString
                VttTokenBindResult(
                    playerId = boundPlayerId,
                    tokenId = boundTokenId,
                    message = payload?.get("message")?.asString ?: "Token vinculado no VTT."
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
