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

    // Versão do formato de search_text. Bump aqui + em CODEX_VERSION_CURRENT para forçar re-importação.
    // v1: apenas texto do chunk. v2: texto + source_title (permite buscar "subaquatico" → chunks do Pyramid).
    private val CODEX_VERSION_KEY = "codex_search_text_version"
    private val CODEX_VERSION_CURRENT = 2

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
     * LOTE 119: Busca DIRETA no Códex (Pula o Grafo).
     * Usa uma combinação de OR para abrangência e AND para precisão.
     */
    suspend fun buscarNoCodexDireto(query: String, termosTecnicos: List<String> = emptyList(), limit: Int = 30): List<MestreIAChunk> {
        val ftsQuery = prepararQueryFTSAgressiva(query, termosTecnicos)
        android.util.Log.i("MestreIA_RAG", "┌─ FTS QUERY: $ftsQuery")

        val resultados = manualChunkDao.buscarRegras(ftsQuery, limit).map { entity ->
            MestreIAChunk(
                chunk_id = entity.chunk_id,
                text = entity.text,
                source_title = entity.source_title,
                source_id = entity.source_id,
                page_number = entity.page_number
            )
        }

        if (resultados.isEmpty()) {
            android.util.Log.w("MestreIA_RAG", "└─ FTS: NENHUM chunk encontrado — query muito específica ou termos ausentes no banco")
        } else {
            val paginas = resultados.mapNotNull { it.page_number }.distinct().sorted().joinToString()
            android.util.Log.i("MestreIA_RAG", "└─ FTS: ${resultados.size} chunks encontrados | páginas: [$paginas]")
        }

        return resultados
    }

    /**
     * Transforma uma busca simples em uma query FTS poderosa com sinônimos.
     */
    private fun prepararQueryFTS(userQuery: String): String {
        return prepararQueryFTSAgressiva(userQuery, emptyList())
    }

    /**
     * LOTE 126: Query FTS Otimizada.
     * Delegada para o MestreIAQueryEngine para testabilidade pura.
     */
    internal fun prepararQueryFTSAgressiva(userQuery: String, termosTecnicos: List<String>): String {
        return MestreIAQueryEngine.prepararQueryFTSAgressiva(userQuery, termosTecnicos)
    }

    private fun String.removeAccents(): String {
        val normalizer = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        return normalizer.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
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
}
