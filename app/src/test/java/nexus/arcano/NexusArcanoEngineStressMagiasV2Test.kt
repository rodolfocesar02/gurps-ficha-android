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
                    // Lote 351: teto atual do motor é 5 (take(5) em sugerirProximasAcoes:
                    // até 3 imediatas da cadeia + lookahead de escolas).
                    if (r.proximasAcoes.size > 5) {
                        inconsistencias += "$alvoId retornou mais de 5 proximas acoes."
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
                // Lote 351: teto atual do motor é 5 (take(5) em sugerirProximasAcoes).
                if (r1.proximasAcoes.size > 5) {
                    inconsistencias += "$alvoId retornou mais de 5 proximas acoes."
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

    @Test
    fun comparativo_delta_incremental_vs_full_por_rodada_magias_v2() {
        val catalogo = carregarCatalogoMagiasV2()
        val engineInc = NexusArcanoEngine(catalogo)
        val engineFull = NexusArcanoEngine(catalogo)
        val alvoId = if (catalogo.existe("desejo")) "desejo" else catalogo.todasMagiasIds().first()

        val additions = catalogo.todasMagiasIds()
            .filter { it != alvoId }
            .take(24)

        var estadoAnterior = ArcanoEstadoPersonagem(
            magiasConhecidasIds = emptySet(),
            am = 3,
            iq = 15,
            dx = 12
        )
        var resultadoAnterior = engineInc.calcularEstadoAlvo(alvoId, estadoAnterior)

        val temposIncMs = mutableListOf<Double>()
        val temposFullMs = mutableListOf<Double>()
        val inconsistencias = mutableListOf<String>()

        additions.forEach { novaMagia ->
            val estadoNovo = estadoAnterior.copy(
                magiasConhecidasIds = estadoAnterior.magiasConhecidasIds + novaMagia
            )

            val tInc = System.nanoTime()
            val delta = engineInc.calcularEstadoAlvoIncremental(
                alvoId = alvoId,
                estadoAnterior = estadoAnterior,
                resultadoAnterior = resultadoAnterior,
                estadoNovo = estadoNovo
            )
            temposIncMs += (System.nanoTime() - tInc) / 1_000_000.0

            val tFull = System.nanoTime()
            val full = engineFull.calcularEstadoAlvo(alvoId, estadoNovo)
            temposFullMs += (System.nanoTime() - tFull) / 1_000_000.0

            val aInc = delta.resultado.chavesAtivas.map { it.id }.toSet()
            val fInc = delta.resultado.chavesFaltantes.map { it.id }.toSet()
            val aFull = full.chavesAtivas.map { it.id }.toSet()
            val fFull = full.chavesFaltantes.map { it.id }.toSet()
            if (aInc != aFull || fInc != fFull || delta.resultado.proximasAcoes.map { it.magiaId } != full.proximasAcoes.map { it.magiaId }) {
                inconsistencias += "Divergencia apos adicionar $novaMagia | modo=${delta.modo}"
            }

            estadoAnterior = estadoNovo
            resultadoAnterior = delta.resultado
        }

        val p50Inc = percentile(temposIncMs, 50.0)
        val p95Inc = percentile(temposIncMs, 95.0)
        val p50Full = percentile(temposFullMs, 50.0)
        val p95Full = percentile(temposFullMs, 95.0)
        val ganhoP95 = if (p95Full > 0.0) (p95Full - p95Inc) / p95Full * 100.0 else 0.0

        val relatorio = buildString {
            appendLine("TESTE=comparativo_delta_incremental_vs_full_por_rodada_magias_v2")
            appendLine("ALVO=$alvoId")
            appendLine("RODADAS=${additions.size}")
            appendLine("DELTA_P50_MS=${"%.3f".format(p50Inc)}")
            appendLine("DELTA_P95_MS=${"%.3f".format(p95Inc)}")
            appendLine("FULL_P50_MS=${"%.3f".format(p50Full)}")
            appendLine("FULL_P95_MS=${"%.3f".format(p95Full)}")
            appendLine("GANHO_P95_PERCENT=${"%.2f".format(ganhoP95)}")
            appendLine("INCONSISTENCIAS=${inconsistencias.size}")
            appendLine()
            appendLine("INCONSISTENCIAS_LIST")
            inconsistencias.forEach { appendLine(it) }
        }
        salvarRelatorio("nexus_arcano_magiasv2_delta_vs_full.txt", relatorio)

        assertTrue("Comparativo nao gerou amostras de delta.", temposIncMs.isNotEmpty())
        assertTrue("Comparativo nao gerou amostras de full.", temposFullMs.isNotEmpty())
        assertTrue(
            "Delta incremental divergiu do full em alguma rodada. Veja build/reports/nexus_arcano_magiasv2_delta_vs_full.txt",
            inconsistencias.isEmpty()
        )
    }

    @Test
    fun telemetria_ranking_lote2_magias_v2() {
        val catalogo = carregarCatalogoMagiasV2()
        val engine = NexusArcanoEngine(catalogo)
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = emptySet(),
            am = 3,
            iq = 15,
            dx = 12
        )

        var alvosComDiagnostico = 0
        var top1EscolaNova = 0
        var top1SemPreReq = 0
        var top3SemPreReq = 0

        val amostras = mutableListOf<String>()
        catalogo.todasMagiasIds().forEach { alvoId ->
            val diag = engine.diagnosticarRankingAlvo(alvoId, estado)
            if (diag.isEmpty()) return@forEach
            val elegiveis = diag.filter { it.elegivel }
            if (elegiveis.isEmpty()) return@forEach
            alvosComDiagnostico += 1

            val top1 = elegiveis.first()
            if (top1.escolaNova) top1EscolaNova += 1
            if (preReqSemConteudo(catalogo.preRequisitoRaw(top1.magiaId))) top1SemPreReq += 1

            val top3 = elegiveis.take(3)
            if (top3.any { preReqSemConteudo(catalogo.preRequisitoRaw(it.magiaId)) }) top3SemPreReq += 1

            if (amostras.size < 40) {
                amostras += "$alvoId -> top1=${top1.magiaId} escolaNova=${top1.escolaNova} custo=${top1.custo} pre='${catalogo.preRequisitoRaw(top1.magiaId)}'"
            }
        }

        val pctTop1Nova = if (alvosComDiagnostico == 0) 0.0 else top1EscolaNova * 100.0 / alvosComDiagnostico
        val pctTop1SemPre = if (alvosComDiagnostico == 0) 0.0 else top1SemPreReq * 100.0 / alvosComDiagnostico
        val pctTop3SemPre = if (alvosComDiagnostico == 0) 0.0 else top3SemPreReq * 100.0 / alvosComDiagnostico

        val relatorio = buildString {
            appendLine("TESTE=telemetria_ranking_lote2_magias_v2")
            appendLine("ALVOS_COM_DIAGNOSTICO=$alvosComDiagnostico")
            appendLine("TOP1_ESCOLA_NOVA_PERCENT=${"%.2f".format(pctTop1Nova)}")
            appendLine("TOP1_SEM_PREREQ_PERCENT=${"%.2f".format(pctTop1SemPre)}")
            appendLine("TOP3_TEM_SEM_PREREQ_PERCENT=${"%.2f".format(pctTop3SemPre)}")
            appendLine()
            appendLine("AMOSTRAS")
            amostras.forEach { appendLine(it) }
        }
        salvarRelatorio("nexus_arcano_magiasv2_telemetria_ranking_lote2.txt", relatorio)

        assertTrue("Telemetria de ranking sem alvos com diagnóstico.", alvosComDiagnostico > 0)
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

    private fun preReqSemConteudo(raw: String): Boolean {
        val t = raw.trim().lowercase()
        if (t.isBlank()) return true
        return t in setOf("-", "—", "–", "−", "?", "??", "???", "â€”", "â€“", "âˆ’")
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
