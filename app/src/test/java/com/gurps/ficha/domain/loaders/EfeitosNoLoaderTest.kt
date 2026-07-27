package com.gurps.ficha.domain.loaders

import com.google.gson.JsonParser
import com.gurps.ficha.domain.rules.traits.TipoEfeito
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Garante que o campo `efeitos` SOBREVIVE ao carregamento do catálogo.
 *
 * Este teste existe por causa de um bug real (27/07/2026): o Lote V-1 declarou
 * os efeitos no JSON, o interpretador tinha 16 testes verdes, o validador
 * Python passava — e no aparelho **o bônus simplesmente não aplicava**.
 *
 * A causa foram DUAS perdas silenciosas no caminho. O catálogo não é
 * desserializado direto para a definição final; ele passa por três etapas:
 *
 * ```
 *   JSON -> JsonObject -> asVantagemV3OrNull() -> VantagemV3 -> toLegacy() -> VantagemDefinicao
 *                              ^ perda 1                            ^ perda 2
 * ```
 *
 * O parser monta a classe intermediária **campo a campo** (não por reflexão),
 * e o `toLegacy()` converte **campo a campo** de novo. Campo novo que não seja
 * citado nos dois lugares é descartado sem erro, sem aviso, sem nada.
 *
 * Todos os testes anteriores liam o JSON DIRETO com Gson, pulando as duas
 * etapas — testavam a camada errada e passavam com o app quebrado.
 *
 * Se alguém adicionar um campo novo à definição e esquecer de repassá-lo,
 * quebra aqui.
 */
class EfeitosNoLoaderTest {

    private fun primeiroObjeto(json: String) =
        JsonParser.parseString(json).asJsonArray.first()

    @Test
    fun `efeitos sobrevive as duas conversoes do loader de vantagem`() {
        val json = """
            [{
              "id": "pendulear",
              "nome": "Pendulear",
              "pagina": 77,
              "costKind": "fixed",
              "fixed": 5,
              "rawCost": "5",
              "tags": ["fisica"],
              "descricao": "Bônus de +2 em Escalada.",
              "efeitos": [ { "tipo": "pericia", "alvo": "Escalada", "valor": 2 } ]
            }]
        """.trimIndent()

        val definicao = parseVantagemParaTeste(primeiroObjeto(json))
        assertTrue("vantagem nao foi parseada", definicao != null)
        assertTrue(
            "efeitos foi descartado entre o JSON e a definicao final",
            definicao!!.efeitos.isNotEmpty()
        )

        val efeito = definicao.efeitos.first()
        assertEquals(TipoEfeito.PERICIA, efeito.tipoResolvido)
        assertEquals("Escalada", efeito.alvo)
        assertEquals(2, efeito.valor)
    }

    @Test
    fun `efeitos sobrevive no loader de desvantagem`() {
        // Sao classes intermediarias DIFERENTES: o esquecimento poderia
        // acontecer so de um lado.
        val json = """
            [{
              "id": "gordo",
              "nome": "Gordo",
              "pagina": 145,
              "costKind": "fixed",
              "fixed": -3,
              "rawCost": "-3",
              "descricao": "-2 em Disfarce.",
              "efeitos": [ { "tipo": "pericia", "alvo": "Disfarce/NT", "valor": -2 } ]
            }]
        """.trimIndent()

        val definicao = parseDesvantagemParaTeste(primeiroObjeto(json))
        assertTrue("desvantagem nao foi parseada", definicao != null)
        assertEquals(1, definicao!!.efeitos.size)
        assertEquals(-2, definicao.efeitos.first().valor)
    }

    @Test
    fun `traco sem efeitos carrega com lista vazia, nunca nula`() {
        val json = """
            [{ "id": "x", "nome": "X", "costKind": "fixed", "fixed": 5, "rawCost": "5" }]
        """.trimIndent()
        val definicao = parseVantagemParaTeste(primeiroObjeto(json))
        assertTrue(definicao!!.efeitos.isEmpty())
    }

    @Test
    fun `varios efeitos no mesmo traco chegam todos`() {
        val json = """
            [{
              "id": "senso_de_direcao", "nome": "Senso de Direção",
              "costKind": "fixed", "fixed": 5, "rawCost": "5",
              "efeitos": [
                { "tipo": "pericia", "alvo": "Percepção do Corpo", "valor": 3 },
                { "tipo": "pericia", "alvo": "Navegação/NT", "valor": 3 }
              ]
            }]
        """.trimIndent()
        val definicao = parseVantagemParaTeste(primeiroObjeto(json))
        assertEquals(2, definicao!!.efeitos.size)
    }
}
