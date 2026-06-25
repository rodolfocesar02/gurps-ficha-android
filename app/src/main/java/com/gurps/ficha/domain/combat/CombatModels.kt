package com.gurps.ficha.domain.combat

/**
 * Lote 359 (Saga B1): modelos do combate GURPS 4ª ed. Kotlin PURO — nenhuma dependência
 * de Android nem da ficha; o herói é convertido para Combatente em lote posterior.
 * Domínio em PT-BR como o resto do projeto. Referências de regra em // MB p.XXX.
 *
 * Divergência do plano: o §4.1 do PLANO_GURPS_SAGA_v2 não existe no repositório; os campos
 * foram derivados do Módulo Básico (combate, p.362+) e do Skill_GURPS.MD.
 */

/** Posturas do combatente (MB p.551 — modificadores de postura). */
enum class Postura(val rotulo: String) {
    EM_PE("em pé"),
    AGACHADO("agachado"),
    AJOELHADO("ajoelhado"),
    SENTADO("sentado"),
    RASTEJANDO("rastejando"),
    DEITADO("deitado")
}

/** Condições/estados que afetam quais manobras são legais e as rolagens (detalhe nos lotes B4/B5). */
enum class Condicao(val rotulo: String) {
    ATORDOADO("atordoado"),       // MB p.420 — só Defesa Total ou recuperar
    CAIDO("caído"),               // derrubado involuntariamente
    INCONSCIENTE("inconsciente"),
    AGARRADO("agarrado"),
    IMOBILIZADO("imobilizado"),   // Lote 411: preso no chão, indefeso (MB p.371)
    SUFOCANDO("sufocando"),       // Lote 412: estrangulado, perde 1 PF/turno (MB p.371/437)
    SANGRANDO("sangrando"),       // Lote PONTE-2: ferimento que sangra; testa HT por intervalo ou perde PV (MB p.420)
    SURPRESO("surpreso")
}

/** Manobras de turno (MB p.362-366 / Skill_GURPS "Manobras"). */
enum class Manobra(val rotulo: String) {
    ATAQUE("Ataque"),
    ATAQUE_TOTAL("Ataque Total"),
    DEFESA_TOTAL("Defesa Total"),
    MOVER("Mover"),
    MOVER_E_ATACAR("Mover e Atacar"),
    MUDAR_POSTURA("Mudar de Postura"),
    PREPARAR("Preparar"),
    AGUARDAR("Aguardar"),
    AVALIAR("Avaliar"),
    FINTAR("Fintar"),              // Lote 383: Disputa Rápida → reduz a defesa do alvo no próximo golpe (MB p.366)
    AGARRAR("Agarrar"),           // Lote 386: agarra o oponente → estado AGARRADO (−4 DX) (MB p.370)
    DERRUBAR("Derrubar"),         // Lote 386: derruba um oponente agarrado (Disputa Rápida) (MB p.371)
    GOLPE_RAPIDO("Golpe Rápido"), // Lote 408: dois ataques corpo-a-corpo, cada um a −6 (MB p.370)
    ENCONTRAO("Encontrão"),       // Lote 409: colisão (PV×vel/100 dados, dano mútuo, derrubada) (MB p.371)
    EMPURRAO("Empurrão"),         // Lote 410: empurra o alvo (GdP×2 → projeção/knockback, sem lesão) (MB p.371)
    IMOBILIZAR("Imobilizar"),     // Lote 411: prende no chão um oponente agarrado (Disputa de ST) (MB p.371)
    ESTRANGULAR("Estrangular"),   // Lote 412: asfixia/estrangula um agarrado (Disputa de ST → sufoca) (MB p.371)
    DESVENCILHAR("Desvencilhar-se"), // Lote 422: herói AGARRADO se solta (Disputa Rápida de ST) (MB p.371)
    CHAVE_MEMBRO("Chave de Membro"), // Lote PONTE-1: chave num membro de um alvo agarrado (Disputa de ST → dano cont) (AM p.69-70/81)
    MATA_LEAO("Mata-Leão"),          // Lote PONTE-1: estrangulamento com 2 mãos (+3 ST) num alvo agarrado (AM p.77)
    APONTAR("Apontar"),            // mira arma à distância → +Precisão no próximo tiro (MB p.364)
    FOGO_RETENCAO("Fogo de Retenção"), // Lote 396: arma CdT 5+ cobre a área → acerta quem avançar (MB p.409)
    CONCENTRAR("Concentrar-se"),
    NAO_FAZER_NADA("Não Fazer Nada")
}

/** Opção da manobra Defesa Total (Lote 388, MB p.366). */
enum class DefesaTotalModo(val rotulo: String) {
    AUMENTADA("Aumentada"), // +2 numa defesa ativa escolhida
    DUPLA("Dupla")          // se a 1ª defesa falhar, tenta uma 2ª defesa DIFERENTE contra o mesmo ataque
}

/**
 * Defesas já usadas pelo combatente NESTE turno — base para as regras de B5
 * (apara múltipla −4 cumulativa, bloqueio/recuo 1×/turno). Em B1 é só o registro.
 */
