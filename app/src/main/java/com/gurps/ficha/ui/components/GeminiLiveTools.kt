package com.gurps.ficha.ui.components

import android.content.Context
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapter
import com.gurps.ficha.domain.tools.ForjadorToolExecutor
import com.gurps.ficha.domain.tools.ForjadorTools
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class GeminiLiveTools(private val viewModel: FichaViewModel, private val context: Context? = null) {

    companion object {
        // Lote 352: limite ÚNICO de payload de toolResponse da Voz. Respostas grandes
        // causam code=1007 (message too big) no WebSocket do Gemini Live. Valor herdado
        // do truncamento que vivia dentro de consultarManual (18k chars ≈ 18 KB).
        const val LIVE_MAX_TOOL_PAYLOAD = 18_000
    }

    private val repo = viewModel.dataRepository
    private val nexusAdapter = NexusArcanoModoAlvoAdapter(repo.magias)
    private val forjador = ForjadorToolExecutor(viewModel, repo, nexusAdapter, context)

    fun executar(nome: String, args: JSONObject): JSONObject {
        return try {
            limitarPayload(nome, when (nome) {
                // ── Ferramentas legadas (delegam ao ForjadorToolExecutor) ──
                "obterFicha"           -> lerFicha("tudo")
                "obterPontosRestantes" -> lerFicha("pontos")
                "inspecionarPersonagem" -> lerFicha(args.optString("secao", "tudo"))

                // ── Leitura de seções (unificada) ──
                "lerFicha"             -> lerFicha(args.optString("secao", "atributos"))

                // ── Busca no catálogo (previne alucinação de IDs) ──
                "buscarCatalogo" -> executarForjador("buscar", args)

                // ── Edição unificada via ForjadorToolExecutor ──
                "editarFicha" -> executarForjador("editar", args)

                // ── GPS de Magias ──
                "trilhaDeMagias" -> executarForjador("gps", args)

                // ── Raças e Metacaracterísticas ──
                ForjadorTools.TOOL_BUSCAR_RACAS   -> executarForjador("buscar_racas", args)
                ForjadorTools.TOOL_APLICAR_RACIAL -> executarForjador("aplicar_racial", args)

                // ── Ferramenta RAG unificada (Lote 324: fusão das 5 anteriores) ──
                // Aceita args.livros: array<string> com 1+ livros. Sem array = busca todos.
                "consultarManual"               -> consultarManual(args)

                // Compatibilidade: ferramentas antigas mapeadas para editarFicha
                "adicionarVantagem"    -> editarCompat("adicionar", "vantagens", args.getString("nome"), "nivel=${args.optInt("nivel", 1)}")
                "removerVantagem"      -> editarCompat("remover", "vantagens", args.getString("nome"), "")
                "adicionarDesvantagem" -> editarCompat("adicionar", "desvantagens", args.getString("nome"), "nivel=${args.optInt("nivel", 1)}")
                "removerDesvantagem"   -> editarCompat("remover", "desvantagens", args.getString("nome"), "")
                "adicionarPericia"     -> editarCompat("adicionar", "pericias", args.getString("nome"), "nivel=${args.optInt("pontos", 1)}")
                "removerPericia"       -> editarCompat("remover", "pericias", args.getString("nome"), "")

                else -> JSONObject().apply { put("erro", "Ferramenta desconhecida: $nome") }
            })
        } catch (e: Exception) {
            android.util.Log.e("GeminiLiveTools", "Erro em $nome: ${e.message}")
            JSONObject().apply { put("erro", e.message ?: "Erro desconhecido") }
        }
    }

    /**
     * Lote 352: truncamento CENTRALIZADO de payload (antes vivia só dentro de
     * consultarManual). Se o JSON serializado passa de LIVE_MAX_TOOL_PAYLOAD,
     * corta o maior campo string até caber — qualquer tool da Voz fica protegida
     * do code=1007 do servidor Live.
     */
    private fun limitarPayload(nome: String, json: JSONObject): JSONObject {
        val total = json.toString().length
        if (total <= LIVE_MAX_TOOL_PAYLOAD) return json
        val marcador = "\n[... truncado por limite de payload]"
        val chaveMaior = json.keys().asSequence()
            .filter { json.opt(it) is String }
            .maxByOrNull { (json.opt(it) as String).length }
        if (chaveMaior != null) {
            val valor = json.getString(chaveMaior)
            val excesso = total - LIVE_MAX_TOOL_PAYLOAD
            val novoTamanho = (valor.length - excesso - marcador.length).coerceAtLeast(0)
            json.put(chaveMaior, valor.take(novoTamanho) + marcador)
            android.util.Log.w(
                "GeminiLiveTools",
                "payload de '$nome' truncado: $total -> ${json.toString().length} chars (campo '$chaveMaior')"
            )
        } else {
            android.util.Log.w("GeminiLiveTools", "payload de '$nome' excede $LIVE_MAX_TOOL_PAYLOAD chars sem campo string truncável ($total)")
        }
        return json
    }

    private fun lerFicha(secao: String): JSONObject {
        val secaoReal = when (secao) {
            "tudo", "" -> "atributos"
            else -> secao
        }
        val resultado = forjador.lerSecao(secaoReal)
        val p = viewModel.personagem
        return if (secao == "tudo") {
            // Para obterFicha: retorna visão geral como JSON
            JSONObject().apply {
                put("nome", p.nome.ifBlank { "Sem nome" })
                put("st", p.st); put("dx", p.dx); put("iq", p.iq); put("ht", p.ht)
                put("pontosIniciais", p.pontosIniciais)
                put("pontosGastos", p.pontosGastos)
                put("pontosRestantes", p.pontosRestantes)
                put("vantagens", p.vantagens.joinToString(", ") { it.nome })
                put("desvantagens", p.desvantagens.joinToString(", ") { it.nome })
                put("pericias", p.pericias.joinToString(", ") { "${it.nome}(${it.pontosGastos}pts)" })
                put("magias", p.magias.joinToString(", ") { it.nome })
                put("tecnicas", p.tecnicas.joinToString(", ") { it.nome })
                put("equipamentos", p.equipamentos.joinToString(", ") { it.nome })
            }
        } else if (secao == "pontos") {
            JSONObject().apply {
                put("pontosRestantes", p.pontosRestantes)
                put("pontosGastos", p.pontosGastos)
                put("pontosIniciais", p.pontosIniciais)
            }
        } else {
            JSONObject().apply { put("resultado", resultado) }
        }
    }

    private fun executarForjador(tipo: String, args: JSONObject): JSONObject {
        val toolName = when (tipo) {
            "buscar"         -> ForjadorTools.TOOL_BUSCAR
            "editar"         -> ForjadorTools.TOOL_EDITAR
            "gps"            -> ForjadorTools.TOOL_GPS_MAGIA
            "buscar_racas"   -> ForjadorTools.TOOL_BUSCAR_RACAS
            "aplicar_racial" -> ForjadorTools.TOOL_APLICAR_RACIAL
            else             -> ForjadorTools.TOOL_EDITAR
        }
        val toolCall = MestreIAClient.MestreIAToolCall(name = toolName, args = args)
        val resultado = forjador.execute(toolCall)
        return try {
            JSONObject(resultado)
        } catch (_: Exception) {
            JSONObject().apply { put("resultado", resultado) }
        }
    }

    private fun editarCompat(op: String, secao: String, alvo: String, valor: String): JSONObject {
        val args = JSONObject().apply {
            put("operacao", op)
            put("secao", secao)
            put("alvo", alvo)
            if (valor.isNotBlank()) put("valor", valor)
        }
        return executarForjador("editar", args)
    }

    /**
     * Lote 352: a Voz passou a usar o MESMO motor de busca do Auditor (Lotes 325-327):
     * localizar_no_codex (FTS4 AND/OR + ranking BM25) + lerPaginas (texto completo).
     * O motor semântico (GraphEngine/HNSW/embeddings) ficou SEM CALLERS — dormente.
     * Em UMA chamada: localiza, lê as melhores páginas do ranking e devolve compacto
     * (o fluxo de voz não comporta o loop localizar→ler de múltiplas rodadas do Auditor).
     *
     * Lote 324 (mantido): aceita args.livros (array) ou args.livro (string, legado).
     */
    private fun consultarManual(args: JSONObject): JSONObject {
        val termos = args.getString("termos")
        val livros: List<String> = run {
            val arr = args.optJSONArray("livros")
            if (arr != null && arr.length() > 0) {
                (0 until arr.length()).mapNotNull { arr.optString(it, "").takeIf { s -> s.isNotBlank() } }
            } else {
                val livroUnico = args.optString("livro", "").takeIf { it.isNotBlank() }
                if (livroUnico != null) listOf(livroUnico) else emptyList()
            }
        }
        android.util.Log.i("GeminiLiveTools", "consultarManual: '$termos'${if (livros.isNotEmpty()) " livros=$livros" else ""}")

        return try {
            val resultado = runBlocking {
                val loc = repo.localizarNoCodex(termos, livros.takeIf { it.isNotEmpty() })
                if (loc.hits.isEmpty()) return@runBlocking ""

                val sb = StringBuilder()
                sb.append("PÁGINAS ENCONTRADAS para \"$termos\" (${loc.total} no total")
                if (loc.modo == "OR") sb.append(", busca aproximada")
                sb.append("):\n")
                loc.hits.take(8).forEach { h ->
                    sb.append("• [${h.livro}, pág. ${h.pagina}] ${h.trecho}\n")
                }

                // Lê o texto COMPLETO das 3 melhores páginas do ranking BM25 (citáveis).
                val melhores = loc.hits.distinctBy { it.livro to it.pagina }.take(3)
                sb.append("\nCONTEÚDO DAS MELHORES PÁGINAS:\n")
                melhores.forEach { h ->
                    val chunks = repo.lerPaginas(h.livro, h.pagina)
                    chunks.forEach { c ->
                        sb.append("--- [${c.source_title}, pág. ${c.page_number}] ---\n${c.text}\n\n")
                    }
                }
                sb.toString()
            }

            android.util.Log.i("GeminiLiveTools", "consultarManual OK (${resultado.length} chars)")

            if (resultado.isBlank()) {
                JSONObject().apply {
                    put("encontrado", false)
                    put("mensagem", "Nenhuma regra encontrada no Códex para '$termos'. Tente termos mais específicos.")
                }
            } else {
                JSONObject().apply {
                    put("encontrado", true)
                    put("regras", resultado)
                    put("instrucao", "Use SOMENTE as regras acima para responder. Cite [Livro, Pág]. Se envolver cálculo: cite a regra, identifique os valores, calcule passo a passo, conclua.")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeminiLiveTools", "Erro na busca do Códex: ${e.message}")
            JSONObject().apply { put("erro", "Falha na busca: ${e.message}") }
        }
    }
}
