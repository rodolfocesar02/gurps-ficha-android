package com.gurps.ficha.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PericiaJsonParsingTest {

    private val gson = Gson()

    @Test
    fun `parseia atributosPossiveis com grafia legada`() {
        val json = """
            {
              "id": "pericia_teste",
              "nome": "Pericia Teste",
              "atributoBase": "IQ",
              "atributosPossÃ­veis": ["DX", "IQ"],
              "atributoEscolhaObrigatoria": true,
              "dificuldadeFixa": "M",
              "dificuldadeVariavel": false,
              "exigeEspecializacao": false,
              "preDefinicoes": []
            }
        """.trimIndent()

        val pericia = gson.fromJson(json, PericiaDefinicao::class.java)

        assertTrue(pericia.atributosPossiveis != null)
        assertEquals(listOf("DX", "IQ"), pericia.atributosPossiveis)
    }

    @Test
    fun `parseia preDefinicoes com grafia acentuada e legada`() {
        val jsonComAcento = """
            {
              "id": "pericia_teste_predef",
              "nome": "Pericia Teste Predef",
              "atributoBase": "DX",
              "dificuldadeFixa": "M",
              "preDefinições": [
                { "atributo": "DX", "modificador": -4 },
                { "atributo": "IQ", "modificador": -5 }
              ]
            }
        """.trimIndent()

        val periciaAcento = gson.fromJson(jsonComAcento, PericiaDefinicao::class.java)
        assertEquals(2, periciaAcento.preDefinicoes.size)
        assertEquals("DX", periciaAcento.preDefinicoes[0].atributo)
        assertEquals(-4, periciaAcento.preDefinicoes[0].modificador)

        val jsonLegado = """
            {
              "id": "pericia_teste_predef_legacy",
              "nome": "Pericia Teste Predef Legacy",
              "atributoBase": "DX",
              "dificuldadeFixa": "M",
              "preDefiniÃ§Ãµes": [
                { "atributo": "HT", "modificador": -6 }
              ]
            }
        """.trimIndent()

        val periciaLegado = gson.fromJson(jsonLegado, PericiaDefinicao::class.java)
        assertEquals(1, periciaLegado.preDefinicoes.size)
        assertEquals("HT", periciaLegado.preDefinicoes[0].atributo)
        assertEquals(-6, periciaLegado.preDefinicoes[0].modificador)
    }
}
