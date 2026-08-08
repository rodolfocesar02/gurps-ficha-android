package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lote MB-7** — ferimento por local do corpo.
 *
 * ## 🔴 O que este arquivo protege
 *
 * O **mínimo que incapacita** é `floor(PV × fração) + 1`, e não "PV/2
 * arredondado para cima". A diferença é de **1 ponto** e só aparece com **PV
 * par** — ou seja, em metade dos personagens, silenciosamente.
 *
 * Os dois exemplos trabalhados do livro estão aqui como teste, porque foram eles
 * que decidiram a fórmula.
 */
class FerimentoPorLocalRulesTest {

    // ==================================================================
    // 1. 🔴 Os exemplos trabalhados do livro
    // ==================================================================

    @Test
    fun `🔴 Friedrick, PV 14 — o livro diz 8, nao 7`() {
        // MB p.419: "No caso de Friedrick, PV/2 é 7. Dano maior que PV/2 é 8 PV,
        // então ele perde apenas 8 PV." Arredondar 7,0 "para cima" daria 7.
        assertEquals(8, FerimentoPorLocalRules.minimoQueIncapacita(LocalAtaque.BRACO, 14))

        val r = FerimentoPorLocalRules.aplicar(
            pvInicial = 14, danoBruto = 11, tipo = DanoTipo.CONT, local = LocalAtaque.BRACO
        )
        assertEquals(8, r.pvPerdidos)
        assertEquals(3, r.desperdicado)
        assertEquals(FerimentoPorLocalRules.EfeitoNoLocal.INCAPACITADO, r.efeito)
    }

    @Test
    fun `🔴 o homem de PV 10 que leva 9 no braco perde 6`() {
        // MB p.421, palavra por palavra.
        assertEquals(6, FerimentoPorLocalRules.minimoQueIncapacita(LocalAtaque.BRACO, 10))
        val r = FerimentoPorLocalRules.aplicar(10, 9, DanoTipo.CONT, LocalAtaque.BRACO)
        assertEquals(6, r.pvPerdidos)
    }

    @Test
    fun `o exemplo de PV 11 da regra opcional tambem bate`() {
        // MB p.421: "no caso de um personagem com 11 PV, um total de 6 PV
        // incapacitariam seu braço".
        assertEquals(6, FerimentoPorLocalRules.minimoQueIncapacita(LocalAtaque.BRACO, 11))
    }

    @Test
    fun `⚠️ com PV impar as duas contas coincidem — por isso o erro passa batido`() {
        // PV 11: floor(5,5)+1 = 6 e ceil(5,5) = 6. Iguais.
        // PV 14: floor(7,0)+1 = 8 e ceil(7,0) = 7. UM ponto de diferença.
        assertEquals(6, FerimentoPorLocalRules.minimoQueIncapacita(LocalAtaque.BRACO, 11))
        assertEquals(8, FerimentoPorLocalRules.minimoQueIncapacita(LocalAtaque.BRACO, 14))
    }

    @Test
    fun `extremidade e um terco, olho e um decimo`() {
        assertEquals(4, FerimentoPorLocalRules.minimoQueIncapacita(LocalAtaque.MAO, 10))
        assertEquals(4, FerimentoPorLocalRules.minimoQueIncapacita(LocalAtaque.PE, 9))
        assertEquals(2, FerimentoPorLocalRules.minimoQueIncapacita(LocalAtaque.OLHO, 10))
    }

    @Test
    fun `tronco, cranio e vitais nao incapacitam`() {
        listOf(LocalAtaque.TORSO, LocalAtaque.CRANIO, LocalAtaque.VITAIS,
            LocalAtaque.PESCOCO, LocalAtaque.ROSTO, LocalAtaque.INGLE).forEach {
            assertNull("$it não devia ter limiar", FerimentoPorLocalRules.minimoQueIncapacita(it, 10))
        }
    }

    // ==================================================================
    // 2. ⚠️ O teto do membro e o decepamento
    // ==================================================================

    @Test
    fun `⚠️ o decepamento olha a lesao ANTES do teto`() {
        // PV 10, braço: incapacita com 6, deceps com 12. Um golpe de 20 de
        // contusão dá lesão 20 → passa dos 12 → destruído. Mas o PV perdido
        // continua sendo 6.
        val r = FerimentoPorLocalRules.aplicar(10, 20, DanoTipo.CONT, LocalAtaque.BRACO)
        assertEquals(FerimentoPorLocalRules.EfeitoNoLocal.DECEPADO, r.efeito)
        assertEquals(6, r.pvPerdidos)
        assertEquals(20, r.lesaoAntesDoTeto)
    }

