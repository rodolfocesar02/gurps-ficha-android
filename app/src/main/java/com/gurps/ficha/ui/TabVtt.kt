package com.gurps.ficha.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.gurps.ficha.vtt.VttSessionService
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.launch

private enum class VttConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

private enum class VttEnvironment(val label: String, val defaultUrl: String) {
    DEV("Dev", "http://10.0.2.2:3001"),
    HOMOLOG("Homolog", "https://seu-vtt-homolog.exemplo.com"),
    PROD("Prod", "https://seu-vtt-producao.exemplo.com"),
    CUSTOM("Custom", "")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabVtt(viewModel: FichaViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var environment by remember { mutableStateOf(VttEnvironment.DEV) }
    var serverUrl by remember { mutableStateOf(VttEnvironment.DEV.defaultUrl) }
    var roomKey by remember { mutableStateOf("") }
    var playerId by remember(viewModel.personagem.nome) { mutableStateOf(viewModel.personagem.nome) }
    var connectionState by remember { mutableStateOf(VttConnectionState.DISCONNECTED) }
    var statusMessage by remember { mutableStateOf("Preencha servidor, sala e player para iniciar.") }
    var sessionId by remember { mutableStateOf<String?>(null) }
    var tokenId by remember { mutableStateOf<String?>(null) }

    fun conectarEmModoShell() {
        connectionState = VttConnectionState.CONNECTING
        if (serverUrl.isBlank() || roomKey.isBlank() || playerId.isBlank()) {
            connectionState = VttConnectionState.ERROR
            statusMessage = "Campos obrigatórios: servidor, sala e player."
            return
        }
        scope.launch {
            VttSessionService.joinSession(
                roomKey = roomKey.trim(),
                playerId = playerId.trim(),
                fichaJsonRaw = viewModel.exportarFichaJsonCompativel(),
                baseUrl = serverUrl.trim()
            ).onSuccess { result ->
                connectionState = VttConnectionState.CONNECTED
                sessionId = result.sessionId
                tokenId = result.tokenId
                statusMessage = buildString {
                    append(result.message)
                    if (!sessionId.isNullOrBlank()) append(" Sessão: $sessionId.")
                    if (!tokenId.isNullOrBlank()) append(" Token: $tokenId.")
                }
            }.onFailure { err ->
                connectionState = VttConnectionState.ERROR
                statusMessage = err.message ?: "Falha ao iniciar sessão VTT."
            }
        }
    }

    fun desconectarEmModoShell() {
        connectionState = VttConnectionState.DISCONNECTED
        statusMessage = "Desconectado (shell)."
        sessionId = null
        tokenId = null
    }

    fun abrirVttNoNavegador() {
        if (serverUrl.isBlank()) {
            connectionState = VttConnectionState.ERROR
            statusMessage = "Defina a URL do servidor para abrir externamente."
            return
        }
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(serverUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            statusMessage = "VTT aberto no navegador."
        }.onFailure {
            connectionState = VttConnectionState.ERROR
            statusMessage = "Falha ao abrir navegador para a URL informada."
        }
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
            Text(
                text = "Ambiente",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
            ) {
                VttEnvironment.entries.forEach { item ->
                    FilterChip(
                        selected = environment == item,
                        onClick = {
                            environment = item
                            if (item != VttEnvironment.CUSTOM) {
                                serverUrl = item.defaultUrl
                            }
                        },
                        label = { Text(item.label) }
                    )
                }
            }
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
            if (!sessionId.isNullOrBlank() || !tokenId.isNullOrBlank()) {
                Text(
                    text = "Sessão: ${sessionId ?: "-"} | Token: ${tokenId ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
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
            Button(
                onClick = { abrirVttNoNavegador() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Abrir VTT no navegador")
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
