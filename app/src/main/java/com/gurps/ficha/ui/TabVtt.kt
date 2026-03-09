package com.gurps.ficha.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.gurps.ficha.vtt.VttSessionSnapshot
import com.gurps.ficha.vtt.VttSessionStorage
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

private enum class VttActionType(val label: String) {
    TESTE("Teste"),
    PERICIA("Pericia"),
    MAGIA("Magia"),
    DEFESA("Defesa")
}

private const val VTT_UI_LOG = "VttTab"

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
    var bootstrapDone by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf(VttActionType.PERICIA) }
    var acaoNome by remember { mutableStateOf("") }
    var alvoTokenId by remember { mutableStateOf("") }
    var modificadorRaw by remember { mutableStateOf("0") }

    LaunchedEffect(context) {
        if (!bootstrapDone) {
            val snap = VttSessionStorage.load(context)
            if (snap.serverUrl.isNotBlank()) {
                serverUrl = snap.serverUrl
                environment = VttEnvironment.CUSTOM
            }
            if (snap.roomKey.isNotBlank()) roomKey = snap.roomKey
            if (snap.playerId.isNotBlank()) playerId = snap.playerId
            if (snap.sessionId.isNotBlank()) sessionId = snap.sessionId
            if (snap.tokenId.isNotBlank()) tokenId = snap.tokenId
            if (!sessionId.isNullOrBlank() || !tokenId.isNullOrBlank()) {
                statusMessage = "Sessao local restaurada. Voce pode reconectar."
            }
            bootstrapDone = true
        }
    }

    fun conectarEmModoShell() {
        connectionState = VttConnectionState.CONNECTING
        if (serverUrl.isBlank() || roomKey.isBlank() || playerId.isBlank()) {
            connectionState = VttConnectionState.ERROR
            statusMessage = "Campos obrigatorios: servidor, sala e player."
            return
        }

        scope.launch {
            VttSessionService.joinSession(
                roomKey = roomKey.trim(),
                playerId = playerId.trim(),
                fichaJsonRaw = viewModel.exportarFichaJsonCompativel(),
                previousSessionId = sessionId,
                previousTokenId = tokenId,
                baseUrl = serverUrl.trim()
            ).onSuccess { result ->
                connectionState = VttConnectionState.CONNECTED
                sessionId = result.sessionId
                tokenId = result.tokenId
                Log.i(
                    VTT_UI_LOG,
                    "joinSession success roomKey=${roomKey.trim()} playerId=${playerId.trim()} sessionId=${sessionId.orEmpty()} tokenId=${tokenId.orEmpty()}"
                )
                VttSessionStorage.save(
                    context,
                    VttSessionSnapshot(
                        serverUrl = serverUrl.trim(),
                        roomKey = roomKey.trim(),
                        playerId = playerId.trim(),
                        sessionId = sessionId.orEmpty(),
                        tokenId = tokenId.orEmpty()
                    )
                )
                statusMessage = buildString {
                    append(result.message)
                    if (!sessionId.isNullOrBlank()) append(" Sessao: $sessionId.")
                    if (!tokenId.isNullOrBlank()) append(" Token: $tokenId.")
                }
            }.onFailure { err ->
                connectionState = VttConnectionState.ERROR
                statusMessage = err.message ?: "Falha ao iniciar sessao VTT."
                Log.w(
                    VTT_UI_LOG,
                    "joinSession failure roomKey=${roomKey.trim()} playerId=${playerId.trim()} reason=$statusMessage"
                )
            }
        }
    }

    fun desconectarEmModoShell() {
        connectionState = VttConnectionState.DISCONNECTED
        statusMessage = "Desconectado (shell)."
        VttSessionStorage.save(
            context,
            VttSessionSnapshot(
                serverUrl = serverUrl.trim(),
                roomKey = roomKey.trim(),
                playerId = playerId.trim(),
                sessionId = sessionId.orEmpty(),
                tokenId = tokenId.orEmpty()
            )
        )
        Log.i(VTT_UI_LOG, "disconnect shell roomKey=${roomKey.trim()} playerId=${playerId.trim()}")
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
            Log.i(VTT_UI_LOG, "openExternalVtt url=${serverUrl.trim()}")
        }.onFailure {
            connectionState = VttConnectionState.ERROR
            statusMessage = "Falha ao abrir navegador para a URL informada."
            Log.w(VTT_UI_LOG, "openExternalVtt failure url=${serverUrl.trim()}")
        }
    }

    fun limparSessaoLocal() {
        sessionId = null
        tokenId = null
        VttSessionStorage.save(
            context,
            VttSessionSnapshot(
                serverUrl = serverUrl.trim(),
                roomKey = roomKey.trim(),
                playerId = playerId.trim(),
                sessionId = "",
                tokenId = ""
            )
        )
        connectionState = VttConnectionState.DISCONNECTED
        statusMessage = "Sessao local limpa. Reconecte para receber novo vinculo."
        Log.i(VTT_UI_LOG, "clearLocalSession roomKey=${roomKey.trim()} playerId=${playerId.trim()}")
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
        SectionCard(title = "Conexao VTT") {
            Text(
                text = "Aba VTT (shell) ativa. Integracao de sessao e rolagem sera ligada pelos proximos lotes.",
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
                    text = "Sessao: ${sessionId ?: "-"} | Token: ${tokenId ?: "-"}",
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
            if (connectionState == VttConnectionState.ERROR) {
                Button(
                    onClick = { limparSessaoLocal() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Limpar sessao local")
                }
            }
        }

        SectionCard(title = "Acoes de Rolagem (VTT)") {
            val personagem = viewModel.personagem
            val primeiraPericia = personagem.pericias.firstOrNull()?.nome.orEmpty()
            val primeiraMagia = personagem.magias.firstOrNull()?.nome.orEmpty()
            val apara = personagem.defesasAtivas.calcularApara(personagem)
            val bloqueio = personagem.defesasAtivas.calcularBloqueio(personagem)
            val esquiva = personagem.defesasAtivas.calcularEsquiva(personagem)

            Text(
                text = "Painel contextual para acao no VTT. Envio para backend entra no proximo passo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
            ) {
                VttActionType.entries.forEach { tipo ->
                    FilterChip(
                        selected = actionType == tipo,
                        onClick = { actionType = tipo },
                        label = { Text(tipo.label) }
                    )
                }
            }

            OutlinedTextField(
                value = acaoNome,
                onValueChange = { acaoNome = it },
                label = { Text("Nome da acao") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = alvoTokenId,
                onValueChange = { alvoTokenId = it },
                label = { Text("Token alvo (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = modificadorRaw,
                onValueChange = { modificadorRaw = it },
                label = { Text("Modificador") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (primeiraPericia.isNotBlank()) {
                            actionType = VttActionType.PERICIA
                            acaoNome = primeiraPericia
                        }
                    },
                    enabled = primeiraPericia.isNotBlank()
                ) { Text("Usar 1a pericia") }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (primeiraMagia.isNotBlank()) {
                            actionType = VttActionType.MAGIA
                            acaoNome = primeiraMagia
                        }
                    },
                    enabled = primeiraMagia.isNotBlank()
                ) { Text("Usar 1a magia") }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        actionType = VttActionType.DEFESA
                        acaoNome = "Apara ${apara ?: "-"}"
                    },
                    enabled = apara != null
                ) { Text("Apara") }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        actionType = VttActionType.DEFESA
                        acaoNome = "Bloqueio ${bloqueio ?: "-"}"
                    },
                    enabled = bloqueio != null
                ) { Text("Bloqueio") }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        actionType = VttActionType.DEFESA
                        acaoNome = "Esquiva $esquiva"
                    }
                ) { Text("Esquiva") }
            }
        }

        SummaryFooterCard(title = "Proximos passos") {
            Text(text = "1. Join de sessao VTT", style = MaterialTheme.typography.bodySmall)
            Text(text = "2. Vinculo player e token", style = MaterialTheme.typography.bodySmall)
            Text(text = "3. Rolagem contextual via contrato v1", style = MaterialTheme.typography.bodySmall)
        }
    }
}
