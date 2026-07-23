package com.gurps.ficha.domain.combat.subsistemas

import com.gurps.ficha.domain.combat.Combatente
import com.gurps.ficha.domain.combat.CombatSession
import com.gurps.ficha.domain.combat.DanoTipo
import com.gurps.ficha.domain.combat.HitLocationRules
import com.gurps.ficha.domain.combat.InjuryRules
import com.gurps.ficha.domain.combat.LocalAtaque
import com.gurps.ficha.domain.combat.ToleranciaFerimentos
import com.gurps.ficha.domain.magic.MagiaMecanica
import com.gurps.ficha.domain.magic.MagicMechanics
import kotlin.random.Random

/**
 * Lote MOTOR-2: **funil de dano mágico**, extraído do `CombatSession`.
 *
 * É a peça CENTRAL e COMPARTILHADA — as quatro entradas de dano de magia passam por aqui: magia
 * direta (`heroiConjurar`), área, feixe (Jatos, P9), explosão de projétil (P5) e o tique de zona.
 * Concentrar isso num lugar só era o pré-requisito para depois soltar projétil/feixe/área do motor:
 * enquanto o funil vivia dentro da classe, qualquer um desses blocos estava amarrado a ela.
 *
 * ## O que ele faz, em ordem (a ordem importa e é regra):
 *  1. IMUNIDADE por elemento (A1) — "torna-se imune ao fogo": não há dano a reduzir, não há dano.
 *  2. TIPO de criatura (A1-b) — "mortos-vivos não são afetados": a mágica nem incide.
 *  3. Rola (ou usa o bruto forçado da explosão, P5), aplica 1/2D (MEC-15), RD, tolerância.
 *  4. Condição embutida (Relâmpago atordoa, Concussão) — testa e impõe.
 *
 * ## Acoplamento (curto e visível): recebe do motor por lambda
 *  - `rdContraMagia` — a RD que vale contra esta mágica (trata "ignora"/"ignora_vestida"/normal).
 *  - `imporCondicao` — impõe a condição embutida (o motor tem o mapa condição→enum e a lista).
 *  - `random` — o MESMO RNG, para a sequência de dados sair idêntica.
 *
 * Nada de comportamento mudou: é o `aplicarDanoMagico` original, só de lugar.
 */
class DanoMagicoResolver(
    private val random: Random,
    private val rdContraMagia: (Combatente, MagiaMecanica?) -> Int,
    /** Impõe a condição embutida: (alvo, condicaoStr, log parcial, duracaoSeg). */
    private val imporCondicao: (Combatente, String?, StringBuilder, Int) -> Unit,
) {
    private fun rolar3d6(): Int = (1..3).sumOf { random.nextInt(1, 7) }

    /**
     * Aplica o dano da mágica em [alvo] e devolve os PV subtraídos. Escreve o relato em [sb].
     *
     * @param brutoForcado dano já rolado (P5: a explosão rola UMA vez e divide por vítima). null = rola.
     */
    fun aplicar(
        alvo: Combatente,
        energia: Int,
        mecanica: MagiaMecanica?,
        sb: StringBuilder,
        distanciaM: Int = 0,
        brutoForcado: Int? = null,
    ): Int {
        // 1. IMUNIDADE por elemento vem ANTES de rolar (A1). "Imune ao fogo" = não há dano.
        if (MagicMechanics.imuneAo(mecanica?.elementoDano, alvo.imunidades)) {
            sb.append(" ${alvo.nome} é IMUNE a ${mecanica?.elementoDano} — a mágica não o fere.")
            return 0
        }
        // 2. TIPO de criatura (A1-b): a mágica nem incide.
        if (MagicMechanics.naoAfetaTipo(mecanica, alvo.tipoCriatura.chave)) {
            sb.append(" ${alvo.nome} é ${alvo.tipoCriatura.rotulo} — esta mágica não o afeta.")
            return 0
        }
        val expr = if (mecanica?.danoPorEnergia != null)
            MagicMechanics.expandirDano(mecanica.danoPorEnergia, energia.coerceAtLeast(1), mecanica.energiaPorDado, mecanica.danoFixo)
        else "${energia.coerceAtLeast(1)}d"
        val tipo = when (mecanica?.tipoDano) {
            "corte" -> DanoTipo.CORT; "perf" -> DanoTipo.PERF
            else -> DanoTipo.CONT // queimadura/contusão/projeção → ×1 (sem enum de queimadura)
        }
        val rd = rdContraMagia(alvo, mecanica) // MEC-38 (P7)
        val brutoCheio = brutoForcado ?: CombatSession.rolarDano(expr, random)
        // MEC-15: a partir de 1/2D (INCLUSIVE) o dano BÁSICO — antes da RD — cai pela metade.
        val bruto = MagicMechanics.aplicarMeioDano(brutoCheio, mecanica, distanciaM)
        val meioDano = bruto < brutoCheio
        if (meioDano) sb.append(" (além de ${mecanica?.alcanceMeioDano}m: metade do dano)")
        val dn = HitLocationRules.aplicarDano(alvo.pvMax, bruto, tipo, LocalAtaque.TORSO, rd,
            alvo.stats?.tolerancia ?: ToleranciaFerimentos.NORMAL)
        InjuryRules.ferir(alvo, dn.pvSubtrair, alvo.htEfetivo, random)
        sb.append(" $expr → ${dn.pvSubtrair} de dano em ${alvo.nome}" + (if (!alvo.vivo) " (fora de combate!)" else "") + ".")
        // Condição embutida (Relâmpago atordoa: HT −1 por 2 PV; Concussão: HT−3). Testa e impõe.
        if (mecanica?.condicao != null && alvo.vivo && dn.pvSubtrair > 0) {
            val pen = MagicMechanics.penalidadeCondicaoPorPv(mecanica.condicaoResistencia, dn.pvSubtrair)
            // MEC-15: além do 1/2D o alvo resiste à atribulação com +3.
            val ht = alvo.htEfetivo + pen + (if (meioDano) 3 else 0)
            if (rolar3d6() > ht) imporCondicao(alvo, mecanica.condicao, sb, MagicMechanics.duracaoCondicaoSeg(mecanica, energia))
            else sb.append(" (${alvo.nome} resiste à condição, HT $ht).")
        }
        return dn.pvSubtrair
    }
}
