package com.gurps.ficha.model

import com.google.gson.JsonObject

/**
 * **Um item do livro, com a regra dele** — IC-2 do `mesa-virtual/planos/itens/PLANO.md`.
 *
 * Vem do catálogo `assets/itens.v1.json` (gerado dos PDFs por `scripts/itens/gerar_itens.py`: Módulo Básico pp.277,
 * 279-280, 288-289, 439 e Magia pp.213-219). A `regra` tem um `tipo` (veneno, elixir, explosivo, municao, acessorio,
 * kit, luz, cura, concede, narrativo) e os números daquele tipo; ela viaja DENTRO do item do inventário
 * (`Equipamento.regra`), como a ficha técnica da montaria, para a Mesa aplicar a regra pelo tipo e nunca pelo nome.
 *
 * ⚠️ A `regra` fica como `JsonObject` de propósito: cada tipo tem campos diferentes, e o app não faz conta com eles
 * (quem faz é a Mesa). Guardar o objeto como veio é o que garante que o número que sai do livro é o que chega lá.
 */
data class ItemDoCatalogo(
    val id: String = "",
    val nome: String = "",
    val categoria: String = "",
    val nt: Int? = null,
    val ntVariavel: Boolean = false,
    val peso: Float = 0f,
    val custo: Float? = null,
    val custoMagiaRara: Float? = null,
    val porDose: Boolean = false,
    val unidade: String? = null,
    val legalidade: Int = 4,
    val pagina: Int = 0,
    val livro: String = "MB",
    val nota: String = "",
    val regra: JsonObject? = null
)

/** O catálogo como está no arquivo. */
data class CatalogoDeItens(val versao: Int = 1, val fonte: String = "", val itens: List<ItemDoCatalogo> = emptyList())
