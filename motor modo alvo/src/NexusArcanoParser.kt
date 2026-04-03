package nexus.arcano

import java.util.concurrent.ConcurrentHashMap
import nexus.arcano.NexusArcanoEngine.RegraEscolas
import nexus.arcano.NexusArcanoEngine.RegraNumerica
import nexus.arcano.NexusArcanoEngine.SnapshotAlvo

internal fun NexusArcanoEngine.regrasEscolasRelevantes(
    magiaId: String,
    known: Set<String>,
    estado: ArcanoEstadoPersonagem
): List<RegraEscolas> {
    val branch = escolherBranchRelevante(magiaId, known, estado)
    // Se temos um ramo identificado, usamos as regras dele. 
    // Magias sem regras de escola retornarão lista vazia aqui, o que é o correto para o ramo escolhido.
    return branch?.regrasEscolas ?: emptyList()
}

internal fun NexusArcanoEngine.regrasNumericasRelevantes(
    magiaId: String,
    known: Set<String>,
    estado: ArcanoEstadoPersonagem
): List<RegraNumerica> {
    val branch = escolherBranchRelevante(magiaId, known, estado)
    return branch?.regrasNumericas ?: emptyList()
}

internal fun NexusArcanoEngine.coletarRegrasEscolasParaEstado(
    ids: List<String>,
    known: Set<String>,
    estado: ArcanoEstadoPersonagem
): List<RegraEscolas> {
    return ids
        .distinct()
        .flatMap { regrasEscolasRelevantes(it, known, estado) }
}

internal fun NexusArcanoEngine.coletarRegrasNumericasParaEstado(
    ids: List<String>,
    known: Set<String>,
    estado: ArcanoEstadoPersonagem
): List<RegraNumerica> {
    return ids
        .distinct()
        .flatMap { regrasNumericasRelevantes(it, known, estado) }
}

internal fun NexusArcanoEngine.snapshotAlvo(alvoId: String): SnapshotAlvo {
    return snapshotCache.getOrPut(alvoId) {
        val cadeia = construirCadeiaObrigatoria(alvoId)
        val cadeiaSemAlvo = cadeia.filter { it != alvoId }
        SnapshotAlvo(
            alvoId = alvoId,
            cadeiaSemAlvo = cadeiaSemAlvo,
            regrasEscolas = coletarRegrasEscolasGlobais(cadeia),
            regrasNumericas = coletarRegrasNumericasGlobais(cadeia)
        )
    }
}

internal fun NexusArcanoEngine.coletarRegrasEscolasGlobais(ids: List<String>): List<RegraEscolas> {
    return ids
        .distinct()
        .flatMap { regrasEscolasPorMagia(it) }
}

internal fun NexusArcanoEngine.coletarRegrasNumericasGlobais(ids: List<String>): List<RegraNumerica> {
    return ids
        .distinct()
        .flatMap { regrasNumericasPorMagia(it) }
}

internal fun NexusArcanoEngine.chavesEsperadasOrdem(snapshot: SnapshotAlvo): List<String> {
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

internal fun NexusArcanoEngine.regrasEscolasPorMagia(magiaId: String): List<RegraEscolas> {
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

internal fun NexusArcanoEngine.regrasNumericasPorMagia(magiaId: String): List<RegraNumerica> {
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

internal fun NexusArcanoEngine.parseNumeroToken(raw: String): Int? {
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
