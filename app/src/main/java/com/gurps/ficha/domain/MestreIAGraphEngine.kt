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

    suspend fun buscarNoGrafo(query: String, offset: Int = 0): GraphSearchResult {
        // LOTE 91: Busca Profunda com Re-Ranking (Inspirado em Nano-GraphRAG Local Query)
        // LOTE 97: Separação de Termos (Original vs Expandido) para evitar poluição
        val termosBase = extrairPalavrasChave(query, apenasOriginais = true)
        val termosExpandidos = extrairPalavrasChave(query, apenasOriginais = false)
        val todosOsTermos = (termosBase + termosExpandidos).distinct()
        
        android.util.Log.i("MestreIA_Auditoria", "TERMOS ORIGINAIS: $termosBase")
        android.util.Log.i("MestreIA_Auditoria", "TERMOS EXPANDIDOS: ${termosExpandidos - termosBase.toSet()}")
        
        if (todosOsTermos.isEmpty()) return GraphSearchResult(emptyList(), emptyList())

        val topNodes = mutableSetOf<GraphNodeEntity>()
        val chunksCandidatos = mutableListOf<MestreIAChunk>()

        // 1. Navegação de Grafo (Seed + Neighbors)
        android.util.Log.i("MestreIA_Auditoria", "--- INICIANDO BUSCA NO CÓDEX (Origem: graph_knowledge.json) ---")
        
        todosOsTermos.forEach { termo ->
            val queryIndividual = "$termo*"
            var encontrados = repository.buscarResumosGrafo(queryIndividual)
            
            // LOTE 91.8: FALLBACK PARA BUSCA 'LIKE'
            if (encontrados.isEmpty()) {
                encontrados = repository.buscarNodesPorTitulo(termo)
            }
            
            // Prioriza nós que dão match com termos BASE (Originais)
            val pesoTermo = if (termo in termosBase) 5 else 2
            
            // LOTE 98: Paginação de Nós (Se offset > 0, pula os primeiros e pega os próximos)
            if (offset > 0) {
                topNodes.addAll(encontrados.drop(offset * 2).take(pesoTermo))
            } else {
                topNodes.addAll(encontrados.take(pesoTermo))
            }
        }

        // LOTE 101: Remoção de 'vizinhos semânticos' para evitar ruído.
        // O sistema agora foca apenas nos nós diretos encontrados pela busca.

        // 2. Busca de Text Units (Chunks)
        val paginasAlvo = mutableSetOf<Pair<Int, String?>>()
        topNodes.forEach { node ->
            // LOTE 100: Regex para capturar [Nome do Livro Pág. X] (Captura a primeira menção completa)
            val matchLivro = Regex("""\[([^\]]*?)\s+P[ágǭg\uFFFD\s.]+(\d+)\]""", RegexOption.IGNORE_CASE).find(node.summary)
            if (matchLivro != null) {
                val livro = matchLivro.groupValues[1].trim()
                val pag = matchLivro.groupValues[2].toIntOrNull()
                if (pag != null) paginasAlvo.add(pag to livro)
            } else {
                // Fallback para quando só tem o número da página (compatibilidade)
                // LOTE 112: Usa findAll para capturar MÚLTIPLAS páginas (ex: [Pág. 353, 354, 388])
                Regex("""P[ágǭg\uFFFD\s.]+(\d+)""", RegexOption.IGNORE_CASE).findAll(node.summary).forEach { m ->
                    m.groupValues[1].toIntOrNull()?.let { paginasAlvo.add(it to null) }
                }
            }
            // NOVO: Capturar também números de página simples que estão no meio da string ex: [Pág. 353, 354, 388]
            // O código acima captura o "Pág. 353". Para "354, 388", a regex anterior pode não pegar.
            // Vamos fazer uma regex mais abrangente para a lista.
            Regex("""\[.*?P[ágǭg\uFFFD\s.]+(.*?)\]""", RegexOption.IGNORE_CASE).find(node.summary)?.let { m ->
                val listPaginas = m.groupValues[1]
                Regex("""(\d+)""").findAll(listPaginas).forEach { p ->
                    p.groupValues[1].toIntOrNull()?.let { 
                        if (matchLivro != null) {
                            paginasAlvo.add(it to matchLivro.groupValues[1].trim())
                        } else {
                            paginasAlvo.add(it to null)
                        }
                    }
                }
            }
        }

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
        paginasAlvo.forEach { (pag, livro) ->
            if (livro != null) {
                android.util.Log.d("MestreIA_Auditoria", "PONTE DE PÁGINA: Forçando carga da Pág. $pag do livro '$livro' indicada pelo Grafo.")
                chunksCandidatos.addAll(repository.buscarPorPaginaESource(pag, livro))
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
            if (paginasAlvo.any { it.first == chunk.page_number && (it.second == null || chunk.source_title.contains(it.second!!, true)) }) {
                score += 1000 // Aumentado de 50 para 1000 para garantir Top 1
            }

            // LOTE 112: HIERARQUIA DE AUTORIDADE (Módulo Básico é a lei mãe)
            if (chunk.source_id == "pt_modulo_basico") {
                score += 50
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
        val paginasGrafoRanking = paginasAlvo.mapIndexed { index, (page, _) -> page to (index + 1) }.toMap()

        // Aplica a fórmula matemática RRF para fundir as duas listas
        val chunksRRF = chunksPontuados.mapIndexed { textIndex, (chunk, lexicalScore) ->
            val textRank = textIndex + 1
            val textRrfScore = 1.0 / (textRank + 60)
            
            val graphRank = paginasGrafoRanking[chunk.page_number] ?: 1000 // Penalidade se o Grafo não recomendou
            val graphRrfScore = 1.0 / (graphRank + 60)
            
            // LOTE 105: Bônus de Autoridade do Grafo (Multiplicador de 5x para páginas sugeridas pelo Grafo)
            val graphMultiplier = if (paginasGrafoRanking.containsKey(chunk.page_number)) 5.0 else 1.0
            
            val finalRrfScore = textRrfScore + (graphRrfScore * graphMultiplier)
            chunk to finalRrfScore
        }.sortedByDescending { it.second }
        
        // LOTE 105: Filtro de Diversidade (Anti-Monopólio)
        // Impede que uma única página (ex: Pág 16) ocupe todas as vagas do contexto.
        // Permitimos no máximo 2 recortes da mesma página no Top 8.
        val contadorPaginas = mutableMapOf<Int, Int>()
        val chunksDiversos = chunksRRF.filter { (chunk, _) ->
            val page = chunk.page_number ?: -1
            val total = contadorPaginas.getOrDefault(page, 0)
            if (total < 2) {
                contadorPaginas[page] = total + 1
                true
            } else {
                false
            }
        }

        // LOTE 111: Priorização de Páginas Recomendadas (Garante que a 433 entre no Top 8)
        val chunksRecomendados = chunksDiversos.filter { (chunk, _) ->
            paginasAlvo.any { it.first == chunk.page_number }
        }
        val outrosChunks = chunksDiversos.filter { (chunk, _) ->
            paginasAlvo.none { it.first == chunk.page_number }
        }
        
        val chunksPaginados = (chunksRecomendados + outrosChunks).take(8)
        
        chunksPaginados.take(5).forEach { (chunk, score) ->
            android.util.Log.i("MestreIA_Auditoria", "TOP CHUNK (Offset $offset): Pág ${chunk.page_number} | Score: $score | Texto: ${chunk.text.take(60)}...")
        }

        // 4. PARENT DOCUMENT RETRIEVAL (Lote 103) + CONTEXTO ADJACENTE (Lote 106/112)
        // Ao invés de trazer apenas o parágrafo atual, nós identificamos as páginas vencedoras
        // e buscamos a PÁGINA INTEIRA (Documento Pai) + ADJACENTES (N-1 e N+1).
        // LOTE 112: Adição da página anterior (N-1) para capturar inícios de regras.
        val chunksFinais = mutableSetOf<MestreIAChunk>()
        chunksPaginados.forEach { (chunk, _) ->
            val pagina = chunk.page_number
            val fonte = chunk.source_title
            
            if (pagina != null && fonte != null) {
                // 1. Página Anterior (N-1) - LOTE 112
                if (pagina > 1) {
                    val paginaAnterior = repository.buscarPorPaginaESource(pagina - 1, fonte)
                    if (paginaAnterior.isNotEmpty()) {
                        chunksFinais.addAll(paginaAnterior)
                    }
                }

                // 2. Página de Impacto (N)
                val paginaImpacto = repository.buscarPorPaginaESource(pagina, fonte)
                chunksFinais.addAll(paginaImpacto)
                
                // 3. Página Seguinte (N+1)
                val paginaSeguinte = repository.buscarPorPaginaESource(pagina + 1, fonte)
                if (paginaSeguinte.isNotEmpty()) {
                    chunksFinais.addAll(paginaSeguinte)
                }
            } else {
                chunksFinais.add(chunk)
            }
        }

        android.util.Log.i("MestreIA_G", "Parent Document Retrieval (Lote 106): ${chunksFinais.size} recortes totais (Impacto + Adjacente) recuperados.")

        return GraphSearchResult(
            summaries = topNodes.toList().sortedByDescending { it.level }.take(5),
            // LOTE 106: Aumentamos para 20 recortes para garantir que as páginas adjacentes não sejam cortadas.
            relatedChunks = chunksFinais.toList().sortedWith(compareByDescending<MestreIAChunk> { chunk ->
                // Prioridade 1: Estar nas páginas recomendadas pelo Grafo
                if (paginasAlvo.any { it.first == chunk.page_number }) 1000.0 else 0.0
            }.thenByDescending { chunk ->
                // Prioridade 2: Score RRF original
                chunksRRF.find { it.first.chunk_id == chunk.chunk_id }?.second ?: 0.0
            }).take(40) // Aumentado para 40 para garantir que o contexto adjacente caiba
        )
    }

    private fun extrairPalavrasChave(texto: String, apenasOriginais: Boolean = false): List<String> {
        val stopWords = listOf(
            "como", "funciona", "regra", "regras", "quais", "preciso", "para", "sobre", "qual",
            "uma", "um", "com", "dos", "das", "pela", "pelo", "onde", "quando", "quem", "são", "sao",
            "gurps", "edicao", "edição", "calculo", "calcular", "lista", "tabela", "me", "de", "da", "do",
            "nos", "nas", "aos", "para", "pelo", "pela", "traga", "atender", "requisitos", "pre", "pra", "me", "fale",
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
        
        val palavrasOriginais = texto.trim().lowercase()
            .replace(Regex("[^a-zA-Z0-9\\sà-úÀ-Ú]"), " ")
            .split(" ")
            .filter { 
                val isNumber = it.all { c -> c.isDigit() } && it.length in 1..4
                (it.length >= 2 || it in siglasGurps || isNumber) && it !in stopWords 
            }

        if (apenasOriginais) return palavrasOriginais

        val expandidas = mutableSetOf<String>()
        expandidas.addAll(palavrasOriginais)

        val dicionarioTecnico = mapOf(
            "sangramento" to listOf("hemorragia", "ferimento", "saúde", "saude", "perda"),
            "hemorragia" to listOf("sangramento"),
            "pular" to listOf("salto", "distância", "altura", "salto em comprimento", "salto em altura"),
            "impacto" to listOf("colisão", "colisao", "batida", "queda", "atropelamento", "dano por colisão"),
            "colisão" to listOf("impacto", "queda", "atropelamento"),
            "colisao" to listOf("impacto", "queda", "atropelamento"),
            "queda" to listOf("impacto", "colisão", "dano de queda"),
            "asfixia" to listOf("afogamento", "sufocamento", "respiração", "fôlego", "folego", "ar"),
            "afogamento" to listOf("asfixia", "sufocamento", "agua", "água"),
            "st" to listOf("força", "forca", "levantamento", "carga", "dano", "gdp", "geb"),
            "força" to listOf("st", "levantamento", "carga"),
            "forca" to listOf("st", "levantamento", "carga"),
            "dx" to listOf("destreza", "agilidade", "coordenação"),
            "iq" to listOf("inteligência", "inteligencia", "vontade", "percepção", "percepcao"),
            "ht" to listOf("vitalidade", "saúde", "saude", "fadiga", "pf", "sobrevivência"),
            "velocidade" to listOf("deslocamento", "esquiva", "movimento", "m/s", "km/h")
        )
        palavrasOriginais.forEach { p ->
            dicionarioTecnico[p]?.let { expandidas.addAll(it) }
            // Busca parcial para termos compostos
            dicionarioTecnico.entries.forEach { entry ->
                if (p.contains(entry.key) || entry.key.contains(p)) {
                    expandidas.addAll(entry.value)
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
