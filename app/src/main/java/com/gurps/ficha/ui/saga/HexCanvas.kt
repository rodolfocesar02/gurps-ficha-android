package com.gurps.ficha.ui.saga

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gurps.ficha.data.storage.TokenImageStore
import com.gurps.ficha.domain.combat.hex.Direcao
import com.gurps.ficha.domain.combat.hex.HexCoord
import com.gurps.ficha.domain.combat.hex.HexTaticoState
import com.gurps.ficha.domain.combat.hex.TokenDemo
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Lote HEX-2 (Fase 2a do PILAR): Canvas 2D que desenha a grade de hexágonos e responde ao toque.
 *
 * Lote TOK-1 (VTT 2D): o canvas evoluiu para o estilo mesa de RPG —
 *  - Token do HERÓI = retrato do personagem recortado CIRCULAR (via [TokenImageStore]) com borda
 *    azul e seta de facing na borda. Sem retrato → círculo colorido + inicial (fallback eterno).
 *  - Hexes VÁLIDOS para mover destacados em VERDE; aviso "Muito longe" quando o toque não completa.
 *  - Movimento ANIMADO (200 ms) nas coordenadas axiais — o token desliza em vez de teleportar.
 *  - Fundo cinza por enquanto; fundo gerado pelo Narrador é o TOK-3.
 *
 * Convenção visual: pointy-top, tamanho do hex escalado para caber a grade na tela.
 * O motor (HexGrid/HexCoord) é kotlin puro e vive em domain/combat/hex/.
 */

private val COR_GRADE_LINHA = Color(0x66FFFFFF)
private val COR_GRADE_FUNDO = Color(0xFF1A2632)
private val COR_HEX_SELECIONADO = Color(0x44FFC107)
private val COR_HEX_VALIDO_2D = Color(0x5910B981)   // verde translúcido — vizinho válido pra mover
private val COR_TOKEN_HEROI = Color(0xFF3B82F6)
private val COR_TOKEN_INIMIGO = Color(0xFFEF4444)
private val COR_FACING = Color(0xCCFFFFFF)
private const val SQRT3 = 1.7320508f

/**
 * Wrapper do modo tático: carrega o token de imagem do herói a partir do retrato da ficha
 * (assíncrono, com cache em disco) e injeta no canvas. É o entry-point usado pelo TabSaga
 * (combate real e preview standalone).
 */
