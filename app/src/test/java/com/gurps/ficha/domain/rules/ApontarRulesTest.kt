package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Apontar, Precisão e Visão Telescópica (MB p.373 e p.99).
 */
class ApontarRulesTest {

    private fun comTelescopica(nivel: Int) = Personagem(
        nome = "T",
        vantagens = listOf(
            VantagemSelecionada(
                definicaoId = ApontarRules.ID_VISAO_TELESCOPICA,
                nome = "Visão Telescópica", nivel = nivel
            )
        )
    )

    private fun semNada() = Personagem(nome = "T")

    // --- Precisão ---

    @Test
    fun `a Precisao so vale se Apontou`() {
        // MB p.373: "o bônus que o personagem recebe SE executar uma ou mais
        // manobras Apontar imediatamente antes do ataque".
        assertEquals(3, ApontarRules.bonusDePrecisao(3, apontou = true))
        assertEquals(0, ApontarRules.bonusDePrecisao(3, apontou = false))
    }

    @Test
    fun `arma sem Precisao cadastrada nao inventa bonus`() {
        // Ficha anterior ao Lote 371 tem o campo nulo. Bonus inventado e pior que
        // nenhum, porque o jogador confiaria nele.
        assertEquals(0, ApontarRules.bonusDePrecisao(null, apontou = true))
        assertEquals(0, ApontarRules.bonusDePrecisao(-2, apontou = true))
    }

    // --- Visão Telescópica ---

    @Test
    fun `cada nivel ignora menos 1 de distancia, ou menos 2 se Apontou`() {
        // MB p.99: "ignorar -- a qualquer momento -- uma penalidade de distância
        // do alcance de -1 (...) ou de -2 se ele realizar uma manobra Apontar".
        val p = comTelescopica(3)
        assertEquals(3, ApontarRules.cancelaDaDistancia(p, -10, apontou = false))
        assertEquals(6, ApontarRules.cancelaDaDistancia(p, -10, apontou = true))
    }

    @Test
    fun `⚠️ ela desconta a DISTANCIA, e sobra nao vira bonus`() {
        // Visão Telescópica 3 num tiro a 3 metros (penalidade -1) cancela 1, não
        // 3. Tratá-la como bônus fixo daria +3 num tiro à queima-roupa, o que o
        // livro não concede.
        val p = comTelescopica(3)
        assertEquals(1, ApontarRules.cancelaDaDistancia(p, -1, apontou = false))
        assertEquals(1, ApontarRules.cancelaDaDistancia(p, -1, apontou = true))
        assertEquals("sem penalidade, nada a cancelar", 0,
            ApontarRules.cancelaDaDistancia(p, 0, apontou = true))
    }

    @Test
    fun `sem a vantagem, nao cancela nada`() {
        assertEquals(0, ApontarRules.cancelaDaDistancia(semNada(), -10, apontou = true))
        assertFalse(ApontarRules.temTelescopica(semNada()))
        assertTrue(ApontarRules.temTelescopica(comTelescopica(1)))
    }

    // --- as duas juntas ---

    @Test
    fun `Apontar soma a Precisao e o desconto da Telescopica`() {
        // Arco Prec 3, Visão Telescópica 2, alvo a 100 m (-10):
        // Precisão +3 e Telescópica 2x2 = +4 -> +7.
        val p = comTelescopica(2)
        assertEquals(7, ApontarRules.bonusTotalDoApontar(p, 3, -10, apontou = true))
        // Sem Apontar: nada de Precisão, e a Telescópica vale so 1 por nivel.
        assertEquals(2, ApontarRules.bonusTotalDoApontar(p, 3, -10, apontou = false))
    }

    @Test
    fun `o rotulo diz de onde vem cada pedaco`() {
        val texto = ApontarRules.rotuloApontar(comTelescopica(2), 3, -10)
        assertTrue(texto, texto.contains("Precisão +3") && texto.contains("Telescópica +4"))
    }

    @Test
    fun `arma sem Prec e sem Telescopica avisa em vez de mentir`() {
        val texto = ApontarRules.rotuloApontar(semNada(), null, -10)
        assertTrue(texto, texto.contains("não tem Precisão cadastrada"))
    }

    @Test
    fun `o rotulo acessivel escreve o total e nao ecoa o estado`() {
        val texto = ApontarRules.rotuloAcessivelApontar(comTelescopica(2), 3, -10)
        assertTrue(texto, texto.contains("mais 7"))
        listOf("Ativado", "Marcado", "ativado", "marcado").forEach {
            assertFalse("nao pode ecoar o estado: $it", texto.contains(it))
        }
    }
}
