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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    private fun ehErroDeApi(texto: String): Boolean {
        val t = texto.trimStart()
        return Regex("^Erro \\d{3}:").containsMatchIn(t) ||
            t.startsWith("Erro de Conexão:") ||
            t.startsWith("Erro de API") ||
            t.startsWith("Erro: Resposta vazia") ||
            t.startsWith("Erro: Modo Stream") ||
            t.startsWith("Erro: Falha na conexão")
    }

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
                // LOTE 127: Reordenado com base em análise de logcat — MiMo loopava sem responder
                listOf(
                    // 1. DeepSeek Pago (Main — teste com chave paga igual ao Forjador)
                    AIConfig(BuildConfig.MESTRE_IA_DEEPSEEK_URL, BuildConfig.MESTRE_IA_DEEPSEEK_KEY, BuildConfig.MESTRE_IA_DEEPSEEK_MODEL),
                    // 2. Gemini 3.1 Flash-Lite (Backup 1 — rápido e econômico)
                    AIConfig(BuildConfig.MESTRE_IA_LITE_1_URL, BuildConfig.MESTRE_IA_GEMINI_KEY, BuildConfig.MESTRE_IA_GEMINI_3_1_FLASH_LITE),
                    // 3. MiMo Pro (Backup 2 — Xiaomi, loop-prone)
                    AIConfig(BuildConfig.MESTRE_IA_MIMO_URL, BuildConfig.MESTRE_IA_MIMO_KEY, BuildConfig.MESTRE_IA_MIMO_MODEL_PRO),
                    // 4. MiMo Flash (Backup 3 — Xiaomi, loop-prone)
                    AIConfig(BuildConfig.MESTRE_IA_MIMO_URL, BuildConfig.MESTRE_IA_MIMO_KEY, BuildConfig.MESTRE_IA_MIMO_MODEL_FLASH),
                    // 5. NVIDIA (Backup 4 — Llama 3.3 70B)
                    AIConfig(BuildConfig.MESTRE_IA_NVIDIA_URL, BuildConfig.MESTRE_IA_NVIDIA_KEY, BuildConfig.MESTRE_IA_NVIDIA_MODEL),
                    // 6. OpenRouter (Backup 5 — Chave 1)
                    AIConfig(BuildConfig.MESTRE_IA_OPENROUTER_URL, BuildConfig.MESTRE_IA_OPENROUTER_1_KEY, BuildConfig.MESTRE_IA_OPENROUTER_MODEL_1),
                    // 7. OpenRouter (Backup 6 — Chave 2)
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
                updateStatus("Analisando pergunta...")
                // LOTE 130: passa inventário do personagem para o Planner cruzar com a pergunta
                val plano = MestreIAPlanner.planejarBusca(prompt, viewModel.personagem.equipamentos)
                android.util.Log.i("MestreIA_RAG", "║  Planner extraiu termos: ${plano.termos.take(8)} | livros: ${plano.livrosRelevantes}")
                updateStatus("Consultando o manual...")
                var resultado = gerarCatalogoDireto(prompt, viewModel.mestreIAChatHistory, plano.termos)

                // Injeta contexto do inventário do personagem antes do RAG geral
                if (plano.contextoEquipamentos.isNotEmpty()) {
                    val secaoInventario = "=== EQUIPAMENTO DO PERSONAGEM (inventário) ===\n${plano.contextoEquipamentos}\n"
                    val ponteComInventario = (secaoInventario + resultado.catalogo.ponteDeFerro).take(35000)
                    resultado = CatalogoLocalResult(
                        resultado.catalogo.copy(ponteDeFerro = ponteComInventario),
                        resultado.isRagSuccess
                    )
                    android.util.Log.i("MestreIA_RAG", "║  INVENTÁRIO: equipamento(s) do personagem injetado(s) no contexto")
                }

                // LOTE 131 (Paralelo): Pré-busca de stats de equipamentos em paralelo
                if (plano.subQueriesStats.isNotEmpty()) {
                    android.util.Log.i("MestreIA_RAG", "║  PRÉ-STATS: ${plano.subQueriesStats.size} equipamento(s) detectado(s) — buscando em paralelo")
                    updateStatus("Verificando stats: ${plano.subQueriesStats.joinToString { it.take(20) }}...")
                    val statsResultados = coroutineScope {
                        plano.subQueriesStats.map { statsQuery ->
                            async { statsQuery to graphEngine.buscarDiretoNoCodex(statsQuery, emptyList()) }
                        }.awaitAll()
                    }
                    var ponte = resultado.catalogo.ponteDeFerro
                    var chunks = resultado.catalogo.chunks.toMutableList()
                    for ((statsQuery, statsRes) in statsResultados) {
                        if (statsRes.relatedChunks.isNotEmpty()) {
                            val statsTxt = graphEngine.formatarParaIA(statsRes)
                            ponte = (ponte + "\n\n=== STATS DO EQUIPAMENTO (tabela) ===\n" + statsTxt).take(35000)
                            chunks = (chunks + statsRes.relatedChunks).distinctBy { it.chunk_id }.toMutableList()
                            android.util.Log.i("MestreIA_RAG", "║  PRÉ-STATS OK: \"${statsQuery.take(50)}\" → ${statsRes.relatedChunks.size} chunks")
                        } else {
                            android.util.Log.w("MestreIA_RAG", "║  PRÉ-STATS VAZIO: \"${statsQuery.take(50)}\"")
                        }
                    }
                    resultado = CatalogoLocalResult(
                        resultado.catalogo.copy(ponteDeFerro = ponte, chunks = chunks),
                        resultado.isRagSuccess
                    )
                }

                // MULTI-QUERY TEMÁTICO: busca paralela por ângulos temáticos da pergunta.
                // Chunks que aparecem em múltiplas sub-queries recebem bonus de relevância,
                // pois sua presença em vários contextos indica alta relevância para a pergunta.
                if (plano.subQueriesTemáticas.isNotEmpty()) {
                    android.util.Log.i("MestreIA_RAG", "║  MULTI-QUERY: ${plano.subQueriesTemáticas.size} ângulos temáticos — buscando em paralelo")
                    updateStatus("Buscando regras relacionadas...")
                    val tematicasResultados = coroutineScope {
                        plano.subQueriesTemáticas.map { tQuery ->
                            async { tQuery to graphEngine.buscarDiretoNoCodex(tQuery, emptyList()) }
                        }.awaitAll()
                    }
                    // Conta em quantas sub-queries cada chunk_id apareceu
                    val contagemChunk = mutableMapOf<String, Int>()
                    val todosChunksPorId = mutableMapOf<String, MestreIAChunk>()
                    for ((_, tRes) in tematicasResultados) {
                        tRes.relatedChunks.forEach { c ->
                            contagemChunk[c.chunk_id] = (contagemChunk[c.chunk_id] ?: 0) + 1
                            todosChunksPorId[c.chunk_id] = c
                        }
                    }
                    // Chunks que aparecem em 2+ sub-queries são promovidos (cross-query bonus)
                    val chunksPromovidos = todosChunksPorId.values
                        .filter { (contagemChunk[it.chunk_id] ?: 0) >= 2 }
                        .sortedByDescending { contagemChunk[it.chunk_id] ?: 0 }
                        .take(10)

                    val chunksTodos = todosChunksPorId.values.toList()
                    val qtdNovos = chunksTodos.count { it.chunk_id !in resultado.catalogo.chunks.map { c -> c.chunk_id }.toSet() }
                    android.util.Log.i("MestreIA_RAG", "║  MULTI-QUERY OK: ${chunksTodos.size} chunks totais | ${chunksPromovidos.size} em 2+ queries (cross-bonus) | $qtdNovos novos")

                    // Injeta chunks promovidos no início do contexto (maior visibilidade para a IA)
                    val chunksAtuais = resultado.catalogo.chunks.toMutableList()
                    val chunksIdsAtuais = chunksAtuais.map { it.chunk_id }.toSet()
                    val chunksNovosPromovidos = chunksPromovidos.filter { it.chunk_id !in chunksIdsAtuais }
                    val chunksNovosComuns = chunksTodos.filter { it.chunk_id !in chunksIdsAtuais && it !in chunksNovosPromovidos }.take(5)

                    if (chunksNovosPromovidos.isNotEmpty() || chunksNovosComuns.isNotEmpty()) {
                        val novosFormatados = graphEngine.formatarParaIA(
                            MestreIAGraphEngine.GraphSearchResult(
                                relatedChunks = chunksNovosPromovidos + chunksNovosComuns,
                                chunkScores = contagemChunk.mapValues { (_, count) -> count * 500.0 }
                            )
                        )
                        val ponteAtualizada = (resultado.catalogo.ponteDeFerro + "\n\n=== REGRAS ADICIONAIS (multi-query) ===\n" + novosFormatados).take(35000)
                        resultado = CatalogoLocalResult(
                            resultado.catalogo.copy(
                                ponteDeFerro = ponteAtualizada,
                                chunks = (chunksNovosPromovidos + chunksNovosComuns + chunksAtuais).distinctBy { it.chunk_id }
                            ),
                            resultado.isRagSuccess || chunksTodos.isNotEmpty()
                        )
                    }
                }

                resultado
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

                // LOTE 133: Handoff de contexto — novo modelo recebe tudo que o anterior pesquisou
                if (historicoInvestigacao.isNotEmpty()) {
                    android.util.Log.i("MestreIA_RAG", "║  HANDOFF → $iaModel: ${historicoInvestigacao.size} entradas preservadas | ctx=${catalogoDinamico.ponteDeFerro.length}chars / ${catalogoDinamico.chunks.size}chunks")
                    promptAtual = prompt  // reset para pergunta original — histórico já tem todas as pesquisas
                }

                try {
                    // Mescla o histórico do chat com as descobertas da investigação atual
                    val historicoLimitado = (viewModel.mestreIAChatHistory.map { it.role to it.text } + historicoInvestigacao).takeLast(12)
                    
                    // Pergunta "complexa" = tem número, alcance, cálculo, arma, penalidade → ativa protocolo 3 fases + Thinking
                    val isQuestaoComplexa = !isCasual && (
                        prompt.contains(Regex("\\d+\\s*(m|metro|hex|km|yard)|alcance|calculo|penalidade|submerso|agua|piscina|quanto de dano|cai|queda|velocidade", RegexOption.IGNORE_CASE))
                        || prompt.length > 80
                    )
                    // Simples = 1 tool call max + sem Thinking; Complexa = 3 iterações + Thinking
                    var loopsRestantes = if (isQuestaoComplexa) 3 else 2
                    var iteracao = 1

                    while (loopsRestantes > 0) {
                        val isUltimaIteracao = loopsRestantes == 1 && iteracao > 1

                        val modeloCurto = iaModel.substringAfterLast("/").take(20)
                        updateStatus(when {
                            iteracao == 1 && isQuestaoComplexa -> "Analisando regras (${catalogoDinamico.chunks.size} chunks encontrados)..."
                            iteracao == 1 -> "Preparando resposta..."
                            isUltimaIteracao -> "Elaborando resposta final..."
                            else -> "Verificando regras adicionais..."
                        })

                        val ctxAtual = catalogoDinamico.ponteDeFerro.length
                        val chunksAtual = catalogoDinamico.chunks.size
                        android.util.Log.i("MestreIA_RAG", "╠══ ITERAÇÃO $iteracao → $iaModel | ctx=${ctxAtual}chars / ${chunksAtual}chunks | desativarTools=$isUltimaIteracao | complexa=$isQuestaoComplexa")

                        // PROTOCOLO 3 FASES: Iteração 1 de questão complexa → identificar lacunas, NÃO responder ainda
                        if (iteracao == 1 && isQuestaoComplexa && !isUltimaIteracao) {
                            promptAtual = "[FASE 1 — INVESTIGAÇÃO] $prompt\n\n" +
                                "PROTOCOLO OBRIGATÓRIO:\n" +
                                "1. Leia os chunks disponíveis no Códex.\n" +
                                "2. Identifique quais informações ainda estão FALTANDO para responder completamente (ex: stat de alcance da arma, penalidade específica, fórmula de cálculo).\n" +
                                "3. Chame 'consultar_manual_direto' com os termos que estão faltando. Seja específico.\n" +
                                "NÃO responda a pergunta ainda — apenas investigue o que falta."
                            android.util.Log.i("MestreIA_RAG", "║  FASE 1: protocolo 3-fases ativado — IA deve identificar lacunas e buscar")
                        }

                        // Última iteração: força resposta final sem tool calls
                        if (isUltimaIteracao) {
                            promptAtual = "[RESPOSTA FINAL OBRIGATÓRIA] $prompt\n\nATENÇÃO: Esta é sua ÚLTIMA oportunidade de responder. Apresente sua conclusão agora com base no contexto já disponível. NÃO chame ferramentas. Se a regra encontrada for indireta (ex: uma fórmula, divisor ou modificador que implique o resultado), calcule e apresente o resultado para o jogador com a fonte [Livro, Pág]."
                            android.util.Log.i("MestreIA_RAG", "║  ÚLTIMA ITERAÇÃO: tools desativados + resposta forçada")
                        }

                        val resposta = MestreIAClient.perguntarAoMestre(
                            baseUrl = iaUrl, apiKey = iaKey, workspaceSlug = iaModel,
                            prompt = promptAtual, history = historicoLimitado, contextoPersonagem = viewModel.personagem.toJson(),
                            catalogo = catalogoDinamico, modo = modo, onChunk = if (loopsRestantes == 1) sendChunk else null,
                            desativarTools = isUltimaIteracao
                        )

                        if (resposta.toolCalls.isNotEmpty()) {
                            android.util.Log.i("MestreIA_RAG", "║  TOOL CALLS: ${resposta.toolCalls.size} chamada(s) — executando em paralelo")

                            val toolResultados: List<ToolResult> = coroutineScope {
                                resposta.toolCalls.mapIndexed { idx, toolCall ->
                                    async {
                                        val queryTc = toolCall.args.optString("query", "").take(50)
                                        android.util.Log.i("MestreIA_RAG", "║  TOOL[$idx]: [${toolCall.name}] query=\"$queryTc\"")

                                        when (toolCall.name) {
                                            MestreIATools.TOOL_MANUAL_DIRETO -> {
                                                val queryTool = toolCall.args.optString("query", prompt)
                                                val queryNorm = queryTool.lowercase().trim().take(40)
                                                val jaFoiBuscado = historicoInvestigacao
                                                    .filter { it.first == "assistant" }
                                                    .any { it.second.lowercase().contains(queryNorm) }
                                                if (jaFoiBuscado) {
                                                    android.util.Log.w("MestreIA_RAG", "║  TOOL[$idx] DUPLICADA: '$queryNorm'")
                                                    ToolResult.Duplicada(queryTool)
                                                } else {
                                                    updateStatus("Buscando: \"${queryTool.take(40)}\"...")
                                                    val resTool = graphEngine.buscarDiretoNoCodex(queryTool, emptyList())
                                                    if (resTool.relatedChunks.isNotEmpty()) {
                                                        val pags = resTool.relatedChunks.mapNotNull { it.page_number }.distinct().sorted().joinToString()
                                                        android.util.Log.i("MestreIA_RAG", "║  TOOL[$idx] OK: ${resTool.relatedChunks.size} chunks | págs: [$pags]")
                                                        ToolResult.Manual(queryTool, graphEngine.formatarParaIA(resTool, queryTool), resTool.relatedChunks)
                                                    } else {
                                                        android.util.Log.e("MestreIA_RAG", "║  TOOL[$idx] VAZIO: \"${queryTool.take(60)}\"")
                                                        ToolResult.Vazio(queryTool)
                                                    }
                                                }
                                            }
                                            MestreIATools.TOOL_INSPECT_CHARACTER -> {
                                                val secao = toolCall.args.optString("secao", "atributos")
                                                updateStatus("Lendo ficha ($secao)...")
                                                val infoFicha = when (secao) {
                                                    "status" -> "PV: ${viewModel.personagem.pontosVidaRolagemAtual ?: viewModel.personagem.pontosVida}/${viewModel.personagem.pontosVida} | PF: ${viewModel.personagem.pontosFadigaRolagemAtual ?: viewModel.personagem.pontosFadiga}/${viewModel.personagem.pontosFadiga}"
                                                    "vantagens" -> "Vantagens: " + viewModel.personagem.vantagens.joinToString { it.nome }
                                                    "pericias" -> "Perícias: " + viewModel.personagem.pericias.joinToString { "${it.nome} (NH ${it.calcularNivel(viewModel.personagem)})" }
                                                    "armas" -> {
                                                        val armas = viewModel.personagem.equipamentos.filter { it.armaTipoCombate != null }
                                                        if (armas.isEmpty()) "Nenhuma arma no inventário."
                                                        else armas.joinToString("\n") { e ->
                                                            buildString {
                                                                append("• ${e.nome}")
                                                                e.armaTipoCombate?.let { append(" | Tipo: $it") }
                                                                e.armaDanoRaw?.let { append(" | Dano: $it") }
                                                                e.armaGrupo?.let { append(" | Grupo: $it") }
                                                                e.armaStMinimo?.let { append(" | ST mín: $it") }
                                                            }
                                                        }
                                                    }
                                                    "armaduras" -> {
                                                        val armaduras = viewModel.personagem.equipamentos.filter { it.armaduraRd != null }
                                                        if (armaduras.isEmpty()) "Nenhuma armadura no inventário."
                                                        else armaduras.joinToString("\n") { e ->
                                                            buildString {
                                                                append("• ${e.nome}")
                                                                e.armaduraRd?.let { append(" | RD: $it") }
                                                                e.armaduraLocal?.let { append(" | Local: $it") }
                                                            }
                                                        }
                                                    }
                                                    else -> "Atributos: ST ${viewModel.personagem.st}, DX ${viewModel.personagem.dx}, IQ ${viewModel.personagem.iq}, HT ${viewModel.personagem.ht}"
                                                }
                                                ToolResult.Ficha(secao, infoFicha)
                                            }
                                            MestreIATools.TOOL_NEXUS_ARCANO -> {
                                                val magiaAlvo = toolCall.args.optString("magia_alvo", prompt)
                                                updateStatus("Nexus: $magiaAlvo...")
                                                ToolResult.Nexus(magiaAlvo, nexusEngine.formatarGabaritoParaIA(magiaAlvo))
                                            }
                                            else -> ToolResult.Vazio(toolCall.name)
                                        }
                                    }
                                }.awaitAll()
                            }

                            // Aplica todos os resultados ao catalogoDinamico
                            val resumoBuscas = mutableListOf<String>()
                            var ponteFinal = catalogoDinamico.ponteDeFerro
                            val chunksFinal = catalogoDinamico.chunks.toMutableList()
                            var todasDuplicadas = true

                            for ((idx, resultado) in toolResultados.withIndex()) {
                                when (resultado) {
                                    is ToolResult.Manual -> {
                                        todasDuplicadas = false
                                        ponteFinal = (ponteFinal + "\n\n=== REGRAS TOOL[$idx]: ${resultado.query} ===\n${resultado.texto}").take(35000)
                                        chunksFinal.addAll(resultado.chunks)
                                        resumoBuscas.add("'${resultado.query.take(30)}'")
                                        historicoInvestigacao.add("assistant" to "Consultando manuais sobre '${resultado.query}'...")
                                        historicoInvestigacao.add("system" to "RESULTADO[$idx]: ${resultado.texto.take(1500)}\n[Contexto completo no Códex]")
                                    }
                                    is ToolResult.Vazio -> {
                                        todasDuplicadas = false
                                        historicoInvestigacao.add("system" to "Nenhum resultado para '${resultado.query}' no Códex.")
                                        resumoBuscas.add("'${resultado.query.take(30)}' sem resultado")
                                    }
                                    is ToolResult.Ficha -> {
                                        todasDuplicadas = false
                                        historicoInvestigacao.add("system" to "DADOS DA FICHA (${resultado.secao}): ${resultado.info}")
                                        resumoBuscas.add("ficha(${resultado.secao})")
                                    }
                                    is ToolResult.Nexus -> {
                                        todasDuplicadas = false
                                        ponteFinal = (ponteFinal + "\n\n=== NEXUS ===\n${resultado.gabarito}").take(35000)
                                        resumoBuscas.add("nexus(${resultado.magia})")
                                    }
                                    is ToolResult.Duplicada -> {
                                        historicoInvestigacao.add("system" to "AVISO: '${resultado.query}' já foi buscado.")
                                    }
                                }
                            }

                            catalogoDinamico = catalogoDinamico.copy(
                                ponteDeFerro = ponteFinal,
                                chunks = chunksFinal.distinctBy { it.chunk_id }
                            )
                            android.util.Log.i("MestreIA_RAG", "║  TOOLS CONCLUÍDAS: ${resumoBuscas.joinToString(" | ")} | ctx=${ponteFinal.length}chars | chunks=${chunksFinal.distinctBy{it.chunk_id}.size}")

                            promptAtual = if (todasDuplicadas) {
                                "[RESPOSTA OBRIGATÓRIA] Todas as buscas já foram realizadas. Responda agora com base no contexto disponível. NÃO repita buscas."
                            } else {
                                "Com base nas regras encontradas (${resumoBuscas.joinToString()}), responda à dúvida. Mostre os cálculos passo a passo se houver números. Se a regra for analógica, deixe isso explícito."
                            }

                            loopsRestantes--
                            iteracao++
                            continue
                        }

                        if (resposta.text.isNotBlank() && !ehErroDeApi(resposta.text)) {
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
     * LOTE 258: Query Rewriting — se FTS retornar < 5 chunks, reformula a pergunta
     * via API leve (Gemini Flash Lite) em termos técnicos do GURPS e tenta novamente.
     */
    suspend fun gerarCatalogoDireto(prompt: String, history: List<MestreIAClient.ChatMessage>, termosExtras: List<String> = emptyList()): CatalogoLocalResult {
        val promptExpandido = prompt.take(500)
        // perguntaOriginal passada para que o TopicIndex use a pergunta curta, não a query expandida com 40+ termos
        val res = graphEngine.buscarDiretoNoCodex(promptExpandido, termosExtras, perguntaOriginal = prompt)

        // Resultado satisfatório: 5+ chunks — não precisa de rewriting
        if (res.relatedChunks.size >= 5) {
            val cat = MestreIAClient.CatalogoNomes(
                chunks = res.relatedChunks,
                ponteDeFerro = graphEngine.formatarParaIA(res, prompt).take(35000)
            )
            return CatalogoLocalResult(cat, true)
        }

        // Resultado fraco: tenta query rewriting via API antes de desistir
        android.util.Log.w("MestreIA_RAG", "║  QUERY REWRITE: apenas ${res.relatedChunks.size} chunks — reformulando com IA...")
        val termosReescritos = reescreverQueryParaGurps(prompt)
        if (termosReescritos.isNotEmpty()) {
            android.util.Log.i("MestreIA_RAG", "║  QUERY REWRITE OK: $termosReescritos")
            val resReescrito = graphEngine.buscarDiretoNoCodex(termosReescritos, termosExtras, perguntaOriginal = prompt)
            if (resReescrito.relatedChunks.size > res.relatedChunks.size) {
                android.util.Log.i("MestreIA_RAG", "║  QUERY REWRITE MELHOROU: ${res.relatedChunks.size} → ${resReescrito.relatedChunks.size} chunks")
                // Merge: resultado original + reescrito, deduplicado
                val chunksMerge = (resReescrito.relatedChunks + res.relatedChunks).distinctBy { it.chunk_id }
                val scoresMerge = resReescrito.chunkScores + res.chunkScores
                val resMerge = MestreIAGraphEngine.GraphSearchResult(relatedChunks = chunksMerge, chunkScores = scoresMerge)
                val cat = MestreIAClient.CatalogoNomes(
                    chunks = chunksMerge,
                    ponteDeFerro = graphEngine.formatarParaIA(resMerge, prompt).take(35000)
                )
                return CatalogoLocalResult(cat, chunksMerge.isNotEmpty())
            }
        }

        // Fallback: retorna o que tinha (pode ser 0 chunks — IA ativará protocolo de lacuna)
        val cat = MestreIAClient.CatalogoNomes(
            chunks = res.relatedChunks,
            ponteDeFerro = graphEngine.formatarParaIA(res, prompt).take(35000)
        )
        return CatalogoLocalResult(cat, res.relatedChunks.isNotEmpty())
    }

    /**
     * LOTE 258: Chama API leve para reformular a query em termos técnicos do GURPS.
     * Usa Gemini Flash Lite — rápido, barato, sem contexto RAG.
     * Retorna string com 5-8 termos técnicos separados por espaço, ou vazia se falhar.
     */
    private suspend fun reescreverQueryParaGurps(pergunta: String): String {
        val geminiUrl = BuildConfig.MESTRE_IA_LITE_1_URL
        val geminiKey = BuildConfig.MESTRE_IA_GEMINI_KEY
        val geminiModel = BuildConfig.MESTRE_IA_GEMINI_3_1_FLASH_LITE
        if (geminiKey.isBlank()) return ""
        return try {
            val promptRewrite = "Reescreva a pergunta abaixo como 5 a 8 termos técnicos do sistema de RPG GURPS 4ª edição, separados por espaço. Apenas os termos, sem explicação, sem pontuação.\n\nPergunta: $pergunta"
            val resp = MestreIAClient.perguntarAoMestre(
                baseUrl = geminiUrl, apiKey = geminiKey, workspaceSlug = geminiModel,
                prompt = promptRewrite, history = emptyList(), contextoPersonagem = "{}",
                catalogo = MestreIAClient.CatalogoNomes(), modo = "conversa",
                onChunk = null, desativarTools = true
            )
            resp.text.trim().take(200).replace(",", " ").replace(";", " ")
        } catch (e: Exception) {
            android.util.Log.w("MestreIA_RAG", "║  QUERY REWRITE FALHOU: ${e.message?.take(50)}")
            ""
        }
    }


    fun limparNarrativaParaChat(texto: String): String {
        if (!texto.contains("```json")) return texto.trim()
        val sb = StringBuilder()
        var i = 0
        while (i < texto.length) {
            val ini = texto.indexOf("```json", i)
            if (ini < 0) { sb.append(texto, i, texto.length); break }
            sb.append(texto, i, ini)
            val fim = texto.indexOf("```", ini + 7)
            i = if (fim < 0) texto.length else fim + 3
        }
        return sb.toString().trim()
    }

    data class CatalogoLocalResult(val catalogo: MestreIAClient.CatalogoNomes, val isRagSuccess: Boolean)
}

private sealed class ToolResult {
    data class Manual(val query: String, val texto: String, val chunks: List<MestreIAChunk>) : ToolResult()
    data class Vazio(val query: String) : ToolResult()
    data class Ficha(val secao: String, val info: String) : ToolResult()
    data class Nexus(val magia: String, val gabarito: String) : ToolResult()
    data class Duplicada(val query: String) : ToolResult()
}
