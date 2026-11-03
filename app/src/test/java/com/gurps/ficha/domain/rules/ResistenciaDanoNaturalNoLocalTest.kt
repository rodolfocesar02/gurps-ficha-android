package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.ModeloRacial
import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **A RD natural por local** — TR-3 do plano dos traços da Mesa (`mesa-virtual/planos/tracos/PLANO.md`).
 *
 * O diálogo de Ferimento somava a RD natural em qualquer local: no olho (o livro diz que não protege, MB p.83) e o
 * Minotauro levava 5 em todo lugar. Os números daqui são os mesmos do `tracos-tr3.test.js` da Mesa (`rd-natural.js`).
 */
class ResistenciaDanoNaturalNoLocalTest {

    private fun rd(nivel: Int, descricao: String = "", vararg mods: String) = VantagemSelecionada(
        definicaoId = "resistencia_a_dano", nome = "Resistência a Dano", nivel = nivel, descricao = descricao,
        modificadores = mods.map { ModificadorSelecao(id = it, nome = it) }
    )

    /** O Minotauro do `racas.v1.json`: RD 2 "Apenas o Crânio" (Parcial) + RD 3 com Pele Resistente. */
    private val minotauro = Personagem(modeloRacial = ModeloRacial(vantagens = listOf(
        rd(2, "Apenas o Crânio", "mod_parcial"), rd(3, "", "pele_resistente")
    )))

    @Test
    fun `Minotauro - 5 no cranio, 3 no tronco, nos vitais e no braco, 0 no olho`() {
        assertEquals(5, ResistenciaDanoNatural.noLocal(minotauro, LocalAtaque.CRANIO).rd)
        assertEquals(3, ResistenciaDanoNatural.noLocal(minotauro, LocalAtaque.TORSO).rd)
        assertEquals(3, ResistenciaDanoNatural.noLocal(minotauro, LocalAtaque.VITAIS).rd)
        assertEquals(3, ResistenciaDanoNatural.noLocal(minotauro, LocalAtaque.BRACO).rd)
        assertEquals(0, ResistenciaDanoNatural.noLocal(minotauro, LocalAtaque.OLHO).rd)
        assertEquals("a Pele Resistente é flexível", 3, ResistenciaDanoNatural.noLocal(minotauro, LocalAtaque.CRANIO).flexivel)
    }

    @Test
    fun `Membrana Nictitante - RD so no olho, por nivel`() {
        val p = Personagem(vantagens = listOf(VantagemSelecionada(definicaoId = "membrana_nictitante", nome = "Membrana Nictitante", nivel = 3)))
        assertEquals(3, ResistenciaDanoNatural.noLocal(p, LocalAtaque.OLHO).rd)
        assertEquals(0, ResistenciaDanoNatural.noLocal(p, LocalAtaque.CRANIO).rd)
    }

    @Test
    fun `Campo de Forca protege o olho, e a Parcial dos bracos protege os dois bracos e mais nada`() {
        assertEquals(2, ResistenciaDanoNatural.noLocal(Personagem(vantagens = listOf(rd(2, "", "campo_de_forca"))), LocalAtaque.OLHO).rd)
        val bracos = Personagem(vantagens = listOf(rd(4, "Apenas os Braços", "mod_parcial")))
        assertEquals(4, ResistenciaDanoNatural.noLocal(bracos, LocalAtaque.BRACO).rd)
        assertEquals(0, ResistenciaDanoNatural.noLocal(bracos, LocalAtaque.PERNA).rd)
        val tronco = Personagem(vantagens = listOf(rd(1, "Apenas o Tronco", "mod_parcial")))
        assertEquals("o Tronco protege os vitais (MB p.83)", 1, ResistenciaDanoNatural.noLocal(tronco, LocalAtaque.VITAIS).rd)
    }

    @Test
    fun `Parcial sem local na ficha nao desconta, e avisa`() {
        val r = ResistenciaDanoNatural.noLocal(Personagem(vantagens = listOf(rd(2, "", "mod_parcial"))), LocalAtaque.TORSO)
        assertEquals(0, r.rd)
        assertTrue(r.explicacao!!, r.explicacao!!.contains("Parcial sem local"))
    }

    @Test
    fun `sem RD natural nem Membrana, nada - e nenhuma linha vazia`() {
        val r = ResistenciaDanoNatural.noLocal(Personagem(), LocalAtaque.TORSO)
        assertEquals(0, r.rd)
        assertEquals(null, r.explicacao)
    }
}
