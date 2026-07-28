package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.BracalRuleBase
import com.gurps.ficha.domain.rules.traits.DxBracalRule
import com.gurps.ficha.model.AtributoBase
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * DX Braçal (MB p.56) — o efeito, e principalmente **o que ela NÃO faz**.
 *
 * O Lote STB-1 corrigiu o custo das duas Braçais, mas o STB-2 implementou o
 * efeito só da ST: quem comprava DX Braçal pagava certo e não recebia nada na
 * tela. Este lote fecha a dívida.
 *
 * A armadilha que o espelho ingênuo cairia: o livro diz que *"as perícias de
 * combate dependem da DX corporal e **não se beneficiam de forma alguma** da
 * destreza braçal"*. Ao contrário da ST Braçal, que aumenta o dano da arma, a
 * DX Braçal **não pode** aumentar NH de ataque. A metade dos testes daqui é
 * sobre isso.
 */
class DxBracalRulesTest {

    private fun dxBracal(niveis: Int, bracos: Int = 2) = VantagemSelecionada(
        definicaoId = DxBracalRule.ID,
        nome = "DX Braçal",
        nivel = niveis,
        metadados = mapOf(BracalRuleBase.CHAVE_BRACOS to bracos.toString())
    )

    private fun personagem(niveis: Int) = Personagem(
        nome = "Teste",
        destreza = 10,
        vantagens = listOf(dxBracal(niveis))
    )

    // --- o que ela FAZ ---

    @Test
    fun `sem DX Bracal o seletor nao aparece`() {
        val p = Personagem(nome = "Teste", destreza = 10)
        assertTrue(!DxBracalRules.temDxBracal(p))
        assertEquals(0, DxBracalRules.bonusDe(p))
    }

    @Test
    fun `os bracos agem com a DX somada, o corpo nao`() {
        val p = personagem(3)
        assertEquals(13, DxBracalRules.dxDosBracos(p))
        assertEquals("a DX do corpo nao pode mudar", 10, p.dx)
    }

    @Test
    fun `duas compras somam`() {
        val p = Personagem(
            nome = "Teste", destreza = 10,
            vantagens = listOf(dxBracal(2, bracos = 1), dxBracal(1, bracos = 1))
        )
        assertEquals(3, DxBracalRules.bonusDe(p))
    }

    @Test
    fun `o rotulo avisa que nao vale para combate`() {
        // E a pegadinha da vantagem: quem le "+3 DX" assume que o ataque melhora.
        val texto = DxBracalRules.rotulo(personagem(3))
        assertTrue(texto, texto.contains("+3"))
        assertTrue(texto, texto.contains("13"))
        assertTrue("o aviso do livro precisa estar no rotulo", texto.contains("combate"))
    }

    // --- o que ela NÃO faz: a metade que importa ---

    @Test
    fun `a DX Bracal NAO entra no NH de pericia de combate`() {
        val comArma = personagem(3).let { p ->
            p.copy(pericias = listOf(
                PericiaSelecionada(
                    definicaoId = "faca", nome = "Faca",
                    atributoBase = AtributoBase.DX, dificuldade = Dificuldade.FACIL,
                    pontosGastos = 1
                )
            ))
        }
        // Faca Facil, 1 ponto = DX. Com DX 10, NH 10 -- e nao 13.
        assertEquals(10, comArma.pericias.first().calcularNivel(comArma))
    }

    @Test
    fun `a DX Bracal NAO entra em pericia nenhuma, nem fora de combate`() {
        // O bonus vale para a ACAO, e quem decide se a acao e de mao e o
        // jogador no seletor -- nao o NH gravado na ficha.
        val p = personagem(4).let {
            it.copy(pericias = listOf(
                PericiaSelecionada(
                    definicaoId = "arrombamento", nome = "Arrombamento",
                    atributoBase = AtributoBase.DX, dificuldade = Dificuldade.MEDIA,
                    pontosGastos = 2
                )
            ))
        }
        // Media, 2 pontos = DX. NH 10.
        assertEquals(10, p.pericias.first().calcularNivel(p))
    }

    @Test
    fun `a DX Bracal NAO mexe na Velocidade Basica`() {
        // MB p.56: "ele nao afeta a Velocidade Basica".
        val semVantagem = Personagem(nome = "Teste", destreza = 10, vitalidade = 10)
        val comVantagem = personagem(4).copy(vitalidade = 10)
        assertEquals(semVantagem.velocidadeBasica, comVantagem.velocidadeBasica, 0.001f)
        assertEquals(semVantagem.deslocamentoBasico, comVantagem.deslocamentoBasico)
    }

    @Test
    fun `ST Bracal e DX Bracal nao se misturam`() {
        val p = Personagem(
            nome = "Teste", forca = 10, destreza = 10,
            vantagens = listOf(
                dxBracal(3),
                VantagemSelecionada(
                    definicaoId = com.gurps.ficha.domain.rules.traits.StBracalRule.ID,
                    nome = "ST Braçal", nivel = 2,
                    metadados = mapOf(BracalRuleBase.CHAVE_BRACOS to "2")
                )
            )
        )
        assertEquals(3, DxBracalRules.bonusDe(p))
        assertEquals(2, StBracalRules.bonusDe(p))
        assertEquals(13, DxBracalRules.dxDosBracos(p))
        assertEquals(12, StBracalRules.stDosBracos(p))
    }
}
