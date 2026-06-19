package com.gurps.ficha.ui.features.dice3d

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class DiceSensorManager(context: Context, private val physicsWorld: PhysicsWorld) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    // Low-pass filter para evitar que o dado trema com micro-movimentos
    private val alpha = 0.8f
    private val gravity = FloatArray(3)

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Isola a força da gravidade usando um filtro passa-baixa (Low-pass filter)
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

            // Atualiza a física (lembrando que no celular em pé, o Y é pra baixo, 
            // e queremos refletir isso nos eixos X e Z do JBullet)
            physicsWorld.updateGravity(gravity[0], gravity[1], gravity[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Não é necessário implementar
    }
}
