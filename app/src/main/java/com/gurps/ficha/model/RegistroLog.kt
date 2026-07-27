package com.gurps.ficha.model

import androidx.compose.runtime.Stable

/**
 * Modelo para registrar uma modificação feita na ficha do personagem.
 */
@Stable
data class RegistroLog(
    val timestamp: Long,
    val descricao: String
)
