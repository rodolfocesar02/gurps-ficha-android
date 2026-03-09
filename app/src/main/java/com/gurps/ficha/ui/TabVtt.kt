package com.gurps.ficha.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.gurps.ficha.vtt.VttRollRequest
import com.gurps.ficha.vtt.VttRollService
import com.gurps.ficha.vtt.VttTokenBindService
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.unit.dp

private enum class VttConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

private enum class VttEnvironment(
    val label: String,
    val apiDefaultUrl: String,
    val webDefaultUrl: String
) {
    DEV("Dev", "http://10.0.2.2:3001", "http://10.0.2.2:5176"),
    HOMOLOG(
        "Homolog",
        "https://seu-vtt-api-homolog.exemplo.com",
        "https://seu-vtt-web-homolog.exemplo.com"
    ),
    PROD(
        "Prod",
        "https://seu-vtt-api-producao.exemplo.com",
        "https://seu-vtt-web-producao.exemplo.com"
    ),
    CUSTOM("Custom", "", "")
}

private enum class VttActionType(val label: String) {
    TESTE("Teste"),
    PERICIA("Pericia"),
    MAGIA("Magia"),
    DEFESA("Defesa")
}

private enum class VttViewMode(val label: String) {
    MAPA("Mapa"),
    PAINEL("Painel")
}

