package com.gurps.ficha.model

/**
 * Um escudo do catálogo — MB p.288.
 *
 * ⚠️ [rdDoEscudo] e [pv] **não protegem o personagem**: protegem o escudo, e só
 * entram em jogo com a regra opcional *Dano a Escudos* (p.484). Quem protege o
 * personagem é o [db]. Os dois campos ficam com nome longo de propósito — `rd`
 * solto, ao lado de uma armadura, seria lido como redução de dano do jogador.
 *
 * Os três últimos campos existiam no `escudos.v1.json` desde sempre e o app os
 * jogava fora (Lote EQP-6).
 */
data class EscudoCatalogoItem(
    val id: String,
    val nome: String,
    val nt: Int?,
    val db: Int,
    val custo: Float?,
    val pesoKg: Float?,
    val stMinimo: Int?,
    val observacoes: String,
    /** RD do próprio escudo, para a regra opcional de Dano a Escudos. */
    val rdDoEscudo: Int? = null,
    /** PV do próprio escudo, idem. */
    val pv: Int? = null,
    /** Classe de Legalidade; v. p.267. */
    val cl: Int? = null
)
