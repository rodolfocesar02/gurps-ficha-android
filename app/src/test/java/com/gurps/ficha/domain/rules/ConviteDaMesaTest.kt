package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **Entrar na sala sozinho** — lote MNA-3.
 *
 * 🔴 O que se prova aqui é a parte perigosa: um nome de personagem é texto que a
 * pessoa digitou, e ele vai acabar dentro de uma linha de JavaScript.
 */
class ConviteDaMesaTest {

    @Test
    fun `o convite comum`() {
        assertEquals(
            "#nome=Cesar&t=ABC123",
            ConviteDaMesa.oFragmento("Cesar", "ABC123")
        )
    }

    @Test
    fun `o token sobe para maiusculas, como na tela`() {
        // ⚠️ A sala compara byte a byte. Sem isto, quem digitasse minúsculo era
        // recusado sem entender porquê.
        assertEquals("#nome=Cesar&t=ABC123", ConviteDaMesa.oFragmento("Cesar", " abc123 "))
    }

    @Test
    fun `espaco no nome vira mais, que e o que a pagina espera`() {
        // A página lê com `URLSearchParams`, que desfaz o `+` em espaço.
        assertEquals(
            "#nome=Cesar+Augusto&t=ABC",
            ConviteDaMesa.oFragmento("Cesar Augusto", "ABC")
        )
    }

    @Test
    fun `acento sobrevive`() {
        val f = ConviteDaMesa.oFragmento("Antônio", "ABC")!!
        assertEquals("#nome=Ant%C3%B4nio&t=ABC", f)
    }

    @Test
    fun `🟥 uma aspa no nome NAO parte a linha de JavaScript`() {
        // 🔴 O caso que este arquivo existe para provar. `O'Brien` sem escape
        // fecharia o texto no meio, e o que viesse depois seria código.
        val js = ConviteDaMesa.oJavascript("O'Brien", "ABC")!!
        assertEquals("location.hash = '#nome=O%27Brien&t=ABC';", js)
        // Só as duas aspas da própria linha, e nenhuma vinda do nome.
        assertEquals(2, js.count { it == '\'' })
    }

    @Test
    fun `🟥 nem uma barra invertida, nem uma quebra de linha`() {
        listOf("a\\'; alert(1); //", "linha\numa", "aspas\"dentro", "</script>")
            .forEach { nome ->
                val js = ConviteDaMesa.oJavascript(nome, "ABC")!!
                assertEquals("[$nome] aspas a mais", 2, js.count { it == '\'' })
                assertTrue("[$nome] sobrou barra invertida", !js.contains('\\'))
                assertTrue("[$nome] sobrou quebra de linha", !js.contains('\n'))
                assertTrue("[$nome] sobrou aspa dupla", !js.contains('"'))
                assertTrue("[$nome] sobrou sinal de menor", !js.contains('<'))
            }
    }

    @Test
    fun `sem nome ou sem token nao ha convite`() {
        assertNull(ConviteDaMesa.oFragmento(null, "ABC"))
        assertNull(ConviteDaMesa.oFragmento("Cesar", null))
        assertNull(ConviteDaMesa.oFragmento("", "ABC"))
        assertNull(ConviteDaMesa.oFragmento("  ", "ABC"))
        assertNull(ConviteDaMesa.oFragmento("Cesar", "   "))
        assertNull(ConviteDaMesa.oJavascript(null, null))
    }

    @Test
    fun `nome e token comprido demais sao cortados`() {
        val f = ConviteDaMesa.oFragmento("x".repeat(200), "y".repeat(200))!!
        // 60 e 32 — os mesmos tetos do `convite.js`.
        assertEquals("#nome=" + "x".repeat(60) + "&t=" + "Y".repeat(32), f)
    }

    @Test
    fun `🔴 e pelo fragmento, e nao por href`() {
        // ⚠️ `location.href` pode recarregar a página, e recarregar é perder a
        // sessão e a voz. Mexer só no `#` não recarrega nada.
        val js = ConviteDaMesa.oJavascript("Cesar", "ABC")!!
        assertTrue(js.startsWith("location.hash = "))
        assertTrue(!js.contains("location.href"))
        assertTrue(!js.contains("reload"))
    }
}
