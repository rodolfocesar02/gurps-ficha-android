package com.gurps.ficha.ui.saga

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.domain.combat.hex.Direcao
import com.gurps.ficha.domain.combat.hex.HexCoord
import com.gurps.ficha.domain.combat.hex.HexTaticoState
import com.gurps.ficha.domain.combat.hex.TokenDemo
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Lote HEX-2 (Fase 2a do PILAR): Canvas 2D que desenha a grade de hexágonos e responde ao toque.
 * Ainda NÃO integra com CombatSession — é a "prova de que a grade funciona". HEX-3 pluga as regras.
 *
 * Convenção visual: pointy-top, tamanho do hex escalado para caber ~15 hexes de diâmetro na largura da tela.
 * O motor (HexGrid/HexCoord) é kotlin puro e vive em domain/combat/hex/.
 */

private val COR_GRADE_LINHA = Color(0x66FFFFFF)
private val COR_GRADE_FUNDO = Color(0xFF1A2632)
private val COR_HEX_SELECIONADO = Color(0x44FFC107)
private val COR_TOKEN_HEROI = Color(0xFF3B82F6)
private val COR_TOKEN_INIMIGO = Color(0xFFEF4444)
private val COR_FACING = Color(0xCCFFFFFF)

@Composable
fun HexCanvasDemo(modifier: Modifier = Modifier) {
    // Estado local do canvas — na HEX-3 será elevado ao ViewModel/controller e vinculado ao CombatSession.
    var estado by remember { mutableStateOf(HexTaticoState.demoInicial()) }
    val textMeasurer = rememberTextMeasurer()

    Column(modifier = modifier.background(COR_GRADE_FUNDO)) {
        // Header: modo tático + o que fazer.
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⬢ Modo tático (demo)", color = Color.White, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            Text("Toque num token, depois num hex adjacente para mover", color = Color(0xCCFFFFFF),
                style = MaterialTheme.typography.labelSmall)
        }
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "Grade tática de hexágonos" }
                    .pointerInput(estado.raioGrade) {
                        detectTapGestures { toque ->
                            val hex = telaParaHex(toque, size.width.toFloat(), size.height.toFloat(), estado.raioGrade)
                            if (hex != null) estado = estado.aoTocarHex(hex)
                        }
                    }
            ) {
                val larguraPx = size.width
                val alturaPx = size.height
                val hexSizePx = tamanhoHex(larguraPx, alturaPx, estado.raioGrade)

                // Fundo já pintado na Column; agora só os hexes.
                for (hex in estado.hexesVisiveis) {
                    val (cx, cy) = hexParaTela(hex, larguraPx, alturaPx, hexSizePx)
                    val destacado = hex == estado.hexSelecionado
                    desenharHex(cx, cy, hexSizePx, destacado, textMeasurer, hex)
                }
                // Tokens desenhados por cima da grade (para não sumirem sob a linha).
                for (t in estado.tokens) {
                    val (cx, cy) = hexParaTela(t.posicao, larguraPx, alturaPx, hexSizePx)
                    val selecionado = t.id == estado.tokenSelecionadoId
                    desenharToken(cx, cy, hexSizePx, t, selecionado, textMeasurer)
                }
            }
        }
    }
}

// ── Geometria: hex ↔ tela (pointy-top, coordenadas axiais) ─────────────────

/** Tamanho (raio de centro à ponta) em pixels que cabe [raioGrade] hexes visíveis na área [larg]x[alt]. */
private fun tamanhoHex(larg: Float, alt: Float, raio: Int): Float {
    // Largura de um hex pointy-top = sqrt(3) * tamanho; altura = 2 * tamanho.
    // A grade de raio r tem largura ~ sqrt(3)*(2r+1)*tamanho e altura ~ 1.5*(2r)+2 * tamanho.
    val diametroCells = (2 * raio + 1).toFloat()
    val limPelaLargura = larg / (sqrt(3.0f) * diametroCells)
    val limPelaAltura = alt / (1.5f * (2 * raio) + 2f)
    return minOf(limPelaLargura, limPelaAltura) * 0.95f
}

/** Centro em pixels do hexágono [c] considerando origem no centro da tela. */
private fun hexParaTela(c: HexCoord, larg: Float, alt: Float, tam: Float): Pair<Float, Float> {
    val sqrt3 = sqrt(3.0f)
    val x = tam * (sqrt3 * c.q + sqrt3 / 2f * c.r) + larg / 2f
    val y = tam * (3f / 2f * c.r) + alt / 2f
    return x to y
}

