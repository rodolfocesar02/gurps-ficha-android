package nexus.arcano

import java.text.Normalizer

data class ArcanoEstadoPersonagem(
    val magiasConhecidasIds: Set<String>,
    val am: Int,
    val iq: Int,
    val dx: Int = 0
)

data class ArcanoChave(
    val id: String,
    val descricao: String,
    val ativa: Boolean
)

data class ArcanoAcao(
    val magiaId: String,
    val motivo: String,
    val prioridade: Int
)

data class ArcanoResultado(
    val chavesAtivas: List<ArcanoChave>,
    val chavesFaltantes: List<ArcanoChave>,
    val proximasAcoes: List<ArcanoAcao>,
    val motivoBloqueio: String? = null,
    val motivoCodigo: String? = null
)

data class ArcanoRankingDiagnostico(
    val magiaId: String,
    val nome: String,
    val escola: String,
    val escolaNova: Boolean,
    val aprendivelAgora: Boolean,
    val custo: Int,
    val elegivel: Boolean,
    val motivoExclusao: String?
)

data class ArcanoCacheStats(
    val entradas: Int,
    val hits: Long,
    val misses: Long
)

data class ArcanoTimingStats(
    val amostras: Int,
    val mediaMs: Double,
    val p95Ms: Double,
    val maxMs: Double
)

interface ArcanoCatalogo {
    fun preRequisitoRaw(magiaId: String): String
    fun escolas(magiaId: String): List<String>
    fun nome(magiaId: String): String
    fun existe(magiaId: String): Boolean
    fun todasMagiasIds(): List<String>
}

