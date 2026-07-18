package com.gurps.ficha.domain.saga

import com.google.gson.Gson

/**
 * Lote 356 (Saga): "session zero" da campanha — define gênero, tom, dificuldade e o que
 * pode/não pode ser usado. Serializada como JSON em CampanhaEntity.configJson e injetada
 * como bloco no prompt do Narrador (e, no futuro, trava real de tools/catálogo).
 */
data class CampanhaConfig(
    val genero: String = "",          // preset (ex.: "Faroeste") ou vazio
    val conceito: String = "",        // texto livre que detalha a campanha
    val tom: String = "Heroico",      // Heroico | Realista | Sombrio | Pulp | Cômico
    val dificuldade: String = "Normal", // Fácil | Normal | Difícil | Mortal
    val magiaPermitida: Boolean = true,
    val nivelTecnologico: Int = 3,    // NT GURPS 0-12
    val livros: List<String> = listOf(MODULO_BASICO),
    // Lote HEX-2 (Fase 2a do PILAR): combate tático em hexágonos (default OFF — modo faixas continua padrão).
    // Aditivo/anulável em fichas antigas (Gson usa o default false); ver docs/planos/PLANO_Combate_Tatico_Hex_3D.md.
    val modoTaticoHex: Boolean = false,
    // DEPRECATED (Lote TOK-1): o render 3D (HEX-7..9) virou legado após teste no aparelho — o modo
    // tático agora é o canvas 2D com tokens de imagem (docs/planos/PLANO_Tokens_VTT_2D.md). O campo permanece só
    // por compat Gson com fichas que o ligaram; qualquer valor cai no mesmo canvas 2D novo.
    val modoTaticoHex3D: Boolean = false
) {
    fun toJson(): String = Gson().toJson(this)

    /** Bloco categorial injetado no prompt do Narrador. Vazio se nada relevante definido. */
    fun paraPromptBloco(): String {
        val sb = StringBuilder("=== CONFIGURAÇÃO DA CAMPANHA (definida pelo jogador — RESPEITE) ===\n")
        if (genero.isNotBlank()) sb.append("Gênero: $genero.\n")
        if (conceito.isNotBlank()) sb.append("Conceito: $conceito.\n")
        sb.append("Tom: $tom — ${descricaoTom(tom)}\n")
        sb.append("Dificuldade: $dificuldade — ${descricaoDificuldade(dificuldade)}\n")
        sb.append(
            if (magiaPermitida) "Magia: permitida neste mundo.\n"
            else "Magia: NÃO existe neste mundo — não ofereça, não use e não peça testes mágicos.\n"
        )
        sb.append("Nível tecnológico: NT$nivelTecnologico (${descricaoNt(nivelTecnologico)}).\n")
        if (livros.isNotEmpty()) {
            sb.append("Regras/livros liberados: ${livros.joinToString(", ")}. ")
            sb.append("Não use conteúdo de suplementos fora desta lista.\n")
        }
        return sb.toString().trimEnd()
    }

    private fun descricaoTom(t: String): String = when (t) {
        "Heroico" -> "protagonismo épico, o herói brilha; perigos existem mas há esperança."
        "Realista" -> "consequências críveis, recursos limitados, pouco maniqueísmo."
        "Sombrio" -> "atmosfera pesada, escolhas difíceis, vitórias custam caro."
        "Pulp" -> "ação exagerada e ritmo veloz, reviravoltas e bravata."
        "Cômico" -> "leve e bem-humorado, situações absurdas sem perder a aventura."
        else -> "tom equilibrado."
    }

    private fun descricaoDificuldade(d: String): String = when (d) {
        "Fácil" -> "NPCs hesitantes, modificadores generosos, erros raramente são fatais."
        "Normal" -> "desafio padrão de GURPS, sem afrouxar nem endurecer."
        "Difícil" -> "NPCs competentes, modificadores realistas, consequências pesam."
        "Mortal" -> "mundo implacável; um erro pode matar, sem perdão narrativo."
        else -> "desafio padrão."
    }

    private fun descricaoNt(nt: Int): String = when {
        nt <= 1 -> "primitivo"
        nt == 2 -> "antiguidade/bronze e ferro"
        nt == 3 -> "medieval"
        nt == 4 -> "renascença/pólvora inicial"
        nt == 5 -> "revolução industrial, pólvora"
        nt == 6 -> "mecânica/início do elétrico (faroeste tardio, mundo-guerras)"
        nt == 7 -> "moderno inicial"
        nt == 8 -> "atual/digital"
        nt == 9 -> "futuro próximo"
        nt in 10..11 -> "ficção científica avançada"
        else -> "ultratecnologia"
    }

    companion object {
        const val MODULO_BASICO = "Módulo Básico"
        val LIVROS_DISPONIVEIS = listOf(MODULO_BASICO, "Artes Marciais", "Magia", "Gun Fu", "Pyramid Aquático")
        val GENEROS = listOf("Fantasia Medieval", "Moderno", "Faroeste", "Cyberpunk", "Horror", "Ficção Científica", "Pós-apocalíptico")
        val TONS = listOf("Heroico", "Realista", "Sombrio", "Pulp", "Cômico")
        val DIFICULDADES = listOf("Fácil", "Normal", "Difícil", "Mortal")

        fun fromJson(json: String?): CampanhaConfig = try {
            if (json.isNullOrBlank() || json == "{}") CampanhaConfig()
            else Gson().fromJson(json, CampanhaConfig::class.java) ?: CampanhaConfig()
        } catch (e: Exception) {
            CampanhaConfig()
        }
    }
}
