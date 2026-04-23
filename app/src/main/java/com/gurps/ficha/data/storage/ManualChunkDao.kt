package com.gurps.ficha.data.storage

import androidx.room.*

/**
 * Data Access Object para busca instantânea em manuais GURPS.
 */
@Dao
interface ManualChunkDao {
    @Insert
    suspend fun insertAll(chunks: List<ManualChunkEntity>)

    /**
     * Busca usando o operador MATCH do SQLite FTS.
     * Esta busca é ordens de magnitude mais rápida que um 'LIKE'.
     */
    @Query("SELECT * FROM manual_chunks WHERE search_text MATCH :query LIMIT :limit")
    suspend fun buscarRegras(query: String, limit: Int): List<ManualChunkEntity>

    @Query("SELECT * FROM manual_chunks WHERE page_number = :page ORDER BY chunk_id")
    suspend fun buscarPorPagina(page: Int): List<ManualChunkEntity>

    @Query("SELECT * FROM manual_chunks WHERE chunk_id = :id")
    suspend fun getChunkById(id: String): ManualChunkEntity?

    @Query("SELECT COUNT(*) FROM manual_chunks")
    suspend fun getCount(): Int

    @Query("DELETE FROM manual_chunks")
    suspend fun clearAll()
}
