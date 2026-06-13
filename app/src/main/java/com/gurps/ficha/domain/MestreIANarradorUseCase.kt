package com.gurps.ficha.domain

import android.content.Context
import android.util.Log
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.data.network.MestreIAPromptsNarrador
import com.gurps.ficha.domain.saga.NarradorOutputValidator
import com.gurps.ficha.domain.saga.NarradorToolExecutor
import com.gurps.ficha.domain.saga.NarradorTools
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ════════════════════════════════════════════════════════════════
 * MESTRE NARRADOR (modo "saga") — conduz a aventura solo
 * ════════════════════════════════════════════════════════════════
 * Clone estrutural do MestreIAGeneratorUseCase: fila de modelos com fallback +
 * loop de tool-use. Aqui o executor é o NarradorToolExecutor (não o Forjador) e
 * a saída passa pelo NarradorOutputValidator (Auto-Healing da lei de ferro nº 1).
 *
 * Entrada: FichaSagaDelegate.enviarMensagem() → narrar()
 * ════════════════════════════════════════════════════════════════
 */
class MestreIANarradorUseCase(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository,
    private val context: Context? = null
) {
    data class Resultado(
        val ok: Boolean,
        val prosa: String,
        val toolsUsadas: Set<String>,
        val modelo: String?
    )

    private fun ehErroDeApi(texto: String): Boolean {
        val t = texto.trimStart()
        return Regex("^Erro \\d{3}:").containsMatchIn(t) ||
            t.startsWith("Erro de Conexão:") ||
            t.startsWith("Erro de API") ||
            t.startsWith("Erro: Resposta vazia") ||
            t.startsWith("Erro: Falha na conexão")
    }

    private val MAX_ITER = 8

    /**
     * Conduz UM turno do Narrador.
     * @param mensagemJogador o que o jogador escreveu.
     * @param executor já configurado com campanhaId/cenaId e rollBridge.
     * @param cenaResumo título/resumo da cena atual (pode ser vazio).
     * @param ultimosTurnos até 8 turnos anteriores (role "user"/"model").
     */
    suspend fun narrar(
        mensagemJogador: String,
        executor: NarradorToolExecutor,
        cenaResumo: String,
        ultimosTurnos: List<Pair<String, String>>,
        onStatus: (String) -> Unit
    ): Resultado = withContext(Dispatchers.IO) {
        val toolsUsadas = mutableSetOf<String>()

        // top-5 consultar_mundo AUTOMÁTICO sobre a mensagem do jogador (contexto canônico).
        // Lote 355 (D): usa PALAVRAS-CHAVE em vez da frase crua — a frase inteira (com aspas
        // e pontuação) virava uma query FTS AND impossível de casar; keywords melhoram o recall.
        onStatus("Consultando o mundo…")
        val consultaKeywords = extrairPalavrasChave(mensagemJogador)
        val fatosContexto = runCatching {
            val json = executor.executar(
                NarradorTools.TOOL_CONSULTAR_MUNDO,
                org.json.JSONObject().put("consulta", consultaKeywords).put("limite", 5).toString()
            )
            val o = org.json.JSONObject(json)
            val arr = o.optJSONArray("fatos")
            if (arr != null && arr.length() > 0) {
                (0 until arr.length()).joinToString("\n") { "• " + arr.getJSONObject(it).optString("fato") }
            } else ""
        }.getOrDefault("")

        val contextoCena = buildString {
            if (cenaResumo.isNotBlank()) append("CENA ATUAL: $cenaResumo\n")
            if (fatosContexto.isNotBlank()) append("FATOS CANÔNICOS RELEVANTES:\n$fatosContexto\n")
        }
        val systemBase = MestreIAPromptsNarrador.PROMPT +
            (if (contextoCena.isNotBlank()) "\n\n=== ESTADO DA CAMPANHA ===\n$contextoCena" else "")

        val contextoPersonagem = MestreIAContextFilter.gerarContexto(viewModel.personagem, "conversa")

        // Lote 355 (C): narração no FLASH (rápido) — antes era Gemini 2.5 Pro, 15-19s/turno.
        // Narração não precisa de raciocínio pesado; Flash corta a latência. Pro fica de fallback.
        val fila = listOf(
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_LITE_1_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_KEY, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_3_FLASH),
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_LITE_1_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_KEY, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_2_5_PRO),
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_KEY, com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_MODEL_V3)
        )

        for (config in fila) {
            if (config.second.isBlank()) continue
            try {
                val historico = ultimosTurnos.takeLast(8).toMutableList()
                var promptAtual = mensagemJogador
                var iteracao = 0
                var jaValidou = false

                while (iteracao < MAX_ITER) {
                    iteracao++
                    val ultima = iteracao >= MAX_ITER
                    onStatus(if (iteracao == 1) "Narrando…" else "Pensando no próximo passo…")

                    val resp = MestreIAClient.perguntarAoMestre(
                        baseUrl = config.first, apiKey = config.second, workspaceSlug = config.third,
                        prompt = promptAtual,
                        history = historico,
                        contextoPersonagem = contextoPersonagem,
                        catalogo = MestreIAClient.CatalogoNomes(),
                        modo = "saga",
                        promptSistema = systemBase,
                        onChunk = null,
                        desativarTools = ultima,
                        maxTokens = 4096
                    )
                    if (ehErroDeApi(resp.text)) break

                    val calls = resp.toolCalls.filter { it.name in NarradorTools.TODAS }
                    if (calls.isEmpty()) {
                        // Resposta final (prosa). Valida a lei de ferro nº 1.
                        val prosa = resp.text.trim()
                        if (!jaValidou) {
                            val v = NarradorOutputValidator.validar(prosa, toolsUsadas)
                            if (!v.ok) {
                                Log.w("MestreIA_Narrador", "Auto-Healing: prosa com número mecânico sem tool — re-pedindo")
                                jaValidou = true
                                historico.add("model" to prosa)
                                promptAtual = v.instrucaoCorrecao!!
                                continue
                            }
                        }
                        return@withContext Resultado(true, prosa, toolsUsadas, config.third)
                    }

                    // Executa as tools chamadas neste turno (suspend → loop, não lambda).
                    val partes = mutableListOf<String>()
                    for (tc in calls) {
                        toolsUsadas.add(tc.name)
                        onStatus(faseDe(tc.name, tc.args))
                        val res = executor.executar(tc.name, tc.args.toString())
                        partes.add("=== ${tc.name} ===\n$res")
                    }
                    historico.add("model" to "Ferramentas executadas.")
                    historico.add("user" to "[SISTEMA — resultado de ferramentas]\n" + partes.joinToString("\n\n"))
                    promptAtual = "Continue a narração usando os resultados acima. Se a ação do jogador já se resolveu, narre a consequência e termine numa escolha."
                }
                // Esgotou iterações sem prosa final limpa: devolve o que houver.
            } catch (e: Exception) {
                Log.e("MestreIA_Narrador", "Falha no modelo ${config.third}: ${e.message}")
            }
        }
        Resultado(false, "O Narrador se calou (falha de conexão com os modelos). Tente de novo.", toolsUsadas, null)
    }

    // Lote 355 (D): extrai até 8 palavras-chave da mensagem do jogador para o consultar_mundo
    // automático. Tira pontuação/aspas, descarta palavras curtas e stopwords comuns de PT-BR.
    private val STOPWORDS = setOf(
        "que", "com", "uma", "uns", "para", "pra", "por", "dos", "das", "como", "mas", "nao",
        "sim", "isso", "esse", "essa", "este", "esta", "aqui", "ali", "vou", "vamos", "ele",
        "ela", "voce", "vc", "meu", "minha", "seu", "sua", "tem", "ter", "fazer", "faco",
        "sobre", "entao", "tambem", "ate", "the", "and", "mestre", "eu", "tu", "ele"
    )

    private fun extrairPalavrasChave(texto: String): String {
        val palavras = texto
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .split(Regex("\\s+"))
            .map { it.trim().lowercase() }
            .filter { it.length >= 4 && it !in STOPWORDS }
            .distinct()
            .take(8)
        // Se sobrou nada (mensagem só de palavras curtas), cai na frase normalizada original.
        return if (palavras.isNotEmpty()) palavras.joinToString(" ") else texto.take(60)
    }

    private fun faseDe(tool: String, args: org.json.JSONObject): String = when (tool) {
        NarradorTools.TOOL_CONSULTAR_MUNDO -> "Consultando o mundo…"
        NarradorTools.TOOL_REGISTRAR_FATO -> "Registrando no cânone…"
        NarradorTools.TOOL_PEDIR_ROLAGEM -> "Pedindo rolagem (${args.optString("pericia", "")})…"
        NarradorTools.TOOL_LOCALIZAR, NarradorTools.TOOL_LER -> "Consultando o Códex…"
        NarradorTools.TOOL_INSPECIONAR_PERSONAGEM -> "Lendo a ficha…"
        NarradorTools.TOOL_INICIAR_COMBATE -> "Preparando o combate…"
        NarradorTools.TOOL_APLICAR_DANO -> "Resolvendo o dano…"
        else -> "Conduzindo a cena…"
    }
}
