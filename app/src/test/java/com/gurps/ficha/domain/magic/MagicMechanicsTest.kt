package com.gurps.ficha.domain.magic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lote AR-1: expansão do dano estruturado por energia + condição embutida. */
class MagicMechanicsTest {

    @Test fun `dano escala pela energia — Relampago 1d-1 por energia`() {
        assertEquals("3d-3", MagicMechanics.expandirDano("1d-1", energia = 3, energiaPorDado = 1))
        assertEquals("1d-1", MagicMechanics.expandirDano("1d-1", energia = 1, energiaPorDado = 1))
    }

    @Test fun `Concussao 1d por 2 pontos de energia`() {
        assertEquals("2d", MagicMechanics.expandirDano("1d", energia = 4, energiaPorDado = 2))
        assertEquals("1d", MagicMechanics.expandirDano("1d", energia = 1, energiaPorDado = 2)) // piso 1 dado
    }

    @Test fun `Toque Chocante 1d+1 por energia`() {
        assertEquals("2d+2", MagicMechanics.expandirDano("1d+1", energia = 2, energiaPorDado = 1))
    }

    @Test fun `dano FIXO nao escala com a energia — Geiser 3d custe o que custar`() {
        // Sem a trava, o Géiser (custo básico 5) sairia como 15d.
        assertEquals("3d", MagicMechanics.expandirDano("3d", energia = 5, energiaPorDado = 1, danoFixo = true))
        assertEquals("1d", MagicMechanics.expandirDano("1d", energia = 4, energiaPorDado = 1, danoFixo = true))
    }

    @Test fun `dano em PONTOS vira 0d+N — o rolador exige n-d e devolveria 0 para um 1 pelado`() {
        // Nuvem de Faíscas: 1 ponto por segundo POR ponto de energia (escala, mas não é dado).
        assertEquals("0d+3", MagicMechanics.expandirDano("1", energia = 3, energiaPorDado = 1))
        assertEquals("0d+1", MagicMechanics.expandirDano("1", energia = 1, energiaPorDado = 1))
        // Pontos + dano fixo: trava em 1 ponto.
        assertEquals("0d+1", MagicMechanics.expandirDano("1", energia = 5, energiaPorDado = 1, danoFixo = true))
    }

    @Test fun `expandirDano sem danoFixo mantem o comportamento antigo (regressao)`() {
        assertEquals("3d-3", MagicMechanics.expandirDano("1d-1", energia = 3, energiaPorDado = 1, danoFixo = false))
    }

    // ── Lote MEC-2: buffs com número ────────────────────────────────────────────────────────────

    @Test fun `Forca escala pela energia (2 por nivel), com teto de 5 e piso de 1`() {
        val forca = MagiaMecanica(efeito = "buff", buffAtributo = "ST", buffAtributoValor = 1,
            buffEnergiaPorNivel = 2, buffMaxNiveis = 5)
        assertEquals(3, MagicMechanics.calcularBuff(forca, energia = 6, alvoId = "heroi").st)
        assertEquals(5, MagicMechanics.calcularBuff(forca, energia = 40, alvoId = "heroi").st) // teto
        assertEquals(1, MagicMechanics.calcularBuff(forca, energia = 1, alvoId = "heroi").st)  // piso: pagou, leva 1
    }

    // ── Lote P3-1: IQ e Vontade abertos no buff ────────────────────────────────────────────────

    @Test fun `Fortalecer Vontade — mais 1 por energia, teto mais 5 (Magia p100)`() {
        val m = MagiaMecanica(efeito = "buff", buffAtributo = "Vontade", buffAtributoValor = 1,
            buffEnergiaPorNivel = 1, buffMaxNiveis = 5)
        assertEquals(3, MagicMechanics.calcularBuff(m, energia = 3, alvoId = "heroi").vontade)
        assertEquals(5, MagicMechanics.calcularBuff(m, energia = 99, alvoId = "heroi").vontade)
    }

    @Test fun `Enfraquecer Vontade — menos 1 a cada DOIS de energia, teto menos 5`() {
        val m = MagiaMecanica(efeito = "buff", buffAtributo = "Vontade", buffAtributoValor = -1,
            buffEnergiaPorNivel = 2, buffMaxNiveis = 5)
        assertEquals("2 de energia compram só -1", -1,
            MagicMechanics.calcularBuff(m, energia = 2, alvoId = "goblin").vontade)
        assertEquals(-3, MagicMechanics.calcularBuff(m, energia = 6, alvoId = "goblin").vontade)
        assertEquals(-5, MagicMechanics.calcularBuff(m, energia = 99, alvoId = "goblin").vontade)
    }

