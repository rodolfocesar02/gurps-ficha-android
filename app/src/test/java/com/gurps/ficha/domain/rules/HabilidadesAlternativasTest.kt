package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.poderes.HabilidadesAlternativas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Habilidades Alternativas** — GURPS Poderes, p.11. Lote POD-6.
 *
 * O livro dá um exemplo trabalhado, e ele é o teste central: se a conta do app
 * não reproduzir os 44 pontos do Jocko, ela está errada — não importa quão
 * plausível pareça.
 */
class HabilidadesAlternativasTest {

    @Test
    fun `o exemplo do livro fecha em 44`() {
        // "Jocko possui Voo (PC, -10%) [36], Super Salto 2 (PC, -10%) [18], e
        // Caminhar no Ar (PC, -10%) [18]. (…) Cada um deles custa 18/5 = 3,6
        // pontos, que arredondados para cima dão 4. O custo final é 36+4+4 = 44."
        assertEquals(44, HabilidadesAlternativas.custoDoGrupo(listOf(36, 18, 18)))
    }

    @Test
    fun `a ordem nao muda o resultado`() {
        // A mais cara é a mais cara, esteja ela onde estiver na lista.
        assertEquals(44, HabilidadesAlternativas.custoDoGrupo(listOf(18, 36, 18)))
        assertEquals(44, HabilidadesAlternativas.custoDoGrupo(listOf(18, 18, 36)))
    }

    @Test
    fun `arredonda para CIMA, nunca para baixo`() {
        // ⚠️ 18/5 = 3,6. Para baixo daria 3, e o total viraria 42 — plausível e
        // errado. Como o desconto é de 80%, o erro passa despercebido.
        assertEquals(4, HabilidadesAlternativas.custoDaAlternativa(18))
        assertEquals(2, HabilidadesAlternativas.custoDaAlternativa(6))
        assertEquals(2, HabilidadesAlternativas.custoDaAlternativa(10))
        assertEquals(3, HabilidadesAlternativas.custoDaAlternativa(11))
    }

    @Test
    fun `a alternativa barata nao vira de graca`() {
        // 1/5 de 4 é 0,8 — arredondando para cima, 1. O desconto não zera nada.
        listOf(1, 2, 3, 4, 5).forEach {
            assertEquals("custo $it deveria virar 1", 1, HabilidadesAlternativas.custoDaAlternativa(it))
        }
    }

    @Test
    fun `duas habilidades de mesmo custo pagam uma cheia e uma barata`() {
        // 🔴 O erro fácil: tratar TODAS as de custo máximo como principal, e
        // cobrar as duas cheias. O livro diz "sua habilidade mais cara", no
        // singular — uma só.
        assertEquals(20 + 4, HabilidadesAlternativas.custoDoGrupo(listOf(20, 20)))
        assertEquals(20 + 4 + 4, HabilidadesAlternativas.custoDoGrupo(listOf(20, 20, 20)))
    }

    @Test
    fun `uma habilidade so custa o preco dela`() {
        assertEquals(36, HabilidadesAlternativas.custoDoGrupo(listOf(36)))
        assertEquals(0, HabilidadesAlternativas.custoDoGrupo(emptyList()))
    }

    @Test
    fun `a economia e a diferenca para comprar separado`() {
        // 36+18+18 = 72 comprando solto; 44 no grupo.
        assertEquals(72 - 44, HabilidadesAlternativas.economia(listOf(36, 18, 18)))
        assertEquals(0, HabilidadesAlternativas.economia(listOf(36)))
    }

    @Test
    fun `grupo precisa de duas ou mais`() {
        assertFalse(HabilidadesAlternativas.ehGrupoValido(0))
        assertFalse(HabilidadesAlternativas.ehGrupoValido(1))
        assertTrue(HabilidadesAlternativas.ehGrupoValido(2))
        val r = HabilidadesAlternativas.resumo(listOf(36))
        assertTrue(r, r.contains("duas ou mais"))
    }

    @Test
    fun `custo negativo passa intacto`() {
        // Desvantagem dentro de um grupo não recebe "desconto" — dividir um
        // número negativo por 5 daria crédito, que é o oposto do que o livro quer.
        assertEquals(-10, HabilidadesAlternativas.custoDaAlternativa(-10))
    }

    @Test
    fun `sao TRES inconvenientes, nao dois`() {
        // 🔴 O plano registrou dois. O terceiro estava logo depois de onde a
        // minha leitura tinha parado — achado só na 2ª revisão.
        assertEquals(3, HabilidadesAlternativas.INCONVENIENTES.size)
        val todos = HabilidadesAlternativas.INCONVENIENTES.joinToString(" ")
        assertTrue("falta o de trocar com Preparar", todos.contains("Preparar"))
        assertTrue("falta o de uma derrubar o conjunto", todos.contains("conjunto"))
        assertTrue("falta o da duracao trancar todas", todos.contains("duração"))
    }

    @Test
    fun `o resumo diz o total, o solto e a economia`() {
        val r = HabilidadesAlternativas.resumo(listOf(36, 18, 18))
        assertTrue(r, r.contains("44"))
        assertTrue(r, r.contains("72"))
        assertTrue(r, r.contains("28"))
        assertFalse("a fala tem sinal cru", RotuloAcessivel.temSinalCru(r))
    }
}
