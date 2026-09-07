package com.gurps.ficha.ui.features.mesa

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.MutableContextWrapper
import android.net.Uri
import android.view.ViewGroup
import android.os.Handler
import android.os.Looper
import android.webkit.PermissionRequest
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.gurps.ficha.domain.rules.ConviteDaMesa
import com.gurps.ficha.domain.rules.EnderecoDaMesa
import com.gurps.ficha.domain.rules.PedidoDaMesa
import com.gurps.ficha.service.ServicoDaMesa

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
 * ## ⚠️ O que fica de fora
 *
 * - O que sobra de fora: a aba está na variante `visual` apenas, e compartilhar
 *   tela não existe em WebView nenhum (MNA-7).
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

    /**
     * **O que a tela faz quando você sai da mesa** — MNA-1b.
     *
     * 🔴 Sair da **sala** não é sair da **aba**. Ele encontrou isto no aparelho:
     * apertar SAIR dentro da Mesa deixava a pessoa olhando para a porta de
     * entrada da sala, dentro da mesma aba, sem nada dizendo para onde ir.
     *
     * ⚠️ Posto pela [TabMesa] enquanto ela estiver na frente, e `null` quando
     * não estiver — quem sai pela notificação, com o telefone no bolso, não tem
     * aba nenhuma para trocar.
     */
    var aoSairDaAba: (() -> Unit)? = null

    /**
     * **Quem sabe abrir o explorador de arquivos** — MNA-9.
     *
     * ⚠️ Mesma razão do [pedirAoTelefone]: um `object` não tem tela.
     *
     * @return `true` se conseguiu abrir. `false` faz o WebView desistir do
     *   pedido — e é melhor do que ficar segurando um pedido que nunca responde.
     */
    var escolherArquivo: ((Array<String>, (Uri?) -> Unit) -> Boolean)? = null

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
        val p = PonteDaMesa(
            aoReceberPedido = aoReceberPedido,
            // 🔴 Saiu pela página: apaga o serviço, e só ele. A janela fica, e a
            // página já se recarregou de volta para a porta de entrada — quem
            // saiu pode entrar outra vez sem sair da aba.
            aoSairDaMesa = {
                janela?.let { ServicoDaMesa.apagar(it.context.applicationContext) }
                // 🔴 E tira a pessoa da aba. Sem isto ela fica olhando para a
                // porta da sala de que acabou de sair, sem saber para onde ir.
                aoSairDaAba?.invoke()
                // ⚠️ O convite volta a valer: entrar de novo pela porta é um ato
                // da pessoa, mas se ela recarregar a página o aplicativo pode
                // convidá-la outra vez sem ela ter de digitar nada.
                jaConvidou = false
            }
        )
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

            /**
             * 🟥 **O `alert` e o `confirm` da página, que sem isto MORREM.**
             *
             * Um `WebChromeClient` que não trate `onJsAlert`/`onJsConfirm` faz o
             * WebView **cancelar** as duas em silêncio. Não é um detalhe de
             * enfeite: a Mesa usa `confirm` em onze lugares, e um deles é o
             * **botão SAIR** (`mesa-conexao.js`). Sem estas linhas, o SAIR não
             * sairia — e a decisão dele é justamente *"fica conectado até dar
             * SAIR"*.
             *
             * ⚠️ Os outros dez também não são pequenos: tirar um boneco do mapa,
             * acabar a luta, apagar uma cena. Todos "não fazem nada", sem erro.
             *
             * 🔴 Só com uma tela na frente. Um diálogo precisa de uma [Activity];
             * pedido de janela despendurada é **cancelado**, que é o mesmo que o
             * navegador faz numa aba escondida.
             */
            override fun onJsAlert(
                janelaDaPagina: WebView?,
                deOnde: String?,
                recado: String?,
                resultado: JsResult?
            ): Boolean {
                val r = resultado ?: return false
                val tela = aTelaDaFrente() ?: run { r.cancel(); return true }
                AlertDialog.Builder(tela)
                    .setMessage(recado.orEmpty())
                    .setPositiveButton("OK") { _, _ -> r.confirm() }
                    // ⚠️ Fechar por fora TAMBÉM responde. Um diálogo dispensado
                    // sem resposta deixa a página esperando para sempre.
                    .setOnCancelListener { r.cancel() }
                    .show()
                return true
            }

            override fun onJsConfirm(
                janelaDaPagina: WebView?,
                deOnde: String?,
                pergunta: String?,
                resultado: JsResult?
            ): Boolean {
                val r = resultado ?: return false
                val tela = aTelaDaFrente() ?: run { r.cancel(); return true }
                AlertDialog.Builder(tela)
                    .setMessage(pergunta.orEmpty())
                    .setPositiveButton("Sim") { _, _ -> r.confirm() }
                    .setNegativeButton("Não") { _, _ -> r.cancel() }
                    .setOnCancelListener { r.cancel() }
                    .show()
                return true
            }

            /**
             * ⚠️ O `prompt` é **recusado**, e não esquecido.
             *
             * A Mesa não usa nenhum hoje. Se um dia usar, ele volta `null` — que
             * a página trata como "a pessoa desistiu" — em vez de ficar pendurado
             * para sempre.
             */
            override fun onJsPrompt(
                janelaDaPagina: WebView?,
                deOnde: String?,
                pergunta: String?,
                porOmissao: String?,
                resultado: JsPromptResult?
            ): Boolean {
                resultado?.cancel()
                return true
            }

            /**
             * **O "escolher arquivo" da página** — MNA-9.
             *
             * A Mesa tem dois: o retrato do personagem e a foto no chat. Sem
             * este gancho, tocar neles **não faz nada** — e nada aparece na tela
             * a dizer porquê.
             *
             * 🟥 **A resposta é obrigatória, mesmo quando é "nada".** Um
             * `onReceiveValue(null)` esquecido não deixa só este pedido pendurado:
             * o WebView passa a **ignorar todos os toques seguintes** naquele
             * campo, para sempre. O campo fica morto e parece defeito da Mesa.
             */
            override fun onShowFileChooser(
                janelaDaPagina: WebView?,
                resposta: ValueCallback<Array<Uri>>?,
                oQueEle: FileChooserParams?
            ): Boolean {
                val responder = resposta ?: return false
                val abrir = escolherArquivo ?: run {
                    // ⚠️ Sem tela na frente. Responde "nada" e devolve `false`:
                    // assim o campo continua vivo para a próxima vez.
                    responder.onReceiveValue(null)
                    return false
                }

                val tipos = oQueEle?.acceptTypes
                    ?.filter { it.isNotBlank() }
                    ?.toTypedArray()
                    // ⚠️ Campo sem `accept` aceita tudo. `arrayOf()` vazio faria o
                    // explorador não mostrar arquivo nenhum.
                    ?.takeIf { it.isNotEmpty() }
                    ?: arrayOf("*/*")

                val abriu = abrir(tipos) { escolhido ->
                    responder.onReceiveValue(
                        if (escolhido != null) arrayOf(escolhido) else null
                    )
                }
                if (!abriu) responder.onReceiveValue(null)
                return abriu
            }
        }

        janela = w
        return w
    }

    /**
     * A tela em que a janela está pendurada agora, ou `null`.
     *
     * 🔴 Um diálogo do Android só nasce de uma [Activity]. Com a janela
     * despendurada, o embrulho aponta para o contexto do aplicativo — e é por
     * isso que este método existe em vez de um `janela.context as Activity`, que
     * estouraria.
     */
    private fun aTelaDaFrente(): Activity? {
        var ctx: Context? = embrulho?.baseContext
        // ⚠️ Um contexto pode vir embrulhado em vários. Desembrulha até achar a
        // tela, ou até não haver mais.
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) return ctx.takeIf { !it.isFinishing }
            ctx = ctx.baseContext
        }
        return null
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
            if (!deu) return@pedirDaTela pedido.deny()
            pedido.grant(conhecidos.toTypedArray())
            // 🟥 MNA-8, a segunda tentativa — e a que costuma pegar.
            //
            // 🔴 O serviço é do tipo `microphone`, e do Android 14 em diante ele
            // só acende com o `RECORD_AUDIO` **já concedido**. A ordem natural é
            // exatamente a errada: a sala sobe primeiro, o microfone vem depois.
            // Aqui a permissão acabou de sair, e é o momento certo.
            ServicoDaMesa.acender(ctx.applicationContext)
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
        // 🟥 MNA-8: a sala passa a valer com o telefone no bolso.
        //
        // ⚠️ Do Android 14 em diante isto costuma NÃO pegar aqui: o serviço é do
        // tipo `microphone`, e o microfone ainda não foi concedido — a pessoa
        // acabou de abrir a aba. Falha em silêncio de propósito, e é tentado
        // outra vez assim que a permissão sai (ver `responderA`).
        ServicoDaMesa.acender(w.context.applicationContext)
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
        // 🔴 O serviço morre com a sala, e só com ela. Trocar de aba, girar o
        // telefone ou ir para o bolso não passam por aqui — é a decisão dele.
        ServicoDaMesa.apagar(w.context.applicationContext)
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
        // ⚠️ Depois de apagar, e não antes: quem trocar de aba durante o apagar
        // encontraria uma janela meio morta.
        aoSairDaAba?.invoke()
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
