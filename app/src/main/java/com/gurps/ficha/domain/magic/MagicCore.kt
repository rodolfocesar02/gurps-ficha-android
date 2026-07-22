package com.gurps.ficha.domain.magic

/**
 * Lote MA-1 (motor de magia, kotlin PURO): helpers básicos do MB / Magia p.6–14.
 *
 * Todas as funções são deterministas, sem Android, sem randomização — o CALLER joga os dados e passa
 * o resultado (mesmo padrão do resto do motor de combate).
 */

// ─────────────────────────────── MANA (Magia p.6) ──────────────────────────────

/** Níveis de mana ambiente. Modifica NH efetivo E permite/proíbe operação. */
enum class NivelMana(val penalidadeNH: Int, val podeOperar: Boolean) {
    /** Qualquer um opera; falhas viram críticas. */
    MUITO_ALTA(0, true),
    /** Qualquer um opera (não apenas magos). */
    ALTA(0, true),
    /** Só magos operam; regras padrão. */
    NORMAL(0, true),
    /** Só magos; −5 no NH. Falhas críticas são amenizadas (a cargo do Narrador). */
    BAIXA(-5, true),
    /** Ninguém opera; itens mágicos param de funcionar. */
    NULA(0, false),
}

object MagicMana {
    /** Modificador de NH efetivo pelo mana ambiente. */
    fun penalidadeMana(nivel: NivelMana): Int = nivel.penalidadeNH

    /** Se retorna false, a mágica NÃO pode ser tentada (mana nula). */
    fun podeOperar(nivel: NivelMana, ehMago: Boolean): Boolean = when (nivel) {
        NivelMana.NULA -> false
        NivelMana.NORMAL, NivelMana.BAIXA -> ehMago
        NivelMana.ALTA, NivelMana.MUITO_ALTA -> true
    }
}

// ─────────────────────────── CUSTO EM ENERGIA (Magia p.8) ──────────────────────

object MagicCost {
    /**
     * Redução do custo pelo NH básico do operador (MB p.8):
     *   NH ≥ 15 → −1 no custo
     *   NH ≥ 20 → −2 no custo
     *   +1 a cada 5 níveis acima de 20 (25=−3, 30=−4…)
     * Piso 0. Aplica em [custoBase] JÁ MULTIPLICADO pelo raio/tamanho.
     *
     * NH ajustado por mana baixo (−5) é o mesmo NH que usa aqui? MB p.8 diz explicitamente que sim:
     * "modificado apenas por uma penalidade igual a -5 se estiver em uma área de baixa intensidade de mana".
     */
    fun custoAjustadoPorNH(custoBase: Int, nh: Int): Int {
        if (custoBase <= 0) return 0
        val reducao = when {
            nh < 15 -> 0
            nh < 20 -> 1
            else -> 2 + (nh - 20) / 5
        }
        return (custoBase - reducao).coerceAtLeast(0)
    }

    /**
     * Custo total de uma mágica de ÁREA: básico × raio (mínimo 1). Magia p.11.
     * Aceita [custoBasico] fracionário (algumas mágicas usam 1/2, 1/10) representado como Double —
     * arredonda para cima (mínimo 1).
     */
    fun custoAreaPorRaio(custoBasico: Double, raioMetros: Int): Int {
        require(raioMetros >= 1) { "raio precisa ser ≥ 1m" }
        val bruto = custoBasico * raioMetros
        return kotlin.math.max(1, kotlin.math.ceil(bruto).toInt())
    }

    /**
     * Multiplicador de custo por Modificador de Tamanho positivo para mágicas COMUNS (Magia p.11):
     *   MT +0 → ×1; MT +1 → ×2; MT +N → ×(N+1). MT negativo NÃO reduz.
     */
    fun custoAjustadoPorTamanho(custoBase: Int, mtAlvo: Int): Int {
        if (mtAlvo <= 0) return custoBase
        return custoBase * (mtAlvo + 1)
    }
}

// ────────────────── RITUAL ALTERNATIVO (Magia p.9) — Lote C12 ──────────────────

