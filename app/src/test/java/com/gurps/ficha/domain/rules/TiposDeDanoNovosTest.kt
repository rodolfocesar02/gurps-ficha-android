package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Os tipos de dano que faltavam** — MB p.380. Lote EQP-12 em diante.
 *
 * A tabela do livro, palavra por palavra:
 *
 * > *"Pouco perfurante (pa-): ×0,5. Por queimadura (qmd), corrosão (cor),
 * > contusão (cont), fadiga (fad), toxina (tox), e perfurante (pa): ×1. Corte
 * > (corte) e muito perfurante (pa+): ×1,5. Perfuração (perf) e extremamente
 * > perfurante (pa++): ×2."*
 */
class TiposDeDanoNovosTest {

    @Test
    fun `o multiplicador de cada tipo e o da tabela`() {
        // Uma linha por linha do livro. Se alguém mexer num número, aqui reprova.
        assertEquals(0.5, DanoTipo.PI_MENOS.multBase, 0.0)
        assertEquals(1.0, DanoTipo.PI.multBase, 0.0)
        assertEquals(1.0, DanoTipo.CONT.multBase, 0.0)
        assertEquals(1.0, DanoTipo.QMD.multBase, 0.0)
        assertEquals(1.5, DanoTipo.CORT.multBase, 0.0)
        assertEquals(1.5, DanoTipo.PI_MAIS.multBase, 0.0)
        assertEquals(2.0, DanoTipo.PERF.multBase, 0.0)
        assertEquals(2.0, DanoTipo.PI_MAIS_MAIS.multBase, 0.0)
    }

    // ── 🔴 A armadilha que o EQP-12 desarmou ───────────────────────────

    @Test
    fun `so o perfurante e a perfuracao triplicam nos vitais`() {
        // A propriedade era escrita AO CONTRÁRIO (`this != CONT && this != CORT`),
        // então **todo tipo novo entrava valendo ×3 nos vitais sozinho** — e sem
        // nenhum teste quebrar, porque todos os `when` do projeto têm `else`.
        val devemTriplicar = setOf(
            DanoTipo.PI_MENOS, DanoTipo.PI, DanoTipo.PI_MAIS, DanoTipo.PI_MAIS_MAIS, DanoTipo.PERF
        )
        DanoTipo.entries.forEach { tipo ->
            assertEquals(
                "'${tipo.rotulo}' está do lado errado do ×3 nos vitais",
                tipo in devemTriplicar,
                tipo.perfuranteOuPerf
            )
        }
    }

    @Test
    fun `queimadura nao triplica nos vitais`() {
        // O caso concreto da armadilha.
        assertFalse(DanoTipo.QMD.perfuranteOuPerf)
        assertEquals(
            1.0,
            FerimentoPorLocalRules.multiplicador(DanoTipo.QMD, LocalAtaque.VITAIS),
            0.0
        )
        // E continua ×4 no crânio, que vale para todos os tipos (p.399).
        assertEquals(
            4.0,
            FerimentoPorLocalRules.multiplicador(DanoTipo.QMD, LocalAtaque.CRANIO),
            0.0
        )
    }

    // ── Trauma por impacto só para quem o livro lista ──────────────────

    @Test
    fun `queimadura nao causa trauma por impacto`() {
        // "Um ataque que provoca dano por contusão, corte, perfuração ou
        // perfurante pode provocar trauma por impacto" — queimadura não está lá.
        assertFalse(DanoTipo.QMD.causaTraumaPorImpacto)
        assertEquals(
            0,
            TraumaPorImpacto.calcular(20, DanoTipo.QMD, penetrante = 0, rdFlexivel = 20)
        )
        // O mesmo golpe de contusão daria 4.
        assertEquals(
            4,
            TraumaPorImpacto.calcular(20, DanoTipo.CONT, penetrante = 0, rdFlexivel = 20)
        )
    }

    @Test
    fun `os quatro tipos do livro causam trauma, e so eles`() {
        val devemCausar = setOf(
            DanoTipo.CONT, DanoTipo.CORT, DanoTipo.PERF,
            DanoTipo.PI_MENOS, DanoTipo.PI, DanoTipo.PI_MAIS, DanoTipo.PI_MAIS_MAIS
        )
        DanoTipo.entries.forEach { tipo ->
            assertEquals(
                "'${tipo.rotulo}' está do lado errado do trauma por impacto",
                tipo in devemCausar,
                tipo.causaTraumaPorImpacto
            )
        }
    }

    // ── A leitura do catálogo ──────────────────────────────────────────

    @Test
    fun `o texto do catalogo reconhece queimadura`() {
        // As 11 armas que não tinham botão: laser, feixe iônico, lança-chamas,
        // espada de energia.
        assertEquals(DanoTipo.QMD, TipoDeDanoNoTexto.ler("3d(2) qmd"))
        assertEquals(DanoTipo.QMD, TipoDeDanoNoTexto.ler("8d(5) qmd"))
        assertEquals(DanoTipo.QMD, TipoDeDanoNoTexto.ler("3d qmd"))
    }

