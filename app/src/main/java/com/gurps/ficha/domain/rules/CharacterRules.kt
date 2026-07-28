package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.TipoCusto
import com.gurps.ficha.data.DataRepository

object CharacterRules {
    var DATA_REPOSITORY_INSTANCE: DataRepository? = null

    private val tabelaGdP = mapOf(
        1 to "1d-6", 2 to "1d-6",
        3 to "1d-5", 4 to "1d-5",
        5 to "1d-4", 6 to "1d-4",
        7 to "1d-3", 8 to "1d-3",
        9 to "1d-2", 10 to "1d-2",
        11 to "1d-1", 12 to "1d-1",
        13 to "1d", 14 to "1d",
        15 to "1d+1", 16 to "1d+1",
        17 to "1d+2", 18 to "1d+2",
        19 to "2d-1", 20 to "2d-1",
        21 to "2d", 22 to "2d",
        23 to "2d+1", 24 to "2d+1",
        25 to "2d+2", 26 to "2d+2",
        27 to "3d-1", 28 to "3d-1",
        29 to "3d", 30 to "3d"
    )

    private val tabelaGeB = mapOf(
        1 to "1d-5", 2 to "1d-5",
        3 to "1d-4", 4 to "1d-4",
        5 to "1d-3", 6 to "1d-3",
        7 to "1d-2", 8 to "1d-2",
        9 to "1d-1", 10 to "1d",
        11 to "1d+1", 12 to "1d+2",
        13 to "2d-1", 14 to "2d",
        15 to "2d+1", 16 to "2d+2",
        17 to "3d-1", 18 to "3d",
        19 to "3d+1", 20 to "3d+2",
        21 to "4d-1", 22 to "4d",
        23 to "4d+1", 24 to "4d+2",
        25 to "5d-1", 26 to "5d",
        27 to "5d+1", 28 to "5d+1",
        29 to "5d+2", 30 to "5d+2"
    )

    fun calcularNivelCarga(baseCarga: Float, pesoTotal: Float): Int {
        return when {
            pesoTotal <= baseCarga -> 0
            pesoTotal <= baseCarga * 2 -> 1
            pesoTotal <= baseCarga * 3 -> 2
            pesoTotal <= baseCarga * 6 -> 3
            pesoTotal <= baseCarga * 10 -> 4
            else -> 5
        }
    }

    fun calcularDeslocamentoAtual(deslocamentoBasico: Int, nivelCarga: Int): Int {
        val deslocamento = when (nivelCarga) {
            0 -> deslocamentoBasico
            1 -> (deslocamentoBasico * 0.8).toInt()
            2 -> (deslocamentoBasico * 0.6).toInt()
            3 -> (deslocamentoBasico * 0.4).toInt()
            4 -> (deslocamentoBasico * 0.2).toInt()
            else -> 1
        }
        // GURPS: penalidades de carga reduzem o deslocamento, mas mantemos mínimo 1 para personagem móvel.
        return deslocamento.coerceAtLeast(1)
    }

    fun calcularPontosAtributos(
        forca: Int,
        destreza: Int,
        inteligencia: Int,
        vitalidade: Int,
        forcaBase: Int = 10,
        destrezaBase: Int = 10,
        inteligenciaBase: Int = 10,
        vitalidadeBase: Int = 10
    ): Int {
        return (forca - forcaBase) * 10 + (destreza - destrezaBase) * 20 +
            (inteligencia - inteligenciaBase) * 20 + (vitalidade - vitalidadeBase) * 10
    }

    fun calcularPontosSecundarios(
        modPontosVida: Int,
        modVontade: Int,
        modPercepcao: Int,
        modPontosFadiga: Int,
        modVelocidadeBasica: Float,
        modDeslocamentoBasico: Int
    ): Int {
        val passosVelocidade = calcularPassosVelocidadeBasica(modVelocidadeBasica)
        return modPontosVida * 2 + modVontade * 5 + modPercepcao * 5 +
            modPontosFadiga * 3 + passosVelocidade * 5 +
            modDeslocamentoBasico * 5
    }

