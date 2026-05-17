package com.gurps.ficha.domain

import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.data.network.MestreIATools
import com.gurps.ficha.model.*
import nexus.arcano.*
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * MestreIAUseCase - ESPECIALISTA EM AUDITORIA (Regras e Dúvidas).
 * Focado em fornecer respostas precisas baseadas no Códex de GURPS.
 */
class MestreIAUseCase(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.IO)
    private val graphEngine = MestreIAGraphEngine(repository)
    
    // Adaptador para o Motor Nexus ler do repositório oficial
    private val arcanoCatalogo = object : ArcanoCatalogo {
        override fun preRequisitoRaw(magiaId: String) = repository.magias.find { it.id == magiaId }?.preRequisitos ?: ""
        override fun escolas(magiaId: String) = repository.magias.find { it.id == magiaId }?.escola ?: emptyList()
        override fun nome(magiaId: String) = repository.magias.find { it.id == magiaId }?.nome ?: magiaId
        override fun existe(magiaId: String) = repository.magias.any { it.id == magiaId }
        override fun todasMagiasIds() = repository.magias.map { it.id }
    }
    private val nexusEngine = NexusArcanoEngine(arcanoCatalogo)

    private data class AIConfig(val url: String, val key: String, val model: String)

    fun conversarComMestreIA(
        prompt: String,
        modo: String = "conversa",
        onStatusUpdate: (String) -> Unit = {},
        onChunk: (String) -> Unit = {},
        onResultado: (Boolean, MestreIAClient.ChatResponse) -> Unit
    ) {
        viewModelScope.launch {
            // LOTE 126: Garante que o Códex esteja carregado e purificado antes de qualquer busca
            repository.sincronizarCodexSeNecessario()
            val updateStatus: (String) -> Unit = { status ->
                CoroutineScope(Dispatchers.Main).launch { onStatusUpdate(status) }
            }
            val sendChunk: (String) -> Unit = { chunk ->
                CoroutineScope(Dispatchers.Main).launch { onChunk(chunk) }
            }
            val sendResult: (Boolean, MestreIAClient.ChatResponse) -> Unit = { success, resp ->
                CoroutineScope(Dispatchers.Main).launch { onResultado(success, resp) }
            }

            val isCasual = prompt.trim().lowercase() in listOf("oi", "olá", "ola", "bom dia", "boa tarde", "boa noite", "tudo bem", "teste", "test") || prompt.length < 5
            
            // LOTE 126: FILA DE CONTINGÊNCIA PRIME (Multi-Cloud)
            val fila = if (modo == "geracao" || modo == "analise") {
                // MODO FORJADOR: Apenas DeepSeek Paga (Conforme solicitado)
                listOf(AIConfig(BuildConfig.MESTRE_IA_DEEPSEEK_URL, BuildConfig.MESTRE_IA_DEEPSEEK_KEY, BuildConfig.MESTRE_IA_DEEPSEEK_MODEL))
            } else {
                // MODO MESTRE/DÚVIDA: Fila de Falha Crítica (Failover)
                listOf(
                    // 1. MiMo Pro (Main - Xiaomi)
                    AIConfig(BuildConfig.MESTRE_IA_MIMO_URL, BuildConfig.MESTRE_IA_MIMO_KEY, BuildConfig.MESTRE_IA_MIMO_MODEL_PRO),
                    // 2. MiMo Flash (Backup 1 - Xiaomi, fallback do Pro)
                    AIConfig(BuildConfig.MESTRE_IA_MIMO_URL, BuildConfig.MESTRE_IA_MIMO_KEY, BuildConfig.MESTRE_IA_MIMO_MODEL_FLASH),
                    // 3. DeepSeek (Backup 2 - Gratuito)
                    AIConfig(BuildConfig.MESTRE_IA_DEEPSEEK_URL, BuildConfig.MESTRE_IA_DEEPSEEK_2_KEY, BuildConfig.MESTRE_IA_DEEPSEEK_MODEL),
                    // 4. Gemini 3.1 Flash-Lite (Backup 3 - Rápido e Econômico)
                    AIConfig(BuildConfig.MESTRE_IA_LITE_1_URL, BuildConfig.MESTRE_IA_GEMINI_KEY, BuildConfig.MESTRE_IA_GEMINI_3_1_FLASH_LITE),
                    // 5. NVIDIA (Backup 4 - Llama 3.1)
                    AIConfig(BuildConfig.MESTRE_IA_NVIDIA_URL, BuildConfig.MESTRE_IA_NVIDIA_KEY, BuildConfig.MESTRE_IA_NVIDIA_MODEL),
                    // 6. OpenRouter (Backup 5 - Chave 1)
                    AIConfig(BuildConfig.MESTRE_IA_OPENROUTER_URL, BuildConfig.MESTRE_IA_OPENROUTER_1_KEY, BuildConfig.MESTRE_IA_OPENROUTER_MODEL_1),
                    // 7. OpenRouter (Backup 6 - Chave 2)
                    AIConfig(BuildConfig.MESTRE_IA_OPENROUTER_URL, BuildConfig.MESTRE_IA_OPENROUTER_2_KEY, BuildConfig.MESTRE_IA_OPENROUTER_MODEL_1)
                )
            }

            android.util.Log.i("MestreIA_RAG", "╔══ MESTRE IA INICIADO ══════════════════════════════")
            android.util.Log.i("MestreIA_RAG", "║  Pergunta: \"${prompt.take(100)}\"")
            android.util.Log.i("MestreIA_RAG", "║  Modo: $modo | Casual: $isCasual")

            val catalogoLocal = if (isCasual) {
                android.util.Log.i("MestreIA_RAG", "║  RAG: pulado (mensagem casual)")
                CatalogoLocalResult(MestreIAClient.CatalogoNomes(), false)
            } else {
                val plano = MestreIAPlanner.planejarBusca(prompt)
                android.util.Log.i("MestreIA_RAG", "║  Planner extraiu termos: ${plano.termos.take(8)}")
                gerarCatalogoDireto(prompt, viewModel.mestreIAChatHistory, plano.termos)
            }
            val isRagUsed = catalogoLocal.isRagSuccess
            val ctxChars = catalogoLocal.catalogo.ponteDeFerro.length
            val ctxChunks = catalogoLocal.catalogo.chunks.size
            if (isRagUsed) {
                android.util.Log.i("MestreIA_RAG", "║  RAG OK: $ctxChunks chunks | $ctxChars chars de contexto")
            } else {
                android.util.Log.e("MestreIA_RAG", "║  RAG VAZIO: contexto=0 chars — IA responderá SEM base no manual!")
            }
            var sucesso = false
            val errosAcumulados = mutableListOf<String>()
            
            // LOTE 122: ESTADO DE INVESTIGAÇÃO PERSISTENTE (Fora do loop de modelos)
            var catalogoDinamico = catalogoLocal.catalogo
            var promptAtual = prompt
            var historicoInvestigacao = mutableListOf<Pair<String, String>>()

            for (config in fila) {
                val iaUrl = config.url
                val iaKey = config.key
                val iaModel = config.model
                
                if (iaKey.isBlank()) {
                    errosAcumulados.add("$iaModel: Chave Vazia")
                    continue
                }

                try {
                    // Mescla o histórico do chat com as descobertas da investigação atual
                    val historicoLimitado = (viewModel.mestreIAChatHistory.map { it.role to it.text } + historicoInvestigacao).takeLast(12)
                    
                    var loopsRestantes = 3
                    var iteracao = 1
                    
                    while (loopsRestantes > 0) {
                        updateStatus(if (iteracao == 1) "Acionando Auditor $iaModel..." else "Refinando busca ($iaModel - iteração $iteracao)...")

                        val ctxAtual = catalogoDinamico.ponteDeFerro.length
                        val chunksAtual = catalogoDinamico.chunks.size
                        android.util.Log.i("MestreIA_RAG", "╠══ ITERAÇÃO $iteracao → $iaModel | ctx=${ctxAtual}chars / ${chunksAtual}chunks")

                        // Última iteração: força resposta final sem tool calls
                        if (loopsRestantes == 1 && iteracao > 1) {
                            promptAtual = "[RESPOSTA FINAL OBRIGATÓRIA] $promptAtual\n\nATENÇÃO: Esta é sua ÚLTIMA oportunidade de responder. Apresente sua conclusão agora com base no contexto já disponível. NÃO chame ferramentas. Se a regra encontrada for indireta (ex: uma fórmula, divisor ou modificador que implique o resultado), calcule e apresente o resultado para o jogador com a fonte [Livro, Pág]."
                            android.util.Log.i("MestreIA_RAG", "║  ÚLTIMA ITERAÇÃO: forçando resposta final")
                        }

                        val resposta = MestreIAClient.perguntarAoMestre(
                            baseUrl = iaUrl, apiKey = iaKey, workspaceSlug = iaModel,
                            prompt = promptAtual, history = historicoLimitado, contextoPersonagem = viewModel.personagem.toJson(),
                            catalogo = catalogoDinamico, modo = modo, onChunk = if (loopsRestantes == 1) sendChunk else null 
                        )

                        if (resposta.toolCalls.isNotEmpty()) {
                            val toolCall = resposta.toolCalls[0]
                            android.util.Log.i("MestreIA_RAG", "║  TOOL CALL: IA chamou [${toolCall.name}] query=\"${toolCall.args.optString("query","").take(70)}\"")

                            if (toolCall.name == MestreIATools.TOOL_MANUAL_DIRETO) {
                                val queryTool = toolCall.args.optString("query", prompt)

                                updateStatus("Vasculhando Códex: $queryTool...")
                                val resTool = graphEngine.buscarDiretoNoCodex(queryTool, emptyList())

                                if (resTool.relatedChunks.isNotEmpty()) {
                                    val novoTextoRegras = graphEngine.formatarParaIA(resTool)
                                    val ponteAtualizada = (catalogoDinamico.ponteDeFerro + "\n\n=== NOVAS REGRAS ENCONTRADAS ===\n" + novoTextoRegras).take(35000)
                                    val paginasTool = resTool.relatedChunks.mapNotNull { it.page_number }.distinct().sorted().joinToString()
                                    android.util.Log.i("MestreIA_RAG", "║  TOOL OK: ${resTool.relatedChunks.size} chunks adicionados | páginas: [$paginasTool] | ctx total: ${ponteAtualizada.length}chars")

                                    catalogoDinamico = catalogoDinamico.copy(
                                        ponteDeFerro = ponteAtualizada,
                                        chunks = (catalogoDinamico.chunks + resTool.relatedChunks).distinctBy { it.chunk_id }
                                    )

                                    historicoInvestigacao.add("assistant" to "Consultando manuais diretamente sobre '$queryTool'...")
                                    // CRÍTICO: guardar apenas resumo no histórico — contexto completo já está em catalogoDinamico.ponteDeFerro
                                    historicoInvestigacao.add("system" to "RESULTADO DA BUSCA DIRETA (resumo): ${novoTextoRegras.take(2000)}\n[Contexto completo já incorporado ao Códex disponível]")
                                    promptAtual = "Com base nestas regras encontradas, responda à dúvida. Se a regra for parcial ou analógica, deixe isso explícito. Se não houver nada útil, admita que não localizou nos manuais."
                                } else {
                                    android.util.Log.e("MestreIA_RAG", "║  TOOL VAZIO: nenhum chunk para \"${queryTool.take(60)}\" — IA sem contexto extra")
                                    historicoInvestigacao.add("system" to "Não foram encontrados resultados técnicos para '$queryTool' no Códex.")
                                    promptAtual = "Auditoria: Não localizei '$queryTool' nos manuais. INFORME ISSO AO JOGADOR. É terminantemente PROIBIDO usar conhecimento geral ou inventar regras. Diga que não achou e sugira que ele use outros termos de busca."
                                }
                                
                                loopsRestantes--
                                iteracao++
                                continue
                            }

                            if (toolCall.name == MestreIATools.TOOL_INSPECT_CHARACTER) {
                                val secao = toolCall.args.optString("secao", "atributos")
                                updateStatus("Lendo ficha ($secao)...")
                                
                                val infoFicha = when(secao) {
                                    "status" -> "PV: ${viewModel.personagem.pontosVidaRolagemAtual ?: viewModel.personagem.pontosVida}/${viewModel.personagem.pontosVida} | PF: ${viewModel.personagem.pontosFadigaRolagemAtual ?: viewModel.personagem.pontosFadiga}/${viewModel.personagem.pontosFadiga}"
                                    "vantagens" -> "Vantagens: " + viewModel.personagem.vantagens.joinToString { it.nome }
                                    "pericias" -> "Perícias: " + viewModel.personagem.pericias.joinToString { "${it.nome} (NH ${it.calcularNivel(viewModel.personagem)})" }
                                    else -> "Atributos: ST ${viewModel.personagem.st}, DX ${viewModel.personagem.dx}, IQ ${viewModel.personagem.iq}, HT ${viewModel.personagem.ht}"
                                }
                                
                                historicoInvestigacao.add("system" to "DADOS DA FICHA ($secao): $infoFicha")
                                loopsRestantes--
                                iteracao++
                                continue
                            }

                            if (toolCall.name == MestreIATools.TOOL_NEXUS_ARCANO) {
                                val magiaAlvo = toolCall.args.optString("magia_alvo", prompt)
                                updateStatus("Nexus: $magiaAlvo...")
                                val gabarito = nexusEngine.formatarGabaritoParaIA(magiaAlvo)
                                catalogoDinamico = catalogoDinamico.copy(
                                    ponteDeFerro = (catalogoDinamico.ponteDeFerro + "\n\n=== NEXUS ===\n" + gabarito).take(35000)
                                )
                                loopsRestantes--
                                iteracao++
                                continue
                            }
                        }

                        if (resposta.text.isNotBlank() && !resposta.text.startsWith("Erro")) {
                            val lowerRes = resposta.text.lowercase()
                            if ((lowerRes.contains("lamento") || lowerRes.contains("não encontrei")) && loopsRestantes > 1) {
                                promptAtual = "Pergunta: $prompt\n\nNão desista. Tente buscar termos mais genéricos no manual usando '${MestreIATools.TOOL_MANUAL_DIRETO}'."
                                loopsRestantes--
                                iteracao++
                                continue
                            }

                            val temCitacao = resposta.text.contains("[") && (resposta.text.contains("Pág", true) || resposta.text.contains("Pg", true))
                            val respostaFinal = if (!temCitacao && iteracao > 1) {
                                resposta.text + "\n\n _[AUDITORIA: Sem citações diretas do manual]_"
                            } else {
                                resposta.text
                            }
                            
                            android.util.Log.i("MestreIA_RAG", "╚══ RESPOSTA OK [$iaModel] | iter=$iteracao | ${respostaFinal.length}chars | citação=${temCitacao}")
                            sendResult(isRagUsed, resposta.copy(text = respostaFinal, modelName = iaModel))
                            sucesso = true
                        } else {
                            android.util.Log.e("MestreIA_RAG", "╠── MODELO FALHOU: $iaModel | resposta=\"${resposta.text.take(50)}\"")
                            errosAcumulados.add("${iaModel.takeLast(10)}: ${resposta.text.take(30)}")
                        }
                        break
                    }

                    if (sucesso) break

                } catch (e: Exception) {
                    errosAcumulados.add("${iaModel.takeLast(10)}: ${e.message?.take(20)}")
                }
            }
            if (!sucesso) {
                android.util.Log.e("MestreIA_RAG", "╚══ FALHA TOTAL: esgotou ${fila.size} modelos | erros: ${errosAcumulados.joinToString(" | ")}")
                val resumoErro = buildString {
                    append(" O Mestre IA esgotou todas as tentativas de conexão.\n\n")
                    append("DETALHES DO FRACASSO:\n")
                    errosAcumulados.forEach { append("- $it\n") }
                    append("\nIsso geralmente ocorre por falta de créditos (402) ou limite de cota (429) em modelos gratuitos. Por favor, tente novamente mais tarde ou recarregue seus créditos nas APIs.")
                }
                sendResult(false, MestreIAClient.ChatResponse(resumoErro))
            }
        }
    }

    /**
     * LOTE 119: Gerador de Catálogo via Busca Direta (Pula o Grafo).
     */
    suspend fun gerarCatalogoDireto(prompt: String, history: List<MestreIAClient.ChatMessage>, termosExtras: List<String> = emptyList()): CatalogoLocalResult {
        // LOTE 126: Simplificamos a busca para evitar duplicação de termos no FTS
        val promptExpandido = prompt.take(500)
        
        val res = graphEngine.buscarDiretoNoCodex(promptExpandido, termosExtras)
        
        val cat = MestreIAClient.CatalogoNomes(
            chunks = res.relatedChunks,
            ponteDeFerro = graphEngine.formatarParaIA(res).take(35000)
        )
        return CatalogoLocalResult(cat, res.relatedChunks.isNotEmpty())
    }


    private fun traduzirModeloParaMestre(id: String): String = when {
        id.contains("qwen") -> "Estrategista"
        id.contains("gemini") -> "Mensageiro"
        else -> "IA"
    }

    fun extrairJsonDeNarrativa(texto: String): String? = MestreIAClient.extrairJsonFicha(texto)?.let { "..." } 
    fun limparNarrativaParaChat(texto: String): String = texto.replace(Regex("```json.*?```", RegexOption.DOT_MATCHES_ALL), "").trim()

    data class CatalogoLocalResult(val catalogo: MestreIAClient.CatalogoNomes, val isRagSuccess: Boolean)
}
