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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.R

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
            .padding(bottom = 12.dp, top = 8.dp),
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
                
                // Margem de 2px entre ícones (1dp de cada lado no padding horizontal do item)
            }
        }
    }
}

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
    // Base: 32dp + 10% (conforme solicitado) = ~35dp. Selecionado: ~70dp.
    val baseSize = 35.dp
    val targetSize = if (isSelected) 70.dp else baseSize
    val animatedSize by animateDpAsState(
        targetValue = targetSize,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "Size"
    )

    // Efeito RPGístico 1: Flutuação (Bobbing)
    val infiniteTransition = rememberInfiniteTransition(label = "RPGAction")
    val bobbingOffset by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = if (isSelected) (-8).dp else 0.dp,
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
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 1.dp) // Margem de 2px total entre itens
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
        // O nome aparece ACIMA dos outros ícones
        Column(
            horizontalAlignment = if (labelOnRight) Alignment.Start else Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Área da Label (Acima do ícone)
            Box(
                modifier = Modifier.height(30.dp),
                contentAlignment = if (labelOnRight) Alignment.BottomStart else Alignment.BottomEnd
            ) {
                if (isSelected) {
                    val labelOffset = if (labelOnRight) 75.dp else (-75).dp
                    
                    Row(
                        modifier = Modifier
                            .wrapContentWidth()
                            .offset(x = labelOffset)
                    ) {
                        TabNameLabel(label)
                    }
                }
            }

            // Ícone
            Box(
                modifier = Modifier.size(animatedSize),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize()
                )
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
