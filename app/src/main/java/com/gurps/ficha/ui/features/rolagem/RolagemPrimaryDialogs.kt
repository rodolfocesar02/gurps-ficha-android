package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gurps.ficha.ui.rolagemVertical
import com.gurps.ficha.ui.appCardColors
import com.gurps.ficha.ui.linhaAlternavel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolagemConfigurarAtaqueDialog(
    opcoesAtaque: List<RollMappedOption>,
    ataqueAtual: RollMappedOption?,
    onAtaqueSelecionado: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var expandedAtaque by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Ataque") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedAtaque,
                    onExpandedChange = { expandedAtaque = !expandedAtaque },
                    modifier = Modifier.semantics { contentDescription = "Selecionar perícia de combate" }
                ) {
                    OutlinedTextField(
                        value = ataqueAtual?.label ?: "Selecionar perícia",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Perícia de combate") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAtaque) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAtaque,
                        onDismissRequest = { expandedAtaque = false }
                    ) {
                        opcoesAtaque.forEach { ataque ->
                            DropdownMenuItem(
                                text = { Text(ataque.label) },
                                onClick = {
                                    onAtaqueSelecionado(ataque.id)
                                    expandedAtaque = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolagemConfigurarDanoDialog(
    fontesDano: List<DamageSourceOption>,
    fonteDanoAtual: DamageSourceOption,
    onFonteDanoSelecionada: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var expandedFonteDano by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Dano") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedFonteDano,
                    onExpandedChange = { expandedFonteDano = !expandedFonteDano },
                    modifier = Modifier.semantics { contentDescription = "Selecionar fonte de dano ou arma" }
                ) {
                    OutlinedTextField(
                        value = fonteDanoAtual.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Arma / Fonte de dano") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFonteDano) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFonteDano,
                        onDismissRequest = { expandedFonteDano = false }
                    ) {
                        fontesDano.forEach { fonte ->
                            DropdownMenuItem(
                                text = { Text(fonte.label) },
                                onClick = {
                                    onFonteDanoSelecionada(fonte.id)
                                    expandedFonteDano = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

/**
 * **A lista completa de caixinhas de uma perícia** — uma fonte só (Lote P-SIT).
 *
 * ⚠️ Existe porque a lista era montada **duas vezes** dentro do diálogo: uma
 * para desenhar as caixinhas e outra para somar o que estava marcado. As duas
 * casavam por **índice**. Bastava uma ganhar uma fonte nova e a outra não para
 * o índice marcado apontar para a caixinha errada — e o número sairia errado
 * **sem erro nenhum**. Com o lote trazendo duas fontes novas de uma vez, o risco
 * deixou de ser teórico.
 *
 * A ORDEM aqui é o contrato: traços da ficha, depois a situação da perícia,
 * depois a cultura, e a luz da cena por último.
 */
private fun condicionaisDaPericia(
    personagem: com.gurps.ficha.model.Personagem,
    nomeDaPericia: String,
    condicionalDaLuz: com.gurps.ficha.domain.rules.traits.BonusCondicional?
): List<com.gurps.ficha.domain.rules.traits.BonusCondicional> =
    com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
        .getBonusCondicionais(personagem, nomeDaPericia) +
        com.gurps.ficha.domain.rules.ModificadoresSituacionais.condicionaisDe(nomeDaPericia) +
        listOfNotNull(
            com.gurps.ficha.domain.rules.FamiliaridadeCulturalRules
                .condicionalDe(personagem, nomeDaPericia),
            condicionalDaLuz
        )

@Composable
fun RolagemPericiasDialog(
    opcoesPericia: List<PericiaRollOption>,
    modificadoresPericia: MutableMap<String, Int>,
    isPraCegoVariant: Boolean,
    // Opcional: quando vem, o card mostra de onde vem o bonus da pericia.
    personagem: com.gurps.ficha.model.Personagem? = null,
    // Lote LUZ-2: a escuridao da cena, oferecida como caixinha em CADA pericia.
    // Nao entra sozinha porque o livro amarra a escuridao a VISAO -- quem sabe se
    // aquele teste depende de ver e o Mestre.
    condicionalDaLuz: com.gurps.ficha.domain.rules.traits.BonusCondicional? = null,
    // Lote P-EQUIP: a qualidade do equipamento vale para as 32 pericias que
    // dependem dele. Mora na ABA, nao aqui, para nao sumir ao fechar o dialogo.
    nivelEquipamento: com.gurps.ficha.domain.rules.QualidadeDoEquipamento.Nivel =
        com.gurps.ficha.domain.rules.QualidadeDoEquipamento.PADRAO,
    onAlternarEquipamento: () -> Unit = {},
    onShowDescricao: (RollDescricaoDialog) -> Unit,
    onExecutarRolagem: (contextoLabel: String, alvo: Int, mod: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val defenseNumberStyle = MaterialTheme.typography.headlineMedium
    val compactLabelStyle = MaterialTheme.typography.labelSmall
    // Bonus condicionais marcados, por id de pericia. Zerado ao fechar o
    // dialogo: a condicao vale para AQUELA rolagem, nao para sempre.
    val condicionaisMarcados = remember { mutableStateMapOf<String, Set<Int>>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Perícias",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                // Lote P-EQUIP: um seletor só para as 32 perícias que dependem
                // de equipamento (MB p.346). Só aparece se a ficha tiver alguma
                // delas — senão seria ruído para quem não usa ferramenta.
                val temPericiaDeEquipamento = opcoesPericia.any {
                    com.gurps.ficha.domain.rules.QualidadeDoEquipamento
                        .dependeDeEquipamento(it.nome)
                }
                if (temPericiaDeEquipamento) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .linhaAlternavel(
                                marcado = nivelEquipamento !=
                                    com.gurps.ficha.domain.rules.QualidadeDoEquipamento.PADRAO,
                                descricao = com.gurps.ficha.domain.rules.QualidadeDoEquipamento
                                    .rotuloAcessivel(nivelEquipamento),
                                onAlternar = onAlternarEquipamento
                            )
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = nivelEquipamento !=
                                com.gurps.ficha.domain.rules.QualidadeDoEquipamento.PADRAO,
                            onCheckedChange = null
                        )
                        Text(
                            com.gurps.ficha.domain.rules.QualidadeDoEquipamento
                                .rotuloDoSeletor(nivelEquipamento),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (nivelEquipamento ==
                                com.gurps.ficha.domain.rules.QualidadeDoEquipamento.PADRAO
                            ) {
                                MaterialTheme.colorScheme.outline
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }

                if (opcoesPericia.isEmpty()) {
                    Text(
                        "Sem perícias disponíveis. Verifique no catálogo ou na aba de Perícias se todos os pré-requisitos foram atendidos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .rolagemVertical(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        opcoesPericia.forEach { pericia ->
                            val modPericia = if (isPraCegoVariant) 0 else (modificadoresPericia[pericia.id] ?: 0)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = appCardColors()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(2f),
                                            horizontalAlignment = Alignment.Start,
                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            val descriçãoPericia = pericia.descricao.ifBlank { "Sem descrição disponível." }
                                            Text(
                                                pericia.nome,
                                                style = defenseNumberStyle,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        onShowDescricao(
                                                            RollDescricaoDialog(
                                                                titulo = "Descrição: ${pericia.nome}",
                                                                texto = descriçãoPericia
                                                            )
                                                        )
                                                    }
                                                    .semantics {
                                                        if (isPraCegoVariant) {
                                                            contentDescription = "Nome da perícia ${pericia.nome}. Toque para abrir descrição."
                                                        }
                                                    },
                                                textAlign = TextAlign.Start,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (pericia.especializacao.isNotBlank()) {
                                                Text(
                                                    pericia.especializacao,
                                                    style = defenseNumberStyle,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Start,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            // O número do equipamento aparece NA PERÍCIA, não só no
                                            // seletor lá em cima: é a diferença entre o jogador
                                            // conferir a conta e ter de confiar nela.
                                            com.gurps.ficha.domain.rules.QualidadeDoEquipamento
                                                .rotuloNaPericia(pericia.nome, nivelEquipamento)
                                                .takeIf { it.isNotBlank() }?.let { linha ->
                                                    Text(
                                                        linha,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            // De onde vem o bônus — mesmo componente da aba Perícias.
                                            personagem?.let { p ->
                                                com.gurps.ficha.ui.features.traits.OrigemDoBonusPericia(
                                                    personagem = p,
                                                    nomeDaPericia = pericia.nome
                                                )
                                                val condicionais =
                                                    condicionaisDaPericia(p, pericia.nome, condicionalDaLuz)
                                                PainelBonusCondicional(
                                                    bonus = condicionais,
                                                    marcados = condicionaisMarcados[pericia.id].orEmpty(),
                                                    onAlternar = { idx ->
                                                        val atual = condicionaisMarcados[pericia.id].orEmpty()
                                                        condicionaisMarcados[pericia.id] =
                                                            if (idx in atual) atual - idx else atual + idx
                                                    }
                                                )
                                            }
                                        }
                                        Text(
                                            "NH ${pericia.target}",
                                            modifier = Modifier
                                                .weight(1f)
                                                .then(
                                                    if (!isPraCegoVariant) {
                                                        Modifier.pointerInput(pericia.id, modPericia) {
                                                            var dragAcumulado = 0f
                                                            val passoPx = 20f
                                                            detectVerticalDragGestures(
                                                                onVerticalDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    dragAcumulado += dragAmount
                                                                    while (abs(dragAcumulado) >= passoPx) {
                                                                        val atual = modificadoresPericia[pericia.id] ?: 0
                                                                        if (dragAcumulado < 0f) {
                                                                            modificadoresPericia[pericia.id] = (atual + 1).coerceIn(-20, 20)
                                                                            dragAcumulado += passoPx
                                                                        } else {
                                                                            modificadoresPericia[pericia.id] = (atual - 1).coerceIn(-20, 20)
                                                                            dragAcumulado -= passoPx
                                                                        }
                                                                    }
                                                                }
                                                            )
                                                        }
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                                .semantics {
                                                    contentDescription = "Rolar perícia ${pericia.nome} com nível ${pericia.target}"
                                                }
                                                .clickable {
                                                    // Os condicionais MARCADOS entram como modificador
                                                    // desta rolagem -- nunca no NH da ficha.
                                                    //
                                                    // ⚠️ A lista sai da MESMA funcao que desenhou as
                                                    // caixinhas. Montada duas vezes, uma podia ganhar
                                                    // uma fonte nova e a outra nao -- e ai o indice
                                                    // marcado apontaria para outra caixinha, somando o
                                                    // numero errado sem erro nenhum.
                                                    val extraCond = personagem?.let { p ->
                                                        somaDosMarcados(
                                                            condicionaisDaPericia(p, pericia.nome, condicionalDaLuz),
                                                            condicionaisMarcados[pericia.id].orEmpty()
                                                        )
                                                    } ?: 0
                                                    val modEquip = com.gurps.ficha.domain.rules
                                                        .QualidadeDoEquipamento
                                                        .modificador(pericia.nome, nivelEquipamento)
                                                    onExecutarRolagem(
                                                        pericia.contextLabel,
                                                        pericia.target,
                                                        modPericia + extraCond + modEquip
                                                    )
                                                    onDismiss()
                                                },
                                            style = defenseNumberStyle,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }
                                    if (!isPraCegoVariant && modPericia != 0) {
                                        Text(
                                            "mod ${if (modPericia >= 0) "+$modPericia" else "$modPericia"}",
                                            style = compactLabelStyle,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}

@Composable
fun RolagemTecnicasDialog(
    opcoesTecnica: List<TecnicaRollOption>,
    modificadoresTecnica: MutableMap<String, Int>,
    isPraCegoVariant: Boolean,
    onShowDescricao: (RollDescricaoDialog) -> Unit,
    onExecutarRolagem: (contextoLabel: String, alvo: Int, mod: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val defenseNumberStyle = MaterialTheme.typography.headlineMedium
    val compactLabelStyle = MaterialTheme.typography.labelSmall

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Técnicas",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                if (opcoesTecnica.isEmpty()) {
                    Text(
                        "Sem técnicas disponíveis. Verifique se a perícia base está correta e se os pré-requisitos foram atendidos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .rolagemVertical(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        opcoesTecnica.forEach { tecnica ->
                            val modTecnica = if (isPraCegoVariant) 0 else (modificadoresTecnica[tecnica.id] ?: 0)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = appCardColors()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(2f),
                                            horizontalAlignment = Alignment.Start,
                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            val descriçãoTecnica = tecnica.descricao.ifBlank { "Sem descrição disponível." }
                                            Text(
                                                tecnica.nome,
                                                style = defenseNumberStyle,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        onShowDescricao(
                                                            RollDescricaoDialog(
                                                                titulo = "Descrição: ${tecnica.nome}",
                                                                texto = descriçãoTecnica
                                                            )
                                                        )
                                                    }
                                                    .semantics {
                                                        if (isPraCegoVariant) {
                                                            contentDescription = "Nome da técnica ${tecnica.nome}. Toque para abrir descrição."
                                                        }
                                                    },
                                                textAlign = TextAlign.Start,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (tecnica.periciaBaseNome.isNotBlank()) {
                                                Text(
                                                    tecnica.periciaBaseNome,
                                                    style = compactLabelStyle,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Start,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Text(
                                            "NH ${tecnica.target ?: "-"}",
                                            modifier = Modifier
                                                .weight(1f)
                                                .then(
                                                    if (!isPraCegoVariant && tecnica.target != null) {
                                                        Modifier.pointerInput(tecnica.id, modTecnica) {
                                                            var dragAcumulado = 0f
                                                            val passoPx = 20f
                                                            detectVerticalDragGestures(
                                                                onVerticalDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    dragAcumulado += dragAmount
                                                                    while (abs(dragAcumulado) >= passoPx) {
                                                                        val atual = modificadoresTecnica[tecnica.id] ?: 0
                                                                        if (dragAcumulado < 0f) {
                                                                            modificadoresTecnica[tecnica.id] = (atual + 1).coerceIn(-20, 20)
                                                                            dragAcumulado += passoPx
                                                                        } else {
                                                                            modificadoresTecnica[tecnica.id] = (atual - 1).coerceIn(-20, 20)
                                                                            dragAcumulado -= passoPx
                                                                        }
                                                                    }
                                                                }
                                                            )
                                                        }
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                                .semantics {
                                                    contentDescription = if (tecnica.target == null) {
                                                        "Técnica ${tecnica.nome} sem nível disponível"
                                                    } else {
                                                        "Rolar técnica ${tecnica.nome} com nível ${tecnica.target}"
                                                    }
                                                }
                                                .clickable(enabled = tecnica.target != null) {
                                                    if (tecnica.target != null) {
                                                        onExecutarRolagem(tecnica.contextLabel, tecnica.target, modTecnica)
                                                        onDismiss()
                                                    }
                                                },
                                            style = defenseNumberStyle,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }
                                    if (!isPraCegoVariant && modTecnica != 0 && tecnica.target != null) {
                                        Text(
                                            "mod ${if (modTecnica >= 0) "+$modTecnica" else "$modTecnica"}",
                                            style = compactLabelStyle,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}

@Composable
fun RolagemMagiasDialog(
    opcoesMagia: List<MagiaRollOption>,
    modificadoresMagia: MutableMap<String, Int>,
    isPraCegoVariant: Boolean,
    onShowDescricao: (RollDescricaoDialog) -> Unit,
    onExecutarRolagem: (MagiaRollOption, mod: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val defenseNumberStyle = MaterialTheme.typography.headlineMedium
    val compactLabelStyle = MaterialTheme.typography.labelSmall

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Magias",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                if (opcoesMagia.isEmpty()) {
                    Text(
                        "Sem magias disponíveis. Verifique se possui Aptidão Mágica e se os pré-requisitos foram atendidos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .rolagemVertical(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        opcoesMagia.forEach { magia ->
                            val modMagia = if (isPraCegoVariant) 0 else (modificadoresMagia[magia.id] ?: 0)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = appCardColors()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            magia.nome,
                                            style = defenseNumberStyle,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier
                                                .weight(2f)
                                                .clickable {
                                                    val descriçãoMagia = magia.descricao.ifBlank { "Sem descrição disponível." }
                                                    onShowDescricao(
                                                        RollDescricaoDialog(
                                                            titulo = "Descrição: ${magia.nome}",
                                                            texto = descriçãoMagia
                                                        )
                                                    )
                                                }
                                                .semantics {
                                                    if (isPraCegoVariant) {
                                                        contentDescription = "Nome da magia ${magia.nome}. Toque para abrir descrição."
                                                    }
                                                },
                                            textAlign = TextAlign.Start,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "NH ${magia.target}",
                                            modifier = Modifier
                                                .weight(1f)
                                                .then(
                                                    if (!isPraCegoVariant) {
                                                        Modifier.pointerInput(magia.id, modMagia) {
                                                            var dragAcumulado = 0f
                                                            val passoPx = 20f
                                                            detectVerticalDragGestures(
                                                                onVerticalDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    dragAcumulado += dragAmount
                                                                    while (abs(dragAcumulado) >= passoPx) {
                                                                        val atual = modificadoresMagia[magia.id] ?: 0
                                                                        if (dragAcumulado < 0f) {
                                                                            modificadoresMagia[magia.id] = (atual + 1).coerceIn(-20, 20)
                                                                            dragAcumulado += passoPx
                                                                        } else {
                                                                            modificadoresMagia[magia.id] = (atual - 1).coerceIn(-20, 20)
                                                                            dragAcumulado -= passoPx
                                                                        }
                                                                    }
                                                                }
                                                            )
                                                        }
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                                .semantics {
                                                    contentDescription = "Rolar magia ${magia.nome} com nível ${magia.target}"
                                                }
                                                .clickable {
                                                    onExecutarRolagem(magia, modMagia)
                                                    onDismiss()
                                                },
                                            style = defenseNumberStyle,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }
                                    magia.duracao?.takeIf { it.isNotBlank() }?.let { duracao ->
                                        Text(
                                            "Duração: $duracao",
                                            style = compactLabelStyle,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .semantics {
                                                    if (isPraCegoVariant) contentDescription = "Duração da magia ${magia.nome}: $duracao"
                                                },
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    magia.energia?.takeIf { it.isNotBlank() }?.let { energia ->
                                        Text(
                                            "Energia: $energia",
                                            style = compactLabelStyle,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .semantics {
                                                    if (isPraCegoVariant) contentDescription = "Energia da magia ${magia.nome}: $energia"
                                                },
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    magia.tempoOperacao?.takeIf { it.isNotBlank() }?.let { tempo ->
                                        Text(
                                            "Tempo de operação: $tempo",
                                            style = compactLabelStyle,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .semantics {
                                                    if (isPraCegoVariant) contentDescription = "Tempo de operação da magia ${magia.nome}: $tempo"
                                                },
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (!isPraCegoVariant && modMagia != 0) {
                                        Text(
                                            "mod ${if (modMagia >= 0) "+$modMagia" else "$modMagia"}",
                                            style = compactLabelStyle,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}