    fun calcularPassosVelocidadeBasica(modVelocidadeBasica: Float): Int {
        // 1 passo = 0.25 de Velocidade Básica (5 pontos por passo), com arredondamento consistente.
        return kotlin.math.round(modVelocidadeBasica / 0.25f).toInt()
    }

    fun calcularDanoGdP(st: Int): String {
        if (st <= 0) return "0"
        return tabelaGdP[st] ?: calcularDanoGdPExtrapolado(st)
    }

    fun calcularDanoGeB(st: Int): String {
        if (st <= 0) return "0"
        return tabelaGeB[st] ?: calcularDanoGeBExtrapolado(st)
    }

    fun resolverDanoPorSt(danoRaw: String, st: Int, bonusPorDado: Int = 0): String {
        var resolved = danoRaw
            .replace("Ã—", "×")
            .replace("—", "—")

        // 1. Resolve tokens baseados em ST (GdP/GeB), preservando o restante da expressão.
        resolved = resolved.replace(Regex("\\bGdP\\b", RegexOption.IGNORE_CASE), calcularDanoGdP(st))
        resolved = resolved.replace(Regex("\\bGeB\\b", RegexOption.IGNORE_CASE), calcularDanoGeB(st))

        // 2. Aplicar Bônus por Dado (Mestre de Armas)
        if (bonusPorDado > 0) {
            val diceMatch = Regex("(\\d+)d").find(resolved)
            if (diceMatch != null) {
                val numDice = diceMatch.groupValues[1].toInt()
                val totalExtra = numDice * bonusPorDado
                // Inserir o bônus extra na string antes de simplificar
                resolved = resolved.replace("${numDice}d", "${numDice}d+$totalExtra")
            }
        }

        // 3. Simplificar expressões (ex: 1d+2+2 -> 1d+4)
        // Regex para capturar: (dados)d (mod1) (mod2) (tipo)
        val fullRegex = Regex("^\\s*(\\d+)d(?:\\s*([+-]\\d+))?\\s*([+-]\\d+)\\s*(.*)$")
        val match = fullRegex.find(resolved)
        if (match != null) {
            val dados = match.groupValues[1].toInt()
            val mod1 = match.groupValues[2].toIntOrNull() ?: 0
            val mod2 = match.groupValues[3].toIntOrNull() ?: 0
            val sufixo = match.groupValues[4]
            val modFinal = mod1 + mod2
            val danoFormatado = when {
                modFinal > 0 -> "${dados}d+$modFinal"
                modFinal < 0 -> "${dados}d$modFinal"
                else -> "${dados}d"
            }
            return (danoFormatado + " " + sufixo).trim()
        }
        return resolved
    }

    private fun calcularDanoGdPExtrapolado(st: Int): String {
        // Continua o padrão da tabela em passos de +1 por 2 níveis de ST após ST 30.
        val baseSt = 30
        val basePips = 3 * 6 // 3d
        val incremento = ((st - baseSt) + 1) / 2
        return formatarDanoPorPips(basePips + incremento)
    }

    private fun calcularDanoGeBExtrapolado(st: Int): String {
        // Continua o padrão da tabela em passos de +1 por 2 níveis de ST após ST 30.
        val baseSt = 30
        val basePips = 5 * 6 + 2 // 5d+2
        val incremento = ((st - baseSt) + 1) / 2
        return formatarDanoPorPips(basePips + incremento)
    }

    private fun formatarDanoPorPips(totalPips: Int): String {
        if (totalPips <= 0) return "0"
        val dados = totalPips / 6
        val resto = totalPips % 6
        return when {
            resto == 0 -> "${dados}d"
            resto <= 2 -> "${dados}d+$resto"
            else -> "${dados + 1}d-${6 - resto}"
        }
    }

