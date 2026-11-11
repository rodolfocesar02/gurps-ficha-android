package com.gurps.ficha.domain.loaders

import com.google.gson.Gson
import com.gurps.ficha.domain.rules.CartaoDoItem
import com.gurps.ficha.model.CatalogoDeItens
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.ItemDoCatalogo
import com.gurps.ficha.model.TipoEquipamento

/**
 * **O catálogo de itens do livro** — IC-2 do `mesa-virtual/planos/itens/PLANO.md`.
 *
 * Lê o `itens.v1.json` e faz as três coisas que a tela precisa, sem Android no meio (para o teste JVM varrer os 207):
 * filtrar, dizer o que o item faz em linhas de cartão, e transformar o item escolhido em item do inventário.
 */
object ItensCatalogLoader {

    const val ARQUIVO = "itens.v1.json"
    const val TODAS = "Todas"

    fun ler(json: String): List<ItemDoCatalogo> =
        try { Gson().fromJson(json, CatalogoDeItens::class.java)?.itens ?: emptyList() } catch (_: Exception) { emptyList() }

    /** O tipo de regra do item (`narrativo` quando não tem regra nenhuma). */
    fun tipoDe(item: ItemDoCatalogo): String =
        item.regra?.get("tipo")?.takeIf { it.isJsonPrimitive }?.asString ?: "narrativo"

    /** O tipo em português, para a tela (nunca o nome cru do campo). */
    fun rotuloDoTipo(tipo: String): String = when (tipo) {
        "veneno" -> "Veneno"
        "elixir" -> "Elixir"
        "explosivo" -> "Explosivo"
        "municao" -> "Munição"
        "acessorio" -> "Acessório de arma"
        "kit" -> "Kit ou ferramenta"
        "luz" -> "Luz"
        "cura" -> "Cura"
        "concede" -> "Dá uma vantagem"
        else -> "Sem regra: o Mestre conduz"
    }

    /** As categorias do livro, na ordem em que aparecem no catálogo. */
    fun categorias(itens: List<ItemDoCatalogo>): List<String> = itens.map { it.categoria }.distinct()

    fun filtrar(itens: List<ItemDoCatalogo>, busca: String, categoria: String?): List<ItemDoCatalogo> {
        val q = semAcento(busca.trim())
        return itens.filter { i ->
            (categoria == null || categoria == TODAS || i.categoria == categoria) &&
                (q.isEmpty() || semAcento(i.nome).contains(q) || semAcento(i.nota).contains(q))
        }
    }

    /** "MB p.288", "Magia p.217". */
    fun referencia(item: ItemDoCatalogo): String = "${item.livro} p.${item.pagina}"

    /** O preço como o livro dá: "$10 por dose", "$5 (cada 10 metros)", "sem preço no livro". */
    fun preco(item: ItemDoCatalogo): String {
        val c = item.custo ?: return "sem preço no livro"
        val base = "$" + numero(c)
        return when {
            item.porDose -> "$base por dose"
            !item.unidade.isNullOrBlank() -> "$base (${item.unidade})"
            else -> base
        }
    }

    /** As linhas do cartão: o que o item faz, os números do livro e o preço. As mesmas na lista e na confirmação. */
    fun linhasDoItem(item: ItemDoCatalogo): List<CartaoDoItem.Linha> = buildList {
        val tipo = tipoDe(item)
        val papel = when (tipo) {
            "veneno", "explosivo", "municao" -> CartaoDoItem.Papel.DANO
            "narrativo" -> CartaoDoItem.Papel.NEUTRO
            else -> CartaoDoItem.Papel.PROTECAO
        }
        if (item.nota.isNotBlank()) add(CartaoDoItem.Linha(item.nota, papel))
        else if (tipo == "elixir") add(CartaoDoItem.Linha("Efeito de interpretação: o Mestre conduz.", CartaoDoItem.Papel.NEUTRO))
        if (tipo == "elixir") formas(item)?.let { add(CartaoDoItem.Linha("Formas: $it", CartaoDoItem.Papel.NEUTRO)) }
        val fatos = buildList {
            add(rotuloDoTipo(tipo))
            when {
                item.ntVariavel -> add("NT da perícia")
                item.nt != null -> add("NT ${item.nt}")
            }
            if (item.peso > 0f) add("${numero(item.peso)} kg")
            if (item.legalidade != 4) add("CL ${item.legalidade}")
        }
        add(CartaoDoItem.Linha(fatos.joinToString(" · "), CartaoDoItem.Papel.NEUTRO))
        add(CartaoDoItem.Linha("Custo: ${preco(item)} · ${referencia(item)}", CartaoDoItem.Papel.CUSTO))
    }

    /**
     * O item escolhido vira item do inventário: os dados do livro e a `regra` **dentro dele**.
     *
     * ⚠️ As `notas` levam a frase do que ele faz e a página: é o que o cartão do inventário já mostra, sem código novo.
     */
    fun equipamentoDoItem(item: ItemDoCatalogo, quantidade: Int = 1, serveAoVeneno: com.gurps.ficha.model.ServeAoVeneno? = null, formaDoElixir: String? = null): Equipamento = Equipamento(
        nome = item.nome,
        peso = item.peso,
        custo = item.custo ?: 0f,
        quantidade = quantidade.coerceAtLeast(1),
        notas = listOf(item.nota, referencia(item)).filter { it.isNotBlank() }.joinToString(" "),
        tipo = TipoEquipamento.GERAL,
        itemCatalogoId = item.id,
        regra = item.regra?.deepCopy(),
        serveAoVeneno = serveAoVeneno?.takeIf { it.escolhido },   // IC-4B: o antídoto serve a um veneno só, escolhido na compra
        formaDoElixir = formaDoElixir?.takeIf { f -> f in com.gurps.ficha.ui.features.equipamento.FormaDoElixirRules.formas(item) }   // IC-5b: só uma forma que o livro aceita
    )

    private fun formas(item: ItemDoCatalogo): String? {
        val f = item.regra?.get("formas")?.takeIf { it.isJsonArray }?.asJsonArray ?: return null
        val nomes = f.mapNotNull {
            when (it.asString) {
                "pocao" -> "poção"
                "po" -> "pó"
                "pastilha" -> "pastilha"
                "unguento" -> "unguento"
                else -> null
            }
        }
        return nomes.takeIf { it.isNotEmpty() }?.joinToString(", ")
    }

    private fun numero(f: Float): String =
        if (f % 1f == 0f) f.toInt().toString() else f.toString().replace('.', ',')

    private fun semAcento(s: String): String =
        java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase()
}
