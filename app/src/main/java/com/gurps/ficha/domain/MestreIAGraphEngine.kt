package com.gurps.ficha.domain

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.MestreIAQueryEngine
import com.gurps.ficha.model.*

/**
 * ⚠️ USADO APENAS PELA VOZ (GeminiLive) E FORJADOR — NÃO REMOVER.
 *
 * Motor de busca RAG SEMÂNTICO (BM25 + HNSW + reranking) direto nos chunks via FTS SQLite.
 *
 * Lote 270-C:
 * - avgdl e IDF globais (pré-computados do corpus, não do pool de candidatos)
 * - Bonus AND proporcional à cobertura de termos de núcleo (não flat +15)
 * - take(30) com Pocket RAG: chunks ★★ e ★ comprimidos a sentenças relevantes
 * - Expansão bidirecional removida de extrairPalavrasChave
 *
 * ⚠️ NÃO usado pelo AUDITOR desde o Lote 325 (que migrou para "grep + leitura dirigida"
 * em MestreIARepository.localizarNoCodex/lerPaginas). Caller ativo: a VOZ
 * (GeminiLiveTools.consultarManual → buscarDiretoNoCodex). O caller morto
 * MestreIAUseCase.gerarCatalogoDireto foi removido no Lote 349.
 * O scoring BM25 daqui foi COPIADO para MestreIARepository.rankearPorBM25 (Lote 327) —
 * se for ajustar o ranking do AUDITOR, mexa LÁ, não aqui. Ver ARQUITETURA_MESTRE_IA.md §5.1.
 */
class MestreIAGraphEngine(private val repository: DataRepository) {

    companion object {
        // FLAG DE TESTE: true = HNSW puro (sem BM25), false = BM25 + HNSW (padrão)
        var MODO_HNSW_PURO: Boolean = false
    }

    init {
        // TopicIndex removido (Lote 272): índice agora injetado no prompt do AUDITOR
    }

    data class GraphSearchResult(
        val summaries: List<String> = emptyList(),
        val relatedChunks: List<MestreIAChunk>,
        val chunkScores: Map<String, Double> = emptyMap()
    )

    // Corpus global: N e avgdl calculados na primeira busca e reutilizados.
    // corpusSize: resolúvel apenas em suspend — inicializado na primeira chamada.
    // corpusAvgdl: valor calibrado, não precisa de suspend.
    @Volatile private var corpusSize: Int = 0
    private val corpusAvgdl: Double = repository.calcularAvgdlCorpus().coerceAtLeast(1.0)

