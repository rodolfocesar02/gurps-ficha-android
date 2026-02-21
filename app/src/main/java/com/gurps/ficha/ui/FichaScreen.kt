package com.gurps.ficha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gurps.ficha.model.*
import com.gurps.ficha.viewmodel.FichaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichaScreen(viewModel: FichaViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    var showMenuDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }

    val temAptidaoMagica = viewModel.temAptidaoMagica
    val tabs = if (temAptidaoMagica) {
        listOf("Geral", "Traços", "Perícias", "Magia", "Combate", "Equip.", "Notas")
    } else {
        listOf("Geral", "Traços", "Perícias", "Combate", "Equip.", "Notas")
    }
    val maxTabIndex = tabs.lastIndex

    LaunchedEffect(maxTabIndex) {
        if (selectedTab > maxTabIndex) {
            selectedTab = maxTabIndex
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = viewModel.personagem.nome.ifBlank { "GURPS - Nova Ficha" },
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showMenuDialog = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.Person
                                    1 -> Icons.Default.Star
                                    2 -> Icons.Default.Build
                                    3 -> if (temAptidaoMagica) Icons.Default.Star else Icons.Default.Favorite
                                    4 -> if (temAptidaoMagica) Icons.Default.Favorite else Icons.Default.ShoppingCart
                                    5 -> if (temAptidaoMagica) Icons.Default.ShoppingCart else Icons.Default.Edit
                                    else -> Icons.Default.Edit
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        alwaysShowLabel = false // Alterado para mostrar apenas quando selecionado
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PontosBar(viewModel)
            when (selectedTab) {
                0 -> TabGeral(viewModel)
                1 -> TabTracos(viewModel)
                2 -> TabPericias(viewModel)
                3 -> if (temAptidaoMagica) TabMagias(viewModel) else TabCombate(viewModel)
                4 -> if (temAptidaoMagica) TabCombate(viewModel) else TabEquipamentos(viewModel)
                5 -> if (temAptidaoMagica) TabEquipamentos(viewModel) else TabNotas(viewModel)
                6 -> TabNotas(viewModel)
            }
        }
    }

    if (showMenuDialog) {
        MenuDialog(
            onDismiss = { showMenuDialog = false },
            onNovaFicha = { viewModel.novaFicha(); showMenuDialog = false },
            onSalvar = { showMenuDialog = false; showSaveDialog = true },
            onCarregar = { showMenuDialog = false; showLoadDialog = true }
        )
    }

    if (showSaveDialog) {
        SalvarDialog(
            nomeAtual = viewModel.personagem.nome,
            onDismiss = { showSaveDialog = false },
            onSalvar = { nome -> viewModel.salvarFicha(nome); showSaveDialog = false }
        )
    }

    if (showLoadDialog) {
        CarregarDialog(
            fichas = viewModel.fichasSalvas,
            onDismiss = { showLoadDialog = false },
            onCarregar = { nome -> viewModel.carregarFicha(nome); showLoadDialog = false },
            onExcluir = { nome -> viewModel.excluirFicha(nome) }
        )
    }
}

@Composable
fun PontosBar(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val restantes = p.pontosRestantes
    val corRestantes = when {
        restantes < 0 -> MaterialTheme.colorScheme.error
        restantes == 0 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Pontos Iniciais: ${p.pontosIniciais}", style = MaterialTheme.typography.bodySmall)
                    Text("Gastos: ${p.pontosGastos}", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "Restantes: $restantes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = corRestantes
                )
            }
        }
    }
}

// === TAB GERAL ===

