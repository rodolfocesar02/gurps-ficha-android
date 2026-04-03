package com.gurps.ficha.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FichaCustomNavigationBar(
    tabs: List<String>,
    currentIndex: Int,
    onTabClick: (Int) -> Unit,
    isPraCegoVariant: Boolean = false
) {
    // Container da Barra de Navegação (Fundo Transparente)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 1.dp, top = 4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
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
                    else -> R.drawable.tab_geral
                }

                // Regra do Usuário: Primeiros 3 (0,1,2) -> Nome na Direita. Restante -> Esquerda.
                val labelOnRight = index <= 2

                RPGNavigationItem(
                    label = title,
                    iconRes = iconRes,
                    isSelected = isSelected,
                    labelOnRight = labelOnRight,
                    isPraCegoVariant = isPraCegoVariant,
                    onClick = { onTabClick(index) }
                )
                
                // Espaçamento total de 12dp entre os itens (4 + 4 + 4)
                if (index < tabs.size - 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RPGNavigationItem(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    labelOnRight: Boolean,
    isPraCegoVariant: Boolean,
    onClick: () -> Unit
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

    Box(
        modifier = Modifier
            .wrapContentSize()
            .semantics { 
                contentDescription = if (isSelected) "$label (Aba Selecionada)" else "Aba $label"
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp) // Parte do cálculo dos 12dp totais entre ícones
            .offset(y = bobbingOffset),
        contentAlignment = Alignment.BottomCenter
    ) {
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
