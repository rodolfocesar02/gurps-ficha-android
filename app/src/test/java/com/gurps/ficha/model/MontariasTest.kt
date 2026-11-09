package com.gurps.ficha.model

import com.google.gson.Gson
import com.gurps.ficha.ui.equipamentoDaMontaria
import com.gurps.ficha.ui.linhasDaMontaria
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **RG-11 (Mesa): as montarias no inventário** (MB pp.459-460).
 *
 * O catálogo tem as 12 do livro com os números da página; o item entra com peso 0 (decisão do Rodolfo: o animal não pesa na
 * carga) e a ficha técnica viaja dentro dele no JSON que vai para a Mesa (`fichaInteiraParaAMesa`), e volta igual do arquivo.
 */
class MontariasTest {
    private fun catalogo(): List<MontariaDados> {
        val f = listOf(File("src/main/assets/montarias.v1.json"), File("app/src/main/assets/montarias.v1.json")).first { it.exists() }
        return Gson().fromJson(f.readText(), CatalogoDeMontarias::class.java).montarias
    }

    @Test
    fun `o catalogo tem as 12 do livro com os numeros da pagina`() {
        val c = catalogo()
        assertEquals(12, c.size)
        val sela = c.first { it.id == "cavalo_de_sela" }
        assertEquals(listOf(21, 9, 3, 11, 6, 8, 3), listOf(sela.st, sela.dx, sela.iq, sela.ht, sela.deslocamento, sela.esquiva, sela.hexes))
        assertEquals(1200f, sela.custo)
        val guerra = c.first { it.id == "cavalo_de_guerra_pesado" }
        assertTrue(guerra.treinadaParaGuerra)
        assertEquals(listOf("Briga 12", "Montaria 13"), guerra.pericias)
        assertFalse(c.first { it.id == "burro" }.montavel)
        assertEquals(10, c.first { it.id == "elefante" }.hexes)
    }

    @Test
    fun `a montaria entra com peso 0 e nao muda a carga`() {
        val sela = catalogo().first { it.id == "cavalo_de_sela" }
        val item = equipamentoDaMontaria(sela)
        assertEquals(TipoEquipamento.MONTARIA, item.tipo)
        assertEquals(0f, item.peso)
        val p = Personagem(nome = "Jack", equipamentos = listOf(Equipamento(nome = "Mochila", peso = 2f), item))
        assertEquals(2f, p.pesoTotalEquipamentos)
        assertTrue(linhasDaMontaria(sela).any { it.texto.contains("600 kg") })
    }

    @Test
    fun `a ficha tecnica vai inteira para a Mesa e volta do arquivo`() {
        val sela = catalogo().first { it.id == "cavalo_de_cavalaria" }
        val p = Personagem(nome = "Jack", equipamentos = listOf(equipamentoDaMontaria(sela)))
        val paraAMesa = PersonagemInterop.fichaInteiraParaAMesa(p)
        val eq = paraAMesa.getAsJsonArray("equipamentos")[0].asJsonObject
        assertEquals("MONTARIA", eq.get("tipo").asString)
        assertEquals(22, eq.getAsJsonObject("montaria").get("st").asInt)
        assertEquals(8, eq.getAsJsonObject("montaria").get("deslocamento").asInt)
        val volta = Personagem.fromJson(p.toJson())
        assertEquals(sela, volta.equipamentos[0].montaria)
    }
}