/**
 * Lote C12: como o mágico executa o ritual. **Regra opcional** do livro.
 *
 * Padrão (`COMPLETO`): *"todas as mágicas exigem gestos com as duas mãos, movimentos sutis dos pés,
 * como passos de dança, e um encantamento entoado com clareza"* — sem modificador.
 *
 * O mágico pode **omitir** partes, aceitando penalidade, ou **caprichar**, dobrando o tempo de
 * operação para ganhar +1. As penalidades **somam** entre si: sem gestos (−4) e em silêncio (−4)
 * dá −8, que é o que a regra descreve para conjurar amarrado e amordaçado.
 */
enum class GestoDoRitual(val modificador: Int, val rotulo: String) {
    DUAS_MAOS(0, "gestos com as duas mãos"),
    UMA_MAO(-2, "gestos com uma mão só"),
    SEM_GESTOS(-4, "sem gestos de mão"),
}

enum class VozDoRitual(val modificador: Int, val rotulo: String) {
    ENTOADO(0, "encantamento entoado com clareza"),
    SUAVE(-2, "encantamento falado suavemente"),
    EM_SILENCIO(-4, "sem entoar nada"),
}

/**
 * Lote C12: modificador total do ritual (Magia p.9). Domínio puro, testável.
 *
 * @param passos `false` = omitiu os movimentos dos pés (−2).
 * @param caprichado `true` = **dobra o tempo de operação** para ganhar **+1**. É a única forma de o
 *   ritual dar bônus, e não é de graça: quem capricha leva o dobro de segundos conjurando.
 */
data class RitualDeConjuracao(
    val gesto: GestoDoRitual = GestoDoRitual.DUAS_MAOS,
    val voz: VozDoRitual = VozDoRitual.ENTOADO,
    val passos: Boolean = true,
    val caprichado: Boolean = false,
) {
    val modificador: Int
        get() = gesto.modificador + voz.modificador +
            (if (passos) 0 else -2) + (if (caprichado) 1 else 0)

    /** Tempo de operação efetivo: caprichar DOBRA os segundos (o preço do +1). */
    fun tempoAjustado(tempoBaseSeg: Int): Int =
        if (caprichado) (tempoBaseSeg * 2).coerceAtLeast(1) else tempoBaseSeg

    /** Só o que FOGE do padrão, para o log não repetir "duas mãos, entoado, com passos". */
    fun descricao(): String = buildList {
        if (gesto != GestoDoRitual.DUAS_MAOS) add(gesto.rotulo)
        if (voz != VozDoRitual.ENTOADO) add(voz.rotulo)
        if (!passos) add("sem os movimentos dos pés")
        if (caprichado) add("ritual caprichado (tempo dobrado)")
    }.joinToString(", ")

    val ehPadrao: Boolean get() = modificador == 0 && !caprichado
}

// ────────────────── PENALIDADES DE DISTÂNCIA (Magia p.11) ──────────────────

object MagicDistance {
    /** Comum: penalidade = distância em METROS entre operador e alvo, se não tocar. */
    fun penalidadeDistanciaMetros(metros: Int): Int = -metros.coerceAtLeast(0)

    /** Como 1 hex = 1 m no PILAR HEX, atalho direto. */
    fun penalidadeDistanciaHex(hexes: Int): Int = penalidadeDistanciaMetros(hexes)

    /** Se operador NÃO vê nem toca o alvo, aplica −5 adicional (Magia p.11). */
    fun penalidadeSemContatoNemVisao(): Int = -5
}

// ─────── PENALIDADES POR OUTRAS MÁGICAS ATIVAS (Magia p.10) ────────

object MagicMultiplasMagias {
    /**
     * Ao operar uma nova mágica com outras ativas:
     *   −3 por cada outra que exija CONCENTRAÇÃO no momento.
     *   −1 por cada outra em andamento (permanentes NÃO penalizam).
     */
    fun penalidade(numExigemConcentracao: Int, numEmAndamentoNaoPermanentes: Int): Int {
        require(numExigemConcentracao >= 0)
        require(numEmAndamentoNaoPermanentes >= 0)
        return -3 * numExigemConcentracao - 1 * numEmAndamentoNaoPermanentes
    }
}

// ─────────────── RESULTADO DA OPERAÇÃO (Magia p.7) ────────────────

