package com.gurps.ficha.domain.magias

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.regras_prerequisitos.PreRequisitoChecker
import com.gurps.ficha.regras_prerequisitos.PreRequisitoParser
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.charset.StandardCharsets
import java.text.Normalizer

class MagiaTargetEngineCatalogSweepTest {

    @Test
    fun `sweep completo do modo alvo por escola nao trava`() {
        val catalogo = carregarCatalogoMagias()
        val repo = SweepFakeRepo(catalogo)
        val engine = MagiaTargetEngine(repo)
        val personagem = Personagem()

        val exceptions = mutableListOf<String>()
        val missingTargetInTrail = mutableListOf<String>()
        val suspiciousSingleNode = mutableListOf<String>()
        val parcialBySchool = mutableMapOf<String, Int>()
        val totalBySchool = mutableMapOf<String, Int>()

        catalogo.forEach { alvo ->
            val escola = alvo.escola?.firstOrNull().orEmpty().ifBlank { "Sem Escola" }
            totalBySchool[escola] = (totalBySchool[escola] ?: 0) + 1
            try {
                val result = engine.calcularModoAlvo(
                    alvo = alvo,
                    personagem = personagem,
                    contextoKey = "sweep:${alvo.id}"
                )
                if (!result.ids.contains(alvo.id)) {
                    missingTargetInTrail.add("${alvo.id} | ${alvo.nome}")
                }
                if (result.parcial) {
                    parcialBySchool[escola] = (parcialBySchool[escola] ?: 0) + 1
                }
                val temPreReq = !repo.magiaSemPreRequisito(alvo)
                if (temPreReq && result.ids.size <= 1) {
                    suspiciousSingleNode.add("${alvo.id} | ${alvo.nome} | prereq=${alvo.preRequisitos.orEmpty()}")
                }
            } catch (t: Throwable) {
                exceptions.add("${alvo.id} | ${alvo.nome} | ${t::class.java.simpleName}: ${t.message}")
            }
        }

        val relatorio = buildString {
            appendLine("CATALOGO_TOTAL=${catalogo.size}")
            appendLine("EXCEPTIONS=${exceptions.size}")
            appendLine("MISSING_TARGET_IN_TRAIL=${missingTargetInTrail.size}")
            appendLine("SUSPICIOUS_SINGLE_NODE=${suspiciousSingleNode.size}")
            appendLine()
            appendLine("PARCIAL_POR_ESCOLA")
            totalBySchool.keys.sorted().forEach { escola ->
                val total = totalBySchool[escola] ?: 0
                val parcial = parcialBySchool[escola] ?: 0
                appendLine("$escola | parcial=$parcial | total=$total")
            }
            appendLine()
            appendLine("EXCEPTIONS_LIST")
            exceptions.forEach { appendLine(it) }
            appendLine()
            appendLine("MISSING_TARGET_IN_TRAIL_LIST")
            missingTargetInTrail.forEach { appendLine(it) }
            appendLine()
            appendLine("SUSPICIOUS_SINGLE_NODE_LIST")
            suspiciousSingleNode.take(250).forEach { appendLine(it) }
        }

        val outDir = Path.of("build", "reports")
        Files.createDirectories(outDir)
        Files.write(
            outDir.resolve("modo_alvo_sweep_report.txt"),
            relatorio.toByteArray(StandardCharsets.UTF_8)
        )

        assertTrue(
            "Foram encontradas excecoes no sweep do Modo Alvo. Veja build/reports/modo_alvo_sweep_report.txt",
            exceptions.isEmpty()
        )
        assertTrue(
            "Alguns alvos nao retornaram o proprio id na trilha. Veja build/reports/modo_alvo_sweep_report.txt",
            missingTargetInTrail.isEmpty()
        )
    }

    private fun carregarCatalogoMagias(): List<MagiaDefinicao> {
        val pathCandidates = listOf(
            Path.of("src", "main", "assets", "magias2versao.json"),
            Path.of("app", "src", "main", "assets", "magias2versao.json")
        )
        val arquivo = pathCandidates.firstOrNull { Files.exists(it) }
            ?: error("Nao foi possivel localizar magias2versao.json para sweep de teste.")
        val json = String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8)
        val type = object : TypeToken<List<MagiaDefinicao>>() {}.type
        return (Gson().fromJson<List<MagiaDefinicao>>(json, type) ?: emptyList())
            .map { it.copy(nome = it.nome.fixMojibakeIfNeeded(), preRequisitos = it.preRequisitos?.fixMojibakeIfNeeded()) }
    }
}

private class SweepFakeRepo(
    override val magias: List<MagiaDefinicao>
) : MagiaPlannerDataSource {

    override fun validarPreRequisitosMagia(definicao: MagiaDefinicao, personagem: Personagem): String? {
        val raw = preRequisitoNormalizadoParaAnalise(definicao)
        if (magiaSemPreRequisito(definicao)) return null
        val parsed = PreRequisitoParser.parse(raw)
        val report = PreRequisitoChecker.checkParseResult(buildCtx(personagem), parsed)
        return if (report.startsWith("faltando")) raw else null
    }

    override fun preRequisitoNormalizadoParaAnalise(definicao: MagiaDefinicao): String {
        return definicao.preRequisitos.orEmpty().trim()
    }

    override fun magiaSemPreRequisito(definicao: MagiaDefinicao): Boolean {
        val raw = preRequisitoNormalizadoParaAnalise(definicao)
        if (raw.isBlank()) return true
        return raw in setOf("-", "—", "–", "−", "?", "??", "???")
    }

    private fun buildCtx(personagem: Personagem): Map<String, Any> {
        val magiasConhecidasNormalizadas = personagem.magias
            .map { normalizar(it.nome) }
            .filter { it.isNotBlank() }
            .toSet()
        val magiasPorEscola = mutableMapOf<String, Int>()
        personagem.magias.forEach { magia ->
            magia.escola.orEmpty().forEach { escola ->
                val key = normalizar(escola)
                magiasPorEscola[key] = (magiasPorEscola[key] ?: 0) + 1
            }
        }
        return mapOf(
            "aptidao_magica" to 0,
            "magias_conhecidas_normalizadas" to magiasConhecidasNormalizadas,
            "magias_por_escola_normalizada" to magiasPorEscola,
            "escolas_conhecidas_normalizadas" to magiasPorEscola.keys.toSet(),
            "escolas_por_magia_normalizadas" to emptyMap<String, Set<String>>(),
            "vantagens_conhecidas_normalizadas" to emptySet<String>(),
            "pericias_conhecidas_normalizadas" to emptySet<String>(),
            "condicoes_estado_normalizadas" to emptySet<String>()
        )
    }

    private fun normalizar(valor: String): String {
        val semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return semAcento
            .lowercase()
            .replace("-", " ")
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

private fun String.fixMojibakeIfNeeded(): String {
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
