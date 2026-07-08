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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.google.android.filament.LightManager
import com.google.android.filament.View as FilamentView
import com.gurps.ficha.domain.combat.hex.HexRender3D
import com.gurps.ficha.domain.combat.hex.HexTaticoState
import io.github.sceneview.Scene
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.PlaneNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberRenderer
import io.github.sceneview.rememberView

/**
 * Lote HEX-7 (Fase 5 do PILAR): render 3D declarativo do combate tático em hexágonos usando SceneView
 * (wrap Compose de Filament — já presente no projeto desde o lote dos dados 3D).
 *
 * Lote HEX-8 (Fase 6 do PILAR): substituiu os cilindros placeholder por MODELOS .glb (CesiumMan como
 * herói, Duck como inimigo — ambos CC-BY 4.0 do Khronos glTF-Sample-Assets, ver `assets/models/LICENSES.txt`),
 * adicionou HALO de seleção como PlaneNode amarelo translúcido, e INTERPOLAÇÃO SUAVE (200 ms) de
 * position/yaw quando o estado muda. Overlay 2D com a grade continua no lugar por enquanto — alinhamento
 * ortográfico perfeito é o HEX-9.
 *
 * A projeção pura vive em [HexRender3D] (kotlin puro, testado); esta função só monta a árvore de nodes.
 */

private val COR_FUNDO_3D = Color(0xFF0E1B29)
private val COR_CHAO_3D = Color(0xFF2F3F52)
private val COR_HALO_SELECAO = Color(1.0f, 0.85f, 0.2f, 0.55f)   // amarelo translúcido — glow no chão
private val COR_HEROI_3D = Color(0xFF3B82F6)                      // azul — cilindro fallback enquanto o .glb do herói carrega
private val COR_INIMIGO_3D = Color(0xFFEF4444)                    // vermelho — cilindro fallback do inimigo
private val COR_ALIADO_3D = Color(0xFF10B981)                     // verde — cilindro para aliado sem modelo próprio

@Composable
fun HexScene3DDemo(modifier: Modifier = Modifier) {
    var estado by remember { mutableStateOf(HexTaticoState.demoInicial()) }
    val textMeasurer = rememberTextMeasurer()

    Column(modifier = modifier.background(COR_FUNDO_3D)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⬢ Modo tático 3D (demo)", color = Color.White, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            Text("Toque num token, depois num hex adjacente para mover", color = Color(0xCCFFFFFF),
                style = MaterialTheme.typography.labelSmall)
        }

        Box(Modifier.fillMaxWidth().weight(1f)) {
            HexScene3DBase(estado = estado, modifier = Modifier.fillMaxSize())
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "Grade tática 3D — overlay 2D de toque" }
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
                for (hex in estado.hexesVisiveis) {
                    val (cx, cy) = hexParaTela(hex, larguraPx, alturaPx, hexSizePx)
                    val destacado = hex == estado.hexSelecionado
                    desenharHex(cx, cy, hexSizePx, destacado, textMeasurer, hex)
                }
            }
        }
    }
}

/**
 * Cena 3D pura (chão + luz + modelos .glb dos tokens + halo de seleção). Não desenha grade — isso
 * fica no overlay 2D. Isolada para poder ser reusada por outra tela sem o overlay caso preciso.
 */
@Composable
private fun HexScene3DBase(estado: HexTaticoState, modifier: Modifier = Modifier) {
    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
    val modelLoader = rememberModelLoader(engine)
    val view = rememberView(engine).apply {
        blendMode = FilamentView.BlendMode.TRANSLUCENT
    }
    val renderer = rememberRenderer(engine).apply {
        clearOptions = clearOptions.apply {
            clear = true
            clearColor = floatArrayOf(0f, 0f, 0f, 0f)
        }
    }

    // Modelos .glb (podem retornar null enquanto carregam ou se o asset sumir).
    val instanciaHeroi = rememberModelInstance(modelLoader, "models/token_heroi.glb")
    val instanciaInimigo = rememberModelInstance(modelLoader, "models/token_inimigo.glb")

    // MaterialInstances memoizadas UMA VEZ pela vida da Scene — Filament não coleta lixo automático.
    val chaoMi = remember(materialLoader) { materialLoader.createColorInstance(
        color = COR_CHAO_3D.toFilamentColor(), metallic = 0.0f, roughness = 0.9f, reflectance = 0.0f) }
    val haloMi = remember(materialLoader) { materialLoader.createColorInstance(
        color = COR_HALO_SELECAO.toFilamentColor(), metallic = 0.0f, roughness = 0.9f, reflectance = 0.0f) }
    val heroiFallbackMi = remember(materialLoader) { materialLoader.createColorInstance(
        color = COR_HEROI_3D.toFilamentColor(), metallic = 0.1f, roughness = 0.5f, reflectance = 0.3f) }
    val inimigoFallbackMi = remember(materialLoader) { materialLoader.createColorInstance(
        color = COR_INIMIGO_3D.toFilamentColor(), metallic = 0.1f, roughness = 0.5f, reflectance = 0.3f) }
    val aliadoMi = remember(materialLoader) { materialLoader.createColorInstance(
        color = COR_ALIADO_3D.toFilamentColor(), metallic = 0.1f, roughness = 0.5f, reflectance = 0.3f) }

    val tokens = remember(estado) {
        HexRender3D.projetar(
            estado.toHexCombatState(),
            idHeroi = "heroi",
            idsInimigos = estado.tokens.filter { !it.ehHeroi }.map { it.id }.toSet()
        )
    }

    Scene(
        modifier = modifier,
        engine = engine,
        materialLoader = materialLoader,
        modelLoader = modelLoader,
        view = view,
        renderer = renderer,
        cameraNode = rememberCameraNode(engine).apply {
            position = Position(x = 0f, y = 12f, z = 6f)
            lookAt(Position(x = 0f, y = 0f, z = 0f))
        },
        isOpaque = false,
        surfaceType = SurfaceType.TextureSurface
    ) {
        // Luz solar mais forte para os PBR dos .glb.
        LightNode(type = LightManager.Type.SUN, apply = { intensity(120_000f) })

        // Chão 20x20 m.
        PlaneNode(
            size = Size(x = 20f, y = 0f, z = 20f),
            normal = io.github.sceneview.geometries.Plane.DEFAULT_NORMAL,
            materialInstance = chaoMi
        )

        for (token in tokens) {
            key(token.id) {
                TokenNode3D(
                    token = token,
                    selecionado = token.id == estado.tokenSelecionadoId,
                    instanciaHeroi = instanciaHeroi,
                    instanciaInimigo = instanciaInimigo,
                    fallbackHeroiMi = heroiFallbackMi,
                    fallbackInimigoMi = inimigoFallbackMi,
                    fallbackAliadoMi = aliadoMi,
                    haloMi = haloMi
                )
            }
        }
    }
}

