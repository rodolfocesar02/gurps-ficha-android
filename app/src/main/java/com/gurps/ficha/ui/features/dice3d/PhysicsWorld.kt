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
    private val baseGravity = -40.0f

    init {
        // Setup Padrão JBullet
        val collisionConfiguration = DefaultCollisionConfiguration()
        val dispatcher = CollisionDispatcher(collisionConfiguration)
        val broadphase = DbvtBroadphase()
        val solver = SequentialImpulseConstraintSolver()

        dynamicsWorld = DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration)

        // Gravidade reduzida para evitar que o dado vibre infinitamente (jittering) contra o chão
        dynamicsWorld.setGravity(Vector3f(0f, baseGravity, 0f))
    }

    var onCollision: ((force: Float) -> Unit)? = null
    private var lastCollisionTime = 0L

    fun updateGravity(x: Float, y: Float, z: Float) {
        // Limita a influencia para evitar que saiam voando
        val maxTilt = 20f
        val newX = (x * 3f).coerceIn(-maxTilt, maxTilt)
        val newZ = (y * 3f).coerceIn(-maxTilt, maxTilt) // Mapeia o Y da tela para o Z da profundidade
        
        // Mantém a gravidade base puxando para baixo
        dynamicsWorld.setGravity(Vector3f(newX, baseGravity, newZ))
    }

    /**
     * Avança a simulação física baseado no tempo transcorrido.
     */
    fun stepSimulation(deltaTimeSec: Float) {
        dynamicsWorld.stepSimulation(deltaTimeSec, 10, 1f / 60f)
        
        // Verifica colisões para som e haptics
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
        
        if (maxImpulse > 1.5f) { // threshold para evitar spam sonoro/vibratório de micro-quiques
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastCollisionTime > 50) { // debounce de 50ms
                onCollision?.invoke(maxImpulse)
                lastCollisionTime = currentTime
            }
        }
    }

    fun addDice(size: Float = 1.0f, initialPosition: Vector3f): RigidBody {
        // Dado é um cubo (BoxShape recebe half-extents)
        val shape = BoxShape(Vector3f(size / 2f, size / 2f, size / 2f))
        
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
        
        val mass = 1.0f
        val localInertia = Vector3f(0f, 0f, 0f)
        shape.calculateLocalInertia(mass, localInertia)
        
        val motionState = DefaultMotionState(transform)
        val rbInfo = RigidBodyConstructionInfo(mass, motionState, shape, localInertia)
        
        // Restituição (Bounce) = 0.6 = elástico mas assenta
        rbInfo.restitution = 0.6f
        rbInfo.friction = 0.5f
        
        val body = RigidBody(rbInfo)
        
        // Dá um "spin" aleatório para ele girar loucamente ao nascer
        body.setLinearVelocity(Vector3f(
            (Math.random() * 20 - 10).toFloat(),
            0f,
            (Math.random() * 20 - 10).toFloat()
        ))
        body.setAngularVelocity(Vector3f((Math.random()*20 - 10).toFloat(), (Math.random()*20 - 10).toFloat(), (Math.random()*20 - 10).toFloat()))
        
        dynamicsWorld.addRigidBody(body)
        return body
    }

    fun createGround() {
        // Chão (plano)
        val groundShape = StaticPlaneShape(Vector3f(0f, 1f, 0f), 0f)
        val groundTransform = Transform()
        groundTransform.setIdentity()
        groundTransform.origin.set(Vector3f(0f, 0f, 0f))
        
        val groundMotionState = DefaultMotionState(groundTransform)
        val rbInfo = RigidBodyConstructionInfo(0f, groundMotionState, groundShape, Vector3f(0f, 0f, 0f))
        rbInfo.restitution = 0.5f // Chão quica um pouco
        rbInfo.friction = 0.8f
        
        val groundBody = RigidBody(rbInfo)
        dynamicsWorld.addRigidBody(groundBody)

        // Paredes invisíveis (para o dado não cair da tela)
        createWall(Vector3f(1f, 0f, 0f), -8f) // Direita
        createWall(Vector3f(-1f, 0f, 0f), -8f) // Esquerda
        createWall(Vector3f(0f, 0f, 1f), -12f) // Frente
        createWall(Vector3f(0f, 0f, -1f), -12f) // Trás
    }
    
    private fun createWall(normal: Vector3f, distance: Float) {
        val wallShape = StaticPlaneShape(normal, distance)
        val transform = Transform()
        transform.setIdentity()
        val motionState = DefaultMotionState(transform)
        val rbInfo = RigidBodyConstructionInfo(0f, motionState, wallShape, Vector3f(0f, 0f, 0f))
        rbInfo.restitution = 0.5f // Paredes quicam bem
        val wallBody = RigidBody(rbInfo)
        dynamicsWorld.addRigidBody(wallBody)
    }

    /**
     * Retorna a matriz 4x4 de transformação (translação + rotação) do corpo rígido
     */
    fun getTransformMatrix(body: RigidBody): FloatArray {
        val transform = Transform()
        body.motionState.getWorldTransform(transform)
        val matrix = FloatArray(16)
        transform.getOpenGLMatrix(matrix)
        return matrix
    }
}
