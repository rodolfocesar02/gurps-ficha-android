package com.gurps.ficha.domain.rules

/**
 * **A cor do texto sobre a cor da nota** — Lote NOTA-3.
 *
 * ## 🔴 O defeito
 *
 * A nota tem fundo colorido (sete tons **100** do Material — todos claros), mas
 * o texto era pintado com `MaterialTheme.colorScheme.onSurface`, que vem do
 * **tema**. No tema escuro isso é quase branco: branco sobre amarelo-claro.
 *
 * E o app segue o tema do sistema (`isSystemInDarkTheme()`, com `dynamicColor`),
 * então metade dos aparelhos da mesa mostrava a nota ilegível — enquanto no meu
 * e no do dono do app, no tema claro, estava perfeita. **Defeito que só aparece
 * no aparelho de outra pessoa.**
 *
 * ## A regra
 *
 * A cor do texto sai do **fundo**, não do tema. Quem manda é a luminância
 * relativa da cor (a fórmula da WCAG, a mesma que os verificadores de contraste
 * usam): fundo claro pede texto escuro, fundo escuro pede texto claro.
 *
 * ⚠️ **Por que não simplesmente "sempre escuro".** Hoje as sete cores são
 * claras, e "sempre escuro" acertaria em todas. Mas a lista de cores é dado, e
 * dado muda: no dia em que alguém acrescentar um tom escuro, o texto some — e
 * some **em silêncio**, que é o pior jeito. A conta custa dez linhas e não tem
 * esse dia ruim.
 *
 * ⚠️ **Só vale quando a nota TEM cor.** Sem cor, o fundo é `surfaceVariant`, que
 * é do tema — e aí a cor do tema é a certa. Sobrepor ali seria trocar um
 * problema de contraste por outro.
 */
object CorDaNota {

    /** Quase preto, e não preto puro: é o `onSurface` claro do Material. */
    const val TEXTO_ESCURO = "#1C1B1F"

    const val TEXTO_CLARO = "#FFFFFF"

    /**
     * O ponto de virada da WCAG: acima dele, o preto contrasta mais; abaixo, o
     * branco. Não é 0,5 — o olho não é linear, e usar 0,5 erra a faixa dos
     * médios, que é justamente onde a escolha importa.
     */
    private const val VIRADA = 0.179

    /**
     * A cor do texto que se lê sobre este fundo.
     *
     * @param fundoHex `#RRGGBB`, `#AARRGGBB`, ou com o `#` de fora
     * @return [TEXTO_ESCURO] ou [TEXTO_CLARO]
     *
     * ⚠️ Hex ilegível cai em [TEXTO_ESCURO]. É o palpite certo para as cores que
     * existem hoje (todas claras) — e um texto escuro que some num fundo escuro
     * é um defeito **visível**, ao contrário de um `null` que ninguém trata.
     */
    fun textoSobre(fundoHex: String?): String {
        val l = luminancia(fundoHex) ?: return TEXTO_ESCURO
        return if (l > VIRADA) TEXTO_ESCURO else TEXTO_CLARO
    }

    /**
     * Luminância relativa (WCAG 2.x), de 0 (preto) a 1 (branco).
     *
     * @return `null` quando o texto não é uma cor que dê para ler
     */
    fun luminancia(fundoHex: String?): Double? {
        val limpo = fundoHex?.trim()?.removePrefix("#") ?: return null
        // 8 dígitos é ARGB (o formato do Android): o alfa não entra na conta.
        val rgb = when (limpo.length) {
            6 -> limpo
            8 -> limpo.substring(2)
            else -> return null
        }
        val valor = rgb.toLongOrNull(16) ?: return null

        val r = canal(((valor shr 16) and 0xFF).toInt())
        val g = canal(((valor shr 8) and 0xFF).toInt())
        val b = canal((valor and 0xFF).toInt())
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /** Desfaz a curva do sRGB: o valor guardado não é a luz que sai da tela. */
    private fun canal(bruto: Int): Double {
        val s = bruto / 255.0
        return if (s <= 0.03928) s / 12.92 else Math.pow((s + 0.055) / 1.055, 2.4)
    }

    /**
     * A razão de contraste entre duas cores (WCAG): de 1 (iguais) a 21
     * (preto e branco). Texto corrido precisa de **4,5**.
     *
     * Existe para o teste poder cobrar o número em vez de acreditar na conta.
     */
    fun contraste(umHex: String?, outroHex: String?): Double? {
        val a = luminancia(umHex) ?: return null
        val b = luminancia(outroHex) ?: return null
        val claro = maxOf(a, b)
        val escuro = minOf(a, b)
        return (claro + 0.05) / (escuro + 0.05)
    }
}
