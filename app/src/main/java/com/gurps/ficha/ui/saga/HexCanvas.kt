package com.gurps.ficha.ui.saga

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.platform.LocalDensity
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
import com.gurps.ficha.data.storage.CenarioImageStore
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
private val COR_HEX_ZONA_2D = Color(0x66C2410C)     // laranja-queimado translúcido — MEC-46 (P1b): zona que FERE
/** Lote TOK-ZOOM: limites da pinça de dois dedos. 1f = enquadramento automático da câmera. */
internal const val ZOOM_MIN = 0.6f
internal const val ZOOM_MAX = 4f

private val COR_TOKEN_HEROI = Color(0xFF3B82F6)
private val COR_TOKEN_INIMIGO = Color(0xFFEF4444)
private val COR_FACING = Color(0xCCFFFFFF)
private val COR_SCRIM_FUNDO = Color(0x66101820)  // escurece o fundo gerado p/ legibilidade da grade
internal const val SQRT3 = 1.7320508f

/**
 * Wrapper do modo tático: carrega o token de imagem do herói a partir do retrato da ficha
 * (assíncrono, com cache em disco) e injeta no canvas. É o entry-point usado pelo TabSaga
 * (combate real e preview standalone).
 */
@Composable
fun HexCanvasTatico(viewModel: FichaViewModel, modifier: Modifier = Modifier) {
    // Lote TOK-4: em combate REAL com grade montada, o canvas é dirigido pelo SagaCombatController;
    // fora de combate (preview standalone) continua o demo.
    // Lote TESTE-1c: o ESTADO TÁTICO (observável) é lido PRIMEIRO de propósito. Se viesse depois de
    // `sagaCombateAtivo` num `&&`, o curto-circuito impediria a leitura enquanto não houvesse combate
    // — e sem leitura não há inscrição, então a grade não redesenharia quando o combate começasse.
    // Cinto e suspensório: `sessao` agora é observável, mas a ordem aqui protege de uma regressão.
    val estadoTaticoAtual = viewModel.sagaEstadoTatico
    if (estadoTaticoAtual != null && viewModel.sagaCombateAtivo) {
        HexCanvasCombateReal(viewModel, modifier)
    } else {
        HexCanvasDemoWrapper(viewModel, modifier)
    }
}

