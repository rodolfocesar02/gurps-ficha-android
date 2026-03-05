package com.gurps.ficha.domain.magias

import com.gurps.ficha.model.MagiaDefinicao
import nexus.arcano.ArcanoCatalogo
import nexus.arcano.ArcanoChave
import nexus.arcano.ArcanoEstadoPersonagem
import nexus.arcano.ArcanoResultado
import nexus.arcano.NexusArcanoEngine

data class NexusArcanoModoAlvoSnapshot(
    val relacionadosIds: List<String>,
    val chavesAtivas: List<ArcanoChave>,
    val chavesFaltantes: List<ArcanoChave>,
    val proximasAcoesIds: List<String>,
    val aviso: String? = null
)

class NexusArcanoModoAlvoAdapter(
    magiasCatalogo: List<MagiaDefinicao>
) {
    private val magiasById: Map<String, MagiaDefinicao> = magiasCatalogo.associateBy { it.id }

    private val engine = NexusArcanoEngine(
        object : ArcanoCatalogo {
            override fun preRequisitoRaw(magiaId: String): String =
                magiasById[magiaId]?.preRequisitos.orEmpty()

            override fun escolas(magiaId: String): List<String> =
                magiasById[magiaId]?.escola.orEmpty()

            override fun nome(magiaId: String): String =
                magiasById[magiaId]?.nome.orEmpty()

            override fun existe(magiaId: String): Boolean =
                magiasById.containsKey(magiaId)

            override fun todasMagiasIds(): List<String> =
                magiasById.keys.toList()
        }
    )

    fun calcular(
        alvoId: String,
        magiasConhecidasIds: Set<String>,
        iq: Int,
        dx: Int,
        am: Int
    ): NexusArcanoModoAlvoSnapshot {
        if (!magiasById.containsKey(alvoId)) {
            return NexusArcanoModoAlvoSnapshot(
                relacionadosIds = emptyList(),
                chavesAtivas = emptyList(),
                chavesFaltantes = emptyList(),
                proximasAcoesIds = emptyList(),
                aviso = "Alvo não encontrado no catálogo."
            )
        }

        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = magiasConhecidasIds,
            am = am,
            iq = iq,
            dx = dx
        )

        val resultado = engine.calcularEstadoAlvo(alvoId, estado)
        val idsAcoesMotor = resultado.proximasAcoes.asSequence()
            .map { it.magiaId }
            .filter { magiasById.containsKey(it) }
            .distinct()
            .take(3)
            .toList()
        val idsAcoes = idsAcoesMotor.toMutableList()

        val relacionados = linkedSetOf<String>()
        relacionados.add(alvoId)
        idsAcoes.forEach { relacionados.add(it) }

        val rankingElegivel = engine.diagnosticarRankingAlvo(alvoId, estado)
            .asSequence()
            .filter { it.elegivel }
            .map { it.magiaId }
            .filter { magiasById.containsKey(it) }
            .filter { it != alvoId }
            .distinct()
            .toList()

        rankingElegivel.forEach { candId ->
            if (idsAcoes.size < 3 && candId !in idsAcoes) {
                idsAcoes.add(candId)
            }
            if (relacionados.size < 7) {
                relacionados.add(candId)
            }
        }

        return NexusArcanoModoAlvoSnapshot(
            relacionadosIds = relacionados.toList(),
            chavesAtivas = resultado.chavesAtivas,
            chavesFaltantes = resultado.chavesFaltantes,
            proximasAcoesIds = idsAcoes,
            aviso = montarAviso(resultado)
        )
    }

    fun falhaPreRequisitoHierarquica(
        alvoId: String,
        magiasConhecidasIds: Set<String>,
        iq: Int,
        dx: Int,
        am: Int
    ): String? {
        if (!magiasById.containsKey(alvoId)) return "Alvo não encontrado no catálogo."

        val resultado = engine.calcularEstadoAlvo(
            alvoId = alvoId,
            estado = ArcanoEstadoPersonagem(
                magiasConhecidasIds = magiasConhecidasIds,
                am = am,
                iq = iq,
                dx = dx
            )
        )

        val chaveAlvoId = "chave_alvo_$alvoId"
        val alvoLiberado = resultado.chavesAtivas.any { it.id == chaveAlvoId }
        if (alvoLiberado) return null

        val faltas = resultado.chavesFaltantes
            .asSequence()
            .filter { it.id != chaveAlvoId }
            .map { it.descricao.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(3)
            .toList()

        if (faltas.isNotEmpty()) return faltas.joinToString(" | ")
        return resultado.motivoBloqueio?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun montarAviso(resultado: ArcanoResultado): String? {
        val faltantes = resultado.chavesFaltantes.take(3)
            .joinToString(" | ") { it.descricao }
            .trim()
        return when {
            !resultado.motivoBloqueio.isNullOrBlank() -> resultado.motivoBloqueio
            faltantes.isNotBlank() -> "Chaves pendentes: $faltantes"
            else -> null
        }
    }
}
