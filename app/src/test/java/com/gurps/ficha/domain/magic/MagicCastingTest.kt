package com.gurps.ficha.domain.magic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lote MA-2: fidelidade do resolvedor de conjuração contra o livro Magia (pt_magia p.5–15).
 * Cada teste cita a regra que valida.
 */
class MagicCastingTest {

    private fun classe(s: String) = MagicClassParser.parse(s)

    // ── Parser do campo `energia` (string livre do catálogo) ──

    @Test fun `energia inteira simples`() {
        val c = MagicEnergy.parse("2")
        assertEquals(2, c.base); assertFalse(c.variavel)
    }

    @Test fun `energia com texto pega o primeiro numero`() {
        // "2 para um fogo que queima o dobro..." (Apressar Fogo)
        assertEquals(2, MagicEnergy.parse("2 para um fogo que queima o dobro").base)
    }

    @Test fun `energia Varia e variavel sem base`() {
        val c = MagicEnergy.parse("Varia")
        assertNull(c.base); assertTrue(c.variavel)
    }

    @Test fun `energia faixa 1 a 3`() {
        val c = MagicEnergy.parse("1 a 3")
        assertEquals(1, c.base); assertEquals(3, c.maximo); assertTrue(c.variavel)
    }

    /**
     * Lote MEC-5 — este teste MUDOU de significado, de propósito.
     *
     * Ele nascia no MA-2 assumindo que "1/2" no campo `energia` era a fração de área da Magia p.11.
     * A auditoria do catálogo INTEIRO (879) mostrou que essa suposição é falsa: **nenhuma mágica usa
     * fração pura nesse campo**. Todo "N/M" ali é operar/manter (263 casos: "04/02", "4/2#", "50/20")
     * ou custo por unidade ("1/4 litros", "250/0,5 kg"). O custo básico fracionário de área (Chuva =
     * 0,1; Correnteza = 0,02) vem da DESCRIÇÃO, não daqui.
     *
     * O regex de fração antigo, por casar primeiro, quebrava o custo de 307 mágicas reais para servir
     * a um formato que não existe. Agora "1/2" lê como as outras 263: operar 1, manter 2.
     */
    @Test fun `1-2 no campo energia e operar-manter, nao fracao — o catalogo nao tem fracao pura`() {
        val c = MagicEnergy.parse("1/2")
        assertEquals(1, c.base)
        assertEquals(2, c.manutencao)
        assertNull(c.fracao)
    }

    @Test fun `energia vazia vira variavel`() {
        assertTrue(MagicEnergy.parse(null).variavel)
    }

    // ── NH efetivo (p.7–11) ──

    @Test fun `mana baixa penaliza NH em -5`() {
        val ctx = ContextoConjuracao(nhBasico = 15, classe = classe("Comum"), mana = NivelMana.BAIXA)
        assertEquals(10, MagicCasting.nhEfetivo(ctx).valor)
    }

    @Test fun `distancia penaliza Comum mas nao Toque`() {
        val comum = ContextoConjuracao(nhBasico = 15, classe = classe("Comum"), distanciaMetros = 5)
        assertEquals(10, MagicCasting.nhEfetivo(comum).valor) // −5 por 5m (p.11)
        val toque = ContextoConjuracao(nhBasico = 15, classe = classe("Toque"), distanciaMetros = 5)
        assertEquals(15, MagicCasting.nhEfetivo(toque).valor) // Toque cria na mão → sem distância (p.12)
    }

    @Test fun `tocar zera a penalidade de distancia`() {
        val ctx = ContextoConjuracao(nhBasico = 15, classe = classe("Comum"), distanciaMetros = 5, tocando = true)
        assertEquals(15, MagicCasting.nhEfetivo(ctx).valor)
    }

    @Test fun `sem ver nem tocar soma -5 alem da distancia`() {
        val ctx = ContextoConjuracao(nhBasico = 15, classe = classe("Comum"), distanciaMetros = 3, veOuToca = false)
        assertEquals(15 - 3 - 5, MagicCasting.nhEfetivo(ctx).valor) // p.11
    }

    @Test fun `multiplas magias ativas -3 por concentracao e -1 por andamento`() {
        val ctx = ContextoConjuracao(nhBasico = 15, classe = classe("Comum"),
            magiasExigindoConcentracao = 1, magiasEmAndamento = 2)
        assertEquals(15 - 3 - 2, MagicCasting.nhEfetivo(ctx).valor) // p.10
    }

