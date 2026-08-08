package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.rules.DanoTipo

import com.gurps.ficha.domain.rules.LocalAtaque

import kotlin.random.Random

/**
 * Lote 362 (Saga B4): estados vitais do GURPS 4ª ed. — choque, ferimento grave, checagens de
 * morte, inconsciência e recuperação de atordoamento. Kotlin puro. Referências // MB.
 */
object InjuryRules {

    /**
     * Choque (MB p.419/381): penalidade em DX/IQ no PRÓXIMO turno por PV perdidos. −1 por PV perdido; se
     * PV Inicial ≥ 20, −1 a cada (PVInicial/10) PV perdidos (arredondado p/ baixo). Teto −4. Não afeta defesas.
     */
    fun penalidadeChoque(pvPerdidos: Int, pvMax: Int): Int {
        if (pvPerdidos <= 0) return 0
        val unidade = if (pvMax >= 20) (pvMax / 10).coerceAtLeast(1) else 1
        return -minOf(pvPerdidos / unidade, 4)
    }

    /** Ferimento grave = mais da metade do PV num único golpe (> PV/2). MB p.420. */
    fun ehFerimentoGrave(dano: Int, pvMax: Int): Boolean = dano * 2 > pvMax

    enum class EfeitoFerimento { NENHUM, ATORDOADO_CAIDO, INCONSCIENTE, MORTO }

    data class ResultadoFerimento(
        val pvDepois: Int,
        val efeito: EfeitoFerimento,
        val logs: List<String>
    )

    private fun rolar3d6(r: Random) = (1..3).sumOf { r.nextInt(1, 7) }

    /**
     * Aplica um golpe (dano já penetrante/multiplicado) e resolve, em ordem:
     *  morte automática (≤ −5×PV) → checagens de morte (−1×..−4×PV recém-cruzados) →
     *  ferimento grave (HT: falha = atordoado+caído; falha por 5+ ou 18 = inconsciente) →
     *  inconsciência por PV ≤ 0. HT rolado via [random] (3d6).
     */
    fun aplicarGolpe(pvAntes: Int, pvMax: Int, ht: Int, dano: Int, random: Random, forcarFerimentoGrave: Boolean = false): ResultadoFerimento {
        val logs = mutableListOf<String>()
        val pvDepois = pvAntes - dano
        logs.add("Dano $dano: PV $pvAntes → $pvDepois")

        if (pvDepois <= -5 * pvMax) {
            logs.add("PV $pvDepois ≤ −5×PV ($pvMax): morte automática.")
            return ResultadoFerimento(pvDepois, EfeitoFerimento.MORTO, logs)
        }

        for (m in 1..4) {
            val limiar = -m * pvMax
            if (pvDepois <= limiar && pvAntes > limiar) {
                val soma = rolar3d6(random)
                val ok = soma <= ht
                logs.add("Cheque de morte (−${m}×PV): HT $ht, rolou $soma → ${if (ok) "resiste" else "MORRE"}")
                if (!ok) return ResultadoFerimento(pvDepois, EfeitoFerimento.MORTO, logs)
            }
        }

        var efeito = EfeitoFerimento.NENHUM
        if (ehFerimentoGrave(dano, pvMax) || forcarFerimentoGrave) {
            val soma = rolar3d6(random)
            efeito = when {
                soma <= ht -> { logs.add("Ferimento grave: HT $ht, rolou $soma → mantém-se."); EfeitoFerimento.NENHUM }
                soma >= ht + 5 || soma == 18 -> { logs.add("Ferimento grave: HT $ht, rolou $soma (falha por 5+) → INCONSCIENTE."); EfeitoFerimento.INCONSCIENTE }
                else -> { logs.add("Ferimento grave: HT $ht, rolou $soma → atordoado e caído."); EfeitoFerimento.ATORDOADO_CAIDO }
            }
        }

        if (pvDepois <= 0 && efeito == EfeitoFerimento.NENHUM) {
            val soma = rolar3d6(random)
            efeito = if (soma > ht) {
                logs.add("PV $pvDepois ≤ 0: HT $ht, rolou $soma → desmaia.")
                EfeitoFerimento.INCONSCIENTE
            } else {
                logs.add("PV $pvDepois ≤ 0: HT $ht, rolou $soma → continua consciente.")
                EfeitoFerimento.NENHUM
            }
        }

        return ResultadoFerimento(pvDepois, efeito, logs)
    }

