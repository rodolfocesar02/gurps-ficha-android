package com.gurps.ficha.data.storage

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

/**
 * Entidade ObjectBox para busca vetorial HNSW.
 * Coexiste com Room — Room guarda texto/metadados, ObjectBox guarda apenas vetores.
 * Embeddings: 3072 dims (Gemini gemini-embedding-001).
 */
@Entity
data class VecChunkOBEntity(
    @Id var id: Long = 0,
    @Index var chunkId: String = "",
    @HnswIndex(dimensions = 3072) var embedding: FloatArray? = null
) {
    override fun equals(other: Any?) = other is VecChunkOBEntity && chunkId == other.chunkId
    override fun hashCode() = chunkId.hashCode()
}
