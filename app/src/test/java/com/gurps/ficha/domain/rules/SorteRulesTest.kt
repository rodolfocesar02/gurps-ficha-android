package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.ModeloRacial
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sorte (MB p.90) — refazer duas vezes e ficar com o melhor dos três.
 */
class SorteRulesTest {

    private fun comSorte(custo: Int) = Personagem(
        nome = "T",
        vantagens = listOf(
            VantagemSelecionada(definicaoId = SorteRules.ID_SORTE, nome = "Sorte", custoEscolhido = custo)
        )
    )

    // --- ⚠️ a pegadinha da vantagem ---

    @Test
    fun `⚠️ num teste de habilidade, melhor e o MENOR total`() {
        // Num teste de pericia o jogador quer tirar POUCO. Programar "melhor =
        // maior" faria a Sorte PIORAR todos os testes do personagem -- e ele
        // levaria sessoes para desconfiar, porque a vantagem "funcionou" (rolou
        // tres vezes) e o resultado sai plausivel.
        assertEquals(6, SorteRules.melhorDe(listOf(14, 6, 11), ehDano = false))
    }

    @Test
    fun `⚠️ numa avaliacao de DANO, melhor e o MAIOR total`() {
        // O livro inclui "avaliacoes de dano" na Sorte, e ai o sinal inverte.
        assertEquals(14, SorteRules.melhorDe(listOf(14, 6, 11), ehDano = true))
    }

    @Test
    fun `sendo atacado, fica o PIOR do atacante`() {
        // "o jogador faz as jogadas tres vezes pelo atacante e fica com o pior
        // resultado" -- pior para o atacante e o total mais alto.
        assertEquals(14, SorteRules.piorDoAtacante(listOf(14, 6, 11)))
    }

    // --- os três graus ---

    @Test
    fun `os tres graus vem do custo pago`() {
        assertEquals(SorteRules.Grau.NORMAL, SorteRules.grauDe(comSorte(15)))
        assertEquals(SorteRules.Grau.EXTRAORDINARIA, SorteRules.grauDe(comSorte(30)))
        assertEquals(SorteRules.Grau.IMPOSSIVEL, SorteRules.grauDe(comSorte(60)))
    }

    @Test
    fun `cada grau tem seu relogio`() {
        assertEquals(60, SorteRules.Grau.NORMAL.minutos)
        assertEquals(30, SorteRules.Grau.EXTRAORDINARIA.minutos)
        assertEquals(10, SorteRules.Grau.IMPOSSIVEL.minutos)
    }

    @Test
    fun `custo fora da tabela cai no grau mais baixo, nao em nulo`() {
        // Ficha antiga com custoEscolhido = 0 tem a vantagem. Devolver null
        // deixaria o jogador com uma vantagem que ele nao consegue usar.
        assertEquals(SorteRules.Grau.NORMAL, SorteRules.grauDe(comSorte(0)))
    }

    @Test
    fun `sem a vantagem, nao tem grau nem painel`() {
        val p = Personagem(nome = "T")
        assertNull(SorteRules.grauDe(p))
        assertFalse(SorteRules.temAlguma(p))
        assertFalse(SorteRules.podeUsar(p, null))
    }

    // --- o relógio ---

    @Test
    fun `nunca usou, pode usar`() {
        assertTrue(SorteRules.podeUsar(comSorte(15), null))
        assertEquals(0L, SorteRules.minutosRestantes(comSorte(15), null))
    }

    @Test
    fun `⚠️ o exemplo do livro - as 11h58 e de novo as 12h01 nao pode`() {
        // "O personagem nao pode utilizar Sorte as 11:58 e novamente as 12:01" --
        // sao 3 minutos, e a Sorte normal exige 60.
        val p = comSorte(15)
        assertFalse(SorteRules.podeUsar(p, 3))
        assertEquals(57L, SorteRules.minutosRestantes(p, 3))
    }

    @Test
    fun `no minuto exato, ja libera`() {
        assertTrue(SorteRules.podeUsar(comSorte(15), 60))
        assertTrue(SorteRules.podeUsar(comSorte(30), 30))
        assertTrue(SorteRules.podeUsar(comSorte(60), 10))
    }

    @Test
    fun `a Impossivel libera onde a normal ainda espera`() {
        assertTrue(SorteRules.podeUsar(comSorte(60), 12))
        assertFalse(SorteRules.podeUsar(comSorte(15), 12))
    }

    @Test
    fun `Sorte RACIAL tambem conta`() {
        val p = Personagem(
            nome = "T",
            modeloRacial = ModeloRacial(
                nome = "Duende",
                vantagens = listOf(
                    VantagemSelecionada(
                        definicaoId = SorteRules.ID_SORTE, nome = "Sorte", custoEscolhido = 30
                    )
                )
            )
        )
        assertEquals(SorteRules.Grau.EXTRAORDINARIA, SorteRules.grauDe(p))
    }

    // --- os textos ---

    @Test
    fun `o botao diz quantos minutos faltam, em vez de so desabilitar`() {
        // Botao morto sem explicacao e bug aos olhos de quem usa.
        val texto = SorteRules.rotulo(comSorte(15), 20)
        assertTrue(texto, texto.contains("40 min"))
        assertTrue(SorteRules.rotulo(comSorte(15), null).startsWith("Usar"))
    }

    @Test
    fun `a explicacao do resultado nomeia as tres e a escolhida`() {
        val texto = SorteRules.explicacaoDoResultado(listOf(14, 6, 11), 6, ehDano = false)
        assertTrue(texto, texto.contains("14, 6, 11") && texto.contains("ficou 6") && texto.contains("menor"))
    }

    @Test
    fun `Super Sorte usa o mesmo painel`() {
        val p = Personagem(
            nome = "T",
            vantagens = listOf(
                VantagemSelecionada(definicaoId = SorteRules.ID_SUPER_SORTE, nome = "Super Sorte")
            )
        )
        assertTrue(SorteRules.temSuperSorte(p))
        assertTrue(SorteRules.temAlguma(p))
        // ...mas sem a Sorte comum nao ha grau, e o botao de refazer nao aparece:
        // Super Sorte e DITAR o resultado, nao rolar de novo.
        assertNull(SorteRules.grauDe(p))
    }
}
