package com.gurps.ficha.domain.rules

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **O teto de níveis dos traços** (Lote TETO-1).
 *
 * ## O bug, e o quanto ele era maior do que parecia
 *
 * Achado por você no aparelho, testando o T-LI6: a **Mão Fraca** trava em 3
 * níveis (MB p.151) e a perícia obedecia — parava em −6 —, mas o **seletor**
 * deixava subir sem limite. O jogador comprava o nível 6, pagava **−30 pontos**
 * e recebia os mesmos −6.
 *
 * ⚠️ **A causa não era da Mão Fraca.** O campo `max` existe nos dois catálogos,
 * é lido do JSON pelo loader e era **descartado** na conversão para
 * `VantagemDefinicao`/`DesvantagemDefinicao`. Os quatro seletores da tela usavam
 * **20 fixo no código**.
 *
 * Ou seja: **os dez Talentos** (`max: 4`, e há um teste afirmando isso porque o
 * livro diz *"nunca pode ter mais que quatro níveis"*, p.91) e a
 * **Suscetibilidade à Magia** (`max: 5`, p.159) estavam no mesmo barco. O teto
 * estava declarado, testado, e não chegava à tela.
 *
 * A placa de velocidade estava pregada na estrada e o radar não lia nenhuma.
 */
class TetoDeNivelDoTracoTest {

    // ==================================================================
    // 1. A ordem de decisão
    // ==================================================================

    @Test
    fun `o max do catalogo manda, quando existe`() {
        assertEquals(3, TetoDeNivelDoTraco.de("mao_fraca", 3))
        assertEquals(4, TetoDeNivelDoTraco.de("artifice", 4))
        assertEquals(5, TetoDeNivelDoTraco.de("susceptibilidade_a_magia", 5))
    }

    @Test
    fun `sem max no catalogo, vale o teto geral de tela`() {
        assertEquals(TetoDeNivelDoTraco.TETO_GERAL, TetoDeNivelDoTraco.de("destemor", null))
        assertEquals(20, TetoDeNivelDoTraco.TETO_GERAL)
    }

    @Test
    fun `a Aptidao Magica tem teto proprio, que nao vem do catalogo`() {
        assertEquals(11, TetoDeNivelDoTraco.de("aptidao_magica", null))
        assertEquals(11, TetoDeNivelDoTraco.de("APTIDAO_MAGICA", null))
    }

    @Test
    fun `⚠️ o catalogo VENCE ate a Aptidao Magica`() {
        // Se um dia o catálogo declarar o teto dela, é o catálogo que manda —
        // senão a constante no código viraria a fonte da verdade, e o dado
        // deixaria de valer.
        assertEquals(6, TetoDeNivelDoTraco.de("aptidao_magica", 6))
    }

    @Test
    fun `max invalido no catalogo e ignorado, e nao trava o traco em zero`() {
        // Um `max: 0` num JSON escrito à mão travaria a compra do traço inteiro.
        // Preferir o fallback a obedecer um número impossível.
        assertEquals(TetoDeNivelDoTraco.TETO_GERAL, TetoDeNivelDoTraco.de("x", 0))
        assertEquals(TetoDeNivelDoTraco.TETO_GERAL, TetoDeNivelDoTraco.de("x", -3))
    }

    // ==================================================================
    // 2. O ajuste que o seletor usa
    // ==================================================================

    @Test
    fun `o seletor para no teto e nao passa dele`() {
        (1..3).forEach { assertEquals(it, TetoDeNivelDoTraco.ajustar(it, "mao_fraca", 3)) }
        assertEquals("o 4 vira 3", 3, TetoDeNivelDoTraco.ajustar(4, "mao_fraca", 3))
        assertEquals("e o 99 tambem", 3, TetoDeNivelDoTraco.ajustar(99, "mao_fraca", 3))
    }

    @Test
    fun `o seletor nunca desce abaixo de 1`() {
        // Nível 0 significaria não ter o traço, e quem quer isso remove da ficha.
        assertEquals(1, TetoDeNivelDoTraco.ajustar(0, "mao_fraca", 3))
        assertEquals(1, TetoDeNivelDoTraco.ajustar(-5, "destemor", null))
    }

