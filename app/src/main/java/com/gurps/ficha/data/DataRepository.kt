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

    val vantagens: List<VantagemDefinicao>
        get() = _vantagens ?: carregarVantagens().also { _vantagens = it }

    val desvantagens: List<DesvantagemDefinicao>
        get() = _desvantagens ?: carregarDesvantagens().also { _desvantagens = it }

    val pericias: List<PericiaDefinicao>
        get() = _pericias ?: carregarPericias().also { _pericias = it }

    val magias: List<MagiaDefinicao>
        get() = _magias ?: carregarMagias().also { _magias = it }

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
            val json = context.assets.open("desvantagens.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<DesvantagemDefinicao>>() {}.type
            (gson.fromJson<List<DesvantagemDefinicao>>(json, type) ?: emptyList()).map { it.normalizada() }
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
        tipoCusto: TipoCusto? = null
    ): List<DesvantagemDefinicao> {
        return desvantagens.filter { d ->
            val matchBusca = busca.isBlank() || d.nome.contains(busca, ignoreCase = true)
            val matchTipo = tipoCusto == null || d.tipoCusto == tipoCusto
            matchBusca && matchTipo
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
    custo = (custo as String?).sanitized()
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
    return this?.trim().orEmpty().ifBlank { default }
}

