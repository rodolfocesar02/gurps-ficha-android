package com.gurps.ficha.ui.features.traits

import com.gurps.ficha.model.ModificadorDefinicao
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Cobre [classificarModificador] — a regra que decide se um modificador cai no
 * botão "Ampliações" ou "Limitações". Se ela errar, o item some do botão certo
 * e aparece no errado (ou some dos dois). Os catálogos têm dado torto real
 * (um "limitation" em inglês), então a cascata precisa aguentar isso.
 */
class ClassificarModificadorTest {

    private fun mod(tipo: String = "", valor: String = "0") =
        ModificadorDefinicao(id = "x", nome = "X", tipo = tipo, valor = valor)

    @Test
    fun `tipo ampliacao vira AMPLIACAO`() {
        assertEquals(TipoModificador.AMPLIACAO, classificarModificador(mod(tipo = "ampliação")))
    }

    @Test
    fun `tipo limitacao vira LIMITACAO`() {
        assertEquals(TipoModificador.LIMITACAO, classificarModificador(mod(tipo = "limitação")))
    }

    @Test
    fun `outlier limitation em ingles ainda cai em LIMITACAO`() {
        // mod_gatilho_30 no catálogo real. Sem a regra startsWith, sumiria.
        assertEquals(TipoModificador.LIMITACAO, classificarModificador(mod(tipo = "limitation", valor = "-30%")))
    }

    @Test
    fun `tipo com espacos e maiusculas e normalizado`() {
        assertEquals(TipoModificador.AMPLIACAO, classificarModificador(mod(tipo = "  Ampliação  ")))
        assertEquals(TipoModificador.LIMITACAO, classificarModificador(mod(tipo = "LIMITAÇÃO")))
    }

    @Test
    fun `sem tipo cai pelo sinal do valor`() {
        assertEquals(TipoModificador.LIMITACAO, classificarModificador(mod(tipo = "", valor = "-20%")))
        assertEquals(TipoModificador.AMPLIACAO, classificarModificador(mod(tipo = "", valor = "+50%")))
        assertEquals(TipoModificador.AMPLIACAO, classificarModificador(mod(tipo = "", valor = "100%")))
    }

    @Test
    fun `sem tipo e valor sem numero assume AMPLIACAO`() {
        // Fallback do fallback: nada de útil. Não some da UI — vai para amp.
        assertEquals(TipoModificador.AMPLIACAO, classificarModificador(mod(tipo = "", valor = "Variável")))
    }

    @Test
    fun `tipo sempre vence o sinal do valor`() {
        // Ampliação de valor negativo (ex.: acompanhamento com nível 0) continua ampliação.
        assertEquals(TipoModificador.AMPLIACAO, classificarModificador(mod(tipo = "ampliação", valor = "-10%")))
    }
}
