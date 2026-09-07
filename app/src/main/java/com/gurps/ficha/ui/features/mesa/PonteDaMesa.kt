package com.gurps.ficha.ui.features.mesa

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import com.gurps.ficha.domain.rules.PedidoDaMesa

/**
 * **A ponte, nos dois sentidos** — lote MNA-4 do `PLANO_MESA_NO_APP.md`.
 *
 * Fora do aplicativo, a Mesa fala com a ficha por um endereço `gurpsapp://`: ela
 * monta um link, você toca nele, o Android abre o aplicativo. É de mão única e
 * leva a página junto — e por isso o `campo-alvo-ui.js` teve de aprender a abrir
 * o link **noutra aba**, para não perder a mesa no salto.
 *
 * Dentro do aplicativo não há salto nenhum a dar: a página e a ficha estão no
 * mesmo lugar. Esta classe é o corrimão entre as duas.
 *
 * ## 🟥 O contrato é o MESMO do CC-8
 *
 * O que atravessa a ponte é um [PedidoDaMesa.Pedido] — quem, contra quem, a que
 * distância, em que parte do corpo. **Não é contrato novo**, e isso é decisão do
 * plano: um segundo contrato entre os mesmos dois programas seria a garantia de
 * que um dia eles discordam, e de que ninguém repara.
 *
 * ## 🔴 Duas portas, e a de trás é a que já funciona
 *
 * 1. **Pelo endereço.** A página faz o que sempre fez — monta o `gurpsapp://` e
 *    manda. O [SalaDaMesa] intercepta antes de navegar e entrega aqui.
 *    ⚠️ **Esta porta funciona com a Mesa exatamente como ela está hoje**, sem uma
 *    linha mudada do lado da página. É o que faz o botão "Atacar" deixar de ser
 *    mudo.
 * 2. **Pela ponte.** A página pergunta `if (window.Ficha)` e chama
 *    [pedido] direto. É mais limpa, dispensa montar um endereço para o desmontar
 *    a seguir, e é para onde os lotes seguintes vão.
 *
 * 🟥 As duas terminam na **mesma** função, de propósito. Duas entradas com dois
 * tratamentos seriam duas regras a divergir.
 *
 * ## ⚠️ O que chega aqui continua vindo de fora
 *
 * Estar dentro do aplicativo não torna o texto confiável: quem escreve é a
 * página, e a página é JavaScript servido pela rede. Tudo passa pelo
 * [PedidoDaMesa], que já corta o que é comprido demais, prende o modificador na
 * faixa que o livro admite, e ignora o que não conhece.
 *
 * @param aoReceberPedido o que fazer com um pedido já limpo. Corre **sempre** na
 *   linha principal — ver [entregar].
 */
class PonteDaMesa(private val aoReceberPedido: (PedidoDaMesa.Pedido) -> Unit) {

    /**
     * **A página pede uma rolagem.**
     *
     * @param link o endereço `gurpsapp://rolar?…`, inteiro
     *
     * 🔴 Recebe o endereço inteiro, e não os campos separados. É o que mantém o
     * contrato num lugar só: se um dia o pedido ganhar um campo, ele é lido pelo
     * [PedidoDaMesa] e chega aqui sem esta classe saber que ele existe.
     */
    @JavascriptInterface
    fun pedido(link: String?) {
        entregar(link)
    }

    /**
     * Entrega um pedido, venha ele da ponte ou do endereço interceptado.
     *
     * 🟥 **Volta para a linha principal antes de tocar em qualquer coisa.** Um
     * método com `@JavascriptInterface` corre na linha do JavaScript, e não na
     * do desenho. Escrever daí um estado que a tela lê é o tipo de defeito que
     * funciona nove vezes em dez e falha na décima, sem deixar rasto.
     */
    fun entregar(link: String?) {
        val pedido = PedidoDaMesa.ler(link) ?: return
        Handler(Looper.getMainLooper()).post { aoReceberPedido(pedido) }
    }

    companion object {
        /**
         * O nome pelo qual a página conhece a ponte: `window.Ficha`.
         *
         * ⚠️ Se ele mudar aqui, muda no `dentro-do-aplicativo.js` da Mesa — e o
         * `else` do lado de lá é que segura o Mestre, que está no PC.
         */
        const val NOME_NA_PAGINA = "Ficha"
    }
}
