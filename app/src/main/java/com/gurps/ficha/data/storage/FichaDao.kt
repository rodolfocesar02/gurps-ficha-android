package com.gurps.ficha.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FichaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ficha: FichaEntity)

    @Query("SELECT json FROM fichas WHERE nomeArquivo = :nomeArquivo LIMIT 1")
    suspend fun getJson(nomeArquivo: String): String?

    @Query("DELETE FROM fichas WHERE nomeArquivo = :nomeArquivo")
    suspend fun deleteByName(nomeArquivo: String)

    @Query("SELECT nomeArquivo FROM fichas ORDER BY updatedAt DESC")
    suspend fun listNames(): List<String>
}
