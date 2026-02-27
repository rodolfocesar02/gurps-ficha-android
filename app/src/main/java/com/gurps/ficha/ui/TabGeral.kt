package com.gurps.ficha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlin.math.abs

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
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionCard(title = "Informacoes Basicas") {
            OutlinedTextField(
                value = p.nome,
                onValueChange = { viewModel.atualizarNome(it) },
                label = { Text("Nome do Personagem") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(6.dp))
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
        }

        SectionCard(title = "Atributos Primarios") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AtributoEditor("ST", p.forca, (p.forca - 10) * 10) { delta ->
                    viewModel.atualizarForca(p.forca + delta)
                }
                AtributoEditor("DX", p.destreza, (p.destreza - 10) * 20) { delta ->
                    viewModel.atualizarDestreza(p.destreza + delta)
                }
                AtributoEditor("IQ", p.inteligencia, (p.inteligencia - 10) * 20) { delta ->
                    viewModel.atualizarInteligencia(p.inteligencia + delta)
                }
                AtributoEditor("HT", p.vitalidade, (p.vitalidade - 10) * 10) { delta ->
                    viewModel.atualizarVitalidade(p.vitalidade + delta)
                }
            }
        }

        SectionCard(title = "Atributos Secundarios") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AtributoSecundarioEditor("PV", p.forca, p.modPontosVida, p.pontosVida, 2) { delta ->
                    viewModel.atualizarModPontosVida(p.modPontosVida + delta)
                }
                AtributoSecundarioEditor("Von", p.inteligencia, p.modVontade, p.vontade, 5) { delta ->
                    viewModel.atualizarModVontade(p.modVontade + delta)
                }
                AtributoSecundarioEditor("Per", p.inteligencia, p.modPercepcao, p.percepcao, 5) { delta ->
                    viewModel.atualizarModPercepcao(p.modPercepcao + delta)
                }
                AtributoSecundarioEditor("PF", p.vitalidade, p.modPontosFadiga, p.pontosFadiga, 3) { delta ->
                    viewModel.atualizarModPontosFadiga(p.modPontosFadiga + delta)
                }
            }
        }

        SectionCard(title = "Caracteristicas Derivadas") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CaracteristicaDisplay("Vel. Basica", String.format("%.2f", p.velocidadeBasica))
                CaracteristicaDisplay("Desloc.", "${p.deslocamentoBasico} m/s")
                CaracteristicaDisplay("BC", String.format("%.1f kg", p.baseCarga))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CaracteristicaDisplay("Dano GdP", p.danoGdP)
                CaracteristicaDisplay("Dano GeB", p.danoGeB)
            }
        }

        SectionCard(title = "Anotacoes") {
            OutlinedTextField(
                value = p.campanha,
                onValueChange = { viewModel.atualizarCampanha(it.take(200)) },
                label = { Text("Campanha (max 200)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = p.historico,
                onValueChange = { viewModel.atualizarHistorico(it.take(1000)) },
                label = { Text("Historia (max 1000)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = p.aparencia,
                onValueChange = { viewModel.atualizarAparencia(it.take(1000)) },
                label = { Text("Aparencia (max 1000)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = p.notas,
                onValueChange = { viewModel.atualizarNotas(it.take(1000)) },
                label = { Text("Notas (max 1000)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )
        }

        SectionCard(title = "Resumo de Pontos") {
            PontosResumoRow("Atributos Primarios", p.pontosAtributos)
            PontosResumoRow("Atributos Secundarios", p.pontosSecundarios)
            PontosResumoRow("Vantagens", p.pontosVantagens)
            PontosResumoRow("Desvantagens", p.pontosDesvantagens)
            PontosResumoRow("Qualidades", p.pontosQualidades)
            PontosResumoRow("Peculiaridades", p.pontosPeculiaridades)
            PontosResumoRow("Pericias", p.pontosPericias)
            PontosResumoRow("Magias", p.pontosMagias)
            Divider(modifier = Modifier.padding(vertical = 6.dp))
            PontosResumoRow("Total Gasto", p.pontosGastos, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AtributoEditor(nome: String, valor: Int, custo: Int, onDelta: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Text(nome, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(
            valor.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(36.dp)
                .pointerInput(nome) {
                    var dragAcumulado = 0f
                    val passoPx = 20f
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            dragAcumulado += dragAmount
                            while (abs(dragAcumulado) >= passoPx) {
                                if (dragAcumulado < 0f) {
                                    onDelta(1)
                                    dragAcumulado += passoPx
                                } else {
                                    onDelta(-1)
                                    dragAcumulado -= passoPx
                                }
                            }
                        }
                    )
                },
            textAlign = TextAlign.Center
        )
        Text("swipe", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "[${if (custo >= 0) "+$custo" else custo}]",
            style = MaterialTheme.typography.bodySmall,
            color = if (custo >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun AtributoSecundarioEditor(
    nome: String,
    valorBase: Int,
    modificador: Int,
    valorFinal: Int,
    custoPorPonto: Int,
    onDelta: (Int) -> Unit
) {
    val custo = modificador * custoPorPonto
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Text(nome, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(
            valorFinal.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(34.dp)
                .pointerInput(nome) {
                    var dragAcumulado = 0f
                    val passoPx = 20f
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            dragAcumulado += dragAmount
                            while (abs(dragAcumulado) >= passoPx) {
                                if (dragAcumulado < 0f) {
                                    onDelta(1)
                                    dragAcumulado += passoPx
                                } else {
                                    onDelta(-1)
                                    dragAcumulado -= passoPx
                                }
                            }
                        }
                    )
                },
            textAlign = TextAlign.Center
        )
        Text("base $valorBase", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (modificador != 0) {
            Text(
                "[${if (custo >= 0) "+$custo" else custo}]",
                style = MaterialTheme.typography.bodySmall,
                color = if (custo >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun CaracteristicaDisplay(nome: String, valor: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
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
        Text(
            if (pontos >= 0) "+$pontos" else pontos.toString(),
            fontWeight = fontWeight,
            color = if (pontos >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
        )
    }
}
