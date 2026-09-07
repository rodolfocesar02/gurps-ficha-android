package com.gurps.ficha.ui.features.mesa

import android.annotation.SuppressLint
import android.content.Context
import android.content.MutableContextWrapper
import android.view.ViewGroup
import android.os.Handler
import android.os.Looper
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.gurps.ficha.domain.rules.ConviteDaMesa
import com.gurps.ficha.domain.rules.EnderecoDaMesa
import com.gurps.ficha.domain.rules.PedidoDaMesa

/**
 * **A sala não mora na aba** — lote MNA-1 do `PLANO_MESA_NO_APP.md`.
 *
 * ## 🟥 O defeito que este arquivo existe para não ter
 *
 * A `FichaScreen` desenha o corpo das abas com um `when`. Um `when` é uma
 * escolha: **só existe a aba que está na frente**. Sair da aba não esconde o que
 * lá estava — apaga.
 *
 * Com um `WebView` criado dentro desse `when`, trocar de aba para olhar a ficha
 * quer dizer: página recarregada, você fora da sala, microfone mudo, e a sessão
 * perdida — a Mesa guarda a sessão no `sessionStorage` de propósito, para ela
 * morrer quando a aba fecha.
 *
 * 🔴 Então a sala mora **aqui**, acima das telas, e a aba apenas a pendura. É
 * como tirar a janela da parede e voltar a pô-la: a sala do outro lado continua
 * lá, com a conversa correndo.
 *
 * ## 🔴 O `MutableContextWrapper`, e por que não é frescura
 *
 * Um `WebView` precisa de um `Context` para desenhar, e o que serve é o da tela.
 * Mas guardar a tela aqui dentro seria segurá-la na memória depois de ela morrer
 * — e a tela morre a cada giro do telefone.
 *
 * O `MutableContextWrapper` é um contexto que **troca de dono por dentro**: o
 * `WebView` segura sempre o mesmo embrulho, e nós trocamos o que está dentro
 * dele. Ao despendurar, o embrulho volta ao contexto do aplicativo, que não
 * morre nunca.
 *
 * ## ⚠️ O que este arquivo AINDA não faz
 *
 * - Não sabe escolher arquivo nem abrir a imagem do chat em tela cheia (MNA-9).
 * - Não sobrevive ao aplicativo ir para o bolso (MNA-8).
 */
object SalaDaMesa {

    /** A janela, enquanto ela existir. */
    private var janela: WebView? = null

    /** O embrulho que troca de dono. Nasce com a janela e morre com ela. */
    private var embrulho: MutableContextWrapper? = null

    /** O endereço a que esta sala está presa. Ver [EnderecoDaMesa]. */
    private var enderecoDaSala: String = ""

    /**
     * Se o convite já foi entregue — MNA-3.
     *
     * 🟥 **Uma vez só, e nunca mais.** O `onPageFinished` dispara a cada carga da
     * página, e a página se recarrega sozinha em dois casos: quando você aperta
     * SAIR (`sairDaMesa`) e quando a versão da Mesa muda. Convidar de novo ali
     * poria você de volta na sala **logo depois de ter saído dela** — e você
     * apertaria SAIR outra vez, e outra.
     *
     * ⚠️ O preço: depois de uma recarga por versão nova, a porta aparece com os
     * campos preenchidos e você aperta entrar. É barato, e é o lado certo de
     * errar.
     */
    private var jaConvidou = false

    /**
     * A ponte com a ficha — MNA-4. Nasce com a janela e morre com ela.
     *
     * ⚠️ Guardada aqui, e não recriada a cada pendurada: o `addJavascriptInterface`
     * vale para a janela, e trocá-la debaixo de uma página aberta deixaria a
     * página com uma ponte para um lugar que já não existe.
     */
    private var ponte: PonteDaMesa? = null

    /**
     * **Quem sabe pedir uma permissão ao telefone** — MNA-7.
     *
     * 🔴 Um `object` não tem tela, e só uma tela pode abrir a caixa de
     * permissão do Android. Quem a tem é a [TabMesa], que põe aqui a forma de
     * pedir enquanto estiver na frente.
     *
     * ⚠️ `null` quando a aba não está pendurada — e aí não se pede nada: uma
     * caixa de permissão aparecendo por cima da aba Perícias seria um susto sem
     * explicação.
     */
    var pedirAoTelefone: ((List<String>, (Boolean) -> Unit) -> Unit)? = null

    /** Quem entra, e com que chave. Guardado para o `onPageFinished` alcançar. */
    private var oNome: String? = null
    private var oToken: String? = null

    /** Se a sala está de pé. */
    val estaDePe: Boolean get() = janela != null

