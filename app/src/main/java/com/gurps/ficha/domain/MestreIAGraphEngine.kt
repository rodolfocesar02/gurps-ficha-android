package com.gurps.ficha.domain

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.*

/**
 * Motor de busca RAG direto nos chunks.jsonl via FTS SQLite.
 */
class MestreIAGraphEngine(private val repository: DataRepository) {

    data class GraphSearchResult(
        val summaries: List<String> = emptyList(),
        val relatedChunks: List<MestreIAChunk>,
        val chunkScores: Map<String, Double> = emptyMap()
    )

    /**
     * LOTE 119: BUSCA DIRETA (Pula o Grafo).
     * Focada 100% em extrair o texto bruto dos manuais usando o novo motor FTS Agressivo.
     */
    suspend fun buscarDiretoNoCodex(query: String, termosExtras: List<String> = emptyList()): GraphSearchResult {
        val termosBase: List<String> = (extrairPalavrasChave(query, apenasOriginais = true) + termosExtras).distinct()
        val termosExpandidos: List<String> = (extrairPalavrasChave(query, apenasOriginais = false) + termosExtras).distinct()
        
        android.util.Log.i("MestreIA_RAG", "══ RAG BUSCA: \"${query.take(80)}\"")

        // 1. Busca Agressiva no RepositÃ³rio
        // Sem limite efetivo: FTS4 retorna em ordem de rowid (inserção), não por relevância.
        // O scoring em Kotlin cuida do ranking — precisamos de TODOS os chunks candidatos no pool.
        val chunksCandidatos: List<MestreIAChunk> = repository.buscarNoCodexDireto(query, termosBase, limit = 1500)

        if (chunksCandidatos.isEmpty()) {
            android.util.Log.e("MestreIA_RAG", "✖ RAG FALHOU: nenhum chunk para \"${query.take(60)}\" — IA não terá contexto do manual!")
            return GraphSearchResult(emptyList(), emptyList())
        }

        // 2. Motor de PontuaÃ§Ã£o (Scoring)
        val termoWeights = mutableMapOf<String, Int>()
        for (termo in termosBase) {
            val ocorrencias = chunksCandidatos.count { it.text.contains(termo, ignoreCase = true) }
            val pesoRaridade = if (ocorrencias > 0) (50 / ocorrencias).coerceIn(1, 50) else 1
            termoWeights[termo.lowercase()] = pesoRaridade
        }

        val chunksPontuados: MutableList<Pair<MestreIAChunk, Double>> = mutableListOf()
        for (chunk in chunksCandidatos) {
            var score = 0.0
            val texto = chunk.text.lowercase()
            
            for (termo in termosBase) {
                val termLower = termo.lowercase()
                if (texto.contains(termLower)) {
                    val peso = termoWeights[termLower] ?: 1
                    score += (100.0 * peso)
                }
            }

            for (termoEx in (termosExpandidos - termosBase.toSet())) {
                if (texto.contains(termoEx.lowercase())) score += 15.0
            }

            // Bonus AND: chunk contém TODOS os termos base → muito mais relevante que matches parciais
            if (termosBase.size >= 2 && termosBase.all { texto.contains(it.lowercase()) }) {
                score += 500.0
            }

            // Bonus Proximidade: pares de termos base que aparecem próximos (< 100 chars)
            if (termosBase.size >= 2) {
                for (i in termosBase.indices) {
                    val t1 = termosBase[i].lowercase()
                    val pos1 = texto.indexOf(t1).takeIf { it >= 0 } ?: continue
                    for (j in (i + 1) until termosBase.size) {
                        val t2 = termosBase[j].lowercase()
                        val pos2 = texto.indexOf(t2).takeIf { it >= 0 } ?: continue
                        if (kotlin.math.abs(pos1 - pos2) < 100) score += 200.0
                    }
                }
            }

            if ((chunk.page_number ?: 0) < 30 && chunk.source_id == "pt_modulo_basico") score -= 20.0

            chunksPontuados.add(Pair(chunk, score))
        }
        
        chunksPontuados.sortByDescending { it.second }

        val top5Log = chunksPontuados.take(5).joinToString(" | ") { "p.${it.first.page_number}(${it.second.toInt()}pts)" }
        android.util.Log.i("MestreIA_RAG", "  Scoring top-5: $top5Log")

        // 3. Diversificação por Página
        val contadorPaginas = mutableMapOf<String, Int>()
        val chunksDiversos: MutableList<MestreIAChunk> = mutableListOf()

        for (pair in chunksPontuados) {
            if (chunksDiversos.size >= 15) break
            val c = pair.first
            val pageKey = "${c.source_id}_${c.page_number}"
            val total = contadorPaginas.getOrDefault(pageKey, 0)
            if (total < 3) {
                contadorPaginas[pageKey] = total + 1
                chunksDiversos.add(c)
            }
        }

        // 3b. Garantia de Diversidade por Fonte
        // Se alguma fonte (ex: Pyramid, Gun Fu) tem chunks no pool mas não entrou nos top-15,
        // adiciona seu melhor chunk. Evita monopólio do Módulo Básico quando há suplementos relevantes.
        val fontesRepresentadas = chunksDiversos.map { it.source_id }.toSet()
        val fontesNoPool = chunksCandidatos.map { it.source_id }.distinct()
        for (fonte in fontesNoPool) {
            if (fonte !in fontesRepresentadas) {
                val melhorDaFonte = chunksPontuados.firstOrNull { it.first.source_id == fonte }
                if (melhorDaFonte != null) {
                    chunksDiversos.add(melhorDaFonte.first)
                    android.util.Log.i("MestreIA_RAG", "  Fonte garantida: ${fonte} → p.${melhorDaFonte.first.page_number}(${melhorDaFonte.second.toInt()}pts)")
                }
            }
        }

        // 4. Expansão por Proximidade
        // Chunks garantidos (fontes extras) são expandidos PRIMEIRO para não serem cortados pelo take(30)
        val chunksBase = chunksDiversos.take(15)
        val chunksGarantidos = chunksDiversos.drop(15)
        val chunksFinais = mutableSetOf<MestreIAChunk>()

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

        val scoresMap = chunksPontuados.associate { it.first.chunk_id to it.second }
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
     * Injeta chunks do manual + tabelas tecnicas extraidas dos mesmos chunks.
     */
    suspend fun formatarParaIA(resultado: GraphSearchResult, query: String = ""): String {
        val s = StringBuilder()

        if (resultado.relatedChunks.isNotEmpty()) {
            s.append("\n=== REGRAS DO CODEX (PAGINAS DO MANUAL) ===\n")
            s.append("INSTRUÇÃO: Chunks marcados [★★★] têm maior relevância para a pergunta. Priorize-os na análise.\n")
            val porFonte = resultado.relatedChunks.groupBy { it.source_id ?: "desconhecido" }
            porFonte.forEach { (_, chunks) ->
                val tituloFonte = chunks.first().source_title ?: "Manual"
                s.append("\n--- FONTE: $tituloFonte ---\n")
                chunks.forEach { chunk ->
                    val score = resultado.chunkScores[chunk.chunk_id] ?: 0.0
                    val relevancia = when {
                        score >= 800 -> "[★★★]"
                        score >= 300 -> "[★★]"
                        else -> "[★]"
                    }
                    s.append("[Pág. ${chunk.page_number}]$relevancia: ${chunk.text}\n")
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