package com.gurps.ficha.domain.combat

import com.gurps.ficha.domain.rules.LocalAtaque

/**
 * Lote 360 (Saga B2): tabelas de modificadores de combate do GURPS 4ª ed. (Kotlin puro).
 * Valores do Módulo Básico — referência // MB nos comentários. Reutilizado pelo B3 (dano localizado).
 */

// `LocalAtaque` MUDOU DE PACOTE em 28/07: foi para `domain/rules/LocaisDeAtaque.kt`.
// Motivo: e tabela do livro, nao logica de combate -- e a aba Rolagem passou a
// precisar dela (Lote MIRA-1). Deixa-la aqui amarraria a ficha a Saga, no ponto
// exato que o projeto quer separar em dois apps. Nenhum valor mudou.

/** Penumbra/escuridão/névoa — penalidade ao ataque por visibilidade. MB p.394 (combate no escuro). */
enum class Visibilidade(val rotulo: String, val penalidade: Int) {
    NORMAL("iluminado", 0),
    NEVOA_LEVE("névoa leve", -1),
    NEVOA_DENSA("névoa densa", -4),
    PENUMBRA("penumbra", -3),
    ESCURIDAO_PARCIAL("escuridão parcial", -6),
    ESCURIDAO_TOTAL("escuridão total", -10)
}

/** Variantes do Ataque Total. MB p.365. */
enum class AtaqueTotalModo(val rotulo: String) {
    DETERMINADO("Determinado"), // +4 NH
    FORTE("Forte"),             // +2 dano (ou +1/dado); NH normal — bônus de dano é no B3
    DUPLO("Duplo"),             // 2 ataques; NH normal cada
    TELEGRAFADO("Telegrafado")  // +4 NH, mas +2 nas defesas do inimigo (Artes Marciais)
}

/** Lote PONTE-4 (AM p98): modo do Ataque Dedicado — Determinado (+2 acerto) ou Forte (+1 dano). */
enum class DedicadoModo(val rotulo: String) {
    DETERMINADO("Determinado"), // +2 no NH
    FORTE("Forte")              // +1 no dano (NH normal)
}

object ModificadoresCombate {

    /** Modificador de ataque CORPO-A-CORPO pela postura do atacante. MB p.551. */
    fun modPostura(postura: Postura): Int = when (postura) {
        Postura.EM_PE -> 0
        Postura.AGACHADO, Postura.AJOELHADO, Postura.SENTADO -> -2
        Postura.RASTEJANDO, Postura.DEITADO -> -4
    }

    /** Modificador de NH do Ataque Total para ACERTAR (o bônus de dano do Forte é tratado no B3). */
    fun modAtaqueTotal(modo: AtaqueTotalModo): Int = when (modo) {
        AtaqueTotalModo.DETERMINADO, AtaqueTotalModo.TELEGRAFADO -> 4
        AtaqueTotalModo.FORTE, AtaqueTotalModo.DUPLO -> 0
    }
}
