package com.gurps.ficha.domain.magias

import com.gurps.ficha.model.MagiaDefinicao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusArcanoModoAlvoAdapterTest {

    @Test
    fun `calcular retorna alvo e ate 3 proximas acoes`() {
        val adapter = NexusArcanoModoAlvoAdapter(catalogoBase())

        val snapshot = adapter.calcular(
            alvoId = "desejo",
            magiasConhecidasIds = emptySet(),
            iq = 15,
            dx = 10,
            am = 3
        )

        assertTrue(snapshot.relacionadosIds.firstOrNull() == "desejo")
        assertTrue(snapshot.proximasAcoesIds.size <= 3)
        assertTrue(snapshot.proximasAcoesIds.isNotEmpty())
        assertTrue(snapshot.chavesFaltantes.isNotEmpty())
        assertTrue(snapshot.progressoCadeia.orEmpty().contains("Cadeia:", ignoreCase = true))
        assertTrue(snapshot.progressoEscolas.isNotEmpty())
        assertTrue(snapshot.progressoEscolas.any { it.contains("Escolas para", ignoreCase = true) })
        assertEquals("encantar", snapshot.proximaObrigatoriaId)
        assertTrue(!snapshot.proximaLateralUtilId.isNullOrBlank())
        assertTrue(snapshot.proximaLateralUtilId != snapshot.proximaObrigatoriaId)
    }

    @Test
    fun `calcular alvo inexistente devolve aviso e vazio`() {
        val adapter = NexusArcanoModoAlvoAdapter(catalogoBase())

        val snapshot = adapter.calcular(
            alvoId = "inexistente",
            magiasConhecidasIds = emptySet(),
            iq = 10,
            dx = 10,
            am = 0
        )

        assertEquals(emptyList<String>(), snapshot.relacionadosIds)
        assertEquals(emptyList<String>(), snapshot.proximasAcoesIds)
        assertEquals(emptyList<String>(), snapshot.progressoEscolas)
        assertEquals(null, snapshot.proximaObrigatoriaId)
        assertEquals(null, snapshot.proximaLateralUtilId)
        assertTrue(snapshot.aviso.orEmpty().contains("não encontrado", ignoreCase = true))
    }

    private fun catalogoBase(): List<MagiaDefinicao> {
        return listOf(
            MagiaDefinicao(
                id = "localizar_planta",
                nome = "Localizar Planta",
                escola = listOf("Plantas"),
                preRequisitos = "-"
            ),
            MagiaDefinicao(
                id = "localizar_agua",
                nome = "Localizar Água",
                escola = listOf("Água"),
                preRequisitos = "-"
            ),
            MagiaDefinicao(
                id = "encantar",
                nome = "Encantar",
                escola = listOf("Encantamento"),
                preRequisitos = "1 magia em 1 outras escolas"
            ),
            MagiaDefinicao(
                id = "pequeno_desejo",
                nome = "Pequeno Desejo",
                escola = listOf("Encantamento"),
                preRequisitos = "Encantar"
            ),
            MagiaDefinicao(
                id = "desejo",
                nome = "Desejo",
                escola = listOf("Encantamento"),
                preRequisitos = "Pequeno Desejo e 1 magia em 2 outras escolas"
            )
        )
    }
}
