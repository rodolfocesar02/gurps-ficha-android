package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.poderes.ReservaDeEnergia
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.Poder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **As telas da Reserva de Energia e do montador** — Lotes POD-9 e POD-7.
 *
 * As regras já estavam prontas e testadas; estes testes guardam a **fiação** —
 * o formato de defeito que mais apareceu neste projeto é a regra existir e a
 * tela não perguntar.
 */
class TelasDePoderTest {

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    // ── POD-9: a Reserva de Energia custa pontos ───────────────────────

    @Test
    fun `a Reserva de Energia entra nos pontos gastos`() {
        // 🔴 A RE **é** uma vantagem comprada ("trate-os como uma nova vantagem",
        // p.119). Guardá-la sem cobrar seria um recurso de graça.
        val sem = Personagem(pontosIniciais = 150)
        val com = sem.copy(poderes = listOf(Poder(nome = "Telepatia", reservaDeEnergia = 10)))
        assertEquals(0, sem.pontosPoderes)
        assertEquals(30, com.pontosPoderes)                 // 10 PF × 3
        assertEquals(sem.pontosGastos + 30, com.pontosGastos)
    }

    @Test
    fun `a limitacao da RE barateia o que entra na ficha`() {
        val p = Poder(
            nome = "Fogo",
            reservaDeEnergia = 10,
            limitacoesDaReserva = listOf(ReservaDeEnergia.Limitacao.CARGA_ESPECIAL.name)
        )
        assertEquals(9, p.custoDaReserva)                   // 30 com −70%
        assertEquals(9, Personagem(poderes = listOf(p)).pontosPoderes)
    }

    @Test
    fun `id de limitacao que nao existe mais e ignorado`() {
        // ⚠️ Ficha antiga, ou um enum renomeado, não pode derrubar a ficha nem
        // mudar o custo em silêncio: o id órfão simplesmente sai.
        val p = Poder(nome = "X", reservaDeEnergia = 10, limitacoesDaReserva = listOf("SUMIU"))
        assertTrue(p.limitacoesDaReserveResolvidas.isEmpty())
        assertEquals(30, p.custoDaReserva)
    }

    @Test
    fun `sem Reserva nao ha custo`() {
        // Regressão: quem só tem Talento não pode ver o total mudar.
        val p = Poder(nome = "X", nivelTalento = 2)
        assertEquals(10, Personagem(poderes = listOf(p)).pontosPoderes)
    }

    // ── POD-6: o desconto de alternativas na ficha ────────────────────

    private fun vant(nome: String, custo: Int, poder: String?, alt: Boolean) =
        com.gurps.ficha.model.VantagemSelecionada(
            definicaoId = nome, nome = nome, custoEscolhido = custo,
            tipoCusto = com.gurps.ficha.model.TipoCusto.FIXO, poderId = poder, alternativaDoPoder = alt
        )

    @Test
    fun `o grupo alternativo desconta no total da ficha`() {
        // O exemplo do livro, agora atravessando a ficha inteira: 36+18+18
        // comprados soltos custam 72; como alternativas, 44.
        val soltas = Personagem(vantagens = listOf(
            vant("Voo", 36, "p1", false),
            vant("Super Salto", 18, "p1", false),
            vant("Caminhar no Ar", 18, "p1", false)
        ))
        val grupo = Personagem(vantagens = listOf(
            vant("Voo", 36, "p1", true),
            vant("Super Salto", 18, "p1", true),
            vant("Caminhar no Ar", 18, "p1", true)
        ))
        assertEquals(72, soltas.pontosVantagens)
        assertEquals(44, grupo.pontosVantagens)
    }

    @Test
    fun `o desconto e POR PODER, nao um grupo so`() {
        // 🔴 Somar tudo num grupo único daria um desconto que o livro não dá:
        // cada poder tem a SUA habilidade mais cara.
        val duas = Personagem(vantagens = listOf(
            vant("A", 30, "p1", true), vant("B", 30, "p1", true),
            vant("C", 30, "p2", true), vant("D", 30, "p2", true)
        ))
        // Por poder: (30 + 6) × 2 = 72. Num grupo só seria 30 + 6×3 = 48.
        assertEquals(72, duas.pontosVantagens)
    }

