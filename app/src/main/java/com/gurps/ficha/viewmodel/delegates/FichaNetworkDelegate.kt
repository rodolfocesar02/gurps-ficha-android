package com.gurps.ficha.viewmodel.delegates

import com.gurps.ficha.BuildConfig
import com.gurps.ficha.data.network.DiscordRollApiClient
import com.gurps.ficha.data.network.DiscordRollPayload
import com.gurps.ficha.data.network.MestreIAClient
import com.gurps.ficha.domain.roll.RollDispatchPolicy
import com.gurps.ficha.model.*
import kotlinx.coroutines.*

class FichaNetworkDelegate {

    suspend fun enviarRolagemDiscord(payload: DiscordRollPayload): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val primeiraTentativa = DiscordRollApiClient.postRoll(
                baseUrl = BuildConfig.DISCORD_ROLL_API_BASE_URL,
                apiKey = BuildConfig.DISCORD_ROLL_API_KEY,
                payload = payload
            )
            val resultadoFinal = if (!primeiraTentativa.ok && RollDispatchPolicy.deveRetentar(primeiraTentativa.statusCode)) {
                DiscordRollApiClient.postRoll(
                    baseUrl = BuildConfig.DISCORD_ROLL_API_BASE_URL,
                    apiKey = BuildConfig.DISCORD_ROLL_API_KEY,
                    payload = payload
                )
            } else {
                primeiraTentativa
            }

            if (resultadoFinal.ok) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(RollDispatchPolicy.mensagemErro(resultadoFinal.statusCode, resultadoFinal.error)))
            }
        }
    }

    suspend fun buscarCanaisDiscord(): Result<List<com.gurps.ficha.data.network.DiscordVoiceChannel>> {
        return withContext(Dispatchers.IO) {
            val resultado = DiscordRollApiClient.fetchVoiceChannels(
                baseUrl = BuildConfig.DISCORD_ROLL_API_BASE_URL,
                apiKey = BuildConfig.DISCORD_ROLL_API_KEY
            )
            if (resultado.ok) {
                Result.success(resultado.channels)
            } else {
                Result.failure(Exception(resultado.error ?: "erro_ao_carregar_canais"))
            }
        }
    }

    suspend fun conversarComMestreIA(
        baseUrl: String,
        apiKey: String,
        workspaceSlug: String,
        prompt: String,
        history: List<MestreIAClient.ChatMessage>,
        contextoPersonagem: String,
        catalogo: MestreIAClient.CatalogoNomes,
        modo: String
    ): MestreIAClient.ChatResponse {
        return withContext(Dispatchers.IO) {
            MestreIAClient.perguntarAoMestre(
                baseUrl = baseUrl,
                apiKey = apiKey,
                workspaceSlug = workspaceSlug,
                prompt = prompt,
                history = history.map { it.role to it.text },
                contextoPersonagem = contextoPersonagem,
                catalogo = catalogo,
                modo = modo
            )
        }
    }

    suspend fun salvarFichaNuvem(deviceId: String, characterName: String, fichaJson: Any): Boolean {
        return withContext(Dispatchers.IO) {
            DiscordRollApiClient.postFicha(
                baseUrl = BuildConfig.DISCORD_ROLL_API_BASE_URL,
                apiKey = BuildConfig.DISCORD_ROLL_API_KEY,
                deviceId = deviceId,
                characterName = characterName,
                fichaJson = fichaJson
            )
        }
    }

    suspend fun buscarFichasNuvem(deviceId: String): List<String> {
        return withContext(Dispatchers.IO) {
            DiscordRollApiClient.fetchFichaList(
                baseUrl = BuildConfig.DISCORD_ROLL_API_BASE_URL,
                apiKey = BuildConfig.DISCORD_ROLL_API_KEY,
                deviceId = deviceId
            )
        }
    }

    suspend fun baixarFichaNuvem(deviceId: String, characterName: String): String? {
        return withContext(Dispatchers.IO) {
            DiscordRollApiClient.fetchFicha(
                baseUrl = BuildConfig.DISCORD_ROLL_API_BASE_URL,
                apiKey = BuildConfig.DISCORD_ROLL_API_KEY,
                deviceId = deviceId,
                characterName = characterName
            )
        }
    }
}
