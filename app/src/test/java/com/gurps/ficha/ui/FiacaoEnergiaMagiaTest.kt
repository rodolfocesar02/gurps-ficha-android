package com.gurps.ficha.ui

import java.io.File
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lotes MAGIA-E1 e E2** — a fiação do gasto de energia.
 *
 * ## 🔴 Por que este arquivo existe
 *
 * O `MagiaEnergiaRulesTest` confere a **regra**: sucesso decisivo não gasta,
 * fracasso perde 1, a faixa é lida, `"Varia"` pergunta. Ele estava todo verde —
 * e no aparelho **o PF não baixava**.
 *
 * O defeito não estava na conta. Estava em duas chamadas, em arquivos
 * diferentes, na ordem errada:
 *
 * > O botão **Aplicar** chamava `onAplicar(...)` e, logo em seguida,
 * > `onDismiss()`. Enquanto o cancelar não fazia nada, chamar os dois era
 * > inofensivo. No E2 o `onDismiss` virou *"desisti da magia"* e passou a limpar
 * > a energia comprometida — então Aplicar comprometia o valor, disparava a
 * > rolagem, e **apagava o compromisso no instante seguinte**.
 *
 * ⚠️ É o terceiro defeito desta forma nesta frente: **quando existem dois
 * caminhos para a mesma coisa, o erro mora na diferença entre eles, e ele não
 * parece erro** — os dois compilam e cada um, lido sozinho, está certo.
 *
 * Sem Robolectric no projeto, a conferência é lendo o código-fonte. Grosseiro, e
 * pega exatamente o que a regra não pega.
 */
class FiacaoEnergiaMagiaTest {

    private fun fonte(caminhoRelativo: String): String {
        val arq = listOf("src/main/java/$caminhoRelativo", "app/src/main/java/$caminhoRelativo")
            .map { File(it) }.firstOrNull { it.exists() }
        assertNotNull("não encontrei $caminhoRelativo", arq)
        return arq!!.readText()
    }

    private val dialogos = "com/gurps/ficha/ui/features/rolagem/RolagemSecondaryDialogs.kt"
    private val aba = "com/gurps/ficha/ui/TabRolagem.kt"

    @Test
    fun `🔴 Aplicar e Ignorar sao caminhos EXCLUSIVOS`() {
        // O bug: `onAplicar(...)` seguido de `onDismiss()` no mesmo onClick.
        // Como o Ignorar limpa a energia comprometida, o Aplicar desfazia o
        // próprio trabalho e o PF não baixava.
        val t = fonte(dialogos)
        val trecho = t.substringAfter("fun RolagemEnergiaManualDialog(")
            .substringBefore("\n@Composable")
        val aplicou = trecho.indexOf("onAplicar(")
        assertTrue("não achei a chamada de onAplicar", aplicou > 0)
        val depoisDoAplicar = trecho.substring(aplicou)
            .substringBefore("dismissButton")
        assertTrue(
            "o botão Aplicar ainda chama onDismiss() logo depois — isso apaga a " +
                "energia comprometida e o PF não baixa",
            !Regex("""onAplicar\([^\n]*\)[\s\S]{0,200}?^\s{20}onDismiss\(\)""", RegexOption.MULTILINE)
                .containsMatchIn(depoisDoAplicar)
        )
    }

    @Test
    fun `🔴 comprometer NAO desconta PF — quem desconta e a rolagem`() {
        // Se o desconto voltar para o momento do comprometimento, o resultado
        // dos dados deixa de valer e voltamos a cobrar o cheio sempre.
        val t = fonte(aba)
        val trecho = t.substringAfter("fun comprometerEnergiaMagia(").substringBefore("\n    fun ")
        assertTrue(
            "comprometerEnergiaMagia voltou a descontar PF direto",
            !trecho.contains("atualizarPontosFadigaRolagemAtual")
        )
        assertTrue("não guarda o valor comprometido", trecho.contains("energiaComprometida = custo"))
    }

    @Test
    fun `🔴 a rolagem cobra a energia conforme o resultado`() {
        val t = fonte(aba)
        val trecho = t.substringAfter("fun finalizarRolagem(").substringBefore("\n    fun ")
        assertTrue("a rolagem não cobra energia", trecho.contains("MagiaEnergiaRules.energiaGasta("))
        assertTrue("não distingue o sucesso decisivo", trecho.contains("SUCESSO_DECISIVO"))
        assertTrue("não distingue a falha crítica", trecho.contains("FALHA_CRITICA"))
        assertTrue("não trata a exceção da mágica de informação", trecho.contains("ehMagiaDeInformacao"))
        assertTrue("não desconta o PF", trecho.contains("atualizarPontosFadigaRolagemAtual"))
    }

    @Test
    fun `⚠️ o campo do dialogo recebe o custo BASE, nao o ja descontado`() {
        // O diálogo mostra "Redução por NH" e aplica o desconto sozinho.
        // Sugerir o valor já reduzido descontava DUAS vezes: um custo 4 com
        // NH 18 virava 2 em vez de 3.
        val t = fonte(aba)
        val trecho = t.substringAfter("fun pedirEnergiaAntesDeRolar(").substringBefore("\n    fun ")
        assertTrue(
            "o prefill voltou a usar o valor descontado",
            trecho.contains("energiaManualInput = custo.minimo.toString()")
        )
    }

    @Test
    fun `🔴 toda magia com custo pergunta ANTES de rolar`() {
        // O caminho do custo fixo já rolou primeiro e confirmou depois. Quando a
        // cobrança foi para dentro da rolagem, esse caminho passou a comprometer
        // energia tarde demais e a magia saía de graça.
        val t = fonte(aba)
        assertTrue(
            "voltou a existir um caminho que rola antes de perguntar",
            !t.contains("tratarCustoEnergiaAposRolagemMagia")
        )
        assertTrue(
            "a rolagem de magia não passa mais pelo pedido de energia",
            t.contains("if (!pedirEnergiaAntesDeRolar(magia, modMagia))")
        )
    }

    @Test
    fun `⚠️ desistir limpa o compromisso`() {
        val t = fonte(aba)
        val trecho = t.substringAfter("RolagemEnergiaManualDialog(").substringBefore("if (showSentidosDialog)")
        assertTrue("cancelar não limpa a energia comprometida", trecho.contains("energiaComprometida = null"))
        assertTrue("cancelar não limpa a rolagem guardada", trecho.contains("rolagemDeMagiaEsperando = null"))
    }
}
