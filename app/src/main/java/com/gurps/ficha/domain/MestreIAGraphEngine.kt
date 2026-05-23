package com.gurps.ficha.domain

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.*

/**
 * Motor de busca RAG direto nos chunks.jsonl via FTS SQLite.
 */
class MestreIAGraphEngine(private val repository: DataRepository) {

    init {
        MestreIATopicIndex.carregar(repository.context)
    }

    data class GraphSearchResult(
        val summaries: List<String> = emptyList(),
        val relatedChunks: List<MestreIAChunk>,
        val chunkScores: Map<String, Double> = emptyMap()
    )

    /**
     * LOTE 119: BUSCA DIRETA (Pula o Grafo).
     * Focada 100% em extrair o texto bruto dos manuais usando o novo motor FTS Agressivo.
     */
    suspend fun buscarDiretoNoCodex(query: String, termosExtras: List<String> = emptyList(), perguntaOriginal: String = ""): GraphSearchResult {
        val termosBase: List<String> = (extrairPalavrasChave(query, apenasOriginais = true) + termosExtras).distinct()
        val termosExpandidos: List<String> = (extrairPalavrasChave(query, apenasOriginais = false) + termosExtras).distinct()
        
        android.util.Log.i("MestreIA_RAG", "══ RAG BUSCA: \"${query.take(80)}\"")

        // 1. Busca FTS4 — pool de 200 candidatos pré-filtrados por keyword match
        // FTS4 já ordena por relevância keyword; 200 candidatos é suficiente para BM25 e escala bem com 5000+ chunks
        val chunksCandidatos: List<MestreIAChunk> = repository.buscarNoCodexDireto(query, termosBase, limit = 200)

        if (chunksCandidatos.isEmpty()) {
            android.util.Log.e("MestreIA_RAG", "✖ RAG FALHOU: nenhum chunk para \"${query.take(60)}\" — IA não terá contexto do manual!")
            return GraphSearchResult(emptyList(), emptyList())
        }

        // 2. BM25-Kotlin: scoring por relevância real (TF-IDF com saturação de frequência)
        // BM25(d,q) = Σ IDF(qi) × tf(qi,d)×(k1+1) / (tf(qi,d) + k1×(1−b+b×|d|/avgdl))
        val k1 = 1.5   // saturação: penaliza repetição excessiva do mesmo termo
        val b  = 0.75  // normalização por comprimento do documento
        val N  = chunksCandidatos.size.toDouble()
        val avgdl = chunksCandidatos.map { it.text.length }.average().coerceAtLeast(1.0)

        // IDF por termo: log((N − df + 0.5) / (df + 0.5))
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

            // Componente BM25 principal
            for (termo in termosBase) {
                val termLower = termo.lowercase()
                val tf = texto.split(termLower).size - 1  // contagem de ocorrências
                if (tf == 0) continue
                val idf = idfMap[termLower] ?: 0.01
                score += idf * (tf * (k1 + 1)) / (tf + k1 * (1 - b + b * dl / avgdl))
            }

            // Bonus: termos expandidos (sinônimos) — peso reduzido vs. termos base
            for (termoEx in (termosExpandidos - termosBase.toSet())) {
                if (texto.contains(termoEx.lowercase())) score += 0.3
            }

            // Bonus AND: chunk contém TODOS os termos base (altamente relevante)
            if (termosBase.size >= 2 && termosBase.all { texto.contains(it.lowercase()) }) {
                score += 15.0
            }

            // Bonus Proximidade: pares de termos base < 100 chars de distância
            if (termosBase.size >= 2) {
                for (i in termosBase.indices) {
                    val t1 = termosBase[i].lowercase()
                    val pos1 = texto.indexOf(t1).takeIf { it >= 0 } ?: continue
                    for (j in (i + 1) until termosBase.size) {
                        val t2 = termosBase[j].lowercase()
                        val pos2 = texto.indexOf(t2).takeIf { it >= 0 } ?: continue
                        if (kotlin.math.abs(pos1 - pos2) < 100) score += 5.0
                    }
                }
            }

            // Penalidade: páginas de índice/sumário (primeiras 30 do módulo básico)
            if ((chunk.page_number ?: 0) < 30 && chunk.source_id == "pt_modulo_basico") score -= 0.5

            chunksPontuados.add(Pair(chunk, score))
        }