    /**
     * Aplica ampliações e limitações sobre um custo já calculado (MB p.102).
     *
     * Extraído em 28/07 para as regras de ST/DX Braçal poderem reusá-lo: uma
     * [com.gurps.ficha.domain.rules.traits.TraitRule] que devolve `calculateCost`
     * pula o cálculo padrão, então precisaria repetir esta conta — e o livro
     * manda aplicar as mesmas limitações da ST normal à ST Braçal.
     *
     * Regras embutidas: teto de −80% para o líquido negativo; ampliação
     * arredonda para cima, limitação elimina frações.
     */
    fun aplicarModificadoresPercentuais(
        valorBase: Int,
        modificadores: List<ModificadorSelecao>
    ): Int {
        if (modificadores.isEmpty()) return valorBase

        val somaPercentual = modificadores.sumOf {
            // bonusBase (fixo) + (valor*níveis se porNivel, senão valor).
            // Cobre modificadores tipo Cone (+50% base + 10%/metro);
            // bonusBase=0 (default) preserva comportamento anterior.
            it.bonusBase + if (it.porNivel) it.valor * it.niveis else it.valor
        }

        // Regra canônica: Limite de -80% para modificadores negativos líquidos (pág. 102)
        val percentualFinal = somaPercentual.coerceAtLeast(-80)
        val multiplicador = 1.0 + (percentualFinal / 100.0)
        return if (percentualFinal < 0)
            kotlin.math.floor(valorBase * multiplicador).toInt()
        else
            kotlin.math.ceil(valorBase * multiplicador).toInt()
    }

    fun calcularCustoVantagem(
        personagem: com.gurps.ficha.model.Personagem? = null, // Tornar opcional para compatibilidade
        definicaoId: String,
        tipoCusto: TipoCusto,
        custoBase: Int,
        custoEscolhido: Int,
        nivel: Int,
        modificadores: List<ModificadorSelecao> = emptyList(),
        specialRule: String? = null,
        metadados: Map<String, String>? = null
    ): Int {
        // Tenta usar regra modular se existir
        val rule = com.gurps.ficha.domain.rules.traits.TraitRuleRegistry.getRuleFor(definicaoId)
        if (rule != null) {
            val s = com.gurps.ficha.model.VantagemSelecionada(
                definicaoId = definicaoId,
                nivel = nivel,
                custoBase = custoBase,
                tipoCusto = tipoCusto,
                specialRule = specialRule,
                metadados = metadados
            )
            val custoModular = rule.calculateCost(s, modificadores)
            if (custoModular != null) return custoModular
        }

        val valorBase = if (
            definicaoId.equals("aptidao_magica", ignoreCase = true) ||
            definicaoId.equals("aptidao_astral", ignoreCase = true) ||
            definicaoId.equals("elo_mental", ignoreCase = true)
        ) {
            // Aptidão Mágica: AM 0 = 5 pts, Níveis 1+ = 10 pts/nível.
            // O nível 1 corresponde a AM 0, nível 2 a AM 1, etc.
            5 + (nivel - 1) * 10
        } else {
            when (tipoCusto) {
                TipoCusto.POR_NIVEL -> custoBase * nivel
                else -> custoEscolhido
            }
        }

        val custoCalculado = aplicarModificadoresPercentuais(valorBase, modificadores)

        // Vantagem deve custar no mínimo 1 ponto se o base era positivo e não foi reduzido a zero
        return if (custoCalculado < 1 && valorBase > 0) 1 else custoCalculado
    }

