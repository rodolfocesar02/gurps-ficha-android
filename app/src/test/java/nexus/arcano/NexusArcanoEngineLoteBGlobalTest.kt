package nexus.arcano

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusArcanoEngineLoteBGlobalTest {

    @Test
    fun planejador_global_encontra_trilha_para_pequeno_desejo() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf(
                "magia_ar",
                "magia_terra",
                "magia_agua",
                "magia_fogo",
                "magia_corpo",
                "magia_luz",
                "magia_som",
                "magia_mente",
                "magia_necro",
                "magia_meta"
            ),
            am = 3,
            iq = 12,
            dx = 10
        )

        val plano = engine.planejarCaminhoMinimo("pequeno_desejo", estado)

        assertTrue(plano.trilhaMagiasIds.contains("encantar"))
        assertTrue(plano.trilhaMagiasIds.size <= 1)
        assertTrue(plano.trilhaMagiasIds.isNotEmpty())
        assertEquals("encantar", plano.proximaAcaoMagiaId)
        assertTrue(plano.metasImpactadasProximaAcao.contains("meta_cadeia_encantar"))
        assertTrue(plano.metasImpactadasProximaAcao.contains("meta_alvo_pequeno_desejo"))
    }

    @Test
    fun recomendacao_do_estado_usa_caminho_global_minimo() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf(
                "magia_ar",
                "magia_terra",
                "magia_agua",
                "magia_fogo",
                "magia_corpo",
                "magia_luz",
                "magia_som",
                "magia_mente",
                "magia_necro",
                "magia_meta"
            ),
            am = 3,
            iq = 12,
            dx = 10
        )

        val resultado = engine.calcularEstadoAlvo("pequeno_desejo", estado)

        assertTrue(resultado.proximasAcoes.isNotEmpty())
        assertTrue(resultado.proximasAcoes.first().magiaId == "encantar")
    }
}
