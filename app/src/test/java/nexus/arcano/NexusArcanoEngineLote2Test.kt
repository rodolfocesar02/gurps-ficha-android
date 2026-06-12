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
    fun sem_fallback_de_escolas_repetidas_sugere_apenas_escola_nova() {
        // Lote 351: contrato atualizado. O fallback que completava 3 ações repetindo
        // escolas foi REMOVIDO por design (NexusArcanoHeuristics, "Passo 2 (Fallback
        // Removido)") — sugerir magias de escola repetida induzia aprendizado redundante.
        // Agora só entram candidatas de escola NOVA.
        val engine = NexusArcanoEngine(catalogoComPoucasEscolas())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("base_ar"),
            am = 3,
            iq = 14,
            dx = 12
        )

        val r = engine.calcularEstadoAlvo("alvo", estado)
        val ids = r.proximasAcoes.map { it.magiaId }

        assertEquals(listOf("cand_agua_1"), ids)
        assertTrue("cand_ar_1" !in ids && "cand_ar_2" !in ids)
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

    @Test
    fun planejador_so_sugere_magia_aprendivel_agora() {
        val engine = NexusArcanoEngine(catalogoComCandidataBloqueada())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("base_ar"),
            am = 3,
            iq = 12,
            dx = 12
        )

        val r = engine.calcularEstadoAlvo("alvo", estado)
        val ids = r.proximasAcoes.map { it.magiaId }

        assertTrue("cand_livre" in ids)
        assertTrue("cand_bloqueada" !in ids)
    }

    @Test
    fun regra_outras_escolas_exclui_escola_da_magia_origem() {
        val engine = NexusArcanoEngine(catalogoOutrasEscolas())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("base_agua"),
            am = 3,
            iq = 14,
            dx = 12
        )

        val r = engine.calcularEstadoAlvo("alvo_agua", estado)
        val ids = r.proximasAcoes.map { it.magiaId }

        assertTrue("cand_agua" !in ids)
        assertTrue("cand_terra" in ids)
    }

    @Test
    fun custo_pesa_dependencias_e_prioriza_candidata_mais_barata() {
        val engine = NexusArcanoEngine(catalogoCusto())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("base_ar", "dep_x"),
            am = 3,
            iq = 14,
            dx = 12
        )

        val r = engine.calcularEstadoAlvo("alvo", estado)
        val ids = r.proximasAcoes.map { it.magiaId }

        assertTrue(ids.indexOf("cand_barata") >= 0)
        assertTrue(ids.indexOf("cand_cara") >= 0)
        assertTrue(ids.indexOf("cand_barata") < ids.indexOf("cand_cara"))
    }

    @Test
    fun diagnostico_de_ranking_mostra_motivos_de_exclusao() {
        val engine = NexusArcanoEngine(catalogoComCandidataBloqueada())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("base_ar"),
            am = 3,
            iq = 12,
            dx = 12
        )

        val diag = engine.diagnosticarRankingAlvo("alvo", estado)
        val livre = diag.firstOrNull { it.magiaId == "cand_livre" }
        val bloqueada = diag.firstOrNull { it.magiaId == "cand_bloqueada" }

        assertTrue(livre != null && livre.elegivel)
        assertTrue(livre?.motivoExclusao == null)
        assertTrue(bloqueada != null && !bloqueada.elegivel)
        assertEquals("NAO_APRENDIVEL_AGORA", bloqueada?.motivoExclusao)
    }

    @Test
    fun prioridade_de_escola_nova_enquanto_meta_de_escolas_esta_pendente() {
        val engine = NexusArcanoEngine(catalogoPriorizaEscolaNovaPendente())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("base_ar"),
            am = 3,
            iq = 14,
            dx = 12
        )

        val r = engine.calcularEstadoAlvo("alvo", estado)
        val primeira = r.proximasAcoes.firstOrNull()?.magiaId.orEmpty()

        assertTrue(primeira in setOf("cand_nova_agua", "cand_nova_terra"))
        assertTrue(primeira != "cand_repete_ar")
    }

    @Test
    fun escola_tecnologica_fica_excluida_da_recomendacao_e_marcada_no_diagnostico() {
        val engine = NexusArcanoEngine(catalogoComTecnologica())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("base_ar"),
            am = 3,
            iq = 14,
            dx = 12
        )

        val r = engine.calcularEstadoAlvo("alvo", estado)
        val ids = r.proximasAcoes.map { it.magiaId }
        assertTrue("cand_tecnologica" !in ids)

        val diag = engine.diagnosticarRankingAlvo("alvo", estado)
        val tec = diag.firstOrNull { it.magiaId == "cand_tecnologica" }
        assertTrue(tec != null)
        assertTrue(tec?.elegivel == false)
        assertEquals("ESCOLA_BLOQUEADA_POLITICA", tec?.motivoExclusao)
    }

    @Test
    fun sem_escola_nova_aprendivel_bloqueia_sem_sugestoes_e_explica() {
        // Lote 351: contrato atualizado (fallback de escolas repetidas removido por
        // design). Sem escola nova aprendível, o motor NÃO inventa sugestões: devolve
        // lista vazia e explica o bloqueio via motivoBloqueio/motivoCodigo.
        val engine = NexusArcanoEngine(catalogoSemEscolaNovaAprendivel())
        val estado = ArcanoEstadoPersonagem(
            magiasConhecidasIds = setOf("base_ar", "base_agua"),
            am = 3,
            iq = 14,
            dx = 12
        )

        val r = engine.calcularEstadoAlvo("alvo", estado)

        assertEquals(0, r.proximasAcoes.size)
        assertTrue(r.motivoBloqueio != null)
        assertEquals("SCHOOL_COUNT_PENDING", r.motivoCodigo)
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

    private fun catalogoComCandidataBloqueada(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo", "Alvo", listOf("Meta"), "1 magica em 5 outras escolas"),
            M("base_ar", "Base Ar", listOf("Ar"), ""),
            M("cand_livre", "Cand Livre", listOf("Terra"), ""),
            M("cand_bloqueada", "Cand Bloqueada", listOf("Agua"), "IQ 14+")
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

    private fun catalogoOutrasEscolas(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo_agua", "Alvo Agua", listOf("Agua"), "1 magica em 5 outras escolas"),
            M("base_agua", "Base Agua", listOf("Agua"), ""),
            M("cand_agua", "Cand Agua", listOf("Agua"), ""),
            M("cand_terra", "Cand Terra", listOf("Terra"), "")
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

    private fun catalogoCusto(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo", "Alvo", listOf("Meta"), "1 magica em 5 outras escolas"),
            M("base_ar", "Base Ar", listOf("Ar"), ""),
            M("cand_barata", "Cand Barata", listOf("Agua"), ""),
            M("cand_cara", "Cand Cara", listOf("Terra"), "dep_x"),
            M("dep_x", "Dep X", listOf("Som"), "")
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

    private fun catalogoPriorizaEscolaNovaPendente(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo", "Alvo", listOf("Meta"), "1 magica em 3 outras escolas"),
            M("base_ar", "Base Ar", listOf("Ar"), ""),
            M("cand_repete_ar", "Cand Repete Ar", listOf("Ar"), ""),
            M("cand_nova_agua", "Cand Nova Agua", listOf("Agua"), "qualquer"),
            M("cand_nova_terra", "Cand Nova Terra", listOf("Terra"), "qualquer")
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

    private fun catalogoComTecnologica(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo", "Alvo", listOf("Meta"), "1 magica em 3 outras escolas"),
            M("base_ar", "Base Ar", listOf("Ar"), ""),
            M("cand_tecnologica", "Cand Tecnologica", listOf("Tecnologica"), ""),
            M("cand_agua", "Cand Agua", listOf("Agua"), "")
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

    private fun catalogoSemEscolaNovaAprendivel(): ArcanoCatalogo {
        data class M(val id: String, val nome: String, val escolas: List<String>, val pre: String)
        val magias = listOf(
            M("alvo", "Alvo", listOf("Meta"), "1 magica em 5 outras escolas"),
            M("base_ar", "Base Ar", listOf("Ar"), ""),
            M("base_agua", "Base Agua", listOf("Agua"), ""),
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
}
