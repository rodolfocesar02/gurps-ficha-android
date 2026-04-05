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

    fun resolverDanoPorSt(danoRaw: String, st: Int): String {
        var resolved = danoRaw
            .replace("Ã—", "×")
            .replace("â€”", "—")

        // Resolve tokens baseados em ST (GdP/GeB), preservando o restante da expressão.
        resolved = resolved.replace(Regex("\\bGdP\\b", RegexOption.IGNORE_CASE), calcularDanoGdP(st))
        resolved = resolved.replace(Regex("\\bGeB\\b", RegexOption.IGNORE_CASE), calcularDanoGeB(st))

        // Caso simples: soma modificadores mantendo notacao intuitiva (ex.: 1d+3 em vez de 2d-3).
        val simple = Regex("^\\s*(\\d+)d(?:\\s*([+-]\\d+))?\\s*([+-]\\d+)\\s*(.*)$").find(resolved)
        if (simple != null) {
            val dados = simple.groupValues[1].toInt()
            val modBase = simple.groupValues[2].toIntOrNull() ?: 0
            val modExtra = simple.groupValues[3].toIntOrNull() ?: 0
            val sufixo = simple.groupValues[4]
            val modFinal = modBase + modExtra
            val danoIntuitivo = when {
                modFinal > 0 -> "${dados}d+$modFinal"
                modFinal < 0 -> "${dados}d$modFinal"
                else -> "${dados}d"
            }
            return (danoIntuitivo + " " + sufixo).trim()
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

    fun calcularCustoVantagem(
        personagem: com.gurps.ficha.model.Personagem? = null, // Tornar opcional para compatibilidade
        definicaoId: String,
        tipoCusto: TipoCusto,
        custoBase: Int,
        custoEscolhido: Int,
        nivel: Int,
        modificadores: List<ModificadorSelecao> = emptyList(),
        metadados: Map<String, String>? = null
    ): Int {
        // Tenta usar regra modular se existir
        val rule = com.gurps.ficha.domain.rules.traits.TraitRuleRegistry.getRuleFor(definicaoId)
        if (rule != null) {
            val s = com.gurps.ficha.model.VantagemSelecionada(
                definicaoId = definicaoId,
                nivel = nivel,
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

        if (modificadores.isEmpty()) {
            return valorBase
        }

        val somaPercentual = modificadores.sumOf {
            if (it.porNivel) it.valor * it.niveis else it.valor
        }

        // Regra canônica: Limite de -80% para modificadores negativos líquidos (pág. 102)
        val percentualFinal = somaPercentual.coerceAtLeast(-80)

        val multiplicador = 1.0 + (percentualFinal / 100.0)
        val custoCalculado = kotlin.math.ceil(valorBase * multiplicador).toInt()

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
                else -> null
            }
            if (baseCostForSpecialRule != null) {
                // For special rules, the base cost is already calculated,
                // and modifiers are applied later.
                // The autocontrole logic is skipped for these special rules.
                val somaPercentual = modificadores.sumOf {
                    if (it.porNivel) it.valor * it.niveis else it.valor
                }
                val percentualFinal = somaPercentual.coerceAtLeast(-80)
                val multiplicadorMod = 1.0 + (percentualFinal / 100.0)
                return kotlin.math.ceil(baseCostForSpecialRule * multiplicadorMod).toInt()
            }
        }

        val custoSemAutocontrole = when (tipoCusto) {
            TipoCusto.POR_NIVEL -> custoBase * nivel
            else -> custoEscolhido
        }
        val custoComAutocontrole = autocontrole?.let { ac ->
            val multiplicador = when (ac) {
                6 -> 2.0
                9 -> 1.5
                12 -> 1.0
                15 -> 0.5
                else -> 1.0
            }
            (custoSemAutocontrole * multiplicador).toInt()
        } ?: custoSemAutocontrole

        val valorBase = if (custoComAutocontrole > 0) -custoComAutocontrole else custoComAutocontrole

        if (modificadores.isEmpty()) {
            return valorBase
        }

        val somaPercentual = modificadores.sumOf {
            if (it.porNivel) it.valor * it.niveis else it.valor
        }

        val percentualFinal = somaPercentual.coerceAtLeast(-80)
        val multiplicadorMod = 1.0 + (percentualFinal / 100.0)
        return kotlin.math.ceil(valorBase * multiplicadorMod).toInt()
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
        val base = metadados?.get("baseRaridade")?.toIntOrNull() ?: -5
        val freq = metadados?.get("multFrequencia")?.toFloatOrNull() ?: 1.0f
        val ilegal = metadados?.get("ilegal")?.toBoolean() ?: false
        
        var total = base.toFloat() * freq
        if (ilegal) total -= 5
        
        return kotlin.math.ceil(total.toDouble()).toInt()
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
        // Favor (Patrono) = 1/10 do custo de um Patrono com frequ\u00eancia 15- (x3)
        // Favor (Contato) = 1/5 do custo de um Contato com frequ\u00eancia 15- (x3)
        val baseModificada = basePoints * multiplicadores + custoFixo
        val freqBase = 3.0 // Sempre base 15-
        val divisor = if (isContact) 5.0 else 10.0
        val finalVal = (baseModificada * freqBase) / divisor
        return kotlin.math.ceil(finalVal).toInt().coerceAtLeast(1)
    }
}
