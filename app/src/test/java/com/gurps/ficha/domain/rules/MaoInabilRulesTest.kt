package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mão inábil e Ambidestria (Lote MAO-1, MB p.14 e p.41).
 *
 * O ponto do lote: a Ambidestria **não concede bônus** — ela remove uma
 * penalidade. Declará-la como efeito daria −4 a quem comprou a vantagem, que é
 * o oposto do livro. Por isso a penalidade vive na situação (qual mão está
 * empunhando) e a vantagem apenas a zera.
 */
class MaoInabilRulesTest {

    private val semAmbidestria = Personagem(nome = "Teste")
    private val comAmbidestria = Personagem(
        nome = "Teste",
        vantagens = listOf(VantagemSelecionada(definicaoId = "ambidestria", nome = "Ambidestria"))
    )

    @Test
    fun `mao habil nunca tem penalidade`() {
        assertEquals(0, MaoInabilRules.penalidadeDe(semAmbidestria, usandoMaoInabil = false))
        assertEquals(0, MaoInabilRules.penalidadeDe(comAmbidestria, usandoMaoInabil = false))
    }

    @Test
    fun `mao inabil custa menos 4 sem a vantagem`() {
        assertEquals(-4, MaoInabilRules.penalidadeDe(semAmbidestria, usandoMaoInabil = true))
    }

    @Test
    fun `Ambidestria zera a penalidade da mao inabil`() {
        assertEquals(0, MaoInabilRules.penalidadeDe(comAmbidestria, usandoMaoInabil = true))
    }

    @Test
    fun `a Ambidestria NUNCA vira bonus positivo`() {
        // A armadilha que o lote existe para evitar: se alguem declarasse a
        // vantagem como efeito, ela mexeria no numero em vez de zerar a
        // penalidade. Aqui o teto e zero, nos dois estados.
        listOf(true, false).forEach { inabil ->
            assertTrue(
                "com Ambidestria a penalidade nao pode ser positiva",
                MaoInabilRules.penalidadeDe(comAmbidestria, inabil) <= 0
            )
        }
        assertEquals(0, MaoInabilRules.penalidadeDe(comAmbidestria, true))
    }

    @Test
    fun `o rotulo mostra o numero so quando ele existe`() {
        assertEquals("Mão hábil", MaoInabilRules.rotuloDe(semAmbidestria, false))
        assertTrue(MaoInabilRules.rotuloDe(semAmbidestria, true).contains("-4"))
    }

    @Test
    fun `com Ambidestria o rotulo EXPLICA por que o numero sumiu`() {
        // Sem a explicacao o jogador acha que o app esqueceu de aplicar.
        val texto = MaoInabilRules.rotuloDe(comAmbidestria, usandoMaoInabil = true)
        assertTrue(texto, texto.contains("Ambidestria"))
        assertTrue("nao pode mostrar numero", !texto.contains("-4"))
    }

    @Test
    fun `a descricao acessivel diz por extenso`() {
        val d = MaoInabilRules.rotuloAcessivel(semAmbidestria)
        assertTrue(d, d.contains("menos 4"))
        assertTrue("nao pode vazar o sinal cru", !d.contains("-4"))

        val comVantagem = MaoInabilRules.rotuloAcessivel(comAmbidestria)
        assertTrue(comVantagem, comVantagem.contains("Ambidestria"))
    }

    @Test
    fun `a descricao acessivel NAO repete o estado da caixinha`() {
        // O TalkBack ja anuncia "marcada"/"nao marcada" pelo papel de checkbox.
        // Repetir aqui virava eco -- corrigido na auditoria de 28/07.
        listOf(
            MaoInabilRules.rotuloAcessivel(semAmbidestria),
            MaoInabilRules.rotuloAcessivel(comAmbidestria)
        ).forEach { d ->
            assertTrue(d, !d.contains("Ativado") && !d.contains("Desativado"))
            assertTrue(d, !d.lowercase().contains("marcad"))
        }
    }
}
