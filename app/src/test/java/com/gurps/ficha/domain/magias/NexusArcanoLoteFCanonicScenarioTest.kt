package com.gurps.ficha.domain.magias

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.MagiaDefinicao
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
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
