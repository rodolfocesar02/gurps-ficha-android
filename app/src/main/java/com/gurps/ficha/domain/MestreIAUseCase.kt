package com.gurps.ficha.domain

import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    fun conversarComMestreIA(
        prompt: String,
        modo: String = "conversa",
        onStatusUpdate: (String) -> Unit = {},
        onChunk: (String) -> Unit = {},
        onResultado: (Boolean, MestreIAClient.ChatResponse) -> Unit
    ) {
        viewModelScope.launch {
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
            
            
            val catalogoLocal = if (isCasual) {
                CatalogoLocalResult(MestreIAClient.CatalogoNomes(), false)
            } else {
                gerarCatalogoLocal(prompt, viewModel.mestreIAChatHistory)
            }
            val isRagUsed = catalogoLocal.isRagSuccess
            //Esse recado foi fixado pelo Rodolfo, voc~e nao tem mais permissão pra trocar modelos sem minha permissão!
            // Fila de Auditores (DETERMINÍSTICO: MODELOS ATIVOS 2026)
            val fila = listOf(
                // 1. TENTATIVA 1: O modelo atual e ativo (Flash Lite)
                Triple(BuildConfig.MESTRE_IA_LITE_1_URL, BuildConfig.MESTRE_IA_GEMINI_KEY, "gemini-3.1-flash-lite-preview"),
                // 2. TENTATIVA 2: Versão estável da Família 3 (Substituto do antigo 1.5)
                Triple(BuildConfig.MESTRE_IA_LITE_1_URL, BuildConfig.MESTRE_IA_GEMINI_KEY, "gemini-3-flash-preview")
            )

            var sucesso = false
            val errosAcumulados = mutableListOf<String>()

            for (config in fila) {
                if (config.second.isBlank()) {
                    errosAcumulados.add("${config.third}: Chave Vazia")
                    continue
                }
                val modelId = config.third
                try {
                    val historicoLimitado = viewModel.mestreIAChatHistory.map { it.role to it.text }.takeLast(10)
                    // LOTE 89.15: LOOP DE INVESTIGAÇÃO AGÊNTICA (Mini-Antigravity)
                    var loopsRestantes = 3
                    var promptInvestigacao = prompt
                    var iteracao = 1
                    
                    while (loopsRestantes > 0) {
                        updateStatus(if (iteracao == 1) "Iniciando Auditor $modelId..." else "Refinando busca (Iteração $iteracao)...")
                        
                        val resposta = MestreIAClient.perguntarAoMestre(
                            baseUrl = config.first, apiKey = config.second, workspaceSlug = config.third,
                            prompt = promptInvestigacao, history = historicoLimitado, contextoPersonagem = viewModel.personagem.toJson(),
                            catalogo = catalogoLocal.catalogo, modo = modo, onChunk = if (loopsRestantes == 1) sendChunk else null 
                        )

                        if (resposta.toolCalls.isNotEmpty()) {
                            val toolCall = resposta.toolCalls[0]
                            if (toolCall.name == "consultar_grafo_regras") {
                                val queryTool = toolCall.args.optString("query", prompt)
                                val paginaTool = toolCall.args.optInt("pagina", 1)
                                val offset = (paginaTool - 1).coerceAtLeast(0)
                                
                                updateStatus("Pesquisando no Códex: $queryTool (Pág. $paginaTool)...")
                                
                                val resTool = graphEngine.buscarNoGrafo(queryTool, offset)
                                val contextoExtra = if (resTool.relatedChunks.isNotEmpty()) {
                                    "<CONTEXTO_TECNICO>\n" + 
                                    resTool.relatedChunks.take(5).joinToString("\n") { 
                                        "[${it.source_title}, Pág. ${it.page_number}]: ${it.text}" 
                                    } + "\n</CONTEXTO_TECNICO>"
                                } else {
                                    "NENHUMA REGRA ENCONTRADA no manual para '$queryTool'. Não tente inventar a regra."
                                }
                                
                                promptInvestigacao = "Pergunta Original: $prompt\n\nUse APENAS este contexto para responder:\n$contextoExtra\n\nSe a resposta estiver no contexto, responda citando a página. Se não, diga que não encontrou."
                                loopsRestantes--
                                iteracao++
                                continue
                            }
                        }

                        if (resposta.text.isNotBlank() && !resposta.text.startsWith("Erro")) {
                            // LOTE 101: Validador de Citações (O "X9" da IA)
                            val temCitacao = resposta.text.contains("[") && (resposta.text.contains("Pág", true) || resposta.text.contains("Pg", true))
                            val respostaFinal = if (!temCitacao && iteracao > 1) {
                                resposta.text + "\n\n⚠️ _[AUDITORIA: Esta resposta não citou fontes do manual e pode conter imprecisões]_"
                            } else {
                                resposta.text
                            }
                            sendResult(isRagUsed, resposta.copy(text = respostaFinal, modelName = modelId))
                            sucesso = true
                        } else {
                            errosAcumulados.add("${modelId.takeLast(10)}: ${resposta.text.take(30)}")
                        }
                        break
                    }

                    if (sucesso) break

                } catch (e: Exception) {
                    errosAcumulados.add("${modelId.takeLast(10)}: ${e.message?.take(20)}")
                }
            }
            if (!sucesso) {
                val msgErro = "Falha Geral. Detalhes: " + errosAcumulados.joinToString(" | ")
                sendResult(false, MestreIAClient.ChatResponse(msgErro))
            }
        }
    }

    suspend fun gerarCatalogoLocal(prompt: String, history: List<MestreIAClient.ChatMessage>): CatalogoLocalResult {
        val contextoRecente = history.takeLast(3).joinToString(" ") { if (it.role == "user") it.text else "" }
        val promptExpandido = "$prompt $contextoRecente".take(500)
        
        var res = graphEngine.buscarNoGrafo(promptExpandido)
        
        // LOTE 94: Fallback genérico desativado para evitar inchaço no prompt (Erro 503).
        // A busca agora confia exclusivamente no mapeamento do Grafo e na Ponte de Página.

        val detalhesItens = mutableListOf<String>()
        
        // LOTE 89.28: FUNIL DE CATÁLOGOS (Roteamento para os JSONs oficiais)
        res.summaries.forEach { node ->
            when {
                node.category.contains("Perícia", true) -> {
                    repository.periciasSuplementares.find { it.nome.equals(node.title, true) }?.let {
                        detalhesItens.add("[PERÍCIA: ${it.nome}]: Dific: ${it.dificuldadeRaw}, Pág. ${it.pagina}\nDesc: ${it.descricao}")
                    }
                }
                node.category.contains("Vantagem", true) -> {
                    repository.vantagens.find { it.nome.equals(node.title, true) }?.let {
                        detalhesItens.add("[VANTAGEM: ${it.nome}]: Custo: ${it.getCustoBase()}, Pág. ${it.pagina}\nDesc: ${it.descricao}")
                    }
                }
                node.category.contains("Desvantagem", true) -> {
                    repository.desvantagens.find { it.nome.equals(node.title, true) }?.let {
                        detalhesItens.add("[DESVANTAGEM: ${it.nome}]: Custo: ${it.getCustoBase()}, Pág. ${it.pagina}\nDesc: ${it.descricao}")
                    }
                }
                node.category.contains("Magia", true) -> {
                    repository.magias.find { it.nome.equals(node.title, true) }?.let {
                        detalhesItens.add("[MAGIA: ${it.nome}]: Energia: ${it.energia}, Tempo: ${it.tempoOperacao}, Duração: ${it.duracao}, Pág. ${it.pagina}\nDesc: ${it.texto ?: it.descricao}")
                    }
                }
                node.category.contains("Técnica", true) -> {
                    repository.tecnicasCatalogo.find { it.nome.equals(node.title, true) }?.let {
                        detalhesItens.add("[TÉCNICA: ${it.nome}]: Dific: ${it.dificuldadeRaw}, PreDef: ${it.preDefinidoRaw}, Pág. ${it.pagina}\nDesc: ${it.descricao}")
                    }
                }
                node.category.contains("Arma", true) -> {
                    repository.armasCatalogo.find { it.nome.equals(node.title, true) }?.let {
                        detalhesItens.add("[ARMA: ${it.nome}]: Dano: ${it.danoRaw}, StMin: ${it.stMinimo}\nObs: ${it.observacoes}")
                    }
                }
                node.category.contains("Armadura", true) || node.category.contains("Escudo", true) -> {
                    repository.armadurasCatalogo.find { it.nome.equals(node.title, true) }?.let {
                        detalhesItens.add("[ARMADURA: ${it.nome}]: RD: ${it.rd}, Peso: ${it.pesoBaseKg}kg\nObs: ${it.observacoes}")
                    }
                }
                node.category.contains("Modificador", true) || node.category.contains("Ampliação", true) || node.category.contains("Limitação", true) -> {
                    repository.modificadoresGerais.find { it.nome.equals(node.title, true) }?.let {
                        detalhesItens.add("[MODIFICADOR: ${it.nome}]: Valor: ${it.valor}, Pág. ${it.pagina}\nDesc: ${it.descricao}")
                    }
                }
                node.category.contains("Equipamento", true) -> {
                    val item = repository.armasCatalogo.find { it.nome.equals(node.title, true) } 
                        ?: repository.armadurasCatalogo.find { it.nome.equals(node.title, true) }
                    item?.let { detalhesItens.add("[EQUIPAMENTO: ${node.title}]: Encontrado nos catálogos técnicos.") }
                }
                node.category.contains("Regra", true) -> {
                    detalhesItens.add("[REGRA: ${node.title}]: ${node.summary}")
                }
            }
        }

        val cat = MestreIAClient.CatalogoNomes(
            vantagens = res.summaries.filter { it.category.contains("Vantagem", true) }.map { it.title },
            pericias = res.summaries.filter { it.category.contains("Perícia", true) }.map { it.title },
            tecnicas = res.summaries.filter { it.category.contains("Técnica", true) }.map { it.title },
            magias = res.summaries.filter { it.category.contains("Magia", true) }.map { it.title },
            equipamentos = res.summaries.filter { it.category.contains("Equipamento", true) }.map { it.title },
            armas = res.summaries.filter { it.category.contains("Arma", true) }.map { it.title },
            itensDetalhes = detalhesItens,
            chunks = res.relatedChunks,
            summaries = res.summaries,
            ponteDeFerro = graphEngine.formatarParaIA(res).take(60000) // LOTE 104: Liberado para 60k chars para contexto total de regras subaquáticas/complexas
        )
        return CatalogoLocalResult(cat, res.relatedChunks.isNotEmpty() || res.summaries.isNotEmpty())
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