    fun calcularCustoDesvantagem(
        tipoCusto: TipoCusto,
        custoBase: Int,
        custoEscolhido: Int,
        nivel: Int,
        autocontrole: Int?,
        modificadores: List<ModificadorSelecao> = emptyList(),
        specialRule: String? = null,
        metadados: Map<String, String>? = null
    ): Int {
        if (metadados != null && specialRule != null) {
            val baseCostForSpecialRule = when (specialRule) {
                "inimigos", "dependentes" -> calcularCustoInimigo(metadados, modificadores)
                "dependencia" -> calcularCustoDependencia(metadados)
                "reputacao" -> calcularCustoReputacao(metadados)
                "dever" -> calcularCustoDever(metadados)
                "dor_cronica" -> calcularCustoDorCronica(metadados)
                "fraqueza" -> calcularCustoFraqueza(metadados)
                "vulnerabilidade" -> calcularCustoVulnerabilidade(metadados)
                "manutencao" -> calcularCustoManutencao(metadados)
                "vicio" -> calcularCustoVicio(metadados)
                "maldicao_divina" -> calcularCustoMaldicaoDivina(metadados)
                "habilidades_modulares" -> calcularCustoHabilidadesModulares(metadados)
                else -> null
            }
            if (baseCostForSpecialRule != null) {
                val somaPercentual = modificadores.sumOf {
                    // bonusBase (fixo) + (valor*níveis se porNivel, senão valor).
            // Cobre modificadores tipo Cone (+50% base + 10%/metro);
            // bonusBase=0 (default) preserva comportamento anterior.
            it.bonusBase + if (it.porNivel) it.valor * it.niveis else it.valor
                }
                val percentualFinal = somaPercentual.coerceAtLeast(-80)
                val multiplicadorMod = 1.0 + (percentualFinal / 100.0)
                
                // Truncar frações (Eliminar frações como pedido em Resistente)
                val total = (baseCostForSpecialRule * multiplicadorMod).toInt()
                return if (total == 0 && baseCostForSpecialRule < 0) -1 else total
            }
        }

        // BUG da DUPLA APLICAÇÃO de autocontrole (universal p/ qualquer
        // desvantagem com autocontrole 6 ou 15): o dialog calcula e PERSISTE
        // em `custoEscolhido` o valor JÁ multiplicado pelo autocontrole
        // (ex: Avareza -10 -> -5). Se o autocontrole fosse reaplicado sobre
        // `custoEscolhido`, viraria -5*0.5 = -2 na lista/edição. Por isso,
        // QUANDO há autocontrole, a base do multiplicador é o `custoBase`
        // CRU (-10), nunca o `custoEscolhido` (pós-autocontrole). Sem
        // autocontrole, mantém `custoEscolhido` (preserva escolha/variável,
        // ex: Intolerância custoBase=-10 mas custoEscolhido=-5 é a verdade).
        val custoComAutocontrole = autocontrole?.let { ac ->
            val multiplicador = when (ac) {
                6 -> 2.0
                9 -> 1.5
                12 -> 1.0
                15 -> 0.5
                else -> 1.0
            }
            val baseCrua = when (tipoCusto) {
                TipoCusto.POR_NIVEL -> custoBase * nivel
                // ESCOLHA/VARIÁVEL: aqui `custoEscolhido` é a escolha do jogador
                // (Flashbacks −5/−10/−20), não um valor já multiplicado — o
                // diálogo de escolha não aplica autocontrole. Usar `custoBase`
                // aqui descartava a escolha: Flashbacks −20 com NA 12 virava −5.
                // São 5 desvantagens com essa combinação no catálogo.
                TipoCusto.ESCOLHA, TipoCusto.VARIAVEL ->
                    if (custoEscolhido != 0) custoEscolhido else custoBase
                else -> custoBase
            }
            (baseCrua * multiplicador).toInt()
        } ?: when (tipoCusto) {
            TipoCusto.POR_NIVEL -> custoBase * nivel
            else -> custoEscolhido
        }

        val valorBase = if (custoComAutocontrole > 0) -custoComAutocontrole else custoComAutocontrole

        if (modificadores.isEmpty()) {
            return valorBase
        }

        val somaPercentual = modificadores.sumOf {
            // bonusBase (fixo) + (valor*níveis se porNivel, senão valor).
            // Cobre modificadores tipo Cone (+50% base + 10%/metro);
            // bonusBase=0 (default) preserva comportamento anterior.
            it.bonusBase + if (it.porNivel) it.valor * it.niveis else it.valor
        }

        val percentualFinal = somaPercentual.coerceAtLeast(-80)
        val multiplicadorMod = 1.0 + (percentualFinal / 100.0)
        val custoFinal = kotlin.math.ceil(valorBase * multiplicadorMod).toInt()
        return if (custoFinal == 0 && valorBase < 0) -1 else custoFinal
    }

