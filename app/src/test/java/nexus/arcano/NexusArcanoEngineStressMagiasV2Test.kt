package nexus.arcano

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.text.Normalizer

class NexusArcanoEngineStressMagiasV2Test {

    @Test
    fun stress_ramificacoes_longas_com_magias_v2() {
        val catalogo = carregarCatalogoMagiasV2()
        val engine = NexusArcanoEngine(catalogo)
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = emptySet(),
            am = 3,
            iq = 15,
            dx = 12
        )

        val candidatosLongos = catalogo.todasMagiasIds()
            .map { id -> id to catalogo.preRequisitoRaw(id) }
            .filter { (_, pre) -> pre.length >= 25 && ("," in pre || " ou " in pre.lowercase()) }
            .sortedByDescending { it.second.length }
            .take(40)
            .map { it.first }

        val latenciasMs = mutableListOf<Double>()
        val inconsistencias = mutableListOf<String>()
        val excecoes = mutableListOf<String>()

        candidatosLongos.forEach { alvoId ->
            repeat(15) {
                try {
                    val t0 = System.nanoTime()
                    val r = engine.calcularEstadoAlvo(alvoId, estado)
                    val d = engine.diagnosticarRankingAlvo(alvoId, estado)
                    val dtMs = (System.nanoTime() - t0) / 1_000_000.0
                    latenciasMs += dtMs

                    val chaveAlvoEsperada = "chave_alvo_$alvoId"
                    if (r.chavesAtivas.none { it.id == chaveAlvoEsperada } &&
                        r.chavesFaltantes.none { it.id == chaveAlvoEsperada }
                    ) {
                        inconsistencias += "$alvoId sem chave de alvo."
                    }
                    if (r.proximasAcoes.size > 3) {
                        inconsistencias += "$alvoId retornou mais de 3 proximas acoes."
                    }
                    if (r.proximasAcoes.map { it.magiaId }.toSet().size != r.proximasAcoes.size) {
                        inconsistencias += "$alvoId retornou acoes duplicadas."
                    }
                    if (d.any { !it.elegivel && it.motivoExclusao == null }) {
                        inconsistencias += "$alvoId diagnostico com exclusao sem motivo."
                    }
                } catch (t: Throwable) {
                    excecoes += "$alvoId | ${t::class.java.simpleName}: ${t.message}"
                }
            }
        }

        val p50 = percentile(latenciasMs, 50.0)
        val p95 = percentile(latenciasMs, 95.0)
        val p99 = percentile(latenciasMs, 99.0)
        val max = latenciasMs.maxOrNull() ?: 0.0

        val relatorio = buildString {
            appendLine("TESTE=stress_ramificacoes_longas_com_magias_v2")
            appendLine("ALVOS_TESTADOS=${candidatosLongos.size}")
            appendLine("AMOSTRAS_LATENCIA=${latenciasMs.size}")
            appendLine("P50_MS=${"%.3f".format(p50)}")
            appendLine("P95_MS=${"%.3f".format(p95)}")
            appendLine("P99_MS=${"%.3f".format(p99)}")
            appendLine("MAX_MS=${"%.3f".format(max)}")
            appendLine("INCONSISTENCIAS=${inconsistencias.size}")
            appendLine("EXCECOES=${excecoes.size}")
            appendLine()
            appendLine("INCONSISTENCIAS_LIST")
            inconsistencias.take(300).forEach { appendLine(it) }
            appendLine()
            appendLine("EXCECOES_LIST")
            excecoes.take(300).forEach { appendLine(it) }
        }

        salvarRelatorio("nexus_arcano_magiasv2_stress_ramificacoes.txt", relatorio)

