package com.gurps.ficha.data.storage

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

/**
 * Entidade para busca rápida via SQLite FTS4.
 * Permite encontrar regras no meio de milhares de chunks em milissegundos.
 */
@Fts4
@Entity(tableName = "manual_chunks")
data class ManualChunkEntity(
    val chunk_id: String,
    val source_title: String,
    val source_id: String? = null, // Identificador da fonte (Ex: pt_modulo_basico)
    val page_number: Int?,
    val text: String,        // Texto original para contexto da IA
    val search_text: String  // Texto normalizado para busca FTS
)