    @Test
    fun `🔴 quem aplicasse o teto primeiro NUNCA deceparia nada`() {
        // O teto é 6 e o gatilho do decepamento é 12: se o teto entrasse antes,
        // a lesão nunca chegaria ao gatilho. Este teste existe para travar essa
        // inversão de ordem, que é o erro fácil deste arquivo.
        val r = FerimentoPorLocalRules.aplicar(10, 12, DanoTipo.CONT, LocalAtaque.BRACO)
        assertTrue(r.lesaoAntesDoTeto > r.pvPerdidos)
        assertEquals(FerimentoPorLocalRules.EfeitoNoLocal.DECEPADO, r.efeito)
    }

    @Test
    fun `⚠️ o teto NAO vale para o olho`() {
        // "esse limite não se aplica aos olhos!" (MB p.421). Uma flecha no olho
        // mata: 8 de perfuração ×4 = 32 PV, e nada é desperdiçado.
        val r = FerimentoPorLocalRules.aplicar(10, 8, DanoTipo.PERF, LocalAtaque.OLHO)
        assertEquals(FerimentoPorLocalRules.EfeitoNoLocal.CEGOU, r.efeito)
        assertEquals(32, r.pvPerdidos)
        assertEquals(0, r.desperdicado)
    }

    // ==================================================================
    // 3. RD e multiplicador
    // ==================================================================

    @Test
    fun `🔴 o cranio tem RD 2 de graca — e o olho NAO herda`() {
        assertEquals(2, FerimentoPorLocalRules.rdExtraNatural(LocalAtaque.CRANIO))
        assertEquals(0, FerimentoPorLocalRules.rdExtraNatural(LocalAtaque.OLHO))
        // Dano 2 na testa não passa; os mesmos 2 no olho passam.
        assertEquals(0, FerimentoPorLocalRules.aplicar(10, 2, DanoTipo.PI, LocalAtaque.CRANIO).pvPerdidos)
        assertTrue(FerimentoPorLocalRules.aplicar(10, 2, DanoTipo.PI, LocalAtaque.OLHO).pvPerdidos > 0)
    }

    @Test
    fun `cranio e olho multiplicam por quatro, qualquer que seja o tipo`() {
        assertEquals(4.0, FerimentoPorLocalRules.multiplicador(DanoTipo.CONT, LocalAtaque.CRANIO), 0.001)
        assertEquals(4.0, FerimentoPorLocalRules.multiplicador(DanoTipo.CORT, LocalAtaque.CRANIO), 0.001)
        assertEquals(4.0, FerimentoPorLocalRules.multiplicador(DanoTipo.PI, LocalAtaque.OLHO), 0.001)
    }

    @Test
    fun `pescoco dobra o corte e da uma vez e meia na contusao`() {
        assertEquals(2.0, FerimentoPorLocalRules.multiplicador(DanoTipo.CORT, LocalAtaque.PESCOCO), 0.001)
        assertEquals(1.5, FerimentoPorLocalRules.multiplicador(DanoTipo.CONT, LocalAtaque.PESCOCO), 0.001)
    }

    @Test
    fun `vitais triplicam so o perfurante`() {
        assertEquals(3.0, FerimentoPorLocalRules.multiplicador(DanoTipo.PERF, LocalAtaque.VITAIS), 0.001)
        assertEquals(3.0, FerimentoPorLocalRules.multiplicador(DanoTipo.PI, LocalAtaque.VITAIS), 0.001)
        // Contusão e corte não ganham nada nos vitais.
        assertEquals(1.0, FerimentoPorLocalRules.multiplicador(DanoTipo.CONT, LocalAtaque.VITAIS), 0.001)
        assertEquals(1.5, FerimentoPorLocalRules.multiplicador(DanoTipo.CORT, LocalAtaque.VITAIS), 0.001)
    }

