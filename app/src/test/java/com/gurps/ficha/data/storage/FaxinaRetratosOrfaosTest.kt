package com.gurps.ficha.data.storage

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cobre [ImagemPersonagemStore.ehOrfao] — a única regra do app que apaga
 * arquivo do usuário sem confirmação. Um falso positivo aqui destrói o retrato
 * do personagem, então os testes de "NÃO apagar" pesam mais que os de apagar.
 */
class FaxinaRetratosOrfaosTest {

    private val AGORA = 1_000_000_000L
    private val CARENCIA = AGORA - 3_600_000L   // 1h atrás
    private val VELHO = CARENCIA - 1_000L       // anterior à carência
    private val NOVO = AGORA                    // dentro da carência

    private fun ehOrfao(
        nome: String,
        modificadoEm: Long = VELHO,
        emUso: Set<String> = emptySet(),
        jsons: List<String> = emptyList()
    ) = ImagemPersonagemStore.ehOrfao(nome, modificadoEm, CARENCIA, emUso, jsons)

    // --- deve apagar ---

    @Test
    fun `retrato antigo que nenhuma ficha cita e orfao`() {
        assertTrue(ehOrfao("retrato_abc-123.jpg"))
    }

    @Test
    fun `original antigo que nenhuma ficha cita e orfao`() {
        assertTrue(ehOrfao("original_abc-123.jpg"))
    }

    @Test
    fun `sobra de ficha excluida e orfao mesmo com outras fichas presentes`() {
        val jsons = listOf(
            """{"nome":"Paths","imagemPersonagemUri":"file:///x/retrato_vivo.jpg"}"""
        )
        assertTrue(ehOrfao("retrato_da-ficha-apagada.jpg", jsons = jsons))
    }

    // --- NÃO deve apagar ---

    @Test
    fun `retrato citado por alguma ficha nunca e apagado`() {
        val jsons = listOf(
            """{"nome":"Outro","imagemPersonagemUri":"file:///x/retrato_zzz.jpg"}""",
            """{"nome":"Paths","imagemPersonagemUri":"file:///x/retrato_abc-123.jpg"}"""
        )
        assertFalse(ehOrfao("retrato_abc-123.jpg", jsons = jsons))
    }

    @Test
    fun `retrato do personagem carregado agora nunca e apagado`() {
        // Caso real: ficha ainda não salva — não aparece em JSON nenhum.
        assertFalse(ehOrfao("retrato_abc-123.jpg", emUso = setOf("retrato_abc-123.jpg")))
    }

    @Test
    fun `arquivo recem criado fica de fora pela carencia`() {
        // A foto acabou de ser escolhida e o auto-save ainda não rodou.
        assertTrue(ehOrfao("retrato_abc-123.jpg", modificadoEm = VELHO))
        assertFalse(ehOrfao("retrato_abc-123.jpg", modificadoEm = NOVO))
    }

    @Test
    fun `arquivo de terceiros na pasta nunca e tocado`() {
        assertFalse(ehOrfao("qualquer_outra_coisa.jpg"))
        assertFalse(ehOrfao("cache_do_coil.bin"))
        assertFalse(ehOrfao(".nomedia"))
    }

    @Test
    fun `citacao dentro do base64 embutido tambem protege`() {
        // Ficha importada guarda o caminho junto do base64 — o contains pega.
        val jsons = listOf("""{"imagemPersonagemOriginalUri":"file:///x/original_abc.jpg"}""")
        assertFalse(ehOrfao("original_abc.jpg", jsons = jsons))
    }

    @Test
    fun `nome parecido mas diferente nao protege o arquivo errado`() {
        val jsons = listOf("""{"imagemPersonagemUri":"file:///x/retrato_aaa.jpg"}""")
        assertTrue(ehOrfao("retrato_bbb.jpg", jsons = jsons))
        assertFalse(ehOrfao("retrato_aaa.jpg", jsons = jsons))
    }
}
