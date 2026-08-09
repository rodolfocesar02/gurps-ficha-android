package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.rules.DanoTipo
import com.gurps.ficha.domain.rules.FerimentoPorLocalRules
import com.gurps.ficha.domain.rules.LocalAtaque
import com.gurps.ficha.domain.rules.ToleranciaFerimentos

import kotlin.math.floor

/**
 * Lote 361 (Saga B3): dano localizado — nasceu como PORTE FIEL da calculadora da
 * "Mesa Virtual" (Mesa Virtual/index.html: DAMAGE_RULES + applySmartDmg).
 * Kotlin puro. Referências // MB nos comentários.
 *
 * Ordem de cálculo: RD do local → dano penetrante → multiplicador (com overrides
 * de crânio/vitais) → limite de membro → PV a subtrair.
 *
 * ## 🔴 Onde a paridade com o JS foi ABANDONADA de propósito (Lote MB-7b)
 *
 * O teto do membro era `ceil(PV × 0,5)`, herdado da Mesa Virtual — e ele **erra
 * 1 ponto para baixo com PV par**. O livro dá o *mínimo necessário para
 * incapacitar* como o menor inteiro **estritamente acima** da fração, e diz isso
 * em dois exemplos trabalhados:
 *
 * > No caso de Friedrick [PV 14], PV/2 é 7. **Dano maior que PV/2 é 8 PV**, então
 * > ele perde apenas 8 PV. (MB p.419)
 *
 * > Se um homem com **10 PV** sofrer 9 pontos de dano no braço direito, ele só
 * > perde **6 PV**. (MB p.421)
 *
 * `ceil` devolvia 7 e 5. Com PV **ímpar** as duas contas coincidem — por isso o
 * erro sobreviveu a dois anos de testes: ele só aparece em metade das fichas.
 *
 * ⚠️ E as extremidades usavam `0,33` em vez de `1/3`, o que errava para todo PV
 * múltiplo de 3 (PV 3: `ceil(0,99)` = 1, quando o mínimo é 2).
 *
 * A conta certa mora em [FerimentoPorLocalRules.minimoQueIncapacita], com os dois
 * exemplos do livro como teste, e este arquivo agora **delega** para lá. Duas
 * cópias da mesma regra foi o que permitiu uma delas ficar errada em silêncio.
 */

object HitLocationRules {

    /** RD extra natural do local (Mesa Virtual: só crânio = +2). MB p.399. */
    private fun rdExtra(local: LocalAtaque): Int = if (local == LocalAtaque.CRANIO) 2 else 0


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

        // O teto do membro vem da regra única (MB p.421). Incapacita quando a
        // lesão ALCANÇA o mínimo — que já é o primeiro valor acima da fração —,
        // e o excesso é desperdiçado.
        //
        // ⚠️ O OLHO fica de fora, de propósito. A regra única também sabe cegar
        // (dano acima de PV/10), mas o motor de combate nunca tratou o olho como
        // membro e ensinar isso a ele agora é mudança de comportamento, não
        // correção de conta. Fica para um lote que possa testar o efeito no
        // combate; aqui o escopo é o off-by-one. A cegueira já funciona no botão
        // PV da ficha.
        var incapacitou = false
        val minimo = if (local == LocalAtaque.OLHO) {
            null
        } else {
            FerimentoPorLocalRules.minimoQueIncapacita(local, pvMax)
        }
        if (minimo != null && final >= minimo) {
            incapacitou = true
            final = minimo
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
