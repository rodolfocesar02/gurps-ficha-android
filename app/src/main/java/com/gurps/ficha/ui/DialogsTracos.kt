package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gurps.ficha.model.DesvantagemDefinicao
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.TipoCusto
import com.gurps.ficha.model.VantagemDefinicao
import com.gurps.ficha.model.VantagemSelecionada
import com.gurps.ficha.viewmodel.FichaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarVantagemDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var busca by remember { mutableStateOf("") }
    var filtroTipo by remember { mutableStateOf<TipoCusto?>(null) }
    var filtroTag by remember { mutableStateOf<String?>(null) }
    var vantagemSelecionada by remember { mutableStateOf<VantagemDefinicao?>(null) }

    val tagsDisponiveis = listOf("combate", "social", "fisica", "mental", "magica")
    val listaFiltrada = viewModel.dataRepository.filtrarVantagens(busca, filtroTipo, filtroTag)

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Selecionar Vantagem", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = busca, onValueChange = { busca = it }, label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) })

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroTag == null,
                        onClick = { filtroTag = null },
                        label = { Text("Todas") }
                    )
                    tagsDisponiveis.forEach { tag ->
                        FilterChip(
                            selected = filtroTag == tag,
                            onClick = { filtroTag = tag },
                            label = { Text(tag) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("${listaFiltrada.size} vantagens encontradas", style = MaterialTheme.typography.bodySmall)

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(listaFiltrada) { definicao ->
                        val jaAdicionada = viewModel.vantagemJaAdicionada(definicao.id)
                        ListItem(
                            headlineContent = { Text(definicao.nome, fontWeight = if (jaAdicionada) FontWeight.Normal else FontWeight.Medium) },
                            supportingContent = { Text("${definicao.custo} pts | ${definicao.tipoCusto.name.lowercase()} | pag. ${definicao.pagina}") },
                            trailingContent = { if (jaAdicionada) Text("Adicionada", color = MaterialTheme.colorScheme.outline) },
                            modifier = Modifier.clickable(enabled = !jaAdicionada) { vantagemSelecionada = definicao }
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

    vantagemSelecionada?.let { definicao ->
        ConfigurarVantagemDialog(definicao = definicao, onDismiss = { vantagemSelecionada = null },
            onSave = { nivel, custoEscolhido, descricao ->
                viewModel.adicionarVantagem(definicao, nivel, custoEscolhido, descricao)
                vantagemSelecionada = null
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarVantagemDialog(definicao: VantagemDefinicao, onDismiss: () -> Unit, onSave: (Int, Int, String) -> Unit) {
    var nivel by remember { mutableStateOf(1) }
    var custoEscolhido by remember { mutableStateOf(definicao.getCustoBase()) }
    var descricao by remember { mutableStateOf("") }

    val opcoesEscolha = definicao.getOpcoesEscolha()
    val intervalo = definicao.getIntervaloVariavel()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar: ${definicao.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tipo: ${definicao.tipoCusto.name} | Custo base: ${definicao.custo} | Pag. ${definicao.pagina}", style = MaterialTheme.typography.bodySmall)

                when (definicao.tipoCusto) {
                    TipoCusto.POR_NIVEL -> {
                        Text("Nível:")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (nivel > 1) nivel-- }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                            Text("$nivel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { if (nivel < 10) nivel++ }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                        }
                        Text("Custo: ${definicao.getCustoPorNivel() * nivel} pts", fontWeight = FontWeight.Bold)
                    }
                    TipoCusto.ESCOLHA -> {
                        Text("Escolha o custo:")
                        opcoesEscolha.forEach { opcao ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { custoEscolhido = opcao }) {
                                RadioButton(selected = custoEscolhido == opcao, onClick = { custoEscolhido = opcao })
                                Text("$opcao pts")
                            }
                        }
                    }
                    TipoCusto.VARIAVEL -> {
                        Text("Custo (${intervalo.first} a ${intervalo.second}):")
                        OutlinedTextField(value = custoEscolhido.toString(), onValueChange = { custoEscolhido = it.toIntOrNull() ?: intervalo.first },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    TipoCusto.FIXO -> {
                        Text("Custo fixo: ${definicao.getCustoBase()} pts", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(value = descricao, onValueChange = { descricao = it },
                    label = { Text("Descrição/Especialização") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val custoFinal = when (definicao.tipoCusto) {
                    TipoCusto.POR_NIVEL -> definicao.getCustoPorNivel() * nivel
                    TipoCusto.ESCOLHA, TipoCusto.VARIAVEL -> custoEscolhido
                    TipoCusto.FIXO -> definicao.getCustoBase()
                }
                onSave(nivel, custoFinal, descricao)
            }) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarDesvantagemDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var busca by remember { mutableStateOf("") }
    var filtroTipo by remember { mutableStateOf<TipoCusto?>(null) }
    var filtroTag by remember { mutableStateOf<String?>(null) }
    var desvantagemSelecionada by remember { mutableStateOf<DesvantagemDefinicao?>(null) }

    val tagsDisponiveis = listOf("combate", "social", "fisica", "mental", "magica")
    val listaFiltrada = viewModel.dataRepository.filtrarDesvantagens(busca, filtroTipo, filtroTag)

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Selecionar Desvantagem", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Text("Atual: ${viewModel.pontosDesvantagens} pts", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = busca, onValueChange = { busca = it }, label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null) })

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroTag == null,
                        onClick = { filtroTag = null },
                        label = { Text("Todas") }
                    )
                    tagsDisponiveis.forEach { tag ->
                        FilterChip(
                            selected = filtroTag == tag,
                            onClick = { filtroTag = tag },
                            label = { Text(tag) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("${listaFiltrada.size} desvantagens encontradas", style = MaterialTheme.typography.bodySmall)

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(listaFiltrada) { definicao ->
                        val jaAdicionada = viewModel.desvantagemJaAdicionada(definicao.id)
                        ListItem(
                            headlineContent = { Text(definicao.nome) },
                            supportingContent = { Text("${definicao.custo} pts | ${definicao.tipoCusto.name.lowercase()} | pag. ${definicao.pagina}") },
                            trailingContent = { if (jaAdicionada) Text("Adicionada", color = MaterialTheme.colorScheme.outline) },
                            modifier = Modifier.clickable(enabled = !jaAdicionada) { desvantagemSelecionada = definicao }
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

    desvantagemSelecionada?.let { definicao ->
        ConfigurarDesvantagemDialog(definicao = definicao, onDismiss = { desvantagemSelecionada = null },
            onSave = { nivel, custoEscolhido, descricao, autocontrole ->
                viewModel.adicionarDesvantagem(definicao, nivel, custoEscolhido, descricao, autocontrole)
                desvantagemSelecionada = null
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarDesvantagemDialog(definicao: DesvantagemDefinicao, onDismiss: () -> Unit, onSave: (Int, Int, String, Int?) -> Unit) {
    var nivel by remember { mutableStateOf(1) }
    var custoEscolhido by remember { mutableStateOf(definicao.getCustoBase()) }
    var descricao by remember { mutableStateOf("") }
    var autocontrole by remember { mutableStateOf<Int?>(null) }

    val opcoesEscolha = definicao.getOpcoesEscolha()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar: ${definicao.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Tipo: ${definicao.tipoCusto.name} | Custo base: ${definicao.custo} | Pag. ${definicao.pagina}", style = MaterialTheme.typography.bodySmall)

                when (definicao.tipoCusto) {
                    TipoCusto.POR_NIVEL -> {
                        Text("Nível:")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (nivel > 1) nivel-- }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                            Text("$nivel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { if (nivel < 10) nivel++ }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                        }
                        Text("Custo: ${definicao.getCustoPorNivel() * nivel} pts", fontWeight = FontWeight.Bold)
                    }
                    TipoCusto.ESCOLHA -> {
                        Text("Escolha o custo:")
                        opcoesEscolha.forEach { opcao ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { custoEscolhido = opcao }) {
                                RadioButton(selected = custoEscolhido == opcao, onClick = { custoEscolhido = opcao })
                                Text("$opcao pts")
                            }
                        }
                    }
                    TipoCusto.VARIAVEL -> {
                        Text("Custo:")
                        OutlinedTextField(value = custoEscolhido.toString(), onValueChange = { custoEscolhido = it.toIntOrNull() ?: -10 },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                    TipoCusto.FIXO -> {
                        Text("Custo fixo: ${definicao.getCustoBase()} pts", fontWeight = FontWeight.Bold)
                    }
                }

                Divider()
                Text("Autocontrole (opcional):", style = MaterialTheme.typography.labelMedium)
                Text("GURPS 4Ed pag. 120 - multiplicadores", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(null to "Nenhum", 6 to "6 (x2)", 9 to "9 (x1.5)", 12 to "12 (x1)", 15 to "15 (x0.5)").forEach { (valor, label) ->
                        FilterChip(selected = autocontrole == valor, onClick = { autocontrole = valor }, label = { Text(label, fontSize = 10.sp) })
                    }
                }

                OutlinedTextField(value = descricao, onValueChange = { descricao = it },
                    label = { Text("Descrição/Especialização") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val custoFinal = when (definicao.tipoCusto) {
                    TipoCusto.POR_NIVEL -> definicao.getCustoPorNivel() * nivel
                    TipoCusto.ESCOLHA, TipoCusto.VARIAVEL -> custoEscolhido
                    TipoCusto.FIXO -> definicao.getCustoBase()
                }
                onSave(nivel, custoFinal, descricao, autocontrole)
            }) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}


@Composable
fun EditarVantagemDialog(vantagem: VantagemSelecionada, onDismiss: () -> Unit, onSave: (VantagemSelecionada) -> Unit) {
    var nivel by remember { mutableStateOf(vantagem.nivel) }
    var custoEscolhido by remember { mutableStateOf(vantagem.custoEscolhido) }
    var descricao by remember { mutableStateOf(vantagem.descricao) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar: ${vantagem.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (vantagem.tipoCusto == TipoCusto.POR_NIVEL) {
                    Text("Nível:")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (nivel > 1) nivel-- }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                        Text("$nivel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { if (nivel < 10) nivel++ }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                    }
                }
                if (vantagem.tipoCusto == TipoCusto.VARIAVEL || vantagem.tipoCusto == TipoCusto.ESCOLHA) {
                    OutlinedTextField(value = custoEscolhido.toString(), onValueChange = { custoEscolhido = it.toIntOrNull() ?: custoEscolhido },
                        label = { Text("Custo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(value = descricao, onValueChange = { descricao = it },
                    label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onSave(vantagem.copy(nivel = nivel, custoEscolhido = custoEscolhido, descricao = descricao)) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarDesvantagemDialog(desvantagem: DesvantagemSelecionada, onDismiss: () -> Unit, onSave: (DesvantagemSelecionada) -> Unit) {
    var nivel by remember { mutableStateOf(desvantagem.nivel) }
    var custoEscolhido by remember { mutableStateOf(desvantagem.custoEscolhido) }
    var descricao by remember { mutableStateOf(desvantagem.descricao) }
    var autocontrole by remember { mutableStateOf(desvantagem.autocontrole) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar: ${desvantagem.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (desvantagem.tipoCusto == TipoCusto.POR_NIVEL) {
                    Text("Nível:")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (nivel > 1) nivel-- }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                        Text("$nivel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { if (nivel < 10) nivel++ }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                    }
                }
                if (desvantagem.tipoCusto == TipoCusto.VARIAVEL || desvantagem.tipoCusto == TipoCusto.ESCOLHA) {
                    OutlinedTextField(value = custoEscolhido.toString(), onValueChange = { custoEscolhido = it.toIntOrNull() ?: custoEscolhido },
                        label = { Text("Custo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
                Text("Autocontrole:")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(null to "Nenhum", 6 to "6", 9 to "9", 12 to "12", 15 to "15").forEach { (valor, label) ->
                        FilterChip(selected = autocontrole == valor, onClick = { autocontrole = valor }, label = { Text(label) })
                    }
                }
                OutlinedTextField(value = descricao, onValueChange = { descricao = it },
                    label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onSave(desvantagem.copy(nivel = nivel, custoEscolhido = custoEscolhido, descricao = descricao, autocontrole = autocontrole)) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}


@Composable
fun PeculiaridadeDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var texto by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Peculiaridade") },
        text = {
            Column {
                Text("Peculiaridades são mini-desvantagens (-1 pt cada, máx 5)", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = texto, onValueChange = { texto = it }, label = { Text("Peculiaridade") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { if (texto.isNotBlank()) onSave(texto) }) { Text("Adicionar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}


