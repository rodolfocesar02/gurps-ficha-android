package com.gurps.ficha.ui.features.dice3d

import com.bulletphysics.collision.broadphase.DbvtBroadphase
import com.bulletphysics.collision.dispatch.CollisionDispatcher
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration
import com.bulletphysics.dynamics.DiscreteDynamicsWorld
import com.bulletphysics.collision.shapes.BoxShape
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver
import com.bulletphysics.collision.shapes.StaticPlaneShape
import com.bulletphysics.dynamics.RigidBody
import com.bulletphysics.dynamics.RigidBodyConstructionInfo
import com.bulletphysics.linearmath.DefaultMotionState
import com.bulletphysics.linearmath.Transform
import javax.vecmath.Vector3f

/**
 * Motor de Física 3D rodando paralelamente à renderização (SceneView).
 * Esta classe isola a complexidade do JBullet.
 */
class PhysicsWorld {

    val dynamicsWorld: DiscreteDynamicsWorld

    init {
        // Setup Padrão JBullet
        val collisionConfiguration = DefaultCollisionConfiguration()
        val dispatcher = CollisionDispatcher(collisionConfiguration)
        val broadphase = DbvtBroadphase()
        val solver = SequentialImpulseConstraintSolver()

        dynamicsWorld = DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration)

