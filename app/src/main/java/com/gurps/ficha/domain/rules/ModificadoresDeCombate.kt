package com.gurps.ficha.domain.rules

/**
 * **Os modificadores condicionais de combate** — Lotes MB-1 e MB-4
 * (MB p.547-549).
 *
 * O livro fecha o capítulo de combate com duas listagens que são o resumo de
 * tudo: os modificadores de ataque **corpo a corpo** e os de ataque **à
 * distância**. É a página que fica aberta na mesa.
 *
 * ## ⚠️ O que este arquivo deliberadamente NÃO tem
 *
 * Metade da lista do livro **já está automatizada** em outro lugar, e repetir
 * aqui faria o jogador aplicar duas vezes o mesmo redutor — que é pior do que
 * não ter a regra:
 *
 * | Da lista do livro | Já vive em |
 * |---|---|
 * | Avançar e Atacar | `AvancarEAtacarRules` |
 * | Golpe Rápido | `GolpeRapidoEAparaRules` |
 * | Ataque com mão inábil | `MaoInabilRules` |
 * | Pontos de impacto e arma do oponente | `LocaisDeAtaque` |
 * | Visibilidade (escuridão) | `IluminacaoRules` |
 * | Distância e velocidade | `TabelaVelocidadeDistancia` |
 * | Modificador de Tamanho do alvo | `TamanhoDoAlvoRules` |
 *
 * Sobra o que está aqui: postura, situação do atacante e cobertura.
 *
 * ## 🔴 As duas travas que quase ninguém lembra
 *
 * São a razão principal de este lote existir — as duas somem numa soma feita de
 * cabeça, e as duas mudam o resultado:
 *
 * > Os modificadores são cumulativos, mas as **penalidades combinadas de
 * > visibilidade não podem exceder −10** (−6 se estiver acostumado à cegueira).
 *
 * > Se qualquer modificador marcado com um asterisco (\*) for aplicado, o **NH
 * > efetivo depois de todos os modificadores não pode exceder 9**.
 *
 * ⚠️ A segunda é **teto, não penalidade** — a mesma armadilha do Avançar e Atacar
 * e do combate em movimento. Um espadachim NH 20 que dá um Golpe Desenfreado vai
 * a **9**, não a 15.
 */
object ModificadoresDeCombate {

    /** O teto do NH quando qualquer modificador com asterisco entra (MB p.548). */
    const val TETO_DO_ASTERISCO = 9

    /** O piso das penalidades de visibilidade somadas. */
    const val LIMITE_VISIBILIDADE = -10

    /** O mesmo limite, para quem tem a vantagem Acostumado à Cegueira. */
    const val LIMITE_VISIBILIDADE_ACOSTUMADO = -6

    enum class Grupo(val titulo: String) {
        MANOBRA("Manobra do atacante"),
        POSICAO("Posição do atacante"),
        SITUACAO("Situação do atacante"),
        ALVO("Situação do alvo"),
        VISIBILIDADE("Visibilidade")
    }

    /**
     * Um modificador da lista.
     *
     * @param asterisco marca os que **derrubam o NH para 9** (MB p.548).
     * @param porUnidade quando o valor se repete: −4 **por pessoa** no caminho,
     *   −1 **por ponto** de ST que falta. Nesses o jogador informa a quantidade.
     */
    data class Modificador(
        val id: String,
        val rotulo: String,
        val valor: Int,
        val grupo: Grupo,
        val asterisco: Boolean = false,
        val porUnidade: Boolean = false,
        val explicacao: String? = null
    )

