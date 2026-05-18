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
            ForjadorTools.TOOL_EDITAR       -> editarFicha(toolCall.args)
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
                else resultados.joinToString("\n") { v ->
                    val mods = v.modificadoresEspecificos.take(8)
                        .joinToString(", ") { "${it.id}(${it.nome})" }
                    buildString {
                        append("• ${v.id} | ${v.nome} | ${v.getCustoBase()} pts | tipoCusto:${v.tipoCusto}")
                        if (mods.isNotBlank()) append(" | modificadores: $mods")
                    }
                }
            }
            "desvantagem" -> {
                val resultados = repository.desvantagens.filter {
                    it.nome.lowercase().contains(query) || it.id.lowercase().contains(query)
                }.take(10)
                if (resultados.isEmpty()) "Nenhuma desvantagem encontrada para '$query'."
                else resultados.joinToString("\n") { d ->
                    val mods = d.modificadoresEspecificos.take(8)
                        .joinToString(", ") { "${it.id}(${it.nome})" }
                    buildString {
                        append("• ${d.id} | ${d.nome} | ${d.getCustoBase()} pts | tipoCusto:${d.tipoCusto}")
                        if (mods.isNotBlank()) append(" | modificadores: $mods")
                    }
                }
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
            "tecnica" -> {
                val resultados = repository.tecnicasCatalogo.filter {
                    it.nome.lowercase().contains(query) || it.id.lowercase().contains(query)
                }.take(10)
                if (resultados.isEmpty()) "Nenhuma técnica encontrada para '$query'."
                else resultados.joinToString("\n") { t ->
                    "• ${t.id} | ${t.nome} | dif:${t.dificuldadeRaw} | predef:${t.preDefinidoRaw} | pré:${t.preRequisitoRaw.take(60)}"
                }
            }
            else -> """{"erro": "tipo inválido: $tipo. Use: vantagem, desvantagem, pericia, magia, tecnica"}"""
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

    /**
     * Edita a ficha DIRETAMENTE (aplica na hora, sem botão).
     * remover → tira a ÚLTIMA ocorrência do alvo (seguro p/ dedup).
     * adicionar/alterar → lookup por ID/nome no catálogo.
     */
    private fun editarFicha(args: JSONObject): String {
        val op    = args.optString("operacao").lowercase().trim()
        val secao = args.optString("secao").lowercase().trim()
        val alvo  = args.optString("alvo").trim()
        val valor = args.optString("valor", "").trim()
        if (op.isBlank() || secao.isBlank() || alvo.isBlank())
            return """{"erro":"operacao, secao e alvo são obrigatórios"}"""

        fun norm(s: String) = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "").lowercase()
            .replace(Regex("\\(.*?\\)"), "").replace(Regex("[^a-z0-9]"), "").trim()
        val alvoN = norm(alvo)
        val p = viewModel.personagem

        // Acha o índice da ÚLTIMA ocorrência cujo id OU nome casa com o alvo
        fun <T> ultimoIndice(lista: List<T>, id: (T) -> String, nome: (T) -> String): Int =
            lista.indexOfLast { norm(id(it)) == alvoN || norm(nome(it)) == alvoN }

        if (op == "remover") {
            val idx = when (secao) {
                "vantagens"      -> ultimoIndice(p.vantagens, { it.definicaoId }, { it.nome })
                    .also { if (it >= 0) viewModel.removerVantagem(it) }
                "desvantagens"   -> ultimoIndice(p.desvantagens, { it.definicaoId }, { it.nome })
                    .also { if (it >= 0) viewModel.removerDesvantagem(it) }
                "pericias"       -> ultimoIndice(p.pericias, { it.definicaoId }, { it.nome })
                    .also { if (it >= 0) viewModel.removerPericia(it) }
                "tecnicas"       -> ultimoIndice(p.tecnicas, { it.definicaoId }, { it.nome })
                    .also { if (it >= 0) viewModel.removerTecnica(it) }
                "magias"         -> ultimoIndice(p.magias, { it.definicaoId }, { it.nome })
                    .also { if (it >= 0) viewModel.removerMagia(it) }
                "equipamentos"   -> ultimoIndice(p.equipamentos, { "" }, { it.nome })
                    .also { if (it >= 0) viewModel.removerEquipamento(it) }
                "qualidades"     -> ultimoIndice(p.qualidades, { "" }, { it })
                    .also { if (it >= 0) viewModel.removerQualidade(it) }
                "peculiaridades" -> ultimoIndice(p.peculiaridades, { "" }, { it })
                    .also { if (it >= 0) viewModel.removerPeculiaridade(it) }
                else -> return """{"erro":"seção inválida: $secao"}"""
            }
            return if (idx >= 0) {
                viewModel.autoSaveIA()
                "OK: removida 1 ocorrência de '$alvo' em $secao (índice $idx)."
            } else "Nada removido: '$alvo' não encontrado em $secao."
        }

        // adicionar / alterar — só para itens com catálogo (id real)
        fun parseValor(k: String): Int? =
            Regex("$k\\s*=\\s*(-?\\d+)").find(valor)?.groupValues?.get(1)?.toIntOrNull()
        val nivel = parseValor("nivel") ?: 1
        val custo = parseValor("custo") ?: 0
        val esp   = Regex("esp\\s*=\\s*([^;]+)").find(valor)?.groupValues?.get(1)?.trim() ?: ""

        when (secao) {
            "vantagens", "desvantagens" -> {
                val v = repository.vantagens.find { norm(it.id) == alvoN || norm(it.nome) == alvoN }
                val d = repository.desvantagens.find { norm(it.id) == alvoN || norm(it.nome) == alvoN }
                if (op == "alterar") {
                    val iv = ultimoIndice(p.vantagens, { it.definicaoId }, { it.nome })
                    if (iv >= 0) {
                        viewModel.atualizarVantagem(iv, p.vantagens[iv].copy(
                            nivel = nivel, custoEscolhido = if (custo != 0) custo else p.vantagens[iv].custoEscolhido))
                        viewModel.autoSaveIA(); return "OK: vantagem '$alvo' alterada (nivel=$nivel)."
                    }
                    return "Nada alterado: '$alvo' não está na ficha."
                }
                when {
                    v != null -> viewModel.adicionarVantagem(v, nivel = nivel,
                        custo = if (custo != 0) custo else v.getCustoBase())
                    d != null -> viewModel.adicionarDesvantagem(d, nivel = nivel,
                        custo = if (custo != 0) custo else d.getCustoBase())
                    else -> return "Não encontrado no catálogo: '$alvo'."
                }
                viewModel.autoSaveIA(); return "OK: '$alvo' adicionada em $secao."
            }
            "pericias" -> {
                val def = repository.pericias.find { norm(it.id) == alvoN || norm(it.nome) == alvoN }
                    ?: return "Perícia não encontrada no catálogo: '$alvo'."
                val pts = com.gurps.ficha.domain.rules.CharacterRules.calcularPontosParaNivel(
                    com.gurps.ficha.model.Dificuldade.fromSigla(def.dificuldadeFixa),
                    p.getAtributo(def.atributoBase), if (nivel > 1) nivel else 12)
                viewModel.adicionarPericia(def, pts, esp)
                viewModel.autoSaveIA(); return "OK: perícia '$alvo' ${if (op=="alterar") "ajustada" else "adicionada"} (NH $nivel)."
            }
            "qualidades"     -> { viewModel.adicionarQualidade(alvo); viewModel.autoSaveIA(); return "OK: qualidade adicionada." }
            "peculiaridades" -> { viewModel.adicionarPeculiaridade(alvo); viewModel.autoSaveIA(); return "OK: peculiaridade adicionada." }
            else -> return """{"erro":"$op em $secao não suportado por esta ferramenta"}"""
        }
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
