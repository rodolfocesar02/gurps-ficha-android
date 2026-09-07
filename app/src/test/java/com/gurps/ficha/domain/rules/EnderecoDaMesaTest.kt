package com.gurps.ficha.domain.rules

import com.gurps.ficha.domain.rules.EnderecoDaMesa.OQueFazer
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * **A aba da Mesa só vai a um lugar** — lote MNA-2.
 *
 * 🔴 A ponte dá o direito de ler a ficha **à janela**, e não ao endereço. Se a
 * janela puder sair da Mesa, o direito sai com ela.
 *
 * ⚠️ Os casos daqui não são teóricos: são as formas conhecidas de fazer um
 * endereço **parecer** ser de um lugar e ser de outro.
 */
class EnderecoDaMesaTest {

    private val MESA = "https://mesagurps.duckdns.org"

    @Test
    fun `a propria Mesa segue`() {
        assertEquals(OQueFazer.SEGUIR, EnderecoDaMesa.oQueFazerCom(MESA, MESA))
    }

    @Test
    fun `paginas de dentro da Mesa seguem`() {
        listOf(
            "https://mesagurps.duckdns.org/",
            "https://mesagurps.duckdns.org/campo",
            "https://mesagurps.duckdns.org/regras/ficha-motor.js",
            "https://mesagurps.duckdns.org/?x=1",
            "https://mesagurps.duckdns.org/#nome=Cesar"
        ).forEach {
            assertEquals(it, OQueFazer.SEGUIR, EnderecoDaMesa.oQueFazerCom(it, MESA))
        }
    }

    @Test
    fun `o dono do endereco em MAIUSCULAS e a mesma Mesa`() {
        // ⚠️ O dono do endereço não distingue maiúsculas; o caminho distingue.
        assertEquals(
            OQueFazer.SEGUIR,
            EnderecoDaMesa.oQueFazerCom("https://MesaGurps.DuckDNS.org/campo", MESA)
        )
    }

    @Test
    fun `a porta escrita por extenso e a mesma porta`() {
        // `https://x` e `https://x:443` são o mesmo lugar.
        assertEquals(
            OQueFazer.SEGUIR,
            EnderecoDaMesa.oQueFazerCom("https://mesagurps.duckdns.org:443/", MESA)
        )
    }

    @Test
    fun `🟥 um endereco que so COMECA igual e recusado`() {
        // 🔴 É por isto que a comparação é por origem, e não por `startsWith`.
        listOf(
            "https://mesagurps.duckdns.org.enganar.com/",
            "https://mesagurps.duckdns.org.br/",
            "https://mesagurps.duckdns.orgx/"
        ).forEach {
            assertEquals(it, OQueFazer.RECUSAR, EnderecoDaMesa.oQueFazerCom(it, MESA))
        }
    }

    @Test
    fun `🟥 a Mesa como nome de usuario e recusada`() {
        // ⚠️ `https://mesa@enganar.com` — o dono do endereço é o `enganar.com`;
        // a Mesa ali é só o nome de quem entra. É a forma clássica.
        assertEquals(
            OQueFazer.RECUSAR,
            EnderecoDaMesa.oQueFazerCom(
                "https://mesagurps.duckdns.org@enganar.com/", MESA
            )
        )
    }

    @Test
    fun `outra porta e outro lugar`() {
        assertEquals(
            OQueFazer.RECUSAR,
            EnderecoDaMesa.oQueFazerCom("https://mesagurps.duckdns.org:8443/", MESA)
        )
    }

    @Test
    fun `🔴 a MESMA Mesa sem cadeado e recusada`() {
        // Sem HTTPS não há microfone nem câmera, e um endereço em `http` no meio
        // da sessão seria a mesa a funcionar pela metade sem dizer porquê.
        assertEquals(
            OQueFazer.RECUSAR,
            EnderecoDaMesa.oQueFazerCom("http://mesagurps.duckdns.org/", MESA)
        )
    }

    @Test
    fun `o mundo inteiro e recusado`() {
        listOf(
            "https://google.com",
            "https://youtube.com/watch?v=1",
            "file:///sdcard/Download/x.html",
            "content://com.android.providers/1",
            "intent://x#Intent;scheme=http;end",
            "data:text/html,<h1>oi</h1>"
        ).forEach {
            assertEquals(it, OQueFazer.RECUSAR, EnderecoDaMesa.oQueFazerCom(it, MESA))
        }
    }

    @Test
    fun `🟥 javascript no endereco e recusado`() {
        // ⚠️ Não é um lugar: é a página se mandando a si mesma. E com a ponte de
        // pé, seria a página se mandando a si mesma com o direito de ler a ficha.
        assertEquals(
            OQueFazer.RECUSAR,
            EnderecoDaMesa.oQueFazerCom("javascript:alert(1)", MESA)
        )
    }

    @Test
    fun `vazio e lixo sao recusados`() {
        listOf(null, "", "   ", "isto nao e um endereco", "://", "https://").forEach {
            assertEquals(
                "[$it]", OQueFazer.RECUSAR, EnderecoDaMesa.oQueFazerCom(it, MESA)
            )
        }
    }

    @Test
    fun `🔴 o pedido do aplicativo NAO e recusa`() {
        // É o botão "Atacar" do tabuleiro. Tratá-lo como navegação recusada
        // deixaria o botão mudo — sem erro, sem nada, que é o pior dos casos.
        listOf(
            "gurpsapp://rolar?o=ataque&pericia=espada_larga&mod=-7",
            "gurpsapp://conectar?id=1",
            "GURPSAPP://rolar?o=dano"
        ).forEach {
            assertEquals(it, OQueFazer.E_UM_PEDIDO, EnderecoDaMesa.oQueFazerCom(it, MESA))
        }
    }

    @Test
    fun `o esquema do pedido e o mesmo do PedidoDaMesa`() {
        // ⚠️ Se um dia alguém trocar o esquema num lado só, isto fica vermelho.
        assertEquals(
            OQueFazer.E_UM_PEDIDO,
            EnderecoDaMesa.oQueFazerCom("${PedidoDaMesa.ESQUEMA}://rolar?o=dano", MESA)
        )
    }

    @Test
    fun `o endereco de abrir nao leva nome nem token`() {
        val abrir = EnderecoDaMesa.paraAbrir("https://mesagurps.duckdns.org/")
        assertEquals("https://mesagurps.duckdns.org", abrir)
        // 🔴 A prova do que importa: nada de token no endereço.
        assertEquals(false, abrir.contains("t="))
        assertEquals(false, abrir.contains("#"))
    }

    @Test
    fun `uma Mesa de teste na rede de casa tambem funciona`() {
        // ⚠️ O endereço da Mesa é fixo hoje, mas este arquivo não decide isso —
        // ele compara com o que receber. Uma base em `http` local segue.
        val local = "http://192.168.0.10:3000"
        assertEquals(
            OQueFazer.SEGUIR,
            EnderecoDaMesa.oQueFazerCom("http://192.168.0.10:3000/campo", local)
        )
        assertEquals(
            OQueFazer.RECUSAR,
            EnderecoDaMesa.oQueFazerCom("http://192.168.0.11:3000/", local)
        )
    }
}
