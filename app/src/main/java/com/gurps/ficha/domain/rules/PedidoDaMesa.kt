package com.gurps.ficha.domain.rules

/**
 * **O que a Mesa Virtual pediu** — lote CC-5 do `PLANO_COMBATE_CONDUZIDO.md`.
 *
 * > *"O jogador seleciona a parte do corpo no tabuleiro, e automaticamente o
 * > navegador abre o aplicativo, aí ele faz o ataque lá."*
 *
 * 🔴 A Mesa não consegue empurrar nada para o telefone — o navegador só salta
 * para um aplicativo **num toque do dedo**, e isso é regra do Android. O que ela
 * faz é montar um link; quem toca nele é a pessoa, e o aplicativo abre já com a
 * rolagem armada.
 *
 * ```
 * gurpsapp://rolar?o=ataque&pericia=espada_larga&mod=-7&onde=no%20crânio&alvo=Orc
 * ```
 *
 * ## 🔴 O token da sala NÃO viaja aqui
 *
 * Um link é texto: ele fica no histórico do navegador, nos registos, e em
 * qualquer coisa que veja a URL. O aplicativo **já sabe** a que mesa está ligado
 * — foi a pessoa que a configurou — e é de lá que o token sai na hora de mandar
 * a rolagem de volta.
 *
 * ## ⚠️ Tudo o que chega aqui é de fora
 *
 * O link pode ter sido escrito por qualquer um. Este arquivo trata tudo como
 * texto suspeito: corta o que é comprido demais, prende o modificador a uma
 * faixa que o livro admite, e ignora o que não conhece. **Nada daqui manda no
 * aplicativo** — é só o que ele mostra e o que ele preenche.
 */
object PedidoDaMesa {

    /** O esquema que o `AndroidManifest.xml` regista. */
    const val ESQUEMA = "gurpsapp"

    /** O caminho deste pedido. O `conectar` é de outro lote e não passa por aqui. */
    const val ANFITRIAO = "rolar"

    /**
     * ⚠️ O mesmo teto do aplicativo (`coerceIn(-20, 20)` no `FichaCombatDelegate`).
     * Um `+999` vindo de um link daria um NH que nenhuma tela mostra.
     */
    const val MOD_MAXIMO = 20

    /** Um texto de fora nunca é maior do que isto. */
    private const val TETO_DO_TEXTO = 60

    /** O que a Mesa pode pedir. */
    enum class Oque(val rotulo: String) {
        ATAQUE("Atacar"),
        DEFESA("Defender"),
        DANO("Rolar o dano")
    }

    /**
     * Um pedido já limpo.
     *
     * @param pericia o `definicaoId` da perícia, quando a Mesa escolheu uma
     * @param mod o modificador que a Mesa já somou (mira, distância, situação)
     * @param onde a parte do corpo, **em português e para ler** — não é um id
     * @param alvo o nome do boneco alvo, para a pessoa saber em quem está batendo
     * @param acao o id da ação na fila da Mesa, para dizer depois que foi feita
     */
    data class Pedido(
        val oQue: Oque,
        val pericia: String?,
        val mod: Int,
        val onde: String?,
        val alvo: String?,
        val acao: String?
    ) {
        /**
         * A frase que a aba Rolagem mostra.
         *
         * ⚠️ Ela diz **de onde veio**. Um modificador que aparece sozinho num
         * campo parece defeito, e a pessoa apaga-o — e aí a Mesa e o telefone
         * passam a dizer números diferentes para o mesmo golpe.
         */
        fun frase(): String {
            val partes = mutableListOf(oQue.rotulo)
            if (alvo != null) partes += "em $alvo"
            if (onde != null) partes += onde
            val cabeca = partes.joinToString(" ")
            return if (mod != 0) "$cabeca (${comSinal(mod)})" else cabeca
        }
    }

    fun comSinal(n: Int): String = if (n > 0) "+$n" else n.toString()

