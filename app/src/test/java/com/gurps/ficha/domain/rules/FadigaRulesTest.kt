package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lote MB-6** — o botão PF.
 *
 * ## 🔴 O que este arquivo protege
 *
 * **PF perdido não é tudo igual.** Dez PF de fome não voltam com descanso, e dez
 * PF de sono perdido não voltam com comida. Se o painel embaralhar as origens, o
 * jogador senta para descansar e não entende por que o número não sobe.
 *
 * E a **reconciliação**: o painel não pode devolver de graça o PF que o
 * personagem gastou fora dele.
 */
class FadigaRulesTest {

    // ==================================================================
    // 1. 🔴 Cada perda volta de um jeito
    // ==================================================================

    @Test
    fun `🔴 fome NAO volta com descanso, e sono NAO volta com comida`() {
        assertEquals(FadigaRules.Recuperacao.COMIDA, FadigaRules.fonte("fome")!!.recuperacao)
        assertEquals(FadigaRules.Recuperacao.SONO, FadigaRules.fonte("sono")!!.recuperacao)
        assertEquals(FadigaRules.Recuperacao.AGUA, FadigaRules.fonte("desidratacao")!!.recuperacao)
        assertEquals(FadigaRules.Recuperacao.DESCANSO, FadigaRules.fonte("esforco")!!.recuperacao)
    }

    @Test
    fun `o rodape separa o total por forma de recuperacao`() {
        // 3 refeições perdidas + 2 PF de esforço: são duas linhas diferentes,
        // não "5 PF".
        val linhas = FadigaRules.resumoDaRecuperacao(mapOf("fome" to 3, "esforco" to 2))
        assertEquals(2, linhas.size)
        assertTrue(linhas.any { it.startsWith("3 PF") && it.contains("Comer") })
        assertTrue(linhas.any { it.startsWith("2 PF") && it.contains("Descanso") })
    }

    @Test
    fun `⚠️ so a sede severa tira PV junto`() {
        val total = FadigaRules.totalDe(mapOf("sede_severa" to 3, "fome" to 4))
        assertEquals(7, total.pf)
        assertEquals("só os 3 dias sem água tiram PV", 3, total.pv)
        assertEquals(0, FadigaRules.totalDe(mapOf("fome" to 9)).pv)
    }

    @Test
    fun `id desconhecido e ignorado, nao quebra a soma`() {
        // Ficha antiga com um id que não existe mais não pode derrubar o painel.
        assertEquals(2, FadigaRules.totalDe(mapOf("fome" to 2, "coisa_que_nao_existe" to 99)).pf)
    }

    // ==================================================================
    // 2. Sono perdido
    // ==================================================================

    @Test
    fun `o dia util e de 16 horas, e ate la nao custa nada`() {
        assertEquals(0, FadigaRules.pfPorSonoPerdido(16))
        assertEquals(0, FadigaRules.pfPorSonoPerdido(10))
    }

    @Test
    fun `passou das 16 horas, custa 1 PF — e mais 1 a cada 4 horas`() {
        assertEquals(1, FadigaRules.pfPorSonoPerdido(17))
        assertEquals(1, FadigaRules.pfPorSonoPerdido(20))
        assertEquals(2, FadigaRules.pfPorSonoPerdido(21))
        assertEquals(3, FadigaRules.pfPorSonoPerdido(25))
    }

    @Test
    fun `⚠️ quem acordou cedo comeca a cansar antes`() {
        // MB p.427: período de 8 h, dormiu 6 → perdeu 2 h → o dia útil encolhe
        // o DOBRO disso, para 12 h. Com 13 h acordado ele já paga.
        assertEquals(0, FadigaRules.pfPorSonoPerdido(12, horasDeSonoPerdidasAntes = 2))
        assertEquals(1, FadigaRules.pfPorSonoPerdido(13, horasDeSonoPerdidasAntes = 2))
        // Sem a noite mal dormida, 13 h ainda não custam nada.
        assertEquals(0, FadigaRules.pfPorSonoPerdido(13))
    }

    @Test
    fun `ficar mais tempo acordado nunca cansa menos`() {
        (1..60).zipWithNext().forEach { (a, b) ->
            assertTrue(
                "$b h cansou menos que $a h",
                FadigaRules.pfPorSonoPerdido(b) >= FadigaRules.pfPorSonoPerdido(a)
            )
        }
    }

    @Test
    fun `o alerta de sono so aparece quando o livro manda`() {
        // Metade ou mais do PF perdida por sono → teste a cada 2 h.
        assertNull(FadigaRules.alertaDeSono(pfPerdidoPorSono = 3, pfMax = 12))
        assertNotNull(FadigaRules.alertaDeSono(pfPerdidoPorSono = 6, pfMax = 12))
        // Abaixo de 1/3 restante → o alerta muda para 30 minutos.
        val grave = FadigaRules.alertaDeSono(pfPerdidoPorSono = 9, pfMax = 12)!!
        assertTrue(grave, grave.contains("30 minutos"))
    }