@Composable
private fun HexCanvasDemoWrapper(viewModel: FichaViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val retratoUri = viewModel.personagem.imagemPersonagemOriginalUri
    var tokenHeroi by remember(retratoUri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(retratoUri) {
        tokenHeroi = TokenImageStore.obterTokenHeroi(context, retratoUri)?.asImageBitmap()
    }

    // Lote TOK-3: fundo da CENA ativa (vista top-down gerada). Cache-first; se o gatilho do
    // delegate ainda está gerando, o Mutex por chave faz esta chamada ESPERAR e entregar o bitmap
    // quando pronto (recompose automática). Sem campanha/cena (preview standalone) → fundo cinza.
    val camp = viewModel.sagaCampanhaAtiva
    val cena = viewModel.sagaCenaAtiva
    // Keys ESPELHAM a identidade do cache (campanha+cena+titulo+bioma — humor fica fora da chave,
    // ver CenarioImageStore.chaveCena): qualquer mudança que regenera o fundo também recarrega aqui.
    var fundoCena by remember(camp?.id, cena?.id, cena?.titulo, cena?.bioma) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(camp?.id, cena?.id, cena?.titulo, cena?.bioma) {
        fundoCena = null
        if (camp == null || cena == null) return@LaunchedEffect
        if (!CenarioImageStore.cenaValidaParaFundo(cena.titulo)) return@LaunchedEffect
        var bmp = CenarioImageStore.fundoCenaCacheado(context, camp.id, cena.id, cena.titulo, cena.bioma)
        if (bmp == null) {
            val imgKey = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_IMAGE_KEY
            val imgModel = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_IMAGE_MODEL
            if (imgKey.isNotBlank()) {
                bmp = CenarioImageStore.obterFundoCena(
                    context, camp.id, cena.id, cena.titulo, cena.bioma, cena.humor
                ) { prompt ->
                    com.gurps.ficha.data.network.GeminiImageService
                        .gerarImagem(imgKey, imgModel, prompt, rotuloLog = "fundo:${cena.titulo}")?.bytes
                }
            }
        }
        fundoCena = bmp?.asImageBitmap()
    }

    HexCanvasDemo(modifier = modifier, tokenHeroi = tokenHeroi, fundoCena = fundoCena)
}

@Composable
fun HexCanvasDemo(
    modifier: Modifier = Modifier,
    tokenHeroi: ImageBitmap? = null,
    fundoCena: ImageBitmap? = null,
) {
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
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val largPx = constraints.maxWidth.toFloat()
            val altPx = constraints.maxHeight.toFloat()
            // Lote TOK-6a: câmera enquadra os tokens + hexes válidos (mesma UX do combate real).
            // Lote TOK-6b-1: piso de toque de 40dp + pan por arrasto (igual ao combate real).
            val pisoToquePx = with(LocalDensity.current) { 40.dp.toPx() }
            val camAlvo = calcularCamera(
                estado.tokens.map { it.posicao } + estado.hexesValidosParaMover,
                largPx, altPx, estado.raioGrade, pisoToquePx = pisoToquePx
            )
            val camTam by animateFloatAsState(camAlvo.tam, tween(400), label = "camTam")
            val camAx by animateFloatAsState(camAlvo.centroAx, tween(400), label = "camAx")
            val camAy by animateFloatAsState(camAlvo.centroAy, tween(400), label = "camAy")
            var panPx by remember { mutableStateOf(Offset.Zero) }
            LaunchedEffect(camAlvo) { panPx = Offset.Zero }
            // Lote TOK-ZOOM: o zoom do usuário NÃO é resetado pelo reenquadramento — quem aproximou
            // pra "achar o espaço" continua aproximado no turno seguinte.
            var zoomUsuario by remember { mutableFloatStateOf(1f) }
            val cam = cameraEfetiva(camTam, camAx, camAy, panPx.x, panPx.y, estado.raioGrade, zoomUsuario)
            val camAtual by rememberUpdatedState(cam)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "Grade tática de hexágonos" }
                    .pointerInput(estado.raioGrade) {
                        detectTapGestures { toque ->
                            val hex = telaParaHexCam(toque.x, toque.y, camAtual,
                                size.width.toFloat(), size.height.toFloat(), estado.raioGrade)
                            if (hex != null) estado = estado.aoTocarHex(hex)
                        }
                    }
                    .pointerInput(Unit) {
                        // Lote TOK-ZOOM: pinça de DOIS DEDOS dá zoom; um dedo arrasta (pan). Antes só
                        // havia arrasto, e o jogador "perdia a noção de espaço" quando a câmera
                        // enquadrava tudo de longe.
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            panPx += pan
                            if (gestureZoom != 1f) {
                                zoomUsuario = (zoomUsuario * gestureZoom).coerceIn(ZOOM_MIN, ZOOM_MAX)
                            }
                        }
                    }
            ) {
                val larguraPx = size.width
                val alturaPx = size.height
                val hexSizePx = cam.tam

                // Lote TOK-3: fundo da cena (vista top-down gerada) em escala COVER + scrim escuro
                // por cima pra grade e os tokens continuarem legíveis. Sem fundo → cinza da Column.
                if (fundoCena != null) {
                    val escala = kotlin.math.max(
                        larguraPx / fundoCena.width.toFloat(),
                        alturaPx / fundoCena.height.toFloat()
                    )
                    val dw = (fundoCena.width * escala).roundToInt()
                    val dh = (fundoCena.height * escala).roundToInt()
                    drawImage(
                        image = fundoCena,
                        dstOffset = IntOffset(
                            ((larguraPx - dw) / 2f).roundToInt(),
                            ((alturaPx - dh) / 2f).roundToInt()
                        ),
                        dstSize = IntSize(dw, dh)
                    )
                    drawRect(color = COR_SCRIM_FUNDO)
                }

                // Grade (clip natural da tela esconde o fora-de-câmera).
                for (hex in estado.hexesVisiveis) {
                    val (cx, cy) = hexParaTelaCam(hex, cam, larguraPx, alturaPx)
                    if (cx < -hexSizePx || cx > larguraPx + hexSizePx ||
                        cy < -hexSizePx || cy > alturaPx + hexSizePx) continue
                    val destacado = hex == estado.hexSelecionado
                    desenharHex(cx, cy, hexSizePx, destacado, textMeasurer, hex)
                }
                // Hexes VÁLIDOS para mover — preenchimento verde translúcido.
                for (hexValido in estado.hexesValidosParaMover) {
                    val (cx, cy) = hexParaTelaCam(hexValido, cam, larguraPx, alturaPx)
                    desenharHexPreenchido(cx, cy, hexSizePx, COR_HEX_VALIDO_2D)
                }
                // Tokens por cima da grade — posição ANIMADA. Herói usa o retrato; inimigo usa a
                // imagem gerada (TOK-2) quando o cache/geração já entregou; fallback círculo+inicial.
                for (t in estado.tokens) {
                    val (ax, ay) = posAnimadas[t.id] ?: continue
                    val cx = cam.tam * (ax - cam.centroAx) + larguraPx / 2f
                    val cy = cam.tam * (ay - cam.centroAy) + alturaPx / 2f
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

/**
 * Lote TOK-4: canvas do combate REAL — tokens/posições vêm do [FichaViewModel] (SagaCombatController),
 * o toque vai pro controller (seleção + manobra MOVER tática), hexes alcançáveis do herói em verde,
 * barra de HP e nome sob cada token. Reusa TODO o desenho do demo (TokenTatico → TokenDemo).
 */
@Composable
private fun HexCanvasCombateReal(viewModel: FichaViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val estado = viewModel.sagaEstadoTatico ?: return
    // Dependência EXPLÍCITA do CombatUiState observável: PV/condições mudam SEM o estadoTatico ser
    // reatribuído (dano sem movimento → sincronizarGridComEncounter devolve a MESMA instância).
    // tokensTaticos lê o encounter mutável não-observável — sem esta leitura, a barra de HP ficaria
    // stale até o próximo movimento. Ler sagaCombateEstado (reatribuído a cada ação) recompõe aqui.
    @Suppress("UNUSED_VARIABLE") val dependenciaEstadoCombate = viewModel.sagaCombateEstado
    val tokens = viewModel.sagaTokensTaticos
    val textMeasurer = rememberTextMeasurer()

    // Token do herói (TOK-1).
    val retratoUri = viewModel.personagem.imagemPersonagemOriginalUri
    var tokenHeroi by remember(retratoUri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(retratoUri) {
        tokenHeroi = TokenImageStore.obterTokenHeroi(context, retratoUri)?.asImageBitmap()
    }

    // Tokens dos inimigos (TOK-2) — TIPO derivado do id do combatente ("goblin_2" → "goblin"),
    // a MESMA chave que o gatilho do iniciar_combate usou (consistência garantida).
    var tokensInimigos by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }
    val tiposInimigos = tokens.filter { !it.ehHeroi }.map { tipoDoId(it.id) }.distinct()
    LaunchedEffect(tiposInimigos) {
        for (tipo in tiposInimigos) {
            val chave = TokenImageStore.normalizarTipo(tipo)
            if (chave.isBlank() || tokensInimigos.containsKey(chave)) continue
            // Cache-first; se o gatilho ainda está gerando, obterTokenInimigo espera no Mutex.
            var bmp = TokenImageStore.tokenInimigoCacheado(context, tipo)
            if (bmp == null) {
                val imgKey = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_IMAGE_KEY
                val imgModel = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_IMAGE_MODEL
                if (imgKey.isNotBlank()) {
                    bmp = TokenImageStore.obterTokenInimigo(context, tipo = tipo) { prompt ->
                        com.gurps.ficha.data.network.GeminiImageService
                            .gerarImagem(imgKey, imgModel, prompt, rotuloLog = "token:$chave")?.bytes
                    }
                }
            }
            if (bmp != null) tokensInimigos = tokensInimigos + (chave to bmp.asImageBitmap())
        }
    }

    // Fundo da cena (TOK-3). Lote TOK-6a — FIX do teste no aparelho: era cache-only; se a geração
    // (gatilho pós-turno) ainda estava rodando quando a grade abriu, ficava cinza PRA SEMPRE (as
    // keys não mudam quando o arquivo aparece). Agora usa o mesmo caminho do demo: cache-first e,
    // no miss, obterFundoCena — que ESPERA no Mutex a geração em curso e recompõe quando chega.
    val camp = viewModel.sagaCampanhaAtiva
    val cena = viewModel.sagaCenaAtiva
    var fundoCena by remember(camp?.id, cena?.id, cena?.titulo, cena?.bioma) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(camp?.id, cena?.id, cena?.titulo, cena?.bioma) {
        fundoCena = null
        if (camp == null || cena == null) return@LaunchedEffect
        if (!CenarioImageStore.cenaValidaParaFundo(cena.titulo)) return@LaunchedEffect
        var bmp = CenarioImageStore.fundoCenaCacheado(context, camp.id, cena.id, cena.titulo, cena.bioma)
        if (bmp == null) {
            val imgKey = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_IMAGE_KEY
            val imgModel = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_IMAGE_MODEL
            if (imgKey.isNotBlank()) {
                bmp = CenarioImageStore.obterFundoCena(
                    context, camp.id, cena.id, cena.titulo, cena.bioma, cena.humor
                ) { prompt ->
                    com.gurps.ficha.data.network.GeminiImageService
                        .gerarImagem(imgKey, imgModel, prompt, rotuloLog = "fundo:${cena.titulo}")?.bytes
                }
            }
        }
        fundoCena = bmp?.asImageBitmap()
    }

    // Aviso do controller (auto-hide 2s).
    val aviso = viewModel.sagaAvisoTatico
    LaunchedEffect(aviso) {
        if (aviso != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.sagaAvisoTatico = null
        }
    }

    // Posições animadas (mesmo padrão do demo — axial-neutras, escala aplicada no draw).
    val posAnimadas = mutableMapOf<String, Pair<Float, Float>>()
    for (t in tokens) {
        key(t.id) {
            val axAlvo = SQRT3 * t.posicao.q + SQRT3 / 2f * t.posicao.r
            val ayAlvo = 1.5f * t.posicao.r
            val ax by animateFloatAsState(axAlvo, tween(250), label = "ax")
            val ay by animateFloatAsState(ayAlvo, tween(250), label = "ay")
            posAnimadas[t.id] = ax to ay
        }
    }

    val hexesAlcancaveis = viewModel.sagaHexesAlcancaveis()
    val hexesDeZona = viewModel.sagaHexesDeZona // MEC-46 (P1b)

    Column(modifier = modifier.background(COR_GRADE_FUNDO)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⬢ Combate tático", color = Color.White, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Text("Toque no seu token → hex VERDE = Mover", color = Color(0xCCFFFFFF),
                    style = MaterialTheme.typography.labelSmall)
            }
            if (aviso != null) {
                Text("⚠ $aviso", color = Color(0xFFEF4444),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 2.dp))
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val largPx = constraints.maxWidth.toFloat()
            val altPx = constraints.maxHeight.toFloat()
            // Lote TOK-6a — CÂMERA: enquadra os combatentes (+margem) em vez da grade inteira.
            // Lote TOK-6b-1: PISO DE TOQUE — o hex não encolhe abaixo do tocável; o resto vai pro PAN.
            // Lote TOK-6b-3 (feedback do teste): quando o herói está SELECIONADO (modo movimento, há
            // hexes verdes), usamos um piso MENOR — cabe mais do deslocamento na tela; ao só observar,
            // o piso volta ao confortável.
            val movendo = hexesAlcancaveis.isNotEmpty()
            val pisoToquePx = with(LocalDensity.current) { (if (movendo) 30.dp else 40.dp).toPx() }
            val camAlvo = calcularCamera(
                tokens.map { it.posicao } + hexesAlcancaveis, largPx, altPx, estado.raioGrade,
                pisoToquePx = pisoToquePx
            )
            val camTam by animateFloatAsState(camAlvo.tam, tween(400), label = "camTam")
            val camAx by animateFloatAsState(camAlvo.centroAx, tween(400), label = "camAx")
            val camAy by animateFloatAsState(camAlvo.centroAy, tween(400), label = "camAy")
            // PAN por arrasto: acumulado em px, convertido pra unidades axiais na câmera efetiva.
            // Reset quando o ENQUADRAMENTO-ALVO muda de verdade (mover, seleção, morte).
            // O CENTRO é clampado ao raio da grade — o usuário nunca "se perde" em tela vazia.
            var panPx by remember { mutableStateOf(Offset.Zero) }
            LaunchedEffect(camAlvo) { panPx = Offset.Zero }
            // Lote TOK-ZOOM: pinça de DOIS DEDOS. Multiplica o tamanho enquadrado pela câmera e
            // NÃO é resetada no reenquadramento de cada turno.
            var zoomUsuario by remember { mutableFloatStateOf(1f) }
            val cam = cameraEfetiva(camTam, camAx, camAy, panPx.x, panPx.y, estado.raioGrade, zoomUsuario)
            val camAtual by rememberUpdatedState(cam)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "Grade tática do combate" }
                    .pointerInput(estado.raioGrade) {
                        detectTapGestures { toque ->
                            val hex = telaParaHexCam(toque.x, toque.y, camAtual,
                                size.width.toFloat(), size.height.toFloat(), estado.raioGrade)
                            if (hex != null) viewModel.sagaAoTocarHexTatico(hex)
                        }
                    }
                    .pointerInput(Unit) {
                        // Lote TOK-ZOOM: pinça de DOIS DEDOS dá zoom; um dedo arrasta (pan). Antes só
                        // havia arrasto, e o jogador "perdia a noção de espaço" quando a câmera
                        // enquadrava tudo de longe.
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            panPx += pan
                            if (gestureZoom != 1f) {
                                zoomUsuario = (zoomUsuario * gestureZoom).coerceIn(ZOOM_MIN, ZOOM_MAX)
                            }
                        }
                    }
            ) {
                val larguraPx = size.width
                val alturaPx = size.height
                val hexSizePx = cam.tam

                // Fundo da cena (TOK-3) + scrim.
                if (fundoCena != null) {
                    val escala = kotlin.math.max(
                        larguraPx / fundoCena!!.width.toFloat(),
                        alturaPx / fundoCena!!.height.toFloat()
                    )
                    val dw = (fundoCena!!.width * escala).roundToInt()
                    val dh = (fundoCena!!.height * escala).roundToInt()
                    drawImage(fundoCena!!, dstOffset = IntOffset(
                        ((larguraPx - dw) / 2f).roundToInt(), ((alturaPx - dh) / 2f).roundToInt()
                    ), dstSize = IntSize(dw, dh))
                    drawRect(color = COR_SCRIM_FUNDO)
                }
                // Grade (o clip da tela esconde o que a câmera deixou de fora).
                for (hex in estado.hexesVisiveis) {
                    val (cx, cy) = hexParaTelaCam(hex, cam, larguraPx, alturaPx)
                    if (cx < -hexSizePx || cx > larguraPx + hexSizePx ||
                        cy < -hexSizePx || cy > alturaPx + hexSizePx) continue
                    desenharHex(cx, cy, hexSizePx, hex == estado.hexSelecionado, textMeasurer, hex)
                }
                // Lote MEC-46 (P1b): ZONA persistente (chuva/nuvem/gás) pintada ANTES dos hexes de
                // movimento e dos tokens — o jogador precisa VER a área que fere.
                for (hexZona in hexesDeZona) {
                    val (cx, cy) = hexParaTelaCam(hexZona, cam, larguraPx, alturaPx)
                    desenharHexPreenchido(cx, cy, hexSizePx, COR_HEX_ZONA_2D)
                }
                // Hexes alcançáveis (Mover real — deslocamento do herói).
                for (hexValido in hexesAlcancaveis) {
                    val (cx, cy) = hexParaTelaCam(hexValido, cam, larguraPx, alturaPx)
                    desenharHexPreenchido(cx, cy, hexSizePx, COR_HEX_VALIDO_2D)
                }
                // Tokens + barra de HP + nome (posições animadas em unidades axiais → câmera).
                for (t in tokens) {
                    val (ax, ay) = posAnimadas[t.id] ?: continue
                    val cx = cam.tam * (ax - cam.centroAx) + larguraPx / 2f
                    val cy = cam.tam * (ay - cam.centroAy) + alturaPx / 2f
                    val selecionado = t.id == estado.idSelecionado
                    val demoToken = TokenDemo(t.id, t.nome, t.posicao, t.ehHeroi, t.facing)
                    val imagem = if (t.ehHeroi) tokenHeroi
                        else tokensInimigos[TokenImageStore.normalizarTipo(tipoDoId(t.id))]
                    if (imagem != null) desenharTokenImagem(cx, cy, hexSizePx, demoToken, selecionado, imagem)
                    else desenharToken(cx, cy, hexSizePx, demoToken, selecionado, textMeasurer)
                    desenharBarraHpENome(cx, cy, hexSizePx, t.nome, t.pvPct, t.condicoesIcones,
                        textMeasurer, t.pfPct, t.pvAtual, t.pfAtual)
                }
            }
        }
    }
}

