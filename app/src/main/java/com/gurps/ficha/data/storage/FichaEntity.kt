package com.gurps.ficha.data.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fichas")
data class FichaEntity(
    @PrimaryKey
    val nomeArquivo: String,
    val json: String,
    val updatedAt: Long
)
