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
        val userMsg = MestreIAClient.ChatMessage("user", pergunta)
        mestreIAChatHistory = mestreIAChatHistory + userMsg

        val assistantMsg = MestreIAClient.ChatMessage("model", "Pensando...", "Mestre IA")
        val assistantUid = assistantMsg.uid
        mestreIAChatHistory = mestreIAChatHistory + assistantMsg

        // Modo é definido exclusivamente pelo botão "+" na UI — nunca auto-detectado
        scope.launch(Dispatchers.IO) {
            if (modo == "geracao" || modo == "analise") {
                mestreIAGeneratorUseCase.gerarOuAnalisarFicha(
                    prompt = pergunta,
                    modo = modo,
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
                    onResultado = { isRagUsed, response ->
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

            // "geracao" sempre integra. "analise" (Consultor) é conversa
            // fluida: quando o usuário só pergunta, a IA responde texto (sem
            // JSON → fichaObjeto null → sem botão). Quando o usuário manda
            // aplicar, a IA devolve um DELTA em JSON → aí sim mostra INTEGRAR.
            // O tradutor mescla o delta sem apagar o resto (dedup do Lote 140).
            if (fichaObjeto != null && (modo == "geracao" || modo == "analise")) {
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
