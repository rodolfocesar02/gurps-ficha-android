package com.gurps.ficha.domain.combat.hex

import com.gurps.ficha.domain.combat.Manobra
import com.gurps.ficha.domain.combat.NpcCombatBrain

/**
 * Lote HEX-5 (Fase 3 do PILAR): IA POSICIONAL do NPC — decide o HEX de destino a partir da intenção do
 * [NpcCombatBrain] existente. Kotlin PURO.
 *
 * O NpcCombatBrain original decide MANOBRA (ATAQUE / ATAQUE_TOTAL / MOVER / NAO_FAZER_NADA / DEFESA_TOTAL)
 * usando só distância. Este objeto COMPLEMENTA: quando a manobra é MOVER (ou MOVER_E_ATACAR), escolhe o hex
 * de destino considerando FLANQUEAR (pegar as costas do herói), KITE (arqueiro mantém distância), COBERTURA
 * (mover-se para hex atrás de obstáculo) e FOCO (aproximar do herói). NÃO altera a manobra do brain — só
 * adiciona a coordenada de destino.
 *
 * O caller (UI do HEX-6 / controlador tático) pega este destino, chama o motor original para resolver a
 * manobra (que muta encounter.distanciaAoHeroi), depois usa [HexPortabilidade.aplicarNovaDistancia] para
 * sincronizar a posição de volta na grade.
 */
object HexTaticaNpc {

    /** Perfil do NPC para a IA posicional — extraído do NpcStats já existente. */
    data class PerfilTatico(
        val agressividade: Int,
        val moral: Int,
        val alcanceArmaMetros: Int,
        val temArmaDistancia: Boolean
    )

    /**
     * Decide o hex de destino do [npcId] considerando a intenção do brain [intencao], as posições do
     * [estado], o [perfil] e a lista de hexes que **bloqueiam LINHA DE VISÃO** ([hexesComCobertura]).
     * Retorna `null` se o NPC não deve mover (manobra ≠ MOVER/MOVER_E_ATACAR ou não há destino melhor).
     */
    fun decidirDestino(
        estado: HexCombatState,
        npcId: String,
        intencao: NpcCombatBrain.IntencaoNpc,
        perfil: PerfilTatico,
        idHeroi: String = "heroi",
        hexesComCobertura: Set<HexCoord> = emptySet()
    ): HexCoord? {
        // NPC só se move quando a intenção envolve movimento — para os demais, mantém posição.
        val precisaMover = intencao.manobra == Manobra.MOVER || intencao.manobra == Manobra.MOVER_E_ATACAR
        if (!precisaMover) return null

        val pNpc = estado.posicoes.firstOrNull { it.id == npcId } ?: return null
        val pHeroi = estado.posicoes.firstOrNull { it.id == idHeroi } ?: return null
        val ocupados = estado.posicoes.filter { it.id != npcId }.map { it.posicao }.toSet()

        // Candidatos: os hexes adjacentes do NPC + o hex atual (ficar). Filtra por não-ocupado.
        val candidatos = (HexGrid.vizinhos(pNpc.posicao) + pNpc.posicao)
            .filter { it == pNpc.posicao || it !in ocupados }

        val heroiFacing = pHeroi.facing

        // Estratégia por perfil:
        //  - RECUAR (moral baixa): maximiza distância.
        //  - ARQUEIRO (temArmaDistancia com alcance ≥ 3): KITE — manter distância no alcance máximo, buscando LoS.
        //  - CORPO-A-CORPO agressivo: aproximar; se dá pra FLANQUEAR (chegar em hex de FLANCO/COSTAS do herói),
        //    prefere flanquear em vez de vir de frente.
        //  - PADRÃO: aproximar.
        return when {
            intencao.recuar -> escolherRecuar(candidatos, pHeroi.posicao)
            perfil.temArmaDistancia && perfil.alcanceArmaMetros >= 3 ->
                escolherKite(candidatos, pHeroi.posicao, perfil.alcanceArmaMetros, hexesComCobertura)
            perfil.agressividade >= 6 ->
                escolherFlanquear(candidatos, pHeroi.posicao, heroiFacing)
                    ?: escolherAproximar(candidatos, pHeroi.posicao)
            else -> escolherAproximar(candidatos, pHeroi.posicao)
        }
    }

    /** Recuar: maximiza distância. Empate → mantém posição. */
    private fun escolherRecuar(candidatos: List<HexCoord>, heroi: HexCoord): HexCoord? =
        candidatos.maxByOrNull { it.distancia(heroi) }

    /** Aproximar: minimiza distância. Empate → prefere um vizinho (não ficar parado). */
    private fun escolherAproximar(candidatos: List<HexCoord>, heroi: HexCoord): HexCoord? {
        val minDist = candidatos.minOf { it.distancia(heroi) }
        val bestSet = candidatos.filter { it.distancia(heroi) == minDist }
        // Se o hex atual está entre os melhores mas há outros iguais em distância, escolhe outro para se mover.
        return bestSet.firstOrNull { true } // qualquer um; determinístico pela ordem dos vizinhos
    }

    /**
     * Kite: arqueiro tenta ficar EXATAMENTE no alcance da arma, buscando LoS (candidato com cobertura ADJACENTE
     * ao seu destino é preferido — atira e volta ao esconderijo).
     */
    private fun escolherKite(candidatos: List<HexCoord>, heroi: HexCoord, alcance: Int,
                             hexesComCobertura: Set<HexCoord>): HexCoord? {
        // Ordena por: (a) distância mais próxima do alcance ideal; (b) tem cobertura vizinha.
        return candidatos
            .map { c ->
                val distDoIdeal = kotlin.math.abs(c.distancia(heroi) - alcance)
                val temCoberturaVizinha = HexGrid.vizinhos(c).any { it in hexesComCobertura }
                Triple(c, distDoIdeal, temCoberturaVizinha)
            }
            .sortedWith(compareBy({ it.second }, { !it.third })) // menor delta, cobertura primeiro
            .firstOrNull()?.first
    }

    /**
     * Flanquear: procura um candidato que caia em [Facing.FLANCO] ou [Facing.COSTAS] do herói. Prefere
     * COSTAS (anula defesa) mas só se o hex é ADJACENTE ao herói (ataque corpo-a-corpo). Se nenhum candidato
     * consegue flanquear, retorna `null` (o caller cai no aproximar).
     */
    private fun escolherFlanquear(candidatos: List<HexCoord>, heroi: HexCoord, heroiFacing: Direcao): HexCoord? {
        val opcoesComFacing = candidatos.mapNotNull { c ->
            if (c == heroi) return@mapNotNull null // não ocupa o mesmo hex
            val facing = HexGrid.facingDoAtaque(origemAtaque = c, alvo = heroi, facingAlvo = heroiFacing)
            val dist = c.distancia(heroi)
            if (facing == Facing.FRENTE) null else Triple(c, facing, dist)
        }
        // Prefere COSTAS + adjacente; depois FLANCO + adjacente; depois COSTAS mais longe; depois FLANCO mais longe.
        return opcoesComFacing
            .sortedWith(compareBy(
                { if (it.second == Facing.COSTAS) 0 else 1 }, // costas < flanco
                { it.third } // menor distância
            ))
            .firstOrNull()?.first
    }
}
