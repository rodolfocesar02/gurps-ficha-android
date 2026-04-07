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

class FichaIADelegate(
    private val viewModel: FichaViewModel,
    private val dataRepository: DataRepository,
    private val scope: CoroutineScope
) {
    private val mestreIAUseCase by lazy { MestreIAUseCase(viewModel, dataRepository) }

    var mestreIAChatHistory by mutableStateOf<List<MestreIAClient.ChatMessage>>(emptyList())
    var fichaGeradaPendente by mutableStateOf<MestreIAResponse?>(null)

    fun limparChat() {
        mestreIAChatHistory = emptyList()
    }

    fun conversar(
        pergunta: String,
        modo: String,
        baseUrl: String,
        apiKey: String,
        workspaceSlug: String,
        onResult: (Boolean, String) -> Unit
    ) {
        mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("user", pergunta)

        scope.launch {
            val contexto = if (modo != "geracao") "Ficha de ${viewModel.personagem.nome}: ${viewModel.personagem.toJson()}" else null
            
            val respostaTexto: String? = withContext(Dispatchers.IO) {
                MestreIAClient.perguntarAoMestre(
                    baseUrl, apiKey, pergunta, workspaceSlug, mestreIAChatHistory.dropLast(1),
                    contexto, getCatalogNames(), modo
                )
            }

            if (respostaTexto != null) {
                mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("model", respostaTexto)
                if (modo == "geracao") {
                    fichaGeradaPendente = MestreIAClient.extrairJsonFicha(respostaTexto)
                    onResult(true, if (fichaGeradaPendente != null) "Ficha pronta para revisão!" else "Ficha gerada em texto.")
                } else onResult(true, "Resposta recebida.")
            } else onResult(false, "Falha na conexão com Mestre Digital.")
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
                    "EQUIPAMENTO" -> mestreIAUseCase.adicionarEquipamento(detalhe)
                }
                
                mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("model", "✅ Ação executada: $detalhe")
                viewModel.autoSaveIA()
            } catch (e: Exception) {
                mestreIAChatHistory = mestreIAChatHistory + MestreIAClient.ChatMessage("model", "❌ Erro ao executar ação: ${e.message}")
            }
        }
    }

    private fun getCatalogNames() = MestreIAClient.CatalogoNomes(
        vantagens = dataRepository.vantagens.map { it.nome },
        desvantagens = dataRepository.desvantagens.map { it.nome },
        pericias = dataRepository.pericias.map { it.nome },
        magias = dataRepository.magias.map { it.nome }
    )
}
