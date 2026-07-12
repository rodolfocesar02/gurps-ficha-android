package com.gurps.ficha.domain.combat.hex

/**
 * Lote HEX-4 (Fase 2c do PILAR): fecha a divergência do HEX-3 sem tocar em CombatSession/CombatEncounter.
 *
 * Kotlin PURO. Quando uma manobra do motor MUTA `distanciaAoHeroi` (Encontrão força =1, Empurrão knockback,
 * Mover, npcResolve aproximar/afastar), a POSIÇÃO EM HEX do NPC no [HexCombatState] precisa refletir a
 * mudança. Esta função projeta a nova distância em metros de volta para a grade, movendo o NPC na LINHA
 * RETA herói↔NPC até que a distância em hex bata com a nova distância em metros (1 hex = 1 metro).
 *
 * O caller (a UI tática do HEX-5) invoca isto DEPOIS de cada ação do motor: pega a nova
 * `encounter.distancia(npc)` e chama [aplicarNovaDistancia].
 */
object HexPortabilidade {

    /**
     * Reposiciona o [idNpc] no [estado] para que a distância em hex ao herói bata com [novaDistanciaMetros].
     *
     * Estratégia: caminha na LINHA RETA da posição atual do NPC em direção ao herói (aproxima) ou no vetor
     * oposto (afasta), passo a passo, mantendo a orientação original do NPC. Se algum hex do caminho estiver
     * ocupado, para no último livre (colisão respeitada). Se o NPC ou o herói não estão no estado, retorna
     * `this`.
     *
     * NÃO muda o facing do NPC (afastar por knockback não vira o NPC — ele foi empurrado).
     */
    fun aplicarNovaDistancia(estado: HexCombatState, idNpc: String, novaDistanciaMetros: Int,
                             idHeroi: String = "heroi"): HexCombatState {
        val pNpc = estado.posicoes.firstOrNull { it.id == idNpc } ?: return estado
        val pHeroi = estado.posicoes.firstOrNull { it.id == idHeroi } ?: return estado
        val distAtual = pNpc.posicao.distancia(pHeroi.posicao)
        val alvo = novaDistanciaMetros.coerceAtLeast(0)
        if (distAtual == alvo) return estado

        // Ocupações (para respeitar colisão ao mover em linha reta).
        val ocupados = estado.posicoes.filter { it.id != idNpc }.map { it.posicao }.toSet()

        var atual = pNpc.posicao
        if (alvo < distAtual) {
            // APROXIMAR: caminha na linha reta NPC→herói.
            val linha = HexGrid.linhaReta(pNpc.posicao, pHeroi.posicao) // inclui as pontas
            // O primeiro item é a posição atual do NPC; iteramos os PRÓXIMOS.
            for (i in 1 until linha.size) {
                if (atual.distancia(pHeroi.posicao) == alvo) break
                val proximo = linha[i]
                // Nunca "atropela" o herói — se o próximo é a posição do herói, para antes.
                if (proximo == pHeroi.posicao) break
                if (proximo in ocupados) break // colisão: para no último livre
                atual = proximo
            }
        } else {
            // AFASTAR: caminha na direção OPOSTA a herói→NPC, hex a hex, até chegar em alvo.
            // Direção a partir do herói na direção do NPC = direção de recuo do NPC.
            val dir = Direcao.de(pHeroi.posicao, pNpc.posicao) ?: return estado
            val faltam = alvo - distAtual
            for (i in 0 until faltam) {
                val proximo = atual + dir.vetor
                if (proximo in ocupados) break
                atual = proximo
            }
        }

        // Lote TOK-4 (achado da revisão adversarial): se a linha reta foi BLOQUEADA por colisão e a
        // distância ainda não bate, desvia para um hex LIVRE do ANEL na distância-alvo (o mais
        // próximo da posição atual). Sem isso, a divergência grid≠encounter era ESCRITA DE VOLTA no
        // encounter no próximo Mover tático do herói (NPC "teleportava" de 1m pra 2m+ sem ação).
        if (atual.distancia(pHeroi.posicao) != alvo && alvo >= 1) {
            val anelLivre = HexGrid.range(pHeroi.posicao, alvo)
                .filter { it.distancia(pHeroi.posicao) == alvo && it !in ocupados }
                .minByOrNull { it.distancia(pNpc.posicao) }
            if (anelLivre != null) atual = anelLivre
        }

        if (atual == pNpc.posicao) return estado
        val novas = estado.posicoes.map { if (it.id == idNpc) it.copy(posicao = atual) else it }
        return estado.copy(posicoes = novas)
    }
}