    @Test
    fun `⚠️ atravessar um braco NAO mata — perfuracao cai para uma vez`() {
        // "reduza para ×1 o modificador de ferimento de um ataque por perfuração,
        // muito perfurante ou extremamente perfurante" (MB p.400).
        assertEquals(1.0, FerimentoPorLocalRules.multiplicador(DanoTipo.PERF, LocalAtaque.BRACO), 0.001)
        assertEquals(1.0, FerimentoPorLocalRules.multiplicador(DanoTipo.PI_MAIS_MAIS, LocalAtaque.PERNA), 0.001)
        // No tronco a mesma perfuração continua ×2.
        assertEquals(2.0, FerimentoPorLocalRules.multiplicador(DanoTipo.PERF, LocalAtaque.TORSO), 0.001)
    }

    @Test
    fun `⚠️ a RD entra ANTES do multiplicador`() {
        // 10 de corte, RD 4, no torso: (10−4) × 1,5 = 9.
        // Se multiplicasse antes: (10 × 1,5) − 4 = 11. Dois pontos de diferença.
        val r = FerimentoPorLocalRules.aplicar(12, 10, DanoTipo.CORT, LocalAtaque.TORSO, rdArmadura = 4)
        assertEquals(9, r.pvPerdidos)
    }

    @Test
    fun `dano que nao passa da RD nao machuca`() {
        val r = FerimentoPorLocalRules.aplicar(10, 3, DanoTipo.CORT, LocalAtaque.TORSO, rdArmadura = 5)
        assertEquals(0, r.pvPerdidos)
        assertEquals(0, r.choque)
        assertNull(r.testeDeNocaute)
    }

    @Test
    fun `⚠️ um ponto que passa a RD sempre causa pelo menos 1 PV`() {
        // 6 pouco perfurante (×0,5) contra RD 5: 1 penetrante × 0,5 = 0,5, e o
        // livro manda arredondar para baixo — mas com mínimo de 1.
        val r = FerimentoPorLocalRules.aplicar(10, 6, DanoTipo.PI_MENOS, LocalAtaque.TORSO, rdArmadura = 5)
        assertEquals(1, r.pvPerdidos)
    }

    // ==================================================================
    // 4. Choque e nocaute
    // ==================================================================

    @Test
    fun `o choque para em menos 4`() {
        assertEquals(-2, FerimentoPorLocalRules.choque(2, 10))
        assertEquals(-4, FerimentoPorLocalRules.choque(4, 10))
        assertEquals(-4, FerimentoPorLocalRules.choque(12, 10))
    }

    @Test
    fun `⚠️ quem tem 20 PV ou mais sente menos choque`() {
        // PV 20: -1 a cada PV/10 = a cada 2 pontos. 4 PV perdidos = -2, não -4.
        assertEquals(-2, FerimentoPorLocalRules.choque(4, 20))
        assertEquals(-4, FerimentoPorLocalRules.choque(4, 10))
    }

    @Test
    fun `🔴 a virilha e a unica excecao ao teto de menos 4`() {
        val virilha = FerimentoPorLocalRules.choque(
            5, 10, LocalAtaque.INGLE, DanoTipo.CONT, masculino = true
        )
        assertEquals(-8, virilha)
        // Só por contusão, e só em macho humanoide.
        assertEquals(-4, FerimentoPorLocalRules.choque(5, 10, LocalAtaque.INGLE, DanoTipo.CORT))
        assertEquals(-4, FerimentoPorLocalRules.choque(5, 10, LocalAtaque.INGLE, DanoTipo.CONT, masculino = false))
    }

    @Test
    fun `ferimento grave exige teste de nocaute`() {
        // PV 10, 6 de contusão no torso: mais que metade → grave.
        val r = FerimentoPorLocalRules.aplicar(10, 6, DanoTipo.CONT, LocalAtaque.TORSO)
        assertTrue(r.ferimentoGrave)
        assertNotNull(r.testeDeNocaute)
        assertEquals(0, r.testeDeNocaute!!.modificador)
    }

    @Test
    fun `🔴 no cranio o teste de nocaute e com menos 10`() {
        val r = FerimentoPorLocalRules.aplicar(10, 5, DanoTipo.CONT, LocalAtaque.CRANIO)
        assertEquals(-10, r.testeDeNocaute!!.modificador)
        // HT 12 vira 2. É a razão de mirar na cabeça.
        assertEquals(2, r.testeDeNocaute!!.alvoCom(12))
    }

