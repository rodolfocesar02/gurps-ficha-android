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
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.CylinderNode
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.PlaneNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberRenderer
import io.github.sceneview.rememberView

/**
 * Lote HEX-7 (Fase 5 do PILAR): render 3D declarativo do combate tático em hexágonos usando SceneView
 * (wrap Compose de Filament — já presente no projeto desde o lote dos dados 3D).
 *
 * Escopo desta fatia (definido pelo usuário):
 *   1. Plataforma vazia (plano chão) + luz solar.
 *   2. Grade de hex desenhada como OVERLAY 2D transparente por cima da Scene — reusa a matemática do
 *      [HexCanvasDemo]. Alinhamento perfeito 2D/3D fica para o HEX-9 (câmera ortográfica dedicada).
 *   3. Tokens como CILINDROS COLORIDOS 3D (herói azul, inimigo vermelho, aliado verde) — modelos .glb
 *      ficam para o HEX-8.
 *   4. Tap acontece no overlay 2D e altera o estado; a cena 3D re-renderiza a partir dele.
 *
 * A projeção pura vive em [HexRender3D] (kotlin puro, testado); esta função só monta a árvore de nodes.
 */

private val COR_FUNDO_3D = Color(0xFF0E1B29)
private val COR_CHAO_3D = Color(0xFF2F3F52)
private val COR_HEROI_3D = Color(0xFF3B82F6)
private val COR_INIMIGO_3D = Color(0xFFEF4444)
private val COR_ALIADO_3D = Color(0xFF10B981)

@Composable
fun HexScene3DDemo(modifier: Modifier = Modifier) {
    // Reusa o estado DEMO do HEX-2 (mesma origem, mesmo raio). Quando HEX-8 pluga com CombatSession real,
    // este `by remember` cai para `viewModel.hexTaticoState`.
    var estado by remember { mutableStateOf(HexTaticoState.demoInicial()) }
    val textMeasurer = rememberTextMeasurer()

    Column(modifier = modifier.background(COR_FUNDO_3D)) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⬢ Modo tático 3D (demo)", color = Color.White, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            Text("Toque num token, depois num hex adjacente para mover", color = Color(0xCCFFFFFF),
                style = MaterialTheme.typography.labelSmall)
        }

        // Área da cena — 3D no fundo, overlay 2D em cima.
        Box(Modifier.fillMaxWidth().weight(1f)) {
            HexScene3DBase(estado = estado, modifier = Modifier.fillMaxSize())
            // Overlay 2D transparente: grade + hex selecionado + captura de tap.
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
 * Cena 3D pura (chão + luz + tokens cilíndricos). Não desenha grade — isso fica no overlay 2D. Isolada
 * para poder ser reusada por outra tela sem o overlay caso preciso.
 */
@Composable
private fun HexScene3DBase(estado: HexTaticoState, modifier: Modifier = Modifier) {
    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
    val view = rememberView(engine).apply {
        blendMode = FilamentView.BlendMode.TRANSLUCENT
    }
    val renderer = rememberRenderer(engine).apply {
        clearOptions = clearOptions.apply {
            clear = true
            clearColor = floatArrayOf(0f, 0f, 0f, 0f)
        }
    }

    val tokens = remember(estado) {
        HexRender3D.projetar(
            estado.toHexCombatState(),
            idHeroi = "heroi",
            idsInimigos = estado.tokens.filter { !it.ehHeroi }.map { it.id }.toSet()
        )
    }

    // MaterialInstances memoizadas UMA VEZ pela vida da Scene. Filament não coleta lixo — cada chamada
    // a `createColorInstance` é rastreada no MaterialLoader até `destroy()`, então sem `remember` cada
    // recomposição (todo tap!) vaza 3+ instâncias. As 4 cores são constantes, então uma alocação basta.
    val chaoMi = remember(materialLoader) { materialLoader.createColorInstance(
        color = COR_CHAO_3D.toFilamentColor(), metallic = 0.0f, roughness = 0.9f, reflectance = 0.0f) }
    val heroiMi = remember(materialLoader) { materialLoader.createColorInstance(
        color = COR_HEROI_3D.toFilamentColor(), metallic = 0.1f, roughness = 0.5f, reflectance = 0.3f) }
    val inimigoMi = remember(materialLoader) { materialLoader.createColorInstance(
        color = COR_INIMIGO_3D.toFilamentColor(), metallic = 0.1f, roughness = 0.5f, reflectance = 0.3f) }
    val aliadoMi = remember(materialLoader) { materialLoader.createColorInstance(
        color = COR_ALIADO_3D.toFilamentColor(), metallic = 0.1f, roughness = 0.5f, reflectance = 0.3f) }

    Scene(
        modifier = modifier,
        engine = engine,
        materialLoader = materialLoader,
        view = view,
        renderer = renderer,
        cameraNode = rememberCameraNode(engine).apply {
            // Câmera top-down inclinada — dá senso de 3D sem esconder o chão.
            position = Position(x = 0f, y = 12f, z = 6f)
            lookAt(Position(x = 0f, y = 0f, z = 0f))
        },
        isOpaque = false,
        surfaceType = SurfaceType.TextureSurface
    ) {
        // Composable do SceneScope — SEM `engine=` (captura do scope) e SEM `.apply { }`, senão
        // Kotlin resolve para o construtor da CLASSE em vez do Composable, criando nodes órfãos que
        // NUNCA são anexados a `filament.scene` (cena renderiza vazia).
        LightNode(type = LightManager.Type.SUN)

        // Chão: plano cinza escuro de 20x20 m (Y-up, normal para cima).
        PlaneNode(
            size = io.github.sceneview.math.Size(x = 20f, y = 0f, z = 20f),
            normal = io.github.sceneview.geometries.Plane.DEFAULT_NORMAL,
            materialInstance = chaoMi
        )

        // Tokens: um cilindro colorido por combatente. `key(id)` estabiliza positional identity do
        // Compose se a ordem da lista mudar.
        for (token in tokens) {
            key(token.id) {
                val mi = when (token.cor) {
                    HexRender3D.Cor.HEROI -> heroiMi
                    HexRender3D.Cor.INIMIGO -> inimigoMi
                    HexRender3D.Cor.ALIADO -> aliadoMi
                }
                CylinderNode(
                    radius = 0.35f,
                    height = 1.6f,
                    materialInstance = mi,
                    // Cilindro fica DE PÉ no chão: altura/2 acima de Y=0.
                    position = Position(x = token.x, y = 0.8f, z = token.z),
                    // Yaw em torno do eixo Y (SceneView Rotation em graus).
                    rotation = Rotation(y = Math.toDegrees(token.yawRad.toDouble()).toFloat())
                )
            }
        }
    }
}

/** Converte um [Color] Compose em [io.github.sceneview.math.Color] (Float4 rgba 0..1). */
private fun Color.toFilamentColor(): io.github.sceneview.math.Color =
    io.github.sceneview.math.colorOf(r = red, g = green, b = blue, a = alpha)

/**
 * Adapta o estado DEMO (que usa [com.gurps.ficha.domain.combat.hex.TokenDemo]) para o formato usado pelo
 * projetor [HexRender3D] (que espera [com.gurps.ficha.domain.combat.hex.HexCombatState]). Ponte simples
 * — no HEX-8 o estado real do combate substitui esta conversão.
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
