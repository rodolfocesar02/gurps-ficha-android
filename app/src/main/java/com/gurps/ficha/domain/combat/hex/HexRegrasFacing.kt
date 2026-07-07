package com.gurps.ficha.domain.combat.hex

import com.gurps.ficha.domain.combat.CombatResolver

/**
 * Lote HEX-4 (Fase 2c do PILAR): regras posicionais BASE via facing/costas na grade.
 *
 * Kotlin PURO — sem Android. Encapsula a aplicação das regras de MB p.374/375/390 e AM p.104 em cima do
 * resultado de [HexGrid.facingDoAtaque]. Não toca em CombatSession/CombatResolver: expõe FUNÇÕES que o caller
 * (a UI/tática do HEX-5) pluga NA HORA de mostrar as defesas e resolver a troca.
 *
 * Regra base GURPS:
 *   - Ataque pelas COSTAS → defesa ANULADA (MB p.374, equivale a `CombatResolver.defesaAnulada(surpresa=true)`;
 *     o caller precisa TAMBÉM setar surpresa=true no `resolverTroca`, senão a curva 3d6 ainda passa em 3-4).
 *   - Ataque pelo FLANCO → todas as defesas ativas sofrem −2 (MB p.390) E o BD do escudo NÃO se aplica
 *     (MB p.375 — "só quando o atacante vem pela frente"). O caller passa `bonusEscudoEmbutido` porque no
 *     Saga esse BD já está BAKED nas defesas de `opcoesDefesaHeroi` (esquiva/apara/bloqueio).
 *   - FRENTE → nada.
 */
object HexRegrasFacing {

    /**
     * Facing do ataque de [origem] contra [alvo], dado o [facingAlvo]. Wrapper mais legível de
     * [HexGrid.facingDoAtaque] para uso da camada de combate.
     */
    fun facingDoAtaque(origem: HexCoord, alvo: HexCoord, facingAlvo: Direcao): Facing =
        HexGrid.facingDoAtaque(origem, alvo, facingAlvo)

    /**
     * Aplica o facing ao valor de uma defesa base (esquiva/apara/bloqueio):
     *   FRENTE  → valor inalterado
     *   FLANCO  → valor − 2 − [bonusEscudoEmbutido] (MB p.375: BD só de frente; piso 0)
     *   COSTAS  → 0 (⚠️ caller DEVE também setar surpresa=true no `resolverTroca` — MB p.374 nega o 3-4)
     *
     * [bonusEscudoEmbutido]: 0 se o valor NÃO tem BD do escudo somado; senão o BD atual (`HeroiPerfilCombate.bonusEscudo`).
     */
    fun ajustarValorDefesa(valorBase: Int, facing: Facing, bonusEscudoEmbutido: Int = 0): Int = when (facing) {
        Facing.FRENTE -> valorBase
        Facing.FLANCO -> (valorBase - 2 - bonusEscudoEmbutido).coerceAtLeast(0)
        Facing.COSTAS -> 0
    }

    /** True quando o ataque anula a defesa por vir pelas costas (MB p.374). */
    fun defesaAnulada(facing: Facing): Boolean = facing.defesaAnulada

    /**
     * Aplica o facing a uma LISTA de [CombatResolver.OpcaoDefesa] já montada por
     * `CombatSession.opcoesDefesaHeroi`: em FLANCO subtrai −2 e −[bonusEscudoEmbutido] (MB p.375) de cada
     * opção, mantendo o INVARIANTE `soma(componentes) contribui ao valorFinal` — adiciona um `ComponenteMod`
     * nomeado "flanco" com o delta real, para o card "Defenda-se!" mostrar o abatimento por qual regra.
     * Em COSTAS devolve `emptyList()` (defesa anulada — MB p.374; caller precisa setar surpresa=true
     * no `resolverTroca` também).
     *
     * NÃO muta as opções originais — devolve novas instâncias (`copy`) preservando as flags recuo/
     * jogar-se-ao-chão/mão-inábil/acrobática que o motor calculou.
     */
    fun ajustarOpcoesDefesa(
        opcoes: List<CombatResolver.OpcaoDefesa>,
        facing: Facing,
        bonusEscudoEmbutido: Int = 0
    ): List<CombatResolver.OpcaoDefesa> {
        return when (facing) {
            Facing.FRENTE -> opcoes
            Facing.COSTAS -> emptyList()
            Facing.FLANCO -> opcoes.map { op ->
                val novaSoma = (op.valorFinal - 2 - bonusEscudoEmbutido).coerceAtLeast(0)
                val delta = novaSoma - op.valorFinal
                val componentes = op.componentes + CombatResolver.ComponenteMod("flanco", delta)
                op.copy(valorFinal = novaSoma, componentes = componentes)
            }
        }
    }
}
