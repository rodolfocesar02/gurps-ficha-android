package com.gurps.ficha.data.network

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * O pacote que a Mesa recebe.
 *
 * ⚠️ Bem mais simples que o do Discord, e de propósito: lá o bot monta a
 * mensagem a partir dos campos, então ele precisa de `dice`, `total`, `margin`.
 * Aqui **o app manda o texto já pronto** — a sala só o coloca no chat.
 *
 * 🔴 Isso significa que o mesmo lançamento pode sair escrito **diferente** nos
 * dois lugares. É o preço de não ter uma biblioteca comum entre um app Kotlin e
 * um servidor Node, e está registrado aqui para ninguém descobrir sozinho.
 */
data class MesaRollPayload(
    val token: String,
    val autor: String,
    val texto: String
)

/**
 * **A sala do PC do Mestre** — Lote MESA-7.
 *
 * Fala com o servidor `mesa-virtual`. O Discord continua existindo, no
 * [DiscordRollApiClient]; este é o **segundo** destino, não o substituto.
 */
object MesaApiClient {
    private const val CONNECT_TIMEOUT_MS = 5000
    private const val READ_TIMEOUT_MS = 5000
    private val gson = Gson()

    /**
     * Manda a rolagem para o chat da sala.
     *
     * ⚠️ O token vai no **corpo**, e não na URL: query string fica no histórico
     * do navegador e nos registros de qualquer intermediário, e este token é a
     * senha da sala inteira.
     */
    fun postRoll(baseUrl: String, payload: MesaRollPayload): DiscordRollSendResult {
        if (baseUrl.isBlank()) {
            return DiscordRollSendResult(ok = false, statusCode = null, error = "endereco_vazio")
        }
        if (payload.token.isBlank()) {
            return DiscordRollSendResult(ok = false, statusCode = null, error = "token_vazio")
        }

        val endpoint = "${baseUrl.trimEnd('/')}/api/rolagem"
        val body = gson.toJson(payload).toByteArray(StandardCharsets.UTF_8)
        var connection: HttpURLConnection? = null

        return try {
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            connection.outputStream.use { it.write(body) }

            val statusCode = connection.responseCode
            if (statusCode in 200..299) {
                DiscordRollSendResult(ok = true, statusCode = statusCode, error = null)
            } else {
                val erro = lerComSeguranca(connection.errorStream)
                DiscordRollSendResult(
                    ok = false,
                    statusCode = statusCode,
                    // 🔴 401 aqui quer dizer token errado, e é o erro que mais
                    // vai acontecer: o token muda a cada arranque do servidor.
                    // Dizer isso poupa a mesa de caçar problema de rede.
                    error = if (statusCode == 401) "token_da_sala_invalido"
                            else "http_$statusCode ${erro.ifBlank { "sem_detalhes" }}"
                )
            }
        } catch (error: Exception) {
            DiscordRollSendResult(
                ok = false,
                statusCode = null,
                error = error.message ?: "erro_desconhecido"
            )
        } finally {
            connection?.disconnect()
        }
    }

    /** A sala está de pé? Serve para o botão de testar, na configuração. */
    fun saude(baseUrl: String): DiscordRollSendResult {
        var connection: HttpURLConnection? = null
        return try {
            val endpoint = "${baseUrl.trimEnd('/')}/api/saude"
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
            }
            val codigo = connection.responseCode
            DiscordRollSendResult(
                ok = codigo in 200..299,
                statusCode = codigo,
                error = if (codigo in 200..299) null else "http_$codigo"
            )
        } catch (error: Exception) {
            DiscordRollSendResult(false, null, error.message ?: "nao_respondeu")
        } finally {
            connection?.disconnect()
        }
    }

    private fun lerComSeguranca(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return try {
            BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use {
                it.readText().trim().take(200)
            }
        } catch (_: Exception) {
            ""
        }
    }
}
