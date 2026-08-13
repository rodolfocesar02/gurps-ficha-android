package com.gurps.ficha.domain.rules.poderes

/**
 * **Reserva de Energia (RE)** — GURPS Poderes, p.119. Lote POD-9.
 *
 * > *"Compre Pontos de Fadiga pelo preço costumeiro de **3 pontos cada**, mas
 * > trate-os como uma nova vantagem, 'Reserva de Energia' (RE). Ela sempre está
 * > ligada a uma fonte de poder particular; por exemplo, 10 PF para poderes
 * > psíquicos será 'RE 10 (Psíquico) [30]'."*
 *
 * ## 🔴 A recarga padrão não é a única regra
 *
 * O plano registrou *"recarrega 1 ponto a cada 10 minutos"* como **a** regra.
 * É só o **padrão**: cinco limitações mudam a recarga **e** o preço, e são elas
 * que fazem a RE valer a pena modelar. Achadas só na 2ª revisão, porque a minha
 * leitura da seção tinha parado antes delas.
 *
 * ## O que a RE **não** é
 *
 * Não é PF. *"Esgotar uma RE não causa nenhum dos efeitos de ficar abaixo de 1/3
 * PF; e ter a RE no valor total não protege contra esses efeitos."* Ataque por
 * Fadiga, falta de sono e esforço adicional comum **não** a gastam.
 */
object ReservaDeEnergia {

    /** *"pelo preço costumeiro de 3 pontos cada"* (p.119). */
    const val CUSTO_POR_PONTO = 3

    /** O padrão: *"se repõe a um ponto a cada 10 minutos, independente do repouso"*. */
    const val MINUTOS_POR_PONTO_PADRAO = 10

    /**
     * *"Os poderes ainda podem recorrer aos PF normais; se não puderem, adicione
     * −5% ao modificador de poder."*
     */
    const val MODIFICADOR_SE_NAO_USA_PF_NORMAL = -5

    /**
     * As cinco limitações da RE (p.119).
     *
     * ⚠️ Duas delas **mudam a recarga**, e é por isso que a RE não pode ser só um
     * número na ficha: Carga Especial não recarrega com o tempo, e Carga Lenta
     * troca os 10 minutos por uma hora ou um dia.
     */
    enum class Limitacao(
        val rotulo: String,
        val valor: Int,
        val minutosPorPonto: Int?,          // null = não recarrega com o tempo
        val explicacao: String
    ) {
        CARGA_ESPECIAL(
            "Carga Especial", -70, null,
            "Não recarrega com o tempo: só por RD com Absorção, Sanguessuga, a mágica " +
                "Roubar Energia e afins."
        ),
        CARGA_ESPECIAL_PERDENDO(
            "Carga Especial (perde 1/segundo)", -80, null,
            "Além de não recarregar, a energia se perde a um ponto por segundo, " +
                "forçando o usuário a gastá-la rapidamente."
        ),
        CARGA_LENTA_HORA("Carga Lenta (1/hora)", -20, 60, "Recarrega um ponto por hora."),
        CARGA_LENTA_DIA("Carga Lenta (1/dia)", -60, 1440, "Recarrega um ponto por dia."),
        PODER_UNICO(
            "Poder Único", -50, MINUTOS_POR_PONTO_PADRAO,
            "Disponível apenas para quem tem dois ou mais poderes da fonte; a RE " +
                "serve a apenas um deles."
        ),
        SOMENTE_HABILIDADES(
            "Somente Habilidades", -10, MINUTOS_POR_PONTO_PADRAO,
            "Paga apenas os custos de PF básicos das habilidades. Não serve para " +
                "esforço adicional nem para proezas."
        ),
        SOMENTE_PROEZAS(
            "Somente Proezas", -10, MINUTOS_POR_PONTO_PADRAO,
            "Só é útil para esforço adicional e proezas. Não cobre o custo de PF do " +
                "uso normal da habilidade."
        );
    }

