package com.gurps.ficha.ui.features.magic

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Divider as _Divider // Unused if possible
import androidx.compose.material3.HorizontalDivider
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
import com.gurps.ficha.ui.UiActionLabels
import com.gurps.ficha.ui.UiTokens
import com.gurps.ficha.ui.appCardColors
import kotlin.math.abs

private val PONTOS_PRESETS = listOf(1, 2, 4, 8, 12)

private fun ajustarPontosPreset(atual: Int, incrementar: Boolean): Int {
    val indice = PONTOS_PRESETS.indexOf(atual).let { if (it == -1) 0 else it }
    return if (incrementar) {
        PONTOS_PRESETS[(indice + 1).coerceAtMost(PONTOS_PRESETS.lastIndex)]
    } else {
        PONTOS_PRESETS[(indice - 1).coerceAtLeast(0)]
    }
}

private fun formatarFalhaPreReq(falha: String): String {
    return falha
        .replace(Regex("(?i)^pré[-‑ ]requisito\\s+não\\s+atendido\\s*:\\s*"), "")
        .replace(Regex("(?i)^pre[-‑ ]requisito\\s+nao\\s+atendido\\s*:\\s*"), "")
        .replace(Regex("(?i)conhecimento\\s+magico\\s+requerido\\s*:\\s*"), "")
        .replace(Regex("(?i)conhecimento\\s+requerido\\s*:\\s*"), "")
        .replace(Regex("\\s*,\\s*"), ", ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdicionarMagiaDialog(
    definicao: MagiaDefinicao,
    personagem: Personagem,
    nivelAptidaoMagica: Int,
    prereqFalha: String?,
    onDismiss: () -> Unit,
    onSave: (pontos: Int, encantamentoAlvo: String?, especializacao: String?, forcada: Boolean) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var pontosGastos by remember { mutableStateOf(1) }
    var encantamentoAlvoInput by remember { mutableStateOf("") }
    var especializacaoMagiaInput by remember { mutableStateOf("") }
    var adicaoForcadaSemPrereq by remember { mutableStateOf(false) }
    var confirmarAdicaoForcada by remember { mutableStateOf(false) }
    var mostrarDescricaoMagiaPopup by remember { mutableStateOf(false) }
    val tags = definicao.descricao?.lowercase() ?: ""
    val exigeEncantamentoAlvo = tags.contains("encantamento") && definicao.classe?.lowercase()?.contains("encantamento") == true
    val exigeEspecializacao = definicao.preRequisitos?.lowercase()?.contains("especializacao") == true
    val exigeSubEscolaAnimais = definicao.escola?.any { it.lowercase().contains("controle de animais") } == true
    val opcoesSubEscolaAnimais = listOf("Mamíferos", "Répteis", "Aves", "Anfíbios", "Peixes", "Moluscos", "Insetos")
    var subEscolaAnimaisExpandida by remember { mutableStateOf(false) }
    val labelEspecializacao = if (exigeSubEscolaAnimais) "Qual categoria de animal?" else "Especialização"

    val previewMagia = MagiaSelecionada(
        definicaoId = definicao.id,
        nome = definicao.nome,
        dificuldade = Dificuldade.fromSigla(definicao.dificuldadeFixa),
        pontosGastos = pontosGastos
    )
    val nivelPreview = previewMagia.calcularNivel(personagem, nivelAptidaoMagica)
    val nivelRelativo = previewMagia.getNivelRelativo(personagem, nivelAptidaoMagica)
    val descricaoMagia = definicao.texto?.trim().orEmpty()
    val erroPersistente = null // Placeholder if needed

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                definicao.nome,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .let {
                        if (descricaoMagia.isNotBlank()) {
                            it.clickable { mostrarDescricaoMagiaPopup = true }
                        } else {
                            it
                        }
                    }
                    .semantics {
                        if (isPraCegoVariant && descricaoMagia.isNotBlank()) {
                            contentDescription = "Nome da magia ${definicao.nome}. Toque para abrir descricao."
                        }
                    }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                val difNome = definicao.dificuldadeFixa ?: "D"
                Text("IQ/$difNome", style = MaterialTheme.typography.bodyMedium)

                HorizontalDivider()
                Text("Pontos Gastos:")
                if (!isPraCegoVariant) {
                    Text(
                        "$pontosGastos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .width(56.dp)
                            .pointerInput(pontosGastos) {
                                var dragAcumulado = 0f
                                val passoPx = 24f
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAcumulado += dragAmount
                                        while (abs(dragAcumulado) >= passoPx) {
                                            pontosGastos = ajustarPontosPreset(
                                                atual = pontosGastos,
                                                incrementar = dragAcumulado < 0f
                                            )
                                            dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                        }
                                    }
                                )
                            },
                        textAlign = TextAlign.Center
                    )
                }
                if (isPraCegoVariant) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(
                            onClick = { pontosGastos = ajustarPontosPreset(pontosGastos, incrementar = false) },
                            modifier = Modifier.semantics { contentDescription = "Diminuir pontos gastos da magia" }
                        ) { Text("-") }
                        Text(
                            "$pontosGastos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(horizontal = 8.dp)
                                .semantics { contentDescription = "Pontos gastos atuais da magia: $pontosGastos" }
                        )
                        TextButton(
                            onClick = { pontosGastos = ajustarPontosPreset(pontosGastos, incrementar = true) },
                            modifier = Modifier.semantics { contentDescription = "Aumentar pontos gastos da magia" }
                        ) { Text("+") }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    PONTOS_PRESETS.forEach { pts ->
                        TextButton(
                            onClick = { pontosGastos = pts },
                            modifier = Modifier.padding(horizontal = 1.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("$pts", fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider()
                val erroPreReq = erroPersistente ?: prereqFalha?.let {
                    "Pre-requisito nao atendido: ${formatarFalhaPreReq(it)}"
                }
                if (!erroPreReq.isNullOrBlank()) {
                    Text(
                        text = erroPreReq,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (!prereqFalha.isNullOrBlank()) {
                    TextButton(
                        onClick = { confirmarAdicaoForcada = true },
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = if (adicaoForcadaSemPrereq) {
                                "Adição Forçada sem pré-requisito (ATIVADA)"
                            } else {
                                "Adição Forçada sem pré-requisito"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (exigeEncantamentoAlvo) {
                    OutlinedTextField(
                        value = encantamentoAlvoInput,
                        onValueChange = { encantamentoAlvoInput = it.take(80) },
                        label = { Text("Qual encantamento?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (exigeSubEscolaAnimais) {
                    ExposedDropdownMenuBox(
                        expanded = subEscolaAnimaisExpandida,
                        onExpandedChange = { subEscolaAnimaisExpandida = !subEscolaAnimaisExpandida },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = especializacaoMagiaInput,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(labelEspecializacao) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = subEscolaAnimaisExpandida)
                            },
                            singleLine = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = subEscolaAnimaisExpandida,
                            onDismissRequest = { subEscolaAnimaisExpandida = false }
                        ) {
                            opcoesSubEscolaAnimais.forEach { opcao ->
                                DropdownMenuItem(
                                    text = { Text(opcao) },
                                    onClick = {
                                        especializacaoMagiaInput = opcao
                                        subEscolaAnimaisExpandida = false
                                    }
                                )
                            }
                        }
                    }
                } else if (exigeEspecializacao) {
                    OutlinedTextField(
                        value = especializacaoMagiaInput,
                        onValueChange = { especializacaoMagiaInput = it.take(80) },
                        label = { Text(labelEspecializacao) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Card(colors = appCardColors()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "NH: $nivelPreview (IQ+AM$nivelRelativo)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(pontosGastos, encantamentoAlvoInput, especializacaoMagiaInput, adicaoForcadaSemPrereq) },
                enabled = (prereqFalha.isNullOrBlank() || adicaoForcadaSemPrereq) &&
                    (!exigeEncantamentoAlvo || encantamentoAlvoInput.isNotBlank()) &&
                    (!(exigeEspecializacao || exigeSubEscolaAnimais) || especializacaoMagiaInput.isNotBlank())
            ) { Text(UiActionLabels.ADICIONAR) }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text(UiActionLabels.CANCELAR) }
        }
    )

    if (confirmarAdicaoForcada) {
        AlertDialog(
            onDismissRequest = { confirmarAdicaoForcada = false },
            title = { Text("CONFIRMAÇÃO") },
            text = {
                Text(
                    "SEU MESTRE AUTORIZOU?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    adicaoForcadaSemPrereq = true
                    confirmarAdicaoForcada = false
                }) { Text("SIM") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarAdicaoForcada = false }) { Text("NAO") }
            }
        )
    }

    if (mostrarDescricaoMagiaPopup) {
        AlertDialog(
            onDismissRequest = { mostrarDescricaoMagiaPopup = false },
            title = { Text(definicao.nome, color = MaterialTheme.colorScheme.primary) },
            text = {
                Text(
                    text = descricaoMagia.ifBlank { "Sem descrição disponível." },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarDescricaoMagiaPopup = false }) { Text(UiActionLabels.FECHAR) }
            }
        )
    }
}

@Composable
fun EditarMagiaDialog(
    magia: MagiaSelecionada,
    personagem: Personagem,
    nivelAptidaoMagica: Int,
    onDismiss: () -> Unit,
    onSave: (MagiaSelecionada) -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var pontosGastos by remember { mutableStateOf(magia.pontosGastos) }
    val descricaoMagia = magia.texto?.trim().orEmpty()
    var mostrarDescricaoMagiaPopup by remember { mutableStateOf(false) }
    
    // Calcula nível preview
    val previewMagia = magia.copy(pontosGastos = pontosGastos)
    val nivelPreview = previewMagia.calcularNivel(personagem, nivelAptidaoMagica)
    val nivelRelativo = previewMagia.getNivelRelativo(personagem, nivelAptidaoMagica)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                magia.nome,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .let {
                        if (descricaoMagia.isNotBlank()) {
                            it.clickable { mostrarDescricaoMagiaPopup = true }
                        } else {
                            it
                        }
                    }
                    .semantics {
                        if (isPraCegoVariant && descricaoMagia.isNotBlank()) {
                            contentDescription = "Nome da magia ${magia.nome}. Toque para abrir descricao."
                        }
                    }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing), modifier = Modifier.verticalScroll(rememberScrollState())) {
                val difNome = magia.dificuldade.sigla
                Text("IQ/$difNome", style = MaterialTheme.typography.bodyMedium)

                HorizontalDivider()
                Text("Pontos Gastos:")
                if (!isPraCegoVariant) {
                    Text(
                        "$pontosGastos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .width(56.dp)
                            .pointerInput(pontosGastos) {
                                var dragAcumulado = 0f
                                val passoPx = 24f
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAcumulado += dragAmount
                                        while (abs(dragAcumulado) >= passoPx) {
                                            pontosGastos = ajustarPontosPreset(
                                                atual = pontosGastos,
                                                incrementar = dragAcumulado < 0f
                                            )
                                            dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                        }
                                    }
                                )
                            },
                        textAlign = TextAlign.Center
                    )
                }
                if (isPraCegoVariant) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        TextButton(
                            onClick = { pontosGastos = ajustarPontosPreset(pontosGastos, incrementar = false) },
                            modifier = Modifier.semantics { contentDescription = "Diminuir pontos gastos da magia" }
                        ) { Text("-") }
                        Text(
                            "$pontosGastos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(horizontal = 8.dp)
                                .semantics { contentDescription = "Pontos gastos atuais da magia: $pontosGastos" }
                        )
                        TextButton(
                            onClick = { pontosGastos = ajustarPontosPreset(pontosGastos, incrementar = true) },
                            modifier = Modifier.semantics { contentDescription = "Aumentar pontos gastos da magia" }
                        ) { Text("+") }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    PONTOS_PRESETS.forEach { pts ->
                        TextButton(
                            onClick = { pontosGastos = pts },
                            modifier = Modifier.padding(horizontal = 1.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("$pts", fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider()
                Card(colors = appCardColors()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "NH: $nivelPreview (IQ+AM$nivelRelativo)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(magia.copy(pontosGastos = pontosGastos)) },
                modifier = Modifier.semantics {
                    if (isPraCegoVariant) contentDescription = "Salvar edição da magia"
                }
            ) {
                Text(UiActionLabels.SALVAR)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics {
                    if (isPraCegoVariant) contentDescription = "Cancelar edição da magia"
                }
            ) { Text(UiActionLabels.CANCELAR) }
        }
    )

    if (mostrarDescricaoMagiaPopup) {
        AlertDialog(
            onDismissRequest = { mostrarDescricaoMagiaPopup = false },
            title = { Text(magia.nome, color = MaterialTheme.colorScheme.primary) },
            text = {
                Text(
                    text = descricaoMagia.ifBlank { "Sem descrição disponível." },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { mostrarDescricaoMagiaPopup = false },
                    modifier = Modifier.semantics {
                        if (isPraCegoVariant) contentDescription = "Fechar descrição da magia"
                    }
                ) { Text(UiActionLabels.FECHAR) }
            }
        )
    }
}
