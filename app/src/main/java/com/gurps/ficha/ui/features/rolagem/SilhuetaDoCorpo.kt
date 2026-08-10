package com.gurps.ficha.ui.features.rolagem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.gurps.ficha.R
import com.gurps.ficha.domain.rules.MapaDaSilhueta
import com.gurps.ficha.ui.AppBotaoSecundario
import com.gurps.ficha.ui.UiEstilos
import com.gurps.ficha.ui.UiTokens

/**
 * **A silhueta do botão PV** — Lote PV-1b.
 *
 * O jogador toca o corpo em vez de procurar o nome numa lista de onze itens.
 * Toca a cabeça, a tela dá zoom nela, e aí ele escolhe entre crânio, rosto,
 * olho e pescoço.
 *
 * ## Só na variante visual
 *
 * Uma silhueta não se tateia: na `pracego` continuam os quadradinhos de sempre.
 * Isto aqui **não substitui** aquela lista, coexiste com ela.
 *
 * ## 🔴 O realce sai do mesmo lugar que o toque
 *
 * O destaque não é um polígono desenhado à parte — ele é montado com
 * [MapaDaSilhueta.Mascara.faixasDaRegiao], que por dentro chama o **mesmo**
 * `idEm` que decide o toque. Então realce e toque não têm como discordar: não
 * existe a situação de o app pintar o braço e registrar o tronco.
 *
 * Se fossem duas fontes — um polígono para pintar e uma regra para tocar —
 * bastaria alguém mexer numa para as duas divergirem em silêncio, e as duas
 * telas continuariam parecendo certas.
 *
 * ## ⚠️ O zoom é a mesma imagem, não outra
 *
 * As três telas são **recortes exatos 1:1** do corpo inteiro (conferido: 100% de
 * coincidência de traço). Então o zoom não troca de arquivo — ele move a janela
 * sobre a mesma imagem. Não existe salto nem desalinhamento possível.
 */
