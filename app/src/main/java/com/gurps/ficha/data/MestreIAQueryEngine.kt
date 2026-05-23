package com.gurps.ficha.data

import com.gurps.ficha.domain.filters.CatalogFilters

/**
 * MestreIAQueryEngine - Gera queries FTS5 válidas para o SQLite.
 *
 * FTS5 suporta: termo* (prefixo), "frase exata", termo1 OR termo2
 * Diferença do FTS4: parênteses agora são válidos, mas evitamos para consistência.
 * BM25 nativo do FTS5 ranqueia chunks por relevância real — não precisamos mais do
 * scoring total em Kotlin, apenas do re-scoring de proximidade/AND para refinamento.
 */
object MestreIAQueryEngine {

    private val stopWords = setOf(
        "como", "para", "com", "dos", "das", "pela", "pelo", "onde", "quando", "quem", "sao",
        "uma", "nos", "nas", "aos", "meu", "meus", "minha", "minhas", "seu", "seus", "sua", "suas",
        "esta", "esta", "estou", "que", "isso", "esse", "essa", "ser", "seja", "pode", "fazer",
        "qual", "quais", "ele", "ela", "eles", "elas", "estao", "gurps"
    )

    // Sinônimos técnicos de GURPS — geram termos adicionais planos (sem parênteses)
    // NOTA: prefixos aqui (ex: "agu") fazem match com qualquer token que COMEÇA com esse prefixo
    private val sinonimos = mapOf(
        // Colisão e impacto
        "colis" to listOf("colis", "encontr", "impact"),
        "impact" to listOf("colis", "encontr", "impact"),
        // Dano e ferimento
        "dano" to listOf("dano", "ferim", "lesao"),
        "ferim" to listOf("dano", "ferim", "lesao"),
        // Fadiga
        "fadig" to listOf("fadig", "cansac", "exaust"),
        "cansac" to listOf("fadig", "cansac"),
        // Combate à distância: atir/dispar/fogo são intercambiáveis
        "atir" to listOf("atir", "dispar", "fogo", "tiro"),
        "dispar" to listOf("atir", "dispar", "fogo", "tiro"),
        "tiro" to listOf("atir", "dispar", "fogo"),
        // Defesas
        "esquiv" to listOf("esquiv", "bloqu", "apar", "defes"),
        "bloqu" to listOf("esquiv", "bloqu", "apar", "defes"),
        "apar" to listOf("esquiv", "bloqu", "apar", "defes"),
        // Ambiente aquático — "underwat" aparece no search_text dos chunks do Pyramid Underwater Adventures
        "agu" to listOf("agu", "submers", "subaquat", "liquid", "underwat", "piscin", "mergulh"),
        "piscin" to listOf("agu", "submers", "subaquat", "piscin", "underwat", "mergulh"),
        "submers" to listOf("agu", "submers", "subaquat", "underwat"),
        "subaquat" to listOf("agu", "submers", "subaquat", "piscin", "underwat", "mergulh"),
        "mergulh" to listOf("agu", "submers", "subaquat", "underwat"),
        "underwat" to listOf("agu", "submers", "subaquat", "piscin", "mergulh"),
        // Alcance e distância
        "alcanc" to listOf("alcanc", "distanc", "range", "metro"),
        "distanc" to listOf("alcanc", "distanc", "range"),
        // Penalidades e modificadores
        "redut" to listOf("redut", "penal", "modif"),
        "penal" to listOf("redut", "penal", "modif", "malus"),
        "modif" to listOf("modif", "penal", "bonus", "ajust"),
        // Combate montado / cavalaria
        "cavalo" to listOf("cavalo", "cavalei", "cavalg", "montar", "montari"),
        "cavalei" to listOf("cavalo", "cavalei", "cavalg", "montar", "montari"),
        "cavalg" to listOf("cavalo", "cavalei", "cavalg", "montar", "montari"),
        "montar" to listOf("cavalo", "cavalei", "cavalg", "montar", "montari"),
        "montari" to listOf("cavalo", "cavalei", "cavalg", "montar", "montari"),
        "carga" to listOf("carga", "colisao", "colis", "impact", "encontr"),
        "lanca" to listOf("lanca", "arma", "dano"),
        "combat" to listOf("combat", "ataque", "defes", "dano"),
        // Armas de fogo específicas
        "pistol" to listOf("pistol", "revolv", "arma", "fogo", "dispar"),
        "revolv" to listOf("revolv", "pistol", "arma", "fogo", "dispar"),
        "espingard" to listOf("espingard", "shotgun", "arma", "fogo"),
        "rifl" to listOf("rifl", "arma", "fogo", "longa"),
        // Magia
        "magic" to listOf("magic", "feitiç", "encant", "conjur"),
        "feitiç" to listOf("magic", "feitiç", "encant"),
        // Cura e medicina
        "cur" to listOf("cur", "recuper", "medicin", "socorr"),
        "medicin" to listOf("medicin", "cur", "socorr"),
        // Queda e altitude
        "quet" to listOf("quet", "cai", "altitud", "impact"),
        "altitud" to listOf("altitud", "quet", "cai"),
        // Visibilidade
        "visibil" to listOf("visibil", "escurid", "iluminac", "nebl"),
        "escurid" to listOf("escurid", "visibil", "noite"),
        // Artes marciais
        "artes marciais" to listOf("artes", "marciais", "luta", "combat"),
        "marciai" to listOf("marciai", "artes", "luta", "combat"),
        // Armadura e proteção
        "armadur" to listOf("armadur", "protec", "blindag"),
        "armor" to listOf("armadur", "protec", "rd")
    )

