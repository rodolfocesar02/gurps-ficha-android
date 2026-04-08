package com.gurps.ficha.domain

import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAResponse
import com.gurps.ficha.data.network.MestreIAPericiaIA
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.domain.rules.CharacterRules
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
                        
                        // --- LOTE 52: LOGICA DE AUTO-HEALING ---
                        if (modo == "geracao" || modo == "analise") {
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
                                    resposta = respostaCorrigida
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
                                "[${chunk.source_title}: Pág. ${chunk.page_number ?: "???"}]" 
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
                        if (modo == "geracao" || modo == "analise") {
                            onResultado(false, resposta.copy(text = "FALHA NO MODELO PRO: ${resposta.text}\n(Não houve fallback para manter a integridade da ficha)"))
                            sucesso = true // Interrompe o loop
                            break
                        } else {
                            // Se for erro de limite, espera um pouco para dar visibilidade ao usuário
                            if (resposta.text.contains("429")) {
                                onStatusUpdate("Limite atingido no $amigavel. Pulando...")
                                kotlinx.coroutines.delay(1500)
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
            
            // 2. Llama 3.3 (OpenRouter) - Backup de Elite
            fila.add(Triple(BuildConfig.MESTRE_IA_OPENROUTER_URL, BuildConfig.MESTRE_IA_OPENROUTER_1_KEY, "meta-llama/llama-3.3-70b-instruct"))
            
            // 3. Gemini Pro (Backup Supremo)
            fila.add(Triple(BuildConfig.MESTRE_IA_LITE_1_URL, BuildConfig.MESTRE_IA_GEMINI_KEY, "gemini-1.5-pro"))
        } else {
            // --- MODO FREE (Zero Custo / 600 usos totais com 3 chaves) ---
            val url = BuildConfig.MESTRE_IA_OPENROUTER_URL
            val keys = listOf(
                BuildConfig.MESTRE_IA_OPENROUTER_1_KEY,
                BuildConfig.MESTRE_IA_OPENROUTER_2_KEY,
                BuildConfig.MESTRE_IA_OPENROUTER_3_KEY
            ).filter { it.isNotEmpty() }

            // 1. DeepSeek R1 Free (Tenta as 3 chaves sequencialmente se houver erro de limite)
            keys.forEach { fila.add(Triple(url, it, "deepseek/deepseek-r1:free")) }
            
            // 2. Qwen 2.5 72B Free (Tenta as 3 chaves)
            keys.forEach { fila.add(Triple(url, it, "qwen/qwen-2.5-72b-instruct:free")) }
            
            // 3. Llama 3.3 70B Free (Tenta as 3 chaves)
            keys.forEach { fila.add(Triple(url, it, "meta-llama/llama-3.3-70b-instruct:free")) }
        }

        return fila.filter { it.second.isNotEmpty() }
    }

    private fun traduzirModeloParaMestre(id: String): String {
        return when {
            id.contains("gemini-1.5-pro") -> "Mestre Supremo (Gemini Pro)"
            id.contains("deepseek-r1:free") -> "Mestre Sábio (R1 Free)"
            id.contains("deepseek-chat") -> "Mestre Sábio (DeepSeek PRO)"
            id.contains("qwen") -> "Mestre Estrategista (Qwen Free)"
            id.contains("llama-3.3") && id.contains(":free") -> "Mestre Brutamontes (Llama Free)"
            id.contains("llama-3.3") -> "Mestre Brutamontes (Llama PRO)"
            else -> "Mestre IA ($id)"
        }
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
        ficha.magias.forEach { nomeMagia ->
            val def = repository.magias.firstOrNull { it.nome.equals(nomeMagia, true) }
            if (def != null) {
                viewModel.adicionarMagia(def)
            }
        }

        // 7. EQUIPAMENTOS
        ficha.equipamentos.forEach { eq ->
            adicionarEquipamento(eq.nome)
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

    fun adicionarEquipamento(nomeSugerido: String) {
        val arma = repository.armasCatalogo.firstOrNull { similaridade(it.nome, nomeSugerido) >= 0.85 }
        val armadura = repository.armadurasCatalogo.firstOrNull { similaridade(it.nome, nomeSugerido) >= 0.85 }
        
        if (arma != null) {
            viewModel.adicionarEquipamentoArma(arma)
        } else if (armadura != null) {
            viewModel.adicionarEquipamentoArmadura(armadura)
        } else {
            viewModel.adicionarEquipamento(com.gurps.ficha.model.Equipamento(nome = nomeSugerido))
        }
    }

    suspend fun gerarCatalogoLocal(userPrompt: String): CatalogoLocalResult {
        val rag = MestreIARagEngine.buscarContexto(userPrompt, repository)
        
        // No modo GERAÇÃO, somos mais generosos com o catálogo para evitar que a IA invente nomes.
        val vantSugestao = (rag.vantagens + repository.vantagens.take(30).map { it.nome }).distinct()
        val periSugestao = (rag.pericias + repository.pericias.take(30).map { it.nome }).distinct()
        val tecSugestao = (rag.tecnicas + repository.tecnicasCatalogo.take(20).map { it.nome }).distinct()

        val catalogo = MestreIAClient.CatalogoNomes(
            vantagens = vantSugestao,
            desvantagens = rag.desvantagens,
            pericias = periSugestao,
            tecnicas = tecSugestao,
            magias = rag.magias,
            chunks = rag.chunks
        )
        return CatalogoLocalResult(catalogo, rag.chunks.isNotEmpty())
    }
}
