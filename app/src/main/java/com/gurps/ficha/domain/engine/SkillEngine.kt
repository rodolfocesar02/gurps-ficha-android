package com.gurps.ficha.domain.engine

import com.gurps.ficha.model.*
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.domain.filters.TextNormalizer

/**
 * Foundation for GURPS Skills and Techniques rules.
 * Handles complex name matching, technique limits, and specialization rules.
 */
object SkillEngine {

    private val PERICIAS_COMBATE = com.gurps.ficha.model.PERICIAS_COMBATE

    enum class TecnicaLimiteKind {
        NENHUM,
        EXPLICITO_RELATIVO,
        PERICIA_BASE,
        PREDEFINIDO_APARAR,
        PREDEFINIDO_BLOQUEAR,
        METADE_PENALIDADE
    }

    data class TecnicaRegraPerfil(
        val limiteKind: TecnicaLimiteKind,
        val limiteRelativo: Int?,
        val preRequisitoExibicao: String
    )

    fun getRegraPerfilTecnica(definicao: TecnicaCatalogoItem, dataRepository: DataRepository): TecnicaRegraPerfil {
        val prerequisitoRaw = definicao.preRequisitoRaw
        val prerequisito = normalizarTexto(prerequisitoRaw)
        val predefMod = dataRepository.extrairModificadorPredefinido(definicao.preDefinidoRaw)
        val bonusExplicito = Regex("([+-]\\d+)").find(prerequisito)?.groupValues?.getOrNull(1)?.toIntOrNull()

        val kind = when {
            prerequisito.contains("metade da penalidade") -> TecnicaLimiteKind.METADE_PENALIDADE
            bonusExplicito != null && prerequisito.contains("nao pode exceder") -> TecnicaLimiteKind.EXPLICITO_RELATIVO
            prerequisito.contains("nao pode exceder") && prerequisito.contains("pre requisito aparar") -> TecnicaLimiteKind.PREDEFINIDO_APARAR
            prerequisito.contains("nao pode exceder") && prerequisito.contains("pre requisito bloquear") -> TecnicaLimiteKind.PREDEFINIDO_BLOQUEAR
            prerequisito.contains("nao pode exceder") -> TecnicaLimiteKind.PERICIA_BASE
            else -> TecnicaLimiteKind.NENHUM
        }

        val limiteRelativo = when (kind) {
            TecnicaLimiteKind.NENHUM -> null
            TecnicaLimiteKind.METADE_PENALIDADE -> {
                val penalidade = kotlin.math.abs(predefMod)
                if (penalidade > 0) penalidade / 2 else null
            }
            TecnicaLimiteKind.EXPLICITO_RELATIVO -> bonusExplicito
            TecnicaLimiteKind.PREDEFINIDO_APARAR,
            TecnicaLimiteKind.PREDEFINIDO_BLOQUEAR,
            TecnicaLimiteKind.PERICIA_BASE -> kotlin.math.abs(predefMod).takeIf { it > 0 } ?: 0
        }

        val preReqExibicao = when (kind) {
            TecnicaLimiteKind.PREDEFINIDO_APARAR -> {
                prerequisitoRaw.replace(
                    Regex("(?i)não\\s+pode\\s+exceder\\s+o\\s+pré-requisito\\s+Aparar"),
                    "não pode exceder o pré-definido Aparar"
                )
            }
            TecnicaLimiteKind.PREDEFINIDO_BLOQUEAR -> {
                prerequisitoRaw.replace(
                    Regex("(?i)não\\s+pode\\s+exceder\\s+o\\s+pré-requisito\\s+Bloquear"),
                    "não pode exceder o pré-definido Bloquear"
                )
            }
            else -> prerequisitoRaw
        }

        return TecnicaRegraPerfil(
            limiteKind = kind,
            limiteRelativo = limiteRelativo,
            preRequisitoExibicao = preReqExibicao
        )
    }

