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
    APONTAR("Apontar"),            // mira arma à distância → +Precisão no próximo tiro (MB p.364)
    CONCENTRAR("Concentrar-se"),
    NAO_FAZER_NADA("Não Fazer Nada")
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
    /** Stats completos quando é NPC do bestiário; null para o herói (vem da ficha). */
    val stats: NpcStats? = null
) {
    val vivo: Boolean get() = pvAtual > -pvMax && Condicao.INCONSCIENTE !in condicoes
    /** Caído = derrubado (condição) ou postura deitada. */
    val caido: Boolean get() = Condicao.CAIDO in condicoes || postura == Postura.DEITADO
}
