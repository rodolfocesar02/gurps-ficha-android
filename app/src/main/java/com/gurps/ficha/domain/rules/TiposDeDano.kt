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
    PERF("perf", 2.0);        // perfuração (impaling)

    /** Perfurante/perfuração ganham ×3 nos vitais (Mesa Virtual: startsWith('pi') || 'perf'). */
    val perfuranteOuPerf: Boolean get() = this != CONT && this != CORT
}

/**
 * Lote 385: Tolerância a Ferimentos (MB p.380/381). Reduz o multiplicador de
 * ferimento de pi/perf (mortos-vivos, máquinas, objetos, enxames). NORMAL = ser
 * vivo comum.
 */
enum class ToleranciaFerimentos { NORMAL, NAO_VIVO, HOMOGENEO, DIFUSO }
