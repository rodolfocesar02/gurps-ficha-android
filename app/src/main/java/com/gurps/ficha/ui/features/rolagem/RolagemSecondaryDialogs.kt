package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gurps.ficha.ui.appCardColors
import com.gurps.ficha.data.network.DiscordVoiceChannel
import com.gurps.ficha.domain.rules.MagiaEnergiaRules
import com.gurps.ficha.model.*
import com.gurps.ficha.viewmodel.DefenseType
import kotlin.math.abs

@Composable
fun RolagemPersonalizadaDialog(
    dadosPersonalizadosQuantidade: Int,
    dadosPersonalizadosFaces: Int,
    dadosPersonalizadosModificador: Int,
    expressaoPersonalizada: String,
    dadosPersonalizadosQuantidadeInput: String,
    dadosPersonalizadosFacesInput: String,
    dadosPersonalizadosModificadorInput: String,
    motivo: String,
    isPraCegoVariant: Boolean,
    onUpdateQuantidade: (Int) -> Unit,
    onUpdateFaces: (Int) -> Unit,
    onUpdateModificador: (Int) -> Unit,
    onInputQuantidade: (String) -> Unit,
    onInputFaces: (String) -> Unit,
    onInputModificador: (String) -> Unit,
    onUpdateMotivo: (String) -> Unit,
    onExecutarRolagem: () -> Unit,
    onDismiss: () -> Unit
) {
    val defenseNumberStyle = MaterialTheme.typography.headlineMedium
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Rolagem Livre",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (!isPraCegoVariant) {
                                    Modifier.pointerInput(dadosPersonalizadosQuantidade) {
                                        var dragAcumulado = 0f
                                        val passoPx = 20f
                                        detectVerticalDragGestures(
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                dragAcumulado += dragAmount
                                                while (abs(dragAcumulado) >= passoPx) {
                                                    if (dragAcumulado < 0f) {
                                                        onUpdateQuantidade((dadosPersonalizadosQuantidade + 1).coerceIn(1, 999))
                                                        dragAcumulado += passoPx
                                                    } else {
                                                        onUpdateQuantidade((dadosPersonalizadosQuantidade - 1).coerceIn(1, 999))
                                                        dragAcumulado -= passoPx
                                                    }
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        colors = appCardColors()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .semantics(mergeDescendants = true) { 
                                    contentDescription = "Quantidade de dados: $dadosPersonalizadosQuantidade. Deslize para ajustar." 
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Qtd", style = MaterialTheme.typography.labelSmall)
                            Text("$dadosPersonalizadosQuantidade", style = defenseNumberStyle, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (!isPraCegoVariant) {
                                    Modifier.pointerInput(dadosPersonalizadosFaces) {
                                        var dragAcumulado = 0f
                                        val passoPx = 20f
                                        detectVerticalDragGestures(
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                dragAcumulado += dragAmount
                                                while (abs(dragAcumulado) >= passoPx) {
                                                    if (dragAcumulado < 0f) {
                                                        onUpdateFaces((dadosPersonalizadosFaces + 1).coerceIn(1, 999))
                                                        dragAcumulado += passoPx
                                                    } else {
                                                        onUpdateFaces((dadosPersonalizadosFaces - 1).coerceIn(1, 999))
                                                        dragAcumulado -= passoPx
                                                    }
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        colors = appCardColors()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Faces", style = MaterialTheme.typography.labelSmall)
                            Text("$dadosPersonalizadosFaces", style = defenseNumberStyle, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (!isPraCegoVariant) {
                                    Modifier.pointerInput(dadosPersonalizadosModificador) {
                                        var dragAcumulado = 0f
                                        val passoPx = 20f
                                        detectVerticalDragGestures(
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                dragAcumulado += dragAmount
                                                while (abs(dragAcumulado) >= passoPx) {
                                                    if (dragAcumulado < 0f) {
                                                        onUpdateModificador((dadosPersonalizadosModificador + 1).coerceIn(-999, 999))
                                                        dragAcumulado += passoPx
                                                    } else {
                                                        onUpdateModificador((dadosPersonalizadosModificador - 1).coerceIn(-999, 999))
                                                        dragAcumulado -= passoPx
                                                    }
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        colors = appCardColors()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Mod", style = MaterialTheme.typography.labelSmall)
                            val modTexto = when {
                                dadosPersonalizadosModificador > 0 -> "+$dadosPersonalizadosModificador"
                                else -> dadosPersonalizadosModificador.toString()
                            }
                            Text(modTexto, style = defenseNumberStyle, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = dadosPersonalizadosQuantidadeInput,
                        onValueChange = onInputQuantidade,
                        modifier = Modifier.weight(1f),
                        label = { Text("Qtd") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = dadosPersonalizadosFacesInput,
                        onValueChange = onInputFaces,
                        modifier = Modifier.weight(1f),
                        label = { Text("Faces") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = dadosPersonalizadosModificadorInput,
                        onValueChange = onInputModificador,
                        modifier = Modifier.weight(1f),
                        label = { Text("Mod") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = appCardColors()
                ) {
                    Text(
                        "Expressão: $expressaoPersonalizada",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = motivo,
                    onValueChange = onUpdateMotivo,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Motivo (opcional)") },
                    placeholder = { Text("Ex: Pânico, Iniciativa...") },
                    singleLine = true
                )

                Button(
                    onClick = {
                        onExecutarRolagem()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    Text(
                        "Rolar $expressaoPersonalizada",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}

@Composable
fun RolagemMagiaAlmaDialog(
    aspectos: List<SoulAspectOption>,
    onAspectoSelecionado: (SoulAspectOption) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Magia da Alma",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    aspectos.forEach { aspecto ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAspectoSelecionado(aspecto) },
                            colors = appCardColors()
                        ) {
                            Text(
                                text = aspecto.nome,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}

@Composable
fun RolagemDescricaoDialogModal(
    dialogInfo: RollDescricaoDialog,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogInfo.titulo) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(dialogInfo.texto, style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolagemEnergiaManualDialog(
    magiaEnergia: MagiaRollOption,
    pfAtualRolagem: Int,
    energiaManualInput: String,
    talismaMagiaVinculada: String?,
    repertorioParaTalisma: List<String>,
    isPraCegoVariant: Boolean,
    onInputMudou: (String) -> Unit,
    onTalismaVinculadoMudou: (String?) -> Unit,
    onAplicar: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val exigeVinculoTalisma = magiaEnergia.definicaoId.equals("talisma", ignoreCase = true)
    var menuTalismaExpandido by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gasto de energia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Magia: ${magiaEnergia.nome}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                // Lote MAGIA-E1: a faixa lida do catálogo, em português. Antes
                // aparecia só o texto cru ("1 a 3") e o campo já vinha com 1 —
                // não dava para saber que 2 e 3 eram escolhas legítimas.
                val custoDoCatalogo = com.gurps.ficha.domain.rules.MagiaEnergiaRules
                    .parseCusto(magiaEnergia.energia)
                if (custoDoCatalogo.precisaEscolher) {
                    Text(
                        "Você escolhe quanto gastar: ${custoDoCatalogo.comoTexto}. " +
                            "Os dados só rolam depois desta escolha.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                magiaEnergia.energia?.takeIf { it.isNotBlank() }?.let { energia ->
                    Text(
                        "Energia da ficha: $energia",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (exigeVinculoTalisma) {
                    Text(
                        "Talismã: escolha uma magia do repertório para finalizar a rolagem.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ExposedDropdownMenuBox(
                        expanded = menuTalismaExpandido,
                        onExpandedChange = { menuTalismaExpandido = !menuTalismaExpandido },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = talismaMagiaVinculada.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Talismã: magia vinculada") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuTalismaExpandido)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = menuTalismaExpandido,
                            onDismissRequest = { menuTalismaExpandido = false }
                        ) {
                            repertorioParaTalisma.forEach { nomeMagia ->
                                DropdownMenuItem(
                                    text = { Text(nomeMagia) },
                                    onClick = {
                                        onTalismaVinculadoMudou(nomeMagia)
                                        menuTalismaExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = energiaManualInput,
                    onValueChange = onInputMudou,
                    label = { Text("Custo base da magia agora") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            if (isPraCegoVariant) contentDescription = "Informar energia gasta para a magia ${magiaEnergia.nome}"
                        }
                )
                Text(
                    "PF da rolagem atual: $pfAtualRolagem",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                energiaManualInput.toIntOrNull()?.let { custoBase ->
                    val reducao = MagiaEnergiaRules.reducaoPorNh(magiaEnergia.target)
                    val custoFinal = MagiaEnergiaRules.custoAjustadoPorNh(custoBase, magiaEnergia.target)
                    Text(
                        "Reducao por NH ${magiaEnergia.target}: -$reducao | custo final: $custoFinal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    energiaManualInput.toIntOrNull()?.let { custoBase ->
                        val custoFinal = MagiaEnergiaRules.custoAjustadoPorNh(custoBase, magiaEnergia.target)
                        onAplicar(custoFinal)
                    }
                    onDismiss()
                },
                enabled = energiaManualInput.toIntOrNull() != null &&
                    (!exigeVinculoTalisma || !talismaMagiaVinculada.isNullOrBlank())
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ignorar")
            }
        }
    )
}

@Composable
fun RolagemEditarPvDialog(
    pvFixoRolagem: Int,
    maxPvRolagem: Int,
    pvAtualInput: String,
    onInputMudou: (String) -> Unit,
    onSalvar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar PV da Rolagem") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // O piso e negativo de proposito: -1x PV e risco de morte,
                // -5x e morte automatica (MB p.423).
                Text("PV fixo: $pvFixoRolagem | Limite: ${-maxPvRolagem} a $maxPvRolagem")
                OutlinedTextField(
                    value = pvAtualInput,
                    onValueChange = onInputMudou,
                    label = { Text("PV atual") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Campo de pontos de vida da rolagem" }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSalvar) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun RolagemEditarPfDialog(
    pfFixoRolagem: Int,
    pfAtualInput: String,
    onInputMudou: (String) -> Unit,
    onSalvar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar PF da Rolagem") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PF fixo: $pfFixoRolagem | Minimo atual: 0")
                OutlinedTextField(
                    value = pfAtualInput,
                    onValueChange = onInputMudou,
                    label = { Text("PF atual") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Campo de pontos de fadiga da rolagem" }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSalvar) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolagemEditarCanalDialog(
    canaisDiscord: List<DiscordVoiceChannel>,
    canalSelecionadoNome: String?,
    canaisCarregando: Boolean,
    canaisErro: String?,
    backendOnline: Boolean,
    onAtualizarCanais: () -> Unit,
    onCanalSelecionado: (DiscordVoiceChannel) -> Unit,
    onDismiss: () -> Unit
) {
    var expandedCanal by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Canal de envio Discord") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedCanal,
                    onExpandedChange = { expandedCanal = !expandedCanal },
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Selecionar canal do Discord" }
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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCanal,
                        onDismissRequest = { expandedCanal = false }
                    ) {
                        canaisDiscord.forEach { canal ->
                            DropdownMenuItem(
                                text = { Text("${canal.guildName} / ${canal.name}") },
                                onClick = {
                                    onCanalSelecionado(canal)
                                    expandedCanal = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = onAtualizarCanais,
                    enabled = !canaisCarregando,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
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
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun EditarEsquivaBonusDialog(
    personagem: Personagem,
    bonusAtual: Int,
    notaAtual: String = "",
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var bonus by remember(bonusAtual) { mutableIntStateOf(bonusAtual.coerceIn(-20, 20)) }
    var texto by remember(bonusAtual) { mutableStateOf(if (bonusAtual >= 0) "+$bonusAtual" else "$bonusAtual") }
    var nota by remember(notaAtual) { mutableStateOf(notaAtual) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Esquiva") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "A Esquiva usa Deslocamento + 3 como base. O Bônus de Defesa (BD) do seu escudo já é adicionado automaticamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ComposicaoDaDefesa(personagem, DefenseType.ESQUIVA)
                OutlinedTextField(
                    value = texto,
                    onValueChange = { novo ->
                        val filtrado = novo.filterIndexed { index, c -> c.isDigit() || ((c == '+' || c == '-') && index == 0) }
                        if (filtrado.isBlank() || filtrado == "+" || filtrado == "-") {
                            texto = filtrado
                        } else {
                            val valor = filtrado.replace("+", "").toIntOrNull() ?: return@OutlinedTextField
                            bonus = valor.coerceIn(-20, 20)
                            texto = if (bonus >= 0) "+$bonus" else "$bonus"
                        }
                    },
                    singleLine = true,
                    label = { Text("Bônus") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            bonus = (bonus - 1).coerceIn(-20, 20)
                            texto = if (bonus >= 0) "+$bonus" else "$bonus"
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) { Text("-1") }
                    Button(
                        onClick = {
                            bonus = (bonus + 1).coerceIn(-20, 20)
                            texto = if (bonus >= 0) "+$bonus" else "$bonus"
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) { Text("+1") }
                }
                CampoNotaBonus(nota = nota, onNotaChange = { nota = it })
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(bonus, nota) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarAparaDialog(
    personagem: Personagem,
    pericias: List<PericiaSelecionada>,
    periciaSelecionadaId: String?,
    bonusAtual: Int,
    notaAtual: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String?, Int, String) -> Unit
) {
    var selectedPericiaId by remember(periciaSelecionadaId) { mutableStateOf(periciaSelecionadaId) }
    var expandedPericia by remember { mutableStateOf(false) }
    var bonus by remember(bonusAtual) { mutableIntStateOf(bonusAtual.coerceIn(-20, 20)) }
    var textoBonus by remember(bonusAtual) { mutableStateOf(if (bonusAtual >= 0) "+$bonusAtual" else "$bonusAtual") }
    var nota by remember(notaAtual) { mutableStateOf(notaAtual) }

    val periciaAtual = pericias.find { it.definicaoId == selectedPericiaId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Apara") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "O Apara é calculado como (Perícia / 2) + 3. O BD do escudo e vantagens também são somados automaticamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Lote NOTA-2: dizer QUAIS vantagens e QUAL escudo. "somados
                // automaticamente" nao deixa o jogador conferir a conta.
                ComposicaoDaDefesa(personagem, DefenseType.APARA)
                ExposedDropdownMenuBox(
                    expanded = expandedPericia,
                    onExpandedChange = { expandedPericia = !expandedPericia }
                ) {
                    OutlinedTextField(
                        value = periciaAtual?.let { "${it.nome} (${it.calcularNivel(personagem)})" } ?: "Nenhuma",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Perícia de combate") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPericia) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPericia,
                        onDismissRequest = { expandedPericia = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma") },
                            onClick = {
                                selectedPericiaId = null
                                expandedPericia = false
                            }
                        )
                        pericias.forEach { pericia ->
                            DropdownMenuItem(
                                text = { Text("${pericia.nome} (${pericia.calcularNivel(personagem)})") },
                                onClick = {
                                    selectedPericiaId = pericia.definicaoId
                                    expandedPericia = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = textoBonus,
                    onValueChange = { novo ->
                        val filtrado = novo.filterIndexed { index, c -> c.isDigit() || ((c == '+' || c == '-') && index == 0) }
                        if (filtrado.isBlank() || filtrado == "+" || filtrado == "-") {
                            textoBonus = filtrado
                        } else {
                            val valor = filtrado.replace("+", "").toIntOrNull() ?: return@OutlinedTextField
                            bonus = valor.coerceIn(-20, 20)
                            textoBonus = if (bonus >= 0) "+$bonus" else "$bonus"
                        }
                    },
                    singleLine = true,
                    label = { Text("Bônus") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            bonus = (bonus - 1).coerceIn(-20, 20)
                            textoBonus = if (bonus >= 0) "+$bonus" else "$bonus"
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) { Text("-1") }
                    Button(
                        onClick = {
                            bonus = (bonus + 1).coerceIn(-20, 20)
                            textoBonus = if (bonus >= 0) "+$bonus" else "$bonus"
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) { Text("+1") }
                }
                CampoNotaBonus(nota = nota, onNotaChange = { nota = it })
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selectedPericiaId, bonus, nota) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarBloqueioDialog(
    personagem: Personagem,
    pericias: List<PericiaSelecionada>,
    escudos: List<Equipamento>,
    periciaSelecionadaId: String?,
    escudoSelecionadoNome: String?,
    bonusAtual: Int,
    notaAtual: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String?, String?, Int, String) -> Unit
) {
    var selectedPericiaId by remember(periciaSelecionadaId) { mutableStateOf(periciaSelecionadaId) }
    var selectedEscudoNome by remember(escudoSelecionadoNome) { mutableStateOf(escudoSelecionadoNome) }
    var expandedPericia by remember { mutableStateOf(false) }
    var expandedEscudo by remember { mutableStateOf(false) }
    var bonus by remember(bonusAtual) { mutableIntStateOf(bonusAtual.coerceIn(-20, 20)) }
    var textoBonus by remember(bonusAtual) { mutableStateOf(if (bonusAtual >= 0) "+$bonusAtual" else "$bonusAtual") }
    var nota by remember(notaAtual) { mutableStateOf(notaAtual) }

    val periciaAtual = pericias.find { it.definicaoId == selectedPericiaId }
    val escudoAtual = escudos.find { it.nome.equals(selectedEscudoNome ?: "", ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Bloqueio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "O Bloqueio é calculado como (Perícia / 2) + 3. O BD do escudo e vantagens também são somados automaticamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ComposicaoDaDefesa(personagem, DefenseType.BLOQUEIO)
                ExposedDropdownMenuBox(
                    expanded = expandedPericia,
                    onExpandedChange = { expandedPericia = !expandedPericia }
                ) {
                    OutlinedTextField(
                        value = periciaAtual?.let { "${it.nome} (${it.calcularNivel(personagem)})" } ?: "Nenhuma",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Perícia de Escudo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPericia) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPericia,
                        onDismissRequest = { expandedPericia = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma") },
                            onClick = {
                                selectedPericiaId = null
                                expandedPericia = false
                            }
                        )
                        pericias.forEach { pericia ->
                            DropdownMenuItem(
                                text = { Text("${pericia.nome} (${pericia.calcularNivel(personagem)})") },
                                onClick = {
                                    selectedPericiaId = pericia.definicaoId
                                    expandedPericia = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedEscudo,
                    onExpandedChange = { expandedEscudo = !expandedEscudo }
                ) {
                    OutlinedTextField(
                        value = escudoAtual?.let { "${it.nome} (DB ${it.bonusDefesa ?: 0})" } ?: "Nenhum",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Escudo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEscudo) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEscudo,
                        onDismissRequest = { expandedEscudo = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Nenhum") },
                            onClick = {
                                selectedEscudoNome = null
                                expandedEscudo = false
                            }
                        )
                        escudos.forEach { escudo ->
                            DropdownMenuItem(
                                text = { Text("${escudo.nome} (DB ${escudo.bonusDefesa ?: 0})") },
                                onClick = {
                                    selectedEscudoNome = escudo.nome
                                    expandedEscudo = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = textoBonus,
                    onValueChange = { novo ->
                        val filtrado = novo.filterIndexed { index, c -> c.isDigit() || ((c == '+' || c == '-') && index == 0) }
                        if (filtrado.isBlank() || filtrado == "+" || filtrado == "-") {
                            textoBonus = filtrado
                        } else {
                            val valor = filtrado.replace("+", "").toIntOrNull() ?: return@OutlinedTextField
                            bonus = valor.coerceIn(-20, 20)
                            textoBonus = if (bonus >= 0) "+$bonus" else "$bonus"
                        }
                    },
                    singleLine = true,
                    label = { Text("Bônus") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            bonus = (bonus - 1).coerceIn(-20, 20)
                            textoBonus = if (bonus >= 0) "+$bonus" else "$bonus"
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) { Text("-1") }
                    Button(
                        onClick = {
                            bonus = (bonus + 1).coerceIn(-20, 20)
                            textoBonus = if (bonus >= 0) "+$bonus" else "$bonus"
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) { Text("+1") }
                }
                CampoNotaBonus(nota = nota, onNotaChange = { nota = it })
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selectedPericiaId, selectedEscudoNome, bonus, nota) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