    /**
     * Corpo a corpo (MB p.548).
     *
     * ⚠️ **Ataque Total (Determinado) dá +4 aqui e +1 à distância.** Não é engano
     * de transcrição: são valores diferentes no livro, e trocar um pelo outro é o
     * erro mais fácil de cometer copiando a tabela.
     */
    val CORPO_A_CORPO: List<Modificador> = listOf(
        Modificador(
            "ataque_total_determinado", "Ataque Total (Determinado)", 4, Grupo.MANOBRA,
            explicacao = "Sem defesa ativa até o seu próximo turno."
        ),
        Modificador(
            "duas_armas_primaria", "Ataque com duas armas (mão boa)", -4, Grupo.MANOBRA
        ),
        Modificador(
            "duas_armas_inabil", "Ataque com duas armas (mão inábil)", -8, Grupo.MANOBRA,
            explicacao = "Vira −4 com Ambidestria."
        ),
        Modificador(
            "golpe_desenfreado", "Golpe Desenfreado", -5, Grupo.MANOBRA, asterisco = true
        ),
        Modificador(
            "combate_corporal", "Golpear em combate corporal", -2, Grupo.MANOBRA
        ),
        Modificador(
            "avaliar", "Avaliar (por turno, máximo 3)", 1, Grupo.MANOBRA, porUnidade = true,
            explicacao = "Cada turno gasto avaliando vale +1, até +3."
        ),
        Modificador(
            "agachado", "Agachado, ajoelhado ou sentado", -2, Grupo.POSICAO
        ),
        Modificador(
            "deitado", "Rastejando ou deitado", -4, Grupo.POSICAO,
            explicacao = "Rastejando, só dá para atacar com alcance C."
        ),
        Modificador("agarrado", "Agarrado", -4, Grupo.SITUACAO),
        Modificador(
            "choque", "Choque (dano do turno passado, máximo 4)", -1, Grupo.SITUACAO,
            porUnidade = true,
            explicacao = "Um por ponto de dano sofrido no último turno, até −4."
        ),
        Modificador("distracao_pequena", "Distração pequena", -2, Grupo.SITUACAO),
        Modificador(
            "distracao_grande", "Distração grande", -3, Grupo.SITUACAO,
            explicacao = "−3 ou pior, a critério do Mestre."
        ),
        Modificador(
            "piso_ruim", "Piso ruim", -2, Grupo.SITUACAO,
            explicacao = "−2 ou pior, a critério do Mestre."
        ),
        Modificador("escudo_grande", "Portando um escudo grande", -2, Grupo.SITUACAO),
        Modificador(
            "st_insuficiente", "ST abaixo do mínimo da arma (por ponto)", -1, Grupo.SITUACAO,
            porUnidade = true,
            explicacao = "Um ponto de penalidade para cada ponto de ST que falta."
        )
    )

    /**
     * À distância (MB p.549), incluindo a **cobertura** do MB-4.
     *
     * ⚠️ A cobertura do livro é uma **escolha**, não uma penalidade automática:
     * ou se atira sem penalidade e o ponto de impacto é sorteado (podendo bater
     * na cobertura), ou se mira num pedaço exposto e paga o −2. O painel oferece
     * o segundo caminho; o primeiro é decisão de mesa.
     */
    val A_DISTANCIA: List<Modificador> = listOf(
        Modificador(
            "ataque_total_determinado_dist", "Ataque Total (Determinado)", 1, Grupo.MANOBRA,
            explicacao = "⚠️ À distância vale +1, não o +4 do corpo a corpo."
        ),
        Modificador(
            "alvo_parcialmente_exposto", "Alvo parcialmente exposto", -2, Grupo.ALVO,
            explicacao = "Mirar num pedaço que aparece por trás da cobertura."
        ),
        Modificador(
            "cobertura_leve", "Disparo através de cobertura leve", -2, Grupo.ALVO,
            explicacao = "Mato alto, cortina de fumaça, vidro."
        ),
        Modificador(
            "pessoa_no_caminho", "Pessoa no caminho (por pessoa)", -4, Grupo.ALVO,
            porUnidade = true
        ),
        Modificador(
            "alvo_abaixado", "Alvo agachado, ajoelhado, sentado ou deitado", -2, Grupo.ALVO,
            explicacao = "Só para acertar tronco, virilha ou pernas."
        )
    )

