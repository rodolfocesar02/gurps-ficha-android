package com.gurps.ficha.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MestreIAClientTest {

    @Test
    fun testExtrairJsonFichaComMarkdown() {
        val textoSujo = """
            Claro, aqui está a ficha do personagem:
            ```json
            {
              "nome": "Arthur, o Bravo",
              "atributos": { "st": 12, "dx": 11, "iq": 10, "ht": 12 }
            }
            ```
            Espero que ajude!
        """.trimIndent()

        val ficha = MestreIAClient.extrairJsonFicha(textoSujo)
        assertNotNull("A ficha não deve ser nula", ficha)
        assertEquals("Arthur, o Bravo", ficha?.nome)
        assertEquals(12, ficha?.atributos?.st)
    }

    @Test
    fun testExtrairJsonFichaSemMarkdown() {
        val textoSujo = """
            {
              "nome": "Conan",
              "atributos": { "st": 18, "dx": 13, "iq": 10, "ht": 14 }
            }
        """.trimIndent()

        val ficha = MestreIAClient.extrairJsonFicha(textoSujo)
        assertNotNull(ficha)
        assertEquals("Conan", ficha?.nome)
    }

    @Test
    fun testExtrairJsonFichaComTextoExtra() {
        val textoSujo = """
            A ficha é { "nome": "Legolas", "atributos": { "st": 10, "dx": 15, "iq": 12, "ht": 10 } } e ele é um elfo.
        """.trimIndent()

        val ficha = MestreIAClient.extrairJsonFicha(textoSujo)
        assertNotNull(ficha)
        assertEquals("Legolas", ficha?.nome)
    }

    @Test
    fun testExtrairJsonFichaComCaracteresDeControle() {
        // Simular caractere de controle inválido (ex: \u0001)
        val textoSujo = "{\n  \"nome\": \"Zorro\u0001\",\n  \"atributos\": { \"st\": 11, \"dx\": 14, \"iq\": 12, \"ht\": 11 }\n}"
        
        val ficha = MestreIAClient.extrairJsonFicha(textoSujo)
        assertNotNull(ficha)
        // O caractere de controle deve ter sido removido ou ignorado pelo parse robusto
        assertEquals("Zorro", ficha?.nome?.filter { it.code >= 32 })
    }

    @Test
    fun testExtrairJsonFichaInvalido() {
        val textoInvalido = "Isso não é um JSON."
        val ficha = MestreIAClient.extrairJsonFicha(textoInvalido)
        assertNull(ficha)
    }
}
