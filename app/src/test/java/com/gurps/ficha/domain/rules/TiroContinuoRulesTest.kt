package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.loaders.ArmasCatalogLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Lotes MB-2 e MB-8** — fogo contínuo e mau funcionamento.
 *
 * As duas regras agem no mesmo instante (depois do tiro sair) e sobre os mesmos
 * dois números: o **dado cru** e a **margem de sucesso**.
 *
 * ## 🔴 O que este arquivo prova sobre o MB-8
 *
 * Que o bloqueio que eu declarei no plano **não existia**. Eu escrevi que faltava
 * o campo `Mauf` nas 62 armas; o livro diz que ele sai do **NT**, e o NT já estava
 * no modelo desde o Lote ARMA-1. O último teste varre o catálogo real e mostra que
 * **todas as 62 armas de fogo já têm Mauf hoje**.
 */
class TiroContinuoRulesTest {

    private fun asset(nome: String): String {
        val direto = File("src/main/assets/$nome")
        val f = if (direto.exists()) direto else File("app/src/main/assets/$nome")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    // ==================================================================
    // MB-2 · Fogo contínuo
    // ==================================================================

    @Test
    fun `errar nao acerta tiro nenhum`() {
        assertEquals(0, TiroContinuoRules.acertos(margem = -1, recuo = 2, tirosDisparados = 10))
        assertEquals(0, TiroContinuoRules.acertos(margem = -5, recuo = 1, tirosDisparados = 10))
    }

    @Test
    fun `acertar na mosca ja vale UM tiro`() {
        // Margem 0 é acerto exato: o disparo do próprio sucesso conta.
        assertEquals(1, TiroContinuoRules.acertos(margem = 0, recuo = 2, tirosDisparados = 10))
    }

    @Test
    fun `🔴 cada multiplo inteiro do Recuo vira mais um tiro`() {
        // MB p.549. Recuo 2, margem 7: 7÷2 = 3 extras, + 1 do acerto = 4.
        assertEquals(4, TiroContinuoRules.acertos(margem = 7, recuo = 2, tirosDisparados = 16))
        // Recuo 4, margem 7: 7÷4 = 1 extra, + 1 = 2.
        assertEquals(2, TiroContinuoRules.acertos(margem = 7, recuo = 4, tirosDisparados = 16))
    }

    @Test
    fun `⚠️ Recuo 1 faz CADA ponto de margem virar um acerto`() {
        // O livro: "Rcl 1 significa que a arma não tem ou possui um recuo muito
        // fraco". É a arma mais perigosa numa rajada, não a mais fraca.
        assertEquals(8, TiroContinuoRules.acertos(margem = 7, recuo = 1, tirosDisparados = 16))
    }

    @Test
    fun `🔴 nao da para acertar mais tiros do que se disparou`() {
        // Margem 12, Recuo 1 daria 13 acertos — mas a arma cuspiu 3.
        assertEquals(3, TiroContinuoRules.acertos(margem = 12, recuo = 1, tirosDisparados = 3))
        // E com CdT 1 o teto é sempre 1, por mais alta que seja a margem.
        assertEquals(1, TiroContinuoRules.acertos(margem = 20, recuo = 1, tirosDisparados = 1))
    }

    @Test
    fun `Recuo desconhecido vira 1, que e o pior caso para o app assumir`() {
        // Sem Recuo cadastrado, assumir 1 dá ao jogador o benefício — e o rótulo
        // conta a conta, então ele vê no que o app se baseou.
        assertEquals(8, TiroContinuoRules.acertos(margem = 7, recuo = null, tirosDisparados = 16))
    }

    @Test
    fun `a conta aparece escrita no resultado`() {
        val t = TiroContinuoRules.explicacao(margem = 7, recuo = 2, tirosDisparados = 16)
        assertTrue(t, t.contains("4 tiros acertaram"))
        assertTrue(t, t.contains("÷ Recuo 2"))
        val errou = TiroContinuoRules.explicacao(margem = -1, recuo = 2, tirosDisparados = 16)
        assertTrue(errou, errou.contains("nenhum tiro"))
    }

    @Test
    fun `so e rajada quando se dispara mais de um tiro`() {
        assertTrue(!TiroContinuoRules.ehRajada(1))
        assertTrue(!TiroContinuoRules.ehRajada(null))
        assertTrue(TiroContinuoRules.ehRajada(3))
    }

    @Test
    fun `mais margem NUNCA acerta menos tiros`() {
        listOf(null, 1, 2, 4).forEach { rcl ->
            (0..20).zipWithNext().forEach { (menos, mais) ->
                assertTrue(
                    "recuo=$rcl margem $mais deu menos que $menos",
                    TiroContinuoRules.acertos(mais, rcl, 20) >=
                        TiroContinuoRules.acertos(menos, rcl, 20)
                )
            }
        }
    }

    // ==================================================================
    // MB-8 · Mau funcionamento
    // ==================================================================

    @Test
    fun `🔴 o Mauf sai do NT, exatamente como o livro diz`() {
        // MB p.407: 12 em NT3, 14 em NT4, 16 em NT5, 17 em NT6+.
        assertEquals(12, MauFuncionamentoRules.maufPorNt(3))
        assertEquals(14, MauFuncionamentoRules.maufPorNt(4))
        assertEquals(16, MauFuncionamentoRules.maufPorNt(5))
        assertEquals(17, MauFuncionamentoRules.maufPorNt(6))
        assertEquals(17, MauFuncionamentoRules.maufPorNt(11))
        // NT abaixo de 3 (pólvora primitiva) fica no mais frágil.
        assertEquals(12, MauFuncionamentoRules.maufPorNt(2))
    }

    @Test
    fun `⚠️ sem NT nao ha formula, e o app NAO chuta`() {
        // Chutar 17 faria a arma parecer mais confiável do que se sabe.
        assertNull(MauFuncionamentoRules.maufPorNt(null))
        assertTrue(!MauFuncionamentoRules.enguicou(18, null))
    }

    @Test
    fun `🔴 enguica no dado CRU, nao no NH efetivo`() {
        // "Qualquer jogada de ataque" = os 3d6 como saíram. Um 17 enguiça mesmo
        // num atirador NH 20 — é esse o sentido da regra.
        assertTrue(MauFuncionamentoRules.enguicou(17, 17))
        assertTrue(MauFuncionamentoRules.enguicou(18, 17))
        assertTrue(!MauFuncionamentoRules.enguicou(16, 17))
        // Numa arma de NT3 o 12 já basta — e 12 sai o tempo todo.
        assertTrue(MauFuncionamentoRules.enguicou(12, 12))
    }

    @Test
    fun `⚠️ a tabela repete o disparo falho nas DUAS pontas`() {
        // Não é engano de transcrição: 5-8 e 12-14 dão o mesmo resultado no livro.
        assertEquals(MauFuncionamentoRules.Falha.MECANICO, MauFuncionamentoRules.tabela(3))
        assertEquals(MauFuncionamentoRules.Falha.MECANICO, MauFuncionamentoRules.tabela(4))
        assertEquals(MauFuncionamentoRules.Falha.DISPARO_FALHO, MauFuncionamentoRules.tabela(5))
        assertEquals(MauFuncionamentoRules.Falha.DISPARO_FALHO, MauFuncionamentoRules.tabela(8))
        assertEquals(MauFuncionamentoRules.Falha.EMPERRAMENTO, MauFuncionamentoRules.tabela(9))
        assertEquals(MauFuncionamentoRules.Falha.EMPERRAMENTO, MauFuncionamentoRules.tabela(11))
        assertEquals(MauFuncionamentoRules.Falha.DISPARO_FALHO, MauFuncionamentoRules.tabela(12))
        assertEquals(MauFuncionamentoRules.Falha.DISPARO_FALHO, MauFuncionamentoRules.tabela(14))
        assertEquals(MauFuncionamentoRules.Falha.EXPLOSAO, MauFuncionamentoRules.tabela(15))
        assertEquals(MauFuncionamentoRules.Falha.EXPLOSAO, MauFuncionamentoRules.tabela(18))
    }

    @Test
    fun `a tabela cobre os 16 resultados possiveis`() {
        (3..18).forEach { MauFuncionamentoRules.tabela(it) }
    }

    @Test
    fun `🔴 as 62 armas de fogo do catalogo JA tem Mauf hoje`() {
        // Este é o teste que desfaz o bloqueio que eu declarei no plano do MB.
        val fogo = ArmasCatalogLoader.distancia(
            asset(ArmasCatalogLoader.ARQUIVO_FOGO), ArmasCatalogLoader.TIPO_FOGO
        )
        assertEquals(62, fogo.size)
        val semMauf = fogo.filter { MauFuncionamentoRules.maufPorNt(it.nt) == null }
        assertTrue("armas sem Mauf: ${semMauf.map { it.nome }}", semMauf.isEmpty())

        // E o Mauf acompanha o NT, arma por arma.
        val porMauf = fogo.groupingBy { MauFuncionamentoRules.maufPorNt(it.nt)!! }.eachCount()
        assertEquals("NT baixo (2-3)", 2, porMauf[12])
        assertEquals("NT 4", 5, porMauf[14])
        assertEquals("NT 5", 6, porMauf[16])
        assertEquals("NT 6+", 49, porMauf[17])
    }

    @Test
    fun `a mensagem diz o dado, o Mauf e o que aconteceu`() {
        val t = MauFuncionamentoRules.explicacao(soma3d6DoAtaque = 17, mauf = 17, soma3d6DaTabela = 10)
        assertTrue(t, t.contains("17"))
        assertTrue(t, t.contains("Emperramento"))
        assertTrue(t, t.contains("enguiçou"))
    }
}
