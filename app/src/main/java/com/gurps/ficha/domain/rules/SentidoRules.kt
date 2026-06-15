package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem

/**
 * Lote 372: Testes de Sentidos (MB p.358, texto literal do Códex). Todo sentido é rolado contra a
 * PERCEPÇÃO, somando a vantagem de "Sentido Aguçado" correspondente e descontando desvantagens.
 * Puro/testável (sem Android). Cobre vantagens/desvantagens PESSOAIS e RACIAIS.
 *
 * IDs conferidos contra os catálogos (vantagens.v3.json / desvantagens.v2.json) — inclui o id com
 * o typo histórico `oflato_discriminatorio` (assim está no catálogo).
 */
object SentidoRules {

    enum class Sentido(val rotulo: String) {
        PERCEPCAO("Percepção"),
        VISAO("Visão"),
        AUDICAO("Audição"),
        OLFATO_PALADAR("Olfato/Paladar"),
        TATO("Tato")
    }

    /** Um componente nomeado do modificador — a "notinha" do motivo (ex.: "Visão Aguçada" +2). */
    data class Componente(val motivo: String, val valor: Int)

    data class ResultadoSentido(
        val sentido: Sentido,
        val percepcao: Int,
        val componentes: List<Componente>,
        val valorFinal: Int,
        val bloqueado: Boolean,
        val motivoBloqueio: String?
    ) {
        /** Notinha legível: "+2 Visão Aguçada, −4 Duro de Ouvido". Vazia se não há modificador. */
        fun nota(): String = componentes.joinToString(", ") {
            (if (it.valor >= 0) "+${it.valor}" else "${it.valor}") + " " + it.motivo
        }
    }

    fun avaliar(p: Personagem, sentido: Sentido): ResultadoSentido {
        val base = p.percepcao
        val comps = mutableListOf<Componente>()
        var bloqueio: String? = null

        fun nivelVant(id: String): Int =
            (p.vantagens + p.modeloRacial.vantagens).filter { it.definicaoId == id }.sumOf { it.nivel }
        fun temVant(id: String): Boolean =
            (p.vantagens + p.modeloRacial.vantagens).any { it.definicaoId == id }
        fun temDesv(id: String): Boolean =
            (p.desvantagens + p.modeloRacial.desvantagens).any { it.definicaoId == id }

        when (sentido) {
            Sentido.PERCEPCAO -> { /* teste de Percepção geral: só a Per */ }
            Sentido.VISAO -> {
                nivelVant("visao_agucada").let { if (it != 0) comps.add(Componente("Visão Aguçada", it)) }
                if (temVant("visao_hiperespectral")) comps.add(Componente("Visão Hiperespectral", 3))
                if (temDesv("disopia")) comps.add(Componente("Disopia (condicional)", -6))
                if (temDesv("cegueira")) bloqueio = "Cego"
            }
            Sentido.AUDICAO -> {
                nivelVant("audicao_agucada").let { if (it != 0) comps.add(Componente("Audição Aguçada", it)) }
                if (temDesv("duro_de_ouvido")) comps.add(Componente("Duro de Ouvido", -4))
                if (temDesv("surdez")) bloqueio = "Surdo"
            }
            Sentido.OLFATO_PALADAR -> {
                nivelVant("paladar_olfato_apurado").let { if (it != 0) comps.add(Componente("Olfato/Paladar Apurado", it)) }
                if (temVant("oflato_discriminatorio") || temVant("paladar_discriminatorio"))
                    comps.add(Componente("Discriminatório", 4))
                if (temDesv("disosmia")) bloqueio = "Sem olfato/paladar"
            }
            Sentido.TATO -> {
                nivelVant("tato_apurado").let { if (it != 0) comps.add(Componente("Tato Apurado", it)) }
            }
        }
        return ResultadoSentido(
            sentido = sentido,
            percepcao = base,
            componentes = comps,
            valorFinal = base + comps.sumOf { it.valor },
            bloqueado = bloqueio != null,
            motivoBloqueio = bloqueio
        )
    }

    fun todos(p: Personagem): List<ResultadoSentido> = Sentido.values().map { avaliar(p, it) }
}
