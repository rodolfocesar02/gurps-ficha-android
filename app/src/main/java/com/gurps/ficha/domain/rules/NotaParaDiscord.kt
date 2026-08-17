package com.gurps.ficha.domain.rules

import com.gurps.ficha.data.network.DiscordRollPayload
import com.gurps.ficha.model.NotaDeJogo

/**
 * **Mandar uma nota para o Discord** — Lote NOTA-1.
 *
 * ## ⚠️ Por que a nota viaja no envelope de uma rolagem
 *
 * O servidor tem **um endpoint só**, `/api/rolls`, e ele recebe
 * [DiscordRollPayload]. Não existe rota de "mandar texto". Então a nota entra
 * naquele envelope, com `testType = "Nota"` para o outro lado distinguir.
 *
 * Isto é uma **adaptação, não a forma certa**. A forma certa seria uma rota
 * própria no bot; enquanto ela não existe, o app não pode fingir que existe nem
 * deixar de mandar. O que dá para garantir daqui é que o pacote seja honesto:
 * `dice` vazio, `total` 0 e `target` nulo — nada que faça a nota **parecer** uma
 * rolagem que aconteceu.
 *
 * 🔴 Se um dia alguém puser dados aqui para "ficar bonito no Discord", a mesa vai
 * ver um resultado que ninguém rolou. Há um teste guardando cada um dos três.
 *
 * ## O texto
 *
 * O título vai em `context` e o corpo em `outcome`, porque é assim que o bot
 * monta a linha: contexto primeiro, resultado depois. Nota sem texto não é
 * enviada — [payloadDe] devolve null e a tela nem oferece o botão.
 */
object NotaParaDiscord {

    /** O rótulo que diz ao outro lado que isto não é rolagem. */
    const val TIPO = "Nota"

    /**
     * O pacote da nota, ou null quando não há o que mandar.
     *
     * ⚠️ Nota em branco devolve null de propósito: mandar uma linha vazia para o
     * canal da mesa é pior do que não mandar nada.
     */
    fun payloadDe(
        nomeDoPersonagem: String,
        nota: NotaDeJogo,
        canalId: String? = null
    ): DiscordRollPayload? {
        val texto = nota.texto.trim()
        if (texto.isEmpty()) return null
        return DiscordRollPayload(
            character = nomeDoPersonagem.ifBlank { "Sem nome" },
            testType = TIPO,
            context = nota.titulo,
            // 🔴 Os três que mantêm a nota honesta. Ver o comentário da classe.
            target = null,
            modifier = 0,
            dice = emptyList(),
            total = 0,
            outcome = texto,
            margin = null,
            channelId = canalId
        )
    }

    /** A pergunta do diálogo de confirmação, com uma prévia do que vai sair. */
    fun perguntaDeConfirmacao(nota: NotaDeJogo): String {
        val texto = nota.texto.trim()
        val previa = if (texto.length > 120) texto.take(120) + "…" else texto
        return "Enviar esta anotação para o Discord?\n\n$previa"
    }
}
