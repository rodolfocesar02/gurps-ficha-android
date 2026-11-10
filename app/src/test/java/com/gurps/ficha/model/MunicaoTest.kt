package com.gurps.ficha.model

import com.gurps.ficha.ui.armaDeLonge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **A munição como estoque** (relatório de testes da Mesa, decisão do Rodolfo em 10/nov: "Deve mexer sim!"): o tipo MUNICAO com a
 * quantidade e a arma que ela serve (`municaoDe`); vai para a Mesa no arquivo da ficha e volta igual; a ficha antiga lê null.
 */
class MunicaoTest {
    @Test
    fun `a municao vai para a Mesa com a arma que serve e volta do arquivo`() {
        val arco = Equipamento(nome = "Arco Longo", tipo = TipoEquipamento.ARMA, armaTipoCombate = "distancia", armaTirosRaw = "1(2)")
        val flechas = Equipamento(nome = "Flechas", tipo = TipoEquipamento.MUNICAO, quantidade = 20, peso = 0.05f, municaoDe = "Arco Longo")
        val p = Personagem(nome = "Jack", equipamentos = listOf(arco, flechas))
        val eq = PersonagemInterop.fichaInteiraParaAMesa(p).getAsJsonArray("equipamentos")[1].asJsonObject
        assertEquals("MUNICAO", eq.get("tipo").asString)
        assertEquals("Arco Longo", eq.get("municaoDe").asString)
        assertEquals(20, eq.get("quantidade").asInt)
        assertEquals(flechas, Personagem.fromJson(p.toJson()).equipamentos[1])
        assertEquals(null, Personagem.fromJson("""{"nome":"V","equipamentos":[{"nome":"Espada","tipo":"ARMA"}]}""").equipamentos[0].municaoDe)
    }

    @Test
    fun `so a arma que atira entra no Serve para`() {
        assertTrue(armaDeLonge(Equipamento(nome = "Arco", tipo = TipoEquipamento.ARMA, armaTirosRaw = "1(2)")))
        assertTrue(armaDeLonge(Equipamento(nome = "Besta", tipo = TipoEquipamento.ARMA, armaTipoCombate = "distancia")))
        assertFalse(armaDeLonge(Equipamento(nome = "Espada", tipo = TipoEquipamento.ARMA, armaTipoCombate = "corpo_a_corpo")))
    }
}
