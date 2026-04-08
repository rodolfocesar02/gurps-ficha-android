package com.gurps.ficha.domain

import com.google.gson.Gson
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.model.*
import com.gurps.ficha.domain.filters.CatalogFilters
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale


/**
 * O motor de RAG Local (Retrieval-Augmented Generation).
 * Busca nos assets locais itens relevantes para o contexto da conversa.
 */
object MestreIARagEngine {

    private val STOP_WORDS = setOf(
        "um", "uma", "o", "a", "de", "do", "da", "em", "com", "no", "na", 
        "para", "por", "que", "se", "seu", "sua", "como", "fazer", "criar",
        "quero", "me", "ajude", "personagem", "ficha", "gurps", "edicao"
    )
    
    // LOTE 56: Variável configurável para limite de contexto
    private var CHUNK_LIMIT = 10 

    data class RagResult(
        val vantagens: List<String> = emptyList(),
        val desvantagens: List<String> = emptyList(),
        val pericias: List<String> = emptyList(),
        val tecnicas: List<String> = emptyList(),
        val magias: List<String> = emptyList(),
        val chunks: List<MestreIAChunk> = emptyList()
    )

    /**
     * Varre o repositório em busca de termos que combinem com o prompt.
     */
    suspend fun buscarContexto(prompt: String, repository: DataRepository): RagResult {
        val keywordsBase = extrairKeywordsBase(prompt)
        if (keywordsBase.isEmpty()) return RagResult()
        
        // Expansão Semântica (Fase 5)
        val keywordsExpandidas = expandirKeywords(keywordsBase, repository.temasMestreIA)

        val v = buscarVantagens(keywordsExpandidas, repository.vantagens)
        val d = buscarDesvantagens(keywordsExpandidas, repository.desvantagens)
        val p = buscarPericias(keywordsExpandidas, repository.pericias)
        val t = buscarTecnicas(keywordsExpandidas, repository.tecnicasCatalogo)
        val m = buscarMagias(keywordsExpandidas, repository.magias)
        val chunksEncontrados = buscarEmChunks(keywordsBase, keywordsExpandidas, repository)

        return RagResult(v, d, p, t, m, chunksEncontrados)
    }

    /**
     * BUSCA INDEXADA (Lote 56): Substitui o linear scan pelo SQLite FTS.
     */
    private suspend fun buscarEmChunks(keywordsBase: List<String>, keywordsExpandidas: List<String>, repository: DataRepository): List<MestreIAChunk> {
        val context = repository.context
        val database = com.gurps.ficha.data.storage.FichaDatabase.getInstance(context)
        val dao = database.manualChunkDao()

        // Garante indexação inicial
        MestreIAIndexer.indexarSeNecessario(context)

        // Prepara a query para o FTS (Full-Text Search)
        // Usamos uma combinação de palavras-chave para o MATCH
        val keywordsBaseNorm = keywordsBase.map { CatalogFilters.normalizarBusca(it) }
        if (keywordsBaseNorm.isEmpty()) return emptyList()
        
        val ftsQuery = keywordsBaseNorm.joinToString(" ") { "$it*" }

        return try {
            val resultados = dao.buscarRegras(ftsQuery, CHUNK_LIMIT)
            resultados.map { entity ->
                MestreIAChunk(
                    source_title = entity.source_title,
                    page_number = entity.page_number,
                    text = entity.text
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("MestreIA_RAG", "Erro na busca FTS: ${e.message}")
            emptyList()
        }
    }

    private fun extrairKeywordsBase(prompt: String): List<String> {
        val normalizado = CatalogFilters.normalizarBusca(prompt)
        val kws = normalizado
            .split(Regex("\\s+"))
            .filter { it.length > 2 && it !in STOP_WORDS }
            .distinct()
        android.util.Log.d("MestreIA_RAG", "Keywords extraídas: $kws")
        return kws
    }

    private fun expandirKeywords(base: List<String>, temas: List<com.gurps.ficha.data.MestreIaTema>): List<String> {
        val resultado = base.toMutableSet()
        base.forEach { kw ->
            val temaEncontrado = temas.find { 
                CatalogFilters.igualNormalizado(it.canonical, kw) || 
                CatalogFilters.igualNormalizado(it.id, kw) 
            }
            temaEncontrado?.let { 
                resultado.addAll(it.keywords) 
            }
        }
        return resultado.toList()
    }

    private fun buscarVantagens(keywords: List<String>, lista: List<VantagemDefinicao>): List<String> {
        return lista.asSequence()
            .map { item -> item to calcularScore(keywords, item.nome, item.tags.joinToString(" ")) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(20)
            .map { it.first.nome }
            .toList()
    }

    private fun buscarDesvantagens(keywords: List<String>, lista: List<DesvantagemDefinicao>): List<String> {
        return lista.asSequence()
            .map { item -> item to calcularScore(keywords, item.nome, item.tags.joinToString(" ")) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(15)
            .map { it.first.nome }
            .toList()
    }

    private fun buscarPericias(keywords: List<String>, lista: List<PericiaDefinicao>): List<String> {
        return lista.asSequence()
            .map { item -> item to calcularScore(keywords, item.nome, item.atributoBase + " " + item.dificuldadeFixa) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(30)
            .map { it.first.nome }
            .toList()
    }

    private fun buscarMagias(keywords: List<String>, lista: List<MagiaDefinicao>): List<String> {
        return lista.asSequence()
            .map { item -> item to calcularScore(keywords, item.nome, item.escola?.joinToString(" ").orEmpty()) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(30)
            .map { it.first.nome }
            .toList()
    }

    private fun buscarTecnicas(keywords: List<String>, lista: List<TecnicaCatalogoItem>): List<String> {
        return lista.asSequence()
            .map { item -> item to calcularScore(keywords, item.nome, item.preRequisitoRaw) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(20)
            .map { it.first.nome }
            .toList()
    }

    private fun calcularScore(keywords: List<String>, nome: String, metadados: String): Int {
        var score = 0
        val nomeNorm = CatalogFilters.normalizarBusca(nome)
        val metaNorm = CatalogFilters.normalizarBusca(metadados)

        keywords.forEach { kw ->
            val kwNorm = CatalogFilters.normalizarBusca(kw)
            if (nomeNorm.contains(kwNorm)) score += 10
            if (metaNorm.contains(kwNorm)) score += 5
        }
        return score
    }
}
