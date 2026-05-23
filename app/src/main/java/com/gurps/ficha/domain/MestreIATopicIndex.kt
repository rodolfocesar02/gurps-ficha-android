package com.gurps.ficha.domain

import android.content.Context
import com.gurps.ficha.domain.filters.CatalogFilters
import org.json.JSONArray
import org.json.JSONObject

/**
 * Índice de tópicos — substitui o sistema hardcoded de "fonte garantida".
 * Lê topic_index.json de assets e retorna (source_id, pages[]) para injeção garantida no RAG.
 *
 * Lote 256: Melhoria 2D — garante que páginas críticas (ex: Pyramid p.7 = tiro subaquático)
 * entrem no contexto mesmo quando a FTS4 falha por keyword mismatch.
 */
object MestreIATopicIndex {

    data class PaginasGarantidas(
        val sourceId: String,
        val pages: List<Int>
    )

    data class TopicEntry(
        val id: String,
        val keywords: List<String>,
        val requireAll: List<String>,
        val fallbackAny: List<List<String>>,
        val pages: List<PaginasGarantidas>
    )

    private var topicos: List<TopicEntry> = emptyList()
    private var carregado = false

    fun carregar(context: Context) {
        if (carregado) return
        try {
            val json = context.assets.open("topic_index.json")
                .bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            val arr = root.getJSONArray("topics")
            val lista = mutableListOf<TopicEntry>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                lista.add(parseEntry(obj))
            }
            topicos = lista
            carregado = true
            android.util.Log.i("MestreIA_TopicIdx", "topic_index carregado: ${topicos.size} tópicos")
        } catch (e: Exception) {
            android.util.Log.e("MestreIA_TopicIdx", "Falha ao carregar topic_index.json: ${e.message}")
        }
    }

    private fun parseEntry(obj: JSONObject): TopicEntry {
        val pagesArr = obj.getJSONArray("pages")
        val pages = mutableListOf<PaginasGarantidas>()
        for (j in 0 until pagesArr.length()) {
            val p = pagesArr.getJSONObject(j)
            val plist = p.getJSONArray("pages").let { a -> (0 until a.length()).map { a.getInt(it) } }
            pages.add(PaginasGarantidas(p.getString("source_id"), plist))
        }

        fun toList(arr: JSONArray) = (0 until arr.length()).map { arr.getString(it) }
        fun toListOfList(arr: JSONArray) = (0 until arr.length()).map { toList(arr.getJSONArray(it)) }

        return TopicEntry(
            id = obj.getString("id"),
            keywords = toList(obj.getJSONArray("keywords")),
            requireAll = toList(obj.getJSONArray("require_all")),
            fallbackAny = if (obj.has("fallback_any")) toListOfList(obj.getJSONArray("fallback_any")) else emptyList(),
            pages = pages
        )
    }

    /**
     * Retorna as páginas garantidas para uma query normalizada.
     * Matching: require_all → todos devem estar presentes na query.
     * Fallback: basta um par de [keyword1, keyword2] estar na query.
     */
    fun resolverPaginasGarantidas(query: String): List<PaginasGarantidas> {
        if (!carregado || topicos.isEmpty()) return emptyList()

        val queryNorm = CatalogFilters.normalizarBusca(query)
        val tokens = queryNorm.split(Regex("\\s+")).filter { it.length >= 2 }.toSet()

        val resultado = mutableListOf<PaginasGarantidas>()

        for (topico in topicos) {
            val match = when {
                // Modo primário: todos os termos de require_all presentes
                topico.requireAll.isNotEmpty() && topico.requireAll.all { req ->
                    tokens.any { t -> t.contains(req) || req.contains(t) }
                } -> true

                // Modo fallback: pelo menos um par [k1, k2] presente
                topico.fallbackAny.isNotEmpty() && topico.fallbackAny.any { par ->
                    par.all { req -> tokens.any { t -> t.contains(req) || req.contains(t) } }
                } -> true

                else -> false
            }

            if (match) {
                android.util.Log.i("MestreIA_TopicIdx", "MATCH tópico '${topico.id}' → ${topico.pages.map { "${it.sourceId}:${it.pages}" }}")
                resultado.addAll(topico.pages)
            }
        }

        // Deduplica: se o mesmo source_id+page aparece em múltiplos tópicos, mantém só uma vez
        return resultado
            .groupBy { it.sourceId }
            .map { (sid, entries) ->
                PaginasGarantidas(sid, entries.flatMap { it.pages }.distinct().sorted())
            }
    }
}