    /** Recuperação de atordoamento no fim do turno: sucesso em HT remove o atordoamento. MB p.420. */
    fun recuperaAtordoamento(ht: Int, random: Random): Boolean = rolar3d6(random) <= ht

    /**
     * Aplica o resultado de um golpe diretamente a um [Combatente] (muta PV e condições).
     * Integração pedida no B4; o executor real `aplicar_dano` (B5) usa isto encadeado ao B3.
     */
    fun ferir(c: Combatente, dano: Int, ht: Int, random: Random, forcarFerimentoGrave: Boolean = false,
              tipo: DanoTipo? = null, local: LocalAtaque? = null): ResultadoFerimento {
        val r = aplicarGolpe(c.pvAtual, c.pvMax, ht, dano, random, forcarFerimentoGrave)
        c.pvAtual = r.pvDepois
        // Choque (Lote 382): acumula os PV perdidos; vira penalidade no próximo turno do combatente (MB p.419).
        if (dano > 0) c.choquePendente += dano
        when (r.efeito) {
            EfeitoFerimento.MORTO, EfeitoFerimento.INCONSCIENTE -> c.condicoes.add(Condicao.INCONSCIENTE)
            EfeitoFerimento.ATORDOADO_CAIDO -> { c.condicoes.add(Condicao.ATORDOADO); c.condicoes.add(Condicao.CAIDO) }
            EfeitoFerimento.NENHUM -> {}
        }
        // Sangramento (Lote PONTE-2, MB p.420 / AM p.138): corte/perfuração marca/agrava o sangramento (tipo/local
        // só vêm pelo funil de troca; default null = sem marcação, mantém call-sites antigos sem regressão).
        if (dano > 0 && c.vivo && tipo != null && local != null) {
            classificarSangramento(tipo, local)?.let { (pen, intervalo) ->
                if (!c.sangramentoAtivo) {
                    c.sangramentoAtivo = true; c.condicoes.add(Condicao.SANGRANDO)
                    c.sangramentoUltimaRodada = Int.MIN_VALUE; c.sangramentoTestesLimpos = 0
                }
                c.sangramentoLesaoPV = maxOf(c.sangramentoLesaoPV, dano)
                c.sangramentoPenalidadeLocal = maxOf(c.sangramentoPenalidadeLocal, pen)
                c.sangramentoIntervaloSeg = minOf(c.sangramentoIntervaloSeg, intervalo)
            }
        }
        return r
    }

    /**
     * Sangramento (MB p.420 / AM p.138): contusão NÃO sangra; corte/perfuração sim. Devolve (penalidade extra de
     * local, intervalo em segundos) ou null se não sangra. Locais graves (AM p.138) testam a cada 30s com penalidade.
     */
    fun classificarSangramento(tipo: DanoTipo, local: LocalAtaque): Pair<Int, Int>? {
        if (tipo == DanoTipo.CONT) return null
        return when (local) {
            LocalAtaque.VITAIS -> 4 to 30
            // Pescoço grave (−2, 30s) só vale para corte/perfuração; perfurantes (pi) caem no comum (AM p.138).
            LocalAtaque.PESCOCO -> if (tipo == DanoTipo.CORT || tipo == DanoTipo.PERF) 2 to 30 else 0 to 60
            LocalAtaque.CRANIO, LocalAtaque.OLHO -> 0 to 30
            else -> 0 to 60
        }
    }

