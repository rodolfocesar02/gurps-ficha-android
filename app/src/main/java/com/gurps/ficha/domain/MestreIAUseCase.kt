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
            
            // LOTE 126: TESTE DE ISOLAMENTO (DeepSeek Only - Sem Fallback)
            val fila = if (modo == "geracao" || modo == "analise") {
                // Modo Criar: Chave PAGA (sk-f1e7...)
                listOf(AIConfig(BuildConfig.MESTRE_IA_DEEPSEEK_URL, BuildConfig.MESTRE_IA_DEEPSEEK_KEY, "deepseek-chat"))
            } else {
                // Modo Dúvida: Chave GRATUITA/TESTE (sk-a6c4...)
                listOf(AIConfig(BuildConfig.MESTRE_IA_DEEPSEEK_URL, BuildConfig.MESTRE_IA_DEEPSEEK_2_KEY, "deepseek-chat"))
            }

            val catalogoLocal = if (isCasual) {
                CatalogoLocalResult(MestreIAClient.CatalogoNomes(), false)
            } else {
                // Fila de Planejadores (Usa o primeiro modelo disponível para planejar)
                val configPlanejador = fila.first()
                updateStatus("Planejando estratégia de busca...")
                val plano = MestreIAPlanner.planejarBusca(prompt, configPlanejador.url, configPlanejador.key, configPlanejador.model)
                
                gerarCatalogoLocal(prompt, viewModel.mestreIAChatHistory, plano.termos)
            }
            val isRagUsed = catalogoLocal.isRagSuccess
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
                        
                        android.util.Log.i("MestreIA_Auditoria", "📤 ENVIANDO PROMPT ($iaModel | Iteração $iteracao) | Contexto: ${promptAtual.take(100)}...")

                        val resposta = MestreIAClient.perguntarAoMestre(
                            baseUrl = iaUrl, apiKey = iaKey, workspaceSlug = iaModel,
                            prompt = promptAtual, history = historicoLimitado, contextoPersonagem = viewModel.personagem.toJson(),
                            catalogo = catalogoDinamico, modo = modo, onChunk = if (loopsRestantes == 1) sendChunk else null 
                        )

                        if (resposta.toolCalls.isNotEmpty()) {
                            val toolCall = resposta.toolCalls[0]
                            android.util.Log.i("MestreIA_Auditoria", "🔍 INVESTIGAÇÃO: A IA chamou ${toolCall.name} com: ${toolCall.args}")

                            if (toolCall.name == "consultar_grafo_regras") {
                                val queryTool = toolCall.args.optString("query", prompt)
                                val paginaTool = toolCall.args.optInt("pagina", 1)
                                val offset = (paginaTool - 1).coerceAtLeast(0)
                                
                                updateStatus("Lendo Códex: $queryTool...")
                                val resTool = graphEngine.buscarNoGrafo(queryTool, offset, emptyList())
                                
                                if (resTool.relatedChunks.isNotEmpty()) {
                                    val novoTextoRegras = graphEngine.formatarParaIA(resTool)
                                    val ponteAtualizada = (catalogoDinamico.ponteDeFerro + "\n\n=== REGRAS ADICIONAIS ===\n" + novoTextoRegras).take(35000)
                                    
                                    android.util.Log.i("MestreIA_Auditoria", "📖 CÓDEX: Encontrados ${resTool.relatedChunks.size} recortes. Injetando na memória...")
                                    
                                    catalogoDinamico = catalogoDinamico.copy(
                                        ponteDeFerro = ponteAtualizada,
                                        chunks = (catalogoDinamico.chunks + resTool.relatedChunks).distinctBy { it.chunk_id }
                                    )
                                    
                                    android.util.Log.d("MestreIA_Auditoria", "🧠 MEMÓRIA ATUALIZADA: Ponte de Ferro agora com ${ponteAtualizada.length} caracteres.")
                                    
                                    historicoInvestigacao.add("assistant" to "Consultando manual sobre '$queryTool'...")
                                    historicoInvestigacao.add("system" to "RESULTADO DA BUSCA: $novoTextoRegras")
                                    promptAtual = "Com base nas novas regras injetadas no contexto, responda: $prompt"
                                } else {
                                    android.util.Log.w("MestreIA_Auditoria", "❌ CÓDEX: Nenhuma regra encontrada para '$queryTool'.")
                                }
                                
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
                                promptAtual = "Pergunta: $prompt\n\nNão desista. Tente buscar termos mais genéricos no manual usando 'consultar_grafo_regras'."
                                loopsRestantes--
                                iteracao++
                                continue
                            }

                            val temCitacao = resposta.text.contains("[") && (resposta.text.contains("Pág", true) || resposta.text.contains("Pg", true))
                            val respostaFinal = if (!temCitacao && iteracao > 1) {
                                resposta.text + "\n\n⚠️ _[AUDITORIA: Sem citações diretas do manual]_"
                            } else {
                                resposta.text
                            }
                            
                            sendResult(isRagUsed, resposta.copy(text = respostaFinal, modelName = iaModel))
                            sucesso = true
                        } else {
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
                val msgErro = "Falha Geral. Detalhes: " + errosAcumulados.joinToString(" | ")
                sendResult(false, MestreIAClient.ChatResponse(msgErro))
            }
        }
    }

    suspend fun gerarCatalogoLocal(prompt: String, history: List<MestreIAClient.ChatMessage>, termosExtras: List<String> = emptyList()): CatalogoLocalResult {
        val contextoRecente = history.takeLast(3).joinToString(" ") { if (it.role == "user") it.text else "" }
        val promptExpandido = "$prompt $contextoRecente".take(500)
        
        var res = graphEngine.buscarNoGrafo(promptExpandido, 0, termosExtras)
        
        // LOTE 94: Fallback genérico desativado para evitar inchaço no prompt (Erro 503).
        // A busca agora confia exclusivamente no mapeamento do Grafo e na Ponte de Página.

        val detalhesItens = mutableListOf<String>()
        
        // LOTE 117: FUNIL DE CATÁLOGOS (Roteamento para os JSONs oficiais)
        res.summaries.forEach { node ->
            val jaAdicionado = when {
                node.category.contains("Regra", true) -> {
                    detalhesItens.add("[REGRA: ${node.title}]:\n${node.summary}")
                    true
                }
                node.category.contains("Perícia", true) -> {
                    repository.periciasSuplementares.find { it.nome.equals(node.title, true) }?.let {
                        detalhesItens.add("[PERÍCIA: ${it.nome}]: Dific: ${it.dificuldadeRaw}, Pág. ${it.pagina}\nDesc: ${it.descricao}")
                        true
                    } ?: false
                }
                node.category.contains("Vantagem", true) -> {
                    repository.vantagens.find { it.nome.equals(node.title, true) }?.let {
                        detalhesItens.add("[VANTAGEM: ${it.nome}]: Custo: ${it.getCustoBase()}, Pág. ${it.pagina}\nDesc: ${it.descricao}")
                        true
                    } ?: false
                }
                node.category.contains("Desvantagem", true) -> {
                    repository.desvantagens.find { it.nome.equals(node.title, true) }?.let {
                        detalhesItens.add("[DESVANTAGEM: ${it.nome}]: Custo: ${it.getCustoBase()}, Pág. ${it.pagina}\nDesc: ${it.descricao}")
                        true
                    } ?: false
                }
                node.category.contains("Magia", true) -> {
                    repository.magias.find { it.nome.equals(node.title, true) }?.let {
                        val escolas = it.escola?.joinToString(", ") ?: "Nenhuma"
                        detalhesItens.add("[MAGIA: ${it.nome}]: Escola: $escolas, Energia: ${it.energia}, Tempo: ${it.tempoOperacao}, Duração: ${it.duracao}, Pré-Requisitos: ${it.preRequisitos ?: "Nenhum"}, Pág. ${it.pagina}\nDesc: ${it.texto ?: it.descricao}")
                        true
                    } ?: false
                }
                node.category.contains("Técnica", true) -> {
                    repository.tecnicasCatalogo.find { tec -> tec.nome.equals(node.title, true) }?.let { item ->
                        detalhesItens.add("[TÉCNICA: ${item.nome}]: Dific: ${item.dificuldadeRaw}, PreDef: ${item.preDefinidoRaw}, Pág. ${item.pagina}\nDesc: ${item.descricao}")
                        true
                    } ?: false
                }
                node.category.contains("Arma", true) -> {
                    repository.armasCatalogo.find { arma -> arma.nome.equals(node.title, true) }?.let { item ->
                        detalhesItens.add("[ARMA: ${item.nome}]: Dano: ${item.danoRaw}, StMin: ${item.stMinimo}\nObs: ${item.observacoes}")
                        true
                    } ?: false
                }
                node.category.contains("Armadura", true) || node.category.contains("Escudo", true) -> {
                    repository.armadurasCatalogo.find { arm -> arm.nome.equals(node.title, true) }?.let { item ->
                        detalhesItens.add("[ARMADURA: ${item.nome}]: RD: ${item.rd}, Peso: ${item.pesoBaseKg}kg\nObs: ${item.observacoes}")
                        true
                    } ?: false
                }
                node.category.contains("Modificador", true) || node.category.contains("Ampliação", true) || node.category.contains("Limitação", true) -> {
                    repository.modificadoresGerais.find { mod -> mod.nome.equals(node.title, true) }?.let { item ->
                        detalhesItens.add("[MODIFICADOR: ${item.nome}]: Valor: ${item.valor}, Pág. ${item.pagina}\nDesc: ${item.descricao}")
                        true
                    } ?: false
                }
                node.category.contains("Equipamento", true) -> {
                    repository.armasCatalogo.find { eq -> eq.nome.equals(node.title, true) }?.let { item ->
                        detalhesItens.add("[EQUIPAMENTO: ${item.nome}]: Obs: ${item.observacoes}")
                        true
                    } ?: false
                }
                else -> false
            }

            // Fallback: Se não encontrou no catálogo específico, usa o resumo do Grafo (Vital para Regras Customizadas)
            if (!jaAdicionado) {
                detalhesItens.add("[${node.category.uppercase()}: ${node.title}]:\n${node.summary}")
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
            ponteDeFerro = graphEngine.formatarParaIA(res).take(30000) // LOTE 104: Reduzido para 30k para maior precisão e economia de tokens
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
