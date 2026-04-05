package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.appCardColors
import com.gurps.ficha.ui.SectionCard
import kotlin.math.abs

@Composable
fun RolagemHeader(
    canalSelecionadoNome: String?,
    backendOnline: Boolean,
    isVerySmallScreen: Boolean,
    compactLabelStyle: androidx.compose.ui.text.TextStyle,
    onEditCanal: () -> Unit
) {
    Button(
        onClick = onEditCanal,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
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
                fontSize = if (isVerySmallScreen) 16.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = canalSelecionadoNome ?: "Selecionar canal de voz",
                style = compactLabelStyle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AtributosQuickRollPanel(
    personagem: Personagem,
    atributosRapidos: List<String>,
    modificadoresAtributo: MutableMap<String, Int>,
    isPraCegoVariant: Boolean,
    cardTitleStyle: androidx.compose.ui.text.TextStyle,
    statsNumberStyle: androidx.compose.ui.text.TextStyle,
    compactLabelStyle: androidx.compose.ui.text.TextStyle,
    innerCardVerticalPadding: androidx.compose.ui.unit.Dp,
    onExecutarRolagem: (String, Int, Int) -> Unit
) {
    if (isPraCegoVariant) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            atributosRapidos.forEach { attr ->
                val valor = personagem.getAtributo(attr)
                val nomeAttr = atributoNomeCompleto(attr)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = appCardColors()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("$attr - $nomeAttr", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = valor.toString(),
                            modifier = Modifier
                                .semantics {
                                    contentDescription = "Rolar $attr $valor"
                                }
                                .clickable {
                                    onExecutarRolagem(attr, valor, 0)
                                },
                            textAlign = TextAlign.Center,
                            style = statsNumberStyle,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            atributosRapidos.forEach { attr ->
                val valor = personagem.getAtributo(attr)
                val modAttr = modificadoresAtributo[attr] ?: 0
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = innerCardVerticalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = attr,
                        textAlign = TextAlign.Center,
                        style = cardTitleStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
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
                                onExecutarRolagem(attr, valor, modAttr)
                            },
                        textAlign = TextAlign.Center,
                        style = statsNumberStyle,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    if (modAttr != 0) {
                        Text(
                            text = "mod ${if (modAttr >= 0) "+$modAttr" else modAttr}",
                            style = compactLabelStyle,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PvPfQuickRollPanel(
    pvFixo: Int,
    pvAtual: Int,
    pfFixo: Int,
    pfAtual: Int,
    isPraCegoVariant: Boolean,
    cardTitleStyle: androidx.compose.ui.text.TextStyle,
    defenseNumberStyle: androidx.compose.ui.text.TextStyle,
    innerCardVerticalPadding: androidx.compose.ui.unit.Dp,
    onEditPv: () -> Unit,
    onEditPf: () -> Unit,
    onAjustarPv: (Boolean) -> Unit,
    onAjustarPf: (Boolean) -> Unit
) {
    if (isPraCegoVariant) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = appCardColors()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = innerCardVerticalPadding),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("PV: $pvFixo/$pvAtual", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                    TextButton(
                        onClick = onEditPv,
                        modifier = Modifier.semantics { contentDescription = "Editar pontos de vida da rolagem" }
                    ) {
                        Text("Editar PV")
                    }
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = appCardColors()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = innerCardVerticalPadding),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("PF: $pfFixo/$pfAtual", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                    TextButton(
                        onClick = onEditPf,
                        modifier = Modifier.semantics { contentDescription = "Editar pontos de fadiga da rolagem" }
                    ) {
                        Text("Editar PF")
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = appCardColors()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .pointerInput(pvAtual) {
                            var dragAcumulado = 0f
                            val passoPx = 20f
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAcumulado += dragAmount
                                    while (abs(dragAcumulado) >= passoPx) {
                                        onAjustarPv(dragAcumulado < 0f)
                                        dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                    }
                                }
                            )
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PV", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$pvFixo/$pvAtual",
                        style = defenseNumberStyle,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = appCardColors()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .pointerInput(pfAtual) {
                            var dragAcumulado = 0f
                            val passoPx = 20f
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAcumulado += dragAmount
                                    while (abs(dragAcumulado) >= passoPx) {
                                        onAjustarPf(dragAcumulado < 0f)
                                        dragAcumulado += if (dragAcumulado < 0f) passoPx else -passoPx
                                    }
                                }
                            )
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PF", style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$pfFixo/$pfAtual",
                        style = defenseNumberStyle,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtaqueDanoQuickArea(
    opcoesAtaque: List<RollMappedOption>,
    ataqueAtual: RollMappedOption?,
    fonteDanoAtual: DamageSourceOption,
    gdp: String,
    geb: String,
    stDamageMode: StDamageMode,
    modificadorAtaque: Int,
    isPraCegoVariant: Boolean,
    isVerySmallScreen: Boolean,
    cardTitleStyle: androidx.compose.ui.text.TextStyle,
    defenseNumberStyle: androidx.compose.ui.text.TextStyle,
    compactLabelStyle: androidx.compose.ui.text.TextStyle,
    rowSpacing: androidx.compose.ui.unit.Dp,
    innerCardPadding: androidx.compose.ui.unit.Dp,
    innerCardVerticalPadding: androidx.compose.ui.unit.Dp,
    onConfigAtaque: () -> Unit,
    onConfigDano: () -> Unit,
    onUpdateStDamageMode: (StDamageMode) -> Unit,
    onModificarAtaque: (Int) -> Unit,
    onExecutarAtaque: (RollMappedOption, Int) -> Unit,
    onExecutarDano: (String) -> Unit
) {
    if (opcoesAtaque.isEmpty()) {
        Text(
            "Sem pericias para ataque. Adicione pericias de combate na aba Pericias.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            Button(
                onClick = onConfigAtaque,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "Ataque",
                    style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
            Button(
                onClick = onConfigDano,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "Dano",
                    style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        val modAtaqueAtual = if (isPraCegoVariant) 0 else modificadorAtaque
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!isPraCegoVariant) {
                        Modifier.pointerInput(modAtaqueAtual) {
                            var dragAcumulado = 0f
                            val passoPx = 20f
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAcumulado += dragAmount
                                    while (abs(dragAcumulado) >= passoPx) {
                                        if (dragAcumulado < 0f) {
                                            onModificarAtaque((modificadorAtaque + 1).coerceIn(-20, 20))
                                            dragAcumulado += passoPx
                                        } else {
                                            onModificarAtaque((modificadorAtaque - 1).coerceIn(-20, 20))
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
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(rowSpacing),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        colors = appCardColors()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = innerCardPadding, vertical = innerCardVerticalPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                ataqueAtual?.contextLabel?.removePrefix("Ataque ") ?: "Ataque",
                                style = cardTitleStyle,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "NH ${ataqueAtual?.target ?: "-"}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        contentDescription = "Rolar ${ataqueAtual?.contextLabel ?: "Ataque"} com nível ${ataqueAtual?.target ?: "-"}"
                                    }
                                    .clickable(enabled = ataqueAtual?.target != null) {
                                        ataqueAtual?.let { onExecutarAtaque(it, modAtaqueAtual) }
                                    },
                                style = defenseNumberStyle,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            if (!isPraCegoVariant && modAtaqueAtual != 0) {
                                Text(
                                    "mod ${if (modAtaqueAtual >= 0) "+$modAtaqueAtual" else "$modAtaqueAtual"}",
                                    style = compactLabelStyle,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = appCardColors()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = innerCardPadding, vertical = innerCardVerticalPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Text(
                                fonteDanoAtual.label,
                                style = cardTitleStyle,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            if (fonteDanoAtual.id == "st_base") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StDamageMode.entries.forEach { mode ->
                                        FilterChip(
                                            selected = stDamageMode == mode,
                                            onClick = { onUpdateStDamageMode(mode) },
                                            label = { Text(mode.label, style = compactLabelStyle) },
                                            modifier = Modifier.padding(horizontal = 2.dp)
                                        )
                                    }
                                }
                            }
                            val danos = splitDamageEntries(fonteDanoAtual.damageExpression)
                            danos.forEach { danoLinha ->
                                // Resolve GdP/GeB apenas para a verificação de "rolável" e execução
                                val danoResolvido = resolveStDamage(danoLinha, gdp, geb)
                                val parsed = parseDamageExpression(danoResolvido)
                                val danoRolavel = parsed != null
                                
                                // Formata o texto para exibir o nome completo do dano se for rolável
                                val textoExibicao = if (parsed != null) {
                                    val core = formatDamageCore(parsed)
                                    val label = formatDamageTypeLabel(parsed.suffix)
                                    "$core $label"
                                } else {
                                    danoLinha
                                }

                                Text(
                                    textoExibicao,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            contentDescription = "Rolar dano $danoLinha"
                                        }
                                        .clickable(enabled = danoRolavel) {
                                            onExecutarDano(danoResolvido)
                                        }
                                        .padding(vertical = 2.dp),
                                    style = cardTitleStyle,
                                    color = if (danoRolavel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    fontWeight = if (danoRolavel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (!isPraCegoVariant && modAtaqueAtual != 0) {
                                Text(
                                    "mod ${if (modAtaqueAtual >= 0) "+$modAtaqueAtual" else "$modAtaqueAtual"}",
                                    style = compactLabelStyle,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuBotoesNavegacaoRolagem(
    showTecnicas: Boolean,
    showMagias: Boolean,
    onShowPericias: () -> Unit,
    onShowTecnicas: () -> Unit,
    onShowMagias: () -> Unit,
    onShowRolagemLivre: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Button(
            onClick = onShowPericias,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(
                "Perícias",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        if (showTecnicas) {
            Button(
                onClick = onShowTecnicas,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(
                    "Técnicas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (showMagias) {
            Button(
                onClick = onShowMagias,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(
                    "Magias",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = onShowRolagemLivre,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(
                "Rolagem Livre",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HistoricoRolagemPanel(
    historico: List<HistoricoRolagemItem>,
    onReenviar: (Int, HistoricoRolagemItem) -> Unit
) {
    SectionCard(title = "Histórico da Sessão") {
        if (historico.isEmpty()) {
            Text(
                "Nenhuma rolagem ainda.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            historico.forEachIndexed { index, item ->
                Text(
                    item.texto,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.statusEnvio == "erro") {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                item.statusEnvio?.let { status ->
                    Text(
                        "envio: $status",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (status == "erro") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.statusEnvio == "erro" && !item.detalheErro.isNullOrBlank()) {
                    Text(
                        "detalhe: ${item.detalheErro}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedButton(
                        onClick = { onReenviar(index, item) }
                    ) {
                        Text("Reenviar")
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun DefesasAtivasQuickRollPanel(
    defesasAtivas: List<com.gurps.ficha.viewmodel.ActiveDefense>,
    modificadoresDefesa: MutableMap<com.gurps.ficha.viewmodel.DefenseType, Int>,
    isPraCegoVariant: Boolean,
    cardTitleStyle: androidx.compose.ui.text.TextStyle,
    defenseNumberStyle: androidx.compose.ui.text.TextStyle,
    compactLabelStyle: androidx.compose.ui.text.TextStyle,
    innerCardVerticalPadding: androidx.compose.ui.unit.Dp,
    onExecutarRolagem: (com.gurps.ficha.viewmodel.ActiveDefense, Int) -> Unit
) {
    if (defesasAtivas.isEmpty()) {
        Text(
            "Nenhuma defesa ativa configurada (Apara/Bloqueio). Configure na aba Combate.",
            style = compactLabelStyle,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        return
    }

    if (isPraCegoVariant) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            defesasAtivas.forEach { defesa ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = appCardColors()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(defesa.name, style = cardTitleStyle, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = defesa.finalValue.toString(),
                            modifier = Modifier
                                .semantics {
                                    contentDescription = "Rolar ${defesa.name} nível ${defesa.finalValue}"
                                }
                                .clickable {
                                    onExecutarRolagem(defesa, 0)
                                },
                            textAlign = TextAlign.Center,
                            style = defenseNumberStyle,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            defesasAtivas.forEach { defesa ->
                val modDef = modificadoresDefesa[defesa.type] ?: 0
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = innerCardVerticalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = defesa.name,
                        textAlign = TextAlign.Center,
                        style = cardTitleStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = defesa.finalValue.toString(),
                        modifier = Modifier
                            .pointerInput(defesa.type, modDef) {
                                var dragAcumulado = 0f
                                val passoPx = 20f
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragAcumulado += dragAmount
                                        while (abs(dragAcumulado) >= passoPx) {
                                            val atual = modificadoresDefesa[defesa.type] ?: 0
                                            if (dragAcumulado < 0f) {
                                                modificadoresDefesa[defesa.type] = (atual + 1).coerceIn(-20, 20)
                                                dragAcumulado += passoPx
                                            } else {
                                                modificadoresDefesa[defesa.type] = (atual - 1).coerceIn(-20, 20)
                                                dragAcumulado -= passoPx
                                            }
                                        }
                                    }
                                )
                            }
                            .clickable {
                                onExecutarRolagem(defesa, modDef)
                            },
                        textAlign = TextAlign.Center,
                        style = defenseNumberStyle,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (modDef != 0) {
                        Text(
                            text = "mod ${if (modDef >= 0) "+$modDef" else modDef}",
                            style = compactLabelStyle,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
