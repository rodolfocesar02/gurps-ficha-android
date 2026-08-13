package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.poderes.HabilidadesDoPoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **As habilidades de um poder** — GURPS Poderes, p.8 e p.34. Lote POD-5.
 *
 * O poder é a **playlist**; as habilidades são as músicas. Estes testes guardam
 * as duas pontas dessa ideia: quem pertence ao poder, e o que acontece com a
 * música quando a playlist é apagada.
 */
class HabilidadesDoPoderTest {

    private fun v(nome: String, custo: Int, poder: String?) = Triple(nome, custo, poder)

    // ── Quem pertence ao poder ─────────────────────────────────────────

    @Test
    fun `so entram os tracos ligados a ESTE poder`() {
        val r = HabilidadesDoPoder.resumir(
            idDoPoder = "p1",
            vantagens = listOf(
                v("Leitura da Mente", 30, "p1"),
                v("Voo", 40, "p2"),
                v("Riqueza", 20, null)
            ),
            desvantagens = listOf(v("Fobia", -10, "p1")),
            nivelDeTalento = 0,
            custoDoTalento = 0
        )
        assertEquals(2, r.quantidade)
        assertEquals(setOf("Leitura da Mente", "Fobia"), r.habilidades.map { it.nome }.toSet())
    }

    @Test
    fun `o indice devolvido aponta para a lista certa`() {
        // ⚠️ Vantagem e desvantagem são DUAS listas. Um índice global misturaria
        // as duas e a tela desligaria o traço errado.
        val r = HabilidadesDoPoder.resumir(
            idDoPoder = "p1",
            vantagens = listOf(v("Nada", 5, null), v("Telepatia", 30, "p1")),
            desvantagens = listOf(v("Insônia", -15, "p1")),
            nivelDeTalento = 0, custoDoTalento = 0
        )
        val vant = r.habilidades.single { !it.ehDesvantagem }
        val desv = r.habilidades.single { it.ehDesvantagem }
        assertEquals("o indice da vantagem esta errado", 1, vant.indice)
        assertEquals("o indice da desvantagem esta errado", 0, desv.indice)
    }

    // ── "Possuir o poder" (p.34) ───────────────────────────────────────

    @Test
    fun `basta UMA habilidade para possuir o poder`() {
        val r = HabilidadesDoPoder.resumir(
            "p1", listOf(v("Telepatia", 30, "p1")), emptyList(), 0, 0
        )
        assertTrue(r.possuiOPoder)
    }

    @Test
    fun `so o Talento tambem conta como possuir o poder`() {
        // "o Mestre pode permitir Talentos sem habilidades" (p.8).
        val r = HabilidadesDoPoder.resumir("p1", emptyList(), emptyList(), 2, 10)
        assertTrue(r.possuiOPoder)
        assertNotNull(HabilidadesDoPoder.avisoDePoderVazio(r))
    }

    @Test
    fun `sem habilidade e sem Talento o poder nao faz nada`() {
        val r = HabilidadesDoPoder.resumir("p1", emptyList(), emptyList(), 0, 0)
        assertFalse(r.possuiOPoder)
        val aviso = HabilidadesDoPoder.avisoDePoderVazio(r)!!
        assertTrue(aviso, aviso.contains("Talento"))
        assertFalse("a fala tem sinal cru", RotuloAcessivel.temSinalCru(aviso))
    }

    @Test
    fun `com habilidade nao ha aviso nenhum`() {
        val r = HabilidadesDoPoder.resumir(
            "p1", listOf(v("Telepatia", 30, "p1")), emptyList(), 1, 5
        )
        assertNull(HabilidadesDoPoder.avisoDePoderVazio(r))
    }

    // ── As contas ──────────────────────────────────────────────────────

    @Test
    fun `o custo soma habilidades e Talento, e a desvantagem entra negativa`() {
        val r = HabilidadesDoPoder.resumir(
            idDoPoder = "p1",
            vantagens = listOf(v("Telepatia", 30, "p1"), v("Escudo Mental", 12, "p1")),
            desvantagens = listOf(v("Fobia", -10, "p1")),
            nivelDeTalento = 3, custoDoTalento = 15
        )
        assertEquals(32, r.custoDasHabilidades)
        assertEquals(15, r.custoDoTalento)
        assertEquals(47, r.custoTotal)
    }

