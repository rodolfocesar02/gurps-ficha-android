package com.gurps.ficha.domain.rules

import com.google.gson.JsonParser
import com.gurps.ficha.domain.rules.traits.ControleRule
import com.gurps.ficha.domain.rules.traits.CriarRule
import com.gurps.ficha.domain.rules.traits.ElementoRuleBase
import com.gurps.ficha.domain.rules.traits.TracoSelecionado
import com.gurps.ficha.domain.rules.traits.regraDeElementoDe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Controle e Criar: faixa do elemento x niveis** — Lote POD-21.
 *
 * 🔴 Achado pelo usuario na tela: *"a vantagem e por nivel, porem esta como
 * variavel, tenho que subir ponto a ponto e nao 20/15/10!"*
 *
 * As duas estavam no catalogo como `costKind: "special"`, que o app traduz para
 * custo variavel. No livro elas sao duas escolhas encadeadas -- a mesma forma
 * que a ST Bracal ja tinha resolvido neste projeto.
 */
class ElementoCustoTest {

    private fun asset(nome: String): File {
        val direto = File("src/main/assets/$nome")
        val f = if (direto.exists()) direto else File("app/src/main/assets/$nome")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f
    }

    // ⚠️ `VantagemSelecionada` de verdade, e não um dublê: é ela que atravessa
    // a ficha, e é nela que o `metadados` precisa chegar.
    private fun traco(id: String, nivel: Int, faixa: String?): TracoSelecionado =
        com.gurps.ficha.model.VantagemSelecionada(
            definicaoId = id,
            nome = id,
            nivel = nivel,
            tipoCusto = com.gurps.ficha.model.TipoCusto.ESCOLHA,
            metadados = faixa?.let { mapOf(ElementoRuleBase.CHAVE_CATEGORIA to it) }
        )

    @Test
    fun `Controle cobra o preco da faixa VEZES os niveis`() {
        val r = ControleRule()
        // "Comum ... 20 pontos/nivel" (p.90). Nivel 3 custa 60, nao 20.
        assertEquals(60, r.calculateCost(traco(ControleRule.ID, 3, "Comum"), emptyList()))
        assertEquals(45, r.calculateCost(traco(ControleRule.ID, 3, "Ocasional"), emptyList()))
        assertEquals(30, r.calculateCost(traco(ControleRule.ID, 3, "Raro"), emptyList()))
    }

    @Test
    fun `Criar tem QUATRO faixas, e a de 5 pontos existe`() {
        // 🔴 O catalogo dizia "10, 20 ou 40/nivel" e o Item Especifico
        // (5 pontos/nivel) tinha ficado de fora. Achado ao ler a secao inteira
        // para escrever a regra -- eu tinha parado onde a resposta apareceu.
        val r = CriarRule()
        assertEquals(4, r.faixas.size)
        assertEquals(listOf(40, 20, 10, 5), r.faixas.map { it.custoPorNivel })
        // ⚠️ Os NOMES tambem sao cobrados, e nao so os precos. Sem isto o teste
        // fica cego: `faixaDe` cai na faixa mais barata quando o nome nao bate,
        // entao renomear "Item Especifico" continuava dando 5 x 2 = 10 e o
        // teste passava. Foi a sonda que mostrou -- ela NAO ficou vermelha.
        assertEquals(
            listOf("Categoria Ampla", "Categoria Média", "Categoria Restrita", "Item Específico"),
            r.faixas.map { it.nome }
        )
        assertEquals(10, r.calculateCost(traco(CriarRule.ID, 2, "Item Específico"), emptyList()))
        assertEquals(80, r.calculateCost(traco(CriarRule.ID, 2, "Categoria Ampla"), emptyList()))
    }

    @Test
    fun `ficha antiga sem a faixa cai na mais barata, e nao quebra`() {
        // ⚠️ Errar para BAIXO e o lado seguro: um custo inflado sem o jogador
        // pedir seria pior do que um modesto que ele pode corrigir.
        val r = ControleRule()
        assertEquals(10, r.calculateCost(traco(ControleRule.ID, 1, null), emptyList()))
        assertEquals(10, r.calculateCost(traco(ControleRule.ID, 1, "Faixa Que Sumiu"), emptyList()))
    }

    @Test
    fun `nivel zero ou negativo ainda cobra um nivel`() {
        val r = CriarRule()
        assertEquals(40, r.calculateCost(traco(CriarRule.ID, 0, "Categoria Ampla"), emptyList()))
    }

    @Test
    fun `o catalogo deixou de dizer custo variavel`() {
        // A fiacao: sem `choice` no asset, o dialogo nunca chega no ElementoConfig.
        val json = JsonParser.parseString(asset("vantagens.v3.json").readText(Charsets.UTF_8))
        var vistos = 0
        json.asJsonArray.forEach { e ->
            val o = e.asJsonObject
            val id = o.get("id")?.asString ?: return@forEach
            if (id != ControleRule.ID && id != CriarRule.ID) return@forEach
            vistos++
            assertEquals(
                "'$id' voltou a ser custo variavel",
                "choice", o.get("costKind").asString
            )
            val opcoes = o.get("options").asJsonArray.map { it.asInt }
            val daRegra = regraDeElementoDe(id)!!.faixas.map { it.custoPorNivel }
            assertEquals("'$id': o asset e a regra discordam", daRegra, opcoes)
        }
        assertEquals(2, vistos)
    }

    @Test
    fun `so Controle e Criar tem regra de elemento`() {
        assertNotNull(regraDeElementoDe(ControleRule.ID))
        assertNotNull(regraDeElementoDe(CriarRule.ID))
        assertNull(regraDeElementoDe("estatica_poderes"))
        assertNull(regraDeElementoDe("st_bracal"))
    }

    @Test
    fun `a tela pergunta a faixa, e nao so o custo`() {
        val direto = File("src/main/java/com/gurps/ficha/ui/features/traits/VantagemDialogs.kt")
        val f = if (direto.exists()) direto
                else File("app/src/main/java/com/gurps/ficha/ui/features/traits/VantagemDialogs.kt")
        val src = f.readText(Charsets.UTF_8)
        assertTrue("o ElementoConfig nao foi ligado", src.contains("ElementoConfig("))
        assertTrue("a faixa nao vai para os metadados",
            src.contains("ElementoRuleBase.CHAVE_CATEGORIA to faixaElemento"))
        // ⚠️ O defeito era a tela nao perguntar. Regra sem tela nao existe.
        val cfg = File(f.parentFile, "ElementoConfig.kt")
        assertTrue("o ElementoConfig sumiu", cfg.exists())
        val tela = cfg.readText(Charsets.UTF_8)
        assertTrue("a tela nao mostra o preco por nivel", tela.contains("pontos/nível"))
        assertFalse("a tela cravou numeros a mao", tela.contains("20, 15 ou 10"))
    }
}
