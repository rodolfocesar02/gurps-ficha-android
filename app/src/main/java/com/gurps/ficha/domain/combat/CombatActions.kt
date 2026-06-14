package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.roll.CriticoRules
import kotlin.random.Random

/**
 * Lote 360 (Saga B2): resolução do ATAQUE (cálculo de NH efetivo + rolagem 3d6).
 * Kotlin puro, sem Android. O dano localizado é o B3; estados vitais o B4; defesas o B5.
 *
 * Divergência do plano: o plano fala "Mover-e-Atacar teto 9" — isso é a regra À DISTÂNCIA
 * (MB p.365). No corpo-a-corpo a regra é −4 (sem teto). Implemento as DUAS via `aDistancia`,
 * mantendo a matemática correta do GURPS (regra 5/12 do plano: regra real vence).
 */
object CombatActions {

    data class ComponenteMod(val nome: String, val valor: Int)

    data class CalculoNH(
        val nhBase: Int,
        val componentes: List<ComponenteMod>,
        val nhEfetivo: Int,
        /** true quando Mover-e-Atacar limitou o NH ao teto (à distância). */
        val limitadoPorTeto: Boolean = false
    ) {
        /** "NH 14 −3 vitais −2 escuridão parcial = 9" */
        fun descricao(): String {
            val sb = StringBuilder("NH $nhBase")
            componentes.forEach { c ->
                val sinal = if (c.valor >= 0) "+${c.valor}" else "${c.valor}"
                sb.append(" $sinal ${c.nome}")
            }
            sb.append(" = $nhEfetivo")
            if (limitadoPorTeto) sb.append(" (teto Mover-e-Atacar)")
            return sb.toString()
        }
    }

    /**
     * Calcula o NH efetivo do ataque (PURO — sem rolar dados).
     * @param nhBaseArma NH do atacante com a arma/perícia de combate.
     * @param aDistancia true para ataque à distância (afeta a regra do Mover-e-Atacar).
     */
    fun calcularNH(
        nhBaseArma: Int,
        manobra: Manobra,
        postura: Postura = Postura.EM_PE,
        local: LocalAtaque = LocalAtaque.TORSO,
        visibilidade: Visibilidade = Visibilidade.NORMAL,
        ataqueTotalModo: AtaqueTotalModo = AtaqueTotalModo.DETERMINADO,
        aDistancia: Boolean = false
    ): CalculoNH {
        val comps = mutableListOf<ComponenteMod>()

        when (manobra) {
            Manobra.ATAQUE_TOTAL -> {
                val m = ModificadoresCombate.modAtaqueTotal(ataqueTotalModo)
                if (m != 0) comps.add(ComponenteMod("Ataque Total ${ataqueTotalModo.rotulo}", m))
            }
            Manobra.MOVER_E_ATACAR -> {
                if (!aDistancia) comps.add(ComponenteMod("Mover e Atacar", -4)) // CaC: −4 (MB p.365)
            }
            else -> { /* Ataque simples e demais: sem mod de manobra ao acerto */ }
        }

        val mp = ModificadoresCombate.modPostura(postura)
        if (mp != 0) comps.add(ComponenteMod(postura.rotulo, mp))
        if (local.penalidadeAtaque != 0) comps.add(ComponenteMod(local.rotulo, local.penalidadeAtaque))
        if (visibilidade.penalidade != 0) comps.add(ComponenteMod(visibilidade.rotulo, visibilidade.penalidade))

        var nh = nhBase(nhBaseArma, comps)
        // Mover-e-Atacar À DISTÂNCIA: NH limitado a 9 (MB p.365).
        var teto = false
        if (manobra == Manobra.MOVER_E_ATACAR && aDistancia && nh > 9) {
            nh = 9
            teto = true
        }
        return CalculoNH(nhBaseArma, comps, nh, teto)
    }

    private fun nhBase(base: Int, comps: List<ComponenteMod>) = base + comps.sumOf { it.valor }

    enum class ResultadoAcerto { ACERTO, FALHA }

    data class RelatorioAtaque(
        val calculo: CalculoNH,
        val dados: List<Int>,
        val soma: Int,
        val resultado: ResultadoAcerto,
        /** margem de sucesso (>=0) ou de falha (>0) — use com `resultado`. */
        val margem: Int,
        val critico: CriticoRules.ResultadoCritico,
        /** Ataque Total: o atacante fica sem defesa ativa neste turno (flag p/ B5). MB p.365. */
        val atacanteSemDefesaAtiva: Boolean,
        /** Mover-e-Atacar: só Esquiva depois (sem aparar). MB p.365. */
        val semApararDepois: Boolean,
        val texto: String
    )

    /**
     * Avalia uma rolagem já feita contra um NH efetivo (PURO — testável com soma fixa).
     * acerto = soma <= NH; crítico pela regra completa do CriticoRules (com NH).
     */
    fun avaliarRolagem(nhEfetivo: Int, soma: Int): Triple<ResultadoAcerto, Int, CriticoRules.ResultadoCritico> {
        val critico = CriticoRules.classificar(soma, nhEfetivo)
        // Decisivo sempre acerta; Falha Crítica sempre falha (independe do NH).
        val acertou = when (critico) {
            CriticoRules.ResultadoCritico.DECISIVO -> true
            CriticoRules.ResultadoCritico.FALHA_CRITICA -> false
            CriticoRules.ResultadoCritico.NORMAL -> soma <= nhEfetivo
        }
        val margem = if (acertou) nhEfetivo - soma else soma - nhEfetivo
        val resultado = if (acertou) ResultadoAcerto.ACERTO else ResultadoAcerto.FALHA
        return Triple(resultado, margem, critico)
    }

    /** Resolve o ataque completo: calcula NH, rola 3d6 e monta o relatório legível. */
    fun resolverAtaque(
        nhBaseArma: Int,
        manobra: Manobra,
        postura: Postura = Postura.EM_PE,
        local: LocalAtaque = LocalAtaque.TORSO,
        visibilidade: Visibilidade = Visibilidade.NORMAL,
        ataqueTotalModo: AtaqueTotalModo = AtaqueTotalModo.DETERMINADO,
        aDistancia: Boolean = false,
        random: Random = Random.Default
    ): RelatorioAtaque {
        val calc = calcularNH(nhBaseArma, manobra, postura, local, visibilidade, ataqueTotalModo, aDistancia)
        val d = List(3) { random.nextInt(1, 7) }
        val soma = d.sum()
        val (res, margem, critico) = avaliarRolagem(calc.nhEfetivo, soma)

        val palavraRes = if (res == ResultadoAcerto.ACERTO) "acerto" else "falha"
        val sufixoCrit = when (critico) {
            CriticoRules.ResultadoCritico.DECISIVO -> ", DECISIVO"
            CriticoRules.ResultadoCritico.FALHA_CRITICA -> ", FALHA CRÍTICA"
            CriticoRules.ResultadoCritico.NORMAL -> ""
        }
        val texto = "${calc.descricao()}; rolou $soma: $palavraRes, margem $margem$sufixoCrit"

        return RelatorioAtaque(
            calculo = calc,
            dados = d,
            soma = soma,
            resultado = res,
            margem = margem,
            critico = critico,
            atacanteSemDefesaAtiva = manobra == Manobra.ATAQUE_TOTAL,
            semApararDepois = manobra == Manobra.MOVER_E_ATACAR,
            texto = texto
        )
    }
}
