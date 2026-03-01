package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.model.PericiaSuplementarItem
import com.gurps.ficha.model.TecnicaCatalogoItem
import com.gurps.ficha.model.TecnicaSelecionada
import com.gurps.ficha.viewmodel.FichaViewModel

private val TECNICA_PONTOS_PRESETS = listOf(1, 2, 4, 8, 12)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarTecnicaDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit
) {
    var busca by remember { mutableStateOf("") }
    var filtroFonte by remember { mutableStateOf<String?>(null) }
    var tecnicaSelecionada by remember { mutableStateOf<TecnicaCatalogoItem?>(null) }

    val fontes = remember(viewModel.tecnicasCatalogo) {
        viewModel.tecnicasCatalogo.map { it.sourceBook }.distinct().sorted()
    }
    val tecnicas = viewModel.tecnicasCatalogo.filter { tecnica ->
        val matchBusca = busca.isBlank() ||
            tecnica.nome.contains(busca, ignoreCase = true) ||
            tecnica.descricao.contains(busca, ignoreCase = true)
        val matchFonte = filtroFonte.isNullOrBlank() || tecnica.sourceBook.equals(filtroFonte, ignoreCase = true)
        matchBusca && matchFonte
    }

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Selecionar Técnica", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                label = { Text("Buscar técnica...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = filtroFonte == null,
                    onClick = { filtroFonte = null },
                    label = { Text("Todas") }
                )
                fontes.forEach { fonte ->
                    FilterChip(
                        selected = filtroFonte == fonte,
                        onClick = { filtroFonte = fonte },
                        label = { Text(fonte) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("${tecnicas.size} técnicas encontradas", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tecnicas) { tecnica ->
                    val jaAdicionada = viewModel.tecnicaJaAdicionada(tecnica.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !jaAdicionada) { tecnicaSelecionada = tecnica },
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(tecnica.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                            Text(
                                "${tecnica.sourceBook} • ${tecnica.dificuldadeRaw}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (jaAdicionada) {
                                Text("Adicionada", color = MaterialTheme.colorScheme.outline)
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

    tecnicaSelecionada?.let { definicao ->
        ConfigurarTecnicaDialog(
            definicao = definicao,
            onDismiss = { tecnicaSelecionada = null },
            onSave = { pontos ->
                viewModel.adicionarTecnica(definicao, pontos)
                tecnicaSelecionada = null
            }
        )
    }
}

@Composable
fun ConfigurarTecnicaDialog(
    definicao: TecnicaCatalogoItem,
    onDismiss: () -> Unit,
    onSave: (pontosGastos: Int) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var pontosGastos by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Técnica") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(definicao.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${definicao.sourceBook} • ${definicao.dificuldadeRaw}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (definicao.preRequisitoRaw.isNotBlank()) {
                    Text("Pré-requisito: ${definicao.preRequisitoRaw}", style = MaterialTheme.typography.bodySmall)
                }
                if (definicao.preDefinidoRaw.isNotBlank()) {
                    Text("Pré-definido: ${definicao.preDefinidoRaw}", style = MaterialTheme.typography.bodySmall)
                }
                Text("Pontos Gastos:", style = MaterialTheme.typography.labelMedium)
                Text(
                    "$pontosGastos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(56.dp)
                )
                if (isPraCegoVariant) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(
                            onClick = { pontosGastos = TECNICA_PONTOS_PRESETS[(TECNICA_PONTOS_PRESETS.indexOf(pontosGastos) - 1).coerceAtLeast(0)] },
                            modifier = Modifier.semantics { contentDescription = "Diminuir pontos da técnica" }
                        ) { Text("-") }
                        TextButton(
                            onClick = { pontosGastos = TECNICA_PONTOS_PRESETS[(TECNICA_PONTOS_PRESETS.indexOf(pontosGastos) + 1).coerceAtMost(TECNICA_PONTOS_PRESETS.lastIndex)] },
                            modifier = Modifier.semantics { contentDescription = "Aumentar pontos da técnica" }
                        ) { Text("+") }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TECNICA_PONTOS_PRESETS.forEach { pontos ->
                        TextButton(
                            onClick = { pontosGastos = pontos },
                            modifier = Modifier.padding(horizontal = 1.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) { Text("$pontos") }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(pontosGastos) }) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun EditarTecnicaDialog(
    tecnica: TecnicaSelecionada,
    onDismiss: () -> Unit,
    onSave: (TecnicaSelecionada) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var pontosGastos by remember { mutableStateOf(tecnica.pontosGastos) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Técnica") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tecnica.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${tecnica.sourceBook} • ${tecnica.dificuldadeRaw}", style = MaterialTheme.typography.bodySmall)
                Text("Pontos Gastos:", style = MaterialTheme.typography.labelMedium)
                Text("$pontosGastos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (isPraCegoVariant) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(
                            onClick = { pontosGastos = TECNICA_PONTOS_PRESETS[(TECNICA_PONTOS_PRESETS.indexOf(pontosGastos) - 1).coerceAtLeast(0)] },
                            modifier = Modifier.semantics { contentDescription = "Diminuir pontos da técnica" }
                        ) { Text("-") }
                        TextButton(
                            onClick = { pontosGastos = TECNICA_PONTOS_PRESETS[(TECNICA_PONTOS_PRESETS.indexOf(pontosGastos) + 1).coerceAtMost(TECNICA_PONTOS_PRESETS.lastIndex)] },
                            modifier = Modifier.semantics { contentDescription = "Aumentar pontos da técnica" }
                        ) { Text("+") }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TECNICA_PONTOS_PRESETS.forEach { pontos ->
                        TextButton(
                            onClick = { pontosGastos = pontos },
                            modifier = Modifier.padding(horizontal = 1.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) { Text("$pontos") }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(tecnica.copy(pontosGastos = pontosGastos.coerceAtLeast(1))) }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun PericiasSuplementaresDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit
) {
    var busca by remember { mutableStateOf("") }
    val itens = viewModel.periciasSuplementaresArtesMarciais.filter { pericia ->
        busca.isBlank() ||
            pericia.nome.contains(busca, ignoreCase = true) ||
            pericia.descricao.contains(busca, ignoreCase = true)
    }

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Perícias Suplementares", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                label = { Text("Buscar perícia...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${itens.size} perícias encontradas", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(itens) { pericia ->
                    PericiaSuplementarCard(item = pericia)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Fechar") }
            }
        }
    }
}

@Composable
private fun PericiaSuplementarCard(item: PericiaSuplementarItem) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(
                "${item.sourceBook} • ${item.dificuldadeRaw}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (item.preRequisitoRaw.isNotBlank()) {
                Text("Pré-requisito: ${item.preRequisitoRaw}", style = MaterialTheme.typography.bodySmall)
            }
            if (item.descricao.isNotBlank()) {
                Text(item.descricao, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
