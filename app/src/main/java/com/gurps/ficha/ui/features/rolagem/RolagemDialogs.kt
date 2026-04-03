package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gurps.ficha.ui.appCardColors
import com.gurps.ficha.data.network.DiscordVoiceChannel
import com.gurps.ficha.domain.rules.MagiaEnergiaRules
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
                    onExpandedChange = { expandedAtaque = !expandedAtaque }
                ) {
                    OutlinedTextField(
                        value = ataqueAtual?.label ?: "Selecionar pericia",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pericia de combate") },
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
                    onExpandedChange = { expandedFonteDano = !expandedFonteDano }
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

@Composable
fun RolagemPericiasDialog(
    opcoesPericia: List<PericiaRollOption>,
    modificadoresPericia: MutableMap<String, Int>,
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
                    "Perícias",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                if (opcoesPericia.isEmpty()) {
                    Text(
                        "Sem pericias configuradas na aba Pericias.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
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
                                            val descricaoPericia = pericia.descricao.ifBlank { "Sem descrição disponível." }
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
                                                                texto = descricaoPericia
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
                                                    contentDescription = "Rolar pericia ${pericia.nome} com nível ${pericia.target}"
                                                }
                                                .clickable {
                                                    onExecutarRolagem(pericia.contextLabel, pericia.target, modPericia)
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
                        "Sem tecnicas configuradas na aba Tecnicas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
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
                                            val descricaoTecnica = tecnica.descricao.ifBlank { "Sem descrição disponível." }
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
                                                                texto = descricaoTecnica
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
                                                        "Tecnica ${tecnica.nome} sem nivel disponivel"
                                                    } else {
                                                        "Rolar tecnica ${tecnica.nome} com nível ${tecnica.target}"
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
                        "Sem magias configuradas na aba Magia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
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

@Composable
fun RolagemPersonalizadaDialog(
    dadosPersonalizadosQuantidade: Int,
    dadosPersonalizadosFaces: Int,
    dadosPersonalizadosModificador: Int,
    dadosPersonalizadosQuantidadeInput: String,
    dadosPersonalizadosFacesInput: String,
    dadosPersonalizadosModificadorInput: String,
    expressaoPersonalizada: String,
    isPraCegoVariant: Boolean,
    onUpdateQuantidade: (Int) -> Unit,
    onUpdateFaces: (Int) -> Unit,
    onUpdateModificador: (Int) -> Unit,
    onInputQuantidade: (String) -> Unit,
    onInputFaces: (String) -> Unit,
    onInputModificador: (String) -> Unit,
    onExecutarRolagem: () -> Unit,
    onDismiss: () -> Unit
) {
    val defenseNumberStyle = MaterialTheme.typography.headlineMedium

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
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Rolagem Personalizada",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Text(
                    "Deslize para cima/baixo em cada card para ajustar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (!isPraCegoVariant) {
                                    Modifier.pointerInput(dadosPersonalizadosQuantidade) {
                                        var dragAcumulado = 0f
                                        val passoPx = 20f
                                        detectVerticalDragGestures(
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                dragAcumulado += dragAmount
                                                while (abs(dragAcumulado) >= passoPx) {
                                                    if (dragAcumulado < 0f) {
                                                        onUpdateQuantidade((dadosPersonalizadosQuantidade + 1).coerceIn(1, 300))
                                                        dragAcumulado += passoPx
                                                    } else {
                                                        onUpdateQuantidade((dadosPersonalizadosQuantidade - 1).coerceIn(1, 300))
                                                        dragAcumulado -= passoPx
                                                    }
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        colors = appCardColors()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Qtd", style = MaterialTheme.typography.labelSmall)
                            Text("$dadosPersonalizadosQuantidade", style = defenseNumberStyle, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (!isPraCegoVariant) {
                                    Modifier.pointerInput(dadosPersonalizadosFaces) {
                                        var dragAcumulado = 0f
                                        val passoPx = 20f
                                        detectVerticalDragGestures(
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                dragAcumulado += dragAmount
                                                while (abs(dragAcumulado) >= passoPx) {
                                                    if (dragAcumulado < 0f) {
                                                        onUpdateFaces((dadosPersonalizadosFaces + 1).coerceIn(1, 1000))
                                                        dragAcumulado += passoPx
                                                    } else {
                                                        onUpdateFaces((dadosPersonalizadosFaces - 1).coerceIn(1, 1000))
                                                        dragAcumulado -= passoPx
                                                    }
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        colors = appCardColors()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Faces", style = MaterialTheme.typography.labelSmall)
                            Text("$dadosPersonalizadosFaces", style = defenseNumberStyle, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (!isPraCegoVariant) {
                                    Modifier.pointerInput(dadosPersonalizadosModificador) {
                                        var dragAcumulado = 0f
                                        val passoPx = 20f
                                        detectVerticalDragGestures(
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                dragAcumulado += dragAmount
                                                while (abs(dragAcumulado) >= passoPx) {
                                                    if (dragAcumulado < 0f) {
                                                        onUpdateModificador((dadosPersonalizadosModificador + 1).coerceIn(-999, 999))
                                                        dragAcumulado += passoPx
                                                    } else {
                                                        onUpdateModificador((dadosPersonalizadosModificador - 1).coerceIn(-999, 999))
                                                        dragAcumulado -= passoPx
                                                    }
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        colors = appCardColors()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Mod", style = MaterialTheme.typography.labelSmall)
                            val modTexto = when {
                                dadosPersonalizadosModificador > 0 -> "+$dadosPersonalizadosModificador"
                                else -> dadosPersonalizadosModificador.toString()
                            }
                            Text(modTexto, style = defenseNumberStyle, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = dadosPersonalizadosQuantidadeInput,
                        onValueChange = onInputQuantidade,
                        modifier = Modifier.weight(1f),
                        label = { Text("Qtd") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = dadosPersonalizadosFacesInput,
                        onValueChange = onInputFaces,
                        modifier = Modifier.weight(1f),
                        label = { Text("Faces") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = dadosPersonalizadosModificadorInput,
                        onValueChange = onInputModificador,
                        modifier = Modifier.weight(1f),
                        label = { Text("Mod") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = appCardColors()
                ) {
                    Text(
                        "Expressão: $expressaoPersonalizada",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        onExecutarRolagem()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Text(
                        "Rolar $expressaoPersonalizada",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
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
fun RolagemMagiaAlmaDialog(
    aspectos: List<SoulAspectOption>,
    onAspectoSelecionado: (SoulAspectOption) -> Unit,
    onDismiss: () -> Unit
) {
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
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Magia da Alma",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    aspectos.forEach { aspecto ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAspectoSelecionado(aspecto) },
                            colors = appCardColors()
                        ) {
                            Text(
                                text = aspecto.nome,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
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
fun RolagemDescricaoDialogModal(
    dialogInfo: RollDescricaoDialog,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogInfo.titulo) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(dialogInfo.texto, style = MaterialTheme.typography.bodyMedium)
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
fun RolagemEnergiaManualDialog(
    magiaEnergia: MagiaRollOption,
    pfAtualRolagem: Int,
    energiaManualInput: String,
    talismaMagiaVinculada: String?,
    repertorioParaTalisma: List<String>,
    isPraCegoVariant: Boolean,
    onInputMudou: (String) -> Unit,
    onTalismaVinculadoMudou: (String) -> Unit,
    onAplicar: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val exigeVinculoTalisma = magiaEnergia.definicaoId.equals("talisma", ignoreCase = true)
    var menuTalismaExpandido by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gasto de energia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Magia: ${magiaEnergia.nome}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                magiaEnergia.energia?.takeIf { it.isNotBlank() }?.let { energia ->
                    Text(
                        "Energia da ficha: $energia",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (exigeVinculoTalisma) {
                    Text(
                        "Talismã: escolha uma magia do repertório para finalizar a rolagem.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ExposedDropdownMenuBox(
                        expanded = menuTalismaExpandido,
                        onExpandedChange = { menuTalismaExpandido = !menuTalismaExpandido },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = talismaMagiaVinculada.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Talismã: magia vinculada") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuTalismaExpandido)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = menuTalismaExpandido,
                            onDismissRequest = { menuTalismaExpandido = false }
                        ) {
                            repertorioParaTalisma.forEach { nomeMagia ->
                                DropdownMenuItem(
                                    text = { Text(nomeMagia) },
                                    onClick = {
                                        onTalismaVinculadoMudou(nomeMagia)
                                        menuTalismaExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = energiaManualInput,
                    onValueChange = onInputMudou,
                    label = { Text("Custo base da magia agora") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            if (isPraCegoVariant) contentDescription = "Informar energia gasta para a magia ${magiaEnergia.nome}"
                        }
                )
                Text(
                    "PF da rolagem atual: $pfAtualRolagem",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                energiaManualInput.toIntOrNull()?.let { custoBase ->
                    val reducao = MagiaEnergiaRules.reducaoPorNh(magiaEnergia.target)
                    val custoFinal = MagiaEnergiaRules.custoAjustadoPorNh(custoBase, magiaEnergia.target)
                    Text(
                        "Reducao por NH ${magiaEnergia.target}: -$reducao | custo final: $custoFinal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    energiaManualInput.toIntOrNull()?.let { custoBase ->
                        val custoFinal = MagiaEnergiaRules.custoAjustadoPorNh(custoBase, magiaEnergia.target)
                        onAplicar(custoFinal)
                    }
                    onDismiss()
                },
                enabled = energiaManualInput.toIntOrNull() != null &&
                    (!exigeVinculoTalisma || !talismaMagiaVinculada.isNullOrBlank())
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ignorar")
            }
        }
    )
}

@Composable
fun RolagemEditarPvDialog(
    pvFixoRolagem: Int,
    maxPvRolagem: Int,
    pvAtualInput: String,
    onInputMudou: (String) -> Unit,
    onSalvar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar PV da Rolagem") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PV fixo: $pvFixoRolagem | Limite atual: 0 a $maxPvRolagem")
                OutlinedTextField(
                    value = pvAtualInput,
                    onValueChange = onInputMudou,
                    label = { Text("PV atual") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Campo de pontos de vida da rolagem" }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSalvar) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun RolagemEditarPfDialog(
    pfFixoRolagem: Int,
    pfAtualInput: String,
    onInputMudou: (String) -> Unit,
    onSalvar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar PF da Rolagem") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PF fixo: $pfFixoRolagem | Minimo atual: 0")
                OutlinedTextField(
                    value = pfAtualInput,
                    onValueChange = onInputMudou,
                    label = { Text("PF atual") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Campo de pontos de fadiga da rolagem" }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSalvar) {
                Text("Salvar")
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
fun RolagemEditarCanalDialog(
    canaisDiscord: List<DiscordVoiceChannel>,
    canalSelecionadoNome: String?,
    canaisCarregando: Boolean,
    canaisErro: String?,
    backendOnline: Boolean,
    onAtualizarCanais: () -> Unit,
    onCanalSelecionado: (DiscordVoiceChannel) -> Unit,
    onDismiss: () -> Unit
) {
    var expandedCanal by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Canal de envio Discord") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedCanal,
                    onExpandedChange = { expandedCanal = !expandedCanal }
                ) {
                    val canalLabel = when {
                        canaisCarregando -> "Carregando canais..."
                        !canalSelecionadoNome.isNullOrBlank() -> canalSelecionadoNome
                        else -> "Selecionar canal de voz"
                    }
                    OutlinedTextField(
                        value = canalLabel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCanal) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCanal,
                        onDismissRequest = { expandedCanal = false }
                    ) {
                        canaisDiscord.forEach { canal ->
                            DropdownMenuItem(
                                text = { Text("${canal.guildName} / ${canal.name}") },
                                onClick = {
                                    onCanalSelecionado(canal)
                                    expandedCanal = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = onAtualizarCanais,
                    enabled = !canaisCarregando,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (backendOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = if (canaisCarregando) "ATUALIZANDO..." else "ATUALIZAR CANAL",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!canaisErro.isNullOrBlank()) {
                    Text(
                        "Erro ao carregar canais: $canaisErro",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
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
