import nexus.arcano.*

fun main() {
    val catalogo = ArcanoCatalogoDesejoExemplo() // Or use a real catalog if possible
    val engine = NexusArcanoEngine(catalogo)
    
    val ids = listOf("encantar", "desejo")
    ids.forEach { id ->
        println("--- Regras para $id ---")
        val raw = engine.preRaw(id)
        println("Raw: $raw")
        val norm = engine.preNorm(id)
        println("Norm: $norm")
        
        val regrasE = engine.regrasEscolasPorMagia(id)
        regrasE.forEach { r ->
            println("Regra Escola: ${r.quantidadeEscolas} escolas (outras=${r.outrasEscolas})")
        }
        
        val regrasN = engine.regrasNumericasPorMagia(id)
        regrasN.forEach { r ->
            println("Regra Numérica: AM=${r.minAm}, IQ=${r.minIq}, Attr=${r.somaAtributos} Min=${r.minSoma}")
        }
    }
}
