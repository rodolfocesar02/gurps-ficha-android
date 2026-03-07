package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.model.TecnicaSelecionada
import com.gurps.ficha.viewmodel.FichaViewModel
import java.text.Normalizer

private fun normalizarNomeTecnicaOrdenacao(valor: String): String {
    val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
    return semAcento.lowercase().trim()
}

@Composable
fun TabTecnicas(viewModel: FichaViewModel) {
    val personagem = viewModel.personagem
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var showSelecionarTecnica by remember { mutableStateOf(false) }
    var editingTecnicaIndex by remember { mutableStateOf<Int?>(null) }
    var tecnicaDescricaoDialog by remember { mutableStateOf<TecnicaSelecionada?>(null) }
    val tecnicasOrdenadas = remember(personagem.tecnicas) {
        personagem.tecnicas.withIndex().sortedBy { normalizarNomeTecnicaOrdenacao(it.value.nome) }
    }
    val descricoesTecnicasPorId = remember(viewModel.tecnicasCatalogo) {
        viewModel.tecnicasCatalogo.associate { it.id to it.descricao }
    }

    StandardTabColumn(contentSpacing = 4.dp) {
        if (isPraCegoVariant) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryActionButton(
                    text = "Adicionar Técnica",
                    onClick = { showSelecionarTecnica = true }
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryActionButton(
                    text = "Adicionar Técnica",
                    onClick = { showSelecionarTecnica = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (personagem.tecnicas.isEmpty()) {
            Text(
                "Nenhuma técnica adicionada",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        tecnicasOrdenadas.forEach { indexed ->
            val index = indexed.index
            val tecnica = indexed.value
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = appCardColors()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    TecnicaItem(
                        tecnica = tecnica,
                        nivelTecnica = tecnica.calcularNivel(personagem),
                        onShowDescription = { tecnicaDescricaoDialog = tecnica },
                        onEdit = { editingTecnicaIndex = index },
                        onDelete = { viewModel.removerTecnica(index) }
                    )
                }
            }
        }

        SummaryFooterCard(title = "Resumo de Técnicas") {
            Text("Total de técnicas: ${personagem.tecnicas.size}", style = MaterialTheme.typography.labelSmall)
            Text(
                "Pontos gastos: ${personagem.pontosTecnicas}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showSelecionarTecnica) {
        SelecionarTecnicaDialog(
            viewModel = viewModel,
            onDismiss = { showSelecionarTecnica = false }
        )
    }

    editingTecnicaIndex?.let { index ->
        EditarTecnicaDialog(
            viewModel = viewModel,
            tecnica = personagem.tecnicas[index],
            personagem = personagem,
            onDismiss = { editingTecnicaIndex = null },
            onSave = {
                viewModel.atualizarTecnica(index, it)
                editingTecnicaIndex = null
            }
        )
    }

    tecnicaDescricaoDialog?.let { tecnica ->
        val descricao = descricoesTecnicasPorId[tecnica.definicaoId].orEmpty()
        AlertDialog(
            onDismissRequest = { tecnicaDescricaoDialog = null },
            title = { Text("Descrição: ${tecnica.nome}") },
            text = {
                Text(
                    descricao.ifBlank { "Sem descrição detalhada disponível." },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { tecnicaDescricaoDialog = null }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
private fun TecnicaItem(
    tecnica: TecnicaSelecionada,
    nivelTecnica: Int?,
    onShowDescription: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                if (isPraCegoVariant) {
                    contentDescription = "Técnica ${tecnica.nome}. ${nivelTecnica?.let { "NH $it." } ?: "NH indisponível."} Toque para editar."
                }
            }
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
                tecnica.nome,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onShowDescription() }
            )
            Text(
                "${tecnica.periciaBaseNome.ifBlank { "Perícia base não definida" }} • ${tecnica.pontosGastos} pts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                "NH ${nivelTecnica ?: "-"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remover técnica ${tecnica.nome}")
            }
        }
    }
}