    private fun calcularCustoInimigo(metadados: Map<String, String>, modificadores: List<ModificadorSelecao>): Int {
        val basePoder = metadados["basePoder"]?.toIntOrNull() ?: -5
        val multIntencao = metadados["multIntencao"]?.toFloatOrNull() ?: 1.0f
        val multFrequencia = metadados["multFrequencia"]?.toFloatOrNull() ?: 1.0f
        
        val valorBase = (basePoder * multIntencao * multFrequencia).toInt()
        
        val somaPercentual = modificadores.sumOf { it.valor }
        val percentualFinal = somaPercentual.coerceAtLeast(-80)
        val multiplicadorMod = 1.0 + (percentualFinal / 100.0)
        
        return kotlin.math.ceil(valorBase * multiplicadorMod).toInt()
    }

    private fun calcularCustoDependencia(metadados: Map<String, String>?): Int {
        val base = metadados?.get("baseRaridade")?.toIntOrNull() ?: metadados?.get("base")?.toIntOrNull() ?: -5
        val freq = metadados?.get("multFrequencia")?.toFloatOrNull() ?: 1.0f
        val ilegal = metadados?.get("ilegal")?.toBoolean() ?: false
        
        var total = base.toFloat() * freq
        if (ilegal) total -= 5
        
        return total.toInt()
    }

    private fun calcularCustoReputacao(metadados: Map<String, String>?): Int {
        val base = metadados?.get("baseReputacao")?.toIntOrNull() ?: -5
        val multGrupo = metadados?.get("multGrupo")?.toFloatOrNull() ?: 1.0f
        val multReconhecimento = metadados?.get("multReconhecimento")?.toFloatOrNull() ?: 1.0f
        
        val total = base.toFloat() * multGrupo * multReconhecimento
        return kotlin.math.ceil(total.toDouble()).toInt()
    }

    private fun calcularCustoDever(metadados: Map<String, String>?): Int {
        val base = metadados?.get("baseDever")?.toIntOrNull() ?: -5
        val perigoso = metadados?.get("perigoso")?.toBoolean() ?: false
        val involuntario = metadados?.get("involuntario")?.toBoolean() ?: false
        val inofensivo = metadados?.get("inofensivo")?.toBoolean() ?: false
        
        var total = base.toFloat()
        if (perigoso) total -= 5
        if (involuntario) total -= 5
        if (inofensivo) total += 5
        
        return total.toInt()
    }

    private fun calcularCustoDorCronica(metadados: Map<String, String>?): Int {
        val base = metadados?.get("baseIntensidade")?.toIntOrNull() ?: -5
        val multFreq = metadados?.get("multFrequencia")?.toFloatOrNull() ?: 1.0f
        
        val total = base.toFloat() * multFreq
        return kotlin.math.ceil(total.toDouble()).toInt()
    }

    private fun calcularCustoFraqueza(metadados: Map<String, String>?): Int {
        val base = metadados?.get("baseRaridade")?.toIntOrNull() ?: -5
        val multFreq = metadados?.get("multFrequencia")?.toFloatOrNull() ?: 1.0f
        
        val total = base.toFloat() * multFreq
        return kotlin.math.ceil(total.toDouble()).toInt()
    }

    private fun calcularCustoVulnerabilidade(metadados: Map<String, String>?): Int {
        val base = metadados?.get("baseRaridade")?.toIntOrNull() ?: -5
        val multDano = metadados?.get("multDano")?.toFloatOrNull() ?: 2.0f
        
        val total = base.toFloat() * multDano
        return kotlin.math.ceil(total.toDouble()).toInt()
    }

    private fun calcularCustoManutencao(metadados: Map<String, String>?): Int {
        val base = metadados?.get("baseManutencao")?.toIntOrNull() ?: -10
        val mult = metadados?.get("multIntervalo")?.toFloatOrNull() ?: 1.0f
        val total = base.toFloat() * mult
        return kotlin.math.ceil(total.toDouble()).toInt()
    }

    private fun calcularCustoVicio(metadados: Map<String, String>?): Int {
        val base = metadados?.get("baseVicio")?.toIntOrNull() ?: -5
        val efeito = metadados?.get("modEfeito")?.toIntOrNull() ?: 0
        val legalidade = metadados?.get("modLegalidade")?.toIntOrNull() ?: 0
        return base + efeito + legalidade
    }

