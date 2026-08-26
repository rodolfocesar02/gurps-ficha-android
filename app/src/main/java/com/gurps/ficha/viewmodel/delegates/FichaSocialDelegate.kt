package com.gurps.ficha.viewmodel.delegates

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gurps.ficha.data.network.DiscordRollPayload
import com.gurps.ficha.data.network.MesaApiClient
import com.gurps.ficha.data.network.MesaRollPayload
import com.gurps.ficha.domain.rules.DestinoDaRolagem
import com.gurps.ficha.domain.rules.ProntidaoDoDestino
import com.gurps.ficha.data.network.DiscordVoiceChannel
import com.gurps.ficha.viewmodel.RollDispatchStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * O que o botao "CONECTAR A MESA" precisa de saber -- MESA-44.
 *
 * 🔴 Duas coisas separadas: **deu certo** e **o que dizer a pessoa**. A primeira
 * versao disto devolvia so a frase, e a tela adivinhava o resto procurando
 * "erro" e "nao" dentro dela.
 *
 * ⚠️ Adivinhar pela frase parecia funcionar e nao funcionava: "Token errado" e
 * "Falta o endereco da sala" passavam as duas pela peneira, e o navegador abria
 * na mesma. Foi medido antes de ir para o aparelho, correndo a heuristica contra
 * as quatro frases de verdade.
 */
data class ResultadoDaConexao(val ok: Boolean, val recado: String)

