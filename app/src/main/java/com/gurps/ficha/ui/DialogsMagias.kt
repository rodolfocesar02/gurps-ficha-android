package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.MagiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.viewmodel.FichaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarMagiaDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var magiaSelecionada by remember { mutableStateOf<MagiaDefinicao?>(null) }

    val listaFiltrada = viewModel.magiasFiltradas
    val escolas = viewModel.todasEscolasMagia
    val classes = viewModel.todasClassesMagia

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Selecionar Magia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.buscaMagia,
                    onValueChange = { viewModel.atualizarBuscaMagia(it) },
                    label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                // Filtro por Classe
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = viewModel.filtroClasseMagia == null,
                            onClick = { viewModel.atualizarFiltroClasseMagia(null) },
                            label = { Text("Todas classes") }
                        )
                    }
                    items(classes) { classe ->
                        FilterChip(
                            selected = viewModel.filtroClasseMagia == classe,
                            onClick = { viewModel.atualizarFiltroClasseMagia(classe) },
                            label = { Text(classe) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filtro por Escola
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = viewModel.filtroEscolaMagia == null,
                            onClick = { viewModel.atualizarFiltroEscolaMagia(null) },
                            label = { Text("Todos") }
                        )
                    }
                    items(escolas) { escola ->
                        FilterChip(
                            selected = viewModel.filtroEscolaMagia == escola,
                            onClick = { viewModel.atualizarFiltroEscolaMagia(escola) },
                            label = { Text(escola) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("${listaFiltrada.size} magias encontradas", style = MaterialTheme.typography.bodySmall)

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(listaFiltrada) { definicao ->
                        val jaAdicionada = viewModel.magiaJaAdicionada(definicao.id)
                        
                        // Formatando Classe e Escola
                        val classeEscola = listOfNotNull(
                            "IQ/${definicao.dificuldadeFixa ?: "D"}",
                            definicao.classe?.takeIf { it.isNotBlank() },
                            definicao.escola?.joinToString(" · ")?.takeIf { it.isNotBlank() }
                        ).joinToString(" · ")

                        ListItem(
                            headlineContent = { Text(definicao.nome) },
                            supportingContent = { Text("$classeEscola | pag. ${definicao.pagina}") },
                            trailingContent = {
                                if (jaAdicionada) {
                                    Text("Adicionada", color = MaterialTheme.colorScheme.outline)
                                }
                            },
                            modifier = Modifier.clickable(enabled = !jaAdicionada) { magiaSelecionada = definicao }
                        )
                        Divider()
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Fechar") }
                }
            }
        }
    }

    magiaSelecionada?.let { definicao ->
        ConfigurarMagiaDialog(
            definicao = definicao,
            personagem = viewModel.personagem,
            nivelAptidaoMagica = viewModel.nivelAptidaoMagica,
            onDismiss = { magiaSelecionada = null },
            onSave = { pontosGastos ->
                viewModel.adicionarMagia(definicao, pontosGastos)
                magiaSelecionada = null
            }
        )
    }
}

@Composable
fun ConfigurarMagiaDialog(
    definicao: MagiaDefinicao,
    personagem: Personagem,
    nivelAptidaoMagica: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var pontosGastos by remember { mutableStateOf(1) }
    val dificuldade = Dificuldade.fromSigla(definicao.dificuldadeFixa ?: "D")
    
    // Calcula nível preview
    val previewMagia = MagiaSelecionada(definicao.id, definicao.nome, dificuldade, pontosGastos, definicao.pagina, definicao.texto, definicao.classe, definicao.escola)
    val nivelPreview = previewMagia.calcularNivel(personagem, nivelAptidaoMagica)
    val nivelRelativo = previewMagia.getNivelRelativo(personagem, nivelAptidaoMagica)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar: ${definicao.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Dificuldade: ${dificuldade.nomeCompleto}", style = MaterialTheme.typography.bodyMedium)

                Divider()
                Text("Pontos Gastos:", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (pontosGastos > 4) pontosGastos -= 4
                        else if (pontosGastos > 2) pontosGastos = 2
                        else if (pontosGastos > 1) pontosGastos = 1
                    }) { Icon(Icons.Default.KeyboardArrowDown, null) }

                    Text(
                        "$pontosGastos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(onClick = {
                        if (pontosGastos < 4) pontosGastos *= 2
                        else if (pontosGastos < 36) pontosGastos += 4
                    }) { Icon(Icons.Default.KeyboardArrowUp, null) }
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
                        Text(
                            "NH: $nivelPreview (IQ+AM$nivelRelativo)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(pontosGastos) }) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancelar") }
        }
    )
}


@Composable
fun EditarMagiaDialog(
    magia: MagiaSelecionada,
    personagem: Personagem,
    nivelAptidaoMagica: Int,
    onDismiss: () -> Unit,
    onSave: (MagiaSelecionada) -> Unit
) {
    var pontosGastos by remember { mutableStateOf(magia.pontosGastos) }
    
    // Calcula nível preview
    val previewMagia = magia.copy(pontosGastos = pontosGastos)
    val nivelPreview = previewMagia.calcularNivel(personagem, nivelAptidaoMagica)
    val nivelRelativo = previewMagia.getNivelRelativo(personagem, nivelAptidaoMagica)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar: ${magia.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                val difNome = magia.dificuldade.sigla
                Text("IQ/$difNome", style = MaterialTheme.typography.bodyMedium)

                Divider()
                Text("Pontos Gastos:")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (pontosGastos > 4) pontosGastos -= 4
                        else if (pontosGastos > 2) pontosGastos = 2
                        else if (pontosGastos > 1) pontosGastos = 1
                    }) { Icon(Icons.Default.KeyboardArrowDown, null) }

                    Text(
                        "$pontosGastos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(onClick = {
                        if (pontosGastos < 4) pontosGastos *= 2
                        else if (pontosGastos < 36) pontosGastos += 4
                    }) { Icon(Icons.Default.KeyboardArrowUp, null) }
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
                        Text(
                            "NH: $nivelPreview (IQ+AM$nivelRelativo)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(magia.copy(pontosGastos = pontosGastos)) }) {
                Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// === DIALOGS SIMPLES ===


