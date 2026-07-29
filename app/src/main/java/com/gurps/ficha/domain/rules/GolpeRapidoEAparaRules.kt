package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * **Golpe Rápido** (MB p.371) e **apara repetida no mesmo turno** (MB p.377).
 *
 * Duas contas que o jogador faz de cabeça no meio do combate, e as duas mudam
 * quando o personagem tem Treinado por um Mestre ou Mestre de Armas.
 *
 * ## Golpe Rápido
 *
 * > Ele desfere dois ataques, os dois com uma penalidade de **-6**. (p.371)
 *
 * Treinado por um Mestre e Mestre de Armas cortam isso pela metade: **-3**.
 *
 * ## Apara repetida
 *
 * O livro é minucioso aqui, e a regra tem **quatro** resultados, não dois:
 *
 * > Depois de tentar aparar com uma arma em particular ou com as mãos nuas, o
 * > personagem sofre uma penalidade **cumulativa de -4** para aparar novamente
 * > com a mesma arma ou mão. Diminua essa penalidade para **-2** por tentativa se
 * > o personagem estiver usando uma **arma de esgrima** ou se ele possuir a
 * > vantagem **Treinado por Um Mestre ou Mestre de Armas** — ou para **-1** por
 * > tentativa se ele cumprir as **duas** condições. (p.377)
 *
 * | | Arma comum | Arma de esgrima |
 * |---|---|---|
 * | **Sem as vantagens** | -4 por apara | -2 por apara |
 * | **Com uma delas** | -2 por apara | **-1 por apara** |
 *
 * ⚠️ **Cumulativa e só no turno.** A segunda apara é -4, a terceira -8, a quarta
 * -12 — e tudo zera no turno seguinte. O livro: *"essa penalidade só se aplica às
 * tentativas de aparar no mesmo turno; ela não se estende a outros turnos"*.
 *
 * ## O limite honesto desta regra
 *
 * ⚠️ **Mestre de Armas vale só para a classe de armas dele** (espadas, arcos…), e
 * a vantagem não guarda na ficha qual classe é. E o livro fecha o parágrafo com
 * *"nenhum desses benefícios se aplica ao uso de valores predefinidos"* — precisa
 * ter a perícia de verdade.
 *
 * O app não tem como conferir nenhuma das duas coisas, então ele **oferece** o
 * número reduzido e escreve a condição no rótulo. Quem decide é o jogador, na
 * mesa — mesma filosofia das caixinhas condicionais. Fingir que sabe seria pior
 * que perguntar.
 *
 * Kotlin puro e testável.
 */
object GolpeRapidoEAparaRules {

    const val ID_TREINADO_POR_UM_MESTRE = "treinado_por_um_mestre"
    const val ID_MESTRE_DE_ARMAS = "mestre_de_armas"

    /** A penalidade do Golpe Rápido sem nenhuma vantagem (MB p.371). */
    const val GOLPE_RAPIDO = -6

    /** A penalidade por apara extra, sem vantagem e com arma comum (MB p.377). */
    const val APARA_EXTRA = -4

    /**
     * Se a ficha tem alguma das duas vantagens que cortam a penalidade.
     *
     * As duas dão o **mesmo** benefício nestes dois casos, então não vale a pena
     * separá-las aqui — o livro trata as duas na mesma frase.
     */
    fun temMestria(personagem: Personagem): Boolean =
        personagem.vantagensTotais.any {
            it.definicaoId == ID_TREINADO_POR_UM_MESTRE || it.definicaoId == ID_MESTRE_DE_ARMAS
        }

    /** O nome da vantagem que está valendo, para a tela poder citá-la. */
    fun nomeDaMestria(personagem: Personagem): String? =
        personagem.vantagensTotais.firstOrNull {
            it.definicaoId == ID_TREINADO_POR_UM_MESTRE || it.definicaoId == ID_MESTRE_DE_ARMAS
        }?.nome

    /** A penalidade do Golpe Rápido para ESTE personagem: -6, ou -3 com mestria. */
    fun penalidadeGolpeRapido(personagem: Personagem): Int =
        if (temMestria(personagem)) GOLPE_RAPIDO / 2 else GOLPE_RAPIDO

    /**
     * A penalidade **por apara extra** — o valor de um degrau, não o total.
     *
     * [armaDeEsgrima] é a outra metade da regra: adaga de esgrima, rapieira,
     * sabre e terçado (MB p.71).
     */
    fun penalidadePorAparaExtra(personagem: Personagem, armaDeEsgrima: Boolean): Int {
        val mestria = temMestria(personagem)
        return when {
            mestria && armaDeEsgrima -> -1
            mestria || armaDeEsgrima -> -2
            else -> APARA_EXTRA
        }
    }

    /**
     * O total acumulado na [numeroDaApara]-ésima apara do turno.
     *
     * A **primeira** apara do turno não tem penalidade; a segunda paga um degrau,
     * a terceira dois, e assim por diante.
     */
    fun penalidadeAcumulada(
        personagem: Personagem,
        numeroDaApara: Int,
        armaDeEsgrima: Boolean = false
    ): Int {
        val extras = (numeroDaApara - 1).coerceAtLeast(0)
        return extras * penalidadePorAparaExtra(personagem, armaDeEsgrima)
    }

    /** As perícias de esgrima do livro (MB p.71), pelos ids do catálogo. */
    private val IDS_ESGRIMA = setOf("adaga_de_esgrima", "rapieira", "sabre", "tercado")

    /** Se a perícia de apara escolhida é de esgrima. */
    fun ehEsgrima(periciaId: String?): Boolean {
        val id = periciaId?.lowercase()?.removePrefix("racial_")?.trim().orEmpty()
        return id.isNotBlank() && IDS_ESGRIMA.any { id == it || id.startsWith(it) }
    }

    /** O rótulo do Golpe Rápido, já com o número certo e a ressalva. */
    fun rotuloGolpeRapido(personagem: Personagem): String {
        val p = penalidadeGolpeRapido(personagem)
        val base = "Golpe Rápido: dois ataques, os dois a $p"
        val mestria = nomeDaMestria(personagem)
        return if (mestria != null) "$base (metade, por $mestria)" else base
    }

    /**
     * O mesmo, para o TalkBack.
     *
     * ⚠️ Não diz se está marcado — quem anuncia o estado é o leitor de tela.
     * Ver `UiA11y.linhaAlternavel`.
     */
    fun rotuloAcessivelGolpeRapido(personagem: Personagem): String {
        val p = -penalidadeGolpeRapido(personagem)
        val mestria = nomeDaMestria(personagem)
        return "Golpe Rápido. Dois ataques no mesmo turno, cada um com menos $p. " +
            (mestria?.let { "Metade da penalidade, por $it. " } ?: "") +
            "Vale para combate desarmado e Armas Brancas."
    }
}
