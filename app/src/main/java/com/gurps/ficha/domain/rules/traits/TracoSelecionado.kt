package com.gurps.ficha.domain.rules.traits

/**
 * O que uma [TraitRule] precisa saber sobre o traço na ficha, seja ele
 * vantagem ou desvantagem.
 *
 * Existe por causa de um buraco silencioso (Lote D-0): os agregadores do
 * [TraitRuleRegistry] varriam apenas `personagem.vantagens`. Uma regra
 * registrada com o id de uma DESVANTAGEM nunca era chamada — sem erro, sem
 * aviso, simplesmente não acontecia nada. Isso bloqueava toda a automação de
 * desvantagem.
 *
 * Só os 4 campos que as regras de fato usam entram aqui (levantados no código:
 * `metadados` 26x, `nome` 5x, `definicaoId` 5x, `nivel` 2x). Manter o contrato
 * mínimo evita que a interface vire um espelho das data classes.
 *
 * Implementada por `VantagemSelecionada` e `DesvantagemSelecionada`, que já
 * tinham exatamente estes campos com os mesmos nomes — por isso nenhuma delas
 * precisou mudar de forma.
 */
interface TracoSelecionado {
    /** Id no catálogo (`vantagens.v3.json` / `desvantagens.v2.json`). */
    val definicaoId: String

    /** Nome exibido — algumas regras o usam para montar rótulo de ataque. */
    val nome: String

    /** Nível comprado; regras "por nível" multiplicam por ele. */
    val nivel: Int

    /** Escolhas do jogador feitas na UI (qual perícia, qual tipo de bônus...). */
    val metadados: Map<String, String>?
}
