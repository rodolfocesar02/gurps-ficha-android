package com.gurps.ficha.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.*
import com.gurps.ficha.regras_prerequisitos.ConditionStatus
import com.gurps.ficha.regras_prerequisitos.PreRequisitoParser
import com.gurps.ficha.regras_prerequisitos.PreRequisitoChecker
import com.gurps.ficha.regras_prerequisitos.PreRequisitoType
import com.gurps.ficha.domain.filters.CatalogFilters
import com.gurps.ficha.domain.loaders.sanitized
import com.gurps.ficha.domain.loaders.fixMojibakeIfNeeded
import java.text.Normalizer

/**
 * Repositorio para carregar dados de Vantagens, Desvantagens, Pericias e Magias
 * a partir dos arquivos JSON em assets/
 */
private val gson = Gson()

data class MestreIaTema(
    val id: String,
    val canonical: String,
    val keywords: List<String>
)

data class MestreIaTemasWrapper(
    val temas: List<MestreIaTema>
)

open class DataRepository(internal val context: Context) {
    private data class MagiaFiltroIndex(
        val definicao: MagiaDefinicao,
        val nomeNorm: String,
        val classeNorm: String,
        val escolasNorm: Set<String>
    )


    private var _vantagens: List<VantagemDefinicao>? = null

    private var _desvantagens: List<DesvantagemDefinicao>? = null
    private var _pericias: List<PericiaDefinicao>? = null
    private var _periciasV2Rules: Map<String, PericiaV2RuleMapItem>? = null
    private var _periciasSuplementares: List<PericiaSuplementarItem>? = null
    private var _magias: List<MagiaDefinicao>? = null
    private var _magiasFiltroIndex: List<MagiaFiltroIndex>? = null
    private var _tecnicasCatalogo: List<TecnicaCatalogoItem>? = null
    private var _armasCatalogo: List<ArmaCatalogoItem>? = null
    private var _escudosCatalogo: List<EscudoCatalogoItem>? = null
    private var _armadurasCatalogo: List<ArmaduraCatalogoItem>? = null
    private var _modificadoresGerais: List<ModificadorDefinicao>? = null
    private var _temasMestreIA: List<MestreIaTema>? = null

    open val vantagens: List<VantagemDefinicao>
        get() = _vantagens ?: carregarVantagens().also { _vantagens = it }

    open val desvantagens: List<DesvantagemDefinicao>
        get() = _desvantagens ?: carregarDesvantagens().also { _desvantagens = it }

    open val pericias: List<PericiaDefinicao>
        get() = _pericias ?: carregarPericias().also { _pericias = it }

    val periciasV2Rules: Map<String, PericiaV2RuleMapItem>
        get() = _periciasV2Rules ?: carregarPericiasV2Rules().also { _periciasV2Rules = it }

    val periciasSuplementares: List<PericiaSuplementarItem>
        get() = _periciasSuplementares ?: carregarPericiasSuplementares().also { _periciasSuplementares = it }

    private val magiasFiltroIndex: List<MagiaFiltroIndex>
        get() = _magiasFiltroIndex ?: magias.map { magia ->
            MagiaFiltroIndex(
                definicao = magia,
                nomeNorm = CatalogFilters.normalizarBusca(magia.nome),
                classeNorm = CatalogFilters.normalizarBusca(agruparClasseMagia(magia.classe).orEmpty()),
                escolasNorm = magia.escola
                    .orEmpty()
                    .asSequence()
                    .map { CatalogFilters.normalizarBusca(it) }
                    .filter { it.isNotBlank() }
                    .toSet()
            )
        }.also { _magiasFiltroIndex = it }

    val tecnicasCatalogo: List<TecnicaCatalogoItem>
        get() = _tecnicasCatalogo ?: carregarTecnicasCatalogo().also { _tecnicasCatalogo = it }

    val armasCatalogo: List<ArmaCatalogoItem>
        get() = _armasCatalogo ?: carregarArmasCatalogo().also { _armasCatalogo = it }

    val escudosCatalogo: List<EscudoCatalogoItem>
        get() = _escudosCatalogo ?: carregarEscudosCatalogo().also { _escudosCatalogo = it }

    val armadurasCatalogo: List<ArmaduraCatalogoItem>
        get() = _armadurasCatalogo ?: carregarArmadurasCatalogo().also { _armadurasCatalogo = it }

    val modificadoresGerais: List<ModificadorDefinicao>
        get() = _modificadoresGerais ?: carregarModificadoresGerais().also { _modificadoresGerais = it }

    open val magias: List<MagiaDefinicao>
        get() = _magias ?: carregarMagias().also { _magias = it }

    open val temasMestreIA: List<MestreIaTema>
        get() = _temasMestreIA ?: carregarTemasMestreIA().also { _temasMestreIA = it }

    private val catalogLoaders = com.gurps.ficha.domain.loaders.CatalogLoaders(context)
    private val database by lazy { com.gurps.ficha.data.storage.FichaDatabase.getInstance(context) }
    private val graphNodeDao by lazy { database.graphNodeDao() }
    private val manualChunkDao by lazy { database.manualChunkDao() }

