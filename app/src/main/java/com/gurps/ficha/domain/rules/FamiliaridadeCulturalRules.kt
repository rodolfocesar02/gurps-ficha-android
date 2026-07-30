package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.BonusCondicional
import com.gurps.ficha.model.Personagem

/**
 * **Familiaridade Cultural** (MB p.24) — Lote P-CULT.
 *
 * ## ⚠️ A vantagem não dá bônus: ela APAGA uma penalidade
 *
 * O texto do livro é explícito:
 *
 * > O personagem está familiarizado com culturas diferentes da sua e **não fica
 * > sujeito à penalidade de −3**.
 *
 * Quem está fora da própria cultura leva **−3**. Isso vale para **todo mundo**,
 * tenha ou não a vantagem — e o app nunca aplicou. Declarar `+3` no traço seria
 * o oposto do livro: daria bônus a quem comprou, em vez de tirar a penalidade
 * de quem já a tinha.
 *
 * É exatamente o desenho da **Ambidestria** (ver [MaoInabilRules]): *"primeiro a
 * penalidade tem que existir na tela, depois a vantagem a apaga"*. Analogia: a
 * vantagem não é um desconto na conta — é a isenção de uma taxa que todo mundo
 * paga.
 *
 * ## Por que é caixinha, e não automático
 *
 * O app **não sabe em que cultura a cena se passa**. E a vantagem é comprada
 * *por cultura* (1 ponto por cultura da mesma raça, 2 por alienígena), enquanto
 * o catálogo tem **uma entrada só**, sem guardar quais. Então:
 *
 * - a caixinha aparece para **qualquer ficha**, porque a penalidade é de todos;
 * - quem **tem** a vantagem vê o texto mudar, avisando que o Mestre confirma se
 *   é aquela cultura.
 *
 * Mesma decisão do Míope no MIRA-2b e do Assassino Relutante no D-MIRA: quando a
 * informação não está na ficha, **o app oferece em vez de chutar**.
 *
 * Kotlin puro e testável.
 */
object FamiliaridadeCulturalRules {

    const val ID = "familiaridade_cultural"

    /** MB p.24: a penalidade que todo estrangeiro cultural paga. */
    const val PENALIDADE = -3

    /**
     * As perícias cujo rodapé cita *"Modificadores de Familiaridade Cultural"*.
     *
     * ⚠️ Não é "toda perícia social". O livro nomeia estas, e alargar por conta
     * própria seria inventar regra — Lábia e Diplomacia, por exemplo, **não**
     * estão na lista, por mais que pareçam candidatas.
     */
    val PERICIAS: Set<String> = setOf(
        "Connoisseur",
        "Dança",
        "Heráldica",
        "Jogos de Entretenimento",
        "Mímica/Pantomima",
        "Oratória",
        "Poesia",
        "Trato-Social"
    )

    /** Se a caixinha deve aparecer nesta perícia. */
    fun dependeDeCultura(nomeDaPericia: String): Boolean = nomeDaPericia in PERICIAS

    /** Se a ficha comprou a vantagem — para qualquer cultura. */
    fun temAVantagem(personagem: Personagem): Boolean =
        personagem.vantagensTotais.any { it.definicaoId == ID }

    /**
     * A caixinha desta perícia, ou null quando ela não se aplica.
     *
     * O valor é sempre [PENALIDADE], **inclusive para quem tem a vantagem**. O
     * que muda é o texto: a decisão de marcar ou não é do jogador com o Mestre,
     * e zerar o número por conta própria assumiria que a vantagem cobre *aquela*
     * cultura — que é justo o que a ficha não sabe.
     */
    fun condicionalDe(personagem: Personagem, nomeDaPericia: String): BonusCondicional? {
        if (!dependeDeCultura(nomeDaPericia)) return null
        return BonusCondicional(
            nomeDoTraco = "Cultura estrangeira",
            alvo = nomeDaPericia,
            valor = PENALIDADE,
            condicao = if (temAVantagem(personagem)) {
                "fora da sua cultura — você tem Familiaridade Cultural, confirme " +
                    "com o Mestre se ela cobre esta (MB p.24)"
            } else {
                "fora da sua cultura, sem Familiaridade Cultural dela (MB p.24)"
            }
        )
    }
}
