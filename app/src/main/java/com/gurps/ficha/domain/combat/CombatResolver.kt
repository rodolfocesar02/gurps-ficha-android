package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.roll.CriticoRules

/**
 * Lote 364 (Saga B5): camada de DEFESA + troca completa (ataque→defesa→dano→ferimento).
 * Kotlin puro. Encadeia B2 (CombatActions), B3 (HitLocationRules) e B4 (InjuryRules).
 *
 * Divergência do plano: o plano dizia "estender CombatRules.kt". `CombatRules` (domain/rules)
 * é usado pelo `Personagem` (defesas da ficha); para não arriscar o que já funciona, a lógica
 * de defesa DO COMBATE fica aqui, em domain/combat. Os valores-base de defesa continuam vindo
 * de CombatRules/Personagem. (Regra 12: realidade do código vence; divergência relatada.)
 */
object CombatResolver {

    enum class TipoDefesa(val rotulo: String) { ESQUIVA("Esquiva"), APARA("Aparar"), BLOQUEIO("Bloquear") }

    const val BONUS_RECUO_ESQUIVA = 3        // recuar dá +3 à Esquiva. MB p.377
    const val BONUS_RECUO_APARA_BLOQUEIO = 1 // +1 a Aparar/Bloquear ao recuar
    const val BONUS_DEFESA_TOTAL = 2         // Defesa Total Determinada: +2 numa defesa. MB p.366
    const val PENALIDADE_APARA_EXTRA = 4     // cada apara extra na MESMA arma: −4 cumulativo. MB p.376
    const val PENALIDADE_APARA_ESGRIMA = 2   // armas de esgrima (E): apara extra −2 em vez de −4. MB p.404

    data class ComponenteMod(val nome: String, val valor: Int)

    data class OpcaoDefesa(
        val tipo: TipoDefesa,
        val valorFinal: Int,
        val componentes: List<ComponenteMod>,
        val disponivel: Boolean,
        val motivoIndisponivel: String? = null,
        val recuo: Boolean = false, // Lote 389: variante "com recuo" (Retirada, MB p.377)
        val jogarSeAoChao: Boolean = false // Lote 404: Esquiva e Queda (+3 vs tiro, termina deitado, MB p.377)
    )

    /**
     * Valor final de uma defesa, aplicando recuo / Defesa Total / aparas extras.
     * @param aparasJaFeitas nº de aparas já feitas com a MESMA arma neste turno (só para APARA).
     */
    fun valorDefesaFinal(
        tipo: TipoDefesa,
        base: Int,
        recuo: Boolean = false,
        defesaTotalDeterminada: Boolean = false,
        aparasJaFeitas: Int = 0,
        esgrima: Boolean = false
    ): Pair<Int, List<ComponenteMod>> {
        val comps = mutableListOf<ComponenteMod>()
        if (recuo) {
            // Recuar: +3 Esquiva; +1 Aparar/Bloquear; EXCEÇÃO +3 ao aparar com esgrima (E) — e Boxe/Caratê/Judô,
            // ainda não modelados (sem flag de perícia de luta) — MB p.377.
            val b = if (tipo == TipoDefesa.ESQUIVA || (tipo == TipoDefesa.APARA && esgrima))
                BONUS_RECUO_ESQUIVA else BONUS_RECUO_APARA_BLOQUEIO
            comps.add(ComponenteMod("recuo", b))
        }
        if (defesaTotalDeterminada) comps.add(ComponenteMod("Defesa Total", BONUS_DEFESA_TOTAL))
        if (tipo == TipoDefesa.APARA && aparasJaFeitas > 0) {
            val porApara = if (esgrima) PENALIDADE_APARA_ESGRIMA else PENALIDADE_APARA_EXTRA
            comps.add(ComponenteMod("apara extra ×$aparasJaFeitas", -porApara * aparasJaFeitas))
        }
        return (base + comps.sumOf { it.valor }) to comps
    }

    /** A defesa é ANULADA por crítico do atacante ou por surpresa/ataque pelas costas. MB p.374. */
    fun defesaAnulada(criticoAtaque: Boolean, surpresa: Boolean): Boolean = criticoAtaque || surpresa

    /** Sucesso na defesa: 3-4 sempre passa, 17-18 sempre falha; senão soma ≤ valor. MB p.374. */
    fun defesaBemSucedida(valorFinal: Int, soma: Int): Boolean = when {
        soma <= 4 -> true
        soma >= 17 -> false
        else -> soma <= valorFinal
    }