    fun getCatalogLoadErrors(): Map<String, String> {
        return synchronized(catalogLoaders.loadErrors) { catalogLoaders.loadErrors.toMap() }
    }

    private fun carregarVantagens(): List<VantagemDefinicao> = catalogLoaders.carregarVantagens()
    private fun carregarDesvantagens(): List<DesvantagemDefinicao> = catalogLoaders.carregarDesvantagens()
    private fun carregarPericias(): List<PericiaDefinicao> = catalogLoaders.carregarPericias()
    private fun carregarPericiasV2Rules(): Map<String, PericiaV2RuleMapItem> = catalogLoaders.carregarPericiasV2Rules()
    private fun carregarPericiasSuplementares(): List<PericiaSuplementarItem> = catalogLoaders.carregarPericiasSuplementares()
    private fun carregarMagias(): List<MagiaDefinicao> = catalogLoaders.carregarMagias()
    private fun carregarTecnicasCatalogo(): List<TecnicaCatalogoItem> = catalogLoaders.carregarTecnicasCatalogo()
    private fun carregarArmasCatalogo(): List<ArmaCatalogoItem> = catalogLoaders.carregarArmasCatalogo()
    private fun carregarEscudosCatalogo(): List<EscudoCatalogoItem> = catalogLoaders.carregarEscudosCatalogo()
    private fun carregarArmadurasCatalogo(): List<ArmaduraCatalogoItem> = catalogLoaders.carregarArmadurasCatalogo()
    private fun carregarModificadoresGerais(): List<ModificadorDefinicao> = catalogLoaders.carregarModificadoresGerais()

    private fun carregarTemasMestreIA(): List<MestreIaTema> {
        return try {
            val json = context.assets.open("mestre_ia_temas.json").bufferedReader().use { it.readText() }
            gson.fromJson(json, MestreIaTemasWrapper::class.java).temas
        } catch (e: Exception) {
            emptyList()
        }
    }

    // === FILTROS DE VANTAGENS E OUTROS (Delegados para CatalogFilters) ===

    fun filtrarVantagens(busca: String = "", tipoCusto: TipoCusto? = null, tag: String? = null, somenteArtesMarciais: Boolean = false): List<VantagemDefinicao> {
        return CatalogFilters.filtrarVantagens(vantagens, catalogLoaders.vantagensArtesMarciaisIds, busca, tipoCusto, tag, somenteArtesMarciais)
    }

    fun filtrarDesvantagens(busca: String = "", tipoCusto: TipoCusto? = null, tag: String? = null): List<DesvantagemDefinicao> {
        return CatalogFilters.filtrarDesvantagens(desvantagens, busca, tipoCusto, tag)
    }

    fun filtrarPericias(busca: String = "", atributoBase: String? = null, dificuldade: String? = null): List<PericiaDefinicao> {
        return CatalogFilters.filtrarPericias(pericias, busca, atributoBase, dificuldade)
    }

    fun filtrarMagias(busca: String = "", escola: String? = null, classe: String? = null): List<MagiaDefinicao> {
        if (busca.isBlank() && escola.isNullOrBlank() && classe.isNullOrBlank()) return magias

        val buscaNorm = CatalogFilters.normalizarBusca(busca)
        val escolaNorm = escola?.let(CatalogFilters::normalizarBusca).orEmpty()
        val classeNorm = classe?.let(CatalogFilters::normalizarBusca).orEmpty()
        return magiasFiltroIndex.asSequence()
            .filter { idx ->
                val matchBusca = buscaNorm.isBlank() || idx.nomeNorm.contains(buscaNorm)
                val matchEscola = escolaNorm.isBlank() || escolaNorm in idx.escolasNorm
                val matchClasse = classeNorm.isBlank() || idx.classeNorm.contains(classeNorm)
                matchBusca && matchEscola && matchClasse
            }
            .map { it.definicao }
            .toList()
    }

    // --- MÉTODOS PARA GRAPHRAG (LOTE 62) ---

    /**
     * Busca nos resumos de comunidades e entidades do Grafo.
     */
    suspend fun buscarResumosGrafo(query: String): List<com.gurps.ficha.data.storage.GraphNodeEntity> {
        return graphNodeDao.searchGraph(query)
    }

    /**
     * Busca nos recortes manuais brutos (FTS4).
     */
    suspend fun buscarRecortesManual(query: String): List<com.gurps.ficha.model.MestreIAChunk> {
        return manualChunkDao.buscarRegras(query, 10).map { entity ->
            com.gurps.ficha.model.MestreIAChunk(
                source_title = entity.source_title,
                page_number = entity.page_number,
                text = entity.text
            )
        }
    }

    fun filtrarTecnicasCatalogo(busca: String = "", sourceBook: String? = null): List<TecnicaCatalogoItem> {
        return CatalogFilters.filtrarTecnicasCatalogo(tecnicasCatalogo, busca, sourceBook)
    }

    fun filtrarArmasCatalogo(busca: String = "", tipoCombate: String? = null, stMaximo: Int? = null): List<ArmaCatalogoItem> {
        return CatalogFilters.filtrarArmasCatalogo(armasCatalogo, busca, tipoCombate, stMaximo)
    }

