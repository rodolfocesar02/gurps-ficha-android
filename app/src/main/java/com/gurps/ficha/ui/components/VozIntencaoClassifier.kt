package com.gurps.ficha.ui.components

import com.gurps.ficha.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object VozIntencaoClassifier {

    private val PROMPT_SISTEMA = """
Você é um classificador de intenção para um app de GURPS.
Dado um comando de voz do usuário, responda APENAS com uma das três palavras:
- DUVIDA: o usuário quer saber uma regra, tirar uma dúvida, fazer uma pergunta sobre GURPS
- ANALISE: o usuário quer modificar a ficha atual (adicionar, remover, alterar vantagem, desvantagem, perícia, equipamento, etc.)
- CRIAR: o usuário quer criar um personagem novo do zero

Responda SOMENTE a palavra, sem explicação.
""".trimIndent()

    suspend fun classificar(textoFalado: String): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("${BuildConfig.MESTRE_IA_LITE_1_URL}/models/${BuildConfig.MESTRE_IA_GEMINI_3_1_FLASH_LITE}:generateContent?key=${BuildConfig.MESTRE_IA_GEMINI_KEY}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("Content-Type", "application/json")

            val body = JSONObject().apply {
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", PROMPT_SISTEMA) })
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", textoFalado) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("maxOutputTokens", 10)
                    put("temperature", 0.0)
                })
            }

            connection.outputStream.use { it.write(body.toString().toByteArray()) }

            val resposta = connection.inputStream.bufferedReader().readText()
            val texto = JSONObject(resposta)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()
                .uppercase()

            android.util.Log.i("VozClassificador", "Texto: '$textoFalado' → Intenção: '$texto'")

            when {
                texto.contains("ANALISE") -> "analise"
                texto.contains("CRIAR")   -> "geracao"
                else                      -> "conversa"
            }
        } catch (e: Exception) {
            android.util.Log.w("VozClassificador", "Falha na classificação, usando 'conversa': ${e.message}")
            "conversa"
        }
    }
}
