package com.gurps.ficha.domain.rules.traits

import com.gurps.ficha.domain.rules.ReacaoRules
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.VantagemSelecionada
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Efeito que muda com a FAIXA DE CUSTO escolhida (Lote OPCAO-1).
 *
 * Vários traços do GURPS não têm nível: têm degraus. Aparência custa 4, 12, 16
 * ou 20 pontos e dá +1, +2, +2, +2 de reação (MB p.21); Hábitos Detestáveis
 * custa −5, −10 ou −15 e dá −1, −2, −3. `porNivel` não serve — os degraus não
 * são múltiplos um do outro.
 *
 * Analogia: `porNivel` é preço por quilo; `porOpcao` é tabela de tamanhos. P, M
 * e G têm cada um o seu número.
 *
 * E há a armadilha que este lote descobriu: **seis ids existem nos DOIS
 * catálogos**. Aparência é vantagem (4 a 20 pts) e desvantagem (−4 a −24) ao
 * mesmo tempo — é uma escala que atravessa o zero. O interpretador procurava
 * sempre em vantagens primeiro, então quem comprasse a versão feia receberia os
 * efeitos da bonita.
 */
class EfeitoPorOpcaoTest {

    @After
    fun limpar() = EfeitoInterpretador.restaurarBuscadorPadrao()

    /** Tabela real da Aparência positiva (MB p.21), em duas parcelas. */
    private val aparenciaVantagem = listOf(
        EfeitoDeclarado(
            tipo = "pericia", alvo = "reacao",
            porOpcao = mapOf("4" to 1, "12" to 2, "16" to 2, "20" to 2),
            condicao = "de quem pode enxergar você"
        ),
        EfeitoDeclarado(
            tipo = "pericia", alvo = "reacao",
            porOpcao = mapOf("12" to 2, "16" to 4, "20" to 6),
            condicao = "adicional, de quem se sente atraído pelo seu sexo"
        )
    )

    private val aparenciaDesvantagem = listOf(
        EfeitoDeclarado(
            tipo = "pericia", alvo = "reacao",
            porOpcao = mapOf("-4" to -1, "-8" to -2, "-16" to -4, "-20" to -5, "-24" to -6),
            condicao = "de quem pode enxergar você"
        )
    )

    // --- a tabela do livro ---

    @Test
    fun `cada degrau de Aparencia devolve o numero do livro`() {
        val base = aparenciaVantagem[0]
        val atracao = aparenciaVantagem[1]
        fun sel(custo: Int) = VantagemSelecionada(
            definicaoId = "aparencia", nome = "Aparência", custoEscolhido = custo
        )
        // Atraente (4 pts): +1 para todo mundo, sem parcela de atração.
        assertEquals(1, base.valorPara(sel(4)))
        assertEquals(0, atracao.valorPara(sel(4)))
        // Elegante (12): +2 para todos, +4 de quem se sente atraído = 2 + 2.
        assertEquals(2, base.valorPara(sel(12)))
        assertEquals(2, atracao.valorPara(sel(12)))
        // Muito Elegante (16): +2 / +6 = 2 + 4.
        assertEquals(4, atracao.valorPara(sel(16)))
        // Lindo (20): +2 / +8 = 2 + 6.
        assertEquals(6, atracao.valorPara(sel(20)))
    }

    @Test
    fun `custo fora da tabela devolve zero, nao um chute`() {
        // Ficha antiga (custoEscolhido = 0) ou opção que o livro não prevê.
        // Preferir não dar o bônus a dar o bônus errado é a regra do
        // interpretador inteiro.
        val efeito = aparenciaVantagem[0]
        assertEquals(0, efeito.valorPara(VantagemSelecionada(definicaoId = "aparencia")))
        assertEquals(
            0,
            efeito.valorPara(VantagemSelecionada(definicaoId = "aparencia", custoEscolhido = 7))
        )
    }

