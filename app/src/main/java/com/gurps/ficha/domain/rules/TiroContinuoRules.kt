package com.gurps.ficha.domain.rules

/**
 * **O que acontece DEPOIS do tiro sair** — fogo contínuo e mau funcionamento.
 *
 * As duas regras moram no mesmo arquivo porque agem no mesmo instante e sobre os
 * mesmos dois números: o **resultado cru dos 3d6** e a **margem de sucesso**.
 * Separá-las faria duas chamadas iguais no mesmo ponto da tela.
 *
 * ⚠️ Nenhuma delas mexe no NH. Elas leem o resultado e dizem o que ele
 * significa — é o mesmo papel do `CriticoRules`.
 */

// ======================================================================
// MB-2 · Fogo contínuo (MB p.409 e p.549)
// ======================================================================

/**
 * **Quantos tiros acertaram** numa rajada.
 *
 * > Num ataque em fogo contínuo, o ataque atinge um **disparo adicional para cada
 * > múltiplo inteiro do Recuo na margem de sucesso**. — MB p.549
 *
 * ## Por que isto é barato de fazer e caro de fazer à mão
 *
 * O `Recuo` e a `CdT` já estão na ficha desde o Lote 371 e já aparecem no card da
 * arma. O que faltava era a conta — e ela é a mais chata da mesa, porque acontece
 * no meio do turno, com o jogador contando nos dedos: *"acertei por 7, o Recuo é
 * 2, então… 7 dividido por 2, mais o primeiro…"*.
 */
object TiroContinuoRules {

    /**
     * @param margem quanto o ataque passou do alvo (NH efetivo − rolagem).
     *   Negativo ou zero significa que **nada** acertou.
     * @param recuo o Rcl da arma. Nulo ou ≤ 1 vira **1** — o livro diz que "Rcl 1
     *   significa que a arma não tem ou possui um recuo muito fraco", e nesse caso
     *   **cada ponto** de margem vira um acerto.
     * @param tirosDisparados a CdT usada neste ataque. É o **teto**: não dá para
     *   acertar mais tiros do que se disparou.
     */
    fun acertos(margem: Int, recuo: Int?, tirosDisparados: Int?): Int {
        val disparados = (tirosDisparados ?: 1).coerceAtLeast(1)
        if (margem < 0) return 0
        val rcl = (recuo ?: 1).coerceAtLeast(1)
        // O primeiro acerto é o do próprio sucesso; os extras vêm da margem.
        val extras = margem / rcl
        return (1 + extras).coerceAtMost(disparados)
    }

    /** Só faz sentido oferecer a conta quando de fato houve rajada. */
    fun ehRajada(tirosDisparados: Int?): Boolean = (tirosDisparados ?: 1) > 1

    /**
     * O texto do resultado, com a conta à vista.
     *
     * ⚠️ Mostra **por que** deu aquele número. Um "3 tiros acertaram" sozinho é
     * um número que o jogador não tem como conferir — e número que não se confere
     * é número em que não se confia.
     */
    fun explicacao(margem: Int, recuo: Int?, tirosDisparados: Int?): String {
        val disparados = (tirosDisparados ?: 1).coerceAtLeast(1)
        if (margem < 0) return "Errou — nenhum tiro acertou."
        val rcl = (recuo ?: 1).coerceAtLeast(1)
        val acertos = acertos(margem, recuo, tirosDisparados)
        val plural = if (acertos == 1) "tiro acertou" else "tiros acertaram"
        val conta = if (rcl <= 1) {
            "margem $margem, Recuo 1 → 1 + $margem"
        } else {
            "margem $margem ÷ Recuo $rcl = ${margem / rcl} extra${if (margem / rcl == 1) "" else "s"}, + 1 do acerto"
        }
        val teto = if (1 + margem / rcl > disparados) " · limitado pelos $disparados tiros disparados" else ""
        return "$acertos $plural ($conta$teto)"
    }
}

// ======================================================================
// MB-8 · Mau funcionamento (MB p.407-408, regra OPCIONAL)
// ======================================================================

