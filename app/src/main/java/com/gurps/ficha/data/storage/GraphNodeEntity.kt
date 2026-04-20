package com.gurps.ficha.data.storage

import androidx.room.*

/**
 * Entidade para busca rápida via SQLite FTS4 nos resumos do GraphRAG.
 * Armazena o conhecimento destilado pelo motor de grafo (Comunidades e Entidades).
 */
@Fts4
@Entity(tableName = "graph_nodes")
data class GraphNodeEntity(
    val entity_id: String,
    val title: String,
    val level: Int,       // Nível da comunidade no grafo
    val summary: String,  // Conhecimento destilado
    val category: String  // Categoria (Regra, Equipamento, Vantagem, etc)
)
