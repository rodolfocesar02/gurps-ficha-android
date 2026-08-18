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
    private val prefMesaEndereco = "mesa_endereco"
    private val prefMesaToken = "mesa_token"

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
    var mesaEndereco by mutableStateOf(configPrefs.getString(prefMesaEndereco, null))
    var mesaToken by mutableStateOf(configPrefs.getString(prefMesaToken, null))

    /** O que impede o envio agora, ou null quando está tudo pronto. */
    val oQueFaltaNoDestino: String?
        get() = ProntidaoDoDestino.oQueFalta(
            destinoDaRolagem, canalDiscordSelecionadoId, mesaEndereco, mesaToken
        )

    fun escolherDestino(destino: DestinoDaRolagem) {
        destinoDaRolagem = destino
        configPrefs.edit().putString(prefDestino, destino.name).apply()
    }

    fun configurarMesa(endereco: String?, token: String?) {
        // ⚠️ O endereço é limpo aqui, e não na tela: assim o valor GUARDADO já
        // está pronto, e quem for ler depois não precisa lembrar de limpar.
        mesaEndereco = ProntidaoDoDestino.enderecoLimpo(endereco)
        mesaToken = token?.trim()?.uppercase()?.ifBlank { null }
        configPrefs.edit()
            .putString(prefMesaEndereco, mesaEndereco)
            .putString(prefMesaToken, mesaToken)
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
     * 🔴 Aqui o texto é montado **no app**, e no Discord ele é montado pelo bot.
     * Isso significa que a mesma rolagem pode sair escrita diferente nos dois
     * lugares — o preço de não haver biblioteca comum entre um app Kotlin e um
     * servidor Node. Fica registrado para ninguém descobrir sozinho.
     */
    private suspend fun enviarParaAMesa(payload: DiscordRollPayload): RollDispatchStatus {
        val endereco = mesaEndereco
        val token = mesaToken
        if (endereco.isNullOrBlank() || token.isNullOrBlank()) {
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
            MesaApiClient.postRoll(endereco, MesaRollPayload(token, payload.character, texto))
        }
        return RollDispatchStatus(enviado = resultado.ok, detalhe = resultado.error)
    }

    /** Testa a sala sem mandar rolagem nenhuma, para a tela de configuração. */
    suspend fun testarMesa(): String {
        val endereco = mesaEndereco ?: return "Falta o endereço da sala."
        val r = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            MesaApiClient.saude(endereco)
        }
        return if (r.ok) "A sala respondeu. Está no ar."
        else "Não respondeu: ${r.error ?: "sem detalhes"}"
    }
}
