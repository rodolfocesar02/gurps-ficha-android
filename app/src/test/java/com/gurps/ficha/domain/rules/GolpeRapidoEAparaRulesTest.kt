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
 * Golpe Rápido (MB p.371) e apara repetida (MB p.377).
 */
class GolpeRapidoEAparaRulesTest {

    private fun vant(id: String, nome: String) =
        VantagemSelecionada(definicaoId = id, nome = nome)

    private fun semNada() = Personagem(nome = "T")

    private fun comTbaM() = Personagem(
        nome = "T",
        vantagens = listOf(vant(GolpeRapidoEAparaRules.ID_TREINADO_POR_UM_MESTRE, "Treinado por um Mestre"))
    )

    private fun comMestreDeArmas() = Personagem(
        nome = "T",
        vantagens = listOf(vant(GolpeRapidoEAparaRules.ID_MESTRE_DE_ARMAS, "Mestre de Armas"))
    )

    // --- Golpe Rápido ---

    @Test
    fun `Golpe Rapido e menos 6 nos dois ataques`() {
        // MB p.371: "Ele desfere dois ataques, os dois com uma penalidade de -6."
        assertEquals(-6, GolpeRapidoEAparaRules.penalidadeGolpeRapido(semNada()))
    }

    @Test
    fun `com mestria, o Golpe Rapido cai para menos 3`() {
        // "Ele sofre metade da penalidade usual" -- vale para as DUAS vantagens.
        assertEquals(-3, GolpeRapidoEAparaRules.penalidadeGolpeRapido(comTbaM()))
        assertEquals(-3, GolpeRapidoEAparaRules.penalidadeGolpeRapido(comMestreDeArmas()))
    }

    @Test
    fun `o rotulo cita a vantagem que esta valendo`() {
        // Sem citar, o jogador ve -3 e nao sabe se e regra ou bug.
        val texto = GolpeRapidoEAparaRules.rotuloGolpeRapido(comTbaM())
        assertTrue(texto, texto.contains("-3") && texto.contains("Treinado por um Mestre"))
        assertNull(GolpeRapidoEAparaRules.nomeDaMestria(semNada()))
    }

    // --- apara repetida: os QUATRO casos do livro ---

    @Test
    fun `⚠️ os quatro degraus da apara repetida`() {
        // MB p.377: -4 normal; -2 com arma de esgrima OU com mestria; -1 com as
        // DUAS condicoes. Errar o caso do "-1" e o mais facil, porque exige
        // perceber que as duas coisas se acumulam.
        assertEquals("arma comum, sem vantagem", -4,
            GolpeRapidoEAparaRules.penalidadePorAparaExtra(semNada(), armaDeEsgrima = false))
        assertEquals("so esgrima", -2,
            GolpeRapidoEAparaRules.penalidadePorAparaExtra(semNada(), armaDeEsgrima = true))
        assertEquals("so mestria", -2,
            GolpeRapidoEAparaRules.penalidadePorAparaExtra(comTbaM(), armaDeEsgrima = false))
        assertEquals("as duas", -1,
            GolpeRapidoEAparaRules.penalidadePorAparaExtra(comTbaM(), armaDeEsgrima = true))
    }

    @Test
    fun `a PRIMEIRA apara do turno nao tem penalidade`() {
        assertEquals(0, GolpeRapidoEAparaRules.penalidadeAcumulada(semNada(), 1))
        assertEquals("nem com numero invalido", 0,
            GolpeRapidoEAparaRules.penalidadeAcumulada(semNada(), 0))
    }

    @Test
    fun `a penalidade e CUMULATIVA`() {
        // Segunda -4, terceira -8, quarta -12.
        assertEquals(-4, GolpeRapidoEAparaRules.penalidadeAcumulada(semNada(), 2))
        assertEquals(-8, GolpeRapidoEAparaRules.penalidadeAcumulada(semNada(), 3))
        assertEquals(-12, GolpeRapidoEAparaRules.penalidadeAcumulada(semNada(), 4))
    }

    @Test
    fun `esgrimista com mestria paga um quarto`() {
        // Quatro aparas no turno: -12 para o comum, -3 para este. E a conta que
        // ninguem faz na mesa.
        assertEquals(-3, GolpeRapidoEAparaRules.penalidadeAcumulada(comTbaM(), 4, armaDeEsgrima = true))
    }

    // --- quem é esgrima ---

    @Test
    fun `as quatro pericias de esgrima do livro`() {
        listOf("adaga_de_esgrima", "rapieira", "sabre", "tercado").forEach {
            assertTrue(it, GolpeRapidoEAparaRules.ehEsgrima(it))
        }
        listOf("espada_curta", "faca", "machado_ou_machadinha", "briga").forEach {
            assertFalse(it, GolpeRapidoEAparaRules.ehEsgrima(it))
        }
        assertFalse(GolpeRapidoEAparaRules.ehEsgrima(null))
        assertFalse(GolpeRapidoEAparaRules.ehEsgrima(""))
    }

    @Test
    fun `pericia racial de esgrima tambem conta`() {
        assertTrue(GolpeRapidoEAparaRules.ehEsgrima("racial_rapieira"))
    }

    @Test
    fun `mestria RACIAL tambem vale`() {
        val p = Personagem(
            nome = "T",
            modeloRacial = ModeloRacial(
                nome = "Espadachim",
                vantagens = listOf(vant(GolpeRapidoEAparaRules.ID_TREINADO_POR_UM_MESTRE, "Treinado por um Mestre"))
            )
        )
        assertTrue(GolpeRapidoEAparaRules.temMestria(p))
        assertEquals(-3, GolpeRapidoEAparaRules.penalidadeGolpeRapido(p))
    }

    @Test
    fun `o rotulo acessivel escreve menos, e nao diz se esta marcado`() {
        val texto = GolpeRapidoEAparaRules.rotuloAcessivelGolpeRapido(comTbaM())
        assertTrue(texto, texto.contains("menos 3"))
        // A convencao do projeto: quem anuncia o estado da caixinha e o TalkBack.
        listOf("Ativado", "Marcado", "ativado", "marcado").forEach {
            assertFalse("nao pode ecoar o estado: $it", texto.contains(it))
        }
    }
}