/** Resultado de um teste 3d contra o NH efetivo. */
enum class ResultadoOperacao {
    /**
     * Critério do MB p.348: **3 ou 4** sempre; **5** com NH efetivo 15+; **6** com NH efetivo 16+.
     * Efeito ampliado e **custo perdoado** — *"não há gasto de energia quando se obtém um sucesso
     * decisivo durante uma operação mágica"* (Magia p.7).
     *
     * ⚠️ Não existe sucesso decisivo por MARGEM. O MB é explícito: *"Resultados muito altos ou
     * baixos nos dados surtem efeitos especiais [...] independentemente da margem exata"*. Margem
     * ≥ 10 só vale para a FALHA crítica. Esta KDoc já disse "rolagem ≤ NH−10", que nunca foi a
     * regra nem foi o que o código faz — corrigido para não induzir mais ninguém ao erro.
     */
    SUCESSO_DECISIVO,
    /** Rolagem ≤ NH efetivo. Efeito normal, custo pago integralmente. */
    SUCESSO,
    /** Rolagem > NH efetivo. Efeito não ocorre. Custo padrão: 1 ponto (informação: todo o custo). */
    FRACASSO,
    /** Falha crítica (tabela de choque de retorno, Magia p.7). Custo total gasto. */
    FALHA_CRITICA,
}

object MagicOperationRuling {
    /**
     * Classifica o resultado de uma jogada 3d contra o NH efetivo, usando as regras padrão GURPS
     * (MB p.348 — conferido no livro, não de memória):
     *  - Sucesso decisivo: **3 ou 4** sempre; **5** com NH 15+; **6** com NH 16+. Sem regra de margem.
     *  - Falha crítica: 18 sempre, 17 se NH < 16, ou margem de fracasso ≥ 10.
     *  - Sucesso: rolagem ≤ NH efetivo (e não é decisivo).
     *  - Fracasso: resto.
     *
     * O caller passa a rolagem 3d (soma 3..18) e o NH efetivo já ajustado por todos os modificadores.
     */
    fun classificar(rolagem3d: Int, nhEfetivo: Int): ResultadoOperacao {
        require(rolagem3d in 3..18) { "rolagem 3d fora do intervalo (3..18): $rolagem3d" }
        // Falha crítica primeiro (18 sempre; 17 quando NH < 16; margem ≥ 10 quando NH < 16).
        if (rolagem3d == 18) return ResultadoOperacao.FALHA_CRITICA
        if (rolagem3d == 17 && nhEfetivo < 16) return ResultadoOperacao.FALHA_CRITICA
        if (rolagem3d - nhEfetivo >= 10) return ResultadoOperacao.FALHA_CRITICA

        // Sucesso decisivo.
        if (rolagem3d <= 4) return ResultadoOperacao.SUCESSO_DECISIVO
        if (rolagem3d == 5 && nhEfetivo >= 15) return ResultadoOperacao.SUCESSO_DECISIVO
        if (rolagem3d == 6 && nhEfetivo >= 16) return ResultadoOperacao.SUCESSO_DECISIVO

        // Sucesso / fracasso comum.
        return if (rolagem3d <= nhEfetivo) ResultadoOperacao.SUCESSO else ResultadoOperacao.FRACASSO
    }

    /**
     * Custo em FP/PV a subtrair após uma operação — regras Magia p.7–8:
     *  - SUCESSO_DECISIVO → 0 (custo perdoado).
     *  - SUCESSO → custo total.
     *  - FRACASSO → 1 ponto (Informação: custo total).
     *  - FALHA_CRITICA → custo total (independente da classe).
     */
    fun custoAPagar(resultado: ResultadoOperacao, custoTotal: Int, ehInformacao: Boolean): Int =
        when (resultado) {
            ResultadoOperacao.SUCESSO_DECISIVO -> 0
            ResultadoOperacao.SUCESSO -> custoTotal
            ResultadoOperacao.FRACASSO -> if (ehInformacao) custoTotal else 1
            ResultadoOperacao.FALHA_CRITICA -> custoTotal
        }
}

// ───────────────── CHOQUE DE RETORNO (Magia p.7) ─────────────────

/** Efeito da tabela de falha crítica em operações mágicas. */
data class EfeitoChoqueRetorno(
    val rolagem3d: Int,
    val rotulo: String,
    val danoAoOperadorDadosD6: Int = 0,
    val danoAoOperadorPontos: Int = 0,
    val atordoaOperador: Boolean = false,
    val magicaFracassaCompleta: Boolean = true,
)

