package com.gurps.ficha.domain.magias

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.MagiaDefinicao
import nexus.arcano.ArcanoCatalogo
import nexus.arcano.ArcanoEstadoPersonagem
import nexus.arcano.ArcanoMetaProgress
import nexus.arcano.NexusArcanoEngine
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import nexus.arcano.diagnosticarMetasAlvo
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class NexusArcanoLoteFCanonicScenarioTest {

    @Test
    fun cenario_canonico_desejo_am3_iq15_seguindo_apenas_recomendadas_libera_alvo() {
        val catalogo = carregarCatalogoMagiasV2Normalizado()
        val byId = catalogo.associateBy { it.id }
        val adapter = NexusArcanoModoAlvoAdapter(catalogo)
        val known = linkedSetOf<String>()
        val trilha = mutableListOf<String>()
        val rodadas = mutableListOf<String>()

        val alvoId = "desejo"
        val am = 3
        val iq = 15
        val dx = 12
        val maxRodadas = 60

        for (rodada in 1..maxRodadas) {
            val falhaAlvo = adapter.falhaPreRequisitoHierarquica(
                alvoId = alvoId,
                magiasConhecidasIds = known,
                iq = iq,
                dx = dx,
                am = am
            )
            if (falhaAlvo == null) break

            val snapshot = adapter.calcular(
                alvoId = alvoId,
                magiasConhecidasIds = known,
                iq = iq,
                dx = dx,
                am = am
            )
            val recomendada = snapshot.proximasAcoesIds.firstOrNull { it !in known }
            assertNotNull("Sem recomendacao na rodada $rodada para alvo '$alvoId'.", recomendada)
            val recomendadaId = recomendada!!

            val falhaRecomendada = adapter.falhaPreRequisitoHierarquica(
                alvoId = recomendadaId,
                magiasConhecidasIds = known,
                iq = iq,
                dx = dx,
                am = am
            )
            assertNull(
                "Recomendacao '$recomendadaId' nao esta aprendivel na rodada $rodada.",
                falhaRecomendada
            )

            known += recomendadaId
            trilha += recomendadaId

            val nomeAcao = byId[recomendadaId]?.nome ?: recomendadaId
            rodadas += buildString {
                append("R$rodada")
                append("|acaoId=$recomendadaId")
                append("|acaoNome=$nomeAcao")
                append("|obrigatoria=${snapshot.proximaObrigatoriaId.orEmpty().ifBlank { "-" }}")
                append("|lateral=${snapshot.proximaLateralUtilId.orEmpty().ifBlank { "-" }}")
                append("|bloqueio=${snapshot.bloqueioCurto.orEmpty().ifBlank { "-" }}")
            }
        }

        val falhaFinal = adapter.falhaPreRequisitoHierarquica(
            alvoId = alvoId,
            magiasConhecidasIds = known,
            iq = iq,
            dx = dx,
            am = am
        )

        salvarRelatorio(
            nome = "nexus_arcano_lote_f_cenario_canonico_desejo.txt",
            conteudo = buildString {
                appendLine("TESTE=cenario_canonico_desejo_am3_iq15")
                appendLine("ALVO=$alvoId")
                appendLine("AM=$am")
                appendLine("IQ=$iq")
                appendLine("DX=$dx")
                appendLine("SUCESSO=${falhaFinal == null}")
                appendLine("PASSOS=${trilha.size}")
                appendLine("FALHA_FINAL=${falhaFinal ?: "-"}")
                appendLine("TRILHA_IDS=${trilha.joinToString(" -> ")}")
                appendLine("TRILHA_NOMES=${trilha.map { byId[it]?.nome ?: it }.joinToString(" -> ")}")
                appendLine("RODADAS")
                rodadas.forEach { appendLine(it) }
            }
        )

        assertTrue("Trilha vazia para o cenario canonico.", trilha.isNotEmpty())
        assertNull(
            "Desejo nao foi liberado seguindo apenas recomendacoes em ate $maxRodadas rodadas.",
            falhaFinal
        )
    }

    @Test
    fun cenario_canonico_desejo_cada_acao_recomendada_reduz_meta_pendente() {
        val catalogo = carregarCatalogoMagiasV2Normalizado()
        val adapter = NexusArcanoModoAlvoAdapter(catalogo)
        val engine = criarEngine(catalogo)
        val known = linkedSetOf<String>()
        val alvoId = "desejo"
        val am = 3
        val iq = 15
        val dx = 12
        val maxRodadas = 60
        val linhas = mutableListOf<String>()

        for (rodada in 1..maxRodadas) {
            val estadoAntes = ArcanoEstadoPersonagem(
                magiasConhecidasIds = known,
                am = am,
                iq = iq,
                dx = dx
            )
            val falhaAlvo = adapter.falhaPreRequisitoHierarquica(
                alvoId = alvoId,
                magiasConhecidasIds = known,
                iq = iq,
                dx = dx,
                am = am
            )
            if (falhaAlvo == null) break

            val metasAntes = engine.diagnosticarMetasAlvo(alvoId, estadoAntes)
            val snapshot = adapter.calcular(
                alvoId = alvoId,
                magiasConhecidasIds = known,
                iq = iq,
                dx = dx,
                am = am
            )
            val recomendada = snapshot.proximasAcoesIds.firstOrNull { it !in known }
            assertNotNull("Sem recomendacao na rodada $rodada para alvo '$alvoId'.", recomendada)
            val recomendadaId = recomendada!!

            val falhaRecomendada = adapter.falhaPreRequisitoHierarquica(
                alvoId = recomendadaId,
                magiasConhecidasIds = known,
                iq = iq,
                dx = dx,
                am = am
            )
            assertNull(
                "Recomendacao '$recomendadaId' nao esta aprendivel na rodada $rodada.",
                falhaRecomendada
            )

            known += recomendadaId
            val estadoDepois = ArcanoEstadoPersonagem(
                magiasConhecidasIds = known,
                am = am,
                iq = iq,
                dx = dx
            )
            val metasDepois = engine.diagnosticarMetasAlvo(alvoId, estadoDepois)
            val melhorou = houveMelhoraMetaPendente(metasAntes, metasDepois)
            linhas += "R$rodada|acao=$recomendadaId|meta_reduzida=$melhorou"
            assertTrue(
                "A acao '$recomendadaId' na rodada $rodada nao reduziu nenhuma meta pendente.",
                melhorou
            )
        }

        val falhaFinal = adapter.falhaPreRequisitoHierarquica(
            alvoId = alvoId,
            magiasConhecidasIds = known,
            iq = iq,
            dx = dx,
            am = am
        )

        salvarRelatorio(
            nome = "nexus_arcano_lote_f_metas_reduzidas_por_rodada.txt",
            conteudo = buildString {
                appendLine("TESTE=cenario_canonico_desejo_metas_reduzidas")
                appendLine("ALVO=$alvoId")
                appendLine("SUCESSO=${falhaFinal == null}")
                appendLine("PASSOS=${known.size}")
                appendLine("FALHA_FINAL=${falhaFinal ?: "-"}")
                appendLine("RODADAS")
                linhas.forEach { appendLine(it) }
            }
        )

        assertNull(
            "Desejo nao foi liberado apos validar reducao de metas por rodada.",
            falhaFinal
        )
    }

    @Test
    fun cenario_canonico_desejo_snapshot_explica_cadeia_contadores_e_proximas_acoes() {
        val catalogo = carregarCatalogoMagiasV2Normalizado()
        val byId = catalogo.associateBy { it.id }
        val adapter = NexusArcanoModoAlvoAdapter(catalogo)
        val known = linkedSetOf<String>()
        val alvoId = "desejo"
        val am = 3
        val iq = 15
        val dx = 12
        val maxRodadas = 8
        val linhas = mutableListOf<String>()

        for (rodada in 1..maxRodadas) {
            val falhaAlvo = adapter.falhaPreRequisitoHierarquica(
                alvoId = alvoId,
                magiasConhecidasIds = known,
                iq = iq,
                dx = dx,
                am = am
            )
            if (falhaAlvo == null) break

            val snapshot = adapter.calcular(
                alvoId = alvoId,
                magiasConhecidasIds = known,
                iq = iq,
                dx = dx,
                am = am
            )

            assertTrue(
                "Rodada $rodada sem progresso de cadeia no snapshot.",
                !snapshot.progressoCadeia.isNullOrBlank()
            )
            assertTrue(
                "Rodada $rodada sem contadores de escolas no snapshot.",
                snapshot.progressoEscolas.isNotEmpty()
            )
            assertTrue(
                "Rodada $rodada sem proxima obrigatoria no snapshot.",
                !snapshot.proximaObrigatoriaId.isNullOrBlank()
            )
            assertTrue(
                "Rodada $rodada sem proxima lateral util no snapshot.",
                !snapshot.proximaLateralUtilId.isNullOrBlank()
            )

            linhas += buildString {
                append("R$rodada")
                append("|cadeia=${snapshot.progressoCadeia.orEmpty()}")
                append("|escolas=${snapshot.progressoEscolas.joinToString(" || ")}")
                append("|obrigatoria=${snapshot.proximaObrigatoriaId.orEmpty().ifBlank { "-" }}")
                append("|lateral=${snapshot.proximaLateralUtilId.orEmpty().ifBlank { "-" }}")
                append("|bloqueio=${snapshot.bloqueioCurto.orEmpty().ifBlank { "-" }}")
            }

            val recomendada = snapshot.proximasAcoesIds.firstOrNull { it !in known } ?: break
            known += recomendada
            linhas += "R$rodada|acao=$recomendada|acao_nome=${byId[recomendada]?.nome ?: recomendada}"
        }

        salvarRelatorio(
            nome = "nexus_arcano_lote_f_ux_snapshot_desejo.txt",
            conteudo = buildString {
                appendLine("TESTE=cenario_canonico_desejo_snapshot_ux")
                appendLine("ALVO=$alvoId")
                appendLine("RODADAS_AVALIADAS=${linhas.count { it.startsWith("R") }}")
                appendLine("LINHAS")
                linhas.forEach { appendLine(it) }
            }
        )

        assertTrue("Nao houve linhas de diagnostico de UX no snapshot.", linhas.isNotEmpty())
    }

    private fun carregarCatalogoMagiasV2Normalizado(): List<MagiaDefinicao> {
        val pathCandidates = listOf(
            Path.of("src", "main", "assets", "magias2versao.json"),
            Path.of("app", "src", "main", "assets", "magias2versao.json")
        )
        val arquivo = pathCandidates.firstOrNull { Files.exists(it) }
            ?: error("Nao foi possivel localizar magias2versao.json para o cenario canonico.")
        val json = String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8)
        val type = object : TypeToken<List<MagiaDefinicao>>() {}.type
        val lista = Gson().fromJson<List<MagiaDefinicao>>(json, type) ?: emptyList()
        return lista
            .asSequence()
            .filter { it.id.isNotBlank() }
            .map { magia ->
                magia.copy(
                    nome = magia.nome.fixMojibakeIfNeededLoteF(),
                    classe = magia.classe?.fixMojibakeIfNeededLoteF(),
                    escola = magia.escola?.map { it.fixMojibakeIfNeededLoteF() },
                    preRequisitos = magia.preRequisitos?.fixMojibakeIfNeededLoteF()
                )
            }
            .toList()
    }

    private fun salvarRelatorio(nome: String, conteudo: String) {
        val outDir = Path.of("build", "reports")
        Files.createDirectories(outDir)
        Files.write(outDir.resolve(nome), conteudo.toByteArray(StandardCharsets.UTF_8))
    }

    private fun criarEngine(catalogo: List<MagiaDefinicao>): NexusArcanoEngine {
        val byId = catalogo.associateBy { it.id }
        return NexusArcanoEngine(
            object : ArcanoCatalogo {
                override fun preRequisitoRaw(magiaId: String): String =
                    byId[magiaId]?.preRequisitos.orEmpty()

                override fun escolas(magiaId: String): List<String> =
                    byId[magiaId]?.escola.orEmpty()

                override fun nome(magiaId: String): String =
                    byId[magiaId]?.nome.orEmpty()

                override fun existe(magiaId: String): Boolean =
                    byId.containsKey(magiaId)

                override fun todasMagiasIds(): List<String> =
                    byId.keys.sorted()
            }
        )
    }

    private fun houveMelhoraMetaPendente(
        metasAntes: List<ArcanoMetaProgress>,
        metasDepois: List<ArcanoMetaProgress>
    ): Boolean {
        val depoisPorId = metasDepois.associateBy { it.id }
        return metasAntes
            .asSequence()
            .filter { !it.atendida }
            .any { antes ->
                val depois = depoisPorId[antes.id] ?: return@any true
                (depois.atendida && !antes.atendida) ||
                    (depois.atual > antes.atual) ||
                    (antes.bloqueadaPorUpstream && !depois.bloqueadaPorUpstream) ||
                    (depois.requerido < antes.requerido)
            }
    }
}

private fun String.fixMojibakeIfNeededLoteF(): String {
    val markers = listOf("Ã", "Â", "â", "�")
    var current = this
    repeat(2) {
        if (!markers.any { current.contains(it) }) return current
        val repaired = runCatching {
            String(current.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
        }.getOrElse { current }
        if (repaired == current) return@repeat
        current = repaired
    }
    return current
}
