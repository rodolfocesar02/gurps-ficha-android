package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.BracalRuleBase
import com.gurps.ficha.domain.rules.traits.DxBracalRule
import com.gurps.ficha.domain.rules.traits.StBracalRule
import com.gurps.ficha.model.ModificadorSelecao
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.TipoCusto
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ST Braçal e DX Braçal (MB p.89 e p.56) — custo e efeito.
 *
 * O defeito encontrado no aparelho em 28/07: o app cobrava 3, 5 ou 8 pontos
 * **uma vez**, quando no livro esses números são o preço de **cada +1**.
 * ST Braçal +4 nos dois braços custava 5 pontos em vez de 20 — a ficha entregava
 * quatro níveis pelo preço de um.
 */
class StBracalRulesTest {

    private fun stBracal(niveis: Int, bracos: Int) = VantagemSelecionada(
        definicaoId = StBracalRule.ID,
        nome = "ST Braçal",
        nivel = niveis,
        tipoCusto = TipoCusto.ESCOLHA,
        metadados = mapOf(BracalRuleBase.CHAVE_BRACOS to bracos.toString())
    )

    // --- custo: a correção ---

    @Test
    fun `o custo do livro e POR NIVEL, nao um valor fechado`() {
        val regra = StBracalRule()
        // "3 pontos para cada +1 de ST para um braço, 5 para dois, 8 para três"
        assertEquals(3, regra.calculateCost(stBracal(niveis = 1, bracos = 1), emptyList()))
        assertEquals(12, regra.calculateCost(stBracal(niveis = 4, bracos = 1), emptyList()))
        assertEquals(20, regra.calculateCost(stBracal(niveis = 4, bracos = 2), emptyList()))
        assertEquals(32, regra.calculateCost(stBracal(niveis = 4, bracos = 3), emptyList()))
    }

    @Test
    fun `o exemplo do livro fecha - ST Bracal mais 4 em ambos os bracos`() {
        // MB p.89: "ST 10 e ST Braçal +4 em ambos os braços". 5 x 4 = 20 pts.
        assertEquals(20, StBracalRule().calculateCost(stBracal(4, 2), emptyList()))
    }

    @Test
    fun `DX Bracal segue a mesma regra com a tabela dela`() {
        val regra = DxBracalRule()
        val dx = { niveis: Int, bracos: Int ->
            VantagemSelecionada(
                definicaoId = DxBracalRule.ID, nome = "DX Braçal", nivel = niveis,
                metadados = mapOf(BracalRuleBase.CHAVE_BRACOS to bracos.toString())
            )
        }
        assertEquals(12, regra.calculateCost(dx(1, 1), emptyList()))
        assertEquals(36, regra.calculateCost(dx(3, 1), emptyList()))
        assertEquals(48, regra.calculateCost(dx(3, 2), emptyList()))
        // O livro para em dois braços: pedir três cai no padrão, nunca inventa preço.
        assertEquals(listOf(1, 2), regra.opcoesDeBracos())
    }

    @Test
    fun `ficha antiga sem o dado de bracos nao quebra`() {
        // Quem salvou antes desta correção não tem `metadados["bracos"]`.
        val antiga = VantagemSelecionada(
            definicaoId = StBracalRule.ID, nome = "ST Braçal", nivel = 2, metadados = null
        )
        assertEquals(6, StBracalRule().calculateCost(antiga, emptyList()))
    }

    @Test
    fun `numero de bracos fora da tabela cai no padrao`() {
        // O livro avisa que 4+ braços não compensa e não dá preço para isso.
        val quatro = VantagemSelecionada(
            definicaoId = StBracalRule.ID, nome = "ST Braçal", nivel = 1,
            metadados = mapOf(BracalRuleBase.CHAVE_BRACOS to "4")
        )
        assertEquals(3, StBracalRule().calculateCost(quatro, emptyList()))
    }

    @Test
    fun `nivel zero ou negativo conta como um`() {
        val zero = VantagemSelecionada(
            definicaoId = StBracalRule.ID, nome = "ST Braçal", nivel = 0,
            metadados = mapOf(BracalRuleBase.CHAVE_BRACOS to "2")
        )
        assertEquals(5, StBracalRule().calculateCost(zero, emptyList()))
    }

    @Test
    fun `limitacao da ST normal vale para a ST Bracal`() {
        // MB p.89: "aplique as mesmas limitações à ST Braçal".
        val mods = listOf(ModificadorSelecao("mod_x", "Manuseadores Precários", -40))
        // 5 x 4 = 20, com -40% -> 12.
        assertEquals(12, StBracalRule().calculateCost(stBracal(4, 2), mods))
    }

    // --- efeito: o que aparece na tela ---

    private fun personagemCom(niveis: Int) = Personagem(
        nome = "Teste",
        forca = 10,
        vantagens = listOf(stBracal(niveis, bracos = 2))
    )

    @Test
    fun `sem ST Bracal o painel nao aparece`() {
        val p = Personagem(nome = "Teste", forca = 10)
        assertTrue(!StBracalRules.temStBracal(p))
        assertEquals(0, StBracalRules.bonusDe(p))
    }

    @Test
    fun `os bracos agem com a ST somada, o corpo nao`() {
        val p = personagemCom(4)
        assertEquals(14, StBracalRules.stDosBracos(p))
        // O corpo continua ST 10 -- e o que o livro exige para PV e Carga.
        assertEquals(10, p.st)
        assertEquals(10, p.pontosVida)
    }

    @Test
    fun `o dano usa a ST dos bracos, nao a do corpo`() {
        val p = personagemCom(4)
        // ST 10 -> GdP 1d-2 / GeB 1d ; ST 14 -> GdP 1d / GeB 2d
        assertEquals(CharacterRules.calcularDanoGdP(14), StBracalRules.danoGdPDosBracos(p))
        assertEquals(CharacterRules.calcularDanoGeB(14), StBracalRules.danoGeBDosBracos(p))
        // E precisa ser DIFERENTE do dano do corpo, senão o seletor não serve.
        assertTrue(StBracalRules.danoGeBDosBracos(p) != p.danoGeB)
    }

    @Test
    fun `duas compras de ST Bracal somam`() {
        val p = Personagem(
            nome = "Teste", forca = 10,
            vantagens = listOf(stBracal(2, 1), stBracal(1, 1))
        )
        assertEquals(3, StBracalRules.bonusDe(p))
    }

    @Test
    fun `a ARMA empunhada bate com a ST dos bracos`() {
        // O pedido do usuario: "o ST bracal vai gerar bonus de dano quando
        // empunhando armas". Empunhar e acao de braco.
        val p = personagemCom(4)
        val faca = com.gurps.ficha.model.Equipamento(
            nome = "Faca",
            tipo = com.gurps.ficha.model.TipoEquipamento.ARMA,
            armaDanoRaw = "GeB-1 corte"
        )
        // ST 10 -> GeB 1d ; ST 14 -> GeB 2d. A faca tira 1: 1d-1 vira 2d-1.
        assertEquals("1d-1 corte", faca.danoCalculadoComSt(p, null, stExtra = 0))
        assertEquals(
            "2d-1 corte",
            faca.danoCalculadoComSt(p, null, stExtra = StBracalRules.bonusDe(p))
        )
    }

    @Test
    fun `o rotulo diz o numero final, nao so o bonus`() {
        // "+4" sozinho nao diz contra o que -- quem le no meio da mesa nao faz a conta.
        val texto = StBracalRules.rotulo(personagemCom(4))
        assertTrue(texto, texto.contains("+4"))
        assertTrue(texto, texto.contains("14"))
    }
}