    /**
     * Busca direta no Codex. Contrato público preservado (callers: GeminiLiveTools).
     */
    suspend fun buscarDiretoNoCodex(
        query: String,
        termosExtras: List<String> = emptyList(),
        perguntaOriginal: String = "",
        termosPonderados: List<MestreIAQueryEngine.TermoPonderado> = emptyList(),
        filtroLivro: String? = null,
        filtroLivros: List<String>? = null
    ): GraphSearchResult {
        // Lote 324: filtroLivros (multi) tem precedência sobre filtroLivro (single).
        // Backward compat: callers antigos seguem usando filtroLivro: String? sem mudança.
        fun mapearLivro(nome: String): String? = when (nome.lowercase().trim()) {
            "módulo básico", "modulo basico"       -> "pt_modulo_basico"
            "artes marciais"                        -> "pt_artes_marciais"
            "magia"                                 -> "pt_magia"
            "gun fu"                                -> "pt_gun_fu"
            "pyramid aquático", "pyramid aquatico" -> "pt_pyramid_26_underwater"
            else                                    -> null
        }
        val sourceIdsFiltro: Set<String>? = when {
            !filtroLivros.isNullOrEmpty() -> filtroLivros.mapNotNull { mapearLivro(it) }.toSet().takeIf { it.isNotEmpty() }
            filtroLivro != null            -> mapearLivro(filtroLivro)?.let { setOf(it) }
            else                           -> null
        }
        val termosBase: List<String> =
            (extrairPalavrasChave(query, apenasOriginais = true) + termosExtras).distinct()
        val termosExpandidos: List<String> =
            (extrairPalavrasChave(query, apenasOriginais = false) + termosExtras).distinct()

        // Separa termos por peso: núcleo (>=1.0) vs contexto (<1.0)
        val termosNucleo = if (termosPonderados.isNotEmpty()) {
            termosPonderados.filter { it.peso >= 1.0 }.map { it.termo.lowercase() }
        } else {
            termosBase.map { it.lowercase() }
        }

        // Inicializa corpus size na primeira busca (suspend, não pode ser em lazy)
        if (corpusSize == 0) corpusSize = repository.contarTotalChunks().coerceAtLeast(1)

        android.util.Log.i("MestreIA_RAG", "══ RAG BUSCA: \"${query.take(80)}\"")
        val livroLogStr: String = when {
            sourceIdsFiltro == null     -> ""
            sourceIdsFiltro.size == 1   -> " | livro=${sourceIdsFiltro.first()}"
            else                        -> " | livros=${sourceIdsFiltro.joinToString(",")}"
        }
        android.util.Log.i("MestreIA_RAG", "  Núcleo: $termosNucleo | modo=${if (MODO_HNSW_PURO) "HNSW_PURO" else "BM25+HNSW"}$livroLogStr")

        // MODO HNSW PURO: ignora BM25, usa só o ranking semântico do HNSW
        // Lote 315: topK reduzido de 50→15 — modelo "se afogava" em chunks demais
        // Lote 322: filtro de livro vira HÍBRIDO — se filtrar deixar <5 chunks,
        // complementa com top-N globais. Resolve o caso onde tools especializadas
        // (consultarRegrasArmasFogo, etc.) retornavam só 1-2 chunks porque os melhores
        // estavam em outros livros (ex: regra de pólvora molhada está em MB pág.408,
        // não em Gun Fu — e o filtro estrito rejeitava esse chunk).
        if (MODO_HNSW_PURO && MestreIAVectorEngine.isReady()) {
            // Lote 322: topK aumentado de 15→30 quando há filtro de livro,
            // pra ter margem após o corte. Sem filtro mantém 15.
            // Lote 324: idem para filtro multi-livro.
            val topKBuscado = if (sourceIdsFiltro != null) 30 else 15
            val hnswIds = MestreIAVectorEngine.buscarTopK(query, topK = topKBuscado)
            if (hnswIds.isNotEmpty()) {
                val chunksTodos = hnswIds.mapNotNull { id -> repository.getChunkById(id) }
                    .filter { !isChunkCorrompido(it.text) }

                val chunksFiltrados = if (sourceIdsFiltro != null) {
                    chunksTodos.filter { it.source_id in sourceIdsFiltro }
                } else {
                    chunksTodos
                }

                // Lote 322 — Fallback complementar:
                // Se filtro rígido deixou <5 chunks, complementa com top globais
                // (chunks de outros livros que NÃO estavam no resultado filtrado).
                // Preserva a intenção da tool especializada (livros escolhidos vêm primeiro)
                // mas garante variedade quando os livros são pequenos ou o tópico cruza livros.
                val chunksFinais = if (sourceIdsFiltro != null && chunksFiltrados.size < 5) {
                    val idsJaIncluidos = chunksFiltrados.map { it.chunk_id }.toSet()
                    val complementares = chunksTodos
                        .filter { it.chunk_id !in idsJaIncluidos }
                        .take(5)
                    android.util.Log.i("MestreIA_RAG",
                        "  Filtro $sourceIdsFiltro deixou ${chunksFiltrados.size} chunks — complementando com ${complementares.size} de outros livros")
                    chunksFiltrados + complementares
                } else {
                    chunksFiltrados
                }

                val scoresMap = hnswIds.mapIndexed { idx, id -> id to (topKBuscado - idx).toDouble() }.toMap()
                android.util.Log.i("MestreIA_RAG", "  HNSW PURO: ${chunksFinais.size} chunks | top-5: ${hnswIds.take(5).joinToString()}")
                return GraphSearchResult(
                    summaries = emptyList(),
                    relatedChunks = chunksFinais.take(15),
                    chunkScores = scoresMap
                )
            }
        }

        // 1. Pool FTS4 — candidatos pré-filtrados por keyword match, sem chunks corrompidos
        val chunksFTS: List<MestreIAChunk> =
            repository.buscarNoCodexDireto(query, termosBase, limit = 500)
                .filter { chunk -> !isChunkCorrompido(chunk.text) }
                .filter { chunk -> sourceIdsFiltro == null || chunk.source_id in sourceIdsFiltro }

        // 1b. Injeção direta por página: se a query menciona um número de página,
        // busca esses chunks diretamente e injeta no pool — evita que páginas relevantes
        // fiquem fora dos top-200 do FTS quando o corpo do texto usa termos diferentes do título.
        val paginasMencionadas = Regex("\\b(\\d{1,3})\\b").findAll(query)
            .map { it.value.toInt() }
            .filter { it in 1..600 }
            .distinct()
        val chunksInjetados = mutableListOf<MestreIAChunk>()
        for (pag in paginasMencionadas) {
            val chunks = repository.buscarPorPagina(pag)
            chunksInjetados.addAll(chunks)
            if (chunks.isNotEmpty()) {
                // Também busca páginas adjacentes (seções frequentemente continuam)
                chunksInjetados.addAll(repository.buscarPorPagina(pag + 1))
                chunksInjetados.addAll(repository.buscarPorPagina(pag + 2))
            }
        }

        val chunksCandidatos: List<MestreIAChunk> = if (chunksInjetados.isNotEmpty()) {
            val idsExistentes = chunksFTS.map { it.chunk_id }.toSet()
            val novos = chunksInjetados.filter { it.chunk_id !in idsExistentes }
            android.util.Log.i("MestreIA_RAG", "  Injeção por página: +${novos.size} chunks de p.${paginasMencionadas.joinToString()}")
            chunksFTS + novos
        } else {
            chunksFTS
        }

        if (chunksCandidatos.isEmpty()) {
            android.util.Log.e("MestreIA_RAG",
                "✖ RAG FALHOU: nenhum chunk para \"${query.take(60)}\" — IA não terá contexto do manual!")
            return GraphSearchResult(emptyList(), emptyList())
        }

        // 2. BM25 com corpus global para N e avgdl corretos
        val k1 = 1.5
        val b  = 0.75
        val N  = corpusSize.toDouble().coerceAtLeast(1.0)
        val avgdl = corpusAvgdl

        // IDF global: usa N do corpus completo (não do pool de 200)
        // df é calculado sobre o pool — aproximação controlada, evita query ao corpus inteiro
        val idfMap = termosBase.associate { termo ->
            val df = chunksCandidatos.count { it.text.contains(termo, ignoreCase = true) }.toDouble()
            val idf = kotlin.math.ln((N - df + 0.5) / (df + 0.5) + 1.0).coerceAtLeast(0.01)
            termo.lowercase() to idf
        }

        val chunksPontuados: MutableList<Pair<MestreIAChunk, Double>> = mutableListOf()
        for (chunk in chunksCandidatos) {
            val texto = chunk.text.lowercase()
            val dl = texto.length.toDouble()
            var score = 0.0

            // Componente BM25 principal — aplica peso do termo no IDF
            for (termo in termosBase) {
                val termLower = termo.lowercase()
                val tf = texto.split(termLower).size - 1
                if (tf == 0) continue
                val idf = idfMap[termLower] ?: 0.01
                // Termos de núcleo têm IDF amplificado pelo peso; contexto reduzido
                val pesoTermo = termosPonderados.firstOrNull { it.termo.lowercase() == termLower }?.peso ?: 1.0
                val idfPonderado = idf * pesoTermo
                score += idfPonderado * (tf * (k1 + 1)) / (tf + k1 * (1 - b + b * dl / avgdl))
            }

            // Bonus para termos expandidos (sinônimos) — peso fixo reduzido
            for (termoEx in (termosExpandidos - termosBase.toSet())) {
                if (texto.contains(termoEx.lowercase())) score += 0.3
            }

            // Bonus proporcional ao núcleo: 10.0 × (termos_nucleo_presentes / total_nucleo)
            // Substitui o flat +15.0 que amplificava contexto igualmente ao núcleo
            if (termosNucleo.isNotEmpty()) {
                val nucleoPresente = termosNucleo.count { texto.contains(it) }.toDouble()
                val coberturaNucleo = nucleoPresente / termosNucleo.size.toDouble()
                if (coberturaNucleo > 0.0) {
                    score += 10.0 * coberturaNucleo
                }
            }

            // Bonus de proximidade: pares de termos de NÚCLEO < 100 chars de distância
            if (termosNucleo.size >= 2) {
                for (i in termosNucleo.indices) {
                    val t1 = termosNucleo[i]
                    val pos1 = texto.indexOf(t1).takeIf { it >= 0 } ?: continue
                    for (j in (i + 1) until termosNucleo.size) {
                        val t2 = termosNucleo[j]
                        val pos2 = texto.indexOf(t2).takeIf { it >= 0 } ?: continue
                        if (kotlin.math.abs(pos1 - pos2) < 100) score += 5.0
                    }
                }
            }

            // Penalidade: páginas de índice/sumário do módulo básico
            if ((chunk.page_number ?: 0) < 30 && chunk.source_id == "pt_modulo_basico") score -= 0.5

            // Boost para chunks injetados por página: garante que entrem no pool de reranking
            val injetado = chunksInjetados.any { it.chunk_id == chunk.chunk_id }
            if (injetado && score < 8.0) score = 8.0

            chunksPontuados.add(Pair(chunk, score))
        }

        chunksPontuados.sortByDescending { it.second }

        val top5Log = chunksPontuados.take(5).joinToString(" | ") {
            "p.${it.first.page_number}(${String.format("%.1f", it.second)}pts)"
        }
        android.util.Log.i("MestreIA_RAG", "  Scoring BM25 top-5: $top5Log")

        // 2b. Reranking semântico via HNSW (ObjectBox) ou fallback cosseno brute-force
        val bm25ScoresMap = chunksPontuados.associate { it.first.chunk_id to it.second }
        val chunksPontuadosFinais: List<Pair<MestreIAChunk, Double>>

        if (MestreIAVectorEngine.isReady()) {
            // HNSW ANN: top-15 por semântica pura, ~1-5ms
            // Lote 315: topK reduzido de 50→15 — modelo "se afogava" em chunks demais
            val hnswIds = MestreIAVectorEngine.buscarTopK(query, topK = 15)
            if (hnswIds.isNotEmpty()) {
                val chunksPorId = chunksCandidatos.associateBy { it.chunk_id }
                val hnswRank = hnswIds.mapIndexed { idx, id -> id to idx }.toMap()

                // Top-5 HNSW recebem score 9.0 fixo — independente do BM25.
                // Isso garante que o chunk semanticamente mais relevante esteja no topo,
                // mesmo que o BM25 o tenha pontuado mal por razões lexicais (ex: chunk
                // começa com outro subtítulo e "Ataque Furacão" aparece depois no texto).
                val top5HnswIds = hnswIds.take(5).toSet()
                val top5HnswChunks = top5HnswIds.mapNotNull { id ->
                    (chunksPorId[id] ?: repository.getChunkById(id))
                        ?.takeIf { !isChunkCorrompido(it.text) }
                }

                val repontua = chunksCandidatos.map { chunk ->
                    val bm25 = bm25ScoresMap[chunk.chunk_id] ?: 0.0
                    val rank = hnswRank[chunk.chunk_id]
                    val scoreHnsw = if (chunk.chunk_id in top5HnswIds) 9.0
                                    else if (rank != null) bm25 + (15.0 - rank) / 15.0 * 3.0
                                    else bm25
                    chunk to scoreHnsw
                }.sortedByDescending { it.second }

                // Chunks HNSW top-5 que não estão no pool FTS4: adiciona como extras com 9.0
                val repontuaIds = repontua.map { it.first.chunk_id }.toSet()
                val top5Extras = top5HnswChunks.filter { it.chunk_id !in repontuaIds }
                    .map { chunk -> chunk to 9.0 }

                android.util.Log.i("MestreIA_RAG", "  HNSW top-5: ${hnswIds.take(5).joinToString()} | extras fora do pool=${top5Extras.size}")
                chunksPontuadosFinais = (repontua + top5Extras).sortedByDescending { it.second }
            } else {
                chunksPontuadosFinais = chunksPontuados
            }
        } else {
            // Fallback: reranking cosseno brute-force (quando HNSW ainda não populado)
            val top50BM25 = chunksPontuados.take(50).map { it.first }
            val top50Reranqueado = MestreIASemanticEngine.reranquear(
                query = query,
                candidatos = top50BM25,
                bm25Scores = bm25ScoresMap,
                vecDao = repository.vecChunkDao
            )
            val top50Ids = top50Reranqueado.map { it.chunk_id }.toSet()
            val restantes = chunksPontuados.drop(50).map { it.first }.filter { it.chunk_id !in top50Ids }
            chunksPontuadosFinais =
                (top50Reranqueado + restantes).map { c -> c to (bm25ScoresMap[c.chunk_id] ?: 0.0) }
        }

        // 3. Diversificação por página (max 3 chunks por página/fonte)
        val contadorPaginas = mutableMapOf<String, Int>()
        val chunksDiversos: MutableList<MestreIAChunk> = mutableListOf()
        for (pair in chunksPontuadosFinais) {
            if (chunksDiversos.size >= 50) break
            val c = pair.first
            val pageKey = "${c.source_id}_${c.page_number}"
            val total = contadorPaginas.getOrDefault(pageKey, 0)
            if (total < 3) {
                contadorPaginas[pageKey] = total + 1
                chunksDiversos.add(c)
            }
        }

        // 3b. Garantia de diversidade por fonte
        // Só injeta fontes cujo melhor chunk tem score BM25 > 1.0 — evita fontes espúrias
        // (ex: pyramid_26_underwater com score ~0.3) dominarem o topo do contexto.
        val chunksTopicIndex = emptyList<MestreIAChunk>()
        val fontesRepresentadas = chunksDiversos.map { it.source_id }.toSet()
        val fontesNoPool = chunksCandidatos.map { it.source_id }.distinct()
        val scoresMapMutavel = chunksPontuadosFinais.associate { it.first.chunk_id to it.second }.toMutableMap()
        for (fonte in fontesNoPool) {
            if (fonte !in fontesRepresentadas) {
                val melhorDaFonte = chunksPontuadosFinais.firstOrNull { it.first.source_id == fonte }
                if (melhorDaFonte != null && melhorDaFonte.second > 1.0) {
                    chunksDiversos.add(melhorDaFonte.first)
                    scoresMapMutavel[melhorDaFonte.first.chunk_id] = melhorDaFonte.second
                    android.util.Log.i("MestreIA_RAG",
                        "  Fonte garantida: $fonte → p.${melhorDaFonte.first.page_number} (score=${String.format("%.1f", melhorDaFonte.second)})")
                } else if (melhorDaFonte != null) {
                    android.util.Log.d("MestreIA_RAG",
                        "  Fonte ignorada (score baixo): $fonte score=${String.format("%.1f", melhorDaFonte.second)}")
                }
            }
        }

        // 4. Expansão por proximidade de página
        // Só expande chunks com score real (> 1.0) — evita que fontes garantidas artificialmente
        // se multipliquem ocupando slots com score 0 (ex: pyramid_26_underwater p.7 × 4 vezes).
        val chunksBase = chunksDiversos.take(50)
        val chunksGarantidos = chunksDiversos.drop(50)
        val chunksFinais = mutableSetOf<MestreIAChunk>()
        chunksFinais.addAll(chunksTopicIndex)

        for (chunk in (chunksGarantidos + chunksBase)) {
            chunksFinais.add(chunk)
            val pagina = chunk.page_number
            val fonte = chunk.source_id
            val scoreChunk = scoresMapMutavel[chunk.chunk_id] ?: 0.0
            if (pagina != null && fonte != null && scoreChunk > 1.0) {
                val pOriginal = repository.buscarPorPaginaESource(pagina, fonte)
                chunksFinais.addAll(pOriginal)
                if (pOriginal.size < 3 ||
                    (pOriginal.isNotEmpty() && pOriginal.last().text.trim().endsWith(","))) {
                    chunksFinais.addAll(repository.buscarPorPaginaESource(pagina + 1, fonte))
                }
            }
        }

        val chunksFinal = chunksFinais.toList().distinctBy { it.chunk_id }.take(50)
        val paginasFinais = chunksFinal.mapNotNull { it.page_number }.distinct().sorted().joinToString()
        android.util.Log.i("MestreIA_RAG",
            "  Contexto final: ${chunksFinal.size} chunks | páginas: [$paginasFinais]")

        // Log top-10 com score e posição para diagnóstico
        val top10Log = chunksFinal.take(10).mapIndexed { idx, chunk ->
            val score = scoresMapMutavel[chunk.chunk_id] ?: 0.0
            "#${idx+1} ${(chunk.source_id ?: "?").removePrefix("pt_")}_p${chunk.page_number}(${String.format("%.1f", score)}pts)"
        }.joinToString(" | ")
        android.util.Log.i("MestreIA_RAG", "  Ranking top-10: $top10Log")

        return GraphSearchResult(
            summaries = emptyList(),
            relatedChunks = chunksFinal,
            chunkScores = scoresMapMutavel
        )
    }