private const val VTT_UI_LOG = "VttTab"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabVtt(viewModel: FichaViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var environment by remember { mutableStateOf(VttEnvironment.DEV) }
    var serverUrl by remember { mutableStateOf(VttEnvironment.DEV.apiDefaultUrl) }
    var webUrl by remember { mutableStateOf(VttEnvironment.DEV.webDefaultUrl) }
    var roomKey by remember { mutableStateOf("") }
    var playerId by remember(viewModel.personagem.nome) { mutableStateOf(viewModel.personagem.nome) }
    var connectionState by remember { mutableStateOf(VttConnectionState.DISCONNECTED) }
    var statusMessage by remember { mutableStateOf("Preencha servidor, sala e player para iniciar.") }
    var sessionId by remember { mutableStateOf<String?>(null) }
    var tokenId by remember { mutableStateOf<String?>(null) }
    var needsBind by remember { mutableStateOf(false) }
    var tokenIdBindInput by remember { mutableStateOf("") }
    var bindingToken by remember { mutableStateOf(false) }
    var bootstrapDone by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf(VttActionType.PERICIA) }
    var acaoNome by remember { mutableStateOf("") }
    var alvoTokenId by remember { mutableStateOf("") }
    var modificadorRaw by remember { mutableStateOf("0") }
    var sendingAction by remember { mutableStateOf(false) }
    var confirmActionDialog by remember { mutableStateOf(false) }
    var lastActionSummary by remember { mutableStateOf("Nenhuma acao enviada.") }
    var lastActionWhen by remember { mutableStateOf("-") }
    var lastActionRequestId by remember { mutableStateOf("-") }
    var viewMode by remember { mutableStateOf(VttViewMode.PAINEL) }
    var webReloadTick by remember { mutableStateOf(0) }

    fun nowLabel(): String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

    LaunchedEffect(context) {
        if (!bootstrapDone) {
            val snap = VttSessionStorage.load(context)
            if (snap.serverUrl.isNotBlank()) {
                serverUrl = snap.serverUrl
                environment = VttEnvironment.CUSTOM
            }
            if (snap.webUrl.isNotBlank()) {
                webUrl = snap.webUrl
                environment = VttEnvironment.CUSTOM
            }
            if (snap.roomKey.isNotBlank()) roomKey = snap.roomKey
            if (snap.playerId.isNotBlank()) playerId = snap.playerId
            if (snap.sessionId.isNotBlank()) sessionId = snap.sessionId
            if (snap.tokenId.isNotBlank()) {
                tokenId = snap.tokenId
                tokenIdBindInput = snap.tokenId
            }
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
                needsBind = result.needsBind || tokenId.isNullOrBlank()
                if (!result.tokenId.isNullOrBlank()) tokenIdBindInput = result.tokenId
                Log.i(
                    VTT_UI_LOG,
                    "joinSession success roomKey=${roomKey.trim()} playerId=${playerId.trim()} sessionId=${sessionId.orEmpty()} tokenId=${tokenId.orEmpty()} needsBind=$needsBind"
                )
                VttSessionStorage.save(
                    context,
                    VttSessionSnapshot(
                        serverUrl = serverUrl.trim(),
                        webUrl = webUrl.trim(),
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
                    if (needsBind) append(" VTT exige vinculo de token.")
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
                webUrl = webUrl.trim(),
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
            val baseUrl = webUrl.trim().trimEnd('/')
            val roomParam = Uri.encode(roomKey.trim())
            val playerParam = Uri.encode(playerId.trim())
            val target = if (baseUrl.isBlank()) {
                serverUrl.trim()
            } else if (roomParam.isBlank() || playerParam.isBlank()) {
                baseUrl
            } else {
                "$baseUrl/?roomKey=$roomParam&playerName=$playerParam"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            statusMessage = "VTT aberto no navegador."
            Log.i(VTT_UI_LOG, "openExternalVtt url=$target")
        }.onFailure {
            connectionState = VttConnectionState.ERROR
            statusMessage = "Falha ao abrir navegador para a URL informada."
            Log.w(VTT_UI_LOG, "openExternalVtt failure url=${webUrl.trim()}")
        }
    }

    fun limparSessaoLocal() {
        sessionId = null
        tokenId = null
        needsBind = false
        tokenIdBindInput = ""
        VttSessionStorage.save(
            context,
            VttSessionSnapshot(
                serverUrl = serverUrl.trim(),
                webUrl = webUrl.trim(),
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

    fun vincularTokenNoVtt() {
        val room = roomKey.trim()
        val player = playerId.trim()
        val token = tokenIdBindInput.trim()
        if (connectionState != VttConnectionState.CONNECTED) {
            connectionState = VttConnectionState.ERROR
            statusMessage = "Conecte no VTT antes de vincular token."
            return
        }
        if (room.isBlank() || player.isBlank() || token.isBlank()) {
            connectionState = VttConnectionState.ERROR
            statusMessage = "Preencha sala, player e token para vincular."
            return
        }
        bindingToken = true
        scope.launch {
            VttTokenBindService.bindToken(
                roomKey = room,
                playerId = player,
                tokenId = token,
                baseUrl = serverUrl.trim()
            ).onSuccess { result ->
                tokenId = result.tokenId ?: token
                needsBind = false
                connectionState = VttConnectionState.CONNECTED
                statusMessage = result.message
                VttSessionStorage.save(
                    context,
                    VttSessionSnapshot(
                        serverUrl = serverUrl.trim(),
                        webUrl = webUrl.trim(),
                        roomKey = room,
                        playerId = player,
                        sessionId = sessionId.orEmpty(),
                        tokenId = tokenId.orEmpty()
                    )
                )
                Log.i(VTT_UI_LOG, "tokenBind success roomKey=$room playerId=$player tokenId=${tokenId.orEmpty()}")
            }.onFailure { err ->
                connectionState = VttConnectionState.ERROR
                statusMessage = err.message ?: "Falha ao vincular token."
                Log.w(VTT_UI_LOG, "tokenBind failure roomKey=$room playerId=$player reason=$statusMessage")
            }
            bindingToken = false
        }
    }

    fun validarEnvioAcao(): String? {
        val room = roomKey.trim()
        val player = playerId.trim()
        val token = tokenId?.trim().orEmpty()
        val nome = acaoNome.trim()
        val mod = modificadorRaw.trim().toIntOrNull()

        if (connectionState != VttConnectionState.CONNECTED) {
            return "Conecte no VTT antes de enviar acao."
        }
        if (room.isBlank() || player.isBlank()) {
            return "Preencha sala e player antes de enviar acao."
        }
        if (token.isBlank()) {
            return "Token nao vinculado. Use o campo de vinculo na secao de conexao."
        }
        if (needsBind) {
            return "Sessao exige vinculo de token antes de enviar acao."
        }
        if (nome.isBlank()) {
            return "Informe o nome da acao."
        }
        if (mod == null) {
            return "Modificador invalido."
        }
        return null
    }

    fun enviarAcaoRolagem() {
        val room = roomKey.trim()
        val player = playerId.trim()
        val token = tokenId?.trim().orEmpty()
        val nome = acaoNome.trim()
        val alvo = alvoTokenId.trim().ifBlank { null }
        val mod = modificadorRaw.trim().toIntOrNull()
        val validationError = validarEnvioAcao()

        if (validationError != null || mod == null) {
            statusMessage = validationError ?: "Nao foi possivel validar a acao."
            connectionState = VttConnectionState.ERROR
            return
        }

        sendingAction = true
        val tipo = actionType.name.lowercase()
        scope.launch {
            VttRollService.sendRollRequest(
                request = VttRollRequest(
                    roomKey = room,
                    playerId = player,
                    tokenId = token,
                    tipoAcao = tipo,
                    nomeAcao = nome,
                    modificador = mod,
                    alvoTokenId = alvo
                ),
                baseUrl = serverUrl.trim()
            ).onSuccess { result ->
                statusMessage = buildString {
                    append(result.message)
                    if (!result.requestId.isNullOrBlank()) append(" ReqId: ${result.requestId}.")
                }
                lastActionSummary = "OK ${actionType.label}: ${nome.take(60)} (mod $mod)"
                lastActionWhen = nowLabel()
                lastActionRequestId = result.requestId ?: "-"
                connectionState = VttConnectionState.CONNECTED
                Log.i(
                    VTT_UI_LOG,
                    "rollRequest success roomKey=$room playerId=$player tokenId=$token tipo=$tipo nome=$nome mod=$mod alvo=${alvo.orEmpty()}"
                )
            }.onFailure { err ->
                statusMessage = err.message ?: "Falha ao enviar acao."
                lastActionSummary = "ERRO ${actionType.label}: ${statusMessage.take(80)}"
                lastActionWhen = nowLabel()
                lastActionRequestId = "-"
                connectionState = VttConnectionState.ERROR
                Log.w(
                    VTT_UI_LOG,
                    "rollRequest failure roomKey=$room playerId=$player tokenId=$token tipo=$tipo reason=$statusMessage"
                )
            }
            sendingAction = false
        }
    }

    fun solicitarEnvioAcao() {
        val validationError = validarEnvioAcao()
        if (validationError != null) {
            statusMessage = validationError
            connectionState = VttConnectionState.ERROR
            return
        }
        confirmActionDialog = true
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
        SectionCard(title = "Visual VTT (Embed)") {
            Text(
                text = "Modo local: use o visual embutido para ver mapa/grid sem sair do app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
            ) {
                VttViewMode.entries.forEach { mode ->
                    FilterChip(
                        selected = viewMode == mode,
                        onClick = { viewMode = mode },
                        label = { Text(mode.label) }
                    )
                }
            }

            if (viewMode == VttViewMode.MAPA) {
                val baseUrl = webUrl.trim().trimEnd('/')
                val roomParam = Uri.encode(roomKey.trim())
                val playerParam = Uri.encode(playerId.trim())
                val embedUrl = if (baseUrl.isBlank()) {
                    ""
                } else if (roomParam.isBlank() || playerParam.isBlank()) {
                    baseUrl
                } else {
                    "$baseUrl/?roomKey=$roomParam&playerName=$playerParam"
                }
                if (embedUrl.isBlank()) {
                    Text(
                        text = "Defina URL do visual, sala e player para abrir o embed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                addJavascriptInterface(
                                    object {
                                        @JavascriptInterface
                                        fun onVttEvent(log: String?) {
                                            Log.i(VTT_UI_LOG, "vttBridge event=${log.orEmpty()}")
                                        }
                                    },
                                    "Android"
                                )
                                webViewClient = WebViewClient()
                                webChromeClient = WebChromeClient()
                                loadUrl(embedUrl)
                            }
                        },
                        update = { webView ->
                            val tagUrl = webView.getTag(android.R.id.primary) as? String
                            val targetUrl = "$embedUrl#$webReloadTick"
                            if (tagUrl != targetUrl) {
                                webView.setTag(android.R.id.primary, targetUrl)
                                webView.loadUrl(embedUrl)
                            }
                        }
                    )
                    Button(
                        onClick = { webReloadTick += 1 },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Recarregar visual VTT")
                    }
                }
            } else {
                Text(
                    text = "Modo painel ativo. Use os comandos de conexao e rolagem abaixo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        SectionCard(title = "Conexao VTT") {
            Text(
                text = "Aba VTT (shell) ativa. Integracao de sessao e rolagem sera ligada pelos proximos lotes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Servidor API (URL)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = webUrl,
                onValueChange = { webUrl = it },
                label = { Text("Visual VTT (Web URL)") },
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
                                serverUrl = item.apiDefaultUrl
                                webUrl = item.webDefaultUrl
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
            if (connectionState == VttConnectionState.CONNECTED && (needsBind || tokenId.isNullOrBlank())) {
                Text(
                    text = "VTT informou needsBind=true. Vincule um token para liberar rolagens.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
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
            if (connectionState == VttConnectionState.CONNECTED && (needsBind || tokenId.isNullOrBlank())) {
                OutlinedTextField(
                    value = tokenIdBindInput,
                    onValueChange = { tokenIdBindInput = it },
                    label = { Text("Token ID para vincular") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = { vincularTokenNoVtt() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !bindingToken
                ) {
                    Text(if (bindingToken) "Vinculando..." else "Vincular token")
                }
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
                text = "Painel contextual para acao no VTT.",
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

            Button(
                onClick = { solicitarEnvioAcao() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !sendingAction && connectionState != VttConnectionState.CONNECTING
            ) {
                Text(if (sendingAction) "Enviando..." else "Enviar acao ao VTT")
            }
        }

        SummaryFooterCard(title = "Proximos passos") {
            Text(text = "1. Join de sessao VTT", style = MaterialTheme.typography.bodySmall)
            Text(text = "2. Vinculo player e token", style = MaterialTheme.typography.bodySmall)
            Text(text = "3. Rolagem contextual via contrato v1", style = MaterialTheme.typography.bodySmall)
        }

        SectionCard(title = "Diagnostico local") {
            Text(
                text = "Ultima acao: $lastActionSummary",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Horario: $lastActionWhen | ReqId: $lastActionRequestId",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    lastActionSummary = "Nenhuma acao enviada."
                    lastActionWhen = "-"
                    lastActionRequestId = "-"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Limpar diagnostico")
            }
        }
    }

    if (confirmActionDialog) {
        AlertDialog(
            onDismissRequest = { confirmActionDialog = false },
            title = { Text("Confirmar envio da acao") },
            text = {
                Text(
                    "Tipo: ${actionType.label}\n" +
                        "Acao: ${acaoNome.trim()}\n" +
                        "Modificador: ${modificadorRaw.trim()}\n\n" +
                        "Este envio nao altera os dados canonicos da ficha."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmActionDialog = false
                        enviarAcaoRolagem()
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmActionDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
