package com.gurps.ficha.domain.saga

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote 354: 4 casos do validador de saída do Narrador (lei de ferro nº 1). */
class NarradorOutputValidatorTest {

    @Test
    fun `prosa pura sem numero mecanico passa`() {
        val r = NarradorOutputValidator.validar(
            "A taverna cheira a cerveja velha. O guarda encara você e pergunta o que faz aqui. O que você responde?",
            emptySet()
        )
        assertTrue(r.ok)
        assertNull(r.instrucaoCorrecao)
    }

    @Test
    fun `numero mecanico SEM tool no turno falha e pede correcao`() {
        val r = NarradorOutputValidator.validar(
            "Sua adaga acerta o flanco do lobo e causa 6 de dano; ele recua sangrando.",
            emptySet()
        )
        assertFalse(r.ok)
        assertNotNull(r.instrucaoCorrecao)
    }

    @Test
    fun `numero mecanico COM pedir_rolagem no turno passa`() {
        val r = NarradorOutputValidator.validar(
            "Você acerta com margem 3 — a flecha encontra o alvo.",
            setOf(NarradorTools.TOOL_PEDIR_ROLAGEM)
        )
        assertTrue(r.ok)
    }

    @Test
    fun `numeros de prosa nao-mecanicos nao disparam falso positivo`() {
        val r = NarradorOutputValidator.validar(
            "Três guardas bloqueiam a ponte. Faltam 2 horas para o anoitecer e 10 metros até o portão.",
            emptySet()
        )
        assertTrue(r.ok)
        assertNull(r.instrucaoCorrecao)
    }
}
