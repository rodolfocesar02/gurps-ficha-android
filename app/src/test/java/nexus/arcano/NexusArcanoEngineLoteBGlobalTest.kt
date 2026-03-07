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
    fun alternativa_com_virgula_preserva_dependencia_compartilhada() {
        val engine = NexusArcanoEngine(catalogoPasso4DependenciaCompartilhada())

        val estadoSoOpcional = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("opcao_b"),
            am = 3,
            iq = 12,
            dx = 10
        )
        val resultadoSoOpcional = engine.calcularEstadoAlvo("alvo_composto", estadoSoOpcional)

        assertTrue(resultadoSoOpcional.chavesFaltantes.any { it.id == "chave_base_obrigatoria" })
        assertTrue(resultadoSoOpcional.proximasAcoes.any { it.magiaId == "base_obrigatoria" })
        assertFalse(resultadoSoOpcional.chavesAtivas.any { it.id == "chave_alvo_alvo_composto" })

        val estadoComBase = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("base_obrigatoria", "opcao_b"),
            am = 3,
            iq = 12,
            dx = 10
        )
        val resultadoComBase = engine.calcularEstadoAlvo("alvo_composto", estadoComBase)
        assertTrue(resultadoComBase.chavesAtivas.any { it.id == "chave_alvo_alvo_composto" })
    }

    @Test
    fun alternativa_com_escolas_e_numerico_nao_bloqueia_ramo_nomeado() {
        val engine = NexusArcanoEngine(catalogoPasso4AlternativaMista())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("dep_livre"),
            am = 1,
            iq = 10,
            dx = 10
        )

        val resultado = engine.calcularEstadoAlvo("alvo_misto", estado)
        assertTrue(resultado.chavesAtivas.any { it.id == "chave_alvo_alvo_misto" })
        assertFalse(resultado.motivoCodigo == "NUMERIC_GATE")
        assertTrue(resultado.proximasAcoes.any { it.magiaId == "alvo_misto" })
    }

    @Test
    fun recomendacao_hard_first_prioriza_cadeia_antes_de_lateral() {
        val engine = NexusArcanoEngine(catalogoPasso5HardFirst())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = emptySet(),
            am = 3,
            iq = 12,
            dx = 10
        )

        val resultado = engine.calcularEstadoAlvo("alvo_hard", estado)
        assertTrue(resultado.proximasAcoes.isNotEmpty())
        assertEquals("dep_cadeia", resultado.proximasAcoes.first().magiaId)
        assertFalse(resultado.proximasAcoes.any { it.magiaId == "lateral_livre" })
    }

    @Test
    fun recomendacao_hard_first_prioriza_alvo_quando_cadeia_concluida() {
        val engine = NexusArcanoEngine(catalogoPasso5HardFirst())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("dep_cadeia"),
            am = 3,
            iq = 12,
            dx = 10
        )

        val resultado = engine.calcularEstadoAlvo("alvo_hard", estado)
        assertTrue(resultado.proximasAcoes.isNotEmpty())
        assertEquals("alvo_hard", resultado.proximasAcoes.first().magiaId)
        assertFalse(resultado.proximasAcoes.any { it.magiaId == "lateral_livre" })
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

    @Test
    fun repeticao_de_escola_em_sequencia_e_permitida_quando_cadeia_obrigatoria_exige() {
        val engine = NexusArcanoEngine(catalogoPasso2ExcecaoCadeia())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("magia_ar", "magia_terra"),
            am = 3,
            iq = 12,
            dx = 10
        )

        val plano = engine.planejarCaminhoMinimo("desejo_local", estado)

        assertTrue(plano.trilhaMagiasIds.size >= 2)
        assertEquals("encantar_local", plano.trilhaMagiasIds[0])
        assertEquals("pequeno_desejo_local", plano.trilhaMagiasIds[1])
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

    private fun catalogoPasso2ExcecaoCadeia(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("encantar_local", "Encantar Local", listOf("Encantamento"), "1 magica em 2 outras escolas"),
            M("pequeno_desejo_local", "Pequeno Desejo Local", listOf("Encantamento"), "Encantar Local"),
            M("desejo_local", "Desejo Local", listOf("Encantamento"), "Pequeno Desejo Local"),
            M("magia_ar", "Magia Ar", listOf("Ar"), ""),
            M("magia_terra", "Magia Terra", listOf("Terra"), ""),
            M("magia_agua_lateral", "Magia Agua Lateral", listOf("Agua"), "")
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

    private fun catalogoPasso4DependenciaCompartilhada(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo_composto", "Alvo Composto", listOf("Meta"), "Base Obrigatoria, Opcao A ou Opcao B"),
            M("base_obrigatoria", "Base Obrigatoria", listOf("Ar"), ""),
            M("opcao_a", "Opcao A", listOf("Terra"), ""),
            M("opcao_b", "Opcao B", listOf("Agua"), "")
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

    private fun catalogoPasso4AlternativaMista(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo_misto", "Alvo Misto", listOf("Meta"), "Dep Livre ou IQ 13+ e 1 magica em 3 escolas diferentes"),
            M("dep_livre", "Dep Livre", listOf("Ar"), "")
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

    private fun catalogoPasso5HardFirst(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo_hard", "Alvo Hard", listOf("Meta"), "Dep Cadeia"),
            M("dep_cadeia", "Dep Cadeia", listOf("Ar"), ""),
            M("lateral_livre", "Lateral Livre", listOf("Terra"), "")
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
