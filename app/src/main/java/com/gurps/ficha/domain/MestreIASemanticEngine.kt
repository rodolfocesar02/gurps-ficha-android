package com.gurps.ficha.domain

import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.storage.VecChunkEntity
import com.gurps.ficha.model.MestreIAChunk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit

/**
 * Lote 259: Motor de busca semântica híbrida.
 *
 * Arquitetura:
 *   1. FTS4 retorna pool de candidatos (keyword match)
 *   2. Embedding da query via API Gemini Flash Lite
 *   3. Similaridade cosseno em Kotlin sobre embeddings pré-computados (offline)
 *   4. Reranking: score_final = 0.6×BM25_norm + 0.4×cosseno
 *
 * Embeddings dos chunks: gerados offline pelo script Python gerar_embeddings.py
 * e importados via chunks.jsonl (campo "embedding": [...384 floats...]).
 * Armazenados na tabela vec_chunks como ByteArray little-endian.
 *
 * Fallback gracioso: se vec_chunks estiver vazio (embeddings não gerados ainda),
 * retorna os chunks originais sem reranking — sem crash, sem erro visível.
 */
object MestreIASemanticEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // Dimensões do modelo all-MiniLM-L6-v2
    private const val EMBEDDING_DIMS = 384

    /**
     * Reranqueia chunks candidatos usando similaridade semântica cosseno.
     * Retorna os chunks reordenados por score híbrido (BM25 + semântico).
     * Se embeddings não disponíveis, retorna candidatos sem modificação.
     */
    suspend fun reranquear(
        query: String,
        candidatos: List<MestreIAChunk>,
        bm25Scores: Map<String, Double>,
        vecDao: com.gurps.ficha.data.storage.VecChunkDao
    ): List<MestreIAChunk> = withContext(Dispatchers.IO) {
        if (candidatos.isEmpty()) return@withContext candidatos

        val vetorCount = vecDao.getCount()
        if (vetorCount == 0) {
            android.util.Log.d("MestreIA_Semantic", "vec_chunks vazio — embeddings não gerados ainda. Usando BM25 puro.")
            return@withContext candidatos
        }

        // Gera embedding da query via API
        val queryEmbedding = gerarEmbeddingViaApi(query)
        if (queryEmbedding == null) {
            android.util.Log.w("MestreIA_Semantic", "Embedding da query falhou — usando BM25 puro.")
            return@withContext candidatos
        }

        // Busca embeddings dos candidatos no banco
        val ids = candidatos.map { it.chunk_id }
        val vetores = vecDao.getByIds(ids).associateBy { it.chunk_id }

        if (vetores.isEmpty()) {
            android.util.Log.d("MestreIA_Semantic", "Nenhum vetor encontrado para candidatos — usando BM25 puro.")
            return@withContext candidatos
        }

        // Normaliza scores BM25 para [0, 1]
        val maxBm25 = bm25Scores.values.maxOrNull()?.coerceAtLeast(0.01) ?: 1.0

        // Score híbrido: 60% BM25 + 40% semântico
        val reranqueados = candidatos.map { chunk ->
            val bm25Norm = (bm25Scores[chunk.chunk_id] ?: 0.0) / maxBm25
            val vetorChunk = vetores[chunk.chunk_id]
            val semantico = if (vetorChunk != null) {
                val chunkVec = byteArrayToFloatArray(vetorChunk.embedding)
                cosineSimilarity(queryEmbedding, chunkVec)
            } else 0.0
            val scoreHibrido = 0.6 * bm25Norm + 0.4 * semantico
            chunk to scoreHibrido
        }.sortedByDescending { it.second }

        val top5 = reranqueados.take(5).joinToString(" | ") { "p.${it.first.page_number}(sem=${String.format("%.2f", it.second)})" }
        android.util.Log.i("MestreIA_Semantic", "Reranking semântico top-5: $top5")

        reranqueados.map { it.first }
    }

    /**
     * Gera embedding de 384 dims via API Gemini Flash Lite.
     * Usa endpoint de embeddings da API Gemini (task_type=RETRIEVAL_QUERY).
     */
    private suspend fun gerarEmbeddingViaApi(texto: String): FloatArray? {
        val apiKey = BuildConfig.MESTRE_IA_GEMINI_KEY
        if (apiKey.isBlank()) return null

        return try {
            val body = JSONObject().apply {
                put("content", JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply { put("text", texto.take(500)) })
                    })
                })
                put("taskType", "RETRIEVAL_QUERY")
            }.toString()

            val model = "text-embedding-004"
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:embedContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                android.util.Log.w("MestreIA_Semantic", "Embedding API ${response.code}: ${response.body?.string()?.take(100)}")
                return null
            }

            val json = JSONObject(response.body!!.string())
            val valuesArr = json.getJSONObject("embedding").getJSONArray("values")
            FloatArray(valuesArr.length()) { valuesArr.getDouble(it).toFloat() }
        } catch (e: Exception) {
            android.util.Log.w("MestreIA_Semantic", "Embedding falhou: ${e.message?.take(60)}")
            null
        }
    }

    /**
     * Serializa FloatArray para ByteArray little-endian (formato de armazenamento).
     */
    fun floatArrayToByteArray(floats: FloatArray): ByteArray {
        val buf = ByteBuffer.allocate(floats.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        floats.forEach { buf.putFloat(it) }
        return buf.array()
    }

    /**
     * Deserializa ByteArray little-endian para FloatArray.
     */
    fun byteArrayToFloatArray(bytes: ByteArray): FloatArray {
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        return FloatArray(bytes.size / 4) { buf.float }
    }

    /**
     * Similaridade cosseno entre dois vetores de mesma dimensão.
     * Retorna valor em [0, 1] — 1 = idênticos, 0 = ortogonais.
     */
    fun cosineSimilarity(a: FloatArray, b: FloatArray): Double {
        if (a.size != b.size || a.isEmpty()) return 0.0
        var dot = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = Math.sqrt(normA) * Math.sqrt(normB)
        return if (denom < 1e-10) 0.0 else (dot / denom).coerceIn(0.0, 1.0)
    }
}
