package com.gurps.ficha.ui.features.traits

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gurps.ficha.model.*

/**
 * TraitDialogs.kt - Hub Central de Diálogos de Características (Vantagens e Desvantagens).
 * 
 * Este arquivo foi modularizado na Etapa 6 para reduzir a dívida técnica.
 * - Lógica de Vantagens movida para [VantagemDialogs.kt]
 * - Lógica de Desvantagens movida para [DesvantagemDialogs.kt]
 * - Componentes Compartilhados movidos para [TraitCommonComponents.kt]
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorListaTraitsDialog(
    catalogo: List<TraitDefinicaoInterface>,
    titulo: String,
    onDismiss: () -> Unit,
    onSelect: (TraitDefinicaoInterface, Int) -> Unit
) {
    var busca by remember { mutableStateOf("") }
    var selecionado by remember { mutableStateOf<TraitDefinicaoInterface?>(null) }
    var level by remember { mutableStateOf(1) }
    
    val filtrada = catalogo.filter { it.nome.contains(busca, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (selecionado == null) {
                    OutlinedTextField(
                        value = busca,
                        onValueChange = { busca = it },
                        label = { Text("Buscar...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        items(filtrada) { trait ->
                            ListItem(
                                headlineContent = { Text(trait.nome, fontWeight = FontWeight.Bold) },
                                supportingContent = { Text("${trait.custo} pts") },
                                modifier = Modifier.clickable { 
                                    selecionado = trait 
                                    level = 1
                                }
                            )
                        }
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selecionado!!.nome, fontWeight = FontWeight.Bold)
                                Text("${selecionado!!.custo} pts", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { selecionado = null }, modifier = Modifier.semantics { contentDescription = "Remover seleção de ${selecionado?.nome ?: "traço"}" }) { Icon(Icons.Default.Delete, null) }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nivel Racial", style = MaterialTheme.typography.labelMedium)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            IconButton(onClick = { if(level > 1) level -= 1 }, modifier = Modifier.semantics { contentDescription = "Diminuir nível racial" }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                            Text("$level", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { level += 1 }, modifier = Modifier.semantics { contentDescription = "Aumentar nível racial" }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                        }
                    }
                    
                    val custoTotal = selecionado!!.custo * level
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Text("Custo Total: $custoTotal pontos", modifier = Modifier.padding(12.dp).fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selecionado != null,
                onClick = { selecionado?.let { onSelect(it, level) } }
            ) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

interface TraitDefinicaoInterface {
    val id: String
    val nome: String
    val custo: Int
}

class VantagemToTrait(val v: VantagemDefinicao) : TraitDefinicaoInterface {
    override val id = v.id
    override val nome = v.nome
    override val custo: Int = if (v.tipoCusto == TipoCusto.POR_NIVEL) v.getCustoPorNivel() else v.getCustoBase()
}

class DesvantagemToTrait(val d: DesvantagemDefinicao) : TraitDefinicaoInterface {
    override val id = d.id
    override val nome = d.nome
    override val custo: Int = if (d.tipoCusto == TipoCusto.POR_NIVEL) d.getCustoPorNivel() else d.getCustoBase()
}
