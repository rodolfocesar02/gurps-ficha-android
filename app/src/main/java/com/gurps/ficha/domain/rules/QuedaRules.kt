package com.gurps.ficha.domain.rules

import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * **Dano por queda e colisão** — Lote MB-9 (MB p.430-432).
 *
 * A conta tem três passos, e o segundo é o que ninguém decora:
 *
 * 1. A altura vira **velocidade**, por uma tabela — ou pela fórmula
 *    `√(21,4 × g × altura)`, que é de onde a tabela sai.
 * 2. A velocidade vira **dados de dano**: `(PV × velocidade) ÷ 100`.
 * 3. As **frações** têm uma regra própria, que não é arredondamento comum.
 *
 * > Um objeto em colisão causa um número de dados de dano por contusão igual a
 * > **(PV × velocidade)/100**. Se o dano for menor que 1d, considere as frações
 * > até **0,25 como 1d-3**, frações até **0,5 como 1d-2** e qualquer fração maior
 * > como **1d-1**. Caso contrário, arredonde apenas as frações de **0,5 ou mais**
 * > para cima.
 *
 * ## ⚠️ O PV é do QUE CAI, não de quem apanha
 *
 * É a parte mais contraintuitiva da regra, e o livro explica por quê:
 *
 * > A massa só importa indiretamente: objetos maciços normalmente têm PV alto,
 * > mas certamente doeria mais chocar-se contra uma **locomotiva** do que contra
 * > um **travesseiro de mesma massa**! O PV leva em conta a massa e a força
 * > estrutural.
 *
 * Numa queda, quem cai bate no chão — então o PV que entra na conta é **o do
 * personagem**. Ele leva o dano e o chão também (MB p.432, *Objetos Imóveis*).
 */
object QuedaRules {

    /**
     * A **Tabela de Velocidade em Quedas** do livro (MB p.432), transcrita.
     *
     * Cada par é `(altura máxima da faixa, velocidade)`. A faixa "30–32 metros"
     * vira `32 to 26`: qualquer altura até 32 m dá 26 m/s.
     */
    private val TABELA: List<Pair<Int, Int>> = listOf(
        1 to 5, 2 to 7, 3 to 8, 4 to 9, 5 to 10, 6 to 11, 7 to 12, 8 to 13,
        9 to 14, 11 to 15, 12 to 16, 14 to 17, 15 to 18, 17 to 19, 19 to 20,
        21 to 21, 23 to 22, 25 to 23, 27 to 24, 29 to 25, 32 to 26, 34 to 27,
        37 to 28, 39 to 29, 42 to 30, 45 to 31, 48 to 32, 51 to 33, 54 to 34,
        57 to 35, 61 to 36, 64 to 37, 67 to 38, 71 to 39, 75 to 40, 79 to 41,
        82 to 42, 86 to 43, 90 to 44, 95 to 45, 99 to 46, 103 to 47, 108 to 48,
        112 to 49
    )

    /**
     * A velocidade de queda, em metros por segundo.
     *
     * ## 🔴 Por que a tabela e não só a fórmula
     *
     * O livro dá as duas coisas — a tabela e a fórmula
     * `√(21,4 × g × altura)` — e **elas discordam**. Conferi ponto a ponto: em
     * 3 de 15 alturas testadas a fórmula devolve **1 a menos** que a tabela
     * impressa (30 m dá 25 pela fórmula e **26** na tabela; 40 m dá 29 contra
     * **30**; 100 m dá 46 contra **47**).
     *
     * Não é erro do livro: as linhas são **faixas** ("30–32 metros"), e o valor
     * publicado corresponde ao topo da faixa, não à base.
     *
     * ⚠️ **A tabela ganha.** É o que o jogador vai conferir no livro aberto na
     * mesa, e um app que diz 25 onde a página diz 26 perde a confiança dele —
     * mesmo estando "matematicamente certo".
     *
     * A fórmula continua servindo para o que a tabela não cobre: **gravidade
     * diferente da Terra** e quedas acima de 112 m.
     */
    fun velocidadeDaQueda(alturaMetros: Int, gravidade: Double = 1.0): Int {
        if (alturaMetros <= 0 || gravidade <= 0.0) return 0
        if (gravidade == 1.0) {
            TABELA.firstOrNull { alturaMetros <= it.first }?.let { return it.second }
        }
        return sqrt(21.4 * gravidade * alturaMetros).roundToInt()
    }

