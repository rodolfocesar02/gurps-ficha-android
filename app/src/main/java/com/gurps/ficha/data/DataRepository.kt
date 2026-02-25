package com.gurps.ficha.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.*
import java.text.Normalizer

/**
 * Repositorio para carregar dados de Vantagens, Desvantagens, Pericias e Magias
 * a partir dos arquivos JSON em assets/
 */
class DataRepository(private val context: Context) {

    private val gson = Gson()

    private var _vantagens: List<VantagemDefinicao>? = null
    private var _desvantagens: List<DesvantagemDefinicao>? = null
    private var _pericias: List<PericiaDefinicao>? = null
    private var _magias: List<MagiaDefinicao>? = null
    private var _armasCatalogo: List<ArmaCatalogoItem>? = null
    private var _escudosCatalogo: List<EscudoCatalogoItem>? = null
    private var _armadurasCatalogo: List<ArmaduraCatalogoItem>? = null
    private val loadErrors = mutableMapOf<String, String>()

    val vantagens: List<VantagemDefinicao>
        get() = _vantagens ?: carregarVantagens().also { _vantagens = it }

    val desvantagens: List<DesvantagemDefinicao>
        get() = _desvantagens ?: carregarDesvantagens().also { _desvantagens = it }

    val pericias: List<PericiaDefinicao>
        get() = _pericias ?: carregarPericias().also { _pericias = it }

    val magias: List<MagiaDefinicao>
        get() = _magias ?: carregarMagias().also { _magias = it }

    val armasCatalogo: List<ArmaCatalogoItem>
        get() = _armasCatalogo ?: carregarArmasCatalogo().also { _armasCatalogo = it }

    val escudosCatalogo: List<EscudoCatalogoItem>
        get() = _escudosCatalogo ?: carregarEscudosCatalogo().also { _escudosCatalogo = it }

    val armadurasCatalogo: List<ArmaduraCatalogoItem>
        get() = _armadurasCatalogo ?: carregarArmadurasCatalogo().also { _armadurasCatalogo = it }

    fun getCatalogLoadErrors(): Map<String, String> = synchronized(loadErrors) { loadErrors.toMap() }

    private fun clearLoadError(catalog: String) {
        synchronized(loadErrors) { loadErrors.remove(catalog) }
    }

    private fun registerLoadError(catalog: String, throwable: Throwable) {
        synchronized(loadErrors) {
            loadErrors[catalog] = throwable.message ?: throwable::class.java.simpleName
        }
    }

