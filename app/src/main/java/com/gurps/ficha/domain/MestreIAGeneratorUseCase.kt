package com.gurps.ficha.domain

import android.content.Context
import android.util.Log
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.data.network.MestreIAItem
import com.gurps.ficha.data.network.MestreIAPromptsForjador
import com.gurps.ficha.data.network.MestreIAResponse
import com.gurps.ficha.data.network.MestreIATools
import com.gurps.ficha.domain.loaders.ForjadorTemplateCatalogo
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
    private val repository: DataRepository,
    private val context: Context? = null
) {
    /**
     * Erro REAL de infra (vindo do MestreIAClient), não texto da IA que
     * por acaso começa com "Erro". Antes, `startsWith("Erro")` confundia
     * uma análise legítima ("Erro comum em magos...") com falha de API e
     * disparava o fallback indevido. Casa só os padrões emitidos pelo
     * client: "Erro <código>:", "Erro de Conexão:", "Erro: Resposta
     * vazia...", "Erro: Modo Stream...", "Erro: Falha na conexão...".
     */
    private fun ehErroDeApi(texto: String): Boolean {
        val t = texto.trimStart()
        return Regex("^Erro \\d{3}:").containsMatchIn(t) ||
            t.startsWith("Erro de Conexão:") ||
            t.startsWith("Erro de API") ||
            t.startsWith("Erro: Resposta vazia") ||
            t.startsWith("Erro: Modo Stream") ||
            t.startsWith("Erro: Falha na conexão")
    }

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
        Log.i("MestreIA_Forjador", "Início ($modo): budget = $pontosIniciais pts | ficha atual gasta ${viewModel.personagem.pontosGastos} pts")
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

        // Template base: escolhe automaticamente o arquétipo mais próximo do pedido.
        // O bloco é injetado no sistema do loop principal (não sobrescreve o prompt do usuário).
        val templateBloco: String = if (modo != "analise" && context != null) {
            val templates = ForjadorTemplateCatalogo.carregar(context)
            val template = ForjadorTemplateCatalogo.escolher(prompt, templates)
            if (template != null) {
                Log.i("MestreIA_Forjador", "Template escolhido: ${template.nome} (id=${template.id})")
                "\n\n" + ForjadorTemplateCatalogo.formatarParaPrompt(template)
            } else {
                Log.i("MestreIA_Forjador", "Nenhum template correspondente para: ${prompt.take(80)}")
                ""
            }
        } else ""

        // Forjador: Gemini primário; fallback = DeepSeek V4 Pro.
        val fila = listOf(
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_LITE_1_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_KEY, com.gurps.ficha.BuildConfig.MESTRE_IA_GEMINI_3_FLASH),
            Triple(com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_URL, com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_KEY, com.gurps.ficha.BuildConfig.MESTRE_IA_DEEPSEEK_MODEL_V3)
        )

        // Lote E: executor de tools do Forjador Agêntico
        val nexusAdapter = NexusArcanoModoAlvoAdapter(repository.magias)
        val toolExecutor = ForjadorToolExecutor(viewModel, repository, nexusAdapter, context)

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
                        // B-completo (Lote 329): a criação agora APLICA incrementalmente na
                        // ficha viva (via forjador_editar_ficha), não no JSON final. Por isso,
                        // se já existe uma ficha na tela, ela seria contaminada pela criação
                        // nova. Regra do usuário: salvar a atual e abrir uma ficha LIMPA antes
                        // de criar do zero — sem perguntar. Roda só na 1ª config (não a cada
                        // fallback) para não re-salvar/re-zerar a cada tentativa.
                        if (config == fila.first()) {
                            val pAtual = viewModel.personagem
                            val fichaNaoVazia = pAtual.nome.isNotBlank() ||
                                pAtual.vantagens.isNotEmpty() || pAtual.desvantagens.isNotEmpty() ||
                                pAtual.pericias.isNotEmpty() || pAtual.magias.isNotEmpty() ||
                                pAtual.tecnicas.isNotEmpty() || pAtual.equipamentos.isNotEmpty() ||
                                pAtual.forcaBase != 10 || pAtual.destrezaBase != 10 ||
                                pAtual.inteligenciaBase != 10 || pAtual.vitalidadeBase != 10
                            if (fichaNaoVazia) {
                                onStatusUpdate("Guardando a ficha atual e abrindo uma nova...")
                                // Mesmo contexto em que o loop já chama viewModel (via
                                // toolExecutor.execute) — autoSaveIA/novaFicha lançam seus
                                // próprios viewModelScope internamente.
                                viewModel.autoSaveIA()   // salva a ficha atual (nome único IA_*)
                                viewModel.novaFicha()     // zera para criar do zero
                            }
                        }
                        // FASE 1 — HISTÓRIA (sem thinking, rápido ~1-3s)
                        // Aparece no chat enquanto o planning processa nos bastidores.
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
                            .takeIf { it.isNotBlank() && !ehErroDeApi(it) }
                            ?.trim()
                            .orEmpty()
                        if (historiaBase.isNotBlank()) onChunk(historiaBase)

                        // Aplica nome e história na ficha IMEDIATAMENTE pelo sistema —
                        // não depende do modelo chamar forjador_editar_ficha para isso.
                        if (historiaBase.isNotBlank()) {
                            // Extrai nome da primeira linha da história (antes do primeiro ponto/quebra)
                            val nomeExtraido = Regex(
                                """(?:^|\n)([A-ZÁÉÍÓÚÀÂÊÔÃÕÇ][a-záéíóúàâêôãõç]+(?: [A-ZÁÉÍÓÚÀÂÊÔÃÕÇ][a-záéíóúàâêôãõç]+){0,3})""",
                                RegexOption.MULTILINE
                            ).find(historiaBase)?.groupValues?.get(1)?.trim()
                                ?.takeIf { it.length in 3..40 }
                            if (!nomeExtraido.isNullOrBlank()) viewModel.atualizarNome(nomeExtraido)

                            // Separa aparência do histórico (linha "Aparência: ...")
                            val aparenciaMatch = Regex("""Aparência:\s*(.+)""", RegexOption.IGNORE_CASE)
                                .find(historiaBase)
                            val aparencia = aparenciaMatch?.groupValues?.get(1)?.trim().orEmpty()
                            val historico = if (aparenciaMatch != null)
                                historiaBase.substring(0, aparenciaMatch.range.first).trim()
                            else historiaBase.trim()

                            if (historico.isNotBlank()) viewModel.atualizarHistorico(historico)
                            if (aparencia.isNotBlank()) viewModel.atualizarAparencia(aparencia)
                            viewModel.autoSaveIA()
                            Log.d("MestreIA_Forjador", "Sistema aplicou: nome='$nomeExtraido' | histórico=${historico.length}chars | aparência=${aparencia.length}chars")
                        }

                        // Injeta a história no localHistory para guiar o loop de execução.
                        if (historiaBase.isNotBlank()) {
                            localHistory.add("model" to historiaBase)
                            localHistory.add("user" to "[SISTEMA — orquestração]\nEsta é a história DEFINITIVA do personagem. Nome e história já foram aplicados na ficha pelo sistema. NÃO aplique nome nem história — já estão na ficha.\n\nFASE DE PLANEJAMENTO OBRIGATÓRIA: antes de aplicar qualquer item, use forjador_ler_ficha('pontos') para ver o orçamento disponível. Depois decida internamente quanto de pontos cada pilar do conceito vai receber. Só então comece a construir, pilar por pilar, na ordem dos 9 pilares.")
                        }
                    }

                    onStatusUpdate(
                        if (ehConsultor) "Mestre $nomeModelo analisando a ficha..."
                        else "Mestre $nomeModelo forjando a ficha de $nomePersonagem..."
                    )
                    // PEDIDO ORIGINAL fixo. Reafirmado a cada iteração para
                    // o modelo NUNCA confundir instrução do sistema (ou
                    // sugestão dele mesmo) com o que o usuário pediu.
                    val pedidoUsuario = prompt
                    fun comAncora(instrucaoSistema: String): String =
                        "═══ ÚNICO PEDIDO REAL DO USUÁRIO (não invente outro, " +
                        "não trate suas próprias sugestões como aceitas) ═══\n" +
                        "\"$pedidoUsuario\"\n\n" +
                        "═══ INSTRUÇÃO INTERNA DO SISTEMA (orquestração — NÃO é " +
                        "fala do usuário) ═══\n$instrucaoSistema"
                    var promptAtual = pedidoUsuario
                    var response: MestreIAClient.ChatResponse? = null

                    // Parada por ESTAGNAÇÃO, não por contador. O loop continua
                    // enquanto o modelo chama ferramentas E progride. Vira
                    // final quando: para de chamar ferramentas (já existe o
                    // break), OU 2 iterações seguidas sem adicionar nada novo
                    // (anti-loop real), OU teto de segurança absoluto.
                    val ITER_HARD_CAP = 60
                    // Quantas iterações de PESQUISA-sem-aplicar são toleradas antes
                    // de forçar a síntese final. Criar ficha completa pesquisa muito.
                    val MAX_PESQUISA = 12
                    // "Fingerprint" do estado da ficha: muda a CADA edição real,
                    // não só ao adicionar item de lista. ANTES (bug Lote 329): só
                    // contava listas (magias/vantagens/...) — então iterações que
                    // só mexiam em ATRIBUTOS ou NOME (pilares 1-2, naturalmente as
                    // primeiras no modo incremental) eram lidas como "estagnação" e
                    // o loop morria em 2 iterações, antes de chegar às magias.
                    // Agora inclui pontosGastos (reflete atributos/secundários/
                    // vantagens/perícias) + nº de itens + tamanho dos textos.
                    fun totalItens() = viewModel.personagem.let {
                        it.pontosGastos +
                        (it.magias.size + it.vantagens.size + it.desvantagens.size +
                         it.pericias.size + it.tecnicas.size + it.equipamentos.size +
                         it.qualidades.size + it.peculiaridades.size) +
                        (if (it.nome.isNotBlank()) 1 else 0) +
                        (if (it.historico.isNotBlank()) 1 else 0) +
                        (if (it.aparencia.isNotBlank()) 1 else 0)
                    }
                    var iteracao = 0
                    var semProgresso = 0
                    var pesquisaSeguida = 0
                    while (iteracao < ITER_HARD_CAP) {
                        iteracao++
                        val itensAntes = totalItens()
                        // "final" = próxima já deve ser síntese: estagnou (2x
                        // sem progresso) ou bateu o teto duro de segurança.
                        val ultima = semProgresso >= 2 || iteracao >= ITER_HARD_CAP
                        // Passa todo o histórico real do chat — sem limite de mensagens.
                        // Filtra bolhas de sistema ([SISTEMA]) que são ruído para o modelo.
                        val histBase = viewModel.mestreIAChatHistory
                            .filter { !it.text.startsWith("[SISTEMA]") }
                            .map { it.role to it.text }
                        val histCompleto = histBase + localHistory

                        val iteracoesRestantes = ITER_HARD_CAP - iteracao
                        response = MestreIAClient.perguntarAoMestre(
                            baseUrl = config.first, apiKey = config.second, workspaceSlug = config.third,
                            prompt = promptAtual,
                            history = histCompleto,
                            contextoPersonagem = viewModel.personagem.toJson(),
                            catalogo = catalogoVazio,
                            modo = modo,
                            promptSistema = promptForjador + templateBloco + "\n[CONTEXTO DE SESSÃO] Iteração $iteracao de $ITER_HARD_CAP | Restam $iteracoesRestantes iterações.",
                            onChunk = null,
                            desativarTools = ultima,
                            maxTokens = if (ultima) 16384 else 16384
                        )

                        if (ehErroDeApi(response.text)) break

                        val forjadorCalls = response.toolCalls.filter { tc ->
                            tc.name == ForjadorTools.TOOL_LER_FICHA      ||
                            tc.name == ForjadorTools.TOOL_BUSCAR          ||
                            tc.name == ForjadorTools.TOOL_GPS_MAGIA       ||
                            tc.name == ForjadorTools.TOOL_EDITAR          ||
                            tc.name == ForjadorTools.TOOL_BUSCAR_RACAS    ||
                            tc.name == ForjadorTools.TOOL_APLICAR_RACIAL
                        }

                        if (forjadorCalls.isEmpty()) {
                            // Modelo parou de chamar ferramentas. Se ISTO já era a
                            // rodada final (tools desativadas), a resposta é o
                            // fechamento legítimo → sai. MAS se ele parou no meio
                            // (ainda com tools ligadas) e devolveu algo curto/interno
                            // (ex: "Dados coletados com sucesso") em vez de um
                            // fechamento de verdade, NÃO encerra com esse lixo: força
                            // UMA rodada final de SÍNTESE (tools off) para ele escrever
                            // a mensagem ao jogador. Bug observado no Lote 329: ficha
                            // ficava montada mas o chat exibia a mensagem interna.
                            val pareceFechamentoReal = ultima || response.text.trim().length > 300
                            if (pareceFechamentoReal) break
                            Log.d("MestreIA_Forjador", "Parou sem fechar (resp curta) → força síntese final it.$iteracao")
                            response = MestreIAClient.perguntarAoMestre(
                                baseUrl = config.first, apiKey = config.second, workspaceSlug = config.third,
                                prompt = comAncora(
                                    "[FECHAMENTO] A ficha já está montada na tela. NÃO chame ferramentas. " +
                                    "Escreva AGORA a mensagem final ao jogador: história/aparência (2-3 parágrafos) " +
                                    "+ resumo \"Atributos: X | Vantagens: Y | Desv: Z | Perícias: W | Total: V/MAX pts\" " +
                                    "(use os números reais do read-back)."),
                                history = histCompleto,
                                contextoPersonagem = viewModel.personagem.toJson(),
                                catalogo = catalogoVazio,
                                modo = modo,
                                promptSistema = promptForjador,
                                onChunk = null,
                                desativarTools = true,
                                maxTokens = 8192
                            )
                            break
                        }

                        val resultados = forjadorCalls.joinToString("\n\n") { tc ->
                            val alvo = tc.args.optString("alvo", "")
                                .ifBlank { tc.args.optString("magia_alvo", "") }
                                .ifBlank { tc.args.optString("query", "") }
                                .ifBlank { tc.args.optString("secao", "") }
                            val acao = when (tc.name) {
                                ForjadorTools.TOOL_EDITAR          -> "✏️ Aplicando: $alvo"
                                ForjadorTools.TOOL_GPS_MAGIA       -> "🧭 Calculando pré-requisitos: $alvo"
                                ForjadorTools.TOOL_BUSCAR          -> "🔎 Buscando no catálogo: $alvo"
                                ForjadorTools.TOOL_LER_FICHA       -> "📖 Lendo a ficha: $alvo"
                                ForjadorTools.TOOL_BUSCAR_RACAS    -> "🧬 Buscando raças/metacaracterísticas..."
                                ForjadorTools.TOOL_APLICAR_RACIAL  -> "🧬 Aplicando modelo racial: $alvo"
                                else -> tc.name.replace("forjador_", "")
                            }
                            onStatusUpdate("$acao  (passo $iteracao)")
                            // Iteração 1 (planejamento): bloqueia edição, só leitura/busca permitida.
                            // O modelo deve primeiro entender o orçamento antes de gastar.
                            if (!ehConsultor && iteracao == 1 && tc.name == ForjadorTools.TOOL_EDITAR) {
                                "=== ${tc.name} ===\n[PLANEJAMENTO] Ainda não aplique itens. Primeiro leia o orçamento disponível com forjador_ler_ficha('pontos'), pesquise os catálogos necessários e decida a distribuição de pontos entre os pilares. A edição começa na próxima iteração."
                            } else {
                                "=== ${tc.name} ===\n${toolExecutor.execute(tc)}"
                            }
                        }
                        Log.d("MestreIA_Forjador", "Iteração $iteracao: ${forjadorCalls.size} tool(s) → ${resultados.length} chars")

                        // READ-BACK: se houve edição, relê AUTOMATICAMENTE as
                        // seções tocadas (estado real pós-edição) e força a IA
                        // a verificar item por item — não pode mais "presumir"
                        // que aplicou. Mapeia atributos→ pontos juntos.
                        val edicoes = forjadorCalls.filter { it.name == ForjadorTools.TOOL_EDITAR }
                        var verificacao = ""
                        if (edicoes.isNotEmpty()) {
                            onStatusUpdate("Verificando as alterações na ficha...")
                            val secoes = edicoes.mapNotNull {
                                it.args.optString("secao", "").lowercase().trim().ifBlank { null }
                            }.toMutableSet()
                            if ("atributos" in secoes) secoes.add("pontos")
                            // Lote C: mudanças que afetam stats calculados → relê "derivados"
                            // (defesas/dano/carga) para o modelo conferir o efeito real.
                            if (secoes.any { it in setOf("atributos", "equipamentos", "pericias", "vantagens") }) {
                                secoes.add("derivados")
                            }
                            val leitura = secoes.joinToString("\n\n") { s ->
                                "--- $s (estado ATUAL na ficha) ---\n${toolExecutor.lerSecao(s)}"
                            }
                            verificacao = "\n\n=== VERIFICAÇÃO PÓS-EDIÇÃO (read-back automático) ===\n" +
                                "$leitura\n\n" +
                                "CONFIRA item por item na ficha relida acima:\n" +
                                "1) Cada alteração que você pediu CONSTA? Se alguma NÃO aplicou, " +
                                "CHAME forjador_editar_ficha de novo agora para resolver (NÃO pergunte ao " +
                                "usuário — corrija você mesmo, 1 tentativa).\n" +
                                "2) Há DUPLICATAS ou itens ÓRFÃOS resultantes desta edição? " +
                                "(ex: duas técnicas com mesmo nome; técnica cuja perícia-base sumiu — " +
                                "aparece como 'ÓRFÃ' no read-back). Se sim, CHAME forjador_editar_ficha " +
                                "('remover') para limpar AGORA, sem perguntar. Você causou, você corrige.\n" +
                                "3) Só depois de limpo, afirme ao usuário SOMENTE o que REALMENTE consta " +
                                "na ficha. Reporte com honestidade o que aplicou, o que corrigiu e o que " +
                                "(e por quê) não foi possível."
                            Log.d("MestreIA_Forjador", "Read-back: ${secoes.joinToString()} (${leitura.length} chars)")
                        }

                        // Estagnação: conta iterações consecutivas que
                        // chamaram ferramenta mas NÃO adicionaram item novo.
                        // Progresso zera o contador → cadeia longa continua
                        // o quanto precisar. 2x sem progresso → encerra.
                        // PROGRESSO = adicionou item OU pesquisou (GPS/buscar).
                        // Bug Lote 160: iteração só com forjador_gps_magia
                        // (descobrir a trilha) era contada como "estagnação"
                        // e o loop morria EXATAMENTE quando o GPS acabava de
                        // liberar o que adicionar. Pesquisar é trabalho real.
                        // Estagnação de verdade = iteração sem nada útil.
                        val pesquisou = forjadorCalls.any {
                            it.name == ForjadorTools.TOOL_GPS_MAGIA ||
                            it.name == ForjadorTools.TOOL_BUSCAR
                        }
                        // != e não >: aplicar DESVANTAGEM baixa pontosGastos (custo
                        // negativo) — também é progresso. Qualquer mudança no estado conta.
                        val progrediu = totalItens() != itensAntes
                        if (progrediu) {
                            // Mudou a ficha: zera tudo, cadeia avançando.
                            semProgresso = 0
                            pesquisaSeguida = 0
                            Log.d("MestreIA_Forjador", "Progresso (estado ${itensAntes}→${totalItens()}) it.$iteracao")
                        } else if (pesquisou) {
                            // Pesquisou mas não adicionou: trabalho válido,
                            // mas limita pesquisa-sem-aplicar p/ não loopar.
                            // Teto 12 (era 4): criar ficha completa precisa
                            // pesquisar MUITO (arco, sobrevivência, furtividade,
                            // rastreamento, naturalista, N vantagens...) ANTES de
                            // aplicar o 1º item; 4 empurrava à síntese cedo demais,
                            // antes do modelo ter os IDs e ainda sem ter aplicado.
                            semProgresso = 0
                            pesquisaSeguida++
                            Log.d("MestreIA_Forjador", "Pesquisa s/ aplicar ($pesquisaSeguida/$MAX_PESQUISA) it.$iteracao")
                        } else {
                            semProgresso++
                            Log.d("MestreIA_Forjador", "Sem avanço ($semProgresso/2) na iteração $iteracao")
                        }
                        val proximaEhFinal = semProgresso >= 2 ||
                            pesquisaSeguida >= MAX_PESQUISA || iteracao >= ITER_HARD_CAP - 1

                        localHistory.add("model" to "Dados coletados com sucesso.")
                        // Resultado de ferramenta = dado do SISTEMA, não fala
                        // do usuário. Rótulo explícito evita o modelo achar
                        // que o usuário "pediu/aceitou" o que está aqui.
                        localHistory.add("user" to "[SISTEMA — resultado de ferramenta, NÃO é mensagem do usuário]\n=== RESULTADO DAS FERRAMENTAS (iteração $iteracao) ===\n$resultados$verificacao")
                        val instrucao = if (proximaEhFinal) {
                            if (modo == "analise") {
                                "[RESPOSTA FINAL] NÃO chame mais ferramentas. Se o usuário tinha pedido para APLICAR algo e a cadeia ficou incompleta (ex: ainda falta a magia-alvo ou pré-requisitos), diga com clareza e honestidade O QUE JÁ FOI APLICADO e O QUE AINDA FALTA — NÃO finja que terminou. NÃO afirme que o usuário aceitou ou pediu algo que não está no PEDIDO REAL acima. Caso contrário, faça a análise + sugestões com IDs reais."
                            } else {
                                // B-completo (Lote 329): a ficha já foi montada INCREMENTALMENTE
                                // via forjador_editar_ficha durante o loop — não há JSON final.
                                // O fechamento é só a mensagem ao jogador (história + resumo real).
                                "[FECHAMENTO] A ficha já está montada na tela (você a construiu aplicando cada bloco). NÃO chame mais ferramentas e NÃO gere JSON. Escreva ao jogador uma mensagem final: a história/aparência do personagem (2-3 parágrafos imersivos, igual à definida no início) e um resumo dos pontos no formato \"Atributos: X | Vantagens: Y | Desv: Z | Perícias: W | Total: V/MAX pts\" (use os números REAIS que apareceram no read-back de pontos)."
                            }
                        } else {
                            // Aviso de orçamento: diz ao modelo quantas rodadas de
                            // PESQUISA ainda restam antes da síntese ser forçada, pra
                            // ele não deixar tudo pro fim e começar a APLICAR cedo.
                            val pesquisasRestantes = (MAX_PESQUISA - pesquisaSeguida).coerceAtLeast(0)
                            val avisoOrcamento = if (pesquisaSeguida > 0)
                                "\n\n⏳ ORÇAMENTO: você já fez $pesquisaSeguida rodada(s) de pesquisa SEM aplicar nada. " +
                                "Restam ~$pesquisasRestantes rodadas de pesquisa antes de eu FORÇAR a ficha final. " +
                                "NÃO deixe para aplicar tudo no fim: assim que tiver os IDs de um bloco (ex.: atributos, " +
                                "ou um grupo de perícias), CHAME forjador_editar_ficha JÁ para aplicá-lo, e só então " +
                                "pesquise o próximo bloco. Aplicar cedo protege o trabalho se a conexão cair."
                            else ""
                            "Continue executando ESTRITAMENTE o PEDIDO REAL do usuário acima — nada além dele. NÃO trate sugestões que você fez antes como aceitas; o usuário só pediu o que está em PEDIDO REAL. Siga os 9 pilares na ordem até a ficha estar completa, sem parar para perguntar.$avisoOrcamento"
                        }
                        promptAtual = comAncora(instrucao)
                        onStatusUpdate("Mestre $nomeModelo montando a cadeia (passo $iteracao)...")
                    }
                    sheetResponse = response
                }

                val finalResponse = sheetResponse ?: continue
                if (!ehErroDeApi(finalResponse.text)) {
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

    /**
     * Lote A+F: usa a FONTE DE VERDADE (Personagem.pontosGastos), que calcula o custo
     * completo e correto (atributos + secundários + vantagens + desvantagens + qualidades
     * + peculiaridades + perícias + técnicas + magias + racial). Chamado APÓS a integração
     * do JSON na ficha, então `viewModel.personagem` já reflete a ficha final.
     *
     * Antes (BUG-2) recalculava por estimativa grosseira — perícia como `nivel*2` (que NÃO
     * é o custo real, depende de dificuldade/atributo) e ignorava técnicas/secundários/racial.
     */
    fun validarBudget(ficha: MestreIAResponse): String? {
        val p = viewModel.personagem
        val total = p.pontosGastos
        val max = p.pontosTotaisDisponiveis
        return if (total > max) {
            Log.w("MestreIA_Forjador", "Budget excedido: $total pts (máximo: $max pts)")
            "⚠️ Ficha usa $total pts (máximo: $max pts)"
        } else null
    }

    fun integrarRespostaNaFicha(ficha: MestreIAResponse) {
        // Delta-safe: ao aplicar só um DELTA (Consultor), o JSON não traz
        // nome/historico/atributos — não pode apagar/zerar o que já existe.
        // Campos descritivos só sobrescrevem se vierem preenchidos.
        if (ficha.nome.isNotBlank())      viewModel.atualizarNome(ficha.nome)
        if (ficha.historico.isNotBlank()) viewModel.atualizarHistorico(ficha.historico)
        if (ficha.aparencia.isNotBlank()) viewModel.atualizarAparencia(ficha.aparencia)
        if (ficha.notas.isNotBlank())     viewModel.atualizarNotas(ficha.notas)
        if (ficha.pontosIniciais > 0)     viewModel.atualizarPontosIniciais(ficha.pontosIniciais)

        // Atributos só são aplicados se o JSON realmente os trouxe (≠ default
        // 10 vindo de objeto ausente). Num delta sem atributos, preserva os atuais.
        val temAtributos = ficha.atributos.st != 10 || ficha.atributos.dx != 10 ||
            ficha.atributos.iq != 10 || ficha.atributos.ht != 10 ||
            ficha.forca != null || ficha.destreza != null ||
            ficha.inteligencia != null || ficha.vitalidade != null ||
            ficha.st != null || ficha.dx != null || ficha.iq != null || ficha.ht != null
        if (temAtributos) {
            val attr = ficha.atributosEfetivos()
            viewModel.atualizarForca(attr.st)
            viewModel.atualizarDestreza(attr.dx)
            viewModel.atualizarInteligencia(attr.iq)
            viewModel.atualizarVitalidade(attr.ht)
        }

        // SUBSTITUIÇÃO DE SEÇÃO: a IA enviou a lista COMPLETA e final de
        // certas seções (ex: corrigir duplicatas → manda as 16 vantagens
        // certas). Zeramos essas seções ANTES de reaplicar — é o único
        // caminho de remoção/edição (o resto do fluxo só soma).
        val subs = ficha.substituir.map { it.lowercase().trim() }.toSet()
        fun limpar(secao: String, tamanho: () -> Int, remover: (Int) -> Unit) {
            if (secao !in subs) return
            for (i in tamanho() - 1 downTo 0) remover(i)
            Log.d("MestreIA_Forjador", "Seção '$secao' substituída (zerada antes de reaplicar)")
        }
        limpar("vantagens",     { viewModel.personagem.vantagens.size })     { viewModel.removerVantagem(it) }
        limpar("desvantagens",  { viewModel.personagem.desvantagens.size })  { viewModel.removerDesvantagem(it) }
        limpar("pericias",      { viewModel.personagem.pericias.size })      { viewModel.removerPericia(it) }
        limpar("tecnicas",      { viewModel.personagem.tecnicas.size })      { viewModel.removerTecnica(it) }
        limpar("magias",        { viewModel.personagem.magias.size })        { viewModel.removerMagia(it) }
        limpar("equipamentos",  { viewModel.personagem.equipamentos.size })  { viewModel.removerEquipamento(it) }
        limpar("qualidades",    { viewModel.personagem.qualidades.size })    { viewModel.removerQualidade(it) }
        limpar("peculiaridades",{ viewModel.personagem.peculiaridades.size }){ viewModel.removerPeculiaridade(it) }

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
