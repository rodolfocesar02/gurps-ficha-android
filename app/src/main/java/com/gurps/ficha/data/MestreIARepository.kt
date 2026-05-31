package com.gurps.ficha.data

import android.content.Context
import com.gurps.ficha.data.storage.FichaDatabase
import com.gurps.ficha.model.MestreIAChunk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * MestreIARepository - Especialista em persistência e sincronização do Códex (RAG).
 * Isola a lógica do motor de regras da lógica da ficha de personagem.
 */
class MestreIARepository(
    private val context: Context,
    private val database: FichaDatabase
) {
    private val manualChunkDao = database.manualChunkDao()
    private val syncMutex = Mutex()

    // Lote 327: N do corpus para IDF do BM25 do localizar. Preenchido na 1ª busca.
    @Volatile private var corpusSizeCache: Int = 1197

    // LRU Cache de buscas FTS — evita re-processar queries repetidas na mesma sessão.
    // Tamanho 20: cobre multi-query temático (4 queries × 5 perguntas) sem pressão de memória.
    private val ftsCache = object : LinkedHashMap<String, List<MestreIAChunk>>(20, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<MestreIAChunk>>) = size > 20
    }
    private val cacheMutex = Mutex()

    // Versão do formato de search_text. Bump aqui + em CODEX_VERSION_CURRENT para forçar re-importação.
    // v1: apenas texto do chunk. v2: texto + source_title (permite buscar "subaquatico" → chunks do Pyramid).
    private val CODEX_VERSION_KEY = "codex_search_text_version"
    private val CODEX_VERSION_CURRENT = 3  // Lote 266: força reimportação de embeddings semânticos

    /**
     * Sincroniza o Códex (chunks.jsonl) se o banco estiver vazio ou desatualizado.
     * Operação atômica e idempotente protegida por Mutex.
     */
    suspend fun sincronizarCodexSeNecessario() = withContext(Dispatchers.IO) {
        syncMutex.withLock {
            val prefs = context.getSharedPreferences("mestre_ia_prefs", android.content.Context.MODE_PRIVATE)
            val versaoSalva = prefs.getInt(CODEX_VERSION_KEY, 1)
            val count = manualChunkDao.getCount()
            val isEmpty = count == 0
            val desatualizado = versaoSalva < CODEX_VERSION_CURRENT

            if (isEmpty || desatualizado) {
                if (desatualizado && !isEmpty) {
                    android.util.Log.i("MestreIA_Auditoria", "CÓDEX DESATUALIZADO (v$versaoSalva → v$CODEX_VERSION_CURRENT): Limpando e re-importando...")
                    manualChunkDao.clearAll()
                } else {
                    android.util.Log.i("MestreIA_Auditoria", "CÓDEX VAZIO: Importando chunks.jsonl...")
                }
                FichaDatabase.prePopulateManual(context, database)
                prefs.edit().putInt(CODEX_VERSION_KEY, CODEX_VERSION_CURRENT).apply()
            } else {
                android.util.Log.i("MestreIA_Auditoria", "CÓDEX OK v$versaoSalva: ${manualChunkDao.getCount()} chunks no banco.")
            }
        }
    }

    /**
     * Busca DIRETA no Códex via FTS4 com LRU cache de sessão.
     * Cache evita re-processar a mesma query FTS dentro da mesma conversa (multi-query temático).
     */
    suspend fun buscarNoCodexDireto(query: String, termosTecnicos: List<String> = emptyList(), limit: Int = 500): List<MestreIAChunk> {
        val ftsQuery = prepararQueryFTSAgressiva(query, termosTecnicos)
        val cacheKey = "$ftsQuery:$limit"

        // Consulta cache primeiro (sem I/O de banco)
        val cached = cacheMutex.withLock { ftsCache[cacheKey] }
        if (cached != null) {
            android.util.Log.i("MestreIA_RAG", "┌─ FTS CACHE HIT: ${cached.size} chunks (query já processada)")
            return cached
        }

        android.util.Log.i("MestreIA_RAG", "┌─ FTS4 QUERY: $ftsQuery")
        val resultados = withContext(Dispatchers.IO) {
            manualChunkDao.buscarRegras(ftsQuery, limit).map { entity ->
                MestreIAChunk(
                    chunk_id = entity.chunk_id,
                    text = entity.text,
                    source_title = entity.source_title,
                    source_id = entity.source_id,
                    page_number = entity.page_number
                )
            }
        }

        if (resultados.isEmpty()) {
            android.util.Log.w("MestreIA_RAG", "└─ FTS4: NENHUM chunk encontrado — query muito específica ou termos ausentes no banco")
        } else {
            val paginas = resultados.mapNotNull { it.page_number }.distinct().sorted().joinToString()
            android.util.Log.i("MestreIA_RAG", "└─ FTS4: ${resultados.size} chunks | páginas: [$paginas]")
            cacheMutex.withLock { ftsCache[cacheKey] = resultados }
        }

        return resultados
    }

    /** Limpa o cache de FTS ao iniciar nova sessão de perguntas. */
    fun limparCacheFTS() {
        kotlinx.coroutines.runBlocking { cacheMutex.withLock { ftsCache.clear() } }
    }

    /**
     * Busca filtrada por fonte específica (ex: apenas Pyramid, apenas GunFu).
     * Útil quando o Planner identifica que a pergunta é de um suplemento específico.
     */
    suspend fun buscarNoCodexPorFonte(query: String, sourceId: String, limit: Int = 50): List<MestreIAChunk> {
        val ftsQuery = prepararQueryFTSAgressiva(query, emptyList())
        return manualChunkDao.buscarRegrasPorFonte(ftsQuery, sourceId, limit).map { entity ->
            MestreIAChunk(
                chunk_id = entity.chunk_id,
                text = entity.text,
                source_title = entity.source_title,
                source_id = entity.source_id,
                page_number = entity.page_number
            )
        }
    }

    /**
     * LOTE 126: Query FTS Otimizada.
     * Delegada para o MestreIAQueryEngine para testabilidade pura.
     */
    internal fun prepararQueryFTSAgressiva(userQuery: String, termosTecnicos: List<String>): String {
        return MestreIAQueryEngine.prepararQueryFTSAgressiva(userQuery, termosTecnicos)
    }

    suspend fun buscarPorPagina(pagina: Int): List<MestreIAChunk> {
        return manualChunkDao.buscarPorPagina(pagina).map { entity ->
            MestreIAChunk(
                chunk_id = entity.chunk_id,
                text = entity.text,
                source_title = entity.source_title,
                source_id = entity.source_id,
                page_number = entity.page_number
            )
        }
    }

    suspend fun buscarPorPaginaESource(pagina: Int, source: String): List<MestreIAChunk> {
        return manualChunkDao.buscarPorPaginaESource(pagina, source).map { entity ->
            MestreIAChunk(
                chunk_id = entity.chunk_id,
                text = entity.text,
                source_title = entity.source_title,
                source_id = entity.source_id,
                page_number = entity.page_number
            )
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Lote 325: NOVO MOTOR DE BUSCA POR PALAVRA-CHAVE (sem embedding)
    //
    // Substitui a busca semântica (HNSW) no fluxo do Auditor por um modelo
    // "grep + leitura dirigida": o modelo LOCALIZA páginas por palavras-chave
    // (AND, igual ao Google: mais palavras = menos resultados) e depois LÊ as
    // páginas que julgar relevantes. Determinístico: mesma query → mesmas páginas.
    // ──────────────────────────────────────────────────────────────────────

    data class LocalizarHit(
        val livro: String,       // source_title amigável
        val sourceId: String,
        val pagina: Int,
        val trecho: String       // snippet (~180 chars) onde o termo casou
    )

    data class LocalizarResultado(
        val total: Int,                 // total de páginas que casaram (antes do corte)
        val hits: List<LocalizarHit>,   // capado em `limit`
        val modo: String                // "AND" (estrito) ou "OR" (aproximado, fallback)
    )

    /** Mapeia nome amigável do livro → source_id do banco. null = livro desconhecido. */
    private fun mapearLivroParaSourceId(nome: String): String? = when (nome.lowercase().trim()) {
        "módulo básico", "modulo basico"       -> "pt_modulo_basico"
        "artes marciais"                        -> "pt_artes_marciais"
        "magia"                                 -> "pt_magia"
        "gun fu"                                -> "pt_gun_fu"
        "pyramid aquático", "pyramid aquatico" -> "pt_pyramid_26_underwater"
        else                                    -> null
    }

    /**
     * Tokeniza os termos da busca: normaliza, remove vazios/curtos.
     * NÃO expande sinônimos nem adiciona OR — controle do modelo (loop explícito).
     */
    private fun tokenizarTermos(termos: String): List<String> {
        return termos.split(Regex("\\s+"))
            .map { com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(it) }
            .filter { it.length >= 2 }
            .distinct()
    }

    /**
     * Lote 327: Ranking BM25 para o localizar (Opção A).
     * Extraído fielmente do scoring que JÁ existia em MestreIAGraphEngine.buscarDiretoNoCodex
     * (BM25 + cobertura de termos + proximidade + penalidade de índice) — esse pipeline foi
     * bypassado quando o Lote 325 criou o localizar como "grep cru + take(60)" sem ordenar.
     * Aqui o reusamos para que as páginas saiam ordenadas por RELEVÂNCIA, não por nº de página.
     *
     * Diferença vs GraphEngine: aqui é só lexical (sem reranking HNSW/cosseno) — o localizar
     * é a "página de resultados" por palavra-chave; a leitura semântica fica com o modelo.
     */
    private fun rankearPorBM25(
        chunks: List<com.gurps.ficha.data.storage.ManualChunkEntity>,
        tokens: List<String>
    ): List<Pair<com.gurps.ficha.data.storage.ManualChunkEntity, Double>> {
        if (chunks.isEmpty()) return emptyList()
        val k1 = 1.5
        val b = 0.75
        val N = corpusSizeCache.toDouble().coerceAtLeast(1.0)
        val avgdl = calcularAvgdlCorpus()

        // IDF: df calculado sobre o pool (aproximação controlada, evita varrer o corpus)
        val idfMap = tokens.associateWith { termo ->
            val df = chunks.count { it.text.contains(termo, ignoreCase = true) }.toDouble()
            kotlin.math.ln((N - df + 0.5) / (df + 0.5) + 1.0).coerceAtLeast(0.01)
        }

        return chunks.map { chunk ->
            val texto = chunk.text.lowercase()
            val dl = texto.length.toDouble()
            var score = 0.0

            // BM25 principal por termo
            for (termo in tokens) {
                val tf = texto.split(termo).size - 1
                if (tf == 0) continue
                val idf = idfMap[termo] ?: 0.01
                score += idf * (tf * (k1 + 1)) / (tf + k1 * (1 - b + b * dl / avgdl))
            }

            // Bônus de cobertura: quantos dos termos pedidos aparecem na página
            // (é a "ideia de quantos termos casaram" — pág. com 4/5 termos > pág. com 1/5)
            val presentes = tokens.count { texto.contains(it) }.toDouble()
            if (tokens.isNotEmpty() && presentes > 0.0) {
                score += 10.0 * (presentes / tokens.size.toDouble())
            }

            // Bônus de proximidade: pares de termos a < 100 chars de distância
            if (tokens.size >= 2) {
                for (i in tokens.indices) {
                    val pos1 = texto.indexOf(tokens[i]).takeIf { it >= 0 } ?: continue
                    for (j in (i + 1) until tokens.size) {
                        val pos2 = texto.indexOf(tokens[j]).takeIf { it >= 0 } ?: continue
                        if (kotlin.math.abs(pos1 - pos2) < 100) score += 5.0
                    }
                }
            }

            // Penalidade: páginas de índice/sumário do Módulo Básico (< 30)
            if ((chunk.page_number ?: 0) < 30 && chunk.source_id == "pt_modulo_basico") score -= 0.5

            chunk to score
        }.sortedByDescending { it.second }
    }

    /** Constrói o trecho (snippet): primeira linha do texto que contém algum token. */
    private fun construirTrecho(texto: String, tokens: List<String>): String {
        val linhas = texto.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        for (linha in linhas) {
            val norm = com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(linha)
            if (tokens.any { norm.contains(it) }) {
                return linha.take(180)
            }
        }
        return (linhas.firstOrNull() ?: texto.trim()).take(180)
    }

    /**
     * LOCALIZA páginas por palavras-chave (FTS4 AND). Igual ao Google: cada palavra
     * a mais ESTREITA o resultado. Retorna lista COMPACTA (livro|página|trecho) —
     * NÃO o texto completo. O modelo usa isto para decidir o que LER depois.
     *
     * @param termos string com palavras-chave separadas por espaço
     * @param livrosFiltro nomes amigáveis de livros para restringir; null/vazio = todos
     * @param limit máximo de hits retornados (o total real vem em LocalizarResultado.total)
     */
    suspend fun localizarNoCodex(
        termos: String,
        livrosFiltro: List<String>? = null,
        limit: Int = 60
    ): LocalizarResultado = withContext(Dispatchers.IO) {
        val tokens = tokenizarTermos(termos)
        if (tokens.isEmpty()) {
            android.util.Log.w("MestreIA_RAG", "║  LOCALIZAR: termos vazios após normalização ('$termos')")
            return@withContext LocalizarResultado(0, emptyList(), "AND")
        }

        val sourceIds: Set<String>? = livrosFiltro
            ?.mapNotNull { mapearLivroParaSourceId(it) }
            ?.toSet()
            ?.takeIf { it.isNotEmpty() }

        // FTS4 AND: tokens separados por espaço = AND implícito; "*" = prefixo.
        val queryAnd = tokens.joinToString(" ") { "$it*" }
        android.util.Log.i("MestreIA_RAG", "║  LOCALIZAR AND: \"$queryAnd\"${if (sourceIds != null) " livros=$sourceIds" else ""}")

        var modo = "AND"
        var brutos = manualChunkDao.buscarRegras(queryAnd, 500)

        // Fallback aproximado: se AND não casou nada e há 2+ tokens, tenta OR.
        if (brutos.isEmpty() && tokens.size > 1) {
            val queryOr = tokens.joinToString(" OR ") { "$it*" }
            android.util.Log.i("MestreIA_RAG", "║  LOCALIZAR fallback OR: \"$queryOr\"")
            brutos = manualChunkDao.buscarRegras(queryOr, 500)
            modo = "OR"
        }

        val filtrados = if (sourceIds != null) brutos.filter { it.source_id in sourceIds } else brutos
        val total = filtrados.size

        // Lote 327: RANKING BM25 antes do corte (Opção A — reusa o scoring que já existia).
        // ANTES: filtrados.take(60) devolvia as páginas em ordem de rowid (= nº de página),
        // então o fallback OR despejava "500 páginas" em ordem aleatória de relevância.
        // AGORA: ordena por relevância real (BM25 + cobertura de termos + proximidade) e
        // corta as melhores. Resolve o ruído do OR e queda de qualidade observados no log.
        val rankeados = rankearPorBM25(filtrados, tokens)
        val hits = rankeados.take(limit).map { (e, _) ->
            LocalizarHit(
                livro = e.source_title,
                sourceId = e.source_id ?: "",
                pagina = e.page_number ?: 0,
                trecho = construirTrecho(e.text, tokens)
            )
        }
        // Lote 326/327: loga QUAIS páginas (agora rankeadas) + o score das 5 primeiras —
        // permite distinguir "página certa nem apareceu" de "apareceu e o modelo ignorou",
        // e ver se o ranking colocou a página relevante no topo.
        val listaPags = hits.joinToString(", ") { h ->
            val sigla = h.sourceId.removePrefix("pt_").take(8)
            "$sigla:${h.pagina}"
        }
        val top5Score = rankeados.take(5).joinToString(" ") { (e, s) ->
            "${(e.source_id ?: "?").removePrefix("pt_").take(6)}:${e.page_number}=${String.format("%.1f", s)}"
        }
        android.util.Log.i("MestreIA_RAG", "║  LOCALIZAR [$modo]: $total páginas (retornando ${hits.size}) | top5: [$top5Score]")
        android.util.Log.i("MestreIA_RAG", "║  LOCALIZAR páginas: [$listaPags]")
        LocalizarResultado(total, hits, modo)
    }

    /**
     * LÊ o texto COMPLETO de uma página (ou intervalo) de um livro específico.
     * Equivalente a abrir o manual na página. Intervalo limitado para não inflar o contexto.
     */
    suspend fun lerPaginas(
        livro: String,
        paginaInicial: Int,
        paginaFinal: Int? = null
    ): List<MestreIAChunk> = withContext(Dispatchers.IO) {
        val sourceId = mapearLivroParaSourceId(livro)
        if (sourceId == null) {
            android.util.Log.w("MestreIA_RAG", "║  LER: livro desconhecido '$livro'")
            return@withContext emptyList()
        }
        val pFim = (paginaFinal ?: paginaInicial).coerceIn(paginaInicial, paginaInicial + 3) // máx 4 páginas
        val resultado = mutableListOf<MestreIAChunk>()
        for (pag in paginaInicial..pFim) {
            resultado.addAll(buscarPorPaginaESource(pag, sourceId))
        }
        android.util.Log.i("MestreIA_RAG", "║  LER $livro p$paginaInicial${if (pFim != paginaInicial) "-$pFim" else ""}: ${resultado.size} chunks")
        resultado
    }

    suspend fun getChunkById(id: String): MestreIAChunk? {
        return manualChunkDao.getChunkById(id)?.let { entity ->
            MestreIAChunk(
                chunk_id = entity.chunk_id,
                text = entity.text,
                source_title = entity.source_title,
                source_id = entity.source_id,
                page_number = entity.page_number
            )
        }
    }

    suspend fun forçarSincronizacaoManual() {
        manualChunkDao.clearAll()
        FichaDatabase.prePopulateManual(context, database)
    }

    /** Retorna o número total de chunks no corpus (para IDF global correto no BM25). */
    suspend fun contarTotalChunks(): Int = withContext(Dispatchers.IO) {
        manualChunkDao.getCount()
    }

    /**
     * Retorna avgdl estimado do corpus completo.
     * Valor calibrado empiricamente com os chunks GURPS (~900 chars/chunk).
     * Evita query extra ao banco mantendo a infraestrutura imutável.
     */
    fun calcularAvgdlCorpus(): Double = 900.0
}
