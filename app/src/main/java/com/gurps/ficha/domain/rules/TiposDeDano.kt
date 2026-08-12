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
    QMD("qmd", 1.0);

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
}

/**
 * Lote 385: Tolerância a Ferimentos (MB p.380/381). Reduz o multiplicador de
 * ferimento de pi/perf (mortos-vivos, máquinas, objetos, enxames). NORMAL = ser
 * vivo comum.
 */
enum class ToleranciaFerimentos { NORMAL, NAO_VIVO, HOMOGENEO, DIFUSO }
