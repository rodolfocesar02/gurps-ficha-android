package com.gurps.ficha.model

data class ArmaCatalogoItem(
    val id: String,
    val nome: String,
    val tipoCombate: String, // "corpo_a_corpo" | "distancia" | "armas_de_fogo"
    val categoria: String,
    val grupo: String,
    val stMinimo: Int?,
    val danoRaw: String,
    val custoBase: Float?,
    val pesoBaseKg: Float?,
    val aparar: String? = null,
    val observacoes: String = "",
    // ── Stats de combate (Lote 371) — lidos dos JSONs normalizados; null = não se aplica/desconhecido ──
    /** Corpo-a-corpo: alcance da arma ("C", "1", "1,2"). MB p.270. */
    val alcanceCorpoACorpo: String? = null,
    /** Arma de duas mãos (flags † / ‡ na coluna ST). MB p.271. */
    val duasMaos: Boolean = false,
    /** À distância: Precisão (Acc) — bônus ao Apontar. MB p.270. */
    val precisao: Int? = null,
    /** À distância: 1/2D em metros (dano cai pela metade além disso). null se usa múltiplo de ST. */
    val meioDanoMetros: Int? = null,
    /** À distância: alcance Máximo em metros. null se usa múltiplo de ST. */
    val maximoMetros: Int? = null,
    /** À distância (arcos/arremesso): alcance como múltiplo de ST, ex.: "×10/×15" ou "×4". */
    val alcanceMultStRaw: String? = null,
    /** À distância: Cadência de Tiro (CdT/RoF). */
    val cadenciaTiro: Int? = null,
    /** À distância: capacidade de tiros + recarga, cru ("1(20)", "6(3)", "A"). */
    val tirosRaw: String? = null,
    /** À distância: Magnitude (Bulk) — penalidade no Avançar e Atacar/Ocultar. MB p.271. */
    val magnitude: Int? = null,
    /** À distância: Recuo (Rcl). */
    val recuo: Int? = null
) {
    companion object {
        /**
         * Lote 380: a arma à distância/de fogo ocupa as DUAS mãos? Determinado pelo GRUPO do catálogo
         * (dado estruturado, não pelo nome). Armas de fogo: só pistola é de uma mão; o resto (rifle,
         * mosquete, espingarda, metralhadora…) é arma longa. À distância: arco/besta/zarabatana = 2 mãos;
         * arremesso/funda = 1 mão. (Corpo-a-corpo usa as flags † / ‡ da coluna ST, tratadas no loader.)
         * Limitação conhecida: na ficha o grupo vem sem o parêntese, então "Feixe (Pistola)" (FC) não é
         * distinguido — fora do escopo de ambientação atual.
         */
        fun duasMaosPorGrupo(tipoCombate: String, grupo: String): Boolean {
            val g = grupo.lowercase()
            return when {
                tipoCombate == "armas_de_fogo" -> !g.contains("pistola")
                else -> g.contains("arco") || g.contains("besta") || g.contains("zarabatana")
            }
        }
    }
}
