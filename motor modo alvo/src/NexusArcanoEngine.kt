package nexus.arcano

/**
 * ==================================================================================
 * 🛡️ BLINDAGEM DO MOTOR NEXUS ARCANO (MODO ALVO) 🛡️
 * ==================================================================================
 * ESTE CÓDIGO CONTÉM A LÓGICA DE "GENIALIDADE" DE BUSCA PROATIVA.
 * 
 * DIRETRIZES DE MANUTENÇÃO:
 * 1. NUNCA remova o Lookahead de Metas (sugerirProximasAcoes): Ele antecipa requisitos
 *    de magias futuras (ex: 15 escolas para Desejo) mesmo que a magia atual não peça.
 * 2. NUNCA simplifique 'custoAproximadoDependencia': Ele deve ser recursivo para
 *    encontrar o caminho REAL mais curto, e não apenas o número de nomes imediatos.
 * 3. NUNCA desabilite os Caches (cacheResultados, custoAproximadoCache): Eles são
 *    vitais para a performance em tempo real no Android.
 * 4. PRIORIDADE 'FASTEST-FIRST': O primeiro item da lista DEVE ser sempre o atalho
 *    matemático mais curto.
 * ==================================================================================
 */

import com.gurps.ficha.regras_prerequisitos.PreRequisitoParser
import com.gurps.ficha.regras_prerequisitos.PreRequisitoType
import java.security.MessageDigest
import java.text.Normalizer


