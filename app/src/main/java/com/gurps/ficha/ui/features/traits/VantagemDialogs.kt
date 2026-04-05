package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.model.*
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.UiTokens
import com.gurps.ficha.viewmodel.FichaViewModel
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.data.DataRepository
import kotlin.math.abs

// --- Utilitários de Vantagem ---

internal fun vantagemEhAptidaoMagica(definicaoId: String): Boolean {
    return definicaoId.equals("aptidao_magica", ignoreCase = true)
}

internal fun nivelExibicaoVantagem(definicaoId: String, nivelInterno: Int): Int {
    return if (vantagemEhAptidaoMagica(definicaoId)) {
        (nivelInterno - 1).coerceAtLeast(0)
    } else {
        nivelInterno
    }
}



// --- Diálogos Principais de Vantagem ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarVantagemDialog(viewModel: FichaViewModel, onDismiss: () -> Unit) {
    var busca by remember { mutableStateOf("") }
    var filtroTipo by remember { mutableStateOf<TipoCusto?>(null) }
    var filtroTag by remember { mutableStateOf<String?>(null) }
    var vantagemSelecionada by remember { mutableStateOf<VantagemDefinicao?>(null) }

    val tagsDisponiveis = listOf("combate", "social", "fisica", "mental", "magica")
    val listaFiltrada = viewModel.dataRepository.filtrarVantagens(busca, filtroTipo, filtroTag)

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Selecionar Vantagem", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                label = { Text("Buscar...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)
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

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(listaFiltrada) { definicao ->
                    val jaAdicionada = viewModel.vantagemJaAdicionada(definicao.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !jaAdicionada) { vantagemSelecionada = definicao },
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    definicao.nome,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (jaAdicionada) FontWeight.Normal else FontWeight.Medium
                                )
                                Text(
                                    "${definicao.custo} pts | ${definicao.tipoCusto.name.lowercase()} | pag. ${definicao.pagina}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (jaAdicionada) {
                                Text("Adicionada", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text(UiActionLabels.FECHAR) }
            }
        }
    }

    vantagemSelecionada?.let { definicao ->
        ConfigurarVantagemDialog(
            definicao = definicao,
            onDismiss = { vantagemSelecionada = null },
            onSave = { nivel, custoEscolhido, descricao, mods, metadados ->
                viewModel.adicionarVantagem(definicao, nivel, custoEscolhido, descricao, mods, metadados)
                vantagemSelecionada = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarVantagemDialog(
    definicao: VantagemDefinicao,
    onDismiss: () -> Unit,
    onSave: (Int, Int, String, List<ModificadorSelecao>, Map<String, String>?) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    
    // Estados base
    var nivel by remember { mutableStateOf(1) }
    var custoEscolhido by remember { mutableStateOf(definicao.getCustoBase()) }
    var descricao by remember { mutableStateOf("") }
    val descricaoCatalogo = definicao.descricao?.trim().orEmpty()
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }

    var mods by remember { mutableStateOf(emptyList<ModificadorSelecao>()) }
    var showAddMod by remember { mutableStateOf(false) }
    var showSchoolPicker by remember { mutableStateOf(false) }
    var pendingModForSchool by remember { mutableStateOf<ModificadorDefinicao?>(null) }

    // Estados para vantagens especiais
    var freqAliado by remember { mutableStateOf(1f) } // Multiplicador
    var ratioAliado by remember { mutableStateOf(5) } // Base 100% = 5 pts
    var grupoAliado by remember { mutableStateOf(1) } // Multiplicador

    var nhContato by remember { mutableStateOf(12) }
    var freqContato by remember { mutableStateOf(1f) }
    var confContato by remember { mutableStateOf(1f) }

    var powerPatrono by remember { mutableStateOf(10) }
    var freqPatrono by remember { mutableStateOf(1f) }
    var modPatrono by remember { mutableStateOf(1.0f) }
    var secretoPatrono by remember { mutableStateOf(false) }

    var powerFavor by remember { mutableStateOf(10) }
    var modFavor by remember { mutableStateOf(1.0f) }
    var secretoFavor by remember { mutableStateOf(false) }
    var isContactFavor by remember { mutableStateOf(false) }

    // Estados para Ataque Inato / Golpeadores / Dentes
    var nomeAtaque by remember { mutableStateOf("") }
    var tipoDanoAtaque by remember { mutableStateOf("cont") }
    var dadosAtaque by remember { mutableStateOf(1) }
    var bonusAtaque by remember { mutableStateOf(0) }
    var tipoDentes by remember { mutableStateOf("rombo") }

    val metadados = when (definicao.id) {
        "ataque_inato", "golpeadores" -> mapOf(
            "tipoDano" to tipoDanoAtaque,
            "dice" to dadosAtaque.toString(),
            "bonus" to bonusAtaque.toString(),
            "nomePersonalizado" to nomeAtaque
        )
        "dentes" -> mapOf("tipoDentes" to tipoDentes)
        else -> null
    }

    // Sincroniza\u00e7\u00e3o de custos especiais
    LaunchedEffect(definicao.id, freqAliado, ratioAliado, grupoAliado, nhContato, freqContato, confContato, powerPatrono, freqPatrono, modPatrono, secretoPatrono, powerFavor, modFavor, secretoFavor, isContactFavor) {
        when (definicao.id) {
            "aliados" -> custoEscolhido = CharacterRules.calcularCustoAliado(ratioAliado, freqAliado, grupoAliado)
            "contatos" -> custoEscolhido = CharacterRules.calcularCustoContato(nhContato, freqContato, confContato)
            "patronos" -> custoEscolhido = CharacterRules.calcularCustoPatrono(powerPatrono, freqPatrono, modPatrono, if (secretoPatrono) -5 else 0)
            "favor" -> custoEscolhido = CharacterRules.calcularCustoFavor(powerFavor, modFavor, if (secretoFavor) -5 else 0, isContactFavor)
        }
    }

    val opcoesEscolha = definicao.getOpcoesEscolha()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                definicao.nome,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .let {
                        if (descricaoCatalogo.isNotBlank()) {
                            it.clickable { mostrarDescricaoCatalogo = true }
                        } else {
                            it
                        }
                    }
                    .semantics {
                        if (isPraCegoVariant && descricaoCatalogo.isNotBlank()) {
                            contentDescription = "Nome da vantagem ${definicao.nome}. Toque para abrir descricao."
                        }
                    }
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)
            ) {
                val custoCalculado = CharacterRules.calcularCustoVantagem(
                    personagem = null,
                    definicaoId = definicao.id,
                    tipoCusto = definicao.tipoCusto,
                    custoBase = definicao.getCustoBase(),
                    custoEscolhido = custoEscolhido,
                    nivel = nivel,
                    modificadores = mods,
                    metadados = metadados
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        "Custo: $custoCalculado pts",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text("Tipo: ${definicao.tipoCusto.name} | Custo base: ${definicao.custo} | Pag. ${definicao.pagina}", style = MaterialTheme.typography.bodySmall)

                when (definicao.tipoCusto) {
                    TipoCusto.POR_NIVEL -> {
                        Text("Nível:")
                        val nivelMinimo = 1
                        val nivelMaximo = if (vantagemEhAptidaoMagica(definicao.id)) 11 else 20
                        if (isPraCegoVariant) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { if (nivel > nivelMinimo) nivel-- }) { Text("-") }
                                Text("${nivelExibicaoVantagem(definicao.id, nivel)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { if (nivel < nivelMaximo) nivel++ }) { Text("+") }
                            }
                        } else {
                            Text(
                                "${nivelExibicaoVantagem(definicao.id, nivel)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(56.dp).pointerInput(nivel) {
                                    var dragAcumulado = 0f
                                    val passoPx = 24f
                                    detectVerticalDragGestures(onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAcumulado += dragAmount
                                        while (abs(dragAcumulado) >= passoPx) {
                                            nivel = if (dragAcumulado < 0f) (nivel + 1).coerceAtMost(nivelMaximo) else (nivel - 1).coerceAtLeast(nivelMinimo)
                                            dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                        }
                                    })
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        if (definicao.id == "atribulacao") {
                            AtribulacaoConfig(
                                modifiers = mods,
                                onAddModifier = { m -> mods = mods.toMutableList().apply { add(m) } },
                                descricaoContent = {
                                    OutlinedTextField(
                                        value = descricao,
                                        onValueChange = { descricao = it },
                                        label = { Text("Descrição/Especializações") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            )
                        } else if (definicao.id == "retencao") {
                            RetencaoConfig(
                                modifiers = mods,
                                onAddModifier = { m -> mods = mods.toMutableList().apply { add(m) } },
                                descricaoContent = {
                                    OutlinedTextField(
                                        value = descricao,
                                        onValueChange = { descricao = it },
                                        label = { Text("Descrição/Especializações") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            )
                        } else {
                            OutlinedTextField(
                                value = descricao,
                                onValueChange = { descricao = it },
                                label = { Text("Descrição/Especializações") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    TipoCusto.ESCOLHA -> {
                        opcoesEscolha.forEach { opcao ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { custoEscolhido = opcao }) {
                                RadioButton(selected = custoEscolhido == opcao, onClick = { custoEscolhido = opcao })
                                Text("$opcao pts")
                            }
                        }
                        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição/Especializações") }, modifier = Modifier.fillMaxWidth())
                    }
                    TipoCusto.VARIAVEL -> {
                        when (definicao.id) {
                            "aliados" -> {
                                AliadosConfig(
                                    currentRatio = ratioAliado,
                                    currentFreq = freqAliado,
                                    currentGroup = grupoAliado,
                                    onChanged = { r, f, g -> ratioAliado = r; freqAliado = f; grupoAliado = g }
                                )
                            }
                            "ataque_inato" -> {
                                AtaqueInatoConfig(
                                    nome = nomeAtaque,
                                    tipoDano = tipoDanoAtaque,
                                    dados = dadosAtaque,
                                    bonus = bonusAtaque,
                                    onChanged = { n, t, d, b ->
                                        nomeAtaque = n
                                        tipoDanoAtaque = t
                                        dadosAtaque = d
                                        bonusAtaque = b
                                    }
                                )
                            }
                            "golpeadores" -> {
                                GolpeadoresConfig(
                                    nome = nomeAtaque,
                                    tipoDano = tipoDanoAtaque,
                                    dados = dadosAtaque,
                                    bonus = bonusAtaque,
                                    onChanged = { n, t, d, b ->
                                        nomeAtaque = n
                                        tipoDanoAtaque = t
                                        dadosAtaque = d
                                        bonusAtaque = b
                                    }
                                )
                            }
                            "dentes" -> {
                                DentesConfig(
                                    currentType = tipoDentes,
                                    onChanged = { tipoDentes = it }
                                )
                            }
                            "contatos" -> {
                                ContatosConfig(
                                    currentNh = nhContato,
                                    currentFreq = freqContato,
                                    currentConf = confContato,
                                    onChanged = { h, f, c -> nhContato = h; freqContato = f; confContato = c }
                                )
                            }
                            "patronos" -> {
                                PatronosConfig(
                                    currentPower = powerPatrono,
                                    currentFreq = freqPatrono,
                                    currentMod = modPatrono,
                                    isSecret = secretoPatrono,
                                    onChanged = { p, f, m, s -> powerPatrono = p; freqPatrono = f; modPatrono = m; secretoPatrono = s }
                                )
                            }
                            "favor" -> {
                                FavorConfig(
                                    currentPower = powerFavor,
                                    currentMod = modFavor,
                                    isSecret = secretoFavor,
                                    isContact = isContactFavor,
                                    onChanged = { p, m, s, c -> powerFavor = p; modFavor = m; secretoFavor = s; isContactFavor = c }
                                )
                            }
                            else -> {
                                Text("Custo Variável:")
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { custoEscolhido -= 1 }) { Text("-1") }
                                    Text("$custoEscolhido pts", fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { custoEscolhido += 1 }) { Text("+1") }
                                }
                            }
                        }
                        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição/Especialização") }, modifier = Modifier.fillMaxWidth())
                    }
                    TipoCusto.FIXO -> {
                        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição/Especialização") }, modifier = Modifier.fillMaxWidth())
                    }
                }

                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Modificadores (%)", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { showAddMod = true }) {
                        Icon(Icons.Default.Add, null)
                        Text("Add")
                    }
                }

                if (mods.isEmpty()) {
                    Text("Nenhum modificador aplicado.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                } else {
                    mods.forEachIndexed { idx, mod ->
                        ModificadorSelecionadoItem(mod, onUpdate = { m -> mods = mods.toMutableList().apply { this[idx] = m } }, onDelete = { mods = mods.toMutableList().apply { removeAt(idx) } })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(nivel, custoEscolhido, descricao, mods, metadados) },
                enabled = (definicao.id != "ataque_inato" && definicao.id != "golpeadores") || nomeAtaque.isNotBlank()
            ) { Text(UiActionLabels.ADICIONAR) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
    )

    if (showAddMod) {
        EscopoModificadoresDialog(
            especificos = definicao.modificadoresEspecificos ?: emptyList(),
            gerais = CharacterRules.DATA_REPOSITORY_INSTANCE?.modificadoresGerais ?: emptyList(),
            onDismiss = { showAddMod = false },
            onSelect = { modDef ->
                if (modDef.id == "mod_aptidao_escola") { pendingModForSchool = modDef; showSchoolPicker = true; showAddMod = false }
                else {
                    val valorInt = Regex("-?\\d+").find(modDef.valor)?.value?.toIntOrNull() ?: 0
                    mods = mods.toMutableList().apply { add(ModificadorSelecao(modDef.id, modDef.nome, valorInt, modDef.porNivel, 1, modDef.descricao, modDef.pagina)) }
                    showAddMod = false
                }
            }
        )
    }

    if (showSchoolPicker) {
        SeletorEscolaMagiaDialog(onDismiss = { showSchoolPicker = false }, onSelect = { escola ->
            pendingModForSchool?.let { modDef ->
                mods = mods.toMutableList().apply { add(ModificadorSelecao(modDef.id, modDef.nome, -40, false, 1, escola, modDef.pagina)) }
            }
            showSchoolPicker = false
            showAddMod = false
        })
    }

    if (mostrarDescricaoCatalogo) {
        AlertDialog(onDismissRequest = { mostrarDescricaoCatalogo = false }, title = { Text(definicao.nome) }, text = { Text(descricaoCatalogo.ifBlank { "Sem descrição disponível." }) },
            confirmButton = { TextButton(onClick = { mostrarDescricaoCatalogo = false }) { Text(UiActionLabels.FECHAR) } })
    }
}

@Composable
fun EditarVantagemDialog(vantagem: VantagemSelecionada, descricaoCatalogo: String = "", onDismiss: () -> Unit, onSave: (VantagemSelecionada) -> Unit) {
    var nivel by remember { mutableStateOf(vantagem.nivel) }
    var custoEscolhido by remember { mutableStateOf(vantagem.custoEscolhido) }
    var descricao by remember { mutableStateOf(vantagem.descricao) }
    var mods by remember { mutableStateOf(vantagem.modificadores.toList()) }

    var showAddMod by remember { mutableStateOf(false) }
    var showSchoolPicker by remember { mutableStateOf(false) }
    var pendingModForSchool by remember { mutableStateOf<ModificadorDefinicao?>(null) }
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }
    
    val descricaoCatalogoFinal = if (vantagem.definicaoId == "retencao") {
        "O personagem tem um ataque capaz de manter o alvo preso no lugar..." 
    } else descricaoCatalogo

    var nomeAtaque by remember { mutableStateOf(vantagem.metadados?.get("nomePersonalizado") ?: vantagem.nome) }
    var tipoDanoAtaque by remember { mutableStateOf(vantagem.metadados?.get("tipoDano") ?: "cont") }
    var dadosAtaque by remember { mutableStateOf(vantagem.metadados?.get("dice")?.toIntOrNull() ?: 1) }
    var bonusAtaque by remember { mutableStateOf(vantagem.metadados?.get("bonus")?.toIntOrNull() ?: 0) }
    var tipoDentes by remember { mutableStateOf(vantagem.metadados?.get("tipoDentes") ?: "rombo") }

    val metadados = when (vantagem.definicaoId) {
        "ataque_inato", "golpeadores" -> mapOf(
            "tipoDano" to tipoDanoAtaque,
            "dice" to dadosAtaque.toString(),
            "bonus" to bonusAtaque.toString(),
            "nomePersonalizado" to nomeAtaque
        )
        "dentes" -> mapOf("tipoDentes" to tipoDentes)
        else -> null
    }

    val def = remember { CharacterRules.DATA_REPOSITORY_INSTANCE?.getVantagemPorId(vantagem.definicaoId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(vantagem.nome, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { if (descricaoCatalogoFinal.isNotBlank()) mostrarDescricaoCatalogo = true })
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                val custoCalculado = CharacterRules.calcularCustoVantagem(
                    personagem = null,
                    definicaoId = vantagem.definicaoId,
                    tipoCusto = vantagem.tipoCusto,
                    custoBase = def?.getCustoBase() ?: vantagem.custoBase,
                    custoEscolhido = custoEscolhido,
                    nivel = nivel,
                    modificadores = mods,
                    metadados = metadados
                )
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text("Custo: $custoCalculado pts", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                if (vantagem.tipoCusto == TipoCusto.POR_NIVEL) {
                    Text("Nível:")
                    val nivelMinimo = 1
                    val nivelMaximo = if (vantagemEhAptidaoMagica(vantagem.definicaoId)) 11 else 20
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { if (nivel > nivelMinimo) nivel-- }) { Text("-") }
                        Text("${nivelExibicaoVantagem(vantagem.definicaoId, nivel)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { if (nivel < nivelMaximo) nivel++ }) { Text("+") }
                    }
                    
                    if (vantagem.definicaoId == "atribulacao") {
                        AtribulacaoConfig(modifiers = mods, onAddModifier = { m -> mods = mods.toMutableList().apply { add(m) } }, descricaoContent = { OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth()) })
                    } else if (vantagem.definicaoId == "retencao") {
                        RetencaoConfig(modifiers = mods, onAddModifier = { m -> mods = mods.toMutableList().apply { add(m) } }, descricaoContent = { OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth()) })
                    } else {
                        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
                    }
                } else if (vantagem.tipoCusto == TipoCusto.VARIAVEL) {
                    Text("Custo Variável: $custoEscolhido pts")
                    if (vantagem.definicaoId == "ataque_inato") {
                        AtaqueInatoConfig(nome = nomeAtaque, tipoDano = tipoDanoAtaque, dados = dadosAtaque, bonus = bonusAtaque, onChanged = { n, t, d, b -> nomeAtaque = n; tipoDanoAtaque = t; dadosAtaque = d; bonusAtaque = b })
                    } else if (vantagem.definicaoId == "golpeadores") {
                        GolpeadoresConfig(nome = nomeAtaque, tipoDano = tipoDanoAtaque, dados = dadosAtaque, bonus = bonusAtaque, onChanged = { n, t, d, b -> nomeAtaque = n; tipoDanoAtaque = t; dadosAtaque = d; bonusAtaque = b })
                    } else if (vantagem.definicaoId == "dentes") {
                        DentesConfig(currentType = tipoDentes, onChanged = { tipoDentes = it })
                    }
                    OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
                } else {
                    OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
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
            TextButton(
                onClick = {
                    onSave(vantagem.copy(nivel = nivel, custoEscolhido = custoEscolhido, descricao = descricao, modificadores = mods, metadados = metadados))
                }
            ) { Text(UiActionLabels.SALVAR) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
    )

    if (showAddMod) {
        EscopoModificadoresDialog(
            especificos = def?.modificadoresEspecificos ?: emptyList(),
            gerais = CharacterRules.DATA_REPOSITORY_INSTANCE?.modificadoresGerais ?: emptyList(),
            onDismiss = { showAddMod = false },
            onSelect = { modDef ->
                if (modDef.id == "mod_aptidao_escola") { pendingModForSchool = modDef; showSchoolPicker = true }
                else {
                    val valorInt = Regex("-?\\d+").find(modDef.valor)?.value?.toIntOrNull() ?: 0
                    mods = mods.toMutableList().apply { add(ModificadorSelecao(modDef.id, modDef.nome, valorInt, modDef.porNivel, 1, modDef.descricao, modDef.pagina)) }
                    showAddMod = false
                }
            }
        )
    }

    if (showSchoolPicker) {
        SeletorEscolaMagiaDialog(onDismiss = { showSchoolPicker = false }, onSelect = { escola ->
            pendingModForSchool?.let { modDef ->
                mods = mods.toMutableList().apply { add(ModificadorSelecao(modDef.id, modDef.nome, -40, false, 1, escola, modDef.pagina)) }
            }
            showSchoolPicker = false
            showAddMod = false
        })
    }
}

// --- Diálogos Menores ---



// --- Configura\u00e7\u00f5es Espec??ficas (Special Rules) ---

@Composable
fun AliadosConfig(currentRatio: Int, currentFreq: Float, currentGroup: Int, onChanged: (Int, Float, Int) -> Unit) {
    var showRatioList by remember { mutableStateOf(false) }
    var showFreqList by remember { mutableStateOf(false) }
    var showGroupList by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { showRatioList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Poder do Aliado") }
        Button(onClick = { showFreqList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Frequência") }
        Button(onClick = { showGroupList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Tamanho do Grupo") }
        
        if (showRatioList) SeletorPoderAliadoDialog(current = currentRatio, onDismiss = { showRatioList = false }, onSelect = { onChanged(it, currentFreq, currentGroup); showRatioList = false })
        if (showFreqList) SeletorFrequenciaAparecimentoDialog(current = currentFreq, onDismiss = { showFreqList = false }, onSelect = { onChanged(currentRatio, it, currentGroup); showFreqList = false })
        if (showGroupList) SeletorGrupoAliadoDialog(current = currentGroup, onDismiss = { showGroupList = false }, onSelect = { onChanged(currentRatio, currentFreq, it); showGroupList = false })
    }
}

@Composable
fun PatronosConfig(currentPower: Int, currentFreq: Float, currentMod: Float, isSecret: Boolean, onChanged: (Int, Float, Float, Boolean) -> Unit) {
    var showPowerList by remember { mutableStateOf(false) }
    var showFreqList by remember { mutableStateOf(false) }
    var showModList by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { showPowerList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Poder do Patrono") }
        Button(onClick = { showFreqList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Frequência") }
        Button(onClick = { showModList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Modificadores") }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onChanged(currentPower, currentFreq, currentMod, !isSecret) }) {
            Checkbox(checked = isSecret, onCheckedChange = {命中 -> onChanged(currentPower, currentFreq, currentMod, 命中) })
            Text("Patrono Secreto (-5 pts)")
        }
        if (showPowerList) SeletorPoderPatronoDialog(current = currentPower, onDismiss = { showPowerList = false }, onSelect = { onChanged(it, currentFreq, currentMod, isSecret); showPowerList = false })
        if (showFreqList) SeletorFrequenciaAparecimentoDialog(current = currentFreq, onDismiss = { showFreqList = false }, onSelect = { onChanged(currentPower, it, currentMod, isSecret); showFreqList = false })
        if (showModList) SeletorModificadorPatronoDialog(current = currentMod, onDismiss = { showModList = false }, onSelect = { onChanged(currentPower, currentFreq, it, isSecret); showModList = false })
    }
}

@Composable
fun FavorConfig(currentPower: Int, currentMod: Float, isSecret: Boolean, isContact: Boolean, onChanged: (Int, Float, Boolean, Boolean) -> Unit) {
    var showTypeList by remember { mutableStateOf(false) }
    var showPowerList by remember { mutableStateOf(false) }
    var showModList by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { showTypeList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text(if (isContact) "Tipo: Contato" else "Tipo: Patrono") }
        Button(onClick = { showPowerList = true }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text(if (isContact) "Perícia" else "Poder") }
        if (showTypeList) SeletorTipoFavorDialog(isContact = isContact, onDismiss = { showTypeList = false }, onSelect = { onChanged(currentPower, currentMod, isSecret, it); showTypeList = false })
        if (showPowerList) {
            if (isContact) SeletorNhContatoDialog(current = currentPower, onDismiss = { showPowerList = false }, onSelect = { onChanged(it, currentMod, isSecret, isContact); showPowerList = false })
            else SeletorPoderPatronoDialog(current = currentPower, onDismiss = { showPowerList = false }, onSelect = { onChanged(it, currentMod, isSecret, isContact); showPowerList = false })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AtaqueInatoConfig(
    nome: String,
    tipoDano: String,
    dados: Int,
    bonus: Int,
    onChanged: (String, String, Int, Int) -> Unit
) {
    val tiposDano = listOf(
        "cont" to "Contusão",
        "queimadura" to "Queimadura",
        "corte" to "Corte",
        "perfuracao" to "Perfuração",
        "pa-" to "Perfurante-",
        "pa" to "Perfurante",
        "pa+" to "Perfurante+",
        "pa++" to "Perfurante++",
        "corrosao" to "Corrosão",
        "fadiga" to "Fadiga",
        "toxina" to "Toxina"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = nome,
            onValueChange = { onChanged(it, tipoDano, dados, bonus) },
            label = { Text("Nome do Ataque (ex: Bola de Fogo)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Tipo de Dano:", style = MaterialTheme.typography.labelMedium)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tiposDano.forEach { (id, label) ->
                FilterChip(
                    selected = tipoDano == id,
                    onClick = { onChanged(nome, id, dados, bonus) },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Dados:", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (dados > 0) onChanged(nome, tipoDano, dados - 1, bonus) }) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                    Text("${dados}d", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (dados < 100) onChanged(nome, tipoDano, dados + 1, bonus) }) {
                        Icon(Icons.Default.Add, "Mais dados")
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Bônus:", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (bonus > -1) onChanged(nome, tipoDano, dados, bonus - 1) }) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                    val bonusStr = if (bonus >= 0) "+$bonus" else "$bonus"
                    Text(bonusStr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (bonus < 2) onChanged(nome, tipoDano, dados, bonus + 1) }) {
                        Icon(Icons.Default.Add, "Mais bônus")
                    }
                }
            }
        }

        val bonusDisplay = if (bonus > 0) "+$bonus" else if (bonus < 0) "$bonus" else ""
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        ) {
            Text(
                "Dano: ${dados}d${bonusDisplay} ${tiposDano.find { it.first == tipoDano }?.second ?: tipoDano}",
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GolpeadoresConfig(
    nome: String,
    tipoDano: String,
    dados: Int,
    bonus: Int,
    onChanged: (String, String, Int, Int) -> Unit
) {
    val tiposDano = listOf(
        "cont" to "Contusão",
        "corte" to "Corte",
        "perfuracao" to "Perfuração",
        "pa-" to "Perfurante-",
        "pa" to "Perfurante",
        "pa+" to "Perfurante+",
        "pa++" to "Perfurante++"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = nome,
            onValueChange = { onChanged(it, tipoDano, dados, bonus) },
            label = { Text("Nome do Golpeador (ex: Cauda Espinhosa)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Tipo de Dano:", style = MaterialTheme.typography.labelMedium)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tiposDano.forEach { (id, label) ->
                FilterChip(
                    selected = tipoDano == id,
                    onClick = { onChanged(nome, id, dados, bonus) },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Dados/Bônus extras (opcional):", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (dados > 0) onChanged(nome, tipoDano, dados - 1, bonus) }) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                    Text("${dados}d", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (dados < 20) onChanged(nome, tipoDano, dados + 1, bonus) }) {
                        Icon(Icons.Default.Add, "Mais dados")
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Bônus:", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (bonus > -5) onChanged(nome, tipoDano, dados, bonus - 1) }) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }
                    val bonusStr = if (bonus >= 0) "+$bonus" else "$bonus"
                    Text(bonusStr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (bonus < 5) onChanged(nome, tipoDano, dados, bonus + 1) }) {
                        Icon(Icons.Default.Add, "Mais bônus")
                    }
                }
            }
        }

        val bonusDisplay = if (bonus > 0) "+$bonus" else if (bonus < 0) "$bonus" else ""
        val danoFinalDesc = if (dados == 0 && bonus == 0) "Dano baseado em ST" else "${dados}d${bonusDisplay}"
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        ) {
            Text(
                "Dano Final: $danoFinalDesc ${tiposDano.find { it.first == tipoDano }?.second ?: tipoDano}",
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
fun AtribulacaoConfig(modifiers: List<ModificadorSelecao>, onAddModifier: (ModificadorSelecao) -> Unit, descricaoContent: @Composable () -> Unit = {}) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        descricaoContent()
        Button(onClick = { /* showCondList = true */ }, modifier = Modifier.fillMaxWidth()) { Text("Condições (Ampliação)") }
    }
}

@Composable
fun RetencaoConfig(modifiers: List<ModificadorSelecao>, onAddModifier: (ModificadorSelecao) -> Unit, descricaoContent: @Composable () -> Unit = {}) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        descricaoContent()
        Button(onClick = { /* showAmpList = true */ }, modifier = Modifier.fillMaxWidth()) { Text("Ampliações de Retenção") }
    }
}

// --- Seletores Gen\u00e9ricos ---

@Composable
fun SeletorPoderAliadoDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val opcoes = listOf(25 to 1, 50 to 2, 75 to 3, 100 to 5, 150 to 10)
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Poder do Aliado") }, text = {
        Column {
            opcoes.forEach { (pts, custo) ->
                ListItem(headlineContent = { Text("$pts% dos pts ($custo pts)") }, modifier = Modifier.clickable { onSelect(custo) })
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}



@Composable
fun SeletorGrupoAliadoDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val opcoes = listOf(
        "Único | x1" to 1,
        "Grupo (2-5) | x2" to 2,
        "Grupo (6-10) | x6" to 6,
        "Grupo (11-20) | x12" to 12
    )
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tamanho do Grupo") }, text = {
        Column {
            opcoes.forEach { (label, mult) ->
                ListItem(headlineContent = { Text(label) }, modifier = Modifier.clickable { onSelect(mult) })
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}

@Composable
fun SeletorPoderPatronoDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val opcoes = listOf(10 to "Menor", 15 to "Médio", 20 to "Poderoso", 25 to "Muito Poderoso")
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Poder do Patrono") }, text = {
        Column { opcoes.forEach { (pts, label) -> ListItem(headlineContent = { Text("$label ($pts pts)") }, modifier = Modifier.clickable { onSelect(pts) }) } }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}

@Composable
fun SeletorModificadorPatronoDialog(current: Float, onDismiss: () -> Unit, onSelect: (Float) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Modificadores") }, text = { Text("Opções de modificadores especiais...") }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}

@Composable
fun SeletorTipoFavorDialog(isContact: Boolean, onDismiss: () -> Unit, onSelect: (Boolean) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tipo de Favor") }, text = {
        Column {
            ListItem(headlineContent = { Text("Como Contato") }, modifier = Modifier.clickable { onSelect(true) })
            ListItem(headlineContent = { Text("Como Patrono") }, modifier = Modifier.clickable { onSelect(false) })
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } })
}



@Composable
fun ContatosConfig(
    currentNh: Int,
    currentFreq: Float,
    currentConf: Float,
    onChanged: (Int, Float, Float) -> Unit
) {
    var showNhList by remember { mutableStateOf(false) }
    var showFreqList by remember { mutableStateOf(false) }
    var showConfList by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 1. NH do Contato
        Button(
            onClick = { showNhList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Perícia (NH $currentNh)", style = MaterialTheme.typography.labelLarge)
        }

        // 2. Frequência
        Button(
            onClick = { showFreqList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            val freqLabel = when(currentFreq) {
                0.5f -> "6-"
                1f -> "9-"
                2f -> "12-"
                3f -> "15-"
                else -> "9-"
            }
            Text("Frequência de Aparecimento ($freqLabel)", style = MaterialTheme.typography.labelLarge)
        }

        // 3. Confiabilidade
        Button(
            onClick = { showConfList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Confiabilidade", style = MaterialTheme.typography.labelLarge)
        }

        // Diálogos
        if (showNhList) {
            SeletorNhContatoDialog(
                current = currentNh,
                onDismiss = { showNhList = false },
                onSelect = { newVal: Int -> onChanged(newVal, currentFreq, currentConf); showNhList = false }
            )
        }
        if (showFreqList) {
            SeletorFrequenciaAparecimentoDialog(
                current = currentFreq,
                onDismiss = { showFreqList = false },
                onSelect = { newVal: Float -> onChanged(currentNh, newVal, currentConf); showFreqList = false }
            )
        }
        if (showConfList) {
            SeletorConfiabilidadeDialog(
                current = currentConf,
                onDismiss = { showConfList = false },
                onSelect = { newVal: Float -> onChanged(currentNh, currentFreq, newVal); showConfList = false }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DentesConfig(
    currentType: String,
    onChanged: (String) -> Unit
) {
    val tipos = listOf(
        "rombo" to "Dentes Rombos (0 pts)",
        "bico_afiado" to "Bico Afiado (1 pt)",
        "dentes_afiados" to "Dentes Afiados (1 pt)",
        "presas" to "Presas (2 pts)"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tipo de Dentição:", style = MaterialTheme.typography.labelLarge)
        
        tipos.forEach { (id, label) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChanged(id) },
                colors = CardDefaults.cardColors(
                    containerColor = if (currentType == id) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = if (currentType == id) 
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
                else null
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = currentType == id, onClick = { onChanged(id) })
                    Spacer(Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
