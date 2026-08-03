package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lote MIRA-4** — Apontar por mais de um segundo (MB p.364 e p.373).
 *
 * ## O que faltava
 *
 * Achado por você em 31/07: o Apontar era **liga/desliga**. O livro deixa
 * acumular, e o app perdia duas coisas:
 *
 * > Se Apontar por mais de um segundo o personagem recebe um bônus adicional:
 * > **+1**, se Apontar por dois segundos, ou **+2** se Apontar por três ou mais.
 *
 * > Se **firmar** uma arma de fogo ou besta o personagem recebe um bônus
 * > adicional de **+1** na Prec.
 *
 * ## 🔴 E com isso o teto do livro passou a existir
 *
 * > A soma do bônus de Precisão com os demais bônus de pontaria nunca podem
 * > exceder o **dobro** do parâmetro Prec da arma. — MB p.373
 *
 * O KDoc antigo do `ApontarRules` dizia, com todas as letras, que o teto *"hoje
 * é o Prec de um turno, sempre abaixo do dobro"*. Era verdade — **enquanto só
 * existia um turno**. Este lote é exatamente o que transforma aquele comentário
 * em regra viva, e é por isso que ele tem teste próprio.
 */
class ApontarAcumuladoTest {

    private fun semNada() = Personagem(nome = "T")

    private fun comTelescopica(nivel: Int) = Personagem(
        nome = "T",
        vantagens = listOf(
            VantagemSelecionada(
                definicaoId = ApontarRules.ID_VISAO_TELESCOPICA,
                nome = "Visão Telescópica", nivel = nivel
            )
        )
    )

    // ==================================================================
    // 1. Os segundos
    // ==================================================================

    @Test
    fun `o primeiro turno nao da extra - ele libera a Precisao`() {
        // O +Prec já é o prêmio do primeiro segundo. Dar +1 nele seria contar
        // duas vezes o mesmo turno.
        assertEquals(0, ApontarRules.bonusPorTurnos(1))
        assertEquals(2, ApontarRules.bonusDePontaria(precisaoDaArma = 2, turnos = 1, armaFirmada = false))
    }

    @Test
    fun `dois segundos dao mais 1, tres ou mais dao mais 2`() {
        assertEquals(1, ApontarRules.bonusPorTurnos(2))
        assertEquals(2, ApontarRules.bonusPorTurnos(3))
        assertEquals("do quarto em diante nada muda", 2, ApontarRules.bonusPorTurnos(9))
    }

    @Test
    fun `sem apontar, nada disso vale`() {
        assertEquals(0, ApontarRules.bonusPorTurnos(0))
        assertEquals(0, ApontarRules.bonusDePontaria(precisaoDaArma = 3, turnos = 0, armaFirmada = true))
    }

    @Test
    fun `o contador cicla e volta ao zero`() {
        var t = 0
        val visitados = mutableListOf<Int>()
        repeat(4) { t = ApontarRules.proximoTurno(t); visitados += t }
        assertEquals(listOf(1, 2, 3, 0), visitados)
    }

    // ==================================================================
    // 2. A arma firmada
    // ==================================================================

    @Test
    fun `arma firmada soma mais 1`() {
        // Prec 3, um turno: 3. Firmada: 4. (E 4 <= 2×3, então o teto não morde.)
        assertEquals(3, ApontarRules.bonusDePontaria(3, turnos = 1, armaFirmada = false))
        assertEquals(4, ApontarRules.bonusDePontaria(3, turnos = 1, armaFirmada = true))
    }

    @Test
    fun `firmar sem apontar nao vale nada`() {
        // O livro diz "bônus adicional **na Prec**", e a Prec só existe apontando.
        assertEquals(0, ApontarRules.bonusDePontaria(3, turnos = 0, armaFirmada = true))
    }

    // ==================================================================
    // 3. 🔴 O teto do dobro da Precisão
    // ==================================================================

    @Test
    fun `🔴 o teto do livro CORTA quando tudo se soma`() {
        // Prec 2, três segundos e firmada: 2 + 2 + 1 = 5. O livro trava em 4.
        // É o caso que só passou a existir com este lote.
        assertEquals(4, ApontarRules.bonusDePontaria(2, turnos = 3, armaFirmada = true))
    }

    @Test
    fun `com Prec alta o teto nao morde`() {
        // Prec 5: bruto 5 + 2 + 1 = 8, teto 10. Passa inteiro.
        assertEquals(8, ApontarRules.bonusDePontaria(5, turnos = 3, armaFirmada = true))
    }

    @Test
    fun `o teto e exatamente o DOBRO, nunca mais que isso`() {
        // Varre Prec de 1 a 6 no pior caso (3 segundos + firmada).
        (1..6).forEach { prec ->
            val total = ApontarRules.bonusDePontaria(prec, turnos = 3, armaFirmada = true)
            assertTrue("Prec $prec passou do dobro: $total", total <= prec * 2)
        }
    }

