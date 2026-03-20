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
import com.gurps.ficha.ui.features.magic.EditarMagiaDialog
import com.gurps.ficha.ui.features.magic.SelecionarMagiaDialog
import com.gurps.ficha.viewmodel.FichaViewModel

// === TAB MAGIAS ===

@Composable
private fun BotaoAdicionarMagiaPadrao(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryActionButton(text = texto, onClick = onClick, modifier = modifier)
}

@Composable
fun TabMagias(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val nivelAptidaoMagica = viewModel.nivelAptidaoMagica

    var showSelecionarMagia by remember { mutableStateOf(false) }
    var editingMagiaIndex by remember { mutableStateOf<Int?>(null) }
    var magiaDescricaoDialog by remember { mutableStateOf<MagiaSelecionada?>(null) }

    StandardTabColumn {

        BotaoAdicionarMagiaPadrao(
            texto = "Adicionar Magia",
            onClick = { showSelecionarMagia = true },
            modifier = Modifier.pracegoTraversal(1)
        )

        // Lista fora do SectionCard
        if (p.magias.isEmpty()) {
            GuidedEmptyState(
                titulo = "Nenhuma magia adicionada ainda.",
                orientacao = "Use \"Adicionar Magia\" para escolher no catálogo e ajustar os pontos."
            )
        } else {
            p.magias.forEachIndexed { index, magia ->
                val definicao = viewModel.dataRepository.getMagiaPorId(magia.definicaoId)
                val amParaEstaMagia = definicao?.let { viewModel.nivelAptidaoMagicaParaMagia(it) } ?: nivelAptidaoMagica
                val failureMsg = definicao?.let { viewModel.prereqFailureForMagia(it) }
                val hasFailure = failureMsg != null

                AppListItemCard(
                    border = if (hasFailure) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
                ) {
                    MagiaItem(
                        magia = magia,
                        nivel = magia.calcularNivel(p, amParaEstaMagia),
                        onShowDescription = { magiaDescricaoDialog = magia },
                        onEdit = { editingMagiaIndex = index },
                        onDelete = { viewModel.removerMagia(index) }
                    )
                    if (failureMsg != null) {
                        Text(
                            failureMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = UiTokens.ItemSpacing)
                        )
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
            nivelAptidaoMagica = viewModel.nivelAptidaoMagicaParaMagia(viewModel.dataRepository.getMagiaPorId(p.magias[index].definicaoId)),
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
            title = { Text(magia.nome) },
            text = {
                StandardDialogColumn {
                    Text(
                        text = magia.texto?.takeIf { it.isNotBlank() }
                            ?: "Descrição não cadastrada para esta magia.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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
