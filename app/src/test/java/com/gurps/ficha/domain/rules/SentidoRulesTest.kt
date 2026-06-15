package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote 372: Testes de Sentidos — soma de Sentido Aguçado, redutores e bloqueios. */
class SentidoRulesTest {

    // inteligencia 12 → IQ 12 → Percepção 12 (sem mods).
    private fun heroi(
        vantagens: List<VantagemSelecionada> = emptyList(),
        desvantagens: List<DesvantagemSelecionada> = emptyList()
    ) = Personagem(inteligencia = 12, vantagens = vantagens, desvantagens = desvantagens)

    @Test
    fun `percepcao base sem tracos`() {
        val p = heroi()
        val per = SentidoRules.avaliar(p, SentidoRules.Sentido.PERCEPCAO)
        assertEquals(12, per.valorFinal)
        assertTrue(per.componentes.isEmpty())
        assertFalse(per.bloqueado)
    }

    @Test
    fun `visao agucada soma o nivel e gera notinha`() {
        val p = heroi(vantagens = listOf(VantagemSelecionada(definicaoId = "visao_agucada", nivel = 2)))
        val v = SentidoRules.avaliar(p, SentidoRules.Sentido.VISAO)
        assertEquals(14, v.valorFinal) // 12 + 2
        assertEquals("+2 Visão Aguçada", v.nota())
        // não afeta os outros sentidos
        assertEquals(12, SentidoRules.avaliar(p, SentidoRules.Sentido.AUDICAO).valorFinal)
    }

    @Test
    fun `duro de ouvido reduz audicao em 4`() {
        val p = heroi(desvantagens = listOf(DesvantagemSelecionada(definicaoId = "duro_de_ouvido")))
        val a = SentidoRules.avaliar(p, SentidoRules.Sentido.AUDICAO)
        assertEquals(8, a.valorFinal) // 12 - 4
        assertEquals("-4 Duro de Ouvido", a.nota())
    }

    @Test
    fun `hiperespectral mais discriminatorio`() {
        val p = heroi(vantagens = listOf(
            VantagemSelecionada(definicaoId = "visao_hiperespectral"),
            VantagemSelecionada(definicaoId = "paladar_discriminatorio")
        ))
        assertEquals(15, SentidoRules.avaliar(p, SentidoRules.Sentido.VISAO).valorFinal)        // +3
        assertEquals(16, SentidoRules.avaliar(p, SentidoRules.Sentido.OLFATO_PALADAR).valorFinal) // +4
    }

    @Test
    fun `cegueira e surdez bloqueiam o sentido`() {
        val p = heroi(desvantagens = listOf(
            DesvantagemSelecionada(definicaoId = "cegueira"),
            DesvantagemSelecionada(definicaoId = "surdez")
        ))
        val v = SentidoRules.avaliar(p, SentidoRules.Sentido.VISAO)
        val a = SentidoRules.avaliar(p, SentidoRules.Sentido.AUDICAO)
        assertTrue(v.bloqueado); assertEquals("Cego", v.motivoBloqueio)
        assertTrue(a.bloqueado); assertEquals("Surdo", a.motivoBloqueio)
        // tato segue normal
        assertFalse(SentidoRules.avaliar(p, SentidoRules.Sentido.TATO).bloqueado)
    }

    @Test
    fun `vantagem racial tambem conta`() {
        val racial = com.gurps.ficha.model.ModeloRacial(
            vantagens = listOf(VantagemSelecionada(definicaoId = "audicao_agucada", nivel = 3))
        )
        val p = heroi().copy(modeloRacial = racial)
        assertEquals(15, SentidoRules.avaliar(p, SentidoRules.Sentido.AUDICAO).valorFinal) // 12 + 3 racial
    }
}
