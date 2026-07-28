package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.rules.LocalAtaque

import kotlin.math.ceil
import kotlin.math.floor

/**
 * Lote 361 (Saga B3): dano localizado — PORTE FIEL da calculadora da "Mesa Virtual"
 * (Mesa Virtual/index.html: DAMAGE_RULES + applySmartDmg). Paridade 100% com aquele JS.
 * Kotlin puro. Referências // MB nos comentários.
 *
 * Ordem de cálculo (igual ao JS): RD do local → dano penetrante → multiplicador (com
 * overrides de crânio/vitais) → limite de membro → PV a subtrair.
 */

/** Tipos de dano (chaves do DAMAGE_RULES da Mesa Virtual). Multiplicador BASE em ferimento. */
enum class DanoTipo(val rotulo: String, val multBase: Double) {
    CONT("cont", 1.0),        // contusão
    CORT("corte", 1.5),       // corte — MB p.379
    PI_MENOS("pi-", 0.5),     // perfurante pequeno
    PI("pi", 1.0),            // perfurante
    PI_MAIS("pi+", 1.5),
    PI_MAIS_MAIS("pi++", 2.0),
    PERF("perf", 2.0);        // perfuração (impaling)

    /** Perfurante/perfuração ganham ×3 nos vitais (Mesa Virtual: startsWith('pi') || 'perf'). */
    val perfuranteOuPerf: Boolean get() = this != CONT && this != CORT
}

/**
 * Lote 385: Tolerância a Ferimentos (MB p.380/381). Reduz o multiplicador de ferimento de pi/perf
 * (mortos-vivos, máquinas, objetos, enxames). NORMAL = ser vivo comum.
 */
enum class ToleranciaFerimentos { NORMAL, NAO_VIVO, HOMOGENEO, DIFUSO }

object HitLocationRules {

    /** RD extra natural do local (Mesa Virtual: só crânio = +2). MB p.399. */
    private fun rdExtra(local: LocalAtaque): Int = if (local == LocalAtaque.CRANIO) 2 else 0

    /** Fração do PV que incapacita o membro, ou null se o local não tem limite. */
    private fun limiteMembro(local: LocalAtaque): Double? = when (local) {
        LocalAtaque.BRACO, LocalAtaque.PERNA -> 0.5    // > PV/2 incapacita
        LocalAtaque.MAO, LocalAtaque.PE -> 0.33        // > PV/3 (Mesa Virtual usa 0.33)
        else -> null
    }

    /**
     * Multiplicador final tipo×local. NORMAL: crânio ×4; vitais ×3 p/ perf.; senão o base.
     * Lote 385 (MB p.381): com Tolerância a Ferimentos, pi/perf têm multiplicador reduzido e os locais
     * vitais/crânio não dão bônus (sem órgãos/cérebro). Difuso é tratado por um teto no dano final.
     */
    fun multiplicador(tipo: DanoTipo, local: LocalAtaque, tolerancia: ToleranciaFerimentos = ToleranciaFerimentos.NORMAL): Double = when (tolerancia) {
        ToleranciaFerimentos.NORMAL -> when {
            local == LocalAtaque.CRANIO -> 4.0
            local == LocalAtaque.VITAIS && tipo.perfuranteOuPerf -> 3.0
            else -> tipo.multBase
        }
        ToleranciaFerimentos.NAO_VIVO -> when (tipo) {
            DanoTipo.PERF, DanoTipo.PI_MAIS_MAIS -> 1.0
            DanoTipo.PI_MAIS -> 0.5
            DanoTipo.PI -> 1.0 / 3
            DanoTipo.PI_MENOS -> 0.2
            else -> tipo.multBase
        }
        ToleranciaFerimentos.HOMOGENEO -> when (tipo) {
            DanoTipo.PERF, DanoTipo.PI_MAIS_MAIS -> 0.5
            DanoTipo.PI_MAIS -> 1.0 / 3
            DanoTipo.PI -> 0.2
            DanoTipo.PI_MENOS -> 0.1
            else -> tipo.multBase
        }
        ToleranciaFerimentos.DIFUSO -> tipo.multBase // o teto (1 p/ pi-perf, 2 p/ resto) é no dano final
    }

    data class RelatorioDano(
        val pvSubtrair: Int,
        val penetrante: Int,
        val multiplicador: Double,
        val rdEfetiva: Int,
        val incapacitouMembro: Boolean,
        val texto: String
    )

    /**
     * Aplica dano a um alvo. Retorna quanto PV subtrair + efeitos.
     * @param pvMax PV máximo do alvo (para o limite de membro).
     * @param danoBase dano rolado já somado (ex.: 2d+1 já resolvido em número).
     * @param rd RD da armadura no local (a RD natural do crânio é somada aqui dentro).
     */
    fun aplicarDano(pvMax: Int, danoBase: Int, tipo: DanoTipo, local: LocalAtaque, rd: Int, tolerancia: ToleranciaFerimentos = ToleranciaFerimentos.NORMAL): RelatorioDano {
        val rdEf = rd + rdExtra(local)
        val penetrante = (danoBase - rdEf).coerceAtLeast(0)
        val mult = multiplicador(tipo, local, tolerancia)
        var final = floor(penetrante * mult).toInt()

        var incapacitou = false
        val limite = limiteMembro(local)
        if (limite != null) {
            val max = ceil(pvMax * limite).toInt()
            if (final > max) {
                final = max
                incapacitou = true
            }
        }

        // Difuso (MB p.381): pi/perf nunca passam de 1 PV; os demais tipos, de 2 PV.
        var notaDifuso = ""
        if (tolerancia == ToleranciaFerimentos.DIFUSO && penetrante > 0) {
            val teto = if (tipo.perfuranteOuPerf) 1 else 2
            if (final > teto) { final = teto; notaDifuso = " (difuso: teto $teto)" }
        }

        val texto = buildString {
            append("${local.rotulo}: $danoBase ${tipo.rotulo} − RD $rdEf = $penetrante penetrante ×$mult = $final PV")
            if (incapacitou) append(" (membro incapacitado)")
            append(notaDifuso)
        }
        return RelatorioDano(final, penetrante, mult, rdEf, incapacitou, texto)
    }
}
