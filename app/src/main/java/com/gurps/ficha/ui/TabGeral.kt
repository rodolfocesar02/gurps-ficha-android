package com.gurps.ficha.ui

import com.gurps.ficha.model.modForcaTotal
import com.gurps.ficha.model.modDestrezaTotal
import com.gurps.ficha.model.modInteligenciaTotal
import com.gurps.ficha.model.modVitalidadeTotal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlin.math.abs
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.domain.rules.VelocidadeEDeslocamento

@Composable
fun TabGeral(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val isCompactScreen = LocalConfiguration.current.screenWidthDp <= 360
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val outerPadding = if (isCompactScreen) 10.dp else 12.dp
    val contentSpacing = if (isCompactScreen) 8.dp else 10.dp
    val rowSpacing = if (isCompactScreen) 6.dp else 8.dp
    val dialogPadding = if (isCompactScreen) 8.dp else 10.dp
    val dialogSpacing = if (isCompactScreen) 3.dp else 4.dp
    
    var nomeInput by rememberSaveable { mutableStateOf(p.nome) }
    var nomeEmFoco by remember { mutableStateOf(false) }
    var jogadorInput by rememberSaveable { mutableStateOf(p.jogador) }
    var jogadorEmFoco by remember { mutableStateOf(false) }
    var pontosInput by rememberSaveable { mutableStateOf(p.pontosIniciais.toString()) }
    var ultimoPontosValidos by rememberSaveable { mutableStateOf(p.pontosIniciais.toString()) }
    var pontosEmFoco by remember { mutableStateOf(false) }
    // Lote GER-1: o XP que o Mestre deu e o NT da campanha.
    var xpInput by rememberSaveable { mutableStateOf(p.xpGanhos.toString()) }
    var xpEmFoco by remember { mutableStateOf(false) }
    var ntInput by rememberSaveable { mutableStateOf(p.nivelTecnologico.toString()) }
    var ntEmFoco by remember { mutableStateOf(false) }
    var showAnotacoesDialog by remember { mutableStateOf(false) }
    var showResumoDialog by remember { mutableStateOf(false) }
    var showBasesDialog by remember { mutableStateOf(false) }
    var showHistoricoDialog by remember { mutableStateOf(false) }
    // Lote DESL-2: a lista inteira de deslocamentos, so leitura.
    var showDeslocamentosDialog by remember { mutableStateOf(false) }
    var showConfirmLimparHistorico by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            viewModel.exportarHistoricoParaTxt(uri, context)
        }
    }

    LaunchedEffect(p.pontosIniciais) {
        if (!pontosEmFoco) {
            pontosInput = p.pontosIniciais.toString()
            ultimoPontosValidos = pontosInput
        }
    }
    
    LaunchedEffect(p.nome) {
        if (!nomeEmFoco) nomeInput = p.nome
    }
    
    LaunchedEffect(p.jogador) {
        if (!jogadorEmFoco) jogadorInput = p.jogador
    }

    // ⚠️ O XP tambem muda por FORA desta tela: o Narrador da Saga premia o heroi
    // (`sagaConcederXp`). Sem este efeito, o campo mostraria o valor velho ate
    // alguem trocar de aba.
    LaunchedEffect(p.xpGanhos) {
        if (!xpEmFoco) xpInput = p.xpGanhos.toString()
    }

    LaunchedEffect(p.nivelTecnologico) {
        if (!ntEmFoco) ntInput = p.nivelTecnologico.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .rolagemVertical()
            .padding(outerPadding),
        verticalArrangement = Arrangement.spacedBy(contentSpacing)
    ) {
        SectionCard(title = "") {
            // Lote GER-2: os cinco campos deste cartao usam o [AppCampoCompacto].
            // O `OutlinedTextField` de fabrica tem 56 dp de altura minima e 16 dp de
            // respiro interno em cima e embaixo -- nenhum dos dois e parametro. Cinco
            // deles empilhados empurravam os atributos para fora da tela.
            AppCampoCompacto(
                value = nomeInput,
                onValueChange = { nomeInput = it },
                label = "Nome do Personagem",
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        val perdeuFoco = nomeEmFoco && !focusState.isFocused
                        nomeEmFoco = focusState.isFocused
                        if (perdeuFoco) {
                            viewModel.atualizarNome(nomeInput)
                        }
                    }
            )
            // ⚠️ 8 dp, e nao 4: com o campo baixo o rotulo flutuante do de baixo
            // quase encosta na moldura do de cima. O respiro que encolheu foi o de
            // DENTRO da caixa; ENTRE as caixas ele precisa continuar visivel.
            Spacer(modifier = Modifier.height(8.dp))
            AppCampoCompacto(
                value = jogadorInput,
                onValueChange = { jogadorInput = it },
                label = "Jogador",
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        val perdeuFoco = jogadorEmFoco && !focusState.isFocused
                        jogadorEmFoco = focusState.isFocused
                        if (perdeuFoco) {
                            viewModel.atualizarJogador(jogadorInput)
                        }
                    }
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Lote GER-1: os tres numeros da campanha na mesma linha.
            //
            // ⚠️ Os tres tem larguras IGUAIS (`weight(1f)`) de proposito. Antes o
            // "Pontos" tinha 100.dp fixos, e com tres campos a largura fixa
            // deixaria o ultimo espremido em tela pequena.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(rowSpacing)
            ) {
                AppCampoCompacto(
                    value = pontosInput,
                    onValueChange = {
                        val somenteDigitos = it.filter(Char::isDigit).take(4)
                        pontosInput = somenteDigitos
                        if (somenteDigitos.isNotBlank()) {
                            ultimoPontosValidos = somenteDigitos
                        }
                    },
                    label = "Pontos",
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            val perdeuFoco = pontosEmFoco && !focusState.isFocused
                            pontosEmFoco = focusState.isFocused
                            if (perdeuFoco) {
                                val valor = pontosInput.toIntOrNull()
                                if (valor != null) {
                                    viewModel.atualizarPontosIniciais(valor)
                                } else {
                                    pontosInput = ultimoPontosValidos
                                }
                            }
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                AppCampoCompacto(
                    value = xpInput,
                    onValueChange = { xpInput = it.filter(Char::isDigit).take(4) },
                    label = "XP",
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            val perdeuFoco = xpEmFoco && !focusState.isFocused
                            xpEmFoco = focusState.isFocused
                            if (perdeuFoco) {
                                // Campo vazio vale ZERO, e nao "nao mexer": apagar o
                                // XP tem de zerar o XP, senao o jogador nao consegue
                                // desfazer um numero digitado errado.
                                viewModel.atualizarXpGanhos(xpInput.toIntOrNull() ?: 0)
                            }
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                AppCampoCompacto(
                    value = ntInput,
                    onValueChange = { ntInput = it.filter(Char::isDigit).take(2) },
                    label = "NT",
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            val perdeuFoco = ntEmFoco && !focusState.isFocused
                            ntEmFoco = focusState.isFocused
                            if (perdeuFoco) {
                                // Vazio volta ao padrao do livro, nao a zero: NT 0 e a
                                // Idade da Pedra, e ninguem apaga o campo querendo isso.
                                viewModel.atualizarNivelTecnologico(
                                    ntInput.toIntOrNull() ?: p.nivelTecnologico
                                )
                            }
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        SectionCard(title = "Atributos Primarios") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(
                    onClick = { showBasesDialog = true },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                ) {
                    Text("Definir Base")
                }
            }
            if (isPraCegoVariant) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AtributoPrimarioLinearCard("ST", p.st, (p.forca - p.forcaBase) * 10) { delta ->
                        viewModel.atualizarForca(delta - p.modeloRacial.modForcaTotal())
                    }
                    AtributoPrimarioLinearCard("DX", p.dx, (p.destreza - p.destrezaBase) * 20) { delta ->
                        viewModel.atualizarDestreza(delta - p.modeloRacial.modDestrezaTotal())
                    }
                    AtributoPrimarioLinearCard("IQ", p.iq, (p.inteligencia - p.inteligenciaBase) * 20) { delta ->
                        viewModel.atualizarInteligencia(delta - p.modeloRacial.modInteligenciaTotal())
                    }
                    AtributoPrimarioLinearCard("HT", p.ht, (p.vitalidade - p.vitalidadeBase) * 10) { delta ->
                        viewModel.atualizarVitalidade(delta - p.modeloRacial.modVitalidadeTotal())
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AtributoEditor("ST", p.st, (p.forca - p.forcaBase) * 10) { delta ->
                        viewModel.atualizarForca(delta - p.modeloRacial.modForcaTotal())
                    }
                    AtributoEditor("DX", p.dx, (p.destreza - p.destrezaBase) * 20) { delta ->
                        viewModel.atualizarDestreza(delta - p.modeloRacial.modDestrezaTotal())
                    }
                    AtributoEditor("IQ", p.iq, (p.inteligencia - p.inteligenciaBase) * 20) { delta ->
                        viewModel.atualizarInteligencia(delta - p.modeloRacial.modInteligenciaTotal())
                    }
                    AtributoEditor("HT", p.ht, (p.vitalidade - p.vitalidadeBase) * 10) { delta ->
                        viewModel.atualizarVitalidade(delta - p.modeloRacial.modVitalidadeTotal())
                    }
                }
            }
        }

        SectionCard(title = "Atributos Secundarios") {
            if (isPraCegoVariant) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AtributoSecundarioLinearCard("PV", p.st, p.modPontosVida, p.pontosVida, 2) { delta ->
                        viewModel.atualizarModPontosVida(delta)
                    }
                    AtributoSecundarioLinearCard("Von", p.iq, p.modVontade, p.vontade, 5) { delta ->
                        viewModel.atualizarModVontade(delta)
                    }
                    AtributoSecundarioLinearCard("Per", p.iq, p.modPercepcao, p.percepcao, 5) { delta ->
                        viewModel.atualizarModPercepcao(delta)
                    }
                    AtributoSecundarioLinearCard("PF", p.ht, p.modPontosFadiga, p.pontosFadiga, 3) { delta ->
                        viewModel.atualizarModPontosFadiga(delta)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AtributoSecundarioEditor("PV", p.st, p.modPontosVida, p.pontosVida, 2) { delta ->
                        viewModel.atualizarModPontosVida(delta)
                    }
                    AtributoSecundarioEditor("Von", p.iq, p.modVontade, p.vontade, 5) { delta ->
                        viewModel.atualizarModVontade(delta)
                    }
                    AtributoSecundarioEditor("Per", p.iq, p.modPercepcao, p.percepcao, 5) { delta ->
                        viewModel.atualizarModPercepcao(delta)
                    }
                    AtributoSecundarioEditor("PF", p.ht, p.modPontosFadiga, p.pontosFadiga, 3) { delta ->
                        viewModel.atualizarModPontosFadiga(delta)
                    }
                }
            }

            // 🔴 Lote ATR-1: os dois que o livro manda comprar com 5 pontos e a
            // ficha só deixava LER. A regra existia inteira — o modelo, o custo
            // em `CharacterRules` e os setters do ViewModel — e a tela nunca
            // perguntou. Quinta vez que este formato aparece no projeto.
            Spacer(modifier = Modifier.height(8.dp))
            CompraDeVelocidadeEDeslocamento(
                modVelocidade = p.modVelocidadeBasica,
                velocidadeFinal = p.velocidadeBasica,
                modDeslocamento = p.modDeslocamentoBasico,
                deslocamentoFinal = p.deslocamentoBasico,
                onVelocidade = { viewModel.atualizarModVelocidadeBasica(it) },
                onDeslocamento = { viewModel.atualizarModDeslocamentoBasico(it) }
            )
        }

        SectionCard(title = "Caracteristicas Derivadas") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CaracteristicaDisplay("Vel. Basica", String.format("%.2f", p.velocidadeBasica), "Velocidade Básica: ${String.format("%.2f", p.velocidadeBasica)}")
                // Lote DESL-2: o numero e o Deslocamento JA DESCONTADO pela carga
                // que ele carrega agora -- e a pergunta que o jogador faz ("quanto
                // eu ando?"). Tocar abre a lista inteira, so leitura.
                val deslocAgora = com.gurps.ficha.domain.rules.DeslocamentosRules.deslocamentoAtual(p)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showDeslocamentosDialog = true }
                        .semantics {
                            contentDescription = "Deslocamento: $deslocAgora metros por segundo. " +
                                "Toque para ver todos os tipos de deslocamento."
                        }
                ) {
                    CaracteristicaDisplay("Desloc. ▸", "$deslocAgora m/s", "")
                }
                CaracteristicaDisplay("BC", String.format("%.1f kg", p.baseCarga), "Carga Básica: ${String.format("%.1f", p.baseCarga)} quilos")
                if (p.modificadorTamanho != 0) {
                    val mtLabel = if (p.modificadorTamanho > 0) "+${p.modificadorTamanho}" else "${p.modificadorTamanho}"
                    CaracteristicaDisplay("MT", mtLabel, "Modificador de Tamanho: $mtLabel (bônus para ser acertado em combate)")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CaracteristicaDisplay("Dano GdP", p.danoGdP, "Dano de Golpe de Ponta: ${p.danoGdP}")
                CaracteristicaDisplay("Dano GeB", p.danoGeB, "Dano de Golpe de Balanço: ${p.danoGeB}")
            }
        }

        Button(
            onClick = { showAnotacoesDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Anotacoes")
        }

        Button(
            onClick = { showResumoDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Resumo de Pontos")
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showDeslocamentosDialog) {
        com.gurps.ficha.ui.features.traits.DialogoDeslocamentos(
            personagem = p,
            onDismiss = { showDeslocamentosDialog = false }
        )
    }

    if (showAnotacoesDialog) {
        Dialog(
            onDismissRequest = { showAnotacoesDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(0.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dialogPadding),
                    verticalArrangement = Arrangement.spacedBy(dialogSpacing)
                ) {
                    Text(
                        "Anotacoes",
                        style = if (isCompactScreen) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .rolagemVertical(),
                        verticalArrangement = Arrangement.spacedBy(dialogSpacing)
                    ) {
                        OutlinedTextField(
                            value = p.campanha,
                            onValueChange = { viewModel.atualizarCampanha(it.take(200)) },
                            label = { Text("Campanha (max 200)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3
                        )
                        OutlinedTextField(
                            value = p.historico,
                            onValueChange = { viewModel.atualizarHistorico(it.take(1000)) },
                            label = { Text("Historia (max 1000)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                        OutlinedTextField(
                            value = p.aparencia,
                            onValueChange = { viewModel.atualizarAparencia(it.take(1000)) },
                            label = { Text("Aparencia (max 1000)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                        OutlinedTextField(
                            value = p.notas,
                            onValueChange = { viewModel.atualizarNotas(it.take(1000)) },
                            label = { Text("Notas (max 1000)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showAnotacoesDialog = false }) {
                            Text("Fechar")
                        }
                        TextButton(
                            onClick = { showHistoricoDialog = true },
                            modifier = Modifier.semantics { contentDescription = "Abrir Histórico de Alterações" }
                        ) {
                            Text("Ver Histórico de Alterações")
                        }
                    }
                }
            }
        }
    }

    if (showHistoricoDialog) {
        Dialog(
            onDismissRequest = { showHistoricoDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(modifier = Modifier.fillMaxSize().padding(16.dp), shape = RoundedCornerShape(12.dp)) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            IconButton(
                                onClick = { showConfirmLimparHistorico = true },
                                modifier = Modifier.semantics { contentDescription = "Apagar todo o Histórico de Alterações" }
                            ) {
                                Text("🗑️", style = MaterialTheme.typography.titleLarge)
                            }
                            IconButton(
                                onClick = { exportLauncher.launch("historico_ficha.txt") },
                                modifier = Modifier.semantics { contentDescription = "Exportar Histórico para arquivo de texto" }
                            ) {
                                Text("📄", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                        Text(
                            "Histórico de Alterações",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider()
                    Column(
                        modifier = Modifier.weight(1f).rolagemVertical(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (p.historicoLog.isEmpty()) {
                            Text("Nenhum registro encontrado.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            p.historicoLog.forEach { log ->
                                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                val dateStr = sdf.format(java.util.Date(log.timestamp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics(mergeDescendants = true) {
                                            contentDescription = "$dateStr. ${log.descricao}"
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(log.descricao, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showHistoricoDialog = false }) {
                            Text("Fechar")
                        }
                    }
                }
            }
        }
    }

    if (showConfirmLimparHistorico) {
        AlertDialog(
            onDismissRequest = { showConfirmLimparHistorico = false },
            title = { Text("Apagar Histórico") },
            text = { Text("Deseja realmente apagar todo o histórico de alterações? Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.limparHistoricoLog()
                    showConfirmLimparHistorico = false 
                }) { Text("Apagar Tudo", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmLimparHistorico = false }) { Text("Cancelar") }
            }
        )
    }

    if (showResumoDialog) {
        AlertDialog(
            onDismissRequest = { showResumoDialog = false },
            title = { Text("Resumo de Pontos") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    PontosResumoRow("Atributos Primarios", p.pontosAtributos)
                    PontosResumoRow("Atributos Secundarios", p.pontosSecundarios)
                    PontosResumoRow("Vantagens", p.pontosVantagens)
                    PontosResumoRow("Desvantagens", p.pontosDesvantagens)
                    PontosResumoRow("Qualidades", p.pontosQualidades)
                    PontosResumoRow("Peculiaridades", p.pontosPeculiaridades)
                    PontosResumoRow("Pericias", p.pontosPericias)
                    PontosResumoRow("Magias", p.pontosMagias)
                    PontosResumoRow("Modelo Racial (${p.modeloRacial.nome})", p.modeloRacial.custoTotal)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    PontosResumoRow("Total Gasto", p.pontosGastos, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showResumoDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    if (showBasesDialog) {
        DefinirBaseAtributosDialog(
            forcaBaseInicial = p.forcaBase,
            destrezaBaseInicial = p.destrezaBase,
            inteligenciaBaseInicial = p.inteligenciaBase,
            vitalidadeBaseInicial = p.vitalidadeBase,
            onDismiss = { showBasesDialog = false },
            onConfirm = { st, dx, iq, ht ->
                viewModel.definirBasesAtributosPrimarios(st, dx, iq, ht)
                showBasesDialog = false
            }
        )
    }
}

@Composable
fun AtributoEditor(nome: String, valor: Int, custo: Int, onSetValor: (Int) -> Unit) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Text(nome, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        if (isPraCegoVariant) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                TextButton(
                    onClick = { onSetValor((valor - 1).coerceIn(1, 30)) },
                    modifier = Modifier.semantics { contentDescription = "Diminuir valor de $nome" },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("-")
                }
                Text(
                    valor.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .width(36.dp)
                        .semantics { contentDescription = "Valor de $nome: $valor" },
                    textAlign = TextAlign.Center
                )
                TextButton(
                    onClick = { onSetValor((valor + 1).coerceIn(1, 30)) },
                    modifier = Modifier.semantics { contentDescription = "Aumentar valor de $nome" },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("+")
                }
            }
        } else {
            Text(
                valor.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .width(36.dp)
                    .pointerInput(nome, valor) {
                        var dragAcumulado = 0f
                        val passoPx = 40f
                        var valorAtual = valor.coerceIn(1, 30)
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                dragAcumulado += dragAmount
                                if (abs(dragAcumulado) >= passoPx) {
                                    if (dragAcumulado < 0f) {
                                        valorAtual = (valorAtual + 1).coerceIn(1, 30)
                                    } else {
                                        valorAtual = (valorAtual - 1).coerceIn(1, 30)
                                    }
                                    onSetValor(valorAtual)
                                    dragAcumulado = 0f
                                }
                            }
                        )
                    },
                textAlign = TextAlign.Center
            )
        }
        Text(
            "[${if (custo >= 0) "+$custo" else custo}]",
            style = MaterialTheme.typography.bodySmall,
            color = if (custo >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun AtributoSecundarioEditor(
    nome: String,
    valorBase: Int,
    modificador: Int,
    valorFinal: Int,
    custoPorPonto: Int,
    onSetModificador: (Int) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val custo = modificador * custoPorPonto
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Text(nome, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        if (isPraCegoVariant) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                TextButton(
                    onClick = { onSetModificador((modificador - 1).coerceIn(-20, 20)) },
                    modifier = Modifier.semantics { contentDescription = "Diminuir valor de $nome" },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("-")
                }
                Text(
                    valorFinal.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .width(34.dp)
                        .semantics { contentDescription = "Valor de $nome: $valorFinal" },
                    textAlign = TextAlign.Center
                )
                TextButton(
                    onClick = { onSetModificador((modificador + 1).coerceIn(-20, 20)) },
                    modifier = Modifier.semantics { contentDescription = "Aumentar valor de $nome" },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("+")
                }
            }
        } else {
            Text(
                valorFinal.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .width(34.dp)
                    .pointerInput(nome, modificador) {
                        var dragAcumulado = 0f
                        val passoPx = 40f
                        var modAtual = modificador.coerceIn(-20, 20)
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                dragAcumulado += dragAmount
                                if (abs(dragAcumulado) >= passoPx) {
                                    if (dragAcumulado < 0f) {
                                        modAtual = (modAtual + 1).coerceIn(-20, 20)
                                    } else {
                                        modAtual = (modAtual - 1).coerceIn(-20, 20)
                                    }
                                    onSetModificador(modAtual)
                                    dragAcumulado = 0f
                                }
                            }
                        )
                    },
                textAlign = TextAlign.Center
            )
        }
        Text("base $valorBase", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (modificador != 0) {
            Text(
                "[${if (custo >= 0) "+$custo" else custo}]",
                style = MaterialTheme.typography.bodySmall,
                color = if (custo >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

private fun nomeAtributoPrimario(sigla: String): String = when (sigla.uppercase()) {
    "ST" -> "Força"
    "DX" -> "Destreza"
    "IQ" -> "Inteligência"
    "HT" -> "Vitalidade"
    else -> sigla
}

private fun nomeAtributoSecundario(sigla: String): String = when (sigla.uppercase()) {
    "PV" -> "Pontos de Vida"
    "VON" -> "Vontade"
    "PER" -> "Percepção"
    "PF" -> "Pontos de Fadiga"
    else -> sigla
}

@Composable
private fun AtributoPrimarioLinearCard(sigla: String, valor: Int, custo: Int, onSetValor: (Int) -> Unit) {
    val nome = nomeAtributoPrimario(sigla)
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("$sigla - $nome", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "[${if (custo >= 0) "+$custo" else custo}]",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (custo >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = { onSetValor((valor - 1).coerceIn(1, 30)) },
                    modifier = Modifier.semantics { contentDescription = "Diminuir valor de $nome" },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) { Text("-") }
                Text(valor.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { contentDescription = "Valor de $nome: $valor" })
                TextButton(
                    onClick = { onSetValor((valor + 1).coerceIn(1, 30)) },
                    modifier = Modifier.semantics { contentDescription = "Aumentar valor de $nome" },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) { Text("+") }
            }
        }
    }
}

@Composable
private fun AtributoSecundarioLinearCard(
    sigla: String,
    valorBase: Int,
    modificador: Int,
    valorFinal: Int,
    custoPorPonto: Int,
    onSetModificador: (Int) -> Unit
) {
    val nome = nomeAtributoSecundario(sigla)
    val custo = modificador * custoPorPonto
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("$sigla - $nome", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("base $valorBase", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (modificador != 0) {
                    Text(
                        "[${if (custo >= 0) "+$custo" else custo}]",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (custo >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = { onSetModificador((modificador - 1).coerceIn(-20, 20)) },
                    modifier = Modifier.semantics { contentDescription = "Diminuir valor de $nome" },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) { Text("-") }
                Text(valorFinal.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { contentDescription = "Valor de $nome: $valorFinal" })
                TextButton(
                    onClick = { onSetModificador((modificador + 1).coerceIn(-20, 20)) },
                    modifier = Modifier.semantics { contentDescription = "Aumentar valor de $nome" },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) { Text("+") }
            }
        }
    }
}

@Composable
fun CaracteristicaDisplay(nome: String, valor: String, descricaoAcessivel: String = "") {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .semantics {
                if (descricaoAcessivel.isNotBlank()) {
                    contentDescription = descricaoAcessivel
                }
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(nome, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
fun PontosResumoRow(label: String, pontos: Int, fontWeight: FontWeight = FontWeight.Normal) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = fontWeight)
        Text(
            if (pontos >= 0) "+$pontos" else pontos.toString(),
            fontWeight = fontWeight,
            color = if (pontos >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun DefinirBaseAtributosDialog(
    forcaBaseInicial: Int,
    destrezaBaseInicial: Int,
    inteligenciaBaseInicial: Int,
    vitalidadeBaseInicial: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int, Int) -> Unit
) {
    var stInput by remember(forcaBaseInicial) { mutableStateOf(forcaBaseInicial.toString()) }
    var dxInput by remember(destrezaBaseInicial) { mutableStateOf(destrezaBaseInicial.toString()) }
    var iqInput by remember(inteligenciaBaseInicial) { mutableStateOf(inteligenciaBaseInicial.toString()) }
    var htInput by remember(vitalidadeBaseInicial) { mutableStateOf(vitalidadeBaseInicial.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Definir Base de Atributos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Esses valores viram o inicial sem custo. Ao salvar, ST/DX/IQ/HT atuais serão ajustados para a nova base.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stInput,
                        onValueChange = { stInput = it.filter(Char::isDigit).take(2) },
                        label = { Text("ST Base") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dxInput,
                        onValueChange = { dxInput = it.filter(Char::isDigit).take(2) },
                        label = { Text("DX Base") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = iqInput,
                        onValueChange = { iqInput = it.filter(Char::isDigit).take(2) },
                        label = { Text("IQ Base") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = htInput,
                        onValueChange = { htInput = it.filter(Char::isDigit).take(2) },
                        label = { Text("HT Base") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val st = (stInput.toIntOrNull() ?: forcaBaseInicial).coerceIn(1, 30)
                    val dx = (dxInput.toIntOrNull() ?: destrezaBaseInicial).coerceIn(1, 30)
                    val iq = (iqInput.toIntOrNull() ?: inteligenciaBaseInicial).coerceIn(1, 30)
                    val ht = (htInput.toIntOrNull() ?: vitalidadeBaseInicial).coerceIn(1, 30)
                    onConfirm(st, dx, iq, ht)
                }
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * **Comprar Velocidade Básica e Deslocamento Básico** — MB p.17. Lote ATR-1.
 *
 * Os dois custam **5 pontos por degrau**: 0,25 na Velocidade, 1 metro/segundo no
 * Deslocamento.
 *
 * ⚠️ Com botões visíveis, e não só com o arraste dos outros atributos. O arraste
 * funciona em passos de 1, e aqui o passo da Velocidade é **0,25** — um gesto
 * que anda quatro degraus de uma vez seria mais fácil de errar do que de acertar.
 */
@Composable
private fun CompraDeVelocidadeEDeslocamento(
    modVelocidade: Float,
    velocidadeFinal: Float,
    modDeslocamento: Int,
    deslocamentoFinal: Int,
    onVelocidade: (Float) -> Unit,
    onDeslocamento: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinhaDeCompra(
            nome = "Vel. Básica",
            valor = VelocidadeEDeslocamento.formatar(velocidadeFinal),
            custo = VelocidadeEDeslocamento.custoDaVelocidade(modVelocidade),
            // ⚠️ O arraste conta DEGRAUS inteiros, não frações — senão o dedo
            // escorregaria 0,03 de Velocidade e o custo viraria um mistério.
            degrauAtual = CharacterRules.calcularPassosVelocidadeBasica(modVelocidade),
            descricaoMenos = "Diminuir a Velocidade Básica em zero vírgula vinte e cinco",
            descricaoMais = "Aumentar a Velocidade Básica em zero vírgula vinte e cinco",
            descricaoDoValor = "Velocidade Básica: " +
                VelocidadeEDeslocamento.formatar(velocidadeFinal),
            // ⚠️ O teto de ±20 degraus (±5,00) não é do livro: é limite de tela,
            // para o gesto não virar uma corrida sem fim. O limite do LIVRO é o
            // aviso de campanha realista, logo abaixo, e ele não trava nada.
            onDegrau = { degrau ->
                onVelocidade(degrau * VelocidadeEDeslocamento.PASSO_DA_VELOCIDADE)
            }
        )
        VelocidadeEDeslocamento.avisoDoLimiteRealista(modVelocidade)?.let { aviso ->
            Text(
                aviso,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        LinhaDeCompra(
            nome = "Desloc. Básico",
            valor = "$deslocamentoFinal m/s",
            custo = VelocidadeEDeslocamento.custoDoDeslocamento(modDeslocamento),
            degrauAtual = modDeslocamento,
            descricaoMenos = "Diminuir o Deslocamento Básico em um metro por segundo",
            descricaoMais = "Aumentar o Deslocamento Básico em um metro por segundo",
            descricaoDoValor = "Deslocamento Básico: $deslocamentoFinal metros por segundo",
            onDegrau = { degrau -> onDeslocamento(degrau) }
        )
    }
}

/**
 * Uma linha de compra, **no controle que a variante já usa**.
 *
 * ⚠️ Na `visual` arrasta-se o dedo para cima e para baixo, igual a PV, Von, Per
 * e PF logo acima; na `pracego` ficam os botões de − e +. Foi decisão do
 * usuário, e é a certa: uma tela em que quatro atributos se ajustam de um jeito
 * e dois de outro ensina duas coisas onde havia uma.
 *
 * O `valor` é sempre o **final**; o `degrauAtual` é só o que o jogador comprou.
 * São coisas diferentes — misturá-las faria o arraste partir do número errado em
 * toda ficha com bônus racial.
 */
@Composable
private fun LinhaDeCompra(
    nome: String,
    valor: String,
    custo: Int,
    degrauAtual: Int,
    descricaoMenos: String,
    descricaoMais: String,
    descricaoDoValor: String,
    onDegrau: (Int) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            nome,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        if (isPraCegoVariant) {
            // Botões: alvo de toque previsível e anunciável. Arraste não tem
            // como ser descrito ao leitor de tela sem virar adivinhação.
            AppBotaoPasso(
                sinal = "−",
                descricao = descricaoMenos,
                onClick = { onDegrau(degrauAtual - 1) }
            )
            Text(
                valor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(72.dp)
                    .semantics { contentDescription = descricaoDoValor }
            )
            AppBotaoPasso(
                sinal = "+",
                descricao = descricaoMais,
                onClick = { onDegrau(degrauAtual + 1) }
            )
        } else {
            // ⚠️ Os 40 px por degrau são os mesmos do `AtributoSecundarioEditor`
            // — não é número novo, é o mesmo gesto dos atributos de cima.
            Text(
                valor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(96.dp)
                    .semantics { contentDescription = descricaoDoValor }
                    .pointerInput(nome, degrauAtual) {
                        var dragAcumulado = 0f
                        val passoPx = 40f
                        var degrau = degrauAtual.coerceIn(-20, 20)
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                dragAcumulado += dragAmount
                                if (abs(dragAcumulado) >= passoPx) {
                                    // Dedo para CIMA aumenta, como nos outros.
                                    degrau = if (dragAcumulado < 0f) {
                                        (degrau + 1).coerceIn(-20, 20)
                                    } else {
                                        (degrau - 1).coerceIn(-20, 20)
                                    }
                                    onDegrau(degrau)
                                    dragAcumulado = 0f
                                }
                            }
                        )
                    }
            )
        }

        // O que ele já pagou por isto. Some quando não custou nada, para uma
        // ficha recém-criada não abrir com dois zeros sem sentido.
        Text(
            if (custo == 0) "" else "[${if (custo > 0) "+$custo" else "$custo"}]",
            style = MaterialTheme.typography.bodySmall,
            color = if (custo >= 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.width(44.dp)
        )
    }
}

