package com.gurps.ficha.domain

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.*
import com.gurps.ficha.data.storage.GraphNodeEntity

/**
 * Motor de busca híbrido (Grafo + Manual)
 * Lote 71: Implementação de Scoring e Re-Ranking para "Informação Mastigada".
 */
class MestreIAGraphEngine(private val repository: DataRepository) {

    data class GraphSearchResult(
        val summaries: List<GraphNodeEntity>,
        val relatedChunks: List<MestreIAChunk>
    )

    suspend fun buscarNoGrafo(query: String): GraphSearchResult {
        // 1. Busca Segmentada e Relacional (Estilo Nano-GraphRAG)
        val subPerguntas = query.split("?", ".").filter { it.trim().length > 4 }
        val chunksComContexto = mutableSetOf<MestreIAChunk>()
        val topNodes = mutableSetOf<GraphNodeEntity>()
        
        for (sub in subPerguntas) {
            val termosSemente = extrairPalavrasChave(sub)
            if (termosSemente.isEmpty()) continue
            
            // SALTO 1: Busca de Entidades Semente
            val querySemente = termosSemente.joinToString(" OR ") { "$it*" }
            val nodesSemente = repository.buscarResumosGrafo(querySemente).take(15)
            topNodes.addAll(nodesSemente)
            
            // SALTO 2: Expansão Relacional (Busca vizinhos por contexto)
            val termosRelacionais = mutableSetOf<String>()
            nodesSemente.forEach { node ->
                // Extrai palavras em caixa alta ou termos técnicos do resumo
                val matchConceitos = Regex("([A-ZÀ-Ú][a-zà-ú]{3,}|Pág\\.?\\s?\\d+)").findAll(node.summary)
                matchConceitos.forEach { termosRelacionais.add(it.value.replace("Pág.", "").trim()) }
            }
            
            val queryRelacional = termosRelacionais.take(10).joinToString(" OR ") { "$it*" }
            if (queryRelacional.isNotBlank()) {
                val nodesVizinhos = repository.buscarResumosGrafo(queryRelacional).take(10)
                topNodes.addAll(nodesVizinhos)
            }

            // BUSCA DE REGRAS (Manual) baseada em todos os nós encontrados
            val termosBuscaManual = (termosSemente + termosRelacionais.take(5)).distinct()
            val queryFtsManual = termosBuscaManual.joinToString(" OR ") { "$it*" }
            android.util.Log.i("MestreIA", "Query GraphRAG: $queryFtsManual")
            
            val chunksRaw = repository.buscarRecortesManual(queryFtsManual, 15)
            chunksRaw.forEach { chunk ->
                chunksComContexto.add(chunk)
                val idBase = chunk.chunk_id.substringBeforeLast("_c")
                val currentIdx = chunk.chunk_id.substringAfterLast("_c").toIntOrNull() ?: return@forEach
                repository.getChunkById("${idBase}_c${currentIdx - 1}")?.let { chunksComContexto.add(it) }
                repository.getChunkById("${idBase}_c${currentIdx + 1}")?.let { chunksComContexto.add(it) }
            }
        }

        // Ordena por ID e limita para evitar overflow
        val listaOrdenada = chunksComContexto.toList()
            .sortedBy { it.chunk_id }
            .take(80) // Aumentado para 80 para cobrir o grafo expandido

        return GraphSearchResult(
            summaries = topNodes.toList().take(40),
            relatedChunks = listaOrdenada
        )
    }

    private fun extrairPalavrasChave(texto: String): List<String> {
        return texto.trim().lowercase()
            .replace(Regex("[^a-zA-Z0-9\\sà-úÀ-Ú]"), " ")
            .split(" ")
            .filter { it.length >= 3 && it !in listOf("como", "funciona", "regra", "quais", "preciso", "para", "sobre", "qual") }
    }