/** "goblin_2" → "goblin": o TIPO do bestiário é o id sem o sufixo _N (chave das imagens TOK-2). */
internal fun tipoDoId(id: String): String = id.replace(Regex("_\\d+$"), "")

/**
 * Lote TOK-6b-1: BARRA DE HP sobre a cabeça do token (substitui o anel e os cards de vida do
 * tracker) + mini-ícones de condição acima dela + nome sob o token. A vida mora no grid.
 */
internal fun DrawScope.desenharBarraHpENome(
    cx: Float, cy: Float, tam: Float, nome: String, pvPct: Float,
    condicoesIcones: String, textMeasurer: TextMeasurer,
    /**
     * Lote TOK-PF: fração de FADIGA. `null` = NPC (o bestiário não rastreia PF), e então o
     * hexágono azul não é desenhado — o que também distingue herói de inimigo (ver TOK-7).
     */
    pfPct: Float? = null,
    /** Lote TOK-7: valores absolutos escritos dentro dos hexágonos. */
    pvAtual: Int = 0,
    pfAtual: Int? = null,
) {
    // ── NOME sobre o topo do retrato ──────────────────────────────────────────────────────────
    // Lote TOK-8: a fonte agora ESCALA com o token. Antes era `sp` fixo, então num token grande
    // (zoom perto) o texto ficava minúsculo — foi exatamente o que o usuário reportou: "de longe é
    // legível, o problema é perto".
    val estiloNome = ESTILO_NOME_TOKEN.copy(fontSize = (tam * 0.30f).toSp())
    val dyNome = -tam * 0.66f
    // A largura disponível é a do hexágono NAQUELA ALTURA, não a máxima (ver meiaLarguraDoHex).
    val larguraUtil = 2f * meiaLarguraDoHex(tam, dyNome) * 0.92f
    var texto = nome
    var label = textMeasurer.measure(androidx.compose.ui.text.AnnotatedString(texto), estiloNome)
    while (label.size.width > larguraUtil && texto.length > 2) {
        texto = texto.dropLast(1)
        label = textMeasurer.measure(androidx.compose.ui.text.AnnotatedString(texto), estiloNome)
    }
    drawText(label, topLeft = Offset(cx - label.size.width / 2f, cy + dyNome))

    // ── Hexágonos de recurso ──────────────────────────────────────────────────────────────────
    // Herói: PV (vermelho) à esquerda e PF (azul) à direita. NPC: só PV — ele não tem fadiga, e a
    // assimetria 2-contra-1 é o que substitui o anel azul como marcador de lado.
    //
    // Lote TOK-8: encolhidos e puxados para DENTRO. O hexágono é PONTUDO embaixo, então perto do
    // vértice inferior quase não há largura — na posição antiga (0.60·tam de centro, 0.42·tam de
    // tamanho) eles vazavam para fora, como o usuário viu.
    val tamMini = tam * 0.26f
    val dyMini = tam * 0.38f
    desenharHexRecurso(cx - tam * 0.47f, cy + dyMini, tamMini, pvPct, pvAtual, COR_PV_HEX, textMeasurer)
    if (pfPct != null) {
        desenharHexRecurso(cx + tam * 0.47f, cy + dyMini, tamMini, pfPct, pfAtual ?: 0, COR_PF_HEX, textMeasurer)
    }

    // ── Condições entre os dois hexágonos ─────────────────────────────────────────────────────
    // Sem .take(): cortaria um emoji composto (ZWJ/surrogate) no meio.
    if (condicoesIcones.isNotBlank()) {
        val icones = textMeasurer.measure(
            androidx.compose.ui.text.AnnotatedString(condicoesIcones),
            ESTILO_NOME_TOKEN.copy(fontSize = (tam * 0.24f).toSp()))
        drawText(icones, topLeft = Offset(cx - icones.size.width / 2f, cy + tam * 0.30f))
    }
}

