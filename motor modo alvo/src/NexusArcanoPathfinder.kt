package nexus.arcano

fun NexusArcanoEngine.planejarCaminhoMinimo(
    alvoId: String,
    estado: ArcanoEstadoPersonagem,
    limiteNos: Int = 1800,
    larguraExpansao: Int = 20,
    profundidadeMax: Int = 28
): ArcanoPlanoResultado {
    val key = cacheKey(alvoId, estado.magiasConhecidasIds, estado)
    cachePlanos[key]?.let {
        cacheHits += 1
        return it
    }
    cacheMisses += 1

    if (!catalogo.existe(alvoId)) {
        val out = ArcanoPlanoResultado(emptyList(), explorados = 0, motivo = "Alvo não encontrado no catálogo.")
        cachePlanos[key] = out
        return out
    }
    val bloqueioNumericoAlvo = bloqueioNumericoParaMagia(alvoId, estado, estado.magiasConhecidasIds)
    if (bloqueioNumericoAlvo != null) {
        val out = ArcanoPlanoResultado(emptyList(), explorados = 0, motivo = bloqueioNumericoAlvo)
        cachePlanos[key] = out
        return out
    }

    data class Node(
        val known: Set<String>,
        val path: List<String>,
        val g: Int,
        val f: Int
    )

    fun assinatura(known: Set<String>): String = known.sorted().joinToString("|")
    val metasMemo = mutableMapOf<String, List<ArcanoMetaProgress>>()

    fun metasEstado(known: Set<String>): List<ArcanoMetaProgress> {
        val sig = assinatura(known)
        return metasMemo.getOrPut(sig) {
            diagnosticarMetasAlvo(alvoId, estado.copy(magiasConhecidasIds = known))
        }
    }

    fun scorePendencias(metas: List<ArcanoMetaProgress>): Int {
        return metas.sumOf { meta ->
            if (meta.atendida) 0 else (meta.requerido - meta.atual).coerceAtLeast(1)
        }
    }

    fun reducaoPendencias(known: Set<String>, candId: String): Int {
        val antes = metasEstado(known)
        val depois = metasEstado(known + candId)
        val pontuacaoAntes = scorePendencias(antes)
        val pontuacaoDepois = scorePendencias(depois)
        return (pontuacaoAntes - pontuacaoDepois).coerceAtLeast(0)
    }

    fun existeMetaEscolaPendente(known: Set<String>): Boolean {
        return metasEstado(known).any { it.tipo == ArcanoMetaTipo.ESCOLAS_DISTINTAS && !it.atendida }
    }

    fun estimativaRestante(known: Set<String>): Int {
        if (magiaAprendivelAgora(alvoId, known, estado) || alvoId in known) return 0
        val cadeiaPend = construirCadeiaObrigatoriaParaEstado(alvoId, known)
            .count { it != alvoId && it !in known }
        val defEscolas = coletarRegrasEscolas(construirCadeiaObrigatoriaParaEstado(alvoId, known))
            .maxOfOrNull { regra ->
                val set = escolasConhecidas(known).toMutableSet().also {
                    if (regra.outrasEscolas) it.removeAll(escolasNorm(regra.magiaOrigemId).toSet())
                }
                (regra.quantidadeEscolas - set.size).coerceAtLeast(0)
            } ?: 0
        val alvoPend = if (alvoId in known) 0 else 1
        return cadeiaPend + defEscolas + alvoPend
    }

    fun custoAcaoPlano(known: Set<String>, candId: String): Int {
        val escolasAtuais = escolasConhecidas(known)
        val escolaCand = escolaPrincipalNorm(candId)
        val escolaRepetida = escolaCand.isNotBlank() && escolaCand in escolasAtuais
        val penalidadeEscola = if (existeMetaEscolaPendente(known) && escolaRepetida) {
            penalidadePlanoEscolaRepetida
        } else {
            0
        }
        val reduziuPendencia = reducaoPendencias(known, candId) > 0
        val penalidadeSemReducao = if (reduziuPendencia) 0 else penalidadePlanoSemReducaoMeta
        return custoBasePlano + penalidadeEscola + penalidadeSemReducao
    }

    data class CandidataExpansao(
        val id: String,
        val escola: String,
        val custoAcao: Int,
        val reducao: Int,
        val escolaNova: Boolean
    )

    fun ordenarCandidatas(cands: List<CandidataExpansao>): List<CandidataExpansao> {
        return cands.sortedWith(
            compareByDescending<CandidataExpansao> { it.reducao }
                .thenByDescending { it.escolaNova }
                .thenBy { it.custoAcao }
                .thenBy { nomeMagiaNorm(it.id) }
        )
    }

    fun candidatasExpandiveis(known: Set<String>, path: List<String>): List<CandidataExpansao> {
        val escolasAtuais = escolasConhecidas(known)
        val base = allMagiaIds.asSequence()
            .filter { it !in known }
            .filterNot { escolaBloqueadaPorPolitica(it) }
            .filter { magiaAprendivelAgora(it, known, estado) }
            .map { id ->
                val escola = escolaPrincipalNorm(id)
                val escolaNova = escola.isNotBlank() && escola !in escolasAtuais
                CandidataExpansao(
                    id = id,
                    escola = escola,
                    custoAcao = custoAcaoPlano(known, id),
                    reducao = reducaoPendencias(known, id),
                    escolaNova = escolaNova
                )
            }
            .toList()

        val metaEscolaPendente = existeMetaEscolaPendente(known)
        val ultimaEscola = path.lastOrNull()?.let { escolaPrincipalNorm(it) }.orEmpty()
        val cadeiaPendente = construirCadeiaObrigatoriaParaEstado(alvoId, known)
            .filter { it !in known }
            .toSet()
        val semRepeticaoSequencial = if (metaEscolaPendente && ultimaEscola.isNotBlank()) {
            base.filterNot { cand ->
                cand.escola == ultimaEscola && cand.id !in cadeiaPendente
            }
        } else {
            base
        }
        val efetivas = if (semRepeticaoSequencial.isNotEmpty()) semRepeticaoSequencial else base
        return ordenarCandidatas(efetivas).take(larguraExpansao)
    }

    val open = java.util.PriorityQueue<Node>(compareBy<Node> { it.f }.thenBy { it.g })
    val bestG = HashMap<String, Int>()
    val startKnown = estado.magiasConhecidasIds
    val start = Node(
        known = startKnown,
        path = emptyList(),
        g = 0,
        f = estimativaRestante(startKnown)
    )
    open.add(start)
    bestG[assinatura(startKnown)] = 0

    var explorados = 0
    while (open.isNotEmpty() && explorados < limiteNos) {
        val atual = open.poll() ?: break
        explorados++

        if (magiaAprendivelAgora(alvoId, atual.known, estado) || alvoId in atual.known) {
            val proximaAcao = atual.path.firstOrNull()
            val metasImpactadas = proximaAcao
                ?.let { metasImpactadasPorAcao(alvoId, estado, startKnown, it) }
                .orEmpty()
            val plano = ArcanoPlanoResultado(
                trilhaMagiasIds = atual.path,
                explorados = explorados,
                proximaAcaoMagiaId = proximaAcao,
                metasImpactadasProximaAcao = metasImpactadas
            )
            cachePlanos[key] = plano
            return plano
        }
        if (atual.g >= profundidadeMax) continue

        val expandiveis = candidatasExpandiveis(atual.known, atual.path)
        expandiveis.forEach { cand ->
            val novoKnown = atual.known + cand.id
            val g2 = atual.g + cand.custoAcao
            val sig = assinatura(novoKnown)
            val prev = bestG[sig]
            if (prev != null && prev <= g2) return@forEach
            bestG[sig] = g2
            val h2 = estimativaRestante(novoKnown)
            open.add(
                Node(
                    known = novoKnown,
                    path = atual.path + cand.id,
                    g = g2,
                    f = g2 + h2
                )
            )
        }
    }

    val out = ArcanoPlanoResultado(
        trilhaMagiasIds = emptyList(),
        explorados = explorados,
        motivo = "Plano global não encontrado dentro do limite."
    )
    cachePlanos[key] = out
    return out
}
