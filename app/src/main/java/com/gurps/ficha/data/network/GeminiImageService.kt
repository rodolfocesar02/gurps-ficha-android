package com.gurps.ficha.data.network

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * ════════════════════════════════════════════════════════════════
 * MESTRE PINTOR — Geração de Retratos via Gemini Image API
 * ════════════════════════════════════════════════════════════════
 * Responsável por gerar retratos artísticos de personagens GURPS.
 *
 * MODELO ATIVO:   gemini-3.1-flash-image  (BuildConfig.MESTRE_IA_GEMINI_IMAGE_MODEL)
 * CHAVE:          mestre.ia.gemini1.key   (BuildConfig.MESTRE_IA_GEMINI_IMAGE_KEY — chave PAGA)
 * CUSTO:          ~$0,067 / imagem
 * PROPORÇÃO:      9:16 vertical (portrait ideal para fichas)
 * ENDPOINT:       POST /v1beta/models/{model}:generateContent
 * FORMATO:        responseModalities = ["IMAGE", "TEXT"]
 *
 * FLUXO DE CHAMADA:
 *   FichaIADelegate.gerarRetratoIA()
 *     → GeminiImageService.gerarRetrato()
 *     → ImagemPersonagemStore.salvarImagem()
 *     → FichaViewModel.atualizarImagemPersonagem()
 *
 * PONTOS DE ENTRADA NA UI:
 *   1. Automático: dialog pós-Forjador (DialogRetratoIA em FichaScreen.kt)
 *   2. Manual:     modo "pintor" no ChatInputBar (DialogsMestreIA.kt → ChatInputBar)
 *
 * OUTROS MESTRES (referência cruzada):
 *   Mestre Bibliotecário → MestreIAUseCase.kt          (modo "conversa" — dúvidas RAG)
 *   Mestre Auditor       → MestreIAGeneratorUseCase.kt  (modo "analise" — analisa ficha)
 *   Mestre Forjador      → MestreIAGeneratorUseCase.kt  (modo "geracao" — cria ficha)
 * ════════════════════════════════════════════════════════════════
 */
object GeminiImageService {

    private const val TAG = "GeminiImageService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    private const val TIMEOUT_MS = 90_000

    data class Resultado(
        val bytes: ByteArray,
        val mimeType: String
    )

    /**
     * Gera retrato 9:16 para o personagem com [nome], [aparencia] e [historia].
     * Retorna [Resultado] com os bytes da imagem, ou null em caso de falha.
     */
    suspend fun gerarRetrato(
        apiKey: String,
        modelId: String,
        nome: String,
        aparencia: String,
        historia: String
    ): Resultado? = withContext(Dispatchers.IO) {
        val prompt = buildPrompt(nome, aparencia, historia)
        val payload = buildPayload(prompt)

        val url = "$BASE_URL/$modelId:generateContent?key=$apiKey"
        Log.i(TAG, "Gerando retrato para '$nome' | model=$modelId")

        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                doOutput = true
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }

            val code = conn.responseCode
            if (code != 200) {
                val err = conn.errorStream?.bufferedReader()?.readText() ?: "sem corpo"
                Log.e(TAG, "HTTP $code: ${err.take(400)}")
                return@withContext null
            }

            val body = JSONObject(conn.inputStream.bufferedReader().readText())
            val parts = body
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?: JSONArray()

            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i) ?: continue
                val inline = part.optJSONObject("inlineData") ?: continue
                val b64 = inline.optString("data", "")
                val mime = inline.optString("mimeType", "image/png")
                if (b64.isNotBlank()) {
                    val bytes = Base64.decode(b64, Base64.DEFAULT)
                    Log.i(TAG, "Retrato gerado: ${bytes.size / 1024} KB | mime=$mime")
                    return@withContext Resultado(bytes, mime)
                }
            }

            Log.w(TAG, "Resposta sem imagem: ${body.toString().take(300)}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao gerar retrato: ${e.message}")
            null
        }
    }

    private fun buildPrompt(nome: String, aparencia: String, historia: String): String {
        val apDesc = aparencia.ifBlank { "personagem de RPG de fantasia" }
        val hiDesc = historia.ifBlank { "aventureiro misterioso" }
        return """Fantasy character portrait for a tabletop RPG character sheet.
Character name: $nome
Physical description: $apDesc
Background: $hiDesc

Style: detailed fantasy illustration, dramatic lighting, painterly style, high detail.
Composition: upper body portrait, centered, facing slightly to the side, 9:16 vertical format.
Do NOT include any text, watermarks, logos, or UI elements."""
    }

    private fun buildPayload(prompt: String): String = JSONObject().apply {
        put("contents", JSONArray().apply {
            put(JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", prompt) })
                })
            })
        })
        put("generationConfig", JSONObject().apply {
            put("responseModalities", JSONArray().apply {
                put("IMAGE")
                put("TEXT")
            })
        })
    }.toString()
}
