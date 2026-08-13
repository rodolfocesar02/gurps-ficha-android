package com.gurps.ficha.domain.rules.poderes

import kotlin.math.ceil

/**
 * **Ampliações temporárias, uso predefinido e multiplicação** — GURPS Poderes,
 * p.172-173 e p.102. Lote POD-13.
 *
 * Adicionar uma ampliação **na hora**, para resolver um problema específico —
 * o que a ficção chama de "proeza".
 */
object AmpliacoesTemporarias {

    /** *"Modificadores: −1 por +10% das ampliações adicionadas, ou fração delas."* */
    const val PERCENTUAL_POR_PENALIDADE = 10

    /** *"a tentativa custa 2 PF além de qualquer gasto voluntário de PF"*. */
    const val CUSTO_EM_PF = 2

    enum class Natureza(val rotulo: String, val manobra: String, val atributo: String) {
        MENTAL("mental", "Concentrar", "Vontade"),
        FISICA("física", "Preparar", "HT")
    }

    /**
     * A perícia que pode substituir o atributo, por fonte (p.172).
     *
     * ⚠️ Só existe em jogos que usam *Perícias Aprimorando Habilidades* (p.161).
     * Fora deles, rola-se o atributo.
     */
    val PERICIA_POR_FONTE: Map<String, String> = mapOf(
        "Chi" to "Meditação",
        "Espiritual" to "Magia Ritualística",
        "Psíquico" to "Perícia Abrangente (Psiquismo)",
        "Divino" to "Ritual Religioso",
        "Mágico" to "Taumatologia"
    )

    fun periciaDaFonte(fonte: String?): String? =
        RegrasDePoder.normalizarFonte(fonte)?.let { PERICIA_POR_FONTE[it] }

    /** A penalidade crua: −1 por +10% de ampliação, **ou fração**. */
    fun penalidadeCrua(percentualAdicionado: Int): Int {
        if (percentualAdicionado <= 0) return 0
        return -ceil(percentualAdicionado.toDouble() / PERCENTUAL_POR_PENALIDADE).toInt()
    }

    /**
     * A penalidade final, depois do Talento e dos PF gastos de propósito.
     *
     * > *"O personagem pode compensar essa penalidade (**mas nunca obter um
     * > bônus, ao final**) gastando voluntariamente PF; cada PF cancela −1 nas
     * > penalidades. Quem usa poderes recebe um bônus igual ao seu Talento."*
     *
     * ⚠️ O teto em **zero** é a regra, não um detalhe de implementação: sem ele,
     * gastar PF de sobra viraria bônus de graça.
     */
    fun modificadorFinal(percentualAdicionado: Int, nivelDoTalento: Int, pfGastos: Int): Int {
        val crua = penalidadeCrua(percentualAdicionado)
        val compensado = crua + nivelDoTalento.coerceAtLeast(0) + pfGastos.coerceAtLeast(0)
        return compensado.coerceAtMost(0)
    }

    /** O total de PF da tentativa: os 2 fixos mais o que se gastou de propósito. */
    fun pfDaTentativa(pfGastos: Int, sucessoDecisivo: Boolean = false): Int =
        if (sucessoDecisivo) 0 else CUSTO_EM_PF + pfGastos.coerceAtLeast(0)

    /**
     * 🔴 A falha crítica não para na habilidade.
     *
     * > *"Em uma falha crítica, ela fica tão bagunçada que fica indisponível por
     * > **1d segundos**. Isso acontece mesmo para habilidades sempre ativas. Além
     * > disso, **verifique se ocorre a incapacitação** — e observe que as
     * > consequências se aplicam a **todo o poder**."*
     *
     * É o mesmo teste do POD-11 ([UsoDoPoder.Incapacitacao]).
     */
    const val FALHA_CRITICA_CHECA_INCAPACITACAO = true
}

/**
 * **Uso predefinido de habilidades** — p.173. Lote POD-13.
 *
 * Quando a proeza exigiria uma vantagem **inteiramente nova**, e não só uma
 * ampliação, o caminho é o uso predefinido.
 */
object UsoPredefinido {
    /** *"Em um sucesso ou fracasso, a tentativa custa 3 PF além do gasto voluntário."* */
    const val CUSTO_EM_PF = 3
}

/**
 * **Regra opcional: modificadores por multiplicação** — p.102. Lote POD-13.
 *
 * > *"Em primeiro lugar, some e aplique as **ampliações**. Em seguida, totalize
 * > as **limitações** (reduzindo qualquer total maior que −80% para −80%) e
 * > aplique-as ao resultado."*
 *
 * ⚠️ *"Não se recomenda usar ambos"* — é uma chave da ficha inteira, não uma
 * escolha por vantagem. Duas contas convivendo dariam preços diferentes para a
 * mesma habilidade.
 */
object ModelosDeModificador {

    // 🔴 As duas contas são INTEIRAS de propósito. Com `Double`,
    // `1 + (-80)/100.0` dá `0.19999999999999996`, e 100 × isso arredondado para
    // baixo devolve **19** em vez de 20. Um ponto de diferença, só em alguns
    // valores, e num número que o jogador não tem como conferir de cabeça.

    /** O padrão do app: soma tudo e aplica uma vez. */
    fun aditivo(custoBase: Int, ampliacoes: Int, limitacoes: Int): Int {
        val total = RegrasDePoder.limitarModificadorTotal(ampliacoes + limitacoes)
        return Math.floorDiv(custoBase.toLong() * (100 + total), 100L).toInt()
    }

    /** A opcional: ampliações primeiro, limitações depois, sobre o resultado. */
    fun multiplicativo(custoBase: Int, ampliacoes: Int, limitacoes: Int): Int {
        val amp = 100 + ampliacoes.coerceAtLeast(0)
        val lim = 100 + RegrasDePoder.limitarModificadorTotal(limitacoes.coerceAtMost(0))
        return Math.floorDiv(custoBase.toLong() * amp * lim, 10_000L).toInt()
    }
}
