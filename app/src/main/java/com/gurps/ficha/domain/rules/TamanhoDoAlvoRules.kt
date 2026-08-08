package com.gurps.ficha.domain.rules

/**
 * **Modificador de Tamanho do alvo** — Lote MB-3 (MB p.549 e p.550).
 *
 * O livro numera os passos do ataque à distância, e o **segundo** é este:
 *
 * > 1. Verifique o NH básico com a arma de combate à distância.
 * > **2. Aplique o Modificador de Tamanho (MT) do alvo.**
 * > 3. Aplique os modificadores relativos à distância e velocidade do alvo.
 * > 4. Aplique os modificadores condicionais.
 *
 * ⚠️ **O app pulava o passo 2.** Fazia o 1, o 3 e parte do 4 — e o alvo do
 * tamanho de um cavalo era tão difícil de acertar quanto um rato.
 *
 * ## Por que é um seletor e não um cálculo
 *
 * O MT é do **alvo**, e o alvo é do Mestre. A ficha guarda o MT do *personagem*
 * (`modificadorTamanho`, vindo da raça), que serve para quando **ele** é o alvo —
 * não para quando ele atira. Só o jogador pode informar em que está mirando.
 *
 * ⚠️ **O sinal é o que confunde.** MT **positivo** é alvo **grande**, e alvo
 * grande é **mais fácil** de acertar — então o MT entra **somando** no NH. Um MT
 * −4 (um rato) é −4 no ataque. É contraintuitivo porque em quase todo o resto do
 * GURPS um número negativo é penalidade *sua*; aqui ele descreve o alvo.
 */
object TamanhoDoAlvoRules {

    /**
     * Um degrau do seletor, com um exemplo do livro.
     *
     * O exemplo não é enfeite: "MT −2" não diz nada a quem não decorou a tabela,
     * e "do tamanho de um cachorro" diz na hora.
     */
    data class Degrau(val mt: Int, val exemplo: String) {
        val rotulo: String get() = if (mt > 0) "+$mt · $exemplo" else "$mt · $exemplo"
    }

    /**
     * Os degraus que aparecem na tela.
     *
     * ⚠️ **Não é a tabela inteira do livro** (que vai de −15 a +10 e mistura
     * altura com comprimento). São as faixas que aparecem numa mesa de verdade,
     * com o exemplo mais reconhecível de cada uma. Quem precisar de um MT fora
     * disso digita — o Mestre manda no número.
     */
    val DEGRAUS: List<Degrau> = listOf(
        Degrau(-6, "inseto grande, moeda"),
        Degrau(-5, "rato pequeno"),
        Degrau(-4, "gato, rato grande"),
        Degrau(-3, "cachorro pequeno"),
        Degrau(-2, "criança, cachorro médio"),
        Degrau(-1, "pessoa baixa, lobo"),
        Degrau(0, "humano adulto"),
        Degrau(1, "cavalo, humano muito grande"),
        Degrau(2, "carro, boi"),
        Degrau(3, "van, elefante"),
        Degrau(4, "caminhão pequeno"),
        Degrau(5, "caminhão grande"),
        Degrau(6, "ônibus, casa pequena")
    )

    /** O índice do humano (MT 0) — o padrão com que o seletor abre. */
    val INDICE_PADRAO: Int = DEGRAUS.indexOfFirst { it.mt == 0 }

    fun degrau(indice: Int): Degrau = DEGRAUS[indice.coerceIn(0, DEGRAUS.lastIndex)]

    fun indiceDoMt(mt: Int): Int =
        DEGRAUS.indexOfFirst { it.mt == mt }.takeIf { it >= 0 } ?: INDICE_PADRAO

    /**
     * O modificador que entra no NH.
     *
     * É o próprio MT, **com o sinal dele**: alvo grande soma, alvo pequeno
     * subtrai. Existe como função — em vez de o chamador ler `degrau.mt` — para
     * o dia em que uma vantagem mexer nisso; aí muda num lugar só.
     */
    fun modificadorNoAtaque(mt: Int): Int = mt

    /**
     * O rótulo da linha, já explicando o sinal.
     *
     * ⚠️ Sem a explicação, um "+2" ao lado de "Tamanho do alvo" parece bônus que
     * o personagem tem. Ele não é: é o alvo que é grande.
     */
    fun rotulo(mt: Int, exemplo: String): String = when {
        mt > 0 -> "Alvo grande ($exemplo): +$mt — mais fácil de acertar"
        mt < 0 -> "Alvo pequeno ($exemplo): $mt — mais difícil de acertar"
        else -> "Alvo do tamanho de um humano: sem modificador"
    }

    fun rotuloAcessivel(mt: Int, exemplo: String): String {
        val comoLer = when {
            mt > 0 -> "mais $mt"
            mt < 0 -> "menos ${-mt}"
            else -> "zero"
        }
        return "Tamanho do alvo: $exemplo. Modificador $comoLer no ataque. " +
            "Toque nos botões para mudar o tamanho do alvo."
    }
}
