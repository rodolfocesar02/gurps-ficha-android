package com.gurps.ficha.domain.loaders

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.gurps.ficha.model.ArmaCatalogoItem
import com.gurps.ficha.model.ModoDeArma

/**
 * **O leitor dos três catálogos de arma** (Lote ARMA-1).
 *
 * ## Por que saiu do `CatalogLoaders`
 *
 * Duas razões, nesta ordem:
 *
 * 1. **Tamanho.** O `CatalogLoaders.kt` estava em 1134 linhas, acima do teto de
 *    1000 que o projeto adotou. Alargar a leitura das armas lá dentro seria
 *    engordar o arquivo que já precisava emagrecer.
 * 2. **Teste.** Aqui a entrada é uma **String** de JSON, não um `Context` do
 *    Android. Isso deixa o parser rodar no teste de unidade contra o asset
 *    **real** — 150 armas, campo por campo. O `CatalogLoaders` continua dono de
 *    abrir o arquivo; este objeto só sabe ler texto.
 *
 * E foi o teste sobre o asset real que provou os dois furos que este lote
 * fecha: o `+N` da mira acoplada e o segundo modo de ataque.
 */
object ArmasCatalogLoader {

    const val ARQUIVO_CORPO_A_CORPO = "armas_corpo_a_corpo.v1.normalized.json"
    const val ARQUIVO_DISTANCIA = "armas_distancia.v1.normalized.json"
    const val ARQUIVO_FOGO = "armas_fogo.v1.normalized.json"

    const val TIPO_CORPO_A_CORPO = "corpo_a_corpo"
    const val TIPO_DISTANCIA = "distancia"
    const val TIPO_FOGO = "armas_de_fogo"

    // ==================================================================
    // Corpo a corpo
    // ==================================================================

    fun corpoACorpo(json: String): List<ArmaCatalogoItem> = itens(json) { obj ->
        val stObj = obj.obj("stMinimo")
        val stRaw = stObj?.str("raw").orEmpty()
        val modos = modosCorpoACorpo(obj)
        val primeiro = modos.firstOrNull()

        ArmaCatalogoItem(
            id = "cc_" + obj.str("id").orEmpty(),
            nome = obj.str("nome").orEmpty().sanitized(),
            tipoCombate = TIPO_CORPO_A_CORPO,
            categoria = obj.str("categoria").orEmpty().sanitized(),
            grupo = obj.str("grupo").orEmpty().sanitized(),
            stMinimo = stObj?.integer("valor"),
            // O `dano.raw` do catálogo traz os modos colados ("A/B"). O que a
            // ficha guarda continua sendo o PRIMEIRO — mudar isso quebraria o
            // cálculo de dano das fichas salvas. Os outros vivem em `modos`.
            danoRaw = primeiro?.danoRaw.orEmpty(),
            custoBase = primeiro?.custo,
            pesoBaseKg = primeiro?.pesoKg,
            aparar = primeiro?.aparar,
            observacoes = obj.str("observacoes").orEmpty().sanitized(),
            alcanceCorpoACorpo = primeiro?.alcanceCorpoACorpo,
            duasMaos = stRaw.contains("†") || stRaw.contains("‡") ||
                temFlag(stObj, "dagger") || temFlag(stObj, "double_dagger"),
            nt = obj.integer("nt"),
            custoRaw = obj.obj("raw")?.str("custo")?.sanitized(),
            pesoRaw = obj.obj("raw")?.str("peso")?.sanitized(),
            stFlags = flags(stObj),
            stRaw = stRaw.sanitized().takeIf { it.isNotBlank() },
            modos = modos
        )
    }

    /**
     * Os modos de ataque de uma arma corpo a corpo.
     *
     * ⚠️ **As duas listas do JSON não são simétricas.** Medindo o catálogo real:
     * 57 armas têm o mesmo número de modos nos dois lados, mas o *Arreador
     * Conjunto* tem 2 danos e 1 linha de alcance, e uma *Espada Bastarda em GdP*
     * tem 1 dano e 2 linhas. Por isso o laço vai até o **maior** dos dois e cada
     * lado cai para o índice 0 quando acaba — assumir simetria perderia
     * justamente as exceções.
     */
    private fun modosCorpoACorpo(obj: JsonObject): List<ModoDeArma> {
        val danoObj = obj.obj("dano")
        val danos = danosDeclarados(danoObj)
        val linhas = obj.arr("modos")?.mapNotNull { it.asObjOrNull() } ?: emptyList()
        val quantos = maxOf(danos.size, linhas.size).coerceAtLeast(1)

        return (0 until quantos).map { i ->
            val linha = linhas.getOrNull(i) ?: linhas.firstOrNull()
            val custoObj = linha?.obj("custo")
            val pesoObj = linha?.obj("peso")
            ModoDeArma(
                ordem = i + 1,
                danoRaw = (danos.getOrNull(i) ?: danos.firstOrNull().orEmpty()).sanitized(),
                alcanceCorpoACorpo = linha?.str("alcanceCorpo")?.sanitized(),
                aparar = linha?.str("aparar")?.sanitized(),
                // Do 2º modo em diante o livro escreve "—", e o catálogo traz
                // nulo. Fica nulo: é a mesma arma, não um item de graça.
                custo = custoObj?.decimal("valor"),
                pesoKg = pesoObj?.decimal("kg")
            )
        }
    }

