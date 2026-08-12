package com.gurps.ficha.domain.rules

/**
 * **Tipo de dano** e o quanto ele machuca (MB p.379-380).
 *
 * ## Por que mora aqui e não em `domain/combat/`
 *
 * Mesma história do [LocalAtaque]: este enum nasceu dentro do pacote do combate
 * tático, mas é **tabela do livro** — não depende de turno, grade nem sessão. Com
 * a aba Rolagem ganhando o botão **PV** (Lote MB-7), deixá-lo lá obrigaria a
 * ficha a importar o motor da Saga justamente no ponto que o projeto quer
 * separar em dois apps.
 *
 * Mudou de pacote em 08/08; o combate passou a importá-lo daqui. **Nenhum valor
 * mudou** — é recorte, não regra nova.
 *
 * O multiplicador do livro, palavra por palavra:
 *
 * > Pouco perfurante (pa-): ×0,5. · Por queimadura, corrosão, contusão, fadiga,
 * > toxina e perfurante (pa): ×1. · Corte e muito perfurante (pa+): ×1,5. ·
 * > Perfuração e extremamente perfurante (pa++): ×2.
 */
enum class DanoTipo(val rotulo: String, val multBase: Double) {
    CONT("cont", 1.0),        // contusão
    CORT("corte", 1.5),       // corte — MB p.379
    PI_MENOS("pi-", 0.5),     // perfurante pequeno
    PI("pi", 1.0),            // perfurante
    PI_MAIS("pi+", 1.5),
    PI_MAIS_MAIS("pi++", 2.0),
    PERF("perf", 2.0),        // perfuração (impaling)

    // ── Lote EQP-12: os tipos que faltavam da tabela da p.380 ──
    /**
     * **Queimadura (qmd)** — MB p.44: *"chamas, um feixe de energia ou
     * queimaduras elétricas localizadas. É possível começar incêndios!"*
     *
     * Onze armas do catálogo causam este dano — laser, feixe iônico,
     * lança-chamas e a espada de energia — e até o Lote EQP-12 **nenhuma delas
     * tinha botão** no diálogo de ferimento.
     */
    QMD("qmd", 1.0),

    /**
     * **Corrosão (cor)** — MB p.43 e p.380: *"ácido, desintegração ou algo
     * semelhante. Para cada 5 pontos de dano básico causado, a RD do alvo é
     * reduzida em 1 ponto, além do dano regular (seres vivos recuperam a RD
     * natural na mesma velocidade que os PV)."*
     *
     * 🔴 É o **único** tipo que muda a ficha DEPOIS do golpe: a armadura se gasta.
     * Ver [CorrosaoNaArmadura].
     *
     * ⚠️ Nenhuma arma do catálogo causa corrosão — ela entra pelo dano digitado à
     * mão pelo Mestre, que é como ácido e sopro de dragão chegam à mesa.
     */
    COR("cor", 1.0),

    /**
     * **Fadiga (fad)** — MB p.43: *"O ataque não é letal. (…) Ele reduz o número
     * de **PF**, não de PV, e não afeta máquinas."*
     *
     * 🔴 É o único tipo que **não desconta PV**. Choque elétrico de baixa
     * amperagem, explosão mental, hipotermia, inanição.
     */
    FAD("fad", 1.0),

    /**
     * **Toxina (tox)** — MB p.44: *"dano celular na forma de doenças,
     * envenenamento ou radiação. Normalmente não afeta máquinas."*
     */
    TOX("tox", 1.0),

    /**
     * **Atribulação (at)** — ⚠️ **não é dano.**
     *
     * Quatro armas do catálogo a usam (pistola paralisante, arreador,
     * eletrolasers) e ela não tira PV nem PF: exige um **teste de HT**, e o
     * fracasso traz o efeito da arma.
     *
     * Está no enum porque é o que o jogador escolhe na tela — mas
     * [causaPerdaDePontos] responde `false`, e é isso que impede a conta de
     * ferimento de rodar sobre ela.
     */
    AT("at", 0.0);

    /**
     * Ganha **×3 nos vitais** (MB p.399).
     *
     * 🔴 Era escrito ao contrário — `this != CONT && this != CORT` — e por isso
     * **qualquer tipo novo entrava valendo ×3 nos vitais sozinho**. A queimadura
     * teria triplicado no peito no dia em que o enum crescesse, sem nenhum teste
     * quebrar: todos os `when` do projeto têm `else`, então o compilador também
     * ficaria calado.
     *
     * Lista positiva: só entra quem o livro nomeia.
     */
    val perfuranteOuPerf: Boolean
        get() = this in setOf(PI_MENOS, PI, PI_MAIS, PI_MAIS_MAIS, PERF)

    /**
     * Pode virar **trauma por impacto** através de armadura flexível (MB p.380).
     *
     * > *"Um ataque que provoca dano por **contusão, corte, perfuração ou
     * > perfurante** pode provocar trauma por impacto."*
     *
     * ⚠️ A queimadura **não** está na lista: fogo que não passa da armadura não
     * machuca por impacto.
     */
    val causaTraumaPorImpacto: Boolean
        get() = this in setOf(CONT, CORT, PERF, PI_MENOS, PI, PI_MAIS, PI_MAIS_MAIS)

    /**
     * Desconta **PF** em vez de PV (MB p.43) — Lote EQP-14.
     *
     * ⚠️ Só a fadiga. A conta do ferimento é a mesma até o fim; o que muda é
     * **onde** o número é debitado. Tratar isso como um tipo comum faria um
     * choque elétrico matar alguém.
     */
    val atingePf: Boolean get() = this == FAD

    /**
     * Se o ataque tira pontos de algum lugar.
     *
     * `false` só na **atribulação**, que não é dano: é teste de HT.
     */
    val causaPerdaDePontos: Boolean get() = this != AT
}

/**
 * Lote 385: Tolerância a Ferimentos (MB p.380/381). Reduz o multiplicador de
 * ferimento de pi/perf (mortos-vivos, máquinas, objetos, enxames). NORMAL = ser
 * vivo comum.
 */
enum class ToleranciaFerimentos { NORMAL, NAO_VIVO, HOMOGENEO, DIFUSO }