    fun periciaCorrespondeTermo(
        pericia: PericiaSelecionada, 
        termoRaw: String, 
        tecnicasNomesNormalizados: Set<String>
    ): Boolean? {
        val termo = normalizarTexto(termoRaw)
        if (termo.isBlank()) return null
        if (termo in setOf("st", "dx", "ht", "iq", "per", "von", "aparar", "bloquear")) return null

        return when {
            termo in tecnicasNomesNormalizados -> null
            termo.contains("consulte pag") || termo.contains("consulte pg") -> null
            termo.startsWith("tecnica ") -> null
            termo.contains("habitos detestaveis") -> null
            termo.contains("arma corpo a corpo apropriada") -> periciaEhCorpoACorpo(pericia)
            termo.contains("arma de corpo a corpo apropriada") -> periciaEhCorpoACorpo(pericia)
            termo.contains("pericia de arma corpo a corpo apropriada") -> periciaEhCorpoACorpo(pericia)
            termo.contains("pericia de arma de corpo a corpo apropriada") -> periciaEhCorpoACorpo(pericia)
            termo.contains("pericia de ataque corpo a corpo") -> periciaEhCorpoACorpo(pericia)
            termo.contains("ataque corpo a corpo") -> periciaEhCorpoACorpo(pericia)
            termo.contains("armas de fogo") && termo.contains("pistola") -> periciaEhArmasFogo(pericia) && periciaEhPistola(pericia)
            termo.contains("qualquer pericia de combate") -> periciaEhCombate(pericia)
            termo.contains("qualquer pericia") && termo.contains("tiro") -> periciaEhTiro(pericia)
            termo.contains("qualquer pericia de tiro adequada") -> periciaEhTiro(pericia)
            termo.contains("qualquer pericia de tiro") -> periciaEhTiro(pericia)
            termo.contains("arma de longo alcance") -> periciaEhTiro(pericia)
            termo.contains("qualquer pericia de sacar rapido") -> periciaEhSacarRapido(pericia)
            termo.contains("agarrar desarmado") -> periciaEhAgarrarDesarmado(pericia)
            termo.contains("combate desarmado") -> periciaEhDesarmado(pericia)
            termo.contains("qualquer pericia com arma de esgrima") -> periciaEhArmaEsgrima(pericia)
            termo.contains("arma de esgrima") -> periciaEhArmaEsgrima(pericia)
            termo.contains("arma de combate corpo a corpo") -> periciaEhCorpoACorpo(pericia)
            termo.contains("arma corpo a corpo") -> periciaEhCorpoACorpo(pericia)
            termo.contains("qualquer pericia com arma") -> periciaEhCorpoACorpo(pericia) || periciaEhTiro(pericia)
            termo.contains("defesa ativa") -> periciaEhDefesaAtiva(pericia)
            termo.contains("bloquear ou aparar") -> periciaEhDefesaAtiva(pericia)
            termo.contains("pericia pre requisito") -> true
            termo.contains("outra pericia") -> true
            termo.contains("apropriada") && termo.contains("arma") -> {
                when {
                    termo.contains("corpo a corpo") || termo.contains("ataque corpo a corpo") -> periciaEhCorpoACorpo(pericia)
                    termo.contains("esgrima") -> periciaEhArmaEsgrima(pericia)
                    termo.contains("tiro") || termo.contains("longo alcance") -> periciaEhTiro(pericia)
                    termo.contains("arma de fogo") || termo.contains("armas de fogo") -> periciaEhTiro(pericia)
                    else -> periciaEhCorpoACorpo(pericia)
                }
            }
            else -> periciaBateNomeLiteral(pericia, termo)
        }
    }

    private fun periciaBateNomeLiteral(pericia: PericiaSelecionada, termoNormalizado: String): Boolean? {
        val termosPericia = termosBuscaPericia(pericia)
        if (termosPericia.isEmpty()) return null
        if (termoNormalizado.length <= 1) return null
        return termosPericia.any { termoPericia ->
            termoPericia.contains(termoNormalizado) || termoNormalizado.contains(termoPericia)
        }
    }

