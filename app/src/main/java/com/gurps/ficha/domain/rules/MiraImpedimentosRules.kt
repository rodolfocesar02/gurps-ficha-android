package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * **O que atrapalha a mira** (Lote D-MIRA) — Zarolho e Assassino Relutante.
 *
 * As duas desvantagens moram no mesmo arquivo porque conversam com a **mesma
 * caixinha**, a do Apontar do MIRA-3, e em direções opostas:
 *
 * - o **Zarolho** é *cancelado* por ela — Apontar apaga o −3;
 * - o **Assassino Relutante** a *desabilita* — o livro proíbe Apontar.
 *
 * Separá-las em dois arquivos esconderia justamente o que precisa ser lido
 * junto: quem mexer numa tem de olhar a outra.
 */

/**
 * **Zarolho** (MB p.163) — um olho só.
 *
 * > Sua DX sofre uma penalidade de **−1** em situações de combate e/ou que
 * > envolvam a coordenação entre mãos e olhos, e de **−3** em situações que
 * > envolvam **ataques à distância** *(a menos que ele realize uma manobra
 * > Apontar antes)* ou a condução de qualquer veículo mais rápido que um cavalo.
 *
 * ## ⚠️ As duas penalidades NÃO se somam
 *
 * O livro descreve duas **categorias de situação**, não dois redutores
 * empilháveis: um ataque à distância é `−3`, e não `−1 −3 = −4`. O app usa a
 * mais específica e escreve isso na tela.
 *
 * ## ⚠️ E o Apontar apaga só o −3
 *
 * A ressalva entre parênteses está grudada no −3, não na frase inteira. Quem
 * Aponta continua com o −1 de combate: ele não deixou de ser zarolho, só teve
 * tempo de compensar a falta de profundidade **naquele tiro**.
 *
 * É a primeira desvantagem do app a ser **cancelada** por uma escolha do
 * jogador, e não por uma vantagem.
 */
object ZarolhoRules {

    const val ID = "zarolho"

    /** Combate corpo a corpo e qualquer coordenação mão-olho. */
    const val PENALIDADE_GERAL = -1

    /** Ataque à distância sem Apontar. */
    const val PENALIDADE_A_DISTANCIA = -3

    fun tem(personagem: Personagem): Boolean =
        personagem.desvantagensTotais.any { it.definicaoId == ID }

    /**
     * A penalidade deste ataque — já resolvida entre as duas categorias.
     *
     * Zero para quem não tem a desvantagem, que é o caminho de sempre.
     */
    fun penalidadeNoAtaque(
        personagem: Personagem,
        ehADistancia: Boolean,
        apontou: Boolean
    ): Int = when {
        !tem(personagem) -> 0
        ehADistancia && !apontou -> PENALIDADE_A_DISTANCIA
        else -> PENALIDADE_GERAL
    }

    /** A linha que explica o número na tela — nunca só o número. */
    fun rotulo(personagem: Personagem, ehADistancia: Boolean, apontou: Boolean): String {
        val p = penalidadeNoAtaque(personagem, ehADistancia, apontou)
        return when {
            p == 0 -> ""
            ehADistancia && apontou ->
                "Zarolho $p — Apontar cancelou o −3 do tiro (MB p.163)"
            ehADistancia -> "Zarolho $p à distância — Apontar cancela (MB p.163)"
            else -> "Zarolho $p em combate e coordenação mão-olho (MB p.163)"
        }
    }
}

