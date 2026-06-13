package com.gurps.ficha.domain.saga

/**
 * Lote 354 (Saga A5): validador de saída do Narrador v1 (padrão Auto-Healing do Lote 52).
 *
 * Lei de ferro nº 1: o Narrador não pode declarar resultado mecânico (dano, PV, margem)
 * sem uma tool call correspondente NO MESMO TURNO. Se a prosa final contém o padrão de um
 * número mecânico mas o turno não chamou nenhuma ferramenta que justifique, devolvemos UM
 * pedido de correção (uma única vez — não loopar).
 */
object NarradorOutputValidator {

    /**
     * Detecta menção a resultado MECÂNICO concreto: "5 de dano", "3 dano", "perde 4 PV",
     * "PV", "margem 2". Number seguido (opcionalmente de "de") de dano, ou PV, ou margem.
     * Não casa números soltos da prosa ("3 guardas", "duas horas").
     */
    private val PADRAO_MECANICO = Regex(
        "\\d+\\s*(?:de\\s+)?(?:dano|pv|pf)\\b|\\bmargem\\s+(?:de\\s+)?\\d+|\\b\\d+\\s+pontos?\\s+de\\s+dano",
        RegexOption.IGNORE_CASE
    )

    /** Tools cuja presença no turno LEGITIMA um número mecânico na prosa. */
    private val TOOLS_QUE_PRODUZEM_NUMERO = setOf(
        NarradorTools.TOOL_PEDIR_ROLAGEM,
        NarradorTools.TOOL_APLICAR_DANO,
        NarradorTools.TOOL_ACAO_NPC,
        NarradorTools.TOOL_APLICAR_CONDICAO,
        NarradorTools.TOOL_GASTAR_RECURSO
    )

    data class Resultado(
        val ok: Boolean,
        /** Instrução de correção a reenviar ao modelo quando ok == false. */
        val instrucaoCorrecao: String? = null
    )

    /**
     * @param prosaFinal texto que o Narrador devolveu ao jogador neste turno.
     * @param toolsChamadasNoTurno nomes das tools que o modelo chamou neste turno.
     */
    fun validar(prosaFinal: String, toolsChamadasNoTurno: Set<String>): Resultado {
        val temNumeroMecanico = PADRAO_MECANICO.containsMatchIn(prosaFinal)
        if (!temNumeroMecanico) return Resultado(ok = true)

        val temToolQueJustifica = toolsChamadasNoTurno.any { it in TOOLS_QUE_PRODUZEM_NUMERO }
        if (temToolQueJustifica) return Resultado(ok = true)

        return Resultado(
            ok = false,
            instrucaoCorrecao =
                "[CORREÇÃO OBRIGATÓRIA] Você declarou um resultado mecânico (dano, PV ou margem) " +
                "sem ter chamado a ferramenta que o produz NESTE turno. Reescreva: ou CHAME " +
                "pedir_rolagem/aplicar_dano para obter o número real, ou narre a cena SEM citar " +
                "qualquer número de dano/PV/margem, terminando numa escolha para o jogador."
        )
    }
}