    private fun calcularCustoMaldicaoDivina(metadados: Map<String, String>?): Int {
        return metadados?.get("custoCustom")?.toIntOrNull() ?: 0
    }

    fun calcularBonusPorDificuldade(dificuldade: Dificuldade, pontosGastos: Int): Int {
        val pts = pontosGastos.coerceAtLeast(1)
        return when (dificuldade) {
            Dificuldade.FACIL -> when {
                pts == 1 -> 0
                pts in 2..3 -> 1
                else -> 2 + (pts - 4) / 4
            }

            Dificuldade.MEDIA -> when {
                pts == 1 -> -1
                pts in 2..3 -> 0
                else -> 1 + (pts - 4) / 4
            }

            Dificuldade.DIFICIL -> when {
                pts == 1 -> -2
                pts in 2..3 -> -1
                else -> (pts - 4) / 4
            }

            Dificuldade.MUITO_DIFICIL -> when {
                pts == 1 -> -3
                pts in 2..3 -> -2
                else -> -1 + (pts - 4) / 4
            }
        }
    }

    /**
     * Calcula quantos pontos (pts) são necessários para atingir um determinado nível (NH).
     * Útil para o Mestre IA integrar fichas sugeridas de forma honesta com as regras.
     */
    fun calcularPontosParaNivel(dificuldade: Dificuldade, atributoValor: Int, nivelAlvo: Int): Int {
        val bonusDesejado = nivelAlvo - atributoValor
        
        return when (dificuldade) {
            Dificuldade.FACIL -> when {
                bonusDesejado <= 0 -> 1
                bonusDesejado == 1 -> 2
                else -> 4 + (bonusDesejado - 2) * 4
            }
            Dificuldade.MEDIA -> when {
                bonusDesejado <= -1 -> 1
                bonusDesejado == 0 -> 2
                else -> 4 + (bonusDesejado - 1) * 4
            }
            Dificuldade.DIFICIL -> when {
                bonusDesejado <= -2 -> 1
                bonusDesejado == -1 -> 2
                else -> 4 + bonusDesejado * 4
            }
            Dificuldade.MUITO_DIFICIL -> when {
                bonusDesejado <= -3 -> 1
                bonusDesejado == -2 -> 2
                else -> 4 + (bonusDesejado + 1) * 4
            }
        }
    }

    fun calcularCustoContato(nh: Int, frequencia: Float, confiabilidade: Float): Int {
        val custoBase = when (nh) {
            12 -> 1
            15 -> 2
            18 -> 3
            21 -> 4
            else -> 1
        }
        val finalVal = (custoBase * frequencia * confiabilidade.toDouble())
        return kotlin.math.ceil(finalVal).toInt().coerceAtLeast(1)
    }

    fun calcularCustoHabilidadesModulares(selecoes: Map<String, Any>): Int {
        var total = 0
        selecoes.forEach { (key, value) ->
            // chaves podem vir com prefixo "habmod_" (metadados persistidos) ou sem (Map<String, HabModTipoSel>)
            val id = key.removePrefix("habmod_")
            val niveis: Int = when (value) {
                is com.gurps.ficha.ui.features.traits.HabModTipoSel -> if (value.ativo) value.niveis else return@forEach
                else -> value.toString().toIntOrNull() ?: return@forEach
            }
            total += when (id) {
                "cerebro_eletronico" -> 6 + 4 * niveis
                "chips" -> 5 + 3 * niveis
                "poder_cosmico" -> 10 * niveis
                "supermemorizar" -> 5 + 3 * niveis
                else -> 0
            }
        }
        return total.coerceAtLeast(1)
    }

    fun calcularCustoAliado(basePoints: Int, frequencia: Float, multiplicadorGrupo: Int): Int {
        val finalVal = (basePoints * frequencia.toDouble() * multiplicadorGrupo)
        return kotlin.math.ceil(finalVal).toInt().coerceAtLeast(1)
    }

