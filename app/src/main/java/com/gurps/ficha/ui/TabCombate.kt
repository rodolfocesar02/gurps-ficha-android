package com.gurps.ficha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.viewmodel.ActiveDefense
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
    Button(onClick = onClick, enabled = enabled) { Text(texto) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabCombate(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val defesasAtivas = viewModel.defesasAtivasVisiveis
    val periciasParaApara = viewModel.periciasParaApara
    val periciasParaBloqueio = viewModel.periciasParaBloqueio
    val escudos = viewModel.escudosEquipados

    var showAdicionarAparaDialog by remember { mutableStateOf(false) }
    var showAdicionarBloqueioDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Defesas Ativas") {
            Text(
                "Ajuste bônus temporários e acompanhe o valor final das defesas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                defesasAtivas.forEach { defesa ->
                    DefenseCard(
                        defesa = defesa,
                        onBonusChange = { bonus -> viewModel.atualizarBonusDefesa(defesa.type, bonus) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        SectionCard(title = "Configuração de Defesas") {
            Text(
                "Defina quais perícias e escudo entram no cálculo de Apara e Bloqueio.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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
                valorApara = defesasAtivas.firstOrNull { it.name == "Apara" }?.finalValue,
                onEditar = { showAdicionarAparaDialog = true },
                onRemover = { viewModel.atualizarPericiaApara(null) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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
                valorBloqueio = defesasAtivas.firstOrNull { it.name == "Bloqueio" }?.finalValue,
                onEditar = { showAdicionarBloqueioDialog = true },
                onRemover = {
                    viewModel.atualizarPericiaBloqueio(null)
                    viewModel.atualizarEscudoBloqueio(null)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
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
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Apara")
                }
                Text(
                    text = "Apara ${valorApara ?: "-"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 6.dp)
                )
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
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar Bloqueio")
                }
                Text(
                    text = "Bloq ${valorBloqueio ?: "-"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 6.dp)
                )
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
fun DefenseCard(
    defesa: ActiveDefense,
    onBonusChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp)
    ) {
        Text(
            defesa.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            defesa.finalValue.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Spacer(modifier = Modifier.height(4.dp))

        var textValue by remember(defesa.bonus) {
            mutableStateOf(if (defesa.bonus >= 0) "+${defesa.bonus}" else "${defesa.bonus}")
        }

        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                if (newValue.length > 4) return@OutlinedTextField
                val filtered = newValue.filterIndexed { index, c ->
                    c.isDigit() || ((c == '+' || c == '-') && index == 0)
                }
                if (filtered.isEmpty() || filtered == "+" || filtered == "-") {
                    textValue = filtered
                } else {
                    val parsed = filtered.replace("+", "").toIntOrNull()
                    if (parsed != null) {
                        val bonusSeguro = parsed.coerceIn(-20, 20)
                        textValue = if (bonusSeguro >= 0) "+$bonusSeguro" else "$bonusSeguro"
                        onBonusChange(bonusSeguro)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        Text(
            "Bônus",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

