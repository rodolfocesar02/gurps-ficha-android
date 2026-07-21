package com.gurps.ficha.domain.engine

import com.gurps.ficha.model.ModeloRacial
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lote AM-0: a aba de Magias sumia de quem comprava **Aptidão Mágica 0**.
 *
 * Causa: o gate usava o BÔNUS de NH (`getNivelAptidaoMagicaParaMagia`), que vale 0 em AM 0, como se
 * fosse "tem a vantagem?". Mas AM 0 é justamente o pré-requisito para aprender magia — *"Representa
 * uma 'consciência mágica' básica, um pré-requisito para se aprender magia na maior parte dos
 * mundos. 5 pontos."* (MB p.41).
 *
 * O nível INTERNO da vantagem é 1-based: interno 1 = AM 0 (5 pts), interno 2 = AM 1, interno 3 =
 * AM 2. É por isso que o bônus é `nivel - 1` e a UI exibe `nivel - 1`.
 */
class MagicEngineAptidaoTest {

    private fun comAptidao(nivelInterno: Int) = Personagem(
        vantagens = listOf(
            VantagemSelecionada(
                definicaoId = "aptidao_magica",
                nome = "Aptidao Magica",
                nivel = nivelInterno
            )
        )
    )

    @Test
    fun `AM 0 conta como TER a vantagem — e o bonus continua zero`() {
        val p = comAptidao(nivelInterno = 1) // interno 1 = AM 0
        assertTrue("quem pagou os 5 pontos de AM 0 tem a vantagem",
            MagicEngine.possuiAptidaoMagica(p))
        assertEquals("mas AM 0 nao da bonus de NH", 0,
            MagicEngine.getNivelAptidaoMagicaParaMagia(p, null))
    }

    @Test
    fun `sem a vantagem nao tem aptidao`() {
        assertFalse(MagicEngine.possuiAptidaoMagica(Personagem()))
    }

    @Test
    fun `AM 3 tem a vantagem e da bonus 3`() {
        val p = comAptidao(nivelInterno = 4) // interno 4 = AM 3
        assertTrue(MagicEngine.possuiAptidaoMagica(p))
        assertEquals(3, MagicEngine.getNivelAptidaoMagicaParaMagia(p, null))
    }

    @Test
    fun `nivel interno zero (ficha antiga ou importada) ainda conta como ter a vantagem`() {
        // Defesa contra dado sujo: se algo gravou nivel 0, a presenca da vantagem manda.
        val p = comAptidao(nivelInterno = 0)
        assertTrue(MagicEngine.possuiAptidaoMagica(p))
        assertEquals("bonus nao pode ficar negativo", 0,
            MagicEngine.getNivelAptidaoMagicaParaMagia(p, null))
    }

    @Test
    fun `aptidao vinda do MODELO RACIAL tambem habilita`() {
        // Elfo/dragão do MB vêm com "Aptidão Mágica 0 [5]" na raça — tem que valer igual.
        val p = Personagem()
        p.modeloRacial = ModeloRacial(
            vantagens = listOf(
                VantagemSelecionada(
                    definicaoId = "aptidao_magica",
                    nome = "Aptidao Magica",
                    nivel = 1
                )
            )
        )
        assertTrue("AM racial habilita magia como a comprada",
            MagicEngine.possuiAptidaoMagica(p))
    }
}
