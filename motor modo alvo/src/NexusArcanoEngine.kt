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
        val minSoma: Int? = null,
        // Lote 330: requisito "N magias de UMA escola específica" (ex: Convocar
        // Elemental = 8 magias de Fogo). Antes o tipo MagiasEscola caía no else
        // do branchFromAlternative e era IGNORADO → magia liberava sem checar.
        val escolaRequerida: String? = null,
        val minMagiasEscola: Int? = null,
        // Lote 335: requisito "N magias QUAISQUER" (ex: Retardo = "AM3, 15 magias";
        // Metamorfose Superior = "10 outras magicas"). Conta o TOTAL de magias da ficha.
        // Antes o tipo QuantidadeOutrasMagias caía no else e era ignorado → liberava sem checar.
        val minMagiasQuaisquer: Int? = null
    )

    internal data class RequisitoBranch(
        val dependencias: List<String>,
        val regrasEscolas: List<RegraEscolas>,
        val regrasNumericas: List<RegraNumerica>,
        // Lote 334: nomes (normalizados) de VANTAGENS/perícias exigidas que NÃO são
        // magia. Antes, um token não-resolvido (ex: "Empatia") era descartado e a
        // branch ficava vazia = passe livre. Agora vira requisito real: só satisfeito
        // se a vantagem/perícia estiver na ficha (estado.vantagensConhecidasNorm).
        val vantagensRequeridas: List<String> = emptyList(),
        // Lote 334 (Frente 2): grupos OU de magias. Cada grupo é satisfeito se a ficha
        // tiver QUALQUER uma das magias dele. Usado quando o pré-requisito cita um
        // NOME-BASE (ex: "Convocar Animal") que no catálogo só existe em sub-escolas
        // ("Convocar Animal (Criaturas da Terra/Ar/Mar)"): qualquer variante serve.
        val gruposDependenciaOu: List<List<String>> = emptyList()
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
        val dx: Int,
        // Lote 334: vantagens/perícias entram na chave de cache. Sem isto, a 1ª consulta
        // (ficha sem a vantagem → bloqueado) ficava cacheada e a 2ª (com a vantagem)
        // retornava o resultado velho = magia continuava bloqueada falsamente.
        val assinaturaVantagens: String = ""
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
    // Lote 330: conjunto de escolas reais (normalizadas), derivado do catálogo.
    // Usado para decidir se "N magias de X" conta por ESCOLA (X é escola real) ou
    // por NOME (X é tema, ex: Ácido). O parsing de MagiasEscola é feito pelo
    // PreRequisitoParser (em branchFromAlternative), não por regex aqui.
    internal val escolasConhecidasNorm: Set<String> by lazy {
        escolasNormById.values.flatten().filter { it.isNotBlank() }.toSet()
    }
    internal val pesoDepsMissing = 6
    internal val pesoFaltaNumerica = 8
    internal val pesoFaltaEscolas = 3
    internal val pesoComplexidadeBase = 4
    internal val custoBasePlano = 1
    internal val penalidadePlanoEscolaRepetida = 1
    internal val penalidadePlanoSemReducaoMeta = 4
    internal val escolasNuncaRecomendar = setOf("tecnologica")
    internal val maxGruposDependencias = 96

    internal fun valorAtributo(atrr: String, estado: ArcanoEstadoPersonagem): Int {
        return when (atrr.lowercase().trim()) {
            "iq" -> estado.iq
            "dx" -> estado.dx
            "am" -> estado.am
            "st" -> 10 // Default se não fornecido
            "ht" -> 10 
            else -> 10
        }
    }

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
                // Lote 330: chave para "N magias de uma escola" com descrição clara
                // (ex: "Ter 8 magias de Ar (atual 3)") — antes a falta caía no
                // motivoBloqueio genérico "Sem ação imediata".
                if (regra.escolaRequerida != null && regra.minMagiasEscola != null) {
                    val qtdAtual = contarMagiasPorEscolaOuNome(regra.escolaRequerida, known)
                    val termoLabel = regra.escolaRequerida.replaceFirstChar { it.uppercase() }
                    chaves += ArcanoChave(
                        id = "chave_escola_qtd_${regra.magiaOrigemId}_${regra.escolaRequerida}_${regra.minMagiasEscola}",
                        descricao = "Ter ${regra.minMagiasEscola} magias de $termoLabel (atual $qtdAtual)",
                        ativa = qtdAtual >= regra.minMagiasEscola
                    )
                }
                // Lote 335: chave para "N magias quaisquer" (ex: Retardo = 15 magias).
                if (regra.minMagiasQuaisquer != null) {
                    val total = known.size
                    chaves += ArcanoChave(
                        id = "chave_magias_quaisquer_${regra.magiaOrigemId}_${regra.minMagiasQuaisquer}",
                        descricao = "Ter ${regra.minMagiasQuaisquer} magias quaisquer (atual $total)",
                        ativa = total >= regra.minMagiasQuaisquer
                    )
                }
            }

            // Lote 334: chave para pré-requisito de VANTAGEM (ex: "ou Empatia com
            // Animais"). Mostra qual vantagem falta na ficha — antes o bloqueio caía
            // no genérico "Sem ação imediata". Usa o ramo relevante ao estado atual.
            val branchVant = escolherBranchRelevante(alvoId, known, estado)
            branchVant?.vantagensRequeridas?.forEach { vantNorm ->
                val ativa = atendeVantagemRequerida(vantNorm, estado)
                val label = vantNorm.replaceFirstChar { it.uppercase() }
                chaves += ArcanoChave(
                    id = "chave_vantagem_${alvoId}_$vantNorm",
                    descricao = "Ter a vantagem/perícia \"$label\" para ${nomeMagia(alvoId)}",
                    ativa = ativa
                )
            }
            // Lote 334 (Frente 2): chave para grupo OU de variantes (ex: qualquer
            // "Convocar Animal (...)"). Mostra as opções aceitas em vez do genérico.
            branchVant?.gruposDependenciaOu?.forEachIndexed { idx, grupo ->
                val ativa = grupo.any { it in known }
                val opcoes = grupo.map { nomeMagia(it) }.distinct().joinToString(" ou ")
                chaves += ArcanoChave(
                    id = "chave_ou_${alvoId}_$idx",
                    descricao = "Aprender qualquer: $opcoes",
                    ativa = ativa
                )
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







    internal fun magiaAprendivelAgora(magiaId: String, known: Set<String>, estado: ArcanoEstadoPersonagem): Boolean {
        if (magiaId in known) return true
        val branches = requisitoBranchesPorMagia(magiaId)
        if (branches.isNotEmpty()) {
            return branches.any { branch ->
                branch.dependencias.all { it in known } &&
                    branch.regrasEscolas.all { atendeRegraEscolas(it, known) } &&
                    branch.regrasNumericas.all { atendeRegraNumerica(it, estado) } &&
                    branch.vantagensRequeridas.all { atendeVantagemRequerida(it, estado) } &&
                    branch.gruposDependenciaOu.all { grupo -> grupo.any { it in known } }
            }
        }

        val gruposDeps = dependenciasNomeadasGrupos(magiaId)
        val depsOk = if (gruposDeps.isEmpty()) true else gruposDeps.any { grupo -> grupo.all { it in known } }
        if (!depsOk) return false
        if (!coletarRegrasEscolasGlobais(listOf(magiaId)).all { atendeRegraEscolas(it, known) }) return false
        return coletarRegrasNumericasGlobais(listOf(magiaId)).all { atendeRegraNumerica(it, estado) }
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
        // REGRA GURPS: "1 mágica EM N escolas diferentes" → cada magia
        // conta como UMA escola (a principal dela). Antes usava flatMap
        // de TODAS as escolas da magia: magias multi-escola (ex:
        // convocar_elemental = Ar/Fogo/Terra/Água) inflavam a contagem
        // (4 magias viravam "10 escolas" falsamente), liberando
        // Encantar/Desejo cedo demais e com trilha errada.
        return known
            .asSequence()
            .map { id -> escolaPrincipalNorm(id) }
            .filter { it.isNotBlank() }
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
        val okEscola = if (regra.escolaRequerida != null && regra.minMagiasEscola != null) {
            contarMagiasPorEscolaOuNome(regra.escolaRequerida, estado.magiasConhecidasIds) >= regra.minMagiasEscola
        } else {
            true
        }
        // Lote 335: "N magias quaisquer" = total de magias da ficha. Não conta a própria
        // magia-alvo (que ainda não foi aprendida) nem precisa: known já é o repertório atual.
        val okQuaisquer = if (regra.minMagiasQuaisquer != null) {
            estado.magiasConhecidasIds.size >= regra.minMagiasQuaisquer
        } else {
            true
        }
        return okAm && okIq && okSoma && okEscola && okQuaisquer
    }

    /**
     * Lote 334: pré-requisito do tipo "ou Vantagem X" (ex: Empatia com Animais,
     * Noção do Perigo). 'vantNorm' é o nome normalizado exigido; só é atendido se a
     * ficha tiver essa vantagem OU perícia (match por prefixo p/ cobrir variações como
     * "Empatia com Animais (Cães)"). Match em ambos os sentidos.
     */
    internal fun atendeVantagemRequerida(vantNorm: String, estado: ArcanoEstadoPersonagem): Boolean {
        val conhecidas = estado.vantagensConhecidasNorm + estado.periciasConhecidasNorm
        return conhecidas.any { it == vantNorm || it.startsWith("$vantNorm ") || vantNorm.startsWith("$it ") }
    }

    /**
     * Lote 330: "N magias de X" — X pode ser ESCOLA real (Fogo, Ar, Mente...) ou
     * TEMA/nome (Ácido = magias com "acido" no nome: Criar Ácido, Jato de Ácido...).
     * Se 'termoNorm' bate com uma escola real do catálogo → conta por escola.
     * Senão → conta magias cujo NOME normalizado contém o termo. Cobre os dois casos.
     */
    internal fun contarMagiasPorEscolaOuNome(termoNorm: String, known: Set<String>): Int {
        val ehEscolaReal = termoNorm in escolasConhecidasNorm
        return known.count { id ->
            if (ehEscolaReal) {
                escolasNormById[id].orEmpty().contains(termoNorm)
            } else {
                nomeNormById[id].orEmpty().contains(termoNorm)
            }
        }
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
            val deps = dependenciasEscolhidasParaEstado(id, known)
            deps.forEach(::dfs)
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
            if (magiaId == "desejo" || magiaId == "pequeno_desejo") {
                println("DEBUG: Resolvendo $magiaId raw='$raw' -> deps=$depsRawCompleto")
            }
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

        // 1. Verificação direta por ID (se o token já for um ID conhecido)
        if (catalogo.existe(tokenRaw)) return listOf(tokenRaw)
        if (catalogo.existe(tokenNorm)) return listOf(tokenNorm)

        // 2. Procura direta nos nomes normalizados (mais rápido)
        val matchDireto = nomesNormalizadosPorTamanho
            .asSequence()
            .filter { it.id != magiaId && it.nome == tokenNorm }
            .map { it.id }
            .distinct()
            .toList()
        if (matchDireto.isNotEmpty()) return matchDireto

        // 3. Fallback para extração por Regex (lida com substrings/variações)
        return extrairDependenciasNomeadas(tokenNorm, magiaId)
    }

    /**
     * Lote 334 (Frente 2): se 'tokenRaw' for um NOME-BASE que no catálogo só existe em
     * SUB-ESCOLAS (ex: "Convocar Animal" → "Convocar Animal (Criaturas da Terra)",
     * "... (do Ar)", "... (do Mar)"), retorna os ids das variantes. Qualquer variante
     * satisfaz (tratado como OU em magiaAprendivelAgora). Só dispara quando NÃO há
     * magia com nome exatamente igual ao token (senão usa a resolução normal).
     */
    internal fun resolverVariantesSubEscola(tokenRaw: String, magiaId: String): List<String> {
        val tokenNorm = normalize(tokenRaw)
        if (tokenNorm.isBlank()) return emptyList()
        // Se existe magia com nome exato, NÃO é caso de sub-escola.
        if (nomesNormalizadosPorTamanho.any { it.nome == tokenNorm }) return emptyList()
        val prefixo = "$tokenNorm "
        return nomesNormalizadosPorTamanho
            .asSequence()
            .filter { it.id != magiaId && it.nome.startsWith(prefixo) }
            .map { it.id }
            .distinct()
            .toList()
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

            val result = combinarBranches(alternativasPorTermo)
            if (result.isEmpty()) {
                val depsFallback = extrairDependenciasNomeadas(preNorm(magiaId), magiaId)
                val escolasFallback = regrasEscolasPorMagia(magiaId)
                val numericasFallback = regrasNumericasPorMagia(magiaId)
                if (depsFallback.isNotEmpty() || escolasFallback.isNotEmpty() || numericasFallback.isNotEmpty()) {
                    val fallback = listOf(
                        RequisitoBranch(
                            dependencias = depsFallback,
                            regrasEscolas = escolasFallback,
                            regrasNumericas = numericasFallback
                        )
                    )
                    return@getOrPut fallback
                }
            }
            result
        }
    }

    // Lote 334: palavras-curinga que aparecem como pré-requisito mas não são uma
    // vantagem real (não podem virar exigência, senão bloqueiam a magia sempre).
    internal val tokensGenericosIgnorados: Set<String> = setOf(
        "qualquer", "qualquer uma", "qualquer magica", "qualquer outra", "nenhum", "nenhuma"
    )

    internal fun branchFromAlternative(
        magiaId: String,
        alternativa: List<PreRequisitoType>
    ): RequisitoBranch {
        val deps = linkedSetOf<String>()
        val regrasEscolas = mutableListOf<RegraEscolas>()
        val regrasNumericas = mutableListOf<RegraNumerica>()
        val vantagensReq = linkedSetOf<String>()
        val gruposOu = mutableListOf<List<String>>()

        // Lote 334: um token nomeado (magia/vantagem) ou resolve para uma MAGIA do
        // catálogo (vira dependência), ou — se não existir como magia — vira VANTAGEM
        // REQUERIDA (checada contra a ficha). Antes, não-resolvido era descartado e a
        // branch ficava vazia = passe livre.
        fun tratarTokenNomeado(nomeRaw: String) {
            // Frente 2: nome-base que só existe em sub-escolas → grupo OU (qualquer serve).
            val variantes = resolverVariantesSubEscola(nomeRaw, magiaId)
            if (variantes.isNotEmpty()) {
                gruposOu += variantes
                return
            }
            val resolved = resolverDependenciasNomeadasToken(nomeRaw, magiaId)
            if (resolved.isNotEmpty()) {
                resolved.forEach { deps += it }
            } else {
                val nomeNorm = normalize(nomeRaw)
                    .replace(Regex("^(a |o |as |os )"), "")
                    .replace(Regex("\\bvantagem\\b|\\bvantagens\\b|\\bpericia\\b|\\bpericias\\b|\\bdesvantagem\\b"), "")
                    .replace(Regex("\\s+"), " ").trim()
                // Tokens genéricos/placeholder NÃO viram exigência de vantagem (senão
                // bloqueariam tudo). Ex: "qualquer" usado como curinga de pré-requisito.
                if (nomeNorm.isNotBlank() && nomeNorm !in tokensGenericosIgnorados) {
                    vantagensReq += nomeNorm
                }
            }
        }

        alternativa.forEach { tipo ->
            when (tipo) {
                is PreRequisitoType.MagiaConhecida -> tratarTokenNomeado(tipo.nomeMagia)
                is PreRequisitoType.VantagemConhecida -> tratarTokenNomeado(tipo.nomeVantagem)
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
                is PreRequisitoType.MagiasEscola -> {
                    // Lote 330: "N magias de X". X pode ser ESCOLA real (Fogo, Ar...)
                    // ou TEMA/nome (Ácido = magias com "Acido" no nome). Antes caía no
                    // else e era ignorado → magia liberava sem checar (84 magias).
                    // A contagem (escola vs nome) é decidida em atendeRegraNumerica.
                    regrasNumericas += RegraNumerica(
                        magiaOrigemId = magiaId,
                        minAm = null,
                        minIq = null,
                        escolaRequerida = normalize(tipo.escola),
                        minMagiasEscola = tipo.quantidade
                    )
                }
                is PreRequisitoType.QuantidadeOutrasMagias -> {
                    // Lote 335: "N magias quaisquer" (contexto vazio) = total de magias da
                    // ficha. Se houver contexto (ex: "de mente"), trata como escola/tema.
                    val ctx = tipo.contexto?.let { normalize(it) }?.takeIf { it.isNotBlank() }
                    if (ctx == null) {
                        regrasNumericas += RegraNumerica(
                            magiaOrigemId = magiaId, minAm = null, minIq = null,
                            minMagiasQuaisquer = tipo.quantidade
                        )
                    } else {
                        regrasNumericas += RegraNumerica(
                            magiaOrigemId = magiaId, minAm = null, minIq = null,
                            escolaRequerida = ctx, minMagiasEscola = tipo.quantidade
                        )
                    }
                }
                else -> Unit
            }
        }

        return RequisitoBranch(
            dependencias = deps.toList(),
            regrasEscolas = regrasEscolas.distinct(),
            regrasNumericas = regrasNumericas.distinct(),
            vantagensRequeridas = vantagensReq.toList(),
            gruposDependenciaOu = gruposOu.toList()
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
                        regrasNumericas = (base.regrasNumericas + alt.regrasNumericas).distinct(),
                        vantagensRequeridas = (base.vantagensRequeridas + alt.vantagensRequeridas).distinct(),
                        gruposDependenciaOu = (base.gruposDependenciaOu + alt.gruposDependenciaOu).distinct()
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
                "${it.magiaOrigemId}:${it.minAm ?: "-"}:${it.minIq ?: "-"}:${it.minSoma ?: "-"}:$soma" +
                    ":${it.escolaRequerida ?: "-"}:${it.minMagiasEscola ?: "-"}:${it.minMagiasQuaisquer ?: "-"}"
            }
        val vant = branch.vantagensRequeridas.sorted().joinToString(",")
        val ous = branch.gruposDependenciaOu
            .map { it.sorted().joinToString("/") }
            .sorted()
            .joinToString(",")
        return "$deps|$escolas|$nums|$vant|$ous"
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
            if (satisfeita != null) {
                return emptyList()
            }

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

    internal fun chavesEsperadasIds(snapshot: SnapshotAlvo): Set<String> = chavesEsperadasOrdem(snapshot).toSet()

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
        val assinaturaVantagens = (estado.vantagensConhecidasNorm + estado.periciasConhecidasNorm)
            .sorted().joinToString(separator = "|", prefix = "|", postfix = "|")
        return CacheKey(
            alvoId = alvoId,
            assinaturaKnown = assinaturaKnown,
            am = estado.am,
            iq = estado.iq,
            dx = estado.dx,
            assinaturaVantagens = assinaturaVantagens
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
        if (!catalogo.existe(alvoId)) return emptyList()

        val known = estado.magiasConhecidasIds
        // Usamos a cadeia fixa para o diagnóstico da trilha completa
        val cadeiaFixa = construirCadeiaObrigatoria(alvoId)
        val cadeiaSemAlvo = cadeiaFixa.filter { it != alvoId }
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

        // --- UNIFICAÇÃO DE METAS (LIGAÇÃO INCREMENTAL) ---
        // Para regras dinâmicas (escolas/atributos), usamos a cadeia dependente do estado
        val cadeiaDinamica = construirCadeiaObrigatoriaParaEstado(alvoId, known)
        val regrasEscolas = coletarRegrasEscolasParaEstado(cadeiaDinamica, known, estado)
            .sortedBy { it.quantidadeEscolas }

        val proximaEscolaIncompleta = regrasEscolas.firstOrNull { regra ->
            val atuais = escolasConhecidas(known).toMutableSet().also { set ->
                if (regra.outrasEscolas) set.removeAll(escolasNorm(regra.magiaOrigemId).toSet())
            }.size
            atuais < regra.quantidadeEscolas
        } ?: regrasEscolas.lastOrNull()

        proximaEscolaIncompleta?.let { regra ->
            val escolasAtuais = escolasConhecidas(known).toMutableSet().also { set ->
                if (regra.outrasEscolas) set.removeAll(escolasNorm(regra.magiaOrigemId).toSet())
            }.size
            val atendida = escolasAtuais >= regra.quantidadeEscolas
            
            metas += ArcanoMetaProgress(
                id = "meta_escolas_${regra.magiaOrigemId}_${regra.quantidadeEscolas}_${if (regra.outrasEscolas) 1 else 0}",
                tipo = ArcanoMetaTipo.ESCOLAS_DISTINTAS,
                origemMagiaId = regra.magiaOrigemId,
                descricao = "Escolas para ${nomeMagia(regra.magiaOrigemId)}",
                requerido = regra.quantidadeEscolas,
                atual = escolasAtuais.coerceAtMost(regra.quantidadeEscolas),
                atendida = atendida,
                bloqueadaPorUpstream = dependenciasMinimasPendentes(regra.magiaOrigemId, known) > 0
            )
        }

        val regrasNumericas = coletarRegrasNumericasParaEstado(cadeiaDinamica, known, estado)
        val tiposNumericos = listOf(ArcanoMetaTipo.NUMERICO_AM, ArcanoMetaTipo.NUMERICO_IQ, ArcanoMetaTipo.NUMERICO_SOMA)
        
        tiposNumericos.forEach { tipo ->
            val pertinentes = regrasNumericas.filter { r ->
                when(tipo) {
                    ArcanoMetaTipo.NUMERICO_AM -> r.minAm != null
                    ArcanoMetaTipo.NUMERICO_IQ -> r.minIq != null
                    ArcanoMetaTipo.NUMERICO_SOMA -> r.somaAtributos.isNotEmpty() && r.minSoma != null
                    else -> false
                }
            }.sortedBy { r ->
                when(tipo) {
                    ArcanoMetaTipo.NUMERICO_AM -> r.minAm ?: 0
                    ArcanoMetaTipo.NUMERICO_IQ -> r.minIq ?: 0
                    ArcanoMetaTipo.NUMERICO_SOMA -> r.minSoma ?: 0
                    else -> 0
                }
            }

            val proximaIncompleta = pertinentes.firstOrNull { r ->
                when(tipo) {
                    ArcanoMetaTipo.NUMERICO_AM -> estado.am < (r.minAm ?: 0)
                    ArcanoMetaTipo.NUMERICO_IQ -> estado.iq < (r.minIq ?: 0)
                    ArcanoMetaTipo.NUMERICO_SOMA -> r.somaAtributos.sumOf { valorAtributo(it, estado) } < (r.minSoma ?: 0)
                    else -> false
                }
            } ?: pertinentes.lastOrNull()

            proximaIncompleta?.let { regra ->
                when(tipo) {
                    ArcanoMetaTipo.NUMERICO_AM -> {
                        val valReq = regra.minAm ?: 0
                        metas += ArcanoMetaProgress(
                            id = "meta_am_${regra.magiaOrigemId}_$valReq",
                            tipo = tipo,
                            origemMagiaId = regra.magiaOrigemId,
                            descricao = "AM para ${nomeMagia(regra.magiaOrigemId)}",
                            requerido = valReq,
                            atual = estado.am.coerceAtMost(valReq),
                            atendida = estado.am >= valReq,
                            bloqueadaPorUpstream = dependenciasMinimasPendentes(regra.magiaOrigemId, known) > 0
                        )
                    }
                    ArcanoMetaTipo.NUMERICO_IQ -> {
                        val valReq = regra.minIq ?: 0
                        metas += ArcanoMetaProgress(
                            id = "meta_iq_${regra.magiaOrigemId}_$valReq",
                            tipo = tipo,
                            origemMagiaId = regra.magiaOrigemId,
                            descricao = "IQ para ${nomeMagia(regra.magiaOrigemId)}",
                            requerido = valReq,
                            atual = estado.iq.coerceAtMost(valReq),
                            atendida = estado.iq >= valReq,
                            bloqueadaPorUpstream = dependenciasMinimasPendentes(regra.magiaOrigemId, known) > 0
                        )
                    }
                    ArcanoMetaTipo.NUMERICO_SOMA -> {
                        val valReq = regra.minSoma ?: 0
                        val somaAtual = regra.somaAtributos.sumOf { valorAtributo(it, estado) }
                        metas += ArcanoMetaProgress(
                            id = "meta_soma_${regra.magiaOrigemId}_${regra.somaAtributos.joinToString("_")}_$valReq",
                            tipo = tipo,
                            origemMagiaId = regra.magiaOrigemId,
                            descricao = "${regra.somaAtributos.joinToString("+").uppercase()} para ${nomeMagia(regra.magiaOrigemId)}",
                            requerido = valReq,
                            atual = somaAtual.coerceAtMost(valReq),
                            atendida = somaAtual >= valReq,
                            bloqueadaPorUpstream = dependenciasMinimasPendentes(regra.magiaOrigemId, known) > 0
                        )
                    }
                    else -> {}
                }
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

    internal fun coletarRegrasEscolasParaEstado(cadeia: List<String>, known: Set<String>, estado: ArcanoEstadoPersonagem): List<RegraEscolas> {
        val out = mutableListOf<RegraEscolas>()
        cadeia.forEach { id ->
            out.addAll(regrasEscolasPorMagia(id))
        }
        return out.distinctBy { "${it.magiaOrigemId}:${it.quantidadeEscolas}:${it.outrasEscolas}" }
    }

    internal fun coletarRegrasNumericasParaEstado(cadeia: List<String>, known: Set<String>, estado: ArcanoEstadoPersonagem): List<RegraNumerica> {
        val out = mutableListOf<RegraNumerica>()
        cadeia.forEach { id ->
            out.addAll(regrasNumericasPorMagia(id))
        }
        return out.distinctBy { r ->
            val soma = r.somaAtributos.sorted().joinToString("+")
            "${r.magiaOrigemId}:${r.minAm}:${r.minIq}:${r.minSoma}:$soma"
        }
    }

    /**
     * Gera um resumo técnico (Gabarito) de pré-requisitos para consumo pela IA.
     * Roda o motor em modo "Consultor" (Estado Zero) para entregar o caminho universal.
     */
    fun formatarGabaritoParaIA(magiaAlvoId: String): String {
        val idLimpo = normalize(magiaAlvoId).replace(" ", "_")
        val idReal = allMagiaIds.find { it == idLimpo || it == magiaAlvoId }
            ?: allMagiaIds.find { nomeNormById[it] == idLimpo }
            ?: return "ERRO: Magia '$magiaAlvoId' não encontrada no catálogo oficial."

        val estadoZero = ArcanoEstadoPersonagem(magiasConhecidasIds = emptySet(), am = 0, iq = 10)
        val snapshot = snapshotAlvo(idReal)
        
        return buildString {
            appendLine("=== GABARITO TÉCNICO NEXUS: ${nomeById[idReal]?.uppercase() ?: idReal.uppercase()} ===")
            appendLine("ID: $idReal")
            appendLine("PRÉ-REQUISITOS (LITERAL NO LIVRO): ${preRawById[idReal]}")
            appendLine("\n--- ÁRVORE DE DEPENDÊNCIAS (CAMINHO COMPLETO) ---")
            
            val cadeia = snapshot.cadeiaSemAlvo
            if (cadeia.isEmpty()) {
                appendLine("- Nenhuma magia pré-requisito necessária.")
            } else {
                cadeia.forEachIndexed { index, id ->
                    appendLine("${index + 1}. ${nomeById[id] ?: id}")
                }
            }

            if (snapshot.regrasEscolas.isNotEmpty()) {
                appendLine("\n--- REQUISITOS DE ESCOLAS ---")
                snapshot.regrasEscolas.forEach { regra ->
                    appendLine("- Requisito: ${regra.quantidadeEscolas} mágicas em escolas diferentes.")
                    appendLine("  Sugestão de Ativação: ${sugerirMagiasParaEscolas(regra.quantidadeEscolas).joinToString(", ")}")
                }
            }

            if (snapshot.regrasNumericas.isNotEmpty()) {
                appendLine("\n--- REQUISITOS DE ATRIBUTOS ---")
                snapshot.regrasNumericas.forEach { regra ->
                    regra.minAm?.let { appendLine("- Aptidão Mágica mínima: $it") }
                    regra.minIq?.let { appendLine("- IQ mínimo: $it") }
                    if (regra.somaAtributos.isNotEmpty()) {
                        appendLine("- Soma de ${regra.somaAtributos.joinToString("+").uppercase()} mínima: ${regra.minSoma}")
                    }
                }
            }
            appendLine("\n[FIM DO GABARITO - NÃO ALUCINE SOBRE ESTES DADOS]")
        }
    }

    private fun sugerirMagiasParaEscolas(quantidade: Int): List<String> {
        val sugestoes = listOf(
            "Localizar Alimento", "Localizar Ar", "Localizar Planta", "Apressar", 
            "Atrofiar (Sentido)", "Criação Inspirada", "Debilitar", "Endurecer", 
            "Hora Certa", "Localizar Fogo", "Localizar Terra", "Luz", "Purificar Ar",
            "Som", "Tatear"
        )
        return sugestoes.take(quantidade)
    }
}
