package com.gurps.ficha.data.storage

import androidx.room.*

/**
 * Entidade para busca rápida via SQLite FTS4 nos resumos do GraphRAG.
 * Armazena o conhecimento destilado pelo motor de grafo (Comunidades e Entidades).
 */
@Fts4
@Entity(tableName = "graph_nodes")
data class GraphNodeEntity(
    val entityId: String,  // ID único
    val title: String,
    val level: Int,       // Nível da comunidade no grafo
    val summary: String,  // Conhecimento destilado
    val category: String, // Categoria (Regra, Equipamento, Vantagem, etc)
    val source_id: String? = null // LOTE 112: Identificador da fonte bibliográfica
)
