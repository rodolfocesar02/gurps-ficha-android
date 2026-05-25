package com.gurps.ficha.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Lote 259: DAO para embeddings semânticos.
 */
@Dao
interface VecChunkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chunks: List<VecChunkEntity>)

    @Query("SELECT * FROM vec_chunks WHERE chunk_id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<VecChunkEntity>

    @Query("SELECT * FROM vec_chunks")
    suspend fun getAll(): List<VecChunkEntity>

    @Query("SELECT COUNT(*) FROM vec_chunks")
    suspend fun getCount(): Int

    @Query("DELETE FROM vec_chunks")
    suspend fun clearAll()
}
