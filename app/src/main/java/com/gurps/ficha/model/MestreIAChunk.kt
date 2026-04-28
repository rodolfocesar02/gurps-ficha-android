package com.gurps.ficha.model

/**
 * Fragmento de regra oficial extraído do Codex (GURPS 4e Brasil).
 * Centralizado para visibilidade total do sistema.
 */
data class MestreIAChunk(
    val chunk_id: String,
    val text: String,
    val source_title: String,
    val source_id: String? = null, // LOTE 112: Identificador único da fonte (Ex: pt_modulo_basico)
    val page_number: Int? = null
)
