package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.poderes.AmpliacoesTemporarias
import com.gurps.ficha.domain.rules.poderes.CustoEmPfDoUso
import com.gurps.ficha.domain.rules.poderes.EsforcoAdicional
import com.gurps.ficha.domain.rules.poderes.ModelosDeModificador
import com.gurps.ficha.domain.rules.poderes.MontadorDeModificador
import com.gurps.ficha.domain.rules.poderes.ReservaDeEnergia
import com.gurps.ficha.domain.rules.poderes.ReservaDeEnergia.Limitacao
import com.gurps.ficha.domain.rules.poderes.UsoPredefinido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lotes POD-9, POD-7, POD-12 e POD-13** — GURPS Poderes.
 *
 * Todos os quatro foram escritos **depois** da leitura completa das seções, e
 * dois deles existem na forma corrigida justamente por causa dela: o POD-12
 * trocava PF por efeito no plano (era penalidade de Vontade), e o POD-9 tinha
 * uma regra de recarga só (são seis, contando as limitações).
 */
class PoderesAvancadosTest {

    // ══ POD-9 — Reserva de Energia (p.119) ═════════════════════════════

    @Test
    fun `a RE custa tres pontos por PF`() {
        assertEquals(30, ReservaDeEnergia.custo(10))
        assertEquals("RE 10 (Psíquico) [30]", ReservaDeEnergia.rotulo(10, "Psíquico"))
        assertEquals(0, ReservaDeEnergia.custo(0))
    }

    @Test
    fun `o padrao e um ponto a cada dez minutos`() {
        assertEquals(10, ReservaDeEnergia.minutosPorPonto())
        assertEquals(3, ReservaDeEnergia.recuperadoEm(30))
        assertEquals(0, ReservaDeEnergia.recuperadoEm(9))
    }

    @Test
    fun `Carga Lenta troca os dez minutos por hora ou dia`() {
        // 🔴 O plano registrou "1 ponto a cada 10 minutos" como A regra. É o
        // padrão: estas duas limitações mudam exatamente isso.
        assertEquals(60, ReservaDeEnergia.minutosPorPonto(setOf(Limitacao.CARGA_LENTA_HORA)))
        assertEquals(1440, ReservaDeEnergia.minutosPorPonto(setOf(Limitacao.CARGA_LENTA_DIA)))
        assertEquals(2, ReservaDeEnergia.recuperadoEm(120, setOf(Limitacao.CARGA_LENTA_HORA)))
        assertEquals(0, ReservaDeEnergia.recuperadoEm(120, setOf(Limitacao.CARGA_LENTA_DIA)))
    }

    @Test
    fun `Carga Especial nao recarrega com o tempo, nunca`() {
        assertNull(ReservaDeEnergia.minutosPorPonto(setOf(Limitacao.CARGA_ESPECIAL)))
        assertEquals(0, ReservaDeEnergia.recuperadoEm(10_000, setOf(Limitacao.CARGA_ESPECIAL)))
        assertEquals(-70, Limitacao.CARGA_ESPECIAL.valor)
        assertEquals(-80, Limitacao.CARGA_ESPECIAL_PERDENDO.valor)
    }

    @Test
    fun `as limitacoes incompativeis do livro sao barradas`() {
        // ⚠️ Sem esta trava daria para somar −70% e −60% e comprar uma RE quase
        // de graça que o livro não permite existir.
        assertFalse(ReservaDeEnergia.ehCombinacaoValida(
            setOf(Limitacao.CARGA_ESPECIAL, Limitacao.CARGA_LENTA_HORA)))
        assertFalse(ReservaDeEnergia.ehCombinacaoValida(
            setOf(Limitacao.SOMENTE_HABILIDADES, Limitacao.SOMENTE_PROEZAS)))
        assertTrue(ReservaDeEnergia.ehCombinacaoValida(
            setOf(Limitacao.CARGA_LENTA_HORA, Limitacao.PODER_UNICO)))
    }