class FichaSocialDelegate(
    private val networkDelegate: FichaNetworkDelegate,
    private val configPrefs: SharedPreferences,
    private val scope: CoroutineScope
) {
    private val prefCanalDiscordId = "discord_canal_id"
    private val prefCanalDiscordNome = "discord_canal_nome"

    // Lote MESA-7: o segundo destino. ⚠️ Chaves NOVAS, sem tocar nas do
    // Discord -- quem já usava o bot não perde a configuração ao atualizar.
    private val prefDestino = "rolagem_destino"
    private val prefMesaToken = "mesa_token"
    private val prefMesaNome = "mesa_nome"

    var canaisDiscord by mutableStateOf<List<DiscordVoiceChannel>>(emptyList())
    var canaisDiscordCarregando by mutableStateOf(false)
    var canaisDiscordErro by mutableStateOf<String?>(null)
    var canalDiscordSelecionadoId by mutableStateOf(configPrefs.getString(prefCanalDiscordId, null))
    var canalDiscordSelecionadoNome by mutableStateOf(configPrefs.getString(prefCanalDiscordNome, null))

    /**
     * Para onde as rolagens vão. 🔴 Começa em [DestinoDaRolagem.NENHUM]: um app
     * recém-instalado não pode chutar um servidor que aquele jogador nunca
     * configurou.
     */
    var destinoDaRolagem by mutableStateOf(
        DestinoDaRolagem.de(configPrefs.getString(prefDestino, null))
    )
    /**
     * **O endereco da sala. FIXO NO CODIGO** -- o conserto do MESA-44 que faltava.
     *
     * 🔴 O MESA-44 tirou o campo do endereco da TELA, mas deixou o valor a ser
     * lido das preferencias -- e la nunca houve nada. Num aparelho onde ninguem
     * tinha digitado o endereco antes (um emulador, uma instalacao nova), ele
     * ficava **vazio**, e entao:
     *
     * - `postFicha` saia logo no `if (baseUrl.isBlank())`, calado;
     * - e a ficha nunca chegava a mesa. Medido: pasta `dados/fichas/` vazia no
     *   servidor, treze bonecos sem ficha, e nada em tela nenhuma a dizer porque.
     *
     * ⚠️ Agora ele **nao vem de lado nenhum**: e a constante, sempre. Assim o
     * defeito deixa de ser possivel, em vez de deixar de acontecer.
     */
    val mesaEndereco: String get() = MesaApiClient.ENDERECO_PADRAO

    var mesaToken by mutableStateOf(configPrefs.getString(prefMesaToken, null))

    /**
     * **O nome com que a pessoa ENTRA na mesa**, e nao o do personagem.
     *
     * 🔴 E por ele que a mesa acha o token que ela criou, para lhe colar a
     * ficha (CAMPO-17). O nome do personagem nao serve: na mesa a pessoa e
     * "Rodolfo", e o boneco e que se chama "Aria".
     */
    var mesaNome by mutableStateOf(configPrefs.getString(prefMesaNome, null))

    /** O que impede o envio agora, ou null quando está tudo pronto. */
    val oQueFaltaNoDestino: String?
        get() = ProntidaoDoDestino.oQueFalta(
            destinoDaRolagem, canalDiscordSelecionadoId, mesaEndereco, mesaToken
        )

    fun escolherDestino(destino: DestinoDaRolagem) {
        destinoDaRolagem = destino
        configPrefs.edit().putString(prefDestino, destino.name).apply()
    }

    /**
     * ⚠️ **Sem endereco.** Ele e fixo (ver `mesaEndereco` acima). Recebe-lo aqui
     * era o caminho por onde entrava o vazio.
     */
    fun configurarMesa(token: String?, nome: String? = null) {
        mesaToken = token?.trim()?.uppercase()?.ifBlank { null }
        // ⚠️ O nome NAO vai para maiusculas: ele e comparado byte a byte com o
        // que a pessoa digitou ao entrar na sala, e "RODOLFO" nao casa com
        // "Rodolfo". Ao contrario do token, que a sala compara em maiusculas.
        mesaNome = nome?.trim()?.ifBlank { null }
        configPrefs.edit()
            .putString(prefMesaToken, mesaToken)
            .putString(prefMesaNome, mesaNome)
            .apply()
    }

    fun atualizarCanais() {
        scope.launch {
            canaisDiscordCarregando = true
            canaisDiscordErro = null
            val result = networkDelegate.buscarCanaisDiscord()
            canaisDiscordCarregando = false

            result.fold(
                onSuccess = { channels ->
                    canaisDiscord = channels
                    val selecionadoAtual = canalDiscordSelecionadoId
                    if (!selecionadoAtual.isNullOrBlank()) {
                        channels.firstOrNull { it.id == selecionadoAtual }?.let { canal ->
                            canalDiscordSelecionadoNome = "${canal.guildName} / ${canal.name}"
                            configPrefs.edit().putString(prefCanalDiscordNome, canalDiscordSelecionadoNome).apply()
                        }
                    }
                },
                onFailure = { canaisDiscordErro = it.message }
            )
        }
    }

    fun selecionarCanal(canal: DiscordVoiceChannel?) {
        canalDiscordSelecionadoId = canal?.id
        canalDiscordSelecionadoNome = canal?.let { "${it.guildName} / ${it.name}" }
        configPrefs.edit()
            .putString(prefCanalDiscordId, canalDiscordSelecionadoId)
            .putString(prefCanalDiscordNome, canalDiscordSelecionadoNome)
            .apply()
    }

    /**
     * Manda a rolagem para onde o jogador escolheu.
     *
     * ⚠️ **Uma porta só.** Toda a aba de Rolagem já chama esta função; espalhar
     * `if (destino == MESA)` pelas quatro rotas de envio seria a quinta rota
     * nascendo diferente das outras — que foi exatamente o defeito do canal do
     * Discord na semana passada.
     */
    /**
     * Sobe o retrato do personagem para a Mesa, se a Mesa for o destino.
     *
     * 🔴 Espelha o Discord, que já fazia isto — e por isso a rolagem chegava
     * lá com a cara do personagem e aqui como texto pelado.
     *
     * ⚠️ Best-effort, como o do Discord: nunca segura o salvar da ficha. Ficar
     * sem imagem é um aborrecimento; não salvar a ficha é perder trabalho.
     */
    suspend fun enviarRetratoParaAMesa(personagem: String, imagemDataUri: String): Boolean {
        if (destinoDaRolagem != DestinoDaRolagem.MESA) return false
        val token = mesaToken ?: return false
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            MesaApiClient.postRetrato(mesaEndereco, token, personagem, imagemDataUri).ok
        }
    }

    /**
     * Manda a ficha calculada para a Mesa, se a Mesa for o destino.
     *
     * 🔴 O `autor` e o NOME DE QUEM ESTA NA MESA, e nao o do personagem: e
     * por ele que a Mesa acha o token que a pessoa criou. Mandar o nome do
     * personagem faria a ficha nunca colar em token nenhum -- e o sintoma seria
     * "nao acontece nada", que e o pior de todos.
     *
     * ⚠️ Best-effort, como o retrato: nunca segura o salvar da ficha.
     *
     * ## 🔴 Mas best-effort NAO quer dizer calado
     *
     * Isto devolvia um `Boolean` que ninguem lia. Custou duas rodadas: a ficha
     * nao chegava, e nao havia nada -- nem no aparelho nem no servidor -- a dizer
     * porque. A causa era o endereco vazio, e um `if (baseUrl.isBlank())` la no
     * fundo do cliente.
     *
     * ⚠️ Agora devolve a frase, e quem salva mostra-a. Nao segurar o salvar e
     * uma coisa; esconder o que aconteceu e outra.
     */
    suspend fun enviarFichaParaAMesa(
        nomeNaMesa: String,
        ficha: com.gurps.ficha.model.FichaCalculada
    ): ResultadoDaConexao {
        // ⚠️ Nao ter a Mesa como destino nao e falha: e a escolha de nao mandar.
        // Dizer "erro" aqui poria o jogador de Discord a cacar problema que nao ha.
        if (destinoDaRolagem != DestinoDaRolagem.MESA) {
            return ResultadoDaConexao(true, "")
        }
        val token = mesaToken
            ?: return ResultadoDaConexao(false, "A ficha nao foi para a mesa: falta o token da sala.")
        if (nomeNaMesa.isBlank()) {
            return ResultadoDaConexao(
                false,
                "A ficha nao foi para a mesa: falta \"Seu nome\" na tela de destino da rolagem."
            )
        }
        val r = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            MesaApiClient.postFicha(mesaEndereco, token, nomeNaMesa, ficha)
        }
        return when {
            r.ok -> ResultadoDaConexao(true, "Ficha enviada para a mesa.")
            r.error == "token_da_sala_invalido" -> ResultadoDaConexao(
                false, "A ficha nao foi para a mesa: token errado. Peca o token novo ao Mestre."
            )
            else -> ResultadoDaConexao(
                false, "A ficha nao foi para a mesa: ${r.error ?: "sem detalhes"}"
            )
        }
    }

    suspend fun enviarRolagem(payload: DiscordRollPayload): RollDispatchStatus =
        when (destinoDaRolagem) {
            DestinoDaRolagem.NENHUM ->
                // Não é falha: é a escolha de não enviar. Dizer "erro" faria o
                // jogador caçar problema de rede que não existe.
                RollDispatchStatus(enviado = false, detalhe = "destino_nenhum")

            DestinoDaRolagem.DISCORD -> {
                val result = networkDelegate.enviarRolagemDiscord(payload)
                result.fold(
                    onSuccess = { RollDispatchStatus(enviado = true) },
                    onFailure = { RollDispatchStatus(enviado = false, detalhe = it.message) }
                )
            }

            DestinoDaRolagem.MESA -> enviarParaAMesa(payload)
        }

    /**
     * O mesmo lançamento, no chat da sala.
     *
     * 🔴 **Quem monta a frase é a sala**, e não este app — como já era com o
     * Discord, onde quem monta é o bot.
     *
     * Antes o texto era montado aqui, à mão, e por isso saía SEM as regras de
     * crítico do GURPS: um SUCESSO DECISIVO chegava na Mesa como "sucesso"
     * comum, enquanto no Discord vinha certo. A mesa decidia em cima de uma
     * linha errada.
     *
     * ⚠️ O texto pronto continua sendo enviado junto: uma sala de versão
     * anterior ignora os campos e usa ele. Sem isso, atualizar o app quebraria
     * as salas que ainda não foram atualizadas.
     */
    private suspend fun enviarParaAMesa(payload: DiscordRollPayload): RollDispatchStatus {
        val endereco = mesaEndereco
        val token = mesaToken
        // ⚠️ So o token pode faltar agora: o endereco e fixo no codigo.
        if (token.isNullOrBlank()) {
            return RollDispatchStatus(enviado = false, detalhe = "mesa_nao_configurada")
        }

        val texto = buildString {
            append(payload.testType)
            if (payload.context.isNotBlank()) append(" (").append(payload.context).append(")")
            if (payload.dice.isNotEmpty()) {
                append(" — ").append(payload.dice.joinToString(" ") { "🎲$it" })
                append(" = ").append(payload.total)
            }
            if (payload.outcome.isNotBlank()) append(" → ").append(payload.outcome)
        }

        val resultado = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            MesaApiClient.postRoll(
                endereco,
                MesaRollPayload(
                    token = token,
                    autor = payload.character,
                    // ⚠️ O texto pronto vai junto, para uma sala de versão
                    // anterior não parar de receber rolagem.
                    texto = texto,
                    // 🔴 E os campos, para a sala montar a frase COM as regras
                    // de crítico — que este texto acima não tem.
                    tipo = payload.testType,
                    contexto = payload.context,
                    dados = payload.dice,
                    total = payload.total,
                    resultado = payload.outcome,
                    margem = payload.margin,
                    alvo = payload.target
                )
            )
        }
        return RollDispatchStatus(enviado = resultado.ok, detalhe = resultado.error)
    }

    /** Testa a sala sem mandar rolagem nenhuma, para a tela de configuração. */
    suspend fun testarMesa(): ResultadoDaConexao {
        val endereco = mesaEndereco
        val r = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            // 🔴 Com o token -- MESA-44. Antes isto so perguntava se a sala estava
            // de pe, e dizia "respondeu" com o token errado.
            MesaApiClient.saude(endereco, mesaToken)
        }
        return when {
            r.ok -> ResultadoDaConexao(true, "Conectado. A sala respondeu.")
            // ⚠️ Cada erro com a sua frase, e a do token dizendo O QUE FAZER. "401"
            // nao ajuda ninguem no meio de uma sessao.
            r.error == "token_da_sala_invalido" ->
                ResultadoDaConexao(false, "Token errado. Peça o token novo ao Mestre.")
            else -> ResultadoDaConexao(false, "Não respondeu: ${r.error ?: "sem detalhes"}")
        }
    }
}
