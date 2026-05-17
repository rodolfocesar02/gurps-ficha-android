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

    fun conversar(pergunta: String, modo: String, onResult: (Boolean, String) -> Unit) {
        val userMsg = MestreIAClient.ChatMessage("user", pergunta)
        mestreIAChatHistory = mestreIAChatHistory + userMsg
        
        val assistantMsg = MestreIAClient.ChatMessage("model", "Pensando...", "Mestre IA")
        mestreIAChatHistory = mestreIAChatHistory + assistantMsg
        val assistantIndex = mestreIAChatHistory.size - 1

        // Modo é definido exclusivamente pelo botão "+" na UI — nunca auto-detectado
        scope.launch(Dispatchers.IO) {
            if (modo == "geracao" || modo == "analise") {
                mestreIAGeneratorUseCase.gerarOuAnalisarFicha(
                    prompt = pergunta,
                    modo = modo,
                    onStatusUpdate = { status ->
                        scope.launch(Dispatchers.Main) {
                            val history = mestreIAChatHistory.toMutableList()
                            if (assistantIndex >= 0 && assistantIndex < history.size) {
                                history[assistantIndex] = history[assistantIndex].copy(modelName = status)
                                mestreIAChatHistory = history
                            }
                        }
                    },
                    onChunk = { chunk ->
                        scope.launch(Dispatchers.Main) {
                            val history = mestreIAChatHistory.toMutableList()
                            if (assistantIndex >= 0 && assistantIndex < history.size) {
                                val currentText = history[assistantIndex].text.replace("Pensando...", "")
                                history[assistantIndex] = history[assistantIndex].copy(text = currentText + chunk)
                                mestreIAChatHistory = history
                            }
                        }
                    },
                    onResultado = { success, response ->
                        scope.launch(Dispatchers.Main) {
                            processarRespostaIA(modo, assistantIndex, false, response, onResult)
                        }
                    }
                )
            } else {
                mestreIAUseCase.conversarComMestreIA(
                    prompt = pergunta,
                    modo = modo,
                    onStatusUpdate = { status ->
                        scope.launch(Dispatchers.Main) {
                            val history = mestreIAChatHistory.toMutableList()
                            if (assistantIndex >= 0 && assistantIndex < history.size) {
                                history[assistantIndex] = history[assistantIndex].copy(modelName = status)
                                mestreIAChatHistory = history
                            }
                        }
                    },
                    onChunk = { chunk ->
                        scope.launch(Dispatchers.Main) {
                            val history = mestreIAChatHistory.toMutableList()
                            if (assistantIndex >= 0 && assistantIndex < history.size) {
                                val currentText = history[assistantIndex].text.replace("Pensando...", "")
                                history[assistantIndex] = history[assistantIndex].copy(text = currentText + chunk)
                                mestreIAChatHistory = history
                            }
                        }
                    },
                    onResultado = { isRagUsed, response ->
                        scope.launch(Dispatchers.Main) {
                            processarRespostaIA(modo, assistantIndex, isRagUsed, response, onResult)
                        }
                    }
                )
            }
        }
    }

    private fun processarRespostaIA(
        modo: String,
        assistantIndex: Int,
        isRagUsed: Boolean,
        response: MestreIAClient.ChatResponse,
        onResult: (Boolean, String) -> Unit
    ) {
        val history = mestreIAChatHistory.toMutableList()
        if (assistantIndex >= 0 && assistantIndex < history.size) {
            val rawText = response.text
            android.util.Log.d("MestreIA", "Resposta Bruta: $rawText")
            android.util.Log.d("MestreIA", "Tool Calls: ${response.toolCalls.size}")
            
            val narrativaLimpa = mestreIAUseCase.limparNarrativaParaChat(rawText)
            
            val gsonIA = com.google.gson.GsonBuilder()
                .registerTypeAdapter(MestreIAItem::class.java, MestreIAItemDeserializer())
                .create()

            android.util.Log.d("MestreIA", "Iniciando Parse - Versao Alvo: v1.5.0-Lote84")
            
            // 1. Tool Call (Auditor com fill_character_sheet)
            val toolCallJson = response.toolCalls.find { it.name == MestreIATools.TOOL_FILL_SHEET }?.args?.toString()
            if (toolCallJson != null) android.util.Log.d("MestreIA", "Ficha detectada via Tool Call!")

            // 2. JSON no texto — busca pelo objeto que começa com "nome" (campo raiz da MestreIAResponse)
            // Procura da ÚLTIMA ocorrência de '{"nome"' para ignorar narrativa combinada antes do JSON
            val jsonNoTexto = run {
                val marcadores = listOf("""{"nome"""", """{ "nome"""")
                val inicio = marcadores.mapNotNull { m ->
                    val idx = rawText.lastIndexOf(m)
                    if (idx >= 0) idx else null
                }.minOrNull()
                if (inicio != null) {
                    val fim = rawText.lastIndexOf("}")
                    if (fim > inicio) rawText.substring(inicio, fim + 1) else null
                } else if (rawText.contains("{") && rawText.contains("}")) {
                    // Fallback clássico
                    rawText.substring(rawText.indexOf("{"), rawText.lastIndexOf("}") + 1)
                } else null
            }

            val jsonReal = toolCallJson ?: jsonNoTexto

            val fichaObjeto = if (jsonReal != null) {
                try {
                    gsonIA.fromJson(jsonReal, MestreIAResponse::class.java)
                } catch (e: Exception) {
                    android.util.Log.e("MestreIA", "Erro de Parse JSON: ${e.message}")
                    null
                }
            } else null

            history[assistantIndex] = history[assistantIndex].copy(
                text = narrativaLimpa,
                modelName = response.modelName ?: "Mestre Sábio",
                isRagUsed = isRagUsed,
                data = fichaObjeto,
                rawJson = jsonReal
            )
            mestreIAChatHistory = history

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
        fichaGeradaPendente?.let {
            mestreIAGeneratorUseCase.integrarRespostaNaFicha(it)
            fichaGeradaPendente = null
            relatorioValidacao = null
            mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("model", "✅ Ficha integrada com sucesso!")
            viewModel.autoSaveIA()
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
}