@Composable
fun HexCanvasTatico(viewModel: FichaViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val retratoUri = viewModel.personagem.imagemPersonagemOriginalUri
    var tokenHeroi by remember(retratoUri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(retratoUri) {
        tokenHeroi = TokenImageStore.obterTokenHeroi(context, retratoUri)?.asImageBitmap()
    }
    HexCanvasDemo(modifier = modifier, tokenHeroi = tokenHeroi)
}

@Composable
fun HexCanvasDemo(modifier: Modifier = Modifier, tokenHeroi: ImageBitmap? = null) {
    // Estado local do canvas — na integração com CombatSession real (TOK-4) será elevado ao controller.
    var estado by remember { mutableStateOf(HexTaticoState.demoInicial()) }
    val textMeasurer = rememberTextMeasurer()
    val context = LocalContext.current

    // Lote TOK-2: imagens dos INIMIGOS por tipo (chave = nome normalizado). Primeiro tenta o cache
    // em disco (pré-aquecido pelo gatilho do iniciar_combate); se não existe, GERA on-demand via
    // Gemini (uma vez por tipo, cache eterno) — assim o preview demo também exercita o TOK-2.
    // Enquanto não chega (ou se falhar), o token fica no fallback círculo+inicial.
    var tokensInimigos by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }
    val nomesInimigos = estado.tokens.filter { !it.ehHeroi }.map { it.nome }.distinct()
    LaunchedEffect(nomesInimigos) {
        for (nome in nomesInimigos) {
            val chave = TokenImageStore.normalizarTipo(nome)
            if (chave.isBlank() || tokensInimigos.containsKey(chave)) continue
            var bmp = TokenImageStore.tokenInimigoCacheado(context, nome)
            if (bmp == null) {
                val imgKey = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_IMAGE_KEY
                val imgModel = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_IMAGE_MODEL
                if (imgKey.isNotBlank()) {
                    bmp = TokenImageStore.obterTokenInimigo(context, tipo = nome) { prompt ->
                        com.gurps.ficha.data.network.GeminiImageService
                            .gerarImagem(imgKey, imgModel, prompt, rotuloLog = "token:$chave")?.bytes
                    }
                }
            }
            if (bmp != null) tokensInimigos = tokensInimigos + (chave to bmp.asImageBitmap())
        }
    }

    // Aviso "Muito longe" some após 2 segundos.
    LaunchedEffect(estado.ultimoAviso) {
        if (estado.ultimoAviso != null) {
            kotlinx.coroutines.delay(2000)
            estado = estado.copy(ultimoAviso = null)
        }
    }

    // Posições ANIMADAS por token, em coordenadas axiais-neutras (sem escala de tela):
    //   ax = √3·q + √3/2·r ; ay = 1.5·r — mesma fórmula de hexParaTela sem o `tam` e o offset.
    // Animar aqui (e multiplicar por `tam` só no draw) mantém a animação correta em qualquer resize.
    val posAnimadas = mutableMapOf<String, Pair<Float, Float>>()
    for (t in estado.tokens) {
        key(t.id) {
            val axAlvo = SQRT3 * t.posicao.q + SQRT3 / 2f * t.posicao.r
            val ayAlvo = 1.5f * t.posicao.r
            val ax by animateFloatAsState(axAlvo, tween(200), label = "ax")
            val ay by animateFloatAsState(ayAlvo, tween(200), label = "ay")
            posAnimadas[t.id] = ax to ay
        }
    }

    Column(modifier = modifier.background(COR_GRADE_FUNDO)) {
        // Header: modo tático + instrução + aviso transitório.
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⬢ Modo tático", color = Color.White, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Text("Toque no token → hex VERDE", color = Color(0xCCFFFFFF),
                    style = MaterialTheme.typography.labelSmall)
            }
            if (estado.ultimoAviso != null) {
                Text("⚠ ${estado.ultimoAviso}", color = Color(0xFFEF4444),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 2.dp))
            }
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

                // Grade.
                for (hex in estado.hexesVisiveis) {
                    val (cx, cy) = hexParaTela(hex, larguraPx, alturaPx, hexSizePx)
                    val destacado = hex == estado.hexSelecionado
                    desenharHex(cx, cy, hexSizePx, destacado, textMeasurer, hex)
                }
                // Hexes VÁLIDOS para mover — preenchimento verde translúcido.
                for (hexValido in estado.hexesValidosParaMover) {
                    val (cx, cy) = hexParaTela(hexValido, larguraPx, alturaPx, hexSizePx)
                    desenharHexPreenchido(cx, cy, hexSizePx, COR_HEX_VALIDO_2D)
                }
                // Tokens por cima da grade — posição ANIMADA. Herói usa o retrato; inimigo usa a
                // imagem gerada (TOK-2) quando o cache/geração já entregou; fallback círculo+inicial.
                for (t in estado.tokens) {
                    val (ax, ay) = posAnimadas[t.id] ?: continue
                    val cx = hexSizePx * ax + larguraPx / 2f
                    val cy = hexSizePx * ay + alturaPx / 2f
                    val selecionado = t.id == estado.tokenSelecionadoId
                    val imagem = if (t.ehHeroi) tokenHeroi
                        else tokensInimigos[TokenImageStore.normalizarTipo(t.nome)]
                    if (imagem != null) {
                        desenharTokenImagem(cx, cy, hexSizePx, t, selecionado, imagem)
                    } else {
                        desenharToken(cx, cy, hexSizePx, t, selecionado, textMeasurer)
                    }
                }
            }
        }
    }
}

// ── Geometria: hex ↔ tela (pointy-top, coordenadas axiais) ─────────────────

/** Tamanho (raio de centro à ponta) em pixels que cabe [raioGrade] hexes visíveis na área [larg]x[alt]. */
internal fun tamanhoHex(larg: Float, alt: Float, raio: Int): Float {
    // Largura de um hex pointy-top = sqrt(3) * tamanho; altura = 2 * tamanho.
    // A grade de raio r tem largura ~ sqrt(3)*(2r+1)*tamanho e altura ~ 1.5*(2r)+2 * tamanho.
    val diametroCells = (2 * raio + 1).toFloat()
    val limPelaLargura = larg / (sqrt(3.0f) * diametroCells)
    val limPelaAltura = alt / (1.5f * (2 * raio) + 2f)
    return minOf(limPelaLargura, limPelaAltura) * 0.95f
}

/** Centro em pixels do hexágono [c] considerando origem no centro da tela. */
internal fun hexParaTela(c: HexCoord, larg: Float, alt: Float, tam: Float): Pair<Float, Float> {
    val sqrt3 = sqrt(3.0f)
    val x = tam * (sqrt3 * c.q + sqrt3 / 2f * c.r) + larg / 2f
    val y = tam * (3f / 2f * c.r) + alt / 2f
    return x to y
}

