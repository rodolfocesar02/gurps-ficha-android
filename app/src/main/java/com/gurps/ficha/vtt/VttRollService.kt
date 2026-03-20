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

data class VttRollRequest(
    val roomKey: String,
    val playerId: String,
    val tokenId: String,
    val tipoAcao: String,
    val nomeAcao: String,
    val modificador: Int,
    val alvoTokenId: String?
)

data class VttRollResult(
    val message: String,
    val requestId: String?
)

object VttRollService {
    private val gson = Gson()

    private fun JsonObject.stringOrNull(key: String): String? {
        val element = get(key) ?: return null
        return if (element.isJsonNull) null else element.asString
    }

    private fun JsonObject.objectOrNull(key: String): JsonObject? {
        val element = get(key) ?: return null
        return if (element.isJsonNull || !element.isJsonObject) null else element.asJsonObject
    }

    suspend fun sendRollRequest(
        request: VttRollRequest,
        baseUrl: String = BuildConfig.VTT_API_BASE_URL
    ): Result<VttRollResult> = withContext(Dispatchers.IO) {
        runCatching {
            val root = baseUrl.trim().trimEnd('/')
            require(root.isNotBlank()) { "URL do VTT nao configurada." }
            val endpoint = "$root/api/v1/roll/request"

            val envelope = JsonObject().apply {
                addProperty("contractVersion", "v1")
                addProperty("source", "ficha-app")
                add("payload", JsonObject().apply {
                    addProperty("roomKey", request.roomKey)
                    addProperty("playerId", request.playerId)
                    addProperty("tokenId", request.tokenId)
                    addProperty("tipoAcao", request.tipoAcao)
                    add("contexto", JsonObject().apply {
                        addProperty("nomeRolagem", request.nomeAcao)
                        addProperty("modificador", request.modificador)
                        if (!request.alvoTokenId.isNullOrBlank()) {
                            addProperty("alvoTokenId", request.alvoTokenId)
                        }
                    })
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
                        ?: "Falha HTTP $code ao enviar rolagem."
                }

                if (code !in 200..299) {
                    val errorJson = runCatching { gson.fromJson(body, JsonObject::class.java) }.getOrNull()
                    val errorPayload = errorJson?.objectOrNull("payload")
                    val errorCode = errorPayload?.stringOrNull("errorCode").orEmpty()
                    val errorMessage = errorPayload?.stringOrNull("message")
                        ?: body.ifBlank { "Falha HTTP $code ao enviar rolagem." }
                    val friendly = when (errorCode) {
                        "UNAUTHORIZED_TOKEN" -> "Token nao autorizado para este jogador."
                        else -> errorMessage
                    }
                    error(friendly)
                }

                val rootJson = gson.fromJson(body, JsonObject::class.java)
                val requestId = rootJson.stringOrNull("requestId")
                val payload = rootJson.objectOrNull("payload")
                val message = payload?.stringOrNull("message") ?: "Acao enviada ao VTT."
                VttRollResult(message = message, requestId = requestId)
            } finally {
                connection?.disconnect()
            }
        }.recoverCatching { err ->
            throw when (err) {
                is SocketTimeoutException -> IllegalStateException("Timeout ao enviar acao para o VTT.")
                is UnknownHostException -> IllegalStateException("Servidor VTT nao encontrado.")
                else -> IllegalStateException(err.message ?: "Falha ao enviar acao para o VTT.")
            }
        }
    }
}
