package com.gurps.ficha.domain.rules

/**
 * **O custo em energia de uma mágica** (MB p.236 e p.238).
 *
 * ## 🔴 O que estava errado antes do Lote MAGIA-E1
 *
 * A versão anterior tinha dez linhas e três buracos, todos silenciosos:
 *
 * 1. **A faixa era jogada fora.** `parseCusto("1 a 3")` fazia `split(' ')[0]` e
 *    devolvia **1**. São **31 mágicas** com custo em faixa no catálogo — em todas
 *    elas o jogador ficava preso ao mínimo, e a *Cura Superficial* nunca curava
 *    mais que 1 PV, porque o que se gasta é o que se cura.
 * 2. **`"Varia"` não cobrava nada.** São **130 mágicas** (mais 10 de
 *    `"Especial"`): o parse devolvia nulo, quem chamava fazia `?: return` e a
 *    magia saía de graça, sem diálogo e sem aviso.
 * 3. **O resultado da rolagem não mexia no gasto** — ver [energiaGasta].
 *
 * ⚠️ Nenhum dos três aparecia como erro. A magia rolava, o número saía, e a
 * ficha só descontava menos do que devia — ou nada.
 */
object MagiaEnergiaRules {

    /**
     * O desconto por NH alto (MB p.238).
     *
     * > **NH 15–19** — Custo: Reduzido em 1 ponto. **NH 20–24** — Reduzido em 2.
     * > **NH 25–29** — Reduzido em 3.
     *
     * ⚠️ O Módulo Básico **não** dá um piso geral de 1 ponto: o custo pode chegar
     * a zero. Sabemos disso porque a mágica *Drenar Energia* precisa dizer, no
     * texto dela, que é **exceção** — *"nunca é reduzido por NH elevado; um
     * mínimo de 1 PF sempre é gasto"*. Exceção escrita implica regra geral
     * diferente.
     */
    fun reducaoPorNh(nhBasico: Int): Int = when {
        nhBasico >= 20 -> 2 + ((nhBasico - 20) / 5)
        nhBasico >= 15 -> 1
        else -> 0
    }

    fun custoAjustadoPorNh(custoBase: Int, nhBasico: Int): Int =
        (custoBase - reducaoPorNh(nhBasico)).coerceAtLeast(0)

    /**
     * O custo de uma mágica como o catálogo o escreve.
     *
     * [desconhecido] é o caso honesto: `"Varia"`, `"Especial"` e as fórmulas em
     * texto ("1 ponto para cada 10 pontos de personagem"). O app **não pode
     * inventar** esse número, mas também não pode fingir que é zero — tem que
     * perguntar.
     */
    data class Custo(
        val minimo: Int,
        val maximo: Int,
        val raw: String,
        val desconhecido: Boolean
    ) {
        val variavel: Boolean get() = maximo > minimo

        /** Quando o app **precisa perguntar** ao jogador quanto ele gasta. */
        val precisaEscolher: Boolean get() = variavel || desconhecido

        /** O valor, quando ele é um só. Nulo se for faixa ou desconhecido. */
        val fixo: Int? get() = if (!variavel && !desconhecido) minimo else null

        fun dentroDaFaixa(escolhido: Int): Boolean = when {
            desconhecido -> escolhido >= 0
            else -> escolhido in minimo..maximo
        }

        val comoTexto: String get() = when {
            desconhecido && minimo > 0 -> "a partir de $minimo (o catálogo diz \"$raw\")"
            desconhecido -> "o catálogo diz \"$raw\" — quem decide é o Mestre"
            variavel -> "de $minimo a $maximo"
            else -> "$minimo"
        }
    }

    private val FAIXA = Regex("""^(\d+)\s*a\s*(\d+)$""", RegexOption.IGNORE_CASE)
    private val SO_NUMERO = Regex("""^\d+$""")
    private val FAIXA_ABERTA = Regex("""^(\d+)\s*a\s+\S+$""", RegexOption.IGNORE_CASE)

