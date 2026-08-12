package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Armadura Flexível e Trauma por Impacto** — MB p.380. Lote EQP-9.
 */
class TraumaPorImpactoTest {

    // ── A trava que cancela tudo ───────────────────────────────────────

    @Test
    fun `um unico ponto que penetre cancela o trauma`() {
        // 🔴 A frase do livro: "se um único ponto de dano penetrar a RD flexível,
        // não sofre trauma por impacto". Não reduz — CANCELA.
        //
        // Foi exatamente isto que eu ignorei ao apresentar a regra ao usuário:
        // dei como exemplo 10 de contusão contra RD 2*, que penetra 8 e portanto
        // não dá trauma nenhum.
        assertEquals(0, TraumaPorImpacto.calcular(10, DanoTipo.CONT, penetrante = 8, rdFlexivel = 2))
        assertEquals(0, TraumaPorImpacto.calcular(11, DanoTipo.CONT, penetrante = 1, rdFlexivel = 10))
        // E com zero penetrando, o mesmo golpe de 10 contra RD 10 flexível conta.
        assertEquals(2, TraumaPorImpacto.calcular(10, DanoTipo.CONT, penetrante = 0, rdFlexivel = 10))
    }

    @Test
    fun `armadura rigida nao da trauma`() {
        assertEquals(0, TraumaPorImpacto.calcular(20, DanoTipo.CONT, penetrante = 0, rdFlexivel = 0, rdRigida = 20))
    }

    // ── Os dois divisores ──────────────────────────────────────────────

    @Test
    fun `contusao a cada cinco, o resto a cada dez`() {
        assertEquals(5, TraumaPorImpacto.divisorDe(DanoTipo.CONT))
        listOf(
            DanoTipo.CORT, DanoTipo.PERF,
            DanoTipo.PI_MENOS, DanoTipo.PI, DanoTipo.PI_MAIS, DanoTipo.PI_MAIS_MAIS
        ).forEach {
            assertEquals("divisor errado para $it", 10, TraumaPorImpacto.divisorDe(it))
        }
    }

    @Test
    fun `so pontos COMPLETOS contam`() {
        // "Para cada 10 pontos completos" — 9 barrados de corte dão zero.
        assertEquals(0, TraumaPorImpacto.calcular(9, DanoTipo.CORT, 0, rdFlexivel = 9))
        assertEquals(1, TraumaPorImpacto.calcular(10, DanoTipo.CORT, 0, rdFlexivel = 10))
        assertEquals(1, TraumaPorImpacto.calcular(19, DanoTipo.CORT, 0, rdFlexivel = 19))
        assertEquals(2, TraumaPorImpacto.calcular(20, DanoTipo.CORT, 0, rdFlexivel = 20))

        assertEquals(0, TraumaPorImpacto.calcular(4, DanoTipo.CONT, 0, rdFlexivel = 4))
        assertEquals(1, TraumaPorImpacto.calcular(5, DanoTipo.CONT, 0, rdFlexivel = 5))
        assertEquals(1, TraumaPorImpacto.calcular(9, DanoTipo.CONT, 0, rdFlexivel = 9))
        assertEquals(2, TraumaPorImpacto.calcular(10, DanoTipo.CONT, 0, rdFlexivel = 10))
    }

    @Test
    fun `a armadura nao pode barrar mais do que a propria RD`() {
        // ⚠️ Achado pelo teste: eu tinha escrito que 9 de contusão contra RD 5
        // flexível daria 2 PV. Não dá -- a peça barra 5, os outros 4 PENETRAM, e
        // aí a trava cancela o trauma inteiro. O estado "dano 9, RD 5, nada
        // penetrou" nem existe; o `coerceIn` só garante que, se alguém chamar
        // assim, o número não infla.
        assertEquals(5, TraumaPorImpacto.barradoPelaCamadaFlexivel(9, rdFlexivel = 5, rdRigida = 0))
    }

    // ── A camada de fora ───────────────────────────────────────────────

    @Test
    fun `so o que passa da camada rigida chega na flexivel`() {
        // "Se uma segunda RD estiver sobreposta à RD flexível, somente o dano que
        // penetrar a camada externa é capaz de provocar trauma" (p.380).
        // 16 de contusão, placa 4 por fora e malha 12 por dentro: 12 chegam.
        assertEquals(2, TraumaPorImpacto.calcular(16, DanoTipo.CONT, 0, rdFlexivel = 12, rdRigida = 4))
        // Sem a placa, os 16 inteiros seriam barrados pela malha... mas ela só
        // barra 12, e o resto teria penetrado. Aqui o `penetrante` já diz isso.
        assertEquals(12, TraumaPorImpacto.barradoPelaCamadaFlexivel(16, rdFlexivel = 12, rdRigida = 4))
    }

    @Test
    fun `a camada rigida sozinha pode zerar o trauma`() {
        // Placa 20 por fora: nada chega na malha, nada de trauma.
        assertEquals(0, TraumaPorImpacto.calcular(15, DanoTipo.CONT, 0, rdFlexivel = 10, rdRigida = 20))
    }

    // ── O caso do usuário, e o que ele não conseguia reproduzir ────────

