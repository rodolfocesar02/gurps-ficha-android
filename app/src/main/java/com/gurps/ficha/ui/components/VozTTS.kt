package com.gurps.ficha.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class VozTTS(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var pronto = false
    var onFimDeFala: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("pt", "BR")
                tts?.setSpeechRate(1.05f)
                tts?.setPitch(1.0f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) { onFimDeFala?.invoke() }
                    override fun onError(utteranceId: String?) { onFimDeFala?.invoke() }
                })
                pronto = true
            }
        }
    }

    fun falar(texto: String) {
        if (!pronto) return
        tts?.stop()
        val textoLimpo = limparParaFala(texto)
        if (textoLimpo.isBlank()) return
        tts?.speak(textoLimpo, TextToSpeech.QUEUE_FLUSH, null, "mestre_ia_fala")
    }

    fun parar() {
        tts?.stop()
    }

    fun liberar() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        pronto = false
    }

    private fun limparParaFala(texto: String): String {
        return texto
            .replace(Regex("```[\\s\\S]*?```"), "") // remove blocos de código
            .replace(Regex("`[^`]+`"), "")           // remove código inline
            .replace(Regex("\\[Pág\\.?\\s*\\d+\\]"), "") // remove [Pág. 7]
            .replace(Regex("\\*{1,3}([^*]+)\\*{1,3}"), "$1") // remove negrito/itálico
            .replace(Regex("#{1,6}\\s*"), "")        // remove títulos markdown
            .replace(Regex("^[-*]\\s+", RegexOption.MULTILINE), "") // remove marcadores de lista
            .replace(Regex("\\n{2,}"), ". ")         // parágrafos viram pausas
            .replace("\n", " ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()
            .take(800) // limita para não falar respostas enormes
    }
}
