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
    private val graphNodeDao = database.graphNodeDao()
    private val syncMutex = Mutex()

    /**
     * Sincroniza o Códex (Chunks e Grafo) se estiverem vazios.
     * Operação atômica e idempotente protegida por Mutex.
     */
    suspend fun sincronizarCodexSeNecessario() = withContext(Dispatchers.IO) {
        syncMutex.withLock {
            if (manualChunkDao.getCount() == 0 || graphNodeDao.countNodes() == 0) {
                android.util.Log.i("MestreIA_Auditoria", "CÓDEX VAZIO: Iniciando carga única protegida...")
                FichaDatabase.prePopulateGraph(context, database)
                FichaDatabase.prePopulateManual(context, database)
            } else {
                android.util.Log.i("MestreIA_Auditoria", "CÓDEX OK: Banco de dados já possui dados.")
            }
        }
    }

    suspend fun buscarResumosGrafo(query: String) = graphNodeDao.buscarNodes(query)
    
    suspend fun buscarNodesPorTitulo(query: String) = graphNodeDao.findByTitleLike(query)
    
    suspend fun buscarResumoNode(entityId: String) = graphNodeDao.getNodeById(entityId)
    
    suspend fun buscarResumosEssenciais() = graphNodeDao.getEssentialNodes()
    
    suspend fun findByCategory(category: String) = graphNodeDao.findByCategory(category)

    /**
     * Busca nos recortes manuais brutos (FTS4).
     */
    suspend fun buscarRecortesManual(query: String, limit: Int = 30): List<MestreIAChunk> {
        return manualChunkDao.buscarRegras(query, limit).map { entity ->
            MestreIAChunk(
                chunk_id = entity.chunk_id,
                text = entity.text,
                source_title = entity.source_title,
                page_number = entity.page_number
            )
        }
    }

    suspend fun buscarPorPagina(pagina: Int): List<MestreIAChunk> {
        return manualChunkDao.buscarPorPagina(pagina).map { entity ->
            MestreIAChunk(
                chunk_id = entity.chunk_id,
                text = entity.text,
                source_title = entity.source_title,
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
                page_number = entity.page_number
            )
        }
    }

    suspend fun forçarSincronizacaoGrafo() {
        graphNodeDao.clearAll()
        FichaDatabase.prePopulateGraph(context, database)
    }

    suspend fun forçarSincronizacaoManual() {
        manualChunkDao.clearAll()
        FichaDatabase.prePopulateManual(context, database)
    }
}
