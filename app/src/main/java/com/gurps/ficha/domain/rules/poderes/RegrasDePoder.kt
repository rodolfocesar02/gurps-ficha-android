package com.gurps.ficha.domain.rules.poderes

/**
 * **Poderes** — GURPS Poderes, p.7-8, p.20-30. Lotes POD-1 a POD-3.
 *
 * ## A anatomia, palavra do livro
 *
 * > *"Além de sua fonte e seu foco, um poder possui três componentes de mecânica
 * > de jogo: 1. Um conjunto de vantagens (…) as habilidades do poder. 2. Um
 * > modificador especial, normalmente uma limitação, chamado de modificador de
 * > poder. 3. Um Talento que facilite o uso de todas as habilidades."* (p.7)
 *
 * E a regra que não abre exceção:
 *
 * > *"Uma vantagem precisa ter o respectivo modificador de poder para ser parte
 * > dele; não há exceções."* (p.8)
 *
 * ## 🔴 O erro de modelagem que este arquivo desfaz
 *
 * O app guardava o modificador como **um número que o jogador digitava no
 * poder**. O livro não funciona assim: cada verbete lista as fontes válidas
 * daquele poder e diz *"Este modificador normalmente é Divino (-10%), Elemental
 * (-10%), Espiritual (-25%)…"*. **O percentual vem da FONTE escolhida**, e cada
 * poder aceita só um subconjunto das onze.
 *
 * Água aceita cinco fontes; Antipsi aceita três. No catálogo inteiro a média é
 * de 3,6 fontes por poder — não é um número livre, é uma escolha.
 */
object RegrasDePoder {

    /**
     * As onze fontes genéricas do livro (p.26-30), com o valor fechado.
     *
     * ⚠️ Esta é a cópia de segurança **em código** do que está em
     * `fontes_de_poder.v1.json`. Existe para o teste poder cobrar o asset: se
     * alguém editar o JSON e trocar um número, a varredura acusa.
     */
    val VALOR_DA_FONTE: Map<String, Int> = mapOf(
        "Biológico" to -10,
        "Chi" to -10,
        "Cósmico" to +50,
        "Divino" to -10,
        "Elemental" to -10,
        "Espiritual" to -25,
        "Mágico" to -10,
        "Moral" to -20,
        "Natureza" to -20,
        "Psíquico" to -10,
        "Super" to -10
    )

    /**
     * O livro escreve a fonte no masculino no modificador (*"Divino (-10%)"*) e
     * no feminino na linha *"Fontes:"* (*"Divina"*). São a mesma coisa.
     */
    private val FEMININO = mapOf(
        "Biológica" to "Biológico", "Cósmica" to "Cósmico", "Divina" to "Divino",
        "Mágica" to "Mágico", "Psíquica" to "Psíquico", "Psiquismo" to "Psíquico"
    )

    fun normalizarFonte(nome: String?): String? {
        val t = nome?.trim().orEmpty()
        if (t.isEmpty()) return null
        val canon = FEMININO[t] ?: t
        return if (VALOR_DA_FONTE.containsKey(canon)) canon else null
    }

    /** O percentual da fonte, ou `null` se ela não for uma das onze. */
    fun valorDaFonte(nome: String?): Int? = normalizarFonte(nome)?.let { VALOR_DA_FONTE[it] }

    // ── O teto do modificador total ────────────────────────────────────

    /**
     * > *"Se o modificador total de determinada habilidade, inclusive o
     * > modificador de poder, for pior que -80%, considere-o como -80%."* (p.28)
     *
     * ⚠️ O piso é do **total**, não de cada modificador. Uma habilidade com
     * -60% de limitações próprias mais um poder Espiritual (-25%) daria -85%;
     * o livro corta em -80%, e é isso que muda o custo.
     */
    const val PIOR_MODIFICADOR_TOTAL = -80

    fun limitarModificadorTotal(total: Int): Int = total.coerceAtLeast(PIOR_MODIFICADOR_TOTAL)

    /** `true` quando o teto realmente mordeu — serve para a tela avisar. */
    fun oTetoCortou(total: Int): Boolean = total < PIOR_MODIFICADOR_TOTAL

    // ── O Talento ──────────────────────────────────────────────────────

    /**
     * > *"A maioria dos Talentos custa 5 pontos por nível. Um Talento com uma
     * > ampla gama de aplicações, comparável à Aptidão Mágica, custa 10 pontos/
     * > nível."* (p.29)
     */
    const val CUSTO_PADRAO_POR_NIVEL = 5
    const val CUSTO_AMPLO_POR_NIVEL = 10

    /**
     * > *"Não é possível comprar mais do que quatro níveis de determinado Talento
     * > sem a permissão do Mestre."* (p.8)
     *
     * ⚠️ É **aviso, não trava**. O livro diz "sem a permissão do Mestre" — quem
     * decide é a mesa, e o app não pode decidir por ela. Mesmo padrão das
     * camadas de armadura.
     */
    const val NIVEIS_SEM_PERMISSAO = 4

    fun custoDoTalento(nivel: Int, custoPorNivel: Int = CUSTO_PADRAO_POR_NIVEL): Int =
        nivel.coerceAtLeast(0) * custoPorNivel

    fun passouDoTetoDeNiveis(nivel: Int): Boolean = nivel > NIVEIS_SEM_PERMISSAO

    fun avisoDoTeto(nivel: Int): String? =
        if (passouDoTetoDeNiveis(nivel))
            "Talento nível $nivel: acima de $NIVEIS_SEM_PERMISSAO o livro pede " +
                "permissão do Mestre (Poderes, p.8)."
        else null

    /** A mesma frase sem o sinal cru, para a variante que é lida em voz alta. */
    fun avisoDoTetoAcessivel(nivel: Int): String? = avisoDoTeto(nivel)
}
