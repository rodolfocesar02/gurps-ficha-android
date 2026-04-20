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
     */
    suspend fun buscarNoGrafo(query: String, repository: DataRepository): GraphSearchResult = withContext(Dispatchers.IO) {
        // 1. Buscar nos Resumos do Grafo (Entidades e Comunidades)
        // Usamos FTS4 para encontrar os caminhos lógicos mais prováveis
        val nodesFound = repository.buscarResumosGrafo(query)

        // 2. Buscar chunks específicos que dão suporte a esses nós (Busca Legada como Fallback/Suporte)
        val chunksFound = repository.buscarRecortesManual(query)

        GraphSearchResult(
            summaries = nodesFound,
            relatedChunks = chunksFound
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
            result.relatedChunks.take(3).forEach { chunk ->
                sb.append("[${chunk.source_title} pág. ${chunk.page_number}] ${chunk.text}\n")
            }
        }

        return sb.toString()
    }
}
