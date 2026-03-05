package nexus.arcano

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusArcanoEngineLoteAGlobalTest {

    @Test
    fun metas_globais_de_desejo_expoem_cadeia_escolas_e_alvo() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar", "magia_terra", "magia_agua"),
            am = 3,
            iq = 15,
            dx = 12
        )

        val metas = engine.diagnosticarMetasAlvo("desejo", estado)

        assertTrue(metas.any { it.id == "meta_cadeia_encantar" && !it.atendida })
        assertTrue(metas.any { it.id == "meta_cadeia_pequeno_desejo" && !it.atendida })
        assertTrue(metas.any { it.id.startsWith("meta_escolas_encantar_10_1") })
        assertTrue(metas.any { it.id.startsWith("meta_escolas_desejo_15_0") })
        assertTrue(metas.any { it.id == "meta_alvo_desejo" && !it.atendida })
    }

    @Test
    fun metas_globais_de_desejo_superior_expoem_regras_numericas() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("desejo"),
            am = 2,
            iq = 12,
            dx = 12
        )

        val metas = engine.diagnosticarMetasAlvo("desejo_superior", estado)

        assertTrue(metas.any { it.id.startsWith("meta_am_desejo_superior_3") && !it.atendida })
        assertTrue(metas.any { it.id.startsWith("meta_soma_desejo_superior_dx_iq_30") && !it.atendida })
        assertTrue(metas.any { it.id == "meta_alvo_desejo_superior" && !it.atendida })
    }

    @Test
    fun metas_globais_funcionam_para_outro_alvo_que_nao_desejo() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("voo_do_falcao"),
            am = 2,
            iq = 12,
            dx = 11
        )

        val metas = engine.diagnosticarMetasAlvo("translocacao", estado)

        assertTrue(metas.any { it.id == "meta_cadeia_teleporte" })
        assertTrue(metas.any { it.id.startsWith("meta_iq_teleporte_13") })
        assertTrue(metas.any { it.id == "meta_alvo_translocacao" && !it.atendida })
        assertFalse(metas.isEmpty())
    }

    @Test
    fun checksum_de_metas_e_deterministico_e_sensivel_a_estado() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estadoBase = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar", "magia_terra", "magia_agua"),
            am = 3,
            iq = 15,
            dx = 12
        )
        val estadoComProgresso = estadoBase.copy(magiasConhecidasIds = estadoBase.magiasConhecidasIds + "encantar")

        val checksum1 = engine.checksumMetasAlvo("desejo", estadoBase)
        val checksum2 = engine.checksumMetasAlvo("desejo", estadoBase)
        val checksum3 = engine.checksumMetasAlvo("desejo", estadoComProgresso)

        assertTrue(checksum1.startsWith("v1:"))
        assertEquals(checksum1, checksum2)
        assertNotEquals(checksum1, checksum3)
    }
}
