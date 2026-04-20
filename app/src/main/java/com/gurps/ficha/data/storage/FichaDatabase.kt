package com.gurps.ficha.data.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FichaEntity::class, ManualChunkEntity::class, GraphNodeEntity::class],
    version = 12,
    exportSchema = false
)
abstract class FichaDatabase : RoomDatabase() {
    abstract fun fichaDao(): FichaDao
    abstract fun manualChunkDao(): ManualChunkDao
    abstract fun graphNodeDao(): GraphNodeDao

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
                        }
                    }
                })
                .build().also { INSTANCE = it }
            }
        }

        private suspend fun prePopulateGraph(context: Context, database: FichaDatabase) {
            try {
                val dao = database.graphNodeDao()
                if (dao.countNodes() == 0) {
                    android.util.Log.i("FichaDatabase", "Iniciando pré-população do Grafo de Conhecimento...")
                    val jsonString = context.assets.open("graph_db/graph_knowledge.json").bufferedReader().use { it.readText() }
                    val jsonArray = org.json.JSONArray(jsonString)
                    
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        dao.insertNode(GraphNodeEntity(
                            entityId = obj.getString("entity_id"),
                            title = obj.getString("title"),
                            summary = obj.getString("summary"),
                            category = obj.getString("category"),
                            level = obj.optInt("level", 0)
                        ))
                    }
                    android.util.Log.i("FichaDatabase", "Grafo pré-populado com sucesso!")
                }
            } catch (e: Exception) {
                android.util.Log.e("FichaDatabase", "Erro ao pré-popular Grafo", e)
            }
        }
    }
}
