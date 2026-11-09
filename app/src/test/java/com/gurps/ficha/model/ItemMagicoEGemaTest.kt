package com.gurps.ficha.model

import com.gurps.ficha.ui.nomeNaLista
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **RG-12 (Mesa): o item mágico e a Gema de Energia** (decisão do Rodolfo, 09/nov): qualquer item pode ser marcado mágico
 * (o cajado mágico, Magia p.13), e a Gema de Energia é um tipo de item com a capacidade (Magia p.69). Os dois vão para a Mesa no
 * arquivo da ficha (`fichaInteiraParaAMesa`) e voltam iguais; a ficha antiga lê falso e nada.
 */
class ItemMagicoEGemaTest {
    @Test
    fun `o item magico e a gema vao para a Mesa e voltam do arquivo`() {
        val cajado = Equipamento(nome = "Cajado", tipo = TipoEquipamento.ARMA, armaDanoRaw = "GdB+2 cont", armaAlcanceCorpoACorpo = "1,2", itemMagico = true)
        val gema = Equipamento(nome = "Gema de Energia", tipo = TipoEquipamento.GEMA_DE_ENERGIA, itemMagico = true, gemaCapacidade = 10, peso = 0.1f)
        val p = Personagem(nome = "César", equipamentos = listOf(cajado, gema))
        val mesa = PersonagemInterop.fichaInteiraParaAMesa(p).getAsJsonArray("equipamentos")
        assertTrue(mesa[0].asJsonObject.get("itemMagico").asBoolean)
        assertEquals("GEMA_DE_ENERGIA", mesa[1].asJsonObject.get("tipo").asString)
        assertEquals(10, mesa[1].asJsonObject.get("gemaCapacidade").asInt)
        val volta = Personagem.fromJson(p.toJson())
        assertEquals(cajado, volta.equipamentos[0])
        assertEquals(gema, volta.equipamentos[1])
    }

    @Test
    fun `a ficha antiga le falso e nada, e a lista marca o magico`() {
        val antiga = Personagem.fromJson("""{"nome":"Velha","equipamentos":[{"nome":"Espada","tipo":"ARMA"}]}""")
        assertFalse(antiga.equipamentos[0].itemMagico)
        assertEquals(null, antiga.equipamentos[0].gemaCapacidade)
        assertEquals("✨ Varinha", nomeNaLista(Equipamento(nome = "Varinha", itemMagico = true)))
        assertEquals("Espada", nomeNaLista(antiga.equipamentos[0]))
    }
}