    private fun isChunkCorrompido(text: String): Boolean {
        val linhas = text.trim().split('\n')
        if (linhas.size < 5) return false
        val vazias = linhas.count { it.trim().replace("|", "").replace(" ", "").isEmpty() }
        return vazias.toDouble() / linhas.size > 0.5
    }

    private fun extrairPalavrasChave(
        texto: String,
        apenasOriginais: Boolean = false,
        termosExtras: List<String> = emptyList()
    ): List<String> {
        val stopWords = listOf(
            "como", "funciona", "regra", "regras", "quais", "preciso", "para", "sobre", "qual",
            "uma", "um", "com", "dos", "das", "pela", "pelo", "onde", "quando", "quem", "sao",
            "gurps", "edicao", "calculo", "calcular", "lista", "tabela", "me", "de", "da", "do",
            "nos", "nas", "aos", "pra", "fale", "meu", "meus", "minha", "minhas",
            "seu", "seus", "sua", "suas", "esta", "estou", "que", "isso", "esse", "essa",
            "queria", "quero", "saber", "ajuda", "tem", "existe", "existem", "por",
            "diga", "explique", "mostre", "entao", "voce", "isso", "esse", "essa",
            "acontece", "num", "numa", "ser", "seja", "pode", "fazer",
            "nao", "mais", "muito", "cada", "deve", "seja", "sejam",
            "pagina", "pag", "paginas", "veja", "vide", "consulte", "ver"
        )
        val siglasGurps = listOf("st", "dx", "iq", "ht", "gdp", "geb", "pv", "pf", "rd", "nh")

        val palavrasOriginais = com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(texto)
            .split(" ")
            .filter {
                val isNumber = it.all { c -> c.isDigit() } && it.length in 1..4
                (it.length >= 2 || it in siglasGurps || isNumber) && it !in stopWords
            }

        if (apenasOriginais) return palavrasOriginais

        val expandidas = mutableSetOf<String>()
        expandidas.addAll(palavrasOriginais)

        // Expansão SOMENTE direta (chave → valores): sem matching bidirecional
        val dicionarioTecnico = mapOf(
            "sangramento" to listOf("hemorragia", "ferimento", "saude", "perda"),
            "hemorragia"  to listOf("sangramento"),
            "pular"       to listOf("salto", "distancia", "altura"),
            "impacto"     to listOf("colisao", "batida", "queda", "atropelamento"),
            "colisao"     to listOf("impacto", "queda", "atropelamento"),
            "queda"       to listOf("impacto", "colisao"),
            "asfixia"     to listOf("afogamento", "sufocamento", "respiracao", "folego", "ar"),
            "afogamento"  to listOf("asfixia", "sufocamento", "agua", "submerso", "piscina"),
            "st"          to listOf("forca", "levantamento", "carga", "dano", "gdp", "geb"),
            "forca"       to listOf("st", "levantamento", "carga"),
            "dx"          to listOf("destreza", "agilidade", "coordenacao"),
            "iq"          to listOf("inteligencia", "vontade", "percepcao"),
            "ht"          to listOf("vitalidade", "saude", "fadiga", "pf", "sobrevivencia"),
            "velocidade"  to listOf("deslocamento", "esquiva", "movimento"),
            "submerso"    to listOf("agua", "aquatico", "mergulho", "piscina", "mar", "subaquatico"),
            "aquatico"    to listOf("submerso", "agua", "subaquatico"),
            "piscina"     to listOf("agua", "submerso", "aquatico", "subaquatico", "mergulho"),
            "agua"        to listOf("submerso", "aquatico", "subaquatico", "piscina", "mar"),
            "redutor"     to listOf("penalidade", "modificador", "subtracao")
        )

        palavrasOriginais.forEach { p ->
            dicionarioTecnico[p]?.let { expandidas.addAll(it) }
        }

        repository.temasMestreIA.forEach { tema ->
            val match = palavrasOriginais.any { it in tema.keywords || it == tema.canonical || it == tema.id }
            if (match) {
                expandidas.add(tema.canonical)
                expandidas.addAll(tema.keywords)
            }
        }

        return expandidas.toList()
    }