    @Test fun `Sabedoria — mais 1 de IQ a cada QUATRO de energia, teto mais 5`() {
        val m = MagiaMecanica(efeito = "buff", buffAtributo = "IQ", buffAtributoValor = 1,
            buffEnergiaPorNivel = 4, buffMaxNiveis = 5)
        assertEquals("4 de energia = +1", 1, MagicMechanics.calcularBuff(m, energia = 4, alvoId = "heroi").iq)
        assertEquals(2, MagicMechanics.calcularBuff(m, energia = 8, alvoId = "heroi").iq)
        assertEquals(5, MagicMechanics.calcularBuff(m, energia = 99, alvoId = "heroi").iq)
    }

    @Test fun `Tolice — menos 1 de IQ por energia, teto menos 5`() {
        val m = MagiaMecanica(efeito = "buff", buffAtributo = "IQ", buffAtributoValor = -1,
            buffEnergiaPorNivel = 1, buffMaxNiveis = 5)
        assertEquals(-3, MagicMechanics.calcularBuff(m, energia = 3, alvoId = "goblin").iq)
        assertEquals(-5, MagicMechanics.calcularBuff(m, energia = 99, alvoId = "goblin").iq)
    }

    @Test fun `um atributo nao vaza no outro`() {
        val vont = MagiaMecanica(efeito = "buff", buffAtributo = "Vontade", buffAtributoValor = 2)
        val b = MagicMechanics.calcularBuff(vont, energia = 1, alvoId = "heroi")
        assertEquals(2, b.vontade)
        assertEquals(0, b.iq); assertEquals(0, b.st); assertEquals(0, b.dx); assertEquals(0, b.ht)
    }

