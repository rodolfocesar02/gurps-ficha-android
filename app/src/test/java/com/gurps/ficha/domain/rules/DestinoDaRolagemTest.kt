package com.gurps.ficha.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * **Para onde a rolagem vai** — Lote MESA-7.
 *
 * Pedido do usuario: *"nao vamos substituir o nosso botao, vamos acrescentar um
 * novo; ai podemos pelo app escolher qual o servidor vamos usar"*.
 *
 * O Discord FICA. A Mesa entra ao lado -- e isso e a rede de seguranca: se a
 * sala do PC cair no meio da sessao, um toque devolve as rolagens ao Discord.
 */
class DestinoDaRolagemTest {

    private fun fonte(caminho: String): String {
        val direto = File("src/main/java/$caminho")
        val f = if (direto.exists()) direto else File("app/src/main/java/$caminho")
        assertTrue("nao encontrei ${f.absolutePath}", f.exists())
        return f.readText(Charsets.UTF_8)
    }

    // == O destino guardado ==============================================

    @Test
    fun `no escuro, NAO envia para lugar nenhum`() {
        // 🔴 Ficha antiga nao tem este campo, e um valor renomeado no futuro nao
        // pode derrubar a rolagem. Mandar para o lugar errado e pior do que nao
        // mandar: a mesa inteira le.
        assertEquals(DestinoDaRolagem.NENHUM, DestinoDaRolagem.de(null))
        assertEquals(DestinoDaRolagem.NENHUM, DestinoDaRolagem.de(""))
        assertEquals(DestinoDaRolagem.NENHUM, DestinoDaRolagem.de("SLACK"))
        assertEquals(DestinoDaRolagem.NENHUM, DestinoDaRolagem.de("discord"))  // minusculo
    }

    @Test
    fun `os tres destinos existem, e o Discord continua`() {
        // ⚠️ Este teste guarda o PEDIDO: acrescentar sem substituir.
        assertEquals(
            listOf("NENHUM", "DISCORD", "MESA"),
            DestinoDaRolagem.entries.map { it.name }
        )
        assertEquals(DestinoDaRolagem.DISCORD, DestinoDaRolagem.de("DISCORD"))
        assertEquals(DestinoDaRolagem.MESA, DestinoDaRolagem.de("MESA"))
    }

    // == O que falta para poder enviar ===================================

    @Test
    fun `Discord sem canal escolhido avisa o que falta`() {
        val falta = ProntidaoDoDestino.oQueFalta(
            DestinoDaRolagem.DISCORD, canalDoDiscord = null,
            enderecoDaMesa = null, tokenDaMesa = null
        )
        assertNotNull(falta)
        assertTrue(falta!!, falta.contains("canal"))
    }

    @Test
    fun `Mesa avisa o que falta, uma coisa de cada vez`() {
        // Dizer "falta endereco e token" de uma vez faz o jogador arrumar um e
        // achar que resolveu. Um de cada vez fecha o assunto.
        val semNada = ProntidaoDoDestino.oQueFalta(
            DestinoDaRolagem.MESA, null, null, null
        )
        assertTrue(semNada!!, semNada.contains("endereço"))

        val soSemToken = ProntidaoDoDestino.oQueFalta(
            DestinoDaRolagem.MESA, null, "https://mesagurps.duckdns.org", null
        )
        assertTrue(soSemToken!!, soSemToken.contains("token"))

        assertNull(
            ProntidaoDoDestino.oQueFalta(
                DestinoDaRolagem.MESA, null, "https://mesagurps.duckdns.org", "ABC12345"
            )
        )
    }

    @Test
    fun `NENHUM nunca reclama de nada`() {
        assertNull(ProntidaoDoDestino.oQueFalta(DestinoDaRolagem.NENHUM, null, null, null))
    }

    @Test
    fun `o Discord nao passa a exigir dado da Mesa`() {
        // Regressao: um destino nao pode ficar refem da configuracao do outro.
        assertNull(
            ProntidaoDoDestino.oQueFalta(
                DestinoDaRolagem.DISCORD, canalDoDiscord = "123",
                enderecoDaMesa = null, tokenDaMesa = null
            )
        )
    }

