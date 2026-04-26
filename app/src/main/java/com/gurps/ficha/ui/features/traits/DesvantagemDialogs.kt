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
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
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
fun SelecionarDesvantagemDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: ((DesvantagemSelecionada) -> Unit)? = null
) {
    var busca by remember { mutableStateOf("") }
    var filtroTipo by remember { mutableStateOf<TipoCusto?>(null) }
    var filtroTag by remember { mutableStateOf<String?>(null) }
    var desvantagemSelecionada by remember { mutableStateOf<DesvantagemDefinicao?>(null) }
    val contextForToast = LocalContext.current

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
            onSave = { nível, custoEsc, desc, ac, ms, meta ->
                val novaDesvantagem = DesvantagemSelecionada(
                    definicaoId = definicao.id,
                    nome = definicao.nome,
                    custoBase = if (definicao.tipoCusto == com.gurps.ficha.model.TipoCusto.POR_NIVEL) definicao.getCustoPorNivel() else definicao.getCustoBase(),
                    nivel = nível,
                    custoEscolhido = custoEsc,
                    descricao = desc,
                    tipoCusto = definicao.tipoCusto,
                    pagina = definicao.pagina,
                    autocontrole = ac,
                    specialRule = definicao.specialRule,
                    modificadores = ms,
                    metadados = meta
                )
                val context = contextForToast
                if (onSelect != null) {
                    onSelect(novaDesvantagem)
                } else {
                    val erro = viewModel.adicionarDesvantagem(definicao, nível, custoEsc, desc, ac, ms, meta)
                    if (erro != null) {
                        Toast.makeText(context, erro, Toast.LENGTH_SHORT).show()
                    }
                }
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
                        }
                    }
                    else -> {}
                }

                // Regras Especiais (UI Compartilhada)
                if (definicao.specialRule == "inimigos" || definicao.specialRule == "dependentes") {
                    InimigosConfig(
                        basePower = enemyBasePower,
                        intention = enemyIntention,
                        frequency = enemyFrequency,
                        isEnemy = definicao.specialRule == "inimigos",
                        onChanged = { p, i, f -> enemyBasePower = p; enemyIntention = i; enemyFrequency = f }
                    )
                    HorizontalDivider()
                }

                if (definicao.specialRule == "vicio") {
                    VicioConfig(
                        base = vicioBase,
                        effect = vicioEffect,
                        legality = vicioLegal,
                        onChanged = { b, e, l -> vicioBase = b; vicioEffect = e; vicioLegal = l }
                    )
                    HorizontalDivider()
                }

                if (definicao.specialRule == "dependencia") {
                    DependenciaConfig(
                        rarity = depRarity,
                        frequency = depFrequency,
                        isIllegal = depIllegal,
                        onChanged = { r, f, i -> depRarity = r; depFrequency = f; depIllegal = i }
                    )
                    HorizontalDivider()
                }

                if (definicao.specialRule == "reputacao") {
                    ReputacaoConfig(
                        base = repBase,
                        group = repGroup,
                        onChanged = { b, g -> repBase = b; repGroup = g }
                    )
                    HorizontalDivider()
                }

                if (definicao.specialRule == "dever") {
                    DeverConfig(
                        base = dutyBase,
                        isHazardous = dutyHazard,
                        isInvoluntary = dutyInvoluntary,
                        onChanged = { b, h, i -> dutyBase = b; dutyHazard = h; dutyInvoluntary = i }
                    )
                    HorizontalDivider()
                }

                if (definicao.specialRule == "manutencao") {
                    ManutencaoConfig(
                        base = maintBase,
                        interval = maintInterval,
                        onChanged = { b, i -> maintBase = b; maintInterval = i }
                    )
                    HorizontalDivider()
                }

                if (definicao.specialRule == "dor_cronica") {
                    DorCronicaConfig(
                        intensity = chronicIntensity,
                        frequency = chronicFreq,
                        onChanged = { i, f -> chronicIntensity = i; chronicFreq = f }
                    )
                    HorizontalDivider()
                }

                if (definicao.specialRule == "maldicao_divina") {
                    Text("Custo Customizado:", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(value = divineCurseValue, onValueChange = { divineCurseValue = it }, label = { Text("Valor em pontos (ex: -10)") }, modifier = Modifier.fillMaxWidth())
                    HorizontalDivider()
                }

                if (definicao.specialRule == "fraqueza") {
                    FraquezaConfig(
                        rarity = weaknessRarity,
                        frequency = weaknessFreq,
                        onChanged = { r, f -> weaknessRarity = r; weaknessFreq = f }
                    )
                    HorizontalDivider()
                }

                if (definicao.specialRule == "vulnerabilidade") {
                    VulnerabilidadeConfig(
                        rarity = vulnRarity,
                        dmgMult = vulnDmgMult,
                        onChanged = { r, m -> vulnRarity = r; vulnDmgMult = m }
                    )
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
                onSave(nivel, custoCalculado, descricao, autocontrole, mods, metadados) 
            }) { Text(UiActionLabels.ADICIONAR) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
    )
    // ... logic for showAddMod etc
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarDesvantagemDialog(
    desvantagem: DesvantagemSelecionada,
    permiteAutocontrole: Boolean = false,
    descricaoCatalogo: String = "",
    onDismiss: () -> Unit,
    onSave: (DesvantagemSelecionada) -> Unit
) {
    var nivel by remember { mutableIntStateOf(desvantagem.nivel) }
    var custoEscolhido by remember { mutableIntStateOf(desvantagem.custoEscolhido) }
    var descricao by remember { mutableStateOf(desvantagem.descricao) }
    var autocontrole by remember { mutableStateOf(desvantagem.autocontrole) }
    var mods by remember { mutableStateOf(desvantagem.modificadores) }
    var showAddMod by remember { mutableStateOf(false) }
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }
    val descricaoCatalogoFinal = descricaoCatalogo.trim()

    // Reconstruir metadados se necessário (caso venham nulos mas a regra exija)
    var metadadosState by remember { mutableStateOf(desvantagem.metadados ?: emptyMap()) }

    // Estados para Regras Especiais (inicializados dos metadados)
    var enemyBasePower by remember { mutableIntStateOf(metadadosState["basePoder"]?.toIntOrNull() ?: -5) }
    var enemyIntention by remember { mutableFloatStateOf(metadadosState["multIntencao"]?.toFloatOrNull() ?: 1.0f) }
    var enemyFrequency by remember { mutableFloatStateOf(metadadosState["multFrequencia"]?.toFloatOrNull() ?: 1.0f) }
    
    var depRarity by remember { mutableIntStateOf(metadadosState["baseRaridade"]?.toIntOrNull() ?: -5) }
    var depFrequency by remember { mutableFloatStateOf(metadadosState["multFrequencia"]?.toFloatOrNull() ?: 1.0f) }
    var depIllegal by remember { mutableStateOf(metadadosState["ilegal"]?.toBoolean() ?: false) }

    var repBase by remember { mutableIntStateOf(metadadosState["baseReputacao"]?.toIntOrNull() ?: -5) }
    var repGroup by remember { mutableFloatStateOf(metadadosState["multGrupo"]?.toFloatOrNull() ?: 1.0f) }
    var repRecognition by remember { mutableFloatStateOf(metadadosState["multReconhecimento"]?.toFloatOrNull() ?: 1.0f) }

    var dutyBase by remember { mutableIntStateOf(metadadosState["baseDever"]?.toIntOrNull() ?: -5) }
    var dutyHazard by remember { mutableStateOf(metadadosState["perigoso"]?.toBoolean() ?: false) }
    var dutyInvoluntary by remember { mutableStateOf(metadadosState["involuntario"]?.toBoolean() ?: false) }
    var dutyHarmless by remember { mutableStateOf(metadadosState["inofensivo"]?.toBoolean() ?: false) }

    var chronicIntensity by remember { mutableIntStateOf(metadadosState["baseIntensidade"]?.toIntOrNull() ?: -5) }
    var chronicFreq by remember { mutableFloatStateOf(metadadosState["multFrequencia"]?.toFloatOrNull() ?: 0.5f) }

    var weaknessRarity by remember { mutableIntStateOf(metadadosState["baseRaridade"]?.toIntOrNull() ?: -5) }
    var weaknessFreq by remember { mutableFloatStateOf(metadadosState["multFrequencia"]?.toFloatOrNull() ?: 1.0f) }

    var vulnRarity by remember { mutableIntStateOf(metadadosState["baseRaridade"]?.toIntOrNull() ?: -5) }
    var vulnDmgMult by remember { mutableFloatStateOf(metadadosState["multDano"]?.toFloatOrNull() ?: 2.0f) }

    var maintBase by remember { mutableIntStateOf(metadadosState["baseManutencao"]?.toIntOrNull() ?: -10) }
    var maintInterval by remember { mutableFloatStateOf(metadadosState["multIntervalo"]?.toFloatOrNull() ?: 1.0f) }

    var vicioBase by remember { mutableIntStateOf(metadadosState["baseVicio"]?.toIntOrNull() ?: -5) }
    var vicioEffect by remember { mutableIntStateOf(metadadosState["modEfeito"]?.toIntOrNull() ?: 0) }
    var vicioLegal by remember { mutableIntStateOf(metadadosState["modLegalidade"]?.toIntOrNull() ?: 0) }

    var divineCurseValue by remember { mutableStateOf(metadadosState["custoCustom"] ?: "0") }

    val def = remember { CharacterRules.DATA_REPOSITORY_INSTANCE?.getDesvantagemPorId(desvantagem.definicaoId) }
    val specialRule = desvantagem.specialRule ?: def?.specialRule ?: ""

    val currentMetadados = when (specialRule) {
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

    val custoCalculado = CharacterRules.calcularCustoDesvantagem(
        desvantagem.tipoCusto, 
        desvantagem.custoBase, 
        custoEscolhido, 
        nivel, 
        autocontrole, 
        mods, 
        specialRule, 
        currentMetadados
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(desvantagem.nome, color = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.clickable { if (descricaoCatalogoFinal.isNotBlank()) mostrarDescricaoCatalogo = true }) 
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text("Custo Final: $custoCalculado pts", modifier = Modifier.padding(12.dp).fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                Text("Tipo: ${desvantagem.tipoCusto.name} | Custo base: ${def?.custo ?: desvantagem.custoBase} | Pag. ${def?.pagina ?: desvantagem.pagina}", style = MaterialTheme.typography.bodySmall)
                
                HorizontalDivider()

                when (desvantagem.tipoCusto) {
                    TipoCusto.POR_NIVEL -> {
                        Text("Nível:", style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { if (nivel > 1) nivel-- }) { Text("-") }
                            Text("$nivel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { if (nivel < 20) nivel++ }) { Text("+") }
                        }
                    }
                    TipoCusto.ESCOLHA -> {
                        if (specialRule != "vicio") {
                            Text("Ajuste Manual de Custo:", style = MaterialTheme.typography.labelMedium)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { custoEscolhido++ }) { Text("+1") }
                                Text("$custoEscolhido pts", fontWeight = FontWeight.Bold)
                                TextButton(onClick = { custoEscolhido-- }) { Text("-1") }
                            }
                        }
                    }
                    TipoCusto.VARIAVEL -> {
                        if (specialRule.isEmpty() || specialRule == "maldicao_divina") {
                            Text("Ajuste de Custo:", style = MaterialTheme.typography.labelMedium)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { custoEscolhido++ }) { Text("+1") }
                                Text("$custoEscolhido pts", fontWeight = FontWeight.Bold)
                                TextButton(onClick = { custoEscolhido-- }) { Text("-1") }
                            }
                        }
                    }
                    else -> {}
                }

                // Regras Especiais (UI Compartilhada)
                if (specialRule == "inimigos" || specialRule == "dependentes") {
                    InimigosConfig(
                        basePower = enemyBasePower,
                        intention = enemyIntention,
                        frequency = enemyFrequency,
                        isEnemy = specialRule == "inimigos",
                        onChanged = { p, i, f -> enemyBasePower = p; enemyIntention = i; enemyFrequency = f }
                    )
                    HorizontalDivider()
                }

                if (specialRule == "vicio") {
                    VicioConfig(
                        base = vicioBase,
                        effect = vicioEffect,
                        legality = vicioLegal,
                        onChanged = { b, e, l -> vicioBase = b; vicioEffect = e; vicioLegal = l }
                    )
                    HorizontalDivider()
                }

                if (specialRule == "dependencia") {
                    DependenciaConfig(
                        rarity = depRarity,
                        frequency = depFrequency,
                        isIllegal = depIllegal,
                        onChanged = { r, f, i -> depRarity = r; depFrequency = f; depIllegal = i }
                    )
                    HorizontalDivider()
                }

                if (specialRule == "reputacao") {
                    ReputacaoConfig(
                        base = repBase,
                        group = repGroup,
                        onChanged = { b, g -> repBase = b; repGroup = g }
                    )
                    HorizontalDivider()
                }

                if (specialRule == "dever") {
                    DeverConfig(
                        base = dutyBase,
                        isHazardous = dutyHazard,
                        isInvoluntary = dutyInvoluntary,
                        onChanged = { b, h, i -> dutyBase = b; dutyHazard = h; dutyInvoluntary = i }
                    )
                    HorizontalDivider()
                }

                if (specialRule == "manutencao") {
                    ManutencaoConfig(
                        base = maintBase,
                        interval = maintInterval,
                        onChanged = { b, i -> maintBase = b; maintInterval = i }
                    )
                    HorizontalDivider()
                }

                if (specialRule == "dor_cronica") {
                    DorCronicaConfig(
                        intensity = chronicIntensity,
                        frequency = chronicFreq,
                        onChanged = { i, f -> chronicIntensity = i; chronicFreq = f }
                    )
                    HorizontalDivider()
                }

                if (specialRule == "fraqueza") {
                    FraquezaConfig(
                        rarity = weaknessRarity,
                        frequency = weaknessFreq,
                        onChanged = { r, f -> weaknessRarity = r; weaknessFreq = f }
                    )
                    HorizontalDivider()
                }

                if (specialRule == "vulnerabilidade") {
                    VulnerabilidadeConfig(
                        rarity = vulnRarity,
                        dmgMult = vulnDmgMult,
                        onChanged = { r, m -> vulnRarity = r; vulnDmgMult = m }
                    )
                    HorizontalDivider()
                }

                if (specialRule == "maldicao_divina") {
                    Text("Custo Customizado:", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(value = divineCurseValue, onValueChange = { divineCurseValue = it }, label = { Text("Valor em pontos (ex: -10)") }, modifier = Modifier.fillMaxWidth())
                    HorizontalDivider()
                }

                OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição Personalizada") }, modifier = Modifier.fillMaxWidth())

                if (permiteAutocontrole || desvantagem.autocontrole != null) {
                    HorizontalDivider()
                    Text("Autocontrole:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(null to "Nenhum", 6 to "6 (x2)", 9 to "9 (x1.5)", 12 to "12 (x1)", 15 to "15 (x0.5)").forEach { (valor, label) ->
                            FilterChip(selected = autocontrole == valor, onClick = { autocontrole = valor }, label = { Text(label) })
                        }
                    }
                }

                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Modificadores (%)", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { showAddMod = true }) { Icon(Icons.Default.Add, null); Text("Add") }
                }

                mods.forEachIndexed { idx, mod ->
                    ModificadorSelecionadoItem(mod, onUpdate = { m -> mods = mods.toMutableList().apply { this[idx] = m } }, onDelete = { mods = mods.toMutableList().apply { removeAt(idx) } })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                onSave(desvantagem.copy(
                    nivel = nivel,
                    custoEscolhido = if (specialRule == "vicio") (vicioBase + vicioEffect + vicioLegal) else custoEscolhido,
                    descricao = descricao,
                    autocontrole = autocontrole,
                    modificadores = mods,
                    metadados = currentMetadados
                )) 
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )

    if (mostrarDescricaoCatalogo) {
        CatalogoDescricaoDialog(
            nome = desvantagem.nome,
            descricao = descricaoCatalogoFinal,
            onDismiss = { mostrarDescricaoCatalogo = false }
        )
    }
}


