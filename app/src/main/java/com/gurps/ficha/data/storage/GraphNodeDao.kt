package com.gurps.ficha.data.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GraphNodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: GraphNodeEntity)

    @Query("SELECT COUNT(*) FROM graph_nodes")
    suspend fun countNodes(): Int

    @Query("DELETE FROM graph_nodes")
    suspend fun clearAll()

    /**
     * Busca no Grafo via FTS4 (Match)
     */
    @Query("""
        SELECT * FROM graph_nodes 
        WHERE graph_nodes MATCH :query 
        ORDER BY level ASC 
        LIMIT 10
    """)
    suspend fun searchGraph(query: String): List<GraphNodeEntity>

    /**
     * Busca simples por categoria (Fallback)
     */
    @Query("SELECT * FROM graph_nodes WHERE category = :category ORDER BY level ASC")
    suspend fun findByCategory(category: String): List<GraphNodeEntity>

    /**
     * Busca os nós essenciais (Nível 1) que devem estar sempre no prompt.
     */
    @Query("SELECT * FROM graph_nodes WHERE level <= 1")
    suspend fun getEssentialNodes(): List<GraphNodeEntity>
}
