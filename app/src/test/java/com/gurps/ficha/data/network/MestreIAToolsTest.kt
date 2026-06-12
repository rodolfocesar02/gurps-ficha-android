package com.gurps.ficha.data.network

import com.gurps.ficha.domain.tools.ForjadorTools
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Lote 350: teste de CONTRATO entre os toolsets enviados à IA e o executor.
 *
 * Protege contra o bug do commit d9d999c (corrigido no Lote 350): o toolset
 * unificado do modo "analise" oferecia ao modelo schemas que o executor
 * (MestreIAGeneratorUseCase) não roda. Se alguém mudar a composição de um
 * toolset sem atualizar o executor (ou vice-versa), este teste quebra.
 */
class MestreIAToolsTest {

    /** Nomes que o executor do modo "analise" aceita (filtro em MestreIAGeneratorUseCase). */
    private val aceitasPeloExecutorUnificado = setOf(
        ForjadorTools.TOOL_LER_FICHA,
        ForjadorTools.TOOL_BUSCAR,
        ForjadorTools.TOOL_GPS_MAGIA,
        ForjadorTools.TOOL_EDITAR,
        ForjadorTools.TOOL_BUSCAR_RACAS,
        ForjadorTools.TOOL_APLICAR_RACIAL,
        MestreIATools.TOOL_LOCALIZAR,
        MestreIATools.TOOL_LER,
        MestreIATools.TOOL_NEXUS_ARCANO
    )

    /** Nomes que o executor do Auditor (modo conversa) aceita (dispatch em MestreIAUseCase). */
    private val aceitasPeloAuditor = setOf(
        MestreIATools.TOOL_LOCALIZAR,
        MestreIATools.TOOL_LER,
        MestreIATools.TOOL_INSPECT_CHARACTER,
        MestreIATools.TOOL_NEXUS_ARCANO
    )

    private fun nomesOpenAI(tools: JSONArray): List<String> =
        (0 until tools.length()).map { tools.getJSONObject(it).getJSONObject("function").getString("name") }

    private fun nomesGemini(tools: JSONArray): List<String> {
        val decls = tools.getJSONObject(0).getJSONArray("functionDeclarations")
        return (0 until decls.length()).map { decls.getJSONObject(it).getString("name") }
    }

    @Test
    fun `toolset unificado OpenAI bate exatamente com o executor do modo analise`() {
        val nomes = nomesOpenAI(MestreIATools.getAuditorUnificadoToolsOpenAI())
        assertEquals("Não pode haver tool duplicada", nomes.size, nomes.toSet().size)
        assertEquals(aceitasPeloExecutorUnificado, nomes.toSet())
    }

    @Test
    fun `toolset unificado Gemini bate exatamente com o executor do modo analise`() {
        val nomes = nomesGemini(MestreIATools.getAuditorUnificadoToolsGemini())
        assertEquals("Não pode haver tool duplicada", nomes.size, nomes.toSet().size)
        assertEquals(aceitasPeloExecutorUnificado, nomes.toSet())
    }

    @Test
    fun `toolset do Auditor OpenAI bate exatamente com o dispatch do MestreIAUseCase`() {
        val nomes = nomesOpenAI(MestreIATools.getAuditorToolsOpenAI())
        assertEquals(aceitasPeloAuditor, nomes.toSet())
    }

    @Test
    fun `toolset do Auditor Gemini bate exatamente com o dispatch do MestreIAUseCase`() {
        val nomes = nomesGemini(MestreIATools.getAuditorToolsGemini())
        assertEquals(aceitasPeloAuditor, nomes.toSet())
    }
}
