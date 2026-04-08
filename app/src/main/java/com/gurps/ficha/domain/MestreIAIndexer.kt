package com.gurps.ficha.domain

import android.content.Context
import com.google.gson.Gson
import com.gurps.ficha.data.storage.FichaDatabase
import com.gurps.ficha.data.storage.ManualChunkEntity
import com.gurps.ficha.model.MestreIAChunk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Motor de Indexação (Lote 56).
 * Processa o arquivo JSONL e salva no SQLite FTS para busca instantânea.
 */
object MestreIAIndexer {
    
    suspend fun indexarSeNecessario(context: Context) = withContext(Dispatchers.IO) {
        val database = FichaDatabase.getInstance(context)
        val dao = database.manualChunkDao()
        
        // Se já existem itens, não re-indexa para economizar bateria/cpu
        if (dao.getCount() > 0) return@withContext

        android.util.Log.d("MestreIA_Indexer", "Iniciando indexação inicial dos manuais...")
        
        val gson = Gson()
        try {
            val inputStream = context.assets.open("chunks.jsonl")
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            val batchSize = 100
            val buffer = mutableListOf<ManualChunkEntity>()
            
            reader.lineSequence().forEach { line ->
                try {
                    val chunk = gson.fromJson(line, MestreIAChunk::class.java)
                    buffer.add(ManualChunkEntity(
                        source_title = chunk.source_title,
                        page_number = chunk.page_number,
                        text = chunk.text
                    ))
                    
                    if (buffer.size >= batchSize) {
                        dao.insertAll(buffer.toList())
                        buffer.clear()
                    }
                } catch (e: Exception) {
                    // Pula linhas malformadas
                }
            }
            
            if (buffer.isNotEmpty()) {
                dao.insertAll(buffer)
            }
            
            android.util.Log.d("MestreIA_Indexer", "Indexação concluída com sucesso!")
        } catch (e: Exception) {
            android.util.Log.e("MestreIA_Indexer", "Erro na indexação: ${e.message}")
        }
    }
}
