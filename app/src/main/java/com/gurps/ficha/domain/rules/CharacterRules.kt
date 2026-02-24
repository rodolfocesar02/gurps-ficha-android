package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.TipoCusto

object CharacterRules {

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

    fun calcularPontosAtributos(forca: Int, destreza: Int, inteligencia: Int, vitalidade: Int): Int {
        return (forca - 10) * 10 + (destreza - 10) * 20 +
            (inteligencia - 10) * 20 + (vitalidade - 10) * 10
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
        definicaoId: String,
        tipoCusto: TipoCusto,
        custoBase: Int,
        custoEscolhido: Int,
        nivel: Int
    ): Int {
        if (
            definicaoId.equals("aptidao_magica", ignoreCase = true) ||
            definicaoId.equals("elo_mental", ignoreCase = true)
        ) {
            return 5 + (nivel - 1) * 10
        }
        val valor = when (tipoCusto) {
            TipoCusto.POR_NIVEL -> custoBase * nivel
            else -> custoEscolhido
        }
        return if (valor < 0) 0 else valor
    }

    fun calcularCustoDesvantagem(
        tipoCusto: TipoCusto,
        custoBase: Int,
        custoEscolhido: Int,
        nivel: Int,
        autocontrole: Int?
    ): Int {
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
        return if (custoComAutocontrole > 0) -custoComAutocontrole else custoComAutocontrole
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
}
