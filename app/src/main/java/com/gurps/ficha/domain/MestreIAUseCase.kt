package com.gurps.ficha.domain

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAResponse
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.model.*
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Atua como o "Juiz" entre a IA e a Ficha Real.
 * Valida os nomes sugeridos pela IA contra os JSONs oficiais do App.
 * Possui busca fuzzy para equiparar nomes semelhantes.
 */
class MestreIAUseCase(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    /**
     * Wrapper para o resultado do catálogo filtrado pelo RAG Local.
     */
    data class CatalogoLocalResult(
        val catalogo: MestreIAClient.CatalogoNomes,
        val isRagSuccess: Boolean
    )

    /**
     * Calcula a "distância" entre dois textos normalizados.
     */
    private fun similaridade(a: String, b: String): Double {
        val na = a.trim().lowercase().replace(Regex("[^a-záàâãéèêíïóôõúüç\\s]"), "").replace("\\s+".toRegex(), " ")
        val nb = b.trim().lowercase().replace(Regex("[^a-záàâãéèêíïóôõúüç\\s]"), "").replace("\\s+".toRegex(), " ")
        if (na == nb) return 1.0
        if (na.isEmpty() || nb.isEmpty()) return 0.0

        if (na.contains(nb) || nb.contains(na)) return 0.85

        val maxLen = maxOf(na.length, nb.length)
        if (maxLen == 0) return 1.0
        
        val dp = Array(na.length + 1) { IntArray(nb.length + 1) }
        for (i in 0..na.length) dp[i][0] = i
        for (j in 0..nb.length) dp[0][j] = j
        for (i in 1..na.length) {
            for (j in 1..nb.length) {
                val cost = if (na[i - 1] == nb[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return 1.0 - (dp[na.length][nb.length].toDouble() / maxLen)
    }

    fun integrarRespostaNaFicha(resposta: MestreIAResponse) {
        android.util.Log.d("MestreIA", "=== Integrando resposta na ficha ===")
        
        viewModel.atualizarNome(resposta.nome)
        viewModel.definirBasesAtributosPrimarios(
            forcaBase = resposta.atributos.st,
            destrezaBase = resposta.atributos.dx,
            inteligenciaBase = resposta.atributos.iq,
            vitalidadeBase = resposta.atributos.ht
        )

        // 2. Integrar Vantagens
        resposta.vantagens.forEach { nomeSugerido ->
            var definicaoOficial = repository.vantagens.firstOrNull { it.nome.trim().equals(nomeSugerido.trim(), ignoreCase = true) }
            if (definicaoOficial == null) {
                val melhorMatch = repository.vantagens.map { it to similaridade(it.nome, nomeSugerido) }.filter { it.second >= 0.80 }.maxByOrNull { it.second }
                if (melhorMatch != null) definicaoOficial = melhorMatch.first
            }
            if (definicaoOficial != null) viewModel.adicionarVantagem(definicaoOficial)
        }

        // 3. Integrar Desvantagens
        resposta.desvantagens.forEach { nomeSugerido ->
            var definicaoOficial = repository.desvantagens.firstOrNull { it.nome.trim().equals(nomeSugerido.trim(), ignoreCase = true) }
            if (definicaoOficial == null) {
                val melhorMatch = repository.desvantagens.map { it to similaridade(it.nome, nomeSugerido) }.filter { it.second >= 0.80 }.maxByOrNull { it.second }
                if (melhorMatch != null) definicaoOficial = melhorMatch.first
            }
            if (definicaoOficial != null) viewModel.adicionarDesvantagem(definicaoOficial)
        }

        // 4. Integrar Perícias
        resposta.pericias.forEach { periciaSugerida ->
            var definicaoOficial = repository.pericias.firstOrNull { it.nome.trim().equals(periciaSugerida.nome.trim(), ignoreCase = true) }
            if (definicaoOficial == null) {
                val melhorMatch = repository.pericias.map { it to similaridade(it.nome, periciaSugerida.nome) }.filter { it.second >= 0.80 }.maxByOrNull { it.second }
                if (melhorMatch != null) definicaoOficial = melhorMatch.first
            }
            if (definicaoOficial != null) {
                val attrValor = when (definicaoOficial.atributoBase.uppercase()) {
                    "ST" -> resposta.atributos.st
                    "DX" -> resposta.atributos.dx
                    "IQ" -> resposta.atributos.iq
                    "HT" -> resposta.atributos.ht
                    else -> 10
                }
                val pontos = CharacterRules.calcularPontosParaNivel(com.gurps.ficha.model.Dificuldade.fromSigla(definicaoOficial.dificuldadeFixa), attrValor, periciaSugerida.nivel)
                viewModel.adicionarPericia(definicaoOficial, pts = pontos)
            }
        }

        // 5. Integrar Magias
        resposta.magias.forEach { mSugerida ->
            var defOficial: MagiaDefinicao? = repository.magias.firstOrNull { it.nome.trim().equals(mSugerida.trim(), ignoreCase = true) }
            if (defOficial == null) {
                var melhorScore = 0.0
                for (mOficial in repository.magias) {
                    val score = similaridade(mOficial.nome, mSugerida)
                    if (score >= 0.80 && score > melhorScore) {
                        melhorScore = score
                        defOficial = mOficial
                    }
                }
            }
            defOficial?.let { viewModel.adicionarMagia(it, ignora = true) }
        }

        // 6. Qualidades
        resposta.qualidades.forEach { if (it.isNotBlank()) viewModel.adicionarQualidade(it.trim()) }

        // 7. Peculiaridades
        resposta.peculiaridades.forEach { if (it.isNotBlank()) viewModel.adicionarPeculiaridade(it.trim()) }

        // 8. Aparência e Histórico
        if (resposta.aparencia.isNotBlank()) viewModel.atualizarAparencia(resposta.aparencia.trim())
        if (resposta.historico.isNotBlank()) viewModel.atualizarHistorico(resposta.historico.trim())

        // 9. Integrar Equipamentos
        resposta.equipamentos.forEach { eq ->
            if (eq.nome.isNotBlank()) {
                val armaMatch = repository.armasCatalogo.firstOrNull { similaridade(it.nome, eq.nome) >= 0.85 }
                val armaduraMatch = repository.armadurasCatalogo.firstOrNull { similaridade(it.nome, eq.nome) >= 0.85 }
                if (armaMatch != null) viewModel.adicionarEquipamentoArma(armaMatch)
                else if (armaduraMatch != null) viewModel.adicionarEquipamentoArmadura(armaduraMatch)
                else viewModel.adicionarEquipamento(com.gurps.ficha.model.Equipamento(nome = eq.nome, peso = eq.peso, custo = eq.custo, quantidade = eq.quantidade))
            }
        }
    }

    /**
     * Motor de conversação principal do Mestre IA PRIME.
     */
    fun conversarComMestreIA(
        prompt: String,
        modo: String = "conversa",
        baseUrl: String,
        apiKey: String,
        workspaceSlug: String,
        onResultado: (Boolean, MestreIAClient.ChatResponse) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val catalogoLocal = gerarCatalogoLocal(prompt)
                val respostaIA = MestreIAClient.perguntarAoMestre(
                    baseUrl = baseUrl,
                    apiKey = apiKey,
                    workspaceSlug = workspaceSlug,
                    prompt = prompt,
                    history = viewModel.mestreIAChatHistory.toList(),
                    contextoPersonagem = viewModel.personagem.toJson(),
                    catalogo = catalogoLocal.catalogo,
                    modo = modo
                )
                onResultado(catalogoLocal.isRagSuccess, respostaIA)
            } catch (e: Exception) {
                onResultado(false, MestreIAClient.ChatResponse("Erro inesperado: ${e.message}"))
            }
        }
    }

    fun gerarCatalogoLocal(userPrompt: String): CatalogoLocalResult {
        val rag = MestreIARagEngine.buscarContexto(userPrompt, repository)
        val hasHits = rag.vantagens.isNotEmpty() || rag.desvantagens.isNotEmpty() || 
                      rag.pericias.isNotEmpty() || rag.magias.isNotEmpty()
        val catalogo = MestreIAClient.CatalogoNomes(
            vantagens = rag.vantagens,
            desvantagens = rag.desvantagens,
            pericias = rag.pericias,
            magias = rag.magias
        )
        return CatalogoLocalResult(catalogo, hasHits)
    }
}
