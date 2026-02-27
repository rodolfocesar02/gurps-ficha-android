package com.gurps.ficha.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.data.network.DiscordRollPayload
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.PERICIAS_COMBATE
import com.gurps.ficha.model.TipoEquipamento
import com.gurps.ficha.viewmodel.FichaViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlinx.coroutines.launch
import kotlin.random.Random

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

private data class DamageSourceOption(
    val id: String,
    val label: String,
    val contextLabel: String,
    val damageExpression: String
)

private data class ParsedDamage(
    val diceCount: Int,
    val modifier: Int,
    val suffix: String
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

private fun parseDamageExpression(expr: String): ParsedDamage? {
    val match = Regex("""^\s*(\d+)d((?:\s*[+-]\s*\d+)*)\s*(.*)$""").find(expr) ?: return null
    val diceCount = match.groupValues[1].toIntOrNull() ?: return null
    val modsRaw = match.groupValues[2]
    val modTokens = Regex("""[+-]\s*\d+""").findAll(modsRaw).map { it.value.replace(" ", "") }.toList()
    val modifier = modTokens.sumOf { it.toIntOrNull() ?: return null }
    val suffix = match.groupValues[3].trim()
    if (diceCount <= 0) return null
    return ParsedDamage(diceCount = diceCount, modifier = modifier, suffix = suffix)
}

private fun formatDamageCore(parsed: ParsedDamage): String {
    val mod = when {
        parsed.modifier > 0 -> "+${parsed.modifier}"
        parsed.modifier < 0 -> parsed.modifier.toString()
        else -> ""
    }
    return "${parsed.diceCount}d$mod"
}

private fun splitDamageEntries(expression: String): List<String> {
    val rawParts = expression.split("/").map { it.trim() }.filter { it.isNotBlank() }
    if (rawParts.isEmpty()) return listOf(expression.trim()).filter { it.isNotBlank() }

    var lastCore: String? = null
    return rawParts.map { part ->
        val parsed = parseDamageExpression(part)
        if (parsed != null) {
            lastCore = formatDamageCore(parsed)
            part
        } else {
            val core = lastCore
            if (core != null) "$core $part" else part
        }
    }
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

    var ataqueSelecionadoKey by remember { mutableStateOf<String?>(null) }
    var fonteDanoSelecionadaId by remember { mutableStateOf<String?>(null) }
    var modificadorAtaque by remember { mutableIntStateOf(0) }
    val atributosRapidos = listOf("ST", "DX", "IQ", "HT", "VON", "PER")
    val modificadoresAtributo = remember {
        mutableStateMapOf(
            "ST" to 0, "DX" to 0, "IQ" to 0, "HT" to 0, "VON" to 0, "PER" to 0
        )
    }

    val periciasCombate = p.pericias.filter { it.definicaoId in PERICIAS_COMBATE }
    val basePericiasAtaque = if (periciasCombate.isNotEmpty()) periciasCombate else p.pericias
    val opcoesAtaque = basePericiasAtaque.mapIndexed { index, pericia ->
        val nivel = pericia.calcularNivel(p)
        RollMappedOption(
            id = periciaSelectionKey(pericia, index),
            label = "${periciaLabel(pericia)} ($nivel)",
            contextLabel = "Ataque ${periciaLabel(pericia)}",
            target = nivel
        )
    }
    val armasEquipadas = p.equipamentos
        .filter { it.tipo == TipoEquipamento.ARMA }
        .mapIndexed { index, equipamento ->
            val dano = equipamento.danoCalculadoComSt(p) ?: equipamento.armaDanoRaw?.trim().orEmpty()
            DamageSourceOption(
                id = "arma_$index",
                label = equipamento.nome,
                contextLabel = "Dano ${equipamento.nome}",
                damageExpression = dano.ifBlank { "-" }
            )
        }
    val fallbackSt = DamageSourceOption(
        id = "st_base",
        label = "Sem arma (ST base)",
        contextLabel = "Dano ST base",
        damageExpression = p.danoGdP
    )
    val fontesDano = if (armasEquipadas.isNotEmpty()) {
        listOf(fallbackSt) + armasEquipadas
    } else {
        listOf(fallbackSt)
    }

    val ataqueAtual = opcoesAtaque.firstOrNull { it.id == ataqueSelecionadoKey }
    val fonteDanoAtual = fontesDano.firstOrNull { it.id == fonteDanoSelecionadaId } ?: fontesDano.first()

    LaunchedEffect(opcoesAtaque) {
        if (opcoesAtaque.isNotEmpty() && opcoesAtaque.none { it.id == ataqueSelecionadoKey }) {
            ataqueSelecionadoKey = opcoesAtaque.first().id
        }
    }
    LaunchedEffect(fontesDano) {
        if (fontesDano.isNotEmpty() && fontesDano.none { it.id == fonteDanoSelecionadaId }) {
            fonteDanoSelecionadaId = fontesDano.first().id
        }
    }

    fun registrarResultado(
        resultado: RolagemResultado,
        payload: DiscordRollPayload,
        statusEnvio: String?,
        detalheErro: String?,
        tipoLabel: String,
        contextoLabel: String,
        alvo: Int?,
        mod: Int
    ) {
        val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val alvoTexto = alvo?.toString() ?: "-"
        val margemTexto = if (resultado.alvo != null) resultado.margem.toString() else "-"
        val linha = "$hora | $tipoLabel ($contextoLabel) | alvo $alvoTexto | mod $mod | total ${resultado.total} | ${resultado.tipoResultado} | margem $margemTexto"
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

    fun executarRolagem(tipo: TipoTeste, contextoLabel: String, alvo: Int?, mod: Int) {
        val resultado = rolarDados(3, mod, alvo)
        val payload = DiscordRollPayload(
            character = p.nome.ifBlank { "Personagem" },
            testType = tipo.label,
            context = contextoLabel,
            target = alvo,
            modifier = mod,
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
                detalheErro = envio.detalhe,
                tipoLabel = tipo.label,
                contextoLabel = contextoLabel,
                alvo = alvo,
                mod = mod
            )
        }
    }

    fun executarRolagemDano(contextoLabel: String, danoExpr: String) {
        val parsed = parseDamageExpression(danoExpr)
        val hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        if (parsed == null) {
            val linha = "$hora | Dano ($contextoLabel) | expressao nao rolavel: $danoExpr"
            val payload = DiscordRollPayload(
                character = p.nome.ifBlank { "Personagem" },
                testType = "Dano",
                context = contextoLabel,
                target = null,
                modifier = 0,
                dice = emptyList(),
                total = 0,
                outcome = TipoResultado.NENHUM.name,
                margin = null,
                channelId = canalSelecionadoId
            )
            coroutineScope.launch {
                val envio = viewModel.enviarRolagemDiscord(payload)
                historico.add(
                    0,
                    HistoricoRolagemItem(
                        texto = linha,
                        payload = payload,
                        statusEnvio = if (envio.enviado) "enviado" else "erro",
                        detalheErro = envio.detalhe
                    )
                )
            }
            return
        }

        val dados = (1..parsed.diceCount).map { Random.nextInt(1, 7) }
        val soma = dados.sum()
        val total = soma + parsed.modifier
        val sufixo = if (parsed.suffix.isBlank()) "" else " ${parsed.suffix}"
        val linha = "$hora | Dano ($contextoLabel) | expr $danoExpr | dados ${dados.joinToString(prefix = "[", postfix = "]")} | total $total$sufixo"
        val payload = DiscordRollPayload(
            character = p.nome.ifBlank { "Personagem" },
            testType = "Dano",
            context = contextoLabel,
            target = null,
            modifier = parsed.modifier,
            dice = dados,
            total = total,
            outcome = TipoResultado.NENHUM.name,
            margin = null,
            channelId = canalSelecionadoId
        )
        coroutineScope.launch {
            val envio = viewModel.enviarRolagemDiscord(payload)
            historico.add(
                0,
                HistoricoRolagemItem(
                    texto = linha,
                    payload = payload,
                    statusEnvio = if (envio.enviado) "enviado" else "erro",
                    detalheErro = envio.detalhe
                )
            )
            if (historico.size > 20) {
                historico.removeLast()
            }
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
                .height(60.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Deslize para cima/baixo em cada atributo para ajustar o modificador.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        atributosRapidos.forEach { attr ->
                            val valor = p.getAtributo(attr)
                            val modAttr = modificadoresAtributo[attr] ?: 0
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = attr,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = valor.toString(),
                                    modifier = Modifier
                                        .pointerInput(attr, modAttr) {
                                            var dragAcumulado = 0f
                                            val passoPx = 20f
                                            detectVerticalDragGestures(
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragAcumulado += dragAmount
                                                    while (abs(dragAcumulado) >= passoPx) {
                                                        val atual = modificadoresAtributo[attr] ?: 0
                                                        if (dragAcumulado < 0f) {
                                                            modificadoresAtributo[attr] = (atual + 1).coerceIn(-20, 20)
                                                            dragAcumulado += passoPx
                                                        } else {
                                                            modificadoresAtributo[attr] = (atual - 1).coerceIn(-20, 20)
                                                            dragAcumulado -= passoPx
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                        .clickable {
                                            executarRolagem(
                                                tipo = TipoTeste.ATRIBUTO,
                                                contextoLabel = attr,
                                                alvo = valor,
                                                mod = modAttr
                                            )
                                        },
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "mod ${if (modAttr >= 0) "+$modAttr" else modAttr}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                text = "PV ${p.pontosVida}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                text = "PF ${p.pontosFadiga}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        var showConfigAtaqueDialog by remember { mutableStateOf(false) }
        if (opcoesAtaque.isEmpty()) {
            Text(
                "Sem pericias para ataque. Adicione pericias de combate na aba Pericias.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Button(
                onClick = { showConfigAtaqueDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Configurar seu Ataque e Dano", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(3.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(modificadorAtaque) {
                        var dragAcumulado = 0f
                        val passoPx = 20f
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                dragAcumulado += dragAmount
                                while (abs(dragAcumulado) >= passoPx) {
                                    if (dragAcumulado < 0f) {
                                        modificadorAtaque = (modificadorAtaque + 1).coerceIn(-20, 20)
                                        dragAcumulado += passoPx
                                    } else {
                                        modificadorAtaque = (modificadorAtaque - 1).coerceIn(-20, 20)
                                        dragAcumulado -= passoPx
                                    }
                                }
                            }
                        )
                    },
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "ATAQUE",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    ataqueAtual?.contextLabel?.removePrefix("Ataque ") ?: "Ataque",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "NH ${ataqueAtual?.target ?: "-"}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = ataqueAtual?.target != null) {
                                            executarRolagem(
                                                tipo = TipoTeste.ATAQUE,
                                                contextoLabel = ataqueAtual?.contextLabel ?: "Ataque",
                                                alvo = ataqueAtual?.target,
                                                mod = modificadorAtaque
                                            )
                                        },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "DANO",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text(
                                    fonteDanoAtual.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val danos = splitDamageEntries(fonteDanoAtual.damageExpression)
                                danos.forEach { danoLinha ->
                                    val danoRolavel = parseDamageExpression(danoLinha) != null
                                    Text(
                                        danoLinha,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = danoRolavel) {
                                                executarRolagemDano(
                                                    contextoLabel = fonteDanoAtual.contextLabel,
                                                    danoExpr = danoLinha
                                                )
                                            },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (danoRolavel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                Text(
                    "Deslize para cima/baixo para ajustar mod de ataque: ${if (modificadorAtaque >= 0) "+$modificadorAtaque" else "$modificadorAtaque"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            if (showConfigAtaqueDialog) {
                var expandedAtaque by remember { mutableStateOf(false) }
                var expandedFonteDano by remember { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = { showConfigAtaqueDialog = false },
                    title = { Text("Configurar Ataque e Dano") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ExposedDropdownMenuBox(
                                expanded = expandedAtaque,
                                onExpandedChange = { expandedAtaque = !expandedAtaque }
                            ) {
                                OutlinedTextField(
                                    value = ataqueAtual?.label ?: "Selecionar pericia",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Pericia de combate") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAtaque) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedAtaque,
                                    onDismissRequest = { expandedAtaque = false }
                                ) {
                                    opcoesAtaque.forEach { ataque ->
                                        DropdownMenuItem(
                                            text = { Text(ataque.label) },
                                            onClick = {
                                                ataqueSelecionadoKey = ataque.id
                                                expandedAtaque = false
                                            }
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = expandedFonteDano,
                                onExpandedChange = { expandedFonteDano = !expandedFonteDano }
                            ) {
                                OutlinedTextField(
                                    value = fonteDanoAtual.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Arma / Fonte de dano") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFonteDano) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedFonteDano,
                                    onDismissRequest = { expandedFonteDano = false }
                                ) {
                                    fontesDano.forEach { fonte ->
                                        DropdownMenuItem(
                                            text = { Text(fonte.label) },
                                            onClick = {
                                                fonteDanoSelecionadaId = fonte.id
                                                expandedFonteDano = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showConfigAtaqueDialog = false }) {
                            Text("Fechar")
                        }
                    }
                )
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
