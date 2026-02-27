package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.viewmodel.FichaViewModel

// === TAB PERICIAS ===

@Composable
private fun BotaoAdicionarPericiaPadrao(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PrimaryActionButton(text = texto, onClick = onClick, modifier = modifier)
}

@Composable
fun TabPericias(viewModel: FichaViewModel) {

    val p = viewModel.personagem

    var showSelecionarPericia by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var editingPericiaIndex by remember { mutableStateOf<Int?>(null) }

    StandardTabColumn(contentSpacing = 6.dp) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotaoAdicionarPericiaPadrao(
                texto = "Adicionar Perícia",
                onClick = { showSelecionarPericia = true },
                modifier = Modifier.weight(1f)
            )
            BotaoAdicionarPericiaPadrao(
                texto = "Criar Perícia",
                onClick = { showCustomDialog = true },
                modifier = Modifier.weight(1f)
            )
        }

        if (p.pericias.isEmpty()) {
            Text(
                "Nenhuma perícia adicionada",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Cards individuais para cada perícia
        p.pericias.forEachIndexed { index, pericia ->

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {

                    PericiaItem(
                        pericia = pericia,
                        nivel = pericia.calcularNivel(p),
                        nivelRelativo = pericia.getNivelRelativo(p),
                        onEdit = { editingPericiaIndex = index },
                        onDelete = { viewModel.removerPericia(index) }
                    )
                }
            }
        }
        ResumoPericiasFooter(
            totalPericias = p.pericias.size,
            pontosPericias = p.pontosPericias
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showSelecionarPericia) {
        SelecionarPericiaDialog(
            viewModel = viewModel,
            onDismiss = { showSelecionarPericia = false }
        )
    }

    if (showCustomDialog) {
        CriarPericiaCustomizadaDialog(
            onDismiss = { showCustomDialog = false },
            onSave = { nome, espec, attr, diff ->
                val novaPericia = PericiaSelecionada(
                    definicaoId = "custom_${System.currentTimeMillis()}",
                    nome = nome,
                    atributoBase = com.gurps.ficha.model.AtributoBase.fromSigla(attr),
                    dificuldade = com.gurps.ficha.model.Dificuldade.fromSigla(diff),
                    pontosGastos = 1,
                    especializacao = espec,
                    exigeEspecializacao = espec.isNotBlank()
                )
                viewModel.adicionarPericiaCustomizada(novaPericia)
                showCustomDialog = false
            }
        )
    }

    editingPericiaIndex?.let { index ->
        EditarPericiaDialog(
            pericia = p.pericias[index],
            personagem = p,
            onDismiss = { editingPericiaIndex = null },
            onSave = {
                viewModel.atualizarPericia(index, it)
                editingPericiaIndex = null
            }
        )
    }
}

@Composable
private fun ResumoPericiasFooter(totalPericias: Int, pontosPericias: Int) {
    SummaryFooterCard(title = "Resumo de Pericias (rodape)") {
        Text("Total de pericias: $totalPericias", style = MaterialTheme.typography.labelSmall)
        Text(
            "Pontos gastos: $pontosPericias",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PericiaItem(
    pericia: PericiaSelecionada,
    nivel: Int,
    nivelRelativo: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 6.dp)
        ) {
            Text(
                pericia.nome +
                        if (pericia.especializacao.isNotBlank())
                            " (${pericia.especializacao})"
                        else "",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${pericia.atributoBase.sigla}/${pericia.dificuldade.sigla} • ${pericia.pontosGastos} pts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "NH $nivel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remover")
            }
        }
    }
}