        chunksPontuados.sortByDescending { it.second }

        val top5Log = chunksPontuados.take(5).joinToString(" | ") { "p.${it.first.page_number}(${it.second.toInt()}pts)" }
        android.util.Log.i("MestreIA_RAG", "  Scoring BM25 top-5: $top5Log")

        // 2b. Lote 259: Reranking semântico (cosseno) sobre os top-50 do BM25.
        // Se vec_chunks estiver vazio (embeddings não gerados), retorna BM25 puro sem erro.
        val bm25ScoresMap = chunksPontuados.associate { it.first.chunk_id to it.second }
        val top50BM25 = chunksPontuados.take(50).map { it.first }
        val top50Reranqueado = MestreIASemanticEngine.reranquear(
            query = query,
            candidatos = top50BM25,
            bm25Scores = bm25ScoresMap,
            vecDao = repository.vecChunkDao
        )
        // Reconstrói a lista ordenada: reranqueados primeiro, demais chunks do BM25 ao final
        val top50Ids = top50Reranqueado.map { it.chunk_id }.toSet()
        val restantes = chunksPontuados.drop(50).map { it.first }.filter { it.chunk_id !in top50Ids }
        val chunksPontuadosFinais = (top50Reranqueado + restantes).map { c -> c to (bm25ScoresMap[c.chunk_id] ?: 0.0) }

        // 3. Diversificação por Página (usa lista pós-reranking semântico)
        val contadorPaginas = mutableMapOf<String, Int>()
        val chunksDiversos: MutableList<MestreIAChunk> = mutableListOf()

        for (pair in chunksPontuadosFinais) {
            if (chunksDiversos.size >= 15) break
            val c = pair.first
            val pageKey = "${c.source_id}_${c.page_number}"
            val total = contadorPaginas.getOrDefault(pageKey, 0)
            if (total < 3) {
                contadorPaginas[pageKey] = total + 1
                chunksDiversos.add(c)
            }
        }

        // 3b. Garantia por Índice de Tópicos (topic_index.json)
        // Usa a pergunta original (curta) para evitar falsos matches na query expandida com 40+ termos
        val queryParaTopicIndex = perguntaOriginal.ifBlank { query }
        val paginasTopicIndex = MestreIATopicIndex.resolverPaginasGarantidas(queryParaTopicIndex)
        val chunksTopicIndex = mutableListOf<MestreIAChunk>()
        for (garantia in paginasTopicIndex) {
            for (page in garantia.pages) {
                val chunks = repository.buscarPorPaginaESource(page, garantia.sourceId)
                if (chunks.isNotEmpty()) {
                    chunksTopicIndex.addAll(chunks)
                    android.util.Log.i("MestreIA_RAG", "  TopicIndex garantido: ${garantia.sourceId} p.$page → ${chunks.size} chunks")
                }
            }
        }

        // 3c. Garantia de Diversidade por Fonte (pool FTS)
        val fontesRepresentadas = (chunksDiversos + chunksTopicIndex).map { it.source_id }.toSet()
        val fontesNoPool = chunksCandidatos.map { it.source_id }.distinct()
        for (fonte in fontesNoPool) {
            if (fonte !in fontesRepresentadas) {
                val melhorDaFonte = chunksPontuadosFinais.firstOrNull { it.first.source_id == fonte }
                if (melhorDaFonte != null) {
                    chunksDiversos.add(melhorDaFonte.first)
                    android.util.Log.i("MestreIA_RAG", "  Fonte garantida: ${fonte} → p.${melhorDaFonte.first.page_number}(${melhorDaFonte.second.toInt()}pts)")
                }
            }
        }

