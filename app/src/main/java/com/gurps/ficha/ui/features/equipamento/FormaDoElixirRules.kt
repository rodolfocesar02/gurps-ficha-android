package com.gurps.ficha.ui.features.equipamento

import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.ItemDoCatalogo

/**
 * **A forma do elixir** — IC-5b do `mesa-virtual/planos/itens/PLANO.md` (Magia p.213).
 *
 * O elixir existe em quatro formas físicas (poção, pó, pastilha, unguento) e o livro lista as que cada um aceita. Quem compra escolhe a que
 * carrega: é ela que diz à Mesa se é "Beber", "Tomar", "Passar na pele" ou "Acender". Puro (sem Android), para o teste JVM varrer.
 */
object FormaDoElixirRules {

    /** O código que viaja para a Mesa, na ordem em que o livro apresenta as formas. */
    val TODAS = listOf("pocao", "po", "pastilha", "unguento")

    fun rotulo(codigo: String): String = when (codigo) {
        "pocao" -> "Poção"
        "po" -> "Pó"
        "pastilha" -> "Pastilha"
        "unguento" -> "Unguento"
        else -> codigo
    }

    /** Uma frase do que a forma faz (Magia p.213), para quem está escolhendo. */
    fun explica(codigo: String): String = when (codigo) {
        "pocao" -> "bebida, age na hora"
        "po" -> "na comida ou na bebida, age em 2d minutos"
        "pastilha" -> "acende e vira fumaça numa área de 3 m"
        "unguento" -> "na pele, age ao toque"
        else -> ""
    }

    private fun formasDaRegra(regra: com.google.gson.JsonObject?): List<String> {
        if (regra == null) return emptyList()
        val tipo = regra.get("tipo")?.takeIf { it.isJsonPrimitive }?.asString
        if (tipo != "elixir") return emptyList()
        val formas = regra.get("formas")?.takeIf { it.isJsonArray }?.asJsonArray ?: return emptyList()
        return formas.mapNotNull { runCatching { it.asString }.getOrNull() }.filter { it in TODAS }
    }

    /** As formas que o livro aceita para este elixir do catálogo (vazio se não é elixir). */
    fun formas(item: ItemDoCatalogo): List<String> = formasDaRegra(item.regra)
    fun formas(e: Equipamento): List<String> = formasDaRegra(e.regra)

    fun ehElixir(e: Equipamento): Boolean = e.regra?.get("tipo")?.takeIf { it.isJsonPrimitive }?.asString == "elixir"

    /** A forma que entra sozinha quando o livro só aceita uma. */
    fun unica(item: ItemDoCatalogo): String? = formas(item).singleOrNull()

    /** A linha do cartão do elixir no inventário: "Forma: Poção", ou o aviso de que falta escolher. */
    fun linha(e: Equipamento): String? {
        if (!ehElixir(e)) return null
        val f = e.formaDoElixir
        return if (f != null && f in formas(e)) "Forma: ${rotulo(f)}" else "Forma não escolhida: abra o lápis e escolha (a Mesa usa a primeira que o livro lista)"
    }
}