    fun calcularCustoPatrono(basePoints: Int, frequencia: Float, multiplicadores: Float, custoFixo: Int = 0): Int {
        val baseModificada = basePoints * multiplicadores + custoFixo
        val finalVal = baseModificada * frequencia.toDouble()
        return kotlin.math.ceil(finalVal).toInt().coerceAtLeast(1)
    }

    fun calcularCustoFavor(basePoints: Int, multiplicadores: Float, custoFixo: Int = 0, isContact: Boolean = false): Int {
        // Favor (Patrono) = 1/10 do custo de um Patrono com frequência 15- (x3)
        // Favor (Contato) = 1/5 do custo de um Contato com frequência 15- (x3)
        val baseModificada = basePoints * multiplicadores + custoFixo
        val freqBase = 3.0 // Sempre base 15-
        val divisor = if (isContact) 5.0 else 10.0
        val finalVal = (baseModificada * freqBase) / divisor
        return kotlin.math.ceil(finalVal).toInt().coerceAtLeast(1)
    }

    fun calcularCustoResistente(baseRaridade: Int, multiplicadorGrau: Float): Int {
        // O grau x1/3 era hardcoded como 0.33f (e 0.3333f noutro dialog) e
        // persistido assim no JSON. 15 * 0.33 = 4.95 -> floor -> 4, quando
        // GURPS manda 15 * (1/3) = 5.0 -> 5. Normalizamos para a fração
        // EXATA antes de calcular: qualquer valor ~1/3 vira 1.0/3.0,
        // ~1/2 vira 0.5, ~1 vira 1.0 — robusto a fichas já salvas.
        val grau = multiplicadorGrau.toDouble()
        val grauExato = when {
            kotlin.math.abs(grau - (1.0 / 3.0)) < 0.02 -> 1.0 / 3.0
            kotlin.math.abs(grau - 0.5) < 0.02 -> 0.5
            kotlin.math.abs(grau - 1.0) < 0.02 -> 1.0
            else -> grau
        }
        val finalVal = baseRaridade * grauExato
        // GURPS: "Elimine todas as frações" -> Truncar/Floor (1/3 exato já
        // dá inteiro nos casos tabelados; floor só morde graus baixos).
        return kotlin.math.floor(finalVal).toInt().coerceAtLeast(1)
    }

    fun calcularCustoPericiaRacial(dificuldade: String, nivelRelativo: Int): Int {
        if (nivelRelativo == 0) {
            return when (dificuldade.uppercase()) {
                "F" -> 1
                "M" -> 2
                "D" -> 4
                "MD" -> 8
                else -> 2
            }
        }
        
        // GURPS 4e p. 170: 
        // 1 pt = F/Atr, M/Atr-1, D/Atr-2, MD/Atr-3
        // 2 pts = F/Atr+1, M/Atr, D/Atr-1, MD/Atr-2
        // 4 pts = F/Atr+2, M/Atr+1, D/Atr, MD/Atr-1
        // 8 pts = F/Atr+3, M/Atr+2, D/Atr+1, MD/Atr
        // +4 pts per level after that
        
        val offset = when (dificuldade.uppercase()) {
            "F" -> 0
            "M" -> 1
            "D" -> 2
            "MD" -> 3
            else -> 1
        }
        
        val totalLevels = nivelRelativo + offset
        
        return when {
            totalLevels <= 0 -> 1 // Mínimo 1 pt para ter a perícia
            totalLevels == 1 -> 2
            totalLevels == 2 -> 4
            totalLevels == 3 -> 8
            else -> 8 + (totalLevels - 3) * 4
        }
    }

    /**
     * BÔNUS racial à perícia (Módulo Básico p.453). Tabela LINEAR, NÃO
     * depende da dificuldade: +1 ao NH = 2 pts, +2 = 4, +3 = 6. Máximo
     * permitido +3. Não concede a perícia — só dá o bônus ao usá-la.
     * Ex.: Elfo "+1 em Arco [2]".
     */
    fun calcularCustoBonusPericiaRacial(bonusNH: Int): Int {
        val b = bonusNH.coerceIn(1, 3)
        return b * 2 // +1=2, +2=4, +3=6
    }
}
