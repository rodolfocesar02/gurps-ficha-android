package com.gurps.ficha.data.network

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

data class DiscordRollPayload(
    val character: String,
    val testType: String,
    val context: String,
    val target: Int?,
    val modifier: Int,
    val dice: List<Int>,
    val total: Int,
    val outcome: String,
    val margin: Int?,
    val channelId: String? = null
)

data class DiscordRollSendResult(
    val ok: Boolean,
    val statusCode: Int?,
    val error: String?
)

data class DiscordVoiceChannel(
    val id: String,
    val name: String,
    val guildId: String,
    val guildName: String
)

data class DiscordChannelsFetchResult(
    val ok: Boolean,
    val channels: List<DiscordVoiceChannel>,
    val statusCode: Int?,
    val error: String?
)

data class FichaCloudPayload(
    val deviceId: String,
    val fichaJson: Any
)

data class FichaCloudResponse(
    val ok: Boolean,
    val ficha: Any? = null,
    val error: String? = null
)

data class DiscordFichasListResponse(
    val ok: Boolean,
    val fichas: List<String>? = null,
    val error: String? = null
)

object DiscordRollApiClient {
    private const val CONNECT_TIMEOUT_MS = 5000
    private const val READ_TIMEOUT_MS = 5000
    private val gson = Gson()

    fun postRoll(baseUrl: String, apiKey: String, payload: DiscordRollPayload): DiscordRollSendResult {
        if (baseUrl.isBlank()) {
            return DiscordRollSendResult(ok = false, statusCode = null, error = "base_url_vazia")
        }
        if (apiKey.isBlank()) {
            return DiscordRollSendResult(ok = false, statusCode = null, error = "api_key_vazia")
        }

        val endpoint = "${baseUrl.trimEnd('/')}/api/rolls"
        val body = gson.toJson(payload).toByteArray(StandardCharsets.UTF_8)
        var connection: HttpURLConnection? = null

        return try {
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("x-api-key", apiKey)
            }

            connection.outputStream.use { it.write(body) }

            val statusCode = connection.responseCode
            if (statusCode in 200..299) {
                DiscordRollSendResult(ok = true, statusCode = statusCode, error = null)
            } else {
                val errorBody = readStreamSafely(connection.errorStream)
                DiscordRollSendResult(
                    ok = false,
                    statusCode = statusCode,
                    error = "http_$statusCode ${errorBody.ifBlank { "sem_detalhes" }}"
                )
            }
        } catch (error: Exception) {
            DiscordRollSendResult(ok = false, statusCode = null, error = error.message ?: "erro_desconhecido")
        } finally {
            connection?.disconnect()
        }
    }

    fun fetchVoiceChannels(baseUrl: String, apiKey: String): DiscordChannelsFetchResult {
        if (baseUrl.isBlank()) {
            return DiscordChannelsFetchResult(ok = false, channels = emptyList(), statusCode = null, error = "base_url_vazia")
        }
        if (apiKey.isBlank()) {
            return DiscordChannelsFetchResult(ok = false, channels = emptyList(), statusCode = null, error = "api_key_vazia")
        }

        val endpoint = "${baseUrl.trimEnd('/')}/api/channels"
        var connection: HttpURLConnection? = null

        return try {
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("x-api-key", apiKey)
            }

            val statusCode = connection.responseCode
            val rawBody = if (statusCode in 200..299) {
                readStreamSafely(connection.inputStream)
            } else {
                readStreamSafely(connection.errorStream)
            }

            if (statusCode in 200..299) {
                val response = gson.fromJson(rawBody, ChannelsResponse::class.java)
                DiscordChannelsFetchResult(
                    ok = response?.ok == true,
                    channels = response?.channels ?: emptyList(),
                    statusCode = statusCode,
                    error = null
                )
            } else {
                DiscordChannelsFetchResult(
                    ok = false,
                    channels = emptyList(),
                    statusCode = statusCode,
                    error = "http_$statusCode ${rawBody.ifBlank { "sem_detalhes" }}"
                )
            }
        } catch (error: Exception) {
            DiscordChannelsFetchResult(
                ok = false,
                channels = emptyList(),
                statusCode = null,
                error = error.message ?: "erro_desconhecido"
            )
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Envia o retrato do personagem (data:image/...;base64,...) ao servidor,
     * que o guarda associado ao personagem e o anexa nos embeds de rolagem.
     * Feito UMA vez (ao salvar a ficha) para não inflar cada rolagem.
     */
    fun postPortrait(
        baseUrl: String,
        apiKey: String,
        characterName: String,
        imageDataUri: String
    ): Boolean {
        if (baseUrl.isBlank() || apiKey.isBlank()) return false
        if (characterName.isBlank() || imageDataUri.isBlank()) return false
        val endpoint = "${baseUrl.trimEnd('/')}/api/portrait"
        val body = gson.toJson(
            mapOf(
                "character" to characterName,
                "image" to imageDataUri
            )
        ).toByteArray(StandardCharsets.UTF_8)
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("x-api-key", apiKey)
            }
            connection.outputStream.use { it.write(body) }
            connection.responseCode in 200..299
        } catch (e: Exception) { false } finally { connection?.disconnect() }
    }

    fun postFicha(baseUrl: String, apiKey: String, deviceId: String, characterName: String, fichaJson: Any): Boolean {
        if (baseUrl.isBlank() || apiKey.isBlank()) return false
        val endpoint = "${baseUrl.trimEnd('/')}/api/fichas"
        val body = gson.toJson(mapOf(
            "deviceId" to deviceId, 
            "characterName" to characterName,
            "fichaJson" to fichaJson
        )).toByteArray(StandardCharsets.UTF_8)
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("x-api-key", apiKey)
            }
            connection.outputStream.use { it.write(body) }
            connection.responseCode in 200..299
        } catch (e: Exception) { false } finally { connection?.disconnect() }
    }

    fun fetchFichaList(baseUrl: String, apiKey: String, deviceId: String): List<String> {
        if (baseUrl.isBlank() || apiKey.isBlank()) return emptyList()
        val endpoint = "${baseUrl.trimEnd('/')}/api/fichas/$deviceId"
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("x-api-key", apiKey)
            }
            if (connection.responseCode in 200..299) {
                val rawBody = readStreamSafely(connection.inputStream)
                val response = gson.fromJson(rawBody, DiscordFichasListResponse::class.java)
                response?.fichas ?: emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() } finally { connection?.disconnect() }
    }

    fun fetchFicha(baseUrl: String, apiKey: String, deviceId: String, characterName: String): String? {
        if (baseUrl.isBlank() || apiKey.isBlank()) return null
        val safeName = characterName.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val endpoint = "${baseUrl.trimEnd('/')}/api/fichas/$deviceId/$safeName"
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("x-api-key", apiKey)
            }
            if (connection.responseCode in 200..299) {
                val rawBody = readStreamSafely(connection.inputStream)
                val response = gson.fromJson(rawBody, FichaCloudResponse::class.java)
                if (response?.ok == true && response.ficha != null) {
                    gson.toJson(response.ficha) 
                } else null
            } else null
        } catch (e: Exception) { null } finally { connection?.disconnect() }
    }

    private fun readStreamSafely(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return runCatching {
            BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                reader.readText()
            }
        }.getOrDefault("")
    }

    private data class ChannelsResponse(
        val ok: Boolean,
        val channels: List<DiscordVoiceChannel>?
    )
}