    /**
     * Monta as opções de defesa do herói para o card "Defenda-se!" (a UI usa em B7/B8).
     * bloqueouEsteTurno/esquivouEsteTurno tornam a opção indisponível quando a regra é 1×/turno.
     */
    fun opcoesDefesa(
        esquivaBase: Int,
        aparaBase: Int?,
        bloqueioBase: Int?,
        defesasUsadas: DefesasUsadas,
        defesaTotalEm: TipoDefesa? = null,
        esgrima: Boolean = false,
        permitirRecuo: Boolean = false, // Lote 389: emite variantes "com recuo" (ataque corpo-a-corpo, 1×/turno)
        permitirJogarSeAoChao: Boolean = false // Lote 404: Esquiva e Queda (+3 na Esquiva vs tiro, termina deitado)
    ): List<OpcaoDefesa> {
        val out = mutableListOf<OpcaoDefesa>()
        fun emitir(tipo: TipoDefesa, base: Int, disponivel: Boolean, motivo: String?, aparas: Int = 0) {
            valorDefesaFinal(tipo, base, false, defesaTotalEm == tipo, aparas, esgrima).let { (v, c) ->
                out.add(OpcaoDefesa(tipo, v, c, disponivel, motivo, recuo = false))
            }
            // Variante "com recuo" (Retirada, MB p.377): só vs corpo-a-corpo e 1×/turno (gateado pelo chamador).
            if (permitirRecuo) valorDefesaFinal(tipo, base, true, defesaTotalEm == tipo, aparas, esgrima).let { (v, c) ->
                out.add(OpcaoDefesa(tipo, v, c, disponivel, motivo, recuo = true))
            }
        }
        emitir(TipoDefesa.ESQUIVA, esquivaBase, true, null)
        // Esquiva e Queda (Lote 404, MB p.377): +3 na Esquiva contra tiro, mas o herói termina deitado (gateado pelo chamador).
        if (permitirJogarSeAoChao) valorDefesaFinal(TipoDefesa.ESQUIVA, esquivaBase, false, defesaTotalEm == TipoDefesa.ESQUIVA).let { (v, c) ->
            out.add(OpcaoDefesa(TipoDefesa.ESQUIVA, v + 3, c + ComponenteMod("jogar-se ao chão", 3),
                disponivel = true, jogarSeAoChao = true))
        }
        if (aparaBase != null)
            emitir(TipoDefesa.APARA, aparaBase, true, null, defesasUsadas.aparasPorArma.values.firstOrNull() ?: 0)
        if (bloqueioBase != null)
            emitir(TipoDefesa.BLOQUEIO, bloqueioBase, !defesasUsadas.bloqueouEsteTurno,
                if (defesasUsadas.bloqueouEsteTurno) "já bloqueou neste turno" else null)
        return out
    }

    // ── Troca completa (ataque → defesa → dano → ferimento) ──────────────────

    data class RelatorioTroca(
        val ataque: CombatActions.RelatorioAtaque,
        val defesaTentada: Boolean,
        val defesaValor: Int?,
        val defesaSoma: Int?,
        val defendeu: Boolean,
        val dano: HitLocationRules.RelatorioDano?,
        val ferimento: InjuryRules.ResultadoFerimento?,
        val texto: String
    )

    /**
     * Resolve uma troca usando ROLAGENS JÁ FEITAS (puro/testável). O atacante acerta?
     * Crítico anula a defesa. Se penetrar, encadeia dano localizado (B3) + ferimento (B4).
     * @param danoBaseRolado dano da arma já rolado em número (ex.: 2d → 9).
     */
    fun resolverTroca(
        defensor: Combatente,
        htDefensor: Int,
        ataque: CombatActions.RelatorioAtaque,
        defesaTipo: TipoDefesa,
        defesaValorFinal: Int,
        defesaSoma: Int,
        surpresa: Boolean,
        danoBaseRolado: Int,
        danoTipo: DanoTipo,
        local: LocalAtaque,
        rdLocal: Int,
        randomFerimento: kotlin.random.Random,
        forcarFerimentoGrave: Boolean = false,
        tolerancia: ToleranciaFerimentos = ToleranciaFerimentos.NORMAL
    ): RelatorioTroca {
        if (ataque.resultado == CombatActions.ResultadoAcerto.FALHA) {
            return RelatorioTroca(ataque, false, null, null, false, null, null, "${ataque.texto} → erra, sem defesa necessária.")
        }
        val critico = ataque.critico == CriticoRules.ResultadoCritico.DECISIVO
        val anulada = defesaAnulada(critico, surpresa)
        val defendeu = if (anulada) false else defesaBemSucedida(defesaValorFinal, defesaSoma)

        if (defendeu) {
            return RelatorioTroca(ataque, true, defesaValorFinal, defesaSoma, true, null, null,
                "${ataque.texto} → ${defesaTipo.rotulo} $defesaValorFinal, rolou $defesaSoma: DEFENDEU.")
        }

        val dano = HitLocationRules.aplicarDano(defensor.pvMax, danoBaseRolado, danoTipo, local, rdLocal, tolerancia)
        val ferimento = InjuryRules.ferir(defensor, dano.pvSubtrair, htDefensor, randomFerimento, forcarFerimentoGrave)
        val motivoSemDefesa = when {
            anulada && critico -> " (defesa ANULADA por golpe decisivo)"
            anulada && surpresa -> " (defesa ANULADA por surpresa)"
            else -> ", rolou $defesaSoma: falhou na defesa"
        }
        val texto = "${ataque.texto} → ${defesaTipo.rotulo} $defesaValorFinal$motivoSemDefesa → ${dano.texto} | ${ferimento.efeito}"
        return RelatorioTroca(ataque, !anulada, defesaValorFinal, if (anulada) null else defesaSoma, false, dano, ferimento, texto)
    }
}
