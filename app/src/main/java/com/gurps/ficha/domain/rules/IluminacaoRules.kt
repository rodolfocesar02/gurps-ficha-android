package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.BonusCondicional
import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
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

    /**
     * **Cegueira** (MB p.127) — quem já não enxerga não piora no escuro.
     *
     * > Seja qual for o caso, o personagem **não sofre nenhuma outra penalidade
     * > por atuar no escuro**.
     *
     * 🔴 O Lote LUZ-1 aplicava a escuridão a todo mundo. O cego **já pagou** essa
     * conta nos −6 das perícias de combate; somar a luz por cima é cobrar duas
     * vezes pela mesma cegueira.
     */
    const val ID_CEGUEIRA = "cegueira"

    /**
     * **Cegueira Noturna** (MB p.127) — o espelho da Visão Noturna.
     *
     * > Se a penalidade (…) estiver entre **-1 e -4**, então a dele será o **dobro
     * > ou -3, o que for pior**. Se a penalidade for de **-5 ou pior**, então ele
     * > deve agir como se fosse **completamente cego**.
     *
     * ⚠️ Duas armadilhas na mesma frase:
     * - Com luz **-1**, o dobro é -2, mas **-3 é pior** → vale **-3**. Só de -2 em
     *   diante o dobro passa a mandar.
     * - De **-5** em diante o salto é para **-10** (cego), não para o dobro. É
     *   abrupto de propósito.
     */
    const val ID_CEGUEIRA_NOTURNA = "cegueira_noturna"

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

    /** Se a ficha tem Cegueira — a desvantagem, não a vantagem. */
    fun ehCego(personagem: Personagem): Boolean =
        personagem.desvantagensTotais.any { it.definicaoId == ID_CEGUEIRA }

    /** Se a ficha tem Cegueira Noturna (MB p.127). */
    fun temCegueiraNoturna(personagem: Personagem): Boolean =
        personagem.desvantagensTotais.any { it.definicaoId == ID_CEGUEIRA_NOTURNA }

    /**
     * A luz da cena vista por quem tem **Cegueira Noturna**.
     *
     * Devolve a penalidade BRUTA já agravada, para o resto da conta seguir igual.
     */
    internal fun agravadaPorCegueiraNoturna(bruta: Int): Int = when {
        bruta >= 0 -> 0
        bruta <= -5 -> ESCURIDAO_TOTAL
        // "o dobro ou -3, o que for pior" -- pior = mais negativo.
        else -> minOf(bruta * 2, -3)
    }

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
        val escolhida = bruta.coerceIn(ESCURIDAO_TOTAL, 0)

        // 🔴 Cego não sofre NADA por escuro: ele já pagou essa conta (MB p.127).
        // Somar a luz por cima seria cobrar duas vezes pela mesma cegueira.
        if (ehCego(personagem)) {
            return Resultado(
                escolhida, -escolhida, 0,
                "Cego — a escuridão não muda nada para ele (MB p.127)."
            )
        }

        // Cegueira Noturna agrava a luz ANTES de tudo: a partir daí a conta é a
        // mesma de todo mundo.
        val luz = if (temCegueiraNoturna(personagem)) {
            agravadaPorCegueiraNoturna(escolhida)
        } else {
            escolhida
        }
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

    /**
     * A escuridão como **caixinha** para uma rolagem de perícia (Lote LUZ-2).
     *
     * ## Por que caixinha, e não desconto automático
     *
     * Achado pelo usuário no T-L5: *"só dá pra saber do redutor na hora que joga
     * ataque ou defesa, confira se teste de perícias devem entrar esse redutor"*.
     *
     * Devem — mas **não em todas**. O livro amarra a escuridão à **visão**: a Visão
     * Noturna cancela penalidade *"em testes que envolvam a visão ou no combate"*
     * (MB p.97). Escalar no escuro é mais difícil; lembrar de uma data de
     * História, não.
     *
     * ⚠️ Descontar em **toda** perícia penalizaria Contabilidade, Teologia e
     * Meditação por causa da luz da sala — número errado em silêncio. Aplicar em
     * **nenhuma** deixa o jogador subtraindo de cabeça. Quem sabe se aquele teste
     * depende de ver é o Mestre, e a caixinha é justamente para isso.
     *
     * Devolve null quando não há escuridão sobrando — nada a oferecer.
     */
    fun condicionalDaLuz(personagem: Personagem, bruta: Int): BonusCondicional? {
        val r = penalidadeEfetiva(personagem, bruta)
        if (r.efetiva == 0) return null
        return BonusCondicional(
            nomeDoTraco = rotuloDaLuz(r.bruta),
            alvo = TraitRuleRegistry.CURINGA_PERICIA,
            valor = r.efetiva,
            condicao = "se este teste depender de ver (MB p.97)"
        )
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
