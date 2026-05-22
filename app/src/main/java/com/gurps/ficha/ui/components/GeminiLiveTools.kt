package com.gurps.ficha.ui.components

import com.gurps.ficha.domain.MestreIAGraphEngine
import com.gurps.ficha.domain.MestreIAPlanner
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class GeminiLiveTools(private val viewModel: FichaViewModel) {

    private val graphEngine = MestreIAGraphEngine(viewModel.dataRepository)

    fun executar(nome: String, args: JSONObject): JSONObject {
        return try {
            when (nome) {
                "obterFicha" -> obterFicha()
                "obterPontosRestantes" -> obterPontosRestantes()
                "adicionarVantagem" -> adicionarVantagem(args)
                "removerVantagem" -> removerVantagem(args)
                "adicionarDesvantagem" -> adicionarDesvantagem(args)
                "adicionarPericia" -> adicionarPericia(args)
                "removerPericia" -> removerPericia(args)
                "consultarManual" -> consultarManual(args)
                "inspecionarPersonagem" -> inspecionarPersonagem(args)
                else -> JSONObject().apply { put("erro", "Ferramenta desconhecida: $nome") }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeminiLiveTools", "Erro em $nome: ${e.message}")
            JSONObject().apply { put("erro", e.message ?: "Erro desconhecido") }
        }
    }

    private fun obterFicha(): JSONObject {
        val p = viewModel.personagem
        return JSONObject().apply {
            put("nome", p.nome.ifBlank { "Sem nome" })
            put("st", p.st); put("dx", p.dx); put("iq", p.iq); put("ht", p.ht)
            put("pontosIniciais", p.pontosIniciais)
            put("pontosGastos", p.pontosGastos)
            put("pontosRestantes", p.pontosRestantes)
            put("vantagens", p.vantagens.joinToString(", ") { it.nome })
            put("desvantagens", p.desvantagens.joinToString(", ") { it.nome })
            put("pericias", p.pericias.joinToString(", ") { "${it.nome}(${it.pontosGastos}pts)" })
        }
    }

    private fun obterPontosRestantes(): JSONObject {
        val p = viewModel.personagem
        return JSONObject().apply {
            put("pontosRestantes", p.pontosRestantes)
            put("pontosGastos", p.pontosGastos)
            put("pontosIniciais", p.pontosIniciais)
        }
    }

    private fun inspecionarPersonagem(args: JSONObject): JSONObject {
        val secao = args.optString("secao", "tudo")
        val p = viewModel.personagem
        return JSONObject().apply {
            when (secao) {
                "atributos" -> {
                    put("st", p.st); put("dx", p.dx); put("iq", p.iq); put("ht", p.ht)
                    put("pv", p.pontosVida); put("pf", p.pontosFadiga)
                    put("velocidade", p.velocidadeBasica.toDouble())
                    put("deslocamento", p.deslocamentoBasico)
                    put("percepcao", p.percepcao); put("vontade", p.vontade)
                }
                "vantagens" -> put("vantagens", p.vantagens.joinToString(", ") { "${it.nome} [${it.custoFinal}pts]" })
                "desvantagens" -> put("desvantagens", p.desvantagens.joinToString(", ") { "${it.nome} [${it.custoFinal}pts]" })
                "pericias" -> put("pericias", p.pericias.joinToString(", ") { "${it.nome} NH${it.calcularNivel(p)}(${it.pontosGastos}pts)" })
                "equipamentos" -> put("equipamentos", p.equipamentos.joinToString(", ") { it.nome })
                else -> {
                    put("nome", p.nome)
                    put("st", p.st); put("dx", p.dx); put("iq", p.iq); put("ht", p.ht)
                    put("pontosRestantes", p.pontosRestantes)
                    put("vantagens", p.vantagens.joinToString(", ") { it.nome })
                    put("desvantagens", p.desvantagens.joinToString(", ") { it.nome })
                    put("pericias", p.pericias.take(10).joinToString(", ") { it.nome })
                }
            }
        }
    }

    private fun adicionarVantagem(args: JSONObject): JSONObject {
        val nome = args.getString("nome")
        val nivel = args.optInt("nivel", 1)
        val def = viewModel.dataRepository.vantagens.firstOrNull {
            it.nome.equals(nome, ignoreCase = true) || it.nome.contains(nome, ignoreCase = true)
        }
        return if (def == null) {
            JSONObject().apply { put("sucesso", false); put("mensagem", "Vantagem '$nome' não encontrada no catálogo") }
        } else {
            val erro = viewModel.adicionarVantagem(def, nivel)
            JSONObject().apply {
                put("sucesso", erro == null)
                put("mensagem", erro ?: "Vantagem '${def.nome}' adicionada com sucesso")
                put("pontosRestantes", viewModel.personagem.pontosRestantes)
            }
        }
    }

    private fun removerVantagem(args: JSONObject): JSONObject {
        val nome = args.getString("nome")
        val index = viewModel.personagem.vantagens.indexOfFirst {
            it.nome.equals(nome, ignoreCase = true) || it.nome.contains(nome, ignoreCase = true)
        }
        return if (index < 0) {
            JSONObject().apply { put("sucesso", false); put("mensagem", "Vantagem '$nome' não encontrada na ficha") }
        } else {
            viewModel.removerVantagem(index)
            JSONObject().apply {
                put("sucesso", true)
                put("mensagem", "Vantagem '$nome' removida")
                put("pontosRestantes", viewModel.personagem.pontosRestantes)
            }
        }
    }

    private fun adicionarDesvantagem(args: JSONObject): JSONObject {
        val nome = args.getString("nome")
        val nivel = args.optInt("nivel", 1)
        val def = viewModel.dataRepository.desvantagens.firstOrNull {
            it.nome.equals(nome, ignoreCase = true) || it.nome.contains(nome, ignoreCase = true)
        }
        return if (def == null) {
            JSONObject().apply { put("sucesso", false); put("mensagem", "Desvantagem '$nome' não encontrada no catálogo") }
        } else {
            val erro = viewModel.adicionarDesvantagem(def, nivel)
            JSONObject().apply {
                put("sucesso", erro == null)
                put("mensagem", erro ?: "Desvantagem '${def.nome}' adicionada")
                put("pontosRestantes", viewModel.personagem.pontosRestantes)
            }
        }
    }

    private fun adicionarPericia(args: JSONObject): JSONObject {
        val nome = args.getString("nome")
        val pontos = args.optInt("pontos", 1)
        val def = viewModel.dataRepository.pericias.firstOrNull {
            it.nome.equals(nome, ignoreCase = true) || it.nome.contains(nome, ignoreCase = true)
        }
        return if (def == null) {
            JSONObject().apply { put("sucesso", false); put("mensagem", "Perícia '$nome' não encontrada no catálogo") }
        } else {
            val erro = viewModel.adicionarPericia(def, pontos)
            JSONObject().apply {
                put("sucesso", erro == null)
                put("mensagem", erro ?: "Perícia '${def.nome}' adicionada com $pontos ponto(s)")
                put("pontosRestantes", viewModel.personagem.pontosRestantes)
            }
        }
    }

    private fun removerPericia(args: JSONObject): JSONObject {
        val nome = args.getString("nome")
        val index = viewModel.personagem.pericias.indexOfFirst {
            it.nome.equals(nome, ignoreCase = true) || it.nome.contains(nome, ignoreCase = true)
        }
        return if (index < 0) {
            JSONObject().apply { put("sucesso", false); put("mensagem", "Perícia '$nome' não encontrada na ficha") }
        } else {
            viewModel.removerPericia(index)
            JSONObject().apply {
                put("sucesso", true)
                put("mensagem", "Perícia '$nome' removida")
                put("pontosRestantes", viewModel.personagem.pontosRestantes)
            }
        }
    }

    // Pipeline RAG real: Planner → FTS → Scoring → Formatação — mesmo motor do Auditor texto
    private fun consultarManual(args: JSONObject): JSONObject {
        val termos = args.getString("termos")
        android.util.Log.i("GeminiLiveTools", "consultarManual: '$termos'")

        return try {
            val resultado = runBlocking {
                // Usa o Planner para extrair termos técnicos de GURPS da query em linguagem natural
                val plano = MestreIAPlanner.planejarBusca(termos, viewModel.personagem.equipamentos)
                val termosExtras = plano.termos

                // Busca FTS + scoring no Códex (mesmo motor do MestreIAUseCase)
                val searchResult = graphEngine.buscarDiretoNoCodex(termos, termosExtras)

                // Sub-queries temáticas em paralelo para ampliar cobertura
                val resultadoFinal = if (plano.subQueriesTemáticas.isNotEmpty()) {
                    val subResultados = plano.subQueriesTemáticas.map { q ->
                        graphEngine.buscarDiretoNoCodex(q, emptyList())
                    }
                    val todosChunks = (searchResult.relatedChunks + subResultados.flatMap { it.relatedChunks })
                        .distinctBy { it.chunk_id }
                        .take(20)
                    MestreIAGraphEngine.GraphSearchResult(
                        relatedChunks = todosChunks,
                        chunkScores = searchResult.chunkScores
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
                    put("mensagem", "Nenhuma regra encontrada no Códex para '$termos'. Tente termos mais específicos como nomes técnicos de GURPS.")
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
