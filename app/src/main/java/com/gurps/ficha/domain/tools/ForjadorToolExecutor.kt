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
        val resultado = when (toolCall.name) {
            ForjadorTools.TOOL_LER_FICHA    -> lerFicha(toolCall.args)
            ForjadorTools.TOOL_BUSCAR       -> buscarCatalogo(toolCall.args)
            ForjadorTools.TOOL_GPS_MAGIA    -> gpsMagia(toolCall.args)
            ForjadorTools.TOOL_EDITAR       -> editarFicha(toolCall.args)
            else -> """{"erro": "ferramenta desconhecida: ${toolCall.name}"}"""
        }
        // Loga o RESULTADO (não só a entrada) — auditoria real no logcat.
        Log.d("Forjador_Tools", "Resultado ${toolCall.name}: ${resultado.take(300)}")
        return resultado
    }

    /** Read-back: relê uma seção pelo nome (reusa lerFicha). Usado na verificação pós-edição. */
    fun lerSecao(secao: String): String = lerFicha(JSONObject().put("secao", secao))

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
            "tecnicas" -> if (p.tecnicas.isEmpty()) "Nenhuma técnica." else
                p.tecnicas.joinToString("\n") { t ->
                    val nh = t.calcularNivel(p)?.toString() ?: "ÓRFÃ (perícia-base ausente)"
                    "• ${t.definicaoId} | ${t.nome} | base:${t.periciaBaseDefinicaoId.ifBlank { "?" }} | NH $nh | ${t.pontosGastos} pts"
                }
            "qualidades" -> if (p.qualidades.isEmpty()) "Nenhuma qualidade." else
                p.qualidades.joinToString("\n") { "• $it" }
            "peculiaridades" -> if (p.peculiaridades.isEmpty()) "Nenhuma peculiaridade." else
                p.peculiaridades.joinToString("\n") { "• $it" }
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
            else -> """{"erro": "seção inválida: $secao. Use: atributos, vantagens, desvantagens, pericias, tecnicas, magias, equipamentos, qualidades, peculiaridades, pontos"}"""
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
                    // Status igual ao que o USUÁRIO vê na tela de magias:
                    // "✓ Requisitos Atendidos" ou o motivo do bloqueio.
                    val status = viewModel.prereqFailureForMagia(m)
                        ?.let { "⚠ FALTA: $it" } ?: "✓ requisitos atendidos"
                    "• ${m.id} | ${m.nome} | escola:${m.escola?.joinToString() ?: "?"} | $status | pré:${m.preRequisitos?.take(60) ?: "—"}"
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

        // VEREDITO determinístico: o alvo já é aprendível AGORA?
        val jaTem = alvoId in magiasConhecidas
        val faltaPrereq = repository.magias.find { it.id == alvoId }
            ?.let { viewModel.prereqFailureForMagia(it) }
        val aprendivel = !jaTem && faltaPrereq == null

        return buildString {
            appendLine("=== GPS de Magias: $alvoNome ($alvoId) ===")
            // Veredito CLARO no topo — não recalcule, obedeça este resultado.
            when {
                jaTem -> appendLine("VEREDITO: ✅ '$alvoNome' JÁ ESTÁ na ficha. Nada a fazer.")
                aprendivel -> appendLine("VEREDITO: ✅ PODE ADICIONAR '$alvoNome' AGORA — pré-requisitos atendidos. Chame forjador_editar_ficha adicionar magias \"$alvoId\".")
                else -> appendLine("VEREDITO: ⛔ AINDA NÃO pode adicionar '$alvoNome'. Falta: $faltaPrereq")
            }
            // Os campos abaixo são CONTEXTO. NÃO recalcule escolas/contagens
            // de cabeça — o número do app é a verdade.
            snapshot.progressoCadeia?.let { appendLine(if (it.startsWith("Cadeia")) it else "Cadeia: $it") }
            if (snapshot.progressoEscolas.isNotEmpty())
                appendLine(snapshot.progressoEscolas.joinToString().let { if (it.startsWith("Escola")) it else "Escolas: $it" })
            appendLine("Próximas magias a aprender: ${snapshot.proximasAcoesIds.mapNotNull { id ->
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

        // ATRIBUTOS — caso especial: só "alterar", valor = novo valor base.
        // Aceita "ST", "forca", "for", etc. valor pode vir solto ("14") ou "valor=14".
        if (secao == "atributos" || secao == "atributo") {
            val novo = (Regex("-?\\d+").find(valor)?.value
                ?: Regex("-?\\d+").find(alvo)?.value)?.toIntOrNull()
                ?: return """{"erro":"valor numérico do atributo ausente (ex: valor=\"14\")"}"""
            val antes: Int
            when {
                alvoN.startsWith("for") || alvoN == "st" -> { antes = p.forca;       viewModel.atualizarForca(novo) }
                alvoN.startsWith("des") || alvoN == "dx" -> { antes = p.destreza;    viewModel.atualizarDestreza(novo) }
                alvoN.startsWith("int") || alvoN.startsWith("iq") || alvoN.startsWith("ig") -> { antes = p.inteligencia; viewModel.atualizarInteligencia(novo) }
                alvoN.startsWith("vit") || alvoN.startsWith("ht") || alvoN.startsWith("sau") -> { antes = p.vitalidade; viewModel.atualizarVitalidade(novo) }
                else -> return """{"erro":"atributo desconhecido: '$alvo'. Use forca/destreza/inteligencia/vitalidade ou ST/DX/IQ/HT"}"""
            }
            viewModel.autoSaveIA()
            return "OK: atributo '$alvo' alterado de $antes para $novo."
        }

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
                // Idempotente por (perícia + ESPECIALIZAÇÃO): re-adicionar a
                // MESMA perícia/especialização atualiza no lugar. Mas
                // Sobrevivência/Florestas e Sobrevivência/Montanhas são
                // perícias DISTINTAS — não apaga a outra especialização.
                val espN = norm(esp)
                var substituiu = false
                while (true) {
                    val i = viewModel.personagem.pericias.indexOfLast {
                        (norm(it.definicaoId) == alvoN || norm(it.nome) == alvoN) &&
                        norm(it.especializacao) == espN
                    }
                    if (i < 0) break
                    viewModel.removerPericia(i); substituiu = true
                }
                val nhAlvo = if (nivel > 1) nivel else 12
                val pts = com.gurps.ficha.domain.rules.CharacterRules.calcularPontosParaNivel(
                    com.gurps.ficha.model.Dificuldade.fromSigla(def.dificuldadeFixa),
                    viewModel.personagem.getAtributo(def.atributoBase), nhAlvo)
                viewModel.adicionarPericia(def, pts, esp)
                viewModel.autoSaveIA()
                return "OK: perícia '$alvo' ${if (substituiu) "atualizada" else "adicionada"} (NH $nhAlvo, $pts pts)."
            }
            "tecnicas" -> {
                // Técnica precisa de uma PERÍCIA-BASE que já esteja na ficha e
                // atenda o pré-requisito. valor pode trazer periciaBase=<id>.
                val tec = repository.tecnicasCatalogo.find { norm(it.id) == alvoN || norm(it.nome) == alvoN }
                    ?: return "Técnica não encontrada no catálogo: '$alvo'."
                val baseRaw = Regex("periciaBase(?:DefinicaoId)?\\s*=\\s*([^;]+)")
                    .find(valor)?.groupValues?.get(1)?.trim()
                val baseN = baseRaw?.let { norm(it) }

                // 1) Resolve a perícia-base ALVO ANTES de remover nada.
                // Candidatas: perícias da ficha que atendem o pré-requisito.
                val pp0 = viewModel.personagem
                val candidatas = pp0.pericias.filter { per ->
                    viewModel.tecnicaAtendePreRequisito(tec, per)
                }
                if (candidatas.isEmpty())
                    return "Não foi possível adicionar '${tec.nome}': nenhuma perícia da ficha atende o pré-requisito (${tec.preRequisitoRaw.take(80)}). Adicione antes uma perícia compatível."
                val base = (baseN?.let { b -> candidatas.firstOrNull { norm(it.definicaoId) == b || norm(it.nome) == b } })
                    ?: candidatas.maxByOrNull { it.calcularNivel(pp0) }!!
                val baseAlvoN = norm(base.definicaoId)

                // 2) Remoção SELETIVA. Técnica é única por (técnica +
                // perícia-base): Contra-Ataque de Espada ≠ de Machado, ambas
                // coexistem. Só removemos:
                //   (a) mesma técnica + MESMA perícia-base alvo (re-aplicar);
                //   (b) mesma técnica ÓRFÃ (perícia-base não existe na ficha).
                // Técnicas da mesma técnica com OUTRA base válida = preservadas.
                val idsPericiasFicha = pp0.pericias.map { norm(it.definicaoId) }.toSet()
                var substituiu = false
                while (true) {
                    val i = viewModel.personagem.tecnicas.indexOfLast { t ->
                        val mesmaTec = norm(t.definicaoId) == alvoN || norm(t.nome) == alvoN
                        if (!mesmaTec) return@indexOfLast false
                        val baseT = norm(t.periciaBaseDefinicaoId)
                        val mesmaBase = baseT == baseAlvoN
                        val orfa = baseT.isBlank() || baseT !in idsPericiasFicha
                        mesmaBase || orfa
                    }
                    if (i < 0) break
                    viewModel.removerTecnica(i); substituiu = true
                }

                val erro = viewModel.adicionarTecnica(tec, base, nivel.coerceAtLeast(0))
                viewModel.autoSaveIA()
                return if (erro == null)
                    "OK: técnica '${tec.nome}' ${if (substituiu) "re-vinculada" else "adicionada"} sobre a perícia-base '${base.nome}' (predef +${nivel.coerceAtLeast(0)})."
                else "Falha ao adicionar técnica '${tec.nome}': $erro"
            }
            "magias" -> {
                val mag = repository.magias.find { norm(it.id) == alvoN || norm(it.nome) == alvoN }
                    ?: return "Magia não encontrada no catálogo: '$alvo'."
                // Idempotente: remove ocorrência existente antes de re-adicionar.
                var substituiu = false
                while (true) {
                    val i = viewModel.personagem.magias.indexOfLast {
                        norm(it.definicaoId) == alvoN || norm(it.nome) == alvoN
                    }
                    if (i < 0) break
                    viewModel.removerMagia(i); substituiu = true
                }
                // Validação de pré-requisito — MESMA do app (a tela mostra
                // "✓ Requisitos Atendidos" / "Pré-requisito não atendido").
                // Por padrão BARRA, igual o botão bloqueado para o usuário.
                val forcar = Regex("forcar\\s*=\\s*(true|sim|1)", RegexOption.IGNORE_CASE).containsMatchIn(valor)
                val faltaPrereq = viewModel.prereqFailureForMagia(mag)
                if (faltaPrereq != null && !forcar) {
                    return "BLOQUEADO: '${mag.nome}' não pode ser adicionada — $faltaPrereq. " +
                        "Use forjador_gps_magia(\"${mag.id}\") para ver a cadeia completa de " +
                        "pré-requisitos e adicione cada magia faltante (na ordem) ANTES desta. " +
                        "Só se for gatilho narrativo intencional, repita com valor=\"forcar=true\"."
                }
                val erro = viewModel.adicionarMagia(mag, pts = if (custo > 0) custo else 1, ignora = forcar)
                viewModel.autoSaveIA()
                val sufixoForca = if (faltaPrereq != null && forcar) " (ADIÇÃO FORÇADA, sem pré-requisito — gatilho narrativo)" else ""
                return if (erro == null) "OK: magia '${mag.nome}' ${if (substituiu) "atualizada" else "adicionada"}$sufixoForca."
                       else "Falha ao adicionar magia '${mag.nome}': $erro"
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
