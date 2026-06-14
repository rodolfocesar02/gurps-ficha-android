package com.gurps.ficha.domain.combat

import kotlin.random.Random

/**
 * Lote 363 (Saga B6): cérebro tático do NPC. Dado o estado do encontro, decide a INTENÇÃO
 * (manobra/alvo/local) de forma determinística, por agressividade, moral, alcance da arma e
 * distância. É o FALLBACK quando o Narrador não especifica os detalhes em `acao_npc` (B8).
 * Kotlin puro.
 */
object NpcCombatBrain {

    data class IntencaoNpc(
        val manobra: Manobra,
        val alvoId: String? = null,
        val local: LocalAtaque = LocalAtaque.TORSO,
        val aDistancia: Boolean = false,
        val recuar: Boolean = false,
        val motivo: String
    )

    /** Fração de PV abaixo da qual a criatura tende a fugir (moral baixa foge antes). */
    fun limiarFugaPV(moral: Int): Double = ((10 - moral).coerceIn(0, 10)) * 0.05

    fun decidir(
        npc: Combatente,
        encounter: CombatEncounter,
        alvoId: String,
        random: Random = Random.Default
    ): IntencaoNpc {
        val legais = encounter.manobrasLegais(npc)
        if (legais.isEmpty()) return IntencaoNpc(Manobra.NAO_FAZER_NADA, motivo = "incapaz de agir")

        // Estado muito restrito (ex.: atordoado → só defesa): assume postura defensiva.
        if (Manobra.MOVER !in legais) {
            val m = if (Manobra.DEFESA_TOTAL in legais) Manobra.DEFESA_TOTAL else legais.first()
            return IntencaoNpc(m, motivo = "atordoado/limitado — defende-se")
        }

        val stats = npc.stats
        val agress = stats?.agressividade ?: 5
        val moral = stats?.moral ?: 5
        val alcance = stats?.alcanceMetros ?: 1
        val dist = encounter.distancia(npc)
        val engaj = encounter.engajado(npc)

        // 1) Moral: foge se o PV caiu abaixo do limiar.
        if (npc.pvAtual <= npc.pvMax * limiarFugaPV(moral)) {
            return IntencaoNpc(Manobra.MOVER, recuar = true,
                motivo = "moral baixa (PV ${npc.pvAtual}/${npc.pvMax}) — recua")
        }

        // 2) Arqueiro (arma de alcance): mantém distância e atira.
        if (alcance >= 3) {
            if (engaj) return IntencaoNpc(Manobra.MOVER, recuar = true, motivo = "arqueiro engajado — abre distância")
            if (dist <= alcance) return IntencaoNpc(Manobra.ATAQUE, alvoId = alvoId, aDistancia = true,
                motivo = "atira à distância (${dist}m, alcance $alcance)")
            return IntencaoNpc(Manobra.MOVER, motivo = "aproxima até o alcance da arma")
        }

        // 3) Corpo-a-corpo.
        if (engaj) {
            val usaTotal = agress >= 7 && Manobra.ATAQUE_TOTAL in legais
            val local = if (agress >= 8) LocalAtaque.VITAIS else LocalAtaque.TORSO
            return if (usaTotal) IntencaoNpc(Manobra.ATAQUE_TOTAL, alvoId, local, motivo = "ataque total (agressivo)")
            else IntencaoNpc(Manobra.ATAQUE, alvoId, local, motivo = "ataque corpo-a-corpo")
        }
        // Não engajado: avança.
        if (dist <= npc.deslocamento + 1 && Manobra.MOVER_E_ATACAR in legais) {
            return IntencaoNpc(Manobra.MOVER_E_ATACAR, alvoId, motivo = "avança e ataca")
        }
        return IntencaoNpc(Manobra.MOVER, motivo = "avança para o alvo (${dist}m)")
    }
}