    /**
     * Formata resultados RAG para o prompt da IA com Pocket RAG.
     *
     * Ordem de saída: ★★★ (score >= 8.0) primeiro, depois ★★ e ★.
     * TopicIndex (score=999) sempre aparecem no início — nunca comprimidos e nunca cortados.
     *
     * ★★★ (score >= 8.0): texto completo
     * ★★  (score >= 2.0): comprimido a sentenças relevantes
     * ★   (score < 2.0) : comprimido, ou omitido se vazio após compressão
     */
    suspend fun formatarParaIA(resultado: GraphSearchResult, query: String = ""): String {
        val s = StringBuilder()

        if (resultado.relatedChunks.isNotEmpty()) {
            s.append("\n=== REGRAS DO CODEX (PAGINAS DO MANUAL) ===\n")
            s.append("INSTRUÇÃO: Chunks marcados [★★★] têm maior relevância para a pergunta. Priorize-os na análise.\n")

            val termosQuery = query.split(Regex("\\s+"))
                .map { it.lowercase().trim() }
                .filter { it.length >= 3 }

            // Ordena todos os chunks por score decrescente antes de agrupar por fonte.
            // Garante que TopicIndex (score=999) e chunks de alta relevância apareçam primeiro,
            // antes do limite de chars ser atingido.
            val chunksOrdenados = resultado.relatedChunks.sortedByDescending {
                resultado.chunkScores[it.chunk_id] ?: 0.0
            }

            val porFonte = chunksOrdenados.groupBy { it.source_id ?: "desconhecido" }
            // Ordena fontes pelo melhor score de cada uma (fonte com chunks mais relevantes vem primeiro)
            val fontesOrdenadas = porFonte.entries.sortedByDescending { (_, chunks) ->
                chunks.maxOfOrNull { resultado.chunkScores[it.chunk_id] ?: 0.0 } ?: 0.0
            }

            fontesOrdenadas.forEach { (_, chunks) ->
                val tituloFonte = chunks.first().source_title ?: "Manual"
                s.append("\n--- FONTE: $tituloFonte ---\n")
                chunks.forEach { chunk ->
                    val score = resultado.chunkScores[chunk.chunk_id] ?: 0.0
                    val relevancia = when {
                        score >= 8.0 -> "[★★★]"
                        score >= 2.0 -> "[★★]"
                        else         -> "[★]"
                    }

                    // TopicIndex (score=999) e ★★★ nunca comprimidos
                    val textoFinal = when {
                        score >= 8.0 -> chunk.text
                        else -> comprimirChunkPorSentencas(chunk.text, termosQuery)
                    }

                    if (textoFinal.isNotBlank()) {
                        s.append("[Pág. ${chunk.page_number}]$relevancia: $textoFinal\n")
                    }
                }
            }
        }

        val termosDoContexto = resultado.relatedChunks
            .flatMap { it.text.split(" ") }
            .filter { it.length > 3 }
            .map { it.lowercase().trim() }
            .distinct()
            .take(20)

        val tecnicos = buscarTabelasTecnicas(termosDoContexto)
        if (tecnicos.isNotBlank()) {
            s.append("\n=== ESTATISTICAS TECNICAS (TABELAS OFICIAIS) ===\n")
            s.append(tecnicos)
        }

        return s.toString()
    }

