package com.gurps.ficha.domain.combat.hex

import com.gurps.ficha.domain.combat.CombatEncounter
import com.gurps.ficha.domain.combat.Combatente

/**
 * Lote HEX-3 (Fase 2b do PILAR): sincroniza a DISTÂNCIA EM METROS do CombatEncounter a partir da POSIÇÃO EM
 * HEX do HexCombatState. Convenção do plano: 1 hex = 1 metro (MB p.366). Kotlin PURO.
 *
 * ⚠️ USO — SÓ para SETUP (uma vez, ao ABRIR o combate na grade). NÃO chame antes de cada ação: o
 * CombatSession JÁ MUTA `encounter.distanciaAoHeroi` durante várias resoluções (heroiMove, heroiMoverEAtacar,
 * heroiEncontrao força =1, heroiEmpurrao aplica knockback, heroiTroca, npcResolve aproxima/afasta). Chamar o
 * sync depois DESFARIA silenciosamente essas mudanças (bug pego pela revisão adversarial do HEX-3).
 *
 * DIVERGÊNCIA HONESTA DO PLANO: o pilar original planejava "trocar a distância-única por posição em hex por
 * combatente" (fonte única de verdade posicional). HEX-3 faz APENAS a projeção de setup — a portabilidade das
 * manobras que mexem em distância (Encontrão passo=1, Avançar-e-Atacar, Empurrão, Troca, npcResolve
 * aproximar/afastar) para operar via HexCombatState.mover fica registrada como pré-requisito do HEX-4/5 (a UI
 * de round tático via hex agrupa naturalmente essa portabilidade com facing/costas).
 *
 * Durante o modo faixas (padrão), esta sincronia NÃO é chamada — nenhuma regressão.
 */
object HexCombatSync {

    /**
     * SETUP INICIAL: projeta as distâncias em metros no [encounter] a partir das posições em hex.
     * ⚠️ Chame UMA VEZ, ao abrir o combate na grade — depois disso, quem manda em `encounter.distanciaAoHeroi`
     * é o CombatSession (as manobras Encontrão/Empurrão/Mover etc. mutam esse mapa e não devem ser sobrescritas).
     *
     * Combatentes ausentes do [estado] são ignorados (mantêm distância anterior).
     */
    fun projetarSetupInicial(estado: HexCombatState, encounter: CombatEncounter, idHeroi: String = "heroi") {
        val posHeroi = estado.posicoes.firstOrNull { it.id == idHeroi }?.posicao ?: return
        for (c in encounter.combatentes) {
            if (c.ehHeroi) continue
            val pNpc = estado.posicoes.firstOrNull { it.id == c.id }?.posicao ?: continue
            val dist = posHeroi.distancia(pNpc) // hexes = metros
            encounter.definirDistancia(c.id, dist)
        }
    }

    /** Projeção one-shot para 1 combatente — útil em testes/demos. Mesma restrição: só p/ SETUP. */
    fun projetarUm(estado: HexCombatState, encounter: CombatEncounter, npc: Combatente, idHeroi: String = "heroi") {
        val posHeroi = estado.posicoes.firstOrNull { it.id == idHeroi }?.posicao ?: return
        val pNpc = estado.posicoes.firstOrNull { it.id == npc.id }?.posicao ?: return
        encounter.definirDistancia(npc.id, posHeroi.distancia(pNpc))
    }
}