object MagicChoqueRetorno {
    /**
     * Consulta a tabela de choque de retorno (Magia p.7) para uma rolagem 3d. Se cair num resultado
     * que o Mestre decidiu "inadequado" (mesmo efeito da mágica pretendida), o caller re-rola.
     *
     * Efeitos NARRATIVOS ("mágica produz um cheiro desagradável" etc.) são só rótulos — Motor devolve
     * o texto pra o feed do Narrador exibir.
     */
    fun consultar(rolagem3d: Int): EfeitoChoqueRetorno {
        require(rolagem3d in 3..18)
        return when (rolagem3d) {
            3 -> EfeitoChoqueRetorno(3, "Mágica fracassa e operador sofre 1d de dano.", danoAoOperadorDadosD6 = 1)
            4 -> EfeitoChoqueRetorno(4, "Mágica afeta o operador (perigosa) ou inimigo próximo (benéfica).")
            5, 6 -> EfeitoChoqueRetorno(rolagem3d, "Mágica afeta um COMPANHEIRO (perigosa) ou inimigo próximo (benéfica).")
            7 -> EfeitoChoqueRetorno(7, "Mágica atinge alguém DIFERENTE do alvo original (aleatório).")
            8 -> EfeitoChoqueRetorno(8, "Mágica fracassa e operador sofre 1 ponto de dano.", danoAoOperadorPontos = 1)
            9 -> EfeitoChoqueRetorno(9, "Mágica fracassa e operador fica ATORDOADO (IQ para se recuperar).",
                atordoaOperador = true)
            10, 11 -> EfeitoChoqueRetorno(rolagem3d, "Mágica produz só um ruído/clarão/cheiro — nenhum efeito real.")
            12 -> EfeitoChoqueRetorno(12, "Mágica produz efeito TÊNUE e inútil.")
            13 -> EfeitoChoqueRetorno(13, "Mágica produz o efeito INVERSO ao desejado.")
            14 -> EfeitoChoqueRetorno(14, "Mágica parece funcionar — mas é ILUSÃO. Mestre convence o mago.")
            15, 16 -> EfeitoChoqueRetorno(rolagem3d, "Efeito INVERSO no alvo ERRADO.")
            17 -> EfeitoChoqueRetorno(17, "Mágica fracassa. Operador ESQUECE a mágica (teste IQ semanal).")
            18 -> EfeitoChoqueRetorno(18, "Mágica fracassa. Um DEMÔNIO/entidade maligna aparece para atacar.")
            else -> EfeitoChoqueRetorno(rolagem3d, "Resultado indefinido — Mestre improvisa.")
        }
    }
}

// ─────────── MÁGICA ATIVA NO COMBATE (Magia p.9-10) ──────────

/** Tipo de duração — Magia p.10. */
enum class TipoDuracao {
    /** Efeito ocorre e some. Não fica ativa. */
    INSTANTANEA,
    /** Precisa de manutenção; duração renovável. Único tipo que conta como "ativa" no combate. */
    TEMPORARIA,
    /** Sem manutenção, dura até um evento a terminar (ex.: Abençoar dura até a bênção ser usada). */
    DURADOURA,
    /** Efeito permanece indefinidamente (ex.: Zumbi). Suspensa em mana nula, retomada ao sair. */
    PERMANENTE,
    /** Cria item mágico. Não anulada por Anular Mágica. Suspensa em mana nula. */
    ENCANTAMENTO,
}