    @Test
    fun `a limitacao barateia a RE, com o teto de menos oitenta`() {
        // 30 pontos com Carga Especial (−70%) → 9.
        assertEquals(9, ReservaDeEnergia.custo(10, setOf(Limitacao.CARGA_ESPECIAL)))
        // −70% e −50% dariam −120%; o teto corta em −80%.
        assertEquals(6, ReservaDeEnergia.custo(10, setOf(Limitacao.CARGA_ESPECIAL, Limitacao.PODER_UNICO)))
        assertTrue("a RE nunca fica de graca", ReservaDeEnergia.custo(1, setOf(Limitacao.CARGA_ESPECIAL)) >= 1)
    }

    @Test
    fun `a RE so abastece a propria fonte`() {
        assertTrue(ReservaDeEnergia.podeAbastecer("Psíquico", "Psíquico"))
        assertTrue("o feminino e a mesma fonte", ReservaDeEnergia.podeAbastecer("Psíquica", "Psíquico"))
        assertFalse(ReservaDeEnergia.podeAbastecer("Psíquico", "Divino"))
        assertFalse(ReservaDeEnergia.podeAbastecer(null, "Divino"))
    }

    @Test
    fun `esgotar a RE nao e ficar com PF baixo`() {
        // 🔴 O erro mais fácil aqui: tratar a RE como se fosse PF. São dois
        // medidores separados, e o livro diz isso nas duas direções.
        assertFalse(ReservaDeEnergia.ESGOTAR_CAUSA_EFEITOS_DE_PF_BAIXO)
    }

    // ══ POD-7 — Montador de modificador (p.20-26) ══════════════════════

    // 🔴 Dois testes deste bloco foram REMOVIDOS no POD-16, e o motivo importa:
    // eles guardavam a minha leitura errada, não o livro.
    //
    // · `Antipoderes tem DOIS niveis` — o grupo ANTIPODERES não existe na
    //   Referência Rápida (p.25). As duas linhas de −5% ficam em Contramedidas,
    //   que **somam**; o "−10% das duas situações" era invenção minha para tapar
    //   o buraco de eu ter feito o grupo como escolha única.
    // · `a desvantagem exigida sao TRES escolhas` — o traço entra por **valor
    //   livre**, não como um grupo de opções fechadas.
    //
    // O que vale agora está em `CorrecaoDosPoderesTest`.

    @Test
    fun `o exemplo do livro monta o poder Calor-Fogo`() {
        // Calor/Fogo recebe −10% por contramedidas mundanas (p.21).
        val mundanas = MontadorDeModificador.CATALOGO
            .first { it.valor == -10 && it.grupo == MontadorDeModificador.Grupo.CONTRAMEDIDAS }
        assertEquals(-10, MontadorDeModificador.total(listOf(mundanas)))
    }

    @Test
    fun `so o grupo marcado escolha um da conflito`() {
        // 🔴 ESTE TESTE DIZIA QUE CONTRAMEDIDAS ERA ESCOLHA ÚNICA — errado, e
        // corrigido no POD-16. O livro só marca "(escolha um)" em alguns grupos;
        // nos outros, "todos os modificadores serão cumulativos" (p.25).
        val energias = MontadorDeModificador.CATALOGO
            .filter { it.grupo == MontadorDeModificador.Grupo.ENERGIAS_CANALIZADAS }.take(2)
        assertEquals(listOf(MontadorDeModificador.Grupo.ENERGIAS_CANALIZADAS),
            MontadorDeModificador.conflitosDeGrupo(energias))

        // Contramedidas e "outros" SOMAM — nenhum dos dois conflita.
        listOf(MontadorDeModificador.Grupo.CONTRAMEDIDAS, MontadorDeModificador.Grupo.EXTRAS)
            .forEach { g ->
                val todos = MontadorDeModificador.CATALOGO.filter { it.grupo == g }
                assertTrue("'${g.rotulo}' voltou a ser escolha unica",
                    MontadorDeModificador.conflitosDeGrupo(todos).isEmpty())
            }
    }

    @Test
    fun `o teto de menos oitenta vale aqui tambem`() {
        val tudoRuim = MontadorDeModificador.CATALOGO.filter { it.valor < 0 }
        assertTrue(MontadorDeModificador.total(tudoRuim) >= -80)
    }

