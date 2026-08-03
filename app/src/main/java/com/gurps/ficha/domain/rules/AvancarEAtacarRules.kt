package com.gurps.ficha.domain.rules

/**
 * **Avançar e Atacar** — atirar (ou golpear) em movimento. MB p.366.
 *
 * > O personagem ataca como descrito na manobra Ataque, mas sofre uma penalidade.
 * > Se estiver realizando um Ataque **à distância**, a penalidade é de **-2 ou
 * > igual à Magnitude da arma, o que for pior**. Se estiver realizando um Ataque
 * > **corpo a corpo**, a penalidade é de **-4** e o nível de habilidade ajustado
 * > **não pode ser maior que 9**.
 *
 * ## Por que a Magnitude só apareceu agora
 *
 * O campo existe em `Equipamento` desde o Lote 371 e era consumido **apenas** pelo
 * combate da Saga. Na aba Rolagem ele nunca chegou à tela: o jogador via a arma
 * com Magnitude −6 e não tinha onde usá-la.
 *
 * ## 🔴 O dado torto que este lote encontrou
 *
 * Três armas de fogo traziam **Magnitude +10** (`"10↑"`), com a CdT vazia: a linha
 * da planilha de origem tinha escorregado **uma coluna inteira** a partir da CdT,
 * e o que estava em `magnitude` era na verdade a **ST**. O Rifle de Atirador .338
 * chegou a ficar com **ST 41** — e como a lista de armas filtra por ST, ele era
 * **impossível de adicionar** a qualquer ficha normal.
 *
 * O JSON foi corrigido com a linha do livro (p.280). Mas a guarda fica: no livro
 * a Magnitude é **sempre ≤ 0**, e um valor positivo é sinal de linha torta, não de
 * arma boa. Um `+10` aqui viraria **bônus de dez** para atirar correndo.
 */
object AvancarEAtacarRules {

    /** A penalidade mínima do livro para ataque à distância em movimento. */
    const val BASICA_A_DISTANCIA = -2

    /** A penalidade fixa do corpo a corpo. */
    const val BASICA_CORPO_A_CORPO = -4

    /** O teto do NH ajustado em corpo a corpo (MB p.366). */
    const val TETO_CORPO_A_CORPO = 9

    /**
     * A penalidade do ataque à distância: **−2 ou a Magnitude, o que for pior**.
     *
     * ⚠️ [magnitude] positiva é tratada como **ausente**. Ver o KDoc da classe: no
     * livro ela nunca passa de 0, então positivo é defeito de catálogo. Somar
     * daria bônus onde a regra manda penalizar.
     */
    fun penalidadeADistancia(magnitude: Int?): Int {
        val mag = magnitude?.takeIf { it <= 0 } ?: return BASICA_A_DISTANCIA
        return minOf(BASICA_A_DISTANCIA, mag)
    }

    /** `true` quando a Magnitude da arma é pior que o −2 e por isso manda. */
    fun magnitudeMandou(magnitude: Int?): Boolean {
        val mag = magnitude?.takeIf { it <= 0 } ?: return false
        return mag < BASICA_A_DISTANCIA
    }

    /** A Magnitude está fora do que o livro admite (positiva)? */
    fun magnitudeSuspeita(magnitude: Int?): Boolean = (magnitude ?: 0) > 0

    /**
     * O NH final do ataque **corpo a corpo** em movimento.
     *
     * ⚠️ São **duas** coisas, não uma: primeiro o −4, depois o **teto de 9**. Um
     * espadachim NH 20 vai a **9**, não a 16 — e é a parte que mais escapa na
     * mesa, porque parece penalidade e é limite.
     */
    fun nhCorpoACorpo(nhBase: Int): Int =
        minOf(nhBase + BASICA_CORPO_A_CORPO, TETO_CORPO_A_CORPO)

    /** Quanto o corpo a corpo perde de fato, já contando o teto. */
    fun penalidadeCorpoACorpo(nhBase: Int): Int = nhCorpoACorpo(nhBase) - nhBase

    /**
     * O rótulo da caixinha, dizendo **de onde veio o número**.
     *
     * Sem isso o jogador vê −6 e não tem como conferir se foi o padrão ou a arma.
     */
    fun rotulo(ehADistancia: Boolean, magnitude: Int?, nhBase: Int): String {
        if (!ehADistancia) {
            val p = penalidadeCorpoACorpo(nhBase)
            val teto = if (nhBase + BASICA_CORPO_A_CORPO > TETO_CORPO_A_CORPO) {
                " · teto de $TETO_CORPO_A_CORPO no corpo a corpo (MB p.366)"
            } else ""
            return "Avançar e Atacar: $p$teto"
        }
        if (magnitudeSuspeita(magnitude)) {
            return "Avançar e Atacar: $BASICA_A_DISTANCIA — a Magnitude desta arma " +
                "está cadastrada como +$magnitude, o que o livro não admite; usei o padrão"
        }
        val p = penalidadeADistancia(magnitude)
        val origem = if (magnitudeMandou(magnitude)) {
            "Magnitude da arma, pior que o $BASICA_A_DISTANCIA básico"
        } else if (magnitude == null) {
            "padrão — esta arma não tem Magnitude cadastrada"
        } else {
            "padrão, pior que a Magnitude $magnitude da arma"
        }
        return "Avançar e Atacar: $p ($origem)"
    }

    fun rotuloAcessivel(ehADistancia: Boolean, magnitude: Int?, nhBase: Int): String {
        val p = if (ehADistancia) penalidadeADistancia(magnitude) else penalidadeCorpoACorpo(nhBase)
        val comoLer = if (p < 0) "menos ${-p}" else "$p"
        return "Marcar que está atacando em movimento, na manobra Avançar e Atacar. " +
            "Custa $comoLer no ataque. " +
            if (!ehADistancia && nhBase + BASICA_CORPO_A_CORPO > TETO_CORPO_A_CORPO) {
                "No corpo a corpo o nível ainda fica limitado a nove."
            } else {
                "Não é possível Apontar enquanto se corre."
            }
    }

    /** Por que a caixinha do Apontar some quando esta está marcada. */
    const val AVISO_EXCLUSIVO =
        "Apontar desligado: não dá para acumular segundos de pontaria correndo."
}
