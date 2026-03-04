package nexus.arcano

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusArcanoEngineLote1Test {

    @Test
    fun desejo_respeita_cadeia_obrigatoria_antes_de_escolas() {
        val catalogo = NexusArcanoTestCatalog.base()
        val engine = NexusArcanoEngine(catalogo)
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar", "magia_terra", "magia_agua", "magia_fogo"),
            am = 3,
            iq = 14,
            dx = 12
        )

        val r = engine.calcularEstadoAlvo("desejo", estado)

        assertTrue(r.chavesFaltantes.any { it.id == "chave_pequeno_desejo" })
        assertTrue(r.chavesFaltantes.any { it.id == "chave_alvo_desejo" })
        assertFalse(r.proximasAcoes.any { it.magiaId == "desejo" })
    }

    @Test
    fun desejo_superior_detecta_regra_composta_dx_iq() {
        val catalogo = NexusArcanoTestCatalog.base()
        val engine = NexusArcanoEngine(catalogo)
        val estadoBloqueado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("desejo"),
            am = 3,
            iq = 12,
            dx = 12
        )
        val estadoOk = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("desejo"),
            am = 3,
            iq = 15,
            dx = 15
        )

        val rBloqueado = engine.calcularEstadoAlvo("desejo_superior", estadoBloqueado)
        val rOk = engine.calcularEstadoAlvo("desejo_superior", estadoOk)

        assertTrue(rBloqueado.chavesFaltantes.any { it.id.contains("chave_soma_desejo_superior") })
        assertTrue(rOk.chavesAtivas.any { it.id.contains("chave_soma_desejo_superior") })
    }

    @Test
    fun teleporte_exige_iq_e_escolas() {
        val catalogo = NexusArcanoTestCatalog.base()
        val engine = NexusArcanoEngine(catalogo)
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("voo_do_falcao"),
            am = 2,
            iq = 12,
            dx = 11
        )

        val r = engine.calcularEstadoAlvo("teleporte", estado)

        assertTrue(r.chavesFaltantes.any { it.id.startsWith("chave_iq_teleporte_13") })
        assertTrue(r.motivoCodigo == "NUMERIC_GATE" || r.motivoCodigo == null)
    }

    @Test
    fun convocar_demonio_exige_am1_e_dez_escolas() {
        val catalogo = NexusArcanoTestCatalog.base()
        val engine = NexusArcanoEngine(catalogo)
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar"),
            am = 0,
            iq = 12,
            dx = 10
        )

        val r = engine.calcularEstadoAlvo("convocar_demonio", estado)

        assertTrue(r.chavesFaltantes.any { it.id.startsWith("chave_am_convocar_demonio_1") })
        assertTrue(r.chavesFaltantes.any { it.id.startsWith("chave_escolas_convocar_demonio_10") })
        assertTrue(r.motivoCodigo == "NUMERIC_GATE" || r.motivoCodigo == null)
    }

    @Test
    fun translocacao_depende_de_teleporte() {
        val catalogo = NexusArcanoTestCatalog.base()
        val engine = NexusArcanoEngine(catalogo)
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("voo_do_falcao"),
            am = 3,
            iq = 13,
            dx = 11
        )

        val r = engine.calcularEstadoAlvo("translocacao", estado)
        assertTrue(r.chavesFaltantes.any { it.id == "chave_teleporte" })
        assertFalse(r.proximasAcoes.any { it.magiaId == "translocacao" })
    }
}
