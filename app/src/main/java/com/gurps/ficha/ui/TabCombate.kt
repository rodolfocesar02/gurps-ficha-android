package com.gurps.ficha.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.viewmodel.DefenseType
import com.gurps.ficha.viewmodel.FichaViewModel

internal fun mensagemBloqueioPendente(
    temPericiaEscudo: Boolean,
    temEscudoEquipado: Boolean
): String? {
    return when {
        temPericiaEscudo && temEscudoEquipado -> null
        !temPericiaEscudo && !temEscudoEquipado ->
            "Sem Bloqueio: adicione perícia de Escudo na aba Perícias e equipe ao menos um escudo."
        !temPericiaEscudo ->
            "Sem Bloqueio: falta perícia de Escudo na aba Perícias."
        else ->
            "Sem Bloqueio: equipe ao menos um escudo na aba Equipamentos."
    }
}

@Composable
private fun BotaoAdicionarCombatePadrao(
    texto: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    PrimaryActionButton(text = texto, onClick = onClick, enabled = enabled)
}

@Composable
fun TabCombate(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val defesasAtivas = viewModel.defesasAtivasVisiveis
    val periciasParaApara = viewModel.periciasParaApara
    val periciasParaBloqueio = viewModel.periciasParaBloqueio
    val escudos = viewModel.escudosEquipados

    val esquivaAtual = defesasAtivas.firstOrNull { it.type == DefenseType.ESQUIVA }
    val aparaAtual = defesasAtivas.firstOrNull { it.type == DefenseType.APARA }
    val bloqueioAtual = defesasAtivas.firstOrNull { it.type == DefenseType.BLOQUEIO }

    var showAdicionarAparaDialog by remember { mutableStateOf(false) }
    var showAdicionarBloqueioDialog by remember { mutableStateOf(false) }
    var showEditarEsquivaDialog by remember { mutableStateOf(false) }
    var showEditarAparaDialog by remember { mutableStateOf(false) }
    var showEditarBloqueioDialog by remember { mutableStateOf(false) }

    StandardTabColumn {
        SectionCard(title = "Configuração de Defesas") {
            Text(
                "Defina quais perícias e escudo entram no cálculo de Apara e Bloqueio.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        EsquivaConfiguradaCard(
            valorEsquiva = esquivaAtual?.finalValue ?: viewModel.esquivaCalculada,
            bonusEsquiva = p.defesasAtivas.bonusManualEsquiva,
            onEditar = { showEditarEsquivaDialog = true }
        )

        BotaoAdicionarCombatePadrao(
            texto = "Adicionar Apara",
            enabled = periciasParaApara.isNotEmpty(),
            onClick = { showAdicionarAparaDialog = true }
        )

        if (periciasParaApara.isEmpty()) {
            Text(
                "Sem Apara: adicione perícias de combate na aba Perícias.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val periciaAparaAtual = periciasParaApara.find { it.definicaoId == p.defesasAtivas.periciaAparaId }
        if (periciaAparaAtual != null) {
            AparaConfiguradaCard(
                personagem = p,
                pericia = periciaAparaAtual,
                valorApara = aparaAtual?.finalValue,
                onEditar = { showEditarAparaDialog = true },
                onRemover = { viewModel.atualizarPericiaApara(null) }
            )
        }

        BotaoAdicionarCombatePadrao(
            texto = "Adicionar Bloqueio",
            enabled = periciasParaBloqueio.isNotEmpty() && escudos.isNotEmpty(),
            onClick = { showAdicionarBloqueioDialog = true }
        )

        if (periciasParaBloqueio.isEmpty() || escudos.isEmpty()) {
            val mensagemPendente = mensagemBloqueioPendente(
                temPericiaEscudo = periciasParaBloqueio.isNotEmpty(),
                temEscudoEquipado = escudos.isNotEmpty()
            )
            if (!mensagemPendente.isNullOrBlank()) {
                Text(
                    mensagemPendente,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val periciaBloqueioAtual = periciasParaBloqueio.find { it.definicaoId == p.defesasAtivas.periciaBloqueioId }
        val escudoAtual = escudos.find {
            it.nome.equals(p.defesasAtivas.escudoSelecionadoNome ?: "", ignoreCase = true)
        }
        if (periciaBloqueioAtual != null && escudoAtual != null) {
            BloqueioConfiguradoCard(
                personagem = p,
                pericia = periciaBloqueioAtual,
                escudo = escudoAtual,
                valorBloqueio = bloqueioAtual?.finalValue,
                onEditar = { showEditarBloqueioDialog = true },
                onRemover = {
                    viewModel.atualizarPericiaBloqueio(null)
                    viewModel.atualizarEscudoBloqueio(null)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showEditarEsquivaDialog) {
        EditarEsquivaBonusDialog(
            bonusAtual = p.defesasAtivas.bonusManualEsquiva,
            onDismiss = { showEditarEsquivaDialog = false },
            onConfirm = { bonus ->
                viewModel.atualizarBonusManualEsquiva(bonus)
                showEditarEsquivaDialog = false
            }
        )
    }

    if (showAdicionarAparaDialog) {
        SelecionarAparaDialog(
            personagem = p,
            pericias = periciasParaApara,
            selecionadaId = p.defesasAtivas.periciaAparaId,
            onDismiss = { showAdicionarAparaDialog = false },
            onSelect = { periciaId ->
                viewModel.atualizarPericiaApara(periciaId)
                showAdicionarAparaDialog = false
            }
        )
    }

    if (showEditarAparaDialog) {
        EditarAparaDialog(
            personagem = p,
            pericias = periciasParaApara,
            periciaSelecionadaId = p.defesasAtivas.periciaAparaId,
            bonusAtual = p.defesasAtivas.bonusManualApara,
            onDismiss = { showEditarAparaDialog = false },
            onConfirm = { periciaId, bonus ->
                viewModel.atualizarPericiaApara(periciaId)
                viewModel.atualizarBonusManualApara(bonus)
                showEditarAparaDialog = false
            }
        )
    }

    if (showAdicionarBloqueioDialog) {
        SelecionarBloqueioDialog(
            personagem = p,
            pericias = periciasParaBloqueio,
            escudos = escudos,
            periciaSelecionadaId = p.defesasAtivas.periciaBloqueioId,
            escudoSelecionadoNome = p.defesasAtivas.escudoSelecionadoNome,
            onDismiss = { showAdicionarBloqueioDialog = false },
            onConfirm = { periciaId, escudoNome ->
                viewModel.atualizarPericiaBloqueio(periciaId)
                viewModel.atualizarEscudoBloqueio(escudoNome)
                showAdicionarBloqueioDialog = false
            }
        )
    }

    if (showEditarBloqueioDialog) {
        EditarBloqueioDialog(
            personagem = p,
            pericias = periciasParaBloqueio,
            escudos = escudos,
            periciaSelecionadaId = p.defesasAtivas.periciaBloqueioId,
            escudoSelecionadoNome = p.defesasAtivas.escudoSelecionadoNome,
            bonusAtual = p.defesasAtivas.bonusManualBloqueio,
            onDismiss = { showEditarBloqueioDialog = false },
            onConfirm = { periciaId, escudoNome, bonus ->
                viewModel.atualizarPericiaBloqueio(periciaId)
                viewModel.atualizarEscudoBloqueio(escudoNome)
                viewModel.atualizarBonusManualBloqueio(bonus)
                showEditarBloqueioDialog = false
            }
        )
    }
}

@Composable
private fun EsquivaConfiguradaCard(
    valorEsquiva: Int,
    bonusEsquiva: Int,
    onEditar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Esquiva", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "Base: Deslocamento + 3 | Bônus ${if (bonusEsquiva >= 0) "+$bonusEsquiva" else "$bonusEsquiva"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = valorEsquiva.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar bônus de Esquiva")
                }
            }
        }
    }
}

@Composable
private fun EditarEsquivaBonusDialog(
    bonusAtual: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var bonus by remember(bonusAtual) { mutableIntStateOf(bonusAtual.coerceIn(-20, 20)) }
    var texto by remember(bonusAtual) { mutableStateOf(if (bonusAtual >= 0) "+$bonusAtual" else "$bonusAtual") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Esquiva") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "A Esquiva usa Deslocamento + 3 como base. Ajuste apenas o bônus manual.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = texto,
                    onValueChange = { novo ->
                        val filtrado = novo.filterIndexed { index, c -> c.isDigit() || ((c == '+' || c == '-') && index == 0) }
                        if (filtrado.isBlank() || filtrado == "+" || filtrado == "-") {
                            texto = filtrado
                        } else {
                            val valor = filtrado.replace("+", "").toIntOrNull() ?: return@OutlinedTextField
                            bonus = valor.coerceIn(-20, 20)
                            texto = if (bonus >= 0) "+$bonus" else "$bonus"
                        }
                    },
                    singleLine = true,
                    label = { Text("Bônus") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        bonus = (bonus - 1).coerceIn(-20, 20)
                        texto = if (bonus >= 0) "+$bonus" else "$bonus"
                    }) { Text("-1") }
                    Button(onClick = {
                        bonus = (bonus + 1).coerceIn(-20, 20)
                        texto = if (bonus >= 0) "+$bonus" else "$bonus"
                    }) { Text("+1") }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(bonus) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun AparaConfiguradaCard(
    personagem: Personagem,
    pericia: PericiaSelecionada,
    valorApara: Int?,
    onEditar: () -> Unit,
    onRemover: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(pericia.nome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "NH ${pericia.calcularNivel(personagem)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (valorApara ?: "-").toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Apara")
                }
                IconButton(onClick = onRemover) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover Apara")
                }
            }
        }
    }
}

@Composable
private fun BloqueioConfiguradoCard(
    personagem: Personagem,
    pericia: PericiaSelecionada,
    escudo: Equipamento,
    valorBloqueio: Int?,
    onEditar: () -> Unit,
    onRemover: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(pericia.nome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "NH ${pericia.calcularNivel(personagem)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Escudo: ${escudo.nome} (DB ${escudo.bonusDefesa})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (valorBloqueio ?: "-").toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Bloqueio")
                }
                IconButton(onClick = onRemover) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover Bloqueio")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelecionarBloqueioDialog(
    personagem: Personagem,
    pericias: List<PericiaSelecionada>,
    escudos: List<Equipamento>,
    periciaSelecionadaId: String?,
    escudoSelecionadoNome: String?,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var selectedPericiaId by remember(periciaSelecionadaId) { mutableStateOf(periciaSelecionadaId) }
    var selectedEscudoNome by remember(escudoSelecionadoNome) { mutableStateOf(escudoSelecionadoNome) }
    var expandedPericia by remember { mutableStateOf(false) }
    var expandedEscudo by remember { mutableStateOf(false) }

    val periciaAtual = pericias.find { it.definicaoId == selectedPericiaId }
    val escudoAtual = escudos.find { it.nome.equals(selectedEscudoNome ?: "", ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Bloqueio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedPericia,
                    onExpandedChange = { expandedPericia = !expandedPericia }
                ) {
                    OutlinedTextField(
                        value = periciaAtual?.let { "${it.nome} (${it.calcularNivel(personagem)})" } ?: "Selecionar Perícia de Escudo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Perícia de Escudo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPericia) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPericia,
                        onDismissRequest = { expandedPericia = false }
                    ) {
                        pericias.forEach { pericia ->
                            DropdownMenuItem(
                                text = { Text("${pericia.nome} (${pericia.calcularNivel(personagem)})") },
                                onClick = {
                                    selectedPericiaId = pericia.definicaoId
                                    expandedPericia = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedEscudo,
                    onExpandedChange = { expandedEscudo = !expandedEscudo }
                ) {
                    OutlinedTextField(
                        value = escudoAtual?.let { "${it.nome} (DB ${it.bonusDefesa})" } ?: "Selecionar Escudo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Escudo para Bloqueio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEscudo) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEscudo,
                        onDismissRequest = { expandedEscudo = false }
                    ) {
                        escudos.forEach { escudo ->
                            DropdownMenuItem(
                                text = { Text("${escudo.nome} (DB ${escudo.bonusDefesa})") },
                                onClick = {
                                    selectedEscudoNome = escudo.nome
                                    expandedEscudo = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val periciaId = selectedPericiaId
                    val escudoNome = selectedEscudoNome
                    if (!periciaId.isNullOrBlank() && !escudoNome.isNullOrBlank()) {
                        onConfirm(periciaId, escudoNome)
                    }
                },
                enabled = !selectedPericiaId.isNullOrBlank() && !selectedEscudoNome.isNullOrBlank()
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun SelecionarAparaDialog(
    personagem: Personagem,
    pericias: List<PericiaSelecionada>,
    selecionadaId: String?,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Apara") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DropdownMenuItem(
                    text = { Text("Nenhuma") },
                    onClick = { onSelect(null) }
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    pericias.forEach { pericia ->
                        val sufixoAtual = if (pericia.definicaoId == selecionadaId) " (atual)" else ""
                        DropdownMenuItem(
                            text = { Text("${pericia.nome} (${pericia.calcularNivel(personagem)})$sufixoAtual") },
                            onClick = { onSelect(pericia.definicaoId) }
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarAparaDialog(
    personagem: Personagem,
    pericias: List<PericiaSelecionada>,
    periciaSelecionadaId: String?,
    bonusAtual: Int,
    onDismiss: () -> Unit,
    onConfirm: (String?, Int) -> Unit
) {
    var selectedPericiaId by remember(periciaSelecionadaId) { mutableStateOf(periciaSelecionadaId) }
    var expandedPericia by remember { mutableStateOf(false) }
    var bonus by remember(bonusAtual) { mutableIntStateOf(bonusAtual.coerceIn(-20, 20)) }
    var textoBonus by remember(bonusAtual) { mutableStateOf(if (bonusAtual >= 0) "+$bonusAtual" else "$bonusAtual") }

    val periciaAtual = pericias.find { it.definicaoId == selectedPericiaId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Apara") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedPericia,
                    onExpandedChange = { expandedPericia = !expandedPericia }
                ) {
                    OutlinedTextField(
                        value = periciaAtual?.let { "${it.nome} (${it.calcularNivel(personagem)})" } ?: "Nenhuma",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Perícia de combate") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPericia) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPericia,
                        onDismissRequest = { expandedPericia = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma") },
                            onClick = {
                                selectedPericiaId = null
                                expandedPericia = false
                            }
                        )
                        pericias.forEach { pericia ->
                            DropdownMenuItem(
                                text = { Text("${pericia.nome} (${pericia.calcularNivel(personagem)})") },
                                onClick = {
                                    selectedPericiaId = pericia.definicaoId
                                    expandedPericia = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = textoBonus,
                    onValueChange = { novo ->
                        val filtrado = novo.filterIndexed { index, c -> c.isDigit() || ((c == '+' || c == '-') && index == 0) }
                        if (filtrado.isBlank() || filtrado == "+" || filtrado == "-") {
                            textoBonus = filtrado
                        } else {
                            val valor = filtrado.replace("+", "").toIntOrNull() ?: return@OutlinedTextField
                            bonus = valor.coerceIn(-20, 20)
                            textoBonus = if (bonus >= 0) "+$bonus" else "$bonus"
                        }
                    },
                    singleLine = true,
                    label = { Text("Bônus") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        bonus = (bonus - 1).coerceIn(-20, 20)
                        textoBonus = if (bonus >= 0) "+$bonus" else "$bonus"
                    }) { Text("-1") }
                    Button(onClick = {
                        bonus = (bonus + 1).coerceIn(-20, 20)
                        textoBonus = if (bonus >= 0) "+$bonus" else "$bonus"
                    }) { Text("+1") }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selectedPericiaId, bonus) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarBloqueioDialog(
    personagem: Personagem,
    pericias: List<PericiaSelecionada>,
    escudos: List<Equipamento>,
    periciaSelecionadaId: String?,
    escudoSelecionadoNome: String?,
    bonusAtual: Int,
    onDismiss: () -> Unit,
    onConfirm: (String?, String?, Int) -> Unit
) {
    var selectedPericiaId by remember(periciaSelecionadaId) { mutableStateOf(periciaSelecionadaId) }
    var selectedEscudoNome by remember(escudoSelecionadoNome) { mutableStateOf(escudoSelecionadoNome) }
    var expandedPericia by remember { mutableStateOf(false) }
    var expandedEscudo by remember { mutableStateOf(false) }
    var bonus by remember(bonusAtual) { mutableIntStateOf(bonusAtual.coerceIn(-20, 20)) }
    var textoBonus by remember(bonusAtual) { mutableStateOf(if (bonusAtual >= 0) "+$bonusAtual" else "$bonusAtual") }

    val periciaAtual = pericias.find { it.definicaoId == selectedPericiaId }
    val escudoAtual = escudos.find { it.nome.equals(selectedEscudoNome ?: "", ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Bloqueio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedPericia,
                    onExpandedChange = { expandedPericia = !expandedPericia }
                ) {
                    OutlinedTextField(
                        value = periciaAtual?.let { "${it.nome} (${it.calcularNivel(personagem)})" } ?: "Nenhuma",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Perícia de Escudo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPericia) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPericia,
                        onDismissRequest = { expandedPericia = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma") },
                            onClick = {
                                selectedPericiaId = null
                                expandedPericia = false
                            }
                        )
                        pericias.forEach { pericia ->
                            DropdownMenuItem(
                                text = { Text("${pericia.nome} (${pericia.calcularNivel(personagem)})") },
                                onClick = {
                                    selectedPericiaId = pericia.definicaoId
                                    expandedPericia = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedEscudo,
                    onExpandedChange = { expandedEscudo = !expandedEscudo }
                ) {
                    OutlinedTextField(
                        value = escudoAtual?.let { "${it.nome} (DB ${it.bonusDefesa})" } ?: "Nenhum",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Escudo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEscudo) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEscudo,
                        onDismissRequest = { expandedEscudo = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhum") },
                            onClick = {
                                selectedEscudoNome = null
                                expandedEscudo = false
                            }
                        )
                        escudos.forEach { escudo ->
                            DropdownMenuItem(
                                text = { Text("${escudo.nome} (DB ${escudo.bonusDefesa})") },
                                onClick = {
                                    selectedEscudoNome = escudo.nome
                                    expandedEscudo = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = textoBonus,
                    onValueChange = { novo ->
                        val filtrado = novo.filterIndexed { index, c -> c.isDigit() || ((c == '+' || c == '-') && index == 0) }
                        if (filtrado.isBlank() || filtrado == "+" || filtrado == "-") {
                            textoBonus = filtrado
                        } else {
                            val valor = filtrado.replace("+", "").toIntOrNull() ?: return@OutlinedTextField
                            bonus = valor.coerceIn(-20, 20)
                            textoBonus = if (bonus >= 0) "+$bonus" else "$bonus"
                        }
                    },
                    singleLine = true,
                    label = { Text("Bônus") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        bonus = (bonus - 1).coerceIn(-20, 20)
                        textoBonus = if (bonus >= 0) "+$bonus" else "$bonus"
                    }) { Text("-1") }
                    Button(onClick = {
                        bonus = (bonus + 1).coerceIn(-20, 20)
                        textoBonus = if (bonus >= 0) "+$bonus" else "$bonus"
                    }) { Text("+1") }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selectedPericiaId, selectedEscudoNome, bonus) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
