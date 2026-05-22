package com.gurps.ficha.ui.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

enum class EstadoVoz { OCIOSO, ESCUTANDO, PROCESSANDO, ERRO }

class VozMestreIA(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    var onResultado: (String) -> Unit = {}
    var onEstado: (EstadoVoz) -> Unit = {}

    fun iniciar() {
        mainHandler.post {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onEstado(EstadoVoz.ERRO)
            return@post
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).also { sr ->
            sr.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(p: Bundle?) { onEstado(EstadoVoz.ESCUTANDO) }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(v: Float) {}
                override fun onBufferReceived(b: ByteArray?) {}
                override fun onEndOfSpeech() { onEstado(EstadoVoz.PROCESSANDO) }
                override fun onPartialResults(r: Bundle?) {}
                override fun onEvent(t: Int, p: Bundle?) {}
                override fun onError(code: Int) { onEstado(EstadoVoz.ERRO) }
                override fun onResults(results: Bundle?) {
                    val texto = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()
                    onEstado(EstadoVoz.OCIOSO)
                    if (texto.isNotBlank()) onResultado(texto)
                }
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        recognizer?.startListening(intent)
        } // fim mainHandler.post
    }

    fun cancelar() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        onEstado(EstadoVoz.OCIOSO)
    }

    fun liberar() {
        recognizer?.destroy()
        recognizer = null
    }
}
