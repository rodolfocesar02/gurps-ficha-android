package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A luz da cena (MB p.395/549) e os deslocamentos especiais (MB p.91/99).
 */
class IluminacaoEDeslocamentoTest {

    private fun vant(id: String, nivel: Int = 1) =
        VantagemSelecionada(definicaoId = id, nome = id, nivel = nivel)

    private fun com(vararg v: VantagemSelecionada) =
        Personagem(nome = "T", destreza = 10, vitalidade = 10, vantagens = v.toList())

    // --- iluminação ---

    @Test
    fun `boa luz nao penaliza ninguem`() {
        assertEquals(0, IluminacaoRules.penalidadeEfetiva(com(), 0).efetiva)
    }

    @Test
    fun `sem vantagem, a escuridao entra inteira`() {
        assertEquals(-7, IluminacaoRules.penalidadeEfetiva(com(), -7).efetiva)
    }

    @Test
    fun `o exemplo do livro - Visao Noturna 4 leva menos 7 para menos 3`() {
        // MB p.97: "Visão Noturna 4 eliminaria completamente as penalidades de
        // uma escuridão de até -4 — e reduziria uma penalidade de -7 para
        // apenas -3."
        val p = com(vant(IluminacaoRules.ID_VISAO_NOTURNA, 4))
        assertEquals(-3, IluminacaoRules.penalidadeEfetiva(p, -7).efetiva)
        assertEquals("ate -4 ela zera", 0, IluminacaoRules.penalidadeEfetiva(p, -4).efetiva)
    }

    @Test
    fun `⚠️ na escuridao TOTAL a Visao Noturna nao vale nada`() {
        // MB p.97: "não surte nenhum efeito sobre a penalidade de -10 de uma
        // escuridão total (...) funciona apenas em situações de escuridão
        // parcial". Deixar ela valer aqui daria a uma vantagem de 9 pontos o
        // trabalho de uma de 25.
        val p = com(vant(IluminacaoRules.ID_VISAO_NOTURNA, 9))
        val r = IluminacaoRules.penalidadeEfetiva(p, IluminacaoRules.ESCURIDAO_TOTAL)
        assertEquals(-10, r.efetiva)
        assertEquals(0, r.cancelado)
        assertTrue(r.explicacao.contains("não vale aqui"))
    }

    @Test
    fun `Visao no Escuro cancela tudo, inclusive a escuridao total`() {
        val p = com(vant(IluminacaoRules.ID_VISAO_NO_ESCURO))
        assertEquals(0, IluminacaoRules.penalidadeEfetiva(p, -10).efetiva)
        assertEquals(0, IluminacaoRules.penalidadeEfetiva(p, -5).efetiva)
    }

    @Test
    fun `Ultravisao soma com a Visao Noturna, como o livro manda`() {
        // MB p.96: "ignorar até -2 em penalidades por escuridão (cumulativo com
        // Visão Noturna)".
        val p = com(vant(IluminacaoRules.ID_VISAO_NOTURNA, 3), vant(IluminacaoRules.ID_ULTRAVISAO))
        assertEquals(-1, IluminacaoRules.penalidadeEfetiva(p, -6).efetiva)
        // ...mas nao na escuridao total.
        assertEquals(-10, IluminacaoRules.penalidadeEfetiva(p, -10).efetiva)
    }

    @Test
    fun `⚠️ vantagem sobrando NAO vira bonus`() {
        // Visão Noturna 9 na penumbra de -1 tem de dar 0, nunca +8.
        val p = com(vant(IluminacaoRules.ID_VISAO_NOTURNA, 9))
        val r = IluminacaoRules.penalidadeEfetiva(p, -1)
        assertEquals(0, r.efetiva)
        assertEquals(1, r.cancelado)
    }

    @Test
    fun `o nivel de Visao Noturna respeita o teto de 9 do livro`() {
        val p = com(vant(IluminacaoRules.ID_VISAO_NOTURNA, 20))
        assertEquals(9, IluminacaoRules.nivelVisaoNoturna(p))
    }