    fun filtrarEscudosCatalogo(busca: String = "", stMaximo: Int? = null): List<EscudoCatalogoItem> {
        return CatalogFilters.filtrarEscudosCatalogo(escudosCatalogo, busca, stMaximo)
    }

    fun filtrarArmadurasCatalogo(busca: String = "", nt: Int? = null, localFiltro: String? = null, tagFiltro: String? = null): List<ArmaduraCatalogoItem> {
        return CatalogFilters.filtrarArmadurasCatalogo(armadurasCatalogo, busca, nt, localFiltro, tagFiltro)
    }

    fun agruparClasseMagia(classe: String?): String? {
        val normalizada = classe
            ?.trim()
            ?.replace(Regex("\\s+"), " ")
            ?.ifBlank { null }
            ?: return null

        val chave = normalizarChaveClasse(normalizada)
        if (chave.startsWith("bloq.") || chave.startsWith("bloqueio")) return "Bloqueio"
        if (chave.startsWith("com.") || chave.startsWith("comm") || chave.startsWith("comum")) return "Comum"
        if (chave.startsWith("encant")) return "Encantamento"
        if (chave.startsWith("especial")) return "Especial"
        if (chave.startsWith("informacao")) return "Informação"
        if (chave.startsWith("projetil")) return "Projétil"
        if (chave.startsWith("toque")) return "Toque"
        if (chave.startsWith("area")) return "Área"
        if (chave in CHAVES_BLOQUEIO) return "Bloqueio"
        if (chave in CHAVES_COMUM) return "Comum"
        if (chave in CHAVES_ENCANTAMENTO) return "Encantamento"
        if (chave in CHAVES_ESPECIAL) return "Especial"
        if (chave in CHAVES_INFORMACAO) return "Informação"
        if (chave in CHAVES_PROJETIL) return "Projétil"
        if (chave in CHAVES_TOQUE) return "Toque"
        if (chave in CHAVES_AREA) return "Área"
        return normalizada
    }

    // === CONVERSORES PARA SELECAO ===

    fun criarVantagemSelecionada(
        definicao: VantagemDefinicao,
        nivel: Int = 1,
        custoEscolhido: Int = 0,
        descricao: String = "",
        modificadores: List<ModificadorSelecao> = emptyList(),
        metadados: Map<String, String>? = null
    ): VantagemSelecionada {
        // custoBase é sempre o custo unitário (por nível) ou o custo fixo inicial
        val custoBase = if (definicao.tipoCusto == TipoCusto.POR_NIVEL) {
            definicao.getCustoPorNivel()
        } else {
            definicao.getCustoBase()
        }

        return VantagemSelecionada(
            definicaoId = definicao.id,
            nome = definicao.nome,
            custoBase = custoBase,
            nivel = nivel,
            // custoEscolhido só é relevante para ESCOLHA ou VARIAVEL
            custoEscolhido = if (custoEscolhido != 0) custoEscolhido else custoBase,
            descricao = descricao,
            tipoCusto = definicao.tipoCusto,
            pagina = definicao.pagina,
            modificadores = modificadores.toMutableList(),
            metadados = metadados
        )
    }

    fun criarDesvantagemSelecionada(
        definicao: DesvantagemDefinicao,
        nivel: Int = 1,
        custoEscolhido: Int = 0,
        descricao: String = "",
        autocontrole: Int? = null,
        modificadores: List<ModificadorSelecao> = emptyList(),
        metadados: Map<String, String>? = null
    ): DesvantagemSelecionada {
        val custoBase = if (definicao.tipoCusto == TipoCusto.POR_NIVEL) {
            definicao.getCustoPorNivel()
        } else {
            definicao.getCustoBase()
        }

        return DesvantagemSelecionada(
            definicaoId = definicao.id,
            nome = definicao.nome,
            custoBase = custoBase,
            nivel = nivel,
            custoEscolhido = if (custoEscolhido != 0) custoEscolhido else custoBase,
            descricao = descricao,
            autocontrole = autocontrole,
            tipoCusto = definicao.tipoCusto,
            pagina = definicao.pagina,
            specialRule = definicao.specialRule,
            modificadores = modificadores.toMutableList(),
            metadados = metadados
        )
    }

    fun criarPericiaSelecionada(
        definicao: PericiaDefinicao,
        pontosGastos: Int = 1,
        especializacao: String = "",
        atributoEscolhido: AtributoBase? = null,
        dificuldadeEscolhida: Dificuldade? = null
    ): PericiaSelecionada {
        val atributo = atributoEscolhido
            ?: AtributoBase.fromSigla(definicao.atributoBase)
        val dificuldade = dificuldadeEscolhida
            ?: Dificuldade.fromSigla(definicao.dificuldadeFixa)

        return PericiaSelecionada(
            definicaoId = definicao.id,
            nome = definicao.nome,
            atributoBase = atributo,
            dificuldade = dificuldade,
            pontosGastos = pontosGastos,
            especializacao = especializacao,
            exigeEspecializacao = definicao.exigeEspecializacao
        )
    }

