package com.gurps.ficha.domain.magic

/**
 * Lote MA-2 (motor de magia, kotlin PURO): o RESOLVEDOR de conjuração — o "cérebro" compartilhado
 * pelos dois palcos (narrativa e combate). Amarra as peças do MA-1 (mana, custo, distância, choque)
 * às regras que faltavam, lidas direto do livro Magia (pt_magia p.5–15):
 *
 *  - NH efetivo com detalhamento transparente (mana, distância, sem visão, múltiplas magias, PV).
 *  - Custo total: área × raio / MT ANTES da redução por NH; Bloqueio NUNCA reduz (p.8/12).
 *  - Resistíveis: só automáticas no sucesso decisivo; senão Disputa Rápida margem×resistência,
 *    com a Regra do 16 para alvos vivos/sencientes e o Abascanto do alvo (p.13–14).
 *  - Queimar PV: −1 no NH por PV gasto, no lugar da penalidade de choque (p.8).
 *  - Escala de efeito por energia com teto pela Aptidão Mágica (p.9): 1 pto = 1d dano / 2d projeção
 *    / 1s de cegueira (diretriz p.14).
 *  - Tempo de operação reduzido por NH alto (p.9).
 *
 * Tudo determinístico: o CALLER joga os dados (3d do operador, 3d da resistência) e passa o
 * resultado — mesmo padrão do resto do motor. Nada de Android aqui; a ficha/o catálogo entram no
 * MA-3 (combate) e MA-4 (narrativa) e chamam este resolvedor.
 */

// ─────────────── CUSTO EM ENERGIA vindo do catálogo (`energia`: string livre) ───────────────

/**
 * Resultado do parse do campo `energia` do `magias2versao.json`. É string livre: "2", "1 a 3",
 * "Varia", "1/2", "3 (mín. 2)"... O parser NUNCA lança — casos que não entende viram [variavel]
 * com [base] nulo (o operador/Narrador decide quanto investir).
 */
data class CustoEnergia(
    /** Custo básico fixo, ou null quando "Varia"/desconhecido (o efeito escala com a energia). */
    val base: Int?,
    /** true se o efeito escala com a energia investida ("Varia", faixas "1 a 3"). */
    val variavel: Boolean,
    /** Piso do custo (nunca abaixo de 1 na prática das regras). */
    val minimo: Int = 1,
    /** Teto do custo quando o JSON traz faixa "1 a 3" (null = sem teto declarado). */
    val maximo: Int? = null,
    /** Custo básico FRACIONÁRIO de mágica de área (1/2, 1/10) — Magia p.11. */
    val fracao: Double? = null,
    val original: String,
)

object MagicEnergy {
    private val FRACAO = Regex("""(\d+)\s*/\s*(\d+)""")
    private val FAIXA = Regex("""(\d+)\s*(?:a|-|–|até)\s*(\d+)""")
    private val PRIMEIRO_INT = Regex("""\d+""")

    /** Parseia o campo `energia`. Tolerante e sem exceções. */
    fun parse(energia: String?): CustoEnergia {
        val original = energia?.trim().orEmpty()
        if (original.isEmpty()) return CustoEnergia(base = null, variavel = true, original = original)
        val lower = original.lowercase()

        // "Varia" (com ou sem detalhes) → escala com a energia; sem base fixa.
        if ("varia" in lower || "variável" in lower || "variavel" in lower) {
            // Ainda pode haver um mínimo explícito ("Varia; mín. 2").
            val min = PRIMEIRO_INT.find(lower)?.value?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            return CustoEnergia(base = null, variavel = true, minimo = min, original = original)
        }

        // Fração de área "1/2", "1/10".
        FRACAO.find(lower)?.let { m ->
            val num = m.groupValues[1].toInt(); val den = m.groupValues[2].toInt().coerceAtLeast(1)
            return CustoEnergia(base = null, variavel = false, fracao = num.toDouble() / den, original = original)
        }

        // Faixa "1 a 3" → variável, com min/max.
        FAIXA.find(lower)?.let { m ->
            val a = m.groupValues[1].toInt(); val b = m.groupValues[2].toInt()
            val lo = minOf(a, b).coerceAtLeast(1); val hi = maxOf(a, b)
            return CustoEnergia(base = lo, variavel = true, minimo = lo, maximo = hi, original = original)
        }

        // Inteiro simples (pega o primeiro número; "3 pontos", "2 para..." → 3/2).
        PRIMEIRO_INT.find(lower)?.let { m ->
            val n = m.value.toInt().coerceAtLeast(0)
            return CustoEnergia(base = n, variavel = false, minimo = n.coerceAtLeast(1), original = original)
        }

        // Nada reconhecido → tratamos como variável (Narrador decide).
        return CustoEnergia(base = null, variavel = true, original = original)
    }
}

