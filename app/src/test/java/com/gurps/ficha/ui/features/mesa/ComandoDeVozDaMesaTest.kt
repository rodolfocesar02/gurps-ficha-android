package com.gurps.ficha.ui.features.mesa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **O comando de voz da Mesa, dentro do app** — MF-9.
 *
 * 🔴 O que a pessoa disse vira uma linha de JavaScript. Se as aspas vazarem, a
 * linha quebra -- ou o que foi dito vira código. A costura com a página (os
 * nomes `doAplicativo` e `falhouNoAplicativo`) é conferida do lado da Mesa, em
 * `mesa-virtual/server/test/mesa-falada-no-app.test.js`.
 */
class ComandoDeVozDaMesaTest {

    @Test
    fun `a frase dele chega inteira`() {
        assertEquals(
            "window.mesaFaladaVoz && window.mesaFaladaVoz.doAplicativo(\"noroeste, três hexágonos\");",
            ComandoDeVozDaMesa.oJavascriptDoTexto("noroeste, três hexágonos")
        )
    }

    @Test
    fun `🟥 aspas, barras e quebras de linha nao fecham a string`() {
        val js = ComandoDeVozDaMesa.oJavascriptDoTexto("a\" + alert(1) + \"b\\\n</script>")
        // Dentro dos parênteses, só UMA string: nenhuma aspa sem barra antes.
        val miolo = js.substringAfter("doAplicativo(").substringBeforeLast(");")
        assertTrue(miolo.startsWith("\"") && miolo.endsWith("\""))
        val semEscapes = miolo.substring(1, miolo.length - 1).replace("\\\\", "").replace("\\\"", "")
        assertFalse("sobrou uma aspa solta: $miolo", semEscapes.contains("\""))
        assertFalse("quebra de linha crua", miolo.contains("\n"))
        assertFalse("o fechamento de script passou cru", miolo.contains("</"))
    }

    @Test
    fun `os separadores de linha do JavaScript tambem sao escapados`() {
        val miolo = ComandoDeVozDaMesa.aspas("a b c")
        assertEquals("\"a\\u2028b\\u2029c\"", miolo)
    }

    @Test
    fun `os erros do reconhecedor viram uma palavra que a pagina entende`() {
        assertEquals("nada", ComandoDeVozDaMesa.motivoDoErro(7))
        assertEquals("nada", ComandoDeVozDaMesa.motivoDoErro(6))
        assertEquals("microfone", ComandoDeVozDaMesa.motivoDoErro(8))
        assertEquals("microfone", ComandoDeVozDaMesa.motivoDoErro(3))
        assertEquals("permissao", ComandoDeVozDaMesa.motivoDoErro(9))
        assertEquals("erro", ComandoDeVozDaMesa.motivoDoErro(42))
    }

    @Test
    fun `texto comprido e cortado antes de ir para a pagina`() {
        val js = ComandoDeVozDaMesa.oJavascriptDoTexto("x".repeat(1000))
        assertTrue(js.length < 400)
    }
}
