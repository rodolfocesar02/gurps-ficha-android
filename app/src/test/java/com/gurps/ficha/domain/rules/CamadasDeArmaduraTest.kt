package com.gurps.ficha.domain.rules

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Combinando e Sobrepondo Armaduras** — MB p.287. Lote EQP-10.
 */
class CamadasDeArmaduraTest {

    private fun peca(nome: String, rd: Int, flexivel: Boolean, ocultavel: Boolean) =
        CamadasDeArmadura.Peca(nome, rd, flexivel, ocultavel)

    private val malha = peca("Cota de Malha", rd = 4, flexivel = true, ocultavel = true)
    private val placas = peca("Armadura de Placas", rd = 6, flexivel = false, ocultavel = false)
    private val couro = peca("Armadura de Couro", rd = 2, flexivel = false, ocultavel = false)

    // ── Uma peça só nunca é problema ───────────────────────────────────

    @Test
    fun `sem sobreposicao nao ha aviso`() {
        listOf(emptyList(), listOf(placas), listOf(malha)).forEach { pecas ->
            val s = CamadasDeArmadura.avaliar(LocalAtaque.TORSO, pecas)
            assertTrue(s.legal)
            assertNull(s.aviso)
            assertFalse(s.sobreposta)
        }
    }

    // ── A regra ────────────────────────────────────────────────────────

    @Test
    fun `malha por dentro da placa e legal`() {
        // A camada de baixo é flexível E ocultável — é o caso que o livro permite.
        val s = CamadasDeArmadura.avaliar(LocalAtaque.TORSO, listOf(placas, malha))
        assertTrue(s.legal)
        assertNull(s.aviso)
        assertEquals(10, s.rdSomada)
    }

    @Test
    fun `duas pecas rigidas nao podem ser sobrepostas`() {
        // 🔴 O que o app fazia: somava 6 + 2 = 8 e não dizia nada.
        val s = CamadasDeArmadura.avaliar(LocalAtaque.TORSO, listOf(placas, couro))
        assertFalse(s.legal)
        assertNotNull(s.aviso)
        assertTrue(s.aviso!!, s.aviso!!.contains("287"))
        // ⚠️ Mas a soma CONTINUA: avisar não é bloquear.
        assertEquals(8, s.rdSomada)
    }

    @Test
    fun `flexivel mas nao ocultavel nao serve de camada de baixo`() {
        // As duas metades do requisito são necessárias. Uma cota flexível que
        // não se esconde sob a roupa não vale.
        val flexivelExposta = peca("Traje Tático", rd = 20, flexivel = true, ocultavel = false)
        val s = CamadasDeArmadura.avaliar(LocalAtaque.TORSO, listOf(placas, flexivelExposta))
        assertFalse(s.legal)
    }

    @Test
    fun `ocultavel mas rigida tambem nao serve`() {
        val rigidaEscondida = peca("Peitoral fino", rd = 3, flexivel = false, ocultavel = true)
        val s = CamadasDeArmadura.avaliar(LocalAtaque.TORSO, listOf(placas, rigidaEscondida))
        assertFalse(s.legal)
    }

    // ── O preço ────────────────────────────────────────────────────────

    @Test
    fun `camada extra fora da cabeca custa menos um na DX`() {
        val s = CamadasDeArmadura.avaliar(LocalAtaque.TORSO, listOf(placas, malha))
        assertEquals(-1, CamadasDeArmadura.penalidadeDeDx(listOf(s)))
    }

    @Test
    fun `na cabeca nao custa DX`() {
        // "em qualquer lugar que não seja a cabeça" (p.287).
        listOf(LocalAtaque.CRANIO, LocalAtaque.ROSTO, LocalAtaque.OLHO).forEach { local ->
            val s = CamadasDeArmadura.avaliar(local, listOf(placas, malha))
            assertEquals("$local cobrou DX", 0, CamadasDeArmadura.penalidadeDeDx(listOf(s)))
        }
    }

    @Test
    fun `a penalidade nao acumula por local`() {
        // ⚠️ O livro chama de cumulativos os redutores de REAÇÃO, no parágrafo
        // logo abaixo, e não diz isso deste. Onde ele é explícito num e silencioso
        // no outro, o silêncio conta: é um -1, não um por local empilhado.
        val tronco = CamadasDeArmadura.avaliar(LocalAtaque.TORSO, listOf(placas, malha))
        val perna = CamadasDeArmadura.avaliar(LocalAtaque.PERNA, listOf(placas, malha))
        val braco = CamadasDeArmadura.avaliar(LocalAtaque.BRACO, listOf(placas, malha))
        assertEquals(-1, CamadasDeArmadura.penalidadeDeDx(listOf(tronco, perna, braco)))
    }

    @Test
    fun `sem pilha nao ha penalidade nem aviso de DX`() {
        val s = CamadasDeArmadura.avaliar(LocalAtaque.TORSO, listOf(placas))
        assertEquals(0, CamadasDeArmadura.penalidadeDeDx(listOf(s)))
        assertNull(CamadasDeArmadura.avisoDeDx(0))
        assertNull(CamadasDeArmadura.avisoDeDxAcessivel(0))
    }

    @Test
    fun `a fala do aviso de DX nao tem sinal cru`() {
        val falado = CamadasDeArmadura.avisoDeDxAcessivel(-1)!!
        assertFalse(falado, RotuloAcessivel.temSinalCru(falado))
        assertTrue(falado, falado.contains("menos 1"))
    }

    // ── Contra o catálogo de verdade ───────────────────────────────────

    @Test
    fun `o catalogo tem exatamente as pecas ocultaveis que o livro marca`() {
        // ⚠️ A "ocultabilidade" não é um campo do catálogo — vive no TEXTO da
        // nota de rodapé. Um teste com exemplos meus só provaria que a minha
        // regexp casa com a minha frase; este pergunta ao asset de verdade.
        val arq = File("src/main/assets/armaduras.v2.json").takeIf { it.exists() }
            ?: File("app/src/main/assets/armaduras.v2.json")
        assertTrue("nao encontrei ${arq.absolutePath}", arq.exists())

        val itens = JsonParser.parseString(arq.readText(Charsets.UTF_8))
            .asJsonObject.getAsJsonArray("items")

        var ocultaveis = 0
        var candidatasACamadaDeBaixo = 0
        itens.forEach { el ->
            val o = el.asJsonObject
            val notas = o.getAsJsonArray("observacoesDetalhadas")?.map { it.asString } ?: emptyList()
            val oculta = CamadasDeArmadura.ehOcultavel(notas)
            if (oculta) ocultaveis++
            val flexivel = o.get("rdRaw")?.asString.orEmpty().contains("*")
            if (oculta && flexivel) candidatasACamadaDeBaixo++
        }

        // Só uma parte pequena do catálogo pode ser camada de baixo — é o que
        // torna a regra uma restrição de verdade, e não decoração.
        assertTrue("nenhuma peça ocultável reconhecida", ocultaveis > 0)
        assertTrue(
            "candidatas demais ($candidatasACamadaDeBaixo de ${itens.size()}) — a leitura da nota está frouxa",
            candidatasACamadaDeBaixo < itens.size() / 3
        )
        assertEquals(
            "toda peça ocultável do catálogo é flexível; se isso mudou, a regra precisa ser revista",
            ocultaveis, candidatasACamadaDeBaixo
        )
    }
}
