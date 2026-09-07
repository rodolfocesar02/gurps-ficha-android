package com.gurps.ficha.domain.rules

/**
 * **Para onde a aba da Mesa pode ir** — lote MNA-2 do `PLANO_MESA_NO_APP.md`.
 *
 * A aba nova é um navegador dentro do aplicativo. E um navegador vai a qualquer
 * lugar — é para isso que ele serve.
 *
 * ## 🟥 Por que isso não pode ficar assim
 *
 * A ponte (MNA-4) dá **à página** o direito de abrir diálogos do aplicativo e de
 * ler a ficha. Esse direito não é do endereço: é da janela. Se a janela puder
 * navegar para fora da Mesa, o direito vai junto para onde quer que ela vá — e
 * um anúncio, um link no chat ou uma página enganada passam a poder pedir a
 * ficha.
 *
 * Por isso a aba só vai a **um** lugar, e este arquivo é quem diz qual.
 *
 * ## 🔴 A cerca vem ANTES da ponte, e não depois
 *
 * É a ordem dos lotes de propósito. Construir a ponte primeiro e cercar depois
 * deixaria uma versão do aplicativo com o direito dado e sem cerca — e é sempre
 * essa a versão que fica instalada no telefone de alguém.
 *
 * ## ⚠️ Kotlin puro, sem Android
 *
 * Nada aqui importa `android.net.Uri`, pela mesma razão do [PedidoDaMesa]: o
 * `Uri` só existe no aparelho, e um teste que precisasse dele seria um teste que
 * ninguém roda. O endereço é lido à mão, e a leitura é o que os testes provam.
 */
object EnderecoDaMesa {

    /** O que fazer com um endereço que a página tentou abrir. */
    enum class OQueFazer {
        /** É a Mesa. Deixa ir. */
        SEGUIR,

        /**
         * É um pedido para o aplicativo (`gurpsapp://…`).
         *
         * 🔴 **Não é navegação**, e não pode ser tratado como recusa: quem
         * recebe isto tem de entregar o endereço ao [PedidoDaMesa] e abrir o
         * diálogo. Um `RECUSAR` aqui faria o botão "Atacar" do tabuleiro ficar
         * mudo — que é exatamente o defeito que a aba veio consertar.
         */
        E_UM_PEDIDO,

        /** Qualquer outro lugar. A aba não vai. */
        RECUSAR
    }

    /**
     * Um endereço de fora nunca é maior do que isto.
     *
     * ⚠️ O mesmo espírito do teto de texto do [PedidoDaMesa]: o que chega aqui
     * pode ter sido escrito por qualquer um.
     */
    private const val TETO_DO_ENDERECO = 2048

    /**
     * **O que fazer com este endereço.**
     *
     * @param endereco o que a página quer abrir
     * @param base o endereço da Mesa (`MesaApiClient.ENDERECO_PADRAO`)
     *
     * 🔴 Compara a **origem** — esquema, dono do endereço e porta —, e não o
     * começo do texto. Comparar por `startsWith` deixaria passar
     * `https://mesagurps.duckdns.org.enganar.com`, que começa igual e é outro
     * lugar inteiramente.
     */
    fun oQueFazerCom(endereco: String?, base: String): OQueFazer {
        val cru = endereco?.trim()?.take(TETO_DO_ENDERECO).orEmpty()
        if (cru.isBlank()) return OQueFazer.RECUSAR

        // 🔴 O pedido do aplicativo vem primeiro: ele não é um lugar, é um
        // recado. Ver `E_UM_PEDIDO`.
        if (cru.startsWith("${PedidoDaMesa.ESQUEMA}://", ignoreCase = true)) {
            return OQueFazer.E_UM_PEDIDO
        }

        val daPagina = origemDe(cru) ?: return OQueFazer.RECUSAR
        val daMesa = origemDe(base) ?: return OQueFazer.RECUSAR
        return if (daPagina == daMesa) OQueFazer.SEGUIR else OQueFazer.RECUSAR
    }

    /**
     * A origem de um endereço: `esquema://dono:porta`, tudo em minúsculas.
     *
     * @return `null` quando não dá para dizer qual é — e aí quem chama recusa.
     *
     * ⚠️ Só `http` e `https`. Um `file://` apontaria para dentro do telefone, e
     * um `javascript:` seria a página se mandando a si mesma — nenhum dos dois é
     * a Mesa, e nenhum dos dois tem "dono do endereço" para comparar.
     */
    private fun origemDe(cru: String?): String? {
        val texto = cru?.trim() ?: return null
        val corte = texto.indexOf("://")
        if (corte <= 0) return null

        val esquema = texto.substring(0, corte).lowercase()
        if (esquema != "http" && esquema != "https") return null

        // O que vem depois de `://` até a primeira `/`, `?` ou `#`.
        val resto = texto.substring(corte + 3)
        val fim = resto.indexOfFirst { it == '/' || it == '?' || it == '#' }
        val autoridade = if (fim >= 0) resto.substring(0, fim) else resto

        // ⚠️ `usuario:senha@dono` — o dono é o que vem **depois** do `@`. Sem
        // esta linha, `https://mesagurps.duckdns.org@enganar.com` teria a Mesa
        // como dono do endereço, e ela é só o nome de usuário.
        val semLogin = autoridade.substringAfterLast('@')
        if (semLogin.isBlank()) return null

        val padrao = if (esquema == "https") "443" else "80"
        // ⚠️ Endereço em IPv6 (`[::1]:8080`) traz `:` dentro dos colchetes. A
        // porta é o que vem depois do ÚLTIMO `:`, e só quando ele está fora dos
        // colchetes.
        val ultimo = semLogin.lastIndexOf(':')
        val temPorta = ultimo > semLogin.lastIndexOf(']')
        val dono = if (temPorta) semLogin.substring(0, ultimo) else semLogin
        val porta = if (temPorta) semLogin.substring(ultimo + 1) else padrao
        if (dono.isBlank()) return null

        return "$esquema://${dono.lowercase()}:${porta.ifBlank { padrao }}"
    }

    /**
     * **O endereço com que a aba abre.**
     *
     * 🔴 Sem o nome e sem o token. Eles vão por dentro, depois de a página
     * carregar (MNA-3): um endereço fica no histórico da janela e em qualquer
     * registo que veja a URL, e o [PedidoDaMesa] já tinha tomado esta decisão
     * uma vez, pelo mesmo motivo.
     */
    fun paraAbrir(base: String): String = base.trim().trimEnd('/')
}
