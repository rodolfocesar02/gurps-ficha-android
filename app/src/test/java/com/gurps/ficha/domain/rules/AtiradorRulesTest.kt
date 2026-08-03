package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lotes ARMA-8 e ARMA-9** — Atirador e Arqueiro Heroico.
 *
 * As duas estavam no catálogo com a descrição inteira e **zero automação**. Este
 * arquivo trava as três coisas que é fácil errar ao ler o livro rápido:
 *
 * 1. O arredondamento da metade é **para cima** (Prec 5 → 3, não 2).
 * 2. Avançar e Atacar e a Precisão de graça são **exclusivos** — o livro diz
 *    *"em vez de"* duas vezes.
 * 3. O Arqueiro Heroico acumula os segundos de Apontar **um turno mais cedo**.
 *
 * E a exclusão que ninguém lembra: *"nenhum desses benefícios se aplica quando o
 * personagem está usando **armas motoras de projétil**"* — arco, besta e funda
 * ficam fora do **Atirador**.
 */
class AtiradorRulesTest {

    private fun com(vararg ids: String) = Personagem(
        nome = "T",
        vantagens = ids.map { VantagemSelecionada(definicaoId = it, nome = it, nivel = 1) }
    )

    private val atirador = com(AtiradorRules.ID_ATIRADOR)
    private val arqueiro = com(AtiradorRules.ID_ARQUEIRO_HEROICO)
    private val semNada = Personagem(nome = "T")

    private val PISTOLA = "armas_de_fogo_nt_pistola"
    private val ARCO = "arcos"

    // ==================================================================
    // 1. Quem tem direito a quê
    // ==================================================================

    @Test
    fun `o Atirador vale nas QUATRO pericias do livro`() {
        listOf(
            "armas_de_fogo_nt_pistola",
            "armas_de_feixe_nt_pistola",
            "canhoneiro_nt_canhao",
            "projetor_de_liquidos_nt"
        ).forEach {
            assertEquals(
                "$it deveria valer",
                AtiradorRules.Estilo.ATIRADOR, AtiradorRules.estiloDe(atirador, it)
            )
        }
    }

    @Test
    fun `⚠️ o Atirador NAO vale em arma motora de projetil`() {
        // MB p.43, a exclusão explícita. É o erro mais provável de quem lê rápido:
        // "é à distância, logo vale" — e não é.
        listOf("arco", "arcos", "besta", "funda", "zarabatana", "arremesso").forEach {
            assertEquals(
                "$it não podia valer para o Atirador",
                AtiradorRules.Estilo.NENHUM, AtiradorRules.estiloDe(atirador, it)
            )
        }
    }

    @Test
    fun `o Arqueiro Heroico vale no arco, e so nele`() {
        assertEquals(AtiradorRules.Estilo.ARQUEIRO_HEROICO, AtiradorRules.estiloDe(arqueiro, ARCO))
        assertEquals(AtiradorRules.Estilo.NENHUM, AtiradorRules.estiloDe(arqueiro, "besta"))
        assertEquals(AtiradorRules.Estilo.NENHUM, AtiradorRules.estiloDe(arqueiro, PISTOLA))
    }

    @Test
    fun `sem a vantagem, nada muda`() {
        assertEquals(AtiradorRules.Estilo.NENHUM, AtiradorRules.estiloDe(semNada, PISTOLA))
        assertEquals(AtiradorRules.Estilo.NENHUM, AtiradorRules.estiloDe(null, PISTOLA))
        assertEquals(0, AtiradorRules.precisaoSemApontar(AtiradorRules.Estilo.NENHUM, 6, false, 3))
        assertNull(AtiradorRules.rotulo(AtiradorRules.Estilo.NENHUM, 6, false, 3, false, false))
    }

    @Test
    fun `pericia racial tambem conta`() {
        assertEquals(
            AtiradorRules.Estilo.ATIRADOR,
            AtiradorRules.estiloDe(atirador, "racial_armas_de_fogo_nt_pistola")
        )
    }

    // ==================================================================
    // 2. 🔴 A Precisão sem Apontar
    // ==================================================================

    @Test
    fun `🔴 pistola de uma mao com CdT 3 da a Precisao INTEIRA`() {
        val A = AtiradorRules.Estilo.ATIRADOR
        assertEquals(2, AtiradorRules.precisaoSemApontar(A, precisao = 2, duasMaos = false, cadenciaTiro = 3))
    }

    @Test
    fun `🔴 arma de duas maos da METADE, arredondada PARA CIMA`() {
        val A = AtiradorRules.Estilo.ATIRADOR
        // Prec 6 → 3. Prec 5 → 3, não 2. É a pegadinha do arredondamento.
        assertEquals(3, AtiradorRules.precisaoSemApontar(A, 6, duasMaos = true, cadenciaTiro = 1))
        assertEquals(3, AtiradorRules.precisaoSemApontar(A, 5, duasMaos = true, cadenciaTiro = 1))
        assertEquals(1, AtiradorRules.precisaoSemApontar(A, 1, duasMaos = true, cadenciaTiro = 1))
    }

    @Test
    fun `🔴 arma automatica tambem da metade, mesmo de uma mao`() {
        val A = AtiradorRules.Estilo.ATIRADOR
        // CdT 1–3 é "um tiro por vez". Acima disso é automática (MB p.43).
        assertEquals(4, AtiradorRules.precisaoSemApontar(A, 4, duasMaos = false, cadenciaTiro = 3))
        assertEquals(2, AtiradorRules.precisaoSemApontar(A, 4, duasMaos = false, cadenciaTiro = 9))
        assertTrue(AtiradorRules.aplicouMetade(A, duasMaos = false, cadenciaTiro = 16))
        assertTrue(!AtiradorRules.aplicouMetade(A, duasMaos = false, cadenciaTiro = 1))
    }