    fun termosBuscaPericia(pericia: PericiaSelecionada): Set<String> {
        val base = mutableSetOf<String>()
        val nome = normalizarTexto(pericia.nome)
        val especializacao = normalizarTexto(pericia.especializacao)
        if (nome.isNotBlank()) base.add(nome)
        if (especializacao.isNotBlank()) base.add(especializacao)
        if (nome.isNotBlank() && especializacao.isNotBlank()) {
            base.add("$nome ($especializacao)")
            base.add("$nome $especializacao")
        }

        fun addAlias(valor: String, vararg aliases: String) {
            if (!base.contains(valor)) return
            aliases.map(::normalizarTexto).filter { it.isNotBlank() }.forEach { base.add(it) }
        }

        addAlias(normalizarTexto("carate"), "karate")
        addAlias(normalizarTexto("judo"), "judo")
        addAlias(normalizarTexto("luta greco romana"), "luta-greco romana", "wrestling")
        addAlias(normalizarTexto("armas de fogo"), "arma de fogo")
        addAlias(normalizarTexto("arcos"), "arco")
        addAlias(normalizarTexto("espadas curtas"), "espada curta")
        addAlias(normalizarTexto("espadas de lamina larga"), "espada de lamina larga", "espada larga")
        addAlias(normalizarTexto("maca/machado"), "maca", "machado")
        addAlias(normalizarTexto("maca/machado de duas maos"), "maca de duas maos", "machado de duas maos")
        return base
    }

    fun periciaEhDesarmado(pericia: PericiaSelecionada): Boolean {
        val termos = termosBuscaPericia(pericia)
        val chaves = listOf("briga", "boxe", "carate", "karate", "judo", "sumo", "luta greco romana")
        return termos.any { termo -> chaves.any { chave -> termo.contains(chave) } }
    }

    fun periciaEhAgarrarDesarmado(pericia: PericiaSelecionada): Boolean {
        val termos = termosBuscaPericia(pericia)
        val chaves = listOf("judo", "sumo", "luta greco romana", "briga")
        return termos.any { termo -> chaves.any { chave -> termo.contains(chave) } }
    }

    fun periciaEhCorpoACorpo(pericia: PericiaSelecionada): Boolean {
        val idNormalizado = pericia.definicaoId.trim().lowercase()
        if (PERICIAS_COMBATE.contains(idNormalizado) && idNormalizado != "escudo") return true
        val termos = termosBuscaPericia(pericia)
        val chaves = listOf("adaga", "espada", "maca", "machado", "chicote", "kusari", "lanca", "bastao", "capa", "jitte", "sai", "mangual", "arma de haste", "faca")
        return termos.any { termo -> chaves.any { chave -> termo.contains(chave) } } || periciaEhDesarmado(pericia)
    }

    fun periciaEhArmaEsgrima(pericia: PericiaSelecionada): Boolean {
        val termos = termosBuscaPericia(pericia)
        if (termos.any { it.contains("esgrima") }) return true
        val ids = setOf("adaga_de_esgrima", "rapieira", "sabre")
        return pericia.definicaoId.trim().lowercase() in ids
    }

    fun periciaEhTiro(pericia: PericiaSelecionada): Boolean {
        val termos = termosBuscaPericia(pericia)
        val chaves = listOf("armas de fogo", "armas de feixe", "arco", "besta", "funda", "arma de arremesso", "arremessador de lanca", "canhoneiro", "artilharia", "arma de longo alcance")
        return termos.any { termo -> chaves.any { chave -> termo.contains(chave) } }
    }

    fun periciaEhArmasFogo(pericia: PericiaSelecionada): Boolean {
        return termosBuscaPericia(pericia).any { it.contains("armas de fogo") || it.contains("arma de fogo") }
    }

    fun periciaEhPistola(pericia: PericiaSelecionada): Boolean {
        return termosBuscaPericia(pericia).any { it.contains("pistola") }
    }

    fun periciaEhCombate(pericia: PericiaSelecionada): Boolean {
        return periciaEhCorpoACorpo(pericia) || periciaEhTiro(pericia) || periciaEhDefesaAtiva(pericia)
    }

    fun periciaEhSacarRapido(pericia: PericiaSelecionada): Boolean {
        return termosBuscaPericia(pericia).any { it.contains("sacar rapido") }
    }

    fun periciaEhDefesaAtiva(pericia: PericiaSelecionada): Boolean {
        val termos = termosBuscaPericia(pericia)
        if (termos.any { it.contains("escudo") }) return true
        return periciaEhCorpoACorpo(pericia) || periciaEhDesarmado(pericia)
    }

    private fun normalizarTexto(texto: String): String =
        TextNormalizer.normalize(texto, TextNormalizer.SIMPLE)
}