    /** Até onde a tabela impressa vai; acima disso vale a fórmula. */
    val ALTURA_MAXIMA_DA_TABELA: Int = TABELA.last().first

    /** O dano de uma colisão, já escrito como o jogador rola: "3d", "1d-2". */
    data class Dano(val dados: Int, val ajuste: Int) {
        val formula: String
            get() = when {
                dados <= 0 -> "nenhum"
                ajuste == 0 -> "${dados}d"
                ajuste > 0 -> "${dados}d+$ajuste"
                else -> "${dados}d$ajuste"
            }
    }

    /**
     * `(PV × velocidade) ÷ 100`, com a regra de frações do livro.
     *
     * ⚠️ **As frações abaixo de 1d não são arredondamento** — são três faixas
     * fixas: até 0,25 → `1d-3`; até 0,5 → `1d-2`; acima → `1d-1`. Arredondar
     * "normalmente" daria **zero dano** numa queda pequena, e o livro nunca deixa
     * uma queda sair de graça.
     */
    fun danoDaColisao(pv: Int, velocidade: Int): Dano {
        if (pv <= 0 || velocidade <= 0) return Dano(0, 0)
        val bruto = (pv.toDouble() * velocidade) / 100.0
        if (bruto < 1.0) {
            return when {
                bruto <= 0.25 -> Dano(1, -3)
                bruto <= 0.5 -> Dano(1, -2)
                else -> Dano(1, -1)
            }
        }
        // Daqui para cima é arredondamento comum: 0,5 sobe.
        return Dano(bruto.roundToInt().coerceAtLeast(1), 0)
    }

    /** O caminho inteiro: altura → velocidade → dano. */
    fun danoDaQueda(pv: Int, alturaMetros: Int, gravidade: Double = 1.0): Dano =
        danoDaColisao(pv, velocidadeDaQueda(alturaMetros, gravidade))

    /**
     * ⚠️ **Objeto pontudo causa METADE do dano — mas de outro tipo.**
     *
     * > Um objeto no formato de uma bala, pontudo ou com cravos causa apenas
     * > metade do dano, mas esse dano é **perfurante, por corte ou por
     * > perfuração** em vez de por contusão.
     *
     * Metade em dados, e a troca de tipo costuma valer **mais** que a metade
     * perdida: perfuração multiplica o ferimento por 1,5 ou 2.
     */
    fun metadeDoDano(d: Dano): Dano {
        if (d.dados <= 0) return d
        val metade = d.dados / 2
        return if (metade < 1) Dano(1, -2) else Dano(metade, d.ajuste)
    }

    /** A conta escrita, para o jogador poder conferir de onde saiu o número. */
    fun explicacao(pv: Int, alturaMetros: Int, gravidade: Double = 1.0): String {
        val v = velocidadeDaQueda(alturaMetros, gravidade)
        val d = danoDaQueda(pv, alturaMetros, gravidade)
        val g = if (gravidade == 1.0) "" else " · gravidade ${gravidade}G"
        return "${d.formula} de contusão — caiu de $alturaMetros m, velocidade $v m/s" +
            "$g · (PV $pv × $v) ÷ 100"
    }

    /**
     * O que o livro manda lembrar depois do dano.
     *
     * Não é automatizável — depende do chão, do treino e do Mestre —, mas é o que
     * o jogador pergunta em seguida.
     */
    const val AVISO_ACROBACIA =
        "Um teste de Acrobacia bem-sucedido reduz o dano da queda como se ela " +
            "fosse 5 m mais curta (MB p.430). Cair em água, neve ou lama também " +
            "reduz — quanto, é decisão do Mestre."
}
