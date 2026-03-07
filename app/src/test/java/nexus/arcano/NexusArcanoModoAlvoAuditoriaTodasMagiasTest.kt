package nexus.arcano

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class NexusArcanoModoAlvoAuditoriaTodasMagiasTest {
    private val maxPassosGreedyAuditoria = 45
    private val maxEstadosBfsAuditoria = 10_000
    private val maxVizinhosBfsAuditoria = 24

    data class Perfil(
        val nome: String,
        val am: Int,
        val iq: Int,
        val dx: Int
    )

    private data class GreedyResultado(
        val sucesso: Boolean,
        val passos: Int,
        val status: String,
        val trilha: List<String>
    )

    @Test
    fun auditoria_modo_alvo_todas_magias_com_nexus_arcano() {
        val catalogo = carregarCatalogoMagiasV2()
        val engine = NexusArcanoEngine(catalogo)
        val ids = catalogo.todasMagiasIds()

        val perfil = Perfil(
            nome = "am3_iq15_dx12",
            am = 3,
            iq = 15,
            dx = 12
        )

        val relatorioLinhas = mutableListOf<String>()
        relatorioLinhas += "TESTE=auditoria_modo_alvo_todas_magias_com_nexus_arcano"
        relatorioLinhas += "PERFIL=${perfil.nome}"
        relatorioLinhas += "TOTAL_MAGIAS=${ids.size}"
        relatorioLinhas += "FORMATO=magiaId|status|passos|bfsStatus|bfsMenorOuIgual|trilha"
        relatorioLinhas += "LIMITES=maxPassosGreedy=$maxPassosGreedyAuditoria,maxEstadosBfs=$maxEstadosBfsAuditoria,maxVizinhosBfs=$maxVizinhosBfsAuditoria"
        relatorioLinhas += ""

        var totalSucesso = 0
        var totalFalha = 0
        var totalBloqueioNumerico = 0
        var totalComDesvioMenorCaminho = 0
        var totalBfsChecado = 0

        val learnablesCache = mutableMapOf<String, List<String>>()

        ids.forEach { alvoId ->
            val greedy = simularGreedy(
                engine = engine,
                todosIds = ids,
                alvoId = alvoId,
                perfil = perfil,
                maxPassos = maxPassosGreedyAuditoria
            )

            when {
                greedy.status == "BLOQUEIO_NUMERICO" -> totalBloqueioNumerico += 1
                greedy.sucesso -> totalSucesso += 1
                else -> totalFalha += 1
            }

            var bfsStatus = "NAO_CHECADO"
            var bfsMenorOuIgual = "-"

            if (greedy.sucesso && greedy.passos in 2..6) {
                totalBfsChecado += 1
                val limite = greedy.passos - 1
                val bfs = buscarMenorCaminhoLimitado(
                    engine = engine,
                    todosIds = ids,
                    alvoId = alvoId,
                    perfil = perfil,
                    profundidadeMax = limite,
                    maxEstadosVisitados = maxEstadosBfsAuditoria,
                    maxVizinhosPorEstado = maxVizinhosBfsAuditoria,
                    learnablesCache = learnablesCache
                )
                bfsStatus = bfs.first
                val bfsPassos = bfs.second
                bfsMenorOuIgual = bfsPassos?.toString() ?: "-"
                if (bfsStatus == "ENCONTRADO" && bfsPassos != null && bfsPassos < greedy.passos) {
                    totalComDesvioMenorCaminho += 1
                }
            }

            relatorioLinhas += listOf(
                alvoId,
                greedy.status,
                greedy.passos.toString(),
                bfsStatus,
                bfsMenorOuIgual,
                greedy.trilha.joinToString(" -> ")
            ).joinToString("|")
        }

        relatorioLinhas += ""
        relatorioLinhas += "RESUMO"
        relatorioLinhas += "SUCESSO=$totalSucesso"
        relatorioLinhas += "FALHA=$totalFalha"
        relatorioLinhas += "BLOQUEIO_NUMERICO=$totalBloqueioNumerico"
        relatorioLinhas += "BFS_CHECADO=$totalBfsChecado"
        relatorioLinhas += "DESVIO_MENOR_CAMINHO_ENCONTRADO=$totalComDesvioMenorCaminho"

        salvarRelatorio(
            nome = "nexus_arcano_modo_alvo_auditoria_todas_magias.txt",
            conteudo = relatorioLinhas.joinToString("\n")
        )

        assertTrue("Auditoria não gerou linhas.", relatorioLinhas.size > 10)
    }

    private fun simularGreedy(
        engine: NexusArcanoEngine,
        todosIds: List<String>,
        alvoId: String,
        perfil: Perfil,
        maxPassos: Int
    ): GreedyResultado {
        val known = linkedSetOf<String>()
        val trilha = mutableListOf<String>()
        val visitStates = mutableSetOf<String>()

        repeat(maxPassos + 1) { passo ->
            val estado = ArcanoEstadoPersonagem(
                magiasConhecidasIds = known,
                am = perfil.am,
                iq = perfil.iq,
                dx = perfil.dx
            )
            if (alvoAprendivel(engine, alvoId, estado) || alvoId in known) {
                return GreedyResultado(
                    sucesso = true,
                    passos = passo,
                    status = "OK",
                    trilha = trilha
                )
            }

            val assinatura = assinaturaKnown(known)
            if (!visitStates.add(assinatura)) {
                return GreedyResultado(
                    sucesso = false,
                    passos = passo,
                    status = "LOOP_ESTADO_REPETIDO",
                    trilha = trilha
                )
            }

            val resultado = engine.calcularEstadoAlvo(alvoId, estado)
            val acao = resultado.proximasAcoes.firstOrNull { it.magiaId !in known }
                ?: return GreedyResultado(
                    sucesso = resultado.motivoCodigo == "NUMERIC_GATE",
                    passos = passo,
                    status = when {
                        resultado.motivoCodigo == "NUMERIC_GATE" -> "BLOQUEIO_NUMERICO"
                        alvoId in todosIds -> "SEM_RECOMENDACAO"
                        else -> "ALVO_INVALIDO"
                    },
                    trilha = trilha
                )

            val estadoAntes = ArcanoEstadoPersonagem(
                magiasConhecidasIds = known,
                am = perfil.am,
                iq = perfil.iq,
                dx = perfil.dx
            )
            if (!alvoAprendivel(engine, acao.magiaId, estadoAntes)) {
                return GreedyResultado(
                    sucesso = false,
                    passos = passo,
                    status = "RECOMENDACAO_NAO_APRENDIVEL",
                    trilha = trilha + acao.magiaId
                )
            }

            if (!known.add(acao.magiaId)) {
                return GreedyResultado(
                    sucesso = false,
                    passos = passo,
                    status = "RECOMENDACAO_DUPLICADA",
                    trilha = trilha + acao.magiaId
                )
            }
            trilha += acao.magiaId
        }

        return GreedyResultado(
            sucesso = false,
            passos = maxPassos,
            status = "LIMITE_PASSOS_EXCEDIDO",
            trilha = trilha
        )
    }

    private fun buscarMenorCaminhoLimitado(
        engine: NexusArcanoEngine,
        todosIds: List<String>,
        alvoId: String,
        perfil: Perfil,
        profundidadeMax: Int,
        maxEstadosVisitados: Int,
        maxVizinhosPorEstado: Int,
        learnablesCache: MutableMap<String, List<String>>
    ): Pair<String, Int?> {
        data class No(val known: Set<String>, val depth: Int)

        val start = emptySet<String>()
        val visit = mutableSetOf<String>()
        val fila = ArrayDeque<No>()
        fila += No(start, 0)
        visit += assinaturaKnown(start)

        while (fila.isNotEmpty()) {
            if (visit.size > maxEstadosVisitados) return "LIMITE_ESTADOS" to null

            val atual = fila.removeFirst()
            val estadoAtual = ArcanoEstadoPersonagem(
                magiasConhecidasIds = atual.known,
                am = perfil.am,
                iq = perfil.iq,
                dx = perfil.dx
            )
            if (alvoAprendivel(engine, alvoId, estadoAtual) || alvoId in atual.known) {
                return "ENCONTRADO" to atual.depth
            }
            if (atual.depth >= profundidadeMax) continue

            val sig = assinaturaKnown(atual.known)
            val vizinhos = learnablesCache.getOrPut(sig) {
                todosIds
                    .asSequence()
                    .filter { it !in atual.known }
                    .filter { cand -> alvoAprendivel(engine, cand, estadoAtual) }
                    .sorted()
                    .take(maxVizinhosPorEstado)
                    .toList()
            }

            vizinhos.forEach { prox ->
                val novo = (atual.known + prox)
                val sigNovo = assinaturaKnown(novo)
                if (visit.add(sigNovo)) {
                    fila += No(novo, atual.depth + 1)
                }
            }
        }

        return "NAO_ENCONTRADO" to null
    }

    private fun alvoAprendivel(
        engine: NexusArcanoEngine,
        alvoId: String,
        estado: ArcanoEstadoPersonagem
    ): Boolean {
        val r = engine.calcularEstadoAlvo(alvoId, estado)
        return r.chavesAtivas.any { it.id == "chave_alvo_$alvoId" }
    }

    private fun assinaturaKnown(known: Set<String>): String {
        return known.sorted().joinToString("|", prefix = "|", postfix = "|")
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
            ?: error("Nao foi possivel localizar magias2versao.json para auditoria do Nexus Arcano.")
        val json = String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8)
        val type = object : TypeToken<List<MagiaV2Audit>>() {}.type
        val lista = (Gson().fromJson<List<MagiaV2Audit>>(json, type) ?: emptyList())
            .filter { it.id.isNotBlank() }
            .map {
                it.copy(
                    nome = it.nome.fixMojibakeIfNeededAudit(),
                    escola = it.escola.map { e -> e.fixMojibakeIfNeededAudit() },
                    preRequisitos = it.preRequisitos.fixMojibakeIfNeededAudit()
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
}

private data class MagiaV2Audit(
    val id: String = "",
    val nome: String = "",
    val escola: List<String> = emptyList(),
    val preRequisitos: String = ""
)

private fun String.fixMojibakeIfNeededAudit(): String {
    val markers = listOf("ÃƒÆ’", "Ãƒâ€š", "ÃƒÂ¢", "Ã¯Â¿Â½")
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
