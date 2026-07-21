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
        val metaEscolaMax = metas
            .filter { it.tipo == ArcanoMetaTipo.ESCOLAS_DISTINTAS && !it.atendida }
            .maxOfOrNull { it.requerido - it.atual } ?: 0
            
        val outrasMetas = metas
            .filter { it.tipo != ArcanoMetaTipo.ESCOLAS_DISTINTAS }
            .sumOf { if (it.atendida) 0 else (it.requerido - it.atual).coerceAtLeast(1) }
            
        return metaEscolaMax + outrasMetas
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
        val defEscolas = coletarRegrasEscolasParaEstado(construirCadeiaObrigatoriaParaEstado(alvoId, known), known, estado)
            .maxOfOrNull { regra ->
                val set = escolasConhecidas(known).toMutableSet().also {
                    if (regra.outrasEscolas) it.removeAll(escolasNorm(regra.magiaOrigemId).toSet())
                }
                (regra.quantidadeEscolas - set.size).coerceAtLeast(0)
            } ?: 0
        val alvoPend = if (alvoId in known) 0 else 1
        return cadeiaPend + defEscolas + alvoPend
    }

    fun custoAcaoPlano(known: Set<String>, candId: String, cadeiaPendente: Set<String>): Int {
        val escolasAtuais = escolasConhecidas(known)
        val escolaCand = escolaPrincipalNorm(candId)
        val escolaRepetida = escolaCand.isNotBlank() && escolaCand in escolasAtuais
        
        // Se a magia é OBRIGATÓRIA para o alvo, não penalizamos escola repetida
        val penalidadeEscola = if (existeMetaEscolaPendente(known) && escolaRepetida && candId !in cadeiaPendente) {
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
        val escolaNova: Boolean,
        val complexidade: Int
    )

    fun ordenarCandidatas(
        cands: List<CandidataExpansao>,
        metaEscolaPendente: Boolean
    ): List<CandidataExpansao> {
        // Com meta de N escolas distintas (Desejo=15), TODA magia de escola
        // nova reduz a pendência igualmente — desempatar por 'reducao' não
        // diferencia e o A* se perde. Aqui, escola nova vira o critério
        // DOMINANTE e preferimos magias "raiz" baratas (complexidade baixa):
        // é o caminho mais curto p/ fechar muitas escolas.
        val cmp = if (metaEscolaPendente) {
            compareByDescending<CandidataExpansao> { it.escolaNova }
                .thenByDescending { it.reducao }
                .thenBy { it.complexidade }
                .thenBy { it.custoAcao }
                .thenBy { nomeMagiaNorm(it.id) }
        } else {
            compareByDescending<CandidataExpansao> { it.reducao }
                .thenByDescending { it.escolaNova }
                .thenBy { it.complexidade }
                .thenBy { it.custoAcao }
                .thenBy { nomeMagiaNorm(it.id) }
        }
        return cands.sortedWith(cmp)
    }

    fun candidatasExpandiveis(known: Set<String>, path: List<String>): List<CandidataExpansao> {
        val escolasAtuais = escolasConhecidas(known)
        val cadeiaPendente = construirCadeiaObrigatoriaParaEstado(alvoId, known)
            .filter { it !in known }
            .toSet()
            
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
                    custoAcao = custoAcaoPlano(known, id, cadeiaPendente),
                    reducao = reducaoPendencias(known, id),
                    escolaNova = escolaNova,
                    complexidade = custoAproximadoDependencia(id, known, mutableSetOf())
                )
            }
            .toList()

        val metaEscolaPendente = existeMetaEscolaPendente(known)
        val filtered = base.filter { cand ->
                // Se ainda precisamos de escolas, NÃO aceitamos repetir uma escola já conhecida
                // a menos que a magia seja um pré-requisito obrigatório nomeado.
                if (metaEscolaPendente && !cand.escolaNova) {
                    cand.id in cadeiaPendente
                } else {
                    true
                }
            }
            .toList()

        val ultimaEscola = path.lastOrNull()?.let { escolaPrincipalNorm(it) }.orEmpty()
        val semRepeticaoSequencial = if (metaEscolaPendente && ultimaEscola.isNotBlank()) {
            filtered.filterNot { cand ->
                cand.escola == ultimaEscola && cand.id !in cadeiaPendente
            }
        } else {
            filtered
        }
        
        // Com meta grande de escolas, alargar a expansão p/ caber magias
        // de escolas variadas (senão o A* nunca vê escolas suficientes).
        return ordenarCandidatas(semRepeticaoSequencial, metaEscolaPendente).take(larguraExpansao)
    }

    // ── ATALHO GULOSO ───────────────────────────────────────────────
    // Metas de "N escolas distintas" (Encantar=10, Desejo=15) tornam o
    // espaço de busca do A* combinatório → estoura memória/limite. Mas o
    // problema é GULOSO-ótimo: a cada passo basta pegar UMA magia
    // aprendível que (a) seja pré-requisito obrigatório pendente, ou
    // (b) abra uma escola NOVA barata. Repete até o alvo liberar.
    // O(passos × catálogo), determinístico, sem explosão. Se falhar,
    // cai no A* original (fallback abaixo).
    run {
        val known = estado.magiasConhecidasIds.toMutableSet()
        val trilha = mutableListOf<String>()
        val maxPassos = 40
        var passos = 0
        while (passos < maxPassos && !(magiaAprendivelAgora(alvoId, known, estado) || alvoId in known)) {
            passos++
            val cadeiaObrig = construirCadeiaObrigatoriaParaEstado(alvoId, known)
            val cadeiaPend = cadeiaObrig
                .filter { it != alvoId && it !in known }.toSet()
            val escolasAtuais = escolasConhecidas(known)
            val aprendiveis = allMagiaIds.asSequence()
                .filter { it !in known }
                .filterNot { escolaBloqueadaPorPolitica(it) }
                .filter { magiaAprendivelAgora(it, known, estado) }
                .toList()
            // 1) prioridade: pré-requisito obrigatório nomeado pendente
            val obrig = aprendiveis.firstOrNull { it in cadeiaPend }
            // 1.5) Lote 425: requisito "N magias da escola X" / "N magias quaisquer"
            //      pendente (ex: Proteger Animal = "3 mágicas sobre Animais"). O guloso
            //      antigo não sabia fechar isso — nenhuma magia da escola X abre escola
            //      NOVA nem é pré-req nomeado — então caía no A* sem gradiente, que numa
            //      ficha cheia estourava o heap (OOM). Aqui pegamos a magia aprendível
            //      MAIS BARATA que satisfaz a contagem pendente.
            val escolhaContagem: String? = if (obrig == null) {
                val regrasContagem = (cadeiaObrig + alvoId).asSequence()
                    .flatMap { requisitoBranchesPorMagia(it).asSequence() }
                    .flatMap { it.regrasNumericas.asSequence() }
                    .filter { (it.escolaRequerida != null && it.minMagiasEscola != null) || it.minMagiasQuaisquer != null }
                    .distinct()
                    .toList()
                var achou: String? = null
                // escola/tema específico primeiro (mais restritivo)
                for (r in regrasContagem) {
                    val esc = r.escolaRequerida; val minEsc = r.minMagiasEscola
                    if (esc != null && minEsc != null && contarMagiasPorEscolaOuNome(esc, known) < minEsc) {
                        achou = aprendiveis
                            .filter { magiaCasaEscolaOuTema(esc, it) }
                            .minByOrNull { custoAproximadoDependencia(it, known, mutableSetOf()) }
                        if (achou != null) break
                    }
                }
                // "N magias quaisquer": qualquer aprendível barata avança a contagem
                if (achou == null) {
                    for (r in regrasContagem) {
                        val minQ = r.minMagiasQuaisquer
                        if (minQ != null && known.size < minQ) {
                            achou = aprendiveis.minByOrNull { custoAproximadoDependencia(it, known, mutableSetOf()) }
                            if (achou != null) break
                        }
                    }
                }
                achou
            } else null
            val escolha = obrig ?: escolhaContagem ?: run {
                // 2) magia que abre escola NOVA, a mais barata (raiz)
                aprendiveis
                    .map { it to escolaPrincipalNorm(it) }
                    .filter { (_, esc) -> esc.isNotBlank() && esc !in escolasAtuais }
                    .minByOrNull { (id, _) -> custoAproximadoDependencia(id, known, mutableSetOf()) }
                    ?.first
            }
            if (escolha == null) break // guloso travou → usa A*
            known.add(escolha); trilha.add(escolha)
        }
        if (magiaAprendivelAgora(alvoId, known, estado) || alvoId in known) {
            val proxima = trilha.firstOrNull()
            val plano = ArcanoPlanoResultado(
                trilhaMagiasIds = trilha,
                explorados = passos,
                proximaAcaoMagiaId = proxima,
                metasImpactadasProximaAcao = proxima
                    ?.let { metasImpactadasPorAcao(alvoId, estado, estado.magiasConhecidasIds, it) }
                    .orEmpty()
            )
            cachePlanos[key] = plano
            return plano
        }
    }
    // ── FALLBACK: A* (cadeias com OU lógico / casos que o guloso não fecha)
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

    // GUARDA DE MEMÓRIA (Lote 425): numa ficha cheia cada nó do A* carrega um Set
    // e uma assinatura longos, e cada expansão avalia o catálogo inteiro (879 magias)
    // rodando diagnóstico de metas. Sem teto, um requisito que o guloso não fecha
    // (ex: "N magias da escola X" em fichas grandes) esgota o heap → OutOfMemoryError
    // que derruba o app (o crash aparecia na recomposição da navbar, mas a origem é
    // aqui). Escalonamos o limite de nós pelo tamanho da ficha e limitamos os mapas de
    // memo; se estourar o orçamento, devolvemos plano vazio (bloqueio honesto p/ o
    // modelo resolver/forçar) em vez de crashar.
    val tamFicha = startKnown.size
    val limiteNosEff = when {
        tamFicha > 40 -> 200
        tamFicha > 25 -> 500
        else -> limiteNos
    }
    val tetoMemo = 6000

    var explorados = 0
    while (open.isNotEmpty() && explorados < limiteNosEff &&
        metasMemo.size < tetoMemo && bestG.size < tetoMemo * 2) {
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

        val cadeiaPendente = construirCadeiaObrigatoriaParaEstado(alvoId, atual.known)
            .filter { it !in atual.known }
            .toSet()
            
        val expandiveis = candidatasExpandiveis(atual.known, atual.path)
        expandiveis.forEach { cand ->
            val novoKnown = atual.known + cand.id
            val g2 = atual.g + custoAcaoPlano(atual.known, cand.id, cadeiaPendente)
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
