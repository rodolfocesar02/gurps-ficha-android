package com.gurps.ficha.domain.rules

/**
 * **Prender o fôlego** — Lote MB-5 (MB p.356).
 *
 * > A HT determina o tempo que um personagem é capaz de prender seu fôlego:
 * >
 * > | Sem Esforço (sentado quietinho, meditando) | HT×10 segundos |
 * > | Esforço Moderado (operando um veículo, movimentos leves na água, caminhando) | HT×4 segundos |
 * > | Esforço Pesado (escalando, combatendo ou correndo) | HT segundos |
 * >
 * > Multiplique todas as durações por **1,5** se conseguir hiperventilar — ou por
 * > **2,5** se hiperventilar com **oxigênio puro**.
 *
 * ## Por que isto vira automação e não uma nota no manual
 *
 * Porque é multiplicação sobre um número que a ficha já tem, e porque aparece no
 * pior momento possível: debaixo d'água, dentro do gás, sendo estrangulado. É
 * conta simples que ninguém faz direito sob pressão — e errar para mais mata o
 * personagem.
 *
 * ⚠️ **A diferença entre as três linhas é brutal.** Uma HT 12 aguenta **120
 * segundos** parada e **12 segundos** lutando. Dez vezes menos. Quem só decora
 * "HT vezes dez" morre achando que tinha dois minutos.
 */
object FolegoRules {

    /** O quanto o personagem está se esforçando enquanto segura o ar. */
    enum class Esforco(val rotulo: String, val multiplicador: Int, val exemplo: String) {
        NENHUM("Sem esforço", 10, "parado, sentado, meditando"),
        MODERADO("Esforço moderado", 4, "caminhando, nadando devagar, pilotando"),
        PESADO("Esforço pesado", 1, "lutando, correndo, escalando")
    }

    /** O preparo antes de segurar o ar. */
    enum class Preparo(val rotulo: String, val fator: Double) {
        NORMAL("Respirou fundo", 1.0),
        HIPERVENTILOU("Hiperventilou", 1.5),
        OXIGENIO_PURO("Hiperventilou com oxigênio puro", 2.5)
    }

    /**
     * Quantos **segundos** o personagem aguenta.
     *
     * ⚠️ Arredonda **para baixo**. Meio segundo a mais numa conta de fôlego é
     * exatamente o tipo de arredondamento generoso que mata alguém na mesa — e o
     * livro não autoriza nenhum.
     */
    fun segundos(ht: Int, esforco: Esforco, preparo: Preparo): Int {
        if (ht <= 0) return 0
        val base = ht * esforco.multiplicador
        return (base * preparo.fator).toInt()
    }

    /**
     * O mesmo tempo escrito de um jeito que se lê na mesa.
     *
     * 120 segundos é "2 minutos"; 12 segundos são 12 **turnos** de combate — e é
     * a segunda leitura que importa quando alguém está sendo estrangulado.
     */
    fun tempoLegivel(segundos: Int): String = when {
        segundos <= 0 -> "nada"
        segundos < 60 -> "$segundos segundos (= $segundos turnos)"
        segundos % 60 == 0 -> "${segundos / 60} min ($segundos segundos)"
        else -> "${segundos / 60} min e ${segundos % 60} s ($segundos segundos)"
    }

    /** A linha completa, com a conta à vista. */
    fun explicacao(ht: Int, esforco: Esforco, preparo: Preparo): String {
        val s = segundos(ht, esforco, preparo)
        val conta = buildString {
            append("HT $ht × ${esforco.multiplicador}")
            if (preparo != Preparo.NORMAL) append(" × ${preparo.fator}".replace(".0", ""))
        }
        return "${tempoLegivel(s)} — $conta"
    }

    /**
     * O que acontece quando o tempo acaba (MB p.356).
     *
     * ⚠️ Não é "morreu". É um teste de HT por segundo, com penalidade
     * acumulativa — e o app **não** deve rolar isso sozinho: quem decide quando o
     * personagem tenta continuar é o jogador, turno a turno.
     */
    const val AVISO_DEPOIS_DO_LIMITE =
        "Passado esse tempo, é um teste de HT por segundo para continuar segurando, " +
            "com −1 acumulativo a cada tentativa. Ao falhar, começa a sufocar."
}