    // ── O nome do modificador injetado ─────────────────────────────────

    @Test
    fun `o modificador injetado e reconhecivel pelo prefixo`() {
        // Todo o desligar do app depende de conseguir achar este modificador
        // depois. Se o prefixo mudar num lugar e não no outro, ele vira órfão.
        val nome = HabilidadesDoPoder.nomeDoModificador("Telepatia")
        assertTrue(nome, nome.startsWith(HabilidadesDoPoder.PREFIXO_DO_MODIFICADOR))
        assertTrue(nome, nome.contains("Telepatia"))
        assertEquals("mod_poder_abc", HabilidadesDoPoder.idDoModificador("abc"))
    }

    // ── A fiação: o defeito que este lote conserta ─────────────────────

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    private val vm get() = fonte("com/gurps/ficha/viewmodel/FichaViewModel.kt")

    @Test
    fun `o poder e desligado das DUAS listas, nao so das vantagens`() {
        // 🔴 O defeito do lote: `removerPoder` e `atualizarPoder` só mexiam nas
        // vantagens — havia até um comentário "// Repetir para desvantagens"
        // parado no código. Uma desvantagem ligada a um poder apagado ficava
        // pagando `Mod. de Poder: X` de um poder que não existe mais.
        assertFalse(
            "o comentario de tarefa pendente voltou",
            vm.contains("Repetir para desvantagens")
        )
        val i = vm.indexOf("private fun reaplicarPoderNosTracos")
        assertTrue("a rota unica sumiu", i > 0)
        val corpo = vm.substring(i, i + 1400)
        assertTrue("nao mexe nas vantagens", corpo.contains("vantagens ="))
        assertTrue("nao mexe nas desvantagens", corpo.contains("desvantagens ="))
    }

    @Test
    fun `ligar desvantagem ao poder aplica o modificador`() {
        // 🔴 `vincularDesvantagemPoder` NAO aplicava o percentual: ligar a
        // desvantagem ao poder não mudava o custo dela. Só a vantagem funcionava.
        val i = vm.indexOf("fun vincularDesvantagemPoder(")
        assertTrue("o vinculo de desvantagem sumiu", i > 0)
        val corpo = vm.substring(i, i + 620)
        assertTrue(
            "a desvantagem nao recebe o modificador do poder: $corpo",
            corpo.contains("comModificadorDoPoder(")
        )
    }

    @Test
    fun `ha uma rota so para montar o modificador`() {
        // O defeito morava na diferença entre quatro cópias. Se voltarem a
        // existir, o filtro por prefixo aparece solto de novo.
        val solto = Regex("""filter \{ !it\.nome\.startsWith\("Mod\. de Poder:"\) \}""")
            .findAll(vm).count()
        assertEquals("voltou a haver copia do filtro de modificador", 0, solto)
        assertEquals(
            "deveria haver exatamente um montador",
            1,
            Regex("""private fun comModificadorDoPoder""").findAll(vm).count()
        )
    }

    @Test
    fun `a tela do poder mostra as habilidades`() {
        val d = fonte("com/gurps/ficha/ui/features/traits/DialogsPoderes.kt")
        assertTrue("o painel nao foi ligado ao dialogo", d.contains("PainelDeHabilidades("))
        assertTrue("a linha do poder nao diz quantas habilidades", d.contains("resumoCurtoDoPoder("))
        val p = fonte("com/gurps/ficha/ui/features/traits/PoderHabilidades.kt")
        assertTrue("nao da para ligar habilidade", p.contains("LigarHabilidadeDialog"))
        assertTrue("desligar nao existe", p.contains("vincularVantagemPoder(h.indice, null)"))
        assertTrue(
            "desligar da desvantagem nao existe",
            p.contains("vincularDesvantagemPoder(h.indice, null)")
        )
    }

    @Test
    fun `o painel nasceu num arquivo proprio`() {
        // Teto de 1000 linhas por arquivo. `DialogsPoderes.kt` já passa de 400.
        val d = fonte("com/gurps/ficha/ui/features/traits/DialogsPoderes.kt")
        val p = fonte("com/gurps/ficha/ui/features/traits/PoderHabilidades.kt")
        assertTrue("DialogsPoderes passou do teto", d.lines().size < 1000)
        assertTrue("PoderHabilidades passou do teto", p.lines().size < 1000)
    }
}
