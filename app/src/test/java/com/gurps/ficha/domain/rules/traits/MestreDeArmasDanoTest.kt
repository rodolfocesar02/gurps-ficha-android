package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.model.AtributoBase
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Quando o Mestre de Armas dá bônus de dano — e quando NÃO dá.
 *
 * Escrito em 28/07 para responder a uma dúvida do usuário no aparelho: a nota
 * `+1/dado Mestre de Armas` não aparecia no card de Dano, com a vantagem na
 * ficha. A suspeita era de defeito na nota; o dano na tela dizia outra coisa
 * (Faca `1d-3` com ST 10, exatamente o valor SEM bônus).
 *
 * São **dois portões**, e o segundo é o que costuma pegar:
 *
 *  1. **Cobertura** — Mestre de Armas de 20 pts é "uma única perícia", e o
 *     jogador precisa dizer qual. Sem isso não cobre nada.
 *  2. **Nível da perícia** — MB p.99: o bônus de dano é +1/dado com NH DX+1 e
 *     +2/dado com NH DX+2 ou mais. Com NH igual à DX, **não há bônus**.
 *
 * Ou seja: a ficha de teste (DX 10, Faca NH 10) não deveria mesmo mostrar nota
 * nenhuma. A nota está certa; a ficha é que ainda não alcançou o bônus.
 */
class MestreDeArmasDanoTest {

    private val regra = MestreDeArmasRule()

    private fun faca(pontos: Int) = PericiaSelecionada(
        definicaoId = "faca",
        nome = "Faca",
        atributoBase = AtributoBase.DX,
        dificuldade = Dificuldade.FACIL,
        pontosGastos = pontos
    )

    private fun personagem(pontosNaFaca: Int) = Personagem(
        nome = "Teste",
        destreza = 10,
        pericias = listOf(faca(pontosNaFaca)),
        vantagens = listOf(
            VantagemSelecionada(
                definicaoId = "mestre_de_armas",
                nome = "Mestre de Armas",
                custoEscolhido = 20,
                metadados = mapOf("classId" to "single", "pericias_cobertas" to "Faca")
            )
        )
    )

    private fun bonus(p: Personagem): Int = TraitRuleRegistry.getDamageBonusPerDie(
        personagem = p, periciaId = "faca", weaponName = "Faca", armaGrupo = "faca"
    )

    // --- o portão que pegou a ficha de teste ---

    @Test
    fun `NH igual a DX NAO da bonus de dano`() {
        // Faca e Facil: 1 ponto = DX. NH 10 com DX 10 -> nada.
        val p = personagem(pontosNaFaca = 1)
        assertEquals(10, p.pericias.first().calcularNivel(p))
        assertEquals("MB p.99: o bonus so comeca em DX+1", 0, bonus(p))
    }

    @Test
    fun `NH em DX mais 1 da mais 1 por dado`() {
        val p = personagem(pontosNaFaca = 2)
        assertEquals(11, p.pericias.first().calcularNivel(p))
        assertEquals(1, bonus(p))
    }

    @Test
    fun `NH em DX mais 2 da mais 2 por dado`() {
        val p = personagem(pontosNaFaca = 4)
        assertEquals(12, p.pericias.first().calcularNivel(p))
        assertEquals(2, bonus(p))
    }

    // --- o outro portão ---

    @Test
    fun `Mestre de Armas de UMA pericia sem dizer QUAL nao cobre nada`() {
        val semEscolha = personagem(pontosNaFaca = 4).let { p ->
            p.copy(vantagens = listOf(
                VantagemSelecionada(
                    definicaoId = "mestre_de_armas",
                    nome = "Mestre de Armas",
                    custoEscolhido = 20,
                    metadados = mapOf("classId" to "single", "pericias_cobertas" to "")
                )
            ))
        }
        assertEquals(0, bonus(semEscolha))
    }

    @Test
    fun `Mestre de Armas de TODAS as pericias cobre sem precisar escolher`() {
        val todas = personagem(pontosNaFaca = 4).let { p ->
            p.copy(vantagens = listOf(
                VantagemSelecionada(
                    definicaoId = "mestre_de_armas",
                    nome = "Mestre de Armas",
                    custoEscolhido = 45,
                    metadados = mapOf("classId" to "todas", "pericias_cobertas" to "")
                )
            ))
        }
        assertEquals(2, bonus(todas))
    }

    // --- e a nota, que era a duvida original ---

    @Test
    fun `a nota so existe quando o bonus existe`() {
        // Sem bonus, `getDamageBonusOrigens` devolve lista vazia e o componente
        // nao renderiza nada -- que e exatamente o que apareceu no aparelho.
        val semBonus = personagem(pontosNaFaca = 1)
        assertEquals(
            emptyList<TraitRuleRegistry.OrigemDeBonus>(),
            TraitRuleRegistry.getDamageBonusOrigens(semBonus, "faca", "Faca", "faca")
        )

        val comBonus = personagem(pontosNaFaca = 4)
        val origens = TraitRuleRegistry.getDamageBonusOrigens(comBonus, "faca", "Faca", "faca")
        assertEquals(1, origens.size)
        assertEquals("Mestre de Armas", origens.first().nomeDoTraco)
        assertEquals(2, origens.first().valor)
    }
}
