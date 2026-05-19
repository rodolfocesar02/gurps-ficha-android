package com.gurps.ficha.domain.loaders

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParser

/**
 * Catálogo PRONTO de metacaracterísticas (metacaracteristicas.v1.json,
 * asset read-only — mesmo padrão de racas.v1.json). GURPS p.262/B262:
 * a metacaracterística é UM traço de custo único; `componentes` é só
 * informativo (o Mestre pode ajustar elementos/custo ao usar).
 *
 * O seletor da UI combina ISTO (do livro) com as criadas pelo usuário
 * (MetacaracteristicaStore, filesDir).
 */
data class MetacaracteristicaCatalogoItem(
    val id: String = "",
    val nome: String = "",
    val custo: Int = 0,
    val pagina: String = "",
    val descricao: String = "",
    val componentes: String = ""
)

object MetacaracteristicaCatalogo {
    fun carregar(context: Context): List<MetacaracteristicaCatalogoItem> {
        return try {
            val texto = context.assets.open("metacaracteristicas.v1.json")
                .bufferedReader().use { it.readText() }
            val root = JsonParser.parseString(texto)
            if (!root.isJsonObject) return emptyList()
            val arr = root.asJsonObject.getAsJsonArray("metacaracteristicas")
                ?: return emptyList()
            val gson = Gson()
            arr.mapNotNull { el ->
                runCatching {
                    gson.fromJson(el, MetacaracteristicaCatalogoItem::class.java)
                }.getOrNull()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
