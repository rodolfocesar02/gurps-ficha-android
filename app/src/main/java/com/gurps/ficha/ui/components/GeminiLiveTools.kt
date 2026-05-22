package com.gurps.ficha.ui.components

import com.gurps.ficha.viewmodel.FichaViewModel
import org.json.JSONObject

class GeminiLiveTools(private val viewModel: FichaViewModel) {

    fun executar(nome: String, args: JSONObject): JSONObject {
        return try {
            when (nome) {
                "obterFicha" -> obterFicha()
                "obterPontosRestantes" -> obterPontosRestantes()
                "adicionarVantagem" -> adicionarVantagem(args)
                "removerVantagem" -> removerVantagem(args)
                "adicionarDesvantagem" -> adicionarDesvantagem(args)
                "adicionarPericia" -> adicionarPericia(args)
                "consultarManual" -> consultarManual(args)
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
            put("pontosIniciais", p.pontosIniciais)
            put("pontosGastos", p.pontosGastos)
            put("pontosRestantes", p.pontosRestantes)
            put("vantagens", p.vantagens.joinToString(", ") { it.nome })
            put("desvantagens", p.desvantagens.joinToString(", ") { it.nome })
            put("pericias", p.pericias.joinToString(", ") { it.nome })
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

    private fun adicionarVantagem(args: JSONObject): JSONObject {
        val nome = args.getString("nome")
        val nivel = args.optInt("nivel", 1)
        // Busca no catálogo carregado no ViewModel
        val def = viewModel.dataRepository.vantagens.firstOrNull {
            it.nome.equals(nome, ignoreCase = true) ||
            it.nome.contains(nome, ignoreCase = true)
        }
        return if (def == null) {
            JSONObject().apply {
                put("sucesso", false)
                put("mensagem", "Vantagem '$nome' não encontrada no catálogo")
            }
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
            it.nome.equals(nome, ignoreCase = true) ||
            it.nome.contains(nome, ignoreCase = true)
        }
        return if (index < 0) {
            JSONObject().apply {
                put("sucesso", false)
                put("mensagem", "Vantagem '$nome' não encontrada na ficha")
            }
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
            it.nome.equals(nome, ignoreCase = true) ||
            it.nome.contains(nome, ignoreCase = true)
        }
        return if (def == null) {
            JSONObject().apply {
                put("sucesso", false)
                put("mensagem", "Desvantagem '$nome' não encontrada no catálogo")
            }
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
            it.nome.equals(nome, ignoreCase = true) ||
            it.nome.contains(nome, ignoreCase = true)
        }
        return if (def == null) {
            JSONObject().apply {
                put("sucesso", false)
                put("mensagem", "Perícia '$nome' não encontrada no catálogo")
            }
        } else {
            val erro = viewModel.adicionarPericia(def, pontos)
            JSONObject().apply {
                put("sucesso", erro == null)
                put("mensagem", erro ?: "Perícia '${def.nome}' adicionada com $pontos ponto(s)")
                put("pontosRestantes", viewModel.personagem.pontosRestantes)
            }
        }
    }

    private fun consultarManual(args: JSONObject): JSONObject {
        val termos = args.getString("termos")
        // Delega para o sistema RAG existente via ViewModel
        // Retorna uma resposta síncrona simplificada — o Mestre usará isso como contexto
        return JSONObject().apply {
            put("termos", termos)
            put("instrucao", "Use conversarComMestreIA para busca RAG completa. Termos: $termos")
        }
    }
}