    @Test
    fun `a faixa recomendada avisa, mas nao trava`() {
        assertNull(MontadorDeModificador.avisoDaFaixa(-10))
        assertNull(MontadorDeModificador.avisoDaFaixa(-25))
        assertNotNull(MontadorDeModificador.avisoDaFaixa(-5))
        assertNotNull(MontadorDeModificador.avisoDaFaixa(-60))
        val positivo = MontadorDeModificador.avisoDaFaixa(50)!!
        assertTrue(positivo, positivo.contains("amplia"))
    }

    // ══ POD-12 — Esforço adicional e custo em PF (p.159-161) ═══════════

    @Test
    fun `o exemplo do livro da menos tres`() {
        // 🔴 O plano dizia "1 PF = +15% de efeito". A regra troca PENALIDADE DE
        // VONTADE por efeito: "Atribulação 9 representa 12,5% mais efeito do que
        // a 8, por isso exigiria uma rolagem com −3".
        assertEquals(-3, EsforcoAdicional.penalidade(13))   // 12,5% arredonda para cima
        assertEquals(-1, EsforcoAdicional.penalidade(5))
        assertEquals(-1, EsforcoAdicional.penalidade(1))    // "ou fração"
        assertEquals(-2, EsforcoAdicional.penalidade(10))
        assertEquals(-4, EsforcoAdicional.penalidade(20))
    }

    @Test
    fun `o teto e cem por cento a menos vinte`() {
        assertEquals(-20, EsforcoAdicional.penalidade(100))
        assertEquals("nao pode passar de -20", -20, EsforcoAdicional.penalidade(500))
        assertTrue(EsforcoAdicional.passouDoTeto(101))
        assertFalse(EsforcoAdicional.passouDoTeto(100))
        assertNotNull(EsforcoAdicional.avisoDoTeto(150))
        assertNull(EsforcoAdicional.avisoDoTeto(50))
    }

    @Test
    fun `esforco adicional nao vale para ataque nem para passiva`() {
        // "a opção equivalente para ataques é o Ataque Total (Determinado)"
        assertTrue(EsforcoAdicional.podeUsarEsforcoAdicional(
            exigeTesteDeAtivacao = true, exigeTesteDeAtaque = false))
        assertFalse(EsforcoAdicional.podeUsarEsforcoAdicional(
            exigeTesteDeAtivacao = true, exigeTesteDeAtaque = true))
        assertFalse("habilidade passiva nao usa", EsforcoAdicional.podeUsarEsforcoAdicional(
            exigeTesteDeAtivacao = false, exigeTesteDeAtaque = false))
    }

    @Test
    fun `o custo em PF do uso e por minuto ou por hora`() {
        assertEquals(5, CustoEmPfDoUso.pfGastos(CustoEmPfDoUso.Intensidade.INTENSIVO, 5))
        assertEquals(2, CustoEmPfDoUso.pfGastos(CustoEmPfDoUso.Intensidade.PROLONGADO, 120))
        assertEquals(0, CustoEmPfDoUso.pfGastos(CustoEmPfDoUso.Intensidade.PROLONGADO, 59))
        assertEquals(0, CustoEmPfDoUso.pfGastos(CustoEmPfDoUso.Intensidade.INTENSIVO, 0))
    }

    // ══ POD-13 — Ampliações temporárias e multiplicação ════════════════

    @Test
    fun `menos um por dez por cento, ou fracao`() {
        assertEquals(-1, AmpliacoesTemporarias.penalidadeCrua(10))
        assertEquals(-2, AmpliacoesTemporarias.penalidadeCrua(11))   // fração conta
        assertEquals(-5, AmpliacoesTemporarias.penalidadeCrua(50))
        assertEquals(0, AmpliacoesTemporarias.penalidadeCrua(0))
    }