    @Test fun `buff so de IQ ou Vontade NAO e considerado so-narrado`() {
        // Regressão da armadilha do MEC-14: `registrarMagiaAtiva` DESCARTA o buff quando
        // `soNarrado` é true. Se um campo novo não entrar naquele teste, o buff é calculado
        // certinho e jogado fora em silêncio.
        val vont = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffAtributo = "Vontade", buffAtributoValor = 1), 1, "heroi")
        val iq = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffAtributo = "IQ", buffAtributoValor = -1), 1, "goblin")
        assertFalse("Vontade tem que ser executável", vont.soNarrado)
        assertFalse("IQ tem que ser executável", iq.soNarrado)
    }

    @Test fun `Bloquear e Robustez sao buff de UM USO com escala de 1 por ponto ate 5`() {
        val bloquear = MagiaMecanica(efeito = "buff", buffBd = 1, buffEnergiaPorNivel = 1,
            buffMaxNiveis = 5, buffUmUnicoUso = true)
        val robustez = MagiaMecanica(efeito = "buff", buffRd = 1, buffEnergiaPorNivel = 1,
            buffMaxNiveis = 5, buffUmUnicoUso = true)
        val b = MagicMechanics.calcularBuff(bloquear, energia = 3, alvoId = "heroi")
        val r = MagicMechanics.calcularBuff(robustez, energia = 5, alvoId = "heroi")
        assertEquals(3, b.bd); assertTrue(b.umUnicoUso)
        assertEquals(5, r.rd); assertTrue(r.umUnicoUso)
        assertEquals("teto de 5 (Magia p.101)", 5,
            MagicMechanics.calcularBuff(robustez, energia = 99, alvoId = "heroi").rd)
    }

    @Test fun `Debilitar aplica atributo NEGATIVO`() {
        val deb = MagiaMecanica(efeito = "buff", buffAtributo = "ST", buffAtributoValor = -1,
            buffEnergiaPorNivel = 1, buffMaxNiveis = 5)
        assertEquals(-3, MagicMechanics.calcularBuff(deb, energia = 3, alvoId = "goblin").st)
    }

    @Test fun `buff sem escala por energia vale 1 nivel — Pele de Crocodilo RD 4 nao vira RD 20`() {
        val pele = MagiaMecanica(efeito = "buff", buffRd = 4, buffEnergiaPorNivel = 0)
        assertEquals(4, MagicMechanics.calcularBuff(pele, energia = 5, alvoId = "heroi").rd)
    }

    @Test fun `Apressar sobe Deslocamento E Esquiva juntos, teto 3`() {
        val ap = MagiaMecanica(efeito = "buff", buffDeslocamento = 1, buffEsquiva = 1,
            buffEnergiaPorNivel = 2, buffMaxNiveis = 3)
        val b = MagicMechanics.calcularBuff(ap, energia = 20, alvoId = "heroi")
        assertEquals(3, b.deslocamento); assertEquals(3, b.esquiva)
    }

    @Test fun `Voo impoe Deslocamento absoluto, nao um delta`() {
        val voo = MagiaMecanica(efeito = "buff", buffDeslocamentoFixo = 10)
        assertEquals(10, MagicMechanics.calcularBuff(voo, energia = 5, alvoId = "heroi").deslocamentoFixo)
        assertEquals(0, MagicMechanics.calcularBuff(voo, energia = 5, alvoId = "heroi").deslocamento)
    }

    @Test fun `bonus de arma so vale no alcance certo — o +2 do gume nao vaza pro arco`() {
        val cac = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffDanoArma = 2, buffArmaTipo = "cac"), 1, "heroi")
        assertTrue(cac.danoArmaVale(aDistancia = false))
        assertFalse(cac.danoArmaVale(aDistancia = true))
        val dist = MagicMechanics.calcularBuff(
            MagiaMecanica(efeito = "buff", buffDanoArma = 2, buffArmaTipo = "distancia"), 1, "heroi")
        assertFalse(dist.danoArmaVale(aDistancia = false))
        assertTrue(dist.danoArmaVale(aDistancia = true))
    }

    @Test fun `buff sem numero nenhum e so narrado — regra de ouro`() {
        val corpoDeAgua = MagiaMecanica(efeito = "buff", buffRotulo = "Corpo de Água")
        assertTrue(MagicMechanics.calcularBuff(corpoDeAgua, 3, "heroi").soNarrado)
        assertFalse(MagicMechanics.temBuffEstruturado(corpoDeAgua))
        assertTrue(MagicMechanics.temBuffEstruturado(MagiaMecanica(efeito = "buff", buffRd = 4)))
    }

    // ── Lote MEC-7: o jogador precisa PODER escolher a energia ──────────────────────────────────
    // Bug achado no aparelho: Escudo e Aumentar Força gastavam a fadiga sozinhos e entregavam o
    // MÍNIMO — o seletor de energia só existia para Projétil.

    @Test fun `Escudo — cada 2 PF compram +1 de Defesa, teto 8 PF (BD +4)`() {
        val escudo = MagiaMecanica(efeito = "buff", buffBd = 1, buffEnergiaPorNivel = 2, buffMaxNiveis = 4)
        val e = MagicMechanics.escalaDeEnergia(escudo)!!
        assertEquals("acima de 8 PF o efeito trava: seria fadiga jogada fora", 8, e.energiaMax)
        assertTrue(e.dica.contains("cada 2 PF"))
        assertTrue(e.dica.contains("+4"))
    }

    @Test fun `Aumentar Forca — 1 PF por +1 de ST, teto 5`() {
        val af = MagiaMecanica(efeito = "buff", buffAtributo = "ST", buffAtributoValor = 1,
            buffEnergiaPorNivel = 1, buffMaxNiveis = 5, buffUmUnicoUso = true)
        val e = MagicMechanics.escalaDeEnergia(af)!!
        assertEquals(5, e.energiaMax)
        assertTrue(e.dica.contains("ST"))
    }

    @Test fun `magia de custo FIXO nao mostra seletor — nao ha o que escolher`() {
        // Pele de Crocodilo: RD 4, não escala.
        assertNull(MagicMechanics.escalaDeEnergia(MagiaMecanica(efeito = "buff", buffRd = 4)))
        // Voo: Deslocamento 10 absoluto, não escala.
        assertNull(MagicMechanics.escalaDeEnergia(MagiaMecanica(efeito = "buff", buffDeslocamentoFixo = 10)))
        // Não é buff.
        assertNull(MagicMechanics.escalaDeEnergia(MagiaMecanica(efeito = "dano", danoPorEnergia = "1d")))
        assertNull(MagicMechanics.escalaDeEnergia(null))
    }

    @Test fun `debuff mostra o sinal NEGATIVO na dica (Nublar e Debilitar)`() {
        val nublar = MagicMechanics.escalaDeEnergia(
            MagiaMecanica(efeito = "buff", buffPenalidadeAtacantes = 1, buffEnergiaPorNivel = 1, buffMaxNiveis = 5))!!
        assertEquals(5, nublar.energiaMax)
        assertTrue("penalidade tem que aparecer negativa", nublar.dica.contains("-1") || nublar.dica.contains("−1"))
        val debilitar = MagicMechanics.escalaDeEnergia(
            MagiaMecanica(efeito = "buff", buffAtributo = "ST", buffAtributoValor = -1,
                buffEnergiaPorNivel = 1, buffMaxNiveis = 5))!!
        assertTrue(debilitar.dica.contains("-5"))
    }

    @Test fun `o teto do seletor bate com o teto que o motor aplica`() {
        // Coerência: gastar o teto de energia tem que dar exatamente o efeito máximo — se o seletor
        // deixasse passar disso, o jogador queimaria PF sem ganhar nada.
        val escudo = MagiaMecanica(efeito = "buff", buffBd = 1, buffEnergiaPorNivel = 2, buffMaxNiveis = 4)
        val teto = MagicMechanics.escalaDeEnergia(escudo)!!.energiaMax
        assertEquals(4, MagicMechanics.calcularBuff(escudo, teto, "heroi").bd)
        assertEquals("acima do teto o efeito não cresce", 4, MagicMechanics.calcularBuff(escudo, teto + 10, "heroi").bd)
    }

    // ── Lote MEC-10: CURA (o `efeito` não tinha valor para curar — tudo caía em "narrado") ───────

    private val superficial = MagiaMecanica(efeito = "cura", curaPvPorEnergia = 1, curaMaxPv = 3)
    private val profunda = MagiaMecanica(efeito = "cura", curaPvPorEnergia = 2, curaMaxPv = 8)
    private val superior = MagiaMecanica(efeito = "cura", curaTotal = true)

    @Test fun `Cura Superficial restaura 1 PV por energia, teto 3`() {
        assertEquals(2, MagicMechanics.pvCurados(superficial, energia = 2, pvPerdidos = 10))
        assertEquals("teto da magia", 3, MagicMechanics.pvCurados(superficial, energia = 9, pvPerdidos = 10))
    }

    @Test fun `Cura Profunda restaura o DOBRO por energia, teto 8`() {
        assertEquals(6, MagicMechanics.pvCurados(profunda, energia = 3, pvPerdidos = 10))
        assertEquals("teto da magia", 8, MagicMechanics.pvCurados(profunda, energia = 9, pvPerdidos = 10))
    }

    @Test fun `cura NUNCA estoura o que o alvo realmente perdeu`() {
        // Curar 8 em quem perdeu 2 restaura 2 — não inventa PV acima do máximo.
        assertEquals(2, MagicMechanics.pvCurados(profunda, energia = 4, pvPerdidos = 2))
        assertEquals(0, MagicMechanics.pvCurados(profunda, energia = 4, pvPerdidos = 0))
    }

    @Test fun `Cura Superior restaura TODOS os PV perdidos`() {
        assertEquals(17, MagicMechanics.pvCurados(superior, energia = 1, pvPerdidos = 17))
        assertEquals(0, MagicMechanics.pvCurados(superior, energia = 1, pvPerdidos = 0))
    }

    @Test fun `teto de energia da cura vem do teto de PV — Profunda 8 por 2 = 4`() {
        assertEquals(3, MagicMechanics.tetoEnergiaCura(superficial)) // 3 PV ÷ 1 por energia
        assertEquals(4, MagicMechanics.tetoEnergiaCura(profunda))    // 8 PV ÷ 2 por energia
        assertEquals("custo fixo: não há energia a escolher", 1, MagicMechanics.tetoEnergiaCura(superior))
    }

    @Test fun `so conta como cura quem tem numero — magia de cura narrada nao entra no motor`() {
        assertTrue(MagicMechanics.temCuraEstruturada(superficial))
        assertTrue(MagicMechanics.temCuraEstruturada(superior))
        assertFalse(MagicMechanics.temCuraEstruturada(MagiaMecanica(efeito = "cura"))) // sem número
        assertFalse(MagicMechanics.temCuraEstruturada(MagiaMecanica(efeito = "narrado")))
        assertFalse(MagicMechanics.temCuraEstruturada(null))
    }

    @Test fun `penalidade da condicao por PV (Relampago -1 a cada 2 PV)`() {
        assertEquals(-3, MagicMechanics.penalidadeCondicaoPorPv("HT_por_pv", pvSofridos = 6))
        assertEquals(-3, MagicMechanics.penalidadeCondicaoPorPv("HT-3", pvSofridos = 1))
        assertEquals(0, MagicMechanics.penalidadeCondicaoPorPv(null, 10))
    }

    @Test fun `temDanoEstruturado so quando efeito dano com formula`() {
        assertTrue(MagicMechanics.temDanoEstruturado(MagiaMecanica(efeito = "dano", danoPorEnergia = "1d")))
        assertFalse(MagicMechanics.temDanoEstruturado(MagiaMecanica(efeito = "dano"))) // sem fórmula
        assertFalse(MagicMechanics.temDanoEstruturado(MagiaMecanica(efeito = "buff", buffRotulo = "x")))
        assertFalse(MagicMechanics.temDanoEstruturado(null))
    }
}
