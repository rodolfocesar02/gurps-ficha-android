package com.gurps.ficha.ui.features.mesa

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.webkit.WebView

/**
 * **O app ouve o comando de voz da Mesa** — lote MF-9.
 *
 * A janela da Mesa não reconhece voz; o Android sim. Ouve UMA frase em
 * português do Brasil e devolve o texto à página ([ComandoDeVozDaMesa]).
 *
 * ⚠️ **Só se prova no aparelho.** A Mesa corta a voz dela para a mesa antes de
 * pedir (`trilha.enabled = false`), mas o microfone continua aberto pela
 * chamada. Em alguns aparelhos o reconhecedor divide o microfone; em outros
 * responde "ocupado" — e aí a Mesa diz isso em voz alta e os botões continuam
 * ali. Ver o `PLANO_MESA_FALADA.md`, MF-9.
 *
 * 🔴 Tudo na linha principal: o `SpeechRecognizer` exige, e a [PonteDaMesa] já
 * entrega o pedido lá.
 */
object OuvidoDoComando {

    private var reconhecedor: SpeechRecognizer? = null

    fun ouvir(janela: WebView) {
        val contexto = janela.context
        if (!SpeechRecognizer.isRecognitionAvailable(contexto)) {
            janela.evaluateJavascript(ComandoDeVozDaMesa.oJavascriptDaFalha("indisponivel"), null)
            return
        }
        reconhecedor?.destroy()
        val r = SpeechRecognizer.createSpeechRecognizer(contexto)
        reconhecedor = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onResults(resultados: Bundle?) {
                val texto = resultados
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                janela.evaluateJavascript(
                    if (texto.isBlank()) ComandoDeVozDaMesa.oJavascriptDaFalha("nada")
                    else ComandoDeVozDaMesa.oJavascriptDoTexto(texto),
                    null
                )
                largar()
            }

            override fun onError(codigo: Int) {
                janela.evaluateJavascript(
                    ComandoDeVozDaMesa.oJavascriptDaFalha(ComandoDeVozDaMesa.motivoDoErro(codigo)),
                    null
                )
                largar()
            }

            override fun onReadyForSpeech(p: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(p: Bundle?) {}
            override fun onEvent(t: Int, p: Bundle?) {}
        })
        val pedido = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        r.startListening(pedido)
    }

    private fun largar() {
        reconhecedor?.destroy()
        reconhecedor = null
    }
}
