package com.gurps.ficha.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.R

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun FichaCustomNavigationBar(
    tabs: List<String>,
    currentIndex: Int,
    onTabClick: (Int) -> Unit,
    onMestreIAClick: () -> Unit = {},
    onMestreIALongPress: () -> Unit = {},
    mestreIAAberto: Boolean = false,
    estadoVoz: EstadoVoz = EstadoVoz.OCIOSO,
    estadoLive: EstadoLive = EstadoLive.OCIOSO,
    isPraCegoVariant: Boolean = false
) {
    // Mapeia EstadoLive para EstadoVoz para reutilizar o anel visual existente
    val estadoVozEfetivo = when {
        estadoLive == EstadoLive.OUVINDO      -> EstadoVoz.ESCUTANDO    // anel verde
        estadoLive == EstadoLive.FALANDO      -> EstadoVoz.PROCESSANDO  // anel amarelo
        estadoLive == EstadoLive.CONECTANDO   -> EstadoVoz.PROCESSANDO  // anel amarelo
        estadoLive == EstadoLive.PROCESSANDO  -> EstadoVoz.PROCESSANDO  // anel amarelo
        else -> estadoVoz
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 1.dp, top = 4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            // Ícone Mestre IA fixo à esquerda — toque abre chat, segurar ativa voz
            RPGNavigationItem(
                label = "Mestre IA",
                iconRes = R.drawable.tab_mestre_ia,
                isSelected = mestreIAAberto,
                labelOnRight = true,
                isPraCegoVariant = isPraCegoVariant,
                estadoVoz = estadoVozEfetivo,
                onClick = onMestreIAClick,
                onLongClick = onMestreIALongPress
            )

            // Abas principais empurradas para a direita
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = index == currentIndex

                    val iconRes = when (title) {
                        "Geral" -> R.drawable.tab_geral
                        "Traços" -> R.drawable.tab_tracos
                        "Perícias" -> R.drawable.tab_pericias
                        "Técnicas" -> R.drawable.tab_tecnicas
                        "Magia" -> R.drawable.tab_magia
                        "Equip." -> R.drawable.tab_equipamentos
                        "Defesas" -> R.drawable.tab_defesas
                        "Rolagem" -> R.drawable.tab_rolagem
                        "Saga" -> R.drawable.tab_mestre_ia
                        else -> R.drawable.tab_geral
                    }

                    // Primeiros 3 (0,1,2) -> Nome na Direita. Restante -> Esquerda.
                    val labelOnRight = index <= 2

                    RPGNavigationItem(
                        label = title,
                        iconRes = iconRes,
                        isSelected = isSelected,
                        labelOnRight = labelOnRight,
                        isPraCegoVariant = isPraCegoVariant,
                        onClick = { onTabClick(index) }
                    )

                    if (index < tabs.size - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun RPGNavigationItem(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    labelOnRight: Boolean,
    isPraCegoVariant: Boolean,
    estadoVoz: EstadoVoz = EstadoVoz.OCIOSO,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Animação de Tamanho (Zoom 2x ao selecionar)
    // Reduzido em 10% conforme solicitado (Base: ~31dp, Selecionado: ~62dp)
    val baseSize = 31.dp
    val targetSize = if (isSelected) 62.dp else baseSize
    val animatedSize by animateDpAsState(
        targetValue = targetSize,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "Size"
    )

    // Efeito RPGístico 1: Flutuação (Bobbing)
    val infiniteTransition = rememberInfiniteTransition(label = "RPGAction")
    val bobbingOffset by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = if (isSelected) (-6).dp else 0.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bobbing"
    )

    // Efeito RPGístico 2: Brilho Pulsante (Glow)
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = if (isSelected) 1.4f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val escutando = estadoVoz == EstadoVoz.ESCUTANDO || estadoVoz == EstadoVoz.PROCESSANDO

    // Anel pulsante durante escuta de voz
    val vozRingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (escutando) 1.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VozRing"
    )
    val vozRingAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (escutando) 0.6f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VozAlpha"
    )
    val vozColor = if (estadoVoz == EstadoVoz.PROCESSANDO) Color(0xFFFFA000) else Color(0xFF4CAF50)

    val clickModifier = if (onLongClick != null) {
        Modifier.pointerInput(onClick, onLongClick) {
            detectTapGestures(
                onTap = { onClick() },
                onLongPress = { onLongClick() }
            )
        }
    } else {
        Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .semantics {
                contentDescription = when {
                    escutando -> "$label (Escutando voz)"
                    isSelected -> "$label (Aba Selecionada)"
                    else -> "Aba $label"
                }
            }
            .then(clickModifier)
            .padding(horizontal = 4.dp)
            .offset(y = bobbingOffset),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Anel verde/amarelo pulsante ao escutar
        if (escutando) {
            Box(
                modifier = Modifier
                    .size(animatedSize * vozRingScale)
                    .alpha(vozRingAlpha)
                    .drawBehind {
                        drawCircle(color = vozColor)
                    }
            )
        }

        // Camada de Brilho atrás do ícone selecionado
        if (isSelected && !isPraCegoVariant) {
            Box(
                modifier = Modifier
                    .size(animatedSize)
                    .drawBehind {
                        scale(glowScale) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                        }
                    }
            )
        }

        // Layout de Label + Ícone
        Box(
            modifier = Modifier.size(animatedSize),
            contentAlignment = Alignment.Center
        ) {
            // O Ícone propriamente dito (agora decorativo, pois o pai já descreve)
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            // Nome da Aba (Apenas se selecionado)
            if (isSelected) {
                // Cálculo Matemático p/ Fora do Ícone:
                // O texto começa no centro (0), então precisamos pular o raio (size/2) 
                // e mais a outra metade para sair do ícone totalmente, mais o gap.
                // Total = animatedSize + gap.
                val gap = 15.dp
                val lateralOffset = animatedSize + gap
                
                Box(
                    modifier = Modifier
                        .wrapContentSize(align = if (labelOnRight) Alignment.CenterStart else Alignment.CenterEnd, unbounded = true)
                        .offset(x = if (labelOnRight) lateralOffset else -lateralOffset, y = (-23).dp) // Baixado 10% para economizar tela
                        .semantics { invisibleToUser() } // Evitar leitura redundante do texto
                ) {
                    TabNameLabel(label)
                }
            }
        }
    }
}

@Composable
private fun TabNameLabel(text: String) {
    // Efeito RPGístico 3: Entrada Suave do Nome
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + expandHorizontally(),
        exit = fadeOut()
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                    blurRadius = 4f
                )
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false
        )
    }
}
