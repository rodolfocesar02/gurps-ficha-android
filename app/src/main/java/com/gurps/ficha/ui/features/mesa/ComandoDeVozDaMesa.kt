package com.gurps.ficha.ui.features.mesa

/**
 * **O comando de voz da Mesa, dentro do aplicativo** — lote MF-9 do
 * `PLANO_MESA_FALADA.md` (a parte que se testa sem aparelho).
 *
 * No navegador, a Mesa ouve o comando sozinha ("noroeste, três hexágonos"). Na
 * janela do aplicativo (WebView) **não há reconhecimento de voz** — então quem
 * ouve é o app, e o texto volta para a página pela ponte:
 *
 * 1. a página chama `window.Ficha.ouvirComando()` ([PonteDaMesa.ouvirComando]);
 * 2. o [OuvidoDoComando] ouve com o reconhecedor do Android;
 * 3. o texto volta por [oJavascriptDoTexto], e a Mesa faz o que foi dito pelo
 *    MESMO caminho dos botões (`window.mesaFaladaVoz.doAplicativo`).
 *
 * ## 🔴 O texto vem de fora, e vira código
 *
 * O que a pessoa disse entra numa linha de JavaScript. Sem as aspas certas, um
 * `"` ou uma quebra de linha no meio do que foi ouvido quebraria a linha — ou
 * pior, viraria código. Por isso [aspas] escapa tudo o que o JavaScript trata
 * como especial, e a bancada prova.
 */
object ComandoDeVozDaMesa {

    /** O texto entre aspas de JavaScript, sem nada que feche a string antes da hora. */
    fun aspas(texto: String): String {
        val s = StringBuilder("\"")
        for (c in texto) {
            when (c) {
                '\\' -> s.append("\\\\")
                '"' -> s.append("\\\"")
                '\n' -> s.append("\\n")
                '\r' -> s.append("\\r")
                '\t' -> s.append("\\t")
                ' ' -> s.append("\\u2028")
                ' ' -> s.append("\\u2029")
                '<' -> s.append("\\u003c")
                else -> if (c < ' ') s.append(String.format("\\u%04x", c.code)) else s.append(c)
            }
        }
        return s.append('"').toString()
    }

    /** O que o app manda para a página quando ouviu um comando. */
    fun oJavascriptDoTexto(texto: String): String =
        "window.mesaFaladaVoz && window.mesaFaladaVoz.doAplicativo(" + aspas(texto.take(300)) + ");"

    /** O que o app manda quando não conseguiu ouvir (a página religa a voz e explica). */
    fun oJavascriptDaFalha(motivo: String): String =
        "window.mesaFaladaVoz && window.mesaFaladaVoz.falhouNoAplicativo(" + aspas(motivo) + ");"

    /**
     * O erro do reconhecedor do Android, em uma palavra que a página entende.
     *
     * Os números são os de `android.speech.SpeechRecognizer` (escritos aqui para
     * a bancada rodar sem Android): 3 áudio, 6 silêncio, 7 não casou, 8 ocupado,
     * 9 sem permissão.
     */
    fun motivoDoErro(codigo: Int): String = when (codigo) {
        6, 7 -> "nada"
        3, 8 -> "microfone"
        9 -> "permissao"
        else -> "erro"
    }
}