    @Test
    fun `⚠️ o Arqueiro Heroico NAO leva metade, mesmo com o arco de duas maos`() {
        val H = AtiradorRules.Estilo.ARQUEIRO_HEROICO
        assertEquals(3, AtiradorRules.precisaoSemApontar(H, 3, duasMaos = true, cadenciaTiro = 1))
        assertTrue(!AtiradorRules.aplicouMetade(H, duasMaos = true, cadenciaTiro = 1))
    }

    @Test
    fun `arma sem Precisao cadastrada nao ganha bonus do nada`() {
        val A = AtiradorRules.Estilo.ATIRADOR
        assertEquals(0, AtiradorRules.precisaoSemApontar(A, null, false, 1))
        assertEquals(0, AtiradorRules.precisaoSemApontar(A, 0, false, 1))
        val r = AtiradorRules.rotulo(A, null, false, 1, false, false)
        assertTrue(r!!, r.contains("não tem Precisão cadastrada"))
    }

    // ==================================================================
    // 3. 🔴 A troca: "em vez de receber o bônus da Prec"
    // ==================================================================

    @Test
    fun `🔴 marcar Avancar e Atacar TIRA a Precisao de graca`() {
        val A = AtiradorRules.Estilo.ATIRADOR
        val parado = AtiradorRules.bonusNoAtaque(A, 2, false, 3, avancarEAtacar = false, apontou = false)
        val correndo = AtiradorRules.bonusNoAtaque(A, 2, false, 3, avancarEAtacar = true, apontou = false)
        assertEquals(2, parado)
        assertEquals("o livro diz 'em vez de', não 'além de'", 0, correndo)
        // E a penalidade da manobra some — é a outra metade da troca.
        assertTrue(AtiradorRules.ignoraAvancarEAtacar(A))
        val r = AtiradorRules.rotulo(A, 2, false, 3, avancarEAtacar = true, apontou = false)
        assertTrue(r!!, r.contains("ignorada"))
    }

    @Test
    fun `⚠️ apontando, a Precisao NAO entra duas vezes`() {
        // Quem aponta já recebe a Prec pelo `ApontarRules`. Somar aqui de novo
        // dobraria o bônus sem que nada na tela explicasse por quê.
        val A = AtiradorRules.Estilo.ATIRADOR
        assertEquals(0, AtiradorRules.bonusNoAtaque(A, 6, false, 1, avancarEAtacar = false, apontou = true))
        val r = AtiradorRules.rotulo(A, 6, false, 1, false, apontou = true)
        assertTrue(r!!, r.contains("bônus cheio"))
    }

    // ==================================================================
    // 4. 🔴 Os segundos do Arqueiro Heroico
    // ==================================================================

    @Test
    fun `🔴 o Arqueiro acumula os segundos UM TURNO mais cedo`() {
        val H = AtiradorRules.Estilo.ARQUEIRO_HEROICO
        // Regra geral: 1 turno = 0, 2 = +1, 3+ = +2.
        assertEquals(0, ApontarRules.bonusPorTurnos(1))
        assertEquals(1, ApontarRules.bonusPorTurnos(2))
        // Arqueiro Heroico: 1 turno = +1, 2+ = +2.
        assertEquals(1, AtiradorRules.bonusPorTurnos(H, 1))
        assertEquals(2, AtiradorRules.bonusPorTurnos(H, 2))
        assertEquals(2, AtiradorRules.bonusPorTurnos(H, 3))
        assertEquals(0, AtiradorRules.bonusPorTurnos(H, 0))
    }

    @Test
    fun `o Atirador comum mantem o ritmo do livro`() {
        val A = AtiradorRules.Estilo.ATIRADOR
        (0..4).forEach { t ->
            assertEquals("turno $t", ApontarRules.bonusPorTurnos(t), AtiradorRules.bonusPorTurnos(A, t))
        }
    }

    @Test
    fun `o Arqueiro nunca acumula MENOS que a regra geral`() {
        val H = AtiradorRules.Estilo.ARQUEIRO_HEROICO
        (0..5).forEach { t ->
            assertTrue(
                "turno $t",
                AtiradorRules.bonusPorTurnos(H, t) >= ApontarRules.bonusPorTurnos(t)
            )
        }
    }

    // ==================================================================
    // 5. Varreduras
    // ==================================================================

    @Test
    fun `a vantagem nunca PIORA o ataque`() {
        listOf(AtiradorRules.Estilo.ATIRADOR, AtiradorRules.Estilo.ARQUEIRO_HEROICO).forEach { e ->
            listOf(null, 0, 1, 2, 5, 6, 12).forEach { prec ->
                listOf(false, true).forEach { duas ->
                    listOf(null, 1, 3, 9, 16).forEach { cdt ->
                        listOf(false, true).forEach { avancar ->
                            listOf(false, true).forEach { apontou ->
                                assertTrue(
                                    "$e prec=$prec duas=$duas cdt=$cdt",
                                    AtiradorRules.bonusNoAtaque(e, prec, duas, cdt, avancar, apontou) >= 0
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `a metade nunca passa do bonus inteiro`() {
        val A = AtiradorRules.Estilo.ATIRADOR
        (1..15).forEach { prec ->
            val inteiro = AtiradorRules.precisaoSemApontar(A, prec, duasMaos = false, cadenciaTiro = 1)
            val metade = AtiradorRules.precisaoSemApontar(A, prec, duasMaos = true, cadenciaTiro = 1)
            assertTrue("prec $prec", metade <= inteiro)
            assertTrue("prec $prec: metade caiu abaixo do arredondamento", metade * 2 >= prec)
        }
    }
}
