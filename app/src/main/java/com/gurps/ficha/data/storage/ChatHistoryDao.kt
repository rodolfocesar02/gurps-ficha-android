package com.gurps.ficha.data.storage

import androidx.room.*

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM chat_sessions ORDER BY lastUpdate DESC")
    suspend fun getAllSessions(): List<ChatSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity): Long

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSession(sessionId: Long): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Long)

    @Query("UPDATE chat_sessions SET lastUpdate = :timestamp WHERE id = :sessionId")
    suspend fun updateSessionTimestamp(sessionId: Long, timestamp: Long)

    @Transaction
    suspend fun deleteFullSession(sessionId: Long) {
        deleteMessagesForSession(sessionId)
        deleteSession(sessionId)
    }
}
