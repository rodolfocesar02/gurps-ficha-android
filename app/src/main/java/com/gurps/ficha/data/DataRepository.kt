package com.gurps.ficha.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.*
import com.gurps.ficha.regras_prerequisitos.PreRequisitoParser
import com.gurps.ficha.regras_prerequisitos.PreRequisitoChecker
import java.text.Normalizer

/**
 * Repositorio para carregar dados de Vantagens, Desvantagens, Pericias e Magias
 * a partir dos arquivos JSON em assets/
 */
private val gson = Gson()

class DataRepository(private val context: Context) {
    private data class MagiaFiltroIndex(
        val definicao: MagiaDefinicao,
        val nomeNorm: String,
        val classeNorm: String,
        val escolasNorm: Set<String>
    )


    private var _vantagens: List<VantagemDefinicao>? = null
    private var _vantagensArtesMarciaisIds: Set<String> = emptySet()
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
    private val loadErrors = mutableMapOf<String, String>()

    val vantagens: List<VantagemDefinicao>
        get() = _vantagens ?: carregarVantagens().also { _vantagens = it }

    val desvantagens: List<DesvantagemDefinicao>
        get() = _desvantagens ?: carregarDesvantagens().also { _desvantagens = it }

    val pericias: List<PericiaDefinicao>
        get() = _pericias ?: carregarPericias().also { _pericias = it }

    val periciasV2Rules: Map<String, PericiaV2RuleMapItem>
        get() = _periciasV2Rules ?: carregarPericiasV2Rules().also { _periciasV2Rules = it }

    val periciasSuplementares: List<PericiaSuplementarItem>
        get() = _periciasSuplementares ?: carregarPericiasSuplementares().also { _periciasSuplementares = it }

    val magias: List<MagiaDefinicao>
        get() = _magias ?: carregarMagias().also { _magias = it }