    @Test
    fun `queimadura nao ganha bonus de qualidade de arma`() {
        // Não é lâmina — a espada de energia superior não bate mais forte.
        assertFalse(QualidadeDaArma.ehLamina(DanoTipo.QMD))
        assertEquals(0, QualidadeDaArma.bonusDeDano(QualidadeDaArma.Nivel.ALTISSIMA, DanoTipo.QMD))
    }

    @Test
    fun `todo tipo tem rotulo curto e unico`() {
        val rotulos = DanoTipo.entries.map { it.rotulo }
        assertEquals("há rótulos repetidos: $rotulos", rotulos.size, rotulos.toSet().size)
        assertTrue(rotulos.all { it.isNotBlank() })
    }
}

/**
 * **Corrosão e Lesão** — MB p.380. Lote EQP-13.
 */
class CorrosaoNaArmaduraTest {

    @Test
    fun `cinco pontos penetrantes comem um de RD`() {
        assertEquals(0, CorrosaoNaArmadura.rdDestruida(DanoTipo.COR, 4))
        assertEquals(1, CorrosaoNaArmadura.rdDestruida(DanoTipo.COR, 5))
        assertEquals(1, CorrosaoNaArmadura.rdDestruida(DanoTipo.COR, 9))
        assertEquals(2, CorrosaoNaArmadura.rdDestruida(DanoTipo.COR, 10))
        assertEquals(4, CorrosaoNaArmadura.rdDestruida(DanoTipo.COR, 23))
    }

    @Test
    fun `so a corrosao come armadura`() {
        DanoTipo.entries.filter { it != DanoTipo.COR }.forEach { tipo ->
            assertEquals(
                "'${tipo.rotulo}' corroeu a armadura",
                0,
                CorrosaoNaArmadura.rdDestruida(tipo, 20)
            )
        }
    }

    @Test
    fun `acido barrado pela armadura nao a corroi`() {
        // ⚠️ A base é o dano PENETRANTE, não o rolado. O que a armadura segura
        // inteiro escorre; é o que passa que a come por dentro.
        assertEquals(0, CorrosaoNaArmadura.rdDestruida(DanoTipo.COR, penetrante = 0))
        val r = FerimentoPorLocalRules.aplicar(
            pvInicial = 10, danoBruto = 20, tipo = DanoTipo.COR,
            local = LocalAtaque.TORSO, rdArmadura = 20
        )
        assertEquals(0, r.penetrante)
        assertEquals(0, r.rdDestruidaPorCorrosao)
    }

    @Test
    fun `o ferimento devolve a RD destruida e avisa`() {
        val r = FerimentoPorLocalRules.aplicar(
            pvInicial = 20, danoBruto = 16, tipo = DanoTipo.COR,
            local = LocalAtaque.TORSO, rdArmadura = 6
        )
        assertEquals(10, r.penetrante)
        assertEquals(2, r.rdDestruidaPorCorrosao)
        // O dano continua ×1 — a corrosão come armadura, não multiplica ferimento.
        assertEquals(10, r.pvPerdidos)
        assertTrue(
            "o aviso não apareceu: ${r.avisos}",
            r.avisos.any { it.contains("Corrosão") && it.contains("RD") }
        )
    }

    @Test
    fun `sem corrosao nao ha aviso nenhum`() {
        val r = FerimentoPorLocalRules.aplicar(
            pvInicial = 20, danoBruto = 16, tipo = DanoTipo.CONT,
            local = LocalAtaque.TORSO, rdArmadura = 6
        )
        assertEquals(0, r.rdDestruidaPorCorrosao)
        assertTrue(r.avisos.none { it.contains("Corrosão") })
        assertNull(CorrosaoNaArmadura.conta(10, 0))
        assertNull(CorrosaoNaArmadura.contaAcessivel(10, 0))
    }

    @Test
    fun `corrosao nao triplica nos vitais nem da trauma`() {
        assertFalse(DanoTipo.COR.perfuranteOuPerf)
        assertFalse(DanoTipo.COR.causaTraumaPorImpacto)
        assertEquals(
            1.0,
            FerimentoPorLocalRules.multiplicador(DanoTipo.COR, LocalAtaque.VITAIS),
            0.0
        )
    }

    @Test
    fun `a fala da corrosao nao tem sinal cru`() {
        val falado = CorrosaoNaArmadura.contaAcessivel(10, 2)!!
        assertFalse(falado, RotuloAcessivel.temSinalCru(falado))
        assertTrue(falado, falado.contains("não se recupera"))
    }
}

/**
 * **Fadiga, toxina e atribulação** — MB p.43-44. Lote EQP-14.
 */
class FadigaToxinaAtribulacaoTest {

