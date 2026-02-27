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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.VantagemSelecionada
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
private fun BotaoAcaoTracosPadrao(texto: String, onClick: () -> Unit) {
    PrimaryActionButton(text = texto, onClick = onClick)
}

@Composable
fun TabTracos(viewModel: FichaViewModel) {
    var showSelecionarVantagem by remember { mutableStateOf(false) }
    var showSelecionarDesvantagem by remember { mutableStateOf(false) }
    var showQualidadeDialog by remember { mutableStateOf(false) }
    var showPeculiaridadeDialog by remember { mutableStateOf(false) }
    var editingVantagemIndex by remember { mutableStateOf<Int?>(null) }
    var editingDesvantagemIndex by remember { mutableStateOf<Int?>(null) }

    val p = viewModel.personagem

    StandardTabColumn {
        BotaoAcaoTracosPadrao(
            texto = "Adicionar Vantagem",
            onClick = { showSelecionarVantagem = true }
        )
        if (p.vantagens.isNotEmpty()) {
            SectionCard(title = "Vantagens") {
                p.vantagens.forEachIndexed { index, vantagem ->
                    VantagemItem(vantagem = vantagem,
                        onEdit = { editingVantagemIndex = index },
                        onDelete = { viewModel.removerVantagem(index) })
                    if (index < p.vantagens.lastIndex) Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        BotaoAcaoTracosPadrao(
            texto = "Adicionar Desvantagem",
            onClick = { showSelecionarDesvantagem = true }
        )
        if (p.desvantagens.isNotEmpty()) {
            SectionCard(title = "Desvantagens") {
                p.desvantagens.forEachIndexed { index, desvantagem ->
                    DesvantagemItem(desvantagem = desvantagem,
                        onEdit = { editingDesvantagemIndex = index },
                        onDelete = { viewModel.removerDesvantagem(index) })
                    if (index < p.desvantagens.lastIndex) Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }

        BotaoAcaoTracosPadrao(
            texto = "Adicionar Qualidade",
            onClick = { showQualidadeDialog = true }
        )
        if (p.qualidades.isNotEmpty()) {
            SectionCard(title = "Qualidades") {
                p.qualidades.forEachIndexed { index, qualidade ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("• $qualidade", modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.removerQualidade(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover")
                        }
                    }
                }
            }
        }

        BotaoAcaoTracosPadrao(
            texto = "Adicionar Peculiaridade",
            onClick = { showPeculiaridadeDialog = true }
        )
        if (p.peculiaridades.isNotEmpty()) {
            SectionCard(title = "Peculiaridades") {
                p.peculiaridades.forEachIndexed { index, peculiaridade ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("• $peculiaridade", modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.removerPeculiaridade(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover")
                        }
                    }
                }
            }
        }

        ResumoTracosFooter(
            totalItens = p.vantagens.size + p.desvantagens.size + p.qualidades.size + p.peculiaridades.size,
            pontosTracos = p.pontosVantagens + p.pontosDesvantagens + p.pontosQualidades + p.pontosPeculiaridades
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showSelecionarVantagem) {
        SelecionarVantagemDialog(viewModel = viewModel, onDismiss = { showSelecionarVantagem = false })
    }

    if (showSelecionarDesvantagem) {
        SelecionarDesvantagemDialog(viewModel = viewModel, onDismiss = { showSelecionarDesvantagem = false })
    }

    if (showQualidadeDialog) {
        QualidadeDialog(onDismiss = { showQualidadeDialog = false },
            onSave = { texto -> viewModel.adicionarQualidade(texto); showQualidadeDialog = false })
    }

    if (showPeculiaridadeDialog) {
        PeculiaridadeDialog(onDismiss = { showPeculiaridadeDialog = false },
            onSave = { texto -> viewModel.adicionarPeculiaridade(texto); showPeculiaridadeDialog = false })
    }

    editingVantagemIndex?.let { index ->
        EditarVantagemDialog(
            vantagem = p.vantagens[index],
            onDismiss = { editingVantagemIndex = null },
            onSave = { novaVantagem ->
                viewModel.atualizarVantagem(index, novaVantagem)
                editingVantagemIndex = null
            }
        )
    }

    editingDesvantagemIndex?.let { index ->
        EditarDesvantagemDialog(
            desvantagem = p.desvantagens[index],
            onDismiss = { editingDesvantagemIndex = null },
            onSave = { novaDesvantagem ->
                viewModel.atualizarDesvantagem(index, novaDesvantagem)
                editingDesvantagemIndex = null
            }
        )
    }
}

@Composable
private fun ResumoTracosFooter(totalItens: Int, pontosTracos: Int) {
    SummaryFooterCard(title = "Resumo de Traços (rodape)") {
        Text("Total de traços: $totalItens", style = MaterialTheme.typography.labelSmall)
        Text(
            "Pontos gastos: $pontosTracos",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun VantagemItem(vantagem: VantagemSelecionada, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(vantagem.nome + if (vantagem.descricao.isNotBlank()) " (${vantagem.descricao})" else "",
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("${vantagem.custoFinal} pts" + if (vantagem.nivel > 1) " (Nível ${vantagem.nivel})" else "",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover") }
    }
}

@Composable
fun DesvantagemItem(desvantagem: DesvantagemSelecionada, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(desvantagem.nome + if (desvantagem.descricao.isNotBlank()) " (${desvantagem.descricao})" else "",
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("${desvantagem.custoFinal} pts" +
                    if (desvantagem.nivel > 1) " (Nível ${desvantagem.nivel})" else "",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            desvantagem.autocontrole?.let { ac ->
                Text("Autocontrole: $ac (${getMultiplicadorAutocontrole(ac)})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover") }
    }
}

fun getMultiplicadorAutocontrole(ac: Int): String = when (ac) {
    6 -> "x2"
    9 -> "x1.5"
    12 -> "x1"
    15 -> "x0.5"
    else -> "x1"
}

