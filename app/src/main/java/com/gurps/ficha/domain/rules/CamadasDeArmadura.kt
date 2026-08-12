package com.gurps.ficha.domain.rules

/**
 * **Combinando e Sobrepondo Armaduras** — MB p.287. Lote EQP-10.
 *
 * > *"O personagem pode combinar várias peças de armadura que não cubram o mesmo
 * > ponto de impacto, mas **só é possível sobrepor uma armadura se a camada
 * > interna for flexível e ocultável**. Some a RD das duas camadas. A utilização
 * > de uma camada adicional de armadura em qualquer lugar que não seja a cabeça
 * > impõe uma penalidade de **-1 sobre a DX e as perícias baseadas em DX**."*
 *
 * ## 🔴 O que o app fazia
 *
 * Somava a RD de **qualquer** peça com **qualquer** outra, e havia um comentário
 * meu em `CoberturaDaArmadura.rdTotal` dizendo que *"somar camadas é decisão do
 * Mestre"*. O livro tem a regra; eu é que não a tinha lido.
 *
 * Das **72** armaduras do catálogo, só **11** são flexíveis *e* ocultáveis — as
 * únicas que podem ser a camada de baixo. O app deixava empilhar as 72.
 *
 * ## ⚠️ Isto confirma a suposição do EQP-9
 *
 * Lá eu assumi *"rígido por fora, flexível por dentro"* para calcular o trauma
 * por impacto, e documentei como suposição. Esta página **exige** que a camada
 * interna seja flexível. Não era palpite.
 *
 * ## O que aqui NÃO se faz
 *
 * Não se bloqueia nada. A ficha do jogador é dele, e um Mestre pode liberar o
 * que quiser — o app **avisa** e mostra o preço. Bloquear seria transformar uma
 * regra numa cerca.
 */
object CamadasDeArmadura {

    /** Uma peça vestida num mesmo local. */
    data class Peca(
        val nome: String,
        val rd: Int,
        val flexivel: Boolean,
        val ocultavel: Boolean
    ) {
        /** Só a peça flexível **e** ocultável serve de camada de baixo (p.287). */
        val podeSerCamadaDeBaixo: Boolean get() = flexivel && ocultavel
    }

    /** O que está acontecendo num local do corpo. */
    data class Situacao(
        val local: LocalAtaque,
        val pecas: List<Peca>,
        val legal: Boolean,
        val aviso: String?
    ) {
        val sobreposta: Boolean get() = pecas.size > 1
        val rdSomada: Int get() = pecas.sumOf { it.rd }
    }

    /**
     * A cabeça é a exceção do redutor de DX — *"em qualquer lugar que não seja a
     * cabeça"*.
     */
    private val CABECA = setOf(LocalAtaque.CRANIO, LocalAtaque.ROSTO, LocalAtaque.OLHO)

    fun avaliar(local: LocalAtaque, pecas: List<Peca>): Situacao {
        if (pecas.size <= 1) return Situacao(local, pecas, legal = true, aviso = null)

        // O livro pede que a camada INTERNA seja flexível e ocultável. A ficha
        // não guarda a ordem em que as peças foram vestidas, então a pergunta que
        // dá para responder é: existe alguma que **poderia** ser a de baixo?
        // Se nenhuma serve, não há ordem nenhuma que torne a pilha legal.
        val temCandidata = pecas.any { it.podeSerCamadaDeBaixo }
        val aviso = if (temCandidata) {
            null
        } else {
            "Nenhuma destas peças é flexível e ocultável — pelo livro (p.287) elas " +
                "não podem ser sobrepostas. A soma continua na tela para o Mestre decidir."
        }
        return Situacao(local, pecas, legal = temCandidata, aviso = aviso)
    }

    /**
     * O preço de empilhar: **−1 na DX e nas perícias baseadas em DX**.
     *
     * ⚠️ **Um −1, não um por local.** O livro diz *"a utilização de uma camada
     * adicional"* e não chama o redutor de cumulativo — ao contrário dos
     * redutores de reação da mesma página, onde ele escreve *"esses redutores são
     * cumulativos"* com todas as letras. Onde o livro é explícito num parágrafo e
     * silencioso no de cima, o silêncio conta.
     */
    fun penalidadeDeDx(situacoes: List<Situacao>): Int =
        if (situacoes.any { it.sobreposta && it.local !in CABECA }) -1 else 0

    /**
     * Se a peça pode ser escondida sob a roupa, a partir das notas do catálogo.
     *
     * São as notas [1] da p.285 (*"Pode ser ocultado como ou sob uma peça de
     * roupa"*) e [2] da p.286 (*"Pode ser considerada uma roupa ou ocultada com ou
     * sob as roupas"*). O catálogo não tem um campo para isto — a informação vive
     * no texto da nota, e é de lá que se lê.
     */
    fun ehOcultavel(observacoes: List<String>): Boolean =
        observacoes.any { linha ->
            val t = linha.lowercase()
            t.contains("ocultad") || t.contains("ocultar") || t.contains("ocultá") ||
                (t.contains("roupa") && t.contains("consider"))
        }

    /** O texto do aviso de DX, pronto para a tela. Null quando não há pilha. */
    fun avisoDeDx(penalidade: Int): String? =
        if (penalidade == 0) null else
            "Camada extra de armadura: $penalidade na DX e em todas as perícias " +
                "baseadas em DX enquanto estiver vestida (MB p.287)."

    /** O mesmo, sem sinal cru, para quem ouve a tela. */
    fun avisoDeDxAcessivel(penalidade: Int): String? =
        if (penalidade == 0) null else
            "Camada extra de armadura: ${RotuloAcessivel.modificador(penalidade)} na destreza " +
                "e em todas as perícias baseadas em destreza enquanto estiver vestida."
}
