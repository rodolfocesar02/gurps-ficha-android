package com.gurps.ficha.domain.rules

/**
 * **Armadura Flexível e Trauma por Impacto** — MB p.380.
 *
 * > *"Armaduras flexíveis como jaquetas de couro, cotas de malha ou coletes
 * > balísticos modernos são muito mais leves que uma armadura rígida, mas não
 * > absorvem a força total dos golpes que recebem."*
 * >
 * > *"Para cada 10 pontos completos de dano por corte, perfuração ou perfurante
 * > ou 5 pontos de dano por contusão barrados pela RD, o personagem sofre 1 PV de
 * > dano devido ao trauma por impacto. Essa é uma lesão real, não dano básico.
 * > Mas não há modificador de ferimento."*
 * >
 * > *"Contudo, se um único ponto de dano penetrar a RD flexível, não sofre trauma
 * > por impacto."*
 *
 * ## 🔴 Por que este arquivo existiu tarde demais
 *
 * O `*` do catálogo era lido desde sempre — `CoberturaDaArmadura.Rd.flexivel` — e
 * o diálogo de ferimento até **anunciava** *"flexível: não impede trauma por
 * impacto"*. Não havia uma linha de código calculando isso. O rótulo prometia uma
 * mecânica que não existia.
 *
 * ## ⚠️ A trava que quase me escapou
 *
 * O trauma só existe quando a armadura barra o golpe **inteiro**. Um único ponto
 * que passe cancela o trauma — não o reduz, cancela. Isso torna trauma e lesão
 * normal **mutuamente exclusivos**, e é o que deixa esta regra entrar sem mexer
 * no resto do cálculo de ferimento.
 *
 * Eu apresentei esta regra ao usuário com um exemplo que ignorava essa trava
 * (10 de dano contra RD 2 flexível, que **penetra** e portanto não dá trauma
 * nenhum). Fica registrado porque foi o erro que motivou o lote.
 */
object TraumaPorImpacto {

    /** Contusão precisa de 5 barrados por PV; o resto, de 10 (MB p.380). */
    const val POR_PV_CONTUSAO = 5
    const val POR_PV_DEMAIS = 10

    fun divisorDe(tipo: DanoTipo): Int =
        if (tipo == DanoTipo.CONT) POR_PV_CONTUSAO else POR_PV_DEMAIS

    /**
     * @param danoBruto o dano rolado, antes de qualquer RD.
     * @param penetrante quanto sobrou depois de **toda** a RD. Qualquer valor
     *   acima de zero zera o trauma.
     * @param rdFlexivel a soma da RD das peças marcadas com `*`.
     * @param rdRigida a soma das peças **sem** `*`, incluindo a RD natural.
     */
    fun calcular(
        danoBruto: Int,
        tipo: DanoTipo,
        penetrante: Int,
        rdFlexivel: Int,
        rdRigida: Int = 0
    ): Int {
        if (penetrante > 0) return 0
        if (rdFlexivel <= 0 || danoBruto <= 0) return 0
        // ⚠️ Lote EQP-12: nem todo tipo causa trauma. O livro lista contusão,
        // corte, perfuração e perfurante — queimadura fica de fora.
        if (!tipo.causaTraumaPorImpacto) return 0
        return barradoPelaCamadaFlexivel(danoBruto, rdFlexivel, rdRigida) / divisorDe(tipo)
    }

    /**
     * Quanto a camada **flexível** barrou sozinha.
     *
     * ⚠️ *"Se uma segunda RD estiver sobreposta à RD flexível, somente o dano que
     * penetrar a camada externa é capaz de provocar trauma por impacto"* (MB
     * p.380). A ficha não guarda a **ordem** das peças, então aqui se assume o
     * caso comum: **o rígido por fora, o flexível por dentro** — que é a própria
     * frase do livro ("sobreposta à RD flexível").
     *
     * A conta fica visível na tela justamente por isso: um Mestre que tenha o
     * couro por cima da placa vê o número e corrige.
     */
    fun barradoPelaCamadaFlexivel(danoBruto: Int, rdFlexivel: Int, rdRigida: Int): Int =
        (danoBruto - rdRigida.coerceAtLeast(0)).coerceIn(0, rdFlexivel)

