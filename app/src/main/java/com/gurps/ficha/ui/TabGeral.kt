package com.gurps.ficha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlin.math.abs

@Composable
fun TabGeral(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val isCompactScreen = LocalConfiguration.current.screenWidthDp <= 360
    val outerPadding = if (isCompactScreen) 10.dp else 12.dp
    val contentSpacing = if (isCompactScreen) 8.dp else 10.dp
    val rowSpacing = if (isCompactScreen) 6.dp else 8.dp
    val dialogPadding = if (isCompactScreen) 8.dp else 10.dp
    val dialogSpacing = if (isCompactScreen) 3.dp else 4.dp
    var pontosInput by rememberSaveable { mutableStateOf(p.pontosIniciais.toString()) }
    var ultimoPontosValidos by rememberSaveable { mutableStateOf(p.pontosIniciais.toString()) }
    var pontosEmFoco by remember { mutableStateOf(false) }
    var showAnotacoesDialog by remember { mutableStateOf(false) }
    var showResumoDialog by remember { mutableStateOf(false) }
    var showBasesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(p.pontosIniciais) {
        if (!pontosEmFoco) {
            pontosInput = p.pontosIniciais.toString()
            ultimoPontosValidos = pontosInput
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(outerPadding),
        verticalArrangement = Arrangement.spacedBy(contentSpacing)
    ) {
        SectionCard(title = "") {
            OutlinedTextField(
                value = p.nome,
                onValueChange = { viewModel.atualizarNome(it) },
                label = { Text("Nome do Personagem") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(rowSpacing)
            ) {
                OutlinedTextField(
                    value = p.jogador,
                    onValueChange = { viewModel.atualizarJogador(it) },
                    label = { Text("Jogador") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = pontosInput,
                    onValueChange = {
                        val somenteDigitos = it.filter(Char::isDigit).take(4)
                        pontosInput = somenteDigitos
                        if (somenteDigitos.isNotBlank()) {
                            ultimoPontosValidos = somenteDigitos
                        }
                    },
                    label = { Text("Pontos") },
                    modifier = Modifier
                        .width(100.dp)
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }

        SectionCard(title = "Atributos Primarios") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showBasesDialog = true }) {
                    Text("Definir Base")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AtributoEditor("ST", p.forca, (p.forca - p.forcaBase) * 10) { delta ->
                    viewModel.atualizarForca(delta)
                }
                AtributoEditor("DX", p.destreza, (p.destreza - p.destrezaBase) * 20) { delta ->
                    viewModel.atualizarDestreza(delta)
                }
                AtributoEditor("IQ", p.inteligencia, (p.inteligencia - p.inteligenciaBase) * 20) { delta ->
                    viewModel.atualizarInteligencia(delta)
                }
                AtributoEditor("HT", p.vitalidade, (p.vitalidade - p.vitalidadeBase) * 10) { delta ->
                    viewModel.atualizarVitalidade(delta)
                }
            }
        }

        SectionCard(title = "Atributos Secundarios") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AtributoSecundarioEditor("PV", p.forca, p.modPontosVida, p.pontosVida, 2) { delta ->
                    viewModel.atualizarModPontosVida(delta)
                }
                AtributoSecundarioEditor("Von", p.inteligencia, p.modVontade, p.vontade, 5) { delta ->
                    viewModel.atualizarModVontade(delta)
                }
                AtributoSecundarioEditor("Per", p.inteligencia, p.modPercepcao, p.percepcao, 5) { delta ->
                    viewModel.atualizarModPercepcao(delta)
                }
                AtributoSecundarioEditor("PF", p.vitalidade, p.modPontosFadiga, p.pontosFadiga, 3) { delta ->
                    viewModel.atualizarModPontosFadiga(delta)
                }
            }
        }

        SectionCard(title = "Caracteristicas Derivadas") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CaracteristicaDisplay("Vel. Basica", String.format("%.2f", p.velocidadeBasica))
                CaracteristicaDisplay("Desloc.", "${p.deslocamentoBasico} m/s")
                CaracteristicaDisplay("BC", String.format("%.1f kg", p.baseCarga))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CaracteristicaDisplay("Dano GdP", p.danoGdP)
                CaracteristicaDisplay("Dano GeB", p.danoGeB)
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
                            .verticalScroll(rememberScrollState()),
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
                        horizontalArrangement = Arrangement.Start
                    ) {
                        TextButton(onClick = { showAnotacoesDialog = false }) {
                            Text("Fechar")
                        }
                    }
                }
            }
        }
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
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Text(nome, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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
    val custo = modificador * custoPorPonto
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Text(nome, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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

@Composable
fun CaracteristicaDisplay(nome: String, valor: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
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