    @Test
    fun `⚠️ sem Prec cadastrado nao ha teto a aplicar`() {
        // Dobro de um número que não se conhece não existe. Os extras entram sem
        // corte e o rótulo avisa — inventar um teto tiraria bônus a que o
        // jogador tem direito.
        assertEquals(3, ApontarRules.bonusDePontaria(null, turnos = 3, armaFirmada = true))
        val rotulo = ApontarRules.rotuloApontar(semNada(), null, 0, turnos = 2, armaFirmada = false)
        assertTrue(rotulo, rotulo.contains("não tem Precisão cadastrada"))
    }

    // ==================================================================
    // 4. A Visão Telescópica continua num eixo separado
    // ==================================================================

    @Test
    fun `⚠️ a Telescopica NAO entra no teto de pontaria`() {
        // Ela não soma no NH: cancela penalidade de DISTÂNCIA (MB p.99). Cortá-la
        // pelo teto misturaria duas contas que o livro mantém separadas.
        //
        // Prec 2 + 3 segundos + firmada = 5, cortado para 4. A Telescópica 2 com
        // Apontar cancela mais 4 da distância −6. Total 8 — e o teto de 4 vale
        // só para a primeira metade.
        val total = ApontarRules.bonusTotalDoApontar(
            comTelescopica(2), precisaoDaArma = 2, penalidadeDistancia = -6,
            turnos = 3, armaFirmada = true
        )
        assertEquals(4 + 4, total)
    }

    @Test
    fun `a assinatura antiga continua valendo como UM turno`() {
        // Quem ainda chama a versão booleana (ou um teste antigo) recebe
        // exatamente o comportamento de antes: um turno, sem arma firmada.
        val p = comTelescopica(2)
        assertEquals(
            ApontarRules.bonusTotalDoApontar(p, 2, -6, turnos = 1, armaFirmada = false),
            ApontarRules.bonusTotalDoApontar(p, 2, -6, apontou = true)
        )
    }

    // ==================================================================
    // 5. O rótulo conta de onde vem cada ponto
    // ==================================================================

    @Test
    fun `o rotulo mostra as parcelas separadas`() {
        val r = ApontarRules.rotuloApontar(semNada(), 3, 0, turnos = 2, armaFirmada = true)
        assertTrue(r, r.contains("Precisão +3"))
        assertTrue(r, r.contains("segundos +1"))
        assertTrue(r, r.contains("firmada +1"))
    }

    @Test
    fun `o rotulo avisa quando o teto CORTOU`() {
        // Bônus que para de subir sem explicação parece defeito.
        val cortado = ApontarRules.rotuloApontar(semNada(), 2, 0, turnos = 3, armaFirmada = true)
        assertTrue(cortado, cortado.contains("teto de 4"))
        assertTrue(cortado, cortado.contains("p.373"))
        // E fica calado quando não cortou nada.
        val inteiro = ApontarRules.rotuloApontar(semNada(), 5, 0, turnos = 3, armaFirmada = true)
        assertTrue(inteiro, !inteiro.contains("teto"))
    }

    @Test
    fun `o rotulo do TalkBack diz o que o proximo toque faz`() {
        // O Apontar virou contador; "marcado" sozinho não diria que ainda há
        // segundos a acumular.
        val meio = ApontarRules.rotuloAcessivelApontar(semNada(), 2, 0, turnos = 1)
        assertTrue(meio, meio.contains("acumula mais um segundo"))
        val cheio = ApontarRules.rotuloAcessivelApontar(semNada(), 2, 0, turnos = 3)
        assertTrue(cheio, cheio.contains("volta a nenhum turno"))
    }

    @Test
    fun `apontar nunca PIORA o ataque, em nenhuma combinacao`() {
        // Varredura: o Apontar só soma. Um sinal trocado em qualquer parcela
        // apareceria aqui.
        listOf(null, 0, 1, 2, 3, 5).forEach { prec ->
            (0..4).forEach { turnos ->
                listOf(false, true).forEach { firmada ->
                    assertTrue(
                        "prec=$prec turnos=$turnos firmada=$firmada",
                        ApontarRules.bonusDePontaria(prec, turnos, firmada) >= 0
                    )
                }
            }
        }
    }

    @Test
    fun `mais segundos NUNCA dao menos bonus`() {
        listOf(null, 1, 2, 3, 5).forEach { prec ->
            listOf(false, true).forEach { firmada ->
                (0..3).zipWithNext().forEach { (menos, mais) ->
                    assertTrue(
                        "prec=$prec firmada=$firmada: $mais deu menos que $menos",
                        ApontarRules.bonusDePontaria(prec, mais, firmada) >=
                            ApontarRules.bonusDePontaria(prec, menos, firmada)
                    )
                }
            }
        }
    }
}
