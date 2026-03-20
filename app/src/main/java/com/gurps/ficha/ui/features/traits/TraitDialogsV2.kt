package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gurps.ficha.model.*
import com.gurps.ficha.ui.*
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun ModeloRacialDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var showCatalogo by remember { mutableStateOf(false) }
    var showPersonalizar by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modelo Racial", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PrimaryActionButton(text = "Escolher do Catálogo", onClick = { showCatalogo = true }, modifier = Modifier.fillMaxWidth())
                PrimaryActionButton(text = "Personalizar Raça", onClick = { showPersonalizar = true }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )

    if (showCatalogo) {
        CatalogoRacasDialog(onDismiss = { showCatalogo = false }) { raca ->
            viewModel.personagem.modeloRacial = raca
            showCatalogo = false
            onDismiss()
        }
    }

    if (showPersonalizar) {
        PersonalizarRacaDialog(
            viewModel = viewModel,
            initial = viewModel.personagem.modeloRacial ?: ModeloRacial("Nova Raça"),
            onDismiss = { showPersonalizar = false },
            onSave = { raca ->
                viewModel.personagem.modeloRacial = raca
                showPersonalizar = false
                onDismiss()
            }
        )
    }
}

@Composable
fun CatalogoRacasDialog(onDismiss: () -> Unit, onSelect: (ModeloRacial) -> Unit) {
    val racas = listOf(
        ModeloRacial("Anão", modVitalidade = 1, vantagens = listOf(VantagemSelecionada(nome = "Resistência à Magia 1", custoBase = 2))),
        ModeloRacial("Elfo", modDestreza = 1, vantagens = listOf(VantagemSelecionada(nome = "Aparência (Atraente)", custoBase = 4))),
        ModeloRacial("Orc", modForca = 2, modInteligencia = -1, desvantagens = listOf(DesvantagemSelecionada(nome = "Aparência (Feio)", custoBase = -8)))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catálogo de Raças") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(racas) { _, r ->
                    Card(onClick = { onSelect(r) }, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(r.nome, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Custo: ${r.custoTotal} pts", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun PersonalizarRacaDialog(viewModel: FichaViewModel, initial: ModeloRacial, onDismiss: () -> Unit, onSave: (ModeloRacial) -> Unit) {
    var nome by remember { mutableStateOf(initial.nome) }
    var modST by remember { mutableIntStateOf(initial.modForca) }
    var modDX by remember { mutableIntStateOf(initial.modDestreza) }
    var modIQ by remember { mutableIntStateOf(initial.modInteligencia) }
    var modHT by remember { mutableIntStateOf(initial.modVitalidade) }
    var modHP by remember { mutableIntStateOf(initial.modPontosVida) }
    var modVon by remember { mutableIntStateOf(initial.modVontade) }
    var modPer by remember { mutableIntStateOf(initial.modPercepcao) }
    var modPF by remember { mutableIntStateOf(initial.modPontosFadiga) }
    var modVB by remember { mutableFloatStateOf(initial.modVelocidadeBasica) }
    var modDB by remember { mutableIntStateOf(initial.modDeslocamentoBasico) }
    
    var vantagensRacais by remember { mutableStateOf(initial.vantagens) }
    var desvantagensRacais by remember { mutableStateOf(initial.desvantagens) }
    var periciasRacais by remember { mutableStateOf(initial.pericias) }
    var descricaoRacial by remember { mutableStateOf(initial.descricao) }

    var showSelecionarVantagem by remember { mutableStateOf(false) }
    var showSelecionarDesvantagem by remember { mutableStateOf(false) }
    var showSelecionarPericia by remember { mutableStateOf(false) }

    var editingVantagemIndex by remember { mutableStateOf<Int?>(null) }
    var editingDesvantagemIndex by remember { mutableStateOf<Int?>(null) }
    var editingPericiaIndex by remember { mutableStateOf<Int?>(null) }

    val tempPersonagem = viewModel.personagem.copy(
        forca = viewModel.personagem.forca + modST,
        destreza = viewModel.personagem.destreza + modDX,
        inteligencia = viewModel.personagem.inteligencia + modIQ,
        vitalidade = viewModel.personagem.vitalidade + modHT
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personalizar Raça", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                item { OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome da Raça") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                item { OutlinedTextField(value = descricaoRacial, onValueChange = { descricaoRacial = it }, label = { Text("Descrição (Aparência)") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }
                
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Atributos Primários", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                AjustadorVerticalRacial("ST", modST, "Força") { modST += it }
                                AjustadorVerticalRacial("DX", modDX, "Destreza") { modDX += it }
                                AjustadorVerticalRacial("IQ", modIQ, "Inteligência") { modIQ += it }
                                AjustadorVerticalRacial("HT", modHT, "Vitalidade") { modHT += it }
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Atributos Secundários", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                AjustadorVerticalRacial("PV", modHP, "Pontos de Vida") { modHP += it }
                                AjustadorVerticalRacial("Von", modVon, "Vontade") { modVon += it }
                                AjustadorVerticalRacial("Per", modPer, "Percepção") { modPer += it }
                                AjustadorVerticalRacial("PF", modPF, "Pontos de Fadiga") { modPF += it }
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Características Derivadas", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("VEL. Básica", style = MaterialTheme.typography.labelSmall)
                                    IconButton(
                                        onClick = { modVB += 0.25f },
                                        modifier = Modifier.size(32.dp).semantics { contentDescription = "Aumentar Velocidade Básica em 0.25" }
                                    ) { Icon(Icons.Default.KeyboardArrowUp, null) }
                                    
                                    Text(
                                        String.format("%.2f", modVB),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.semantics { contentDescription = "Bônus de Velocidade Básica atual: ${String.format("%.2f", modVB)}" }
                                    )
                                    
                                    IconButton(
                                        onClick = { modVB -= 0.25f },
                                        modifier = Modifier.size(32.dp).semantics { contentDescription = "Diminuir Velocidade Básica em 0.25" }
                                    ) { Icon(Icons.Default.KeyboardArrowDown, null) }
                                }
                                AjustadorVerticalRacial("Desloc.", modDB, "Deslocamento Básico") { modDB += it }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Traços Racais", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        PrimaryActionButton(text = "Adicionar Vantagem Racial", onClick = { showSelecionarVantagem = true }, modifier = Modifier.fillMaxWidth())
                        PrimaryActionButton(text = "Adicionar Desvantagem Racial", onClick = { showSelecionarDesvantagem = true }, modifier = Modifier.fillMaxWidth())
                        PrimaryActionButton(text = "Adicionar Perícia Racial", onClick = { showSelecionarPericia = true }, modifier = Modifier.fillMaxWidth())
                    }
                }

                if (vantagensRacais.isNotEmpty()) {
                    item { Text("Vantagens", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) }
                    itemsIndexed(vantagensRacais) { index, v ->
                        AppListItemCard { VantagemItem(vantagem = v, onEdit = { editingVantagemIndex = index }, onDelete = { vantagensRacais = vantagensRacais.toMutableList().apply { removeAt(index) } }) }
                    }
                }

                if (desvantagensRacais.isNotEmpty()) {
                    item { Text("Desvantagens", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) }
                    itemsIndexed(desvantagensRacais) { index, d ->
                        AppListItemCard { DesvantagemItem(desvantagem = d, exibirAutocontrole = true, onEdit = { editingDesvantagemIndex = index }, onDelete = { desvantagensRacais = desvantagensRacais.toMutableList().apply { removeAt(index) } }) }
                    }
                }

                if (periciasRacais.isNotEmpty()) {
                    item { Text("Perícias", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) }
                    itemsIndexed(periciasRacais) { index, pr ->
                        // CONVERSÃO TEMPORÁRIA PARA EXIBIÇÃO NO CARD DA FICHA
                        val ps = PericiaSelecionada(
                            nome = pr.nome,
                            dificuldade = Dificuldade.fromSigla(pr.diff),
                            atributoBase = AtributoBase.fromSigla(pr.baseAtributo),
                            pontosGastos = pr.custo
                        )
                        AppListItemCard {
                            PericiaItem(
                                pericia = ps,
                                nivel = ps.calcularNivel(tempPersonagem),
                                nivelRelativo = ps.getNivelRelativo(tempPersonagem),
                                onShowDescription = { },
                                onEdit = { editingPericiaIndex = index },
                                onDelete = { periciasRacais = periciasRacais.toMutableList().apply { removeAt(index) } }
                            )
                        }
                    }
                }

                item {
                    val tempModelo = ModeloRacial(nome, modST, modDX, modIQ, modHT, modHP, modVon, modPer, modPF, modVB, modDB, vantagensRacais, desvantagensRacais, periciasRacais, descricaoRacial)
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Text("Resumo de Bônus Racial: ${tempModelo.custoTotal} pontos", modifier = Modifier.padding(12.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                onSave(ModeloRacial(nome, modST, modDX, modIQ, modHT, modHP, modVon, modPer, modPF, modVB, modDB, vantagensRacais, desvantagensRacais, periciasRacais, descricaoRacial))
            }) { Text("Salvar Modelo") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    // DIALOGS DE SELEÇÃO E EDIÇÃO
    if (showSelecionarVantagem) { 
        SelecionarVantagemDialog(viewModel = viewModel, onDismiss = { showSelecionarVantagem = false }) 
    }
    if (showSelecionarDesvantagem) { 
        SelecionarDesvantagemDialog(viewModel = viewModel, onDismiss = { showSelecionarDesvantagem = false }) 
    }
    
    // RESTAURAÇÃO: SELEÇÃO DE PERÍCIA RACIAL
    if (showSelecionarPericia) {
        SelecionarPericiaDialog(
            viewModel = viewModel,
            onDismiss = { showSelecionarPericia = false },
            onConfirm = { pericia ->
                periciasRacais = periciasRacais + ItemPericiaRacial(
                    nome = pericia.nome,
                    baseAtributo = pericia.atributoBase.sigla,
                    diff = pericia.dificuldade.sigla,
                    custo = 1,
                    descricao = pericia.definicaoId
                )
                showSelecionarPericia = false
            }
        )
    }

    // RESTAURAÇÃO: EDIÇÃO DE PERÍCIA RACIAL
    if (editingPericiaIndex != null) {
        val pr = periciasRacais[editingPericiaIndex!!]
        var novoCusto by remember { mutableStateOf(pr.custo) }
        
        AlertDialog(
            onDismissRequest = { editingPericiaIndex = null },
            title = { Text("Editar Bônus Racial: ${pr.nome}") },
            text = {
                Column {
                    Text("Defina o bônus ou custo em pontos para esta perícia racial.")
                    AjustadorVerticalRacial("Bônus/Custo", novoCusto, pr.nome) { novoCusto = (novoCusto + it).coerceAtLeast(1) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    periciasRacais = periciasRacais.toMutableList().apply {
                        this[editingPericiaIndex!!] = pr.copy(custo = novoCusto)
                    }
                    editingPericiaIndex = null
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { editingPericiaIndex = null }) { Text("Cancelar") } }
        )
    }
    if (showSelecionarPericia) { SelecionarPericiaDialog(viewModel = viewModel, onDismiss = { showSelecionarPericia = false }) }

    // SINCRONIZAÇÃO
    LaunchedEffect(viewModel.personagem.vantagens.size) {
        if (showSelecionarVantagem && viewModel.personagem.vantagens.isNotEmpty()) {
            val n = viewModel.personagem.vantagens.last()
            vantagensRacais = vantagensRacais + n
            viewModel.removerVantagem(viewModel.personagem.vantagens.size - 1)
            showSelecionarVantagem = false
        }
    }
    LaunchedEffect(viewModel.personagem.desvantagens.size) {
        if (showSelecionarDesvantagem && viewModel.personagem.desvantagens.isNotEmpty()) {
            val n = viewModel.personagem.desvantagens.last()
            desvantagensRacais = desvantagensRacais + n
            viewModel.removerDesvantagem(viewModel.personagem.desvantagens.size - 1)
            showSelecionarDesvantagem = false
        }
    }
    LaunchedEffect(viewModel.personagem.pericias.size) {
        if (showSelecionarPericia && viewModel.personagem.pericias.isNotEmpty()) {
            val n = viewModel.personagem.pericias.last()
            val pr = PericiaRacial(n.nome, n.dificuldade.sigla, n.atributoBase.sigla, 0, n.pontosGastos)
            periciasRacais = periciasRacais + pr
            viewModel.removerPericia(viewModel.personagem.pericias.size - 1)
            showSelecionarPericia = false
        }
    }

    // EDIÇÃO
    editingVantagemIndex?.let { i ->
        EditarVantagemDialog(vantagem = vantagensRacais[i], descricaoCatalogo = "", onDismiss = { editingVantagemIndex = null }, onSave = { n ->
            vantagensRacais = vantagensRacais.toList().mapIndexed { idx, v -> if (idx == i) n else v }; editingVantagemIndex = null
        })
    }
    editingDesvantagemIndex?.let { i ->
        EditarDesvantagemDialog(desvantagem = desvantagensRacais[i], descricaoCatalogo = "", onDismiss = { editingDesvantagemIndex = null }, onSave = { n ->
            desvantagensRacais = desvantagensRacais.toList().mapIndexed { idx, d -> if (idx == i) n else d }; editingDesvantagemIndex = null
        })
    }
}

@Composable
fun AjustadorVerticalRacial(rotulo: String, valor: Int, nomeCompleto: String, onDelta: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(rotulo, style = MaterialTheme.typography.labelSmall)
        IconButton(
            onClick = { onDelta(1) },
            modifier = Modifier.size(32.dp).semantics { contentDescription = "Aumentar bônus de $nomeCompleto" }
        ) { Icon(Icons.Default.KeyboardArrowUp, null) }
        
        Text(
            "${if(valor>0) "+" else ""}$valor",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { contentDescription = "Bônus de $nomeCompleto atual: ${if(valor>0) "+" else ""}$valor" }
        )
        
        IconButton(
            onClick = { onDelta(-1) },
            modifier = Modifier.size(32.dp).semantics { contentDescription = "Diminuir bônus de $nomeCompleto" }
        ) { Icon(Icons.Default.KeyboardArrowDown, null) }
    }
}
