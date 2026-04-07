package com.gurps.ficha.domain

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.domain.filters.CatalogFilters
import com.gurps.ficha.model.*
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

    data class RagResult(
        val vantagens: List<String> = emptyList(),
        val desvantagens: List<String> = emptyList(),
        val pericias: List<String> = emptyList(),
        val magias: List<String> = emptyList()
    )

    /**
     * Varre o repositório em busca de termos que combinem com o prompt.
     */
    fun buscarContexto(prompt: String, repository: DataRepository): RagResult {
        val keywordsBase = extrairKeywordsBase(prompt)
        if (keywordsBase.isEmpty()) return RagResult()
        
        // Expansão Semântica (Fase 5)
        val keywordsExpandidas = expandirKeywords(keywordsBase, repository.temasMestreIA)

        return RagResult(
            vantagens = buscarVantagens(keywordsExpandidas, repository.vantagens),
            desvantagens = buscarDesvantagens(keywordsExpandidas, repository.desvantagens),
            pericias = buscarPericias(keywordsExpandidas, repository.pericias),
            magias = buscarMagias(keywordsExpandidas, repository.magias)
        )
    }

    private fun extrairKeywordsBase(prompt: String): List<String> {
        return prompt.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 && it !in STOP_WORDS }
            .distinct()
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
