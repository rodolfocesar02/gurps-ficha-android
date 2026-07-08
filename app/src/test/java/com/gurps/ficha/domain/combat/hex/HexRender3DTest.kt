package com.gurps.ficha.domain.combat.hex

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sqrt

/** Lote HEX-7: projeção hex → mundo 3D (metros/radianos). Kotlin puro, sem SceneView. */
class HexRender3DTest {

    private fun assertClose(esperado: Float, real: Float, tol: Float = 0.001f) {
        assertTrue("esperado=$esperado real=$real (dif=${kotlin.math.abs(esperado - real)})",
            kotlin.math.abs(esperado - real) < tol)
    }

    @Test
    fun `origem projeta para 0 zero e 0 zero`() {
        val (x, z) = HexRender3D.hexParaMundo(HexCoord.ORIGEM)
        assertClose(0f, x)
        assertClose(0f, z)
    }

    @Test
    fun `distancia entre vizinhos e sempre 1 metro`() {
        // Para cada uma das 6 direcoes, o vizinho em axial deve estar a 1m em euclidiano no plano XZ.
        for (d in Direcao.values()) {
            val vizinho = HexCoord.ORIGEM + d.vetor
            val (x, z) = HexRender3D.hexParaMundo(vizinho)
            val distMetros = sqrt((x * x + z * z).toDouble()).toFloat()
            assertClose(1f, distMetros, tol = 0.001f)
        }
    }

    @Test
    fun `LESTE gera yaw zero`() {
        val yaw = HexRender3D.facingParaYaw(Direcao.LESTE)
        assertClose(0f, yaw)
    }

    @Test
    fun `SUDESTE gera yaw mais PI sobre 3`() {
        val yaw = HexRender3D.facingParaYaw(Direcao.SUDESTE)
        assertClose((PI / 3).toFloat(), yaw)
    }

    @Test
    fun `OESTE gera yaw PI ou menos PI (mesmo ponto na circunferencia)`() {
        val yaw = HexRender3D.facingParaYaw(Direcao.OESTE)
        // atan2(0, -1) devolve pi. Toleramos qualquer coisa que seja PI +- 2*PI equivalente.
        val ok = kotlin.math.abs(yaw - PI.toFloat()) < 0.001f ||
                 kotlin.math.abs(yaw + PI.toFloat()) < 0.001f
        assertTrue("OESTE deveria ser +-PI, foi $yaw", ok)
    }

    @Test
    fun `projetar herei em cenario padrao`() {
        val estado = HexCombatState.setupInicial()
        val tokens = HexRender3D.projetar(estado, idHeroi = "heroi", idsInimigos = setOf("goblin_1"))
        assertEquals(2, tokens.size)
        val heroi = tokens.first { it.id == "heroi" }
        val goblin = tokens.first { it.id == "goblin_1" }
        assertEquals(HexRender3D.Cor.HEROI, heroi.cor)
        assertEquals(HexRender3D.Cor.INIMIGO, goblin.cor)
        // Goblin em (3,0) → x=3, z=0.
        assertClose(3f, goblin.x)
        assertClose(0f, goblin.z)
    }

    @Test
    fun `combatente fora do conjunto de inimigos vira ALIADO por default`() {
        val estado = HexCombatState(posicoes = listOf(
            PosicaoCombatente("heroi", HexCoord.ORIGEM, Direcao.LESTE),
            PosicaoCombatente("clerigo_amigo", HexCoord(1, 0), Direcao.OESTE)
        ))
        val tokens = HexRender3D.projetar(estado, idHeroi = "heroi", idsInimigos = emptySet())
        val amigo = tokens.first { it.id == "clerigo_amigo" }
        assertEquals(HexRender3D.Cor.ALIADO, amigo.cor)
    }

    @Test
    fun `ordem dos tokens segue estado posicoes para render deterministico`() {
        val estado = HexCombatState(posicoes = listOf(
            PosicaoCombatente("z", HexCoord.ORIGEM, Direcao.LESTE),
            PosicaoCombatente("a", HexCoord(1, 0), Direcao.LESTE),
            PosicaoCombatente("m", HexCoord(2, 0), Direcao.LESTE)
        ))
        val tokens = HexRender3D.projetar(estado)
        assertEquals(listOf("z", "a", "m"), tokens.map { it.id })
    }

    @Test
    fun `facing diferente gera yaw diferente para mesma posicao`() {
        val a = HexRender3D.facingParaYaw(Direcao.LESTE)
        val b = HexRender3D.facingParaYaw(Direcao.SUDOESTE)
        assertNotEquals(a, b)
    }
}
