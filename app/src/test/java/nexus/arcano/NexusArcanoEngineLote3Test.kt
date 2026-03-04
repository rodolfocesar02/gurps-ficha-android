package nexus.arcano

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusArcanoEngineLote3Test {

    @Test
    fun cache_reusa_resultado_para_mesmo_alvo_e_estado() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar"),
            am = 3,
            iq = 14,
            dx = 12
        )

        engine.limparCache()
        engine.calcularEstadoAlvo("desejo", estado)
        val depoisPrimeira = engine.cacheStats()

        engine.calcularEstadoAlvo("desejo", estado)
        val depoisSegunda = engine.cacheStats()

        assertTrue(depoisPrimeira.misses >= 1)
        assertTrue(depoisSegunda.hits >= 1)
        assertEquals(depoisPrimeira.entradas, depoisSegunda.entradas)
    }

    @Test
    fun invalidacao_por_magia_remove_entradas_relacionadas() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar", "magia_terra"),
            am = 3,
            iq = 14,
            dx = 12
        )

        engine.limparCache()
        engine.calcularEstadoAlvo("desejo", estado)
        val antes = engine.cacheStats().entradas

        engine.invalidarCachePorMagia("magia_ar")
        val depois = engine.cacheStats().entradas

        assertTrue(antes >= 1)
        assertTrue(depois < antes)
    }
}