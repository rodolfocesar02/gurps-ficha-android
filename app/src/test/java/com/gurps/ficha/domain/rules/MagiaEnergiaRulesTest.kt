package com.gurps.ficha.domain.rules

import com.google.gson.JsonParser
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Lote MAGIA-E1** — o custo em energia das mágicas.
 *
 * ## 🔴 Os três buracos que este arquivo tranca
 *
 * A versão antiga tinha dez linhas, e nenhum dos defeitos aparecia como erro:
 *
 * 1. `"1 a 3"` virava **1** — a faixa era descartada, e a *Cura Superficial*
 *    nunca curava mais que 1 PV.
 * 2. `"Varia"` virava **nulo**, quem chamava fazia `?: return`, e **130 mágicas**
 *    saíam de graça.
 * 3. O resultado da rolagem não mexia no gasto, contra a p.236.
 *
 * ⚠️ Por isso o teste central não é de exemplos escolhidos a dedo: é a
 * **varredura das 879 mágicas reais**, exigindo que nenhuma caia num caso que o
 * app não sabe representar.
 */
class MagiaEnergiaRulesTest {

    private fun catalogo(): List<Pair<String, String>>? {
        val arq = listOf("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")
            .map { File(it) }.firstOrNull { it.exists() } ?: return null
        val raiz = JsonParser.parseString(arq.readText())
        val itens = if (raiz.isJsonArray) raiz.asJsonArray else raiz.asJsonObject.entrySet()
            .firstNotNullOf { if (it.value.isJsonArray) it.value.asJsonArray else null }
        return itens.map { el ->
            val o = el.asJsonObject
            (o.get("nome")?.asString ?: "") to (o.get("energia")?.asString ?: "")
        }
    }

    // ==================================================================
    // 1. 🔴 A faixa
    // ==================================================================

    @Test
    fun `🔴 Cura Superficial custa de 1 a 3, nao 1`() {
        // O bug que o usuário viu: "rolei Cura Superficial, 1 a 3 de fadiga, era
        // pra ter opção de quanto ia gastar". O que se gasta é o que se cura,
        // então travar no mínimo travava a cura em 1 PV.
        val c = MagiaEnergiaRules.parseCusto("1 a 3")
        assertEquals(1, c.minimo)
        assertEquals(3, c.maximo)
        assertTrue("é faixa", c.variavel)
        assertTrue("o app tem que perguntar", c.precisaEscolher)
        assertEquals(null, c.fixo)
    }

    @Test
    fun `todas as faixas do catalogo sao lidas como faixa`() {
        listOf("1 a 3" to (1 to 3), "1 a 4" to (1 to 4), "1 a 5" to (1 to 5),
            "2 a 6" to (2 to 6), "10 a 30" to (10 to 30), "10 a 50" to (10 to 50))
            .forEach { (texto, esperado) ->
                val c = MagiaEnergiaRules.parseCusto(texto)
                assertEquals(texto, esperado.first, c.minimo)
                assertEquals(texto, esperado.second, c.maximo)
            }
    }

    @Test
    fun `⚠️ a faixa sobrevive ao sufixo do catalogo`() {
        // "1 a 5/I" e "1 a 3/M" existem no arquivo: a barra separa operar de
        // manter, e o sufixo é nota de rodapé.
        assertEquals(5, MagiaEnergiaRules.parseCusto("1 a 5/I").maximo)
        assertEquals(3, MagiaEnergiaRules.parseCusto("1 a 3/M").maximo)
    }

    // ==================================================================
    // 2. 🔴 O desconhecido é representado, não omitido
    // ==================================================================

    @Test
    fun `🔴 Varia nao vira zero nem nulo — vira "precisa perguntar"`() {
        // Devolver nulo era o buraco: quem chamava escrevia `?: return` e a
        // mágica saía de graça, sem diálogo e sem aviso. São 130 mágicas.
        val c = MagiaEnergiaRules.parseCusto("Varia")
        assertTrue(c.desconhecido)
        assertTrue("o app tem que perguntar", c.precisaEscolher)
        assertEquals(null, c.fixo)
    }

