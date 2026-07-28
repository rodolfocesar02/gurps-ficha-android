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

    /**
     * A faixa de custo escolhida, para traços de custo ESCOLHA/VARIÁVEL.
     *
     * Entrou no Lote OPCAO-1. Vários traços não têm nível: têm **degraus**, e o
     * efeito muda de degrau para degrau. Aparência custa 4, 12, 16 ou 20 pontos
     * e dá +1, +2, +2, +2 de reação (MB p.21); Hábitos Detestáveis custa −5, −10
     * ou −15 e dá −1, −2 ou −3. Sem este campo, o interpretador não tinha como
     * saber QUAL degrau o jogador comprou — e os três maiores modificadores de
     * reação do livro ficavam de fora.
     *
     * Vale 0 em traço de custo fixo ou por nível, onde não há escolha.
     */
    val custoEscolhido: Int

    /**
     * De qual catálogo este traço veio.
     *
     * Parece redundante — e era, até 28/07/2026. **Seis ids existem nos DOIS
     * catálogos**: `aparencia`, `destino`, `forma_de_sombras`, `reputacao`,
     * `riqueza` e `status`. São traços que o GURPS trata como uma escala única
     * que atravessa o zero: Aparência vai de Horrendo (−24 pts) a Lindo (+20).
     *
     * O [EfeitoInterpretador] procurava sempre em vantagens primeiro, com um
     * comentário afirmando que "o mesmo id nunca existe nos dois". Estava
     * errado; só não fazia mal porque nenhum id repetido tinha `efeitos`. Ao
     * declarar a Aparência, quem comprasse a versão FEIA receberia os efeitos
     * da versão BONITA — bônus no lugar de penalidade.
     */
    val ehDesvantagem: Boolean get() = false
}
