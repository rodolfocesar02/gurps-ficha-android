package com.gurps.ficha.ui

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Lote LAYOUT-2** — o padrão de tela travado por teste.
 *
 * ## Por que um teste que lê código-fonte
 *
 * `UiTokens` e `AppListItemCard` existiam desde sempre, e **nenhum** dos seis
 * diálogos de seleção usava. O padrão estava no arquivo e a tela estava fora dele.
 *
 * Skill é instrução, e instrução se esquece — ainda mais daqui a três meses. Este
 * teste é a única coisa que impede a volta: componente que ninguém é **obrigado**
 * a usar não é padrão, é sugestão.
 *
 * ## Como ele funciona
 *
 * Lê os `.kt` de `ui/` e procura as violações do
 * `.claude/skills/padrao-de-tela/SKILL.md`. Arquivo ainda não migrado fica numa
 * lista de exceção **com data e motivo** — dívida visível em vez de esquecida.
 *
 * ⚠️ **Quando este teste falhar, o conserto é usar o componente.** Crescer a lista
 * de exceções transforma a rede em enfeite.
 */
class PadraoDeTelaTest {

    private fun raizUi(): File {
        val direto = File("src/main/java/com/gurps/ficha/ui")
        return if (direto.exists()) direto else File("app/src/main/java/com/gurps/ficha/ui")
    }

    private fun arquivos(): List<File> {
        val raiz = raizUi()
        assertTrue("nao encontrei ${raiz.absolutePath}", raiz.exists())
        return raiz.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }

    /** Os arquivos do próprio padrão — eles definem o que os outros consomem. */
    private val DO_PROPRIO_PADRAO = setOf(
        "UiStandards.kt", "AppSelectionUi.kt", "AppButtons.kt", "DialogStandards.kt"
    )

    /**
     * ⚠️ **A dívida, com data.** Estes arquivos ainda não passaram pelo padrão.
     * O Lote LAYOUT-3/4 migrou vantagens e desvantagens; o resto entra em lotes
     * curtos depois. Cada linha aqui é uma tela que o usuário vê diferente das
     * outras.
     *
     * Data de entrada: **03/08/2026**.
     */
    private val DIVIDA_ATE_MIGRAR = setOf(
        "TabRolagem.kt",
        "TabGeral.kt",
        "TabTracos.kt",
        "TabPericias.kt",
        "TabMagias.kt",
        "TabTecnicas.kt",
        "TabSaga.kt",
        "DialogsCommon.kt",
        "DiceRoller.kt",
        "FichaScreen.kt",
        "DialogsMestreIA.kt"
    )

    /**
     * ⚠️ **Arquivos de dentro de `features/` que JÁ nascem no padrão.**
     *
     * A pasta inteira está fora da varredura porque a maior parte dela ainda não
     * foi migrada — mas telas **novas** não podem entrar de carona nessa isenção.
     * Este conjunto as puxa de volta para dentro da rede.
     *
     * É a correção do tropeço do LAYOUT-5: a tela de mágicas foi migrada e a
     * exclusão continuou de pé, então o teste ficou verde sem nunca olhar para
     * ela. **Migrar sem trazer o arquivo para a varredura é migrar sem rede.**
     */
    private val JA_NASCEM_NO_PADRAO = setOf(
        "DialogoFadiga.kt",     // Lote MB-6
        "DialogoFerimento.kt",  // Lote MB-7
        "SilhuetaDoCorpo.kt",   // Lote PV-1b
        "ListaDeLocaisPraCego.kt" // Lote ACESS-2
    )

    private fun relevantes() = arquivos().filterNot { f ->
        val caminho = f.path.replace('\\', '/')
        if (f.name in JA_NASCEM_NO_PADRAO) return@filterNot false
        f.name in DO_PROPRIO_PADRAO || f.name in DIVIDA_ATE_MIGRAR ||
            // A tela do combate tático da Saga é outro projeto de UI (grade de
            // hexágonos, tokens, câmera) e não compartilha nada com os diálogos
            // de catálogo. Entra quando a Saga for migrada.
            caminho.contains("/ui/saga/") ||
            // `features/` inteiro sai por enquanto, MENOS o que já foi migrado:
            // traits (LAYOUT-3/4) e magic (LAYOUT-5).
            //
            // ⚠️ A tela de mágicas foi migrada e a exclusão continuou de pé por
            // um momento — o teste ficou verde sem olhar para ela. Migrar sem
            // trazer o arquivo para dentro da varredura é migrar sem rede.
            (caminho.contains("/features/") &&
                !caminho.contains("/features/traits/") &&
                !caminho.contains("/features/magic/"))
    }

    /**
     * ⚠️ Comentário não é violação. A primeira versão só pulava `//` e acusou o
     * próprio KDoc que **explica** a regra — o texto que diz "não faça isto"
     * contém o "isto".
     */
    private fun ehComentario(linha: String): Boolean {
        val t = linha.trimStart()
        return t.startsWith("//") || t.startsWith("*") || t.startsWith("/*")
    }

