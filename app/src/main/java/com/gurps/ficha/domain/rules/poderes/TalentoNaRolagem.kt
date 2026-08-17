package com.gurps.ficha.domain.rules.poderes

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.Poder
import com.gurps.ficha.ui.features.rolagem.RollMappedOption

/**
 * **O Talento chega na rolagem** — GURPS Poderes, p.158. Lote POD-29.
 *
 * ## 🔴 O que faltava
 *
 * A regra existia **inteira** desde o POD-11, em [UsoDoPoder.TipoDeTeste]: o
 * Talento soma em *ativar, atacar, controlar e defender*, e **não** soma em
 * dano, reação, teste exigido por limitação nem teste feito pelo alvo.
 *
 * E o app sabia o nível do Talento desde o POD-3.
 *
 * Só que os dois nunca se encontraram. Quem comprava Telepatia com Talento 3 e
 * um Ataque Inato dentro dela via o ataque na aba Rolagem com o NH **sem os
 * +3**. É o formato de defeito que mais apareceu neste projeto: **a regra
 * existe, a tela não pergunta**.
 *
 * ## Por que aqui, e não dentro de cada regra de ataque
 *
 * Três regras produzem ataque hoje (Ataque Inato, Dentes, Golpeadores) e o
 * catálogo vai ganhar mais. Somar o Talento dentro de cada uma seria criar três
 * cópias da mesma decisão — e **o defeito mora na diferença**, que é como este
 * projeto já perdeu quatro rotas de modificador e duas RDs.
 *
 * Aqui é um lugar só, puro, e testável sem subir tela.
 *
 * ## ⚠️ O que este arquivo NÃO faz
 *
 * Não toca em **dano**. O livro exclui o dano por escrito, e a exclusão já está
 * escrita em [UsoDoPoder.TipoDeTeste.DANO]. Quem chamar isto para uma lista de
 * dano está usando errado — por isso a função só aceita [RollMappedOption], que
 * é o tipo do teste, e não `DamageSourceOption`.
 */
object TalentoNaRolagem {

    /**
     * O poder a que este traço pertence, se pertencer a algum.
     *
     * ⚠️ `poderId` vazio conta como solto: ficha antiga pode ter string vazia
     * em vez de `null`, e um traço solto não pode pegar carona no Talento de
     * ninguém.
     */
    fun poderDoTraco(personagem: Personagem, poderId: String?): Poder? {
        val id = poderId?.takeIf { it.isNotBlank() } ?: return null
        return personagem.poderes.firstOrNull { it.id == id }
    }

    /**
     * O bônus que este traço recebe por pertencer a um poder com Talento.
     *
     * Zero quando o traço é solto, quando o poder não tem Talento, ou quando o
     * poder é um dos que **não têm Talento nenhum** — o Antipsi do Módulo
     * Básico (MB p.256), cujo `nivelTalento` fica em 0 desde o POD-15.
     */
    fun bonusDoTalento(personagem: Personagem, poderId: String?): Int {
        val poder = poderDoTraco(personagem, poderId) ?: return 0
        return UsoDoPoder.bonusDoTalento(
            nivelDoTalento = poder.nivelTalento,
            tipo = UsoDoPoder.TipoDeTeste.ATACAR
        )
    }

    /**
     * As opções de rolagem do traço, já com o Talento somado e com o poder de
     * origem no rótulo.
     *
     * ⚠️ O nome do poder no rótulo **não é enfeite**: pela p.156 uma falha
     * crítica derruba o **poder inteiro**, todas as habilidades dele de uma vez.
     * Sem o rótulo, o jogador não tem como saber o que cai junto.
     */
    fun aplicar(
        personagem: Personagem,
        poderId: String?,
        opcoes: List<RollMappedOption>
    ): List<RollMappedOption> {
        val poder = poderDoTraco(personagem, poderId) ?: return opcoes
        val bonus = bonusDoTalento(personagem, poderId)
        return opcoes.map { o ->
            o.copy(
                label = "${o.label} (${poder.nome})",
                contextLabel = "${o.contextLabel} — poder ${poder.nome}",
                target = o.target?.plus(bonus),
                descricao = descricaoDe(o.descricao, poder, bonus)
            )
        }
    }

    /** A explicação que vai junto: de onde veio o bônus e o que derruba tudo. */
    fun descricaoDe(original: String, poder: Poder, bonus: Int): String = buildString {
        if (original.isNotBlank()) append(original).append(" ")
        append("Habilidade do poder ${poder.nome}.")
        if (bonus > 0) {
            append(" Talento ${poder.nivelTalento} já somado no NH (p.158) — ")
            append("ele vale para atacar, mas não para o dano.")
        }
        append(" ")
        append(UsoDoPoder.Incapacitacao.explicar(poder.fonte))
    }
}