    /**
     * ⚠️ Pares que o livro declara **incompatíveis**. Sem esta trava o jogador
     * somaria −70% e −60% e compraria uma RE quase de graça que, no livro, não
     * pode existir.
     */
    private val INCOMPATIVEIS: List<Set<Limitacao>> = listOf(
        setOf(Limitacao.CARGA_ESPECIAL, Limitacao.CARGA_LENTA_HORA),
        setOf(Limitacao.CARGA_ESPECIAL, Limitacao.CARGA_LENTA_DIA),
        setOf(Limitacao.CARGA_ESPECIAL_PERDENDO, Limitacao.CARGA_LENTA_HORA),
        setOf(Limitacao.CARGA_ESPECIAL_PERDENDO, Limitacao.CARGA_LENTA_DIA),
        setOf(Limitacao.CARGA_ESPECIAL, Limitacao.CARGA_ESPECIAL_PERDENDO),
        setOf(Limitacao.CARGA_LENTA_HORA, Limitacao.CARGA_LENTA_DIA),
        setOf(Limitacao.SOMENTE_HABILIDADES, Limitacao.SOMENTE_PROEZAS)
    )

    fun conflitos(escolhidas: Set<Limitacao>): List<Set<Limitacao>> =
        INCOMPATIVEIS.filter { escolhidas.containsAll(it) }

    fun ehCombinacaoValida(escolhidas: Set<Limitacao>): Boolean = conflitos(escolhidas).isEmpty()

    /**
     * O custo da RE: pontos × 3, com o total das limitações aplicado.
     *
     * ⚠️ Usa o mesmo teto de −80% do resto do livro
     * ([RegrasDePoder.PIOR_MODIFICADOR_TOTAL]) — a Carga Especial sozinha já é
     * −70%, e somar mais uma passaria disso.
     */
    fun custo(pontosDeRE: Int, limitacoes: Set<Limitacao> = emptySet()): Int {
        if (pontosDeRE <= 0) return 0
        val base = pontosDeRE * CUSTO_POR_PONTO
        val total = RegrasDePoder.limitarModificadorTotal(limitacoes.sumOf { it.valor })
        // 🔴 Conta INTEIRA, e não com `Double`. Com ponto flutuante,
        // `1.0 - 0.70` dá `0.30000000000000004`; 30 × isso vira `9.000000000000002`
        // e o arredondamento para cima devolve **10** em vez de 9. O erro é de
        // um ponto e só aparece em alguns valores — o teste pegou.
        val bruto = base.toLong() * (100 + total)
        return Math.floorDiv(bruto + 99, 100L).toInt().coerceAtLeast(1)
    }

    /** Quantos minutos para recuperar um ponto, dadas as limitações escolhidas. */
    fun minutosPorPonto(limitacoes: Set<Limitacao> = emptySet()): Int? {
        if (limitacoes.any { it.minutosPorPonto == null }) return null
        return limitacoes.mapNotNull { it.minutosPorPonto }.maxOrNull() ?: MINUTOS_POR_PONTO_PADRAO
    }

    /** Quanto a RE recupera num intervalo — nada, se ela não recarrega com o tempo. */
    fun recuperadoEm(minutos: Int, limitacoes: Set<Limitacao> = emptySet()): Int {
        val porPonto = minutosPorPonto(limitacoes) ?: return 0
        if (minutos <= 0) return 0
        return minutos / porPonto
    }

    /**
     * 🔴 A RE **só** abastece habilidades da mesma fonte.
     * *"Uma RE pode abastecer somente habilidades da mesma fonte."*
     */
    fun podeAbastecer(fonteDaRE: String?, fonteDoPoder: String?): Boolean {
        val a = RegrasDePoder.normalizarFonte(fonteDaRE)
        val b = RegrasDePoder.normalizarFonte(fonteDoPoder)
        return a != null && a == b
    }

    /**
     * ⚠️ Esgotar a RE **não** dispara os efeitos de PF baixo, e tê-la cheia
     * **não** protege deles. São dois medidores separados, e confundi-los seria
     * o erro mais fácil de cometer aqui.
     */
    const val ESGOTAR_CAUSA_EFEITOS_DE_PF_BAIXO = false

    fun rotulo(pontos: Int, fonte: String): String = "RE $pontos ($fonte) [${custo(pontos)}]"
}
