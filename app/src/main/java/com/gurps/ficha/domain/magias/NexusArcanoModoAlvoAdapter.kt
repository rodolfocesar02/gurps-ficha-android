package com.gurps.ficha.domain.magias

import com.gurps.ficha.model.MagiaDefinicao
import nexus.arcano.ArcanoCatalogo
import nexus.arcano.ArcanoChave
import nexus.arcano.ArcanoEstadoPersonagem
import nexus.arcano.ArcanoMetaProgress
import nexus.arcano.ArcanoMetaTipo
import nexus.arcano.ArcanoResultado
import nexus.arcano.NexusArcanoEngine
import nexus.arcano.diagnosticarMetasAlvo
import nexus.arcano.diagnosticarRankingAlvo

data class NexusArcanoModoAlvoSnapshot(
    val relacionadosIds: List<String>,
    val chavesAtivas: List<ArcanoChave>,
    val chavesFaltantes: List<ArcanoChave>,
    val proximasAcoesIds: List<String>,
    val progressoCadeia: String? = null,
    val progressoEscolas: List<String> = emptyList(),
    val proximaObrigatoriaId: String? = null,
    val proximaLateralUtilId: String? = null,
    val bloqueioCurto: String? = null,
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
                progressoCadeia = null,
                progressoEscolas = emptyList(),
                proximaObrigatoriaId = null,
                proximaLateralUtilId = null,
                bloqueioCurto = null,
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
        val metas = engine.diagnosticarMetasAlvo(alvoId, estado)
        val idsAcoesMotor = resultado.proximasAcoes.asSequence()
            .map { it.magiaId }
            .filter { magiasById.containsKey(it) }
            .distinct()
            .take(5)
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

        val cadeiasMetas = metas.filter { it.tipo == ArcanoMetaTipo.CADEIA_MAGIA }.map { it.origemMagiaId }
        cadeiasMetas.forEach { relacionados.add(it) }

        rankingElegivel.forEach { candId ->
            if (idsAcoes.size < 5 && candId !in idsAcoes) {
                idsAcoes.add(candId)
            }
            if (relacionados.size < 20) {
                relacionados.add(candId)
            }
        }
        val proximaObrigatoria = extrairPrimeiraCadeiaPendenteId(resultado)
            ?.takeIf { magiasById.containsKey(it) }
        val proximaLateralUtil = idsAcoes.firstOrNull { it != proximaObrigatoria } ?: idsAcoes.firstOrNull()

        return NexusArcanoModoAlvoSnapshot(
            relacionadosIds = relacionados.toList(),
            chavesAtivas = resultado.chavesAtivas,
            chavesFaltantes = resultado.chavesFaltantes,
            proximasAcoesIds = idsAcoes,
            progressoCadeia = montarProgressoCadeia(alvoId, metas),
            progressoEscolas = montarProgressoEscolas(metas),
            proximaObrigatoriaId = proximaObrigatoria,
            proximaLateralUtilId = proximaLateralUtil,
            bloqueioCurto = montarBloqueioCurto(resultado),
            aviso = mensagemFalhaHierarquica(alvoId, resultado)
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
        return mensagemFalhaHierarquica(alvoId, resultado)
    }

    private fun mensagemFalhaHierarquica(alvoId: String, resultado: ArcanoResultado): String? {
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

    private fun montarProgressoCadeia(alvoId: String, metas: List<ArcanoMetaProgress>): String? {
        val cadeiaIds = metas
            .filter { it.tipo == ArcanoMetaTipo.CADEIA_MAGIA }
            .map { it.origemMagiaId }
            .distinct()
        if (cadeiaIds.isEmpty()) return null
        val nomes = (cadeiaIds + alvoId)
            .distinct()
            .map { id -> magiasById[id]?.nome ?: id }
        return "Cadeia: ${nomes.joinToString(" -> ")}"
    }

    private fun montarProgressoEscolas(metas: List<ArcanoMetaProgress>): List<String> {
        return metas
            .filter { it.tipo == ArcanoMetaTipo.ESCOLAS_DISTINTAS }
            .map { meta -> "${meta.descricao}: ${meta.atual}/${meta.requerido}" }
    }

    private fun extrairPrimeiraCadeiaPendenteId(resultado: ArcanoResultado): String? {
        return resultado.chavesFaltantes
            .asSequence()
            .map { it.id }
            .firstOrNull { id ->
                id.startsWith("chave_") &&
                    !id.startsWith("chave_escolas_") &&
                    !id.startsWith("chave_am_") &&
                    !id.startsWith("chave_iq_") &&
                    !id.startsWith("chave_soma_") &&
                    !id.startsWith("chave_alvo_")
            }?.removePrefix("chave_")
    }

    private fun montarBloqueioCurto(resultado: ArcanoResultado): String? {
        return when (resultado.motivoCodigo) {
            "NUMERIC_GATE" -> "Bloqueio: atributo ou aptidao magica insuficiente."
            "SCHOOL_COUNT_PENDING" -> "Bloqueio: contador de escolas pendente."
            "CHAIN_PENDING" -> "Bloqueio: cadeia obrigatoria pendente."
            "TARGET_PENDING" -> "Bloqueio: alvo ainda nao liberado."
            "UNKNOWN_BLOCK" -> "Bloqueio: requisitos pendentes."
            else -> null
        }
    }
}
