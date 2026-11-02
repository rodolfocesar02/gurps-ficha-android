package com.gurps.ficha.domain.magic

/**
 * P-9 — a faixa **Operar / Dura / Manter** do diálogo de energia da aba Rolagem, igual à do cartão da Mesa.
 *
 * O que o livro diz da magia (tempo de concentração, duração, custo de manter) a **Mesa já lê** do cabeçalho do catálogo e dos números conferidos.
 * Em vez de portar essa leitura (duas contas que um dia divergem), o script `server/scripts/gerar-faixa-do-livro-no-catalogo-do-app.js` da Mesa
 * grava o resultado em cada magia da `magias2versao.json` (`faixaDoLivro`), e este arquivo só **desenha** — e refaz o único pedaço que depende do
 * que o jogador escolhe: o "Manter", que muda com o custo base e com o desconto do NH.
 *
 * 🔴 `FaixaDoLivroTest` confere esta conta contra os casos que a Mesa gera (`faixa_do_livro_casos_gerados.json`: as 879 magias e as escolhas de custo).
 * Mudou o catálogo (`duracao`, `tempoOperacao`, `energia`, números conferidos)? Rode o gerador da Mesa de novo.
 */

/** Uma coluna pronta para a tela: [rotulo] pequeno, [valor] grande e a [nota] embaixo. */
data class FaixaColuna(val rotulo: String, val valor: String, val nota: String)

/** O que o catálogo guarda de "Operar" e de "Dura": o texto pronto. */
data class FaixaTexto(val valor: String = "", val nota: String = "")

/**
 * O "Manter" do livro. [tipo]: `numero` ([valor] por [periodo]), `metade` ou `igual` (do custo de operar), `nao` (a magia não se mantém) ou
 * `naoDiz` (o livro não diz; [nota] explica: "o livro não diz" ou "não se aplica"). [periodo]: "a cada 1 minuto", quando a duração é temporária.
 */
data class FaixaManter(
    val tipo: String = "naoDiz",
    val valor: Int? = null,
    val periodo: String? = null,
    val nota: String? = null
)

/**
 * O que a conta do custo por condição diz sobre manter (peso, material, alcance...): [tipo] `nenhum` = a magia não se mantém; qualquer outro
 * = o número dela ([manter]). `null` = a magia não tem conta por condição.
 */
data class ManterDaConta(val tipo: String?, val manter: Int?)

data class FaixaDoLivro(
    val operar: FaixaTexto = FaixaTexto(),
    val dura: FaixaTexto = FaixaTexto(),
    val manter: FaixaManter = FaixaManter()
) {
    /**
     * As três colunas.
     *
     * @param base o custo base que o jogador escolheu agora (antes do desconto do NH); `null` enquanto não escolheu
     * @param reducao o desconto do NH sobre esse custo (0 onde o NH não reduz)
     * @param daConta o "manter" que a conta por condição deu, ou `null`
     */
    fun colunas(base: Int? = null, reducao: Int = 0, daConta: ManterDaConta? = null): List<FaixaColuna> = listOf(
        FaixaColuna("Operar", operar.valor, operar.nota),
        FaixaColuna("Dura", dura.valor, dura.nota),
        colunaManter(base, reducao, daConta)
    )

    private fun colunaManter(base: Int?, reducao: Int, daConta: ManterDaConta?): FaixaColuna {
        // Onde o custo depende da condição, a CONTA manda: `nenhum` não se mantém, senão é o número dela.
        val m = when {
            daConta?.tipo == null -> manter
            daConta.tipo == "nenhum" -> FaixaManter(tipo = "nao")
            else -> FaixaManter(tipo = "numero", valor = daConta.manter, periodo = manter.periodo)
        }
        if (m.tipo == "naoDiz") return FaixaColuna("Manter", "—", m.nota ?: "o livro não diz")
        if (m.tipo == "nao") return FaixaColuna("Manter", "—", "não se mantém")
        val custoDeManter: Int? = when {
            m.tipo == "numero" -> m.valor
            base != null && m.tipo == "igual" -> base.coerceAtLeast(0)
            base != null && m.tipo == "metade" -> (base.coerceAtLeast(0) + 1) / 2
            else -> null
        }
        if (custoDeManter == null) {
            return when (m.tipo) {
                "metade" -> FaixaColuna("Manter", "Metade", "do custo de operar")
                "igual" -> FaixaColuna("Manter", "Igual", "ao custo de operar")
                else -> FaixaColuna("Manter", "—", "o livro não diz")
            }
        }
        val ajustado = (custoDeManter - reducao).coerceAtLeast(0)
        val mudou = ajustado != custoDeManter
        return FaixaColuna(
            "Manter",
            if (mudou) "$custoDeManter → $ajustado" else "$custoDeManter",
            (m.periodo ?: "por período") + if (mudou) ", com o seu NH" else ""
        )
    }

    companion object {
        /** A frase para quem ouve (TalkBack): "Operar 3 s, tempo de concentração. Dura 1 minuto, depois: manter. Manter 3 passa a 1, ...". */
        fun fala(colunas: List<FaixaColuna>): String = colunas.joinToString(". ", postfix = ".") { c ->
            c.rotulo + " " + c.valor.replace(" → ", " passa a ") + (if (c.nota.isNotBlank()) ", ${c.nota}" else "")
        }
    }
}
