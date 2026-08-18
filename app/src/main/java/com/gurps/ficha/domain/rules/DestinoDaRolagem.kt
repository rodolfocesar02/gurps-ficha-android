package com.gurps.ficha.domain.rules

/**
 * **Para onde a rolagem vai** — Lote MESA-7.
 *
 * ## O que o usuário pediu, e o que ele NÃO pediu
 *
 * *"não vamos substituir o nosso botão, vamos acrescentar um novo; aí podemos
 * pelo app escolher qual o servidor vamos usar."*
 *
 * Ou seja: o Discord **fica**. A Mesa entra ao lado. E isso não é só gentileza
 * com o que já existe — é a rede de segurança da mesa: se a sala do PC cair no
 * meio da sessão, um toque devolve as rolagens ao Discord, que já está de pé
 * há meses.
 *
 * ⚠️ Por isso **[NENHUM] existe**. Sem ele, um app recém-instalado teria de
 * chutar um destino, e chutaria errado na metade das vezes — mandando a rolagem
 * para um servidor que aquele jogador nunca configurou.
 */
enum class DestinoDaRolagem(val rotulo: String, val descricao: String) {

    /** Nada configurado ainda: a rolagem acontece na tela e não sai do aparelho. */
    NENHUM(
        "Só no aparelho",
        "A rolagem aparece aqui e não é enviada para lugar nenhum."
    ),

    DISCORD(
        "Discord",
        "Vai para o canal do bot, como sempre foi."
    ),

    /** A sala do PC do Mestre — o servidor `mesa-virtual`. */
    MESA(
        "Mesa virtual",
        "Vai para o chat da sala, junto com a conversa."
    );

    companion object {
        /**
         * O destino guardado, com tolerância a lixo.
         *
         * ⚠️ Ficha antiga não tem este campo, e um valor renomeado no futuro não
         * pode derrubar a rolagem: no escuro, **não envia**. Mandar para o lugar
         * errado é pior do que não mandar — a mesa inteira lê.
         */
        fun de(guardado: String?): DestinoDaRolagem =
            entries.firstOrNull { it.name == guardado } ?: NENHUM
    }
}

/**
 * **O que precisa estar preenchido para cada destino.**
 *
 * Separado do enum porque é **regra de configuração**, não identidade: o mesmo
 * destino pode estar pronto num aparelho e faltando dado noutro.
 */
object ProntidaoDoDestino {

    /** O que falta para poder enviar, ou null quando está tudo pronto. */
    fun oQueFalta(
        destino: DestinoDaRolagem,
        canalDoDiscord: String?,
        enderecoDaMesa: String?,
        tokenDaMesa: String?
    ): String? = when (destino) {
        DestinoDaRolagem.NENHUM -> null

        DestinoDaRolagem.DISCORD ->
            if (canalDoDiscord.isNullOrBlank()) "Escolha o canal do Discord." else null

        DestinoDaRolagem.MESA -> when {
            enderecoDaMesa.isNullOrBlank() -> "Falta o endereço da sala."
            tokenDaMesa.isNullOrBlank() -> "Falta o token da sala."
            else -> null
        }
    }

    /**
     * O endereço da sala, arrumado.
     *
     * ⚠️ O jogador vai digitar isto no celular, e vai digitar de todo jeito:
     * com `https://` ou sem, com barra no fim ou sem, com espaço colado do
     * copiar-colar. Recusar por causa de uma barra seria transformar um detalhe
     * de digitação num "não funciona" no meio da sessão.
     */
    fun enderecoLimpo(cru: String?): String? {
        val texto = cru?.trim()?.trimEnd('/') ?: return null
        if (texto.isBlank()) return null
        return if (texto.startsWith("http://") || texto.startsWith("https://")) texto
        else "https://$texto"
    }
}
