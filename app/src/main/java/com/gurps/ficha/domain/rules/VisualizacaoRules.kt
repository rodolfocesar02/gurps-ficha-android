package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * **Visualização** (MB p.99) — imaginar a cena para ganhar bônus nela.
 *
 * > Para usar este talento, o personagem precisa se concentrar durante um minuto
 * > (…). Em seguida, ele faz um **teste de IQ**. O personagem recebe um bônus de
 * > **+1 na ação visualizada para cada ponto na margem de sucesso** se as
 * > circunstâncias forem quase idênticas às visualizadas. Se elas não forem
 * > exatamente as mesmas, o que normalmente acontece, o bônus é **reduzido pela
 * > metade** (no mínimo +1). Por outro lado, se alguma coisa for gritantemente
 * > diferente, **divida o bônus por 3** (sem um valor mínimo).
 *
 * ## Por que vale automatizar
 *
 * É uma calculadora fechada com **três regras de arredondamento diferentes** — e
 * duas delas são exceções:
 *
 * | Semelhança | Conta | Piso |
 * |---|---|---|
 * | Quase idêntico | margem inteira | — |
 * | Parecido (*"o que normalmente acontece"*) | ÷ 2 | **mínimo +1** |
 * | Gritantemente diferente | ÷ 3 | **sem mínimo** |
 *
 * ⚠️ O piso existe num caso e **não** existe no outro. Fazer de cabeça, no meio da
 * mesa, com o jogador ansioso pelo bônus, é pedir para errar para cima.
 *
 * Divisão de GURPS **descarta a fração**, então margem 5 ÷ 2 = 2, não 2,5.
 *
 * ## O que ela NÃO faz
 *
 * ⚠️ **Não vale em combate.** O livro: *"Isso a torna inútil durante um combate,
 * onde a situação se altera com mais rapidez do que é possível perceber."* Um
 * minuto de concentração não cabe num turno de um segundo.
 *
 * Kotlin puro e testável.
 */
object VisualizacaoRules {

    const val ID = "visualizacao"

    /** Quanto a cena real parece com a que ele imaginou. */
    enum class Semelhanca(val rotulo: String, val descricao: String) {
        QUASE_IDENTICO(
            "Quase idêntico",
            "As circunstâncias são quase as mesmas que ele visualizou."
        ),
        PARECIDO(
            "Parecido",
            "Não são exatamente as mesmas — o que normalmente acontece. Bônus pela metade."
        ),
        MUITO_DIFERENTE(
            "Muito diferente",
            "Alguma coisa é gritantemente diferente. Bônus dividido por 3, sem mínimo."
        )
    }

    /** Se a ficha tem Visualização — o botão só aparece então. */
    fun tem(personagem: Personagem): Boolean =
        personagem.vantagensTotais.any { it.definicaoId == ID }

    /**
     * O bônus final.
     *
     * [margem] é a margem de sucesso do teste de IQ (alvo − resultado). Margem
     * negativa ou zero significa que ele **fracassou** ou empatou: não há bônus.
     */
    fun bonusDe(margem: Int, semelhanca: Semelhanca): Int {
        if (margem <= 0) return 0
        return when (semelhanca) {
            Semelhanca.QUASE_IDENTICO -> margem
            // "reduzido pela metade (no mínimo +1)"
            Semelhanca.PARECIDO -> (margem / 2).coerceAtLeast(1)
            // "divida o bônus por 3 (sem um valor mínimo)"
            Semelhanca.MUITO_DIFERENTE -> margem / 3
        }
    }

    /**
     * A explicação da conta, para a tela poder mostrar de onde veio o número.
     *
     * Mesma razão das notinhas de origem: um bônus que o jogador não consegue
     * conferir é um bônus em que ele não confia.
     */
    fun explicacao(margem: Int, semelhanca: Semelhanca): String {
        if (margem <= 0) return "Fracassou no teste de IQ — sem bônus."
        val bonus = bonusDe(margem, semelhanca)
        return when (semelhanca) {
            Semelhanca.QUASE_IDENTICO -> "Margem $margem → +$bonus"
            Semelhanca.PARECIDO ->
                if (margem / 2 < 1) "Margem $margem ÷ 2 = 0, mas o mínimo é +1 → +$bonus"
                else "Margem $margem ÷ 2 → +$bonus"
            Semelhanca.MUITO_DIFERENTE ->
                if (bonus == 0) "Margem $margem ÷ 3 = 0, e aqui não há mínimo → sem bônus"
                else "Margem $margem ÷ 3 → +$bonus"
        }
    }

    /** O aviso que precisa estar na tela. */
    const val AVISO_COMBATE =
        "Não vale em combate: um minuto de concentração não cabe num turno (MB p.99)."

    /** O rótulo do bônus guardado, para o jogador não esquecer que tem um. */
    fun rotuloGuardado(bonus: Int, alvo: String): String =
        "Visualização guardada: +$bonus em $alvo"
}
