package com.gurps.ficha.domain.roll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RollDispatchPolicyTest {
    @Test
    fun `deve retentar apenas quando nao houve status HTTP`() {
        assertTrue(RollDispatchPolicy.deveRetentar(null))
        assertFalse(RollDispatchPolicy.deveRetentar(400))
        assertFalse(RollDispatchPolicy.deveRetentar(401))
        assertFalse(RollDispatchPolicy.deveRetentar(500))
    }

    @Test
    fun `mensagens de erro mapeadas por status`() {
        assertEquals("chave de acesso inválida (401)", RollDispatchPolicy.mensagemErro(401, null))
        assertEquals("canal de envio não definido (400)", RollDispatchPolicy.mensagemErro(400, null))
        assertEquals("servidor não configurado corretamente (500)", RollDispatchPolicy.mensagemErro(500, null))
        assertEquals("falha ao publicar no Discord (502)", RollDispatchPolicy.mensagemErro(502, null))
        assertEquals("erro HTTP 429", RollDispatchPolicy.mensagemErro(429, null))
    }

    @Test
    fun `mensagem de rede sem status usa texto padrao e detalhe opcional`() {
        assertEquals(
            "falha de internet/timeout ao conectar no servidor",
            RollDispatchPolicy.mensagemErro(null, null)
        )
        assertEquals(
            "falha de internet/timeout ao conectar no servidor: timeout",
            RollDispatchPolicy.mensagemErro(null, "timeout")
        )
    }
}
