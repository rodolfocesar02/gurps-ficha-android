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
 * ## 🔴 A tabela que eu tinha estava incompleta
 *
 * A 1ª versão do plano listou 7 componentes. A leitura completa da seção achou
 * mais 6, e corrigiu um: **Antipoderes tem dois níveis**, não um valor fixo.
 */
object MontadorDeModificador {

    /**
     * Um componente é sempre uma **escolha dentro de um grupo** — não uma caixa
     * que se marca à vontade. Contramedidas é uma só; energias canalizadas é uma
     * só. Somar duas do mesmo grupo seria cobrar o mesmo inconveniente duas vezes.
     */
    enum class Grupo(val rotulo: String) {
        CONTRAMEDIDAS("Contramedidas"),
        ANTIPODERES("Antipoderes"),
        ENERGIAS_CANALIZADAS("Energias canalizadas"),
        DESVANTAGEM_TRACO("Desvantagem exigida — o traço"),
        DESVANTAGEM_ESVAI("Desvantagem exigida — velocidade com que o poder se esvai"),
        DESVANTAGEM_RESTAURA("Desvantagem exigida — ato para restaurar"),
        EXTRAS("Outros inconvenientes")
    }

    data class Componente(
        val grupo: Grupo,
        val rotulo: String,
        val valor: Int,
        val pagina: Int,
        val explicacao: String = ""
    )

    /**
     * ⚠️ **Desvantagem exigida** entra como três escolhas encadeadas, e não uma.
     * O plano registrou só a primeira; um montador que pedisse apenas o valor do
     * traço devolveria um número incompleto.
     */
    val CATALOGO: List<Componente> = listOf(
        // ── Contramedidas (p.20-21) — escolha UMA ──
        Componente(Grupo.CONTRAMEDIDAS, "Sem contramedidas (padrão)", 0, 21,
            "O poder não sofre contramedidas além das que já afetam a versão instintiva."),
        Componente(Grupo.CONTRAMEDIDAS, "Contramedidas mundanas", -10, 20,
            "Ambiente comum na natureza derruba o poder, e o inimigo explora isso com " +
                "objetos e conhecimentos cotidianos."),
        Componente(Grupo.CONTRAMEDIDAS, "Contramedidas tecnológicas", -5, 20,
            "Só tecnologia ou treinamento especializado desmonta o poder."),
        Componente(Grupo.CONTRAMEDIDAS, "Poder cósmico", 50, 21,
            "As habilidades ignoram o que bloquearia a versão instintiva, e nada tira " +
                "o poder do portador."),

        // ── Antipoderes (p.20-21) — DOIS níveis, não um ──
        Componente(Grupo.ANTIPODERES, "Nenhum antipoder", 0, 21),
        Componente(Grupo.ANTIPODERES, "Capacidades específicas o anulam", -5, 20,
            "Portadores de Estática, Neutralizar ou perícias conseguem desligar o poder."),
        Componente(Grupo.ANTIPODERES, "As duas situações se aplicam", -10, 20,
            "🔴 O plano dizia que Antipoderes era −5% fixo. São dois níveis."),

        // ── Energias canalizadas (p.24) — TRÊS níveis ──
        Componente(Grupo.ENERGIAS_CANALIZADAS, "Energia onipresente, não filtrável", 0, 24),
        Componente(Grupo.ENERGIAS_CANALIZADAS, "Bloqueável só por isolante exótico", -5, 24,
            "Também vale para poderes que dependem do mana da região."),
        Componente(Grupo.ENERGIAS_CANALIZADAS, "Bloqueável por isolante mundano", -10, 24),

        // ── Desvantagem exigida (p.23) — TRÊS escolhas ──
        Componente(Grupo.DESVANTAGEM_TRACO, "Nenhuma desvantagem exigida", 0, 23),
        Componente(Grupo.DESVANTAGEM_TRACO, "Desvantagem de −5 pontos", -5, 23),
        Componente(Grupo.DESVANTAGEM_TRACO, "Desvantagem de −10 pontos", -10, 23,
            "\"Por exemplo, um Voto de −10 pontos vale −10%.\""),
        Componente(Grupo.DESVANTAGEM_TRACO, "Desvantagem de −15 pontos", -15, 23),

        Componente(Grupo.DESVANTAGEM_ESVAI, "Some gradualmente, dá tempo de escapar", 5, 23),
        Componente(Grupo.DESVANTAGEM_ESVAI, "Some rápido, deixa em perigo no combate", 0, 23),
        Componente(Grupo.DESVANTAGEM_ESVAI, "Some rápido E se volta contra o usuário", -5, 23),

        Componente(Grupo.DESVANTAGEM_RESTAURA, "Um dia de oração ou a renda de um dia", 5, 23),
        Componente(Grupo.DESVANTAGEM_RESTAURA, "Uma semana, aventura menor ou ferimento leve", 0, 23),
        Componente(Grupo.DESVANTAGEM_RESTAURA, "Um mês, aventura maior ou ferimento grave", -5, 23),

        // ── Outros (podem somar entre si) ──
        Componente(Grupo.EXTRAS, "Fica inútil quando a Reserva de Energia esgota", -5, 25,
            "Liga com a RE (p.119)."),
        Componente(Grupo.EXTRAS, "Efeito Incômodo", -5, 25),
        Componente(Grupo.EXTRAS, "Penitência para reparar (até um mês)", -5, 25)
    )

    /**
     * Soma os componentes, com o teto de −80% do resto do livro.
     *
     * ⚠️ Não valida se há duas escolhas do mesmo grupo — quem valida é
     * [conflitosDeGrupo], para a tela poder avisar em vez de calar.
     */
    fun total(escolhidos: List<Componente>): Int =
        RegrasDePoder.limitarModificadorTotal(escolhidos.sumOf { it.valor })

    /** Grupos com mais de uma escolha — exceto [Grupo.EXTRAS], que soma. */
    fun conflitosDeGrupo(escolhidos: List<Componente>): List<Grupo> =
        escolhidos.filter { it.grupo != Grupo.EXTRAS }
            .groupBy { it.grupo }
            .filter { it.value.size > 1 }
            .keys.toList()

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
