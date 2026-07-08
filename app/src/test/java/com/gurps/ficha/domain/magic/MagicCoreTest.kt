package com.gurps.ficha.domain.magic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote MA-1: helpers de mana, custo, distância, operação, choque e magias ativas. Kotlin puro. */
class MagicCoreTest {

    // ─── Mana ─────────────────────────────────────────────

    @Test fun `mana NORMAL sem penalidade e mago pode operar`() {
        assertEquals(0, MagicMana.penalidadeMana(NivelMana.NORMAL))
        assertTrue(MagicMana.podeOperar(NivelMana.NORMAL, ehMago = true))
        assertFalse(MagicMana.podeOperar(NivelMana.NORMAL, ehMago = false))
    }

    @Test fun `mana BAIXA aplica menos 5 no NH`() {
        assertEquals(-5, MagicMana.penalidadeMana(NivelMana.BAIXA))
    }

    @Test fun `mana NULA impede operar mesmo para mago`() {
        assertFalse(MagicMana.podeOperar(NivelMana.NULA, ehMago = true))
    }

    @Test fun `mana ALTA permite nao-magos operarem`() {
        assertTrue(MagicMana.podeOperar(NivelMana.ALTA, ehMago = false))
    }

    // ─── Custo por NH (Magia p.8) ──────────────────────────

    @Test fun `NH menor que 15 nao reduz custo`() {
        assertEquals(3, MagicCost.custoAjustadoPorNH(3, nh = 12))
    }

    @Test fun `NH 15 reduz 1 do custo`() {
        assertEquals(2, MagicCost.custoAjustadoPorNH(3, nh = 15))
    }

    @Test fun `NH 20 reduz 2 do custo`() {
        assertEquals(1, MagicCost.custoAjustadoPorNH(3, nh = 20))
    }

    @Test fun `NH 25 reduz 3 do custo`() {
        assertEquals(2, MagicCost.custoAjustadoPorNH(5, nh = 25))
    }

    @Test fun `piso zero mesmo com NH altissimo`() {
        assertEquals(0, MagicCost.custoAjustadoPorNH(1, nh = 30))
    }

    @Test fun `custo zero permanece zero (magias sem custo)`() {
        assertEquals(0, MagicCost.custoAjustadoPorNH(0, nh = 12))
    }

    // ─── Custo de Area (Magia p.11) ──────────────────────

    @Test fun `area custo basico 2 raio 3 vira 6`() {
        assertEquals(6, MagicCost.custoAreaPorRaio(2.0, 3))
    }

    @Test fun `area custo basico fracionario meio raio 4 vira 2`() {
        assertEquals(2, MagicCost.custoAreaPorRaio(0.5, 4))
    }

    @Test fun `area custo fracionario com raio pequeno tem piso 1`() {
        assertEquals(1, MagicCost.custoAreaPorRaio(0.1, 3))
    }

    // ─── Custo por Modificador de Tamanho ──────────────

    @Test fun `MT positivo mais 1 multiplica custo por 2`() {
        assertEquals(10, MagicCost.custoAjustadoPorTamanho(5, mtAlvo = 1))
    }

    @Test fun `MT negativo nao reduz`() {
        assertEquals(5, MagicCost.custoAjustadoPorTamanho(5, mtAlvo = -3))
    }

    // ─── Distancia ─────────────────────────────────────

    @Test fun `penalidade distancia em metros e negativa`() {
        assertEquals(-5, MagicDistance.penalidadeDistanciaMetros(5))
    }

    @Test fun `penalidade hex igual metros no PILAR`() {
        assertEquals(-3, MagicDistance.penalidadeDistanciaHex(3))
    }

    // ─── Multiplas magias (Magia p.10) ────────────────

    @Test fun `sem magias ativas penalidade zero`() {
        assertEquals(0, MagicMultiplasMagias.penalidade(0, 0))
    }

    @Test fun `duas magias em concentracao aplicam menos 6`() {
        assertEquals(-6, MagicMultiplasMagias.penalidade(2, 0))
    }

    @Test fun `tres magias em andamento aplicam menos 3`() {
        assertEquals(-3, MagicMultiplasMagias.penalidade(0, 3))
    }

    // ─── Resultado da operacao (Magia p.7) ────────────

    @Test fun `sucesso decisivo em 3 ou 4`() {
        assertEquals(ResultadoOperacao.SUCESSO_DECISIVO, MagicOperationRuling.classificar(3, nhEfetivo = 12))
        assertEquals(ResultadoOperacao.SUCESSO_DECISIVO, MagicOperationRuling.classificar(4, nhEfetivo = 12))
    }

    @Test fun `sucesso decisivo em 5 quando NH pelo menos 15`() {
        assertEquals(ResultadoOperacao.SUCESSO_DECISIVO, MagicOperationRuling.classificar(5, nhEfetivo = 15))
        assertEquals(ResultadoOperacao.SUCESSO, MagicOperationRuling.classificar(5, nhEfetivo = 14))
    }

    @Test fun `falha critica em 18 sempre`() {
        assertEquals(ResultadoOperacao.FALHA_CRITICA, MagicOperationRuling.classificar(18, nhEfetivo = 30))
    }

    @Test fun `falha critica em 17 se NH menor que 16`() {
        assertEquals(ResultadoOperacao.FALHA_CRITICA, MagicOperationRuling.classificar(17, nhEfetivo = 15))
        assertEquals(ResultadoOperacao.FRACASSO, MagicOperationRuling.classificar(17, nhEfetivo = 16))
    }

