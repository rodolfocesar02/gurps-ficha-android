package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.model.AtributoBase
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.PericiaDefinicao
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlin.math.abs

private val PONTOS_PRESETS = listOf(1, 2, 4, 8, 12)

private fun ajustarPontosPreset(atual: Int, incrementar: Boolean): Int {
    val indice = PONTOS_PRESETS.indexOf(atual).let { if (it == -1) 0 else it }
    return if (incrementar) {
        PONTOS_PRESETS[(indice + 1).coerceAtMost(PONTOS_PRESETS.lastIndex)]
    } else {
        PONTOS_PRESETS[(indice - 1).coerceAtLeast(0)]
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarPericiaDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var busca by remember { mutableStateOf("") }
    var filtroAtributo by remember { mutableStateOf<String?>(null) }
    var periciaSelecionada by remember { mutableStateOf<PericiaDefinicao?>(null) }

    val listaFiltrada = viewModel.dataRepository.filtrarPericias(busca, filtroAtributo, null)

    FullscreenDialogContainer(onDismiss = onDismiss) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text("Selecionar Perícia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(value = busca, onValueChange = { busca = it }, label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null) })

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PericiaFiltroChip(
                        label = "Todos",
                        selected = filtroAtributo == null,
                        onClick = { filtroAtributo = null }
                    )
                    listOf("DX", "IQ", "HT", "PER", "VON").forEach { attr ->
                        PericiaFiltroChip(
                            label = attr,
                            selected = filtroAtributo == attr,
                            onClick = { filtroAtributo = attr }
                        )
                    }
                }

                Text("${listaFiltrada.size} perícias encontradas", style = MaterialTheme.typography.bodySmall)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(listaFiltrada) { definicao ->
                        val jaAdicionada = viewModel.periciaJaAdicionada(definicao.id)
                        val atributos = definicao.atributosPossiveis?.joinToString("/") ?: definicao.atributoBase
                        val dificuldade = if (definicao.dificuldadeVariavel) "F/M/D/MD" else definicao.dificuldadeFixa ?: "M"
                        val atributoBaseTexto = "$atributos/$dificuldade"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !jaAdicionada || definicao.exigeEspecializacao) { periciaSelecionada = definicao },
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = definicao.nome + if (definicao.exigeEspecializacao) " *" else "",
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = atributoBaseTexto,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                if (jaAdicionada) {
                                    Text("Adicionada", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) }
                }
            }
    }

    periciaSelecionada?.let { definicao ->
        ConfigurarPericiaDialog(viewModel = viewModel, definicao = definicao, personagem = viewModel.personagem, onDismiss = { periciaSelecionada = null },
            onSave = { pontosGastos, especializacao, atributo, dificuldade ->
                val erro = viewModel.adicionarPericia(definicao, pontosGastos, especializacao, atributo, dificuldade)
                if (erro == null) {
                    periciaSelecionada = null
                }
                erro
            })
    }
}