    /**
     * **Ler o link.**
     *
     * @param uri o endereço inteiro, como o Android o entrega
     * @return o pedido, ou `null` quando o link não é deste tipo
     *
     * 🔴 Devolver `null` é o normal, e não um erro: o mesmo esquema serve o
     * `gurpsapp://conectar` de outro lote, e um link estragado tem de deixar o
     * aplicativo abrir na tela de sempre — e não numa mensagem de erro.
     */
    fun ler(uri: String?): Pedido? {
        val cru = uri?.trim() ?: return null
        val prefixo = "$ESQUEMA://$ANFITRIAO"

        // ⚠️ **A barra depois do host**, quando ela vier.
        //
        // O `am start` do Android mostra o endereço como `gurpsapp://rolar/...`,
        // e eu escrevi isto a achar que era essa a causa de o link não pegar. NÃO
        // era: o registo do aparelho mostrou o endereço a chegar inteiro e sem
        // barra, e o defeito estava noutro lado (ver a `FichaScreen`).
        //
        // 🔴 Fica na mesma, e de propósito: um `Uri` pode trazer o caminho `/`
        // conforme quem o monta, e recusá-lo faria o aplicativo abrir na aba de
        // sempre sem dizer porquê. Mas o comentário diz a verdade — eu tinha
        // escrito aqui uma causa que nunca confirmei.
        val semBarra = if (cru.startsWith("$prefixo/")) {
            prefixo + cru.substring(prefixo.length + 1)
        } else cru

        if (!semBarra.startsWith("$prefixo?") && semBarra != prefixo) return null

        val campos = camposDe(semBarra.substringAfter('?', ""))
        val oQue = when (campos["o"]?.lowercase()) {
            "ataque" -> Oque.ATAQUE
            "defesa" -> Oque.DEFESA
            "dano" -> Oque.DANO
            // ⚠️ Sem dizer o que é, não há pedido. Escolher um por omissão poria
            // a pessoa a rolar dano quando a Mesa pediu um ataque.
            else -> return null
        }

        return Pedido(
            oQue = oQue,
            pericia = texto(campos["pericia"])?.lowercase(),
            mod = numero(campos["mod"]),
            onde = texto(campos["onde"]),
            alvo = texto(campos["alvo"]),
            acao = texto(campos["acao"])
        )
    }

    /**
     * `a=1&b=2` em pares.
     *
     * ⚠️ Escrito à mão para este arquivo poder ser provado **sem Android**: o
     * `android.net.Uri` só existe no aparelho, e um teste que precisasse dele
     * seria um teste que ninguém corre.
     */
    private fun camposDe(consulta: String): Map<String, String> {
        if (consulta.isBlank()) return emptyMap()
        val fora = HashMap<String, String>()
        consulta.split('&').forEach { par ->
            val i = par.indexOf('=')
            if (i <= 0) return@forEach
            val chave = par.substring(0, i)
            // ⚠️ Só o primeiro de cada chave. Um link com `mod=0&mod=-99`
            // aceitaria o último, e um link torto não escolhe por nós.
            if (!fora.containsKey(chave)) fora[chave] = desescapar(par.substring(i + 1))
        }
        return fora
    }

    /** `%C3%A2` volta a ser `â`, e `+` volta a ser espaço. */
    private fun desescapar(cru: String): String {
        val bytes = ArrayList<Byte>(cru.length)
        var i = 0
        while (i < cru.length) {
            val c = cru[i]
            when {
                c == '+' -> { bytes += ' '.code.toByte(); i++ }
                c == '%' && i + 2 < cru.length -> {
                    val hex = cru.substring(i + 1, i + 3).toIntOrNull(16)
                    if (hex == null) { bytes += c.code.toByte(); i++ }
                    else { bytes += hex.toByte(); i += 3 }
                }
                else -> {
                    // ⚠️ Um caractere já legível pode ocupar mais de um byte.
                    c.toString().toByteArray(Charsets.UTF_8).forEach { bytes += it }
                    i++
                }
            }
        }
        return String(bytes.toByteArray(), Charsets.UTF_8)
    }

    /** Texto de fora: sem espaços nas pontas, cortado, e vazio vira nulo. */
    private fun texto(cru: String?): String? {
        val limpo = cru?.trim()?.take(TETO_DO_TEXTO) ?: return null
        return limpo.ifBlank { null }
    }

    /**
     * O modificador, preso entre −20 e +20.
     *
     * ⚠️ O que não é número é **zero**, e não um pedido recusado: perder o
     * ataque inteiro por causa de um campo torto é pior do que rolar sem o
     * modificador — a pessoa vê a frase e corrige à mão.
     */
    private fun numero(cru: String?): Int {
        val n = cru?.trim()?.toIntOrNull() ?: return 0
        return n.coerceIn(-MOD_MAXIMO, MOD_MAXIMO)
    }
}