    @Test fun `queimar PV penaliza -1 por PV`() {
        val ctx = ContextoConjuracao(nhBasico = 15, classe = classe("Comum"), pvQueimados = 3)
        assertEquals(12, MagicCasting.nhEfetivo(ctx).valor) // p.8
    }

    // ── Custo (p.8/11) ──

    @Test fun `area multiplica pelo raio ANTES de reduzir por NH`() {
        // custo básico 2, raio 3 → 6 bruto; NH 15 reduz −1 → 5 (p.8: multiplica antes de reduzir).
        val ctx = ContextoConjuracao(nhBasico = 15, classe = classe("Área"), raioAreaMetros = 3)
        assertEquals(5, MagicCasting.custoTotal(ctx, MagicEnergy.parse("2")))
    }

    @Test fun `MT positivo multiplica custo de Comum`() {
        // MT +2 → ×3; base 2 → 6; NH 10 (sem redução) → 6 (p.11).
        val ctx = ContextoConjuracao(nhBasico = 10, classe = classe("Comum"), mtAlvo = 2)
        assertEquals(6, MagicCasting.custoTotal(ctx, MagicEnergy.parse("2")))
    }

    @Test fun `Bloqueio NUNCA reduz custo por NH alto`() {
        // NH 20 reduziria −2, mas Bloqueio é exceção (p.8/12): custo fica 3.
        val ctx = ContextoConjuracao(nhBasico = 20, classe = classe("Bloqueio"))
        assertEquals(3, MagicCasting.custoTotal(ctx, MagicEnergy.parse("3")))
        // Comparação: uma Comum com NH 20 reduziria de 3 → 1.
        val comum = ContextoConjuracao(nhBasico = 20, classe = classe("Comum"))
        assertEquals(1, MagicCasting.custoTotal(comum, MagicEnergy.parse("3")))
    }

    @Test fun `custo por NH usa apenas o -5 de mana baixa, nao a distancia`() {
        // NH básico 20 em mana baixa vira 15 p/ custo → reduz −1 (não −2). Base 3 → 2.
        val ctx = ContextoConjuracao(nhBasico = 20, classe = classe("Comum"), mana = NivelMana.BAIXA, distanciaMetros = 9)
        assertEquals(2, MagicCasting.custoTotal(ctx, MagicEnergy.parse("3")))
    }

    // ── Resolução da operação (p.7) ──

    @Test fun `sucesso decisivo perdoa o custo`() {
        val r = MagicCasting.resolver(nhEfetivo = 15, rolagem3d = 4, custoTotal = 5, classe = classe("Comum"))
        assertEquals(ResultadoOperacao.SUCESSO_DECISIVO, r.resultado)
        assertEquals(0, r.custoAPagar) // p.7
    }

    @Test fun `fracasso comum paga 1 mas Informacao paga tudo`() {
        val comum = MagicCasting.resolver(nhEfetivo = 10, rolagem3d = 14, custoTotal = 4, classe = classe("Comum"))
        assertEquals(ResultadoOperacao.FRACASSO, comum.resultado); assertEquals(1, comum.custoAPagar)
        val info = MagicCasting.resolver(nhEfetivo = 10, rolagem3d = 14, custoTotal = 4, classe = classe("Informação"))
        assertEquals(4, info.custoAPagar) // p.13: Informação paga custo total mesmo no fracasso
    }

    @Test fun `falha critica traz choque de retorno e paga tudo`() {
        val r = MagicCasting.resolver(nhEfetivo = 10, rolagem3d = 18, custoTotal = 5, classe = classe("Comum"))
        assertEquals(ResultadoOperacao.FALHA_CRITICA, r.resultado)
        assertEquals(5, r.custoAPagar)
        assertTrue(r.choqueRetorno != null)
    }

    @Test fun `resistivel exige disputa no sucesso normal mas nao no decisivo`() {
        // Sucesso normal (rolagem 12 vs NH 15) → exige resistência (p.13).
        val normal = MagicCasting.resolver(nhEfetivo = 15, rolagem3d = 12, custoTotal = 3, classe = classe("Comum/R-HT"))
        assertEquals(ResultadoOperacao.SUCESSO, normal.resultado)
        assertTrue(normal.exigeResistencia)
        // Sucesso decisivo → automático, sem disputa.
        val decisivo = MagicCasting.resolver(nhEfetivo = 15, rolagem3d = 4, custoTotal = 3, classe = classe("Comum/R-HT"))
        assertEquals(ResultadoOperacao.SUCESSO_DECISIVO, decisivo.resultado)
        assertFalse(decisivo.exigeResistencia)
    }

