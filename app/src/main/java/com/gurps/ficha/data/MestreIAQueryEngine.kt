package com.gurps.ficha.data

import com.gurps.ficha.domain.filters.CatalogFilters

/**
 * MestreIAQueryEngine — Gera queries FTS4 válidas para o SQLite.
 *
 * FTS4 suporta: termo* (prefixo), termo1 OR termo2
 * Parênteses em torno de wildcards NÃO são suportados — sempre tokens planos com OR.
 *
 * Mudança Lote 270: tokens ordenados por relevância semântica antes de construir a query.
 * Núcleo da pergunta (peso 1.0+) aparece primeiro → FTS4 prioriza documentos que os contêm.
 * Expansão de sinônimos somente para termos do núcleo — evita amplificação de contexto.
 */
object MestreIAQueryEngine {

    /**
     * Termo com peso explícito (Lote 349: movida de MestreIAPlanner.kt, deletado).
     * peso=1.0 → núcleo da pergunta (sujeito principal)
     * peso=0.6 → entidade secundária (alvo ou contexto)
     * peso=0.3 → expansão semântica (sinônimos)
     */
    data class TermoPonderado(
        val termo: String,
        val peso: Double
    )

    private val stopWords = setOf(
        "como", "para", "com", "dos", "das", "pela", "pelo", "onde", "quando", "quem", "sao",
        "uma", "nos", "nas", "aos", "meu", "meus", "minha", "minhas", "seu", "seus", "sua", "suas",
        "esta", "estou", "que", "isso", "esse", "essa", "ser", "seja", "pode", "fazer",
        "qual", "quais", "ele", "ela", "eles", "elas", "estao", "gurps",
        "posso", "possivel", "usar", "utilizar", "aplicar", "contra", "sobre",
        "se", "ou", "ao", "e", "a", "o", "as", "os"
    )

    // Sinônimos técnicos de GURPS — expansão APENAS direta (chave → valores).
    // Sem matching bidirecional para evitar expansão não controlada.
    private val sinonimos = mapOf(
        "colis"     to listOf("colis", "encontr", "impact"),
        "impact"    to listOf("colis", "encontr", "impact"),
        "dano"      to listOf("dano", "ferim", "lesao"),
        "ferim"     to listOf("dano", "ferim", "lesao"),
        "fadig"     to listOf("fadig", "cansac", "exaust"),
        "cansac"    to listOf("fadig", "cansac"),
        "atir"      to listOf("atir", "dispar", "fogo", "tiro"),
        "dispar"    to listOf("atir", "dispar", "fogo", "tiro"),
        "tiro"      to listOf("atir", "dispar", "fogo"),
        "esquiv"    to listOf("esquiv", "bloqu", "apar", "defes"),
        "bloqu"     to listOf("esquiv", "bloqu", "apar", "defes"),
        "apar"      to listOf("esquiv", "bloqu", "apar", "defes"),
        "agu"       to listOf("agu", "submers", "subaquat", "liquid", "underwat", "piscin", "mergulh"),
        "piscin"    to listOf("agu", "submers", "subaquat", "piscin", "underwat", "mergulh"),
        "submers"   to listOf("agu", "submers", "subaquat", "underwat"),
        "subaquat"  to listOf("agu", "submers", "subaquat", "piscin", "underwat", "mergulh"),
        "mergulh"   to listOf("agu", "submers", "subaquat", "underwat"),
        "underwat"  to listOf("agu", "submers", "subaquat", "piscin", "mergulh"),
        "alcanc"    to listOf("alcanc", "distanc", "range", "metro"),
        "distanc"   to listOf("alcanc", "distanc", "range"),
        "redut"     to listOf("redut", "penal", "modif"),
        "penal"     to listOf("redut", "penal", "modif", "malus"),
        "modif"     to listOf("modif", "penal", "bonus", "ajust"),
        "cavalo"    to listOf("cavalo", "cavalei", "cavalg", "montar", "montari"),
        "cavalei"   to listOf("cavalo", "cavalei", "cavalg", "montar", "montari"),
        "cavalg"    to listOf("cavalo", "cavalei", "cavalg", "montar", "montari"),
        "montar"    to listOf("cavalo", "cavalei", "cavalg", "montar", "montari"),
        "montari"   to listOf("cavalo", "cavalei", "cavalg", "montar", "montari"),
        "combat"    to listOf("combat", "ataque", "defes", "dano"),
        "pistol"    to listOf("pistol", "revolv", "arma", "fogo", "dispar"),
        "revolv"    to listOf("revolv", "pistol", "arma", "fogo", "dispar"),
        "espingard" to listOf("espingard", "shotgun", "arma", "fogo"),
        "rifl"      to listOf("rifl", "arma", "fogo", "longa"),
        "magic"     to listOf("magic", "feitico", "encant", "conjur"),
        "feitico"   to listOf("magic", "feitico", "encant"),
        "cur"       to listOf("cur", "recuper", "medicin", "socorr"),
        "medicin"   to listOf("medicin", "cur", "socorr"),
        "altitud"   to listOf("altitud", "quet", "cai"),
        "visibil"   to listOf("visibil", "escurid", "iluminac", "nebl"),
        "escurid"   to listOf("escurid", "visibil", "noite"),
        "marciai"   to listOf("marciai", "artes", "luta", "combat"),
        "armadur"   to listOf("armadur", "protec", "blindag"),
        "armor"     to listOf("armadur", "protec", "rd")
    )