        // 4. Expansão por Proximidade
        // Prioridade: TopicIndex > chunks garantidos de fonte > chunks por ranking BM25
        val chunksBase = chunksDiversos.take(15)
        val chunksGarantidos = chunksDiversos.drop(15)
        val chunksFinais = mutableSetOf<MestreIAChunk>()

        // TopicIndex tem prioridade absoluta — entram primeiro, nunca cortados
        chunksFinais.addAll(chunksTopicIndex)

        for (chunk in (chunksGarantidos + chunksBase)) {
            chunksFinais.add(chunk)
            val pagina = chunk.page_number
            val fonte = chunk.source_id
            if (pagina != null && fonte != null) {
                val pOriginal = repository.buscarPorPaginaESource(pagina, fonte)
                chunksFinais.addAll(pOriginal)

                if (pOriginal.size < 3 || (pOriginal.isNotEmpty() && pOriginal.last().text.trim().endsWith(","))) {
                    chunksFinais.addAll(repository.buscarPorPaginaESource(pagina + 1, fonte))
                }
            }
        }

        val chunksFinal = chunksFinais.toList().distinctBy { it.chunk_id }.take(30)
        val paginasFinais = chunksFinal.mapNotNull { it.page_number }.distinct().sorted().joinToString()
        android.util.Log.i("MestreIA_RAG", "  Contexto final: ${chunksFinal.size} chunks | páginas: [$paginasFinais]")