class NexusArcanoEngine(
    internal val catalogo: ArcanoCatalogo
) {
    internal data class NomeVariante(
        val id: String,
        val nome: String
    )

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

    internal data class RequisitoBranch(
        val dependencias: List<String>,
        val regrasEscolas: List<RegraEscolas>,
        val regrasNumericas: List<RegraNumerica>
    )

    internal data class AvaliacaoCandidata(
        val id: String,
        val escola: String,
        val escolaNova: Boolean,
        val custo: Int,
        val aprendivelAgora: Boolean,
        val escolaBloqueadaOrigem: Boolean,
        val escolaBloqueadaPolitica: Boolean
    ) {
        val elegivel: Boolean
            get() = escola.isNotBlank() &&
                aprendivelAgora &&
                !escolaBloqueadaOrigem &&
                !escolaBloqueadaPolitica
    }

    internal data class CacheKey(
        val alvoId: String,
        val assinaturaKnown: String,
        val am: Int,
        val iq: Int,
        val dx: Int
    )

    internal data class SnapshotAlvo(
        val alvoId: String,
        val cadeiaSemAlvo: List<String>,
        val regrasEscolas: List<RegraEscolas>,
        val regrasNumericas: List<RegraNumerica>
    )

    internal val cacheResultados = object : LinkedHashMap<CacheKey, ArcanoResultado>(512, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CacheKey, ArcanoResultado>?): Boolean {
            return size > 2048
        }
    }
    internal val cacheDiagnosticos = object : LinkedHashMap<CacheKey, List<ArcanoRankingDiagnostico>>(512, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CacheKey, List<ArcanoRankingDiagnostico>>?): Boolean {
            return size > 2048
        }
    }
    internal val cachePlanos = object : LinkedHashMap<CacheKey, ArcanoPlanoResultado>(256, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CacheKey, ArcanoPlanoResultado>?): Boolean {
            return size > 1024
        }
    }
    internal var cacheHits: Long = 0
    internal var cacheMisses: Long = 0
    internal val allMagiaIds: List<String> = catalogo.todasMagiasIds().sorted()
    internal val nomeById: Map<String, String> = allMagiaIds.associateWith { catalogo.nome(it) }
    internal val nomeNormById: Map<String, String> = allMagiaIds.associateWith { id -> normalize(nomeById[id].orEmpty()) }
    internal val preRawById: Map<String, String> = allMagiaIds.associateWith { catalogo.preRequisitoRaw(it) }
    internal val preNormById: Map<String, String> = allMagiaIds.associateWith { id -> normalize(preRawById[id].orEmpty()) }
    internal val escolasById: Map<String, List<String>> = allMagiaIds.associateWith { catalogo.escolas(it) }
    internal val escolasNormById: Map<String, List<String>> = allMagiaIds.associateWith { id ->
        escolasById[id].orEmpty().map(::normalize).filter { it.isNotBlank() }
    }
    internal val escolaPrincipalNormById: Map<String, String> = allMagiaIds.associateWith { id ->
        escolasNormById[id].orEmpty().firstOrNull().orEmpty()
    }
    internal val nomesNormalizadosPorTamanho: List<NomeVariante> = allMagiaIds
        .asSequence()
        .flatMap { id ->
            val nomeNorm = nomeNormById[id].orEmpty()
            if (nomeNorm.isBlank()) {
                emptySequence()
            } else {
                variantesSingularPlural(nomeNorm)
                    .asSequence()
                    .map { variante -> NomeVariante(id = id, nome = variante) }
            }
        }
        .distinctBy { "${it.id}|${it.nome}" }
        .sortedByDescending { it.nome.length }
        .toList()

    internal val regraEscolasRegex = Regex(
        "([a-z0-9]+)\\s*m\\s*a\\s*g\\s*i(?:\\s*c)?\\s*a(?:s)?\\s*(?:em|de)\\s*([a-z0-9]+)\\s*(outras\\s+)?escolas(?:\\s+diferentes)?"
    )
    internal val somaRegex = Regex("\\(([^\\)]*?)\\)\\s*:?\\s*(\\d+)\\+?", RegexOption.IGNORE_CASE)
    internal val amRegex = Regex("\\bam\\s*(\\d+)\\b")
    internal val iqRegex = Regex("\\biq\\s*(\\d+)\\b")
    internal val pesoDepsMissing = 6
    internal val pesoFaltaNumerica = 8
    internal val pesoFaltaEscolas = 3
    internal val pesoComplexidadeBase = 4
    internal val custoBasePlano = 1
    internal val penalidadePlanoEscolaRepetida = 4
    internal val penalidadePlanoSemReducaoMeta = 12
    internal val escolasNuncaRecomendar = setOf("tecnologica")
    internal val maxGruposDependencias = 96

    internal val dependenciasCache = mutableMapOf<String, List<String>>()
    internal val dependenciasGruposCache = mutableMapOf<String, List<List<String>>>()
    internal val requisitoBranchesCache = mutableMapOf<String, List<RequisitoBranch>>()
    internal val regrasEscolasCache = mutableMapOf<String, List<RegraEscolas>>()
    internal val regrasNumericasCache = mutableMapOf<String, List<RegraNumerica>>()
    internal val cadeiaCache = mutableMapOf<String, List<String>>()
    internal val snapshotCache = mutableMapOf<String, SnapshotAlvo>()
    internal val temposRodadaNs = ArrayDeque<Long>()
    internal val maxTempos = 512
    internal val alvosComRegraEscolas: Set<String> by lazy {
        allMagiaIds.filter { regrasEscolasPorMagia(it).isNotEmpty() }.toSet()
    }
    internal val dependentesDiretosByMagia: Map<String, Set<String>> by lazy {
        val out = mutableMapOf<String, MutableSet<String>>()
        allMagiaIds.forEach { alvo ->
            dependenciasNomeadas(alvo).forEach { dep ->
                out.getOrPut(dep) { mutableSetOf() }.add(alvo)
            }
        }
        out.mapValues { it.value.toSet() }
    }
    internal val custoAproximadoCache = mutableMapOf<String, Int>()
    internal val cadeiaEstadoCache = mutableMapOf<String, List<String>>()
    internal val dependentesTransitivosByMagia: Map<String, Set<String>> by lazy {
        allMagiaIds.associateWith { magiaId ->
            val visit = mutableSetOf<String>()
            val queue = ArrayDeque<String>()
            queue.addLast(magiaId)
            while (queue.isNotEmpty()) {
                val atual = queue.removeFirst()
                dependentesDiretosByMagia[atual].orEmpty().forEach { dep ->
                    if (visit.add(dep)) queue.addLast(dep)
                }
            }
            visit.toSet()
        }
    }

    fun calcularEstadoAlvo(alvoId: String, estado: ArcanoEstadoPersonagem): ArcanoResultado {
        custoAproximadoCache.clear()
        cadeiaEstadoCache.clear()
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
            val snapshot = snapshotAlvo(alvoId)
            val cadeiaSemAlvo = snapshot.cadeiaSemAlvo
            val regrasEscolas = snapshot.regrasEscolas
            val regrasNumericas = snapshot.regrasNumericas

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

            val cadeiaSemAlvoEfetiva = construirCadeiaObrigatoriaParaEstado(alvoId, known).filter { it != alvoId }
            val proximas = sugerirProximasAcoes(alvoId, known, estado)
            val ativas = chaves.filter { it.ativa }
            val faltantes = chaves.filterNot { it.ativa }

            val bloqueioNumerico = primeiroBloqueioNumerico(alvoId, cadeiaSemAlvoEfetiva, estado, known)
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
        custoAproximadoCache.clear()
        cadeiaEstadoCache.clear()
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
                    bloqueioNumericoParaMagia(proximaObrigatoria, estado, known) == null -> proximaObrigatoria
                proximaObrigatoria == null &&
                    alvoId !in known &&
                    !magiaAprendivelAgora(alvoId, known, estado) &&
                    bloqueioNumericoParaMagia(alvoId, estado, known) == null -> alvoId
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
                        cand.escolaBloqueadaOrigem -> "ESCOLA_DA_ORIGEM_BLOQUEADA"
                        cand.escolaBloqueadaPolitica -> "ESCOLA_BLOQUEADA_POLITICA"
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

    fun diagnosticarMetasAlvo(alvoId: String, estado: ArcanoEstadoPersonagem): List<ArcanoMetaProgress> {
        custoAproximadoCache.clear()
        cadeiaEstadoCache.clear()
        if (!catalogo.existe(alvoId)) return emptyList()

        val known = estado.magiasConhecidasIds
        val cadeia = construirCadeiaObrigatoriaParaEstado(alvoId, known)
        val cadeiaSemAlvo = cadeia.filter { it != alvoId }
        val metas = mutableListOf<ArcanoMetaProgress>()

        cadeiaSemAlvo.distinct().forEach { magiaId ->
            metas += ArcanoMetaProgress(
                id = "meta_cadeia_$magiaId",
                tipo = ArcanoMetaTipo.CADEIA_MAGIA,
                origemMagiaId = magiaId,
                descricao = "Aprender ${nomeMagia(magiaId)}",
                requerido = 1,
                atual = if (magiaId in known) 1 else 0,
                atendida = magiaId in known
            )
        }

        coletarRegrasEscolas(cadeia).forEach { regra ->
            val escolasAtuais = escolasConhecidas(known).toMutableSet().also { set ->
                if (regra.outrasEscolas) {
                    set.removeAll(escolasNorm(regra.magiaOrigemId).toSet())
                }
            }.size
            metas += ArcanoMetaProgress(
                id = "meta_escolas_${regra.magiaOrigemId}_${regra.quantidadeEscolas}_${if (regra.outrasEscolas) 1 else 0}",
                tipo = ArcanoMetaTipo.ESCOLAS_DISTINTAS,
                origemMagiaId = regra.magiaOrigemId,
                descricao = "Escolas para ${nomeMagia(regra.magiaOrigemId)}",
                requerido = regra.quantidadeEscolas,
                atual = escolasAtuais.coerceAtMost(regra.quantidadeEscolas),
                atendida = escolasAtuais >= regra.quantidadeEscolas,
                bloqueadaPorUpstream = dependenciasMinimasPendentes(regra.magiaOrigemId, known) > 0
            )
        }

        coletarRegrasNumericas(cadeia).forEach { regra ->
            regra.minAm?.let { minAm ->
                metas += ArcanoMetaProgress(
                    id = "meta_am_${regra.magiaOrigemId}_$minAm",
                    tipo = ArcanoMetaTipo.NUMERICO_AM,
                    origemMagiaId = regra.magiaOrigemId,
                    descricao = "AM para ${nomeMagia(regra.magiaOrigemId)}",
                    requerido = minAm,
                    atual = estado.am.coerceAtMost(minAm),
                    atendida = estado.am >= minAm,
                    bloqueadaPorUpstream = dependenciasMinimasPendentes(regra.magiaOrigemId, known) > 0
                )
            }
            regra.minIq?.let { minIq ->
                metas += ArcanoMetaProgress(
                    id = "meta_iq_${regra.magiaOrigemId}_$minIq",
                    tipo = ArcanoMetaTipo.NUMERICO_IQ,
                    origemMagiaId = regra.magiaOrigemId,
                    descricao = "IQ para ${nomeMagia(regra.magiaOrigemId)}",
                    requerido = minIq,
                    atual = estado.iq.coerceAtMost(minIq),
                    atendida = estado.iq >= minIq,
                    bloqueadaPorUpstream = dependenciasMinimasPendentes(regra.magiaOrigemId, known) > 0
                )
            }
            if (regra.somaAtributos.isNotEmpty() && regra.minSoma != null) {
                val somaAtual = regra.somaAtributos.sumOf { valorAtributo(it, estado) }
                metas += ArcanoMetaProgress(
                    id = "meta_soma_${regra.magiaOrigemId}_${regra.somaAtributos.joinToString("_")}_${regra.minSoma}",
                    tipo = ArcanoMetaTipo.NUMERICO_SOMA,
                    origemMagiaId = regra.magiaOrigemId,
                    descricao = "${regra.somaAtributos.joinToString("+").uppercase()} para ${nomeMagia(regra.magiaOrigemId)}",
                    requerido = regra.minSoma,
                    atual = somaAtual.coerceAtMost(regra.minSoma),
                    atendida = somaAtual >= regra.minSoma,
                    bloqueadaPorUpstream = dependenciasMinimasPendentes(regra.magiaOrigemId, known) > 0
                )
            }
        }

        val alvoAtendido = magiaAprendivelAgora(alvoId, known, estado) || alvoId in known
        metas += ArcanoMetaProgress(
            id = "meta_alvo_$alvoId",
            tipo = ArcanoMetaTipo.ALVO_LIBERADO,
            origemMagiaId = alvoId,
            descricao = "Liberar ${nomeMagia(alvoId)}",
            requerido = 1,
            atual = if (alvoAtendido) 1 else 0,
            atendida = alvoAtendido,
            bloqueadaPorUpstream = dependenciasMinimasPendentes(alvoId, known) > 0
        )

        return metas
            .distinctBy { it.id }
            .sortedWith(compareBy<ArcanoMetaProgress> { it.tipo.ordinal }.thenBy { it.id })
    }

    fun checksumMetasAlvo(alvoId: String, estado: ArcanoEstadoPersonagem): String {
        val metas = diagnosticarMetasAlvo(alvoId, estado)
        if (metas.isEmpty()) return "v1:SEM_METAS"
        val canonical = metas.joinToString("|") { meta ->
            listOf(
                meta.id,
                meta.tipo.name,
                meta.origemMagiaId,
                meta.requerido.toString(),
                meta.atual.toString(),
                if (meta.atendida) "1" else "0",
                if (meta.bloqueadaPorUpstream) "1" else "0"
            ).joinToString(":")
        }
        return "v1:${sha256Hex(canonical)}"
    }


    fun calcularEstadoAlvoIncremental(
        alvoId: String,
        estadoAnterior: ArcanoEstadoPersonagem,
        resultadoAnterior: ArcanoResultado,
        estadoNovo: ArcanoEstadoPersonagem
    ): ArcanoDeltaResultado {
        if (!catalogo.existe(alvoId)) {
            return ArcanoDeltaResultado(
                resultado = calcularEstadoAlvo(alvoId, estadoNovo),
                modo = "FULL_TARGET_NOT_FOUND",
                chavesRecalculadas = 0
            )
        }
        val attrsMudaram = estadoAnterior.am != estadoNovo.am ||
            estadoAnterior.iq != estadoNovo.iq ||
            estadoAnterior.dx != estadoNovo.dx
        val knownAntes = estadoAnterior.magiasConhecidasIds
        val knownNovo = estadoNovo.magiasConhecidasIds
        val knownMudou = knownAntes != knownNovo
        if (!attrsMudaram && !knownMudou) {
            return ArcanoDeltaResultado(
                resultado = resultadoAnterior,
                modo = "NO_CHANGES",
                chavesRecalculadas = 0
            )
        }

        val snapshot = snapshotAlvo(alvoId)
        val expectedIds = chavesEsperadasIds(snapshot)
        val prevMap = (resultadoAnterior.chavesAtivas + resultadoAnterior.chavesFaltantes).associateBy { it.id }
        if (!expectedIds.all { it in prevMap }) {
            return ArcanoDeltaResultado(
                resultado = calcularEstadoAlvo(alvoId, estadoNovo),
                modo = "FULL_PREV_MISMATCH",
                chavesRecalculadas = 0
            )
        }

        val novasChaves = prevMap.toMutableMap()
        var recalculadas = 0
        var escolasMudaram = false
        if (knownMudou) {
            val changedKnown = (knownAntes - knownNovo) + (knownNovo - knownAntes)
            changedKnown.forEach { magiaId ->
                val keyId = "chave_$magiaId"
                val old = novasChaves[keyId]
                if (old != null) {
                    novasChaves[keyId] = old.copy(ativa = magiaId in knownNovo)
                    recalculadas += 1
                }
            }
        }

        if (knownMudou) {
            val escolasAntes = escolasConhecidas(knownAntes)
            val escolasAgora = escolasConhecidas(knownNovo)
            escolasMudaram = escolasAntes != escolasAgora
            if (escolasAntes != escolasAgora) {
                snapshot.regrasEscolas.forEach { regra ->
                    val keyId = "chave_escolas_${regra.magiaOrigemId}_${regra.quantidadeEscolas}"
                    val old = novasChaves[keyId]
                    if (old != null) {
                        novasChaves[keyId] = old.copy(ativa = atendeRegraEscolas(regra, knownNovo))
                        recalculadas += 1
                    }
                }
            }
        }

        if (attrsMudaram) {
            snapshot.regrasNumericas.forEach { regra ->
                regra.minAm?.let { minAm ->
                    val keyId = "chave_am_${regra.magiaOrigemId}_$minAm"
                    val old = novasChaves[keyId]
                    if (old != null) {
                        novasChaves[keyId] = old.copy(ativa = estadoNovo.am >= minAm)
                        recalculadas += 1
                    }
                }
                regra.minIq?.let { minIq ->
                    val keyId = "chave_iq_${regra.magiaOrigemId}_$minIq"
                    val old = novasChaves[keyId]
                    if (old != null) {
                        novasChaves[keyId] = old.copy(ativa = estadoNovo.iq >= minIq)
                        recalculadas += 1
                    }
                }
                if (regra.somaAtributos.isNotEmpty() && regra.minSoma != null) {
                    val keyId = "chave_soma_${regra.magiaOrigemId}_${regra.somaAtributos.joinToString("_")}_${regra.minSoma}"
                    val old = novasChaves[keyId]
                    if (old != null) {
                        val somaAtual = regra.somaAtributos.sumOf { valorAtributo(it, estadoNovo) }
                        novasChaves[keyId] = old.copy(ativa = somaAtual >= regra.minSoma)
                        recalculadas += 1
                    }
                }
            }
        }

        val changedKnown = if (knownMudou) (knownAntes - knownNovo) + (knownNovo - knownAntes) else emptySet()
        val impactaDependenciaAlvo = changedKnown.any { mudanca ->
            mudanca in snapshot.cadeiaSemAlvo ||
                mudanca == alvoId ||
                alvoId in alvosDependentesTransitivos(mudanca)
        }
        val precisaRecalcularDerivados = attrsMudaram || escolasMudaram || impactaDependenciaAlvo || knownMudou

        val chaveAlvoId = "chave_alvo_$alvoId"
        if (precisaRecalcularDerivados) {
            novasChaves[chaveAlvoId]?.let { old ->
                val ativaAlvo = magiaAprendivelAgora(alvoId, knownNovo, estadoNovo) || alvoId in knownNovo
                novasChaves[chaveAlvoId] = old.copy(ativa = ativaAlvo)
                recalculadas += 1
            }
        }

        val ordemEsperada = chavesEsperadasOrdem(snapshot)
        val chavesOrdenadas = ordemEsperada.mapNotNull { novasChaves[it] }
        val ativas = chavesOrdenadas.filter { it.ativa }
        val faltantes = chavesOrdenadas.filterNot { it.ativa }
        val proximas: List<ArcanoAcao>
        val bloqueio: String?
        val codigo: String?
        if (precisaRecalcularDerivados) {
            val cadeiaSemAlvoEfetiva = construirCadeiaObrigatoriaParaEstado(alvoId, knownNovo).filter { it != alvoId }
            proximas = sugerirProximasAcoes(alvoId, knownNovo, estadoNovo)
            val bloqueioNumerico = primeiroBloqueioNumerico(alvoId, cadeiaSemAlvoEfetiva, estadoNovo, knownNovo)
            bloqueio = if (proximas.isEmpty() && faltantes.isNotEmpty()) {
                bloqueioNumerico ?: "Sem ação imediata. Verifique chaves pendentes."
            } else {
                null
            }
            codigo = if (bloqueio != null) {
                codigoBloqueio(bloqueioNumerico, faltantes)
            } else {
                null
            }
        } else {
            proximas = resultadoAnterior.proximasAcoes
            bloqueio = resultadoAnterior.motivoBloqueio
            codigo = resultadoAnterior.motivoCodigo
        }
        val modo = when {
            knownMudou && attrsMudaram -> "INCREMENTAL_KNOWN_ATTR"
            knownMudou -> "INCREMENTAL_KNOWN_ONLY"
            attrsMudaram -> "INCREMENTAL_ATTR_ONLY"
            !precisaRecalcularDerivados -> "INCREMENTAL_NO_IMPACT"
            else -> "NO_CHANGES"
        }

        return ArcanoDeltaResultado(
            resultado = ArcanoResultado(
                chavesAtivas = ativas,
                chavesFaltantes = faltantes,
                proximasAcoes = proximas,
                motivoBloqueio = bloqueio,
                motivoCodigo = codigo
            ),
            modo = modo,
            chavesRecalculadas = recalculadas
        )
    }

    internal fun sugerirProximasAcoes(
        alvoId: String,
        known: Set<String>,
        estado: ArcanoEstadoPersonagem
    ): List<ArcanoAcao> {
        val cadeiaIntegral = construirCadeiaObrigatoriaParaEstado(alvoId, known)
        val pendentes = cadeiaIntegral.filter { it !in known }
        val out = mutableListOf<ArcanoAcao>()
        
        // 1. Prioridade Máxima: Próximas magias da trilha que já podem ser aprendidas agora
        val imediatas = pendentes.filter { magiaAprendivelAgora(it, known, estado) && !escolaBloqueadaPorPolitica(it) }
        imediatas.take(2).forEach { id ->
            out += ArcanoAcao(id, "Cadeia obrigatória (Caminho mais rápido)", 0)
        }

        // 2. Lookahead: Buscar a primeira meta de escolas pendente em qualquer ponto da cadeia
        // Ordenamos por proximidade do aprendizado (melhor primeiro)
        pendentes.forEach { magiaId ->
            if (out.size >= 5) return@forEach
            if (bloqueioNumericoParaMagia(magiaId, estado, known) == null) {
                // Se a magia atual já tem escolas suficientes, não sugerimos mais para ELA, pulamos para a próxima pendente
                if (atendeRegraEscolas(regrasEscolasRelevantes(magiaId, known, estado).firstOrNull() ?: return@forEach, known)) return@forEach

                val sugestoes = sugerirParaRegraDeEscolas(
                    magiaId = magiaId,
                    known = known,
                    estado = estado,
                    idsProibidos = pendentes.toSet() + out.map { it.magiaId }.toSet()
                )
                if (sugestoes.isNotEmpty()) {
                    out.addAll(sugestoes)
                    if (out.size >= 4) return@forEach
                }
            }
        }

        return out
            .filter { it.magiaId !in known }
            .distinctBy { it.magiaId }
            .sortedWith(compareBy<ArcanoAcao> { it.prioridade }.thenBy { nomeMagiaNorm(it.magiaId) })
            .take(5)
    }

    internal fun sugerirParaRegraDeEscolas(
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
                .thenBy { tieBreakPorAlvo(magiaId, it.id) }
                .thenBy { nomeMagiaNorm(it.id) }
        )
        val temEscolaNovaElegivel = ordenados.any { it.escolaNova }
        val motivoSemEscolaNova = "Sem escola nova aprendivel agora para ${nomeMagia(magiaId)}"

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
                motivo = if (temEscolaNovaElegivel) {
                    "Fallback de progresso para ${nomeMagia(magiaId)}"
                } else {
                    "$motivoSemEscolaNova; fallback de progresso"
                },
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
                motivo = if (temEscolaNovaElegivel) {
                    "Fallback final para ${nomeMagia(magiaId)}"
                } else {
                    "$motivoSemEscolaNova; fallback final"
                },
                prioridade = 20 + cand.custo
            )
        }
        return out
    }

    internal fun avaliarCandidatasParaRegraDeEscolas(
        magiaId: String,
        known: Set<String>,
        estado: ArcanoEstadoPersonagem,
        idsProibidos: Set<String>
    ): List<AvaliacaoCandidata> {
        val regras = regrasEscolasRelevantes(magiaId, known, estado)
        if (regras.isEmpty()) return emptyList()

        val escolasConhecidas = escolasConhecidas(known)
        val escolasProibidas = regras
            .asSequence()
            .filter { it.outrasEscolas }
            .flatMap { regra -> escolasNorm(regra.magiaOrigemId).asSequence() }
            .toSet()

        fun custoDesbloqueio(candId: String): Int {
            val depsMissing = dependenciasMinimasPendentes(candId, known)
            val regraNum = regrasNumericasRelevantes(candId, known, estado).firstOrNull()
            val faltaNum = if (regraNum == null) 0 else {
                if (atendeRegraNumerica(regraNum, estado)) 0 else 1
            }
            val regraEsc = regrasEscolasRelevantes(candId, known, estado).firstOrNull()
            val faltaEsc = if (regraEsc == null) 0 else {
                val count = escolasConhecidas.size
                (regraEsc.quantidadeEscolas - count).coerceAtLeast(0)
            }
            val complexidadeBase = if (preRequisitoSemConteudo(candId)) 0 else 1
            return depsMissing * pesoDepsMissing +
                faltaNum * pesoFaltaNumerica +
                faltaEsc * pesoFaltaEscolas +
                complexidadeBase * pesoComplexidadeBase
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
                    escolaBloqueadaOrigem = escola in escolasProibidas,
                    escolaBloqueadaPolitica = escola in escolasNuncaRecomendar
                )
            }.toList()
    }

    internal fun magiaAprendivelAgora(magiaId: String, known: Set<String>, estado: ArcanoEstadoPersonagem): Boolean {
        if (magiaId in known) return true
        val branches = requisitoBranchesPorMagia(magiaId)
        if (branches.isNotEmpty()) {
            return branches.any { branch ->
                branch.dependencias.all { it in known } &&
                    branch.regrasEscolas.all { atendeRegraEscolas(it, known) } &&
                    branch.regrasNumericas.all { atendeRegraNumerica(it, estado) }
            }
        }

        val gruposDeps = dependenciasNomeadasGrupos(magiaId)
        val depsOk = if (gruposDeps.isEmpty()) true else gruposDeps.any { grupo -> grupo.all { it in known } }
        if (!depsOk) return false
        if (!coletarRegrasEscolas(listOf(magiaId)).all { atendeRegraEscolas(it, known) }) return false
        return coletarRegrasNumericas(listOf(magiaId)).all { atendeRegraNumerica(it, estado) }
    }

    internal fun regrasEscolasRelevantes(
        magiaId: String,
        known: Set<String>,
        estado: ArcanoEstadoPersonagem
    ): List<RegraEscolas> {
        val branch = escolherBranchRelevante(magiaId, known, estado)
        if (branch != null && branch.regrasEscolas.isNotEmpty()) return branch.regrasEscolas
        return coletarRegrasEscolas(listOf(magiaId))
    }

    internal fun regrasNumericasRelevantes(
        magiaId: String,
        known: Set<String>,
        estado: ArcanoEstadoPersonagem
    ): List<RegraNumerica> {
        val branch = escolherBranchRelevante(magiaId, known, estado)
        if (branch != null && branch.regrasNumericas.isNotEmpty()) return branch.regrasNumericas
        return coletarRegrasNumericas(listOf(magiaId))
    }

    internal fun escolherBranchRelevante(
        magiaId: String,
        known: Set<String>,
        estado: ArcanoEstadoPersonagem
    ): RequisitoBranch? {
        val branches = requisitoBranchesPorMagia(magiaId)
        if (branches.isEmpty()) return null
        return branches.minWithOrNull(
            compareBy<RequisitoBranch> { branch -> branch.dependencias.count { it !in known } }
                .thenBy { branch -> branch.regrasEscolas.count { !atendeRegraEscolas(it, known) } }
                .thenBy { branch -> branch.regrasNumericas.count { !atendeRegraNumerica(it, estado) } }
                .thenBy { it.dependencias.size }
                .thenBy { it.regrasEscolas.size }
                .thenBy { it.regrasNumericas.size }
                .thenBy { branchKey(it) }
        )
    }

    internal fun atendeRegraEscolas(regra: RegraEscolas, known: Set<String>): Boolean {
        val set = escolasConhecidas(known).toMutableSet()
        if (regra.outrasEscolas) {
            val daMagia = escolasNorm(regra.magiaOrigemId).toSet()
            set.removeAll(daMagia)
        }
        return set.size >= regra.quantidadeEscolas
    }

    internal fun escolasConhecidas(known: Set<String>): Set<String> {
        return known
            .asSequence()
            .flatMap { id -> escolasNorm(id).asSequence() }
            .toSet()
    }

    internal fun atendeRegraNumerica(regra: RegraNumerica, estado: ArcanoEstadoPersonagem): Boolean {
        val okAm = regra.minAm?.let { estado.am >= it } ?: true
        val okIq = regra.minIq?.let { estado.iq >= it } ?: true
        val okSoma = if (regra.somaAtributos.isNotEmpty() && regra.minSoma != null) {
            regra.somaAtributos.sumOf { valorAtributo(it, estado) } >= regra.minSoma
        } else {
            true
        }
        return okAm && okIq && okSoma
    }

    internal fun construirCadeiaObrigatoria(alvoId: String): List<String> {
        return cadeiaCache.getOrPut(alvoId) {
            val visit = mutableSetOf<String>()
            val ordem = mutableListOf<String>()

            fun dfs(id: String) {
                if (!visit.add(id)) return
                dependenciasNomeadasGrupos(id)
                    .flatMap { it }
                    .distinct()
                    .forEach(::dfs)
                ordem += id
            }

            dfs(alvoId)
            ordem
        }
    }

    internal fun construirCadeiaObrigatoriaParaEstado(alvoId: String, known: Set<String>): List<String> {
        val cacheKey = "$alvoId|${known.size}|${known.hashCode()}"
        cadeiaEstadoCache[cacheKey]?.let { return it }

        val visit = mutableSetOf<String>()
        val ordem = mutableListOf<String>()

        fun dfs(id: String) {
            if (!visit.add(id)) return
            dependenciasEscolhidasParaEstado(id, known).forEach(::dfs)
            ordem += id
        }

        dfs(alvoId)
        return ordem.also { cadeiaEstadoCache[cacheKey] = it }
    }

    internal fun dependenciasNomeadas(magiaId: String): List<String> {
        return dependenciasCache.getOrPut(magiaId) {
            dependenciasNomeadasGrupos(magiaId).flatMap { it }.distinct()
        }
    }

    internal fun dependenciasNomeadasGrupos(magiaId: String): List<List<String>> {
        return dependenciasGruposCache.getOrPut(magiaId) {
            val gruposPorBranch = requisitoBranchesPorMagia(magiaId)
                .map { it.dependencias }
                .filter { it.isNotEmpty() }
                .distinctBy { it.joinToString("|") }
            if (gruposPorBranch.isNotEmpty()) {
                return@getOrPut gruposPorBranch
            }

            val gruposPorParser = dependenciasNomeadasGruposPorParser(magiaId)
            if (gruposPorParser.isNotEmpty()) {
                return@getOrPut gruposPorParser
            }

            val raw = preNorm(magiaId)
            if (raw.isBlank()) return@getOrPut emptyList()

            val depsRawCompleto = extrairDependenciasNomeadas(raw, magiaId)
            val partesOu = raw.split(Regex("\\s+ou\\s+")).map { it.trim() }.filter { it.isNotBlank() }
            if (partesOu.size <= 1) {
                return@getOrPut if (depsRawCompleto.isEmpty()) emptyList() else listOf(depsRawCompleto)
            }

            val grupos = partesOu
                .map { parte -> extrairDependenciasNomeadas(parte, magiaId) }
                .filter { it.isNotEmpty() }
                .distinctBy { it.joinToString("|") }

            if (grupos.isEmpty()) {
                if (depsRawCompleto.isEmpty()) emptyList() else listOf(depsRawCompleto)
            } else {
                grupos
            }
        }
    }

    internal fun dependenciasNomeadasGruposPorParser(magiaId: String): List<List<String>> {
        val raw = preRaw(magiaId).trim()
        if (raw.isBlank()) return emptyList()

        val parsed = PreRequisitoParser.parse(raw)
        if (parsed.terms.isEmpty()) return emptyList()

        val alternativasPorTermo = parsed.terms
            .mapNotNull { termo ->
                val alternativas = termo.alternatives
                    .mapNotNull { alternativa ->
                        val deps = linkedSetOf<String>()
                        alternativa
                            .filterIsInstance<PreRequisitoType.MagiaConhecida>()
                            .forEach { token ->
                                resolverDependenciasNomeadasToken(token.nomeMagia, magiaId)
                                    .forEach { deps += it }
                            }
                        deps.takeIf { it.isNotEmpty() }?.toList()
                    }
                    .distinctBy { it.joinToString("|") }
                alternativas.takeIf { it.isNotEmpty() }
            }

        if (alternativasPorTermo.isEmpty()) return emptyList()
        return combinarAlternativasDependencias(alternativasPorTermo)
    }

    internal fun resolverDependenciasNomeadasToken(tokenRaw: String, magiaId: String): List<String> {
        val tokenNorm = normalize(tokenRaw)
        if (tokenNorm.isBlank()) return emptyList()

        val matchDireto = nomesNormalizadosPorTamanho
            .asSequence()
            .filter { it.id != magiaId && it.nome == tokenNorm }
            .map { it.id }
            .distinct()
            .toList()
        if (matchDireto.isNotEmpty()) return matchDireto

        return extrairDependenciasNomeadas(tokenNorm, magiaId)
    }

    internal fun combinarAlternativasDependencias(
        alternativasPorTermo: List<List<List<String>>>
    ): List<List<String>> {
        var acumulado = listOf(emptyList<String>())

        alternativasPorTermo.forEach { alternativas ->
            val prox = linkedMapOf<String, List<String>>()
            loop@ for (base in acumulado) {
                for (alt in alternativas) {
                    val combinado = (base + alt).distinct()
                    if (combinado.isEmpty()) continue
                    val key = combinado.joinToString("|")
                    prox.putIfAbsent(key, combinado)
                    if (prox.size >= maxGruposDependencias) break@loop
                }
            }
            acumulado = prox.values.toList()
            if (acumulado.isEmpty()) return emptyList()
        }

        return acumulado
            .filter { it.isNotEmpty() }
            .distinctBy { it.joinToString("|") }
    }

    internal fun requisitoBranchesPorMagia(magiaId: String): List<RequisitoBranch> {
        return requisitoBranchesCache.getOrPut(magiaId) {
            val raw = preRaw(magiaId).trim()
            if (raw.isBlank()) {
                return@getOrPut listOf(
                    RequisitoBranch(
                        dependencias = emptyList(),
                        regrasEscolas = emptyList(),
                        regrasNumericas = emptyList()
                    )
                )
            }

            val parsed = PreRequisitoParser.parse(raw)
            if (parsed.terms.isEmpty()) {
                val depsFallback = extrairDependenciasNomeadas(preNorm(magiaId), magiaId)
                val escolasFallback = regrasEscolasPorMagia(magiaId)
                val numericasFallback = regrasNumericasPorMagia(magiaId)
                if (depsFallback.isEmpty() && escolasFallback.isEmpty() && numericasFallback.isEmpty()) {
                    return@getOrPut emptyList()
                }
                return@getOrPut listOf(
                    RequisitoBranch(
                        dependencias = depsFallback,
                        regrasEscolas = escolasFallback,
                        regrasNumericas = numericasFallback
                    )
                )
            }

            val alternativasPorTermo = parsed.terms.map { termo ->
                val alternativas = termo.alternatives
                    .map { alternativa -> branchFromAlternative(magiaId, alternativa) }
                    .ifEmpty {
                        listOf(
                            RequisitoBranch(
                                dependencias = emptyList(),
                                regrasEscolas = emptyList(),
                                regrasNumericas = emptyList()
                            )
                        )
                    }
                    .distinctBy { branchKey(it) }
                alternativas
            }

            combinarBranches(alternativasPorTermo)
        }
    }

    internal fun branchFromAlternative(
        magiaId: String,
        alternativa: List<PreRequisitoType>
    ): RequisitoBranch {
        val deps = linkedSetOf<String>()
        val regrasEscolas = mutableListOf<RegraEscolas>()
        val regrasNumericas = mutableListOf<RegraNumerica>()

        alternativa.forEach { tipo ->
            when (tipo) {
                is PreRequisitoType.MagiaConhecida -> {
                    resolverDependenciasNomeadasToken(tipo.nomeMagia, magiaId)
                        .forEach { deps += it }
                }
                is PreRequisitoType.MagiasEmEscolasDiferentes -> {
                    regrasEscolas += RegraEscolas(
                        magiaOrigemId = magiaId,
                        quantidadeEscolas = tipo.escolasDiferentes,
                        outrasEscolas = tipo.outrasEscolas
                    )
                }
                is PreRequisitoType.AptidaoMagica -> {
                    regrasNumericas += RegraNumerica(
                        magiaOrigemId = magiaId,
                        minAm = tipo.nivel,
                        minIq = null
                    )
                }
                is PreRequisitoType.AttributeMin -> {
                    if (normalize(tipo.atributo) == "iq") {
                        regrasNumericas += RegraNumerica(
                            magiaOrigemId = magiaId,
                            minAm = null,
                            minIq = tipo.minimo
                        )
                    }
                }
                is PreRequisitoType.AtributosSomaMin -> {
                    regrasNumericas += RegraNumerica(
                        magiaOrigemId = magiaId,
                        minAm = null,
                        minIq = null,
                        somaAtributos = tipo.atributos.map(::normalize).filter { it.isNotBlank() },
                        minSoma = tipo.minimo
                    )
                }
                else -> Unit
            }
        }

        return RequisitoBranch(
            dependencias = deps.toList(),
            regrasEscolas = regrasEscolas.distinct(),
            regrasNumericas = regrasNumericas.distinct()
        )
    }

    internal fun combinarBranches(alternativasPorTermo: List<List<RequisitoBranch>>): List<RequisitoBranch> {
        var acumulado = listOf(
            RequisitoBranch(
                dependencias = emptyList(),
                regrasEscolas = emptyList(),
                regrasNumericas = emptyList()
            )
        )

        alternativasPorTermo.forEach { alternativas ->
            val prox = linkedMapOf<String, RequisitoBranch>()
            loop@ for (base in acumulado) {
                for (alt in alternativas) {
                    val combinado = RequisitoBranch(
                        dependencias = (base.dependencias + alt.dependencias).distinct(),
                        regrasEscolas = (base.regrasEscolas + alt.regrasEscolas).distinct(),
                        regrasNumericas = (base.regrasNumericas + alt.regrasNumericas).distinct()
                    )
                    prox.putIfAbsent(branchKey(combinado), combinado)
                    if (prox.size >= maxGruposDependencias) break@loop
                }
            }
            acumulado = prox.values.toList()
            if (acumulado.isEmpty()) return emptyList()
        }

        return acumulado
            .distinctBy { branchKey(it) }
    }

    internal fun branchKey(branch: RequisitoBranch): String {
        val deps = branch.dependencias.sorted().joinToString(",")
        val escolas = branch.regrasEscolas
            .sortedWith(compareBy<RegraEscolas> { it.magiaOrigemId }.thenBy { it.quantidadeEscolas }.thenBy { it.outrasEscolas })
            .joinToString(",") { "${it.magiaOrigemId}:${it.quantidadeEscolas}:${if (it.outrasEscolas) 1 else 0}" }
        val nums = branch.regrasNumericas
            .sortedWith(compareBy<RegraNumerica> { it.magiaOrigemId }.thenBy { it.minAm ?: -1 }.thenBy { it.minIq ?: -1 }.thenBy { it.minSoma ?: -1 })
            .joinToString(",") {
                val soma = it.somaAtributos.sorted().joinToString("+")
                "${it.magiaOrigemId}:${it.minAm ?: "-"}:${it.minIq ?: "-"}:${it.minSoma ?: "-"}:$soma"
            }
        return "$deps|$escolas|$nums"
    }

    internal fun dependenciasMinimasPendentes(magiaId: String, known: Set<String>): Int {
        val branches = requisitoBranchesPorMagia(magiaId)
        if (branches.isNotEmpty()) {
            if (branches.any { branch ->
                    branch.dependencias.all { it in known }
                }) return 0
            return branches.minOfOrNull { branch ->
                branch.dependencias.count { it !in known }
            } ?: 0
        }

        val grupos = dependenciasNomeadasGrupos(magiaId)
        if (grupos.isEmpty()) return 0
        return grupos.minOfOrNull { grupo -> grupo.count { it !in known } } ?: 0
    }

    internal fun dependenciasEscolhidasParaEstado(magiaId: String, known: Set<String>): List<String> {
        val branches = requisitoBranchesPorMagia(magiaId)
        if (branches.isNotEmpty()) {
            val satisfeita = branches.firstOrNull { branch ->
                branch.dependencias.all { it in known } &&
                    branch.regrasEscolas.all { atendeRegraEscolas(it, known) }
            }
            if (satisfeita != null) return emptyList()

            val escolhida = branches.minWithOrNull(
                compareBy<RequisitoBranch> { branch -> branch.dependencias.count { it !in known } }
                    .thenBy { branch -> branch.regrasEscolas.count { !atendeRegraEscolas(it, known) } }
                    .thenBy { branch -> branch.regrasNumericas.count() }
                    .thenBy { branch -> branch.dependencias.sumOf { custoAproximadoDependencia(it, known, mutableSetOf()) } }
                    .thenBy { it.dependencias.size }
                    .thenBy { it.dependencias.joinToString("|") }
            )
            return escolhida
                ?.dependencias
                ?.filter { it !in known }
                .orEmpty()
        }

        val grupos = dependenciasNomeadasGrupos(magiaId)
        if (grupos.isEmpty()) return emptyList()
        if (grupos.any { grupo -> grupo.all { it in known } }) return emptyList()
        return grupos.minWithOrNull(
            compareBy<List<String>> { grupo -> grupo.count { it !in known } }
                .thenBy { grupo -> grupo.sumOf { custoAproximadoDependencia(it, known, mutableSetOf()) } }
                .thenBy { it.size }
                .thenBy { it.joinToString("|") }
        ).orEmpty()
    }

    internal fun custoAproximadoDependencia(magiaId: String, known: Set<String>, visit: MutableSet<String>): Int {
        if (magiaId in known) return 0
        val cached = custoAproximadoCache[magiaId]
        if (cached != null) return cached
        
        if (!visit.add(magiaId)) return 999
        val grupos = dependenciasNomeadasGrupos(magiaId)
        if (grupos.isEmpty()) {
            custoAproximadoCache[magiaId] = 1
            return 1
        }
        val melhorGrupo = grupos.minOfOrNull { grupo ->
            grupo.sumOf { dep -> custoAproximadoDependencia(dep, known, visit) }
        } ?: 0
        val total = 1 + melhorGrupo
        custoAproximadoCache[magiaId] = total
        return total
    }

    internal fun extrairDependenciasNomeadas(raw: String, magiaId: String): List<String> {
        val out = linkedSetOf<String>()
        val rangesAceitos = mutableListOf<IntRange>()
        nomesNormalizadosPorTamanho.forEach { candidato ->
            val id = candidato.id
            if (id == magiaId || id in out) return@forEach
            if (!raw.contains(candidato.nome)) return@forEach
            
            val rgx = Regex("\\b${Regex.escape(candidato.nome)}\\b")
            val match = rgx.find(raw) ?: return@forEach
            val range = match.range
            val sobreposto = rangesAceitos.any { r -> range.first <= r.last && r.first <= range.last }
            if (sobreposto) return@forEach
            if (!pareceReferenciaDeEscola(raw, match.range.first, match.range.last + 1)) {
                out += id
                rangesAceitos += range
            }
        }
        return out.toList()
    }

    internal fun coletarRegrasEscolas(ids: List<String>): List<RegraEscolas> {
        return ids
            .distinct()
            .flatMap { regrasEscolasPorMagia(it) }
    }

    internal fun coletarRegrasNumericas(ids: List<String>): List<RegraNumerica> {
        return ids
            .distinct()
            .flatMap { regrasNumericasPorMagia(it) }
    }

    internal fun snapshotAlvo(alvoId: String): SnapshotAlvo {
        return snapshotCache.getOrPut(alvoId) {
            val cadeia = construirCadeiaObrigatoria(alvoId)
            val cadeiaSemAlvo = cadeia.filter { it != alvoId }
            SnapshotAlvo(
                alvoId = alvoId,
                cadeiaSemAlvo = cadeiaSemAlvo,
                regrasEscolas = coletarRegrasEscolas(cadeia),
                regrasNumericas = coletarRegrasNumericas(cadeia)
            )
        }
    }

    internal fun chavesEsperadasOrdem(snapshot: SnapshotAlvo): List<String> {
        val ordem = mutableListOf<String>()
        snapshot.cadeiaSemAlvo.forEach { ordem += "chave_$it" }
        snapshot.regrasEscolas.forEach { regra ->
            ordem += "chave_escolas_${regra.magiaOrigemId}_${regra.quantidadeEscolas}"
        }
        snapshot.regrasNumericas.forEach { regra ->
            regra.minAm?.let { ordem += "chave_am_${regra.magiaOrigemId}_$it" }
            regra.minIq?.let { ordem += "chave_iq_${regra.magiaOrigemId}_$it" }
            if (regra.somaAtributos.isNotEmpty() && regra.minSoma != null) {
                ordem += "chave_soma_${regra.magiaOrigemId}_${regra.somaAtributos.joinToString("_")}_${regra.minSoma}"
            }
        }
        ordem += "chave_alvo_${snapshot.alvoId}"
        return ordem
    }

    internal fun chavesEsperadasIds(snapshot: SnapshotAlvo): Set<String> = chavesEsperadasOrdem(snapshot).toSet()

    internal fun regrasEscolasPorMagia(magiaId: String): List<RegraEscolas> {
        return regrasEscolasCache.getOrPut(magiaId) {
            val raw = preNorm(magiaId)
            regraEscolasRegex
                .findAll(raw)
                .mapNotNull { m ->
                    val qtd = parseNumeroToken(m.groupValues[2]) ?: return@mapNotNull null
                    RegraEscolas(
                        magiaOrigemId = magiaId,
                        quantidadeEscolas = qtd,
                        outrasEscolas = m.groupValues[3].isNotBlank()
                    )
                }
                .toList()
        }
    }

    internal fun parseNumeroToken(raw: String): Int? {
        val token = normalize(raw)
        token.toIntOrNull()?.let { return it }
        return when (token) {
            "um", "uma" -> 1
            "dois", "duas" -> 2
            "tres" -> 3
            "quatro" -> 4
            "cinco" -> 5
            "seis" -> 6
            "sete" -> 7
            "oito" -> 8
            "nove" -> 9
            "dez" -> 10
            "onze" -> 11
            "doze" -> 12
            "treze" -> 13
            "catorze", "quatorze" -> 14
            "quinze" -> 15
            "dezesseis" -> 16
            "dezessete" -> 17
            "dezoito" -> 18
            "dezenove" -> 19
            "vinte" -> 20
            else -> null
        }
    }

    internal fun regrasNumericasPorMagia(magiaId: String): List<RegraNumerica> {
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

    internal fun bloqueioNumericoParaMagia(
        magiaId: String,
        estado: ArcanoEstadoPersonagem,
        known: Set<String>
    ): String? {
        val regra = regrasNumericasRelevantes(magiaId, known, estado).firstOrNull() ?: return null
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

    internal fun primeiroBloqueioNumerico(
        alvoId: String,
        cadeiaSemAlvo: List<String>,
        estado: ArcanoEstadoPersonagem,
        known: Set<String>
    ): String? {
        val alvoDeBloqueio = cadeiaSemAlvo.firstOrNull { it !in known } ?: alvoId
        return bloqueioNumericoParaMagia(alvoDeBloqueio, estado, known)
    }

    internal fun codigoBloqueio(
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
        cachePlanos.clear()
        cacheHits = 0
        cacheMisses = 0
        temposRodadaNs.clear()
    }

    fun invalidarCachePorMagia(magiaId: String) {
        val token = "|$magiaId|"
        cacheResultados.keys.removeIf { token in it.assinaturaKnown }
        cacheDiagnosticos.keys.removeIf { token in it.assinaturaKnown }
        cachePlanos.keys.removeIf { token in it.assinaturaKnown }
    }

    fun invalidarCacheIncrementalPorMagia(magiaId: String) {
        val impactados = alvosImpactadosPorMagia(magiaId)
        if (impactados.isEmpty()) return
        cacheResultados.keys.removeIf { it.alvoId in impactados }
        cacheDiagnosticos.keys.removeIf { it.alvoId in impactados }
        cachePlanos.keys.removeIf { it.alvoId in impactados }
    }

    fun alvosImpactadosPorMagia(magiaId: String): Set<String> {
        val visit = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.addLast(magiaId)
        while (queue.isNotEmpty()) {
            val atual = queue.removeFirst()
            dependentesDiretosByMagia[atual].orEmpty().forEach { dep ->
                if (visit.add(dep)) queue.addLast(dep)
            }
        }
        if (catalogo.existe(magiaId)) {
            visit += magiaId
        }
        visit += alvosComRegraEscolas
        return visit
    }

    internal fun alvosDependentesTransitivos(magiaId: String): Set<String> {
        return dependentesTransitivosByMagia[magiaId].orEmpty()
    }

    fun cacheStats(): ArcanoCacheStats {
        val entradas = cacheResultados.size + cacheDiagnosticos.size + cachePlanos.size
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

    internal fun registrarTempoRodada(deltaNs: Long) {
        if (deltaNs < 0) return
        temposRodadaNs.addLast(deltaNs)
        while (temposRodadaNs.size > maxTempos) {
            temposRodadaNs.removeFirst()
        }
    }

    internal fun cacheKey(
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

    internal fun tieBreakPorAlvo(alvoId: String, candId: String): Int {
        return (alvoId + "|" + candId).hashCode()
    }

    internal fun metasImpactadasPorAcao(
        alvoId: String,
        estado: ArcanoEstadoPersonagem,
        known: Set<String>,
        acaoMagiaId: String
    ): List<String> {
        if (acaoMagiaId in known) return emptyList()
        val antes = diagnosticarMetasAlvo(alvoId, estado.copy(magiasConhecidasIds = known))
        val antesById = antes.associateBy { it.id }
        val depois = diagnosticarMetasAlvo(alvoId, estado.copy(magiasConhecidasIds = known + acaoMagiaId))
        val depoisIds = depois.map { it.id }.toSet()
        val impactadas = linkedSetOf<String>()

        depois.forEach { metaDepois ->
            val metaAntes = antesById[metaDepois.id]
            if (metaAntes == null) {
                impactadas += metaDepois.id
                return@forEach
            }
            val progressoNumerico = metaDepois.atual > metaAntes.atual
            val passouParaAtendida = !metaAntes.atendida && metaDepois.atendida
            val destravouUpstream = metaAntes.bloqueadaPorUpstream && !metaDepois.bloqueadaPorUpstream
            if (progressoNumerico || passouParaAtendida || destravouUpstream) {
                impactadas += metaDepois.id
            }
        }
        antes.forEach { metaAntes ->
            if (metaAntes.id !in depoisIds && !metaAntes.atendida) {
                impactadas += metaAntes.id
            }
        }

        return impactadas
            .sorted()
            .toList()
    }

    internal fun sha256Hex(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte ->
            val value = byte.toInt() and 0xff
            val token = value.toString(16)
            if (token.length == 1) "0$token" else token
        }
    }

    internal fun preRequisitoSemConteudo(magiaId: String): Boolean {
        val raw = preRaw(magiaId).trim().lowercase()
        if (raw.isBlank()) return true
        if (raw in setOf("-", "—", "–", "−", "?", "??", "???", "â€”", "â€“", "âˆ’")) return true
        val norm = preNorm(magiaId)
        return norm.isBlank()
    }

    internal fun nomeMagia(magiaId: String): String = nomeById[magiaId] ?: catalogo.nome(magiaId)

    internal fun nomeMagiaNorm(magiaId: String): String = nomeNormById[magiaId] ?: normalize(nomeMagia(magiaId))

    internal fun preRaw(magiaId: String): String = preRawById[magiaId] ?: catalogo.preRequisitoRaw(magiaId)

    internal fun preNorm(magiaId: String): String = preNormById[magiaId] ?: normalize(preRaw(magiaId))

    internal fun escolasNorm(magiaId: String): List<String> = escolasNormById[magiaId]
        ?: catalogo.escolas(magiaId).map(::normalize).filter { it.isNotBlank() }

    internal fun escolaPrincipalNorm(magiaId: String): String = escolaPrincipalNormById[magiaId]
        ?: escolasNorm(magiaId).firstOrNull().orEmpty()

    internal fun escolaBloqueadaPorPolitica(magiaId: String): Boolean {
        val escola = escolaPrincipalNorm(magiaId)
        return escola in escolasNuncaRecomendar
    }

    internal fun variantesSingularPlural(nomeNorm: String): Set<String> {
        val tokens = nomeNorm
            .split(" ")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toMutableList()
        if (tokens.isEmpty()) return emptySet()
        val idx = tokens.indexOfLast { it.length > 2 }.coerceAtLeast(tokens.lastIndex)
        val tokenBase = tokens[idx]
        val singular = singularizarTokenPt(tokenBase)
        val plural = pluralizarTokenPt(singular)
        val out = linkedSetOf(nomeNorm)

        fun addComToken(novoToken: String) {
            if (novoToken.isBlank()) return
            val copia = tokens.toMutableList()
            copia[idx] = novoToken
            out += copia.joinToString(" ")
        }

        addComToken(singular)
        addComToken(plural)
        return out
    }

    internal fun singularizarTokenPt(token: String): String {
        if (token.length <= 3) return token
        return when {
            token.endsWith("oes") && token.length > 4 -> token.dropLast(3) + "ao"
            token.endsWith("aes") && token.length > 4 -> token.dropLast(3) + "ao"
            token.endsWith("ais") && token.length > 4 -> token.dropLast(3) + "al"
            token.endsWith("eis") && token.length > 4 -> token.dropLast(3) + "el"
            token.endsWith("ois") && token.length > 4 -> token.dropLast(3) + "ol"
            token.endsWith("uis") && token.length > 4 -> token.dropLast(3) + "ul"
            token.endsWith("ns") && token.length > 3 -> token.dropLast(2) + "m"
            token.endsWith("s") && !token.endsWith("ss") -> token.dropLast(1)
            else -> token
        }
    }

    internal fun pluralizarTokenPt(token: String): String {
        if (token.isBlank()) return token
        return when {
            token.endsWith("s") -> token
            token.endsWith("m") && token.length > 2 -> token.dropLast(1) + "ns"
            token.endsWith("al") && token.length > 3 -> token.dropLast(2) + "ais"
            token.endsWith("el") && token.length > 3 -> token.dropLast(2) + "eis"
            token.endsWith("ol") && token.length > 3 -> token.dropLast(2) + "ois"
            token.endsWith("ul") && token.length > 3 -> token.dropLast(2) + "uis"
            token.endsWith("ao") && token.length > 3 -> token.dropLast(2) + "oes"
            token.endsWith("r") || token.endsWith("z") -> token + "es"
            else -> token + "s"
        }
    }

    internal fun normalize(raw: String): String {
        val semAcento = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    internal fun pareceReferenciaDeEscola(raw: String, start: Int, endExclusive: Int): Boolean {
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

    internal fun valorAtributo(nome: String, estado: ArcanoEstadoPersonagem): Int {
        return when (normalize(nome)) {
            "am" -> estado.am
            "iq" -> estado.iq
            "dx" -> estado.dx
            else -> 0
        }
    }
}