    @Test
    fun `a luz fica presa entre 0 e menos 10`() {
        assertEquals(0, IluminacaoRules.penalidadeEfetiva(com(), 5).efetiva)
        assertEquals(-10, IluminacaoRules.penalidadeEfetiva(com(), -99).efetiva)
    }

    @Test
    fun `a explicacao mostra a conta`() {
        // Sem a conta o jogador le "-3" onde escolheu "-7" e nao sabe por que.
        val p = com(vant(IluminacaoRules.ID_VISAO_NOTURNA, 4))
        val texto = IluminacaoRules.penalidadeEfetiva(p, -7).explicacao
        assertTrue(texto, texto.contains("-7") && texto.contains("Visão Noturna 4") && texto.contains("-3"))
    }

    @Test
    fun `o rotulo acessivel escreve menos em vez do sinal`() {
        val p = com(vant(IluminacaoRules.ID_VISAO_NOTURNA, 2))
        val texto = IluminacaoRules.descricaoAcessivel(p, -6)
        assertTrue(texto, texto.contains("menos 4"))
        assertFalse("nao pode ter o sinal grafico", texto.contains("-4"))
    }

    // --- deslocamentos especiais ---

    @Test
    fun `voando e a Velocidade Basica vezes dois, sem fracao`() {
        // MB p.99: "igual à sua Velocidade Básica × 2; descarte todas as
        // frações". DX 10 + HT 10 = Velocidade 5,00 -> 10.
        val p = com(vant(DeslocamentosEspeciais.ID_VOO))
        assertEquals(5.0f, p.velocidadeBasica, 0.001f)
        assertEquals(10, DeslocamentosEspeciais.deslocamentoVoando(p))
        assertTrue(DeslocamentosEspeciais.podeVoar(p))
    }

    @Test
    fun `⚠️ a fracao e CORTADA, nao arredondada`() {
        // Velocidade 5,75 -> 11,5 -> 11. Arredondar daria 12, e o livro manda
        // descartar.
        val p = Personagem(
            nome = "T", destreza = 13, vitalidade = 10,
            vantagens = listOf(vant(DeslocamentosEspeciais.ID_VOO))
        )
        assertEquals(5.75f, p.velocidadeBasica, 0.001f)
        assertEquals(11, DeslocamentosEspeciais.deslocamentoVoando(p))
    }

    @Test
    fun `Super Escalada soma ao Deslocamento`() {
        val p = com(vant(DeslocamentosEspeciais.ID_SUPER_ESCALADA, 3))
        assertEquals(3, DeslocamentosEspeciais.bonusEscalada(p))
        assertEquals(p.deslocamentoBasico + 3, DeslocamentosEspeciais.deslocamentoEscalando(p))
        assertTrue(DeslocamentosEspeciais.temSuperEscalada(p))
    }

    @Test
    fun `sem as vantagens, as linhas nao aparecem`() {
        val p = com()
        assertFalse(DeslocamentosEspeciais.podeVoar(p))
        assertFalse(DeslocamentosEspeciais.temSuperEscalada(p))
        assertFalse(IluminacaoRules.temAlgumaVantagem(p))
    }

    @Test
    fun `vantagem RACIAL de voo e de escalada tambem conta`() {
        val p = Personagem(
            nome = "T", destreza = 10, vitalidade = 10,
            modeloRacial = com.gurps.ficha.model.ModeloRacial(
                nome = "Ave",
                vantagens = listOf(vant(DeslocamentosEspeciais.ID_VOO), vant(IluminacaoRules.ID_VISAO_NOTURNA, 5))
            )
        )
        assertTrue(DeslocamentosEspeciais.podeVoar(p))
        assertEquals(5, IluminacaoRules.nivelVisaoNoturna(p))
    }
}
