package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.AtributoBase
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.ModeloRacial
import com.gurps.ficha.model.PericiaDefinicao
import com.gurps.ficha.model.PericiaRacial
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.TipoPericiaRacial
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Talento Instintivo (MB p.92) — rolar o atributo cheio numa perícia desconhecida.
 */
class TalentoInstintivoRulesTest {

    private val catalogo = listOf(
        PericiaDefinicao(id = "arrombamento", nome = "Arrombamento/NT", atributoBase = "DX"),
        PericiaDefinicao(id = "programacao", nome = "Programação/NT", atributoBase = "IQ"),
        PericiaDefinicao(id = "natacao", nome = "Natação", atributoBase = "HT"),
        PericiaDefinicao(id = "escalada", nome = "Escalada", atributoBase = "DX")
    )

    private fun p(
        pericias: List<PericiaSelecionada> = emptyList(),
        nivel: Int = 1,
        racial: ModeloRacial = ModeloRacial()
    ) = Personagem(
        nome = "T", destreza = 14, inteligencia = 12, vitalidade = 11,
        pericias = pericias,
        vantagens = if (nivel > 0) listOf(
            VantagemSelecionada(definicaoId = TalentoInstintivoRules.ID, nome = "Talento Instintivo", nivel = nivel)
        ) else emptyList(),
        modeloRacial = racial
    )

    private fun pericia(id: String, nome: String) = PericiaSelecionada(
        definicaoId = id, nome = nome,
        atributoBase = AtributoBase.DX, dificuldade = Dificuldade.MEDIA, pontosGastos = 2
    )

    // --- usos por sessão ---

    @Test
    fun `o nivel E o numero de usos por sessao`() {
        assertEquals(1, TalentoInstintivoRules.usosPorSessao(p(nivel = 1)))
        assertEquals(3, TalentoInstintivoRules.usosPorSessao(p(nivel = 3)))
        assertTrue(TalentoInstintivoRules.tem(p(nivel = 1)))
        assertFalse(TalentoInstintivoRules.tem(p(nivel = 0)))
    }

    @Test
    fun `os usos restantes nunca ficam negativos`() {
        val pj = p(nivel = 2)
        assertEquals(2, TalentoInstintivoRules.usosRestantes(pj, 0))
        assertEquals(0, TalentoInstintivoRules.usosRestantes(pj, 2))
        assertEquals("gastar mais do que tem nao vira divida", 0,
            TalentoInstintivoRules.usosRestantes(pj, 9))
    }

    // --- a lista de perícias ---

    @Test
    fun `⚠️ so oferece pericia que o personagem NAO tem`() {
        // MB p.92: "Esta vantagem nao surte efeito nas pericias que o personagem
        // ja conhece". Oferece-las seria oferecer um NH pior que o que ele tem.
        val pj = p(pericias = listOf(pericia("escalada", "Escalada")))
        val nomes = TalentoInstintivoRules.opcoesDe(pj, catalogo).map { it.nome }
        assertFalse("Escalada" in nomes)
        assertEquals(3, nomes.size)
    }

    @Test
    fun `rola o ATRIBUTO da pericia, sem penalidade`() {
        // DX 14, IQ 12, HT 11 -- cada pericia usa o seu.
        val opcoes = TalentoInstintivoRules.opcoesDe(p(), catalogo).associateBy { it.nome }
        assertEquals(14, opcoes.getValue("Arrombamento/NT").nh)
        assertEquals(12, opcoes.getValue("Programação/NT").nh)
        assertEquals(11, opcoes.getValue("Natação").nh)
    }

    @Test
    fun `pericia RACIAL concedida tambem conta como conhecida`() {
        // Ela entra na ficha com o id prefixado ("racial_rastreamento"), entao a
        // comparacao por id sozinha deixaria passar.
        val pj = p(
            racial = ModeloRacial(
                nome = "Elfo",
                pericias = listOf(
                    PericiaRacial(nome = "Escalada", tipo = TipoPericiaRacial.CONCEDIDA, baseAtributo = "DX")
                )
            )
        )
        val nomes = TalentoInstintivoRules.opcoesDe(pj, catalogo).map { it.nome }
        assertFalse("Escalada veio da raca e nao deveria estar na lista", "Escalada" in nomes)
    }

    @Test
    fun `a lista sai em ordem alfabetica`() {
        // Sao ~250 pericias; sem ordem, a busca e a unica saida.
        val nomes = TalentoInstintivoRules.opcoesDe(p(), catalogo).map { it.nome }
        assertEquals(nomes.sorted(), nomes)
    }

    @Test
    fun `atributo em branco cai em IQ, e nao quebra`() {
        val estranha = listOf(PericiaDefinicao(id = "x", nome = "Estranha", atributoBase = ""))
        val opcao = TalentoInstintivoRules.opcoesDe(p(), estranha).single()
        assertEquals("IQ", opcao.atributo)
        assertEquals(12, opcao.nh)
    }

    // --- os textos ---

    @Test
    fun `o rotulo mostra os usos restantes`() {
        val texto = TalentoInstintivoRules.rotulo(p(nivel = 3), 1)
        assertTrue(texto, texto.contains("2 de 3"))
    }

    @Test
    fun `o aviso diz as duas ressalvas`() {
        assertTrue(TalentoInstintivoRules.AVISO.contains("NÃO tem"))
        assertTrue(TalentoInstintivoRules.AVISO.contains("NT"))
    }

    @Test
    fun `Talento Instintivo RACIAL tambem conta`() {
        val pj = Personagem(
            nome = "T",
            modeloRacial = ModeloRacial(
                nome = "Fada",
                vantagens = listOf(
                    VantagemSelecionada(
                        definicaoId = TalentoInstintivoRules.ID,
                        nome = "Talento Instintivo", nivel = 2
                    )
                )
            )
        )
        assertEquals(2, TalentoInstintivoRules.usosPorSessao(pj))
    }
}
