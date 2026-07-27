package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import com.gurps.ficha.ui.FullscreenDialogContainer
import com.gurps.ficha.model.PoderDefinicao
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gurps.ficha.model.Poder
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun DialogsPoderes(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit
) {
    val personagem = viewModel.personagem
    var showEditDialog by remember { mutableStateOf<Poder?>(null) }
    var showSelecionarDialog by remember { mutableStateOf(false) }
    var showNovoDialog by remember { mutableStateOf(false) }
    var novoPoderPreenchido by remember { mutableStateOf<Poder?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Poderes do Personagem", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    itemsIndexed(personagem.poderes) { index, poder ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(poder.nome, fontWeight = FontWeight.Bold)
                                    Text("${poder.fonte} - Mod: ${poder.modificadorDePoder}%")
                                }
                                Row {
                                    IconButton(onClick = { showEditDialog = poder }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar Poder")
                                    }
                                    IconButton(onClick = { viewModel.removerPoder(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Apagar Poder")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showSelecionarDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Novo Poder")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Fechar")
                }
            }
        }
    }

    if (showSelecionarDialog) {
        SelecionarPoderDialog(
            viewModel = viewModel,
            onDismiss = { showSelecionarDialog = false },
            onSelect = { definicao ->
                showSelecionarDialog = false
                if (definicao == null) {
                    novoPoderPreenchido = null
                } else {
                    novoPoderPreenchido = Poder(
                        nome = definicao.nome,
                        fonte = "",
                        foco = definicao.foco,
                        modificadorDePoder = definicao.modificadorDePoder
                    )
                }
                showNovoDialog = true
            }
        )
    }

    if (showNovoDialog) {
        PoderEditDialog(
            poderBase = novoPoderPreenchido,
            isNew = true,
            onDismiss = { showNovoDialog = false },
            onSave = { novoPoder ->
                viewModel.adicionarPoder(novoPoder)
                showNovoDialog = false
            }
        )
    }

    showEditDialog?.let { poderEdit ->
        val index = personagem.poderes.indexOfFirst { it.id == poderEdit.id }
        PoderEditDialog(
            poderBase = poderEdit,
            isNew = false,
            onDismiss = { showEditDialog = null },
            onSave = { modPoder ->
                if (index >= 0) {
                    viewModel.atualizarPoder(index, modPoder)
                }
                showEditDialog = null
            }
        )
    }
}

@Composable
fun PoderEditDialog(
    poderBase: Poder?,
    isNew: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (Poder) -> Unit
) {
    var nome by remember(poderBase) { mutableStateOf(poderBase?.nome ?: "") }
    var fonte by remember(poderBase) { mutableStateOf(poderBase?.fonte ?: "") }
    var foco by remember(poderBase) { mutableStateOf(poderBase?.foco ?: "") }
    var modificador by remember(poderBase) { mutableStateOf(poderBase?.modificadorDePoder?.toString() ?: "0") }
    var nivelTalento by remember(poderBase) { mutableStateOf(poderBase?.nivelTalento?.toString() ?: "0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(if (isNew) "Novo Poder" else "Editar Poder", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do Poder") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fonte,
                    onValueChange = { fonte = it },
                    label = { Text("Fonte (ex: Psíquico, Divino)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = foco,
                    onValueChange = { foco = it },
                    label = { Text("Foco (ex: Fogo, Mentes)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = modificador,
                    onValueChange = { modificador = it },
                    label = { Text("Modificador de Poder (%)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val p = poderBase?.copy(
                            nome = nome,
                            fonte = fonte,
                            foco = foco,
                            modificadorDePoder = modificador.toIntOrNull() ?: 0,
                            nivelTalento = nivelTalento.toIntOrNull() ?: 0
                        ) ?: Poder(
                            nome = nome,
                            fonte = fonte,
                            foco = foco,
                            modificadorDePoder = modificador.toIntOrNull() ?: 0,
                            nivelTalento = nivelTalento.toIntOrNull() ?: 0
                        )
                        onSave(p)
                    }) {
                        Text("Salvar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarPoderDialog(
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSelect: (PoderDefinicao?) -> Unit
) {
    var busca by remember { mutableStateOf("") }
    val listaPoderes = viewModel.dataRepository.poderes.filter { 
        busca.isBlank() || it.nome.contains(busca, ignoreCase = true) 
    }

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Selecionar Poder", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                label = { Text("Buscar...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onSelect(null) }, modifier = Modifier.fillMaxWidth()) {
                Text("Criar Poder Personalizado")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("${listaPoderes.size} poderes encontrados (Baseado em GURPS Poderes)", style = MaterialTheme.typography.bodySmall)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(listaPoderes) { definicao ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(definicao) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(definicao.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Fontes: ${definicao.fontesPossiveis}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            Text("Foco: ${definicao.foco}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(definicao.descricao, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Fechar") }
            }
        }
    }
}
