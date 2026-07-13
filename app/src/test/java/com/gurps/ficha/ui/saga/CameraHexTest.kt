package com.gurps.ficha.ui.saga

import com.gurps.ficha.domain.combat.hex.HexCoord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote TOK-6a: câmera do canvas tático — enquadrar combatentes, round-trip do toque. */
class CameraHexTest {

    private val LARG = 1080f
    private val ALT = 900f

    @Test
    fun `combate colado usa hexes MUITO maiores que a visao full-grid`() {
        val posicoes = listOf(HexCoord(0, 0), HexCoord(1, 0)) // herói + goblin adjacentes
        val cam = calcularCamera(posicoes, LARG, ALT, raioGrade = 7)
        val tamFull = tamanhoHex(LARG, ALT, 7)
        assertTrue("câmera (${cam.tam}) deve ampliar bem além do full-grid ($tamFull)",
            cam.tam > tamFull * 2.5f)
    }

    @Test
    fun `combate espalhado afasta a camera (tam menor que o colado)`() {
        val colado = calcularCamera(listOf(HexCoord(0, 0), HexCoord(1, 0)), LARG, ALT, 7)
        val espalhado = calcularCamera(listOf(HexCoord(-5, 0), HexCoord(6, 0)), LARG, ALT, 7)
        assertTrue(espalhado.tam < colado.tam)
    }

    @Test
    fun `camera nunca fica menor que a visao full-grid`() {
        // Pontos nos extremos opostos da grade raio 7.
        val cam = calcularCamera(listOf(HexCoord(-7, 0), HexCoord(7, 0)), LARG, ALT, 7)
        val tamFull = tamanhoHex(LARG, ALT, 7)
        assertTrue(cam.tam >= tamFull * 0.99f)
    }

    @Test
    fun `camera centraliza no meio dos combatentes`() {
        // Ambos em r=0: centroAy deve ser ~0; centroAx no meio dos dois.
        val cam = calcularCamera(listOf(HexCoord(2, 0), HexCoord(4, 0)), LARG, ALT, 7)
        assertEquals(0f, cam.centroAy, 0.01f)
        val axMeio = (SQRT3 * 2 + SQRT3 * 4) / 2f
        assertEquals(axMeio, cam.centroAx, 0.01f)
    }

    @Test
    fun `sem posicoes cai na visao full-grid centrada na origem`() {
        val cam = calcularCamera(emptyList(), LARG, ALT, 7)
        assertEquals(tamanhoHex(LARG, ALT, 7), cam.tam, 0.01f)
        assertEquals(0f, cam.centroAx, 0.01f)
        assertEquals(0f, cam.centroAy, 0.01f)
    }

    @Test
    fun `round-trip toque - hexParaTelaCam e telaParaHexCam sao inversos`() {
        val posicoes = listOf(HexCoord(0, 0), HexCoord(3, -1))
        val cam = calcularCamera(posicoes, LARG, ALT, 7)
        for (hex in listOf(HexCoord(0, 0), HexCoord(1, 0), HexCoord(2, -1), HexCoord(-1, 2))) {
            val (x, y) = hexParaTelaCam(hex, cam, LARG, ALT)
            val volta = telaParaHexCam(x, y, cam, LARG, ALT, raioGrade = 7)
            assertEquals("round-trip de $hex", hex, volta)
        }
    }

    @Test
    fun `toque fora da grade devolve null`() {
        val cam = calcularCamera(listOf(HexCoord(0, 0)), LARG, ALT, 7)
        // Um ponto MUITO longe do centro (fora do raio 7 da grade).
        val (x, y) = hexParaTelaCam(HexCoord(30, 0), cam, LARG, ALT)
        assertNull(telaParaHexCam(x, y, cam, LARG, ALT, raioGrade = 7))
    }

    @Test
    fun `camera com tam invalido nao crasha o toque`() {
        assertNull(telaParaHexCam(100f, 100f, CameraHex(0f, 0f, 0f), LARG, ALT, 7))
    }

    @Test
    fun `hexes alcancaveis incluidos ABREM a camera — todos ficam dentro da tela`() {
        // Achado da revisão TOK-6a: com deslocamento 5 > margem 2,5, os hexes verdes na direção
        // oposta aos inimigos ficavam FORA da viewport e intocáveis. Incluir os alcançáveis no
        // enquadramento garante que TODO hex tocável esteja na tela.
        val combatentes = listOf(HexCoord(0, 0), HexCoord(1, 0))
        val alcancaveis = listOf(HexCoord(-5, 0), HexCoord(-4, 0), HexCoord(0, -5)) // recuo p/ longe
        val camSem = calcularCamera(combatentes, LARG, ALT, 7)
        val camCom = calcularCamera(combatentes + alcancaveis, LARG, ALT, 7)
        assertTrue("incluir alcançáveis deve abrir a câmera", camCom.tam < camSem.tam)
        // Todo alcançável cai DENTRO da viewport com a câmera nova.
        for (hex in alcancaveis) {
            val (x, y) = hexParaTelaCam(hex, camCom, LARG, ALT)
            assertTrue("hex $hex deve estar visível (x=$x)", x in 0f..LARG)
            assertTrue("hex $hex deve estar visível (y=$y)", y in 0f..ALT)
        }
    }
}
