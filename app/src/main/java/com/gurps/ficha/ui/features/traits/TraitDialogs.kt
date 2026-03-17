package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
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
    LaunchedEffect(definicao.id, freqAliado, ratioAliado, grupoAliado, nhContato, freqContato, confContato, powerPatrono, freqPatrono, modPatrono) {
        when (definicao.id) {
            "aliados" -> custoEscolhido = CharacterRules.calcularCustoAliado(ratioAliado, freqAliado, grupoAliado)
            "contatos" -> custoEscolhido = CharacterRules.calcularCustoContato(nhContato, freqContato, confContato)
            "patronos" -> custoEscolhido = CharacterRules.calcularCustoPatrono(powerPatrono, freqPatrono, modPatrono)
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
                        
                        // Configura\u00e7\u00f5es Específicas para Leveled Traits
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
                                    onChanged = { p, f, m -> powerPatrono = p; freqPatrono = f; modPatrono = m }
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
                Divider()
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
    onSelect: (TraitDefinicaoInterface) -> Unit
) {
    var busca by remember { mutableStateOf("") }
    val filtrada = catalogo.filter { it.nome.contains(busca, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    label = { Text("Buscar...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtrada) { trait ->
                        ListItem(
                            headlineContent = { Text(trait.nome) },
                            supportingContent = { Text("${trait.custo} pts") },
                            modifier = Modifier.clickable { onSelect(trait) }
                        )
                        Divider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
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
                    // Para vantagens variáveis, o custoEscolhido já está sendo rastreado
                    // e atualizado via LaunchedEffect na ConfigurarVantagemDialog.
                    // Aqui, apenas exibimos o custo e permitimos a descrição.
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
                    // Para vantagens de escolha, o custoEscolhido já está definido.
                    // Apenas exibimos e permitimos a descrição.
                    Text("Custo Escolhido: $custoEscolhido pts")
                    OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descri\u00e7\u00e3o") }, modifier = Modifier.fillMaxWidth())
                } else { // TipoCusto.FIXO
                    OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descri\u00e7\u00e3o") }, modifier = Modifier.fillMaxWidth())
                }


                Divider()
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
            onSave = { nivel, custoEscolhido, descricao, autocontrole, mods ->
                viewModel.adicionarDesvantagem(definicao, nivel, custoEscolhido, descricao, autocontrole, mods)
                desvantagemSelecionada = null
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarDesvantagemDialog(definicao: DesvantagemDefinicao, onDismiss: () -> Unit, onSave: (Int, Int, String, Int?, List<ModificadorSelecao>) -> Unit) {
    var nivel by remember { mutableStateOf(1) }
    var custoEscolhido by remember { mutableStateOf(definicao.getCustoBase()) }
    var descricao by remember { mutableStateOf("") }
    val descricaoCatalogo = definicao.descricao?.trim().orEmpty()
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }
    var autocontrole by remember { mutableStateOf<Int?>(null) }
    var mods by remember { mutableStateOf(emptyList<ModificadorSelecao>()) }
    var showAddMod by remember { mutableStateOf(false) }

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
                val custoCalculado = CharacterRules.calcularCustoDesvantagem(definicao.tipoCusto, definicao.getCustoPorNivel().takeIf { it != 0 } ?: definicao.getCustoBase(), custoEscolhido, nivel, autocontrole, mods)
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("Custo: $custoCalculado pts", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (definicao.tipoCusto == TipoCusto.POR_NIVEL) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { if (nivel > 1) nivel-- }) { Text("-") }
                        Text("$nivel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { if (nivel < 10) nivel++ }) { Text("+") }
                    }
                }

                if (permiteAutocontrole) {
                    Divider()
                    Text("Autocontrole:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(null to "Nenhum", 6 to "6 (x2)", 9 to "9 (x1.5)", 12 to "12 (x1)", 15 to "15 (x0.5)").forEach { (valor, label) ->
                            FilterChip(selected = autocontrole == valor, onClick = { autocontrole = valor }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }
                }

                OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descri\u00e7\u00e3o") }, modifier = Modifier.fillMaxWidth())

                Divider()
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
            TextButton(onClick = { onSave(nivel, custoEscolhido, descricao, autocontrole, mods) }) { Text(UiActionLabels.ADICIONAR) }
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
            text = { Text(descricaoCatalogo.ifBlank { "Sem descri\u00e7\u00e3o dispon\u00edvel." }) },
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

    val descCatalogo = def?.descricao?.trim().orEmpty()
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }

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
                val custoCalculado = CharacterRules.calcularCustoDesvantagem(desvantagem.tipoCusto, def?.getCustoPorNivel() ?: desvantagem.custoBase, custoEscolhido, nivel, autocontrole, mods)
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("Custo: $custoCalculado pts", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (desvantagem.tipoCusto == TipoCusto.POR_NIVEL) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { if (nivel > 1) nivel-- }) { Text("-") }
                        Text("$nivel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { if (nivel < 10) nivel++ }) { Text("+") }
                    }
                }

                if (permiteAutocontrole) {
                    Divider()
                    Text("Autocontrole:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(null to "Nenhum", 6 to "6 (x2)", 9 to "9 (x1.5)", 12 to "12 (x1)", 15 to "15 (x0.5)").forEach { (valor, label) ->
                            FilterChip(selected = autocontrole == valor, onClick = { autocontrole = valor }, label = { Text(label, fontSize = 10.sp) })
                        }
                    }
                }

                OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descri\u00e7\u00e3o") }, modifier = Modifier.fillMaxWidth())

                Divider()
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
                onSave(desvantagem.copy(nivel = nivel, custoEscolhido = custoEscolhido, descricao = descricao, autocontrole = autocontrole, modificadores = mods.toMutableList()))
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
            text = { Text(descCatalogo.ifBlank { "Sem descri\u00e7\u00e3o dispon\u00edvel." }) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContatosConfig(
    currentNh: Int,
    currentFreq: Float,
    currentConf: Float,
    onChanged: (Int, Float, Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Per\u00edcia Efetiva do Contato:", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(12 to 1, 15 to 2, 18 to 3, 21 to 4).forEach { (nh, pts) ->
                FilterChip(
                    selected = currentNh == nh,
                    onClick = { onChanged(nh, currentFreq, currentConf) },
                    label = { Text("NH $nh (${pts}pts)") },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        Text("Frequ\u00eancia de Aparecimento:", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(0.5f to "6- (x0.5)", 1f to "9- (x1)", 2f to "12- (x2)", 3f to "15- (x3)").forEach { (f, label) ->
                FilterChip(
                    selected = currentFreq == f,
                    onClick = { onChanged(currentNh, f, currentConf) },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        Text("Confiabilidade:", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(0.5f to "N\u00e3o (x0.5)", 1f to "Meio (x1)", 2f to "Geral. (x2)", 3f to "Total (x3)").forEach { (c, label) ->
                FilterChip(
                    selected = currentConf == c,
                    onClick = { onChanged(currentNh, currentFreq, c) },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliadosConfig(
    currentRatio: Int,
    currentFreq: Float,
    currentGroup: Int,
    onChanged: (Int, Float, Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Pontos do Aliado (% do PJ):", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(1 to "25% (1)", 2 to "50% (2)", 3 to "75% (3)", 5 to "100% (5)", 10 to "150% (10)").forEach { (v, label) ->
                FilterChip(
                    selected = currentRatio == v,
                    onClick = { onChanged(v, currentFreq, currentGroup) },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        Text("Frequ\u00eancia de Aparecimento:", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(0.5f to "6- (x0.5)", 1f to "9- (x1)", 2f to "12- (x2)", 3f to "15- (x3)").forEach { (f, label) ->
                FilterChip(
                    selected = currentFreq == f,
                    onClick = { onChanged(currentRatio, f, currentGroup) },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        Text("Tamanho do Grupo:", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(1 to "Um (x1)", 6 to "6-10 (x6)", 8 to "11-20 (x8)", 10 to "21-50 (x10)", 12 to "51-100 (x12)").forEach { (g, label) ->
                FilterChip(
                    selected = currentGroup == g,
                    onClick = { onChanged(currentRatio, currentFreq, g) },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatronosConfig(
    currentPower: Int,
    currentFreq: Float,
    currentMod: Float,
    onChanged: (Int, Float, Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Poder do Patrono:", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(5 to "Indiv. (5)", 10 to "Org. Peq (10)", 15 to "Org. Gde (15)", 20 to "Mundo (20)", 25 to "Interstel. (25)", 30 to "Multi (30)").forEach { (p, label) ->
                FilterChip(
                    selected = currentPower == p,
                    onClick = { onChanged(p, currentFreq, currentMod) },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        Text("Frequ\u00eancia de Aparecimento:", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(0.5f to "6- (x0.5)", 1f to "9- (x1)", 2f to "12- (x2)", 3f to "15- (x3)").forEach { (f, label) ->
                FilterChip(
                    selected = currentFreq == f,
                    onClick = { onChanged(currentPower, f, currentMod) },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        Text("Modificadores:", style = MaterialTheme.typography.labelMedium)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(0.5f to "Interv. M\u00ednima (x0.5)", 1f to "Normal (x1)", 1.5f to "Muito Poderoso (x1.5)", 2f to "Extrem. Poderoso (x2)").forEach { (m, label) ->
                FilterChip(
                    selected = currentMod == m,
                    onClick = { onChanged(currentPower, currentFreq, m) },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
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
        Divider()
        
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
                onSelect = { trait ->
                    onAddModifier(ModificadorSelecao("atrib_vant_${trait.id}", "Concede ${trait.nome}", trait.custo * 10))
                    showVantList = false
                }
            )
        }

        if (showDesvList) {
            SeletorListaTraitsDialog(
                catalogo = dataRepo.desvantagens.map { d -> DesvantagemToTrait(d) },
                titulo = "Selecionar Desvantagem (+1%/pt)",
                onDismiss = { showDesvList = false },
                onSelect = { trait ->
                    onAddModifier(ModificadorSelecao("atrib_desv_${trait.id}", "Imp\u00f5e ${trait.nome}", trait.custo))
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
                Divider()
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
        Divider()
        
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
