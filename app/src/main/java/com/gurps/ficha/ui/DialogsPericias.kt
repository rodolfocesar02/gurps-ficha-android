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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import com.gurps.ficha.regras_prerequisitos.ConditionStatus
import com.gurps.ficha.regras_prerequisitos.PreRequisitoType
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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

private fun ajustarPontos(atual: Int, incrementar: Boolean): Int {
    return if (incrementar) {
        when {
            atual < 1 -> 1
            atual == 1 -> 2
            atual == 2 -> 4
            else -> {
                // Se atual for múltiplo de 4, sobe mais 4.
                // Se não for (manualmente digitado?), vai para o próximo múltiplo de 4.
                val resto = atual % 4
                if (resto == 0) atual + 4 else atual + (4 - resto)
            }
        }
    } else {
        when {
            atual <= 1 -> 1
            atual == 2 -> 1
            atual <= 4 -> 2
            else -> {
                val resto = atual % 4
                if (resto == 0) (atual - 4).coerceAtLeast(4) else (atual - resto).coerceAtLeast(4)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarPericiaDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var busca by remember { mutableStateOf("") }
    var filtroAtributo by remember { mutableStateOf<String?>(null) }
    var periciaSelecionada by remember { mutableStateOf<PericiaDefinicao?>(null) }

    val listaFiltrada = viewModel.dataRepository.filtrarPericias(busca, filtroAtributo, null)

    // Lote LAYOUT-6. O `detalheADireita` existe justamente para este caso: o
    // "DX/D" fica encostado na borda, como sempre esteve, mas agora com o mesmo
    // respiro e a mesma cor de card das outras listas.
    AppSelectionDialog(
        titulo = "Selecionar Perícia",
        busca = busca,
        onBusca = { busca = it },
        contador = contadorDe(listaFiltrada.size, "perícia", "perícias"),
        filtros = {
            AppFiltroChip("Todos", filtroAtributo == null) { filtroAtributo = null }
            listOf("DX", "IQ", "HT", "PER", "VON").forEach { attr ->
                AppFiltroChip(attr, filtroAtributo == attr) { filtroAtributo = attr }
            }
        },
        onDismiss = onDismiss
    ) {
        items(listaFiltrada) { definicao ->
            val jaAdicionada = viewModel.periciaJaAdicionada(definicao.id)
            val atributos = definicao.atributosPossiveis?.joinToString("/") ?: definicao.atributoBase
            val dificuldade = if (definicao.dificuldadeVariavel) "F/M/D/MD" else definicao.dificuldadeFixa ?: "M"
            AppSelectionRow(
                nome = definicao.nome + if (definicao.exigeEspecializacao) " *" else "",
                detalheADireita = "$atributos/$dificuldade",
                detalhe = if (jaAdicionada) "Já está na ficha" else null,
                // ⚠️ Perícia com especialização continua clicável mesmo já
                // adicionada — dá para ter Armas de Fogo (Pistola) e (Rifle).
                habilitado = !jaAdicionada || definicao.exigeEspecializacao,
                descricaoAcessivel = "${definicao.nome}. $atributos barra $dificuldade." +
                    if (definicao.exigeEspecializacao) " Exige especialização." else "",
                onClick = { periciaSelecionada = definicao }
            )
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
            val rolagemConfig = rememberScrollState()
            Column(
                verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                modifier = Modifier
                    .barraDeRolagem(rolagemConfig)
                    .verticalScroll(rolagemConfig)
                    .padding(end = 10.dp)
            ) {
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
                
                HorizontalDivider()
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
                                            pontosGastos = ajustarPontos(
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
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { pontosGastos = ajustarPontos(pontosGastos, incrementar = false) },
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
                            onClick = { pontosGastos = ajustarPontos(pontosGastos, incrementar = true) },
                            modifier = Modifier.semantics { contentDescription = "Aumentar pontos gastos da perícia" }
                        ) { Text("+") }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 2, 4, 8, 12, 16, 20, 24, 32).forEach { pts ->
                        TextButton(
                            onClick = { pontosGastos = pts },
                            modifier = Modifier.padding(horizontal = 1.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("$pts", fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider()
                Card(colors = appCardColors()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("NH: $nivelPreview (${atributoEscolhido.sigla}$nivelRelativo)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                val statusPrerequisitos = remember(personagem.pericias.size) {
                    viewModel.validarPreRequisitosPericiaDetailed(definicao.id)
                }
                if (statusPrerequisitos.isNotEmpty()) {
                    Text("REQUISITOS:", style = MaterialTheme.typography.labelMedium)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            statusPrerequisitos.forEach { status ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (status.isMet) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (status.isMet) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    val detail = if (status.required.isNotEmpty() && status.required != "0") {
                                        " (${status.current}/${status.required})"
                                    } else ""
                                    
                                    Text(
                                        text = status.label + detail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (status.isMet) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                        fontWeight = if (status.isMet) FontWeight.Normal else FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                erroCadastro?.let {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
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
                                            pontosGastos = ajustarPontos(
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
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { pontosGastos = ajustarPontos(pontosGastos, incrementar = false) },
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
                            onClick = { pontosGastos = ajustarPontos(pontosGastos, incrementar = true) },
                            modifier = Modifier.semantics { contentDescription = "Aumentar pontos gastos da perícia" }
                        ) { Text("+") }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 2, 4, 8, 12, 16, 20, 24, 32).forEach { pts ->
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
        // A descrição de perícia costuma ser longa (Arrombamento, Mecânica).
        // Sem barra, o texto some no meio da frase e nada indica que há mais.
        val rolagemDescricao = rememberScrollState()
        AlertDialog(
            onDismissRequest = { mostrarDescricao = false },
            title = { Text(pericia.nome) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                    modifier = Modifier
                        .barraDeRolagem(rolagemDescricao)
                        .verticalScroll(rolagemDescricao)
                        .padding(end = 10.dp)
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
    val rolagem = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(definicao.nome) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                modifier = Modifier
                    .barraDeRolagem(rolagem)
                    .verticalScroll(rolagem)
                    .padding(end = 10.dp)
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




