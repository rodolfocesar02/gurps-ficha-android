package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gurps.ficha.model.Equipamento

@Composable
fun MenuDialog(
    onDismiss: () -> Unit,
    onNovaFicha: () -> Unit,
    onSalvar: () -> Unit,
    onCarregar: () -> Unit,
    onExportarCompativel: () -> Unit,
    onExportarVersionado: () -> Unit,
    onImportar: () -> Unit,
    onCompartilhar: () -> Unit,
    onVerificarAtualizacao: () -> Unit,
    onMestreIA: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PrimaryActionButton(text = "Nova Ficha", onClick = onNovaFicha, modifier = Modifier.pracegoTraversal(1))
                PrimaryActionButton(text = "Salvar Ficha", onClick = onSalvar, modifier = Modifier.pracegoTraversal(2))
                PrimaryActionButton(text = "Carregar Ficha", onClick = onCarregar, modifier = Modifier.pracegoTraversal(3))
                PrimaryActionButton(text = "Exportar JSON Compatível", onClick = onExportarCompativel, modifier = Modifier.pracegoTraversal(4))
                PrimaryActionButton(text = "Exportar JSON Versionado", onClick = onExportarVersionado, modifier = Modifier.pracegoTraversal(5))
                PrimaryActionButton(text = "Importar Ficha (JSON)", onClick = onImportar, modifier = Modifier.pracegoTraversal(6))
                PrimaryActionButton(text = "Compartilhar Ficha", onClick = onCompartilhar, modifier = Modifier.pracegoTraversal(7))
                PrimaryActionButton(text = "Atualizar app", onClick = onVerificarAtualizacao, modifier = Modifier.pracegoTraversal(8))
                PrimaryActionButton(text = "Mestre IA (Beta)", onClick = onMestreIA, modifier = Modifier.pracegoTraversal(9))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss, modifier = Modifier.pracegoTraversal(9)) { Text(UiActionLabels.FECHAR) } }
    )
}

@Composable
fun SalvarDialog(nomeAtual: String, onDismiss: () -> Unit, onSalvar: (String) -> Unit) {
    var name by remember { mutableStateOf(nomeAtual) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Salvar Ficha") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome da Ficha") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onSalvar(name) }) { Text(UiActionLabels.SALVAR) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
    )
}

@Composable
fun CarregarDialog(fichas: List<String>, onDismiss: () -> Unit, onCarregar: (String) -> Unit, onExcluir: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Carregar Ficha") },
        text = {
            if (fichas.isEmpty()) {
                StandardDialogColumn {
                    Text("Nenhuma ficha salva ainda.")
                    Text(
                        "Volte ao menu e use \"Salvar Ficha\" para criar seu primeiro slot.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
            }
            else LazyColumn {
                itemsIndexed(fichas) { _, nome ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { onCarregar(nome) }.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(nome.replace("_", " "))
                        IconButton(onClick = { onExcluir(nome) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onDismiss() }) { Text(UiActionLabels.FECHAR) } }
    )
}


@Composable
fun EquipamentoDialog(initialEquipamento: Equipamento? = null, onDismiss: () -> Unit, onSave: (Equipamento) -> Unit) {
    var nome by remember { mutableStateOf(initialEquipamento?.nome ?: "") }
    var peso by remember { mutableStateOf(initialEquipamento?.peso?.toString() ?: "0") }
    var custo by remember { mutableStateOf(initialEquipamento?.custo?.toString() ?: "0") }
    var quantidade by remember { mutableStateOf(initialEquipamento?.quantidade?.toString() ?: "1") }
    var notas by remember { mutableStateOf(initialEquipamento?.notas ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialEquipamento != null) "Editar Equipamento" else "Adicionar Equipamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                    OutlinedTextField(value = peso, onValueChange = { peso = it }, label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = custo, onValueChange = { custo = it }, label = { Text("Custo (\$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = quantidade, onValueChange = { quantidade = it }, label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notas, onValueChange = { notas = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nome.isNotBlank()) {
                    val novo = (initialEquipamento ?: Equipamento()).copy(
                        nome = nome,
                        peso = peso.toFloatOrNull() ?: 0f,
                        custo = custo.toFloatOrNull() ?: 0f,
                        quantidade = quantidade.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                        notas = notas
                    )
                    onSave(novo)
                }
            }) { Text(UiActionLabels.SALVAR) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiActionLabels.CANCELAR) } }
    )

}


