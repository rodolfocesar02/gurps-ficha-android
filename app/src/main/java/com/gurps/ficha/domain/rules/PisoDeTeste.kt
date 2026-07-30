package com.gurps.ficha.domain.rules

/**
 * **O alvo de um teste nunca desce abaixo de 3** (Lote D-ESPELHO).
 *
 * ## Por que 3, e não 1
 *
 * Em 3d6 o **menor resultado possível é 3**. Um alvo de 3 ainda deixa uma chance
 * mínima — 0,46%, um resultado em 216. Um alvo **2 seria fracasso automático,
 * sempre**, e o livro não quer que uma desvantagem torne o teste impossível: ela
 * deve piorar as chances, não apagá-las.
 *
 * ## Por que num lugar só
 *
 * Três regras diferentes do livro repetem o mesmo piso, palavra por palavra:
 *
 * - **Fácil de Matar** (MB p.140): *"Os testes de HT não podem ser reduzidos
 *   abaixo de 3."*
 * - **Temor** (MB p.159): *"Não é permitido reduzir o número alvo do teste de
 *   Vontade a um valor menor que 3."*
 * - **Suscetível** (MB p.159): o mesmo, sobre a HT efetiva.
 *
 * Espalhado, o `3` viveria em **cinco** pontos do código — e o risco não é
 * esquecer de escrevê-lo, é alguém digitar `4` num deles. Cada trecho pareceria
 * certo sozinho.
 *
 * Analogia: é o salário mínimo escrito em cinco contratos. Num lugar só, um erro
 * aparece nos cinco de uma vez e o teste pega.
 *
 * ## ⚠️ Piso na ROLAGEM, aviso na compra
 *
 * O livro expressa o limite de duas formas — como piso do alvo (*"o alvo não
 * desce de 3"*) e como teto de compra (*"HT 10 está limitado a Fácil de Matar
 * 7"*). O app aplica na **rolagem**.
 *
 * A escolha é deliberada e segue a decisão do teto de HT do Magro: **avisar, não
 * impedir**. Bloquear a compra invalidaria ficha antiga que já passou do limite;
 * o piso na rolagem faz a ficha continuar abrindo e o número sair certo.
 */
object PisoDeTeste {

    /** O menor alvo que qualquer teste de 3d6 pode ter (MB p.140/159). */
    const val MINIMO = 3

    /**
     * Aplica o piso a um alvo já calculado.
     *
     * Use **depois** de somar bônus e penalidades — o piso é a última palavra.
     */
    fun aplicar(alvo: Int): Int = alvo.coerceAtLeast(MINIMO)

    /**
     * Quantos níveis de uma desvantagem cabem antes de o piso começar a comer o
     * resto — o "teto de compra" que o livro cita nos exemplos.
     *
     * Com HT 10, devolve **7**: é o exemplo do próprio livro para Fácil de Matar.
     * Serve para o aviso na tela, não para bloquear.
     */
    fun tetoDeNiveis(atributo: Int): Int = (atributo - MINIMO).coerceAtLeast(0)
}
