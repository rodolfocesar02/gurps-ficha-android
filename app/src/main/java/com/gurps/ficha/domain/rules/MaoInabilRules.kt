package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * A penalidade por usar a **mão inábil** (MB p.14), e quem a anula.
 *
 * Ideia do usuário: um seletor no botão de Ataque distinguindo mão hábil e
 * inábil. Marcando inábil, −4; com **Ambidestria**, o redutor não aparece, mas
 * o seletor continua funcionando.
 *
 * **Por que isto é UI e não efeito declarado.** A Ambidestria não *concede*
 * bônus — ela **remove** uma penalidade que o app nunca aplicou. Declarar
 * `{"tipo":"atributo","alvo":"DX","valor":-4}` nela daria −4 a quem comprou a
 * vantagem, que é o oposto do livro. A penalidade pertence à situação (qual mão
 * está sendo usada), e a vantagem apenas a zera.
 *
 * O mesmo raciocínio vale para qualquer vantagem que "isenta de penalidade":
 * primeiro a penalidade tem que existir na tela, depois a vantagem a apaga.
 *
 * Kotlin puro e testável.
 */
object MaoInabilRules {

    /** A penalidade do livro, quando nada a anula. */
    const val PENALIDADE = -4

    private const val ID_AMBIDESTRIA = "ambidestria"

    /** Se a ficha tem Ambidestria (MB p.41). */
    fun temAmbidestria(personagem: Personagem): Boolean =
        personagem.vantagensTotais.any { it.definicaoId == ID_AMBIDESTRIA }

    /**
     * Quanto vale usar a mão inábil para ESTE personagem.
     *
     * Zero com Ambidestria — e zero também quando a mão usada é a hábil, claro.
     */
    fun penalidadeDe(personagem: Personagem, usandoMaoInabil: Boolean): Int = when {
        !usandoMaoInabil -> 0
        temAmbidestria(personagem) -> 0
        else -> PENALIDADE
    }

    /**
     * O que mostrar ao lado do seletor.
     *
     * Com Ambidestria a linha vira explicação em vez de número: some o "−4" mas
     * fica dito por que sumiu, senão o jogador acha que o app esqueceu.
     */
    fun rotuloDe(personagem: Personagem, usandoMaoInabil: Boolean): String = when {
        !usandoMaoInabil -> "Mão hábil"
        temAmbidestria(personagem) -> "Mão inábil — sem penalidade (Ambidestria)"
        else -> "Mão inábil ($PENALIDADE)"
    }

    /**
     * O mesmo, escrito para o TalkBack.
     *
     * ⚠️ Descreve **o que marcar significa**, não o estado atual: quem anuncia
     * marcado/não marcado é o TalkBack, pelo papel de caixa de seleção. Dizer
     * aqui também viraria eco.
     */
    fun rotuloAcessivel(personagem: Personagem): String =
        if (temAmbidestria(personagem)) {
            "Usar a mão inábil. Sem penalidade, porque o personagem tem Ambidestria."
        } else {
            "Usar a mão inábil. Penalidade de menos 4."
        }
}

/**
 * **Sem Um Dedo / Sem o Polegar** (MB p.157) — o Lote D-MIRA.
 *
 * > **Sem Um Dedo:** ele sofre uma penalidade de **−1 na DX da mão em questão
 * > (somente)**. −2 pontos.
 * >
 * > **Sem o Polegar:** ele sofre uma penalidade de **−5 na DX da mão em questão
 * > (somente)**. −5 pontos.
 *
 * ## ⚠️ "Da mão em questão (somente)" — e a ficha não guarda QUAL mão
 *
 * O catálogo tem uma entrada só, com dois custos, e nenhum campo dizendo se o
 * dedo que falta é o da mão hábil ou o da inábil. O app **não adivinha**: o
 * seletor de mão ganha uma segunda caixinha — *"esta é a mão sem o dedo"* — e o
 * jogador responde no momento do ataque.
 *
 * É a mesma decisão do Míope no MIRA-2b e do Assassino Relutante no D-MIRA:
 * quando a informação não está na ficha, **o app oferece em vez de chutar**.
 * Assumir que é sempre a mão inábil seria dar de graça a versão barata da
 * desvantagem para quem comprou a cara.
 *
 * ## Por que não é `porOpcao` no catálogo
 *
 * `porOpcao` resolveria os dois custos (−2 → −1, −5 → −5), mas o efeito é sobre
 * **um membro**, e o interpretador ignora efeito com escopo diferente de
 * `global` — de propósito, desde o primeiro dia. Aqui a "escolha do membro" é
 * situacional, não permanente: vive na rolagem, não no NH.
 */
object SemUmDedoRules {

    const val ID = "sem_um_dedo"

    /** −2 pontos: falta um dedo qualquer. */
    const val CUSTO_DEDO = -2
    const val PENALIDADE_DEDO = -1

    /** −5 pontos: falta o polegar, que é o que segura. */
    const val CUSTO_POLEGAR = -5
    const val PENALIDADE_POLEGAR = -5

    /** Se a ficha tem a desvantagem, em qualquer das duas versões. */
    fun tem(personagem: Personagem): Boolean =
        personagem.desvantagensTotais.any { it.definicaoId == ID }

    /**
     * A penalidade quando a mão usada é **a que perdeu o dedo**.
     *
     * ⚠️ Lê o `custoEscolhido`, não o nível: o catálogo guarda as duas versões
     * como degraus de custo (−2 e −5). Ler o nível daria −1 para o polegar.
     */
    fun penalidadeDe(personagem: Personagem, ehAMaoAfetada: Boolean): Int {
        if (!ehAMaoAfetada) return 0
        return personagem.desvantagensTotais
            .filter { it.definicaoId == ID }
            .sumOf { if (it.custoEscolhido <= CUSTO_POLEGAR) PENALIDADE_POLEGAR else PENALIDADE_DEDO }
    }

    /** O rótulo da caixinha, já com o número desta ficha. */
    fun rotuloDe(personagem: Personagem): String {
        val p = penalidadeDe(personagem, ehAMaoAfetada = true)
        val oQueFalta = if (p == PENALIDADE_POLEGAR) "o polegar" else "um dedo"
        return "É esta a mão sem $oQueFalta ($p na DX)"
    }

    const val ROTULO_ACESSIVEL =
        "Marcar que a mão usada neste ataque é a que perdeu o dedo. O livro " +
            "aplica a penalidade a uma mão somente, e a ficha não guarda qual."
}
