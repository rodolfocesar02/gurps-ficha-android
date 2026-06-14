package com.gurps.ficha.model

import com.google.gson.Gson
import com.gurps.ficha.domain.combat.Combatente
import com.gurps.ficha.domain.combat.NpcStats

/**
 * Lote 363 (Saga B6): catálogo de criaturas (bestiário) carregado de assets/bestiario.v1.json.
 *
 * Divergência do plano: o §4.5 do PLANO_GURPS_SAGA_v2 não existe no repo — o schema foi
 * desenhado a partir do Módulo Básico (stats de criaturas) e do que o NpcCombatBrain precisa.
 */

data class AtaqueCriatura(
    val nome: String = "",
    val dano: String = "",          // ex.: "1d-2"
    val tipo: String = "cont",      // cont/corte/pi-/pi/pi+/pi++/perf
    val nh: Int = 10,
    val alcanceMetros: Int = 1
)

data class BestiarioCriatura(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val st: Int = 10,
    val dx: Int = 10,
    val iq: Int = 8,
    val ht: Int = 10,
    val pv: Int = st,
    val rd: Int = 0,
    val velocidadeBasica: Double = (dx + ht) / 4.0,
    val deslocamento: Int = ((dx + ht) / 4.0).toInt(),
    val agressividade: Int = 5,     // 0-10
    val moral: Int = 5,             // 0-10
    val ataques: List<AtaqueCriatura> = emptyList()
) {
    /** Maior alcance entre os ataques (define se a criatura é "de distância"). */
    val alcanceMaximo: Int get() = ataques.maxOfOrNull { it.alcanceMetros } ?: 1

    /** Converte a criatura num Combatente pronto para o encontro (B1). */
    fun novoCombatente(id: String, nome: String = this.nome): Combatente {
        val principal = ataques.firstOrNull()
        val stats = NpcStats(
            st = st, dx = dx, iq = iq, ht = ht, pvMax = pv, rd = rd,
            velocidadeBasica = velocidadeBasica, deslocamento = deslocamento,
            armaNome = principal?.nome ?: "", armaDano = principal?.dano ?: "",
            armaTipo = principal?.tipo ?: "", armaNh = principal?.nh ?: 10,
            alcanceMetros = alcanceMaximo,
            agressividade = agressividade, moral = moral
        )
        return Combatente(
            id = id, nome = nome, ehHeroi = false, dx = dx,
            velocidadeBasica = velocidadeBasica, deslocamento = deslocamento,
            pvMax = pv, pvAtual = pv, pfAtual = ht, stats = stats
        )
    }
}

data class Bestiario(val versao: Int = 1, val criaturas: List<BestiarioCriatura> = emptyList()) {
    // Busca direta (sem cache): o Gson não roda o init da data class, então um mapa
    // cacheado no construtor ficaria vazio após a desserialização.
    fun get(id: String): BestiarioCriatura? = criaturas.firstOrNull { it.id == id }
}

/** Loader no padrão dos demais catálogos (parse de String; o asset é lido pela camada Android). */
object BestiarioLoader {
    fun parse(json: String): Bestiario =
        try { Gson().fromJson(json, Bestiario::class.java) ?: Bestiario() } catch (e: Exception) { Bestiario() }
}