/** Converte um toque em pixels para o `HexCoord` mais próximo (round-trip com arredondamento cube). */
internal fun telaParaHex(toque: Offset, larg: Float, alt: Float, raio: Int): HexCoord? {
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
internal val ESTILO_LABEL_HEX = TextStyle(color = Color(0x77FFFFFF), fontSize = 9.sp)
private val ESTILO_INICIAL_TOKEN = TextStyle(color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center)

/** Path de hex pointy-top centrado em ([cx],[cy]) com raio [tam]. */
private fun pathDoHex(cx: Float, cy: Float, tam: Float): Path {
    val path = Path()
    for (i in 0..5) {
        val ang = PI / 6 + i * PI / 3 // 30°, 90°, 150°...
        val vx = (cx + tam * cos(ang)).toFloat()
        val vy = (cy + tam * sin(ang)).toFloat()
        if (i == 0) path.moveTo(vx, vy) else path.lineTo(vx, vy)
    }
    path.close()
    return path
}

internal fun DrawScope.desenharHex(
    cx: Float, cy: Float, tam: Float, destacado: Boolean,
    textMeasurer: TextMeasurer, hex: HexCoord
) {
    val path = pathDoHex(cx, cy, tam)
    if (destacado) drawPath(path, color = COR_HEX_SELECIONADO)
    drawPath(path, color = COR_GRADE_LINHA, style = Stroke(width = 1.5f))
    // Rótulo (q,r) minúsculo no topo do hex — útil pra depuração; sai no polimento do TOK-4.
    val label = textMeasurer.measure(text = androidx.compose.ui.text.AnnotatedString("${hex.q},${hex.r}"),
        style = ESTILO_LABEL_HEX)
    drawText(label, topLeft = Offset(cx - label.size.width / 2f, cy - tam / 2f))
}

/** Hex PREENCHIDO com [cor] (usado para destacar os hexes válidos de movimento). */
internal fun DrawScope.desenharHexPreenchido(cx: Float, cy: Float, tam: Float, cor: Color) {
    drawPath(pathDoHex(cx, cy, tam), color = cor)
}

/** Fallback clássico: círculo colorido + inicial + linha de facing (inimigos sem imagem e herói sem retrato). */
internal fun DrawScope.desenharToken(
    cx: Float, cy: Float, tam: Float, t: TokenDemo, selecionado: Boolean,
    textMeasurer: TextMeasurer
) {
    val cor = if (t.ehHeroi) COR_TOKEN_HEROI else COR_TOKEN_INIMIGO
    val raio = tam * 0.6f
    drawCircle(color = cor, radius = raio, center = Offset(cx, cy))
    if (selecionado) drawCircle(color = Color.White, radius = raio + 4f, center = Offset(cx, cy),
        style = Stroke(width = 3f))
    // Indicador de FACING: pequena "flecha" na direção que o token olha.
    val angFacing = anguloDaDirecao(t.facing)
    val fx = (cx + raio * 0.9f * cos(angFacing)).toFloat()
    val fy = (cy + raio * 0.9f * sin(angFacing)).toFloat()
    drawLine(color = COR_FACING, start = Offset(cx, cy), end = Offset(fx, fy), strokeWidth = 3f)
    // Inicial do nome no meio do token.
    val inicial = t.nome.firstOrNull()?.uppercase() ?: "?"
    val label = textMeasurer.measure(text = androidx.compose.ui.text.AnnotatedString(inicial),
        style = ESTILO_INICIAL_TOKEN)
    drawText(label, topLeft = Offset(cx - label.size.width / 2f, cy - label.size.height / 2f))
}

/**
 * Lote TOK-1: token de IMAGEM estilo VTT — retrato circular (clip) + borda colorida + seta de
 * facing como TRIÂNGULO na borda externa (estilo Roll20/Foundry).
 */
internal fun DrawScope.desenharTokenImagem(
    cx: Float, cy: Float, tam: Float, t: TokenDemo, selecionado: Boolean,
    imagem: ImageBitmap
) {
    val raio = tam * 0.62f
    val corBorda = if (t.ehHeroi) COR_TOKEN_HEROI else COR_TOKEN_INIMIGO
    // Imagem recortada em círculo.
    val clip = Path().apply { addOval(Rect(center = Offset(cx, cy), radius = raio)) }
    clipPath(clip) {
        drawImage(
            image = imagem,
            dstOffset = IntOffset((cx - raio).roundToInt(), (cy - raio).roundToInt()),
            dstSize = IntSize((raio * 2).roundToInt(), (raio * 2).roundToInt())
        )
    }
    // Borda colorida por cima (herói azul / inimigo vermelho).
    drawCircle(color = corBorda, radius = raio, center = Offset(cx, cy),
        style = Stroke(width = (tam * 0.09f).coerceAtLeast(2f)))
    if (selecionado) drawCircle(color = Color.White, radius = raio + 4f, center = Offset(cx, cy),
        style = Stroke(width = 3f))
    // Facing: triângulo pequeno na borda externa apontando na direção.
    val ang = anguloDaDirecao(t.facing)
    val pontaR = raio + tam * 0.22f
    val baseR = raio + tam * 0.04f
    val abertura = 0.28 // rad — meia-largura angular da base
    val ponta = Offset((cx + pontaR * cos(ang)).toFloat(), (cy + pontaR * sin(ang)).toFloat())
    val base1 = Offset((cx + baseR * cos(ang - abertura)).toFloat(), (cy + baseR * sin(ang - abertura)).toFloat())
    val base2 = Offset((cx + baseR * cos(ang + abertura)).toFloat(), (cy + baseR * sin(ang + abertura)).toFloat())
    val seta = Path().apply {
        moveTo(ponta.x, ponta.y); lineTo(base1.x, base1.y); lineTo(base2.x, base2.y); close()
    }
    drawPath(seta, color = corBorda)
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