/**
 * Lote TOK-7: hexágono de recurso — o preenchimento ESVAZIA de cima para baixo conforme [pct] cai,
 * e o valor absoluto vai escrito dentro.
 *
 * O corpo fica a 50% de opacidade (pedido do usuário) para não competir com o retrato. Abaixo de
 * [TAM_MIN_NUMERO_TOKEN] o número não é desenhado: num token de ~30dp (a câmera abre assim ao
 * mostrar o deslocamento inteiro — ver TOK-6b-3) ele viraria borrão. A COR continua contando a
 * história, que é o que se enxerga de longe.
 */
internal fun DrawScope.desenharHexRecurso(
    cx: Float, cy: Float, tam: Float, pct: Float, valor: Int, cor: Color, textMeasurer: TextMeasurer
) {
    val hex = pathDoHex(cx, cy, tam)
    drawPath(hex, color = cor.copy(alpha = 0.18f))            // leito vazio
    val altura = alturaPreenchidaDoHex(tam, pct)
    if (altura > 0f) {
        // Recorta o hex e pinta só a faixa de baixo proporcional ao recurso restante.
        clipPath(hex) {
            drawRect(
                color = cor.copy(alpha = 0.5f),
                topLeft = Offset(cx - tam, cy + tam - altura),
                size = androidx.compose.ui.geometry.Size(tam * 2f, altura)
            )
        }
    }
    drawPath(hex, color = cor.copy(alpha = 0.9f), style = Stroke(width = 1.5f))
    if (mostraNumeroDeRecurso(tam)) {
        // Lote TOK-8: fonte proporcional ao mini-hex (era `sp` fixo e sumia num token grande).
        val n = textMeasurer.measure(
            androidx.compose.ui.text.AnnotatedString(valor.toString()),
            ESTILO_VALOR_RECURSO.copy(fontSize = (tam * 0.95f).toSp()))
        drawText(n, topLeft = Offset(cx - n.size.width / 2f, cy - n.size.height / 2f))
    }
}

