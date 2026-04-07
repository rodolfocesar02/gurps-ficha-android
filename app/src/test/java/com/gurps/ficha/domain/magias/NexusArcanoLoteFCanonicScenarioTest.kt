package com.gurps.ficha.domain.magias

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.MagiaDefinicao
import nexus.arcano.ArcanoCatalogo
import nexus.arcano.ArcanoEstadoPersonagem
import nexus.arcano.ArcanoMetaProgress
import nexus.arcano.ArcanoMetaTipo
import nexus.arcano.NexusArcanoEngine
import nexus.arcano.sugerirProximasAcoes
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

        val engine = criarEngine(catalogo)

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

            // Rodada 1 e diante devem ter progresso de cadeia
            assertTrue(
                "Rodada $rodada sem progresso de cadeia no snapshot.",
                !snapshot.progressoCadeia.isNullOrBlank()
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
            rodadas += "R$rodada|acao=$nomeAcao"
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
                appendLine("SUCESSO=${falhaFinal == null}")
                appendLine("PASSOS=${trilha.size}")
                appendLine("TRILHA=${trilha.joinToString(" -> ")}")
            }
        )

        assertTrue("Nao conseguiu atingir o desejo em $maxRodadas rodadas", falhaFinal == null)
    }

    @Test
    fun cenario_canonico_desejo_snapshot_explica_cadeia_contadores_e_proximas_acoes() {
        val catalogo = carregarCatalogoMagiasV2Normalizado()
        val engine = criarEngine(catalogo)
        val known = mutableSetOf<String>()
        val iq = 15
        val dx = 12
        val am = 3
        val estado = ArcanoEstadoPersonagem(known, iq, dx, am)

        // Rodada 1: 0 magias
        val metas = engine.diagnosticarMetasAlvo("desejo", estado)
        
        // Deve mostrar a cadeia (Pequeno Desejo) e as escolas (Incremental: 10 escolas para Encantar)
        assertTrue("Deve mostrar meta de cadeia", metas.any { it.tipo == ArcanoMetaTipo.CADEIA_MAGIA })
        assertTrue("Deve mostrar meta incremental de 10 escolas para Encantar", 
            metas.any { it.tipo == ArcanoMetaTipo.ESCOLAS_DISTINTAS && it.requerido == 10 })
        
        // Não deve mostrar 15 escolas ainda (Incremental)
        assertTrue("Nao deve mostrar meta final de 15 escolas ainda", 
            metas.none { it.tipo == ArcanoMetaTipo.ESCOLAS_DISTINTAS && it.requerido == 15 })

        val proximas = engine.sugerirProximasAcoes("desejo", known, estado)
        assertTrue("Deve sugerir magias de escolas novas para Encantar", proximas.isNotEmpty())
    }

    private fun carregarCatalogoMagiasV2Normalizado(): List<MagiaDefinicao> {
        val pathCandidates = listOf(
            Path.of("src/main/assets/magias2versao.json"),
            Path.of("app/src/main/assets/magias2versao.json"),
            Path.of("../app/src/main/assets/magias2versao.json")
        )
        val arquivo = pathCandidates.firstOrNull { Files.exists(it) }
            ?: error("Nao foi possivel localizar magias2versao.json para o cenario canonico.")
        
        // Usar readAllBytes + String para máxima compatibilidade com charsets
        val json = String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8)
        val type = object : TypeToken<List<MagiaDefinicao>>() {}.type
        val lista = Gson().fromJson<List<MagiaDefinicao>>(json, type) ?: emptyList()
        
        return lista.map { magia ->
            magia.copy(
                nome = magia.nome.fixMojibakeIfNeededLoteF(),
                classe = magia.classe?.fixMojibakeIfNeededLoteF(),
                escola = magia.escola?.map { it.fixMojibakeIfNeededLoteF() },
                preRequisitos = magia.preRequisitos?.fixMojibakeIfNeededLoteF()
            )
        }
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
}

private fun String.fixMojibakeIfNeededLoteF(): String {
    val markers = listOf("Ã", "Â", "â", "")
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
