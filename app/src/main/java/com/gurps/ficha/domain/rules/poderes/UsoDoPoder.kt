package com.gurps.ficha.domain.rules.poderes

/**
 * **Usar um poder** — GURPS Poderes, p.156 e p.158. Lote POD-11.
 *
 * ## 🔴 O plano descrevia este lote errado
 *
 * O `PLANO_PODERES.md` dizia: *"botão de ativar o poder na Rolagem — faça teste
 * de HT se a fonte for Biológico, Elemental, Natureza ou Super, ou de Vontade
 * se for Chi, Divino, Espiritual, Mágico, Moral ou Psíquico (p.156)"*.
 *
 * Relendo a página inteira, **essa tabela não é o teste de ativação**. Ela está
 * dentro de *Poderes Incapacitados* e vale só para quando uma **falha crítica**
 * em esforço adicional ou proeza ameaça derrubar o poder:
 *
 * > *"Falhas críticas ao usar um esforço adicional (…) ou ao realizar uma proeza
 * > (…) com um poder pode prejudicar o poder. **Faça um teste de HT** se o
 * > modificador de poder for Biológico, Elemental, Natureza ou Super, **ou de
 * > Vontade** se for Chi, Divino, Espiritual, Mágico, Moral, ou Psíquico."*
 *
 * E a frase seguinte explica o buraco que eu tinha notado na lista: *"Os poderes
 * **cósmicos são imunes** a incapacitações."*
 *
 * ⚠️ **Não existe "atributo de ativação do poder".** O que se rola depende da
 * **habilidade** (a vantagem), não da fonte. O que a fonte decide é o teste de
 * incapacitação — e é isso que [Incapacitacao] modela.
 */
object UsoDoPoder {

    // ── O Talento (p.158) ──────────────────────────────────────────────

    /**
     * > *"O Talento com um poder atua como um bônus (…) em todos os testes contra
     * > atributos, características secundárias ou perícias **para usar as
     * > habilidades do poder**. Isso inclui testes para ativar, atacar, controlar
     * > ou defender com essas habilidades."* (p.158)
     *
     * A lista de exclusões é tão importante quanto a regra: sem ela o bônus
     * vazaria para o dano e para o teste de resistência do alvo, que é onde ele
     * mais desequilibraria.
     */
    enum class TipoDeTeste(val rotulo: String, val recebeTalento: Boolean) {
        ATIVAR("ativar a habilidade", true),
        ATACAR("atacar com a habilidade", true),
        CONTROLAR("controlar a habilidade", true),
        DEFENDER("defender com a habilidade", true),

        // 🔴 Os quatro que NÃO recebem. O livro é explícito.
        DANO("dano", false),
        REACAO("reação", false),
        EXIGIDO_POR_LIMITACAO("teste exigido por uma limitação", false),
        FEITO_PELO_ALVO("teste feito pelo alvo", false)
    }

    /** O bônus que o Talento dá neste tipo de teste — zero quando não se aplica. */
    fun bonusDoTalento(nivelDoTalento: Int, tipo: TipoDeTeste): Int =
        if (tipo.recebeTalento) nivelDoTalento.coerceAtLeast(0) else 0

    /**
     * ⚠️ Exceções que o livro abre **dentro** da exclusão de reação: Aliados com
     * Invocável e Patrono com Altamente Acessível. São nomeadas uma a uma, e por
     * isso ficam aqui em vez de virar "às vezes reação conta".
     */
    fun reacaoRecebeTalento(habilidade: String): Boolean {
        val h = habilidade.lowercase()
        return (h.contains("aliado") && h.contains("invocável")) ||
            (h.contains("patrono") && h.contains("altamente acessível"))
    }

    // ── A incapacitação do poder (p.156) ───────────────────────────────

    object Incapacitacao {

        enum class Atributo { HT, VONTADE, IMUNE, A_CRITERIO_DO_MESTRE }

        private val POR_HT = setOf("Biológico", "Elemental", "Natureza", "Super")
        private val POR_VONTADE = setOf("Chi", "Divino", "Espiritual", "Mágico", "Moral", "Psíquico")

        /**
         * Contra o que se rola para o poder **não** ficar incapacitado.
         *
         * ⚠️ Só entra em cena depois de uma **falha crítica** em esforço
         * adicional (p.160) ou proeza (p.170). Não é um teste de rotina.
         */
        fun atributo(fonte: String?): Atributo {
            val f = RegrasDePoder.normalizarFonte(fonte) ?: return Atributo.A_CRITERIO_DO_MESTRE
            return when {
                // "Os poderes cósmicos são imunes a incapacitações." (p.156)
                f == "Cósmico" -> Atributo.IMUNE
                f in POR_HT -> Atributo.HT
                f in POR_VONTADE -> Atributo.VONTADE
                // "O Mestre decide o teste para outros poderes."
                else -> Atributo.A_CRITERIO_DO_MESTRE
            }
        }

        fun explicar(fonte: String?): String = when (atributo(fonte)) {
            Atributo.HT -> "Poder de fonte $fonte: teste de HT para não ficar incapacitado (p.156)."
            Atributo.VONTADE -> "Poder de fonte $fonte: teste de Vontade para não ficar incapacitado (p.156)."
            Atributo.IMUNE -> "Poderes cósmicos são imunes a incapacitações (p.156)."
            Atributo.A_CRITERIO_DO_MESTRE ->
                "O livro não define o teste para esta fonte — o Mestre decide (p.156)."
        }

        /**
         * ⚠️ A incapacitação atinge o **poder inteiro**, não a habilidade que
         * falhou: *"Uma falha ou uma falha crítica prejudica todas as habilidades
         * do poder"*. E, em habilidades alternativas (p.11), uma incapacitada
         * derruba o conjunto.
         */
        const val ATINGE_O_PODER_INTEIRO = true
    }
}
