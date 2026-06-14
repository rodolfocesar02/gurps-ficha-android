package com.gurps.ficha.domain.combat

import kotlin.random.Random

/**
 * Lote 362 (Saga B4): estados vitais do GURPS 4ª ed. — choque, ferimento grave, checagens de
 * morte, inconsciência e recuperação de atordoamento. Kotlin puro. Referências // MB.
 */
object InjuryRules {

    /** Choque: −1 por PV perdido, até −4, no PRÓXIMO turno (em testes de DX/IQ). MB p.419. */
    fun penalidadeChoque(dano: Int): Int = -minOf(dano.coerceAtLeast(0), 4)

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
    fun aplicarGolpe(pvAntes: Int, pvMax: Int, ht: Int, dano: Int, random: Random): ResultadoFerimento {
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
        if (ehFerimentoGrave(dano, pvMax)) {
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
    fun ferir(c: Combatente, dano: Int, ht: Int, random: Random): ResultadoFerimento {
        val r = aplicarGolpe(c.pvAtual, c.pvMax, ht, dano, random)
        c.pvAtual = r.pvDepois
        when (r.efeito) {
            EfeitoFerimento.MORTO, EfeitoFerimento.INCONSCIENTE -> c.condicoes.add(Condicao.INCONSCIENTE)
            EfeitoFerimento.ATORDOADO_CAIDO -> { c.condicoes.add(Condicao.ATORDOADO); c.condicoes.add(Condicao.CAIDO) }
            EfeitoFerimento.NENHUM -> {}
        }
        return r
    }
}
