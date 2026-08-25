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
    /**
     * O texto já pronto.
     *
     * ⚠️ Continua indo, e de propósito: se a sala for de uma versão anterior,
     * ela ignora os campos abaixo e usa este. Sem ele, atualizar o app quebraria
     * as salas que ainda não foram atualizadas.
     */
    val texto: String,

    // 🔴 Os CAMPOS, para a sala montar a frase com as regras de crítico.
    //
    // O app montava o texto sozinho, sem as regras, e por isso um SUCESSO
    // DECISIVO chegava na Mesa como "sucesso" comum — enquanto no Discord vinha
    // certo, porque lá quem monta é o bot.
    val tipo: String? = null,
    val contexto: String? = null,
    val dados: List<Int>? = null,
    val total: Int? = null,
    val resultado: String? = null,
    val margem: Int? = null,
    val alvo: Int? = null
)

/**
 * **A sala do PC do Mestre** — Lote MESA-7.
 *
 * Fala com o servidor `mesa-virtual`. O Discord continua existindo, no
 * [DiscordRollApiClient]; este é o **segundo** destino, não o substituto.
 */
object MesaApiClient {
    /**
     * **O endereco da mesa** -- lote MESA-44.
     *
     * 🔴 Fixo aqui, e fora da tela. Antes era um campo que toda a gente tinha de
     * digitar, e ele nunca muda: e sempre a mesa do Rodolfo.
     *
     * ⚠️ O preco esta escrito: se este endereco mudar -- outro dominio, outro
     * servico de DNS dinamico -- **e preciso um app novo** para todo o mundo,
     * inclusive para quem so tem o APK instalado. Foi decisao consciente, tomada
     * em 25/ago/2026, contra a alternativa de deixar o campo escondido atras de
     * um "outra mesa?".
     */
    const val ENDERECO_PADRAO = "https://mesagurps.duckdns.org"

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

