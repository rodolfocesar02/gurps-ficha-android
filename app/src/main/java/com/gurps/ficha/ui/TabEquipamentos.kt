package com.gurps.ficha.ui

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun TabEquipamentos(viewModel: FichaViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var editingEquipamento by remember { mutableStateOf<Pair<Int, Equipamento>?>(null) }
    val p = viewModel.personagem

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Equipamentos", onAdd = { showDialog = true }) {
            if (p.equipamentos.isEmpty()) {
                Text("Nenhum equipamento adicionado", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                p.equipamentos.forEachIndexed { index, equipamento ->
                    EquipamentoItem(equipamento = equipamento, onEdit = { editingEquipamento = index to equipamento },
                        onDelete = { viewModel.removerEquipamento(index) })
                    if (index < p.equipamentos.lastIndex) Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        SectionCard(title = "Próximos Recursos") {
            Text(
                "Espaço reservado para módulos futuros desta aba.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showDialog) {
        EquipamentoDialog(onDismiss = { showDialog = false },
            onSave = { equipamento -> viewModel.adicionarEquipamento(equipamento); showDialog = false })
    }

    editingEquipamento?.let { (index, equipamento) ->
        EquipamentoDialog(initialEquipamento = equipamento, onDismiss = { editingEquipamento = null },
            onSave = { novoEquipamento -> viewModel.atualizarEquipamento(index, novoEquipamento); editingEquipamento = null })
    }
}

@Composable
fun EquipamentoItem(equipamento: Equipamento, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(equipamento.nome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("${equipamento.quantidade}x | ${equipamento.peso}kg cada | Total: ${equipamento.peso * equipamento.quantidade}kg",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (equipamento.custo > 0) {
                Text("Custo: \$${equipamento.custo * equipamento.quantidade}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            if (equipamento.notas.isNotBlank()) {
                Text(equipamento.notas, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover") }
    }
}

