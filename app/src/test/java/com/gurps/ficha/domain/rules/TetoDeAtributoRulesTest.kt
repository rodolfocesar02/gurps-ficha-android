package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tetos de atributo (Lote TETO-HT, MB p.19).
 *
 * `Magro` limita a HT em 14; `Muito Gordo` em 13. Não é bônus nem penalidade —
 * é limite de criação, e por isso não cabe no campo `efeitos`, que só soma e
 * subtrai.
 *
 * O comportamento é **avisar, não impedir**: o jogador pode ter comprado a HT
 * antes da desvantagem, ou o Mestre pode ter liberado.
 */
class TetoDeAtributoRulesTest {

    private fun heroi(ht: Int, vararg desvantagens: String) = Personagem(
        nome = "Teste",
        vitalidade = ht,
        desvantagens = desvantagens.map {
            DesvantagemSelecionada(definicaoId = it, nome = it.replace('_', ' '))
        }
    )

    @Test
    fun `ficha sem desvantagem de peso nao tem teto`() {
        assertTrue(TetoDeAtributoRules.violacoes(heroi(ht = 20)).isEmpty())
    }

    @Test
    fun `Magro com HT dentro do teto nao gera aviso`() {
        assertTrue(TetoDeAtributoRules.violacoes(heroi(14, "magro")).isEmpty())
    }

    @Test
    fun `Magro com HT acima de 14 gera aviso`() {
        val v = TetoDeAtributoRules.violacoes(heroi(15, "magro"))
        assertEquals(1, v.size)
        assertEquals("HT", v.first().atributo)
        assertEquals(14, v.first().teto)
        assertEquals(15, v.first().valorAtual)
    }

    @Test
    fun `Muito Gordo tem o teto mais baixo`() {
        assertTrue(TetoDeAtributoRules.violacoes(heroi(13, "muito_gordo")).isEmpty())
        assertEquals(13, TetoDeAtributoRules.violacoes(heroi(14, "muito_gordo")).first().teto)
    }

    @Test
    fun `com as duas vale o teto MAIS BAIXO`() {
        // Ninguem e Magro e Muito Gordo ao mesmo tempo, mas se a ficha tiver as
        // duas o app nao pode escolher a mais generosa em silencio.
        val v = TetoDeAtributoRules.violacoes(heroi(14, "magro", "muito_gordo"))
        assertEquals(1, v.size)
        assertEquals(13, v.first().teto)
    }

    @Test
    fun `o aviso diz o traco, o teto e o valor atual`() {
        val aviso = TetoDeAtributoRules.violacoes(heroi(16, "magro")).first().aviso
        assertTrue(aviso, aviso.contains("14"))
        assertTrue(aviso, aviso.contains("16"))
        assertTrue(aviso, aviso.contains("HT"))
    }

    @Test
    fun `o teto olha a HT FINAL, nao os pontos gastos`() {
        // HT vem de vitalidade + racial + bonus de traco. O livro fala do valor
        // que o personagem TEM.
        val comRacial = heroi(12, "magro").copy(
            modeloRacial = com.gurps.ficha.model.ModeloRacial(modVitalidade = 4)
        )
        assertEquals(16, comRacial.ht)
        assertEquals(1, TetoDeAtributoRules.violacoes(comRacial).size)
    }
}