    @Test
    fun `sem porOpcao o comportamento antigo nao muda`() {
        val simples = EfeitoDeclarado(tipo = "pericia", alvo = "Escalada", valor = 2)
        val porNivel = EfeitoDeclarado(
            tipo = "pericia", alvo = "Furtividade", valor = 2, porNivel = true
        )
        val sel = VantagemSelecionada(definicaoId = "x", nome = "X", nivel = 3, custoEscolhido = 99)
        assertEquals(2, simples.valorPara(sel))
        assertEquals(6, porNivel.valorPara(sel))
    }

    // --- a colisão de id entre os dois catálogos ---

    @Test
    fun `Aparencia FEIA recebe a penalidade, nao o bonus da bonita`() {
        // O bug que este lote quase introduziu. Antes da correção, o
        // interpretador procurava sempre em vantagens primeiro: a desvantagem
        // `aparencia` levaria a tabela POSITIVA e o personagem hediondo
        // ganharia bônus de reação.
        EfeitoInterpretador.buscador = { id, ehDesvantagem ->
            when {
                id != "aparencia" -> null
                ehDesvantagem -> aparenciaDesvantagem
                else -> aparenciaVantagem
            }
        }

        val feio = Personagem(
            nome = "Teste",
            desvantagens = listOf(
                DesvantagemSelecionada(
                    definicaoId = "aparencia", nome = "Aparência", custoEscolhido = -16
                )
            )
        )
        val condicionais = ReacaoRules.condicionaisDe(feio)
        assertEquals(1, condicionais.size)
        assertEquals("Hediondo deveria dar -4", -4, condicionais.first().valor)

        val bonito = Personagem(
            nome = "Teste",
            vantagens = listOf(
                VantagemSelecionada(
                    definicaoId = "aparencia", nome = "Aparência", custoEscolhido = 12
                )
            )
        )
        val doBonito = ReacaoRules.condicionaisDe(bonito)
        assertEquals(2, doBonito.size)
        assertEquals(4, doBonito.sumOf { it.valor })
        assertTrue("nenhuma parcela pode ser negativa", doBonito.all { it.valor > 0 })
    }

    @Test
    fun `a Aparencia nao entra no total automatico - depende de enxergar`() {
        // Mesma regra da Voz Melodiosa: o livro diz "modificadores de reação só
        // afetam pessoas capazes de enxergar o personagem". Somar sempre daria
        // bônus contra cegos e contra máquinas.
        EfeitoInterpretador.buscador = { id, _ ->
            if (id == "aparencia") aparenciaVantagem else null
        }
        val p = Personagem(
            nome = "Teste",
            vantagens = listOf(
                VantagemSelecionada(
                    definicaoId = "aparencia", nome = "Aparência", custoEscolhido = 20
                )
            )
        )
        assertEquals(0, ReacaoRules.totalDe(p))
        assertTrue(ReacaoRules.temAlgumModificador(p))
    }

    @Test
    fun `Habitos Detestaveis escala com a faixa de custo`() {
        // MB p.22: -5 pontos para cada -1 de reação.
        val efeito = EfeitoDeclarado(
            tipo = "pericia", alvo = "reacao",
            porOpcao = mapOf("-5" to -1, "-10" to -2, "-15" to -3),
            condicao = "de quem percebe o hábito"
        )
        fun sel(custo: Int) = DesvantagemSelecionada(
            definicaoId = "habitos_detestaveis", nome = "Hábitos Detestáveis",
            custoEscolhido = custo
        )
        assertEquals(-1, efeito.valorPara(sel(-5)))
        assertEquals(-2, efeito.valorPara(sel(-10)))
        assertEquals(-3, efeito.valorPara(sel(-15)))
        // Sinal trocado nao casa com nada -- e por isso o teste do catalogo
        // confere as chaves contra as `options` do traco.
        assertEquals(0, efeito.valorPara(sel(5)))
    }
}
