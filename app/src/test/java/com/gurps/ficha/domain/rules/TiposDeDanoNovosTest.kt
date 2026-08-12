package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