/**
 * **A arma enguiça** — regra opcional do Módulo Básico.
 *
 * > Como opção, todas as armas de fogo e granadas podem ter um valor que indica
 * > "mau funcionamento" ou "Mauf.". A arma emperra, erra o alvo ou falha de alguma
 * > outra maneira, se o resultado de **qualquer jogada de ataque** for **maior ou
 * > igual ao seu Mauf**.
 * >
 * > O número de mau funcionamento é uma **função do nível tecnológico**: ele é
 * > **12 em NT3, 14 em NT4, 16 em NT5 e 17 em NT6+**.
 *
 * ## 🔴 O bloqueio que eu inventei e não existia
 *
 * No plano do MB eu escrevi que faltava um campo `Mauf` nas 62 armas e que seria
 * preciso extrair do livro uma por uma. **Errado**: o quadro acima diz que o
 * número sai do **NT**, e o NT de toda arma já chegou ao modelo no Lote ARMA-1.
 * As 62 armas já tinham Mauf — faltava a fórmula.
 *
 * A lição, que já apareceu antes neste projeto: **confirmar o dado antes de
 * declarar o bloqueio**. Um "não dá" errado custa mais caro que um "não sei".
 *
 * ## ⚠️ Duas coisas que é fácil implementar errado
 *
 * 1. **O resultado é o dado CRU.** "Qualquer jogada de ataque" quer dizer os 3d6
 *    como saíram, não o NH efetivo. Um 17 enguiça mesmo num atirador NH 20 — e é
 *    justamente esse o sentido da regra.
 * 2. **Não depende de errar.** Enguiçar não é falhar: são coisas diferentes, e a
 *    arma pode enguiçar num resultado que teria acertado.
 */
object MauFuncionamentoRules {

    /**
     * O Mauf da arma, deduzido do NT (MB p.407).
     *
     * Devolve **null** quando o NT é desconhecido — sem NT não há fórmula, e
     * chutar 17 faria a arma parecer mais confiável do que se sabe.
     */
    fun maufPorNt(nt: Int?): Int? = when {
        nt == null -> null
        nt <= 3 -> 12
        nt == 4 -> 14
        nt == 5 -> 16
        else -> 17
    }

    /** A arma enguiçou? [soma3d6] é o dado **cru**, sem modificador nenhum. */
    fun enguicou(soma3d6: Int, mauf: Int?): Boolean {
        val limite = mauf ?: return false
        return soma3d6 >= limite
    }

    /** O que aconteceu, pela tabela de 3d (MB p.408). */
    enum class Falha(val rotulo: String, val detalhe: String) {
        MECANICO(
            "Problema mecânico ou elétrico",
            "A arma não dispara. Um teste de Armeiro (ou da própria perícia da " +
                "arma, gastando uma manobra Preparar) diagnostica o problema."
        ),
        DISPARO_FALHO(
            "Disparo falho",
            "O tiro não sai. Em arma de repetição, basta engatilhar de novo; em " +
                "arma de tiro único, é preciso recarregar."
        ),
        EMPERRAMENTO(
            "Emperramento",
            "A munição prendeu. Desemperrar leva tempo e exige as duas mãos."
        ),
        EXPLOSAO(
            "Problema mecânico e possível explosão",
            "O pior resultado: além de não disparar, a arma pode explodir na mão " +
                "de quem atirou."
        )
    }

    /**
     * A tabela do livro.
     *
     * ⚠️ Repare que **5–8 e 12–14 dão o mesmo resultado**. Não é engano de
     * transcrição — é a tabela do livro, e o "disparo falho" ocupa as duas faixas
     * em volta do emperramento.
     */
    fun tabela(soma3d6: Int): Falha = when (soma3d6) {
        in 3..4 -> Falha.MECANICO
        in 5..8 -> Falha.DISPARO_FALHO
        in 9..11 -> Falha.EMPERRAMENTO
        in 12..14 -> Falha.DISPARO_FALHO
        else -> Falha.EXPLOSAO
    }

    /** O texto que aparece quando a arma enguiça, já com a origem do número. */
    fun explicacao(soma3d6DoAtaque: Int, mauf: Int?, soma3d6DaTabela: Int): String {
        val falha = tabela(soma3d6DaTabela)
        return "⚠️ A arma enguiçou (saiu $soma3d6DoAtaque, o Mauf desta arma é " +
            "${mauf ?: "—"}) · ${falha.rotulo}: ${falha.detalhe}"
    }
}
