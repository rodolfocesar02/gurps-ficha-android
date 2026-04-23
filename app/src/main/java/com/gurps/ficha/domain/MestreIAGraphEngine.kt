package com.gurps.ficha.domain

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.storage.GraphNodeEntity
import com.gurps.ficha.model.MestreIAChunk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * O Novo Cérebro: Motor GraphRAG Local.
 * Em vez de buscar apenas palavras soltas em "pedaços" de texto,
 * este motor busca resumos de "comunidades" e conexões lógicas.
 */
object MestreIAGraphEngine {

    data class GraphSearchResult(
        val summaries: List<GraphNodeEntity>,
        val relatedChunks: List<MestreIAChunk>
    )

    /**
     * Realiza uma busca híbrida no Grafo: Resumos de Comunidade + Chunks Relacionados.
     * AGORA COM EXPANSÃO SEMÂNTICA (Lote 56): Busca palavras correlacionadas.
     */
    suspend fun buscarNoGrafo(query: String, repository: DataRepository): GraphSearchResult = withContext(Dispatchers.IO) {
        // 1. EXPANSÃO DE QUERY (Pensamento de Investigador)
        val palavrasOriginais = query.lowercase().split(" ").filter { it.length >= 2 }
        val termosExpandidos = mutableSetOf<String>()
        termosExpandidos.addAll(palavrasOriginais)

        // Suporte a Busca por Página (ex: "pág. 407" ou apenas "407")
        val paginaDetectada = Regex("(\\d+)").find(query)?.groupValues?.get(1)

        // Cruzar com o dicionário de temas do app
        repository.temasMestreIA.forEach { tema ->
            val match = palavrasOriginais.any { it == tema.id || it == tema.canonical || tema.keywords.contains(it) }
            if (match) {
                termosExpandidos.addAll(tema.keywords)
            }
        }

        // Criar a query final formatada para SQLite FTS (MATCH)
        // Se houver página, damos um peso enorme para ela na busca
        val queryFts = if (paginaDetectada != null && query.contains("pág", ignoreCase = true)) {
            "\"$paginaDetectada\" OR \"pág $paginaDetectada\""
        } else {
            termosExpandidos.take(20).joinToString(" OR ") { "\"$it\"" }
        }
        android.util.Log.d("MestreIA", "Query Expandida: $queryFts")

        // 2. Buscar nos Resumos do Grafo (Entidades e Comunidades)
        val essentialNodes = repository.buscarResumosEssenciais()
        val dynamicNodes = repository.buscarResumosGrafo(query) // Busca original no grafo para manter precisão
        
        val nodesFound = (essentialNodes + dynamicNodes).distinctBy { it.entityId }

        // 3. Buscar chunks específicos no manual usando a Query Expandida
        val chunksFound = repository.buscarRecortesManual(queryFts)

        // 4. INVESTIGAÇÃO DE VIZINHANÇA (Lote 69 - O Toque do Mestre)
        // Se achamos resultados, pegamos os chunks vizinhos (anterior e próximo)
        // para garantir que a regra não venha "cortada".
        val chunksComContexto = mutableSetOf<MestreIAChunk>()
        chunksComContexto.addAll(chunksFound)
        
        chunksFound.take(5).forEach { chunk ->
            repository.getChunkById(chunk.chunk_id - 1)?.let { chunksComContexto.add(it) }
            repository.getChunkById(chunk.chunk_id + 1)?.let { chunksComContexto.add(it) }
        }

        GraphSearchResult(
            summaries = nodesFound,
            relatedChunks = chunksComContexto.toList().sortedBy { it.chunk_id }
        )
    }

    /**
     * Formata os resultados do grafo para que a IA consiga entender as conexões.
     */
    fun formatarParaIA(result: GraphSearchResult): String {
        val sb = StringBuilder()
        
        if (result.summaries.isNotEmpty()) {
            sb.append("--- CONHECIMENTO DO GRAFO (RESUMOS) ---\n")
            result.summaries.forEach { node ->
                sb.append("Tópico: ${node.title} (${node.category})\n")
                sb.append("Resumo: ${node.summary}\n\n")
            }
        }

        if (result.relatedChunks.isNotEmpty()) {
            sb.append("--- DETALHES TÉCNICOS (MANUAL) ---\n")
            result.relatedChunks.take(15).forEach { chunk ->
                sb.append("[${chunk.source_title} pág. ${chunk.page_number}] ${chunk.text}\n")
            }
        }

        return sb.toString()
    }
}
