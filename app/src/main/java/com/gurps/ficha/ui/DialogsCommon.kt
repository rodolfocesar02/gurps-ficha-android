package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import com.gurps.ficha.model.TipoEquipamento
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun MenuDialog(
    onDismiss: () -> Unit,
    onNovaFicha: () -> Unit,
    onSalvar: () -> Unit,
    onCarregar: () -> Unit,
    onImportar: () -> Unit,
    onCompartilhar: () -> Unit,
    onConfigurarDados: () -> Unit,
    onVerificarAtualizacao: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PrimaryActionButton(text = "Nova Ficha", onClick = onNovaFicha, modifier = Modifier.pracegoTraversal(1))
                PrimaryActionButton(text = "Salvar Ficha", onClick = onSalvar, modifier = Modifier.pracegoTraversal(2))
                PrimaryActionButton(text = "Carregar Ficha", onClick = onCarregar, modifier = Modifier.pracegoTraversal(3))
                PrimaryActionButton(text = "Importar Ficha (JSON)", onClick = onImportar, modifier = Modifier.pracegoTraversal(4))
                PrimaryActionButton(text = "Compartilhar Ficha", onClick = onCompartilhar, modifier = Modifier.pracegoTraversal(5))
                PrimaryActionButton(text = "Configurar Dados 3D", onClick = onConfigurarDados, modifier = Modifier.pracegoTraversal(6))
                PrimaryActionButton(text = "Atualizar app", onClick = onVerificarAtualizacao, modifier = Modifier.pracegoTraversal(7))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss, modifier = Modifier.pracegoTraversal(8)) { Text(UiActionLabels.FECHAR) } }
    )
}

@Composable
fun SalvarDialog(
    nomeAtual: String,
    viewModel: FichaViewModel,
    onDismiss: () -> Unit,
    onSalvar: (String) -> Unit
) {
    var name by remember { mutableStateOf(nomeAtual) }
    var showConflictWarning by remember { mutableStateOf(false) }
    var suggestedName by remember { mutableStateOf("") }

    if (showConflictWarning) {
        AlertDialog(
            onDismissRequest = { showConflictWarning = false },
            title = { Text("Ficha já existente") },
            text = { Text("Já existe uma ficha chamada '$name'. Deseja sobrescrevê-la ou criar uma cópia?") },
            confirmButton = {
                TextButton(onClick = { onSalvar(name); showConflictWarning = false }) {
                    Text("Sobrescrever")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    val n = viewModel.gerarSugeridoComIndice(name)
                    onSalvar(n)
                    showConflictWarning = false 
                }) {
                    Text("Criar cópia")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Salvar Ficha") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome da Ficha") }, singleLine = true) },
        confirmButton = { 
            TextButton(
                onClick = { 
                    if (viewModel.verificarConflitoNome(name)) {
                        showConflictWarning = true
                    } else {
                        onSalvar(name) 
                    }
                },
                modifier = Modifier.pracegoTraversal(2)
            ) { Text(UiActionLabels.SALVAR) } 
        },
        dismissButton = { 
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.pracegoTraversal(3)
            ) { Text(UiActionLabels.CANCELAR) } 
        }
    )
}