    /**
     * **A janela da sala** — cria na primeira vez, devolve a mesma daí em
     * diante.
     *
     * @param dono o contexto da tela que vai pendurar a janela agora
     * @param endereco o endereço da Mesa (`MesaApiClient.ENDERECO_PADRAO`)
     *
     * ⚠️ `SuppressLint("SetJavaScriptEnabled")`: sem JavaScript não há Mesa
     * nenhuma — a página inteira é JavaScript. O aviso do Android é sobre abrir
     * JavaScript a uma página qualquer, e é exatamente por isso que o
     * [EnderecoDaMesa] existe: aqui não entra página qualquer.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun aJanela(
        dono: Context,
        endereco: String,
        aoReceberPedido: (PedidoDaMesa.Pedido) -> Unit
    ): WebView {
        embrulho?.baseContext = dono
        janela?.let { return it }

        val novo = MutableContextWrapper(dono.applicationContext)
        novo.baseContext = dono
        embrulho = novo
        enderecoDaSala = endereco

        val w = WebView(novo)
        w.settings.apply {
            // 🔴 A Mesa inteira é JavaScript. Sem isto abre uma página branca.
            javaScriptEnabled = true
            // 🔴 A sessão da Mesa mora no `sessionStorage`. Sem isto, entrar e
            // recarregar seria entrar de novo, e a página nem saberia porquê.
            domStorageEnabled = true
            // 🔴 O som dos outros começa a tocar **sozinho**, e não depois de um
            // toque. Numa conversa por voz, esperar um toque é ficar surdo.
            mediaPlaybackRequiresUserGesture = false
        }

        // 🔴 A ponte (MNA-4). Só depois do [EnderecoDaMesa] existir, e é essa a
        // ordem dos lotes: quem dá o direito de ler a ficha à janela tem de saber
        // que a janela não vai a lado nenhum.
        val p = PonteDaMesa(aoReceberPedido)
        ponte = p
        w.addJavascriptInterface(p, PonteDaMesa.NOME_NA_PAGINA)

        w.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean = decidir(request?.url?.toString())

            @Deprecated("Só para Android 6 e anteriores; o app vai até o 24.")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
                decidir(url)

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                entregarOConvite()
            }
        }

        w.webChromeClient = object : WebChromeClient() {
            /**
             * 🟥 **A linha que todo mundo esquece.**
             *
             * O WebView recebe o pedido de microfone da página e, por padrão,
             * **não responde**. Não recusa — fica calado, e a página espera para
             * sempre. Do lado de lá parece que a mesa travou.
             *
             * 🔴 E não basta dizer que sim: o WebView só pode dar o que o
             * aplicativo já tem. Um `grant` com o `RECORD_AUDIO` por conceder
             * devolve um microfone que não grava nada — e, de novo, em silêncio.
             */
            override fun onPermissionRequest(pedido: PermissionRequest?) {
                val p = pedido ?: return
                Handler(Looper.getMainLooper()).post { responderA(p) }
            }
        }

        janela = w
        return w
    }

    /**
     * O que a página pediu, traduzido para o que o Android entende.
     *
     * ⚠️ Só microfone e câmera. Qualquer outro recurso é **recusado**, e não
     * ignorado: a página que peça algo que este aplicativo não sabe dar tem de
     * receber um não e seguir em frente.
     */
    private fun aPermissaoDo(recurso: String): String? = when (recurso) {
        PermissionRequest.RESOURCE_AUDIO_CAPTURE -> android.Manifest.permission.RECORD_AUDIO
        PermissionRequest.RESOURCE_VIDEO_CAPTURE -> android.Manifest.permission.CAMERA
        else -> null
    }

    private fun responderA(pedido: PermissionRequest) {
        val janelaViva = janela ?: return pedido.deny()
        val ctx = janelaViva.context

        val querem = pedido.resources.orEmpty()
        val conhecidos = querem.filter { aPermissaoDo(it) != null }
        // 🔴 Pediu só coisa que não sabemos dar: um NÃO, e não o silêncio.
        if (conhecidos.isEmpty()) return pedido.deny()

        val precisa = conhecidos.mapNotNull { aPermissaoDo(it) }.distinct()
        val falta = precisa.filter {
            androidx.core.content.ContextCompat.checkSelfPermission(ctx, it) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (falta.isEmpty()) return pedido.grant(conhecidos.toTypedArray())

        val pedirDaTela = pedirAoTelefone
        // ⚠️ Sem tela na frente não há como perguntar. Recusar é o certo: a
        // página trata o não, e a pessoa tenta outra vez com a aba aberta.
        if (pedirDaTela == null) return pedido.deny()

        pedirDaTela(falta) { deu ->
            if (deu) pedido.grant(conhecidos.toTypedArray()) else pedido.deny()
        }
    }

    /**
     * **A cerca.** `true` quer dizer *"eu trato, não navegue"*.
     *
     * 🔴 A decisão é do [EnderecoDaMesa], que é Kotlin puro e tem teste. Aqui só
     * se pergunta — um `if` a mais neste arquivo seria uma regra sem teste,
     * porque este arquivo não tem como ser testado sem aparelho.
     */
    private fun decidir(endereco: String?): Boolean =
        when (EnderecoDaMesa.oQueFazerCom(endereco, enderecoDaSala)) {
            EnderecoDaMesa.OQueFazer.SEGUIR -> false
            // 🟥 MNA-4: o pedido é ENTREGUE, e a navegação é recusada.
            //
            // 🔴 É esta linha que tira o botão "Atacar" do mudo. Fora do
            // aplicativo a página monta um `<a target="_blank">` com este
            // endereço e clica nele — e dentro de um WebView esse clique some sem
            // erro nenhum. Aqui ele é apanhado antes de virar navegação.
            //
            // ⚠️ E funciona com a Mesa **exatamente como ela está hoje**, sem uma
            // linha mudada do lado da página.
            EnderecoDaMesa.OQueFazer.E_UM_PEDIDO -> {
                ponte?.entregar(endereco)
                true
            }
            EnderecoDaMesa.OQueFazer.RECUSAR -> true
        }

    /**
     * Abre a Mesa, se ela ainda não estiver aberta.
     *
     * ⚠️ Só na primeira vez. Chamar `loadUrl` a cada pendurada recarregaria a
     * página em cada troca de aba — que é o defeito inteiro, voltando pela
     * porta dos fundos.
     */
    fun abrirSePreciso() {
        val w = janela ?: return
        if (w.url != null) return
        w.loadUrl(EnderecoDaMesa.paraAbrir(enderecoDaSala))
    }

    /**
     * **Quem vai entrar** — MNA-3.
     *
     * ⚠️ Escrito a cada pendurada, e não só na criação: o token pode ser trocado
     * na tela de configuração com a aba já de pé, e o convite seguinte tem de
     * levar o novo. O `jaConvidou` é que garante que ele não é usado duas vezes.
     */
    fun quemEntra(nome: String?, token: String?) {
        oNome = nome
        oToken = token
    }

    /**
     * Manda o convite para dentro da página.
     *
     * 🔴 Pelo `#`, e não chamando o `conectar` da página. O `convite.js` já sabe
     * os três casos — ninguém dentro, o mesmo nome, outro nome — e já preenche os
     * campos antes de tentar. Ver o [ConviteDaMesa].
     *
     * ⚠️ Mexer no `#` **não recarrega** a página. Ela ouve o `hashchange`, trata,
     * e a primeira coisa que faz é limpar o endereço.
     */
    private fun entregarOConvite() {
        if (jaConvidou) return
        val w = janela ?: return
        val js = ConviteDaMesa.oJavascript(oNome, oToken) ?: return
        jaConvidou = true
        w.evaluateJavascript(js, null)
    }

    /** Anda para trás dentro da página. `false` quando não há para onde. */
    fun voltarDentroDaPagina(): Boolean {
        val w = janela ?: return false
        if (!w.canGoBack()) return false
        w.goBack()
        return true
    }

    /**
     * **Sair da mesa de verdade.**
     *
     * 🔴 O único caminho que apaga a sala. Trocar de aba, girar o telefone ou
     * mandar o aplicativo para o bolso **não** passam por aqui — é essa a
     * decisão dele: *"tudo fica conectado até dar SAIR"*.
     */
    fun sair() {
        val w = janela ?: return
        (w.parent as? ViewGroup)?.removeView(w)
        w.stopLoading()
        // ⚠️ `about:blank` antes de destruir: sem isto, o som que estiver tocando
        // pode continuar tocando depois de a janela morrer.
        w.loadUrl("about:blank")
        w.destroy()
        janela = null
        embrulho = null
        enderecoDaSala = ""
        // 🔴 O convite volta a valer só depois de SAIR. Ver `jaConvidou`.
        jaConvidou = false
        oNome = null
        oToken = null
        ponte = null
    }

    /**
     * A janela sai da parede, mas continua viva.
     *
     * ⚠️ O embrulho volta ao contexto do aplicativo. Deixá-lo apontando para uma
     * tela que já morreu é a fuga de memória que o `MutableContextWrapper` veio
     * evitar.
     */
    fun despendurar() {
        val w = janela ?: return
        (w.parent as? ViewGroup)?.removeView(w)
        embrulho?.let { it.baseContext = it.baseContext.applicationContext }
    }
}