    @Test
    fun `ajustar e idempotente - aplicar duas vezes nao muda`() {
        (-5..30).forEach { n ->
            val uma = TetoDeNivelDoTraco.ajustar(n, "mao_fraca", 3)
            assertEquals(uma, TetoDeNivelDoTraco.ajustar(uma, "mao_fraca", 3))
        }
    }

    // ==================================================================
    // 3. O aviso na tela
    // ==================================================================

    @Test
    fun `o aviso aparece so quando o teto e do LIVRO`() {
        // Sem ele, o jogador toca no + , não acontece nada, e conclui que
        // travou. Mas dizer "máximo 20" seria expor um detalhe de tela como se
        // fosse regra do livro.
        val aviso = TetoDeNivelDoTraco.avisoDoTeto("mao_fraca", 3)
        assertTrue(aviso!!, aviso.contains("3"))
        assertTrue(aviso, aviso.contains("níveis"))
        assertNull(TetoDeNivelDoTraco.avisoDoTeto("destemor", null))
    }

    @Test
    fun `a Aptidao Magica tambem avisa, porque 11 e do livro`() {
        assertTrue(TetoDeNivelDoTraco.avisoDoTeto("aptidao_magica", null)!!.contains("11"))
    }

    @Test
    fun `com teto de 1 o texto vai para o singular`() {
        assertTrue(TetoDeNivelDoTraco.avisoDoTeto("x", 1)!!.contains("1 nível"))
    }

    // ==================================================================
    // 4. Contra os catálogos reais
    // ==================================================================

    private data class TracoCru(val id: String = "", val nome: String = "", val max: Int? = null)

    private fun ler(nome: String): List<TracoCru> {
        val direto = File("src/main/assets/$nome")
        val arquivo = if (direto.exists()) direto else File("app/src/main/assets/$nome")
        val tipo = object : TypeToken<List<TracoCru>>() {}.type
        return Gson().fromJson(arquivo.readText(Charsets.UTF_8), tipo)
    }

    @Test
    fun `🔴 os dez Talentos param em 4, como o livro manda`() {
        // MB p.91: "nunca pode ter mais que quatro níveis em um determinado
        // Talento". O `max: 4` já estava no catálogo E já havia teste sobre
        // ele — mas o número não chegava ao seletor. Este teste fecha o
        // caminho inteiro: catálogo → regra.
        val talentos = setOf(
            "artifice", "artista_talentoso", "companheiro_animal", "curandeiro",
            "dedos_verdes", "agente_cativante", "explorador",
            "habilidade_matematica", "habilidade_musical", "perspicacia_comercial"
        )
        val porId = ler("vantagens.v3.json").associateBy { it.id }
        talentos.forEach { id ->
            val t = porId[id]
            assertEquals("$id sem max no catalogo", 4, t?.max)
            assertEquals("$id nao para em 4", 4, TetoDeNivelDoTraco.de(id, t?.max))
        }
    }

    @Test
    fun `Mao Fraca para em 3 e Suscetibilidade a Magia em 5`() {
        val desv = ler("desvantagens.v2.json").associateBy { it.id }
        assertEquals(3, TetoDeNivelDoTraco.de("mao_fraca", desv["mao_fraca"]?.max))
        assertEquals(
            5,
            TetoDeNivelDoTraco.de(
                "susceptibilidade_a_magia", desv["susceptibilidade_a_magia"]?.max
            )
        )
    }

    @Test
    fun `nenhum max do catalogo e absurdo`() {
        // Varre os dois catálogos: um `max` zerado ou gigante seria erro de
        // digitação, e o de baixo travaria a compra do traço.
        (ler("vantagens.v3.json") + ler("desvantagens.v2.json"))
            .filter { it.max != null }
            .forEach {
                assertTrue(
                    "${it.nome} [${it.id}] com max = ${it.max}",
                    it.max!! in 1..TetoDeNivelDoTraco.TETO_GERAL
                )
            }
    }
}
