package com.gurps.ficha.domain

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.*

/**
 * Motor de busca RAG direto nos chunks.jsonl via FTS SQLite.
 *
 * Lote 270-C:
 * - avgdl e IDF globais (pré-computados do corpus, não do pool de candidatos)
 * - Bonus AND proporcional à cobertura de termos de núcleo (não flat +15)
 * - take(30) com Pocket RAG: chunks ★★ e ★ comprimidos a sentenças relevantes
 * - Expansão bidirecional removida de extrairPalavrasChave
 */
class MestreIAGraphEngine(private val repository: DataRepository) {

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
        termosPonderados: List<MestreIAPlanner.TermoPonderado> = emptyList()
    ): GraphSearchResult {
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
        android.util.Log.i("MestreIA_RAG", "  Núcleo: $termosNucleo")

        // 1. Pool FTS4 — 200 candidatos pré-filtrados por keyword match
        val chunksFTS: List<MestreIAChunk> =
            repository.buscarNoCodexDireto(query, termosBase, limit = 500)

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
            // HNSW ANN: top-50 por semântica pura, ~1-5ms
            val hnswIds = MestreIAVectorEngine.buscarTopK(query, topK = 50)
            if (hnswIds.isNotEmpty()) {
                // Mapeia chunk_ids HNSW para objetos do pool BM25 (texto já carregado)
                val chunksPorId = chunksCandidatos.associateBy { it.chunk_id }
                val hnswChunks = hnswIds.mapNotNull { chunksPorId[it] }
                // Chunks do HNSW recebem bonus de score: posição HNSW vira boost semântico
                val hnswBonus = hnswIds.mapIndexed { idx, id ->
                    id to (50.0 - idx) / 50.0 * 10.0  // top-1 = +10pts, top-50 = +0.2pts
                }.toMap()
                val hnswIds2 = hnswIds.toSet()
                // Re-pontua: BM25 + bonus HNSW para chunks no resultado HNSW
                val repontua = chunksCandidatos.map { chunk ->
                    val bm25 = bm25ScoresMap[chunk.chunk_id] ?: 0.0
                    val bonus = hnswBonus[chunk.chunk_id] ?: 0.0
                    chunk to (bm25 + bonus)
                }.sortedByDescending { it.second }
                android.util.Log.i("MestreIA_RAG", "  HNSW top-5: ${hnswIds.take(5).joinToString()}")
                chunksPontuadosFinais = repontua
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
        val chunksTopicIndex = emptyList<MestreIAChunk>()
        val fontesRepresentadas = chunksDiversos.map { it.source_id }.toSet()
        val fontesNoPool = chunksCandidatos.map { it.source_id }.distinct()
        val scoresMapMutavel = chunksPontuadosFinais.associate { it.first.chunk_id to it.second }.toMutableMap()
        for (fonte in fontesNoPool) {
            if (fonte !in fontesRepresentadas) {
                val melhorDaFonte = chunksPontuadosFinais.firstOrNull { it.first.source_id == fonte }
                if (melhorDaFonte != null) {
                    chunksDiversos.add(melhorDaFonte.first)
                    // Eleva score para garantir texto completo (não comprimido pelo Pocket RAG)
                    scoresMapMutavel[melhorDaFonte.first.chunk_id] = 9.0
                    android.util.Log.i("MestreIA_RAG",
                        "  Fonte garantida: $fonte → p.${melhorDaFonte.first.page_number}")
                }
            }
        }

        // 4. Expansão por proximidade de página
        val chunksBase = chunksDiversos.take(50)
        val chunksGarantidos = chunksDiversos.drop(50)
        val chunksFinais = mutableSetOf<MestreIAChunk>()
        chunksFinais.addAll(chunksTopicIndex)

        for (chunk in (chunksGarantidos + chunksBase)) {
            chunksFinais.add(chunk)
            val pagina = chunk.page_number
            val fonte = chunk.source_id
            if (pagina != null && fonte != null) {
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

        return GraphSearchResult(
            summaries = emptyList(),
            relatedChunks = chunksFinal,
            chunkScores = scoresMapMutavel
        )
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
