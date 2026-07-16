package com.gurps.ficha.domain.magic

/**
 * Lote MEC-5: leitura dos campos de TEMPO do catálogo (`duracao`, `tempoOperacao`), que são TEXTO
 * LIVRE ("1 min.", "Perm.", "Indef.#", "10 horas", "1 dia de marcha").
 *
 * Vive aqui, no domínio puro, para ser TESTÁVEL contra as 879 do catálogo — antes era privado no
 * controller, e por isso ninguém testou e o bug abaixo passou batido:
 *
 *  - procurava a palavra "permanente" INTEIRA, mas o catálogo abrevia **"Perm." em 154 mágicas** →
 *    todas viravam INSTANTÂNEA (mágica permanente nunca era rastreada);
 *  - só multiplicava por 60 quando achava "min", então **"1 hora" (82 mágicas) virava 1 SEGUNDO** —
 *    o buff expirava no turno seguinte ao lançamento;
 *  - "Indef." (31) e "1 dia" (13) também caíam errado.
 *
 * Total: 325 das 879 mágicas com a duração errada.
 */
object MagicTime {

    /** Multiplicador da unidade escrita no campo ("1 hora" → 3600). Sem unidade, o padrão é segundo. */
    fun unidadeEmSegundos(txt: String): Int {
        val t = txt.lowercase()
        return when {
            "seg" in t -> 1
            "min" in t -> 60
            "hora" in t || "hr" in t -> 3600
            "semana" in t -> 604800
            "dia" in t -> 86400
            else -> 1
        }
    }

    /**
     * (tipo, segundos) do campo `duracao`.
     *
     * "Indef." vira PERMANENTE de propósito: dura enquanto a condição valer (concentração, contato
     * visual) — não expira no relógio, sai por dissipação. É o comportamento mais próximo que o
     * motor tem; a condição em si (manter concentração) fica com o Narrador.
     */
    fun parseDuracao(txt: String?): Pair<TipoDuracao, Int> {
        val t = txt?.lowercase()?.trim() ?: return TipoDuracao.INSTANTANEA to 0
        return when {
            "perm" in t -> TipoDuracao.PERMANENTE to 0
            "indef" in t -> TipoDuracao.PERMANENTE to 0
            "instant" in t -> TipoDuracao.INSTANTANEA to 0
            else -> {
                val unidade = unidadeEmSegundos(t)
                // O catálogo às vezes escreve a unidade SEM número — "Hora" (8 mágicas), "Dia#" (2).
                // Sem isto elas não têm dígito, caem em INSTANTANEA e a mágica não dura nada.
                // "Varia"/"Especial" não têm unidade → seguem instantâneas (o Narrador decide).
                val n = Regex("""\d+""").find(t)?.value?.toIntOrNull()
                    ?: if (temUnidadeDeTempo(t)) 1 else 0
                if (n == 0) TipoDuracao.INSTANTANEA to 0
                else TipoDuracao.TEMPORARIA to n * unidade
            }
        }
    }

    /** true se o campo nomeia uma unidade de tempo (mesmo sem número: "Hora", "Dia#"). */
    private fun temUnidadeDeTempo(t: String): Boolean =
        "seg" in t || "min" in t || "hora" in t || "hr" in t || "dia" in t || "semana" in t

    /** Segundos do campo `tempoOperacao` (padrão 1s quando não informa — Magia p.9). */
    fun parseTempoSeg(txt: String?): Int {
        val t = txt?.lowercase().orEmpty()
        val n = Regex("""\d+""").find(t)?.value?.toIntOrNull() ?: 1
        return (n * unidadeEmSegundos(t)).coerceAtLeast(1)
    }
}