    /**
     * A conta por extenso, para a tela mostrar em vez de só o resultado.
     * Null quando não houve trauma — aí não há o que explicar.
     */
    fun conta(danoBruto: Int, tipo: DanoTipo, rdFlexivel: Int, rdRigida: Int, trauma: Int): String? {
        if (trauma <= 0) return null
        val barrado = barradoPelaCamadaFlexivel(danoBruto, rdFlexivel, rdRigida)
        return "trauma por impacto: $barrado barrados por armadura flexível ÷ ${divisorDe(tipo)} = $trauma PV"
    }

    /** O mesmo, sem sinal cru e sem símbolo de divisão, para quem ouve a tela. */
    fun descricaoAcessivel(danoBruto: Int, tipo: DanoTipo, rdFlexivel: Int, rdRigida: Int, trauma: Int): String? {
        if (trauma <= 0) return null
        val barrado = barradoPelaCamadaFlexivel(danoBruto, rdFlexivel, rdRigida)
        return "A armadura flexível barrou o golpe inteiro, mas $barrado pontos " +
            "atravessaram como trauma por impacto: perde ${RotuloAcessivel.valor(trauma)} " +
            if (trauma == 1) "ponto de vida." else "pontos de vida."
    }
}

/**
 * **Corrosão e Lesão** — MB p.380. Lote EQP-13.
 *
 * > *"Um ataque que causa dano por corrosão (cor) — ácidos, feixes de
 * > desintegração, etc. — destrói **um ponto da RD do alvo a cada 5 pontos de
 * > dano considerado "dano penetrante"**."*
 *
 * ## 🔴 O único tipo que muda a ficha depois do golpe
 *
 * Todos os outros terminam quando o PV é descontado. A corrosão deixa marca: a
 * armadura fica **permanentemente** mais fraca, e o próximo golpe no mesmo lugar
 * encontra menos proteção.
 *
 * ## ⚠️ Por que o app calcula e NÃO aplica sozinho
 *
 * Aplicar significaria reescrever o campo `armaduraRd` da peça — e a peça pode
 * ser uma só de várias no local, sem o app saber **qual** o ácido atingiu. Um
 * desconto automático na peça errada é pior que nenhum: some RD de onde não
 * devia, em silêncio, e o jogador só descobre no golpe seguinte.
 *
 * Então: o número aparece na tela, e quem edita o RD é o jogador — no campo que
 * o Lote EQP-8 criou justamente para isso.
 *
 * ⚠️ *"Seres vivos recuperam a RD natural na mesma velocidade que os PV"* — vale
 * para a RD **do corpo**, não para a armadura. Armadura corroída não sara.
 */
object CorrosaoNaArmadura {

    /** Cinco pontos de dano penetrante por ponto de RD destruído (p.380). */
    const val POR_PONTO_DE_RD = 5

    /**
     * Quantos pontos de RD o ácido destruiu.
     *
     * ⚠️ A base é o **dano penetrante**, não o rolado. Um ácido que a armadura
     * barrou inteiro não a corrói — é o que passa que a come por dentro.
     */
    fun rdDestruida(tipo: DanoTipo, penetrante: Int): Int {
        if (tipo != DanoTipo.COR) return 0
        if (penetrante <= 0) return 0
        return penetrante / POR_PONTO_DE_RD
    }

    /** A conta pronta para a tela. Null quando não houve corrosão. */
    fun conta(penetrante: Int, destruida: Int): String? {
        if (destruida <= 0) return null
        return "Corrosão: $penetrante penetrante ÷ $POR_PONTO_DE_RD = " +
            "$destruida ponto(s) de RD destruídos. Ajuste o RD da peça no lápis — " +
            "armadura corroída não se recupera."
    }

    /** O mesmo, sem sinal cru, para quem ouve a tela. */
    fun contaAcessivel(penetrante: Int, destruida: Int): String? {
        if (destruida <= 0) return null
        return "O ácido destruiu ${RotuloAcessivel.valor(destruida)} " +
            (if (destruida == 1) "ponto" else "pontos") +
            " de resistência a dano da armadura. Ajuste o campo de RD da peça na aba " +
            "Equipamentos: armadura corroída não se recupera sozinha."
    }
}
