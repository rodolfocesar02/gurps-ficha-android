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

    suspend fun buscarNoGrafo(query: String, offset: Int = 0, termosExtras: List<String> = emptyList()): GraphSearchResult {
        // LOTE 91: Busca Profunda com Re-Ranking (Inspirado em Nano-GraphRAG Local Query)
        // LOTE 97: Separação de Termos (Original vs Expandido) para evitar poluição
        val termosBase = (extrairPalavrasChave(query, apenasOriginais = true) + termosExtras).distinct()
        val termosExpandidos = (extrairPalavrasChave(query, apenasOriginais = false) + termosExtras).distinct()
        val todosOsTermos = (termosBase + termosExpandidos).distinct()
        
        android.util.Log.i("MestreIA_Auditoria", "TERMOS ORIGINAIS: $termosBase")
        android.util.Log.i("MestreIA_Auditoria", "TERMOS EXPANDIDOS: ${termosExpandidos - termosBase.toSet()}")
        
        if (todosOsTermos.isEmpty()) return GraphSearchResult(emptyList(), emptyList())

        val topNodes = mutableSetOf<GraphNodeEntity>()
        val chunksCandidatos = mutableListOf<MestreIAChunk>()
        android.util.Log.i("MestreIA_Auditoria", "TERMOS DE BUSCA (Total): $todosOsTermos")        
        // 1. Navegação de Grafo (Seed + Neighbors)
        android.util.Log.i("MestreIA_Auditoria", "--- INICIANDO BUSCA NO CÓDEX (Origem: graph_knowledge.json) ---")
        
        val allNodesFound = mutableListOf<GraphNodeEntity>()
        todosOsTermos.forEach { termo ->
            val encontrados = repository.buscarNodesPorTitulo(termo).toMutableList()
            val matchesNoResumo = repository.buscarResumosGrafo("$termo*")
            encontrados.addAll(matchesNoResumo.filter { res -> encontrados.none { it.entityId == res.entityId } })
            allNodesFound.addAll(encontrados)
        }

        // LOTE 117: Priorização e Re-Ranking
        // Regras e Perícias sempre sobem, Equipamentos descem.
        val prioritizedNodes = allNodesFound.distinctBy { it.entityId }
            .sortedWith(compareByDescending<GraphNodeEntity> { 
                when (it.category) {
                    "Regra", "Perícia", "Técnica" -> 3
                    "Vantagem", "Desvantagem" -> 2
                    else -> 1
                }
            }.thenBy { it.level })
            .take(15) // Expandido de 5 para 15 para evitar silenciamento

        topNodes.addAll(prioritizedNodes)
        
        // 2. Extração de Páginas Alvo do Grafo
        data class PaginaAlvo(val numero: Int, val tituloLivro: String?, val sourceId: String?, val isOriginal: Boolean)
        val paginasAlvo = mutableSetOf<PaginaAlvo>()

        prioritizedNodes.forEach { node ->
            val titleNorm = node.title.lowercase()
            
            // Determinamos se o nó é original baseado nos termos base
            val realmenteOriginal = termosBase.any { termo -> 
                titleNorm.contains(termo.lowercase()) || (termo.length > 5)
            }

            // Extração de páginas do resumo do nó (Ex: [Módulo Básico Pág. 100])
            Regex("""\[([^\]]*?)\s+P[ágǭg\uFFFD\s.]+(\d+)\]""", RegexOption.IGNORE_CASE).findAll(node.summary).forEach { m ->
                val livro = m.groupValues[1].trim()
                val pag = m.groupValues[2].toIntOrNull()
                if (pag != null) paginasAlvo.add(PaginaAlvo(pag, livro, node.source_id, realmenteOriginal))
            }
            
            // Fallback para citações simples de páginas (Ex: Pág. 45)
            Regex("""P[ágǭg\uFFFD\s.]+(\d+)""", RegexOption.IGNORE_CASE).findAll(node.summary).forEach { m ->
                m.groupValues[1].toIntOrNull()?.let { paginasAlvo.add(PaginaAlvo(it, null, node.source_id, realmenteOriginal)) }
            }
        }
        
        android.util.Log.i("MestreIA_Auditoria", "NÓS ATIVADOS (Grafo): ${topNodes.map { it.title }}")

        // Log das páginas encontradas
        android.util.Log.i("MestreIA_Auditoria", "PÁGINAS ALVO: ${paginasAlvo.map { "${it.numero} (Orig=${it.isOriginal})" }}")

        // LOTE 102: Layering de Busca com TF-IDF Proxy (Inteligência Real)
        val termoWeights = mutableMapOf<String, Int>()
        
        // Fazemos buscas independentes para cada termo e calculamos sua raridade no livro.
        termosBase.forEach { termo ->
            val termoNorm = com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(termo)
            if (termoNorm.length >= 3) { 
                val encontradas = repository.buscarRecortesManual("$termoNorm*", 50)
                chunksCandidatos.addAll(encontradas)
                
                // CÁLCULO DE RARIDADE: Se achou 50 recortes (bateu no limite), é palavra comum (peso baixo).
                // Se achou 2 recortes, é palavra raríssima e central para a dúvida (peso alto).
                val ocorrencias = encontradas.size.coerceAtLeast(1)
                val pesoRaridade = (50 / ocorrencias).coerceIn(1, 50)
                termoWeights[termoNorm] = pesoRaridade
            }
        }
        
        // LOTE 104: Redução de Ruído. 
        // Termos expandidos (sinônimos) agora servem APENAS para encontrar o Nó do Grafo inicial.
        // A busca de texto bruto (Chunks) deve ser cirúrgica nos termos ORIGINAIS do usuário.
        // Se o usuário falou 'piscina', não queremos recortes de 'gladiador'.
        /* if (termosExpandidos.isNotEmpty()) {
            val queryExpandida = termosExpandidos.take(15).joinToString(" OR ") { "$it*" }
            chunksCandidatos.addAll(repository.buscarRecortesManual(queryExpandida, 30))
        } */
        
        // Ponte de Página: Força a carga das páginas reais indicadas no Grafo com filtro de fonte
        paginasAlvo.forEach { alvo ->
            val pag = alvo.numero
            val livro = alvo.tituloLivro
            val sourceId = alvo.sourceId
            
            if (sourceId != null) {
                android.util.Log.d("MestreIA_Auditoria", "PONTE DE PÁGINA: Forçando carga da Pág. $pag ($sourceId) indicada pelo Grafo.")
                chunksCandidatos.addAll(repository.buscarPorPaginaESource(pag, sourceId))
            } else if (livro != null) {
                android.util.Log.d("MestreIA_Auditoria", "PONTE DE PÁGINA: Forçando carga da Pág. $pag ($livro) indicada pelo Grafo.")
                chunksCandidatos.addAll(repository.buscarPorPagina(pag))
            } else {
                android.util.Log.d("MestreIA_Auditoria", "PONTE DE PÁGINA: Forçando carga da Pág. $pag indicada pelo Grafo.")
                chunksCandidatos.addAll(repository.buscarPorPagina(pag))
            }
        }

        // 3. RE-RANKING PROFUNDO COM PESO DE RARIDADE
        val chunksPontuados = chunksCandidatos.map { chunk ->
            var score = 0
            val texto = chunk.text.lowercase()
            var rareMatchBonus = 0
            
            // Prioridade baseada na raridade da palavra (TF-IDF Proxy)
            termosBase.forEach { termo ->
                val termLower = com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(termo)
                val pesoRaridade = termoWeights[termLower] ?: 1
                
                if (texto.contains(termLower)) {
                    score += (100 * pesoRaridade) // Palavra rara dá pontuação massiva!
                    rareMatchBonus += pesoRaridade
                } 
            }
            
            // Co-ocorrência agora multiplica o bônus de raridade (palavras raras juntas explodem o score)
            if (rareMatchBonus > 1) {
                score += (rareMatchBonus * 50)
            }
            
            (termosExpandidos - termosBase.toSet()).forEach { if (texto.contains(it.lowercase())) score += 10 }
            
            // LOTE 101: Grafo agora é secundário (Peso reduzido para 5)
            topNodes.forEach { 
                if (texto.contains(it.title.lowercase())) {
                    score += 5
                    if (termosBase.any { base -> base.equals(it.title, true) }) score += 20
                    // LOTE 112: Prioridade para Nós Mestres (Índices Gerais)
                    if (it.level == 0) score += 40
                }
            }

            // Bônus por categoria (Peso 5)
            if (topNodes.any { it.category.lowercase() in texto }) score += 5
            
            // LOTE 111: BÔNUS CRÍTICO DE PÁGINA RECOMENDADA PELO GRAFO (Prioridade Absoluta)
            val alvoCorrespondente = paginasAlvo.find { alvo -> 
                alvo.numero == chunk.page_number && 
                (alvo.sourceId == chunk.source_id || 
                 (alvo.sourceId == null && (alvo.tituloLivro == null || chunk.source_title.contains(alvo.tituloLivro, true))))
            }
            if (alvoCorrespondente != null) {
                // Se a página veio de um termo ORIGINAL, bônus máximo. Se veio de SINÔNIMO, bônus menor.
                score += if (alvoCorrespondente.isOriginal) 1000 else 150
            }

            // LOTE 112: HIERARQUIA DE AUTORIDADE (Módulo Básico é a lei mãe)
            if (chunk.source_id == "pt_modulo_basico") {
                score += 50
            }
            
            // LOTE 112.3: BÔNUS DE AUTORIDADE PARA MAGIA E ARTES MARCIAIS
            val isMagiaQuery = termosBase.any { it.matches(Regex("magia|magias|feitiço|feitiços|grimório|encantamento|praga|maldição|desejo")) } || topNodes.any { it.category.lowercase() == "magia" }
            if (isMagiaQuery && chunk.source_id == "pt_gurps_magia") {
                score += 60 // Ultrapassa Módulo Básico em questões exclusivas de Magia!
            }
            val isArtesMarciaisQuery = termosBase.any { it.matches(Regex("artes marciais|técnica|golpe|chute|soco|estilo|katá")) } || topNodes.any { it.category.lowercase() == "artes marciais" || it.category.lowercase() == "manobra" }
            if (isArtesMarciaisQuery && chunk.source_id == "pt_artes_marciais") {
                score += 60 
            }

            // NOVO: Bônus de "Regra Geral" (Tabelas e Cálculos Técnicos)
            val termosRegraGeral = listOf("tabela", "ritmo", "m³", "calculo", "bc", "base de carga", "m3", "m/h")
            if (termosRegraGeral.any { texto.contains(it) }) {
                score += 60
            }
            
            // Penalidade para páginas muito baixas (Índice/Introdução)
            if ((chunk.page_number ?: 0) < 30) score -= 10
            
            chunk to score
        }.sortedByDescending { it.second }
        
        // LOTE 103: RRF (Reciprocal Rank Fusion) - O Casamento Perfeito (Grafo + Semântica)
        // Mapeia o ranking de cada página segundo o Grafo de Conhecimento
        val paginasGrafoRanking = paginasAlvo.mapIndexed { index, alvo -> alvo to (index + 1) }.toMap()

        // Aplica a fórmula matemática RRF para fundir as duas listas
        val chunksRRF = chunksPontuados.mapIndexed { textIndex, (chunk, lexicalScore) ->
            val textRank = textIndex + 1
            val textRrfScore = 1.0 / (textRank + 60)
            
            val inGraph = paginasAlvo.any { alvo ->
                alvo.numero == chunk.page_number && 
                (alvo.sourceId == chunk.source_id || 
                 (alvo.sourceId == null && (alvo.tituloLivro == null || chunk.source_title.contains(alvo.tituloLivro, true))))
            }
            
            // LOTE 112.2: Todas as páginas do Grafo são IGUALMENTE importantes. 
            // Não devemos penalizar a Pág 388 só porque ela apareceu depois da Pág 353 na lista.
            val graphRank = if (inGraph) 1 else 1000 
            val graphRrfScore = 1.0 / (graphRank + 60)
            
            // LOTE 105: Bônus de Autoridade do Grafo (Multiplicador de 5x para páginas sugeridas pelo Grafo)
            val graphMultiplier = if (inGraph) 5.0 else 1.0
            
            val finalRrfScore = textRrfScore + (graphRrfScore * graphMultiplier)
            chunk to finalRrfScore
        }.sortedByDescending { it.second }
        
        // LOTE 120: Otimização de Performance - Map para busca rápida de score no sorting final
        val chunksRRFMap = chunksRRF.associate { it.first.chunk_id to it.second }
        
        // LOTE 105: Filtro de Diversidade (Anti-Monopólio)
        // Impede que uma única página (ex: Pág 16) ocupe todas as vagas do contexto.
        // Permitimos no máximo 2 recortes da mesma página no Top 8.
        val contadorPaginas = mutableMapOf<String, Int>()
        val chunksDiversos = chunksRRF.filter { (chunk, _) ->
            val pageKey = "${chunk.source_id}_${chunk.page_number ?: -1}"
            val total = contadorPaginas.getOrDefault(pageKey, 0)
            if (total < 2) {
                contadorPaginas[pageKey] = total + 1
                true
            } else {
                false
            }
        }

        // LOTE 111: Priorização de Páginas Recomendadas (Garante que a 433 entre no Top 8)
        val chunksRecomendados = chunksDiversos.filter { (chunk, _) ->
            paginasAlvo.any { alvo ->
                alvo.numero == chunk.page_number && 
                (alvo.sourceId == chunk.source_id || 
                 (alvo.sourceId == null && (alvo.tituloLivro == null || chunk.source_title.contains(alvo.tituloLivro, true))))
            }
        }
        val outrosChunks = chunksDiversos.filter { (chunk, _) ->
            !paginasAlvo.any { alvo ->
                alvo.numero == chunk.page_number && 
                (alvo.sourceId == chunk.source_id || 
                 (alvo.sourceId == null && (alvo.tituloLivro == null || chunk.source_title.contains(alvo.tituloLivro, true))))
            }
        }
        
        val chunksPaginados = (chunksRecomendados + outrosChunks).take(8) // Expandido para 8 recortes para maior cobertura
        
        chunksPaginados.forEach { (chunk, score) ->
            android.util.Log.i("MestreIA_Auditoria", "TOP CHUNK: Pág ${chunk.page_number} | Score: $score")
        }

        // 4. PARENT DOCUMENT RETRIEVAL CIRÚRGICO (Lote 123)
        val chunksFinais = mutableSetOf<MestreIAChunk>()
        chunksPaginados.forEach { (chunk, _) ->
            val pagina = chunk.page_number
            val fonte = chunk.source_id // Usar ID para precisão total
            
            if (pagina != null && fonte != null) {
                // 1. Página de Impacto (N)
                val paginaImpacto = repository.buscarPorPaginaESource(pagina, fonte)
                chunksFinais.addAll(paginaImpacto)
                
                // 2. Verificação de Continuidade (Pág N+1)
                // Se a página atual termina de forma que sugira continuidade (vírgula, dois pontos, ou tabela), trazemos a próxima.
                val textoFim = paginaImpacto.lastOrNull()?.text?.trim()?.lowercase() ?: ""
                val continua = textoFim.endsWith(",") || textoFim.endsWith(":") || 
                               textoFim.contains("tabela") || textoFim.contains("continua") || textoFim.contains("vide")
                
                if (continua) {
                    val paginaSeguinte = repository.buscarPorPaginaESource(pagina + 1, fonte)
                    chunksFinais.addAll(paginaSeguinte)
                    android.util.Log.d("MestreIA_Auditoria", "CONTINUIDADE: Detectada na Pág $pagina, anexando Pág ${pagina+1}")
                }
            } else {
                chunksFinais.add(chunk)
            }
        }

        return GraphSearchResult(
            summaries = topNodes.toList().sortedByDescending { it.level }.take(5),
            relatedChunks = chunksFinais.toList().distinctBy { it.chunk_id }.take(25) // Limite de 25 recortes (Aprox 15k a 20k tokens)
        )
    }

    private fun extrairPalavrasChave(texto: String, apenasOriginais: Boolean = false, termosExtras: List<String> = emptyList()): List<String> {
        val stopWords = listOf(
            "como", "funciona", "regra", "regras", "quais", "preciso", "para", "sobre", "qual",
            "uma", "um", "com", "dos", "das", "pela", "pelo", "onde", "quando", "quem", "são", "sao",
            "gurps", "edicao", "edição", "calculo", "calcular", "lista", "tabela", "me", "de", "da", "do",
            "nos", "nas", "aos", "para", "pelo", "pela", "traga", "atender", "pra", "me", "fale",
            "meu", "meus", "minha", "minhas", "seu", "seus", "sua", "suas", "esta", "está", "estou", "estamos",
            "queria", "quero", "saber", "ajuda", "ajude", "tem", "temos", "existe", "existem", "por", "sobre",
            "fale", "diga", "explique", "mostre", "então", "entao", "você", "voce", "está", "isso", "esse", "essa",
            "que", "acontece", "acontecer", "aconteceu", "estiver", "estivesse", "num", "numa", "nuns", "numas",
            "pelas", "pelos", "ser", "seja", "seria", "sido", "ter", "tinha", "teria", "tenha", "houver", "houvesse",
            "algum", "alguma", "alguns", "algumas", "pode", "podem", "poderia", "poderiam", "fazer", "faz", "feito",
            "página", "pagina", "pag", "paginas", "pág", "páginas", "veja", "vide", "consulte", "ver", "está", "estao",
            "entende", "entendendo", "entendi", "explicar", "explique", "fale", "diga", "mostre", "ajuda", "ajude", "quero", "queria",
            "não", "nao", "que", "pra", "pro", "com", "uma", "uns", "pelo", "pela", "pelas", "pelos", "mais", "muito",
            "cada", "deve", "devem", "esta", "estao", "estão", "seja", "sejam"
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
            // Busca parcial APENAS para termos técnicos longos (>4 letras) para evitar ruído (ex: 'ar')
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
     * Formata os resultados para o prompt da IA.
     */
    suspend fun formatarParaIA(resultado: GraphSearchResult): String {
        val s = StringBuilder()

        // PRIORIDADE 1: REGRAS DO CÓDEX (Texto Bruto do Manual) - Agora no Topo
        if (resultado.relatedChunks.isNotEmpty()) {
            s.append("\n=== REGRAS DO CÓDEX (PÁGINAS DO MANUAL) ===\n")
            resultado.relatedChunks.forEach { chunk ->
                s.append("[${chunk.source_title} Pág. ${chunk.page_number}]: ${chunk.text}\n")
            }
        }

        // PRIORIDADE 2: DETALHES TÉCNICOS (Estatísticas)
        val tecnicos = buscarTabelasTecnicas(resultado.summaries.map { it.title })
        if (tecnicos.isNotBlank()) {
            s.append("\n=== ESTATÍSTICAS TÉCNICAS (TABELAS OFICIAIS) ===\n")
            s.append(tecnicos)
        }

        // PRIORIDADE 3: NAVEGAÇÃO POR COMUNIDADES (Resumos)
        if (resultado.summaries.isNotEmpty()) {
            s.append("\n=== NAVEGAÇÃO POR COMUNIDADES (GRAFO) ===\n")
            val grupos = resultado.summaries.groupBy { it.category }
            grupos.forEach { (categoria, nodes) ->
                s.append("\n[Comunidade: $categoria]\n")
                nodes.forEach { s.append("- ${it.title}: ${it.summary.take(400)}\n") }
            }

            // LOTE 89.7: Redução drástica para evitar estouro de créditos (402) no OpenRouter
            val magiasIds = resultado.summaries.filter { it.category.equals("Magia", true) }.take(1).map { it.title.lowercase().trim() }
            if (magiasIds.isNotEmpty()) {
                s.append("\n=== FICHA TÉCNICA DAS MAGIAS (DETALHADO) ===\n")
                val magiasParaInjetar = mutableSetOf<MagiaDefinicao>()
                
                // Pega as magias identificadas
                val identificadas = repository.magias.filter { it.nome.lowercase().trim() in magiasIds }
                magiasParaInjetar.addAll(identificadas)
                
                /* LOTE 89.8: Desativado temporariamente para evitar estouro de créditos (402)
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
                    repository.magias.filter { it.nome.lowercase().trim() in preReqs }.forEach { 
                        magiasParaInjetar.add(it)
                    }
                }
                */
                
                magiasParaInjetar.forEach { m ->
                    s.append("\n- Magia: ${m.nome}\n")
                    s.append("  Escolas: ${m.escola?.joinToString(", ") ?: "N/A"} | Energia: ${m.energia ?: "N/A"} | Tempo: ${m.tempoOperacao ?: "N/A"}\n")
                    s.append("  Duração: ${m.duracao ?: "N/A"} | Pre-req: ${m.preRequisitos ?: "N/A"}\n")
                    s.append("  Resumo: ${(m.texto ?: m.descricao ?: "").take(300)}\n")
                }

                // INJEÇÃO GABARITO NEXUS (MOTOR ALVO)
                for (nome in magiasIds) {
                    s.append(resolverTrilhaNexus(nome))
                }
            } // Fim do if magiasIds
        } // Fim do if summaries

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

    /**
     * LOTE 92: Busca profunda em arquivos JSON normalizados (assets).
     * Vasculha Armas, Perícias, Vantagens e Armaduras para dados exatos.
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

        // 2. Buscar Perícias
        val periciasEncontradas = repository.pericias.filter { per ->
            pistasNorm.any { pista -> per.nome.lowercase().contains(pista) }
        }.take(5)

        periciasEncontradas.forEach { per ->
            s.append("\n[Perícia: ${per.nome}]\n")
            s.append("- Atributo: ${per.atributoBase} | Dificuldade: ${per.dificuldadeFixa ?: "Variavel"}\n")
            if (per.exigeEspecializacao) s.append("- Obs: Exige Especialização.\n")
        }

        // 3. Buscar Vantagens/Desvantagens
        val vantagensEncontradas = repository.vantagens.filter { v ->
            pistasNorm.any { pista -> v.nome.lowercase().contains(pista) }
        }.take(5)

        vantagensEncontradas.forEach { v ->
            s.append("\n[Vantagem: ${v.nome}]\n")
            s.append("- Custo: ${v.custo} | Tipo: ${v.tipoCusto} | Pág: ${v.pagina}\n")
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
