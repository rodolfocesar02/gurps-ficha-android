package com.gurps.ficha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.viewmodel.ActiveDefense
import com.gurps.ficha.viewmodel.FichaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabCombate(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val defesasAtivas = viewModel.defesasAtivasVisiveis

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Defesas Ativas") {
            // Renderização dinâmica das defesas em Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                defesasAtivas.forEach { defesa ->
                    DefenseCard(
                        defesa = defesa,
                        onBonusChange = { bonus ->
                            viewModel.atualizarBonusDefesa(defesa.type, bonus)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Configuração de Apara
            Text("Configurar Apara:", style = MaterialTheme.typography.labelMedium)
            val periciasParaApara = viewModel.periciasParaApara
            
            if (periciasParaApara.isEmpty()) {
                Text(
                    "Adicione perícias de combate para calcular a Apara.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            } else {
                var expandedApara by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedApara,
                    onExpandedChange = { expandedApara = !expandedApara }
                ) {
                    val currentPericia = periciasParaApara.find { it.definicaoId == p.defesasAtivas.periciaAparaId }
                    OutlinedTextField(
                        value = currentPericia?.let { "${it.nome} (${it.calcularNivel(p)})" } ?: "Nenhuma",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedApara) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedApara,
                        onDismissRequest = { expandedApara = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma") },
                            onClick = {
                                viewModel.atualizarPericiaApara(null)
                                expandedApara = false
                            }
                        )
                        periciasParaApara.forEach { pericia ->
                            DropdownMenuItem(
                                text = { Text("${pericia.nome} (${pericia.calcularNivel(p)})") },
                                onClick = {
                                    viewModel.atualizarPericiaApara(pericia.definicaoId)
                                    expandedApara = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Configuração de Bloqueio
            Text("Configurar Bloqueio:", style = MaterialTheme.typography.labelMedium)
            val periciasParaBloqueio = viewModel.periciasParaBloqueio

            if (periciasParaBloqueio.isEmpty()) {
                Text(
                    "Adicione uma perícia de Escudo para calcular Bloqueio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            } else {
                var expandedBloqueio by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedBloqueio,
                    onExpandedChange = { expandedBloqueio = !expandedBloqueio }
                ) {
                    val currentPericia = periciasParaBloqueio.find { it.definicaoId == p.defesasAtivas.periciaBloqueioId }
                    OutlinedTextField(
                        value = currentPericia?.let { "${it.nome} (${it.calcularNivel(p)})" } ?: "Nenhuma",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Perícia de Escudo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBloqueio) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedBloqueio,
                        onDismissRequest = { expandedBloqueio = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma") },
                            onClick = {
                                viewModel.atualizarPericiaBloqueio(null)
                                expandedBloqueio = false
                            }
                        )
                        periciasParaBloqueio.forEach { pericia ->
                            DropdownMenuItem(
                                text = { Text("${pericia.nome} (${pericia.calcularNivel(p)})") },
                                onClick = {
                                    viewModel.atualizarPericiaBloqueio(pericia.definicaoId)
                                    expandedBloqueio = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefenseCard(defesa: ActiveDefense, onBonusChange: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp)
            .width(100.dp)
    ) {
        Text(
            defesa.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            defesa.finalValue.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Spacer(modifier = Modifier.height(4.dp))

        // Campo de Bônus reativo
        var textValue by remember(defesa.bonus) {
            mutableStateOf(if (defesa.bonus >= 0) "+${defesa.bonus}" else "${defesa.bonus}")
        }

        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() || it == '+' || it == '-' }
                if (filtered.isEmpty() || filtered == "+" || filtered == "-") {
                    textValue = filtered
                } else {
                    val parsed = filtered.replace("+", "").toIntOrNull()
                    if (parsed != null) {
                        textValue = if (parsed >= 0) "+$parsed" else "$parsed"
                        onBonusChange(parsed)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        Text(
            "Bônus",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