/** Estado de uma mágica ativa no combate — o [CombatEncounter] terá uma lista destes em MA-2. */
data class MagiaAtivaNoCombate(
    /** ID da mágica no catálogo (`magias2versao.json`). */
    val magiaId: String,
    /** ID do combatente que operou a mágica. */
    val operadorId: String,
    /** ID do alvo (mesmo ID que o operador para automagia; null para mágicas de área sem foco). */
    val alvoId: String?,
    /** Energia total investida (pontos de FP/PV). Determina o poder do efeito. */
    val energiaInvestida: Int,
    /** Custo de manutenção em pontos por período (0 se manter é gratuito pelo NH alto). */
    val custoManutencaoSeg: Int,
    /** Segundos até a próxima cobrança de manutenção (decai por [avancarTurnoSegundos]). */
    val segundosParaProximaCobranca: Int,
    /** Duração total configurada, para o feed exibir "8s restantes". */
    val duracaoTotalSeg: Int,
    /** Tipo de duração para o motor saber quando/como expirar. */
    val duracao: TipoDuracao,
    /** true se a mágica exige concentração contínua (ex.: Levitar objeto). */
    val exigeConcentracao: Boolean,
    /**
     * Lote MEC-2: os deltas que este buff aplicou no alvo (null = efeito só narrado). Guardar o que
     * ENTROU é o que deixa a expiração reverter sem recalcular — imune a drift.
     */
    val buff: BuffAplicado? = null,
    /**
     * Lote MEC-22: a mecânica curada, quando esta mágica **fere a cada turno** (Morte Candente,
     * Morte Putrefata). O tique lê daqui o dado, o atributo testado e a regra de quebra.
     */
    val mecanica: MagiaMecanica? = null,
    /**
     * Lote MEC-22 — **regra da estreia**: a mágica não pode ticar no mesmo turno em que foi
     * aplicada. Ela é registrada durante a ação do herói e o tique roda no fim desse mesmo turno,
     * então sem esta trava a vítima levaria um turno de dano de brinde. É o mesmo tropeço já
     * corrigido em `aplicar_modificador_combate`.
     */
    val pularPrimeiroTique: Boolean = false,
)

object MagicActive {
    /**
     * Avança o timer de todas as mágicas ativas por [segundos]. Devolve:
     *   - Lista atualizada (mágicas expiradas removidas).
     *   - Cobranças a debitar (mapa operadorId → total de FP consumido em manutenções).
     *
     * Contrato:
     *  - INSTANTANEA nunca deveria estar em [ativas]; se estiver, é ignorada.
     *  - PERMANENTE / ENCANTAMENTO nunca cobram nem expiram.
     *  - TEMPORARIA/DURADOURA reduzem [segundosParaProximaCobranca]; ao chegar em 0 cobra manutenção
     *    (a menos que custo=0), reseta o timer para [duracaoTotalSeg] se TEMPORARIA. DURADOURA
     *    expira ao chegar em 0.
     */
    data class ResultadoTurno(
        val ativasApos: List<MagiaAtivaNoCombate>,
        val cobrancasPorOperador: Map<String, Int>,
        val expiradas: List<MagiaAtivaNoCombate>,
        /**
         * Lote MEC-23: quais mágicas venceram manutenção NESTE avanço, e por quanto. O total
         * agregado não serve para PERGUNTAR ao jogador se ele quer manter cada uma — em GURPS
         * manter é OPCIONAL, então a decisão é por mágica.
         */
        val venceramManutencao: List<Pair<MagiaAtivaNoCombate, Int>> = emptyList(),
    )

    fun avancarTurnoSegundos(ativas: List<MagiaAtivaNoCombate>, segundos: Int): ResultadoTurno {
        require(segundos >= 0)
        val restantes = mutableListOf<MagiaAtivaNoCombate>()
        val cobrancas = mutableMapOf<String, Int>()
        val expiradas = mutableListOf<MagiaAtivaNoCombate>()
        val venceram = mutableListOf<Pair<MagiaAtivaNoCombate, Int>>()

        for (m in ativas) {
            when (m.duracao) {
                TipoDuracao.INSTANTANEA -> Unit // não deveria estar aqui — filtra
                TipoDuracao.PERMANENTE, TipoDuracao.ENCANTAMENTO -> restantes.add(m)
                TipoDuracao.TEMPORARIA -> {
                    val novoTimer = m.segundosParaProximaCobranca - segundos
                    if (novoTimer <= 0) {
                        val renovada = m.copy(segundosParaProximaCobranca = m.duracaoTotalSeg)
                        if (m.custoManutencaoSeg > 0) {
                            cobrancas.merge(m.operadorId, m.custoManutencaoSeg) { a, b -> a + b }
                            venceram.add(renovada to m.custoManutencaoSeg) // MEC-23
                        }
                        restantes.add(renovada)
                    } else {
                        restantes.add(m.copy(segundosParaProximaCobranca = novoTimer))
                    }
                }
                TipoDuracao.DURADOURA -> {
                    val novoTimer = m.segundosParaProximaCobranca - segundos
                    if (novoTimer <= 0) expiradas.add(m) else restantes.add(m.copy(segundosParaProximaCobranca = novoTimer))
                }
            }
        }
        return ResultadoTurno(restantes, cobrancas, expiradas, venceram)
    }
}
