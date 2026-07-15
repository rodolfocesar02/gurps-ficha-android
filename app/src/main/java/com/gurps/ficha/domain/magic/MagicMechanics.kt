package com.gurps.ficha.domain.magic

/**
 * Lote AR-1 (mecânica estruturada das magias): a `descricao` de cada feitiço no catálogo é FIEL ao
 * livro, mas é PROSA — o motor não a executa. Este é o campo `mecanica` legível pela máquina, ao lado
 * da descrição (que fica intocada): a versão CURADA das regras que o motor aplica.
 *
 * O `efeito` é um conjunto FECHADO; o que não couber vira "narrado" (honesto — Criar Ar, Convocar
 * Elemental). Escola por escola, começando por Ar.
 */
data class MagiaMecanica(
    /** "dano" | "condicao" | "buff" | "ambiente" | "controle" | "informacao" | "narrado". */
    val efeito: String = "narrado",

    // ── efeito "dano" ──
    /** Dado por unidade de energia (ex.: "1d-1" no Relâmpago, "1d+1" no Toque Chocante). */
    val danoPorEnergia: String? = null,
    /** Quantos pontos de energia compram 1 "unidade" de dado (1 no Relâmpago, 2 na Concussão). */
    val energiaPorDado: Int = 1,
    /** "quei" (queimadura) | "cont" | "projecao" | "corte"… (default contusão no motor). */
    val tipoDano: String? = null,
    /** null = RD normal; "ignora" = armadura não protege (Toque Chocante); "metal_rd_1" = metal vira RD 1 (aprox.: mantém RD). */
    val armadura: String? = null,
    /** Como acerta: "projetil" | "toque" | "feixe" (DX−4) | "area" | "auto". Complementa a `classe`. */
    val entrega: String? = null,

    // ── condição embutida (rider no dano ou standalone) ──
    /** Condição imposta ("atordoado", "cego"…). */
    val condicao: String? = null,
    /** Teste para resistir à condição: "HT" | "HT-3" | "HT_por_pv" (Relâmpago: −1 por 2 PV). */
    val condicaoResistencia: String? = null,
    /** Raio (m) em que a condição se espalha (Concussão = 10). 0 = só o alvo. */
    val condicaoRaioM: Int = 0,

    // ── efeito "buff" (rastreado como magia ativa; bônus numérico simples quando houver) ──
    val buffRotulo: String? = null,
    /** Bônus de dano numa arma (Arma de Relâmpago = +2). */
    val buffDanoArma: Int = 0,

    // ── notas para o Narrador (ambiente/controle/utilidade: o motor tagueia, o Mestre descreve) ──
    val notas: String? = null,
)

object MagicMechanics {

    /** true se a mágica tem dano estruturado que o motor aplica automaticamente. */
    fun temDanoEstruturado(m: MagiaMecanica?): Boolean = m?.efeito == "dano" && m.danoPorEnergia != null

    /**
     * Expande o dano por energia para a expressão total, escalando pela energia investida.
     * Ex.: "1d-1" por 1 energia, energia 3 → "3d-3"; "1d" por 2 energia, energia 4 → "2d".
     * Regras: dados = (energia / energiaPorDado) coerçado a ≥1; o modificador escala com a contagem.
     */
    fun expandirDano(danoPorEnergia: String, energia: Int, energiaPorDado: Int): String {
        val n = (energia / energiaPorDado.coerceAtLeast(1)).coerceAtLeast(1)
        val m = Regex("""(\d*)d([+-]\d+)?""").find(danoPorEnergia.replace(" ", "")) ?: return "${n}d"
        val dadosBase = m.groupValues[1].toIntOrNull() ?: 1
        val modBase = m.groupValues[2].toIntOrNull() ?: 0
        val dados = dadosBase * n
        val mod = modBase * n
        return "${dados}d" + when {
            mod > 0 -> "+$mod"
            mod < 0 -> "$mod"
            else -> ""
        }
    }

    /** Penalidade da condição imposta pela mágica, dado o dano sofrido (ex.: Relâmpago −1 por 2 PV). */
    fun penalidadeCondicaoPorPv(resistencia: String?, pvSofridos: Int): Int = when {
        resistencia == "HT_por_pv" -> -(pvSofridos / 2)
        resistencia != null && resistencia.startsWith("HT-") -> resistencia.removePrefix("HT-").toIntOrNull()?.let { -it } ?: 0
        else -> 0
    }
}
