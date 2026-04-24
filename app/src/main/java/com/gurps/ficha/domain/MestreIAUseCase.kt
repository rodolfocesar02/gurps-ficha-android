/**
 * MestreIAUseCase - ESPECIALISTA EM AUDITORIA (Regras e Dúvidas).
 * Focado em fornecer respostas precisas baseadas no Códex de GURPS.
 */
class MestreIAUseCase(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)
    private val graphEngine = MestreIAGraphEngine(repository)

    fun conversarComMestreIA(
        prompt: String,
        modo: String = "conversa",
        onStatusUpdate: (String) -> Unit = {},
        onChunk: (String) -> Unit = {},
        onResultado: (Boolean, MestreIAClient.ChatResponse) -> Unit
    ) {
        viewModelScope.launch {
            val catalogoLocal = gerarCatalogoLocal(prompt, viewModel.mestreIAChatHistory)
            val isRagUsed = catalogoLocal.isRagSuccess
            
            // Fila de Fallback (Lote 82: Focada em Modelos LITE para velocidade)
            val fila = listOf(
                Triple(BuildConfig.MESTRE_IA_OPENROUTER_URL, BuildConfig.MESTRE_IA_OPENROUTER_2_KEY, "qwen/qwen-2.5-72b-instruct"),
                Triple(BuildConfig.MESTRE_IA_LITE_1_URL, BuildConfig.MESTRE_IA_GEMINI_KEY, "gemini-2.5-flash")
            )

            var sucesso = false
            for (config in fila) {
                if (config.second.isBlank()) continue
                onStatusUpdate("Consultando Auditor ${traduzirModeloParaMestre(config.third)}...")

                try {
                    val historicoLimitado = viewModel.mestreIAChatHistory.takeLast(6).map { it.role to it.text }
                    var resposta = MestreIAClient.perguntarAoMestre(
                        baseUrl = config.first, apiKey = config.second, workspaceSlug = config.third,
                        prompt = prompt, history = historicoLimitado, contextoPersonagem = viewModel.personagem.toJson(),
                        catalogo = catalogoLocal.catalogo, modo = modo, onChunk = onChunk
                    )

                    if (!resposta.text.contains("Erro de API")) {
                        onResultado(isRagUsed, resposta)
                        sucesso = true
                        break
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MestreIA", "Falha no Auditor: ${e.message}")
                }
            }
            if (!sucesso) onResultado(false, MestreIAClient.ChatResponse("Erro: Falha na conexão com os auditores."))
        }
    }

    suspend fun gerarCatalogoLocal(prompt: String, history: List<MestreIAClient.ChatMessage>): CatalogoLocalResult {
        val contextoRecente = history.takeLast(3).joinToString(" ") { if (it.role == "user") it.text else "" }
        val promptExpandido = "$prompt $contextoRecente".take(500)
        
        val res = graphEngine.buscarNoGrafo(promptExpandido)
        val cat = MestreIAClient.CatalogoNomes(
            vantagens = res.summaries.filter { it.category.contains("Vantagem", true) }.map { it.title },
            pericias = res.summaries.filter { it.category.contains("Perícia", true) }.map { it.title },
            magias = res.summaries.filter { it.category.contains("Magia", true) }.map { it.title },
            chunks = res.relatedChunks,
            summaries = res.summaries,
            ponteDeFerro = graphEngine.formatarParaIA(res)
        )
        return CatalogoLocalResult(cat, res.relatedChunks.isNotEmpty() || res.summaries.isNotEmpty())
    }

    private fun traduzirModeloParaMestre(id: String): String = when {
        id.contains("qwen") -> "Estrategista"
        id.contains("gemini") -> "Mensageiro"
        else -> "IA"
    }

    fun extrairJsonDeNarrativa(texto: String): String? = MestreIAClient.extrairJsonFicha(texto)?.let { "..." } // Placeholder para compatibilidade de interface
    fun limparNarrativaParaChat(texto: String): String = texto.replace(Regex("```json.*?```", RegexOption.DOT_MATCHES_ALL), "").trim()

    data class CatalogoLocalResult(val catalogo: MestreIAClient.CatalogoNomes, val isRagSuccess: Boolean)
}
