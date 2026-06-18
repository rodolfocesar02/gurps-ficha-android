package com.gurps.ficha.ui.features.dice3d

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.gurps.ficha.R

/**
 * Lote 3: Gerenciador de Áudio de Colisão.
 * Dispara os sons do dado quicando, calculando o volume 
 * com base na força física (velocidade) da colisão.
 */
class DiceSoundManager(context: Context) {

    private var soundPool: SoundPool
    private var rollSoundId: Int = -1

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // Permite até 10 quiques simultâneos
        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        rollSoundId = soundPool.load(context, R.raw.dice_roll, 1)
    }

    /**
     * Toca o som do dado quicando.
     * @param collisionForce Força do impacto calculada pelo JBullet (velocidade no eixo Y)
     */
    fun playBounceSound(collisionForce: Float) {
        if (rollSoundId == -1) return

        // Mapeia a força física (ex: 0.1 a 10.0) para volume do Android (0.0f a 1.0f)
        var volume = (collisionForce / 10f).coerceIn(0.1f, 1.0f)

        // Variação de pitch para não parecer artificial (quiques rápidos têm som diferente)
        val pitch = 0.9f + (Math.random() * 0.2f).toFloat() 

        soundPool.play(rollSoundId, volume, volume, 1, 0, pitch)
    }

    fun release() {
        soundPool.release()
    }
}