    @Test
    fun `Especial e vazio tambem sao desconhecidos`() {
        assertTrue(MagiaEnergiaRules.parseCusto("Especial").desconhecido)
        assertTrue(MagiaEnergiaRules.parseCusto("").desconhecido)
        assertTrue(MagiaEnergiaRules.parseCusto(null).desconhecido)
    }

    @Test
    fun `⚠️ faixa aberta guarda o piso que se sabe`() {
        // "1 a AM#" — sabemos o mínimo, não o teto. Melhor guardar o 1 do que
        // jogar fora a única informação que existe.
        val c = MagiaEnergiaRules.parseCusto("1 a AM#")
        assertTrue(c.desconhecido)
        assertEquals(1, c.minimo)
        assertTrue(c.comoTexto.contains("a partir de 1"))
    }

    // ==================================================================
    // 3. O custo fixo e o operar/manter
    // ==================================================================

    @Test
    fun `custo fixo nao pede escolha`() {
        val c = MagiaEnergiaRules.parseCusto("4")
        assertEquals(4, c.fixo)
        assertFalse(c.precisaEscolher)
    }

    @Test
    fun `⚠️ a barra separa OPERAR de MANTER — vale o de operar`() {
        // "04/02" = 4 para operar, 2 para manter. São mais de 200 mágicas nesse
        // formato; ler o número errado dobraria ou dividiria o gasto.
        assertEquals(4, MagiaEnergiaRules.parseCusto("04/02").fixo)
        assertEquals(3, MagiaEnergiaRules.parseCusto("03/01").fixo)
        assertEquals(2, MagiaEnergiaRules.parseCusto("2/M").fixo)
        assertEquals(3, MagiaEnergiaRules.parseCusto("3#").fixo)
    }

    // ==================================================================
    // 4. 🔴 A varredura do catálogo de verdade
    // ==================================================================

    @Test
    fun `🔴 as 879 magias reais passam pelo parse sem sumir`() {
        val magias = catalogo() ?: return
        assertTrue("o catálogo encolheu", magias.size >= 800)
        val comFaixa = mutableListOf<String>()
        val desconhecidas = mutableListOf<String>()
        magias.forEach { (nome, energia) ->
            val c = MagiaEnergiaRules.parseCusto(energia)
            if (c.variavel) comFaixa += nome
            if (c.desconhecido) desconhecidas += nome
            // A invariante que importa: toda mágica ou tem número, ou é
            // declaradamente desconhecida. Nenhuma pode passar como "zero".
            assertTrue(
                "'$nome' (energia '$energia') virou custo zero sem ser desconhecida",
                c.desconhecido || c.minimo > 0 || energia.trim().startsWith("0")
            )
        }
        assertTrue("nenhuma faixa reconhecida: o parse quebrou", comFaixa.size >= 30)
        assertTrue("nenhuma desconhecida: virou zero em silêncio", desconhecidas.size >= 100)
    }

    @Test
    fun `⚠️ o parse nunca devolve faixa invertida`() {
        val magias = catalogo() ?: return
        magias.forEach { (nome, energia) ->
            val c = MagiaEnergiaRules.parseCusto(energia)
            assertTrue("'$nome': mínimo ${c.minimo} > máximo ${c.maximo}", c.minimo <= c.maximo)
        }
    }

    // ==================================================================
    // 5. 🔴 O resultado da rolagem muda o gasto (MB p.236)
    // ==================================================================

    @Test
    fun `🔴 sucesso decisivo nao gasta NADA`() {
        // "não há gasto de energia quando o personagem obtém um sucesso decisivo"
        assertEquals(0, MagiaEnergiaRules.energiaGasta(5, MagiaEnergiaRules.Resultado.SUCESSO_DECISIVO))
    }

    @Test
    fun `🔴 fracasso perde 1 ponto, nao o custo cheio`() {
        // "O operador perderá um ponto de energia se houver custo em energia
        // para o uso bem-sucedido."
        assertEquals(1, MagiaEnergiaRules.energiaGasta(5, MagiaEnergiaRules.Resultado.FRACASSO))
        assertEquals(1, MagiaEnergiaRules.energiaGasta(30, MagiaEnergiaRules.Resultado.FRACASSO))
    }

