package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Cobre [MensagensDefesa.bloqueioPendente] — as quatro combinações de
 * perícia de Escudo x escudo equipado.
 *
 * Migrado de `ui/TabCombateStateTest.kt` quando a tela `TabCombate` foi
 * removida (não era renderizada por ninguém). A regra e os testes foram
 * preservados; só mudaram de casa.
 */
class MensagensDefesaTest {

    @Test
    fun `sem pericia e sem escudo pede as duas coisas`() {
        assertEquals(
            "Sem Bloqueio: adicione perícia de Escudo na aba Perícias e equipe ao menos um escudo.",
            MensagensDefesa.bloqueioPendente(
                temPericiaEscudo = false,
                temEscudoEquipado = false
            )
        )
    }

    @Test
    fun `sem pericia mas com escudo pede so a pericia`() {
        assertEquals(
            "Sem Bloqueio: falta perícia de Escudo na aba Perícias.",
            MensagensDefesa.bloqueioPendente(
                temPericiaEscudo = false,
                temEscudoEquipado = true
            )
        )
    }

    @Test
    fun `com pericia mas sem escudo pede so o escudo`() {
        assertEquals(
            "Sem Bloqueio: equipe ao menos um escudo na aba Equipamentos.",
            MensagensDefesa.bloqueioPendente(
                temPericiaEscudo = true,
                temEscudoEquipado = false
            )
        )
    }

    @Test
    fun `com pericia e com escudo nao ha pendencia`() {
        assertNull(
            MensagensDefesa.bloqueioPendente(
                temPericiaEscudo = true,
                temEscudoEquipado = true
            )
        )
    }
}
