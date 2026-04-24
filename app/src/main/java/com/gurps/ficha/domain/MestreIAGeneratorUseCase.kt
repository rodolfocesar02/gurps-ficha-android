package com.gurps.ficha.domain

import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.*
import com.gurps.ficha.model.*
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.gson.Gson

/**
 * MestreIAGeneratorUseCase - ESPECIALISTA EM FORJA (Geração e Análise de Fichas).
 * Focado estritamente em extração de JSON e integridade estrutural.
 */
class MestreIAGeneratorUseCase(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)
    private val graphEngine = MestreIAGraphEngine(repository)

    fun gerarOuAnalisarFicha(
        prompt: String,
        modo: String, // "geracao" ou "analise"
        onStatusUpdate: (String) -> Unit = {},
        onChunk: (String) -> Unit = {},
        onResultado: (Boolean, MestreIAClient.ChatResponse) -> Unit
    ) {
        viewModelScope.launch {
            onStatusUpdate("Preparando Forja...")
            
            // 1. Contexto especializado para Geração
            val res = graphEngine.buscarNoGrafo(prompt)
            val cat = MestreIAClient.CatalogoNomes(
                vantagens = res.summaries.filter { it.category.contains("Vantagem", true) }.map { it.title },
                pericias = res.summaries.filter { it.category.contains("Perícia", true) }.map { it.title },
                magias = res.summaries.filter { it.category.contains("Magia", true) }.map { it.title },
                chunks = res.relatedChunks,
                ponteDeFerro = graphEngine.formatarParaIA(res)
            )

            // 2. Fila de Fallback focada em Modelos de Elite (Pro)
            val fila = listOf(
                Triple(BuildConfig.MESTRE_IA_DEEPSEEK_URL, BuildConfig.MESTRE_IA_DEEPSEEK_KEY, BuildConfig.MESTRE_IA_DEEPSEEK_MODEL),
                Triple(BuildConfig.MESTRE_IA_LITE_1_URL, BuildConfig.MESTRE_IA_GEMINI_KEY, "gemini-1.5-pro")
            )

            var sucesso = false
            for (config in fila) {
                if (config.second.isBlank()) continue
                
                onStatusUpdate("Forjando com ${config.third}...")
                
                try {
                    val response = MestreIAClient.perguntarAoMestre(
                        baseUrl = config.first,
                        apiKey = config.second,
                        workspaceSlug = config.third,
                        prompt = prompt,
                        contextoPersonagem = viewModel.personagem.toJson(),
                        catalogo = cat,
                        modo = modo,
                        onChunk = onChunk
                    )

                    if (!response.text.contains("Erro de API")) {
                        onResultado(true, response)
                        sucesso = true
                        break
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MestreIA_G", "Erro na forja: ${e.message}")
                }
            }
            
            if (!sucesso) onResultado(false, MestreIAClient.ChatResponse("Erro: Falha na forja do personagem."))
        }
    }

    fun integrarRespostaNaFicha(ficha: MestreIAResponse) {
        if (ficha.nome.isNotBlank()) viewModel.atualizarNome(ficha.nome)
        
        // Atributos
        viewModel.atualizarForca(ficha.atributos.st)
        viewModel.atualizarDestreza(ficha.atributos.dx)
        viewModel.atualizarInteligencia(ficha.atributos.iq)
        viewModel.atualizarVitalidade(ficha.atributos.ht)

        // Traços (Vantagens/Desvantagens/Qualidades)
        ficha.vantagens.forEach { adicionarVantagem(it.nome) }
        ficha.desvantagens.forEach { adicionarVantagem(it.nome) }
        ficha.qualidades.forEach { adicionarVantagem(it.nome) }
        ficha.peculiaridades.forEach { adicionarVantagem(it.nome) }

        // Perícias
        ficha.pericias.forEach { pericia ->
            val def = repository.pericias.find { it.nome.equals(pericia.nome, true) }
            if (def != null) {
                val pts = com.gurps.ficha.domain.rules.CharacterRules.calcularPontosParaNivel(
                    Dificuldade.fromSigla(def.dificuldadeFixa),
                    viewModel.personagem.getAtributo(def.atributoBase),
                    pericia.nivel
                )
                viewModel.adicionarPericia(def, pts)
            }
        }

        // Magias
        ficha.magias.forEach { magia ->
            val def = repository.magias.find { it.nome.equals(magia.nome, true) }
            if (def != null) {
                viewModel.adicionarMagia(def)
            }
        }

        // Equipamentos
        ficha.equipamentos.forEach { eq ->
            viewModel.adicionarEquipamento(Equipamento(nome = eq.nome, peso = eq.peso, custo = eq.custo, quantidade = eq.quantidade))
        }
    }

    private fun adicionarVantagem(nome: String) {
        val def = repository.vantagens.find { it.nome.equals(nome, true) }
            ?: repository.desvantagens.find { it.nome.equals(nome, true) }
        if (def != null) {
            if (def is VantagemDefinicao) viewModel.adicionarVantagem(def)
            else if (def is DesvantagemDefinicao) viewModel.adicionarDesvantagem(def)
        }
    }
}