    @Test
    fun `⚠️ magia de informacao paga tudo mesmo fracassando`() {
        assertEquals(
            5,
            MagiaEnergiaRules.energiaGasta(5, MagiaEnergiaRules.Resultado.FRACASSO, ehMagiaDeInformacao = true)
        )
    }

    @Test
    fun `falha critica gasta o custo total`() {
        assertEquals(5, MagiaEnergiaRules.energiaGasta(5, MagiaEnergiaRules.Resultado.FALHA_CRITICA))
    }

    @Test
    fun `sucesso comum paga o que foi comprometido`() {
        assertEquals(3, MagiaEnergiaRules.energiaGasta(3, MagiaEnergiaRules.Resultado.SUCESSO))
    }

    @Test
    fun `magia sem custo nao gasta em resultado nenhum`() {
        MagiaEnergiaRules.Resultado.entries.forEach {
            assertEquals("$it", 0, MagiaEnergiaRules.energiaGasta(0, it))
        }
    }

    // ==================================================================
    // 5b. 🔴 A exceção da mágica de informação
    // ==================================================================

    @Test
    fun `🔴 a classe do catalogo identifica a magia de informacao`() {
        // São 59 no catálogo, e a classe vem escrita — não precisa de lista à
        // mão. Casa por prefixo porque existe "Informação/R-Vont".
        assertTrue(MagiaEnergiaRules.ehMagiaDeInformacao("Informação"))
        assertTrue(MagiaEnergiaRules.ehMagiaDeInformacao("Informação/R-Vont"))
        assertFalse(MagiaEnergiaRules.ehMagiaDeInformacao("Comum"))
        assertFalse(MagiaEnergiaRules.ehMagiaDeInformacao(null))
    }

    @Test
    fun `⚠️ o catalogo tem magias de informacao de verdade`() {
        // Se a classe mudar de nome no JSON, a exceção do fracasso some em
        // silêncio e essas 59 mágicas passam a pagar 1 onde deviam pagar tudo.
        val arq = listOf("src/main/assets/magias2versao.json", "app/src/main/assets/magias2versao.json")
            .map { File(it) }.firstOrNull { it.exists() } ?: return
        val raiz = JsonParser.parseString(arq.readText())
        val itens = if (raiz.isJsonArray) raiz.asJsonArray else raiz.asJsonObject.entrySet()
            .firstNotNullOf { if (it.value.isJsonArray) it.value.asJsonArray else null }
        val info = itens.count {
            MagiaEnergiaRules.ehMagiaDeInformacao(it.asJsonObject.get("classe")?.asString)
        }
        assertTrue("nenhuma mágica de informação encontrada: a classe mudou?", info >= 40)
    }

    // ==================================================================
    // 6. O desconto por NH
    // ==================================================================

    @Test
    fun `o desconto por NH segue a tabela da p 238`() {
        assertEquals(0, MagiaEnergiaRules.reducaoPorNh(14))
        assertEquals(1, MagiaEnergiaRules.reducaoPorNh(15))
        assertEquals(1, MagiaEnergiaRules.reducaoPorNh(19))
        assertEquals(2, MagiaEnergiaRules.reducaoPorNh(20))
        assertEquals(3, MagiaEnergiaRules.reducaoPorNh(25))
    }

    @Test
    fun `⚠️ o custo PODE chegar a zero com NH alto`() {
        // O Módulo Básico não dá piso geral de 1. Sabemos porque a mágica Drenar
        // Energia precisa dizer que é EXCEÇÃO ("nunca é reduzido por NH elevado;
        // um mínimo de 1 PF sempre é gasto") — exceção escrita implica regra
        // geral diferente.
        assertEquals(0, MagiaEnergiaRules.custoAjustadoPorNh(1, 15))
        assertEquals(0, MagiaEnergiaRules.custoAjustadoPorNh(2, 20))
        // E nunca fica negativo.
        assertEquals(0, MagiaEnergiaRules.custoAjustadoPorNh(1, 30))
    }
}