        val scoresMap = chunksPontuadosFinais.associate { it.first.chunk_id to it.second }
        return GraphSearchResult(
            summaries = emptyList(),
            relatedChunks = chunksFinal,
            chunkScores = scoresMap
        )
    }

    private fun extrairPalavrasChave(texto: String, apenasOriginais: Boolean = false, termosExtras: List<String> = emptyList()): List<String> {
        val stopWords = listOf(
            "como", "funciona", "regra", "regras", "quais", "preciso", "para", "sobre", "qual",
            "uma", "um", "com", "dos", "das", "pela", "pelo", "onde", "quando", "quem", "sÃ£o", "sao",
            "gurps", "edicao", "ediÃ§Ã£o", "calculo", "calcular", "lista", "tabela", "me", "de", "da", "do",
            "nos", "nas", "aos", "para", "pelo", "pela", "traga", "atender", "pra", "me", "fale",
            "meu", "meus", "minha", "minhas", "seu", "seus", "sua", "suas", "esta", "estÃ¡", "estou", "estamos",
            "queria", "quero", "saber", "ajuda", "ajude", "tem", "temos", "existe", "existem", "por", "sobre",
            "fale", "diga", "explique", "mostre", "entÃ£o", "entao", "vocÃª", "voce", "estÃ¡", "isso", "esse", "essa",
            "que", "acontece", "acontecer", "aconteceu", "estiver", "estivesse", "num", "numa", "nuns", "numas",
            "pelas", "pelos", "ser", "seja", "seria", "sido", "ter", "tinha", "teria", "tenha", "houver", "houvesse",
            "algum", "alguma", "alguns", "algumas", "pode", "podem", "poderia", "poderiam", "fazer", "faz", "feito",
            "pÃ¡gina", "pagina", "pag", "paginas", "pÃ¡g", "pÃ¡ginas", "veja", "vide", "consulte", "ver", "estÃ¡", "estao",
            "entende", "entendendo", "entendi", "explicar", "explique", "fale", "diga", "mostre", "ajuda", "ajude", "quero", "queria",
            "nÃ£o", "nao", "que", "pra", "pro", "com", "uma", "uns", "pelo", "pela", "pelas", "pelos", "mais", "muito",
            "cada", "deve", "devem", "esta", "estao", "estÃ£o", "seja", "sejam"
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

        val dicionarioTecnico = mapOf(
            "sangramento" to listOf("hemorragia", "ferimento", "saude", "perda"),
            "hemorragia" to listOf("sangramento"),
            "pular" to listOf("salto", "distancia", "altura"),
            "impacto" to listOf("colisao", "batida", "queda", "atropelamento"),
            "colisao" to listOf("impacto", "queda", "atropelamento"),
            "queda" to listOf("impacto", "colisao"),
            "asfixia" to listOf("afogamento", "sufocamento", "respiracao", "folego", "ar"),
            "afogamento" to listOf("asfixia", "sufocamento", "agua", "submerso", "piscina"),
            "st" to listOf("forca", "levantamento", "carga", "dano", "gdp", "geb"),
            "forca" to listOf("st", "levantamento", "carga"),
            "dx" to listOf("destreza", "agilidade", "coordenacao"),
            "iq" to listOf("inteligencia", "vontade", "percepcao"),
            "ht" to listOf("vitalidade", "saude", "fadiga", "pf", "sobrevivencia"),
            "velocidade" to listOf("deslocamento", "esquiva", "movimento", "m/s", "km/h"),
            "submerso" to listOf("agua", "aquatico", "mergulho", "piscina", "mar", "subaquatico"),
            "aquatico" to listOf("submerso", "agua", "subaquatico"),
            "piscina" to listOf("agua", "submerso", "aquatico", "subaquatico", "mergulho"),
            "agua" to listOf("submerso", "aquatico", "subaquatico", "piscina", "mar"),
            "redutor" to listOf("penalidade", "modificador", "subtracao", "decremento")
        )
        palavrasOriginais.forEach { p ->
            dicionarioTecnico[p]?.let { expandidas.addAll(it) }
            // Busca parcial APENAS para termos tÃ©cnicos longos (>4 letras) para evitar ruÃ­do (ex: 'ar')
            if (p.length > 4) {
                dicionarioTecnico.entries.forEach { entry ->
                    if (p.contains(entry.key) || entry.key.contains(p)) {
                        expandidas.addAll(entry.value)
                    }
                }
            }
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
     * Formata os resultados RAG para o prompt da IA.
     * Pocket RAG: chunks ★★★ enviados completos; ★★ e ★ comprimidos por sentenças relevantes.
     * Reduz contexto de ~35KB para ~12-18KB sem perder informação crítica.
     */
    suspend fun formatarParaIA(resultado: GraphSearchResult, query: String = ""): String {
        val s = StringBuilder()

        if (resultado.relatedChunks.isNotEmpty()) {
            s.append("\n=== REGRAS DO CODEX (PAGINAS DO MANUAL) ===\n")
            s.append("INSTRUÇÃO: Chunks marcados [★★★] têm maior relevância para a pergunta. Priorize-os na análise.\n")

            // Extrai termos de busca do query para guiar a compressão de sentenças
            val termosQuery = query.split(Regex("\\s+"))
                .map { it.lowercase().trim() }
                .filter { it.length >= 3 }

            val porFonte = resultado.relatedChunks.groupBy { it.source_id ?: "desconhecido" }
            porFonte.forEach { (_, chunks) ->
                val tituloFonte = chunks.first().source_title ?: "Manual"
                s.append("\n--- FONTE: $tituloFonte ---\n")
                chunks.forEach { chunk ->
                    val score = resultado.chunkScores[chunk.chunk_id] ?: 0.0
                    val relevancia = when {
                        score >= 8.0  -> "[★★★]"
                        score >= 2.0  -> "[★★]"
                        else          -> "[★]"
                    }
                    // Chunks de alta relevância: texto completo (IA precisa de todo o contexto)
                    // Chunks de baixa relevância: comprimidos às sentenças que contêm os termos
                    val textoFinal = when {
                        score >= 8.0 -> chunk.text
                        termosQuery.isNotEmpty() -> comprimirChunk(chunk.text, termosQuery)
                        else -> chunk.text.take(600)  // fallback: limita sem comprimir
                    }
                    s.append("[Pág. ${chunk.page_number}]$relevancia: $textoFinal\n")
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
     * Pocket RAG — extrai sentenças relevantes de um chunk.
     * Mantém sentenças que contêm pelo menos um termo de busca + 1 sentença de contexto vizinha.
     * Chunks sem nenhuma sentença relevante retornam as primeiras 2 sentenças (contexto mínimo).
     */
    private fun comprimirChunk(texto: String, termos: List<String>, maxSentencas: Int = 4): String {
        val sentencas = texto.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        if (sentencas.size <= 2) return texto  // chunk pequeno: não comprimir

        val indicesRelevantes = mutableSetOf<Int>()
        sentencas.forEachIndexed { i, sentenca ->
            val sent = sentenca.lowercase()
            if (termos.any { sent.contains(it) }) {
                indicesRelevantes.add(i)
                // inclui sentença vizinha seguinte como contexto (regras frequentemente continuam na próxima frase)
                if (i + 1 < sentencas.size) indicesRelevantes.add(i + 1)
            }
        }

        if (indicesRelevantes.isEmpty()) {
            // Nenhuma sentença contém os termos — retorna início do chunk como contexto
            return sentencas.take(2).joinToString(" ")
        }

        return indicesRelevantes.sorted().take(maxSentencas)
            .joinToString(" ") { sentencas[it] }
    }

    /**
     * LOTE 92: Busca profunda em arquivos JSON normalizados (assets).
     * Vasculha Armas, PerÃ­cias, Vantagens e Armaduras para dados exatos.
     */
    private fun buscarTabelasTecnicas(pistas: List<String>): String {
        val s = StringBuilder()
        val pistasNorm = pistas.map { it.lowercase().trim() }.filter { it.length > 3 }
        if (pistasNorm.isEmpty()) return ""

        // 1. Buscar Armas
        val armasEncontradas = repository.armasCatalogo.filter { arma ->
            pistasNorm.any { pista -> arma.nome.lowercase().contains(pista) }
        }.take(5)
        
        armasEncontradas.forEach { arma ->
            s.append("\n[Arma: ${arma.nome}]\n")
            s.append("- Tipo: ${arma.tipoCombate} (${arma.categoria})\n")
            s.append("- Dano: ${arma.danoRaw} | ST: ${arma.stMinimo ?: "N/A"} | Aparar: ${arma.aparar ?: "N/A"}\n")
            s.append("- Alcance: ${arma.observacoes.take(50)} | Peso: ${arma.pesoBaseKg}kg | Custo: $${arma.custoBase}\n")
        }

        // 2. Buscar PerÃ­cias
        val periciasEncontradas = repository.pericias.filter { per ->
            pistasNorm.any { pista -> per.nome.lowercase().contains(pista) }
        }.take(5)

        periciasEncontradas.forEach { per ->
            s.append("\n[PerÃ­cia: ${per.nome}]\n")
            s.append("- Atributo: ${per.atributoBase} | Dificuldade: ${per.dificuldadeFixa ?: "Variavel"}\n")
            if (per.exigeEspecializacao) s.append("- Obs: Exige EspecializaÃ§Ã£o.\n")
        }

        // 3. Buscar Vantagens/Desvantagens
        val vantagensEncontradas = repository.vantagens.filter { v ->
            pistasNorm.any { pista -> v.nome.lowercase().contains(pista) }
        }.take(5)

        vantagensEncontradas.forEach { v ->
            s.append("\n[Vantagem: ${v.nome}]\n")
            s.append("- Custo: ${v.custo} | Tipo: ${v.tipoCusto} | PÃ¡g: ${v.pagina}\n")
        }

        // 4. Buscar Armaduras e Escudos
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