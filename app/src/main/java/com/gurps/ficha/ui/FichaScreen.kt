package com.gurps.ficha.ui

import android.Manifest
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.content.pm.ActivityInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import com.gurps.ficha.domain.MestreIAContextFilter
import com.gurps.ficha.ui.components.FichaCustomNavigationBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.R
import com.gurps.ficha.model.PersonagemInterop
import com.gurps.ficha.update.AppUpdateService
import com.gurps.ficha.update.AppUpdateHelper
import com.gurps.ficha.viewmodel.FichaViewModel
import com.gurps.ficha.ui.components.EstadoLive
import com.gurps.ficha.ui.components.EstadoVoz
import com.gurps.ficha.ui.components.GeminiLiveService
import com.gurps.ficha.ui.features.dice3d.ConfigurarDadosDialog
import com.gurps.ficha.ui.components.GeminiLiveTools
import kotlinx.coroutines.launch

// Flag para ligar/desligar a Aba SAGA na compilação do APK.
// Mude para true para mostrar a aba, ou false para escondê-la e desacoplar.
const val HABILITAR_ABA_SAGA = false

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaScreen(viewModel: FichaViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    var vttImmersiveUi by remember { mutableStateOf(false) }
    var showMenuDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showConfigDadosDialog by remember { mutableStateOf(false) }
    var showMestreIADialog by remember { mutableStateOf(false) }
    var updateDialogTitle by remember { mutableStateOf("Atualização") }
    var updateDialogMessage by remember { mutableStateOf("") }
    var updateApkUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var estadoVoz by remember { mutableStateOf(EstadoVoz.OCIOSO) }
    var estadoLive by remember { mutableStateOf(EstadoLive.OCIOSO) }
    var mostrarImagemFullscreen by remember { mutableStateOf(false) }

    // OpenDocument (SAF): explorador de arquivos completo — permite escolher de
    // QUALQUER pasta do telefone (Downloads, WhatsApp, Drive, etc.), não só a galeria.
    val imagemPersonagemPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val store = com.gurps.ficha.data.storage.ImagemPersonagemStore
            val imagens = store.salvarImagem(context, uri)
            if (imagens != null) {
                // Remove os arquivos anteriores, se houver, para não acumular.
                val anteriorRecorte = viewModel.personagem.imagemPersonagemUri
                val anteriorOriginal = viewModel.personagem.imagemPersonagemOriginalUri
                if (anteriorRecorte.isNotBlank() && anteriorRecorte != imagens.recortadaUri) {
                    store.excluirImagem(anteriorRecorte)
                }
                if (anteriorOriginal.isNotBlank() && anteriorOriginal != imagens.originalUri) {
                    store.excluirImagem(anteriorOriginal)
                }
                viewModel.atualizarImagemPersonagem(imagens.recortadaUri, imagens.originalUri)
            } else {
                snackbarHostState.showSnackbar("Falha ao processar a imagem.")
            }
        }
    }
    val geminiLive = remember { GeminiLiveService(context) }
    val geminiLiveTools = remember { GeminiLiveTools(viewModel, context) }

    SideEffect {
        geminiLive.onEstado = { novoEstado ->
            estadoLive = novoEstado
            if (novoEstado == EstadoLive.OUVINDO && !showMestreIADialog) {
                Handler(Looper.getMainLooper()).post { showMestreIADialog = true }
            }
        }
        geminiLive.onTranscricaoUsuario = { texto ->
            // Primeiro fragmento: cria entrada no chat e abre o diálogo
            viewModel.adicionarMensagemVoz(texto, "user")
            Handler(Looper.getMainLooper()).post { showMestreIADialog = true }
        }
        geminiLive.onAtualizarTranscricaoUsuario = { texto ->
            // Fragmentos seguintes e versão final: atualiza a entrada existente
            viewModel.atualizarUltimaMensagemVozUsuario(texto)
            Handler(Looper.getMainLooper()).post { showMestreIADialog = true }
        }
        geminiLive.onRespostaMestre = { texto ->
            viewModel.adicionarMensagemVoz(texto, "model")
            Handler(Looper.getMainLooper()).post { showMestreIADialog = true }
        }
        geminiLive.onToolCall = { nome, args ->
            geminiLiveTools.executar(nome, args)
        }
    }
    DisposableEffect(Unit) { onDispose { geminiLive.encerrar() } }

    val permissaoMicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedida ->
        if (concedida) {
            val ctx = MestreIAContextFilter.gerarContexto(viewModel.personagem, "conversa")
            geminiLive.iniciarSessao(ctx)
        }
    }

    fun iniciarVozComPermissao() {
        if (estadoLive != EstadoLive.OCIOSO && estadoLive != EstadoLive.ERRO) {
            geminiLive.encerrar()
            return
        }
        val permissao = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (permissao == PackageManager.PERMISSION_GRANTED) {
            val ctx = MestreIAContextFilter.gerarContexto(viewModel.personagem, "conversa")
            geminiLive.iniciarSessao(ctx)
        } else {
            permissaoMicLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Check automático de atualização na inicialização
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            AppUpdateService.checkForUpdates().onSuccess { state ->
                if (state.hasUpdate) {
                    updateDialogTitle = "Nova versão disponível"
                    updateDialogMessage = buildString {
                        append("Atual: ${state.currentVersionName} (${state.currentVersionCode})\n")
                        append("Nova: ${state.latestVersionName} (${state.latestVersionCode})")
                        if (!state.notes.isNullOrBlank()) {
                            append("\n\nNotas: ${state.notes}")
                        }
                    }
                    updateApkUrl = state.apkUrl
                    showUpdateDialog = true
                }
            }
        }
    }

    val temAptidaoMagica = viewModel.temAptidaoMagica
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val usarNavegacaoCompacta = if (isPraCegoVariant) {
        false
    } else {
        configuration.screenWidthDp < 390 || density.fontScale > 1.1f
    }
    val tabs = buildList {
        add("Geral")
        add("Traços")
        add("Perícias")
        add("Técnicas")
        if (temAptidaoMagica) add("Magia")
        add("Equip.")
        add("Rolagem")
        if (HABILITAR_ABA_SAGA) add("Saga")
    }
    val selectedTitle = tabs.getOrNull(selectedTab).orEmpty()
    val vttFullscreen = selectedTitle == "VTT" && vttImmersiveUi
    // Lote TOK-6a — MODO JOGO: dentro de uma campanha da Saga, o app vira "jogo em tela cheia"
    // (sem cabeçalho da ficha, sem PontosBar, sem abas). O X no header da campanha (TabSaga) sai.
    // Diferente do VTT, NÃO força landscape — a Saga é vertical.
    val sagaModoJogo = selectedTitle == "Saga" && viewModel.sagaCampanhaAtiva != null
    val hideAppChrome = vttFullscreen || sagaModoJogo
    val maxTabIndex = tabs.lastIndex
    val exportCompativelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val exportResult = runCatching {
                val json = viewModel.exportarFichaJsonCompativelComImagem()
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.writer(Charsets.UTF_8).use { writer -> writer.write(json) }
                }
            }
            val mensagem = if (exportResult.isSuccess) {
                "Ficha exportada (JSON compatível)."
            } else {
                "Falha ao exportar ficha (JSON compatível)."
            }
            snackbarHostState.showSnackbar(
                message = mensagem,
                duration = SnackbarDuration.Short
            )
        }
    }
    val exportVersionadoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val exportResult = runCatching {
                val json = viewModel.exportarFichaJsonVersionadaComImagem()
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.writer(Charsets.UTF_8).use { writer -> writer.write(json) }
                }
            }
            val mensagem = if (exportResult.isSuccess) {
                "Ficha exportada (JSON versionado)."
            } else {
                "Falha ao exportar ficha (JSON versionado)."
            }
            snackbarHostState.showSnackbar(
                message = mensagem,
                duration = SnackbarDuration.Short
            )
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val mensagem = runCatching {
            val json = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).use { it.readText() }
            }.orEmpty()
            if (json.isBlank()) {
                "Arquivo vazio. Selecione um JSON exportado pelo app."
            } else {
                viewModel.importarFichaJson(json) ?: "Ficha importada com sucesso."
            }
        }.getOrElse { "Falha ao importar. Verifique se o arquivo é um JSON válido da ficha." }
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = mensagem,
                duration = SnackbarDuration.Short
            )
        }
    }

    fun compartilharFicha() {
        coroutineScope.launch {
            runCatching {
                // Exporta com a imagem do personagem embutida (vai junto no arquivo).
                val json = viewModel.exportarFichaJsonCompativelComImagem()
                val nomeBase = viewModel.personagem.nome.ifBlank { "ficha_gurps" }
                    .replace(Regex("[^a-zA-Z0-9._-]"), "_")
                val fileName = "${nomeBase}.json"

                // Grava em arquivo temporário no cache
                val cacheDir = context.cacheDir
                val file = java.io.File(cacheDir, fileName)
                file.writeText(json)

                // Obtém URI via FileProvider
                val contentUri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                // Prepara a Intent
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_SUBJECT, "Ficha GURPS: ${viewModel.personagem.nome}")
                    putExtra(Intent.EXTRA_TEXT, "Segue em anexo a ficha de GURPS de ${viewModel.personagem.nome}.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(intent, "Compartilhar Ficha via...")
                context.startActivity(chooser)
            }.onFailure { e ->
                snackbarHostState.showSnackbar("Erro ao compartilhar: ${e.message}")
            }
        }
    }

    LaunchedEffect(maxTabIndex) {
        if (selectedTab > maxTabIndex) {
            selectedTab = maxTabIndex
        }
    }
    LaunchedEffect(selectedTitle) {
        if (selectedTitle != "VTT") {
            vttImmersiveUi = false
        }
    }
    // Orientação landscape é EXCLUSIVA do VTT legado — o Modo Jogo da Saga fica vertical.
    DisposableEffect(vttFullscreen) {
        val previousOrientation = activity?.requestedOrientation
        if (vttFullscreen && activity != null) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        onDispose {
            if (activity != null) {
                activity.requestedOrientation =
                    previousOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!hideAppChrome) {
                val imagemUri = viewModel.personagem.imagemPersonagemUri
                if (imagemUri.isNotBlank()) {
                    CabecalhoComImagem(
                        viewModel = viewModel,
                        imagemUri = imagemUri,
                        mostrarPontos = selectedTitle != "Rolagem",
                        isPraCegoVariant = isPraCegoVariant,
                        onMenuClick = { showMenuDialog = true },
                        onTrocarImagem = {
                            imagemPersonagemPicker.launch(arrayOf("image/*"))
                        },
                        onExpandirImagem = { mostrarImagemFullscreen = true }
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(
                                text = viewModel.personagem.nome.ifBlank { "GURPS - Nova Ficha" },
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        actions = {
                            IconButton(
                                onClick = {
                                    imagemPersonagemPicker.launch(arrayOf("image/*"))
                                },
                                modifier = if (isPraCegoVariant) {
                                    Modifier.semantics { contentDescription = "Adicionar foto do personagem" }
                                } else {
                                    Modifier
                                }
                            ) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = "Adicionar foto",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            IconButton(
                                onClick = { showMenuDialog = true },
                                modifier = if (isPraCegoVariant) {
                                    Modifier.semantics { contentDescription = "Abrir menu da ficha" }
                                } else {
                                    Modifier
                                }
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            if (!hideAppChrome) {
                FichaCustomNavigationBar(
                    tabs = tabs,
                    currentIndex = selectedTab,
                    onTabClick = { index -> selectedTab = index },
                    onMestreIAClick = { showMestreIADialog = true },
                    onMestreIALongPress = { iniciarVozComPermissao() },
                    mestreIAAberto = showMestreIADialog,
                    estadoVoz = estadoVoz,
                    estadoLive = estadoLive,
                    isPraCegoVariant = isPraCegoVariant
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Quando há foto, a linha de pontos já é exibida SOBRE a imagem no
            // cabeçalho (CabecalhoComImagem), então não repetimos a PontosBar aqui.
            val temImagemNoCabecalho = viewModel.personagem.imagemPersonagemUri.isNotBlank()
            if (!hideAppChrome && selectedTitle != "Rolagem" && !temImagemNoCabecalho) {
                PontosBar(viewModel)
            }
            when (selectedTitle) {
                "Geral" -> TabGeral(viewModel)
                "Traços" -> TabTracos(viewModel)
                "Perícias" -> TabPericias(viewModel)
                "Técnicas" -> TabTecnicas(viewModel)
                "Magia" -> TabMagias(viewModel)
                "Equip." -> TabEquipamentos(viewModel)
                "Rolagem" -> TabRolagem(viewModel)
                "Saga" -> TabSaga(viewModel)
                else -> TabGeral(viewModel)
            }
        }
    }

    if (showMenuDialog) {
        MenuDialog(
            onDismiss = { showMenuDialog = false },
            onNovaFicha = {
                viewModel.novaFicha()
                showMenuDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Nova ficha criada.",
                        duration = SnackbarDuration.Short
                    )
                }
            },
            onSalvar = { showMenuDialog = false; showSaveDialog = true },
            onCarregar = { 
                showMenuDialog = false
                viewModel.atualizarListaFichasUnificada()
                showLoadDialog = true 
            },
            onImportar = {
                showMenuDialog = false
                importLauncher.launch(arrayOf("application/json", "text/plain"))
            },
            onCompartilhar = {
                showMenuDialog = false
                compartilharFicha()
            },
            onConfigurarDados = {
                showMenuDialog = false
                showConfigDadosDialog = true
            },
            onVerificarAtualizacao = {
                showMenuDialog = false
                coroutineScope.launch {
                    val result = AppUpdateService.checkForUpdates()
                    result.onSuccess { state ->
                        if (state.hasUpdate) {
                            updateDialogTitle = "Nova versão disponível"
                            updateDialogMessage = buildString {
                                append("Atual: ${state.currentVersionName} (${state.currentVersionCode})\n")
                                append("Nova: ${state.latestVersionName} (${state.latestVersionCode})")
                                if (!state.notes.isNullOrBlank()) {
                                    append("\n\nNotas: ${state.notes}")
                                }
                            }
                            updateApkUrl = state.apkUrl
                        } else {
                            updateDialogTitle = "App atualizado"
                            updateDialogMessage = "Você já está na versão mais recente (${state.currentVersionName})."
                            updateApkUrl = null
                        }
                        showUpdateDialog = true
                    }.onFailure { throwable ->
                        updateDialogTitle = "Falha ao verificar"
                        updateDialogMessage = throwable.message ?: "Não foi possível consultar atualização."
                        updateApkUrl = null
                        showUpdateDialog = true
                    }
                }
            },
        )
    }

    if (showSaveDialog) {
        SalvarDialog(
            nomeAtual = viewModel.personagem.nome,
            viewModel = viewModel,
            onDismiss = { showSaveDialog = false },
            onSalvar = { nome ->
                viewModel.salvarFicha(nome)
                showSaveDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Ficha salva.",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    if (showLoadDialog) {
        CarregarDialog(
            fichasLocais = viewModel.fichasSalvas,
            fichasNuvem = viewModel.fichasNuvem,
            onDismiss = { showLoadDialog = false },
            onCarregar = { nome ->
                viewModel.carregarFicha(nome) { sucesso, msg ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                showLoadDialog = false
            },
            onCarregarNuvem = { nome ->
                showLoadDialog = false
                viewModel.restaurarDaNuvem(nome) { sucesso, msg ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            },
            onExcluir = { nome ->
                viewModel.excluirFicha(nome)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Ficha excluída.",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    if (viewModel.mostrarConfirmacaoLimpezaMagias) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarLimpezaMagiasAoPerderAptidao() },
            title = { Text("Remover magias") },
            text = { Text("Ao perder Aptidão Mágica, as magias serão removidas. Deseja continuar?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmarLimpezaMagiasAoPerderAptidao() }) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarLimpezaMagiasAoPerderAptidao() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text(updateDialogTitle) },
            text = { Text(updateDialogMessage) },
            confirmButton = {
                if (!updateApkUrl.isNullOrBlank()) {
                    TextButton(
                        onClick = {
                            val url = updateApkUrl ?: return@TextButton
                            val fileName = "gurps_update_v${updateDialogMessage.substringAfter("Nova: ").substringBefore(" ")}.apk"
                            AppUpdateHelper.downloadAndInstall(context, url, fileName)
                            showUpdateDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Baixando atualização... Acompanhe na barra de notificações.")
                            }
                        }
                    ) { Text("Atualizar agora") }
                } else {
                    TextButton(onClick = { showUpdateDialog = false }) { Text("Fechar") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showConfigDadosDialog) {
        ConfigurarDadosDialog(onDismiss = { showConfigDadosDialog = false })
    }

    if (showMestreIADialog) {
        LaunchedEffect(Unit) {
            if (estadoLive == EstadoLive.OCIOSO || estadoLive == EstadoLive.ERRO) {
                viewModel.gerarSaudacaoMestreIA()
            }
        }
        DialogMestreIA(
            viewModel = viewModel,
            onDismiss = { showMestreIADialog = false },
            estadoLive = if (BuildConfig.VOZ_BIDIRECIONAL_HABILITADA) estadoLive else EstadoLive.OCIOSO,
            onEncerrarLive = { geminiLive.encerrar() }
        )
    }

    if (viewModel.mostrarDialogRetrato) {
        DialogRetratoIA(
            nomePersonagem = viewModel.personagem.nome.ifBlank { "o personagem" },
            onGerar = { viewModel.gerarRetratoIA() },
            onDispensар = { viewModel.dispensarDialogRetrato() }
        )
    }

    if (viewModel.retratoGerandoStatus.isNotBlank()) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Gerando retrato...") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(viewModel.retratoGerandoStatus, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {}
        )
    }

    if (mostrarImagemFullscreen && viewModel.personagem.imagemPersonagemUri.isNotBlank()) {
        // Tela cheia mostra a imagem INTEIRA (original). Fichas antigas sem
        // original caem na recortada.
        val original = viewModel.personagem.imagemPersonagemOriginalUri
        val recorte = viewModel.personagem.imagemPersonagemUri
        ImagemPersonagemFullscreenDialog(
            imagemUri = original.ifBlank { recorte },
            nomePersonagem = viewModel.personagem.nome,
            onDismiss = { mostrarImagemFullscreen = false }
        )
    }
}

/**
 * Cabeçalho da ficha com a foto do personagem ao fundo (estilo "capa"),
 * com título e a linha de pontos (Iniciais/Gastos/Restantes) por cima,
 * em branco, sobre um gradiente escuro para legibilidade.
 *
 * - Toque na foto -> abre em tela cheia ([onExpandirImagem]).
 * - Ícone de câmera -> trocar a foto.
 * - Ícone de menu -> menu da ficha.
 */
@Composable
private fun CabecalhoComImagem(
    viewModel: FichaViewModel,
    imagemUri: String,
    mostrarPontos: Boolean,
    isPraCegoVariant: Boolean,
    onMenuClick: () -> Unit,
    onTrocarImagem: () -> Unit,
    onExpandirImagem: () -> Unit
) {
    val p = viewModel.personagem
    val restantes = p.pontosRestantes
    val corRestantes = when {
        restantes < 0 -> MaterialTheme.colorScheme.error
        else -> Color.White
    }

    // Altura fixa: mesma faixa que o cabeçalho original (TopAppBar + linha de
    // pontos) ocupava, para NÃO empurrar a ficha pra baixo. A foto preenche
    // essa faixa como fundo (Crop), alinhada ao TOPO (favorece cabeça/rosto).
    // A foto vai ATÉ atrás da status bar (sem statusBarsPadding no Box);
    // só o conteúdo interno (título/pontos) respeita a status bar.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onExpandirImagem)
            .semantics {
                contentDescription = "Foto do personagem. Toque para ampliar."
            }
    ) {
        AsyncImage(
            model = imagemUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )
        // Gradiente escuro de cima e de baixo para o texto branco ficar legível.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        // Linha do topo: título + ações (câmera, menu)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = p.nome.ifBlank { "GURPS - Nova Ficha" },
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f, fill = false)
            )
            Row {
                IconButton(
                    onClick = onTrocarImagem,
                    modifier = if (isPraCegoVariant) {
                        Modifier.semantics { contentDescription = "Trocar foto do personagem" }
                    } else Modifier
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Trocar foto", tint = Color.White)
                }
                IconButton(
                    onClick = onMenuClick,
                    modifier = if (isPraCegoVariant) {
                        Modifier.semantics { contentDescription = "Abrir menu da ficha" }
                    } else Modifier
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                }
            }
        }

        // Linha de pontos, ancorada na parte de baixo da foto.
        if (mostrarPontos) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .semantics {
                        if (isPraCegoVariant) {
                            contentDescription =
                                "Resumo de pontos. Iniciais ${p.pontosIniciais}. Gastos ${p.pontosGastos}. Restantes $restantes."
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "Pontos Iniciais: ${p.pontosIniciais}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                    Text(
                        "Gastos: ${p.pontosGastos}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
                Text(
                    text = "Restantes: $restantes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = corRestantes
                )
            }
        }
    }
}

/**
 * Mostra a foto do personagem em tela cheia; toque em qualquer lugar fecha.
 *
 * Também é a porta de saída da imagem: o retrato mora em filesDir/portraits/,
 * armazenamento privado do app, invisível para a galeria e para qualquer
 * gerenciador de arquivos. O botão de baixar copia a foto para Imagens/GURPS —
 * é o único jeito de o usuário ficar com o retrato do Mestre Pintor fora do app.
 */
@Composable
private fun ImagemPersonagemFullscreenDialog(
    imagemUri: String,
    nomePersonagem: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var salvando by remember { mutableStateOf(false) }

    // Declarado ANTES do launcher de permissão porque o callback dele chama isto.
    val salvarAgora: () -> Unit = {
        if (!salvando) {
            salvando = true
            status = "Salvando na galeria…"
            escopo.launch {
                val resultado = com.gurps.ficha.data.storage.ImagemPersonagemStore
                    .salvarNaGaleria(context, imagemUri, nomePersonagem)
                salvando = false
                status = when (resultado) {
                    com.gurps.ficha.data.storage.ImagemPersonagemStore.ResultadoGaleria.OK ->
                        "Foto salva na galeria, na pasta Imagens, GURPS."
                    com.gurps.ficha.data.storage.ImagemPersonagemStore.ResultadoGaleria.SEM_PERMISSAO ->
                        "Sem permissão para gravar na galeria. Libere o acesso a fotos nas configurações do app."
                    com.gurps.ficha.data.storage.ImagemPersonagemStore.ResultadoGaleria.FALHOU ->
                        "Não foi possível salvar a foto na galeria."
                }
            }
        }
    }

    val permissaoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedida ->
        if (concedida) salvarAgora()
        else status = "Permissão negada. Sem ela o app não consegue gravar na galeria."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss)
        ) {
            AsyncImage(
                model = imagemUri,
                contentDescription = "Foto do personagem em tela cheia",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = {
                        val jaTemPermissao =
                            !com.gurps.ficha.data.storage.ImagemPersonagemStore.precisaPermissaoGaleria() ||
                                ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) == PackageManager.PERMISSION_GRANTED
                        if (salvando) return@IconButton
                        if (jaTemPermissao) salvarAgora()
                        else permissaoLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    // Sem `enabled = false` durante o salvamento: um IconButton
                    // desabilitado deixa o toque VAZAR para o Box de trás, que
                    // fecha a tela cheia. O bloqueio fica no próprio onClick.
                ) {
                    if (salvando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Salvar foto na galeria",
                            tint = Color.White
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                }
            }

            // liveRegion: o TalkBack anuncia o resultado sozinho, sem o usuário
            // precisar caçar o texto na tela.
            if (status.isNotBlank()) {
                Text(
                    text = status,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }
    }
}

@Composable
fun PontosBar(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val restantes = p.pontosRestantes
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val corRestantes = when {
        restantes < 0 -> MaterialTheme.colorScheme.error
        restantes == 0 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = if (isPraCegoVariant) {
            Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription =
                        "Resumo de pontos. Iniciais ${p.pontosIniciais}. Gastos ${p.pontosGastos}. Restantes $restantes."
                }
        } else {
            Modifier.fillMaxWidth()
        },
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Pontos Iniciais: ${p.pontosIniciais}", style = MaterialTheme.typography.bodySmall)
                    Text("Gastos: ${p.pontosGastos}", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "Restantes: $restantes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = corRestantes
                )
            }
        }
    }
}



