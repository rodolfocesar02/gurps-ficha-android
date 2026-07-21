package nexus.arcano

import nexus.arcano.NexusArcanoEngine.AvaliacaoCandidata
import nexus.arcano.NexusArcanoEngine.RequisitoBranch

internal fun NexusArcanoEngine.sugerirProximasAcoes(
    alvoId: String,
    known: Set<String>,
    estado: ArcanoEstadoPersonagem
): List<ArcanoAcao> {
    val cadeiaIntegral = construirCadeiaObrigatoriaParaEstado(alvoId, known)
    val pendentes = cadeiaIntegral.filter { it !in known }
    val out = mutableListOf<ArcanoAcao>()
    val escolasSugeridasGlobal = escolasConhecidas(known).toMutableSet()
    
    // 1. Prioridade Máxima: Próximas magias da trilha que já podem ser aprendidas agora
    // Blindagem V5: Filtramos magias que repetem escola se ainda houver metas de escola pendentes no futuro
    val metasGerais = diagnosticarMetasAlvo(alvoId, estado.copy(magiasConhecidasIds = known))
    val metaEscolaPendente = metasGerais.any { it.tipo == ArcanoMetaTipo.ESCOLAS_DISTINTAS && !it.atendida }

    val imediatas = pendentes.filter { id ->
        if (!magiaAprendivelAgora(id, known, estado)) return@filter false
        if (escolaBloqueadaPorPolitica(id)) return@filter false
        
        // Prioritariamente, se a magia pertence ao próximo degrau direto da cadeia, sugerimos sem filtro de escola repetida
        val ehProximoPassoDireto = id == pendentes.firstOrNull()
        if (ehProximoPassoDireto) return@filter true

        if (metaEscolaPendente && escolaPrincipalNorm(id) in escolasSugeridasGlobal) {
            false 
        } else true
    }
    imediatas.take(3).forEach { id ->
        out += ArcanoAcao(id, "Cadeia obrigatória (Caminho mais rápido)", 0)
        escolasSugeridasGlobal.addAll(escolasNorm(id))
    }

    // 2. Lookahead: Buscar a primeira meta de escolas pendente em qualquer ponto da cadeia
    // Ordenamos por proximidade do aprendizado (melhor primeiro)
    pendentes.forEach { magiaId ->
        if (out.size >= 5) return@forEach
        if (bloqueioNumericoParaMagia(magiaId, estado, known) == null) {
            // Se ainda houver qualquer meta de escola pendente na cadeia de metas do alvo, persistir nas sugestões de escolas
            val metasGerais = diagnosticarMetasAlvo(alvoId, estado.copy(magiasConhecidasIds = known))
            val escolasPendentesNaCadeia = metasGerais.any { it.tipo == ArcanoMetaTipo.ESCOLAS_DISTINTAS && !it.atendida }
            if (!escolasPendentesNaCadeia) return@forEach

            val sugestoes = sugerirParaRegraDeEscolas(
                magiaId = magiaId,
                known = known,
                estado = estado,
                idsProibidos = pendentes.toSet() + out.map { it.magiaId }.toSet(),
                escolasProibidasGlobal = escolasSugeridasGlobal
            )
            if (sugestoes.isNotEmpty()) {
                sugestoes.forEach { acao ->
                    escolasSugeridasGlobal.addAll(escolasNorm(acao.magiaId))
                }
                out.addAll(sugestoes)
                // OTIMIZAÇÃO V5: Se já encontramos sugestões para a primeira meta pendente,
                // paramos aqui para evitar "ansiedade" e sobreposição de escolas futuras.
                return out
                    .filter { it.magiaId !in known }
                    .distinctBy { it.magiaId }
                    .sortedWith(compareBy<ArcanoAcao> { it.prioridade }.thenBy { nomeMagiaNorm(it.magiaId) })
                    .take(5)
            }
        }
    }

    return out
        .filter { it.magiaId !in known }
        .distinctBy { it.magiaId }
        .sortedWith(compareBy<ArcanoAcao> { it.prioridade }.thenBy { nomeMagiaNorm(it.magiaId) })
        .take(5)
}

internal fun NexusArcanoEngine.sugerirParaRegraDeEscolas(
    magiaId: String,
    known: Set<String>,
    estado: ArcanoEstadoPersonagem,
    idsProibidos: Set<String>,
    escolasProibidasGlobal: Set<String>
): List<ArcanoAcao> {
    val regras = coletarRegrasEscolasParaEstado(listOf(magiaId), known, estado)
    if (regras.isEmpty()) return emptyList()

    val avaliados = avaliarCandidatasParaRegraDeEscolas(
        magiaId = magiaId,
        known = known,
        estado = estado,
        idsProibidos = idsProibidos
    ).filter { it.elegivel && it.escola !in escolasProibidasGlobal }

    val escolasUsadasNaRodada = mutableSetOf<String>()
    val ordenados = avaliados.sortedWith(
        compareByDescending<AvaliacaoCandidata> { it.escolaNova }
            .thenBy { it.custo }
            .thenBy { if (preRequisitoSemConteudo(it.id)) 0 else 1 } // Prioriza magias raiz
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
    // Passo 2 (Fallback Removido): Não sugerimos mais escolas repetidas como fallback
    // para evitar que o usuário aprenda magias redundantes sem necessidade.
    return out
}

internal fun NexusArcanoEngine.avaliarCandidatasParaRegraDeEscolas(
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
            if (atendeRegraNumerica(regraNum, estado, known)) 0 else 1
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

internal fun NexusArcanoEngine.escolherBranchRelevante(
    magiaId: String,
    known: Set<String>,
    estado: ArcanoEstadoPersonagem
): RequisitoBranch? {
    val branches = requisitoBranchesPorMagia(magiaId)
    if (branches.isEmpty()) return null
    return branches.minWithOrNull(
        compareBy<RequisitoBranch> { branch -> branch.dependencias.count { it !in known } }
            .thenBy { branch -> branch.regrasEscolas.count { !atendeRegraEscolas(it, known) } }
            .thenBy { branch -> branch.regrasNumericas.count { !atendeRegraNumerica(it, estado, known) } }
            .thenBy { branch -> branch.vantagensRequeridas.count { !atendeVantagemRequerida(it, estado) } }
            .thenBy { branch -> branch.gruposDependenciaOu.count { grupo -> grupo.none { it in known } } }
            .thenBy { it.dependencias.size }
            .thenBy { it.regrasEscolas.size }
            .thenBy { it.regrasNumericas.size }
            .thenBy { branchKey(it) }
    )
}

internal fun NexusArcanoEngine.bloqueioNumericoParaMagia(
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

internal fun NexusArcanoEngine.primeiroBloqueioNumerico(
    alvoId: String,
    cadeiaSemAlvo: List<String>,
    estado: ArcanoEstadoPersonagem,
    known: Set<String>
): String? {
    val alvoDeBloqueio = cadeiaSemAlvo.firstOrNull { it !in known } ?: alvoId
    return bloqueioNumericoParaMagia(alvoDeBloqueio, estado, known)
}

internal fun NexusArcanoEngine.codigoBloqueio(
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