    fun validarPreRequisitosPericia(definicao: PericiaDefinicao, personagem: Personagem): String? {
        val regra = periciasV2Rules[definicao.id] ?: return null
        if (regra.preRequisito.allowWithoutPrerequisite) return null

        val raw = regra.preRequisito.raw
        if (raw.isBlank() || isSemPreRequisitoRaw(raw)) return null

        val parsed = PreRequisitoParser.parse(raw)
        if (parsed.bypassValidation || parsed.terms.isEmpty()) return null

        val mapa = buildPreReqContext(personagem)
        val report = PreRequisitoChecker.checkParseResult(mapa, parsed)

        return if (report.contains("faltando")) report.removePrefix("faltando:").trim() else null
    }

    fun validarPreRequisitosPericiaDetailed(definicaoId: String, personagem: Personagem): List<ConditionStatus> {
        val regra = periciasV2Rules[definicaoId] ?: return emptyList()
        val raw = regra.preRequisito.raw
        if (raw.isBlank() || isSemPreRequisitoRaw(raw)) return emptyList()

        val parsed = PreRequisitoParser.parse(raw)
        val mapa = buildPreReqContext(personagem)
        return PreRequisitoChecker.checkDetailed(mapa, parsed)
    }

    fun regraPericiaV2(id: String): PericiaV2RuleMapItem? = periciasV2Rules[id]

    fun criarTecnicaSelecionada(
        definicao: TecnicaCatalogoItem,
        periciaBase: PericiaSelecionada,
        nivelRelativoPredefinido: Int = 0
    ): TecnicaSelecionada {
        val nivelNormalizado = nivelRelativoPredefinido.coerceAtLeast(0)
        val dificuldade = tecnicaDificuldade(definicao.dificuldadeRaw)
        val custo = calcularCustoTecnica(dificuldade, nivelNormalizado)
        val predefMod = extrairModificadorPredefinido(definicao.preDefinidoRaw)
        val limiteMaximo = extrairLimiteMaximoRelativo(definicao.preRequisitoRaw, predefMod)
        return TecnicaSelecionada(
            definicaoId = definicao.id,
            nome = definicao.nome,
            pontosGastos = custo,
            nivelRelativoPredefinido = nivelNormalizado,
            periciaBaseDefinicaoId = periciaBase.definicaoId,
            periciaBaseNome = periciaBase.nome,
            periciaBaseEspecializacao = periciaBase.especializacao,
            preDefinidoModificador = predefMod,
            limiteMaximoRelativo = limiteMaximo,
            dificuldadeRaw = definicao.dificuldadeRaw,
            preDefinidoRaw = definicao.preDefinidoRaw,
            preRequisitoRaw = definicao.preRequisitoRaw,
            sourceBook = definicao.sourceBook
        )
    }

    fun criarMagiaSelecionada(
        definicao: MagiaDefinicao,
        pontosGastos: Int = 1,
        encantamentoAlvo: String? = null,
        especializacaoMagia: String? = null
    ): MagiaSelecionada {
        val pontosNormalizados = pontosGastos.coerceAtLeast(1)
        return MagiaSelecionada(
            definicaoId = definicao.id,
            nome = definicao.nome,
            dificuldade = Dificuldade.fromSigla(definicao.dificuldadeFixa),
            pontosGastos = pontosNormalizados,
            pagina = definicao.pagina,
            texto = definicao.texto ?: "",
            classe = definicao.classe,
            escola = definicao.escola,
            duracao = definicao.duracao,
            energia = definicao.energia,
            tempoOperacao = definicao.tempoOperacao,
            encantamentoAlvo = encantamentoAlvo?.trim()?.takeIf { it.isNotBlank() },
            especializacaoMagia = especializacaoMagia?.trim()?.takeIf { it.isNotBlank() }
        )
    }

    /**
     * Valida pré-requisitos de uma magia usando o parser/checar simples.
     * Retorna `null` se não há problemas ou a string bruta em caso de falha.
     *
     * A implementação é deliberadamente simples: gera um mapa de atributos, nível
     * de aptidão mágica, contagem de magias por escola e conjunto de magias
     * conhecidas, e delega para [PreRequisitoChecker.checkSimples].
     *
     * O retorno igual ao raw facilita exibir a condição original ao usuário—
     * o código de UI (ViewModel) não precisa entender a gramática.
     */
    fun validarPreRequisitosMagia(definicao: MagiaDefinicao, personagem: Personagem): String? {
        val raw = preRequisitoRawNormalizado(definicao)
        if (isSemPreRequisitoRaw(raw)) return null

        val parsed = PreRequisitoParser.parse(raw)
        if (parsed.bypassValidation || parsed.terms.isEmpty()) return null

        val mapa = buildPreReqContext(personagem)
        val report = PreRequisitoChecker.checkParseResult(mapa, parsed)
        if (isPrerequisitoTratadoComoSemRequisito(raw, parsed, report)) return null
        return if (report.startsWith("faltando")) raw else null
    }

