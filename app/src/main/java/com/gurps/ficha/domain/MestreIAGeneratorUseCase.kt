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
        // Lote 136: Forjador NÃO usa RAG do manual. O texto bruto do livro
        // (Ponte de Ferro ~35k) só servia ao Auditor de regras e era ruído puro
        // aqui — competia com o JSON pelo limite de tokens e causava truncamento.
        // O Forjador precisa de IDs do catálogo (injetados via promptForjador) e
        // das ferramentas forjador_buscar_catalogo, não do texto do manual.
        // O modo Dúvidas (MestreIAUseCase.conversarComMestreIA) segue intacto.
        val catalogoVazio = MestreIAClient.CatalogoNomes()

        // Lote A: injeta catálogo real de IDs no prompt do Forjador
        // vantagens já inclui as de Artes Marciais (merge feito no CatalogLoaders)
        // periciasSuplementares (AM) são tipo diferente — incluídas só no catálogo textual
        // Lote D: inclui budget de pontos para o modelo respeitar o limite
        val pontosIniciais = viewModel.personagem.pontosIniciais
        val vantagensCat = repository.vantagens.map { it.id to it.nome }
        val desvantagensCat = repository.desvantagens.map { it.id to it.nome }
        val periciasCat = repository.pericias.map { it.id to it.nome } +
            repository.periciasSuplementares.map { it.id to it.nome }
        val magiasCat = repository.magias.map { it.id to it.nome }
        val tecnicasCat = repository.tecnicasCatalogo.map { it.id to it.nome }
        val promptForjador = if (modo == "analise") {
            MestreIAPromptsForjador.gerarPromptConsultor(
                vantagensCat, desvantagensCat, periciasCat, magiasCat, tecnicasCat
            )
        } else {
            MestreIAPromptsForjador.gerarPromptComCatalogo(
                vantagensCat, desvantagensCat, periciasCat, magiasCat, tecnicasCat, pontosIniciais
            )
        }

        // Forjador: DeepSeek paga como primário, Gemini Pro como fallback
        val fila = listOf(
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_KEY, com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_MODEL),
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_LITE_1_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_KEY, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_3_1_PRO)
        )

        // Lote E: executor de tools do Forjador Agêntico
        val nexusAdapter = NexusArcanoModoAlvoAdapter(repository.magias)
        val toolExecutor = ForjadorToolExecutor(viewModel, repository, nexusAdapter)

        // Nome só para mensagens de status. A narrativa usa o PEDIDO INTEIRO
        // (prompt) — extrair "nome" por regex falhava em "crie o Aragorn de
        // senhor dos aneis" (sem "chamado") e a IA inventava um personagem.
        val nomePersonagem = Regex(
            """(?:chamad[oa]|nome(?:ad[oa])?|ficha d[eoa]|crie?\s+[oa]?)\s+([\p{L} .'-]{2,40})""",
            RegexOption.IGNORE_CASE
        ).find(prompt)?.groupValues?.get(1)?.trim()?.trimEnd('.', ',')
            ?.takeIf { it.isNotBlank() } ?: "o personagem solicitado"

        var sucesso = false
        for (config in fila) {
            if (config.second.isBlank()) continue
            val nomeModelo = if (config.third.contains("gemini")) "Arcano" else "Forjador"

            try {
                var sheetResponse: MestreIAClient.ChatResponse? = null

                run {
                    val ehConsultor = modo == "analise"
                    val localHistory = mutableListOf<Pair<String, String>>()

                    // ITERAÇÃO 0 — só no modo CRIAR. O agente escreve/preserva a
                    // história e ela vira base da ficha. No modo CONSULTOR
                    // (analise) NÃO há história: ele lê a ficha existente e
                    // sugere, sem criar personagem novo.
                    if (!ehConsultor) {
                        onStatusUpdate("Mestre $nomeModelo concebendo a história...")
                        val narrativaResp = MestreIAClient.perguntarAoMestre(
                            baseUrl = config.first, apiKey = config.second, workspaceSlug = config.third,
                            prompt = MestreIAPromptsForjador.gerarPromptHistoria(prompt),
                            history = emptyList(),
                            contextoPersonagem = "",
                            catalogo = catalogoVazio,
                            modo = "conversa",
                            promptSistema = MestreIAPromptsForjador.PROMPT_HISTORIA_SISTEMA,
                            onChunk = null,
                            desativarTools = true,
                            maxTokens = 1500
                        )
                        val historiaBase = narrativaResp.text
                            .takeIf { it.isNotBlank() && !it.startsWith("Erro") }
                            ?.trim()
                            .orEmpty()
                        if (historiaBase.isNotBlank()) onChunk(historiaBase)
                        if (historiaBase.isNotBlank()) {
                            localHistory.add("model" to historiaBase)
                            localHistory.add("user" to "Esta é a história/aparência DEFINITIVA do personagem (acima). Agora construa a ficha GURPS COERENTE com ela: as perícias, vantagens e desvantagens devem refletir o que a história descreve. No JSON final, use EXATAMENTE esta história no campo \"historico\" e a descrição física no campo \"aparencia\" — não reescreva.")
                        }
                    }

                    onStatusUpdate(
                        if (ehConsultor) "Mestre $nomeModelo analisando a ficha..."
                        else "Mestre $nomeModelo forjando a ficha de $nomePersonagem..."
                    )
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
                            catalogo = catalogoVazio,
                            modo = modo,
                            promptSistema = promptForjador,
                            onChunk = null,
                            desativarTools = iteracao >= 4,
                            maxTokens = if (iteracao >= 4) 8192 else 2048
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
                            if (modo == "analise") {
                                "[RESPOSTA FINAL] Você já leu a ficha e o catálogo. NÃO chame ferramentas e NÃO gere JSON. Responda em TEXTO ao usuário: análise objetiva da ficha + sugestões priorizadas (cite o ID real de cada vantagem/perícia/magia sugerida e o porquê, ligado ao conceito do personagem). Termine perguntando se ele quer que você aplique alguma das sugestões."
                            } else {
                                "[SÍNTESE FINAL OBRIGATÓRIA] Você já tem todos os dados necessários. NÃO chame ferramentas. Gere AGORA o JSON completo da ficha usando os IDs reais encontrados acima. No campo \"historico\" copie EXATAMENTE a história já definida no início desta conversa (não reescreva nem resuma) e no campo \"aparencia\" a descrição física correspondente. Responda APENAS com o JSON, sem texto adicional."
                            }
                        } else {
                            "Dados coletados acima. Continue a análise — use mais ferramentas se necessário, ou finalize se já tiver tudo."
                        }
                        onStatusUpdate("Mestre $nomeModelo processando dados (iteração $iteracao)...")
                    }
                    sheetResponse = response
                }

                val finalResponse = sheetResponse ?: continue
                if (!finalResponse.text.contains("Erro de API") && !finalResponse.text.startsWith("Erro")) {
                    onResultado(true, finalResponse)
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

        fun validarTecnica(id: String?, nome: String): ItemValidacao {
            val entrada = id ?: nome
            if (!id.isNullOrBlank()) {
                repository.tecnicasCatalogo.find { it.id == id }?.let {
                    return ItemValidacao(entrada, id, it.nome, StatusValidacao.OK, "✅ ${it.nome}")
                }
            }
            val alvo = limparNome(nome)
            repository.tecnicasCatalogo.find { limparNome(it.nome) == alvo }?.let {
                return ItemValidacao(nome, it.id, it.nome, StatusValidacao.FUZZY, "〰️ Técnica fuzzy → ${it.nome}")
            }
            return ItemValidacao(entrada, null, null, StatusValidacao.FALLBACK, "⚠️ Técnica não encontrada → virará Qualidade")
        }

        val itensV = ficha.vantagens.map    { validarItem(it.id, it.nome) }
        val itensD = ficha.desvantagens.map { validarItem(it.id, it.nome) }
        val itensP = ficha.pericias.map     { validarItem(it.id, it.nome) }
        val itensM = ficha.magias.map       { validarItem(it.id, it.nome, isMagia = true) }
        val itensT = ficha.tecnicas.map     { validarTecnica(it.id, it.nome) }

        val todosItens = itensV + itensD + itensP + itensM + itensT
        val totalOk = todosItens.count { it.status == StatusValidacao.OK || it.status == StatusValidacao.FUZZY }
        val totalFallback = todosItens.count { it.status == StatusValidacao.FALLBACK }

        return RelatorioValidacao(
            vantagens    = itensV,
            desvantagens = itensD,
            pericias     = itensP,
            magias       = itensM,
            totalOk      = totalOk,
            totalFallback= totalFallback,
            alertaBudget = validarBudget(ficha),
            tecnicas     = itensT
        )
    }

    fun validarBudget(ficha: MestreIAResponse): String? {
        val attr = ficha.atributosEfetivos()
        val st = attr.st; val dx = attr.dx
        val iq = attr.iq; val ht = attr.ht
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
        if (ficha.notas.isNotBlank()) viewModel.atualizarNotas(ficha.notas)
        if (ficha.pontosIniciais > 0) viewModel.atualizarPontosIniciais(ficha.pontosIniciais)

        val attr = ficha.atributosEfetivos()
        viewModel.atualizarForca(attr.st)
        viewModel.atualizarDestreza(attr.dx)
        viewModel.atualizarInteligencia(attr.iq)
        viewModel.atualizarVitalidade(attr.ht)

        // Dedup defensivo: LLMs (DeepSeek) às vezes geram a mesma lista 2x
        // dentro do JSON. Sem isto, vantagens por-nível e equipamentos
        // (que não têm dedup no app) entram duplicados na ficha.
        fun chave(it: MestreIAItem) = (it.id ?: it.nome).lowercase().trim() +
            "|" + (it.especializacao ?: "").lowercase().trim()
        val vantagensU    = ficha.vantagens.distinctBy(::chave)
        val desvantagensU = ficha.desvantagens.distinctBy(::chave)
        val periciasU     = ficha.pericias.distinctBy(::chave)
        val tecnicasU     = ficha.tecnicas.distinctBy(::chave)
        val magiasU       = ficha.magias.distinctBy(::chave)
        val equipamentosU = ficha.equipamentos.distinctBy {
            it.nome.lowercase().trim() + "|" + (it.tipo ?: "")
        }

        // Ordem importa: perícias antes de técnicas (técnica precisa da perícia-base já na ficha)
        vantagensU.forEach    { v -> adicionarVantagem(v, v.descricao ?: "", v.custo ?: 0) }
        desvantagensU.forEach { d -> adicionarVantagem(d, d.descricao ?: "", d.custo ?: 0) }
        periciasU.forEach     { p -> adicionarPericia(p, p.nivel) }
        tecnicasU.forEach     { t -> adicionarTecnica(t) }
        magiasU.forEach       { m -> adicionarMagia(m) }

        ficha.qualidades.distinctBy(::chave).forEach { q -> viewModel.adicionarQualidade(textoLivre(q)) }
        ficha.peculiaridades.distinctBy(::chave).forEach { p -> viewModel.adicionarPeculiaridade(textoLivre(p)) }

        equipamentosU.forEach { eq ->
            val tipoEnum = when (eq.tipo?.uppercase()) {
                "ARMA"     -> TipoEquipamento.ARMA
                "ARMADURA" -> TipoEquipamento.ARMADURA
                "ESCUDO"   -> TipoEquipamento.ESCUDO
                "CAPA"     -> TipoEquipamento.CAPA
                else       -> TipoEquipamento.GERAL
            }
            viewModel.adicionarEquipamento(Equipamento(
                nome = eq.nome, peso = eq.peso, custo = eq.custo,
                quantidade = eq.quantidade,
                tipo = tipoEnum,
                bonusDefesa = eq.bonusDefesa ?: 0,
                armaCatalogoId = eq.catalogoId,
                armaTipoCombate = eq.tipoCombate,
                armaDanoRaw = eq.dano,
                armaStMinimo = eq.st_min,
                notas = eq.notas?.takeIf { it.isNotBlank() }
                    ?: if ((eq.rd ?: 0) > 0) "RD: ${eq.rd}" else ""
            ))
        }
    }

    private fun textoLivre(item: MestreIAItem): String {
        val base = item.nome.ifBlank { item.id ?: "?" }
        return item.descricao?.takeIf { it.isNotBlank() }?.let { "$base: $it" } ?: base
    }

    /** Converte os modificadores da IA em ModificadorSelecao casando com o catálogo da definição. */
    private fun construirMods(
        item: MestreIAItem,
        defMods: List<ModificadorDefinicao>
    ): List<ModificadorSelecao> {
        if (item.modificadores.isEmpty() || defMods.isEmpty()) return emptyList()
        return item.modificadores.mapNotNull { m ->
            val def = (m.id?.let { id -> defMods.find { it.id == id } })
                ?: defMods.find { limparNome(it.nome) == limparNome(m.nome) }
                ?: return@mapNotNull null
            ModificadorSelecao(
                id = def.id,
                nome = def.nome,
                valor = Regex("-?\\d+").find(def.valor)?.value?.toIntOrNull() ?: 0,
                porNivel = def.porNivel,
                niveis = m.niveis.coerceAtLeast(1),
                descricao = def.descricao,
                pagina = def.pagina
            )
        }
    }

    private fun adicionarVantagem(item: MestreIAItem, desc: String, custo: Int) {
        val nivel = item.nivel.coerceAtLeast(1)

        // 1. Lookup por ID direto — caminho feliz
        if (!item.id.isNullOrBlank()) {
            repository.vantagens.find { it.id == item.id }?.let {
                Log.d("MestreIA_Forjador", "Vantagem por ID: ${item.id}")
                viewModel.adicionarVantagem(it, nivel = nivel,
                    custo = if (custo != 0) custo else it.getCustoBase(), desc = desc,
                    mods = construirMods(item, it.modificadoresEspecificos))
                return
            }
            repository.desvantagens.find { it.id == item.id }?.let {
                Log.d("MestreIA_Forjador", "Desvantagem por ID: ${item.id}")
                viewModel.adicionarDesvantagem(it, nivel = nivel,
                    custo = if (custo != 0) custo else it.getCustoBase(), desc = desc,
                    ctrl = item.autocontrole,
                    mods = construirMods(item, it.modificadoresEspecificos))
                return
            }
        }

        // 2. Fallback: fuzzy match por nome
        val nomeLimpo = limparNome(item.nome)
        repository.vantagens.find { limparNome(it.nome) == nomeLimpo }?.let {
            Log.d("MestreIA_Forjador", "Vantagem por nome fuzzy: ${item.nome}")
            viewModel.adicionarVantagem(it, nivel = nivel,
                custo = if (custo != 0) custo else it.getCustoBase(), desc = desc,
                mods = construirMods(item, it.modificadoresEspecificos))
            return
        }
        repository.desvantagens.find { limparNome(it.nome) == nomeLimpo }?.let {
            Log.d("MestreIA_Forjador", "Desvantagem por nome fuzzy: ${item.nome}")
            viewModel.adicionarDesvantagem(it, nivel = nivel,
                custo = if (custo != 0) custo else it.getCustoBase(), desc = desc,
                ctrl = item.autocontrole,
                mods = construirMods(item, it.modificadoresEspecificos))
            return
        }

        // 3. Não achou — Qualidade ou Peculiaridade
        Log.w("MestreIA_Forjador", "Não encontrado no catálogo, fallback: ${item.id ?: item.nome}")
        if (custo >= 0) viewModel.adicionarQualidade("${item.nome.ifBlank { item.id ?: "?" }} ($custo pts): $desc")
        else            viewModel.adicionarPeculiaridade("${item.nome.ifBlank { item.id ?: "?" }} ($custo pts): $desc")
    }

    private fun adicionarPericia(item: MestreIAItem, nivel: Int) {
        val esp = item.especializacao ?: ""

        // 1. Lookup por ID direto
        if (!item.id.isNullOrBlank()) {
            repository.pericias.find { it.id == item.id }?.let { def ->
                Log.d("MestreIA_Forjador", "Perícia por ID: ${item.id}")
                val pts = CharacterRules.calcularPontosParaNivel(
                    Dificuldade.fromSigla(def.dificuldadeFixa),
                    viewModel.personagem.getAtributo(def.atributoBase),
                    nivel
                )
                viewModel.adicionarPericia(def, pts, esp)
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
            viewModel.adicionarPericia(def, pts, esp)
            return
        }

        Log.w("MestreIA_Forjador", "Perícia não encontrada, fallback Qualidade: ${item.id ?: item.nome}")
        viewModel.adicionarQualidade("Perícia: ${item.nome.ifBlank { item.id ?: "?" }} (NH $nivel)")
    }

    private fun adicionarTecnica(item: MestreIAItem) {
        val def = (item.id?.takeIf { it.isNotBlank() }
            ?.let { id -> repository.tecnicasCatalogo.find { it.id == id } })
            ?: repository.tecnicasCatalogo.find { limparNome(it.nome) == limparNome(item.nome) }

        if (def == null) {
            Log.w("MestreIA_Forjador", "Técnica não encontrada, fallback Qualidade: ${item.id ?: item.nome}")
            viewModel.adicionarQualidade("Técnica: ${item.nome.ifBlank { item.id ?: "?" }}")
            return
        }

        val periciaBase = encontrarPericiaBase(item)
        if (periciaBase == null) {
            Log.w("MestreIA_Forjador", "Perícia-base ausente para técnica '${def.nome}'")
            viewModel.adicionarQualidade("Técnica: ${def.nome} (perícia-base ausente)")
            return
        }

        val erro = viewModel.adicionarTecnica(def, periciaBase, item.nivel.coerceAtLeast(0))
        if (erro != null) {
            Log.w("MestreIA_Forjador", "Falha ao adicionar técnica ${def.nome}: $erro")
            viewModel.adicionarQualidade("Técnica: ${def.nome} ($erro)")
        } else {
            Log.d("MestreIA_Forjador", "Técnica adicionada: ${def.nome} sobre ${periciaBase.nome}")
        }
    }

    private fun encontrarPericiaBase(item: MestreIAItem): PericiaSelecionada? {
        val pericias = viewModel.personagem.pericias
        if (pericias.isEmpty()) return null
        val pid = item.periciaBaseId?.takeIf { it.isNotBlank() }
        if (pid != null) {
            pericias.firstOrNull {
                it.definicaoId == pid &&
                    (item.periciaBaseEspecializacao.isNullOrBlank() ||
                     it.especializacao.equals(item.periciaBaseEspecializacao, ignoreCase = true))
            }?.let { return it }
            pericias.firstOrNull { it.definicaoId == pid }?.let { return it }
        }
        // Fallback: perícia de combate com mais pontos investidos
        return pericias.maxByOrNull { it.pontosGastos }
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