internal val ESTILO_NOME_TOKEN = TextStyle(color = Color(0xEEFFFFFF), fontSize = 10.sp)

/** Lote TOK-7: número dentro do hexágono de recurso. */
internal val ESTILO_VALOR_RECURSO = TextStyle(color = Color(0xEEFFFFFF), fontSize = 11.sp)

/**
 * Lote TOK-7: abaixo deste tamanho de mini-hex o número não é desenhado (viraria borrão) e só a
 * cor conta a história. Decisão do usuário: "sumir número, manter a cor".
 */
internal const val TAM_MIN_NUMERO_TOKEN = 13f

/** Lote TOK-7: o número cabe neste mini-hex? Puro, para ter teste (o desenho em si só no aparelho). */
internal fun mostraNumeroDeRecurso(tamMini: Float): Boolean = tamMini >= TAM_MIN_NUMERO_TOKEN

/**
 * Lote TOK-8: MEIA-LARGURA do hexágono pointy-top na altura [dy] (deslocamento vertical do centro).
 *
 * Existe porque o TOK-7 clampou o nome pela largura MÁXIMA (`√3·tam`), que só vale na faixa central.
 * O nome era desenhado a `0.92·tam` acima do centro, onde o hexágono tem só `0.14·tam` de meia-
 * largura — e por isso vazava para o hex de cima, como o usuário viu no aparelho.
 *
 * Geometria (ver `pathDoHex`, vértices a 30°/90°/...): a meia-largura é constante `√3/2·tam` até
 * `|dy| = tam/2` e daí decresce linearmente até 0 no vértice (`|dy| = tam`).
 */
