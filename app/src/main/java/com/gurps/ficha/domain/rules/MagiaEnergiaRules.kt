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

    fun parseCusto(energiaStr: String?): Int? {
        if (energiaStr.isNullOrBlank()) return null
        val cleans = energiaStr.replace("ponto ", "").replace("pontos", "").trim()
        val fixed = cleans.split(' ')[0].split('/')[0].split('-')[0].trim()
        return fixed.toIntOrNull()
    }
}
