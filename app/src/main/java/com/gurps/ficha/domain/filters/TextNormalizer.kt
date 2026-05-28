package com.gurps.ficha.domain.filters

import java.text.Normalizer
import java.util.Locale

/**
 * Normalizador de texto canônico do projeto.
 *
 * Substitui as 7 implementações paralelas de "normalizar texto pra busca/comparação"
 * que existiam em arquivos diferentes (ver RELATORIO_DRY_DUPLICACOES.md §1).
 *
 * Cada modo replica exatamente o comportamento de uma das variantes originais —
 * a migração das chamadas é feita preservando comportamento, não unificando "pela média".
 *
 * Lote 314 — Plano §1 Etapa 2.
 */
object TextNormalizer {

    data class Options(
        val fixMojibake: Boolean = false,
        val stripParens: Boolean = false,
        val replaceNonAlphanumWithSpace: Boolean = true,
        val preserveChars: String = "",
        val stemPluralS: Boolean = false,
        val collapseWhitespace: Boolean = true,
    )

    val SIMPLE = Options(
        replaceNonAlphanumWithSpace = false,
    )

    val BUSCA_PADRAO = Options(
        fixMojibake = true,
    )

    val PERICIA_RAW = Options(
        preserveChars = "/+_-",
    )

    val ARMA_GRUPO = Options(
        stripParens = true,
        stemPluralS = true,
    )

    fun normalize(text: String?, opts: Options = BUSCA_PADRAO): String {
        if (text.isNullOrEmpty()) return ""

        var res = text

        if (opts.fixMojibake) {
            res = res
                .replace("ǜ", "a")
                .replace("ǭ", "a")
                .replace("Ǹ", "e")
                .replace("Ǧ", "e")
                .replace("Ǭ", "o")
                .replace("ǽ", "a")
        }

        if (opts.stripParens && res.contains("(")) {
            res = res.substringBefore("(").trim()
        }

        res = res.lowercase(Locale.ROOT)

        val nfd = Normalizer.normalize(res, Normalizer.Form.NFD)
        res = nfd.replace(Regex("\\p{M}+"), "")

        if (opts.replaceNonAlphanumWithSpace) {
            val keep = if (opts.preserveChars.isEmpty()) {
                "a-z0-9\\s"
            } else {
                "a-z0-9\\s" + Regex.escape(opts.preserveChars)
            }
            res = res.replace(Regex("[^$keep]"), " ")
        }

        if (opts.stemPluralS) {
            res = res.split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { word ->
                    if (word.length > 3 && word.endsWith("s")) {
                        word.substring(0, word.length - 1)
                    } else {
                        word
                    }
                }
        }

        if (opts.collapseWhitespace) {
            res = res.replace(Regex("\\s+"), " ").trim()
        }

        return res
    }

    fun contains(haystack: String, needle: String, opts: Options = BUSCA_PADRAO): Boolean {
        if (needle.isBlank()) return true
        val needleNorm = normalize(needle, opts)
        if (needleNorm.isBlank()) return true
        return normalize(haystack, opts).contains(needleNorm)
    }
}
