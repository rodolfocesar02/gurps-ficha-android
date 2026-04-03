package nexus.arcano

import org.junit.Test
import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ArcanoDeepDiagnosticTest {

    @Test
    fun diagnosticarDesejoReal() {
        val jsonPath = "src/main/assets/magias2versao.json"
        val file = File(jsonPath)
        if (!file.exists()) {
            println("ERRO: Arquivo não encontrado em ${file.absolutePath}")
            return
        }
        
        val json = file.readText()
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        val magias: List<Map<String, Any>> = Gson().fromJson(json, type)
        
        val catalogo = object : ArcanoCatalogo {
            override fun existe(id: String) = magias.any { it["id"] == id }
            override fun nome(id: String) = magias.firstOrNull { it["id"] == id }?.get("nome") as? String ?: ""
            override fun preRequisitoRaw(id: String) = magias.firstOrNull { it["id"] == id }?.get("preRequisitos") as? String ?: ""
            override fun escolas(id: String): List<String> {
                val raw = magias.firstOrNull { it["id"] == id }?.get("escola")
                return when (raw) {
                    is List<*> -> raw.mapNotNull { it as? String }
                    is String -> listOf(raw)
                    else -> emptyList()
                }
            }
            override fun todasMagiasIds() = magias.mapNotNull { it["id"] as? String }
        }

        val engine = NexusArcanoEngine(catalogo)
        val estado = ArcanoEstadoPersonagem(emptySet(), am = 3, iq = 14)
        
        println("\n=== DIAGNÓSTICO PROFUNDO: DESEJO ===")
        val id = "desejo"
        println("Prereq Raw: ${engine.preRaw(id)}")
        
        val regrasE = engine.regrasEscolasPorMagia(id)
        regrasE.forEach { println("Regra Escola: ${it.quantidadeEscolas} escolas (outras=${it.outrasEscolas})") }
        
        val depsN = engine.dependenciasNomeadas(id)
        println("Dependências Nomeadas: $depsN")
        
        val cadeia = engine.construirCadeiaObrigatoriaParaEstado(id, emptySet())
        println("Cadeia Obrigatória: $cadeia")
        
        println("\n--- SIMULANDO BUSCA A* ---")
        val pathfinder = NexusArcanoPathfinder(engine, id, estado)
        val resultado = pathfinder.procurar()
        println("Caminho A* (${resultado.size} magias):")
        resultado.forEachIndexed { index, m -> 
            println("${index + 1}. $m [${engine.escolaPrincipalNorm(m)}]")
        }
    }
}