    private fun carregarVantagens(): List<VantagemDefinicao> {
        return try {
            carregarVantagensV3()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarVantagensV3(): List<VantagemDefinicao> {
        return try {
            val json = context.assets.open("vantagens.v3.json").bufferedReader().use { it.readText() }
            val root = JsonParser.parseString(json)
            if (!root.isJsonArray) return emptyList()
            root.asJsonArray
                .mapNotNull { it.asVantagemV3OrNull() }
                .map { it.toLegacy() }
                .map { it.normalizada() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarDesvantagens(): List<DesvantagemDefinicao> {
        return try {
            carregarDesvantagensV2()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarDesvantagensV2(): List<DesvantagemDefinicao> {
        return try {
            val json = context.assets.open("desvantagens.v2.json").bufferedReader().use { it.readText() }
            val root = JsonParser.parseString(json)
            if (!root.isJsonArray) return emptyList()
            root.asJsonArray
                .mapNotNull { it.asDesvantagemV2OrNull() }
                .map { it.toLegacy() }
                .map { it.normalizada() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarPericias(): List<PericiaDefinicao> {
        return try {
            val json = context.assets.open("pericias.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<PericiaDefinicao>>() {}.type
            (gson.fromJson<List<PericiaDefinicao>>(json, type) ?: emptyList()).map { it.normalizada() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarMagias(): List<MagiaDefinicao> {
        return try {
            val json = context.assets.open("magias2versao.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<MagiaDefinicao>>() {}.type
            (gson.fromJson<List<MagiaDefinicao>>(json, type) ?: emptyList()).map { it.normalizada() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarArmasCatalogo(): List<ArmaCatalogoItem> {
        val cc = carregarArmasCorpoACorpoNormalizadas()
        val dist = carregarArmasDistanciaNormalizadas()
        return (cc + dist).sortedBy { it.nome.lowercase() }
    }

    private fun carregarArmasCorpoACorpoNormalizadas(): List<ArmaCatalogoItem> {
        return try {
            val json = context.assets.open("armas_corpo_a_corpo.v1.normalized.json")
                .bufferedReader()
                .use { it.readText() }
            val root = JsonParser.parseString(json)
            if (!root.isJsonObject) return emptyList()
            val items = root.asJsonObject.array("items") ?: return emptyList()
            val parsed = items.mapNotNull { el ->
                if (!el.isJsonObject) return@mapNotNull null
                val obj = el.asJsonObject
                val stObj = obj.obj("stMinimo")
                val danoObj = obj.obj("dano")
                val modos = obj.array("modos")
                val modo1 = modos?.firstOrNull()?.takeIf { it.isJsonObject }?.asJsonObject
                val custoObj = modo1?.obj("custo")
                val pesoObj = modo1?.obj("peso")
                val aparar = modo1?.string("aparar")?.sanitized()
                ArmaCatalogoItem(
                    id = "cc_" + obj.string("id").orEmpty(),
                    nome = obj.string("nome").orEmpty().sanitized(),
                    tipoCombate = "corpo_a_corpo",
                    categoria = obj.string("categoria").orEmpty().sanitized(),
                    grupo = obj.string("grupo").orEmpty().sanitized(),
                    stMinimo = stObj?.int("valor"),
                    danoRaw = danoObj?.string("raw").orEmpty().sanitized(),
                    custoBase = custoObj?.float("valor"),
                    pesoBaseKg = pesoObj?.float("kg"),
                    aparar = aparar,
                    observacoes = obj.string("observacoes").orEmpty().sanitized()
                )
            }.filter { it.id.isNotBlank() && it.nome.isNotBlank() }
            clearLoadError("armas_corpo_a_corpo")
            parsed
        } catch (e: Exception) {
            registerLoadError("armas_corpo_a_corpo", e)
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarArmasDistanciaNormalizadas(): List<ArmaCatalogoItem> {
        val arquivos = listOf(
            "armas_distancia.v1.normalized.json" to "distancia",
            "armas_fogo.v1.normalized.json" to "armas_de_fogo"
        )
        return arquivos.flatMap { (arquivo, tipo) -> carregarArmasDistanciaDeArquivo(arquivo, tipo) }
    }

    private fun carregarArmasDistanciaDeArquivo(
        nomeArquivo: String,
        tipoCombate: String
    ): List<ArmaCatalogoItem> {
        return try {
            val json = context.assets.open(nomeArquivo).bufferedReader().use { it.readText() }
            val root = JsonParser.parseString(json)
            if (!root.isJsonObject) return emptyList()
            val items = root.asJsonObject.array("items") ?: return emptyList()
            val parsed = items.mapNotNull { el ->
                if (!el.isJsonObject) return@mapNotNull null
                val obj = el.asJsonObject
                val stObj = obj.obj("stMinimo")
                val danoObj = obj.obj("dano")
                val custoObj = obj.obj("custo")
                val pesoObj = obj.obj("peso")
                ArmaCatalogoItem(
                    id = "dist_" + obj.string("id").orEmpty(),
                    nome = obj.string("nome").orEmpty().sanitized(),
                    tipoCombate = tipoCombate,
                    categoria = obj.string("categoria").orEmpty().sanitized(),
                    grupo = obj.string("grupo").orEmpty().sanitized(),
                    stMinimo = stObj?.int("valor"),
                    danoRaw = danoObj?.string("raw").orEmpty().sanitized(),
                    custoBase = custoObj?.float("valor"),
                    pesoBaseKg = pesoObj?.float("armaKg"),
                    aparar = null,
                    observacoes = if (tipoCombate == "distancia" || tipoCombate == "armas_de_fogo") {
                        obj.string("observacoes").orEmpty().sanitized()
                    } else {
                        ""
                    }
                )
            }.filter { it.id.isNotBlank() && it.nome.isNotBlank() }
            clearLoadError(nomeArquivo)
            parsed
        } catch (e: Exception) {
            registerLoadError(nomeArquivo, e)
            emptyList()
        }
    }

    private fun carregarEscudosCatalogo(): List<EscudoCatalogoItem> {
        return try {
            val json = context.assets.open("escudos.v1.json").bufferedReader().use { it.readText() }
            val root = JsonParser.parseString(json)
            if (!root.isJsonObject) return emptyList()
            val items = root.asJsonObject.array("items") ?: return emptyList()
            val parsed = items.mapNotNull { el ->
                if (!el.isJsonObject) return@mapNotNull null
                val obj = el.asJsonObject
                val db = obj.int("db") ?: return@mapNotNull null
                EscudoCatalogoItem(
                    id = obj.string("id").orEmpty(),
                    nome = obj.string("nome").orEmpty().sanitized(),
                    nt = obj.int("nt"),
                    db = db,
                    custo = obj.float("custo"),
                    pesoKg = obj.float("pesoKg"),
                    stMinimo = obj.int("stMinimo"),
                    observacoes = obj.string("observacoes").orEmpty().sanitized()
                )
            }.filter { it.id.isNotBlank() && it.nome.isNotBlank() }
                .sortedBy { it.nome.lowercase() }
            clearLoadError("escudos")
            parsed
        } catch (e: Exception) {
            registerLoadError("escudos", e)
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarArmadurasCatalogo(): List<ArmaduraCatalogoItem> {
        return try {
            val json = context.assets.open("armaduras.v2.json").bufferedReader().use { it.readText() }
            val root = JsonParser.parseString(json)
            if (!root.isJsonObject) return emptyList()
            val items = root.asJsonObject.array("items") ?: return emptyList()
            val parsed = items.mapNotNull { el ->
                if (!el.isJsonObject) return@mapNotNull null
                val obj = el.asJsonObject
                val comps = obj.array("componentes")
                    ?.mapNotNull { c ->
                        if (!c.isJsonObject) return@mapNotNull null
                        val co = c.asJsonObject
                        ArmaduraComponenteCatalogo(
                            local = co.string("localRaw").orEmpty().sanitized(),
                            rd = co.string("rdRaw").orEmpty().sanitized(),
                            custoBase = co.float("custoBase"),
                            pesoKg = co.float("pesoKg"),
                            tags = co.array("tags")
                                ?.mapNotNull { it.asStringOrNull()?.sanitized() }
                                ?.filter { it.isNotBlank() }
                                .orEmpty(),
                            observacoesDetalhadas = co.array("observacoesDetalhadas")
                                ?.mapNotNull { it.asStringOrNull()?.sanitized() }
                                ?.filter { it.isNotBlank() }
                                .orEmpty()
                        )
                    }
                    .orEmpty()
                ArmaduraCatalogoItem(
                    id = obj.string("id").orEmpty(),
                    nome = obj.string("nome").orEmpty().sanitized(),
                    nt = obj.int("nt"),
                    local = obj.string("localRaw").orEmpty().sanitized(),
                    rd = obj.string("rdRaw").orEmpty().sanitized(),
                    custoBase = obj.float("custoBase"),
                    pesoBaseKg = obj.float("pesoBaseKg"),
                    observacoes = (
                        obj.string("observacoes")
                            ?: obj.string("observacoesRaw")
                    ).orEmpty().sanitized(),
                    componentes = comps,
                    tags = obj.array("tags")
                        ?.mapNotNull { it.asStringOrNull()?.sanitized() }
                        ?.filter { it.isNotBlank() }
                        .orEmpty(),
                    observacoesDetalhadas = obj.array("observacoesDetalhadas")
                        ?.mapNotNull { it.asStringOrNull()?.sanitized() }
                        ?.filter { it.isNotBlank() }
                        .orEmpty()
                )
            }.filter { it.id.isNotBlank() && it.nome.isNotBlank() }
                .sortedBy { it.nome.lowercase() }
            clearLoadError("armaduras")
            parsed
        } catch (e: Exception) {
            registerLoadError("armaduras", e)
            e.printStackTrace()
            emptyList()
        }
    }

    // === FILTROS DE VANTAGENS ===

    fun filtrarVantagens(
        busca: String = "",
        tipoCusto: TipoCusto? = null,
        tag: String? = null
    ): List<VantagemDefinicao> {
        return vantagens.filter { v ->
            val matchBusca = busca.isBlank() || v.nome.contains(busca, ignoreCase = true)
            val matchTipo = tipoCusto == null || v.tipoCusto == tipoCusto
            val matchTag = tag.isNullOrBlank() || v.tags.any { it.equals(tag, ignoreCase = true) }
            matchBusca && matchTipo && matchTag
        }
    }

    // === FILTROS DE DESVANTAGENS ===

    fun filtrarDesvantagens(
        busca: String = "",
        tipoCusto: TipoCusto? = null,
        tag: String? = null
    ): List<DesvantagemDefinicao> {
        return desvantagens.filter { d ->
            val matchBusca = busca.isBlank() || d.nome.contains(busca, ignoreCase = true)
            val matchTipo = tipoCusto == null || d.tipoCusto == tipoCusto
            val matchTag = tag.isNullOrBlank() || d.tags.any { it.equals(tag, ignoreCase = true) }
            matchBusca && matchTipo && matchTag
        }
    }

    // === FILTROS DE PERICIAS ===

    fun filtrarPericias(
        busca: String = "",
        atributoBase: String? = null,
        dificuldade: String? = null
    ): List<PericiaDefinicao> {
        return pericias.filter { p ->
            val matchBusca = busca.isBlank() || p.nome.contains(busca, ignoreCase = true)
            val matchAtributo = atributoBase.isNullOrBlank() ||
                p.atributoBase.equals(atributoBase, ignoreCase = true) ||
                p.atributosPossiveis?.any { it.equals(atributoBase, ignoreCase = true) } == true
            val matchDificuldade = dificuldade.isNullOrBlank() ||
                p.dificuldadeFixa.equals(dificuldade, ignoreCase = true)
            matchBusca && matchAtributo && matchDificuldade
        }
    }

    // === FILTROS DE MAGIAS ===

    fun filtrarMagias(
        busca: String = "",
        escola: String? = null,
        classe: String? = null
    ): List<MagiaDefinicao> {
        return magias.filter { m ->
            val matchBusca = busca.isBlank() || m.nome.contains(busca, ignoreCase = true)
            val matchEscola = escola.isNullOrBlank() ||
                m.escola?.any { it.equals(escola, ignoreCase = true) } == true
            val matchClasse = classe.isNullOrBlank() ||
                agruparClasseMagia(m.classe)?.equals(classe, ignoreCase = true) == true
            matchBusca && matchEscola && matchClasse
        }
    }

    fun filtrarArmasCatalogo(
        busca: String = "",
        tipoCombate: String? = null,
        stMaximo: Int? = null
    ): List<ArmaCatalogoItem> {
        val buscaNormalizada = busca.trim()
        return armasCatalogo.filter { a ->
            val matchBusca = buscaNormalizada.isBlank() ||
                a.nome.contains(buscaNormalizada, ignoreCase = true) ||
                a.grupo.contains(buscaNormalizada, ignoreCase = true) ||
                a.categoria.contains(buscaNormalizada, ignoreCase = true)
            val matchTipo = tipoCombate.isNullOrBlank() || a.tipoCombate.equals(tipoCombate, ignoreCase = true)
            val matchSt = stMaximo == null || a.stMinimo == null || a.stMinimo <= stMaximo
            matchBusca && matchTipo && matchSt
        }.sortedBy { it.nome.lowercase() }
    }

    fun filtrarEscudosCatalogo(
        busca: String = "",
        stMaximo: Int? = null
    ): List<EscudoCatalogoItem> {
        val b = busca.trim()
        return escudosCatalogo.filter { e ->
            val matchBusca = b.isBlank() || e.nome.contains(b, ignoreCase = true)
            val matchSt = stMaximo == null || e.stMinimo == null || e.stMinimo <= stMaximo
            matchBusca && matchSt
        }.sortedBy { it.nome.lowercase() }
    }

    fun filtrarArmadurasCatalogo(
        busca: String = "",
        nt: Int? = null,
        localFiltro: String? = null,
        tagFiltro: String? = null
    ): List<ArmaduraCatalogoItem> {
        val b = busca.trim()
        val tag = tagFiltro?.trim().orEmpty()
        return armadurasCatalogo.filter { a ->
            val matchBusca = b.isBlank() ||
                a.nome.contains(b, ignoreCase = true) ||
                a.local.contains(b, ignoreCase = true) ||
                a.rd.contains(b, ignoreCase = true) ||
                a.tags.any { it.contains(b, ignoreCase = true) } ||
                a.observacoesDetalhadas.any { it.contains(b, ignoreCase = true) } ||
                a.componentes.any { c ->
                    c.tags.any { it.contains(b, ignoreCase = true) } ||
                        c.observacoesDetalhadas.any { it.contains(b, ignoreCase = true) }
                }
            val matchNt = nt == null || a.nt == nt
            val matchLocal = localFiltro.isNullOrBlank() || armaduraCobreLocal(a, localFiltro)
            val matchTag = tag.isBlank() || armaduraTemTag(a, tag)
            matchBusca && matchNt && matchLocal && matchTag
        }.sortedBy { it.nome.lowercase() }
    }

    private fun armaduraTemTag(armadura: ArmaduraCatalogoItem, tagFiltro: String): Boolean {
        if (tagFiltro.isBlank()) return true
        return armadura.tags.any { it.equals(tagFiltro, ignoreCase = true) } ||
            armadura.componentes.any { c -> c.tags.any { it.equals(tagFiltro, ignoreCase = true) } }
    }

    private fun armaduraCobreLocal(armadura: ArmaduraCatalogoItem, localFiltro: String): Boolean {
        val filtro = normalizarLocal(localFiltro)
        if (filtro.isBlank()) return true
        val locaisBrutos = mutableSetOf<String>()
        locaisBrutos.addAll(extrairLocais(armadura.local))
        armadura.componentes.forEach { c -> locaisBrutos.addAll(extrairLocais(c.local)) }

        val locaisExpandidos = locaisBrutos
            .flatMap { expandirLocalMacro(it) }
            .map { normalizarLocal(it) }
            .filter { it.isNotBlank() }
            .toSet()

        val filtroExpandido = expandirLocalMacro(filtro).map { normalizarLocal(it) }.toSet()
        return filtroExpandido.any { it in locaisExpandidos }
    }

    private fun extrairLocais(raw: String): List<String> {
        return raw
            .split(Regex("[,;/|]"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun expandirLocalMacro(local: String): List<String> {
        return when (normalizarLocal(local)) {
            "cabeca" -> listOf("cranio", "olhos", "rosto")
            "corpo" -> listOf("pescoco", "tronco", "virilha")
            "membros" -> listOf("bracos", "pernas")
            "traje_completo" -> listOf("pescoco", "tronco", "virilha", "bracos", "maos", "pernas", "pes")
            else -> listOf(local)
        }
    }

    private fun normalizarLocal(local: String): String {
        return Normalizer.normalize(local, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
            .replace(Regex("\\s+"), "_")
            .trim()
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
        descricao: String = ""
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
            pagina = definicao.pagina
        )
    }

    fun criarDesvantagemSelecionada(
        definicao: DesvantagemDefinicao,
        nivel: Int = 1,
        custoEscolhido: Int = 0,
        descricao: String = "",
        autocontrole: Int? = null
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
            pagina = definicao.pagina
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

    fun criarMagiaSelecionada(
        definicao: MagiaDefinicao,
        pontosGastos: Int = 1
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
            escola = definicao.escola
        )
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

private data class VantagemV3(
    val id: String? = null,
    val nome: String? = null,
    val pagina: Int? = null,
    val costKind: String? = null,
    val fixed: Int? = null,
    val perLevel: Int? = null,
    val options: JsonArray? = null,
    val min: Int? = null,
    val max: Int? = null,
    val rawCost: String? = null,
    val specialRule: String? = null,
    val tags: List<String>? = null
) {
    fun toLegacy(): VantagemDefinicao {
        val tipo = when {
            id.equals("aptidao_magica", ignoreCase = true) -> TipoCusto.POR_NIVEL
            id.equals("elo_mental", ignoreCase = true) -> TipoCusto.POR_NIVEL
            costKind == "fixed" -> TipoCusto.FIXO
            costKind == "perLevel" -> TipoCusto.POR_NIVEL
            costKind == "choice" -> TipoCusto.ESCOLHA
            costKind == "range" || costKind == "special" -> TipoCusto.VARIAVEL
            else -> TipoCusto.FIXO
        }

        val optionsList = options?.mapNotNull { it.asIntOrNull() }.orEmpty()

        val custoLegacy = when {
            id.equals("aptidao_magica", ignoreCase = true) || id.equals("elo_mental", ignoreCase = true) ->
                rawCost ?: "5 + 10/nível"
            costKind == "fixed" -> fixed?.toString().orEmpty()
            costKind == "perLevel" -> {
                val base = perLevel ?: fixed ?: extractFirstInt(rawCost)
                if (base != null) "$base/nível" else (rawCost ?: "0")
            }
            costKind == "choice" -> {
                if (optionsList.isNotEmpty()) optionsList.joinToString(" ou ")
                else rawCost ?: "0"
            }
            costKind == "range" -> {
                when {
                    min != null && max != null -> "$min a $max"
                    min != null -> "$min+"
                    else -> rawCost ?: "0"
                }
            }
            else -> rawCost ?: fixed?.toString() ?: "0"
        }

        return VantagemDefinicao(
            id = id.orEmpty(),
            nome = nome.orEmpty(),
            custo = custoLegacy,
            tipoCusto = tipo,
            pagina = pagina ?: 0,
            tags = tags.orEmpty()
        )
    }
}

private data class DesvantagemV2(
    val id: String? = null,
    val nome: String? = null,
    val pagina: Int? = null,
    val costKind: String? = null,
    val fixed: Int? = null,
    val perLevel: Int? = null,
    val options: JsonArray? = null,
    val min: Int? = null,
    val max: Int? = null,
    val rawCost: String? = null,
    val tags: List<String>? = null
) {
    fun toLegacy(): DesvantagemDefinicao {
        val tipo = when (costKind) {
            "fixed" -> TipoCusto.FIXO
            "perLevel" -> TipoCusto.POR_NIVEL
            "choice" -> TipoCusto.ESCOLHA
            "range", "special" -> TipoCusto.VARIAVEL
            else -> TipoCusto.FIXO
        }

        val optionsList = options?.mapNotNull { it.asIntOrNull() }.orEmpty()

        val custoLegacy = when (costKind) {
            "fixed" -> fixed?.toString().orEmpty()
            "perLevel" -> {
                val base = perLevel ?: fixed ?: extractFirstInt(rawCost)
                if (base != null) "$base/nível" else (rawCost ?: "0")
            }
            "choice" -> {
                if (optionsList.isNotEmpty()) optionsList.joinToString(" ou ")
                else rawCost ?: "0"
            }
            "range" -> {
                when {
                    min != null && max != null -> "$min a $max"
                    min != null -> "$min+"
                    else -> rawCost ?: "0"
                }
            }
            else -> rawCost ?: fixed?.toString() ?: "0"
        }

        return DesvantagemDefinicao(
            id = id.orEmpty(),
            nome = nome.orEmpty(),
            custo = custoLegacy,
            tipoCusto = tipo,
            pagina = pagina ?: 0,
            tags = tags.orEmpty()
        )
    }
}

private fun extractFirstInt(raw: String?): Int? {
    if (raw.isNullOrBlank()) return null
    return Regex("-?\\d+").find(raw)?.value?.toIntOrNull()
}

private fun com.google.gson.JsonElement.asIntOrNull(): Int? {
    return runCatching {
        when {
            isJsonPrimitive && asJsonPrimitive.isNumber -> asInt
            isJsonPrimitive && asJsonPrimitive.isString -> asString.toInt()
            else -> null
        }
    }.getOrNull()
}

private fun JsonElement.asVantagemV3OrNull(): VantagemV3? {
    if (!isJsonObject) return null
    val obj = asJsonObject
    return VantagemV3(
        id = obj.string("id"),
        nome = obj.string("nome"),
        pagina = obj.int("pagina"),
        costKind = obj.string("costKind"),
        fixed = obj.int("fixed"),
        perLevel = obj.int("perLevel"),
        options = obj.array("options"),
        min = obj.int("min"),
        max = obj.int("max"),
        rawCost = obj.string("rawCost"),
        specialRule = obj.string("specialRule"),
        tags = obj.array("tags")?.mapNotNull { it.asStringOrNull() }
    )
}

private fun JsonElement.asDesvantagemV2OrNull(): DesvantagemV2? {
    if (!isJsonObject) return null
    val obj = asJsonObject
    return DesvantagemV2(
        id = obj.string("id"),
        nome = obj.string("nome"),
        pagina = obj.int("pagina"),
        costKind = obj.string("costKind"),
        fixed = obj.int("fixed"),
        perLevel = obj.int("perLevel"),
        options = obj.array("options"),
        min = obj.int("min"),
        max = obj.int("max"),
        rawCost = obj.string("rawCost"),
        tags = obj.array("tags")?.mapNotNull { it.asStringOrNull() }
    )
}

private fun JsonObject.string(key: String): String? {
    val el = get(key) ?: return null
    return if (el.isJsonNull) null else el.asStringOrNull()
}

private fun JsonObject.int(key: String): Int? {
    val el = get(key) ?: return null
    if (el.isJsonNull) return null
    return el.asIntOrNull()
}

private fun JsonObject.array(key: String): JsonArray? {
    val el = get(key) ?: return null
    return if (el.isJsonArray) el.asJsonArray else null
}

private fun JsonObject.obj(key: String): JsonObject? {
    val el = get(key) ?: return null
    return if (el.isJsonObject) el.asJsonObject else null
}

private fun JsonObject.float(key: String): Float? {
    val el = get(key) ?: return null
    if (el.isJsonNull) return null
    return runCatching {
        when {
            el.isJsonPrimitive && el.asJsonPrimitive.isNumber -> el.asFloat
            el.isJsonPrimitive && el.asJsonPrimitive.isString -> el.asString.replace(",", ".").toFloat()
            else -> null
        }
    }.getOrNull()
}

private fun JsonElement.asStringOrNull(): String? {
    return runCatching {
        if (isJsonPrimitive && asJsonPrimitive.isString) asString else null
    }.getOrNull()
}

private fun VantagemDefinicao.normalizada(): VantagemDefinicao = copy(
    id = (id as String?).sanitized(),
    nome = (nome as String?).sanitized(),
    custo = (custo as String?).sanitized(),
    tags = tags.map { (it as String?).sanitized() }.filter { it.isNotBlank() }
)

private fun DesvantagemDefinicao.normalizada(): DesvantagemDefinicao = copy(
    id = (id as String?).sanitized(),
    nome = (nome as String?).sanitized(),
    custo = (custo as String?).sanitized(),
    tags = tags.map { (it as String?).sanitized() }.filter { it.isNotBlank() }
)

private fun PericiaDefinicao.normalizada(): PericiaDefinicao = copy(
    id = (id as String?).sanitized(),
    nome = (nome as String?).sanitized(),
    atributoBase = (atributoBase as String?).sanitized(default = "IQ"),
    atributosPossiveis = atributosPossiveis
        ?.map { (it as String?).sanitized() }
        ?.filter { it.isNotBlank() },
    dificuldadeFixa = (dificuldadeFixa as String?)?.sanitized()
)

private fun MagiaDefinicao.normalizada(): MagiaDefinicao = copy(
    id = (id as String?).sanitized(),
    nome = (nome as String?).sanitized(),
    dificuldadeFixa = (dificuldadeFixa as String?)?.sanitized(),
    classe = (classe as String?)?.sanitized(),
    escola = escola?.map { (it as String?).sanitized() }?.filter { it.isNotBlank() },
    duracao = (duracao as String?)?.sanitized(),
    energia = (energia as String?)?.sanitized(),
    tempoOperacao = (tempoOperacao as String?)?.sanitized(),
    preRequisitos = (preRequisitos as String?)?.sanitized()
)

private fun String?.sanitized(default: String = ""): String {
    return this
        ?.fixMojibakeIfNeeded()
        ?.trim()
        .orEmpty()
        .ifBlank { default }
}

private fun String.fixMojibakeIfNeeded(): String {
    val markers = listOf("Ã", "Â", "â", "�")
    var current = this
    repeat(2) {
        if (!markers.any { current.contains(it) }) return current
        val repaired = runCatching {
            String(current.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
        }.getOrElse { current }
        if (repaired == current) return@repeat
        current = repaired
    }
    return current
}