    /**
     * Retorna um texto descritivo dos pré‑requisitos faltantes para a magia ou
     * `null` se todos forem atendidos. A string resultante não inclui o prefixo
     * "faltando:" que [PreRequisitoChecker.checkSimples] produz.
     */
    fun missingPreRequisitoReport(definicao: MagiaDefinicao, personagem: Personagem): String? {
        val raw = preRequisitoRawNormalizado(definicao)
        if (isSemPreRequisitoRaw(raw)) return null

        val parsed = PreRequisitoParser.parse(raw)
        if (parsed.bypassValidation || parsed.terms.isEmpty()) return null

        val mapa = buildPreReqContext(personagem)
        val report = PreRequisitoChecker.checkParseResult(mapa, parsed)
        if (isPrerequisitoTratadoComoSemRequisito(raw, parsed, report)) return null
        if (!report.startsWith("faltando")) return null
        return report.removePrefix("faltando:").trim().takeIf { it.isNotBlank() }
    }

    fun preRequisitoNormalizadoParaAnalise(definicao: MagiaDefinicao): String {
        return preRequisitoRawNormalizado(definicao)
    }

    fun magiaSemPreRequisito(definicao: MagiaDefinicao): Boolean {
        return isSemPreRequisitoRaw(preRequisitoRawNormalizado(definicao))
    }

    /**
     * Correções pontuais de textos conhecidos com mojibake/ambiguidade para
     * evitar quebrar validação automática enquanto preserva a regra funcional.
     */
    private fun preRequisitoRawNormalizado(definicao: MagiaDefinicao): String {
        return preRequisitoCanonicoTexto(definicao.id, definicao.preRequisitos)
    }

    fun preRequisitoCanonicoPorId(magiaId: String): String {
        val definicao = getMagiaPorId(magiaId) ?: return ""
        return preRequisitoRawNormalizado(definicao)
    }

    private fun isSemPreRequisitoRaw(raw: String): Boolean {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return true
        if (trimmed == "-" || trimmed == "—" || trimmed == "–" || trimmed == "−") return true
        if (trimmed == "—" || trimmed == "–" || trimmed == "âˆ’") return true
        if (trimmed == "?" || trimmed == "??" || trimmed == "???") return true

        val normalizado = normalizarNomeRequisito(trimmed)
        if (normalizado.isBlank()) return true
        return normalizado in setOf(
            "nenhum",
            "nenhuma",
            "na",
            "n a",
            "nao ha",
            "sem prerequisito",
            "sem prerequisitos"
        )
    }

    private val preRequisitosOverridePorMagiaId: Map<String, String> = mapOf(
        "acelerar_tempo" to "Aptidao Magica 2, IQ 13+, 2 magicas em dez escolas diferentes",
        "conceder_magica" to "Aptidao Magica 1, Conceder Pericia, 2 magicas em seis escolas diferentes",
        "convocacao_planar" to "Aptidao Magica 1 e 1 magica em dez escolas diferentes",
        "convocar_demonio" to "Aptidao Magica 1 e 1 magica em dez escolas diferentes",
        "desejo" to "Pequeno Desejo e 1 magica em quinze escolas diferentes",
        "encantar" to "Aptidao Magica 2, 1 magica em dez outras escolas diferentes",
        "expulsar" to "Aptidao Magica 1 e 1 magica em dez escolas diferentes",
        "extrair_corrente_eletricant" to "Roubar Corrente Eletrica e 2 magicas em dez escolas diferentes",
        "imunidade_a_encantamento" to "Encantar",
        "localizar_portal" to "Aptidao Magica 2, Localizar Magica e 1 magica em dez escolas diferentes",
        "criar_portal" to "Controle de Portal ou Teleporte ou Viagem no Tempo ou Trocar de Plano",
        "metamorfose_superior" to "AM3, Alterar Corpo, quaisquer 4 magicas Metamorfose, 10 outras magicas",
        "restauracao" to "Cura Profunda ou 2 entre Aliviar Paralisia e magicas de Restaurar",
        "geiser" to "6 magicas da Agua, incl. Criar Nascente, quaisquer 4 magicas da Terra ou Fogo",
        "espirito_de_caveira" to "4 magicas de Necromancia",
        "armadura_de_relampagos" to "6 magicas de Relampagos, incl. Imunidade a Relampagos",
        "condicionamento_permanente" to "AM3, 15 magicas de Controle da Mente, incl. Condicionamento",
        "controle_de_membro" to "AM1, 5 magicas de Corpo, incl. Espasmo",
        "adivinhacao" to "Historia",
        "anular_possessao" to "Passageiro da Alma e Possessao",
        "convocar_elemental" to "Aptidao Magica 1 e 8 magicas da escola apropriada",
        "controle_de_elemental" to "Convocar Elemental para a escola apropriada",
        "criar_elemental" to "AM2, Controle de Elemental",
        "corpo_de_vento" to "Aptidao Magica 3, Corpo de Ar e Furacao, cada um com NH 16 ou superior, 1 magica em cinco escolas diferentes",
        "cavalgar" to "Pelo menos uma magia de Controle de Animal",
        "controle_de_hibrido" to "2 magicas de Controle de Animal",
        "espantar_zumbi" to "Zumbi",
        "golem" to "Encantar, Moldar Terra e Animacao",
        "maldicao" to "Aptidao Magica 2 e 2 magicas em dez escolas diferentes",
        "passageiro_interno" to "2 magicas de Controle de Animal diferentes",
        "reconstruirnt" to "Aptidao Magica 3, Consertar, Criar Objeto e 3 magicas de cada escola Ar, Fogo, Terra e Agua",
        "repelir_animal" to "Controle de Animal",
        "suspender" to "Aptidao Magica 2 e 2 magicas em dez escolas diferentes",
        "transformar_objeto" to "Aptidao Magica 2, Remodelar e 4 magicas de Criar",
        "transformar_outro" to "Metamorfosear Outro e Transformar Corpo",
        "talisma" to "Encantar"
    )

