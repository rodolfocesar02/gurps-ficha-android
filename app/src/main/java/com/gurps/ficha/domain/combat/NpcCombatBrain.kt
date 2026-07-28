package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.rules.LocalAtaque

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
        val motivo: String,
        /** Lote MA-7: mágica que o NPC conjurador vai lançar neste turno (null = ação mundana). */
        val conjurar: NpcMagia? = null
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

        // Estado muito restrito (ex.: atordoado/dormindo/paralisado → só defesa ou nada): postura defensiva.
        if (Manobra.MOVER !in legais) {
            val m = if (Manobra.DEFESA_TOTAL in legais) Manobra.DEFESA_TOTAL else legais.first()
            return IntencaoNpc(m, motivo = "incapacitado/limitado — não ataca")
        }
        // Lote COND-1: amedrontado — foge do herói (medo/pânico), sem atacar.
        if (Condicao.AMEDRONTADO in npc.condicoes) {
            return IntencaoNpc(Manobra.MOVER, recuar = true, motivo = "amedrontado — recua do herói")
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

        // 1.5) CONJURADOR (Lote MA-7): se tem mágica ofensiva e fôlego, conjura no herói. Projéteis têm
        // alcance longo; se estiver colado (engajado), recua um passo primeiro (não gosta de melee).
        val magiaEscolhida = stats?.magias?.firstOrNull { npc.pfAtual >= it.custoFP }
        if (magiaEscolhida != null && dist >= 1) {
            if (engaj && dist <= 1) return IntencaoNpc(Manobra.MOVER, recuar = true,
                motivo = "conjurador colado — abre distância para lançar")
            return IntencaoNpc(Manobra.CONCENTRAR, alvoId = alvoId, aDistancia = true,
                conjurar = magiaEscolhida, motivo = "conjura ${magiaEscolhida.nome} no herói (${dist}m)")
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
            // Luta agarrada NPC→herói (Lote 422, MB p.370/371): um NPC DESARMADO (luta natural — fera/lutador)
            // pode prender o herói em vez de só golpear. Se já o agarrou, IMOBILIZA; senão, tenta AGARRAR.
            val desarmadoNpc = stats?.armaNome.isNullOrBlank()
            val alvo = encounter.combatentes.firstOrNull { it.id == alvoId }
            val heroiAgarrado = alvo != null && Condicao.AGARRADO in alvo.condicoes
            val heroiImobilizado = alvo != null && Condicao.IMOBILIZADO in alvo.condicoes
            if (desarmadoNpc && !heroiImobilizado) {
                // Já agarrou o herói: brutos agressivos partem para a chave/mata-leão (Lote PONTE-1); senão imobilizam.
                if (heroiAgarrado) return when {
                    agress >= 8 -> IntencaoNpc(Manobra.MATA_LEAO, alvoId, motivo = "mata-leão no herói agarrado")
                    agress >= 6 -> IntencaoNpc(Manobra.CHAVE_MEMBRO, alvoId, motivo = "chave no herói agarrado")
                    else -> IntencaoNpc(Manobra.IMOBILIZAR, alvoId, motivo = "imobiliza o herói agarrado")
                }
                if (random.nextInt(2) == 0) return IntencaoNpc(Manobra.AGARRAR, alvoId, motivo = "tenta agarrar o herói")
            }
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
