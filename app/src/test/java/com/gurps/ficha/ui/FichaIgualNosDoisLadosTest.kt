package com.gurps.ficha.ui

import com.gurps.ficha.domain.rules.CartaoDoItem
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.TipoEquipamento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Lote EQP-7** — a mesma peça não pode ter duas caras.
 *
 * ## O defeito que este arquivo tranca
 *
 * Depois do EQP-6, a *Túnica* aparecia com ficha completa ao **escolher** e como
 * formulário pelado ao **editar**. Duas causas somadas:
 *
 * 1. O editor perguntava a ficha só para a **arma** — armadura e escudo caíam em
 *    `null`.
 * 2. Mesmo com a ficha, ele a desenhava do **seu jeito**: lista chapada, sem os
 *    cartões e sem os títulos de bloco.
 *
 * ⚠️ É a terceira vez que este defeito aparece (LAYOUT-7 na arma, EQP-2 no cartão
 * de armadura, agora no editor). Sempre o mesmo formato: **o conserto anterior
 * foi feito para um tipo de item, não para "um item"**.
 */
class FichaIgualNosDoisLadosTest {

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    private val tab by lazy { fonte("com/gurps/ficha/ui/TabEquipamentos.kt") }
    private val editor by lazy { fonte("com/gurps/ficha/ui/DialogsCommon.kt") }
    private val card by lazy { fonte("com/gurps/ficha/ui/features/equipamento/CardDetalheArma.kt") }
    private val vm by lazy { fonte("com/gurps/ficha/viewmodel/FichaViewModel.kt") }

    @Test
    fun `o editor pede a ficha de qualquer item, nao so da arma`() {
        assertFalse(
            "o editor voltou a perguntar so pela arma — armadura e escudo caem em null",
            tab.contains("fichaTecnica = viewModel.armaDoCatalogoPara(equipamento)")
        )
        assertTrue(
            "o editor nao usa o ponto unico de entrada",
            tab.contains("fichaTecnica = viewModel.fichaTecnicaDoItem(equipamento)")
        )
    }

    @Test
    fun `o when por tipo mora num lugar so`() {
        // Espalhar o `when` pelas telas e como a divergencia recomeca.
        assertTrue(vm.contains("fun fichaTecnicaDoItem("))
        assertTrue("o ponto unico nao cobre a armadura", vm.contains("armaduraDoCatalogoPara("))
        assertTrue("o ponto unico nao cobre o escudo", vm.contains("escudoDoCatalogoPara("))
    }

    @Test
    fun `card e editor desenham a ficha com o mesmo composable`() {
        assertTrue("o card nao usa o desenho compartilhado", card.contains("fun ColumnScope.BlocosDaFicha("))
        // Sem casar a chamada inteira: o editor passa `mostrarEditaveis = false`
        // desde o EQP-8, e o que importa aqui e que os dois usem O MESMO desenho.
        assertTrue("o card nao chama o desenho compartilhado", card.contains("BlocosDaFicha(ficha"))
        assertTrue("o editor nao chama o desenho compartilhado", editor.contains("BlocosDaFicha(ficha"))
        assertFalse(
            "o editor voltou a ter desenho proprio da ficha",
            editor.contains("(ficha.destaques + ficha.detalhes)")
        )
    }

    // ── Lote EQP-8: os campos que faltavam, e o bloco que sobrava ──────

    @Test
    fun `a armadura tem campo de RD`() {
        // 🔴 Era o unico numero da armadura que o jogador nao conseguia mexer.
        // Uma peca encantada de +1 RD so podia ser anotada na NOTA -- e o
        // combate nao le nota, le o campo.
        assertTrue("nao ha campo de RD", editor.contains("value = rdArmadura"))
        assertTrue("o RD nao e gravado no salvar", editor.contains("armaduraRd = if (ehArmadura)"))
    }

    @Test
    fun `o escudo tem campo de BD`() {
        assertTrue("nao ha campo de BD", editor.contains("value = bdEscudo"))
        assertTrue("o BD nao e gravado no salvar", editor.contains("bonusDefesa = if (ehEscudo)"))
    }

