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
import org.json.JSONObject

/**
 * MestreIAUseCase - LOTE 77: DIAGNÓSTICO PROFUNDO E ALTA FIDELIDADE.
 */
class MestreIAUseCase(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)
    private val graphEngine = MestreIAGraphEngine(repository)

    data class CatalogoLocalResult(
        val catalogo: MestreIAClient.CatalogoNomes,
        val isRagSuccess: Boolean
    )

    private fun similaridade(a: String, b: String): Double {
        val na = a.trim().lowercase().replace(Regex("[^a-záàâãéèêíïóôõúüç\\s]"), "").replace("\\s+".toRegex(), " ")
        val nb = b.trim().lowercase().replace(Regex("[^a-záàâãéèêíïóôõúüç\\s]"), "").replace("\\s+".toRegex(), " ")
        if (na == nb) return 1.0
        if (na.isEmpty() || nb.isEmpty()) return 0.0
        if (na.contains(nb) || nb.contains(na)) return 0.85
        val maxLen = maxOf(na.length, nb.length)
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

    fun conversarComMestreIA(
        prompt: String,
        modo: String = "conversa",
        onStatusUpdate: (String) -> Unit = {},
        onChunk: (String) -> Unit = {},
        onResultado: (Boolean, MestreIAClient.ChatResponse) -> Unit
    ) {
        viewModelScope.launch {
            val catalogoLocal = gerarCatalogoLocal(prompt, viewModel.mestreIAChatHistory)
            val isRagUsed = catalogoLocal.isRagSuccess
            val filaFallback = gerarFilaDeFallback(prompt, modo)
            
            var sucesso = false
            var ultimaResposta: MestreIAClient.ChatResponse? = null

            for ((index, config) in filaFallback.withIndex()) {
                val (url, key, model) = config
                val amigavel = traduzirModeloParaMestre(model)
                val modoLabel = if (url.contains("openrouter")) "LITE-${index+1}" else "PRO"
                
                onStatusUpdate("Consultando $amigavel...")

                try {
                    val startTime = System.currentTimeMillis()
                    val historicoLimitado = viewModel.mestreIAChatHistory.takeLast(8).map { it.role to it.text }
                    val contextoPersonagem = MestreIAContextFilter.gerarContexto(viewModel.personagem, modo)

                    var resposta = MestreIAClient.perguntarAoMestre(
                        baseUrl = url, apiKey = key, workspaceSlug = model,
                        prompt = prompt, history = historicoLimitado, contextoPersonagem = contextoPersonagem,
                        catalogo = catalogoLocal.catalogo, modo = modo, onChunk = onChunk
                    )

                    if (!resposta.text.startsWith("Erro")) {
                        // --- MOTOR REACT MULTI-STAGE ---
                        var iteracoes = 0
                        val maxIteracoes = 4
                        val queriesRealizadas = mutableSetOf<String>()
                        val historicoLoop = historicoLimitado.toMutableList()

                        while (resposta.toolCalls.isNotEmpty() && iteracoes < maxIteracoes) {
                            iteracoes++
                            val searchCall = resposta.toolCalls.find { it.name == MestreIATools.TOOL_SEARCH_RULES }
                            if (searchCall != null) {
                                val query = searchCall.args.optString("query", "").lowercase().trim()
                                if (queriesRealizadas.contains(query)) break
                                queriesRealizadas.add(query)

                                onStatusUpdate("Nexus: Investigando $query...")
                                val graphResult = graphEngine.buscarNoGrafo(query)
                                val extraContext = graphEngine.formatarParaIA(graphResult)
                                
                                val isHallucinating = detectarAlucinacao(resposta.text, extraContext)
                                val novoPrompt = if (isHallucinating) {
                                    "[AUDITORIA] Erro detectado. RECOMECE usando APENAS:\n$extraContext"
                                } else {
                                    "[INVESTIGAÇÃO $iteracoes] Dados para '$query':\n$extraContext\n\nAnalise e prossiga."
                                }

                                if (resposta.text.isNotBlank()) historicoLoop.add("model" to resposta.text)
                                historicoLoop.add("user" to novoPrompt)

                                val respostaProxima = MestreIAClient.perguntarAoMestre(
                                    baseUrl = url, apiKey = key, workspaceSlug = model,
                                    prompt = if (iteracoes >= maxIteracoes) "$novoPrompt\n\nCONCLUA AGORA." else novoPrompt,
                                    history = historicoLoop, contextoPersonagem = contextoPersonagem,
                                    catalogo = catalogoLocal.catalogo, modo = modo, onChunk = onChunk
                                )
                                
                                if (!respostaProxima.text.startsWith("Erro")) {
                                    val textoAnterior = if (resposta.text.contains("Investigando")) "" else resposta.text
                                    resposta = respostaProxima.copy(text = if (textoAnterior.isNotBlank()) "$textoAnterior\n\n${respostaProxima.text}" else respostaProxima.text)
                                } else break
                            } else break
                        }

                        // --- AUTO-HEALING ---
                        if ((modo == "geracao" || modo == "analise") && resposta.rawJson == null) {
                            if (MestreIAClient.extrairJsonFicha(resposta.text) == null) {
                                onStatusUpdate("Auto-Healing: Reparando JSON...")
                                val respostaCorrigida = MestreIAClient.perguntarAoMestre(
                                    baseUrl = url, apiKey = key, workspaceSlug = model,
                                    prompt = "[SISTEMA] JSON inválido. Repita APENAS o bloco JSON.",
                                    history = historicoLoop, contextoPersonagem = contextoPersonagem,
                                    catalogo = catalogoLocal.catalogo, modo = modo, onChunk = onChunk
                                )
                                if (!respostaCorrigida.text.startsWith("Erro")) resposta = respostaCorrigida
                            }
                        }

                        val textoLimpo = limparRascunhoIA(resposta.text, catalogoLocal.catalogo.chunks)
                        val textoAuditado = if (modo == "conversa") MestreIARuleAuditor.auditarTextoPorContexto(textoLimpo, viewModel.personagem) else textoLimpo
                        
                        val bibliografia = if (isRagUsed && modo == "conversa") {
                            val refs = catalogoLocal.catalogo.chunks.take(2).joinToString(", ") { " [Pág. ${it.page_number ?: "???"}]" }
                            "\n\n_Ref. Manual: $refs _"
                        } else ""

                        onResultado(isRagUsed, resposta.copy(text = textoAuditado + bibliografia, modelName = amigavel, latencyMs = System.currentTimeMillis() - startTime))
                        sucesso = true
                        break
                    } else {
                        android.util.Log.e("MestreIA", "Falha no provedor $modoLabel ($model): ${resposta.text}")
                        ultimaResposta = resposta
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MestreIA", "Exceção no provedor $modoLabel ($model)", e)
                    ultimaResposta = MestreIAClient.ChatResponse("Erro: ${e.message}")
                }
            }
            if (!sucesso) onResultado(false, ultimaResposta ?: MestreIAClient.ChatResponse("Falha em todos os provedores."))
        }
    }

    /**
     * ATENÇÃO AGENTE IA: NÃO ALTERE OS IDS DOS MODELOS ABAIXO.
     * Esta configuração foi estabilizada para o ambiente de ABRIL/2026.
     * Gemini 2.0 foi depreciado. Use APENAS Gemini 2.5 e 3.0 conforme definido.
     */
    private fun gerarFilaDeFallback(prompt: String, modo: String): List<Triple<String, String, String>> {
        val fila = mutableListOf<Triple<String, String, String>>()
        val isComplexo = modo == "geracao" || modo == "analise"
        if (isComplexo) {
            fila.add(Triple(BuildConfig.MESTRE_IA_DEEPSEEK_URL, BuildConfig.MESTRE_IA_DEEPSEEK_KEY, BuildConfig.MESTRE_IA_DEEPSEEK_MODEL))
            fila.add(Triple(BuildConfig.MESTRE_IA_LITE_1_URL, BuildConfig.MESTRE_IA_GEMINI_KEY, "gemini-3.0-pro"))
        } else {
            // Fila de Fallback (Modo Conversa/LITE)
            fila.add(Triple(BuildConfig.MESTRE_IA_OPENROUTER_URL, BuildConfig.MESTRE_IA_OPENROUTER_2_KEY, "qwen/qwen-2.5-72b-instruct"))
            fila.add(Triple(BuildConfig.MESTRE_IA_OPENROUTER_URL, BuildConfig.MESTRE_IA_OPENROUTER_3_KEY, "google/gemini-2.5-flash"))
            fila.add(Triple(BuildConfig.MESTRE_IA_LITE_1_URL, BuildConfig.MESTRE_IA_GEMINI_KEY, "gemini-2.5-flash"))
        }
        return fila.filter { it.second.isNotBlank() }
    }

    private fun traduzirModeloParaMestre(id: String): String {
        return when {
            id.contains("gemini-3.0") -> "Mestre Supremo (Gemini 3.0)"
            id.contains("gemini-2.5") -> "Mestre Mensageiro (Gemini 2.5)"
            id.contains("deepseek") -> "Mestre Sábio (DeepSeek PRO)"
            id.contains("qwen") -> "Mestre Estrategista (Qwen)"
            else -> "Mestre IA ($id)"
        }
    }

    fun integrarRespostaNaFicha(ficha: com.gurps.ficha.data.network.MestreIAResponse) {
        if (ficha.nome.isNotBlank()) viewModel.personagem.nome = ficha.nome
        viewModel.atualizarForca(ficha.atributos.st)
        viewModel.atualizarDestreza(ficha.atributos.dx)
        viewModel.atualizarInteligencia(ficha.atributos.iq)
        viewModel.atualizarVitalidade(ficha.atributos.ht)
        ficha.vantagens.forEach { adicionarVantagem(it) }
        ficha.desvantagens.forEach { adicionarVantagem(it) }
        ficha.pericias.forEach { adicionarPericia(it.nome, it.nivel) }
        ficha.tecnicas.forEach { tecnica ->
            val def = repository.tecnicasCatalogo.find { it.nome.equals(tecnica.nome, true) }
            if (def != null) {
                val base = viewModel.personagem.pericias.find { it.nome.equals(def.preRequisitoRaw, true) }
                if (base != null) {
                    val nivelRelativo = tecnica.nivel - base.calcularNivel(viewModel.personagem)
                    viewModel.adicionarTecnica(def, base, nivelRelativo)
                }
            }
        }
        ficha.equipamentos.forEach { adicionarEquipamento(it) }
    }

    fun adicionarVantagem(nome: String) {
        val def = repository.vantagens.find { similaridade(it.nome, nome) >= 0.80 }
        if (def != null) viewModel.adicionarVantagem(def)
    }

    fun adicionarPericia(nome: String, nivel: Int) {
        val def = repository.pericias.find { similaridade(it.nome, nome) >= 0.80 }
        if (def != null) {
            val pts = CharacterRules.calcularPontosParaNivel(com.gurps.ficha.model.Dificuldade.fromSigla(def.dificuldadeFixa), viewModel.personagem.getAtributo(def.atributoBase), nivel)
            viewModel.adicionarPericia(def, pts)
        }
    }

    fun adicionarEquipamento(eqIA: com.gurps.ficha.data.network.MestreIAEquipamento) {
        val arma = repository.armasCatalogo.find { similaridade(it.nome, eqIA.nome) >= 0.85 }
        val armor = repository.armadurasCatalogo.find { similaridade(it.nome, eqIA.nome) >= 0.85 }
        when {
            eqIA.dano != null || arma != null -> {
                val def = arma ?: com.gurps.ficha.model.ArmaCatalogoItem(id = "ia_${eqIA.nome}", nome = eqIA.nome, tipoCombate = "corpo_a_corpo", categoria = "IA", grupo = "IA", stMinimo = eqIA.st_min ?: 10, danoRaw = eqIA.dano ?: "1d", custoBase = eqIA.custo, pesoBaseKg = eqIA.peso, aparar = eqIA.aparar ?: "0", observacoes = "Mestre IA")
                viewModel.adicionarEquipamentoArma(def)
            }
            eqIA.rd != null || armor != null -> {
                val def = armor ?: com.gurps.ficha.model.ArmaduraCatalogoItem(id = "ia_${eqIA.nome}", nome = eqIA.nome, nt = 4, local = "corpo", rd = eqIA.rd?.toString() ?: "1", custoBase = eqIA.custo, pesoBaseKg = eqIA.peso, observacoes = "Mestre IA")
                viewModel.adicionarEquipamentoArmadura(def)
            }
            else -> viewModel.adicionarEquipamento(com.gurps.ficha.model.Equipamento(nome = eqIA.nome, peso = eqIA.peso.toFloat(), custo = eqIA.custo.toFloat(), quantidade = eqIA.quantidade))
        }
    }

    fun extrairJsonDeNarrativa(texto: String): String? {
        val regex = Regex("```json(.*?)```", RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(texto)
        return if (match != null) match.groupValues[1].trim() else if (texto.trim().startsWith("{")) texto.trim() else null
    }

    fun limparNarrativaParaChat(texto: String): String {
        return texto.replace(Regex("```json.*?```", RegexOption.DOT_MATCHES_ALL), "").trim()
    }

    private fun limparRascunhoIA(texto: String, chunks: List<MestreIAChunk>?): String {
        return texto.lines().filter { !it.trim().lowercase().startsWith("thought ") }.joinToString("\n").trim()
    }

    suspend fun gerarCatalogoLocal(prompt: String, history: List<com.gurps.ficha.data.network.MestreIAClient.ChatMessage> = emptyList()): CatalogoLocalResult {
        // LOTE 71: BUSCA CONTEXTUAL
        // Combinamos o prompt atual com as últimas mensagens do histórico para garantir que a busca
        // no grafo não perca o assunto principal (ex: se o user diz "e sobre ela?", a busca precisa saber o que é "ela").
        // LOTE 78: MEMÓRIA CONTEXTUAL EXPANDIDA
        // Capturamos as últimas 5 mensagens, filtrando para focar na intenção do usuário
        val contextoRecente = history.takeLast(5)
            .joinToString(" ") { msg -> 
                if (msg.role == "user") msg.text else "" 
            }.trim()
        
        val promptExpandido = "$prompt $contextoRecente".take(600)
        android.util.Log.d("MestreIA", "Nexus Prompt Contextual: $promptExpandido")
        
        val res = graphEngine.buscarNoGrafo(promptExpandido)
        val summaries = res.summaries
        val vantSugestao = summaries.filter { it.category.trim().equals("Vantagem", true) || it.category.trim().equals("Desvantagem", true) }.map { it.title }
        val periSugestao = summaries.filter { it.category.trim().equals("Perícia", true) || it.category.trim().equals("Pericia", true) }.map { it.title }
        val magiaSugestao = summaries.filter { it.category.trim().equals("Magia", true) }.map { it.title }
        val tecnSugestao = summaries.filter { it.category.trim().equals("Técnica", true) || it.category.trim().equals("Tecnica", true) }.map { it.title }

        val cat = MestreIAClient.CatalogoNomes(
            vantagens = vantSugestao, 
            pericias = periSugestao, 
            tecnicas = tecnSugestao, 
            magias = magiaSugestao, 
            chunks = res.relatedChunks, 
            summaries = summaries,
            ponteDeFerro = graphEngine.formatarParaIA(res)
        )

        // Lógica de Sincronização Forçada (Lote 80):
        // Se o prompt pede por magia e o catálogo de magias veio zerado,
        // limpamos o grafo para forçar a re-população no próximo boot/abertura.
        if (magiaSugestao.isEmpty() && (prompt.contains("magia", true) || prompt.contains("desejo", true))) {
            android.util.Log.w("MestreIA", "Catálogo de Magia Vazio! Forçando Sincronização do Grafo...")
            repository.forçarSincronizacaoGrafo()
        }

        return CatalogoLocalResult(cat, res.relatedChunks.isNotEmpty() || res.summaries.isNotEmpty())
    }

    private fun detectarAlucinacao(text: String, context: String): Boolean {
        val t = text.lowercase()
        if (listOf("nível 1", "nível 2", "primeiro nível", "círculo de magia", "mana 1").any { t.contains(it) }) return true
        val match = Regex("pág\\.? (\\d+)").find(text)
        if (match != null && (match.groupValues[1].toIntOrNull() ?: 0) > 500) return true
        return false
    }
}
