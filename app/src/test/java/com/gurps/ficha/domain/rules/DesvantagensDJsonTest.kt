package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.traits.CegueiraRule
import com.gurps.ficha.domain.rules.traits.TraitRuleRegistry
import com.gurps.ficha.model.AtributoBase
import com.gurps.ficha.model.DesvantagemSelecionada
import com.gurps.ficha.model.Dificuldade
import com.gurps.ficha.model.ModeloRacial
import com.gurps.ficha.model.PericiaSelecionada
import com.gurps.ficha.model.Personagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * As quatro correções do Lote D-FIX, todas achadas lendo o livro página a página.
 *
 * Três delas **consertam código que já estava rodando** — não são features novas.
 */
class DesvantagensDJsonTest {

    private fun desv(id: String, nome: String = id, nivel: Int = 1) =
        DesvantagemSelecionada(definicaoId = id, nome = nome, nivel = nivel)

    private fun p(vararg d: DesvantagemSelecionada, pericias: List<PericiaSelecionada> = emptyList()) =
        Personagem(
            nome = "T", destreza = 10, vitalidade = 10,
            desvantagens = d.toList(), pericias = pericias
        )

    private fun pericia(id: String, nome: String) = PericiaSelecionada(
        definicaoId = id, nome = nome,
        atributoBase = AtributoBase.DX, dificuldade = Dificuldade.MEDIA, pontosGastos = 4
    )

    // --- 1. Cegueira: −6 nas perícias de combate (MB p.127) ---

    @Test
    fun `Cegueira tira 6 de toda pericia de COMBATE`() {
        val pj = p(
            desv(CegueiraRule.ID, "Cegueira"),
            pericias = listOf(
                pericia("espada_curta", "Espada Curta"),
                pericia("arcos", "Arcos"),
                pericia("escalada", "Escalada")
            )
        )
        assertEquals(-6, TraitRuleRegistry.getSkillBonus(pj, "Espada Curta"))
        assertEquals("o livro penaliza as de distância também", -6,
            TraitRuleRegistry.getSkillBonus(pj, "Arcos"))
    }

    @Test
    fun `⚠️ Cegueira NAO toca pericia que nao e de combate`() {
        // Escalada continua igual: o -6 do livro é só para combate.
        val pj = p(
            desv(CegueiraRule.ID, "Cegueira"),
            pericias = listOf(pericia("escalada", "Escalada"))
        )
        assertEquals(0, TraitRuleRegistry.getSkillBonus(pj, "Escalada"))
    }

    @Test
    fun `sem Cegueira, nada muda`() {
        val pj = p(pericias = listOf(pericia("espada_curta", "Espada Curta")))
        assertEquals(0, TraitRuleRegistry.getSkillBonus(pj, "Espada Curta"))
    }

    // --- 2. 🔴 Cego não sofre penalidade de escuridão (MB p.127) ---

    @Test
    fun `🔴 a escuridao nao penaliza quem ja e cego`() {
        // "Seja qual for o caso, o personagem não sofre nenhuma outra penalidade
        // por atuar no escuro." O Lote LUZ-1 aplicava a todo mundo -- somar a luz
        // por cima do -6 seria cobrar duas vezes pela mesma cegueira.
        val cego = p(desv(IluminacaoRules.ID_CEGUEIRA, "Cegueira"))
        assertEquals(0, IluminacaoRules.penalidadeEfetiva(cego, -10).efetiva)
        assertEquals(0, IluminacaoRules.penalidadeEfetiva(cego, -5).efetiva)
        assertTrue(
            IluminacaoRules.penalidadeEfetiva(cego, -5).explicacao.contains("Cego")
        )
        // ...e a caixinha de escuridão nas perícias também não aparece para ele.
        assertEquals(null, IluminacaoRules.condicionalDaLuz(cego, -7))
    }

    // --- 3. Cegueira Noturna (MB p.127) ---

    @Test
    fun `⚠️ Cegueira Noturna - o dobro OU menos 3, o que for pior`() {
        val pj = p(desv(IluminacaoRules.ID_CEGUEIRA_NOTURNA, "Cegueira Noturna"))
        // Com -1 o dobro seria -2, mas -3 é pior -> vale -3.
        assertEquals(-3, IluminacaoRules.penalidadeEfetiva(pj, -1).efetiva)
        // Com -2 o dobro (-4) já é pior que -3.
        assertEquals(-4, IluminacaoRules.penalidadeEfetiva(pj, -2).efetiva)
        assertEquals(-6, IluminacaoRules.penalidadeEfetiva(pj, -3).efetiva)
        assertEquals(-8, IluminacaoRules.penalidadeEfetiva(pj, -4).efetiva)
    }

