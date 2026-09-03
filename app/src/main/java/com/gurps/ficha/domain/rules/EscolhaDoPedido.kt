package com.gurps.ficha.domain.rules

/**
 * **Qual rolagem responde ao que a Mesa pediu** — lote CC-6.
 *
 * > *"Não seria mais fácil, já que você tem todas as informações da ficha,
 * > simplesmente abrir na tela, o cara clica, rola o teste, aparece no chat?"*
 *
 * 🔴 Ele tem razão, e este arquivo é o que torna isso possível: dado o pedido da
 * Mesa e as rolagens que o personagem tem, ele diz **qual é a certa** — ou, se
 * não houver uma certa, quais são as candidatas para a pessoa escolher.
 *
 * ## ⚠️ Ele não rola nada, e não sabe desenhar
 *
 * É Kotlin puro, para poder ser provado sem aparelho. Quem rola é a aba Rolagem,
 * com a mesma máquina de sempre — um segundo caminho de rolagem seria um segundo
 * sítio onde a regra de crítico podia divergir.
 */
object EscolhaDoPedido {

    /**
     * O que a tela precisa saber.
     *
     * @param escolhida a rolagem que casa com o que a Mesa pediu, ou `null`
     * @param candidatas o que oferecer quando não há uma certa
     * @param porqueNaoCasou dito em português, para a tela não ficar muda
     */
    data class Resposta(
        val escolhida: Opcao?,
        val candidatas: List<Opcao>,
        val porqueNaoCasou: String?
    )

    /** Uma rolagem possível — o mínimo que este arquivo precisa de saber dela. */
    data class Opcao(val id: String, val rotulo: String, val nh: Int?)

    /**
     * **Achar a rolagem certa.**
     *
     * 🔴 A Mesa manda o `definicaoId` da perícia (`espada_larga`), e as opções da
     * tela têm ids como `pericia_espada_larga_` — com a especialização colada
     * atrás. Casar por *começa com* seria frágil; casar pelo **pedaço do meio** é
     * o que corresponde à forma como o id é montado.
     *
     * ⚠️ Quando não casa, **não se escolhe uma por acaso**: devolve-se a lista
     * inteira e a pessoa decide. Um ataque saído da perícia errada é um número
     * plausível, e ninguém repara.
     */
    fun escolher(pedido: PedidoDaMesa.Pedido, opcoes: List<Opcao>): Resposta {
        if (opcoes.isEmpty()) {
            return Resposta(null, emptyList(),
                "Este boneco não tem nenhuma rolagem de combate na ficha.")
        }
        val quer = pedido.pericia?.trim()?.lowercase()
        if (quer.isNullOrBlank()) {
            return Resposta(null, opcoes, "A Mesa não disse com qual perícia.")
        }

        val casam = opcoes.filter { pedacosDoId(it.id).contains(quer) }
        return when {
            casam.size == 1 -> Resposta(casam[0], opcoes, null)
            // ⚠️ Duas com o mesmo id e especializações diferentes (duas Espadas
            // Largas) — a Mesa não disse qual, e adivinhar erraria metade das
            // vezes. Vão as duas para a tela.
            casam.size > 1 -> Resposta(null, casam,
                "Há mais de uma \"$quer\" nesta ficha.")
            else -> Resposta(null, opcoes,
                "Este boneco não tem \"$quer\" na ficha.")
        }
    }

    /**
     * `pericia_espada_larga_` vira `[pericia, espada, larga, espada_larga]`.
     *
     * 🔴 O `espada_larga` inteiro entra na lista **além** dos pedaços soltos: é
     * ele que a Mesa manda, e sem ele um id de duas palavras nunca casaria.
     */
    private fun pedacosDoId(id: String): Set<String> {
        // 🟥 Só o `pericia_`. Eu tirava o `ataque_` também, a pensar em ids do
        // tipo `ataque_alguma_coisa` — e isso comia o nome da perícia: o
        // `ataque_inato` do César virava `inato`, e o pedido da Mesa deixava de
        // casar com a única rolagem que ele tem.
        val limpo = id.lowercase().removePrefix("pericia_").trimEnd('_')
        val partes = limpo.split('_')
        val fora = mutableSetOf(limpo)
        // Cada prefixo: `espada`, `espada_larga`, `espada_larga_direita`. É
        // assim que se casa um id com especialização colada atrás.
        for (quantas in partes.size downTo 1) {
            fora += partes.take(quantas).joinToString("_")
        }
        return fora
    }

    /**
     * A linha que vai para a conversa da mesa.
     *
     * ⚠️ Ela leva **a parte do corpo e o alvo**, porque é o que faz a jogada ser
     * lida como uma jogada e não como um número solto. *"Espada Larga"* e
     * *"Espada Larga em Orc no crânio"* são coisas diferentes para quem lê o
     * chat depois.
     */
    fun rotuloDaJogada(pedido: PedidoDaMesa.Pedido, escolhida: Opcao?): String {
        val partes = mutableListOf<String>()
        partes += escolhida?.rotulo ?: pedido.oQue.rotulo
        pedido.alvo?.let { partes += "em $it" }
        pedido.onde?.let { partes += it }
        return partes.joinToString(" ")
    }
}