/** Converte um toque em pixels para o `HexCoord` mais próximo (round-trip com arredondamento cube). */
private fun telaParaHex(toque: Offset, larg: Float, alt: Float, raio: Int): HexCoord? {
    val tam = tamanhoHex(larg, alt, raio)
    val sqrt3 = sqrt(3.0f)
    val x = (toque.x - larg / 2f) / tam
    val y = (toque.y - alt / 2f) / tam
    // Inverso do mapeamento acima (fórmula RedBlob para pointy-top).
    val qFrac = (sqrt3 / 3f * x - 1f / 3f * y)
    val rFrac = (2f / 3f * y)
    val sFrac = -qFrac - rFrac
    // Cube round.
    var rq = qFrac.toDouble().let { Math.round(it) }.toInt()
    var rr = rFrac.toDouble().let { Math.round(it) }.toInt()
    var rs = sFrac.toDouble().let { Math.round(it) }.toInt()
    val dq = kotlin.math.abs(rq - qFrac); val dr = kotlin.math.abs(rr - rFrac); val ds = kotlin.math.abs(rs - sFrac)
    if (dq > dr && dq > ds) rq = -rr - rs else if (dr > ds) rr = -rq - rs else rs = -rq - rr
    val hex = HexCoord(rq, rr)
    // Descartar toques FORA da grade visível.
    return if (HexCoord.ORIGEM.distancia(hex) <= raio) hex else null
}

// ── Desenho ────────────────────────────────────────────────────────────────

/** Estilos usados dentro do DrawScope (materializados fora do @Composable). */
private val ESTILO_LABEL_HEX = TextStyle(color = Color(0x77FFFFFF), fontSize = 9.sp)
private val ESTILO_INICIAL_TOKEN = TextStyle(color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center)

private fun DrawScope.desenharHex(
    cx: Float, cy: Float, tam: Float, destacado: Boolean,
    textMeasurer: TextMeasurer, hex: HexCoord
) {
    val path = Path()
    // Pointy-top: primeiro vértice a 30°, depois de 60 em 60.
    for (i in 0..5) {
        val ang = PI / 6 + i * PI / 3 // 30°, 90°, 150°...
        val vx = (cx + tam * cos(ang)).toFloat()
        val vy = (cy + tam * sin(ang)).toFloat()
        if (i == 0) path.moveTo(vx, vy) else path.lineTo(vx, vy)
    }
    path.close()
    if (destacado) drawPath(path, color = COR_HEX_SELECIONADO)
    drawPath(path, color = COR_GRADE_LINHA, style = Stroke(width = 1.5f))
    // Rótulo (q,r) minúsculo no topo do hex — útil pra depuração; some no HEX-7 (render 3D).
    val label = textMeasurer.measure(text = androidx.compose.ui.text.AnnotatedString("${hex.q},${hex.r}"),
        style = ESTILO_LABEL_HEX)
    drawText(label, topLeft = Offset(cx - label.size.width / 2f, cy - tam / 2f))
}

private fun DrawScope.desenharToken(
    cx: Float, cy: Float, tam: Float, t: TokenDemo, selecionado: Boolean,
    textMeasurer: TextMeasurer
) {
    val cor = if (t.ehHeroi) COR_TOKEN_HEROI else COR_TOKEN_INIMIGO
    val raio = tam * 0.6f
    drawCircle(color = cor, radius = raio, center = Offset(cx, cy))
    if (selecionado) drawCircle(color = Color.White, radius = raio + 4f, center = Offset(cx, cy),
        style = Stroke(width = 3f))
    // Indicador de FACING: pequena "flecha" na direção que o token olha (útil pra HEX-4).
    val angFacing = anguloDaDirecao(t.facing)
    val fx = (cx + raio * 0.9f * cos(angFacing)).toFloat()
    val fy = (cy + raio * 0.9f * sin(angFacing)).toFloat()
    drawLine(color = COR_FACING, start = Offset(cx, cy), end = Offset(fx, fy), strokeWidth = 3f)
    // Inicial do nome no meio do token (padrão B7).
    val inicial = t.nome.firstOrNull()?.uppercase() ?: "?"
    val label = textMeasurer.measure(text = androidx.compose.ui.text.AnnotatedString(inicial),
        style = ESTILO_INICIAL_TOKEN)
    drawText(label, topLeft = Offset(cx - label.size.width / 2f, cy - label.size.height / 2f))
}

/** Ângulo em radianos (frame de tela: 0 = leste, cresce para SUL) para cada direção. Pointy-top. */
private fun anguloDaDirecao(d: Direcao): Double = when (d) {
    Direcao.LESTE     -> 0.0
    Direcao.SUDESTE   -> PI / 3
    Direcao.SUDOESTE  -> 2 * PI / 3
    Direcao.OESTE     -> PI
    Direcao.NOROESTE  -> 4 * PI / 3
    Direcao.NORDESTE  -> 5 * PI / 3
}
