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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.gurps.ficha.data.storage.ChatSessionEntity
import com.gurps.ficha.data.storage.ChatMessageEntity
import java.util.Date

class FichaIADelegate(
    private val viewModel: FichaViewModel,
    private val dataRepository: DataRepository,
    private val scope: CoroutineScope
) {
    private val mestreIAUseCase by lazy { MestreIAUseCase(viewModel, dataRepository) }
    private val mestreIAGeneratorUseCase by lazy { MestreIAGeneratorUseCase(viewModel, dataRepository) }

    var mestreIAChatHistory by mutableStateOf<List<MestreIAClient.ChatMessage>>(emptyList())
    var fichaGeradaPendente by mutableStateOf<MestreIAResponse?>(null)
    var relatorioValidacao by mutableStateOf<RelatorioValidacao?>(null)
    var mestreIAMode by mutableStateOf("conversa") // Default: Dúvidas/Free

    var currentSessionId by mutableStateOf<Long?>(null)
    var savedSessions by mutableStateOf<List<ChatSessionEntity>>(emptyList())
    private var sincroniaExecutadaNestaSessao = false
    @Volatile private var integracaoEmAndamento = false

    fun verificarSincroniaAutomatica() {
        if (sincroniaExecutadaNestaSessao) return
        sincroniaExecutadaNestaSessao = true
        
        scope.launch(Dispatchers.IO) {
            android.util.Log.i("MestreIA_Auditoria", "VERIFICANDO INTEGRIDADE DO CÓDEX (Início de Sessão)")
            dataRepository.sincronizarCodexSeNecessario()
        }
    }

    fun limparChat() {
        mestreIAChatHistory = emptyList()
        currentSessionId = null
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
        ref: MestreIAClient.ChatMessage,
        transform: (MestreIAClient.ChatMessage) -> MestreIAClient.ChatMessage
    ): MestreIAClient.ChatMessage {
        val atual = mestreIAChatHistory
        val idx = atual.indexOfFirst { it === ref }
        if (idx < 0) return ref
        val nova = transform(atual[idx])
        mestreIAChatHistory = atual.toMutableList().also { it[idx] = nova }
        return nova
    }

    fun conversar(pergunta: String, modo: String, onResult: (Boolean, String) -> Unit) {
        val userMsg = MestreIAClient.ChatMessage("user", pergunta)
        mestreIAChatHistory = mestreIAChatHistory + userMsg

        var assistantRef = MestreIAClient.ChatMessage("model", "Pensando...", "Mestre IA")
        mestreIAChatHistory = mestreIAChatHistory + assistantRef

        // Modo é definido exclusivamente pelo botão "+" na UI — nunca auto-detectado
        scope.launch(Dispatchers.IO) {
            if (modo == "geracao" || modo == "analise") {
                mestreIAGeneratorUseCase.gerarOuAnalisarFicha(
                    prompt = pergunta,
                    modo = modo,
                    onStatusUpdate = { status ->
                        scope.launch(Dispatchers.Main) {
                            assistantRef = atualizarMsgAssistente(assistantRef) { it.copy(modelName = status) }
                        }
                    },
                    onChunk = { chunk ->
                        scope.launch(Dispatchers.Main) {
                            assistantRef = atualizarMsgAssistente(assistantRef) {
                                it.copy(text = it.text.replace("Pensando...", "") + chunk)
                            }
                        }
                    },
                    onResultado = { success, response ->
                        scope.launch(Dispatchers.Main) {
                            processarRespostaIA(modo, assistantRef, false, response, onResult)
                        }
                    }
                )
            } else {
                mestreIAUseCase.conversarComMestreIA(
                    prompt = pergunta,
                    modo = modo,
                    onStatusUpdate = { status ->
                        scope.launch(Dispatchers.Main) {
                            assistantRef = atualizarMsgAssistente(assistantRef) { it.copy(modelName = status) }
                        }
                    },
                    onChunk = { chunk ->
                        scope.launch(Dispatchers.Main) {
                            assistantRef = atualizarMsgAssistente(assistantRef) {
                                it.copy(text = it.text.replace("Pensando...", "") + chunk)
                            }
                        }
                    },
                    onResultado = { isRagUsed, response ->
                        scope.launch(Dispatchers.Main) {
                            processarRespostaIA(modo, assistantRef, isRagUsed, response, onResult)
                        }
                    }
                )
            }
        }
    }

    private fun processarRespostaIA(
        modo: String,
        ref: MestreIAClient.ChatMessage,
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
            try {
                processarRespostaIAInterno(modo, ref, isRagUsed, response, onResult)
            } catch (e: Throwable) {
                android.util.Log.e("MestreIA", "Falha no processamento — exibindo texto bruto: ${e.message}", e)
                val textoSeguro = mestreIAUseCase.limparNarrativaParaChat(rawText)
                    .ifBlank { rawText.ifBlank { "⚠️ A resposta chegou vazia. Tente reformular o pedido." } }
                atualizarMsgAssistente(ref) {
                    it.copy(text = textoSeguro, modelName = response.modelName ?: "Mestre Sábio")
                }
                salvarSessaoChat()
                onResult(true, textoSeguro)
            }
        }
    }

    private fun processarRespostaIAInterno(
        modo: String,
        ref: MestreIAClient.ChatMessage,
        isRagUsed: Boolean,
        response: MestreIAClient.ChatResponse,
        onResult: (Boolean, String) -> Unit
    ) {
        run {
            val rawText = response.text
            
            val narrativaLimpa = mestreIAUseCase.limparNarrativaParaChat(rawText)
            
            val gsonIA = com.google.gson.GsonBuilder()
                .registerTypeAdapter(MestreIAItem::class.java, MestreIAItemDeserializer())
                .create()

            android.util.Log.d("MestreIA", "Iniciando Parse - Versao Alvo: v1.5.0-Lote84")
            
            // 1. Tool Call (Auditor com fill_character_sheet)
            val toolCallJson = response.toolCalls.find { it.name == MestreIATools.TOOL_FILL_SHEET }?.args?.toString()
            if (toolCallJson != null) android.util.Log.d("MestreIA", "Ficha detectada via Tool Call!")

            // 2. JSON no texto — localiza a RAIZ e repara truncamento.
            // GATE: só tenta se houver SINAL REAL de JSON de ficha (bloco
            // ```json``` OU {"nome":). Resposta de análise/consultor é texto
            // markdown (com '{' em exemplos/tabelas) — sem este gate o
            // repararJsonTruncado mastiga markdown e TRAVA a coroutine.
            val regexNome = Regex("""\{\s*"nome"\s*:""")
            val temSinalJson = rawText.contains("```json") || regexNome.containsMatchIn(rawText)
            val jsonNoTexto = if (!temSinalJson) null else run {
                // Camada 1: bloco ```json ... ``` — pega o PRIMEIRO { após a cerca (raiz)
                val fence = rawText.indexOf("```json")
                val inicioPorFence = if (fence >= 0) {
                    rawText.indexOf("{", fence).takeIf { it >= 0 }
                } else null

                // Camada 2: primeiro {"nome": do texto (objeto raiz começa pela chave nome)
                val inicioPorNome = regexNome.find(rawText)?.range?.first

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

            val jsonReal = toolCallJson ?: jsonNoTexto

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

            // Atualiza na lista VIVA (preserva as [SISTEMA] injetadas
            // durante o loop). Antes: sobrescrevia com cópia velha → a
            // resposta final e as msgs [SISTEMA] sumiam do chat.
            atualizarMsgAssistente(ref) {
                it.copy(
                    text = textoChat,
                    modelName = response.modelName ?: "Mestre Sábio",
                    isRagUsed = isRagUsed,
                    data = fichaObjeto,
                    rawJson = jsonReal
                )
            }

            // "geracao" sempre integra. "analise" (Consultor) é conversa
            // fluida: quando o usuário só pergunta, a IA responde texto (sem
            // JSON → fichaObjeto null → sem botão). Quando o usuário manda
            // aplicar, a IA devolve um DELTA em JSON → aí sim mostra INTEGRAR.
            // O tradutor mescla o delta sem apagar o resto (dedup do Lote 140).
            if (fichaObjeto != null && (modo == "geracao" || modo == "analise")) {
                fichaGeradaPendente = fichaObjeto
                relatorioValidacao = mestreIAGeneratorUseCase.gerarRelatorio(fichaObjeto)
            }
            
            salvarSessaoChat()
            onResult(true, narrativaLimpa)
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
        mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("model", texto)
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
