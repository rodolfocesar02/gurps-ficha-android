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

    @Test
    fun timing_stats_registra_media_p95_e_max() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar"),
            am = 3,
            iq = 14,
            dx = 12
        )

        engine.limparCache()
        repeat(6) {
            engine.calcularEstadoAlvo("desejo", estado)
            engine.diagnosticarRankingAlvo("desejo", estado)
        }
        val stats = engine.timingStats()

        assertTrue(stats.amostras >= 12)
        assertTrue(stats.mediaMs >= 0.0)
        assertTrue(stats.p95Ms >= 0.0)
        assertTrue(stats.maxMs >= stats.p95Ms)
    }

    @Test
    fun limpar_cache_zera_tambem_metricas_de_tempo() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar"),
            am = 3,
            iq = 14,
            dx = 12
        )

        engine.calcularEstadoAlvo("desejo", estado)
        assertTrue(engine.timingStats().amostras >= 1)

        engine.limparCache()
        assertEquals(0, engine.timingStats().amostras)
    }

    @Test
    fun invalidacao_incremental_remove_so_alvos_impactados() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar"),
            am = 3,
            iq = 14,
            dx = 12
        )

        engine.limparCache()
        engine.calcularEstadoAlvo("desejo", estado)
        engine.calcularEstadoAlvo("translocacao", estado)
        val antes = engine.cacheStats().entradas

        engine.invalidarCacheIncrementalPorMagia("encantar")
        val depois = engine.cacheStats().entradas

        assertTrue(antes >= 2)
        assertTrue(depois >= 1)
        assertTrue(depois < antes)
    }

    @Test
    fun alvos_impactados_inclui_cadeia_dependente() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())

        val impactados = engine.alvosImpactadosPorMagia("encantar")

        assertTrue("pequeno_desejo" in impactados)
        assertTrue("desejo" in impactados)
    }

    @Test
    fun incremental_bate_com_recalculo_completo_quando_muda_known() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estadoAntes = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar"),
            am = 3,
            iq = 14,
            dx = 12
        )
        val estadoDepois = estadoAntes.copy(
            magiasConhecidasIds = estadoAntes.magiasConhecidasIds + "encantar"
        )
        val anterior = engine.calcularEstadoAlvo("desejo", estadoAntes)
        val inc = engine.calcularEstadoAlvoIncremental("desejo", estadoAntes, anterior, estadoDepois)
        val full = engine.calcularEstadoAlvo("desejo", estadoDepois)

        assertEquals("INCREMENTAL_KNOWN_ONLY", inc.modo)
        assertTrue(inc.chavesRecalculadas > 0)
        assertEquals(full.chavesAtivas.map { it.id }.toSet(), inc.resultado.chavesAtivas.map { it.id }.toSet())
        assertEquals(full.chavesFaltantes.map { it.id }.toSet(), inc.resultado.chavesFaltantes.map { it.id }.toSet())
        assertEquals(full.proximasAcoes.map { it.magiaId }, inc.resultado.proximasAcoes.map { it.magiaId })
    }

    @Test
    fun incremental_trata_mudanca_de_atributo_sem_fallback_full() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estadoAntes = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar"),
            am = 3,
            iq = 12,
            dx = 12
        )
        val estadoDepois = estadoAntes.copy(iq = 14)
        val anterior = engine.calcularEstadoAlvo("teleporte", estadoAntes)
        val full = engine.calcularEstadoAlvo("teleporte", estadoDepois)

        val inc = engine.calcularEstadoAlvoIncremental("teleporte", estadoAntes, anterior, estadoDepois)

        assertEquals("INCREMENTAL_ATTR_ONLY", inc.modo)
        assertTrue(inc.chavesRecalculadas > 0)
        assertEquals(full.chavesAtivas.map { it.id }.toSet(), inc.resultado.chavesAtivas.map { it.id }.toSet())
        assertEquals(full.chavesFaltantes.map { it.id }.toSet(), inc.resultado.chavesFaltantes.map { it.id }.toSet())
    }
}