    @Test
    fun `rosto, vitais e virilha pedem o teste com menos 5`() {
        listOf(LocalAtaque.ROSTO, LocalAtaque.VITAIS, LocalAtaque.INGLE).forEach { local ->
            val r = FerimentoPorLocalRules.aplicar(10, 9, DanoTipo.CONT, local)
            assertEquals("$local", -5, r.testeDeNocaute!!.modificador)
        }
    }

    @Test
    fun `⚠️ na cabeca basta haver choque — nao precisa ser ferimento grave`() {
        // 2 de contusão no rosto de quem tem PV 20: longe de grave, mas houve
        // choque, e o livro pede o teste mesmo assim (MB p.420).
        val r = FerimentoPorLocalRules.aplicar(20, 2, DanoTipo.CONT, LocalAtaque.ROSTO)
        assertTrue(!r.ferimentoGrave)
        assertNotNull("golpe na cabeça com choque pede teste", r.testeDeNocaute)
        // No braço, o mesmo golpe não pede nada.
        assertNull(FerimentoPorLocalRules.aplicar(20, 2, DanoTipo.CONT, LocalAtaque.BRACO).testeDeNocaute)
    }

    @Test
    fun `lesao incapacitante tambem e ferimento grave`() {
        // 4 de contusão na mão de quem tem PV 10: incapacita (mínimo 4), mas é
        // MENOS que metade dos PV. Ainda assim conta como grave (MB p.420).
        val r = FerimentoPorLocalRules.aplicar(10, 4, DanoTipo.CONT, LocalAtaque.MAO)
        assertEquals(FerimentoPorLocalRules.EfeitoNoLocal.INCAPACITADO, r.efeito)
        assertTrue("incapacitou, então é grave", r.ferimentoGrave)
    }

    // ==================================================================
    // 5. Invariantes
    // ==================================================================

    @Test
    fun `⚠️ nenhum golpe num membro passa do minimo que o incapacita`() {
        // Varredura: todo PV de 1 a 40, todo tipo, todo membro, dano de 1 a 60.
        listOf(LocalAtaque.BRACO, LocalAtaque.PERNA, LocalAtaque.MAO, LocalAtaque.PE).forEach { local ->
            (1..40).forEach { pv ->
                val teto = FerimentoPorLocalRules.minimoQueIncapacita(local, pv)!!
                DanoTipo.entries.forEach { tipo ->
                    (1..60).forEach { dano ->
                        val r = FerimentoPorLocalRules.aplicar(pv, dano, tipo, local)
                        assertTrue(
                            "$local PV $pv $tipo dano $dano → ${r.pvPerdidos} PV (teto $teto)",
                            r.pvPerdidos <= teto
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `bater mais forte nunca machuca menos`() {
        LocalAtaque.entries.forEach { local ->
            DanoTipo.entries.forEach { tipo ->
                (1..50).zipWithNext().forEach { (a, b) ->
                    val ra = FerimentoPorLocalRules.aplicar(12, a, tipo, local).pvPerdidos
                    val rb = FerimentoPorLocalRules.aplicar(12, b, tipo, local).pvPerdidos
                    assertTrue("$local $tipo: $b doeu menos que $a", rb >= ra)
                }
            }
        }
    }

    @Test
    fun `mais RD nunca deixa passar mais dano`() {
        (0..20).zipWithNext().forEach { (a, b) ->
            val ra = FerimentoPorLocalRules.aplicar(12, 15, DanoTipo.CORT, LocalAtaque.TORSO, rdArmadura = a)
            val rb = FerimentoPorLocalRules.aplicar(12, 15, DanoTipo.CORT, LocalAtaque.TORSO, rdArmadura = b)
            assertTrue("RD $b deixou passar mais que RD $a", rb.pvPerdidos <= ra.pvPerdidos)
        }
    }

    // ==================================================================
    // 6. A situação depois do golpe
    // ==================================================================

    @Test
    fun `os marcos de PV baixo aparecem na ordem certa`() {
        assertTrue(FerimentoPorLocalRules.situacao(3, 10).any { it.contains("1/3") })
        assertTrue(FerimentoPorLocalRules.situacao(0, 10).any { it.contains("0 PV") })
        assertTrue(FerimentoPorLocalRules.situacao(-10, 10).any { it.contains("não morrer") })
        assertTrue(FerimentoPorLocalRules.situacao(-50, 10).any { it.contains("morte imediata") })
        assertTrue(FerimentoPorLocalRules.situacao(10, 10).isEmpty())
    }
}
