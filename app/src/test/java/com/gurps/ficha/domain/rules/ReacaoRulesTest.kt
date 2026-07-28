package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.EfeitoDeclarado
import com.gurps.ficha.domain.rules.traits.EfeitoInterpretador
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre os Testes de Reação (Lote REACAO-1, MB p.494).
 *
 * O app nunca teve isto: o jogador rolava 3d6, consultava a tabela no livro e
 * lembrava de cabeça os modificadores das vantagens sociais.
 *
 * Reação é diferente dos outros testes do GURPS: **não há NH**. Rola-se 3d6 e
 * consulta-se a faixa; o modificador desloca o resultado. Por isso um +2 pode
 * transformar uma reação neutra em boa — e é justamente esse o efeito das
 * vantagens sociais.
 */
class ReacaoRulesTest {

    @After
    fun limpar() = EfeitoInterpretador.restaurarBuscadorPadrao()

    private fun comEfeitos(mapa: Map<String, List<EfeitoDeclarado>>) {
        EfeitoInterpretador.buscador = { id -> mapa[id] }
    }

    // --- a tabela ---

    @Test
    fun `as cinco faixas da tabela do livro`() {
        assertEquals(ReacaoRules.Faixa.PESSIMA, ReacaoRules.faixaDe(3))
        assertEquals(ReacaoRules.Faixa.PESSIMA, ReacaoRules.faixaDe(6))
        assertEquals(ReacaoRules.Faixa.RUIM, ReacaoRules.faixaDe(7))
        assertEquals(ReacaoRules.Faixa.RUIM, ReacaoRules.faixaDe(9))
        assertEquals(ReacaoRules.Faixa.NEUTRA, ReacaoRules.faixaDe(10))
        assertEquals(ReacaoRules.Faixa.NEUTRA, ReacaoRules.faixaDe(12))
        assertEquals(ReacaoRules.Faixa.BOA, ReacaoRules.faixaDe(13))
        assertEquals(ReacaoRules.Faixa.BOA, ReacaoRules.faixaDe(15))
        assertEquals(ReacaoRules.Faixa.EXCELENTE, ReacaoRules.faixaDe(16))
        assertEquals(ReacaoRules.Faixa.EXCELENTE, ReacaoRules.faixaDe(18))
    }

    @Test
    fun `o modificador desloca a faixa - e esse o efeito das vantagens sociais`() {
        // 11 sozinho e neutra; com +2 vira boa.
        assertEquals(ReacaoRules.Faixa.NEUTRA, ReacaoRules.faixaDe(11))
        assertEquals(ReacaoRules.Faixa.BOA, ReacaoRules.faixaDe(11, modificador = 2))
        // E uma desvantagem pode empurrar para baixo.
        assertEquals(ReacaoRules.Faixa.RUIM, ReacaoRules.faixaDe(11, modificador = -2))
    }

    @Test
    fun `modificador grande nao estoura a tabela`() {
        assertEquals(ReacaoRules.Faixa.EXCELENTE, ReacaoRules.faixaDe(10, modificador = 20))
        assertEquals(ReacaoRules.Faixa.PESSIMA, ReacaoRules.faixaDe(10, modificador = -20))
    }

    @Test
    fun `toda faixa tem descricao util na mesa`() {
        ReacaoRules.Faixa.values().forEach { faixa ->
            val d = ReacaoRules.descricaoDa(faixa)
            assertTrue("faixa $faixa sem descricao", d.length > 10)
        }
    }

    // --- os modificadores da ficha ---

    @Test
    fun `sem traco social nao ha modificador nem painel`() {
        comEfeitos(emptyMap())
        val p = Personagem(nome = "Teste")
        assertTrue(ReacaoRules.modificadoresDe(p).isEmpty())
        assertEquals(0, ReacaoRules.totalDe(p))
    }

    @Test
    fun `Carisma soma por nivel`() {
        comEfeitos(mapOf("carisma" to listOf(
            EfeitoDeclarado(tipo = "pericia", alvo = "reacao", valor = 1, porNivel = true)
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "carisma", nome = "Carisma", nivel = 3))
        )
        assertEquals(3, ReacaoRules.totalDe(p))
        assertEquals("Carisma", ReacaoRules.modificadoresDe(p).first().nomeDoTraco)
    }