    private fun golpe(tipo: DanoTipo, dano: Int = 10, rd: Int = 0) =
        FerimentoPorLocalRules.aplicar(
            pvInicial = 10, danoBruto = dano, tipo = tipo,
            local = LocalAtaque.TORSO, rdArmadura = rd
        )

    // ── Fadiga vai para o outro medidor ────────────────────────────────

    @Test
    fun `so a fadiga desconta PF`() {
        // "Ele reduz o número de PF, não de PV" (p.43).
        DanoTipo.entries.forEach { tipo ->
            assertEquals("'${tipo.rotulo}'", tipo == DanoTipo.FAD, tipo.atingePf)
        }
        assertTrue(golpe(DanoTipo.FAD).atingePf)
        assertFalse(golpe(DanoTipo.CONT).atingePf)
    }

    @Test
    fun `a conta da fadiga e a mesma, so muda o destino`() {
        // 🔴 O ponto: a regra NÃO muda a aritmética. Um choque de 10 contra RD 4
        // dá 6, exatamente como uma contusão daria — mas em PF.
        val fadiga = golpe(DanoTipo.FAD, dano = 10, rd = 4)
        val contusao = golpe(DanoTipo.CONT, dano = 10, rd = 4)
        assertEquals(contusao.pvPerdidos, fadiga.pvPerdidos)
        assertTrue(fadiga.atingePf)
        assertFalse(contusao.atingePf)
    }

    @Test
    fun `fadiga nao triplica nos vitais nem da trauma`() {
        assertFalse(DanoTipo.FAD.perfuranteOuPerf)
        assertFalse(DanoTipo.FAD.causaTraumaPorImpacto)
        assertEquals(1.0, FerimentoPorLocalRules.multiplicador(DanoTipo.FAD, LocalAtaque.VITAIS), 0.0)
    }

    // ── Toxina é dano comum ────────────────────────────────────────────

    @Test
    fun `toxina desconta PV normalmente`() {
        val r = golpe(DanoTipo.TOX, dano = 10, rd = 4)
        assertFalse(r.atingePf)
        assertFalse(r.ehAtribulacao)
        assertEquals(6, r.pvPerdidos)
    }

    // ── Atribulação não é dano ─────────────────────────────────────────

    @Test
    fun `atribulacao nao tira ponto nenhum`() {
        // ⚠️ Não é dano: o que penetra vira teste de HT.
        val r = golpe(DanoTipo.AT, dano = 20)
        assertTrue(r.ehAtribulacao)
        assertEquals(0, r.pvPerdidos)
        assertFalse(r.atingePf)
    }

    @Test
    fun `so a atribulacao deixa de tirar pontos`() {
        DanoTipo.entries.forEach { tipo ->
            assertEquals("'${tipo.rotulo}'", tipo != DanoTipo.AT, tipo.causaPerdaDePontos)
        }
    }

    @Test
    fun `atribulacao com armadura continua sem tirar ponto`() {
        assertEquals(0, golpe(DanoTipo.AT, dano = 20, rd = 30).pvPerdidos)
        assertEquals(0, golpe(DanoTipo.AT, dano = 1, rd = 0).pvPerdidos)
    }

    // ── A fronteira inteira, de uma vez ────────────────────────────────

    @Test
    fun `cada tipo novo esta do lado certo de todas as fronteiras`() {
        // Uma tabela só, para o próximo tipo que entrar não escorregar em
        // nenhuma das quatro propriedades sem alguém perceber.
        data class Esperado(
            val vitais3x: Boolean, val trauma: Boolean, val pf: Boolean, val tiraPontos: Boolean
        )
        val tabela = mapOf(
            DanoTipo.CONT to Esperado(false, true, false, true),
            DanoTipo.CORT to Esperado(false, true, false, true),
            DanoTipo.PERF to Esperado(true, true, false, true),
            DanoTipo.PI_MENOS to Esperado(true, true, false, true),
            DanoTipo.PI to Esperado(true, true, false, true),
            DanoTipo.PI_MAIS to Esperado(true, true, false, true),
            DanoTipo.PI_MAIS_MAIS to Esperado(true, true, false, true),
            DanoTipo.QMD to Esperado(false, false, false, true),
            DanoTipo.COR to Esperado(false, false, false, true),
            DanoTipo.FAD to Esperado(false, false, true, true),
            DanoTipo.TOX to Esperado(false, false, false, true),
            DanoTipo.AT to Esperado(false, false, false, false)
        )
        assertEquals("há tipo fora da tabela", DanoTipo.entries.size, tabela.size)
        tabela.forEach { (tipo, e) ->
            assertEquals("${tipo.rotulo}: ×3 nos vitais", e.vitais3x, tipo.perfuranteOuPerf)
            assertEquals("${tipo.rotulo}: trauma", e.trauma, tipo.causaTraumaPorImpacto)
            assertEquals("${tipo.rotulo}: PF", e.pf, tipo.atingePf)
            assertEquals("${tipo.rotulo}: tira pontos", e.tiraPontos, tipo.causaPerdaDePontos)
        }
    }
}
