package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry.OrigemDeBonus
import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.viewmodel.DefenseType

/**
 * De onde vem cada pedaço dos números de **defesa** e de **item** (Lote NOTA-2).
 *
 * O NOTA-1 já explicava o bônus das perícias. Faltavam os outros números que o
 * app calcula sozinho: a Esquiva que subiu por causa de um escudo, o Bloqueio
 * que ganhou +1 de Reflexos em Combate, a Apara com um ajuste digitado à mão.
 *
 * O problema é sempre o mesmo: o app mostra **8** e o jogador não tem como
 * conferir se 8 está certo. Automação que não se explica é caixa preta — e num
 * jogo de regras, quem não confere não confia.
 *
 * Analogia: é a diferença entre o extrato que mostra só o saldo e o que mostra
 * as linhas. O saldo é o que interessa, mas sem as linhas você não descobre o
 * lançamento errado.
 *
 * Kotlin puro e testável. A montagem do TEXTO fica na camada de UI
 * (`OrigemDoBonus.kt`), que já é compartilhada por Perícias e Rolagem.
 */
object OrigemDosNumeros {

    /**
     * Componentes que o app somou à defesa **além da base**.
     *
     * Fora da lista de propósito: a base (Velocidade Básica + 3, ou metade do
     * NH) e a penalidade de carga. Aquilo é a regra do GURPS, não bônus —
     * listar tudo transformaria a notinha num parágrafo e esconderia o que
     * realmente veio da ficha do personagem.
     */
    fun daDefesa(personagem: Personagem, tipo: DefenseType): List<OrigemDeBonus> {
        val d = personagem.defesasAtivas
        val origens = mutableListOf<OrigemDeBonus>()

        // 1. Escudo — o único "item" que entra numa defesa (MB p.375). Só conta
        //    quando o jogador declarou que está usando, na defesa de Bloqueio.
        val bonusEscudo = d.getBonusEscudo(personagem)
        if (bonusEscudo != 0) {
            val nome = d.escudoSelecionadoNome?.trim().orEmpty().ifBlank { "Escudo" }
            origens += OrigemDeBonus(nome, bonusEscudo)
        }

        // 2. Traços automatizados (Reflexos em Combate, Esquiva Ampliada...),
        //    um por um. A soma pronta existe no Registry, mas ela não diz QUAIS
        //    — e é exatamente isso que a notinha precisa dizer.
        origens += origensDeTracos(personagem, tipo)

        // 3. O que o jogador digitou à mão, com a nota dele quando existe.
        //    Sem a nota o número é um mistério até para quem digitou, meses
        //    depois — foi por isso que o Lote M-1 criou o campo.
        val manual = when (tipo) {
            DefenseType.ESQUIVA -> d.bonusManualEsquiva to d.notaBonusManualEsquiva
            DefenseType.APARA -> d.bonusManualApara to d.notaBonusManualApara
            DefenseType.BLOQUEIO -> d.bonusManualBloqueio to d.notaBonusManualBloqueio
        }
        if (manual.first != 0) {
            origens += OrigemDeBonus(manual.second.trim().ifBlank { "Manual" }, manual.first)
        }

        return origens
    }

    /**
     * Quebra o bônus de traço por traço, para a notinha poder nomeá-los.
     *
     * Sem isto o jogador leria "Vantagens +2" e continuaria sem saber quais —
     * que é metade do problema que o NOTA-2 existe para resolver.
     */
    private fun origensDeTracos(
        personagem: Personagem,
        tipo: DefenseType
    ): List<OrigemDeBonus> {
        val periciaApara = personagem.defesasAtivas.getPericiaApara(personagem)?.definicaoId
        return (personagem.vantagens + personagem.desvantagens).mapNotNull { selecao ->
            val regra = TraitRuleRegistry.getRuleFor(selecao.definicaoId) ?: return@mapNotNull null
            val valor = when (tipo) {
                DefenseType.ESQUIVA -> regra.getDodgeModifier(personagem, selecao)
                DefenseType.BLOQUEIO -> regra.getBlockModifier(personagem, selecao)
                DefenseType.APARA -> regra.getParryModifier(personagem, selecao, periciaApara)
            }
            if (valor != 0) OrigemDeBonus(selecao.nome, valor) else null
        }
    }
}