class NexusArcanoEngine(
    private val catalogo: ArcanoCatalogo
) {
    data class RegraEscolas(
        val magiaOrigemId: String,
        val quantidadeEscolas: Int,
        val outrasEscolas: Boolean
    )

    data class RegraNumerica(
        val magiaOrigemId: String,
        val minAm: Int?,
        val minIq: Int?,
        val somaAtributos: List<String> = emptyList(),
        val minSoma: Int? = null
    )

    private data class AvaliacaoCandidata(
        val id: String,
        val escola: String,
        val escolaNova: Boolean,
        val custo: Int,
        val aprendivelAgora: Boolean,
        val escolaBloqueada: Boolean
    ) {
        val elegivel: Boolean
            get() = escola.isNotBlank() && aprendivelAgora && !escolaBloqueada
    }

    private data class CacheKey(
        val alvoId: String,
        val assinaturaKnown: String,
        val am: Int,
        val iq: Int,
        val dx: Int
    )

    private val cacheResultados = object : LinkedHashMap<CacheKey, ArcanoResultado>(128, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CacheKey, ArcanoResultado>?): Boolean {
            return size > 256
        }
    }
    private val cacheDiagnosticos = object : LinkedHashMap<CacheKey, List<ArcanoRankingDiagnostico>>(128, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CacheKey, List<ArcanoRankingDiagnostico>>?): Boolean {
            return size > 256
        }
    }
    private var cacheHits: Long = 0
    private var cacheMisses: Long = 0
    private val allMagiaIds: List<String> = catalogo.todasMagiasIds().sorted()
    private val nomeById: Map<String, String> = allMagiaIds.associateWith { catalogo.nome(it) }
    private val nomeNormById: Map<String, String> = allMagiaIds.associateWith { id -> normalize(nomeById[id].orEmpty()) }
    private val preRawById: Map<String, String> = allMagiaIds.associateWith { catalogo.preRequisitoRaw(it) }
    private val preNormById: Map<String, String> = allMagiaIds.associateWith { id -> normalize(preRawById[id].orEmpty()) }
    private val escolasById: Map<String, List<String>> = allMagiaIds.associateWith { catalogo.escolas(it) }
    private val escolasNormById: Map<String, List<String>> = allMagiaIds.associateWith { id ->
        escolasById[id].orEmpty().map(::normalize).filter { it.isNotBlank() }
    }
    private val escolaPrincipalNormById: Map<String, String> = allMagiaIds.associateWith { id ->
        escolasNormById[id].orEmpty().firstOrNull().orEmpty()
    }
    private val nomesNormalizadosPorTamanho: List<Pair<String, String>> = allMagiaIds
        .map { id -> id to nomeNormById[id].orEmpty() }
        .filter { (_, nomeNorm) -> nomeNorm.isNotBlank() }
        .sortedByDescending { it.second.length }

    private val regraEscolasRegex = Regex(
        "(\\d+)\\s*m\\s*a\\s*g\\s*i\\s*c\\s*a(?:s)?\\s*(?:em|de)\\s*(\\d+)\\s*(outras\\s+)?escolas(?:\\s+diferentes)?"
    )
    private val somaRegex = Regex("\\(([^\\)]*?)\\)\\s*:?\\s*(\\d+)\\+?", RegexOption.IGNORE_CASE)
    private val amRegex = Regex("\\bam\\s*(\\d+)\\b")
    private val iqRegex = Regex("\\biq\\s*(\\d+)\\b")

    private val dependenciasCache = mutableMapOf<String, List<String>>()
    private val regrasEscolasCache = mutableMapOf<String, List<RegraEscolas>>()
    private val regrasNumericasCache = mutableMapOf<String, List<RegraNumerica>>()
    private val cadeiaCache = mutableMapOf<String, List<String>>()
    private val temposRodadaNs = ArrayDeque<Long>()
    private val maxTempos = 512

    fun calcularEstadoAlvo(alvoId: String, estado: ArcanoEstadoPersonagem): ArcanoResultado {
        val t0 = System.nanoTime()
        try {
            val key = cacheKey(alvoId, estado.magiasConhecidasIds, estado)
            cacheResultados[key]?.let {
                cacheHits += 1
                return it
            }
            cacheMisses += 1

            if (!catalogo.existe(alvoId)) {
                val resultado = ArcanoResultado(
                    chavesAtivas = emptyList(),
                    chavesFaltantes = emptyList(),
                    proximasAcoes = emptyList(),
                    motivoBloqueio = "Alvo não encontrado no catálogo."
                )
                cacheResultados[key] = resultado
                return resultado
            }

            val known = estado.magiasConhecidasIds
            val cadeia = construirCadeiaObrigatoria(alvoId)
            val cadeiaSemAlvo = cadeia.filter { it != alvoId }
            val regrasEscolas = coletarRegrasEscolas(cadeia)
            val regrasNumericas = coletarRegrasNumericas(cadeia)

            val chaves = mutableListOf<ArcanoChave>()

            cadeiaSemAlvo.forEach { id ->
                chaves += ArcanoChave(
                    id = "chave_$id",
                    descricao = "Aprender ${nomeMagia(id)}",
                    ativa = id in known
                )
            }

            regrasEscolas.forEach { regra ->
                val ativa = atendeRegraEscolas(regra, known)
                chaves += ArcanoChave(
                    id = "chave_escolas_${regra.magiaOrigemId}_${regra.quantidadeEscolas}",
                    descricao = "Atender ${regra.quantidadeEscolas} escolas para ${nomeMagia(regra.magiaOrigemId)}",
                    ativa = ativa
                )
            }
            regrasNumericas.forEach { regra ->
                regra.minAm?.let { minAm ->
                    chaves += ArcanoChave(
                        id = "chave_am_${regra.magiaOrigemId}_$minAm",
                        descricao = "Ter AM $minAm para ${nomeMagia(regra.magiaOrigemId)}",
                        ativa = estado.am >= minAm
                    )
                }
                regra.minIq?.let { minIq ->
                    chaves += ArcanoChave(
                        id = "chave_iq_${regra.magiaOrigemId}_$minIq",
                        descricao = "Ter IQ $minIq para ${nomeMagia(regra.magiaOrigemId)}",
                        ativa = estado.iq >= minIq
                    )
                }
                if (regra.somaAtributos.isNotEmpty() && regra.minSoma != null) {
                    val somaAtual = regra.somaAtributos.sumOf { valorAtributo(it, estado) }
                    chaves += ArcanoChave(
                        id = "chave_soma_${regra.magiaOrigemId}_${regra.somaAtributos.joinToString("_")}_${regra.minSoma}",
                        descricao = "Ter ${regra.somaAtributos.joinToString("+").uppercase()} >= ${regra.minSoma} para ${nomeMagia(regra.magiaOrigemId)}",
                        ativa = somaAtual >= regra.minSoma
                    )
                }
            }

            val alvoLiberado = magiaAprendivelAgora(alvoId, known, estado)
            chaves += ArcanoChave(
                id = "chave_alvo_$alvoId",
                descricao = "Liberar ${nomeMagia(alvoId)}",
                ativa = alvoLiberado || alvoId in known
            )

            val proximas = sugerirProximasAcoes(alvoId, known, cadeiaSemAlvo, estado)
            val ativas = chaves.filter { it.ativa }
            val faltantes = chaves.filterNot { it.ativa }

            val bloqueioNumerico = primeiroBloqueioNumerico(alvoId, cadeiaSemAlvo, estado, known)
            val bloqueio = if (proximas.isEmpty() && faltantes.isNotEmpty()) {
                bloqueioNumerico ?: "Sem ação imediata. Verifique chaves pendentes."
            } else {
                null
            }
            val codigo = if (bloqueio != null) {
                codigoBloqueio(bloqueioNumerico, faltantes)
            } else {
                null
            }

            val resultado = ArcanoResultado(
                chavesAtivas = ativas,
                chavesFaltantes = faltantes,
                proximasAcoes = proximas,
                motivoBloqueio = bloqueio,
                motivoCodigo = codigo
            )
            cacheResultados[key] = resultado
            return resultado
        } finally {
            registrarTempoRodada(System.nanoTime() - t0)
        }
    }

    fun diagnosticarRankingAlvo(alvoId: String, estado: ArcanoEstadoPersonagem): List<ArcanoRankingDiagnostico> {
        val t0 = System.nanoTime()
        try {
            val key = cacheKey(alvoId, estado.magiasConhecidasIds, estado)
            cacheDiagnosticos[key]?.let {
                cacheHits += 1
                return it
            }
            cacheMisses += 1

            if (!catalogo.existe(alvoId)) {
                cacheDiagnosticos[key] = emptyList()
                return emptyList()
            }

            val known = estado.magiasConhecidasIds
            val cadeia = construirCadeiaObrigatoria(alvoId)
            val cadeiaSemAlvo = cadeia.filter { it != alvoId }

            val proximaObrigatoria = cadeiaSemAlvo.firstOrNull { it !in known }
            val focoMagia = when {
                proximaObrigatoria != null &&
                    !magiaAprendivelAgora(proximaObrigatoria, known, estado) &&
                    bloqueioNumericoParaMagia(proximaObrigatoria, estado) == null -> proximaObrigatoria
                proximaObrigatoria == null &&
                    alvoId !in known &&
                    !magiaAprendivelAgora(alvoId, known, estado) &&
                    bloqueioNumericoParaMagia(alvoId, estado) == null -> alvoId
                else -> null
            } ?: run {
                cacheDiagnosticos[key] = emptyList()
                return emptyList()
            }

            val idsProibidos = if (focoMagia == proximaObrigatoria) {
                cadeiaSemAlvo.toSet() + alvoId
            } else {
                setOf(alvoId)
            }

            val diag = avaliarCandidatasParaRegraDeEscolas(
                magiaId = focoMagia,
                known = known,
                estado = estado,
                idsProibidos = idsProibidos
            ).sortedWith(
                compareByDescending<AvaliacaoCandidata> { it.elegivel }
                    .thenByDescending { it.escolaNova }
                    .thenBy { it.custo }
                    .thenBy { nomeMagiaNorm(it.id) }
            ).map { cand ->
                ArcanoRankingDiagnostico(
                    magiaId = cand.id,
                    nome = nomeMagia(cand.id),
                    escola = cand.escola,
                    escolaNova = cand.escolaNova,
                    aprendivelAgora = cand.aprendivelAgora,
                    custo = cand.custo,
                    elegivel = cand.elegivel,
                    motivoExclusao = when {
                        cand.escola.isBlank() -> "SEM_ESCOLA"
                        !cand.aprendivelAgora -> "NAO_APRENDIVEL_AGORA"
                        cand.escolaBloqueada -> "ESCOLA_DA_ORIGEM_BLOQUEADA"
                        else -> null
                    }
                )
            }
            cacheDiagnosticos[key] = diag
            return diag
        } finally {
            registrarTempoRodada(System.nanoTime() - t0)
        }
    }

    private fun sugerirProximasAcoes(
        alvoId: String,
        known: Set<String>,
        cadeiaSemAlvo: List<String>,
        estado: ArcanoEstadoPersonagem
    ): List<ArcanoAcao> {
        val out = mutableListOf<ArcanoAcao>()

        val proximaObrigatoria = cadeiaSemAlvo.firstOrNull { it !in known }
        if (proximaObrigatoria != null) {
            if (magiaAprendivelAgora(proximaObrigatoria, known, estado)) {
                out += ArcanoAcao(
                    magiaId = proximaObrigatoria,
                    motivo = "Cadeia obrigatória",
                    prioridade = 0
                )
            } else if (bloqueioNumericoParaMagia(proximaObrigatoria, estado) != null) {
                // Bloqueio por atributo/AM: não sugerir escola como falso avanço.
            } else {
                out += sugerirParaRegraDeEscolas(
                    magiaId = proximaObrigatoria,
                    known = known,
                    estado = estado,
                    idsProibidos = cadeiaSemAlvo.toSet() + alvoId
                )
            }
        } else {
            if (alvoId !in known) {
                if (magiaAprendivelAgora(alvoId, known, estado)) {
                    out += ArcanoAcao(
                        magiaId = alvoId,
                        motivo = "Alvo liberado",
                        prioridade = 0
                    )
                } else if (bloqueioNumericoParaMagia(alvoId, estado) != null) {
                    // Bloqueio por atributo/AM: sem sugestão de escola.
                } else {
                    out += sugerirParaRegraDeEscolas(
                        magiaId = alvoId,
                        known = known,
                        estado = estado,
                        idsProibidos = setOf(alvoId)
                    )
                }
            }
        }

        return out
            .filter { it.magiaId !in known }
            .distinctBy { it.magiaId }
            .sortedWith(compareBy<ArcanoAcao> { it.prioridade }.thenBy { nomeMagiaNorm(it.magiaId) })
            .take(3)
    }

    private fun sugerirParaRegraDeEscolas(
        magiaId: String,
        known: Set<String>,
        estado: ArcanoEstadoPersonagem,
        idsProibidos: Set<String>
    ): List<ArcanoAcao> {
        val regras = coletarRegrasEscolas(listOf(magiaId))
        if (regras.isEmpty()) return emptyList()

        val avaliados = avaliarCandidatasParaRegraDeEscolas(
            magiaId = magiaId,
            known = known,
            estado = estado,
            idsProibidos = idsProibidos
        ).filter { it.elegivel }

        val escolasUsadasNaRodada = mutableSetOf<String>()
        val ordenados = avaliados.sortedWith(
            compareByDescending<AvaliacaoCandidata> { it.escolaNova }
                .thenBy { it.custo }
                .thenBy { nomeMagiaNorm(it.id) }
        )

        val out = mutableListOf<ArcanoAcao>()
        // Passo 1: escolas novas sem repetição.
        ordenados.forEach { cand ->
            if (out.size >= 3) return@forEach
            if (!cand.escolaNova) return@forEach
            if (cand.escola in escolasUsadasNaRodada) return@forEach
            out += ArcanoAcao(
                magiaId = cand.id,
                motivo = "Abrir escola para liberar ${nomeMagia(magiaId)}",
                prioridade = 1 + cand.custo
            )
            escolasUsadasNaRodada += cand.escola
        }
        // Passo 2: fallback robusto para completar 3 ações (mesmo com escola repetida).
        ordenados.forEach { cand ->
            if (out.size >= 3) return@forEach
            if (out.any { it.magiaId == cand.id }) return@forEach
            if (cand.escola in escolasUsadasNaRodada) return@forEach
            out += ArcanoAcao(
                magiaId = cand.id,
                motivo = "Fallback de progresso para ${nomeMagia(magiaId)}",
                prioridade = 10 + cand.custo
            )
            escolasUsadasNaRodada += cand.escola
        }
        // Passo 3: último recurso com repetição permitida.
        ordenados.forEach { cand ->
            if (out.size >= 3) return@forEach
            if (out.any { it.magiaId == cand.id }) return@forEach
            out += ArcanoAcao(
                magiaId = cand.id,
                motivo = "Fallback final para ${nomeMagia(magiaId)}",
                prioridade = 20 + cand.custo
            )
        }
        return out
    }

    private fun avaliarCandidatasParaRegraDeEscolas(
        magiaId: String,
        known: Set<String>,
        estado: ArcanoEstadoPersonagem,
        idsProibidos: Set<String>
    ): List<AvaliacaoCandidata> {
        val regras = coletarRegrasEscolas(listOf(magiaId))
        if (regras.isEmpty()) return emptyList()

        val escolasConhecidas = escolasConhecidas(known)
        val escolasProibidas = regras
            .asSequence()
            .filter { it.outrasEscolas }
            .flatMap { regra -> escolasNorm(regra.magiaOrigemId).asSequence() }
            .toSet()

        fun custoDesbloqueio(candId: String): Int {
            val depsMissing = dependenciasNomeadas(candId).count { it !in known }
            val regraNum = coletarRegrasNumericas(listOf(candId)).firstOrNull()
            val faltaNum = if (regraNum == null) 0 else {
                if (atendeRegraNumerica(regraNum, estado)) 0 else 1
            }
            val regraEsc = coletarRegrasEscolas(listOf(candId)).firstOrNull()
            val faltaEsc = if (regraEsc == null) 0 else {
                val count = escolasConhecidas.size
                (regraEsc.quantidadeEscolas - count).coerceAtLeast(0)
            }
            val complexidadeBase = if (preNorm(candId).isBlank()) 0 else 1
            return depsMissing * 3 + faltaNum * 5 + faltaEsc * 2 + complexidadeBase
        }

        return allMagiaIds
            .asSequence()
            .filter { it !in known && it != magiaId && it !in idsProibidos }
            .map { candId ->
                val escola = escolaPrincipalNorm(candId)
                val escolaNova = escola.isNotBlank() && escola !in escolasConhecidas
                AvaliacaoCandidata(
                    id = candId,
                    escola = escola,
                    escolaNova = escolaNova,
                    custo = custoDesbloqueio(candId),
                    aprendivelAgora = magiaAprendivelAgora(candId, known, estado),
                    escolaBloqueada = escola in escolasProibidas
                )
            }.toList()
    }

    private fun magiaAprendivelAgora(magiaId: String, known: Set<String>, estado: ArcanoEstadoPersonagem): Boolean {
        if (magiaId in known) return true
        val deps = dependenciasNomeadas(magiaId)
        if (deps.any { it !in known }) return false
        if (!coletarRegrasEscolas(listOf(magiaId)).all { atendeRegraEscolas(it, known) }) return false
        return coletarRegrasNumericas(listOf(magiaId)).all { atendeRegraNumerica(it, estado) }
    }

    private fun atendeRegraEscolas(regra: RegraEscolas, known: Set<String>): Boolean {
        val set = escolasConhecidas(known).toMutableSet()
        if (regra.outrasEscolas) {
            val daMagia = escolasNorm(regra.magiaOrigemId).toSet()
            set.removeAll(daMagia)
        }
        return set.size >= regra.quantidadeEscolas
    }

    private fun escolasConhecidas(known: Set<String>): Set<String> {
        return known
            .asSequence()
            .flatMap { id -> escolasNorm(id).asSequence() }
            .toSet()
    }

    private fun atendeRegraNumerica(regra: RegraNumerica, estado: ArcanoEstadoPersonagem): Boolean {
        val okAm = regra.minAm?.let { estado.am >= it } ?: true
        val okIq = regra.minIq?.let { estado.iq >= it } ?: true
        val okSoma = if (regra.somaAtributos.isNotEmpty() && regra.minSoma != null) {
            regra.somaAtributos.sumOf { valorAtributo(it, estado) } >= regra.minSoma
        } else {
            true
        }
        return okAm && okIq && okSoma
    }

    private fun construirCadeiaObrigatoria(alvoId: String): List<String> {
        return cadeiaCache.getOrPut(alvoId) {
            val visit = mutableSetOf<String>()
            val ordem = mutableListOf<String>()

            fun dfs(id: String) {
                if (!visit.add(id)) return
                dependenciasNomeadas(id).forEach(::dfs)
                ordem += id
            }

            dfs(alvoId)
            ordem
        }
    }

    private fun dependenciasNomeadas(magiaId: String): List<String> {
        return dependenciasCache.getOrPut(magiaId) {
            val raw = preNorm(magiaId)
            if (raw.isBlank()) return@getOrPut emptyList()

            val out = linkedSetOf<String>()
            val rangesAceitos = mutableListOf<IntRange>()
            nomesNormalizadosPorTamanho.forEach { (id, nomeNorm) ->
                if (id == magiaId) return@forEach
                val rgx = Regex("\\b${Regex.escape(nomeNorm)}\\b")
                val match = rgx.find(raw) ?: return@forEach
                val range = match.range
                val sobreposto = rangesAceitos.any { r -> range.first <= r.last && r.first <= range.last }
                if (sobreposto) return@forEach
                if (!pareceReferenciaDeEscola(raw, match.range.first, match.range.last + 1)) {
                    out += id
                    rangesAceitos += range
                }
            }
            out.toList()
        }
    }

    private fun coletarRegrasEscolas(ids: List<String>): List<RegraEscolas> {
        return ids
            .distinct()
            .flatMap { regrasEscolasPorMagia(it) }
    }

    private fun coletarRegrasNumericas(ids: List<String>): List<RegraNumerica> {
        return ids
            .distinct()
            .flatMap { regrasNumericasPorMagia(it) }
    }

    private fun regrasEscolasPorMagia(magiaId: String): List<RegraEscolas> {
        return regrasEscolasCache.getOrPut(magiaId) {
            val raw = preNorm(magiaId)
            regraEscolasRegex
                .findAll(raw)
                .mapNotNull { m ->
                    val qtd = m.groupValues[2].toIntOrNull() ?: return@mapNotNull null
                    RegraEscolas(
                        magiaOrigemId = magiaId,
                        quantidadeEscolas = qtd,
                        outrasEscolas = m.groupValues[3].isNotBlank()
                    )
                }
                .toList()
        }
    }

    private fun regrasNumericasPorMagia(magiaId: String): List<RegraNumerica> {
        return regrasNumericasCache.getOrPut(magiaId) {
            val rawOriginal = preRaw(magiaId)
            val raw = preNorm(magiaId)
            val somaMatch = somaRegex.find(rawOriginal)
            val somaAtributos = somaMatch
                ?.groupValues
                ?.getOrNull(1)
                ?.split("+")
                ?.map(::normalize)
                ?.filter { it in setOf("dx", "iq", "st", "ht", "am") }
                ?: emptyList()
            val minSoma = somaMatch?.groupValues?.getOrNull(2)?.toIntOrNull()
            val rawSemSoma = if (somaMatch != null) {
                val attrsNorm = somaAtributos.joinToString(" ")
                if (attrsNorm.isNotBlank() && minSoma != null) {
                    raw.replace(Regex("\\b${Regex.escape(attrsNorm)}\\s*${Regex.escape(minSoma.toString())}\\b"), " ")
                } else {
                    raw
                }
            } else {
                raw
            }
            val am = amRegex.find(rawSemSoma)?.groupValues?.getOrNull(1)?.toIntOrNull()
            val iqEncontrado = iqRegex.find(rawSemSoma)?.groupValues?.getOrNull(1)?.toIntOrNull()
            val iq = if (somaAtributos.contains("iq") && minSoma != null && iqEncontrado == minSoma) null else iqEncontrado
            if (am != null || iq != null || (somaAtributos.isNotEmpty() && minSoma != null)) {
                listOf(
                    RegraNumerica(
                        magiaOrigemId = magiaId,
                        minAm = am,
                        minIq = iq,
                        somaAtributos = somaAtributos,
                        minSoma = minSoma
                    )
                )
            } else {
                emptyList()
            }
        }
    }

    private fun bloqueioNumericoParaMagia(magiaId: String, estado: ArcanoEstadoPersonagem): String? {
        val regra = coletarRegrasNumericas(listOf(magiaId)).firstOrNull() ?: return null
        val faltas = mutableListOf<String>()
        regra.minAm?.let { if (estado.am < it) faltas += "AM >= $it" }
        regra.minIq?.let { if (estado.iq < it) faltas += "IQ >= $it" }
        if (regra.somaAtributos.isNotEmpty() && regra.minSoma != null) {
            val somaAtual = regra.somaAtributos.sumOf { valorAtributo(it, estado) }
            if (somaAtual < regra.minSoma) {
                faltas += "${regra.somaAtributos.joinToString("+").uppercase()} >= ${regra.minSoma}"
            }
        }
        if (faltas.isEmpty()) return null
        return "Falta ${faltas.joinToString(" e ")} para ${nomeMagia(magiaId)}."
    }

    private fun primeiroBloqueioNumerico(
        alvoId: String,
        cadeiaSemAlvo: List<String>,
        estado: ArcanoEstadoPersonagem,
        known: Set<String>
    ): String? {
        val alvoDeBloqueio = cadeiaSemAlvo.firstOrNull { it !in known } ?: alvoId
        return bloqueioNumericoParaMagia(alvoDeBloqueio, estado)
    }

    private fun codigoBloqueio(
        bloqueioNumerico: String?,
        faltantes: List<ArcanoChave>
    ): String {
        if (bloqueioNumerico != null) return "NUMERIC_GATE"
        val primeiro = faltantes.firstOrNull()?.id.orEmpty()
        return when {
            primeiro.startsWith("chave_escolas_") -> "SCHOOL_COUNT_PENDING"
            primeiro.startsWith("chave_") && !primeiro.startsWith("chave_alvo_") -> "CHAIN_PENDING"
            primeiro.startsWith("chave_alvo_") -> "TARGET_PENDING"
            else -> "UNKNOWN_BLOCK"
        }
    }

    fun limparCache() {
        cacheResultados.clear()
        cacheDiagnosticos.clear()
        cacheHits = 0
        cacheMisses = 0
        temposRodadaNs.clear()
    }

    fun invalidarCachePorMagia(magiaId: String) {
        val token = "|$magiaId|"
        cacheResultados.keys.removeIf { token in it.assinaturaKnown }
        cacheDiagnosticos.keys.removeIf { token in it.assinaturaKnown }
    }

    fun cacheStats(): ArcanoCacheStats {
        val entradas = cacheResultados.size + cacheDiagnosticos.size
        return ArcanoCacheStats(
            entradas = entradas,
            hits = cacheHits,
            misses = cacheMisses
        )
    }

    fun timingStats(): ArcanoTimingStats {
        if (temposRodadaNs.isEmpty()) {
            return ArcanoTimingStats(
                amostras = 0,
                mediaMs = 0.0,
                p95Ms = 0.0,
                maxMs = 0.0
            )
        }
        val asMs = temposRodadaNs.map { it / 1_000_000.0 }.sorted()
        val media = asMs.average()
        val p95Idx = ((asMs.size - 1) * 0.95).toInt().coerceIn(0, asMs.size - 1)
        val max = asMs.last()
        return ArcanoTimingStats(
            amostras = asMs.size,
            mediaMs = media,
            p95Ms = asMs[p95Idx],
            maxMs = max
        )
    }

    private fun registrarTempoRodada(deltaNs: Long) {
        if (deltaNs < 0) return
        temposRodadaNs.addLast(deltaNs)
        while (temposRodadaNs.size > maxTempos) {
            temposRodadaNs.removeFirst()
        }
    }

    private fun cacheKey(
        alvoId: String,
        known: Set<String>,
        estado: ArcanoEstadoPersonagem
    ): CacheKey {
        val assinaturaKnown = known.sorted().joinToString(separator = "|", prefix = "|", postfix = "|")
        return CacheKey(
            alvoId = alvoId,
            assinaturaKnown = assinaturaKnown,
            am = estado.am,
            iq = estado.iq,
            dx = estado.dx
        )
    }

    private fun nomeMagia(magiaId: String): String = nomeById[magiaId] ?: catalogo.nome(magiaId)

    private fun nomeMagiaNorm(magiaId: String): String = nomeNormById[magiaId] ?: normalize(nomeMagia(magiaId))

    private fun preRaw(magiaId: String): String = preRawById[magiaId] ?: catalogo.preRequisitoRaw(magiaId)

    private fun preNorm(magiaId: String): String = preNormById[magiaId] ?: normalize(preRaw(magiaId))

    private fun escolasNorm(magiaId: String): List<String> = escolasNormById[magiaId]
        ?: catalogo.escolas(magiaId).map(::normalize).filter { it.isNotBlank() }

    private fun escolaPrincipalNorm(magiaId: String): String = escolaPrincipalNormById[magiaId]
        ?: escolasNorm(magiaId).firstOrNull().orEmpty()

    private fun normalize(raw: String): String {
        val semAcento = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun pareceReferenciaDeEscola(raw: String, start: Int, endExclusive: Int): Boolean {
        val antes = raw.substring(0, start).takeLast(24)
        val depois = raw.substring(endExclusive).take(24)
        val contextoAntes = normalize(antes)
        val contextoDepois = normalize(depois)
        val padroesAntes = listOf(
            "magica de",
            "magicas de",
            "magica em",
            "magicas em",
            "de",
            "em"
        )
        val padroesDepois = listOf(
            "escola",
            "escolas",
            "outras escolas"
        )
        val forteAntes = padroesAntes.any { contextoAntes.endsWith(it) }
        val forteDepois = padroesDepois.any { contextoDepois.startsWith(it) }
        return forteAntes || forteDepois
    }

    private fun valorAtributo(nome: String, estado: ArcanoEstadoPersonagem): Int {
        return when (normalize(nome)) {
            "am" -> estado.am
            "iq" -> estado.iq
            "dx" -> estado.dx
            else -> 0
        }
    }
}
