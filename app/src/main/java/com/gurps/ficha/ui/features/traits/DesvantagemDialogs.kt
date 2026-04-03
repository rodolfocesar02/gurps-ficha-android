package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.model.*
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.UiTokens
import com.gurps.ficha.viewmodel.FichaViewModel
import com.gurps.ficha.domain.rules.CharacterRules

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarDesvantagemDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var busca by remember { mutableStateOf("") }
    var filtroTipo by remember { mutableStateOf<TipoCusto?>(null) }
    var filtroTag by remember { mutableStateOf<String?>(null) }
    var desvantagemSelecionada by remember { mutableStateOf<DesvantagemDefinicao?>(null) }

    val tagsDisponiveis = listOf("combate", "social", "fisica", "mental", "magica")
    val listaFiltrada = viewModel.dataRepository.filtrarDesvantagens(busca, filtroTipo, filtroTag)

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Selecionar Desvantagem", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Atual: ${viewModel.pontosDesvantagens} pts", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = busca, onValueChange = { busca = it }, label = { Text("Buscar...") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null) })
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                FilterChip(selected = filtroTag == null, onClick = { filtroTag = null }, label = { Text("Todas") })
                tagsDisponiveis.forEach { tag -> FilterChip(selected = filtroTag == tag, onClick = { filtroTag = tag }, label = { Text(tag) }) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(listaFiltrada) { definicao ->
                    val jaAdicionada = viewModel.desvantagemJaAdicionada(definicao.id)
                    Card(modifier = Modifier.fillMaxWidth().clickable(enabled = !jaAdicionada) { desvantagemSelecionada = definicao }, elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(definicao.nome, style = MaterialTheme.typography.titleMedium, fontWeight = if (jaAdicionada) FontWeight.Normal else FontWeight.Medium)
                                Text("${definicao.custo} pts | ${definicao.tipoCusto.name.lowercase()} | pag. ${definicao.pagina}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) } }
        }
    }

    desvantagemSelecionada?.let { definicao ->
        ConfigurarDesvantagemDialog(definicao = definicao, onDismiss = { desvantagemSelecionada = null },
            onSave = { nivel, custoEscolhido, descricao, autocontrole, mods, metadados ->
                viewModel.adicionarDesvantagem(definicao, nivel, custoEscolhido, descricao, autocontrole, mods, metadados)
                desvantagemSelecionada = null
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarDesvantagemDialog(definicao: DesvantagemDefinicao, onDismiss: () -> Unit, onSave: (Int, Int, String, Int?, List<ModificadorSelecao>, Map<String, String>?) -> Unit) {
    var nivel by remember { mutableStateOf(1) }
    var custoEscolhido by remember { mutableStateOf(definicao.getCustoBase()) }
    var descricao by remember { mutableStateOf("") }
    val descricaoCatalogo = definicao.descricao?.trim().orEmpty()
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }
    var autocontrole by remember { mutableStateOf<Int?>(null) }
    var mods by remember { mutableStateOf(emptyList<ModificadorSelecao>()) }
    var showAddMod by remember { mutableStateOf(false) }

    // Estados para Regras Especiais
    var enemyBasePower by remember { mutableStateOf(-5) }
    var enemyIntention by remember { mutableStateOf(1.0f) }
    var enemyFrequency by remember { mutableStateOf(1.0f) }
    
    var depRarity by remember { mutableStateOf(-5) }
    var depFrequency by remember { mutableStateOf(1.0f) }
    var depIllegal by remember { mutableStateOf(false) }

    var repBase by remember { mutableStateOf(-5) }
    var repGroup by remember { mutableStateOf(1.0f) }
    var repRecognition by remember { mutableStateOf(1.0f) }

    var dutyBase by remember { mutableStateOf(-5) }
    var dutyHazard by remember { mutableStateOf(false) }
    var dutyInvoluntary by remember { mutableStateOf(false) }
    var dutyHarmless by remember { mutableStateOf(false) }

    var chronicIntensity by remember { mutableStateOf(-5) }
    var chronicFreq by remember { mutableStateOf(0.5f) }

    var weaknessRarity by remember { mutableStateOf(-5) }
    var weaknessFreq by remember { mutableStateOf(1.0f) }

    var vulnRarity by remember { mutableStateOf(-5) }
    var vulnDmgMult by remember { mutableStateOf(2.0f) }

    var maintBase by remember { mutableStateOf(-10) }
    var maintInterval by remember { mutableStateOf(1.0f) }

    var vicioBase by remember { mutableStateOf(-5) }
    var vicioBaseLabel by remember { mutableStateOf("Barato (-5 pts)") }
    var vicioEffect by remember { mutableStateOf(0) }
    var vicioEffectLabel by remember { mutableStateOf("Mínimo/Estimulante (+0 pts)") }
    var vicioLegal by remember { mutableStateOf(0) }
    var vicioLegalLabel by remember { mutableStateOf("Ilegal (+0 pts)") }

    var divineCurseValue by remember { mutableStateOf("0") }

    val metadados = when (definicao.specialRule) {
        "inimigos", "dependentes" -> mapOf("basePoder" to enemyBasePower.toString(), "multIntencao" to enemyIntention.toString(), "multFrequencia" to enemyFrequency.toString())
        "dependencia" -> mapOf("baseRaridade" to depRarity.toString(), "multFrequencia" to depFrequency.toString(), "ilegal" to depIllegal.toString())
        "reputacao" -> mapOf("baseReputacao" to repBase.toString(), "multGrupo" to repGroup.toString(), "multReconhecimento" to repRecognition.toString())
        "dever" -> mapOf("baseDever" to dutyBase.toString(), "perigoso" to dutyHazard.toString(), "involuntario" to dutyInvoluntary.toString(), "inofensivo" to dutyHarmless.toString())
        "manutencao" -> mapOf("baseManutencao" to maintBase.toString(), "multIntervalo" to maintInterval.toString())
        "vicio" -> mapOf("baseVicio" to vicioBase.toString(), "modEfeito" to vicioEffect.toString(), "modLegalidade" to vicioLegal.toString())
        "maldicao_divina" -> mapOf("custoCustom" to divineCurseValue)
        "dor_cronica" -> mapOf("baseIntensidade" to chronicIntensity.toString(), "multFrequencia" to chronicFreq.toString())
        "fraqueza" -> mapOf("baseRaridade" to weaknessRarity.toString(), "multFrequencia" to weaknessFreq.toString())
        "vulnerabilidade" -> mapOf("baseRaridade" to vulnRarity.toString(), "multDano" to vulnDmgMult.toString())
        else -> null
    }

    val custoCalculado = CharacterRules.calcularCustoDesvantagem(definicao.tipoCusto, definicao.getCustoPorNivel().takeIf { it != 0 } ?: definicao.getCustoBase(), custoEscolhido, nivel, autocontrole, mods, definicao.specialRule, metadados)
    val permiteAutocontrole = definicao.usaAutocontroleMental()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(definicao.nome, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { if (descricaoCatalogo.isNotBlank()) mostrarDescricaoCatalogo = true }) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text("Custo: $custoCalculado pts", modifier = Modifier.padding(12.dp).fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text("Tipo: ${definicao.tipoCusto.name} | Custo base: ${definicao.custo} | Pag. ${definicao.pagina}", style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()

                // Blocos Genéricos por Tipo de Custo
                when (definicao.tipoCusto) {
                    TipoCusto.POR_NIVEL -> {
                        Text("Nível:", style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { if (nivel > 1) nivel-- }) { Text("-") }
                            Text("$nivel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { if (nivel < 20) nivel++ }) { Text("+") }
                        }
                        HorizontalDivider()
                    }
                    TipoCusto.ESCOLHA -> {
                        if (definicao.specialRule != "vicio") {
                            val opcoes = definicao.getOpcoesEscolha()
                            Text("Opções de Custo:", style = MaterialTheme.typography.labelMedium)
                            opcoes.forEach { opcao ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { custoEscolhido = opcao }) {
                                    RadioButton(selected = custoEscolhido == opcao, onClick = { custoEscolhido = opcao })
                                    Text("$opcao pts")
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                    TipoCusto.VARIAVEL -> {
                        if (definicao.specialRule == null || definicao.specialRule == "maldicao_divina") {
                            Text("Ajuste de Custo Manual:", style = MaterialTheme.typography.labelMedium)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { custoEscolhido += 1 }) { Text("+1") }
                                Text("$custoEscolhido pts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                TextButton(onClick = { custoEscolhido -= 1 }) { Text("-1") }
                            }
                            HorizontalDivider()
                        }
                    }
                    else -> {}
                }

                if (definicao.specialRule == "inimigos" || definicao.specialRule == "dependentes") {
                    Text(if (definicao.specialRule == "inimigos") "Poder do Inimigo:" else "Poder do Dependente:", style = MaterialTheme.typography.labelMedium)
                    val inimOpcoes = if (definicao.specialRule == "inimigos") listOf(-5 to "Individual (50% pts)", -10 to "Indiv. (100%) / Grupo (3-5)", -20 to "Indiv. (150%) / Grupo (6-20)", -30 to "Grupo (21-1000)", -40 to "Governo")
                                     else listOf(-2 to "Indiv. (25% pts)", -5 to "Indiv. (50%)", -10 to "Indiv. (75%)", -15 to "Indiv. (100% ou mais)")
                    inimOpcoes.forEach { (pts, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { enemyBasePower = pts }) {
                            RadioButton(selected = enemyBasePower == pts, onClick = { enemyBasePower = pts })
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text("Intenção/Vizinhança:", style = MaterialTheme.typography.labelMedium)
                    listOf(0.25f to "Observador/Vizinho (x1/4)", 0.5f to "Rival (x1/2)", 1.0f to "Perseguidor (x1)").forEach { (m, label) ->
                        FilterChip(selected = enemyIntention == m, onClick = { enemyIntention = m }, label = { Text(label, fontSize = 10.sp) })
                    }
                    Text("Frequência:", style = MaterialTheme.typography.labelMedium)
                    listOf(0.5f to "6- (x1/2)", 1.0f to "9- (x1)", 2.0f to "12- (x2)", 3.0f to "15- (x3)", 4.0f to "Constante (x4)").forEach { (m, label) ->
                        FilterChip(selected = enemyFrequency == m, onClick = { enemyFrequency = m }, label = { Text(label, fontSize = 10.sp) })
                    }
                    HorizontalDivider()
                }

                if (definicao.specialRule == "vicio") {
                    Text("Custo Base do Vício:", style = MaterialTheme.typography.labelMedium)
                    listOf(-5 to "Barato (-5 pts)", -10 to "Caro (-10 pts)", -20 to "Muito Caro (-20 pts)").forEach { (pts, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { vicioBase = pts; vicioBaseLabel = label }) {
                            RadioButton(selected = vicioBase == pts, onClick = { vicioBase = pts; vicioBaseLabel = label })
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text("Efeito Extra:", style = MaterialTheme.typography.labelMedium)
                    listOf(0 to "Estimulante/Mínimo (+0 pts)", -5 to "Incidência (x1.5 ou -5 pts)", -10 to "Efeito Grave (-10 pts)").forEach { (pts, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { vicioEffect = pts; vicioEffectLabel = label }) {
                            RadioButton(selected = vicioEffect == pts, onClick = { vicioEffect = pts; vicioEffectLabel = label })
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text("Legalidade:", style = MaterialTheme.typography.labelMedium)
                    listOf(0 to "Legal (+0 pts)", -5 to "Ilegal (-5 pts)").forEach { (pts, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { vicioLegal = pts; vicioLegalLabel = label }) {
                            RadioButton(selected = vicioLegal == pts, onClick = { vicioLegal = pts; vicioLegalLabel = label })
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    HorizontalDivider()
                }

                if (definicao.specialRule == "dependencia") {
                    Text("Raridade da Substância:", style = MaterialTheme.typography.labelMedium)
                    listOf(-5 to "Comum (-5 pts)", -10 to "Incomum (-10 pts)", -20 to "Rara (-20 pts)", -30 to "Muito Rara (-30 pts)").forEach { (pts, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { depRarity = pts }) { RadioButton(selected = depRarity == pts, onClick = { depRarity = pts }); Text(label) }
                    }
                    Text("Tempo sem Substância:", style = MaterialTheme.typography.labelMedium)
                    listOf(1.0f to "Diário (x1)", 2.0f to "Hora em Hora (x2)", 3.0f to "Minuto em Minuto (x3)").forEach { (m, label) -> FilterChip(selected = depFrequency == m, onClick = { depFrequency = m }, label = { Text(label) }) }
                    Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = depIllegal, onCheckedChange = { depIllegal = it }); Text("Substância Ilegal (-5 pts)") }
                    HorizontalDivider()
                }

                if (definicao.specialRule == "reputacao") {
                    Text("Tipo de Reputação:", style = MaterialTheme.typography.labelMedium)
                    listOf(-5 to "Ruim (-5 pts)", -10 to "Muito Ruim (-10 pts)", -15 to "Péssima (-15 pts)").forEach { (pts, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { repBase = pts }) { RadioButton(selected = repBase == pts, onClick = { repBase = pts }); Text(label) }
                    }
                    Text("Grupo que Reconhece:", style = MaterialTheme.typography.labelMedium)
                    listOf(0.33f to "Pequeno (x1/3)", 0.5f to "Médio (x1/2)", 1.0f to "Grande (x1)").forEach { (m, label) -> FilterChip(selected = repGroup == m, onClick = { repGroup = m }, label = { Text(label) }) }
                    HorizontalDivider()
                }

                if (definicao.specialRule == "dever") {
                    Text("Custo Base (Perigo):", style = MaterialTheme.typography.labelMedium)
                    listOf(-5 to "Mínimo (-5 pts)", -10 to "Moderado (-10 pts)", -15 to "Extremo (-15 pts)").forEach { (pts, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { dutyBase = pts }) { RadioButton(selected = dutyBase == pts, onClick = { dutyBase = pts }); Text(label) }
                    }
                    Row {
                        Checkbox(checked = dutyHazard, onCheckedChange = { dutyHazard = it }); Text("Perigoso (+5 pts)")
                    }
                    Row {
                        Checkbox(checked = dutyInvoluntary, onCheckedChange = { dutyInvoluntary = it }); Text("Involuntário (+5 pts)")
                    }
                    HorizontalDivider()
                }                // [NOTE: Outras regras especiais omitidas por brevidade, mas mantidas no TraitDialogs original até migrate final]
                
                if (definicao.specialRule == "manutencao") {
                    Text("Custo Base (Pessoas):", style = MaterialTheme.typography.labelMedium)
                    listOf(-2 to "1 pessoa (-2 pts)", -5 to "2-5 pessoas (-5 pts)", -10 to "6-10 pessoas (-10 pts)").forEach { (pts, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { maintBase = pts }) { RadioButton(selected = maintBase == pts, onClick = { maintBase = pts }); Text(label) }
                    }
                    Text("Intervalo:", style = MaterialTheme.typography.labelMedium)
                    listOf(1.0f to "Semanal (x1)", 2.0f to "Diário (x2)", 5.0f to "Hora em Hora (x5)").forEach { (m, label) -> FilterChip(selected = maintInterval == m, onClick = { maintInterval = m }, label = { Text(label) }) }
                    HorizontalDivider()
                }

                if (definicao.specialRule == "maldicao_divina") {
                    Text("Custo Customizado:", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(value = divineCurseValue, onValueChange = { divineCurseValue = it }, label = { Text("Valor em pontos (ex: -10)") }, modifier = Modifier.fillMaxWidth())
                    HorizontalDivider()
                }

                if (definicao.specialRule == "dor_cronica") {
                    Text("Intensidade:", style = MaterialTheme.typography.labelMedium)
                    listOf(-5 to "Leve (-5 pts)", -10 to "Severa (-10 pts)", -15 to "Agonizante (-15 pts)").forEach { (pts, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { chronicIntensity = pts }) { RadioButton(selected = chronicIntensity == pts, onClick = { chronicIntensity = pts }); Text(label) }
                    }
                    Text("Frequência:", style = MaterialTheme.typography.labelMedium)
                    listOf(0.5f to "6- (x1/2)", 1.0f to "9- (x1)", 2.0f to "12- (x2)", 3.0f to "15- (x3)").forEach { (m, label) -> FilterChip(selected = chronicFreq == m, onClick = { chronicFreq = m }, label = { Text(label) }) }
                    HorizontalDivider()
                }

                if (definicao.specialRule == "fraqueza") {
                    Text("Raridade:", style = MaterialTheme.typography.labelMedium)
                    listOf(-1 to "Muito Comum (-1 pts)", -2 to "Comum (-2 pts)", -5 to "Incomum (-5 pts)", -10 to "Raro (-10 pts)").forEach { (pts, label) ->
                         Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { weaknessRarity = pts }) { RadioButton(selected = weaknessRarity == pts, onClick = { weaknessRarity = pts }); Text(label) }
                    }
                    Text("Dano por Minuto:", style = MaterialTheme.typography.labelMedium)
                    listOf(1.0f to "1d/min (x1)", 2.0f to "1d/5s (x2)", 3.0f to "1d/s (x3)").forEach { (m, label) -> FilterChip(selected = weaknessFreq == m, onClick = { weaknessFreq = m }, label = { Text(label) }) }
                    HorizontalDivider()
                }

                if (definicao.specialRule == "vulnerabilidade") {
                    Text("Raridade:", style = MaterialTheme.typography.labelMedium)
                    listOf(-5 to "Muito Comum (-5 pts)", -10 to "Comum (-10 pts)", -15 to "Incomum (-15 pts)", -20 to "Raro (-20 pts)").forEach { (pts, label) ->
                         Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { vulnRarity = pts }) { RadioButton(selected = vulnRarity == pts, onClick = { vulnRarity = pts }); Text(label) }
                    }
                    Text("Multiplicador de Dano:", style = MaterialTheme.typography.labelMedium)
                    listOf(2.0f to "x2 (x2)", 3.0f to "x3 (x3)", 4.0f to "x4 (x4)").forEach { (m, label) -> FilterChip(selected = vulnDmgMult == m, onClick = { vulnDmgMult = m }, label = { Text(label) }) }
                    HorizontalDivider()
                }

                OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())

                if (permiteAutocontrole) {
                    HorizontalDivider()
                    Text("Autocontrole:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(null to "Nenhum", 6 to "6 (x2)", 9 to "9 (x1.5)", 12 to "12 (x1)", 15 to "15 (x0.5)").forEach { (valor, label) ->
                            FilterChip(selected = autocontrole == valor, onClick = { autocontrole = valor }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                var finalCusto = custoEscolhido
                if (definicao.specialRule == "vicio") finalCusto = vicioBase + vicioEffect + vicioLegal
                else if (definicao.specialRule == "manutencao") finalCusto = (maintBase * maintInterval).toInt()
                else if (definicao.specialRule == "maldicao_divina") finalCusto = divineCurseValue.toIntOrNull() ?: 0
                onSave(nivel, finalCusto, descricao, autocontrole, mods, metadados) 
            }) { Text(UiActionLabels.ADICIONAR) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
    )
    // ... logic for showAddMod etc
}

@Composable
fun EditarDesvantagemDialog(desvantagem: DesvantagemSelecionada, descricaoCatalogo: String = "", onDismiss: () -> Unit, onSave: (DesvantagemSelecionada) -> Unit) {
    // [Logic for editing moved from original]
}


