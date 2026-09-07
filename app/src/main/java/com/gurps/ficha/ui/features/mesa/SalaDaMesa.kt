package com.gurps.ficha.ui.features.mesa

import android.annotation.SuppressLint
import android.content.Context
import android.content.MutableContextWrapper
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.gurps.ficha.domain.rules.EnderecoDaMesa

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
 * - Não entra na sala sozinho (MNA-3).
 * - Não tem ponte (MNA-4), e por isso o `E_UM_PEDIDO` ainda não abre diálogo
 *   nenhum: por enquanto ele só **não navega**, que já é melhor do que a página
 *   ir a lado nenhum com um erro na cara.
 * - Não entrega microfone nem câmera (MNA-7 e MNA-9). A página vai pedir e ficar
 *   esperando, **calada** — é assim que um `WebView` se comporta por omissão, e
 *   é o defeito que aqueles lotes consertam.
 * - Não sobrevive ao aplicativo ir para o bolso (MNA-8).
 */
object SalaDaMesa {

    /** A janela, enquanto ela existir. */
    private var janela: WebView? = null

    /** O embrulho que troca de dono. Nasce com a janela e morre com ela. */
    private var embrulho: MutableContextWrapper? = null

    /** O endereço a que esta sala está presa. Ver [EnderecoDaMesa]. */
    private var enderecoDaSala: String = ""

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
    fun aJanela(dono: Context, endereco: String): WebView {
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

        w.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean = decidir(request?.url?.toString())

            @Deprecated("Só para Android 6 e anteriores; o app vai até o 24.")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean =
                decidir(url)
        }

        janela = w
        return w
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
            // ⚠️ Ainda sem ponte (MNA-4). Por agora só não navega: o pedido é
            // engolido, e o botão do tabuleiro continua sem fazer nada — o que
            // já era verdade antes deste lote, e passa a ser verdade **sem**
            // levar a página a um erro.
            EnderecoDaMesa.OQueFazer.E_UM_PEDIDO -> true
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
