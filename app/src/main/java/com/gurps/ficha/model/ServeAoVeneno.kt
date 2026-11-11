package com.gurps.ficha.model

/**
 * **Para que veneno este antídoto serve** — IC-4B do `mesa-virtual/planos/itens/PLANO.md` (pedido do Rodolfo, 11/nov).
 *
 * O Kit Antitoxinas é "antídoto para um veneno específico" (MB p.289): um kit de Veneno de Cobra não cura Cianureto. O jogador escolhe
 * o veneno na compra (e no lápis do item), e a Mesa só deixa usar o kit contra aquele veneno.
 *
 * Guarda o **id do catálogo de itens** (quando o veneno é um dos 7 do livro ou vem de um item do catálogo) e o **nome**; nunca uma chave
 * de veneno: quem traduz para a chave é a Mesa (a mesma conta que ela faz com o item de veneno do inventário), para os dois lados não
 * terem cada um a sua regra. Ficha antiga lê `null` (kit sem escolha: a Mesa não o deixa usar até escolher).
 */
data class ServeAoVeneno(
    val id: String? = null,
    val nome: String = ""
) {
    /** A escolha está feita (tem um nome para mostrar e a Mesa comparar). */
    val escolhido: Boolean get() = nome.isNotBlank()
}