    fun preRequisitoCanonicoTexto(magiaId: String, raw: String?): String {
        val rawOriginal = raw
            ?.fixMojibakeIfNeeded()
            ?.trim()
            .orEmpty()
        val override = preRequisitosOverridePorMagiaId[magiaId]
        return (override ?: rawOriginal)
            .fixMojibakeIfNeeded()
            .trim()
    }

    /**
     * Regra operacional pedida: se o texto de pre-requisito estiver em um
     * formato ainda quebrado/ambíguo para o parser, libera adição da magia
     * como se fosse sem pre-requisito.
     */
    private fun isPrerequisitoTratadoComoSemRequisito(
        raw: String,
        parsed: PreRequisitoParser.ParseResult,
        report: String
    ): Boolean {
        if (!report.startsWith("faltando")) return false

        val normalized = normalizarNomeRequisito(raw)
        if (
            normalized.contains("todos os pre requisitos") ||
            normalized.contains("qualquer encantamento de limitacao")
        ) return true

        val suspiciousFreeText = parsed.tipos
            .filterIsInstance<com.gurps.ficha.regras_prerequisitos.PreRequisitoType.MagiaConhecida>()
            .map { normalizarNomeRequisito(it.nomeMagia) }
            .any { token ->
                token.contains("quaisquer") ||
                    token.contains("qualquer uma") ||
                    token.contains("outras magicas") ||
                    token.contains("nao pode ser") ||
                    token.contains("incl")
            }

        return suspiciousFreeText
    }

    private fun buildPreReqContext(personagem: Personagem): MutableMap<String, Any> {
        val mapa = mutableMapOf<String, Any>()
        mapa["ST"] = personagem.st
        mapa["DX"] = personagem.dx
        mapa["IQ"] = personagem.iq
        mapa["HT"] = personagem.ht

        val nivelAptidaoMagica = personagem.vantagens
            .filter { it.definicaoId.equals("aptidao_magica", ignoreCase = true) }
            .maxOfOrNull { (it.nivel - 1).coerceAtLeast(0) }
            ?: 0
        mapa["aptidao_magica"] = nivelAptidaoMagica

        val magiasConhecidas = mutableSetOf<String>()
        val magiasConhecidasNormalizadas = mutableSetOf<String>()
        val escolasPorMagiaNormalizadas = mutableMapOf<String, MutableSet<String>>()
        val magiasPorEscolaNormalizada = mutableMapOf<String, Int>()
        personagem.magias.forEach { selecionada ->
            val nome = selecionada.nome
            magiasConhecidas.add(nome)
            magiasConhecidasNormalizadas.add(normalizarNomeRequisito(nome))
            val magiaKey = normalizarNomeRequisito(nome)
            val escolasNormalizadas = escolasPorMagiaNormalizadas.getOrPut(magiaKey) { mutableSetOf() }
            selecionada.escola.orEmpty().forEach { escola ->
                val escolaKey = normalizarNomeRequisito(escola)
                escolasNormalizadas.add(escolaKey)
                val countKey = "magias_$escolaKey"
                mapa[countKey] = (mapa[countKey] as? Int ?: 0) + 1
                magiasPorEscolaNormalizada[escolaKey] = (magiasPorEscolaNormalizada[escolaKey] ?: 0) + 1
            }
        }
        mapa["magias_conhecidas"] = magiasConhecidas
        mapa["magias_conhecidas_normalizadas"] = magiasConhecidasNormalizadas
        mapa["magias_por_escola_normalizada"] = magiasPorEscolaNormalizada.toMap()
        mapa["escolas_conhecidas_normalizadas"] = magiasPorEscolaNormalizada.keys.toSet()
        mapa["escolas_por_magia_normalizadas"] = escolasPorMagiaNormalizadas.mapValues { it.value.toSet() }

        val vantagensConhecidasNormalizadas = personagem.vantagens
            .map { v -> normalizarNomeRequisito(v.nome) }
            .filter { it.isNotBlank() }
            .toSet()
        mapa["vantagens_conhecidas_normalizadas"] = vantagensConhecidasNormalizadas

        val periciasConhecidasNormalizadas = mutableSetOf<String>()
        val periciasNiveisNormalizadas = mutableMapOf<String, Int>()
        personagem.pericias.forEach { p ->
            val nomeNorm = normalizarNomeRequisito(p.nome)
            if (nomeNorm.isNotBlank()) {
                val nivel = p.calcularNivel(personagem)
                // Mapeamento triplo por segurança: Original, Normalizado e ID
                periciasNiveisNormalizadas[p.nome] = maxOf(periciasNiveisNormalizadas[p.nome] ?: 0, nivel)
                periciasNiveisNormalizadas[nomeNorm] = maxOf(periciasNiveisNormalizadas[nomeNorm] ?: 0, nivel)
                
                if (p.definicaoId.isNotBlank()) {
                    val idNorm = p.definicaoId.lowercase().trim()
                    periciasNiveisNormalizadas[idNorm] = maxOf(periciasNiveisNormalizadas[idNorm] ?: 0, nivel)
                }
                periciasConhecidasNormalizadas.add(nomeNorm)
            }
        }
        mapa["pericias_normalizadas"] = periciasConhecidasNormalizadas
        mapa["pericias_conhecidas_normalizadas"] = periciasConhecidasNormalizadas
        mapa["pericias_niveis_normalizadas"] = periciasNiveisNormalizadas

        val condicoesEstado = mutableSetOf<String>()
        personagem.desvantagens.forEach { condicoesEstado.add(normalizarNomeRequisito(it.nome)) }
        personagem.qualidades.forEach { condicoesEstado.add(normalizarNomeRequisito(it)) }
        personagem.peculiaridades.forEach { condicoesEstado.add(normalizarNomeRequisito(it)) }
        mapa["condicoes_estado_normalizadas"] = condicoesEstado

        return mapa
    }