/**
 * **Pacifismo (Assassino Relutante)** (MB p.153) — o −5 pontos.
 *
 * > Sempre que fizer um **ataque letal** (ex.: com uma faca ou arma de fogo)
 * > contra uma **pessoa que possa ver**, ele sofre uma penalidade de **−4** para
 * > acertar o alvo e **não pode Apontar**. Se ele não puder ver o rosto do
 * > inimigo (por causa de uma máscara, escuridão, distância ou porque o atacou
 * > pelas costas), a penalidade é de **−2**.
 *
 * ## ⚠️ Por que são caixinhas, e não penalidade automática
 *
 * O livro lista quatro isenções, e **nenhuma delas está na ficha**: o app não
 * sabe se o alvo é um veículo, se o personagem acredita que aquilo é uma pessoa,
 * se o rosto está coberto nem se o ataque foi pelas costas. Aplicar −4 em todo
 * ataque transformaria a desvantagem numa penalidade permanente que o livro não
 * dá — e o jogador ia acabar desligando a automação inteira.
 *
 * Mesma decisão do Míope no MIRA-2b: **o app oferece, o jogador responde**.
 *
 * ## O `porOpcao` que não deu para usar
 *
 * O catálogo guarda **um** id `pacifismo` com quatro custos (−5, −10, −15, −30),
 * um por variante. Só a de −5 tem número; as outras três são proibições de
 * conduta ("nunca matar", "não ferir inocentes") que não viram modificador. Por
 * isso a checagem é pelo **custo escolhido**, e não pelo id.
 */
object PacifismoRules {

    const val ID = "pacifismo"

    /** O custo que identifica a variante Assassino Relutante. */
    const val CUSTO_ASSASSINO_RELUTANTE = -5

    /** Contra pessoa que ele consegue ver, de rosto. */
    const val PENALIDADE_VENDO_O_ROSTO = -4

    /** Máscara, escuridão, distância ou ataque pelas costas. */
    const val PENALIDADE_SEM_VER_O_ROSTO = -2

    fun ehAssassinoRelutante(personagem: Personagem): Boolean =
        personagem.desvantagensTotais.any {
            it.definicaoId == ID && it.custoEscolhido == CUSTO_ASSASSINO_RELUTANTE
        }

    /**
     * A penalidade do ataque.
     *
     * @param ataqueLetal o jogador confirmou que é ataque letal contra alguém
     *   que ele acredita ser uma pessoa e consegue ver.
     * @param veORosto se enxerga o rosto do alvo.
     */
    fun penalidade(
        personagem: Personagem,
        ataqueLetal: Boolean,
        veORosto: Boolean
    ): Int = when {
        !ataqueLetal || !ehAssassinoRelutante(personagem) -> 0
        veORosto -> PENALIDADE_VENDO_O_ROSTO
        else -> PENALIDADE_SEM_VER_O_ROSTO
    }

    /**
     * Se a caixinha do Apontar deve ficar **desabilitada**.
     *
     * O livro não dá penalidade por Apontar: ele diz que o personagem *não
     * pode*. Deixar a caixinha marcável e depois ignorar o bônus seria pior que
     * escondê-la — o jogador veria o número mudar e não mudar.
     */
    fun bloqueiaApontar(personagem: Personagem, ataqueLetal: Boolean): Boolean =
        ataqueLetal && ehAssassinoRelutante(personagem)

    fun rotulo(personagem: Personagem, ataqueLetal: Boolean, veORosto: Boolean): String {
        val p = penalidade(personagem, ataqueLetal, veORosto)
        if (p == 0) return "Ataque letal contra uma pessoa que eu consigo ver"
        return if (veORosto) {
            "Assassino Relutante $p — vendo o rosto, e sem poder Apontar (MB p.153)"
        } else {
            "Assassino Relutante $p — sem ver o rosto (MB p.153)"
        }
    }

    const val ROTULO_VE_O_ROSTO = "Consigo ver o rosto do alvo"

    const val ROTULO_ACESSIVEL_LETAL =
        "Marcar que este é um ataque letal contra uma pessoa que o personagem " +
            "consegue ver. Aplica a penalidade do Assassino Relutante e desabilita o Apontar."

    const val ROTULO_ACESSIVEL_ROSTO =
        "Marcar que o personagem enxerga o rosto do alvo. Sem o rosto a " +
            "penalidade cai de menos 4 para menos 2."
}
