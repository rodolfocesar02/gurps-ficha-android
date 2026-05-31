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

            // Lote 271: FILA DE CONTINGÊNCIA PRIME (Multi-Cloud)
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

            // Lote 271: sem pré-contexto RAG — IA decide sozinha o que buscar
            if (!isCasual) {
                android.util.Log.i("MestreIA_RAG", "║  Lote 271: busca livre — IA controla as queries (max 5 tool calls)")
            } else {
                android.util.Log.i("MestreIA_RAG", "║  RAG: pulado (mensagem casual)")
            }

            var sucesso = false
            val errosAcumulados = mutableListOf<String>()

            // Contexto dinâmico acumulado pelas tool calls da IA
            var catalogoDinamico = MestreIAClient.CatalogoNomes()
            var promptAtual = prompt
            var historicoInvestigacao = mutableListOf<Pair<String, String>>()
            var toolCallsFeitas = 0
            // Lote 328: trava anti-confabulação — rastreia se o modelo REALMENTE leu alguma
            // página (ler_pagina), não só localizou. Sem leitura, a resposta seria de memória.
            var leuAlgumaPagina = false
            // Lote 325: novo loop "localizar → ler" precisa de mais idas (localizar e ler
            // são chamadas separadas). Antes 5 era suficiente quando 1 tool fazia tudo.
            val MAX_TOOL_CALLS = 8
            var perguntaAoUsuarioPendente: String? = null

            for (config in fila) {
                val iaUrl = config.url
                val iaKey = config.key
                val iaModel = config.model

                if (iaKey.isBlank()) {
                    errosAcumulados.add("$iaModel: Chave Vazia")
                    continue
                }

                if (historicoInvestigacao.isNotEmpty()) {
                    android.util.Log.i("MestreIA_RAG", "║  HANDOFF → $iaModel: ${historicoInvestigacao.size} entradas preservadas")
                    promptAtual = prompt
                }

                try {
                    val historicoLimitado = (viewModel.mestreIAChatHistory.map { it.role to it.text } + historicoInvestigacao).takeLast(12)

                    // Lote 271: loop livre — IA busca até MAX_TOOL_CALLS vezes, depois responde
                    var iteracao = 1

                    while (true) {
                        val toolsRestantes = MAX_TOOL_CALLS - toolCallsFeitas
                        val isUltimaIteracao = toolsRestantes <= 0 || iteracao > MAX_TOOL_CALLS + 1

                        updateStatus(when {
                            iteracao == 1 -> "Consultando o mestre..."
                            isUltimaIteracao -> "Elaborando resposta final..."
                            else -> "Buscando regras (busca ${toolCallsFeitas}/${MAX_TOOL_CALLS})..."
                        })

                        val ctxAtual = catalogoDinamico.ponteDeFerro.length
                        android.util.Log.i("MestreIA_RAG", "╠══ ITERAÇÃO $iteracao → $iaModel | ctx=${ctxAtual}chars | toolsFeitas=$toolCallsFeitas | desativarTools=$isUltimaIteracao")

                        if (isUltimaIteracao) {
                            // Lote 328: trava anti-confabulação categorial (sem exemplos).
                            // Se o modelo nunca leu uma página, ele NÃO tem base textual — qualquer
                            // citação seria de memória. Nesse caso a instrução exige declarar a
                            // ausência, em vez de "responda com o que tem" (que induzia confabulação).
                            promptAtual = if (!leuAlgumaPagina) {
                                "[RESPOSTA FINAL OBRIGATÓRIA] $prompt\n\n" +
                                "ATENÇÃO: você NÃO leu nenhuma página com ler_pagina nesta investigação. " +
                                "Portanto você NÃO possui texto de regra para citar. NÃO invente páginas, " +
                                "números ou regras de memória. Responda declarando que não localizou a regra " +
                                "nos manuais e, se quiser, ofereça apenas uma interpretação claramente rotulada " +
                                "como tal — sem citar páginas que você não leu."
                            } else {
                                "[RESPOSTA FINAL OBRIGATÓRIA] $prompt\n\n" +
                                "NÃO chame ferramentas. Responda usando SOMENTE o texto das páginas que você " +
                                "leu com ler_pagina. Cite [Livro, Pág] apenas de páginas efetivamente lidas. " +
                                "NÃO acrescente páginas, números ou regras de memória. Se a regra for indireta, " +
                                "calcule a partir do que leu, mostrando os valores. Se não encontrou, declare explicitamente."
                            }
                            android.util.Log.i("MestreIA_RAG", "║  ÚLTIMA ITERAÇÃO: tools desativados + resposta forçada | leuAlgumaPagina=$leuAlgumaPagina")
                        }

                        val resposta = MestreIAClient.perguntarAoMestre(
                            baseUrl = iaUrl, apiKey = iaKey, workspaceSlug = iaModel,
                            prompt = promptAtual, history = historicoLimitado, contextoPersonagem = viewModel.personagem.toJson(),
                            catalogo = catalogoDinamico, modo = modo, onChunk = if (isUltimaIteracao) sendChunk else null,
                            desativarTools = isUltimaIteracao,
                            maxTokens = if (isUltimaIteracao) 16384 else 2048
                        )

                        if (resposta.toolCalls.isNotEmpty() && !isUltimaIteracao) {
                            android.util.Log.i("MestreIA_RAG", "║  TOOL CALLS: ${resposta.toolCalls.size} chamada(s) — executando em paralelo")

                            val toolResultados: List<ToolResult> = coroutineScope {
                                resposta.toolCalls.mapIndexed { idx, toolCall ->
                                    async {
                                        // Lote 326: log de dispatch consciente do tipo de tool.
                                        // Tools antigas usam 'query'/'livro'; localizar usa 'termos'/'livros';
                                        // ler usa 'livro'/'pagina'. Loga os args reais de cada uma.
                                        val argsLog = when (toolCall.name) {
                                            MestreIATools.TOOL_LOCALIZAR -> {
                                                val t = toolCall.args.optString("termos", "")
                                                val l = toolCall.args.optJSONArray("livros")?.let { arr ->
                                                    (0 until arr.length()).joinToString(",") { arr.optString(it) }
                                                }
                                                "termos=\"$t\"" + if (!l.isNullOrBlank()) " livros=[$l]" else ""
                                            }
                                            MestreIATools.TOOL_LER -> {
                                                val l = toolCall.args.optString("livro", "")
                                                val p = toolCall.args.optInt("pagina", -1)
                                                val pf = if (toolCall.args.has("pagina_final")) "-${toolCall.args.optInt("pagina_final")}" else ""
                                                "livro=\"$l\" pag=$p$pf"
                                            }
                                            else -> {
                                                val q = toolCall.args.optString("query", "").take(50)
                                                val lv = toolCall.args.optString("livro", "").takeIf { it.isNotBlank() }
                                                "query=\"$q\"" + if (lv != null) " livro=\"$lv\"" else ""
                                            }
                                        }
                                        android.util.Log.i("MestreIA_RAG", "║  TOOL[$idx]: [${toolCall.name}] $argsLog")

                                        when (toolCall.name) {
                                            MestreIATools.TOOL_MANUAL_DIRETO -> {
                                                val queryTool = toolCall.args.optString("query", prompt)
                                                val filtroLivro = toolCall.args.optString("livro", "").takeIf { it.isNotBlank() }
                                                executarBuscaCodex(idx, queryTool, filtroLivro, historicoInvestigacao, updateStatus)
                                            }
                                            // Lote 317: tools especializadas — cada uma força filtroLivro fixo
                                            MestreIATools.TOOL_REGRAS_MAGIA -> {
                                                val queryTool = toolCall.args.optString("query", prompt)
                                                executarBuscaCodex(idx, queryTool, "Magia", historicoInvestigacao, updateStatus)
                                            }
                                            MestreIATools.TOOL_REGRAS_ARMAS_FOGO -> {
                                                val queryTool = toolCall.args.optString("query", prompt)
                                                executarBuscaCodex(idx, queryTool, "Gun Fu", historicoInvestigacao, updateStatus)
                                            }
                                            MestreIATools.TOOL_REGRAS_ARTES_MARCIAIS -> {
                                                val queryTool = toolCall.args.optString("query", prompt)
                                                executarBuscaCodex(idx, queryTool, "Artes Marciais", historicoInvestigacao, updateStatus)
                                            }
                                            MestreIATools.TOOL_REGRAS_AQUATICO -> {
                                                val queryTool = toolCall.args.optString("query", prompt)
                                                executarBuscaCodex(idx, queryTool, "Pyramid Aquático", historicoInvestigacao, updateStatus)
                                            }
                                            // Lote 325: novo motor de busca do Auditor (grep + leitura dirigida)
                                            MestreIATools.TOOL_LOCALIZAR -> {
                                                val termos = toolCall.args.optString("termos", prompt)
                                                val livros = toolCall.args.optJSONArray("livros")?.let { arr ->
                                                    (0 until arr.length()).mapNotNull { i -> arr.optString(i, "").takeIf { it.isNotBlank() } }
                                                }
                                                executarLocalizar(idx, termos, livros, updateStatus)
                                            }
                                            MestreIATools.TOOL_LER -> {
                                                val livro = toolCall.args.optString("livro", "")
                                                val pagina = toolCall.args.optInt("pagina", -1)
                                                val paginaFinal = if (toolCall.args.has("pagina_final")) toolCall.args.optInt("pagina_final") else null
                                                if (livro.isBlank() || pagina < 0) ToolResult.Vazio("ler: argumentos inválidos")
                                                else executarLer(idx, livro, pagina, paginaFinal, updateStatus)
                                            }
                                            MestreIATools.TOOL_INSPECT_CHARACTER -> {
                                                val secao = toolCall.args.optString("secao", "atributos")
                                                updateStatus("Lendo ficha ($secao)...")
                                                val p = viewModel.personagem
                                                val def = p.defesasAtivas
                                                val infoFicha = when (secao) {
                                                    "status" -> {
                                                        val nivelCargaLabel = when (p.nivelCarga) {
                                                            0 -> "Sem carga"
                                                            1 -> "Carga leve"
                                                            2 -> "Carga média"
                                                            3 -> "Carga pesada"
                                                            4 -> "Carga extra-pesada"
                                                            else -> "Carga ${p.nivelCarga}"
                                                        }
                                                        "PV: ${p.pontosVidaRolagemAtual ?: p.pontosVida}/${p.pontosVida} | PF: ${p.pontosFadigaRolagemAtual ?: p.pontosFadiga}/${p.pontosFadiga} | Carga: $nivelCargaLabel"
                                                    }
                                                    "vantagens" -> buildString {
                                                        append("Vantagens: " + p.vantagens.joinToString { it.nome })
                                                        if (p.desvantagens.isNotEmpty())
                                                            append("\nDesvantagens: " + p.desvantagens.joinToString { it.nome })
                                                    }
                                                    "pericias" -> buildString {
                                                        append("Perícias: " + p.pericias.joinToString { "${it.nome} (NH ${it.calcularNivel(p)})" })
                                                        if (p.tecnicas.isNotEmpty())
                                                            append("\nTécnicas: " + p.tecnicas.joinToString { "${it.nome} (NH ${it.calcularNivel(p) ?: "?"})" })
                                                        if (p.magias.isNotEmpty()) {
                                                            val aptidao = com.gurps.ficha.domain.engine.MagicEngine.getNivelAptidaoMagicaParaMagia(p, null)
                                                            append("\nMagias: " + p.magias.joinToString { "${it.nome} (NH ${it.calcularNivel(p, aptidao)})" })
                                                        }
                                                    }
                                                    "completo" -> buildString {
                                                        // Atributos e defesas
                                                        append("=== ATRIBUTOS ===\n")
                                                        append("ST ${p.st}, DX ${p.dx}, IQ ${p.iq}, HT ${p.ht}")
                                                        append(" | Vel. Básica: ${"%.2f".format(p.velocidadeBasica)}")
                                                        append(" | Deslocamento: ${p.deslocamentoBasico}\n")
                                                        append("Esquiva: ${def.calcularEsquiva(p)}")
                                                        val apara = def.calcularApara(p)
                                                        val periciaApara = def.getPericiaApara(p)
                                                        if (apara != null && periciaApara != null)
                                                            append(" | Apara: $apara (${periciaApara.nome})")
                                                        val bloqueio = def.calcularBloqueio(p)
                                                        if (bloqueio != null) append(" | Bloqueio: $bloqueio")
                                                        // Status
                                                        val nivelCargaLabel = when (p.nivelCarga) { 0 -> "Sem carga"; 1 -> "Leve"; 2 -> "Média"; 3 -> "Pesada"; 4 -> "Extra-pesada"; else -> "${p.nivelCarga}" }
                                                        append("\nPV: ${p.pontosVidaRolagemAtual ?: p.pontosVida}/${p.pontosVida} | PF: ${p.pontosFadigaRolagemAtual ?: p.pontosFadiga}/${p.pontosFadiga} | Carga: $nivelCargaLabel")
                                                        // Vantagens e desvantagens
                                                        if (p.vantagens.isNotEmpty())
                                                            append("\n=== VANTAGENS ===\n" + p.vantagens.joinToString { it.nome })
                                                        if (p.desvantagens.isNotEmpty())
                                                            append("\n=== DESVANTAGENS ===\n" + p.desvantagens.joinToString { it.nome })
                                                        // Perícias
                                                        if (p.pericias.isNotEmpty())
                                                            append("\n=== PERÍCIAS ===\n" + p.pericias.joinToString { "${it.nome} (NH ${it.calcularNivel(p)})" })
                                                        // Técnicas
                                                        if (p.tecnicas.isNotEmpty())
                                                            append("\n=== TÉCNICAS ===\n" + p.tecnicas.joinToString { "${it.nome} (NH ${it.calcularNivel(p) ?: "?"})" })
                                                        // Magias
                                                        if (p.magias.isNotEmpty()) {
                                                            val aptidao = com.gurps.ficha.domain.engine.MagicEngine.getNivelAptidaoMagicaParaMagia(p, null)
                                                            append("\n=== MAGIAS ===\n" + p.magias.joinToString { "${it.nome} (NH ${it.calcularNivel(p, aptidao)})" })
                                                        }
                                                        // Armas
                                                        val armas = p.equipamentos.filter { it.armaTipoCombate != null }
                                                        if (armas.isNotEmpty())
                                                            append("\n=== ARMAS ===\n" + armas.joinToString("\n") { e ->
                                                                buildString {
                                                                    append("• ${e.nome}")
                                                                    e.armaTipoCombate?.let { append(" | Tipo: $it") }
                                                                    e.armaDanoRaw?.let { append(" | Dano: $it") }
                                                                    e.armaGrupo?.let { append(" | Grupo: $it") }
                                                                    e.armaStMinimo?.let { append(" | ST mín: $it") }
                                                                }
                                                            })
                                                        // Armaduras
                                                        val armaduras = p.equipamentos.filter { it.armaduraRd != null }
                                                        if (armaduras.isNotEmpty())
                                                            append("\n=== ARMADURAS ===\n" + armaduras.joinToString("\n") { e ->
                                                                buildString {
                                                                    append("• ${e.nome}")
                                                                    e.armaduraRd?.let { append(" | RD: $it") }
                                                                    e.armaduraLocal?.let { append(" | Local: $it") }
                                                                }
                                                            })
                                                    }
                                                    "armas" -> {
                                                        val armas = p.equipamentos.filter { it.armaTipoCombate != null }
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
                                                        val armaduras = p.equipamentos.filter { it.armaduraRd != null }
                                                        if (armaduras.isEmpty()) "Nenhuma armadura no inventário."
                                                        else armaduras.joinToString("\n") { e ->
                                                            buildString {
                                                                append("• ${e.nome}")
                                                                e.armaduraRd?.let { append(" | RD: $it") }
                                                                e.armaduraLocal?.let { append(" | Local: $it") }
                                                            }
                                                        }
                                                    }
                                                    else -> buildString {
                                                        append("ST ${p.st}, DX ${p.dx}, IQ ${p.iq}, HT ${p.ht}")
                                                        append(" | Vel. Básica: ${"%.2f".format(p.velocidadeBasica)}")
                                                        append(" | Deslocamento: ${p.deslocamentoBasico}")
                                                        append(" | Esquiva: ${def.calcularEsquiva(p)}")
                                                        val apara = def.calcularApara(p)
                                                        val periciaApara = def.getPericiaApara(p)
                                                        if (apara != null && periciaApara != null)
                                                            append(" | Apara: $apara (${periciaApara.nome})")
                                                        val bloqueio = def.calcularBloqueio(p)
                                                        if (bloqueio != null)
                                                            append(" | Bloqueio: $bloqueio")
                                                    }
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
                                        toolCallsFeitas++
                                        // Lote 328: marca leitura real. localizar e ler ambos viram Manual;
                                        // distinguimos pelo prefixo "ler:" da query e pela presença de chunks reais.
                                        if (resultado.query.startsWith("ler:") && resultado.chunks.isNotEmpty()) {
                                            leuAlgumaPagina = true
                                        }
                                        ponteFinal = (ponteFinal + "\n\n=== BUSCA ${toolCallsFeitas} [\"${resultado.query.take(40)}\"]: ===\n${resultado.texto}").take(60000)
                                        chunksFinal.addAll(resultado.chunks)
                                        resumoBuscas.add("'${resultado.query.take(30)}'")
                                        historicoInvestigacao.add("assistant" to "Buscando '${resultado.query}'...")
                                        historicoInvestigacao.add("system" to "RESULTADO: ${resultado.texto.take(2000)}")
                                    }
                                    is ToolResult.Vazio -> {
                                        todasDuplicadas = false
                                        toolCallsFeitas++
                                        historicoInvestigacao.add("system" to "Nenhum resultado para '${resultado.query}' no Códex.")
                                        resumoBuscas.add("'${resultado.query.take(30)}' sem resultado")
                                    }
                                    is ToolResult.Ficha -> {
                                        todasDuplicadas = false
                                        historicoInvestigacao.add("system" to "FICHA (${resultado.secao}): ${resultado.info}")
                                        resumoBuscas.add("ficha(${resultado.secao})")
                                    }
                                    is ToolResult.Nexus -> {
                                        todasDuplicadas = false
                                        ponteFinal = (ponteFinal + "\n\n=== NEXUS: ${resultado.magia} ===\n${resultado.gabarito}").take(60000)
                                        resumoBuscas.add("nexus(${resultado.magia})")
                                    }
                                    is ToolResult.Duplicada -> {
                                        historicoInvestigacao.add("system" to "AVISO: '${resultado.query}' já foi buscado anteriormente.")
                                    }
                                }
                            }

                            catalogoDinamico = catalogoDinamico.copy(
                                ponteDeFerro = ponteFinal,
                                chunks = chunksFinal.distinctBy { it.chunk_id }
                            )
                            val toolsRestantesLog = MAX_TOOL_CALLS - toolCallsFeitas
                            android.util.Log.i("MestreIA_RAG", "║  TOOLS CONCLUÍDAS: ${resumoBuscas.joinToString(" | ")} | ctx=${ponteFinal.length}chars | toolsRestantes=$toolsRestantesLog")

                            promptAtual = if (todasDuplicadas) {
                                "[RESPOSTA OBRIGATÓRIA] Buscas duplicadas detectadas. Responda agora com o contexto disponível. NÃO repita buscas."
                            } else if (toolCallsFeitas >= MAX_TOOL_CALLS) {
                                "[RESPOSTA OBRIGATÓRIA] Limite de $MAX_TOOL_CALLS buscas atingido. Responda agora com o contexto acumulado."
                            } else {
                                "Buscas realizadas: ${resumoBuscas.joinToString()}. Você tem ${toolsRestantesLog} busca(s) restante(s). Se precisar de mais informação, busque agora. Senão, responda."
                            }

                            iteracao++
                            continue
                        }

                        if (resposta.text.isNotBlank() && !ehErroDeApi(resposta.text)) {
                            val temCitacao = resposta.text.contains("[") && (resposta.text.contains("Pág", true) || resposta.text.contains("Pg", true))

                            // Lote 315: Verificador de Citações — detecta páginas alucinadas
                            val citacoes = MestreIACitationValidator.extrair(resposta.text)
                            val validacao = MestreIACitationValidator.validar(citacoes, catalogoDinamico.chunks)
                            val avisoAlucinacao = MestreIACitationValidator.formatarAviso(validacao)
                            if (validacao.temAlucinacao) {
                                android.util.Log.w(
                                    "MestreIA_RAG",
                                    "║  ALUCINAÇÃO DETECTADA: ${validacao.naoVerificadas.size} citações fora do contexto RAG: " +
                                        validacao.naoVerificadas.joinToString { it.trecho.take(60) }
                                )
                            }

                            val respostaFinal = buildString {
                                append(resposta.text)
                                if (!temCitacao && toolCallsFeitas > 0) {
                                    append("\n\n _[AUDITORIA: Sem citações diretas do manual]_")
                                }
                                append(avisoAlucinacao)
                            }

                            android.util.Log.i("MestreIA_RAG", "╚══ RESPOSTA OK [$iaModel] | iter=$iteracao | toolsFeitas=$toolCallsFeitas | ${respostaFinal.length}chars | citação=$temCitacao | alucinou=${validacao.temAlucinacao}")
                            sendResult(toolCallsFeitas > 0, resposta.copy(text = respostaFinal, modelName = iaModel))
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
     * LOTE 317: helper compartilhado entre TOOL_MANUAL_DIRETO e as 4 tools
     * especializadas (TOOL_REGRAS_MAGIA, _ARMAS_FOGO, _ARTES_MARCIAIS, _AQUATICO).
     * Cada tool especializada chama isto com filtroLivro fixo.
     *
     * ⚠️ LEGADO desde o Lote 325. As 5 tools de embedding que chamavam este helper
     * NÃO são mais oferecidas ao modelo (o Auditor usa localizar_no_codex + ler_pagina).
     * Os `when` cases que chamam esta função permanecem no dispatch como rede, mas nunca
     * disparam. Usa graphEngine.buscarDiretoNoCodex (motor semântico, também legado p/ Auditor).
     * Candidato a remoção em lote de limpeza — ver .agent/skills/ARQUITETURA_MESTRE_IA.md §5.2.
     */
    private suspend fun executarBuscaCodex(
        idx: Int,
        queryTool: String,
        filtroLivro: String?,
        historicoInvestigacao: MutableList<Pair<String, String>>,
        updateStatus: (String) -> Unit
    ): ToolResult {
        val queryNorm = queryTool.lowercase().trim().take(40)
        val jaFoiBuscado = historicoInvestigacao
            .filter { it.first == "assistant" }
            .any { it.second.lowercase().contains(queryNorm) }
        if (jaFoiBuscado) {
            android.util.Log.w("MestreIA_RAG", "║  TOOL[$idx] DUPLICADA: '$queryNorm'")
            return ToolResult.Duplicada(queryTool)
        }
        val statusMsg = if (filtroLivro != null)
            "Buscando em $filtroLivro: \"${queryTool.take(30)}\"..."
        else
            "Buscando: \"${queryTool.take(40)}\"..."
        updateStatus(statusMsg)
        val resTool = graphEngine.buscarDiretoNoCodex(queryTool, emptyList(), filtroLivro = filtroLivro)
        return if (resTool.relatedChunks.isNotEmpty()) {
            val pags = resTool.relatedChunks.mapNotNull { it.page_number }.distinct().sorted().joinToString()
            val textoFormatado = graphEngine.formatarParaIA(resTool, queryTool)
            val livroLog = if (filtroLivro != null) " [livro=$filtroLivro]" else ""
            android.util.Log.i("MestreIA_RAG", "║  TOOL[$idx] OK$livroLog: ${resTool.relatedChunks.size} chunks | págs: [$pags]")
            android.util.Log.i("MestreIA_RAG", "║  TOOL[$idx] CONTEUDO (800chars):\n${textoFormatado.take(800)}")
            ToolResult.Manual(queryTool, textoFormatado, resTool.relatedChunks)
        } else {
            android.util.Log.e("MestreIA_RAG", "║  TOOL[$idx] VAZIO: \"${queryTool.take(60)}\"")
            ToolResult.Vazio(queryTool)
        }
    }

    /**
     * Lote 325: LOCALIZAR — "página de resultados" por palavra-chave (FTS4 AND).
     * Retorna lista compacta (livro|página|trecho), não o texto completo.
     */
    private suspend fun executarLocalizar(
        idx: Int,
        termos: String,
        livros: List<String>?,
        updateStatus: (String) -> Unit
    ): ToolResult {
        updateStatus("Localizando: \"${termos.take(40)}\"...")
        val res = repository.localizarNoCodex(termos, livros)
        if (res.hits.isEmpty()) {
            android.util.Log.w("MestreIA_RAG", "║  LOCALIZAR[$idx] VAZIO: \"${termos.take(60)}\"")
            return ToolResult.Vazio("localizar: $termos")
        }
        val sb = StringBuilder()
        sb.append("PÁGINAS ENCONTRADAS para \"$termos\" (${res.total} no total")
        if (res.modo == "OR") sb.append(", busca aproximada — nenhuma página continha TODAS as palavras")
        sb.append("):\n")
        for (h in res.hits) {
            sb.append("• [${h.livro}, pág. ${h.pagina}] ${h.trecho}\n")
        }
        if (res.total > res.hits.size) {
            sb.append("(... e mais ${res.total - res.hits.size} páginas — adicione palavras para estreitar)\n")
        }
        sb.append("\nAGORA use ler_pagina(livro, pagina) para ler o conteúdo completo das páginas que parecem responder à pergunta.")
        android.util.Log.i("MestreIA_RAG", "║  LOCALIZAR[$idx] OK: ${res.total} págs [${res.modo}]")
        return ToolResult.Manual("localizar:$termos", sb.toString(), emptyList())
    }

    /**
     * Lote 325: LER — abre o texto completo de uma página (ou intervalo curto).
     * Adiciona os chunks reais ao contexto (o Verificador de Citações valida contra eles).
     */
    private suspend fun executarLer(
        idx: Int,
        livro: String,
        pagina: Int,
        paginaFinal: Int?,
        updateStatus: (String) -> Unit
    ): ToolResult {
        val faixa = if (paginaFinal != null && paginaFinal != pagina) "$pagina-$paginaFinal" else "$pagina"
        updateStatus("Lendo $livro pág. $faixa...")
        val chunks = repository.lerPaginas(livro, pagina, paginaFinal)
        if (chunks.isEmpty()) {
            android.util.Log.w("MestreIA_RAG", "║  LER[$idx] VAZIO: $livro p$faixa")
            return ToolResult.Vazio("ler: $livro pág. $faixa")
        }
        val texto = chunks.joinToString("\n\n") { c ->
            "--- [${c.source_title}, pág. ${c.page_number}] ---\n${c.text}"
        }
        android.util.Log.i("MestreIA_RAG", "║  LER[$idx] OK: $livro p$faixa → ${chunks.size} chunks (${texto.length} chars)")
        // Lote 326: preview do texto LIDO — permite comparar "o que o modelo leu" com
        // "o que ele afirmou na resposta" e auditar confabulação.
        android.util.Log.i("MestreIA_RAG", "║  LER[$idx] PREVIEW: \"${texto.replace("\n", " ").take(300)}\"")
        return ToolResult.Manual("ler:$livro p$faixa", texto, chunks)
    }

    /**
     * LOTE 119: Gerador de Catálogo via Busca Direta (Pula o Grafo).
     * LOTE 258: Query Rewriting — se FTS retornar < 5 chunks, reformula a pergunta
     * via API leve (Gemini Flash Lite) em termos técnicos do GURPS e tenta novamente.
     *
     * ⚠️ MORTO desde o Lote 325 — ZERO callers no app. Era o pré-contexto RAG do fluxo
     * antigo. Usa graphEngine.buscarDiretoNoCodex (semântico) + reescreverQueryParaGurps,
     * ambos legados p/ o Auditor. Candidato a remoção — ver ARQUITETURA_MESTRE_IA.md §5.3.
     */
    suspend fun gerarCatalogoDireto(
        prompt: String,
        history: List<MestreIAClient.ChatMessage>,
        termosExtras: List<String> = emptyList(),
        termosPonderados: List<MestreIAPlanner.TermoPonderado> = emptyList()
    ): CatalogoLocalResult {
        val promptExpandido = prompt.take(500)
        val res = graphEngine.buscarDiretoNoCodex(
            query = promptExpandido,
            termosExtras = termosExtras,
            perguntaOriginal = prompt,
            termosPonderados = termosPonderados
        )

        if (res.relatedChunks.size >= 5) {
            val cat = MestreIAClient.CatalogoNomes(
                chunks = res.relatedChunks,
                ponteDeFerro = graphEngine.formatarParaIA(res, prompt).take(35000)
            )
            return CatalogoLocalResult(cat, true)
        }

        // Resultado fraco: tenta query rewriting via API
        android.util.Log.w("MestreIA_RAG", "║  QUERY REWRITE: apenas ${res.relatedChunks.size} chunks — reformulando com IA...")
        val termosReescritos = reescreverQueryParaGurps(prompt)
        if (termosReescritos.isNotEmpty()) {
            android.util.Log.i("MestreIA_RAG", "║  QUERY REWRITE OK: $termosReescritos")
            val resReescrito = graphEngine.buscarDiretoNoCodex(
                query = termosReescritos,
                termosExtras = termosExtras,
                perguntaOriginal = prompt,
                termosPonderados = termosPonderados
            )
            if (resReescrito.relatedChunks.size > res.relatedChunks.size) {
                android.util.Log.i("MestreIA_RAG", "║  QUERY REWRITE MELHOROU: ${res.relatedChunks.size} → ${resReescrito.relatedChunks.size} chunks")
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
