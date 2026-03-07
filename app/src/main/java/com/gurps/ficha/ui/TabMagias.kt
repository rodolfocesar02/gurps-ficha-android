package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
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
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.MagiaSelecionada
import com.gurps.ficha.viewmodel.FichaViewModel

// === TAB MAGIAS ===

@Composable
private fun BotaoAdicionarMagiaPadrao(texto: String, onClick: () -> Unit) {
    PrimaryActionButton(text = texto, onClick = onClick)
}

@Composable
fun TabMagias(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val nivelAptidaoMagica = viewModel.nivelAptidaoMagica

    var showSelecionarMagia by remember { mutableStateOf(false) }
    var editingMagiaIndex by remember { mutableStateOf<Int?>(null) }
    var magiaDescricaoDialog by remember { mutableStateOf<MagiaSelecionada?>(null) }

    StandardTabColumn(contentSpacing = 4.dp) {

        BotaoAdicionarMagiaPadrao(
            texto = "Adicionar Magia",
            onClick = { showSelecionarMagia = true }
        )

        // Lista fora do SectionCard
        if (p.magias.isEmpty()) {
            Text(
                "Nenhuma magia adicionada",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            p.magias.forEachIndexed { index, magia ->
                // retrieve definition to inspect prerequisites
                val definicao = viewModel.dataRepository.getMagiaPorId(magia.definicaoId)
                val failureMsg = definicao?.let { viewModel.prereqFailureForMagia(it) }
                val hasFailure = failureMsg != null

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = appCardColors(),
                    border = if (hasFailure) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        MagiaItem(
                            magia = magia,
                            nivel = magia.calcularNivel(p, nivelAptidaoMagica),
                            onShowDescription = { magiaDescricaoDialog = magia },
                            onEdit = { editingMagiaIndex = index },
                            onDelete = { viewModel.removerMagia(index) }
                        )
                        if (failureMsg != null) {
                            Text(
                                failureMsg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        ResumoMagiasFooter(
            totalMagias = p.magias.size,
            pontosMagias = p.pontosMagias,
            nivelAptidaoMagica = nivelAptidaoMagica
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showSelecionarMagia) {
        SelecionarMagiaDialog(
            viewModel = viewModel,
            onDismiss = { showSelecionarMagia = false }
        )
    }

    editingMagiaIndex?.let { index ->
        EditarMagiaDialog(
            magia = p.magias[index],
            personagem = p,
            nivelAptidaoMagica = nivelAptidaoMagica,
            onDismiss = { editingMagiaIndex = null },
            onSave = { atualizada ->
                viewModel.atualizarMagia(index, atualizada)
                editingMagiaIndex = null
            }
        )
    }

    magiaDescricaoDialog?.let { magia ->
        AlertDialog(
            onDismissRequest = { magiaDescricaoDialog = null },
            title = { Text("Descrição: ${magia.nome}") },
            text = {
                Text(
                    text = magia.texto?.takeIf { it.isNotBlank() } ?: "Sem descrição disponível.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { magiaDescricaoDialog = null }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
private fun ResumoMagiasFooter(totalMagias: Int, pontosMagias: Int, nivelAptidaoMagica: Int) {
    SummaryFooterCard(title = "Resumo de Magias") {
        Text("Total de magias: $totalMagias", style = MaterialTheme.typography.labelSmall)
        Text("Pontos gastos: $pontosMagias", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text("IQ + AM: $nivelAptidaoMagica", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MagiaItem(
    magia: MagiaSelecionada,
    nivel: Int,
    onShowDescription: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val nomeExibicao = if (magia.especializacaoMagia.isNullOrBlank()) {
        magia.nome
    } else {
        "${magia.nome} (${magia.especializacaoMagia})"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                if (isPraCegoVariant) {
                    contentDescription = "Magia $nomeExibicao. Nível $nivel. Toque para editar."
                }
            }
            .clickable { onEdit() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                nomeExibicao,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onShowDescription() }
            )
            val difNome = magia.dificuldade.sigla
            
            // Formatando Classe e Escola
            val classeEscola = listOfNotNull(
                "IQ/$difNome",
                magia.classe?.takeIf { it.isNotBlank() },
                magia.escola?.joinToString(" · ")?.takeIf { it.isNotBlank() }
            ).joinToString(" · ")

            Text(
                "$classeEscola | ${magia.pontosGastos} pts",
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
                Icon(Icons.Default.Delete, contentDescription = "Remover magia $nomeExibicao")
            }
        }
    }
}
