package com.gurps.ficha.domain.roll

import kotlin.math.max

/**
 * **A conta do teste de habilidade**, pelo Módulo Básico (pp.343, 345 e 347) — PE-2 passo 0.
 *
 * O `TabRolagem` e o rolador avulso tinham cada um a sua conta de "passou ou não" dentro da tela, e as duas erravam:
 *
 * | caso | o livro | o app fazia |
 * |---|---|---|
 * | tirou 17 com NH efetivo 17+ | **sempre fracasso** (p.343) | "Sucesso" |
 * | tirou 4 com NH efetivo 3 | sempre sucesso, margem 0 | texto certo, margem −1 no Discord e na Mesa |
 * | NH efetivo abaixo de 3 | não pode tentar, exceto defesa (p.345) | rolava contra 2, ou contra número negativo |
 *
 * Não há teto de 18 no NH efetivo (a margem é a distância até ele, p.347); o app já estava certo nisso e continua.
 *
 * 🔴 O par dela na Mesa é `src/campo/ficha-rolagem.js` (`resultadoDe`, `podeTentar`). Os dois testes (`ContaDoTesteTest.kt`
 * aqui e `pe2-conta-do-teste.test.js` lá) usam os MESMOS números: é isso que costura os dois lados.
 */
object ContaDoTeste {
    /** 3 e 4 sempre passam; 17 e 18 sempre falham; no resto, soma ≤ NH efetivo (igual ao NH é sucesso). */
    fun passou(soma: Int, nhEfetivo: Int): Boolean = soma <= 4 || (soma < 17 && soma <= nhEfetivo)

    /**
     * A margem com sinal que o app sempre mandou: positiva no sucesso, negativa no fracasso. Num 3 ou 4 com NH menor, ou
     * num 17 com NH maior, o livro não diz quanto: vale 0.
     */
    fun margemAssinada(soma: Int, nhEfetivo: Int): Int =
        if (passou(soma, nhEfetivo)) max(0, nhEfetivo - soma) else -max(0, soma - nhEfetivo)

    /** NH efetivo abaixo de 3 não pode tentar, exceto defesa (p.345). */
    fun podeTentar(nhEfetivo: Int, ehDefesa: Boolean): Boolean = ehDefesa || nhEfetivo >= 3

    /** Decisivo e falha crítica ficam como estavam: `CriticoRules`, que já era a regra do livro (pp.347-348). */
    fun critico(soma: Int, nhEfetivo: Int?): CriticoRules.ResultadoCritico = CriticoRules.classificar(soma, nhEfetivo)
}