internal fun meiaLarguraDoHex(tam: Float, dy: Float): Float {
    val ay = kotlin.math.abs(dy)
    if (ay >= tam) return 0f
    val plena = tam * (SQRT3 / 2f)
    return if (ay <= tam / 2f) plena else plena * (1f - 2f * (ay / tam - 0.5f))
}

/**
 * Lote TOK-7: altura da faixa preenchida do hexágono de recurso, de baixo para cima.
 * Cheio = a altura toda (2·tam); vazio = 0. `pct` fora de 0..1 é clampado — PV negativo existe em
 * GURPS (o herói vai a PV negativo antes de morrer) e não pode virar retângulo invertido.
 */
internal fun alturaPreenchidaDoHex(tam: Float, pct: Float): Float = 2f * tam * pct.coerceIn(0f, 1f)

private val COR_PV_HEX = Color(0xFFDC2626)   // vermelho — PV
private val COR_PF_HEX = Color(0xFF2563EB)   // azul — PF

// ── Câmera (Lote TOK-6a): enquadra os COMBATENTES, não a grade inteira ─────

/**
 * A grade raio 7 inteira espremida na tela deixava cada hex com ~20px (o mínimo de toque do
 * Android é 48dp) — no aparelho físico era intocável e as imagens dos tokens invisíveis.
 * A câmera enquadra o BOUNDING BOX dos combatentes + margem, com piso (nunca menor que a visão
 * full-grid) e teto (nunca hexes gigantes demais). Valores em "unidades axiais" (1 unidade = tam).
 */
