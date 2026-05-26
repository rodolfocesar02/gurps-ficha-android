package com.gurps.ficha.ui.components

import android.content.Context
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.domain.MestreIAGraphEngine
import com.gurps.ficha.domain.MestreIAPlanner
import com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapter
import com.gurps.ficha.domain.tools.ForjadorToolExecutor
import com.gurps.ficha.domain.tools.ForjadorTools
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class GeminiLiveTools(private val viewModel: FichaViewModel, private val context: Context? = null) {

    private val repo = viewModel.dataRepository
    private val graphEngine = MestreIAGraphEngine(repo)
    private val nexusAdapter = NexusArcanoModoAlvoAdapter(repo.magias)
    private val forjador = ForjadorToolExecutor(viewModel, repo, nexusAdapter, context)

    fun executar(nome: String, args: JSONObject): JSONObject {
        return try {
            when (nome) {
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

                // ── Ferramenta RAG (única que não vai para o Forjador) ──
                "consultarManual" -> consultarManual(args)

                // Compatibilidade: ferramentas antigas mapeadas para editarFicha
                "adicionarVantagem"    -> editarCompat("adicionar", "vantagens", args.getString("nome"), "nivel=${args.optInt("nivel", 1)}")
                "removerVantagem"      -> editarCompat("remover", "vantagens", args.getString("nome"), "")
                "adicionarDesvantagem" -> editarCompat("adicionar", "desvantagens", args.getString("nome"), "nivel=${args.optInt("nivel", 1)}")
                "removerDesvantagem"   -> editarCompat("remover", "desvantagens", args.getString("nome"), "")
                "adicionarPericia"     -> editarCompat("adicionar", "pericias", args.getString("nome"), "nivel=${args.optInt("pontos", 1)}")
                "removerPericia"       -> editarCompat("remover", "pericias", args.getString("nome"), "")

                else -> JSONObject().apply { put("erro", "Ferramenta desconhecida: $nome") }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeminiLiveTools", "Erro em $nome: ${e.message}")
            JSONObject().apply { put("erro", e.message ?: "Erro desconhecido") }
        }
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

    private fun consultarManual(args: JSONObject): JSONObject {
        val termos = args.getString("termos")
        val livro = args.optString("livro", "").takeIf { it.isNotBlank() }
        android.util.Log.i("GeminiLiveTools", "consultarManual: '$termos'${if (livro != null) " livro='$livro'" else ""}")

        return try {
            val resultado = runBlocking {
                val plano = MestreIAPlanner.planejarBusca(termos, viewModel.personagem.equipamentos)
                val termosExtras = plano.termos
                val searchResult = graphEngine.buscarDiretoNoCodex(termos, termosExtras, filtroLivro = livro)

                val resultadoFinal = if (plano.subQueriesTemáticas.isNotEmpty()) {
                    val subResultados = plano.subQueriesTemáticas.map { q ->
                        graphEngine.buscarDiretoNoCodex(q, emptyList(), filtroLivro = livro)
                    }
                    val scoresUnidos = searchResult.chunkScores.toMutableMap()
                    subResultados.forEach { sub -> scoresUnidos.putAll(sub.chunkScores) }
                    val todosChunks = (searchResult.relatedChunks + subResultados.flatMap { it.relatedChunks })
                        .distinctBy { it.chunk_id }
                        .take(40)
                    MestreIAGraphEngine.GraphSearchResult(
                        relatedChunks = todosChunks,
                        chunkScores = scoresUnidos
                    )
                } else {
                    searchResult
                }

                graphEngine.formatarParaIA(resultadoFinal, termos)
            }

            val chunksEncontrados = resultado.lines().count { it.startsWith("[Pág.") }
            android.util.Log.i("GeminiLiveTools", "consultarManual OK: $chunksEncontrados chunks retornados")

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
            android.util.Log.e("GeminiLiveTools", "Erro no RAG: ${e.message}")
            JSONObject().apply { put("erro", "Falha na busca: ${e.message}") }
        }
    }
}
