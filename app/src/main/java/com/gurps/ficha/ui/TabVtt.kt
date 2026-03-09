package com.gurps.ficha.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.gurps.ficha.viewmodel.FichaViewModel

private enum class VttConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

@Composable
fun TabVtt(viewModel: FichaViewModel) {
    var serverUrl by remember { mutableStateOf("") }
    var roomKey by remember { mutableStateOf("") }
    var playerId by remember(viewModel.personagem.nome) { mutableStateOf(viewModel.personagem.nome) }
    var connectionState by remember { mutableStateOf(VttConnectionState.DISCONNECTED) }
    var statusMessage by remember { mutableStateOf("Preencha servidor, sala e player para iniciar.") }

    fun conectarEmModoShell() {
        connectionState = VttConnectionState.CONNECTING
        if (serverUrl.isBlank() || roomKey.isBlank() || playerId.isBlank()) {
            connectionState = VttConnectionState.ERROR
            statusMessage = "Campos obrigatórios: servidor, sala e player."
            return
        }
        connectionState = VttConnectionState.CONNECTED
        statusMessage = "Configuração local validada. Integração de sessão entra no próximo passo."
    }

    fun desconectarEmModoShell() {
        connectionState = VttConnectionState.DISCONNECTED
        statusMessage = "Desconectado (shell)."
    }

    val statusLabel = when (connectionState) {
        VttConnectionState.DISCONNECTED -> "Desconectado"
        VttConnectionState.CONNECTING -> "Conectando"
        VttConnectionState.CONNECTED -> "Conectado"
        VttConnectionState.ERROR -> "Erro"
    }
    val statusColor = when (connectionState) {
        VttConnectionState.CONNECTED -> Color(0xFF2E7D32)
        VttConnectionState.ERROR -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    StandardTabColumn {
        SectionCard(title = "Conexão VTT") {
            Text(
                text = "Aba VTT (shell) ativa. Integração de sessão e rolagem será ligada pelos próximos lotes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Servidor (URL)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = roomKey,
                onValueChange = { roomKey = it },
                label = { Text("Sala (roomKey)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = playerId,
                onValueChange = { playerId = it },
                label = { Text("Player ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: $statusLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { conectarEmModoShell() },
                    enabled = connectionState != VttConnectionState.CONNECTING
                ) {
                    Text("Conectar")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { desconectarEmModoShell() },
                    enabled = connectionState != VttConnectionState.DISCONNECTED
                ) {
                    Text("Desconectar")
                }
            }
        }

        SummaryFooterCard(title = "Próximos passos") {
            Text(
                text = "1. Join de sessão VTT",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "2. Vínculo player e token",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "3. Rolagem contextual via contrato v1",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
