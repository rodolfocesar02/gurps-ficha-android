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

        val entidadesRelacionadas = mutableSetOf<String>()
        topNodes.forEach { node ->
            // Extrai conceitos em CamelCase ou específicos do resumo
            Regex("([A-ZÀ-Ú][A-ZÀ-Úa-zà-ú]{3,})").findAll(node.summary).forEach { 
                val conceito = it.value.trim()
                if (conceito.length > 3) entidadesRelacionadas.add(conceito) 
            }
            // Só adiciona a categoria como semente se não for uma categoria genérica (LOTE 94: Filtro rígido)
            val categoriasGenericas = listOf(
                "Regra", "Vantagem", "Desvantagem", "Perícia", "Pericia", "Magia", "Equipamento", 
                "Sistema", "Arma", "Armadura", "Técnica", "Tecnica", "Atributo"
            )
            val cat = node.category.trim()
            if (cat !in categoriasGenericas && cat.length > 3) {
                entidadesRelacionadas.add(cat)
            }
        }

        // LOTE 96: Busca de vizinhos apenas se a semente for POBRE (evita trazer lixo)
        if (topNodes.size < 3 && entidadesRelacionadas.isNotEmpty()) {
            val queryVizinhos = entidadesRelacionadas.take(3).joinToString(" OR ") { "$it*" }
            val vizinhos = repository.buscarResumosGrafo(queryVizinhos).take(3)
            topNodes.addAll(vizinhos)
        }

        // 2. Busca de Text Units (Chunks)
        val paginasAlvo = mutableSetOf<Pair<Int, String?>>()
        topNodes.forEach { node ->
            // LOTE 100: Regex para capturar [Nome do Livro Pág. X]
            val match = Regex("""\[([^\]]*?)\s+P[ágǭg\uFFFD\s.]+(\d+)\]""", RegexOption.IGNORE_CASE).find(node.summary)
            if (match != null) {
                val livro = match.groupValues[1].trim()
                val pag = match.groupValues[2].toIntOrNull()
                if (pag != null) paginasAlvo.add(pag to livro)
            } else {
                // Fallback para quando só tem o número da página (compatibilidade)
                Regex("""P[ágǭg\uFFFD\s.]+(\d+)""", RegexOption.IGNORE_CASE).find(node.summary)?.let { m ->
                    m.groupValues[1].toIntOrNull()?.let { paginasAlvo.add(it to null) }
                }
            }
        }

        val termosBusca = (todosOsTermos + entidadesRelacionadas.take(5)).distinct()
        val queryManual = termosBusca.joinToString(" OR ") { "$it*" }
        chunksCandidatos.addAll(repository.buscarRecortesManual(queryManual, 30)) // Reduzido de 50 para 30
        
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

        // 3. RE-RANKING PROFUNDO (O "Coração" do GraphRAG)
        val chunksPontuados = chunksCandidatos.map { chunk ->
            var score = 0
            val texto = chunk.text.lowercase()
            
            // Pontua por termos da query (Peso 10 para originais, 2 para expandidos)
            termosBase.forEach { if (texto.contains(it.lowercase())) score += 10 }
            (termosExpandidos - termosBase.toSet()).forEach { if (texto.contains(it.lowercase())) score += 2 }
            
            // Pontua por entidades do grafo (Peso 20 - CRÍTICO: Prioriza o que está no seu JSON!)
            topNodes.forEach { 
                if (texto.contains(it.title.lowercase())) {
                    score += 20
                    // Bônus extra se o título do nó for exatamente uma palavra da query original
                    if (termosBase.any { base -> base.equals(it.title, true) }) score += 15
                    android.util.Log.v("MestreIA_Auditoria", "BÔNUS DE TÍTULO: Chunk pág ${chunk.page_number} contém '${it.title}' (+15)")
                }
            }
            
            // Bônus por categoria (Peso 5)
            if (topNodes.any { it.category.lowercase() in texto }) score += 5
            
            // LOTE 95/100: BÔNUS MASSIVO DE PÁGINA (Peso 30) - Prioriza a página E LIVRO corretos
            if (paginasAlvo.any { it.first == chunk.page_number && (it.second == null || chunk.source_title.contains(it.second!!, true)) }) {
                score += 30
            }
            
            // Penalidade para páginas muito baixas (Índice/Introdução)
            if ((chunk.page_number ?: 0) < 30) score -= 10
            
            chunk to score
        }.sortedByDescending { it.second }
        
        // LOTE 98: Paginação de Chunks (Pula os primeiros 'offset * 5' resultados)
        val chunksPaginados = if (offset > 0) {
            chunksPontuados.drop(offset * 5).take(8)
        } else {
            chunksPontuados.take(8)
        }
        
        chunksPaginados.take(3).forEach { (chunk, score) ->
            android.util.Log.i("MestreIA_Auditoria", "TOP CHUNK (Offset $offset): Pág ${chunk.page_number} | Score: $score | Texto: ${chunk.text.take(60)}...")
        }

        // 4. EXPANSÃO DE CONTEXTO
        val chunksFinais = mutableSetOf<MestreIAChunk>()
        chunksPaginados.forEach { (chunk, _) ->
            chunksFinais.add(chunk)
            val idBase = chunk.chunk_id.substringBeforeLast("_c")
            val currentIdx = chunk.chunk_id.substringAfterLast("_c").toIntOrNull() ?: return@forEach
            
            // Traz o parágrafo anterior e o próximo para profundidade técnica
            repository.getChunkById("${idBase}_c${currentIdx - 1}")?.let { chunksFinais.add(it) }
            repository.getChunkById("${idBase}_c${currentIdx + 1}")?.let { chunksFinais.add(it) }
        }

        android.util.Log.i("MestreIA_G", "Busca Profunda: ${chunksFinais.size} recortes filtrados.")

        return GraphSearchResult(
            summaries = topNodes.toList().sortedByDescending { it.level }.take(5),
            // CRÍTICO: Ordenar por SCORE (Relevância) e NÃO por ID Alfabético!
            relatedChunks = chunksFinais.toList().sortedByDescending { chunk -> 
                chunksPontuados.find { it.first.chunk_id == chunk.chunk_id }?.second ?: 0 
            }.take(5)
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
            "cavar" to listOf("escavação", "trabalho", "braçal", "escavar"),
            "buraco" to listOf("escavação", "fender", "valeta"),
            "sangramento" to listOf("hemorragia", "ferimento", "saúde", "perda"),
            "pular" to listOf("salto", "distância", "altura")
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
