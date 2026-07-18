package com.gurps.ficha.domain.combat

/**
 * Lote LOG-1: espelho do combate no logcat do Android Studio.
 *
 * **Como usar no Android Studio:** abra o Logcat e filtre por `tag:Saga_Combate`.
 * Toda linha que o motor escreve no log narrativo da luta (NH, rolagem, dano, RD, condição,
 * escape, recusa por alcance…) sai lá também, na ordem em que aconteceu.
 *
 * O motor de combate não tinha log nenhum até aqui — todo o logging do app era do lado da IA
 * (`MestreIA_*`). Como o combate já narra os números em 164 pontos, espelhar a lista dá a luta
 * inteira de graça, sem instrumentar cada um deles.
 *
 * Seguro para os testes: o `unitTests.isReturnDefaultValues = true` do build faz `android.util.Log`
 * virar no-op no JVM, então nada aqui quebra a suíte nem suja a saída do gradle.
 */
object SagaLog {
    const val TAG = "Saga_Combate"

    /** Desligue para silenciar sem mexer no resto. */
    var ativo: Boolean = true

    fun d(msg: String) {
        if (!ativo) return
        android.util.Log.d(TAG, msg)
    }

    /** Detalhe mecânico que NÃO aparece na narrativa (números intermediários). */
    fun mecanica(msg: String) {
        if (!ativo) return
        android.util.Log.d(TAG, "⚙ $msg")
    }
}

/**
 * Lista do log narrativo que espelha cada linha nova para o logcat.
 *
 * Trocar `mutableListOf()` por esta classe é a única alteração necessária: em Kotlin, `log += "x"`
 * numa `MutableList` chama `add`, então os 164 pontos existentes passam a espelhar sem serem tocados.
 */
class LogDeCombate : ArrayList<String>() {
    override fun add(element: String): Boolean {
        SagaLog.d(element)
        return super.add(element)
    }
}
