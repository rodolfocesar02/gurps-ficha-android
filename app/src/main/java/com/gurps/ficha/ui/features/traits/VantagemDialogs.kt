package com.gurps.ficha.ui.features.traits

import com.gurps.ficha.domain.rules.TetoDeNivelDoTraco
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
import com.gurps.ficha.ui.rolagemVertical
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.UiTokens
import com.gurps.ficha.ui.AppSelectionDialog
import com.gurps.ficha.ui.AppSelectionRow
import com.gurps.ficha.ui.AppFiltroChip
import com.gurps.ficha.ui.contadorDe
import com.gurps.ficha.viewmodel.FichaViewModel
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.domain.rules.traits.BracalRuleBase
import com.gurps.ficha.domain.rules.traits.ControleRule
import com.gurps.ficha.domain.rules.traits.CriarRule
import com.gurps.ficha.domain.rules.traits.ElementoRuleBase
import com.gurps.ficha.domain.rules.traits.regraDeElementoDe
import com.gurps.ficha.domain.rules.traits.DxBracalRule
import com.gurps.ficha.domain.rules.traits.StBracalRule
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

    // Lote LAYOUT-4: a moldura inteira vem do padrão. O `Card + Row` escrito à
    // mão que morava aqui era uma das seis cópias que fizeram os diálogos
    // divergirem de tamanho.
    AppSelectionDialog(
        titulo = "Selecionar Vantagem",
        busca = busca,
        onBusca = { busca = it },
        contador = contadorDe(listaFiltrada.size, "vantagem", "vantagens"),
        filtros = {
            AppFiltroChip("Todas", filtroTag == null) { filtroTag = null }
            tagsDisponiveis.forEach { tag ->
                AppFiltroChip(tag, filtroTag == tag) { filtroTag = tag }
            }
        },
        onDismiss = onDismiss
    ) {
        items(listaFiltrada) { definicao ->
            val jaAdicionada = viewModel.vantagemJaAdicionada(definicao.id)
            AppSelectionRow(
                nome = definicao.nome,
                detalhe = "${definicao.custo} pts | ${rotuloDoTipoDeCusto(definicao.tipoCusto)} | pag. ${definicao.pagina}",
                detalheADireita = if (jaAdicionada) "Adicionada" else null,
                habilitado = !jaAdicionada,
                onClick = { vantagemSelecionada = definicao }
            )
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

    // Estados para Idioma
    var nomeIdioma by remember { mutableStateOf("") }
    var nivelFaladoIdioma by remember { mutableStateOf("sotaque") }
    var nivelEscritoIdioma by remember { mutableStateOf("sotaque") }

    // Estados para Mestre de Armas
    var classMestre by remember { mutableStateOf("todas") }
    var periciasMestre by remember { mutableStateOf("") }

    // Estados para Habilidades Modulares
    var selecoesHabMod by remember { mutableStateOf(mapOf<String, HabModTipoSel>()) }

    // Estados para Resistente
    var raridadeResistente by remember { mutableStateOf(10) } // Ocasional (Default)
    var grauResistente by remember { mutableStateOf(1f) } // Imunidade (Default)
    var atributoResistente by remember { mutableStateOf("HT") }

    // ST/DX Braçal: o custo depende de DUAS escolhas (braços × níveis).
    val regraBracal = regraBracalDe(definicao.id)
    var bracosBracal by remember { mutableStateOf(1) }

    // Controle e Criar (POD-21): mesma forma — a faixa do elemento fixa o
    // preço de cada nível. Ver `ElementoCustoRules.kt`.
    val regraElemento = regraDeElementoDe(definicao.id)
    var faixaElemento by remember {
        mutableStateOf(regraElemento?.faixas?.last()?.nome.orEmpty())
    }

    val metadados = when (definicao.id) {
        StBracalRule.ID, DxBracalRule.ID ->
            mapOf(BracalRuleBase.CHAVE_BRACOS to bracosBracal.toString())
        ControleRule.ID, CriarRule.ID ->
            mapOf(ElementoRuleBase.CHAVE_CATEGORIA to faixaElemento)
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
        "idioma" -> mapOf(
            "nomeIdioma" to nomeIdioma,
            "nivelFalado" to nivelFaladoIdioma,
            "nivelEscrito" to nivelEscritoIdioma
        )
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
    LaunchedEffect(definicao.id, freqAliado, ratioAliado, grupoAliado, nhContato, freqContato, confContato, powerPatrono, freqPatrono, modPatrono, secretoPatrono, powerFavor, modFavor, secretoFavor, isContactFavor, tipoGarras, tipoTelecomunicacao, tipoAparar, raridadeResistente, grauResistente, selecoesHabMod, nivelFaladoIdioma, nivelEscritoIdioma) {
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
            "idioma" -> {
                custoEscolhido = com.gurps.ficha.domain.rules.traits.IdiomaRule.metadeCusto(nivelFaladoIdioma) +
                    com.gurps.ficha.domain.rules.traits.IdiomaRule.metadeCusto(nivelEscritoIdioma)
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
                modifier = Modifier.rolagemVertical(),
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

                Text("Tipo: ${rotuloDoTipoDeCusto(definicao.tipoCusto)} | Custo base: ${definicao.custo} | Pag. ${definicao.pagina}", style = MaterialTheme.typography.bodySmall)

                when (definicao.tipoCusto) {
                    TipoCusto.POR_NIVEL -> {
                        Text("Nível:")
                        val nivelMinimo = 1
                        // O teto sai do catalogo (Talentos = 4, MB p.91); a Aptidao
                        // Magica e o fallback de 20 ficam em `TetoDeNivelDoTraco`.
                        val nivelMaximo = TetoDeNivelDoTraco.de(definicao.id, definicao.max)
                        // ⚠️ Lote LAYOUT-3: os botões passam a existir nas DUAS
                        // variantes. Antes, na `visual`, o nível só mudava
                        // arrastando o dedo — gesto que ninguém tinha como
                        // descobrir. O arrastar continua logo abaixo, como
                        // atalho de quem já sabe.
                        NivelDoTraco(
                            nivel = nivel,
                            nivelMaximo = nivelMaximo,
                            nivelMinimo = nivelMinimo,
                            onNivel = { nivel = it },
                            exibicao = "${nivelExibicaoVantagem(definicao.id, nivel)}"
                        )
                        if (!isPraCegoVariant) {
                            Text(
                                "",
                                modifier = Modifier.fillMaxWidth().pointerInput(nivel) {
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
                                        label = { Text(ROTULO_DESCRICAO_TRACO) },
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
                                        label = { Text(ROTULO_DESCRICAO_TRACO) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            )
                        } else {
                            OutlinedTextField(
                                value = descricao,
                                onValueChange = { descricao = it },
                                label = { Text(ROTULO_DESCRICAO_TRACO) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    TipoCusto.ESCOLHA -> {
                        if (regraBracal != null) {
                            // Braços × níveis: os 3/5/8 do livro são o preço de
                            // CADA +1, não um custo fechado. Ver `BracalConfig`.
                            BracalConfig(
                                regra = regraBracal,
                                bracos = bracosBracal,
                                nivel = nivel,
                                nomeAtributo = if (definicao.id == StBracalRule.ID) "ST" else "DX",
                                onChanged = { b, n ->
                                    bracosBracal = b
                                    nivel = n
                                    custoEscolhido = regraBracal.custoPorNivel(b)
                                }
                            )
                        } else if (regraElemento != null) {
                            // Faixa do elemento × níveis (POD-21). O usuário viu
                            // Criar e Controle como custo variável, subindo de 1
                            // em 1 ponto, quando o livro dá 40/20/10/5 por nível.
                            ElementoConfig(
                                regra = regraElemento,
                                faixaAtual = faixaElemento,
                                nivel = nivel,
                                onChanged = { f, n ->
                                    faixaElemento = f
                                    nivel = n
                                    custoEscolhido = regraElemento.faixas
                                        .first { it.nome == f }.custoPorNivel
                                }
                            )
                        } else if (definicao.id == "dentes") {
                            DentesConfig(currentType = tipoDentes, onChanged = { tipoDentes = it })
                        } else if (definicao.id == "garras") {
                            GarrasConfig(currentType = tipoGarras, onChanged = { tipoGarras = it })
                        } else if (definicao.id == "telecomunicacao") {
                            TelecomunicacaoConfig(currentType = tipoTelecomunicacao, onChanged = { tipoTelecomunicacao = it })
                        } else if (definicao.id == "idioma") {
                            IdiomaConfig(
                                nome = nomeIdioma, nivelFalado = nivelFaladoIdioma, nivelEscrito = nivelEscritoIdioma,
                                onChanged = { n, f, e -> nomeIdioma = n; nivelFaladoIdioma = f; nivelEscritoIdioma = e }
                            )
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
                        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text(ROTULO_DESCRICAO_TRACO) }, modifier = Modifier.fillMaxWidth())
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
                            "idioma" -> {
                                IdiomaConfig(
                                    nome = nomeIdioma, nivelFalado = nivelFaladoIdioma, nivelEscrito = nivelEscritoIdioma,
                                    onChanged = { n, f, e -> nomeIdioma = n; nivelFaladoIdioma = f; nivelEscritoIdioma = e }
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
                        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text(ROTULO_DESCRICAO_TRACO) }, modifier = Modifier.fillMaxWidth())
                    }
                    TipoCusto.FIXO -> {
                        OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text(ROTULO_DESCRICAO_TRACO) }, modifier = Modifier.fillMaxWidth())
                    }
                }

                HorizontalDivider()
                BotoesModificadoresPorTipo(
                    especificos = definicao.modificadoresEspecificos ?: emptyList(),
                    gerais = CharacterRules.DATA_REPOSITORY_INSTANCE?.modificadoresGerais ?: emptyList(),
                    poderes = CharacterRules.DATA_REPOSITORY_INSTANCE?.modificadoresPoderes ?: emptyList(),
                    tracoId = definicao.id,
                    onEscolher = { modDef ->
                        val valorInt = Regex("-?\\d+").find(modDef.valor)?.value?.toIntOrNull() ?: 0
                        mods = mods.toMutableList().apply { add(ModificadorSelecao(modDef.id, modDef.nome, valorInt, modDef.porNivel, 1, modDef.descricao, modDef.pagina, bonusBase = modDef.bonusBase)) }
                    },
                    onModAptidaoEscola = { modDef -> pendingModForSchool = modDef; showSchoolPicker = true }
                )

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
                onClick = {
                    // Idioma: usa o nome do idioma como descrição (aparece na lista e diferencia instâncias).
                    val descFinal = if (definicao.id == "idioma" && nomeIdioma.isNotBlank()) nomeIdioma else descricao
                    onSave(nivel, custoEscolhido, descFinal, mods, metadados)
                },
                enabled = (definicao.id != "ataque_inato" && definicao.id != "golpeadores") || nomeAtaque.isNotBlank()
            ) { Text(UiActionLabels.ADICIONAR) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
    )

    if (showSchoolPicker) {
        SeletorEscolaMagiaDialog(onDismiss = { showSchoolPicker = false }, onSelect = { escola ->
            pendingModForSchool?.let { modDef ->
                mods = mods.toMutableList().apply { add(ModificadorSelecao(modDef.id, modDef.nome, -40, false, 1, escola, modDef.pagina)) }
            }
            showSchoolPicker = false
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
