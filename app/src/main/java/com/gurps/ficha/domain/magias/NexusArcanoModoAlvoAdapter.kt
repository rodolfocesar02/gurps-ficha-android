package com.gurps.ficha.domain.magias

import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.Personagem
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

    fun calcular(alvoId: String, personagem: Personagem, am: Int): NexusArcanoModoAlvoSnapshot {
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
            magiasConhecidasIds = personagem.magias.asSequence().map { it.definicaoId }.toSet(),
            am = am,
            iq = personagem.inteligencia,
            dx = personagem.destreza
        )

        val resultado = engine.calcularEstadoAlvo(alvoId, estado)
        val idsAcoes = resultado.proximasAcoes.asSequence()
            .map { it.magiaId }
            .filter { magiasById.containsKey(it) }
            .distinct()
            .take(3)
            .toList()

        val relacionados = linkedSetOf<String>()
        relacionados.add(alvoId)
        idsAcoes.forEach { relacionados.add(it) }

        if (idsAcoes.isEmpty()) {
            engine.diagnosticarRankingAlvo(alvoId, estado)
                .asSequence()
                .filter { it.elegivel }
                .map { it.magiaId }
                .filter { magiasById.containsKey(it) }
                .distinct()
                .take(3)
                .forEach { relacionados.add(it) }
        }

        return NexusArcanoModoAlvoSnapshot(
            relacionadosIds = relacionados.toList(),
            chavesAtivas = resultado.chavesAtivas,
            chavesFaltantes = resultado.chavesFaltantes,
            proximasAcoesIds = idsAcoes,
            aviso = montarAviso(resultado)
        )
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