// ─────────────── NH EFETIVO com detalhamento ───────────────

/** Uma parcela do NH efetivo, para o feed/UI mostrar de onde vem cada modificador (transparência). */
data class ComponenteNH(val motivo: String, val valor: Int)

/** NH efetivo + a lista de parcelas que o formaram. */
data class NHEfetivo(val valor: Int, val componentes: List<ComponenteNH>) {
    val totalMods: Int get() = componentes.sumOf { it.valor }
}

/** Tudo que o resolvedor precisa saber do contexto de UMA conjuração. */
data class ContextoConjuracao(
    /** NH BÁSICO do operador na mágica (IQ + Aptidão + pontos), já calculado pela ficha. */
    val nhBasico: Int,
    /** Classe parseada da mágica (do [MagicClassParser]). */
    val classe: ClasseParseada,
    /** Intensidade de mana ambiente. */
    val mana: NivelMana = NivelMana.NORMAL,
    /** Distância operador→objetivo em metros (0 se toca). Só penaliza Comum/Área/Informação. */
    val distanciaMetros: Int = 0,
    /** true se o operador TOCA o objetivo (zera a penalidade de distância). */
    val tocando: Boolean = false,
    /** true se o operador vê OU toca o objetivo. false → −5 adicional (p.11). */
    val veOuToca: Boolean = true,
    /** Nº de outras mágicas ativas que EXIGEM concentração agora (−3 cada, p.10). */
    val magiasExigindoConcentracao: Int = 0,
    /** Nº de outras mágicas em andamento não-permanentes (−1 cada, p.10). */
    val magiasEmAndamento: Int = 0,
    /** PV que o operador vai queimar no lugar de PF (−1 no NH por PV, p.8). */
    val pvQueimados: Int = 0,
    /** Modificador de Tamanho do objetivo (mágica Comum; só MT>0 encarece, p.11). */
    val mtAlvo: Int = 0,
    /** Raio da área em metros (mágica de Área; custo × raio, p.11). */
    val raioAreaMetros: Int = 1,
    /**
     * Lote MA-6: a conjuração causa DANO por energia (1d por ponto — diretriz de Mágicas de Combate,
     * Magia p.14). Marcado pelo jogador para magias de dano que NÃO são Projétil (jatos, etc.), cujo
     * efeito o catálogo não estrutura. Projétil tem o seu próprio caminho de dano.
     */
    val danoPorEnergia: Boolean = false,
)

object MagicCasting {

    /**
     * NH EFETIVO = NH básico + modificadores de contexto (Magia p.7–11). Devolve o valor E as
     * parcelas, pra a UI/feed exibir "NH 15 · mana baixa −5 · distância −3".
     *
     * Penalidade de distância só se aplica a Comum/Área/Informação e apenas se NÃO tocar. Toque e
     * Projétil criam o efeito na própria mão → sem distância (p.11–12).
     */
    fun nhEfetivo(ctx: ContextoConjuracao): NHEfetivo {
        val comps = mutableListOf<ComponenteNH>()

        val penMana = MagicMana.penalidadeMana(ctx.mana)
        if (penMana != 0) comps.add(ComponenteNH("mana ${ctx.mana.name.lowercase()}", penMana))

        val usaDistancia = ctx.classe.classes.any {
            it == TipoClasseMagia.COMUM || it == TipoClasseMagia.AREA || it == TipoClasseMagia.INFORMACAO
        }
        if (usaDistancia && !ctx.tocando && ctx.distanciaMetros > 0) {
            comps.add(ComponenteNH("distância ${ctx.distanciaMetros}m", MagicDistance.penalidadeDistanciaMetros(ctx.distanciaMetros)))
        }
        if (usaDistancia && !ctx.tocando && !ctx.veOuToca) {
            comps.add(ComponenteNH("sem ver nem tocar", MagicDistance.penalidadeSemContatoNemVisao()))
        }

        val penMagias = MagicMultiplasMagias.penalidade(ctx.magiasExigindoConcentracao, ctx.magiasEmAndamento)
        if (penMagias != 0) comps.add(ComponenteNH("outras mágicas ativas", penMagias))

        if (ctx.pvQueimados > 0) comps.add(ComponenteNH("queimar ${ctx.pvQueimados} PV", -ctx.pvQueimados))

        val valor = ctx.nhBasico + comps.sumOf { it.valor }
        return NHEfetivo(valor, comps)
    }

