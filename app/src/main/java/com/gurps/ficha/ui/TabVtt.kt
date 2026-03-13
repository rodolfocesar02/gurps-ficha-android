package com.gurps.ficha.ui

import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import android.util.Log
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.ConsoleMessage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.gurps.ficha.R
import com.gurps.ficha.vtt.VttSessionService
import com.gurps.ficha.vtt.VttSessionSnapshot
import com.gurps.ficha.vtt.VttSessionStorage
import com.gurps.ficha.vtt.VttRollRequest
import com.gurps.ficha.vtt.VttRollService
import com.gurps.ficha.vtt.VttTokenBindService
import com.gurps.ficha.vtt.VttBridgeCodec
import com.gurps.ficha.vtt.VttHostAutoDetect
import com.gurps.ficha.viewmodel.FichaViewModel
import com.gurps.ficha.ui.UiTokens
import com.google.gson.JsonParser
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
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
        "https://vttaudiovideo-e-ficha-de-gurps-production.up.railway.app",
        "https://surprising-compassion-production-7a88.up.railway.app"
    ),
    CUSTOM("Custom", "", "")
}

private enum class VttActionType(val label: String) {
    TESTE("Teste"),
    PERICIA("Pericia"),
    MAGIA("Magia"),
    DEFESA("Defesa")
}

private data class VttActionOption(
    val key: String,
    val type: VttActionType,
    val nome: String,
    val label: String
)

private const val VTT_UI_LOG = "VttTab"

private fun normalizeProdWebUrl(currentWebUrl: String): String {
    val trimmed = currentWebUrl.trim()
    if (trimmed.isBlank()) return VttEnvironment.PROD.webDefaultUrl
    val host = runCatching { Uri.parse(trimmed).host.orEmpty() }.getOrDefault("")
    return if (
        host.equals("vttaudiovideo-e-ficha-de-gurps-production.up.railway.app", ignoreCase = true) ||
        host.equals("surprising-compassion-production-7a8b.up.railway.app", ignoreCase = true)
    ) {
        VttEnvironment.PROD.webDefaultUrl
    } else {
        currentWebUrl
    }
}

private fun periciaLabel(pericia: com.gurps.ficha.model.PericiaSelecionada): String {
    return if (pericia.especializacao.isBlank()) {
        pericia.nome
    } else {
        "${pericia.nome} (${pericia.especializacao})"
    }
}

private fun normalizeRoomKey(raw: String): String = raw.trim()

private fun isLoopbackUrl(url: String): Boolean {
    val host = runCatching { Uri.parse(url.trim()).host.orEmpty() }.getOrDefault("")
    return host.equals("localhost", ignoreCase = true) || host == "127.0.0.1" || host == "10.0.2.2"
}

