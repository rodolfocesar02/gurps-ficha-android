package com.gurps.ficha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.random.Random

/**
 * Dialog para rolar dados no estilo GURPS (3d6)
 */
@Composable
fun RoladorDadosDialog(
    targetNumber: Int? = null,
    skillName: String? = null,
    onDismiss: () -> Unit
) {
    var resultado by remember { mutableStateOf<RolagemResultado?>(null) }
    var quantidadeDados by remember { mutableStateOf(3) }
    var modificador by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Rolador de Dados",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (skillName != null && targetNumber != null) {
                    Text(
                        text = "Teste de $skillName (alvo: $targetNumber)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Configuracao de dados
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Dados:")
                    IconButton(
                        onClick = { if (quantidadeDados > 1) quantidadeDados-- }
                    ) {
                        Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${quantidadeDados}d6",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { if (quantidadeDados < 10) quantidadeDados++ }
                    ) {
                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Modificador
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Modificador:")
                    IconButton(
                        onClick = { modificador-- }
                    ) {
                        Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = if (modificador >= 0) "+$modificador" else "$modificador",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { modificador++ }
                    ) {
                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Botao Rolar
                Button(
                    onClick = {
                        resultado = rolarDados(quantidadeDados, modificador, targetNumber)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "ROLAR ${quantidadeDados}d6${if (modificador != 0) (if (modificador > 0) "+$modificador" else "$modificador") else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Resultado
                resultado?.let { res ->
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Dados individuais
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        res.dadosIndividuais.forEach { dado ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dado.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Total
                    Text(
                        text = "Total: ${res.total}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Resultado do teste (se aplicavel)
                    if (targetNumber != null) {
                        val corResultado = when (res.tipoResultado) {
                            TipoResultado.SUCESSO_CRITICO -> MaterialTheme.colorScheme.primary
                            TipoResultado.SUCESSO -> MaterialTheme.colorScheme.tertiary
                            TipoResultado.FALHA -> MaterialTheme.colorScheme.error
                            TipoResultado.FALHA_CRITICA -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        val textoResultado = when (res.tipoResultado) {
                            TipoResultado.SUCESSO_CRITICO -> "SUCESSO CRÍTICO!"
                            TipoResultado.SUCESSO -> "SUCESSO"
                            TipoResultado.FALHA -> "FALHA"
                            TipoResultado.FALHA_CRITICA -> "FALHA CRÍTICA!"
                            else -> ""
                        }

                        val margemTexto = if (res.margem >= 0) {
                            "Margem de Sucesso: ${res.margem}"
                        } else {
                            "Margem de Falha: ${-res.margem}"
                        }

                        Text(
                            text = textoResultado,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = corResultado
                        )

                        Text(
                            text = margemTexto,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Botoes de acao
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { resultado = null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Limpar")
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}

/**
 * Botoes rapidos de rolagem para tela principal
 */
@Composable
fun QuickDiceRoller(
    modifier: Modifier = Modifier,
    onRollResult: (String) -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf("") }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 3d6 (teste padrao GURPS)
        ElevatedButton(
            onClick = {
                val res = rolarDados(3, 0, null)
                lastResult = "3d6 = ${res.total}"
                onRollResult(lastResult)
            }
        ) {
            Text("3d6")
        }

        // 1d6
        ElevatedButton(
            onClick = {
                val res = rolarDados(1, 0, null)
                lastResult = "1d6 = ${res.total}"
                onRollResult(lastResult)
            }
        ) {
            Text("1d6")
        }

        // 2d6
        ElevatedButton(
            onClick = {
                val res = rolarDados(2, 0, null)
                lastResult = "2d6 = ${res.total}"
                onRollResult(lastResult)
            }
        ) {
            Text("2d6")
        }

        // Customizado
        ElevatedButton(
            onClick = { showDialog = true }
        ) {
            Text("Nd6")
        }
    }

    if (showDialog) {
        RoladorDadosDialog(
            onDismiss = { showDialog = false }
        )
    }
}

// === Logica de Rolagem ===

data class RolagemResultado(
    val dadosIndividuais: List<Int>,
    val soma: Int,
    val modificador: Int,
    val total: Int,
    val alvo: Int?,
    val margem: Int,
    val tipoResultado: TipoResultado
)

enum class TipoResultado {
    SUCESSO_CRITICO,
    SUCESSO,
    FALHA,
    FALHA_CRITICA,
    NENHUM
}

fun rolarDados(quantidade: Int, modificador: Int, alvo: Int?): RolagemResultado {
    val dados = (1..quantidade).map { Random.nextInt(1, 7) }
    val soma = dados.sum()
    val total = soma + modificador

    val (tipoResultado, margem) = if (alvo != null) {
        val m = alvo - total
        val tipo = when {
            // Sucesso Critico: 3-4 sempre, 5 se NH >= 15, 6 se NH >= 16
            soma <= 4 -> TipoResultado.SUCESSO_CRITICO
            soma == 5 && alvo >= 15 -> TipoResultado.SUCESSO_CRITICO
            soma == 6 && alvo >= 16 -> TipoResultado.SUCESSO_CRITICO
            // Falha Critica: 18 sempre, 17 se NH <= 15
            soma == 18 -> TipoResultado.FALHA_CRITICA
            soma == 17 && alvo <= 15 -> TipoResultado.FALHA_CRITICA
            // Falha por 10+ tambem e critica
            m <= -10 -> TipoResultado.FALHA_CRITICA
            // Sucesso/Falha normal
            total <= alvo -> TipoResultado.SUCESSO
            else -> TipoResultado.FALHA
        }
        tipo to m
    } else {
        TipoResultado.NENHUM to 0
    }

    return RolagemResultado(
        dadosIndividuais = dados,
        soma = soma,
        modificador = modificador,
        total = total,
        alvo = alvo,
        margem = margem,
        tipoResultado = tipoResultado
    )
}

/**
 * Tela de Combate - Rastreador de PV/PF
 */
@Composable
fun CombateTrackerDialog(
    pvMaximo: Int,
    pfMaximo: Int,
    pvAtual: Int,
    pfAtual: Int,
    onPvChange: (Int) -> Unit,
    onPfChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Rastreador de Combate",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Pontos de Vida
                TrackerRow(
                    label = "Pontos de Vida",
                    atual = pvAtual,
                    maximo = pvMaximo,
                    corBarra = MaterialTheme.colorScheme.error,
                    onChange = onPvChange
                )

                // Pontos de Fadiga
                TrackerRow(
                    label = "Pontos de Fadiga",
                    atual = pfAtual,
                    maximo = pfMaximo,
                    corBarra = MaterialTheme.colorScheme.primary,
                    onChange = onPfChange
                )

                // Status
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Status:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Status de PV
                    val statusPv = when {
                        pvAtual <= 0 -> "Inconsciente (teste HT a cada turno)"
                        pvAtual <= pvMaximo / 3 -> "Ferido gravemente"
                        pvAtual <= pvMaximo * 2 / 3 -> "Ferido"
                        else -> "Saudável"
                    }
                    Text("PV: $statusPv")

                    // Status de PF
                    val statusPf = when {
                        pfAtual <= 0 -> "Exausto (move pela metade)"
                        pfAtual <= pfMaximo / 3 -> "Muito cansado"
                        pfAtual <= pfMaximo * 2 / 3 -> "Cansado"
                        else -> "Descansado"
                    }
                    Text("PF: $statusPf")
                }

                // Botoes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onPvChange(pvMaximo)
                            onPfChange(pfMaximo)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Restaurar")
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}

@Composable
fun TrackerRow(
    label: String,
    atual: Int,
    maximo: Int,
    corBarra: androidx.compose.ui.graphics.Color,
    onChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Barra de progresso
        LinearProgressIndicator(
            progress = (atual.toFloat() / maximo).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = corBarra,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Controles
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // -5
            FilledTonalButton(onClick = { onChange((atual - 5).coerceAtLeast(-maximo)) }) {
                Text("-5")
            }
            // -1
            FilledTonalButton(onClick = { onChange((atual - 1).coerceAtLeast(-maximo)) }) {
                Text("-1")
            }
            // Valor atual
            Text(
                text = "$atual / $maximo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(100.dp),
                textAlign = TextAlign.Center
            )
            // +1
            FilledTonalButton(onClick = { onChange((atual + 1).coerceAtMost(maximo)) }) {
                Text("+1")
            }
            // +5
            FilledTonalButton(onClick = { onChange((atual + 5).coerceAtMost(maximo)) }) {
                Text("+5")
            }
        }
    }
}

