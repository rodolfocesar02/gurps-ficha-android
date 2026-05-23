package com.gurps.ficha.data.storage

import androidx.room.*

/**
 * Data Access Object para busca instantânea em manuais GURPS via FTS4.
 * O scoring BM25 é feito em Kotlin no MestreIAGraphEngine após a busca FTS4.
 * (FTS5 nativo requer Room 2.7+ — documentado como Onda 3 do PLANO_MesteIA_RAG.md)
 */
@Dao
interface ManualChunkDao {
    @Insert
    suspend fun insertAll(chunks: List<ManualChunkEntity>)

    /**
     * Busca FTS4 — retorna em ordem de rowid (inserção). Ranking feito pelo BM25-Kotlin no GraphEngine.
     */
    @Query("SELECT * FROM manual_chunks WHERE search_text MATCH :query LIMIT :limit")
    suspend fun buscarRegras(query: String, limit: Int): List<ManualChunkEntity>

    /**
     * Busca FTS4 filtrada por fonte específica.
     */
    @Query("SELECT * FROM manual_chunks WHERE search_text MATCH :query AND source_id = :sourceId LIMIT :limit")
    suspend fun buscarRegrasPorFonte(query: String, sourceId: String, limit: Int): List<ManualChunkEntity>

    @Query("SELECT * FROM manual_chunks WHERE page_number = :page ORDER BY chunk_id")
    suspend fun buscarPorPagina(page: Int): List<ManualChunkEntity>

    @Query("SELECT * FROM manual_chunks WHERE page_number = :page AND source_id = :sourceId ORDER BY chunk_id")
    suspend fun buscarPorPaginaESource(page: Int, sourceId: String): List<ManualChunkEntity>

    @Query("SELECT * FROM manual_chunks WHERE chunk_id = :id")
    suspend fun getChunkById(id: String): ManualChunkEntity?

    @Query("SELECT COUNT(*) FROM manual_chunks")
    suspend fun getCount(): Int

    @Query("DELETE FROM manual_chunks")
    suspend fun clearAll()
}
