import nexus.arcano.*
import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Minimal catalog implementation for testing with real JSON
class RealJsonCatalogo(val magias: List<Map<String, Any>>) : ArcanoCatalogo {
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

fun main() {
    val file = File("app/src/main/assets/magias2versao.json")
    val json = file.readText()
    val type = object : TypeToken<List<Map<String, Any>>>() {}.type
    val magias: List<Map<String, Any>> = Gson().fromJson(json, type)
    
    val catalogo = RealJsonCatalogo(magias)
    val engine = NexusArcanoEngine(catalogo)
    
    val estado = ArcanoEstadoPersonagem(
        magiasConhecidasIds = emptySet(),
        am = 1,
        iq = 14
    )
    
    println("--- PLANEJANDO DESEJO ---")
    val plano = engine.planejarCaminhoMinimo("desejo", estado)
    println("Tamanho do Caminho: ${plano.trilhaMagiasIds.size}")
    println("Trilha: ${plano.trilhaMagiasIds.joinToString(", ")}")
    
    plano.trilhaMagiasIds.forEach { id ->
        println("$id: ${engine.escolaPrincipalNorm(id)}")
    }
}