    /**
     * Lê o campo `energia` do catálogo. **Nunca devolve nulo** — o desconhecido
     * é representado, não omitido.
     *
     * ⚠️ Devolver nulo era o buraco nº 2: quem chamava escrevia `?: return` e a
     * mágica saía de graça. Um tipo que **não consegue** dizer "não sei" obriga
     * quem chama a decidir por conta própria — e a decisão fácil é não fazer nada.
     *
     * O catálogo tem **242 formatos distintos** em 879 mágicas. Os que importam:
     * - `"3"` — custo fixo.
     * - `"1 a 3"` — faixa; é o jogador que escolhe.
     * - `"04/02"` — **operar/manter**. Aqui vale o de operar.
     * - `"2/M"`, `"1/I"`, `"3#"` — sufixos de nota do catálogo; ignorados.
     * - `"Varia"`, `"Especial"`, `"1 a AM#"` — desconhecido.
     */
    fun parseCusto(energiaStr: String?): Custo {
        val bruto = energiaStr?.trim().orEmpty()
        if (bruto.isBlank()) return Custo(0, 0, bruto, desconhecido = true)

        // A barra separa OPERAR de MANTER ("04/02"). Vale o de operar.
        val cabeca = bruto
            .replace("pontos", "", ignoreCase = true)
            .replace("ponto", "", ignoreCase = true)
            .split('/')[0]
            .replace("#", "")
            .trim()

        FAIXA.find(cabeca)?.let { m ->
            val a = m.groupValues[1].toInt()
            val b = m.groupValues[2].toInt()
            return Custo(minOf(a, b), maxOf(a, b), bruto, desconhecido = false)
        }
        if (SO_NUMERO.matches(cabeca)) {
            val n = cabeca.toInt()
            return Custo(n, n, bruto, desconhecido = false)
        }
        // "1 a AM#": sabemos o piso, não o teto.
        FAIXA_ABERTA.find(cabeca)?.let { m ->
            return Custo(m.groupValues[1].toInt(), m.groupValues[1].toInt(), bruto, desconhecido = true)
        }
        return Custo(0, 0, bruto, desconhecido = true)
    }

    // ==================================================================
    // 🔴 O resultado da rolagem muda o gasto
    // ==================================================================

    enum class Resultado { SUCESSO_DECISIVO, SUCESSO, FRACASSO, FALHA_CRITICA }

    /**
     * Quanta energia sai da ficha, dado o que os dados fizeram (MB p.236).
     *
     * > Em todo caso, **não há gasto de energia** quando o personagem obtém um
     * > **sucesso decisivo** durante uma operação mágica.
     *
     * > O **fracasso** indica que a mágica não funcionou. O operador perderá **um
     * > ponto** de energia se houver custo em energia para o uso bem-sucedido.
     * > (…) exceção: no caso de uma **mágica de informação**, o personagem deve
     * > pagar o custo total em energia, mesmo que ocorra um fracasso.
     *
     * > Uma **falha crítica** indica que o custo em energia total da mágica foi
     * > gasto e o resultado foi muito ruim!
     *
     * ⚠️ O app cobrava o **custo cheio em todos os quatro casos**. Quem tirava
     * sucesso decisivo pagava o que devia ser de graça, e quem falhava pagava
     * cinco pontos onde o livro cobra **um**. É o tipo de erro que favorece e
     * prejudica o jogador em momentos diferentes, então nunca vira reclamação —
     * só vira uma ficha errada.
     */
    fun energiaGasta(
        comprometido: Int,
        resultado: Resultado,
        ehMagiaDeInformacao: Boolean = false
    ): Int {
        val custo = comprometido.coerceAtLeast(0)
        if (custo == 0) return 0
        return when (resultado) {
            Resultado.SUCESSO_DECISIVO -> 0
            Resultado.SUCESSO -> custo
            Resultado.FALHA_CRITICA -> custo
            Resultado.FRACASSO -> if (ehMagiaDeInformacao) custo else 1
        }
    }

    /** A frase que a tela mostra explicando por que saiu aquele número. */
    fun explicarGasto(
        comprometido: Int,
        resultado: Resultado,
        ehMagiaDeInformacao: Boolean = false
    ): String = when {
        comprometido <= 0 -> "Sem custo em energia."
        resultado == Resultado.SUCESSO_DECISIVO ->
            "Sucesso decisivo: nenhuma energia é gasta (MB p.236)."
        resultado == Resultado.FRACASSO && !ehMagiaDeInformacao ->
            "Fracasso: perde 1 ponto de energia, não o custo cheio (MB p.236)."
        resultado == Resultado.FRACASSO ->
            "Fracasso numa mágica de informação: paga o custo total mesmo assim (MB p.241)."
        resultado == Resultado.FALHA_CRITICA ->
            "Falha crítica: o custo total foi gasto, e o resultado foi ruim (MB p.236)."
        else -> "Sucesso: paga o custo comprometido."
    }
}
