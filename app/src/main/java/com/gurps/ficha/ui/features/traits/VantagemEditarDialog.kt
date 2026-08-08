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
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.UiTokens
import com.gurps.ficha.viewmodel.FichaViewModel
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.domain.rules.traits.BracalRuleBase
import com.gurps.ficha.domain.rules.traits.DxBracalRule
import com.gurps.ficha.domain.rules.traits.StBracalRule
import com.gurps.ficha.data.DataRepository
import kotlin.math.abs

/**
 * Diálogo de EDIÇÃO de uma vantagem já na ficha.
 *
 * Separado de `VantagemDialogs.kt` em 28/07: aquele arquivo passou de 1.000
 * linhas ao ganhar o bloco de ST/DX Braçal, e o projeto tem teto de 1.000.
 * Nada mudou de comportamento — é o mesmo código, em outro arquivo.
 *
 * O irmão dele (adicionar) continua em `VantagemDialogs.kt`.
 */

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EditarVantagemDialog(
    vantagem: VantagemSelecionada,
    descricaoCatalogo: String = "",
    weaponSuggestions: List<String> = emptyList(),
    poderesDisponiveis: List<com.gurps.ficha.model.Poder> = emptyList(),
    /**
     * 🔴 Lote LAYOUT-3: o teto do nível vindo do **catálogo**.
     *
     * Até aqui a edição passava `null` a `TetoDeNivelDoTraco.de(...)`, com o
     * comentário *"na edição só temos o traço selecionado, que não carrega o max
     * do catálogo"*. Só que **16 vantagens têm teto no catálogo** — Artífice,
     * Curandeiro, Explorador e os outros Talentos param em **4**; Espinhos em 3.
     * Com `null`, a edição caía no teto geral de **20**.
     *
     * ⚠️ Era o T-LI6 pela metade: o usuário validou o teto no seletor de
     * adicionar, e quem já tinha o traço na ficha continuava levando ao 20 pelo
     * lápis. Os dois pontos de chamada já buscavam a definição no catálogo para
     * pegar a descrição — passar o `max` junto foi uma linha em cada um.
     */
    maxDoCatalogo: Int? = null,
    onDismiss: () -> Unit,
    onSave: (VantagemSelecionada) -> Unit
) {
    var nivel by remember { mutableStateOf(vantagem.nivel) }
    var custoEscolhido by remember { mutableStateOf(vantagem.custoEscolhido) }
    var descricao by remember { mutableStateOf(vantagem.descricao) }
    var mods by remember { mutableStateOf(vantagem.modificadores.toList()) }

    var showSchoolPicker by remember { mutableStateOf(false) }
    var pendingModForSchool by remember { mutableStateOf<ModificadorDefinicao?>(null) }
    var mostrarDescricaoCatalogo by remember { mutableStateOf(false) }
    var poderIdSelecionado by remember { mutableStateOf(vantagem.poderId) }
    
    val descricaoCatalogoFinal = if (vantagem.definicaoId == "retencao") {
        "O personagem tem um ataque capaz de manter o alvo preso no lugar..." 
    } else descricaoCatalogo

    // ST/DX Braçal: braços × níveis. Ficha antiga não tem o dado — cai em 1.
    val regraBracal = regraBracalDe(vantagem.definicaoId)
    var bracosBracal by remember {
        mutableStateOf(vantagem.metadados?.get(BracalRuleBase.CHAVE_BRACOS)?.toIntOrNull() ?: 1)
    }

    var nomeAtaque by remember { mutableStateOf(vantagem.metadados?.get("nomePersonalizado") ?: vantagem.nome) }
    var tipoDanoAtaque by remember { mutableStateOf(vantagem.metadados?.get("tipoDano") ?: "cont") }
    var dadosAtaque by remember { mutableStateOf(vantagem.metadados?.get("dice")?.toIntOrNull() ?: 1) }
    var bonusAtaque by remember { mutableStateOf(vantagem.metadados?.get("bonus")?.toIntOrNull() ?: 0) }
    var tipoDentes by remember { mutableStateOf(vantagem.metadados?.get("tipoDentes") ?: "rombo") }
    var tipoGarras by remember { mutableStateOf(vantagem.metadados?.get("tipoGarras") ?: "afiadas") }
    var tipoTelecomunicacao by remember { mutableStateOf(vantagem.metadados?.get("tipoTelecomunicacao") ?: "radio") }
    var tipoAparar by remember { mutableStateOf(vantagem.metadados?.get("tipo") ?: "global") }
    var periciaAparar by remember { mutableStateOf(vantagem.metadados?.get("skillId") ?: "desarmado") }
    var nomeIdioma by remember { mutableStateOf(vantagem.metadados?.get("nomeIdioma") ?: "") }
    var nivelFaladoIdioma by remember { mutableStateOf(vantagem.metadados?.get("nivelFalado") ?: "sotaque") }
    var nivelEscritoIdioma by remember { mutableStateOf(vantagem.metadados?.get("nivelEscrito") ?: "sotaque") }

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
    LaunchedEffect(vantagem.definicaoId, freqAliado, ratioAliado, grupoAliado, nhContato, freqContato, confContato, powerPatrono, freqPatrono, modPatrono, secretoPatrono, powerFavor, modFavor, secretoFavor, isContactFavor, tipoGarras, tipoTelecomunicacao, tipoAparar, classMestre, raridadeResistente, grauResistente, selecoesHabMod, nivelFaladoIdioma, nivelEscritoIdioma) {
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
        StBracalRule.ID, DxBracalRule.ID ->
            mapOf(BracalRuleBase.CHAVE_BRACOS to bracosBracal.toString())
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

                Text("Tipo: ${rotuloDoTipoDeCusto(vantagem.tipoCusto)} | Custo base: ${def?.custo ?: vantagem.custoBase} | Pag. ${def?.pagina ?: vantagem.pagina}", style = MaterialTheme.typography.bodySmall)
                
                // Vinculação de Poderes
                if (poderesDisponiveis.isNotEmpty()) {
                    var expandedPoder by remember { mutableStateOf(false) }
                    val selectedPoder = poderesDisponiveis.find { it.id == poderIdSelecionado }
                    
                    ExposedDropdownMenuBox(expanded = expandedPoder, onExpandedChange = { expandedPoder = !expandedPoder }) {
                        OutlinedTextField(
                            value = selectedPoder?.nome ?: "Nenhum Poder",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Vincular a Poder") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPoder) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedPoder, onDismissRequest = { expandedPoder = false }) {
                            DropdownMenuItem(text = { Text("Nenhum Poder") }, onClick = { 
                                poderIdSelecionado = null 
                                expandedPoder = false 
                            })
                            poderesDisponiveis.forEach { poder ->
                                DropdownMenuItem(
                                    text = { Text("${poder.nome} (${poder.modificadorDePoder}%)") },
                                    onClick = {
                                        poderIdSelecionado = poder.id
                                        expandedPoder = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Como passar o poderId de volta no onSave? Modificando a cópia final.
                }

                if (vantagem.tipoCusto == TipoCusto.POR_NIVEL) {
                    // 🔴 O `maxDoCatalogo` no lugar do `null` de antes: é o que
                    // devolve o teto do livro à edição.
                    val nivelMaximo = TetoDeNivelDoTraco.de(vantagem.definicaoId, maxDoCatalogo)
                    NivelDoTraco(
                        nivel = nivel,
                        nivelMaximo = nivelMaximo,
                        onNivel = { nivel = it },
                        exibicao = "${nivelExibicaoVantagem(vantagem.definicaoId, nivel)}"
                    )
                } else if (regraBracal != null) {
                    BracalConfig(
                        regra = regraBracal,
                        bracos = bracosBracal,
                        nivel = nivel,
                        nomeAtributo = if (vantagem.definicaoId == StBracalRule.ID) "ST" else "DX",
                        onChanged = { b, n ->
                            bracosBracal = b
                            nivel = n
                            custoEscolhido = regraBracal.custoPorNivel(b)
                        }
                    )
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
                    "idioma" -> IdiomaConfig(nome = nomeIdioma, nivelFalado = nivelFaladoIdioma, nivelEscrito = nivelEscritoIdioma, onChanged = { n, f, e -> nomeIdioma = n; nivelFaladoIdioma = f; nivelEscritoIdioma = e })
                    "defesas_ampliadas_aparar_ampliado" -> ApararAmpliadoConfig(currentType = tipoAparar, currentSkill = periciaAparar, onChanged = { t, s -> tipoAparar = t; periciaAparar = s })
                    "mestre_de_armas" -> MestreDeArmasConfig(currentClass = classMestre, currentSkills = periciasMestre, onChanged = { c, s -> classMestre = c; periciasMestre = s })
                    "habilidades_modulares" -> HabilidadesModularesConfig(selecoes = selecoesHabMod, onChanged = { selecoesHabMod = it })
                }

                OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text(ROTULO_DESCRICAO_TRACO) }, modifier = Modifier.fillMaxWidth())


                HorizontalDivider()
                BotoesModificadoresPorTipo(
                    especificos = def?.modificadoresEspecificos ?: emptyList(),
                    gerais = CharacterRules.DATA_REPOSITORY_INSTANCE?.modificadoresGerais ?: emptyList(),
                    poderes = CharacterRules.DATA_REPOSITORY_INSTANCE?.modificadoresPoderes ?: emptyList(),
                    tracoId = def?.id ?: vantagem.definicaoId,
                    onEscolher = { modDef ->
                        val valorInt = Regex("-?\\d+").find(modDef.valor)?.value?.toIntOrNull() ?: 0
                        mods = mods.toMutableList().apply { add(ModificadorSelecao(modDef.id, modDef.nome, valorInt, modDef.porNivel, 1, modDef.descricao, modDef.pagina, bonusBase = modDef.bonusBase)) }
                    },
                    onModAptidaoEscola = { modDef -> pendingModForSchool = modDef; showSchoolPicker = true }
                )

                mods.forEachIndexed { idx, mod ->
                    ModificadorSelecionadoItem(mod, onUpdate = { m -> mods = mods.toMutableList().apply { this[idx] = m } }, onDelete = { mods = mods.toMutableList().apply { removeAt(idx) } })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val descFinal = if (vantagem.definicaoId == "idioma" && nomeIdioma.isNotBlank()) nomeIdioma else descricao
                    onSave(vantagem.copy(nivel = nivel, custoEscolhido = custoEscolhido, descricao = descFinal, modificadores = mods, metadados = metadados, poderId = poderIdSelecionado))
                }
            ) { Text(UiActionLabels.SALVAR) }
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
            nome = vantagem.nome,
            descricao = descricaoCatalogoFinal,
            onDismiss = { mostrarDescricaoCatalogo = false }
        )
    }
}