    // == O endereco digitado no celular ==================================

    @Test
    fun `o endereco aceita o jeito que a pessoa digita`() {
        // ⚠️ Recusar por causa de uma barra seria transformar um detalhe de
        // digitacao num "nao funciona" no meio da sessao.
        val esperado = "https://mesagurps.duckdns.org"
        listOf(
            "mesagurps.duckdns.org",
            "https://mesagurps.duckdns.org",
            "https://mesagurps.duckdns.org/",
            "  https://mesagurps.duckdns.org/  "
        ).forEach {
            assertEquals("falhou com '$it'", esperado, ProntidaoDoDestino.enderecoLimpo(it))
        }
    }

    @Test
    fun `endereco http continua http`() {
        // Sem certificado ainda, o teste na rede de casa e http -- forcar https
        // deixaria o app sem conseguir falar com a propria sala.
        assertEquals(
            "http://192.168.1.50:8080",
            ProntidaoDoDestino.enderecoLimpo("http://192.168.1.50:8080/")
        )
    }

    @Test
    fun `endereco vazio vira nulo, e nao uma URL quebrada`() {
        assertNull(ProntidaoDoDestino.enderecoLimpo(null))
        assertNull(ProntidaoDoDestino.enderecoLimpo(""))
        assertNull(ProntidaoDoDestino.enderecoLimpo("   "))
    }

    // == A fiacao ========================================================

    @Test
    fun `o cliente da Mesa manda o token no CORPO, e nao na URL`() {
        // Query string fica no historico do navegador e nos registros de
        // qualquer intermediario -- e este token e a senha da sala inteira.
        val src = fonte("com/gurps/ficha/data/network/MesaApiClient.kt")
        assertTrue("o token saiu do corpo", src.contains("gson.toJson(payload)"))
        assertTrue(
            "o token voltou para a URL",
            !src.contains("?token=") && !src.contains("&token=")
        )
    }

    @Test
    fun `401 da Mesa e explicado como token errado`() {
        // 🔴 E o erro que MAIS vai acontecer: o token muda a cada arranque do
        // servidor. Dizer isso poupa a mesa de cacar problema de rede.
        val src = fonte("com/gurps/ficha/data/network/MesaApiClient.kt")
        assertTrue(src.contains("token_da_sala_invalido"))
    }

    @Test
    fun `o cliente do Discord continua existindo, intocado`() {
        // ⚠️ O pedido era ACRESCENTAR. Este teste reprova quem apagar o antigo.
        val src = fonte("com/gurps/ficha/data/network/DiscordRollApiClient.kt")
        assertTrue("o cliente do Discord sumiu", src.contains("fun postRoll("))
        assertTrue("a rota do bot mudou", src.contains("/api/rolls"))
    }

    // == A tela =========================================================
    //
    // ⚠️ Estes testes leem o CODIGO-FONTE da tela. Nao e elegante, mas ate o
    // lote MESA-7 a regra existia inteira e a tela nao perguntava nada -- foi a
    // sexta vez neste projeto que "a regra existe, a tela nao pergunta". Um
    // teste de unidade da regra passaria verde com a tela vazia.

    @Test
    fun `a tela tem o botao novo do destino`() {
        val src = fonte("com/gurps/ficha/ui/TabRolagem.kt")
        assertTrue("o botao do destino nao esta na tela", src.contains("RolagemDestinoBotao("))
        assertTrue("o dialogo do destino nao abre", src.contains("RolagemDestinoDialog("))
    }

    @Test
    fun `o botao de CANAL do Discord continua na tela, ao lado`() {
        // 🔴 O pedido foi ACRESCENTAR. Se um dia alguem "simplificar" juntando os
        // dois num so, quem usa Discord ganha um toque a mais e uma pergunta nova
        // no caminho que faz ha meses. Este teste reprova essa simplificacao.
        val src = fonte("com/gurps/ficha/ui/TabRolagem.kt")
        assertTrue("o botao de canal do Discord sumiu", src.contains("RolagemHeader("))
        assertTrue("o dialogo do canal sumiu", src.contains("RolagemEditarCanalDialog("))
    }

