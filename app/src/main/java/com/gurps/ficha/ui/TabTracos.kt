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

private fun nivelExibicaoVantagem(vantagem: VantagemSelecionada): Int {
    return if (vantagem.definicaoId.equals("aptidao_magica", ignoreCase = true)) {
        (vantagem.nivel - 1).coerceAtLeast(0)
    } else {
        vantagem.nivel
    }
}

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
    val desvantagensPorId = remember(viewModel.dataRepository.desvantagens) {
        viewModel.dataRepository.desvantagens.associateBy { it.id }
    }

    StandardTabColumn {
        BotaoAcaoTracosPadrao(
            texto = "Adicionar Vantagem",
            onClick = { showSelecionarVantagem = true }
        )
        if (p.vantagens.isNotEmpty()) {
            Text("Vantagens", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            p.vantagens.forEachIndexed { index, vantagem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = appCardColors()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        VantagemItem(
                            vantagem = vantagem,
                            onEdit = { editingVantagemIndex = index },
                            onDelete = { viewModel.removerVantagem(index) }
                        )
                    }
                }
            }
        }

        BotaoAcaoTracosPadrao(
            texto = "Adicionar Desvantagem",
            onClick = { showSelecionarDesvantagem = true }
        )
        if (p.desvantagens.isNotEmpty()) {
            Text("Desvantagens", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            p.desvantagens.forEachIndexed { index, desvantagem ->
                val permiteAutocontrole = desvantagensPorId[desvantagem.definicaoId]?.usaAutocontroleMental() ?: false
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = appCardColors()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        DesvantagemItem(
                            desvantagem = desvantagem,
                            exibirAutocontrole = permiteAutocontrole,
                            onEdit = { editingDesvantagemIndex = index },
                            onDelete = { viewModel.removerDesvantagem(index) }
                        )
                    }
                }
            }
        }

        BotaoAcaoTracosPadrao(
            texto = "Adicionar Qualidade",
            onClick = { showQualidadeDialog = true }
        )
        if (p.qualidades.isNotEmpty()) {
            Text("Qualidades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            p.qualidades.forEachIndexed { index, qualidade ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = appCardColors()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(qualidade, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { viewModel.removerQualidade(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover qualidade $qualidade")
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
            Text("Peculiaridades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            p.peculiaridades.forEachIndexed { index, peculiaridade ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = appCardColors()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(peculiaridade, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { viewModel.removerPeculiaridade(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover peculiaridade $peculiaridade")
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
        val vantagem = p.vantagens[index]
        val descricaoCatalogo = viewModel.dataRepository.vantagens
            .firstOrNull { it.id == vantagem.definicaoId }
            ?.descricao
            .orEmpty()
        EditarVantagemDialog(
            vantagem = vantagem,
            descricaoCatalogo = descricaoCatalogo,
            onDismiss = { editingVantagemIndex = null },
            onSave = { novaVantagem ->
                viewModel.atualizarVantagem(index, novaVantagem)
                editingVantagemIndex = null
            }
        )
    }

    editingDesvantagemIndex?.let { index ->
        val desvantagem = p.desvantagens[index]
        val descricaoCatalogo = viewModel.dataRepository.desvantagens
            .firstOrNull { it.id == desvantagem.definicaoId }
            ?.descricao
            .orEmpty()
        val permiteAutocontrole = viewModel.dataRepository.desvantagens
            .firstOrNull { it.id == desvantagem.definicaoId }
            ?.usaAutocontroleMental()
            ?: false
        EditarDesvantagemDialog(
            desvantagem = desvantagem,
            permiteAutocontrole = permiteAutocontrole,
            descricaoCatalogo = descricaoCatalogo,
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
    SummaryFooterCard(title = "Resumo de Traços") {
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
    val nivelExibicao = nivelExibicaoVantagem(vantagem)
    val sufixoNivel = if (
        vantagem.definicaoId.equals("aptidao_magica", ignoreCase = true) ||
        nivelExibicao > 1
    ) {
        " (Nível $nivelExibicao)"
    } else {
        ""
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(vantagem.nome + if (vantagem.descricao.isNotBlank()) " (${vantagem.descricao})" else "",
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("${vantagem.custoFinal} pts$sufixoNivel",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar vantagem ${vantagem.nome}") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover vantagem ${vantagem.nome}") }
    }
}

@Composable
fun DesvantagemItem(desvantagem: DesvantagemSelecionada, exibirAutocontrole: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(desvantagem.nome + if (desvantagem.descricao.isNotBlank()) " (${desvantagem.descricao})" else "",
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("${desvantagem.custoFinal} pts" +
                    if (desvantagem.nivel > 1) " (Nível ${desvantagem.nivel})" else "",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            if (exibirAutocontrole) {
                desvantagem.autocontrole?.let { ac ->
                Text("Autocontrole: $ac (${getMultiplicadorAutocontrole(ac)})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar desvantagem ${desvantagem.nome}") }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Remover desvantagem ${desvantagem.nome}") }
    }
}

fun getMultiplicadorAutocontrole(ac: Int): String = when (ac) {
    6 -> "x2"
    9 -> "x1.5"
    12 -> "x1"
    15 -> "x0.5"
    else -> "x1"
}

