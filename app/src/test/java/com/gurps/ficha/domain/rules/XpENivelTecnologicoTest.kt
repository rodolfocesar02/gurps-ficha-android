package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.Personagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **XP e NT da campanha** — Lote GER-1.
 *
 * ## O que já existia, e o que faltava
 *
 * 🔴 O `xpGanhos` **já somava** em `pontosTotaisDisponiveis` desde sempre, já
 * tinha linha no histórico e já era escrito pelo Narrador da Saga. O que não
 * existia era **campo na tela** — o jogador não tinha como digitar o XP que o
 * Mestre deu.
 *
 * É o mesmo formato do `Obs: [1]` e do RD da armadura: a regra pronta, e a tela
 * sem perguntar. Por isso o teste de fiação, abaixo, lê o código-fonte.
 */
class XpENivelTecnologicoTest {

    // ── XP ─────────────────────────────────────────────────────────────

    @Test
    fun `o XP entra no total disponivel`() {
        // O caso real do usuario: Cesar/Aureo foi FEITO com 150 pontos, e os
        // outros 164 sao experiencia de campanha.
        val cesar = Personagem(pontosIniciais = 150, xpGanhos = 164)
        assertEquals(314, cesar.pontosTotaisDisponiveis)
    }

    @Test
    fun `o XP pode ser maior que os pontos iniciais`() {
        // ⚠️ Nao e caso de borda: num personagem de campanha longa o XP passa dos
        // iniciais com folga -- 164 contra 150. Qualquer trava que assumisse "XP e
        // um numero pequeno" quebraria aqui.
        val cesar = Personagem(pontosIniciais = 150, xpGanhos = 164)
        assertTrue(cesar.xpGanhos > cesar.pontosIniciais)
        val r = cesar.rotuloDePontos
        assertTrue(r, r.contains("314"))
        assertTrue(r, r.contains("150"))
        assertTrue(r, r.contains("164 XP"))
    }

    @Test
    fun `o XP aumenta o que sobra, nao o que foi gasto`() {
        val semXp = Personagem(pontosIniciais = 150, forca = 12)
        val comXp = semXp.copy(xpGanhos = 20)
        assertEquals("o XP mexeu nos gastos", semXp.pontosGastos, comXp.pontosGastos)
        assertEquals(semXp.pontosRestantes + 20, comXp.pontosRestantes)
    }

    @Test
    fun `sem XP o cabecalho continua como sempre foi`() {
        // Regressão: quem nunca ganhou XP não pode ver a tela mudar.
        val heroi = Personagem(pontosIniciais = 150)
        assertEquals("Pontos Iniciais: 150", heroi.rotuloDePontos)
    }

    @Test
    fun `com XP o cabecalho mostra o total e a conta`() {
        // 🔴 Sem isto o cabeçalho diria 150 enquanto o personagem tem 314 —
        // e o jogador concluiria que os 164 de XP não entraram.
        val heroi = Personagem(pontosIniciais = 150, xpGanhos = 164)
        val r = heroi.rotuloDePontos
        assertTrue(r, r.contains("314"))
        assertTrue(r, r.contains("150"))
        assertTrue(r, r.contains("164 XP"))
    }

    @Test
    fun `a fala do cabecalho nao tem sinal cru`() {
        val falado = Personagem(pontosIniciais = 150, xpGanhos = 164).rotuloDePontosAcessivel
        assertFalse(falado, RotuloAcessivel.temSinalCru(falado))
        assertTrue(falado, falado.contains("314"))
    }

    // ── NT ─────────────────────────────────────────────────────────────

    @Test
    fun `o NT padrao e o medieval`() {
        // ⚠️ 3 é o NT da maior parte do catálogo de armas e armaduras do Básico.
        // Ficha antiga (Gson sem o campo) desserializa neste valor.
        assertEquals(3, Personagem().nivelTecnologico)
    }

    @Test
    fun `o NT nao mexe em nenhuma conta hoje`() {
        // Ele existe para as regras que dependem dele e ainda não foram feitas.
        // Se um dia alguém o ligar a uma conta sem avisar, este teste acusa.
        val nt0 = Personagem(pontosIniciais = 150, forca = 12, nivelTecnologico = 0)
        val nt12 = nt0.copy(nivelTecnologico = 12)
        assertEquals(nt0.pontosGastos, nt12.pontosGastos)
        assertEquals(nt0.pontosRestantes, nt12.pontosRestantes)
        assertEquals(nt0.danoGdP, nt12.danoGdP)
        assertEquals(nt0.danoGeB, nt12.danoGeB)
    }

    // ── A fiação, que é onde faltava ───────────────────────────────────

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    @Test
    fun `a aba Geral tem os dois campos`() {
        val tab = fonte("com/gurps/ficha/ui/TabGeral.kt")
        assertTrue("falta o campo de XP", tab.contains("label = { Text(\"XP\") }"))
        assertTrue("falta o campo de NT", tab.contains("label = { Text(\"NT\") }"))
        assertTrue("o XP nao e gravado", tab.contains("atualizarXpGanhos("))
        assertTrue("o NT nao e gravado", tab.contains("atualizarNivelTecnologico("))
    }

    @Test
    fun `o campo de XP DEFINE, nao acumula`() {
        // ⚠️ `sagaConcederXp` soma, porque é o Narrador premiando. O campo da
        // ficha define — somar faria o número crescer sozinho a cada toque.
        val vm = fonte("com/gurps/ficha/viewmodel/FichaViewModel.kt")
        val i = vm.indexOf("fun atualizarXpGanhos(")
        assertTrue("nao ha setter de XP", i > 0)
        val corpo = vm.substring(i, i + 260)
        assertTrue("o setter esta somando: $corpo", corpo.contains("xpGanhos = valor"))
        assertFalse("o setter acumula", corpo.contains("personagem.xpGanhos +"))
    }

    @Test
    fun `o cabecalho usa o rotulo com XP`() {
        val tela = fonte("com/gurps/ficha/ui/FichaScreen.kt")
        assertFalse(
            "o cabecalho voltou a escrever so os pontos iniciais",
            tela.contains("Pontos Iniciais: \${p.pontosIniciais}")
        )
        assertTrue(tela.contains("p.rotuloDePontos"))
    }

    @Test
    fun `mudar o NT entra no historico`() {
        val h = fonte("com/gurps/ficha/viewmodel/delegates/FichaHistoryDelegate.kt")
        assertTrue("o NT nao e registrado", h.contains("nivelTecnologico"))
    }
}