    private val magiasFiltroIndex: List<MagiaFiltroIndex>
        get() = _magiasFiltroIndex ?: magias.map { magia ->
            MagiaFiltroIndex(
                definicao = magia,
                nomeNorm = normalizarBusca(magia.nome),
                classeNorm = normalizarBusca(agruparClasseMagia(magia.classe).orEmpty()),
                escolasNorm = magia.escola
                    .orEmpty()
                    .asSequence()
                    .map { normalizarBusca(it) }
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
            val jsonBase = context.assets.open("vantagens.v3.json").bufferedReader().use { it.readText() }
            val root = JsonParser.parseString(jsonBase)
            if (!root.isJsonArray) return emptyList()
            val base = root.asJsonArray
                .mapNotNull { it.asVantagemV3OrNull() }
                .map { it.toLegacy() }
                .map { it.normalizada() }
            val extras = carregarVantagensExtrasArtesMarciaisV1()
            val seen = base.map { it.id.lowercase() }.toMutableSet()
            val merged = base.toMutableList()
            extras.forEach { extra ->
                val key = extra.id.lowercase()
                if (key !in seen) {
                    merged.add(extra)
                    seen.add(key)
                }
            }
            clearLoadError("vantagens")
            merged
        } catch (e: Exception) {
            registerLoadError("vantagens", e)
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarVantagensExtrasArtesMarciaisV1(): List<VantagemDefinicao> {
        return try {
            val json = context.assets.open("vantagens_artes_marciais.v1.json")
                .bufferedReader()
                .use { it.readText() }
            val root = JsonParser.parseString(json)
            if (!root.isJsonArray) return emptyList()
            val parsed = root.asJsonArray
                .mapNotNull { it.asVantagemV3OrNull() }
                .map { it.toLegacy() }
                .map { it.normalizada() }
            _vantagensArtesMarciaisIds = parsed
                .map { it.id.lowercase() }
                .filter { it.isNotBlank() }
                .toSet()
            clearLoadError("vantagens_artes_marciais")
            parsed
        } catch (e: Exception) {
            registerLoadError("vantagens_artes_marciais", e)
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
            val parsed = (gson.fromJson<List<PericiaDefinicao>>(json, type) ?: emptyList())
                .map { it.normalizada() }
                .map { aplicarRegraPericiaV2(it, periciasV2Rules[it.id]) }
            clearLoadError("pericias")
            parsed
        } catch (e: Exception) {
            registerLoadError("pericias", e)
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarPericiasV2Rules(): Map<String, PericiaV2RuleMapItem> {
        return try {
            val json = context.assets.open("pericias_v2_rules_map.json")
                .bufferedReader()
                .use { it.readText() }
            val root = JsonParser.parseString(json)
            if (!root.isJsonObject) return emptyMap()
            val items = root.asJsonObject.array("items") ?: return emptyMap()
            val parsed = items.mapNotNull { el ->
                if (!el.isJsonObject) return@mapNotNull null
                val obj = el.asJsonObject
                val id = obj.string("id").orEmpty().sanitized()
                if (id.isBlank()) return@mapNotNull null
                val tipoObj = obj.obj("tipo")
                val preReqObj = obj.obj("preRequisito")
                val predefObj = obj.obj("preDefinido")

                val tipoRegra = PericiaV2TipoRegra(
                    attributeMode = tipoObj?.string("attributeMode").orEmpty().sanitized(default = "fixed"),
                    attributeOptions = tipoObj?.array("attributeOptions")
                        ?.mapNotNull { it.asStringOrNull()?.sanitized() }
                        ?.filter { it.isNotBlank() }
                        .orEmpty(),
                    difficultyMode = tipoObj?.string("difficultyMode").orEmpty().sanitized(default = "fixed"),
                    difficulty = tipoObj?.string("difficulty")?.sanitized()
                )

                val andGroups = preReqObj
                    ?.obj("logic")
                    ?.array("and")
                    ?.mapNotNull { andEl ->
                        if (!andEl.isJsonObject) return@mapNotNull null
                        val orArray = andEl.asJsonObject.array("or") ?: return@mapNotNull null
                        val conds = orArray.mapNotNull { tokenEl ->
                            if (!tokenEl.isJsonObject) return@mapNotNull null
                            val tokenObj = tokenEl.asJsonObject
                            val tokenType = tokenObj.string("type").orEmpty().sanitized()
                            when (tokenType) {
                                "required_advantage" -> {
                                    val value = (tokenObj.string("catalogMatch")
                                        ?: tokenObj.string("value"))
                                        .orEmpty()
                                        .sanitized()
                                    if (value.isBlank()) null else PericiaV2CondicaoPreRequisito(
                                        type = tokenType,
                                        value = value
                                    )
                                }
                                "required_skill_level" -> {
                                    val value = tokenObj.string("value").orEmpty().sanitized()
                                    val minLevel = tokenObj.int("minLevel")
                                    if (value.isBlank() || minLevel == null) null
                                    else PericiaV2CondicaoPreRequisito(
                                        type = tokenType,
                                        value = value,
                                        minLevel = minLevel
                                    )
                                }
                                else -> null
                            }
                        }.filter { it.value.isNotBlank() }
                        conds.takeIf { it.isNotEmpty() }
                    }
                    .orEmpty()

                val parsedPredef = predefObj
                    ?.array("parsed")
                    ?.mapNotNull { predefEl ->
                        if (!predefEl.isJsonObject) return@mapNotNull null
                        val predef = predefEl.asJsonObject
                        PericiaV2PreDefinidoEntrada(
                            type = predef.string("type").orEmpty().sanitized(),
                            base = predef.string("base").orEmpty().sanitized(),
                            modifier = predef.int("modifier") ?: 0
                        )
                    }
                    .orEmpty()

                val regra = PericiaV2RuleMapItem(
                    id = id,
                    nome = obj.string("nome").orEmpty().sanitized(),
                    tipo = tipoRegra,
                    preRequisito = PericiaV2PreRequisitoRegra(
                        raw = preReqObj?.string("raw").orEmpty().sanitized(),
                        allowWithoutPrerequisite = preReqObj?.bool("allowWithoutPrerequisite") ?: true,
                        andGroups = andGroups
                    ),
                    preDefinido = PericiaV2PreDefinidoRegra(
                        raw = predefObj?.string("raw").orEmpty().sanitized(),
                        onZeroPoints = predefObj?.string("onZeroPoints").orEmpty().sanitized(),
                        parsed = parsedPredef
                    ),
                    descricao = obj.string("descricao").orEmpty().sanitized(),
                    modificadoresRaw = obj
                        .obj("modificadores")
                        ?.string("raw")
                        .orEmpty()
                        .sanitized()
                )
                id to regra
            }.toMap()

            clearLoadError("pericias_v2_rules_map")
            parsed
        } catch (e: Exception) {
            registerLoadError("pericias_v2_rules_map", e)
            emptyMap()
        }
    }

    private fun carregarPericiasSuplementares(): List<PericiaSuplementarItem> {
        return try {
            val json = context.assets.open("pericias_artes_marciais.v1.json")
                .bufferedReader()
                .use { it.readText() }
            val root = JsonParser.parseString(json)
            if (!root.isJsonObject) return emptyList()
            val items = root.asJsonObject.array("items") ?: return emptyList()
            val parsed = items.mapNotNull { el ->
                if (!el.isJsonObject) return@mapNotNull null
                val obj = el.asJsonObject
                PericiaSuplementarItem(
                    id = obj.string("id").orEmpty().sanitized(),
                    nome = obj.string("nome").orEmpty().sanitized(),
                    pagina = obj.int("pagina"),
                    paginaRaw = obj.string("paginaRaw").orEmpty().sanitized(),
                    dificuldadeRaw = obj.string("dificuldadeRaw").orEmpty().sanitized(),
                    preDefinidoRaw = obj.string("preDefinidoRaw").orEmpty().sanitized(),
                    preRequisitoRaw = obj.string("preRequisitoRaw").orEmpty().sanitized(),
                    descricao = obj.string("descricao").orEmpty().sanitized(),
                    modificadores = obj.string("modificadores").orEmpty().sanitized(),
                    sourceBook = obj.string("sourceBook").orEmpty().sanitized(),
                    sourceFile = obj.string("sourceFile").orEmpty().sanitized()
                )
            }.filter { it.id.isNotBlank() && it.nome.isNotBlank() }
            clearLoadError("pericias_artes_marciais")
            parsed
        } catch (e: Exception) {
            registerLoadError("pericias_artes_marciais", e)
            emptyList()
        }
    }

    private fun carregarMagias(): List<MagiaDefinicao> {
        return try {
            val json = context.assets.open("magias2versao.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<MagiaDefinicao>>() {}.type
            val parsed = (gson.fromJson<List<MagiaDefinicao>>(json, type) ?: emptyList())
                .map { it.normalizada() }
                .map { magia ->
                    magia.copy(
                        preRequisitos = preRequisitoCanonicoTexto(magia.id, magia.preRequisitos)
                            .takeIf { it.isNotBlank() }
                    )
                }
            clearLoadError("magias")
            parsed
        } catch (e: Exception) {
            registerLoadError("magias", e)
            e.printStackTrace()
            emptyList()
        }
    }

    private fun carregarTecnicasCatalogo(): List<TecnicaCatalogoItem> {
        return try {
            val json = context.assets.open("tecnicas.v1.json")
                .bufferedReader()
                .use { it.readText() }
            val root = JsonParser.parseString(json)
            if (!root.isJsonObject) return emptyList()
            val items = root.asJsonObject.array("items") ?: return emptyList()
            val parsed = items.mapNotNull { el ->
                if (!el.isJsonObject) return@mapNotNull null
                val obj = el.asJsonObject
                TecnicaCatalogoItem(
                    id = obj.string("id").orEmpty().sanitized(),
                    nome = obj.string("nome").orEmpty().sanitized(),
                    pagina = obj.int("pagina"),
                    paginaRaw = obj.string("paginaRaw").orEmpty().sanitized(),
                    dificuldadeRaw = obj.string("dificuldadeRaw").orEmpty().sanitized(),
                    preDefinidoRaw = obj.string("preDefinidoRaw").orEmpty().sanitized(),
                    preRequisitoRaw = obj.string("preRequisitoRaw").orEmpty().sanitized(),
                    descricao = obj.string("descricao").orEmpty().sanitized(),
                    modificadores = obj.string("modificadores").orEmpty().sanitized(),
                    sourceBook = obj.string("sourceBook").orEmpty().sanitized(),
                    sourceFile = obj.string("sourceFile").orEmpty().sanitized()
                )
            }.filter { it.id.isNotBlank() && it.nome.isNotBlank() }
            clearLoadError("tecnicas")
            parsed
        } catch (e: Exception) {
            registerLoadError("tecnicas", e)
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

    private fun carregarModificadoresGerais(): List<ModificadorDefinicao> {
        return try {
            val json = context.assets.open("modificadores.v1.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<ModificadorDefinicao>>() {}.type
            gson.fromJson<List<ModificadorDefinicao>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            registerLoadError("modificadores", e)
            e.printStackTrace()
            emptyList()
        }
    }

    // === FILTROS DE VANTAGENS ===

    private fun normalizarBusca(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun contemBusca(texto: String, busca: String): Boolean {
        if (busca.isBlank()) return true
        val buscaNorm = normalizarBusca(busca)
        if (buscaNorm.isBlank()) return true
        return normalizarBusca(texto).contains(buscaNorm)
    }

    private fun igualNormalizado(texto: String, valor: String): Boolean {
        val textoNorm = normalizarBusca(texto)
        val valorNorm = normalizarBusca(valor)
        if (textoNorm.isBlank() || valorNorm.isBlank()) return false
        return textoNorm == valorNorm
    }

    fun filtrarVantagens(
        busca: String = "",
        tipoCusto: TipoCusto? = null,
        tag: String? = null,
        somenteArtesMarciais: Boolean = false
    ): List<VantagemDefinicao> {
        return vantagens.filter { v ->
            val matchBusca = contemBusca(v.nome, busca)
            val matchTipo = tipoCusto == null || v.tipoCusto == tipoCusto
            val matchTag = tag.isNullOrBlank() || v.tags.any { contemBusca(it, tag) }
            val matchArtesMarciais = !somenteArtesMarciais || _vantagensArtesMarciaisIds.contains(v.id.lowercase())
            matchBusca && matchTipo && matchTag && matchArtesMarciais
        }
    }

    // === FILTROS DE DESVANTAGENS ===

    fun filtrarDesvantagens(
        busca: String = "",
        tipoCusto: TipoCusto? = null,
        tag: String? = null
    ): List<DesvantagemDefinicao> {
        return desvantagens.filter { d ->
            val matchBusca = contemBusca(d.nome, busca)
            val matchTipo = tipoCusto == null || d.tipoCusto == tipoCusto
            val matchTag = tag.isNullOrBlank() || d.tags.any { contemBusca(it, tag) }
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
            val matchBusca = contemBusca(p.nome, busca)
            val matchAtributo = atributoBase.isNullOrBlank() ||
                contemBusca(p.atributoBase, atributoBase) ||
                p.atributosPossiveis?.any { contemBusca(it, atributoBase) } == true
            val matchDificuldade = dificuldade.isNullOrBlank() ||
                contemBusca(p.dificuldadeFixa.orEmpty(), dificuldade)
            matchBusca && matchAtributo && matchDificuldade
        }
    }

    // === FILTROS DE MAGIAS ===

    fun filtrarMagias(
        busca: String = "",
        escola: String? = null,
        classe: String? = null
    ): List<MagiaDefinicao> {
        if (busca.isBlank() && escola.isNullOrBlank() && classe.isNullOrBlank()) return magias

        val buscaNorm = normalizarBusca(busca)
        val escolaNorm = escola?.let(::normalizarBusca).orEmpty()
        val classeNorm = classe?.let(::normalizarBusca).orEmpty()
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

    fun filtrarTecnicasCatalogo(
        busca: String = "",
        sourceBook: String? = null
    ): List<TecnicaCatalogoItem> {
        val b = busca.trim()
        return tecnicasCatalogo.filter { t ->
            val matchBusca = b.isBlank() ||
                contemBusca(t.nome, b) ||
                contemBusca(t.descricao, b) ||
                contemBusca(t.preRequisitoRaw, b)
            val matchSource = sourceBook.isNullOrBlank() || contemBusca(t.sourceBook, sourceBook)
            matchBusca && matchSource
        }.sortedBy { it.nome.lowercase() }
    }

    fun filtrarArmasCatalogo(
        busca: String = "",
        tipoCombate: String? = null,
        stMaximo: Int? = null
    ): List<ArmaCatalogoItem> {
        val buscaNormalizada = busca.trim()
        return armasCatalogo.filter { a ->
            val matchBusca = buscaNormalizada.isBlank() ||
                contemBusca(a.nome, buscaNormalizada) ||
                contemBusca(a.grupo, buscaNormalizada) ||
                contemBusca(a.categoria, buscaNormalizada)
            val matchTipo = tipoCombate.isNullOrBlank() || contemBusca(a.tipoCombate, tipoCombate)
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
            val matchBusca = b.isBlank() || contemBusca(e.nome, b)
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
                contemBusca(a.nome, b) ||
                contemBusca(a.local, b) ||
                contemBusca(a.rd, b) ||
                a.tags.any { contemBusca(it, b) } ||
                a.observacoesDetalhadas.any { contemBusca(it, b) } ||
                a.componentes.any { c ->
                    c.tags.any { contemBusca(it, b) } ||
                        c.observacoesDetalhadas.any { contemBusca(it, b) }
                }
            val matchNt = nt == null || a.nt == nt
            val matchLocal = localFiltro.isNullOrBlank() || armaduraCobreLocal(a, localFiltro)
            val matchTag = tag.isBlank() || armaduraTemTag(a, tag)
            matchBusca && matchNt && matchLocal && matchTag
        }.sortedBy { it.nome.lowercase() }
    }

    private fun armaduraTemTag(armadura: ArmaduraCatalogoItem, tagFiltro: String): Boolean {
        if (tagFiltro.isBlank()) return true
        return armadura.tags.any { contemBusca(it, tagFiltro) } ||
            armadura.componentes.any { c -> c.tags.any { contemBusca(it, tagFiltro) } }
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
        val andGroups = regra.preRequisito.andGroups
        if (andGroups.isEmpty() || regra.preRequisito.allowWithoutPrerequisite) return null

        val gruposFalhos = andGroups.filter { grupo ->
            grupo.none { condicao -> atendeCondicaoPreReqPericia(condicao, personagem) }
        }
        if (gruposFalhos.isEmpty()) return null

        return regra.preRequisito.raw
            .takeIf { it.isNotBlank() }
            ?: "Pré-requisito não atendido."
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
        if (trimmed == "â€”" || trimmed == "â€“" || trimmed == "âˆ’") return true
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

    private fun preRequisitoCanonicoTexto(magiaId: String, raw: String?): String {
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
        mapa["ST"] = personagem.forca
        mapa["DX"] = personagem.destreza
        mapa["IQ"] = personagem.inteligencia
        mapa["HT"] = personagem.vitalidade

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

        val periciasConhecidasNormalizadas = personagem.pericias
            .map { p -> normalizarNomeRequisito(p.nome) }
            .filter { it.isNotBlank() }
            .toSet()
        mapa["pericias_conhecidas_normalizadas"] = periciasConhecidasNormalizadas

        val condicoesEstado = mutableSetOf<String>()
        personagem.desvantagens.forEach { condicoesEstado.add(normalizarNomeRequisito(it.nome)) }
        personagem.qualidades.forEach { condicoesEstado.add(normalizarNomeRequisito(it)) }
        personagem.peculiaridades.forEach { condicoesEstado.add(normalizarNomeRequisito(it)) }
        mapa["condicoes_estado_normalizadas"] = condicoesEstado

        return mapa
    }

    private fun normalizarNomeRequisito(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace("-", " ")
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
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

    private fun aplicarRegraPericiaV2(
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
    val tags: List<String>? = null,
    val descricao: String? = null,
    val modificadores_especificos: List<ModificadorDefinicao>? = null
) {
    fun toLegacy(): VantagemDefinicao {
        val tipo = when {
            id.equals("aptidao_magica", ignoreCase = true) -> TipoCusto.POR_NIVEL
            id.equals("aptidao_astral", ignoreCase = true) -> TipoCusto.POR_NIVEL
            id.equals("elo_mental", ignoreCase = true) -> TipoCusto.POR_NIVEL
            costKind == "fixed" -> TipoCusto.FIXO
            costKind == "perLevel" -> TipoCusto.POR_NIVEL
            costKind == "choice" -> TipoCusto.ESCOLHA
            costKind == "range" || costKind == "special" -> TipoCusto.VARIAVEL
            else -> TipoCusto.FIXO
        }

        val optionsList = options?.mapNotNull { it.asIntOrNull() }.orEmpty()

        val custoLegacy = when {
            id.equals("aptidao_magica", ignoreCase = true) ||
            id.equals("aptidao_astral", ignoreCase = true) ||
            id.equals("elo_mental", ignoreCase = true) ->
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
            tags = tags.orEmpty(),
            descricao = descricao,
            modificadoresEspecificos = modificadores_especificos.orEmpty()
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
    val specialRule: String? = null,
    val tags: List<String>? = null,
    val descricao: String? = null,
    val modificadores_especificos: List<ModificadorDefinicao>? = null
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
            // Preserva sufixos canonicos do rawCost (ex.: "*" de autocontrole mental).
            "fixed" -> rawCost?.takeIf { it.isNotBlank() } ?: fixed?.toString().orEmpty()
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
            tags = tags.orEmpty(),
            descricao = descricao,
            specialRule = specialRule,
            modificadoresEspecificos = modificadores_especificos.orEmpty()
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
        tags = obj.array("tags")?.mapNotNull { it.asStringOrNull() },
        descricao = obj.string("descricao"),
        modificadores_especificos = obj.array("modificadores_especificos")?.mapNotNull {
            gson.fromJson(it, ModificadorDefinicao::class.java)
        }
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
        specialRule = obj.string("specialRule"),
        tags = obj.array("tags")?.mapNotNull { it.asStringOrNull() },
        descricao = obj.string("descricao"),
        modificadores_especificos = obj.array("modificadores_especificos")?.mapNotNull {
            gson.fromJson(it, ModificadorDefinicao::class.java)
        }
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

private fun JsonObject.bool(key: String): Boolean? {
    val el = get(key) ?: return null
    if (el.isJsonNull) return null
    return runCatching {
        when {
            el.isJsonPrimitive && el.asJsonPrimitive.isBoolean -> el.asBoolean
            el.isJsonPrimitive && el.asJsonPrimitive.isString -> el.asString.equals("true", ignoreCase = true)
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
    tags = tags.map { (it as String?).sanitized() }.filter { it.isNotBlank() },
    descricao = descricao?.sanitized()
)

private fun DesvantagemDefinicao.normalizada(): DesvantagemDefinicao = copy(
    id = (id as String?).sanitized(),
    nome = (nome as String?).sanitized(),
    custo = (custo as String?).sanitized(),
    tags = tags.map { (it as String?).sanitized() }.filter { it.isNotBlank() },
    descricao = descricao?.sanitized()
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

private fun MagiaDefinicao.normalizada(): MagiaDefinicao {
    val magiaId = id.sanitized()
    val textoRaw = texto.sanitized()
    val descricaoRaw = descricao.sanitized()
    val meta = extrairMetadadosDoTextoMagia(textoRaw)
    val nomeSanitizado = nome.sanitized()
    val nomeCorrigido = if (magiaId == "suspender") {
        "Suspender Aptidao Magica"
    } else {
        corrigirNomeMagiaPorId(magiaId, nomeSanitizado)
    }
    val escolaNormalizada = escola?.map { it.sanitized() }?.filter { it.isNotBlank() }

    return copy(
        id = magiaId,
        nome = nomeCorrigido,
        dificuldadeFixa = dificuldadeFixa?.sanitized(),
        classe = classe?.sanitized().takeUnless { it.isNullOrBlank() } ?: meta.classe,
        escola = escolaNormalizada?.takeUnless { it.isEmpty() } ?: meta.escola,
        duracao = duracao?.sanitized().takeUnless { it.isNullOrBlank() } ?: meta.duracao,
        energia = energia?.sanitized().takeUnless { it.isNullOrBlank() } ?: meta.energia,
        tempoOperacao = tempoOperacao?.sanitized().takeUnless { it.isNullOrBlank() } ?: meta.tempoOperacao,
        preRequisitos = corrigirTextoMagiaCorrompido(
            preRequisitos?.sanitized().takeUnless { it.isNullOrBlank() } ?: meta.preRequisitos.orEmpty()
        ).takeIf { it.isNotBlank() },
        texto = descricaoRaw.takeUnless { it.isBlank() } ?: meta.descricao
    )
}

private data class MetaMagiaExtraida(
    val classe: String? = null,
    val escola: List<String>? = null,
    val duracao: String? = null,
    val energia: String? = null,
    val tempoOperacao: String? = null,
    val preRequisitos: String? = null,
    val descricao: String? = null
)

private fun extrairMetadadosDoTextoMagia(texto: String?): MetaMagiaExtraida {
    if (texto.isNullOrBlank()) return MetaMagiaExtraida(descricao = "")
    var classe: String? = null
    var escola: List<String>? = null
    var duracao: String? = null
    var energia: String? = null
    var tempo: String? = null
    var preReq: String? = null
    val descricaoLinhas = mutableListOf<String>()

    texto.lines().forEach { linha ->
        val atual = linha.trim()
        if (atual.isBlank()) return@forEach
        val classeMatch = Regex("(?i)^classe\\s*:\\s*(.+)$").find(atual)
        if (classeMatch != null) {
            classe = classeMatch.groupValues[1].trim().takeIf { it.isNotBlank() }
            return@forEach
        }
        val escolaMatch = Regex("(?i)^escola\\s*:\\s*(.+)$").find(atual)
        if (escolaMatch != null) {
            val escolas = escolaMatch.groupValues[1]
                .split("/", ",", ";")
                .map { it.trim() }
                .filter { it.isNotBlank() }
            escola = escolas.takeIf { it.isNotEmpty() }
            return@forEach
        }
        val duracaoMatch = Regex("(?i)^dura[cç][aã]o\\s*:\\s*(.+)$").find(atual)
        if (duracaoMatch != null) {
            duracao = duracaoMatch.groupValues[1].trim().takeIf { it.isNotBlank() }
            return@forEach
        }
        val energiaMatch = Regex("(?i)^energia\\s*:\\s*(.+)$").find(atual)
        if (energiaMatch != null) {
            energia = energiaMatch.groupValues[1].trim().takeIf { it.isNotBlank() }
            return@forEach
        }
        val tempoMatch = Regex("(?i)^tempo\\s+de\\s+opera[cç][aã]o\\s*:\\s*(.+)$").find(atual)
        if (tempoMatch != null) {
            tempo = tempoMatch.groupValues[1].trim().takeIf { it.isNotBlank() }
            return@forEach
        }
        val preReqMatch = Regex("(?i)^pr[eé][ -]?requisitos?\\s*:\\s*(.+)$").find(atual)
        if (preReqMatch != null) {
            preReq = preReqMatch.groupValues[1].trim().takeIf { it.isNotBlank() }
            return@forEach
        }
        descricaoLinhas.add(atual)
    }

    return MetaMagiaExtraida(
        classe = classe,
        escola = escola,
        duracao = duracao,
        energia = energia,
        tempoOperacao = tempo,
        preRequisitos = preReq,
        descricao = descricaoLinhas.joinToString("\n").trim().takeIf { it.isNotBlank() } ?: ""
    )
}

private fun corrigirNomeMagiaPorId(id: String, nomeAtual: String): String {
    if (nomeAtual.isBlank()) return humanizarIdMagia(id)
    val nomeReparado = corrigirTextoMagiaCorrompido(nomeAtual)

    val idRotulo = humanizarIdMagia(id)
    val nomeNorm = normalizarComparacaoMagia(nomeReparado)
    val idNorm = normalizarComparacaoMagia(idRotulo)
    if (nomeNorm.isBlank()) return idRotulo

    val suspeitaTruncamento = nomeNorm.length + 1 <= idNorm.length &&
        (idNorm.contains(nomeNorm) || distanciaLevenshtein(nomeNorm, idNorm) <= 2)
    return if (suspeitaTruncamento) idRotulo else nomeReparado
}

private fun corrigirTextoMagiaCorrompido(valor: String): String {
    if (valor.isBlank()) return valor
    return valor
        .replace(Regex("\\bRelmpagos\\b", RegexOption.IGNORE_CASE), "Relampagos")
        .replace(Regex("\\bRelmpago\\b", RegexOption.IGNORE_CASE), "Relampago")
        .replace(Regex("\\bFuraco\\b", RegexOption.IGNORE_CASE), "Furacao")
        .replace(Regex("\\bMgicas\\b", RegexOption.IGNORE_CASE), "Magicas")
        .replace(Regex("\\bMgica\\b", RegexOption.IGNORE_CASE), "Magica")
}

private fun humanizarIdMagia(id: String): String {
    return id
        .split("_")
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.lowercase().replaceFirstChar { c -> c.titlecase() }
        }
}

private fun normalizarComparacaoMagia(valor: String): String {
    val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
    return semAcento
        .lowercase()
        .replace(Regex("[^a-z0-9\\s]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun distanciaLevenshtein(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length
    val dp = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        var prevDiagonal = dp[0]
        dp[0] = i
        for (j in 1..b.length) {
            val prevUp = dp[j]
            val custo = if (a[i - 1] == b[j - 1]) 0 else 1
            dp[j] = minOf(
                dp[j] + 1,
                dp[j - 1] + 1,
                prevDiagonal + custo
            )
            prevDiagonal = prevUp
        }
    }
    return dp[b.length]
}

private fun String?.sanitized(default: String = ""): String {
    return this
        ?.fixMojibakeIfNeeded()
        ?.trim()
        .orEmpty()
        .ifBlank { default }
}

private fun String.fixMojibakeIfNeeded(): String {
    // Importante: "â" isolado é letra válida em português (ex.: Tolerância).
    // O detector deve olhar sequências típicas de mojibake, não letras isoladas.
    val markers = listOf("Ã", "Â", "â€", "â€“", "â€”", "â€œ", "â€\u009d", "â€™", "�")
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
