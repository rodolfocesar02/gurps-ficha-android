package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.BuildConfig
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
    var showPericiasSuplementares by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var editingPericiaIndex by remember { mutableStateOf<Int?>(null) }
    var periciaDescricaoDialog by remember { mutableStateOf<PericiaSelecionada?>(null) }

    StandardTabColumn {

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SectionSpacing)
        ) {
            BotaoAdicionarPericiaPadrao(
                texto = "Adicionar Perícia",
                onClick = { showSelecionarPericia = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .pracegoTraversal(1)
            )
            BotaoAdicionarPericiaPadrao(
                texto = "Criar Perícia",
                onClick = { showCustomDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .pracegoTraversal(2)
            )
            BotaoAdicionarPericiaPadrao(
                texto = "Perícias Suplementares",
                onClick = { showPericiasSuplementares = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .pracegoTraversal(3)
            )
        }

        if (p.periciasTotais.isEmpty()) {
            GuidedEmptyState(
                titulo = "Nenhuma perícia adicionada ainda.",
                orientacao = "Use \"Adicionar Perícia\" para escolher do catálogo ou \"Criar Perícia\" para cadastro manual."
            )
        }

        // Cards individuais para cada perícia
        p.periciasTotais.forEachIndexed { index, pericia ->
            val isRacial = pericia.definicaoId.startsWith("racial_")
            val definicao = viewModel.dataRepository.getPericiaPorId(pericia.definicaoId)
            val failureMsg = definicao?.let { viewModel.validarPreRequisitosPericia(it) }
            val hasFailure = failureMsg != null

            AppListItemCard(
                border = if (hasFailure) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
            ) {
                Column {
                    PericiaItem(
                        pericia = pericia,
                        personagem = p,
                        nivel = pericia.calcularNivel(p),
                        nivelRelativo = pericia.getNivelRelativo(p),
                        failureMsg = failureMsg,
                        onShowDescription = { periciaDescricaoDialog = pericia },
                        onEdit = { if (!isRacial) editingPericiaIndex = index },
                        onDelete = { if (!isRacial) viewModel.removerPericia(index) }
                    )
                    if (hasFailure) {
                        Text(
                            text = failureMsg ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        ResumoPericiasFooter(
            totalPericias = p.periciasTotais.size,
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

    if (showPericiasSuplementares) {
        PericiasSuplementaresDialog(
            viewModel = viewModel,
            onDismiss = { showPericiasSuplementares = false }
        )
    }

    editingPericiaIndex?.let { index ->
        val pericia = p.pericias[index]
        val regraV2 = viewModel.dataRepository.regraPericiaV2(pericia.definicaoId)
        EditarPericiaDialog(
            pericia = pericia,
            personagem = p,
            descricaoRegra = regraV2?.descricao.orEmpty(),
            preRequisitoRegra = regraV2?.preRequisito?.raw.orEmpty(),
            preDefinidoRegra = regraV2?.preDefinido?.raw.orEmpty(),
            modificadoresRegra = regraV2?.modificadoresRaw.orEmpty(),
            onDismiss = { editingPericiaIndex = null },
            onSave = {
                viewModel.atualizarPericia(index, it)
                editingPericiaIndex = null
            }
        )
    }

    periciaDescricaoDialog?.let { pericia ->
        val regraV2 = viewModel.dataRepository.regraPericiaV2(pericia.definicaoId)
        AlertDialog(
            onDismissRequest = { periciaDescricaoDialog = null },
            title = { Text(pericia.nome) },
            text = {
                StandardDialogColumn {
                    Text(
                        regraV2?.descricao?.takeIf { it.isNotBlank() }
                            ?: "Descrição não cadastrada para esta perícia.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    regraV2?.preRequisito?.raw?.takeIf { it.isNotBlank() }?.let {
                        Text("Pré-requisito: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    regraV2?.preDefinido?.raw?.takeIf { it.isNotBlank() }?.let {
                        Text("Pré-definido: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    regraV2?.modificadoresRaw?.takeIf { it.isNotBlank() }?.let {
                        Text("Modificadores: $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { periciaDescricaoDialog = null }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
private fun ResumoPericiasFooter(totalPericias: Int, pontosPericias: Int) {
    SummaryFooterCard(title = "Resumo de Pericias") {
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
    personagem: com.gurps.ficha.model.Personagem,
    nivel: Int,
    nivelRelativo: String,
    failureMsg: String? = null,
    onShowDescription: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isPraCegoVariant = BuildConfig.UI_VARIANT.equals("pracego", ignoreCase = true)
    val hasFailure = failureMsg != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                if (isPraCegoVariant) {
                    val statusPart = if (hasFailure) ". AVISO: $failureMsg" else ""
                    val origemPart = com.gurps.ficha.ui.features.traits.descricaoAcessivelDeOrigem(
                        com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
                            .getSkillBonusOrigens(personagem, pericia.nome)
                    )
                    contentDescription = "Perícia ${pericia.nome}. NH $nivel$origemPart$statusPart. Toque para editar."
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
                pericia.nome +
                        if (pericia.especializacao.isNotBlank())
                            " (${pericia.especializacao})"
                        else "",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onShowDescription() }
            )
            Text(
                "${pericia.atributoBase.sigla}/${pericia.dificuldade.sigla} • ${pericia.pontosGastos} pts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // De onde vem o bônus, quando há: sem isso o NH muda e nada explica.
            com.gurps.ficha.ui.features.traits.OrigemDoBonusPericia(
                personagem = personagem,
                nomeDaPericia = pericia.nome
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
            if (!pericia.definicaoId.startsWith("racial_")) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover perícia ${pericia.nome}")
                }
            }
        }
    }
}
