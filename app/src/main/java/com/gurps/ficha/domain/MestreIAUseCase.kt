package com.gurps.ficha.domain

import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAResponse
import com.gurps.ficha.data.network.MestreIAPericiaIA
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.data.network.MestreIATools
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.domain.MestreIAContextFilter
import com.gurps.ficha.model.*
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Atua como o "Juiz" entre a IA e a Ficha Real.
 * Valida os nomes sugeridos pela IA contra os JSONs oficiais do App.
 * Possui busca fuzzy para equiparar nomes semelhantes.
 */
class MestreIAUseCase(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    /**
     * Wrapper para o resultado do catálogo filtrado pelo RAG Local.
     */
    data class CatalogoLocalResult(
        val catalogo: MestreIAClient.CatalogoNomes,
        val isRagSuccess: Boolean
    )

    /**
     * Calcula a "distância" entre dois textos normalizados.
     */
    private fun similaridade(a: String, b: String): Double {
        val na = a.trim().lowercase().replace(Regex("[^a-záàâãéèêíïóôõúüç\\s]"), "").replace("\\s+".toRegex(), " ")
        val nb = b.trim().lowercase().replace(Regex("[^a-záàâãéèêíïóôõúüç\\s]"), "").replace("\\s+".toRegex(), " ")
        if (na == nb) return 1.0
        if (na.isEmpty() || nb.isEmpty()) return 0.0

        if (na.contains(nb) || nb.contains(na)) return 0.85

        val maxLen = maxOf(na.length, nb.length)
        if (maxLen == 0) return 1.0
        
        val dp = Array(na.length + 1) { IntArray(nb.length + 1) }
        for (i in 0..na.length) dp[i][0] = i
        for (j in 0..nb.length) dp[0][j] = j
        for (i in 1..na.length) {
            for (j in 1..nb.length) {
                val cost = if (na[i - 1] == nb[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return 1.0 - (dp[na.length][nb.length].toDouble() / maxLen)
    }

    /**
     * Motor de conversação principal com RESILIÊNCIA TOTAL (Lote 53).
     * Tenta múltiplos provedores em cascata se houver falha.
     */
    fun conversarComMestreIA(
        prompt: String,
        modo: String = "conversa",
        onStatusUpdate: (String) -> Unit = {},
        onChunk: (String) -> Unit = {},
        onResultado: (Boolean, MestreIAClient.ChatResponse) -> Unit
    ) {
        viewModelScope.launch {
            val catalogoLocal = gerarCatalogoLocal(prompt)
            val isRagUsed = catalogoLocal.isRagSuccess
            android.util.Log.d("MestreIA", "Modo: $modo | RAG Ativo: $isRagUsed")

            val filaFallback = gerarFilaDeFallback(prompt, modo)
            
            var sucesso = false
            var ultimaResposta: MestreIAClient.ChatResponse? = null

            for ((index, config) in filaFallback.withIndex()) {
                val (url, key, model) = config
                val amigavel = traduzirModeloParaMestre(model)
                val modoLabel = if (url.contains("openrouter")) "LITE-${index+1}" else "PRO"
                
                onStatusUpdate("Tentando $amigavel...")
                android.util.Log.d("MestreIA", "Tentativa ${index+1}: Usando [$modoLabel] | Model: $model")

                try {
                    val startTime = System.currentTimeMillis()
                    var resposta = MestreIAClient.perguntarAoMestre(
                        baseUrl = url,
                        apiKey = key,
                        workspaceSlug = model,
                        prompt = prompt,
                        history = viewModel.mestreIAChatHistory.map { it.role to it.text },
                        contextoPersonagem = MestreIAContextFilter.gerarContexto(viewModel.personagem, modo),
                        catalogo = catalogoLocal.catalogo,
                        modo = modo,
                        onChunk = onChunk
                    )
                    val latency = System.currentTimeMillis() - startTime

                    if (!resposta.text.startsWith("Erro de API") && !resposta.text.startsWith("Erro de Conexão")) {
                        
                        // --- LOTE 61: ORQUESTRADOR REACT (TOOL CALLING) ---
                        if (resposta.toolCalls.isNotEmpty()) {
                            
                            val searchCall = resposta.toolCalls.find { it.name == MestreIATools.TOOL_SEARCH_RULES }
                            if (searchCall != null) {
                                val query = searchCall.args.optString("query", "")
                                android.util.Log.d("MESTRE_IA", "Busca no Grafo iniciada para: $query")
                                android.util.Log.i("MestreIA", "Agent disparou ferramenta ${MestreIATools.TOOL_SEARCH_RULES} (GraphRAG) para: $query")
                                
                                // --- LOTE 62: CONSULTA AO GRAFO ---
                                val graphResult = MestreIAGraphEngine.buscarNoGrafo(query, repository)
                                val extraContext = MestreIAGraphEngine.formatarParaIA(graphResult)
                                
                                val novoPrompt = "[SISTEMA AUTOMÁTICO] Resultado da busca no Grafo de Conhecimento GURPS ('$query'):\n$extraContext\n\nPor favor, com base nessas relações e regras, continue sua tarefa."
                                
                                val historicoAtualizado = viewModel.mestreIAChatHistory.map { it.role to it.text }.toMutableList()
                                if (resposta.text.isNotBlank()) {
                                    historicoAtualizado.add("model" to resposta.text)
                                }
                                
                                val respostaRecursiva = MestreIAClient.perguntarAoMestre(
                                    baseUrl = url,
                                    apiKey = key,
                                    workspaceSlug = model,
                                    prompt = novoPrompt,
                                    history = historicoAtualizado,
                                    contextoPersonagem = MestreIAContextFilter.gerarContexto(viewModel.personagem, modo),
                                    catalogo = catalogoLocal.catalogo,
                                    modo = modo,
                                    onChunk = onChunk
                                )
                                
                                if (!respostaRecursiva.text.startsWith("Erro")) {
                                    resposta = respostaRecursiva.copy(text = resposta.text + "\n" + respostaRecursiva.text)
                                }
                            }

                            val fillSheetCall = resposta.toolCalls.find { it.name == MestreIATools.TOOL_FILL_SHEET }
                            if (fillSheetCall != null) {
                                android.util.Log.i("MestreIA", "Agent disparou ferramenta fill_character_sheet!")
                                resposta = resposta.copy(
                                    text = resposta.text + "\n📦 Ficha preenchida com sucesso pelo Forjador Nativo (Tool Calling).",
                                    rawJson = fillSheetCall.args.toString()
                                )
                            }
                        }

                        // --- LOTE 52: LOGICA DE AUTO-HEALING (FALLBACK PARA QUANDO NÃO USOU TOOL) ---
                        if ((modo == "geracao" || modo == "analise") && resposta.rawJson == null && resposta.toolCalls.isEmpty()) {
                            val fichaTeste = MestreIAClient.extrairJsonFicha(resposta.text)
                            if (fichaTeste == null) {
                                android.util.Log.w("MestreIA", "Auto-Healing Ativado: JSON malformado detectado no provedor $modoLabel")
                                val promptCorrecao = prompt + "\n\n⚠️ AVISO TÉCNICO: Sua resposta anterior continha um JSON malformado ou incompleto. Por favor, envie APENAS o bloco JSON válido, sem textos explicativos antes ou depois, garantindo que todas as chaves e aspas estejam fechadas."
                                
                                val respostaCorrigida = MestreIAClient.perguntarAoMestre(
                                    baseUrl = url,
                                    apiKey = key,
                                    workspaceSlug = model,
                                    prompt = promptCorrecao,
                                    history = viewModel.mestreIAChatHistory.map { it.role to it.text },
                                    contextoPersonagem = MestreIAContextFilter.gerarContexto(viewModel.personagem, modo),
                                    catalogo = catalogoLocal.catalogo,
                                    modo = modo,
                                    onChunk = onChunk
                                )
                                
                                // Se a correção funcionar, usamos ela. Caso contrário, mantemos a original (que falhará no parse final)
                                if (!respostaCorrigida.text.startsWith("Erro")) {
                                    var limpo = respostaCorrigida.text
                                    val vazamentos = listOf(
                                        "Regras e Fatos do Mundo que o App extraiu",
                                        "REGRAS ENCONTRADAS PARA ESTA SITUAÇÃO",
                                        "Contexto RAG",
                                        "--- REGRAS ENCONTRADAS PARA ESTA SITUAÇÃO ---",
                                        "---------------------------------------------"
                                    )
                                    vazamentos.forEach { vazamento ->
                                        limpo = limpo.replace(vazamento, "").trim()
                                    }
                                    resposta = respostaCorrigida.copy(text = limpo)
                                }
                            }
                        }

                        val thematicModel = traduzirModeloParaMestre(resposta.modelName ?: model)
                        val textoLimpo = limparRascunhoIA(resposta.text, catalogoLocal.catalogo.chunks)
                        
                        // LOTE 55: Auditoria de Regras (Fiscal Ativo)
                        // O fiscal analisa o texto limpo e injeta notas se houver erros de custo/NH
                        val textoAuditado = if (modo == "conversa") {
                            MestreIARuleAuditor.auditarTextoPorContexto(textoLimpo, viewModel.personagem)
                        } else textoLimpo
                        
                        // NOTA DE DEPURACAO RAG: Bibliografia (NÃO adicionar em modo geração para não quebrar o JSON)
                        val bibliografia = if (isRagUsed && catalogoLocal.catalogo.chunks.isNotEmpty() && modo == "conversa") {
                            val chunks = catalogoLocal.catalogo.chunks
                            val refs = chunks.take(2).joinToString(", ") { chunk ->
                                val abrev = if (chunk.source_title.contains("Módulo Básico")) "MB" else chunk.source_title
                                "[$abrev pág. ${chunk.page_number ?: "???"}]" 
                            }
                            "\n\n_Ref. Manual: ${refs}_"
                        } else ""
                        
                        onResultado(isRagUsed, resposta.copy(text = textoAuditado + bibliografia, modelName = thematicModel, latencyMs = latency))
                        sucesso = true
                        break
                    } else {
                        android.util.Log.w("MestreIA", "Falha no provedor $modoLabel: ${resposta.text}")
                        ultimaResposta = resposta
                        
                        // FIDELIDADE: Em modos PRO, não falha silenciosamente se o modelo de elite falhar
                        // LOTE 16: Permitir fallback se for erro de limite (429), para não deixar o usuário na mão
                        if ((modo == "geracao" || modo == "analise") && !resposta.text.contains("429")) {
                            onResultado(false, resposta.copy(text = "FALHA NO MODELO PRO: ${resposta.text}\n(Não houve fallback para manter a integridade da ficha)"))
                            sucesso = true // Interrompe o loop
                            break
                        } else {
                            // Se for erro de limite, espera um pouco para dar visibilidade ao usuário
                            if (resposta.text.contains("429")) {
                                onStatusUpdate("Limite atingido no $amigavel. Tentando salvamento...")
                                kotlinx.coroutines.delay(2000)
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MestreIA", "Erro crítico no provedor $modoLabel", e)
                    val err = "Erro inesperado ($modoLabel): ${e.message}"
                    if (modo == "geracao" || modo == "analise") {
                        onResultado(false, MestreIAClient.ChatResponse(err))
                        sucesso = true
                        break
                    }
                    ultimaResposta = MestreIAClient.ChatResponse(err)
                }
            }

            if (!sucesso) {
                onResultado(false, ultimaResposta ?: MestreIAClient.ChatResponse("Todos os provedores falharam."))
            }
        }
    }

    /**
     * Gera a ordem de tentativa dos "cérebros".
     * LITE 1 -> LITE 2 -> PRO.
     * 
     * 🛑 REGRA CRÍTICA (Lote 16.6): NÃO altere esta lista de modelos sem confirmação do Rodolfo.
     * Estes nomes (Gemini 2.5, Llama 3.3, etc) foram validados via API e são essenciais.
     */
    private fun gerarFilaDeFallback(prompt: String, modo: String): List<Triple<String, String, String>> {
        val p = prompt.lowercase()
        val keywordsComplexas = listOf("ficha", "personagem", "criar", "analisar", "mudar", "alterar")
        val isComplexo = modo == "geracao" || modo == "analise" || keywordsComplexas.any { p.contains(it) }

        val fila = mutableListOf<Triple<String, String, String>>()

        if (isComplexo) {
            // --- MODO PRO (Alta Fidelidade) ---
            // 1. DeepSeek Chat (Mestre Sábio - API OFICIAL / PRO)
            fila.add(Triple(BuildConfig.MESTRE_IA_DEEPSEEK_URL, BuildConfig.MESTRE_IA_DEEPSEEK_KEY, BuildConfig.MESTRE_IA_DEEPSEEK_MODEL))
            
            // 2. Gemini 2.5 Flash (Fronteira Tecnológica Nativa)
            fila.add(Triple(BuildConfig.MESTRE_IA_LITE_1_URL, BuildConfig.MESTRE_IA_GEMINI_KEY, "gemini-2.5-flash"))
            
            // 3. Llama 3.3 70B (OpenRouter - Reserva de Elite)
            fila.add(Triple(BuildConfig.MESTRE_IA_OPENROUTER_URL, BuildConfig.MESTRE_IA_OPENROUTER_1_KEY, "meta-llama/llama-3.3-70b-instruct"))

            // 4. Gemini Pro Latest (Backup de Segurança Nativo)
            fila.add(Triple(BuildConfig.MESTRE_IA_LITE_1_URL, BuildConfig.MESTRE_IA_GEMINI_KEY, "gemini-pro-latest"))
        } else {
            // --- MODO FREE ---
            val url = BuildConfig.MESTRE_IA_OPENROUTER_URL
            val keys = listOf(
                BuildConfig.MESTRE_IA_OPENROUTER_1_KEY,
                BuildConfig.MESTRE_IA_OPENROUTER_2_KEY,
                BuildConfig.MESTRE_IA_OPENROUTER_3_KEY
            ).filter { it.isNotEmpty() }

            // 1. Gemini 2.5 Flash (O mais confiável, rápido e nativo - Prioridade Máxima agora)
            fila.add(Triple(BuildConfig.MESTRE_IA_LITE_1_URL, BuildConfig.MESTRE_IA_GEMINI_KEY, "gemini-2.5-flash"))

            // 2. DeepSeek R1 (OpenRouter) - Estabilizado (Sem sufixo :free)
            keys.forEach { fila.add(Triple(url, it, "deepseek/deepseek-r1")) }
            
            // 3. Llama 3.3 70B (OpenRouter)
            keys.forEach { fila.add(Triple(url, it, "meta-llama/llama-3.3-70b-instruct")) }
            
            // 4. Qwen 2.5 72B (OpenRouter)
            keys.forEach { fila.add(Triple(url, it, "qwen/qwen-2.5-72b-instruct")) }
        }

        return fila.filter { it.second.isNotEmpty() }
    }

    /** 
     * Converte o ID técnico no nome temático do Mestre.
     * 🛑 NÃO remova mapeamentos sem confirmação do usuário.
     */
    private fun traduzirModeloParaMestre(id: String): String {
        return when {
            id.contains("gemini-2.0-flash") -> "Mestre Mensageiro (Gemini 2.0 Flash)"
            id.contains("gemini-2.0-pro") -> "Mestre Supremo (Gemini 2.0 Pro)"
            id.contains("gemini-1.5-flash") -> "Mestre Mensageiro (Gemini Flash)"
            id.contains("gemini-1.5-pro") -> "Mestre Supremo (Gemini Pro)"
            id.contains("deepseek-r1") -> "Mestre Sábio (R1)"
            id.contains("deepseek-chat") -> "Mestre Sábio (DeepSeek PRO)"
            id.contains("qwen") -> "Mestre Estrategista (Qwen)"
            id.contains("llama-3.3") -> "Mestre Brutamontes (Llama)"
            else -> "Mestre IA ($id)"
        }
    }

    fun extrairJsonDeNarrativa(texto: String): String? {
        val regex = Regex("```json(.*?)```", RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(texto)
        return if (match != null) {
            match.groupValues[1].trim()
        } else if (texto.trim().startsWith("{")) {
            texto.trim()
        } else {
            null
        }
    }

    fun limparNarrativaParaChat(texto: String): String {
        // LOTE 16: Só remove se for explicitamente JSON. Mantém blocos genéricos de código.
        return texto.replace(Regex("```json.*?```", RegexOption.DOT_MATCHES_ALL), "").trim()
    }

    private fun limparRascunhoIA(texto: String, chunks: List<com.gurps.ficha.model.MestreIAChunk>? = null): String {
        var processado = texto.lines().filter { line ->
            val clean = line.trim().lowercase()
            !clean.startsWith("paragraph ") && 
            !clean.startsWith("step ") && 
            !clean.startsWith("thought ") &&
            !clean.startsWith("s check ") &&
            !clean.startsWith("confirming ")
        }.joinToString("\n").trim()

        // 1. REMOVER TERMOS EM INGLÊS ENTRE PARÊNTESES (ex: (Power Blow))
        // Padrão: remove (Qualquer coisa que pareça um título em inglês ou termo técnico não oficial)
        processado = processado.replace(Regex("\\s\\([^)]*[A-Z][a-z]+[^)]*\\)"), "")

        // 2. VERIFICAR FIDELIDADE DAS PÁGINAS CITADAS NO TEXTO
        // Se a IA citar "Pág. XX" e XX não estiver na lista de páginas dos chunks, a informação é duvidosa.
        if (chunks != null) {
            val paginasReais = chunks.map { it.page_number?.toString() }.filterNotNull().toSet()
            val matches = Regex("Pág\\.\\s*(\\d+)").findAll(processado)
            matches.forEach { match ->
                val pNum = match.groupValues[1]
                if (pNum !in paginasReais) {
                    processado = processado.replace(match.value, "${match.value} (Verificar no manual)")
                }
            }
        }

        return processado
    }

    // --- MÉTODOS PARA AÇÕES INDIVIDUAIS (USADOS PELO VIEWMODEL NAS SUGESTÕES CLICÁVEIS) ---

    /**
     * Integra uma resposta JSON completa na ficha do personagem.
     * Fidelidade Total: Atributos, Vantagens, Perícias, Técnicas, Magias e Equipamentos.
     */
    fun integrarRespostaNaFicha(ficha: com.gurps.ficha.data.network.MestreIAResponse) {
        // 1. NOME E HISTÓRICO
        if (ficha.nome.isNotBlank()) viewModel.personagem.nome = ficha.nome
        if (ficha.historico.isNotBlank()) viewModel.personagem.historico = ficha.historico
        if (ficha.aparencia.isNotBlank()) viewModel.personagem.aparencia = ficha.aparencia

        // 2. ATRIBUTOS
        viewModel.atualizarForca(ficha.atributos.st)
        viewModel.atualizarDestreza(ficha.atributos.dx)
        viewModel.atualizarInteligencia(ficha.atributos.iq)
        viewModel.atualizarVitalidade(ficha.atributos.ht)

        // 3. VANTAGENS E DESVANTAGENS
        ficha.vantagens.forEach { adicionarVantagem(it) }
        ficha.desvantagens.forEach { adicionarVantagem(it) } // Reutiliza lógica de busca semântica

        // 4. PERÍCIAS
        ficha.pericias.forEach { adicionarPericia(it.nome, it.nivel) }

        // 5. TÉCNICAS
        ficha.tecnicas.forEach { tecnica ->
            val def = repository.tecnicasCatalogo.firstOrNull { it.nome.equals(tecnica.nome, true) }
                ?: repository.tecnicasCatalogo.map { it to similaridade(it.nome, tecnica.nome) }
                    .filter { it.second >= 0.85 }
                    .maxByOrNull { it.second }?.first
            
            if (def != null) {
                // técnicas exigem uma perícia de base já existente na ficha
                val base = viewModel.personagem.pericias.firstOrNull { it.nome.equals(def.preRequisitoRaw, true) }
                if (base != null) {
                    val nivelRelativo = tecnica.nivel - base.calcularNivel(viewModel.personagem)
                    viewModel.adicionarTecnica(def, base, nivelRelativo)
                }
            }
        }

        // 6. MAGIAS
        ficha.magias.forEach { magiaIA ->
            val def = repository.magias.firstOrNull { it.nome.equals(magiaIA, true) }
            if (def != null) {
                viewModel.adicionarMagia(def)
            }
        }

        // 7. EQUIPAMENTOS (Fidelidade 9/10: Suporte a Armas e Armaduras reais do Mestre)
        ficha.equipamentos.forEach { eq ->
            adicionarEquipamento(eq)
        }
    }

    fun adicionarVantagem(nomeSugerido: String) {
        val def = repository.vantagens.firstOrNull { it.nome.equals(nomeSugerido, true) }
            ?: repository.vantagens.map { it to similaridade(it.nome, nomeSugerido) }
                .filter { it.second >= 0.80 }
                .maxByOrNull { it.second }?.first
        
        if (def != null) {
            viewModel.adicionarVantagem(def)
        }
    }

    fun adicionarPericia(nomeSugerido: String, nhSugerido: Int) {
        val def = repository.pericias.firstOrNull { it.nome.equals(nomeSugerido, true) }
            ?: repository.pericias.map { it to similaridade(it.nome, nomeSugerido) }
                .filter { it.second >= 0.80 }
                .maxByOrNull { it.second }?.first
        
        if (def != null) {
            val attrValor = viewModel.personagem.getAtributo(def.atributoBase)
            val pontos = CharacterRules.calcularPontosParaNivel(
                com.gurps.ficha.model.Dificuldade.fromSigla(def.dificuldadeFixa),
                attrValor,
                nhSugerido
            )
            viewModel.adicionarPericia(def, pontos)
        }
    }

    fun adicionarEquipamento(eqIA: com.gurps.ficha.data.network.MestreIAEquipamento) {
        val nomeSugerido = eqIA.nome
        val armaMatch = repository.armasCatalogo.firstOrNull { similaridade(it.nome, nomeSugerido) >= 0.85 }
        val armaduraMatch = repository.armadurasCatalogo.firstOrNull { similaridade(it.nome, nomeSugerido) >= 0.85 }
        
        when {
            // É uma Arma (ou a IA forneceu dados de dano)
            eqIA.dano != null || armaMatch != null -> {
                val def = armaMatch ?: com.gurps.ficha.model.ArmaCatalogoItem(
                    id = "ia_${nomeSugerido.lowercase().replace(" ", "_")}",
                    nome = nomeSugerido,
                    tipoCombate = if (nomeSugerido.contains("arco", true) || nomeSugerido.contains("pistola", true)) "distancia" else "corpo_a_corpo",
                    categoria = "IA",
                    grupo = "IA",
                    stMinimo = eqIA.st_min ?: 10,
                    danoRaw = eqIA.dano ?: "1d-2",
                    custoBase = eqIA.custo,
                    pesoBaseKg = eqIA.peso,
                    aparar = eqIA.aparar ?: "0",
                    observacoes = "Forjado pelo Mestre IA"
                )
                viewModel.adicionarEquipamentoArma(def)
            }
            // É uma Armadura (ou a IA forneceu RD)
            eqIA.rd != null || armaduraMatch != null -> {
                val def = armaduraMatch ?: com.gurps.ficha.model.ArmaduraCatalogoItem(
                    id = "ia_${nomeSugerido.lowercase().replace(" ", "_")}",
                    nome = nomeSugerido,
                    nt = 4,
                    local = "corpo",
                    rd = eqIA.rd?.toString() ?: "1",
                    custoBase = eqIA.custo,
                    pesoBaseKg = eqIA.peso,
                    observacoes = "Forjado pelo Mestre IA"
                )
                viewModel.adicionarEquipamentoArmadura(def)
            }
            // É um Equipamento Genérico
            else -> {
                viewModel.adicionarEquipamento(com.gurps.ficha.model.Equipamento(
                    nome = nomeSugerido,
                    peso = eqIA.peso.toFloat(),
                    custo = eqIA.custo.toFloat(),
                    quantidade = eqIA.quantidade
                ))
            }
        }
    }

    suspend fun gerarCatalogoLocal(userPrompt: String): CatalogoLocalResult {
        val graphResult = MestreIAGraphEngine.buscarNoGrafo(userPrompt, repository)
        
        // No modo GERAÇÃO, somos mais generosos com o catálogo para evitar que a IA invente nomes.
        val vantSugestao = (repository.vantagens.take(30).map { it.nome }).distinct()
        val periSugestao = (repository.pericias.take(30).map { it.nome }).distinct()
        val tecSugestao = (repository.tecnicasCatalogo.take(20).map { it.nome }).distinct()

        val catalogo = MestreIAClient.CatalogoNomes(
            vantagens = vantSugestao,
            desvantagens = emptyList(), // Será preenchido pelo grafo se necessário
            pericias = periSugestao,
            tecnicas = tecSugestao,
            magias = emptyList(),
            chunks = graphResult.relatedChunks
        )
        return CatalogoLocalResult(catalogo, graphResult.relatedChunks.isNotEmpty() || graphResult.summaries.isNotEmpty())
    }
}
