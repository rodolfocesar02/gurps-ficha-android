package com.gurps.ficha.ui.features.rolagem

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.ui.appCardColors
import com.gurps.ficha.ui.SectionCard
import com.gurps.ficha.ui.linhaAlternavel
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
            .height(50.dp),
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
    bonusStBracal: Int = 0,
    bonusDxBracal: Int = 0,
    onExecutarRolagem: (String, Int, Int) -> Unit
) {
    // Braçais ligadas: o atributo rolado passa a ser o dos braços. Cada uma
    // mexe SÓ no seu (MB p.89 e p.56) -- a ST Braçal nunca toca a DX e vice-versa.
    fun valorDe(attr: String): Int = personagem.getAtributo(attr) + when (attr) {
        "ST" -> bonusStBracal
        "DX" -> bonusDxBracal
        else -> 0
    }

    if (isPraCegoVariant) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            atributosRapidos.forEach { attr ->
                val valor = valorDe(attr)
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
                val valor = valorDe(attr)
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    onShowDescricao: (RollDescricaoDialog) -> Unit,
    onExecutarAtaque: (RollMappedOption, Int) -> Unit,
    onExecutarDano: (String) -> Unit,
    // Lote NOTA-2: de onde veio o bonus de dano. Vem pronta de fora porque
    // quem sabe montar a lista e a regra, nao o componente de tela.
    origensDoDano: List<com.gurps.ficha.domain.rules.traits.TraitRuleRegistry.OrigemDeBonus> = emptyList(),
    // Lote MAO-1: qual mao esta empunhando, e quanto isso custa nesta ficha.
    usandoMaoInabil: Boolean = false,
    rotuloDaMao: String = "",
    descricaoDaMao: String = "",
    onAlternarMao: () -> Unit = {},
    // Lote MIRA-1: toque longo no NH abre a lista de onde acertar.
    onAbrirMira: (RollMappedOption) -> Unit = {}
) {
    if (opcoesAtaque.isEmpty()) {
        Text(
            "Sem ataques disponíveis. Verifique se as perícias de combate estão na aba Perícias e se todos os pré-requisitos foram atendidos.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
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
                    .height(36.dp),
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
                    .height(36.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "Dano",
                    style = if (isVerySmallScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
        }

        // Seletor de mao: a penalidade e da SITUACAO, e a Ambidestria a zera.
        // Por isso o quadrado continua funcionando mesmo com a vantagem -- so o
        // numero some.
        if (rotuloDaMao.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .linhaAlternavel(
                        marcado = usandoMaoInabil,
                        descricao = descricaoDaMao,
                        onAlternar = onAlternarMao
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = usandoMaoInabil, onCheckedChange = null)
                Text(
                    rotuloDaMao,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 2.dp)
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = ataqueAtual != null) {
                                        ataqueAtual?.let {
                                            onShowDescricao(
                                                RollDescricaoDialog(
                                                    titulo = "Descrição: ${it.label}",
                                                    texto = it.descricao.ifBlank { "Sem descrição disponível." }
                                                )
                                            )
                                        }
                                    },
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "NH ${ataqueAtual?.target ?: "-"}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        contentDescription = "Rolar ${ataqueAtual?.contextLabel ?: "Ataque"} com nível ${ataqueAtual?.target ?: "-"}. Segure para escolher onde acertar."
                                    }
                                    // Toque = rola no torso (o padrao do livro).
                                    // Segurar = abre a mira. O toque simples,
                                    // que e o gesto de sempre, nao mudou.
                                    .combinedClickable(
                                        enabled = ataqueAtual?.target != null,
                                        onClick = { ataqueAtual?.let { onExecutarAtaque(it, modAtaqueAtual) } },
                                        onLongClick = { ataqueAtual?.let { onAbrirMira(it) } }
                                    ),
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
                            // "+1/dado Mestre de Armas" -- sem isto a Faca pula
                            // de 1d-3 para 2d-1 e nada diz por que.
                            com.gurps.ficha.ui.features.traits.OrigemDoBonusNumero(
                                origens = origensDoDano,
                                unidade = "/dado",
                                modifier = Modifier.fillMaxWidth()
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
    onShowRolagemLivre: () -> Unit,
    onShowResistencia: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Button(
            onClick = onShowPericias,
            modifier = Modifier.fillMaxWidth().height(42.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            Text(
                "Perícias",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Lote RESIST-1: tudo que e "resistir" num lugar so -- Reacao,
        // Autocontrole, consciencia, morte, doenca, veneno, medo.
        Button(
            onClick = onShowResistencia,
            modifier = Modifier.fillMaxWidth().height(42.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            Text(
                "Reação e Resistência",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        if (showTecnicas) {
            Button(
                onClick = onShowTecnicas,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
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
                modifier = Modifier.fillMaxWidth().height(42.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
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
            modifier = Modifier.fillMaxWidth().height(42.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
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
                        onClick = { onReenviar(index, item) },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Reenviar", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun SectionHeaderPraCego(titulo: String) {
    Text(
        text = "Sessão: $titulo",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
            .semantics { heading() },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
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
    onConfigEsquiva: () -> Unit,
    onConfigApara: () -> Unit,
    onConfigBloqueio: () -> Unit,
    onExecutarRolagem: (com.gurps.ficha.viewmodel.ActiveDefense, Int) -> Unit
) {
    if (defesasAtivas.isEmpty()) {
        if (isPraCegoVariant) SectionHeaderPraCego("Defesas Ativas")
        Text(
            "Nenhuma defesa ativa configurada.",
            style = compactLabelStyle,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        return
    }

    if (isPraCegoVariant) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SectionHeaderPraCego("Defesas Ativas")
            defesasAtivas.forEach { defesa ->
                val configClick = when (defesa.type) {
                    com.gurps.ficha.viewmodel.DefenseType.ESQUIVA -> onConfigEsquiva
                    com.gurps.ficha.viewmodel.DefenseType.APARA -> onConfigApara
                    com.gurps.ficha.viewmodel.DefenseType.BLOQUEIO -> onConfigBloqueio
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = appCardColors()
                ) {
                    val detailSuffix = if (!defesa.detail.isNullOrBlank()) " atual ${defesa.detail}" else ""
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                defesa.name,
                                style = cardTitleStyle,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        contentDescription = "Configurar ${defesa.name}$detailSuffix"
                                    }
                                    .clickable { configClick() }
                            )
                            if (!defesa.detail.isNullOrBlank()) {
                                Text(defesa.detail!!, style = compactLabelStyle)
                            }
                        }
                        Text(
                            text = defesa.finalValue.toString(),
                            modifier = Modifier
                                .semantics {
                                    contentDescription = "Rolar ${defesa.name} nível base ${defesa.finalValue}"
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
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            defesasAtivas.forEach { defesa ->
                val modDef = modificadoresDefesa[defesa.type] ?: 0
                val configClick = when (defesa.type) {
                    com.gurps.ficha.viewmodel.DefenseType.ESQUIVA -> onConfigEsquiva
                    com.gurps.ficha.viewmodel.DefenseType.APARA -> onConfigApara
                    com.gurps.ficha.viewmodel.DefenseType.BLOQUEIO -> onConfigBloqueio
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = appCardColors()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Button(
                            onClick = configClick,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .fillMaxWidth()
                                .height(32.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = defesa.name.uppercase(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                        }

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
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        val lblAtivo = when {
                            modDef != 0 -> "mod ${if (modDef >= 0) "+$modDef" else modDef}"
                            !defesa.detail.isNullOrBlank() -> defesa.detail!!
                            else -> ""
                        }
                        
                        if (lblAtivo.isNotBlank()) {
                            Text(
                                text = lblAtivo,
                                style = compactLabelStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
