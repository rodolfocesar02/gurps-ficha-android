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
    fun `piso de toque VENCE o enquadramento — hex nunca menor que o tocavel`() {
        // Lote TOK-6b-1 (feedback do usuário): enquadrar todos os alcançáveis de deslocamento 5+
        // deixava os hexes pequenos demais pro dedo. Com o piso, o zoom para no tocável e o resto
        // fica pro pan.
        val combatentes = listOf(HexCoord(0, 0), HexCoord(1, 0))
        val alcancaveis = (1..5).flatMap { d -> listOf(HexCoord(-d, 0), HexCoord(d, 0)) }
        val piso = 110f // px (~40dp em densidade típica)
        val cam = calcularCamera(combatentes + alcancaveis, LARG, ALT, 7, pisoToquePx = piso)
        assertTrue("tam (${cam.tam}) deve respeitar o piso de toque ($piso)", cam.tam >= piso)
    }

    @Test
    fun `pan gigante e CLAMPADO — o centro da camera nunca sai da grade`() {
        // Achado da revisão TOK-6b-1: sem clamp, arrastar demais deixava a viewport vazia
        // ("o combate sumiu"). O centro efetivo fica dentro da extensão axial da grade.
        val cam = cameraEfetiva(tam = 100f, ax = 0f, ay = 0f, panX = 1_000_000f, panY = -1_000_000f, raioGrade = 7)
        assertTrue(kotlin.math.abs(cam.centroAx) <= 7 * SQRT3 + 0.01f)
        assertTrue(kotlin.math.abs(cam.centroAy) <= 7 * 1.5f + 0.01f)
    }

    @Test
    fun `pan zero preserva a camera animada`() {
        val cam = cameraEfetiva(tam = 100f, ax = 2f, ay = -1f, panX = 0f, panY = 0f, raioGrade = 7)
        assertEquals(2f, cam.centroAx, 0.001f)
        assertEquals(-1f, cam.centroAy, 0.001f)
    }

    @Test
    fun `piso de toque nunca ultrapassa o teto da camera`() {
        val cam = calcularCamera(listOf(HexCoord(0, 0)), LARG, ALT, 7, pisoToquePx = 10_000f)
        val teto = minOf(LARG, ALT) / 7f
        assertTrue(cam.tam <= teto + 0.01f)
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

    // ---- Lote TOK-ZOOM: pinça de dois dedos ----

    @Test
    fun `zoom multiplica o tamanho do hex e o padrao 1f nao muda nada`() {
        val semZoom = cameraEfetiva(tam = 40f, ax = 0f, ay = 0f, panX = 0f, panY = 0f, raioGrade = 7)
        val comZoom = cameraEfetiva(tam = 40f, ax = 0f, ay = 0f, panX = 0f, panY = 0f, raioGrade = 7, zoom = 2f)
        assertEquals(40f, semZoom.tam, 0.001f)   // chamada antiga (default) é idêntica
        assertEquals(80f, comZoom.tam, 0.001f)
    }

    @Test
    fun `zoom e clampado entre o minimo e o maximo`() {
        val esmagado = cameraEfetiva(40f, 0f, 0f, 0f, 0f, 7, zoom = 0.01f)
        val estourado = cameraEfetiva(40f, 0f, 0f, 0f, 0f, 7, zoom = 99f)
        assertEquals(40f * ZOOM_MIN, esmagado.tam, 0.001f)
        assertEquals(40f * ZOOM_MAX, estourado.tam, 0.001f)
    }

    @Test
    fun `com zoom o mesmo arrasto em px anda MENOS hexes`() {
        // Comportamento de mapa: aproximado, o dedo percorre menos terreno. O pan é convertido
        // pelo tamanho JÁ ampliado — se dividisse pelo tam original, o mapa dispararia.
        val perto = cameraEfetiva(40f, 0f, 0f, panX = 80f, panY = 0f, raioGrade = 7, zoom = 2f)
        val longe = cameraEfetiva(40f, 0f, 0f, panX = 80f, panY = 0f, raioGrade = 7, zoom = 1f)
        assertEquals(-1f, perto.centroAx, 0.001f)  // 80px / (40*2)
        assertEquals(-2f, longe.centroAx, 0.001f)  // 80px / 40
    }

    @Test
    fun `com zoom o centro continua clampado a grade`() {
        val cam = cameraEfetiva(40f, 0f, 0f, panX = -99_000f, panY = 0f, raioGrade = 7, zoom = 4f)
        assertTrue("centro não pode escapar da grade", cam.centroAx <= 7 * SQRT3 + 0.001f)
    }

    @Test
    fun `toque continua acertando o hex certo sob zoom`() {
        // Round-trip: com a câmera ampliada, o centro do hex na tela deve voltar ao mesmo hex.
        val cam = cameraEfetiva(40f, 0f, 0f, 0f, 0f, 7, zoom = 2.5f)
        val alvo = HexCoord(2, -1)
        val (x, y) = hexParaTelaCam(alvo, cam, LARG, ALT)
        assertEquals(alvo, telaParaHexCam(x, y, cam, LARG, ALT, 7))
    }
}
