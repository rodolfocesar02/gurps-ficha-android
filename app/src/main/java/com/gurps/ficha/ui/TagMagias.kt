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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.MagiaSelecionada
import com.gurps.ficha.viewmodel.FichaViewModel

// === TAB MAGIAS ===

@Composable
fun TabMagias(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val nivelAptidaoMagica = viewModel.nivelAptidaoMagica

    var showSelecionarMagia by remember { mutableStateOf(false) }
    var editingMagiaIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Header separado
        SectionCard(
            title = "Magias [${p.pontosMagias} pts] (IQ + AM $nivelAptidaoMagica)",
            onAdd = { showSelecionarMagia = true }
        ) {}

        // Lista fora do SectionCard
        if (p.magias.isEmpty()) {
            Text(
                "Nenhuma magia adicionada",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    p.magias.forEachIndexed { index, magia ->
                        MagiaItem(
                            magia = magia,
                            nivel = magia.calcularNivel(p, nivelAptidaoMagica),
                            onEdit = { editingMagiaIndex = index },
                            onDelete = { viewModel.removerMagia(index) }
                        )
                        if (index < p.magias.lastIndex) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
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
}

@Composable
fun MagiaItem(
    magia: MagiaSelecionada,
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
                magia.nome,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
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