    /** Uma escolha do jogador: qual modificador e, quando repete, quantas vezes. */
    data class Escolha(val modificador: Modificador, val quantidade: Int = 1) {
        /** Já com o teto do próprio modificador (Avaliar para em +3, Choque em −4). */
        val total: Int
            get() {
                val bruto = modificador.valor * quantidade.coerceAtLeast(1)
                return when (modificador.id) {
                    "avaliar" -> bruto.coerceAtMost(3)
                    "choque" -> bruto.coerceAtLeast(-4)
                    else -> bruto
                }
            }
    }

    /** O que a conta devolve, com as travas já aplicadas e explicadas. */
    data class Resultado(
        val nhFinal: Int,
        val somaDosModificadores: Int,
        val tetoDoAsteriscoAplicado: Boolean,
        val limiteDeVisibilidadeAplicado: Boolean
    )

    /**
     * Aplica a lista inteira sobre um NH.
     *
     * A ordem importa e é a do livro: soma tudo, **depois** trava a visibilidade,
     * **depois** aplica o teto do asterisco. Trocar a ordem muda o resultado.
     *
     * @param penalidadeDeVisibilidadeJaAplicada o que a luz da cena
     *   (`IluminacaoRules`) já descontou. Entra na conta do limite de −10 —
     *   senão o app permitiria −10 de luz **mais** −10 de fumaça.
     */
    fun aplicar(
        nhBase: Int,
        escolhas: List<Escolha>,
        penalidadeDeVisibilidadeJaAplicada: Int = 0,
        acostumadoACegueira: Boolean = false
    ): Resultado {
        val deVisibilidade = escolhas
            .filter { it.modificador.grupo == Grupo.VISIBILIDADE }
            .sumOf { it.total } + penalidadeDeVisibilidadeJaAplicada
        val limite = if (acostumadoACegueira) LIMITE_VISIBILIDADE_ACOSTUMADO else LIMITE_VISIBILIDADE
        val visibilidadeTravada = deVisibilidade.coerceAtLeast(limite)
        val travouVisibilidade = deVisibilidade < limite

        val outros = escolhas
            .filter { it.modificador.grupo != Grupo.VISIBILIDADE }
            .sumOf { it.total }

        // A parcela de visibilidade já aplicada fora não entra de novo na soma:
        // ela já está dentro do `nhBase` que chegou aqui.
        val soma = outros + (visibilidadeTravada - penalidadeDeVisibilidadeJaAplicada)
        val comModificadores = nhBase + soma

        val temAsterisco = escolhas.any { it.modificador.asterisco }
        val nhFinal = if (temAsterisco) {
            comModificadores.coerceAtMost(TETO_DO_ASTERISCO)
        } else {
            comModificadores
        }

        return Resultado(
            nhFinal = nhFinal,
            somaDosModificadores = soma,
            tetoDoAsteriscoAplicado = temAsterisco && comModificadores > TETO_DO_ASTERISCO,
            limiteDeVisibilidadeAplicado = travouVisibilidade
        )
    }

    /** O aviso do teto, quando ele de fato cortou. Silêncio quando não cortou. */
    fun avisoDoTeto(r: Resultado): String? = when {
        r.tetoDoAsteriscoAplicado ->
            "⚠️ Teto de $TETO_DO_ASTERISCO: um modificador com asterisco (*) foi " +
                "aplicado, e o livro trava o NH aí (MB p.548)."
        else -> null
    }

    fun avisoDaVisibilidade(r: Resultado, acostumado: Boolean): String? = when {
        r.limiteDeVisibilidadeAplicado ->
            "⚠️ As penalidades de visibilidade somadas param em " +
                "${if (acostumado) LIMITE_VISIBILIDADE_ACOSTUMADO else LIMITE_VISIBILIDADE} " +
                "(MB p.548)."
        else -> null
    }
}
