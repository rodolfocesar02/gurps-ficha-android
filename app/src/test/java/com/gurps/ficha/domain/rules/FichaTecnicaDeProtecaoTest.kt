package com.gurps.ficha.domain.rules

import com.gurps.ficha.model.ArmaduraCatalogoItem
import com.gurps.ficha.model.ArmaduraComponenteCatalogo
import com.gurps.ficha.model.EscudoCatalogoItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lote EQP-6** — a ficha técnica da armadura e do escudo.
 *
 * O pedido do usuário foi *"no mesmo padrão de como ficou as armas"*, e o padrão
 * da arma não é o desenho: é a **divisão** entre o que se olha no meio da jogada
 * e o que se olha na hora de comprar. É isso que estes testes cobram.
 */
class FichaTecnicaDeProtecaoTest {

    private fun armadura(
        nome: String = "Túnica",
        local: String = "tronco",
        rd: String = "2",
        nt: Int? = 1,
        peso: Float? = 3f,
        custo: Float? = 60f,
        componentes: List<ArmaduraComponenteCatalogo> = emptyList()
    ) = ArmaduraCatalogoItem(
        id = "x", nome = nome, nt = nt, local = local, rd = rd,
        custoBase = custo, pesoBaseKg = peso, observacoes = "",
        componentes = componentes
    )

    private fun escudo(
        nome: String = "Escudo Grande",
        db: Int = 3,
        nt: Int? = 1,
        custo: Float? = 90f,
        peso: Float? = 12.5f,
        stMinimo: Int? = null,
        observacoes: String = "[2, 4, 6]",
        rdDoEscudo: Int? = 9,
        pv: Int? = 60,
        cl: Int? = 4
    ) = EscudoCatalogoItem(
        id = "e", nome = nome, nt = nt, db = db, custo = custo, pesoKg = peso,
        stMinimo = stMinimo, observacoes = observacoes,
        rdDoEscudo = rdDoEscudo, pv = pv, cl = cl
    )

    private fun textoDe(linhas: List<FichaDeEquipamento.Linha>) =
        linhas.joinToString(" | ") { it.descricaoAcessivel }

    // ── A forma compartilhada ──────────────────────────────────────────

    @Test
    fun `nem armadura nem escudo tem modo de ataque`() {
        // O bloco existe na forma porque a arma precisa; para quem não ataca ele
        // vem vazio e o card simplesmente não o desenha.
        assertTrue(FichaTecnicaDaArmadura.de(armadura()).modos.isEmpty())
        assertTrue(FichaTecnicaDoEscudo.de(escudo(), 10).modos.isEmpty())
    }

    @Test
    fun `os dois blocos da arma existem nos tres itens`() {
        // A "divisão" que o usuário chamou de padrão: sempre há o que olhar no
        // meio da jogada e o que olhar na hora de comprar.
        listOf(
            FichaTecnicaDaArmadura.de(armadura()),
            FichaTecnicaDoEscudo.de(escudo(), 10)
        ).forEach { ficha ->
            assertTrue("${ficha.nome} sem destaques", ficha.destaques.isNotEmpty())
            assertTrue("${ficha.nome} sem detalhes de compra", ficha.detalhes.isNotEmpty())
            assertTrue("${ficha.nome} sem nome", ficha.nome.isNotBlank())
            assertTrue("${ficha.nome} sem subtítulo", ficha.subtitulo.isNotBlank())
        }
    }

    // ── Armadura ───────────────────────────────────────────────────────

    @Test
    fun `a RD e o local sao o que se olha no meio da jogada`() {
        val ficha = FichaTecnicaDaArmadura.de(armadura(rd = "2", local = "tronco"))
        val destaque = textoDe(ficha.destaques)
        assertTrue(destaque, destaque.contains("RD: 2"))
        assertTrue(destaque, destaque.contains("tronco"))
    }

    @Test
    fun `o local torto do catalogo sai consertado na ficha`() {
        val ficha = FichaTecnicaDaArmadura.de(armadura(local = "crnio, pescoo"))
        val tudo = ficha.subtitulo + " " + textoDe(ficha.destaques)
        assertFalse(tudo, tudo.contains("crnio"))
        assertFalse(tudo, tudo.contains("pescoo"))
        assertTrue(tudo, tudo.contains("cranio"))
    }

