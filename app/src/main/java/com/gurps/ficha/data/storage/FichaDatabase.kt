package com.gurps.ficha.data.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gurps.ficha.domain.loaders.fixMojibakeIfNeeded
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FichaEntity::class, ManualChunkEntity::class, GraphNodeEntity::class, ChatSessionEntity::class, ChatMessageEntity::class],
    version = 16,
    exportSchema = false
)
abstract class FichaDatabase : RoomDatabase() {
    abstract fun fichaDao(): FichaDao
    abstract fun manualChunkDao(): ManualChunkDao
    abstract fun graphNodeDao(): GraphNodeDao
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: FichaDatabase? = null

        fun getInstance(context: Context): FichaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FichaDatabase::class.java,
                    "gurps_fichas.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Lógica de pré-população assíncrona
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            prePopulateGraph(context, getInstance(context))
                            prePopulateManual(context, getInstance(context))
                        }
                    }
                })
                .build().also { INSTANCE = it }
            }
        }

        suspend fun prePopulateGraph(context: Context, database: FichaDatabase) {
            try {
                val dao = database.graphNodeDao()
                val count = dao.countNodes()
                android.util.Log.i("MestreIA_Auditoria", "ESTADO DO CÓDEX: $count nós carregados no banco de dados.")
                
                if (count == 0) {
                    android.util.Log.i("MestreIA_Auditoria", "AUDITORIA: Banco de dados vazio. Iniciando leitura de 'assets/graph_db/graph_knowledge.json'...")
                    val jsonString = context.assets.open("graph_db/graph_knowledge.json").bufferedReader().use { it.readText() }
                        .fixMojibakeIfNeeded()
                    val jsonArray = org.json.JSONArray(jsonString)
                    val nodes = mutableListOf<GraphNodeEntity>()
                    
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        nodes.add(GraphNodeEntity(
                            entityId = obj.getString("entity_id"),
                            title = obj.getString("title"),
                            summary = obj.getString("summary"),
                            category = obj.getString("category"),
                            level = obj.optInt("level", 0)
                        ))
                    }
                    dao.insertAll(nodes)
                    android.util.Log.i("MestreIA_Auditoria", "AUDITORIA: Carga concluída com sucesso! ${nodes.size} nós inseridos via Bulk Insert.")
                }
            } catch (e: Exception) {
                android.util.Log.e("FichaDatabase", "Erro ao pré-popular Grafo", e)
            }
        }

        suspend fun prePopulateManual(context: Context, database: FichaDatabase) {
            try {
                val dao = database.manualChunkDao()
                val totalChunks = dao.getCount()
                android.util.Log.i("MestreIA_Auditoria", "ESTADO DO MANUAL: $totalChunks recortes de páginas (Chunks) carregados.")

                if (totalChunks == 0) {
                    android.util.Log.i("FichaDatabase", "Iniciando importação do manual (Chunks)...")
                    val assets = context.assets
                    val inputStream = assets.open("chunks.jsonl")
                    val reader = inputStream.bufferedReader()
                    
                    val chunks = mutableListOf<ManualChunkEntity>()
                    reader.useLines { lines ->
                        lines.forEach { line ->
                            if (line.isNotBlank()) {
                                val obj = org.json.JSONObject(line)
                                val textLimpo = obj.getString("text").fixMojibakeIfNeeded()
                                chunks.add(ManualChunkEntity(
                                    chunk_id = obj.getString("chunk_id"),
                                    source_title = obj.getString("source_title"),
                                    page_number = obj.optInt("page_number", 0),
                                    text = textLimpo,
                                    search_text = textLimpo // FTS4 field
                                ))
                                
                                if (chunks.size >= 100) {
                                    dao.insertAll(chunks.toList())
                                    chunks.clear()
                                }
                            }
                        }
                    }
                    if (chunks.isNotEmpty()) {
                        dao.insertAll(chunks)
                    }
                    android.util.Log.i("MestreIA_Auditoria", "AUDITORIA: Manual importado com sucesso! ${dao.getCount()} chunks ativos.")
                }
            } catch (e: Exception) {
                android.util.Log.e("FichaDatabase", "Erro ao importar manual", e)
            }
        }
    }
}
