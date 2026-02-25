package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.model.ArmaduraCatalogoItem
import com.gurps.ficha.model.ArmaCatalogoItem
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.EscudoCatalogoItem
import com.gurps.ficha.model.TipoEquipamento
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun TabEquipamentos(viewModel: FichaViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var showArmaDialog by remember { mutableStateOf(false) }
    var showEscudoDialog by remember { mutableStateOf(false) }
    var showArmaduraDialog by remember { mutableStateOf(false) }
    var armaduraPendenteConfiguracao by remember { mutableStateOf<ArmaduraCatalogoItem?>(null) }
    var editingEquipamento by remember { mutableStateOf<Pair<Int, Equipamento>?>(null) }

    val p = viewModel.personagem
    val errosCarga = viewModel.errosCargaCatalogos
    val equipamentosComIndice = p.equipamentos.withIndex().toList()
    val equipamentosManuais = equipamentosComIndice.filter { it.value.tipo == TipoEquipamento.GERAL }
    val armasEquipadas = equipamentosComIndice.filter { it.value.tipo == TipoEquipamento.ARMA }
    val escudosEquipados = equipamentosComIndice.filter { it.value.tipo == TipoEquipamento.ESCUDO }
    val armadurasEquipadas = equipamentosComIndice.filter { it.value.tipo == TipoEquipamento.ARMADURA }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (errosCarga.isNotEmpty()) {
            SectionCard(title = "Aviso de Catálogo") {
                Text(
                    "Alguns catálogos não foram carregados corretamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                errosCarga.forEach { (catalogo, mensagem) ->
                    Text(
                        "- $catalogo: $mensagem",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        SectionCard(title = "Equipamentos Manuais", onAdd = { showDialog = true }) {
            if (equipamentosManuais.isEmpty()) {
                Text("Nenhum item manual adicionado", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                equipamentosManuais.forEachIndexed { idx, entry ->
                    EquipamentoItem(
                        equipamento = entry.value,
                        onEdit = { editingEquipamento = entry.index to entry.value },
                        onDelete = { viewModel.removerEquipamento(entry.index) },
                        viewModel = viewModel
                    )
                    if (idx < equipamentosManuais.lastIndex) Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        SectionCard(title = "Armas") {
            Text(
                "Itens equipados: ${armasEquipadas.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showArmaDialog = true }) { Text("Adicionar Arma") }
            Spacer(modifier = Modifier.height(8.dp))
            if (armasEquipadas.isEmpty()) {
                Text("Nenhuma arma selecionada", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                armasEquipadas.forEachIndexed { idx, entry ->
                    EquipamentoArmaItem(
                        equipamento = entry.value,
                        onEdit = { editingEquipamento = entry.index to entry.value },
                        onDelete = { viewModel.removerEquipamento(entry.index) },
                        viewModel = viewModel
                    )
                    if (idx < armasEquipadas.lastIndex) Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        SectionCard(title = "Escudos") {
            Text(
                "Itens equipados: ${escudosEquipados.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showEscudoDialog = true }) { Text("Adicionar Escudo") }
            Spacer(modifier = Modifier.height(8.dp))
            if (escudosEquipados.isEmpty()) {
                Text("Nenhum escudo selecionado", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                escudosEquipados.forEachIndexed { idx, entry ->
                    EquipamentoItem(
                        equipamento = entry.value,
                        onEdit = { editingEquipamento = entry.index to entry.value },
                        onDelete = { viewModel.removerEquipamento(entry.index) },
                        viewModel = viewModel
                    )
                    if (idx < escudosEquipados.lastIndex) Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        SectionCard(title = "Armaduras") {
            Text(
                "Itens selecionados: ${armadurasEquipadas.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showArmaduraDialog = true }) { Text("Adicionar Armadura") }
            Text(
                "Selecao por NT e Local (regra do livro).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (armadurasEquipadas.isEmpty()) {
                Text("Nenhuma armadura selecionada", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                armadurasEquipadas.forEachIndexed { idx, entry ->
                    ArmaduraSelecionadaItem(
                        equipamento = entry.value,
                        onEdit = { editingEquipamento = entry.index to entry.value },
                        onDelete = { viewModel.removerEquipamento(entry.index) }
                    )
                    if (idx < armadurasEquipadas.lastIndex) Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        ResumoEquipamentosFooter(viewModel = viewModel)
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showDialog) {
        EquipamentoDialog(
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.adicionarEquipamento(it)
                showDialog = false
            }
        )
    }

    editingEquipamento?.let { (index, equipamento) ->
        EquipamentoDialog(
            initialEquipamento = equipamento,
            onDismiss = { editingEquipamento = null },
            onSave = {
                viewModel.atualizarEquipamento(index, it)
                editingEquipamento = null
            }
        )
    }

    if (showArmaDialog) {
        SelecionarArmaEquipamentoDialog(
            viewModel = viewModel,
            onDismiss = { showArmaDialog = false },
            onSelect = {
                viewModel.adicionarEquipamentoArma(it)
                showArmaDialog = false
            }
        )
    }

    if (showEscudoDialog) {
        SelecionarEscudoEquipamentoDialog(
            viewModel = viewModel,
            onDismiss = { showEscudoDialog = false },
            onSelect = {
                viewModel.adicionarEquipamentoEscudo(it)
                showEscudoDialog = false
            }
        )
    }

    if (showArmaduraDialog) {
        SelecionarArmaduraEquipamentoDialog(
            viewModel = viewModel,
            onDismiss = { showArmaduraDialog = false },
            onSelect = {
                armaduraPendenteConfiguracao = it
                showArmaduraDialog = false
            }
        )
    }

    armaduraPendenteConfiguracao?.let { armadura ->
        ConfigurarArmaduraDialog(
            armadura = armadura,
            onDismiss = { armaduraPendenteConfiguracao = null },
            onConfirm = { locais ->
                viewModel.adicionarEquipamentoArmaduraComSelecao(armadura, locais)
                armaduraPendenteConfiguracao = null
            }
        )
    }
}

@Composable
private fun ArmaduraSelecionadaItem(
    equipamento: Equipamento,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val rd = equipamento.rdArmaduraExibicao().orEmpty()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(equipamento.nome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                if (rd.isNotBlank()) "RD: $rd" else "RD: -",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover") }
    }
}

@Composable
private fun ResumoEquipamentosFooter(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "Resumo de Equipamentos (rodape)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("ST atual: ${p.forca}", style = MaterialTheme.typography.labelSmall)
            Text("Peso total: ${viewModel.pesoTotal} kg", style = MaterialTheme.typography.labelSmall)
            Text(
                "Custo total: $${viewModel.custoTotalEquipamentos}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EquipamentoArmaItem(equipamento: Equipamento, onEdit: () -> Unit, onDelete: () -> Unit, viewModel: FichaViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(equipamento.nome, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            val danoRaw = equipamento.armaDanoRaw
            if (!danoRaw.isNullOrBlank()) {
                val danoCalc = viewModel.calcularDanoArmaComSt(danoRaw)
                Text(
                    "Dano: $danoCalc",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            } else {
                Text(
                    "Dano: -",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover") }
    }
}

@Composable
fun EquipamentoItem(equipamento: Equipamento, onEdit: () -> Unit, onDelete: () -> Unit, viewModel: FichaViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(equipamento.nome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                "${equipamento.quantidade}x | ${equipamento.peso}kg cada | Total: ${equipamento.peso * equipamento.quantidade}kg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (equipamento.custo > 0) {
                Text(
                    "Custo: $${equipamento.custo * equipamento.quantidade}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (equipamento.notas.isNotBlank()) {
                Text(
                    equipamento.notas,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val danoRaw = equipamento.armaDanoRaw
            if (!danoRaw.isNullOrBlank()) {
                val danoCalc = viewModel.calcularDanoArmaComSt(danoRaw)
                Text(
                    "Dano: $danoRaw -> $danoCalc",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover") }
    }
}

@Composable
fun SelecionarArmaEquipamentoDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (ArmaCatalogoItem) -> Unit
) {
    val stAtual = viewModel.personagem.forca
    val armas = viewModel.armasEquipamentosFiltradas

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Arma") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ST do personagem: $stAtual")
                OutlinedTextField(
                    value = viewModel.buscaArmaEquipamento,
                    onValueChange = { viewModel.atualizarBuscaArmaEquipamento(it) },
                    label = { Text("Buscar por nome ou grupo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TipoArmaFiltroChip(
                        label = "Todas",
                        selected = viewModel.filtroTipoArmaEquipamento == null,
                        onClick = { viewModel.atualizarFiltroTipoArmaEquipamento(null) }
                    )
                    TipoArmaFiltroChip(
                        label = "Corpo a corpo",
                        selected = viewModel.filtroTipoArmaEquipamento == "corpo_a_corpo",
                        onClick = { viewModel.atualizarFiltroTipoArmaEquipamento("corpo_a_corpo") }
                    )
                    TipoArmaFiltroChip(
                        label = "Distancia",
                        selected = viewModel.filtroTipoArmaEquipamento == "distancia",
                        onClick = { viewModel.atualizarFiltroTipoArmaEquipamento("distancia") }
                    )
                    TipoArmaFiltroChip(
                        label = "Armas de Fogo",
                        selected = viewModel.filtroTipoArmaEquipamento == "armas_de_fogo",
                        onClick = { viewModel.atualizarFiltroTipoArmaEquipamento("armas_de_fogo") }
                    )
                }
                if (armas.isEmpty()) {
                    Text(
                        "Nenhuma arma disponivel para o ST atual e filtros aplicados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(armas, key = { it.id }) { arma ->
                            ArmaItemSelecao(
                                arma = arma,
                                danoCalculado = viewModel.calcularDanoArmaComSt(arma.danoRaw),
                                onClick = { onSelect(arma) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@Composable
private fun TipoArmaFiltroChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
}

private fun formatarTagArmadura(tag: String): String {
    val limpa = tag.trim()
    if (limpa.isBlank()) return limpa
    return when {
        limpa.startsWith("rd:", ignoreCase = true) ->
            "RD ${limpa.substringAfter(":").replace('_', ' ')}"
        limpa.startsWith("obs:", ignoreCase = true) ->
            "Obs ${limpa.substringAfter(":")}"
        else -> limpa.replace('_', ' ')
    }
}

@Composable
private fun ArmaItemSelecao(arma: ArmaCatalogoItem, danoCalculado: String, onClick: () -> Unit) {
    val tipoLabel = when (arma.tipoCombate) {
        "corpo_a_corpo" -> "Corpo a corpo"
        "armas_de_fogo" -> "Armas de Fogo"
        else -> "Distancia"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(arma.nome, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(
            "${arma.grupo} | ST ${arma.stMinimo ?: "-"} | $tipoLabel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Dano: ${arma.danoRaw} -> $danoCalculado | Custo: $${arma.custoBase ?: 0f} | Peso: ${arma.pesoBaseKg ?: 0f} kg",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )
        Divider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun SelecionarEscudoEquipamentoDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (EscudoCatalogoItem) -> Unit
) {
    val escudos = viewModel.escudosEquipamentosFiltrados
    val stAtual = viewModel.personagem.forca
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Escudo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ST do personagem: $stAtual")
                OutlinedTextField(
                    value = viewModel.buscaEscudoEquipamento,
                    onValueChange = { viewModel.atualizarBuscaEscudoEquipamento(it) },
                    label = { Text("Buscar escudo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (escudos.isEmpty()) {
                    Text(
                        "Nenhum escudo disponivel para o ST atual.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(escudos, key = { it.id }) { escudo ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(escudo) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(escudo.nome, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(
                                    "DB ${escudo.db} | Custo: $${escudo.custo ?: 0f} | Peso: ${escudo.pesoKg ?: 0f} kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Divider(modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

private val LOCAIS_ARMADURA = listOf(
    "cabeca" to "Cabeca",
    "corpo" to "Corpo",
    "pescoco" to "Pescoco",
    "tronco" to "Tronco",
    "virilha" to "Virilha",
    "membros" to "Membros",
    "bracos" to "Bracos",
    "pernas" to "Pernas",
    "pes" to "Pes",
    "maos" to "Maos",
    "traje_completo" to "Traje Completo"
)

@Composable
private fun SelecionarArmaduraEquipamentoDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (ArmaduraCatalogoItem) -> Unit
) {
    val armaduras = viewModel.armadurasEquipamentosFiltradas
    val tags = viewModel.tagsArmadurasEquipamentos
    val filtrosAtivos = viewModel.buscaArmaduraEquipamento.isNotBlank() ||
        viewModel.filtroLocalArmaduraEquipamento != null ||
        viewModel.filtroNtArmaduraEquipamento != null ||
        viewModel.filtroTagArmaduraEquipamento != null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Armadura") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Use Local, depois NT e Tag para refinar mais rapido.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = viewModel.buscaArmaduraEquipamento,
                    onValueChange = { viewModel.atualizarBuscaArmaduraEquipamento(it) },
                    label = { Text("Buscar armadura") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TipoArmaFiltroChip(
                        label = "Local: Todos",
                        selected = viewModel.filtroLocalArmaduraEquipamento == null,
                        onClick = { viewModel.atualizarFiltroLocalArmaduraEquipamento(null) }
                    )
                    LOCAIS_ARMADURA.forEach { (id, label) ->
                        TipoArmaFiltroChip(
                            label = label,
                            selected = viewModel.filtroLocalArmaduraEquipamento == id,
                            onClick = { viewModel.atualizarFiltroLocalArmaduraEquipamento(id) }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TipoArmaFiltroChip(
                        label = "NT: Todas",
                        selected = viewModel.filtroNtArmaduraEquipamento == null,
                        onClick = { viewModel.atualizarFiltroNtArmaduraEquipamento(null) }
                    )
                    for (nt in 0..10) {
                        TipoArmaFiltroChip(
                            label = "NT $nt",
                            selected = viewModel.filtroNtArmaduraEquipamento == nt,
                            onClick = { viewModel.atualizarFiltroNtArmaduraEquipamento(nt) }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TipoArmaFiltroChip(
                        label = "Tag: Todas",
                        selected = viewModel.filtroTagArmaduraEquipamento == null,
                        onClick = { viewModel.atualizarFiltroTagArmaduraEquipamento(null) }
                    )
                    tags.forEach { tag ->
                        TipoArmaFiltroChip(
                            label = formatarTagArmadura(tag),
                            selected = viewModel.filtroTagArmaduraEquipamento == tag,
                            onClick = { viewModel.atualizarFiltroTagArmaduraEquipamento(tag) }
                        )
                    }
                }
                if (filtrosAtivos) {
                    TextButton(
                        onClick = { viewModel.limparFiltrosArmaduraEquipamento() },
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("Limpar filtros") }
                }
                Text(
                    "Resultados: ${armaduras.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (armaduras.isEmpty()) {
                    Text(
                        "Nenhuma armadura encontrada para o filtro aplicado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(armaduras, key = { it.id }) { armadura ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(armadura) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(armadura.nome, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(
                                    "NT ${armadura.nt ?: "-"} | RD ${armadura.rd} | Peso ${armadura.pesoBaseKg ?: 0f} kg | Custo $${armadura.custoBase ?: 0f}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Local: ${armadura.local}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val tagsVisiveis = armadura.tags
                                    .filterNot { it.startsWith("local:", ignoreCase = true) }
                                    .filterNot { it.startsWith("local_exp:", ignoreCase = true) }
                                    .filterNot { it.startsWith("nt:", ignoreCase = true) }
                                    .filterNot { it.startsWith("tipo:", ignoreCase = true) }
                                    .take(4)
                                if (tagsVisiveis.isNotEmpty()) {
                                    Text(
                                        "Tags: ${tagsVisiveis.joinToString(", ") { formatarTagArmadura(it) }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Divider(modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@Composable
private fun ConfigurarArmaduraDialog(
    armadura: ArmaduraCatalogoItem,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val locais = remember(armadura.id, armadura.local, armadura.componentes) {
        val locaisBase = armadura.local
            .split(Regex("[,;/|]"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val viaComponentes = armadura.componentes.flatMap { c ->
            c.local
                .split(Regex("[,;/|]"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }
        (locaisBase + viaComponentes).distinct().ifEmpty { listOf("corpo") }
    }
    val conjuntoObrigatorio = remember(armadura.id, armadura.nome, armadura.componentes) {
        armadura.componentes.isNotEmpty() && armadura.nome.contains("+")
    }

    var selecionados by remember(armadura.id, conjuntoObrigatorio) { mutableStateOf(locais.toSet()) }

    val divisor = selecionados.size.coerceAtLeast(1).toFloat()
    val custoPrevisto = armadura.custoBase ?: 0f
    val pesoPrevisto = armadura.pesoBaseKg ?: 0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Armadura") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(armadura.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (conjuntoObrigatorio)
                        "Este conjunto adiciona todas as partes automaticamente."
                    else
                        "Escolha os locais para adicionar no inventario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    locais.forEach { local ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !conjuntoObrigatorio) {
                                    selecionados = if (selecionados.contains(local)) selecionados - local else selecionados + local
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selecionados.contains(local),
                                enabled = !conjuntoObrigatorio,
                                onCheckedChange = { checked ->
                                    selecionados = if (checked) selecionados + local else selecionados - local
                                }
                            )
                            Text(local, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Divider()
                Text(
                    "Previsto total -> Custo: $${custoPrevisto} | Peso: ${pesoPrevisto} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Por local selecionado: Custo ${String.format("%.2f", custoPrevisto / divisor)} | Peso ${String.format("%.2f", pesoPrevisto / divisor)} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selecionados.toList()) },
                enabled = selecionados.isNotEmpty()
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
