package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O catálogo de testes de resistência (Lote RESIST-1).
 *
 * A ideia do usuário: juntar num botão só tudo que é "resistir". Antes disso o
 * GURPS tinha vários testes que a ficha simplesmente não sabia montar.
 */
class ResistenciaRulesTest {

    private fun heroi(
        ht: Int = 10,
        iq: Int = 10,
        vantagens: List<VantagemSelecionada> = emptyList()
    ) = Personagem(nome = "Teste", vitalidade = ht, inteligencia = iq, vantagens = vantagens)

    private fun vant(id: String, nome: String, nivel: Int = 1, custo: Int = 0) =
        VantagemSelecionada(definicaoId = id, nome = nome, nivel = nivel, custoEscolhido = custo)

    @Test
    fun `ficha sem vantagem nenhuma ja tem os testes de corpo e mente`() {
        // Qualquer personagem pode precisar resistir a veneno -- nao depende de
        // traco. So o sobrenatural e condicional.
        val testes = ResistenciaRules.testesDe(heroi())
        assertTrue(testes.any { it.rotulo.contains("veneno") })
        assertTrue(testes.any { it.rotulo.contains("doença") })
        assertTrue(testes.any { it.rotulo.contains("Pânico") })
        assertTrue(
            "sem Abascanto nao pode ter teste sobrenatural",
            testes.none { it.familia == ResistenciaRules.Familia.SOBRENATURAL }
        )
    }

    @Test
    fun `os testes de corpo saem do HT e os de mente da Vontade`() {
        val p = heroi(ht = 12, iq = 14)   // Vontade = IQ = 14
        val testes = ResistenciaRules.testesDe(p)
        assertEquals(12, testes.first { it.rotulo.contains("veneno") }.alvo)
        assertEquals(14, testes.first { it.rotulo.contains("Pânico") }.alvo)
    }

    @Test
    fun `Boa Forma entra em TODOS os testes de HT e em nenhum de Vontade`() {
        val p = heroi(vantagens = listOf(vant("boa_forma", "Boa Forma", custo = 15)))
        val testes = ResistenciaRules.testesDe(p)
        testes.filter { it.familia == ResistenciaRules.Familia.CORPO }.forEach {
            assertEquals("${it.rotulo} deveria ter +2", 12, it.alvo)
        }
        // Panico e Vontade -- Boa Forma nao toca.
        assertEquals(10, testes.first { it.rotulo.contains("Pânico") }.alvo)
    }

    @Test
    fun `Boa Forma de 5 pontos da mais 1, nao mais 2`() {
        val p = heroi(vantagens = listOf(vant("boa_forma", "Boa Forma", custo = 5)))
        assertEquals(
            11,
            ResistenciaRules.testesDe(p).first { it.rotulo.contains("veneno") }.alvo
        )
    }

    @Test
    fun `Destemor soma nos dois testes de mente`() {
        val p = heroi(vantagens = listOf(vant("destemor", "Destemor", nivel = 3)))
        val testes = ResistenciaRules.testesDe(p)
        assertEquals(13, testes.first { it.rotulo.contains("Pânico") }.alvo)
        assertEquals(13, testes.first { it.rotulo.contains("Intimidação") }.alvo)
        // ...e em nenhum de corpo.
        assertEquals(10, testes.first { it.rotulo.contains("veneno") }.alvo)
    }

    @Test
    fun `a Verificacao de Panico avisa que NAO e disparada por dano`() {
        // Foi um erro meu no plano: eu havia juntado Panico com os testes de
        // marco de PV. O texto na tela impede que o proximo repita.
        val t = ResistenciaRules.testesDe(heroi()).first { it.rotulo.contains("Pânico") }
        assertTrue(t.explicacao, t.explicacao.contains("NÃO é disparada por dano"))
    }

    @Test
    fun `Dificil de Subjugar e Duro de Matar cada um no seu teste`() {
        val p = heroi(vantagens = listOf(
            vant("dificil_de_subjugar", "Difícil de Subjugar", nivel = 2),
            vant("duro_de_matar", "Duro de Matar", nivel = 3)
        ))
        val testes = ResistenciaRules.testesDe(p)
        assertEquals(12, testes.first { it.rotulo.contains("consciência") }.alvo)
        assertEquals(13, testes.first { it.rotulo.contains("morte") }.alvo)
    }

    @Test
    fun `Abascanto acrescenta o teste de elixir e expoe o nivel`() {
        val p = heroi(vantagens = listOf(vant("abascanto", "Abascanto", nivel = 3)))
        val testes = ResistenciaRules.testesDe(p)
        val elixir = testes.first { it.familia == ResistenciaRules.Familia.SOBRENATURAL }
        assertEquals("HT 10 + RM 3", 13, elixir.alvo)
        assertEquals(3, ResistenciaRules.resistenciaAMagia(p))
    }

    @Test
    fun `sem Abascanto a Resistencia a Magia e zero`() {
        assertEquals(0, ResistenciaRules.resistenciaAMagia(heroi()))
    }

    @Test
    fun `todo teste explica o que acontece e diz de onde veio o bonus`() {
        val p = heroi(vantagens = listOf(vant("boa_forma", "Boa Forma", custo = 15)))
        ResistenciaRules.testesDe(p).forEach { t ->
            assertTrue("${t.rotulo} sem explicacao", t.explicacao.length > 15)
            val d = t.descricaoAcessivel
            assertTrue(d, d.contains("Alvo ${t.alvo}"))
        }
    }

    @Test
    fun `temAptidaoMagica enxerga a vantagem`() {
        assertTrue(!ResistenciaRules.temAptidaoMagica(heroi()))
        val p = heroi(vantagens = listOf(vant("aptidao_magica", "Aptidão Mágica")))
        assertTrue(ResistenciaRules.temAptidaoMagica(p))
    }
}
