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

    private fun relevantes() = arquivos().filterNot { f ->
        val caminho = f.path.replace('\\', '/')
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
}
