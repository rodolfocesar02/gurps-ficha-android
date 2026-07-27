package com.gurps.ficha.ui.features.rolagem

import com.gurps.ficha.ui.PendingRollState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre o texto mostrado ao fim de uma rolagem de dados 3D e o que o TalkBack
 * anuncia.
 *
 * Antes do Lote UI-2 essa lógica vivia dentro do composable da `TabRolagem` e
 * era intestável — só dava para conferir rolando dados no aparelho. Ao extrair
 * o overlay, ela virou função pura e passou a ter teste.
 *
 * Importa porque é aqui que se decide se o jogador vê "Sucesso por 3" ou
 * "Falha Crítica" — errar isso é errar o resultado da jogada na cara dele.
 */
class TextoDoResultadoTest {

    private fun teste(alvo: Int?, mod: Int = 0, label: String = "Espada Larga") =
        PendingRollState(contextoLabel = label, alvo = alvo, mod = mod)

    private fun dano(mod: Int = 0, label: String = "Machado") =
        PendingRollState(contextoLabel = label, alvo = null, mod = mod, isDano = true)

    private fun livre(mod: Int = 0, label: String = "1d6") =
        PendingRollState(contextoLabel = label, alvo = null, mod = mod, isPersonalizada = true)

    // --- teste contra NH ---

    @Test
    fun `soma abaixo do NH e sucesso com a margem`() {
        // NH 14, rolou 10 -> passou por 4
        assertEquals(
            "Sucesso\n(por 4)",
            textoDoResultado(teste(alvo = 14), soma = 10, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
        )
    }

    @Test
    fun `soma acima do NH e falha com a margem`() {
        assertEquals(
            "Falha\n(por 3)",
            textoDoResultado(teste(alvo = 10), soma = 13, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
        )
    }

    @Test
    fun `soma igual ao NH ainda e sucesso`() {
        // GURPS: rolar exatamente o NH passa (margem 0).
        assertEquals(
            "Sucesso\n(por 0)",
            textoDoResultado(teste(alvo = 12), soma = 12, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
        )
    }

    @Test
    fun `3 e sempre sucesso critico`() {
        assertTrue(
            textoDoResultado(teste(alvo = 10), soma = 3, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
                .startsWith("Sucesso Crítico!")
        )
    }

    @Test
    fun `18 e sempre falha critica`() {
        assertTrue(
            textoDoResultado(teste(alvo = 18), soma = 18, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
                .startsWith("Falha Crítica!")
        )
    }

    // --- modificadores ---

    @Test
    fun `modificador do teste desloca o NH efetivo`() {
        // NH 10 com -3 vira 7; rolou 8 -> falha por 1
        assertEquals(
            "Falha\n(por 1)",
            textoDoResultado(teste(alvo = 10, mod = -3), soma = 8, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
        )
    }

    @Test
    fun `modificador global so conta na variante PraCego`() {
        val pr = teste(alvo = 10)
        // Visual ignora o modificador global: NH 10, rolou 12 -> falha por 2
        assertEquals(
            "Falha\n(por 2)",
            textoDoResultado(pr, soma = 12, isPraCegoVariant = false, modificadorGlobalPraCego = 5)
        )
        // PraCego aplica: NH 15, rolou 12 -> sucesso por 3
        assertEquals(
            "Sucesso\n(por 3)",
            textoDoResultado(pr, soma = 12, isPraCegoVariant = true, modificadorGlobalPraCego = 5)
        )
    }

    // --- dano e rolagem livre ---

    @Test
    fun `dano soma o modificador`() {
        assertEquals(
            "Dano: 9",
            textoDoResultado(dano(mod = 2), soma = 7, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
        )
    }

    @Test
    fun `dano nunca fica abaixo de 1`() {
        // GURPS: ataque que penetra causa no mínimo 1 ponto.
        assertEquals(
            "Dano: 1",
            textoDoResultado(dano(mod = -10), soma = 3, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
        )
    }

    @Test
    fun `rolagem livre pode ser negativa`() {
        // Diferente de dano: aqui não há piso.
        assertEquals(
            "Resultado: -2",
            textoDoResultado(livre(mod = -5), soma = 3, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
        )
    }

    @Test
    fun `teste sem NH so mostra a soma`() {
        assertEquals(
            "Rolagem: 11",
            textoDoResultado(teste(alvo = null), soma = 11, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
        )
    }

    // --- anúncio do TalkBack ---

    @Test
    fun `anuncio cita o rotulo e o NH efetivo`() {
        assertEquals(
            "Espada Larga (NH 14). Passou por 4",
            anuncioDoResultado(teste(alvo = 14), soma = 10, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
        )
    }

    @Test
    fun `anuncio de dano cita o rotulo`() {
        assertEquals(
            "Machado causou 9 de Dano",
            anuncioDoResultado(dano(mod = 2), soma = 7, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
        )
    }

    @Test
    fun `anuncio e texto da tela concordam no veredito`() {
        // Formatos diferentes de propósito (a tela é curta, a voz é por extenso),
        // mas não podem discordar sobre passar ou falhar.
        val pr = teste(alvo = 12)
        val tela = textoDoResultado(pr, soma = 9, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
        val voz = anuncioDoResultado(pr, soma = 9, isPraCegoVariant = false, modificadorGlobalPraCego = 0)
        assertTrue(tela.startsWith("Sucesso"))
        assertTrue(voz.contains("Passou"))
    }
}