    @Test
    fun `o dialogo do destino chega ate o ViewModel`() {
        // Sem isto o dialogo abriria bonito e nao guardaria nada.
        val src = fonte("com/gurps/ficha/ui/TabRolagem.kt")
        listOf(
            "viewModel.destinoDaRolagem",
            "viewModel.escolherDestinoDaRolagem",
            "viewModel.configurarMesa",
            "viewModel.testarMesa"
        ).forEach {
            assertTrue("a tela nao chama $it", src.contains(it))
        }
    }

    @Test
    fun `a Mesa so pede endereco e token quando a Mesa esta escolhida`() {
        val src = fonte("com/gurps/ficha/ui/features/rolagem/RolagemDestinoDialog.kt")
        assertTrue(
            "os campos da Mesa aparecem para quem usa Discord",
            src.contains("if (destino == DestinoDaRolagem.MESA) {")
        )
        // ⚠️ Fechar sem testar nao pode apagar o que a pessoa digitou.
        //
        // ⚠️ A comparacao NAO fixa os argumentos: ela fixava, e quebrou no
        // CAMPO-17 quando entrou o terceiro campo (o nome na mesa). O que
        // importa e que o `onSalvarMesa` seja chamado ao fechar -- os
        // argumentos sao assunto do compilador, que ja os confere.
        assertTrue(
            "fechar o dialogo perde o que foi digitado",
            src.contains("if (destino == DestinoDaRolagem.MESA) onSalvarMesa(")
        )
        // 🔴 E o terceiro campo TEM de ir junto: sem o nome na mesa, a ficha do
        // CAMPO-17 nunca cola em token nenhum, e o sintoma e "nao acontece nada".
        assertTrue(
            "o nome na mesa nao e guardado ao fechar",
            src.contains("onSalvarMesa(token, nomeNaMesa)")
        )
    }

    @Test
    fun `a Mesa recebe o retrato do personagem, como o Discord`() {
        // 🔴 A rolagem chegava no Discord com a cara do personagem e na Mesa
        // como texto pelado. A mesma imagem, os dois destinos.
        val cliente = fonte("com/gurps/ficha/data/network/MesaApiClient.kt")
        assertTrue("o cliente da Mesa nao sabe subir retrato", cliente.contains("fun postRetrato("))
        assertTrue("o retrato nao vai para /api/retrato", cliente.contains("/api/retrato"))
        // ⚠️ Token no CORPO, como na rolagem: query string fica no historico.
        assertTrue(
            "o token do retrato foi para a URL",
            cliente.contains("mapOf(\"token\" to token")
        )

        val vm = fonte("com/gurps/ficha/viewmodel/FichaViewModel.kt")
        assertTrue(
            "salvar a ficha nao sobe o retrato para a Mesa",
            vm.contains("socialDelegate.enviarRetratoParaAMesa(nome, dataUri)")
        )
        // E continua subindo para o Discord: o pedido era ACRESCENTAR.
        assertTrue(
            "o retrato deixou de ir para o Discord",
            vm.contains("networkDelegate.enviarRetratoDiscord(nome, dataUri)")
        )
    }

    @Test
    fun `o retrato so vai para a Mesa quando a Mesa e o destino`() {
        // Subir a imagem para um servidor que a pessoa nao escolheu seria
        // mandar a cara do personagem para onde ninguem pediu.
        val delegate = fonte("com/gurps/ficha/viewmodel/delegates/FichaSocialDelegate.kt")
        assertTrue(
            "o retrato vai para a Mesa mesmo com o Discord escolhido",
            delegate.contains("if (destinoDaRolagem != DestinoDaRolagem.MESA) return false")
        )
    }

    @Test
    fun `o token nao aparece na tela em minusculo`() {
        // O servidor compara byte a byte; o teclado do telefone nao ajuda.
        val src = fonte("com/gurps/ficha/ui/features/rolagem/RolagemDestinoDialog.kt")
        assertTrue("o token nao e forcado para maiusculo", src.contains("token = it.uppercase()"))
    }
}
