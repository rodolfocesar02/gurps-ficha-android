package com.gurps.ficha.domain.saga

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lote 353: teste de CONTRATO do toolset do Narrador (mesmo padrão do MestreIAToolsTest).
 * Garante que os dois formatos (Gemini e OpenAI) expõem exatamente as MESMAS tools
 * e que o conjunto bate com o que o NarradorToolExecutor sabe rotear (TODAS).
 */
class NarradorToolsTest {

    private fun nomesOpenAI(tools: JSONArray): List<String> =
        (0 until tools.length()).map { tools.getJSONObject(it).getJSONObject("function").getString("name") }

    private fun nomesGemini(tools: JSONArray): List<String> {
        val decls = tools.getJSONObject(0).getJSONArray("functionDeclarations")
        return (0 until decls.length()).map { decls.getJSONObject(it).getString("name") }
    }

    @Test
    fun `toolset OpenAI cobre exatamente as tools que o executor conhece`() {
        val nomes = nomesOpenAI(NarradorTools.getOpenAITools())
        assertEquals("Sem tool duplicada", nomes.size, nomes.toSet().size)
        assertEquals(NarradorTools.TODAS, nomes.toSet())
    }

    @Test
    fun `toolset Gemini cobre exatamente as tools que o executor conhece`() {
        val nomes = nomesGemini(NarradorTools.getGeminiTools())
        assertEquals("Sem tool duplicada", nomes.size, nomes.toSet().size)
        assertEquals(NarradorTools.TODAS, nomes.toSet())
    }

    @Test
    fun `sao 17 tools - 15 do narrador mais localizar e ler do Codex`() {
        assertEquals(17, NarradorTools.TODAS.size)
        assertTrue(NarradorTools.TOOL_LOCALIZAR in NarradorTools.TODAS)
        assertTrue(NarradorTools.TOOL_LER in NarradorTools.TODAS)
        assertTrue(NarradorTools.TOOL_GERIR_EQUIPAMENTO in NarradorTools.TODAS)
    }

    @Test
    fun `campos obrigatorios declarados nos dois formatos`() {
        // pedir_rolagem exige a perícia; registrar_fato exige o trio sujeito-predicado-objeto + peso.
        val openai = NarradorTools.getOpenAITools()
        fun required(nome: String): Set<String> {
            for (i in 0 until openai.length()) {
                val fn = openai.getJSONObject(i).getJSONObject("function")
                if (fn.getString("name") == nome) {
                    val req = fn.getJSONObject("parameters").optJSONArray("required") ?: return emptySet()
                    return (0 until req.length()).map { req.getString(it) }.toSet()
                }
            }
            return emptySet()
        }
        assertTrue("pericia" in required(NarradorTools.TOOL_PEDIR_ROLAGEM))
        assertEquals(setOf("sujeito", "predicado", "objeto", "peso"), required(NarradorTools.TOOL_REGISTRAR_FATO))
        assertTrue("consulta" in required(NarradorTools.TOOL_CONSULTAR_MUNDO))
        assertEquals(setOf("livro", "pagina"), required(NarradorTools.TOOL_LER))
    }
}
