package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Qual rolagem responde ao pedido da Mesa** — lote CC-6.
 *
 * 🔴 O que este arquivo protege é uma coisa só: **nunca escolher por acaso**. Um
 * ataque saído da perícia errada dá um número plausível, vai para o chat, e
 * ninguém repara.
 */
class EscolhaDoPedidoTest {

    private fun pedido(pericia: String?, oQue: String = "ataque") =
        PedidoDaMesa.ler(
            "gurpsapp://rolar?o=$oQue&mod=-7&onde=no%20cr%C3%A2nio&alvo=Orc" +
                (pericia?.let { "&pericia=$it" } ?: "")
        )!!

    private val AS_DO_CESAR = listOf(
        EscolhaDoPedido.Opcao("pericia_ataque_inato_Feixe", "Ataque Inato (Feixe)", 14),
        EscolhaDoPedido.Opcao("pericia_bastao_", "Bastão", 13),
        EscolhaDoPedido.Opcao("pericia_espada_larga_", "Espada Larga", 12)
    )

    /**
     * 🔴 O id que a Mesa manda é o `definicaoId` (`espada_larga`); o id da tela
     * tem prefixo e a especialização colada atrás.
     */
    @Test
    fun `casa a pericia que a Mesa pediu`() {
        val r = EscolhaDoPedido.escolher(pedido("espada_larga"), AS_DO_CESAR)
        assertEquals("Espada Larga", r.escolhida?.rotulo)
        assertEquals(12, r.escolhida?.nh)
        assertNull(r.porqueNaoCasou)

        // Com especialização atrás, também.
        val inato = EscolhaDoPedido.escolher(pedido("ataque_inato"), AS_DO_CESAR)
        assertEquals("Ataque Inato (Feixe)", inato.escolhida?.rotulo)

        // E de uma palavra só.
        assertEquals("Bastão",
            EscolhaDoPedido.escolher(pedido("bastao"), AS_DO_CESAR).escolhida?.rotulo)
    }

    /**
     * 🟥 **Não casou? Não se escolhe nenhuma.**
     *
     * ⚠️ A tela recebe a lista inteira e a pessoa decide. Cair na primeira faria
     * o César atacar de Bastão quando a Mesa pediu uma Faca — e o número sairia
     * bonito.
     */
    @Test
    fun `sem casar, devolve a lista e diz porque`() {
        val r = EscolhaDoPedido.escolher(pedido("faca"), AS_DO_CESAR)
        assertNull("escolheu uma que nao foi pedida", r.escolhida)
        assertEquals(3, r.candidatas.size)
        assertTrue(r.porqueNaoCasou!!, r.porqueNaoCasou!!.contains("faca"))

        // Sem perícia nenhuma no pedido: a lista, com o porquê.
        val semNada = EscolhaDoPedido.escolher(pedido(null), AS_DO_CESAR)
        assertNull(semNada.escolhida)
        assertEquals(3, semNada.candidatas.size)
        assertNotNull(semNada.porqueNaoCasou)
    }

    /**
     * ⚠️ Duas com o mesmo id e especializações diferentes: a Mesa não disse
     * qual, e adivinhar erraria metade das vezes.
     */
    @Test
    fun `duas iguais vao as duas para a tela`() {
        asDuasIguais().let { duas ->
            val r = EscolhaDoPedido.escolher(pedido("espada_larga"), duas)
            assertNull(r.escolhida)
            assertEquals(2, r.candidatas.size)
            assertTrue(r.porqueNaoCasou!!.contains("mais de uma"))
        }
    }

    private fun asDuasIguais() = listOf(
        EscolhaDoPedido.Opcao("pericia_espada_larga_Direita", "Espada Larga (Direita)", 12),
        EscolhaDoPedido.Opcao("pericia_espada_larga_Esquerda", "Espada Larga (Esquerda)", 8)
    )

    /**
     * 🟥 **A palavra muda com o que a Mesa pediu.**
     *
     * ⚠️ Num pedido de DANO a lista são armas, e não perícias. A frase *"a Mesa
     * não disse com qual perícia"* por cima de "Dano ST / Cajado Encantado"
     * manda a pessoa procurar uma coisa que não está ali. Apanhado no emulador.
     */
    @Test
    fun `a frase fala de arma quando o pedido e de dano`() {
        val asArmas = listOf(
            EscolhaDoPedido.Opcao("st_base", "Dano ST", null),
            EscolhaDoPedido.Opcao("arma_cajado", "Cajado Encantado", null)
        )
        val doDano = EscolhaDoPedido.escolher(pedido(null, "dano"), asArmas)
        assertTrue(doDano.porqueNaoCasou, doDano.porqueNaoCasou!!.contains("arma"))
        assertTrue("falou de pericia num pedido de dano",
            !doDano.porqueNaoCasou!!.contains("perícia"))

        val doAtaque = EscolhaDoPedido.escolher(pedido(null), AS_DO_CESAR)
        assertTrue(doAtaque.porqueNaoCasou!!.contains("perícia"))
    }

    /** ⚠️ Uma ficha sem nenhuma rolagem de combate não pode ficar muda. */
    @Test
    fun `sem opcao nenhuma, diz isso`() {
        val r = EscolhaDoPedido.escolher(pedido("espada_larga"), emptyList())
        assertNull(r.escolhida)
        assertTrue(r.candidatas.isEmpty())
        assertTrue(r.porqueNaoCasou!!.contains("nenhuma rolagem"))
    }

    /**
     * 🔴 A linha do chat leva a **parte do corpo e o alvo**.
     *
     * ⚠️ *"Espada Larga"* e *"Espada Larga em Orc no crânio"* são coisas
     * diferentes para quem lê o chat depois — e a mesa decide em cima da linha
     * que lê.
     */
    @Test
    fun `o rotulo da jogada diz em quem e onde`() {
        val p = pedido("espada_larga")
        val escolhida = EscolhaDoPedido.escolher(p, AS_DO_CESAR).escolhida
        assertEquals("Espada Larga em Orc no crânio",
            EscolhaDoPedido.rotuloDaJogada(p, escolhida))

        // Sem escolhida, usa o que a Mesa pediu — e não fica vazio.
        assertEquals("Atacar em Orc no crânio",
            EscolhaDoPedido.rotuloDaJogada(p, null))

        // Sem alvo nem parte, só o nome.
        val so = PedidoDaMesa.ler("gurpsapp://rolar?o=ataque&pericia=bastao")!!
        assertEquals("Bastão", EscolhaDoPedido.rotuloDaJogada(so,
            EscolhaDoPedido.escolher(so, AS_DO_CESAR).escolhida))
    }
}