    @Test
    fun `⚠️ de menos 5 em diante ele age como CEGO, ou seja menos 10`() {
        // O salto é abrupto de propósito: não é o dobro de -5 (-10 por
        // coincidência), é a regra "age como se fosse completamente cego".
        val pj = p(desv(IluminacaoRules.ID_CEGUEIRA_NOTURNA, "Cegueira Noturna"))
        assertEquals(-10, IluminacaoRules.penalidadeEfetiva(pj, -5).efetiva)
        assertEquals(-10, IluminacaoRules.penalidadeEfetiva(pj, -7).efetiva)
        assertEquals(-10, IluminacaoRules.penalidadeEfetiva(pj, -10).efetiva)
    }

    @Test
    fun `com boa luz a Cegueira Noturna nao faz nada`() {
        val pj = p(desv(IluminacaoRules.ID_CEGUEIRA_NOTURNA, "Cegueira Noturna"))
        assertEquals(0, IluminacaoRules.penalidadeEfetiva(pj, 0).efetiva)
    }

    // --- 4. Míope dobra a distância (MB p.135) ---

    @Test
    fun `🔴 o miope conta o dobro da distancia`() {
        // 20 m contam como 40, que arredonda para o degrau de 50 -> -8, em vez
        // de -6. Sem isto o míope atira como quem enxerga -- e a diferença é
        // maior do que parece, porque o dobro atravessa dois degraus da tabela.
        assertEquals(-6, TabelaVelocidadeDistancia.penalidadeCombinada(20, 0, miope = false))
        assertEquals(-8, TabelaVelocidadeDistancia.penalidadeCombinada(20, 0, miope = true))
    }

    @Test
    fun `⚠️ o miope dobra so a DISTANCIA, nao a velocidade`() {
        // O livro fala de "distância até o alvo". 40 m + 30 m/s viram 80 + 30 =
        // 110 -> -11; dobrar tudo daria 140 -> -12.
        assertEquals(-11, TabelaVelocidadeDistancia.penalidadeCombinada(40, 30, miope = true))
    }

    @Test
    fun `a caixinha do miope so aparece para quem tem Disopia`() {
        assertFalse(DisopiaRules.tem(p()))
        assertTrue(DisopiaRules.tem(p(desv(DisopiaRules.ID, "Disopia"))))
    }

    // --- 5. Fácil de Matar (MB p.140) ---

    @Test
    fun `Facil de Matar tira do teste de EVITAR A MORTE`() {
        val pj = p(desv("facil_de_matar", "Fácil de Matar", nivel = 3))
        val morte = ResistenciaRules.testesDe(pj).first { it.rotulo.contains("morte") }
        assertEquals("HT 10 - 3", 7, morte.alvo)
        assertTrue(morte.origens.any { it.contains("Fácil de Matar") })
    }

    @Test
    fun `⚠️ Facil de Matar NAO toca veneno, doenca nem esforco`() {
        // "Isso não afeta a maioria dos testes normais de HT -- apenas aqueles
        // que servem para evitar a morte."
        val pj = p(desv("facil_de_matar", "Fácil de Matar", nivel = 3))
        listOf("veneno", "doença", "esforço").forEach { alvo ->
            val teste = ResistenciaRules.testesDe(pj).first { it.rotulo.lowercase().contains(alvo) }
            assertEquals("$alvo não pode ser afetado", 10, teste.alvo)
        }
    }

    @Test
    fun `⚠️ o teste de morte nunca desce abaixo de 3`() {
        // "Os testes de HT não podem ser reduzidos abaixo de 3." Um HT 10 está
        // limitado a Fácil de Matar 7 -- mas se a ficha tiver mais, o piso segura.
        val pj = p(desv("facil_de_matar", "Fácil de Matar", nivel = 20))
        val morte = ResistenciaRules.testesDe(pj).first { it.rotulo.contains("morte") }
        assertEquals(3, morte.alvo)
    }

    @Test
    fun `o marco de PV tambem respeita o Facil de Matar e o piso`() {
        val pj = p(desv("facil_de_matar", "Fácil de Matar", nivel = 2))
        val marco = MarcosDeVidaRules.testesAoPerderPv(pj, -9, -10).first()
        assertEquals(8, marco.alvo)
        assertTrue(marco.origens.any { it.contains("Fácil de Matar") })
    }

    @Test
    fun `desvantagem RACIAL de Facil de Matar tambem conta`() {
        val pj = Personagem(
            nome = "T", vitalidade = 10,
            modeloRacial = ModeloRacial(
                nome = "Frágil",
                desvantagens = listOf(desv("facil_de_matar", "Fácil de Matar", nivel = 2))
            )
        )
        val morte = ResistenciaRules.testesDe(pj).first { it.rotulo.contains("morte") }
        assertEquals(8, morte.alvo)
    }
}