    @Test
    fun `PF e Talento compensam a penalidade, mas nunca viram bonus`() {
        // ⚠️ "mas nunca obter um bônus, ao final". Sem o teto em zero, gastar PF
        // de sobra viraria bônus de graça.
        assertEquals(-5, AmpliacoesTemporarias.modificadorFinal(50, 0, 0))
        assertEquals(-2, AmpliacoesTemporarias.modificadorFinal(50, 3, 0))
        assertEquals(0, AmpliacoesTemporarias.modificadorFinal(50, 3, 2))
        assertEquals("virou bonus", 0, AmpliacoesTemporarias.modificadorFinal(50, 9, 9))
    }

    @Test
    fun `a tentativa custa dois PF, ou nada em sucesso decisivo`() {
        assertEquals(2, AmpliacoesTemporarias.pfDaTentativa(0))
        assertEquals(5, AmpliacoesTemporarias.pfDaTentativa(3))
        assertEquals(0, AmpliacoesTemporarias.pfDaTentativa(3, sucessoDecisivo = true))
        assertEquals(3, UsoPredefinido.CUSTO_EM_PF)
    }

    @Test
    fun `a manobra e o atributo mudam com a natureza da habilidade`() {
        assertEquals("Concentrar", AmpliacoesTemporarias.Natureza.MENTAL.manobra)
        assertEquals("Vontade", AmpliacoesTemporarias.Natureza.MENTAL.atributo)
        assertEquals("Preparar", AmpliacoesTemporarias.Natureza.FISICA.manobra)
        assertEquals("HT", AmpliacoesTemporarias.Natureza.FISICA.atributo)
    }

    @Test
    fun `cada fonte tem a sua pericia substituta`() {
        assertEquals("Meditação", AmpliacoesTemporarias.periciaDaFonte("Chi"))
        assertEquals("Taumatologia", AmpliacoesTemporarias.periciaDaFonte("Mágico"))
        assertEquals("Ritual Religioso", AmpliacoesTemporarias.periciaDaFonte("Divina"))
        assertNull(AmpliacoesTemporarias.periciaDaFonte("Super"))
        assertNull(AmpliacoesTemporarias.periciaDaFonte(null))
    }

    @Test
    fun `a falha critica checa a incapacitacao do POD-11`() {
        assertTrue(AmpliacoesTemporarias.FALHA_CRITICA_CHECA_INCAPACITACAO)
    }

    @Test
    fun `o exemplo do livro para os dois modelos de modificador`() {
        // "+20% em ampliações e -50% em limitações resultam em -30%, portanto a
        // habilidade custa 70%" — e no multiplicativo, 120% × 50% = 60%.
        assertEquals(70, ModelosDeModificador.aditivo(100, 20, -50))
        assertEquals(60, ModelosDeModificador.multiplicativo(100, 20, -50))
    }

    @Test
    fun `o multiplicativo tambem corta as limitacoes em menos oitenta`() {
        assertEquals(20, ModelosDeModificador.multiplicativo(100, 0, -80))
        assertEquals("nao pode passar de -80", 20, ModelosDeModificador.multiplicativo(100, 0, -200))
    }

    @Test
    fun `nenhuma conta escorrega por ponto flutuante`() {
        // 🔴 Duas destas contas nasceram com `Double` e erravam por UM ponto em
        // alguns valores: `1.0 - 0.70` dá `0.30000000000000004`, e `1 + (-80)/100.0`
        // dá `0.19999999999999996`. Dois casos pontuais pegaram, mas caso pontual
        // não cobre uma classe de erro — esta varredura cobre.
        for (base in 1..60) {
            for (mod in -80..80 step 5) {
                val esperado = Math.floorDiv(base.toLong() * (100 + mod), 100L).toInt()
                assertEquals(
                    "aditivo escorregou em base=$base mod=$mod",
                    esperado,
                    ModelosDeModificador.aditivo(base, if (mod > 0) mod else 0, if (mod < 0) mod else 0)
                )
            }
        }
        for (pontos in 1..40) {
            val esperado = Math.floorDiv(pontos.toLong() * 3 * 30 + 99, 100L).toInt().coerceAtLeast(1)
            assertEquals(
                "a RE escorregou em $pontos pontos",
                esperado,
                ReservaDeEnergia.custo(pontos, setOf(Limitacao.CARGA_ESPECIAL))
            )
        }
    }
}
