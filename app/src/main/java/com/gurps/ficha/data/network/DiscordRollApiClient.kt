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
