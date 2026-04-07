package com.gurps.ficha.viewmodel.delegates

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gurps.ficha.data.network.DiscordRollPayload
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

    var canaisDiscord by mutableStateOf<List<DiscordVoiceChannel>>(emptyList())
    var canaisDiscordCarregando by mutableStateOf(false)
    var canaisDiscordErro by mutableStateOf<String?>(null)
    var canalDiscordSelecionadoId by mutableStateOf(configPrefs.getString(prefCanalDiscordId, null))
    var canalDiscordSelecionadoNome by mutableStateOf(configPrefs.getString(prefCanalDiscordNome, null))

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

    suspend fun enviarRolagem(payload: DiscordRollPayload): RollDispatchStatus {
        val result = networkDelegate.enviarRolagemDiscord(payload)
        return result.fold(
            onSuccess = { RollDispatchStatus(enviado = true) },
            onFailure = { RollDispatchStatus(enviado = false, detalhe = it.message) }
        )
    }
}
