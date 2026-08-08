package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lotes MB-5 e MB-3** — prender o fôlego e o Modificador de Tamanho do alvo.
 *
 * Duas regras pequenas, e cada uma tem exatamente **uma** armadilha:
 *
 * - No fôlego, a diferença entre parado e lutando é de **dez vezes** — quem
 *   decora só "HT×10" morre achando que tinha dois minutos.
 * - No tamanho, o **sinal é contraintuitivo**: MT positivo é alvo grande, e alvo
 *   grande é mais **fácil** de acertar.
 */
class FolegoETamanhoTest {

    // ==================================================================
    // MB-5 · Prender o fôlego
    // ==================================================================

    @Test
    fun `as tres linhas do livro, com HT 12`() {
        // MB p.356: HT×10 parado, HT×4 moderado, HT lutando.
        assertEquals(120, FolegoRules.segundos(12, FolegoRules.Esforco.NENHUM, FolegoRules.Preparo.NORMAL))
        assertEquals(48, FolegoRules.segundos(12, FolegoRules.Esforco.MODERADO, FolegoRules.Preparo.NORMAL))
        assertEquals(12, FolegoRules.segundos(12, FolegoRules.Esforco.PESADO, FolegoRules.Preparo.NORMAL))
    }

    @Test
    fun `🔴 lutando dura DEZ VEZES menos que parado`() {
        // É a razão de a regra existir na tela. Quem decora "HT vezes dez"
        // acha que tem dois minutos e tem doze segundos.
        val parado = FolegoRules.segundos(12, FolegoRules.Esforco.NENHUM, FolegoRules.Preparo.NORMAL)
        val lutando = FolegoRules.segundos(12, FolegoRules.Esforco.PESADO, FolegoRules.Preparo.NORMAL)
        assertEquals(parado, lutando * 10)
    }

    @Test
    fun `hiperventilar multiplica por 1,5 e o oxigenio puro por 2,5`() {
        val base = FolegoRules.segundos(10, FolegoRules.Esforco.NENHUM, FolegoRules.Preparo.NORMAL)
        assertEquals(100, base)
        assertEquals(150, FolegoRules.segundos(10, FolegoRules.Esforco.NENHUM, FolegoRules.Preparo.HIPERVENTILOU))
        assertEquals(250, FolegoRules.segundos(10, FolegoRules.Esforco.NENHUM, FolegoRules.Preparo.OXIGENIO_PURO))
    }

    @Test
    fun `⚠️ arredonda para BAIXO, sempre`() {
        // HT 7 lutando com hiperventilação: 7 × 1 × 1,5 = 10,5 → 10.
        // Meio segundo generoso numa conta de fôlego mata alguém na mesa.
        assertEquals(10, FolegoRules.segundos(7, FolegoRules.Esforco.PESADO, FolegoRules.Preparo.HIPERVENTILOU))
    }

    @Test
    fun `HT invalida devolve zero, nao numero negativo`() {
        assertEquals(0, FolegoRules.segundos(0, FolegoRules.Esforco.NENHUM, FolegoRules.Preparo.NORMAL))
        assertEquals(0, FolegoRules.segundos(-3, FolegoRules.Esforco.NENHUM, FolegoRules.Preparo.NORMAL))
    }

    @Test
    fun `o tempo tambem aparece em TURNOS, nao so em minutos`() {
        // 12 segundos são 12 turnos de combate, e é essa a leitura que importa
        // quando alguém está sendo estrangulado.
        val curto = FolegoRules.tempoLegivel(12)
        assertTrue(curto, curto.contains("12 turnos"))
        assertEquals("2 min (120 segundos)", FolegoRules.tempoLegivel(120))
        assertTrue(FolegoRules.tempoLegivel(90).contains("1 min e 30 s"))
    }

    @Test
    fun `a conta aparece escrita`() {
        val t = FolegoRules.explicacao(12, FolegoRules.Esforco.MODERADO, FolegoRules.Preparo.NORMAL)
        assertTrue(t, t.contains("HT 12 × 4"))
        assertTrue(t, t.contains("48"))
    }

    @Test
    fun `mais esforco NUNCA aguenta mais tempo`() {
        (1..20).forEach { ht ->
            FolegoRules.Preparo.values().forEach { prep ->
                val nenhum = FolegoRules.segundos(ht, FolegoRules.Esforco.NENHUM, prep)
                val moderado = FolegoRules.segundos(ht, FolegoRules.Esforco.MODERADO, prep)
                val pesado = FolegoRules.segundos(ht, FolegoRules.Esforco.PESADO, prep)
                assertTrue("ht=$ht prep=$prep", nenhum >= moderado)
                assertTrue("ht=$ht prep=$prep", moderado >= pesado)
            }
        }
    }

    // ==================================================================
    // MB-3 · Modificador de Tamanho do alvo
    // ==================================================================

    @Test
    fun `🔴 o sinal e o do ALVO, nao o do atirador`() {
        // MT positivo = alvo grande = mais FÁCIL de acertar. É contraintuitivo,
        // e é a única coisa que dá para errar nesta regra.
        assertEquals(2, TamanhoDoAlvoRules.modificadorNoAtaque(2))
        assertEquals(-4, TamanhoDoAlvoRules.modificadorNoAtaque(-4))
        val grande = TamanhoDoAlvoRules.rotulo(2, "carro")
        assertTrue(grande, grande.contains("mais fácil"))
        val pequeno = TamanhoDoAlvoRules.rotulo(-4, "gato")
        assertTrue(pequeno, pequeno.contains("mais difícil"))
    }

    @Test
    fun `o humano e o padrao, e nao vira modificador`() {
        val d = TamanhoDoAlvoRules.degrau(TamanhoDoAlvoRules.INDICE_PADRAO)
        assertEquals(0, d.mt)
        assertTrue(d.exemplo.contains("umano"))
        val r = TamanhoDoAlvoRules.rotulo(0, d.exemplo)
        assertTrue(r, r.contains("sem modificador"))
    }

    @Test
    fun `os degraus sobem sem pular e sem repetir`() {
        val mts = TamanhoDoAlvoRules.DEGRAUS.map { it.mt }
        assertEquals("os degraus não estão em ordem", mts.sorted(), mts)
        assertEquals("há MT repetido", mts.distinct().size, mts.size)
        mts.zipWithNext().forEach { (a, b) ->
            assertEquals("pulou de $a para $b", 1, b - a)
        }
    }

    @Test
    fun `todo degrau tem exemplo — MT sozinho nao diz nada`() {
        TamanhoDoAlvoRules.DEGRAUS.forEach {
            assertTrue("MT ${it.mt} sem exemplo", it.exemplo.isNotBlank())
            assertTrue("MT ${it.mt} sem rótulo", it.rotulo.contains(it.exemplo))
        }
    }

    @Test
    fun `o indice nao estoura quando o valor e absurdo`() {
        assertEquals(TamanhoDoAlvoRules.DEGRAUS.first(), TamanhoDoAlvoRules.degrau(-99))
        assertEquals(TamanhoDoAlvoRules.DEGRAUS.last(), TamanhoDoAlvoRules.degrau(999))
        // MT fora da lista cai no humano, que é o padrão seguro.
        assertEquals(TamanhoDoAlvoRules.INDICE_PADRAO, TamanhoDoAlvoRules.indiceDoMt(42))
    }

    @Test
    fun `ida e volta entre MT e indice`() {
        TamanhoDoAlvoRules.DEGRAUS.forEach {
            assertEquals(it.mt, TamanhoDoAlvoRules.degrau(TamanhoDoAlvoRules.indiceDoMt(it.mt)).mt)
        }
    }
}
