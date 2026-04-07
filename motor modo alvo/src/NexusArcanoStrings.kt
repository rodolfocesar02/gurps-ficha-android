package nexus.arcano

import java.security.MessageDigest
import java.text.Normalizer

internal fun NexusArcanoEngine.sha256Hex(raw: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
    return digest.joinToString(separator = "") { byte ->
        val value = byte.toInt() and 0xff
        val token = value.toString(16)
        if (token.length == 1) "0$token" else token
    }
}

internal fun NexusArcanoEngine.preRequisitoSemConteudo(magiaId: String): Boolean {
    val raw = preRaw(magiaId).trim().lowercase()
    if (raw.isBlank()) return true
    if (raw in setOf("-", "—", "–", "−", "?", "??", "???", "â€”", "â€“", "âˆ’")) return true
    val norm = preNorm(magiaId)
    return norm.isBlank()
}

internal fun NexusArcanoEngine.nomeMagia(magiaId: String): String = nomeById[magiaId] ?: catalogo.nome(magiaId)

internal fun NexusArcanoEngine.nomeMagiaNorm(magiaId: String): String = nomeNormById[magiaId] ?: normalize(nomeMagia(magiaId))

internal fun NexusArcanoEngine.preRaw(magiaId: String): String = preRawById[magiaId] ?: catalogo.preRequisitoRaw(magiaId)

internal fun NexusArcanoEngine.preNorm(magiaId: String): String = preNormById[magiaId] ?: normalize(preRaw(magiaId))

internal fun NexusArcanoEngine.escolasNorm(magiaId: String): List<String> = escolasNormById[magiaId]
    ?: catalogo.escolas(magiaId).map(::normalize).filter { it.isNotBlank() }

internal fun NexusArcanoEngine.escolaPrincipalNorm(magiaId: String): String = escolaPrincipalNormById[magiaId]
    ?: escolasNorm(magiaId).firstOrNull().orEmpty()

internal fun NexusArcanoEngine.escolaBloqueadaPorPolitica(magiaId: String): Boolean {
    val escola = escolaPrincipalNorm(magiaId)
    return escola in escolasNuncaRecomendar
}

internal fun NexusArcanoEngine.variantesSingularPlural(nomeNorm: String): Set<String> {
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

internal fun NexusArcanoEngine.singularizarTokenPt(token: String): String {
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

internal fun NexusArcanoEngine.pluralizarTokenPt(token: String): String {
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

internal fun NexusArcanoEngine.normalize(raw: String): String {
    val semAcento = Normalizer.normalize(raw, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
    return semAcento
        .lowercase()
        .replace(Regex("[^a-z0-9\\s]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

internal fun NexusArcanoEngine.pareceReferenciaDeEscola(raw: String, start: Int, endExclusive: Int): Boolean {
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

internal fun NexusArcanoEngine.valorAtributo(nome: String, estado: ArcanoEstadoPersonagem): Int {
    return when (normalize(nome)) {
        "am" -> estado.am
        "iq" -> estado.iq
        "dx" -> estado.dx
        else -> 0
    }
}
