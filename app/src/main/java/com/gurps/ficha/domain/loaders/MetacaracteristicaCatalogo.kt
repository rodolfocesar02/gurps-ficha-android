package com.gurps.ficha.domain.loaders

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.ModeloRacial
import com.gurps.ficha.model.TipoModeloRacial

/**
 * Catálogo PRONTO de metacaracterísticas (metacaracteristicas.v1.json,
 * read-only). GURPS p.262: na ficha aparece como UM traço (nome+custo),
 * mas os componentes EXISTEM estruturados (mesmo formato de raça:
 * vantagens/desvantagens com ids). O resolver reconstrói um ModeloRacial
 * interno (custoTotal recalcula ao editar) — reusa RacaCatalogo.resolver.
 */
data class MetacaracteristicaCatalogoItem(
    val id: String = "",
    val nome: String = "",
    val pagina: String = "",
    val descricao: String = "",
    val componentes: String = ""
)

object MetacaracteristicaCatalogo {

    /** Lê o catálogo cru (cada item já no formato RacaDefinicao para os
     *  componentes — vantagens/desvantagens/etc). */
    fun carregarBruto(context: Context): List<RacaDefinicao> {
        return try {
            val texto = context.assets.open("metacaracteristicas.v1.json")
                .bufferedReader().use { it.readText() }
            val root = JsonParser.parseString(texto)
            if (!root.isJsonObject) return emptyList()
            val arr = root.asJsonObject.getAsJsonArray("metacaracteristicas")
                ?: return emptyList()
            val gson = Gson()
            arr.mapNotNull { el ->
                runCatching { gson.fromJson(el, RacaDefinicao::class.java) }.getOrNull()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Resolve cada metacaracterística em um ModeloRacial completo
     *  (tipo = METACARACTERISTICA), com os componentes estruturados e
     *  custoTotal recalculável. Reusa o resolver de raça. */
    fun carregar(context: Context, repo: DataRepository): List<ModeloRacial> {
        return carregarBruto(context).map { def ->
            val res = RacaCatalogo.resolver(def, repo)
            res.modelo.copy(tipo = TipoModeloRacial.METACARACTERISTICA)
        }
    }
}
