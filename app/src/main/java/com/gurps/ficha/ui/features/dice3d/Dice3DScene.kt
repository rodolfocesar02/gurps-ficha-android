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

@Composable
fun Dice3DScene(modifier: Modifier = Modifier) {
    val physicsWorld = remember { PhysicsWorld().apply { createGround() } }
    
    // Arrays para os 3 dados
    val diceRigidBodies = remember { mutableStateListOf<RigidBody?>(null, null, null) }
    val modelNodes = remember { mutableStateListOf<ModelNode?>(null, null, null) }
    val diceResults = remember { mutableStateListOf<Int?>(null, null, null) }
    
    var isRolling by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        var lastFrameTime = System.nanoTime()
        while (true) {
            withFrameNanos { frameTime ->
                val deltaTime = (frameTime - lastFrameTime) / 1_000_000_000f
                lastFrameTime = frameTime
                
                if (deltaTime > 0f) {
                    physicsWorld.stepSimulation(deltaTime)
                    
                    var allStopped = true
                    
                    for (i in 0..2) {
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
                    
                    if (isRolling && allStopped) {
                        isRolling = false
                        for (i in 0..2) {
                            diceRigidBodies[i]?.let { rb ->
                                val btTransform = com.bulletphysics.linearmath.Transform()
                                rb.motionState.getWorldTransform(btTransform)
                                diceResults[i] = readDieValue(btTransform)
                            }
                        }
                    }
                }
            }
        }
    }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    val NoRippleNodeFactory = remember {
        object : IndicationNodeFactory {
            override fun create(interactionSource: InteractionSource): androidx.compose.ui.node.DelegatableNode {
                return object : androidx.compose.ui.Modifier.Node() {}
            }
            override fun equals(other: Any?): Boolean = this === other
            override fun hashCode(): Int = System.identityHashCode(this)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0F172A))) { // Fundo mais escuro
        CompositionLocalProvider(LocalIndication provides NoRippleNodeFactory) {
            Scene(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                cameraNode = rememberCameraNode(engine).apply {
                    // Visão top-down inclinada, câmera um pouco mais alta para caberem bem
                    position = io.github.sceneview.math.Position(x = 0f, y = 18f, z = 4f)
                    lookAt(io.github.sceneview.math.Position(x = 0f, y = 0f, z = 0f))
                },
                isOpaque = false
            ) {
                LightNode(
                    type = LightManager.Type.SUN
                )

                // 3 Instâncias do mesmo modelo
                for (i in 0..2) {
                    val model = rememberModelInstance(modelLoader, "models/Dado.glb")
                    
                    if (model != null) {
                        LaunchedEffect(model) {
                            if (diceRigidBodies[i] == null) {
                                // Inicia cada um numa posição espalhada
                                val initialPositions = listOf(
                                    Vector3f(-2f, 5f, -1f),
                                    Vector3f(0f, 7f, 1f),
                                    Vector3f(2f, 5f, -0.5f)
                                )
                                diceRigidBodies[i] = physicsWorld.addDice(1.2f, initialPositions[i])
                            }
                        }

                        ModelNode(
                            modelInstance = model,
                            scaleToUnits = 1.2f, // Um pouco menores para caberem 3
                            apply = {
                                modelNodes[i] = this
                            }
                        )
                    }
                }
            }
        }
        
        // HUD Inferior (Neon GURPS Style)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth(0.9f)
                .background(Color(0xD90B1320), shape = RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp)) // Cyan border
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Resultados dos dados
                if (diceResults.all { it != null }) {
                    val r1 = diceResults[0] ?: 0
                    val r2 = diceResults[1] ?: 0
                    val r3 = diceResults[2] ?: 0
                    val soma = r1 + r2 + r3
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DiceResultBox(r1)
                        Text(" + ", color = Color.White)
                        DiceResultBox(r2)
                        Text(" + ", color = Color.White)
                        DiceResultBox(r3)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "= $soma",
                            color = Color(0xFF00E5FF), // Cyan text
                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                        )
                    }
                } else if (isRolling) {
                    Text(
                        text = "ROLANDO...",
                        color = Color(0xFF00E5FF),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        text = "Aguardando Rolagem",
                        color = Color.Gray,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        diceResults[0] = null
                        diceResults[1] = null
                        diceResults[2] = null
                        isRolling = true
                        
                        diceRigidBodies.forEachIndexed { i, rb ->
                            rb?.let {
                                val transform = com.bulletphysics.linearmath.Transform()
                                transform.setIdentity()
                                
                                val startPositions = listOf(
                                    Vector3f(-2f, 6f, -1f),
                                    Vector3f(0f, 7f, 1f),
                                    Vector3f(2f, 6f, -1f)
                                )
                                transform.origin.set(startPositions[i])
                                
                                rb.setWorldTransform(transform)
                                rb.setLinearVelocity(Vector3f(0f, 0f, 0f))
                                rb.setAngularVelocity(Vector3f(0f, 0f, 0f))
                                rb.clearForces()

                                // Variações aleatórias: jogando-os de volta pra mesa (Z negativo, X variado)
                                val vx = (Math.random() * 8 - 4).toFloat()
                                val vy = 8f + (Math.random() * 4).toFloat()
                                val vz = (Math.random() * -10).toFloat()
                                rb.setLinearVelocity(Vector3f(vx, vy, vz))
                                
                                val ax = (Math.random() * 40 - 20).toFloat()
                                val ay = (Math.random() * 40 - 20).toFloat()
                                val az = (Math.random() * 40 - 20).toFloat()
                                rb.setAngularVelocity(Vector3f(ax, ay, az))
                                
                                rb.activate(true)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D233A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ROLAR 3D6", color = Color(0xFF00E5FF)) // Cyan text
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