    @Test fun `falha critica por margem 10`() {
        assertEquals(ResultadoOperacao.FALHA_CRITICA, MagicOperationRuling.classificar(15, nhEfetivo = 5))
    }

    @Test fun `sucesso normal`() {
        assertEquals(ResultadoOperacao.SUCESSO, MagicOperationRuling.classificar(10, nhEfetivo = 12))
    }

    @Test fun `fracasso normal`() {
        assertEquals(ResultadoOperacao.FRACASSO, MagicOperationRuling.classificar(13, nhEfetivo = 12))
    }

    // ─── Custo a pagar ────────────────────────────────

    @Test fun `sucesso decisivo perdoa o custo`() {
        assertEquals(0, MagicOperationRuling.custoAPagar(ResultadoOperacao.SUCESSO_DECISIVO, 5, false))
    }

    @Test fun `sucesso paga total`() {
        assertEquals(5, MagicOperationRuling.custoAPagar(ResultadoOperacao.SUCESSO, 5, false))
    }

    @Test fun `fracasso comum paga 1 ponto`() {
        assertEquals(1, MagicOperationRuling.custoAPagar(ResultadoOperacao.FRACASSO, 5, ehInformacao = false))
    }

    @Test fun `fracasso em Informacao paga total`() {
        assertEquals(5, MagicOperationRuling.custoAPagar(ResultadoOperacao.FRACASSO, 5, ehInformacao = true))
    }

    @Test fun `falha critica paga total`() {
        assertEquals(5, MagicOperationRuling.custoAPagar(ResultadoOperacao.FALHA_CRITICA, 5, false))
    }

    // ─── Choque de retorno ────────────────────────────

    @Test fun `choque 3 causa 1d dano`() {
        val e = MagicChoqueRetorno.consultar(3)
        assertEquals(1, e.danoAoOperadorDadosD6)
    }

    @Test fun `choque 8 causa 1 ponto de dano`() {
        val e = MagicChoqueRetorno.consultar(8)
        assertEquals(1, e.danoAoOperadorPontos)
    }

    @Test fun `choque 9 atordoa o operador`() {
        val e = MagicChoqueRetorno.consultar(9)
        assertTrue(e.atordoaOperador)
    }

    @Test fun `todas as rolagens 3 a 18 retornam efeito nao nulo`() {
        for (r in 3..18) {
            val e = MagicChoqueRetorno.consultar(r)
            assertTrue("choque $r deve ter rotulo", e.rotulo.isNotBlank())
        }
    }

    // ─── Magia ativa no combate ──────────────────────

    private fun ativa(id: String = "luz", operador: String = "heroi",
                     custoManu: Int = 1, timer: Int = 60, duracao: Int = 60,
                     tipo: TipoDuracao = TipoDuracao.TEMPORARIA): MagiaAtivaNoCombate =
        MagiaAtivaNoCombate(
            magiaId = id, operadorId = operador, alvoId = operador,
            energiaInvestida = 1, custoManutencaoSeg = custoManu,
            segundosParaProximaCobranca = timer, duracaoTotalSeg = duracao,
            duracao = tipo, exigeConcentracao = false
        )

    @Test fun `PERMANENTE nao expira nem cobra`() {
        val r = MagicActive.avancarTurnoSegundos(
            listOf(ativa(tipo = TipoDuracao.PERMANENTE)), segundos = 10
        )
        assertEquals(1, r.ativasApos.size)
        assertEquals(emptyMap<String, Int>(), r.cobrancasPorOperador)
        assertTrue(r.expiradas.isEmpty())
    }

    @Test fun `TEMPORARIA cobra manutencao quando timer chega em zero e reseta`() {
        val r = MagicActive.avancarTurnoSegundos(
            listOf(ativa(custoManu = 1, timer = 5, duracao = 60)), segundos = 5
        )
        assertEquals(1, r.ativasApos.size)
        assertEquals(60, r.ativasApos[0].segundosParaProximaCobranca) // resetou
        assertEquals(1, r.cobrancasPorOperador["heroi"])
    }

    @Test fun `TEMPORARIA nao cobra se ainda ha tempo`() {
        val r = MagicActive.avancarTurnoSegundos(
            listOf(ativa(timer = 30)), segundos = 5
        )
        assertEquals(25, r.ativasApos[0].segundosParaProximaCobranca)
        assertTrue(r.cobrancasPorOperador.isEmpty())
    }

    @Test fun `DURADOURA expira quando timer chega em zero`() {
        val r = MagicActive.avancarTurnoSegundos(
            listOf(ativa(timer = 5, tipo = TipoDuracao.DURADOURA)), segundos = 5
        )
        assertTrue(r.ativasApos.isEmpty())
        assertEquals(1, r.expiradas.size)
    }

    @Test fun `INSTANTANEA e filtrada (nunca deveria estar ativa)`() {
        val r = MagicActive.avancarTurnoSegundos(
            listOf(ativa(tipo = TipoDuracao.INSTANTANEA)), segundos = 1
        )
        assertTrue(r.ativasApos.isEmpty())
    }

    @Test fun `duas magias temporarias do mesmo operador somam cobranca`() {
        val r = MagicActive.avancarTurnoSegundos(
            listOf(
                ativa(id = "a", custoManu = 1, timer = 5, duracao = 60),
                ativa(id = "b", custoManu = 2, timer = 5, duracao = 60)
            ), segundos = 5
        )
        assertEquals(3, r.cobrancasPorOperador["heroi"])
    }
}
