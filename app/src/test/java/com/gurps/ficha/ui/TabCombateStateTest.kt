package com.gurps.ficha.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class TabCombateStateTest {

    @Test
    fun mensagemBloqueio_semPericia_semEscudo() {
        val mensagem = mensagemBloqueioPendente(
            temPericiaEscudo = false,
            temEscudoEquipado = false
        )
        assertEquals(
            "Sem Bloqueio: adicione perícia de Escudo na aba Perícias e equipe ao menos um escudo.",
            mensagem
        )
    }

    @Test
    fun mensagemBloqueio_semPericia_comEscudo() {
        val mensagem = mensagemBloqueioPendente(
            temPericiaEscudo = false,
            temEscudoEquipado = true
        )
        assertEquals(
            "Sem Bloqueio: falta perícia de Escudo na aba Perícias.",
            mensagem
        )
    }

    @Test
    fun mensagemBloqueio_comPericia_semEscudo() {
        val mensagem = mensagemBloqueioPendente(
            temPericiaEscudo = true,
            temEscudoEquipado = false
        )
        assertEquals(
            "Sem Bloqueio: equipe ao menos um escudo na aba Equipamentos.",
            mensagem
        )
    }

    @Test
    fun mensagemBloqueio_comPericia_comEscudo() {
        val mensagem = mensagemBloqueioPendente(
            temPericiaEscudo = true,
            temEscudoEquipado = true
        )
        assertEquals(null, mensagem)
    }
}
