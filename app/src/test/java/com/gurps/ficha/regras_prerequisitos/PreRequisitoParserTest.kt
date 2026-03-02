package com.gurps.ficha.regras_prerequisitos

import android.content.ContextWrapper
import com.gurps.ficha.data.DataRepository
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.MagiaDefinicao
import com.gurps.ficha.model.MagiaSelecionada
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreRequisitoParserTest {

    @Test
    fun `empty or dash returns no terms`() {
        assertTrue(PreRequisitoParser.parse("  ").terms.isEmpty())
        assertTrue(PreRequisitoParser.parse("—").terms.isEmpty())
    }

    @Test
    fun `recognizes aptidao magica variants`() {
        val examples = listOf("AM2", "AM 2", "AM+2", "am 2", "Aptidao Magica 2", "Aptidao Magica nivel 3")
        examples.forEach {
            val tipos = PreRequisitoParser.parse(it).tipos
            assertTrue(tipos.any { t -> t is PreRequisitoType.AptidaoMagica })
        }
    }

    @Test
    fun `ou and e precedence works for corrected string`() {
        val parsed = PreRequisitoParser.parse("Cura Superficial e Paralisar Membro ou Cura Profunda")
        val personagem = mapOf<String, Any>(
            "aptidao_magica" to 0,
            "magias_conhecidas_normalizadas" to setOf("cura profunda"),
            "escolas_conhecidas_normalizadas" to emptySet<String>(),
            "magias_por_escola_normalizada" to emptyMap<String, Int>(),
            "escolas_por_magia_normalizadas" to emptyMap<String, Set<String>>(),
            "vantagens_conhecidas_normalizadas" to emptySet<String>(),
            "pericias_conhecidas_normalizadas" to emptySet<String>(),
            "condicoes_estado_normalizadas" to emptySet<String>()
        )
        val report = PreRequisitoChecker.checkParseResult(personagem, parsed)
        assertEquals("todos requisitos atendidos", report)
    }

    @Test
    fun `inclusive requirement is parsed`() {
        val parsed = PreRequisitoParser.parse("6 magicas de Relampago, Incl.(ou inclusive) Imunidade a Relampagos")
        assertTrue(parsed.tipos.any { it is PreRequisitoType.MagiaInclusaNaContagem })
    }

    @Test
    fun `special and hash bypass automatic validation`() {
        assertTrue(PreRequisitoParser.parse("Especial").bypassValidation)
        assertTrue(PreRequisitoParser.parse("AM2 #").bypassValidation)
    }

    @Test
    fun `quantificadores complexos sao reconhecidos`() {
        val quaisquer = PreRequisitoParser.parse("Quaisquer 2 magicas Atrofiar")
        assertTrue(quaisquer.tipos.any { it is PreRequisitoType.QuantidadeMagiasPorTemas })

        val outras = PreRequisitoParser.parse("8 outras magicas")
        assertTrue(outras.tipos.any { it is PreRequisitoType.QuantidadeOutrasMagias })

        val naoPode = PreRequisitoParser.parse("Nao pode ser cego")
        assertTrue(naoPode.tipos.any { it is PreRequisitoType.NaoPodeSer })
    }

    @Test
    fun `repository validates fallback magia vantagem pericia and escudo exception`() {
        val repo = DataRepository(object : ContextWrapper(null) {})
        val person = Personagem().apply {
            vantagens = listOf(
                VantagemSelecionada(definicaoId = "aptidao_magica", nome = "Aptidao Magica", nivel = 1),
                VantagemSelecionada(definicaoId = "v_custom", nome = "Empatia em Animais", nivel = 1)
            )
            pericias = listOf(
                PericiaSelecionada(definicaoId = "p_custom", nome = "Persuasao"),
                PericiaSelecionada(definicaoId = "escudo", nome = "Escudo")
            )
            magias = listOf(
                MagiaSelecionada(definicaoId = "m_custom", nome = "Vexacao", escola = listOf("Mente"))
            )
        }

        val porMagia = MagiaDefinicao(id = "m1", nome = "Teste", preRequisitos = "Vexacao")
        val porVantagem = MagiaDefinicao(id = "m2", nome = "Teste2", preRequisitos = "Empatia em Animais")
        val porPericia = MagiaDefinicao(id = "m3", nome = "Teste3", preRequisitos = "Persuasao")
        val escudo = MagiaDefinicao(id = "m4", nome = "Teste4", preRequisitos = "Escudo")

        assertNull(repo.validarPreRequisitosMagia(porMagia, person))
        assertNull(repo.validarPreRequisitosMagia(porVantagem, person))
        assertNull(repo.validarPreRequisitosMagia(porPericia, person))
        assertNotNull(repo.validarPreRequisitosMagia(escudo, person))
    }

    @Test
    fun `repository validates wildcard and negative condition`() {
        val repo = DataRepository(object : ContextWrapper(null) {})
        val person = Personagem().apply {
            vantagens = listOf(VantagemSelecionada(definicaoId = "aptidao_magica", nome = "Aptidao Magica", nivel = 1))
            desvantagens = listOf(DesvantagemSelecionada(definicaoId = "cego", nome = "Cego", custoBase = -50))
            magias = listOf(
                MagiaSelecionada(definicaoId = "a1", nome = "Atravessar Terra", escola = listOf("Terra")),
                MagiaSelecionada(definicaoId = "a2", nome = "Atravessar Agua", escola = listOf("Agua"))
            )
        }

        val criarPorta = MagiaDefinicao(id = "m1", nome = "Criar Porta", preRequisitos = "qualquer uma magia Atravessar")
        val ilusao = MagiaDefinicao(id = "m2", nome = "Ilusao Simples", preRequisitos = "Nao pode ser cego")

        assertNull(repo.validarPreRequisitosMagia(criarPorta, person))
        assertNotNull(repo.validarPreRequisitosMagia(ilusao, person))
    }

    @Test
    fun `repository validates basic attribute minimum`() {
        val repo = DataRepository(object : ContextWrapper(null) {})
        val person = Personagem().apply {
            inteligencia = 12
            vantagens = listOf(VantagemSelecionada(definicaoId = "aptidao_magica", nome = "Aptidao Magica", nivel = 1))
        }

        val magia = MagiaDefinicao(id = "m1", nome = "Teste", preRequisitos = "IQ 13+")
        assertNotNull(repo.validarPreRequisitosMagia(magia, person))

        person.inteligencia = 14
        assertNull(repo.validarPreRequisitosMagia(magia, person))
        assertNull(repo.missingPreRequisitoReport(magia, person))
    }
}