internal data class CameraHex(val tam: Float, val centroAx: Float, val centroAy: Float)

internal fun calcularCamera(
    posicoes: List<HexCoord>,
    larg: Float,
    alt: Float,
    raioGrade: Int,
    /**
     * Lote TOK-6b-1 (feedback do teste): PISO DE TOQUE — o hex nunca fica menor que isto em px
     * (~40dp convertidos pelo caller). Enquadrar TODOS os alcançáveis de deslocamento 5+ abria a
     * câmera demais e os hexes viravam alvo de mouse, não de dedo. Com o piso, o que não couber
     * fica fora da viewport e o PAN por arrasto alcança.
     */
    pisoToquePx: Float = 0f,
): CameraHex {
    val tamFull = tamanhoHex(larg, alt, raioGrade)
    if (posicoes.isEmpty() || larg <= 0f || alt <= 0f) return CameraHex(tamFull, 0f, 0f)
    val axs = posicoes.map { SQRT3 * it.q + SQRT3 / 2f * it.r }
    val ays = posicoes.map { 1.5f * it.r }
    // Lote TOK-6b-3 (feedback do teste): margem menor (~1 hex) — a folga de 2,5 hexes desperdiçava
    // borda e, com o piso de toque, empurrava o range de movimento pra fora da viewport (o usuário
    // não conseguia andar o deslocamento todo). Menos borda = mais hexes alcançáveis visíveis.
    val margemAx = SQRT3 * 1.1f
    val margemAy = 1.5f * 1.1f
    val minAx = axs.min() - margemAx; val maxAx = axs.max() + margemAx
    val minAy = ays.min() - margemAy; val maxAy = ays.max() + margemAy
    val wUnid = (maxAx - minAx).coerceAtLeast(0.1f)
    val hUnid = (maxAy - minAy).coerceAtLeast(0.1f)
    val tamMax = minOf(larg, alt) / 7f  // teto: ~7 unidades visíveis (combate colado não vira zoom infinito)
    val piso = minOf(tamFull, tamMax)
    val teto = maxOf(tamFull, tamMax)
    var tam = minOf(larg / wUnid, alt / hUnid).coerceIn(piso, teto)
    // Piso de toque vence o enquadramento (mas nunca acima do teto).
    if (pisoToquePx > 0f) tam = tam.coerceAtLeast(minOf(pisoToquePx, teto))
    return CameraHex(tam, (minAx + maxAx) / 2f, (minAy + maxAy) / 2f)
}

