package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * **A luz da cena** e as vantagens que enxergam no escuro (MB p.395 e p.549).
 *
 * ## Por que isto existe
 *
 * A Visão Noturna custa 1 ponto por nível e cancela penalidade de escuridão. Só
 * que o app **não tinha escuridão nenhuma** — então a vantagem era decorativa: o
 * jogador tinha de lembrar do nível dele e subtrair de cabeça a cada rolagem.
 *
 * Resolver a vantagem exigia resolver a cena. Então o seletor vale para todas as
 * rolagens de **visão e combate**, e a Visão Noturna come parte dele sozinha.
 *
 * ## O que o livro dá, e o que ele NÃO dá
 *
 * O livro **não** tem uma tabela de degraus nomeados de escuridão. Ele dá:
 *
 * - *"Escuridão parcial, nevoeiro, fumaça, etc.: **-1 a -9** (a critério do
 *   Mestre)"* (p.549). É faixa aberta, e quem escolhe é o Mestre.
 * - **-10** para escuridão total ou cegueira; **-6** se o personagem for
 *   acostumado à cegueira (p.395).
 * - *"qualquer luz desta espécie dentro da linha de visão reduz a penalidade de
 *   -10 (por escuridão total) para **-3**"* — tocha ou lanterna (p.395).
 *
 * ⚠️ Por isso o seletor é uma **faixa de 0 a -10**, e não quatro botões com nomes
 * inventados. Batizar degraus que o livro não batiza seria pôr regra na boca do
 * livro. Os rótulos existem só como referência do que aquele número costuma
 * significar.
 *
 * ## A regra que separa as duas vantagens
 *
 * | | Escuridão parcial (-1 a -9) | Escuridão total (-10) |
 * |---|---|---|
 * | **Visão Noturna** | cancela até o nível dela | **não vale nada** |
 * | **Visão no Escuro** | cancela tudo | cancela tudo |
 *
 * O livro é explícito na Visão Noturna: *"não surte nenhum efeito sobre a
 * penalidade de -10 de uma escuridão total"* e *"funciona apenas em situações de
 * escuridão parcial"*. Errar isso daria ao jogador uma vantagem de 9 pontos que
 * faz o trabalho de uma de 25.
 *
 * Kotlin puro e testável.
 */
object IluminacaoRules {

    const val ID_VISAO_NOTURNA = "visao_noturna"
    const val ID_VISAO_NO_ESCURO = "visao_no_escuro"
    const val ID_ULTRAVISAO = "ultravisao"

    /** A penalidade de escuridão total (MB p.395). */
    const val ESCURIDAO_TOTAL = -10

    /** Nível máximo de Visão Noturna que o livro permite. */
    const val MAX_VISAO_NOTURNA = 9

    /**
     * Quanto a Ultravisão ignora de escuridão à noite (MB p.96).
     *
     * *"Durante a noite, uma pequena quantidade de raios UV atinge o solo (…).
     * Isso não permite que o personagem enxergue no escuro, mas o permite ignorar
     * até -2 em penalidades por escuridão (cumulativo com Visão Noturna)."*
     */
    const val UV_IGNORA = 2

    /** O que aquele número costuma significar — referência, não regra. */
    fun rotuloDaLuz(penalidadeBruta: Int): String = when (penalidadeBruta) {
        0 -> "Boa luz"
        -1, -2 -> "Penumbra"
        -3 -> "Escuro (ou tocha na escuridão)"
        -4, -5, -6 -> "Muito escuro"
        -7, -8, -9 -> "Quase nada de luz"
        else -> "Escuridão total"
    }

    /** Níveis de Visão Noturna na ficha, limitados ao teto do livro. */
    fun nivelVisaoNoturna(personagem: Personagem): Int =
        personagem.vantagensTotais
            .filter { it.definicaoId == ID_VISAO_NOTURNA }
            .sumOf { it.nivel.coerceAtLeast(1) }
            .coerceAtMost(MAX_VISAO_NOTURNA)

    /** Se a ficha tem Visão no Escuro (MB p.97). */
    fun temVisaoNoEscuro(personagem: Personagem): Boolean =
        personagem.vantagensTotais.any { it.definicaoId == ID_VISAO_NO_ESCURO }

    /** Se a ficha tem Ultravisão (MB p.96). */
    fun temUltravisao(personagem: Personagem): Boolean =
        personagem.vantagensTotais.any { it.definicaoId == ID_ULTRAVISAO }

    /** O resultado, pronto para a tela: o número final e de onde ele veio. */
    data class Resultado(
        val bruta: Int,
        val cancelado: Int,
        val efetiva: Int,
        val explicacao: String
    )

    /**
     * A penalidade que sobra depois das vantagens do personagem.
     *
     * [bruta] é a luz da cena, de 0 a -10, escolhida pelo jogador.
     */
    fun penalidadeEfetiva(personagem: Personagem, bruta: Int): Resultado {
        val luz = bruta.coerceIn(ESCURIDAO_TOTAL, 0)
        if (luz == 0) return Resultado(0, 0, 0, "Boa luz — sem penalidade.")

        // Visão no Escuro ignora tudo, inclusive a escuridão total.
        if (temVisaoNoEscuro(personagem)) {
            return Resultado(
                luz, -luz, 0,
                "${rotuloDaLuz(luz)} $luz, Visão no Escuro cancela tudo → 0"
            )
        }

        // ⚠️ Na escuridão TOTAL a Visão Noturna e a Ultravisão não valem nada.
        if (luz == ESCURIDAO_TOTAL) {
            return Resultado(
                luz, 0, luz,
                "Escuridão total $luz — Visão Noturna não vale aqui (MB p.97)."
            )
        }

        val noturna = nivelVisaoNoturna(personagem)
        val uv = if (temUltravisao(personagem)) UV_IGNORA else 0
        // Cancela no máximo o tamanho da penalidade: sobra não vira bônus.
        val cancelado = (noturna + uv).coerceAtMost(-luz)
        val efetiva = luz + cancelado

        val partes = buildList {
            add("${rotuloDaLuz(luz)} $luz")
            if (noturna > 0) add("Visão Noturna $noturna")
            if (uv > 0) add("Ultravisão $uv")
        }
        val texto = if (cancelado == 0) {
            "${partes.first()} — nenhuma vantagem ajuda aqui."
        } else {
            partes.joinToString(" + ") + " → ${if (efetiva == 0) "0" else "$efetiva"}"
        }
        return Resultado(luz, cancelado, efetiva, texto)
    }

    /** Se vale mostrar a linha das vantagens — sem nenhuma delas, não vale. */
    fun temAlgumaVantagem(personagem: Personagem): Boolean =
        nivelVisaoNoturna(personagem) > 0 ||
            temVisaoNoEscuro(personagem) ||
            temUltravisao(personagem)

    /** O mesmo, para o TalkBack: sem sinal gráfico, com "menos" escrito. */
    fun descricaoAcessivel(personagem: Personagem, bruta: Int): String {
        val r = penalidadeEfetiva(personagem, bruta)
        val comoLer = if (r.efetiva < 0) "menos ${-r.efetiva}" else "zero"
        return "Luz da cena: ${rotuloDaLuz(r.bruta).lowercase()}. " +
            "Modificador de $comoLer nas rolagens de visão e combate." +
            if (r.cancelado > 0) " Suas vantagens cancelaram ${r.cancelado}." else ""
    }
}
