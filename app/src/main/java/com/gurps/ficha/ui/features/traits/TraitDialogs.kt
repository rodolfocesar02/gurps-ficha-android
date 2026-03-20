package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign

import com.gurps.ficha.BuildConfig
import com.gurps.ficha.model.*
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.UiTokens
import com.gurps.ficha.viewmodel.FichaViewModel
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.data.DataRepository
import kotlin.math.abs

private fun vantagemEhAptidaoMagica(definicaoId: String): Boolean {
    return definicaoId.equals("aptidao_magica", ignoreCase = true)
}

private fun nivelExibicaoVantagem(definicaoId: String, nivelInterno: Int): Int {
    return if (vantagemEhAptidaoMagica(definicaoId)) {
        (nivelInterno - 1).coerceAtLeast(0)
    } else {
        nivelInterno
    }
}

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

    // Estados para Ataque Inato
    var nomeAtaque by remember { mutableStateOf("") }
    var tipoDanoAtaque by remember { mutableStateOf("cont") }
    var dadosAtaque by remember { mutableStateOf(1) }
    var bonusAtaque by remember { mutableStateOf(0) }

    val metadadosAtaque = if (definicao.id == "ataque_inato") {
        mapOf(
            "tipoDano" to tipoDanoAtaque,
            "dice" to dadosAtaque.toString(),
            "bonus" to bonusAtaque.toString(),
            "nomePersonalizado" to nomeAtaque
        )
    } else null

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
    val intervalo = definicao.getIntervaloVariavel()

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
                // C\u00e1lculo do custo em tempo real
                val custoCalculado = CharacterRules.calcularCustoVantagem(
                    definicaoId = definicao.id,
                    tipoCusto = definicao.tipoCusto,
                    custoBase = definicao.getCustoPorNivel().takeIf { it != 0 } ?: definicao.getCustoBase(),
                    custoEscolhido = custoEscolhido,
                    nivel = nivel,
                    modificadores = mods,
                    metadados = metadadosAtaque
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
                        Text("N\u00edvel:")
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
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        
                        // Configura\u00e7\u00f5es Espec??ficas para Leveled Traits
                        if (definicao.id == "atribulacao") {
                            AtribulacaoConfig(
                                modifiers = mods,
                                onAddModifier = { m -> mods = mods.toMutableList().apply { add(m) } },
                                descricaoContent = {
                                    OutlinedTextField(
                                        value = descricao,
                                        onValueChange = { descricao = it },
                                        label = { Text("Descri\u00e7\u00e3o/Especializa\u00e7\u00f5es") },
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
                                        label = { Text("Descri\u00e7\u00e3o/Especializa\u00e7\u00f5es") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            )
                        } else {
                            OutlinedTextField(
                                value = descricao,
                                onValueChange = { descricao = it },
                                label = { Text("Descri\u00e7\u00e3o/Especializa\u00e7\u00f5es") },
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
                        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descri\u00e7\u00e3o/Especializa\u00e7\u00f5e") }, modifier = Modifier.fillMaxWidth())
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
                                Text("Custo Vari\u00e1vel:")
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { custoEscolhido -= 1 }) { Text("-1") }
                                    Text("$custoEscolhido pts", fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { custoEscolhido += 1 }) { Text("+1") }
                                }
                            }
                        }
                        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descri\u00e7\u00e3o/Especializa\u00e7\u00f5e") }, modifier = Modifier.fillMaxWidth())
                    }
                    TipoCusto.FIXO -> {
                        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descri\u00e7\u00e3o/Especializa\u00e7\u00f5e") }, modifier = Modifier.fillMaxWidth())
                    }
                }


                // Se\u00e7\u00e3o de Modificadores (Novo!)
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
                onClick = { onSave(nivel, custoEscolhido, descricao, mods, metadadosAtaque) },
                enabled = definicao.id != "ataque_inato" || nomeAtaque.isNotBlank()
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
        AlertDialog(onDismissRequest = { mostrarDescricaoCatalogo = false }, title = { Text(definicao.nome) }, text = { Text(descricaoCatalogo.ifBlank { "Sem descri\u00e7\u00e3o dispon\u00edvel." }) },
            confirmButton = { TextButton(onClick = { mostrarDescricaoCatalogo = false }) { Text(UiActionLabels.FECHAR) } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorListaTraitsDialog(
    catalogo: List<TraitDefinicaoInterface>,
    titulo: String,
    onDismiss: () -> Unit,
    onSelect: (TraitDefinicaoInterface, Int) -> Unit
) {
    var busca by remember { mutableStateOf("") }
    var selecionado by remember { mutableStateOf<TraitDefinicaoInterface?>(null) }
    var level by remember { mutableStateOf(1) }
    
    val filtrada = catalogo.filter { it.nome.contains(busca, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (selecionado == null) {
                    OutlinedTextField(
                        value = busca,
                        onValueChange = { busca = it },
                        label = { Text("Buscar...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        items(filtrada) { trait ->
                            ListItem(
                                headlineContent = { Text(trait.nome, fontWeight = FontWeight.Bold) },
                                supportingContent = { Text("${trait.custo} pts") },
                                modifier = Modifier.clickable { 
                                    selecionado = trait 
                                    level = 1
                                }
                            )
                        }
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selecionado!!.nome, fontWeight = FontWeight.Bold)
                                Text("${selecionado!!.custo} pts", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { selecionado = null }) { Icon(Icons.Default.Delete, null) }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nivel Racial", style = MaterialTheme.typography.labelMedium)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            IconButton(onClick = { if(level > 1) level -= 1 }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                            Text("$level", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { level += 1 }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                        }
                    }
                    
                    val custoTotal = selecionado!!.custo * level
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Text("Custo Total: $custoTotal pontos", modifier = Modifier.padding(12.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selecionado != null,
                onClick = { selecionado?.let { onSelect(it, level) } }
            ) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

interface TraitDefinicaoInterface {
    val id: String
    val nome: String
    val custo: Int
}

class VantagemToTrait(val v: VantagemDefinicao) : TraitDefinicaoInterface {
    override val id = v.id
    override val nome = v.nome
    override val custo: Int = v.getCustoBase()
}

class DesvantagemToTrait(val d: DesvantagemDefinicao) : TraitDefinicaoInterface {
    override val id = d.id
    override val nome = d.nome
    override val custo: Int = d.getCustoBase()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarVantagemDialog(vantagem: VantagemSelecionada, descricaoCatalogo: String = "", onDismiss: () -> Unit, onSave: (VantagemSelecionada) -> Unit) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var nivel by remember { mutableStateOf(vantagem.nivel) }
    var custoEscolhido by remember { mutableStateOf(vantagem.custoEscolhido) }
    var descricao by remember { mutableStateOf(vantagem.descricao) }
    var mods by remember { mutableStateOf(vantagem.modificadores.toList()) }

    var showAddMod by remember { mutableStateOf(false) }
    var showSchoolPicker by remember { mutableStateOf(false) }
    var pendingModForSchool by remember { mutableStateOf<ModificadorDefinicao?>(null) }
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }
    val descricaoCatalogo = if (vantagem.definicaoId == "retencao") {
        "O personagem tem um ataque capaz de manter o alvo preso no lugar. O jogador deve descrever os efeitos do ataque quando comprar a vantagem. O personagem pode enroscar sua v\u00edtima com vinhas, prend\u00ea-la com teias, congel\u00e1-la num bloco de gelo, transformar o solo em areia movedi\u00e7a, etc. Se o ataque obtiver sucesso, a v\u00edtima fica agarrada (v. p\u00e1g. 370) e presa no lugar. Ela n\u00e3o pode utilizar as manobras Deslocamento e Mudan\u00e7a de Posi\u00e7\u00e3o nem mudar de dire\u00e7\u00e3o e sua DX sofre uma penalidade de -4. A ST desse efeito \u00e9 igual ao n\u00edvel de Reten\u00e7\u00e3o do agressor, mas ataques adicionais podem se acumular sobre a v\u00edtima retida. Cada nova \u201ccamada\u201d acrescenta um b\u00f4nus de +1 \u00e0 ST. Para se libertar, a v\u00edtima precisa vencer uma Disputa R\u00e1pida de ST, ou de Fuga, contra a ST empregada para ret\u00ea-la. Cada tentativa dura um segundo. Se n\u00e3o conseguir se libertar, o alvo perde 1 PF, mas pode tentar novamente. A v\u00edtima tamb\u00e9m pode tentar destruir o que quer que a esteja prendendo. Nesse caso, Ataques Inatos atingem automaticamente, enquanto outros ataques sofrem uma penalidade de -4. Ataques externos n\u00e3o sofrem essa penalidade, mas se fracassarem, podem ferir a v\u00edtima retida (v. Intervendo em um Combate Corporal, p\u00e1g. 392). A Reten\u00e7\u00e3o tem uma RD de 1/3 do seu n\u00edvel (arredondado para baixo). Cada ponto de dano causado reduz a ST em um ponto. Quando a ST chegar a zero, a Reten\u00e7\u00e3o \u00e9 destru\u00edda e a v\u00edtima libertada."
    } else descricaoCatalogo

    // Estados para Ataque Inato
    var nomeAtaque by remember { mutableStateOf(vantagem.metadados?.get("nomePersonalizado") ?: vantagem.nome) }
    var tipoDanoAtaque by remember { mutableStateOf(vantagem.metadados?.get("tipoDano") ?: "cont") }
    var dadosAtaque by remember { mutableStateOf(vantagem.metadados?.get("dice")?.toIntOrNull() ?: 1) }
    var bonusAtaque by remember { mutableStateOf(vantagem.metadados?.get("bonus")?.toIntOrNull() ?: 0) }

    val metadadosAtaque = if (vantagem.definicaoId == "ataque_inato") {
        mapOf(
            "tipoDano" to tipoDanoAtaque,
            "dice" to dadosAtaque.toString(),
            "bonus" to bonusAtaque.toString(),
            "nomePersonalizado" to nomeAtaque
        )
    } else null

    val def = remember { CharacterRules.DATA_REPOSITORY_INSTANCE?.getVantagemPorId(vantagem.definicaoId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(vantagem.nome, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { if (descricaoCatalogo.isNotBlank()) mostrarDescricaoCatalogo = true })
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                val custoCalculado = CharacterRules.calcularCustoVantagem(
                    definicaoId = vantagem.definicaoId,
                    tipoCusto = vantagem.tipoCusto,
                    custoBase = def?.getCustoPorNivel() ?: vantagem.custoBase,
                    custoEscolhido = custoEscolhido,
                    nivel = nivel,
                    modificadores = mods,
                    metadados = metadadosAtaque
                )
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text("Custo: $custoCalculado pts", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (vantagem.tipoCusto == TipoCusto.POR_NIVEL) {
                    Text("N\u00edvel:")
                    val nivelMinimo = 1
                    val nivelMaximo = if (vantagemEhAptidaoMagica(vantagem.definicaoId)) 11 else 20
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { if (nivel > nivelMinimo) nivel-- }) { Text("-") }
                        Text("${nivelExibicaoVantagem(vantagem.definicaoId, nivel)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { if (nivel < nivelMaximo) nivel++ }) { Text("+") }
                    }
                    
                    if (vantagem.definicaoId == "atribulacao") {
                        AtribulacaoConfig(
                            modifiers = mods,
                            onAddModifier = { m -> mods = mods.toMutableList().apply { add(m) } },
                            descricaoContent = {
                                OutlinedTextField(
                                    value = descricao,
                                    onValueChange = { descricao = it },
                                    label = { Text("Descri\u00e7\u00e3o") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        )
                    } else if (vantagem.definicaoId == "retencao") {
                        RetencaoConfig(
                            modifiers = mods,
                            onAddModifier = { m -> mods = mods.toMutableList().apply { add(m) } },
                            descricaoContent = {
                                OutlinedTextField(
                                    value = descricao,
                                    onValueChange = { descricao = it },
                                    label = { Text("Descri\u00e7\u00e3o") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        )
                    } else {
                        OutlinedTextField(
                            value = descricao,
                            onValueChange = { descricao = it },
                            label = { Text("Descri\u00e7\u00e3o") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (vantagem.tipoCusto == TipoCusto.VARIAVEL) {
                    // Para vantagens vari??veis, o custoEscolhido j?? est?? sendo rastreado
                    // e atualizado via LaunchedEffect na ConfigurarVantagemDialog.
                    // Aqui, apenas exibimos o custo e permitimos a descri????o.
                    Text("Custo Vari\u00e1vel: $custoEscolhido pts")
                    if (vantagem.definicaoId == "ataque_inato") {
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
                    OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descri\u00e7\u00e3o") }, modifier = Modifier.fillMaxWidth())
                } else if (vantagem.tipoCusto == TipoCusto.ESCOLHA) {
                    // Para vantagens de escolha, o custoEscolhido j?? est?? definido.
                    // Apenas exibimos e permitimos a descri????o.
                    Text("Custo Escolhido: $custoEscolhido pts")
                    OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descri\u00e7\u00e3o") }, modifier = Modifier.fillMaxWidth())
                } else { // TipoCusto.FIXO
                    OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descri\u00e7\u00e3o") }, modifier = Modifier.fillMaxWidth())
                }


                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Modificadores (%)", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { showAddMod = true }) { Icon(Icons.Default.Add, null); Text("Add") }
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
            TextButton(onClick = {
                onSave(vantagem.copy(nivel = nivel, custoEscolhido = custoEscolhido, descricao = descricao, modificadores = mods.toMutableList(), metadados = metadadosAtaque))
            }) { Text(UiActionLabels.SALVAR) }
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

    if (mostrarDescricaoCatalogo) {
        AlertDialog(onDismissRequest = { mostrarDescricaoCatalogo = false }, title = { Text(vantagem.nome) }, text = { Text(descricaoCatalogo.ifBlank { "Sem descri\u00e7\u00e3o dispon\u00edvel." }) },
            confirmButton = { TextButton(onClick = { mostrarDescricaoCatalogo = false }) { Text(UiActionLabels.FECHAR) } })
    }
}

// === DESVANTAGENS ===

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

    // Estados para Inimigos e Dependencia
    var enemyBasePower by remember { mutableStateOf(-5) }
    var enemyIntention by remember { mutableStateOf(1.0f) }
    var enemyFrequency by remember { mutableStateOf(1.0f) }
    
    var depRarity by remember { mutableStateOf(-5) }
    var depFrequency by remember { mutableStateOf(1.0f) }
    var depIllegal by remember { mutableStateOf(false) }

    val metadados = when (definicao.specialRule) {
        "inimigos" -> mapOf(
            "basePoder" to enemyBasePower.toString(),
            "multIntencao" to enemyIntention.toString(),
            "multFrequencia" to enemyFrequency.toString()
        )
        "dependencia" -> mapOf(
            "baseRaridade" to depRarity.toString(),
            "multFrequencia" to depFrequency.toString(),
            "ilegal" to depIllegal.toString()
        )
        else -> null
    }

    val permiteAutocontrole = definicao.usaAutocontroleMental()

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
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                val custoCalculado = CharacterRules.calcularCustoDesvantagem(
                    definicao.tipoCusto, 
                    definicao.getCustoPorNivel().takeIf { it != 0 } ?: definicao.getCustoBase(), 
                    custoEscolhido, 
                    nivel, 
                    autocontrole, 
                    mods, 
                    definicao.specialRule, 
                    metadados
                )
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("Custo: $custoCalculado pts", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (definicao.specialRule == "inimigos") {
                    Text("Poder do Inimigo:", style = MaterialTheme.typography.labelMedium)
                    val inimigoBaseOptions = listOf(
                        -5 to "Individual (50% pts)",
                        -10 to "Indiv. (100%) / Grupo (3-5)",
                        -20 to "Indiv. (150%) / Grupo (6-20)",
                        -30 to "Grupo (21-1000)",
                        -40 to "Governo"
                    )
                    inimigoBaseOptions.forEach { (pts, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { enemyBasePower = pts }) {
                            RadioButton(selected = enemyBasePower == pts, onClick = { enemyBasePower = pts })
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    Text("Intenção:", style = MaterialTheme.typography.labelMedium)
                    val intencaoOptions = listOf(
                        0.25f to "Observador (x1/4)",
                        0.5f to "Rival (x1/2)",
                        1.0f to "Perseguidor (x1)"
                    )
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        intencaoOptions.forEach { (m, label) ->
                            FilterChip(selected = enemyIntention == m, onClick = { enemyIntention = m }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }

                    Text("Frequência:", style = MaterialTheme.typography.labelMedium)
                    val freqOptions = listOf(
                        0.5f to "6- (x1/2)",
                        1.0f to "9- (x1)",
                        2.0f to "12- (x2)",
                        3.0f to "15- (x3)",
                        4.0f to "Constante (x4)"
                    )
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        freqOptions.forEach { (m, label) ->
                            FilterChip(selected = enemyFrequency == m, onClick = { enemyFrequency = m }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }
                    
                    HorizontalDivider()
                } else if (definicao.specialRule == "dependencia") {
                    Text("Raridade:", style = MaterialTheme.typography.labelMedium)
                    val depRarityOptions = listOf(
                        -5 to "Muito Comum",
                        -10 to "Comum",
                        -20 to "Ocasional",
                        -30 to "Rara"
                    )
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        depRarityOptions.forEach { (pts, label) ->
                            FilterChip(selected = depRarity == pts, onClick = { depRarity = pts }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }

                    Text("Frequência:", style = MaterialTheme.typography.labelMedium)
                    val depFreqOptions = listOf(
                        0.10f to "Anual (x1/10)",
                        0.33f to "Trimestral (x1/3)",
                        1.0f to "Mensal (x1)",
                        2.0f to "Semanal (x2)",
                        3.0f to "Diária (x3)",
                        4.0f to "Hora em Hora (x4)",
                        5.0f to "Constante (x5)"
                    )
                    Column {
                        depFreqOptions.forEach { (m, label) ->
                             Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { depFrequency = m }) {
                                RadioButton(selected = depFrequency == m, onClick = { depFrequency = m })
                                Text(label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = depIllegal, onCheckedChange = { depIllegal = it })
                        Text("Substância Ilegal (-5 pts)")
                    }
                    HorizontalDivider()
                } else {
                    val opcoesEscolha = definicao.getOpcoesEscolha()

                    when (definicao.tipoCusto) {
                        TipoCusto.POR_NIVEL -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { if (nivel > 1) nivel-- }) { Text("-") }
                                Text("$nivel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { if (nivel < 10) nivel++ }) { Text("+") }
                            }
                        }
                        TipoCusto.ESCOLHA -> {
                            Text("Selecione o nível de custo:", style = MaterialTheme.typography.labelMedium)
                            opcoesEscolha.forEach { opcao ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { custoEscolhido = opcao }
                                ) {
                                    RadioButton(selected = custoEscolhido == opcao, onClick = { custoEscolhido = opcao })
                                    Text("$opcao pts", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                        TipoCusto.VARIAVEL -> {
                            Text("Custo Variável:", style = MaterialTheme.typography.labelMedium)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { custoEscolhido += 1 }) { Text("+1") }
                                Text("$custoEscolhido pts", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { custoEscolhido -= 1 }) { Text("-1") }
                            }
                            Text("Ajuste o custo final conforme a regra na descrição.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        else -> {}
                    }
                }

                if (permiteAutocontrole) {
                    HorizontalDivider()
                    Text("Autocontrole:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(null to "Nenhum", 6 to "6 (x2)", 9 to "9 (x1.5)", 12 to "12 (x1)", 15 to "15 (x0.5)").forEach { (valor, label) ->
                            FilterChip(selected = autocontrole == valor, onClick = { autocontrole = valor }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }
                }

                OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())

                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Modificadores (%)", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { showAddMod = true }) { Icon(Icons.Default.Add, null); Text("Add") }
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
            TextButton(onClick = { onSave(nivel, custoEscolhido, descricao, autocontrole, mods, metadados) }) { Text(UiActionLabels.ADICIONAR) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
    )

    if (showAddMod) {
        EscopoModificadoresDialog(
            especificos = definicao.modificadoresEspecificos ?: emptyList(),
            gerais = CharacterRules.DATA_REPOSITORY_INSTANCE?.modificadoresGerais ?: emptyList(),
            onDismiss = { showAddMod = false },
            onSelect = { modDef ->
                val valorInt = Regex("-?\\d+").find(modDef.valor)?.value?.toIntOrNull() ?: 0
                mods = mods.toMutableList().apply { add(ModificadorSelecao(modDef.id, modDef.nome, valorInt, modDef.porNivel, 1, modDef.descricao, modDef.pagina)) }
                showAddMod = false
            }
        )
    }

    if (mostrarDescricaoCatalogo) {
        AlertDialog(
            onDismissRequest = { mostrarDescricaoCatalogo = false },
            title = { Text(definicao.nome) },
            text = { Text(descricaoCatalogo.ifBlank { "Sem descrição disponível." }) },
            confirmButton = { TextButton(onClick = { mostrarDescricaoCatalogo = false }) { Text(UiActionLabels.FECHAR) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarDesvantagemDialog(desvantagem: DesvantagemSelecionada, descricaoCatalogo: String = "", onDismiss: () -> Unit, onSave: (DesvantagemSelecionada) -> Unit) {
    var nivel by remember { mutableStateOf(desvantagem.nivel) }
    var custoEscolhido by remember { mutableStateOf(desvantagem.custoEscolhido) }
    var descricao by remember { mutableStateOf(desvantagem.descricao) }
    var autocontrole by remember { mutableStateOf(desvantagem.autocontrole) }
    var mods by remember { mutableStateOf(desvantagem.modificadores.toList()) }
    var showAddMod by remember { mutableStateOf(false) }

    val def = remember { CharacterRules.DATA_REPOSITORY_INSTANCE?.getDesvantagemPorId(desvantagem.definicaoId) }
    val permiteAutocontrole = def?.usaAutocontroleMental() == true
    val specialRule = desvantagem.specialRule ?: def?.specialRule

    val descCatalogo = def?.descricao?.trim().orEmpty()
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }

    // Estados para Inimigos e Dependencia (inicializados com metadados existentes)
    var enemyBasePower by remember { 
        mutableStateOf(desvantagem.metadados?.get("basePoder")?.toIntOrNull() ?: -5) 
    }
    var enemyIntention by remember { 
        mutableStateOf(desvantagem.metadados?.get("multIntencao")?.toFloatOrNull() ?: 1.0f) 
    }
    var enemyFrequency by remember { 
        mutableStateOf(desvantagem.metadados?.get("multFrequencia")?.toFloatOrNull() ?: 1.0f) 
    }
    
    var depRarity by remember { 
        mutableStateOf(desvantagem.metadados?.get("baseRaridade")?.toIntOrNull() ?: -5) 
    }
    var depFrequency by remember { 
        mutableStateOf(desvantagem.metadados?.get("multFrequencia")?.toFloatOrNull() ?: 1.0f) 
    }
    var depIllegal by remember { 
        mutableStateOf(desvantagem.metadados?.get("ilegal")?.toBoolean() ?: false) 
    }

    val metadadosNovo = when (specialRule) {
        "inimigos" -> mapOf(
            "basePoder" to enemyBasePower.toString(),
            "multIntencao" to enemyIntention.toString(),
            "multFrequencia" to enemyFrequency.toString()
        )
        "dependencia" -> mapOf(
            "baseRaridade" to depRarity.toString(),
            "multFrequencia" to depFrequency.toString(),
            "ilegal" to depIllegal.toString()
        )
        else -> desvantagem.metadados
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                desvantagem.nome,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .let {
                        if (descCatalogo.isNotBlank()) {
                            it.clickable { mostrarDescricaoCatalogo = true }
                        } else {
                            it
                        }
                    }
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                val custoCalculado = CharacterRules.calcularCustoDesvantagem(
                    desvantagem.tipoCusto, 
                    def?.getCustoPorNivel() ?: desvantagem.custoBase, 
                    custoEscolhido, 
                    nivel, 
                    autocontrole, 
                    mods,
                    specialRule,
                    metadadosNovo
                )
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("Custo: $custoCalculado pts", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (specialRule == "inimigos") {
                    Text("Poder do Inimigo:", style = MaterialTheme.typography.labelMedium)
                    val inimigoBaseOptions = listOf(
                        -5 to "Individual (50% pts)",
                        -10 to "Indiv. (100%) / Grupo (3-5)",
                        -20 to "Indiv. (150%) / Grupo (6-20)",
                        -30 to "Grupo (21-1000)",
                        -40 to "Governo"
                    )
                    inimigoBaseOptions.forEach { (pts, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { enemyBasePower = pts }) {
                            RadioButton(selected = enemyBasePower == pts, onClick = { enemyBasePower = pts })
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    Text("Intenção:", style = MaterialTheme.typography.labelMedium)
                    val intencaoOptions = listOf(
                        0.25f to "Observador (x1/4)",
                        0.5f to "Rival (x1/2)",
                        1.0f to "Perseguidor (x1)"
                    )
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        intencaoOptions.forEach { (m, label) ->
                            FilterChip(selected = enemyIntention == m, onClick = { enemyIntention = m }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }

                    Text("Frequência:", style = MaterialTheme.typography.labelMedium)
                    val freqOptions = listOf(
                        0.5f to "6- (x1/2)",
                        1.0f to "9- (x1)",
                        2.0f to "12- (x2)",
                        3.0f to "15- (x3)",
                        4.0f to "Constante (x4)"
                    )
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        freqOptions.forEach { (m, label) ->
                            FilterChip(selected = enemyFrequency == m, onClick = { enemyFrequency = m }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }
                    HorizontalDivider()
                } else if (specialRule == "dependencia") {
                    Text("Raridade:", style = MaterialTheme.typography.labelMedium)
                    val depRarityOptions = listOf(
                        -5 to "Muito Comum",
                        -10 to "Comum",
                        -20 to "Ocasional",
                        -30 to "Rara"
                    )
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        depRarityOptions.forEach { (pts, label) ->
                            FilterChip(selected = depRarity == pts, onClick = { depRarity = pts }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }

                    Text("Frequência:", style = MaterialTheme.typography.labelMedium)
                    val depFreqOptions = listOf(
                        0.10f to "Anual (x1/10)",
                        0.33f to "Trimestral (x1/3)",
                        1.0f to "Mensal (x1)",
                        2.0f to "Semanal (x2)",
                        3.0f to "Diária (x3)",
                        4.0f to "Hora em Hora (x4)",
                        5.0f to "Constante (x5)"
                    )
                    Column {
                        depFreqOptions.forEach { (m, label) ->
                             Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { depFrequency = m }) {
                                RadioButton(selected = depFrequency == m, onClick = { depFrequency = m })
                                Text(label, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = depIllegal, onCheckedChange = { depIllegal = it })
                        Text("Substância Ilegal (-5 pts)")
                    }
                    HorizontalDivider()
                } else {
                    val opcoesEscolha = def?.getOpcoesEscolha() ?: emptyList()

                    when (desvantagem.tipoCusto) {
                        TipoCusto.POR_NIVEL -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { if (nivel > 1) nivel-- }) { Text("-") }
                                Text("$nivel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { if (nivel < 10) nivel++ }) { Text("+") }
                            }
                        }
                        TipoCusto.ESCOLHA -> {
                            Text("Selecione o nível de custo:", style = MaterialTheme.typography.labelMedium)
                            opcoesEscolha.forEach { opcao ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { custoEscolhido = opcao }
                                ) {
                                    RadioButton(selected = custoEscolhido == opcao, onClick = { custoEscolhido = opcao })
                                    Text("$opcao pts", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                        TipoCusto.VARIAVEL -> {
                            Text("Custo Variável:", style = MaterialTheme.typography.labelMedium)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { custoEscolhido += 1 }) { Text("+1") }
                                Text("$custoEscolhido pts", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { custoEscolhido -= 1 }) { Text("-1") }
                            }
                        }
                        else -> {}
                    }
                }

                if (permiteAutocontrole) {
                    HorizontalDivider()
                    Text("Autocontrole:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(null to "Nenhum", 6 to "6 (x2)", 9 to "9 (x1.5)", 12 to "12 (x1)", 15 to "15 (x0.5)").forEach { (valor, label) ->
                            FilterChip(selected = autocontrole == valor, onClick = { autocontrole = valor }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }
                }

                OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())

                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Modificadores (%)", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { showAddMod = true }) { Icon(Icons.Default.Add, null); Text("Add") }
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
            TextButton(onClick = {
                onSave(desvantagem.copy(
                    nivel = nivel, 
                    custoEscolhido = custoEscolhido, 
                    descricao = descricao, 
                    autocontrole = autocontrole, 
                    modificadores = mods.toMutableList(),
                    metadados = metadadosNovo,
                    specialRule = specialRule
                ))
            }) { Text(UiActionLabels.SALVAR) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
    )

    if (showAddMod) {
        EscopoModificadoresDialog(
            especificos = def?.modificadoresEspecificos ?: emptyList(),
            gerais = CharacterRules.DATA_REPOSITORY_INSTANCE?.modificadoresGerais ?: emptyList(),
            onDismiss = { showAddMod = false },
            onSelect = { modDef ->
                val valorInt = Regex("-?\\d+").find(modDef.valor)?.value?.toIntOrNull() ?: 0
                mods = mods.toMutableList().apply { add(ModificadorSelecao(modDef.id, modDef.nome, valorInt, modDef.porNivel, 1, modDef.descricao, modDef.pagina)) }
                showAddMod = false
            }
        )
    }

    if (mostrarDescricaoCatalogo) {
        AlertDialog(
            onDismissRequest = { mostrarDescricaoCatalogo = false },
            title = { Text(desvantagem.nome) },
            text = { Text(descCatalogo.ifBlank { "Sem descrição disponível." }) },
            confirmButton = { TextButton(onClick = { mostrarDescricaoCatalogo = false }) { Text(UiActionLabels.FECHAR) } }
        )
    }
}

// === COMPONENTES AUXILIARES ===

@Composable
fun EscopoModificadoresDialog(especificos: List<ModificadorDefinicao>, gerais: List<ModificadorDefinicao>, onDismiss: () -> Unit, onSelect: (ModificadorDefinicao) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Modificador") },
        text = {
            val especificosIds = especificos.map { it.id }.toSet()
            val geraisFiltrados = gerais.filter { it.id !in especificosIds }
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(300.dp)) {
                if (especificos.isNotEmpty()) {
                    item { Text("Espec\u00edficos desta caracter\u00edstica", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                    items(especificos) { mod -> ModificadorItemRow(mod) { onSelect(mod) } }
                }
                if (geraisFiltrados.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { Text("Gerais (Cat\u00e1logo)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                    items(geraisFiltrados) { mod -> ModificadorItemRow(mod) { onSelect(mod) } }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun SeletorEscolaMagiaDialog(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val escolas = listOf("Ar", "\u00c1gua", "Alimento", "Animais", "Arremesso", "Comunica\u00e7\u00e3o e Empatia", "Controle da Mente", "Corpo", "Cura", "Deslocamento", "Encantamentos", "Esculpir", "Fogo", "Ilus\u00e3o e Cria\u00e7\u00e3o", "Impregna\u00e7\u00e3o M\u00e1gica", "Inviabiliza\u00e7\u00e3o", "Luz e Trevas", "Meta-M\u00e1gicas", "Necromancia", "Planta", "Prote\u00e7\u00e3o e Advert\u00eancia", "Som", "Tecnomagia", "Terra").sorted()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecione a Escola") },
        text = {
            LazyColumn(modifier = Modifier.height(300.dp)) {
                items(escolas) { escola ->
                    TextButton(onClick = { onSelect(escola) }, modifier = Modifier.fillMaxWidth()) {
                        Text(escola, textAlign = androidx.compose.ui.text.style.TextAlign.Start, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun ModificadorItemRow(mod: ModificadorDefinicao, onClick: () -> Unit) {
    var mostrarDescricao by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(mod.nome, fontWeight = FontWeight.Bold)
                Text("${mod.tipo} | ${mod.valor}" + (if (mod.porNivel) " p/ n\u00edvel" else ""), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            if (mod.descricao?.isNotBlank() == true) {
                IconButton(onClick = { mostrarDescricao = true }) { Icon(Icons.Default.Info, null) }
            }
        }
    }
    if (mostrarDescricao) {
        AlertDialog(onDismissRequest = { mostrarDescricao = false }, title = { Text(mod.nome) }, text = { Text(mod.descricao ?: "") }, confirmButton = { TextButton(onClick = { mostrarDescricao = false }) { Text("Fechar") } })
    }
}

@Composable
fun PeculiaridadeDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var texto by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Adicionar Peculiaridade") }, text = { Column { Text("Peculiaridades s\u00e3o mini-desvantagens (-1 pt cada, m\u00e1x 5)", style = MaterialTheme.typography.bodySmall); Spacer(modifier = Modifier.height(8.dp)); OutlinedTextField(value = texto, onValueChange = { texto = it }, label = { Text("Peculiaridade") }, modifier = Modifier.fillMaxWidth()) } }, confirmButton = { TextButton(onClick = { if (texto.isNotBlank()) onSave(texto) }) { Text(UiActionLabels.ADICIONAR) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } })
}

@Composable
fun QualidadeDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var texto by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Adicionar Qualidade") }, text = { Column { Text("Qualidades s\u00e3o tra\u00e7os positivos (+1 pt cada, m\u00e1x 5)", style = MaterialTheme.typography.bodySmall); Spacer(modifier = Modifier.height(8.dp)); OutlinedTextField(value = texto, onValueChange = { texto = it }, label = { Text("Qualidade") }, modifier = Modifier.fillMaxWidth()) } }, confirmButton = { TextButton(onClick = { if (texto.isNotBlank()) onSave(texto) }) { Text(UiActionLabels.ADICIONAR) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } })
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
            Text("Per\u00edcia (NH $currentNh)", style = MaterialTheme.typography.labelLarge)
        }

        // 2. Frequ\u00eancia
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
            Text("Frequ\u00eancia de Aparecimento ($freqLabel)", style = MaterialTheme.typography.labelLarge)
        }

        // 3. Confiabilidade
        Button(
            onClick = { showConfList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Confiabilidade", style = MaterialTheme.typography.labelLarge)
        }

        // Di\u00e1logos
        if (showNhList) {
            SeletorNhContatoDialog(
                current = currentNh,
                onDismiss = { showNhList = false },
                onSelect = { onChanged(it, currentFreq, currentConf); showNhList = false }
            )
        }
        if (showFreqList) {
            SeletorFrequenciaAparecimentoDialog(
                current = currentFreq,
                onDismiss = { showFreqList = false },
                onSelect = { onChanged(currentNh, it, currentConf); showFreqList = false }
            )
        }
        if (showConfList) {
            SeletorConfiabilidadeDialog(
                current = currentConf,
                onDismiss = { showConfList = false },
                onSelect = { onChanged(currentNh, currentFreq, it); showConfList = false }
            )
        }
    }
}

@Composable
fun AliadosConfig(
    currentRatio: Int,
    currentFreq: Float,
    currentGroup: Int,
    onChanged: (Int, Float, Int) -> Unit
) {
    var showRatioList by remember { mutableStateOf(false) }
    var showFreqList by remember { mutableStateOf(false) }
    var showGroupList by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { showRatioList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Poder do Aliado", style = MaterialTheme.typography.labelLarge)
        }

        Button(
            onClick = { showFreqList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Frequ\u00eancia de Aparecimento", style = MaterialTheme.typography.labelLarge)
        }

        Button(
            onClick = { showGroupList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Tamanho do Grupo", style = MaterialTheme.typography.labelLarge)
        }

        // Di\u00e1logos
        if (showRatioList) {
            SeletorPoderAliadoDialog(
                current = currentRatio,
                onDismiss = { showRatioList = false },
                onSelect = { onChanged(it, currentFreq, currentGroup); showRatioList = false }
            )
        }
        if (showFreqList) {
            SeletorFrequenciaAparecimentoDialog(
                current = currentFreq,
                onDismiss = { showFreqList = false },
                onSelect = { onChanged(currentRatio, it, currentGroup); showFreqList = false }
            )
        }
        if (showGroupList) {
            SeletorGrupoAliadoDialog(
                current = currentGroup,
                onDismiss = { showGroupList = false },
                onSelect = { onChanged(currentRatio, currentFreq, it); showGroupList = false }
            )
        }
    }
}

@Composable
fun PatronosConfig(
    currentPower: Int,
    currentFreq: Float,
    currentMod: Float,
    isSecret: Boolean,
    onChanged: (Int, Float, Float, Boolean) -> Unit
) {
    var showPowerList by remember { mutableStateOf(false) }
    var showFreqList by remember { mutableStateOf(false) }
    var showModList by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { showPowerList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Poder do Patrono", style = MaterialTheme.typography.labelLarge)
        }

        Button(
            onClick = { showFreqList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Frequ\u00eancia de Aparecimento", style = MaterialTheme.typography.labelLarge)
        }

        Button(
            onClick = { showModList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Modificadores Especiais", style = MaterialTheme.typography.labelLarge)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { onChanged(currentPower, currentFreq, currentMod, !isSecret) },
            horizontalArrangement = Arrangement.Center
        ) {
            Checkbox(checked = isSecret, onCheckedChange = { onChanged(currentPower, currentFreq, currentMod, it) })
            Text("Patrono Secreto (-5 pts)", style = MaterialTheme.typography.bodyMedium)
        }

        // Di\u00e1logos
        if (showPowerList) {
            SeletorPoderPatronoDialog(
                current = currentPower,
                onDismiss = { showPowerList = false },
                onSelect = { onChanged(it, currentFreq, currentMod, isSecret); showPowerList = false }
            )
        }
        if (showFreqList) {
            SeletorFrequenciaAparecimentoDialog(
                current = currentFreq,
                onDismiss = { showFreqList = false },
                onSelect = { onChanged(currentPower, it, currentMod, isSecret); showFreqList = false }
            )
        }
        if (showModList) {
            SeletorModificadorPatronoDialog(
                current = currentMod,
                onDismiss = { showModList = false },
                onSelect = { onChanged(currentPower, currentFreq, it, isSecret); showModList = false }
            )
        }
    }
}

@Composable
fun FavorConfig(
    currentPower: Int,
    currentMod: Float,
    isSecret: Boolean,
    isContact: Boolean,
    onChanged: (Int, Float, Boolean, Boolean) -> Unit
) {
    var showTypeList by remember { mutableStateOf(false) }
    var showPowerList by remember { mutableStateOf(false) }
    var showModList by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { showTypeList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(if (isContact) "Tipo: Contato (1/5)" else "Tipo: Patrono (1/10)", style = MaterialTheme.typography.labelLarge)
        }

        Button(
            onClick = { showPowerList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(if (isContact) "Per\u00edcia do Contato" else "Poder do Patrono", style = MaterialTheme.typography.labelLarge)
        }

        Button(
            onClick = { showModList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Modificadores Especiais", style = MaterialTheme.typography.labelLarge)
        }

        if (!isContact) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onChanged(currentPower, currentMod, !isSecret, isContact) },
                horizontalArrangement = Arrangement.Center
            ) {
                Checkbox(checked = isSecret, onCheckedChange = { onChanged(currentPower, currentMod, it, isContact) })
                Text("Patrono Secreto (-5 pts)", style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (showTypeList) {
            SeletorTipoFavorDialog(
                isContact = isContact,
                onDismiss = { showTypeList = false },
                onSelect = { onChanged(currentPower, currentMod, isSecret, it); showTypeList = false }
            )
        }
        if (showPowerList) {
            if (isContact) {
                SeletorNhContatoDialog(
                    current = currentPower,
                    onDismiss = { showPowerList = false },
                    onSelect = { onChanged(it, currentMod, isSecret, isContact); showPowerList = false }
                )
            } else {
                SeletorPoderPatronoDialog(
                    current = currentPower,
                    onDismiss = { showPowerList = false },
                    onSelect = { onChanged(it, currentMod, isSecret, isContact); showPowerList = false }
                )
            }
        }
        if (showModList) {
            SeletorModificadorPatronoDialog(
                current = currentMod,
                onDismiss = { showModList = false },
                onSelect = { onChanged(currentPower, it, isSecret, isContact); showModList = false }
            )
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = nome,
            onValueChange = { onChanged(it, tipoDano, dados, bonus) },
            label = { AdaptiveText("Nome do Ataque (Obrigat\u00f3rio)", style = MaterialTheme.typography.labelMedium) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        AdaptiveText("Tipo de Dano:", style = MaterialTheme.typography.labelMedium)
        val tipos = listOf(
            "cont" to "Cont", "cor" to "Cor", "corte" to "Corte", "fad" to "Fad",
            "perf" to "Perf", "perfurante" to "Perfor", "pa" to "PA", "pa+" to "PA+", "pa++" to "PA++",
            "qmd" to "Qmd", "tox" to "Tox"
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tipos.forEach { (id, _) ->
                FilterChip(
                    selected = tipoDano == id,
                    onClick = { onChanged(nome, id, dados, bonus) },
                    label = { AdaptiveText(id.uppercase(), fontSize = 10.sp) },
                    modifier = Modifier.height(32.dp)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                AdaptiveText("Dados (1d):", style = MaterialTheme.typography.labelSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { if (dados > 1) onChanged(nome, tipoDano, (dados - 1), bonus) }) { Text("-1") }
                    AdaptiveText("${dados}d", fontWeight = FontWeight.Bold)
                    TextButton(onClick = { onChanged(nome, tipoDano, dados + 1, bonus) }) { Text("+1") }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                AdaptiveText("B\u00f4nus fixo:", style = MaterialTheme.typography.labelSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onChanged(nome, tipoDano, dados, bonus - 1) }) { Text("-1") }
                    AdaptiveText(if (bonus >= 0) "+$bonus" else "$bonus", fontWeight = FontWeight.Bold)
                    TextButton(onClick = { onChanged(nome, tipoDano, dados, bonus + 1) }) { Text("+1") }
                }
            }
        }
        
        val descFinal = "${dados}d${if(bonus > 0) "+$bonus" else if(bonus < 0) bonus else ""} ($tipoDano)"
        AdaptiveText("Dano Final: $descFinal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ModificadorSelecionadoItem(mod: ModificadorSelecao, onUpdate: (ModificadorSelecao) -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(mod.nome, fontWeight = FontWeight.Bold)
                if (mod.id == "mod_aptidao_escola" && !mod.descricao.isNullOrBlank()) Text("Escola: ${mod.descricao}", style = MaterialTheme.typography.labelSmall)
                Text("${if (mod.valor >= 0) "+" else ""}${mod.valor}%", style = MaterialTheme.typography.labelSmall)
                
                if (mod.porNivel) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("N\u00edvel: ${mod.niveis}", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { if (mod.niveis > 1) onUpdate(mod.copy(niveis = mod.niveis - 1)) }, modifier = Modifier.size(24.dp), contentPadding = PaddingValues(0.dp)) { Text("-", fontSize = 14.sp) }
                        TextButton(onClick = { onUpdate(mod.copy(niveis = mod.niveis + 1)) }, modifier = Modifier.size(24.dp), contentPadding = PaddingValues(0.dp)) { Text("+", fontSize = 14.sp) }
                    }
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AtribulacaoConfig(
    modifiers: List<ModificadorSelecao>,
    onAddModifier: (ModificadorSelecao) -> Unit,
    descricaoContent: @Composable () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val dataRepo = remember { DataRepository.getInstance(context) }
    
    var showCondList by remember { mutableStateOf(false) }
    var showVantList by remember { mutableStateOf(false) }
    var showDesvList by remember { mutableStateOf(false) }
    var showAtribList by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider()
        
        // 1. Descri\u00e7\u00e3o no topo (linear)
        descricaoContent()

        // 2. Bot\u00e3o Amplia\u00e7\u00f5es (abre lista de condi\u00e7\u00f5es)
        Button(
            onClick = { showCondList = true }, 
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Amplia\u00e7\u00f5es", style = MaterialTheme.typography.labelLarge)
        }
        
        // 3. Bot\u00e3o Vantagem (+10%/pt)
        Button(
            onClick = { showVantList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Vantagem (+10%)", style = MaterialTheme.typography.labelLarge)
        }
        
        // 4. Bot\u00e3o Desvantagem (+1%/pt)
        Button(
            onClick = { showDesvList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Desvantagem (+1%)", style = MaterialTheme.typography.labelLarge)
        }

        // 5. Bot\u00e3o Atributos (+5% por -1)
        Button(
            onClick = { showAtribList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Atributos", style = MaterialTheme.typography.labelLarge)
        }

        // Di\u00e1logos de Sele\u00e7\u00e3o
        if (showCondList) {
            SeletorCondicoesDialog(
                modifiers = modifiers,
                onDismiss = { showCondList = false },
                onAddModifier = { mod ->
                    onAddModifier(mod)
                    showCondList = false
                }
            )
        }

        if (showVantList) {
            SeletorListaTraitsDialog(
                catalogo = dataRepo.vantagens.map { v -> VantagemToTrait(v) },
                titulo = "Selecionar Vantagem (+10%/pt)",
                onDismiss = { showVantList = false },
                onSelect = { trait, level ->
                    val finalBonus = (trait.custo * level) * 10
                    onAddModifier(ModificadorSelecao("atrib_vant_${trait.id}", "Concede ${trait.nome} Lvl $level", finalBonus))
                    showVantList = false
                }
            )
        }

        if (showDesvList) {
            SeletorListaTraitsDialog(
                catalogo = dataRepo.desvantagens.map { d -> DesvantagemToTrait(d) },
                titulo = "Selecionar Desvantagem (+1%/pt)",
                onDismiss = { showDesvList = false },
                onSelect = { trait, level ->
                    val finalPenalidade = (trait.custo * level)
                    onAddModifier(ModificadorSelecao("atrib_desv_${trait.id}", "Imp\u00f5e ${trait.nome} Lvl $level", finalPenalidade))
                    showDesvList = false
                }
            )
        }

        if (showAtribList) {
            SeletorAtributoPenalidadeDialog(
                onDismiss = { showAtribList = false },
                onSelect = { nome, penalidade ->
                    onAddModifier(ModificadorSelecao("atrib_penal_$nome", "Penalidade -${penalidade} em $nome", penalidade * 5))
                    showAtribList = false
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SeletorCondicoesDialog(
    modifiers: List<ModificadorSelecao>,
    onDismiss: () -> Unit,
    onAddModifier: (ModificadorSelecao) -> Unit
) {
    val condicoes = listOf(
        "Ataque Card\u00edaco" to 300, "Coma" to 250, "Inconsci\u00eancia" to 200, "Paralisia" to 150, "Sono" to 150,
        "Agonia" to 100, "Dor Terr\u00edvel" to 60, "Dor Intensa" to 40, "N\u00e1usea" to 30, "Mudez" to 25, 
        "Surdez" to 20, "Cegueira" to 50, "Tossem" to 20, "Paranoia" to 10
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Condi\u00e7\u00e3o (Amplia\u00e7\u00e3o)") },
        text = {
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                condicoes.forEach { (nome, valor) ->
                    val jaTem = modifiers.any { it.nome == nome }
                    FilterChip(
                        selected = jaTem,
                        onClick = { if (!jaTem) onAddModifier(ModificadorSelecao("atrib_$nome", nome, valor)) },
                        label = { Text(nome) },
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeletorAtributoPenalidadeDialog(onDismiss: () -> Unit, onSelect: (String, Int) -> Unit) {
    var penalidade by remember { mutableStateOf(1) }
    val atributos = listOf("ST", "DX", "IQ", "HT", "Von", "Per")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Penalidade de Atributo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Selecione a penalidade:")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { if (penalidade > 1) penalidade-- }) { Text("-") }
                    Text("-$penalidade", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { penalidade++ }) { Text("+") }
                }
                HorizontalDivider()
                Text("Selecione o atributo:")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    atributos.forEach { atrib ->
                        AssistChip(onClick = { onSelect(atrib, penalidade) }, label = { Text(atrib) })
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RetencaoConfig(
    modifiers: List<ModificadorSelecao>,
    onAddModifier: (ModificadorSelecao) -> Unit,
    descricaoContent: @Composable () -> Unit = {}
) {
    var showAmpList by remember { mutableStateOf(false) }
    var showLimList by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider()
        
        // 1. Descri\u00e7\u00e3o no topo (linear)
        descricaoContent()

        // 2. Bot\u00e3o Amplia\u00e7\u00f5es
        Button(
            onClick = { showAmpList = true }, 
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Amplia\u00e7\u00f5es", style = MaterialTheme.typography.labelLarge)
        }
        
        // 3. Bot\u00e3o Limita\u00e7\u00f5es Especiais
        Button(
            onClick = { showLimList = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Limita\u00e7\u00f5es Especiais", style = MaterialTheme.typography.labelLarge)
        }

        // Di\u00e1logos
        if (showAmpList) {
            SeletorAmpliacaoRetencaoDialog(
                modifiers = modifiers,
                onDismiss = { showAmpList = false },
                onAddModifier = { mod ->
                    onAddModifier(mod)
                    showAmpList = false
                }
            )
        }

        if (showLimList) {
            SeletorLimitacaoRetencaoDialog(
                modifiers = modifiers,
                onDismiss = { showLimList = false },
                onAddModifier = { mod ->
                    onAddModifier(mod)
                    showLimList = false
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SeletorAmpliacaoRetencaoDialog(
    modifiers: List<ModificadorSelecao>,
    onDismiss: () -> Unit,
    onAddModifier: (ModificadorSelecao) -> Unit
) {
    var showDanoPicker by remember { mutableStateOf(false) }
    
    val ampliacoes = listOf(
        Triple("Engolfar", 60, "O ataque do personagem imobiliza o alvo. Ele n\u00e3o consegue mover os bra\u00e7os, nem as pernas, nem falar; a \u00fanica op\u00e7\u00e3o dele \u00e9 usar habilidades puramente mentais, utilizar Ataques Inatos ou tentar libertar-se usando a ST (n\u00e3o a per\u00edcia Fuga). Se tentar se libertar e n\u00e3o conseguir, a v\u00edtima poder\u00e1 fazer uma nova tentativa depois de 10 segundos, contudo, com um resultado de 17 ou 18, ela fica t\u00e3o enredada que n\u00e3o conseguir\u00e1 mais escapar sozinha!"),
        Triple("Grudento", 20, "A Reten\u00e7\u00e3o \u00e9 considerada Persistente (p\u00e1g. 108), mas afeta apenas aqueles que tocarem o alvo original do ataque."),
        Triple("Inquebr\u00e1vel", 40, "A Reten\u00e7\u00e3o n\u00e3o pode ser destru\u00edda. A \u00fanica maneira de escapar \u00e9 tentando se libertar com a ST.")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Amplia\u00e7\u00f5es de Reten\u00e7\u00e3o") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ampliacoes.forEach { (nome, valor, desc) ->
                    val jaTem = modifiers.any { it.nome == nome }
                    Card(
                        onClick = { if (!jaTem) onAddModifier(ModificadorSelecao("ret_$nome", nome, valor)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !jaTem
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(nome, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("+$valor%", style = MaterialTheme.typography.labelSmall)
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                // Op\u00e7\u00e3o Especial: S\u00f3 Sofre Dano
                Button(onClick = { showDanoPicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("S\u00f3 Sofre Dano (+10%/+20%/+30%)")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )

    if (showDanoPicker) {
        var selecionados by remember { mutableStateOf(setOf<String>()) }
        val tipos = listOf("Queimadura", "Corros\u00e3o", "Contus\u00e3o", "Corte")
        
        AlertDialog(
            onDismissRequest = { showDanoPicker = false },
            title = { Text("S\u00f3 Sofre Dano") },
            text = {
                Column {
                    tipos.forEach { tipo ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                            selecionados = if (selecionados.contains(tipo)) selecionados - tipo else selecionados + tipo
                        }) {
                            Checkbox(checked = selecionados.contains(tipo), onCheckedChange = null)
                            Text(tipo)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val count = selecionados.size.coerceIn(1, 3)
                    val custo = count * 10 // 1=10, 2=20, 3=30 (Pedido pelo USER)
                    onAddModifier(ModificadorSelecao("ret_dano", "S\u00f3 sofre dano (${selecionados.joinToString(", ")})", custo))
                    showDanoPicker = false
                    onDismiss()
                }, enabled = selecionados.isNotEmpty()) { Text("Adicionar (+${selecionados.size * 10}%)") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SeletorLimitacaoRetencaoDialog(
    modifiers: List<ModificadorSelecao>,
    onDismiss: () -> Unit,
    onAddModifier: (ModificadorSelecao) -> Unit
) {
    val limitacoes = listOf(
        Triple("Ambientais (-20%)", -20, "A Reten\u00e7\u00e3o manipula uma condi\u00e7\u00e3o ou objeto existente no ambiente e n\u00e3o funciona na aus\u00eancia dele"),
        Triple("Ambientais (-40%)", -40, "Se a v\u00edtima tiver que tocar o ch\u00e3o"),
        Triple("Ambientais (-30%)", -30, "Se a v\u00edtima tiver que estar em meio a vegeta\u00e7\u00e3o densa"),
        Triple("Chance \u00fanica", -10, "O personagem n\u00e3o pode sobrepor novas Reten\u00e7\u00f5es para aumentar a ST delas.")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Limita\u00e7\u00f5es Especiais") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                limitacoes.forEach { (nome, valor, desc) ->
                    val jaTem = modifiers.any { it.nome == nome }
                    Card(
                        onClick = { if (!jaTem) onAddModifier(ModificadorSelecao("ret_lim_$nome", nome, valor)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !jaTem
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(nome, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("$valor%", style = MaterialTheme.typography.labelSmall)
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@Composable
fun AdaptiveText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    textAlign: androidx.compose.ui.text.style.TextAlign? = null
) {
    BoxWithConstraints(modifier = modifier) {
        val finalFontSize = if (fontSize != androidx.compose.ui.unit.TextUnit.Unspecified) fontSize else style.fontSize
        // Reduz o tamanho da fonte se a largura for menor que 300dp (di\u00e1logo estreito no celular)
        val factor = if (maxWidth < 250.dp) 0.7f else if (maxWidth < 350.dp) 0.85f else 1.0f
        
        Text(
            text = text,
            style = style,
            color = color,
            fontWeight = fontWeight,
            fontSize = finalFontSize * factor,
            textAlign = textAlign,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorNhContatoDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val opcoes = listOf(
        Triple(12, "NH 12 (Boa forma????o)", "Possui um conhecimento justo e pr??tico da sua ??rea. Custo base: 1 pt."),
        Triple(15, "NH 15 (Especialista)", "Um profissional competente e respeitado. Custo base: 2 pts."),
        Triple(18, "NH 18 (Mestre)", "Destaque nacional em sua ??rea de expertise. Custo base: 3 pts."),
        Triple(21, "NH 21 (Renome Mundial)", "Uma das maiores autoridades vivas no assunto. Custo base: 4 pts.")
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("N??vel de Per??cia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                opcoes.forEach { (nh, titulo, desc) ->
                    Card(onClick = { onSelect(nh) }, modifier = Modifier.fillMaxWidth(),
                        colors = if (current == nh) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(desc, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorFrequenciaAparecimentoDialog(current: Float, onDismiss: () -> Unit, onSelect: (Float) -> Unit) {
    val opcoes = listOf(
        Triple(0.5f, "6 ou menos (Raramente)", "O tra??o est?? dispon??vel apenas em situa????es excepcionais. Multiplicador: x0.5."),
        Triple(1f, "9 ou menos (Ocasionalmente)", "Disponibilidade moderada ao longo da campanha. Multiplicador: x1."),
        Triple(2f, "12 ou menos (Frequentemente)", "Quase sempre dispon??vel quando solicitado. Multiplicador: x2."),
        Triple(3f, "15 ou menos (Quase sempre)", "Garante presen??a na maioria absoluta das sess??es. Multiplicador: x3.")
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Frequ??ncia de Aparecimento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                opcoes.forEach { (freq, titulo, desc) ->
                    Card(onClick = { onSelect(freq) }, modifier = Modifier.fillMaxWidth(),
                        colors = if (current == freq) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(desc, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorConfiabilidadeDialog(current: Float, onDismiss: () -> Unit, onSelect: (Float) -> Unit) {
    val opcoes = listOf(
        Triple(0.5f, "Pode Mentir (N??o confi??vel)", "O contato pode ser enganoso ou falhar propositalmente. Multiplicador: x0.5."),
        Triple(1f, "Um tanto Confi??vel", "Age com cautela, mas geralmente n??o mente para o PJ. Multiplicador: x1."),
        Triple(2f, "Geralmente Confi??vel", "Amigo leal que ajuda sem hesita????es comuns. Multiplicador: x2."),
        Triple(3f, "Totalmente Confi??vel", "Devo????o total; daria a vida pelo personagem. Multiplicador: x3.")
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confiabilidade") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                opcoes.forEach { (conf, titulo, desc) ->
                    Card(onClick = { onSelect(conf) }, modifier = Modifier.fillMaxWidth(),
                        colors = if (current == conf) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(desc, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorPoderAliadoDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val opcoes = listOf(
        Triple(1, "25% dos Pontos (Muito Fraco)", "O aliado ?? significativamente mais fraco que o PJ. Custo base: 1 pt."),
        Triple(2, "50% dos Pontos (Fraco)", "Possui metade da capacidade do personagem principal. Custo base: 2 pts."),
        Triple(3, "75% dos Pontos (Companheiro)", "Um parceiro pr??ximo em termos de poder. Custo base: 3 pts."),
        Triple(5, "100% dos Pontos (Equivalente)", "Mesmo n??vel de pontos que o personagem. Custo base: 5 pts."),
        Triple(10, "150% dos Pontos (Superior)", "O aliado ?? mais poderoso que o pr??prio PJ. Custo base: 10 pts.")
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Poder do Aliado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                opcoes.forEach { (pts, titulo, desc) ->
                    Card(onClick = { onSelect(pts) }, modifier = Modifier.fillMaxWidth(),
                        colors = if (current == pts) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(desc, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorGrupoAliadoDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val opcoes = listOf(
        Triple(1, "Apenas um", "Um ??nico aliado fiel. Multiplicador: x1."),
        Triple(6, "Grupo Pequeno (6-10)", "Uma pequena equipe ou esquadr??o. Multiplicador: x6."),
        Triple(8, "Grupo M??dio (11-20)", "Uma companhia ou grupo organizado. Multiplicador: x8."),
        Triple(10, "Grupo Grande (21-50)", "Uma tropa ou fac????o local. Multiplicador: x10."),
        Triple(12, "Batalh??o (51-100)", "Um grande contingente de subordinados. Multiplicador: x12.")
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tamanho do Grupo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                opcoes.forEach { (mult, titulo, desc) ->
                    Card(onClick = { onSelect(mult) }, modifier = Modifier.fillMaxWidth(),
                        colors = if (current == mult) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(desc, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorPoderPatronoDialog(current: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    val opcoes = listOf(
        Triple(5, "Individual (Muito Limitado)", "Um indiv??duo com recursos 10x ou 100x maiores que os iniciais. Custo base: 5 pts."),
        Triple(10, "Organiza????o Pequena", "Influ??ncia local, como uma pequena cidade ou grande empresa. Custo base: 10 pts."),
        Triple(15, "Organiza????o Regional/Nacional", "Recursos amplos sob comando (ex: FBI, grande igreja). Custo base: 15 pts."),
        Triple(20, "Superpot??ncia Mundial", "Influ??ncia multinacional ou governo de uma na????o poderosa. Custo base: 20 pts."),
        Triple(25, "Organiza????o Interestelar/Gal??ctica", "Dom??nio sobre m??ltiplos sistemas ou planetas. Custo base: 25 pts."),
        Triple(30, "Entidade Multidimensional/Divina", "Poder para moldar aspectos da realidade. Custo base: 30 pts.")
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Poder do Patrono") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                opcoes.forEach { (pts, titulo, desc) ->
                    Card(onClick = { onSelect(pts) }, modifier = Modifier.fillMaxWidth(),
                        colors = if (current == pts) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(desc, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorModificadorPatronoDialog(current: Float, onDismiss: () -> Unit, onSelect: (Float) -> Unit) {
    val opcoes = listOf(
        Triple(0.5f, "Interven????o M??nima", "O Patrono est?? ocupado ou n??o quer se envolver diretamente. Multiplicador: x0.5."),
        Triple(1.0f, "Interven????o Normal", "Suporte padr??o conforme as regras da vantagem. Multiplicador: x1."),
        Triple(1.5f, "Interven????o Muito Poderosa", "Recursos extensos e imunidade legal para o grupo. Multiplicador: x1.5."),
        Triple(2.0f, "Interven????o Extremamente Poderosa", "Pode mudar a vida do personagem completamente. Multiplicador: x2.")
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modificadores de Interven????o") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                opcoes.forEach { (mod, titulo, desc) ->
                    Card(onClick = { onSelect(mod) }, modifier = Modifier.fillMaxWidth(),
                        colors = if (current == mod) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(titulo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(desc, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorTipoFavorDialog(isContact: Boolean, onDismiss: () -> Unit, onSelect: (Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tipo de Favor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(onClick = { onSelect(false) }, modifier = Modifier.fillMaxWidth(),
                    colors = if (!isContact) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Favor de Patrono (1/10)", fontWeight = FontWeight.Bold)
                        Text("Um favor de uso ??nico de uma organiza????o ou indiv??duo poderoso.")
                    }
                }
                Card(onClick = { onSelect(true) }, modifier = Modifier.fillMaxWidth(),
                    colors = if (isContact) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Favor de Contato (1/5)", fontWeight = FontWeight.Bold)
                        Text("O favor de um indiv??duo especialista, usado apenas uma vez.")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ModeloRacialDialog(current: ModeloRacial, onDismiss: () -> Unit, onSave: (ModeloRacial) -> Unit) {
    var showCatalogo by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showPersonalizar by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("Modelo Racial") },
        text = {
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text("Raca Atual: ${current.nome}", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                
                com.gurps.ficha.ui.PrimaryActionButton(text = "Selecionar Raca do Catalogo", onClick = { showCatalogo = true })
                
                com.gurps.ficha.ui.PrimaryActionButton(text = "Personalizar Raca", onClick = { showPersonalizar = true })
                
                if (showPersonalizar) {
                    PersonalizarRacaDialog(
                        initial = current,
                        onDismiss = { showPersonalizar = false },
                        onSave = { r ->
                            onSave(r)
                            showPersonalizar = false
                        }
                    )
                }
                
                androidx.compose.material3.Card(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.padding(12.dp)) {
                        androidx.compose.material3.Text("Resumo do Modelo", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        androidx.compose.material3.Text("Custo: ${current.custoTotal} pontos")
                        if (current.modForca != 0) androidx.compose.material3.Text("ST ${if(current.modForca>0) "+" else ""}${current.modForca}")
                        if (current.modDestreza != 0) androidx.compose.material3.Text("DX ${if(current.modDestreza>0) "+" else ""}${current.modDestreza}")
                        if (current.modInteligencia != 0) androidx.compose.material3.Text("IQ ${if(current.modInteligencia>0) "+" else ""}${current.modInteligencia}")
                        if (current.modVitalidade != 0) androidx.compose.material3.Text("HT ${if(current.modVitalidade>0) "+" else ""}${current.modVitalidade}")
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { androidx.compose.material3.Text("Fechar") }
        }
    )

    if (showCatalogo) {
        SeletorRacaCatalogoDialog(
            onDismiss = { showCatalogo = false },
            onSelect = { r ->
                onSave(r)
                showCatalogo = false
            }
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SeletorRacaCatalogoDialog(onDismiss: () -> Unit, onSelect: (ModeloRacial) -> Unit) {
    val racas = listOf(
        ModeloRacial(nome = "Humano"),
        ModeloRacial(
            nome = "Anao",
            modVitalidade = 1,
            modDeslocamentoBasico = -1,
            vantagens = listOf(
                VantagemSelecionada(nome = "Resistencia a Venenos", custoBase = 5),
                VantagemSelecionada(nome = "Visao Noturna 5", custoBase = 5),
                VantagemSelecionada(nome = "Longevidade", custoBase = 2),
                VantagemSelecionada(nome = "Talento (Artifice) 1", custoBase = 10)
            )
        )
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text("Catalogo de Racas") },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = androidx.compose.ui.Modifier.heightIn(max = 400.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                items(racas.size) { index ->
                    val r = racas[index]
                    androidx.compose.material3.Card(
                        onClick = { onSelect(r) },
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.padding(12.dp)) {
                            androidx.compose.material3.Text(r.nome, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            androidx.compose.material3.Text("Custo: ${r.custoTotal} pts", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { androidx.compose.material3.Text("Cancelar") }
        }
    )
}

@Composable
fun PersonalizarRacaDialog(initial: ModeloRacial, onDismiss: () -> Unit, onSave: (ModeloRacial) -> Unit) {
    var nome by remember { mutableStateOf(initial.nome) }
    var modST by remember { mutableStateOf(initial.modForca) }
    var modDX by remember { mutableStateOf(initial.modDestreza) }
    var modIQ by remember { mutableStateOf(initial.modInteligencia) }
    var modHT by remember { mutableStateOf(initial.modVitalidade) }
    var modHP by remember { mutableStateOf(initial.modPontosVida) }
    var modVon by remember { mutableStateOf(initial.modVontade) }
    var modPer by remember { mutableStateOf(initial.modPercepcao) }
    var modPF by remember { mutableStateOf(initial.modPontosFadiga) }
    var modVB by remember { mutableStateOf(initial.modVelocidadeBasica) }
    var modDB by remember { mutableStateOf(initial.modDeslocamentoBasico) }
    var vantagensRacais by remember { mutableStateOf(initial.vantagens) }
    var desvantagensRacais by remember { mutableStateOf(initial.desvantagens) }
    var periciasRacais by remember { mutableStateOf(initial.pericias) }
    var descricaoRacial by remember { mutableStateOf(initial.descricao) }

    var showVantList by remember { mutableStateOf(false) }
    var showDesvList by remember { mutableStateOf(false) }
    var showPerList by remember { mutableStateOf(false) }
    val dataRepo = remember { CharacterRules.DATA_REPOSITORY_INSTANCE }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Personalizar Raca", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, 
                 style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) 
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    OutlinedTextField(
                        value = nome, 
                        onValueChange = { nome = it }, 
                        label = { Text("Nome da Raca") }, 
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = descricaoRacial, 
                        onValueChange = { descricaoRacial = it }, 
                        label = { Text("Descricao da Raca (Aparencia)") }, 
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
                
                // ATRIBUTOS
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Atributos Primarios", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                AjustadorVerticalRacial("ST", modST) { modST += it }
                                AjustadorVerticalRacial("DX", modDX) { modDX += it }
                                AjustadorVerticalRacial("IQ", modIQ) { modIQ += it }
                                AjustadorVerticalRacial("HT", modHT) { modHT += it }
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Atributos Secundarios", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                AjustadorVerticalRacial("PV", modHP) { modHP += it }
                                AjustadorVerticalRacial("Von", modVon) { modVon += it }
                                AjustadorVerticalRacial("Per", modPer) { modPer += it }
                                AjustadorVerticalRacial("PF", modPF) { modPF += it }
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Caracteristicas Derivadas", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("VEL. Basica", style = MaterialTheme.typography.labelSmall)
                                    IconButton(onClick = { modVB += 0.25f }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.KeyboardArrowUp, null) }
                                    Text(String.format("%.2f", modVB), fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { modVB -= 0.25f }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.KeyboardArrowDown, null) }
                                }
                                AjustadorVerticalRacial("Desloc.", modDB) { modDB += it }
                            }
                        }
                    }
                }

                item {
                    Text("Tracos Racais", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = { showVantList = true }, modifier = Modifier.fillMaxWidth(0.9f)) { Text("+ Vantagem") }
                        Button(onClick = { showDesvList = true }, modifier = Modifier.fillMaxWidth(0.9f)) { Text("+ Desvantagem") }
                        Button(onClick = { showPerList = true }, modifier = Modifier.fillMaxWidth(0.9f)) { Text("+ Pericia") }
                    }
                }

                items(vantagensRacais.size) { index ->
                    val t = vantagensRacais[index]
                    RacialTraitItem(t.nome, "${if(t.custoFinal>0) "+" else ""}${t.custoFinal} pts") { vantagensRacais = vantagensRacais.toMutableList().apply { removeAt(index) } }
                }

                items(desvantagensRacais.size) { index ->
                    val t = desvantagensRacais[index]
                    RacialTraitItem(t.nome, "${if(t.custoFinal>0) "+" else ""}${t.custoFinal} pts") { desvantagensRacais = desvantagensRacais.toMutableList().apply { removeAt(index) } }
                }

                items(periciasRacais.size) { index ->
                    val p = periciasRacais[index]
                    RacialTraitItem(p.nome, "${p.baseAtributo}${if(p.nivelRelativo>=0) "+" else ""}${p.nivelRelativo} [${if(p.custo>=0) "+" else ""}${p.custo} pts]") { periciasRacais = periciasRacais.toMutableList().apply { removeAt(index) } }
                }
                
                item {
                    val tempModelo = ModeloRacial(nome, modST, modDX, modIQ, modHT, modHP, modVon, modPer, modPF, modVB, modDB, vantagensRacais, desvantagensRacais, periciasRacais, descricaoRacial)
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Text("Resumo com b\u00f4nus racial (+1 lvl free): ${tempModelo.custoTotal} pontos", modifier = Modifier.padding(12.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(ModeloRacial(nome, modST, modDX, modIQ, modHT, modHP, modVon, modPer, modPF, modVB, modDB, vantagensRacais, desvantagensRacais, periciasRacais, descricaoRacial)) }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showVantList && dataRepo != null) {
        SeletorListaTraitsDialog(dataRepo.vantagens.map { VantagemToTrait(it) }, "Adicionar Vantagem", { showVantList = false }) { trait, level ->
            vantagensRacais = vantagensRacais + VantagemSelecionada(nome = trait.nome, definicaoId = trait.id, custoBase = trait.custo, nivel = level)
            showVantList = false
        }
    }
    if (showDesvList && dataRepo != null) {
        SeletorListaTraitsDialog(dataRepo.desvantagens.map { DesvantagemToTrait(it) }, "Adicionar Desvantagem", { showDesvList = false }) { trait, level ->
            desvantagensRacais = desvantagensRacais + DesvantagemSelecionada(nome = trait.nome, definicaoId = trait.id, custoBase = trait.custo, nivel = level)
            showDesvList = false
        }
    }
    if (showPerList) {
        PericiaRacialSeletorDialog({ showPerList = false }) { p -> 
            periciasRacais = periciasRacais + p
            showPerList = false
        }
    }
}

@Composable
fun RacialTraitItem(nome: String, ptsExibicao: String, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(0.95f)) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(nome, fontWeight = FontWeight.Bold)
                Text(ptsExibicao, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun AjustadorVerticalRacial(rotulo: String, valor: Int, onDelta: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(rotulo, style = MaterialTheme.typography.labelSmall)
        IconButton(onClick = { onDelta(1) }, modifier = Modifier.size(32.dp).semantics { contentDescription = "Aumentar $rotulo" }) { Icon(Icons.Default.KeyboardArrowUp, null) }
        Text("${if(valor>0) "+" else ""}$valor", fontWeight = FontWeight.Bold, modifier = Modifier.semantics { contentDescription = "Valor de $rotulo: $valor" })
        IconButton(onClick = { onDelta(-1) }, modifier = Modifier.size(32.dp).semantics { contentDescription = "Diminuir $rotulo" }) { Icon(Icons.Default.KeyboardArrowDown, null) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PericiaRacialSeletorDialog(onDismiss: () -> Unit, onSave: (PericiaRacial) -> Unit) {
    var busca by remember { mutableStateOf("") }
    var periciaSelecionada by remember { mutableStateOf<PericiaDefinicao?>(null) }
    var level by remember { mutableStateOf(1) } // Grau no Ranking de Custo GURPS (0=1pt, 1=2pt, 2=4pt...)
    
    val dataRepo = remember { CharacterRules.DATA_REPOSITORY_INSTANCE }
    val listFiltrada = remember(busca) { dataRepo?.filtrarPericias(busca) ?: emptyList() }

    // GURPS OFFICIAL RACIAL SKILL COST TABLE (1, 2, 4, 8, 12, 16...)
    // Conforme p\u00e1g. 260+ e exemplos do usuário: 2 pts = DX+1 para Médio
    fun getCustoTabela(uiLvl: Int): Int {
        val r = abs(uiLvl)
        val v = when {
            r == 0 -> 1
            r == 1 -> 2
            r == 2 -> 4
            r == 3 -> 8
            r >= 4 -> 8 + (r - 3) * 4
            else -> 0
        }
        return if (uiLvl < 0) -v else v
    }

    // Calcula o nível relativo incluindo o bônus racial de +1 para perícias raciais
    fun getRelativeDisplay(p: PericiaDefinicao, uiLvl: Int): Int {
        val diffOffset = when(p.dificuldadeFixa?.uppercase()) {
            "F" -> 0
            "M" -> -1
            "D" -> -2
            "VH", "MD" -> -3
            else -> -1
        }
        // Cada grau na escala de custo (uiLvl) aumenta o nível do personagem.
        // O Bônus Racial de +1 é o que faz '2 pts = DX+1' para dificuldade Médio.
        // Sem o bônus, Rank 1 (2 pts) para Médio seria Atrib+0. Com bônus vira Atrib+1.
        return diffOffset + uiLvl + 1
    }

    val finalCost = getCustoTabela(level)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Pericia Racial", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (periciaSelecionada == null) {
                    OutlinedTextField(value = busca, onValueChange = { busca = it }, label = { Text("Buscar Pericia...") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(listFiltrada) { p ->
                            ListItem(headlineContent = { Text(p.nome, fontWeight = FontWeight.Bold) }, 
                                     supportingContent = { Text("${p.atributoBase}/${p.dificuldadeFixa ?: "M"}") },
                                     modifier = Modifier.clickable { periciaSelecionada = p })
                        }
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(periciaSelecionada!!.nome, fontWeight = FontWeight.Bold)
                                Text("${periciaSelecionada!!.atributoBase}/${periciaSelecionada!!.dificuldadeFixa ?: "M"}", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { periciaSelecionada = null }) { Icon(Icons.Default.Delete, null) }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Bonus/Nivel Racial", style = MaterialTheme.typography.labelSmall)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            IconButton(onClick = { level-- }, modifier = Modifier.semantics { contentDescription = "Diminuir nível racial de perícia" }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                            val rel = getRelativeDisplay(periciaSelecionada!!, level)
                            Text("${periciaSelecionada!!.atributoBase}${if(rel>=0) "+" else ""}$rel", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { contentDescription = "Nível final da perícia: ${periciaSelecionada!!.atributoBase}${if(rel>=0) "+" else ""}$rel" })
                            IconButton(onClick = { level++ }, modifier = Modifier.semantics { contentDescription = "Aumentar nível racial de perícia" }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                        }
                        Text("${if(level>=0) "+" else ""}$level grau(s) na escala de pontos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0F0))) {
                        Text("Custo: $finalCost pontos", modifier = Modifier.padding(12.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color(0xFF880E4F))
                    }
                    Text("* Inclui b\u00f4nus racial de +1 para per\u00edcias nativas", style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        },
        confirmButton = {
            TextButton(enabled = periciaSelecionada != null, onClick = {
                periciaSelecionada?.let { p ->
                    val rel = getRelativeDisplay(p, level)
                    onSave(PericiaRacial(p.nome, p.dificuldadeFixa ?: "M", p.atributoBase, rel, finalCost))
                }
            }) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

