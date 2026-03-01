package com.gurps.ficha.regras_prerequisitos

import org.junit.Assert.*
import org.junit.Test

import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.viewmodel.FichaViewModel

class PreRequisitoParserTest {
    @Test
    fun `empty or dash returns no types`() {
        assertTrue(PreRequisitoParser.parse("  ").tipos.isEmpty())
        assertTrue(PreRequisitoParser.parse("—").tipos.isEmpty())
    }

    @Test
    fun `recognizes attribute minimum`() {
        val res = PreRequisitoParser.parse("IQ 12+")
        assertEquals(1, res.tipos.size)
        val item = res.tipos[0]
        assertTrue(item is PreRequisitoType.AttributeMin)
        assertEquals("IQ", (item as PreRequisitoType.AttributeMin).atributo)
        assertEquals(12, item.minimo)
    }

    @Test
    fun `recognizes aptidao magica variants`() {
        // various forms should all parse to same level
        val examples = listOf("AM2", "AM 2", "AM+2", "am 2", "Aptidao Magica 2",
            "Aptidão Mágica nível 3")
        examples.forEach {
            val tipos = PreRequisitoParser.parse(it).tipos
            assertEquals(1, tipos.size)
            assertTrue(tipos[0] is PreRequisitoType.AptidaoMagica)
        }
    }

    @Test
    fun `recognizes magias escola`() {
        val res = PreRequisitoParser.parse("2 mágicas de Fogo")
        assertEquals(1, res.tipos.size)
        val item = res.tipos[0]
        assertTrue(item is PreRequisitoType.MagiasEscola)
        val me = item as PreRequisitoType.MagiasEscola
        assertEquals(2, me.quantidade)
        assertEquals("Fogo", me.escola)
    }

    @Test
    fun `fallback to magic name`() {
        val res = PreRequisitoParser.parse("Magia Exemplo")
        assertEquals(1, res.tipos.size)
        assertTrue(res.tipos[0] is PreRequisitoType.MagiaConhecida)
    }

    @Test
    fun `checkSimples validates character attributes`() {
        val char = mapOf<String, Any>(
            "IQ" to 13,
            "aptidao_magica" to 2,
            "magias_fogo" to 1,
            "magias_conhecidas" to setOf("Magia Exemplo")
        )
        val prereqs = PreRequisitoParser.parse("IQ 12+, AM2, 1 mágicas de Fogo, Magia Exemplo").tipos
        val report = PreRequisitoChecker.checkSimples(char, prereqs)
        assertEquals("todos requisitos atendidos", report)

        val badChar = mapOf<String, Any>(
            "IQ" to 10,
            "aptidao_magica" to 1,
            "magias_fogo" to 0,
            "magias_conhecidas" to emptySet<String>()
        )
        val report2 = PreRequisitoChecker.checkSimples(badChar, prereqs)
        assertTrue(report2.contains("faltando"))
    }

    // --- integração com repositório e ViewModel ------------------------------------------------

    @Test
    fun `repository validates magia prerequisites correctly`() {
        // contexto mínimo com lista vazia de magias (não acessa assets)
        val repo = DataRepository(object : android.content.ContextWrapper(null) {})
        val person = Personagem()
        person.inteligencia = 12
        person.vantagens = listOf(VantagemSelecionada(definicaoId = "aptidao_magica", nivel = 1))

        val magia = MagiaDefinicao(id = "m1", nome = "Teste", preRequisitos = "IQ 13+")
        assertNotNull(repo.validarPreRequisitosMagia(magia, person))
        person.inteligencia = 14
        assertNull(repo.validarPreRequisitosMagia(magia, person))
    }

}