    fun prepararQueryFTSAgressiva(userQuery: String, termosTecnicos: List<String>): String {
        // 1. Limpa e extrai tokens da query do usuário
        val cleanQuery = userQuery.replace(Regex("[^a-zA-ZáàâãéèêíïóôõöúçÁÀÂÃÉÈÊÍÏÓÔÕÖÚÇ0-9\\s]"), " ")
        val tokensUsuario = cleanQuery.split(Regex("\\s+"))
            .map { CatalogFilters.normalizarBusca(it) }
            .filter { it.length >= 3 && it !in stopWords && !it.all { c -> c.isDigit() } }

        // 2. Normaliza os termos técnicos do Planner
        val tokensTecnicos = termosTecnicos
            .map { CatalogFilters.normalizarBusca(it) }
            .filter { it.length >= 3 && it !in stopWords && !it.all { c -> c.isDigit() } }

        val todosTokens = (tokensUsuario + tokensTecnicos).distinct()

        if (todosTokens.isEmpty()) {
            // Fallback: usa as primeiras palavras da query original
            return userQuery.split(" ")
                .filter { it.length >= 3 }
                .take(5)
                .joinToString(" OR ") { "${CatalogFilters.normalizarBusca(it)}*" }
                .ifBlank { CatalogFilters.normalizarBusca(userQuery) }
        }

        // 3. Expande com sinônimos e gera lista PLANA de prefixos
        // NUNCA use parênteses em torno de wildcards — FTS4 não suporta (termo*)
        val prefixos = mutableSetOf<String>()
        for (token in todosTokens) {
            prefixos.add("${token}*")

            // Busca expansão por sinônimos usando prefixo do token
            for ((chave, expansao) in sinonimos) {
                if (token.startsWith(chave) || token.contains(chave)) {
                    expansao.forEach { prefixos.add("${it}*") }
                }
            }
        }

        // Siglas técnicas de GURPS — sem wildcard (são tokens exatos)
        if (tokensUsuario.any { it == "pv" || it == "vida" || it == "saude" }) prefixos.add("pv")
        if (tokensUsuario.any { it == "pf" || it == "fadig" }) prefixos.add("pf")
        if (tokensUsuario.any { it == "st" || it == "forca" }) prefixos.add("st")
        if (tokensUsuario.any { it == "dx" || it == "destrez" }) prefixos.add("dx")
        if (tokensUsuario.any { it == "iq" || it == "intelig" }) prefixos.add("iq")
        if (tokensUsuario.any { it == "ht" || it == "vitalid" }) prefixos.add("ht")

        // 4. Query final: termos planos separados por OR — sintaxe válida para FTS4
        return prefixos.take(20).joinToString(" OR ")
    }
}
