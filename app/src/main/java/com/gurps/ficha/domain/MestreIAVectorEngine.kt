package com.gurps.ficha.domain

import android.content.Context
import android.util.Log
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.storage.ObjectBoxStore
import com.gurps.ficha.data.storage.VecChunkOBEntity
import com.gurps.ficha.data.storage.VecChunkOBEntity_
import com.gurps.ficha.data.storage.VecChunkDao
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Motor de busca vetorial HNSW via ObjectBox.
 *
 * Substitui o brute-force O(n) do MestreIASemanticEngine por ANN HNSW (~1-5ms).
 * Fluxo: query → embedding Gemini API → HNSW nearestNeighbors → chunk_ids → Room busca textos.
 *
 * Coexiste com Room: ObjectBox guarda apenas chunkId + FloatArray(3072).
 * Populado na inicialização a partir dos embeddings já existentes no Room (vec_chunks).
 *
 * ⚠️ DORMENTE para o AUDITOR desde o Lote 325. Busca semântica HNSW não é mais usada
 * pelo Auditor (que migrou para grep + leitura dirigida). Embeddings (48MB) ficam
 * dormentes; existe chunks.jsonl.bak sem eles. Ver ARQUITETURA_MESTRE_IA.md §5.6.
 */
object MestreIAVectorEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val embeddingCache = java.util.concurrent.ConcurrentHashMap<String, FloatArray>(64)

    private const val TAG = "MestreIA_HNSW"

    /**
     * Inicializa o ObjectBox e popula com embeddings do Room se necessário.
     * Deve ser chamado uma vez na inicialização, após o Room estar pronto.
     */
    suspend fun inicializar(context: Context, vecDao: VecChunkDao) = withContext(Dispatchers.IO) {
        ObjectBoxStore.init(context)
        val box = ObjectBoxStore.get().boxFor(VecChunkOBEntity::class)

        val countOB = box.count()
        Log.i(TAG, "ObjectBox inicializado: $countOB vetores no índice HNSW.")

        if (countOB == 0L) {
            Log.i(TAG, "Populando índice HNSW a partir do Room vec_chunks...")
            val todos = vecDao.getAll()
            if (todos.isEmpty()) {
                Log.w(TAG, "Room vec_chunks vazio — embeddings ainda não importados.")
                return@withContext
            }

            val entidades = todos.map { vec ->
                val floats = MestreIASemanticEngine.byteArrayToFloatArray(vec.embedding)
                VecChunkOBEntity(chunkId = vec.chunk_id, embedding = floats)
            }

            // Insere em lotes de 200 para não explodir memória
            entidades.chunked(200).forEach { lote ->
                box.put(lote)
            }
            Log.i(TAG, "HNSW populado: ${box.count()} vetores indexados em ${entidades.size} chunks.")
        }
    }

    /**
     * Busca os K chunks mais semanticamente próximos da query via HNSW.
     * Retorna lista de chunk_ids ordenados por relevância semântica.
     * Se ObjectBox não estiver pronto ou embedding falhar, retorna lista vazia (fallback para BM25).
     */
    suspend fun buscarTopK(query: String, topK: Int = 50): List<String> = withContext(Dispatchers.IO) {
        val store = try { ObjectBoxStore.get() } catch (e: Exception) {
            Log.w(TAG, "ObjectBox não inicializado — fallback BM25.")
            return@withContext emptyList()
        }

        val box = store.boxFor(VecChunkOBEntity::class)
        if (box.count() == 0L) {
            Log.w(TAG, "Índice HNSW vazio — fallback BM25.")
            return@withContext emptyList()
        }

        val cacheKey = query.take(200).lowercase().trim()
        val queryEmbedding = embeddingCache[cacheKey] ?: run {
            val novo = gerarEmbedding(query) ?: return@withContext emptyList<String>().also {
                Log.w(TAG, "Embedding falhou — fallback BM25.")
            }
            if (embeddingCache.size >= 50) embeddingCache.clear()
            embeddingCache[cacheKey] = novo
            Log.d(TAG, "Embedding gerado e cacheado (size=${embeddingCache.size})")
            novo
        }

        val t0 = System.currentTimeMillis()
        val results = box.query(VecChunkOBEntity_.embedding.nearestNeighbors(queryEmbedding, topK))
            .build()
            .findWithScores()
        val ms = System.currentTimeMillis() - t0

        val chunkIds = results.map { it.get().chunkId }
        val top5 = results.take(5).joinToString(" | ") {
            "${it.get().chunkId.takeLast(8)}(dist=${String.format("%.3f", it.score)})"
        }
        Log.i(TAG, "HNSW top-$topK em ${ms}ms | top-5: $top5")

        chunkIds
    }

    /**
     * Gera embedding de 3072 dims via API Gemini (task_type=RETRIEVAL_QUERY).
     */
    private fun gerarEmbedding(texto: String): FloatArray? {
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

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Embedding API ${response.code}: ${response.body?.string()?.take(100)}")
                return null
            }

            val json = JSONObject(response.body!!.string())
            val valuesArr = json.getJSONObject("embedding").getJSONArray("values")
            FloatArray(valuesArr.length()) { valuesArr.getDouble(it).toFloat() }
        } catch (e: Exception) {
            Log.w(TAG, "Embedding falhou: ${e.message?.take(60)}")
            null
        }
    }

    fun isReady(): Boolean = try {
        ObjectBoxStore.get().boxFor(VecChunkOBEntity::class).count() > 0L
    } catch (e: Exception) { false }
}
