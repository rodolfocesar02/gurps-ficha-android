package com.gurps.ficha.domain.rules.poderes

/**
 * **Montar um modificador de poder por componentes** — GURPS Poderes, p.20-26.
 * Lote POD-7.
 *
 * > *"Em todos os casos, o valor final do modificador de poder será definido ao
 * > **somar** as limitações e ampliações individuais que ele envolve."* (p.20)
 *
 * Serve para quem cria poder **personalizado**. Quem usa um dos 11 prontos
 * (p.26-30) não precisa disto — o valor já vem fechado.
 *
 * ## 🔴 Refeito no POD-16 contra a Referência Rápida (p.25)
 *
 * A 1ª versão fez **Contramedidas como escolha única** — o livro diz que elas
 * **somam**. Faltava **Volúvel (−20%)**, o **Cósmico** estava no grupo errado, e
 * a desvantagem exigida virou uma lista de opções minhas em vez do **valor em
 * pontos** que o livro manda usar.
 */
object MontadorDeModificador {

    /**
     * ⚠️ `escolhaUnica` vem da **Referência Rápida** (p.25), que marca
     * *"(escolha um)"* em alguns grupos e diz, no cabeçalho:
     * *"a menos que seja indicado o contrário, todos os modificadores serão
     * **cumulativos**"*.
     *
     * 🔴 A 1ª versão fez **Contramedidas como escolha única**. O livro não marca
     * esse grupo — as três somam. E foi para tapar esse buraco que eu inventei
     * uma linha "Antipoderes −10% (as duas situações)", que nada mais é do que a
     * soma de dois −5% que eu mesmo tinha proibido de coexistir.
     */
    enum class Grupo(val rotulo: String, val escolhaUnica: Boolean) {
        CONTRAMEDIDAS("Contramedidas (somam entre si)", false),
        DESVANTAGEM_TRACO("Desvantagem exigida — o traço", true),
        ENERGIAS_CANALIZADAS("Energias canalizadas (escolha uma)", true),
        DESVANTAGEM_ESVAI("Desvantagem exigida — o poder desaparece (escolha uma)", true),
        DESVANTAGEM_RESTAURA("Desvantagem exigida — a restauração exige (escolha uma)", true),
        EXTRAS("Outros inconvenientes (somam entre si)", false)
    }

    data class Componente(
        val grupo: Grupo,
        val rotulo: String,
        val valor: Int,
        val pagina: Int,
        val explicacao: String = ""
    )

    /**
     * A **Referência Rápida** do livro (p.25), na ordem dela.
     *
     * ⚠️ O **Poder Cósmico (+50%)** aparece em *Energias Canalizadas*, com a nota
     * de rodapé: *"poderes cósmicos não podem ser bloqueados nem possuem
     * contramedidas"*. Escolhê-lo **zera** o grupo de contramedidas — ver
     * [conflitosDeGrupo].
     */
    val CATALOGO: List<Componente> = listOf(
        // ── Energias Canalizadas (p.24-25) — escolha UMA ──
        Componente(Grupo.ENERGIAS_CANALIZADAS, "Não é bloqueável de forma nenhuma", 0, 24),
        Componente(Grupo.ENERGIAS_CANALIZADAS, "Bloqueável por item ou condição exótica/sobrenatural", -5, 24),
        Componente(Grupo.ENERGIAS_CANALIZADAS, "Bloqueável por item ou condição mundana", -10, 24),
        Componente(Grupo.ENERGIAS_CANALIZADAS, "Volúvel", -20, 24,
            "A energia é, ou parece ser, senciente, e às vezes reage mal."),
        Componente(Grupo.ENERGIAS_CANALIZADAS, "Poder Cósmico", 50, 21,
            "Não pode ser bloqueado e não possui contramedidas."),

        // ── Contramedidas (p.20-21) — CUMULATIVAS ──
        Componente(Grupo.CONTRAMEDIDAS, "Contramedidas mundanas", -10, 20,
            "Ambiente comum na natureza derruba o poder, e o inimigo explora isso " +
                "com objetos e conhecimentos cotidianos."),
        Componente(Grupo.CONTRAMEDIDAS, "Vantagens ou perícias especiais", -5, 20,
            "Portadores de Estática, Neutralizar ou perícias conseguem desligá-lo."),
        Componente(Grupo.CONTRAMEDIDAS, "Contramedidas tecnológicas", -5, 20,
            "Drogas, frequências sonoras, aparelhos dedicados."),

        // ── Desvantagens Exigidas (p.23) ──
        // O traço entra por VALOR LIVRE (ver `componenteDaDesvantagem`), porque
        // o livro diz "valor em pontos das desvantagens exigidas, expresso como
        // percentual" — e não uma lista de opções.
        Componente(Grupo.DESVANTAGEM_ESVAI, "Gradualmente, dá tempo de escapar", 5, 23),
        Componente(Grupo.DESVANTAGEM_ESVAI, "Imediatamente", 0, 23),
        Componente(Grupo.DESVANTAGEM_ESVAI, "Some e ainda se volta contra o usuário", -5, 23),

        Componente(Grupo.DESVANTAGEM_RESTAURA, "Um dia", 5, 23),
        Componente(Grupo.DESVANTAGEM_RESTAURA, "Uma semana, aventura menor ou ferimento menor", 0, 23),
        Componente(Grupo.DESVANTAGEM_RESTAURA, "Um mês, aventura maior ou ferimento maior", -5, 23),

        // ── Outros (p.25) — somam ──
        Componente(Grupo.EXTRAS, "Fica inútil quando a Reserva de Energia esgota", -5, 25),
        Componente(Grupo.EXTRAS, "Efeito Incômodo", -5, 25),
        Componente(Grupo.EXTRAS, "Penitência para reparar (até um mês)", -5, 25)
    )