    /**
     * Formata os resultados para o prompt da IA.
     */
    suspend fun formatarParaIA(result: GraphSearchResult): String {
        val s = StringBuilder()
        if (result.summaries.isNotEmpty()) {
            s.append("\n=== CONHECIMENTO DO GRAFO (RESUMOS) ===\n")
            result.summaries.forEach { s.append("Tópico: ${it.title} | Cat: ${it.category} | Resumo: ${it.summary}\n") }

            // LOTE 71: A PONTE DE FERRO (Grafo + Catálogo)
            val magiasIds = result.summaries.filter { it.category.equals("Magia", true) }.take(5).map { it.title.lowercase().trim() }
            if (magiasIds.isNotEmpty()) {
                s.append("\n=== FICHA TÉCNICA DAS MAGIAS (DETALHADO) ===\n")
                val magiasParaInjetar = mutableSetOf<MagiaDefinicao>()
                
                // Pega as magias identificadas
                val identificadas = repository.magias.filter { it.nome.lowercase().trim() in magiasIds }
                magiasParaInjetar.addAll(identificadas)
                
                // Nível 1 de dependência
                val nivel1 = mutableListOf<MagiaDefinicao>()
                identificadas.forEach { m ->
                    val preReqs = m.preRequisitos?.split(" e ", ",")?.map { it.trim().lowercase() } ?: emptyList()
                    repository.magias.filter { it.nome.lowercase().trim() in preReqs }.forEach { 
                        if (magiasParaInjetar.add(it)) nivel1.add(it)
                    }
                }
                
                // Nível 2 de dependência
                nivel1.forEach { m ->
                    val preReqs = m.preRequisitos?.split(" e ", ",")?.map { it.trim().lowercase() } ?: emptyList()
                    repository.magias.filter { it.nome.lowercase().trim() in preReqs }.forEach { magiasParaInjetar.add(it) }
                }

                // LOTE 78: INJEÇÃO INTELIGENTE (Ficha completa só para o Top 3)
                magiasParaInjetar.forEachIndexed { index, m ->
                    val incluirDescricao = index < 3 // Apenas as 3 mais relevantes levam descrição
                    s.append("ID: ${m.id} | Nome: ${m.nome} | Requisitos: ${m.preRequisitos} | Pág: ${m.pagina}\n")
                    if (incluirDescricao) {
                        val desc = (m.descricao ?: m.texto ?: "").take(1000)
                        s.append("Descrição: $desc\n")
                    }
                    s.append("---\n")
                }

                // INJEÇÃO GABARITO NEXUS (MOTOR ALVO)
                for (nome in magiasIds) {
                    s.append(resolverTrilhaNexus(nome))
                }
            }
        }
        if (result.relatedChunks.isNotEmpty()) {
            s.append("\n=== REGRAS DO CÓDEX (PÁGINAS DO MANUAL) ===\n")
            // Limite de segurança de 80k caracteres para evitar overflow do prompt
            for (chunk in result.relatedChunks) {
                if (s.length > 80000) {
                    s.append("\n[Corte de segurança: Contexto muito extenso...]\n")
                    break
                }
                s.append("[${chunk.source_title} Pág. ${chunk.page_number}]: ${chunk.text}\n")
            }
        }
        return s.toString()
    }

    private suspend fun resolverTrilhaNexus(termoBusca: String): String {
        val s = StringBuilder()
        val todasMagias = repository.magias
        val magiaAlvo = todasMagias.find { 
            it.nome.equals(termoBusca, ignoreCase = true) || it.id.equals(termoBusca, ignoreCase = true) 
        } ?: return ""

        s.append("\n=== GABARITO NEXUS (TRILHA TÉCNICA REAL) ===\n")
        s.append("Para a magia '${magiaAlvo.nome}', o caminho exato é:\n")

        val visitados = mutableSetOf<String>()
        val trilha = mutableListOf<String>()
        
        fun calcular(m: MagiaDefinicao) {
            if (m.id in visitados) return
            visitados.add(m.id)
            
            val reqStr = m.preRequisitos ?: ""
            if (reqStr.isBlank()) return

            // 1. Resolver Escolas (Ex: 1 em 10 escolas ou 1 em quinze escolas)
            if (reqStr.contains("escola", ignoreCase = true)) {
                val match = Regex("(\\d+|dez|quinze|vinte).*?(\\d+|dez|quinze|vinte) escolas").find(reqStr)
                val qtdText = match?.groupValues?.getOrNull(2) ?: "15"
                val qtd = when (qtdText.lowercase()) {
                    "dez" -> 10
                    "quinze" -> 15
                    "vinte" -> 20
                    else -> qtdText.toIntOrNull() ?: 15
                }
                
                s.append("- Guia de Diversidade ($qtd Escolas necessárias):\n")
                val basicas = mutableListOf<String>()
                val escolasVistas = mutableSetOf<String>()
                // Busca em todas as magias do repositório
                todasMagias.filter { (it.preRequisitos?.length ?: 0) < 15 }.forEach { basic ->
                    basic.escola?.forEach { esc ->
                        if (esc !in escolasVistas && escolasVistas.size < 25) { // Pega até 25 para garantir
                            basicas.add("${basic.nome} ($esc) [Pág. ${basic.pagina}]")
                            escolasVistas.add(esc)
                        }
                    }
                }
                s.append("  Sugeridas: " + basicas.joinToString(", ") + "\n")
            }

            // 2. Resolver Dependências Nominais (Recursão)
            val partes = reqStr.split(" e ", " ou ", ",").map { it.trim() }
            partes.forEach { p ->
                if (p.length > 3 && !p.contains("IQ") && !p.contains("escola")) {
                    val sub = todasMagias.find { it.nome.equals(p, ignoreCase = true) }
                    if (sub != null) {
                        calcular(sub)
                        trilha.add("${sub.nome} -> ${m.nome}")
                    }
                }
            }
        }

        calcular(magiaAlvo)
        trilha.distinct().forEach { s.append("- $it\n") }
        
        return s.toString()
    }

    private fun extrairIdsDaString(texto: String): List<String> {
        return texto.split(",", ";", "/").map { it.trim().lowercase().replace(" ", "_") }.filter { it.isNotBlank() }
    }

    private fun sanitizarTexto(texto: String): String {
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
            .replace(Regex("[\\p{InCombiningDiacriticalMarks}]"), "")
            .replace(Regex("[^a-zA-Z0-9\\s,;/]"), "")
    }
}
