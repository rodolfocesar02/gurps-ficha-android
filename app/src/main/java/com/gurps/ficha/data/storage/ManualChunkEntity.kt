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
    val source_title: String,
    val page_number: Int?,
    val text: String
)
