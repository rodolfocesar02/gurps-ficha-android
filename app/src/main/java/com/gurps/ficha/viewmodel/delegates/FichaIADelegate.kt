package com.gurps.ficha.viewmodel.delegates

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.data.network.MestreIAResponse
import com.gurps.ficha.domain.MestreIAUseCase
import com.gurps.ficha.model.Personagem
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

    var mestreIAChatHistory by mutableStateOf<List<MestreIAClient.ChatMessage>>(emptyList())
    var fichaGeradaPendente by mutableStateOf<MestreIAResponse?>(null)
    var mestreIAMode by mutableStateOf("conversa") // Default: Dúvidas/Free

    var currentSessionId by mutableStateOf<Long?>(null)
    var savedSessions by mutableStateOf<List<ChatSessionEntity>>(emptyList())

    fun limparChat() {
        mestreIAChatHistory = emptyList()
        currentSessionId = null
    }

    fun carregarHistorico() {
        scope.launch {
            savedSessions = viewModel.dataRepository.getDatabase(viewModel.getApplication()).chatHistoryDao().getAllSessions()
        }
    }

    fun carregarSessao(sessionId: Long) {
        scope.launch {
            val messages = viewModel.dataRepository.getDatabase(viewModel.getApplication()).chatHistoryDao().getMessagesForSession(sessionId)
            mestreIAChatHistory = messages.map { 
                MestreIAClient.ChatMessage(it.role, it.text, modelName = it.modelName) 
            }
            currentSessionId = sessionId
        }
    }

    private suspend fun garantirSessao(primeiraMensagem: String): Long {
        val dao = viewModel.dataRepository.getDatabase(viewModel.getApplication()).chatHistoryDao()
        val sid = currentSessionId ?: run {
            val title = if (primeiraMensagem.length > 30) primeiraMensagem.take(30) + "..." else primeiraMensagem
            val newId = dao.insertSession(ChatSessionEntity(title = title, lastUpdate = System.currentTimeMillis()))
            currentSessionId = newId
            newId
        }
        return sid
    }

    private fun salvarMensagemNoBanco(role: String, text: String, modelName: String? = null) {
        val sid = currentSessionId ?: return
        scope.launch(Dispatchers.IO) {
            val dao = viewModel.dataRepository.getDatabase(viewModel.getApplication()).chatHistoryDao()
            dao.insertMessage(ChatMessageEntity(
                sessionId = sid,
                role = role,
                text = text,
                timestamp = System.currentTimeMillis(),
                modelName = modelName
            ))
            // Atualiza timestamp da sessão
            val session = dao.getAllSessions().find { it.id == sid }
            session?.let {
                dao.insertSession(it.copy(lastUpdate = System.currentTimeMillis()))
            }
        }
    }

    fun conversar(
        pergunta: String,
        modo: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val userMsg = MestreIAClient.ChatMessage("user", pergunta)
        mestreIAChatHistory = mestreIAChatHistory + userMsg

        scope.launch {
            val sid = garantirSessao(pergunta)
            salvarMensagemNoBanco("user", pergunta)

            // LOTE 54: Adiciona mensagem vazia para ser preenchida via stream
            val assistantMsg = MestreIAClient.ChatMessage("model", "", modelName = "Mestre IA...")
            withContext(Dispatchers.Main) {
                mestreIAChatHistory = mestreIAChatHistory + assistantMsg
            }
            val assistantIndex = mestreIAChatHistory.size - 1

            mestreIAUseCase.conversarComMestreIA(
                prompt = pergunta,
                modo = mestreIAMode,
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
                            val currentMsg = history[assistantIndex]
                            history[assistantIndex] = currentMsg.copy(text = currentMsg.text + chunk)
                            mestreIAChatHistory = history
                        }
                    }
                }
            ) { isRagUsed, response ->
                scope.launch(Dispatchers.Main) {
                    val history = mestreIAChatHistory.toMutableList()
                    if (assistantIndex >= 0 && assistantIndex < history.size) {
                        val rawText = response.text
                        val jsonExtraido = response.rawJson ?: mestreIAUseCase.extrairJsonDeNarrativa(rawText)
                        val narrativaLimpa = mestreIAUseCase.limparNarrativaParaChat(rawText)
                        
                        var erroParse = false
                        val fichaObjeto = jsonExtraido?.let { 
                            try {
                                com.google.gson.Gson().fromJson(it, MestreIAResponse::class.java)
                            } catch (e: Exception) {
                                erroParse = true
                                null
                            }
                        }

                        val textoFinal = when {
                            erroParse -> "⚠️ O Mestre gerou a ficha, mas o código contém um erro técnico. Peça para ele: 'Corrija o código JSON da ficha'."
                            fichaObjeto != null && narrativaLimpa.isBlank() -> "📦 Ficha pronta com sucesso! Clique no botão abaixo para integrar."
                            else -> narrativaLimpa
                        }

                        history[assistantIndex] = history[assistantIndex].copy(
                            text = if (jsonExtraido != null) textoFinal else rawText,
                            modelName = response.modelName,
                            isRagUsed = isRagUsed,
                            latencyMs = response.latencyMs,
                            data = fichaObjeto,
                            rawJson = jsonExtraido
                        )
                        mestreIAChatHistory = history
                        fichaGeradaPendente = fichaObjeto

                        if (modo == "geracao") {
                            val msgResultado = when {
                                erroParse -> "Falha técnica no código do Mestre."
                                fichaGeradaPendente != null -> "Ficha disponível no balão do chat!"
                                else -> "Mestre IA ainda está processando..."
                            }
                            onResult(true, msgResultado)
                        } else {
                            onResult(true, "Resposta recebida.")
                        }

                        salvarMensagemNoBanco("model", response.text, response.modelName)
                    }
                }
            }
        }
    }

    fun confirmarIntegracao() {
        val ficha = fichaGeradaPendente ?: return
        scope.launch {
            mestreIAUseCase.integrarRespostaNaFicha(ficha)
            viewModel.autoSaveIA()
            fichaGeradaPendente = null
        }
    }

    fun descartarPendente() {
        fichaGeradaPendente = null
    }

    fun executarAcao(comando: String) {
        scope.launch {
            try {
                val partes = comando.removePrefix("[").removeSuffix("]").split(":")
                if (partes.size < 3) return@launch
                
                val tipo = partes[1].trim().uppercase()
                val detalhe = partes[2].trim()

                when (tipo) {
                    "ATRIBUTO" -> {
                        val subPartes = detalhe.split(" ")
                        if (subPartes.size >= 2) {
                            val attr = subPartes[0].uppercase()
                            val valor = subPartes[1].toIntOrNull() ?: return@launch
                            when (attr) {
                                "ST" -> viewModel.atualizarForca(valor)
                                "DX" -> viewModel.atualizarDestreza(valor)
                                "IQ" -> viewModel.atualizarInteligencia(valor)
                                "HT" -> viewModel.atualizarVitalidade(valor)
                            }
                        }
                    }
                    "PERICIA" -> {
                        val subPartes = detalhe.split(":")
                        if (subPartes.size >= 2) {
                            val nomeStr = subPartes[0]
                            val nivelInt = subPartes[1].toIntOrNull() ?: 12
                            mestreIAUseCase.adicionarPericia(nomeStr, nivelInt)
                        } else {
                            mestreIAUseCase.adicionarPericia(detalhe, 12)
                        }
                    }
                    "VANTAGEM" -> mestreIAUseCase.adicionarVantagem(detalhe)
                    "EQUIPAMENTO" -> mestreIAUseCase.adicionarEquipamento(com.gurps.ficha.data.network.MestreIAEquipamento(nome = detalhe))
                }
                
                mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("model", "✅ Ação executada: $detalhe")
                viewModel.autoSaveIA()
            } catch (e: Exception) {
                mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("model", "❌ Erro ao executar ação: ${e.message}")
            }
        }
    }

    private fun getCatalogNames() = MestreIAClient.CatalogoNomes()
}