    /**
     * A desvantagem exigida entra pelo **valor em pontos**, não por uma lista.
     *
     * > *"Código de conduta: **valor em pontos das desvantagens exigidas**,
     * > expresso como percentual."* (p.25)
     *
     * Um Voto de −10 pontos vale −10%; um Fanatismo de −15 vale −15%.
     */
    fun componenteDaDesvantagem(valorEmPontos: Int): Componente =
        Componente(
            Grupo.DESVANTAGEM_TRACO,
            "Desvantagem exigida de ${valorEmPontos} pontos",
            -Math.abs(valorEmPontos), 23,
            "Idêntico à limitação Pacto (MB p.114); não pode ser escolhida duas vezes."
        )

    /**
     * Soma os componentes, com o teto de −80% do resto do livro.
     *
     * ⚠️ Não valida se há duas escolhas do mesmo grupo — quem valida é
     * [conflitosDeGrupo], para a tela poder avisar em vez de calar.
     */
    fun total(escolhidos: List<Componente>): Int =
        RegrasDePoder.limitarModificadorTotal(escolhidos.sumOf { it.valor })

    /** Grupos marcados "(escolha um)" no livro com mais de uma escolha. */
    fun conflitosDeGrupo(escolhidos: List<Componente>): List<Grupo> =
        escolhidos.filter { it.grupo.escolhaUnica }
            .groupBy { it.grupo }
            .filter { it.value.size > 1 }
            .keys.toList()

    /**
     * 🔴 *"Poderes cósmicos não podem ser bloqueados nem possuem contramedidas."*
     * (p.25, nota de rodapé da Referência Rápida.)
     *
     * Escolher o Cósmico e ainda marcar contramedidas seria cobrar um
     * inconveniente que, por definição, o poder não tem.
     */
    fun cosmicoComContramedidas(escolhidos: List<Componente>): Boolean =
        escolhidos.any { it.valor == 50 && it.rotulo.contains("Cósmico") } &&
            escolhidos.any { it.grupo == Grupo.CONTRAMEDIDAS && it.valor != 0 }

    /**
     * *"Tente manter o valor entre −10% e −30%"* (p.25).
     *
     * ⚠️ É **conselho**, não trava — o próprio livro diz que dá para chegar a
     * −25% só com desvantagens exigidas, e o Cósmico é +50%.
     */
    const val FAIXA_RECOMENDADA_MIN = -30
    const val FAIXA_RECOMENDADA_MAX = -10

    fun avisoDaFaixa(total: Int): String? = when {
        total in FAIXA_RECOMENDADA_MIN..FAIXA_RECOMENDADA_MAX -> null
        total > 0 -> "Modificador positivo: o poder amplia as habilidades em vez de restringi-las (p.20)."
        total > FAIXA_RECOMENDADA_MAX ->
            "O livro sugere manter entre −30% e −10% (p.25). Uma limitação de −5% costuma " +
                "ser pequena demais para o jogador aceitar as restrições."
        else ->
            "O livro sugere manter entre −30% e −10% (p.25). Acima de −20%, considere " +
                "remover o que não é essencial para o clima do poder."
    }
}
