package com.gurps.ficha.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.R
import com.gurps.ficha.model.PersonagemInterop
import com.gurps.ficha.update.AppUpdateService
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaScreen(viewModel: FichaViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    var vttImmersiveUi by remember { mutableStateOf(false) }
    var showMenuDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showMestreIADialog by remember { mutableStateOf(false) }
    var updateDialogTitle by remember { mutableStateOf("Atualização") }
    var updateDialogMessage by remember { mutableStateOf("") }
    var updateApkUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val temAptidaoMagica = viewModel.temAptidaoMagica
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val usarNavegacaoCompacta = if (isPraCegoVariant) {
        false
    } else {
        configuration.screenWidthDp < 390 || density.fontScale > 1.1f
    }
    val tabs = if (temAptidaoMagica) {
        listOf("Geral", "Traços", "Perícias", "Técnicas", "Magia", "Equip.", "Rolagem")
    } else {
        listOf("Geral", "Traços", "Perícias", "Técnicas", "Equip.", "Rolagem")
    }
    val selectedTitle = tabs.getOrNull(selectedTab).orEmpty()
    val hideAppChrome = selectedTitle == "VTT" && vttImmersiveUi
    val maxTabIndex = tabs.lastIndex
    val exportCompativelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val exportResult = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.writer(Charsets.UTF_8).use { writer ->
                    writer.write(viewModel.exportarFichaJsonCompativel())
                }
            }
        }
        coroutineScope.launch {
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
        val exportResult = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.writer(Charsets.UTF_8).use { writer ->
                    writer.write(viewModel.exportarFichaJsonVersionada())
                }
            }
        }
        coroutineScope.launch {
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
        runCatching {
            val json = viewModel.exportarFichaJsonCompativel()
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
            coroutineScope.launch {
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
    DisposableEffect(hideAppChrome) {
        val previousOrientation = activity?.requestedOrientation
        if (hideAppChrome && activity != null) {
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
        },
        bottomBar = {
            if (!hideAppChrome) {
                FichaCustomNavigationBar(
                    tabs = tabs,
                    currentIndex = selectedTab,
                    onTabClick = { index -> selectedTab = index },
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
            if (!hideAppChrome && selectedTitle != "Rolagem") {
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
            onCarregar = { showMenuDialog = false; showLoadDialog = true },
            onImportar = {
                showMenuDialog = false
                importLauncher.launch(arrayOf("application/json", "text/plain"))
            },
            onCompartilhar = {
                showMenuDialog = false
                compartilharFicha()
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
            onMestreIA = {
                showMenuDialog = false
                showMestreIADialog = true
            }
        )
    }

    if (showSaveDialog) {
        SalvarDialog(
            nomeAtual = viewModel.personagem.nome,
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
            fichas = viewModel.fichasSalvas,
            onDismiss = { showLoadDialog = false },
            onCarregar = { nome ->
                viewModel.carregarFicha(nome)
                showLoadDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Ficha carregada.",
                        duration = SnackbarDuration.Short
                    )
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
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateApkUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            runCatching { context.startActivity(intent) }
                            showUpdateDialog = false
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

    if (showMestreIADialog) {
        DialogMestreIA(
            viewModel = viewModel,
            onDismiss = { showMestreIADialog = false }
        )
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