    @Test
    fun `alternativa sem poder nao ganha desconto`() {
        // ⚠️ Alternativa é sempre alternativa DENTRO de um poder. Marcada solta,
        // ela seria um desconto de graça.
        val p = Personagem(vantagens = listOf(
            vant("A", 30, null, true), vant("B", 30, null, true)
        ))
        assertEquals(60, p.pontosVantagens)
    }

    @Test
    fun `vantagem comum no mesmo poder nao entra no grupo`() {
        val p = Personagem(vantagens = listOf(
            vant("Voo", 36, "p1", true),
            vant("Super Salto", 18, "p1", true),
            vant("Riqueza", 20, "p1", false)     // ligada, mas não alternativa
        ))
        assertEquals(36 + 4 + 20, p.pontosVantagens)
    }

    @Test
    fun `a tela deixa marcar a alternativa`() {
        val p = fonte("com/gurps/ficha/ui/features/traits/PoderHabilidades.kt")
        assertTrue("nao da para marcar alternativa", p.contains("marcarAlternativa("))
        assertTrue("a economia do grupo nao aparece",
            p.contains("HabilidadesAlternativas.resumo("))
        assertTrue("os inconvenientes nao aparecem",
            p.contains("HabilidadesAlternativas.INCONVENIENTES"))
        val vm = fonte("com/gurps/ficha/viewmodel/FichaViewModel.kt")
        val i = vm.indexOf("fun marcarAlternativa(")
        assertTrue("o setter nao existe", i > 0)
        assertTrue("marca alternativa em vantagem sem poder",
            vm.substring(i, i + 400).contains("v.poderId != null"))
    }

    // ── A fiação das duas telas ────────────────────────────────────────

    @Test
    fun `o dialogo do poder tem o painel da Reserva`() {
        val d = fonte("com/gurps/ficha/ui/features/traits/DialogsPoderes.kt")
        assertTrue("o painel da RE nao foi ligado", d.contains("PainelDaReserva("))
        val p = fonte("com/gurps/ficha/ui/features/traits/PoderReservaEMontador.kt")
        assertTrue("nao da para escolher limitacao",
            p.contains("ReservaDeEnergia.Limitacao.entries"))
        assertTrue("o conflito do livro nao e avisado", p.contains("ReservaDeEnergia.conflitos("))
        assertTrue("nao diz que esgotar a RE nao e PF baixo",
            p.contains("não causa os efeitos de PF baixo"))
    }

    @Test
    fun `o montador escreve no MESMO campo de modificador`() {
        // ⚠️ Um campo próprio para o resultado do montador criaria dois lugares
        // com o mesmo número — e o defeito moraria na diferença, como já
        // aconteceu quatro vezes neste projeto.
        val d = fonte("com/gurps/ficha/ui/features/traits/DialogsPoderes.kt")
        assertTrue("o montador nao foi ligado", d.contains("MontadorDeModificadorDialog("))
        val i = d.indexOf("onAplicar = { total ->")
        assertTrue("o montador nao aplica nada", i > 0)
        assertTrue(
            "o montador nao escreve no campo de modificador",
            d.substring(i, i + 260).contains("modificador = total.toString()")
        )
    }

    @Test
    fun `no montador, o grupo aceita uma escolha so`() {
        // Contramedidas é UMA escolha; marcar duas cobraria o mesmo
        // inconveniente duas vezes. Só o grupo "Outros" acumula.
        val p = fonte("com/gurps/ficha/ui/features/traits/PoderReservaEMontador.kt")
        assertTrue("a escolha unica por grupo sumiu",
            p.contains("escolhidos.filterNot { it.grupo == grupo }"))
        assertTrue("o grupo Outros deixou de acumular",
            p.contains("Grupo.EXTRAS) escolhidos + c"))
    }

    @Test
    fun `os arquivos novos cabem no teto do projeto`() {
        listOf(
            "com/gurps/ficha/ui/features/traits/DialogsPoderes.kt",
            "com/gurps/ficha/ui/features/traits/PoderReservaEMontador.kt",
            "com/gurps/ficha/ui/features/traits/PoderHabilidades.kt"
        ).forEach {
            val linhas = fonte(it).lines().size
            assertTrue("$it passou de 1000 linhas ($linhas)", linhas < 1000)
        }
    }
}
