package com.gurps.ficha.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lotes MB-6 e MB-7** — a fiação dos botões PV e PF.
 *
 * ## 🔴 Por que este arquivo existe
 *
 * `FadigaRulesTest` e `FerimentoPorLocalRulesTest` conferem a **regra**. Nenhum
 * dos dois nota se a palavra "PF" na tela simplesmente **não abre nada** — a
 * regra fica verde, perfeita, e o botão não faz coisa alguma no aparelho.
 *
 * É a mesma armadilha de sempre: teste da camada errada. A fiação passa por
 * **quatro** arquivos (`RolagemComponents` → `PainelAtributosEStatus` →
 * `TabRolagem` → `FichaViewModel`), e os dois callbacks têm valor padrão `{}`.
 * Um `= {}` esquecido no meio do caminho compila, roda e não faz nada.
 *
 * Não há Robolectric no projeto, então a conferência é lendo o código-fonte —
 * grosseiro, mas pega exatamente o defeito que a regra não pega.
 */
class BotoesPvPfLigadosTest {

    private fun fonte(caminhoRelativo: String): String {
        val arq = listOf("src/main/java/$caminhoRelativo", "app/src/main/java/$caminhoRelativo")
            .map { File(it) }.firstOrNull { it.exists() }
        assertTrue("não encontrei $caminhoRelativo", arq != null)
        return arq!!.readText()
    }

    private val rolagem = "com/gurps/ficha/ui/features/rolagem"

    @Test
    fun `🔴 as palavras PV e PF abrem alguma coisa`() {
        val t = fonte("$rolagem/RolagemComponents.kt")
        assertTrue("a palavra PV não está clicável", t.contains("onAbrirPainelPv()"))
        assertTrue("a palavra PF não está clicável", t.contains("onAbrirPainelPf()"))
    }

    @Test
    fun `🔴 o painel do meio REPASSA os dois callbacks`() {
        // Este é o elo que some sem avisar: o parâmetro tem default `{}`, então
        // esquecer de repassar compila e o botão fica morto.
        val t = fonte("$rolagem/PainelAtributosEStatus.kt")
        assertTrue(
            "PainelAtributosEStatus não repassa onAbrirPainelPv",
            t.contains("onAbrirPainelPv = onAbrirPainelPv")
        )
        assertTrue(
            "PainelAtributosEStatus não repassa onAbrirPainelPf",
            t.contains("onAbrirPainelPf = onAbrirPainelPf")
        )
    }

    @Test
    fun `🔴 a TabRolagem liga os callbacks nos dialogos`() {
        val t = fonte("com/gurps/ficha/ui/TabRolagem.kt")
        assertTrue("nada abre o painel de ferimento", t.contains("onAbrirPainelPv = { showFerimentoDialog = true }"))
        assertTrue("nada abre o painel de fadiga", t.contains("onAbrirPainelPf = { showFadigaDialog = true }"))
        assertTrue("o DialogoFadiga não é chamado", t.contains("DialogoFadiga("))
        assertTrue("o DialogoFerimento não é chamado", t.contains("DialogoFerimento("))
    }

    @Test
    fun `🔴 salvar chega no ViewModel — senao nada persiste`() {
        // Um diálogo que calcula certo e não grava é pior que não existir: o
        // jogador fecha achando que anotou.
        val t = fonte("com/gurps/ficha/ui/TabRolagem.kt")
        assertTrue(t.contains("viewModel.aplicarPainelDeFadiga("))
        assertTrue(t.contains("viewModel.aplicarFerimentoPorLocal("))

        val vm = fonte("com/gurps/ficha/viewmodel/FichaViewModel.kt")
        // ⚠️ E o ViewModel precisa SALVAR. Sem `salvarFicha()` o número volta ao
        // que era assim que o app é fechado.
        listOf("aplicarPainelDeFadiga", "aplicarFerimentoPorLocal").forEach { nome ->
            val trecho = vm.substringAfter("fun $nome(").substringBefore("\n    fun ")
            assertTrue("$nome não chama salvarFicha()", trecho.contains("salvarFicha()"))
        }
    }

    @Test
    fun `⚠️ a variante PraCego tambem tem como abrir os paineis`() {
        // Gesto sem affordance é a violação nº 7 do padrão de tela. Na variante
        // visual a palavra é sublinhada e clicável; na PraCego precisa ser botão
        // rotulado, porque não dá para descobrir um alvo de toque tateando.
        val t = fonte("$rolagem/RolagemComponents.kt")
        // ⚠️ Ancorado no PvPfQuickRollPanel de propósito: `if (isPraCegoVariant)`
        // aparece quatro vezes no arquivo, e a primeira é de outro composable.
        // A primeira versão deste teste pegou o bloco errado e reprovou por isso.
        val painel = t.substringAfter("fun PvPfQuickRollPanel(")
        val praCego = painel.substringAfter("if (isPraCegoVariant) {").substringBefore("    } else {")
        assertTrue("falta o botão do painel de PV na variante PraCego", praCego.contains("onAbrirPainelPv"))
        assertTrue("falta o botão do painel de PF na variante PraCego", praCego.contains("onAbrirPainelPf"))
    }

    @Test
    fun `🔴 os campos novos existem na ficha — sem eles nada sobrevive ao fechar o app`() {
        val p = fonte("com/gurps/ficha/model/Personagem.kt")
        assertTrue("falta fadigaPorFonte", p.contains("var fadigaPorFonte: Map<String, Int> = emptyMap()"))
        assertTrue(
            "falta armadurasGuardadas",
            p.contains("var armadurasGuardadas: List<String> = emptyList()")
        )
    }

    @Test
    fun `⚠️ a ficha guarda o que esta GUARDADO, nao o que esta vestido`() {
        // Se alguém inverter para `armadurasVestidas`, toda ficha existente
        // (lista vazia) passa a andar nua e perde a RD sem nenhum aviso.
        val p = fonte("com/gurps/ficha/model/Personagem.kt")
        assertTrue(
            "o campo virou 'vestidas' — isso zera a RD de toda ficha antiga",
            !p.contains("armadurasVestidas")
        )
    }
}
