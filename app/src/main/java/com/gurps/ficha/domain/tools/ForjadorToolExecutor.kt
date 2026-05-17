package com.gurps.ficha.domain.tools

import android.util.Log
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapter
import com.gurps.ficha.domain.engine.MagicEngine
import com.gurps.ficha.viewmodel.FichaViewModel
import org.json.JSONObject

class ForjadorToolExecutor(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository,
    private val nexusAdapter: NexusArcanoModoAlvoAdapter
) {
    fun execute(toolCall: MestreIAClient.MestreIAToolCall): String {
        Log.d("Forjador_Tools", "Executando tool: ${toolCall.name} | args: ${toolCall.args}")
        return when (toolCall.name) {
            ForjadorTools.TOOL_LER_FICHA    -> lerFicha(toolCall.args)
            ForjadorTools.TOOL_BUSCAR       -> buscarCatalogo(toolCall.args)
            ForjadorTools.TOOL_GPS_MAGIA    -> gpsMagia(toolCall.args)
            else -> """{"erro": "ferramenta desconhecida: ${toolCall.name}"}"""
        }
    }

    private fun lerFicha(args: JSONObject): String {
        val p = viewModel.personagem
        val secao = args.optString("secao", "atributos")
        return when (secao) {
            "atributos" -> buildString {
                appendLine("ST: ${p.st} | DX: ${p.dx} | IQ: ${p.iq} | HT: ${p.ht}")
                appendLine("PV: ${p.pontosVida} | PF: ${p.pontosFadiga}")
                val am = viewModel.nivelAptidaoMagica
                appendLine("Aptidão Mágica: $am")
            }
            "vantagens" -> if (p.vantagens.isEmpty()) "Nenhuma vantagem." else
                p.vantagens.joinToString("\n") { "• ${it.definicaoId} | ${it.nome} | ${it.custoFinal} pts" }
            "desvantagens" -> if (p.desvantagens.isEmpty()) "Nenhuma desvantagem." else
                p.desvantagens.joinToString("\n") { "• ${it.definicaoId} | ${it.nome} | ${it.custoFinal} pts" }
            "pericias" -> if (p.pericias.isEmpty()) "Nenhuma perícia." else
                p.pericias.joinToString("\n") { "• ${it.definicaoId} | ${it.nome} | NH ${it.calcularNivel(p)} | ${it.pontosGastos} pts" }
            "magias" -> if (p.magias.isEmpty()) "Nenhuma magia." else
                p.magias.joinToString("\n") { "• ${it.definicaoId} | ${it.nome} | escola:${it.escola?.joinToString() ?: "?"}" }
            "equipamentos" -> if (p.equipamentos.isEmpty()) "Nenhum equipamento." else
                p.equipamentos.joinToString("\n") { e ->
                    buildString {
                        append("• ${e.nome}")
                        e.armaDanoRaw?.let { append(" | dano:$it") }
                        e.armaduraRd?.let { append(" | RD:$it") }
                        append(" | ${e.peso}kg | ${e.custo}$")
                    }
                }
            "pontos" -> {
                val gastos = calcularPontosGastos()
                val max = p.pontosIniciais
                "Pontos gastos: $gastos / $max pts disponíveis. Livres: ${max - gastos} pts."
            }
            else -> """{"erro": "seção inválida: $secao. Use: atributos, vantagens, desvantagens, pericias, magias, equipamentos, pontos"}"""
        }
    }

    private fun buscarCatalogo(args: JSONObject): String {
        val tipo = args.optString("tipo", "vantagem")
        val query = args.optString("query", "").lowercase().trim()
        if (query.isBlank()) return """{"erro": "query não pode ser vazia"}"""

        return when (tipo) {
            "vantagem" -> {
                val resultados = repository.vantagens.filter {
                    it.nome.lowercase().contains(query) || it.id.lowercase().contains(query)
                }.take(10)
                if (resultados.isEmpty()) "Nenhuma vantagem encontrada para '$query'."
                else resultados.joinToString("\n") { "• ${it.id} | ${it.nome} | ${it.getCustoBase()} pts" }
            }
            "desvantagem" -> {
                val resultados = repository.desvantagens.filter {
                    it.nome.lowercase().contains(query) || it.id.lowercase().contains(query)
                }.take(10)
                if (resultados.isEmpty()) "Nenhuma desvantagem encontrada para '$query'."
                else resultados.joinToString("\n") { "• ${it.id} | ${it.nome} | ${it.getCustoBase()} pts" }
            }
            "pericia" -> {
                val resultados = (repository.pericias + repository.periciasSuplementares.map {
                    com.gurps.ficha.model.PericiaDefinicao(id = it.id, nome = it.nome)
                }).filter {
                    it.nome.lowercase().contains(query) || it.id.lowercase().contains(query)
                }.take(10)
                if (resultados.isEmpty()) "Nenhuma perícia encontrada para '$query'."
                else resultados.joinToString("\n") { "• ${it.id} | ${it.nome}" }
            }
            "magia" -> {
                val resultados = repository.magias.filter {
                    it.nome.lowercase().contains(query) || it.id.lowercase().contains(query) ||
                    it.escola?.any { e -> e.lowercase().contains(query) } == true
                }.take(10)
                if (resultados.isEmpty()) "Nenhuma magia encontrada para '$query'."
                else resultados.joinToString("\n") { m ->
                    "• ${m.id} | ${m.nome} | escola:${m.escola?.joinToString() ?: "?"} | pré:${m.preRequisitos?.take(60) ?: "—"}"
                }
            }
            else -> """{"erro": "tipo inválido: $tipo. Use: vantagem, desvantagem, pericia, magia"}"""
        }
    }

    private fun gpsMagia(args: JSONObject): String {
        val alvoId = args.optString("magia_alvo", "").trim()
        if (alvoId.isBlank()) return "Erro: magia_alvo é obrigatório."

        val p = viewModel.personagem
        val magiasConhecidas = p.magias.map { it.definicaoId }.toSet()
        val am = viewModel.nivelAptidaoMagica

        val snapshot = nexusAdapter.calcular(alvoId, magiasConhecidas, p.iq, p.dx, am)
        val alvoNome = repository.magias.find { it.id == alvoId }?.nome ?: alvoId

        return buildString {
            appendLine("=== GPS de Magias: $alvoNome ($alvoId) ===")
            snapshot.progressoCadeia?.let { appendLine("Cadeia: $it") }
            if (snapshot.progressoEscolas.isNotEmpty()) appendLine("Escolas: ${snapshot.progressoEscolas.joinToString()}")
            appendLine("Próximas ações: ${snapshot.proximasAcoesIds.mapNotNull { id ->
                repository.magias.find { it.id == id }?.nome?.let { "$id ($it)" }
            }.joinToString().ifBlank { "—" }}")
            snapshot.proximaObrigatoriaId?.let { id ->
                val nome = repository.magias.find { it.id == id }?.nome ?: id
                appendLine("PRÓXIMA OBRIGATÓRIA: $id ($nome)")
            }
            snapshot.bloqueioCurto?.let { appendLine("Bloqueio: $it") }
            snapshot.aviso?.let { appendLine("Aviso: $it") }
            appendLine("Magias já conhecidas: ${magiasConhecidas.size}")
        }.trim()
    }

    private fun calcularPontosGastos(): Int {
        val p = viewModel.personagem
        val atributos = ((p.st - 10).coerceAtLeast(0) * 10) +
                        ((p.dx - 10).coerceAtLeast(0) * 20) +
                        ((p.iq - 10).coerceAtLeast(0) * 20) +
                        ((p.ht - 10).coerceAtLeast(0) * 10)
        val vantagens    = p.vantagens.sumOf { it.custoFinal }
        val desvantagens = p.desvantagens.sumOf { it.custoFinal }
        val pericias     = p.pericias.sumOf { it.pontosGastos }
        val magias       = p.magias.sumOf { it.pontosGastos }
        return atributos + vantagens + desvantagens + pericias + magias
    }
}
