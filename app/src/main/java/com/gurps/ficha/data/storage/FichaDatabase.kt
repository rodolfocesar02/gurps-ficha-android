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
    entities = [FichaEntity::class, ManualChunkEntity::class, GraphNodeEntity::class, ChatSessionEntity::class, ChatMessageEntity::class, VecChunkEntity::class],
    version = 24,  // Lote 259: adiciona tabela vec_chunks para embeddings semânticos
    exportSchema = false
)
abstract class FichaDatabase : RoomDatabase() {
    abstract fun fichaDao(): FichaDao
    abstract fun manualChunkDao(): ManualChunkDao
    abstract fun graphNodeDao(): GraphNodeDao
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun vecChunkDao(): VecChunkDao

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
                        // A pré-população agora é gerenciada pelo FichaIADelegate
                        // para evitar condições de corrida e duplicação.
                    }
                })
                .build().also { INSTANCE = it }
            }
        }

        suspend fun prePopulateManual(context: Context, database: FichaDatabase) {
            try {
                val dao = database.manualChunkDao()
                val totalChunks = dao.getCount()
                android.util.Log.i("MestreIA_Auditoria", "ESTADO DO MANUAL: $totalChunks recortes de páginas (Chunks) carregados.")

                if (totalChunks == 0) {
                    android.util.Log.i("MestreIA_Auditoria", "Iniciando leitura do arquivo chunks.jsonl...")
                    val assets = context.assets
                    val inputStream = assets.open("chunks.jsonl")
                    val reader = inputStream.bufferedReader(Charsets.UTF_8)
                    
                    val chunks = mutableListOf<ManualChunkEntity>()
                    val vecChunks = mutableListOf<VecChunkEntity>()
                    val vecDao = database.vecChunkDao()
                    var totalLido = 0
                    var totalVetores = 0
                    reader.useLines { lines ->
                        lines.forEach { line ->
                            if (line.isNotBlank()) {
                                try {
                                    val obj = org.json.JSONObject(line)
                                    val textLimpo = obj.getString("text")
                                    val sourceTitleRaw = obj.optString("source_title", "")
                                    val sourceTitleNorm = com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(sourceTitleRaw)
                                    val searchTextNorm = com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(textLimpo) + " " + sourceTitleNorm
                                    val chunkId = obj.getString("chunk_id")
                                    chunks.add(ManualChunkEntity(
                                        chunk_id = chunkId,
                                        source_title = obj.getString("source_title"),
                                        source_id = obj.optString("source_id", "pt_modulo_basico"),
                                        page_number = obj.optInt("page_number", 0),
                                        text = textLimpo,
                                        search_text = searchTextNorm
                                    ))
                                    totalLido++

                                    // Lote 259: importa embedding se presente no jsonl
                                    if (obj.has("embedding")) {
                                        val arr = obj.getJSONArray("embedding")
                                        val floats = FloatArray(arr.length()) { arr.getDouble(it).toFloat() }
                                        vecChunks.add(VecChunkEntity(
                                            chunk_id = chunkId,
                                            embedding = com.gurps.ficha.domain.MestreIASemanticEngine.floatArrayToByteArray(floats)
                                        ))
                                        totalVetores++
                                    }

                                    if (chunks.size >= 100) {
                                        dao.insertAll(chunks.toList())
                                        chunks.clear()
                                        android.util.Log.d("MestreIA_Auditoria", "Progresso: $totalLido chunks importados...")
                                    }
                                    if (vecChunks.size >= 100) {
                                        vecDao.insertAll(vecChunks.toList())
                                        vecChunks.clear()
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("MestreIA_Auditoria", "Erro na linha $totalLido: ${e.message}")
                                }
                            }
                        }
                    }
                    if (chunks.isNotEmpty()) dao.insertAll(chunks)
                    if (vecChunks.isNotEmpty()) vecDao.insertAll(vecChunks)
                    android.util.Log.i("MestreIA_Auditoria", "AUDITORIA: Carga concluída! TOTAL: ${dao.getCount()} chunks | $totalVetores embeddings semânticos.")
                }
            } catch (e: Exception) {
                android.util.Log.e("FichaDatabase", "Erro ao importar manual", e)
            }
        }
    }
}
