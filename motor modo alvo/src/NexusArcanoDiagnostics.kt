package nexus.arcano

import java.security.MessageDigest
import nexus.arcano.NexusArcanoEngine.AvaliacaoCandidata

fun NexusArcanoEngine.diagnosticarRankingAlvo(alvoId: String, estado: ArcanoEstadoPersonagem): List<ArcanoRankingDiagnostico> {
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

fun NexusArcanoEngine.diagnosticarMetasAlvo(alvoId: String, estado: ArcanoEstadoPersonagem): List<ArcanoMetaProgress> {
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

fun NexusArcanoEngine.checksumMetasAlvo(alvoId: String, estado: ArcanoEstadoPersonagem): String {
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
