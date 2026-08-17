package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.TipoCusto
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **A RD do proprio corpo** — MB p.83. Lote POD-30.
 *
 * 🔴 A vantagem estava no catalogo desde sempre (5 pontos por nivel) e NAO
 * DESCONTAVA NADA. Quem comprava RD 5 pagava 25 pontos e continuava levando o
 * dano inteiro: o dialogo de ferimento so somava a armadura vestida.
 *
 * O livro diz onde ela entra: *"Subtraia esse valor do dano ... depois de
 * aplicar a RD de armaduras artificiais"* -- ela SOMA com a armadura.
 */
class ResistenciaDanoNaturalTest {

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    private fun rd(nivel: Int, nome: String = "Resistência a Dano", mods: List<String> = emptyList()) =
        VantagemSelecionada(
            definicaoId = ResistenciaDanoNatural.ID,
            nome = nome,
            nivel = nivel,
            tipoCusto = TipoCusto.POR_NIVEL,
            custoBase = 5,
            modificadores = mods.map {
                ModificadorSelecao(id = it, nome = it, valor = -20)
            }
        )

    // == O numero =======================================================

    @Test
    fun `cada nivel da 1 de RD`() {
        assertEquals(5, ResistenciaDanoNatural.total(Personagem(vantagens = listOf(rd(5)))))
    }

    @Test
    fun `sem a vantagem a RD natural e zero, e nao ha linha na tela`() {
        val p = Personagem()
        assertEquals(0, ResistenciaDanoNatural.total(p))
        assertNull("apareceu linha dizendo RD natural 0", ResistenciaDanoNatural.explicar(p))
    }

    @Test
    fun `duas fontes somam, e as duas aparecem`() {
        // ⚠️ Uma lista e nao um numero so: quem le a ficha precisa saber qual e
        // qual, ainda mais quando uma tem ressalva e a outra nao.
        val p = Personagem(vantagens = listOf(rd(2, "Couro Grosso"), rd(3, "Escamas")))
        assertEquals(5, ResistenciaDanoNatural.total(p))
        val texto = ResistenciaDanoNatural.explicar(p)!!
        assertTrue(texto, texto.contains("Couro Grosso 2"))
        assertTrue(texto, texto.contains("Escamas 3"))
    }

    @Test
    fun `nivel zero ou negativo nao entra`() {
        // Ficha com lixo nao pode virar RD de graca nem RD negativa.
        assertEquals(0, ResistenciaDanoNatural.total(Personagem(vantagens = listOf(rd(0)))))
        assertEquals(0, ResistenciaDanoNatural.total(Personagem(vantagens = listOf(rd(-3)))))
        // ⚠️ E nao basta o TOTAL dar zero: a fonte tem de sair da lista, senao
        // a tela ganha uma linha dizendo "Resistencia a Dano 0". Foi a sonda
        // que mostrou -- trocar o descarte por `rd < 0` NAO reprovava aqui,
        // porque a soma continuava dando zero do mesmo jeito.
        assertTrue(ResistenciaDanoNatural.fontes(Personagem(vantagens = listOf(rd(0)))).isEmpty())
        assertNull(ResistenciaDanoNatural.explicar(Personagem(vantagens = listOf(rd(0)))))
    }

    @Test
    fun `outra vantagem qualquer nao vira RD`() {
        val outra = VantagemSelecionada(
            definicaoId = "reputacao", nome = "Reputação", nivel = 4, tipoCusto = TipoCusto.POR_NIVEL
        )
        assertEquals(0, ResistenciaDanoNatural.total(Personagem(vantagens = listOf(outra))))
    }

    // == A ressalva =====================================================

    @Test
    fun `RD com modificador avisa, mas nao trava`() {
        // ⚠️ Uma RD "Limitada (so contra fogo)" nao protege de tudo, e o app nao
        // tem como saber o que esta chegando. O numero aparece somado e a
        // ressalva ao lado -- quem decide e o jogador. Travar repetiria o erro
        // do `conhecimento_oculto`; somar em silencio seria pior ainda.
        val p = Personagem(vantagens = listOf(rd(4, mods = listOf("Limitada, Fogo"))))
        assertEquals("o numero foi travado por causa do modificador", 4,
            ResistenciaDanoNatural.total(p))
        val texto = ResistenciaDanoNatural.explicar(p)!!
        assertTrue(texto, texto.contains("Limitada, Fogo"))
        assertTrue("nao avisa para conferir", texto.contains("Confira"))
        assertFalse("a fala tem sinal cru", RotuloAcessivel.temSinalCru(texto))
    }

    @Test
    fun `RD sem modificador nao ganha aviso de conferir`() {
        val p = Personagem(vantagens = listOf(rd(3)))
        assertFalse(ResistenciaDanoNatural.explicar(p)!!.contains("Confira"))
    }

    // == A fiacao =======================================================

    @Test
    fun `a RD natural SOMA com a da armadura, e nao substitui`() {
        // "depois de aplicar a RD de armaduras artificiais" (p.83).
        val d = fonte("com/gurps/ficha/ui/features/rolagem/DialogoFerimento.kt")
        assertTrue(
            "a RD natural deixou de somar com a da armadura",
            d.contains("CoberturaDaArmadura.rdTotal(pecasVestidas) + rdNatural")
        )
        val t = fonte("com/gurps/ficha/ui/TabRolagem.kt")
        assertTrue("o dialogo nao recebe a RD natural",
            t.contains("ResistenciaDanoNatural.total(p)"))
        assertTrue("o dialogo nao recebe a explicacao",
            t.contains("ResistenciaDanoNatural.explicar(p)"))
    }

    @Test
    fun `uma caixa de marcar so, e o rotulo deixou de dizer so armadura`() {
        // ⚠️ Duas caixas seriam duas rotas para a mesma decisao. Quem desmarca
        // quer o ataque sem RD NENHUMA.
        val d = fonte("com/gurps/ficha/ui/features/rolagem/DialogoFerimento.kt")
        assertFalse(
            "o rotulo voltou a prometer so a RD da armadura",
            d.contains("\"Descontar RD da armadura\"")
        )
        assertTrue("a conta some da tela", d.contains("explicacaoRdNatural"))
    }

    @Test
    fun `a RD da raca e a de um poder contam igual`() {
        // `vantagensTotais`: mesmo criterio do agregador de ataques. Uma raca
        // com couraca vale igual a quem comprou com pontos.
        val src = fonte("com/gurps/ficha/domain/rules/ResistenciaDanoNatural.kt")
        assertTrue("parou de contar a RD racial", src.contains("personagem.vantagensTotais"))
        // E a habilidade de poder tambem: ela e uma vantagem comum, com poderId.
        val doPoder = rd(3).apply { poderId = "p1" }
        assertEquals(3, ResistenciaDanoNatural.total(Personagem(vantagens = listOf(doPoder))))
    }
}
