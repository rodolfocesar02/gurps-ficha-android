package com.gurps.ficha.ui.features.dice3d

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import io.github.sceneview.math.toTransform
import com.bulletphysics.dynamics.RigidBody
import javax.vecmath.Vector3f
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import io.github.sceneview.Scene
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberView
import io.github.sceneview.rememberRenderer
import com.google.android.filament.View as FilamentView
import io.github.sceneview.node.LightNode
import com.google.android.filament.LightManager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults

import androidx.compose.ui.platform.LocalContext

@Composable
fun Dice3DScene(
    modifier: Modifier = Modifier,
    diceCount: Int = 3,
    onRollFinished: (List<Int>) -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { DiceSoundManager(context) }
    
    val physicsWorld = remember { 
        PhysicsWorld().apply { 
            createGround() 
            onCollision = { force ->
                soundManager.playBounceSound(force)
            }
        } 
    }
    
    // Arrays para os N dados
    val diceRigidBodies = remember(diceCount) { mutableStateListOf<RigidBody?>().apply { repeat(diceCount) { add(null) } } }
    val modelNodes = remember(diceCount) { mutableStateListOf<ModelNode?>().apply { repeat(diceCount) { add(null) } } }
    val diceResults = remember(diceCount) { mutableStateListOf<Int?>().apply { repeat(diceCount) { add(null) } } }
    
    // Inicia a rolagem automaticamente quando a cena é aberta
    var isRolling by remember { mutableStateOf(true) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    LaunchedEffect(Unit) {
        var lastFrameTime = System.nanoTime()
        while (true) {
            withFrameNanos { frameTime ->
                val deltaTime = (frameTime - lastFrameTime) / 1_000_000_000f
                lastFrameTime = frameTime
                
                if (deltaTime > 0f) {
                    physicsWorld.stepSimulation(deltaTime)
                    
                    var allStopped = true
                    
                    for (i in 0 until diceCount) {
                        diceRigidBodies[i]?.let { rb ->
                            val matrix = physicsWorld.getTransformMatrix(rb)
                            
                            // Preserva a escala calculada pelo SceneView (scaleToUnits)
                            val scale = modelNodes[i]?.scale ?: io.github.sceneview.math.Position(1f, 1f, 1f)
                            matrix[0] *= scale.x; matrix[1] *= scale.x; matrix[2] *= scale.x
                            matrix[4] *= scale.y; matrix[5] *= scale.y; matrix[6] *= scale.y
                            matrix[8] *= scale.z; matrix[9] *= scale.z; matrix[10] *= scale.z
                            
                            modelNodes[i]?.transform = matrix.toTransform()

                            if (isRolling) {
                                val v = Vector3f()
                                rb.getLinearVelocity(v)
                                val av = Vector3f()
                                rb.getAngularVelocity(av)
                                
                                if (v.lengthSquared() > 0.05f || av.lengthSquared() > 0.05f) {
                                    allStopped = false
                                }
                            }
                        }
                    }
                    
                    if (isRolling && allStopped && diceRigidBodies.all { it != null }) {
                        isRolling = false
                        val finalResults = mutableListOf<Int>()
                        for (i in 0 until diceCount) {
                            diceRigidBodies[i]?.let { rb ->
                                val btTransform = com.bulletphysics.linearmath.Transform()
                                rb.motionState.getWorldTransform(btTransform)
                                val value = readDieValue(btTransform)
                                diceResults[i] = value
                                finalResults.add(value)
                            }
                        }
                        onRollFinished(finalResults)
                    }
                }
            }
        }
    }

    val engine = rememberEngine()
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

    val NoRippleNodeFactory = remember {
        object : IndicationNodeFactory {
            override fun create(interactionSource: InteractionSource): androidx.compose.ui.node.DelegatableNode {
                return object : androidx.compose.ui.Modifier.Node() {}
            }
            override fun equals(other: Any?): Boolean = this === other
            override fun hashCode(): Int = System.identityHashCode(this)
        }
    }

    Box(modifier = modifier.fillMaxSize()) { 
        CompositionLocalProvider(LocalIndication provides NoRippleNodeFactory) {
            Scene(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                view = view,
                renderer = renderer,
                cameraNode = rememberCameraNode(engine).apply {
                    // Visão top-down inclinada, câmera um pouco mais alta para caberem bem
                    position = io.github.sceneview.math.Position(x = 0f, y = 18f, z = 4f)
                    lookAt(io.github.sceneview.math.Position(x = 0f, y = 0f, z = 0f))
                },
                isOpaque = false,
                surfaceType = io.github.sceneview.SurfaceType.TextureSurface
            ) {
                LightNode(
                    type = LightManager.Type.SUN
                )

                // Instâncias do mesmo modelo
                for (i in 0 until diceCount) {
                    val model = rememberModelInstance(modelLoader, "models/Dado.glb")
                    
                    if (model != null) {
                        LaunchedEffect(model) {
                            if (diceRigidBodies[i] == null) {
                                // Inicia cada um numa posição espalhada com bastante aleatoriedade
                                val randomX = (Math.random() * 10 - 5).toFloat()
                                val randomZ = (Math.random() * 6 - 3).toFloat()
                                val randomY = 15f + (Math.random() * 6).toFloat() // Mais alto para dar tempo de girar no ar
                                val initialPos = Vector3f(randomX, randomY, randomZ)
                                
                                diceRigidBodies[i] = physicsWorld.addDice(1.2f, initialPos)
                            }
                        }

                        ModelNode(
                            modelInstance = model,
                            scaleToUnits = 1.2f, // Um pouco menores para caberem vários
                            apply = {
                                modelNodes[i] = this
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiceResultBox(value: Int) {
    Box(
        modifier = Modifier
            .background(Color(0xFFE52E2D), shape = RoundedCornerShape(6.dp)) // Vermelho dado
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            color = Color.White,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge
        )
    }
}

fun readDieValue(transform: com.bulletphysics.linearmath.Transform): Int {
    var best = -2f
    var value = 1
    
    val faces = listOf(
        Pair(Vector3f(0f, 1f, 0f), 1),   // +Y
        Pair(Vector3f(0f, -1f, 0f), 6),  // -Y
        Pair(Vector3f(1f, 0f, 0f), 3),   // +X
        Pair(Vector3f(-1f, 0f, 0f), 4),  // -X
        Pair(Vector3f(0f, 0f, 1f), 2),   // +Z
        Pair(Vector3f(0f, 0f, -1f), 5)   // -Z
    )
    
    for ((axis, num) in faces) {
        val rotated = Vector3f()
        transform.basis.transform(axis, rotated)
        if (rotated.y > best) {
            best = rotated.y
            value = num
        }
    }
    return value
}