    /**
     * NH BÁSICO para fins de CUSTO (Magia p.8): é o NH básico modificado APENAS pela penalidade de
     * −5 de mana baixa — nada de distância/múltiplas magias. É este que reduz o custo por NH alto.
     */
    fun nhParaCusto(ctx: ContextoConjuracao): Int =
        ctx.nhBasico + if (ctx.mana == NivelMana.BAIXA) -5 else 0

    /**
     * Custo TOTAL em energia (p.8/11): primeiro o multiplicador de tamanho (área × raio ou Comum ×
     * MT), DEPOIS a redução por NH alto — exceto Bloqueio, que nunca reduz (p.12).
     *
     * [energiaInvestida] é usada quando a mágica é de custo variável (o operador escolhe quanto
     * gastar); para custo fixo, passa o [CustoEnergia.base].
     */
    fun custoTotal(ctx: ContextoConjuracao, custo: CustoEnergia, energiaInvestida: Int? = null): Int {
        val ehArea = TipoClasseMagia.AREA in ctx.classe.classes
        // Custo bruto ANTES da redução por NH.
        val bruto: Int = when {
            ehArea && custo.fracao != null -> MagicCost.custoAreaPorRaio(custo.fracao, ctx.raioAreaMetros.coerceAtLeast(1))
            ehArea -> {
                val basico = (energiaInvestida ?: custo.base ?: custo.minimo).coerceAtLeast(1)
                MagicCost.custoAreaPorRaio(basico.toDouble(), ctx.raioAreaMetros.coerceAtLeast(1))
            }
            else -> {
                val base = (energiaInvestida ?: custo.base ?: custo.minimo).coerceAtLeast(0)
                MagicCost.custoAjustadoPorTamanho(base, ctx.mtAlvo)
            }
        }.coerceAtLeast(custo.minimo)

        // Bloqueio (p.12) e Encantamento não reduzem por NH; as demais, sim.
        val naoReduz = TipoClasseMagia.BLOQUEIO in ctx.classe.classes
        return if (naoReduz) bruto else MagicCost.custoAjustadoPorNH(bruto, nhParaCusto(ctx))
    }

    // ─────────────── RESULTADO da operação (une MA-1) ───────────────

    /** Resultado completo de uma conjuração já rolada. */
    data class ResultadoConjuracao(
        val resultado: ResultadoOperacao,
        val nhEfetivo: Int,
        val margem: Int,                 // NH efetivo − rolagem (positivo = folga)
        val custoAPagar: Int,
        /** Preenchido só em FALHA_CRITICA. */
        val choqueRetorno: EfeitoChoqueRetorno? = null,
        /**
         * true quando a mágica é RESISTÍVEL e obteve SUCESSO NORMAL (não decisivo): o efeito ainda
         * depende da Disputa Rápida de resistência ([resolverResistencia]). Em sucesso DECISIVO a
         * resistível funciona automaticamente (p.13). Em fracasso/crítico não há resistência a rolar.
         */
        val exigeResistencia: Boolean = false,
    )

    /**
     * Resolve a rolagem 3d do operador contra o NH efetivo. Custo conforme p.7–8 (decisivo perdoa;
     * fracasso paga 1 exceto Informação; crítico paga tudo + choque). Marca [exigeResistencia]
     * quando é resistível e o sucesso foi normal.
     */
    fun resolver(
        nhEfetivo: Int,
        rolagem3d: Int,
        custoTotal: Int,
        classe: ClasseParseada,
        rolagemChoqueRetorno3d: Int? = null,
    ): ResultadoConjuracao {
        val res = MagicOperationRuling.classificar(rolagem3d, nhEfetivo)
        val ehInformacao = TipoClasseMagia.INFORMACAO in classe.classes
        val custo = MagicOperationRuling.custoAPagar(res, custoTotal, ehInformacao)
        val choque = if (res == ResultadoOperacao.FALHA_CRITICA)
            MagicChoqueRetorno.consultar(rolagemChoqueRetorno3d ?: rolagem3d) else null
        val resistivel = classe.resistencia != null
        val exigeResist = resistivel && res == ResultadoOperacao.SUCESSO // decisivo = automático
        return ResultadoConjuracao(
            resultado = res,
            nhEfetivo = nhEfetivo,
            margem = nhEfetivo - rolagem3d,
            custoAPagar = custo,
            choqueRetorno = choque,
            exigeResistencia = exigeResist,
        )
    }