    @Test
    fun `a Tunica RD 2 flexivel nunca produz trauma`() {
        // Para nada penetrar o dano tem de ser no máximo 2 — e 2 está abaixo do
        // mínimo de 5 da contusão. Não existe golpe que dê trauma nesta peça.
        for (dano in 0..2) {
            assertEquals(
                "dano $dano na Túnica deu trauma",
                0,
                TraumaPorImpacto.calcular(dano, DanoTipo.CONT, penetrante = 0, rdFlexivel = 2)
            )
        }
    }

    @Test
    fun `o Traje Pressurizado RD 6 flexivel produz`() {
        // O exemplo que eu dei ao usuário depois de corrigir o errado.
        assertEquals(1, TraumaPorImpacto.calcular(6, DanoTipo.CONT, penetrante = 0, rdFlexivel = 6))
        // E com 10 de dano, 4 penetram → sem trauma, dano normal.
        assertEquals(0, TraumaPorImpacto.calcular(10, DanoTipo.CONT, penetrante = 4, rdFlexivel = 6))
    }

    // ── Ligado ao ferimento ────────────────────────────────────────────

    @Test
    fun `o ferimento aplica o trauma como PV de verdade`() {
        val r = FerimentoPorLocalRules.aplicar(
            pvInicial = 10, danoBruto = 6, tipo = DanoTipo.CONT,
            local = LocalAtaque.TORSO, rdArmadura = 6, rdFlexivel = 6
        )
        assertEquals(0, r.penetrante)
        assertEquals(1, r.traumaPorImpacto)
        assertEquals("o trauma não virou PV perdido", 1, r.pvPerdidos)
    }

    @Test
    fun `trauma e lesao normal nunca coexistem`() {
        // A invariante que deixa esta regra entrar sem mexer no resto do cálculo:
        // varre a tabela inteira em vez de conferir um ponto.
        for (dano in 0..30) {
            for (rd in 0..20) {
                val r = FerimentoPorLocalRules.aplicar(
                    pvInicial = 10, danoBruto = dano, tipo = DanoTipo.CONT,
                    local = LocalAtaque.TORSO, rdArmadura = rd, rdFlexivel = rd
                )
                assertFalse(
                    "dano $dano contra RD $rd deu lesão E trauma",
                    r.lesaoAntesDoTeto > 0 && r.traumaPorImpacto > 0
                )
                assertTrue("desperdício negativo em $dano/$rd", r.desperdicado >= 0)
            }
        }
    }

    @Test
    fun `sem RD flexivel o resultado nao mudou`() {
        // Regressão: quem chamava `aplicar` sem o parâmetro novo tem de continuar
        // com exatamente o mesmo número.
        for (dano in 1..25) {
            val comParametro = FerimentoPorLocalRules.aplicar(
                pvInicial = 10, danoBruto = dano, tipo = DanoTipo.CORT,
                local = LocalAtaque.TORSO, rdArmadura = 4, rdFlexivel = 0
            )
            val semParametro = FerimentoPorLocalRules.aplicar(
                pvInicial = 10, danoBruto = dano, tipo = DanoTipo.CORT,
                local = LocalAtaque.TORSO, rdArmadura = 4
            )
            assertEquals(semParametro.pvPerdidos, comParametro.pvPerdidos)
            assertEquals(0, comParametro.traumaPorImpacto)
        }
    }

    @Test
    fun `o cranio conta como rigido, porque e osso`() {
        // A RD 2 natural do crânio fica por DENTRO da armadura. Com um capacete
        // flexível de RD 4 e 6 de contusão: os 6 chegam ao capacete (a RD natural
        // é a de dentro), ele barra 4, 2 passariam — logo penetrante > 0.
        val r = FerimentoPorLocalRules.aplicar(
            pvInicial = 10, danoBruto = 6, tipo = DanoTipo.CONT,
            local = LocalAtaque.CRANIO, rdArmadura = 4, rdFlexivel = 4
        )
        // RD efetiva = 4 + 2 do crânio = 6 → nada penetra.
        assertEquals(0, r.penetrante)
        // Mas o osso barrou 2 deles, então a camada flexível só barrou 4.
        assertEquals(0, r.traumaPorImpacto)  // 4 ÷ 5 = 0
    }

    // ── A conta na tela ────────────────────────────────────────────────

    @Test
    fun `a conta explica de onde veio o PV`() {
        val texto = TraumaPorImpacto.conta(6, DanoTipo.CONT, rdFlexivel = 6, rdRigida = 0, trauma = 1)!!
        assertTrue(texto, texto.contains("6 barrados"))
        assertTrue(texto, texto.contains("5"))
        assertTrue(texto, texto.contains("1 PV"))
    }

    @Test
    fun `sem trauma nao ha conta a mostrar`() {
        assertNull(TraumaPorImpacto.conta(2, DanoTipo.CONT, 2, 0, trauma = 0))
        assertNull(TraumaPorImpacto.descricaoAcessivel(2, DanoTipo.CONT, 2, 0, trauma = 0))
    }

    @Test
    fun `a fala do trauma nao tem sinal cru`() {
        val r = FerimentoPorLocalRules.aplicar(
            pvInicial = 10, danoBruto = 6, tipo = DanoTipo.CONT,
            local = LocalAtaque.TORSO, rdArmadura = 6, rdFlexivel = 6
        )
        val falado = r.descricaoAcessivel(pvNovo = 9, pvInicial = 10)
        assertTrue(falado, falado.contains("trauma por impacto"))
        assertFalse("sinal cru na fala: $falado", RotuloAcessivel.temSinalCru(falado))
    }
}
