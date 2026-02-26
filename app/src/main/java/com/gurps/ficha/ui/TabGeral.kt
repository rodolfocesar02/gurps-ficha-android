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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.gurps.ficha.viewmodel.FichaViewModel

@Composable
fun TabGeral(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    var pontosInput by rememberSaveable { mutableStateOf(p.pontosIniciais.toString()) }
    var ultimoPontosValidos by rememberSaveable { mutableStateOf(p.pontosIniciais.toString()) }
    var pontosEmFoco by remember { mutableStateOf(false) }

    LaunchedEffect(p.pontosIniciais) {
        if (!pontosEmFoco) {
            pontosInput = p.pontosIniciais.toString()
            ultimoPontosValidos = pontosInput
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Informações Básicas") {
            OutlinedTextField(
                value = p.nome,
                onValueChange = { viewModel.atualizarNome(it) },
                label = { Text("Nome do Personagem") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = p.jogador,
                    onValueChange = { viewModel.atualizarJogador(it) },
                    label = { Text("Jogador") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = pontosInput,
                    onValueChange = {
                        val somenteDigitos = it.filter(Char::isDigit).take(4)
                        pontosInput = somenteDigitos
                        if (somenteDigitos.isNotBlank()) {
                            ultimoPontosValidos = somenteDigitos
                        }
                    },
                    label = { Text("Pontos") },
                    modifier = Modifier
                        .width(100.dp)
                        .onFocusChanged { focusState ->
                            val perdeuFoco = pontosEmFoco && !focusState.isFocused
                            pontosEmFoco = focusState.isFocused
                            if (perdeuFoco) {
                                val valor = pontosInput.toIntOrNull()
                                if (valor != null) {
                                    viewModel.atualizarPontosIniciais(valor)
                                } else {
                                    pontosInput = ultimoPontosValidos
                                }
                            }
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = p.campanha,
                onValueChange = { viewModel.atualizarCampanha(it) },
                label = { Text("Campanha") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        SectionCard(title = "Atributos Primários") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AtributoEditor("ST", p.forca, (p.forca - 10) * 10,
                    { viewModel.atualizarForca(p.forca - 1) }, { viewModel.atualizarForca(p.forca + 1) })
                AtributoEditor("DX", p.destreza, (p.destreza - 10) * 20,
                    { viewModel.atualizarDestreza(p.destreza - 1) }, { viewModel.atualizarDestreza(p.destreza + 1) })
                AtributoEditor("IQ", p.inteligencia, (p.inteligencia - 10) * 20,
                    { viewModel.atualizarInteligencia(p.inteligencia - 1) }, { viewModel.atualizarInteligencia(p.inteligencia + 1) })
                AtributoEditor("HT", p.vitalidade, (p.vitalidade - 10) * 10,
                    { viewModel.atualizarVitalidade(p.vitalidade - 1) }, { viewModel.atualizarVitalidade(p.vitalidade + 1) })
            }
        }

        SectionCard(title = "Atributos Secundários") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AtributoSecundarioEditor("PV", p.forca, p.modPontosVida, p.pontosVida, 2,
                    { viewModel.atualizarModPontosVida(p.modPontosVida - 1) }, { viewModel.atualizarModPontosVida(p.modPontosVida + 1) })
                AtributoSecundarioEditor("Von", p.inteligencia, p.modVontade, p.vontade, 5,
                    { viewModel.atualizarModVontade(p.modVontade - 1) }, { viewModel.atualizarModVontade(p.modVontade + 1) })
                AtributoSecundarioEditor("Per", p.inteligencia, p.modPercepcao, p.percepcao, 5,
                    { viewModel.atualizarModPercepcao(p.modPercepcao - 1) }, { viewModel.atualizarModPercepcao(p.modPercepcao + 1) })
                AtributoSecundarioEditor("PF", p.vitalidade, p.modPontosFadiga, p.pontosFadiga, 3,
                    { viewModel.atualizarModPontosFadiga(p.modPontosFadiga - 1) }, { viewModel.atualizarModPontosFadiga(p.modPontosFadiga + 1) })
            }
        }

        SectionCard(title = "Características Derivadas") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CaracteristicaDisplay("Vel. Básica", String.format("%.2f", p.velocidadeBasica))
                CaracteristicaDisplay("Desloc.", "${p.deslocamentoBasico} m/s")
                CaracteristicaDisplay("BC", String.format("%.1f kg", p.baseCarga))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CaracteristicaDisplay("Dano GdP", p.danoGdP)
                CaracteristicaDisplay("Dano GeB", p.danoGeB)
            }
        }

        SectionCard(title = "Resumo de Pontos") {
            PontosResumoRow("Atributos Primários", p.pontosAtributos)
            PontosResumoRow("Atributos Secundários", p.pontosSecundarios)
            PontosResumoRow("Vantagens", p.pontosVantagens)
            PontosResumoRow("Desvantagens", p.pontosDesvantagens)
            PontosResumoRow("Qualidades", p.pontosQualidades)
            PontosResumoRow("Peculiaridades", p.pontosPeculiaridades)
            PontosResumoRow("Perícias", p.pontosPericias)
            PontosResumoRow("Magias", p.pontosMagias)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            PontosResumoRow("Total Gasto", p.pontosGastos, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AtributoEditor(nome: String, valor: Int, custo: Int, onMenos: () -> Unit, onMais: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Text(nome, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenos, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Diminuir")
            }
            Text(valor.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
            IconButton(onClick = onMais, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Aumentar")
            }
        }
        Text("[${if (custo >= 0) "+$custo" else custo}]", style = MaterialTheme.typography.bodySmall,
            color = if (custo >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
fun AtributoSecundarioEditor(nome: String, valorBase: Int, modificador: Int, valorFinal: Int, custoPorPonto: Int, onMenos: () -> Unit, onMais: () -> Unit) {
    val custo = modificador * custoPorPonto
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Text(nome, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text("($valorBase)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMenos, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Diminuir", modifier = Modifier.size(20.dp))
            }
            Text(valorFinal.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
            IconButton(onClick = onMais, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Aumentar", modifier = Modifier.size(20.dp))
            }
        }
        if (modificador != 0) {
            Text("[${if (custo >= 0) "+$custo" else custo}]", style = MaterialTheme.typography.bodySmall,
                color = if (custo >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
fun CaracteristicaDisplay(nome: String, valor: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(nome, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
fun PontosResumoRow(label: String, pontos: Int, fontWeight: FontWeight = FontWeight.Normal) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = fontWeight)
        Text(if (pontos >= 0) "+$pontos" else pontos.toString(), fontWeight = fontWeight,
            color = if (pontos >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
    }
}

