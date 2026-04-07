package com.gurps.ficha.domain.loaders

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.*
import java.text.Normalizer
import com.gurps.ficha.data.DataRepository

private val gson = Gson()

class CatalogLoaders(private val context: Context) {
    val loadErrors = mutableMapOf<String, String>()

    fun clearLoadError(catalog: String) {
        synchronized(loadErrors) { loadErrors.remove(catalog) }
    }

    fun registerLoadError(catalog: String, throwable: Throwable) {
        synchronized(loadErrors) {
            loadErrors[catalog] = throwable.message ?: throwable::class.java.simpleName
        }
    }

    var vantagensArtesMarciaisIds: Set<String> = emptySet()
        private set

    fun carregarVantagens(): List<VantagemDefinicao> {
        return try {
            carregarVantagensV3()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun carregarVantagensV3(): List<VantagemDefinicao> {
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

    fun carregarVantagensExtrasArtesMarciaisV1(): List<VantagemDefinicao> {
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
            vantagensArtesMarciaisIds = parsed
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

    fun carregarDesvantagens(): List<DesvantagemDefinicao> {
        return try {
            carregarDesvantagensV2()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun carregarDesvantagensV2(): List<DesvantagemDefinicao> {
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

    fun carregarPericias(): List<PericiaDefinicao> {
        return try {
            val json = context.assets.open("pericias.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<PericiaDefinicao>>() {}.type
            val parsed = (gson.fromJson<List<PericiaDefinicao>>(json, type) ?: emptyList())
                .map { it.normalizada() }
                .map { DataRepository.getInstance(context).aplicarRegraPericiaV2(it, DataRepository.getInstance(context).regraPericiaV2(it.id)) }
            
            android.util.Log.d("CatalogLoaders", "Pericias carregadas: ${parsed.size}")
            clearLoadError("pericias")
            parsed
        } catch (e: Exception) {
            android.util.Log.e("CatalogLoaders", "Erro loading pericias: ${e.message}")
            registerLoadError("pericias", e)
            e.printStackTrace()
            emptyList()
        }
    }

    fun carregarPericiasV2Rules(): Map<String, PericiaV2RuleMapItem> {
        return try {
            val json = context.assets.open("pericias_v2_rules_map.json")
                .bufferedReader()
                .use { it.readText() }
            android.util.Log.d("CatalogLoaders", "Rules map JSON size: ${json.length}")
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

    fun carregarPericiasSuplementares(): List<PericiaSuplementarItem> {
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

    fun carregarMagias(): List<MagiaDefinicao> {
        return try {
            val json = context.assets.open("magias2versao.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<MagiaDefinicao>>() {}.type
            val parsed = (gson.fromJson<List<MagiaDefinicao>>(json, type) ?: emptyList())
                .map { it.normalizada() }
                .map { magia ->
                    magia.copy(
                        preRequisitos = DataRepository.getInstance(context).preRequisitoCanonicoTexto(magia.id, magia.preRequisitos)
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

    fun carregarTecnicasCatalogo(): List<TecnicaCatalogoItem> {
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

    fun carregarArmasCatalogo(): List<ArmaCatalogoItem> {
        val cc = carregarArmasCorpoACorpoNormalizadas()
        val dist = carregarArmasDistanciaNormalizadas()
        return (cc + dist).sortedBy { it.nome.lowercase() }
    }

    fun carregarArmasCorpoACorpoNormalizadas(): List<ArmaCatalogoItem> {
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

    fun carregarArmasDistanciaNormalizadas(): List<ArmaCatalogoItem> {
        val arquivos = listOf(
            "armas_distancia.v1.normalized.json" to "distancia",
            "armas_fogo.v1.normalized.json" to "armas_de_fogo"
        )
        return arquivos.flatMap { (arquivo, tipo) -> carregarArmasDistanciaDeArquivo(arquivo, tipo) }
    }

    fun carregarArmasDistanciaDeArquivo(
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

    fun carregarEscudosCatalogo(): List<EscudoCatalogoItem> {
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

    fun carregarArmadurasCatalogo(): List<ArmaduraCatalogoItem> {
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

    fun carregarModificadoresGerais(): List<ModificadorDefinicao> {
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

}

// Extensions

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

fun String?.sanitized(default: String = ""): String {
    return this
        ?.fixMojibakeIfNeeded()
        ?.trim()
        .orEmpty()
        .ifBlank { default }
}

fun String.fixMojibakeIfNeeded(): String {
    // Agora que todos os assets foram limpos e convertidos para UTF-8 nativo,
    // retornamos 'this' diretamente para evitar corrupção de acentos legítimos.
    return this
}

/*
    // Importante: "â" isolado é letra válida em português (ex.: Tolerância).
    // O detector deve olhar sequências típicas de mojibake, não letras isoladas.
    val markers = listOf("Ã", "", "â€", "–", "—", "“", "â€\u009d", "’", "�")
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
*/
