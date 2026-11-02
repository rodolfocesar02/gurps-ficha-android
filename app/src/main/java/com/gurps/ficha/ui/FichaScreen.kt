package com.gurps.ficha.ui

import android.app.Activity
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.update.AppUpdateService
import com.gurps.ficha.update.AppUpdateHelper
import com.gurps.ficha.viewmodel.FichaViewModel
import com.gurps.ficha.ui.features.dice3d.ConfigurarDadosDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaScreen(viewModel: FichaViewModel) {
    /**
     * 🟥 **A aba guarda-se pelo NOME, e não pela posição.**
     *
     * A lista de abas MUDA de tamanho enquanto o aplicativo corre: as "Magias" só
     * entram quando o personagem tem aptidão mágica, e isso é decidido depois de
     * a ficha acabar de carregar. Uma posição guardada antes disso passa a
     * apontar para outra aba — e ninguém vê nada de errado, porque a tela mostra
     * uma aba de verdade, só que a errada.
     *
     * ⚠️ Medido no emulador, no lote CC-5: o link da Mesa pedia a Rolagem, o
     * registro dizia `idx=5` — que era a Rolagem — e um segundo depois o mesmo
     * `idx=5` desenhava o Equipamento, porque a Magia tinha entrado no meio.
     *
     * 🔴 E isto **já acontecia sem link nenhum**: quem estivesse na Rolagem
     * enquanto uma ficha com magia carregava era atirado para o Equipamento.
     */
    // 🔴 `rememberSaveable`, e não `remember`: girar o telefone DESMONTA a tela
    // e monta outra, e o `remember` voltava a "Geral" -- quem estava na Mesa
    // era atirado para a Geral a cada giro. O `Saveable` guarda a escolha fora
    // da tela e a devolve.
    var abaEscolhida by rememberSaveable { mutableStateOf("Geral") }
    var showMenuDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showConfigDadosDialog by remember { mutableStateOf(false) }
    var updateDialogTitle by remember { mutableStateOf("Atualização") }
    var updateDialogMessage by remember { mutableStateOf("") }
    var updateApkUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
    // A aba da Mesa entra sob a mesma condição da Magia — MNA-1, `AAbaDaMesa.kt`.
    val temMesaLigada =
        com.gurps.ficha.ui.features.mesa.aAbaDaMesaAparece(viewModel)
    val tabs = buildList {
        add("Geral")
        add("Traços")
        add("Perícias")
        add("Técnicas")
        if (temAptidaoMagica) add("Magias")
        add("Equipamentos")
        add("Rolagem")
        if (temMesaLigada) add("Mesa")
    }
    // ⚠️ Se a aba escolhida sumir da lista (a Magia, quando a aptidão sai),
    // cai-se na primeira — e não num índice que já não quer dizer nada.
    val selectedTab = tabs.indexOf(abaEscolhida).takeIf { it >= 0 } ?: 0
    val selectedTitle = tabs.getOrNull(selectedTab).orEmpty()
    /**
     * 🟥 **Esconder o cabeçalho NÃO é esconder as abas** — MNA-1b.
     *
     * 🔴 Eu tinha metido a Mesa no `hideAppChrome`, junto do VTT e do Modo Jogo.
     * Era errado, e ele encontrou no aparelho na primeira vez que entrou:
     *
     * > *"entrei na mesa, porém não tenho como voltar pra outras abas, não
     * > aparece na parte de baixo!"*
     *
     * O VTT e o Modo Jogo têm saída própria dentro deles. A Mesa **não tem** — e
     * o botão SAIR dela sai da *sala*, não da *aba*. Esconder a barra de baixo
     * era trancar a pessoa lá dentro, com o aplicativo inteiro do outro lado.
     *
     * ⚠️ O cabeçalho continua escondido, e por uma razão que se vê na tela: a
     * Mesa já tem a barra dela (SALAS / PESSOAS / MENU) e o mapa é o que
     * importa. Duas barras uma em cima da outra num telefone não deixam
     * tabuleiro nenhum à vista.
     *
     * 🔴 E a barra de baixo, agora, **não some nunca**. O `hideAppChrome` só
     * existia para o VTT (apagado em 22/set) e para o Modo Jogo da Saga (que saiu
     * no lote SEM-IA). Sem nenhum dos dois, uma condição ali só serviria para
     * alguém voltar a trancar a pessoa dentro de uma aba.
     */
    val esconderOCabecalho = selectedTitle == "Mesa"
    val maxTabIndex = tabs.lastIndex
    val exportCompativelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val exportResult = runCatching {
                val json = viewModel.exportarFichaJsonCompativel()
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
                val json = viewModel.exportarFichaJsonVersionada()
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
                snackbarHostState.showSnackbar("Erro ao compartilhar: ${e.message}")
            }
        }
    }

    // ⚠️ O clampe do índice saiu daqui: com a aba guardada pelo NOME, ela nunca
    // fica fora da lista — quando o nome some, o `indexOf` acima já cai na
    // primeira aba sozinho.

    /**
     * **A Mesa pediu uma rolagem** — lote CC-5.
     *
     * 🔴 O link `gurpsapp://rolar?...` abre o aplicativo em qualquer aba em que
     * ele tenha ficado. Sem este salto, a pessoa toca no tabuleiro, o aplicativo
     * abre na aba Equipamento, e ela tem de ir procurar a Rolagem sozinha — que
     * e exatamente o trabalho que este lote veio tirar.
     *
     * ⚠️ NAO limpa o pedido aqui: quem o consome e a `TabRolagem`, e limpa-lo
     * antes de ela o ler deixaria a pessoa na aba certa sem o modificador.
     */
    LaunchedEffect(viewModel.pedidoDaMesa) {
        if (viewModel.pedidoDaMesa != null) abaEscolhida = "Rolagem"
    }

    com.gurps.ficha.ui.features.mesa.SaltarParaAMesaQuandoPedirem(
        viewModel, temMesaLigada
    ) { abaEscolhida = it }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!esconderOCabecalho) {
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
            FichaCustomNavigationBar(
                tabs = tabs,
                currentIndex = selectedTab,
                onTabClick = { index -> abaEscolhida = tabs[index] },
                isPraCegoVariant = isPraCegoVariant
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!esconderOCabecalho && selectedTitle != "Rolagem") {
                PontosBar(viewModel)
            }
            when (selectedTitle) {
                "Geral" -> TabGeral(viewModel)
                "Traços" -> TabTracos(viewModel)
                "Perícias" -> TabPericias(viewModel)
                "Técnicas" -> TabTecnicas(viewModel)
                "Magias" -> TabMagias(viewModel)
                "Equipamentos" -> TabEquipamentos(viewModel)
                "Rolagem" -> TabRolagem(viewModel)
                "Mesa" -> com.gurps.ficha.ui.features.mesa.TabMesa(
                    nome = viewModel.mesaNome,
                    token = viewModel.mesaToken,
                    // Saiu da sala: volta para a Rolagem, que é de onde se entra.
                    aoSairDaMesa = { abaEscolhida = "Rolagem" },
                    aoReceberPedido = { viewModel.pedidoDaMesa = it }
                )
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

    // 🔴 O recado da mesa (MESA-45). O envio da ficha e best-effort e corre a
    // parte do salvar -- entao o resultado chega DEPOIS do "Ficha salva.", e tem
    // de aparecer sozinho quando chegar.
    //
    // ⚠️ Duracao longa: e um recado que diz o que fazer ("falta Seu nome"), e
    // dois segundos nao chegam para o ler.
    LaunchedEffect(viewModel.recadoDaMesa) {
        val recado = viewModel.recadoDaMesa ?: return@LaunchedEffect
        viewModel.recadoDaMesa = null
        snackbarHostState.showSnackbar(message = recado, duration = SnackbarDuration.Long)
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
                        "Resumo de pontos. ${p.rotuloDePontosAcessivel}. Gastos ${p.pontosGastos}. Restantes $restantes."
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
                    Text(p.rotuloDePontos, style = MaterialTheme.typography.bodySmall)
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



