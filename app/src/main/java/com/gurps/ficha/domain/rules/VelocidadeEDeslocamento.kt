package com.gurps.ficha.domain.rules

/**
 * **Comprar Velocidade Básica e Deslocamento Básico** — MB p.17. Lote ATR-1.
 *
 * ## 🔴 O que faltava, e o que NÃO faltava
 *
 * Quase tudo já existia:
 *
 * - o modelo tinha `modVelocidadeBasica` e `modDeslocamentoBasico`;
 * - [CharacterRules.calcularPontosSecundarios] **já cobrava** 5 pontos por passo
 *   de 0,25 e 5 por metro/segundo;
 * - o `FichaViewModel` já tinha `atualizarModVelocidadeBasica` e
 *   `atualizarModDeslocamentoBasico`;
 * - a Raça já mexia nos dois.
 *
 * O que faltava era **a tela perguntar**. Na aba Geral os dois apareciam só em
 * *Características Derivadas*, como número para ler. Quem quisesse comprar
 * Velocidade Básica — que é o que o livro manda fazer com 5 pontos — não tinha
 * onde.
 *
 * ⚠️ É a quinta vez que este formato aparece no projeto: **a regra existe, a tela
 * não pergunta**. Antes foi o XP, o campo de RD, o `custoTotalTalento` e o
 * `nivelTalento`.
 *
 * ## O que este arquivo acrescenta
 *
 * Só o que ainda não existia: o **aviso do limite realista** e os rótulos. O
 * custo continua saindo de [CharacterRules] — escrever a conta de novo aqui
 * criaria duas rotas para o mesmo número, e o defeito mora na diferença.
 */
object VelocidadeEDeslocamento {

    /** O degrau que se compra de cada vez: *"±5 pontos por ±0,25 de Velocidade"*. */
    const val PASSO_DA_VELOCIDADE = 0.25f

    /** O preço de cada degrau, nos dois: 5 pontos. */
    const val PONTOS_POR_DEGRAU = 5

    /**
     * O quanto o livro sugere não passar **numa campanha realista**.
     *
     * > *"Em uma campanha realista, o Mestre não deve permitir que a Velocidade
     * > Básica do personagem sofra uma alteração de mais que 2 pontos positivos
     * > ou negativos. Personagens não-humanos e supers não estão sujeitos a essa
     * > limitação."*
     */
    const val LIMITE_REALISTA = 2.0f

    /**
     * O Deslocamento Básico que nasce de uma Velocidade Básica.
     *
     * > *"O Deslocamento Básico começa com um valor igual à Velocidade Básica,
     * > **ignorando as frações**."*
     *
     * ⚠️ Ignorar não é arredondar: Velocidade 5,75 dá Deslocamento **5**, e não 6.
     */
    fun deslocamentoDe(velocidadeBasica: Float): Int = velocidadeBasica.toInt()

    /**
     * O aviso do limite realista, ou null quando não há o que avisar.
     *
     * ⚠️ **Avisa, não impede.** O próprio livro abre a exceção na mesma frase
     * (não-humanos e supers), e quem decide na mesa é o Mestre. Bloquear
     * repetiria o erro do `conhecimento_oculto` — e aqui seria pior, porque
     * metade das fichas deste app é de personagem sobre-humano.
     */
    fun avisoDoLimiteRealista(modVelocidadeBasica: Float): String? {
        if (kotlin.math.abs(modVelocidadeBasica) <= LIMITE_REALISTA) return null
        val sinal = if (modVelocidadeBasica > 0) "aumento" else "redução"
        return "Numa campanha realista o livro sugere no máximo " +
            "${formatar(LIMITE_REALISTA)} de $sinal na Velocidade Básica (p.17). " +
            "Personagens não-humanos e supers não têm esse limite."
    }

    /** O texto do degrau, para a tela dizer o que o botão faz. */
    fun rotuloDoPasso(): String =
        "${formatar(PASSO_DA_VELOCIDADE)} por $PONTOS_POR_DEGRAU pontos"

    /**
     * O custo em pontos de um ajuste de Velocidade Básica.
     *
     * ⚠️ Encaminha para [CharacterRules], que é quem já cobrava. Não recalcula.
     */
    fun custoDaVelocidade(modVelocidadeBasica: Float): Int =
        CharacterRules.calcularPassosVelocidadeBasica(modVelocidadeBasica) * PONTOS_POR_DEGRAU

    /** O custo em pontos de um ajuste de Deslocamento Básico. */
    fun custoDoDeslocamento(modDeslocamentoBasico: Int): Int =
        modDeslocamentoBasico * PONTOS_POR_DEGRAU

    /** Vírgula, e não ponto: é assim que o número aparece na ficha em português. */
    fun formatar(valor: Float): String =
        String.format(java.util.Locale("pt", "BR"), "%.2f", valor)
}
