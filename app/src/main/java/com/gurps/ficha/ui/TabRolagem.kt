package com.gurps.ficha.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gurps.ficha.viewmodel.FichaViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private enum class TipoTeste(val label: String) {
    ATRIBUTO("Atributo"),
    ATAQUE("Ataque"),
    DEFESA("Defesa"),
    LIVRE("Livre")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabRolagem(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val historico = remember { mutableStateListOf<String>() }

    var tipoTeste by remember { mutableStateOf(TipoTeste.ATRIBUTO) }
    var modificador by remember { mutableIntStateOf(0) }

    var atributoSelecionado by remember { mutableStateOf("DX") }
    var ataqueSelecionadoId by remember { mutableStateOf<String?>(null) }
    var defesaSelecionada by remember { mutableStateOf(viewModel.defesasAtivasVisiveis.firstOrNull()?.name) }

    val ataqueAtual = p.pericias.firstOrNull { it.definicaoId == ataqueSelecionadoId }
    val defesaAtual = viewModel.defesasAtivasVisiveis.firstOrNull { it.name == defesaSelecionada }

    val alvoBase = when (tipoTeste) {
        TipoTeste.ATRIBUTO -> p.getAtributo(atributoSelecionado)
        TipoTeste.ATAQUE -> ataqueAtual?.calcularNivel(p)
        TipoTeste.DEFESA -> defesaAtual?.finalValue
        TipoTeste.LIVRE -> null
    }

    val contexto = when (tipoTeste) {
        TipoTeste.ATRIBUTO -> atributoSelecionado
        TipoTeste.ATAQUE -> ataqueAtual?.nome ?: "Ataque"
        TipoTeste.DEFESA -> defesaAtual?.name ?: "Defesa"
        TipoTeste.LIVRE -> "Livre"
    }

    fun registrarResultado(resultado: RolagemResultado) {
        val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val alvoTexto = alvoBase?.toString() ?: "-"
        val margemTexto = if (resultado.alvo != null) resultado.margem.toString() else "-"
        val linha = "$hora | ${tipoTeste.label} ($contexto) | alvo $alvoTexto | mod $modificador | total ${resultado.total} | ${resultado.tipoResultado} | margem $margemTexto"
        historico.add(0, linha)
        if (historico.size > 20) {
            historico.removeLast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Rolagem (MVP)") {
            Text(
                "Aba jogavel inicial. Testes locais 3d6 com modificador e historico.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            var expandedTipo by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedTipo,
                onExpandedChange = { expandedTipo = !expandedTipo }
            ) {
                OutlinedTextField(
                    value = tipoTeste.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de teste") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedTipo,
                    onDismissRequest = { expandedTipo = false }
                ) {
                    TipoTeste.entries.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo.label) },
                            onClick = {
                                tipoTeste = tipo
                                expandedTipo = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (tipoTeste == TipoTeste.ATRIBUTO) {
                var expandedAttr by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedAttr,
                    onExpandedChange = { expandedAttr = !expandedAttr }
                ) {
                    OutlinedTextField(
                        value = atributoSelecionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Atributo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAttr) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAttr,
                        onDismissRequest = { expandedAttr = false }
                    ) {
                        listOf("ST", "DX", "IQ", "HT", "PER", "VON").forEach { attr ->
                            DropdownMenuItem(
                                text = { Text(attr) },
                                onClick = {
                                    atributoSelecionado = attr
                                    expandedAttr = false
                                }
                            )
                        }
                    }
                }
            }

            if (tipoTeste == TipoTeste.ATAQUE) {
                var expandedAtk by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedAtk,
                    onExpandedChange = { expandedAtk = !expandedAtk }
                ) {
                    val label = ataqueAtual?.let { "${it.nome} (${it.calcularNivel(p)})" } ?: "Selecionar pericia"
                    OutlinedTextField(
                        value = label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ataque") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAtk) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAtk,
                        onDismissRequest = { expandedAtk = false }
                    ) {
                        p.pericias.forEach { pericia ->
                            DropdownMenuItem(
                                text = { Text("${pericia.nome} (${pericia.calcularNivel(p)})") },
                                onClick = {
                                    ataqueSelecionadoId = pericia.definicaoId
                                    expandedAtk = false
                                }
                            )
                        }
                    }
                }
            }

            if (tipoTeste == TipoTeste.DEFESA) {
                var expandedDef by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedDef,
                    onExpandedChange = { expandedDef = !expandedDef }
                ) {
                    val label = defesaAtual?.let { "${it.name} (${it.finalValue})" } ?: "Selecionar defesa"
                    OutlinedTextField(
                        value = label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Defesa") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDef) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDef,
                        onDismissRequest = { expandedDef = false }
                    ) {
                        viewModel.defesasAtivasVisiveis.forEach { defesa ->
                            DropdownMenuItem(
                                text = { Text("${defesa.name} (${defesa.finalValue})") },
                                onClick = {
                                    defesaSelecionada = defesa.name
                                    expandedDef = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { modificador-- },
                    modifier = Modifier.weight(1f)
                ) { Text("-1") }
                Card(
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        "Modificador: ${if (modificador >= 0) "+$modificador" else "$modificador"}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedButton(
                    onClick = { modificador++ },
                    modifier = Modifier.weight(1f)
                ) { Text("+1") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val podeRolar = when (tipoTeste) {
                TipoTeste.ATAQUE -> ataqueAtual != null
                TipoTeste.DEFESA -> defesaAtual != null
                else -> true
            }

            Button(
                onClick = {
                    val resultado = rolarDados(3, modificador, alvoBase)
                    registrarResultado(resultado)
                },
                enabled = podeRolar,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Rolar 3d6")
            }
        }

        SectionCard(title = "Historico da Sessao") {
            if (historico.isEmpty()) {
                Text(
                    "Nenhuma rolagem ainda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                historico.forEach { item ->
                    Text(item, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
