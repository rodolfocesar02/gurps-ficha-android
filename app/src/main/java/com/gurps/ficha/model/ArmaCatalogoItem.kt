package com.gurps.ficha.model

/**
 * Um **modo de ataque** da arma (Lote ARMA-1).
 *
 * ## O furo que isto fecha
 *
 * A tabela do livro põe dois (às vezes três) ataques na mesma linha, separados
 * por barra: a Katana é `GeB+1 corte/GdP+1 perf`, a Alabarda tem três. O loader
 * antigo lia `modos.first()` e parava — **28 das 60 armas corpo a corpo perdiam
 * um ataque inteiro** que o personagem tem no livro.
 *
 * ⚠️ [custo] e [pesoKg] vêm **nulos** do segundo modo em diante, e isso está
 * certo: é a mesma arma física. O livro escreve `$650 / —` justamente para dizer
 * "não pague duas vezes". Quem exibe deve ler nulo como *"mesma arma"*, nunca
 * como *"de graça"*.
 */
data class ModoDeArma(
    /** 1, 2, 3… na ordem em que o livro escreve. */
    val ordem: Int,
    /** `"GeB+1 corte"`, `"GdP+1 perf"`. */
    val danoRaw: String,
    /** Corpo a corpo: alcance deste modo (`"1"`, `"1, 2"`, `"1–3*"`). */
    val alcanceCorpoACorpo: String? = null,
    /** Corpo a corpo: coluna Aparar deste modo (`"0"`, `"0D"`, `"F"`, `"Não"`). */
    val aparar: String? = null,
    /** Nulo do 2º modo em diante — é a mesma arma, não um item novo. */
    val custo: Float? = null,
    /** Nulo do 2º modo em diante — mesma razão do [custo]. */
    val pesoKg: Float? = null
)

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
    val recuo: Int? = null,
    // ── Lote ARMA-1: o que o catálogo sempre teve e o app nunca lia ──
    /** NT da arma. MB p.29. */
    val nt: Int? = null,
    /** Classe de Legalidade (LC no original). Só as armas de fogo trazem. MB p.507. */
    val cl: Int? = null,
    /**
     * Peso da **munição**, separado do peso da arma.
     *
     * O livro escreve `2,3/0,5` — arma/munição. O app lia só o primeiro número e
     * o segundo sumia, então uma ADP Gauss com 80 tiros parecia pesar 2,3 kg.
     */
    val municaoKg: Float? = null,
    /**
     * 🔴 **A mira acoplada** (Lote ARMA-1).
     *
     * A Precisão vem escrita `"6+1"` na tabela: 6 da arma **+1 do acessório
     * embutido** (luneta, red dot). O loader lia só o campo `valor` (6) e jogava
     * o `+1` fora — em **12 armas de fogo**, e o Rifle de Atirador .338 perdia
     * **3 pontos** inteiros.
     *
     * Não é enfeite de vitrine: essa Precisão alimenta o Apontar
     * (`ApontarRules`), então o atirador vinha apontando com menos do que o livro
     * dá — e o teto do dobro da Prec (MB p.373) saía errado por tabela.
     *
     * Fica **separado** de [precisao] de propósito: o bônus do acessório só vale
     * se o jogador estiver usando a mira, e essa é uma escolha dele.
     */
    val precisaoAcessorio: Int? = null,
    /** O texto literal da coluna Custo (`"$3.600"`, `"—"`). */
    val custoRaw: String? = null,
    /** O texto literal da coluna Peso (`"2,3/0,5"`). */
    val pesoRaw: String? = null,
    /** O texto literal da coluna Alcance (`"700/2.900"`, `"×15/×20"`). */
    val alcanceRaw: String? = null,
    /** Os símbolos da coluna ST: `"dagger"` (†) e `"double_dagger"` (‡). */
    val stFlags: List<String> = emptyList(),
    /**
     * O texto literal da coluna ST (`"9"`, `"13‡ / 12"`).
     *
     * ⚠️ Existe porque [stMinimo] vem **nulo** em duas armas — Glaive e Alabarda
     * trazem `"13‡ / 12"`, uma ST por modo de ataque, e o normalizador não teve
     * como escolher uma. Sem o cru, a tela mostrava um travessão e o jogador
     * ficava sem saber a ST de uma alabarda.
     */
    val stRaw: String? = null,
    /**
     * Os modos de ataque, na ordem do livro. **Nunca vazio** para arma do
     * catálogo: uma arma de um modo só tem lista de um elemento.
     */
    val modos: List<ModoDeArma> = emptyList()
) {
    /** A Precisão que o jogador realmente tem **usando** a mira acoplada. */
    val precisaoComAcessorio: Int?
        get() {
            val base = precisao ?: return null
            return base + (precisaoAcessorio ?: 0)
        }

    /** Tem mira acoplada de fábrica? Só então a caixinha faz sentido na tela. */
    val temMiraAcoplada: Boolean
        get() = (precisaoAcessorio ?: 0) > 0

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
