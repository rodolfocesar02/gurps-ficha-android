package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.BonusCondicional
import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
import com.gurps.ficha.model.Personagem

/**
 * Testes de Reação (GURPS MB p.494).
 *
 * O Mestre rola 3d6 e consulta a tabela: 3-6 é péssima, 7-9 ruim, 10-12 neutra,
 * 13-15 boa, 16+ excelente. Vantagens e desvantagens sociais deslocam o
 * resultado — Aparência, Carisma, Voz Melodiosa somam; Aparência ruim, Odioso
 * e Reputação negativa subtraem.
 *
 * O app nunca teve isso: o jogador rolava e consultava a tabela do livro na
 * mão, e os modificadores dos traços ficavam por conta da memória dele.
 *
 * Kotlin puro, testável — mesmo desenho de `SentidoRules` e `AutocontroleRules`.
 */
object ReacaoRules {

    /** Faixa do resultado, conforme a tabela do MB p.494. */
    enum class Faixa(val rotulo: String) {
        PESSIMA("Péssima"),
        RUIM("Ruim"),
        NEUTRA("Neutra"),
        BOA("Boa"),
        EXCELENTE("Excelente")
    }

    /**
     * Contexto do teste. O mesmo personagem causa reações diferentes conforme
     * quem reage: Aparência conta pouco para animais, Carisma não conta para
     * máquinas.
     */
    enum class Contexto(val rotulo: String) {
        GERAL("Pessoas em geral"),
        ANIMAIS("Animais"),
        PLANTAS("Plantas"),
        MAQUINAS("Máquinas e IAs")
    }

    /** Um traço que desloca a reação, com o motivo — a "notinha". */
    data class ModificadorDeReacao(val nomeDoTraco: String, val valor: Int)

    /**
     * Modificadores de reação vindos dos traços da ficha.
     *
     * Lê os efeitos declarados de tipo `defesa` com alvo `reacao` — reusa o
     * mesmo campo `efeitos` do catálogo, sem inventar um formato novo.
     */
    fun modificadoresDe(personagem: Personagem): List<ModificadorDeReacao> =
        TraitRuleRegistry.getSkillBonusOrigens(personagem, ALVO_REACAO)
            .map { ModificadorDeReacao(it.nomeDoTraco, it.valor) }

    /** Soma dos modificadores da ficha. */
    fun totalDe(personagem: Personagem): Int = modificadoresDe(personagem).sumOf { it.valor }

    /**
     * Modificadores de reação que dependem de uma CONDIÇÃO.
     *
     * *Voz Melodiosa* dá +2 só "de quem pode ouvir sua voz" — somar sempre daria
     * bônus contra surdos e contra máquinas. Quem sabe se a condição vale é o
     * jogador, no momento do teste, então isso vira caixinha marcável na tela,
     * igual ao bônus condicional de perícia (Lote V-5).
     */
    fun condicionaisDe(personagem: Personagem): List<BonusCondicional> =
        TraitRuleRegistry.getBonusCondicionais(personagem, ALVO_REACAO)

    /**
     * Se há QUALQUER coisa de reação na ficha — fixa ou condicional.
     *
     * O painel usa isto para decidir se aparece. Olhar só [modificadoresDe]
     * esconderia a tela de quem só tem traço condicional: era o caso da ficha de
     * teste com Voz Melodiosa, em que o +2 só surgia no resultado da rolagem.
     */
    fun temAlgumModificador(personagem: Personagem): Boolean =
        modificadoresDe(personagem).isNotEmpty() || condicionaisDe(personagem).isNotEmpty()

    /**
     * Em que faixa cai um resultado.
     *
     * O modificador entra ANTES da consulta: um +2 pode transformar reação
     * neutra em boa, que é justamente o efeito das vantagens sociais.
     */
    fun faixaDe(soma: Int, modificador: Int = 0): Faixa {
        val efetivo = soma + modificador
        return when {
            efetivo <= 6 -> Faixa.PESSIMA
            efetivo <= 9 -> Faixa.RUIM
            efetivo <= 12 -> Faixa.NEUTRA
            efetivo <= 15 -> Faixa.BOA
            else -> Faixa.EXCELENTE
        }
    }

    /** O que cada faixa significa na mesa, em uma linha. */
    fun descricaoDa(faixa: Faixa): String = when (faixa) {
        Faixa.PESSIMA -> "Hostil. Ataca, denuncia ou atrapalha ativamente."
        Faixa.RUIM -> "Desconfiado. Recusa ajuda, mas não age contra."
        Faixa.NEUTRA -> "Indiferente. Trata como um estranho qualquer."
        Faixa.BOA -> "Simpático. Ajuda dentro do razoável."
        Faixa.EXCELENTE -> "Muito favorável. Faz o possível para ajudar."
    }

    /** Alvo reservado no campo `efeitos` para modificador de reação. */
    const val ALVO_REACAO = "reacao"
}
