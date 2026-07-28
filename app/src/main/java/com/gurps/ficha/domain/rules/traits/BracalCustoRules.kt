package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.ModificadorSelecao

/**
 * Custo de **ST Braçal** (MB p.89) e **DX Braçal** (MB p.56).
 *
 * O app tratava as duas como custo de ESCOLHA: três valores fixos (3, 5 ou 8
 * pontos) e pronto. Está errado — no livro esses números são o preço de **cada
 * +1**, e o que muda entre eles é **quantos braços** recebem o aumento:
 *
 * > "A ST Braçal custa 3 pontos para cada +1 de ST para um braço, 5 pontos para
 * > cada +1 em dois braços e 8 pontos para cada +1 nos três braços."
 *
 * Ou seja: ST Braçal +4 nos dois braços custa 5 × 4 = **20 pontos**, e não 5.
 * A ficha vinha cobrando um nível e entregando quantos o jogador quisesse.
 *
 * Por que estas duas viraram classe Kotlin em vez de `efeitos` no JSON: o
 * formato declarativo cobre *"+N em X"*, e aqui o preço depende de **duas**
 * escolhas do jogador (braços × níveis). Isso é regra, não dado — o critério
 * do [EfeitoInterpretador].
 *
 * O número de braços vive em `metadados["bracos"]`.
 */
abstract class BracalRuleBase : TraitRule {

    /** Custo de cada +1, por quantidade de braços beneficiados. */
    protected abstract val tabelaPorBracos: Map<Int, Int>

    /** Quantidade de braços padrão quando a ficha é antiga e não tem o dado. */
    protected open val bracosPadrao: Int = 1

    override fun calculateCost(
        selection: TracoSelecionado,
        modifiers: List<ModificadorSelecao>
    ): Int {
        val bracos = bracosDe(selection)
        val niveis = selection.nivel.coerceAtLeast(1)
        val base = custoPorNivel(bracos) * niveis
        return com.gurps.ficha.domain.rules.CharacterRules.aplicarModificadoresPercentuais(base, modifiers)
    }

    /** Braços beneficiados, lidos dos metadados e presos à tabela do livro. */
    fun bracosDe(selection: TracoSelecionado): Int {
        val cru = selection.metadados?.get(CHAVE_BRACOS)?.toIntOrNull() ?: bracosPadrao
        return if (cru in tabelaPorBracos.keys) cru else bracosPadrao
    }

    /** Preço de cada +1 para [bracos] braços. */
    fun custoPorNivel(bracos: Int): Int =
        tabelaPorBracos[bracos] ?: tabelaPorBracos.getValue(bracosPadrao)

    /** Opções que o diálogo mostra, da menor para a maior. */
    fun opcoesDeBracos(): List<Int> = tabelaPorBracos.keys.sorted()

    companion object {
        const val CHAVE_BRACOS = "bracos"
    }
}

/**
 * ST Braçal — 3, 5 ou 8 pontos **por +1 de ST**, para um, dois ou três braços.
 *
 * O livro avisa que a partir de quatro braços não compensa: sai mais barato
 * comprar ST geral. Por isso a tabela para em três.
 */
class StBracalRule : BracalRuleBase() {
    override val traitId: String = ID
    override val tabelaPorBracos: Map<Int, Int> = mapOf(1 to 3, 2 to 5, 3 to 8)

    companion object {
        const val ID = "st_bracal"
    }
}

/**
 * DX Braçal — 12 ou 16 pontos **por +1 de DX**, para um ou dois braços.
 *
 * Mesmo defeito, mesma correção. O livro para em dois braços aqui.
 */
class DxBracalRule : BracalRuleBase() {
    override val traitId: String = ID
    override val tabelaPorBracos: Map<Int, Int> = mapOf(1 to 12, 2 to 16)

    companion object {
        const val ID = "dx_bracal"
    }
}
