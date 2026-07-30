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
        ),

        // --- Lote D-PAR: os oito pares que a leitura das desvantagens revelou.
        // Todos com a frase "não é possível adquirir ambos" (ou equivalente) no
        // texto do livro. Até aqui o app travava UM par; o livro dá nove.

        // 🔴 O mais grave da lista: Reflexos em Combate é das vantagens mais
        // compradas do jogo. Sem esta trava dava para ter as duas, e o app somava
        // +1 nas defesas e -2 no pânico ao mesmo tempo — com a ficha achando que
        // estava tudo certo.
        Par(
            "paralisia_frente_ao_combate", "reflexos_em_combate",
            "Paralisia Frente ao Combate é o oposto de Reflexos em Combate " +
                "(MB p.153) — quem paralisa não tem reflexos apurados."
        ),
        Par(
            "voz_irritante", "voz_melodiosa",
            "Voz Irritante é o oposto de Voz Melodiosa (MB p.162). A mesma voz " +
                "não pode atrair e afastar ao mesmo tempo."
        ),
        Par(
            "temor", "destemor",
            "Temor é o oposto de Destemor (MB p.159): um soma na Vontade contra " +
                "o medo, o outro subtrai."
        ),
        Par(
            "atrapalhado", "destreza_manual_elevada",
            "Atrapalhado é o oposto de Destreza Manual Elevada (MB p.124) — as " +
                "duas mexem na mesma lista de trabalhos delicados."
        ),
        Par(
            "cegueira_noturna", "visao_noturna",
            "Cegueira Noturna não combina com Visão Noturna (MB p.127): uma " +
                "dobra a penalidade de escuridão, a outra cancela."
        ),
        Par(
            "cegueira_noturna", "visao_no_escuro",
            "Cegueira Noturna não combina com Visão no Escuro (MB p.127)."
        ),
        Par(
            "mao_fraca", "manuseadores_precarios",
            "Mão Fraca e Manuseadores Precários são mutuamente excludentes " +
                "(MB p.151): quem não tem mãos não pode ter a mão trêmula."
        ),
        Par(
            "mao_fraca", "sem_manuseadores",
            "Mão Fraca e Sem Manuseadores são mutuamente excludentes (MB p.151)."
        ),
        Par(
            "sem_nocao_de_profundidade", "zarolho",
            "Sem Noção de Profundidade tem efeitos idênticos aos de Zarolho " +
                "(MB p.156) — comprar as duas seria receber pontos duas vezes " +
                "pela mesma coisa."
        ),
        Par(
            "pouca_empatia", "insensivel",
            "Pouca Empatia não combina com Insensível (MB p.154): Insensível " +
                "ENTENDE as emoções e não se importa; Pouca Empatia não entende."
        ),
        Par(
            "pouca_empatia", "oblivio",
            "Pouca Empatia não combina com Oblívio (MB p.154): Oblívio entende " +
                "as emoções e não as motivações; Pouca Empatia não entende nada."
        ),
        Par(
            "suscetivel", "resistente",
            "Suscetível e Resistente ao mesmo objeto se anulam (MB p.159). " +
                "⚠️ O livro proíbe só quando é o MESMO objeto — Suscetível a " +
                "Veneno com Resistente a Doença é legítimo. O app não guarda o " +
                "objeto, então avisa; a decisão é do Mestre."
        ),
        Par(
            "susceptibilidade_a_magia", ID_ABASCANTO,
            "Suscetibilidade à Magia não combina com Abascanto (MB p.159): uma " +
                "facilita ser enfeitiçado, a outra dificulta."
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
            personagem.vantagensTotais.any { it.definicaoId == id } ||
                personagem.desvantagensTotais.any { it.definicaoId == id }
        }
        return PARES.firstOrNull { par ->
            (novoId == par.umId && jaTem(par.outroId)) ||
                (novoId == par.outroId && jaTem(par.umId))
        }?.motivo
    }
}
