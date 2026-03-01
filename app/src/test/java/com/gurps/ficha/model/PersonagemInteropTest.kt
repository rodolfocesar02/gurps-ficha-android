package com.gurps.ficha.model

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonagemInteropTest {

    @Test
    fun `exportar gera envelope com metadados e personagem`() {
        val personagem = Personagem(nome = "Merlin", jogador = "Rodolfo")

        val json = PersonagemInterop.exportarJson(
            personagem = personagem,
            appVersion = "1.1",
            uiVariant = "visual"
        )

        val root = JsonParser.parseString(json).asJsonObject
        assertEquals(PersonagemInterop.SCHEMA, root.get("schema").asString)
        assertEquals(PersonagemInterop.SCHEMA_VERSION_ATUAL, root.get("schemaVersion").asInt)
        assertEquals("1.1", root.get("appVersion").asString)
        assertEquals("visual", root.get("uiVariant").asString)
        assertNotNull(root.get("exportedAtUtc").asString)
        assertEquals("Merlin", root.getAsJsonObject("character").get("nome").asString)
    }

    @Test
    fun `importar envelope atual restaura personagem`() {
        val original = Personagem(nome = "Aria", forca = 13, pontosIniciais = 200)
        val json = PersonagemInterop.exportarJson(original)

        val resultado = PersonagemInterop.importarJson(json)

        assertEquals("Aria", resultado.personagem.nome)
        assertEquals(13, resultado.personagem.forca)
        assertEquals(200, resultado.personagem.pontosIniciais)
        assertEquals(PersonagemInterop.SCHEMA_VERSION_ATUAL, resultado.metadata?.schemaVersion)
    }

    @Test
    fun `importar json legado sem envelope continua compativel`() {
        val legado = Personagem(nome = "Legado", inteligencia = 12).toJson()

        val resultado = PersonagemInterop.importarJson(legado)

        assertEquals("Legado", resultado.personagem.nome)
        assertEquals(12, resultado.personagem.inteligencia)
        assertEquals(null, resultado.metadata)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `importar versao futura falha com erro de compatibilidade`() {
        val jsonFuturo = """
            {
              "schema": "${PersonagemInterop.SCHEMA}",
              "schemaVersion": ${PersonagemInterop.SCHEMA_VERSION_ATUAL + 1},
              "character": { "nome": "Futuro" }
            }
        """.trimIndent()

        PersonagemInterop.importarJson(jsonFuturo)
    }

    @Test
    fun `importar schema desconhecido falha`() {
        val jsonInvalido = """
            {
              "schema": "outro.schema",
              "schemaVersion": 1,
              "character": { "nome": "Invalido" }
            }
        """.trimIndent()

        val erro = runCatching { PersonagemInterop.importarJson(jsonInvalido) }.exceptionOrNull()
        assertTrue(erro is IllegalArgumentException)
    }
}
