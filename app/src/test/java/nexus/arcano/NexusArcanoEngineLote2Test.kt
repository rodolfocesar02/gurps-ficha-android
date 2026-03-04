package nexus.arcano

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusArcanoEngineLote2Test {

    @Test
    fun planejador_limita_em_tres_acoes_unicas() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar"),
            am = 3,
            iq = 14,
            dx = 12
        )

        val r = engine.calcularEstadoAlvo("encantar", estado)

        assertTrue(r.proximasAcoes.size <= 3)
        assertEquals(r.proximasAcoes.map { it.magiaId }.toSet().size, r.proximasAcoes.size)
    }

    @Test
    fun planejador_e_deterministico_para_mesmo_estado() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar"),
            am = 3,
            iq = 14,
            dx = 12
        )

        val r1 = engine.calcularEstadoAlvo("encantar", estado)
        val r2 = engine.calcularEstadoAlvo("encantar", estado)

        assertEquals(r1.proximasAcoes.map { it.magiaId }, r2.proximasAcoes.map { it.magiaId })
        assertEquals(r1.proximasAcoes.map { it.prioridade }, r2.proximasAcoes.map { it.prioridade })
    }

    @Test
    fun planejador_nao_sugere_alvo_ou_cadeia_como_preenchimento_lateral() {
        val engine = NexusArcanoEngine(NexusArcanoTestCatalog.base())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar"),
            am = 3,
            iq = 14,
            dx = 12
        )

        val r = engine.calcularEstadoAlvo("desejo", estado)
        val sugeridas = r.proximasAcoes.map { it.magiaId }.toSet()

        assertTrue("desejo" !in sugeridas)
        assertTrue("pequeno_desejo" !in sugeridas)
        assertTrue("encantar" !in sugeridas)
    }

    @Test
    fun fallback_final_completa_tres_acoes_quando_so_ha_escolas_repetidas() {
        val engine = NexusArcanoEngine(catalogoComPoucasEscolas())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("base_ar"),
            am = 3,
            iq = 14,
            dx = 12
        )

        val r = engine.calcularEstadoAlvo("alvo", estado)

        assertEquals(3, r.proximasAcoes.size)
        assertEquals(3, r.proximasAcoes.map { it.magiaId }.toSet().size)
    }

    @Test
    fun empate_de_custo_mantem_ordem_deterministica_por_nome() {
        val engine = NexusArcanoEngine(catalogoEmpate())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = emptySet(),
            am = 3,
            iq = 14,
            dx = 12
        )

        val r = engine.calcularEstadoAlvo("alvo", estado)
        val primeiros = r.proximasAcoes.take(2).map { it.magiaId }

        assertEquals(listOf("cand_agua", "cand_terra"), primeiros)
    }

    private fun catalogoComPoucasEscolas(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo", "Alvo", listOf("Meta"), "1 magica em 5 outras escolas"),
            M("base_ar", "Base Ar", listOf("Ar"), ""),
            M("cand_ar_1", "Cand Ar 1", listOf("Ar"), ""),
            M("cand_ar_2", "Cand Ar 2", listOf("Ar"), ""),
            M("cand_agua_1", "Cand Agua 1", listOf("Agua"), "")
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

    private fun catalogoEmpate(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo", "Alvo", listOf("Meta"), "1 magica em 5 outras escolas"),
            M("cand_agua", "Aguia", listOf("Agua"), ""),
            M("cand_terra", "Bardo", listOf("Terra"), ""),
            M("cand_ar", "Canto", listOf("Ar"), "")
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
