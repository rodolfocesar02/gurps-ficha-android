package com.gurps.ficha.domain.rules.poderes

import kotlin.math.ceil

/**
 * **Habilidades Alternativas** — GURPS Poderes, p.11. Lote POD-6.
 *
 * ## A regra, palavra do livro
 *
 * > *"Personagens com 'habilidades alternativas' pagarão o preço integral apenas
 * > para sua habilidade **mais cara**. Todas as outras terão **1/5 do custo**. O
 * > custo final de cada habilidade deve ser definido **após** calcular todas as
 * > ampliações e limitações (inclusive o modificador de poder), aplicar o divisor
 * > e **arredondar para cima**."*
 *
 * O exemplo do livro, que este arquivo reproduz como teste:
 * Voo [36] + Super Salto 2 [18] + Caminhar no Ar [18] →
 * 36 + ⌈18/5⌉ + ⌈18/5⌉ = 36 + 4 + 4 = **44**.
 *
 * ## ⚠️ A ordem das operações é a regra
 *
 * "Após calcular todas as ampliações e limitações" e "arredondar **para cima**"
 * não são detalhe: dividir antes dos modificadores, ou arredondar para baixo,
 * dá outro número. Como o desconto é de 80%, o erro passa despercebido — fica
 * barato de qualquer jeito.
 *
 * ## O que NÃO é conta
 *
 * O livro condiciona tudo a *"com o consentimento do Mestre"*, e proíbe
 * explicitamente juntar habilidades com explicações incompatíveis dentro do
 * jogo: fumaça e bola de fogo servem; trevas e laser exigiriam superciência.
 * Isso é [MESA] — o app calcula, não autoriza.
 */
object HabilidadesAlternativas {

    const val DIVISOR = 5

    /**
     * O custo de uma habilidade **não** principal: 1/5, arredondando para cima.
     *
     * ⚠️ O mínimo é 1, não 0. Uma habilidade de 1 a 4 pontos continua custando
     * 1 — o desconto não zera nada.
     */
    fun custoDaAlternativa(custoFinal: Int): Int =
        if (custoFinal <= 0) custoFinal
        else ceil(custoFinal.toDouble() / DIVISOR).toInt().coerceAtLeast(1)

    /**
     * O total do grupo: preço cheio na mais cara, 1/5 nas demais.
     *
     * ⚠️ Recebe os custos **já finais** — com ampliações, limitações e o
     * modificador de poder aplicados. Passar o custo base daria outro número, e
     * é justamente o erro que o livro se dá ao trabalho de prevenir.
     */
    fun custoDoGrupo(custosFinais: List<Int>): Int {
        if (custosFinais.isEmpty()) return 0
        val maisCara = custosFinais.max()
        var jaCobrouAPrincipal = false
        return custosFinais.sumOf { c ->
            if (c == maisCara && !jaCobrouAPrincipal) {
                jaCobrouAPrincipal = true
                c
            } else {
                custoDaAlternativa(c)
            }
        }
    }

    /** Quanto o grupo economiza em relação a comprar tudo separado. */
    fun economia(custosFinais: List<Int>): Int =
        custosFinais.sum() - custoDoGrupo(custosFinais)

    /**
     * Os **três** inconvenientes (p.11), que o app mostra junto com a economia.
     *
     * 🔴 A primeira versão do plano registrou **dois**. O terceiro estava logo
     * depois de onde a minha leitura tinha parado.
     */
    val INCONVENIENTES: List<String> = listOf(
        "Só uma funciona por vez. Trocar exige a manobra Preparar — de um ataque " +
            "para outro ataque é ação livre.",
        "O que desativar, incapacitar, neutralizar ou drenar uma derruba o conjunto " +
            "inteiro, até ela se recuperar.",
        "Habilidade que não possa ser reativada antes de a duração acabar (Neutralizar " +
            "com Furto de Poder, qualquer coisa com Longa Distância) tranca todas pela duração."
    )

    /** Um grupo só faz sentido com duas ou mais. */
    fun ehGrupoValido(quantidade: Int): Boolean = quantidade >= 2

    /**
     * **Várias Cópias da Mesma Vantagem** — p.12. Lote POD-18.
     *
     * > *"É possível comprar a mesma vantagem mais de uma vez como a habilidade
     * > de **diferentes poderes** (…). O preço total deve ser pago somente para a
     * > habilidade mais cara, após aplicar todos os modificadores; as outras terão
     * > **1/5 do custo** (arredondado para cima)."*
     *
     * ⚠️ **A conta é a mesma das alternativas, mas a coisa é outra.** O livro é
     * explícito: *"diferente das Habilidades Alternativas, **não há conexão**
     * entre estas habilidades"*. Alternativas são configurações mutuamente
     * exclusivas do **mesmo** poder; cópias são a mesma vantagem em **poderes
     * diferentes**, e funcionam ao mesmo tempo.
     *
     * Por isso os **três inconvenientes não valem aqui** — e é a diferença que
     * mais importa: quem confundir os dois vai achar que o desconto veio de graça.
     */
    fun custoDasCopias(custosFinais: List<Int>): Int = custoDoGrupo(custosFinais)

    /**
     * As mesmas vantagens (por nome) ligadas a **poderes diferentes**.
     * Devolve, por nome, os custos finais de cada cópia.
     */
    fun agruparCopias(
        habilidades: List<Triple<String, Int, String?>>
    ): Map<String, List<Int>> =
        habilidades
            .filter { it.third != null }
            .groupBy { it.first.trim().lowercase() }
            .filterValues { copias -> copias.map { it.third }.distinct().size >= 2 }
            .mapValues { (_, copias) -> copias.map { it.second } }

    fun resumoDasCopias(nome: String, custos: List<Int>): String =
        "$nome em ${custos.size} poderes: ${custoDasCopias(custos)} pontos em vez de " +
            "${custos.sum()}. Elas funcionam ao mesmo tempo — não são alternativas."

    fun resumo(custosFinais: List<Int>): String {
        if (!ehGrupoValido(custosFinais.size)) {
            return "Um grupo de habilidades alternativas precisa de duas ou mais."
        }
        return "Grupo de ${custosFinais.size}: ${custoDoGrupo(custosFinais)} pontos " +
            "em vez de ${custosFinais.sum()} — economia de ${economia(custosFinais)}."
    }
}