/**
 * Lote TOK-6b-1: câmera EFETIVA = câmera animada + PAN do usuário, com o centro CLAMPADO à
 * extensão axial da grade (ax ∈ ±√3·R, ay ∈ ±1.5·R) — arrastar nunca "perde" o mapa. PURA.
 */
internal fun cameraEfetiva(
    tam: Float, ax: Float, ay: Float, panX: Float, panY: Float, raioGrade: Int,
    zoom: Float = 1f
): CameraHex {
    // Lote TOK-ZOOM: [zoom] é a pinça de dois dedos, MULTIPLICADA sobre o tamanho que a câmera
    // enquadrou sozinha. Como o hex passa a medir `tam·zoom` px, o pan em px é convertido por
    // esse mesmo produto — aproximado, o dedo anda menos hexes (comportamento de mapa).
    val t = tam * zoom.coerceIn(ZOOM_MIN, ZOOM_MAX)
    if (t <= 0f) return CameraHex(t, ax, ay)
    val limiteAx = raioGrade * SQRT3
    val limiteAy = raioGrade * 1.5f
    return CameraHex(
        t,
        (ax - panX / t).coerceIn(-limiteAx, limiteAx),
        (ay - panY / t).coerceIn(-limiteAy, limiteAy)
    )
}

/** Centro em px do hex [c] sob a câmera [cam]. */
internal fun hexParaTelaCam(c: HexCoord, cam: CameraHex, larg: Float, alt: Float): Pair<Float, Float> {
    val ax = SQRT3 * c.q + SQRT3 / 2f * c.r
    val ay = 1.5f * c.r
    return (cam.tam * (ax - cam.centroAx) + larg / 2f) to (cam.tam * (ay - cam.centroAy) + alt / 2f)
}

/** Toque em px → hex sob a câmera [cam] (cube-round); null fora da grade. */
internal fun telaParaHexCam(x: Float, y: Float, cam: CameraHex, larg: Float, alt: Float, raioGrade: Int): HexCoord? {
    if (cam.tam <= 0f) return null
    val ax = (x - larg / 2f) / cam.tam + cam.centroAx
    val ay = (y - alt / 2f) / cam.tam + cam.centroAy
    val rFrac = ay / 1.5f
    val qFrac = (ax - SQRT3 / 2f * rFrac) / SQRT3
    val sFrac = -qFrac - rFrac
    var rq = Math.round(qFrac.toDouble()).toInt()
    var rr = Math.round(rFrac.toDouble()).toInt()
    var rs = Math.round(sFrac.toDouble()).toInt()
    val dq = kotlin.math.abs(rq - qFrac); val dr = kotlin.math.abs(rr - rFrac); val ds = kotlin.math.abs(rs - sFrac)
    if (dq > dr && dq > ds) rq = -rr - rs else if (dr > ds) rr = -rq - rs else rs = -rq - rr
    val hex = HexCoord(rq, rr)
    return if (HexCoord.ORIGEM.distancia(hex) <= raioGrade) hex else null
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

/** Lote TOK-6a: coordenadas (q,r) nos hexes poluíam a grade — agora só atrás desta flag de debug. */
private const val DEBUG_COORDENADAS_HEX = false

internal fun DrawScope.desenharHex(
    cx: Float, cy: Float, tam: Float, destacado: Boolean,
    textMeasurer: TextMeasurer, hex: HexCoord
) {
    val path = pathDoHex(cx, cy, tam)
    if (destacado) drawPath(path, color = COR_HEX_SELECIONADO)
    drawPath(path, color = COR_GRADE_LINHA, style = Stroke(width = 1.5f))
    if (DEBUG_COORDENADAS_HEX) {
        val label = textMeasurer.measure(text = androidx.compose.ui.text.AnnotatedString("${hex.q},${hex.r}"),
            style = ESTILO_LABEL_HEX)
        drawText(label, topLeft = Offset(cx - label.size.width / 2f, cy - tam / 2f))
    }
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
    // Lote TOK-7: o ANEL COLORIDO SAIU. Ele comia a borda do retrato e o usuário pediu mais espaço
    // para o rosto. Quem passou a marcar o lado é o par de hexágonos de recurso (herói tem PV+PF,
    // inimigo só PV) — ver `desenharBarraHpENome`. Sem o anel, o raio pode crescer.
    val raio = tam * 0.72f
    // A cor ainda serve ao TRIÂNGULO de facing, que o usuário quis manter.
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
