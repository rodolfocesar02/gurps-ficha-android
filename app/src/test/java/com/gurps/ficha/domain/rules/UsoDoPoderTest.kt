package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.poderes.RegrasDePoder
import com.gurps.ficha.domain.rules.poderes.UsoDoPoder
import com.gurps.ficha.domain.rules.poderes.UsoDoPoder.Incapacitacao.Atributo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Usar um poder** — GURPS Poderes, p.156 e p.158. Lote POD-11.
 *
 * ## 🔴 Este lote começou com o plano errado
 *
 * O plano mandava fazer um "botão de ativar o poder" que rolasse HT ou Vontade
 * conforme a fonte. Relendo a p.156 inteira, aquela tabela **não é ativação**:
 * é o teste para o poder não ficar **incapacitado** depois de uma falha crítica
 * em esforço adicional ou proeza.
 *
 * ⚠️ **Não existe "atributo de ativação do poder".** O que se rola depende da
 * habilidade (a vantagem), não da fonte. Estes testes existem, em boa parte,
 * para que essa confusão não volte.
 */
class UsoDoPoderTest {

    // ── O Talento (p.158) ──────────────────────────────────────────────

    @Test
    fun `o Talento soma nos testes para USAR a habilidade`() {
        // "testes para ativar, atacar, controlar ou defender com essas habilidades"
        listOf(
            UsoDoPoder.TipoDeTeste.ATIVAR,
            UsoDoPoder.TipoDeTeste.ATACAR,
            UsoDoPoder.TipoDeTeste.CONTROLAR,
            UsoDoPoder.TipoDeTeste.DEFENDER
        ).forEach {
            assertEquals("'${it.rotulo}' deveria receber o Talento", 3, UsoDoPoder.bonusDoTalento(3, it))
        }
    }

    @Test
    fun `o Talento NAO soma no dano, na reacao, na limitacao nem no alvo`() {
        // 🔴 A lista de exclusões é o que impede o bônus de vazar para onde ele
        // mais desequilibraria: o dano e o teste de resistência da vítima.
        listOf(
            UsoDoPoder.TipoDeTeste.DANO,
            UsoDoPoder.TipoDeTeste.REACAO,
            UsoDoPoder.TipoDeTeste.EXIGIDO_POR_LIMITACAO,
            UsoDoPoder.TipoDeTeste.FEITO_PELO_ALVO
        ).forEach {
            assertEquals("'${it.rotulo}' nao pode receber o Talento", 0, UsoDoPoder.bonusDoTalento(5, it))
        }
    }

    @Test
    fun `nivel zero ou negativo nao vira bonus`() {
        assertEquals(0, UsoDoPoder.bonusDoTalento(0, UsoDoPoder.TipoDeTeste.ATIVAR))
        assertEquals(0, UsoDoPoder.bonusDoTalento(-2, UsoDoPoder.TipoDeTeste.ATACAR))
    }

    @Test
    fun `as duas excecoes de reacao que o livro nomeia`() {
        // "exceto para Aliados com Invocável, e Patrono com Altamente Acessível"
        assertTrue(UsoDoPoder.reacaoRecebeTalento("Aliados, com Invocável"))
        assertTrue(UsoDoPoder.reacaoRecebeTalento("Patrono, com Altamente Acessível"))
        assertFalse(UsoDoPoder.reacaoRecebeTalento("Aliados"))
        assertFalse(UsoDoPoder.reacaoRecebeTalento("Carisma"))
    }

    // ── A incapacitação (p.156) ────────────────────────────────────────

    @Test
    fun `HT para as fontes fisicas, Vontade para as demais`() {
        listOf("Biológico", "Elemental", "Natureza", "Super").forEach {
            assertEquals("$it deveria rolar HT", Atributo.HT, UsoDoPoder.Incapacitacao.atributo(it))
        }
        listOf("Chi", "Divino", "Espiritual", "Mágico", "Moral", "Psíquico").forEach {
            assertEquals("$it deveria rolar Vontade", Atributo.VONTADE,
                UsoDoPoder.Incapacitacao.atributo(it))
        }
    }

    @Test
    fun `o Cosmico e IMUNE, e por isso nao estava na lista`() {
        // 🔴 Eu tinha anotado no plano "o Cósmico não aparece — tratar como caso
        // à parte, não chutar". O motivo estava na frase seguinte do livro:
        // "Os poderes cósmicos são imunes a incapacitações."
        assertEquals(Atributo.IMUNE, UsoDoPoder.Incapacitacao.atributo("Cósmico"))
        val e = UsoDoPoder.Incapacitacao.explicar("Cósmico")
        assertTrue(e, e.contains("imunes"))
    }

    @Test
    fun `fonte que o livro nao lista fica com o Mestre`() {
        // "O Mestre decide o teste para outros poderes."
        assertEquals(Atributo.A_CRITERIO_DO_MESTRE, UsoDoPoder.Incapacitacao.atributo("Antimagia"))
        assertEquals(Atributo.A_CRITERIO_DO_MESTRE, UsoDoPoder.Incapacitacao.atributo(null))
        assertEquals(Atributo.A_CRITERIO_DO_MESTRE, UsoDoPoder.Incapacitacao.atributo("  "))
    }

    @Test
    fun `as onze fontes do catalogo estao todas classificadas`() {
        // Varredura: nenhuma das fontes que o app oferece pode cair no
        // "o Mestre decide" por esquecimento nosso.
        RegrasDePoder.VALOR_DA_FONTE.keys.forEach { fonte ->
            assertFalse(
                "a fonte '$fonte' do catalogo ficou sem classificacao de incapacitacao",
                UsoDoPoder.Incapacitacao.atributo(fonte) == Atributo.A_CRITERIO_DO_MESTRE
            )
        }
    }

    @Test
    fun `o feminino da linha Fontes tambem e reconhecido`() {
        assertEquals(Atributo.VONTADE, UsoDoPoder.Incapacitacao.atributo("Divina"))
        assertEquals(Atributo.HT, UsoDoPoder.Incapacitacao.atributo("Biológica"))
    }

    @Test
    fun `a explicacao nao tem sinal cru`() {
        listOf("Super", "Psíquico", "Cósmico", null).forEach {
            val e = UsoDoPoder.Incapacitacao.explicar(it)
            assertFalse(e, RotuloAcessivel.temSinalCru(e))
            assertTrue(e, e.contains("156"))
        }
    }

    @Test
    fun `a incapacitacao atinge o poder inteiro`() {
        // "Uma falha ou uma falha crítica prejudica TODAS as habilidades do poder."
        assertTrue(UsoDoPoder.Incapacitacao.ATINGE_O_PODER_INTEIRO)
    }
}
