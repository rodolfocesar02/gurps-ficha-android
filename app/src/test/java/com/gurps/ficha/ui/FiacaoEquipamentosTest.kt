package com.gurps.ficha.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Lote EQP-3** — a tela chama quem sabe a resposta?
 *
 * ## A camada que faltava
 *
 * Os dois defeitos deste lote são do **mesmo tipo**, e nenhum teste de regra os
 * pegaria porque em ambos a regra estava certa:
 *
 * - `Obs: [1]` — o resolvedor de notas de rodapé existia e funcionava. A ficha
 *   técnica usava, o cartão da arma equipada usava, e a **lista de seleção**
 *   imprimia o campo cru.
 * - `1x | 0.2kg cada | Total: 0.2kg` — o cartão montava a frase à mão em vez de
 *   perguntar a `CartaoDoItem`.
 *
 * Em ambos, o teste da regra fica **verde** e a tela continua errada. Só lendo o
 * código-fonte da `ui/` dá para perguntar *"esta tela chama o resolvedor?"*, e é
 * o que este arquivo faz — mesma técnica de `BotoesPvPfLigadosTest`.
 */
class FiacaoEquipamentosTest {

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    private val tab by lazy { fonte("com/gurps/ficha/ui/TabEquipamentos.kt") }

    @Test
    fun `a lista de armas mostra o texto da nota, nao o numero dela`() {
        assertFalse(
            "a lista voltou a imprimir o campo cru — o jogador ve 'Obs: [1]'",
            tab.contains("\"Obs: \${arma.observacoes}\"")
        )
        assertTrue(
            "a lista de selecao nao esta pedindo as notas ja casadas com o texto",
            tab.contains("observacoesArmaDoCatalogo(")
        )
    }

    @Test
    fun `o cartao pergunta o peso em vez de montar a frase`() {
        assertTrue(
            "o cartao nao usa CartaoDoItem.pesoEQuantidade",
            tab.contains("CartaoDoItem.pesoEQuantidade(")
        )
        assertFalse(
            "o cartao voltou a montar a linha de peso a mao (repete o numero tres vezes)",
            tab.contains("kg cada | ") || tab.contains("Total: \${")
        )
    }

    @Test
    fun `os tres cartoes usam o mesmo desenho`() {
        // 🔴 A armadura tinha o seu proprio, e divergiu sozinha (Lote EQP-2).
        val nomes = Regex("NomeDoItemPadrao\\(").findAll(tab).count()
        val corpos = Regex("CorpoDoItemPadrao\\(").findAll(tab).count()
        // 1 declaracao + 3 usos, para cada um.
        assertTrue("so $nomes usos de NomeDoItemPadrao — algum cartao tem desenho proprio", nomes >= 4)
        assertTrue("so $corpos usos de CorpoDoItemPadrao — algum cartao tem desenho proprio", corpos >= 4)
        assertFalse(
            "voltou um nome de item em bodyLarge — o padrao e o corpo mais 1 sp",
            tab.contains("equipamento.nome, style = MaterialTheme.typography.bodyLarge")
        )
    }
}