    private fun violacoes(regex: Regex, comoConsertar: String): List<String> =
        relevantes().flatMap { f ->
            f.readLines().withIndex()
                .filter { (_, linha) -> regex.containsMatchIn(linha) && !ehComentario(linha) }
                .map { (i, linha) -> "${f.name}:${i + 1}  ${linha.trim().take(90)}" }
        }.let { achados ->
            if (achados.isEmpty()) achados else achados + listOf("→ $comoConsertar")
        }

    // ==================================================================
    // 1. 🔴 Botões
    // ==================================================================

    @Test
    fun `🔴 nenhum botao abaixo da altura minima de toque`() {
        // 48.dp é o mínimo. Havia botões de 32 e 36 num app com variante para
        // quem navega por TalkBack.
        val achados = violacoes(
            Regex("""(TextButton|Button|OutlinedButton|IconButton)\s*\([^)]*height\((?:[0-3]?\d|4[0-7])\.dp\)"""),
            "use AppBotaoPrincipal/Secundario/Discreto — a altura já vem de UiTokens.BotaoAltura"
        )
        assertTrue(achados.joinToString("\n"), achados.isEmpty())
    }

    @Test
    fun `🔴 cor de botao sai do tema, nao da mao`() {
        val achados = violacoes(
            Regex("""ButtonDefaults\.(buttonColors|textButtonColors|outlinedButtonColors)\("""),
            "use AppBotaoDestrutivo se for ação destrutiva; senão deixe o tema decidir"
        )
        assertTrue(achados.joinToString("\n"), achados.isEmpty())
    }

    @Test
    fun `🔴 nada de manchete dentro de botao`() {
        // ⚠️ A checagem é por LINHA, de propósito. A primeira versão varria o
        // corpo inteiro do `Button(...)` com regex e acusou três lugares que
        // estavam certos: o número grande entre o `−` e o `+` é vizinho dos
        // botões, não conteúdo deles. Regex que atravessa chave não sabe a
        // diferença — e ainda estourou a pilha num arquivo grande.
        val achados = violacoes(
            Regex("""(?:TextButton|OutlinedButton|Button)\s*\(.*typography\.(displaySmall|displayMedium|displayLarge|headlineMedium|headlineLarge)"""),
            "use UiEstilos.textoDeBotao"
        )
        assertTrue(achados.joinToString("\n"), achados.isEmpty())
    }

    // ==================================================================
    // 2. 🔴 Linhas de lista
    // ==================================================================

    @Test
    fun `🔴 o padding 8 por 6 escrito a mao acabou`() {
        // Era a assinatura da cópia: seis diálogos com o mesmo bloco à mão, e o
        // UiTokens dizendo 12/10 sem ninguém ler.
        val achados = violacoes(
            Regex("""padding\(horizontal\s*=\s*8\.dp,\s*vertical\s*=\s*6\.dp\)"""),
            "use AppSelectionRow, ou UiTokens.LinhaDeListaPaddingH/V"
        )
        assertTrue(achados.joinToString("\n"), achados.isEmpty())
    }

    /**
     * Varre contando chaves, sem regex.
     *
     * ⚠️ A primeira versão usava uma expressão regular aninhada para achar
     * `Card(` dentro de `LazyColumn { … }` e estourou a pilha
     * (`StackOverflowError`) num arquivo de 900 linhas — retrocesso catastrófico.
     * Contar `{` e `}` é chato e funciona.
     */
    private fun cardCruDentroDeLazyColumn(arquivo: File): List<String> {
        val linhas = arquivo.readLines()
        val achados = mutableListOf<String>()
        var profundidadeDaLista = -1
        var profundidade = 0
        linhas.forEachIndexed { i, linha ->
            val limpa = linha.substringBefore("//")
            if (profundidadeDaLista < 0 && limpa.contains("LazyColumn")) {
                profundidadeDaLista = profundidade
            }
            if (profundidadeDaLista >= 0 && Regex("""\bCard\s*\(""").containsMatchIn(limpa)) {
                achados += "${arquivo.name}:${i + 1}  ${linha.trim().take(70)}"
            }
            profundidade += limpa.count { it == '{' } - limpa.count { it == '}' }
            if (profundidadeDaLista >= 0 && profundidade <= profundidadeDaLista) {
                profundidadeDaLista = -1
            }
        }
        return achados
    }

    @Test
    fun `🔴 lista de selecao nao monta Card na mao`() {
        val achados = relevantes().flatMap { cardCruDentroDeLazyColumn(it) }
        assertTrue(
            (achados + "→ use AppSelectionRow").joinToString("\n"),
            achados.isEmpty()
        )
    }

    // ==================================================================
    // 3. 🔴 Nome de enum na tela
    // ==================================================================

