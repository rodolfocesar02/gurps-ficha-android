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
    entities = [FichaEntity::class, ManualChunkEntity::class, GraphNodeEntity::class, ChatSessionEntity::class, ChatMessageEntity::class, VecChunkEntity::class,
        CampanhaEntity::class, CenaEntity::class, CampaignFactEntity::class, WorldStateEntity::class],
    version = 25,  // Lote 353 (Saga A4): tabelas da campanha (campanhas, cenas, campaign_facts FTS4, world_state)
    exportSchema = false
)
abstract class FichaDatabase : RoomDatabase() {
    abstract fun fichaDao(): FichaDao
    abstract fun manualChunkDao(): ManualChunkDao
    abstract fun graphNodeDao(): GraphNodeDao
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun vecChunkDao(): VecChunkDao
    abstract fun sagaDao(): SagaDao

    companion object {
        @Volatile
        private var INSTANCE: FichaDatabase? = null

        /**
         * Lote 353: PRIMEIRA migração explícita do projeto. O "padrão anterior" era
         * fallbackToDestructiveMigration (bump de versão APAGAVA o banco — fichas
         * sobreviviam pela sincronização em nuvem). Como a 24→25 é puramente ADITIVA,
         * preservamos tudo criando só as 4 tabelas novas. O SQL espelha EXATAMENTE o
         * createAllTables gerado pelo Room (FichaDatabase_Impl) — divergência = crash
         * de validação na abertura.
         */
        val MIGRATION_24_25 = object : androidx.room.migration.Migration(24, 25) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `campanhas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, `cenarioId` TEXT NOT NULL, `personagemId` TEXT NOT NULL, `criadaEm` INTEGER NOT NULL, `capituloAtual` INTEGER NOT NULL, `resumoCapitulo` TEXT NOT NULL, `tempoJogoMin` INTEGER NOT NULL, `seedMundo` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `cenas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `campanhaId` INTEGER NOT NULL, `indice` INTEGER NOT NULL, `titulo` TEXT NOT NULL, `resumo` TEXT NOT NULL, `bioma` TEXT NOT NULL, `humor` TEXT NOT NULL, `fechadaEm` INTEGER)")
                db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `campaign_facts` USING FTS4(`campanhaId` INTEGER NOT NULL, `sujeito` TEXT NOT NULL, `predicado` TEXT NOT NULL, `objeto` TEXT NOT NULL, `peso` INTEGER NOT NULL, `cenaId` INTEGER, `texto` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `world_state` (`campanhaId` INTEGER NOT NULL, `climaPorRegiaoJson` TEXT NOT NULL, `relogiosJson` TEXT NOT NULL, `ecologiaJson` TEXT NOT NULL, `economiaJson` TEXT NOT NULL, `ultimoTickMin` INTEGER NOT NULL, PRIMARY KEY(`campanhaId`))")
            }
        }

        fun getInstance(context: Context): FichaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FichaDatabase::class.java,
                    "gurps_fichas.db"
                )
                .addMigrations(MIGRATION_24_25)
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
                val vecDao = database.vecChunkDao()
                val totalChunks = dao.getCount()
                val totalVecExistente = vecDao.getCount()
                android.util.Log.i("MestreIA_Auditoria", "ESTADO DO MANUAL: $totalChunks chunks | $totalVecExistente embeddings no banco.")

                // Reimporta embeddings se chunks existem mas vec_chunks está vazio
                if (totalChunks > 0 && totalVecExistente == 0) {
                    android.util.Log.i("MestreIA_Auditoria", "EMBEDDINGS AUSENTES: Reimportando embeddings do chunks.jsonl...")
                    val assets = context.assets
                    val reader = assets.open("chunks.jsonl").bufferedReader(Charsets.UTF_8)
                    val vecChunks = mutableListOf<VecChunkEntity>()
                    var totalVetores = 0
                    reader.useLines { lines ->
                        lines.forEach { line ->
                            if (line.isNotBlank()) {
                                try {
                                    val obj = org.json.JSONObject(line)
                                    if (obj.has("embedding")) {
                                        val arr = obj.getJSONArray("embedding")
                                        val floats = FloatArray(arr.length()) { arr.getDouble(it).toFloat() }
                                        vecChunks.add(VecChunkEntity(
                                            chunk_id = obj.getString("chunk_id"),
                                            embedding = com.gurps.ficha.domain.MestreIASemanticEngine.floatArrayToByteArray(floats)
                                        ))
                                        totalVetores++
                                        if (vecChunks.size >= 100) {
                                            vecDao.insertAll(vecChunks.toList())
                                            vecChunks.clear()
                                        }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("MestreIA_Auditoria", "Erro embedding: ${e.message}")
                                }
                            }
                        }
                    }
                    if (vecChunks.isNotEmpty()) vecDao.insertAll(vecChunks)
                    android.util.Log.i("MestreIA_Auditoria", "EMBEDDINGS: $totalVetores embeddings semânticos importados.")
                }

                if (totalChunks == 0) {
                    android.util.Log.i("MestreIA_Auditoria", "Iniciando leitura do arquivo chunks.jsonl...")
                    val assets = context.assets
                    val inputStream = assets.open("chunks.jsonl")
                    val reader = inputStream.bufferedReader(Charsets.UTF_8)
                    
                    val chunks = mutableListOf<ManualChunkEntity>()
                    val vecChunks = mutableListOf<VecChunkEntity>()
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
