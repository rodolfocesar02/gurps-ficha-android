package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gurps.ficha.model.*
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun ModeloRacialDialog(
    viewModel: FichaViewModel,
    modeloAtual: ModeloRacial? = null,
    onDismiss: () -> Unit,
    onSave: ((ModeloRacial) -> Unit)? = null
) {
    val modeloOriginal = modeloAtual ?: viewModel.personagem.modeloRacial
    var nome by remember { mutableStateOf(modeloOriginal.nome) }
    var descricaoRacial by remember { mutableStateOf(modeloOriginal.descricao) }
    
    // Atributos baseados no modelo ModeloRacial.kt
    var modForca by remember { mutableIntStateOf(modeloOriginal.modForca) }
    var modDestreza by remember { mutableIntStateOf(modeloOriginal.modDestreza) }
    var modInteligencia by remember { mutableIntStateOf(modeloOriginal.modInteligencia) }
    var modVitalidade by remember { mutableIntStateOf(modeloOriginal.modVitalidade) }
    var modPontosVida by remember { mutableIntStateOf(modeloOriginal.modPontosVida) }
    var modVontade by remember { mutableIntStateOf(modeloOriginal.modVontade) }
    var modPercepcao by remember { mutableIntStateOf(modeloOriginal.modPercepcao) }
    var modPontosFadiga by remember { mutableIntStateOf(modeloOriginal.modPontosFadiga) }
    var modVelocidadeBasica by remember { mutableFloatStateOf(modeloOriginal.modVelocidadeBasica) }
    var modDeslocamentoBasico by remember { mutableIntStateOf(modeloOriginal.modDeslocamentoBasico) }
    
    var vantagensRacais by remember { mutableStateOf(modeloOriginal.vantagens) }
    var desvantagensRacais by remember { mutableStateOf(modeloOriginal.desvantagens) }
    var periciasRacais by remember { mutableStateOf(modeloOriginal.pericias) }

    var showSelecionarVantagem by remember { mutableStateOf(false) }
    var showSelecionarDesvantagem by remember { mutableStateOf(false) }
    var showSelecionarPericia by remember { mutableStateOf(false) }
    
    var editingVantagemIndex by remember { mutableStateOf<Int?>(null) }
    var editingDesvantagemIndex by remember { mutableStateOf<Int?>(null) }
    var editingPericiaIndex by remember { mutableStateOf<Int?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // CABEÇALHO FIXO
                Box(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Personalizar Modelo Racial", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.Default.Close, "Fechar") }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item { OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome da Raça") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                    item { OutlinedTextField(value = descricaoRacial, onValueChange = { descricaoRacial = it }, label = { Text("Descrição (Aparência, Hábitos)") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }

                    // ATRIBUTOS
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Ajustes de Atributos", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    AjustadorVerticalRacial("ST", modForca, "Ajuste de Força (ST)") { modForca += it }
                                    AjustadorVerticalRacial("DX", modDestreza, "Ajuste de Destreza (DX)") { modDestreza += it }
                                    AjustadorVerticalRacial("IQ", modInteligencia, "Ajuste de Inteligência (IQ)") { modInteligencia += it }
                                    AjustadorVerticalRacial("HT", modVitalidade, "Ajuste de Vitalidade (HT)") { modVitalidade += it }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    AjustadorVerticalRacial("PV", modPontosVida, "Ajuste de Pontos de Vida (PV)") { modPontosVida += it }
                                    AjustadorVerticalRacial("Von", modVontade, "Ajuste de Vontade (Von)") { modVontade += it }
                                    AjustadorVerticalRacial("Per", modPercepcao, "Ajuste de Percepção (Per)") { modPercepcao += it }
                                    AjustadorVerticalRacial("PF", modPontosFadiga, "Ajuste de Pontos de Fadiga (PF)") { modPontosFadiga += it }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("VEL. Básica", style = MaterialTheme.typography.labelSmall)
                                        IconButton(onClick = { modVelocidadeBasica += 0.25f }, modifier = Modifier.size(32.dp).semantics { contentDescription = "Aumentar Velocidade Básica" }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                                        Text(String.format("%.2f", modVelocidadeBasica), fontWeight = FontWeight.Bold)
                                        IconButton(onClick = { modVelocidadeBasica -= 0.25f }, modifier = Modifier.size(32.dp).semantics { contentDescription = "Diminuir Velocidade Básica" }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                                    }
                                    AjustadorVerticalRacial("Desloc.", modDeslocamentoBasico, "Ajuste de Deslocamento Básico") { modDeslocamentoBasico += it }
                                }
                            }
                        }
                    }

                    // BOTÕES DE ADIÇÃO
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Traços e Habilidades Raciais", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(onClick = { showSelecionarVantagem = true }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Abrir seletor de Vantagem Racial" }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Adicionar Vantagem Racial") }
                            Button(onClick = { showSelecionarDesvantagem = true }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Abrir seletor de Desvantagem Racial" }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Adicionar Desvantagem Racial") }
                            Button(onClick = { showSelecionarPericia = true }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Abrir seletor de Perícia Racial" }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Adicionar Perícia Racial") }
                        }
                    }

                    // LISTAGENS PADRONIZADAS
                    if (vantagensRacais.isNotEmpty()) {
                        item { Text("Vantagens", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                        itemsIndexed(vantagensRacais) { index, v ->
                            ItemTraitRacial(nome = v.nome, detalhes = "${v.nivel} lvl | ${v.custoFinal} pts", onEdit = { editingVantagemIndex = index }, onDelete = { vantagensRacais = vantagensRacais.toMutableList().apply { removeAt(index) } })
                        }
                    }
                    if (desvantagensRacais.isNotEmpty()) {
                        item { Text("Desvantagens", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                        itemsIndexed(desvantagensRacais) { index, d ->
                            ItemTraitRacial(nome = d.nome, detalhes = "${d.nivel} lvl | ${d.custoFinal} pts", onEdit = { editingDesvantagemIndex = index }, onDelete = { desvantagensRacais = desvantagensRacais.toMutableList().apply { removeAt(index) } })
                        }
                    }
                    if (periciasRacais.isNotEmpty()) {
                        item { Text("Perícias Raciais (Bônus)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                        itemsIndexed(periciasRacais) { index, pr ->
                            ItemTraitRacial(
                                nome = pr.nome, 
                                detalhes = "${pr.baseAtributo}${if(pr.nivelRelativo >= 0) "+" else ""}${pr.nivelRelativo} (${pr.diff}) | Custo: ${pr.custo} pts", 
                                onEdit = { editingPericiaIndex = index }, 
                                onDelete = { periciasRacais = periciasRacais.toMutableList().apply { removeAt(index) } }
                            )
                        }
                    }

                    // RESUMO DE CUSTO
                    item {
                        val tempModelo = ModeloRacial(nome, modForca, modDestreza, modInteligencia, modVitalidade, modPontosVida, modVontade, modPercepcao, modPontosFadiga, modVelocidadeBasica, modDeslocamentoBasico, vantagensRacais, desvantagensRacais, periciasRacais, descricaoRacial)
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Text("Custo Total Racial: ${tempModelo.custoTotal} pontos", modifier = Modifier.padding(16.dp).fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // RODAPÉ FIXO
                Surface(shadowElevation = 8.dp) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                        Button(
                            onClick = { 
                                val novoModelo = ModeloRacial(nome, modForca, modDestreza, modInteligencia, modVitalidade, modPontosVida, modVontade, modPercepcao, modPontosFadiga, modVelocidadeBasica, modDeslocamentoBasico, vantagensRacais, desvantagensRacais, periciasRacais, descricaoRacial)
                                if (onSave != null) onSave(novoModelo)
                                else {
                                    viewModel.atualizarModeloRacial(novoModelo)
                                    onDismiss()
                                }
                            }, 
                            modifier = Modifier.weight(1f)
                        ) { Text("Salvar Modelo") }
                    }
                }
            }
        }
    }

    // DIÁLOGOS DE APOIO (Vantagens/Desvantagens usam a lógica do ViewModel)
    if (showSelecionarVantagem) { SelecionarVantagemDialog(viewModel = viewModel, onDismiss = { showSelecionarVantagem = false }) }
    if (showSelecionarDesvantagem) { SelecionarDesvantagemDialog(viewModel = viewModel, onDismiss = { showSelecionarDesvantagem = false }) }
    
    // PERÍCIA RACIAL (Lógica integrada e segura)
    var periciaEmCriacao by remember { mutableStateOf<PericiaRacial?>(null) }
    if (showSelecionarPericia) {
        var filtroBusca by remember { mutableStateOf("") }
        val listaFiltrada = viewModel.dataRepository.pericias.filter { it.nome.contains(filtroBusca, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { showSelecionarPericia = false },
            title = { Text("Selecionar Perícia") },
            text = {
                Column {
                    OutlinedTextField(value = filtroBusca, onValueChange = { filtroBusca = it }, label = { Text("Nome da Perícia") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(listaFiltrada) { p ->
                            ListItem(headlineContent = { Text(p.nome) }, modifier = Modifier.clickable {
                                periciaEmCriacao = PericiaRacial(p.nome, p.atributoBase, p.dificuldadeFixa ?: "M", 1, 0)
                                showSelecionarPericia = false
                            })
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSelecionarPericia = false }) { Text("Fechar") } }
        )
    }

    val prParaEditar = periciaEmCriacao ?: if (editingPericiaIndex != null) periciasRacais[editingPericiaIndex!!] else null
    if (prParaEditar != null) {
        var nivelRelativo by remember(prParaEditar) { mutableIntStateOf(prParaEditar.nivelRelativo) }
        AlertDialog(
            onDismissRequest = { periciaEmCriacao = null; editingPericiaIndex = null },
            title = { Text("Configurar Bônus Racial: ${prParaEditar.nome}") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Pontos de bônus no Nível de Habilidade", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { nivelRelativo-- }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                        Text("${if(nivelRelativo>=0) "+" else ""}$nivelRelativo", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                        IconButton(onClick = { nivelRelativo++ }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                    }
                    val custoFinal = when {
                        nivelRelativo > 0 -> {
                            val base = if (prParaEditar.diff == "MD") 4 else if (prParaEditar.diff == "D") 2 else 1
                            if (nivelRelativo == 1) base else base * (nivelRelativo * 2)
                        }
                        nivelRelativo < 0 -> nivelRelativo * 2
                        else -> 0
                    }
                    Text("Custo: $custoFinal pontos", color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val custoFinal = when {
                        nivelRelativo > 0 -> {
                            val base = if (prParaEditar.diff == "MD") 4 else if (prParaEditar.diff == "D") 2 else 1
                            if (nivelRelativo == 1) base else base * (nivelRelativo * 2)
                        }
                        nivelRelativo < 0 -> nivelRelativo * 2
                        else -> 0
                    }
                    val novaPericia = prParaEditar.copy(nivelRelativo = nivelRelativo, custo = custoFinal)
                    if (periciaEmCriacao != null) periciasRacais = periciasRacais + novaPericia
                    else if (editingPericiaIndex != null) periciasRacais = periciasRacais.toMutableList().apply { this[editingPericiaIndex!!] = novaPericia }
                    periciaEmCriacao = null; editingPericiaIndex = null
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { periciaEmCriacao = null; editingPericiaIndex = null }) { Text("Cancelar") } }
        )
    }

    // SINCRONIZAÇÃO COM O VIEWMODEL (Manteve segunça contra null e nomes corretos)
    LaunchedEffect(viewModel.personagem.vantagens.size) {
        if (showSelecionarVantagem && viewModel.personagem.vantagens.isNotEmpty()) {
            vantagensRacais = vantagensRacais + viewModel.personagem.vantagens.last()
            viewModel.removerVantagem(viewModel.personagem.vantagens.size - 1)
            showSelecionarVantagem = false
        }
    }
    LaunchedEffect(viewModel.personagem.desvantagens.size) {
        if (showSelecionarDesvantagem && viewModel.personagem.desvantagens.isNotEmpty()) {
            desvantagensRacais = desvantagensRacais + viewModel.personagem.desvantagens.last()
            viewModel.removerDesvantagem(viewModel.personagem.desvantagens.size - 1)
            showSelecionarDesvantagem = false
        }
    }

    // EDIÇÃO DE TRAÇOS (V2 utiliza os diálogos originais do TraitDialogs.kt)
    editingVantagemIndex?.let { i ->
        EditarVantagemDialog(vantagensRacais[i], "", { editingVantagemIndex = null }, { n -> 
            vantagensRacais = vantagensRacais.toList().mapIndexed { idx, v -> if (idx == i) n else v }; editingVantagemIndex = null 
        })
    }
    editingDesvantagemIndex?.let { i ->
        EditarDesvantagemDialog(desvantagensRacais[i], "", { editingDesvantagemIndex = null }, { n -> 
            desvantagensRacais = desvantagensRacais.toList().mapIndexed { idx, d -> if (idx == i) n else d }; editingDesvantagemIndex = null 
        })
    }
}

@Composable
fun ItemTraitRacial(nome: String, detalhes: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        ListItem(
            headlineContent = { Text(nome, fontWeight = FontWeight.Bold) },
            supportingContent = { Text(detalhes) },
            trailingContent = {
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Editar") }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Excluir", tint = MaterialTheme.colorScheme.error) }
                }
            }
        )
    }
}

@Composable
fun AjustadorVerticalRacial(rotulo: String, valor: Int, nomeCompleto: String, onDelta: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(rotulo, style = MaterialTheme.typography.labelSmall)
        IconButton(onClick = { onDelta(1) }, modifier = Modifier.size(32.dp).semantics { contentDescription = "Aumentar $nomeCompleto" }) { Icon(Icons.Default.KeyboardArrowUp, null) }
        Text("${if(valor>0) "+" else ""}$valor", fontWeight = FontWeight.Bold)
        IconButton(onClick = { onDelta(-1) }, modifier = Modifier.size(32.dp).semantics { contentDescription = "Diminuir $nomeCompleto" }) { Icon(Icons.Default.KeyboardArrowDown, null) }
    }
}