@Composable
private fun PericiaFiltroChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarPericiaCustomizadaDialog(
    onDismiss: () -> Unit,
    onSave: (nome: String, especializacao: String, atributo: String, difficulty: String) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var especializacao by remember { mutableStateOf("") }
    var atributoSelecionado by remember { mutableStateOf("DX") }
    var dificuldadeSelecionada by remember { mutableStateOf("M") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Perícia Personalizada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Perícia") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = especializacao,
                    onValueChange = {
                        if (it.length <= 20) {
                            especializacao = it
                        }
                    },
                    label = { Text("Especialização (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Atributo Base:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("DX", "IQ", "HT", "ST", "PER", "VON").forEach { attr ->
                        FilterChip(
                            selected = atributoSelecionado == attr,
                            onClick = { atributoSelecionado = attr },
                            label = { Text(attr) }
                        )
                    }
                }

                Text("Dificuldade:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("F", "M", "D", "MD").forEach { dif ->
                        FilterChip(
                            selected = dificuldadeSelecionada == dif,
                            onClick = { dificuldadeSelecionada = dif },
                            label = { Text(dif) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(nome, especializacao, atributoSelecionado, dificuldadeSelecionada)
                },
                enabled = nome.isNotBlank()
            ) {
                Text(UiActionLabels.ADICIONAR)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(UiActionLabels.CANCELAR)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarPericiaDialog(viewModel: FichaViewModel, definicao: PericiaDefinicao, personagem: Personagem, onDismiss: () -> Unit,
    onSave: (Int, String, AtributoBase?, Dificuldade?) -> String?) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var pontosGastos by remember { mutableStateOf(1) }
    var especializacao by remember { mutableStateOf("") }
    var atributoEscolhido by remember { mutableStateOf(AtributoBase.fromSigla(definicao.atributoBase)) }
    var dificuldadeEscolhida by remember { mutableStateOf(Dificuldade.fromSigla(definicao.dificuldadeFixa)) }
    var erroCadastro by remember { mutableStateOf<String?>(null) }
    var mostrarDescricao by remember { mutableStateOf(false) }
    val regraV2 = remember(definicao.id) { viewModel.dataRepository.regraPericiaV2(definicao.id) }

    val atributosPossiveis = definicao.atributosPossiveis?.map { AtributoBase.fromSigla(it) } ?: listOf(AtributoBase.fromSigla(definicao.atributoBase))
    val precisaEscolherAtributo = atributosPossiveis.size > 1 || definicao.atributoEscolhaObrigatoria

    // Calcula nível preview
    val previewPericia = PericiaSelecionada(definicao.id, definicao.nome, atributoEscolhido, dificuldadeEscolhida, pontosGastos, especializacao, definicao.exigeEspecializacao)
    val nivelPreview = previewPericia.calcularNivel(personagem)
    val nivelRelativo = previewPericia.getNivelRelativo(personagem)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing), modifier = Modifier.verticalScroll(rememberScrollState())) {
                TextButton(
                    onClick = { mostrarDescricao = true },
                    modifier = Modifier.semantics {
                        contentDescription = "Abrir descrição da perícia ${definicao.nome}"
                    }
                ) {
                    Text(
                        definicao.nome,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (definicao.exigeEspecializacao) {
                    Text("Esta perícia exige especialização!", color = MaterialTheme.colorScheme.error)
                    OutlinedTextField(value = especializacao, onValueChange = {
                        if (it.length <= 20) {
                            especializacao = it
                        }
                    },
                        label = { Text("Especialização *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                } else {
                    OutlinedTextField(value = especializacao, onValueChange = {
                        if (it.length <= 20) {
                            especializacao = it
                        }
                    },
                        label = { Text("Especialização (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }

                if (precisaEscolherAtributo) {
                    Text("Atributo Base:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        atributosPossiveis.forEach { attr ->
                            FilterChip(selected = atributoEscolhido == attr, onClick = { atributoEscolhido = attr },
                                label = { Text("${attr.sigla} (${personagem.getAtributo(attr.sigla)})") })
                        }
                    }
                }

                if (definicao.dificuldadeVariavel) {
                    Text("Dificuldade:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Dificuldade.entries.forEach { dif ->
                            FilterChip(selected = dificuldadeEscolhida == dif, onClick = { dificuldadeEscolhida = dif },
                                label = { Text(dif.sigla) })
                        }
                    }
                } else {
                    Text("Dificuldade: ${dificuldadeEscolhida.nomeCompleto}", style = MaterialTheme.typography.bodyMedium)
                }

                Divider()
                Text("Pontos Gastos:", style = MaterialTheme.typography.labelMedium)
                if (!isPraCegoVariant) {
                    Text(
                        "$pontosGastos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .width(56.dp)
                            .pointerInput(pontosGastos) {
                                var dragAcumulado = 0f
                                val passoPx = 24f
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAcumulado += dragAmount
                                        while (abs(dragAcumulado) >= passoPx) {
                                            pontosGastos = ajustarPontosPreset(
                                                atual = pontosGastos,
                                                incrementar = dragAcumulado < 0f
                                            )
                                            dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                        }
                                    }
                                )
                            },
                        textAlign = TextAlign.Center
                    )
                }
                if (isPraCegoVariant) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(
                            onClick = { pontosGastos = ajustarPontosPreset(pontosGastos, incrementar = false) },
                            modifier = Modifier.semantics { contentDescription = "Diminuir pontos gastos da perícia" }
                        ) { Text("-") }
                        Text(
                            "$pontosGastos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(horizontal = 8.dp)
                                .semantics { contentDescription = "Pontos gastos atuais da perícia: $pontosGastos" }
                        )
                        TextButton(
                            onClick = { pontosGastos = ajustarPontosPreset(pontosGastos, incrementar = true) },
                            modifier = Modifier.semantics { contentDescription = "Aumentar pontos gastos da perícia" }
                        ) { Text("+") }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    PONTOS_PRESETS.forEach { pts ->
                        TextButton(
                            onClick = { pontosGastos = pts },
                            modifier = Modifier.padding(horizontal = 1.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("$pts", fontSize = 12.sp)
                        }
                    }
                }

                Divider()
                Card(colors = appCardColors()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("NH: $nivelPreview (${atributoEscolhido.sigla}$nivelRelativo)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                erroCadastro?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { erroCadastro = onSave(pontosGastos, especializacao, atributoEscolhido, dificuldadeEscolhida) },
                enabled = !definicao.exigeEspecializacao || especializacao.isNotBlank()
            ) { Text(UiActionLabels.ADICIONAR) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
    )

    if (mostrarDescricao) {
        PericiaDescricaoDialog(
            definicao = definicao,
            descricao = regraV2?.descricao.orEmpty(),
            preRequisito = regraV2?.preRequisito?.raw.orEmpty(),
            preDefinido = regraV2?.preDefinido?.raw.orEmpty(),
            modificadores = regraV2?.modificadoresRaw.orEmpty(),
            onDismiss = { mostrarDescricao = false }
        )
    }
}

// === DIALOGS DE EDICAO ===


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPericiaDialog(
    pericia: PericiaSelecionada,
    personagem: Personagem,
    descricaoRegra: String = "",
    preRequisitoRegra: String = "",
    preDefinidoRegra: String = "",
    modificadoresRegra: String = "",
    onDismiss: () -> Unit,
    onSave: (PericiaSelecionada) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var pontosGastos by remember { mutableStateOf(pericia.pontosGastos) }
    var especializacao by remember { mutableStateOf(pericia.especializacao) }
    var mostrarDescricao by remember { mutableStateOf(false) }

    val previewPericia = pericia.copy(pontosGastos = pontosGastos, especializacao = especializacao)
    val nivelPreview = previewPericia.calcularNivel(personagem)
    val nivelRelativo = previewPericia.getNivelRelativo(personagem)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                pericia.nome,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .let {
                        if (descricaoRegra.isNotBlank()) {
                            it.clickable { mostrarDescricao = true }
                        } else {
                            it
                        }
                    }
                    .semantics {
                        if (isPraCegoVariant && descricaoRegra.isNotBlank()) {
                            contentDescription = "Nome da perícia ${pericia.nome}. Toque para abrir descricao."
                        }
                    }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                Text("${pericia.atributoBase.sigla}/${pericia.dificuldade.sigla}")

                OutlinedTextField(value = especializacao, onValueChange = {
                    if (it.length <= 20) {
                        especializacao = it
                    }
                },
                    label = { Text("Especialização") }, modifier = Modifier.fillMaxWidth())

                Text("Pontos Gastos:")
                if (!isPraCegoVariant) {
                    Text(
                        "$pontosGastos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .width(56.dp)
                            .pointerInput(pontosGastos) {
                                var dragAcumulado = 0f
                                val passoPx = 24f
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAcumulado += dragAmount
                                        while (abs(dragAcumulado) >= passoPx) {
                                            pontosGastos = ajustarPontosPreset(
                                                atual = pontosGastos,
                                                incrementar = dragAcumulado < 0f
                                            )
                                            dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                        }
                                    }
                                )
                            },
                        textAlign = TextAlign.Center
                    )
                }
                if (isPraCegoVariant) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(
                            onClick = { pontosGastos = ajustarPontosPreset(pontosGastos, incrementar = false) },
                            modifier = Modifier.semantics { contentDescription = "Diminuir pontos gastos da perícia" }
                        ) { Text("-") }
                        Text(
                            "$pontosGastos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(horizontal = 8.dp)
                                .semantics { contentDescription = "Pontos gastos atuais da perícia: $pontosGastos" }
                        )
                        TextButton(
                            onClick = { pontosGastos = ajustarPontosPreset(pontosGastos, incrementar = true) },
                            modifier = Modifier.semantics { contentDescription = "Aumentar pontos gastos da perícia" }
                        ) { Text("+") }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    PONTOS_PRESETS.forEach { pts ->
                        TextButton(
                            onClick = { pontosGastos = pts },
                            modifier = Modifier.padding(horizontal = 1.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("$pts", fontSize = 12.sp)
                        }
                    }
                }

                Text("NH: $nivelPreview (${pericia.atributoBase.sigla}$nivelRelativo)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(pericia.copy(pontosGastos = pontosGastos, especializacao = especializacao)) },
                modifier = Modifier.semantics {
                    if (isPraCegoVariant) contentDescription = "Salvar edição da perícia"
                }
            ) { Text(UiActionLabels.SALVAR) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics {
                    if (isPraCegoVariant) contentDescription = "Cancelar edição da perícia"
                }
            ) { Text(UiActionLabels.CANCELAR) }
        }
    )

    if (mostrarDescricao) {
        AlertDialog(
            onDismissRequest = { mostrarDescricao = false },
            title = { Text(pericia.nome) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        descricaoRegra.ifBlank { "Sem descrição detalhada disponível." },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (preRequisitoRegra.isNotBlank() && preRequisitoRegra != "-") {
                        Text("Pré-requisito: $preRequisitoRegra", style = MaterialTheme.typography.bodySmall)
                    }
                    if (preDefinidoRegra.isNotBlank() && preDefinidoRegra != "-") {
                        Text("Pré-definido: $preDefinidoRegra", style = MaterialTheme.typography.bodySmall)
                    }
                    if (modificadoresRegra.isNotBlank() && modificadoresRegra != "-") {
                        Text("Modificadores: $modificadoresRegra", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { mostrarDescricao = false },
                    modifier = Modifier.semantics {
                        if (isPraCegoVariant) contentDescription = "Fechar descrição da perícia"
                    }
                ) { Text(UiActionLabels.FECHAR) }
            }
        )
    }
}

@Composable
private fun PericiaDescricaoDialog(
    definicao: PericiaDefinicao,
    descricao: String,
    preRequisito: String,
    preDefinido: String,
    modificadores: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(definicao.nome) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    descricao.ifBlank { "Sem descrição detalhada disponível." },
                    style = MaterialTheme.typography.bodyMedium
                )
                if (preRequisito.isNotBlank() && preRequisito != "-") {
                    Text("Pré-requisito: $preRequisito", style = MaterialTheme.typography.bodySmall)
                }
                if (preDefinido.isNotBlank() && preDefinido != "-") {
                    Text("Pré-definido: $preDefinido", style = MaterialTheme.typography.bodySmall)
                }
                if (modificadores.isNotBlank() && modificadores != "-") {
                    Text("Modificadores: $modificadores", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) } }
    )
}




