package com.gurps.ficha.domain.rules

object CombatRules {

    fun calcularEsquiva(esquivaBase: Int, nivelCarga: Int, bonusEscudo: Int, bonusManual: Int): Int {
        return (esquivaBase - nivelCarga + bonusEscudo + bonusManual).coerceAtLeast(1)
    }

    fun calcularEsquivaBase(esquivaBase: Int, nivelCarga: Int): Int {
        return (esquivaBase - nivelCarga).coerceAtLeast(1)
    }

    fun calcularAparaBase(nh: Int): Int {
        return (nh / 2) + 3
    }

    fun calcularApara(nh: Int, bonusEscudo: Int, bonusManual: Int): Int {
        return calcularAparaBase(nh) + bonusEscudo + bonusManual
    }

    fun calcularBloqueioBase(nh: Int): Int {
        return (nh / 2) + 3
    }

    fun calcularBloqueio(nh: Int, bonusEscudo: Int, bonusManual: Int): Int {
        return calcularBloqueioBase(nh) + bonusEscudo + bonusManual
    }
}