        // 🔴 401 aqui quer dizer token errado, e é o erro que mais vai
        // acontecer: o token muda a cada arranque do servidor. Quem traduz isso
        // é o `enviar`, uma vez, para as três rotas.
        val body = gson.toJson(payload).toByteArray(StandardCharsets.UTF_8)
        return enviar("${baseUrl.trimEnd('/')}/api/rolagem", body)
    }

    /**
     * **Vincula a ficha ao token da pessoa na Mesa** -- lote CAMPO-17.
     *
     * 🔴 Manda o bloco `calculado` (CAMPO-16), e nao a ficha crua. A Mesa
     * precisa da Velocidade Basica para ordenar a iniciativa e da Esquiva para
     * mostrar ao lado do boneco -- numeros que o JSON cru **nao tem**, porque os
     * derivados sao propriedades e o Gson serializa campos.
     *
     * ⚠️ Best-effort, como o retrato: **nunca segura o salvar da ficha**. Ficar
     * sem ficha na Mesa e um aborrecimento; nao salvar e perder trabalho.
     *
     * ⚠️ O token vai no **corpo**, e nao na URL, pelo motivo de sempre: query
     * string fica no historico e nos registros de qualquer intermediario.
     */
    fun postFicha(
        baseUrl: String,
        token: String,
        autor: String,
        ficha: com.gurps.ficha.model.FichaCalculada
    ): DiscordRollSendResult {
        if (baseUrl.isBlank()) return DiscordRollSendResult(false, null, "endereco_vazio")
        if (token.isBlank()) return DiscordRollSendResult(false, null, "token_vazio")
        if (autor.isBlank()) return DiscordRollSendResult(false, null, "sem_autor")

        val corpo = gson.toJson(
            mapOf("token" to token, "autor" to autor, "ficha" to ficha)
        ).toByteArray(StandardCharsets.UTF_8)

        return enviar("${baseUrl.trimEnd('/')}/api/ficha", corpo)
    }

    /**
     * O POST em si, que era copiado em cada rota.
     *
     * ⚠️ Extraido no CAMPO-17: eram tres copias da mesma abertura de conexao,
     * do mesmo tratamento de 401 e do mesmo `finally`. A quarta copia ia nascer
     * diferente das outras -- que e como comeca o defeito numero um deste projeto.
     */
    private fun enviar(
        endpoint: String,
        corpo: ByteArray,
        // ⚠️ A folga de leitura e parametro porque o retrato precisa de MAIS:
        // a imagem e bem maior que uma linha de texto, e um telefone em 3G leva
        // tempo. Fixar o valor aqui teria feito o retrato voltar a falhar em rede
        // lenta, calado, so por causa da extracao.
        folgaDeLeitura: Int = READ_TIMEOUT_MS
    ): DiscordRollSendResult {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = folgaDeLeitura
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            connection.outputStream.use { it.write(corpo) }
            val statusCode = connection.responseCode
            if (statusCode in 200..299) {
                DiscordRollSendResult(ok = true, statusCode = statusCode, error = null)
            } else {
                val erro = lerComSeguranca(connection.errorStream)
                DiscordRollSendResult(
                    ok = false,
                    statusCode = statusCode,
                    error = if (statusCode == 401) "token_da_sala_invalido"
                            else "http_$statusCode ${erro.ifBlank { "sem_detalhes" }}"
                )
            }
        } catch (error: Exception) {
            DiscordRollSendResult(false, null, error.message ?: "erro_desconhecido")
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Sobe o retrato do PERSONAGEM para a sala.
     *
     * 🔴 Espelha o que já se fazia com o Discord: a imagem sobe uma vez, ao
     * salvar a ficha, e a sala a reaproveita em cada rolagem daquele
     * personagem. Sem isto a rolagem chegava na Mesa como texto pelado,
     * enquanto no Discord vinha com a cara do personagem ao lado.
     *
     * ⚠️ O token vai no **corpo**, como na rolagem, e pela mesma razão: query
     * string fica no histórico e nos registros de qualquer intermediário.
     */
    fun postRetrato(
        baseUrl: String,
        token: String,
        personagem: String,
        imagemDataUri: String
    ): DiscordRollSendResult {
        if (baseUrl.isBlank()) {
            return DiscordRollSendResult(false, null, "endereco_vazio")
        }
        if (token.isBlank()) return DiscordRollSendResult(false, null, "token_vazio")
        if (personagem.isBlank() || imagemDataUri.isBlank()) {
            return DiscordRollSendResult(false, null, "sem_imagem")
        }

        val corpo = gson.toJson(
            mapOf("token" to token, "personagem" to personagem, "imagem" to imagemDataUri)
        ).toByteArray(StandardCharsets.UTF_8)

        // ⚠️ Mais folga na leitura que a rolagem: a imagem é bem maior que uma
        // linha de texto, e um telefone em 3G leva tempo.
        return enviar(
            "${baseUrl.trimEnd('/')}/api/retrato", corpo,
            folgaDeLeitura = READ_TIMEOUT_MS * 4
        )
    }

    /** A sala está de pé? Serve para o botão de testar, na configuração. */
    fun saude(baseUrl: String, token: String? = null): DiscordRollSendResult {
        var connection: HttpURLConnection? = null
        return try {
            val endpoint = "${baseUrl.trimEnd('/')}/api/saude"
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                // 🔴 Com token, a sala confere se ele vale -- MESA-44. Sem esta
                // conferencia o botao "CONECTAR A MESA" dizia "a sala respondeu" com
                // o token errado, abria o navegador, e a pessoa caia na tela de
                // entrada sem saber por que -- culpando o navegador, e nao o token.
                //
                // ⚠️ No CABECALHO, e nunca na URL: query string vai para os
                // registros de qualquer intermediario, e este token e a senha da sala.
                if (!token.isNullOrBlank()) setRequestProperty("X-Token", token)
            }
            val codigo = connection.responseCode
            DiscordRollSendResult(
                ok = codigo in 200..299,
                statusCode = codigo,
                error = when {
                    codigo in 200..299 -> null
                    // 🔴 401 aqui e o erro que mais vai acontecer, e agora ele tem um
                    // nome proprio: dizer "token errado" poupa a mesa de cacar
                    // problema de rede que nao existe.
                    codigo == 401 -> "token_da_sala_invalido"
                    else -> "http_$codigo"
                }
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