    @Test fun `mágica nao-resistivel nunca exige disputa`() {
        val r = MagicCasting.resolver(nhEfetivo = 15, rolagem3d = 12, custoTotal = 3, classe = classe("Comum"))
        assertFalse(r.exigeResistencia)
    }

    // ── Disputa Rápida de resistência (p.14) ──

    @Test fun `operador vence quando sua margem e maior`() {
        // op: NH 15 rola 8 → margem 7. alvo: resistência 12 rola 10 → margem 2. Operador afeta.
        val r = MagicCasting.resolverResistencia(nhOperadorEfetivo = 15, rolagemOperador3d = 8,
            resistenciaAlvo = 12, rolagemAlvo3d = 10, regraDo16 = false)
        assertFalse(r.alvoResistiu)
    }

    @Test fun `empate favorece o defensor (alvo resiste)`() {
        // ambas as margens = 4 → alvo resiste (p.14: empate = mágica não surte efeito).
        val r = MagicCasting.resolverResistencia(nhOperadorEfetivo = 14, rolagemOperador3d = 10,
            resistenciaAlvo = 14, rolagemAlvo3d = 10, regraDo16 = false)
        assertTrue(r.alvoResistiu)
    }

    @Test fun `Regra do 16 limita o teste do operador contra alvo vivo`() {
        // NH efetivo 20, mas contra ser vivo o teste é como NH 16 (p.14 / MB349).
        val comRegra = MagicCasting.resolverResistencia(nhOperadorEfetivo = 20, rolagemOperador3d = 10,
            resistenciaAlvo = 10, rolagemAlvo3d = 8, regraDo16 = true)
        assertEquals(16 - 10, comRegra.margemOperador) // 6, não 10
    }

    @Test fun `Abascanto do alvo penaliza o teste do operador`() {
        assertEquals(-3, MagicCasting.penalidadeAbascantoOperador(3))
        assertEquals(0, MagicCasting.penalidadeAbascantoOperador(0))
    }

    // ── Escala de efeito (p.9/14) ──

    @Test fun `teto de niveis e o maior entre a magia e a Aptidao Magica`() {
        assertEquals(10, MagicCasting.tetoNiveisEfeito(niveisDeclarados = 4, aptidaoMagica = 10)) // p.9 exemplo Cura Profunda
        assertEquals(4, MagicCasting.tetoNiveisEfeito(niveisDeclarados = 4, aptidaoMagica = 2))
        assertNull(MagicCasting.tetoNiveisEfeito(niveisDeclarados = null, aptidaoMagica = 3)) // sem limite
    }

    @Test fun `energia vira dados de dano e segundos de cegueira 1 por 1`() {
        assertEquals(6, MagicCasting.dadosDeDanoPorEnergia(6))       // p.14: 1d por ponto
        assertEquals(3, MagicCasting.segundosDeCegueiraPorEnergia(3))
    }

    // ── Tempo de operação por NH (p.9) ──

    @Test fun `tempo reduz pela metade em NH 20 e por quatro em NH 25`() {
        assertEquals(8, MagicCasting.tempoOperacaoAjustado(tempoBaseSeg = 8, nhBasico = 14)) // sem redução
        assertEquals(4, MagicCasting.tempoOperacaoAjustado(tempoBaseSeg = 8, nhBasico = 20)) // metade
        assertEquals(2, MagicCasting.tempoOperacaoAjustado(tempoBaseSeg = 8, nhBasico = 25)) // 1/4
        assertEquals(1, MagicCasting.tempoOperacaoAjustado(tempoBaseSeg = 1, nhBasico = 30)) // piso 1s
    }

    @Test fun `tempo arredonda a fracao para cima e nunca abaixo de 1`() {
        // 3s em NH 20 → ceil(1,5) = 2.
        assertEquals(2, MagicCasting.tempoOperacaoAjustado(tempoBaseSeg = 3, nhBasico = 20))
    }

    // ── Lote MEC-5: "NN/MM" é operar/manter, não fração ──────────────────────────────────────────

