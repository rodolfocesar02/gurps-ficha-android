package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * Pares de traços que o livro **proíbe** de conviver na mesma ficha.
 *
 * Hoje só existe um: **Abascanto e Aptidão Mágica** (MB p.85) — *"Esta vantagem
 * não pode ser combinada com Aptidão Mágica"*. Quem resiste à magia não
 * consegue lançá-la.
 *
 * ## O critério para entrar aqui
 *
 * ⚠️ **Só o que o livro PROÍBE.** Pré-requisito "a critério do Mestre" **não é
 * trava** — foi assim que o `conhecimento_oculto` acabou bloqueando uma compra
 * legítima e teve de ser corrigido em 28/07. A pergunta para cada par novo é:
 * *o livro proíbe, ou o Mestre decide?* Só o primeiro caso vira bloqueio; o
 * segundo, no máximo, vira aviso.
 *
 * ## O que a trava NÃO faz
 *
 * ⚠️ **Vale para adicionar, nunca para ficha já salva.** Uma ficha antiga com as
 * duas continua abrindo normalmente — bloquear a abertura seria perder a ficha
 * do jogador por causa de uma regra que entrou depois.
 *
 * Vive em `domain/rules/` para ser Kotlin puro e testável: o delegate que a
 * consome precisa de `Context` do Android, e a regra não.
 */
object IncompatibilidadeDeTracos {

    const val ID_ABASCANTO = "abascanto_resistencia_a_magia"
    const val ID_APTIDAO_MAGICA = "aptidao_magica"

    /** Um par proibido, com o texto que o jogador vai ler. */
    data class Par(val umId: String, val outroId: String, val motivo: String)

    val PARES: List<Par> = listOf(
        Par(
            ID_ABASCANTO, ID_APTIDAO_MAGICA,
            "Abascanto não combina com Aptidão Mágica (MB p.85): quem resiste à " +
                "magia não consegue lançá-la. Remova uma para adicionar a outra."
        )
    )

    /**
     * A mensagem de recusa ao tentar adicionar [novoId], ou null se pode entrar.
     *
     * A checagem é simétrica de propósito: tanto faz qual das duas o jogador
     * comprou primeiro.
     */
    fun motivoParaRecusar(personagem: Personagem, novoId: String): String? {
        val jaTem = { id: String ->
            personagem.vantagens.any { it.definicaoId == id } ||
                personagem.desvantagens.any { it.definicaoId == id }
        }
        return PARES.firstOrNull { par ->
            (novoId == par.umId && jaTem(par.outroId)) ||
                (novoId == par.outroId && jaTem(par.umId))
        }?.motivo
    }
}
