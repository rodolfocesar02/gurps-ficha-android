package com.gurps.ficha.domain.combat.hex

import com.gurps.ficha.domain.combat.Condicao

/**
 * Lote TESTE-C: regras de movimento tático que viviam DENTRO do `SagaCombatController`.
 *
 * Por que foram movidas: o controller depende de `FichaViewModel`, que é um
 * `AndroidViewModel(Application)` — não instanciável na JVM. Logo nada dele entrava na suíte, e o
 * teste da virada final acabou **espelhando** a regra (uma cópia em `HexRegrasFacingTest`) em vez de
 * exercitá-la. Uma cópia passa verde mesmo que o original quebre: a real usa `deslocamentoEfetivo`
 * (afetado por carga/ferimento) e a cópia recebia um deslocamento cru, então trocar um pelo outro
 * quebraria o jogo sem derrubar teste nenhum.
 *
 * Aqui é Kotlin puro — sem Android, sem Compose, sem ViewModel.
 */
object RegrasMovimentoTatico {

    /**
     * MB p.388: *"O personagem pode se virar para QUALQUER direção se não usou mais que a METADE dos
     * seus pontos de movimento; se usou mais, ele pode mudar sua direção em apenas UM LADO DE
     * HEXÁGONO."*
     *
     * [deslocamentoEfetivo] é o Deslocamento JÁ descontado de carga e ferimento — não o valor cru
     * da ficha. Era exatamente essa distinção que o teste-cópia não conseguia proteger.
     */
    fun viradaFinalLivre(andou: Int, deslocamentoEfetivo: Int): Boolean =
        andou <= (deslocamentoEfetivo + 1) / 2

    /**
     * MB p.388: as direções que a virada de fim de movimento permite. Livre → todas as 6; senão a
     * atual mais UMA vizinha de cada lado (um lado de hexágono), dando a volta na roda.
     */
    fun direcoesDaViradaFinal(facingAtual: Direcao, livreParaQualquer: Boolean): List<Direcao> {
        val todas = Direcao.values().toList()
        if (livreParaQualquer) return todas
        val i = facingAtual.ordinal
        return listOf(facingAtual, todas[(i + 1) % 6], todas[(i + 5) % 6])
    }

    /**
     * O herói pode se deslocar pela GRADE agora?
     *
     * MB p.420: atordoado não se move. MB p.371: agarrado/imobilizado não desloca sem antes se
     * Desvencilhar. Magia multi-turno em andamento prende o operador (só continuar ou abortar).
     *
     * Isto é o que impede a grade de driblar a luta agarrada — sem a trava, bastava tocar um hex
     * verde e sair andando mesmo preso.
     */
    fun podeMoverNaGrade(condicoes: Set<Condicao>, conjurandoMultiTurno: Boolean): Boolean {
        if (conjurandoMultiTurno) return false
        return Condicao.ATORDOADO !in condicoes &&
            Condicao.AGARRADO !in condicoes &&
            Condicao.IMOBILIZADO !in condicoes
    }

    // ── Lote REFACTOR-1: julgamento que vivia SOLTO no SagaCombatController ─────────────────────
    // As funções abaixo respondem "o que é permitido agora?" a partir de dados SIMPLES (booleans,
    // conjuntos, o estado da grade). O controller só COLETA esses dados e OBEDECE — a decisão em si
    // deixou de estar amarrada ao ViewModel e virou testável na JVM. É isto que fecha, com teste, a
    // porta do TOK-8 (a virada final pendente bloqueando o movimento).

    /**
     * Por que o herói **não** pode mover na grade agora — ou `null` se pode.
     *
     * A ordem das checagens é a mesma do controller de origem, de propósito: uma mudança de ordem
     * mudaria qual aviso o jogador vê primeiro. `heroiSelecionado` = o token do herói está escolhido
     * na grade (senão a grade nem oferece movimento). [temViradaPendente] é a trava do TOK-8.
     */
    fun motivoNaoPodeMover(
        combateEncerrado: Boolean,
        ehTurnoDoHeroi: Boolean,
        temViradaPendente: Boolean,
        heroiSelecionado: Boolean,
        condicoes: Set<Condicao>,
        conjurandoMultiTurno: Boolean,
    ): MotivoBloqueio? = when {
        combateEncerrado || !ehTurnoDoHeroi -> MotivoBloqueio.NAO_E_SEU_TURNO
        temViradaPendente -> MotivoBloqueio.VIRADA_PENDENTE
        !heroiSelecionado -> MotivoBloqueio.HEROI_NAO_SELECIONADO
        !podeMoverNaGrade(condicoes, conjurandoMultiTurno) -> MotivoBloqueio.PRESO_OU_CONCENTRANDO
        else -> null
    }

    /** true se o herói pode mover agora (nenhum motivo de bloqueio). */
    fun podeMoverAgora(
        combateEncerrado: Boolean, ehTurnoDoHeroi: Boolean, temViradaPendente: Boolean,
        heroiSelecionado: Boolean, condicoes: Set<Condicao>, conjurandoMultiTurno: Boolean,
    ): Boolean = motivoNaoPodeMover(combateEncerrado, ehTurnoDoHeroi, temViradaPendente,
        heroiSelecionado, condicoes, conjurandoMultiTurno) == null

    enum class MotivoBloqueio { NAO_E_SEU_TURNO, VIRADA_PENDENTE, HEROI_NAO_SELECIONADO, PRESO_OU_CONCENTRANDO }

    /**
     * O que um TOQUE num hex significa. Decisão pura; o controller executa o [TipoDeToque] devolvido.
     *
     * A precedência importa e é a de origem: mira de área e virada pendente vêm ANTES de tudo, senão
     * o toque que deveria resolvê-las viraria movimento (foi metade do bug TOK-8).
     */
    fun interpretarToque(
        temMiraAreaPendente: Boolean,
        temViradaPendente: Boolean,
        hexTemToken: Boolean,
        heroiSelecionado: Boolean,
        podeMover: Boolean,
    ): TipoDeToque = when {
        temMiraAreaPendente -> TipoDeToque.RESOLVER_MIRA_AREA
        temViradaPendente -> TipoDeToque.AVISAR_ESCOLHA_DIRECAO
        hexTemToken -> TipoDeToque.SELECIONAR_TOKEN
        heroiSelecionado && podeMover -> TipoDeToque.MOVER
        else -> TipoDeToque.APENAS_DESTACAR
    }

    enum class TipoDeToque {
        RESOLVER_MIRA_AREA, AVISAR_ESCOLHA_DIRECAO, SELECIONAR_TOKEN, MOVER, APENAS_DESTACAR
    }
}
