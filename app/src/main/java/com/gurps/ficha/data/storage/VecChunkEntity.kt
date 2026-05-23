package com.gurps.ficha.data.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lote 259: Tabela de embeddings para busca semântica híbrida.
 * Embeddings gerados offline pelo script Python (gerar_embeddings.py)
 * e importados junto com chunks.jsonl.
 *
 * embedding: 384 floats (all-MiniLM-L6-v2) serializados como ByteArray little-endian.
 * Cada float ocupa 4 bytes → 384 × 4 = 1536 bytes por chunk.
 */
@Entity(tableName = "vec_chunks")
data class VecChunkEntity(
    @PrimaryKey val chunk_id: String,
    val embedding: ByteArray
) {
    override fun equals(other: Any?) = other is VecChunkEntity && chunk_id == other.chunk_id
    override fun hashCode() = chunk_id.hashCode()
}
