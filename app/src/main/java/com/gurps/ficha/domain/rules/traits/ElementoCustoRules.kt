package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.ModificadorSelecao

/**
 * Custo de **Controle** (Poderes, p.90) e **Criar** (Poderes, p.92). Lote POD-21.
 *
 * ## 🔴 O defeito, achado pelo usuário na tela
 *
 * *"a vantagem é por nível, porém está como variável, tenho que subir ponto a
 * ponto e não 20/15/10!"*
 *
 * As duas entraram no catálogo como `costKind: "special"`, que o app traduz
 * para custo **variável** — o diálogo do `−1 / +1` de um ponto por vez. Mas no
 * livro elas não são variáveis: são **duas escolhas encadeadas**, exatamente
 * como a ST Braçal:
 *
 * 1. **qual é o elemento** — e isso fixa o preço de cada nível;
 * 2. **quantos níveis** — e o preço se multiplica.
 *
 * Analogia: é a mesma diferença de [BracalRuleBase]. O app perguntava só
 * *"quanto você quer gastar?"*, quando o livro pergunta *"o quê, e quanto
 * disso?"*.
 *
 * ## Por que é classe Kotlin, e não `efeitos` no JSON
 *
 * Mesmo critério do Braçal: o formato declarativo do catálogo dá conta de
 * *"+N em X"*, e aqui o preço depende de **duas** escolhas do jogador. Isso é
 * regra, não dado.
 *
 * A categoria escolhida vive em `metadados["categoria"]`.
 */
abstract class ElementoRuleBase : TraitRule {

    /** Uma faixa do livro: o nome, o preço de cada nível e exemplos. */
    data class Faixa(
        val nome: String,
        val custoPorNivel: Int,
        val exemplos: String
    )

    /** As faixas do verbete, da mais cara para a mais barata. */
    abstract val faixas: List<Faixa>

    /** A página do verbete em GURPS Poderes. */
    abstract val pagina: Int

    private val padrao: Faixa get() = faixas.last()

    override fun calculateCost(
        selection: TracoSelecionado,
        modifiers: List<ModificadorSelecao>
    ): Int {
        val faixa = faixaDe(selection)
        val niveis = selection.nivel.coerceAtLeast(1)
        val base = faixa.custoPorNivel * niveis
        return com.gurps.ficha.domain.rules.CharacterRules
            .aplicarModificadoresPercentuais(base, modifiers)
    }

    /**
     * A faixa escolhida, lida dos metadados.
     *
     * ⚠️ Ficha antiga não tem o dado, e nome que não existe mais também não
     * pode derrubar a ficha: nos dois casos vale a faixa **mais barata**. Errar
     * para baixo é o lado seguro — um custo inflado sem o jogador pedir seria
     * pior do que um custo modesto que ele pode corrigir.
     */
    fun faixaDe(selection: TracoSelecionado): Faixa {
        val escolhida = selection.metadados?.get(CHAVE_CATEGORIA)
        return faixas.firstOrNull { it.nome == escolhida } ?: padrao
    }

    companion object {
        const val CHAVE_CATEGORIA = "categoria"
    }
}

/**
 * **Controle** — 20, 15 ou 10 pontos **por nível**, pela raridade do elemento.
 *
 * > *"O custo por nível depende do quão significativo o elemento poderá ser em
 * > uma aventura."* (p.90)
 *
 * ⚠️ O livro deixa o Mestre permitir categorias *"Muito Comuns"* por 25 ou 30
 * pontos/nível, e elementos abstratos como Espaço e Tempo por pelo menos 30.
 * Essas **não** entram na lista: são autorização do Mestre caso a caso, não
 * faixa fechada. Quem precisar delas usa o campo de custo à mão.
 */
class ControleRule : ElementoRuleBase() {
    override val traitId: String = ID
    override val pagina: Int = 90
    override val faixas = listOf(
        Faixa(
            "Comum", 20,
            "Água, Fogo, Gravidade, Luz, Madeira, Metal, Plástico, Som, Terra"
        ),
        Faixa(
            "Ocasional", 15,
            "Cerâmica, Gelo, Metais Ferrosos, Pedra, Vapor"
        ),
        Faixa(
            "Raro", 10,
            "Borracha, Ferro, Papel, Tijolo"
        )
    )

    companion object {
        const val ID = "controle_poderes"
    }
}

/**
 * **Criar** — 40, 20, 10 ou 5 pontos **por nível**, pela amplitude da categoria.
 *
 * 🔴 São **quatro** faixas, e o catálogo trazia três: o `rawCost` dizia
 * *"10, 20 ou 40/nível"* e **Item Específico (5 pontos/nível) tinha ficado de
 * fora**. Descoberto ao ler a seção inteira para escrever esta regra — o mesmo
 * formato de erro da sessão toda: eu tinha lido até onde a resposta apareceu.
 */
class CriarRule : ElementoRuleBase() {
    override val traitId: String = ID
    override val pagina: Int = 92
    override val faixas = listOf(
        Faixa(
            "Categoria Ampla", 40,
            "Sólidos, Líquidos, Gases, Orgânico, Inorgânico, Ondas Eletromagnéticas"
        ),
        Faixa(
            "Categoria Média", 20,
            "Eletricidade, Som, Luz, Radiação, EM de Ondas Longas ou Curtas"
        ),
        Faixa(
            "Categoria Restrita", 10,
            "Metais Ferrosos, Fogo, Combustíveis Fósseis, Madeira, Luz Visível"
        ),
        Faixa(
            "Item Específico", 5,
            "Ferro, Sal, Água, Ar, Salmoura — um único elemento ou composto"
        )
    )

    companion object {
        const val ID = "criar_poderes"
    }
}

/** A regra de elemento de um traço, se ele tiver uma. */
fun regraDeElementoDe(traitId: String): ElementoRuleBase? = when (traitId) {
    ControleRule.ID -> ControleRule()
    CriarRule.ID -> CriarRule()
    else -> null
}