        assertTrue(
            "Foram encontradas excecoes no stress de ramificacoes. Veja build/reports/nexus_arcano_magiasv2_stress_ramificacoes.txt",
            excecoes.isEmpty()
        )
        assertTrue(
            "Foram encontradas inconsistencias no stress de ramificacoes. Veja build/reports/nexus_arcano_magiasv2_stress_ramificacoes.txt",
            inconsistencias.isEmpty()
        )
        assertTrue("Relatorio de stress nao gerou latencias.", latenciasMs.isNotEmpty())
    }

    @Test
    fun sweep_escola_encantamento_consistencia_e_tempo_magias_v2() {
        val catalogo = carregarCatalogoMagiasV2()
        val engine = NexusArcanoEngine(catalogo)
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = emptySet(),
            am = 3,
            iq = 15,
            dx = 12
        )

        val escolaNorm = "encantamento"
        val alvosEscola = catalogo.todasMagiasIds()
            .filter { id -> catalogo.escolas(id).map(::norm).any { it == escolaNorm } }
            .sortedBy { norm(catalogo.nome(it)) }

        val latenciasMs = mutableListOf<Double>()
        val inconsistencias = mutableListOf<String>()
        val excecoes = mutableListOf<String>()

        alvosEscola.forEach { alvoId ->
            try {
                val t0 = System.nanoTime()
                val r1 = engine.calcularEstadoAlvo(alvoId, estado)
                val r2 = engine.calcularEstadoAlvo(alvoId, estado)
                val dtMs = (System.nanoTime() - t0) / 1_000_000.0
                latenciasMs += dtMs

                val a1 = r1.proximasAcoes.map { it.magiaId to it.prioridade }
                val a2 = r2.proximasAcoes.map { it.magiaId to it.prioridade }
                if (a1 != a2) {
                    inconsistencias += "$alvoId nao deterministico entre chamadas consecutivas."
                }
                if (r1.proximasAcoes.size > 3) {
                    inconsistencias += "$alvoId retornou mais de 3 proximas acoes."
                }
                if (r1.proximasAcoes.map { it.magiaId }.toSet().size != r1.proximasAcoes.size) {
                    inconsistencias += "$alvoId retornou acoes duplicadas."
                }
            } catch (t: Throwable) {
                excecoes += "$alvoId | ${t::class.java.simpleName}: ${t.message}"
            }
        }

        val p50 = percentile(latenciasMs, 50.0)
        val p95 = percentile(latenciasMs, 95.0)
        val max = latenciasMs.maxOrNull() ?: 0.0

        val relatorio = buildString {
            appendLine("TESTE=sweep_escola_encantamento_consistencia_e_tempo_magias_v2")
            appendLine("ESCOLA=Encantamento")
            appendLine("ALVOS_TESTADOS=${alvosEscola.size}")
            appendLine("P50_MS=${"%.3f".format(p50)}")
            appendLine("P95_MS=${"%.3f".format(p95)}")
            appendLine("MAX_MS=${"%.3f".format(max)}")
            appendLine("INCONSISTENCIAS=${inconsistencias.size}")
            appendLine("EXCECOES=${excecoes.size}")
            appendLine()
            appendLine("INCONSISTENCIAS_LIST")
            inconsistencias.take(300).forEach { appendLine(it) }
            appendLine()
            appendLine("EXCECOES_LIST")
            excecoes.take(300).forEach { appendLine(it) }
        }

        salvarRelatorio("nexus_arcano_magiasv2_sweep_encantamento.txt", relatorio)

        assertTrue(
            "Foram encontradas excecoes no sweep da escola Encantamento. Veja build/reports/nexus_arcano_magiasv2_sweep_encantamento.txt",
            excecoes.isEmpty()
        )
        assertTrue(
            "Foram encontradas inconsistencias no sweep da escola Encantamento. Veja build/reports/nexus_arcano_magiasv2_sweep_encantamento.txt",
            inconsistencias.isEmpty()
        )
        assertTrue("Relatorio da escola Encantamento nao gerou latencias.", latenciasMs.isNotEmpty())
    }

    private fun salvarRelatorio(nome: String, conteudo: String) {
        val outDir = Path.of("build", "reports")
        Files.createDirectories(outDir)
        Files.write(outDir.resolve(nome), conteudo.toByteArray(StandardCharsets.UTF_8))
    }

    private fun carregarCatalogoMagiasV2(): ArcanoCatalogo {
        val pathCandidates = listOf(
            Path.of("src", "main", "assets", "magias2versao.json"),
            Path.of("app", "src", "main", "assets", "magias2versao.json")
        )
        val arquivo = pathCandidates.firstOrNull { Files.exists(it) }
            ?: error("Nao foi possivel localizar magias2versao.json para os testes do Nexus Arcano.")
        val json = String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8)
        val type = object : TypeToken<List<MagiaV2>>() {}.type
        val lista = (Gson().fromJson<List<MagiaV2>>(json, type) ?: emptyList())
            .filter { it.id.isNotBlank() }
            .map {
                it.copy(
                    nome = it.nome.fixMojibakeIfNeeded(),
                    escola = it.escola.map { e -> e.fixMojibakeIfNeeded() },
                    preRequisitos = it.preRequisitos.fixMojibakeIfNeeded()
                )
            }
        val byId = lista.associateBy { it.id }

        return object : ArcanoCatalogo {
            override fun preRequisitoRaw(magiaId: String): String = byId[magiaId]?.preRequisitos.orEmpty()
            override fun escolas(magiaId: String): List<String> = byId[magiaId]?.escola.orEmpty()
            override fun nome(magiaId: String): String = byId[magiaId]?.nome ?: magiaId
            override fun existe(magiaId: String): Boolean = byId.containsKey(magiaId)
            override fun todasMagiasIds(): List<String> = byId.keys.sorted()
        }
    }

    private fun percentile(values: List<Double>, p: Double): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        val idx = ((p / 100.0) * (sorted.size - 1)).toInt().coerceIn(0, sorted.size - 1)
        return sorted[idx]
    }

    private fun norm(raw: String): String {
        val semAcento = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

private data class MagiaV2(
    val id: String = "",
    val nome: String = "",
    val escola: List<String> = emptyList(),
    val preRequisitos: String = ""
)

private fun String.fixMojibakeIfNeeded(): String {
    val markers = listOf("Ãƒ", "Ã‚", "Ã¢", "ï¿½")
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