    /**
     * A lista de danos declarados, já desgrudada.
     *
     * O normal é o catálogo trazer `dano.modos` pronto. Mas há uma linha em que
     * a barra separadora **sumiu na digitação** e os dois ataques vieram colados
     * num só: `"GeB+2 corteGdP+3 perf"`. Sem o desgrude, essa arma perderia a
     * estocada em silêncio — exatamente o defeito que este lote existe para
     * acabar.
     */
    private fun danosDeclarados(danoObj: JsonObject?): List<String> {
        val declarados = danoObj?.arr("modos")
            ?.mapNotNull { it.asObjOrNull()?.str("raw") }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        val base = declarados.ifEmpty { listOfNotNull(danoObj?.str("raw")).filter { it.isNotBlank() } }
        return base.flatMap { desgrudarDano(it) }
    }

    /** Regex do dano colado: um tipo de ferimento seguido direto de outra base. */
    private val DANO_COLADO = Regex(
        "(?<=[a-zçã])(?=(GeB|GdP|StB|StP)\\b)",
        RegexOption.IGNORE_CASE
    )

    internal fun desgrudarDano(raw: String): List<String> {
        val porBarra = raw.split("/").map { it.trim() }.filter { it.isNotBlank() }
        return porBarra.flatMap { parte ->
            DANO_COLADO.split(parte).map { it.trim() }.filter { it.isNotBlank() }
        }.ifEmpty { listOf(raw.trim()) }
    }

    // ==================================================================
    // À distância e armas de fogo
    // ==================================================================

    fun distancia(json: String, tipoCombate: String): List<ArmaCatalogoItem> = itens(json) { obj ->
        val stObj = obj.obj("stMinimo")
        val stRaw = stObj?.str("raw").orEmpty()
        val danoRaw = obj.obj("dano")?.str("raw").orEmpty().sanitized()
        val custoObj = obj.obj("custo")
        val pesoObj = obj.obj("peso")
        val alcObj = obj.obj("alcanceDistancia")
        val meioRaw = alcObj?.str("metadeDano")
        val maxRaw = alcObj?.str("maximo")
        // Alcance pode ser metros fixos ("75") ou múltiplo de ST ("×4", "×10/×15", p/ arcos/arremesso).
        val usaMultST = (meioRaw?.startsWith("×") == true) || (maxRaw?.startsWith("×") == true)
        val precisao = precisaoDe(obj.obj("precisao"))
        val grupo = obj.str("grupo").orEmpty().sanitized()

        ArmaCatalogoItem(
            id = "dist_" + obj.str("id").orEmpty(),
            nome = obj.str("nome").orEmpty().sanitized(),
            tipoCombate = tipoCombate,
            categoria = obj.str("categoria").orEmpty().sanitized(),
            grupo = grupo,
            stMinimo = stObj?.integer("valor"),
            danoRaw = danoRaw,
            custoBase = custoObj?.decimal("valor"),
            pesoBaseKg = pesoObj?.decimal("armaKg"),
            aparar = null,
            observacoes = obj.str("observacoes").orEmpty().sanitized(),
            precisao = precisao.first,
            meioDanoMetros = if (usaMultST) null else meioRaw?.emMetros(),
            maximoMetros = if (usaMultST) null else maxRaw?.emMetros(),
            alcanceMultStRaw = if (usaMultST) alcObj?.str("raw")?.sanitized() else null,
            cadenciaTiro = obj.obj("cdt")?.integer("valor"),
            tirosRaw = obj.obj("tiros")?.str("raw")?.sanitized(),
            magnitude = obj.obj("magnitude")?.integer("valor"),
            recuo = obj.obj("recuo")?.integer("valor"),
            // Duas mãos: † / ‡ na ST OU determinado pelo grupo (fogo: só pistola é 1 mão; arco/besta = 2). Lote 380.
            duasMaos = stRaw.contains("†") || stRaw.contains("‡") ||
                temFlag(stObj, "dagger") || temFlag(stObj, "double_dagger") ||
                ArmaCatalogoItem.duasMaosPorGrupo(tipoCombate, grupo),
            nt = obj.integer("nt"),
            cl = obj.obj("cl")?.integer("valor"),
            municaoKg = pesoObj?.decimal("municaoKg"),
            precisaoAcessorio = precisao.second,
            custoRaw = custoObj?.str("raw")?.sanitized(),
            pesoRaw = pesoObj?.str("raw")?.sanitized(),
            alcanceRaw = alcObj?.str("raw")?.sanitized(),
            stFlags = flags(stObj),
            stRaw = stRaw.sanitized().takeIf { it.isNotBlank() },
            modos = listOf(ModoDeArma(ordem = 1, danoRaw = danoRaw))
        )
    }