    @Test
    fun `⚠️ Dorminhoco piora a penalidade em 1`() {
        val comum = FadigaRules.alertaDeSono(6, 12, dorminhoco = false)!!
        val dorminhoco = FadigaRules.alertaDeSono(6, 12, dorminhoco = true)!!
        assertTrue(comum, comum.contains("-2"))
        assertTrue(dorminhoco, dorminhoco.contains("-3"))
    }

    // ==================================================================
    // 3. Os efeitos do PF baixo
    // ==================================================================

    @Test
    fun `menos de um terco do PF deixa o personagem muito cansado`() {
        assertTrue(FadigaRules.muitoCansado(3, 12))
        assertTrue(!FadigaRules.muitoCansado(4, 12))
    }

    @Test
    fun `⚠️ a metade do cansaco arredonda para CIMA`() {
        // Deslocamento 5 vira 3, não 2. Arredondar para baixo tira mais do que o
        // livro manda.
        assertEquals(3, FadigaRules.metadeCansada(5))
        assertEquals(3, FadigaRules.metadeCansada(6))
        assertEquals(1, FadigaRules.metadeCansada(1))
    }

    @Test
    fun `🔴 a zero PF o aviso fala em perder PV`() {
        val e = FadigaRules.estadoDe(0, 12)
        assertTrue(e.aBeiraDoColapso)
        assertTrue(e.avisos.any { it.contains("1 PV") && it.contains("Vontade") })
    }

    @Test
    fun `⚠️ o aviso do cansaco diz que PV e dano NAO mudam`() {
        // É a metade da regra que todo mundo esquece: a ST cai, mas o dano não.
        val e = FadigaRules.estadoDe(3, 12)
        assertTrue(e.avisos.any { it.contains("PV e dano NÃO mudam") })
    }

    @Test
    fun `com PF cheio nao ha aviso nenhum`() {
        assertTrue(FadigaRules.estadoDe(12, 12).avisos.isEmpty())
    }

    // ==================================================================
    // 4. 🔴 A reconciliação
    // ==================================================================

    @Test
    fun `🔴 o PF gasto fora do painel NAO volta de graca`() {
        // PF 12/12, o jogador gastou 4 numa magia (ficha em 8) e agora abre o
        // painel para marcar 1 refeição perdida. Os 4 da magia não podem sumir.
        val reconciliado = FadigaRules.reconciliar(pfMax = 12, pfAtual = 8, quantidades = mapOf("fome" to 1))
        assertEquals(3, reconciliado[FadigaRules.ID_OUTROS])
        assertEquals(8, FadigaRules.pfDepoisDoPainel(12, reconciliado))
    }

    @Test
    fun `abrir e fechar o painel sem mexer em nada nao muda o PF`() {
        // A propriedade que importa: reconciliar é idempotente para o número
        // final. Varre todos os PF possíveis de uma ficha comum.
        (0..12).forEach { atual ->
            val r = FadigaRules.reconciliar(12, atual, emptyMap())
            assertEquals("PF $atual mudou ao abrir o painel", atual, FadigaRules.pfDepoisDoPainel(12, r))
        }
    }

    @Test
    fun `quando o painel ja explica tudo, a linha de outros some`() {
        val r = FadigaRules.reconciliar(12, 9, mapOf("fome" to 3))
        assertNull(r[FadigaRules.ID_OUTROS])
        assertEquals(9, FadigaRules.pfDepoisDoPainel(12, r))
    }

    @Test
    fun `⚠️ painel que explica MAIS que o buraco nao inventa PF`() {
        // O jogador marcou 5 refeições mas a ficha só perdeu 2 PF: o painel
        // manda, e o PF cai para 7. O que não pode é aparecer "outros" negativo.
        val r = FadigaRules.reconciliar(12, 10, mapOf("fome" to 5))
        assertNull(r[FadigaRules.ID_OUTROS])
        assertEquals(7, FadigaRules.pfDepoisDoPainel(12, r))
    }

    @Test
    fun `todo id do catalogo e unico e tem rotulo`() {
        val ids = FadigaRules.FONTES.map { it.id }
        assertEquals(ids.distinct().size, ids.size)
        FadigaRules.FONTES.forEach {
            assertTrue("${it.id} sem rótulo", it.rotulo.isNotBlank())
            assertTrue("${it.id} sem unidade", it.unidade.isNotBlank())
            assertTrue("${it.id} sem explicação", it.explicacao.isNotBlank())
        }
    }
}