    // ─────────────── RESISTÊNCIA (Disputa Rápida, p.13–14) ───────────────

    /** Resultado da disputa de resistência de um alvo. */
    data class ResultadoResistencia(
        val alvoResistiu: Boolean,
        val margemOperador: Int,
        val margemAlvo: Int,
    )

    /**
     * Disputa Rápida da mágica resistível (p.14): compara a margem de sucesso do OPERADOR com a
     * margem do teste de RESISTÊNCIA do alvo. O operador vence (afeta) só se sua margem for MAIOR;
     * empate ou derrota → o alvo resiste (mas o operador paga o custo total mesmo assim).
     *
     *  - [nhOperadorEfetivo] já inclui o −Abascanto do alvo (o caller aplica; ver [penalidadeAbascantoOperador]).
     *  - [regraDo16]: para alvo vivo/senciente, o teste do operador é limitado a NH 16 (p.14 / MB349).
     *  - [resistenciaAlvo] = atributo de resistência do alvo + Abascanto (o caller soma).
     */
    fun resolverResistencia(
        nhOperadorEfetivo: Int,
        rolagemOperador3d: Int,
        resistenciaAlvo: Int,
        rolagemAlvo3d: Int,
        regraDo16: Boolean = true,
    ): ResultadoResistencia {
        val nhOp = if (regraDo16) minOf(nhOperadorEfetivo, 16) else nhOperadorEfetivo
        val margemOp = nhOp - rolagemOperador3d
        val margemAlvo = resistenciaAlvo - rolagemAlvo3d
        // Disputa Rápida: maior margem vence; empate favorece o DEFENSOR (o alvo resiste).
        val alvoResistiu = margemAlvo >= margemOp
        return ResultadoResistencia(alvoResistiu, margemOp, margemAlvo)
    }

    /** Penalidade que o Abascanto (Resistência a Magia) do alvo impõe ao teste do operador (p.34). */
    fun penalidadeAbascantoOperador(abascantoAlvo: Int): Int = -abascantoAlvo.coerceAtLeast(0)

    // ─────────────── ESCALA DE EFEITO por energia (p.9/14) ───────────────

    /**
     * Teto de "níveis de efeito" que o operador pode comprar com energia: o MAIOR entre o número de
     * níveis previsto na mágica e o nível de Aptidão Mágica do operador (p.9). Mágicas sem limite
     * declarado ([niveisDeclarados] = null) só respeitam o que o operador puder pagar.
     */
    fun tetoNiveisEfeito(niveisDeclarados: Int?, aptidaoMagica: Int): Int? {
        if (niveisDeclarados == null) return null // sem teto: gasta o que puder
        return maxOf(niveisDeclarados, aptidaoMagica.coerceAtLeast(0))
    }

    /** Diretriz de combate (p.14): energia investida → dados de dano (1d por ponto). */
    fun dadosDeDanoPorEnergia(energia: Int): Int = energia.coerceAtLeast(0)

    /** Diretriz (p.14): energia → segundos de cegueira (1s por ponto). */
    fun segundosDeCegueiraPorEnergia(energia: Int): Int = energia.coerceAtLeast(0)

    // ─────────────── TEMPO DE OPERAÇÃO reduzido por NH alto (p.9) ───────────────

    /**
     * Tempo de operação ajustado pelo NH BÁSICO (p.9): NH 20–24 reduz pela metade, 25–29 por 1/4, e
     * a cada +5 níveis reduz mais uma vez pela metade. Mínimo de 1 segundo. Projétil/Bloqueio têm
     * tempo fixo por regra própria — o caller NÃO deve chamar isto para essas classes.
     */
    fun tempoOperacaoAjustado(tempoBaseSeg: Int, nhBasico: Int): Int {
        if (tempoBaseSeg <= 1) return tempoBaseSeg.coerceAtLeast(0)
        // nº de "metades" a aplicar: 1 em 20–24, 2 em 25–29, 3 em 30–34...
        val metades = if (nhBasico < 20) 0 else 1 + (nhBasico - 20) / 5
        if (metades == 0) return tempoBaseSeg
        var t = tempoBaseSeg.toDouble()
        repeat(metades) { t = kotlin.math.ceil(t / 2.0) }
        return t.toInt().coerceAtLeast(1)
    }
}