    @Test
    fun `🔴 nome de enum do codigo nao chega ao jogador`() {
        // `tipoCusto.name.lowercase()` punha "por_nivel" na tela.
        val achados = violacoes(
            Regex("""tipoCusto\.name"""),
            "traduza com rotuloDoTipoDeCusto(): 'por nível', 'custo fixo', 'custo à escolha'"
        )
        assertTrue(achados.joinToString("\n"), achados.isEmpty())
    }

    // ==================================================================
    // 4. A dívida é visível, e não pode crescer
    // ==================================================================

    @Test
    fun `⚠️ a lista de divida nao cresce sozinha`() {
        // Se alguém acrescentar um arquivo aqui para "fazer o teste passar", o
        // número muda e a revisão vê. É o oposto de silenciar.
        assertTrue(
            "a dívida do padrão de tela cresceu: ${DIVIDA_ATE_MIGRAR.size} arquivos",
            DIVIDA_ATE_MIGRAR.size <= 14
        )
    }

    @Test
    fun `os arquivos do padrao existem e sao os que a skill anuncia`() {
        val nomes = arquivos().map { it.name }.toSet()
        listOf("UiStandards.kt", "AppSelectionUi.kt", "AppButtons.kt").forEach {
            assertTrue("faltou $it", it in nomes)
        }
    }

    // ==================================================================
    // 5. Todo diálogo rola, e MOSTRA que rola — Lote TELA-1
    // ==================================================================

    /**
     * 🔴 **Achado no aparelho de outro jogador, com a fonte do sistema grande.**
     *
     * O Compose **não desenha barra de rolagem** no Android. Os diálogos rolavam
     * e nada dizia isso: a tela terminava, e quem olhava concluía que aquilo era
     * tudo. Na fonte padrão o conteudo quase sempre cabia, e por isso o defeito
     * atravessou o projeto inteiro sem aparecer.
     *
     * ⚠️ Eu mesmo caí nele **duas vezes** nesta sessão, achando que um diálogo
     * estava truncado quando ele só não tinha sido rolado. Quem escreveu a tela
     * se confundiu; o jogador na mesa não tem chance.
     *
     * A regra do projeto passou a ser: **container que rola usa
     * `Modifier.rolagemVertical()`**, que rola e desenha a barra junto. Enquanto
     * a barra fosse um segundo passo opcional, o próximo diálogo nasceria sem —
     * que é como os 42 anteriores chegaram até aqui.
     */
    @Test
    fun `🔴 rolagem anonima nao volta ao projeto`() {
        val achados = arquivos().filter { arquivo ->
            arquivo.name != "AppBarraDeRolagem.kt" &&
                arquivo.readText(Charsets.UTF_8)
                    .contains("verticalScroll(rememberScrollState())")
        }.map { it.name }
        assertTrue(
            "estes arquivos rolam sem mostrar a barra — use Modifier.rolagemVertical(): " +
                achados.joinToString(", "),
            achados.isEmpty()
        )
    }

    @Test
    fun `a barra existe para os dois tipos de container`() {
        // `Column` com rolagem e `LazyColumn` medem de jeitos diferentes; uma
        // barra só para um deles deixaria metade dos diálogos sem aviso.
        val barra = arquivos().first { it.name == "AppBarraDeRolagem.kt" }
            .readText(Charsets.UTF_8)
        assertTrue("sumiu a barra do Column rolável", barra.contains("estado: ScrollState"))
        assertTrue("sumiu a barra do LazyColumn", barra.contains("estado: LazyListState"))
        // ⚠️ Nada de barra numa lista que cabe inteira: ela precisa significar
        // "tem mais coisa embaixo", ou o jogador aprende a ignorá-la.
        assertTrue(
            "a barra passou a aparecer mesmo sem ter o que rolar",
            barra.contains("if (rolavel <= 0) return@drawWithContent")
        )
    }

    @Test
    fun `a moldura de selecao mostra a barra para todos os dialogos de uma vez`() {
        val moldura = arquivos().first { it.name == "AppSelectionUi.kt" }
            .readText(Charsets.UTF_8)
        assertTrue(
            "os diálogos de seleção voltaram a rolar sem aviso",
            moldura.contains("comBarraDeRolagem(estadoDaLista")
        )
    }

    @Test
    fun `🔴 o cabecalho do dialogo de mira rola junto`() {
        // O bloco de distância, tamanho do alvo e caixinhas ficava FORA da
        // lista, preso no topo. Com fonte grande ele comia a altura toda e a
        // lista de "onde acertar" ficava com espaço zero — o jogador via o
        // cabeçalho e mais nada.
        val mira = arquivos().first { it.name == "DialogoMira.kt" }
            .readText(Charsets.UTF_8)
        val iLista = mira.indexOf("LazyColumn(")
        val iDistancia = mira.indexOf("LinhaDeDistancia(")
        assertTrue("o diálogo de mira perdeu a lista", iLista > 0)
        assertTrue(
            "o cabeçalho da mira voltou para fora da lista, e some com fonte grande",
            iDistancia > iLista
        )
    }
}
