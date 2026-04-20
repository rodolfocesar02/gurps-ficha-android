package com.gurps.ficha.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GraphNodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nodes: List<GraphNodeEntity>)

    @Query("DELETE FROM graph_nodes")
    suspend fun clearAll()

    /**
     * Busca no Grafo via FTS4 (Match)
     */
    @Query("""
        SELECT * FROM graph_nodes 
        WHERE graph_nodes MATCH :query 
        ORDER BY level ASC, rank DESC 
        LIMIT 10
    """)
    suspend fun searchGraph(query: String): List<GraphNodeEntity>

    /**
     * Busca simples por categoria (Fallback)
     */
    @Query("SELECT * FROM graph_nodes WHERE category = :category ORDER BY level ASC")
    suspend fun findByCategory(category: String): List<GraphNodeEntity>
}
