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

    @Test
    fun regra_escolas_aceita_numero_por_palavra_dez() {
        val catalogo = object : ArcanoCatalogo {
            private val dados = mapOf(
                "encantar" to Triple("Encantar", listOf("Encantamento"), "1 magia em dez outras escolas"),
                "m1" to Triple("M1", listOf("Ar"), "-"),
                "m2" to Triple("M2", listOf("Terra"), "-"),
                "m3" to Triple("M3", listOf("Agua"), "-"),
                "m4" to Triple("M4", listOf("Fogo"), "-"),
                "m5" to Triple("M5", listOf("Luz"), "-"),
                "m6" to Triple("M6", listOf("Som"), "-"),
                "m7" to Triple("M7", listOf("Corpo"), "-"),
                "m8" to Triple("M8", listOf("Mente"), "-"),
                "m9" to Triple("M9", listOf("Portais"), "-")
            )
            override fun preRequisitoRaw(magiaId: String): String = dados[magiaId]?.third.orEmpty()
            override fun escolas(magiaId: String): List<String> = dados[magiaId]?.second.orEmpty()
            override fun nome(magiaId: String): String = dados[magiaId]?.first.orEmpty()
            override fun existe(magiaId: String): Boolean = dados.containsKey(magiaId)
            override fun todasMagiasIds(): List<String> = dados.keys.toList()
        }
        val engine = NexusArcanoEngine(catalogo)
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8", "m9"),
            am = 0,
            iq = 10
        )

        val r = engine.calcularEstadoAlvo("encantar", estado)
        assertTrue(r.chavesFaltantes.any { it.id == "chave_escolas_encantar_10" })
        assertFalse(r.chavesAtivas.any { it.id == "chave_escolas_encantar_10" })
    }

    @Test
    fun dependencia_nomeada_reconhece_singular_plural() {
        val catalogo = object : ArcanoCatalogo {
            private val dados = mapOf(
                "curar_planta" to Triple("Curar Planta", listOf("Plantas"), "-"),
                "crescimento_de_plantas" to Triple("Crescimento de Plantas", listOf("Plantas"), "Curar Plantas")
            )
            override fun preRequisitoRaw(magiaId: String): String = dados[magiaId]?.third.orEmpty()
            override fun escolas(magiaId: String): List<String> = dados[magiaId]?.second.orEmpty()
            override fun nome(magiaId: String): String = dados[magiaId]?.first.orEmpty()
            override fun existe(magiaId: String): Boolean = dados.containsKey(magiaId)
            override fun todasMagiasIds(): List<String> = dados.keys.toList()
        }
        val engine = NexusArcanoEngine(catalogo)
        val estadoBloqueado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = emptySet(),
            am = 0,
            iq = 10
        )
        val estadoLiberado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("curar_planta"),
            am = 0,
            iq = 10
        )

        val rBloqueado = engine.calcularEstadoAlvo("crescimento_de_plantas", estadoBloqueado)
        val rLiberado = engine.calcularEstadoAlvo("crescimento_de_plantas", estadoLiberado)

        assertTrue(rBloqueado.chavesFaltantes.any { it.id == "chave_curar_planta" })
        assertTrue(rLiberado.chavesAtivas.any { it.id == "chave_curar_planta" })
    }
}