private fun replaceLoopbackHost(url: String, newHost: String): String {
    val trimmed = url.trim()
    if (trimmed.isBlank()) return url
    val uri = runCatching { Uri.parse(trimmed) }.getOrNull() ?: return url
    val host = uri.host.orEmpty()
    if (!host.equals("localhost", ignoreCase = true) && host != "127.0.0.1") return url
    return runCatching {
        uri.buildUpon().encodedAuthority(
            if (uri.port > 0) "$newHost:${uri.port}" else newHost
        ).build().toString()
    }.getOrDefault(url)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabVtt(
    viewModel: FichaViewModel,
    onImmersiveSessionChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var environment by remember { mutableStateOf(VttEnvironment.PROD) }
    var serverUrl by remember { mutableStateOf(VttEnvironment.PROD.apiDefaultUrl) }
    var webUrl by remember { mutableStateOf(VttEnvironment.PROD.webDefaultUrl) }
    var roomKey by remember { mutableStateOf("") }
    var playerId by remember(viewModel.personagem.nome) { mutableStateOf(viewModel.personagem.nome) }
    var connectionState by remember { mutableStateOf(VttConnectionState.DISCONNECTED) }
    var statusMessage by remember { mutableStateOf("Informe a sala e toque em Entrar.") }
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
    var webReloadTick by remember { mutableStateOf(0) }
    var embeddedWebView by remember { mutableStateOf<WebView?>(null) }
    var audioAutoJoin by remember { mutableStateOf(true) }
    var audioCommandStatus by remember { mutableStateOf("Nenhum comando enviado.") }
    var lastAudioEvent by remember { mutableStateOf("-") }
    var showConfig by remember { mutableStateOf(false) }
    var webLoadError by remember { mutableStateOf<String?>(null) }
    var webConsoleLast by remember { mutableStateOf<String?>(null) }
    var roomStateJson by remember { mutableStateOf<String?>(null) }
    var rollResultJson by remember { mutableStateOf<String?>(null) }
    var audioStateJson by remember { mutableStateOf<String?>(null) }
    var fichaSyncJson by remember { mutableStateOf<String?>(null) }
    var fichaSyncPlayerId by remember { mutableStateOf<String?>(null) }
    var fichaSyncTokenId by remember { mutableStateOf<String?>(null) }
    var fichaSyncSource by remember { mutableStateOf<String?>(null) }
    var fichaSyncEventId by remember { mutableStateOf(0) }
    var lastSnackbar by remember { mutableStateOf<String?>(null) }
    var participantes by remember { mutableStateOf<List<String>>(emptyList()) }
    var audioSummary by remember { mutableStateOf("Voice off") }
    var selectedTokenId by remember { mutableStateOf<String?>(null) }
    var selectedTokenName by remember { mutableStateOf<String?>(null) }
    var selectedTokenIsOwn by remember { mutableStateOf(false) }
    var showTokenActionDialog by remember { mutableStateOf(false) }
    var selectedActionKey by remember { mutableStateOf<String?>(null) }
    var immersiveMapMode by remember { mutableStateOf(true) }
    var showExitVttDialog by remember { mutableStateOf(false) }
    var autoReconnectEnabled by remember { mutableStateOf(false) }
    var tokenImageUri by remember { mutableStateOf("") }
    var tokenImagePayload by remember { mutableStateOf<String?>(null) }
    var externalOpenAttempted by remember { mutableStateOf(false) }

    val tokenImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION)
        }
        tokenImageUri = uri.toString()
        tokenImagePayload = null
        statusMessage = "Imagem do token selecionada."
    }

    LaunchedEffect(viewModel.personagem.nome) {
        val nome = viewModel.personagem.nome.trim()
        if (nome.isNotBlank()) {
            playerId = nome
        }
    }

    fun nowLabel(): String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

    fun jsEscape(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "")
    }

    fun openExternalVtt() {
        if (serverUrl.isBlank()) {
            connectionState = VttConnectionState.ERROR
            statusMessage = "Defina a URL do servidor para abrir externamente."
            return
        }
        runCatching {
            val effectiveServerUrl = if (isLoopbackUrl(serverUrl)) {
                VttEnvironment.PROD.apiDefaultUrl
            } else {
                serverUrl
            }
            val baseUrl = if (isLoopbackUrl(webUrl)) {
                VttEnvironment.PROD.webDefaultUrl
            } else {
                webUrl
            }.trim().trimEnd('/')
            val roomParam = Uri.encode(roomKey.trim())
            val playerParam = Uri.encode(playerId.trim())
            val target = if (baseUrl.isBlank()) {
                effectiveServerUrl.trim()
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

    fun sha1Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun inferImageExtension(url: String, contentType: String?): String {
        val type = contentType?.lowercase().orEmpty()
        return when {
            type.contains("image/webp") -> "webp"
            type.contains("image/png") -> "png"
            type.contains("image/jpeg") || type.contains("image/jpg") -> "jpg"
            url.lowercase().endsWith(".webp") -> "webp"
            url.lowercase().endsWith(".png") -> "png"
            url.lowercase().endsWith(".jpg") || url.lowercase().endsWith(".jpeg") -> "jpg"
            else -> "png"
        }
    }

    suspend fun cacheMapFromUrl(mapUrl: String): Uri? = withContext(Dispatchers.IO) {
        val safeUrl = mapUrl.trim()
        if (safeUrl.isBlank()) return@withContext null
        val absoluteUrl = if (safeUrl.startsWith("http")) safeUrl else {
            val base = serverUrl.trim().trimEnd('/')
            "$base${if (safeUrl.startsWith("/")) "" else "/"}$safeUrl"
        }
        runCatching {
            val conn = (URL(absoluteUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                instanceFollowRedirects = true
            }
            val code = conn.responseCode
            if (code !in 200..299) {
                conn.disconnect()
                return@runCatching null
            }
            val extension = inferImageExtension(safeUrl, conn.contentType)
            val dir = java.io.File(context.filesDir, "maps").apply { mkdirs() }
            val name = "map_${System.currentTimeMillis()}_${sha1Hex(safeUrl)}.$extension"
            val file = java.io.File(dir, name)
            conn.inputStream.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            conn.disconnect()
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }.getOrNull()
    }

    suspend fun resolveTokenImagePayload(rawUri: String): String? = withContext(Dispatchers.IO) {
        val trimmed = rawUri.trim()
        if (trimmed.isBlank()) return@withContext null
        if (trimmed.startsWith("data:", ignoreCase = true)) return@withContext trimmed
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            return@withContext trimmed
        }

        val uri = runCatching { Uri.parse(trimmed) }.getOrNull() ?: return@withContext null
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: "image/png"
        val bytes = runCatching {
            resolver.openInputStream(uri)?.use { it.readBytes() }
        }.getOrNull() ?: return@withContext null
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        "data:$mime;base64,$base64"
    }

    fun enviarMapaParaWebView(mapUrl: String?, mapImage: String?) {
        val web = embeddedWebView
        if (web == null) {
            Log.w(VTT_UI_LOG, "mapDispatch skipped: webview null")
            return
        }
        val safeUrl = mapUrl?.trim().orEmpty()
        val safeImage = mapImage?.trim().orEmpty()

        fun dispatchPayload(payloadJson: String) {
            val js = """
                (function() {
                  try {
                    const ev = new CustomEvent('mapa_atualizado', { detail: $payloadJson });
                    window.dispatchEvent(ev);
                    return 'sent';
                  } catch(e) {
                    return 'error:' + (e && e.message ? e.message : 'unknown');
                  }
                })();
            """.trimIndent()
            web.evaluateJavascript(js) { raw ->
                val result = raw?.trim('"').orEmpty()
                Log.i(VTT_UI_LOG, "mapDispatch result=$result payload=$payloadJson")
            }
        }

        fun buildPayloadForUrl(url: String): String = "{ \"mapUrl\": \"${jsEscape(url)}\" }"
        fun buildPayloadForImage(image: String): String = "{ \"mapImage\": \"${jsEscape(image)}\" }"

        fun showMapError(msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            Log.w(VTT_UI_LOG, msg)
        }

        suspend fun isUrlReachable(url: String): Boolean = withContext(Dispatchers.IO) {
            val absoluteUrl = if (url.startsWith("http")) url else {
                val base = serverUrl.trim().trimEnd('/')
                "$base${if (url.startsWith("/")) "" else "/"}$url"
            }
            runCatching {
                val headConn = (URL(absoluteUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "HEAD"
                    connectTimeout = 5000
                    readTimeout = 5000
                    instanceFollowRedirects = true
                }
                val headCode = headConn.responseCode
                headConn.disconnect()
                if (headCode in 200..299) return@runCatching true
                if (headCode == 405) {
                    val getConn = (URL(absoluteUrl).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 5000
                        readTimeout = 5000
                        instanceFollowRedirects = true
                    }
                    val getCode = getConn.responseCode
                    getConn.disconnect()
                    return@runCatching getCode in 200..299
                }
                false
            }.getOrDefault(false)
        }

        if (safeUrl.isBlank() && safeImage.isBlank()) {
            dispatchPayload("{}")
            return
        }

        if (safeUrl.isNotBlank()) {
            scope.launch {
                val cached = cacheMapFromUrl(safeUrl)
                if (cached != null) {
                    dispatchPayload(buildPayloadForUrl(cached.toString()))
                    Log.i(VTT_UI_LOG, "mapCache saved uri=$cached")
                    return@launch
                }
                val ok = isUrlReachable(safeUrl)
                if (ok) {
                    dispatchPayload(buildPayloadForUrl(safeUrl))
                } else if (safeImage.isNotBlank()) {
                    showMapError("Mapa por URL indisponivel. Usando fallback local.")
                    dispatchPayload(buildPayloadForImage(safeImage))
                } else {
                    showMapError("Nao foi possivel carregar o mapa por URL.")
                }
            }
            return
        }

        dispatchPayload(buildPayloadForImage(safeImage))
    }

    fun enviarComandoAudioEmbed(action: String) {
        val web = embeddedWebView
        if (web == null) {
            audioCommandStatus = "Visual embed nao carregado."
            return
        }
        val js = """
            (function() {
              const action = '$action';
              window.dispatchEvent(new CustomEvent('gurps-android-audio-command', { detail: { action } }));
              const map = {
                join: ['[data-testid="join-voice"]', '#join-voice', '.join-voice', '[aria-label*="Entrar"][aria-label*="voz"]'],
                toggle_mic: ['[data-testid="toggle-mic"]', '#toggle-mic', '.toggle-mic', '[aria-label*="microfone"]'],
                toggle_deafen: ['[data-testid="toggle-deafen"]', '#toggle-deafen', '.toggle-deafen', '[aria-label*="som"]', '[aria-label*="audio"]']
              };
              const selectors = map[action] || [];
              for (const s of selectors) {
                const el = document.querySelector(s);
                if (el) {
                  el.click();
                  return 'clicked:' + s;
                }
              }
              return 'dispatched-only';
            })();
        """.trimIndent()
        web.evaluateJavascript(js) { raw ->
            val result = raw?.trim('"').orEmpty()
            audioCommandStatus = "Comando ${action}: ${result.ifBlank { "ok" }}"
            Log.i(VTT_UI_LOG, "audioCommand action=$action result=$result")
        }
    }

    fun enviarComandoEmbed(action: String) {
        val web = embeddedWebView ?: return
        val js = """
            (function() {
              try {
                window.dispatchEvent(new CustomEvent('gurps-android-command', { detail: { action: '$action' } }));
                return 'sent';
              } catch (e) {
                return 'error:' + e.message;
              }
            })();
        """.trimIndent()
        web.evaluateJavascript(js) { raw ->
            val result = raw?.trim('"').orEmpty()
            Log.i(VTT_UI_LOG, "embedCommand action=$action result=$result")
        }
    }

    fun enviarAcaoBridgeEmbed(
        room: String,
        player: String,
        token: String,
        tipo: String,
        nome: String,
        mod: Int,
        alvo: String?
    ) {
        val web = embeddedWebView ?: return
        val safeRoom = room.replace("\\", "\\\\").replace("'", "\\'")
        val safePlayer = player.replace("\\", "\\\\").replace("'", "\\'")
        val safeToken = token.replace("\\", "\\\\").replace("'", "\\'")
        val safeTipo = tipo.replace("\\", "\\\\").replace("'", "\\'")
        val safeNome = nome.replace("\\", "\\\\").replace("'", "\\'")
        val safeAlvo = (alvo ?: "").replace("\\", "\\\\").replace("'", "\\'")
        val js = """
            (function() {
              try {
                const payload = {
                  roomKey: '$safeRoom',
                  playerId: '$safePlayer',
                  tokenId: '$safeToken',
                  tipoAcao: '$safeTipo',
                  nomeAcao: '$safeNome',
                  modificador: $mod,
                  alvoTokenId: '$safeAlvo' || null
                };
                const msg = JSON.stringify({ type: 'APP_ROLL', payload });
                window.postMessage(msg, '*');
                window.dispatchEvent(new CustomEvent('gurps-android-command', {
                  detail: { action: 'APP_ROLL', payload: payload }
                }));
                return 'sent';
              } catch (e) {
                return 'error:' + (e && e.message ? e.message : 'unknown');
              }
            })();
        """.trimIndent()
        web.evaluateJavascript(js) { raw ->
            val result = raw?.trim('"').orEmpty()
            Log.i(VTT_UI_LOG, "embedRollCommand tipo=$tipo nome=$nome result=$result")
        }
    }

    fun enviarFichaSnapshot() {
        val web = embeddedWebView ?: return
        val fichaJson = viewModel.exportarFichaJsonCompativel()
        val safeJsonLiteral = VttBridgeCodec.toJavascriptStringLiteral(fichaJson)
        val js = """
            (function() {
              try {
                const payload = $safeJsonLiteral;
                const data = JSON.parse(payload);
                const evt = new CustomEvent('gurps-android-ficha', { detail: data });
                window.dispatchEvent(evt);
                return 'sent';
              } catch (e) {
                return 'error:' + e.message;
              }
            })();
        """.trimIndent()
        web.evaluateJavascript(js) { raw ->
            val result = raw?.trim('"').orEmpty()
            Log.i(VTT_UI_LOG, "fichaSnapshot sent result=$result length=${fichaJson.length}")
        }
    }

    fun enviarJoinBridgeEmbed() {
        val web = embeddedWebView ?: return
        val room = roomKey.trim()
        val player = playerId.trim()
        if (room.isBlank() || player.isBlank()) return
        val safeRoom = room.replace("\\", "\\\\").replace("'", "\\'")
        val safePlayer = player.replace("\\", "\\\\").replace("'", "\\'")
        val tokenName = viewModel.personagem.nome.trim().ifBlank { player }
        val safeTokenName = tokenName.replace("\\", "\\\\").replace("'", "\\'")
        val tokenImageSend = tokenImagePayload ?: tokenImageUri.trim()
        val safeTokenImage = tokenImageSend.replace("\\", "\\\\").replace("'", "\\'")
        val fichaJson = viewModel.exportarFichaJsonCompativel()
        val safeFichaLiteral = VttBridgeCodec.toJavascriptStringLiteral(fichaJson)
        val js = """
            (function() {
              try {
                const payload = {
                  roomKey: '$safeRoom',
                  playerName: '$safePlayer',
                  playerId: '$safePlayer',
                  tokenName: '$safeTokenName',
                  avatarUrl: '$safeTokenImage',
                  tokenImageUrl: '$safeTokenImage',
                  isMaster: false,
                  fichaJson: JSON.parse($safeFichaLiteral)
                };
                const msg = JSON.stringify({ type: 'VTT_JOIN', payload });
                window.postMessage(msg, '*');
                window.dispatchEvent(new CustomEvent('gurps-android-command', {
                  detail: { action: 'VTT_JOIN', payload: payload }
                }));
                return 'sent';
              } catch (e) {
                return 'error:' + (e && e.message ? e.message : 'unknown');
              }
            })();
        """.trimIndent()
        web.evaluateJavascript(js) { raw ->
            val result = raw?.trim('"').orEmpty()
            Log.i(VTT_UI_LOG, "embedJoinCommand room=$room player=$player result=$result")
        }
    }

    fun tratarMensagemBridge(raw: String) {
        val msg = raw.trim()
        if (msg.isBlank()) return
        // Tentativa 1: JSON com {type, payload}
        runCatching {
            val root = JsonParser.parseString(msg).asJsonObject
            val type = root.get("type")?.asString ?: return@runCatching
            val payload = root.get("payload")
            when (type) {
                "ROOM_STATE" -> {
                    roomStateJson = payload?.toString()
                    val payloadObj = payload?.takeIf { it.isJsonObject }?.asJsonObject
                    val participants = payloadObj?.get("participants")?.asJsonArray
                    if (participants != null) {
                        participantes = participants.mapNotNull { it.asString }
                    }
                    val audio = payloadObj?.get("audio")?.asJsonObject
                    if (audio != null) {
                        val playing = audio.get("playing")?.asBoolean
                        val speakerOnly = audio.get("speakerOnly")?.asBoolean
                        val volume = audio.get("volume")?.asNumber
                        audioSummary = "playing=$playing volume=$volume speakerOnly=$speakerOnly"
                    }
                    val mapUrl = payloadObj?.get("mapUrl")?.asString
                    val mapImage = payloadObj?.get("mapImage")?.asString
                    if (!mapUrl.isNullOrBlank() || !mapImage.isNullOrBlank()) {
                        enviarMapaParaWebView(mapUrl, mapImage)
                    }
                }
                "MAPA_ATUALIZADO", "MAP_UPDATED" -> {
                    val payloadObj = payload?.takeIf { it.isJsonObject }?.asJsonObject
                    val mapUrl = payloadObj?.get("mapUrl")?.asString
                    val mapImage = payloadObj?.get("mapImage")?.asString
                    enviarMapaParaWebView(mapUrl, mapImage)
                }
                "ROLL_RESULT" -> {
                    rollResultJson = payload?.toString()
                    val label = payload?.asJsonObject?.get("label")?.asString
                    val resumo = payload?.asJsonObject?.get("textoResumo")?.asString
                        ?: payload?.asJsonObject?.get("resultadoTexto")?.asString
                    val summary = listOfNotNull(label, resumo).joinToString(" ")
                    if (summary.isNotBlank()) {
                        lastSnackbar = "Rolagem: $summary"
                    }
                }
                "TOKEN_SELECTED" -> {
                    selectedTokenId = payload?.asJsonObject?.get("tokenId")?.asString
                    selectedTokenName = payload?.asJsonObject?.get("name")?.asString
                    selectedTokenIsOwn = payload?.asJsonObject?.get("isOwn")?.asBoolean ?: false
                    if (!selectedTokenId.isNullOrBlank()) {
                        // Pré-seleciona o alvo quando for token inimigo.
                        if (!selectedTokenIsOwn) {
                            alvoTokenId = selectedTokenId.orEmpty()
                        }
                        selectedActionKey = null
                        showTokenActionDialog = true
                    }
                }
                "AUDIO_STATE" -> {
                    audioStateJson = payload?.toString()
                    audioSummary = payload?.toString()?.take(80) ?: audioSummary
                }
                "FICHA_SYNC" -> {
                    val payloadObj = payload?.takeIf { it.isJsonObject }?.asJsonObject
                    val ficha = payloadObj?.get("fichaJson")
                    fichaSyncJson = ficha?.toString() ?: payload?.toString()
                    fichaSyncPlayerId = payloadObj?.get("playerId")?.asString
                    fichaSyncTokenId = payloadObj?.get("tokenId")?.asString
                    fichaSyncSource = payloadObj?.get("source")?.asString
                    fichaSyncEventId += 1
                }
            }
            lastAudioEvent = msg
            return
        }
        // Tentativa 2: legado com prefixo
        val legacy = msg
        when {
            legacy.startsWith("ROOM_STATE:") -> roomStateJson = legacy.removePrefix("ROOM_STATE:")
            legacy.startsWith("MAPA_ATUALIZADO:") -> {
                val payloadText = legacy.removePrefix("MAPA_ATUALIZADO:")
                runCatching {
                    val payloadObj = JsonParser.parseString(payloadText).asJsonObject
                    val mapUrl = payloadObj.get("mapUrl")?.asString
                    val mapImage = payloadObj.get("mapImage")?.asString
                    enviarMapaParaWebView(mapUrl, mapImage)
                }
            }
            legacy.startsWith("ROLL_RESULT:") -> {
                rollResultJson = legacy.removePrefix("ROLL_RESULT:")
                lastSnackbar = "Rolagem: $rollResultJson"
            }
            legacy.startsWith("AUDIO_STATE:") -> {
                audioStateJson = legacy.removePrefix("AUDIO_STATE:")
                audioSummary = audioStateJson.orEmpty().take(80)
            }
            legacy.startsWith("FICHA_SYNC:") -> {
                fichaSyncJson = legacy.removePrefix("FICHA_SYNC:")
                fichaSyncPlayerId = null
                fichaSyncTokenId = null
                fichaSyncSource = "legacy"
                fichaSyncEventId += 1
            }
        }
        lastAudioEvent = legacy
    }

    fun checarCanvasWebgl(tentativa: Int = 1) {
        val web = embeddedWebView ?: return
        val js = """
            (function() {
              const canvas = document.querySelector('canvas');
              if (!canvas) return 'no_canvas';
              const gl = canvas.getContext('webgl2') || canvas.getContext('webgl');
              if (gl) return 'webgl_ok';
              return 'canvas_ok';
            })();
        """.trimIndent()
        web.evaluateJavascript(js) { raw ->
            val result = raw?.trim('"').orEmpty()
            if (result.isBlank() || result == "webgl_ok" || result == "canvas_ok") {
                webLoadError = null
                Log.i(VTT_UI_LOG, "webview probe result=$result tentativa=$tentativa")
                return@evaluateJavascript
            }

            Log.w(VTT_UI_LOG, "webview probe result=$result tentativa=$tentativa")
            if (tentativa < 10) {
                web.postDelayed({ checarCanvasWebgl(tentativa + 1) }, 1000)
                return@evaluateJavascript
            }

            webLoadError = "Canvas/WebGL indisponivel: $result"
            if (result == "no_canvas" && !externalOpenAttempted) {
                externalOpenAttempted = true
                statusMessage = "Sem canvas no WebView. Abrindo VTT no navegador..."
                openExternalVtt()
            }
        }
    }

    fun injetarConsoleBridge() {
        val web = embeddedWebView ?: return
        val js = """
            (function() {
              if (window.__gurpsConsoleHook) return;
              window.__gurpsConsoleHook = true;
              const safeSend = function(prefix, args) {
                try {
                  if (window.Android && window.Android.onVttEvent) {
                    const msg = Array.from(args || []).map(a => {
                      if (typeof a === 'string') return a;
                      try { return JSON.stringify(a); } catch (e) { return String(a); }
                    }).join(' ');
                    window.Android.onVttEvent(prefix + msg);
                  }
                } catch (e) {}
              };
              const origError = console.error;
              const origWarn = console.warn;
              const origLog = console.log;
              console.error = function() { safeSend('console_error:', arguments); origError && origError.apply(console, arguments); };
              console.warn = function() { safeSend('console_warn:', arguments); origWarn && origWarn.apply(console, arguments); };
              console.log = function() { safeSend('console_log:', arguments); origLog && origLog.apply(console, arguments); };
              window.addEventListener('error', function(e) {
                if (window.Android && window.Android.onVttEvent) {
                  window.Android.onVttEvent('js_error:' + (e.message || 'erro') + ' @' + (e.filename || ''));
                }
              });
            })();
        """.trimIndent()
        web.evaluateJavascript(js, null)
    }

    fun forcarCanvas2D() {
        val web = embeddedWebView ?: return
        val js = """
            (function() {
              try {
                // Forca Pixi a usar Canvas2D quando disponivel
                if (window.PIXI && window.PIXI.settings && window.PIXI.ENV) {
                  window.PIXI.settings.PREFER_ENV = window.PIXI.ENV.CANVAS;
                }
                // Bloqueia WebGL para o VTT embedado
                const originalGetContext = HTMLCanvasElement.prototype.getContext;
                if (!window.__gurpsWebglDisabled) {
                  HTMLCanvasElement.prototype.getContext = function(type, attrs) {
                    const t = (type || '').toString().toLowerCase();
                    if (t === 'webgl' || t === 'webgl2' || t === 'experimental-webgl') {
                      return null;
                    }
                    return originalGetContext.call(this, type, attrs);
                  };
                  window.__gurpsWebglDisabled = true;
                }
                return 'canvas2d_forced';
              } catch (e) {
                return 'canvas2d_error:' + (e && e.message ? e.message : 'unknown');
              }
            })();
        """.trimIndent()
        web.evaluateJavascript(js) { raw ->
            val result = raw?.trim('"').orEmpty()
            Log.i(VTT_UI_LOG, "canvas2d force result=$result")
        }
    }

    LaunchedEffect(context) {
        if (!bootstrapDone) {
            val snap = VttSessionStorage.load(context)
            if (snap.serverUrl.isNotBlank() && !isLoopbackUrl(snap.serverUrl)) {
                serverUrl = snap.serverUrl
                environment = VttEnvironment.CUSTOM
            }
            if (snap.webUrl.isNotBlank() && !isLoopbackUrl(snap.webUrl)) {
                webUrl = normalizeProdWebUrl(snap.webUrl)
                environment = VttEnvironment.CUSTOM
            }
            if (snap.roomKey.isNotBlank()) roomKey = snap.roomKey
            if (snap.playerId.isNotBlank()) playerId = snap.playerId
            if (snap.sessionId.isNotBlank()) sessionId = snap.sessionId
            if (snap.tokenId.isNotBlank()) {
                tokenId = snap.tokenId
                tokenIdBindInput = snap.tokenId
            }
            if (snap.tokenImageUri.isNotBlank()) {
                tokenImageUri = snap.tokenImageUri
                tokenImagePayload = null
            }
            autoReconnectEnabled = snap.autoReconnect
            if (!sessionId.isNullOrBlank() || !tokenId.isNullOrBlank()) {
                statusMessage = "Sessao local restaurada. Voce pode reconectar."
            }
            if (webUrl != normalizeProdWebUrl(webUrl)) {
                webUrl = normalizeProdWebUrl(webUrl)
            }
            bootstrapDone = true
        }
    }

    LaunchedEffect(fichaSyncEventId) {
        val raw = fichaSyncJson?.trim().orEmpty()
        if (raw.isBlank()) return@LaunchedEffect
        val expectedPlayer = playerId.trim()
        val expectedToken = tokenId?.trim().orEmpty()
        val source = fichaSyncSource.orEmpty()
        val syncPlayer = fichaSyncPlayerId?.trim().orEmpty()
        val syncToken = fichaSyncTokenId?.trim().orEmpty()
        val matchPlayer = syncPlayer.isBlank() || expectedPlayer.isBlank() || syncPlayer.equals(expectedPlayer, ignoreCase = true)
        val matchToken = syncToken.isBlank() ||
            expectedToken.isBlank() ||
            syncToken.equals(expectedToken, ignoreCase = true) ||
            syncToken.equals(viewModel.personagem.nome.trim(), ignoreCase = true)

        if (!matchPlayer || !matchToken) {
            lastSnackbar = "Sync ignorado (alvo diferente). source=${source.ifBlank { "-" }} player=$syncPlayer token=$syncToken"
            Log.i(
                VTT_UI_LOG,
                "fichaSync ignored source=${source.ifBlank { "-" }} expectedPlayer=$expectedPlayer syncPlayer=$syncPlayer expectedToken=$expectedToken syncToken=$syncToken"
            )
            return@LaunchedEffect
        }

        val result = viewModel.importarFichaJson(raw)
        if (!result.isNullOrBlank()) {
            lastSnackbar = result
        } else {
            lastSnackbar = "Ficha sincronizada do VTT. source=${source.ifBlank { "-" }}"
        }
    }

    fun conectarEmModoShell() {
        connectionState = VttConnectionState.CONNECTING
        val normalizedRoomKey = normalizeRoomKey(roomKey)
        if (serverUrl.isBlank() || normalizedRoomKey.isBlank() || playerId.isBlank()) {
            connectionState = VttConnectionState.ERROR
            statusMessage = "Campos obrigatorios: servidor, sala e player."
            return
        }
        roomKey = normalizedRoomKey

        scope.launch {
            if (isLoopbackUrl(serverUrl) || isLoopbackUrl(webUrl)) {
                serverUrl = VttEnvironment.PROD.apiDefaultUrl
                webUrl = VttEnvironment.PROD.webDefaultUrl
                environment = VttEnvironment.PROD
                statusMessage = "Usei o servidor publico automaticamente."
                Log.i(VTT_UI_LOG, "loopback overridden to PROD")
            }

            val detectResult = runCatching {
                if (isLoopbackUrl(serverUrl) || isLoopbackUrl(webUrl)) {
                    statusMessage = "Detectando servidor VTT na rede local..."
                    val detectedHost = VttHostAutoDetect.detectLanHost()
                    if (detectedHost != null) {
                        serverUrl = replaceLoopbackHost(serverUrl, detectedHost)
                        webUrl = replaceLoopbackHost(webUrl, detectedHost)
                        statusMessage = "Servidor detectado em $detectedHost. Conectando..."
                        Log.i(VTT_UI_LOG, "autoHostDetect success host=$detectedHost")
                    } else {
                        connectionState = VttConnectionState.ERROR
                        statusMessage = "Nao encontrei o servidor na rede. Deixe API/Web com IP do PC (ex.: 192.168.x.x)."
                        Log.w(VTT_UI_LOG, "autoHostDetect failure")
                        return@launch
                    }
                }
            }
            if (detectResult.isFailure) {
                connectionState = VttConnectionState.ERROR
                statusMessage = "Falha ao detectar host local. Informe o IP do PC (ex.: 192.168.x.x)."
                Log.e(VTT_UI_LOG, "autoHostDetect crash-safe fallback", detectResult.exceptionOrNull())
                return@launch
            }

            val snap = VttSessionStorage.load(context)
            val snapshotRoom = normalizeRoomKey(snap.roomKey)
            val canReuseSession = snapshotRoom.isNotBlank() && snapshotRoom == normalizedRoomKey
            val previousSessionForJoin = if (canReuseSession) sessionId else null
            val previousTokenForJoin = if (canReuseSession) tokenId else null
            if (!canReuseSession) {
                sessionId = null
                tokenId = null
                needsBind = false
                tokenIdBindInput = ""
                Log.i(
                    VTT_UI_LOG,
                    "joinSession reset previous session due room change snapshotRoom=$snapshotRoom room=$normalizedRoomKey"
                )
            }

            tokenImagePayload = resolveTokenImagePayload(tokenImageUri)

            VttSessionService.joinSession(
                roomKey = normalizedRoomKey,
                playerId = playerId.trim(),
                fichaJsonRaw = viewModel.exportarFichaJsonCompativel(),
                tokenImageUri = tokenImagePayload?.trim()?.ifBlank { null },
                tokenDisplayName = viewModel.personagem.nome.trim().ifBlank { playerId.trim() },
                previousSessionId = previousSessionForJoin,
                previousTokenId = previousTokenForJoin,
                baseUrl = serverUrl.trim()
            ).onSuccess { result ->
                connectionState = VttConnectionState.CONNECTED
                autoReconnectEnabled = true
                roomKey = normalizedRoomKey
                sessionId = result.sessionId
                tokenId = result.tokenId
                needsBind = result.needsBind || tokenId.isNullOrBlank()
                if (!result.tokenId.isNullOrBlank()) tokenIdBindInput = result.tokenId
                Log.i(
                    VTT_UI_LOG,
                    "joinSession success roomKey=${roomKey.trim()} playerId=${playerId.trim()} sessionId=${sessionId.orEmpty()} tokenId=${tokenId.orEmpty()} needsBind=$needsBind"
                )
                enviarJoinBridgeEmbed()
                VttSessionStorage.save(
                    context,
                    VttSessionSnapshot(
                        serverUrl = serverUrl.trim(),
                        webUrl = webUrl.trim(),
                        roomKey = normalizedRoomKey,
                        playerId = playerId.trim(),
                        sessionId = sessionId.orEmpty(),
                        tokenId = tokenId.orEmpty(),
                        tokenImageUri = tokenImageUri.trim(),
                        autoReconnect = true
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
        autoReconnectEnabled = false
        statusMessage = "Desconectado (shell)."
        VttSessionStorage.save(
            context,
            VttSessionSnapshot(
                serverUrl = serverUrl.trim(),
                webUrl = webUrl.trim(),
                roomKey = roomKey.trim(),
                playerId = playerId.trim(),
                sessionId = sessionId.orEmpty(),
                tokenId = tokenId.orEmpty(),
                tokenImageUri = tokenImageUri.trim(),
                autoReconnect = false
            )
        )
        Log.i(VTT_UI_LOG, "disconnect shell roomKey=${roomKey.trim()} playerId=${playerId.trim()}")
    }

    fun sairDoVtt() {
        desconectarEmModoShell()
        immersiveMapMode = false
        showConfig = false
        embeddedWebView?.loadUrl("about:blank")
        statusMessage = "Voce saiu do VTT."
    }

    fun abrirVttNoNavegador() {
        openExternalVtt()
    }

    fun limparSessaoLocal() {
        sessionId = null
        tokenId = null
        autoReconnectEnabled = false
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
                tokenId = "",
                tokenImageUri = tokenImageUri.trim(),
                autoReconnect = false
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
                autoReconnectEnabled = true
                statusMessage = result.message
                VttSessionStorage.save(
                    context,
                    VttSessionSnapshot(
                        serverUrl = serverUrl.trim(),
                        webUrl = webUrl.trim(),
                        roomKey = room,
                        playerId = player,
                        sessionId = sessionId.orEmpty(),
                        tokenId = tokenId.orEmpty(),
                        tokenImageUri = tokenImageUri.trim(),
                        autoReconnect = true
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
        enviarAcaoBridgeEmbed(
            room = room,
            player = player,
            token = token,
            tipo = tipo,
            nome = nome,
            mod = mod,
            alvo = alvo
        )
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
    val isConnected = connectionState == VttConnectionState.CONNECTED
    val vttOnlyMode = isConnected && immersiveMapMode
    LaunchedEffect(vttOnlyMode) {
        onImmersiveSessionChanged(vttOnlyMode)
    }
    val showDetails = showConfig || !isConnected || !immersiveMapMode
    val canAutoReconnect by rememberUpdatedState(
        autoReconnectEnabled &&
            !isConnected &&
            connectionState != VttConnectionState.CONNECTING &&
            roomKey.trim().isNotBlank() &&
            playerId.trim().isNotBlank() &&
            !sessionId.isNullOrBlank()
    )
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && canAutoReconnect) {
                statusMessage = "Retomando sessao VTT..."
                conectarEmModoShell()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val effectiveServerUrl = if (isLoopbackUrl(serverUrl)) {
        VttEnvironment.PROD.apiDefaultUrl
    } else {
        serverUrl
    }
    val baseUrl = if (isLoopbackUrl(webUrl)) {
        VttEnvironment.PROD.webDefaultUrl
    } else {
        webUrl
    }.trim().trimEnd('/')
    val roomParam = Uri.encode(roomKey.trim())
    val playerParam = Uri.encode(playerId.trim())
    val tokenParam = tokenId?.takeIf { it.isNotBlank() }?.let { "&tokenId=${Uri.encode(it)}" }.orEmpty()
    val embedUrl = if (baseUrl.isBlank()) {
        ""
    } else if (roomParam.isBlank() || playerParam.isBlank()) {
        baseUrl
    } else {
        val apiParam = Uri.encode(effectiveServerUrl.trim())
        "$baseUrl/?embed=1&roomKey=$roomParam&playerName=$playerParam$tokenParam&apiUrl=$apiParam"
    }

    if (vttOnlyMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (embedUrl.isBlank()) {
                Text(
                    text = "Informe a sala para abrir o VTT.",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            embeddedWebView = this
                            WebView.setWebContentsDebuggingEnabled(true)
                            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowContentAccess = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            addJavascriptInterface(
                                object {
                                    @JavascriptInterface
                                    fun onVttEvent(log: String?) {
                                        tratarMensagemBridge(log.orEmpty())
                                    }
                                },
                                "Android"
                            )
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    forcarCanvas2D()
                                    view?.postDelayed({ enviarFichaSnapshot() }, 350)
                                    view?.postDelayed({ enviarJoinBridgeEmbed() }, 900)
                                    if (audioAutoJoin) {
                                        view?.postDelayed({ enviarComandoAudioEmbed("join") }, 1300)
                                    }
                                    injetarConsoleBridge()
                                }
                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        webLoadError = error?.description?.toString()
                                        Log.w(
                                            VTT_UI_LOG,
                                            "webview error=${error?.description} url=${request.url}"
                                        )
                                    }
                                }

                                override fun onReceivedHttpError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    errorResponse: WebResourceResponse?
                                ) {
                                    super.onReceivedHttpError(view, request, errorResponse)
                                    if (request?.isForMainFrame == true) {
                                        webLoadError = "HTTP ${errorResponse?.statusCode}"
                                        Log.w(
                                            VTT_UI_LOG,
                                            "webview http=${errorResponse?.statusCode} url=${request.url}"
                                        )
                                    }
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    if (request != null) request.grant(request.resources)
                                }
                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                    if (consoleMessage != null) {
                                        val msg = consoleMessage.message()
                                        webConsoleLast = msg
                                        Log.i(
                                            VTT_UI_LOG,
                                            "webconsole ${consoleMessage.messageLevel()}: $msg @${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}"
                                        )
                                    }
                                    return true
                                }
                            }
                            loadUrl(embedUrl)
                        }
                    },
                    update = { webView ->
                        embeddedWebView = webView
                        val tagUrl = webView.getTag(R.id.vtt_webview_tag) as? String
                        val targetUrl = "$embedUrl#$webReloadTick"
                        if (tagUrl != targetUrl) {
                            webView.setTag(R.id.vtt_webview_tag, targetUrl)
                            webView.loadUrl(embedUrl)
                        }
                    }
                )
            }
            Button(
                onClick = { showExitVttDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Text("Sair do VTT")
            }
        }
    } else StandardTabColumn {
        SectionCard(title = "VTT") {
            Text(
                text = "Digite a sala e toque em Entrar. O VTT abre embutido.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { showConfig = !showConfig },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showConfig) "Ocultar ajustes avancados" else "Mostrar ajustes avancados")
            }
            FilterChip(
                selected = immersiveMapMode,
                onClick = { immersiveMapMode = !immersiveMapMode },
                label = { Text(if (immersiveMapMode) "Modo imersivo ativo" else "Modo imersivo desligado") }
            )
            OutlinedTextField(
                value = roomKey,
                onValueChange = { roomKey = it },
                label = { Text("Sala") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(
                text = "Jogador: ${playerId.ifBlank { "nome da ficha" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (tokenImageUri.isBlank()) {
                    "Imagem do token: nao definida"
                } else {
                    "Imagem do token: definida"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!isConnected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { tokenImagePicker.launch(arrayOf("image/*")) }
                    ) {
                        Text("Escolher imagem")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            tokenImageUri = ""
                            tokenImagePayload = null
                            statusMessage = "Imagem do token removida."
                        },
                        enabled = tokenImageUri.isNotBlank()
                    ) {
                        Text("Remover imagem")
                    }
                }
            } else {
                Text(
                    text = "Para trocar a imagem do token, saia do VTT.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = { showExitVttDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sair para trocar imagem")
                }
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
                    Text("Entrar")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { desconectarEmModoShell() },
                    enabled = connectionState != VttConnectionState.DISCONNECTED
                ) {
                    Text("Desconectar")
                }
            }
            Text(
                text = "Status: $statusLabel",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isConnected) {
                Text(
                    text = "Conecte para abrir o VTT.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (embedUrl.isBlank()) {
                Text(
                    text = "Informe a sala para abrir o VTT.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isConnected && immersiveMapMode) {
                                Modifier.heightIn(min = 920.dp)
                            } else {
                                Modifier.aspectRatio(16f / 9f).heightIn(min = 520.dp)
                            }
                        ),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            embeddedWebView = this
                            WebView.setWebContentsDebuggingEnabled(true)
                            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowContentAccess = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            addJavascriptInterface(
                                object {
                                    @JavascriptInterface
                                    fun onVttEvent(log: String?) {
                                        val msg = log.orEmpty()
                                        Log.i(VTT_UI_LOG, "vttBridge event=$msg")
                                        tratarMensagemBridge(msg)
                                    }

                                    @JavascriptInterface
                                    fun onAudioStatus(status: String?) {
                                        lastAudioEvent = status.orEmpty()
                                        Log.i(VTT_UI_LOG, "vttBridge audioStatus=${status.orEmpty()}")
                                    }
                                },
                                "Android"
                            )
                            webViewClient = object : WebViewClient() {
                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true) {
                                        webLoadError = error?.description?.toString()
                                        Log.w(
                                            VTT_UI_LOG,
                                            "webview error=${error?.description} url=${request.url}"
                                        )
                                    }
                                }

                                override fun onReceivedHttpError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    errorResponse: WebResourceResponse?
                                ) {
                                    super.onReceivedHttpError(view, request, errorResponse)
                                    if (request?.isForMainFrame == true) {
                                        webLoadError = "HTTP ${errorResponse?.statusCode}"
                                        Log.w(
                                            VTT_UI_LOG,
                                            "webview httpError=${errorResponse?.statusCode} url=${request.url}"
                                        )
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    webLoadError = null
                                    forcarCanvas2D()
                                    view?.postDelayed({ enviarFichaSnapshot() }, 350)
                                    view?.postDelayed({ enviarJoinBridgeEmbed() }, 900)
                                    view?.postDelayed({ checarCanvasWebgl() }, 1200)
                                    if (audioAutoJoin) {
                                        view?.postDelayed({
                                            enviarComandoAudioEmbed("join")
                                        }, 1300)
                                    }
                                    injetarConsoleBridge()
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    if (request == null) return
                                    val audioResources = request.resources
                                        ?.filter { it == PermissionRequest.RESOURCE_AUDIO_CAPTURE }
                                        ?.toTypedArray()
                                    if (!audioResources.isNullOrEmpty()) {
                                        request.grant(audioResources)
                                        Log.i(VTT_UI_LOG, "webview permission granted audio capture")
                                    } else {
                                        request.grant(request.resources)
                                        Log.i(
                                            VTT_UI_LOG,
                                            "webview permission granted resources=${request.resources?.joinToString()}"
                                        )
                                    }
                                }

                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                    val msg = consoleMessage?.message()?.take(200).orEmpty()
                                    webConsoleLast = if (msg.isBlank()) null else msg
                                    Log.i(VTT_UI_LOG, "webview console=${msg}")
                                    return super.onConsoleMessage(consoleMessage)
                                }
                            }
                            loadUrl(embedUrl)
                        }
                    },
                    update = { webView ->
                        embeddedWebView = webView
                        val tagUrl = webView.getTag(R.id.vtt_webview_tag) as? String
                        val targetUrl = "$embedUrl#$webReloadTick"
                        if (tagUrl != targetUrl) {
                            webView.setTag(R.id.vtt_webview_tag, targetUrl)
                            webView.loadUrl(embedUrl)
                        }
                    }
                )
                Button(
                    onClick = { webReloadTick += 1 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Recarregar mapa")
                }
                if (!webLoadError.isNullOrBlank()) {
                    val friendly = if (webLoadError?.contains("canvas", ignoreCase = true) == true) {
                        "Mapa aguardando carregamento pelo mestre."
                    } else webLoadError.orEmpty()
                    Text(
                        text = friendly,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (webLoadError?.contains("canvas", ignoreCase = true) == true)
                            MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                }
                if (!webConsoleLast.isNullOrBlank()) {
                    Text(
                        text = "Console: ${webConsoleLast}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!roomStateJson.isNullOrBlank()) {
                    Text(
                        text = "Sala: ${roomStateJson}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (participantes.isNotEmpty()) {
                    Text(
                        text = "Participantes: ${participantes.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!rollResultJson.isNullOrBlank()) {
                    Text(
                        text = "Última rolagem: ${rollResultJson}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!audioStateJson.isNullOrBlank()) {
                    Text(
                        text = "Áudio: ${audioSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (showDetails) {
                    Text(
                        text = "Audio (Embed)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!lastSnackbar.isNullOrBlank()) {
                        Text(
                            text = lastSnackbar!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
                    ) {
                        FilterChip(
                            selected = audioAutoJoin,
                            onClick = { audioAutoJoin = !audioAutoJoin },
                            label = { Text(if (audioAutoJoin) "Auto-join ativo" else "Auto-join desligado") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
                    ) {
                        Button(
                            onClick = { enviarComandoAudioEmbed("join") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Entrar") }
                        Button(
                            onClick = { enviarComandoAudioEmbed("toggle_mic") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Mic") }
                        Button(
                            onClick = { enviarComandoAudioEmbed("toggle_deafen") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Som") }
                    }
                    Text(
                        text = audioCommandStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Evento JS: $lastAudioEvent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Controles do mapa",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
                    ) {
                        Button(onClick = { enviarComandoEmbed("ping") }, modifier = Modifier.weight(1f)) { Text("Ping") }
                        Button(onClick = { enviarComandoEmbed("zoom_in") }, modifier = Modifier.weight(1f)) { Text("Zoom +") }
                        Button(onClick = { enviarComandoEmbed("zoom_out") }, modifier = Modifier.weight(1f)) { Text("Zoom -") }
                    }
                }
            }
        }

        if (showConfig) {
            SectionCard(title = "Ajustes Avancados VTT") {
                Text(
                    text = "Use apenas se precisar depurar. Em uso normal, basta Sala + Entrar.",
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
        }

        if (showDetails) SectionCard(title = "Acoes de Rolagem (VTT)") {
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

        if (showDetails) SummaryFooterCard(title = "Proximos passos") {
            Text(text = "1. Join de sessao VTT", style = MaterialTheme.typography.bodySmall)
            Text(text = "2. Vinculo player e token", style = MaterialTheme.typography.bodySmall)
            Text(text = "3. Rolagem contextual via contrato v1", style = MaterialTheme.typography.bodySmall)
        }

        if (showDetails) SectionCard(title = "Diagnostico local") {
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

        if (showExitVttDialog) {
            AlertDialog(
                onDismissRequest = { showExitVttDialog = false },
                title = { Text("Sair do VTT?") },
                text = {
                    Text("Voce sera desconectado da sala atual. Deseja continuar?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitVttDialog = false
                            sairDoVtt()
                        }
                    ) { Text("Sair") }
                },
                dismissButton = {
                    TextButton(onClick = { showExitVttDialog = false }) { Text("Cancelar") }
                }
            )
        }

        if (showTokenActionDialog) {
            AlertDialog(
                onDismissRequest = { showTokenActionDialog = false },
                title = { Text("Ações no VTT") },
                text = {
                    val personagem = viewModel.personagem
                    val nivelAptidaoMagica = viewModel.nivelAptidaoMagica
                    val pericias = personagem.pericias.mapIndexed { index, pericia ->
                        val nivel = pericia.calcularNivel(personagem)
                        VttActionOption(
                            key = "pericia_${pericia.definicaoId}_$index",
                            type = VttActionType.PERICIA,
                            nome = periciaLabel(pericia),
                            label = "${periciaLabel(pericia)} (NH $nivel)"
                        )
                    }
                    val magias = personagem.magias.mapIndexedNotNull { index, magia ->
                        val definicao = viewModel.dataRepository.getMagiaPorId(magia.definicaoId)
                        if (definicao != null && !viewModel.prereqsSatisfied(definicao)) return@mapIndexedNotNull null
                        val nivel = magia.calcularNivel(personagem, nivelAptidaoMagica)
                        VttActionOption(
                            key = "magia_${magia.definicaoId}_$index",
                            type = VttActionType.MAGIA,
                            nome = magia.nome,
                            label = "${magia.nome} (NH $nivel)"
                        )
                    }
                    val apara = personagem.defesasAtivas.calcularApara(personagem)
                    val bloqueio = personagem.defesasAtivas.calcularBloqueio(personagem)
                    val esquiva = personagem.defesasAtivas.calcularEsquiva(personagem)
                    val defesas = buildList {
                        if (apara != null) {
                            add(
                                VttActionOption(
                                    key = "defesa_apara",
                                    type = VttActionType.DEFESA,
                                    nome = "Apara",
                                    label = "Apara ($apara)"
                                )
                            )
                        }
                        if (bloqueio != null) {
                            add(
                                VttActionOption(
                                    key = "defesa_bloqueio",
                                    type = VttActionType.DEFESA,
                                    nome = "Bloqueio",
                                    label = "Bloqueio ($bloqueio)"
                                )
                            )
                        }
                        add(
                            VttActionOption(
                                key = "defesa_esquiva",
                                type = VttActionType.DEFESA,
                                nome = "Esquiva",
                                label = "Esquiva ($esquiva)"
                            )
                        )
                    }

                    StandardDialogColumn {
                        Text(
                            text = if (selectedTokenIsOwn)
                                "Seu token: ${selectedTokenName.orEmpty()}"
                            else
                                "Alvo: ${selectedTokenName.orEmpty()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Toque em uma ação para preparar a rolagem.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
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
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (pericias.isNotEmpty()) {
                                Text(
                                    text = "Pericias",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                pericias.forEach { option ->
                                    FilterChip(
                                        selected = selectedActionKey == option.key,
                                        onClick = {
                                            selectedActionKey = option.key
                                            actionType = option.type
                                            acaoNome = option.nome
                                            modificadorRaw = "0"
                                        },
                                        label = { Text(option.label) }
                                    )
                                }
                            }
                            if (magias.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Magias",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                magias.forEach { option ->
                                    FilterChip(
                                        selected = selectedActionKey == option.key,
                                        onClick = {
                                            selectedActionKey = option.key
                                            actionType = option.type
                                            acaoNome = option.nome
                                            modificadorRaw = "0"
                                        },
                                        label = { Text(option.label) }
                                    )
                                }
                            }
                            if (defesas.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Defesas",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                defesas.forEach { option ->
                                    FilterChip(
                                        selected = selectedActionKey == option.key,
                                        onClick = {
                                            selectedActionKey = option.key
                                            actionType = option.type
                                            acaoNome = option.nome
                                            modificadorRaw = "0"
                                        },
                                        label = { Text(option.label) }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = acaoNome,
                            onValueChange = { acaoNome = it },
                            label = { Text("Nome da ação") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = modificadorRaw,
                            onValueChange = { modificadorRaw = it },
                            label = { Text("Modificador") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showTokenActionDialog = false
                            enviarAcaoRolagem()
                        }
                    ) { Text("Enviar") }
                },
                dismissButton = {
                    TextButton(onClick = { showTokenActionDialog = false }) {
                        Text("Fechar")
                    }
                }
            )
        }
}