    /**
     * Um teste de sangramento (chamado pelo motor quando fecha um intervalo). HT −(lesão/5) −penalidade de local.
     * Sucesso decisivo (≤4) ou 3 intervalos limpos = estanca; sucesso = não sangra neste intervalo; falha = perde
     * 1 PV (3 se falha crítica/18), reprocessando morte/inconsciência. Retorna o ResultadoFerimento ou null se não sangra.
     */
    fun tickSangramento(c: Combatente, ht: Int, random: Random): ResultadoFerimento? {
        if (!c.sangramentoAtivo) return null
        // Penalidade −1 a cada 5 PV de LESÃO ACUMULADA (déficit de PV atual) + penalidade de local grave (MB p.420 / AM p.138).
        val lesao = (c.pvMax - c.pvAtual).coerceAtLeast(0)
        val htEf = ht - (lesao / 5) - c.sangramentoPenalidadeLocal
        val soma = rolar3d6(random)
        val logs = mutableListOf<String>()
        return when {
            soma <= 4 -> { // sucesso decisivo → estanca de vez (MB p.420)
                estancarSangramento(c)
                logs.add("sangramento estanca (HT $htEf, rolou $soma — decisivo).")
                ResultadoFerimento(c.pvAtual, EfeitoFerimento.NENHUM, logs)
            }
            soma < 17 && soma <= htEf -> { // 3d6: 17/18 NUNCA são sucesso (cf. defesaBemSucedida). Não sangra neste intervalo.
                c.sangramentoTestesLimpos += 1
                if (c.sangramentoTestesLimpos >= 3) { estancarSangramento(c); logs.add("sangramento parou (3 intervalos sem sangrar).") }
                else logs.add("não sangra neste intervalo (HT $htEf, rolou $soma).")
                ResultadoFerimento(c.pvAtual, EfeitoFerimento.NENHUM, logs)
            }
            else -> { // falha (soma > htEf, ou auto-falha 17/18) → perde PV
                c.sangramentoTestesLimpos = 0
                // Falha crítica de HT (18 sempre; 17 se HT efetivo ≤ 15) → hemorragia maior: −3 PV. MB p.420.
                val perda = if (soma == 18 || (soma == 17 && htEf <= 15)) 3 else 1
                val r = aplicarGolpe(c.pvAtual, c.pvMax, ht, perda, random)
                c.pvAtual = r.pvDepois
                when (r.efeito) {
                    EfeitoFerimento.MORTO, EfeitoFerimento.INCONSCIENTE -> c.condicoes.add(Condicao.INCONSCIENTE)
                    EfeitoFerimento.ATORDOADO_CAIDO -> { c.condicoes.add(Condicao.ATORDOADO); c.condicoes.add(Condicao.CAIDO) }
                    EfeitoFerimento.NENHUM -> {}
                }
                logs.add("sangra (HT $htEf, rolou $soma): perde $perda PV → ${c.pvAtual}/${c.pvMax}.")
                ResultadoFerimento(c.pvAtual, r.efeito, logs)
            }
        }
    }

    /** Desfecho da passagem de tempo com sangramento (Lote 423). morto/desmaiou vêm do VEREDITO do motor (cheques de HT). */
    data class ResultadoSangramentoTempo(val logs: List<String>, val morto: Boolean, val desmaiou: Boolean)

    /**
     * Lote 423: sangramento FORA de combate — processa [minutos] de tempo narrado (passar_tempo do Narrador).
     * Um teste por intervalo fechado, até estancar ou morrer. Continua mesmo inconsciente (pode sangrar até a
     * morte, MB p.420). A morte é o EfeitoFerimento.MORTO real dos cheques — NUNCA inferida por limiar de PV.
     */
    fun sangrarPorTempo(c: Combatente, ht: Int, minutos: Int, random: Random): ResultadoSangramentoTempo {
        val logs = mutableListOf<String>()
        var morto = false; var desmaiou = false
        if (!c.sangramentoAtivo || minutos <= 0) return ResultadoSangramentoTempo(logs, false, false)
        val testes = (minutos * 60) / c.sangramentoIntervaloSeg.coerceAtLeast(1)
        for (i in 1..testes) {
            val r = tickSangramento(c, ht, random) ?: break // estancou num tick anterior
            logs.addAll(r.logs)
            if (r.efeito == EfeitoFerimento.INCONSCIENTE) desmaiou = true
            if (r.efeito == EfeitoFerimento.MORTO) { morto = true; break }
        }
        return ResultadoSangramentoTempo(logs, morto, desmaiou)
    }

    /** Estanca o sangramento (Primeiros Socorros, cura de ≥1 PV — MB p.52). Retorna true se havia sangramento. */
    fun estancarSangramento(c: Combatente): Boolean {
        if (!c.sangramentoAtivo) return false
        c.sangramentoAtivo = false; c.condicoes.remove(Condicao.SANGRANDO)
        c.sangramentoLesaoPV = 0; c.sangramentoPenalidadeLocal = 0
        c.sangramentoIntervaloSeg = 60; c.sangramentoUltimaRodada = Int.MIN_VALUE; c.sangramentoTestesLimpos = 0
        return true
    }
}
