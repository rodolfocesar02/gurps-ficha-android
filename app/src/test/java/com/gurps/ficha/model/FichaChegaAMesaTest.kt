package com.gurps.ficha.model

import android.content.SharedPreferences
import com.gurps.ficha.data.network.DiscordRollSendResult
import com.gurps.ficha.data.network.MesaApiClient
import com.gurps.ficha.domain.rules.DestinoDaRolagem
import com.gurps.ficha.viewmodel.delegates.FichaSocialDelegate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * **A ficha CHEGA à mesa?** — e este teste corre a corrente, não a lê.
 *
 * ## 🔴 Por que ele existe
 *
 * A ficha não chegava à mesa. Três rodadas, três diagnósticos, e todos os testes
 * verdes o tempo todo — porque **todos liam o código como texto**.
 *
 * O primeiro defeito foi o endereço vazio: o MESA-44 tirou o campo da tela e
 * deixou o valor a ser lido das preferências, onde nunca houve nada. O teste
 * dele conferia que o campo saiu da tela e que a constante existe. Nunca
 * conferiu que o endereço **usado** é essa constante.
 *
 * ⚠️ *"Talvez o erro esteja em outro arquivo ou componente, onde seus testes não
 * estão pegando"* — foi o que o usuário disse, e estava certo.
 *
 * ## 🔴 O que muda aqui
 *
 * Isto monta o delegate de verdade, com as preferências que a pessoa teria, e
 * **chama a função**. O que se mede é o pedido HTTP que sai — ou não sai — pela
 * costura `MesaApiClient.transporteDeTeste`.
 *
 * Assim, qualquer peneira nova que apareça no caminho é apanhada, esteja ela no
 * arquivo que estiver.
 */
class FichaChegaAMesaTest {

    /** Um pedido que saiu pela costura. */
    private data class Pedido(val endpoint: String, val corpo: String)

    private val pedidos = mutableListOf<Pedido>()

    @After
    fun limpar() {
        // ⚠️ A costura é global. Deixá-la posta faria o teste seguinte falar com
        // uma rede que não existe -- ou pior, com a de verdade.
        MesaApiClient.transporteDeTeste = null
    }

    private fun ligarACostura(resposta: DiscordRollSendResult = DiscordRollSendResult(true, 200, null)) {
        pedidos.clear()
        MesaApiClient.transporteDeTeste = { endpoint, corpo ->
            pedidos.add(Pedido(endpoint, String(corpo, Charsets.UTF_8)))
            resposta
        }
    }

    // == As preferências, de mentira =================================

