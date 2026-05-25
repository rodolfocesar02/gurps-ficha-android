package com.gurps.ficha.data

import android.content.Context
import com.gurps.ficha.data.storage.FichaDatabase
import com.gurps.ficha.model.MestreIAChunk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * MestreIARepository - Especialista em persistência e sincronização do Códex (RAG).
 * Isola a lógica do motor de regras da lógica da ficha de personagem.
 */
class MestreIARepository(
    private val context: Context,
    private val database: FichaDatabase
) {
    private val manualChunkDao = database.manualChunkDao()
    private val syncMutex = Mutex()

    // LRU Cache de buscas FTS — evita re-processar queries repetidas na mesma sessão.
    // Tamanho 20: cobre multi-query temático (4 queries × 5 perguntas) sem pressão de memória.
    private val ftsCache = object : LinkedHashMap<String, List<MestreIAChunk>>(20, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<MestreIAChunk>>) = size > 20
    }
    private val cacheMutex = Mutex()

    // Versão do formato de search_text. Bump aqui + em CODEX_VERSION_CURRENT para forçar re-importação.
    // v1: apenas texto do chunk. v2: texto + source_title (permite buscar "subaquatico" → chunks do Pyramid).
    private val CODEX_VERSION_KEY = "codex_search_text_version"
    private val CODEX_VERSION_CURRENT = 3  // Lote 266: força reimportação de embeddings semânticos

    /**
     * Sincroniza o Códex (chunks.jsonl) se o banco estiver vazio ou desatualizado.
     * Operação atômica e idempotente protegida por Mutex.
     */
    suspend fun sincronizarCodexSeNecessario() = withContext(Dispatchers.IO) {
        syncMutex.withLock {
            val prefs = context.getSharedPreferences("mestre_ia_prefs", android.content.Context.MODE_PRIVATE)
            val versaoSalva = prefs.getInt(CODEX_VERSION_KEY, 1)
            val count = manualChunkDao.getCount()
            val isEmpty = count == 0
            val desatualizado = versaoSalva < CODEX_VERSION_CURRENT

            if (isEmpty || desatualizado) {
                if (desatualizado && !isEmpty) {
                    android.util.Log.i("MestreIA_Auditoria", "CÓDEX DESATUALIZADO (v$versaoSalva → v$CODEX_VERSION_CURRENT): Limpando e re-importando...")
                    manualChunkDao.clearAll()
                } else {
                    android.util.Log.i("MestreIA_Auditoria", "CÓDEX VAZIO: Importando chunks.jsonl...")
                }
                FichaDatabase.prePopulateManual(context, database)
                prefs.edit().putInt(CODEX_VERSION_KEY, CODEX_VERSION_CURRENT).apply()
            } else {
                android.util.Log.i("MestreIA_Auditoria", "CÓDEX OK v$versaoSalva: ${manualChunkDao.getCount()} chunks no banco.")
            }
        }
    }

    /**
     * Busca DIRETA no Códex via FTS4 com LRU cache de sessão.
     * Cache evita re-processar a mesma query FTS dentro da mesma conversa (multi-query temático).
     */
    suspend fun buscarNoCodexDireto(query: String, termosTecnicos: List<String> = emptyList(), limit: Int = 500): List<MestreIAChunk> {
        val ftsQuery = prepararQueryFTSAgressiva(query, termosTecnicos)
        val cacheKey = "$ftsQuery:$limit"

        // Consulta cache primeiro (sem I/O de banco)
        val cached = cacheMutex.withLock { ftsCache[cacheKey] }
        if (cached != null) {
            android.util.Log.i("MestreIA_RAG", "┌─ FTS CACHE HIT: ${cached.size} chunks (query já processada)")
            return cached
        }

        android.util.Log.i("MestreIA_RAG", "┌─ FTS4 QUERY: $ftsQuery")
        val resultados = withContext(Dispatchers.IO) {
            manualChunkDao.buscarRegras(ftsQuery, limit).map { entity ->
                MestreIAChunk(
                    chunk_id = entity.chunk_id,
                    text = entity.text,
                    source_title = entity.source_title,
                    source_id = entity.source_id,
                    page_number = entity.page_number
                )
            }
        }

        if (resultados.isEmpty()) {
            android.util.Log.w("MestreIA_RAG", "└─ FTS4: NENHUM chunk encontrado — query muito específica ou termos ausentes no banco")
        } else {
            val paginas = resultados.mapNotNull { it.page_number }.distinct().sorted().joinToString()
            android.util.Log.i("MestreIA_RAG", "└─ FTS4: ${resultados.size} chunks | páginas: [$paginas]")
            cacheMutex.withLock { ftsCache[cacheKey] = resultados }
        }

        return resultados
    }

    /** Limpa o cache de FTS ao iniciar nova sessão de perguntas. */
    fun limparCacheFTS() {
        kotlinx.coroutines.runBlocking { cacheMutex.withLock { ftsCache.clear() } }
    }

    /**
     * Busca filtrada por fonte específica (ex: apenas Pyramid, apenas GunFu).
     * Útil quando o Planner identifica que a pergunta é de um suplemento específico.
     */
    suspend fun buscarNoCodexPorFonte(query: String, sourceId: String, limit: Int = 50): List<MestreIAChunk> {
        val ftsQuery = prepararQueryFTSAgressiva(query, emptyList())
        return manualChunkDao.buscarRegrasPorFonte(ftsQuery, sourceId, limit).map { entity ->
            MestreIAChunk(
                chunk_id = entity.chunk_id,
                text = entity.text,
                source_title = entity.source_title,
                source_id = entity.source_id,
                page_number = entity.page_number
            )
        }
    }

    /**
     * LOTE 126: Query FTS Otimizada.
     * Delegada para o MestreIAQueryEngine para testabilidade pura.
     */
    internal fun prepararQueryFTSAgressiva(userQuery: String, termosTecnicos: List<String>): String {
        return MestreIAQueryEngine.prepararQueryFTSAgressiva(userQuery, termosTecnicos)
    }

    suspend fun buscarPorPagina(pagina: Int): List<MestreIAChunk> {
        return manualChunkDao.buscarPorPagina(pagina).map { entity ->
            MestreIAChunk(
                chunk_id = entity.chunk_id,
                text = entity.text,
                source_title = entity.source_title,
                source_id = entity.source_id,
                page_number = entity.page_number
            )
        }
    }

    suspend fun buscarPorPaginaESource(pagina: Int, source: String): List<MestreIAChunk> {
        return manualChunkDao.buscarPorPaginaESource(pagina, source).map { entity ->
            MestreIAChunk(
                chunk_id = entity.chunk_id,
                text = entity.text,
                source_title = entity.source_title,
                source_id = entity.source_id,
                page_number = entity.page_number
            )
        }
    }

    suspend fun getChunkById(id: String): MestreIAChunk? {
        return manualChunkDao.getChunkById(id)?.let { entity ->
            MestreIAChunk(
                chunk_id = entity.chunk_id,
                text = entity.text,
                source_title = entity.source_title,
                source_id = entity.source_id,
                page_number = entity.page_number
            )
        }
    }

    suspend fun forçarSincronizacaoManual() {
        manualChunkDao.clearAll()
        FichaDatabase.prePopulateManual(context, database)
    }

    /** Retorna o número total de chunks no corpus (para IDF global correto no BM25). */
    suspend fun contarTotalChunks(): Int = withContext(Dispatchers.IO) {
        manualChunkDao.getCount()
    }

    /**
     * Retorna avgdl estimado do corpus completo.
     * Valor calibrado empiricamente com os chunks GURPS (~900 chars/chunk).
     * Evita query extra ao banco mantendo a infraestrutura imutável.
     */
    fun calcularAvgdlCorpus(): Double = 900.0
}