    /**
     * 🔴 Lê `"6+1"` e devolve **(6, 1)**: a Precisão da arma e a da mira acoplada.
     *
     * O campo `valor` do catálogo já traz só o 6 — foi ele que o app usava, e é
     * por isso que o `+1` sumia sem deixar rastro. O segundo número mora apenas
     * no `raw`.
     *
     * Formato sem `+` devolve o acessório **nulo**, não zero: "esta arma não tem
     * mira" e "a mira dá +0" são coisas diferentes na tela.
     */
    internal fun precisaoDe(precisaoObj: JsonObject?): Pair<Int?, Int?> {
        val valor = precisaoObj?.integer("valor")
        val raw = precisaoObj?.str("raw")?.trim().orEmpty()
        val casou = Regex("^(-?\\d+)\\s*\\+\\s*(\\d+)$").find(raw)
            ?: return valor to null
        val base = casou.groupValues[1].toIntOrNull() ?: valor
        val acessorio = casou.groupValues[2].toIntOrNull()
        return base to acessorio?.takeIf { it > 0 }
    }

    /**
     * 🔴 `"2.900"` é **dois mil e novecentos** metros. O ponto é milhar.
     *
     * O código antigo chamava `toIntOrNull()` direto, e `"2.900".toIntOrNull()`
     * devolve **null**. Medindo o catálogo real: **57 dos 124** valores de
     * alcance caíam nesse buraco — praticamente todo `Máx` de arma de fogo, que
     * passa de mil metros.
     *
     * O efeito na mesa era invisível e ruim: o aviso *"Fora de alcance"* do
     * diálogo de mira **nunca podia disparar** para pistola nenhuma, porque o
     * Máx dela era desconhecido. A tela mostrava só o 1/2D e ficava calada sobre
     * o resto.
     */
    private fun String.emMetros(): Int? =
        replace(".", "").replace(" ", "").trim().toIntOrNull()

    // ==================================================================
    // Encanamento
    // ==================================================================

    private fun itens(
        json: String,
        mapear: (JsonObject) -> ArmaCatalogoItem
    ): List<ArmaCatalogoItem> {
        val root = JsonParser.parseString(json)
        if (!root.isJsonObject) return emptyList()
        val items = root.asJsonObject.arr("items") ?: return emptyList()
        return items.mapNotNull { it.asObjOrNull() }
            .map(mapear)
            .filter { it.id.isNotBlank() && it.nome.isNotBlank() }
    }

    private fun flags(stObj: JsonObject?): List<String> =
        stObj?.arr("flags")?.mapNotNull { el ->
            runCatching { el.asString }.getOrNull()?.trim()?.takeIf { it.isNotBlank() }
        } ?: emptyList()

    private fun temFlag(stObj: JsonObject?, flag: String): Boolean =
        flags(stObj).any { it.equals(flag, ignoreCase = true) }

    private fun JsonElement.asObjOrNull(): JsonObject? =
        if (isJsonObject) asJsonObject else null

    private fun JsonObject.str(key: String): String? {
        val el = get(key) ?: return null
        if (el.isJsonNull) return null
        return runCatching {
            if (el.isJsonPrimitive && el.asJsonPrimitive.isString) el.asString else null
        }.getOrNull()
    }

    private fun JsonObject.integer(key: String): Int? {
        val el = get(key) ?: return null
        if (el.isJsonNull) return null
        return runCatching {
            when {
                el.isJsonPrimitive && el.asJsonPrimitive.isNumber -> el.asInt
                el.isJsonPrimitive && el.asJsonPrimitive.isString -> el.asString.trim().toIntOrNull()
                else -> null
            }
        }.getOrNull()
    }

    private fun JsonObject.decimal(key: String): Float? {
        val el = get(key) ?: return null
        if (el.isJsonNull) return null
        return runCatching {
            when {
                el.isJsonPrimitive && el.asJsonPrimitive.isNumber -> el.asFloat
                el.isJsonPrimitive && el.asJsonPrimitive.isString ->
                    el.asString.replace(",", ".").toFloatOrNull()
                else -> null
            }
        }.getOrNull()
    }

    private fun JsonObject.arr(key: String): JsonArray? {
        val el = get(key) ?: return null
        return if (el.isJsonArray) el.asJsonArray else null
    }

    private fun JsonObject.obj(key: String): JsonObject? {
        val el = get(key) ?: return null
        return if (el.isJsonObject) el.asJsonObject else null
    }
}
