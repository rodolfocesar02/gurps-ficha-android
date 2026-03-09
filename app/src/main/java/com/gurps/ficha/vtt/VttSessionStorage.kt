package com.gurps.ficha.vtt

import android.content.Context

data class VttSessionSnapshot(
    val serverUrl: String = "",
    val roomKey: String = "",
    val playerId: String = "",
    val sessionId: String = "",
    val tokenId: String = ""
)

object VttSessionStorage {
    private const val PREF_NAME = "vtt_session_pref"
    private const val KEY_SERVER_URL = "server_url"
    private const val KEY_ROOM_KEY = "room_key"
    private const val KEY_PLAYER_ID = "player_id"
    private const val KEY_SESSION_ID = "session_id"
    private const val KEY_TOKEN_ID = "token_id"

    fun load(context: Context): VttSessionSnapshot {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return VttSessionSnapshot(
            serverUrl = pref.getString(KEY_SERVER_URL, "").orEmpty(),
            roomKey = pref.getString(KEY_ROOM_KEY, "").orEmpty(),
            playerId = pref.getString(KEY_PLAYER_ID, "").orEmpty(),
            sessionId = pref.getString(KEY_SESSION_ID, "").orEmpty(),
            tokenId = pref.getString(KEY_TOKEN_ID, "").orEmpty()
        )
    }

    fun save(context: Context, snapshot: VttSessionSnapshot) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_SERVER_URL, snapshot.serverUrl)
            .putString(KEY_ROOM_KEY, snapshot.roomKey)
            .putString(KEY_PLAYER_ID, snapshot.playerId)
            .putString(KEY_SESSION_ID, snapshot.sessionId)
            .putString(KEY_TOKEN_ID, snapshot.tokenId)
            .apply()
    }
}

