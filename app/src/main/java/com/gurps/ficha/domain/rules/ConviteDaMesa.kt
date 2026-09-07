package com.gurps.ficha.domain.rules

import java.net.URLEncoder

/**
 * **Entrar na sala sozinho** — lote MNA-3 do `PLANO_MESA_NO_APP.md`.
 *
 * O nome vem da ficha e o token já está guardado no aplicativo. A aba não tem
 * por que perguntar nada.
 *
 * ## 🔴 O token NÃO vai no endereço com que a aba abre
 *
 * O caminho do navegador monta `…/#nome=Cesar&t=ABC` e manda. Funciona, e para o
 * navegador é o certo — é assim que um convite viaja. Mas dentro do aplicativo
 * não há convite nenhum a viajar: o aplicativo **já sabe** a que mesa está
 * ligado.
 *
 * Então a aba abre no endereço limpo, e o convite entra **depois**, por dentro.
 * É a mesma decisão que o [PedidoDaMesa] já tinha tomado uma vez, pelo mesmo
 * motivo: um endereço fica no histórico da janela e em qualquer registro que
 * veja a URL.
 *
 * ## 🟥 E por que pelo `#`, e não chamando o `conectar` direto
 *
 * O `conectar` da página é uma função global, e chamá-la parece mais direto. É
 * pior, e por duas razões que já custaram caro do outro lado:
 *
 * 1. O `entrarPeloConvite` do `convite.js` **preenche os campos** antes de
 *    conectar. Se a sala recusar — token trocado, nome em uso —, a pessoa fica na
 *    tela de sempre com o que ela ia digitar já lá, em vez de um formulário vazio
 *    e um erro solto.
 * 2. O `ouvirOsConvites` já sabe os **três** casos: ninguém dentro entra; quem já
 *    está dentro com o mesmo nome não é mexido; quem está dentro com outro nome
 *    vê a porta com os campos preenchidos e decide. Entrar por cima de alguém
 *    seria tirar da mesa, no meio de uma cena, quem não pediu.
 *
 * ⚠️ Mexer no `#` de uma página **não a recarrega**. Ela ouve o `hashchange` e
 * trata — e a primeira coisa que ela faz é limpar o endereço.
 *
 * ## ⚠️ Kotlin puro
 *
 * Nada aqui é do Android — o `URLEncoder` é da biblioteca padrão. É o que deixa
 * a parte perigosa (um nome com acento, com espaço, ou com uma aspa dentro) ser
 * provada na bancada, e não no aparelho.
 */
object ConviteDaMesa {

    /** O mesmo teto do nome que a página usa (`convite.js`). */
    private const val TETO_DO_NOME = 60

    /** O mesmo teto do token que a página usa. */
    private const val TETO_DO_TOKEN = 32

    /**
     * O fragmento do convite, ou `null` quando falta nome ou token.
     *
     * 🔴 Devolver `null` é o normal: a aba nem sequer aparece sem token, mas o
     * token pode ser apagado com a aba aberta, e aí não há convite nenhum a
     * montar.
     */
    fun oFragmento(nome: String?, token: String?): String? {
        val n = nome?.trim()?.take(TETO_DO_NOME).orEmpty()
        // ⚠️ Maiúsculas: o token é comparado byte a byte do lado da sala, e é a
        // mesma limpeza que o campo da tela faz.
        val t = token?.trim()?.uppercase()?.take(TETO_DO_TOKEN).orEmpty()
        if (n.isBlank() || t.isBlank()) return null
        return "#nome=" + escapar(n) + "&t=" + escapar(t)
    }

    /**
     * **A linha que entra na página.**
     *
     * @return `null` quando não há convite a dar — e aí não se manda nada.
     *
     * 🟥 O valor vai **percent-escapado**, e é isso que o torna seguro de meter
     * dentro de uma linha de JavaScript: depois de escapado não sobra aspa,
     * barra invertida nem quebra de linha para fechar o texto cedo demais. Um
     * personagem chamado `O'Brien` seria, sem isso, uma linha de código partida
     * ao meio.
     */
    fun oJavascript(nome: String?, token: String?): String? {
        val fragmento = oFragmento(nome, token) ?: return null
        // ⚠️ `location.hash = …`, e não `location.href = …`: o segundo pode
        // recarregar a página, e recarregar é perder a sessão e a voz.
        return "location.hash = '$fragmento';"
    }

    /**
     * ⚠️ O `URLEncoder` escreve espaço como `+`. É o que a página espera: ela lê
     * o fragmento com `URLSearchParams`, que desfaz o `+` de volta em espaço.
     */
    private fun escapar(valor: String): String = URLEncoder.encode(valor, "UTF-8")
}
