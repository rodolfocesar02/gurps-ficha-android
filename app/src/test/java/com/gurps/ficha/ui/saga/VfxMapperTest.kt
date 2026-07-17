package com.gurps.ficha.ui.saga

import com.gurps.ficha.domain.magic.MagiaMecanica
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Lote VFX-1: o efeito visual é derivado da MECÂNICA curada + elemento, não de heurística frágil.
 * Estes testes usam mágicas REAIS do catálogo, com a mecânica que a curadoria gravou.
 */
class VfxMapperTest {

    @Test fun `Bola de Fogo é projetil laranja`() {
        val mec = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d", tipoDano = "quei", entrega = "projetil")
        val e = VfxMapper.efeitoDe("Bola de Fogo", mec)
        assertEquals(ArquetipoVfx.PROJETIL, e.arquetipo)
        assertEquals(PaletaVfx.FOGO, e.paleta)
    }

    @Test fun `Relampago é projetil AZUL — mesmo arquetipo da Bola de Fogo, cor diferente`() {
        val mec = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d-1", tipoDano = "quei",
            entrega = "projetil", condicao = "atordoado")
        val e = VfxMapper.efeitoDe("Relâmpago", mec)
        assertEquals(ArquetipoVfx.PROJETIL, e.arquetipo)
        assertEquals(PaletaVfx.RAIO, e.paleta) // reaproveita o desenho, muda só a paleta
    }

    @Test fun `Bola de Fogo Explosiva é EXPLOSAO (entrega area)`() {
        val mec = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d", entrega = "area", tipoDano = "quei")
        assertEquals(ArquetipoVfx.EXPLOSAO, VfxMapper.arquetipoDe(mec))
    }

    @Test fun `Toque Chocante é TOQUE`() {
        val mec = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d+1", entrega = "toque", tipoDano = "quei")
        assertEquals(ArquetipoVfx.TOQUE, VfxMapper.arquetipoDe(mec))
    }

    @Test fun `Cegar é FLASH — projetil que cega vira clarao, nao dardo`() {
        val mec = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d", entrega = "projetil", condicao = "cego")
        assertEquals(ArquetipoVfx.FLASH, VfxMapper.arquetipoDe(mec))
    }

    @Test fun `Escudo é AURA dourada — buff sem elemento cai no arcano`() {
        val mec = MagiaMecanica(efeito = "buff", buffBd = 1, buffEnergiaPorNivel = 2, buffMaxNiveis = 4)
        val e = VfxMapper.efeitoDe("Escudo", mec)
        assertEquals(ArquetipoVfx.AURA, e.arquetipo)
        assertEquals(PaletaVfx.ARCANO, e.paleta)
    }

    @Test fun `Arma Flamejante é AURA, mas LARANJA — a paleta segue o elemento do nome`() {
        val mec = MagiaMecanica(efeito = "buff", buffDanoArma = 2, buffArmaTipo = "cac")
        val e = VfxMapper.efeitoDe("Arma Flamejante", mec)
        assertEquals(ArquetipoVfx.AURA, e.arquetipo)
        assertEquals(PaletaVfx.FOGO, e.paleta)
    }

    @Test fun `Sono é MENTAL roxo`() {
        val mec = MagiaMecanica(efeito = "condicao", condicao = "dormindo")
        val e = VfxMapper.efeitoDe("Sono", mec)
        assertEquals(ArquetipoVfx.MENTAL, e.arquetipo)
        assertEquals(PaletaVfx.MENTE, e.paleta)
    }

    @Test fun `Adaga de Gelo é projetil CIANO`() {
        val mec = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d-1", tipoDano = "perf", entrega = "projetil")
        assertEquals(PaletaVfx.GELO, VfxMapper.efeitoDe("Adaga de Gelo", mec).paleta)
    }

    @Test fun `Jato de Acido é projetil VERDE`() {
        val mec = MagiaMecanica(efeito = "dano", danoPorEnergia = "1d-1", entrega = "feixe", condicao = "cego")
        // Jato de Ácido cega → FLASH; mas a paleta é ácido (verde).
        assertEquals(PaletaVfx.ACIDO, VfxMapper.efeitoDe("Jato de Ácido", mec).paleta)
    }
}
