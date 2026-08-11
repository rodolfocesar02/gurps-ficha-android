package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.ArmaduraCatalogoItem
import com.gurps.ficha.model.Equipamento
import com.gurps.ficha.model.EscudoCatalogoItem
import com.gurps.ficha.model.TipoEquipamento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lote EQP-8** — a ficha não pode contradizer o item.
 *
 * ## O teste que faltou no EQP-7
 *
 * Eu cobri *"o editor mostra uma ficha"*. Não cobri *"a ficha descreve **este**
 * item"* — e passou verde com três números errados na tela:
 *
 * | a ficha dizia | o campo logo abaixo dizia |
 * |---|---|
 * | Local: tronco, virilha | a peça é só da **virilha** |
 * | Peso: 3 kg | `1.5` |
 * | Custo: $30 | `15.0` |
 *
 * A *Túnica* do catálogo é uma peça só de `tronco, virilha`, 3 kg e $30; ao
 * escolher os dois locais o app a parte em duas metades. A ficha continuava
 * montada do **catálogo**.
 *
 * ⚠️ Para arma isso nunca apareceu porque uma espada não se parte em duas — mas a
 * brecha era a mesma: peso e custo editados à mão também divergiriam.
 */
class FichaNaoContradizOItemTest {

    /** A Túnica como o `armaduras.v2.json` a traz. */
    private val tunicaDoLivro = ArmaduraCatalogoItem(
        id = "tunica", nome = "Túnica", nt = 1,
        local = "tronco, virilha", rd = "1*",
        custoBase = 30f, pesoBaseKg = 3f, observacoes = ""
    )

    /** A metade da virilha, como o app a grava. */
    private fun metadeDaVirilha(rd: String = "1*") = Equipamento(
        nome = "Túnica (virilha)",
        peso = 1.5f,
        custo = 15f,
        tipo = TipoEquipamento.ARMADURA,
        armaduraLocal = "virilha",
        armaduraRd = rd,
        armaduraCatalogoId = "tunica"
    )

    private fun valorDe(ficha: FichaDeEquipamento.Ficha, rotulo: String): String =
        (ficha.destaques + ficha.detalhes).first { it.rotulo == rotulo }.valor

    // ── A invariante ───────────────────────────────────────────────────

    @Test
    fun `nenhum numero da ficha contradiz o campo do item`() {
        val peca = metadeDaVirilha()
        val ficha = FichaTecnicaDaArmadura.de(tunicaDoLivro, peca)

        assertEquals("1,5 kg", valorDe(ficha, "Peso"))
        assertEquals("$15", valorDe(ficha, "Custo"))
        assertEquals("virilha", valorDe(ficha, "Local"))
        assertEquals(peca.rdArmaduraExibicao(), valorDe(ficha, "RD"))
    }

    @Test
    fun `o subtitulo tambem fala da peca`() {
        val ficha = FichaTecnicaDaArmadura.de(tunicaDoLivro, metadeDaVirilha())
        assertTrue(ficha.subtitulo, ficha.subtitulo.contains("virilha"))
        assertFalse("o subtítulo ainda fala da peça inteira", ficha.subtitulo.contains("tronco"))
    }

    @Test
    fun `o RD editado pelo jogador aparece na ficha`() {
        // O caso do pedido: armadura encantada, +1 RD.
        val encantada = metadeDaVirilha(rd = "2*")
        val ficha = FichaTecnicaDaArmadura.de(tunicaDoLivro, encantada)
        assertEquals("2*", valorDe(ficha, "RD"))
        // E a explicação acompanha o valor novo, não o do livro.
        val linha = (ficha.destaques).first { it.rotulo == "RD" }
        assertTrue(linha.explicacao.orEmpty(), linha.explicacao.orEmpty().contains("flexível"))
    }

    @Test
    fun `sem peca a ficha continua sendo a do catalogo`() {
        // É o caso da LISTA DE ESCOLHA: ali ainda não existe peça nenhuma.
        val ficha = FichaTecnicaDaArmadura.de(tunicaDoLivro)
        assertEquals("3 kg", valorDe(ficha, "Peso"))
        assertEquals("$30", valorDe(ficha, "Custo"))
        assertEquals("tronco, virilha", valorDe(ficha, "Local"))
    }

    @Test
    fun `o que so o catalogo sabe continua vindo dele`() {
        val ficha = FichaTecnicaDaArmadura.de(tunicaDoLivro, metadeDaVirilha())
        // O NT não está gravado na peça — e não deveria sumir por causa disso.
        assertEquals("1", valorDe(ficha, "NT"))
    }

    // ── O escudo ───────────────────────────────────────────────────────

    @Test
    fun `o escudo tambem fala da peca`() {
        val doLivro = EscudoCatalogoItem(
            id = "escudo_grande", nome = "Escudo Grande", nt = 1, db = 3,
            custo = 90f, pesoKg = 12.5f, stMinimo = null, observacoes = "[2, 4, 6]"
        )
        val meu = Equipamento(
            nome = "Escudo Grande",
            peso = 10f, custo = 200f,
            tipo = TipoEquipamento.ESCUDO,
            bonusDefesa = 4,          // encantado: +1 BD
            escudoCatalogoId = "escudo_grande"
        )
        val ficha = FichaTecnicaDoEscudo.de(doLivro, stDoPersonagem = 10, peca = meu)
        assertEquals("+4", valorDe(ficha, "BD"))
        assertEquals("10 kg", valorDe(ficha, "Peso"))
        assertEquals("$200", valorDe(ficha, "Custo"))
    }

    // ── O marcador de "tem campo no editor" ────────────────────────────

    @Test
    fun `tudo que tem campo esta marcado como editavel`() {
        val ficha = FichaTecnicaDaArmadura.de(tunicaDoLivro, metadeDaVirilha())
        val comCampo = setOf("RD", "Peso", "Custo")
        (ficha.destaques + ficha.detalhes).forEach { linha ->
            if (linha.rotulo in comCampo) {
                assertTrue("'${linha.rotulo}' tem campo e não está marcada", linha.editavel)
            } else {
                assertFalse("'${linha.rotulo}' não tem campo e está marcada", linha.editavel)
            }
        }
    }

    @Test
    fun `no editor sobra o que nao tem campo`() {
        // É o que `BlocosDaFicha(mostrarEditaveis = false)` desenha.
        val ficha = FichaTecnicaDaArmadura.de(tunicaDoLivro, metadeDaVirilha())
        val noEditor = (ficha.destaques + ficha.detalhes).filterNot { it.editavel }
        val rotulos = noEditor.map { it.rotulo }

        assertFalse("o peso apareceria duas vezes na mesma tela", rotulos.contains("Peso"))
        assertFalse("o custo apareceria duas vezes", rotulos.contains("Custo"))
        assertFalse("o RD apareceria duas vezes", rotulos.contains("RD"))
        // O que não tem campo TEM de continuar: senão a ficha vira uma casca.
        assertTrue("o Local sumiu do editor e não há campo para ele", rotulos.contains("Local"))
        assertTrue(rotulos.contains("NT"))
    }
}