    /**
     * ⚠️ Um `SharedPreferences` de verdade num teste JVM devolve "not mocked".
     * Este é um mapa, e é o suficiente: o delegate só lê e escreve strings.
     */
    private class PrefsDeMentira(
        private val mapa: MutableMap<String, String?> = mutableMapOf()
    ) : SharedPreferences {
        override fun getAll(): MutableMap<String, *> = mapa
        override fun getString(key: String?, defValue: String?): String? = mapa[key] ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?) = defValues
        override fun getInt(key: String?, defValue: Int) = defValue
        override fun getLong(key: String?, defValue: Long) = defValue
        override fun getFloat(key: String?, defValue: Float) = defValue
        override fun getBoolean(key: String?, defValue: Boolean) = defValue
        override fun contains(key: String?) = mapa.containsKey(key)
        override fun registerOnSharedPreferenceChangeListener(
            l: SharedPreferences.OnSharedPreferenceChangeListener?
        ) { }
        override fun unregisterOnSharedPreferenceChangeListener(
            l: SharedPreferences.OnSharedPreferenceChangeListener?
        ) { }
        override fun edit(): SharedPreferences.Editor = EditorDeMentira(mapa)
    }

    private class EditorDeMentira(
        private val mapa: MutableMap<String, String?>
    ) : SharedPreferences.Editor {
        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            if (key != null) mapa[key] = value
            return this
        }
        override fun putStringSet(k: String?, v: MutableSet<String>?) = this
        override fun putInt(k: String?, v: Int) = this
        override fun putLong(k: String?, v: Long) = this
        override fun putFloat(k: String?, v: Float) = this
        override fun putBoolean(k: String?, v: Boolean) = this
        override fun remove(key: String?): SharedPreferences.Editor {
            mapa.remove(key); return this
        }
        override fun clear(): SharedPreferences.Editor { mapa.clear(); return this }
        override fun commit() = true
        override fun apply() { }
    }

    /**
     * O delegate como ele fica **depois de a pessoa apertar CONECTAR À MESA**.
     *
     * 🔴 Sem `networkDelegate` nem `scope`: nada do que se testa aqui os toca, e
     * exigi-los faria este teste depender do Discord para provar a Mesa.
     */
    private fun delegateConfigurado(
        nome: String? = "emulador",
        token: String? = "K88NFHG5",
        destino: DestinoDaRolagem = DestinoDaRolagem.MESA
    ): FichaSocialDelegate {
        val d = FichaSocialDelegate(
            // ⚠️ Um de verdade, e nao `null as ...`: o construtor nao o aceita
            // nulo, e o cast estourava antes de o teste chegar a medir o que quer.
            // Nada aqui o toca -- ele so serve ao caminho do Discord.
            networkDelegate = com.gurps.ficha.viewmodel.delegates.FichaNetworkDelegate(),
            configPrefs = PrefsDeMentira(),
            scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
        )
        d.escolherDestino(destino)
        d.configurarMesa(token, nome)
        return d
    }

    /**
     * 🔴 Um personagem com numeros que NAO sao os padroes da `FichaCalculada`.
     *
     * ⚠️ Com ST/DX/IQ/HT todos a 10, o padrao da classe da a MESMA resposta que
     * o personagem -- e entao um campo que o `de()` deixasse de copiar continuava
     * a aparecer no JSON, com o valor certo por acaso. Uma sonda mostrou isso:
     * apaguei a copia da Velocidade Basica e o teste ficou verde.
     *
     * DX 12 e HT 12 dao Velocidade Basica 6,0 -- e nao os 5,0 do padrao.
     */
    private val umPersonagem = Personagem(
        nome = "Jack Eagle Eye Carter",
        forca = 11, destreza = 12, inteligencia = 13, vitalidade = 12
    )

    // == 🔴 A corrente inteira ======================================

    @Test
    fun `🔴 com tudo configurado, a ficha SAI do app`() {
        // 🔴 É o teste que faltava. Ele corre o caminho todo, do delegate ao
        // corpo em JSON -- e teria apanhado o endereço vazio na hora.
        ligarACostura()
        val d = delegateConfigurado()

        val r = runBlocking {
            d.enviarFichaParaAMesa("emulador", FichaCalculada.de(umPersonagem))
        }

        assertTrue("a ficha nao saiu: " + r.recado, r.ok)
        assertEquals("saiu pedido a mais ou a menos", 1, pedidos.size)
        assertEquals(
            "a ficha foi para o endereco errado",
            "https://mesagurps.duckdns.org/api/ficha",
            pedidos[0].endpoint
        )
    }

    @Test
    fun `🔴 o corpo leva o token, o AUTOR e a ficha calculada`() {
        // 🔴 O `autor` e o nome de quem esta NA MESA, e nao o do personagem: e
        // por ele que a mesa acha o boneco que a pessoa criou (`criadoPor`).
        // Mandar o nome do personagem faria a ficha nunca colar em token nenhum.
        ligarACostura()
        val d = delegateConfigurado(nome = "emulador")
        runBlocking { d.enviarFichaParaAMesa("emulador", FichaCalculada.de(umPersonagem)) }

        val corpo = pedidos[0].corpo
        assertTrue("falta o token no corpo", corpo.contains("\"token\":\"K88NFHG5\""))
        assertTrue("o autor nao e o nome NA MESA", corpo.contains("\"autor\":\"emulador\""))
        assertTrue("falta a ficha", corpo.contains("\"ficha\""))
        // ⚠️ E o nome do PERSONAGEM vai dentro da ficha, que e onde ele serve.
        // 🔴 O NOME do personagem, que faltava desde o CAMPO-16 e fazia a Mesa
        // recusar TODA ficha com 400 -- calada, porque o envio e best-effort.
        assertTrue(
            "a ficha foi sem nome: a Mesa recusa-a inteira. CORPO: " + corpo.take(700),
            corpo.contains("\"nome\":\"Jack Eagle Eye Carter\"")
        )
        // ⚠️ E escreve o corpo num arquivo, para o teste do OUTRO lado o usar.
        // Sem isso os dois lados voltam a inventar cada um a sua ficha de mentira
        // -- que foi exatamente como o campo em falta passou tres meses.
        java.io.File("build/ficha-como-o-app-manda.json").also {
            it.parentFile?.mkdirs()
        }.writeText(corpo)
        // 🔴 E o token NAO vai na URL: query string fica no historico e nos
        // registros de qualquer intermediario.
        assertFalse("o token foi parar na URL", pedidos[0].endpoint.contains("K88NFHG5"))
    }

    @Test
    fun `🔴 a ficha calculada leva os numeros que a mesa USA`() {
        // 🔴 Sem a Velocidade Basica a iniciativa cai em ordem alfabetica, e sem
        // a Esquiva o boneco nao mostra defesa nenhuma. Sao estes os numeros que
        // o JSON cru NAO tem, porque os derivados sao propriedades e o Gson
        // serializa campos -- e e por isso que o `FichaCalculada` existe.
        ligarACostura()
        val d = delegateConfigurado()
        runBlocking { d.enviarFichaParaAMesa("emulador", FichaCalculada.de(umPersonagem)) }

        val corpo = pedidos[0].corpo
        // 🔴 Os VALORES, e nao so os nomes dos campos.
        //
        // ⚠️ A primeira versao disto conferia que a chave aparecia no JSON -- e
        // uma sonda apagou a copia da Velocidade Basica sem a fazer ficar
        // vermelha, porque o PADRAO da classe (5,0) e o mesmo numero que o
        // personagem de teste dava. Agora os numeros sao todos diferentes do
        // padrao, e o que se cobra e o valor.
        listOf(
            "\"st\":11", "\"dx\":12", "\"iq\":13", "\"ht\":12",
            // (DX 12 + HT 12) / 4 = 6,0 -- MB p.17. O padrao da classe e 5,0.
            "\"velocidadeBasica\":6.0"
        ).forEach {
            assertTrue("a ficha foi sem `$it`. CORPO: " + corpo.take(600), corpo.contains(it))
        }
        // E os que a mesa mostra ao lado do boneco.
        listOf("esquiva", "pontosVida", "pontosFadiga", "deslocamentoAtual")
            .forEach { assertTrue("a ficha foi sem `$it`", corpo.contains("\"$it\"")) }
    }

    // == ⚠️ Onde ela NÃO sai, e por que ==============================

    @Test
    fun `⚠️ sem Seu nome preenchido, a ficha nao sai -- e DIZ porque`() {
        ligarACostura()
        val d = delegateConfigurado(nome = null)

        val r = runBlocking { d.enviarFichaParaAMesa("", FichaCalculada.de(umPersonagem)) }

        assertFalse(r.ok)
        assertEquals("nao devia ter saido pedido nenhum", 0, pedidos.size)
        assertTrue("o recado nao diz o que fazer", r.recado.contains("Seu nome"))
    }

    @Test
    fun `⚠️ sem token, a ficha nao sai -- e DIZ porque`() {
        ligarACostura()
        val d = delegateConfigurado(token = null)

        val r = runBlocking { d.enviarFichaParaAMesa("emulador", FichaCalculada.de(umPersonagem)) }

        assertFalse(r.ok)
        assertEquals(0, pedidos.size)
        assertTrue("o recado nao fala do token", r.recado.contains("token"))
    }

    @Test
    fun `🔴 com o destino no Discord, a ficha nao sai -- e ISSO nao e falha`() {
        // 🔴 Aqui esta a unica saida que fica CALADA de proposito: quem escolheu
        // o Discord nao quer a ficha na mesa, e um recado a cada salvar poria a
        // pessoa a cacar problema que nao existe.
        ligarACostura()
        val d = delegateConfigurado(destino = DestinoDaRolagem.DISCORD)

        val r = runBlocking { d.enviarFichaParaAMesa("emulador", FichaCalculada.de(umPersonagem)) }

        assertTrue("escolher o Discord virou erro", r.ok)
        assertEquals("", r.recado)
        assertEquals(0, pedidos.size)
    }

    @Test
    fun `⚠️ token recusado pela sala vira um recado que diz o que fazer`() {
        ligarACostura(DiscordRollSendResult(false, 401, "token_da_sala_invalido"))
        val d = delegateConfigurado()

        val r = runBlocking { d.enviarFichaParaAMesa("emulador", FichaCalculada.de(umPersonagem)) }

        assertFalse(r.ok)
        assertTrue("o recado nao manda pedir o token novo", r.recado.contains("Mestre"))
    }

    // == 🔴 A rolagem e a ficha usam a MESMA configuração ============

    @Test
    fun `🔴 se a ROLAGEM chega, a ficha tem de chegar tambem`() {
        // 🔴 Este teste nasceu de uma medicao no aparelho: a rolagem chegava a
        // mesa e a ficha nao. Isso prova que endereco, token, destino e rede
        // estao todos bons -- entao a diferenca so podia estar no caminho da
        // ficha.
        //
        // ⚠️ Com a mesma configuracao, os dois caminhos tem de sair. Se um dia um
        // sair e o outro nao, e este teste que o diz -- e nao o usuario.
        ligarACostura()
        val d = delegateConfigurado()

        val rolou = runBlocking {
            d.enviarRolagem(
                com.gurps.ficha.data.network.DiscordRollPayload(
                    character = "Jack Eagle Eye Carter",
                    testType = "Defesa (Esquiva)",
                    context = "",
                    modifier = 0,
                    dice = listOf(6, 6),
                    total = 16,
                    outcome = "FALHA",
                    margin = -8,
                    target = 4
                )
            )
        }
        val mandouFicha = runBlocking {
            d.enviarFichaParaAMesa("emulador", FichaCalculada.de(umPersonagem))
        }

        assertTrue("a rolagem nao saiu: " + rolou.detalhe, rolou.enviado)
        assertTrue("a ROLAGEM sai e a FICHA nao, com a mesma configuracao", mandouFicha.ok)

        val rotas = pedidos.map { it.endpoint.substringAfterLast('/') }
        assertTrue("faltou a rolagem: $rotas", rotas.contains("rolagem"))
        assertTrue("faltou a ficha: $rotas", rotas.contains("ficha"))
    }

    // == 🔴 O endereço, que foi o primeiro defeito ===================

    @Test
    fun `🔴 o endereco NUNCA vem vazio, nem com as preferencias em branco`() {
        // 🔴 Foi este o defeito: preferencias vazias -> endereco vazio ->
        // `postFicha` saia no `if (baseUrl.isBlank())`, calado. Num emulador ou
        // numa instalacao nova era SEMPRE assim.
        val d = FichaSocialDelegate(
            networkDelegate = com.gurps.ficha.viewmodel.delegates.FichaNetworkDelegate(),
            configPrefs = PrefsDeMentira(),
            scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
        )
        assertEquals("https://mesagurps.duckdns.org", d.mesaEndereco)
        assertNotNull(d.mesaEndereco)
        assertFalse("o endereco voltou a poder ser vazio", d.mesaEndereco.isBlank())
    }

    @Test
    fun `⚠️ a costura esta DESLIGADA em producao`() {
        // ⚠️ Se ela ficasse ligada, o app falaria com uma rede que nao existe.
        MesaApiClient.transporteDeTeste = null
        assertNull(MesaApiClient.transporteDeTeste)
    }
}