    private fun normalizarNomeRequisito(valor: String): String {
        return PreRequisitoChecker.normalizar(valor)
    }

    // === BUSCA POR ID ===

    fun getVantagemPorId(id: String): VantagemDefinicao? {
        return vantagens.find { it.id == id }
    }

    fun getDesvantagemPorId(id: String): DesvantagemDefinicao? {
        return desvantagens.find { it.id == id }
    }

    fun getPericiaPorId(id: String): PericiaDefinicao? {
        return pericias.find { it.id == id }
    }

    fun getMagiaPorId(id: String): MagiaDefinicao? {
        return magias.find { it.id == id }
    }

    fun tecnicaDificuldade(dificuldadeRaw: String): String {
        val normalizada = dificuldadeRaw.sanitized().lowercase()
        return if (normalizada.contains("dif")) "DIFICIL" else "MEDIA"
    }

    fun calcularCustoTecnica(dificuldade: String, nivelRelativoPredefinido: Int): Int {
        val nivel = nivelRelativoPredefinido.coerceAtLeast(0)
        if (nivel == 0) return 0
        return if (dificuldade == "DIFICIL") nivel + 1 else nivel
    }

    fun extrairModificadorPredefinido(preDefinidoRaw: String): Int {
        val raw = preDefinidoRaw.sanitized()
        if (raw == "-") return 0
        return Regex("([+-]\\d+)").find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    }

    fun extrairLimiteMaximoRelativo(preRequisitoRaw: String, preDefinidoModificador: Int): Int? {
        val raw = preRequisitoRaw.sanitized()
        val normalizado = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
        if (normalizado.contains("metade da penalidade")) {
            val penalidade = kotlin.math.abs(preDefinidoModificador)
            return if (penalidade > 0) penalidade / 2 else null
        }
        if (!normalizado.contains("nao pode exceder")) return null
        val bonus = Regex("([+-]\\d+)").find(normalizado)?.groupValues?.getOrNull(1)?.toIntOrNull()
        if (bonus != null) return bonus
        val relativoAteBase = kotlin.math.abs(preDefinidoModificador)
        return relativoAteBase.takeIf { it > 0 } ?: 0
    }

    fun aplicarRegraPericiaV2(
        definicao: PericiaDefinicao,
        regra: PericiaV2RuleMapItem?
    ): PericiaDefinicao {
        if (regra == null) return definicao

        val atributoMode = regra.tipo.attributeMode.lowercase()
        val dificuldadeMode = regra.tipo.difficultyMode.lowercase()

        val atributosAjustados = when {
            atributoMode == "choice" && regra.tipo.attributeOptions.isNotEmpty() -> regra.tipo.attributeOptions
            definicao.atributosPossiveis != null && definicao.atributosPossiveis.isNotEmpty() -> definicao.atributosPossiveis
            else -> listOf(definicao.atributoBase)
        }.map { it.sanitized(default = "IQ") }
            .filter { it.isNotBlank() }
            .distinct()

        val atributoBaseAjustado = when {
            definicao.atributoBase.isNotBlank() -> definicao.atributoBase
            atributosAjustados.isNotEmpty() -> atributosAjustados.first()
            else -> "IQ"
        }

        val dificuldadeAjustada = when {
            dificuldadeMode == "fixed" && !regra.tipo.difficulty.isNullOrBlank() -> regra.tipo.difficulty
            else -> definicao.dificuldadeFixa
        }?.sanitized(default = "M")

        return definicao.copy(
            atributoBase = atributoBaseAjustado.sanitized(default = "IQ"),
            atributosPossiveis = atributosAjustados.takeIf { it.size > 1 },
            atributoEscolhaObrigatoria = atributosAjustados.size > 1,
            dificuldadeFixa = dificuldadeAjustada,
            dificuldadeVariavel = dificuldadeMode == "variable"
        )
    }