    /**
     * Gera query FTS4 a partir da pergunta e dos termos ponderados do Planner.
     *
     * Ordem dos tokens na query resultante:
     *   1. Termos de núcleo (peso >= 1.0) — aparecem primeiro
     *   2. Expansão de sinônimos dos termos de núcleo
     *   3. Termos de contexto (peso 0.6) — aparecem depois
     *   4. Termos gerais da query original (sem classificação explícita)
     *
     * Limite: 20 prefixos. FTS4 não ordena por posição no OR, mas ter os termos
     * relevantes no início garante que o pool inicial de candidatos os contenha.
     */
    fun prepararQueryFTSAgressiva(
        userQuery: String,
        termosTecnicos: List<String>,
        termosPonderados: List<TermoPonderado> = emptyList()
    ): String {
        val cleanQuery = userQuery.replace(Regex("[^a-zA-ZáàâãéèêíïóôõöúçÁÀÂÃÉÈÊÍÏÓÔÕÖÚÇ0-9\\s]"), " ")
        val tokensUsuario = cleanQuery.split(Regex("\\s+"))
            .map { CatalogFilters.normalizarBusca(it) }
            .filter { it.length >= 3 && it !in stopWords && !it.all { c -> c.isDigit() } }

        val tokensTecnicos = termosTecnicos
            .map { CatalogFilters.normalizarBusca(it) }
            .filter { it.length >= 3 && it !in stopWords && !it.all { c -> c.isDigit() } }

        if ((tokensUsuario + tokensTecnicos).isEmpty()) {
            return userQuery.split(" ")
                .filter { it.length >= 3 }
                .take(5)
                .joinToString(" OR ") { "${CatalogFilters.normalizarBusca(it)}*" }
                .ifBlank { CatalogFilters.normalizarBusca(userQuery) }
        }

        // Separa termos por camada de relevância
        val termosNucleo = termosPonderados
            .filter { it.peso >= 1.0 }
            .map { CatalogFilters.normalizarBusca(it.termo) }
            .filter { it.length >= 3 && it !in stopWords }

        val termosContexto = termosPonderados
            .filter { it.peso < 1.0 }
            .map { CatalogFilters.normalizarBusca(it.termo) }
            .filter { it.length >= 3 && it !in stopWords }

        // Expansão de sinônimos — somente para termos do núcleo
        val expansaoNucleo = mutableListOf<String>()
        for (token in termosNucleo) {
            for ((chave, expansao) in sinonimos) {
                if (token.startsWith(chave)) {
                    expansao.forEach { expansaoNucleo.add(it) }
                }
            }
        }

        // Siglas técnicas de GURPS (tokens exatos, sem wildcard)
        val siglas = mutableListOf<String>()
        val todosTokensBrutos = tokensUsuario + tokensTecnicos + termosNucleo
        if (todosTokensBrutos.any { it == "pv" || it == "vida" || it == "saude" }) siglas.add("pv")
        if (todosTokensBrutos.any { it == "pf" || it.startsWith("fadig") }) siglas.add("pf")
        if (todosTokensBrutos.any { it == "st" || it.startsWith("forca") }) siglas.add("st")
        if (todosTokensBrutos.any { it == "dx" || it.startsWith("destrez") }) siglas.add("dx")
        if (todosTokensBrutos.any { it == "iq" || it.startsWith("intelig") }) siglas.add("iq")
        if (todosTokensBrutos.any { it == "ht" || it.startsWith("vitalid") }) siglas.add("ht")

        // Monta prefixos em ordem de prioridade: núcleo → expansão núcleo → contexto → geral
        val prefixosOrdenados = mutableListOf<String>()

        termosNucleo.forEach { prefixosOrdenados.add("${it}*") }
        expansaoNucleo.distinct().forEach {
            val pref = "${it}*"
            if (pref !in prefixosOrdenados) prefixosOrdenados.add(pref)
        }
        termosContexto.forEach {
            val pref = "${it}*"
            if (pref !in prefixosOrdenados) prefixosOrdenados.add(pref)
        }
        (tokensUsuario + tokensTecnicos).forEach {
            val pref = "${it}*"
            if (pref !in prefixosOrdenados) prefixosOrdenados.add(pref)
        }
        siglas.forEach {
            if (it !in prefixosOrdenados) prefixosOrdenados.add(it)
        }

        return prefixosOrdenados.distinct().take(20).joinToString(" OR ")
    }
}
