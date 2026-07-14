package com.gurps.ficha.ui.saga

import com.gurps.ficha.domain.combat.Manobra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lote TOK-6b-2: mapeamento GURPS dos menus de token (MB p.363-371, AM p.69-77).
 * O menu do HERÓI só tem manobras sobre si mesmo; o do INIMIGO só as direcionadas,
 * condicionadas por alcance/Deslocamento/luta agarrada. Mover nunca é botão (hex verde).
 */
class MenuTaticoTest {

    private val TODAS = Manobra.entries.toList()

    @Test
    fun `menu do heroi so contem manobras sobre si mesmo`() {
        val menu = menuTaticoHeroi(TODAS)
        assertEquals(MANOBRAS_SOBRE_SI, menu.toSet())
        assertFalse(Manobra.ATAQUE in menu)
        assertFalse(Manobra.AGARRAR in menu)
        assertFalse(Manobra.MOVER in menu) // Mover = tocar hex verde, não botão
    }

    @Test
    fun `menu do heroi respeita as manobras legais do motor`() {
        // Atordoado, por exemplo: o motor só oferece Defesa Total / Não Fazer Nada.
        val legais = listOf(Manobra.DEFESA_TOTAL, Manobra.NAO_FAZER_NADA)
        assertEquals(legais, menuTaticoHeroi(legais))
    }

    /** Adjacente ao alcance da arma, em pé, ainda não agarrado (o caso padrão de corpo-a-corpo). */
    private fun menuAdjacente(manobras: List<Manobra> = TODAS, agarrado: Boolean = false, noChao: Boolean = false) =
        menuTaticoInimigo(manobras, aoAlcance = true, adjacente = true, alcancaMovendo = true, agarrado = agarrado, alvoNoChao = noChao)

    @Test
    fun `inimigo ADJACENTE oferece golpes e projeções — e nunca manobras sobre si`() {
        val menu = menuAdjacente()
        for (m in listOf(Manobra.ATAQUE, Manobra.ATAQUE_TOTAL, Manobra.GOLPE_RAPIDO, Manobra.FINTAR,
                Manobra.AGARRAR, Manobra.DERRUBAR, Manobra.ENCONTRAO, Manobra.EMPURRAO)) {
            assertTrue("$m deveria aparecer adjacente", m in menu)
        }
        for (m in MANOBRAS_SOBRE_SI) assertFalse("$m é manobra sobre si", m in menu)
        assertFalse(Manobra.MOVER in menu)
    }

    @Test
    fun `alcance de arma sem adjacencia — golpes SIM, agarrar-derrubar-empurrao NAO (soft-fail do lance a 2m)`() {
        // Achado ALTA da revisão: lança (alcance 2m) põe o alvo em estado.alvos, mas o motor exige
        // dist<=1 para agarrar/derrubar/empurrar — o chip dispararia e o turno se perderia.
        val menu = menuTaticoInimigo(TODAS, aoAlcance = true, adjacente = false, alcancaMovendo = true, agarrado = false, alvoNoChao = false)
        assertTrue(Manobra.ATAQUE in menu)
        assertTrue(Manobra.FINTAR in menu)
        assertFalse("Agarrar exige adjacência", Manobra.AGARRAR in menu)
        assertFalse("Derrubar exige adjacência", Manobra.DERRUBAR in menu)
        assertFalse("Empurrão exige adjacência", Manobra.EMPURRAO in menu)
    }

    @Test
    fun `inimigo FORA de alcance so oferece Avaliar-Apontar-Encontrão e Mover e Atacar se alcançavel`() {
        val menu = menuTaticoInimigo(TODAS, aoAlcance = false, adjacente = false, alcancaMovendo = true, agarrado = false, alvoNoChao = false)
        assertEquals(setOf(Manobra.MOVER_E_ATACAR, Manobra.ENCONTRAO, Manobra.AVALIAR, Manobra.APONTAR), menu.toSet())
    }

    @Test
    fun `chaves e estrangular exigem o alvo JA AGARRADO`() {
        val chaves = setOf(Manobra.ESTRANGULAR, Manobra.CHAVE_MEMBRO, Manobra.MATA_LEAO)
        val solto = menuAdjacente(agarrado = false)
        val preso = menuAdjacente(agarrado = true)
        for (m in chaves) {
            assertFalse("$m sem agarrar", m in solto)
            assertTrue("$m com o alvo agarrado", m in preso)
        }
    }

    @Test
    fun `IMOBILIZAR exige agarrado E no chao (soft-fail do agarrado em pe)`() {
        // Achado ALTA: agarrado mas EM PÉ → o motor manda 'derrube-o antes' e o turno se perderia.
        assertFalse("agarrado em pé", Manobra.IMOBILIZAR in menuAdjacente(agarrado = true, noChao = false))
        assertTrue("agarrado e no chão", Manobra.IMOBILIZAR in menuAdjacente(agarrado = true, noChao = true))
        assertFalse("no chão mas solto", Manobra.IMOBILIZAR in menuAdjacente(agarrado = false, noChao = true))
    }

    @Test
    fun `menu do inimigo preserva a ordem das manobras legais do motor`() {
        val legais = listOf(Manobra.AVALIAR, Manobra.ATAQUE, Manobra.FINTAR)
        assertEquals(legais, menuTaticoInimigo(legais, aoAlcance = true, adjacente = true, alcancaMovendo = false, agarrado = false, alvoNoChao = false))
    }

    @Test
    fun `toda manobra tem icone proprio no chip`() {
        // Garante que ninguém adiciona manobra nova sem cair no `when` exaustivo do ícone.
        for (m in Manobra.entries) assertTrue(iconeDaManobra(m).isNotBlank())
    }
}
