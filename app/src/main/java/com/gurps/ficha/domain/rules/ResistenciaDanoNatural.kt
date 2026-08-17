package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * **Resistência a Dano do próprio corpo** — MB p.83. Lote POD-30.
 *
 * ## 🔴 O que faltava
 *
 * A vantagem estava no catálogo desde sempre (`resistencia_a_dano`, 5 pontos por
 * nível) e **não fazia nada**. Quem comprava RD 5 pagava 25 pontos e continuava
 * levando o dano inteiro no diálogo de ferimento, que só somava a armadura
 * vestida.
 *
 * O livro é direto sobre onde ela entra:
 *
 * > *"Subtraia esse valor do dano causado por qualquer ataque físico ou de
 * > energia **depois de aplicar a RD de armaduras artificiais**."*
 *
 * Ou seja: ela **soma** com a armadura, não substitui.
 *
 * ## Por que ela chegou pelo pilar Poderes
 *
 * É a habilidade que o livro de *Poderes* mais pede entre as que têm efeito
 * mecânico direto: **23 vezes** nos 567 exemplos dos 47 poderes. Mas a regra é
 * geral — vale igual para quem compra RD sem poder nenhum, e é por isso que
 * este arquivo mora em `domain/rules/` e não em `domain/rules/poderes/`.
 *
 * ## ⚠️ O que este arquivo NÃO decide
 *
 * Se a RD **vale contra aquele ataque**. Uma RD com *Limitada (só contra fogo)*
 * ou *Parcial* não protege de tudo, e o app não tem como saber o que está
 * chegando. Por isso os modificadores viram **ressalva na tela**: o número
 * aparece somado, e a ressalva ao lado, para o jogador decidir. Travar seria
 * repetir o erro do `conhecimento_oculto`; somar em silêncio seria pior ainda.
 */
object ResistenciaDanoNatural {

    /** O id da vantagem no `vantagens.v3.json`. */
    const val ID = "resistencia_a_dano"

    /**
     * De onde veio cada pedaço da RD natural.
     *
     * ⚠️ Uma lista, e não um número só: o personagem pode ter RD comprada com
     * pontos **e** RD da raça, e quem lê a ficha precisa saber qual é qual —
     * ainda mais quando uma delas tem ressalva e a outra não.
     */
    data class Fonte(
        val nome: String,
        val rd: Int,
        /** Os modificadores que podem restringir esta RD. Vazio quando não há. */
        val ressalva: String
    )

    /**
     * As RDs naturais do personagem, incluindo as que vêm da raça e as que são
     * habilidade de um poder.
     *
     * ⚠️ `vantagensTotais` de propósito: uma raça com couraça tem de contar
     * igual a quem comprou a vantagem com pontos. Foi o mesmo critério do
     * agregador de ataques.
     */
    fun fontes(personagem: Personagem): List<Fonte> =
        personagem.vantagensTotais
            .filter { it.definicaoId == ID }
            .mapNotNull { v ->
                val rd = v.nivel.coerceAtLeast(0)
                if (rd == 0) null else Fonte(
                    nome = v.nome,
                    rd = rd,
                    ressalva = v.modificadores.joinToString(", ") { it.nome }
                )
            }

    /** Quanto a RD natural desconta, somando todas as fontes. */
    fun total(personagem: Personagem): Int = fontes(personagem).sumOf { it.rd }

    /**
     * A linha que explica a soma, quando há RD natural.
     *
     * Null quando não há nenhuma — nada de linha vazia dizendo "RD natural 0".
     */
    fun explicar(personagem: Personagem): String? {
        val fontes = fontes(personagem)
        if (fontes.isEmpty()) return null
        val detalhe = fontes.joinToString(" · ") { f ->
            f.nome + " " + f.rd + (if (f.ressalva.isBlank()) "" else " (${f.ressalva})")
        }
        return buildString {
            append("RD natural ${total(personagem)}, somada à da armadura (p.83): ")
            append(detalhe)
            if (fontes.any { it.ressalva.isNotBlank() }) {
                append(". Confira se os modificadores valem contra este ataque.")
            }
        }
    }
}
