package com.gurps.ficha.domain.combat.subsistemas

import com.gurps.ficha.domain.combat.Combatente
import com.gurps.ficha.domain.combat.CombatSession
import com.gurps.ficha.domain.combat.SagaLog
import com.gurps.ficha.domain.magic.ContextoConjuracao
import com.gurps.ficha.domain.magic.MagicMechanics
import kotlin.random.Random

/**
 * Lote MOTOR-3: **como a magia à distância ACERTA e é DEFENDIDA** — extraído do `CombatSession`.
 *
 * É a camada de "ataque mágico à distância" que fica ENTRE a conjuração (que continua no motor,
 * porque é o coração — teste de NH, custo, PF, mira) e o funil de dano (MOTOR-2, `DanoMagicoResolver`).
 * Só foi possível soltar isto depois do funil sair: antes, o dano estava amarrado dentro do motor.
 *
 * Cobre:
 *  - **Feixe** (Jatos e Sopros, P9): DX−4/−2 ou Ataque Inato sem redutor; esquiva ou bloqueio, nunca aparar.
 *  - **Arremesso de projétil** (P6/P11): Ataque Inato ou DX + penalidade de distância + mira.
 *  - **Explosão do projétil** (P5): alvo cheio + respingo dividido por `3×distância`; dado rolado uma vez.
 *
 * Acoplamento por lambda (o motor tem estas peças; o resolver só as usa): o RNG, o funil de dano,
 * o NH de Ataque Inato e a DX do herói, e as defesas do NPC (esquiva/bloqueio/"se defendeu").
 * O ponto de injeção da OCUPAÇÃO do respingo (`vizinhosDoImpacto`) mora aqui, como o `ocupantesDaZona`
 * mora no ZonaDelegate — o controller o troca pelo cálculo real por hex.
 *
 * Nada de comportamento mudou: é o `resolverArremessoProjetil`/`resolverFeixe`/`resolverExplosao`
 * original, só de lugar.
 */
class AtaqueMagicoResolver(
    private val random: Random,
    private val danoMagico: DanoMagicoResolver,
    private val heroiNhAtaqueInato: () -> Int?,
    private val heroiDx: () -> Int,
    private val esquivaNpc: (Combatente) -> Int,
    private val bloqueioNpc: (Combatente) -> Int,
    /** true = o NPC conseguiu se defender (o motor trata BONECO/CONGELADO por dentro disto). */
    private val npcSeDefendeu: (valor: Int, rolagem: Int) -> Boolean,
) {
    private fun rolar3d6(): Int = (1..3).sumOf { random.nextInt(1, 7) }

    /**
     * Quem está PERTO do ponto de impacto (para o respingo da explosão), com a distância em metros.
     * Ponto de injeção: o controller substitui pelo cálculo real por hex. O alvo direto sai da lista.
     */
    var vizinhosDoImpacto: (Combatente) -> List<Pair<Combatente, Int>> = { emptyList() }

    /** Lote P9 — FEIXE. DX−penal ou Ataque Inato (sem redutor); esquiva/bloqueio, nunca aparar. */
    fun resolverFeixe(alvo: Combatente, energia: Int, ctx: ContextoConjuracao, sb: StringBuilder): Int {
        val mec = ctx.mecanica
        val penal = mec?.feixePenalidadeDx ?: 4
        val nhInato = heroiNhAtaqueInato()
        val nhAcerto = nhInato ?: (heroiDx() - penal)
        val comQue = if (nhInato != null) "Ataque Inato" else "DX−$penal (sem a perícia)"
        val rolAcerto = rolar3d6()
        if (rolAcerto > nhAcerto) {
            sb.append(" O jato passa longe ($comQue NH $nhAcerto, rolou $rolAcerto).")
            return 0
        }
        val esq = esquivaNpc(alvo)
        val podeBloquear = mec?.feixeBloqueavel ?: true
        val bloq = if (podeBloquear) bloqueioNpc(alvo) else 0
        val (rotulo, valor) = if (bloq > esq) "Bloqueio" to bloq else "Esquiva" to esq
        if (npcSeDefendeu(valor, rolar3d6())) {
            sb.append(" ${alvo.nome} se defende do jato ($comQue NH $nhAcerto, rolou $rolAcerto; $rotulo $valor).")
            return 0
        }
        sb.append(" O jato acerta ($comQue NH $nhAcerto, rolou $rolAcerto) —")
        return danoMagico.aplicar(alvo, energia, mec, sb, ctx.distanciaMetros)
    }

    /** Lote P6/P11 — arremesso do projétil: acerto (Ataque Inato/DX + distância + mira) → esquiva → dano/explosão. */
    fun resolverArremesso(
        alvo: Combatente, energia: Int, ctx: ContextoConjuracao, sb: StringBuilder, bonusPrecisao: Int = 0,
    ): Int {
        val nhInato = heroiNhAtaqueInato()
        val base = nhInato ?: heroiDx()
        val comQue = if (nhInato != null) "Ataque Inato" else "DX (sem a perícia)"
        val nhAcerto = base + CombatSession.penalidadeDistancia(ctx.distanciaMetros) + bonusPrecisao
        if (bonusPrecisao > 0) sb.append(" (mira: +$bonusPrecisao)")
        val rolAcerto = rolar3d6()
        if (rolAcerto > nhAcerto) {
            sb.append(" O projétil passa longe ($comQue NH $nhAcerto, rolou $rolAcerto).")
            return 0
        }
        val esq = esquivaNpc(alvo)
        if (npcSeDefendeu(esq, rolar3d6())) {
            sb.append(" ${alvo.nome} ESQUIVA do projétil ($comQue NH $nhAcerto, rolou $rolAcerto; Esquiva $esq).")
            return 0
        }
        sb.append(" Projétil acerta ($comQue NH $nhAcerto, rolou $rolAcerto) —")
        val divisor = ctx.mecanica?.explosaoDivisorPorMetro ?: 0
        if (divisor <= 0) return danoMagico.aplicar(alvo, energia, ctx.mecanica, sb, ctx.distanciaMetros)
        return resolverExplosao(alvo, energia, ctx, sb, divisor)
    }

    /**
     * Lote P5 — explosão do projétil. Alvo cheio; vizinhos dividem por `3×distância`. O dado rola
     * UMA vez (`brutoForcado`), senão não seria a mesma explosão.
     */
    private fun resolverExplosao(
        alvo: Combatente, energia: Int, ctx: ContextoConjuracao, sb: StringBuilder, divisor: Int,
    ): Int {
        val mec = ctx.mecanica
        val expr = MagicMechanics.expandirDano(
            mec?.danoPorEnergia ?: "1d", energia.coerceAtLeast(1), mec?.energiaPorDado ?: 1, mec?.danoFixo ?: false)
        val bruto = CombatSession.rolarDano(expr, random)
        val danoAlvo = danoMagico.aplicar(alvo, energia, mec, sb, ctx.distanciaMetros, brutoForcado = bruto)
        val respingos = vizinhosDoImpacto(alvo).mapNotNull { (v, distM) ->
            val brutoAqui = MagicMechanics.danoDaExplosao(bruto, distM, divisor)
            if (brutoAqui <= 0) return@mapNotNull null
            SagaLog.mecanica("explosão do projétil: ${v.nome} a ${distM}m do impacto — $bruto → $brutoAqui")
            val d = danoMagico.aplicar(v, energia, mec, StringBuilder(), 0, brutoForcado = brutoAqui)
            "${v.nome} $d"
        }
        if (respingos.isNotEmpty()) sb.append(" Respingo da explosão: ${respingos.joinToString(", ")}.")
        return danoAlvo
    }
}