@Composable
fun CarregarDialog(
    fichasLocais: List<String>,
    fichasNuvem: List<String>,
    onDismiss: () -> Unit,
    onCarregar: (String) -> Unit,
    onCarregarNuvem: (String) -> Unit,
    onExcluir: (String) -> Unit
) {
    val todasFichas = (fichasLocais + fichasNuvem).distinct().sortedBy { it.lowercase() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Carregar Ficha") },
        text = {
            if (todasFichas.isEmpty()) {
                StandardDialogColumn {
                    Text("Nenhuma ficha encontrada.")
                    Text(
                        "Use 'Salvar Ficha' para criar seu primeiro slot.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
            }
            else LazyColumn {
                itemsIndexed(todasFichas) { _, nome ->
                    val estaLocal = fichasLocais.contains(nome)
                    val estaNuvem = fichasNuvem.contains(nome)
                    
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (estaLocal) onCarregar(nome) else onCarregarNuvem(nome) }
                        .padding(vertical = 8.dp)
                        .semantics(mergeDescendants = true) {
                            contentDescription = buildString {
                                append(nome.replace("_", " "))
                                if (estaLocal && estaNuvem) append(". Disponível localmente e sincronizado na nuvem.")
                                else if (estaLocal) append(". Disponível apenas neste aparelho.")
                                else if (estaNuvem) append(". Disponível apenas na nuvem. Clique para baixar.")
                            }
                        },
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.CenterVertically) {
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(if (estaLocal) "📱" else "☁️")
                            Text(" " + nome.replace("_", " "), modifier = Modifier.padding(start = 4.dp))
                            if (estaLocal && estaNuvem) {
                                Text(" ✨", color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary)
                            }
                        }
                        
                        if (estaLocal) {
                            IconButton(onClick = { onExcluir(nome) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir ficha ${nome.replace("_", " ")}") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onDismiss() }) { Text(UiActionLabels.FECHAR) } }
    )
}


/**
 * **Editar / adicionar um item do inventário** (Lote LAYOUT-7).
 *
 * ## O que mudou, e por quê
 *
 * Era um `AlertDialog` estreito com sete campos emoldurados empilhados. O card
 * do seletor, do outro lado, é de tela cheia, com seções e a ficha do livro. A
 * mesma arma tinha duas caras — achado seu nos prints de 08/08.
 *
 * Agora os dois usam a **mesma moldura**: tela cheia, cabeçalho com nome e
 * subtítulo, seções com título, e o rodapé padrão de botões.
 *
 * ⚠️ **A ficha técnica entra em cima, só leitura.** É o mesmo conteúdo do card
 * do seletor — quem abre o lápis quer ver a arma *e* mexer nela. Repetir os
 * números do livro como campo editável seria convidar a estragá-los.
 *
 * @param fichaTecnica a ficha do catálogo, quando o item casa com uma arma.
 *   Nula para item criado à mão, e aí só aparecem os campos.
 */
@Composable
fun EquipamentoDialog(
    initialEquipamento: Equipamento? = null,
    fichaTecnica: com.gurps.ficha.domain.rules.FichaTecnicaDaArma.Ficha? = null,
    onDismiss: () -> Unit,
    onSave: (Equipamento) -> Unit
) {
    var nome by remember { mutableStateOf(initialEquipamento?.nome ?: "") }
    var peso by remember { mutableStateOf(initialEquipamento?.peso?.toString() ?: "0") }
    var custo by remember { mutableStateOf(initialEquipamento?.custo?.toString() ?: "0") }
    var quantidade by remember { mutableStateOf(initialEquipamento?.quantidade?.toString() ?: "1") }
    var notas by remember { mutableStateOf(initialEquipamento?.notas ?: "") }
    var dano by remember { mutableStateOf(initialEquipamento?.armaDanoRaw ?: "") }
    var stMin by remember { mutableStateOf(initialEquipamento?.armaStMinimo?.toString() ?: "") }

    val editando = initialEquipamento != null

    FullscreenDialogContainer(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                if (editando) nome.ifBlank { "Editar Equipamento" } else "Adicionar Equipamento",
                style = UiEstilos.tituloDialogo,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            fichaTecnica?.let {
                Text(it.subtitulo, style = UiEstilos.subtituloDialogo, color = MaterialTheme.colorScheme.outline)
            }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)
            ) {
                fichaTecnica?.let { ficha ->
                    SecaoDoEditor("Ficha do livro")
                    (ficha.destaques + ficha.detalhes).forEach { linha ->
                        LinhaSoLeitura(linha)
                    }
                    if (ficha.observacoes.isNotEmpty()) {
                        ficha.observacoes.forEach { texto ->
                            Text(
                                texto,
                                style = UiEstilos.detalheDoItem,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                SecaoDoEditor(if (fichaTecnica != null) "Seus dados" else "Item")
                OutlinedTextField(
                    value = nome, onValueChange = { nome = it },
                    label = { Text("Nome") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                    OutlinedTextField(
                        value = peso, onValueChange = { peso = it }, label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = custo, onValueChange = { custo = it }, label = { Text("Custo (\$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = quantidade, onValueChange = { quantidade = it }, label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notas, onValueChange = { notas = it },
                    label = { Text("Notas") }, modifier = Modifier.fillMaxWidth()
                )

                SecaoDoEditor("Automação de combate (opcional)")
                Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.DialogContentSpacing)) {
                    OutlinedTextField(
                        value = dano, onValueChange = { dano = it },
                        label = { Text("Dano (ex: GeB+1)") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stMin, onValueChange = { stMin = it }, label = { Text("ST Mín") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(0.6f)
                    )
                }
            }

            AppFileiraDeBotoes {
                AppBotaoSecundario(texto = UiActionLabels.CANCELAR, onClick = onDismiss)
                AppBotaoPrincipal(
                    texto = UiActionLabels.SALVAR,
                    enabled = nome.isNotBlank(),
                    onClick = {
                        val danoFinal = dano.ifBlank { null }
                        val tipoFinal = when {
                            danoFinal != null -> TipoEquipamento.ARMA
                            initialEquipamento != null -> initialEquipamento.tipo
                            else -> TipoEquipamento.GERAL
                        }
                        onSave(
                            (initialEquipamento ?: Equipamento()).copy(
                                nome = nome,
                                peso = peso.toFloatOrNull() ?: 0f,
                                custo = custo.toFloatOrNull() ?: 0f,
                                quantidade = quantidade.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                                notas = notas,
                                tipo = tipoFinal,
                                armaDanoRaw = danoFinal,
                                armaStMinimo = stMin.toIntOrNull()
                            )
                        )
                    }
                )
            }
        }
    }
}

/** O título de uma seção do editor — o mesmo do card de detalhe. */
@Composable
private fun SecaoDoEditor(texto: String) {
    Text(
        texto,
        style = UiEstilos.subtituloDialogo,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(top = 6.dp)
    )
}

/** Uma linha da ficha do livro, no editor: mesma cara do card, sem editar. */
@Composable
private fun LinhaSoLeitura(linha: com.gurps.ficha.domain.rules.FichaTecnicaDaArma.Linha) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = linha.descricaoAcessivel },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(linha.rotulo, style = UiEstilos.detalheDoItem)
            linha.explicacao?.let {
                Text(it, style = UiEstilos.detalheDoItem, color = MaterialTheme.colorScheme.outline)
            }
        }
        Text(
            linha.valor,
            style = UiEstilos.detalheDoItem,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