    /**
     * Pocket RAG: mantém somente sentenças que contêm ao menos um termo da query.
     * Preserva tabelas (linhas com |) e frases com números (fórmulas, valores).
     */
    private fun comprimirChunkPorSentencas(texto: String, termosQuery: List<String>): String {
        if (termosQuery.isEmpty()) return texto

        val sentencas = texto.split(Regex("(?<=[.!?])\\s+|\\n"))
        val relevantes = sentencas.filter { sentenca ->
            val s = sentenca.lowercase()
            val temTermo = termosQuery.any { s.contains(it) }
            val temTabela = sentenca.contains("|") || sentenca.trim().matches(Regex(".*\\d+.*"))
            temTermo || temTabela
        }

        return if (relevantes.isEmpty()) "" else relevantes.joinToString(" ").trim()
    }

    private fun buscarTabelasTecnicas(pistas: List<String>): String {
        val s = StringBuilder()
        val pistasNorm = pistas.map { it.lowercase().trim() }.filter { it.length > 3 }
        if (pistasNorm.isEmpty()) return ""

        val armasEncontradas = repository.armasCatalogo.filter { arma ->
            pistasNorm.any { pista -> arma.nome.lowercase().contains(pista) }
        }.take(5)
        armasEncontradas.forEach { arma ->
            s.append("\n[Arma: ${arma.nome}]\n")
            s.append("- Tipo: ${arma.tipoCombate} (${arma.categoria})\n")
            s.append("- Dano: ${arma.danoRaw} | ST: ${arma.stMinimo ?: "N/A"} | Aparar: ${arma.aparar ?: "N/A"}\n")
            s.append("- Alcance: ${arma.observacoes.take(50)} | Peso: ${arma.pesoBaseKg}kg | Custo: $${arma.custoBase}\n")
        }

        val periciasEncontradas = repository.pericias.filter { per ->
            pistasNorm.any { pista -> per.nome.lowercase().contains(pista) }
        }.take(5)
        periciasEncontradas.forEach { per ->
            s.append("\n[Perícia: ${per.nome}]\n")
            s.append("- Atributo: ${per.atributoBase} | Dificuldade: ${per.dificuldadeFixa ?: "Variavel"}\n")
            if (per.exigeEspecializacao) s.append("- Obs: Exige Especialização.\n")
        }

        val vantagensEncontradas = repository.vantagens.filter { v ->
            pistasNorm.any { pista -> v.nome.lowercase().contains(pista) }
        }.take(5)
        vantagensEncontradas.forEach { v ->
            s.append("\n[Vantagem: ${v.nome}]\n")
            s.append("- Custo: ${v.custo} | Tipo: ${v.tipoCusto} | Pág: ${v.pagina}\n")
        }

        val armadurasEncontradas = repository.armadurasCatalogo.filter { arm ->
            pistasNorm.any { pista -> arm.nome.lowercase().contains(pista) }
        }.take(3)
        armadurasEncontradas.forEach { arm ->
            s.append("\n[Armadura: ${arm.nome}]\n")
            s.append("- RD: ${arm.rd} | Local: ${arm.local} | Peso: ${arm.pesoBaseKg}kg\n")
        }

        return s.toString()
    }
}