    @Test
    fun `o asterisco e a barra da RD ganham explicacao`() {
        // ⚠️ Não são enfeite: são as notas [1] e [3] da p.286, e mudam quanto
        // dano passa. `4/2*` sem explicação é escolher armadura no escuro.
        assertNotNull(FichaTecnicaDaArmadura.explicarRd("2*"))
        assertNotNull(FichaTecnicaDaArmadura.explicarRd("4/2"))
        val ambos = FichaTecnicaDaArmadura.explicarRd("4/2*")
        assertNotNull(ambos)
        assertTrue(ambos!!, ambos.contains("dividida"))
        assertTrue(ambos, ambos.contains("flexível"))
        // RD comum não ganha ruído.
        assertEquals(null, FichaTecnicaDaArmadura.explicarRd("5"))
    }

    @Test
    fun `peca composta mostra a RD de cada parte`() {
        // Esconder isso faria a ficha prometer uma RD que ela não dá em todo lugar.
        val ficha = FichaTecnicaDaArmadura.de(
            armadura(
                local = "cranio",
                rd = "4",
                componentes = listOf(
                    ArmaduraComponenteCatalogo(local = "pescoo", rd = "2", custoBase = 20f, pesoKg = 1f)
                )
            )
        )
        val destaque = textoDe(ficha.destaques)
        assertTrue(destaque, destaque.contains("RD 2"))
        assertFalse("o local do componente saiu torto: $destaque", destaque.contains("pescoo"))
    }

    // ── Escudo ─────────────────────────────────────────────────────────

    @Test
    fun `o BD e o primeiro destaque do escudo`() {
        val ficha = FichaTecnicaDoEscudo.de(escudo(db = 3), stDoPersonagem = 10)
        assertEquals("BD", ficha.destaques.first().rotulo)
        assertTrue(ficha.destaques.first().valor.contains("3"))
    }

    @Test
    fun `a RD do escudo diz que nao protege o personagem`() {
        // 🔴 A linha mais perigosa da ficha: "RD 9" ao lado de uma armadura de
        // RD 2 convida a somar 9 na defesa — e essa RD protege o ESCUDO.
        val ficha = FichaTecnicaDoEscudo.de(escudo(rdDoEscudo = 9, pv = 60), stDoPersonagem = 10)
        val linha = ficha.detalhes.first { it.rotulo.contains("RD") }
        assertTrue(linha.valor, linha.valor.contains("9"))
        assertNotNull(linha.explicacao)
        assertTrue(linha.explicacao!!, linha.explicacao!!.contains("não protege você"))
    }

    @Test
    fun `a mao ocupada aparece, porque muda a jogada`() {
        val ficha = FichaTecnicaDoEscudo.de(escudo(), stDoPersonagem = 10)
        val texto = textoDe(ficha.destaques)
        assertTrue(texto, texto.contains("duas mãos"))
    }

    @Test
    fun `o escudo avisa quando falta ST, e cala quando nao falta`() {
        val pesado = FichaTecnicaDoEscudo.de(escudo(stMinimo = 12), stDoPersonagem = 9)
        assertTrue(textoDe(pesado.destaques).contains("Falta ST 3"))

        val leve = FichaTecnicaDoEscudo.de(escudo(stMinimo = 5), stDoPersonagem = 9)
        assertFalse(textoDe(leve.destaques).contains("Falta ST"))

        val semSt = FichaTecnicaDoEscudo.de(escudo(stMinimo = null), stDoPersonagem = 9)
        assertFalse(textoDe(semSt.destaques).contains("Falta ST"))
    }

    @Test
    fun `as notas do escudo entram por extenso na ficha`() {
        val ficha = FichaTecnicaDoEscudo.de(escudo(observacoes = "[2, 4, 6]"), stDoPersonagem = 10)
        assertEquals(3, ficha.observacoes.size)
        ficha.observacoes.forEach {
            assertTrue("nota sem texto: $it", it.substringAfter("]").trim().length > 10)
        }
    }

    // ── Formatação ─────────────────────────────────────────────────────

    @Test
    fun `dinheiro grande nao perde o ponto de milhar`() {
        // Uma armadura de $10000 sem o ponto lê como $1000.
        assertEquals("$10.000", FichaDeEquipamento.formatarDinheiro(10000f))
        assertEquals("$90", FichaDeEquipamento.formatarDinheiro(90f))
    }

    @Test
    fun `peso usa virgula, como o livro em portugues`() {
        assertEquals("0,125", FichaDeEquipamento.formatarKg(0.125f))
        assertEquals("12", FichaDeEquipamento.formatarKg(12f))
    }

    @Test
    fun `campo ausente e travessao, nunca zero`() {
        // Zero é um dado; "não sei" é outro.
        val ficha = FichaTecnicaDaArmadura.de(armadura(nt = null, peso = null, custo = null))
        val compra = ficha.detalhes.map { it.valor }
        assertTrue(compra.toString(), compra.all { it == FichaDeEquipamento.AUSENTE })
        assertFalse(compra.contains("0"))
    }
}