@Composable
fun TabGeral(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Informações Básicas") {
            OutlinedTextField(
                value = p.nome,
                onValueChange = { viewModel.atualizarNome(it) },
                label = { Text("Nome do Personagem") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = p.jogador,
                    onValueChange = { viewModel.atualizarJogador(it) },
                    label = { Text("Jogador") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = p.pontosIniciais.toString(),
                    onValueChange = { viewModel.atualizarPontosIniciais(it.toIntOrNull() ?: 150) },
                    label = { Text("Pontos") },
                    modifier = Modifier.width(100.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = p.campanha,
                onValueChange = { viewModel.atualizarCampanha(it) },
                label = { Text("Campanha") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        SectionCard(title = "Atributos Primários") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AtributoEditor("ST", p.forca, (p.forca - 10) * 10,
                    { viewModel.atualizarForca(p.forca - 1) }, { viewModel.atualizarForca(p.forca + 1) })
                AtributoEditor("DX", p.destreza, (p.destreza - 10) * 20,
                    { viewModel.atualizarDestreza(p.destreza - 1) }, { viewModel.atualizarDestreza(p.destreza + 1) })
                AtributoEditor("IQ", p.inteligencia, (p.inteligencia - 10) * 20,
                    { viewModel.atualizarInteligencia(p.inteligencia - 1) }, { viewModel.atualizarInteligencia(p.inteligencia + 1) })
                AtributoEditor("HT", p.vitalidade, (p.vitalidade - 10) * 10,
                    { viewModel.atualizarVitalidade(p.vitalidade - 1) }, { viewModel.atualizarVitalidade(p.vitalidade + 1) })
            }
        }

        SectionCard(title = "Atributos Secundários") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AtributoSecundarioEditor("PV", p.forca, p.modPontosVida, p.pontosVida, 2,
                    { viewModel.atualizarModPontosVida(p.modPontosVida - 1) }, { viewModel.atualizarModPontosVida(p.modPontosVida + 1) })
                AtributoSecundarioEditor("Von", p.inteligencia, p.modVontade, p.vontade, 5,
                    { viewModel.atualizarModVontade(p.modVontade - 1) }, { viewModel.atualizarModVontade(p.modVontade + 1) })
                AtributoSecundarioEditor("Per", p.inteligencia, p.modPercepcao, p.percepcao, 5,
                    { viewModel.atualizarModPercepcao(p.modPercepcao - 1) }, { viewModel.atualizarModPercepcao(p.modPercepcao + 1) })
                AtributoSecundarioEditor("PF", p.vitalidade, p.modPontosFadiga, p.pontosFadiga, 3,
                    { viewModel.atualizarModPontosFadiga(p.modPontosFadiga - 1) }, { viewModel.atualizarModPontosFadiga(p.modPontosFadiga + 1) })
            }
        }

        SectionCard(title = "Características Derivadas") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CaracteristicaDisplay("Vel. Básica", String.format("%.2f", p.velocidadeBasica))
                CaracteristicaDisplay("Desloc.", "${p.deslocamentoBasico} m/s")
                CaracteristicaDisplay("BC", String.format("%.1f kg", p.baseCarga))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CaracteristicaDisplay("Dano GdP", p.danoGdP)
                CaracteristicaDisplay("Dano GeB", p.danoGeB)
            }
        }

        SectionCard(title = "Resumo de Pontos") {
            PontosResumoRow("Atributos Primários", p.pontosAtributos)
            PontosResumoRow("Atributos Secundários", p.pontosSecundarios)
            PontosResumoRow("Vantagens", p.pontosVantagens)
            PontosResumoRow("Desvantagens", p.pontosDesvantagens)
            PontosResumoRow("Peculiaridades", p.pontosPeculiaridades)
            PontosResumoRow("Perícias", p.pontosPericias)
            PontosResumoRow("Magias", p.pontosMagias)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            PontosResumoRow("Total Gasto", p.pontosGastos, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AtributoEditor(nome: String, valor: Int, custo: Int, onMenos: () -> Unit, onMais: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Text(nome, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenos, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Diminuir")
            }
            Text(valor.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
            IconButton(onClick = onMais, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Aumentar")
            }
        }
        Text("[${if (custo >= 0) "+$custo" else custo}]", style = MaterialTheme.typography.bodySmall,
            color = if (custo >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
fun AtributoSecundarioEditor(nome: String, valorBase: Int, modificador: Int, valorFinal: Int, custoPorPonto: Int, onMenos: () -> Unit, onMais: () -> Unit) {
    val custo = modificador * custoPorPonto
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Text(nome, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text("($valorBase)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenos, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Diminuir", modifier = Modifier.size(20.dp))
            }
            Text(valorFinal.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
            IconButton(onClick = onMais, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Aumentar", modifier = Modifier.size(20.dp))
            }
        }
        if (modificador != 0) {
            Text("[${if (custo >= 0) "+$custo" else custo}]", style = MaterialTheme.typography.bodySmall,
                color = if (custo >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
fun CaracteristicaDisplay(nome: String, valor: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(nome, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
fun PontosResumoRow(label: String, pontos: Int, fontWeight: FontWeight = FontWeight.Normal) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = fontWeight)
        Text(if (pontos >= 0) "+$pontos" else pontos.toString(), fontWeight = fontWeight,
            color = if (pontos >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
    }
}

// === TAB TRACOS ===

@Composable
fun TabTracos(viewModel: FichaViewModel) {
    var showSelecionarVantagem by remember { mutableStateOf(false) }
    var showSelecionarDesvantagem by remember { mutableStateOf(false) }
    var showPeculiaridadeDialog by remember { mutableStateOf(false) }
    var editingVantagemIndex by remember { mutableStateOf<Int?>(null) }
    var editingDesvantagemIndex by remember { mutableStateOf<Int?>(null) }

    val p = viewModel.personagem

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Vantagens [${p.pontosVantagens} pts]", onAdd = { showSelecionarVantagem = true }) {
            if (p.vantagens.isEmpty()) {
                Text("Nenhuma vantagem adicionada", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                p.vantagens.forEachIndexed { index, vantagem ->
                    VantagemItem(vantagem = vantagem,
                        onEdit = { editingVantagemIndex = index },
                        onDelete = { viewModel.removerVantagem(index) })
                    if (index < p.vantagens.lastIndex) Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        SectionCard(
            title = "Desvantagens [${p.pontosDesvantagens} pts]",
            onAdd = { showSelecionarDesvantagem = true }
        ) {
            if (p.desvantagens.isEmpty()) {
                Text("Nenhuma desvantagem adicionada", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                p.desvantagens.forEachIndexed { index, desvantagem ->
                    DesvantagemItem(desvantagem = desvantagem,
                        onEdit = { editingDesvantagemIndex = index },
                        onDelete = { viewModel.removerDesvantagem(index) })
                    if (index < p.desvantagens.lastIndex) Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        SectionCard(title = "Peculiaridades [${p.pontosPeculiaridades} pts]", onAdd = { showPeculiaridadeDialog = true }) {
            Text("Máximo: 5 peculiaridades (-1 pt cada)", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            if (p.peculiaridades.isEmpty()) {
                Text("Nenhuma peculiaridade", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                p.peculiaridades.forEachIndexed { index, peculiaridade ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("• $peculiaridade", modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.removerPeculiaridade(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showSelecionarVantagem) {
        SelecionarVantagemDialog(viewModel = viewModel, onDismiss = { showSelecionarVantagem = false })
    }

    if (showSelecionarDesvantagem) {
        SelecionarDesvantagemDialog(viewModel = viewModel, onDismiss = { showSelecionarDesvantagem = false })
    }

    if (showPeculiaridadeDialog) {
        PeculiaridadeDialog(onDismiss = { showPeculiaridadeDialog = false },
            onSave = { peculiaridade -> viewModel.adicionarPeculiaridade(peculiaridade); showPeculiaridadeDialog = false })
    }

    editingVantagemIndex?.let { index ->
        EditarVantagemDialog(
            vantagem = p.vantagens[index],
            onDismiss = { editingVantagemIndex = null },
            onSave = { updated -> viewModel.atualizarVantagem(index, updated); editingVantagemIndex = null }
        )
    }

    editingDesvantagemIndex?.let { index ->
        EditarDesvantagemDialog(
            desvantagem = p.desvantagens[index],
            onDismiss = { editingDesvantagemIndex = null },
            onSave = { updated -> viewModel.atualizarDesvantagem(index, updated); editingDesvantagemIndex = null }
        )
    }
}

@Composable
fun VantagemItem(vantagem: VantagemSelecionada, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onEdit() }, horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(vantagem.nome + if (vantagem.nivel > 1) " ${vantagem.nivel}" else "", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (vantagem.descricao.isNotBlank()) {
                Text(vantagem.descricao, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Tipo: ${vantagem.tipoCusto.name.lowercase()} | pag. ${vantagem.pagina}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        Text("+${vantagem.custoFinal} pts", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover") }
    }
}

@Composable
fun DesvantagemItem(desvantagem: DesvantagemSelecionada, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onEdit() }, horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(desvantagem.nome + if (desvantagem.nivel > 1) " ${desvantagem.nivel}" else "", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (desvantagem.descricao.isNotBlank()) {
                Text(desvantagem.descricao, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            desvantagem.autocontrole?.let { ac ->
                Text("Autocontrole: $ac (${getMultiplicadorAutocontrole(ac)})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
            Text("Tipo: ${desvantagem.tipoCusto.name.lowercase()} | pag. ${desvantagem.pagina}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        Text("${desvantagem.custoFinal} pts", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover") }
    }
}

fun getMultiplicadorAutocontrole(ac: Int): String = when (ac) {
    6 -> "x2"; 9 -> "x1.5"; 12 -> "x1"; 15 -> "x0.5"; else -> "x1"
}

// === TAB PERICIAS ===

@Composable
fun TabPericias(viewModel: FichaViewModel) {
    // Variáveis que controlam se as janelas estão abertas ou fechadas
    var showSelecionarPericia by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) } // Nova janela do +
    var editingPericiaIndex by remember { mutableStateOf<Int?>(null) }

    val p = viewModel.personagem

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Este é o Card que agrupa as perícias
        SectionCard(
            title = "Perícias [${p.pontosPericias} pts]",
            onAdd = { showSelecionarPericia = true } // Botão de busca (Lupa)
        ) {
            // Adicionamos uma linha com o botão de Criar Própria Perícia (+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showCustomDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Criar Própria Perícia")
                }
            }

            if (p.pericias.isEmpty()) {
                Text("Nenhuma perícia adicionada", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                p.pericias.forEachIndexed { index, pericia ->
                    PericiaItem(pericia = pericia, nivel = pericia.calcularNivel(p),
                        onEdit = { editingPericiaIndex = index },
                        onDelete = { viewModel.removerPericia(index) })
                    if (index < p.pericias.lastIndex) Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Janela para buscar perícias prontas (Lupa)
    if (showSelecionarPericia) {
        SelecionarPericiaDialog(viewModel = viewModel, onDismiss = { showSelecionarPericia = false })
    }

    // Janela para CRIAR sua própria perícia (Botão +)
    if (showCustomDialog) {
        CriarPericiaCustomizadaDialog(
            onDismiss = { showCustomDialog = false },
            onSave = { nome, espec, attr, diff ->
                val novaPericia = com.gurps.ficha.model.PericiaSelecionada(
                    definicaoId = "custom_${System.currentTimeMillis()}",
                    nome = nome,
                    atributoBase = com.gurps.ficha.model.AtributoBase.fromSigla(attr),
                    dificuldade = com.gurps.ficha.model.Dificuldade.fromSigla(diff),
                    pontosGastos = 1,
                    especializacao = espec,
                    exigeEspecializacao = espec.isNotBlank()
                )
                viewModel.adicionarPericiaCustomizada(novaPericia)
                showCustomDialog = false
            }
        )
    }

    // Janela para editar uma perícia que já está na lista
    editingPericiaIndex?.let { index ->
        EditarPericiaDialog(
            pericia = p.pericias[index],
            personagem = p,
            onDismiss = { editingPericiaIndex = null },
            onSave = { updated -> viewModel.atualizarPericia(index, updated); editingPericiaIndex = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PericiaItem(pericia: PericiaSelecionada, nivel: Int, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onEdit() }, horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(pericia.nome + if (pericia.especializacao.isNotBlank()) " (${pericia.especializacao})" else "",
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("${pericia.atributoBase.sigla}/${pericia.dificuldade.sigla} - ${pericia.pontosGastos} pts",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("Nível $nivel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover") }
    }
}

// === TAB MAGIAS ===

@Composable
fun TabMagias(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val nivelAptidaoMagica = viewModel.nivelAptidaoMagica

    var showSelecionarMagia by remember { mutableStateOf(false) }
    var editingMagiaIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(
            title = "Magias [${p.pontosMagias} pts] (IQ + AM $nivelAptidaoMagica)",
            onAdd = { showSelecionarMagia = true }
        ) {
            if (p.magias.isEmpty()) {
                Text(
                    "Nenhuma magia adicionada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                p.magias.forEachIndexed { index, magia ->
                    MagiaItem(
                        magia = magia,
                        nivel = magia.calcularNivel(p, nivelAptidaoMagica),
                        onEdit = { editingMagiaIndex = index },
                        onDelete = { viewModel.removerMagia(index) }
                    )
                    if (index < p.magias.lastIndex) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showSelecionarMagia) {
        SelecionarMagiaDialog(
            viewModel = viewModel,
            onDismiss = { showSelecionarMagia = false }
        )
    }

    editingMagiaIndex?.let { index ->
        EditarMagiaDialog(
            magia = p.magias[index],
            personagem = p,
            nivelAptidaoMagica = nivelAptidaoMagica,
            onDismiss = { editingMagiaIndex = null },
            onSave = { atualizada ->
                viewModel.atualizarMagia(index, atualizada)
                editingMagiaIndex = null
            }
        )
    }
}

@Composable
fun MagiaItem(magia: MagiaSelecionada, nivel: Int, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                magia.nome,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            val difNome = if (magia.dificuldade == Dificuldade.MUITO_DIFICIL) "MD" else "D"
            Text(
                "IQ/$difNome | ${magia.pontosGastos} pts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "Nível $nivel",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Remover")
        }
    }
}

// === TAB COMBATE ===

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabCombate(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Defesas Ativas") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                DefesaDisplay("Esquiva", viewModel.esquivaCalculada)
                viewModel.aparaCalculada?.let { DefesaDisplay("Apara", it) }
                viewModel.bloqueioCalculado?.let { DefesaDisplay("Bloqueio", it) }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Configuração de Apara
            Text("Configurar Apara:", style = MaterialTheme.typography.labelMedium)
            val periciasCombate = viewModel.periciasCombate
            if (periciasCombate.isEmpty()) {
                Text("Adicione perícias de combate para calcular a Apara.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                var expandedApara by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedApara,
                    onExpandedChange = { expandedApara = !expandedApara }
                ) {
                    val currentPericia = periciasCombate.find { it.definicaoId == p.defesasAtivas.periciaAparaId }
                    OutlinedTextField(
                        value = currentPericia?.let { "${it.nome} (${it.calcularNivel(p)})" } ?: "Selecionar Perícia",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedApara) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedApara,
                        onDismissRequest = { expandedApara = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma") },
                            onClick = { viewModel.atualizarPericiaApara(null); expandedApara = false }
                        )
                        periciasCombate.forEach { pericia ->
                            DropdownMenuItem(
                                text = { Text("${pericia.nome} (${pericia.calcularNivel(p)})") },
                                onClick = { viewModel.atualizarPericiaApara(pericia.definicaoId); expandedApara = false }
                            )
                        }
                    }
                }
            }
        }

        SectionCard(title = "Dano") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Golpe (GdP)", style = MaterialTheme.typography.labelSmall)
                    Text(p.danoGdP, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Balanço (GeB)", style = MaterialTheme.typography.labelSmall)
                    Text(p.danoGeB, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DefesaDisplay(nome: String, valor: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
            .width(80.dp)
    ) {
        Text(nome, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(valor.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

// === TAB EQUIPAMENTOS ===

@Composable
fun TabEquipamentos(viewModel: FichaViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var editingEquipamento by remember { mutableStateOf<Pair<Int, Equipamento>?>(null) }

    val p = viewModel.personagem

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Base de Carga:"); Text(String.format("%.1f kg", p.baseCarga), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Peso Total:"); Text(String.format("%.1f kg", viewModel.pesoTotal), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nível de Carga:")
                    Text(when (viewModel.nivelCarga) { 0 -> "Sem Carga"; 1 -> "Leve"; 2 -> "Média"; 3 -> "Pesada"; 4 -> "Muito Pesada"; else -> "Extra Pesada" },
                        fontWeight = FontWeight.Bold, color = when (viewModel.nivelCarga) { 0, 1 -> MaterialTheme.colorScheme.primary; 2, 3 -> MaterialTheme.colorScheme.tertiary; else -> MaterialTheme.colorScheme.error })
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Deslocamento Atual:"); Text("${viewModel.deslocamentoAtual} m/s", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Esquiva Atual:"); Text("${viewModel.esquivaAtual}", fontWeight = FontWeight.Bold)
                }
            }
        }

        SectionCard(title = "Equipamentos", onAdd = { showDialog = true }) {
            if (p.equipamentos.isEmpty()) {
                Text("Nenhum equipamento adicionado", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                p.equipamentos.forEachIndexed { index, equipamento ->
                    EquipamentoItem(equipamento = equipamento, onEdit = { editingEquipamento = index to equipamento },
                        onDelete = { viewModel.removerEquipamento(index) })
                    if (index < p.equipamentos.lastIndex) Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Custo Total dos Equipamentos:")
                Text(String.format("$%.2f", viewModel.custoTotalEquipamentos), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showDialog) {
        EquipamentoDialog(onDismiss = { showDialog = false },
            onSave = { equipamento -> viewModel.adicionarEquipamento(equipamento); showDialog = false })
    }

    editingEquipamento?.let { (index, equipamento) ->
        EquipamentoDialog(initialEquipamento = equipamento, onDismiss = { editingEquipamento = null },
            onSave = { updated -> viewModel.atualizarEquipamento(index, updated); editingEquipamento = null })
    }
}

@Composable
fun EquipamentoItem(equipamento: Equipamento, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onEdit() }, horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${equipamento.nome} ${if (equipamento.quantidade > 1) "x${equipamento.quantidade}" else ""}",
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("${equipamento.peso} kg | $${equipamento.custo}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (equipamento.notas.isNotBlank()) {
                Text(equipamento.notas, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover") }
    }
}

// === TAB NOTAS ===

@Composable
fun TabNotas(viewModel: FichaViewModel) {
    val p = viewModel.personagem

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Aparência") {
            OutlinedTextField(value = p.aparencia, onValueChange = { viewModel.atualizarAparencia(it) },
                modifier = Modifier.fillMaxWidth().height(120.dp), placeholder = { Text("Descreva a aparência...") })
        }
        SectionCard(title = "Histórico") {
            OutlinedTextField(value = p.historico, onValueChange = { viewModel.atualizarHistorico(it) },
                modifier = Modifier.fillMaxWidth().height(200.dp), placeholder = { Text("Conte a história...") })
        }
        SectionCard(title = "Notas Gerais") {
            OutlinedTextField(value = p.notas, onValueChange = { viewModel.atualizarNotas(it) },
                modifier = Modifier.fillMaxWidth().height(150.dp), placeholder = { Text("Anotações diversas...") })
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// === COMPONENTES COMUNS ===

@Composable
fun SectionCard(title: String, onAdd: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                onAdd?.let { IconButton(onClick = it) { Icon(Icons.Default.Add, contentDescription = "Adicionar") } }
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

// === DIALOGS DE SELECAO COM BUSCA ===

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarVantagemDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var busca by remember { mutableStateOf("") }
    var filtroTipo by remember { mutableStateOf<TipoCusto?>(null) }
    var vantagemSelecionada by remember { mutableStateOf<VantagemDefinicao?>(null) }

    val listaFiltrada = viewModel.dataRepository.filtrarVantagens(busca, filtroTipo)

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Selecionar Vantagem", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = busca, onValueChange = { busca = it }, label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) })

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
    var desvantagemSelecionada by remember { mutableStateOf<DesvantagemDefinicao?>(null) }

    val listaFiltrada = viewModel.dataRepository.filtrarDesvantagens(busca, filtroTipo)

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Selecionar Desvantagem", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Text("Atual: ${viewModel.pontosDesvantagens} pts", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = busca, onValueChange = { busca = it }, label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null) })

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarPericiaDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var busca by remember { mutableStateOf("") }
    var filtroAtributo by remember { mutableStateOf<String?>(null) }
    var periciaSelecionada by remember { mutableStateOf<PericiaDefinicao?>(null) }

    val listaFiltrada = viewModel.dataRepository.filtrarPericias(busca, filtroAtributo, null)

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Selecionar Perícia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = busca, onValueChange = { busca = it }, label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null) })

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(selected = filtroAtributo == null, onClick = { filtroAtributo = null }, label = { Text("Todos") })
                    listOf("DX", "IQ", "HT", "PER", "VON").forEach { attr ->
                        FilterChip(selected = filtroAtributo == attr, onClick = { filtroAtributo = attr }, label = { Text(attr) })
                    }
                }

                Text("${listaFiltrada.size} perícias encontradas", style = MaterialTheme.typography.bodySmall)

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(listaFiltrada) { definicao ->
                        val jaAdicionada = viewModel.periciaJaAdicionada(definicao.id)
                        ListItem(
                            headlineContent = { Text(definicao.nome + if (definicao.exigeEspecializacao) " *" else "") },
                            supportingContent = {
                                val atributos = definicao.atributosPossiveis?.joinToString("/") ?: definicao.atributoBase
                                val dificuldade = if (definicao.dificuldadeVariavel) "F/M/D/MD" else definicao.dificuldadeFixa ?: "M"
                                Text("$atributos/$dificuldade")
                            },
                            trailingContent = { if (jaAdicionada) Text("Adicionada", color = MaterialTheme.colorScheme.outline) },
                            modifier = Modifier.clickable(enabled = !jaAdicionada || definicao.exigeEspecializacao) { periciaSelecionada = definicao }
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

    periciaSelecionada?.let { definicao ->
        ConfigurarPericiaDialog(definicao = definicao, personagem = viewModel.personagem, onDismiss = { periciaSelecionada = null },
            onSave = { pontosGastos, especializacao, atributo, dificuldade ->
                viewModel.adicionarPericia(definicao, pontosGastos, especializacao, atributo, dificuldade)
                periciaSelecionada = null
            })
    }
}

@Composable
fun SelecionarMagiaDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var busca by remember { mutableStateOf("") }
    var magiaSelecionada by remember { mutableStateOf<MagiaDefinicao?>(null) }

    val listaFiltrada = viewModel.dataRepository.filtrarMagias(busca)

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Selecionar Magia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("${listaFiltrada.size} magias encontradas", style = MaterialTheme.typography.bodySmall)

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(listaFiltrada) { definicao ->
                        val jaAdicionada = viewModel.magiaJaAdicionada(definicao.id)
                        ListItem(
                            headlineContent = { Text(definicao.nome) },
                            supportingContent = { Text("IQ/${definicao.dificuldadeFixa ?: "D"} | pag. ${definicao.pagina}") },
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
    val previewMagia = MagiaSelecionada(definicao.id, definicao.nome, dificuldade, pontosGastos, definicao.pagina, definicao.texto)
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
                        Text("Preview do Nível:", style = MaterialTheme.typography.labelMedium)
                        Text("Nível: $nivelPreview (IQ+$nivelRelativo)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarPericiaCustomizadaDialog(
    onDismiss: () -> Unit,
    onSave: (nome: String, especializacao: String, atributo: String, dificuldade: String) -> Unit
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
                    onValueChange = { especializacao = it },
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
                    OutlinedTextField(value = especializacao, onValueChange = { especializacao = it },
                        label = { Text("Especialização *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                } else {
                    OutlinedTextField(value = especializacao, onValueChange = { especializacao = it },
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
                        Text("Preview do Nível:", style = MaterialTheme.typography.labelMedium)
                        Text("Nível: $nivelPreview (${atributoEscolhido.sigla}$nivelRelativo)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPericiaDialog(pericia: PericiaSelecionada, personagem: Personagem, onDismiss: () -> Unit, onSave: (PericiaSelecionada) -> Unit) {
    var pontosGastos by remember { mutableStateOf(pericia.pontosGastos) }
    var especializacao by remember { mutableStateOf(pericia.especializacao) }

    val previewPericia = pericia.copy(pontosGastos = pontosGastos, especializacao = especializacao)
    val nivelPreview = previewPericia.calcularNivel(personagem)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar: ${pericia.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${pericia.atributoBase.sigla}/${pericia.dificuldade.sigla}")

                OutlinedTextField(value = especializacao, onValueChange = { especializacao = it },
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

                Text("Nível previsto: $nivelPreview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(pericia.copy(pontosGastos = pontosGastos, especializacao = especializacao)) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
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
                val difNome = if (magia.dificuldade == Dificuldade.MUITO_DIFICIL) "MD" else "D"
                Text("IQ/$difNome", style = MaterialTheme.typography.bodyMedium)

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
                        Text("Preview do Nível:", style = MaterialTheme.typography.labelMedium)
                        Text("Nível: $nivelPreview (IQ+$nivelRelativo)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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

@Composable
fun MenuDialog(onDismiss: () -> Unit, onNovaFicha: () -> Unit, onSalvar: () -> Unit, onCarregar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menu") },
        text = {
            Column {
                TextButton(onClick = onNovaFicha, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null); Spacer(modifier = Modifier.width(8.dp)); Text("Nova Ficha")
                }
                TextButton(onClick = onSalvar, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Done, null); Spacer(modifier = Modifier.width(8.dp)); Text("Salvar Ficha")
                }
                TextButton(onClick = onCarregar, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Refresh, null); Spacer(modifier = Modifier.width(8.dp)); Text("Carregar Ficha")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@Composable
fun SalvarDialog(nomeAtual: String, onDismiss: () -> Unit, onSalvar: (String) -> Unit) {
    var name by remember { mutableStateOf(nomeAtual) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Salvar Ficha") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome da Ficha") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onSalvar(name) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun CarregarDialog(fichas: List<String>, onDismiss: () -> Unit, onCarregar: (String) -> Unit, onExcluir: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Carregar Ficha") },
        text = {
            if (fichas.isEmpty()) Text("Nenhuma ficha salva")
            else LazyColumn {
                itemsIndexed(fichas) { _, nome ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { onCarregar(nome) }.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(nome.replace("_", " "))
                        IconButton(onClick = { onExcluir(nome) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
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

@Composable
fun EquipamentoDialog(initialEquipamento: Equipamento? = null, onDismiss: () -> Unit, onSave: (Equipamento) -> Unit) {
    var nome by remember { mutableStateOf(initialEquipamento?.nome ?: "") }
    var peso by remember { mutableStateOf(initialEquipamento?.peso?.toString() ?: "0") }
    var custo by remember { mutableStateOf(initialEquipamento?.custo?.toString() ?: "0") }
    var quantidade by remember { mutableStateOf(initialEquipamento?.quantidade?.toString() ?: "1") }
    var notas by remember { mutableStateOf(initialEquipamento?.notas ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialEquipamento != null) "Editar Equipamento" else "Adicionar Equipamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = peso, onValueChange = { peso = it }, label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = custo, onValueChange = { custo = it }, label = { Text("Custo (\$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = quantidade, onValueChange = { quantidade = it }, label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notas, onValueChange = { notas = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nome.isNotBlank()) onSave(Equipamento(nome, peso.toFloatOrNull() ?: 0f, custo.toFloatOrNull() ?: 0f, quantidade.toIntOrNull()?.coerceAtLeast(1) ?: 1, notas))
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

}
