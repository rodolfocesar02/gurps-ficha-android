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
import com.gurps.ficha.BuildConfig
import com.gurps.ficha.model.TecnicaSelecionada
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun TabTecnicas(viewModel: FichaViewModel) {
    val personagem = viewModel.personagem
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    var showSelecionarTecnica by remember { mutableStateOf(false) }
    var editingTecnicaIndex by remember { mutableStateOf<Int?>(null) }

    StandardTabColumn(contentSpacing = 6.dp) {
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

        personagem.tecnicas.forEachIndexed { index, tecnica ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    TecnicaItem(
                        tecnica = tecnica,
                        nivelTecnica = tecnica.calcularNivel(personagem),
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
            tecnica = personagem.tecnicas[index],
            personagem = personagem,
            onDismiss = { editingTecnicaIndex = null },
            onSave = {
                viewModel.atualizarTecnica(index, it)
                editingTecnicaIndex = null
            }
        )
    }
}

@Composable
private fun TecnicaItem(
    tecnica: TecnicaSelecionada,
    nivelTecnica: Int?,
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
                tecnica.nome,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            val base = tecnica.periciaBaseNome.ifBlank { "Perícia base não vinculada" }
            val nivel = nivelTecnica?.let { " • NH $it" } ?: ""
            Text(
                "$base • ${tecnica.dificuldadeRaw} • ${tecnica.pontosGastos} pts$nivel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Remover técnica ${tecnica.nome}")
        }
    }
}
