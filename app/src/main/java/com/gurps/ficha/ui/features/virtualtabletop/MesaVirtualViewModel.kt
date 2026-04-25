package com.gurps.ficha.ui.features.virtualtabletop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MesaVirtualState(
    val discordId: String? = null,
    val token: String? = null,
    val campaignId: String? = null,
    val isConnected: Boolean = false,
    val activePlayers: List<String> = emptyList()
)

class MesaVirtualViewModel : ViewModel() {
    private val _state = MutableStateFlow(MesaVirtualState())
    val state = _state.asStateFlow()

    fun conectar(discordId: String, token: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                discordId = discordId,
                token = token,
                isConnected = true
            )
            // Aqui futuramente chamaremos o Railway para confirmar a conexão
        }
    }

    fun desconectar() {
        _state.value = MesaVirtualState()
    }
}
