package com.gurps.ficha.domain.tools

import android.content.Context
import android.util.Log
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.domain.loaders.MetacaracteristicaCatalogo
import com.gurps.ficha.domain.loaders.RacaCatalogo
import com.gurps.ficha.domain.loaders.RacaDefinicao
import com.gurps.ficha.domain.magias.NexusArcanoModoAlvoAdapter
import com.gurps.ficha.domain.engine.MagicEngine
import com.gurps.ficha.viewmodel.FichaViewModel
import org.json.JSONObject

class ForjadorToolExecutor(
    private val viewModel: FichaViewModel,
    private val repository: DataRepository,
    private val nexusAdapter: NexusArcanoModoAlvoAdapter,
    private val context: Context? = null
) {
    // Catálogos de raças/metacaracterísticas carregados lazily (context pode ser null em testes)
    private val catalogoRacas: List<RacaDefinicao> by lazy {
        context?.let { RacaCatalogo.carregar(it) } ?: emptyList()
    }
    private val catalogoMetas: List<RacaDefinicao> by lazy {
        context?.let { MetacaracteristicaCatalogo.carregarBruto(it) } ?: emptyList()
    }

    fun execute(toolCall: MestreIAClient.MestreIAToolCall): String {
        Log.d("Forjador_Tools", "Executando tool: ${toolCall.name} | args: ${toolCall.args}")
        val resultado = when (toolCall.name) {
            ForjadorTools.TOOL_LER_FICHA         -> lerFicha(toolCall.args)
            ForjadorTools.TOOL_BUSCAR            -> buscarCatalogo(toolCall.args)
            ForjadorTools.TOOL_GPS_MAGIA         -> gpsMagia(toolCall.args)
            ForjadorTools.TOOL_EDITAR            -> editarFicha(toolCall.args)
            ForjadorTools.TOOL_BUSCAR_RACAS      -> buscarRacas(toolCall.args)
            ForjadorTools.TOOL_APLICAR_RACIAL    -> aplicarModeloRacial(toolCall.args)
            else -> """{"erro": "ferramenta desconhecida: ${toolCall.name}"}"""
        }
        // Loga o RESULTADO (não só a entrada) — auditoria real no logcat.
        Log.d("Forjador_Tools", "Resultado ${toolCall.name}: ${resultado.take(300)}")
        return resultado
    }

    /** Read-back: relê uma seção pelo nome (reusa lerFicha). Usado na verificação pós-edição. */
    fun lerSecao(secao: String): String = lerFicha(JSONObject().put("secao", secao))

    /**
     * Lote G: prepara a descrição oficial do catálogo para o modelo — fonte real em vez de
     * ele citar de memória (chutava "B43" sendo a pág. 82). Corrige mojibake (ï¿½/Ã) e trunca.
     */
    private fun descricaoFonte(raw: String?): String? {
        val t = raw?.trim().orEmpty()
        if (t.isBlank()) return null
        // Remove o caractere de substituição (�) que aparece no mojibake dos JSONs e colapsa espaços.
        val limpo = t.replace("�", "").replace(Regex("\\s+"), " ").trim()
        return limpo.take(320).let { if (limpo.length > 320) "$it…" else it }
    }

    /**
     * Mostra ao modelo as opções de custo de uma vantagem/desvantagem de ESCALA,
     * para ele ESCOLHER conscientemente (via valor="custo=N" ou "nivel=N") em vez
     * de pegar sempre o custo-base. Fichas humanas usam muito custoEscolhido —
     * Riqueza (10/20/30/50), Status/Reputação (por nível), Mestre de Armas (variável).
     * Retorna null para FIXO (não há o que escolher). NÃO inventa valores: usa os
     * que o catálogo define — o app valida o que for aplicado.
     */
    private fun opcoesCustoTexto(
        tipo: com.gurps.ficha.model.TipoCusto,
        opcoes: List<Int>,
        custoPorNivel: Int
    ): String? = when (tipo) {
        com.gurps.ficha.model.TipoCusto.ESCOLHA ->
            if (opcoes.size > 1) " | opções de custo: ${opcoes.joinToString("/")} (passe valor=\"custo=N\")" else null
        com.gurps.ficha.model.TipoCusto.POR_NIVEL ->
            " | $custoPorNivel pts/nível (passe valor=\"nivel=N\")"
        com.gurps.ficha.model.TipoCusto.VARIAVEL ->
            " | custo variável (passe valor=\"custo=N\" conforme o conceito)"
        com.gurps.ficha.model.TipoCusto.FIXO -> null
    }

    private fun lerFicha(args: JSONObject): String {
        val p = viewModel.personagem
        val secao = args.optString("secao", "atributos")
        return when (secao) {
            "atributos" -> buildString {
                if (p.nome.isNotBlank()) appendLine("Nome: ${p.nome}")
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
            // Lote C: stats DERIVADOS (o que a ficha CALCULOU, não o que foi inserido).
            // Permite o Forjador conferir o resultado real e auto-corrigir (ex: "a Esquiva
            // ficou 8, baixa demais"). Tudo vem das propriedades calculadas de Personagem/
            // DefesasAtivas — NÃO recalcular aqui. "combate" é alias de "derivados".
            "derivados", "combate", "secundarios", "secundários" -> {
                val def = p.defesasAtivas
                val esquiva = def.calcularEsquiva(p)
                val apara = def.calcularApara(p)
                val periciaApara = def.getPericiaApara(p)
                val bloqueio = def.calcularBloqueio(p)
                buildString {
                    appendLine("=== ATRIBUTOS PRIMÁRIOS ===")
                    appendLine("ST ${p.st} | DX ${p.dx} | IQ ${p.iq} | HT ${p.ht}")
                    appendLine("=== SECUNDÁRIOS (calculados) ===")
                    appendLine("PV ${p.pontosVida} | PF ${p.pontosFadiga} | Vontade ${p.vontade} | Percepção ${p.percepcao}")
                    appendLine("Vel. Básica ${"%.2f".format(p.velocidadeBasica)} | Deslocamento ${p.deslocamentoBasico}")
                    appendLine("Dano: GdP ${p.danoGdP} | GeB ${p.danoGeB}")
                    appendLine("=== DEFESAS ATIVAS (calculadas) ===")
                    append("Esquiva $esquiva")
                    if (apara != null) append(" | Apara $apara${periciaApara?.let { " (${it.nome})" } ?: ""}")
                    if (bloqueio != null) append(" | Bloqueio $bloqueio")
                    appendLine()
                    appendLine("=== CARGA ===")
                    val cargaLabel = when (p.nivelCarga) {
                        0 -> "Nenhuma"; 1 -> "Leve"; 2 -> "Média"; 3 -> "Pesada"; 4 -> "Extra-pesada"; else -> "${p.nivelCarga}"
                    }
                    appendLine("Peso equipado ${"%.1f".format(p.pesoTotalEquipamentos)}kg | Carga base ${"%.1f".format(p.baseCarga)}kg | Nível $cargaLabel | Deslocamento c/ carga ${p.deslocamentoAtual}")
                    appendLine("=== MAGIA ===")
                    appendLine("Aptidão Mágica: ${viewModel.nivelAptidaoMagica}")
                }.trim()
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
                // Lote A+F: fonte de VERDADE = Personagem.pontosGastos (calcula TUDO:
                // atributos + secundários + vantagens + desvantagens + qualidades +
                // peculiaridades + perícias + técnicas + magias + modelo racial).
                // NÃO recalcular aqui — o número do app é a verdade.
                buildString {
                    appendLine("Pontos gastos: ${p.pontosGastos} / ${p.pontosTotaisDisponiveis} disponíveis | Livres: ${p.pontosRestantes}")
                    appendLine("Quebra: atributos ${p.pontosAtributos} | secundários ${p.pontosSecundarios} | " +
                        "vantagens ${p.pontosVantagens} | desvantagens ${p.pontosDesvantagens} | " +
                        "perícias ${p.pontosPericias} | técnicas ${p.pontosTecnicas} | magias ${p.pontosMagias} | " +
                        "qualidades ${p.pontosQualidades} | peculiaridades ${p.pontosPeculiaridades}" +
                        if (p.modeloRacial.custoTotal != 0) " | racial ${p.modeloRacial.custoTotal}" else "")
                    // Limite de desvantagens (GURPS): aviso se excedeu.
                    if (p.desvantagensExcedemLimite) {
                        appendLine("⚠ LIMITE DE DESVANTAGENS EXCEDIDO: ${p.pontosDesvantagens} pts (limite ${p.limiteDesvantagens}).")
                    }
                }.trim()
            }
            else -> """{"erro": "seção inválida: $secao. Use: atributos, derivados, vantagens, desvantagens, pericias, tecnicas, magias, equipamentos, qualidades, peculiaridades, pontos"}"""
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
                    val schema = RegrasEspeciaisSchema.para(v.specialRule, v.id)
                    buildString {
                        append("• ${v.id} | ${v.nome} | ${v.getCustoBase()} pts | tipoCusto:${v.tipoCusto}")
                        // Escala de custo: mostra as opções/custo-por-nível para o modelo
                        // ESCOLHER conscientemente (valor="custo=20" ou "nivel=N").
                        opcoesCustoTexto(v.tipoCusto, v.getOpcoesEscolha(), v.getCustoPorNivel())?.let { append(it) }
                        if (mods.isNotBlank()) append(" | modificadores: $mods")
                        if (schema != null) append("\n   ⚙ REGRA ESPECIAL — $schema")
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
                    val schema = RegrasEspeciaisSchema.para(d.specialRule, d.id)
                    buildString {
                        append("• ${d.id} | ${d.nome} | ${d.getCustoBase()} pts | tipoCusto:${d.tipoCusto}")
                        opcoesCustoTexto(d.tipoCusto, d.getOpcoesEscolha(), d.getCustoPorNivel())?.let { append(it) }
                        if (d.pagina > 0) append(" | pág.${d.pagina}")
                        if (mods.isNotBlank()) append(" | modificadores: $mods")
                        descricaoFonte(d.descricao)?.let { append("\n   📖 $it") }
                        if (schema != null) append("\n   ⚙ REGRA ESPECIAL — $schema")
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
                else resultados.joinToString("\n") { p ->
                    // Lote G: descrição da perícia vem do mapa de regras v2 (pericias_v2_rules_map.json).
                    val regra = repository.regraPericiaV2(p.id)
                    buildString {
                        append("• ${p.id} | ${p.nome}")
                        descricaoFonte(regra?.descricao)?.let { append("\n   📖 $it") }
                    }
                }
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
                    buildString {
                        append("• ${m.id} | ${m.nome} | escola:${m.escola?.joinToString() ?: "?"}")
                        m.pagina?.takeIf { it > 0 }?.let { append(" | pág.$it") }
                        append(" | $status | pré:${m.preRequisitos?.take(60) ?: "—"}")
                        descricaoFonte(m.descricao)?.let { append("\n   📖 $it") }
                    }
                }
            }
            "tecnica" -> {
                val resultados = repository.tecnicasCatalogo.filter {
                    it.nome.lowercase().contains(query) || it.id.lowercase().contains(query)
                }.take(10)
                if (resultados.isEmpty()) "Nenhuma técnica encontrada para '$query'."
                else resultados.joinToString("\n") { t ->
                    // Lote D: VEREDITO determinístico de pré-requisito (análogo ao GPS de magia).
                    // Técnica precisa de uma perícia-base na ficha que atenda o pré-requisito.
                    // O app é o juiz (tecnicaAtendePreRequisito) — o modelo NÃO calcula de cabeça.
                    val pp = viewModel.personagem
                    val baseOk = pp.pericias.firstOrNull { per -> viewModel.tecnicaAtendePreRequisito(t, per) }
                    val status = if (baseOk != null) "✓ PODE ADICIONAR (base: ${baseOk.nome})"
                                 else "⚠ FALTA perícia-base compatível na ficha"
                    buildString {
                        append("• ${t.id} | ${t.nome} | dif:${t.dificuldadeRaw}")
                        if (t.pagina != null && t.pagina > 0) append(" | pág.${t.pagina}")
                        append(" | $status | pré:${t.preRequisitoRaw.take(50)}")
                        descricaoFonte(t.descricao)?.let { append("\n   📖 $it") }
                    }
                }
            }
            // Lote B: equipamento de catálogo — stats REAIS (dano/ST/peso/custo), em vez de
            // o modelo inventar números no JSON. O dano vem como "GdP+2 corte" e a ficha
            // resolve por ST automaticamente ao adicionar (Equipamento.danoCalculadoComSt).
            "arma" -> {
                val resultados = repository.armasCatalogo.filter {
                    it.nome.lowercase().contains(query) || it.id.lowercase().contains(query) ||
                    it.grupo.lowercase().contains(query) || it.categoria.lowercase().contains(query)
                }.take(12)
                if (resultados.isEmpty()) "Nenhuma arma encontrada para '$query'."
                else resultados.joinToString("\n") { a ->
                    "• ${a.id} | ${a.nome} | ${a.tipoCombate} | dano:${a.danoRaw} | ST mín:${a.stMinimo ?: "—"}" +
                    " | grupo:${a.grupo} | ${a.pesoBaseKg ?: "?"}kg | ${a.custoBase ?: "?"}\$" +
                    (a.aparar?.let { " | aparar:$it" } ?: "")
                }
            }
            "armadura" -> {
                val resultados = repository.armadurasCatalogo.filter {
                    it.nome.lowercase().contains(query) || it.id.lowercase().contains(query) ||
                    it.local.lowercase().contains(query)
                }.take(12)
                if (resultados.isEmpty()) "Nenhuma armadura encontrada para '$query'."
                else resultados.joinToString("\n") { a ->
                    "• ${a.id} | ${a.nome} | RD:${a.rd} | local:${a.local}" +
                    " | ${a.pesoBaseKg ?: "?"}kg | ${a.custoBase ?: "?"}\$" + (a.nt?.let { " | NT$it" } ?: "")
                }
            }
            "escudo" -> {
                val resultados = repository.escudosCatalogo.filter {
                    it.nome.lowercase().contains(query) || it.id.lowercase().contains(query)
                }.take(12)
                if (resultados.isEmpty()) "Nenhum escudo encontrado para '$query'."
                else resultados.joinToString("\n") { e ->
                    "• ${e.id} | ${e.nome} | BD:${e.db} | ST mín:${e.stMinimo ?: "—"}" +
                    " | ${e.pesoKg ?: "?"}kg | ${e.custo ?: "?"}\$" + (e.nt?.let { " | NT$it" } ?: "")
                }
            }
            else -> """{"erro": "tipo inválido: $tipo. Use: vantagem, desvantagem, pericia, magia, tecnica, arma, armadura, escudo"}"""
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
                // Lote 329: o sistema resolve a cadeia sozinho. NÃO mande o modelo
                // adicionar pré-requisitos um a um (era o que o tornava lento).
                else -> appendLine("VEREDITO: ⚙ '$alvoNome' tem pré-requisitos faltando ($faltaPrereq), MAS você NÃO precisa adicioná-los um a um: basta chamar forjador_editar_ficha adicionar magias \"$alvoId\" — o sistema adiciona AUTOMATICAMENTE toda a cadeia de pré-requisitos, na ordem certa, junto com o alvo.")
            }
            // A trilha abaixo é só INFORMATIVA (o que será adicionado junto). Você
            // NÃO precisa aplicar item por item — peça o ALVO e o sistema resolve.
            if (!jaTem && !aprendivel && snapshot.trilhaOtimaIds.isNotEmpty()) {
                appendLine("Pré-requisitos que serão adicionados junto (automático, nesta ordem):")
                snapshot.trilhaOtimaIds.filter { it != alvoId }.forEachIndexed { i, id ->
                    val nome = repository.magias.find { it.id == id }?.nome ?: id
                    appendLine("  ${i + 1}. $id ($nome)")
                }
                appendLine("  → e então o alvo: $alvoId ($alvoNome)")
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

        // ATRIBUTOS + CAMPOS ESPECIAIS (nome, historia, PF, pontosIniciais)
        if (secao == "atributos" || secao == "atributo" || secao == "personagem") {
            // Campos de texto: nome e historia
            when {
                alvoN == "nome" || alvoN == "name" -> {
                    viewModel.atualizarNome(valor)
                    viewModel.autoSaveIA()
                    return "OK: nome do personagem alterado para '$valor'."
                }
                alvoN == "historia" || alvoN == "historico" || alvoN == "background" || alvoN == "antecedentes" -> {
                    viewModel.atualizarHistorico(valor)
                    viewModel.autoSaveIA()
                    return "OK: história do personagem atualizada."
                }
                alvoN == "aparencia" || alvoN == "aparência" || alvoN == "appearance" || alvoN == "descricaofisica" -> {
                    viewModel.atualizarAparencia(valor)
                    viewModel.autoSaveIA()
                    return "OK: aparência do personagem atualizada."
                }
                alvoN == "notas" || alvoN == "notes" || alvoN == "observacoes" || alvoN == "anotacoes" -> {
                    viewModel.atualizarNotas(valor)
                    viewModel.autoSaveIA()
                    return "OK: notas do personagem atualizadas."
                }
                alvoN == "pontosIniciais" || alvoN == "pontosiniciais" || alvoN == "pontos_iniciais" || alvoN == "budget" -> {
                    val novo = Regex("\\d+").find(valor)?.value?.toIntOrNull()
                        ?: return """{"erro":"valor numérico ausente para pontosIniciais"}"""
                    viewModel.atualizarPontosIniciais(novo)
                    viewModel.autoSaveIA()
                    return "OK: pontosIniciais alterado para $novo."
                }
            }
            // PF / Fadiga: mod incremental (não valor absoluto)
            if (alvoN == "pf" || alvoN == "fadiga" || alvoN == "pontosfadiga" || alvoN == "pontos_fadiga") {
                val mod = Regex("-?\\d+").find(valor)?.value?.toIntOrNull()
                    ?: return """{"erro":"valor numérico ausente para PF (ex: valor=\"2\" para +2 de fadiga)"}"""
                viewModel.atualizarModPontosFadiga(mod)
                viewModel.autoSaveIA()
                return "OK: modificador de PF alterado para $mod (PF base = HT + $mod)."
            }
            // Lote E: atributos SECUNDÁRIOS via modificador incremental (não absoluto).
            // O valor é o mod sobre a base (ex: PV=HT+mod). Custo entra automático em
            // Personagem.pontosSecundarios. Velocidade aceita decimal (passos de 0.25).
            run {
                val modInt: Int? = Regex("-?\\d+").find(valor)?.value?.toIntOrNull()
                when {
                    alvoN == "pv" || alvoN == "pontosvida" || alvoN == "pontos_vida" -> {
                        val m = modInt ?: return """{"erro":"valor numérico ausente para PV"}"""
                        viewModel.atualizarModPontosVida(m); viewModel.autoSaveIA()
                        return "OK: modificador de PV = $m (PV base = ST + $m)."
                    }
                    alvoN == "vontade" || alvoN == "von" || alvoN == "will" -> {
                        val m = modInt ?: return """{"erro":"valor numérico ausente para Vontade"}"""
                        viewModel.atualizarModVontade(m); viewModel.autoSaveIA()
                        return "OK: modificador de Vontade = $m (Vontade = IQ + $m)."
                    }
                    alvoN == "percepcao" || alvoN == "per" || alvoN == "percepção" -> {
                        val m = modInt ?: return """{"erro":"valor numérico ausente para Percepção"}"""
                        viewModel.atualizarModPercepcao(m); viewModel.autoSaveIA()
                        return "OK: modificador de Percepção = $m (Percepção = IQ + $m)."
                    }
                    alvoN == "deslocamento" || alvoN == "desloc" || alvoN == "movimento" -> {
                        val m = modInt ?: return """{"erro":"valor numérico ausente para Deslocamento"}"""
                        viewModel.atualizarModDeslocamentoBasico(m); viewModel.autoSaveIA()
                        return "OK: modificador de Deslocamento = $m."
                    }
                    alvoN == "velocidade" || alvoN == "velocidadebasica" || alvoN == "velocidade_basica" -> {
                        val mf = Regex("-?\\d+(?:[.,]\\d+)?").find(valor)?.value?.replace(",", ".")?.toFloatOrNull()
                            ?: return """{"erro":"valor numérico ausente para Velocidade (ex: 0.25)"}"""
                        viewModel.atualizarModVelocidadeBasica(mf); viewModel.autoSaveIA()
                        return "OK: modificador de Velocidade Básica = $mf (custo: 5 pts por 0.25)."
                    }
                }
            }
            // Atributos primários numéricos: ST/DX/IQ/HT
            val novo = (Regex("-?\\d+").find(valor)?.value
                ?: Regex("-?\\d+").find(alvo)?.value)?.toIntOrNull()
                ?: return """{"erro":"atributo/campo desconhecido ou valor ausente: '$alvo'. Use ST/DX/IQ/HT, nome, historia, aparencia, notas, PF ou pontosIniciais"}"""
            val antes: Int
            when {
                alvoN.startsWith("for") || alvoN == "st" -> { antes = p.forca;       viewModel.atualizarForca(novo) }
                alvoN.startsWith("des") || alvoN == "dx" -> { antes = p.destreza;    viewModel.atualizarDestreza(novo) }
                alvoN.startsWith("int") || alvoN.startsWith("iq") || alvoN.startsWith("ig") -> { antes = p.inteligencia; viewModel.atualizarInteligencia(novo) }
                alvoN.startsWith("vit") || alvoN.startsWith("ht") || alvoN.startsWith("sau") -> { antes = p.vitalidade; viewModel.atualizarVitalidade(novo) }
                else -> return """{"erro":"atributo desconhecido: '$alvo'. Use ST/DX/IQ/HT, nome, historia, PF ou pontosIniciais"}"""
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
                // RESPEITA A SEÇÃO PEDIDA. Alguns ids existem nas DUAS listas
                // (ex: "riqueza" é escala única: Falido/Pobre são desvantagem,
                // Confortável/Rico são vantagem). Antes o código preferia SEMPRE
                // a vantagem → pedir "riqueza" em desvantagens aplicava +10 (rico)
                // em vez de negativo (pobre). Agora: se o jogador escolheu a seção
                // "desvantagens", usa a desvantagem; em "vantagens", usa a vantagem.
                // Só cai na outra lista se o id não existir na seção pedida.
                val aplicado = when {
                    secao == "desvantagens" && d != null -> {
                        viewModel.adicionarDesvantagem(d, nivel = nivel,
                            custo = if (custo != 0) custo else d.getCustoBase()); "desvantagem"
                    }
                    secao == "vantagens" && v != null -> {
                        viewModel.adicionarVantagem(v, nivel = nivel,
                            custo = if (custo != 0) custo else v.getCustoBase()); "vantagem"
                    }
                    v != null -> {
                        viewModel.adicionarVantagem(v, nivel = nivel,
                            custo = if (custo != 0) custo else v.getCustoBase()); "vantagem"
                    }
                    d != null -> {
                        viewModel.adicionarDesvantagem(d, nivel = nivel,
                            custo = if (custo != 0) custo else d.getCustoBase()); "desvantagem"
                    }
                    else -> return "Não encontrado no catálogo: '$alvo'."
                }
                viewModel.autoSaveIA()
                // Informa o custo REAL aplicado: se vier sinal trocado do esperado
                // (ex: pediu desvantagem mas só existe como vantagem +10), o modelo
                // vê no retorno e no read-back e pode corrigir. Relê da ficha (estado
                // pós-edição) pelo mesmo alvo.
                val pAtual = viewModel.personagem
                val custoReal: Int? = if (aplicado == "vantagem")
                    pAtual.vantagens.lastOrNull { norm(it.definicaoId) == alvoN || norm(it.nome) == alvoN }?.custoFinal
                else
                    pAtual.desvantagens.lastOrNull { norm(it.definicaoId) == alvoN || norm(it.nome) == alvoN }?.custoFinal
                return "OK: '$alvo' aplicada como $aplicado" +
                    (custoReal?.let { " ($it pts)" } ?: "") + "."
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
                val forcar = Regex("forcar\\s*=\\s*(true|sim|1)", RegexOption.IGNORE_CASE).containsMatchIn(valor)
                val faltaPrereq = viewModel.prereqFailureForMagia(mag)

                // CADEIA AUTOMÁTICA (Lote 329): se faltam pré-requisitos e o modelo
                // NÃO pediu para forçar, o SISTEMA adiciona a cadeia inteira na ordem
                // correta — em vez de BLOQUEAR e fazer o modelo tatear 1 magia por
                // iteração (lento: o Raspha gastou 5 iterações p/ 1 magia). O motor
                // Nexus já calcula a trilha ótima (trilhaOtimaIds), a mesma do GPS.
                // Não recalculamos regra aqui: cada magia da trilha é aplicada com a
                // validação normal, na ordem que o motor garante ser aprendível.
                if (faltaPrereq != null && !forcar) {
                    val p0 = viewModel.personagem
                    val trilha = nexusAdapter.calcular(
                        mag.id, p0.magias.map { it.definicaoId }.toSet(),
                        p0.iq, p0.dx, viewModel.nivelAptidaoMagica
                    ).trilhaOtimaIds.filter { it != mag.id }

                    if (trilha.isEmpty()) {
                        // Sem trilha calculável (motor não achou caminho) → mantém o
                        // bloqueio honesto para o modelo resolver/forçar conscientemente.
                        return "BLOQUEADO: '${mag.nome}' não pode ser adicionada — $faltaPrereq. " +
                            "Use forjador_gps_magia(\"${mag.id}\") para ver a cadeia e adicione as " +
                            "pré-requisito antes. Só com valor=\"forcar=true\" para gatilho narrativo."
                    }

                    val gastoAntes = viewModel.personagem.pontosGastos
                    val aplicadas = mutableListOf<String>()
                    val falhas = mutableListOf<String>()
                    // Adiciona cada magia-base na ordem; só prossegue enquanto o passo
                    // anterior libera o próximo (validação real a cada uma).
                    for (id in trilha) {
                        val base = repository.magias.find { it.id == id } ?: continue
                        if (viewModel.personagem.magias.any { it.definicaoId == id }) continue // já tem
                        if (viewModel.prereqFailureForMagia(base) != null) {
                            falhas.add(base.nome); break
                        }
                        if (viewModel.adicionarMagia(base, pts = 1) == null) aplicadas.add(base.nome)
                        else { falhas.add(base.nome); break }
                    }
                    // Por fim, o alvo (agora com a cadeia satisfeita).
                    val erroAlvo = if (viewModel.prereqFailureForMagia(mag) == null)
                        viewModel.adicionarMagia(mag, pts = if (custo > 0) custo else 1) else "pré-requisito ainda pendente"
                    viewModel.autoSaveIA()
                    // AVISO TRANSPARENTE: quantas magias e quantos pontos a cadeia
                    // consumiu (fonte de verdade = pontosGastos da ficha, não soma de
                    // cabeça). Cadeias longas (Desejo ~16) gastam muito — é a regra do
                    // GURPS, mas o modelo precisa VER o impacto no orçamento.
                    val pAtual = viewModel.personagem
                    val gastoNaCadeia = pAtual.pontosGastos - gastoAntes
                    val totalMagias = aplicadas.size + (if (erroAlvo == null) 1 else 0)
                    return buildString {
                        if (erroAlvo == null) {
                            append("OK: '${mag.nome}' + ${aplicadas.size} pré-requisito(s) = $totalMagias magia(s) adicionada(s)")
                            append(", consumindo $gastoNaCadeia pts. Total da ficha agora: ${pAtual.pontosGastos}/${pAtual.pontosTotaisDisponiveis} (livres: ${pAtual.pontosRestantes}).")
                            if (aplicadas.isNotEmpty()) append(" Cadeia (na ordem): ${aplicadas.joinToString(", ")}.")
                        } else {
                            append("PARCIAL: adicionadas ${aplicadas.joinToString(", ").ifBlank { "nenhuma" }} ($gastoNaCadeia pts), ")
                            append("mas '${mag.nome}' ainda não entrou ($erroAlvo).")
                        }
                        if (falhas.isNotEmpty()) append(" Falhou em: ${falhas.joinToString(", ")}.")
                    }
                }

                val erro = viewModel.adicionarMagia(mag, pts = if (custo > 0) custo else 1, ignora = forcar)
                viewModel.autoSaveIA()
                val sufixoForca = if (faltaPrereq != null && forcar) " (ADIÇÃO FORÇADA, sem pré-requisito — gatilho narrativo)" else ""
                return if (erro == null) "OK: magia '${mag.nome}' ${if (substituiu) "atualizada" else "adicionada"}$sufixoForca."
                       else "Falha ao adicionar magia '${mag.nome}': $erro"
            }
            "qualidades"     -> { viewModel.adicionarQualidade(alvo); viewModel.autoSaveIA(); return "OK: qualidade adicionada." }
            "peculiaridades" -> { viewModel.adicionarPeculiaridade(alvo); viewModel.autoSaveIA(); return "OK: peculiaridade adicionada." }
            // Lote B: adicionar equipamento DO CATÁLOGO (stats reais). Procura em armas →
            // escudos → armaduras pelo id/nome. Usa os métodos do ViewModel que já preenchem
            // dano/ST/peso/custo/grupo corretos (dano resolve por ST na exibição).
            "equipamentos" -> {
                val arma = repository.armasCatalogo.find { norm(it.id) == alvoN || norm(it.nome) == alvoN }
                if (arma != null) {
                    viewModel.adicionarEquipamentoArma(arma); viewModel.autoSaveIA()
                    return "OK: arma '${arma.nome}' adicionada do catálogo (dano ${arma.danoRaw}, ST mín ${arma.stMinimo ?: "—"})."
                }
                val escudo = repository.escudosCatalogo.find { norm(it.id) == alvoN || norm(it.nome) == alvoN }
                if (escudo != null) {
                    viewModel.adicionarEquipamentoEscudo(escudo); viewModel.autoSaveIA()
                    return "OK: escudo '${escudo.nome}' adicionado do catálogo (BD ${escudo.db})."
                }
                val armadura = repository.armadurasCatalogo.find { norm(it.id) == alvoN || norm(it.nome) == alvoN }
                if (armadura != null) {
                    viewModel.adicionarEquipamentoArmadura(armadura); viewModel.autoSaveIA()
                    return "OK: armadura '${armadura.nome}' adicionada do catálogo (RD ${armadura.rd})."
                }
                // Não está no catálogo: cria item genérico (texto livre) com o nome dado.
                viewModel.adicionarEquipamento(com.gurps.ficha.model.Equipamento(nome = alvo))
                viewModel.autoSaveIA()
                return "OK: equipamento genérico '$alvo' adicionado (não estava no catálogo — sem stats). " +
                    "Para item com dano/RD reais, use forjador_buscar_catalogo(tipo=arma|armadura|escudo) e adicione pelo id."
            }
            else -> return """{"erro":"$op em $secao não suportado por esta ferramenta"}"""
        }
    }

    private fun buscarRacas(args: JSONObject): String {
        val query = args.optString("query", "").lowercase().trim()
        val tipo = args.optString("tipo", "todos").lowercase().trim()

        val racasFiltradas = if (tipo != "meta" && tipo != "metacaracteristica") {
            catalogoRacas.filter {
                query.isBlank() || it.nome.lowercase().contains(query) || it.id.lowercase().contains(query)
            }
        } else emptyList()

        val metasFiltradas = if (tipo != "raca") {
            catalogoMetas.filter {
                query.isBlank() || it.nome.lowercase().contains(query) || it.id.lowercase().contains(query)
            }
        } else emptyList()

        if (racasFiltradas.isEmpty() && metasFiltradas.isEmpty()) {
            return if (context == null) "Erro: contexto Android não disponível para carregar catálogos."
            else "Nenhuma raça/metacaracterística encontrada para '$query'."
        }

        return buildString {
            if (racasFiltradas.isNotEmpty()) {
                appendLine("=== RAÇAS (${racasFiltradas.size}) ===")
                racasFiltradas.forEach { r ->
                    append("• [raca] id=${r.id} | ${r.nome}")
                    if (r.pagina.isNotBlank()) append(" | pág.${r.pagina}")
                    if (r.descricao.isNotBlank()) append(" | ${r.descricao.take(80)}")
                    appendLine()
                }
            }
            if (metasFiltradas.isNotEmpty()) {
                appendLine("=== METACARACTERÍSTICAS (${metasFiltradas.size}) ===")
                metasFiltradas.forEach { m ->
                    append("• [meta] id=${m.id} | ${m.nome}")
                    if (m.pagina.isNotBlank()) append(" | pág.${m.pagina}")
                    if (m.descricao.isNotBlank()) append(" | ${m.descricao.take(80)}")
                    appendLine()
                }
            }
            appendLine("Para aplicar, use aplicarModeloRacial(id=..., tipo=raca|meta).")
        }.trim()
    }

    private fun aplicarModeloRacial(args: JSONObject): String {
        if (context == null) return """{"erro":"contexto Android não disponível"}"""
        val id = args.optString("id", "").trim()
        val tipo = args.optString("tipo", "raca").lowercase().trim()
        if (id.isBlank()) return """{"erro":"id é obrigatório"}"""

        fun norm(s: String) = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "").lowercase().replace(Regex("[^a-z0-9]"), "").trim()
        val idN = norm(id)

        val isMeta = tipo == "meta" || tipo == "metacaracteristica"
        val catalogo = if (isMeta) catalogoMetas else catalogoRacas
        val def = catalogo.firstOrNull { norm(it.id) == idN || norm(it.nome) == idN }
            ?: return """{"erro":"${if (isMeta) "Metacaracterística" else "Raça"} '$id' não encontrada no catálogo. Use buscarRacas para ver os disponíveis."}"""

        val resolucao = RacaCatalogo.resolver(def, repository)
        val modelo = if (isMeta) {
            resolucao.modelo.copy(tipo = com.gurps.ficha.model.TipoModeloRacial.METACARACTERISTICA)
        } else {
            resolucao.modelo
        }

        viewModel.atualizarModeloRacial(modelo)
        viewModel.autoSaveIA()

        val avisos = if (resolucao.naoResolvidos.isNotEmpty())
            " (avisos: ${resolucao.naoResolvidos.joinToString(", ")})"
        else ""
        return "OK: modelo racial '${def.nome}' aplicado ao personagem$avisos. " +
            "Vantagens: ${modelo.vantagens.size}, Desvantagens: ${modelo.desvantagens.size}, " +
            "Perícias: ${modelo.pericias.size}. Use lerFicha(secao=atributos) para confirmar."
    }

}
