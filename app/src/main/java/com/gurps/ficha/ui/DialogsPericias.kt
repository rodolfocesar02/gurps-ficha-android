package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gurps.ficha.model.AtributoBase
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.PericiaDefinicao
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.viewmodel.FichaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarPericiaDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var busca by remember { mutableStateOf("") }
    var filtroAtributo by remember { mutableStateOf<String?>(null) }
    var periciaSelecionada by remember { mutableStateOf<PericiaDefinicao?>(null) }

    val listaFiltrada = viewModel.dataRepository.filtrarPericias(busca, filtroAtributo, null)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(), shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("Selecionar Perícia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(value = busca, onValueChange = { busca = it }, label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null) })

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    TextButton(onClick = onDismiss) { Text("Fechar") }
                }
            }
        }
    }

    periciaSelecionada?.let { definicao ->
        ConfigurarPericiaDialog(definicao = definicao, personagem = viewModel.personagem, onDismiss = { periciaSelecionada = null },
            onSave = { pontosGastos, especializacao, atributo, dificuldade ->
                viewModel.adicionarPericia(definicao, pontosGastos, especializacao, atributo, dificuldade)
                periciaSelecionada = null
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarPericiaDialog(definicao: PericiaDefinicao, personagem: Personagem, onDismiss: () -> Unit,
    onSave: (Int, String, AtributoBase?, Dificuldade?) -> Unit) {
    var pontosGastos by remember { mutableStateOf(1) }
    var especializacao by remember { mutableStateOf("") }
    var atributoEscolhido by remember { mutableStateOf(AtributoBase.fromSigla(definicao.atributoBase)) }
    var dificuldadeEscolhida by remember { mutableStateOf(Dificuldade.fromSigla(definicao.dificuldadeFixa)) }

    val atributosPossiveis = definicao.atributosPossiveis?.map { AtributoBase.fromSigla(it) } ?: listOf(AtributoBase.fromSigla(definicao.atributoBase))
    val precisaEscolherAtributo = atributosPossiveis.size > 1 || definicao.atributoEscolhaObrigatoria

    // Calcula nível preview
    val previewPericia = PericiaSelecionada(definicao.id, definicao.nome, atributoEscolhido, dificuldadeEscolhida, pontosGastos, especializacao, definicao.exigeEspecializacao)
    val nivelPreview = previewPericia.calcularNivel(personagem)
    val nivelRelativo = previewPericia.getNivelRelativo(personagem)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar: ${definicao.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (pontosGastos > 4) pontosGastos -= 4
                        else if (pontosGastos > 2) pontosGastos = 2
                        else if (pontosGastos > 1) pontosGastos = 1
                    }) { Icon(Icons.Default.KeyboardArrowDown, null) }

                    Text("$pontosGastos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)

                    IconButton(onClick = {
                        if (pontosGastos < 4) pontosGastos *= 2
                        else if (pontosGastos < 36) pontosGastos += 4
                    }) { Icon(Icons.Default.KeyboardArrowUp, null) }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    listOf(1, 2, 4, 8, 12, 16, 20).forEach { pts ->
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
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("NH: $nivelPreview (${atributoEscolhido.sigla}$nivelRelativo)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(pontosGastos, especializacao, atributoEscolhido, dificuldadeEscolhida) },
                enabled = !definicao.exigeEspecializacao || especializacao.isNotBlank()
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// === DIALOGS DE EDICAO ===


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPericiaDialog(pericia: PericiaSelecionada, personagem: Personagem, onDismiss: () -> Unit, onSave: (PericiaSelecionada) -> Unit) {
    var pontosGastos by remember { mutableStateOf(pericia.pontosGastos) }
    var especializacao by remember { mutableStateOf(pericia.especializacao) }

    val previewPericia = pericia.copy(pontosGastos = pontosGastos, especializacao = especializacao)
    val nivelPreview = previewPericia.calcularNivel(personagem)
    val nivelRelativo = previewPericia.getNivelRelativo(personagem)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar: ${pericia.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${pericia.atributoBase.sigla}/${pericia.dificuldade.sigla}")

                OutlinedTextField(value = especializacao, onValueChange = {
                    if (it.length <= 20) {
                        especializacao = it
                    }
                },
                    label = { Text("Especialização") }, modifier = Modifier.fillMaxWidth())

                Text("Pontos Gastos:")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (pontosGastos > 4) pontosGastos -= 4
                        else if (pontosGastos > 2) pontosGastos = 2
                        else if (pontosGastos > 1) pontosGastos = 1
                    }) { Icon(Icons.Default.KeyboardArrowDown, null) }

                    Text("$pontosGastos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)

                    IconButton(onClick = {
                        if (pontosGastos < 4) pontosGastos *= 2
                        else if (pontosGastos < 36) pontosGastos += 4
                    }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                }

                Text("NH: $nivelPreview (${pericia.atributoBase.sigla}$nivelRelativo)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(pericia.copy(pontosGastos = pontosGastos, especializacao = especializacao)) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}


