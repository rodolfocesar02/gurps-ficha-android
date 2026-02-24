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
}
