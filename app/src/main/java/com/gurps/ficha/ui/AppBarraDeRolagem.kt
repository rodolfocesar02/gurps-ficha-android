package com.gurps.ficha.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
 * **A barra de rolagem que o Android não desenha** — Lote TELA-1.
 *
 * ## 🔴 O problema, achado no aparelho de outro jogador
 *
 * O Compose **não mostra barra de rolagem** numa lista de Android. Nada indica
 * que existe mais conteúdo abaixo: a tela simplesmente termina, e quem está
 * olhando conclui que aquilo é tudo.
 *
 * Isso passou despercebido durante o projeto inteiro porque, na fonte padrão, o
 * conteúdo quase sempre cabia. **Num aparelho com fonte grande, não cabe** — e o
 * jogador não tinha como saber que faltava metade do diálogo.
 *
 * ⚠️ Já aconteceu duas vezes nesta sessão do outro jeito: eu mesmo achei que o
 * diálogo estava truncado quando ele só não tinha sido rolado. Se **eu**, que
 * escrevi a tela, me confundi, o jogador na mesa não tem chance.
 *
 * ## Por que é desenho, e não um componente
 *
 * Sendo um `Modifier`, ela entra em qualquer container que já role — `Column`
 * com `verticalScroll`, `LazyColumn`, `LazyVerticalStaggeredGrid` — sem mexer
 * na estrutura de nenhuma tela. Um componente novo obrigaria a reescrever os
 * 44 diálogos do app.
 *
 * ⚠️ Ela **não** recebe toque. É indicador, não controle: quem arrasta a barra
 * espera precisão de mouse, e num dedo isso vira rolagem tremida. O gesto de
 * rolar continua sendo o da lista.
 *
 * ## Acessibilidade
 *
 * Nada aqui é anunciado pelo leitor de tela, e está certo: o leitor já navega
 * item a item e sabe onde está. A barra existe para **os olhos** — é a variante
 * `visual` que precisa dela.
 */
object AppBarraDeRolagem {

    /** A largura do traço. Fina o bastante para não roubar espaço do texto. */
    val LARGURA: Dp = 3.dp

    /** O tamanho mínimo do polegar, para ele não sumir em lista muito longa. */
    val MINIMO_DO_POLEGAR: Dp = 24.dp

    /** O quanto ela se destaca do fundo. Presente, sem competir com o conteúdo. */
    const val OPACIDADE = 0.35f
}

/**
 * **Rola e mostra que rola** — o jeito padrão do projeto.
 *
 * Substitui `Modifier.verticalScroll(rememberScrollState())`, que era o que
 * havia em **42 lugares** do app: todos rolavam, e nenhum dizia que rolava.
 *
 * ⚠️ Existe para que a barra não dependa de alguém lembrar dela. Enquanto fosse
 * um segundo passo opcional, o próximo diálogo nasceria sem — que é exatamente
 * como os 42 chegaram até aqui.
 */
@Composable
fun Modifier.rolagemVertical(
    cor: Color = MaterialTheme.colorScheme.primary
): Modifier {
    val estado = rememberScrollState()
    return this.verticalScroll(estado).comBarraDeRolagem(estado, cor)
}

/**
 * Desenha a barra ao lado de um conteúdo que rola com `verticalScroll`.
 *
 * ⚠️ Quando não há o que rolar (`maxValue == 0`), **nada é desenhado**. Uma barra
 * cheia numa lista curta mentiria dizendo "isto é tudo" — só que ela diria isso
 * do mesmo jeito numa lista longa, e o jogador aprenderia a ignorá-la.
 */
@Composable
fun Modifier.comBarraDeRolagem(
    estado: ScrollState,
    cor: Color
): Modifier = this.drawWithContent {
    drawContent()

    val rolavel = estado.maxValue
    if (rolavel <= 0) return@drawWithContent

    val alturaVisivel = size.height
    val alturaTotal = alturaVisivel + rolavel
    val largura = AppBarraDeRolagem.LARGURA.toPx()
    val minimo = AppBarraDeRolagem.MINIMO_DO_POLEGAR.toPx()

    val polegar = (alturaVisivel / alturaTotal * alturaVisivel).coerceAtLeast(minimo)
    // A sobra: o polegar tem tamanho mínimo, então ele percorre menos que a
    // altura toda. Sem isto, no fim da rolagem ele passaria da borda de baixo.
    val percurso = alturaVisivel - polegar
    val topo = (estado.value.toFloat() / rolavel) * percurso

    drawRoundRect(
        color = cor.copy(alpha = AppBarraDeRolagem.OPACIDADE),
        topLeft = Offset(size.width - largura, topo),
        size = Size(largura, polegar),
        cornerRadius = CornerRadius(largura / 2f, largura / 2f)
    )
}

/**
 * A mesma barra, para `LazyColumn`.
 *
 * ⚠️ Aqui a posição é **aproximada**, e não dá para ser exata: a lista preguiçosa
 * só conhece a altura dos itens que já desenhou. Um item enorme no fim faria a
 * barra "pular" no último trecho.
 *
 * Isso é aceitável porque o que ela precisa responder é *"existe mais coisa
 * embaixo?"*, e não *"quantos pixels faltam"*. Prometer precisão que a lista não
 * tem seria pior do que a aproximação.
 */
@Composable
fun Modifier.comBarraDeRolagem(
    estado: LazyListState,
    cor: Color
): Modifier = this.drawWithContent {
    drawContent()

    val total = estado.layoutInfo.totalItemsCount
    val visiveis = estado.layoutInfo.visibleItemsInfo.size
    if (total <= 0 || visiveis <= 0 || visiveis >= total) return@drawWithContent

    val alturaVisivel = size.height
    val largura = AppBarraDeRolagem.LARGURA.toPx()
    val minimo = AppBarraDeRolagem.MINIMO_DO_POLEGAR.toPx()

    val polegar = (visiveis.toFloat() / total * alturaVisivel).coerceAtLeast(minimo)
    val percurso = alturaVisivel - polegar
    val ultimoPrimeiroPossivel = (total - visiveis).coerceAtLeast(1)
    val progresso = (estado.firstVisibleItemIndex.toFloat() / ultimoPrimeiroPossivel)
        .coerceIn(0f, 1f)

    drawRoundRect(
        color = cor.copy(alpha = AppBarraDeRolagem.OPACIDADE),
        topLeft = Offset(size.width - largura, progresso * percurso),
        size = Size(largura, polegar),
        cornerRadius = CornerRadius(largura / 2f, largura / 2f)
    )
}
