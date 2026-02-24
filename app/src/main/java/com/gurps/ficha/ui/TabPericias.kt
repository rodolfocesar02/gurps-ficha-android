package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
fun TabPericias(viewModel: FichaViewModel) {

    val p = viewModel.personagem

    var showSelecionarPericia by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var editingPericiaIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Header separado (igual Magias)
        SectionCard(
            title = "Perícias [${p.pontosPericias} pts]",
            onAdd = { showSelecionarPericia = true }
        ) {}

        // Lista fora do SectionCard
        // Card do botão
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showCustomDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Criar Própria Perícia")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (p.pericias.isEmpty()) {
                    Text(
                        "Nenhuma perícia adicionada",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

// Cards individuais para cada perícia
        p.pericias.forEachIndexed { index, pericia ->

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    PericiaItem(
                        pericia = pericia,
                        nivel = pericia.calcularNivel(p),
                        onEdit = { editingPericiaIndex = index },
                        onDelete = { viewModel.removerPericia(index) }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
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
fun PericiaItem(
    pericia: PericiaSelecionada,
    nivel: Int,
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
        Column(modifier = Modifier.weight(1f)) {

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

        Text(
            "Nível $nivel",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Remover")
        }
    }
}