    @Test
    fun `armadura nao mostra automacao de combate`() {
        // Armadura nao ataca. E pior que inutil: preencher o Dano virava o tipo
        // para ARMA e a peca sumia da secao de armaduras sem aviso.
        assertTrue(editor.contains("val temAutomacaoDeCombate = !ehArmadura"))
        assertTrue(editor.contains("if (temAutomacaoDeCombate) {"))
    }

    @Test
    fun `armadura nunca vira arma ao salvar`() {
        val i = editor.indexOf("val tipoFinal = when {")
        val trecho = editor.substring(i, i + 400)
        val posArmadura = trecho.indexOf("ehArmadura ->")
        val posDano = trecho.indexOf("danoFinal != null ->")
        assertTrue("a armadura nao e verificada antes do dano", posArmadura in 0 until posDano)
    }

    @Test
    fun `o editor esconde da ficha o que tem campo`() {
        // Sem isto o peso aparece duas vezes na mesma tela, com valores
        // diferentes -- o defeito que o usuario fotografou em 12/08.
        assertTrue(
            "o editor nao esta escondendo as linhas editaveis",
            editor.contains("BlocosDaFicha(ficha, mostrarEditaveis = false)")
        )
        assertTrue(
            "o card de selecao deixou de mostrar tudo",
            card.contains("mostrarEditaveis: Boolean = true")
        )
    }

    // ── O cabeçalho automático no campo editável ───────────────────────

    @Test
    fun `o editor nao mostra o cabecalho que o app mesmo escreveu`() {
        // A ficha logo acima ja diz Local e RD. Repetir num campo EDITAVEL
        // convida a corrigir ali o que so muda no campo de verdade.
        val botas = Equipamento(
            nome = "Botas (pés)",
            notas = "Local: pés; RD: 2*\n[1] Pode ser ocultado como ou sob uma peça de roupa.",
            tipo = TipoEquipamento.ARMADURA,
            armaduraLocal = "pes",
            armaduraRd = "2*"
        )
        val texto = CartaoDoItem.notasParaEditar(botas)
        assertFalse(texto, texto.contains("Local: pés; RD:"))
        assertTrue("perdeu a nota do livro", texto.contains("Pode ser ocultado"))
    }

    @Test
    fun `quebras de linha sobrevivem no editor`() {
        // No cartao as notas viram uma linha so com " · ". No editor, nao:
        // ali o texto e para ser lido e mexido.
        val eq = Equipamento(
            nome = "X",
            notas = "Local: tronco; RD: 2\nprimeira\nsegunda",
            tipo = TipoEquipamento.ARMADURA,
            armaduraLocal = "tronco",
            armaduraRd = "2"
        )
        assertEquals("primeira\nsegunda", CartaoDoItem.notasParaEditar(eq))
    }

    @Test
    fun `ficha antiga sem os campos mantem o cabecalho intacto`() {
        // 🔴 Sem `armaduraRd`, aquela frase E o RD. O editor grava o que mostra,
        // entao esconde-la ali apagaria o dado de verdade.
        val antiga = Equipamento(
            nome = "Túnica",
            notas = "Local: tronco; RD: 2*\nBoa qualidade",
            tipo = TipoEquipamento.ARMADURA,
            armaduraLocal = null,
            armaduraRd = null
        )
        val texto = CartaoDoItem.notasParaEditar(antiga)
        assertTrue("apagou o unico lugar onde o RD existia", texto.contains("RD: 2*"))
        assertEquals(antiga.notas, texto)
    }

    @Test
    fun `item comum passa pelas notas sem mexer`() {
        val mascara = Equipamento(
            nome = "Mascara",
            notas = "Local: onde eu quiser; e uma nota minha",
            tipo = TipoEquipamento.GERAL
        )
        assertEquals(mascara.notas, CartaoDoItem.notasParaEditar(mascara))
    }
}