    @Test fun `energia 04-02 e operar 4 manter 2 — nao a fracao 2`() {
        // Antes: o regex de fração casava primeiro e devolvia fracao=2.0 com base=null (307 mágicas).
        val c = MagicEnergy.parse("04/02")
        assertEquals(4, c.base)
        assertEquals(2, c.manutencao)
        assertNull("não é fração", c.fracao)
        assertFalse(c.variavel)
    }

    @Test fun `formatos reais do catalogo de operar-manter`() {
        assertEquals(8 to 6, MagicEnergy.parse("08/06").let { it.base to it.manutencao }) // Agonizar
        assertEquals(4 to 2, MagicEnergy.parse("4/2#").let { it.base to it.manutencao })  // marcador de nota
        assertEquals(50 to 20, MagicEnergy.parse("50/20").let { it.base to it.manutencao }) // ritual caro
        assertEquals(2 to 1, MagicEnergy.parse("02/01").let { it.base to it.manutencao })
    }

    @Test fun `custo por UNIDADE nao vira operar-manter — regra estrita`() {
        // "1 de energia por 4 litros" não é operar/manter; sem unidade no schema, fica variável.
        val litros = MagicEnergy.parse("1/4 litros")
        assertNull("não pode inventar manutenção 4", litros.manutencao)
        val kg = MagicEnergy.parse("250/0,5 kg")
        assertNull(kg.manutencao)
        val marcador = MagicEnergy.parse("1/10/I") // barra extra → não casa
        assertNull(marcador.manutencao)
    }

    // ── Lote MEC-9: teto de energia (o MEC-7 abriu a porta para dano inflado) ───────────────────

    @Test fun `faixa simples limita o dano — Toque Candente 1 a 3 nao aceita 10`() {
        // O bug: com o seletor do MEC-7 e sem teto, 10 de energia saía 10d numa magia de custo 1 a 3.
        // Lote MEC-25: com Aptidão MAIOR que a faixa, a Aptidão manda (Magia p.9).
        assertEquals(5, MagicEnergy.tetoDeEnergiaDano("1 a 3", aptidaoMagica = 5))
        assertEquals(9, MagicEnergy.tetoDeEnergiaDano("1 a 4", aptidaoMagica = 9)) // MEC-25
        // Aptidão MENOR que a faixa: quem manda é a faixa da magia.
        assertEquals(6, MagicEnergy.tetoDeEnergiaDano("2 a 6", aptidaoMagica = 2))

    }

    @Test
    fun `MEC-25 o exemplo literal do livro — Cura Profunda 1 a 4 com Aptidao 10 vai a 10 niveis`() {
        // Magia p.9: "Cura Profunda permite gastar 1, 2, 3 ou 4 pontos... Aptidão Mágica 10
        // permitiria aumentar esse limite para 10 níveis de efeito".
        assertEquals(10, MagicEnergy.tetoDeEnergiaDano("1 a 4", aptidaoMagica = 10))
        // E o teto NUNCA encolhe abaixo da faixa por causa de uma Aptidão baixa.
        assertEquals(4, MagicEnergy.tetoDeEnergiaDano("1 a 4", aptidaoMagica = 1))
    }

    @Test fun `teto por APTIDAO nao pode ser lido como faixa — 2 a 2xAM e 2 vezes a aptidao`() {
        // Armadilha real do catálogo: o regex de faixa leria "2 a 2" e daria teto 2 (restritivo demais).
        assertEquals(6, MagicEnergy.tetoDeEnergiaDano("2 a 2×AM#", aptidaoMagica = 3))
        assertEquals(8, MagicEnergy.tetoDeEnergiaDano("2 a 2x AM", aptidaoMagica = 4))
    }

    @Test fun `Projetil Varia usa a Aptidao Magica (Magia p12)`() {
        assertEquals(4, MagicEnergy.tetoDeEnergiaDano("Varia", aptidaoMagica = 4))
        assertEquals(1, MagicEnergy.tetoDeEnergiaDano("Varia", aptidaoMagica = 0)) // piso 1
    }

    @Test fun `custo FIXO e o proprio teto`() {
        assertEquals(5, MagicEnergy.tetoDeEnergiaDano("5", aptidaoMagica = 9))
    }

    @Test fun `Varia continua variavel e sem manutencao declarada`() {
        val c = MagicEnergy.parse("Varia")
        assertTrue(c.variavel)
        assertNull(c.base)
        assertNull(c.manutencao)
    }
}
