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
    version = 21,
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
                        // A pré-população agora é gerenciada pelo FichaIADelegate
                        // para evitar condições de corrida e duplicação.
                    }
                })
                .build().also { INSTANCE = it }
            }
        }

        private const val TECNICA_PURIFICACAO_LOTE = 110

        suspend fun prePopulateGraph(context: Context, database: FichaDatabase) {
            try {
                val graphDao = database.graphNodeDao()
                val manualDao = database.manualChunkDao()
                
                // Lote 108.Purificação: Se o banco já existe mas os dados estão corrompidos (mojibake), 
                // limpamos as tabelas técnicas sem tocar nas fichas dos usuários.
                val manualCount = manualDao.getCount()
                val needsPurification = true // Forçamos uma vez para limpar o Mojibake do Lote 109
                
                if (manualCount > 0 && needsPurification) {
                    android.util.Log.w("FichaDatabase", "PURIFICAÇÃO LOTE $TECNICA_PURIFICACAO_LOTE: Limpando tabelas do Códex para corrigir encoding...")
                    graphDao.clearAll()
                    manualDao.clearAll()
                    // Resetamos o contador para entrar no bloco de importação abaixo
                }

                // Lote 111.Purificação: Força re-importação total para cura de DNA (Colisão e UTF-8)
                if (needsPurification && graphDao.countNodes() > 0) {
                    android.util.Log.w("FichaDatabase", "PURIFICAÇÃO GRAFO LOTE 111: Resetando grafo para cura de DNA...")
                    graphDao.clearAll()
                }

                val count = graphDao.countNodes()
                android.util.Log.i("MestreIA_Auditoria", "ESTADO DO CÓDEX: $count nós carregados no banco de dados.")
                
                if (count == 0) {
                    // Lote 111.Fix: Arquivos confirmados como UTF-8 íntegros no disco.
                    val jsonString = context.assets.open("graph_db/graph_knowledge.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val jsonArray = org.json.JSONArray(jsonString)
                    val nodes = mutableListOf<GraphNodeEntity>()
                    
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        nodes.add(GraphNodeEntity(
                            entityId = obj.getString("entity_id"),
                            title = obj.getString("title"),
                            level = obj.getInt("level"),
                            summary = obj.optString("summary", ""),
                            category = obj.optString("category", "Geral"),
                            source_id = obj.optString("source_id", "pt_modulo_basico"),
                            search_text = com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca("${obj.getString("title")} ${obj.optString("summary", "")}")
                        ))
                    }
                    graphDao.insertAll(nodes)
                    android.util.Log.i("MestreIA_Auditoria", "AUDITORIA: Carga concluída com sucesso! ${nodes.size} nós inseridos via Bulk Insert.")
                }
            } catch (e: Exception) {
                android.util.Log.e("FichaDatabase", "Erro ao pré-popular Grafo", e)
            }
        }

        suspend fun prePopulateManual(context: Context, database: FichaDatabase) {
            try {
                val dao = database.manualChunkDao()
                
                // Lote 111.Purificação: Limpeza do Manual para sincronia com o Grafo curado
                val needsPurification = true 
                val currentCount = dao.getCount()
                
                if (currentCount > 0 && needsPurification) {
                    android.util.Log.w("FichaDatabase", "PURIFICAÇÃO MANUAL LOTE 110: Limpando chunks...")
                    dao.clearAll()
                }

                val totalChunks = dao.getCount()
                android.util.Log.i("MestreIA_Auditoria", "ESTADO DO MANUAL: $totalChunks recortes de páginas (Chunks) carregados.")

                if (totalChunks == 0) {
                    android.util.Log.i("FichaDatabase", "Iniciando importação do manual (Chunks)...")
                    val assets = context.assets
                    val inputStream = assets.open("chunks.jsonl")
                    val reader = inputStream.bufferedReader(Charsets.UTF_8)
                    
                    val chunks = mutableListOf<ManualChunkEntity>()
                    reader.useLines { lines ->
                        lines.forEach { line ->
                            if (line.isNotBlank()) {
                                val obj = org.json.JSONObject(line)
                                val textLimpo = obj.getString("text")
                                val searchTextNorm = com.gurps.ficha.domain.filters.CatalogFilters.normalizarBusca(textLimpo)
                                chunks.add(ManualChunkEntity(
                                    chunk_id = obj.getString("chunk_id"),
                                    source_title = obj.getString("source_title"),
                                    source_id = obj.optString("source_id", "pt_modulo_basico"),
                                    page_number = obj.optInt("page_number", 0),
                                    text = textLimpo,
                                    search_text = searchTextNorm // FTS4 field blindado contra acentos
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
