package com.gurps.ficha.domain.rules

object MagiaEnergiaRules {

    fun reducaoPorNh(nhBasico: Int): Int {
        return when {
            nhBasico >= 20 -> 2 + ((nhBasico - 20) / 5)
            nhBasico >= 15 -> 1
            else -> 0
        }
    }

    fun custoAjustadoPorNh(custoBase: Int, nhBasico: Int): Int {
        val reduzido = custoBase - reducaoPorNh(nhBasico)
        return reduzido.coerceAtLeast(0)
    }
}
