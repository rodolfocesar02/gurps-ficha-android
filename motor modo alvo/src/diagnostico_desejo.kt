import nexus.arcano.*

fun main() {
    val catalogo = ArcanoCatalogoDesejoExemplo()
    val engine = NexusArcanoEngine(catalogo)
    
    // We start from scratch: AM 2 (needed for Encantar in real GURPS, but not in this example catalog)
    // Actually, ArcanoCatalogoDesejoExemplo doesn't list AM, but let's give AM 2 just in case.
    val estado = ArcanoEstadoPersonagem(
        magiasConhecidasIds = emptySet(),
        am = 2,
        iq = 14
    )
    
    println("--- DIAGNÓSTICO DESEJO ---")
    val resultado = engine.calcularEstadoAlvo("desejo", estado)
    val plano = engine.planejarCaminhoMinimo("desejo", estado)
    
    println("Tamanho do Caminho: ${plano.trilhaMagiasIds.size}")
    println("Caminho: ${plano.trilhaMagiasIds.joinToString(" -> ")}")
    
    val metas = engine.diagnosticarMetasAlvo("desejo", estado)
    println("\nMetas Iniciais:")
    metas.forEach { m -> 
        println("- ${m.descricao}: ${m.atual}/${m.requerido} (${if(m.atendida) "OK" else "PENDENTE"})")
    }
}
