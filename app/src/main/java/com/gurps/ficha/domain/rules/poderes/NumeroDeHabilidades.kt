package com.gurps.ficha.domain.rules.poderes

/**
 * **Número de Habilidades** — GURPS Poderes, p.19. Lote POD-19.
 *
 * ## O que a regra é, e o que ela não é
 *
 * É **orientação para o Mestre**, não conta de pontos. O livro fecha a seção
 * dizendo, com todas as letras: *"Estes limites são apenas sugestões. Caso
 * decida aderir a eles, esteja aberto a negociações."*
 *
 * Por isso aqui não existe validação, teto, nem aviso vermelho. Existe **texto**,
 * com a página, para o jogador ler enquanto monta o poder.
 *
 * ## ⚠️ Por que o app não conta para você
 *
 * A tentação era escrever *"o livro sugere 2 ou 3 de movimento; você tem 5"*.
 * Não dá, e a razão é do catálogo: das **276 vantagens**, só **69** trazem
 * categoria (`combate`, `fisica`, `mental`, `social`) — e essas quatro etiquetas
 * **não são** as cinco categorias do livro. "Combate" junta ataque e defesa, que
 * o livro conta separado; "Movimento" e "Transformações Físicas" não existem
 * como etiqueta.
 *
 * Contar com isso daria um número errado em três de cada quatro fichas, e um
 * número errado é pior do que número nenhum: ele parece uma verificação.
 *
 * O dia em que as 276 vantagens tiverem categoria, o contador entra aqui — a
 * regra já está escrita.
 */
object NumeroDeHabilidades {

    const val PAGINA = 19

    /** Uma linha do livro: a categoria e quantas ele sugere. */
    data class Orientacao(
        val categoria: String,
        val quantidade: String,
        val explicacao: String
    )

    /**
     * As cinco categorias da p.19, com o número que o livro dá em cada uma.
     *
     * ⚠️ Transformações Físicas não tem número próprio de propósito: o livro
     * manda **contá-las como defesa principal** em vez de criar uma cota nova.
     */
    val ORIENTACOES = listOf(
        Orientacao(
            categoria = "Ataques",
            quantidade = "duas",
            explicacao = "Duas vantagens de ataque são bastante, e mantidas " +
                "razoavelmente genéricas para estimular a criatividade."
        ),
        Orientacao(
            categoria = "Defesas",
            quantidade = "duas ou três principais",
            explicacao = "Principal é a defesa mais ampla, como a RD, ou a que " +
                "concede imunidade total. As menores não precisam ser contadas."
        ),
        Orientacao(
            categoria = "Movimento",
            quantidade = "duas ou três principais",
            explicacao = "Principal é a que abre um ambiente antes inacessível, " +
                "como Voo, ou dá mobilidade quase irrestrita, como Dobra."
        ),
        Orientacao(
            categoria = "Mentais",
            quantidade = "duas ou três",
            explicacao = "Sentidos ou comunicação com potencial para alterar o " +
                "jogo. Influência potente, como Controle da Mente, conta como ataque."
        ),
        Orientacao(
            categoria = "Transformações Físicas",
            quantidade = "contam como defesa",
            explicacao = "Qualquer transformação com utilidade real em combate — " +
                "Insubstancialidade, Invisibilidade — conta como defesa principal."
        )
    )

    /**
     * A frase que impede a orientação de virar regra.
     *
     * ⚠️ Ela é obrigatória na tela. Sem ela a lista acima parece um limite, e o
     * jogador vai achar que o app está reprovando o poder dele.
     */
    const val NAO_E_LIMITE =
        "Estes limites são apenas sugestões (p.$PAGINA). Dá para reduzir uma " +
            "categoria para reforçar outra, ou zerar uma inteira e compensar em " +
            "outra parte. A própria Telepatia do livro não tem habilidade de " +
            "movimento nenhuma."

    /** O resumo curto, para quem só quer saber que a orientação existe. */
    fun resumo(): String =
        "O livro sugere duas de ataque, e duas ou três de defesa, de movimento " +
            "e mentais (p.$PAGINA) — sugestão, não limite."
}
