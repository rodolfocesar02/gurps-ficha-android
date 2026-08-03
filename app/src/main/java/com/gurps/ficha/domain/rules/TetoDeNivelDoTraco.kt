package com.gurps.ficha.domain.rules

/**
 * **Quantos níveis um traço pode ter** (Lote TETO-1).
 *
 * ## O bug que originou este arquivo
 *
 * Achado no aparelho em 31/07, testando o T-LI6. A **Mão Fraca** trava em 3
 * níveis (MB p.151: *"cada nível, até no máximo 3"*), e a perícia obedecia —
 * parava em −6. Mas o **seletor de nível** deixava subir sem limite: o jogador
 * comprava o nível 6, pagava **−30 pontos** e recebia os mesmos −6.
 *
 * A causa era mais funda do que a Mão Fraca. O campo **`max` existe nos dois
 * catálogos**, é lido do JSON pelo loader… e **jogado fora** na conversão para
 * `VantagemDefinicao`/`DesvantagemDefinicao`. Os quatro seletores da tela
 * usavam **20 fixo, escrito no código**, com um `if` especial para a Aptidão
 * Mágica.
 *
 * ⚠️ **Não era só a Mão Fraca.** Os dez **Talentos** têm `max: 4` no catálogo —
 * e há um teste afirmando isso, porque o livro diz *"nunca pode ter mais que
 * quatro níveis em um determinado Talento"* (MB p.91). O teto estava declarado,
 * testado… e não chegava à tela. A **Suscetibilidade à Magia** (`max: 5`,
 * p.159) idem.
 *
 * Analogia: a placa de velocidade estava pregada na estrada e o radar não lia
 * nenhuma delas — usava sempre o mesmo número.
 *
 * ## A ordem de decisão
 *
 * 1. O **`max` do catálogo**, quando houver. É o número do livro.
 * 2. A **Aptidão Mágica**, que tem teto próprio de 11 e não vem do catálogo.
 * 3. O **teto geral de 20**, que não é regra do livro: é um limite de tela para
 *    o seletor não virar rolagem infinita.
 *
 * ⚠️ **O teto vale para a COMPRA, não para ficha salva.** Mesma decisão do teto
 * de HT do Magro e do piso de 3: uma ficha antiga com nível acima do máximo
 * continua abrindo, e o `MaoFracaRule` já limita o *efeito* por conta própria.
 * Bloquear a abertura seria perder a ficha do jogador por uma regra que entrou
 * depois.
 *
 * Kotlin puro e testável.
 */
object TetoDeNivelDoTraco {

    /** Limite de tela, não do livro — evita seletor sem fim. */
    const val TETO_GERAL = 20

    /** Aptidão Mágica vai até 11 e não declara `max` no catálogo. */
    const val TETO_APTIDAO_MAGICA = 11

    const val ID_APTIDAO_MAGICA = "aptidao_magica"

    /** Todo traço pode ter pelo menos um nível. */
    const val MINIMO = 1

    /**
     * O teto deste traço.
     *
     * [maxDoCatalogo] é o campo `max` do JSON — null na maioria dos traços, que
     * não têm limite escrito no livro.
     */
    fun de(id: String, maxDoCatalogo: Int?): Int = when {
        maxDoCatalogo != null && maxDoCatalogo >= MINIMO -> maxDoCatalogo
        id.equals(ID_APTIDAO_MAGICA, ignoreCase = true) -> TETO_APTIDAO_MAGICA
        else -> TETO_GERAL
    }

    /**
     * O nível já ajustado ao teto, para o seletor.
     *
     * Nunca desce de [MINIMO]: nível 0 significaria não ter o traço, e quem
     * quer isso remove da ficha.
     */
    fun ajustar(nivel: Int, id: String, maxDoCatalogo: Int?): Int =
        nivel.coerceIn(MINIMO, de(id, maxDoCatalogo))

    /**
     * A linha que explica por que o botão **+** parou de responder.
     *
     * Sem ela o jogador toca, não acontece nada, e ele conclui que travou. Só
     * aparece quando o teto vem do **livro** — dizer "máximo 20" seria expor um
     * detalhe de tela como se fosse regra.
     */
    fun avisoDoTeto(id: String, maxDoCatalogo: Int?): String? {
        val teto = de(id, maxDoCatalogo)
        if (teto >= TETO_GERAL) return null
        return "Máximo de $teto ${if (teto == 1) "nível" else "níveis"} pelo livro."
    }
}
