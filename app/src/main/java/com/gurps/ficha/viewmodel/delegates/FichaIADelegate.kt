package com.gurps.ficha.viewmodel.delegates

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.*
import com.gurps.ficha.domain.MestreIAUseCase
import com.gurps.ficha.domain.MestreIAGeneratorUseCase
import com.gurps.ficha.domain.RelatorioValidacao
import com.gurps.ficha.model.*
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.gurps.ficha.data.storage.ChatSessionEntity
import android.content.Context
import com.gurps.ficha.data.storage.ChatMessageEntity
import java.util.Date

class FichaIADelegate(
    private val viewModel: FichaViewModel,
    private val dataRepository: DataRepository,
    private val scope: CoroutineScope,
    private val context: Context? = null
) {
    private val mestreIAUseCase by lazy { MestreIAUseCase(viewModel, dataRepository) }
    private val mestreIAGeneratorUseCase by lazy { MestreIAGeneratorUseCase(viewModel, dataRepository, context) }

    var mestreIAChatHistory by mutableStateOf<List<MestreIAClient.ChatMessage>>(emptyList())
    var fichaGeradaPendente by mutableStateOf<MestreIAResponse?>(null)
    var relatorioValidacao by mutableStateOf<RelatorioValidacao?>(null)
    var mestreIAMode by mutableStateOf("conversa") // Default: Dúvidas/Free

    var currentSessionId by mutableStateOf<Long?>(null)
    var savedSessions by mutableStateOf<List<ChatSessionEntity>>(emptyList())
    private var sincroniaExecutadaNestaSessao = false
    @Volatile private var integracaoEmAndamento = false

    // Retrato IA: true após o Forjador terminar uma ficha nova (modo geracao)
    var mostrarDialogRetrato by mutableStateOf(false)
    var retratoGerandoStatus by mutableStateOf("")  // mensagem de progresso durante geração

    // uid da bolha [SISTEMA] agregadora ativa. Eventos consecutivos
    // (cada item aplicado pelo Forjador) viram LINHAS na MESMA bolha em
    // vez de N bolhas separadas — chat limpo, feedback ao vivo mantido.
    // Reinicia quando outra coisa (resposta da IA) é escrita no chat.
    private var sistemaBatchUid: String? = null

    // Entrevista do Forjador (Pilar 0): guarda o pedido original enquanto
    // aguarda as respostas do jogador sobre cenário/NT/pontos/magia/conceito.
    private var aguardandoEntrevistaForjador = false
    private var pedidoOriginalForjador = ""

    fun verificarSincroniaAutomatica() {
        if (sincroniaExecutadaNestaSessao) return
        sincroniaExecutadaNestaSessao = true
        
        // Lote 352: caminho semântico (HNSW/ObjectBox) DORMENTE — a Voz migrou para o
        // motor localizar/ler do Auditor e o GraphEngine ficou sem callers. A inicialização
        // abaixo só desperdiçava startup/RAM carregando embeddings que ninguém consulta.
        // Para reativar o experimento HNSW: descomente as 2 linhas e gere chunks.jsonl com embeddings.
        // com.gurps.ficha.domain.MestreIAGraphEngine.MODO_HNSW_PURO = true

        scope.launch(Dispatchers.IO) {
            android.util.Log.i("MestreIA_Auditoria", "VERIFICANDO INTEGRIDADE DO CÓDEX (Início de Sessão)")
            dataRepository.sincronizarCodexSeNecessario()
            // if (context != null) {
            //     com.gurps.ficha.domain.MestreIAVectorEngine.inicializar(context, dataRepository.vecChunkDao)
            // }
        }
    }

    fun limparChat() {
        mestreIAChatHistory = emptyList()
        currentSessionId = null
        sistemaBatchUid = null
        aguardandoEntrevistaForjador = false
        pedidoOriginalForjador = ""
    }

    fun gerarSaudacaoSeVazio() {
        if (mestreIAChatHistory.isNotEmpty()) return
        val nomePersonagem = viewModel.personagem.nome.takeIf { it.isNotBlank() }
        val msg = MestreIAClient.ChatMessage("model", "...", "Mestre IA")
        val saudacaoUid = msg.uid
        scope.launch(Dispatchers.Main) {
            mestreIAChatHistory = mestreIAChatHistory + msg
        }
        scope.launch(Dispatchers.IO) {
            try {
                val prompt = if (nomePersonagem != null) {
                    "Você é o Mestre de um RPG de mesa (GURPS). Gere UMA saudação curta (2-3 linhas) e imersiva para o jogador chamado '$nomePersonagem' que acabou de abrir o chat com você. Seja criativo, use tom épico/fantasia, mencione o nome. Nunca comece com 'Olá' ou 'Bem-vindo'. Varie o estilo a cada vez. Responda APENAS a saudação, sem explicações."
                } else {
                    "Você é o Mestre de um RPG de mesa (GURPS). Gere UMA saudação curta (2-3 linhas) e imersiva para um aventureiro que acabou de abrir o chat. Use tom épico, fantasia ou mistério. Nunca comece com 'Olá' ou 'Bem-vindo'. Varie o estilo a cada vez. Responda APENAS a saudação, sem explicações."
                }
                val resp = MestreIAClient.perguntarAoMestre(
                    baseUrl = "https://generativelanguage.googleapis.com/v1beta",
                    apiKey = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_KEY,
                    workspaceSlug = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_3_1_FLASH_LITE,
                    prompt = prompt,
                    modo = "conversa",
                    desativarTools = true,
                    maxTokens = 150,
                    silencioso = true
                )
                withContext(Dispatchers.Main) {
                    val texto = resp.text.trim().ifBlank { "O Mestre está pronto para guiar sua jornada." }
                    mestreIAChatHistory = mestreIAChatHistory.map {
                        if (it.uid == saudacaoUid) it.copy(text = texto) else it
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mestreIAChatHistory = mestreIAChatHistory.map {
                        if (it.uid == saudacaoUid) it.copy(text = "O Códex está aberto. O que deseja saber, aventureiro?") else it
                    }
                }
            }
        }
    }

    // uid da última mensagem de voz do usuário — para atualizar em streaming
    private var ultimaMensagemVozUsuarioUid: String? = null

    fun adicionarMensagemVoz(texto: String, role: String) {
        val msg = MestreIAClient.ChatMessage(role, texto, "Mestre IA (Voz)")
        mestreIAChatHistory = mestreIAChatHistory + msg
        if (role == "user") ultimaMensagemVozUsuarioUid = msg.uid
        sistemaBatchUid = null
        scope.launch(Dispatchers.IO) {
            val dao = dataRepository.chatHistoryDao()
            val titulo = mestreIAChatHistory.firstOrNull { it.role == "user" }?.text?.take(30) ?: "Conversa por Voz"
            val sessionId = currentSessionId ?: dao.insertSession(ChatSessionEntity(title = titulo, lastUpdate = System.currentTimeMillis()))
            withContext(Dispatchers.Main) { currentSessionId = sessionId }
            dao.updateSessionTimestamp(sessionId, System.currentTimeMillis())
            dao.insertMessage(ChatMessageEntity(sessionId = sessionId, role = role, text = texto, modelName = "Mestre IA (Voz)", timestamp = System.currentTimeMillis()))
        }
    }

    // Atualiza o texto da última mensagem de voz do usuário (streaming de transcrição)
    // Se não houver mensagem anterior, cria uma nova (fallback)
    fun atualizarUltimaMensagemVozUsuario(textoCompleto: String) {
        val uid = ultimaMensagemVozUsuarioUid
        if (uid != null) {
            mestreIAChatHistory = mestreIAChatHistory.map {
                if (it.uid == uid) it.copy(text = textoCompleto) else it
            }
        } else {
            adicionarMensagemVoz(textoCompleto, "user")
        }
    }

    fun carregarHistorico() {
        scope.launch(Dispatchers.IO) {
            val sessions = dataRepository.chatHistoryDao().getAllSessions()
            withContext(Dispatchers.Main) {
                savedSessions = sessions
            }
        }
    }

    fun carregarSessao(id: Long) {
        scope.launch(Dispatchers.IO) {
            val history = dataRepository.chatHistoryDao().getMessagesForSession(id)
            withContext(Dispatchers.Main) {
                currentSessionId = id
                mestreIAChatHistory = history.map { 
                    MestreIAClient.ChatMessage(it.role, it.text, it.modelName)
                }
            }
        }
    }

    /**
     * Atualiza ATOMICAMENTE a mensagem do assistente na lista VIVA.
     * Acha por identidade de objeto (===) — robusto a [SISTEMA] injetadas
     * durante o loop agêntico (antes: índice fixo + sobrescrita com cópia
     * velha descartava as mensagens injetadas e a resposta final sumia).
     * Retorna a nova referência da mensagem (a lista é imutável/recriada).
     */
    private fun atualizarMsgAssistente(
        uid: String,
        transform: (MestreIAClient.ChatMessage) -> MestreIAClient.ChatMessage
    ) {
        val atual = mestreIAChatHistory
        val idx = atual.indexOfFirst { it.uid == uid }
        if (idx < 0) return
        mestreIAChatHistory = atual.toMutableList().also { it[idx] = transform(atual[idx]) }
    }

    /**
     * Finaliza a resposta do assistente.
     *
     * CAUSA RAIZ (loop longo do Forjador / Pathfinder ligado): existe UMA
     * só bolha placeholder (uid=assistantUid). Numa cadeia longa o modelo
     * responde texto MAIS DE UMA VEZ (ex.: "Perfeito! Vamos analisar..."
     * na 1ª iteração; "Tudo certo!" no fim) e entre elas o loop injeta
     * mensagens [SISTEMA]. Atualizar SEMPRE por uid sobrescrevia a bolha
     * original — que ficou ACIMA de todas as [SISTEMA] — então a resposta
     * final substituía silenciosamente a 1ª no topo e NÃO aparecia no fim
     * do chat (o trace ia até [P6] FIM ok, pois a escrita "funcionava").
     *
     * Regra: se a bolha-alvo ainda é a ÚLTIMA da lista, atualiza no lugar
     * (caso simples). Se já há mensagens depois dela (loop injetou texto),
     * anexa a resposta final como NOVA bolha no fim, visível para o usuário.
     */
    private fun finalizarMsgAssistente(
        uid: String,
        transform: (MestreIAClient.ChatMessage) -> MestreIAClient.ChatMessage
    ) {
        // Resposta da IA encerra o lote [SISTEMA]: próximo evento abre
        // uma bolha agregadora nova (não anexa nesta resposta).
        sistemaBatchUid = null
        val atual = mestreIAChatHistory
        val idx = atual.indexOfFirst { it.uid == uid }
        if (idx < 0) {
            // Placeholder some por completo: garante que a resposta apareça.
            mestreIAChatHistory = atual + transform(
                MestreIAClient.ChatMessage("model", "")
            )
            return
        }
        if (idx == atual.lastIndex) {
            mestreIAChatHistory = atual.toMutableList().also { it[idx] = transform(atual[idx]) }
        } else {
            // Bolha NOVA (uid próprio gerado pelo construtor) para não
            // colidir com o uid da bolha velha do topo.
            mestreIAChatHistory = atual + transform(
                MestreIAClient.ChatMessage("model", "")
            )
        }
    }

    fun conversar(pergunta: String, modo: String, onResult: (Boolean, String) -> Unit) {
        sistemaBatchUid = null  // novo turno = novo lote [SISTEMA]
        val userMsg = MestreIAClient.ChatMessage("user", pergunta)
        mestreIAChatHistory = mestreIAChatHistory + userMsg

        val assistantMsg = MestreIAClient.ChatMessage("model", "Pensando...", "Mestre IA")
        val assistantUid = assistantMsg.uid
        mestreIAChatHistory = mestreIAChatHistory + assistantMsg

        // Loading dinâmico: Gemini Flash Lite gera frases temáticas enquanto o RAG processa
        var loadingJob: Job? = null
        if (modo == "conversa") {
            val frasesUsadas = mutableSetOf<String>()
            loadingJob = scope.launch(Dispatchers.IO) {
                delay(2000) // espera 2s antes de começar (evita flicker em respostas rápidas)
                while (isActive) {
                    try {
                        val p = viewModel.personagem
                        val nome = p.nome.takeIf { it.isNotBlank() }
                        val desvs = p.desvantagens.map { it.nome }.filter { it.isNotBlank() }.shuffled().take(2)
                        val vants = p.vantagens.map { it.nome }.filter { it.isNotBlank() }.shuffled().take(1)
                        val tracos = (desvs + vants).joinToString(", ").takeIf { it.isNotBlank() }

                        val nomeCtx = if (nome != null) "O nome do personagem é \"$nome\"." else ""
                        val tracosCtx = if (tracos != null) "Traços da ficha: $tracos." else ""
                        val usadas = if (frasesUsadas.isNotEmpty()) "Frases já usadas (NÃO repita): ${frasesUsadas.toList().takeLast(5).joinToString("; ")}." else ""

                        val prompt = """
                            Você é um Mestre de RPG de fantasia medieval. Está consultando a ficha do jogador enquanto pensa na resposta.
                            $nomeCtx $tracosCtx
                            Crie UMA frase curta e original (máx 12 palavras) em português. Use os dados da ficha do jeito que quiser — pode ser dramático, irônico, bem-humorado, misterioso, o que parecer mais interessante. Seja imprevisível.
                            $usadas
                            Responda APENAS a frase, sem aspas, sem explicações.
                        """.trimIndent()

                        val resp = MestreIAClient.perguntarAoMestre(
                            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
                            apiKey = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_KEY,
                            workspaceSlug = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_3_1_FLASH_LITE,
                            prompt = prompt,
                            modo = "conversa",
                            desativarTools = true,
                            maxTokens = 40,
                            silencioso = true
                        )
                        val frase = resp.text.trim().ifBlank { null }
                        if (frase != null && frase !in frasesUsadas) {
                            frasesUsadas.add(frase)
                            withContext(Dispatchers.Main) {
                                atualizarMsgAssistente(assistantUid) {
                                    val textoAtual = it.text
                                    if (textoAtual == "Pensando..." || textoAtual.startsWith("⏳")) {
                                        it.copy(text = "⏳ $frase")
                                    } else it
                                }
                            }
                        }
                    } catch (_: Exception) {}
                    delay(4500)
                }
            }
        }

        // Modo é definido pelo botão "+" na UI. Exceção: se o usuário está em "geracao"
        // mas já existe histórico de chat (ficha criada, conversa em andamento), trata
        // como "analise" — evita disparar nova criação a cada mensagem de acompanhamento.
        val modoEfetivo = if (modo == "geracao" && mestreIAChatHistory.size > 2 && !aguardandoEntrevistaForjador) "analise" else modo
        scope.launch(Dispatchers.IO) {
            // ENTREVISTA (Pilar 0): na primeira mensagem do modo geracao, o Forjador
            // faz perguntas sobre cenário/NT/pontos/magia antes de criar a ficha.
            // Se o modelo detectar que o pedido já contém tudo, responde com JSON e
            // a criação começa imediatamente (sem esperar o usuário responder).
            if (modoEfetivo == "geracao" && !aguardandoEntrevistaForjador) {
                pedidoOriginalForjador = pergunta
                val entrevistaResp = MestreIAClient.perguntarAoMestre(
                    baseUrl = com.gurps.ficha.BuildConfig.MESTRE_IA_LITE_1_URL,
                    apiKey = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_KEY,
                    workspaceSlug = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_3_FLASH,
                    prompt = MestreIAPromptsForjador.gerarPromptEntrevista(pergunta),
                    history = emptyList(),
                    contextoPersonagem = "",
                    catalogo = MestreIAClient.CatalogoNomes(),
                    modo = "conversa",
                    promptSistema = MestreIAPromptsForjador.PROMPT_ENTREVISTA_SISTEMA,
                    desativarTools = true,
                    maxTokens = 512,
                    silencioso = true
                )
                val textoEntrevista = entrevistaResp.text.trim()
                // Se o modelo devolveu JSON completo, contexto já está definido → forja direto.
                // Caso contrário, exibe as perguntas e aguarda resposta do jogador.
                val ehJsonCompleto = textoEntrevista.trimStart().startsWith("{") &&
                    textoEntrevista.contains("\"completo\":true")
                if (ehJsonCompleto) {
                    // Contexto suficiente — forja imediatamente com o pedido original + contexto
                    val promptEnriquecido = "$pedidoOriginalForjador\n\n[CONTEXTO DA CAMPANHA]\n$textoEntrevista"
                    aguardandoEntrevistaForjador = false
                    mestreIAGeneratorUseCase.gerarFichaViaPlano(
                        prompt = promptEnriquecido,
                        onStatusUpdate = { status ->
                            scope.launch(Dispatchers.Main) {
                                atualizarMsgAssistente(assistantUid) { it.copy(modelName = status) }
                            }
                        },
                        onChunk = { chunk ->
                            scope.launch(Dispatchers.Main) {
                                atualizarMsgAssistente(assistantUid) {
                                    it.copy(text = it.text.replace("Pensando...", "") + chunk)
                                }
                            }
                        },
                        onResultado = { success, response ->
                            scope.launch(Dispatchers.Main) {
                                processarRespostaIA(modo, assistantUid, false, response, onResult)
                            }
                        }
                    )
                } else {
                    // Faz as perguntas ao jogador e aguarda resposta
                    aguardandoEntrevistaForjador = true
                    scope.launch(Dispatchers.Main) {
                        atualizarMsgAssistente(assistantUid) {
                            it.copy(text = textoEntrevista, modelName = "Forjador")
                        }
                        onResult(true, textoEntrevista)
                    }
                }
                return@launch
            }

            // Respostas do jogador à entrevista → forja com contexto completo
            if (aguardandoEntrevistaForjador) {
                aguardandoEntrevistaForjador = false
                val promptEnriquecido = "$pedidoOriginalForjador\n\n[RESPOSTAS DO JOGADOR]\n$pergunta"
                mestreIAGeneratorUseCase.gerarFichaViaPlano(
                    prompt = promptEnriquecido,
                    onStatusUpdate = { status ->
                        scope.launch(Dispatchers.Main) {
                            atualizarMsgAssistente(assistantUid) { it.copy(modelName = status) }
                        }
                    },
                    onChunk = { chunk ->
                        scope.launch(Dispatchers.Main) {
                            atualizarMsgAssistente(assistantUid) {
                                it.copy(text = it.text.replace("Pensando...", "") + chunk)
                            }
                        }
                    },
                    onResultado = { success, response ->
                        scope.launch(Dispatchers.Main) {
                            processarRespostaIA(modo, assistantUid, false, response, onResult)
                        }
                    }
                )
                return@launch
            }

            // Modo analise ou conversa normal (sem entrevista)
            if (modoEfetivo == "analise") {
                mestreIAGeneratorUseCase.gerarOuAnalisarFicha(
                    prompt = pergunta,
                    modo = modoEfetivo,
                    onStatusUpdate = { status ->
                        scope.launch(Dispatchers.Main) {
                            atualizarMsgAssistente(assistantUid) { it.copy(modelName = status) }
                        }
                    },
                    onChunk = { chunk ->
                        scope.launch(Dispatchers.Main) {
                            atualizarMsgAssistente(assistantUid) {
                                it.copy(text = it.text.replace("Pensando...", "") + chunk)
                            }
                        }
                    },
                    onResultado = { success, response ->
                        scope.launch(Dispatchers.Main) {
                            processarRespostaIA(modo, assistantUid, false, response, onResult)
                        }
                    }
                )
            } else {
                mestreIAUseCase.conversarComMestreIA(
                    prompt = pergunta,
                    modo = modo,
                    onStatusUpdate = { status ->
                        // Em modo conversa o loading job anima o balão — status técnico vai pro log apenas
                        if (modo != "conversa") {
                            scope.launch(Dispatchers.Main) {
                                atualizarMsgAssistente(assistantUid) {
                                    val textoAtual = it.text
                                    if (textoAtual == "Pensando..." || textoAtual.startsWith("⏳")) {
                                        it.copy(text = "⏳ $status")
                                    } else it
                                }
                            }
                        }
                    },
                    onChunk = { chunk ->
                        scope.launch(Dispatchers.Main) {
                            atualizarMsgAssistente(assistantUid) {
                                val textoLimpo = if (it.text == "Pensando..." || it.text.startsWith("⏳")) "" else it.text
                                it.copy(text = textoLimpo + chunk)
                            }
                        }
                    },
                    onResultado = { isRagUsed, response ->
                        loadingJob?.cancel()
                        scope.launch(Dispatchers.Main) {
                            processarRespostaIA(modo, assistantUid, isRagUsed, response, onResult)
                        }
                    }
                )
            }
        }
    }

    private fun processarRespostaIA(
        modo: String,
        uid: String,
        isRagUsed: Boolean,
        response: MestreIAClient.ChatResponse,
        onResult: (Boolean, String) -> Unit
    ) {
        run {
            val rawText = response.text
            android.util.Log.d("MestreIA", "Resposta Bruta: $rawText")
            android.util.Log.d("MestreIA", "Tool Calls: ${response.toolCalls.size}")

            // BLINDAGEM: qualquer exceção no parse (regex/JSON/reparo sobre
            // texto grande) NÃO pode mais matar a coroutine em silêncio e
            // deixar o chat vazio. Se algo falhar, o texto da IA aparece
            // mesmo assim (limpo do bloco ```json```).
            android.util.Log.d("MestreIA_Trace", "[P0] entrou no try, chamando Interno")
            try {
                processarRespostaIAInterno(modo, uid, isRagUsed, response, onResult)
            } catch (e: Throwable) {
                android.util.Log.e("MestreIA_Trace", "[PX] EXCEÇÃO no Interno: ${e.message}", e)
                android.util.Log.e("MestreIA", "Falha no processamento — exibindo texto bruto: ${e.message}", e)
                val textoSeguro = mestreIAUseCase.limparNarrativaParaChat(rawText)
                    .ifBlank { rawText.ifBlank { "⚠️ A resposta chegou vazia. Tente reformular o pedido." } }
                atualizarMsgAssistente(uid) {
                    it.copy(text = textoSeguro, modelName = response.modelName ?: "Mestre Sábio")
                }
                salvarSessaoChat()
                onResult(true, textoSeguro)
            }
        }
    }

    private fun processarRespostaIAInterno(
        modo: String,
        uid: String,
        isRagUsed: Boolean,
        response: MestreIAClient.ChatResponse,
        onResult: (Boolean, String) -> Unit
    ) {
        run {
            val rawText = response.text
            android.util.Log.d("MestreIA_Trace", "[I0] Interno entrou, rawText=${rawText.length} chars")

            val narrativaLimpa = mestreIAUseCase.limparNarrativaParaChat(rawText)
            android.util.Log.d("MestreIA_Trace", "[I1] narrativaLimpa=${narrativaLimpa.length} chars")

            val gsonIA = com.google.gson.GsonBuilder()
                .registerTypeAdapter(MestreIAItem::class.java, MestreIAItemDeserializer())
                .create()

            android.util.Log.d("MestreIA", "Iniciando Parse - Versao Alvo: v1.5.0-Lote84")
            
            // 1. Tool Call (Auditor com fill_character_sheet)
            val toolCallJson = response.toolCalls.find { it.name == MestreIATools.TOOL_FILL_SHEET }?.args?.toString()
            if (toolCallJson != null) android.util.Log.d("MestreIA", "Ficha detectada via Tool Call!")

            // 2. JSON no texto — localiza a RAIZ e repara truncamento.
            // GATE: só tenta se houver SINAL de JSON de ficha.
            // CAUSA RAIZ (Lote 157): o regex \{\s*"nome"\s*: rodando com
            // containsMatchIn sobre 4000+ chars de markdown sofre
            // catastrophic backtracking (ReDoS) → a coroutine TRAVA (não
            // exceção, loop de CPU; o try/catch do 152 não pega). Por isso
            // o log parava em "Iniciando Parse". Substituído por checagem
            // LITERAL O(n) (indexOf), sem regex sobre texto livre grande.
            val temSinalJson = rawText.contains("```json") ||
                rawText.contains("\"nome\"") // barato, sem backtracking
            val jsonNoTexto = if (!temSinalJson) null else run {
                // Camada 1: bloco ```json ... ``` — pega o PRIMEIRO { após a cerca (raiz)
                val fence = rawText.indexOf("```json")
                val inicioPorFence = if (fence >= 0) {
                    rawText.indexOf("{", fence).takeIf { it >= 0 }
                } else null

                // Camada 2: primeiro { que precede "nome" (literal, sem regex)
                val posNome = rawText.indexOf("\"nome\"")
                val inicioPorNome = if (posNome >= 0)
                    rawText.lastIndexOf("{", posNome).takeIf { it >= 0 } else null

                // Camada 3: primeiro { do texto
                val inicio = inicioPorFence ?: inicioPorNome
                    ?: rawText.indexOf("{").takeIf { it >= 0 }
                    ?: return@run null

                // Corta o sufixo após a cerca de fechamento, se houver
                val corpoBruto = run {
                    val fechaFence = rawText.indexOf("```", inicio)
                    if (fechaFence >= 0) rawText.substring(inicio, fechaFence)
                    else rawText.substring(inicio)
                }

                // Tenta fechar normalmente; se truncado, repara balanceando chaves/colchetes
                val fim = corpoBruto.lastIndexOf("}")
                val candidato = if (fim > 0) corpoBruto.substring(0, fim + 1) else corpoBruto
                repararJsonTruncado(candidato)
            }

            android.util.Log.d("MestreIA_Trace", "[P1] gate JSON ok, temSinalJson=$temSinalJson")
            val jsonReal = toolCallJson ?: jsonNoTexto
            android.util.Log.d("MestreIA_Trace", "[P2] jsonReal=${jsonReal?.length ?: -1} chars")

            val fichaObjeto = if (jsonReal != null) {
                try {
                    // Modo leniente: aceita quebras de linha literais em strings JSON (gerado por LLMs)
                    val reader = com.google.gson.stream.JsonReader(java.io.StringReader(jsonReal))
                    reader.isLenient = true
                    val type = object : com.google.gson.reflect.TypeToken<MestreIAResponse>() {}.type
                    gsonIA.fromJson<MestreIAResponse>(reader, type)
                } catch (e: Exception) {
                    android.util.Log.e("MestreIA", "Erro de Parse JSON: ${e.message}")
                    null
                }
            } else null
            android.util.Log.d("MestreIA_Trace", "[P3] fichaObjeto=${if (fichaObjeto==null) "null" else "ok"}")

            // Fonte única: se a ficha foi parseada, o chat mostra a história
            // DELA (historico + aparencia do JSON), não uma narrativa gerada
            // à parte que podia divergir. Fallback: narrativa limpa do texto.
            val textoChat = fichaObjeto
                ?.takeIf { it.historico.isNotBlank() || it.aparencia.isNotBlank() }
                ?.let { f ->
                    buildString {
                        if (f.historico.isNotBlank()) append(f.historico.trim())
                        if (f.aparencia.isNotBlank()) {
                            if (isNotEmpty()) append("\n\n")
                            append("**Aparência:** ${f.aparencia.trim()}")
                        }
                    }
                } ?: narrativaLimpa
            android.util.Log.d("MestreIA_Trace", "[P4] textoChat=${textoChat.length} chars, escrevendo no chat")

            // Atualiza na lista VIVA (preserva as [SISTEMA] injetadas
            // durante o loop). Antes: sobrescrevia com cópia velha → a
            // resposta final e as msgs [SISTEMA] sumiam do chat.
            finalizarMsgAssistente(uid) {
                it.copy(
                    text = textoChat,
                    modelName = response.modelName ?: "Mestre Sábio",
                    isRagUsed = isRagUsed,
                    data = fichaObjeto,
                    rawJson = jsonReal
                )
            }

            // B-completo (Lote 329): no modo "geracao" a ficha é montada
            // INCREMENTALMENTE via forjador_editar_ficha durante o loop — já está
            // aplicada na tela. Não há JSON final nem botão INTEGRAR: a criação
            // termina com uma mensagem de fechamento (texto), fichaObjeto = null.
            // Só o modo "analise" (Consultor) mantém o fluxo sugerir→INTEGRAR:
            // quando o usuário manda aplicar, a IA devolve um DELTA em JSON e aí
            // mostramos o botão. O tradutor mescla o delta sem apagar (dedup L140).

            // Retrato IA: oferece geração de retrato ao final da criação de ficha.
            // Só dispara no modo geracao e quando a ficha tem nome (foi criada agora).
            if (modo == "geracao" && viewModel.personagem.nome.isNotBlank() &&
                viewModel.personagem.imagemPersonagemUri.isBlank()) {
                mostrarDialogRetrato = true
            }

            if (fichaObjeto != null && modo == "analise") {
                fichaGeradaPendente = fichaObjeto
                relatorioValidacao = mestreIAGeneratorUseCase.gerarRelatorio(fichaObjeto)
            }
            
            android.util.Log.d("MestreIA_Trace", "[P5] chat atualizado, salvando sessão")
            salvarSessaoChat()
            onResult(true, narrativaLimpa)
            android.util.Log.d("MestreIA_Trace", "[P6] FIM ok")
        }
    }

    private fun salvarSessaoChat() {
        scope.launch(Dispatchers.IO) {
            val dao = dataRepository.chatHistoryDao()
            val titulo = mestreIAChatHistory.firstOrNull { it.role == "user" }?.text?.take(30) ?: "Nova Conversa"
            
            val sessionId = currentSessionId ?: dao.insertSession(ChatSessionEntity(title = titulo, lastUpdate = System.currentTimeMillis()))
            currentSessionId = sessionId
            
            dao.updateSessionTimestamp(sessionId, System.currentTimeMillis())
            
            val lastMsg = mestreIAChatHistory.last()
            dao.insertMessage(ChatMessageEntity(
                sessionId = sessionId,
                role = lastMsg.role,
                text = lastMsg.text,
                modelName = lastMsg.modelName,
                timestamp = System.currentTimeMillis()
            ))
            
            if (mestreIAChatHistory.size >= 2) {
                val userMsg = mestreIAChatHistory[mestreIAChatHistory.size - 2]
                if (userMsg.role == "user") {
                    dao.insertMessage(ChatMessageEntity(
                        sessionId = sessionId,
                        role = userMsg.role,
                        text = userMsg.text,
                        modelName = userMsg.modelName,
                        timestamp = System.currentTimeMillis() - 1000
                    ))
                }
            }
        }
    }

    /**
     * Gera retrato via Gemini Image e aplica na ficha.
     * [promptCustom] — descrição livre do usuário; se null usa nome/aparência/história da ficha.
     * [onFim] — callback chamado ao terminar (sucesso ou falha), para atualizar estado de loading na UI.
     */
    fun gerarRetratoIA(promptCustom: String? = null, onFim: (() -> Unit)? = null) {
        mostrarDialogRetrato = false
        val p = viewModel.personagem
        val apiKey = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_IMAGE_KEY
        val modelId = com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_IMAGE_MODEL
        if (apiKey.isBlank()) {
            android.util.Log.w("GeminiImage", "MESTRE_IA_GEMINI_IMAGE_KEY vazia — abortando")
            onFim?.invoke()
            return
        }
        // Se o usuário digitou um prompt livre, usa ele diretamente.
        // Senão, monta o prompt a partir dos campos da ficha.
        val nome     = if (promptCustom != null) "" else p.nome
        val aparencia = if (promptCustom != null) "" else p.aparencia
        val historia  = promptCustom ?: p.historico
        val statusMsg = if (promptCustom != null) "Gerando imagem..." else "Gerando retrato de ${p.nome}..."
        retratoGerandoStatus = statusMsg

        // Mostra no chat o que o usuário pediu
        if (promptCustom != null) {
            mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("user", promptCustom, "Você")
        }

        scope.launch(Dispatchers.IO) {
            val resultado = com.gurps.ficha.data.network.GeminiImageService.gerarRetrato(
                apiKey = apiKey,
                modelId = modelId,
                nome = nome,
                aparencia = aparencia,
                historia = historia
            )
            withContext(Dispatchers.Main) {
                retratoGerandoStatus = ""
                if (resultado == null) {
                    mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage(
                        "model", "⚠️ Não foi possível gerar o retrato agora. Tente pela galeria depois."
                    )
                    return@withContext
                }
                // Salva bytes como arquivo temporário e passa ao ImagemPersonagemStore
                val ctx = context ?: return@withContext
                try {
                    val tmpFile = java.io.File(ctx.cacheDir, "retrato_ia_tmp.${resultado.mimeType.substringAfter('/')}")
                    tmpFile.writeBytes(resultado.bytes)
                    val uri = android.net.Uri.fromFile(tmpFile)
                    val imagens = com.gurps.ficha.data.storage.ImagemPersonagemStore.salvarImagem(ctx, uri)
                    if (imagens != null) {
                        viewModel.atualizarImagemPersonagem(imagens.recortadaUri, imagens.originalUri)
                        viewModel.autoSaveIA()
                        mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage(
                            "model", "🎨 Retrato de ${p.nome} gerado com sucesso!"
                        )
                    } else {
                        mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage(
                            "model", "⚠️ Retrato gerado mas não foi possível salvar. Tente pela galeria."
                        )
                    }
                    tmpFile.delete()
                } catch (e: Exception) {
                    android.util.Log.e("GeminiImage", "Erro ao salvar retrato: ${e.message}")
                    mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage(
                        "model", "⚠️ Erro ao salvar o retrato. Tente pela galeria."
                    )
                } finally {
                    onFim?.invoke()
                }
            }
        }
    }

    fun confirmarIntegracao() {
        // Idempotente: duplo-clique / recomposição do Compose disparava
        // integrarRespostaNaFicha 2x → vantagens por-nível e equipamentos
        // (que não têm dedup) eram duplicados. Captura a ficha, zera o
        // pendente ANTES de integrar e trava reentrada.
        if (integracaoEmAndamento) return
        val ficha = fichaGeradaPendente ?: return
        integracaoEmAndamento = true
        fichaGeradaPendente = null
        relatorioValidacao = null
        try {
            // Delta inócuo: JSON sem nenhuma lista e sem "substituir" (ex: a
            // IA mandou "removerVantagens" inventado). Antes isso dizia
            // "integrada com sucesso" sem mudar nada — feedback enganoso.
            val temConteudo = ficha.substituir.isNotEmpty() ||
                ficha.vantagens.isNotEmpty() || ficha.desvantagens.isNotEmpty() ||
                ficha.pericias.isNotEmpty() || ficha.tecnicas.isNotEmpty() ||
                ficha.magias.isNotEmpty() || ficha.equipamentos.isNotEmpty() ||
                ficha.qualidades.isNotEmpty() || ficha.peculiaridades.isNotEmpty() ||
                ficha.nome.isNotBlank() || ficha.historico.isNotBlank()
            if (!temConteudo) {
                mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage(
                    "model",
                    "⚠️ Nada foi aplicado: o JSON não trouxe itens válidos nem o campo \"substituir\". " +
                    "Para remover/corrigir duplicatas, eu preciso reenviar a lista COMPLETA da seção com \"substituir\". " +
                    "Peça novamente, ex: \"reaplique corrigindo as duplicatas de vantagens e equipamentos\"."
                )
                return
            }
            mestreIAGeneratorUseCase.integrarRespostaNaFicha(ficha)
            mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("model", "✅ Ficha integrada com sucesso!")
            viewModel.autoSaveIA()
        } finally {
            integracaoEmAndamento = false
        }
    }

    fun descartarPendente() {
        fichaGeradaPendente = null
        relatorioValidacao = null
    }

    fun injetarEvento(texto: String) {
        // Linha do item, sem o prefixo [SISTEMA] repetido (vira bullet).
        val linha = "• " + texto.removePrefix("[SISTEMA]").trim()
        val atual = mestreIAChatHistory
        val ultima = atual.lastOrNull()

        // Continua a bolha agregadora SE ela ainda é a última do chat
        // (nada da IA foi escrito depois). Senão, abre uma bolha nova.
        val batchUid = sistemaBatchUid
        if (batchUid != null && ultima != null && ultima.uid == batchUid) {
            mestreIAChatHistory = atual.toMutableList().also { lista ->
                val idx = lista.lastIndex
                lista[idx] = lista[idx].copy(text = lista[idx].text + "\n" + linha)
            }
        } else {
            val nova = MestreIAClient.ChatMessage(
                "model",
                "[SISTEMA] Aplicando à ficha...\n" + linha
            )
            sistemaBatchUid = nova.uid
            mestreIAChatHistory = atual + nova
        }
    }

    fun executarAcao(comando: String) {
        try {
            val partes = comando.split("|")
            if (partes.size < 2) return
            val acao = partes[0].trim().uppercase()
            val detalhe = partes[1].trim()

            when (acao) {
                "PERICIA" -> {
                    val subPartes = detalhe.split(":")
                    if (subPartes.size >= 2) {
                        val nomeStr = subPartes[0].trim()
                        val nivelInt = subPartes[1].trim().filter { it.isDigit() }.toIntOrNull() ?: 12
                        adicionarPericiaManual(nomeStr, nivelInt)
                    } else {
                        adicionarPericiaManual(detalhe, 12)
                    }
                }
                "VANTAGEM" -> adicionarVantagemManual(detalhe)
                "EQUIPAMENTO" -> viewModel.adicionarEquipamento(Equipamento(nome = detalhe))
            }
            
            mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("model", "✅ Ação executada: $detalhe")
            viewModel.autoSaveIA()
        } catch (e: Exception) {
            mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("model", "❌ Erro ao executar ação: ${e.message}")
        }
    }

    private fun adicionarPericiaManual(nome: String, nivel: Int) {
        val def = dataRepository.pericias.find { it.nome.equals(nome, true) }
        if (def != null) {
            val pts = com.gurps.ficha.domain.rules.CharacterRules.calcularPontosParaNivel(
                Dificuldade.fromSigla(def.dificuldadeFixa),
                viewModel.personagem.getAtributo(def.atributoBase),
                nivel
            )
            viewModel.adicionarPericia(def, pts)
        }
    }

    private fun adicionarVantagemManual(nome: String) {
        val def = dataRepository.vantagens.find { it.nome.equals(nome, true) }
            ?: dataRepository.desvantagens.find { it.nome.equals(nome, true) }
        if (def != null) {
            if (def is VantagemDefinicao) viewModel.adicionarVantagem(def)
            else if (def is DesvantagemDefinicao) viewModel.adicionarDesvantagem(def)
        }
    }

    /**
     * Repara JSON cortado pelo limite de tokens da IA: remove o último item
     * incompleto e fecha strings, arrays e objetos abertos balanceando a pilha.
     * Ignora chaves/aspas dentro de strings e respeita escapes.
     */
    private fun repararJsonTruncado(bruto: String): String {
        // Defesa: JSON de ficha não passa de ~50k chars. Acima disso é
        // texto que vazou (markdown) — não reparar, devolver como veio.
        if (bruto.length > 50000) return bruto
        val s = bruto.trimEnd().trimEnd(',')
        val pilha = ArrayDeque<Char>()
        var emString = false
        var escape = false
        var ultimoSeguro = -1 // índice (exclusivo) após o último ',' ou '{'/'[' em nível seguro

        for (i in s.indices) {
            val c = s[i]
            if (escape) { escape = false; continue }
            if (c == '\\' && emString) { escape = true; continue }
            if (c == '"') { emString = !emString; continue }
            if (emString) continue
            when (c) {
                '{', '[' -> { pilha.addLast(if (c == '{') '}' else ']'); ultimoSeguro = i + 1 }
                '}', ']' -> { if (pilha.isNotEmpty()) pilha.removeLast(); ultimoSeguro = i + 1 }
                ',' -> ultimoSeguro = i + 1
            }
        }

        // Se cortou no meio de uma string ou de um valor, volta ao último ponto seguro
        var corpo = if (emString && ultimoSeguro in 0..s.length) s.substring(0, ultimoSeguro)
                    else s
        corpo = corpo.trimEnd().trimEnd(',')

        // Recalcula a pilha sobre o corpo já podado e fecha o que restou
        pilha.clear()
        emString = false; escape = false
        for (c in corpo) {
            if (escape) { escape = false; continue }
            if (c == '\\' && emString) { escape = true; continue }
            if (c == '"') { emString = !emString; continue }
            if (emString) continue
            when (c) {
                '{' -> pilha.addLast('}')
                '[' -> pilha.addLast(']')
                '}', ']' -> if (pilha.isNotEmpty()) pilha.removeLast()
            }
        }
        val fechamento = buildString { while (pilha.isNotEmpty()) append(pilha.removeLast()) }
        return corpo + fechamento
    }
}
