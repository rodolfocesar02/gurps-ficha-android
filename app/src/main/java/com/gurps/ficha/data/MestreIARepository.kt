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
            val needsPurification = true // LOTE 108: Forçamos a entrada para corrigir o Mojibake
            val isEmpty = manualChunkDao.getCount() == 0 || graphNodeDao.countNodes() == 0
            
            if (isEmpty || needsPurification) {
                android.util.Log.i("MestreIA_Auditoria", "INICIANDO VERIFICAÇÃO TÉCNICA (Lote 108/Purificação)...")
                FichaDatabase.prePopulateGraph(context, database)
                FichaDatabase.prePopulateManual(context, database)
            } else {
                android.util.Log.i("MestreIA_Auditoria", "CÓDEX OK: Banco de dados íntegro.")
            }
        }
    }

    suspend fun buscarResumosGrafo(query: String) = graphNodeDao.buscarNodes(query)
    
    suspend fun buscarNodesPorTitulo(query: String) = graphNodeDao.findByTitleLike(query)
    
    suspend fun buscarResumoNode(entityId: String) = graphNodeDao.getNodeById(entityId)
    
    suspend fun buscarResumosEssenciais() = graphNodeDao.getEssentialNodes()
    
    suspend fun findByCategory(category: String) = graphNodeDao.findByCategory(category)

    /**
     * Busca inteligente nos manuais com expansão de termos (Sinônimos GURPS).
     */
    suspend fun buscarRecortesManual(query: String, limit: Int = 30): List<MestreIAChunk> {
        val expandedQuery = prepararQueryFTS(query)
        android.util.Log.d("MestreIA_Auditoria", "QUERY EXPANDIDA: $expandedQuery")
        
        return manualChunkDao.buscarRegras(expandedQuery, limit).map { entity ->
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
     * Transforma uma busca simples em uma query FTS poderosa com sinônimos.
     */
    private fun prepararQueryFTS(userQuery: String): String {
        // Limpa asteriscos e lixo da query original para evitar duplicidade
        val cleanQuery = userQuery.replace("*", "").trim()
        val terms = cleanQuery.split(Regex("\\s+")).filter { it.length >= 2 }
        if (terms.isEmpty()) return cleanQuery
        
        val expandedTerms = terms.map { term ->
            val termNorm = term.lowercase().removeAccents()
            val synonyms = when (termNorm) {
                "colisao", "colisões", "bater", "impacto", "encontrao", "encontrão" -> "colis* OR encontr* OR impact*"
                "dano", "ferimento", "vida", "pv" -> "dano* OR ferim* OR PV"
                "fadiga", "cansaco", "pf" -> "fadig* OR cansac* OR PF"
                "atirar", "disparo", "arma" -> "atir* OR dispar* OR arma*"
                "esquiva", "bloqueio", "aparar", "defesa" -> "esquiv* OR bloqu* OR apar* OR defens*"
                "agua", "água", "piscina", "mar", "rio", "submerso", "subaquatico", "subaquático" -> "agua* OR águ* OR submers* OR subaquat*"
                "redutor", "penalidade", "modificador" -> "redut* OR penal* OR modif*"
                else -> if (termNorm.length > 3) "$termNorm*" else termNorm
            }
            "($synonyms)"
        }
        
        return expandedTerms.joinToString(" AND ")
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

    suspend fun forçarSincronizacaoGrafo() {
        graphNodeDao.clearAll()
        FichaDatabase.prePopulateGraph(context, database)
    }

    suspend fun forçarSincronizacaoManual() {
        manualChunkDao.clearAll()
        FichaDatabase.prePopulateManual(context, database)
    }
}