@Composable
fun SilhuetaDoCorpo(
    selecionada: MapaDaSilhueta.Regiao?,
    onSelecionar: (MapaDaSilhueta.Regiao) -> Unit,
    modifier: Modifier = Modifier,
    altura: androidx.compose.ui.unit.Dp = 340.dp
) {
    val contexto = LocalContext.current
    val arte = ImageBitmap.imageResource(R.drawable.silhueta_corpo)
    val mascara = remember {
        contexto.assets.open(MapaDaSilhueta.ARQUIVO_DA_MASCARA).bufferedReader().use {
            MapaDaSilhueta.lerMascara(it.readLines().asSequence())
        }
    }

    var telaAberta by remember { mutableStateOf<MapaDaSilhueta.Tela?>(null) }

    // A janela sobre a imagem. Sem zoom é a imagem inteira; com zoom é o recorte.
    val alvoX0 = (telaAberta?.x0 ?: 0).toFloat()
    val alvoY0 = (telaAberta?.y0 ?: 0).toFloat()
    val alvoX1 = (telaAberta?.x1 ?: MapaDaSilhueta.LARGURA).toFloat()
    val alvoY1 = (telaAberta?.y1 ?: MapaDaSilhueta.ALTURA).toFloat()
    val duracao = tween<Float>(durationMillis = 260)
    val x0 by animateFloatAsState(alvoX0, duracao, label = "x0")
    val y0 by animateFloatAsState(alvoY0, duracao, label = "y0")
    val x1 by animateFloatAsState(alvoX1, duracao, label = "x1")
    val y1 by animateFloatAsState(alvoY1, duracao, label = "y1")

    // O caminho do realce, em coordenadas da ARTE. Só é refeito quando a seleção
    // muda — não a cada quadro da animação.
    val realce = remember(selecionada?.id) {
        val p = Path()
        selecionada?.let { r ->
            // Só as linhas da tela dela: varrer as 1.555 linhas da arte a cada
            // toque daria uma engasgada visível, e o resto nem apareceria.
            for (y in r.tela.y0 until r.tela.y1) {
                mascara.faixasDaRegiao(r.id, y).forEach { faixa ->
                    p.addRect(
                        androidx.compose.ui.geometry.Rect(
                            faixa.first.toFloat(), y.toFloat(),
                            (faixa.last + 1).toFloat(), (y + 1).toFloat()
                        )
                    )
                }
            }
        }
        p
    }

    val corRealce = MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
    val descricao = telaAberta?.let { "Silhueta ampliada: ${it.rotulo}." }
        ?: "Silhueta do corpo. Toque numa região para ampliar."

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(UiTokens.ItemSpacing)
    ) {
        Text(
            telaAberta?.rotulo ?: "Toque onde o golpe acertou",
            style = UiEstilos.subtituloDialogo,
            fontWeight = FontWeight.SemiBold
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(altura)
                // 🔴 O Canvas do Compose NÃO recorta sozinho. Sem isto a imagem
                // inteira é desenhada escalada e vaza para fora da área dela: no
                // zoom do tronco as pernas passavam por cima dos botões, e no das
                // pernas as mãos apareciam em cima do título.
                .clipToBounds()
                .semantics { contentDescription = descricao }
                .pointerInput(telaAberta) {
                    detectTapGestures { toque ->
                        val v = Janela(x0, y0, x1, y1, size.width.toFloat(), size.height.toFloat())
                        val ix = ((toque.x - v.dx) / v.escala).toInt()
                        val iy = ((toque.y - v.dy) / v.escala).toInt()
                        if (ix !in 0 until MapaDaSilhueta.LARGURA) return@detectTapGestures
                        if (iy !in 0 until MapaDaSilhueta.ALTURA) return@detectTapGestures
                        val aberta = telaAberta
                        if (aberta == null) {
                            telaAberta = MapaDaSilhueta.telaEm(iy)
                        } else {
                            // ⚠️ Um recorte pega um naco da tela vizinha (o ombro
                            // aparece na tela da cabeça). Tocar ali não seleciona
                            // nada, em vez de escolher uma parte que o jogador
                            // não está vendo direito.
                            MapaDaSilhueta.regiaoEm(ix, iy)
                                ?.takeIf { it.tela == aberta }
                                ?.let(onSelecionar)
                        }
                    }
                }
        ) {
            val v = Janela(x0, y0, x1, y1, size.width, size.height)
            drawImage(
                image = arte,
                dstOffset = IntOffset(v.dx.toInt(), v.dy.toInt()),
                dstSize = IntSize(
                    (MapaDaSilhueta.LARGURA * v.escala).toInt(),
                    (MapaDaSilhueta.ALTURA * v.escala).toInt()
                )
            )
            if (selecionada != null) {
                desenharRealce(realce, v, corRealce)
            }
        }

        if (telaAberta != null) {
            AppBotaoSecundario(
                texto = "Ver o corpo inteiro",
                onClick = { telaAberta = null },
                larguraTotal = true
            )
        }
    }
}

/**
 * A conta que leva coordenada da arte para coordenada da tela, e de volta.
 *
 * É a mesma para desenhar e para o toque — de propósito. Duas contas parecidas
 * escritas em lugares diferentes é como o toque acaba a alguns pixels do
 * desenho, e ninguém percebe até um alvo pequeno ficar impossível de acertar.
 */
private class Janela(
    x0: Float, y0: Float, x1: Float, y1: Float,
    larguraDaTela: Float, alturaDaTela: Float
) {
    val escala: Float = minOf(
        larguraDaTela / (x1 - x0).coerceAtLeast(1f),
        alturaDaTela / (y1 - y0).coerceAtLeast(1f)
    )
    val dx: Float = -x0 * escala + (larguraDaTela - (x1 - x0) * escala) / 2f
    val dy: Float = -y0 * escala + (alturaDaTela - (y1 - y0) * escala) / 2f
}

/**
 * O caminho vem em coordenadas da arte; os dois blocos aninhados o levam para a
 * tela sem ambiguidade de ordem — primeiro a escala, depois o deslocamento.
 */
private fun DrawScope.desenharRealce(
    caminho: Path,
    v: Janela,
    cor: androidx.compose.ui.graphics.Color
) {
    translate(left = v.dx, top = v.dy) {
        scale(v.escala, v.escala, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            drawPath(caminho, cor)
        }
    }
}
