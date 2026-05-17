package com.gurps.ficha.domain

import android.util.Log
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.data.network.MestreIAItem
import com.gurps.ficha.data.network.MestreIAPromptsForjador
import com.gurps.ficha.data.network.MestreIAResponse
import com.gurps.ficha.data.network.MestreIATools
import com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapter
import com.gurps.ficha.domain.rules.CharacterRules
import com.gurps.ficha.domain.tools.ForjadorToolExecutor
import com.gurps.ficha.domain.tools.ForjadorTools
import com.gurps.ficha.model.*
import com.gurps.ficha.viewmodel.FichaViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MestreIAGeneratorUseCase(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository
) {
    suspend fun gerarOuAnalisarFicha(
        prompt: String,
        modo: String,
        onStatusUpdate: (String) -> Unit,
        onChunk: (String) -> Unit,
        onResultado: (Boolean, MestreIAClient.ChatResponse) -> Unit
    ) = withContext(Dispatchers.IO) {
        onStatusUpdate("Consultando o Códex para $modo...")
        val catalogoLocal = MestreIAUseCase(viewModel, repository).gerarCatalogoDireto(prompt, viewModel.mestreIAChatHistory)

        // Lote A: injeta catálogo real de IDs no prompt do Forjador
        // vantagens já inclui as de Artes Marciais (merge feito no CatalogLoaders)
        // periciasSuplementares (AM) são tipo diferente — incluídas só no catálogo textual
        // Lote D: inclui budget de pontos para o modelo respeitar o limite
        val pontosIniciais = viewModel.personagem.pontosIniciais
        val promptForjador = MestreIAPromptsForjador.gerarPromptComCatalogo(
            vantagens    = repository.vantagens.map { it.id to it.nome },
            desvantagens = repository.desvantagens.map { it.id to it.nome },
            pericias     = repository.pericias.map { it.id to it.nome } +
                           repository.periciasSuplementares.map { it.id to it.nome },
            magias       = repository.magias.map { it.id to it.nome },
            pontosIniciais = pontosIniciais
        )

        // Forjador: DeepSeek paga como primário, Gemini Pro como fallback
        val fila = listOf(
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_KEY, com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_MODEL),
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_LITE_1_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_KEY, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_3_1_PRO)
        )

        // Lote E: executor de tools do Forjador Agêntico
        val nexusAdapter = NexusArcanoModoAlvoAdapter(repository.magias)
        val toolExecutor = ForjadorToolExecutor(viewModel, repository, nexusAdapter)

        // Extrai o nome do personagem do prompt para usar na narrativa paralela
        val nomePersonagem = Regex("""chamado\s+([^,—\-\n]+)""", RegexOption.IGNORE_CASE)
            .find(prompt)?.groupValues?.get(1)?.trim() ?: "o personagem"

        var sucesso = false
        for (config in fila) {
            if (config.second.isBlank()) continue
            val nomeModelo = if (config.third.contains("gemini")) "Arcano" else "Forjador"

            try {
                var narrativaTexto = ""
                var sheetResponse: MestreIAClient.ChatResponse? = null

                coroutineScope {
                    // Job 1: Narrativa rápida — sem tools, sem RAG, aparece primeiro na tela
                    val narrativaDeferred = async {
                        MestreIAClient.perguntarAoMestre(
                            baseUrl = config.first, apiKey = config.second, workspaceSlug = config.third,
                            prompt = "Escreva a história de origem e aparência física de $nomePersonagem em 2 parágrafos evocativos para RPG. Seja imersivo e cinematográfico. Não mencione atributos numéricos ou mecânicas de jogo.",
                            history = emptyList(),
                            contextoPersonagem = "",
                            catalogo = null,
                            modo = "conversa",
                            promptSistema = "Você é um escritor especializado em RPG de fantasia. Crie histórias de personagens ricas, dramáticas e imersivas.",
                            desativarTools = true
                        )
                    }

                    // Job 2: Ficha via loop agêntico — roda em paralelo com a narrativa
                    val fichaDeferred = async {
                        onStatusUpdate("Mestre $nomeModelo forjando a ficha...")
                        val localHistory = mutableListOf<Pair<String, String>>()
                        var promptAtual = prompt
                        var response: MestreIAClient.ChatResponse? = null

                        for (iteracao in 1..4) {
                            val histBase = viewModel.mestreIAChatHistory.takeLast(4).map { it.role to it.text }
                            val histCompleto = histBase + localHistory

                            response = MestreIAClient.perguntarAoMestre(
                                baseUrl = config.first, apiKey = config.second, workspaceSlug = config.third,
                                prompt = promptAtual,
                                history = histCompleto,
                                contextoPersonagem = viewModel.personagem.toJson(),
                                catalogo = catalogoLocal.catalogo,
                                modo = modo,
                                promptSistema = promptForjador,
                                onChunk = null,
                                desativarTools = iteracao >= 4
                            )

                            if (response.text.contains("Erro de API") || response.text.startsWith("Erro")) break

                            val forjadorCalls = response.toolCalls.filter { tc ->
                                tc.name == ForjadorTools.TOOL_LER_FICHA ||
                                tc.name == ForjadorTools.TOOL_BUSCAR     ||
                                tc.name == ForjadorTools.TOOL_GPS_MAGIA
                            }

                            if (forjadorCalls.isEmpty()) break

                            val resultados = forjadorCalls.joinToString("\n\n") { tc ->
                                onStatusUpdate("${tc.name.replace("forjador_", "")}...")
                                "=== ${tc.name} ===\n${toolExecutor.execute(tc)}"
                            }
                            Log.d("MestreIA_Forjador", "Iteração $iteracao: ${forjadorCalls.size} tool(s) → ${resultados.length} chars")

                            localHistory.add("model" to "Dados coletados com sucesso.")
                            localHistory.add("user" to "=== RESULTADO DAS FERRAMENTAS (iteração $iteracao) ===\n$resultados")
                            promptAtual = if (iteracao >= 3) {
                                "[SÍNTESE FINAL OBRIGATÓRIA] Você já tem todos os dados necessários. NÃO chame ferramentas. Gere AGORA o JSON completo da ficha usando os IDs reais encontrados acima. Responda APENAS com o JSON, sem texto adicional."
                            } else {
                                "Dados coletados acima. Continue a análise — use mais ferramentas se necessário, ou finalize se já tiver tudo."
                            }
                            onStatusUpdate("Mestre $nomeModelo processando dados (iteração $iteracao)...")
                        }
                        response
                    }

                    // Narrativa chega em ~2s → aparece imediatamente no chat
                    onStatusUpdate("Escrevendo a história de $nomePersonagem...")
                    val narrativaResp = narrativaDeferred.await()
                    if (narrativaResp.text.isNotBlank() && !narrativaResp.text.startsWith("Erro")) {
                        narrativaTexto = narrativaResp.text
                        onChunk(narrativaTexto)
                    }

                    // Ficha demora mais — aguarda enquanto usuário já lê a história
                    onStatusUpdate("Finalizando a ficha de $nomePersonagem...")
                    sheetResponse = fichaDeferred.await()
                }

                val finalResponse = sheetResponse ?: continue
                if (!finalResponse.text.contains("Erro de API") && !finalResponse.text.startsWith("Erro")) {
                    // Combina narrativa + JSON: o chat exibe a história e o parser extrai o JSON
                    val respostaCombinada = if (narrativaTexto.isNotBlank()) {
                        finalResponse.copy(text = narrativaTexto + "\n\n" + finalResponse.text)
                    } else {
                        finalResponse
                    }
                    onResultado(true, respostaCombinada)
                    sucesso = true
                    break
                }
            } catch (e: Exception) {
                Log.e("MestreIA_Forjador", "Falha no Gerador: ${e.message}")
            }
        }
        if (!sucesso) onResultado(false, MestreIAClient.ChatResponse("Erro: Falha na conexão com os forjadores."))
    }

    fun gerarRelatorio(ficha: MestreIAResponse): RelatorioValidacao {
        fun validarItem(id: String?, nome: String, isMagia: Boolean = false): ItemValidacao {
            val entrada = id ?: nome
            if (!id.isNullOrBlank()) {
                val achouV = repository.vantagens.find { it.id == id }
                if (achouV != null) return ItemValidacao(entrada, id, achouV.nome, StatusValidacao.OK, "✅ ${achouV.nome}")
                val achouD = repository.desvantagens.find { it.id == id }
                if (achouD != null) return ItemValidacao(entrada, id, achouD.nome, StatusValidacao.OK, "✅ ${achouD.nome}")
                val achouP = repository.pericias.find { it.id == id }
                if (achouP != null) return ItemValidacao(entrada, id, achouP.nome, StatusValidacao.OK, "✅ ${achouP.nome}")
                val achouM = repository.magias.find { it.id == id }
                if (achouM != null) return ItemValidacao(entrada, id, achouM.nome, StatusValidacao.OK, "✅ ${achouM.nome}")
                return ItemValidacao(entrada, null, null, StatusValidacao.FALLBACK, "⚠️ ID '$id' não encontrado → virará Qualidade")
            }
            if (nome.isNotBlank()) {
                val nomeLimpo = limparNome(nome)
                val fuzzyV = repository.vantagens.find { limparNome(it.nome) == nomeLimpo }
                if (fuzzyV != null) return ItemValidacao(nome, fuzzyV.id, fuzzyV.nome, StatusValidacao.FUZZY, "〰️ Nome fuzzy → ${fuzzyV.nome}")
                val fuzzyD = repository.desvantagens.find { limparNome(it.nome) == nomeLimpo }
                if (fuzzyD != null) return ItemValidacao(nome, fuzzyD.id, fuzzyD.nome, StatusValidacao.FUZZY, "〰️ Nome fuzzy → ${fuzzyD.nome}")
                val fuzzyP = repository.pericias.find { limparNome(it.nome) == nomeLimpo }
                if (fuzzyP != null) return ItemValidacao(nome, fuzzyP.id, fuzzyP.nome, StatusValidacao.FUZZY, "〰️ Nome fuzzy → ${fuzzyP.nome}")
                val fuzzyM = repository.magias.find { limparNome(it.nome) == nomeLimpo }
                if (fuzzyM != null) return ItemValidacao(nome, fuzzyM.id, fuzzyM.nome, StatusValidacao.FUZZY, "〰️ Nome fuzzy → ${fuzzyM.nome}")
            }
            return ItemValidacao(entrada, null, null, StatusValidacao.FALLBACK, "⚠️ Não encontrado → virará Qualidade/Peculiaridade")
        }

        val itensV = ficha.vantagens.map    { validarItem(it.id, it.nome) }
        val itensD = ficha.desvantagens.map { validarItem(it.id, it.nome) }
        val itensP = ficha.pericias.map     { validarItem(it.id, it.nome) }
        val itensM = ficha.magias.map       { validarItem(it.id, it.nome, isMagia = true) }

        val todosItens = itensV + itensD + itensP + itensM
        val totalOk = todosItens.count { it.status == StatusValidacao.OK || it.status == StatusValidacao.FUZZY }
        val totalFallback = todosItens.count { it.status == StatusValidacao.FALLBACK }

        return RelatorioValidacao(
            vantagens    = itensV,
            desvantagens = itensD,
            pericias     = itensP,
            magias       = itensM,
            totalOk      = totalOk,
            totalFallback= totalFallback,
            alertaBudget = validarBudget(ficha)
        )
    }

    fun validarBudget(ficha: MestreIAResponse): String? {
        val st = ficha.atributos.st; val dx = ficha.atributos.dx
        val iq = ficha.atributos.iq; val ht = ficha.atributos.ht
        val custoAtributos = ((st - 10).coerceAtLeast(0) * 10) +
                             ((dx - 10).coerceAtLeast(0) * 20) +
                             ((iq - 10).coerceAtLeast(0) * 20) +
                             ((ht - 10).coerceAtLeast(0) * 10)
        val custoVantagens    = ficha.vantagens.sumOf    { it.custo ?: 0 }
        val custoDesvantagens = ficha.desvantagens.sumOf { it.custo ?: 0 } // já negativo
        val custoPericias     = ficha.pericias.sumOf     { it.nivel * 2 }  // estimativa
        val total = custoAtributos + custoVantagens + custoDesvantagens + custoPericias
        val max = viewModel.personagem.pontosIniciais
        return if (total > max) {
            Log.w("MestreIA_Forjador", "Budget excedido: $total pts (máximo: $max pts)")
            "⚠️ Ficha usa ~$total pts (máximo: $max pts)"
        } else null
    }

    fun integrarRespostaNaFicha(ficha: MestreIAResponse) {
        viewModel.atualizarNome(ficha.nome)
        viewModel.atualizarHistorico(ficha.historico)
        viewModel.atualizarAparencia(ficha.aparencia ?: "")

        viewModel.atualizarForca(ficha.atributos.st)
        viewModel.atualizarDestreza(ficha.atributos.dx)
        viewModel.atualizarInteligencia(ficha.atributos.iq)
        viewModel.atualizarVitalidade(ficha.atributos.ht)

        ficha.vantagens.forEach    { v -> adicionarVantagem(v, v.descricao ?: "", v.custo ?: 0) }
        ficha.desvantagens.forEach { d -> adicionarVantagem(d, d.descricao ?: "", d.custo ?: 0) }
        ficha.pericias.forEach     { p -> adicionarPericia(p, p.nivel) }
        ficha.magias.forEach       { m -> adicionarMagia(m) }

        ficha.equipamentos.forEach { eq ->
            viewModel.adicionarEquipamento(Equipamento(
                nome = eq.nome, peso = eq.peso, custo = eq.custo,
                quantidade = eq.quantidade, armaDanoRaw = eq.dano,
                armaStMinimo = eq.st_min,
                notas = if ((eq.rd ?: 0) > 0) "RD: ${eq.rd}" else ""
            ))
        }
    }

    private fun adicionarVantagem(item: MestreIAItem, desc: String, custo: Int) {
        // 1. Lookup por ID direto — caminho feliz (Lote A)
        if (!item.id.isNullOrBlank()) {
            repository.vantagens.find { it.id == item.id }?.let {
                Log.d("MestreIA_Forjador", "Vantagem por ID: ${item.id}")
                viewModel.adicionarVantagem(it, custo = if (custo != 0) custo else it.getCustoBase(), desc = desc)
                return
            }
            repository.desvantagens.find { it.id == item.id }?.let {
                Log.d("MestreIA_Forjador", "Desvantagem por ID: ${item.id}")
                viewModel.adicionarDesvantagem(it, custo = if (custo != 0) custo else it.getCustoBase(), desc = desc)
                return
            }
        }

        // 2. Fallback: fuzzy match por nome (comportamento legado)
        val nomeLimpo = limparNome(item.nome)
        repository.vantagens.find { limparNome(it.nome) == nomeLimpo }?.let {
            Log.d("MestreIA_Forjador", "Vantagem por nome fuzzy: ${item.nome}")
            viewModel.adicionarVantagem(it, custo = if (custo != 0) custo else it.getCustoBase(), desc = desc)
            return
        }
        repository.desvantagens.find { limparNome(it.nome) == nomeLimpo }?.let {
            Log.d("MestreIA_Forjador", "Desvantagem por nome fuzzy: ${item.nome}")
            viewModel.adicionarDesvantagem(it, custo = if (custo != 0) custo else it.getCustoBase(), desc = desc)
            return
        }

        // 3. Não achou — Qualidade ou Peculiaridade
        Log.w("MestreIA_Forjador", "Não encontrado no catálogo, fallback: ${item.id ?: item.nome}")
        if (custo >= 0) viewModel.adicionarQualidade("${item.nome.ifBlank { item.id ?: "?" }} ($custo pts): $desc")
        else            viewModel.adicionarPeculiaridade("${item.nome.ifBlank { item.id ?: "?" }} ($custo pts): $desc")
    }

    private fun adicionarPericia(item: MestreIAItem, nivel: Int) {
        // 1. Lookup por ID direto (Lote A)
        if (!item.id.isNullOrBlank()) {
            repository.pericias.find { it.id == item.id }?.let { def ->
                Log.d("MestreIA_Forjador", "Perícia por ID: ${item.id}")
                val pts = CharacterRules.calcularPontosParaNivel(
                    Dificuldade.fromSigla(def.dificuldadeFixa),
                    viewModel.personagem.getAtributo(def.atributoBase),
                    nivel
                )
                viewModel.adicionarPericia(def, pts)
                return
            }
        }

        // 2. Fallback: fuzzy match por nome
        val nomeLimpo = limparNome(item.nome)
        repository.pericias.find { limparNome(it.nome) == nomeLimpo }?.let { def ->
            Log.d("MestreIA_Forjador", "Perícia por nome fuzzy: ${item.nome}")
            val pts = CharacterRules.calcularPontosParaNivel(
                Dificuldade.fromSigla(def.dificuldadeFixa),
                viewModel.personagem.getAtributo(def.atributoBase),
                nivel
            )
            viewModel.adicionarPericia(def, pts)
            return
        }

        Log.w("MestreIA_Forjador", "Perícia não encontrada, fallback Qualidade: ${item.id ?: item.nome}")
        viewModel.adicionarQualidade("Perícia: ${item.nome.ifBlank { item.id ?: "?" }} (NH $nivel)")
    }

    private fun adicionarMagia(item: MestreIAItem) {
        // 1. Lookup por ID direto (Lote A)
        if (!item.id.isNullOrBlank()) {
            repository.magias.find { it.id == item.id }?.let {
                Log.d("MestreIA_Forjador", "Magia por ID: ${item.id}")
                viewModel.adicionarMagia(it)
                return
            }
        }

        // 2. Fallback: fuzzy match por nome
        val nomeLimpo = limparNome(item.nome)
        repository.magias.find { limparNome(it.nome) == nomeLimpo }?.let {
            Log.d("MestreIA_Forjador", "Magia por nome fuzzy: ${item.nome}")
            viewModel.adicionarMagia(it)
            return
        }

        Log.w("MestreIA_Forjador", "Magia não encontrada, fallback Qualidade: ${item.id ?: item.nome}")
        viewModel.adicionarQualidade("Magia: ${item.nome.ifBlank { item.id ?: "?" }} (${item.custo ?: 0} fp)")
    }

    private fun limparNome(nome: String): String =
        nome.lowercase()
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("\\d+"), "")
            .trim()
            .replace(" ", "")
}
