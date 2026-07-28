package com.gurps.ficha.domain.rules

/**
 * Onde mirar num alvo, e o que isso custa no ataque (MB p.398-400).
 *
 * ## Por que mora aqui e não em `domain/combat/`
 *
 * Este enum nasceu no Lote 360, dentro do pacote do combate tático. Mas ele é
 * **tabela do livro** — não depende de turno, grade nem sessão de combate. Com a
 * aba Rolagem ganhando a mira (Lote MIRA-1), deixá-lo lá criaria uma amarra da
 * ficha para a Saga justamente no ponto que o projeto quer separar em dois apps.
 *
 * Mudou de pacote em 28/07; o combate passou a importá-lo daqui. Nenhum valor
 * mudou.
 */
enum class LocalAtaque(val rotulo: String, val penalidadeAtaque: Int) {
    TORSO("torso", 0),
    CRANIO("crânio", -7),     // MB p.398
    OLHO("olho", -9),
    ROSTO("rosto", -5),
    PESCOCO("pescoço", -5),
    VITAIS("vitais", -3),
    INGLE("virilha", -3),
    BRACO("braço", -2),
    PERNA("perna", -2),
    MAO("mão", -4),
    PE("pé", -4)
}

/**
 * Mira: a lista de alvos com o NH já calculado, pronta para a tela.
 *
 * Ideia do usuário: tocar no NH do ataque abre um diálogo com todos os locais e
 * **o número já reduzido** — se a Faca é NH 12 e você quer o olho (−9), a tela
 * mostra **3**. Sem penalidade entre parênteses, sem conta mental no meio da
 * mesa.
 *
 * **Só o ataque, nunca o dano.** O dano depende da RD do oponente, que a ficha
 * não tem e nunca terá — é informação do Mestre. Decisão do usuário, e está
 * certa: calcular sem a RD seria inventar número.
 *
 * Kotlin puro e testável.
 */
object MiraRules {

    enum class Grupo(val rotulo: String) {
        CORPO("Onde acertar"),
        ARMA("Arma do oponente")
    }

    /**
     * Um alvo possível.
     *
     * [detalhe] carrega a consequência da regra ("×4 de ferimento", "cega o
     * olho") — o jogador escolhe onde mirar pelo efeito, não pelo número.
     */
    data class Opcao(
        val rotulo: String,
        val penalidade: Int,
        val grupo: Grupo,
        val detalhe: String? = null
    ) {
        /** O NH final. Pode ficar negativo — e isso é informação, não erro. */
        fun nhCom(nhBase: Int): Int = nhBase + penalidade

        fun descricaoAcessivel(nhBase: Int): String {
            val nh = nhCom(nhBase)
            val comoLer = if (nh < 0) "menos ${-nh}" else "$nh"
            return "$rotulo. Nível $comoLer." + (detalhe?.let { " $it" } ?: "")
        }
    }

    /**
     * Golpe na arma do oponente (MB p.400). A penalidade vem do **alcance** da
     * arma dele, não do tamanho da sua.
     */
    private val ARMAS = listOf(
        Opcao("Arma pequena", -5, Grupo.ARMA, "Alcance C ou pistola — faca, adaga."),
        Opcao("Arma média", -4, Grupo.ARMA, "Alcance 1 ou arma de fogo média — espada, maça."),
        Opcao("Arma grande", -3, Grupo.ARMA, "Alcance 2+ ou rifle — lança, espada grande.")
    )

    private val CORPO = listOf(
        Opcao("Torso", 0, Grupo.CORPO, "Alvo padrão. Sem efeito especial."),
        Opcao("Braço", -2, Grupo.CORPO, "Ferimento grave incapacita o membro."),
        Opcao("Perna", -2, Grupo.CORPO, "Ferimento grave incapacita o membro."),
        Opcao("Vitais", -3, Grupo.CORPO, "×3 de ferimento para perfurante e perfuração."),
        Opcao("Virilha", -3, Grupo.CORPO, "Dobro do choque e −5 no teste de nocaute."),
        Opcao("Mão", -4, Grupo.CORPO, "Dano acima de 1/3 dos PV já incapacita."),
        Opcao("Pé", -4, Grupo.CORPO, "Dano acima de 1/3 dos PV já incapacita."),
        Opcao("Braço com escudo", -4, Grupo.CORPO, "O escudo protege o braço."),
        Opcao("Rosto", -5, Grupo.CORPO, "Muitos elmos são abertos. −5 no nocaute."),
        Opcao("Pescoço", -5, Grupo.CORPO, "×2 de ferimento no corte. Pode decapitar."),
        Opcao("Crânio", -7, Grupo.CORPO, "×4 de ferimento, mas RD +2. −10 no nocaute."),
        Opcao("Mão com escudo", -8, Grupo.CORPO, "O escudo protege a mão."),
        Opcao("Olho", -9, Grupo.CORPO, "Dano acima de PV/10 cega o olho.")
    )

    /** Penalidade extra por querer **desarmar** em vez de quebrar (MB p.400). */
    const val PENALIDADE_DESARMAR = -2

    /**
     * Todos os alvos, do mais fácil para o mais difícil.
     *
     * [desarmar] só afeta o grupo da ARMA — no corpo não existe "desarmar".
     */
    fun opcoes(desarmar: Boolean = false): List<Opcao> {
        val armas = if (desarmar) {
            ARMAS.map {
                it.copy(
                    penalidade = it.penalidade + PENALIDADE_DESARMAR,
                    detalhe = "${it.detalhe} Desarmar: −2."
                )
            }
        } else {
            ARMAS
        }
        return CORPO.sortedByDescending { it.penalidade } +
            armas.sortedByDescending { it.penalidade }
    }
}