    private fun atendeCondicaoPreReqPericia(
        condicao: PericiaV2CondicaoPreRequisito,
        personagem: Personagem
    ): Boolean {
        return when (condicao.type.lowercase()) {
            "required_advantage" -> personagem.vantagens.any { vantagem ->
                val alvo = normalizarComparacao(condicao.value)
                val nome = normalizarComparacao(vantagem.nome)
                val id = normalizarComparacao(vantagem.definicaoId)
                nome.contains(alvo) || id.contains(alvo)
            }
            "required_skill_level" -> {
                val min = condicao.minLevel ?: return false
                personagem.pericias.any { pericia ->
                    periciaCorrespondeNome(condicao.value, pericia) &&
                        pericia.calcularNivel(personagem) >= min
                }
            }
            else -> true
        }
    }

    private fun periciaCorrespondeNome(valorRaw: String, pericia: PericiaSelecionada): Boolean {
        val alvo = normalizarComparacao(valorRaw)
        if (alvo.isBlank()) return false
        val nome = normalizarComparacao(pericia.nome)
        val especializacao = normalizarComparacao(pericia.especializacao)
        return nome.contains(alvo) || alvo.contains(nome) ||
            (especializacao.isNotBlank() && (especializacao.contains(alvo) || alvo.contains(especializacao)))
    }

    private fun normalizarComparacao(valor: String): String {
        val semAcento = Normalizer.normalize(valor.sanitized(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace("-", " ")
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    companion object {
        private val CLASSES_BLOQUEIO = setOf(
            "Bloq./R-Mágica",
            "Bloqueio",
            "Bloqueio/R-DX",
            "Bloqueio/R-Espec.",
            "Bloqueio/R-Vont"
        )

        private val CLASSES_COMUM = setOf(
            "Com./R-Vont+AM",
            "Comm",
            "Comm/R-HT",
            "Comm/R-Mágica",
            "Comm/R-Portal",
            "Comm/R-Vont",
            "Comum",
            "Comum ou Bloqueio",
            "Comum ou Bloqueio/R-Vont",
            "Comum/ R-Especial",
            "Comum/ R-HT ou IQ",
            "Comum/ R-Vont+AM",
            "Comum/Bloqueio/R-IQ",
            "Comum/R-#",
            "Comum/R-DX",
            "Comum/R-Espec.",
            "Comum/R-Especial",
            "Comum/R-HT",
            "Comum/R-HT#",
            "Comum/R-HT+2",
            "Comum/R-Mágica",
            "Comum/R-Ocultar Rastros",
            "Comum/R-ST",
            "Comum/R-ST ou Vont",
            "Comum/R-Tranca Mágica",
            "Comum/R-Vont",
            "Comum/R-Vont ou perícia",
            "Comum/R-Vont#",
            "Comum/R-Vont+1",
            "Comum/R-Vont+AM",
            "Comum/R-Vont-2",
            "Comum/R/HT",
            "Comum/Área/R-IØ#"
        )

        private val CLASSES_ENCANTAMENTO = setOf(
            "Encant./R-Especial",
            "Encantamento",
            "Encantamento/ R-HT"
        )
        private val CLASSES_ESPECIAL = setOf(
            "Especial",
            "Especial/R-Vont",
            "Especial/Área"
        )
        private val CLASSES_INFORMACAO = setOf(
            "Informação",
            "Informação/ R-Mágica",
            "Informação/R-Espec.",
            "Informação/R-Mágica",
            "Informação/R-Vont",
            "Informação/Área"
        )
        private val CLASSES_PROJETIL = setOf(
            "Projetil",
            "Projetil/Especial",
            "Projetil/R-HT",
            "Projétil"
        )
        private val CLASSES_TOQUE = setOf(
            "Toque",
            "Toque/R-HT"
        )
        private val CLASSES_AREA = setOf(
            "Área",
            "Área/Informação",
            "Área/R-(ST+Vont)/2",
            "Área/R-Espacial",
            "Área/R-Espec.",
            "Área/R-Especial",
            "Área/R-HT",
            "Área/R-HT ou DX",
            "Área/R-Mágica",
            "Área/R-Vont",
            "Área/R-Vont-1"
        )

        private val CHAVES_BLOQUEIO = CLASSES_BLOQUEIO.map(::normalizarChaveClasse).toSet()
        private val CHAVES_COMUM = CLASSES_COMUM.map(::normalizarChaveClasse).toSet()
        private val CHAVES_ENCANTAMENTO = CLASSES_ENCANTAMENTO.map(::normalizarChaveClasse).toSet()
        private val CHAVES_ESPECIAL = CLASSES_ESPECIAL.map(::normalizarChaveClasse).toSet()
        private val CHAVES_INFORMACAO = CLASSES_INFORMACAO.map(::normalizarChaveClasse).toSet()
        private val CHAVES_PROJETIL = CLASSES_PROJETIL.map(::normalizarChaveClasse).toSet()
        private val CHAVES_TOQUE = CLASSES_TOQUE.map(::normalizarChaveClasse).toSet()
        private val CHAVES_AREA = CLASSES_AREA.map(::normalizarChaveClasse).toSet()

        @Volatile
        private var INSTANCE: DataRepository? = null

        fun getInstance(context: Context): DataRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun normalizarChaveClasse(valor: String): String {
            val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "")
            return semAcento
                .lowercase()
                .replace(Regex("\\s+"), " ")
                .trim()
        }
    }
}

