package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.poderes.NumeroDeHabilidades
import com.gurps.ficha.domain.rules.poderes.ReservaDeEnergia
import com.gurps.ficha.model.Personagem
import com.gurps.ficha.model.Poder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    // ── POD-12/13: o botão Poderes da aba Rolagem ─────────────────────

    @Test
    fun `o botao Poderes segue o padrao de Tecnicas e Magias`() {
        // ⚠️ Só aparece quando há poder configurado. Personagem sem poder não
        // pode ganhar um botão morto — foi o próprio usuário quem propôs isso,
        // apontando o padrão que já existia na tela.
        val c = fonte("com/gurps/ficha/ui/features/rolagem/RolagemComponents.kt")
        assertTrue("o parametro nao existe", c.contains("showPoderes: Boolean"))
        assertTrue("o botao nao e condicional", c.contains("if (showPoderes) {"))

        val a = fonte("com/gurps/ficha/ui/TabRolagem.kt")
        assertTrue(
            "o botao aparece mesmo sem poder configurado",
            a.contains("showPoderes = p.poderes.isNotEmpty()")
        )
        assertTrue("o dialogo nao foi ligado", a.contains("DialogoPoderes("))
    }

    @Test
    fun `o dialogo da Rolagem usa as regras dos POD-11, 12 e 13`() {
        val d = fonte("com/gurps/ficha/ui/features/rolagem/DialogoPoderes.kt")
        assertTrue("nao diz o que a fonte manda rolar",
            d.contains("UsoDoPoder.Incapacitacao.explicar("))
        assertTrue("falta o esforco adicional", d.contains("EsforcoAdicional.penalidade("))
        assertTrue("falta a ampliacao temporaria",
            d.contains("AmpliacoesTemporarias.modificadorFinal("))
        assertTrue("falta o custo em PF da tentativa",
            d.contains("AmpliacoesTemporarias.pfDaTentativa("))
        // 🔴 O elo com o POD-11: a falha crítica atinge o poder INTEIRO.
        assertTrue("nao avisa que a falha critica atinge o poder inteiro",
            d.contains("poder INTEIRO checa"))
    }

    @Test
    fun `o dialogo NAO desconta PF sozinho`() {
        // ⚠️ Ele diz o número; quem gasta é a mesa. O app não sabe se o jogador
        // foi em frente com a proeza, e cobrar PF de uma tentativa que não
        // aconteceu seria pior do que não cobrar.
        val d = fonte("com/gurps/ficha/ui/features/rolagem/DialogoPoderes.kt")
        assertFalse("o dialogo esta mexendo nos PF do personagem",
            d.contains("aplicarFadiga") || d.contains("atualizarPf"))
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

    // ══ POD-20 — o rodapé do diálogo tem de continuar alcançável ═══════

    /**
     * 🔴 **Achado pelo usuário no aparelho.** O `PoderEditDialog` montava tudo
     * num `Column` sem rolagem e sem teto: com habilidades, Reserva de Energia
     * e as sugestões do livro, o rodapé com **Salvar e Cancelar** saía para
     * fora da tela. O poder ficava impossível de salvar.
     *
     * ⚠️ Este teste lê a **fonte**, e não o comportamento — layout de Compose
     * não roda em teste de unidade. É o mesmo formato dos testes de fiação:
     * ele guarda a decisão, não a pintura.
     */
    @Test
    fun `todo dialogo de poder rola o miolo e prende o rodape`() {
        val arquivos = listOf(
            "com/gurps/ficha/ui/features/traits/DialogsPoderes.kt",
            "com/gurps/ficha/ui/features/traits/PoderReservaEMontador.kt"
        )
        arquivos.forEach { caminho ->
            val src = fonte(caminho)
            // Todo diálogo com rodapé de ação precisa de um miolo que role.
            assertTrue(
                "$caminho perdeu a rolagem do miolo",
                src.contains("verticalScroll(") || src.contains("LazyColumn(")
            )
            // E de um teto de altura -- sem ele a rolagem nunca chega a valer,
            // porque o diálogo simplesmente cresce.
            assertTrue(
                "$caminho ficou sem teto de altura",
                src.contains("heightIn(max = ALTURA_MAXIMA_DO_DIALOGO)")
            )
        }
    }

    @Test
    fun `o miolo rolavel nao usa fill igual a false`() {
        // 🔴 Era `weight(1f, fill = false)` no montador: o miolo pedia mais
        // altura do que sobrava e o "Total" era desenhado POR CIMA do último
        // item. Com fill=true o miolo ocupa exatamente o que restou.
        listOf(
            "com/gurps/ficha/ui/features/traits/DialogsPoderes.kt",
            "com/gurps/ficha/ui/features/traits/PoderReservaEMontador.kt"
        ).forEach { caminho ->
            // ⚠️ Sem regex de propósito: `\n` dentro de string bruta do Kotlin
            // já me deu dois testes CEGOS nesta sessão (o `\b` que virou
            // backspace). Busca de texto simples não tem como sair errada.
            //
            // ⚠️ Mas as linhas de comentário saem fora: este mesmo arquivo
            // *explica* o defeito citando "fill = false", e o teste casaria com
            // a explicação em vez de casar com o código.
            val src = fonte(caminho)
                .lines()
                .filterNot { it.trimStart().startsWith("//") || it.trimStart().startsWith("*") }
                .joinToString("\n")
            assertFalse(
                "$caminho voltou a rolar com fill = false",
                src.contains("fill = false")
            )
        }
    }

    @Test
    fun `o teto de altura e um numero so`() {
        // ⚠️ Duas rotas para a mesma decisão: eram 600.dp num arquivo e 620.dp
        // no outro. A que ninguém olha é a que quebra.
        val src = fonte("com/gurps/ficha/ui/features/traits/DialogsPoderes.kt")
        assertTrue(
            "a constante do teto sumiu",
            src.contains("internal val ALTURA_MAXIMA_DO_DIALOGO")
        )
        val soltos = Regex("""heightIn\(max = \d+\.dp\)""").findAll(
            src + fonte("com/gurps/ficha/ui/features/traits/PoderReservaEMontador.kt")
        ).map { it.value }.toList()
        assertTrue("voltou altura solta: $soltos", soltos.isEmpty())
    }

    // == POD-24 -- o nome do modificador na hora de ESCOLHER ============

    @Test
    fun `o dialogo mostra o nome do modificador do Modulo Basico`() {
        // O nome existia desde o POD-15, mas so aparecia DEPOIS, ao ligar uma
        // habilidade. Na hora da escolha a tela so oferecia as fontes genericas
        // -- e o usuario concluiu, com razao, que nao tinha sido feito.
        val src = fonte("com/gurps/ficha/ui/features/traits/DialogsPoderes.kt")
        assertTrue(
            "o dialogo voltou a esconder o nome do modificador",
            src.contains("modificadorProprio.isNotBlank()")
        )
    }

    @Test
    fun `poder sem modificador nao oferece fonte nenhuma`() {
        // "Modificador de Poder: Nenhum, ja que as habilidades Antipsi nao
        // podem ser bloqueadas!" (MB p.256) -- e mesmo assim a tela oferecia
        // tres fontes ao Antipsi.
        val src = fonte("com/gurps/ficha/ui/features/traits/DialogsPoderes.kt")
        val i = src.indexOf("EscolhaDaFonte(")
        assertTrue("a chamada de EscolhaDaFonte sumiu", i > 0)
        // A escolha da fonte tem de estar sob a guarda de semModificador.
        val antes = src.substring(0, i)
        assertTrue(
            "a escolha da fonte deixou de ser condicional",
            antes.contains("definicao?.semModificador == true")
        )
        assertTrue(
            "o Talento deixou de sumir para quem nao tem",
            src.contains("definicao?.semTalento != true")
        )
    }

    // == POD-26 -- o montador so em poder personalizado ==================

    @Test
    fun `o montador nao aparece em poder de catalogo`() {
        // Nos 47 verbetes o valor ja vem com a fonte; o botao era so ruido.
        val src = fonte("com/gurps/ficha/ui/features/traits/DialogsPoderes.kt")
        val i = src.indexOf("\"Montar o modificador por componentes\"")
        assertTrue("o botao do montador sumiu por inteiro", i > 0)
        val guarda = src.substring(maxOf(0, i - 400), i)
        assertTrue(
            "o montador voltou a aparecer sempre",
            guarda.contains("if (definicao == null)")
        )
    }

    // == POD-25 e POD-19 -- regra recolhida, nunca apagada ==============

    @Test
    fun `os inconvenientes das alternativas ficam atras de um toque`() {
        val src = fonte("com/gurps/ficha/ui/features/traits/PoderHabilidades.kt")
        assertTrue(
            "os inconvenientes voltaram a ser despejados de uma vez",
            src.contains("if (mostrarPorque) {")
        )
        // E continuam existindo -- recolher nao e apagar.
        assertTrue(
            "os inconvenientes sumiram da tela",
            src.contains("HabilidadesAlternativas.INCONVENIENTES")
        )
    }

    @Test
    fun `a orientacao de quantidade diz que NAO e limite`() {
        // "Estes limites sao apenas sugestoes." Sem esta frase a lista de cinco
        // categorias parece uma reprovacao do poder do jogador.
        assertTrue(
            NumeroDeHabilidades.NAO_E_LIMITE,
            NumeroDeHabilidades.NAO_E_LIMITE.contains("apenas sugestões")
        )
        assertEquals(5, NumeroDeHabilidades.ORIENTACOES.size)
        assertEquals(19, NumeroDeHabilidades.PAGINA)
        val src = fonte("com/gurps/ficha/ui/features/traits/PoderHabilidades.kt")
        assertTrue(
            "a tela mostra as categorias sem a frase que as desarma",
            src.contains("NumeroDeHabilidades.NAO_E_LIMITE")
        )
    }

    @Test
    fun `a orientacao de quantidade nao inventa um contador`() {
        // 🔴 A tentacao era "voce tem 5 de movimento". Das 276 vantagens so 69
        // trazem categoria, e as 4 etiquetas do catalogo nao sao as 5 do livro.
        // Um numero errado e pior do que numero nenhum: parece verificacao.
        val regra = fonte("com/gurps/ficha/domain/rules/poderes/NumeroDeHabilidades.kt")
        assertFalse(
            "apareceu contagem automatica na regra de orientacao",
            regra.contains("fun contar") || regra.contains("fun quantas")
        )
    }
}
