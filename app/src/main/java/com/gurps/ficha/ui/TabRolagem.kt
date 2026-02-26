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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.data.network.DiscordRollPayload
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.viewmodel.FichaViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private enum class TipoTeste(val label: String) {
    ATRIBUTO("Atributo"),
    ATAQUE("Ataque"),
    DEFESA("Defesa"),
    LIVRE("Livre")
}

private data class HistoricoRolagemItem(
    val texto: String,
    val payload: DiscordRollPayload,
    val statusEnvio: String?,
    val detalheErro: String?
)

private data class RollMappedOption(
    val id: String,
    val label: String,
    val contextLabel: String,
    val target: Int?
)

private fun periciaLabel(pericia: PericiaSelecionada): String {
    return if (pericia.especializacao.isBlank()) {
        pericia.nome
    } else {
        "${pericia.nome} (${pericia.especializacao})"
    }
}

private fun periciaSelectionKey(pericia: PericiaSelecionada, index: Int): String {
    return "${pericia.definicaoId}|${pericia.especializacao}|$index"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabRolagem(viewModel: FichaViewModel) {
    val p = viewModel.personagem
    val historico = remember { mutableStateListOf<HistoricoRolagemItem>() }
    val coroutineScope = rememberCoroutineScope()
    val canaisDiscord = viewModel.canaisDiscord
    val canalSelecionadoId = viewModel.canalDiscordSelecionadoId
    val canalSelecionadoNome = viewModel.canalDiscordSelecionadoNome
    val canaisCarregando = viewModel.canaisDiscordCarregando
    val canaisErro = viewModel.canaisDiscordErro
    val backendOnline = canaisErro.isNullOrBlank()
    var showEditarCanalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (canaisDiscord.isEmpty() && !canaisCarregando) {
            viewModel.atualizarCanaisDiscord()
        }
    }

    var tipoTeste by remember { mutableStateOf(TipoTeste.ATRIBUTO) }
    var modificador by remember { mutableIntStateOf(0) }

    var atributoSelecionado by remember { mutableStateOf("DX") }
    var ataqueSelecionadoKey by remember { mutableStateOf<String?>(null) }
    var defesaSelecionada by remember { mutableStateOf(viewModel.defesasAtivasVisiveis.firstOrNull()?.name) }

    val opcoesAtributo = listOf("ST", "DX", "IQ", "HT", "PER", "VON").map { sigla ->
        RollMappedOption(
            id = sigla,
            label = sigla,
            contextLabel = sigla,
            target = p.getAtributo(sigla)
        )
    }
    val opcoesAtaque = p.pericias.mapIndexed { index, pericia ->
        val nivel = pericia.calcularNivel(p)
        RollMappedOption(
            id = periciaSelectionKey(pericia, index),
            label = "${periciaLabel(pericia)} ($nivel)",
            contextLabel = "Ataque ${periciaLabel(pericia)}",
            target = nivel
        )
    }
    val opcoesDefesa = viewModel.defesasAtivasVisiveis.map { defesa ->
        RollMappedOption(
            id = defesa.name,
            label = "${defesa.name} (${defesa.finalValue})",
            contextLabel = "Defesa ${defesa.name}",
            target = defesa.finalValue
        )
    }

    val atributoAtual = opcoesAtributo.firstOrNull { it.id == atributoSelecionado }
    val ataqueAtual = opcoesAtaque.firstOrNull { it.id == ataqueSelecionadoKey }
    val defesaAtual = opcoesDefesa.firstOrNull { it.id == defesaSelecionada }

    val alvoBase = when (tipoTeste) {
        TipoTeste.ATRIBUTO -> atributoAtual?.target
        TipoTeste.ATAQUE -> ataqueAtual?.target
        TipoTeste.DEFESA -> defesaAtual?.target
        TipoTeste.LIVRE -> null
    }

    val contexto = when (tipoTeste) {
        TipoTeste.ATRIBUTO -> atributoAtual?.contextLabel ?: atributoSelecionado
        TipoTeste.ATAQUE -> ataqueAtual?.contextLabel ?: "Ataque"
        TipoTeste.DEFESA -> defesaAtual?.contextLabel ?: "Defesa"
        TipoTeste.LIVRE -> "Livre"
    }

    fun registrarResultado(
        resultado: RolagemResultado,
        payload: DiscordRollPayload,
        statusEnvio: String?,
        detalheErro: String?
    ) {
        val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val alvoTexto = alvoBase?.toString() ?: "-"
        val margemTexto = if (resultado.alvo != null) resultado.margem.toString() else "-"
        val linha = "$hora | ${tipoTeste.label} ($contexto) | alvo $alvoTexto | mod $modificador | total ${resultado.total} | ${resultado.tipoResultado} | margem $margemTexto"
        historico.add(
            0,
            HistoricoRolagemItem(
                texto = linha,
                payload = payload,
                statusEnvio = statusEnvio,
                detalheErro = detalheErro
            )
        )
        if (historico.size > 20) {
            historico.removeLast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 10.dp, top = 6.dp, end = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button(
            onClick = { showEditarCanalDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (backendOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "EDITAR CANAL",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = canalSelecionadoNome ?: "Selecionar canal de voz",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

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
                        opcoesAtributo.forEach { atributo ->
                            DropdownMenuItem(
                                text = { Text(atributo.label) },
                                onClick = {
                                    atributoSelecionado = atributo.id
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
                    val label = ataqueAtual?.label ?: "Selecionar pericia"
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
                        opcoesAtaque.forEach { ataque ->
                            DropdownMenuItem(
                                text = { Text(ataque.label) },
                                onClick = {
                                    ataqueSelecionadoKey = ataque.id
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
                    val label = defesaAtual?.label ?: "Selecionar defesa"
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
                        opcoesDefesa.forEach { defesa ->
                            DropdownMenuItem(
                                text = { Text(defesa.label) },
                                onClick = {
                                    defesaSelecionada = defesa.id
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
                    val payload = DiscordRollPayload(
                        character = p.nome.ifBlank { "Personagem" },
                        testType = tipoTeste.label,
                        context = contexto,
                        target = alvoBase,
                        modifier = modificador,
                        dice = resultado.dadosIndividuais,
                        total = resultado.total,
                        outcome = resultado.tipoResultado.name,
                        margin = if (resultado.alvo != null) resultado.margem else null,
                        channelId = canalSelecionadoId
                    )
                    coroutineScope.launch {
                        val envio = viewModel.enviarRolagemDiscord(payload)
                        registrarResultado(
                            resultado = resultado,
                            payload = payload,
                            statusEnvio = if (envio.enviado) "enviado" else "erro",
                            detalheErro = envio.detalhe
                        )
                    }
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
                historico.forEachIndexed { index, item ->
                    val statusLabel = item.statusEnvio?.let { status ->
                        " | envio: $status"
                    }.orEmpty()
                    Text(
                        "${item.texto}$statusLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.statusEnvio == "erro") {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (item.statusEnvio == "erro" && !item.detalheErro.isNullOrBlank()) {
                        Text(
                            "detalhe: ${item.detalheErro}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val envio = viewModel.enviarRolagemDiscord(item.payload)
                                    val atualizado = item.copy(
                                        statusEnvio = if (envio.enviado) "enviado" else "erro",
                                        detalheErro = envio.detalhe
                                    )
                                    if (index in historico.indices) {
                                        historico[index] = atualizado
                                    }
                                }
                            }
                        ) {
                            Text("Reenviar")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showEditarCanalDialog) {
        var expandedCanal by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showEditarCanalDialog = false },
            title = { Text("Canal de envio Discord") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedCanal,
                        onExpandedChange = { expandedCanal = !expandedCanal }
                    ) {
                        val canalLabel = when {
                            canaisCarregando -> "Carregando canais..."
                            !canalSelecionadoNome.isNullOrBlank() -> canalSelecionadoNome
                            else -> "Selecionar canal de voz"
                        }
                        OutlinedTextField(
                            value = canalLabel,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCanal) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCanal,
                            onDismissRequest = { expandedCanal = false }
                        ) {
                            canaisDiscord.forEach { canal ->
                                DropdownMenuItem(
                                    text = { Text("${canal.guildName} / ${canal.name}") },
                                    onClick = {
                                        viewModel.selecionarCanalDiscord(canal)
                                        expandedCanal = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.atualizarCanaisDiscord() },
                        enabled = !canaisCarregando,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (backendOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = if (canaisCarregando) "ATUALIZANDO..." else "ATUALIZAR CANAL",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!canaisErro.isNullOrBlank()) {
                        Text(
                            "Erro ao carregar canais: $canaisErro",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEditarCanalDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}