        // Gravidade reduzida para evitar que o dado vibre infinitamente (jittering) contra o chão
        dynamicsWorld.setGravity(Vector3f(0f, -40.0f, 0f))
    }

    var onCollision: ((force: Float) -> Unit)? = null
    private var lastCollisionTime = 0L

    /**
     * Avança a simulação física baseado no tempo transcorrido.
     */
    fun stepSimulation(deltaTimeSec: Float) {
        dynamicsWorld.stepSimulation(deltaTimeSec, 10, 1f / 60f)
        
        // Verifica colisões para som
        val dispatcher = dynamicsWorld.dispatcher
        val numManifolds = dispatcher.numManifolds
        var maxImpulse = 0f
        
        for (i in 0 until numManifolds) {
            val contactManifold = dispatcher.getManifoldByIndexInternal(i)
            val numContacts = contactManifold.numContacts
            for (j in 0 until numContacts) {
                val pt = contactManifold.getContactPoint(j)
                if (pt.appliedImpulse > maxImpulse) {
                    maxImpulse = pt.appliedImpulse
                }
            }
        }
        
        // Debounce de 80ms para evitar spam do SoundPool (Log infinito)
        val currentTime = System.currentTimeMillis()
        // O impulso de repouso para a gravidade -40 (com massa 1.0) é por volta de 0.6 por frame.
        // Bater no chão gera muito mais, então o threshold tem que ser alto (> 3.0) para ignorar o jitter
        if (maxImpulse > 3.0f && (currentTime - lastCollisionTime) > 80) {
            onCollision?.invoke(maxImpulse)
            lastCollisionTime = currentTime
        }
    }

    /**
     * Adiciona o chão/mesa para os dados não caírem infinitamente.
     */
    fun createGround() {
        val groundShape = StaticPlaneShape(Vector3f(0f, 1f, 0f), 0f)
        
        val groundTransform = Transform()
        groundTransform.setIdentity()
        groundTransform.origin.set(0f, -1f, 0f) // 1 unidade abaixo da câmera
        
        val motionState = DefaultMotionState(groundTransform)
        val rbInfo = RigidBodyConstructionInfo(0f, motionState, groundShape, Vector3f(0f, 0f, 0f))
        
        // Atrito da mesa aumentado, quique diminuído para realismo (plástico no feltro)
        rbInfo.restitution = 0.3f // Quique
        rbInfo.friction = 0.8f    // Atrito

        val groundRigidBody = RigidBody(rbInfo)
        dynamicsWorld.addRigidBody(groundRigidBody)
        
        // Paredes invisíveis com grande espessura para evitar tunnelling (dados passarem através)
        val thickness = 10f
        val wallHeight = 40f
        
        // Esquerda e Direita (limites em X mais apertados para não sair da tela)
        createWall(Vector3f(thickness, wallHeight, 20f), Vector3f(-3.5f - thickness, 0f, 0f))
        createWall(Vector3f(thickness, wallHeight, 20f), Vector3f(3.5f + thickness, 0f, 0f))
        
        // Fundo (longe) e Frente (perto) (limites em Z)
        createWall(Vector3f(20f, wallHeight, thickness), Vector3f(0f, 0f, -3.5f - thickness))
        createWall(Vector3f(20f, wallHeight, thickness), Vector3f(0f, 0f, 5f + thickness))
    }

    private fun createWall(halfExtents: Vector3f, position: Vector3f) {
        val shape = BoxShape(halfExtents)
        val transform = Transform()
        transform.setIdentity()
        transform.origin.set(position)
        
        val motionState = DefaultMotionState(transform)
        val rbInfo = RigidBodyConstructionInfo(0f, motionState, shape, Vector3f(0f, 0f, 0f))
        rbInfo.restitution = 0.3f
        rbInfo.friction = 0.5f
        dynamicsWorld.addRigidBody(RigidBody(rbInfo))
    }

    /**
     * Adiciona um dado genérico na simulação física.
     * @param size O tamanho do dado calculado a partir da BoundingBox do modelo .glb
     * @param initialPosition A posição (x,y,z) de onde o dado vai ser jogado
     * @return O corpo rígido (RigidBody) para sincronizarmos com o SceneView depois
     */
    fun addDice(size: Float, initialPosition: Vector3f): RigidBody {
        // 1. Cria a caixa de colisão do tamanho dinâmico do modelo
        val halfExtents = size / 2f
        val boxShape = BoxShape(Vector3f(halfExtents, halfExtents, halfExtents))

        // 2. Define a posição e Rotação inicial aleatória
        val transform = Transform()
        transform.setIdentity()
        
        val rotX = javax.vecmath.Matrix3f()
        rotX.rotX((Math.random() * Math.PI * 2).toFloat())
        val rotY = javax.vecmath.Matrix3f()
        rotY.rotY((Math.random() * Math.PI * 2).toFloat())
        val rotZ = javax.vecmath.Matrix3f()
        rotZ.rotZ((Math.random() * Math.PI * 2).toFloat())
        
        rotX.mul(rotY)
        rotX.mul(rotZ)
        transform.basis.set(rotX)
        
        transform.origin.set(initialPosition)

        // 3. Define massa (dados não são estáticos, então massa > 0)
        val mass = 1.0f
        val localInertia = Vector3f(0f, 0f, 0f)
        boxShape.calculateLocalInertia(mass, localInertia)

        // 4. Cria o corpo físico
        val motionState = DefaultMotionState(transform)
        val rbInfo = RigidBodyConstructionInfo(mass, motionState, boxShape, localInertia)
        
        // Quique do dado (restitution) e atrito
        rbInfo.restitution = 0.2f
        rbInfo.friction = 0.8f
        // Amortecimento aumentado para forçar a inércia e parar de vibrar
        rbInfo.linearDamping = 0.5f
        rbInfo.angularDamping = 0.5f

        val diceRigidBody = RigidBody(rbInfo)
        
        // Impulso aleatório de movimento (espalhamento) e giro
        diceRigidBody.setLinearVelocity(Vector3f(
            (Math.random() * 20 - 10).toFloat(), // X
            0f,                                  // Y
            (Math.random() * 20 - 10).toFloat()  // Z
        ))
        
        diceRigidBody.setAngularVelocity(Vector3f(
            (Math.random() * 50 - 25).toFloat(),
            (Math.random() * 50 - 25).toFloat(),
            (Math.random() * 50 - 25).toFloat()
        ))
        
        // 5. Adiciona o dado no mundo da simulação
        dynamicsWorld.addRigidBody(diceRigidBody)
        
        return diceRigidBody
    }

    /**
     * Obtém a matriz de transformação do corpo rígido no formato 4x4 (FloatArray)
     * compatível com a engine do SceneView.
     */
    fun getTransformMatrix(body: RigidBody): FloatArray {
        val transform = Transform()
        body.motionState.getWorldTransform(transform)
        val matrix = FloatArray(16)
        transform.getOpenGLMatrix(matrix)
        return matrix
    }
}