/**
 * Renderiza UM token: modelo .glb (se disponível para a cor) OU cilindro fallback para ALIADO,
 * mais o halo circular no chão quando selecionado. Position e yaw são INTERPOLADOS em 200 ms para
 * evitar teleporte visual quando o estado muda.
 *
 * Limitação honesta: `animateFloatAsState` do yaw NÃO trata wrap circular — girar de 170° para −170°
 * anima pelo caminho longo (giro de 340° em vez de 20°). O caller pode chegar em um valor equivalente
 * módulo 360° para amenizar; corrigir com uma spec circular fica para o HEX-9.
 */
@Composable
private fun io.github.sceneview.SceneScope.TokenNode3D(
    token: HexRender3D.Token3D,
    selecionado: Boolean,
    instanciaHeroi: io.github.sceneview.model.ModelInstance?,
    instanciaInimigo: io.github.sceneview.model.ModelInstance?,
    fallbackHeroiMi: com.google.android.filament.MaterialInstance,
    fallbackInimigoMi: com.google.android.filament.MaterialInstance,
    fallbackAliadoMi: com.google.android.filament.MaterialInstance,
    haloMi: com.google.android.filament.MaterialInstance,
) {
    val yawGraus = Math.toDegrees(token.yawRad.toDouble()).toFloat()
    val xAnim by animateFloatAsState(targetValue = token.x, animationSpec = tween(200), label = "x")
    val zAnim by animateFloatAsState(targetValue = token.z, animationSpec = tween(200), label = "z")
    val yawAnim by animateFloatAsState(targetValue = yawGraus, animationSpec = tween(200), label = "yaw")

    val instancia = when (token.cor) {
        HexRender3D.Cor.HEROI -> instanciaHeroi
        HexRender3D.Cor.INIMIGO -> instanciaInimigo
        HexRender3D.Cor.ALIADO -> null // aliado NÃO tem .glb próprio ainda — cai no cilindro verde
    }

    if (instancia != null) {
        // Modelos .glb — auto-play animação (CesiumMan tem walk cycle), escala para caber em ~0.9 m
        // de aresta, base alinhada ao chão (centerOrigin y=-1).
        ModelNode(
            modelInstance = instancia,
            autoAnimate = true,
            scaleToUnits = 0.9f,
            centerOrigin = Position(x = 0f, y = -1f, z = 0f),
            position = Position(x = xAnim, y = 0f, z = zAnim),
            rotation = Rotation(y = yawAnim)
        )
    } else {
        // Fallback: cilindro colorido por categoria. Cobre:
        //   - HEROI/INIMIGO enquanto o .glb carrega assincronamente (evita token invisível no 1º frame).
        //   - ALIADO permanente enquanto não houver .glb próprio.
        val fallbackMi = when (token.cor) {
            HexRender3D.Cor.HEROI -> fallbackHeroiMi
            HexRender3D.Cor.INIMIGO -> fallbackInimigoMi
            HexRender3D.Cor.ALIADO -> fallbackAliadoMi
        }
        CylinderNode(
            radius = 0.35f,
            height = 1.6f,
            materialInstance = fallbackMi,
            position = Position(x = xAnim, y = 0.8f, z = zAnim),
            rotation = Rotation(y = yawAnim)
        )
    }

    // Halo de seleção — plano circular amarelo translúcido no chão, um pouco acima para não sobrepor.
    if (selecionado) {
        PlaneNode(
            size = Size(x = 1.3f, y = 0f, z = 1.3f),
            normal = Direction(y = 1f),
            materialInstance = haloMi,
            position = Position(x = xAnim, y = 0.03f, z = zAnim)
        )
    }
}

/** Converte um [Color] Compose em [io.github.sceneview.math.Color] (Float4 rgba 0..1). */
private fun Color.toFilamentColor(): io.github.sceneview.math.Color =
    io.github.sceneview.math.colorOf(r = red, g = green, b = blue, a = alpha)

/**
 * Adapta o estado DEMO (que usa [com.gurps.ficha.domain.combat.hex.TokenDemo]) para o formato usado pelo
 * projetor [HexRender3D] (que espera [com.gurps.ficha.domain.combat.hex.HexCombatState]).
 */
private fun HexTaticoState.toHexCombatState(): com.gurps.ficha.domain.combat.hex.HexCombatState {
    return com.gurps.ficha.domain.combat.hex.HexCombatState(
        posicoes = tokens.map {
            com.gurps.ficha.domain.combat.hex.PosicaoCombatente(
                id = it.id, posicao = it.posicao, facing = it.facing
            )
        }
    )
}
