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
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
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
fun SelecionarVantagemDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: ((VantagemSelecionada) -> Unit)? = null
) {
    var busca by remember { mutableStateOf("") }
    var filtroTipo by remember { mutableStateOf<TipoCusto?>(null) }
    var filtroTag by remember { mutableStateOf<String?>(null) }
    var vantagemSelecionada by remember { mutableStateOf<VantagemDefinicao?>(null) }
    val contextForToast = LocalContext.current

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
            onSave = { nivel, custoEscolhido, desc, mods, metadados ->
                val novaVantagem = VantagemSelecionada(
                    definicaoId = definicao.id,
                    nome = definicao.nome,
                    custoBase = if (definicao.tipoCusto == com.gurps.ficha.model.TipoCusto.POR_NIVEL) definicao.getCustoPorNivel() else definicao.getCustoBase(),
                    nivel = nivel,
                    custoEscolhido = custoEscolhido,
                    descricao = desc,
                    tipoCusto = definicao.tipoCusto,
                    pagina = definicao.pagina,
                    specialRule = definicao.specialRule,
                    modificadores = mods,
                    metadados = metadados
                )
                val context = contextForToast
                if (onSelect != null) {
                    onSelect(novaVantagem)
                } else {
                    val erro = viewModel.adicionarVantagem(definicao, nivel, custoEscolhido, desc, mods, metadados)
                    if (erro != null) {
                        Toast.makeText(context, erro, Toast.LENGTH_SHORT).show()
                    }
                }
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
    var tipoGarras by remember { mutableStateOf("afiadas") }
    var tipoTelecomunicacao by remember { mutableStateOf("radio") }
    var tipoAparar by remember { mutableStateOf("global") }
    var periciaAparar by remember { mutableStateOf("desarmado") }

    // Estados para Mestre de Armas
    var classMestre by remember { mutableStateOf("todas") }
    var periciasMestre by remember { mutableStateOf("") }

    // Estados para Habilidades Modulares
    var selecoesHabMod by remember { mutableStateOf(mapOf<String, HabModTipoSel>()) }

    // Estados para Resistente
    var raridadeResistente by remember { mutableStateOf(10) } // Ocasional (Default)
    var grauResistente by remember { mutableStateOf(1f) } // Imunidade (Default)
    var atributoResistente by remember { mutableStateOf("HT") }

    val metadados = when (definicao.id) {
        "mestre_de_armas" -> mapOf("classId" to classMestre, "pericias_cobertas" to periciasMestre)
        "ataque_inato", "golpeadores" -> mapOf(
            "tipoDano" to tipoDanoAtaque,
            "dice" to dadosAtaque.toString(),
            "bonus" to bonusAtaque.toString(),
            "nomePersonalizado" to nomeAtaque
        )
        "dentes" -> mapOf("tipoDentes" to tipoDentes)
        "garras" -> mapOf("tipoGarras" to tipoGarras)
        "telecomunicacao" -> mapOf("tipoTelecomunicacao" to tipoTelecomunicacao)
        "defesas_ampliadas_aparar_ampliado" -> mapOf("tipo" to tipoAparar, "skillId" to periciaAparar)
        "resistente" -> mapOf(
            "raridade" to raridadeResistente.toString(),
            "grau" to grauResistente.toString(),
            "atributo" to atributoResistente
        )
        "habilidades_modulares" -> selecoesHabMod.entries.filter { it.value.ativo }.associate { e -> "habmod_${e.key}" to e.value.niveis.toString() }
        else -> null
    }

    // Sincronização de custos especiais
    LaunchedEffect(definicao.id, freqAliado, ratioAliado, grupoAliado, nhContato, freqContato, confContato, powerPatrono, freqPatrono, modPatrono, secretoPatrono, powerFavor, modFavor, secretoFavor, isContactFavor, tipoGarras, tipoTelecomunicacao, tipoAparar, raridadeResistente, grauResistente, selecoesHabMod) {
        when (definicao.id) {
            "aliados" -> custoEscolhido = CharacterRules.calcularCustoAliado(ratioAliado, freqAliado, grupoAliado)
            "contatos" -> custoEscolhido = CharacterRules.calcularCustoContato(nhContato, freqContato, confContato)
            "patronos" -> custoEscolhido = CharacterRules.calcularCustoPatrono(powerPatrono, freqPatrono, modPatrono, if (secretoPatrono) -5 else 0)
            "favor" -> custoEscolhido = CharacterRules.calcularCustoFavor(powerFavor, modFavor, if (secretoFavor) -5 else 0, isContactFavor)
            "garras" -> {
                custoEscolhido = when (tipoGarras) {
                    "cascos" -> 3
                    "cegas" -> 3
                    "afiadas" -> 5
                    "pontudas" -> 8
                    "longas_pontudas" -> 11
                    else -> 5
                }
            }
            "telecomunicacao" -> {
                custoEscolhido = when (tipoTelecomunicacao) {
                    "laser" -> 15
                    "diapsiquia" -> 30
                    "radio" -> 10
                    else -> 10
                }
            }
            "defesas_ampliadas_aparar_ampliado" -> {
                custoEscolhido = if (tipoAparar == "global") 10 else 5
            }
            "mestre_de_armas" -> {
                custoEscolhido = when (classMestre) {
                    "todas" -> 45
                    "amp_laminas", "amp_uma_mao" -> 40
                    "int_espadas", "int_ninja" -> 35
                    "peq_esgrima", "peq_cavaleiro" -> 30
                    "set_two" -> 25
                    "single" -> 20
                    else -> 45
                }
            }
            "resistente" -> {
                custoEscolhido = CharacterRules.calcularCustoResistente(raridadeResistente, grauResistente)
            }
            "habilidades_modulares" -> {
                custoEscolhido = CharacterRules.calcularCustoHabilidadesModulares(selecoesHabMod)
            }
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
                    custoBase = if (definicao.tipoCusto == com.gurps.ficha.model.TipoCusto.POR_NIVEL) definicao.getCustoPorNivel() else definicao.getCustoBase(),
                    custoEscolhido = custoEscolhido,
                    nivel = nivel,
                    modificadores = mods,
                    specialRule = definicao.specialRule,
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
                        if (definicao.id == "dentes") {
                            DentesConfig(currentType = tipoDentes, onChanged = { tipoDentes = it })
                        } else if (definicao.id == "garras") {
                            GarrasConfig(currentType = tipoGarras, onChanged = { tipoGarras = it })
                        } else if (definicao.id == "telecomunicacao") {
                            TelecomunicacaoConfig(currentType = tipoTelecomunicacao, onChanged = { tipoTelecomunicacao = it })
                        } else if (definicao.id == "defesas_ampliadas_aparar_ampliado") {
                            ApararAmpliadoConfig(
                                currentType = tipoAparar,
                                currentSkill = periciaAparar,
                                onChanged = { t, s -> tipoAparar = t; periciaAparar = s }
                            )
                        } else if (definicao.id == "mestre_de_armas") {
                            MestreDeArmasConfig(
                                currentClass = classMestre,
                                currentSkills = periciasMestre,
                                onChanged = { c, s -> classMestre = c; periciasMestre = s }
                            )
                        } else {
                            opcoesEscolha.forEach { opcao ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { custoEscolhido = opcao }) {
                                    RadioButton(selected = custoEscolhido == opcao, onClick = { custoEscolhido = opcao })
                                    Text("$opcao pts")
                                }
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
                            "garras" -> {
                                GarrasConfig(
                                    currentType = tipoGarras,
                                    onChanged = { tipoGarras = it }
                                )
                            }
                            "telecomunicacao" -> {
                                TelecomunicacaoConfig(
                                    currentType = tipoTelecomunicacao,
                                    onChanged = { tipoTelecomunicacao = it }
                                )
                            }
                            "defesas_ampliadas_aparar_ampliado" -> {
                                ApararAmpliadoConfig(
                                    currentType = tipoAparar,
                                    currentSkill = periciaAparar,
                                    onChanged = { t, s -> tipoAparar = t; periciaAparar = s }
                                )
                            }
                            "mestre_de_armas" -> {
                                MestreDeArmasConfig(
                                    currentClass = classMestre,
                                    currentSkills = periciasMestre,
                                    onChanged = { c, s -> classMestre = c; periciasMestre = s }
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
                            "resistente" -> {
                                ResistenteConfig(
                                    currentRarity = raridadeResistente,
                                    currentDegree = grauResistente,
                                    currentAttr = atributoResistente,
                                    onChanged = { r, g, a -> raridadeResistente = r; grauResistente = g; atributoResistente = a }
                                )
                            }
                            "habilidades_modulares" -> {
                                HabilidadesModularesConfig(
                                    selecoes = selecoesHabMod,
                                    onChanged = { selecoesHabMod = it }
                                )
                            }
                            else -> {
                                val (minCusto, maxCusto) = definicao.getIntervaloVariavel()
                                Text("Custo Variável:")
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { custoEscolhido = (custoEscolhido - 1).coerceIn(minCusto, maxCusto) }, enabled = custoEscolhido > minCusto) { Text("-1") }
                                    Text("$custoEscolhido pts", fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { custoEscolhido = (custoEscolhido + 1).coerceIn(minCusto, maxCusto) }, enabled = custoEscolhido < maxCusto) { Text("+1") }
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
                    mods = mods.toMutableList().apply { add(ModificadorSelecao(modDef.id, modDef.nome, valorInt, modDef.porNivel, 1, modDef.descricao, modDef.pagina, bonusBase = modDef.bonusBase)) }
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
        CatalogoDescricaoDialog(
            nome = definicao.nome,
            descricao = descricaoCatalogo,
            onDismiss = { mostrarDescricaoCatalogo = false }
        )
    }
}

@Composable
fun EditarVantagemDialog(
    vantagem: VantagemSelecionada,
    descricaoCatalogo: String = "",
    weaponSuggestions: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (VantagemSelecionada) -> Unit
) {
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
    var tipoGarras by remember { mutableStateOf(vantagem.metadados?.get("tipoGarras") ?: "afiadas") }
    var tipoTelecomunicacao by remember { mutableStateOf(vantagem.metadados?.get("tipoTelecomunicacao") ?: "radio") }
    var tipoAparar by remember { mutableStateOf(vantagem.metadados?.get("tipo") ?: "global") }
    var periciaAparar by remember { mutableStateOf(vantagem.metadados?.get("skillId") ?: "desarmado") }

    // Estados para Mestre de Armas
    var classMestre by remember { mutableStateOf(vantagem.metadados?.get("classId") ?: "todas") }
    var periciasMestre by remember { mutableStateOf(vantagem.metadados?.get("pericias_cobertas") ?: "") }

    // Estados para Resistente (Edição)
    var raridadeResistente by remember { mutableStateOf(vantagem.metadados?.get("raridade")?.toIntOrNull() ?: 10) }
    var grauResistente by remember { mutableStateOf(vantagem.metadados?.get("grau")?.toFloatOrNull() ?: 1f) }

    // Estados para Habilidades Modulares (Edição) — reconstrói do metadados salvo
    var selecoesHabMod by remember {
        val inicial: Map<String, HabModTipoSel> = vantagem.metadados
            ?.entries
            ?.filter { it.key.startsWith("habmod_") }
            ?.associate { entry ->
                entry.key.removePrefix("habmod_") to HabModTipoSel(ativo = true, niveis = entry.value.toIntOrNull() ?: 1)
            } ?: emptyMap()
        mutableStateOf(inicial)
    }
    var atributoResistente by remember { mutableStateOf(vantagem.metadados?.get("atributo") ?: "HT") }

    var freqAliado by remember { mutableStateOf(vantagem.metadados?.get("multFrequencia")?.toFloatOrNull() ?: 1f) }
    var ratioAliado by remember { mutableStateOf(vantagem.metadados?.get("basePoder")?.toIntOrNull() ?: 5) }
    var grupoAliado by remember { mutableStateOf(vantagem.metadados?.get("multGrupo")?.toIntOrNull() ?: 1) }

    var nhContato by remember { mutableStateOf(vantagem.metadados?.get("nhContato")?.toIntOrNull() ?: 12) }
    var freqContato by remember { mutableStateOf(vantagem.metadados?.get("multFrequencia")?.toFloatOrNull() ?: 1f) }
    var confContato by remember { mutableStateOf(vantagem.metadados?.get("multConfiabilidade")?.toFloatOrNull() ?: 1f) }

    var powerPatrono by remember { mutableStateOf(vantagem.metadados?.get("basePoder")?.toIntOrNull() ?: 10) }
    var freqPatrono by remember { mutableStateOf(vantagem.metadados?.get("multFrequencia")?.toFloatOrNull() ?: 1f) }
    var modPatrono by remember { mutableStateOf(vantagem.metadados?.get("multModificador")?.toFloatOrNull() ?: 1.0f) }
    var secretoPatrono by remember { mutableStateOf(vantagem.metadados?.get("bonusSecreto")?.toIntOrNull() == -5) }

    var powerFavor by remember { mutableStateOf(vantagem.metadados?.get("basePoder")?.toIntOrNull() ?: 10) }
    var modFavor by remember { mutableStateOf(vantagem.metadados?.get("multModificador")?.toFloatOrNull() ?: 1.0f) }
    var secretoFavor by remember { mutableStateOf(vantagem.metadados?.get("bonusSecreto")?.toIntOrNull() == -5) }
    var isContactFavor by remember { mutableStateOf(vantagem.metadados?.get("isContact")?.toBoolean() ?: false) }

    // Sincronização de custos para Editar
    LaunchedEffect(vantagem.definicaoId, freqAliado, ratioAliado, grupoAliado, nhContato, freqContato, confContato, powerPatrono, freqPatrono, modPatrono, secretoPatrono, powerFavor, modFavor, secretoFavor, isContactFavor, tipoGarras, tipoTelecomunicacao, tipoAparar, classMestre, raridadeResistente, grauResistente, selecoesHabMod) {
        when (vantagem.definicaoId) {
            "aliados" -> custoEscolhido = CharacterRules.calcularCustoAliado(ratioAliado, freqAliado, grupoAliado)
            "contatos" -> custoEscolhido = CharacterRules.calcularCustoContato(nhContato, freqContato, confContato)
            "patronos" -> custoEscolhido = CharacterRules.calcularCustoPatrono(powerPatrono, freqPatrono, modPatrono, if (secretoPatrono) -5 else 0)
            "favor" -> custoEscolhido = CharacterRules.calcularCustoFavor(powerFavor, modFavor, if (secretoFavor) -5 else 0, isContactFavor)
            "garras" -> {
                custoEscolhido = when (tipoGarras) {
                    "cascos" -> 3
                    "cegas" -> 3
                    "afiadas" -> 5
                    "pontudas" -> 8
                    "longas_pontudas" -> 11
                    else -> 5
                }
            }
            "telecomunicacao" -> {
                custoEscolhido = when (tipoTelecomunicacao) {
                    "laser" -> 15
                    "diapsiquia" -> 30
                    "radio" -> 10
                    else -> 10
                }
            }
            "defesas_ampliadas_aparar_ampliado" -> {
                custoEscolhido = if (tipoAparar == "global") 10 else 5
            }
            "mestre_de_armas" -> {
                custoEscolhido = when (classMestre) {
                    "todas" -> 45
                    "amp_classe" -> 40
                    "int_classe" -> 35
                    "peq_classe" -> 30
                    "set_two" -> 25
                    "single" -> 20
                    else -> 45
                }
            }
            "resistente" -> {
                custoEscolhido = CharacterRules.calcularCustoResistente(raridadeResistente, grauResistente)
            }
            "habilidades_modulares" -> {
                custoEscolhido = CharacterRules.calcularCustoHabilidadesModulares(
                    selecoesHabMod.entries.filter { it.value.ativo }.associate { e -> "habmod_${e.key}" to e.value.niveis.toString() }
                )
            }
        }
    }

    val metadados = when (vantagem.definicaoId) {
        "aliados" -> mapOf("basePoder" to ratioAliado.toString(), "multFrequencia" to freqAliado.toString(), "multGrupo" to grupoAliado.toString())
        "contatos" -> mapOf("nhContato" to nhContato.toString(), "multFrequencia" to freqContato.toString(), "multConfiabilidade" to confContato.toString())
        "patronos" -> mapOf("basePoder" to powerPatrono.toString(), "multFrequencia" to freqPatrono.toString(), "multModificador" to modPatrono.toString(), "bonusSecreto" to (if (secretoPatrono) "-5" else "0"))
        "favor" -> mapOf("basePoder" to powerFavor.toString(), "multModificador" to modFavor.toString(), "bonusSecreto" to (if (secretoFavor) "-5" else "0"), "isContact" to isContactFavor.toString())
        "mestre_de_armas" -> mapOf("classId" to classMestre, "pericias_cobertas" to periciasMestre)
        "ataque_inato", "golpeadores" -> mapOf(
            "tipoDano" to tipoDanoAtaque,
            "dice" to dadosAtaque.toString(),
            "bonus" to bonusAtaque.toString(),
            "nomePersonalizado" to nomeAtaque
        )
        "dentes" -> mapOf("tipoDentes" to tipoDentes)
        "garras" -> mapOf("tipoGarras" to tipoGarras)
        "telecomunicacao" -> mapOf("tipoTelecomunicacao" to tipoTelecomunicacao)
        "defesas_ampliadas_aparar_ampliado" -> mapOf("tipo" to tipoAparar, "skillId" to periciaAparar)
        "resistente" -> mapOf(
            "raridade" to raridadeResistente.toString(),
            "grau" to grauResistente.toString(),
            "atributo" to atributoResistente
        )
        "habilidades_modulares" -> selecoesHabMod.entries.filter { it.value.ativo }.associate { e -> "habmod_${e.key}" to e.value.niveis.toString() }
        else -> null
    }

    val def = remember { CharacterRules.DATA_REPOSITORY_INSTANCE?.getVantagemPorId(vantagem.definicaoId) }
    // specialRule: tenta na instância salva, depois no catálogo, depois infere pelo próprio id da vantagem
    val specialRule = vantagem.specialRule
        ?: def?.specialRule
        ?: CharacterRules.DATA_REPOSITORY_INSTANCE?.getVantagemPorId(vantagem.definicaoId)?.specialRule
        ?: vantagem.definicaoId

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
                    specialRule = specialRule,
                    metadados = metadados
                )
                
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text("Custo: $custoCalculado pts", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Text("Tipo: ${vantagem.tipoCusto.name} | Custo base: ${def?.custo ?: vantagem.custoBase} | Pag. ${def?.pagina ?: vantagem.pagina}", style = MaterialTheme.typography.bodySmall)
                
                if (vantagem.tipoCusto == TipoCusto.POR_NIVEL) {
                    Text("Nível:")
                    val nivelMinimo = 1
                    val nivelMaximo = if (vantagemEhAptidaoMagica(vantagem.definicaoId)) 11 else 20
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { if (nivel > nivelMinimo) nivel-- }) { Text("-") }
                        Text("${nivelExibicaoVantagem(vantagem.definicaoId, nivel)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { if (nivel < nivelMaximo) nivel++ }) { Text("+") }
                    }
                } else if (vantagem.tipoCusto == TipoCusto.ESCOLHA) {
                    val opcoes = def?.custo?.split(" ou ")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
                    if (opcoes.isNotEmpty()) {
                        opcoes.forEach { opcao ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { custoEscolhido = opcao }) {
                                RadioButton(selected = custoEscolhido == opcao, onClick = { custoEscolhido = opcao })
                                Text("$opcao pts")
                            }
                        }
                    }
                }

                // Regras Especiais (UI Compartilhada)
                when (specialRule) {
                    "aliados" -> AliadosConfig(currentRatio = ratioAliado, currentFreq = freqAliado, currentGroup = grupoAliado, onChanged = { r, f, g -> ratioAliado = r; freqAliado = f; grupoAliado = g })
                    "contatos" -> ContatosConfig(currentNh = nhContato, currentFreq = freqContato, currentConf = confContato, onChanged = { h, f, c -> nhContato = h; freqContato = f; confContato = c })
                    "patronos" -> PatronosConfig(currentPower = powerPatrono, currentFreq = freqPatrono, currentMod = modPatrono, isSecret = secretoPatrono, onChanged = { p, f, m, s -> powerPatrono = p; freqPatrono = f; modPatrono = m; secretoPatrono = s })
                    "favor" -> FavorConfig(currentPower = powerFavor, currentMod = modFavor, isSecret = secretoFavor, isContact = isContactFavor, onChanged = { p, m, s, c -> powerFavor = p; modFavor = m; secretoFavor = s; isContactFavor = c })
                    "resistente" -> ResistenteConfig(currentRarity = raridadeResistente, currentDegree = grauResistente, currentAttr = atributoResistente, onChanged = { r, g, a -> raridadeResistente = r; grauResistente = g; atributoResistente = a })
                    "ataque_inato", "golpeadores" -> AtaqueInatoConfig(nome = nomeAtaque, tipoDano = tipoDanoAtaque, dados = dadosAtaque, bonus = bonusAtaque, onChanged = { n, t, d, b -> nomeAtaque = n; tipoDanoAtaque = t; dadosAtaque = d; bonusAtaque = b })
                    "dentes" -> DentesConfig(currentType = tipoDentes, onChanged = { tipoDentes = it })
                    "garras" -> GarrasConfig(currentType = tipoGarras, onChanged = { tipoGarras = it })
                    "telecomunicacao" -> TelecomunicacaoConfig(currentType = tipoTelecomunicacao, onChanged = { tipoTelecomunicacao = it })
                    "defesas_ampliadas_aparar_ampliado" -> ApararAmpliadoConfig(currentType = tipoAparar, currentSkill = periciaAparar, onChanged = { t, s -> tipoAparar = t; periciaAparar = s })
                    "mestre_de_armas" -> MestreDeArmasConfig(currentClass = classMestre, currentSkills = periciasMestre, onChanged = { c, s -> classMestre = c; periciasMestre = s })
                    "habilidades_modulares" -> HabilidadesModularesConfig(selecoes = selecoesHabMod, onChanged = { selecoesHabMod = it })
                }

                OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())


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
                    mods = mods.toMutableList().apply { add(ModificadorSelecao(modDef.id, modDef.nome, valorInt, modDef.porNivel, 1, modDef.descricao, modDef.pagina, bonusBase = modDef.bonusBase)) }
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
        CatalogoDescricaoDialog(
            nome = vantagem.nome,
            descricao = descricaoCatalogoFinal,
            onDismiss = { mostrarDescricaoCatalogo = false }
        )
    }
}