data class DefesasUsadas(
    val aparasPorArma: Map<String, Int> = emptyMap(), // arma → nº de aparas já feitas
    val bloqueouEsteTurno: Boolean = false,
    val esquivouEsteTurno: Boolean = false,
    val retracaoUsada: Boolean = false                // recuo só vale 1×/turno por inimigo
)

/**
 * Estatísticas de combate de um NPC/criatura (o bestiário do B6 popula isto).
 * velocidadeBasica e deslocamento têm default derivado de DX/HT (MB p.17).
 */
data class NpcStats(
    val st: Int = 10,
    val dx: Int = 10,
    val iq: Int = 10,
    val ht: Int = 10,
    val pvMax: Int = st,
    val rd: Int = 0,
    val velocidadeBasica: Double = (dx + ht) / 4.0,
    val deslocamento: Int = ((dx + ht) / 4.0).toInt(),
    val armaNome: String = "",
    val armaDano: String = "",
    val armaTipo: String = "",   // cont/corte/perf/imp...
    val armaNh: Int = 10,        // NH do ataque principal (B7: o motor precisa do "para acertar")
    val alcanceMetros: Int = 1,
    /** Lote 380: arma de fogo? (o BD do escudo do herói não vale contra fogo — MB p.375.) Default infere pelo nome. */
    val armaDeFogo: Boolean = false,
    /** Lote 381: Modificador de Tamanho (MT) da criatura — somado ao acerto À DISTÂNCIA contra ela (MB p.549). */
    val modificadorTamanho: Int = 0,
    /** Lote 385: Tolerância a Ferimentos (MB p.381) — reduz dano pi/perf (mortos-vivos = NÃO_VIVO, etc.). */
    val tolerancia: ToleranciaFerimentos = ToleranciaFerimentos.NORMAL,
    /** Comportamento tático (Lote 363/B6): 0-10. Alimenta o NpcCombatBrain. */
    val agressividade: Int = 5,
    val moral: Int = 5
)

/**
 * Um combatente no encontro. Os campos de ESTADO mutável (pv/pf/postura/condições) mudam
 * durante a luta; os imutáveis definem a iniciativa e o alcance.
 */
data class Combatente(
    val id: String,
    val nome: String,
    val ehHeroi: Boolean = false,
    val dx: Int,
    val velocidadeBasica: Double,
    val deslocamento: Int,
    val pvMax: Int,
    var pvAtual: Int = pvMax,
    var pfAtual: Int = 10,
    var postura: Postura = Postura.EM_PE,
    val condicoes: MutableSet<Condicao> = mutableSetOf(),
    var defesasUsadas: DefesasUsadas = DefesasUsadas(),
    /** Lote 382: PV perdidos desde o último turno deste combatente → penalidade de Choque no próximo (MB p.419). */
    var choquePendente: Int = 0,
    /** Lote 403: metros percorridos no último movimento → penalidade de Velocidade/Distância ao ser alvejado (MB p.550). */
    var velocidadeAtual: Int = 0,
    // ── Sangramento (Lote PONTE-2, MB p.420 / AM p.138) — estado vivo do ferimento que sangra. ──
    var sangramentoAtivo: Boolean = false,
    var sangramentoLesaoPV: Int = 0,          // maior lesão única que sangra → penalidade −1 a cada 5 PV
    var sangramentoPenalidadeLocal: Int = 0,  // penalidade extra de local grave (AM p.138; 0 = sangramento comum)
    var sangramentoIntervaloSeg: Int = 60,    // 60s comum; 30s nos locais graves do AM
    var sangramentoUltimaRodada: Int = Int.MIN_VALUE, // rodada do último teste (MIN = recém-iniciado, inicializa no 1º tick)
    var sangramentoTestesLimpos: Int = 0,     // intervalos seguidos sem sangrar; 3 = estanca de vez
    /** Stats completos quando é NPC do bestiário; null para o herói (vem da ficha). */
    val stats: NpcStats? = null
) {
    val vivo: Boolean get() = pvAtual > -pvMax && Condicao.INCONSCIENTE !in condicoes
    /** Caído = derrubado (condição) ou postura deitada. */
    val caido: Boolean get() = Condicao.CAIDO in condicoes || postura == Postura.DEITADO
    /** Cambaleante (MB p.380): com menos de 1/3 do PV Inicial, Vel.Básica/Deslocamento e Esquiva caem à metade. */
    val cambaleante: Boolean get() = vivo && pvAtual * 3 < pvMax
    /** Deslocamento efetivo: metade (arredondado p/ cima) se cambaleante (MB p.380). */
    /**
     * Deslocamento efetivo: a postura reduz o movimento (Lote 400, MB p.368) — em pé/agachado = cheio;
     * ajoelhado/rastejando = 1/3; deitado = 1; sentado = 0; depois, cambaleante corta pela metade (MB p.380).
     */
    val deslocamentoEfetivo: Int get() {
        val porPostura = when (postura) {
            Postura.EM_PE, Postura.AGACHADO -> deslocamento
            Postura.AJOELHADO, Postura.RASTEJANDO -> deslocamento / 3
            Postura.DEITADO -> 1
            Postura.SENTADO -> 0
        }
        return if (cambaleante) (porPostura + 1) / 2 else porPostura
    }
}
