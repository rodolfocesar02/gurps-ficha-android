package com.gurps.ficha.domain.combat.hex

/**
 * Lote TOK-4 (VTT 2D): montagem do estado tático REAL a partir do encontro + helpers de movimento
 * do herói pelo grid. Kotlin PURO.
 *
 * Substitui o "botão de faixa" (aproximar/afastar N metros) pelo toque no hex: o grid vira a fonte
 * das distâncias no turno de mover do herói (docs/fonte-regras/Combate.md "Deslocamento"/"Movimento"; o Passo de 1m e
 * o Espaçamento — antes FORA DO ESCOPO por falta de grade — passam a ser reais).
 */
object HexSetup {

    /**
     * Cena inicial do combate REAL: herói na ORIGEM olhando LESTE; cada inimigo a [distanciaM] hexes,
     * espalhados começando pelo LESTE e alternando pelas 6 direções (7º+ inimigo sobe um anel).
     * Colisão: se o hex calculado está ocupado, tenta o próximo anel (+1 hex na mesma direção).
     * Facing dos inimigos: olhando para o herói (direção oposta à posição).
     */
    fun setupDoEncontro(idsInimigos: List<String>, distanciaM: Int, raioGrade: Int = 7): HexCombatState {
        val posicoes = mutableListOf(PosicaoCombatente("heroi", HexCoord.ORIGEM, Direcao.LESTE))
        val ocupados = mutableSetOf(HexCoord.ORIGEM)
        val dist = distanciaM.coerceIn(1, raioGrade)
        idsInimigos.forEachIndexed { i, id ->
            val dir = Direcao.values()[i % 6]
            // Anel clampado à grade JÁ NO CÁLCULO (achado da revisão do TOK-4: a "trava de borda"
            // antiga resetava para um hex possivelmente OCUPADO, sobrepondo dois inimigos).
            val anel = (dist + i / 6).coerceAtMost(raioGrade)
            var pos = HexCoord(dir.vetor.q * anel, dir.vetor.r * anel)
            // Colisão: empurra pra DENTRO (na direção do herói) até achar livre (dist mínima 1).
            while (pos in ocupados && HexCoord.ORIGEM.distancia(pos) > 1) {
                pos = pos - dir.vetor
            }
            // Raio inteiro daquela direção tomado: pega o primeiro hex livre de toda a grade.
            if (pos in ocupados) {
                pos = HexGrid.range(HexCoord.ORIGEM, raioGrade)
                    .firstOrNull { it !in ocupados } ?: pos
            }
            ocupados.add(pos)
            val facing = Direcao.de(pos, HexCoord.ORIGEM) ?: Direcao.OESTE
            posicoes.add(PosicaoCombatente(id, pos, facing))
        }
        return HexCombatState(posicoes = posicoes, raioGrade = raioGrade)
    }

    /**
     * Hexes que o herói pode alcançar neste turno de Mover — BFS de custo 1 sobre hexes LIVRES:
     * o CAMINHO não atravessa hex ocupado por NINGUÉM (achado da revisão adversarial do TOK-4:
     * o range geométrico deixava o herói "atravessar" a linha inimiga de graça — fuga de cerco
     * sem rolagem; o docs/fonte-regras/Combate.md exige Evadir para passar por hex de INIMIGO). Conservador até o
     * TOK-5 relaxar aliados (atravessar aliado é livre, MB p.389) e implementar Evadir.
     */
    fun hexesAlcancaveis(estado: HexCombatState, deslocamento: Int, idHeroi: String = "heroi"): Set<HexCoord> {
        val pHeroi = estado.posicoes.firstOrNull { it.id == idHeroi } ?: return emptySet()
        if (deslocamento <= 0) return emptySet()
        val ocupados = estado.posicoes.filter { it.id != idHeroi }.map { it.posicao }.toSet()
        val visitados = mutableSetOf(pHeroi.posicao)
        var fronteira = listOf(pHeroi.posicao)
        repeat(deslocamento) {
            val proxima = mutableListOf<HexCoord>()
            for (h in fronteira) {
                for (viz in HexGrid.vizinhos(h)) {
                    if (viz in visitados || viz in ocupados) continue
                    if (HexCoord.ORIGEM.distancia(viz) > estado.raioGrade) continue
                    visitados.add(viz)
                    proxima.add(viz)
                }
            }
            fronteira = proxima
        }
        visitados.remove(pHeroi.posicao)
        return visitados
    }

    /**
     * Move o herói para [destino] (qualquer hex livre — o CALLER valida o alcance via
     * [hexesAlcancaveis]); facing vira a direção do deslocamento. Devolve o estado inalterado se o
     * destino está ocupado ou o herói não existe.
     */
    fun moverHeroi(estado: HexCombatState, destino: HexCoord, idHeroi: String = "heroi"): HexCombatState {
        val p = estado.posicoes.firstOrNull { it.id == idHeroi } ?: return estado
        if (estado.posicoes.any { it.id != idHeroi && it.posicao == destino }) return estado
        if (destino == p.posicao) return estado
        val facing = Direcao.de(p.posicao, destino) ?: p.facing
        val novas = estado.posicoes.map { if (it.id == idHeroi) it.copy(posicao = destino, facing = facing) else it }
        return estado.copy(posicoes = novas, hexSelecionado = destino, idSelecionado = idHeroi)
    }

    /**
     * Distâncias em metros (= hexes) de cada NPC ao herói — o mapa que alimenta o encounter depois
     * de um movimento pelo grid.
     */
    fun distanciasAoHeroi(estado: HexCombatState, idHeroi: String = "heroi"): Map<String, Int> {
        val pHeroi = estado.posicoes.firstOrNull { it.id == idHeroi } ?: return emptyMap()
        return estado.posicoes.filter { it.id != idHeroi }
            .associate { it.id to pHeroi.posicao.distancia(it.posicao) }
    }

    /** Remove do estado os combatentes cujos ids não estão em [vivos] (mortos saem da grade). */
    fun manterApenas(estado: HexCombatState, vivos: Set<String>, idHeroi: String = "heroi"): HexCombatState {
        val novas = estado.posicoes.filter { it.id == idHeroi || it.id in vivos }
        return if (novas.size == estado.posicoes.size) estado else estado.copy(posicoes = novas)
    }
}
