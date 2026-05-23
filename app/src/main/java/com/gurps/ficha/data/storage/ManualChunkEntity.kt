package com.gurps.ficha.data.storage

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

/**
 * Entidade para busca rápida via SQLite FTS4.
 * Room 2.6.1 não suporta @Fts5 — BM25 é implementado em Kotlin no MestreIAGraphEngine.
 * Upgrade para FTS5 requer Room 2.7+; documentado em PLANO_MesteIA_RAG.md Onda 3.
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
