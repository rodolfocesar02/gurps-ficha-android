package nexus.arcano

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun custo_do_plano_evitar_escola_repetida_quando_meta_escolas_pendente() {
        val engine = NexusArcanoEngine(catalogoPasso2Escolas())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("base_ar"),
            am = 3,
            iq = 12,
            dx = 10
        )

        val plano = engine.planejarCaminhoMinimo("alvo_escolas", estado)

        assertTrue(plano.trilhaMagiasIds.isNotEmpty())
        assertFalse(plano.trilhaMagiasIds.first() == "repete_ar")
    }

    @Test
    fun custo_do_plano_penaliza_forte_acao_sem_reducao_de_meta_pendente() {
        val engine = NexusArcanoEngine(catalogoPasso2SemReducao())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = emptySet(),
            am = 3,
            iq = 12,
            dx = 10
        )

        val plano = engine.planejarCaminhoMinimo("alvo_deps", estado)

        assertTrue(plano.trilhaMagiasIds.isNotEmpty())
        assertFalse(plano.trilhaMagiasIds.first() == "aaa_lateral")
        assertTrue(plano.trilhaMagiasIds.first() in setOf("dep_a", "dep_b"))
    }

    @Test
    fun expansao_global_considera_apenas_magias_aprendiveis_agora() {
        val engine = NexusArcanoEngine(catalogoPasso3AprendivelAgora())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = emptySet(),
            am = 3,
            iq = 12,
            dx = 10
        )

        val plano = engine.planejarCaminhoMinimo("alvo_ou", estado)

        assertTrue(plano.trilhaMagiasIds.isNotEmpty())
        assertTrue(plano.trilhaMagiasIds.first() == "dep_livre")
        assertFalse(plano.trilhaMagiasIds.contains("dep_bloqueada"))
    }

    @Test
    fun contrato_de_saida_do_plano_retorna_proxima_trilha_e_metas_impactadas() {
        val engine = NexusArcanoEngine(catalogoPasso2SemReducao())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = emptySet(),
            am = 3,
            iq = 12,
            dx = 10
        )

        val plano = engine.planejarCaminhoMinimo("alvo_deps", estado)

        assertTrue(plano.trilhaMagiasIds.size >= 2)
        assertEquals(plano.trilhaMagiasIds.first(), plano.proximaAcaoMagiaId)
        assertTrue(plano.metasImpactadasProximaAcao.isNotEmpty())
    }

    private fun catalogoPasso2Escolas(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo_escolas", "Alvo Escolas", listOf("Meta"), "1 magica em 3 escolas"),
            M("base_ar", "Base Ar", listOf("Ar"), ""),
            M("repete_ar", "Repete Ar", listOf("Ar"), ""),
            M("abre_terra", "Abre Terra", listOf("Terra"), ""),
            M("abre_agua", "Abre Agua", listOf("Agua"), "")
        )
        val byId = magias.associateBy { it.id }
        return object : ArcanoCatalogo {
            override fun preRequisitoRaw(magiaId: String): String = byId[magiaId]?.pre.orEmpty()
            override fun escolas(magiaId: String): List<String> = byId[magiaId]?.escolas.orEmpty()
            override fun nome(magiaId: String): String = byId[magiaId]?.nome ?: magiaId
            override fun existe(magiaId: String): Boolean = byId.containsKey(magiaId)
            override fun todasMagiasIds(): List<String> = byId.keys.sorted()
        }
    }

    private fun catalogoPasso2SemReducao(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo_deps", "Alvo Deps", listOf("Meta"), "dep_a, dep_b"),
            M("dep_a", "Dep A", listOf("Ar"), ""),
            M("dep_b", "Dep B", listOf("Terra"), ""),
            M("aaa_lateral", "AAA Lateral", listOf("Agua"), "")
        )
        val byId = magias.associateBy { it.id }
        return object : ArcanoCatalogo {
            override fun preRequisitoRaw(magiaId: String): String = byId[magiaId]?.pre.orEmpty()
            override fun escolas(magiaId: String): List<String> = byId[magiaId]?.escolas.orEmpty()
            override fun nome(magiaId: String): String = byId[magiaId]?.nome ?: magiaId
            override fun existe(magiaId: String): Boolean = byId.containsKey(magiaId)
            override fun todasMagiasIds(): List<String> = byId.keys.sorted()
        }
    }

    private fun catalogoPasso3AprendivelAgora(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo_ou", "Alvo OU", listOf("Meta"), "dep_bloqueada ou dep_livre"),
            M("dep_livre", "Dep Livre", listOf("Ar"), ""),
            M("dep_bloqueada", "Dep Bloqueada", listOf("Terra"), "IQ 14+")
        )
        val byId = magias.associateBy { it.id }
        return object : ArcanoCatalogo {
            override fun preRequisitoRaw(magiaId: String): String = byId[magiaId]?.pre.orEmpty()
            override fun escolas(magiaId: String): List<String> = byId[magiaId]?.escolas.orEmpty()
            override fun nome(magiaId: String): String = byId[magiaId]?.nome ?: magiaId
            override fun existe(magiaId: String): Boolean = byId.containsKey(magiaId)
            override fun todasMagiasIds(): List<String> = byId.keys.sorted()
        }
    }
}
