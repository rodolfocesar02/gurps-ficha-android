package com.gurps.ficha.data.storage

import androidx.room.Entity
import androidx.room.Fts4

/**
 * Entidade para busca rápida via SQLite FTS4 (Room 2.6.1).
 * BM25 é implementado em Kotlin no MestreIAGraphEngine sobre o pool FTS4.
 * FTS5 nativo requer Room 2.7-alpha+ — upgrade agendado quando estabilizar.
 */
@Fts4
@Entity(tableName = "manual_chunks")
data class ManualChunkEntity(
    val chunk_id: String,
    val source_title: String,
    val source_id: String? = null,
    val page_number: Int? = null,
    val text: String,        // Texto original para contexto da IA
    val search_text: String  // Texto normalizado para busca FTS
)
