package com.gurps.ficha.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Barra de rolagem visível para conteúdo com `Modifier.verticalScroll`.
 *
 * ## Por que precisa existir
 *
 * `Column(Modifier.verticalScroll(...))` **rola, mas não desenha barra nenhuma**
 * — o Compose não tem uma pronta para esse caso (só as listas preguiçosas têm).
 * O resultado é um diálogo que parece truncado: o texto some no meio da frase e
 * nada indica que há mais embaixo.
 *
 * Foi o que o usuário reportou em 28/07 nas descrições de perícia. O
 * Arrombamento e a Mecânica têm páginas de texto, e a única pista de que dava
 * para rolar era tentar.
 *
 * ## Como usar
 *
 * O estado do scroll precisa ser o **mesmo** nos dois lugares, então tem que ser
 * criado antes:
 *
 * ```kotlin
 * val rolagem = rememberScrollState()
 * Column(
 *     modifier = Modifier
 *         .barraDeRolagem(rolagem)      // desenha por cima, no canto direito
 *         .verticalScroll(rolagem)
 *         .padding(end = 10.dp)         // afasta o texto da barra
 * ) { ... }
 * ```
 *
 * A ordem importa: `barraDeRolagem` vem **antes** do `verticalScroll` para
 * enxergar a largura cheia do nó, e o `padding` vem **depois** para o texto não
 * correr por baixo dela.
 *
 * ## Comportamento
 *
 * Não desenha nada quando o conteúdo cabe na tela — barra parada é ruído visual,
 * e pior, sugere que há mais conteúdo quando não há.
 *
 * A altura do cursor é proporcional: texto curto dá cursor grande, texto de
 * várias páginas dá cursor pequeno. É a mesma pista que o navegador dá — só de
 * olhar, dá para saber se falta pouco ou muito.
 */
@Composable
fun Modifier.barraDeRolagem(
    estado: ScrollState,
    cor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    largura: Dp = 4.dp
): Modifier = this.drawWithContent {
    drawContent()

    val maximo = estado.maxValue
    // maxValue == 0 → cabe tudo; Int.MAX_VALUE → ainda não foi medido.
    if (maximo <= 0 || maximo == Int.MAX_VALUE) return@drawWithContent

    val alturaVisivel = size.height
    val alturaTotal = alturaVisivel + maximo
    val alturaCursor = (alturaVisivel / alturaTotal * alturaVisivel)
        .coerceAtLeast(largura.toPx() * 4)   // cursor mínimo, para não sumir
    val fracaoPercorrida = estado.value.toFloat() / maximo
    val topo = fracaoPercorrida * (alturaVisivel - alturaCursor)

    val larguraPx = largura.toPx()
    drawRoundRect(
        color = cor,
        topLeft = Offset(size.width - larguraPx, topo),
        size = Size(larguraPx, alturaCursor),
        cornerRadius = CornerRadius(larguraPx / 2)
    )
}
