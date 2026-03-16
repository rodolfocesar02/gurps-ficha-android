package com.gurps.ficha.data.network

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Cliente para comunicação com o AnythingLLM (Gemini).
 * Agora recebe os nomes reais do catálogo do App para injetar no prompt.
 */
object MestreIAClient {
    private const val CONNECT_TIMEOUT_MS = 15000
    private const val READ_TIMEOUT_MS = 90000  // Prompt maior = mais tempo de resposta
    private val gson = Gson()

    /**
     * Dados do catálogo oficial do App para injetar no prompt.
     */
    data class CatalogoNomes(
        val vantagens: List<String> = emptyList(),
        val desvantagens: List<String> = emptyList(),
        val pericias: List<String> = emptyList(),
        val magias: List<String> = emptyList()
    )

    fun perguntarAoMestre(
        baseUrl: String,
        apiKey: String,
        historia: String,
        catalogo: CatalogoNomes = CatalogoNomes()
    ): MestreIAResponse? {
        if (baseUrl.isBlank()) return null

        val endpoint = "${baseUrl.trimEnd('/')}/api/v1/workspace/meu-workspace/chat"
        
        // Monta as listas de nomes reais do catálogo (separados por vírgula)
        val listaVantagens = if (catalogo.vantagens.isNotEmpty()) 
            catalogo.vantagens.joinToString(", ") else "Reflexos de Combate, Tolerância à Dor, Aptidão Mágica, Senso de Perigo"
        val listaDesvantagens = if (catalogo.desvantagens.isNotEmpty()) 
            catalogo.desvantagens.joinToString(", ") else "Código de Honra, Fúria, Dever, Honestidade"
        val listaPericias = if (catalogo.pericias.isNotEmpty()) 
            catalogo.pericias.joinToString(", ") else "Espada Larga, Escudo, Tática, Liderança"
        val listaMagias = if (catalogo.magias.isNotEmpty()) 
            catalogo.magias.joinToString(", ") else "Bola de Fogo, Criar Fogo, Míssil Mágico"

        val promptCompleto = """
            Você é um Mestre de GURPS 4ª Edição especialista, usando a tradução oficial da Devir Livraria.
            Com base na história abaixo, gere uma FICHA COMPLETA em formato JSON.
            
            IMPORTANTE: Você DEVE usar APENAS os nomes que estão na lista abaixo. NÃO invente nomes.
            Se precisar de algo que não está na lista, escolha o mais próximo que estiver disponível.
            
            O JSON deve ter EXATAMENTE esta estrutura (inclua TODOS os campos):
            {
              "nome": "Nome do Personagem",
              "atributos": { "st": 10, "dx": 10, "iq": 10, "ht": 10 },
              "vantagens": ["nome exato da lista"],
              "desvantagens": ["nome exato da lista"],
              "pericias": [
                { "nome": "nome exato da lista", "nivel": 14 }
              ],
              "magias": ["nome exato da lista"],
              "qualidades": ["traço de personalidade positivo"],
              "peculiaridades": ["mania ou hábito particular"],
              "aparencia": "Descrição física detalhada",
              "historico": "Resumo da história em 2-3 frases"
            }
            
            REGRAS OBRIGATÓRIAS:
            1. Use APENAS nomes que estão nas listas abaixo. Copie o nome EXATAMENTE como está escrito.
            2. Os atributos devem ser coerentes com a história (guerreiros têm ST alta, magos IQ alta).
            3. Inclua pelo menos 4-6 vantagens e 3-5 desvantagens coerentes com o personagem.
            4. Inclua pelo menos 6-10 perícias relevantes para o conceito.
            5. Se o personagem for mago ou tiver poderes mágicos, inclua pelo menos 4-8 magias. Senão, deixe vazia [].
            6. Inclua 2-3 qualidades e 2-3 peculiaridades.
            7. Preencha aparência e histórico.
            8. Retorne APENAS o JSON, sem texto explicativo.
            
            === CATÁLOGO DE VANTAGENS DISPONÍVEIS ===
            $listaVantagens
            
            === CATÁLOGO DE DESVANTAGENS DISPONÍVEIS ===
            $listaDesvantagens
            
            === CATÁLOGO DE PERÍCIAS DISPONÍVEIS ===
            $listaPericias
            
            === CATÁLOGO DE MAGIAS DISPONÍVEIS ===
            $listaMagias
            
            História: $historia
        """.trimIndent()

        android.util.Log.d("MestreIA", "Tamanho do prompt: ${promptCompleto.length} chars")

        val payload = mapOf(
            "message" to promptCompleto,
            "mode" to "chat"
        )
        
        val body = gson.toJson(payload).toByteArray(StandardCharsets.UTF_8)
        var connection: HttpURLConnection? = null

        return try {
            android.util.Log.d("MestreIA", "Conectando em: $endpoint")
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
            }

            connection.outputStream.use { it.write(body) }

            val statusCode = connection.responseCode
            android.util.Log.d("MestreIA", "Status Code: $statusCode")
            
            if (statusCode in 200..299) {
                val rawBody = readStreamSafely(connection.inputStream)
                android.util.Log.d("MestreIA", "Resposta Raw: $rawBody")
                
                // O AnythingLLM retorna um JSON que contém o campo 'textResponse'
                val fullResponse = gson.fromJson(rawBody, Map::class.java)
                val aiText = fullResponse["textResponse"] as? String ?: rawBody
                
                extractJson(aiText)
            } else {
                val errorBody = readStreamSafely(connection.errorStream)
                android.util.Log.e("MestreIA", "Erro: $errorBody")
                null
            }
        } catch (error: Exception) {
            android.util.Log.e("MestreIA", "Exception: ${error.message}", error)
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun extractJson(rawResponse: String): MestreIAResponse? {
        return try {
            val jsonStart = rawResponse.indexOf("{")
            val jsonEnd = rawResponse.lastIndexOf("}") + 1
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonString = rawResponse.substring(jsonStart, jsonEnd)
                android.util.Log.d("MestreIA", "JSON Extraído: $jsonString")
                gson.fromJson(jsonString, MestreIAResponse::class.java)
            } else {
                android.util.Log.e("MestreIA", "JSON não encontrado no texto da IA")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("MestreIA", "Erro ao fazer parse do JSON: ${e.message}")
            null
        }
    }

    private fun readStreamSafely(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { it.readText() }
    }
}