    @Test
    fun `desvantagem social subtrai`() {
        comEfeitos(mapOf(
            "carisma" to listOf(EfeitoDeclarado(tipo = "pericia", alvo = "reacao", valor = 2)),
            "odioso" to listOf(EfeitoDeclarado(tipo = "pericia", alvo = "reacao", valor = -3))
        ))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "carisma", nome = "Carisma")),
            desvantagens = listOf(DesvantagemSelecionada(definicaoId = "odioso", nome = "Odioso"))
        )
        assertEquals(-1, ReacaoRules.totalDe(p))
        assertEquals(2, ReacaoRules.modificadoresDe(p).size)
    }

    @Test
    fun `modificador CONDICIONAL nao entra no total`() {
        // Voz Melodiosa: +2 so de quem PODE OUVIR a voz. Somar sempre daria
        // bonus contra surdos e contra maquinas.
        comEfeitos(mapOf("voz" to listOf(
            EfeitoDeclarado(
                tipo = "pericia", alvo = "reacao", valor = 2,
                condicao = "de quem pode ouvir sua voz"
            )
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "voz", nome = "Voz Melodiosa"))
        )
        assertEquals(0, ReacaoRules.totalDe(p))
        // ...mas PRECISA aparecer como caixinha para o jogador marcar.
        val cond = ReacaoRules.condicionaisDe(p)
        assertEquals(1, cond.size)
        assertEquals(2, cond.first().valor)
        assertEquals("de quem pode ouvir sua voz", cond.first().condicao)
    }

    @Test
    fun `ficha SO com modificador condicional ainda mostra o painel`() {
        // O defeito visto no aparelho em 28/07: com Voz Melodiosa e mais nada,
        // `modificadoresDe` vinha vazia e o card sumia -- o +2 so aparecia no
        // resultado da rolagem, sem o jogador poder escolher.
        comEfeitos(mapOf("voz" to listOf(
            EfeitoDeclarado(tipo = "pericia", alvo = "reacao", valor = 2, condicao = "quem ouve")
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "voz", nome = "Voz Melodiosa"))
        )
        assertTrue(ReacaoRules.modificadoresDe(p).isEmpty())
        assertTrue(ReacaoRules.temAlgumModificador(p))
    }

    @Test
    fun `ficha sem traco nenhum nao mostra o painel`() {
        comEfeitos(emptyMap())
        assertTrue(!ReacaoRules.temAlgumModificador(Personagem(nome = "Teste")))
    }

    @Test
    fun `bonus condicional de PERICIA nao vira condicional de reacao`() {
        comEfeitos(mapOf("rosto" to listOf(
            EfeitoDeclarado(
                tipo = "pericia", alvo = "Dissimulação", valor = 1,
                condicao = "para parecer inocente"
            )
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "rosto", nome = "Rosto Sincero"))
        )
        assertTrue(ReacaoRules.condicionaisDe(p).isEmpty())
    }

    @Test
    fun `bonus de pericia normal nao vira modificador de reacao`() {
        comEfeitos(mapOf("pendulear" to listOf(
            EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 2)
        )))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(VantagemSelecionada(definicaoId = "pendulear", nome = "Pendulear"))
        )
        assertTrue(ReacaoRules.modificadoresDe(p).isEmpty())
    }

    @Test
    fun `a notinha diz de onde vem cada ponto`() {
        comEfeitos(mapOf(
            "a" to listOf(EfeitoDeclarado(tipo = "pericia", alvo = "reacao", valor = 2)),
            "b" to listOf(EfeitoDeclarado(tipo = "pericia", alvo = "reacao", valor = 1))
        ))
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(
                VantagemSelecionada(definicaoId = "a", nome = "Carisma"),
                VantagemSelecionada(definicaoId = "b", nome = "Aparência")
            )
        )
        val mods = ReacaoRules.modificadoresDe(p)
        assertEquals(listOf("Carisma", "Aparência"), mods.map { it.nomeDoTraco })
        assertEquals(listOf(2, 1), mods.map { it.valor })
    }
}
