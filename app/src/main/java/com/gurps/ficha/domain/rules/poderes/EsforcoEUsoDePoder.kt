package com.gurps.ficha.domain.rules.poderes

import kotlin.math.ceil

/**
 * **Esforço adicional e o custo em PF de usar um poder** — GURPS Poderes,
 * p.159-161. Lote POD-12.
 *
 * ## 🔴 O plano descrevia a troca errada
 *
 * Estava escrito: *"esforço adicional: 1 PF = +15% de efeito"*. Isso é o exemplo
 * de uma **variante cinematográfica opcional** ("Esforço Adicional Divino",
 * p.161), que multiplica pelos PF gastos e **ignora o teto**.
 *
 * A regra base **não troca PF por efeito** — troca **penalidade em Vontade**:
 *
 * > *"Para usar um esforço adicional, faça um teste de Vontade com redutor de
 * > **−1 por aumento de 5% no efeito**, ou fração; por exemplo, Atribulação 9
 * > representa 12,5% mais efeito do que a Atribulação 8, por isso exigiria uma
 * > rolagem com **−3**. O bônus máximo para o efeito é de **100%**, com redutor
 * > **−20** para Vontade."*
 */
object EsforcoAdicional {

    const val PERCENTUAL_POR_PENALIDADE = 5
    const val EFEITO_MAXIMO = 100
    const val PENALIDADE_MAXIMA = -20

    /** *"+5 nas situações descritas em Apenas em Emergências (pág. 100)"*. */
    const val BONUS_APENAS_EM_EMERGENCIAS = 5

    /**
     * A penalidade de Vontade para um aumento de efeito.
     *
     * ⚠️ *"ou fração"* — 12,5% não são dois degraus e meio, são **três**. Por isso
     * arredonda para cima, e é o número que o próprio livro usa no exemplo.
     */
    fun penalidade(percentualDeAumento: Int): Int {
        if (percentualDeAumento <= 0) return 0
        val efetivo = percentualDeAumento.coerceAtMost(EFEITO_MAXIMO)
        return -ceil(efetivo.toDouble() / PERCENTUAL_POR_PENALIDADE).toInt()
    }

    /** O aumento pedido passa do que o livro permite? */
    fun passouDoTeto(percentualDeAumento: Int): Boolean = percentualDeAumento > EFEITO_MAXIMO

    fun avisoDoTeto(percentualDeAumento: Int): String? =
        if (passouDoTeto(percentualDeAumento))
            "O bônus máximo de efeito é $EFEITO_MAXIMO%, com redutor $PENALIDADE_MAXIMA " +
                "na Vontade (p.160)."
        else null

    /**
     * ⚠️ Só vale para habilidade **ativa que exija teste para ativar** ou Disputa
     * Rápida. Habilidade passiva não usa; e habilidade que pede **teste de
     * ataque** também não — para ataque o equivalente é o Ataque Total
     * (Determinado).
     */
    fun podeUsarEsforcoAdicional(exigeTesteDeAtivacao: Boolean, exigeTesteDeAtaque: Boolean): Boolean =
        exigeTesteDeAtivacao && !exigeTesteDeAtaque
}

/**
 * **O custo em PF de usar uma habilidade** — GURPS Poderes, p.159. Lote POD-12.
 *
 * ⚠️ O livro é explícito sobre por que esses números são baixos: *"é por isso que
 * poucas habilidades custam PF (…) mesmo 1 PF por uso cansaria todos em segundos,
 * exceto os mais em forma"*.
 */
object CustoEmPfDoUso {

    enum class Intensidade(val rotulo: String, val minutosPorPf: Int, val explicacao: String) {
        INTENSIVO(
            "Uso intensivo", 1,
            "Testes a cada um ou dois segundos: 1 PF por minuto. Ex.: um psi lendo a " +
                "mente de todos que descem de um avião."
        ),
        PROLONGADO(
            "Uso prolongado", 60,
            "Baixa intensidade, mas relativamente constante: 1 PF por hora. Ex.: uma " +
                "missão inteira alternando Super Salto, Invisibilidade e Telecinese."
        ),
        CONTINUO(
            "Habilidade contínua", 1,
            "Teste uma vez por minuto de uso contínuo, e 1 PF por teste."
        )
    }

    fun pfGastos(intensidade: Intensidade, minutos: Int): Int {
        if (minutos <= 0) return 0
        return minutos / intensidade.minutosPorPf
    }
}
